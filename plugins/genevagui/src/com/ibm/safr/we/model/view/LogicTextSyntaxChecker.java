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

import org.genevaers.runcontrolgenerator.workbenchinterface.WorkbenchCompiler;

import com.ibm.safr.we.constants.ReportType;
import com.ibm.safr.we.ui.reports.ReportUtils;

public class LogicTextSyntaxChecker {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.view.LogicTextSyntaxChecker");
    private static String calcStackString;

    public static void checkSyntaxFormatFilter(String text, View view) {
        WBCompilerDataStore.initLogicTextProcessor(text, view, null, null);
        calcStackString = WorkbenchCompiler.checkSyntaxFormatFilter(view.getId(), text);
        WBCompilerDataStore.setCalculationStack(calcStackString);
        generateOutputReport();
    }

    public static void checkSyntaxFormatCalc(String text, View view, ViewColumn col) {
        WBCompilerDataStore.initLogicTextProcessor(text, view, null, col);
        calcStackString = WorkbenchCompiler.checkSyntaxFormatCalc(view.getId(), col.getColumnNo(), text);
        WBCompilerDataStore.setCalculationStack(calcStackString);
        generateOutputReport();
    }

    public static void checkSyntaxExtractAssign(String text, View view, ViewSource viewsource, ViewColumn col) {
        WBCompilerDataStore.initLogicTextProcessor(text, view, viewsource, col);
        WorkbenchCompiler.checkSyntaxExtractAssign(view.getId(), viewsource.getSequenceNo(), col.getColumnNo(), text);
        generateOutputReport();
    }

    public static void checkSyntaxExtractFilter(String text, View view, ViewSource viewsource) {
        WBCompilerDataStore.initLogicTextProcessor(text, view, viewsource, null);
        WorkbenchCompiler.checkSyntaxExtractFilter(view.getId(), viewsource.getSequenceNo(), text);
        generateOutputReport();
    }

    public static void checkSyntaxExtractOutput(String text, View view, ViewSource viewsource) {
        WBCompilerDataStore.initLogicTextProcessor(text, view, viewsource, null);
        WorkbenchCompiler.checkSyntaxExtractOutput(view.getId(), viewsource.getSequenceNo(), text);
        generateOutputReport();
    }

    private static void generateOutputReport() {
        WBCompilerDataStore.setLogicTableLog();
        ReportUtils.openReportEditor(ReportType.LogicTable);
    }

}    

