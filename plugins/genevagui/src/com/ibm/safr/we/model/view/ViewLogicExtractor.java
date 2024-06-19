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
import org.genevaers.runcontrolgenerator.workbenchinterface.WorkbenchCompiler;

public class ViewLogicExtractor {
	public View view;
	private int depCounter;
	
	private List<ViewLogicDependency> viewLogicDependencies;

	public ViewLogicExtractor(View v, List<ViewLogicDependency> vlds) {
		view = v;
        this.viewLogicDependencies = vlds;
	}
	
    public void extractDependencies() {
        depCounter=1;
        WorkbenchCompiler.getDependenciesStream().forEach(d -> addDependency(d));
        System.out.println(WorkbenchCompiler.getDependenciesAsString());
	}
	
	private Object addDependency(org.genevaers.repository.data.ViewLogicDependency d) {
        viewLogicDependencies.add(new ViewLogicDependency(view, d.getLogicTextType().getTypeValue(), d.getParentId(), depCounter++, d.getLookupPathId(), d.getLrFieldId(), d.getUserExitRoutineId(), d.getFileAssociationId()));
		return null;
	}
}
