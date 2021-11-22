package org.genevaers.ccb2lr;

import java.util.ArrayList;
import java.util.List;

public class CobolCollection {
    
	private List<CobolField> fields = new ArrayList<>();
	private CobolField currentField;
	private GroupField recordGroup;
	private boolean expansionRequired;

    public void addCobolField(CobolField newField) {
		if(currentField == null) {
			recordGroup = (GroupField) newField;
		} else {
			if(newField.getSection() == currentField.getSection()) {
				currentField.addSibling(newField);
			} else if ( newField.getSection() > currentField.getSection()) {
				currentField.addChild(newField);
			} else {
				CobolField s = findSibling(newField);
				s.addSibling(newField);
			}
		}
		if(newField.getType() == FieldType.OCCURSGROUP) {
			expansionRequired = true;
		}
		fields.add(newField);
		currentField = newField;
	}

	private CobolField findSibling(CobolField newField) {
		//section < than currentGroup - Find the LAST match not the first
		//CobolField grp = currentField;
		CobolField prnt = currentField.getParent();
		while(prnt != null && prnt.getSection() != newField.getSection() ) {
			// if(prnt.getType() == FieldType.GROUP || prnt.getType() == FieldType.OCCURSGROUP) {
			// 	grp = prnt;
			// }
			prnt = prnt.getParent();
		}
		// CobolField n = currentGroup.parent;
		// while (s <=  n.getSection()) {
		// 	if(n.getType() == FieldType.GROUP || n.getType() == FieldType.OCCURSGROUP) {
		// 		currentGroup = (GroupField) n;
		// 	}
		// 	n = n.next();
		// }
		return prnt;
	}

    public List<CobolField> getFields() {
        return fields;
    }

    public GroupField getRecordGroup() {
        return recordGroup;
    }

	public void resolvePositions() {
		if(expansionRequired) {
			expandOccursGroups();
		}
			// int pos = 1;
			// CobolField c = firstChild;
			// while(c != null) {
			// 	pos = c.resolvePosition(pos);
			// 	c = c.next();
			// }
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
            GroupField newMe = (GroupField) CobolFieldFactory.makeNamelessFieldFrom(og);
            String ext = parentExt + "-" + String.format("%02d", t);
            String newName = og.getName() + ext;
            newMe.setName(newName);

            CobolField child = og.next(); //this will be our first child
            //What if the child is itself an occurs group
            while(child != null) {

				// child could be a group
				// so we will need to go into it
                if(child.getType() == FieldType.OCCURSGROUP) {
					expandOccursGroup(((OccursGroup)child), ext);
                } else {
                    CobolField newChild = CobolFieldFactory.makeNamelessFieldFrom(child);
                    String newChildName = child.getName() + ext;
                    newChild.setName(newChildName);
                    newMe.addChild(newChild);
                }
                child = child.getNextSibling();
            }

            if(t == 1) {
                if(connectSibling) {
                    og.getPreviousSibling().setNextSibling(newMe);
                    newMe.setPreviousSibling(og.getPreviousSibling());
                } else {
                    og.getParent().addChild(newMe);
                    newMe.parent = og.getParent();
                }
                connectToMe = newMe;
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
            connectToMe.nextSibling = og.getNextSibling();
			og.getNextSibling().setPreviousSibling(connectToMe);
        }
	}
}
