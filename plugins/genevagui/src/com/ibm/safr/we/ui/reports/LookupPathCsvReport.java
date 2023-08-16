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


import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.LRFieldKeyType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.LookupPathSourceField;
import com.ibm.safr.we.model.LookupPathStep;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.query.LookupPrimaryKeysBean;
import com.ibm.safr.we.model.query.LookupReportQueryBean;
import com.ibm.safr.we.model.query.UserGroupsReportBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;

import static j2html.TagCreator.*;


/**
 * A class used to display data of the LookupPath in the LookupPath report.
 * 
 */
public class LookupPathCsvReport extends GenevaCSVReport {

	private Integer lookupPathId;
	private String lookupPathName;
	private Boolean valid;
	private Map<Integer, ReportLookup> lookupsByID = new TreeMap<>();

	private class ReportLookup {
		String name;
		Map<Integer,  ReportLookupStep> lookupSteps= new TreeMap<>();
	}
	
	private class ReportLookupStep {
		List<LookupReportQueryBean> sourceKeys = new ArrayList<>();
	}
	
	public LookupPathCsvReport() throws SAFRException {
	}
	
	public void addLookups(Map<Integer, List<LookupReportQueryBean>> lookupsById) {
		int lookupId = 0;
		int stepNumber = 0;
		ReportLookup reportLookup = null;
		ReportLookupStep reportStep = null;
		for(List<LookupReportQueryBean> lookupBeans : lookupsById.values()) {
			for(LookupReportQueryBean lk : lookupBeans) {
				if(lookupId !=lk.getLookupid()) {
					lookupId = lk.getLookupid();
					reportLookup = new ReportLookup();
					reportLookup.name = lk.getName();
					lookupsByID.put(lookupId, reportLookup);
				}
				if(stepNumber != lk.getStepseqnbr()) {
					stepNumber = lk.getStepseqnbr();
					reportStep = new ReportLookupStep();
					reportLookup.lookupSteps.put(stepNumber, reportStep);
				}
				reportStep.sourceKeys.add(lk);
			}
		}
	}

	public Integer getLookupPathId() {
		return lookupPathId;
	}

	public String getLookupPathName() {
		if (this.lookupPathName == null) {
			this.lookupPathName = "";
		}
		return UIUtilities.getComboString(lookupPathName, lookupPathId);
	}

	public String getValid() {
		if (valid) {
			return "Active";
		} else {
			return "Inactive";
		}
	}

	private String getDataTypeCode(String input) {
		Code code = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.DATATYPE).getCode(input.trim());
		return code.getDescription();
	}

	private String getDateTimeFormatCode(String input) {
		if(input != null) {
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.FLDCONTENT).getCode(input.trim());
			return code.getDescription();
		} else {
			return "";
		}
	}

	private String getSourceFieldType(LookupReportQueryBean sf) {
		switch(sf.getFldtype()) {
		case "0":
			return "LRFIELD";
		case "1":
		return "CONSTANT";
		case "3":
		return "SYMBOL";
		default:
			return "";
		}
	}

	private String getValueEntry(LookupReportQueryBean sf) {
		switch(sf.getFldtype()) {
		case "0":{
			if(sf.getKeylr() != null) {
				return sf.getKeylr() + "[" +sf.getKeylrid() + "]." + sf.getFldname() + "[" + sf.getLrfieldid() + "]";
			} else {
				return sf.getFldname() + "[" + sf.getLrfieldid() + "]";				
			}
		}
		case "1":
			return sf.getValue();
		case "3":
			return sf.getSymbolicname();
		default:
			return "";
		}
	}
	
	public void setFileName(Path path, String baseName, List<Integer> ids) {
		Path htmlPath = makeCsvDirIfNeeded(path);
		String outputFile = baseName + "_Env" + SAFRApplication.getUserSession().getEnvironment().getId();
		for(Integer id : ids) {
			outputFile += "_[" + id.toString() + "]";
		}
		outputFile += ".csv";
		reportPath = htmlPath.resolve(outputFile);
	}
	
	@Override
	protected List<String> getHeaders() {
		headers.add("Lookup ");
		headers.add("ID");
		headers.add("Step Number");
		headers.add("Step Source Logical Record");
		headers.add("Target Logical Record");
		headers.add("Exit");
		headers.add("Parameters");
		headers.add("Source Key Number");
		headers.add("Source");
		headers.add("Field");
		headers.add("Datatype");
		headers.add("Length");
		headers.add("Date Code");
		headers.add("Decimals");
		return headers;
	}

	@Override
	protected List<List<String>> getRows() {
		for(ReportLookup lk : lookupsByID.values()) {
			for( ReportLookupStep step : lk.lookupSteps.values()) {
				for(LookupReportQueryBean src : step.sourceKeys) {
					List<String> row = new ArrayList<>();
					row.add(src.getName());
					row.add(Integer.toString(src.getLookupid()));
					row.add(Integer.toString(src.getStepseqnbr()));
					row.add(src.getStepsrclr());
					row.add(src.getSteptarglr());
					row.add(src.getExitname());
					row.add(src.getLookupexitstartup());
					row.add(Integer.toString(src.getKeyseqnbr()));
					row.add(getSourceFieldType(src));
					row.add(getValueEntry(src));
					row.add(getDataTypeCode(src.getValuefmtcd()));
					row.add(src.getValuelen());
					row.add(getDateTimeFormatCode(src.getFldcontentcd()));
					row.add(src.getDecimalcnt());
					allrows.add(row);
				}
			}
		}
		return allrows;
	}

}
