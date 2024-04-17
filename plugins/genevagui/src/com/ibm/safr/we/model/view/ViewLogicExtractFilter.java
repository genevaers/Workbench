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
import java.util.logging.Logger;

import org.genevaers.runcontrolgenerator.workbenchinterface.WBCompilerType;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBCompilerFactory;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBExtractFilterCompiler;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;

public class ViewLogicExtractFilter {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.view.ViewLogicExtractFilter");
    
    private View view;
    private SAFRViewActivationException vaException;
    private List<ViewLogicDependency> viewLogicDependencies;

	private WBExtractFilterCompiler extractFilterCompiler;

    
    public ViewLogicExtractFilter(View view, 
        SAFRViewActivationException vaException, 
        List<ViewLogicDependency> viewLogicDependencies) {
        super();
        this.view = view;
        this.vaException = vaException;
        this.viewLogicDependencies = viewLogicDependencies;
    } 

    public void compile(ViewSource source) throws DAOException, SAFRException, SAFRViewActivationException, IOException {
		if(source.getExtractRecordFilter() != null) {
	        compileExtractFilter(source);
	        if(vaException.hasErrorOccured()) {
	        	throw vaException;
	        }
		}
    }

    protected void compileExtractFilter(ViewSource source) {
        // Compile extract record filter.
		extractFilterCompiler = (WBExtractFilterCompiler) WBCompilerFactory.getProcessorFor(WBCompilerType.EXTRACT_FILTER);
		extractFilterCompiler.run();
        if(extractFilterCompiler.hasErrors()) {
			vaException.addCompilerErrorsNew(extractFilterCompiler.getSyntaxErrors(), source, null, SAFRCompilerErrorType.EXTRACT_RECORD_FILTER);
        }
    }

	protected void extractLogicDependencies(ViewSource source) {
    	ViewLogicExtractor vle = new ViewLogicExtractor(view, viewLogicDependencies);
    }

}
