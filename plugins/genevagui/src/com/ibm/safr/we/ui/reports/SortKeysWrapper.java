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


import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ViewSortKeyReportQueryBean;

/************************************************************************************************************************/

public class SortKeysWrapper extends ReportTableData{
	private Boolean title;
	private SortKeyTitlesRD sortKeyTitle;
	private ViewSortKeyReportQueryBean sortKey;
	private boolean report;

	public SortKeysWrapper(ViewSortKeyReportQueryBean vsk) {
		sortKey = vsk;
	}

	public String getTitle() {
		if (title) {
			return "True";
		} else {
			return "False";
		}
	}

	public void setTitle(Boolean title) {
		this.title = title;
	}

	public SortKeyTitlesRD getSortKeyTitles() {
		return sortKeyTitle;
	}

	public void setSortKeyTitle(SortKeyTitlesRD sortKeyTitle) {
		this.sortKeyTitle = sortKeyTitle;
	}
	
	public String getTaglessHtmlHeaderRow() {
		String hr = "";
		hr += getHeaderEntry("Sequence No");
		hr += getHeaderEntry("Start Position");
		hr += getHeaderEntry("Order");
		hr += getHeaderEntry("Sort Break");
		hr += getHeaderEntry("Label");
		hr += getHeaderEntry("Data Type");
		hr += getHeaderEntry("Format");
		hr += getHeaderEntry("Length");
		hr += getHeaderEntry("Decimals");
		hr += getHeaderEntry("Signed");
		if(sortKeyTitle != null) {
			hr += sortKeyTitle.getTaglessHtmlHeaderRow();
		}
		return hr;
	}

	@Override
	public String getHtmlHeaderRow() {
		String hr = "<tr>";
		if(report) {
			hr += "<th colspan=\"13\" bgcolor=\"yellow\" >Sortkey Properties</th></tr>";
		} else {
			hr += "<th colspan=\"8\" bgcolor=\"yellow\" >Sortkey Properties</th></tr>";			
		}
		hr +=  "<tr>";
		hr += getHeaderEntry("Sequence No", "yellow");
		hr += getHeaderEntry("Start Position", "yellow");
		hr += getHeaderEntry("Order", "yellow");
		hr += getHeaderEntry("Data Type", "yellow");
		hr += getHeaderEntry("Format", "yellow");
		hr += getHeaderEntry("Length", "yellow");
		hr += getHeaderEntry("Decimals", "yellow");
		hr += getHeaderEntry("Signed", "yellow");
		if(report) {
			hr += getHeaderEntry("Display Mode", "yellow");
			hr += getHeaderEntry("Label", "yellow");
			hr += getHeaderEntry("Footer Label", "yellow");
			hr += getHeaderEntry("Header Opt", "yellow");
			hr += getHeaderEntry("FooterOpt", "yellow");
			if(sortKeyTitle != null) {
				hr += sortKeyTitle.getTaglessHtmlHeaderRow();
			}
		}
		hr += "</tr>";
		return hr;
	}

	@Override
	public String getHtmlDataRow() {
		String data = "<tr>";
		data += getRowEntry(sortKey.getKeyseqnbr());
		data += getRowEntry(sortKey.getSkstartpos());
		data += getRowEntry(getSortSeq());
		data += getRowEntry(getFieldDatatype());
		data += getRowEntry(getDatetime());
		data += getRowEntry(sortKey.getSkfldlen());
		data += getRowEntry(sortKey.getSkdecimalcnt());
		data += getRowEntry(sortKey.getSksigned().equals("0") ? "false" : "true");
		if(report) {
			data += getRowEntry(getDisplayOpt());
			if(sortKey.getSortkeydisplaycd() != null && sortKey.getSortkeydisplaycd().trim().equals("ASDTA")) {
				data += getRowEntry("");
				data += getRowEntry("");
				data += getRowEntry("");			
			} else {
				data += getRowEntry(sortKey.getSortkeylabel());
				data += getRowEntry(sortKey.getSubtlabel());
				data += getRowEntry(getPageBreak());
			}
			data += getRowEntry(getFooterBreak());
		if(sortKeyTitle != null) {
			data += sortKeyTitle.getTaglessHtmlDataRow();
		}
		}
		data += "</tr>";
		return  data;
	}

	private String getFooterBreak() {
		Code code = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SORTBRKFTR).getCode(Integer.valueOf(sortKey.getSortbrkind()));
		return code.getDescription();
	}

	private String getDisplayOpt() {
		if(sortKey.getSortkeydisplaycd() != null) {
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SORTDSP).getCode(sortKey.getSortkeydisplaycd().trim());
			return code.getDescription();
		} else {
			return "";
		}
	}

	private String getPageBreak() {
		Code code = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SORTBRKHDR).getCode(Integer.valueOf(sortKey.getPagebrkind()));
		return code.getDescription();
	}

	private String getSortSeq() {
		if(sortKey.getSortseqcd() != null) {
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SORTSEQ).getCode(sortKey.getSortseqcd());
			return code.getDescription();
		} else {
			return "";
		}
	}

	private String getDatetime() {
		if(sortKey.getSkfldcontentcd() == null) {
			return "";
		} else {
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.FLDCONTENT).getCode(sortKey.getSkfldcontentcd().trim());
			return code.getDescription();
		}
	}

	private String getFieldDatatype() {
		if(sortKey.getSkfldfmtcd() != null) {
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.DATATYPE).getCode(sortKey.getSkfldfmtcd().trim());
			return code.getDescription();
		} else {
			return "";
		}
	}


	public String getTaglessHtmlDataRow() {
		String data = "";
		data += getRowEntry(sortKey.getKeyseqnbr());
		data += getRowEntry(sortKey.getSkstartpos());
		data += getRowEntry(sortKey.getSortseqcd());
		data += getRowEntry(sortKey.getSortbrkind());
		data += getRowEntry(sortKey.getSortkeylabel());
		data += getRowEntry(sortKey.getSkfldfmtcd());
		data += getRowEntry(sortKey.getSkfldcontentcd());
		data += getRowEntry(sortKey.getSkfldlen());
		data += getRowEntry(sortKey.getSkdecimalcnt());
		data += getRowEntry(sortKey.getSksigned());
		if(sortKeyTitle != null) {
			data += sortKeyTitle.getTaglessHtmlDataRow();
		}
		return  data;
	}

	public void setReport(boolean report) {
		this.report = report;
	}

}
