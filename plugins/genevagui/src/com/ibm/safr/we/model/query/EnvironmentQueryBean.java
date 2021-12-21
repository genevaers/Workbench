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

/**
 * Query information for a Environment. Includes just the info displayed in a
 * list of Environment and the key info needed to instantiate a Environment.
 * 
 */
public class EnvironmentQueryBean extends NumericIdQueryBean {
	private Boolean adminRights;

	/**
	 * Parameterized constructor to initialize values
	 */
	public EnvironmentQueryBean(Integer id, String name,
			boolean adminRights, Date createTime,
			String createBy, Date modifyTime, String modifyBy) {
		super(id, name, createTime, createBy, modifyTime, modifyBy);
		this.adminRights = adminRights;
	}

	/**
	 * @return a Boolean indicating whether the user has admin rights in this
	 *         environment or not
	 */
	public Boolean hasAdminRights() {
		return adminRights;
	}

}
