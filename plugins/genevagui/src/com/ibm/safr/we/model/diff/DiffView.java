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

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.HeaderFooterItem;
import com.ibm.safr.we.model.view.View;

public class DiffView extends DiffNodeComp {

    private View lhs;
    private View rhs;
    
    public DiffView(View lhs, View rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    protected DiffNode generateGeneral() throws SAFRException {
        DiffNodeSection gi = new DiffNodeSection();
        gi.setName("General"); 
        gi.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        diff.add(gi.addCodeField("Status", lhs.getStatusCode(), rhs.getStatusCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(gi.addStringField("Name", lhs.getName(), rhs.getName(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
        diff.add(gi.addObjectField("Output Phase", lhs.getOutputPhase(), rhs.getOutputPhase(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(gi.addStringField("Output Format", lhs.getOutputFormat().getDesc(), rhs.getOutputFormat().getDesc(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
                
        List<DiffFieldReference> refList = diffSingleReferences(MetaType.CONTROL_RECORDS, "Control Record", 
            lhs.getControlRecord(), rhs.getControlRecord(), 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            gi.getFields().add(ref);
        }
        
        if (DiffNode.weFields) {
            diff.add(gi.addStringField("Comment", lhs.getComment(), rhs.getComment(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(gi.addStringField("Created By", lhs.getCreateBy(), rhs.getCreateBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addDateField("Created Time", lhs.getCreateTime(), rhs.getCreateTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addStringField("Modified By", lhs.getModifyBy(), rhs.getModifyBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addDateField("Last Modified", lhs.getModifyTime(), rhs.getModifyTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addStringField("De/activated By", lhs.getActivatedBy(), rhs.getActivatedBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addDateField("Last De/activated", lhs.getActivatedTime(), rhs.getActivatedTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addStringField("Compiler", lhs.getCompilerVersion(), rhs.getCompilerVersion(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        }
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            gi.setState(DiffNodeState.Different);
        }
        else {
            gi.setState(DiffNodeState.Same);
        }
        return gi;
    }

    
    protected DiffNodeSection generateOutput() throws DAOException, SAFRException {
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();                
        
        DiffNodeSection output = new DiffNodeSection();
        output.setName("Output"); 
        
        // referenced LF output
        SAFREnvironmentalComponent lhsComp = null;
        if (lhs.getExtractFileAssociation() != null && lhs.getExtractFileAssociation().getAssociationId() != 0) {
            lhsComp = SAFRApplication.getSAFRFactory().getLogicalFile(
                lhs.getExtractFileAssociation().getAssociatingComponentId(),lhs.getEnvironment().getId());                        
        }
        SAFREnvironmentalComponent rhsComp = null;
        if (rhs.getExtractFileAssociation() != null && rhs.getExtractFileAssociation().getAssociationId() != 0) {
            rhsComp = SAFRApplication.getSAFRFactory().getLogicalFile(
                rhs.getExtractFileAssociation().getAssociatingComponentId(),rhs.getEnvironment().getId());                        
        }
        List<DiffFieldReference> refList = diffSingleReferences(MetaType.LOGICAL_FILES, "Output LF", lhsComp, rhsComp, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            output.getFields().add(ref);
        }    
        
        // referenced PF output
        lhsComp = null;
        if (lhs.getExtractFileAssociation() != null && lhs.getExtractFileAssociation().getAssociationId() != 0) {
            lhsComp = SAFRApplication.getSAFRFactory().getPhysicalFile(
                lhs.getExtractFileAssociation().getAssociatedComponentIdNum(),lhs.getEnvironment().getId());                        
        }
        rhsComp = null;
        if (rhs.getExtractFileAssociation() != null && rhs.getExtractFileAssociation().getAssociationId() != 0) {
            rhsComp = SAFRApplication.getSAFRFactory().getPhysicalFile(
                rhs.getExtractFileAssociation().getAssociatedComponentIdNum(),rhs.getEnvironment().getId());                        
        }
        refList = diffSingleReferences(MetaType.PHYSICAL_FILES, "Output PF", lhsComp, rhsComp, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            output.getFields().add(ref);
        }
                
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            output.setState(DiffNodeState.Different);
        }
        else {
            output.setState(DiffNodeState.Same);
        }
        
        return output;
    }
    
    protected DiffNodeSection generateExtractExit() throws DAOException, SAFRException {
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        
        DiffNodeSection exit = new DiffNodeSection();
        exit.setName("Exit"); 
        
        List<DiffFieldReference> refList = diffSingleReferences(MetaType.USER_EXIT_ROUTINES, "Write Exit", 
            lhs.getWriteExit(), rhs.getWriteExit(), 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            exit.getFields().add(ref);
        }        
        diff.add(exit.addStringField("Write Exit Param", lhs.getWriteExitParams(), rhs.getWriteExitParams(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            exit.setState(DiffNodeState.Different);
        }
        else {
            exit.setState(DiffNodeState.Same);
        }
        return exit;        
    }
    
    protected DiffNode generateExtract() throws SAFRException {
        DiffNodeSection ex = new DiffNodeSection();
        ex.setName("Extract Phase"); 
        ex.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();                

        if (!lhs.isFormatPhaseInUse() || !rhs.isFormatPhaseInUse()) {
            DiffNodeSection output = generateOutput();
            output.setParent(ex);
            ex.addChild(output);
            diff.add(output.getState());
        }

        DiffNodeSection exit = generateExtractExit();
        exit.setParent(ex);
        ex.addChild(exit);
        diff.add(exit.getState());        
        
        String lhsAgg = lhs.isExtractAggregateBySortKey() ?  "Aggregate all records with identical sort keys using a buffer of " + lhs.getExtractAggregateBufferSize() + " record(s)" 
            : "Do not aggregate records";            
        String rhsAgg = rhs.isExtractAggregateBySortKey() ?  "Aggregate all records with identical sort keys using a buffer of " + lhs.getExtractAggregateBufferSize() + " record(s)" 
            : "Do not aggregate records";            
        diff.add(ex.addStringField("Record Aggregation", lhsAgg, rhsAgg, lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        String lhsLim = lhs.hasExtractPhaseOutputLimit() ?  
            "Stop Extract-Phase processing for this view after " + lhs.getExtractMaxRecords() + " record(s) are written" 
            : "Write all eligible records";            
        String rhsLim = rhs.hasExtractPhaseOutputLimit() ?  
            "Stop Extract-Phase processing for this view after " + rhs.getExtractMaxRecords() + " record(s) are written" 
            : "Write all eligible records";            
        diff.add(ex.addStringField("Output Limit", lhsLim, rhsLim, lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        diff.add(ex.addIntField("Work File Number", lhs.getExtractWorkFileNo(), rhs.getExtractWorkFileNo(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            ex.setState(DiffNodeState.Different);
        }
        else {
            ex.setState(DiffNodeState.Same);
        }
        return ex;
        
    }
    
    
    protected DiffNodeSection generateFormatExit() throws DAOException, SAFRException {
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        
        DiffNodeSection exit = new DiffNodeSection();
        exit.setName("Exit"); 
        
        List<DiffFieldReference> refList = diffSingleReferences(MetaType.USER_EXIT_ROUTINES, "Format Exit", 
            lhs.getFormatExit(), rhs.getFormatExit(), 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            exit.getFields().add(ref);
        }        
        diff.add(exit.addStringField("Format Exit Param", lhs.getFormatExitParams(), rhs.getFormatExitParams(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            exit.setState(DiffNodeState.Different);
        }
        else {
            exit.setState(DiffNodeState.Same);
        }
        return exit;        
    }
    
    protected DiffNode generateFormat() throws SAFRException {

        if (lhs.isFormatPhaseInUse() || rhs.isFormatPhaseInUse()) {
            if (lhs.isFormatPhaseInUse() && rhs.isFormatPhaseInUse()) {
                return generateFormatBoth();
            } else {
                return generateFormatSingle();                
            }
        } else {
            return null;
        }
    }
    
    private DiffNode generateFormatSingle() {
        DiffNodeState state = null;
        View view = null;
        int otherEnv;
        if (lhs.isFormatPhaseInUse()) {
            state = DiffNodeState.Removed;
            view = lhs;
            otherEnv = rhs.getEnvironmentId();
        } else {
            state = DiffNodeState.Added;
            view = rhs;
            otherEnv = lhs.getEnvironmentId();
            
        }           
        NodeViewFormatGenerator gen = new NodeViewFormatGenerator(this, view, state, otherEnv);                            
        return gen.generateTree();
    }
    

    protected DiffNode generateFormatBoth() {
        DiffNodeSection fm = new DiffNodeSection();
        fm.setName("Format Phase"); 
        fm.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        
        DiffNodeSection output = generateOutput();
        output.setParent(fm);
        fm.addChild(output);
        diff.add(output.getState());
        DiffNodeSection exit = generateFormatExit();
        exit.setParent(fm);
        fm.addChild(exit);
        diff.add(exit.getState());        

        String lhsAgg = lhs.isFormatPhaseRecordAggregationOn() ?  "Aggregate all records with identical sort keys" 
            : "Do not aggregate records";            
        String rhsAgg = rhs.isFormatPhaseRecordAggregationOn() ?  "Aggregate all records with identical sort keys" 
            : "Do not aggregate records";            
        diff.add(fm.addStringField("Record Aggregation", lhsAgg, rhsAgg, lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        String lhsLim = lhs.hasFormatPhaseOutputLimit() ?  
            "Stop Format-Phase processing for this view after " + lhs.getOutputMaxRecCount() + " record(s) are written" 
            : "Write all eligible records";            
        String rhsLim = rhs.hasFormatPhaseOutputLimit() ?  
            "Stop Format-Phase processing for this view after " + rhs.getOutputMaxRecCount() + " record(s) are written" 
            : "Write all eligible records";            
        diff.add(fm.addStringField("Output Limit", lhsLim, rhsLim, lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(fm.addLargeStringField("Format Filter", lhs.getFormatRecordFilter(), rhs.getFormatRecordFilter(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));        
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            fm.setState(DiffNodeState.Different);
        }
        else {
            fm.setState(DiffNodeState.Same);
        }
        return fm;
    }
    
    @SuppressWarnings("unchecked")
    protected DiffNode generateSources() throws SAFRException {
        DiffNode srcs = new DiffNodeSection();
        srcs.setName("View Sources"); 
        srcs.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();                

        List<SAFREnvironmentalComponent> lhsList = (List<SAFREnvironmentalComponent>)(List<?>)lhs.getViewSources();
        List<SAFREnvironmentalComponent> rhsList = (List<SAFREnvironmentalComponent>)(List<?>)rhs.getViewSources();
        List<DiffNodeComp> flist = diffChildren(lhsList, rhsList, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());

        for (DiffNodeComp node : flist) {
            node.setParent(node);
            diff.add(node.getState());
            srcs.addChild(node);
        }
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            srcs.setState(DiffNodeState.Different);
        }
        else {
            srcs.setState(DiffNodeState.Same);
        }
        return srcs;        
    }
    
    @SuppressWarnings("unchecked")
    protected DiffNode generateColumns() throws SAFRException {
        DiffNode cols = new DiffNodeSection();
        cols.setName("View Columns"); 
        cols.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();  
        
        List<SAFREnvironmentalComponent> lhsList = (List<SAFREnvironmentalComponent>)(List<?>)lhs.getViewColumns();
        List<SAFREnvironmentalComponent> rhsList = (List<SAFREnvironmentalComponent>)(List<?>)rhs.getViewColumns();
        List<DiffNodeComp> flist = diffChildren(lhsList, rhsList, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());

        for (DiffNodeComp node : flist) {
            node.setParent(cols);
            diff.add(node.getState());
            cols.addChild(node);
        }
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            cols.setState(DiffNodeState.Different);
        }
        else {
            cols.setState(DiffNodeState.Same);
        }
        return cols;        
    }
    
    /**
     * This function is used to retrieve the Header/Footer items from the model
     * and convert it into a string array representing the Header/Footer text
     * for the Left, Center and Right selections.
     * 
     * @param headerFooter
     *            the HeaderFooter object
     * @return string array representing the combined Header/Footer text
     */
    private String[] loadHeaderFooter(View.HeaderFooterItems headerFooter) {
        String[] combinedHFText = new String[3];
        if (headerFooter != null) {
            List<HeaderFooterItem> hfItems = headerFooter.getItems();
            Code functionCode;
            Code justifyCode;
            int row;
            String itemText;
    
            int leftCounter = 1;
            int centerCounter = 1;
            int rightCounter = 1;
            String functionString = "";
            StringBuffer leftString = new StringBuffer();
            StringBuffer centerString = new StringBuffer();
            StringBuffer rightString = new StringBuffer();
    
            // combinedHFText will contain 3 items - one for the text of each
            // selection -Left, Center and Right
    
            for (HeaderFooterItem item : hfItems) {
                functionCode = item.getFunctionCode();
                justifyCode = item.getJustifyCode();
                row = item.getRow();
                itemText = item.getItemText();
    
                if (functionCode.getGeneralId() == Codes.HF_TEXT) { // user text
                    if (itemText != null) {
                        switch (justifyCode.getGeneralId()) {
                        case Codes.LEFT:
                            if (leftCounter != row) {
                                leftString.append(SAFRUtilities.LINEBREAK);
                            }
                            leftString.append(itemText);
                            leftCounter = row;
                            break;
                        case Codes.CENTER:
                            if (centerCounter != row) {
                                centerString.append(SAFRUtilities.LINEBREAK);
                            }
                            centerString.append(itemText);
                            centerCounter = row;
                            break;
                        case Codes.RIGHT:
                            if (rightCounter != row) {
                                rightString.append(SAFRUtilities.LINEBREAK);
                            }
                            rightString.append(itemText);
                            rightCounter = row;
                            break;
                        }
                    }
                } else { // function code
                    functionString = "&[" + functionCode.getDescription() + "]";
                    switch (justifyCode.getGeneralId()) {
                    case Codes.LEFT:
                        if (leftCounter != row) {
                            leftString.append(SAFRUtilities.LINEBREAK);
                        }
                        leftString.append(functionString);
                        leftCounter = row;
                        break;
                    case Codes.CENTER:
                        if (centerCounter != row) {
                            centerString.append(SAFRUtilities.LINEBREAK);
                        }
                        centerString.append(functionString);
                        centerCounter = row;
                        break;
                    case Codes.RIGHT:
                        if (rightCounter != row) {
                            rightString.append(SAFRUtilities.LINEBREAK);
                        }
                        rightString.append(functionString);
                        rightCounter = row;
                        break;
                    }
                }
            }
    
            combinedHFText[0] = leftString.toString();
            combinedHFText[1] = centerString.toString();
            combinedHFText[2] = rightString.toString();
        }
        return combinedHFText;
    }
    
    protected DiffNode generateReportSection() throws SAFRException {
        if (lhs.getOutputFormat() == OutputFormat.Format_Report || 
            rhs.getOutputFormat() == OutputFormat.Format_Report) {
            
            // check for report in both sections 
            if (lhs.getOutputFormat() == OutputFormat.Format_Report && 
                rhs.getOutputFormat() == OutputFormat.Format_Report) {            
                return generateReportBoth();
            } else {
                return generateReportSingle();
            }
        }
        else {
            return null;
        }
    }

    private DiffNode generateReportSingle() {
        DiffNodeState state = null;
        View view = null;
        if (lhs.getOutputFormat() == OutputFormat.Format_Report) {
            state = DiffNodeState.Removed;
            view = lhs;
        } else {
            state = DiffNodeState.Added;
            view = rhs;
            
        }           
        NodeViewReportGenerator gen = new NodeViewReportGenerator(this, view, state);                            
        return gen.generateTree();
    }

    protected DiffNode generateReportBoth() {
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();                
   
        DiffNodeSection report = new DiffNodeSection();
        report.setName("Report"); 
        
        DiffNode details = reportDetails();
        report.addChild(details);
        details.setParent(report);
        diff.add(details.getState());
        
        DiffNode headers = reportHeaders();                    
        report.addChild(headers);
        headers.setParent(report);
        diff.add(headers.getState());
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            report.setState(DiffNodeState.Different);
        }
        else {
            report.setState(DiffNodeState.Same);
        }
        
        return report;
    }

    protected DiffNode reportDetails() {
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        
        DiffNodeSection details = new DiffNodeSection();
        details.setName("Details");
        diff.add(details.addIntField("Lines Per Page", lhs.getLinesPerPage(), rhs.getLinesPerPage(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(details.addIntField("Report Width", lhs.getReportWidth(), rhs.getReportWidth(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            details.setState(DiffNodeState.Different);
        }
        else {
            details.setState(DiffNodeState.Same);
        }
        return details;        
    }
    
    
    protected DiffNode reportHeaders() {
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        
        DiffNodeSection headers = new DiffNodeSection();
        headers.setName("Header/Footer"); 

        // diff header 
        String lhsHeader[] = loadHeaderFooter(lhs.getHeader());
        String rhsHeader[] = loadHeaderFooter(rhs.getHeader());

        diff.add(headers.addLargeStringField("Header Left", lhsHeader[0], rhsHeader[0], lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(headers.addLargeStringField("Header Center", lhsHeader[1], rhsHeader[1], lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(headers.addLargeStringField("Header Right", lhsHeader[2], rhsHeader[2], lhs.getEnvironmentId(), rhs.getEnvironmentId()));

        // diff footer 
        String lhsFooter[] = loadHeaderFooter(lhs.getFooter());
        String rhsFooter[] = loadHeaderFooter(rhs.getFooter());

        diff.add(headers.addLargeStringField("Footer Left", lhsFooter[0], rhsFooter[0], lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(headers.addLargeStringField("Footer Center", lhsFooter[1], rhsFooter[1], lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(headers.addLargeStringField("Footer Right", lhsFooter[2], rhsFooter[2], lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            headers.setState(DiffNodeState.Different);
        }
        else {
            headers.setState(DiffNodeState.Same);
        }
        return headers;
    }

    protected DiffNode generateDelimSection() throws SAFRException {
        if (lhs.getOutputFormat() == OutputFormat.Format_Delimited_Fields || 
            rhs.getOutputFormat() == OutputFormat.Format_Delimited_Fields) {            
            if (lhs.getOutputFormat() == OutputFormat.Format_Delimited_Fields && 
                rhs.getOutputFormat() == OutputFormat.Format_Delimited_Fields) {            
                return generateDelimBoth();
            } else {
                return generateDelimSingle();
            }
        }
        else {
            return null;
        }
    }

    private DiffNode generateDelimSingle() {
        DiffNodeState state = null;
        View view = null;
        if (lhs.getOutputFormat() == OutputFormat.Format_Delimited_Fields) {
            state = DiffNodeState.Removed;
            view = lhs;
        } else {
            state = DiffNodeState.Added;
            view = rhs;
            
        }           
        NodeViewDelimGenerator gen = new NodeViewDelimGenerator(this, view, state);                            
        return gen.generateTree();
    }

    protected DiffNode generateDelimBoth() {
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();                
   
        DiffNodeSection delim = new DiffNodeSection();
        delim.setName("Delimiters"); 
        
        diff.add(delim.addCodeField("Delim Field", lhs.getFileFieldDelimiterCode(), rhs.getFileFieldDelimiterCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(delim.addCodeField("Delim String", lhs.getFileStringDelimiterCode(), rhs.getFileStringDelimiterCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            delim.setState(DiffNodeState.Different);
        }
        else {
            delim.setState(DiffNodeState.Same);
        }
        
        return delim;
    }
    
    @Override
    protected void generateTree() throws SAFRException {
        DiffView tree = (DiffView) getGenerated(DiffView.class, lhs.getId());
        if (tree == null) {
            setId(lhs.getId());
            setName("View");

            Set<DiffNodeState> diff = new HashSet<DiffNodeState>();  
            
            DiffNode gi = generateGeneral();
            addChild(gi);      
            diff.add(gi.getState());

            DiffNode ex = generateExtract();
            addChild(ex);            
            diff.add(ex.getState());

            DiffNode fm = generateFormat();
            if (fm != null) {
                addChild(fm);
                diff.add(fm.getState());
            }

            DiffNode report = generateReportSection();
            if (report != null) {
                addChild(report);
                diff.add(report.getState());
            }

            DiffNode delim = generateDelimSection();
            if (delim != null) {
                addChild(delim);
                diff.add(delim.getState());
            }
            
            DiffNode srcs = generateSources();
            addChild(srcs);            
            diff.add(srcs.getState());

            DiffNode cols = generateColumns();
            addChild(cols);            
            diff.add(cols.getState());
            
            if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
                setState(DiffNodeState.Different);
            }
            else {
                setState(DiffNodeState.Same);
            }
            
            storeGenerated(DiffView.class, lhs.getId(), this);
            addMetadataNode(MetaType.VIEWS, this);         
        }        
    }

}
