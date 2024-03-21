package com.ibm.safr.we.model.view;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


import java.util.List;
import java.util.Set;

import org.genevaers.runcontrolgenerator.workbenchinterface.LookupRef;
import org.genevaers.runcontrolgenerator.workbenchinterface.WorkbenchCompiler;

import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.base.SAFRComponent;

public class ViewLogicExtractor {
	public View view;
	private int depCounter;
	
	private List<ViewLogicDependency> viewLogicDependencies;

	public ViewLogicExtractor(View v, List<ViewLogicDependency> vlds) {
		view = v;
        this.viewLogicDependencies = vlds;
	}
    // Why do we collect the field ids?
    //The source tells us the LR required.
    //We do need the lookup ids, lfpf assocs, and exit ids.
    //There is code checking if fields are used before allowing them to be deleted.
    //As part of LR management and LR delete.
    public void extractDependencies(WorkbenchCompiler extractCompiler, SAFRComponent comp, LogicTextType logicType) {
        depCounter=1;
        //System.out.println(extractCompiler.getDependenciesAsString());
       	extractCompiler.getLFPFAssocIDs().forEach(lfpf -> addLfPfAssoc(lfpf, comp, logicType));
        extractCompiler.getExitIDs().forEach(e -> addExitId(e, comp, logicType)); 
        extractCompiler.getFieldIDs().forEach(f -> addField(comp, f, logicType));
        extractCompiler.getLookupsStream().forEach(lkref -> addLookupRef(comp, lkref, logicType));
	}
	
	private void addLookupRef(SAFRComponent comp, LookupRef lkref, LogicTextType logicType) {
		if(lkref.hasFields()) {
			lkref.getLookFieldIdsStream().forEach(f -> addLookupField(comp, lkref.getId(), f, logicType));
		} else {
			viewLogicDependencies.add(new ViewLogicDependency(view, logicType, comp, depCounter++, lkref.getId(), null, null, null));			
		}
	}

	private void addLookupField(SAFRComponent comp, int lkid, Integer f, LogicTextType logicType) {
        viewLogicDependencies.add(new ViewLogicDependency(view, logicType, comp, depCounter++, lkid, f, null, null));
	}

	private void addField(SAFRComponent comp, Integer f, LogicTextType logicType) {
      viewLogicDependencies.add(new ViewLogicDependency(view, logicType, comp, depCounter++, null, f, null, null));
	}

	private void addLfPfAssoc(int associd, SAFRComponent comp, LogicTextType logicType) {
		viewLogicDependencies.add(new ViewLogicDependency(view, logicType, comp, depCounter++, null, null, null, associd));
	}
    
	private void addExitId(int exit, SAFRComponent comp, LogicTextType logicType) {
    	viewLogicDependencies.add(new ViewLogicDependency(view, logicType, comp, depCounter++, null, null, exit, null));
	}
}
