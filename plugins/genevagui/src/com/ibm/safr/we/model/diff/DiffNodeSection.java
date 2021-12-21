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


import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.FileUtils;

public class DiffNodeSection extends DiffNode {

	public DiffNodeSection() {
		super();
		state = DiffNodeState.Removed;
		fields = new ArrayList<DiffField>();
	}
	
	protected List<DiffField> fields = null;
	
	public List<DiffField> getFields() {
		return fields;
	}

	public void setFields(List<DiffField> fields) {
		this.fields = fields;
	}

    public DiffNodeState addCodeField(String name, Code lhsValue, Code rhsValue, int lhsEnv, int rhsEnv) {
        String lhsStr = "";
        if (lhsValue != null) {
            lhsStr = lhsValue.getDescription();
        }
        String rhsStr = "";
        if (rhsValue != null) {
            rhsStr = rhsValue.getDescription();
        }
        return addStringField(name, lhsStr, rhsStr, lhsEnv, rhsEnv);
    }

    public DiffNodeState addDateField(String name, Date lhsValue, Date rhsValue, int lhsEnv, int rhsEnv) {
        String lhsStr = UIUtilities.formatDate(lhsValue);
        String rhsStr = UIUtilities.formatDate(rhsValue);
        return addStringField(name, lhsStr, rhsStr, lhsEnv, rhsEnv);
    }

    public DiffNodeState addObjectField(String name, Object lhsValue, Object rhsValue, int lhsEnv, int rhsEnv) {
        String lhs = null;        
        if (lhsValue != null) {
            lhs = lhsValue.toString();
        }
        String rhs = null;        
        if (rhsValue != null) {
            rhs = rhsValue.toString();
        }        
        return addStringField(name, lhs, rhs, lhsEnv, rhsEnv);
    }

    public void addObjectField(String name, Object value, int envID, DiffNodeState state) {
        String objStr = null;        
        if (value != null) {
            objStr = value.toString();
        }
        addStringField(name, objStr, envID, state);
    }
    
	public DiffNodeState addStringField(String name, String lhsValue, String rhsValue, int lhsEnv, int rhsEnv) {
	    
        if (lhsValue == null || lhsValue.isEmpty()) {
            if (rhsValue == null || rhsValue.isEmpty()) {
                // add single same field
                addSameStringField(name, lhsValue);
                return DiffNodeState.Same;                
            }
            else {
                // add two fields
                addStringField(name, lhsValue, lhsEnv, DiffNodeState.Different);
                addStringField(name, rhsValue, rhsEnv, DiffNodeState.Different);
                return DiffNodeState.Different;                
            }
        }
        else if (lhsValue.equals(rhsValue)) {
	        // add single same field
	        addSameStringField(name, lhsValue);
	        return DiffNodeState.Same;
	    }
	    else {
	        // add two fields
	        addStringField(name, lhsValue, lhsEnv, DiffNodeState.Different);
            addStringField(name, rhsValue, rhsEnv, DiffNodeState.Different);
            return DiffNodeState.Different;
	    }
	}

	
    public DiffNodeState addIntField(String name, Integer lhsValue, Integer rhsValue, int lhsEnv, int rhsEnv) {
        String lhs = null;
        if (lhsValue != null) {
            lhs = lhsValue.toString();
        }
        String rhs = null;
        if (rhsValue != null) {
            rhs = rhsValue.toString();
        }
        return addStringField(name, lhs, rhs, lhsEnv, rhsEnv);
    }

    public DiffNodeState addBoolField(String name, Boolean lhsValue, Boolean rhsValue, int lhsEnv, int rhsEnv) {        
        String lhs = null;
        if (lhsValue != null) {
            if (lhsValue) {
                lhs = "Yes";                
            }
            else {
                lhs = "No";
            }
        }
        String rhs = null;
        if (rhsValue != null) {
            if (rhsValue) {
                rhs = "Yes";                
            }
            else {
                rhs = "No";
            }
        }
        return addStringField(name, lhs, rhs, lhsEnv, rhsEnv);
    }
    
    public void addCodeField(String name, Code value, int envID, DiffNodeState state) {
        String str = "";
        if (value != null) {
            str = value.getDescription();
        }
        addStringField(name, str, envID, state);
    }

    public void addSameStringField(String name, String value) {
        DiffFieldValue field = new DiffFieldValue();
        fields.add(field);      
        field.setName(name);
        field.setParent(this);
        field.setEnvID(0);
        if (value == null) {
            field.setValue("");
        }
        else {
            field.setValue(value);
        }
        field.setState(DiffNodeState.Same);
    }

    public void addLargeSameStringField(String name, String value) {
        DiffFieldValue field = new DiffFieldValue();
        fields.add(field);      
        field.setName(name);
        field.setParent(this);
        field.setEnvID(0);
        field.setLarge(true);
        if (value == null) {
            field.setValue("");
        }
        else {
            field.setValue(value);
        }
        field.setState(DiffNodeState.Same);
    }
    
	public void addStringField(String name, String value, int envID, DiffNodeState state) {
		DiffFieldValue field = new DiffFieldValue();
		fields.add(field);		
		field.setName(name);
        field.setParent(this);
        field.setEnvID(envID);
		if (value == null) {
		    field.setValue("");
		}
		else {
		    field.setValue(value);
		}
		field.setState(state);
	}

    public void addLargeStringField(String name, String value, int envID, DiffNodeState state) {
        DiffFieldValue field = new DiffFieldValue();
        fields.add(field);      
        field.setName(name);
        field.setParent(this);
        field.setEnvID(envID);
        field.setLarge(true);
        if (value == null) {
            field.setValue("");
        }
        else {
            field.setValue(value);
        }
        field.setState(state);
    }
	
    public void addIntField(String name, Integer value, int envID, DiffNodeState state) {
        String str = "";
        if (value != null) {
            str = value.toString();
        }
        addStringField(name, str, envID, state);
    }

    public DiffNodeState addLargeStringField(String name, String lhsVal, String rhsVal, int lhsEnv, int rhsEnv) {
        
    	String lhsValue = FileUtils.fixLineEndings(lhsVal);
    	String rhsValue = FileUtils.fixLineEndings(rhsVal);
        if (lhsValue == null) {
            if (rhsValue == null) {
                // add single same field
                addLargeSameStringField(name, lhsValue);
                return DiffNodeState.Same;                
            }
            else {
                // add two fields
                addLargeStringField(name, lhsValue, lhsEnv, DiffNodeState.Different);
                addLargeStringField(name, rhsValue, rhsEnv, DiffNodeState.Different);
                return DiffNodeState.Different;                
            }
        }
        else if (lhsValue.equals(rhsValue)) {
            // add single same field
            addLargeSameStringField(name, lhsValue);
            return DiffNodeState.Same;
        }
        else {
            // add two fields
            addLargeStringField(name, lhsValue, lhsEnv, DiffNodeState.Different);
            addLargeStringField(name, rhsValue, rhsEnv, DiffNodeState.Different);
            return DiffNodeState.Different;
        }
    }
    
    public void addDateField(String name, Date value, int envID, DiffNodeState state) {
        String str = UIUtilities.formatDate(value);
        addStringField(name, str, envID, state);
    }

    public void addBoolField(String name, Boolean value, int envID, DiffNodeState state) {
        String str = null;
        if (value != null) {
            if (value) {
                str = "Yes";                
            }
            else {
                str = "No";
            }
        }        
        addStringField(name, str, envID, state);
    }
    
	protected void dumper(OutputStream out, int indent, Set<DiffNodeState> states) throws IOException {
		String spaces = new String(new char[indent]).replace('\0', ' ');
		
		out.write((spaces + toString() + SAFRUtilities.LINEBREAK).getBytes());			
		
		// show all fields
		if (fields.size() > 0) {
			for (DiffField field : fields) {
                if (field.hasState(states)) {			    
                    out.write((spaces + " " + field.toString() + SAFRUtilities.LINEBREAK).getBytes());
                }
			}
		}

		// show all children
		if (children.size() > 0) {
			indent++;
			for (DiffNode child : children) {
				child.dumper(out, indent, states);
			}
		}
	}

    @Override
    protected void dumpEnv(OutputStream out, int envId, int indent) throws IOException {
        String spaces = new String(new char[indent]).replace('\0', ' ');
        
        out.write((spaces + toString() + SAFRUtilities.LINEBREAK).getBytes());          
        
        // show all fields
        if (fields.size() > 0) {
            for (DiffField field : fields) {
                if (field.getEnvID() == envId || field.getEnvID() == 0) {
                    out.write((spaces + " " + field.toString() + SAFRUtilities.LINEBREAK).getBytes());
                }
            }
        }

        // show all children
        if (children.size() > 0) {
            indent++;
            for (DiffNode child : children) {
                child.dumpEnv(out, envId, indent);
            }
        }
    }
        	
    @Override
    public Object[] getChildObjects() {
        List<Object> objects = new ArrayList<Object>();
        objects.addAll(fields);
        objects.addAll(children);
        return objects.toArray();
    }

    @Override
    public String toString() {
        if (state.equals(DiffNodeState.Different)) {
            return name + " *";            
        }
        else {
            return name;            
        }
    }
	
    // will return true if any child has one of the listed states
    @Override
    public boolean childHasState(Set<DiffNodeState> states) {       
        if (hasState(states)) {
            return true;
        }
        else {
            boolean found = false;
            // loop and check fields for states
            for (DiffField field : ((DiffNodeSection)this).getFields()) {
                if (field.hasState(states)) {
                    found = true;
                    break;
                }
            }
            
            // check children
            if (!found) {
                for (DiffNode node : children) {
                    if (node.childHasState(states)) {
                        found = true;
                        break;
                    }
                }
            }
            return found;
        }
    }

}
