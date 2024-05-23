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
import java.util.prefs.Preferences;

import org.genevaers.repository.data.CompilerMessage;
import org.genevaers.runcontrolgenerator.workbenchinterface.WorkbenchCompiler;

import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.view.CompilerFactory;
import com.ibm.safr.we.model.view.LogicTextSyntaxChecker;
import com.ibm.safr.we.preferences.SAFRPreferences;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;

public class LogicTableHTMLReport extends  GenevaHTMLReport  {

	
	private Path htmlPath;
	private String viewName;

	public LogicTableHTMLReport() {
	}

	public void setFileName(Path path, String baseName, List<Integer> viewIDs) {
		htmlPath = makeHtmlDirIfNeeded(path);
		viewName = "_Env" + LogicTextSyntaxChecker.getView().getEnvironmentId()+"_V";
		String outputFile = baseName + "_Env" + SAFRApplication.getUserSession().getEnvironment().getId();
		outputFile += ".html";
		reportPath = htmlPath.resolve(outputFile);
		setupDotIfEnabled(path);
	}

	private void setupDotIfEnabled(Path reportPath) {
		Preferences prefs = SAFRPreferences.getSAFRPreferences();
		if(prefs.get(UserPreferencesNodes.DOT_ENABLED, "").equals("Y")) {
			if(prefs.get(UserPreferencesNodes.DOT_FILTER, "").equalsIgnoreCase("Y")) {
				WorkbenchCompiler.setDotFilter();
				WorkbenchCompiler.setDotViews(prefs.get(UserPreferencesNodes.DOT_VIEWS, ""));
				WorkbenchCompiler.setDotCols(prefs.get(UserPreferencesNodes.DOT_COLS, ""));
			}
			WorkbenchCompiler.dotTo(htmlPath.resolve("dots").resolve("XLT" + viewName + ".dot"));
		}
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

	private DomContent addASTDiagrams() {
		if(htmlPath.resolve(htmlPath.resolve("dots").resolve("XLT" + viewName + ".dot")).toFile().exists()) {
			return div(h2("XLT AST Tree"),
					img().attr("src=\"dots/XLT" + viewName + ".dot.svg\""));
		} else {
			return null;
		}
	}

	private DomContent getValidationReport() {
		if (SAFRPreferences.isFullActicationReportEnabled()) {
			return div(
					h3("Logic Text"),
					div(pre(LogicTextSyntaxChecker.getLogicText())).withClass("w3-code"),
					h4("Result"),
					div(pre(LogicTextSyntaxChecker.getLogicTableLog()).withClass("w3-code"),
					addASTDiagrams()
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
		List<String> ws = LogicTextSyntaxChecker.getWarnings();
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
