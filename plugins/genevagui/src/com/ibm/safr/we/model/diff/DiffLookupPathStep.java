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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPathSourceField;
import com.ibm.safr.we.model.LookupPathStep;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

public class DiffLookupPathStep extends DiffNodeComp {

    private LookupPathStep lhs;
    private LookupPathStep rhs;
    
    public DiffLookupPathStep(LookupPathStep lhs, LookupPathStep rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public List<DiffNodeComp> diffSourceFields(
        List<LookupPathSourceField> lhsList, 
        List<LookupPathSourceField> rhsList,
        int lhsEnvId,
        int rhsEnvId) throws SAFRException {

        List<DiffNodeComp> nodes = new ArrayList<DiffNodeComp>();
        
        // loop through lhs list
        for (LookupPathSourceField lhsComp  : lhsList) {
            boolean found = false;
            for (LookupPathSourceField rhsComp : rhsList) {
                // we have a match
                if (lhsComp.getKeySeqNbr().equals(rhsComp.getKeySeqNbr())) {
                    // check same or different
                    found = true;
                    DiffNodeComp node = DiffNodeFactory.generateDiffComp(lhsComp, rhsComp);
                    nodes.add(node);
                    break;
                }
            }
            
            // if not found then must have been removed
            if (!found) {
                DiffNodeComp node = DiffNodeFactory.generateNodeComp(lhsComp, DiffNodeState.Removed, rhsEnvId);
                nodes.add(node);                
            }
        }
        
        // loop through rhs list
        for (LookupPathSourceField rhsComp : rhsList) {
            boolean found = false;
            for (LookupPathSourceField lhsComp  : lhsList) {
                // we have a match
                if (lhsComp.getKeySeqNbr().equals(rhsComp.getKeySeqNbr())) {
                    found = true;
                    break;
                }
            }
            
            // if not found then must have been removed
            if (!found) {
                DiffNodeComp node = DiffNodeFactory.generateNodeComp(rhsComp, DiffNodeState.Added, lhsEnvId);
                nodes.add(node);                                
            }
        }
        return nodes;
    }
    
    
    protected DiffNode generateSourceFields() throws SAFRException {
        DiffNode fields = new DiffNodeSection();
        fields.setName("Source Fields");
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        
        List<DiffNodeComp> flist = diffSourceFields(lhs.getSourceFields(), rhs.getSourceFields(), 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffNodeComp node : flist) {
            node.setParent(fields);
            diff.add(node.getState());
            fields.addChild(node);
        }        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            fields.setState(DiffNodeState.Different);
        }
        else {
            fields.setState(DiffNodeState.Same);
        }
        return fields;
    }
    
    @Override
    protected void generateTree() throws SAFRException {
        DiffLookupPathStep tree = (DiffLookupPathStep) getGenerated(DiffLookupPathStep.class, lhs.getId());
        if (tree == null) {
            setId(lhs.getSequenceNumber());
            setName("Step");
            
            Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
            diff.add(addIntField("Sequence", lhs.getSequenceNumber(), rhs.getSequenceNumber(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            
            // referenced LR source
            SAFREnvironmentalComponent lhsComp = null;
            LogicalRecord lhsSrcLR = lhs.getSourceLR();
            if (lhsSrcLR != null && lhsSrcLR.getId() != 0) {
                lhsComp = lhsSrcLR;
            }
            SAFREnvironmentalComponent rhsComp = null;     
            LogicalRecord rhsSrcLR = rhs.getSourceLR();
            if (rhsSrcLR != null && rhsSrcLR.getId() != 0) {
                rhsComp = rhsSrcLR;
            }
            List<DiffFieldReference> refList = diffSingleReferences(MetaType.LOGICAL_RECORDS, 
                "Source LR", lhsComp, rhsComp, 
                lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
            for (DiffFieldReference ref :refList) {         
                diff.add(ref.getState());
                getFields().add(ref);
            }
            
            // referenced LR target
            lhsComp = null;
            LogicalRecord lhsTrgLR = SAFRApplication.getSAFRFactory().getLogicalRecord(
                lhs.getTargetLRLFAssociation().getAssociatingComponentId(),lhs.getEnvironment().getId());            
            if (lhsTrgLR != null && lhsTrgLR.getId() != 0) {
                lhsComp = lhsTrgLR;
            }
            rhsComp = null;     
            LogicalRecord rhsTrgLR = SAFRApplication.getSAFRFactory().getLogicalRecord(
                rhs.getTargetLRLFAssociation().getAssociatingComponentId(),rhs.getEnvironment().getId());            
            if (rhsTrgLR != null && rhsTrgLR.getId() != 0) {
                rhsComp = rhsTrgLR;
            }
            refList = diffSingleReferences(MetaType.LOGICAL_RECORDS, "Target LR", lhsComp, rhsComp, 
                lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
            for (DiffFieldReference ref :refList) {         
                diff.add(ref.getState());
                getFields().add(ref);
            }

            // referenced LF target
            lhsComp = null;
            LogicalFile lhsTrgLF = SAFRApplication.getSAFRFactory().getLogicalFile(
                lhs.getTargetLRLFAssociation().getAssociatedComponentIdNum(),lhs.getEnvironment().getId());
            if (lhsTrgLF != null && lhsTrgLF.getId() != 0) {
                lhsComp = lhsTrgLF;
            }
            rhsComp = null;     
            LogicalFile rhsTrgLF = SAFRApplication.getSAFRFactory().getLogicalFile(
                rhs.getTargetLRLFAssociation().getAssociatedComponentIdNum(),rhs.getEnvironment().getId());
            if (rhsTrgLF != null && rhsTrgLF.getId() != 0) {
                rhsComp = rhsTrgLF;
            }
            refList = diffSingleReferences(MetaType.LOGICAL_FILES, "Target LF", lhsComp, rhsComp, 
                lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
            for (DiffFieldReference ref :refList) {         
                diff.add(ref.getState());
                getFields().add(ref);
            }

            DiffNode fields = generateSourceFields();
            diff.add(fields.getState());
            addChild(fields);
                        
            if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
                setState(DiffNodeState.Different);
            }
            else {
                setState(DiffNodeState.Same);
            }
            
            storeGenerated(DiffLookupPathStep.class, lhs.getId(), this);                        
        }

    }

}
