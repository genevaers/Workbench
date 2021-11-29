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


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.LRFieldKeyType;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactory;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.data.transfer.LogicalFileTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.PhysicalFile.InputDataset;
import com.ibm.safr.we.model.PhysicalFile.OutputDataset;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;

public class TestPGLogicalRecord extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestLogicalRecord");

	static String TABLE_NAME = "LOGREC";
	static String COL_ID = "LOGRECID";
	static boolean postgres = true;

	static String DEP_TABLE_LIST[] = { "LRINDEX", "LRLFASSOC", "SECLOGREC", "LRFIELD", TABLE_NAME };
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

	public void removeLogicalRecord(Integer id) {

		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);

			PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
			ConnectionParameters params = fact.getConnectionParameters();
			SQLGenerator generator = new SQLGenerator();

			String schema = params.getSchema();
			try {
				String statement = "DELETE FROM "
						+ schema + ".LRINDEXFLD A WHERE A.LRINDEXID IN (SELECT B.LRINDEXID FROM "
						+ schema + ".LRINDEX B WHERE B.LOGRECID = ? AND B.ENVIRONID=A.ENVIRONID) ";
				PreparedStatement pst = fact.getConnection().prepareStatement(statement);
				pst.setInt(1, id);
				pst.execute();
				pst.close();
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}

            try {
                // Deleting field attributes
                String statement = "DELETE FROM "
                        + schema + ".LRFIELDATTR A WHERE LRFIELDID IN (SELECT LRFIELDID FROM "
                        + schema + ".LRFIELD B WHERE LOGRECID = ?)";

                PreparedStatement pst = fact.getConnection().prepareStatement(statement);
                pst.setInt(1, id);
                pst.execute();
                pst.close();
            } catch (Exception e1) {
                e1.printStackTrace();
                assertTrue(false);
            }
			
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
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
	}

	public void tearDown() {
        try {
            for (Integer i : delIds) {
                try {
                    //removeLogicalRecord(i);
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

	public void testTransaction() throws SAFRException {
		dbStartup();
		LogicalRecord lr = null;

		// Check name and comment when the transaction fails as
		// they were before starting the transaction i.e the LR gets saved in
		// the DB and as the LRField length is null the transactions gets failed
		// and the whole transaction is rolled back.
		try {

			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1349);

			lr.setName("LRchangedname123456");
			lr.setComment("changed comment");
			lr.setLookupExitId(5);
			LRField lrField = lr.getLRFields().get(0);
			lrField.setLength(null);
			lr.store();

		} catch (NullPointerException e) {
			assertTrue(true);
			assertEquals("TestSaveAsView1", SAFRApplication.getSAFRFactory()
					.getLogicalRecord(1349).getName());
			assertEquals("updatedcomment", SAFRApplication.getSAFRFactory()
					.getLogicalRecord(1349).getComment());

		}
		LogicalRecord lr1 = null;
		try {
			// Check the name when the transaction is successful.

			lr1 = SAFRApplication.getSAFRFactory().getLogicalRecord(1350);

			lr1.setComment("updatedcomment");
			lr1.setLookupExitId(2);
			lr1.setActive(true);
			lr1.setCheckLookupDependencies(true);
			lr1.setCheckViewDependencies(true);
			lr1.setLookupExitParams("lookup exit params");
			lr1.setLookupExitRoutine(new UserExitRoutine(1));

			lr1.store();
			assertEquals("TestSaveAsView2", SAFRApplication.getSAFRFactory()
					.getLogicalRecord(1350).getName());

		} catch (NullPointerException e) {
			assertTrue(false);

		}

		LogicalRecord lr2 = null;
		try {
			// Check the transaction and the undo usage to reset the model
			// objects if the transaction fails in between.

			lr2 = SAFRApplication.getSAFRFactory().createLogicalRecord();
			lr2.setName("NewLRName");
			lr2.setComment("updatedcomment");
			lr2.setLookupExitId(2);
			lr2.setActive(true);
			lr2.setCheckLookupDependencies(true);
			lr2.setCheckViewDependencies(true);
			lr2.setLookupExitParams("lookup exit params");
			lr2.setLookupExitRoutine(new UserExitRoutine(1));
			lr2.setCheckViewDependencies(true);
			lr2.setLookupExitId(1);
			lr2.setLRStatusCode(SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.LRSTATUS).getCode("ACTVE"));
			lr2.setLRTypeCode(SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.LRTYPE).getCode("FILE"));
			LRField lrField = lr2.addField();
			lrField.setName("LR Field");
			lrField.setComment("comment");
			lrField.setDatabaseColumnName("db_column_name");
			lrField.setDataTypeCode(SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode("ALNUM"));
			lrField.setLength(null);

			lr2.store();
			delIds.add(lr2.getId());

		} catch (NullPointerException e) {
			assertTrue(true);
			assertTrue(lr2.getId() == 0);

		}

	}

	public void testAddAssociatedLogicalFile() throws SAFRException{
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1359);
		LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(1354);
		
		
		ComponentAssociation comp_Association = new ComponentAssociation(lr, lf.getId(), lf.getName(), null);

		lr.addAssociatedLogicalFile(comp_Association);
		
		/* test LF (1354) is added to the association list */
		assertEquals(lr.getLogicalFileAssociations().get(1).getAssociatedComponentName(),"Test_LF");
		
		
		
	}
	
	public void testGetLogicalFileAssociations() throws SAFRException {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		SAFRList<ComponentAssociation> safrList = lr
				.getLogicalFileAssociations();
		assertTrue(safrList.isEmpty());

		
		lr = SAFRApplication.getSAFRFactory().getLogicalRecord(20);

		safrList = lr.getLogicalFileAssociations();
		assertEquals(safrList.get(0).getAssociatedComponentName(),"TT2851_LKUP_LF");
		
		
	}

	public void testGetSetActive() {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();

		try {
			assertFalse(lr.isActive());
		} catch (SAFRException e) {
			throw new RuntimeException(e);
		}
		boolean noException = false;
		boolean active = true;
		try {
			lr.setActive(active);
			assertEquals(lr.isActive(), active);
			noException = true;
		} catch (SAFRException e) {
			throw new RuntimeException(e);
		}
		assertTrue(noException);

	}

	public void testGetSetLookupExitRoutine() {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();

		try {
			assertNull(lr.getLookupExitRoutine());
		} catch (SAFRException e) {
			throw new RuntimeException(e);
		}
		boolean noException = false;
		UserExitRoutine LookupExitRoutine;
		try {
			LookupExitRoutine = SAFRApplication.getSAFRFactory()
					.getUserExitRoutine(1);
			lr.setLookupExitRoutine(LookupExitRoutine);
			assertEquals(lr.getLookupExitRoutine(), LookupExitRoutine);
			noException = true;
		} catch (SAFRException e) {
			throw new RuntimeException(e);
		}
		assertTrue(noException);

	}

	public void testGetSetLookupExitParams() {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		assertNull(lr.getLookupExitParams());

		String LookupExitParams = "LEParams";
		lr.setLookupExitParams(LookupExitParams);
		assertEquals(lr.getLookupExitParams(), LookupExitParams);
	}

	public void testGetLRFields() throws SAFRException {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1359);
		LRField lr_field = lr.getLRFields().get(0);
		
		assertEquals(lr_field.getDatabaseColumnName(),"test");
		assertEquals(lr_field.getDataTypeCode().getDescription(),"Alphanumeric");
		assertEquals(lr_field.getLength(),new Integer(1));
	
	}

	public void testAddField() {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		boolean noException = false;
		try {
			assertNotNull(lr.addField());
			noException = true;
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue(noException);
		noException = false;
		try {
			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1359);
			assertNotNull(lr.addField());
			noException = true;
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		assertTrue(noException);
	}
    
	public void testInsertFieldBefore() throws SAFRException {
		dbStartup();
		
        LogicalRecord lr = SAFRApplication.getSAFRFactory().createLogicalRecord();
        LRField lf1 = lr.insertFieldBefore(null);
        LRField lf2 = lr.insertFieldBefore(lf1);
        List<LRField> flds = lr.getLRFields().getActiveItems();
        assertEquals(lf2, flds.get(0));
        assertEquals(flds.get(0).getOrdinalPosition(), new Integer(1));
        assertEquals(lf1, flds.get(1));
        assertEquals(flds.get(1).getOrdinalPosition(), new Integer(2));		
	}

    public void testInsertFieldAfter() throws SAFRException {
        dbStartup();
        
        LogicalRecord lr = SAFRApplication.getSAFRFactory().createLogicalRecord();
        LRField lf1 = lr.insertFieldAfter(null);
        LRField lf2 = lr.insertFieldAfter(lf1);
        List<LRField> flds = lr.getLRFields().getActiveItems();
        assertEquals(lf1, flds.get(0));
        assertEquals(flds.get(0).getOrdinalPosition(), new Integer(1));
        assertEquals(lf2, flds.get(1));
        assertEquals(flds.get(1).getOrdinalPosition(), new Integer(2));
    }
	
	public void testRemoveField() {
		// method not in use.
		dbStartup();
		assertTrue(true);

		Boolean noException = false;
		LogicalRecord lr = null;
		// List<LRField> lrFields=null;
		try {

			lr = SAFRApplication.getSAFRFactory().createLogicalRecord();
			LRField lrField1 = lr.addField();
			LRField lrField2 = lr.addField();
			List<LRField> selectedLrFields = new ArrayList<LRField>();
			selectedLrFields.add(lrField1);
			selectedLrFields.add(lrField2);
			List<LRField> lrFieldsDeleted = lr.removeFields(selectedLrFields);

			assertTrue(lrFieldsDeleted.contains(lrField1));
			assertTrue(lrFieldsDeleted.contains(lrField2));
			noException = true;

		} catch (SAFRValidationException e) {
			e.printStackTrace();
		} catch (DAOException e) {
			e.printStackTrace();
		} catch (SAFRException e) {
			e.printStackTrace();
		}

		assertTrue(noException);

	}

	public void testGetPrimayKeyLength() {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		Integer pkLength = lr.getPrimayKeyLength();
		assertNotNull(pkLength);

		Boolean noException = false;
		try {
			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(20);
			noException = true;
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		pkLength = lr.getPrimayKeyLength();
		assertNotNull(pkLength);
		assertTrue(noException);
	}

	public void testGetTotalLength() {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		Integer totalLength = lr.getTotalLength();
		assertNotNull(totalLength);

		Boolean noException = false;
		try {
			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(20);
			noException = true;
		} catch (SAFRException e) {
			e.printStackTrace();
		}
		totalLength = lr.getTotalLength();
		assertNotNull(totalLength);
		assertTrue(noException);
	}

	public void testMoveFieldDown() throws SAFRException {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		LRField lrf1 = null;
		LRField lrf2 = null;
		lr.addField().setName("test1");
		lr.addField().setName("test2");
		lr.addField().setName("test3");
		lr.addField().setName("test4");

		lrf1 = (LRField) lr.getLRFields().getActiveItems().get(2);
		lr.moveFieldDown(lrf1);
		lrf2 = (LRField) lr.getLRFields().getActiveItems().get(3);

		assertEquals(lrf1.getName(), lrf2.getName());

		LogicalRecord lr1 = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		LRField lrf3 = null;
		LRField lrf4 = null;
		lr1.addField().setName("test1");
		LRField tmpField = lr1.addField();
		tmpField.setName("test2");
		tmpField.setPersistence(SAFRPersistence.DELETED);
		lr1.addField().setName("test3");
		LRField tmpField1 = lr1.addField();
		tmpField1.setName("test4");
		tmpField1.setPersistence(SAFRPersistence.DELETED);
		lr1.addField().setName("test5");
		lr1.addField().setName("test6");

		lrf3 = (LRField) lr1.getLRFields().getActiveItems().get(2);
		lr1.moveFieldDown(lrf3);
		lrf4 = (LRField) lr1.getLRFields().getActiveItems().get(3);

		assertEquals(lrf3.getName(), lrf4.getName());

		LogicalRecord lr2 = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		LRField lrf5 = null;
		LRField lrf6 = null;
		lr2.addField().setName("test1");
		tmpField = lr2.addField();
		tmpField.setName("test2");
		tmpField.setPersistence(SAFRPersistence.DELETED);
		lr2.addField().setName("test3");
		tmpField1 = lr2.addField();
		tmpField1.setName("test4");
		tmpField1.setPersistence(SAFRPersistence.DELETED);
		lr2.addField().setName("test5");
		lr2.addField().setName("test6");

		lrf5 = (LRField) lr2.getLRFields().getActiveItems().get(1);
		System.out.println("list1");
		printActiveitems(lr2);

		lr2.moveFieldDown(lrf5);
		lrf6 = (LRField) lr2.getLRFields().getActiveItems().get(2);
		System.out.println("list2");
		printActiveitems(lr2);

		assertEquals(lrf5.getName(), lrf6.getName());

	}

	public void testMoveFieldUp() throws SAFRException {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		LRField lrf1 = null;
		LRField lrf2 = null;
		lr.addField().setName("test1");
		lr.addField().setName("test2");
		lr.addField().setName("test3");
		lr.addField().setName("test4");

		lrf1 = (LRField) lr.getLRFields().getActiveItems().get(2);
		lr.moveFieldUp(lrf1);
		lrf2 = (LRField) lr.getLRFields().getActiveItems().get(1);

		assertEquals(lrf1.getName(), lrf2.getName());

		LogicalRecord lr1 = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		LRField lrf3 = null;
		LRField lrf4 = null;
		lr1.addField().setName("test1");
		LRField tmpField = lr1.addField();
		tmpField.setName("test2");
		tmpField.setPersistence(SAFRPersistence.DELETED);
		lr1.addField().setName("test3");
		LRField tmpField1 = lr1.addField();
		tmpField1.setName("test4");
		tmpField1.setPersistence(SAFRPersistence.DELETED);
		lr1.addField().setName("test5");
		lr1.addField().setName("test6");

		lrf3 = (LRField) lr1.getLRFields().getActiveItems().get(2);
		lr1.moveFieldUp(lrf3);
		lrf4 = (LRField) lr1.getLRFields().getActiveItems().get(1);

		assertEquals(lrf3.getName(), lrf4.getName());

		LogicalRecord lr2 = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		LRField lrf5 = null;
		boolean correctException = false;
		try {
			lr2.addField().setName("test1");
			tmpField = lr2.addField();
			tmpField.setName("test2");
			tmpField.setPersistence(SAFRPersistence.DELETED);
			lr2.addField().setName("test3");
			tmpField1 = lr2.addField();
			tmpField1.setName("test4");
			tmpField1.setPersistence(SAFRPersistence.DELETED);
			lr2.addField().setName("test5");
			lr2.addField().setName("test6");

			lrf5 = (LRField) lr2.getLRFields().getActiveItems().get(4);
			System.out.println("list1");
			printActiveitems(lr2);

			lr2.moveFieldUp(lrf5);
			lr2.getLRFields().getActiveItems().get(3);
			System.out.println("list2");
			printActiveitems(lr2);

		} catch (IndexOutOfBoundsException e) {
			correctException = true;
		} 
		assertTrue(correctException);
	}

    public void testMoveFieldParent() throws SAFRException {
        dbStartup();
        LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1494);
        LRField fld = lr.getLRFields().getActiveItems().get(9);
        lr.moveFieldUp(fld);
        LRField fld1 = lr.getLRFields().getActiveItems().get(8);
        assertEquals(fld1.getName(), "JRNL_DATE_TIME");
        LRField fld2 = lr.getLRFields().getActiveItems().get(9);
        assertEquals(fld2.getName(), "JRNL_DAY");
        assertEquals(fld2.getRedefine(), new Integer(92731));        
        LRField fld3 = lr.getLRFields().getActiveItems().get(10);
        assertEquals(fld3.getName(), "JRNL_DATE2");
        assertEquals(fld3.getRedefine(), new Integer(92731));        
        LRField fld4 = lr.getLRFields().getActiveItems().get(11);
        assertEquals(fld4.getName(), "JRNL_TIME");
        assertEquals(fld4.getRedefine(), new Integer(92730));        
        LRField fld5 = lr.getLRFields().getActiveItems().get(12);
        assertEquals(fld5.getName(), "JRNL_TIME_HH");
        assertEquals(fld5.getRedefine(), new Integer(92745));        
    }
	
	private void printActiveitems(LogicalRecord lr2) {
		List<LRField> activeItems1 = lr2.getLRFields().getActiveItems();
		LRField item;
		Iterator<LRField> i = activeItems1.iterator();
		while (i.hasNext()) {
			item = i.next();
			if (item.getPersistence() != SAFRPersistence.DELETED) {
				System.out.println("  " + item.getName());
			}
		}
	}

    public void testCorrectRedefine1() throws SAFRException {
        dbStartup();
        LogicalRecord lr = SAFRApplication.getSAFRFactory().createLogicalRecord();

        LRField lrf1 = lr.addField();
        lrf1.setID(1);
        lrf1.setName("test1");
        lrf1.setLength(10);        
        LRField lrf2 = lr.addField();
        lrf2.setID(2);
        lrf2.setName("test2");
        lrf2.setLength(10);
        LRField lrf3 = lr.addField();
        lrf3.setID(3);
        lrf3.setName("test3");
        lrf3.setPosition(11);
        lrf3.setLength(5);
        LRField lrf4 = lr.addField();
        lrf4.setID(4);
        lrf4.setName("test4");
        lrf4.setPosition(16);
        lrf4.setLength(5);
        
        assertTrue(lrf2.correctRedefine(0));
        assertTrue(lrf2.correctRedefine(1));
        assertFalse(lrf3.correctRedefine(0));
        assertFalse(lrf3.correctRedefine(1));
        assertTrue(lrf3.correctRedefine(2));
        assertTrue(lrf4.correctRedefine(0));
        assertTrue(lrf4.correctRedefine(1));
        assertTrue(lrf4.correctRedefine(3));        
    }

    public void testCorrectRedefine2() throws SAFRException {
        dbStartup();
        LogicalRecord lr = SAFRApplication.getSAFRFactory().createLogicalRecord();

        LRField lrf1 = lr.addField();
        lrf1.setID(1);
        lrf1.setName("test1");
        lrf1.setLength(10);        
        LRField lrf2 = lr.addField();
        lrf2.setID(2);
        lrf2.setName("test2");
        lrf2.setLength(15);
        LRField lrf3 = lr.addField();
        lrf3.setID(3);
        lrf3.setName("test3");
        lrf3.setPosition(11);
        lrf3.setLength(5);
        LRField lrf4 = lr.addField();
        lrf4.setID(4);
        lrf4.setName("test4");
        lrf4.setPosition(16);
        lrf4.setLength(4);
        LRField lrf5 = lr.addField();
        lrf5.setID(5);
        lrf5.setName("test5");
        lrf5.setPosition(21);
        lrf5.setLength(5);
        
        assertTrue(lrf2.correctRedefine(0));
        assertTrue(lrf2.correctRedefine(1));
        assertFalse(lrf3.correctRedefine(0));
        assertTrue(lrf3.correctRedefine(2));
        assertFalse(lrf4.correctRedefine(0));
        assertTrue(lrf4.correctRedefine(2));
        assertTrue(lrf4.correctRedefine(3));        
        assertTrue(lrf5.correctRedefine(0));
        assertTrue(lrf5.correctRedefine(2));
        assertTrue(lrf5.correctRedefine(4));        
    }
    
    public void testSetRedefine() throws SAFRException {
        dbStartup();
        LogicalRecord lr = SAFRApplication.getSAFRFactory().createLogicalRecord();
        
        // check the loops exception
        LRField lrf1 = lr.addField();
        lrf1.setID(1);
        lrf1.setName("test1");
        lrf1.setLength(10);        
        LRField lrf2 = lr.addField();
        lrf2.setID(2);
        lrf2.setName("test2");
        lrf2.setLength(15);        
        lrf2.setRedefine(1);
        
        assertEquals(lrf2.getRedefine(), new Integer(1));        
    }
    
    public void testSetRedefineFail() throws SAFRException {
        dbStartup();
        LogicalRecord lr = SAFRApplication.getSAFRFactory().createLogicalRecord();
        
        // check the loops exception
        LRField lrf1 = lr.addField();
        lrf1.setID(1);
        lrf1.setName("test1");
        lrf1.setLength(10);        
        LRField lrf2 = lr.addField();
        lrf2.setID(2);
        lrf2.setName("test2");
        lrf2.setLength(15);
        
        lrf1.setRedefine(2);
        try {
            lrf2.setRedefine(1);
            fail();
        } catch (SAFRFatalException e) {
        }        
    }
    
    
	public void testAutoCalcRedefine() throws SAFRException {
        dbStartup();
        LogicalRecord lr = SAFRApplication.getSAFRFactory()
                .createLogicalRecord();
        LRField lrf1 = lr.addField();
        lrf1.setID(1);
        lrf1.setName("test1");
        lrf1.setLength(10);
        LRField lrf2 = lr.addField();
        lrf2.setID(2);
        lrf2.setName("test2");
        lrf2.setPosition(1);
        lrf2.setLength(5);
        LRField lrf3 = lr.addField();
        lrf3.setID(3);
        lrf3.setName("test3");
        lrf3.setPosition(6);
        lrf3.setLength(5);
        LRField lrf4 = lr.addField();
        lrf4.setID(4);
        lrf4.setName("test4");
        lrf4.setPosition(6);
        lrf4.setLength(2);
        LRField lrf5 = lr.addField();
        lrf5.setID(5);
        lrf5.setName("test5");
        lrf5.setPosition(11);
        lrf5.setLength(5);
        
        assertEquals(lrf1.getRedefine(), new Integer(0));
	    assertEquals(lrf2.getRedefine(),new Integer(1));
        assertEquals(lrf3.getRedefine(),new Integer(1));
        assertEquals(lrf4.getRedefine(), new Integer(3));
        assertEquals(lrf5.getRedefine(), new Integer(0));
	}

    public void testAutoCalcRedefine172() throws SAFRException {
        dbStartup();
        LogicalRecord lr = SAFRApplication.getSAFRFactory()
                .createLogicalRecord();
        LRField lrf1 = lr.addField();
        lrf1.setID(1);
        lrf1.setName("FRMT");
        lrf1.setLength(5);
        LRField lrf2 = lr.addField();
        lrf2.setID(2);
        lrf2.setName("DT_CTNT");
        lrf2.setPosition(6);
        lrf2.setLength(8);
        LRField lrf3 = lr.addField();
        lrf3.setID(3);
        lrf3.setName("DT");
        lrf3.setPosition(6);
        lrf3.setLength(8);
        LRField lrf4 = lr.addField();
        lrf4.setID(4);
        lrf4.setName("CCYYMM");
        lrf4.setPosition(6);
        lrf4.setLength(6);
        LRField lrf5 = lr.addField();
        lrf5.setID(5);
        lrf5.setName("CCYY");
        lrf5.setPosition(6);
        lrf5.setLength(4);
        LRField lrf6 = lr.addField();
        lrf6.setID(6);
        lrf6.setName("YYMMDD");
        lrf6.setPosition(8);
        lrf6.setLength(6);
        LRField lrf7 = lr.addField();
        lrf7.setID(7);
        lrf7.setName("YY");
        lrf7.setPosition(8);
        lrf7.setLength(2);
        LRField lrf8 = lr.addField();
        lrf8.setID(8);
        lrf8.setName("MMDD");
        lrf8.setPosition(10);
        lrf8.setLength(4);
        LRField lrf9 = lr.addField();
        lrf9.setID(9);
        lrf9.setName("MM");
        lrf9.setPosition(10);
        lrf9.setLength(2);
        LRField lrf10 = lr.addField();
        lrf10.setID(10);
        lrf10.setName("DD");
        lrf10.setPosition(12);
        lrf10.setLength(2);
        
        assertEquals(lrf1.getRedefine(), new Integer(0));
        assertEquals(lrf2.getRedefine(),new Integer(0));
        assertEquals(lrf3.getRedefine(),new Integer(2));
        assertEquals(lrf4.getRedefine(), new Integer(3));
        assertEquals(lrf5.getRedefine(), new Integer(4));
        assertEquals(lrf6.getRedefine(), new Integer(3));
        assertEquals(lrf7.getRedefine(),new Integer(6));
        assertEquals(lrf8.getRedefine(),new Integer(6));
        assertEquals(lrf9.getRedefine(), new Integer(8));
        assertEquals(lrf10.getRedefine(), new Integer(8));
    }

    public void testAutoCalcRedefineInside() throws SAFRException {
        dbStartup();
        LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1494);
        List<LRField> flds = lr.getLRFields().getActiveItems();
        LRField fld = flds.get(3);
        assertEquals(fld.getName(), "JRNL_YEAR");
        fld.setLength(100);
        lr.autocalcRedefine();
        assertEquals(flds.get(3).getRedefine(), new Integer(92730));
        assertEquals(flds.get(4).getRedefine(), new Integer(92732));
        assertEquals(flds.get(5).getRedefine(), new Integer(92733));
        assertEquals(flds.get(6).getRedefine(), new Integer(92732));
        assertEquals(flds.get(7).getRedefine(), new Integer(92732));
        assertEquals(flds.get(8).getRedefine(), new Integer(92732));
        assertEquals(flds.get(9).getRedefine(), new Integer(92732));
        assertEquals(flds.get(10).getRedefine(), new Integer(92738));
        assertEquals(flds.get(11).getRedefine(), new Integer(92738));
        assertEquals(flds.get(12).getRedefine(), new Integer(92745));
        assertEquals(flds.get(13).getRedefine(), new Integer(92745));
        assertEquals(flds.get(14).getRedefine(), new Integer(92745));
    }
    
    public void testStoreUpdate() throws SAFRException {
        dbStartup();
        LogicalRecord rec = SAFRApplication.getSAFRFactory().getLogicalRecord(1494);
        rec.getLRFields().get(0).setName("NewName");
        rec.getLRFields().get(0).setHeading1("Newheader");
        rec.store();
        rec.getLRFields().get(0).setName("NAME");
        rec.getLRFields().get(0).setHeading1("");        
    }

//    public void testStoreActivateUpdate() throws SAFRException, SQLException {
//        dbStartup();
//        LogicalRecord rec = SAFRApplication.getSAFRFactory().getLogicalRecord(1494);
//        assertNull(rec.getActivatedTime());
//        assertNull(rec.getActivatedBy());
//        Date upDate = rec.getModifyTime();
//        String upBy = rec.getModifyBy();
//        rec.setActive(true);
//        rec.store();
//        assertNotNull(rec.getActivatedTime());
//        assertNotNull(rec.getActivatedBy());        
//        assertEquals(upDate,rec.getModifyTime());
//        assertEquals(upBy,rec.getModifyBy());
//        
//        // cleanup
//        Connection con = ((DB2DAOFactory) DAOFactoryHolder.getDAOFactory()).getConnection();
//        ConnectionParameters params = ((DB2DAOFactory) DAOFactoryHolder.getDAOFactory()).getConnectionParameters();
//        String updateStr = 
//            "UPDATE " + params.getSchema() + ".LOGREC " +
//            "SET LASTACTTIMESTAMP=NULL,LASTACTUSERID=NULL,LRSTATUSCD='INACT' " +
//            "WHERE ENVIRONID=? AND LOGRECID=?";
//        PreparedStatement pst = con.prepareStatement(updateStr);
//        pst.setInt(1, ENVIRONID);
//        pst.setInt(2, 1494);
//        pst.executeUpdate();
//    }
    
	public void testStore() throws SAFRException {
		dbStartup();
		LogicalFile lf = makeLFPF();
		ComponentAssociation componentAssociation = null;
		LogicalRecord lr = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();

		Code lrTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.LRTYPE).getCode(Codes.LOGICAL_FILE);
		Code lrStatusCode = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.LRSTATUS).getCode(Codes.INACTIVE);

		componentAssociation = new ComponentAssociation(lr, lf.getId(), lf.getName(),null);
		
		LRField lrf = lr.addField();
		lrf.setName("mylrfield");
		
		lr.setName("lr12");
		lr.setComment("test");
		lr.setLookupExitId(null);
		lr.setLRTypeCode(lrTypeCode);
		lr.setLRStatusCode(lrStatusCode);
		lr.getLogicalFileAssociations().add(componentAssociation);
		lr.store();
        delIds.add(lr.getId());
		DAOFactory fact = null;
		fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
		
		LogicalRecordTransfer dblr = fact.getLogicalRecordDAO().getLogicalRecord(lr.getId(), lr.getEnvironmentId());

		assertEquals(lr.getId(), dblr.getId());
		assertEquals(lr.getName(), dblr.getName());
		assertEquals(lr.getComment(), dblr.getComments());
		assertEquals(lr.getCreateBy(), fact.getSAFRLogin().getUserId());
		assertEquals(lr.getLogicalFileAssociations().get(0),componentAssociation);
		assertEquals(lr.getModifyBy(), fact.getSAFRLogin().getUserId());
		assertNotNull(lr.getCreateTime());
		assertNotNull(lr.getModifyTime());
        assertNull(lr.getActivatedTime());
        assertNull(lr.getActivatedBy());
        
//		lr.getLogicalFileAssociations().remove(componentAssociation);
//		lr.getLogicalFileAssociations().remove(componentAssociation1);
//		lr.getLogicalFileAssociations().remove(componentAssociation2);
//		componentAssociation.setPersistence(SAFRPersistence.DELETED);
//
//		lr.store();
//
//		LogicalRecord lr1 = null;
//		lr1 = SAFRApplication.getSAFRFactory().getLogicalRecord(5);
//		lr1.setName("Update_test12");
//		lr1.setComment("sssssssssss");
//		lr.getLogicalFileAssociations().remove(componentAssociation1);
//		lr.store();
//
//		// delete LR with associated View and lookups.
//		LogicalRecord lr2 = null;
//		lr2 = SAFRApplication.getSAFRFactory().createLogicalRecord();
//		lr2.setName("Update_test12345678901");
//		lr2.setComment("sssssssssss");
//		lr2.setLookupExitId(1);
//		lr2.setLRStatusCode(lrStatusCode);
//		lr2.setLRTypeCode(lrTypeCode);
//		componentAssociation = new ComponentAssociation(lr2, 5, "jaydeep",
//				null);
//		lr2.getLogicalFileAssociations().add(componentAssociation);
//		lr2.addField();
//		LRField lf2 = lr2.addField();
//		lr2.addField();
//		lr2.store();
//
//		List<LRField> lrFields = new ArrayList<LRField>();
//		lrFields.add(lf2);
//		lr2.removeFields(lrFields);
//		lr2.getLRFields().flushDeletedItems();
//		lr2.store();
//		lr2 = SAFRApplication.getSAFRFactory()
//				.getLogicalRecord(lr2.getId());
//		assertEquals(lr2.getLRFields().size(), 3);
//
//		// delete the Logical record.
//		SAFRApplication.getSAFRFactory().removeLogicalRecord(lr2.getId());
//
//		// try updating deleted logical record.
//		lr.setName("NOLogicalRecord");
//		try {
//			lr2.store();
//			fail();
//		} catch (SAFRException e) {
//		}

	}

    public void testStoreActivate() throws SAFRException {
        dbStartup();
        LogicalRecord lr = SAFRApplication.getSAFRFactory().createLogicalRecord();

        Code lrTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.LRTYPE).getCode(Codes.LOGICAL_FILE);
        ComponentAssociation componentAssociation = new ComponentAssociation(lr,5,"jaydeep",null);
        
        lr.setName("lr12");
        lr.setComment("test");
        lr.setLookupExitId(1);
        lr.setLRTypeCode(lrTypeCode);
        lr.setActive(true);
        lr.getLogicalFileAssociations().add(componentAssociation);
        lr.store();
        delIds.add(lr.getId());

        assertNotNull(lr.getActivatedTime());
        assertNotNull(lr.getActivatedBy());
        assertNotNull(lr.getModifyTime());
        assertNotNull(lr.getModifyBy());
    }
	
	public void testStoreNoPermissionCreate() throws SAFRException {
		
		dbStartup();		
		helper.setUser("NOPERM");
		
		LogicalRecord lr = SAFRApplication.getSAFRFactory().createLogicalRecord();

		Code lrTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.LRTYPE).getCode(Codes.LOGICAL_FILE);
		Code lrStatusCode = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.LRSTATUS).getCode(Codes.INACTIVE);
		
		ComponentAssociation ca1 = new ComponentAssociation(lr, 5, "jaydeep",
				null);
		ComponentAssociation ca2 = new ComponentAssociation(lr, 3,
				"test_jaydeep", null);
		ComponentAssociation ca3 = new ComponentAssociation(lr, 4,
				"test_jayd_21", null);
		
		lr.setName("Testnopermissioncreate");
		lr.setComment("test");
		lr.setLookupExitId(1);
		lr.setLRTypeCode(lrTypeCode);
		lr.setLRStatusCode(lrStatusCode);
		lr.getLogicalFileAssociations().add(ca1);
		lr.getLogicalFileAssociations().add(ca2);
		lr.getLogicalFileAssociations().add(ca3);
		
		try {
			lr.store();
			fail();			
		} catch (SAFRException e) {
			String mes = e.getMessage();
			assertEquals(mes, "The user is not authorized to create a new logical record.");
		}
	}

	public void testStoreNoPermissionUpdate() throws SAFRException {
		
		dbStartup();		
		helper.setUser("NOPERM");
		
		LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1439);

		lr.setName("Test_Modify_NoPerm");

		try {
			lr.store();
			fail();
		} catch (SAFRException e) {
			String mes = e.getMessage();
			assertEquals(mes, "The user is not authorized to update this logical record.");
		}
	}
	
	public void testValidate() {
		boolean correctException = false;
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		Code lrTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.LRTYPE).getCode(Codes.LOGICAL_FILE);
		Code lrStatusCode = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.LRSTATUS).getCode(Codes.INACTIVE);

		try {
			lr.validate();
		} catch (SAFRException se) {
			correctException = true;
		} catch (Exception e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}

		assertTrue(correctException);

		lr.setName("123testname");
		correctException = false;
		try {
			lr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}

		assertTrue(correctException);

		lr.setName("");
		correctException = false;
		try {
			lr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}

		assertTrue(correctException);

		lr.setName("ss123_$#@");
		correctException = false;
		try {
			lr.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}

		ComponentAssociation componentAssociation = null;
		try {
			componentAssociation = new ComponentAssociation(lr, 5, "jaydeep",
					null);
		} catch (SAFRException e3) {
			assertTrue(false);
			e3.printStackTrace();
		}
		Boolean noException = false;
		lr.setName("Test123");
		lr.setComment("test");
		lr.getLogicalFileAssociations().add(componentAssociation);
		lr.setLRTypeCode(lrTypeCode);
		lr.setLRStatusCode(lrStatusCode);

		try {
			LRField lrf = lr.addField();
			lrf.setName("test");
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode("ALNUM");
			lrf.setDataTypeCode(code);
			lrf.setLength(5);
			lr.validate();
			noException = true;
		} catch (SAFRException e1) {
			e1.printStackTrace();
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		} catch (Exception e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}
		assertTrue(noException);

		// Total key length should not be greater than 256.
		correctException = false;
		try {
			LRField lrf = lr.addField();
			lrf.setName("test13");
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode("ALNUM");
			lrf.setDataTypeCode(code);
			lrf.setKeyType(LRFieldKeyType.PRIMARYKEY);
			lrf.setPkeySeqNo(1);
			lrf.setLength(250);

			LRField lrf1 = lr.addField();
			lrf1.setDataTypeCode(code);
			lrf1.setName("test23");
			lrf1.setKeyType(LRFieldKeyType.PRIMARYKEY);
			lrf1.setPkeySeqNo(2);
			lrf1.setLength(10);
			lr.getLogicalFileAssociations().add(componentAssociation);
			lr.validate();
		} catch (SAFRValidationException e) {
			if (e.getMessageString().contains(
					"Total key length should not be greater than 256")) {
				correctException = true;
			}
			e.printStackTrace();
		} catch (DAOException e) {
			assertTrue(false);
			e.printStackTrace();
		} catch (SAFRException e) {
			assertTrue(false);
			e.printStackTrace();
		}
		assertTrue(correctException);

		// primary keys are in sequence.
		correctException = false;
		noException = false;
		try {
			lr = SAFRApplication.getSAFRFactory().createLogicalRecord();
			lr.setName("qwer");
			LRField lrf = lr.addField();
			lrf.setName("test1");
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode("ALNUM");
			lrf.setDataTypeCode(code);
			lrf.setKeyType(LRFieldKeyType.PRIMARYKEY);
			lrf.setPkeySeqNo(1);
			lrf.setLength(20);

			LRField lrf1 = lr.addField();
			lrf1.setDataTypeCode(code);
			lrf1.setName("test2");
			lrf1.setKeyType(LRFieldKeyType.PRIMARYKEY);
			lrf1.setPkeySeqNo(3);
			lrf1.setLength(10);
			lr.getLogicalFileAssociations().add(componentAssociation);
			lr.validate();
			noException = true;
		} catch (SAFRValidationException e) {
			if (e.getMessageString().contains(
					"The LR primary keys are not in sequence")) {
				correctException = true;
			}
			e.printStackTrace();
		} catch (SAFRException e) {
			assertTrue(false);
			e.printStackTrace();
		}
		// assertTrue(noException);
		assertTrue(correctException);

		// There must be at least one corresponding file.
		correctException = false;
		noException = false;
		try {
			lr = SAFRApplication.getSAFRFactory().createLogicalRecord();
			lr.setName("qwer1");
			lr.setLRTypeCode(lrTypeCode);
			lr.setLRStatusCode(lrStatusCode);
			LRField lrf = lr.addField();
			lrf.setName("test1");
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode("ALNUM");
			lrf.setDataTypeCode(code);
			lrf.setKeyType(LRFieldKeyType.PRIMARYKEY);
			lrf.setPkeySeqNo(1);
			lrf.setLength(20);

			LRField lrf1 = lr.addField();
			lrf1.setDataTypeCode(code);
			lrf1.setName("test2");
			lrf1.setKeyType(LRFieldKeyType.PRIMARYKEY);
			lrf1.setPkeySeqNo(2);
			lrf1.setLength(25);
			lr.validate();
			noException = true;

		} catch (SAFRValidationException e) {
			correctException = true;
			e.printStackTrace();
		} catch (SAFRException e) {
			correctException = true;
			e.printStackTrace();
		}
		// assertTrue(noException);
		assertTrue(correctException);

	 	// A field with a primary key is required before a field with an
		// alternate key is created.
		correctException = false;
		noException = false;
		try {
			lr = SAFRApplication.getSAFRFactory().createLogicalRecord();
			lr.setLRTypeCode(lrTypeCode);
			lr.setLRStatusCode(lrStatusCode);
			
			lr.setName("qwer12");
			LRField lrf = lr.addField();
			lrf.setName("test1");
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode("ALNUM");
			lrf.setDataTypeCode(code);
			lrf.setLength(25);
			lrf.setKeyType(LRFieldKeyType.EFFENDDATE);
		//	lrf.setKeyType(LRFieldKeyType.PRIMARYKEY);
		//	lrf.setPkeySeqNo(1);
		//	lrf.setLength(20);

			LRField lrf1 = lr.addField();
			lrf1.setDataTypeCode(code);
			lrf1.setName("test2");
			// lrf1.setKeyType(LRFieldKeyType.PRIMARYKEY);
			// lrf1.setPkeySeqNo(2);
			lrf1.setLength(25);
			lr.getLogicalFileAssociations().add(componentAssociation);
			lr.validate();
			noException = true;

		} catch (SAFRValidationException e) {
			if (e.getMessageString().contains(
					"A field with a primary key is required")) {
				correctException = true;
			}
			e.printStackTrace();
		} catch (SAFRException e) {
			correctException = true;
			e.printStackTrace();
		}
		// assertTrue(noException);
		assertTrue(correctException); 

		// Field Name must be unique already exist.
		correctException = false;
		noException = false;
		try {
			lr = SAFRApplication.getSAFRFactory().createLogicalRecord();
			lr.setLRTypeCode(lrTypeCode);
			lr.setLRStatusCode(lrStatusCode);
			
			lr.setName("qwer23");
			LRField lrf = lr.addField();
			lrf.setName("test1");
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode("ALNUM");
			lrf.setDataTypeCode(code);
			lrf.setKeyType(LRFieldKeyType.PRIMARYKEY);
			lrf.setPkeySeqNo(1);
			lrf.setLength(20);

			LRField lrf1 = lr.addField();
			lrf1.setDataTypeCode(code);
			lrf1.setName("test1");
			lrf1.setKeyType(LRFieldKeyType.PRIMARYKEY);
			lrf1.setPkeySeqNo(2);
			lrf1.setLength(25);
			lr.getLogicalFileAssociations().add(componentAssociation);
			lr.validate();
			noException = true;

		} catch (SAFRValidationException e) {
			if (e.getMessageString().contains("Field Name must be unique")) {
				correctException = true;
			}
			e.printStackTrace();
		} catch (SAFRException e) {
			correctException = true;
			e.printStackTrace();
		}
		// assertTrue(noException);
		assertTrue(correctException);

		// // There must be at least one corresponding file. test 2: add and
		// then remove lf.
		correctException = false;
		noException = false;
		componentAssociation = null;
		try {
			componentAssociation = new ComponentAssociation(lr, 5, "jaydeep",
					null);
		} catch (SAFRException e3) {
			assertTrue(false);
			e3.printStackTrace();
		}
		LogicalRecord lr1 = null;
		try {
			lr1 = SAFRApplication.getSAFRFactory().createLogicalRecord();
			lr1.setName("qwer23434wr1231");
			LRField lrf = lr1.addField();
			lrf.setName("test1");
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode("ALNUM");
			lrf.setDataTypeCode(code);
			lrf.setKeyType(LRFieldKeyType.PRIMARYKEY);
			lrf.setPkeySeqNo(1);
			lrf.setLength(20);

			LRField lrf1 = lr1.addField();
			lrf1.setDataTypeCode(code);
			lrf1.setName("test2");
			lrf1.setKeyType(LRFieldKeyType.PRIMARYKEY);
			lrf1.setPkeySeqNo(2);
			lrf1.setLength(25);

			lr1.getLogicalFileAssociations().add(componentAssociation);
			LRField lrf2 = lr.addField();
			lrf2.setName("test");
			Code code1 = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode("ALNUM");
			lrf.setDataTypeCode(code1);
			lrf.setLength(5);
			lr1.setLookupExitId(1);
			lr1.setLRTypeCode(lrTypeCode);
			lr1.setLRStatusCode(lrStatusCode);

			lr1.store();
			delIds.add(lr1.getId());

			lr1.getLogicalFileAssociations().remove(componentAssociation);
			lr1.validate();

		} catch (SAFRValidationException e) {
			if (e.getMessageString().contains(
					"There must be at least one corresponding file.")) {
				correctException = true;
			}
		} catch (SAFRException e) {
			e.printStackTrace();
            assertTrue(false);
		}
		assertTrue(correctException);

	}

	public void testRemoveFieldDepView() throws SAFRException {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1439);

		// remove dependent field
		LRField fld = lr.getLRFields().get(1);
		List<LRField> flds = new ArrayList<LRField>();
		flds.add(fld);
		
		try {
			lr.removeFields(flds);
			fail();			
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "(?s)^\\s*Field\\s*'Name\\[85062\\]\\s*':.*VIEWS:.*ProcessEvent\\[8638\\]\\s*-\\s*\\[Col\\s*1.*$";
			assertTrue(errMsg.matches(pattern));						
		}
	}

	public void testRemoveFieldDepLookup() throws SAFRException {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1440);

		// remove dependent field
		LRField fld = lr.getLRFields().get(0);
		List<LRField> flds = new ArrayList<LRField>();
		flds.add(fld);
		
		try {
			lr.removeFields(flds);
			fail();
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "(?s)^\\s*Field\\s*'Id\\[85064\\]\\s*':.*LOOKUP PATHS:.*EventLookup\\[2060\\].*$";
			assertTrue(errMsg.matches(pattern));						
		}
	}
	
	public void testSaveAs() throws SAFRValidationException, SAFRException {
		dbStartup();
		LogicalRecord lr1 = null;
		LogicalRecord lr2 = null;
		lr1 = SAFRApplication.getSAFRFactory().getLogicalRecord(1351);
		lr2 = (LogicalRecord) lr1.saveAs("TestSaveAsCopy_Junit");
		delIds.add(lr2.getId());
		assertTrue(lr2.getId() > 0);
		assertEquals("TestSaveAsCopy_Junit", lr2.getName());
		assertEquals(lr1.getComment(), lr2.getComment());
		assertEquals(lr1.getLookupExitParams(), lr2.getLookupExitParams());
		assertEquals(2, lr1.getLogicalFileAssociations().size());
		assertEquals(3, lr2.getLRFields().size());
		assertEquals(lr1.getLRTypeCode(), lr2.getLRTypeCode());

	}

	public void testValidateInvalidExit() throws SAFRException {
		dbStartup();
		
		LogicalRecord rec = SAFRApplication.getSAFRFactory().getLogicalRecord(1439);
		UserExitRoutine exit = SAFRApplication.getSAFRFactory().getUserExitRoutine(312);
		rec.setLookupExitRoutine(exit);
		
		try {
			rec.validate();
			fail();			
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "(?s)^.*'READEXIT\\[312\\]'.*$";
			assertTrue(errMsg.matches(pattern));						
		}
		
		rec.setLookupExitId(666);
		
		try {
			rec.validate();
			fail();			
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "The user exit routine with id \\[666\\] does not exist\\. Please select a valid user exit routine\\.";
			assertTrue(errMsg.matches(pattern));						
		}
		
	}

	public void testValidateLongName() throws SAFRException {
		dbStartup();
		
		LogicalRecord rec = SAFRApplication.getSAFRFactory().getLogicalRecord(1439);
		rec.setName("01234567890123456789012345678901234567890123456789");
		
		try {
			rec.validate();
			fail();			
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "The length of Logical Record name '01234567890123456789012345678901234567890123456789' cannot exceed 48 characters\\.*$";
			assertTrue(errMsg.matches(pattern));						
		}
	}

	public void testValidateDuplicate() throws SAFRException {
		dbStartup();
		
		LogicalRecord rec = SAFRApplication.getSAFRFactory().createLogicalRecord();
		rec.setName("Event");
		Code lrTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.LRTYPE).getCode(Codes.LOGICAL_FILE);
		rec.setLRTypeCode(lrTypeCode);
		
		try {
			rec.validate();
			fail();			
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "The Logical Record name 'Event' already exists\\. Please specify a different name\\.";
			assertTrue(errMsg.matches(pattern));						
		}
	}

	public void testRemoveAssociatedLogicalFileDepView() throws SAFRException {
		dbStartup();
		LogicalRecord rec = SAFRApplication.getSAFRFactory().getLogicalRecord(1439);

		try {
			rec.removeAssociatedLogicalFiles(rec.getLogicalFileAssociations());
			fail();			
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "(?s)^.*Event\\[1492\\].*VIEWS\\s*:.*ProcessEvent\\[8638\\].*$";
			assertTrue(errMsg.matches(pattern));						
		}
	}

	public void testRemoveAssociatedLogicalFileDepLookup() throws SAFRException {
		dbStartup();
		LogicalRecord rec = SAFRApplication.getSAFRFactory().getLogicalRecord(1440);

		try {
			rec.removeAssociatedLogicalFiles(rec.getLogicalFileAssociations());
			fail();
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "(?s)^.*Lookup\\[1493\\].*LOOKUP PATHS\\s*:.*EventLookup\\[2060\\].*$";
			assertTrue(errMsg.matches(pattern));						
		}
	}
	
	public void testRecalculateFields() throws SAFRException {
		dbStartup();
		
		LogicalRecord rec = SAFRApplication.getSAFRFactory().getLogicalRecord(1439);
		
		rec.getLRFields().get(0).setPositionSimple(1);
		rec.getLRFields().get(1).setPositionSimple(4002);
		rec.getLRFields().get(2).setPositionSimple(4003);
		
		rec.recalculateFields(rec.getLRFields().get(0));
		
		assertEquals(rec.getLRFields().get(0).getPosition().intValue(), 1);
		assertEquals(rec.getLRFields().get(1).getPosition().intValue(), 11);
		assertEquals(rec.getLRFields().get(2).getPosition().intValue(), 31);
		
	}

	public void testRecalculateFieldsSelect() throws SAFRException {
		dbStartup();
		
		LogicalRecord rec = SAFRApplication.getSAFRFactory().getLogicalRecord(1439);
		
		rec.getLRFields().get(0).setPositionSimple(1);
		rec.getLRFields().get(1).setPositionSimple(4002);
		rec.getLRFields().get(2).setPositionSimple(4003);
		
		rec.recalculateFields(rec.getLRFields());
		
		assertEquals(rec.getLRFields().get(0).getPosition().intValue(), 1);
		assertEquals(rec.getLRFields().get(1).getPosition().intValue(), 11);
		assertEquals(rec.getLRFields().get(2).getPosition().intValue(), 31);
		
	}

    public void testRecalculateFieldsSelectRedefine() throws SAFRException {
        dbStartup();
        
        LogicalRecord rec = SAFRApplication.getSAFRFactory().getLogicalRecord(606);
        
        List<LRField> flds = rec.getLRFields().subList(19, 26);
        rec.recalculateFields(flds);
        
        assertEquals(flds.get(0).getPosition().intValue(), 39);
        assertEquals(flds.get(1).getPosition().intValue(), 40);
        assertEquals(flds.get(2).getPosition().intValue(), 42);
        assertEquals(flds.get(3).getPosition().intValue(), 46);
        assertEquals(flds.get(4).getPosition().intValue(), 47);
        assertEquals(flds.get(5).getPosition().intValue(), 47);
        assertEquals(flds.get(6).getPosition().intValue(), 50);
        
    }

	public LogicalFile makeLFPF() throws SAFRException {
		//Make a PF 
        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();
        InputDataset inputDatasetDataSource = pf.new InputDataset();
        OutputDataset outputDatasetDataSource = pf.new OutputDataset();
        //SQLDatabase sqlpf = getSQLPhysicalFile(1);

        CodeSet codeSet;
        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FILETYPE");
        Code fileTypeCode = codeSet.getCode("DISK");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
        Code accessMethodCode = codeSet.getCode("VSAM");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("RECFM");
        Code recfmCode = codeSet.getCode("FB");

        pf.setName("test4");
        pf.setFileTypeCode(fileTypeCode);
        pf.setAccessMethod(accessMethodCode);
        pf.setComment("comment1");
        inputDatasetDataSource.setDatasetName("dataset1");
        inputDatasetDataSource.setInputDDName("inputDD1");
        inputDatasetDataSource.setMinRecordLen(10);
        inputDatasetDataSource.setMaxRecordLen(20);
        outputDatasetDataSource.setOutputDDName("output1");
        outputDatasetDataSource.setRecfm(recfmCode);
        outputDatasetDataSource.setLrecl(50);
        
        boolean noException = true;
        try {
            pf.store();
        } catch (DAOException e1) {
        	noException = false;
        } catch (SAFRException e) {
        	noException = false;
        }

		DAOFactory fact = null;
		if(postgres) {
			fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();		
		}
		PhysicalFileTransfer dbpf = fact.getPhysicalFileDAO().getPhysicalFile(pf.getId(), pf.getEnvironmentId());	
		delIds.add(pf.getId());
        assertTrue(noException);

		/* Setup File Association and create logical file object */
		FileAssociation fileAssociation = null;
		LogicalFile lf = SAFRApplication.getSAFRFactory().createLogicalfile();

		fileAssociation = new FileAssociation(lf, pf.getId(), pf.getName(), null);

		/* Test storing of logical File with name , comment and pf file being associated, Fail if exception is thrown */
		
		lf.setName("TestLF");
		lf.setComment("test");
		lf.addAssociatedPhysicalFile(fileAssociation);

		lf.store();
		LogicalFileTransfer dblf = fact.getLogicalFileDAO().getLogicalFile(lf.getId(), lf.getEnvironmentId());	

		/* Get logical file fields and check on stored values */
		assertEquals(lf.getName(), dblf.getName());
		assertEquals(lf.getComment(), dblf.getComments());
		assertEquals(lf.getCreateBy(), fact.getSAFRLogin().getUserId());
		//assertEquals(lf.getPhysicalFileAssociations().get(0).getAssociationId(), dblf.);
		assertNotNull(lf.getCreateTime());
		assertNotNull(lf.getModifyTime());
		
		return lf;
	}

}
