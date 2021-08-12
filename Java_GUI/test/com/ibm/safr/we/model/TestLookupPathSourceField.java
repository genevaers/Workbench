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

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.LookupPathSourceFieldType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;

public class TestLookupPathSourceField extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestLookupPathSourceField");

	static String TABLE_NAME = "LOOKUPSRCKEY";
	static String COL_ID = "LOOKUPID";
	static String DEP_TABLE_LIST[] = { TABLE_NAME };
	TestDataLayerHelper helper = new TestDataLayerHelper();

	public void setUp() {
	}

	public void dbStartup() {
		helper.initDataLayer();
	}

	public void tearDown() {
		helper.closeDataLayer();
	}

	public void testGetSetSourceFieldType() throws DAOException {
		dbStartup();
		Boolean correctException = false;
		LookupPath lookupPath = null;
		SAFRList<LookupPathStep> lookupPathSteps = null;
		LookupPathStep step1 = null;
		try {
			lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
			lookupPathSteps = lookupPath.getLookupPathSteps();
			step1 = lookupPathSteps.get(0);
			step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
			SAFRList<LookupPathSourceField> sourceFields = step1
					.getSourceFields();
			LookupPathSourceField sourceField = sourceFields.get(0);

			sourceField.setSourceFieldType(LookupPathSourceFieldType.CONSTANT);
			assertEquals(sourceField.getSourceFieldType(),
					LookupPathSourceFieldType.CONSTANT);

			sourceField.setSourceFieldType(LookupPathSourceFieldType.LRFIELD);
			assertEquals(sourceField.getSourceFieldType(),
					LookupPathSourceFieldType.LRFIELD);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}

		assertFalse(correctException);

	}

	public void testGetSetSourceLRField() throws DAOException {
		LogicalRecord lr = null;
		Boolean correctException = false;
		dbStartup();
		LookupPath lookupPath = null;
		SAFRList<LookupPathStep> lookupPathSteps = null;
		LookupPathStep step1 = null;
		try {
			lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
			lookupPathSteps = lookupPath.getLookupPathSteps();
			step1 = lookupPathSteps.get(0);
			step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
			SAFRList<LookupPathSourceField> sourceFields = step1
					.getSourceFields();
			LookupPathSourceField sourceField = sourceFields.get(0);
			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1245);
			SAFRList<LRField> lrFileds = lr.getLRFields();
			LRField lrField = lrFileds.get(0);

			sourceField.setSourceLRField(lrField);
			assertEquals(sourceField.getSourceLRField(), lrField);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
	}

	public void testGetSourceFieldSourceLR() throws DAOException {
		LogicalRecord lr = null;
		Boolean correctException = false;
		dbStartup();
		try {
			LookupPath lookupPath = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			SAFRList<LookupPathStep> lookupPathSteps = lookupPath
					.getLookupPathSteps();
			LookupPathStep step1 = lookupPathSteps.get(0);
			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1245);
			step1.setSourceLR(lr);

			LookupPathSourceField sourceField = step1
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			sourceField.setSourceLRField(lr.getLRFields().get(0));
			sourceField.setSourceFieldSourceLR(lr);

			assertEquals(sourceField.getSourceFieldSourceLR(), lr);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);

	}

	public void testGetSetSourceFieldLRLFAssociation() throws DAOException {
		LogicalRecord lr = null;
		Boolean correctException = false;
		dbStartup();
		try {
			LookupPath lookupPath = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			SAFRList<LookupPathStep> lookupPathSteps = lookupPath
					.getLookupPathSteps();
			LookupPathStep step1 = lookupPathSteps.get(0);
			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1245);
			step1.setSourceLR(lr);
			ComponentAssociation sourceFieldLRLFAssociation = SAFRAssociationFactory
					.getLogicalRecordToLogicalFileAssociations(lr).get(0);
			LookupPathSourceField sourceField = step1
					.addSourceField(LookupPathSourceFieldType.LRFIELD);
			sourceField
					.setSourceFieldLRLFAssociation(sourceFieldLRLFAssociation);
			assertEquals(sourceField.getSourceFieldLRLFAssociation(),
					sourceFieldLRLFAssociation);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
	}

	public void testGetSetKeySeqNbr() throws DAOException {
		dbStartup();
		Boolean correctException = false;
		try {
			LookupPath lookupPath = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			LookupPathStep step1 = lookupPath.getLookupPathSteps().get(0);
			step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
			LookupPathSourceField sourceField = step1.getSourceFields().get(0);
			sourceField.setKeySeqNbr(1);
			assertEquals(sourceField.getKeySeqNbr(), new Integer(1));
			sourceField.setKeySeqNbr(1);
			assertFalse(sourceField.getKeySeqNbr() != 1l);
			sourceField.setKeySeqNbr(2);
			assertTrue(sourceField.getKeySeqNbr() == 2l);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);

	}

	public void testGetSetSymbolicName() throws DAOException {
		dbStartup();
		Boolean correctException = false;
		try {
			LookupPath lookupPath = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			LookupPathStep step1 = lookupPath.getLookupPathSteps().get(0);
			step1.addSourceField(LookupPathSourceFieldType.SYMBOL);
			LookupPathSourceField sourceField = step1.getSourceFields().get(0);
			sourceField.setSymbolicName("SYMBOLIC NAME");
			assertTrue(sourceField.getSymbolicName().equals("SYMBOLIC NAME"));
			sourceField.setSymbolicName("syMbolic");
			assertEquals(sourceField.getSymbolicName(), "syMbolic");
			sourceField.setSymbolicName("syMbolic");
			assertFalse(sourceField.getSymbolicName() != "syMbolic");
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
	}

	public void testGetSetSourceValue() throws DAOException {
		dbStartup();
		Boolean correctException = false;
		try {
			LookupPath lookupPath = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			LookupPathStep step1 = lookupPath.getLookupPathSteps().get(0);
			step1.addSourceField(LookupPathSourceFieldType.CONSTANT);
			LookupPathSourceField sourceField = step1.getSourceFields().get(0);
			sourceField.setSourceValue("SOURCE VALUE");
			assertTrue(sourceField.getSourceValue().equals("SOURCE VALUE"));
			sourceField.setSourceValue("SOUrce VALUE");
			assertFalse(sourceField.getSourceValue() != "SOUrce VALUE");
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
	}

	public void testGetParentLookupPathStep() throws DAOException {
		dbStartup();
		Boolean correctException = false;
		try {
			LookupPath lookupPath = SAFRApplication.getSAFRFactory()
					.createLookupPath();
			LookupPathStep step1 = lookupPath.getLookupPathSteps().get(0);
			step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
			LookupPathSourceField sourceField = step1.getSourceFields().get(0);
			assertEquals(sourceField.getParentLookupPathStep(), step1);
			lookupPath.createStep();
			LookupPathStep step2 = lookupPath.getLookupPathSteps().get(1);
			step2.addSourceField(LookupPathSourceFieldType.CONSTANT);
			LookupPathSourceField sourceField2 = step2.getSourceFields().get(0);
			assertTrue(sourceField2.getParentLookupPathStep() == step2);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);

	}

	public void testValidate() throws DAOException {
		dbStartup();
		LookupPath lookupPath = null;
		Boolean correctException = false;
		Boolean noException = false;
		LookupPathStep step1 = null;
		LookupPathSourceField sourceField = null;
		Code code = null;

		try {

			lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
			step1 = lookupPath.getLookupPathSteps().get(0);
			step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
			sourceField = step1.getSourceFields().get(0);
			assertEquals(sourceField.getParentLookupPathStep(), step1);
			code = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode("ALNUM");
			sourceField.setDataTypeCode(code);
			sourceField.setDecimals(5);
			sourceField.validate();
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
				System.out.println(((SAFRValidationException) e)
						.getMessageString());
			}
		}
		assertTrue(correctException);

		// check for null LR field
		correctException = false;
		try {

			lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
			step1 = lookupPath.getLookupPathSteps().get(0);
			step1.setSourceLR(SAFRApplication.getSAFRFactory()
					.getLogicalRecord(1));
			step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
			sourceField = step1.getSourceFields().get(0);
			assertEquals(sourceField.getParentLookupPathStep(), step1);
			sourceField.setSourceLRField(null);
			sourceField.validate();
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
				System.out.println(((SAFRValidationException) e)
						.getMessageString());
			}
		}
		assertTrue(correctException);

		// check if LR field is not null
		correctException = false;
		try {

			lookupPath = SAFRApplication.getSAFRFactory().createLookupPath();
			step1 = lookupPath.getLookupPathSteps().get(0);
			step1.setSourceLR(SAFRApplication.getSAFRFactory()
					.getLogicalRecord(1));
			step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
			sourceField = step1.getSourceFields().get(0);
			assertEquals(sourceField.getParentLookupPathStep(), step1);
			sourceField.setSourceLRField(step1.getSourceLR().getLRFields().get(
					0));
			sourceField.validate();
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
				System.out.println(((SAFRValidationException) e)
						.getMessageString());
			}
		}
		assertFalse(correctException);

		// check for constant type of source field.
		correctException = false;
		try {
			sourceField.setSourceFieldType(LookupPathSourceFieldType.CONSTANT);
			sourceField.setSourceValue("");
			sourceField.validate();
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
				System.out.println(((SAFRValidationException) e)
						.getMessageString());

			}
		}
		assertTrue(correctException);

		// check for symbol type of source field.s
		correctException = false;
		try {
			sourceField.setSourceFieldType(LookupPathSourceFieldType.SYMBOL);
			sourceField.setSymbolicName("");
			sourceField.validate();
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
				System.out.println(((SAFRValidationException) e)
						.getMessageString());

			}
		}
		assertTrue(correctException);

		// no exception
		correctException = false;
		LookupPathSourceField sourceField2 = null;

		try {
			step1.addSourceField(LookupPathSourceFieldType.LRFIELD);
			sourceField2 = step1.getSourceFields().get(1);
			sourceField2.setSourceLRField(step1.getSourceLR().getLRFields()
					.get(0));
			sourceField2.setDataTypeCode(code);
			sourceField2.setLength(5);
			sourceField2.setName("fld1");
			sourceField2.validate();
			noException = true;
		} catch (SAFRException e) {
			correctException = true;
		}

		assertTrue(noException);
		assertFalse(correctException);
		assertEquals(sourceField2.getName(), "fld1");
		assertEquals(sourceField2.getLength(), new Integer(5));

		// no exception for constant type of source field.
		correctException = false;
		try {
			sourceField2.setSourceFieldType(LookupPathSourceFieldType.CONSTANT);
			sourceField2.setSourceValue("value");
			sourceField2.validate();
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
			}

			assertTrue(correctException);
			assertEquals(sourceField2.getSourceValue(), "value");
			assertEquals(sourceField2.getSourceFieldType(),
					LookupPathSourceFieldType.CONSTANT);
		}

		// no exception for symbol type of source field.
		correctException = false;
		try {
			sourceField2.setSourceFieldType(LookupPathSourceFieldType.SYMBOL);
			sourceField2.setSymbolicName("SymbolicName");
			sourceField2.validate();
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				correctException = true;
			}

			assertTrue(correctException);
			assertEquals(sourceField2.getSymbolicName(), "SymbolicName");
			assertEquals(sourceField2.getSourceFieldType(),
					LookupPathSourceFieldType.SYMBOL);
		}
	}

}
