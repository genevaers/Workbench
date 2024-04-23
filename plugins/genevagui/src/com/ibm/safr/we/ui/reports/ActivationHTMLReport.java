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
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSource;

import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;

/************************************************************************************************************************/
/************************************************************************************************************************/

public class ActivationHTMLReport extends  GenevaHTMLReport  {

	public ActivationHTMLReport() {
	}

	public void setFileName(Path path, String baseName, List<Integer> viewIDs) {
		Path htmlPath = makeHtmlDirIfNeeded(path);
		String outputFile = baseName + "_Env" + SAFRApplication.getUserSession().getEnvironment().getId()+"_V"+ CompilerFactory.getView().getId();
		outputFile += ".html";
		reportPath = htmlPath.resolve(outputFile);
	}

	@Override
	protected ContainerTag<DivTag> bodyContent() {
		return div(
				h1("Activation Report"),
				getheader(),
				getFormatFilterReport(),
				getFormatColumnCalculations(),
				getExtractLogic(),
				getLogicTable(),
				getWarningsIfThereAreAny(),
				getDependencies()
			).withClass("w3-container");
	}

	private DomContent getDependencies() {
		return div(h3("Dependencies"),
				pre(WorkbenchCompiler.getDependenciesAsString()).withClass("w3-code")).withClass("w3-panel w3-card w3-grey");
	}

	private DomContent getLogicTable() {
		return div(h3("Logic Table"),
				pre(CompilerFactory.getLogicTableLog()).withClass("w3-code")).withClass("w3-panel w3-card w3-grey");
	}

	private DomContent getExtractLogic() {
		return div(
					h2("Extract Logic"),
					each(CompilerFactory.getView().getViewSources(), vs -> getViewSouceLogic(vs))
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
		boolean logicFound = false;
		return div(
				each(CompilerFactory.getView().getViewColumns(), c -> getColumnCalc(c))
				);
	}

	private DomContent getColumnCalc(ViewColumn col) {
    	if(col.getFormatColumnCalculation() != null && col.getFormatColumnCalculation().length() > 0) {
			return div(
				h4("Column " + col.getColumnNo()),
				div(pre(col.getFormatColumnCalculation())).withClass("w3-code"),
				h4("Calculation Stack"),
				div(pre(CompilerFactory.getColumnCalcStacks(col.getColumnNo()))).withClass("w3-code")
			).withClass("w3-panel w3-card w3-light-gray");
    	} else {
    		return null;
    	}
	}

	private DomContent getFormatFilterReport() {
		String ff = CompilerFactory.getView().getFormatRecordFilter();
		if(ff != null && ff.length() > 0) {
			return div(
					h3("Format Filter"),
					div(pre(ff)).withClass("w3-code"),
					h4("Calculation Stack"),
					div(pre(CompilerFactory.getFormatFilterCalculationStack())).withClass("w3-code")
					).withClass("w3-panel w3-card w3-gray");
		} else {
			return div(
					h3("Format Filter"),
					pre("None").withClass("w3-code")).withClass("w3-panel w3-card w3-gray");
		}
	}

	private DomContent getWarningsIfThereAreAny() {
		List<String> ws = CompilerFactory.getWarnings();
		return div(
				h3("Warnings"),
				table(tbody(getWarningsHeader(), each(ws, w -> tr(td(w)))))
				.withClass("w3-table-all w3-striped w3-border")
				).withClass("w3-panel w3-card w3-gray");
	}


	private DomContent getWarningsHeader() {
		return tr(
				th("Message").withClass("w3-border")
				);
	}

	private DomContent getheader() {
		return 	h2("View: " + CompilerFactory.getView().getName());
	}

}
