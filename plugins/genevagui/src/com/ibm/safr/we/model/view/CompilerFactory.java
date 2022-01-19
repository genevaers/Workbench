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


import java.util.logging.Logger;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.exceptions.SAFRCompilerException;
import com.ibm.safr.we.exceptions.SAFRCompilerParseException;
import com.ibm.safr.we.exceptions.SAFRCompilerUnexpectedException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.preferences.SAFRPreferences;

public class CompilerFactory {
    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.view.CompilerFactory");
    
    // used to compile a view or to check syntax
    final static int CT_ADDITION = 13;

    private static SAFRViewActivationException sva;

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
            return;
        }

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

    private static void checkSyntaxFormatFilter(String text, View view, ViewSource currentSource, ViewColumn col) {
        processViewColumnsFormat(view, currentSource, null, SAFRCompilerErrorType.FORMAT_RECORD_FILTER);
        
        try {
//            compiler.compileFormatFilter(text);
        } catch (SAFRCompilerParseException e) {
            // Compilation error.
//            sva.addCompilerErrorsNew(compiler, currentSource, col, SAFRCompilerErrorType.FORMAT_RECORD_FILTER);            
//            sva.addCompilerWarnings(compiler, currentSource, col, SAFRCompilerErrorType.FORMAT_RECORD_FILTER);
            throw sva;
        }        
    }

    private static void checkSyntaxFormatCalc(String text, View view, ViewSource currentSource, ViewColumn col) {
        processViewColumnsFormat(view, currentSource, col, SAFRCompilerErrorType.FORMAT_COLUMN_CALCULATION);
        
//        try {
////            compiler.compileFormatCalculation(text);
//            sva.addCompilerWarnings(compiler, currentSource, col, SAFRCompilerErrorType.FORMAT_COLUMN_CALCULATION);
//        } catch (SAFRCompilerParseException e) {
//            // Compilation error.
//            sva.addCompilerErrorsNew(compiler, currentSource, col, SAFRCompilerErrorType.FORMAT_COLUMN_CALCULATION);            
//            sva.addCompilerWarnings(compiler, currentSource, col, SAFRCompilerErrorType.FORMAT_COLUMN_CALCULATION);
//            throw sva;
//        }
        
    }

    private static void checkSyntaxExtractAssign(String text, View view, ViewSource currentSource, ViewColumn col) {
        
//        compiler.setViewSource(currentSource.getLrFileAssociation().getAssociatingComponentId(), 
//            currentSource.getLrFileAssociation().getAssociatedComponentIdNum());
//        
//        processViewColumnsExtract(view, currentSource, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT);
//        
//        try {
//            compiler.compileExtractColumn(col.getColumnNo(), text);
//            sva.addCompilerWarnings(compiler, currentSource, col, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT);
//        } catch (SAFRCompilerParseException e) {
//            // Compilation error.
//            sva.addCompilerErrorsNew(compiler, currentSource, col, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT);            
//            sva.addCompilerWarnings(compiler, currentSource, col, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT);
//            throw sva;
//        }        
    }

    private static void checkSyntaxExtractFilter(String text, View view, ViewSource currentSource, ViewColumn col) {
        
//        compiler.setViewSource(currentSource.getLrFileAssociation().getAssociatingComponentId(), 
//            currentSource.getLrFileAssociation().getAssociatedComponentIdNum());
//        
//        processViewColumnsExtract(view, currentSource, SAFRCompilerErrorType.EXTRACT_RECORD_FILTER);
//        
//        try {
//            compiler.compileExtractFilter(text);
//            sva.addCompilerWarnings(compiler, currentSource, col, SAFRCompilerErrorType.EXTRACT_RECORD_FILTER);
//        } catch (SAFRCompilerParseException e) {
//            // Compilation error.
//            sva.addCompilerErrorsNew(compiler, currentSource, col, SAFRCompilerErrorType.EXTRACT_RECORD_FILTER);            
//            sva.addCompilerWarnings(compiler, currentSource, col, SAFRCompilerErrorType.EXTRACT_RECORD_FILTER);
//            throw sva;
//        }        
    }

    private static void checkSyntaxExtractOutput(String text, View view, ViewSource currentSource, ViewColumn col) {
        
//        compiler.setViewSource(currentSource.getLrFileAssociation().getAssociatingComponentId(), 
//            currentSource.getLrFileAssociation().getAssociatedComponentIdNum());
//        
//        processViewColumnsExtract(view, currentSource, SAFRCompilerErrorType.EXTRACT_RECORD_OUTPUT);
//        
//        try {
//            compiler.compileExtractOutput(text);
//            sva.addCompilerWarnings(compiler, currentSource, col, SAFRCompilerErrorType.EXTRACT_RECORD_OUTPUT);
//        } catch (SAFRCompilerParseException e) {
//            // Compilation error.
//            sva.addCompilerErrorsNew(compiler, currentSource, col, SAFRCompilerErrorType.EXTRACT_RECORD_OUTPUT);            
//            sva.addCompilerWarnings(compiler, currentSource, col, SAFRCompilerErrorType.EXTRACT_RECORD_OUTPUT);
//            throw sva;
//        }        
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
    static void checkSyntaxOld(LogicTextType type, String text, View view,
            ViewSource currentSource, ViewColumn col) throws DAOException, SAFRException 
    {
        if ((type == LogicTextType.Extract_Column_Assignment || type == LogicTextType.Extract_Record_Filter)
                && currentSource == null) {
            throw new IllegalArgumentException(
                    "View source cannot be null for Extract record filter and Extract column assignment and Extract output syntax check.");

        } else if ((type == LogicTextType.Extract_Column_Assignment || type == LogicTextType.Format_Column_Calculation)
                && col == null) {
            throw new IllegalArgumentException(
                    "View column cannot be null for Format column calculation and Extract column assignment syntax check.");
        }
        if (text == null || text.trim().equals("")) {
            // no logic text found.
            return;
        }

        SAFRCompilerErrorType safrCompilerErrorType = null;
        if (type == LogicTextType.Format_Record_Filter) {
            safrCompilerErrorType = SAFRCompilerErrorType.FORMAT_RECORD_FILTER;
        } else if (type == LogicTextType.Extract_Column_Assignment) {
            safrCompilerErrorType = SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT;
        } else if (type == LogicTextType.Extract_Record_Filter) {
            safrCompilerErrorType = SAFRCompilerErrorType.EXTRACT_RECORD_FILTER;
        } else if (type == LogicTextType.Format_Column_Calculation) {
            safrCompilerErrorType = SAFRCompilerErrorType.FORMAT_COLUMN_CALCULATION;
        }
        initializeOldCompiler(type, view, currentSource);
        
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
                    sva.addActivationErrorNew(new ViewActivationError(
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

//            try {
//                compiler.setColumnData(col.getColumnNo(), col
//                        .getId(), modifiedDefaultVal, colType, col
//                        .getDataTypeCode(), col.isSigned(), col
//                        .getLength(), col.getDecimals(), col
//                        .getDateTimeFormatCode(), col.getScaling(),
//                        col.getDataAlignmentCode(), col
//                                .getNumericMaskCode(), col
//                                .getStartPosition(), col
//                                .getOrdinalPosition(), null, null,
//                        null, null, null);
//            } catch (SAFRCompilerException ce) {
//                sva.addActivationErrorNew(new ViewActivationError(
//                                currentSource, col,
//                                safrCompilerErrorType,
//                                "ERROR: Error sending column information to compiler. "
//                                        + ce.getMessage()));
//                throw sva;
//            }
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
                    sva.addActivationErrorNew(new ViewActivationError(
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
            if (col.isSortKey()) {
                // send sort title info too
//                try {
//                    compiler.setColumnData(
//                                    col.getColumnNo(),
//                                    col.getId(),
//                                    modifiedDefaultVal,
//                                    colType,
//                                    col.getDataTypeCode(),
//                                    col.isSigned(),
//                                    col.getLength(),
//                                    col.getDecimals(),
//                                    col.getDateTimeFormatCode(),
//                                    col.getScaling(),
//                                    col.getDataAlignmentCode(),
//                                    col.getNumericMaskCode(),
//                                    col.getStartPosition(),
//                                    col.getOrdinalPosition(),
//                                    colSource.getSortKeyTitleLookupPathQueryBean(),
//                                    colSource.getSortKeyTitleLRField(),
//                                    colSource.getEffectiveDateTypeCode(),
//                                    colSource.getEffectiveDateValue(),
//                                    null);
//                } catch (SAFRCompilerUnexpectedException ce) {
//                    sva.addActivationErrorNew(new ViewActivationError(
//                                    currentSource, col,
//                                    safrCompilerErrorType,
//                                    "ERROR: Error sending column information to compiler. "
//                                            + ce.getMessage()));
//                    throw sva;
//                }

            } else {
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
//                try {
//                    compiler.setColumnData(col.getColumnNo(), col
//                            .getId(), modifiedDefaultVal, colType,
//                            col.getDataTypeCode(), col.isSigned(),
//                            col.getLength(), col.getDecimals(), col
//                                    .getDateTimeFormatCode(), col
//                                    .getScaling(), col
//                                    .getDataAlignmentCode(), col
//                                    .getNumericMaskCode(), col
//                                    .getExtractAreaPosition(), col
//                                    .getOrdinalPosition(), null,
//                            null, null, null, null);
//                } catch (SAFRCompilerUnexpectedException ce) {
//                    sva.addActivationErrorNew(new ViewActivationError(
//                                    currentSource, col,
//                                    safrCompilerErrorType,
//                                    "ERROR: Error sending column information to compiler. "
//                                            + ce.getMessage()));
//                    throw sva;
//                }
            }// sort key check
        }// columns loop
    }
    
    /**
     * Initializes the SAFRCompiler based on the type of logic text. This
     * includes passing information of view, view columns and view source.
     * 
     * @param type
     *            the type of logic text.
     * @param view
     *            the View containing logic text.
     * @param currentSource
     *            the View source containing logic text.
     * @throws DAOException
     * @throws SAFRException
     *             {@link SAFRValidationException} is thrown if any of the view
     *             columns has invalid data attributes.
     */
    private static void initializeOldCompiler(LogicTextType type, View view,
            ViewSource currentSource) throws DAOException, SAFRException {
        SAFRCompilerErrorType safrCompilerErrorType = null;
        if (type == LogicTextType.Format_Record_Filter) {
            safrCompilerErrorType = SAFRCompilerErrorType.FORMAT_RECORD_FILTER;
        } else if (type == LogicTextType.Extract_Column_Assignment) {
            safrCompilerErrorType = SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT;
        } else if (type == LogicTextType.Extract_Record_Filter) {
            safrCompilerErrorType = SAFRCompilerErrorType.EXTRACT_RECORD_FILTER;
        } else if (type == LogicTextType.Format_Column_Calculation) {
            safrCompilerErrorType = SAFRCompilerErrorType.FORMAT_COLUMN_CALCULATION;
        }

        // If the view is not yet saved (from view wizard), pass a dummy View
        // Id=1
       
        
        // send view columns to oldComp if its not Extract record filter
        if (type != LogicTextType.Extract_Record_Filter) {
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
                        sva.addActivationErrorOld(new ViewActivationError(
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
                

                // send column info to oldComp
                
                   
                }// logic text type check
            }// columns loop
        }       
    }
    

