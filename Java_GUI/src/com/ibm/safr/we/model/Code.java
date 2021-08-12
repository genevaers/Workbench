package com.ibm.safr.we.model;

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


import com.ibm.safr.we.model.base.SAFRObject;

/**
 * It is an item in {@link CodeSet} class.
 * 
 */
public class Code extends SAFRObject {

	private String key;
	private String description;
	private Integer generalId;

	/**
	 * Create an Code object.
	 * 
	 * @param key
	 *            keyid from CODE database.
	 * @param description
	 *            description from CODE database.
	 * @param generalId
	 *            generalid from CODE database.
	 */
	public Code(String key, String description, Integer generalId) {
		this.key = key;
		this.description = description;
		this.generalId = generalId;
	}

	/**
	 * Get key of the code object.
	 * 
	 * @return key value of Code.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Get description of Code object.
	 * 
	 * @return description of Code object.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get generalid of Code object.
	 * 
	 * @return generalid of Code object.
	 */
	public Integer getGeneralId() {
		return generalId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Code)) {
			return false;
		}
		Code that = (Code) obj;

		if (this.key.equals(that.key) && (this.generalId == that.generalId)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (null == this.key ? 0 : this.key.hashCode());
		hash = 31 * hash
				+ (null == this.generalId ? 0 : this.generalId.hashCode());
		return hash;
	}

}
