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
import com.ibm.safr.we.model.LRField;

public class DiffLRField extends DiffNodeComp {

    private LRField lhs;
    private LRField rhs;
    
    public DiffLRField(LRField lhs, LRField rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    
    @Override
    protected void generateTree() throws SAFRException {
        DiffLRField tree = (DiffLRField) getGenerated(DiffLRField.class, lhs.getId());
        if (tree == null) {
            setId(lhs.getId());
            setName("LR Field");
            
            Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
            diff.add(addStringField("Name", lhs.getName(), rhs.getName(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addCodeField("Data Type", lhs.getDataTypeCode(), rhs.getDataTypeCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addIntField("Fixed Position", lhs.getPosition(), rhs.getPosition(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addIntField("Length", lhs.getLength(), rhs.getLength(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addCodeField("Date Format", lhs.getDateTimeFormatCode(), rhs.getDateTimeFormatCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addBoolField("Signed", lhs.isSigned(), rhs.isSigned(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addIntField("Decimal Places", lhs.getDecimals(), rhs.getDecimals(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addIntField("Scaling", lhs.getScaling(), rhs.getScaling(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addCodeField("Mask", lhs.getNumericMaskCode(), rhs.getNumericMaskCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addIntField("Pkey Seq", lhs.getPkeySeqNo(), rhs.getPkeySeqNo(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addStringField("Eff Date", lhs.getEffectiveDateString(), rhs.getEffectiveDateString(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            String lhsFieldStr = "";
            LRField lhsField = lhs.getLogicalRecord().findLRField(lhs.getRedefine());
            if (lhsField != null) {
                lhsFieldStr = lhsField.getDescriptor();
            }
            String rhsFieldStr = "";
            LRField rhsField = rhs.getLogicalRecord().findLRField(rhs.getRedefine());
            if (rhsField != null) {
                rhsFieldStr = rhsField.getDescriptor();
            }
            diff.add(addStringField("Redefines", lhsFieldStr, rhsFieldStr, lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            
            diff.add(addCodeField("Align Heading", lhs.getHeaderAlignmentCode(), rhs.getHeaderAlignmentCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addStringField("Heading 1", lhs.getHeading1(), rhs.getHeading1(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addStringField("Heading 2", lhs.getHeading2(), rhs.getHeading2(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addStringField("Heading 3", lhs.getHeading3(), rhs.getHeading3(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addStringField("Sort Header", lhs.getSortKeyLabel(), rhs.getSortKeyLabel(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addStringField("Sort Footer", lhs.getSubtotalLabel(), rhs.getSubtotalLabel(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            if (DiffNode.weFields) {
                diff.add(addStringField("Comment", lhs.getComment(), rhs.getComment(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            }
            
            if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
                setState(DiffNodeState.Different);
            }
            else {
                setState(DiffNodeState.Same);
            }
            
            storeGenerated(DiffLRField.class, lhs.getId(), this);            
        }

    }

}
