package com.ibm.safr.we.constants;

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


/**
 *This enum is used in the find replace logic text utility.It is used for
 * refining search according to the specified date.i.e.Before , after or on the
 * specified date.
 * 
 */
public enum SearchPeriod {
	None("N"), After("A"), Before("B"), On("O");

	private String typeValue;

	SearchPeriod(String typeValue) {
		this.typeValue = typeValue;
	}

	/**
	 * This method is used to get the {@link String} value of the type of the
	 * {@link SearchPeriod} selected which is defined in the enum.
	 * 
	 * @return the value of the type of {@link SearchPeriod}.
	 */
	public String getTypeValue() {
		return typeValue;
	}
}
