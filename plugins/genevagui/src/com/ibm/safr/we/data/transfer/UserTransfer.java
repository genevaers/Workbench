package com.ibm.safr.we.data.transfer;

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


public class UserTransfer extends SAFRTransfer {
	// Transfer class to transfer user details from Data layer to Business
	// layer.

	// True if User already exists. To decide if SQL INSERT or UPDATE.
	private boolean persistent;

	// set initially by the user, then not modifiable.
	private String userid = "";

	// modifiable by the user
	private String password; // TODO encode or encrypt
	private String firstName;
	private String middleInitial;
	private String lastName;
	private String email;
	private int logLevel; // TODO confirm data type
	private int maxCompileErrors;
	private Integer defaultViewFolderId;
	private Integer defaultEnvironmentId;
	private Integer defaultGroupId;
	private Boolean isAdmin;

	public Boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(Boolean persistent) {
		this.persistent = persistent;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String id) {
		this.userid = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleInitial() {
		return middleInitial;
	}

	public void setMiddleInitial(String middleInitial) {
		this.middleInitial = middleInitial;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	public int getMaxCompileErrors() {
		return maxCompileErrors;
	}

	public void setMaxCompileErrors(int maxCompileErrors) {
		this.maxCompileErrors = maxCompileErrors;
	}

	public Integer getDefaultViewFolderId() {
		return defaultViewFolderId;
	}

	public void setDefaultViewFolderId(Integer defaultViewFolder) {
		this.defaultViewFolderId = defaultViewFolder;
	}

	public Integer getDefaultEnvironmentId() {
		return defaultEnvironmentId;
	}

	public void setDefaultEnvironmentId(Integer defaultEnvironment) {
		this.defaultEnvironmentId = defaultEnvironment;
	}

	public Integer getDefaultGroupId() {
		return defaultGroupId;
	}

	public void setDefaultGroupId(Integer defaultGroup) {
		this.defaultGroupId = defaultGroup;
	}

	public Boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

}
