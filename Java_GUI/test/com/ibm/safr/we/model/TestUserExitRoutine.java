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
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;

public class TestUserExitRoutine extends TestCase {
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

	public void testGetEnvironment() throws SAFRException {
		// Environment should not be null while creating a new UserExit routine.
		dbStartup();
		Environment env = null;
		UserExitRoutine uer = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		env = uer.getEnvironment();
		assertNotNull(env);
	}

	public void testGetId() {
		dbStartup();
		UserExitRoutine uer = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		assertEquals(Integer.valueOf(uer.getId()), Integer.valueOf(0));

	}

	public void testGetSetName() {
		dbStartup();
		UserExitRoutine uer = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		assertEquals(uer.getName(), null);

		uer.setName("testName1");
		assertEquals(uer.getName(), "testName1");

		uer.setName("testName2");
		assertEquals(uer.getName(), "testName2");

		uer.setName("");
		assertEquals(uer.getName(), "");

		uer.setName(null);
		assertEquals(uer.getName(), null);
	}

	public void testGetSetComment() {
		dbStartup();
		UserExitRoutine uer = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();

		assertEquals(uer.getComment(), null);

		uer.setComment("testComment1");
		assertEquals(uer.getComment(), "testComment1");

		uer.setComment("testComment2");
		assertEquals(uer.getComment(), "testComment2");

		uer.setComment("");
		assertEquals(uer.getComment(), "");

		uer.setComment(null);
		assertEquals(uer.getComment(), null);

	}

	public void testGetSetTypeCode() {
		dbStartup();
		UserExitRoutine uer = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		Code typeCode;
		CodeSet codeSet;
		codeSet = SAFRApplication.getSAFRFactory().getCodeSet("EXITTYPE");
		typeCode = codeSet.getCode("READ");
		uer.setTypeCode(typeCode);
		assertEquals(uer.getTypeCode(), typeCode);

		// test for null type code
		boolean correctException = false;
		try {
			uer.setTypeCode(null);
		} catch (NullPointerException npe) {
			correctException = true;
		}
		assertTrue(correctException);
	}

	public void testGetSetLanguageCode() {
		dbStartup();
		UserExitRoutine uer = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		Code languageCode;
		CodeSet codeSet;
		codeSet = SAFRApplication.getSAFRFactory().getCodeSet("PROGTYPE");
		languageCode = codeSet.getCode("LECOB");
		uer.setLanguageCode(languageCode);
		assertEquals(uer.getLanguageCode(), languageCode);

		// test for null language code
		boolean correctException = false;
		try {
			uer.setLanguageCode(null);
		} catch (NullPointerException npe) {
			correctException = true;
		}
		assertTrue(correctException);
	}

	public void testGetSetExecutable() {
		dbStartup();
		UserExitRoutine uer = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();

		assertEquals(uer.getExecutable(), null);

		uer.setExecutable("test");
		assertEquals(uer.getExecutable(), "test");

		uer.setExecutable("test");
		assertEquals(uer.getExecutable(), "test");

		uer.setExecutable("");
		assertEquals(uer.getExecutable(), "");

		uer.setExecutable(null);
		assertEquals(uer.getExecutable(), null);

	}

	public void testGetSetOptimized() {
		dbStartup();
		UserExitRoutine uer = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();

		assertEquals(uer.isOptimize(), null);

		uer.setOptimize(false);
		assertEquals(uer.isOptimize(), new Boolean(false));

		uer.setOptimize(true);
		assertEquals(uer.isOptimize(), new Boolean(true));

		uer.setOptimize(null);
		assertEquals(uer.isOptimize(), null);
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

	public void testSameName() throws SAFRException {
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

		//Test uniqueness based on name and module name
		delIds.add(uer.getId());
		UserExitRoutine uer2 = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		uer2.setName("Utest");
		uer2.setComment("test");
		uer2.setExecutable("test");
		uer2.setLanguageCode(languageCode);
		uer2.setOptimize(false);
		uer2.setTypeCode(typeCode);

		try {
			uer.store();
			noException = true;
		} catch (DAOException e1) {
			logger.log(Level.SEVERE, "", e1);
			correctException = true;
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}
		assertTrue(correctException);
		UserExitRoutine uer3 = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		uer3.setName("test");
		uer3.setComment("test");
		uer3.setExecutable("Utest");
		uer3.setLanguageCode(languageCode);
		uer3.setOptimize(false);
		uer3.setTypeCode(typeCode);

		correctException = false;
		try {
			uer.store();
			noException = true;
		} catch (DAOException e1) {
			logger.log(Level.SEVERE, "", e1);
			correctException = true;
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}
		assertTrue(correctException);
	}

	public void testValidate() {
		boolean correctException = false;
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
		try {
			uer.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);

		correctException = false;
		uer.setName("");
		uer.setComment("test");
		uer.setExecutable("test_executable");
		uer.setLanguageCode(languageCode);
		uer.setOptimize(false);
		uer.setTypeCode(typeCode);
		try {
			uer.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);

		correctException = false;
		uer.setName("!@#vz");
		uer.setComment("test");
		uer.setExecutable("test_executable");
		uer.setLanguageCode(languageCode);
		uer.setOptimize(false);
		uer.setTypeCode(typeCode);
		try {
			uer.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);

		correctException = false;
		uer.setName("123");
		uer.setComment("test");
		uer.setExecutable("test_executable");
		uer.setLanguageCode(languageCode);
		uer.setOptimize(false);
		uer.setTypeCode(typeCode);
		try {
			uer.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

//This is now done in the database
		assertTrue(correctException);
		// test for duplicate name
		correctException = false;
		uer.setName("test");
		uer.setComment("test");
		uer.setExecutable("test_executable");
		uer.setLanguageCode(languageCode);
		uer.setOptimize(false);
		uer.setTypeCode(typeCode);
		try {
			uer.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

//		assertTrue(correctException);

		correctException = false;
		uer.setName("test_name");
		uer.setComment("test");
		uer.setExecutable(null);
		uer.setLanguageCode(languageCode);
		uer.setOptimize(false);
		uer.setTypeCode(typeCode);
		try {
			uer.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);

		correctException = false;
		uer.setName("test1");
		uer.setComment("test");
		uer.setExecutable("()*&P^");
		uer.setLanguageCode(languageCode);
		uer.setOptimize(false);
		uer.setTypeCode(typeCode);
		try {
			uer.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);
		//Ditto databse does this now
		// check for duplicate executable
		correctException = false;
		uer.setName("test_12");
		uer.setComment("test");
		uer.setExecutable("abc");
		uer.setLanguageCode(languageCode);
		uer.setOptimize(false);
		uer.setTypeCode(typeCode);
		try {
			uer.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

//		assertTrue(correctException);

		Boolean noException = false;
		uer.setComment("test");
		uer.setExecutable("test_executable");
		uer.setLanguageCode(languageCode);
		uer.setName("test_name");
		uer.setOptimize(false);
		uer.setTypeCode(typeCode);
		try {
			uer.validate();
			noException = true;
		} catch (SAFRException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		//Not so sure we can do this test here since is is a dependency check
		//There are no other components created yet
		assertTrue(noException);
		assertEquals(uer.getComment(), "test");
		assertEquals(uer.getExecutable(), "test_executable");
		assertEquals(uer.getName(), "test_name");
		assertEquals(uer.isOptimize(), new Boolean(false));
		assertEquals(uer.getLanguageCode(), languageCode);
		assertEquals(uer.getTypeCode(), typeCode);

		correctException = false;
		try {
			UserExitRoutine userExit = SAFRApplication.getSAFRFactory()
					.getUserExitRoutine(232);
			userExit.setTypeCode(codeSet.getCode("READ"));
			userExit.validate();

		} catch (SAFRException e) {
			if (e instanceof SAFRDependencyException) {
				correctException = true;
			}
			e.printStackTrace();
		}
		//assertTrue(correctException);

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
