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


public abstract class SAFRFieldTransfer extends
		SAFREnvironmentalComponentTransfer {

	// modifiable by the user
	private String dataType;
	private Integer length;
	private Integer decimalPlaces;
	private Boolean signed;
	private Integer scalingFactor;
	private String dateTimeFormat;
	private String headerAlignment;
	private String columnHeading1;
	private String columnHeading2;
	private String columnHeading3;
	private String numericMask;
	private String sortKeyLabel;
	private String defaultValue;
	private String subtotalLabel;

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType
	 *            the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the length
	 */
	public Integer getLength() {
		return length;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(Integer length) {
		this.length = length;
	}

	/**
	 * @return the decimalPlaces
	 */
	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}

	/**
	 * @param decimalPlaces
	 *            the decimalPlaces to set
	 */
	public void setDecimalPlaces(Integer decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	/**
	 * @return the signed
	 */
	public Boolean isSigned() {
		return signed;
	}

	/**
	 * @param signed
	 *            the signed to set
	 */
	public void setSigned(Boolean signed) {
		this.signed = signed;
	}

	/**
	 * @return the scalingFactor
	 */
	public Integer getScalingFactor() {
		return scalingFactor;
	}

	/**
	 * @param scalingFactor
	 *            the scalingFactor to set
	 */
	public void setScalingFactor(Integer scalingFactor) {
		this.scalingFactor = scalingFactor;
	}

	/**
	 * @return the dateTimeFormat
	 */
	public String getDateTimeFormat() {
		return dateTimeFormat;
	}

	/**
	 * @param dateTimeFormat
	 *            the dateTimeFormat to set
	 */
	public void setDateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}

	/**
	 * @return the headerAlignment
	 */
	public String getHeaderAlignment() {
		return headerAlignment;
	}

	/**
	 * @param headerAlignment
	 *            the headerAlignment to set
	 */
	public void setHeaderAlignment(String headerAlignment) {
		this.headerAlignment = headerAlignment;
	}

	/**
	 * @return the columnHeading1
	 */
	public String getColumnHeading1() {
		return columnHeading1;
	}

	/**
	 * @param columnHeading1
	 *            the columnHeading1 to set
	 */
	public void setColumnHeading1(String columnHeading1) {
		this.columnHeading1 = columnHeading1;
	}

	/**
	 * @return the columnHeading2
	 */
	public String getColumnHeading2() {
		return columnHeading2;
	}

	/**
	 * @param columnHeading2
	 *            the columnHeading2 to set
	 */
	public void setColumnHeading2(String columnHeading2) {
		this.columnHeading2 = columnHeading2;
	}

	/**
	 * @return the columnHeading3
	 */
	public String getColumnHeading3() {
		return columnHeading3;
	}

	/**
	 * @param columnHeading3
	 *            the columnHeading3 to set
	 */
	public void setColumnHeading3(String columnHeading3) {
		this.columnHeading3 = columnHeading3;
	}

	/**
	 * @return the numericMask
	 */
	public String getNumericMask() {
		return numericMask;
	}

	/**
	 * @param numericMask
	 *            the numericMask to set
	 */
	public void setNumericMask(String numericMask) {
		this.numericMask = numericMask;
	}

	/**
	 * @return the sortKeyLabel
	 */
	public String getSortKeyLabel() {
		return sortKeyLabel;
	}

	/**
	 * @param sortKeyLabel
	 *            the sortKeyLabel to set
	 */
	public void setSortKeyLabel(String sortKeyLabel) {
		this.sortKeyLabel = sortKeyLabel;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue
	 *            the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the subtotalLabel
	 */
	public String getSubtotalLabel() {
		return subtotalLabel;
	}

	/**
	 * @param subtotalLabel
	 *            the subtotalLabel to set
	 */
	public void setSubtotalLabel(String subtotalLabel) {
		this.subtotalLabel = subtotalLabel;
	}

}
