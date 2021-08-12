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


import java.util.Date;

public class ViewTransfer extends SAFRActiveComponentTransfer {

	private String statusCode;
	private String typeCode;
	private Integer workFileNumber;
	private String outputFormatCode;
	private Integer outputLRId;
	private Integer extractFileAssocId;
	private Integer pageSize;
	private Integer lineSize;
	private Boolean zeroSuppressInd;
	private Boolean headerRow;
	private Integer extractMaxRecCount;
	private Boolean extractSummaryIndicator;
	private Integer extractSummaryBuffer;
	private Integer outputMaxRecCount;
	private Integer controlRecId;
	private Integer writeExitId;
	private String writeExitParams;
	private Integer formatExitId;
	private String formatExitParams;
	private String fieldDelimCode;
	private String stringDelimCode;
	
	private Date effectiveDate;
	private String formatFilterlogic;
	private String compilerVersion;
	// This field is to store the logic text of type Blob for a view from E_LOGICTBL.
	private byte[] logicTextBytes;
	// this will store the compiled version of logic text.
	private byte[] compiledLogicTextBytes;

	public void setStatusCode(String viewStatusCode) {
		this.statusCode = viewStatusCode;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setTypeCode(String viewTypeCode) {
		this.typeCode = viewTypeCode;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public Integer getWorkFileNumber() {
		return workFileNumber;
	}

	public void setWorkFileNumber(Integer workFileNumber) {
		this.workFileNumber = workFileNumber;
	}

	public void setOutputFormatCode(String viewOutputFormat) {
		this.outputFormatCode = viewOutputFormat;
	}

	public String getOutputFormatCode() {
		return outputFormatCode;
	}

	public Integer getOutputLRId() {
		return outputLRId;
	}

	public void setOutputLRId(Integer outputLRId) {
		this.outputLRId = outputLRId;
	}

	public Integer getExtractFileAssocId() {
		return extractFileAssocId;
	}

	public void setExtractFileAssocId(Integer extractFileAssocId) {
		this.extractFileAssocId = extractFileAssocId;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getLineSize() {
		return lineSize;
	}

	public void setLineSize(Integer lineSize) {
		this.lineSize = lineSize;
	}

	public Boolean isSuppressZeroRecords() {
		return zeroSuppressInd;
	}

	public void setZeroSuppressInd(Boolean zeroSuppressInd) {
		this.zeroSuppressInd = zeroSuppressInd;
	}

	public Boolean isHeaderRow() {
		return headerRow;
	}
	
	public void setHeaderRow(Boolean headerRow) {
		this.headerRow = headerRow;
	}
	
	public Integer getExtractMaxRecCount() {
		return extractMaxRecCount;
	}

	public void setExtractMaxRecCount(Integer extractMaxRecCnt) {
		this.extractMaxRecCount = extractMaxRecCnt;
	}

	public Boolean isAggregateBySortKey() {
		return extractSummaryIndicator;
	}

	public void setExtractSummaryIndicator(Boolean aggregateBySortKey) {
		this.extractSummaryIndicator = aggregateBySortKey;
	}

	public Integer getExtractSummaryBuffer() {
		return extractSummaryBuffer;
	}

	public void setExtractSummaryBuffer(Integer aggregateBufferSize) {
		this.extractSummaryBuffer = aggregateBufferSize;
	}

	public Integer getOutputMaxRecCount() {
		return outputMaxRecCount;
	}

	public void setOutputMaxRecCount(Integer outputMaxRecCnt) {
		this.outputMaxRecCount = outputMaxRecCnt;
	}

	public Integer getControlRecId() {
		return controlRecId;
	}

	public void setControlRecId(Integer controlRecId) {
		this.controlRecId = controlRecId;
	}

	public Integer getWriteExitId() {
		return writeExitId;
	}

	public void setWriteExitId(Integer writeExitId) {
		this.writeExitId = writeExitId;
	}

	public String getWriteExitParams() {
		return writeExitParams;
	}

	public void setWriteExitParams(String writeExitParams) {
		this.writeExitParams = writeExitParams;
	}

	public Integer getFormatExitId() {
		return formatExitId;
	}

	public void setFormatExitId(Integer formatExitId) {
		this.formatExitId = formatExitId;
	}

	public String getFormatExitParams() {
		return formatExitParams;
	}

	public void setFormatExitParams(String formatExitParams) {
		this.formatExitParams = formatExitParams;
	}

	public String getFieldDelimCode() {
		return fieldDelimCode;
	}

	public void setFieldDelimCode(String fieldDelimCode) {
		this.fieldDelimCode = fieldDelimCode;
	}

	public String getStringDelimCode() {
		return stringDelimCode;
	}

	public void setStringDelimCode(String stringDelimCode) {
		this.stringDelimCode = stringDelimCode;
	}

	public byte[] getLogicTextBytes() {
		return logicTextBytes;
	}

	public void setLogicTextBytes(byte[] logicTextBytes) {
		this.logicTextBytes = logicTextBytes;
	}

	public void setCompiledLogicTextBytes(byte[] compiledLogicTextBytes) {
		this.compiledLogicTextBytes = compiledLogicTextBytes;
	}

	public byte[] getCompiledLogicTextBytes() {
		return compiledLogicTextBytes;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public String getFormatFilterlogic() {
		return formatFilterlogic;
	}

	public void setFormatFilterlogic(String formatFilterlogic) {
		this.formatFilterlogic = formatFilterlogic;
	}

    public String getCompilerVersion() {
        return compilerVersion;
    }

    public void setCompilerVersion(String compilerVersion) {
        this.compilerVersion = compilerVersion;
    }	
}
