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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LookupPrimaryKeysBean;
import com.ibm.safr.we.model.query.LookupReportQueryBean;
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
public class LookupPathHTMLReport extends GenevaHTMLReport {

	private Integer lookupPathId;
	private String lookupPathName;
	private Boolean valid;
	private Map<Integer, ReportLookup> lookupsByID = new TreeMap<>();
	private Map<Integer, String> stepColour = new HashMap();
	
	private Map<String, Integer>lrname2StepNumber = new HashMap();;

	private class ReportLookup {
		int id;
		String name;
		Map<Integer,  ReportLookupStep> lookupSteps= new TreeMap<>();
	}
	
	private class ReportLookupStep {
		List<LookupReportQueryBean> sourceKeys = new ArrayList<>();
		List<LookupPrimaryKeysBean> primaryKeys  = new ArrayList<>();
		public int number;
		
	}
	
	public LookupPathHTMLReport() throws SAFRException {
		stepColour.put(0, "w3-flat-alizarin");
		stepColour.put(1, "w3-flat-pumpkin");
		stepColour.put(2, "w3-flat-carrot");
		stepColour.put(3, "w3-flat-orange");
		stepColour.put(4, "w3-flat-sun-flower");
		stepColour.put(5, "w3-flat-emerald");
		stepColour.put(6, "w3-flat-green-sea");
		stepColour.put(7, "w3-flat-turquoise");
		stepColour.put(8, "w3-flat-peter-river");
		stepColour.put(9, "w3-flat-belize-hole");
		stepColour.put(10, "w3-flat-amethyst");
		stepColour.put(11, "w3-flat-wisteria");
		stepColour.put(12, "w3-flat-wet-asphalt");
		stepColour.put(13, "w3-flat-midnight-blue");
		stepColour.put(14, "w3-flat-nephritis");
	}
	
	public void addLookups(Map<Integer, List<LookupReportQueryBean>> lookupBeansById) {
		ReportLookup reportLookup = null;
		ReportLookupStep reportStep = null;
		for(Integer lkid : lookupBeansById.keySet()) {
			int lookupId = 0;
			int stepNumber = 0;
			for(LookupReportQueryBean lk : lookupBeansById.get(lkid)) {
				if(lookupId !=lk.getLookupid()) {
					lookupId = lk.getLookupid();
					reportLookup = new ReportLookup();
					reportLookup.name = lk.getName();
					reportLookup.id = lookupId;
					lookupsByID.put(lookupId, reportLookup);
				}
				if(stepNumber != lk.getStepseqnbr()) {
					stepNumber = lk.getStepseqnbr();
					reportStep = new ReportLookupStep();
					reportStep.number = stepNumber;
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

	public String getHtml() {
		String html = "";
		return html;
	}

	public String getHtmlHeaderRow() {
		String hdr = "<tr>";
		return hdr;
	}

	public String getHtmlDataRow() {
		return "";
	}
	
	public String getStepColour(int step) {
		return stepColour.get(step);
	}
	
	@Override
	protected ContainerTag<DivTag> bodyContent() {
		return div(
				h1("Lookup Path Report"),
				each(lookupsByID.values(), lk -> getLookupReport(lk))
			);
	}

	private DomContent getLookupReport(ReportLookup lk) {
		
		return div(
					h3(lk.name + "[" + lk.id + "]"),
					getTargetTable(lk),
					h4("Steps"),
					each(lk.lookupSteps.values(), s -> getStep(s))
				).withClass("w3-container");
	}

	private DomContent getStep(ReportLookupStep s) {
		return table(
						tbody(
							getStepHeader(),
							getStepData(s),
							tr(td(getSourceKeys(s)).attr("colspan", "4"))
						)	
					).withClass("w3-table-all w3-striped w3-border");
	}
	
	private DomContent getSourceKeys(ReportLookupStep s) {
		return table(
				tbody(
						tr(
								td(getSourceFieldTable(s.sourceKeys)),
								td(getPrimaryKeysTable(s.primaryKeys))
							)
				)	
			).withClass("w3-table-all w3-striped w3-border");
	}

	private DomContent getPrimaryKeysTable(List<LookupPrimaryKeysBean> primaryKeys) {
		return table(
				tbody(
						getPrimaryKeysHeader(),
						each(primaryKeys, p -> getPrimaryKeysData(p))
				)	
			).withClass("w3-table-all w3-striped w3-border");
	}

	private DomContent getPrimaryKeysData(LookupPrimaryKeysBean p) {
		return tr(	
				td(p.getFldname() + "[" +  p.getLrfieldid() + "]").withClass("w3-border"),
				td(getDataTypeCode(p.getFldfmtcd())).withClass("w3-border"),
				td(p.getMaxlen()).withClass("w3-border"),
				td(getDateTimeFormatCode(p.getFldcontentcd())).withClass("w3-border"),
				td(p.getDecimalcnt()).withClass("w3-border")
				);
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

	private DomContent getPrimaryKeysHeader() {
		return tr(	
				th("Field").withClass("w3-border"),
				th("Datatype").withClass("w3-border"),
				th("Length").withClass("w3-border"),
				th("Date Code").withClass("w3-border"),
				th("Decimals").withClass("w3-border")
				);
	}

	private DomContent getSourceFieldTable(List<LookupReportQueryBean> sourceKeys) {
		return table(
				tbody(
						getSourceKeyHeader(),
						each(sourceKeys, sf -> getSourceField(sf))
					)	
				).withClass("w3-table-all w3-striped w3-border");
	}

	private DomContent getSourceField(LookupReportQueryBean sf) {
		return tr(	
				td(getSourceFieldType(sf)).withClass("w3-border"),
				td(getValueEntry(sf)).withClass("w3-border " + getColour(sf)),
				td(getDataTypeCode(sf.getValuefmtcd())).withClass("w3-border"),
				td(sf.getValuelen()).withClass("w3-border"),
				td(getDateTimeFormatCode(sf.getFldcontentcd())).withClass("w3-border"),
				td(sf.getDecimalcnt()).withClass("w3-border")
				);
	}

	private String getColour(LookupReportQueryBean sf) {
		if(sf.getKeylr() != null) {
			return stepColour.get(lrname2StepNumber.get(sf.getKeylr()));
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

	
	private DomContent getSourceKeyHeader() {
		return tr(	
					th("Source").withClass("w3-border"),
					th("Field").withClass("w3-border"),
					th("Datatype").withClass("w3-border"),
					th("Length").withClass("w3-border"),
					th("Date Code").withClass("w3-border"),
					th("Decimals").withClass("w3-border")
				);
	}

	private DomContent getStepData(ReportLookupStep s) {
		LookupReportQueryBean stepBean = s.sourceKeys.get(0); 
		lrname2StepNumber.put(stepBean.getSteptarglr(), s.number);
		return tr(	
				td(Integer.toString(stepBean.getStepseqnbr())).withClass("w3-border"),
				td(stepBean.getSteptarglr()+ "[" + stepBean.getTarglrid() + "]").withClass("w3-border"),
				td(stepBean.getSteptarglf()+ "[" + stepBean.getTarglfld() + "]").withClass("w3-border"),
				td(stepBean.getExitname() != null ? stepBean.getExitname() + "[" + stepBean.getLookupexitid() + "]" : "").withClass("w3-border"),
				td(stepBean.getLookupexitstartup()).withClass("w3-border")
				).withClass(getStepColour(s.number));
	}

	private DomContent getStepHeader() {
		return tr(	
				th("Number").withClass("w3-border"),
				th("Target Logical Record").withClass("w3-border"),
				th("Target Logical File").withClass("w3-border"),
				th("Exit").withClass("w3-border"),
				th("Parameters").withClass("w3-border")
				);
	}

	private TableTag getTargetTable(ReportLookup lk) {
		return table(
			tbody(
				getTargetHeader(),
				getTargetData(lk)
			)
		).withClass("w3-table-all w3-striped w3-border");
	}
	private DomContent getTargetData(ReportLookup lk) {
		int numSteps = lk.lookupSteps.size();
		ReportLookupStep targStep = lk.lookupSteps.get(numSteps);
		LookupReportQueryBean targBean = targStep.sourceKeys.get(0); 
		return tr( 
					td(targBean.getSteptarglr() + "[" + targBean.getTarglrid() + "]").withClass("w3-border"),
					td(targBean.getSrclrname() + "[" + targBean.getSrclrid() + "]").withClass("w3-border"),
					td(targBean.getExitname() != null ? targBean.getExitname() + "[" + targBean.getLookupexitid() + "]" : "").withClass("w3-border"),
					td(targBean.getLookupexitstartup()).withClass("w3-border")
				).withClass(getStepColour(numSteps));
	}

	private DomContent getTargetHeader() {
		return tr(
				th("Target Logical Record").withClass("w3-border"),
				th("Source Logical Record").withClass("w3-border"),
				th("Exit").withClass("w3-border"),
				th("Parameters").withClass("w3-border")
			);
	}

	public void setFileName(Path path, String baseName, List<Integer> ids) {
		Path htmlPath = makeHtmlDirIfNeeded(path);
		String outputFile = baseName + "_Env" + SAFRApplication.getUserSession().getEnvironment().getId();
		for(Integer id : ids) {
			outputFile += "_[" + id.toString() + "]";
		}
		outputFile += ".html";
		reportPath = htmlPath.resolve(outputFile);
	}
	
	public void addPrimaryKeys(Map<Integer, List<LookupPrimaryKeysBean>> primarysById) {
		ReportLookup reportLookup = null;
		ReportLookupStep reportStep = null;
		Map<String, List<LookupReportQueryBean>> steps = null;
		for(Integer pkid : primarysById.keySet()) {
			int lookupId = 0;
			int stepNumber = 0;
			for( LookupPrimaryKeysBean pk : primarysById.get(pkid)) {
				if(lookupId != pk.getLookupid()) {
					lookupId = pk.getLookupid();
					reportLookup = lookupsByID.get(pk.getLookupid());
				}
				if(stepNumber != pk.getStepseqnbr()) {
					stepNumber = pk.getStepseqnbr();
					reportStep = reportLookup.lookupSteps.get(stepNumber);
				}
				if(reportStep != null) {
					reportStep.primaryKeys.add(pk);
				}	
			}
		}
	}


}
