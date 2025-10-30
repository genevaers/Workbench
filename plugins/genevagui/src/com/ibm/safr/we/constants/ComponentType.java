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

public enum ComponentType {
	User("User"), 
	Group("Group"), 
	Environment("Environment"), 
	ControlRecord("Control Record"),
	UserExitRoutine("User-Exit Routine"), 
	ReadUserExitRoutine("Read User Exit Routine"),
	LookupUserExitRoutine("Lookup User Exit Routine"), 
	WriteUserExitRoutine("Write User Exit Routine"),
	FormatUserExitRoutine("Format User Exit Routine"), 
	PhysicalFile("Physical File"), 
	LogicalFile("Logical File"),
	LogicalRecord("Logical Record"), 
	LogicalRecordField("LR Field"), 
	LookupPath("Lookup Path"), 
	View("View"),
	ViewFolder("View Folder"), 
	AllComponents("All Components"),
	CobolCopyBook("COBOL Copybook"),
	Connection("Connection");

	private String label;

	private ComponentType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
