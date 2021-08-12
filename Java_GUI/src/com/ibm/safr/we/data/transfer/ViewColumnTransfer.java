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


public class ViewColumnTransfer extends SAFRFieldTransfer {

	private Integer viewId;
	private Integer columnNo;
	private Integer startPosition;
	private String dataAlignmentCode;
	private Boolean visible;
	private Integer spacesBeforeColumn;
	private String sortkeyFooterLabel;

	private Integer ordinalPosition;
	private String subtotalTypeCode;
	private String extractAreaCode;
	private Integer extractAreaPosition;
	private String formatColumnLogic;

	public void setViewId(Integer viewId) {
		this.viewId = viewId;
	}

	public Integer getViewId() {
		return viewId;
	}

	public void setColumnNo(Integer columnNo) {
		this.columnNo = columnNo;
	}

	public Integer getColumnNo() {
		return columnNo;
	}

	public void setStartPosition(Integer startPosition) {
		this.startPosition = startPosition;
	}

	public Integer getStartPosition() {
		return startPosition;
	}

	public void setDataAlignmentCode(String dataAlignmentCode) {
		this.dataAlignmentCode = dataAlignmentCode;
	}

	public String getDataAlignmentCode() {
		return dataAlignmentCode;
	}

	public Boolean isVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Integer getSpacesBeforeColumn() {
		return spacesBeforeColumn;
	}

	public void setSpacesBeforeColumn(Integer spacesBeforeColumn) {
		this.spacesBeforeColumn = spacesBeforeColumn;
	}

	public String getSortkeyFooterLabel() {
		return sortkeyFooterLabel;
	}

	public void setSortkeyFooterLabel(String sortkeyFooterLabel) {
		this.sortkeyFooterLabel = sortkeyFooterLabel;
	}

	public Integer getOrdinalPosition() {
		return ordinalPosition;
	}

	public void setOrdinalPosition(Integer ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
	}

	public String getSubtotalTypeCode() {
		return subtotalTypeCode;
	}

	public void setSubtotalTypeCode(String subtotalTypeCode) {
		this.subtotalTypeCode = subtotalTypeCode;
	}

	public String getExtractAreaCode() {
		return extractAreaCode;
	}

	public void setExtractAreaCode(String extractAreaCode) {
		this.extractAreaCode = extractAreaCode;
	}

	public Integer getExtractAreaPosition() {
		return extractAreaPosition;
	}

	public void setExtractAreaPosition(Integer extractAreaPosition) {
		this.extractAreaPosition = extractAreaPosition;
	}

	public String getFormatColumnLogic() {
		return formatColumnLogic;
	}

	public void setFormatColumnLogic(String formatColumnLogic) {
		this.formatColumnLogic = formatColumnLogic;
	}

}
