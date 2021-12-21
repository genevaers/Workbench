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



public class DiffFieldValue extends DiffField {

    public class OtherValue {
        public boolean isLeft;
        public String value;
    }
    
	private String value = null;
	private boolean large = false;

    public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

    public boolean isLarge() {
        return large;
    }

    public void setLarge(boolean large) {
        this.large = large;
    }
	
	@Override
	public String toString() {
		String str = "";
		if (state.equals(DiffNodeState.Same)) {
		    str = name + ": " + value;
		}
		else {
		    // pad env
		    String envStr = "[" + envID + "]";
		    envStr = String.format("%1$-8s", envStr);  
		    str = envStr + name + ": " + value;
		}
		return str;
	}
	
	public OtherValue getOtherValue() {
	    if (!getState().equals(DiffNodeState.Different)) {
	        return null;
	    }
        OtherValue ret = new OtherValue();
	    DiffNodeSection section = (DiffNodeSection)getParent();
	    DiffFieldValue prev = null;
	    int i=0;
	    for (DiffField fld : section.getFields()) {
	        // if we find ourselves
	        if (fld==this) {
	            
	            // check previous
	            if (prev != null) {
	                if (prev.getName().equals(getName())) {
	                    // found the other value
	                    ret.value = prev.getValue();
	                    ret.isLeft = true;
	                    return ret;
	                }
	            }
	            
	            // check next
	            if (i + 1 < section.getFields().size()) {
	                DiffField temp = (DiffField) section.getFields().get(i+1);
	                if (temp.getName().equals(getName()) &&
	                    temp instanceof DiffFieldValue) {
                        // found the other value
	                    DiffFieldValue next = (DiffFieldValue)temp;
                        ret.value = next.getValue();
                        ret.isLeft = false;	                    
                        return ret;	                    
	                }
	            }
	        }
	        
	        // set prev
	        if (fld instanceof DiffFieldValue) {
	            prev = (DiffFieldValue)fld;            
	        }
	        else {
	            prev = null;
	        }
	        
	        i++;
	    }
	    return null;
	}
}
