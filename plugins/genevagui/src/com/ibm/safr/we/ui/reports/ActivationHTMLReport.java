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
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.model.view.WBCompilerDataStore;
import com.ibm.safr.we.preferences.SAFRPreferences;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;

/************************************************************************************************************************/
/************************************************************************************************************************/

public class ActivationHTMLReport extends  GenevaHTMLReport  {

	private Path htmlPath;
	private String viewName;

	public ActivationHTMLReport() {
	}

	public void setFileName(Path path, String baseName, List<Integer> viewIDs) {
		htmlPath = makeHtmlDirIfNeeded(path);
		viewName = "_Env" + WBCompilerDataStore.getView().getEnvironmentId()+"_V";
		if(viewIDs.isEmpty()) {		
			viewName += WBCompilerDataStore.getView().getId();
		} else {
			viewName += viewIDs.get(0);			
		}
		String outputFile = baseName + viewName;
		String outputFileHTML = outputFile + ".html";
		reportPath = htmlPath.resolve(outputFileHTML);
		setupDotIfEnabled(path);
		System.out.println("Actvation Report Path " + reportPath);
	}

	
	private void setupDotIfEnabled(Path reportPath) {
		Preferences prefs = SAFRPreferences.getSAFRPreferences();
		if(prefs.get(UserPreferencesNodes.DOT_ENABLED, "").equals("Y")) {
			if(prefs.get(UserPreferencesNodes.DOT_FILTER, "").equalsIgnoreCase("Y")) {
				WorkbenchCompiler.setDotFilter();
				WorkbenchCompiler.setDotViews(prefs.get(UserPreferencesNodes.DOT_VIEWS, ""));
				WorkbenchCompiler.setDotCols(prefs.get(UserPreferencesNodes.DOT_COLS, ""));
			}
			htmlPath.resolve("dots").toFile().mkdir();
			WorkbenchCompiler.dotTo(htmlPath.resolve("dots").resolve("XLT" + viewName + ".dot"));
		}
	}

	@Override
	protected ContainerTag<DivTag> bodyContent() {
		return div(
				h1("Activation Report "),
				h3("Compiler version " + WorkbenchCompiler.getVersion()),
				getheader(),
				getStatus(),
				getWarningsIfThereAreAny(),
				showDetailsIfRequired()
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

	private DomContent showDetailsIfRequired() {
		if (SAFRPreferences.isFullActicationReportEnabled()) {
			return div(
					getExtractLogic(), 
					getLogicTable(),
					getFormatFilterReport(), 
					getFormatColumnCalculations(), 
					getDependencies(),
					addASTDiagrams());
		} else {
			return null;
		}
	}

	private DomContent getDependencies() {
		return div(h3("Dependencies"),
				pre(WorkbenchCompiler.getDependenciesAsString()).withClass("w3-code")).withClass("w3-panel w3-card w3-grey");
	}

	private DomContent getLogicTable() {
		return div(h3("Logic Table"),
				pre(WorkbenchCompiler.getLogicTableLog()).withClass("w3-code")).withClass("w3-panel w3-card w3-grey");
	}

	private DomContent getExtractLogic() {
		return div(
					h2("Extract Logic"),
					each(WBCompilerDataStore.getView().getViewSources(), vs -> getViewSouceLogic(vs))
				).withClass("w3-panel w3-card w3-grey");
	}

	private DomContent getViewSouceLogic(ViewSource vs) {
		return div(
					h3("View Source " + vs.getSequenceNo()),
					getExtractFilter(vs),
					getColumnLogic(vs),
					getExtractOutoutLogic(vs)
				).withClass("w3-panel w3-card w3-light-grey");
	}

	private DomContent getExtractOutoutLogic(ViewSource vs) {
		if(vs.getExtractRecordOutput() != null && vs.getExtractRecordOutput().length() > 0) {
			return div(
					h4("Extract Output Logic"),
					div(pre(vs.getExtractRecordOutput())).withClass("w3-code")
					);
		} else {
			return null;
		}
	}

	private DomContent getColumnLogic(ViewSource vs) {
		return table(tbody(getColLogicHeader(), each(vs.getViewColumnSources(), vcs -> getViewColumnSourceLogic(vcs))))
				.withClass("w3-table-all w3-striped w3-border");
	}

	private DomContent getViewColumnSourceLogic(ViewColumnSource vcs) {
		return tr(
				td(vcs.getViewColumn().getColumnNo().toString()).withClass("w3-border"),
				td(vcs.getExtractColumnAssignment()).withClass("w3-border")
				);
	}

	private DomContent getColLogicHeader() {
		return tr(
				th("Column Number").withClass("w3-border"),
				th("Logic Text").withClass("w3-border")
				);
	}

	private DomContent getExtractFilter(ViewSource vs) {
		String ef = vs.getExtractRecordFilter();
		return div(h4("Extract Filter"), div(pre(ef != null && ef.length() > 0 ? ef : "None")).withClass("w3-code"));
	}

	private DomContent getFormatColumnCalculations() {
		return div(
				h3("Format Column Calculations"),
				getFormatColumnCalculationDetails()
				).withClass("w3-panel w3-card w3-grey");
	}

	private DomContent getFormatColumnCalculationDetails() {
		return div(
				each(WBCompilerDataStore.getView().getViewColumns(), c -> getColumnCalc(c))
				);
	}

	private DomContent getColumnCalc(ViewColumn col) {
    	if(col.getFormatColumnCalculation() != null && col.getFormatColumnCalculation().length() > 0) {
			return div(
				h4("Column " + col.getColumnNo()),
				div(pre(col.getFormatColumnCalculation())).withClass("w3-code"),
				h4("Calculation Stack"),
				div(pre(WBCompilerDataStore.getColumnCalcStacks(col.getColumnNo()))).withClass("w3-code")
			).withClass("w3-panel w3-card w3-light-gray");
    	} else {
    		return null;
    	}
	}

	private DomContent getFormatFilterReport() {
		String ff = WBCompilerDataStore.getView().getFormatRecordFilter();
		if(ff != null && ff.length() > 0) {
			return div(
					h3("Format Filter"),
					div(pre(ff)).withClass("w3-code"),
					h4("Calculation Stack"),
					div(pre(WBCompilerDataStore.getFormatFilterCalculationStack())).withClass("w3-code")
					).withClass("w3-panel w3-card w3-gray");
		} else {
			return div(
					h3("Format Filter"),
					pre("None").withClass("w3-code")).withClass("w3-panel w3-card w3-gray");
		}
	}

	private DomContent getStatus() {
		List<CompilerMessage> errs = WorkbenchCompiler.getErrorMessages();
		if (errs.size() > 0) {
			return div(h3("Errors"), table(tbody(getMeassageHeader(), each(WorkbenchCompiler.getErrorMessages(), m -> getMessageRow(m))))
					.withClass("w3-table-all w3-striped w3-border")).withClass("w3-panel w3-card w3-red");
		} else {
			return div(h3("Status"), p("Activation Completed" + (WorkbenchCompiler.hasWarnings() ? " with Warnings" : ""))
					).withClass("w3-panel w3-card w3-green");
		}
	}

	private DomContent getWarningsIfThereAreAny() {
		List<CompilerMessage> ws = WorkbenchCompiler.getWarningMessages();
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

	private DomContent getheader() {
		return 	h1("View: " + WBCompilerDataStore.getView().getName());
	}

}
