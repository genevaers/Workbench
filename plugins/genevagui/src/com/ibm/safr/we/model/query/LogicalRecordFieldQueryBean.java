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
 * Query information for a Logical Record Field. Includes just the info
 * displayed in a list of Logical Record Field and the key info needed to
 * instantiate a Logical Record Field.
 * 
 */
public class LogicalRecordFieldQueryBean extends EnvironmentalQueryBean {

	private Integer lrid = null;
	
	/**
	 *Parameterized constructor to initialize values
	 */
	public LogicalRecordFieldQueryBean(Integer environmentId, Integer id,
			Integer lrid, String name, Date createTime, String createBy,
			Date modifyTime, String modifyBy) {

		super(environmentId, id, name, null, createTime, createBy,
				modifyTime, modifyBy);
		this.lrid = lrid;
	}

	/**
	 * @return the lrid
	 */
	public Integer getLrId() {
		return lrid;
	}

}
