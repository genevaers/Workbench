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

public class TestCode extends TestCase {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestCode");

	TestDataLayerHelper helper = new TestDataLayerHelper();

	static boolean postgres = true;
	
	public void dbStartup() {
		TestDataLayerHelper.setPostgres(postgres);
		helper.initDataLayer();

	}

	public void testGetDescription()

	{
		CodeSet codeSet_accm = null;
		CodeSet codeSet_datatype = null;

		dbStartup();
		codeSet_accm = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
		codeSet_datatype = SAFRApplication.getSAFRFactory().getCodeSet("DATATYPE");

		Code code_accm = codeSet_accm.getCode("DB2SQ");
		assertEquals(code_accm.getDescription(), "Db2 via SQL");
		
		Code code_datatype = codeSet_datatype.getCode("PSORT");
		assertEquals(code_datatype.getDescription(), "Packed Sortable");

	}

	public void testGetGeneralId() {
		CodeSet codeSet = null;

		dbStartup();
		codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");

		Code code = codeSet.getCode("DB2VS");
		assertEquals(code.getGeneralId(), new Integer(7));
		
		code = codeSet.getCode("KSDS");
		assertEquals(code.getGeneralId(), new Integer(3));

		Boolean correctException = false;
		try {
			codeSet.getCode("DB2123"); 
		} catch (IllegalArgumentException e) {
			correctException = true;
		}
		assertTrue(correctException);
	}

	public void testGetKey() {

		CodeSet codeSet = null;

		dbStartup();
		codeSet = SAFRApplication.getSAFRFactory().getCodeSet("CODESET");

		Code code = codeSet.getCode("ASCII");
		assertEquals(code.getKey(), "ASCII");

		code = codeSet.getCode("EBCDI");
		assertEquals(code.getKey(), "EBCDI");
		
		Boolean correctException = false;
		try {
			codeSet.getCode("DB2123");
		} catch (IllegalArgumentException e) {
			correctException = true;
		}
		assertTrue(correctException);

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
