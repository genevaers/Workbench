package com.ibm.safr.we.model.view;

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


import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.SAFRApplication;

public class TestViewSortKey extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestViewSource");

	static String VIEWSOURCE_KEY = "x_viewsrclrfiletbl";
	TestDataLayerHelper helper = new TestDataLayerHelper();
	Integer nextId = null;

	public void setUp() {
		nextId = null;
	}

	public void tearDown(){
		helper.closeDataLayer();
	}
	
	public void dbStartup() {
		try {
			helper.initDataLayer();
		} catch (DAOException e) {
			assertFalse(true);
		}
	}

	public void testGetSetView() {
		dbStartup();
		View view = null;
		View view1 = null;
		ViewSortKey viewSortKey = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			view1 = SAFRApplication.getSAFRFactory().getView(1);
			viewSortKey = view.getViewSortKeys().get(0);
			assertNotNull(viewSortKey);
			assertEquals(view, viewSortKey.getView());
			viewSortKey.setView(view1);
			assertEquals(view1, viewSortKey.getView());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetViewColumn() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);

			assertEquals(view.getViewColumns().get(0), viewSortKey
					.getViewColumn());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetKeySequenceNo() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		ViewSortKey viewSortKey1 = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			viewSortKey1 = view.getViewSortKeys().get(1);

			assertEquals(new Integer(1), viewSortKey.getKeySequenceNo());

			assertEquals(new Integer(2), viewSortKey1.getKeySequenceNo());

			viewSortKey1.setKeySequenceNo(1);
			assertEquals(new Integer(1), viewSortKey1.getKeySequenceNo());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetSortSequenceCode() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		CodeSet codeSet = null;
		Code code = null;
		Code code1 = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("SORTSEQ");
			code = codeSet.getCode("ASCND");
			code1 = codeSet.getCode("DSCND");
			assertEquals(code, viewSortKey.getSortSequenceCode());

			viewSortKey.setSortSequenceCode(code1);
			assertEquals(code1, viewSortKey.getSortSequenceCode());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);

		// test for null Sort Sequence Code.
		correctException = false;
		try {
			viewSortKey.setSortSequenceCode(null);
		} catch (NullPointerException npe) {
			correctException = true;
		}
		assertTrue(correctException);
	}

	public void testGetSetFooterOptionCode() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		CodeSet codeSet = null;
		Code code = null;
		Code code1 = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("SORTBRKFTR");
			code = codeSet.getCode("NOPRT");
			code1 = codeSet.getCode("PRINT");
			viewSortKey.setFooterOption(code);
			assertEquals(code, viewSortKey.getFooterOptionCode());

			viewSortKey.setFooterOption(code1);
			assertEquals(code1, viewSortKey.getFooterOptionCode());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);

		// test for null Footer Option Code
		correctException = false;
		try {
			viewSortKey.setFooterOption(null);
		} catch (NullPointerException npe) {
			correctException = true;
		}
		assertTrue(correctException);
	}

	public void testGetSetHeaderOptionCode() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		CodeSet codeSet = null;
		Code code = null;
		Code code1 = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("SORTBRKHDR");
			code = codeSet.getCode("NOPRT");
			code1 = codeSet.getCode("PNEW");
			viewSortKey.setHeaderOption(code);
			assertEquals(code, viewSortKey.getHeaderOptionCode());

			viewSortKey.setHeaderOption(code1);
			assertEquals(code1, viewSortKey.getHeaderOptionCode());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);

		correctException = false;
		try {
			viewSortKey.setHeaderOption(null);
		} catch (NullPointerException npe) {
			correctException = true;
		}
		assertTrue(correctException);
	}

	public void testGetSetDisplayModeCode() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		CodeSet codeSet = null;
		Code code = null;
		Code code1 = null;
		Boolean correctException = false;
		ViewColumn viewColumn = null;
		CodeSet codeSet1 = null;
		CodeSet codeSet2 = null;
		Code codeHdr = null;
		Code codeFtr = null;

		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("SORTDSP");
			code = codeSet.getCode("ASDTA");
			code1 = codeSet.getCode("CAT");
			viewSortKey.setDisplayModeCode(code);
			assertEquals(code, viewSortKey.getDisplayModeCode());

			viewSortKey.setDisplayModeCode(code1);
			assertEquals(code1, viewSortKey.getDisplayModeCode());
			viewSortKey.setDisplayModeCode(null);
			assertEquals(null, viewSortKey.getDisplayModeCode());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);

		// test for resetting fields when display mode is changed
		correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8545);
			viewSortKey = view.getViewSortKeys().get(0);

			// display mode = As Data
			viewSortKey.setDisplayModeCode(code);
			assertEquals(null, viewSortKey.getSortkeyLabel());
			assertEquals(null, viewSortKey.getTitleField());
			viewColumn = viewSortKey.getViewColumn();
			assertEquals(null, viewColumn.getSortkeyFooterLabel());

			// display mode = Categorize
			viewSortKey.setDisplayModeCode(code1);
			codeSet1 = SAFRApplication.getSAFRFactory()
					.getCodeSet("SORTBRKHDR");
			codeHdr = codeSet1.getCode("PSAME");
			codeSet2 = SAFRApplication.getSAFRFactory()
					.getCodeSet("SORTBRKFTR");
			codeFtr = codeSet2.getCode("PRINT");
			assertEquals(codeHdr, viewSortKey.getHeaderOptionCode());
			assertEquals(codeFtr, viewSortKey.getFooterOptionCode());
			assertEquals(viewColumn.getHeading1(),null); 
					
			assertEquals(viewSortKey.getSortkeyLabel(),"");
			assertEquals("Subtotal,", viewColumn.getSortkeyFooterLabel());

			// display mode = null
			viewSortKey.setDisplayModeCode(null);
			assertEquals(codeHdr, viewSortKey.getHeaderOptionCode());
			assertEquals(codeFtr, viewSortKey.getFooterOptionCode());
			assertEquals(viewColumn.getHeading1(),null); 
			assertEquals(viewSortKey.getSortkeyLabel(),"");
			assertEquals("Subtotal,", viewColumn.getSortkeyFooterLabel());
		} catch (SAFRException e) {
			correctException = true;
		}
	}

	public void testGetSetSortKeyLabel() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		View view1 = null;
		ViewSortKey viewSortKey1 = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			assertEquals(viewSortKey.getSortkeyLabel(), view.getViewColumns()
					.get(0).getHeading1());
			viewSortKey.setSortkeyLabel("test");

			assertEquals(new String("test"), viewSortKey.getSortkeyLabel());

			viewSortKey.setSortkeyLabel(null);

			assertEquals(null, viewSortKey.getSortkeyLabel());

			// check if sortkey label gets populated
			// according to view column source value.
			view1 = SAFRApplication.getSAFRFactory().getView(8540);
			viewSortKey1 = view1.getViewSortKeys().get(0);
			assertEquals(view1.getViewColumnSources().get(0).getLRField()
					.getSortKeyLabel(), viewSortKey1.getSortkeyLabel());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetDataTypeCode() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		CodeSet codeSet = null;
		Code code = null;
		Code code1 = null;
		Code code2 = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("DATATYPE");
			code = codeSet.getCode("ALNUM");
			code1 = codeSet.getCode("PACKD");
			viewSortKey.setDataTypeCode(code);
			assertEquals(code, viewSortKey.getDataTypeCode());
			assertFalse(viewSortKey.isSigned());

			viewSortKey.setDataTypeCode(code1);
			assertEquals(code1, viewSortKey.getDataTypeCode());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);

		// test for null Data Type code
		correctException = false;
		try {
			viewSortKey.setDataTypeCode(null);
		} catch (NullPointerException npe) {
			correctException = true;
		}
		assertTrue(correctException);

		// test for date/time format if data type is Masked Numeric
		try {
			code2 = codeSet.getCode("MSKNM");
			viewSortKey.setDataTypeCode(code2);
			assertEquals(viewSortKey.getDateTimeFormatCode(), null);
		} catch (NullPointerException npe) {
			assertTrue(false);
		}

		// test for date/time format and signed if data type is Edited Numeric
		try {
			code2 = codeSet.getCode("EDNUM");
			viewSortKey.setDataTypeCode(code2);
			assertTrue(viewSortKey.isSigned());
			assertEquals(viewSortKey.getDateTimeFormatCode(), null);
		} catch (NullPointerException npe) {
			assertTrue(false);
		}

	}

	public void testGetSetSigned() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			viewSortKey.setSigned(false);

			assertEquals(new Boolean(false), viewSortKey.isSigned());

			viewSortKey.setSigned(true);

			assertEquals(new Boolean(true), viewSortKey.isSigned());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetStartPosition() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			viewSortKey.setStartPosition(1);

			assertEquals(new Integer(1), viewSortKey.getStartPosition());

			viewSortKey.setStartPosition(11);

			assertEquals(new Integer(11), viewSortKey.getStartPosition());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetLength() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			viewSortKey.setLength(1);

			assertEquals(new Integer(1), viewSortKey.getLength());

			viewSortKey.setLength(11);

			assertEquals(new Integer(11), viewSortKey.getLength());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetDecimalPlaces() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			viewSortKey.setDecimalPlaces(1);

			assertEquals(new Integer(1), viewSortKey.getDecimalPlaces());

			viewSortKey.setDecimalPlaces(11);

			assertEquals(new Integer(11), viewSortKey.getDecimalPlaces());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetDateTimeFormatCode() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		CodeSet codeSet = null;
		Code code = null;
		Code code1 = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FLDCONTENT");
			code = codeSet.getCode("CCYY");
			code1 = codeSet.getCode("CYAP");
			viewSortKey.setDateTimeFormatCode(code);
			assertEquals(code, viewSortKey.getDateTimeFormatCode());

			viewSortKey.setDateTimeFormatCode(code1);
			assertEquals(code1, viewSortKey.getDateTimeFormatCode());
			viewSortKey.setDateTimeFormatCode(null);
			assertEquals(null, viewSortKey.getDateTimeFormatCode());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetTitleField() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		LRField lrFiled = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			lrFiled = SAFRApplication.getSAFRFactory().getLRField(62683, false);
			viewSortKey = view.getViewSortKeys().get(0);
			viewSortKey.setTitleField(lrFiled);

			assertEquals(lrFiled, viewSortKey.getTitleField());

			viewSortKey.setTitleField(null);

			assertEquals(null, viewSortKey.getTitleField());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetTitleLength() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewSortKey = view.getViewSortKeys().get(0);
			viewSortKey.setTitleLength(1);

			assertEquals(new Integer(1), viewSortKey.getTitleLength());

			viewSortKey.setTitleLength(11);

			assertEquals(new Integer(11), viewSortKey.getTitleLength());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testValidateTitleField() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		ViewColumnSource viewColSource = null;
		Boolean correctException = false;
		Boolean correctException1 = false;
		LRField lrField1 = null;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8307);
			viewSortKey = view.getViewSortKeys().get(0);
			viewColSource = viewSortKey.getViewColumn().getViewColumnSources()
					.get(0);
			lrField1 = SAFRApplication.getSAFRFactory()
					.getLRField(16620, false);

			viewColSource.setSortKeyTitleLRField(lrField1);

			viewSortKey.validateTitleField();
		} catch (SAFRValidationException e) {
			correctException = true;
		} catch (SAFRException e) {
			correctException1 = true;
		}
		assertTrue(correctException);
		assertFalse(correctException1);

		correctException = false;
		correctException1 = false;
		try {
			viewSortKey.setTitleLength(null);
			viewSortKey.validateTitleField();
		} catch (SAFRValidationException e) {
			correctException = true;
		} catch (SAFRException e) {
			correctException1 = true;
		}
		assertTrue(correctException);
		assertFalse(correctException1);
	}

	public void testRemoveSortKeyTitleField() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey = null;
		Boolean correctException1 = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8512);
			viewSortKey = view.getViewSortKeys().get(0);
			viewSortKey.removeSortKeyTitleField();
		} catch (SAFRException e) {
			correctException1 = true;
		}

		assertFalse(correctException1);
	}

	public void testGetTitleAlignment() {
		dbStartup();
		View view = null;
		ViewSortKey viewSortKey;
		Boolean correctException1 = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8512);
		} catch (SAFRException e) {
			correctException1 = true;
		}
		assertFalse(correctException1);
		viewSortKey = view.getViewSortKeys().get(0);
		// alphanumeric sort key will always have alignment as LEFT.
		viewSortKey.setDataTypeCode(SAFRApplication.getSAFRFactory()
				.getCodeSet(CodeCategories.DATATYPE).getCode(
						(Integer) Codes.ALPHANUMERIC));
		Code align = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.JUSTIFY).getCode((Integer) Codes.LEFT);
		assertTrue(viewSortKey.getTitleAlignment().getGeneralId().equals(
				align.getGeneralId()));

		// Edited numeric sort key will always have alignment as Right.
		viewSortKey.setDataTypeCode(SAFRApplication.getSAFRFactory()
				.getCodeSet(CodeCategories.DATATYPE).getCode(
						(Integer) Codes.EDITED_NUMERIC));
		align = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.JUSTIFY).getCode((Integer) Codes.RIGHT);
		assertTrue(viewSortKey.getTitleAlignment().getGeneralId().equals(
				align.getGeneralId()));

		// sort key other than alpha or edited numeric will have no alignment.
		viewSortKey.setDataTypeCode(SAFRApplication.getSAFRFactory()
				.getCodeSet(CodeCategories.DATATYPE).getCode(
						(Integer) Codes.PACKED));
		assertTrue(viewSortKey.getTitleAlignment() == null);

	}
	
	public void testActivate_CQ9804() throws SAFRException {
        // open ViewSortKey env
        helper.initDataLayer(106);

        View view = SAFRApplication.getSAFRFactory().getView(1);
        try {
            view.activate();
            
            // should fail activation
            fail();
        } catch (SAFRViewActivationException e) {
            List<ViewActivationError> errors = e.getActivationLogNew();
            assertEquals(errors.size(), 1);
            ViewActivationError error = errors.get(0);
            assertEquals(error.getErrorText(), "Sort Key Footer Option must be 'Print' for the last sort key of a hard copy summary view.");
            assertEquals(error.getErrorType(), SAFRCompilerErrorType.VIEW_PROPERTIES);
        }
        
        // set display mode "asdata" and confirm activation works
        assertEquals(view.getViewSortKeys().size(), 1);
        ViewSortKey key = view.getViewSortKeys().get(0);
        Code categorise = new Code("CAT", "Categorize", 1);
        assertEquals(key.getDisplayModeCode(), categorise);
        Code asData = new Code("ASDTA", "As Data", 2);
        key.setDisplayModeCode(asData);
        view.activate();
        
        helper.closeDataLayer();
	}
}
