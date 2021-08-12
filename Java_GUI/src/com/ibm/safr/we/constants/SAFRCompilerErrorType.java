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
 * 
 * This enum class represents the type of compiler error.
 * 
 */
public enum SAFRCompilerErrorType {
	VIEW_PROPERTIES("View Properties"), 
	FORMAT_RECORD_FILTER("Format-Phase Record Filter"), 
	EXTRACT_RECORD_FILTER("Extract-Phase Record Filter"), 
	EXTRACT_COLUMN_ASSIGNMENT("Extract-Phase Column Logic"), 
    EXTRACT_RECORD_OUTPUT("Extract-Phase Record Logic"), 
	FORMAT_COLUMN_CALCULATION("Format-Phase Column Logic");
	
	String text;

	private SAFRCompilerErrorType(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
