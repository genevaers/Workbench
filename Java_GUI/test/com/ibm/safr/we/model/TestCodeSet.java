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


import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.data.TestDataLayerHelper;

public class TestCodeSet extends TestCase {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestCodeSet");

	TestDataLayerHelper helper = new TestDataLayerHelper();
	static boolean postgres = true;

	public void dbStartup() {
		TestDataLayerHelper.setPostgres(postgres);
		helper.initDataLayer();

	}

	public void testGetCode() {
		boolean correctException = false;

		CodeSet codeSet = null;

		try {
			dbStartup();
			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("");
		} catch (Throwable e2) {
			correctException = true;
		}

		assertTrue(correctException);
		correctException = false;
		try {

			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
		} catch (Throwable e2) {
			correctException = true;
		}

		assertFalse(correctException);

		correctException = false;
		Code code = null;
		try {
			code = codeSet.getCode("NA");
		} catch (IllegalArgumentException e) {
			correctException = true;
		}
		assertTrue(correctException);

		// Test if the getCode method throws IllegalArgumentException if a
		// negative is given as a generalId.
		correctException = false;
		try {
			codeSet.getCode(-1);
		} catch (IllegalArgumentException e) {
			correctException = true;
		}
		assertTrue(correctException);

		code = codeSet.getCode("DB2SQ");
		assertNotNull(code);

	}

	public void testGetCodeCategory() {

		CodeSet codeSet = null;
		boolean correctException = false;

		try {

			dbStartup();
			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("");
		} catch (Throwable e2) {
			correctException = true;
		}
		assertTrue(correctException);
		correctException = false;
		try {

			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");

		} catch (Throwable e2) {
			correctException = true;
		}
		assertNotNull(codeSet.getCodeCategory());
		assertEquals(codeSet.getCodeCategory(), "ACCMETHOD");
		assertFalse(correctException);

	}

	public void testGetCodes() {
		CodeSet codeSet = null;
		boolean correctException = false;

		try {
			dbStartup();

			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");

		} catch (Throwable e2) {
			correctException = true;
		}
		assertNotNull(codeSet.getCodes());
		assertTrue(codeSet.getCodes().size() > 0);
		assertFalse(correctException);

	}

	@Override
	protected void tearDown() {
		try {
			super.tearDown();

			helper.closeDataLayer();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
