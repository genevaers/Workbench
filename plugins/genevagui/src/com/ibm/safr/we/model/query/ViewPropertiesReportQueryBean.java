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


import java.util.Date;

import com.ibm.safr.we.constants.EditRights;

public class ViewPropertiesReportQueryBean  {
	
	private String viewid;
	private String name;
	private String viewtypecd;
	private String extractfilepartnbr;
	private String outputmediacd;
	private String pagesize;
	private String linesize;
	private String zerosuppressind;
	private String extractmaxreccnt;
	private String extractsummaryind;
	private String extractsummarybuf;
	private String outputmaxreccnt;
	private String controlrecid;
	private String controlName;
	private String formatexitid;
	private String frmtexit;
	private String formatexitstartup;
	private String fileflddelimcd;
	private String filestrdelimcd;
	private String delimheaderrowind;
	private String formatfiltlogic;
	private String compiler;
	private String lastacttimestamp;
	private String lastactuserid;
	private String lfname;
	private String pfname;
	
	public ViewPropertiesReportQueryBean(String viewid, String name, String viewtypecd, String extractfilepartnbr, String outputmediacd,
			String pagesize, String linesize, String zerosuppressind, String extractmaxreccnt, String extractsummaryind, String extractsummarybuf,
			String outputmaxreccnt, String controlrecid, String controlName, String formatexitid, String frmtexit, String formatexitstartup,
			String fileflddelimcd, String filestrdelimcd, String delimheaderrowind, String formatfiltlogic, String compiler, String lastacttimestamp,
			String lastactuserid, String lfname, String pfname) {
		super();
		this.viewid = viewid;
		this.name = name;
		this.viewtypecd = viewtypecd;
		this.extractfilepartnbr = extractfilepartnbr;
		this.outputmediacd = outputmediacd;
		this.pagesize = pagesize;
		this.linesize = linesize;
		this.zerosuppressind = zerosuppressind;
		this.extractmaxreccnt = extractmaxreccnt;
		this.extractsummaryind = extractsummaryind;
		this.extractsummarybuf = extractsummarybuf;
		this.outputmaxreccnt = outputmaxreccnt;
		this.controlrecid = controlrecid;
		this.controlName = controlName;
		this.formatexitid = formatexitid;
		this.frmtexit = frmtexit;
		this.formatexitstartup = formatexitstartup;
		this.fileflddelimcd = fileflddelimcd;
		this.filestrdelimcd = filestrdelimcd;
		this.delimheaderrowind = delimheaderrowind;
		this.formatfiltlogic = formatfiltlogic;
		this.compiler = compiler;
		this.lastacttimestamp = lastacttimestamp;
		this.lastactuserid = lastactuserid;
	}

	public String getViewid() {
		return viewid;
	}

	public String getName() {
		return name;
	}

	public String getViewtypecd() {
		return viewtypecd;
	}

	public String getExtractfilepartnbr() {
		return extractfilepartnbr;
	}

	public String getOutputmediacd() {
		return outputmediacd;
	}

	public String getPagesize() {
		return pagesize;
	}

	public String getLinesize() {
		return linesize;
	}

	public String getZerosuppressind() {
		return zerosuppressind;
	}

	public String getExtractmaxreccnt() {
		return extractmaxreccnt;
	}

	public String getExtractsummaryind() {
		return extractsummaryind;
	}

	public String getExtractsummarybuf() {
		return extractsummarybuf;
	}

	public String getOutputmaxreccnt() {
		return outputmaxreccnt;
	}

	public String getControlrecid() {
		return controlrecid;
	}

	public String getControlName() {
		return controlName;
	}

	public String getFormatexitid() {
		return formatexitid == null ? "0" : formatexitid;
	}

	public String getFrmtexit() {
		return frmtexit == null ? "" : frmtexit;
	}

	public String getFormatexitstartup() {
		return formatexitstartup;
	}

	public String getFileflddelimcd() {
		return fileflddelimcd;
	}

	public String getFilestrdelimcd() {
		return filestrdelimcd;
	}

	public String getDelimheaderrowind() {
		return delimheaderrowind;
	}

	public String getFormatfiltlogic() {
		return formatfiltlogic;
	}

	public String getCompiler() {
		return compiler;
	}

	public String getLastacttimestamp() {
		return lastacttimestamp;
	}

	public String getLastactuserid() {
		return lastactuserid;
	}

	public String getLfname() {
		return lfname;
	}

	public String getPfname() {
		return pfname;
	}
	
}
