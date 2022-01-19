package com.ibm.safr.we.constants;

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


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ibm.safr.we.utilities.ProfileLocation;

public enum ReportType {
	ViewProperties("ViewPropertiesReportDesign.rptdesign",
			"View Properties Report", "ViewProperties"), ViewColumnPICReport(
			"ViewColumnPICReportDesign.rptdesign", "View Column PIC Report",
			"ViewColumnPIC"), LogicalRecord(
			"LRPropertiesReportDesign.rptdesign", "Logical Record Report",
			"LogicalRecord"), LookupPath("LookupPathReportDesign.rptdesign",
			"Lookup Path Report", "LookupPath"), ViewColumnReport(
			"ViewColumnReportDesign.rptdesign", "View Column Report",
			"ViewColumn"), EnvironmentSecurityById(
			"EnvironmentSecurityReportDesign.rptdesign",
			"Environment Security Report by ID", "EnvironmentSecurity"), EnvironmentSecurityByName(
			"EnvironmentSecurityReportDesign.rptdesign",
			"Environment Security Report by Name", "EnvironmentSecurity"), DependencyChecker(
			"DependencyCheckerReportDesign.rptdesign",
			"Dependency Checker Report", "Dependency Checker");

	private String fileName;
	private String reportName;
	private String outputFile;

	ReportType(String filePath, String reportName, String outputFileName) {
		this.fileName = "/reportDesignFiles/" + filePath;
		this.reportName = reportName;
		outputFile = outputFileName;
	}

	public String getFileName() {
		return fileName;
	}

	public String getReportName() {
		return reportName;
	}

	public String getOutputFile() {
		String outputPath = ProfileLocation.getProfileLocation().getLocalProfile() + "pdf\\";
		File outputDirs = new File(outputPath);
		if (!outputDirs.exists()) {
			outputDirs.mkdirs();
		}
		Date CurrDate = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyMMdd_HHmmss");
		String timeStamp = df.format(CurrDate);
		return outputPath + outputFile + "_" + timeStamp + ".pdf";
	}
}
