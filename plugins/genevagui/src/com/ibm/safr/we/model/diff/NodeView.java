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

import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.View;

public class NodeView extends DiffNodeComp {

    private View view;
    private int otherEnv;
    
    public NodeView(View view, DiffNodeState state, int otherEnv) {
        super();
        this.view = view;
        this.state = state;
        this.otherEnv = otherEnv;
    }

    protected DiffNode generateGeneral() throws SAFRException {
        DiffNodeSection gi = new DiffNodeSection();
        gi.setName("General"); 
        gi.setParent(this);
        gi.addCodeField("Status", view.getStatusCode(), view.getEnvironmentId(), state);
        gi.addStringField("Name", view.getName(), view.getEnvironmentId(), state); 
        gi.addCodeField("Output Format", view.getOutputFormatCode(), view.getEnvironmentId(), state);
        if (view.getOutputFormatCode().getGeneralId().equals(Codes.HARDCOPY)) {
            gi.addIntField("Lines Per Page", view.getLinesPerPage(), view.getEnvironmentId(), state);
            gi.addIntField("Report Width", view.getReportWidth(), view.getEnvironmentId(), state);
        }
        gi.addObjectField("Output Phase", view.getOutputPhase(), view.getEnvironmentId(), state);                
        gi.addObjectField("Output Format", view.getOutputFormat(), view.getEnvironmentId(), state);
        gi.getFields().add(nodeSingleReference(MetaType.CONTROL_RECORDS, "Control Record", view.getControlRecord(), state, otherEnv));                
        
        if (DiffNode.weFields) {
            gi.addStringField("Comment", view.getComment(), view.getEnvironmentId(), state); 
            gi.addStringField("Created By", view.getCreateBy(), view.getEnvironmentId(), state);
            gi.addDateField("Created Time", view.getCreateTime(), view.getEnvironmentId(), state);
            gi.addStringField("Modified By", view.getModifyBy(), view.getEnvironmentId(), state);
            gi.addDateField("Last Modified", view.getModifyTime(), view.getEnvironmentId(), state);
            gi.addStringField("De/activated By", view.getActivatedBy(), view.getEnvironmentId(), state);
            gi.addDateField("Last De/activated", view.getActivatedTime(), view.getEnvironmentId(), state);
            gi.addStringField("Compiler", view.getCompilerVersion(), view.getEnvironmentId(), state);
        }
        gi.setState(state);
        return gi;
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
            output.getFields().add(nodeSingleReference(MetaType.LOGICAL_FILES, "Output LF", outComp, state, otherEnv));
        }
                
        // referenced PF output
        outComp = null;
        if (view.getExtractFileAssociation() != null && view.getExtractFileAssociation().getAssociationId() != 0) {
            outComp = SAFRApplication.getSAFRFactory().getPhysicalFile(
                view.getExtractFileAssociation().getAssociatedComponentIdNum(),view.getEnvironment().getId());                        
        }
        if (outComp != null) {
            output.getFields().add(nodeSingleReference(MetaType.PHYSICAL_FILES, "Output PF", outComp, state, otherEnv));
        }
        output.setState(state);
        return output;
    }
    
    protected DiffNodeSection generateExtractExit() throws DAOException, SAFRException {
        DiffNodeSection exit = new DiffNodeSection();
        exit.setName("Exit"); 
        exit.getFields().add(nodeSingleReference(MetaType.USER_EXIT_ROUTINES, "Write Exit", view.getWriteExit(), state, otherEnv));        
        exit.addStringField("Write Exit Param", view.getWriteExitParams(), view.getEnvironmentId(), state);    
        exit.setState(state);
        return exit;        
    }
    
    protected DiffNode generateExtract() throws SAFRException {
        DiffNodeSection ex = new DiffNodeSection();
        ex.setName("Extract Phase"); 
        ex.setParent(this);
        if (!view.isFormatPhaseInUse()) {
            DiffNodeSection output = generateOutput();
            output.setParent(ex);
            ex.addChild(output);
        }

        DiffNodeSection exit = generateExtractExit();
        exit.setParent(ex);
        ex.addChild(exit);
        
        String agg = view.isExtractAggregateBySortKey() ?  "Aggregate all records with identical sort keys using a buffer of " + view.getExtractAggregateBufferSize() + " record(s)" 
            : "Do not aggregate records";            
        ex.addStringField("Record Aggregation", agg, view.getEnvironmentId(), state);
        
        String lim = view.hasExtractPhaseOutputLimit() ?  
            "Stop Extract-Phase processing for this view after " + view.getExtractMaxRecords() + " record(s) are written" 
            : "Write all eligible records";            
        ex.addStringField("Output Limit", lim, view.getEnvironmentId(), state);
        ex.addIntField("Work File Number", view.getExtractWorkFileNo(), view.getEnvironmentId(), state); 
        ex.setState(state);
        return ex;
        
    }
    
    
    @SuppressWarnings("unchecked")
    protected DiffNode generateSources() throws SAFRException {
        DiffNode srcs = new DiffNodeSection();
        srcs.setName("View Sources"); 
        srcs.setParent(this);

        List<SAFREnvironmentalComponent> list = (List<SAFREnvironmentalComponent>)(List<?>)view.getViewSources();
        List<DiffNodeComp> flist = nodeChildren(list, state, otherEnv);
        for (DiffNodeComp node : flist) {
            node.setParent(srcs);
            srcs.addChild(node);
        }
        srcs.setState(state);
        return srcs;        
    }
    
    @SuppressWarnings("unchecked")
    protected DiffNode generateColumns() throws SAFRException {
        DiffNode cols = new DiffNodeSection();
        cols.setName("View Columns"); 
        cols.setParent(this);
        
        List<SAFREnvironmentalComponent> list = (List<SAFREnvironmentalComponent>)(List<?>)view.getViewColumns();
        List<DiffNodeComp> flist = nodeChildren(list, state, otherEnv);
        for (DiffNodeComp node : flist) {
            node.setParent(cols);
            cols.addChild(node);
        }        
        cols.setState(state);
        
        return cols;
    }
    
    @Override
    protected void generateTree() throws SAFRException {
        NodeView tree = (NodeView) getGenerated(NodeView.class, view.getId());
        if (tree == null) {
            setId(view.getId());
            setEnvID(view.getEnvironmentId());            
            setName("View");
    
            DiffNode gi = generateGeneral();
            addChild(gi);      
    
            DiffNode ex = generateExtract();
            addChild(ex);            

            NodeViewFormatGenerator formatGen = new NodeViewFormatGenerator(this, view, state, otherEnv);
            DiffNode fm = formatGen.generateTree();
            if (fm != null) {
                addChild(fm);
            }
            
            NodeViewReportGenerator reportGen = new NodeViewReportGenerator(this, view, state);
            DiffNode report = reportGen.generateTree();
            if (report != null) {
                addChild(report);
            }

            NodeViewDelimGenerator delimGen = new NodeViewDelimGenerator(this, view, state);
            DiffNode delim = delimGen.generateTree();
            if (delim != null) {
                addChild(delim);
            }
            
            DiffNode srcs = generateSources();
            addChild(srcs);            
    
            DiffNode cols = generateColumns();
            addChild(cols);            
            
            setState(state);
            
            storeGenerated(NodeView.class, view.getId(), this);
            addMetadataNode(MetaType.VIEWS, this);         
        }
    }

}
