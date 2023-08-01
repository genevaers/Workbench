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


public class EnvironmentSecurityReportBean {
	
	private String environid;
	private String envname;
	private String groupid;
	private String grpname;
	private String envrole;
	private String userid;
	private String firstname;
	private String lastname;
	
	public EnvironmentSecurityReportBean(String environid, String envname, String groupid, String grpname, String envrole, String userid,
			String firstname, String lastname) {
		super();
		this.environid = environid;
		this.envname = envname;
		this.groupid = groupid;
		this.grpname = grpname;
		this.envrole = envrole;
		this.userid = userid;
		this.firstname = firstname;
		this.lastname = lastname;
	}
	public String getEnvironid() {
		return environid;
	}
	public String getEnvname() {
		return envname;
	}
	public String getGroupid() {
		return groupid;
	}
	public String getGrpname() {
		return grpname;
	}
	public String getEnvrole() {
		return envrole;
	}
	public String getUserid() {
		return userid;
	}
	public String getFirstname() {
		return firstname;
	}
	public String getLastname() {
		return lastname;
	}
	
}
