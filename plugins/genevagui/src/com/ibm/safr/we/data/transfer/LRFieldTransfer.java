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


public class LRFieldTransfer extends SAFRFieldTransfer {

	private Integer lrId;
	private String dbmsColName;
	private boolean redefinesInd;
	private Integer redefine;
	private Integer fixedStartPos;
	private Integer ordinalPos;
	private Integer ordinalOffset;
	private boolean effStartDate;
	private boolean effEndDate;
	private Integer pkeySeqNo;

	/**
	 * @return the lrId
	 */
	public Integer getLrId() {
		return lrId;
	}

	/**
	 * @param lrId
	 *            the lrId to set
	 */
	public void setLrId(Integer lrId) {
		this.lrId = lrId;
	}

	/**
	 * @return the dbmsColName
	 */
	public String getDbmsColName() {
		return dbmsColName;
	}

	/**
	 * @param dbmsColName
	 *            the dbmsColName to set
	 */
	public void setDbmsColName(String dbmsColName) {
		this.dbmsColName = dbmsColName;
	}

    public Integer getRedefine() {
        return redefine;
    }

    public void setRedefine(Integer redefine) {
        this.redefine = redefine;
    }
    
	/**
	 * @return the fixedStartPos
	 */
	public Integer getFixedStartPos() {
		return fixedStartPos;
	}

	/**
	 * @param fixedStartPos
	 *            the fixedStartPos to set
	 */
	public void setFixedStartPos(Integer fixedStartPos) {
		this.fixedStartPos = fixedStartPos;
	}

	/**
	 * @return the ordinalPos
	 */
	public Integer getOrdinalPos() {
		return ordinalPos;
	}

	/**
	 * @param ordinalPos
	 *            the ordinalPos to set
	 */
	public void setOrdinalPos(Integer ordinalPos) {
		this.ordinalPos = ordinalPos;
	}

	/**
	 * @return the ordinalOffset
	 */
	public Integer getOrdinalOffset() {
		return ordinalOffset;
	}

	/**
	 * @param ordinalOffset
	 *            the ordinalOffset to set
	 */
	public void setOrdinalOffset(Integer ordinalOffset) {
		this.ordinalOffset = ordinalOffset;
	}

	/**
	 * @param effStartDate
	 *            the effStartDate to set
	 */
	public void setEffStartDate(boolean effStartDate) {
		this.effStartDate = effStartDate;
	}

	/**
	 * @return the effStartDate
	 */
	public boolean isEffStartDate() {
		return effStartDate;
	}

	/**
	 * @param effEndDate
	 *            the effEndDate to set
	 */
	public void setEffEndDate(boolean effEndDate) {
		this.effEndDate = effEndDate;
	}

	/**
	 * @return the effEndDate
	 */
	public boolean isEffEndDate() {
		return effEndDate;
	}

	/**
	 * @param pkeySeqNo
	 *            the pkeySeqNo to set
	 */
	public void setPkeySeqNo(Integer pkeySeqNo) {
		this.pkeySeqNo = pkeySeqNo;
	}

	/**
	 * @return the pkeySeqNo
	 */
	public Integer getPkeySeqNo() {
		return pkeySeqNo;
	}

    public boolean isRedefinesInd() {
        return redefinesInd;
    }

    public void setRedefinesInd(boolean redefinesInd) {
        this.redefinesInd = redefinesInd;
    }

}
