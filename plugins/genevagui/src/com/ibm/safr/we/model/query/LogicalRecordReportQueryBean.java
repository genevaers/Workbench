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


public class LogicalRecordReportQueryBean {
	

	public String environid; 
	public String logrecid; 
	public String lrname; 
	public String lrtypecd; 
	public String lrstatuscd; 
	public String lookupexitid;
	public String lookupexitstartup; 
	public String lrfieldid; 
	public String fieldname; 
	public String dbmscolname; 
	public String fixedstartpos;
	public String ordinalpos; 
	public String redefinedfieldid;
	public String fldfmtcd; 
	public String signedind; 
	public String maxlen; 
	public String decimalcnt;
	public String rounding; 
	public String fldcontentcd; 
	public String justifycd; 
	public String subtlabel; 
	public String sortkeylabel; 
	public String inputmask; 
	public String effdatestartfldid; 
	public String effdateendfldid;
	public String fldseqnbr;
	private String exname;
	private String primary;

	public LogicalRecordReportQueryBean(String environid, String logrecid, String lrname, String lrtypecd,
			String lrstatuscd, String lookupexitid, String exname, String lookupexitstartup, String lrfieldid, 
			String fieldname, String primary,
			String dbmscolname, String fixedstartpos, String ordinalpos, String redefinedfieldid, String fldfmtcd,
			String signedind, String maxlen, String decimalcnt, String rounding, String fldcontentcd, String justifycd,
			String subtlabel, String sortkeylabel, String inputmask, String effdatestartfldid, String effdateendfldid,
			String fldseqnbr) {
		super();
		this.environid = environid;
		this.logrecid = logrecid;
		this.lrname = lrname;
		this.lrtypecd = lrtypecd;
		this.lrstatuscd = lrstatuscd;
		this.lookupexitid = lookupexitid;
		this.exname = exname;
		this.lookupexitstartup = lookupexitstartup;
		this.lrfieldid = lrfieldid;
		this.fieldname = fieldname;
		this.primary = primary;
		this.dbmscolname = dbmscolname;
		this.fixedstartpos = fixedstartpos;
		this.ordinalpos = ordinalpos;
		this.redefinedfieldid = redefinedfieldid;
		this.fldfmtcd = fldfmtcd;
		this.signedind = signedind;
		this.maxlen = maxlen;
		this.decimalcnt = decimalcnt;
		this.rounding = rounding;
		this.fldcontentcd = fldcontentcd;
		this.justifycd = justifycd;
		this.subtlabel = subtlabel;
		this.sortkeylabel = sortkeylabel;
		this.inputmask = inputmask;
		this.effdatestartfldid = effdatestartfldid;
		this.effdateendfldid = effdateendfldid;
		this.fldseqnbr = fldseqnbr;
	}
	
	public String getEnvironid() {
		return environid;
	}

	public String getLogrecid() {
		return logrecid;
	}

	public String getLrname() {
		return lrname;
	}

	public String getLrtypecd() {
		return lrtypecd;
	}

	public String getLrstatuscd() {
		return lrstatuscd;
	}

	public String getLookupexitid() {
		return lookupexitid == null ? "0" : lookupexitid;
	}
	
	public String getExitName() {
		return exname;
	}

	public String getLookupexitstartup() {
		return lookupexitstartup;
	}

	public String getLrfieldid() {
		return lrfieldid;
	}

	public String getFieldname() {
		return fieldname;
	}
	
	public String getPrimary() {
		return primary;
	}

	public String getDbmscolname() {
		return dbmscolname;
	}

	public String getFixedstartpos() {
		return fixedstartpos;
	}

	public String getOrdinalpos() {
		return ordinalpos;
	}

	public String getRedefinedfieldid() {
		return redefinedfieldid;
	}

	public String getFldfmtcd() {
		return fldfmtcd;
	}

	public String getSignedind() {
		return signedind;
	}

	public String getMaxlen() {
		return maxlen;
	}

	public String getDecimalcnt() {
		return decimalcnt;
	}

	public String getRounding() {
		return rounding;
	}

	public String getFldcontentcd() {
		return fldcontentcd;
	}

	public String getJustifycd() {
		return justifycd;
	}

	public String getSubtlabel() {
		return subtlabel;
	}

	public String getSortkeylabel() {
		return sortkeylabel;
	}

	public String getInputmask() {
		return inputmask;
	}

	public String getEffdatestartfldid() {
		return effdatestartfldid;
	}

	public String getEffdateendfldid() {
		return effdateendfldid;
	}

	public String getFldseqnbr() {
		return fldseqnbr;
	}

}
