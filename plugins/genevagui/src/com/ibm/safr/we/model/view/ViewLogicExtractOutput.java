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

import org.genevaers.sycadas.ExtractDependencyAnalyser;
import org.genevaers.sycadas.ExtractOutputSycada;
import org.genevaers.sycadas.SycadaFactory;
import org.genevaers.sycadas.SycadaType;
import org.genevaers.sycadas.SyntaxChecker;

import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.data.WESycadaDataProvider;
import com.ibm.safr.we.exceptions.SAFRCompilerParseException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;

public class ViewLogicExtractOutput {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.view.ViewLogicExtractFilter");
    
    private View view;
    private SAFRViewActivationException vaException;
    private List<ViewLogicDependency> viewLogicDependencies;

	private ExtractOutputSycada extractOutputSycada;
    
    public ViewLogicExtractOutput(View view, 
        SAFRViewActivationException vaException, 
        List<ViewLogicDependency> viewLogicDependencies) {
        super();
        this.view = view;
        this.vaException = vaException;
        this.viewLogicDependencies = viewLogicDependencies;
    }

    public void compile(ViewSource source, WESycadaDataProvider dataProvider) {
        compileExtractOutput(source, dataProvider);
        extractLogicDependencies(source);        
    }

    void compileExtractOutput(ViewSource source, WESycadaDataProvider dataProvider) {
        try {
    		extractOutputSycada = (ExtractOutputSycada) SycadaFactory.getProcessorFor(SycadaType.EXTRACT_OUTPUT);
    		extractOutputSycada.setDataProvider(dataProvider);
    		extractOutputSycada.getFieldsForSourceLr(source.getLrFileAssociation().getAssociatingComponentId());
    		try {
    			if(source.getExtractRecordOutput() != null)
    				extractOutputSycada.syntaxCheckLogic(source.getExtractRecordOutput());
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		if(extractOutputSycada.hasSyntaxErrors())
    			vaException.addCompilerErrorsNew(extractOutputSycada.getSyntaxErrors(), source, null, SAFRCompilerErrorType.EXTRACT_RECORD_OUTPUT);
    		extractOutputSycada.generateDependencies();
    		if(extractOutputSycada.hasDataErrors()) 
    			vaException.addCompilerErrorsNew(extractOutputSycada.getDataErrors(), source, null, SAFRCompilerErrorType.EXTRACT_RECORD_OUTPUT);
        } catch (SAFRCompilerParseException ee) {
        } 
    }

    void extractLogicDependencies(ViewSource source) {
    	ViewLogicExtractor vle = new ViewLogicExtractor(view, viewLogicDependencies);
    	vle.extractDependencies(extractOutputSycada, source, LogicTextType.Extract_Record_Output);
	}
    
}
