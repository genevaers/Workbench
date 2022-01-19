package com.ibm.safr.we.exceptions;

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
 * Represents exceptions for component-not-found errors.
 * 
 */
public class SAFRNotFoundException extends SAFRException {

	private static final long serialVersionUID = 1L;
	private Integer componentId;

	public SAFRNotFoundException(String string) {
		super(string);
	}

	public SAFRNotFoundException(String string, Integer componentId) {
		super(string);
		this.componentId = componentId;
	}

	public Integer getComponentId() {
		return componentId;
	}

}
