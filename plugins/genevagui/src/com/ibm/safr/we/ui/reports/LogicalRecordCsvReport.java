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

public class LogicalRecordCsvReport extends GenevaCSVReport {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.ViewColumnPICReport");

    private Map<Integer, List<LogicalRecordReportQueryBean>> lrs = new TreeMap<>();

	public LogicalRecordCsvReport() {
	}


	public void setFileName(Path path, String baseName, List<Integer> viewIDs) {
		Path csvPath = makeCsvDirIfNeeded(path);
		String outputFile = baseName + "_Env" + SAFRApplication.getUserSession().getEnvironment().getId();
		for(Integer id : viewIDs) {
			outputFile += "_[" + id.toString() + "]";
		}
		outputFile += ".csv";
		reportPath = csvPath.resolve(outputFile);
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



	@Override
	protected List<String>  getHeaders() {
		headers.add("LR ID");
		headers.add("LR Name");
		headers.add("Lookup Exit ID");
		headers.add("Lookup Exit Name");
		headers.add("Lookup Exit Param");
		headers.add("Field");
		headers.add("Start Position");
		headers.add("Ordinal");
		headers.add("Datatype");
		headers.add("Primary");
		headers.add("Signed");
		headers.add("Length");
		headers.add("Decimals");
		headers.add("Rounding");
		headers.add("Date Code");
		headers.add("Effective date start");
		headers.add("Effective date end");
		return headers;
	}
	
	@Override
	protected List<List<String>> getRows() {
		for(List<LogicalRecordReportQueryBean> lr : lrs.values()) {
			for(LogicalRecordReportQueryBean lrb : lr) {
				List<String> row = new ArrayList<>();
				row.add(lrb.getLogrecid());
				row.add(lrb.getLrname());
				row.add(lrb.getLookupexitid().equals("0") ? "" : lrb.getLookupexitid());
				row.add(lrb.getExitName() != null ? lrb.getExitName() : "");
				row.add(lrb.getLookupexitstartup());
				row.add(lrb.getFieldname());
				row.add(lrb.getFixedstartpos());
				row.add(lrb.getOrdinalpos());
				row.add(getFieldDatatype(lrb.getFldfmtcd()));
				row.add(lrb.getPrimary() != null ? lrb.getPrimary() : "");
				row.add(lrb.getSignedind());
				row.add(lrb.getMaxlen());
				row.add(lrb.getDecimalcnt());
				row.add(lrb.getRounding());
				row.add(getDatetime(lrb.getFldcontentcd()));
				row.add(lrb.getEffdatestartfldid());
				row.add(lrb.getEffdateendfldid());
				allrows.add(row);
			}
		}
		return allrows;
	}


	public void addLogicalRecords(Map<Integer, List<LogicalRecordReportQueryBean>> lrsById) {
		lrs = lrsById;
	}

}
