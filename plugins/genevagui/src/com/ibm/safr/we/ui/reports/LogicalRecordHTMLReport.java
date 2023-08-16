package com.ibm.safr.we.ui.reports;

import java.nio.file.Path;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LogicalRecordReportQueryBean;
import com.ibm.safr.we.model.query.ViewColumnPICQueryBean;
import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TrTag;

public class LogicalRecordHTMLReport extends GenevaHTMLReport {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.ViewColumnPICReport");

    private Map<Integer, List<LogicalRecordReportQueryBean>> lrs;

	public LogicalRecordHTMLReport() {
	}

	@Override
	protected ContainerTag<DivTag> bodyContent() {
		return  div(h2("Logical Record Report"),   
					lrTables());
	}
	
	private DomContent lrTables() {
		return  each(lrs.values(), lr-> getLrSection(lr));
	}

	private ContainerTag<DivTag> getLrSection(List<LogicalRecordReportQueryBean> lr) {
		LogicalRecordReportQueryBean lrb = lr.get(0);
		return div(
				h3(lrb.getLrname()+"[" + lrb.getLogrecid() + "]"),
				getLRProperties(lrb),
				h3("Fields"),
				table(
					tbody(
						getFieldTableHeader(),
						each(lr, f -> getField(f))
					)
				).withClass("w3-table-all w3-striped w3-border")
			   ).withClass("w3-container  w3-twothird");
	}

	private DomContent getField(LogicalRecordReportQueryBean f) {
		return tr(
				td(f.getFieldname()).withClass("w3-border"),
				td(f.getFixedstartpos()).withClass("w3-border"),
				td(f.getOrdinalpos()).withClass("w3-border"),
				td(getFieldDatatype(f.getFldfmtcd())).withClass("w3-border"),
				td(f.getPrimary() != null ? f.getPrimary() : "").withClass("w3-border"),
				td(f.getSignedind()).withClass("w3-border"),
				td(f.getMaxlen()).withClass("w3-border"),
				td(f.getDecimalcnt()).withClass("w3-border"),
				td(f.getRounding()).withClass("w3-border"),
				td(getDatetime(f.getFldcontentcd())).withClass("w3-border"),
				td(f.getEffdatestartfldid()).withClass("w3-border"),
				td(f.getEffdateendfldid()).withClass("w3-border")
				).withClass("w3-border").withCondClass(f.getPrimary() != null, "w3-text-red");
	}

	private DomContent getLRProperties(LogicalRecordReportQueryBean lrb) {
		return table(
				tbody(
					getLrPropTableHeader(),
					getLrPropsData(lrb)
				)
			).withClass("w3-table-all w3-striped w3-border");
	}

	private DomContent getLrPropsData(LogicalRecordReportQueryBean lrb) {
		return tr(
				td(lrb.getLrtypecd()).withClass("w3-border"),
				td(lrb.getLookupexitid().equals("0") ? "" : lrb.getLookupexitid()).withClass("w3-border"),
				td(lrb.getExitName() != null ? lrb.getExitName() : "").withClass("w3-border"),
				td(lrb.getLookupexitstartup()).withClass("w3-border")
				).withClass("w3-border");
	}

	private DomContent getLrPropTableHeader() {
		return tr(
				th("LR Type").withClass("w3-border"),
				th("Lookup Exit ID").withClass("w3-border"),
				th("Lookup Exit NAme").withClass("w3-border"),
				th("Lookup Exit Startup").withClass("w3-border")
			);
	}

	private ContainerTag<TrTag> getFieldTableHeader() {
		return tr(
				th("Name").withClass("w3-border"),
				th("Start Position").withClass("w3-border"),
				th("Ordinal").withClass("w3-border"),
				th("Datatype").withClass("w3-border"),
				th("Primary").withClass("w3-border"),				
				th("Signed").withClass("w3-border"),
				th("Length").withClass("w3-border"),
				th("Decimals").withClass("w3-border"),
				th("Rounding").withClass("w3-border"),
				th("Date Code").withClass("w3-border"),
				th("Effective date start").withClass("w3-border"),
				th("Effective date end").withClass("w3-border")
				).withClass("w3-border");
	}
	
	private String getDatetime(String input) {
		if(input == null) {
			return "";
		} else {
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.FLDCONTENT).getCode(input.trim());
			return code.getDescription();
		}
	}

	private String getFieldDatatype(String input) {
		Code code = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.DATATYPE).getCode(input.trim());
		return code.getDescription();
	}


	public void addLogicalRecords(Map<Integer, List<LogicalRecordReportQueryBean>> lrsById) {
		lrs = lrsById;		
	}

	public void setFileName(Path path, String baseName, List<Integer> viewIDs) {
		Path htmlPath = makeHtmlDirIfNeeded(path);
		String outputFile = baseName + "_Env" + SAFRApplication.getUserSession().getEnvironment().getId();
		for(Integer id : viewIDs) {
			outputFile += "_[" + id.toString() + "]";
		}
		outputFile += ".html";
		reportPath = htmlPath.resolve(outputFile);
	}

}
