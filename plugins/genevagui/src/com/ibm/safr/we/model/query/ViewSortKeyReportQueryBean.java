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


public class ViewSortKeyReportQueryBean {

	private String viewid;
	private String viewcolumnid;
	private String srcseqnbr;
	private String keyseqnbr;
	private String sortseqcd;
	private String sortbrkind;
	private String pagebrkind;
	private String sortkeydisplaycd;
	private String sortkeylabel;
	private String skfldfmtcd;
	private String sksigned;
	private String skstartpos;
	private String skfldlen;
	private String skdecimalcnt;
	private String skfldcontentcd;
	private String sorttitlelookupid;
	private String lkName;
	private String srcsktfieldid;
	private String sksktfieldid;
	private String sktfieldname;
	private String effdatevalue;
	private String effdatetype;
	private String effdatelrfieldid;
	private String subtlabel;
	
	
	public ViewSortKeyReportQueryBean(String viewid, String viewcolumnid, String srcseqnbr, String keyseqnbr, String sortseqcd, String sortbrkind,
			String pagebrkind, String sortkeydisplaycd, String sortkeylabel, String subtlabel, String skfldfmtcd, String sksigned, String skstartpos, String skfldlen,
			String skdecimalcnt, String skfldcontentcd, String sorttitlelookupid, String lkName, String srcsktfieldid, String sksktfieldid,
			String sktfieldname, String effdatevalue, String effdatetype, String effdatelrfieldid) {
		super();
		this.viewid = viewid;
		this.viewcolumnid = viewcolumnid;
		this.srcseqnbr = srcseqnbr;
		this.keyseqnbr = keyseqnbr;
		this.sortseqcd = sortseqcd;
		this.sortbrkind = sortbrkind;
		this.pagebrkind = pagebrkind;
		this.sortkeydisplaycd = sortkeydisplaycd;
		this.sortkeylabel = sortkeylabel;
		this.subtlabel = subtlabel;
		this.skfldfmtcd = skfldfmtcd;
		this.sksigned = sksigned;
		this.skstartpos = skstartpos;
		this.skfldlen = skfldlen;
		this.skdecimalcnt = skdecimalcnt;
		this.skfldcontentcd = skfldcontentcd;
		this.sorttitlelookupid = sorttitlelookupid;
		this.lkName = lkName;
		this.srcsktfieldid = srcsktfieldid;
		this.sksktfieldid = sksktfieldid;
		this.sktfieldname = sktfieldname;
		this.effdatevalue = effdatevalue;
		this.effdatetype = effdatetype;
		this.effdatelrfieldid = effdatelrfieldid;
	}


	public String getViewid() {
		return viewid;
	}


	public String getViewcolumnid() {
		return viewcolumnid;
	}


	public String getSrcseqnbr() {
		return srcseqnbr;
	}


	public String getKeyseqnbr() {
		return keyseqnbr;
	}


	public String getSortseqcd() {
		return sortseqcd;
	}


	public String getSortbrkind() {
		return sortbrkind;
	}


	public String getPagebrkind() {
		return pagebrkind;
	}


	public String getSortkeydisplaycd() {
		return sortkeydisplaycd;
	}


	public String getSortkeylabel() {
		return sortkeylabel;
	}

	public String getSubtlabel() {
		return subtlabel;
	}


	public String getSkfldfmtcd() {
		return skfldfmtcd;
	}


	public String getSksigned() {
		return sksigned == null ? "0" : sksigned;
	}


	public String getSkstartpos() {
		return skstartpos;
	}


	public String getSkfldlen() {
		return skfldlen;
	}


	public String getSkdecimalcnt() {
		return skdecimalcnt;
	}


	public String getSkfldcontentcd() {
		return skfldcontentcd;
	}


	public String getSorttitlelookupid() {
		return sorttitlelookupid;
	}


	public String getLkName() {
		return lkName;
	}


	public String getSrcsktfieldid() {
		return srcsktfieldid;
	}


	public String getSksktfieldid() {
		return sksktfieldid;
	}


	public String getSktfieldname() {
		return sktfieldname;
	}


	public String getEffdatevalue() {
		return effdatevalue;
	}


	public String getEffdatetype() {
		return effdatetype;
	}


	public String getEffdatelrfieldid() {
		return effdatelrfieldid;
	}
	
}
