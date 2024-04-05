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


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.genevaers.runcontrolgenerator.workbenchinterface.WBCompilerType;
import org.genevaers.genevaio.dataprovider.CompilerDataProvider;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBCompilerFactory;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBExtractOutputCompiler;

import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.data.WECompilerDataProvider;
import com.ibm.safr.we.exceptions.SAFRCompilerParseException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;

public class ViewLogicExtractOutput {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.view.ViewLogicExtractFilter");
    
    private View view;
    private SAFRViewActivationException vaException;
    private List<ViewLogicDependency> viewLogicDependencies;

	private WBExtractOutputCompiler extractOutputCompiler;
    
    public ViewLogicExtractOutput(View view, 
        SAFRViewActivationException vaException, 
        List<ViewLogicDependency> viewLogicDependencies) {
        super();
        this.view = view;
        this.vaException = vaException;
        this.viewLogicDependencies = viewLogicDependencies;
    }

    public void compile(ViewSource source) {
        compileExtractOutput(source);
        extractLogicDependencies(source);        
    }

    void compileExtractOutput(ViewSource source){
        try {
    		extractOutputCompiler = (WBExtractOutputCompiler) WBCompilerFactory.getProcessorFor(WBCompilerType.EXTRACT_OUTPUT);
    		if(source.getExtractRecordOutput() != null)
				extractOutputCompiler.syntaxCheckLogic(source.getExtractRecordOutput());
    		if(extractOutputCompiler.hasSyntaxErrors())
    			vaException.addCompilerErrorsNew(extractOutputCompiler.getSyntaxErrors(), source, null, SAFRCompilerErrorType.EXTRACT_RECORD_OUTPUT);
//    		extractOutputCompiler.generateDependencies();
//    		if(extractOutputCompiler.hasDataErrors()) 
//    			vaException.addCompilerErrorsNew(extractOutputCompiler.getDataErrors(), source, null, SAFRCompilerErrorType.EXTRACT_RECORD_OUTPUT);
        } catch (SAFRCompilerParseException ee) {
        } 
    }

    void extractLogicDependencies(ViewSource source) {
    	ViewLogicExtractor vle = new ViewLogicExtractor(view, viewLogicDependencies);
    	vle.extractDependencies(extractOutputCompiler, source, LogicTextType.Extract_Record_Output);
	}
    
}
