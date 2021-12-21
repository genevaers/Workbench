package com.ibm.safr.we.data.transfer;

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


public class ViewSortKeyTransfer extends SAFREnvironmentalComponentTransfer {

	private Integer viewId; // VIEWID
	private Integer viewColumnId; // VIEWCOLUMNID
	private Integer keySequenceNo; // KEYSEQNBR
	private String sortSequenceCode; // SORTSEQCD
	private Integer footerOptionCode; // SORTBRKIND
	private Integer headerOptionCode; // PAGEBRKIND
	private String displayModeCode; // SORTKEYDISPLAYCD
	private String sortkeyLabel; // SORTKEYLABEL
	private String dataTypeCode; // SKFLDFMTCD
	private Boolean signed; // SKSIGNED
	private Integer startPosition; // SKSTARTPOS
	private Integer length; // SKFLDLEN
	private Integer decimalPlaces; // SKDECIMALCNT
	private String dateTimeFormatCode; // SKFLDCONTENTCD
	private Integer titleFieldId; // SORTTITLELRFIELDID
	private Integer titleLength; // SORTTITLELENGTH

	public Integer getViewId() {
		return viewId;
	}

	public void setViewId(Integer viewId) {
		this.viewId = viewId;
	}

	public Integer getViewColumnId() {
		return viewColumnId;
	}

	public void setViewColumnId(Integer viewColumnId) {
		this.viewColumnId = viewColumnId;
	}

	public Integer getKeySequenceNo() {
		return keySequenceNo;
	}

	public void setKeySequenceNo(Integer keySequenceNo) {
		this.keySequenceNo = keySequenceNo;
	}

	public String getSortSequenceCode() {
		return sortSequenceCode;
	}

	public void setSortSequenceCode(String sortSequenceCode) {
		this.sortSequenceCode = sortSequenceCode;
	}

	public Integer getFooterOptionCode() {
		return footerOptionCode;
	}

	public void setFooterOptionCode(Integer footerOptionCode) {
		this.footerOptionCode = footerOptionCode;
	}

	public Integer getHeaderOptionCode() {
		return headerOptionCode;
	}

	public void setHeaderOptionCode(Integer headerOptionCode) {
		this.headerOptionCode = headerOptionCode;
	}

	public String getDisplayModeCode() {
		return displayModeCode;
	}

	public void setDisplayModeCode(String displayModeCode) {
		this.displayModeCode = displayModeCode;
	}

	public String getSortkeyLabel() {
		return sortkeyLabel;
	}

	public void setSortkeyLabel(String sortkeyLabel) {
		this.sortkeyLabel = sortkeyLabel;
	}

	public String getDataTypeCode() {
		return dataTypeCode;
	}

	public void setDataTypeCode(String dataTypeCode) {
		this.dataTypeCode = dataTypeCode;
	}

	public Boolean isSigned() {
		return signed;
	}

	public void setSigned(Boolean signed) {
		this.signed = signed;
	}

	public Integer getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(Integer startPosition) {
		this.startPosition = startPosition;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(Integer decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	public String getDateTimeFormatCode() {
		return dateTimeFormatCode;
	}

	public void setDateTimeFormatCode(String dateTimeFormatCode) {
		this.dateTimeFormatCode = dateTimeFormatCode;
	}

	public Integer getTitleFieldId() {
		return titleFieldId;
	}

	public void setTitleFieldId(Integer titleFieldId) {
		this.titleFieldId = titleFieldId;
	}

	/**
	 * @return the titleLength
	 */
	public Integer getTitleLength() {
		return titleLength;
	}

	/**
	 * @param titleLength
	 *            the titleLength to set
	 */
	public void setTitleLength(Integer titleLength) {
		this.titleLength = titleLength;
	}

}
