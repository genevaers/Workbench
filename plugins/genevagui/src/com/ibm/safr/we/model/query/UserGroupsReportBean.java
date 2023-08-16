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


public class UserGroupsReportBean {

	private String lastname;
	private String firstname;
	private String userid;
	private String sysadmin;
	private String password;
	private String groupid;
	private String grpname;
	
	public UserGroupsReportBean(String lastname, String firstname, String userid, String sysadmin, String password, String groupid, String grpname) {
		super();
		this.lastname = lastname;
		this.firstname = firstname;
		this.userid = userid;
		this.sysadmin = sysadmin;
		this.password = password;
		this.groupid = groupid;
		this.grpname = grpname;
	}
	public String getLastname() {
		return lastname;
	}
	public String getFirstname() {
		return firstname;
	}
	public String getUserid() {
		return userid;
	}
	public String getSysadmin() {
		return sysadmin;
	}
	public String getPassword() {
		return password;
	}
	public String getGroupid() {
		return groupid;
	}
	public String getGrpname() {
		return grpname;
	}
	
	
}
