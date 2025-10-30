package com.ibm.safr.we.model.view;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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


import java.util.logging.Logger;

import org.genevaers.runcontrolgenerator.workbenchinterface.ColumnData;
import org.genevaers.runcontrolgenerator.workbenchinterface.ViewColumnSourceData;
import org.genevaers.runcontrolgenerator.workbenchinterface.ViewData;
import org.genevaers.runcontrolgenerator.workbenchinterface.ViewSourceData;
import org.genevaers.runcontrolgenerator.workbenchinterface.WorkbenchCompiler;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ReportType;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.ui.reports.ReportUtils;

public class WBCompilerDataStore {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.view.CompilerFactory");
    private static View currentView;
    private static ViewColumn currentColumn;
    private static ViewSource currentViewSource;
    // used to compile a view or to check syntax
    final static int CT_ADDITION = 13;

    private static String ltLog;

    private static String formatFilterCalculationStack;
    private static Map<Integer, String> colCalcs = new TreeMap<>();
    private static String logicText;
    private static String calcStackString;

    public static List<String> getWarnings() {
    	return WorkbenchCompiler.getWarnings();
    }

    public static String getLogicTableLog() {
        if(calcStackString != null) {
        	return calcStackString;
        } else {
        	return ltLog;
        }
    }

    public static void initLogicTextProcessor(String text, View view, ViewSource currentSource, ViewColumn col) {
        currentViewSource = currentSource;
        currentColumn = col;
        logicText = text;
        calcStackString = null;
        initializeWorkbenchCompiler(view);
    }
    
    public static void initializeWorkbenchCompiler(View view) {
        currentView = view;
        WorkbenchCompiler.reset();
        WorkbenchCompiler.setSQLConnection(DAOFactoryHolder.getDAOFactory().getConnection());
        WorkbenchCompiler.setSchema(DAOFactoryHolder.getDAOFactory().getConnectionParameters().getSchema());
        WorkbenchCompiler.addView(makeView(view));
        WorkbenchCompiler.setEnvironment(view.getEnvironmentId());
        setView(view);
        setupWorkbenchCompilerViewColumnSources(view);
    }

    private static void setupWorkbenchCompilerViewColumnSources(View view) {
        for (ViewSource source : view.getViewSources().getActiveItems()) {
            WorkbenchCompiler.addViewSource(makeViewSource(source));
            WorkbenchCompiler.setSourceLRID(source.getLrFileAssociation().getAssociatingComponentId());
            WorkbenchCompiler.setSourceLFID(source.getLrFileAssociation().getAssociatedComponentIdNum());
            for (ViewColumn col : view.getViewColumns().getActiveItems()) {
                WorkbenchCompiler.addColumn(getColumnData(col));
                ViewColumnSource colSource = col.getViewColumnSources().get(source.getSequenceNo() - 1);
                WorkbenchCompiler.addViewColumnSource(makeViewColumnSource(view, source, col, colSource.getExtractColumnAssignment()));
            }
        }
    }
    
    public static void setLogicTableLog() {
        if (WorkbenchCompiler.hasErrors()) {
            ltLog = "Errors detected";
        } else {
            ltLog = WorkbenchCompiler.getLogicTableLog();
        }
    }

    public static ViewData makeView(View v) {
    	ViewData vd = new ViewData();
        vd.setId(v.getId());
        vd.setName(v.getName());
        vd.setTypeValue(v.getTypeCode().getGeneralId());
        vd.setFormatFilter(v.getFormatRecordFilter());
        return vd;
      }
    
      protected static int getColumnType(ViewColumn col) {
        
        int colType;
        
        if (col.isSortKey()) {
            colType = Codes.SORTKEY;
        }
        else {
            colType = Codes.DT_AREA;;
        }
        return colType;
    }
    
    public static ColumnData getColumnData(ViewColumn vc) {
        ColumnData cd = new ColumnData();
        cd.setColumnId(vc.getId());
        cd.setColumnNumber(vc.getColumnNo());
        cd.setDataTypeValue(vc.getDataTypeCode().getGeneralId());
        if(vc.getDateTimeFormatCode() != null) {
        	cd.setDateCodeValue(vc.getDateTimeFormatCode().getGeneralId());
        } else {
        	cd.setDateCodeValue(0);        	
        }
        cd.setExtractAreaValue(vc.getExtractAreaCode().getGeneralId());
        cd.setExtractAreaPosition(vc.getExtractAreaPosition() != null ? vc.getExtractAreaPosition() : 1);
        if(vc.getExtractAreaCode().getGeneralId() == Codes.SORTKEY && vc.getViewSortKey()!=null) {
            ViewSortKey vsk = vc.getViewSortKey();
            cd.setLength(vsk.getLength());
        } else {
            cd.setLength(vc.getLength());
        }
        if(vc.getDataAlignmentCode() != null) {
        	cd.setAlignment(vc.getDataAlignmentCode().getGeneralId());
        } else {
        	cd.setAlignment(0);
        }
        cd.setNumDecimalPlaces(vc.getDecimals());
        cd.setRounding(vc.getScaling());
        cd.setSigned(vc.isSigned());
        cd.setStartPosition(vc.getExtractAreaPosition() != null ? vc.getExtractAreaPosition() : 1);
        cd.setViewID(vc.getView().getId());
        cd.setColumnCalculation(vc.getFormatColumnCalculation());
        return cd;
    }

    public static ViewSourceData makeViewSource(ViewSource vs) {
        ViewSourceData vsd = new ViewSourceData();
        vsd.setId(vs.getId());
        vsd.setViewID(vs.getView().getId());
        vsd.setExtractFilter(vs.getExtractRecordFilter() != null ? vs.getExtractRecordFilter() : "");
        vsd.setOutputLogic(vs.getExtractRecordOutput() != null ? vs.getExtractRecordOutput() : "");
        vsd.setSequenceNumber(vs.getSequenceNo());
        vsd.setSourceLrId(vs.getLrFileAssociation().getAssociatingComponentId());
        return vsd;
     }

    public static ViewColumnSourceData makeViewColumnSource(View view, ViewSource vs, ViewColumn col, String logicText) {
        ViewColumnSourceData vcs = new ViewColumnSourceData();
        vcs.setColumnId(col.getId());
        vcs.setColumnNumber(col.getColumnNo());
        vcs.setLogicText(logicText);
        vcs.setSequenceNumber(vs.getSequenceNo());
        vcs.setViewSourceId(vs.getId());
        vcs.setViewID(view.getId());
        vcs.setViewSourceLrId(vs.getLrFileAssociation().getAssociatingComponentId());
        return vcs;
     }

    public static String getLogicText() {
    	return logicText;
    }
    
    public static ViewColumn getViewColumn() {
    	return currentColumn;
    }
    
    public static ViewSource getViewSource() {
    	return  currentViewSource;
    }
    
    public static String getCalculationStack() {
    	return calcStackString;
    }
    
    public static void setCalculationStack(String cs) {
        calcStackString = cs;
    }

    public static void setView(View view) {
        currentView = view;
    }

    public static View getView() {
        return currentView;
    }

    public static void setFormatFilterCalculationStack(String generateCalcStack) {
        formatFilterCalculationStack = generateCalcStack;
    }

    public static String getFormatFilterCalculationStack() {
        return formatFilterCalculationStack;
    }

    public static void addColumnCalcStack(int colnum, String stack) {
        colCalcs.put(colnum, stack);
    }

    public static String getColumnCalcStacks(int colnum) {
        return colCalcs.get(colnum);
    }

}    

