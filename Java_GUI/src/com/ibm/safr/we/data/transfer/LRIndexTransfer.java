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


public class LRIndexTransfer extends SAFRComponentTransfer {

	/*
	 * Fields for table LRINDEX.
	 */
	private Integer environmentId;
	//private Integer lrIndexId;
	private Integer lrId;
	private Integer effectiveStartDateLRFieldId;
	private Integer effectiveEndDateLRFieldId;

	public Integer getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(Integer environmentId) {
		this.environmentId = environmentId;
	}

//	public Integer getLrIndexId() {
//		return lrIndexId;
//	}
//
//	public void setLrIndexId(Integer lrIndexId) {
//		this.lrIndexId = lrIndexId;
//	}

	public Integer getLrId() {
		return lrId;
	}

	public void setLrId(Integer lrId) {
		this.lrId = lrId;
	}

	public Integer getEffectiveStartDateLRFieldId() {
		return effectiveStartDateLRFieldId;
	}

	public void setEffectiveStartDateLRFieldId(
			Integer effectiveStartDateLRFieldId) {
		this.effectiveStartDateLRFieldId = effectiveStartDateLRFieldId;
	}

	public Integer getEffectiveEndDateLRFieldId() {
		return effectiveEndDateLRFieldId;
	}

	public void setEffectiveEndDateLRFieldId(Integer effectiveEndDateLRFieldId) {
		this.effectiveEndDateLRFieldId = effectiveEndDateLRFieldId;
	}

}
