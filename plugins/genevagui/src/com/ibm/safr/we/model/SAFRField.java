package com.ibm.safr.we.model;

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


import java.util.ArrayList;
import java.util.List;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.SAFRFieldTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

/**
 * Abstract class to define a parent SAFR Field having various general field
 * attributes. Metadata classes having a usage of fields (LR, 
 * etc.) should extend this class.
 * 
 */
public abstract class SAFRField extends SAFREnvironmentalComponent {

	private Code dataTypeCode;
	private Integer length;
	private Integer decimals;
	private Boolean signed;
	private Integer scaling;
	private Code dateTimeFormatCode;
	private Code headerAlignmentCode;
	private String heading1;
	private String heading2;
	private String heading3;
	private Code numericMaskCode;
	private String sortKeyLabel;
	private String defaultValue;
	private String subtotalLabel;

	private List<Property> loadWarningProperties;

	public enum Property {
		DATA_TYPE("data type"), DATE_TIME_FORMAT("date/time format"), HEADER_ALIGNMENT(
				"header alignment"), NUMERIC_MASK("numeric mask");

		private String text;

		Property(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	protected SAFRField(Integer environmentId) {
		super(environmentId);
		initializeVariables();
	}

	protected SAFRField(Environment environment) {
		super(environment);
		initializeVariables();
	}

	protected SAFRField(SAFRFieldTransfer trans) {
		super(trans);
	}

	private void initializeVariables() {
		length = 0;
		decimals = 0;
		signed = false;
		scaling = 0; // rounding
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		SAFRFieldTransfer trans = (SAFRFieldTransfer) safrTrans;

		loadWarningProperties = new ArrayList<Property>();

		try {
			this.dataTypeCode = ModelUtilities.getCodeFromKey(
					CodeCategories.DATATYPE, trans.getDataType());
		} catch (IllegalArgumentException iae) {
			loadWarningProperties.add(Property.DATA_TYPE);
			this.dataTypeCode = null;
		}
		this.length = trans.getLength();
		this.decimals = trans.getDecimalPlaces();
		this.signed = trans.isSigned();
		this.scaling = trans.getScalingFactor();
		try {
			this.dateTimeFormatCode = ModelUtilities.getCodeFromKey(
					CodeCategories.FLDCONTENT, trans.getDateTimeFormat());
		} catch (IllegalArgumentException iae) {
			loadWarningProperties.add(Property.DATE_TIME_FORMAT);
			this.dateTimeFormatCode = null;
		}
		try {
			this.headerAlignmentCode = ModelUtilities.getCodeFromKey(
					CodeCategories.JUSTIFY, trans.getHeaderAlignment());
		} catch (IllegalArgumentException iae) {
			loadWarningProperties.add(Property.HEADER_ALIGNMENT);
			this.headerAlignmentCode = null;
		}
		this.heading1 = trans.getColumnHeading1();
		this.heading2 = trans.getColumnHeading2();
		this.heading3 = trans.getColumnHeading3();
		try {
			this.numericMaskCode = ModelUtilities.getCodeFromKey(
					CodeCategories.FORMATMASK, trans.getNumericMask());
		} catch (IllegalArgumentException iae) {
			loadWarningProperties.add(Property.NUMERIC_MASK);
			this.numericMaskCode = null;

		}
		this.sortKeyLabel = trans.getSortKeyLabel();
		this.defaultValue = trans.getDefaultValue();
		this.subtotalLabel = trans.getSubtotalLabel();
	}

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		SAFRFieldTransfer trans = (SAFRFieldTransfer) safrTrans;
		trans.setDataType(dataTypeCode == null ? null : dataTypeCode.getKey());
		trans.setLength(length);
		trans.setDecimalPlaces(decimals);
		trans.setSigned(signed);
		trans.setScalingFactor(scaling);
		trans.setDateTimeFormat(dateTimeFormatCode == null ? null
				: dateTimeFormatCode.getKey());
		trans.setHeaderAlignment(headerAlignmentCode == null ? null
				: headerAlignmentCode.getKey());
		trans.setColumnHeading1(heading1);
		trans.setColumnHeading2(heading2);
		trans.setColumnHeading3(heading3);
		trans.setNumericMask(numericMaskCode == null ? null : numericMaskCode
				.getKey());
		trans.setSortKeyLabel(sortKeyLabel);
		trans.setDefaultValue(defaultValue);
		trans.setSubtotalLabel(subtotalLabel);
	}

	/**
	 * This method is used to get the data type code.
	 * 
	 * @return The data type code object.
	 */
	public Code getDataTypeCode() {
		return dataTypeCode;
	}

	/**
	 * This method is used to set Data type. Subclasses should override this
	 * behavior to throw a NullPointerException if they consider this attribute
	 * mandatory and the method argument is null.
	 * 
	 * @param dataTypeCode
	 *            : The data type which is to be set.
	 * 
	 */
	public void setDataTypeCode(Code dataTypeCode) {
		this.dataTypeCode = dataTypeCode;
		markModified();
	}

	/**
	 * This method is used to get the length.
	 * 
	 * @return length
	 */
	public Integer getLength() {
		return length;
	}

	/**
	 * This method is used to set the length.
	 * 
	 * @param length
	 *            : the length which is to be set.
	 */
	public void setLength(Integer length) {
		this.length = length;
		markModified();
	}

	/**
	 * This method is used to get the decimals.
	 * 
	 * @return: decimals.
	 */
	public Integer getDecimals() {
		return decimals;
	}

	/**
	 * This method is used to set the decimals.
	 * 
	 * @param decimals
	 */
	public void setDecimals(Integer decimals) {
		this.decimals = decimals;
		markModified();
	}

	/**
	 * This method is used to get the signed.
	 * 
	 * @return true if signed.
	 */
	public Boolean isSigned() {
		return signed;
	}

    /**
     */
    public boolean isNumeric() {
        if (dataTypeCode != null) {
            return !dataTypeCode.getGeneralId().equals(Codes.ALPHANUMERIC);
        } else {
            return false;
        }
    }
    
	
	/**
	 *This method is used to set signed.
	 * 
	 * @param signed
	 *            : true or false depending on whether it should be signed or
	 *            unsigned respectively.
	 */
	public void setSigned(Boolean signed) {
		this.signed = signed;
		markModified();
	}

	/**
	 * This method is used to get the scaling.
	 * 
	 * @return scaling
	 */
	public Integer getScaling() {
		return scaling;
	}

	/**
	 * This method is used to set the scaling.
	 * 
	 * @param scaling
	 *            : The scaling which is to be set.
	 */
	public void setScaling(Integer scaling) {
		this.scaling = scaling;
		markModified();
	}

	/**
	 * this method is used to get the date time format.
	 * 
	 * @return The date time format.
	 */
	public Code getDateTimeFormatCode() {
		return dateTimeFormatCode;
	}

	/**
	 * This method is used to set the date time format.
	 * 
	 * @param dateTimeFormatCode
	 *            : The date time format which is to be set.
	 */
	public void setDateTimeFormatCode(Code dateTimeFormatCode) {
		this.dateTimeFormatCode = dateTimeFormatCode;
		markModified();
	}

	/**
	 * This method is used to get the header alignment.
	 * 
	 * @return The header alignment.
	 */
	public Code getHeaderAlignmentCode() {
		return headerAlignmentCode;
	}

	/**
	 * This method is used to set the header alignment.
	 * 
	 * @param headerAlignmentCode
	 *            : The header alignment which is to be set.
	 */
	public void setHeaderAlignmentCode(Code headerAlignmentCode) {
		this.headerAlignmentCode = headerAlignmentCode;
		markModified();
	}

	/**
	 * This method is used to get heading 1.
	 * 
	 * @return Heading 1.
	 */
	public String getHeading1() {
		return heading1;
	}

	/**
	 * This method is used to set Heading 1
	 * 
	 * @param heading1
	 *            : The heading 1 which is to be set.
	 */
	public void setHeading1(String heading1) {
		this.heading1 = heading1;
		markModified();
	}

	/**
	 * This method is used to get heading 2.
	 * 
	 * @return Heading 2.
	 */
	public String getHeading2() {
		return heading2;
	}

	/**
	 * This method is used to set Heading 2
	 * 
	 * @param heading2
	 *            : The heading 2 which is to be set.
	 */
	public void setHeading2(String heading2) {
		this.heading2 = heading2;
		markModified();
	}

	/**
	 * This method is used to get heading 3.
	 * 
	 * @return Heading 3.
	 */
	public String getHeading3() {
		return heading3;
	}

	/**
	 * This method is used to set Heading 3.
	 * 
	 * @param heading3
	 *            : The heading 3 which is to be set.
	 */
	public void setHeading3(String heading3) {
		this.heading3 = heading3;
		markModified();
	}

	/**
	 * This method is used to get the numeric mask.
	 * 
	 * @return The numeric mask.
	 */
	public Code getNumericMaskCode() {
		return numericMaskCode;
	}

	/**
	 * This method is used to set the numeric mask.
	 * 
	 * @param numericMaskCode
	 *            : The numeric mask which is to be set.
	 */
	public void setNumericMaskCode(Code numericMaskCode) {
		this.numericMaskCode = numericMaskCode;
		markModified();
	}

	/**
	 * This method is used to get sort key label.
	 * 
	 * @return sort key label.
	 */
	public String getSortKeyLabel() {
		return sortKeyLabel;
	}

	/**
	 * This method is used to set sort key label.
	 * 
	 * @param sortkeylabel
	 *            . : The sort key label which is to be set.
	 */
	public void setSortKeyLabel(String sortKeyLabel) {
		this.sortKeyLabel = sortKeyLabel;
		markModified();
	}

	/**
	 * This method is used to get default value.
	 * 
	 * @return default value.
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * This method is used to set default value.
	 * 
	 * @param defaultValue
	 *            : The default value which is to be set.
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		markModified();
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
		markModified();
	}

	public void validate() throws SAFRException, DAOException {
		SAFRValidator safrValidator = new SAFRValidator();
		SAFRValidationException safrValidationException = new SAFRValidationException();
		safrValidator.verifyField(this);

		if (!safrValidationException.getErrorMessages().isEmpty())
			throw safrValidationException;
	}

	protected List<Property> getLoadWarningProperties() {
		return loadWarningProperties;
	}

}
