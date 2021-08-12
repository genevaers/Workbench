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
import java.util.List;
import java.util.Set;

import com.ibm.safr.we.constants.LookupPathSourceFieldType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LookupPathSourceField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

public class DiffLookupPathSrcField extends DiffNodeComp {

    LookupPathSourceField lhs;
    LookupPathSourceField rhs;
    
    public DiffLookupPathSrcField(LookupPathSourceField lhs, LookupPathSourceField rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    protected void generateTree() throws SAFRException {
        Integer key = lhs.getId()*100 + ((LookupPathSourceField) lhs).getKeySeqNbr();  
        DiffLookupPathSrcField tree = (DiffLookupPathSrcField) getGenerated(DiffLookupPathSrcField.class, key);
        if (tree == null) {
            setId(lhs.getKeySeqNbr());
            setName("Source Field");
            
            Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
            diff.add(addStringField("Type", lhs.getSourceFieldType().toString(), rhs.getSourceFieldType().toString(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            if (lhs.getSourceFieldType().equals(LookupPathSourceFieldType.LRFIELD) ||
                rhs.getSourceFieldType().equals(LookupPathSourceFieldType.LRFIELD)) {
                // referenced LR LF
                
                SAFREnvironmentalComponent lhsComp = null;
                if (lhs.getSourceFieldLRLFAssociation() != null && lhs.getSourceFieldLRLFAssociation().getAssociationId() != 0) {
                    // referenced LR source
                    lhsComp = SAFRApplication.getSAFRFactory().getLogicalRecord(
                        lhs.getSourceFieldLRLFAssociation().getAssociatingComponentId(),lhs.getEnvironment().getId());            
                }
                SAFREnvironmentalComponent rhsComp = null;
                if (rhs.getSourceFieldLRLFAssociation() != null && rhs.getSourceFieldLRLFAssociation().getAssociationId() != 0) {
                    rhsComp = SAFRApplication.getSAFRFactory().getLogicalRecord(
                        rhs.getSourceFieldLRLFAssociation().getAssociatingComponentId(),rhs.getEnvironment().getId());            
                }
                List<DiffFieldReference> refList = diffSingleReferences(MetaType.LOGICAL_RECORDS, 
                    "Source LR", lhsComp, rhsComp, 
                    lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
                for (DiffFieldReference ref :refList) {         
                    diff.add(ref.getState());
                    getFields().add(ref);
                }

                // referenced LF source
                lhsComp =  null;
                if (lhs.getSourceFieldLRLFAssociation() != null && lhs.getSourceFieldLRLFAssociation().getAssociationId() != 0) {                
                    lhsComp = SAFRApplication.getSAFRFactory().getLogicalFile(
                        lhs.getSourceFieldLRLFAssociation().getAssociatedComponentIdNum(),lhs.getEnvironment().getId());
                }
                rhsComp = null;
                if (rhs.getSourceFieldLRLFAssociation() != null && rhs.getSourceFieldLRLFAssociation().getAssociationId() != 0) {                
                    rhsComp = SAFRApplication.getSAFRFactory().getLogicalFile(
                        rhs.getSourceFieldLRLFAssociation().getAssociatedComponentIdNum(),rhs.getEnvironment().getId());
                }
                refList = diffSingleReferences(MetaType.LOGICAL_FILES, "Source LF", lhsComp, rhsComp, 
                    lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
                for (DiffFieldReference ref :refList) {         
                    diff.add(ref.getState());
                    getFields().add(ref);
                }
                
                Integer lhsLr = null;
                Integer lhsId = null;
                if (lhs.getSourceLRField() != null) {
                    lhsLr = lhs.getSourceLRField().getLogicalRecord().getId();
                    lhsId = lhs.getSourceLRField().getId();
                }
                Integer rhsLr = null;
                Integer rhsId = null;
                if (rhs.getSourceLRField() != null) {
                    rhsLr = rhs.getSourceLRField().getLogicalRecord().getId();
                    rhsId = rhs.getSourceLRField().getId();
                }
                    
                diff.add(addIntField("LR", lhsLr, rhsLr, lhs.getEnvironmentId(), rhs.getEnvironmentId()));
                diff.add(addIntField("LR Field", lhsId, rhsId, lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            }
            if (lhs.getSourceFieldType().equals(LookupPathSourceFieldType.CONSTANT) ||
                rhs.getSourceFieldType().equals(LookupPathSourceFieldType.CONSTANT)) {
                diff.add(addStringField("Value", lhs.getSourceValue(), rhs.getSourceValue(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));                
            }
            if  (lhs.getSourceFieldType().equals(LookupPathSourceFieldType.SYMBOL) ||
                rhs.getSourceFieldType().equals(LookupPathSourceFieldType.SYMBOL)) {
                diff.add(addStringField("Name", lhs.getSymbolicName(), rhs.getSymbolicName(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
                diff.add(addStringField("Default Value", lhs.getSourceValue(), rhs.getSourceValue(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            }            
            diff.add(addCodeField("Data Type", lhs.getDataTypeCode(), rhs.getDataTypeCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            diff.add(addIntField("Length", lhs.getLength(), rhs.getLength(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            diff.add(addCodeField("Date Format", lhs.getDateTimeFormatCode(), rhs.getDateTimeFormatCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            diff.add(addIntField("Scaling Factor", lhs.getScaling(), rhs.getScaling(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));                         
            diff.add(addIntField("Decimal Places", lhs.getDecimals(), rhs.getDecimals(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));                         
            diff.add(addBoolField("Signed", lhs.isSigned(), rhs.isSigned(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));                         
            diff.add(addCodeField("Numeric Mask", lhs.getNumericMaskCode(), rhs.getNumericMaskCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            
            if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
                setState(DiffNodeState.Different);
            }
            else {
                setState(DiffNodeState.Same);
            }
            
            storeGenerated(DiffLookupPathSrcField.class, key, this);            
        }

    }

}
