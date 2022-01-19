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


public class ViewColumnSourceTransfer extends
		SAFREnvironmentalComponentTransfer {

	private Integer viewId; // VIEWID
	private Integer viewColumnId; // VIEWCOLUMNID
	private Integer viewSourceId; // VIEWSOURCEID
	private Integer sourceTypeId; // SOURCETYPEID
	private Integer sourceLRFieldId; // LRFIELDID
	private Integer sortKeyTitleLRFieldId; // SORTTITLELRFIELDID
	private Integer lookupPathId; // LOOKUPID
	private String sourceValue; // SRCVAL
	private String effectiveDateTypeCode; // EFFDATTYPE
	private String effectiveDateValue; // EFFDATVALUE
	private Integer effectiveDateLRFieldId;// EFFDATELRFIELDID
	private Integer sortKeyTitleLookupPathId; // SORTTITLEPATHID
	private String extractColumnLogic;

	public Integer getSortKeyTitleLookupPathId() {
		return sortKeyTitleLookupPathId;
	}

	public void setSortKeyTitleLookupPathId(Integer sortKeyTitleLookupPathId) {
		this.sortKeyTitleLookupPathId = sortKeyTitleLookupPathId;
	}

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

	public Integer getViewSourceId() {
		return viewSourceId;
	}

	public void setViewSourceId(Integer viewSourceId) {
		this.viewSourceId = viewSourceId;
	}

	public Integer getSourceTypeId() {
		return sourceTypeId;
	}

	public void setSourceTypeId(Integer sourceTypeId) {
		this.sourceTypeId = sourceTypeId;
	}

	public Integer getSourceLRFieldId() {
		return sourceLRFieldId;
	}

	public void setSourceLRFieldId(Integer sourceLRFieldId) {
		this.sourceLRFieldId = sourceLRFieldId;
	}

	public Integer getSortKeyTitleLRFieldId() {
		return sortKeyTitleLRFieldId;
	}

	public void setSortKeyTitleLRFieldId(Integer sortKeyTitleLRFieldId) {
		this.sortKeyTitleLRFieldId = sortKeyTitleLRFieldId;
	}

	public Integer getLookupPathId() {
		return lookupPathId;
	}

	public void setLookupPathId(Integer lookupPathId) {
		this.lookupPathId = lookupPathId;
	}

	public String getSourceValue() {
		return sourceValue;
	}

	public void setSourceValue(String sourceValue) {
		this.sourceValue = sourceValue;
	}

	public String getEffectiveDateTypeCode() {
		return effectiveDateTypeCode;
	}

	public void setEffectiveDateTypeCode(String effectiveDateTypeCode) {
		this.effectiveDateTypeCode = effectiveDateTypeCode;
	}

	public String getEffectiveDateValue() {
		return effectiveDateValue;
	}

	public void setEffectiveDateValue(String effectiveDateValue) {
		this.effectiveDateValue = effectiveDateValue;
	}

	public Integer getEffectiveDateLRFieldId() {
		return effectiveDateLRFieldId;
	}

	public void setEffectiveDateLRFieldId(Integer effectiveDateLRFieldId) {
		this.effectiveDateLRFieldId = effectiveDateLRFieldId;
	}

	public String getExtractColumnLogic() {
		return extractColumnLogic;
	}

	public void setExtractColumnLogic(String extractColumnLogic) {
		this.extractColumnLogic = extractColumnLogic;
	}

}
