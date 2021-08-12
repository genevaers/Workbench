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

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.LookupPathSourceFieldType;
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

public class TestLookupPathStep extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestLookupPathStep");

	static String TABLE_NAME = "x_jointargetlrtbl";
	static String COL_ID = "XJOINSTEPID";

	static String DEP_TABLE_LIST[] = { "e_joinsourcekeytbl", TABLE_NAME };
	TestDataLayerHelper helper = new TestDataLayerHelper();
    List<Integer> delIds = new ArrayList<Integer>();

	public void setUp() {
	}

	public void dbStartup() {
		try {
			helper.initDataLayer();
		} catch (DAOException e) {
			assertFalse(true);
		}
	}

	public void removeLookupPathStep(Integer id) {

		try {
			List<String> idnames = new ArrayList<String>();
			idnames.add(COL_ID);

			PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder
					.getDAOFactory();
			ConnectionParameters params = fact.getConnectionParameters();
			SQLGenerator generator = new SQLGenerator();

			for (String tableName : DEP_TABLE_LIST) {
				try {
					String statement = generator.getDeleteStatement(params
							.getSchema(), tableName, idnames);
					PreparedStatement pst = fact.getConnection()
							.prepareStatement(statement);
					pst.setInt(1, id);
					pst.execute();
					pst.close();
				} catch (Exception e) {
					assertTrue(false);
					e.printStackTrace();
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
                    removeLookupPathStep(i);
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
	
	public void testMoveFieldUpDown() throws SAFRException{
		dbStartup();
		LookupPath lkup_path = SAFRApplication.getSAFRFactory().getLookupPath(2011);
		
		LookupPathStep lkup_step = lkup_path.getLookupPathSteps().get(0);
		
		/* move field down */
		lkup_step.moveFieldDown(0);
		lkup_path.store();
		
		/* Test result */
		List<LookupPathSourceField> lkup_step_down = lkup_step.getSourceFields().getActiveItems();
		assertEquals(new Integer(6),lkup_step_down.get(0).getLength());
		
		/*move field up */
		lkup_step.moveFieldUp(1);
		lkup_path.store();
		
		/* Test result */
		List<LookupPathSourceField> lkup_step_up = lkup_step.getSourceFields().getActiveItems();
		assertEquals(new Integer(10),lkup_step_up.get(0).getLength());
		
	}

	// Tests the getSequenceNumber and setSequenceNumber data.

	public void testGetSetSequenceNumber() throws DAOException {
		dbStartup();
		Boolean correctException = false;

		try {
			LookupPath parentLookup = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			LookupPathStep step1 = parentLookup.getLookupPathSteps().get(0);
			assertTrue(step1.getSequenceNumber() == 1);
			LookupPathStep step2 = parentLookup.createStep();
			assertTrue(step2.getSequenceNumber() == 2);
			LookupPathStep step3 = parentLookup.createStep();
			assertEquals(step3.getSequenceNumber(), new Integer(3));
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
	}

	public void testGetParentLookupPath() throws DAOException {
		dbStartup();
		LookupPath parentLookupPath = null;
		LookupPathStep step1 = null;
		LookupPathStep step2 = null;
		Boolean correctException = false;
		try {
			parentLookupPath = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			step1 = parentLookupPath.getLookupPathSteps().get(0);
			assertEquals(step1.getParentLookupPath(), parentLookupPath);
			step2 = parentLookupPath.createStep();
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertEquals(step2.getParentLookupPath(), parentLookupPath);
		assertFalse(correctException);
	}

	public void testgetSourceFields() throws DAOException {
		dbStartup();
		LookupPath parentLookup = null;
		Boolean correctException = false;
		try {
			parentLookup = SAFRApplication.getSAFRFactory().createLookupPath();
			LookupPathStep step1 = parentLookup.getLookupPathSteps().get(0);
			LookupPathSourceField field1 = step1
					.addSourceField(LookupPathSourceFieldType.CONSTANT);
			assertTrue(step1.getSourceFields().get(0).getSourceFieldType()
					.equals(LookupPathSourceFieldType.CONSTANT));
			LookupPathSourceField field2 = step1
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			assertTrue(step1.getSourceFields().get(1).getSourceFieldType()
					.equals(LookupPathSourceFieldType.LRFIELD));
			assertTrue(step1.getSourceFields().getActiveItems().size() == 2);
			step1.deleteSourceField(field1);
			assertTrue(step1.getSourceFields().getActiveItems().size() == 1);
			step1.deleteSourceField(field2);
			assertTrue(step1.getSourceFields().getActiveItems().size() == 0);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);

	}

	public void testGetSetSourceLR() throws DAOException {
		LogicalRecord lr = null;
		LogicalRecord lr1 = null;
		LogicalRecord lr2 = null;
		LookupPathStep step2 = null;
		Boolean correctException = false;
		LookupPath parentLookup = null;
		LookupPathStep step1 = null;
		dbStartup();

		try {
			parentLookup = SAFRApplication.getSAFRFactory().createLookupPath();
			step1 = parentLookup.getLookupPathSteps().get(0);
			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1245);
			lr1 = SAFRApplication.getSAFRFactory().getLogicalRecord(1243);
			step1.setSourceLR(lr);
			step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
		try {
			LookupPathSourceField srcfld = step1.getSourceFields().get(0);
			LRField lrField = lr.getLRFields().get(0);
			srcfld.setSourceLRField(lrField);
			srcfld.setSourceFieldSourceLR(lr);
			step1.setSourceLR(lr1);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
			}
		}
		assertTrue(correctException);

		// if step 2 source LR is changed.

		correctException = false;
		try {
			step2 = parentLookup.createStep();
			lr2 = SAFRApplication.getSAFRFactory().getLogicalRecord(1247);
			step2.setSourceLR(lr2);
			step2.addSourceField(LookupPathSourceFieldType.LRFIELD);
			LookupPathSourceField srcfld1 = step2.getSourceFields().get(0);
			srcfld1.setSourceLRField(lr2.getLRFields().get(0));
			srcfld1.setSourceFieldSourceLR(lr2);
			step2.setSourceLR(lr1);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
			}

			assertTrue(correctException);
		}
	}

	public void testGetSetTargetLRLFAssociation() throws DAOException {
		LogicalRecord lr = null;
		LogicalRecord lr1 = null;
		LookupPathStep step1 = null;
		dbStartup();
		LookupPath lookupPath;
		Boolean correctException = false;
		try {
			lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
			SAFRList<LookupPathStep> lookupPathSteps = lookupPath
					.getLookupPathSteps();
			step1 = lookupPathSteps.get(0);
			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1245);
			SAFRAssociationList<ComponentAssociation> targetLRLFAssociation = SAFRAssociationFactory
					.getLogicalRecordToLogicalFileAssociations(lr);
			step1.setTargetLRLFAssociation(targetLRLFAssociation.get(0));
			assertEquals(step1.getTargetLRLFAssociation(),
					targetLRLFAssociation.get(0));
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);

		// check for dependency.
		try {
			correctException = false;
			lr1 = SAFRApplication.getSAFRFactory().getLogicalRecord(1243);
			step1.setSourceLR(lr);
			step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
			LookupPathSourceField srcfld = step1.getSourceFields().get(0);
			LRField lrField = lr.getLRFields().get(0);
			srcfld.setSourceLRField(lrField);
			srcfld.setSourceFieldSourceLR(lr);
			step1.setSourceLR(lr1);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
			}
		}
		assertTrue(correctException);

	}

	public void testGetSetTargetLR() throws DAOException {
		LogicalRecord lr = null;
		LookupPath lookupPath = null;
		SAFRList<LookupPathStep> lookupPathSteps = null;
		LookupPathStep step1 = null;
		Boolean correctException = false;
		dbStartup();
		try {
			lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
			lookupPathSteps = lookupPath.getLookupPathSteps();
			step1 = lookupPathSteps.get(0);

			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1245);
			step1.setTargetLR(lr);
			assertEquals(step1.getTargetLR(), lr);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);

	}

	public void testGetSetSourceLRLFAssociation() throws DAOException {
		LogicalRecord lr = null;
		Boolean correctException = false;
		dbStartup();
		try {
			LookupPath lookupPath = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			SAFRList<LookupPathStep> lookupPathSteps = lookupPath
					.getLookupPathSteps();
			LookupPathStep step1 = lookupPathSteps.get(0);
			assertEquals(step1.getSourceLRLFAssociation(), null);
			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1245);
			SAFRAssociationList<ComponentAssociation> sourceLRLFAssociation = SAFRAssociationFactory
					.getLogicalRecordToLogicalFileAssociations(lr);
			step1.setSourceLRLFAssociation(sourceLRLFAssociation.get(0));
			assertEquals(step1.getSourceLRLFAssociation(),
					sourceLRLFAssociation.get(0));
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
	}

	public void testAddDeleteSourceField() throws SAFRException {
		dbStartup();

		LookupPath lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
		SAFRList<LookupPathStep> lookupPathSteps = lookupPath.getLookupPathSteps();
		LookupPathStep step1 = lookupPathSteps.get(0);
		step1.addSourceField(LookupPathSourceFieldType.CONSTANT);
		assertEquals(step1.getSourceFields().get(0).getSourceFieldType(), LookupPathSourceFieldType.CONSTANT);
		step1.deleteSourceField(step1.getSourceFields().get(0));
		assertEquals(step1.getSourceFields().size(), 0);

	}

	public void testCheckValid() throws SAFRException {
		LogicalRecord srcLr = null;
		LogicalRecord srcLr1 = null;
		LogicalRecord trgLr = null;
		Boolean correctException = false;
		dbStartup();
	
		LookupPath lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();

		correctException = false;
		SAFRList<LookupPathStep> lookupPathSteps = lookupPath
				.getLookupPathSteps();
		LookupPathStep step1 = lookupPathSteps.get(0);
		try {
			step1.checkValid();
		} catch (SAFRValidationException sve) {
			correctException = true;
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(correctException);

		// check for complete but the lengths of target and source Lr are
		// different.
		try {
			correctException = false;
			srcLr = SAFRApplication.getSAFRFactory().getLogicalRecord(1245);
			trgLr = SAFRApplication.getSAFRFactory().getLogicalRecord(1247);
			step1.setSourceLR(srcLr);
			step1.setTargetLR(trgLr);
			assertNotNull(trgLr.getLogicalFileAssociations().get(0));
			step1.setTargetLRLFAssociation(trgLr.getLogicalFileAssociations()
					.get(0));
			assertNotNull(step1.getTargetLRLFAssociation());
			step1.checkValid();

		} catch (SAFRValidationException e) {
			correctException = true;
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(trgLr.getPrimayKeyLength() > 0);
		assertTrue(correctException);

		// check for no exception

		try {
			correctException = false;
			srcLr1 = SAFRApplication.getSAFRFactory().getLogicalRecord(1306);
			trgLr = SAFRApplication.getSAFRFactory().getLogicalRecord(1247);
			step1.setSourceLR(srcLr1);
			step1.setTargetLR(trgLr);
			assertNotNull(trgLr.getLogicalFileAssociations().get(0));
			step1.setTargetLRLFAssociation(trgLr.getLogicalFileAssociations()
					.get(0));
			assertNotNull(step1.getTargetLRLFAssociation());
			LookupPathSourceField sourceField = step1
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			sourceField.setLength(30);
			step1.checkValid();
			
		} catch (SAFRValidationException e) {
			correctException = true;
		} catch (SAFRException e) {
			correctException = true;
		}
		assertTrue(trgLr.getPrimayKeyLength() > 0);
		assertFalse(correctException);
	}

	public void testCheckComplete() throws DAOException {
		LogicalRecord srcLr = null;
		LogicalRecord trgLr = null;
		dbStartup();
		Boolean correctException = false;
		Boolean noException = false;
		LookupPath lookupPath = null;
		SAFRList<LookupPathStep> lookupPathSteps = null;
		LookupPathStep step1 = null;

		try {
			lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
			lookupPathSteps = lookupPath.getLookupPathSteps();
			step1 = lookupPathSteps.get(0);
			step1.checkComplete();
		} catch (SAFRValidationException sve) {
			correctException = true;
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(correctException);

		try {
			correctException = false;
			srcLr = SAFRApplication.getSAFRFactory().getLogicalRecord(1245);
			trgLr = SAFRApplication.getSAFRFactory().getLogicalRecord(1247);
			step1.setSourceLR(srcLr);
			step1.setTargetLR(trgLr);
			assertNotNull(trgLr.getLogicalFileAssociations().get(0));
			step1.setTargetLRLFAssociation(trgLr.getLogicalFileAssociations()
					.get(0));
			assertNotNull(step1.getTargetLRLFAssociation());

			step1.checkComplete();
			noException = true;
		} catch (SAFRException e) {
			correctException = true;
		}
		assertTrue(trgLr.getPrimayKeyLength() > 0);
		assertTrue(noException);
		assertFalse(correctException);
	}

	public void testGetSourceLength() throws DAOException {
		dbStartup();
		Boolean correctException = false;
		try {
			LookupPath lookupPath = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			SAFRList<LookupPathStep> lookupPathSteps = lookupPath
					.getLookupPathSteps();
			LookupPathStep step1 = lookupPathSteps.get(0);
			assertTrue(step1.getSourceLength() == 0);
			step1.addSourceField(LookupPathSourceFieldType.CONSTANT).setLength(
					10);
			assertTrue(step1.getSourceLength() == 10);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
	}

	public void testGetTargetLength() throws DAOException {
		LogicalRecord lr = null;
		dbStartup();
		Boolean correctException = false;
		try {
			LookupPath lookupPath = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			SAFRList<LookupPathStep> lookupPathSteps = lookupPath
					.getLookupPathSteps();
			LookupPathStep step1 = lookupPathSteps.get(0);
			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1245);
			assertEquals(step1.getTargetLR(), null);

			step1.setTargetLR(lr);

			assertEquals(step1.getTargetLR(), lr);
			assertEquals(step1.getTargetLength(), lr.getPrimayKeyLength()
					.intValue());
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
	}

    public void testStore() throws DAOException {
        dbStartup();
        LookupPath lookupPath = null;
        LookupPathStep stepNext = null;

        try {
            lookupPath = SAFRApplication.getSAFRFactory().getLookupPath(1956);
        } catch (SAFRException e1) {
            e1.printStackTrace();
            fail();
        }

        // try to store it...
        SAFRList<LookupPathStep> lookupPathSteps = lookupPath
                .getLookupPathSteps();
        int numSteps = lookupPathSteps.size();
        try {
            LogicalRecord logicalRecord = SAFRApplication.getSAFRFactory().getLogicalRecord(22);
            SAFRList<ComponentAssociation> logicalFileAssociations = logicalRecord
                .getLogicalFileAssociations();
            stepNext = lookupPath.createStep();
            stepNext.setSourceLR(logicalRecord);
            stepNext.setTargetLRLFAssociation(logicalFileAssociations.get(0));
            assertEquals(lookupPath.getLookupPathSteps().get(numSteps),stepNext);
            LookupPathSourceField srcFld1 = stepNext.addSourceField(LookupPathSourceFieldType.LRFIELD);
            SAFRList<LRField> lrFileds = logicalRecord.getLRFields();
            LRField lrField = lrFileds.get(0);

            srcFld1.setSourceFieldSourceLR(logicalRecord);
            srcFld1.setSourceLRField(lrField);
            srcFld1.setSourceFieldLRLFAssociation(null);
            LookupPathSourceField srcFld2 = stepNext
                    .addSourceField(LookupPathSourceFieldType.CONSTANT);
            srcFld2.setSourceValue("value");
            lookupPath.store();
            
        } catch (SAFRException e) {
            e.printStackTrace();
            fail();
        }

        LookupPathSourceField sourceFld1 = null;
        LookupPathSourceField sourceFld2 = null;
        try {

            lookupPath = SAFRApplication.getSAFRFactory().getLookupPath(1956);
            lookupPathSteps = lookupPath.getLookupPathSteps();
            stepNext = lookupPathSteps.get(1);
            sourceFld1 = stepNext.getSourceFields().get(0);
            sourceFld2 = stepNext.getSourceFields().get(1);
            assertEquals(LookupPathSourceFieldType.LRFIELD, sourceFld1
                    .getSourceFieldType());
            LogicalRecord lr1 = sourceFld1.getSourceFieldSourceLR();
            LRField lrField = lr1.getLRFields().get(0);
            assertEquals("DEPT_ID", lrField.getName());
            assertEquals(LookupPathSourceFieldType.CONSTANT, sourceFld2
                    .getSourceFieldType());
            assertEquals("value", sourceFld2.getSourceValue());
            
            lookupPath.removeStep(1);
            lookupPath.store();

        } catch (SAFRException e) {
            e.printStackTrace();
            fail();
        }

    }

	public void testValidate() throws DAOException {
		dbStartup();
		LookupPath lookupPath = null;
		Boolean correctException = false;
		try {
			lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();

			LookupPathStep step1 = lookupPath.getLookupPathSteps().get(0);
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

			step1.validate();
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
			LookupPathStep step2 = lookupPath.createStep();
			step2.addSourceField(LookupPathSourceFieldType.CONSTANT);
			LookupPathSourceField sourceField2 = step2.getSourceFields().get(0);
			sourceField2.setSourceValue("");
			step2.validate();
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
				System.out.println(((SAFRValidationException) e)
						.getMessageString());

			}
		}
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

			step3.validate();
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
				System.out.println(((SAFRValidationException) e)
						.getMessageString());

			}
		}
		assertTrue(correctException);
	}
}
