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
import com.ibm.safr.we.model.LRField;

public class NodeLRField extends DiffNodeComp {

    private LRField field;
    @SuppressWarnings("unused")
    private int otherEnv;
    
    public NodeLRField(LRField field, DiffNodeState state, int otherEnv) {
        super();
        this.field = field;
        this.state = state;
        this.otherEnv = otherEnv;
    }

    
    @Override
    protected void generateTree() throws SAFRException {
        NodeLRField tree = (NodeLRField) getGenerated(NodeLRField.class, field.getId());
        if (tree == null) {
            setId(field.getId());
            setEnvID(field.getEnvironmentId());
            setName("LR Field");
            
            addStringField("Name", field.getName(), field.getEnvironmentId(), state); 
            addCodeField("Data Type", field.getDataTypeCode(), field.getEnvironmentId(), state); 
            addIntField("Fixed Position", field.getPosition(), field.getEnvironmentId(), state); 
            addIntField("Length", field.getLength(), field.getEnvironmentId(), state); 
            addCodeField("Date Format", field.getDateTimeFormatCode(), field.getEnvironmentId(), state); 
            addBoolField("Signed", field.isSigned(), field.getEnvironmentId(), state); 
            addIntField("Decimal Places", field.getDecimals(), field.getEnvironmentId(), state); 
            addIntField("Scaling", field.getScaling(), field.getEnvironmentId(), state); 
            addCodeField("Mask", field.getNumericMaskCode(), field.getEnvironmentId(), state); 
            addIntField("Pkey Seq", field.getPkeySeqNo(), field.getEnvironmentId(), state); 
            addStringField("Eff Date", field.getEffectiveDateString(), field.getEnvironmentId(), state);
            String rFieldStr = "";
            LRField rField = field.getLogicalRecord().findLRField(field.getRedefine());
            if (rField != null) {
                rFieldStr = rField.getDescriptor();
            }            
            addStringField("Redefines", rFieldStr, field.getEnvironmentId(), state); 
            addCodeField("Align Heading", field.getHeaderAlignmentCode(), field.getEnvironmentId(), state); 
            addStringField("Heading 1", field.getHeading1(), field.getEnvironmentId(), state); 
            addStringField("Heading 2", field.getHeading2(), field.getEnvironmentId(), state); 
            addStringField("Heading 3", field.getHeading3(), field.getEnvironmentId(), state); 
            addStringField("Sort Header", field.getSortKeyLabel(), field.getEnvironmentId(), state); 
            addStringField("Sort Footer", field.getSubtotalLabel(), field.getEnvironmentId(), state); 
            if (DiffNode.weFields) {            
                addStringField("Comment", field.getComment(), field.getEnvironmentId(), state);
            }
            
            storeGenerated(NodeLRField.class, field.getId(), this);            
        }

    }

}
