package com.ibm.safr.we.model.query;

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


import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.ModelUtilities;

public class OldCompilerJoinQueryBean {
	
    private String joinName;
    private Integer joinID;
    private String fieldName;
    private Integer fieldId;
    private Code format;
    private boolean signed;
    private Integer length;
    private Integer decimalCount;
    private Code content;
    private Integer rounding;
    private Code justify;
    private String mask;
    private Integer startPosition;
    private Integer ordPosition;
    
	public OldCompilerJoinQueryBean(String joinName, Integer joinID,
			String fieldName, Integer fieldId, String format, boolean signed, 
			Integer length, Integer decimalCount, String content, 
			Integer rounding, String justify, String mask,
			Integer startPosition, Integer ordPosition) {
		super();
		this.joinName = joinName;
		this.joinID = joinID;
		this.fieldName = fieldName;
		this.fieldId = fieldId;
		this.format = ModelUtilities.getCodeFromKey(CodeCategories.DATATYPE, format);
		this.signed = signed;
		this.length = length;
		this.decimalCount = decimalCount;
		this.content = ModelUtilities.getCodeFromKey(CodeCategories.FLDCONTENT, content);
		this.rounding = rounding;
		this.justify = ModelUtilities.getCodeFromKey(CodeCategories.JUSTIFY, justify);
		this.mask = mask;
		this.startPosition = startPosition;
		this.ordPosition = ordPosition;
	}

	public String getJoinName() {
		return joinName;
	}

	public void setJoinName(String joinName) {
		this.joinName = joinName;
	}

	public Integer getJoinID() {
		return joinID;
	}

	public void setJoinID(Integer joinID) {
		this.joinID = joinID;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Integer getFieldId() {
		return fieldId;
	}

	public void setFieldId(Integer fieldId) {
		this.fieldId = fieldId;
	}

	public Code getFormat() {
		return format;
	}

	public void setFormat(Code format) {
		this.format = format;
	}
	
	public boolean isSigned() {
		return signed;
	}

	public void setSigned(boolean signed) {
		this.signed = signed;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getDecimalCount() {
		return decimalCount;
	}

	public void setDecimalCount(Integer decimalCount) {
		this.decimalCount = decimalCount;
	}

	public Code getContent() {
		return content;
	}

	public void setContent(Code content) {
		this.content = content;
	}

	public Integer getRounding() {
		return rounding;
	}

	public void setRounding(Integer rounding) {
		this.rounding = rounding;
	}

	public Code getJustify() {
		return justify;
	}

	public void setJustify(Code justify) {
		this.justify = justify;
	}

	public String getMask() {
		return mask;
	}

	public void setMask(String mask) {
		this.mask = mask;
	}

	public Integer getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(Integer startPosition) {
		this.startPosition = startPosition;
	}

	public Integer getOrdPosition() {
		return ordPosition;
	}

	public void setOrdPosition(Integer ordPosition) {
		this.ordPosition = ordPosition;
	}
    
    
}
