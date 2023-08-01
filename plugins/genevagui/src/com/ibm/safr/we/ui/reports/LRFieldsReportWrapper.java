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


import com.ibm.safr.we.constants.LRFieldKeyType;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class LRFieldsReportWrapper extends ReportTableData{
	private LRField field;
	private Integer fieldId;
	private String fieldName;
	private Integer startPosition;
	private String redefine;
	private String primaryKey;
	private String dataType;
	private Integer length;
	private Integer decimal;
	private String dateTimeFormat;

	public LRFieldsReportWrapper(LRField lrField) {
		field = lrField;
		fieldName = lrField.getName();
		startPosition = lrField.getPosition();
		if (lrField.getRedefine() == null) {
            redefine = "";
		} else {
            redefine = lrField.getRedefine().toString();
		}
		if (lrField.getKeyType().equals(LRFieldKeyType.PRIMARYKEY)) {
			primaryKey = "P";
		} else {
			primaryKey = "";
		}
		if (lrField.getDataTypeCode() != null) {
			dataType = lrField.getDataTypeCode().getDescription();
		}
		length = lrField.getLength();
		decimal = lrField.getDecimals();
		if (lrField.getDateTimeFormatCode() != null) {
			dateTimeFormat = lrField.getDateTimeFormatCode().getDescription();
		}
	}

	public Integer getFieldId() {
		return fieldId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Integer getStartPosition() {
		return startPosition;
	}

	public String getRedefine() {
		return redefine;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public String getDataType() {
		return dataType;
	}

	public Integer getLength() {
		return length;
	}

	public Integer getDecimal() {
		return decimal;
	}

	public String getDateTimeFormat() {
		return dateTimeFormat;
	}

	@Override
	public String getHtmlHeaderRow() {
		String hdr ="<tr>"
				+ getHeaderEntry("Field Name")
				+ getHeaderEntry("Start Position")
				+ getHeaderEntry("DataType")
				+ "</tr>";
		return hdr;
	}

	@Override
	public String getHtmlDataRow() {
		String row ="<tr>"
				+ getRowEntry(UIUtilities.getComboString(field.getName(), field.getId()))
				+ getRowEntry(field.getPosition().toString())
				+ getRowEntry(field.getDataTypeCode().getDescription())
				+ "</tr>";
		return row;
	}

}
