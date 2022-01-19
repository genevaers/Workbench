package com.ibm.safr.we.ui.editors.lr;

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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ibm.safr.we.model.LRField;

public class LRFieldsExpandState {

    enum ExpandState {
        EXPANDED, COLLAPSED, NONE;
    }
    
    private Map<Integer, Boolean> expandState = new HashMap<Integer, Boolean>();
    
    public LRFieldsExpandState(List<LRField> flds) {
        updateState(flds);
    }

    public void updateState(List<LRField> flds) {
        Map<Integer, Boolean> newExpandState = new HashMap<Integer, Boolean>();
        
        for (LRField fld : flds) {
            if (fld.getChildren().size() >= 1) {
                if (expandState.containsKey(fld.getId())) {
                    // keep expanded states that were already there
                    newExpandState.put(fld.getId(), expandState.get(fld.getId()));
                }
                else {
                    // default to expanded
                    newExpandState.put(fld.getId(), new Boolean(true));
                }
            }
        }
        
        expandState = newExpandState;
    }
    
    public ExpandState getFieldState(int fldId) {
        Boolean expanded = expandState.get(fldId);
        if (expanded == null) {
            return ExpandState.NONE;
        }
        else if (expanded) {
            return ExpandState.EXPANDED;
        }
        else {
            return ExpandState.COLLAPSED;            
        }
    }
    
    public void setFieldState(int fldId, ExpandState state) {
        switch (state) {
        case COLLAPSED:
            expandState.put(fldId, false);
            break;
        case EXPANDED:
            expandState.put(fldId, true);
            break;
        case NONE:
            expandState.remove(fldId);
            break;
        default:
            break;
        
        }
    }
    
    public void expandToShowField(LRField fld) {
        Integer parent = fld.getRedefine();
        LRField child = fld;
        while (parent != null) {
            expandState.put(parent, true);
            child = child.getParent();
            if (child == null) {
                parent = null;
            }
            else {
                parent = child.getRedefine();                
            }
        }
    }

    public void expandAll() {
        for (Entry<Integer, Boolean> entry : expandState.entrySet()) {
            entry.setValue(true);
        }        
    }

    public void collapseAll() {
        for (Entry<Integer, Boolean> entry : expandState.entrySet()) {
            entry.setValue(false);
        }        
    }
    
}
