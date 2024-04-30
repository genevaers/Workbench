package com.ibm.safr.we.model.view;

import java.util.ArrayList;
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

import org.genevaers.runcontrolgenerator.workbenchinterface.WBCompilerType;
//import org.genevaers.genevaio.ltfile.LTLogger;
import org.genevaers.genevaio.ltfile.LogicTable;
import org.genevaers.runcontrolgenerator.workbenchinterface.ColumnData;
import org.genevaers.runcontrolgenerator.workbenchinterface.ViewColumnSourceData;
import org.genevaers.runcontrolgenerator.workbenchinterface.ViewData;
import org.genevaers.runcontrolgenerator.workbenchinterface.ViewSourceData;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBCompilerFactory;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBExtractColumnCompiler;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBExtractFilterCompiler;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBExtractOutputCompiler;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBFormatCalculationCompiler;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBFormatFilterCompiler;
import org.genevaers.runcontrolgenerator.workbenchinterface.WorkbenchCompiler;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.ReportType;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.ui.reports.ReportUtils;

public class CompilerFactory {
    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.view.CompilerFactory");
    private static View currentView;
    private static ViewColumn currentColumn;
    private static ViewSource currentViewSource;
    // used to compile a view or to check syntax
    final static int CT_ADDITION = 13;

    private static SAFRViewActivationException sva;

	private static String ltLog;

	private static String formatFilterCalculationStack;
	private static Map<Integer, String> colCalcs = new TreeMap<>();
	private static String logicText;
	private static List<String> warnings;
	private static String calcStackString;

	static void checkSyntax(LogicTextType type, String text, View view,
			ViewSource currentSource, ViewColumn col) throws DAOException,
			SAFRException {

		sva = new SAFRViewActivationException(view);
		try {
			checkSyntaxNew(type, text, view, currentSource, col);
		} catch (SAFRViewActivationException e) {
		}
		if (sva.hasErrorOrWarningOccured()) {
			throw sva;
		}
	}
    
    /**
     * Checks the syntax of logic text.
     * 
     * @param type
     *            the type of logic text to be checked.
     * @param text
     *            the actual logic text to be checked.
     * @param view
     *            the View containing this logic text.
     * @param currentSource
     *            the View Source containing this logic text. Required for
     *            Extract record filter and Extract column assignment.
     * @param col
     *            the View Column containing this logic text. Required for
     *            Format column calculation and Extract column assignment.
     * @return 
     * @throws DAOException
     * @throws SAFRException
     *             {@link SAFRValidationException} is thrown if the View
     *             contains columns with invalid data attributes or if the
     *             Syntax of the text is not proper.
     * @throws IllegalArgumentException
     *             if currentSource is null for Extract record filter and
     *             Extract column assignment or col is null for Extract column
     *             assignment and Format column calculation.
     */
    static void checkSyntaxNew(LogicTextType type, String text, View view,
            ViewSource currentSource, ViewColumn col) throws DAOException,
            SAFRException {
        if ((type == LogicTextType.Extract_Column_Assignment || type == LogicTextType.Extract_Record_Filter ||
             type == LogicTextType.Extract_Record_Output) && currentSource == null) {
            throw new IllegalArgumentException(
                    "View source cannot be null for Extract record filter and Extract column assignment and Extract output syntax check.");

        } else if ((type == LogicTextType.Extract_Column_Assignment || type == LogicTextType.Format_Column_Calculation)
                && col == null) {
            throw new IllegalArgumentException(
                    "View column cannot be null for Format column calculation and Extract column assignment syntax check.");
        }
        if (text == null || text.trim().equals("")) {
            // no logic text found.
            return ;
        }
        logicText = text;
        calcStackString = null;
        WorkbenchCompiler.reset();
        currentViewSource = currentSource;
		initialiseCompilerData(view, currentSource, col);

		//Consider this as separate classes Syntax Checkers
        if (type == LogicTextType.Format_Record_Filter) {
            checkSyntaxFormatFilter(text, view, currentSource, col);
        } else if (type == LogicTextType.Extract_Column_Assignment) {
            checkSyntaxExtractAssign(text, view, currentSource, col);
        } else if (type == LogicTextType.Extract_Record_Filter) {
            checkSyntaxExtractFilter(text, view, currentSource, col);
        } else if (type == LogicTextType.Format_Column_Calculation) {
            checkSyntaxFormatCalc(text, view, currentSource, col);
        } else if (type == LogicTextType.Extract_Record_Output) {
            checkSyntaxExtractOutput(text, view, currentSource, col);
        }        
    }

	private static void initialiseCompilerData(View view, ViewSource currentSource, ViewColumn col) {
		//Format logic will have a NULL currentSource
		WorkbenchCompiler.setSQLConnection(DAOFactoryHolder.getDAOFactory().getConnection());
		WorkbenchCompiler.setSchema(DAOFactoryHolder.getDAOFactory().getConnectionParameters().getSchema());
		if(currentSource != null) {
		    WorkbenchCompiler.setEnvironment(currentSource.getEnvironmentId());
		    WorkbenchCompiler.setSourceLRID(currentSource.getLrFileAssociation().getAssociatingComponentId());
		    WorkbenchCompiler.setSourceLFID(currentSource.getLrFileAssociation().getAssociatedComponentIdNum());
	
			WorkbenchCompiler.addView(makeView(view));
			WorkbenchCompiler.addViewSource(makeViewSource(currentSource));
		}
	}

    private static void checkSyntaxFormatFilter(String text, View view, ViewSource currentSource, ViewColumn col) {
        processViewColumnsFormat(view, currentSource, null, SAFRCompilerErrorType.FORMAT_RECORD_FILTER);
		WorkbenchCompiler.addView(makeView(view));
		WorkbenchCompiler.addViewSource(makeViewSource(view.getViewSources().get(0)));
		WBFormatFilterCompiler wbffc = (WBFormatFilterCompiler) WBCompilerFactory.getProcessorFor(WBCompilerType.FORMAT_FILTER);
		calcStackString = wbffc.generateCalcStack(view.getId());
		ReportUtils.openReportEditor(ReportType.LogicTable);
		if(wbffc.hasSyntaxErrors()) {
			sva.addCompilerErrorsNew(wbffc.getSyntaxErrors(), currentSource, col, SAFRCompilerErrorType.FORMAT_RECORD_FILTER);            
			throw sva;
		}
    }

    private static void checkSyntaxFormatCalc(String text, View view, ViewSource currentSource, ViewColumn col) {
        processViewColumnsFormat(view, currentSource, col, SAFRCompilerErrorType.FORMAT_COLUMN_CALCULATION);
		WorkbenchCompiler.addView(makeView(view));
		WorkbenchCompiler.addViewSource(makeViewSource(view.getViewSources().get(0)));
		WorkbenchCompiler.addColumn(getColumnData(col));
		WBFormatCalculationCompiler wbcc = (WBFormatCalculationCompiler) WBCompilerFactory.getProcessorFor(WBCompilerType.FORMAT_CALCULATION);
		calcStackString = wbcc.generateCalcStack(view.getId(), col.getColumnNo());
		ReportUtils.openReportEditor(ReportType.LogicTable);
		if(wbcc.hasSyntaxErrors()) {
			sva.addCompilerErrorsNew(wbcc.getSyntaxErrors(), currentSource, col, SAFRCompilerErrorType.FORMAT_COLUMN_CALCULATION);            
			throw sva;
		}
    }

    private static void checkSyntaxExtractAssign(String text, View view, ViewSource currentSource, ViewColumn col) {
    	currentColumn = col;
		WorkbenchCompiler.addColumn(getColumnData(col));
        WBExtractColumnCompiler extractCompiler = (WBExtractColumnCompiler) WBCompilerFactory.getProcessorFor(WBCompilerType.EXTRACT_COLUMN);
        WorkbenchCompiler.addViewColumnSource(makeViewColumnSource(view, currentSource, col, text));
        extractCompiler.run();
       	ReportUtils.openReportEditor(ReportType.LogicTable);
        if(WorkbenchCompiler.hasErrors()) {
        	ltLog = "Errors detected";
        } else {
           	makeLogicTableLog(WorkbenchCompiler.getXlt());
        }
        if(WorkbenchCompiler.hasWarnings()) {
    		sva.addCompilerWarnings(WorkbenchCompiler.getWarnings(), currentSource, col, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT);
        }
    }
    
    public static List<String> getWarnings() {
    	return WorkbenchCompiler.getWarnings();
    }

	public static void makeLogicTableLog(LogicTable logicTable) {
		ltLog = WorkbenchCompiler.getLogicTableLog();
	}
    
    public static String getLogicTableLog() {
    	if(calcStackString != null) {
    		return calcStackString;
    	} else {
    		return ltLog;
    	}
    }

    private static void checkSyntaxExtractFilter(String text, View view, ViewSource currentSource, ViewColumn col) {
    	currentColumn = col;
    	WBExtractFilterCompiler extractFilterCompiler = (WBExtractFilterCompiler) WBCompilerFactory.getProcessorFor(WBCompilerType.EXTRACT_FILTER);
        extractFilterCompiler.run();
        if(WorkbenchCompiler.hasErrors()) {
    		sva.addCompilerErrorsNew(WorkbenchCompiler.getErrors(), currentSource, col, SAFRCompilerErrorType.EXTRACT_RECORD_FILTER);        	
        } else {
        	ltLog = WorkbenchCompiler.getLogicTableLog();
        	List<Integer> vws = new ArrayList<Integer>();
        	vws.add(view.getId());
        	ReportUtils.openReportEditor(ReportType.LogicTable);
        }
        if(WorkbenchCompiler.hasWarnings()) {
    		sva.addCompilerWarnings(WorkbenchCompiler.getWarnings(), currentSource, col, SAFRCompilerErrorType.EXTRACT_RECORD_FILTER);
        }
    }

    private static void checkSyntaxExtractOutput(String text, View view, ViewSource currentSource, ViewColumn col) {
    	currentColumn = col;
    	WBExtractOutputCompiler extractOutputCompiler = (WBExtractOutputCompiler) WBCompilerFactory.getProcessorFor(WBCompilerType.EXTRACT_OUTPUT);
        extractOutputCompiler.run();
        if(WorkbenchCompiler.hasErrors()) {
    		sva.addCompilerErrorsNew(WorkbenchCompiler.getErrors(), currentSource, col, SAFRCompilerErrorType.EXTRACT_RECORD_OUTPUT);        	
        } else {
        	ltLog = WorkbenchCompiler.getLogicTableLog();
        	List<Integer> vws = new ArrayList<Integer>();
        	vws.add(view.getId());
        	ReportUtils.openReportEditor(ReportType.LogicTable);
        }
        if(WorkbenchCompiler.hasWarnings()) {
    		sva.addCompilerWarnings(WorkbenchCompiler.getWarnings(), currentSource, col, SAFRCompilerErrorType.EXTRACT_RECORD_OUTPUT);
        }
    }

    private static void processViewColumnsFormat(View view,
        ViewSource currentSource, ViewColumn icol,
        SAFRCompilerErrorType safrCompilerErrorType) {
        int colType;
        for (ViewColumn col : view.getViewColumns().getActiveItems()) {
            // send only new and modified columns if the columns were
            // already sent last time.
            try {
                // validate the view column first.
                col.validate();
            } catch (SAFRValidationException sve) {
                // Collect the error in SAFRCompilerException and throw
                // back.
                for (String error : sve.getErrorMessages()) {
                    sva.addActivationError(new ViewActivationError(
                                    currentSource,
                                    col,
                                    SAFRCompilerErrorType.VIEW_PROPERTIES,
                                    error));
                }
                throw sva; // cannot continue.
            }

            // calculate the type of extract area.
            if (col.isSortKey()) {
                colType = Codes.SORTKEY;
            } else {
                colType = Codes.DT_AREA;
            }
            col.setExtractAreaCode(SAFRApplication.getSAFRFactory()
                    .getCodeSet(CodeCategories.EXTRACT)
                    .getCode(colType));

            String modifiedDefaultVal = col.getDefaultValue() == null ? null
                    : col.getDefaultValue().trim();
            if (col == icol) {
                break;
            }            
        }
    }

    private static void processViewColumnsExtract(View view,
        ViewSource currentSource,
        SAFRCompilerErrorType safrCompilerErrorType) {
        int positionDT = 1;
        int positionCT = 1;
        int colType;
        for (ViewColumn col : view.getViewColumns().getActiveItems()) {
            // send only new and modified columns if the columns were
            // already sent last time.
            try {
                // validate the view column first.
                col.validate();
            } catch (SAFRValidationException sve) {
                // Collect the error in SAFRCompilerException and throw
                // back.
                for (String error : sve.getErrorMessages()) {
                    sva.addActivationError(new ViewActivationError(
                                    currentSource,
                                    col,
                                    SAFRCompilerErrorType.VIEW_PROPERTIES,
                                    error));
                }
                throw sva; // cannot continue.
            }

            // calculate the type of extract area.
            if (col.isSortKey()) {
                colType = Codes.SORTKEY;
            } else {
                colType = Codes.DT_AREA;
            }
            col.setExtractAreaCode(SAFRApplication.getSAFRFactory()
                    .getCodeSet(CodeCategories.EXTRACT)
                    .getCode(colType));

            String modifiedDefaultVal = col.getDefaultValue() == null ? null
                    : col.getDefaultValue().trim();

            // View column source information needed for Extract col
            // assignment.
            ViewColumnSource colSource = col.getViewColumnSources()
                    .get(currentSource.getSequenceNo() - 1);
                if (col.getSubtotalTypeCode() != null) {
                    colType = Codes.CT_AREA;
                }
                if (colType == Codes.CT_AREA) {
                    col.setExtractAreaPosition(positionCT);
                    positionCT += CT_ADDITION;
                } else {
                    col.setExtractAreaPosition(positionDT);
                    positionDT += col.getLength();
                }
        }// columns loop
    }
    
    public static ViewData makeView(View v) {
    	ViewData vd = new ViewData();
        vd.setId(v.getId());
        vd.setName(v.getName());
        vd.setTypeValue(v.getTypeCode().getGeneralId());
        vd.setFormatFilter(v.getFormatRecordFilter());
        return vd;
      }
    
    protected static  void setAllSourceColumnInfo(View view, ViewSource source) {
        int positionDT = 1;
        int positionCT = 1;
        
		for (ViewColumn col : view.getViewColumns().getActiveItems()) {
			ViewColumnSource colSource = col.getViewColumnSources().get(source.getSequenceNo() - 1);
			int colType = getColumnType(col);
			col.setExtractAreaCode(SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.EXTRACT).getCode(colType));

			if (colType == Codes.SORTKEY) {
				col.setExtractAreaPosition(null);
			} else if (colType == Codes.CT_AREA) {
				col.setExtractAreaPosition(positionCT);
				positionCT += CompilerFactory.CT_ADDITION;
			} else {
				col.setExtractAreaPosition(positionDT);
				positionDT += col.getLength();
			}
		}
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
        cd.setLength(vc.getLength());
        if(vc.getDataAlignmentCode() != null) {
        	cd.setAlignment(vc.getDataAlignmentCode().getGeneralId());
        } else {
        	
        	cd.setAlignment(0);
        }
        cd.setNumDecimalPlaces(vc.getDecimals());
        cd.setRounding(vc.getScaling());
        cd.setSigned(vc.isSigned());
        cd.setStartPosition(vc.getStartPosition());
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
    

