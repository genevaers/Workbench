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


import java.util.List;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPathStep;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

public class NodeLookupPathStep extends DiffNodeComp {

    private LookupPathStep step;
    private int otherEnv;
    
    public NodeLookupPathStep(LookupPathStep lhs, DiffNodeState state, int otherEnv) {
        super();
        this.step = lhs;
        this.state = state;
        this.otherEnv = otherEnv;
    }

    @SuppressWarnings("unchecked")
    protected DiffNode generateSourceFields() throws SAFRException {
        DiffNode fields = new DiffNodeSection();
        fields.setName("Source Fields");
        fields.setParent(this);
        fields.setState(state);
        List<SAFREnvironmentalComponent> list = (List<SAFREnvironmentalComponent>)(List<?>)step.getSourceFields();
        
        List<DiffNodeComp> flist = nodeChildren(list, state, otherEnv); 
        for (DiffNodeComp node : flist) {
            node.setParent(fields);
            fields.addChild(node);
        }        
        return fields;
    }    
    
    @Override
    protected void generateTree() throws SAFRException {
        NodeLookupPathStep tree = (NodeLookupPathStep) getGenerated(NodeLookupPathStep.class, step.getId());
        if (tree == null) {
            setId(step.getSequenceNumber());
            setEnvID(step.getEnvironmentId());
            setName("Step");
            addIntField("Sequence", step.getSequenceNumber(), step.getEnvironmentId(), state); 
            
            // referenced LR source
            getFields().add(nodeSingleReference(MetaType.LOGICAL_RECORDS, "Source LR", step.getSourceLR(), state, otherEnv)); 
            
            // referenced LR target
            LogicalRecord trgLR = SAFRApplication.getSAFRFactory().getLogicalRecord(
                step.getTargetLRLFAssociation().getAssociatingComponentId(),step.getEnvironment().getId());
            if (trgLR != null && trgLR.getId() == 0) {
                trgLR = null;
            }
            getFields().add(nodeSingleReference(MetaType.LOGICAL_RECORDS, "Target LR", trgLR, state, otherEnv));

            // referenced LF target
            LogicalFile trgLF = SAFRApplication.getSAFRFactory().getLogicalFile(
                step.getTargetLRLFAssociation().getAssociatedComponentIdNum(),step.getEnvironment().getId());
            if (trgLF != null && trgLR.getId() == 0) {
                trgLF = null;
            }
            getFields().add(nodeSingleReference(MetaType.LOGICAL_FILES, "Target LF", trgLF, state, otherEnv)); 
            
            DiffNode fields = generateSourceFields();
            addChild(fields);            
            
            storeGenerated(NodeLookupPathStep.class, step.getId(), this);                        
        }

    }

}
