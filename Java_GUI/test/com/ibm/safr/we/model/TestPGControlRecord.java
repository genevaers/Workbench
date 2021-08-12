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

import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactory;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.data.transfer.ControlRecordTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;

public class TestPGControlRecord extends TestCase {

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

//		//Update a record that does not exist
//		// try updating
//		correctException = false;
//		cr.setName("NoControlrecord");
//		try {
//			cr.store();
//		} catch (SAFRException e) {
//			correctException = true;
//			e.printStackTrace();
//
//		}
//		assertTrue(correctException);

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
	
	public void testUpdate() throws SAFRValidationException, SAFRException {
		dbStartup();
		// store component -> should be inserted
		// Verify by checking values
		boolean noException = false;
		ControlRecord cr;
		cr = SAFRApplication.getSAFRFactory().createControlRecord();
		cr.setName("Fred");
		cr.setComment("Update test");
		cr.setBeginPeriod(1);
		cr.setEndPeriod(12);
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
		assertTrue(noException);
		
		cr.setName("Ginger");
		try {
			cr.store();
			noException = true;
		} catch (NullPointerException ne) {
			noException = false;
		} catch (DAOException e1) {
			noException = false;
		} catch (Throwable e2) {
			noException = false;
		}
		assertTrue(noException);
		DAOFactory fact = null;
		try {
			if(postgres) {
				fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();				
			}
		} catch (DAOException e) {
			e.printStackTrace();
		}
		
		ControlRecordTransfer updated = fact.getControlRecordDAO().getControlRecord(cr.getId(), cr.getEnvironmentId());
		assertEquals(cr.getName(), updated.getName());
	}
}
