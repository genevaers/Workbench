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


import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactory;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.data.transfer.ControlRecordTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;

public class TestPGUserExitRoutine extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestUserExitRoutine");

	static String TABLE_NAME = "EXIT";
	static String COL_ID = "EXITID";
	static boolean postgres = true;

	static String DEP_TABLE_LIST[] = { TABLE_NAME };
	TestDataLayerHelper helper = new TestDataLayerHelper();
    List<Integer> delIds = new ArrayList<Integer>();

	public void setUp() {
	}

	public void dbStartup() {
		try {
			TestDataLayerHelper.setPostgres(postgres);
			helper.initDataLayer();
		} catch (DAOException e) {
			assertFalse(true);
		}
	}

	public void removeUserExitRoutine(Integer id) {

		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);

			DAOFactory fact;
			if(postgres) {
				fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();				
			}
			ConnectionParameters params = fact.getConnectionParameters();
			SQLGenerator generator = new SQLGenerator();

			for (String tableName : DEP_TABLE_LIST) {
				try {
					String statement = generator.getDeleteStatement(params
							.getSchema(), tableName, idNames);
					PreparedStatement pst = fact.getConnection()
							.prepareStatement(statement);
					pst.setInt(1, id);
					pst.execute();
					pst.close();
				} catch (Exception e) {
					e.printStackTrace();
					assertTrue(false);
				}
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
			assertTrue(false);
		}
	}

	public void tearDown() {
        try {
            for (Integer i : delIds) {
                try {
                    removeUserExitRoutine(i);
                } catch (Exception e) {
                    // log errors
                    logger.log(Level.SEVERE, "", e);
                }
            }

        } catch (DAOException e) {
            assertFalse(true);
        }
        helper.closeDataLayer();        
	}

	public void testStore() throws SAFRException {
		dbStartup();
		boolean correctException = false;
		UserExitRoutine uer = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		Code typeCode = null;
		CodeSet codeSet;
		Code languageCode = null;
		CodeSet languageCodeSet;
		codeSet = SAFRApplication.getSAFRFactory().getCodeSet("EXITTYPE");
		typeCode = codeSet.getCode("READ");
		languageCodeSet = SAFRApplication.getSAFRFactory().getCodeSet(
				"PROGTYPE");
		languageCode = languageCodeSet.getCode("LECOB");

		try {
			uer.store();
		} catch (DAOException e1) {
			correctException = true;
		} catch (NullPointerException e1) {
			correctException = true;
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);

		boolean noException = false;
		uer.setName("test");
		uer.setComment("test");
		uer.setExecutable("test");
		uer.setLanguageCode(languageCode);
		uer.setOptimize(false);
		uer.setTypeCode(typeCode);

		try {
			uer.store();
			noException = true;
		} catch (DAOException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}
		DAOFactory fact = null;
		try {
			if(postgres) {
				fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();				
			}
		} catch (DAOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		delIds.add(uer.getId());
		assertTrue(noException);
		assertEquals(uer.getName(), "test");
		assertEquals(uer.getComment(), "test");
		assertEquals(uer.getExecutable(), "test");
		assertEquals(uer.isOptimize(), new Boolean(false));
		assertEquals(uer.getCreateBy(), fact.getSAFRLogin().getUserId());
		assertEquals(uer.getModifyBy(), fact.getSAFRLogin().getUserId());
		assertNotNull(uer.getCreateTime());
		assertNotNull(uer.getModifyTime());

		assertEquals(uer.getLanguageCode(), languageCode);
		assertEquals(uer.getTypeCode(), typeCode);

		// delete the user exit routine
		SAFRApplication.getSAFRFactory().removeUserExitRoutine(uer.getId());

		// try updating deleted user exit routine
		uer.setName("DeletedVF");
		correctException = false;
		try {
			uer.store();
		} catch (SAFRException e) {
			e.printStackTrace();
			correctException = true;
		}
		assertTrue(correctException);
	}

	public void testUpdate() throws SAFRException {
		dbStartup();
		UserExitRoutine uer = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		Code typeCode = null;
		CodeSet codeSet;
		Code languageCode = null;
		CodeSet languageCodeSet;
		codeSet = SAFRApplication.getSAFRFactory().getCodeSet("EXITTYPE");
		typeCode = codeSet.getCode("READ");
		languageCodeSet = SAFRApplication.getSAFRFactory().getCodeSet(
				"PROGTYPE");
		languageCode = languageCodeSet.getCode("LECOB");

		boolean noException = false;
		uer.setName("test");
		uer.setComment("test");
		uer.setExecutable("test");
		uer.setLanguageCode(languageCode);
		uer.setOptimize(false);
		uer.setTypeCode(typeCode);

		try {
			uer.store();
			noException = true;
		} catch (DAOException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}
		DAOFactory fact = null;
		try {
			if(postgres) {
				fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();				
			}
		} catch (DAOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		delIds.add(uer.getId());
		assertTrue(noException);
		assertEquals(uer.getName(), "test");
		assertEquals(uer.getComment(), "test");
		assertEquals(uer.getExecutable(), "test");
		assertEquals(uer.isOptimize(), new Boolean(false));
		assertEquals(uer.getCreateBy(), fact.getSAFRLogin().getUserId());
		assertEquals(uer.getModifyBy(), fact.getSAFRLogin().getUserId());
		assertNotNull(uer.getCreateTime());
		assertNotNull(uer.getModifyTime());

		assertEquals(uer.getLanguageCode(), languageCode);
		assertEquals(uer.getTypeCode(), typeCode);
		
		uer.setName("Different");
		try {
			uer.store();
			noException = true;
		} catch (DAOException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}
		assertTrue(noException);
		UserExitRoutineTransfer updated = fact.getUserExitRoutineDAO().getUserExitRoutine(uer.getId(), uer.getEnvironmentId());
		assertEquals(uer.getName(), updated.getName());
	}

	public void testSaveAs() throws SAFRValidationException, SAFRException {
		dbStartup();
		boolean correctException = false;
		UserExitRoutine uer = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		Code typeCode = null;
		CodeSet codeSet;
		Code languageCode = null;
		CodeSet languageCodeSet;
		codeSet = SAFRApplication.getSAFRFactory().getCodeSet("EXITTYPE");
		typeCode = codeSet.getCode("READ");
		languageCodeSet = SAFRApplication.getSAFRFactory().getCodeSet(
				"PROGTYPE");
		languageCode = languageCodeSet.getCode("LECOB");

		uer.setName("test");
		uer.setComment("test");
		uer.setExecutable("test");
		uer.setLanguageCode(languageCode);
		uer.setOptimize(false);
		uer.setTypeCode(typeCode);

		try {
			uer.store();
		} catch (DAOException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}
		DAOFactory fact = null;
		try {
			if(postgres) {
				fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();				
			}
		} catch (DAOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		delIds.add(uer.getId());
		UserExitRoutine userExit2 = null;
		assertFalse(correctException);

		userExit2 = (UserExitRoutine) uer.saveAs("TestSaveAsCopy_Junit",
				"newexecutable");
		delIds.add(userExit2.getId());
		assertTrue(userExit2.getId() > 0);
		assertEquals("TestSaveAsCopy_Junit", userExit2.getName());
		assertEquals(uer.getComment(), userExit2.getComment());
		assertEquals("newexecutable", userExit2.getExecutable());
		assertEquals(uer.getTypeCode(), userExit2.getTypeCode());
		assertEquals(uer.getLanguageCode(), userExit2.getLanguageCode());
		assertEquals(uer.isOptimize(), userExit2.isOptimize());

	}
	
}
