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


import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.view.ViewSortKey;

public class NodeViewSortKey extends DiffNodeComp {

    private ViewSortKey vsk;
    @SuppressWarnings("unused")
    private int otherEnv;
    
    public NodeViewSortKey(ViewSortKey lhs, DiffNodeState state, int otherEnv) {
        super();
        this.vsk = lhs;
        this.state = state;
        this.otherEnv = otherEnv;        
    }
    
    @Override    
    protected void generateTree() throws SAFRException {
        NodeViewSortKey tree = (NodeViewSortKey) getGenerated(NodeViewSortKey.class, vsk.getId());
        if (tree == null) {
            setId(vsk.getId());
            setDispId(vsk.getKeySequenceNo().toString());            
            setEnvID(vsk.getEnvironmentId());            
            setName("View Sort Key");
            addIntField("Key Number", vsk.getKeySequenceNo(), vsk.getEnvironmentId(), state);             
            addCodeField("Sequence", vsk.getSortSequenceCode(), vsk.getEnvironmentId(), state);             
            addCodeField("Data Type", vsk.getDataTypeCode(), vsk.getEnvironmentId(), state);             
            addCodeField("Date Format", vsk.getDateTimeFormatCode(), vsk.getEnvironmentId(), state);  
            addIntField("Length", vsk.getLength(), vsk.getEnvironmentId(), state);             
            addIntField("Decimals", vsk.getDecimalPlaces(), vsk.getEnvironmentId(), state);             
            addBoolField("Signed", vsk.isSigned(), vsk.getEnvironmentId(), state);             
            addCodeField("Display Mode", vsk.getDisplayModeCode(), vsk.getEnvironmentId(), state);  
            addStringField("Sort Key Label", vsk.getSortkeyLabel(), vsk.getEnvironmentId(), state);  
            addCodeField("Header Option", vsk.getHeaderOptionCode(), vsk.getEnvironmentId(), state);
            addStringField("Sort Key Footer", vsk.getViewColumn().getSortkeyFooterLabel(), vsk.getEnvironmentId(), state);
            addCodeField("Footer Option", vsk.getFooterOptionCode(), vsk.getEnvironmentId(), state);
            addIntField("Title Length", vsk.getTitleLength(), vsk.getEnvironmentId(), state);
            storeGenerated(NodeViewSortKey.class, vsk.getId(), this);                        
        }
    }

}
