package com.ibm.safr.we.model.query;

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


import java.util.Date;

public class UserQueryBean extends SAFRQueryBean {

	private String id;
	private Boolean admin;
	private String email;

	/**
	 * Parameterized constructor to initialize values
	 */
	public UserQueryBean(String id, String firstName, String middleInitial,
			String lastName, boolean admin, String email, Date createTime,
			String createBy, Date modifyTime, String modifyBy) {

		super(getFullName(lastName, firstName, middleInitial), createTime,
				createBy, modifyTime, modifyBy);
		this.id = id;
		this.admin = new Boolean(admin);
		this.email = email;
	}

	/**
	 * @return the user id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the user id or an empty string if it's missing
	 */
	public String getIdLabel() {
		return id != null ? id : "";
	}

	/**
	 * @return the user's email address
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return the user's email address or an empty string if it's missing
	 */
	public String getEmailLabel() {
		return email != null ? email : "";
	}

	/**
	 * @return true if user is Admin, false if not
	 */
	public Boolean isAdmin() {
		return admin;
	}

	/**
	 * @return "Yes" if user is an Administrator, otherwise "No"
	 */
	public String getAdminLabel() {
		return (admin != null && isAdmin()) ? "Yes" : "No";
	}

	/**
	 * @return the full name in form LastName, FirstName MiddleInitial
	 */
	public static String getFullName(String lastName, String firstName,
			String middleInitial) {
		String fullName = "";
		if (lastName != null && !lastName.equals("")) {
			fullName = lastName + ",";
		}
		if (firstName != null) {
			fullName += firstName + " ";
		}
		if (middleInitial != null) {
			fullName += middleInitial;
		}
		return fullName;
	}

	/**
	 * @return a String userid.
	 */
	public String getDescriptor() {
		return getIdLabel();
	}
}
