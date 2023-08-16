package org.genevaers.ccb2lr;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class CobolCollection {
    
	private Map<String, CobolField> fields = new HashMap<>();
	private Map<String, CobolField> redefinedFields = new LinkedHashMap<>();
	private CobolField currentField;
	private CobolField currentRedefineField;
	private GroupField recordGroup;
	private int expansionsRequired = 0;

    public void addCobolField(CobolField newField) {
		if(currentField == null) {
			recordGroup = (GroupField) newField;
			currentField = newField;
		} else {
			if(newField.isRedefines()) {
				insertRedefines(newField);
				currentRedefineField = newField;
			} else {
				insert(newField);
				currentField = newField;
			}
		}
	}

	private void insertRedefines(CobolField newField) {
		if(currentRedefineField == null) {
			currentRedefineField = newField;
		}
		if(currentRedefineField.isRedefines() && newField.getType() != FieldType.GROUP) { //redefined groups float
			addChildOrSiblingTo(newField, currentRedefineField);
		}
		newField.setRedefinedField(fields.get(newField.getRedefinedName()));
		redefinedFields.put(newField.getName(), newField);
	}

	private void insert(CobolField newField) {
		addChildOrSiblingTo(newField, currentField);
		fields.put(newField.getName(), newField);
	}

	private void addChildOrSiblingTo(CobolField newField, CobolField srcField)  {
		if(newField.getSection() == srcField.getSection()) {
			srcField.addSibling(newField);
		} else if ( newField.getSection() > srcField.getSection()) {
			srcField.addChild(newField);
		} else {
			CobolField s = findSiblingOf(newField, srcField);
			if(s != null) {
				s.addSibling(newField);
			}
		}
	}

	private CobolField findSiblingOf(CobolField newField, CobolField srcField) {
		CobolField prnt = srcField.getParent();
		while(prnt != null && prnt.getSection() != newField.getSection() ) {
			prnt = prnt.getParent();
		}
		return prnt;
	}

    public Collection<CobolField> getFields() {
        return fields.values();
    }

    public GroupField getRecordGroup() {
        return recordGroup;
    }

	public void resolvePositions() {
		int pos = 1;
		CobolField c = recordGroup.getFirstChild();
		resolveChildPositions(pos, c);
		resolveRedefinedPostions();
	}

	private void resolveChildPositions(int pos, CobolField c) {
		while(c != null) {
			pos = c.resolvePosition(pos);
			c = c.next();
		}
	}

	private void resolveRedefinedPostions() {
		Iterator<CobolField> ri = redefinedFields.values().iterator();
		while(ri.hasNext()) {
			CobolField r = ri.next();
			CobolField f = fields.get(r.getRedefinedName());
			if(r.getType() == FieldType.GROUP) {
				GroupField grp = (GroupField) r; 
				r.setPosition(f.getPosition());
				resolveChildPositions(f.getPosition(), grp.getFirstChild());
			} else {
				if(f != null) {
					r.setPosition(f.getPosition());
				}
			}
		}
	}

	public void expandOccursGroupsIfNeeded() {
		countExpansionsRequired();
		int e = 1;
		while(expansionsRequired > 0) {
			expandOccursGroups();
			refreshFields();
			countExpansionsRequired();
			e++;
		}
		refreshFields();
	}

	private void countExpansionsRequired() {
		expansionsRequired = 0;
		CobolField c = recordGroup.getFirstChild();
		while(c != null) {
			if(c.getType() == FieldType.OCCURSGROUP) {
				expansionsRequired++;
			}
			c = c.next();
		}
	}

	private void refreshFields() {
		fields.clear();
		fields.put(recordGroup.getName(), recordGroup);
		CobolField n = recordGroup.next();
		while(n != null) {
			fields.put(n.getName(), n);
			n = n.next();
		}
	}

	private void expandOccursGroups() {
		CobolField n = recordGroup.next();
		while(n != null) {
			if(n.getType() == FieldType.OCCURSGROUP) {
				expandOccursGroup((OccursGroup)n, "");
				n = n.getNextSibling();
			} else {
				n = n.next();
			}
		}
	}

	private void expandOccursGroup(OccursGroup og, String parentExt) {
        boolean connectSibling = false;
        if(og.getPreviousSibling() != null) {
            connectSibling = true;
        }
        CobolField connectToMe = null;
        for(int t=1; t<=og.getTimes(); t++) {
			String ext = parentExt + "-" + String.format("%02d", t);
			OccursGroup newMe = makeNewMe(og, t, ext);
			copyAndRenameOriginalChildren(og, newMe, ext);

            if(t == 1) {
                connectToMe = linkFirstExpandedField(og, connectSibling, newMe);
            } else {
                //we are a sibling of the first newME
                if(connectToMe != null) {
                    connectToMe.setNextSibling(newMe);
                    newMe.setPreviousSibling(connectToMe);
                    connectToMe = newMe;
                }
            }
        }
        if(connectToMe != null) {
			if(og.getNextSibling() != null) {
            	connectToMe.nextSibling = og.getNextSibling();
				og.getNextSibling().setPreviousSibling(connectToMe);
			} else if(og.getParent() != null) {
				connectToMe.parent = og.getParent();
			}
        }
	}

	private CobolField linkFirstExpandedField(OccursGroup og, boolean connectSibling, OccursGroup newMe) {
		CobolField connectToMe;
		if(connectSibling) {
		    og.getPreviousSibling().setNextSibling(newMe);
		    newMe.setPreviousSibling(og.getPreviousSibling());
		} else {
		    og.getParent().replaceFirstChild(newMe); 
		    newMe.parent = og.getParent();
		}
		connectToMe = newMe;
		return connectToMe;
	}

	private OccursGroup makeNewMe(OccursGroup og, int t, String ext) {
		OccursGroup newMe = (OccursGroup) CobolFieldFactory.makeNamelessFieldFrom(og);
		String newName = og.getName() + ext;
		newMe.setName(newName);
		newMe.resetOccurs();
		return newMe;
}		

	private void copyAndRenameOriginalChildren(GroupField origin, GroupField newMe, String ext) {
		CobolField ochild = origin.getFirstChild();
		
		while(ochild != null) {
			CobolField newChild = makeNewChild(ochild, ext);
			newMe.addChild(newChild);
			if(ochild.getType() == FieldType.OCCURSGROUP || ochild.getType() == FieldType.GROUP) {
				copyAndRenameOriginalChildren((GroupField)ochild, (GroupField)newChild, ext);
			} else {
				CobolField sib = makeNewChild(newChild, ext);
				ochild.addChild(sib);
			}
			ochild = ochild.getNextSibling();
		}
	}

	private CobolField makeNewChild(CobolField child, String ext) {
		CobolField newChild = CobolFieldFactory.makeNamelessFieldFrom(child);
		String newChildName = child.getName() + ext;
		newChild.setName(newChildName);
		return newChild;
	}

	public int getNumberOfFields() {
		return fields.size() + redefinedFields.size();
	}

	public  CobolField getNamedRedefine(String name) {
		return redefinedFields.get(name);
	}

    public  Iterator<CobolField> getRedifinesIterator() {
		return redefinedFields.values().iterator();
    }

	public Collection<CobolField> getRedefines() {
		return redefinedFields.values();
	}
}
