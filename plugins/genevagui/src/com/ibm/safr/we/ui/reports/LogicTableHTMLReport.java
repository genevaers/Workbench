package com.ibm.safr.we.ui.reports;

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


import static j2html.TagCreator.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.genevaers.runcontrolgenerator.workbenchinterface.WorkbenchCompiler;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ViewMappingsReportQueryBean;
import com.ibm.safr.we.model.query.ViewPropertiesReportQueryBean;
import com.ibm.safr.we.model.query.ViewSortKeyReportQueryBean;
import com.ibm.safr.we.model.query.ViewSourcesReportQueryBean;
import com.ibm.safr.we.model.view.CompilerFactory;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;

/************************************************************************************************************************/
/************************************************************************************************************************/

public class LogicTableHTMLReport extends  GenevaHTMLReport  {

	// The Logic table that has been build will be available in the WB Interface 
	// So get it and generate its string when needed
	private String lt;
	
	public LogicTableHTMLReport() {
	}

	public void setFileName(Path path, String baseName, List<Integer> viewIDs) {
		Path htmlPath = makeHtmlDirIfNeeded(path);
		String outputFile = baseName + "_Env" + SAFRApplication.getUserSession().getEnvironment().getId();
		outputFile += ".html";
		reportPath = htmlPath.resolve(outputFile);
	}

	@Override
	protected ContainerTag<DivTag> bodyContent() {
		return div(
				h1("Logic Report"),
				getheader(),
				h3("Logic Text"),
				pre(CompilerFactory.getLogicText()),
				h3("Abstract Syntax Tree"),
				getWarningsIfThereAreAny(),
				h3("Logic Table"),
				pre(CompilerFactory.getLogicTableLog())
			);
	}

	private DomContent getWarningsIfThereAreAny() {
		List<String> ws = CompilerFactory.getWarnings();
		if(ws.size() > 0) {
		return div(
					h3("Warnings"),
					each(ws, w -> pre(w)));
					
		} else return h4("No warnings");
	}


	private DomContent getheader() {
		if(CompilerFactory.getViewColumn() != null) {
			return 	h2("Column Number " + CompilerFactory.getViewColumn().getColumnNo());
 		} else {
			return 	h2("Source "); 			
 		}
	}

}
