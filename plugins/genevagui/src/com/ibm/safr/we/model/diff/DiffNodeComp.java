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
import java.util.Set;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.exceptions.SAFRException;

public abstract class DiffNodeComp extends DiffNodeSection {

	private Integer id = null;
	private Integer envID = null;
	private String dispId = null;
	
    public DiffNodeComp() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

    public Integer getEnvID() {
        return envID;
    }

    public void setEnvID(Integer envID) {
        this.envID = envID;
    }

    protected String getDispId() {
        return dispId;
    }

    protected void setDispId(String dispId) {
        this.dispId = dispId;
    }
    
	protected void dumper(OutputStream out, int indent, Set<DiffNodeState> states) throws IOException {
	    
        // filter out certain states
        if (!childHasState(states)) {
            return;
        }
	    
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
        
        if (envID == null || envID == 0 || this.envID == envId) {
            out.write((spaces + toString() + SAFRUtilities.LINEBREAK).getBytes());
        }
        
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
	
	public DiffNode generateWholeTree() throws SAFRException {
		generateTree();
		return metadata;
	}

	@Override
    public String toString() {
	    String idStr = null;
	    if (dispId == null) {
	        idStr = " [" + id.toString() + "]";
	    }
	    else {
	        if (dispId.isEmpty()) {
	            idStr = "";
	        }
	        else {
	            idStr = " [" + dispId + "]";
	        }
	    }	    
        String str;        
        if (state.equals(DiffNodeState.Same)) {
            str = name + idStr;
        }
        else if (state.equals(DiffNodeState.Different)) {
            str = name + idStr + " *";            
        }
        else {
            str = "[" + envID + "] " + name + idStr;            
        }
	    
        return str;
    }

    protected abstract void generateTree() throws SAFRException;
	
}
