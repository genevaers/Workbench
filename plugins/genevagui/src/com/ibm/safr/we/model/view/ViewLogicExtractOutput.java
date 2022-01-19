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

import org.genevaers.sycadas.ExtractOutputSycada;
import org.genevaers.sycadas.SycadaFactory;
import org.genevaers.sycadas.SycadaType;

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

    public void compile(ViewSource source) {
        compileExtractOutput(source);
        extractLogicDependencies(source);        
    }

    void compileExtractOutput(ViewSource source) {
        try {
    		extractOutputSycada = (ExtractOutputSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_OUTPUT);
    		try {
    			if(source.getExtractRecordOutput() != null)
    				extractOutputSycada.processLogic(source.getExtractRecordOutput());
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		if(extractOutputSycada.hasSyntaxErrors())
    			vaException.addCompilerErrorsNew(extractOutputSycada.getSyntaxErrors(), source, null, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT);
    		WESycadaDataProvider dataProvider = new WESycadaDataProvider();
    		dataProvider.setEnvironmentID(source.getEnvironmentId());
    		dataProvider.setSourceLRID(source.getLrFileAssociation().getAssociatingComponentId());
    		extractOutputSycada.generateDependencyDataFrom(dataProvider);
    		if(extractOutputSycada.hasDataErrors()) 
    			vaException.addCompilerErrorsNew(extractOutputSycada.getDataErrors(), source, null, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT);
        } catch (SAFRCompilerParseException ee) {
        } 
//        if (!compiler.getWarnings().isEmpty()) {
//            vaException.addCompilerWarnings(compiler, source, null, SAFRCompilerErrorType.EXTRACT_RECORD_OUTPUT);
//        }
    }

    void extractLogicDependencies(ViewSource source) {
        int depCounter=1;
        for (int i : extractOutputSycada.getLFPFAssocIDs()) {
            if (i > 0) {
                viewLogicDependencies.add(new ViewLogicDependency(view,
                        LogicTextType.Extract_Record_Output,
                        source, depCounter++, null, null, null, i));
            }
        }
        for (int i : extractOutputSycada.getFieldIDs()) {
            if (i > 0) {
                viewLogicDependencies.add(new ViewLogicDependency(view,
                        LogicTextType.Extract_Record_Output,
                        source, depCounter++, null, i, null, null));
            }
        }
        for (int i : extractOutputSycada.getExitIDs()) {
            if (i > 0) {
                viewLogicDependencies.add(new ViewLogicDependency(view,
                        LogicTextType.Extract_Record_Output,
                        source, depCounter++, null, null, i, null));
            }
        }
        Map<Integer, List<Integer>> lookupFieldMap = extractOutputSycada.getLookupIDs();
        if (!lookupFieldMap.isEmpty()) {
            for (int lookup : lookupFieldMap.keySet()) {
                boolean fieldsAvailable = false;
                List<Integer> depLookupFields = lookupFieldMap.get(lookup);
                for (int i : depLookupFields) {
                    if (i > 0) {
                        viewLogicDependencies.add(new ViewLogicDependency(
                            view,
                            LogicTextType.Extract_Record_Output,
                            source, depCounter++,
                            lookup, i, null, null));
                        fieldsAvailable = true;
                    }
                }
                if (!fieldsAvailable) {
                    viewLogicDependencies.add(new ViewLogicDependency(view,
                            LogicTextType.Extract_Record_Output,
                            source, depCounter++, lookup, null, null, null));
                }
            }
        }        
    }

}
