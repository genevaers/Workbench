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


import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.diff.DiffBaseNode.DiffNodeState;
import com.ibm.safr.we.model.diff.DiffNode.MetaType;
import com.ibm.safr.we.model.view.View;

public class NodeViewFormatGenerator {

    private DiffNodeComp parent;
    private View view;
    private DiffNodeState state;
    private int otherEnv;
    private DiffNodeSection fm;
    
    public NodeViewFormatGenerator(DiffNodeComp parent, View view, DiffNodeState state, int otherEnv) {
        super();
        this.parent = parent;
        this.view = view;
        this.state = state;
        this.otherEnv = otherEnv;
    }

    protected DiffNode generateTree() throws SAFRException {
        if (view.isFormatPhaseInUse()) {
            return generateFormat();
        } else {
            return null;
        }
    }

    protected DiffNode generateFormat() throws SAFRException {
        fm = new DiffNodeSection();
        fm.setName("Format Phase"); 
        fm.setParent(parent);
        
        DiffNodeSection output = generateOutput();
        output.setParent(fm);
        fm.addChild(output);
        DiffNodeSection exit = generateFormatExit();
        exit.setParent(fm);
        fm.addChild(exit);

        String agg = view.isFormatPhaseRecordAggregationOn() ?  "Aggregate all records with identical sort keys" 
            : "Do not aggregate records";            
        fm.addStringField("Record Aggregation", agg, view.getEnvironmentId(), state);
        
        String lim = view.hasFormatPhaseOutputLimit() ?  
            "Stop Format-Phase processing for this view after " + view.getOutputMaxRecCount() + " record(s) are written" 
            : "Write all eligible records";            
        fm.addStringField("Output Limit", lim, view.getEnvironmentId(), state);
        fm.addLargeStringField("Format Filter", view.getFormatRecordFilter(), view.getEnvironmentId(), state);        
        fm.addBoolField("Zero suppress", view.isSuppressZeroRecords(), view.getEnvironmentId(), state); 
        
        fm.setState(state);
        
        return fm;
    }    
    
    protected DiffNodeSection generateOutput() throws DAOException, SAFRException {
        DiffNodeSection output = new DiffNodeSection();
        output.setName("Output"); 
        
        SAFREnvironmentalComponent outComp = null;
        if (view.getExtractFileAssociation() != null && view.getExtractFileAssociation().getAssociationId() != 0) {
            outComp = SAFRApplication.getSAFRFactory().getLogicalFile(
                view.getExtractFileAssociation().getAssociatingComponentId(),view.getEnvironment().getId());                        
        }
        if (outComp != null) {
            output.getFields().add(fm.nodeSingleReference(MetaType.LOGICAL_FILES, "Output LF", outComp, state, otherEnv));
        }
                
        // referenced PF output
        outComp = null;
        if (view.getExtractFileAssociation() != null && view.getExtractFileAssociation().getAssociationId() != 0) {
            outComp = SAFRApplication.getSAFRFactory().getPhysicalFile(
                view.getExtractFileAssociation().getAssociatedComponentIdNum(),view.getEnvironment().getId());                        
        }        
        if (outComp != null) {
            output.getFields().add(fm.nodeSingleReference(MetaType.PHYSICAL_FILES, "Output PF", outComp, state, otherEnv));
        }
        output.setState(state);
        return output;
    }
    
    protected DiffNodeSection generateFormatExit() throws DAOException, SAFRException {
        DiffNodeSection exit = new DiffNodeSection();
        exit.setName("Exit"); 
        
        exit.getFields().add(fm.nodeSingleReference(MetaType.USER_EXIT_ROUTINES, "Format Exit", view.getFormatExit(), state, otherEnv));        
        exit.addStringField("Format Exit Param", view.getFormatExitParams(), view.getEnvironmentId(), state);       
        exit.setState(state);
        return exit;        
    }
        
}
