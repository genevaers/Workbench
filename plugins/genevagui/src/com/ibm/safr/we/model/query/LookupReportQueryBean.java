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












public class LookupReportQueryBean {

    private String srclrid;
    private int lookupid;
    private String name;
    private int stepseqnbr;
    private String stepsrclrid;
    private String stepsrclr;
    private int keyseqnbr;
    private String fldtype;
    private String steptarglr;
    private String keylrid;
    private String keylr;
    private String lrfieldid;
    private String fldname;
    private String valuefmtcd;
    private String signed;
    private String valuelen;
    private String decimalcnt;
    private String fldcontentcd;
    private String rounding;
    private String justifycd;
    private String symbolicname;
    private String value;
    private String createdtimestamp;
    private String createduserid;
    private String lastmodtimestamp;
    private String lastmoduserid;
    private String targlrid;
    private String targlfld;
    private String steptarglf;
    private String keylf;
    private String lookupexitid;
    private String exitname;
    private String lookupexitstartup;
	public LookupReportQueryBean(String srclrid, int lookupid, String name, int stepseqnbr, String stepsrclrid, String stepsrclr,
			int keyseqnbr, String fldtype, String steptarglr, String keylrid, String keylr, String lrfieldid, String fldname,
			String valuefmtcd, String signed, String valuelen, String decimalcnt, String fldcontentcd, String rounding,
			String justifycd, String symbolicname, String value, String createdtimestamp, String createduserid,
			String lastmodtimestamp, String lastmoduserid, String targlrid, String targlfld, String steptarglf,
			String keylf, String lookupexitid, String exitname, String lookupexitstartup) {
		super();
		this.srclrid = srclrid;
		this.lookupid = lookupid;
		this.name = name;
		this.stepseqnbr = stepseqnbr;
		this.stepsrclrid = stepsrclrid;
		this.stepsrclr = stepsrclr;
		this.keyseqnbr = keyseqnbr;
		this.fldtype = fldtype;
		this.steptarglr = steptarglr;
		this.keylrid = keylrid;
		this.keylr = keylr;
		this.lrfieldid = lrfieldid;
		this.fldname = fldname;
		this.valuefmtcd = valuefmtcd;
		this.signed = signed;
		this.valuelen = valuelen;
		this.decimalcnt = decimalcnt;
		this.fldcontentcd = fldcontentcd;
		this.rounding = rounding;
		this.justifycd = justifycd;
		this.symbolicname = symbolicname;
		this.value = value;
		this.createdtimestamp = createdtimestamp;
		this.createduserid = createduserid;
		this.lastmodtimestamp = lastmodtimestamp;
		this.lastmoduserid = lastmoduserid;
		this.targlrid = targlrid;
		this.targlfld = targlfld;
		this.steptarglf = steptarglf;
		this.keylf = keylf;
		this.lookupexitid = lookupexitid;
		this.exitname = exitname;
		this.lookupexitstartup = lookupexitstartup;
	}
	public String getSrclrid() {
		return srclrid;
	}
	public int getLookupid() {
		return lookupid;
	}
	public String getName() {
		return name;
	}
	public int getStepseqnbr() {
		return stepseqnbr;
	}
	public String getStepsrclrid() {
		return stepsrclrid;
	}
	public String getStepsrclr() {
		return stepsrclr;
	}
	public int getKeyseqnbr() {
		return keyseqnbr;
	}
	public String getFldtype() {
		return fldtype;
	}
	public String getSteptarglr() {
		return steptarglr;
	}
	public String getKeylrid() {
		return keylrid;
	}
	public String getKeylr() {
		return keylr;
	}
	public String getLrfieldid() {
		return lrfieldid;
	}
	public String getFldname() {
		return fldname;
	}
	public String getValuefmtcd() {
		return valuefmtcd;
	}
	public String getSigned() {
		return signed;
	}
	public String getValuelen() {
		return valuelen;
	}
	public String getDecimalcnt() {
		return decimalcnt;
	}
	public String getFldcontentcd() {
		return fldcontentcd;
	}
	public String getRounding() {
		return rounding;
	}
	public String getJustifycd() {
		return justifycd;
	}
	public String getSymbolicname() {
		return symbolicname;
	}
	public String getValue() {
		return value;
	}
	public String getCreatedtimestamp() {
		return createdtimestamp;
	}
	public String getCreateduserid() {
		return createduserid;
	}
	public String getLastmodtimestamp() {
		return lastmodtimestamp;
	}
	public String getLastmoduserid() {
		return lastmoduserid;
	}
	public String getTarglrid() {
		return targlrid;
	}
	public String getTarglfld() {
		return targlfld;
	}
	public String getSteptarglf() {
		return steptarglf;
	}
	public String getKeylf() {
		return keylf;
	}
	public String getLookupexitid() {
		return lookupexitid;
	}
	public String getExitname() {
		return exitname;
	}
	public String getLookupexitstartup() {
		return lookupexitstartup;
	}
    
    
}
