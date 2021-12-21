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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.genevaers.sycadas.ExtractColumnSycada;
import org.genevaers.sycadas.SycadaFactory;
import org.genevaers.sycadas.SycadaType;
import org.genevaers.sycadas.dataprovider.SycadaDataProvider;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.WESycadaDataProvider;
import com.ibm.safr.we.exceptions.SAFRCompilerException;
import com.ibm.safr.we.exceptions.SAFRCompilerParseException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.SAFRApplication;

public class ViewLogicExtractCalc {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.view.ViewLogicExtractFilter");

    private View view;
    private SAFRViewActivationException vaException;
    //private ISAFRCompiler compiler;
    private List<ViewLogicDependency> viewLogicDependencies;
    private Set<Integer> CTCols;

	private ExtractColumnSycada extractColumnSycada;
    
    public ViewLogicExtractCalc(View view,
        SAFRViewActivationException vaException, 
        List<ViewLogicDependency> viewLogicDependencies) {
        super();
        this.view = view;
        this.vaException = vaException;
        //this.compiler = compiler;
        this.viewLogicDependencies = viewLogicDependencies;
    }
    
    public void compile(ViewSource source, Set<Integer> cTCols) {
        CTCols = cTCols;
        setAllSourceColumnInfo(source);
        
        for (ViewColumn col : view.getViewColumns().getActiveItems()) {
            processExtractCalculation(source, col);
        }
    }
    
    protected void setAllSourceColumnInfo(ViewSource source) {
        // get CT columns from the compiler
        //CTCols = compiler.getCTColumns();        
        
        // compile extract column assignment
        int positionDT = 1;
        int positionCT = 1;
        
        for (ViewColumn col : view.getViewColumns().getActiveItems()) {
            ViewColumnSource colSource = col.getViewColumnSources().get(source.getSequenceNo() - 1);
            int colType = getColumnType(col);
            col.setExtractAreaCode(SAFRApplication.getSAFRFactory()
                .getCodeSet(CodeCategories.EXTRACT).getCode(colType));
            
            if (colType == Codes.SORTKEY) {
                // make sure column position is reset so that the ViewColumn will be updated
                col.setExtractAreaPosition(null);
                setExtractCalcSortColumnInfo(source, col, colSource);
            } else if (colType == Codes.CT_AREA) {                
                col.setExtractAreaPosition(positionCT);
                positionCT += CompilerFactory.CT_ADDITION;
                setExtractCalcNormalColumnInfo(source, col, colType);
            } else {
                col.setExtractAreaPosition(positionDT);
                positionDT += col.getLength();
                setExtractCalcNormalColumnInfo(source, col, colType);
            }    
            
        }// columns loop
    }

    protected void setExtractCalcSortColumnInfo(ViewSource source, ViewColumn col, ViewColumnSource colSource) {
//        int colType;
//        // send sort title info too
//        ViewSortKey colKey = col.getViewSortKey();
//        colType = Codes.SORTKEY;
//        try {
//            compiler.setColumnData(col.getColumnNo(), col.getId(),
//                    col.getDefaultValue(), colType,
//                    colKey.getDataTypeCode(), colKey.isSigned(),
//                    colKey.getLength(), colKey.getDecimalPlaces(),
//                    colKey.getDateTimeFormatCode(), 0,
//                    colKey.getTitleAlignment(), col.getNumericMaskCode(),
//                    colKey.getStartPosition(), 0,
//                    colSource.getSortKeyTitleLookupPathQueryBean(),
//                    colSource.getSortKeyTitleLRField(),
//                    colSource.getEffectiveDateTypeCode(),
//                    colSource.getEffectiveDateValue(), null);
//        } catch (SAFRCompilerException ce) {
//            ArrayList<String> errors = compiler.getErrors();
//            if (errors != null && errors.size() > 0) {
//                for (String error : errors) {
//                    vaException.addActivationErrorNew(new ViewActivationError(source,
//                            col, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT,
//                            error));
//                }
//            }
//            else {
//                vaException.addActivationErrorNew(new ViewActivationError(source, col,
//                        SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT,
//                        "ERROR: Exception in compiler when sent column information. See log file for details."));
//                logger.severe("Exception in compiler when sent column information. " + ce.getMessage());
//            }                                               
//            throw vaException;
//        }
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
    
    protected void setExtractCalcNormalColumnInfo(ViewSource source, ViewColumn col, int colType) {
//        try {
//            compiler.setColumnData(col.getColumnNo(), col.getId(),
//                    col.getDefaultValue(), colType,
//                    col.getDataTypeCode(), col.isSigned(),
//                    col.getLength(), col.getDecimals(),
//                    col.getDateTimeFormatCode(), col.getScaling(),
//                    col.getDataAlignmentCode(),
//                    col.getNumericMaskCode(),
//                    col.getExtractAreaPosition(),
//                    col.getOrdinalPosition(), null, null, null,
//                    null, null);
//        } catch (SAFRCompilerException ce) {
//            ArrayList<String> errors = compiler.getErrors();
//            if (errors != null && errors.size() > 0) {
//                for (String error : errors) {
//                    vaException.addActivationErrorNew(new ViewActivationError(source,
//                            col, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT,
//                            error));
//                }
//            }
//            else {
//                vaException.addActivationErrorNew(new ViewActivationError(source, col,
//                        SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT,
//                        "ERROR: Exception in compiler when sent column information. See log file for details."));
//                logger.severe("Exception in compiler when sent column information. " + ce.getMessage());
//            }
//            throw vaException;
//        }
    }
    
    protected void processExtractCalculation(ViewSource source, ViewColumn col) 
        throws SAFRViewActivationException, SAFRException, DAOException {
        
        ViewColumnSource colSource = col.getViewColumnSources().get(source.getSequenceNo() - 1);        
        String formulaToCompile = generateColumnLogic(source, col, colSource);        
        if (formulaToCompile == null) {
            return;
        }        
        compileLogic(source, col, colSource, formulaToCompile);   
        if(vaException.hasErrorOccured()) {
        	throw vaException;
        } else {
        	extractDependencies(colSource);
        }
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
            vaException.addActivationErrorNew(new ViewActivationError(source, col,
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
                // error.Cannot have blank value for non-alpha cols.
                vaException.addActivationErrorNew(new ViewActivationError(
                        source,
                        col,
                        SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT,
                        "ERROR: Cannot have blank value for non-alphanumeric column."));
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
                    vaException.addActivationErrorNew(new ViewActivationError(
                            source,
                            col,
                            SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT,
                            "ERROR: Cannot have alphanumeric value for non-alphanumeric column."));
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
            vaException.addActivationErrorNew(new ViewActivationError(
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
            vaException.addActivationErrorNew(new ViewActivationError(
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
                    vaException.addActivationErrorNew(new ViewActivationError(
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
        try {
            colSource.setExtractColumnAssignmentBasic(formulaToCompile);
            //compiler.compileExtractColumn(col.getColumnNo(), formulaToCompile);
    		extractColumnSycada = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
    		extractColumnSycada.processLogic(formulaToCompile);
    		if(extractColumnSycada.hasSyntaxErrors())
    			vaException.addCompilerErrorsNew(extractColumnSycada.getSyntaxErrors(), source, col, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT);
    		WESycadaDataProvider dataProvider = new WESycadaDataProvider();
    		dataProvider.setEnvironmentID(source.getEnvironmentId());
    		dataProvider.setSourceLRID(source.getLrFileAssociation().getAssociatingComponentId());
    		extractColumnSycada.generateDependencyDataFrom(dataProvider);
    		if(extractColumnSycada.hasDataErrors()) 
    			vaException.addCompilerErrorsNew(extractColumnSycada.getDataErrors(), source, col, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT);
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//        if (!compiler.getWarnings().isEmpty()) {
//            vaException.addCompilerWarnings(compiler, source, col, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT);
//        }
    }

    protected void extractDependencies(ViewColumnSource colSource) {
        // get the dependencies
        int depCounter=1;
        if (colSource.getSourceType().getGeneralId().intValue() == Codes.FORMULA) {
        	//extractColumnSycada.getLFPFAssocIDs();
            //int[] depLFPFs = compiler.getPhysicalFiles();
            for (int i : extractColumnSycada.getLFPFAssocIDs()) {
                if (i > 0) {
                    viewLogicDependencies.add(new ViewLogicDependency(view,
                            LogicTextType.Extract_Column_Assignment,
                            colSource, depCounter++, null, null, null, i));
                }
            }
            //int[] depExits = compiler.getUserExits();
            for (int i : extractColumnSycada.getExitIDs()) {
                if (i > 0) {
                    viewLogicDependencies.add(new ViewLogicDependency(view,
                            LogicTextType.Extract_Column_Assignment,
                            colSource, depCounter++, null, null, i, null));
                }
            }
        }
        //int[] depLRFields1 = compiler.getLRFields();
        for (int i : extractColumnSycada.getFieldIDs()) {
            if (i > 0) {
                viewLogicDependencies.add(new ViewLogicDependency(view,
                        LogicTextType.Extract_Column_Assignment,
                        colSource, depCounter++, null, i, null, null));
            }
        }
		Map<Integer, List<Integer>> lookupFieldMap1 = extractColumnSycada
				.getLookupIDs();
		if (!lookupFieldMap1.isEmpty()) {
			for (int lookup : lookupFieldMap1.keySet()) {
				boolean fieldsAvailable = false;
				List<Integer> depLookupFields = lookupFieldMap1.get(lookup);
				if (depLookupFields != null) {

					for (int i : depLookupFields) {
						if (i > 0) {
							viewLogicDependencies.add(new ViewLogicDependency(
									view,
									LogicTextType.Extract_Column_Assignment,
									colSource, depCounter++, lookup, i, null,
									null));
							fieldsAvailable = true;
						}
					}
								
				}
				if (!fieldsAvailable) {
					// if no fields were used from this lookup path,
					// then
					// it must have been used independently. Store the
					// lookup path id in dependencies.
					viewLogicDependencies.add(new ViewLogicDependency(view,
							LogicTextType.Extract_Column_Assignment,
							colSource, depCounter++, lookup, null, null,
							null));
				}
			}
			
		}
	}
    
}
