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


import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.security.UserSession;

public class TestSAFRApplication extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestSAFRApplication");
	TestDataLayerHelper helper = new TestDataLayerHelper();

	public void setUp() {
		helper.initDataLayer();
	}

	public void tearDown() {
		helper.closeDataLayer();
	}

	public void testGetSAFRFactory() {
		SAFRFactory sFactory = SAFRApplication.getSAFRFactory();
		assertNotNull(sFactory);
	}
	
	public void testGetSetDatabaseSchema(){
		SAFRApplication.setDatabaseSchema("test_dbschema");
		assertEquals("test_dbschema",SAFRApplication.getDatabaseSchema());

	}

	public void testGetSetUserSession() {
		try {
			SAFRApplication.setUserSession(null);
		} catch (NullPointerException e) {
			assertTrue(true);
			logger.log(Level.SEVERE, "", e);
		} catch (DAOException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}

		User user = null;
		Environment environment = null;
		try {
			user = SAFRApplication.getSAFRFactory().getUser("ADMIN");
			environment = SAFRApplication.getSAFRFactory().getEnvironment(1);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		UserSession userSession1 = new UserSession(user, environment, null);
		try {
			SAFRApplication.setUserSession(userSession1);
		} catch (DAOException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertEquals(SAFRApplication.getUserSession(), userSession1);

		UserSession userSession2 = new UserSession(user, environment, null);
		try {
			SAFRApplication.setUserSession(userSession2);
		} catch (DAOException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertEquals(SAFRApplication.getUserSession(), userSession2);

		// try getting the user and env from user session param object
		Boolean noException = false;
		UserSessionParameters params = null;
		try {
			params = ((PGDAOFactory) DAOFactoryHolder.getDAOFactory())
					.getSAFRLogin();
			noException = true;
		} catch (DAOException e) {
			noException = false;
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(noException);
		assertTrue(params.getUserId().equals(user.getUserid()));
		assertTrue(params.getEnvId() == 1l);
	}

    public void testGetVersion()  throws SAFRException {
        String ver = SAFRApplication.getStoredProcedureVersion();
        assertNotNull(ver);
        assertTrue(ver.matches("SD\\d\\.\\d\\d\\.\\d\\d\\d.\\d+"));
    }
    	
}
