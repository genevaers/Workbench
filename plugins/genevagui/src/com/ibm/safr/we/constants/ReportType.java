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

public enum ReportType {
	ViewProperties("View Properties Report", "View"), 
	ViewColumnPICReport("View Column PIC Report", "ViewColumnPIC"),
	LogicalRecord("Logical Record Report", "LogicalRecord"), 
	LookupPath("Lookup Path Report", "LookupPath"),
	HelpReport("Help Report", "Help"),
	LogicTable("Logic Table", "LT"),
	EnvironmentSecurityById("Environment Security Report", "EnvironmentSecurity"),
	EnvironmentSecurityByName("User Groups Report", "UserGroups"),
	DependencyChecker("Dependency Checker Report", "Dependency Checker");

	private String reportName;
	private String outputFile;

	ReportType(String reportName, String outputFileName) {
		this.reportName = reportName;
		outputFile = outputFileName;
	}

	public String getReportName() {
		return reportName;
	}
	
	public String getOutputFile() {
		return outputFile;
	}

}
