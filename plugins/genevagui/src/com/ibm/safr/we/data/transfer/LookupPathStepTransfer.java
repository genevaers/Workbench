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


public class LookupPathStepTransfer extends SAFREnvironmentalComponentTransfer {

	private Integer sequenceNumber;
	private Integer sourceLRId;
	private Integer targetXLRFileId;
	private Integer joinId;

	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public void setSourceLRId(Integer sourceLRId) {
		this.sourceLRId = sourceLRId;
	}

	public Integer getSourceLRId() {
		return sourceLRId;
	}

	public void setTargetXLRFileId(Integer targetXLRFileId) {
		this.targetXLRFileId = targetXLRFileId;
	}

	public Integer getTargetXLRFileId() {
		return targetXLRFileId;
	}

	public Integer getJoinId() {
		return joinId;
	}

	public void setJoinId(Integer joinId) {
		this.joinId = joinId;
	}

}
