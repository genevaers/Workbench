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




public class DiffFieldReference extends DiffField {
	
	DiffNodeComp reference = null;
	Integer id = null;

    public DiffNodeComp getReference() {
		return reference;
	}

	public void setReference(DiffNodeComp reference) {
		this.reference = reference;
	}	

    protected Integer getId() {
        return id;
    }

    protected void setId(Integer id) {
        this.id = id;
    }
	
	@Override
	public String toString() {
        String str;
        String idStr = ":";
        if (reference == null) {
            if (id != null) {
                idStr = ": [" + id + "] ";
            }
        }
        else {
            idStr = ": [" + reference.getId()+ "] ";
        }
        
        if (state.equals(DiffNodeState.Same)) {
            str = name + idStr;
        }
        else {
            // pad env
            String envStr = "[" + envID + "]";
            envStr = String.format("%1$-8s", envStr);                  
            str = envStr + name + idStr;            
        }            

		return str;
	}
	
}
