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


import com.ibm.safr.we.constants.LookupPathSourceFieldType;

public class LookupPathSourceFieldTransfer extends SAFRFieldTransfer {

	private LookupPathSourceFieldType sourceFieldType;
	private Integer keySeqNbr;
	private Integer lookupPathStepId;
	private Integer sourceXLRFLDId;
	private Integer sourceXLRFileId;
	private Integer sourceJoinId;
	private String symbolicName;
	private String sourceValue;

	public void setSourceFieldType(LookupPathSourceFieldType sourceFieldType) {
		this.sourceFieldType = sourceFieldType;
	}

	public LookupPathSourceFieldType getSourceFieldType() {
		return sourceFieldType;
	}

	public void setKeySeqNbr(Integer keySeqNbr) {
		this.keySeqNbr = keySeqNbr;
	}

	public void setLookupPathStepId(Integer lookupPathStepId) {
		this.lookupPathStepId = lookupPathStepId;
	}

	public Integer getLookupPathStepId() {
		return lookupPathStepId;
	}

	public void setSourceXLRFLDId(Integer sourceXLRFLDId) {
		this.sourceXLRFLDId = sourceXLRFLDId;
	}

	public Integer getSourceXLRFLDId() {
		return sourceXLRFLDId;
	}

	public void setSourceXLRFileId(Integer sourceXLRFileId) {
		this.sourceXLRFileId = sourceXLRFileId;
	}

	public Integer getSourceXLRFileId() {
		return sourceXLRFileId;
	}

	public Integer getKeySeqNbr() {
		return keySeqNbr;
	}

	public Integer getSourceJoinId() {
		return sourceJoinId;
	}

	public void setSourceJoinId(Integer sourceJoinId) {
		this.sourceJoinId = sourceJoinId;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public String getSourceValue() {
		return sourceValue;
	}

	public void setSourceValue(String sourceValue) {
		this.sourceValue = sourceValue;
	}

}
