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


/************************************************************************************************************************/

public class SortKeyTitlesRD extends ReportTableData{
	private Integer viewSourceNo;
	private String sortKeyTitleFieldString;
	private String effectiveDateType;
	private String effectiveDateValue;

	public Integer getViewSourceNo() {
		return viewSourceNo;
	}

	public void setViewSourceNo(Integer viewSourceNo) {
		this.viewSourceNo = viewSourceNo;
	}

	public void setSortKeyTitleFieldString(String sortKeyTitleFieldString) {
		this.sortKeyTitleFieldString = sortKeyTitleFieldString;
	}

	public String getSortKeyTitleFieldString() {
		return sortKeyTitleFieldString;
	}

	public String getEffectiveDateType() {
		return effectiveDateType;
	}

	public void setEffectiveDateType(String effectiveDateType) {
		this.effectiveDateType = effectiveDateType;
	}

	public void setEffectiveDateValue(String effectiveDateValue) {
		this.effectiveDateValue = effectiveDateValue;
	}

	public String getEffectiveDateValue() {
		return effectiveDateValue;
	}

	@Override
	public String getHtmlHeaderRow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHtmlDataRow() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getTaglessHtmlHeaderRow() {
		String hr = "";
		hr += getHeaderEntry("Title Field", "yellow");
		hr += getHeaderEntry("Effective Date", "yellow");
		hr += getHeaderEntry("Effective Date Value", "yellow");
		return hr;
	}

	public String getTaglessHtmlDataRow() {
		String data = "";
		data += getRowEntry(sortKeyTitleFieldString);
		data += getRowEntry(effectiveDateType);
		data += getRowEntry(effectiveDateValue);
		return  data;
	}

}
