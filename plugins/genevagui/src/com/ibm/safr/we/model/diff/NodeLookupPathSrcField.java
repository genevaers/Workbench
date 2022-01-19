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


import com.ibm.safr.we.constants.LookupPathSourceFieldType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LookupPathSourceField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

public class NodeLookupPathSrcField extends DiffNodeComp {

    private LookupPathSourceField srcFld;
    private int otherEnv;
    
    public NodeLookupPathSrcField(LookupPathSourceField lhs, DiffNodeState state, int otherEnv) {
        super();
        this.srcFld = lhs;
        this.state = state;
        this.otherEnv = otherEnv;
    }

    @Override
    protected void generateTree() throws SAFRException {
        Integer key = srcFld.getId()*100 + ((LookupPathSourceField) srcFld).getKeySeqNbr();          
        NodeLookupPathSrcField tree = (NodeLookupPathSrcField) getGenerated(NodeLookupPathSrcField.class, key);
        if (tree == null) {
            setId(srcFld.getKeySeqNbr());
            setEnvID(srcFld.getEnvironmentId());            
            setName("Source Field");
            
            addStringField("Type", srcFld.getSourceFieldType().toString(), srcFld.getEnvironmentId(), state);
            if (srcFld.getSourceFieldType().equals(LookupPathSourceFieldType.LRFIELD)) {
                // referenced LR LF
                
                SAFREnvironmentalComponent comp = null;
                if (srcFld.getSourceFieldLRLFAssociation() != null && srcFld.getSourceFieldLRLFAssociation().getAssociationId() != 0) {
                    // referenced LR source
                    comp = SAFRApplication.getSAFRFactory().getLogicalRecord(
                        srcFld.getSourceFieldLRLFAssociation().getAssociatingComponentId(),srcFld.getEnvironmentId());            
                }
                getFields().add(nodeSingleReference(MetaType.LOGICAL_RECORDS, "Source LR", comp, state, otherEnv)); 

                // referenced LF source
                comp = null;
                if (srcFld.getSourceFieldLRLFAssociation() != null && srcFld.getSourceFieldLRLFAssociation().getAssociationId() != 0) {                
                    comp = SAFRApplication.getSAFRFactory().getLogicalFile(
                        srcFld.getSourceFieldLRLFAssociation().getAssociatedComponentIdNum(),srcFld.getEnvironmentId());
                }
                getFields().add(nodeSingleReference(MetaType.LOGICAL_FILES, "Source LF", comp, state, otherEnv)); 
                
                Integer lhsId = null;
                Integer lhsLr = null;
                if (srcFld.getSourceLRField() != null) {
                    lhsId = srcFld.getSourceLRField().getId();
                    lhsLr = srcFld.getSourceLRField().getLogicalRecord().getId();                    
                }
                    
                addIntField("LR", lhsLr, srcFld.getEnvironmentId(), state);
                addIntField("LR Field", lhsId, srcFld.getEnvironmentId(), state);
            }
            if (srcFld.getSourceFieldType().equals(LookupPathSourceFieldType.CONSTANT)) {
                addStringField("Value", srcFld.getSourceValue(), srcFld.getEnvironmentId(), state);                
            }
            if  (srcFld.getSourceFieldType().equals(LookupPathSourceFieldType.SYMBOL)) {
                addStringField("Name", srcFld.getSymbolicName(), srcFld.getEnvironmentId(), state);
                addStringField("Default Value", srcFld.getSourceValue(), srcFld.getEnvironmentId(), state);
            }            
            addCodeField("Data Type", srcFld.getDataTypeCode(), srcFld.getEnvironmentId(), state);             
            addIntField("Length", srcFld.getLength(), srcFld.getEnvironmentId(), state);             
            addCodeField("Date Format", srcFld.getDateTimeFormatCode(), srcFld.getEnvironmentId(), state);             
            addIntField("Scaling Factor", srcFld.getScaling(), srcFld.getEnvironmentId(), state);                         
            addIntField("Decimal Places", srcFld.getDecimals(), srcFld.getEnvironmentId(), state);                         
            addBoolField("Signed", srcFld.isSigned(), srcFld.getEnvironmentId(), state);                         
            addCodeField("Numeric Mask", srcFld.getNumericMaskCode(), srcFld.getEnvironmentId(), state);             
            
            storeGenerated(NodeLookupPathSrcField.class, key, this);            
        }
    }

}
