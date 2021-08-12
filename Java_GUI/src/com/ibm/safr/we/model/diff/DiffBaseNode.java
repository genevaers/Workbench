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


import java.util.Set;


public class DiffBaseNode {

    public enum DiffNodeState {
        Removed, Added, Same, Different;        
    }
    
    protected String name = null;
    protected DiffBaseNode parent = null;
    protected DiffNodeState state = DiffNodeState.Same;
    
    public boolean hasState(Set<DiffNodeState> states) {
        return states.contains(state);
    }
    
    public String getName() {
    	return name;
    }
    public void setName(String name) {
    	this.name = name;
    }
    public DiffBaseNode getParent() {
        return parent;
    }
    public void setParent(DiffBaseNode parent) {
        this.parent = parent;
    }
    public DiffNodeState getState() {
    	return state;
    }
    public void setState(DiffNodeState state) {
    	this.state = state;
    }

}
