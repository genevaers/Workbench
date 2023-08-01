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


public class ViewSourcesReportQueryBean {
	
	private String viewid;
	private String srcseqnbr;
	private String inlrlfassocid;
	private String extractfiltlogic;
	private String outlfpfassocid;
	private String writeexitid;
	private String writeexitparm;
	private String extractoutputlogic;
	private String logfileid;
	private String logrecid;
	private String lrname;
	private String lfname;
	private String lofid;
	private String pfid;
	private String loname;
	private String pfname;
	private String exitName;
	
	public ViewSourcesReportQueryBean(String viewid, String srcseqnbr, String inlrlfassocid, String extractfiltlogic, String outlfpfassocid,
			String writeexitid, String writeexitparm, String extractoutputlogic, String logfileid, String logrecid, String lrname, String lfname, 
			String lofid, String pfid, String loname, String pfname, String exitName) {
		super();
		this.viewid = viewid;
		this.srcseqnbr = srcseqnbr;
		this.inlrlfassocid = inlrlfassocid;
		this.extractfiltlogic = extractfiltlogic;
		this.outlfpfassocid = outlfpfassocid;
		this.writeexitid = writeexitid;
		this.writeexitparm = writeexitparm;
		this.extractoutputlogic = extractoutputlogic;
		this.logfileid = logfileid;
		this.logrecid = logrecid;
		this.lrname = lrname;
		this.lfname = lfname;
		this.lofid = lofid;
		this.pfid = pfid;
		this.loname = loname;
		this.pfname = pfname;
		this.exitName = exitName;
		
	}
	public String getViewid() {
		return viewid;
	}
	public String getSrcseqnbr() {
		return srcseqnbr;
	}
	public String getInlrlfassocid() {
		return inlrlfassocid;
	}
	public String getExtractfiltlogic() {
		return extractfiltlogic == null ? "" : extractfiltlogic;
	}
	public String getOutlfpfassocid() {
		return outlfpfassocid;
	}
	public String getWriteexitid() {
		return writeexitid;
	}
	public String getWriteexitparm() {
		return writeexitparm;
	}
	public String getExtractoutputlogic() {
		return extractoutputlogic;
	}
	public String getLogfileid() {
		return logfileid;
	}
	public String getLogrecid() {
		return logrecid;
	}
	public String getLrname() {
		return lrname + "[" + logrecid + "]";
	}
	public String getLfname() {
		return lfname + "[" + logfileid + "]";
	}
	public String getOutLf() {
		return lofid.equals("0")? "" : loname + "[" + lofid+ "]";
	}
	public String getOutPF() {
		return pfid.equals("0")? "" : pfname + "[" + pfid+ "]";
	}
	public String getWriteExitName() {
		return exitName != null ? exitName + "[" + writeexitid+ "]" : "";
	}
}
