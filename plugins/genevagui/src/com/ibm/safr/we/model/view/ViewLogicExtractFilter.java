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
import org.genevaers.genevaio.dataprovider.CompilerDataProvider;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBCompilerFactory;
import org.genevaers.runcontrolgenerator.workbenchinterface.WBExtractFilterCompiler;
import org.genevaers.repository.*;

import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.WECompilerDataProvider;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRFactory;

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

    public void compile(ViewSource source, WECompilerDataProvider dataProvider) throws DAOException, SAFRException, SAFRViewActivationException, IOException {
		if(source.getExtractRecordFilter() != null) {
	        compileExtractFilter(source, dataProvider);
	        if(vaException.hasErrorOccured()) {
	        	throw vaException;
	        } else {
	        	extractLogicDependencies(source);
	        }
		}
    }

    protected void compileExtractFilter(ViewSource source, CompilerDataProvider dataProvider) {
        // Compile extract record filter.
		extractFilterCompiler = (WBExtractFilterCompiler) WBCompilerFactory.getProcessorFor(WBCompilerType.EXTRACT_FILTER);
		extractFilterCompiler.setDataProvider(dataProvider);
		SAFRFactory factory = SAFRApplication.getSAFRFactory();
		LogicalRecord lr = factory.getLogicalRecordFromLRLFAssociation(source.getLrFileAssociationId(), source.getEnvironmentId());
		addRcgLrToCompiler(lr);
		
		try {
			extractFilterCompiler.syntaxCheckLogic(source.getExtractRecordFilter());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(extractFilterCompiler.hasSyntaxErrors())
			vaException.addCompilerErrorsNew(extractFilterCompiler.getSyntaxErrors(), source, null, SAFRCompilerErrorType.EXTRACT_RECORD_FILTER);
		extractFilterCompiler.generateDependencies();
		if(extractFilterCompiler.hasDataErrors()) 
			vaException.addCompilerErrorsNew(extractFilterCompiler.getDataErrors(), source, null, SAFRCompilerErrorType.EXTRACT_RECORD_FILTER);
    }

    private org.genevaers.repository.components.LogicalRecord addRcgLrToCompiler(LogicalRecord lr) {
		org.genevaers.repository.components.LogicalRecord rcgLR = makeLrAndSaveInRcgRepository(lr);
		for( LRField f :lr.getLRFields()) {
			makeFieldAndAddToLR(lr, f);
		}
		return rcgLR;
	}

	private void makeFieldAndAddToLR(LogicalRecord lr, LRField f) {
		org.genevaers.repository.components.LRField lrf = new org.genevaers.repository.components.LRField();
		lrf.setComponentId(f.getId());
		Code type = f.getDataTypeCode();
		if(type != null) {
			lrf.setDatatype(org.genevaers.repository.components.enums.DataType.values()[type.getGeneralId()]);
		}
		Code dt = f.getDateTimeFormatCode();
		if(dt != null) {
			lrf.setDateTimeFormat(org.genevaers.repository.components.enums.DateCode.values()[dt.getGeneralId()]);
		}
		int l = f.getLength();
		lrf.setLength((short)l);
		lrf.setLrID(lr.getId());
		lrf.setName(f.getName());
		int d = f.getDecimals();
		lrf.setNumDecimalPlaces((short)d);
		int s = f.getScaling();
		lrf.setRounding((short)s);
		lrf.setSigned(f.isSigned());
		int p = f.getPosition();
		lrf.setStartPosition((short)p);
		extractFilterCompiler.addField(lrf);
	}

	private org.genevaers.repository.components.LogicalRecord makeLrAndSaveInRcgRepository(LogicalRecord lr) {
		org.genevaers.repository.components.LogicalRecord rcgLR = new org.genevaers.repository.components.LogicalRecord();
		rcgLR.setComponentId(lr.getId());
		rcgLR.setName(lr.getName());
		extractFilterCompiler.addLR(rcgLR);
		return rcgLR;
	}

	protected void extractLogicDependencies(ViewSource source) {
    	ViewLogicExtractor vle = new ViewLogicExtractor(view, viewLogicDependencies);
    	vle.extractDependencies(extractFilterCompiler, source, LogicTextType.Extract_Record_Filter);
    }

}
