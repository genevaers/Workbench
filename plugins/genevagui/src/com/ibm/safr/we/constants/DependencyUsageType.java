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


public enum DependencyUsageType {
	NONE("none"), SOURCE("Source"), OUTPUT("Output"), LOGIC_TEXT("Logic Text"), SORT_KEY_TITLE(
			"Sort Key Title"), EFFECTIVE_DATE("Effective Date"), TARGET(
			"Target"), FORMAT("Format"), WRITE("Write"), READ("Read"), LOOKUP(
			"Lookup");
	private String label;

	private DependencyUsageType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
