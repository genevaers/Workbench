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


public class LookupPrimaryKeysBean {
    private int lookupid; 
    private int stepseqnbr; 
    private String logrecid;
    private String lrfieldid;
    private String fldseqnbr;
    private String fldname;
    private String fldfmtcd;
    private String maxlen;
    private String decimalcnt;
    private String fldcontentcd;
    
	public LookupPrimaryKeysBean(int lookupid, int stepseqnbr, String logrecid, String lrfieldid,
			String fldseqnbr, String fldname, String fldfmtcd, String maxlen, String decimalcnt, String fldcontentcd) {
		super();
		this.lookupid = lookupid;
		this.stepseqnbr = stepseqnbr;
		this.logrecid = logrecid;
		this.lrfieldid = lrfieldid;
		this.fldseqnbr = fldseqnbr;
		this.fldname = fldname;
		this.fldfmtcd = fldfmtcd;
		this.maxlen = maxlen;
		this.decimalcnt = decimalcnt;
		this.fldcontentcd = fldcontentcd;
	}
	public int getLookupid() {
		return lookupid;
	}
	public int getStepseqnbr() {
		return stepseqnbr;
	}
	public String getLogrecid() {
		return logrecid;
	}
	public String getLrfieldid() {
		return lrfieldid;
	}
	public String getFldseqnbr() {
		return fldseqnbr;
	}
	public String getFldname() {
		return fldname;
	}
	public String getFldfmtcd() {
		return fldfmtcd;
	}
	public String getMaxlen() {
		return maxlen;
	}
	public String getDecimalcnt() {
		return decimalcnt;
	}
	public String getFldcontentcd() {
		return fldcontentcd;
	}
    
}
