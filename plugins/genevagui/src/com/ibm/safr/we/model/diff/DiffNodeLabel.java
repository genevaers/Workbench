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

public class DiffNodeLabel extends DiffNode {

    public DiffNodeLabel(String label) {
		super();
		this.name = label;
	}
	
	@Override
	protected void dumper(OutputStream out, int indent, Set<DiffNodeState> states) throws IOException {
	    
	    // filter out certain states
	    if (!childHasState(states)) {
	        return;
	    }
	    
		String spaces = new String(new char[indent]).replace('\0', ' ');
		out.write((spaces + toString() + SAFRUtilities.LINEBREAK).getBytes());
		
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
        
        // show all children
        if (children.size() > 0) {
            indent++;
            for (DiffNode child : children) {
                child.dumpEnv(out, envId, indent);
            }
        }       
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

}
