package com.ibm.safr.we.model.query;

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


public class ViewColumnPICQueryBean  {
	
	private String environid;
	private String viewid;
	private String viewname;
	private String columnnumber;
	private String fldfmtcd;
	private int maxlen;
	private boolean signedind;
	private int decimalcnt;
	private String hdrline1;
	private String hdrline2;
	private String hdrline3;
	
	public ViewColumnPICQueryBean(String environid, String viewid, String viewname, String columnnumber, String fldfmtcd, int maxlen,
			boolean signedind, int decimalcnt, String hdrline1, String hdrline2, String hdrline3) {
		super();
		this.environid = environid;
		this.viewid = viewid;
		this.viewname = viewname;
		this.columnnumber = columnnumber;
		this.fldfmtcd = fldfmtcd;
		this.maxlen = maxlen;
		this.signedind = signedind;
		this.decimalcnt = decimalcnt;
		this.hdrline1 = hdrline1;
		this.hdrline2 = hdrline2;
		this.hdrline3 = hdrline3;
	}

	public String getEnvironid() {
		return environid;
	}

	public String getViewid() {
		return viewid;
	}
	
	public String getViewname() {
		return viewname;
	}

	public String getColumnnumber() {
		return columnnumber;
	}

	public String getFldfmtcd() {
		return fldfmtcd;
	}

	public int getMaxlen() {
		return maxlen;
	}

	public boolean getSignedind() {
		return signedind;
	}

	public int getDecimalcnt() {
		return decimalcnt;
	}

	public String getHdrline1() {
		return hdrline1;
	}

	public String getHdrline2() {
		return hdrline2;
	}

	public String getHdrline3() {
		return hdrline3;
	}
	
}
