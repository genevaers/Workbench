package com.ibm.safr.we.model.diff;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

public abstract class DiffNode extends DiffBaseNode {

    public enum MetaType {
		USER_EXIT_ROUTINES("User Exit Routines"),
		CONTROL_RECORDS("Control Records"),
		PHYSICAL_FILES("Physical Files"),
		LOGICAL_FILES("Logical Files"),
		LOGICAL_RECORDS("Logical Records"),
		LOOKUP_PATHS("Lookup Paths"),
        VIEWS("Views"),
        VIEW_FOLDERS("View Folder");
		
		private String name;
		
		MetaType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
		
	protected static Map<Class<?>, Map<Integer, DiffNodeComp>> generated = new HashMap<Class<?>, Map<Integer, DiffNodeComp>>();
	protected static DiffNodeLabel metadata = null;
	protected static boolean related = true;
    protected static boolean weFields = true;

    public static void initGenerated() {
        generated.clear();
        metadata = new DiffNodeLabel("Metadata");
    }

    public static DiffNodeComp getGenerated(Class<?> cl, Integer id) {
        Map<Integer, DiffNodeComp> entry = generated.get(cl);
        if (entry == null) {
            return null;            
        }
        else {
            return entry.get(id);
        }       
    }
    
    public static void storeGenerated(Class<?> cl, Integer id, DiffNodeComp node) {
        Map<Integer, DiffNodeComp> entry = generated.get(cl);
        if (entry == null) {
            entry = new HashMap<Integer, DiffNodeComp>();
            generated.put(cl, entry);
        }
        entry.put(id, node);
    }        
	
    public static void setRelated(boolean relate) {
        related = relate;
    }

    public static void setWEFields(boolean weField) {
        weFields = weField;
    }
    
    public static boolean isRelated() {
        return related;
    }

    public static boolean isWeFields() {
        return weFields;
    }
    
	protected List<DiffNode> children = null;
	
	protected int calcInsertIndex(MetaType type) {
		int pos=0;
		List<DiffNode> children = metadata.getChildren();
		if (children.isEmpty()) {
			return 0;
		}
		for (MetaType mType : MetaType.values()) {
			if (pos >= children.size()) {
				break;
			}
			if (mType.equals(type)) {
				break;
			}
			String cname = children.get(pos).getName();
			if (mType.getName().equals(cname)) {
				pos++;
			}
		}
		return pos;
	}
	
	protected void addMetadataNode(MetaType type, DiffNodeComp node) {
		DiffNodeLabel mnode = (DiffNodeLabel)metadata.getChild(type.getName()); 
		if (mnode == null) {
			mnode = new DiffNodeLabel(type.getName());
			metadata.addChild(mnode, calcInsertIndex(type));
			mnode.setParent(metadata);
			mnode.setState(DiffNodeState.Same);
		}
		mnode.addChild(node);
		node.setParent(mnode);
		if (node.getState() != DiffNodeState.Same) {
		    mnode.setState(DiffNodeState.Different);
		    metadata.setState(DiffNodeState.Different);
		}
	}

	protected List<DiffFieldReference> diffReferences(
	    MetaType type,
	    String name, 
	    List<SAFREnvironmentalComponent> lhsList, 
	    List<SAFREnvironmentalComponent> rhsList,
	    int lhsEnvId,
	    int rhsEnvId) throws SAFRException
	{
		List<DiffFieldReference> result = new ArrayList<DiffFieldReference>(); 
		// loop lhs
		for (SAFREnvironmentalComponent lhsComp : lhsList) {
			// for each lhs comp check whether in rhs
			boolean found = false;
			for (SAFREnvironmentalComponent rhsComp : rhsList) {
				// we have a match
				if (lhsComp.getId().equals(rhsComp.getId())) {
					found = true;
                    DiffFieldReference ref = new DiffFieldReference();
					if (related) {
					    ref.setReference(DiffNodeFactory.generateDiffComp(lhsComp, rhsComp));
					}
					else {
					    ref.setId(lhsComp.getId());
					}
					ref.setName(name);
					ref.setParent(this);
					ref.setState(DiffNodeState.Same);
					result.add(ref);
					break;
				}
			}
			// if not found then reference has been removed
			if (!found) {
                // add referenced component			    
			    SAFREnvironmentalComponent rhsComp = DiffNodeFactory.generateComp(type, lhsComp.getId(), rhsEnvId);
                DiffFieldReference ref = new DiffFieldReference();
			    if (related) {
    			    if (rhsComp == null) {
    			        ref.setReference(DiffNodeFactory.generateNodeComp(lhsComp, DiffNodeState.Removed, rhsEnvId));
    			    }
    			    else {
    			        ref.setReference(DiffNodeFactory.generateDiffComp(lhsComp, rhsComp));
    			    }
			    }
			    else {
			        ref.setId(lhsComp.getId());
			    }
				ref.setName(name);
                ref.setEnvID(lhsEnvId);				
                ref.setParent(this);
				ref.setState(DiffNodeState.Removed);
				result.add(ref);				
			}
		}
		
		// loop rhs
		int i=0;
		for (SAFREnvironmentalComponent rhsComp : rhsList) {
			// for each rhs comp check whether in lhs
			boolean found = false;
			for (SAFREnvironmentalComponent lhsComp : lhsList) {
				if (lhsComp.getId().equals(rhsComp.getId())) {
					found = true;
					break;
				}				
			}
			// if not found then reference has been added
			if (!found) {
			    // add referenced component
			    SAFREnvironmentalComponent lhsComp = DiffNodeFactory.generateComp(type, rhsComp.getId(), lhsEnvId);
                DiffFieldReference ref = new DiffFieldReference();
	            if (related) {
                    if (lhsComp == null) {
                        ref.setReference(DiffNodeFactory.generateNodeComp(rhsComp, DiffNodeState.Added, lhsEnvId));
                    }
                    else {
                        ref.setReference(DiffNodeFactory.generateDiffComp(lhsComp, rhsComp));
                    }
	            }
	            else {
	                ref.setId(rhsComp.getId());
	            }
				ref.setName(name);
                ref.setEnvID(rhsEnvId);             				
                ref.setParent(this);
				ref.setState(DiffNodeState.Added);
				result.add(i, ref);								
			}	
			
			i++;
			
            // skip any removed items
            while (i < result.size() && result.get(i).getState().equals(DiffNodeState.Removed)) {
                i++;
            }
			
		}		
		return result;
	}
	
    protected List<DiffFieldReference> diffSingleReferences(
        MetaType type,
        String name, 
        SAFREnvironmentalComponent lhsComp, 
        SAFREnvironmentalComponent rhsComp,
        int lhsEnvId,
        int rhsEnvId) throws SAFRException
    {
        List<DiffFieldReference> result = new ArrayList<DiffFieldReference>();

        if (lhsComp == null) {
            if (rhsComp == null) {
                // add same null reference
                DiffFieldReference ref = new DiffFieldReference();
                ref.setName(name);
                ref.setParent(this);
                ref.setState(DiffNodeState.Same);
                result.add(ref);                
            } 
            else {
                // add null lhs ref
                DiffFieldReference lhsRef = new DiffFieldReference();
                lhsRef.setName(name);
                lhsRef.setParent(this);
                lhsRef.setState(DiffNodeState.Different);
                lhsRef.setEnvID(lhsEnvId);
                result.add(lhsRef);
                
                // add rhs reference
                DiffFieldReference rhsRef = new DiffFieldReference();
                if (related) {
                    SAFREnvironmentalComponent lhsCom = DiffNodeFactory.generateComp(type, rhsComp.getId(), lhsEnvId); 
                    if (lhsCom == null) {
                        rhsRef.setReference(DiffNodeFactory.generateNodeComp(rhsComp, DiffNodeState.Added, lhsEnvId));
                    }
                    else {
                        rhsRef.setReference(DiffNodeFactory.generateDiffComp(lhsCom, rhsComp));                                                                                
                    }
                }
                else {
                    rhsRef.setId(rhsComp.getId());
                }
                rhsRef.setName(name);
                rhsRef.setParent(this);
                rhsRef.setState(DiffNodeState.Different);
                rhsRef.setEnvID(rhsEnvId);
                result.add(rhsRef);                                    
            }            
        }
        else {
            if (rhsComp == null) {
                // lhs reference
                DiffFieldReference lhsRef = new DiffFieldReference();
                if (related) {
                    SAFREnvironmentalComponent rhsCom = DiffNodeFactory.generateComp(type, lhsComp.getId(), rhsEnvId);                        
                    if (rhsCom == null) {
                        lhsRef.setReference(DiffNodeFactory.generateNodeComp(lhsComp, DiffNodeState.Removed, rhsEnvId));
                    }
                    else {
                        lhsRef.setReference(DiffNodeFactory.generateDiffComp(lhsComp, rhsCom));                                                    
                    }
                }
                else {
                    lhsRef.setId(lhsComp.getId());
                }
                lhsRef.setName(name);
                lhsRef.setParent(this);
                lhsRef.setEnvID(lhsEnvId);                
                lhsRef.setState(DiffNodeState.Different);
                result.add(lhsRef);
                
                // add null rhs ref
                DiffFieldReference rhsRef = new DiffFieldReference();
                rhsRef.setName(name);
                rhsRef.setParent(this);
                rhsRef.setEnvID(rhsEnvId);                
                rhsRef.setState(DiffNodeState.Different);
                result.add(rhsRef);                                
            }
            else {
                // if one same reference
                if (lhsComp.getId().equals(rhsComp.getId())) {
                    DiffFieldReference ref = new DiffFieldReference();
                    if (related) {
                        ref.setReference(DiffNodeFactory.generateDiffComp(lhsComp, rhsComp));
                    }
                    else {
                        ref.setId(lhsComp.getId());
                    }
                    ref.setName(name);
                    ref.setParent(this);
                    ref.setState(DiffNodeState.Same);
                    result.add(ref);
                }   
                // two different references
                else {
                    // lhs reference
                    
                    // check other env
                    DiffFieldReference lhsRef = new DiffFieldReference();
                    if (related) {
                        SAFREnvironmentalComponent rhsCom = DiffNodeFactory.generateComp(type, lhsComp.getId(), rhsEnvId);                        
                        if (rhsCom == null) {
                            lhsRef.setReference(DiffNodeFactory.generateNodeComp(lhsComp, DiffNodeState.Removed, rhsEnvId));
                        }
                        else {
                            lhsRef.setReference(DiffNodeFactory.generateDiffComp(lhsComp, rhsCom));                            
                        }
                    }
                    else {
                        lhsRef.setId(lhsComp.getId());
                    }
                    lhsRef.setName(name);
                    lhsRef.setParent(this);
                    lhsRef.setEnvID(lhsEnvId);                    
                    lhsRef.setState(DiffNodeState.Different);
                    result.add(lhsRef);

                    // rhs reference
                    DiffFieldReference rhsRef = new DiffFieldReference();
                    if (related) {
                        SAFREnvironmentalComponent lhsCom = DiffNodeFactory.generateComp(type, rhsComp.getId(), lhsEnvId);
                        if (lhsCom == null) {
                            rhsRef.setReference(DiffNodeFactory.generateNodeComp(rhsComp, DiffNodeState.Added, lhsEnvId));                            
                        }
                        else {
                            rhsRef.setReference(DiffNodeFactory.generateDiffComp(lhsCom, rhsComp));                                                        
                        }
                    }
                    else {
                        rhsRef.setId(rhsComp.getId());
                    }
                    rhsRef.setName(name);
                    rhsRef.setParent(this);
                    rhsRef.setEnvID(rhsEnvId);                    
                    rhsRef.setState(DiffNodeState.Different);
                    result.add(rhsRef);                    
                }
            }
        }
        
        return result;
    }
	

	public List<DiffNodeComp> diffChildren(
	    List<SAFREnvironmentalComponent> lhsList, 
	    List<SAFREnvironmentalComponent> rhsList,
	    int lhsEnvId,
	    int rhsEnvId) throws SAFRException {

	    List<DiffNodeComp> nodes = new ArrayList<DiffNodeComp>();
	    
        // loop through lhs list
        for (SAFREnvironmentalComponent lhsComp  : lhsList) {
            boolean found = false;
            for (SAFREnvironmentalComponent rhsComp : rhsList) {
                // we have a match
                if (lhsComp.getId().equals(rhsComp.getId())) {
                    // check same or different
                    found = true;
                    DiffNodeComp node = DiffNodeFactory.generateDiffComp(lhsComp, rhsComp);
                    nodes.add(node);
                    break;
                }
            }
            
            // if not found then must have been removed
            if (!found) {
                DiffNodeComp node = DiffNodeFactory.generateNodeComp(lhsComp, DiffNodeState.Removed, rhsEnvId);
                nodes.add(node);                
            }
        }
        
        // loop through rhs list
        int i=0;
        for (SAFREnvironmentalComponent rhsComp : rhsList) {            
            boolean found = false;
            for (SAFREnvironmentalComponent lhsComp  : lhsList) {
                // we have a match
                if (lhsComp.getId().equals(rhsComp.getId())) {
                    // check same or different
                    found = true;
                    break;
                }
            }
            
            // if not found then must have been removed
            if (!found) {
                DiffNodeComp node = DiffNodeFactory.generateNodeComp(rhsComp, DiffNodeState.Added, lhsEnvId);
                nodes.add(i, node);                                
            }
            
            i++;
            
            // skip any removed items
            while (i < nodes.size() && nodes.get(i).getState().equals(DiffNodeState.Removed)) {
                i++;
            }
        }
        return nodes;
	}

    public List<DiffNodeComp> nodeChildren(
        List<SAFREnvironmentalComponent> list, 
        DiffNodeState state,
        int otherEnv) throws SAFRException {

        List<DiffNodeComp> nodes = new ArrayList<DiffNodeComp>();
        // loop through list
        for (SAFREnvironmentalComponent comp  : list) {
            DiffNodeComp node = DiffNodeFactory.generateNodeComp(comp, state, otherEnv);
            nodes.add(node);            
        }        
        return nodes;
    }
	
	List<DiffFieldReference> nodeReferences(
	    MetaType type,
	    String name,
	    List<SAFREnvironmentalComponent> list,
	    DiffNodeState state,
	    int otherEnv) throws SAFRException {
	    
        List<DiffFieldReference> result = new ArrayList<DiffFieldReference>(); 
	    
	    for (SAFREnvironmentalComponent comp : list) {
            // check for existence of comp in rhsEnv
	        SAFREnvironmentalComponent ocomp = DiffNodeFactory.generateComp(type, comp.getId(), otherEnv);
    
            DiffFieldReference ref = new DiffFieldReference();
            ref.setName(name);
            ref.setParent(this);
            ref.setState(state);
            ref.setEnvID(comp.getEnvironmentId());
            result.add(ref);                    
            
            // generate node to reference
            if (related) {
                if (ocomp == null) {
                    DiffNodeComp node = DiffNodeFactory.generateNodeComp(comp, state, otherEnv);
                    ref.setReference(node);
                }
                else {
                    DiffNodeComp node = null;
                    if (state.equals(DiffNodeState.Removed)) {
                        node = DiffNodeFactory.generateDiffComp(comp, ocomp);
                    }
                    else if (state.equals(DiffNodeState.Added)) {
                        node = DiffNodeFactory.generateDiffComp(ocomp, comp);                      
                    }
                    ref.setReference(node);
                }
            }
            else {
                ref.setId(comp.getId());
            }
	    }
        return result;
	}
	
    DiffFieldReference nodeSingleReference(
        MetaType type,
        String name,
        SAFREnvironmentalComponent comp,
        DiffNodeState state,
        int otherEnv) throws SAFRException {
        
        DiffFieldReference ref = new DiffFieldReference();
        ref.setName(name);
        ref.setParent(this);
        ref.setState(state);
        
        // generate node to reference
        if (comp != null) {
            ref.setEnvID(comp.getEnvironmentId());
            
            if (related) {
                SAFREnvironmentalComponent ocomp = DiffNodeFactory.generateComp(type, comp.getId(), otherEnv);
    
                if (ocomp == null) {
                    DiffNodeComp node = DiffNodeFactory.generateNodeComp(comp, state, otherEnv);
                    ref.setReference(node);
                }
                else {
                    DiffNodeComp node = null;
                    if (state.equals(DiffNodeState.Removed)) {
                        node = DiffNodeFactory.generateDiffComp(comp, ocomp);
                    }
                    else if (state.equals(DiffNodeState.Added)) {
                        node = DiffNodeFactory.generateDiffComp(ocomp, comp);                      
                    }
                    ref.setReference(node);
                }
            }
            else {
                ref.setId(comp.getId());
            }
        }
        return ref;
    }
	
	public DiffNode() {
		children = new ArrayList<DiffNode>();		
	}

	public List<DiffNode> getChildren() {
		return children;
	}

    public Object[] getChildObjects() {
        return children.toArray();
    }
	
	public void setChildren(List<DiffNode> children) {
		this.children = children;
	}

	public void addChild(DiffNode child) {
		this.children.add(child);
	}
	
	public void addChild(DiffNode child, int pos) {		
		this.children.add(pos, child);
	}
	
	public DiffBaseNode getChild(String name) {
		DiffBaseNode result = null;
		for (DiffBaseNode child : children) {
			if (child.getName().equals(name)) {
				result = child;
				break;
			}
		}
		return result;
	}
	
	protected abstract void dumper(OutputStream out, int indent, Set<DiffNodeState> states) throws IOException;

    protected abstract void dumpEnv(OutputStream out, int envId, int indent) throws IOException;

    public void dumpEnv(OutputStream out, int envId) throws IOException {
        dumpEnv(out, envId, 0);
    }
    
	public void dumper(OutputStream out, Set<DiffNodeState> states)  throws IOException {
		dumper(out, 0, states);
	}

	public String dumper(Set<DiffNodeState> states) {
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		try {
			dumper(outstream, states);
			return new String(outstream.toByteArray(), "UTF-8");
		} catch (IOException e) {
			return e.getMessage();
		}
	}

    public String dumper() {
        Set<DiffNodeState> states = new HashSet<DiffNodeState>();
        states.add(DiffNodeState.Added);
        states.add(DiffNodeState.Different);
        states.add(DiffNodeState.Removed);
        states.add(DiffNodeState.Same);
        return dumper(states);
    }
	
	// will return true if any child has one of the listed states
	public boolean childHasState(Set<DiffNodeState> states) {	    
	    if (hasState(states)) {
	        return true;
	    }
	    else {
            boolean found = false;
            for (DiffNode node : children) {
                if (node.childHasState(states)) {
                    found = true;
                    break;
                }
            }
            
            return found;
	    }
	}
	
}
