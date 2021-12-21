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


import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.genevaers.sycadas.FormatCalculationSyntaxChecker;
import org.genevaers.sycadas.SycadaFactory;
import org.genevaers.sycadas.SycadaType;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.SAFRApplication;

public class ViewLogicFormatCalc {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.view.ViewLogicFormatCalc");
    
    private View view;
    private SAFRViewActivationException vaException;
	private FormatCalculationSyntaxChecker formatCalulationChecker; 
    private Set<Integer> CTCols = new HashSet<Integer>();

    
    public ViewLogicFormatCalc(View view,
        SAFRViewActivationException vaException) {
        super();
        this.view = view;
        this.vaException = vaException;
    }
    
    public void compile() {
        
        for (ViewColumn col : view.getViewColumns().getActiveItems()) {
            
            checkColumnBinary(col);            
            checkColumnHeaderSize(col);
            
            int colType = getColumnType(col);            
            initializeColumn(col, colType);            
            validateColumn(col);            
            sendCompilerColumnInfo(col, colType);            
            compileLogicText(col);
            
        }
    }

    protected void checkColumnBinary(ViewColumn col) {
        if (!isBinaryColumnAllowed() && col.getDataTypeCode() != null && 
            col.getDataTypeCode().getGeneralId() == Codes.BINARY && col.isVisible()) {
            // error. visible binary column is not allowed for this view type.
            vaException.addActivationErrorNew(new ViewActivationError(null,col,SAFRCompilerErrorType.VIEW_PROPERTIES,
                "ERROR: Hardcopy and Delimited outputs cannot contain Binary visible columns."));
            throw vaException;
        }
    }
        
    protected boolean isBinaryColumnAllowed() {
        boolean binaryColumnAllowed = true;
        if (view.getOutputFormat() == OutputFormat.Format_Report || 
            view.getOutputFormat() == OutputFormat.Format_Delimited_Fields) {
            binaryColumnAllowed = false; 
        }
        return binaryColumnAllowed;
    }
    
    protected void checkColumnHeaderSize(ViewColumn col) {
        // check for headers exceeding column width
        if (view.getOutputFormat() == OutputFormat.Format_Report &&
            col.isVisible() && !col.isSortKey() && (
           (col.getHeading1() != null && col.getHeading1().length() > col.getLength()) ||
           (col.getHeading2() != null && col.getHeading2().length() > col.getLength()) ||
           (col.getHeading3() != null && col.getHeading3().length() > col.getLength())) ) {
            
            vaException.addActivationWarning(new ViewActivationError(null, col,
                SAFRCompilerErrorType.VIEW_PROPERTIES,
                "WARNING: A column header is greater than the column's length"));               
        }
    }
    
    protected int getColumnType(ViewColumn col) {
        // set the column's extract area code
        int colType;
        if (col.isSortKey()) {
            colType = Codes.SORTKEY;
        } else {
            colType = Codes.DT_AREA;
        }
        return colType;
    }

    protected void initializeColumn(ViewColumn col, int colType) {
        col.setExtractAreaCode(SAFRApplication.getSAFRFactory()
                .getCodeSet(CodeCategories.EXTRACT).getCode(colType));
        col.setDefaultValue(col.getDefaultValue() == null ? null : col
                .getDefaultValue().trim());
    }
    
    protected void validateColumn(ViewColumn col) {
        try {
            // check if the column is valid
            col.validate();
        } catch (SAFRValidationException sve) {
            // get errors from sve and attach to compiler errors.
            for (String error : sve.getErrorMessages()) {
                vaException.addActivationErrorNew(new ViewActivationError(null, col,
                        SAFRCompilerErrorType.VIEW_PROPERTIES, error));
            }
        }
        if (vaException.hasNewErrorOccured()) {
            throw vaException; // cannot continue.
        }
    }

    protected void sendCompilerColumnInfo(ViewColumn col, int colType) {
        // send column info to the compiler before compile text can be
        // executed
//        try {
//            compiler.setColumnData(col.getColumnNo(), col.getId(),
//                    col.getDefaultValue(), colType, col.getDataTypeCode(),
//                    col.isSigned(), col.getLength(), col.getDecimals(),
//                    col.getDateTimeFormatCode(), col.getScaling(),
//                    col.getDataAlignmentCode(), col.getNumericMaskCode(),
//                    col.getExtractAreaPosition(), col.getOrdinalPosition(),
//                    null, null, null, null, null);
//        } catch (SAFRCompilerException ce) {
//            ArrayList<String> errors = compiler.getErrors();
//            if (errors != null && errors.size() > 0) {
//                for (String error : errors) {
//                    vaException.addActivationErrorNew(new ViewActivationError(null,
//                            col, SAFRCompilerErrorType.FORMAT_COLUMN_CALCULATION,
//                            error));
//                }
//            }
//            else {
//                vaException.addActivationErrorNew(new ViewActivationError(null, col,
//                        SAFRCompilerErrorType.FORMAT_COLUMN_CALCULATION,
//                        "ERROR: Exception in compiler when sent column information. See log file for details."));
//                logger.severe("Exception in compiler when sent column information. " + ce.getMessage());
//            }
//            throw vaException;              
//        }
    }    
    
    protected void compileLogicText(ViewColumn col) {
    	//So this thing is already checking that the column is not a sortkey
    	//and is not ALPHANUMERIC - which is checked within the compiler at the moment
    	//
        if (!col.isSortKey() && view.isFormatPhaseInUse() && col.getDataTypeCode().getGeneralId() != Codes.ALPHANUMERIC) {
    		formatCalulationChecker = (FormatCalculationSyntaxChecker) SycadaFactory.getProcesorFor(SycadaType.FORMAT_CALCULATION);
    		String calc = col.getFormatColumnCalculation();
    		if(calc != null) {
    			CTCols.add(col.getColumnNo());
	    		formatCalulationChecker.processLogic(col.getFormatColumnCalculation());
	    		if(formatCalulationChecker.hasSyntaxErrors())
	    			vaException.addCompilerErrorsNew(formatCalulationChecker.getSyntaxErrors(), null, col, SAFRCompilerErrorType.FORMAT_COLUMN_CALCULATION);
	//            if (!compiler.getWarnings().isEmpty()) {
	//                vaException.addCompilerWarnings(compiler, null, col, SAFRCompilerErrorType.FORMAT_COLUMN_CALCULATION);                  
	//            }
    		}
        }
    }    
    
    public Set<Integer> getCTCols() {
    	return CTCols;
    }
    
}
