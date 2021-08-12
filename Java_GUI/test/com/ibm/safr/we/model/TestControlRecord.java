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
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;

public class TestControlRecord extends TestCase {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestControlRecord");

	static String TABLE_NAME = "CONTROLREC";
	static String COL_ID = "CONTROLRECID";
	static boolean postgres = true;

	static String DEP_TABLE_LIST[] = { TABLE_NAME };
	TestDataLayerHelper helper = new TestDataLayerHelper();
	List<Integer> delIds = new ArrayList<Integer>();

	public void setUp() {
	    delIds.clear();
	}

	public void dbStartup() {
		try {
			TestDataLayerHelper.setPostgres(postgres);
			helper.initDataLayer();			
		} catch (DAOException e) {
			assertFalse(true);
		}
	}

	public void removeControlRecord(Integer id) {

		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);

			DAOFactory fact;
			fact = DAOFactoryHolder.getDAOFactory();
			ConnectionParameters params = fact.getConnectionParameters();
			SQLGenerator generator = new SQLGenerator();

			for (String tableName : DEP_TABLE_LIST) {
				try {
					String statement = generator.getDeleteStatement(params.getSchema(), tableName, idNames);
					PreparedStatement pst = fact.getConnection().prepareStatement(statement);
					pst.setInt(1, id);
					pst.execute();
					pst.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	public void tearDown() {
		try {
			for (Integer i : delIds) {
				try {
					removeControlRecord(i);
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

	public void testGetSetBeginPeriod() {
		dbStartup();
		ControlRecord cr = SAFRApplication.getSAFRFactory()
				.createControlRecord();
		assertEquals(cr.getBeginPeriod(), null);

		cr.setBeginPeriod(0);
		assertEquals(cr.getBeginPeriod(), new Integer(0));

		cr.setBeginPeriod(1);
		assertEquals(cr.getBeginPeriod(), new Integer(1));

		cr.setBeginPeriod(null);
		assertEquals(cr.getBeginPeriod(), null);

		cr.setBeginPeriod(-1);
		assertEquals(cr.getBeginPeriod(), new Integer(-1));

	}

	public void testGetSetComment() {
		dbStartup();
		ControlRecord cr = SAFRApplication.getSAFRFactory()
				.createControlRecord();
		assertEquals(cr.getComment(), null);

		cr.setComment("testComment1");
		assertEquals(cr.getComment(), "testComment1");

		cr.setComment("testComment2");
		assertEquals(cr.getComment(), "testComment2");

		cr.setComment("");
		assertEquals(cr.getComment(), "");

		cr.setComment(null);
		assertEquals(cr.getComment(), null);

	}

	public void testGetSetEndPeriod() {
		dbStartup();
		ControlRecord cr = SAFRApplication.getSAFRFactory()
				.createControlRecord();
		assertEquals(cr.getEndPeriod(), null);

		cr.setEndPeriod(0);
		assertEquals(cr.getEndPeriod(), new Integer(0));

		cr.setEndPeriod(1);
		assertEquals(cr.getEndPeriod(), new Integer(1));

		cr.setEndPeriod(null);
		assertEquals(cr.getEndPeriod(), null);

		cr.setEndPeriod(-1);
		assertEquals(cr.getEndPeriod(), new Integer(-1));
	}

	public void testGetEnvironment() throws SAFRException {

		// Environment should not be null while creating a new control record.
		dbStartup();
		Environment env = null;
		dbStartup();
		ControlRecord cr = SAFRApplication.getSAFRFactory()
				.createControlRecord();
		env = cr.getEnvironment();
		assertNotNull(env);
	}

	public void testGetSetFirstFiscalMonth() {
		dbStartup();
		ControlRecord cr = SAFRApplication.getSAFRFactory()
				.createControlRecord();
		assertEquals(cr.getFirstFiscalMonth(), null);

		cr.setFirstFiscalMonth(1);
		assertEquals(cr.getFirstFiscalMonth(), new Integer(1));

		cr.setFirstFiscalMonth(12);
		assertEquals(cr.getFirstFiscalMonth(), new Integer(12));

		cr.setFirstFiscalMonth(null);
		assertEquals(cr.getFirstFiscalMonth(), null);

	}

	public void testGetSetName() {
		dbStartup();
		ControlRecord cr = SAFRApplication.getSAFRFactory()
				.createControlRecord();
		assertEquals(cr.getName(), null);

		cr.setName("testName1");
		assertEquals(cr.getName(), "testName1");

		cr.setName("testName2");
		assertEquals(cr.getName(), "testName2");

		cr.setName("");
		assertEquals(cr.getName(), "");

		cr.setName(null);
		assertEquals(cr.getName(), null);
	}

	public void testStore() throws SAFRException {
		dbStartup();
		boolean correctException = false;
		boolean correctException1 = false;

		// Empty component -> should fail
		ControlRecord cr;
		cr = SAFRApplication.getSAFRFactory().createControlRecord();

		try {
			cr.store();
		} catch (DAOException e1) {
			correctException1 = true;
		} catch (NullPointerException e1) {
			correctException = true;
		} catch (Throwable e2) {
			correctException1 = true;
		}

		assertTrue(correctException);
		assertFalse(correctException1);

		// null name component -> should fail
		correctException = false;
		cr.setName(null);
		cr.setComment("");
		cr.setFirstFiscalMonth(2);
		cr.setBeginPeriod(1);
		cr.setEndPeriod(2);
		try {
			cr.store();
		} catch (DAOException e1) {
			correctException = true;
		} catch (SAFRException e) {
			correctException = false;
		}

		assertTrue(correctException);

		// store component -> should be inserted
		// Verify by checking values
		boolean noException = false;
		correctException = false;
		cr.setName("Test1");
		cr.setComment("Test1 comment");
		cr.setBeginPeriod(1);
		cr.setEndPeriod(2);
		cr.setFirstFiscalMonth(2);

		try {
			cr.store();
			noException = true;
			delIds.add(cr.getId());
		} catch (NullPointerException ne) {
			noException = false;
		} catch (DAOException e1) {
			noException = false;
		} catch (Throwable e2) {
			noException = false;
		}
		DAOFactory fact = null;
		try {
			if(postgres) {
				fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();				
			}
		} catch (DAOException e) {
			e.printStackTrace();
		}
		assertTrue(noException);
		assertEquals(cr.getName(), "Test1");
		assertEquals(cr.getComment(), "Test1 comment");
		assertEquals(cr.getBeginPeriod(), new Integer(1));
		assertEquals(cr.getEndPeriod(), new Integer(2));
		assertEquals(cr.getFirstFiscalMonth(), new Integer(2));
		assertEquals(cr.getCreateBy(), fact.getSAFRLogin().getUserId());
		assertEquals(cr.getModifyBy(), fact.getSAFRLogin().getUserId());
		assertNotNull(cr.getCreateTime());
		assertNotNull(cr.getModifyTime());

		// Test for Exception when insert with same name

		// store Control Record
		correctException = false;
		cr = SAFRApplication.getSAFRFactory().createControlRecord();
		cr.setName("Test1");
		cr.setComment("Test1 comment");
		cr.setBeginPeriod(1);
		cr.setEndPeriod(2);
		cr.setFirstFiscalMonth(2);
		try {
			cr.store();
		} catch (NullPointerException ne) {
			correctException = false;
		} catch (DAOException e1) {
			correctException = true;
		} catch (Throwable e2) {
			correctException = false;
		}
		assertTrue(correctException);

		// Delete the control record
		Integer id = cr.getId(); 
		SAFRApplication.getSAFRFactory().removeControlRecord(id);

		noException = false;
		correctException = false;
		cr.setName("Test2");
		cr.setComment("Test2 comment");
		cr.setBeginPeriod(3);
		cr.setEndPeriod(3);
		cr.setFirstFiscalMonth(3);

		try {
			cr.store();
			noException = true;
			delIds.add(cr.getId());
		} catch (NullPointerException ne) {
			noException = false;
		} catch (DAOException e1) {
			noException = false;
		} catch (Throwable e2) {
			noException = false;
		}
		assertTrue(noException);
		id = cr.getId(); 
		SAFRApplication.getSAFRFactory().removeControlRecord(id);
		
		//Update a record that does not exist
		// try updating
		correctException = false;
		cr.setName("NoControlrecord");
		try {
			cr.store();
		} catch (SAFRException e) {
			correctException = true;
			e.printStackTrace();

		}
		assertTrue(correctException);

	}

	public void testValidate() throws SAFRException {
		boolean correctException = false;
		dbStartup();
		ControlRecord cr;
		cr = SAFRApplication.getSAFRFactory().createControlRecord();
		try {
			cr.validate();
		} catch (SAFRException se) {
			correctException = true;
		}

		assertTrue(correctException);

		cr.setName("123testname");
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		cr.setName("");
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		cr.setName("ss123_$#@");
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);
		// test for duplicate name.
		cr.setName("Test_Eve");
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		cr.setName("Test");
		cr.setBeginPeriod(2);
		cr.setEndPeriod(0);
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}
		assertTrue(correctException);

		cr.setName("Test");
		cr.setBeginPeriod(2);
		cr.setEndPeriod(2);
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);
		cr.setName("Test");
		cr.setBeginPeriod(2);
		cr.setEndPeriod(1);
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);
		cr.setName("Test");
		cr.setBeginPeriod(1);
		cr.setEndPeriod(-2);
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		cr.setName("Test");
		cr.setBeginPeriod(-1);
		cr.setEndPeriod(2);
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		cr.setName("Test");
		cr.setBeginPeriod(1);
		cr.setEndPeriod(2);
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);
		cr.setName("Test");
		cr.setBeginPeriod(1);
		cr.setEndPeriod(2);
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);
		cr.setName("Test");
		cr.setBeginPeriod(0);
		cr.setEndPeriod(2);
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}
		assertTrue(correctException);

		cr.setName("Test_ControlRecord");
		cr.setBeginPeriod(0);
		cr.setEndPeriod(2);
		cr.setFirstFiscalMonth(0);
		correctException = false;
		try {
			cr.validate();
		} catch (SAFRValidationException e1) {
			correctException = true;
		}
		assertTrue(correctException);

		boolean noException = false;
		cr.setName("Test1");
		cr.setComment("Test comment");
		cr.setBeginPeriod(2);
		cr.setEndPeriod(3);
		cr.setFirstFiscalMonth(3);
		correctException = false;
		try {
			cr.validate();
			noException = true;

		} catch (DAOException e1) {
			correctException = true;
		} catch (NullPointerException e2) {
			correctException = true;
		} catch (Exception e3) {
			correctException = true;
		}
		assertTrue(noException);
		assertFalse(correctException);

	}

	public void testSaveAs() throws SAFRValidationException, SAFRException {
		dbStartup();
		ControlRecord cr1 = null;
		ControlRecord cr2 = null;
		Boolean correctException = false;
		try {
			cr1 = SAFRApplication.getSAFRFactory().getControlRecord(1);
		} catch (SAFRException se) {
			correctException = true;
		}
		assertFalse(correctException);

		cr2 = (ControlRecord) cr1.saveAs("TestSaveAsCopy_Junit");
		assertTrue(cr2.getId() > 0);
		assertEquals("TestSaveAsCopy_Junit", cr2.getName());
		assertEquals(cr1.getComment(), cr2.getComment());
		assertEquals(cr1.getFirstFiscalMonth(), cr2.getFirstFiscalMonth());
		assertEquals(cr1.getBeginPeriod(), cr2.getBeginPeriod());
		assertEquals(cr1.getEndPeriod(), cr2.getEndPeriod());
		delIds.add(cr2.getId());
	}
}
