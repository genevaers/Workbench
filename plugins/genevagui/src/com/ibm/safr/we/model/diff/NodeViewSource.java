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
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.ViewSource;

public class NodeViewSource extends DiffNodeComp {

    private ViewSource src;
    private int otherEnv;
    
    public NodeViewSource(ViewSource src, DiffNodeState state, int otherEnv) {
        super();
        this.src = src;
        this.state = state;
        this.otherEnv = otherEnv;
    }

    @Override
    protected void generateTree() throws SAFRException {
        NodeViewSource tree = (NodeViewSource) getGenerated(NodeViewSource.class, src.getId());
        if (tree == null) {
            setId(src.getId());
            setDispId(src.getSequenceNo().toString());
            setEnvID(src.getEnvironmentId());            
            setName("View Source");                        
            addIntField("Sequence", src.getSequenceNo(), src.getEnvironmentId(), state); 
            
            DiffNode input = generateInput();
            addChild(input);     

            DiffNode output = generateOutput();
            addChild(output);     
            
            storeGenerated(NodeViewSource.class, src.getId(), this);            
        }
        
    }
    
    protected DiffNode generateInput() throws SAFRException {
        DiffNodeSection input = new DiffNodeSection();
        input.setName("Extract-Phase Input");
        input.setParent(this);
        input.setState(state);
        
        SAFREnvironmentalComponent comp = null;
        if (src.getLrFileAssociation() != null && src.getLrFileAssociation().getAssociationId() != 0) {
            // referenced LR source
            comp = SAFRApplication.getSAFRFactory().getLogicalRecord(
                src.getLrFileAssociation().getAssociatingComponentId(),src.getEnvironment().getId());            
        }
        input.getFields().add(nodeSingleReference(MetaType.LOGICAL_RECORDS, "Logical Record", comp, state, otherEnv)); 

        // referenced LF source
        comp = null;
        if (src.getLrFileAssociation() != null && src.getLrFileAssociation().getAssociationId() != 0) {                
            comp = SAFRApplication.getSAFRFactory().getLogicalFile(
                src.getLrFileAssociation().getAssociatedComponentIdNum(),src.getEnvironment().getId());
        }
        input.getFields().add(nodeSingleReference(MetaType.LOGICAL_FILES, "Logical File", comp, state, otherEnv)); 
        
        input.addLargeStringField("Extract Record Filter", src.getExtractRecordFilter(), src.getEnvironmentId(), state); 
            
        return input;
    }

    private DiffNode generateOutput() {
        DiffNodeSection output = new DiffNodeSection();
        output.setName("Extract-Phase Output");
        output.setParent(this);
        output.setState(state);
        
        // referenced LF source
        SAFREnvironmentalComponent comp = null;
        if (src.getExtractFileAssociation() != null && src.getExtractFileAssociation().getAssociationId() != 0) {                
            comp = SAFRApplication.getSAFRFactory().getLogicalFile(
                src.getExtractFileAssociation().getAssociatingComponentId(),src.getEnvironment().getId());
        }
        output.getFields().add(nodeSingleReference(MetaType.LOGICAL_FILES, "Logical File", comp, state, otherEnv)); 

        // referenced PF source
        comp = null;
        if (src.getExtractFileAssociation() != null && src.getExtractFileAssociation().getAssociationId() != 0) {                
            comp = SAFRApplication.getSAFRFactory().getPhysicalFile(
                src.getExtractFileAssociation().getAssociatedComponentIdNum(),src.getEnvironment().getId());
        }
        output.getFields().add(nodeSingleReference(MetaType.LOGICAL_FILES, "Physical File", comp, state, otherEnv)); 

        // referenced exit source
        output.getFields().add(nodeSingleReference(MetaType.USER_EXIT_ROUTINES, "Write Exit", src.getWriteExit(), state, otherEnv));
        output.addStringField("Write Exit Param", src.getWriteExitParams(), src.getEnvironmentId(),state);        
        output.addLargeStringField("Extract Output Logic", src.getExtractRecordOutput(), src.getEnvironmentId(), state); 
            
        return output;
    }

    

}
