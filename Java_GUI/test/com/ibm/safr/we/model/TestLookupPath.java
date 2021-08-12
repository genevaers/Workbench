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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.LookupPathSourceFieldType;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.view.View;

public class TestLookupPath extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestLookupPath");

	static String TABLE_NAME = "LOOKUP";
	static String COL_ID = "LOOKUPID";
	static String DEP_TABLE_LIST[] = { "LOOKUPSRCKEY", "LOOKUPSTEP", TABLE_NAME };
	static int ENVIRONID=111;
	TestDataLayerHelper helper = new TestDataLayerHelper();
    List<Integer> delIds = new ArrayList<Integer>();

	public void setUp() {
	}

	public void dbStartup() {
		try {
			helper.initDataLayer(ENVIRONID);
		} catch (DAOException e) {
			assertFalse(true);
		}
	}

	public void removeLookupPath(Integer id) {

		try {
			PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder
					.getDAOFactory();
			ConnectionParameters params = fact.getConnectionParameters();
			SQLGenerator generator = new SQLGenerator();

			List<String> idnames = new ArrayList<String>();
			idnames.add(COL_ID);

			for (String tableName : DEP_TABLE_LIST) {
				try {
					String statement1 = generator.getDeleteStatement(params
							.getSchema(), tableName, idnames);
					PreparedStatement pst1 = fact.getConnection().prepareStatement(statement1);
					pst1.setInt(1, id);
					pst1.execute();
					pst1.close();
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
                    removeLookupPath(i);
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

	public void testCreateRemoveStep() throws SAFRException{
		dbStartup();
		
		LookupPath lkup = SAFRApplication.getSAFRFactory().createLookupPath();
		int steps_before = lkup.getLookupPathSteps().size();
		assertEquals (steps_before,1);
		
		lkup.createStep();
		int steps_create = lkup.getLookupPathSteps().size();
		assertEquals (steps_create,2);
		
		lkup.removeStep(1);
		int steps_remove = lkup.getLookupPathSteps().size();
		assertEquals (steps_remove,1);
	}
	
	public void testGetSetTragetLRLFAssociation() throws DAOException, SAFRException{
		dbStartup();
		LookupPath lkup = SAFRApplication.getSAFRFactory().createLookupPath();
		LookupPath lkup_1 = SAFRApplication.getSAFRFactory().getLookupPath(2003);
		ComponentAssociation lrlf_association_1 = lkup_1.getTargetLrFileAssociation();
		
		assertEquals("TestGetLFDependencies",lrlf_association_1.getAssociatedComponentName());
		
		lkup.setTargetLRLFAssociation(lrlf_association_1);
		ComponentAssociation lrlf_association = lkup.getTargetLrFileAssociation();
		
		assertEquals("TestGetLFDependencies",lrlf_association.getAssociatedComponentName());
	
	}
	
	public void testSetSourceLR() throws DAOException, SAFRException{
		dbStartup();
		LookupPath lkup = SAFRApplication.getSAFRFactory().createLookupPath();
		LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1359);
		
		lkup.setSourceLR(lr);
		assertEquals(new Integer(1359),lkup.getLookupPathSteps().get(0).getSourceLRId());	
	}
	
	public void testTransaction() throws SAFRException {
		dbStartup();
		LookupPath lukp = null;

		// Check name and comment when the transaction fails as
		// they were before starting the transaction i.e the LUKP gets saved in
		// the DB and as the step is null the transactions gets failed
		// and the whole transaction is rolled back.
		try {

			lukp = SAFRApplication.getSAFRFactory().getLookupPath(2003);

			lukp.setName("LUKPchangedname");
			lukp.setComment("changed comment");
			SAFRList<LookupPathStep> step = lukp.getLookupPathSteps();
			step.set(0, null);
			lukp.store();

		} catch (NullPointerException e) {
			assertTrue(true);
			assertEquals("TestLookupPathViewDependencies", SAFRApplication
					.getSAFRFactory().getLookupPath(2003).getName());
			assertEquals("updatedcomment", SAFRApplication.getSAFRFactory()
					.getLookupPath(2003).getComment());

		}
		LookupPath lukp1 = null;
		try {
			// Check the name when the transaction is successful.

			lukp1 = SAFRApplication.getSAFRFactory().getLookupPath(2003);

			lukp1.setComment("updatedcomment");

			lukp1.store();
			assertEquals("TestLookupPathViewDependencies", SAFRApplication
					.getSAFRFactory().getLookupPath(2003).getName());

		} catch (NullPointerException e) {
			assertTrue(false);

		}

		LookupPath lukp2 = null;
		try {
			// Check the transaction and the undo usage to reset the model
			// objects if the transaction fails in between.

			lukp2 = SAFRApplication.getSAFRFactory().createLookupPath();
			lukp2.setName("NewLUKPName");
			lukp2.setComment("updatedcomment");
			LookupPathStep step = lukp2.getLookupPathSteps().getActiveItems()
					.get(0);
			step.setSequenceNumber(null);

			lukp2.store();
			delIds.add(lukp2.getId());			

		} catch (NullPointerException e) {
			assertTrue(true);
			assertTrue(lukp2.getId() == 0);

		}

	}

	public void testIsSetValid() throws SAFRException {
		dbStartup();
			
		LookupPath lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();

		assertEquals(lookupPath.isValid(), new Boolean(false));

		lookupPath.setValid(false);
		assertEquals(lookupPath.isValid(), new Boolean(false));

		lookupPath.setValid(true);
		assertEquals(lookupPath.isValid(), new Boolean(true));
	}

	public void testGetLookupPathSteps() throws SAFRException {
		dbStartup();

		LookupPath lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
		assertEquals(1, lookupPath.getLookupPathSteps().size());
		lookupPath.createStep();
		assertEquals(2, lookupPath.getLookupPathSteps().size());
		LookupPathStep step1 = lookupPath.getLookupPathSteps().get(0);
		LookupPathStep step2 = lookupPath.getLookupPathSteps().get(1);
		assertEquals(step2.getSourceLR(), step1.getTargetLR());

		LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(21);
		step1.setTargetLR(lr);
		assertEquals(step2.getSourceLR(), lr);

		lookupPath.removeStep(1);
		assertEquals(1, lookupPath.getLookupPathSteps().size());
	}

	public void testCheckSourceLRUsage() throws DAOException {
		dbStartup();
		Boolean correctException = false;
		Boolean noException = false;
		LookupPath parentLookup = null;
		LookupPathStep step1 = null;
		LookupPathStep step3 = null;
		LookupPathStep step2 = null;
		LogicalRecord st1srcLR = null;
		SAFRAssociationList<ComponentAssociation> lrlfAssociationList = null;
		try {
			parentLookup = SAFRApplication.getSAFRFactory().createLookupPath();
			step1 = parentLookup.getLookupPathSteps().get(0);
			st1srcLR = SAFRApplication.getSAFRFactory().getLogicalRecord(906);
			step1.setSourceLR(st1srcLR);
			LogicalRecord st1trgLR = SAFRApplication.getSAFRFactory()
					.getLogicalRecord(1278);
			step1.setTargetLR(st1trgLR);
			lrlfAssociationList = SAFRAssociationFactory
					.getLogicalRecordToLogicalFileAssociations(st1trgLR);
			ComponentAssociation LRLFAssociation = lrlfAssociationList.get(0);
			step1.setTargetLRLFAssociation(LRLFAssociation);

			step2 = parentLookup.createStep();
			LogicalRecord st2trgLR = SAFRApplication.getSAFRFactory()
					.getLogicalRecord(1235);
			step2.setTargetLR(st2trgLR);

			LookupPathSourceField step2SourceField = step2
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			step2SourceField.setSourceLRField(step2.getSourceLR().getLRFields()
					.get(0));
			step2SourceField.setSourceFieldSourceLR(step2.getSourceLR());
			step2SourceField.setSourceFieldLRLFAssociation(step2
					.getSourceLRLFAssociation());

			try {
				step1.setTargetLR(st1srcLR);
			} catch (SAFRException se) {
				if (se instanceof SAFRValidationException) {
					correctException = true;

				}
			}

			assertTrue(correctException);
			correctException = false;

			step3 = parentLookup.createStep();
			LogicalRecord st3trgLR = SAFRApplication.getSAFRFactory()
					.getLogicalRecord(916);
			step3.setTargetLR(st3trgLR);
			LookupPathSourceField step3SourceField = step3
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			step3SourceField.setSourceLRField(step2.getSourceLR().getLRFields()
					.get(0));
			step3SourceField.setSourceFieldSourceLR(step2.getSourceLR());
			step3SourceField.setSourceFieldLRLFAssociation(step2
					.getSourceLRLFAssociation());

			try {
				step1.setTargetLR(st1srcLR);
			} catch (SAFRException se) {
				if (se instanceof SAFRValidationException) {
					correctException = true;
				}
			}
			assertTrue(correctException);

			// changing type of step 2 source field.exception expected because
			// it is used in step 3.
			correctException = false;
			step2SourceField
					.setSourceFieldType(LookupPathSourceFieldType.CONSTANT);

			try {
				step1.setTargetLR(st1srcLR);
			} catch (SAFRException se) {
				if (se instanceof SAFRValidationException) {
					correctException = true;
				}
			}
			assertTrue(correctException);

			// changing the type of step 3 source field as constant. No
			// Exception expected.
			correctException = false;
			step3SourceField
					.setSourceFieldType(LookupPathSourceFieldType.CONSTANT);
			try {
				step1.setTargetLR(st1srcLR);
				noException = true;
			} catch (SAFRException se) {
				if (se instanceof SAFRValidationException) {
					correctException = true;
				}
			}
			assertTrue(noException);
			assertFalse(correctException);

		} catch (SAFRException e) {
			logger.log(Level.SEVERE, "", e);
		}

		// when LR/LF association pair is different, then no exception.

		try {
			correctException = false;
			noException = false;
			parentLookup.removeStep(2);
			parentLookup.removeStep(1);
			ComponentAssociation LRLFAssociation1 = lrlfAssociationList.get(1);
			step1.setTargetLRLFAssociation(LRLFAssociation1);

			LookupPathStep step4 = parentLookup.createStep();
			LogicalRecord st4trgLR = SAFRApplication.getSAFRFactory()
					.getLogicalRecord(1235);
			step4.setTargetLR(st4trgLR);

			LookupPathSourceField step4SourceField = step4
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			step4SourceField.setSourceLRField(step4.getSourceLR().getLRFields()
					.get(0));
			step4SourceField.setSourceFieldSourceLR(step4.getSourceLR());
			step4SourceField.setSourceFieldLRLFAssociation(step1
					.getSourceLRLFAssociation());
			step1.setTargetLR(st1srcLR);
			noException = true;

		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(noException);
		assertFalse(correctException);
	}

    public void testStoreActivate() throws SAFRException {

        dbStartup();

        LookupPath lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
        LogicalRecord sourceLR = SAFRApplication.getSAFRFactory().getLogicalRecord(1245);
        lookupPath.setSourceLR(sourceLR);
        lookupPath.setName("JUnitTestCase" + 999999);
        lookupPath.setValid(true);
        lookupPath.store();
        delIds.add(lookupPath.getId());

        assertNotNull(lookupPath.getActivatedTime());
        assertNotNull(lookupPath.getActivatedBy());
        
    }

    public void testStoreActivateUpdate() throws SAFRException, SQLException {

        dbStartup();

        LookupPath lookupPath = SAFRApplication.getSAFRFactory().getLookupPath(2062);
        Date modTime = lookupPath.getModifyTime();
        String modBy = lookupPath.getModifyBy(); 
        lookupPath.setValid(false);
        lookupPath.store();

        assertNull(lookupPath.getActivatedTime());
        assertNull(lookupPath.getActivatedBy());
        assertEquals(lookupPath.getModifyTime(), modTime);
        assertEquals(lookupPath.getModifyBy(), modBy);
        
        lookupPath.setValid(true);
        lookupPath.store();

        assertNotNull(lookupPath.getActivatedTime());
        assertNotNull(lookupPath.getActivatedBy());
        assertEquals(lookupPath.getModifyTime(), modTime);
        assertEquals(lookupPath.getModifyBy(), modBy);
        
        // cleanup
        Connection con = ((PGDAOFactory) DAOFactoryHolder.getDAOFactory()).getConnection();
        ConnectionParameters params = ((PGDAOFactory) DAOFactoryHolder.getDAOFactory()).getConnectionParameters();
        String updateStr = 
            "UPDATE " + params.getSchema() + ".LOOKUP " +
            "SET LASTACTTIMESTAMP=NULL,LASTACTUSERID=NULL,VALIDIND=0 " +
            "WHERE ENVIRONID=? AND LOOKUPID=?";
        PreparedStatement pst = con.prepareStatement(updateStr);
        pst.setInt(1, ENVIRONID);
        pst.setInt(2, 2062);
        pst.executeUpdate();
        
    }
    
	public void testStore() throws SAFRException {

		Boolean correctException = false;
		Boolean noException = false;
		LookupPath lookupPath = null;

		dbStartup();

		try {
			lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
			LogicalRecord sourceLR = SAFRApplication.getSAFRFactory()
					.getLogicalRecord(1245);
			lookupPath.setSourceLR(sourceLR);
			lookupPath.setName("JUnitTestCase" + 999999);
			lookupPath.store();
			noException = true;
		} catch (SAFRException e) {
			correctException = true;
		}

        assertNull(lookupPath.getActivatedTime());
        assertNull(lookupPath.getActivatedBy());
		
		assertFalse(correctException);
		assertTrue(noException);

		// when everything is set
		correctException = false;

		try {
			LogicalRecord sourceLR = SAFRApplication.getSAFRFactory()
					.getLogicalRecord(1245);

			lookupPath.setSourceLR(sourceLR);
			lookupPath.setValid(false);
			lookupPath.store();
		} catch (NullPointerException e1) {
			correctException = true;
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
		}

		assertFalse(correctException);
		Integer id = lookupPath.getId();

		// to test whether its getting stored correctly
		correctException = false;
		LogicalRecord sourceLR = null;
		try {
			lookupPath = SAFRApplication.getSAFRFactory().getLookupPath(id);
			LookupPathStep step1 = lookupPath.getLookupPathSteps().get(0);
			sourceLR = step1.getSourceLR();
		} catch (SAFRException e) {
			correctException = true;
			e.printStackTrace();
		}

		assertFalse(correctException);
		assertEquals(1, lookupPath.getLookupPathSteps().size());
		assertTrue(1245l == sourceLR.getId());

		// delete the Lookup Path
		SAFRApplication.getSAFRFactory().removeLookupPath(lookupPath.getId());

		// try updating the deleted lookup path
		lookupPath.setName("deletedLookupPath");
		correctException = false;
		try {
			lookupPath.store();
		} catch (SAFRException e) {
			e.printStackTrace();
			correctException = true;
		}
		assertTrue(correctException);

	}

	public void testStoreRemoveStep() throws SAFRException {
		dbStartup();

		LookupPath lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
		LogicalRecord sourceLR = SAFRApplication.getSAFRFactory().getLogicalRecord(1443);
		LogicalRecord targetLR = SAFRApplication.getSAFRFactory().getLogicalRecord(1444);
		LogicalRecord targetLR2 = SAFRApplication.getSAFRFactory().getLogicalRecord(1445);

		lookupPath.setName("Remove_Step");
		lookupPath.setValid(false);
		lookupPath.setSourceLR(sourceLR);
		lookupPath.changeStepTargetLRLFAssocistion(targetLR.getLogicalFileAssociations().get(0), 1);
		
		// Step 1
		LookupPathStep step1 = lookupPath.getLookupPathSteps().get(0);
		step1.setTargetLRLFAssociation(targetLR.getLogicalFileAssociations().get(0));
		LookupPathSourceField field = step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
		field.setSourceFieldLRLFAssociation(sourceLR.getLogicalFileAssociations().get(0));
		field.setSourceLRField(sourceLR.getLRFields().get(0));
		
		// Step 2
		LookupPathStep step2 = lookupPath.createStep();
		step2.setSourceLR(targetLR);
		step2.setTargetLRLFAssociation(targetLR2.getLogicalFileAssociations().get(0));
		lookupPath.changeStepTargetLRLFAssocistion(targetLR2.getLogicalFileAssociations().get(0), 2);
		field = step2.addSourceField(LookupPathSourceFieldType.LRFIELD);
		field.setSourceFieldLRLFAssociation(targetLR.getLogicalFileAssociations().get(0));
		field.setSourceLRField(targetLR.getLRFields().get(2));
		
		// store original LU
		lookupPath.validate();
		lookupPath.store();	
        delIds.add(lookupPath.getId());
		assertEquals(lookupPath.getLookupPathSteps().size(), 2);
		
		// remove step
		boolean deleted = lookupPath.removeStep(1);
		assertTrue(deleted);
		lookupPath.validate();
		lookupPath.store();	
		assertEquals(lookupPath.getLookupPathSteps().size(), 1);
	}

	public void testStoreNoPermissionCreate() throws SAFRException {
		
		dbStartup();		
		helper.setUser("NOPERM");
		
		LookupPath lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
		LogicalRecord sourceLR = SAFRApplication.getSAFRFactory().getLogicalRecord(1443);
		LogicalRecord targetLR = SAFRApplication.getSAFRFactory().getLogicalRecord(1444);

		lookupPath.setName("No_Create_Perm");
		lookupPath.setSourceLR(sourceLR);		
		lookupPath.changeStepTargetLRLFAssocistion(targetLR.getLogicalFileAssociations().get(0), 1);
		
		// Step 1
		LookupPathStep step1 = lookupPath.getLookupPathSteps().get(0);
		step1.setTargetLRLFAssociation(targetLR.getLogicalFileAssociations().get(0));
		LookupPathSourceField field = step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
		field.setSourceFieldLRLFAssociation(sourceLR.getLogicalFileAssociations().get(0));
		field.setSourceLRField(sourceLR.getLRFields().get(0));

		lookupPath.setValid(false);
		lookupPath.validate();
		try {
			lookupPath.store();
			fail();
		} catch (SAFRException e) {
			String errMsg = e.getMessage();
			assertEquals(errMsg,"The user is not authorized to create this lookup path.");
		}			
	}
	
	public void testStoreNoPermissionUpdate() throws SAFRException {
		dbStartup();
		helper.setUser("NOPERM");
		LookupPath lu = SAFRApplication.getSAFRFactory().getLookupPath(2062);
		lu.setName("No_Update_Perm");
		lu.validate();
		try {
			lu.store();
			fail();
		} catch (SAFRException e) {
			String errMsg = e.getMessage();
			assertEquals(errMsg,"The user is not authorized to update this lookup path.");
		}
	}
	
	public void testStoreDepViewDeact() throws SAFRException {
		dbStartup();
		
		LookupPath lu = SAFRApplication.getSAFRFactory().getLookupPath(2003);
		try {
			lu.validate();
			fail();
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "(?s)^.*Views.*TestLookupDep\\s*\\[8639\\].*$";
			assertTrue(errMsg.matches(pattern));						
		}
		lu.store();
		
		// confirm dependent view is deactivated
		View vw = SAFRApplication.getSAFRFactory().getView(8639);
        assertEquals(vw.getStatusCode(), SAFRApplication.getSAFRFactory()
                .getCodeSet(CodeCategories.VIEWSTATUS).getCode(Codes.INACTIVE));
        vw.activate();
        vw.store();
	}
		
	public void testMoveFieldUp() throws DAOException {
		dbStartup();
		Boolean correctException = false;

		try {
			LookupPath lookupPath = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			SAFRList<LookupPathStep> lookupPathSteps = lookupPath
					.getLookupPathSteps();
			LookupPathStep step1 = lookupPathSteps.get(0);
			LookupPathSourceField fld1 = step1
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			fld1.setName("fld1");
			LookupPathSourceField fld2 = step1
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			fld2.setName("fld2");
			LookupPathSourceField fld3 = step1
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			fld3.setName("fld3");

			assertEquals(step1.getSourceFields().get(0), fld1);
			assertEquals(step1.getSourceFields().get(0).getKeySeqNbr(),
					new Integer(1));
			step1.moveFieldUp(1);
			assertEquals(step1.getSourceFields().get(0), fld2);
			assertEquals(fld2.getKeySeqNbr(), new Integer(1));
			assertEquals(fld1.getKeySeqNbr(), new Integer(2));

		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
	}

	public void testMoveFieldDown() throws DAOException {
		dbStartup();
		Boolean correctException = false;
		try {
			LookupPath lookupPath = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			SAFRList<LookupPathStep> lookupPathSteps = lookupPath
					.getLookupPathSteps();
			LookupPathStep step1 = lookupPathSteps.get(0);
			LookupPathSourceField fld1 = step1
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			fld1.setName("fld1");
			LookupPathSourceField fld2 = step1
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			fld2.setName("fld2");
			LookupPathSourceField fld3 = step1
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			fld3.setName("fld3");

			assertEquals(step1.getSourceFields().get(0), fld1);
			assertEquals(step1.getSourceFields().get(0).getKeySeqNbr(),
					new Integer(1));
			step1.moveFieldDown(0);
			assertEquals(step1.getSourceFields().get(1), fld1);
			assertEquals(fld1.getKeySeqNbr(), new Integer(2));
			assertEquals(fld2.getKeySeqNbr(), new Integer(1));

		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
	}

	public void testValidate() throws DAOException {
		dbStartup();
		LookupPath lookupPath = null;
		LookupPathStep step1 = null;
		Boolean correctException = false;
		// name empty and single step error.
		try {
			lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();

			step1 = lookupPath.getLookupPathSteps().get(0);

			step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
			step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
			LookupPathSourceField sourceField = step1.getSourceFields().get(0);
			LookupPathSourceField sourceField1 = step1.getSourceFields().get(1);
			assertEquals(sourceField.getParentLookupPathStep(), step1);
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode("ALNUM");
			sourceField.setDataTypeCode(code);
			sourceField.setDecimals(5);
			sourceField.setLength(3);
			sourceField1.setDataTypeCode(code);
			sourceField1.setLength(398);

			lookupPath.validate();
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
				System.out.println(((SAFRValidationException) e)
						.getMessageString());

			}
		}
		assertTrue(correctException);

		// special characters.
		correctException = false;
		lookupPath.setName("!@#$^&&");
		try {
			lookupPath.validate();

		} catch (SAFRValidationException e) {
			correctException = true;
			System.out
					.println(((SAFRValidationException) e).getMessageString());
		} catch (SAFRException e) {
			logger.log(Level.SEVERE, "", e);
		}

		assertTrue(correctException);

		// multiple step error.
		try {
			correctException = false;
			LookupPathStep step2 = lookupPath.createStep();
			step2.addSourceField(LookupPathSourceFieldType.CONSTANT);
			LookupPathSourceField sourceField2 = step2.getSourceFields().get(0);
			sourceField2.setSourceValue("");
			lookupPath.validate();
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
				System.out.println(((SAFRValidationException) e)
						.getMessageString());

			}
		}
		assertTrue(correctException);

		try {
			correctException = false;
			LookupPathStep step3 = lookupPath.createStep();
			step3.addSourceField(LookupPathSourceFieldType.LRFIELD);
			step3.addSourceField(LookupPathSourceFieldType.LRFIELD);
			LookupPathSourceField sourceFiel3 = step3.getSourceFields().get(0);
			LookupPathSourceField sourceFiel4 = step3.getSourceFields().get(1);
			Code code = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode("ALNUM");
			sourceFiel3.setDataTypeCode(code);
			sourceFiel3.setDecimals(5);
			sourceFiel4.setDataTypeCode(code);
			sourceFiel4.setLength(277);

			lookupPath.validate();

		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
				System.out.println(((SAFRValidationException) e)
						.getMessageString());

			}
		}
		assertTrue(correctException);

		// check valid
		correctException = false;
		LogicalRecord srcLr;
		LogicalRecord trgLr;
		try {
			lookupPath.setName("Test_1");
			srcLr = SAFRApplication.getSAFRFactory().getLogicalRecord(1245);
			trgLr = SAFRApplication.getSAFRFactory().getLogicalRecord(1247);
			step1.setSourceLR(srcLr);
			step1.setTargetLR(trgLr);
			assertNotNull(trgLr.getLogicalFileAssociations().get(0));
			step1.setTargetLRLFAssociation(trgLr.getLogicalFileAssociations()
					.get(0));
			assertNotNull(step1.getTargetLRLFAssociation());
			LookupPathSourceField sourceField = step1
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			LRField lrField = srcLr.getLRFields().get(0);
			sourceField.setSourceFieldSourceLR(srcLr);
			lrField.setLength(6);
			sourceField.setSourceLRField(lrField);

			lookupPath.validate();

		} catch (SAFRValidationException e) {
			if (e.getSafrValidationType() == SAFRValidationType.WARNING) {
				correctException = true;
				System.out.println(((SAFRValidationException) e)
						.getMessageString());
			}

		} catch (SAFRException e) {
			logger.log(Level.SEVERE, "", e);
		}

	}

	public void testValidateLongName() throws SAFRException {
		dbStartup();

		LookupPath lu = SAFRApplication.getSAFRFactory().getLookupPath(2004);
		lu.setName("012345678901234567890123456789012345678901234567890123456789");
		
		try {
			lu.validate();
			fail();			
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "^The length of Lookup Path name '012345678901234567890123456789012345678901234567890123456789' cannot exceed 48 characters\\.*$";
			assertTrue(errMsg.matches(pattern));						
		}
		
	}
	
	public void testValidateDuplicate() throws SAFRException {
		dbStartup();
		
		LookupPath lu = SAFRApplication.getSAFRFactory().createLookupPath();
		lu.setName("TestLookupPathViewDependencies");
		
		try {
			lu.validate();
			fail();			
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "The Lookup Path name 'TestLookupPathViewDependencies' already exists\\. Please specify a different name\\.";
			assertTrue(errMsg.matches(pattern));						
		}
	}
	
	public void testValidateDepView() throws SAFRException {
		dbStartup();
		
		LookupPath lu = SAFRApplication.getSAFRFactory().getLookupPath(2003);
		lu.setName("Testdep");
		
		try {
			lu.validate();
			fail();			
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "(?s)^.*Views.*TestLookupDep\\s*\\[8639\\].*$";
			assertTrue(errMsg.matches(pattern));						
		}
		
	}
	
	public void testSaveAs() throws SAFRValidationException, SAFRException {
		dbStartup();

		LookupPath lookup1 = SAFRApplication.getSAFRFactory().getLookupPath(2004);

		// Copy of LookupPath one.
		LookupPath lookup2 = (LookupPath) lookup1.saveAs("TestSaveAsCopy_LookupPath1");
		// Testing the general Properties.
		assertTrue(lookup2.getId() > 0);
        delIds.add(lookup2.getId());
		assertEquals("TestSaveAsCopy_LookupPath1", lookup2.getName());
		assertEquals(lookup1.getComment(), lookup2.getComment());
		assertEquals(lookup1.isValid(), lookup2.isValid());
		// Step one of the lookupPath one.
		LookupPathStep lukp1Step1 = lookup1.getLookupPathSteps().get(0);

		// Step one of the lookupPath two.
		LookupPathStep lukp2Step1 = lookup2.getLookupPathSteps().get(0);
		// Testing the Step one Properties.
		assertEquals(lukp1Step1.getName(), lukp2Step1.getName());
		assertEquals(lukp1Step1.getSequenceNumber(), lukp2Step1
				.getSequenceNumber());
		assertEquals(lukp1Step1.getSourceLR().getName(), lukp2Step1
				.getSourceLR().getName());
		assertEquals(lukp1Step1.getSourceLength(), lukp2Step1.getSourceLength());
		assertEquals(lukp1Step1.getTargetLength(), lukp2Step1.getTargetLength());
		assertEquals(lukp1Step1.getTargetLR().getName(), lukp2Step1
				.getTargetLR().getName());
		// SourceField of LookupPath one First Step.
		LookupPathSourceField lukp1SrcFld = lukp1Step1.getSourceFields().get(0);
		// SourceField of LookupPath two First Step.
		LookupPathSourceField lukp2SrcFld = lukp2Step1.getSourceFields().get(0);
		assertEquals(lukp1SrcFld.getName(), lukp2SrcFld.getName());
		assertEquals(lukp1SrcFld.getDefaultValue(), lukp2SrcFld
				.getDefaultValue());
		assertEquals(lukp1SrcFld.getHeading1(), lukp2SrcFld.getHeading1());
		assertEquals(lukp1SrcFld.getHeading2(), lukp2SrcFld.getHeading2());
		assertEquals(lukp1SrcFld.getHeading3(), lukp2SrcFld.getHeading3());
		assertEquals(lukp1SrcFld.getSortKeyLabel(), lukp2SrcFld
				.getSortKeyLabel());
		assertEquals(lukp1SrcFld.getSourceValue(), lukp2SrcFld.getSourceValue());
		assertEquals(lukp1SrcFld.getSubtotalLabel(), lukp2SrcFld
				.getSubtotalLabel());
		assertEquals(lukp1SrcFld.getSymbolicName(), lukp2SrcFld
				.getSymbolicName());
		assertEquals(lukp1SrcFld.getDataTypeCode(), lukp2SrcFld.getDataTypeCode());
		assertEquals(lukp1SrcFld.getDateTimeFormatCode(), lukp2SrcFld
				.getDateTimeFormatCode());
		assertEquals(lukp1SrcFld.getDecimals(), lukp2SrcFld.getDecimals());
		assertEquals(lukp1SrcFld.getHeaderAlignmentCode(), lukp2SrcFld
				.getHeaderAlignmentCode());
		assertEquals(lukp1SrcFld.getKeySeqNbr(), lukp2SrcFld.getKeySeqNbr());
		assertEquals(lukp1SrcFld.getLength(), lukp2SrcFld.getLength());
		assertEquals(lukp1SrcFld.getScaling(), lukp2SrcFld.getScaling());
		assertEquals(lukp1SrcFld.getSourceFieldType(), lukp2SrcFld
				.getSourceFieldType());
		assertEquals(lukp1SrcFld.isSigned(), lukp2SrcFld.isSigned());

	}
	
	
}
