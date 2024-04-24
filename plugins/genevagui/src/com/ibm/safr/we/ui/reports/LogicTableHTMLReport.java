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

import org.genevaers.repository.data.CompilerMessage;
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
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.preferences.SAFRPreferences;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;

public class LogicTableHTMLReport extends  GenevaHTMLReport  {

	
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
				h1("Validation Report"),
				getErrorsIfThereAreAny(),
				getWarningsIfThereAreAny(),
				getValidationReport()
			).withClass("w3-container");
	}

	private DomContent getValidationReport() {
		if (SAFRPreferences.isFullActicationReportEnabled()) {
			return div(
					h3("Logic Text"),
					div(pre(CompilerFactory.getLogicText())).withClass("w3-code"),
					h4("Result"),
					div(pre(CompilerFactory.getLogicTableLog()).withClass("w3-code")
					)).withClass("w3-panel w3-card w3-gray");
		} else {
			return null;
		}
	}

	private DomContent getErrorsIfThereAreAny() {
		List<CompilerMessage> errs = WorkbenchCompiler.getErrorMessages();
		if (errs.size() > 0) {
			return div(h3("Errors"), table(tbody(getMeassageHeader(), each(WorkbenchCompiler.getErrorMessages(), m -> getMessageRow(m))))
					.withClass("w3-table-all w3-striped w3-border")).withClass("w3-panel w3-card w3-red");
		} else {
			return div(h3("Status"), p("Validation Completed" + (WorkbenchCompiler.hasWarnings() ? " with Warnings" : ""))
					).withClass("w3-panel w3-card w3-green");
		}
	}
	
	private DomContent getWarningsIfThereAreAny() {
		List<String> ws = CompilerFactory.getWarnings();
		if (ws.size() > 0) {
			return div(h3("Warnings"), table(tbody(getMeassageHeader(), each(WorkbenchCompiler.getWarningMessages(), w -> getMessageRow(w))))
					.withClass("w3-table-all w3-striped w3-border")).withClass("w3-panel w3-card w3-yellow");
		} else {
			return null;
		}
	}

	private DomContent getMessageRow(CompilerMessage w) {
		return tr(
				td(w.getSource().toString()).withClass("w3-border"), 
				td(Integer.toString(w.getColumnNumber())).withClass("w3-border"), 
				td(w.getDetail()).withClass("w3-border"));
	}

	private DomContent getMeassageHeader() {
		return tr(
				th("Source").withClass("w3-border"),
				th("Number").withClass("w3-border"),
				th("Message").withClass("w3-border")
				);
	}

}
