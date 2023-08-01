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


import java.nio.file.Path;
import java.util.ArrayList;
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

public class ViewCsvReport extends  GenevaCSVReport  {

	private Map<Integer, ViewReportData> viewReportData;
	private String outputFile;
	private Path csvPath;
	
	public ViewCsvReport() {
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

	private String getFormatCalcLogic(ViewMappingsReportQueryBean cm) {
		if(cm.getFormatcalclogic() == null) {
			return "";
		} else { 
			return cm.getFormatcalclogic();
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

	private String getColumnValue(ViewMappingsReportQueryBean cm) {
		int sourceType = cm.getSourcetypeid();
		if (sourceType == Codes.CONSTANT) {
			return cm.getConstval();
		} else if (sourceType == Codes.SOURCE_FILE_FIELD) {
			return cm.getFieldName() + "[" + cm.getLrfieldid() + "]";
		} else if (sourceType == Codes.LOOKUP_FIELD) {
			return cm.getLkName() + "." +  cm.getFieldName() + "[" + cm.getLrfieldid() + "]";
		} else if (sourceType == Codes.FORMULA) {
			return cm.getExtractcalclogic();
		}
		return "invalid";
	}
	
	private String getSourceHref(ViewSourceReportData src) {
		return "javascript:toggleDiv(\""+ getSrcID(src) + "\")";
	}

	private String getSrcID(ViewSourceReportData src) {
		return "src" + src.getsourceNumber();
	}

	public void setFileName(Path path, String baseName, List<Integer> viewIDs) {
		csvPath = makeCsvDirIfNeeded(path);
		outputFile = baseName + "_Env" + SAFRApplication.getUserSession().getEnvironment().getId();
		for(Integer id : viewIDs) {
			outputFile += "_[" + id.toString() + "]";
		}
	}

	public void addViewData(Map<Integer, ViewReportData> viewReportDataById) {
		this.viewReportData = viewReportDataById;
	}

	@Override
	protected List<String> getHeaders() {
		return headers;
	}

	@Override
	protected List<List<String>> getRows() {
		return allrows;
	}

	public void writeProperties() {
		reportPath = csvPath.resolve(outputFile + "_props.csv");
		addPropsHeaders();
		addPropsData();
		write();
	}

	private void addPropsData() {
		for( Integer v : viewReportData.keySet()) {
			addViewPropsData(v);
		}
	}

	private void addViewPropsData(Integer v) {
		List<String> row = new ArrayList<>();
		ViewReportData vr = viewReportData.get(v);
		ViewPropertiesReportQueryBean vb = vr.getView();
		row.add(Integer.toString(v));
		row.add(vb.getName());
		row.add(vr.getPhase());
		row.add(getOutputformat(vb));
		row.add(vb.getLfname() == null  ? "" : vb.getLfname());
		row.add(vb.getPfname() == null  ? "" : vb.getPfname());
		row.add(vb.getExtractmaxreccnt());
		row.add(vb.getOutputmaxreccnt());
		row.add(vb.getPagesize());
		row.add(vb.getLinesize());
		row.add(vb.getControlName());
		row.add(vb.getFormatexitid().equals("0") ? "" : vb.getFrmtexit() + "[" + vb.getFormatexitid() + "]");
		row.add(vb.getFormatexitstartup() == null  ?  "" : vb.getFormatexitstartup());
		row.add(vb.getZerosuppressind().equals("0") ? "false" : "true");
		row.add(vb.getFormatfiltlogic());
		allrows.add(row);
	}

	private void addPropsHeaders() {
		headers.add("ViewID");
		headers.add("Name");
		headers.add("Phase");
		headers.add("Output Format");
		headers.add("Output Logical File");
		headers.add("Output Physical File");
		headers.add("Extract Output Limit");
		headers.add("Format Output Limit");
		headers.add("Lines Per Page");
		headers.add("Report Width");
		headers.add("Control Record");
		headers.add("Format Exit");
		headers.add("Format Exit Parameter");
		headers.add("Extract Buffer Size");
		headers.add("Zero Suppression");
		headers.add("Format Logic");
	}

	public void writeSourcesAndMappings() {
		headers.clear();
		allrows.clear();
		reportPath = csvPath.resolve(outputFile + "_sourceIO.csv");
		writeSourceIOForEachView();
		headers.clear();
		allrows.clear();
		reportPath = csvPath.resolve(outputFile + "_mappings.csv");
		addMappingsHeaders();
		addMappingsDataForEachView();
		write();
	}

	private void writeSourceIOForEachView() {
		addSourcesHeader();
		for( Integer v : viewReportData.keySet()) {
			writeSourceIO(v);
		}
	}

	private void writeSourceIO(Integer v) {
		ViewReportData vr = viewReportData.get(v);
		for (ViewSourceReportData src : vr.getViewSources() ) {
			addViewSourceData(src);
		}
		write();
	}

	private void addViewSourceData(ViewSourceReportData src) {
		List<String> row = new ArrayList<>();
		ViewSourcesReportQueryBean sb = src.getViewSource();
		row.add(sb.getViewid());
		row.add(sb.getSrcseqnbr());
		row.add(sb.getLrname());
		row.add(sb.getLogrecid());
		row.add(sb.getLfname());
		row.add(sb.getLogfileid());
		row.add(sb.getExtractfiltlogic());
		row.add(sb.getOutLf());
		row.add(sb.getOutPF());
		row.add(sb.getWriteExitName());
		row.add(sb.getWriteexitparm());
		row.add(sb.getExtractoutputlogic());
		allrows.add(row);
	}

	private void addSourcesHeader() {
		headers.add("View");
		headers.add("Source Number");
		headers.add("Logical Record Name");
		headers.add("Logical Record ID");
		headers.add("Logical File Name");
		headers.add("Logical File ID");
		headers.add("Record Filter");
		headers.add("Output Logical File");
		headers.add("Output Physical File");
		headers.add("Write Exit");
		headers.add("Write Exit Parm");
		headers.add("Output Logic");
	}

	private void addMappingsDataForEachView() {
		for( Integer v : viewReportData.keySet()) {
			addViewMappingsData(v);
		}
	}

	private void addViewMappingsData(Integer v) {
		ViewReportData vr = viewReportData.get(v);
		for (ViewSourceReportData src : vr.getViewSources() ) {
			addViewSourceMappings(src);
		}
	}

	private void addSourceMappings(ViewSourceReportData src, ViewMappingsReportQueryBean m, List<String> row) {
		addSourcePrefix(src, row);
		addColumnMappings(m, row);
	}

	private void addColumnMappings(ViewMappingsReportQueryBean cm, List<String> row) {
		row.add(cm.getColumnnumber());
		row.add(getColumnType(cm));
		row.add(getColumnValue(cm));
		row.add("->");
		row.add(cm.getStartposition());
		row.add(getFieldDatatype(cm.getFldfmtcd()));
		row.add(getDatetime(cm.getFldcontentcd()));
		row.add(cm.getMaxlen());
		row.add(getAlignment(cm));
		row.add(cm.getDecimalcnt());
		row.add(cm.getRounding());
		row.add(cm.getSignedind().equals("0") ? "false" : "true");
		row.add(cm.getVisible().equals("0") ? "false" : "true");
		row.add(cm.getSpacesbeforecolumn());
		row.add(getFormatCalcLogic(cm));
		if(cm.getSortkey() != null) {
			addSortkeyToReport(cm.getSortkey(), row);
		}
	}

	private void addSortkeyToReport(ViewSortKeyReportQueryBean sk, List<String> row) {
		if(sk.getKeyseqnbr().equalsIgnoreCase("1")) {
			appendSortKeyHeader();
		}
		row.add(sk.getKeyseqnbr());
		row.add(sk.getSkstartpos());
		row.add(sk.getSortseqcd());
		row.add(sk.getSortbrkind());
		row.add(sk.getSortkeylabel());
		row.add(getFieldDatatype(sk.getSkfldfmtcd()));
		row.add(getDatetime(sk.getSkfldcontentcd()));
		row.add(sk.getSkfldlen());
		row.add(sk.getSkdecimalcnt());
		row.add(sk.getSksigned());
	}

	private void appendSortKeyHeader() {
		headers.add("Sort Sequence Number");
		headers.add("Start Position");
		headers.add("Order");
		headers.add("Sort Break");
		headers.add("Label");
		headers.add("Datatype");
		headers.add("Format");
		headers.add("Length");
		headers.add("DecimalPlaces");
		headers.add("Signed");
	}

	private void addViewSourceMappings(ViewSourceReportData src) {
		for( ViewMappingsReportQueryBean m : src.getColumnMappings()) {
			List<String> row = new ArrayList<>();
			addSourceMappings(src, m, row);
			allrows.add(row);			
		}
		
	}

	private void addSourcePrefix(ViewSourceReportData src, List<String> row) {
		ViewSourcesReportQueryBean s = src.getViewSource();
		row.add(s.getViewid());
		row.add(s.getLrname());
		row.add(s.getLogrecid());
		row.add(s.getLfname());
		row.add(s.getLogfileid());
	}

	private void addMappingsHeaders() {
		headers.add("View");
		headers.add("Logical Record Name");
		headers.add("Logical Record ID");
		headers.add("Logical File Name");
		headers.add("Logical File ID");
		headers.add("Column Number");
		headers.add("Type");
		headers.add("Value");
		headers.add(" ");
		headers.add("Start Position");
		headers.add("Type");
		headers.add("Format");
		headers.add("Length");
		headers.add("Alignment");
		headers.add("DecimalPlaces");
		headers.add("Scaling");
		headers.add("Signed");
		headers.add("Visible");
		headers.add("Spaces");
		headers.add("FormatPhaseCalculation");
	}


}
