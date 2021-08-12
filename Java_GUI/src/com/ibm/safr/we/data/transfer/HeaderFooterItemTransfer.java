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


public class HeaderFooterItemTransfer extends
		SAFREnvironmentalComponentTransfer {

	private Integer viewId;
	private String stdFuctionCode;
	private String justifyCode;
	private Integer rowNumber;
	private Integer colNumber;
	private Integer length;
	private String itemText;
	private Boolean header;

	public Integer getViewId() {
		return viewId;
	}

	public void setViewId(Integer viewId) {
		this.viewId = viewId;
	}

	public String getStdFuctionCode() {
		return stdFuctionCode;
	}

	public void setStdFuctionCode(String stdFuctionCode) {
		this.stdFuctionCode = stdFuctionCode;
	}

	public String getJustifyCode() {
		return justifyCode;
	}

	public void setJustifyCode(String justifyCode) {
		this.justifyCode = justifyCode;
	}

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	public Integer getColNumber() {
		return colNumber;
	}

	public void setColNumber(Integer colNumber) {
		this.colNumber = colNumber;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public String getItemText() {
		return itemText;
	}

	public void setItemText(String itemText) {
		this.itemText = itemText;
	}

	public Boolean isHeader() {
		return header;
	}

	public void setHeader(Boolean header) {
		this.header = header;
	}

	public Integer getHeaderFooterId() {
		return getId();
	}

	public void setHeaderFooterId(Integer headerFooterId) {
		setId(headerFooterId);
	}

}
