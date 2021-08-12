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
 * Query information for a group. Includes just the information displayed in a
 * list of groups and the key information needed to instantiate a group.
 */
public class GroupQueryBean extends NumericIdQueryBean {

	/**
	 *Parameterized constructor to initialize values.
	 */
	public GroupQueryBean(Integer id, String name, Date createTime,
			String createBy, Date modifyTime, String modifyBy) {
		super(id, name, createTime, createBy, modifyTime, modifyBy);
	}

}
