package com.ibm.safr.we.model.utilities;

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


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.query.LookupQueryBean;

public class TestBatchActivateLookupPaths extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestBatchActivateLookupPaths");

	TestDataLayerHelper helper = new TestDataLayerHelper();

	public void dbStartup() {
		helper.initDataLayer();

	}

	public void tearDown() {

		helper.closeDataLayer();
	}

	public void testActivate() {
		dbStartup();

		List<BatchComponent> batchComponentList = new ArrayList<BatchComponent>();

		BatchComponent batchComponent1 = new BatchComponent(
				new LookupQueryBean(1, 1999, "", "", 0, 0, null, null, null, null, 
						"", null, "", null, null), false);

		BatchComponent batchComponent2 = new BatchComponent(
				new LookupQueryBean(1, 2000, "", "", 0, 0, null, null, null, null,
						"", null, "", null, null), false);

		BatchComponent batchComponent3 = new BatchComponent(
				new LookupQueryBean(1, 2001, "", "", 0, 0, null, null, null, null,
						"", null, "", null, null), false);

		batchComponentList.add(batchComponent1);
		batchComponentList.add(batchComponent2);
		batchComponentList.add(batchComponent3);

		try {
			BatchActivateLookupPaths.activate(1, batchComponentList);
		} catch (DAOException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (SAFRException e) {
			e.printStackTrace();
			assertFalse(true);
		}

		assertEquals(ActivityResult.PASS, batchComponent1.getResult());
		assertEquals(ActivityResult.FAIL, batchComponent2.getResult());
		assertEquals(ActivityResult.LOADERRORS, batchComponent3.getResult());
	}
}
