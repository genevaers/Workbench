package com.ibm.safr.we.model.utilities.importer;

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


import junit.framework.Test;
import junit.framework.TestSuite;


public class AllImportTests extends TestSuite {

	static Class<?> classes[] = {
		TestImportUtility.class,
        TestImportLookup.class,   
        TestImportLR.class,   
        TestImportLF.class,   
        TestImportView.class,   
	};

	public AllImportTests() {
		super(classes);

	}

	public static Test suite() {
		return new AllImportTests();
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AllImportTests.suite());
	}
}
