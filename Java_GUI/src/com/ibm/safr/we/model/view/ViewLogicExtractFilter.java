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

import org.genevaers.sycadas.ExtractFilterSycada;
import org.genevaers.sycadas.SycadaFactory;
import org.genevaers.sycadas.SycadaType;

import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.WESycadaDataProvider;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;

public class ViewLogicExtractFilter {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.view.ViewLogicExtractFilter");
    
    private View view;
    private SAFRViewActivationException vaException;
    private List<ViewLogicDependency> viewLogicDependencies;

	private ExtractFilterSycada extractFilterSycada;

    
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
	        } else {
	        	extractLogicDependencies(source);
	        }
		}
    }

    protected void compileExtractFilter(ViewSource source) {
        // Compile extract record filter.
		extractFilterSycada = (ExtractFilterSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_FILTER);
		try {
				extractFilterSycada.processLogic(source.getExtractRecordFilter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(extractFilterSycada.hasSyntaxErrors())
			vaException.addCompilerErrorsNew(extractFilterSycada.getSyntaxErrors(), source, null, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT);
		WESycadaDataProvider dataProvider = new WESycadaDataProvider();
		dataProvider.setEnvironmentID(source.getEnvironmentId());
		dataProvider.setSourceLRID(source.getLrFileAssociation().getAssociatingComponentId());
		extractFilterSycada.generateDependencyDataFrom(dataProvider);
		if(extractFilterSycada.hasDataErrors()) 
			vaException.addCompilerErrorsNew(extractFilterSycada.getDataErrors(), source, null, SAFRCompilerErrorType.EXTRACT_COLUMN_ASSIGNMENT);
//        if (!compiler.getWarnings().isEmpty()) {
//            vaException.addCompilerWarnings(compiler, source, null, SAFRCompilerErrorType.EXTRACT_RECORD_FILTER);
//        }
    }

    protected void extractLogicDependencies(ViewSource source) {
        int depCounter=1;
        // retrieve dependency LR fields and lookup paths used in logic text
        // and add to view logic depend list.
        for (int i : extractFilterSycada.getFieldIDs()) {
            if (i > 0) {
                viewLogicDependencies.add(new ViewLogicDependency(view,
                        LogicTextType.Extract_Record_Filter, source,
                        depCounter++, null, i, null, null));
            }
        }
        Map<Integer, List<Integer>> lookupFieldMap = extractFilterSycada.getLookupIDs();
        if (!lookupFieldMap.isEmpty()) {
            for (int lookup : lookupFieldMap.keySet()) {
                boolean fieldsAvailable = false;
                List<Integer> depLookupFields = lookupFieldMap.get(lookup);
                for (int i : depLookupFields) {
                    if (i > 0) {
                        viewLogicDependencies.add(new ViewLogicDependency(view,
                                LogicTextType.Extract_Record_Filter,
                                source, depCounter++, lookup, i, null, null));
                        fieldsAvailable = true;
                    }
                }
                if (!fieldsAvailable) {
                    // if no fields were used from this lookup path, then
                    // it must have been used independently. Store the
                    // lookup path id in dependencies.
                    viewLogicDependencies.add(new ViewLogicDependency(view,
                            LogicTextType.Extract_Record_Filter, source,
                            depCounter++, lookup, null, null, null));
                }
            }
        }
    }

    
}
