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

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ViewMappingsReportQueryBean;
import com.ibm.safr.we.model.query.ViewPropertiesReportQueryBean;
import com.ibm.safr.we.model.query.ViewSortKeyReportQueryBean;
import com.ibm.safr.we.model.query.ViewSourcesReportQueryBean;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;

/************************************************************************************************************************/
/************************************************************************************************************************/

public class ViewHTMLReport extends  GenevaHTMLReport  {

	private Map<Integer, ViewReportData> viewReportData;
	
	public ViewHTMLReport() {
	}

	private String getPhase(ViewPropertiesReportQueryBean vwb) {
		if(vwb.getViewtypecd().equals("DETL") || vwb.getViewtypecd().equals("SUMRY")) {
			return "Format";
		} else {
			return "Extract";
		}
	}

	public String  getHtml() {
		String html = "";
//		html += getViewProperties();
//		html += getExtractPhaseProperties();
//		if(view.getViewtypecd().equals("DETL") || view.getViewtypecd().equals("SUMRY")) {
//			html += getFormatPhaseProperties();
//		}
		html += getViewSources();
		return html;		
	}

	private String getViewSources() {
		String html = "";
		html += "<h3> Sources</h3>";
//		for(ViewSourcesWrapper vs : viewSources) {
//			html += vs.getSourceTable();
//		}
		return html;		
	}

	private ContainerTag<DivTag> getViewReport(ViewReportData vw) {
		return div(
				h2(vw.getView().getName() + "[" + vw.getView().getViewid() + "]"),
				h3("View Properties"),
				getViewProperties(vw),
				getExtractPhaseProperties(vw),
				div().condWith(isFormatView(vw),getFormatPhasePropertiesIfNeeded(vw)),
				getSourceIODetails(vw)
				
				).withClass("w3-container");
	}

private boolean isFormatView(ViewReportData vw){
	return vw.getView().getViewtypecd().equals("DETL") || vw.getView().getViewtypecd().equals("SUMRY");
}

	private DomContent getViewProperties(ViewReportData vw) {
		return table(
				tbody(
					getViewPropTableHeader(),
					getViewPropsData(vw.getView())
				)
			).withClass("w3-table-all w3-striped w3-border");
	}

	private DomContent getViewPropsData(ViewPropertiesReportQueryBean vwb) {
		return tr(
				td(getPhase(vwb)).withClass("w3-border"),
				td(getOutputformat(vwb)).withClass("w3-border"),
				td(vwb.getLfname() == null  ? "" : vwb.getLfname()).withClass("w3-border"),
				td(vwb.getPfname() == null  ? "" : vwb.getPfname()).withClass("w3-border"),
				td(vwb.getControlName() + "[" + vwb.getControlrecid() + "]").withClass("w3-border")
				).withClass("w3-border");
	}

	private DomContent getViewPropTableHeader() {
		return tr(
				th("Phase").withClass("w3-border"),
				th("Output Format").withClass("w3-border"),
				th("Output Logical File").withClass("w3-border"),
				th("Output Physical File").withClass("w3-border"),
				th("Control Record").withClass("w3-border")
				);
	}

	private String getOutputformat(ViewPropertiesReportQueryBean view) {
		if(view.getOutputmediacd().equals("HCOPY")) {
			return "Report";
		} else if (view.getOutputmediacd().equals("DELIM")) {
			return "Delimited";
		} else {
			if(view.getViewtypecd().equals("COPY")) {
				return "Source-Record";				
			} else {
				return "Fixed-width";
			}
		}
	}

	public void setSorkKeys(Map<Integer, SortKeysWrapper> map) {
		// TODO Auto-generated method stub
		
	}

	public String getViewPropertiesHeaderCsv() {
		String hdr = "ViewID,"
				+ "Phase,"
				+ "Output Format,"
				+ "Output Logical File,"
				+ "Output Physical File,"
				+ "Extract Output Limit,"
				+ "Format Output Limit,"
				+ "Lines Per Page,"
				+ "Report Width,"
				+ "Control Record,"
				+ "Format Exit,"
				+ "Format Exit Parameter,"
				+ "Extract Buffer Size,"
				+ "Zero Suppression\n";
	return hdr;
	}

	@Override
	protected ContainerTag<DivTag> bodyContent() {
		return div(
				h1("View Properties Report"),
				each(viewReportData.values(), vw -> getViewReport(vw))
			);
	}
	
	private ContainerTag<DivTag> getSourceIODetails(ViewReportData vw) {
		return div(
				h3("Source Input Output Details"),
				each(vw.getViewSources(), src -> getSourceDetails(src))
				);
	}

	private DomContent getSourceDetails(ViewSourceReportData src) {
		return div (
						getSourceHeader(src),
						//getSourceLink(src),
						getSourceDiv(src)
				).withClass("w3-container w3-border");
	}

	private DomContent getSourceDiv(ViewSourceReportData src) {
		return div(
				getInputDiv(src),
				getColumnMapplingsDiv(src),
				getOutputDiv(src)
				).withId(getSrcID(src)); //.withStyle("display: none");
	}

	private DomContent getOutputDiv(ViewSourceReportData src) {
		return div(
					h4("Output"),
					table(tbody(
						getOutputHeader(),
						getOutputDetails(src),
						getOutputLogicHeader(),
						getOutputLogic(src)
				)).withClass("w3-table-all w3-striped w3-border"));
	}

	private DomContent getOutputLogicHeader() {
		return tr(
				th("Output Logic").withClass("w3-border").attr("colspan", "4")
			);
	}

	private DomContent getOutputLogic(ViewSourceReportData src) {
		return tr(
				td(pre(src.getViewSource().getExtractoutputlogic())).withClass("w3-border").attr("colspan", "4")
			);
	}

	private DomContent getOutputDetails(ViewSourceReportData src) {
		return tr(
				td(src.getViewSource().getOutLf()).withClass("w3-border"),
				td(src.getViewSource().getOutPF()).withClass("w3-border"),
				td(src.getViewSource().getWriteExitName()).withClass("w3-border"),
				td(src.getViewSource().getWriteexitparm()).withClass("w3-border")
			);
	}

	private DomContent getColumnMapplingsDiv(ViewSourceReportData src) {
		return div(
					h4("Column Mappings"),
					getColumnMappings(src.getColumnMappings())
				).withStyle("overflow-x: scroll").withStyle("overflow-y: scroll");
	}

	private DomContent getInputDiv(ViewSourceReportData src) {
		return div(
				h4("Input"),
				table(tbody(
					getInputHeader(),
					getInputRow(src.getViewSource()),
					getRecordFilterHeader(),
					getRecordFilter(src.getViewSource())
				)).withClass("w3-table-all w3-striped w3-border"));
	}

	private DomContent getOutputHeader() {
		return tr(
				th("Output Logical File").withClass("w3-border"),
				th("Output Physical File").withClass("w3-border"),
				th("Write Exit").withClass("w3-border"),
				th("Write Exit Parm").withClass("w3-border")
			);
	}

	private DomContent getColumnMappingsHeader() {
		return tr(
				th("Column Mappings").withClass("w3-border").attr("colspan", "2")
			);
	}
	private DomContent getColumnMappings(List<ViewMappingsReportQueryBean> colMappings) {
		return tr(
				td(
					table(
							tbody(
									getColMappingsHeader(),
									each(colMappings, cm ->getColMappingWithSK(cm))
									)
							).withClass("w3-table-all w3-striped w3-border")
					)
				);
	}

	private DomContent getColMappingWithSK(ViewMappingsReportQueryBean cm) {
		if(cm.getSortkey() != null) {
			return join(getColMappingDetails(cm),getSortKeyTable(cm));
		} else {
			return getColMappingDetails(cm);
		}
	}
	
	public DomContent getSortKeyTable(ViewMappingsReportQueryBean cm) {
		return 	tr(
					td(
				table(
						tbody(
								getSortKeyHeader(),
								getSortKeyData(cm)
							)
						)
				).withId(getSKID(cm)).attr("colspan", "9").withStyle("display: none"));
	}

	public DomContent getSortKeyHeader() {
		return 	tr(
					th("Sequence No").withClass("w3-border"),
					th("Start Position").withClass("w3-border"),
					th("Order").withClass("w3-border"),
					th("Sort Break").withClass("w3-border"),
					th("Label").withClass("w3-border"),
					th("Data Type").withClass("w3-border"),
					th("Format").withClass("w3-border"),
					th("Length").withClass("w3-border"),
					th("Decimals").withClass("w3-border"),
					th("Signed").withClass("w3-border")
				);
	}


	public DomContent getSortKeyData(ViewMappingsReportQueryBean cm) {
		ViewSortKeyReportQueryBean sk = cm.getSortkey();
		return 	tr(
					td(sk.getKeyseqnbr()).withClass("w3-border"),
					td(sk.getSkstartpos()).withClass("w3-border"),
					td(sk.getSortseqcd()).withClass("w3-border"),
					td(sk.getSortbrkind()).withClass("w3-border"),
					td(sk.getSortkeylabel()).withClass("w3-border"),
					td(getFieldDatatype(sk.getSkfldfmtcd())).withClass("w3-border"),
					td(getDatetime(sk.getSkfldcontentcd())).withClass("w3-border"),
					td(sk.getSkfldlen()).withClass("w3-border"),
					td(sk.getSkdecimalcnt()).withClass("w3-border"),
					td(sk.getSksigned()).withClass("w3-border")
				);
	}


	private DomContent getColMappingDetails(ViewMappingsReportQueryBean cm) {
		return 	tr(
					td(getColumnNumber(cm)).withClass("w3-border"),
					td(getColumnType(cm)).withClass("w3-border"),
					td(getColumnValue(cm)).withClass("w3-border"),
					td(pre("->")).withClass("w3-border"),
					td(cm.getStartposition()).withClass("w3-border"),
					td(getFieldDatatype(cm.getFldfmtcd())).withClass("w3-border"),
					td(getDatetime(cm.getFldcontentcd())).withClass("w3-border"),
					td(cm.getMaxlen()).withClass("w3-border"),
					td(getAlignment(cm)).withClass("w3-border"),
					td(cm.getDecimalcnt()).withClass("w3-border"),
					td(cm.getRounding()).withClass("w3-border"),
					td(cm.getSignedind().equals("0") ? "false" : "true").withClass("w3-border"),
					td(cm.getVisible().equals("0") ? "false" : "true").withClass("w3-border"),
					td(cm.getSpacesbeforecolumn()).withClass("w3-border"),
					td(getFormatCalcLogic(cm)).withClass("w3-border")
				).withCondClass(cm.getSortkey() != null, "w3-yellow");
	}

	private DomContent getColumnNumber(ViewMappingsReportQueryBean cm) {
		if(cm.getSortkey() != null) {
			return p(a(cm.getColumnnumber() +" (Sortkey " + cm.getSortkey().getKeyseqnbr() + ")").withHref(getSortkeyHref(cm)));
		} else {
			return p(cm.getColumnnumber());
		}
	}

	private String getSortkeyHref(ViewMappingsReportQueryBean cm) {
		return "javascript:toggleDiv(\"" + getSKID(cm) + "\")";
	}
	
	private String getSKID(ViewMappingsReportQueryBean cm) {
		return "sk"+ cm.getSrcseqnbr() + "_" +cm.getColumnnumber();
	}

	private DomContent getFormatCalcLogic(ViewMappingsReportQueryBean cm) {
		if(cm.getFormatcalclogic() == null) {
			return p();
		} else { 
			return pre(cm.getFormatcalclogic());
		}
	}

	private String getAlignment(ViewMappingsReportQueryBean cm) {
		if(cm.getJustifycd() == null) {
			return "";
		} else {
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.JUSTIFY).getCode(cm.getJustifycd().trim());
			return code.getDescription();
		}
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

	private String getColumnType(ViewMappingsReportQueryBean cm) {
		Code code = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.COLSRCTYPE).getCode(cm.getSourcetypeid());
		return code.getDescription();
	}

	private DomContent getColumnValue(ViewMappingsReportQueryBean cm) {
		int sourceType = cm.getSourcetypeid();
		if (sourceType == Codes.CONSTANT) {
			return p(cm.getConstval());
		} else if (sourceType == Codes.SOURCE_FILE_FIELD) {
			return p(cm.getFieldName() + "[" + cm.getLrfieldid() + "]");
		} else if (sourceType == Codes.LOOKUP_FIELD) {
			return p(cm.getLkName() + "." +  cm.getFieldName() + "[" + cm.getLrfieldid() + "]");
		} else if (sourceType == Codes.FORMULA) {
			return pre(cm.getExtractcalclogic());
		}
		return p();
	}
	
	private DomContent getColMappingsHeader() {
		return tr(
				th("Column Number").withClass("w3-border"),
				th("Type").withClass("w3-border"),
				th("Value").withClass("w3-border"),
				th(" ").withClass("w3-border"),
				th("Start Position").withClass("w3-border"),
				th("Type").withClass("w3-border"),
				th("Format").withClass("w3-border"),
				th("Length").withClass("w3-border"),
				th("Alignment").withClass("w3-border"),
				th("DecimalPlaces").withClass("w3-border"),
				th("Scaling").withClass("w3-border"),
				th("Signed").withClass("w3-border"),
				th("Visible").withClass("w3-border"),
				th("Spaces").withClass("w3-border"),
				th("FormatPhaseCalculation").withClass("w3-border")
				);
	}

	private DomContent getRecordFilterHeader() {
		return tr(
					th("Record Filter").withClass("w3-border").attr("colspan", "2")
				);
	}
	private DomContent getRecordFilter(ViewSourcesReportQueryBean vsb) {
		return 	tr(
					td(pre(vsb.getExtractfiltlogic())).withClass("w3-border").attr("colspan", "2")
				);
	}

	private DomContent getInputRow(ViewSourcesReportQueryBean vsb) {
		return tr(
				td(vsb.getLrname()).withClass("w3-border"),
				td(vsb.getLfname()).withClass("w3-border")
				);
	}

	private DomContent getInputHeader() {
		return tr(
				th("Logical Record").withClass("w3-border"),
				th("Logical File").withClass("w3-border")
			);
	}

	private DomContent getSourceHeader(ViewSourceReportData src) {
		return h3("Source " + src.getsourceNumber()).withClass("w3-blue");
	}

	private DomContent getSourceLink(ViewSourceReportData src) {
		return a(" Show/Hide Source" +src.getsourceNumber()).withHref(getSourceHref(src)).withClass("w3-btn");
	}

	private String getSourceHref(ViewSourceReportData src) {
		return "javascript:toggleDiv(\""+ getSrcID(src) + "\")";
	}

	private String getSrcID(ViewSourceReportData src) {
		return "src" + src.getsourceNumber();
	}

	private ContainerTag<DivTag> getFormatPhasePropertiesIfNeeded(ViewReportData vw) {
		return div(
				h3("Format Phase Properties"),
				table(
						tbody(
								getFormatPropTableHeader(),
								getFormatPropsData(vw.getView())
								)
						).withClass("w3-table-all w3-striped w3-border")
				);
	}

	private DomContent getFormatPropsData(ViewPropertiesReportQueryBean view) {
		return tr(
				td(view.getOutputmaxreccnt()).withClass("w3-border"),
				td(view.getPagesize()).withClass("w3-border"),
				td(view.getLinesize()).withClass("w3-border"),
				td(view.getFormatexitid().equals("0") ? "" : view.getFrmtexit() + "[" + view.getFormatexitid() + "]").withClass("w3-border"),
				td(view.getFormatexitstartup() == null  ?  "" : view.getFormatexitstartup()).withClass("w3-border"),
				td(view.getZerosuppressind().equals("0") ? "false" : "true").withClass("w3-border"),
				td(view.getFormatfiltlogic()).withClass("w3-border")
				).withClass("w3-border");
	}

	private DomContent getFormatPropTableHeader() {
		return tr(
					th("Format Output Limit").withClass("w3-border"),
					th("Lines Per Page").withClass("w3-border"),
					th("Report Width").withClass("w3-border"),
					th("Format Exit").withClass("w3-border"),
					th("Format Exit Parameter").withClass("w3-border"),
					th("Zero Suppression").withClass("w3-border"),
					th("Format Filter").withClass("w3-border")
				);
	}

	private ContainerTag<DivTag> getExtractPhaseProperties(ViewReportData vw) {
		return div(
				h3("Extract Phase Properties"),
				table(
						tbody(
								getExtractPropTableHeader(),
								getExtractPropsData(vw.getView())
								)
						).withClass("w3-table-all w3-striped w3-border")
				).withClass("w3-twothirds");
	}

	private DomContent getExtractPropsData(ViewPropertiesReportQueryBean view) {
		return tr(
				td(view.getExtractmaxreccnt()).withClass("w3-border"),
				td(view.getExtractsummarybuf()).withClass("w3-border"),
				td(view.getExtractfilepartnbr()).withClass("w3-border")
				).withClass("w3-border");
	}

	private DomContent getExtractPropTableHeader() {
		return tr(
				th("Extract Output Limit").withClass("w3-border"),
				th("Extract Buffer Size").withClass("w3-border"),
				th("Extract Work File Number").withClass("w3-border")
				);
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

	public void addViewData(Map<Integer, ViewReportData> viewReportDataById) {
		this.viewReportData = viewReportDataById;
	}


}
