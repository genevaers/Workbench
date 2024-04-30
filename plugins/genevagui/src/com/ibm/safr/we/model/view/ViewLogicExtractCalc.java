package com.ibm.safr.we.model.view;

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
import java.util.Set;
import java.util.logging.Logger;

import org.genevaers.runcontrolgenerator.workbenchinterface.WBCompilerType;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBCompilerFactory;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBExtractColumnCompiler;
import org.genevaers.runcontrolgenerator.workbenchinterface.WorkbenchCompiler;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.SAFRApplication;

public class ViewLogicExtractCalc {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.view.ViewLogicExtractFilter");

	private View view;

	private SAFRViewActivationException vaException;

	private Set<Integer> CTCols;

	private WBExtractColumnCompiler extractColumnCompiler;

	public ViewLogicExtractCalc(View view,
        SAFRViewActivationException vaException, 
        List<ViewLogicDependency> viewLogicDependencies) {
        super();
        this.view = view;
        this.vaException = vaException;
    }
    
    public void compile(ViewSource source, Set<Integer> cTCols) {
        CTCols = cTCols;
        setAllSourceColumnInfo(source);
        
		extractColumnCompiler = (WBExtractColumnCompiler) WBCompilerFactory.getProcessorFor(WBCompilerType.EXTRACT_COLUMN);
        for (ViewColumn col : view.getViewColumns().getActiveItems()) {
            processExtractCalculation(source, col);
        }
    }
    
    protected void setAllSourceColumnInfo(ViewSource source) {
        int positionDT = 1;
        int positionCT = 1;
        
		for (ViewColumn col : view.getViewColumns().getActiveItems()) {
			col.getViewColumnSources().get(source.getSequenceNo() - 1);
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

    
    protected int getColumnType(ViewColumn col) {
        
        int colType;
        
        if (col.isSortKey()) {
            colType = Codes.SORTKEY;
        }
        else if (isCTColumn(col)) {
            colType = Codes.CT_AREA;
        }
        else {
            colType = Codes.DT_AREA;;
        }
        return colType;
    }
    
    protected boolean isCTColumn(ViewColumn col) {
        boolean isCTColumn = false;
        if (view.isFormatPhaseInUse()) {
            if (col.getSubtotalTypeCode() != null ||
                CTCols.contains(col.getColumnNo())) {
                isCTColumn = true;
            }
        } 
        return isCTColumn;
    }
    
    
	protected void processExtractCalculation(ViewSource source, ViewColumn col) throws SAFRViewActivationException, SAFRException, DAOException {
		ViewColumnSource colSource = col.getViewColumnSources().get(source.getSequenceNo() - 1);
		String formulaToCompile;
        if((colSource.getExtractColumnAssignment() == null || colSource.getExtractColumnAssignment().length() == 0) || !colSource.getPersistence().equals(SAFRPersistence.OLD)) {
        	formulaToCompile = generateColumnLogic(source, col, colSource);
            colSource.setExtractColumnAssignmentBasic(formulaToCompile);
        } else {
        	formulaToCompile = colSource.getExtractColumnAssignment();
        }
		if (formulaToCompile == null) {
			return;
		}
		compileLogic(source, col, colSource, formulaToCompile);
	}

    protected String generateColumnLogic(ViewSource source, ViewColumn col, ViewColumnSource colSource) {
        String formulaToCompile = null;
        
        checkViewColumnSourceType(source, col, colSource);
        
        switch (colSource.getSourceType().getGeneralId().intValue()) {
        case Codes.CONSTANT:
            formulaToCompile = generateConstantLogic(source, col, colSource);
            break;
        case Codes.SOURCE_FILE_FIELD:
            formulaToCompile = generateSourceFieldLogic(source, col, colSource);
            break;
        case Codes.LOOKUP_FIELD:
            formulaToCompile = generateLookupLogic(source, col, colSource);
            break;
        case Codes.FORMULA:
            formulaToCompile = colSource.getExtractColumnAssignment();
            break;
        }// switch end
        return formulaToCompile;
    }

    protected void checkViewColumnSourceType(ViewSource source, ViewColumn col,
        ViewColumnSource colSource) {
        if (colSource.getSourceType() == null) {
            vaException.addActivationError(new ViewActivationError(source, col,
                SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT,
                "ERROR: View Column Source does not have a valid source type."));                  
            throw vaException;
        }
    }
    
    protected String generateConstantLogic(ViewSource source, ViewColumn col,
        ViewColumnSource colSource) {
        String formulaToCompile = null;
        String colValue = colSource.getSourceValue();
        if (col.getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC) {
            if (colValue == null) {
                formulaToCompile = "COLUMN = \"\"";
            } else if (colValue.contains("REPEAT")
                    || colValue.contains("repeat")) {
                formulaToCompile = "COLUMN = " + colValue;
            } else {
                // replace double quote with escape char "\"
                colValue.replace("\"", "\\\"");
                formulaToCompile = "COLUMN = \"" + colValue + "\"";
            }
        } else {
            // column is not alphanumeric
            if (colValue == null || colValue.equals("")) {
                formulaToCompile = "COLUMN = 0";
            } else {
                // check if the column source value is numeric.
                boolean strFlag = false;
                try {
                    Double.parseDouble(colValue);
                } catch (NumberFormatException nf) {
                    // not a number
                    strFlag = true;
                }
                if (strFlag) {
                    // error.Value cannot be a string.
                	WorkbenchCompiler.addColumnAssignmentErrorMessage("Cannot have alphanumeric value for non-alphanumeric column.");
                } else {
                    formulaToCompile = "COLUMN = " + colValue;
                }
            }
        }
        return formulaToCompile;        
    }

    protected String generateSourceFieldLogic(ViewSource source,
        ViewColumn col, ViewColumnSource colSource) {
        
        String formulaToCompile = null;        
        if (colSource.getLRField() == null) {
            // error. no source field selected
            vaException.addActivationError(new ViewActivationError(
                    source,
                    col,
                    SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT,
                    "ERROR: No source field selected."));
        } else {
            formulaToCompile = "COLUMN = {"
                    + colSource.getLRField().getName() + "}";
        }
        return formulaToCompile;
    }

    protected String generateLookupLogic(ViewSource source, ViewColumn col,
        ViewColumnSource colSource) {
        String formulaToCompile = null;
        
        if (colSource.getLRField() == null) {
            // error. no lookup lr field selected
            vaException.addActivationError(new ViewActivationError(
                    source,
                    col,
                    SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT,
                    "ERROR: No lookup field selected."));
        } else {
            switch (colSource.getEffectiveDateTypeCode()
                    .getGeneralId().intValue()) {
            case Codes.RELPERIOD_CONSTANT:
                // prepare logic text.
                // eg. COLUMN = {Cust_Sales.ID,DATE("20090909",
                // CCYYMMDD)}
                formulaToCompile = "COLUMN = {"
                        + colSource.getLookupQueryBean().getName()
                        + "."
                        + colSource.getLRField().getName()
                        + ",DATE(\"";
                formulaToCompile += colSource
                        .getEffectiveDateValue() == null ? "  "
                        : colSource.getEffectiveDateValue();
                formulaToCompile += "\", CCYYMMDD)}";
                break;
            case Codes.RELPERIOD_RUNDATE:
                // default is always RunDay() function.
                // eg. COLUMN = {Cust_Sales.ID}
                formulaToCompile = "COLUMN = {"
                        + colSource.getLookupQueryBean().getName()
                        + "."
                        + colSource.getLRField().getName()
                        + "}";
                break;
            case Codes.RELPERIOD_SOURCE_FILE_FIELD:
                if (colSource.getEffectiveDateLRField() == null) {
                    // error.no LR field selected for effective
                    // dating.
                    vaException.addActivationError(new ViewActivationError(
                            source,
                            col,
                            SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT,
                            "ERROR: No effective date field selected."));
                } else {
                    // create the LT.
                    // eg. COLUMN = {Cust_Sales.ID,{date_del}}
                    formulaToCompile = "COLUMN = {"
                            + colSource.getLookupQueryBean()
                                    .getName()
                            + "."
                            + colSource.getLRField().getName()
                            + ",{"
                            + colSource.getEffectiveDateLRField()
                                    .getName() + "}}";
                }
                break;
            }
        }
        return formulaToCompile;
    }    
    
    protected void compileLogic(ViewSource source, ViewColumn col, ViewColumnSource colSource, String formulaToCompile) {
    		WorkbenchCompiler.setCurrentColumnNumber(col.getColumnNo());
			WBExtractColumnCompiler extractCompiler = (WBExtractColumnCompiler) WBCompilerFactory.getProcessorFor(WBCompilerType.EXTRACT_COLUMN);
			WorkbenchCompiler.addViewColumnSource(CompilerFactory.makeViewColumnSource(view, source, col, formulaToCompile));
    		extractCompiler.buildAST();
    }

	
}
