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

public abstract class NumericIdQueryBean extends SAFRQueryBean {

	private Integer id;

	public NumericIdQueryBean(Integer id, String name, Date createTime,
			String createBy, Date modifyTime, String modifyBy) {
		super(name, createTime, createBy, modifyTime, modifyBy);
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public String getIdLabel() {
		return id != null ? id.toString() : "";
	}
	
	/**
	 * @return a String concatenation of component name and id in the format Name[Id].
	 */
	public String getDescriptor() {
		return getNameLabel() + "[" + getIdLabel() + "]";
	}

}
