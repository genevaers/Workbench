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

import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.base.SAFRComponent;

public class TestSAFRValidator extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestSAFRValidator");
	TestDataLayerHelper helper = new TestDataLayerHelper();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testIsNameValid() {
		// check for null
		String componentName = null;
		Boolean correctException = false;
		SAFRValidator safrValidator = new SAFRValidator();
		try {
			safrValidator.isNameValid(componentName);
		} catch (NullPointerException e) {
			correctException = true;
		} catch (Exception e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		}

		assertTrue(correctException);

		// check for valid name
		componentName = "jaydeep";
		Boolean isNameValid = false;
		isNameValid = safrValidator.isNameValid(componentName);
		assertTrue(isNameValid);

		// check for valid with number and underscores.
		componentName = "jaydeep1123_";
		isNameValid = false;
		isNameValid = safrValidator.isNameValid(componentName);
		assertTrue(isNameValid);

		// check for starting with number
		componentName = "1jaydeep";
		isNameValid = false;
		isNameValid = safrValidator.isNameValid(componentName);
		assertFalse(isNameValid);

		// check for starting with _
		componentName = "_jaydeep";
		isNameValid = false;
		isNameValid = safrValidator.isNameValid(componentName);
		assertFalse(isNameValid);
	}

	private class SAFRFieldId extends SAFRField {

		public SAFRFieldId(Integer id, Integer environmentId, String name,
				Integer decimals, Integer scaling, Integer length,
				Boolean signed, String heading1, String heading2,
				String heading3, String sortKeyLabel, String defaultValue) {
			super(environmentId);
			super.id = id;
			super.setName(name);
			super.setDecimals(decimals);
			super.setScaling(scaling);
			super.setLength(length);
			super.setSigned(signed);
			super.setHeading1(heading1);
			super.setHeading2(heading2);
			super.setHeading3(heading3);
			super.setSortKeyLabel(sortKeyLabel);
			super.setDefaultValue(defaultValue);
		}

		public void store() {
			// no-op implementation of an inherited abstract method
		}

		@Override
		public SAFRComponent saveAs(String newName)
				throws SAFRValidationException, SAFRException {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public void testVerifyField() {
		Boolean correctException = false;
		Boolean noException = false;
		SAFRValidator safrValidator = new SAFRValidator();
		SAFRFieldId field;
		Code dataType;
		Code dateTime;
		Code mask;

		// field with no datatype
		field = new SAFRFieldId(1, 2, "Name", 0, 0, 5, false, "HEADING1",
				"HEADING2", "HEADING3", "SORT KEY LABEL", "DEFAULT VALUE");

		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"specify")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		// ***********************ZONED DECIMALS**************************//

		field = new SAFRFieldId(1, 2, "Name", 0, 0, 5, false, "HEADING1",
				"HEADING2", "HEADING3", "SORT KEY LABEL", "DEFAULT VALUE");
		dataType = new Code("NUMER", "", 3);
		field.setDataTypeCode(dataType);

		checkLength1to16(field); // length should be between 1 and 16

		// ***********************ALPHANUMERIC**************************//

		field = new SAFRFieldId(1, 2, "Name", 0, 0, 5, false, "HEADING1",
				"HEADING2", "HEADING3", "SORT KEY LABEL", "DEFAULT VALUE");
		// decimal count
		correctException = false;
		dataType = new Code("ALNUM", "", 1);
		field.setDataTypeCode(dataType);
		field.setDecimals(9);
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"decimals")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		noException = false;
		field.setDecimals(0);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		// Signed
		correctException = false;
		field.setSigned(true);
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"signed")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		noException = false;
		field.setSigned(false);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		// Scaling
		correctException = false;
		field.setScaling(3);
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"scaling")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		noException = false;
		field.setScaling(0);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		// length
		checkLength1to256(field);

		// Date time format
		dateTime = new Code("YMD", "YYMMDD", 1);
		field.setDateTimeFormatCode(dateTime);
		correctException = false;
		field.setLength(1);
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"Date time format")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		noException = false;
		field.setLength(7);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		// ***********************BINARY SORTABLE**************************//

		field = new SAFRFieldId(1, 2, "Name", 0, 0, 5, false, "HEADING1",
				"HEADING2", "HEADING3", "SORT KEY LABEL", "DEFAULT VALUE");

		dataType = new Code("BSORT", "", 7);
		field.setDataTypeCode(dataType);

		checkBinaryLength(field);

		// ****************BINARY CODED DECIMALS****************//

		field = new SAFRFieldId(1, 2, "Name", 0, 0, 5, false, "HEADING1",
				"HEADING2", "HEADING3", "SORT KEY LABEL", "DEFAULT VALUE");
		dataType = new Code("BCD", "", 8);
		field.setDataTypeCode(dataType);

		// length
		correctException = false;
		field.setLength(0);
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"length")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		correctException = false;
		field.setLength(200);
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"length")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		noException = false;
		field.setLength(2);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		// ***********************PACKED**************************//

		field = new SAFRFieldId(1, 2, "Name", 0, 0, 5, false, "HEADING1",
				"HEADING2", "HEADING3", "SORT KEY LABEL", "DEFAULT VALUE");
		dataType = new Code("PACKD", "", 4);
		field.setDataTypeCode(dataType);

		checkLength1to16(field); // length should be between 1 and 16

		// ***********************PACKED SORTABLE**************************//

		field = new SAFRFieldId(1, 2, "Name", 0, 0, 5, false, "HEADING1",
				"HEADING2", "HEADING3", "SORT KEY LABEL", "DEFAULT VALUE");
		dataType = new Code("PSORT", "", 5);
		field.setDataTypeCode(dataType);

		checkLength1to16(field); // length should be between 1 and 16

		// ***********************BINARY**************************//

		field = new SAFRFieldId(1, 2, "Name", 0, 0, 5, false, "HEADING1",
				"HEADING2", "HEADING3", "SORT KEY LABEL", "DEFAULT VALUE");

		dataType = new Code("BINRY", "", 6);
		field.setDataTypeCode(dataType);

		checkBinaryLength(field);

		// Date time format
		dateTime = new Code("YMD", "YYMMDD", 1);
		field.setDateTimeFormatCode(dateTime);
		correctException = false;
		field.setLength(1);
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"Date time format")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		noException = false;
		field.setLength(8);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		// Signed
		field.setSigned(true);
		correctException = false;
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"signed")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		field.setSigned(false);
		noException = false;
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		field.setDateTimeFormatCode(null);
		field.setSigned(true);
		noException = false;
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		// ***********************MASK NUMERIC**************************//
		field = new SAFRFieldId(1, 2, "Name", 0, 0, 5, false, "HEADING1",
				"HEADING2", "HEADING3", "SORT KEY LABEL", "DEFAULT VALUE");

		dataType = new Code("MSKNM", "", 9);
		field.setDataTypeCode(dataType);
		mask = new Code("LNNAN", "", 1);
		field.setNumericMaskCode(mask);

		checkLength1to256(field);

		// ***********************EDITED NUMERIC**************************//
		field = new SAFRFieldId(1, 2, "Name", 0, 0, 5, false, "HEADING1",
				"HEADING2", "HEADING3", "SORT KEY LABEL", "DEFAULT VALUE");

		dataType = new Code("EDNUM", "", 10);
		field.setDataTypeCode(dataType);

		checkLength1to256(field);
		// Date time format
		dateTime = new Code("YMD", "YYMMDD", 1);
		field.setDateTimeFormatCode(dateTime);
		correctException = false;
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"date time format")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		field.setDateTimeFormatCode(null);
		noException = false;
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

	}

	private void checkLength1to16(SAFRField field) {
		// Validate acceptable length between 1 and 16 (inclusive)
		Boolean correctException = false;
		SAFRValidator safrValidator = new SAFRValidator();

		correctException = false;
		// invalid lengths
		field.setLength(0);
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"length")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		field.setLength(200);
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"length")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		boolean noException = false;

		// valid lengths
		field.setLength(1);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		field.setLength(10);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		field.setLength(16);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);
	}

	private void checkBinaryLength(SAFRField field) {
		// Validate acceptable lengths as 1,2,4 and 8 only.
		Boolean correctException = false;
		SAFRValidator safrValidator = new SAFRValidator();
		// Invalid length
		correctException = false;
		field.setLength(3);
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"length")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		// valid lengths
		boolean noException = false;

		field.setLength(1);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		noException = false;
		field.setLength(2);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		noException = false;
		field.setLength(4);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		noException = false;
		field.setLength(8);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);
	}

	private void checkLength1to256(SAFRField field) {
		// Validate acceptable length between 1 and 16 (inclusive)
		Boolean correctException = false;
		SAFRValidator safrValidator = new SAFRValidator();

		correctException = false;
		// invalid lengths
		field.setLength(0);
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"length")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		field.setLength(300);
		try {
			safrValidator.verifyField(field);
		} catch (SAFRException e) {
			if (e instanceof SAFRValidationException) {
				if (((SAFRValidationException) e).getMessageString().contains(
						"length")) {
					correctException = true;
				}
			}
		}
		assertTrue(correctException);

		boolean noException = false;

		// valid lengths
		field.setLength(1);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		field.setLength(256);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);

		field.setLength(16);
		try {
			safrValidator.verifyField(field);
			noException = true;
		} catch (SAFRException e) {
		}
		assertTrue(noException);
	}
}
