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

import com.ibm.safr.we.constants.EditRights;

public abstract class EnvironmentalQueryBean extends NumericIdQueryBean {

	private Integer environmentId;
	private EditRights rights;

	public EnvironmentalQueryBean(Integer environmentId, Integer id,
			String name, EditRights rights,
			Date createTime, String createBy, Date modifyTime, String modifyBy) {
		super(id, name, createTime, createBy, modifyTime, modifyBy);
		this.environmentId = environmentId;
		this.rights = rights;
	}

	public Integer getEnvironmentId() {
		return environmentId;
	}

	public String getEnvironmentIdLabel() {
		return environmentId != null ? environmentId.toString() : "";
	}

	// determine rights based on user role as a default
	public EditRights getRights() {
        return rights;
	}
}
