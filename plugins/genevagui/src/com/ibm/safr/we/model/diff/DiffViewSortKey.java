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


import java.util.HashSet;
import java.util.Set;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.view.ViewSortKey;

public class DiffViewSortKey extends DiffNodeComp {

    private ViewSortKey lhs;
    private ViewSortKey rhs;
    
    public DiffViewSortKey(ViewSortKey lhs, ViewSortKey rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    
    @Override    
    protected void generateTree() throws SAFRException {
        DiffViewSortKey tree = (DiffViewSortKey) getGenerated(DiffViewSortKey.class, lhs.getId());
        if (tree == null) {
            setId(lhs.getId());
            setDispId((new Integer(Math.max(lhs.getKeySequenceNo(), rhs.getKeySequenceNo()))).toString());
            setName("View Sort Key");
            Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
            diff.add(addIntField("Key Number", lhs.getKeySequenceNo(), rhs.getKeySequenceNo(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));                         
            diff.add(addCodeField("Sequence", lhs.getSortSequenceCode(), rhs.getSortSequenceCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            diff.add(addCodeField("Data Type", lhs.getDataTypeCode(), rhs.getDataTypeCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            diff.add(addCodeField("Date Format", lhs.getDateTimeFormatCode(), rhs.getDateTimeFormatCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));  
            diff.add(addIntField("Length", lhs.getLength(), rhs.getLength(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            diff.add(addIntField("Decimals", lhs.getDecimalPlaces(), rhs.getDecimalPlaces(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            diff.add(addBoolField("Signed", lhs.isSigned(), rhs.isSigned(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            diff.add(addCodeField("Display Mode", lhs.getDisplayModeCode(), rhs.getDisplayModeCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));  
            diff.add(addStringField("Sort Key Label", lhs.getSortkeyLabel(), rhs.getSortkeyLabel(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));  
            diff.add(addCodeField("Header Option", lhs.getHeaderOptionCode(), rhs.getHeaderOptionCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(addStringField("Sort Key Footer", lhs.getViewColumn().getSortkeyFooterLabel(), rhs.getViewColumn().getSortkeyFooterLabel(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(addCodeField("Footer Option", lhs.getFooterOptionCode(), rhs.getFooterOptionCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(addIntField("Title Length", lhs.getTitleLength(), lhs.getTitleLength(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
                setState(DiffNodeState.Different);
            }
            else {
                setState(DiffNodeState.Same);
            }
            storeGenerated(DiffViewSortKey.class, lhs.getId(), this);                        
        }
    }

}
