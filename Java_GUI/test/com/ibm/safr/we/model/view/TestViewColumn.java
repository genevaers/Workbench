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


import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.SAFRApplication;

public class TestViewColumn extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestViewColumn");

	static String VIEW_COLUMN_KEY = "xov_viewlrfldattr";
	TestDataLayerHelper helper = new TestDataLayerHelper();

	public void setUp() {
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

	public void testBreakSubtotalCodeIntoAggregationFunctions() {
		dbStartup();
		ViewColumn viewColumn = null;
		View view = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8512);
			viewColumn = view.getViewColumns().get(3);

			assertEquals(SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.RECORDAGGR).getCode("SUM"), viewColumn
					.getRecordAggregationCode());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testCalculateSubtotalCodeUsingAggregationFunctions() {
		dbStartup();
		ViewColumn viewColumn = null;
		View view = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(1);

			assertNull(viewColumn.getSubtotalTypeCode());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetView() {
		dbStartup();
		ViewColumn viewColumn = null;
		View view = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(1);

			assertEquals(view, viewColumn.getView());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetColumnNo() {
		dbStartup();
		ViewColumn viewColumn = null;
		ViewColumn viewColumn1 = null;
		View view = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
			viewColumn1 = view.getViewColumns().get(1);

			assertEquals(new Integer(1), viewColumn.getColumnNo());
			assertEquals(new Integer(2), viewColumn1.getColumnNo());
			viewColumn.setColumnNo(3);
			assertEquals(new Integer(3), viewColumn.getColumnNo());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetStartPosition() {
		dbStartup();
		ViewColumn viewColumn = null;
		ViewColumn viewColumn1 = null;
		View view = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
			viewColumn.setLength(10);
			viewColumn1 = view.getViewColumns().get(1);

			assertEquals(new Integer(1), viewColumn.getStartPosition());
			assertEquals(new Integer(11), viewColumn1.getStartPosition());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetDataTypeCode() {
		dbStartup();
		ViewColumn viewColumn = null;
		View view = null;
		CodeSet codeSet = null;
		Code dataTypeCd = null;
		Code dataTypeCd1 = null;
		Code dataTypeCd2 = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("DATATYPE");
			dataTypeCd = codeSet.getCode("ALNUM");
			dataTypeCd1 = codeSet.getCode("PACKD");
			assertEquals(dataTypeCd, viewColumn.getDataTypeCode());

			viewColumn.setDataTypeCode(dataTypeCd1);
			assertEquals(dataTypeCd1, viewColumn.getDataTypeCode());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);

		correctException = false;
		try {
			viewColumn.setDataTypeCode(null);
		} catch (NullPointerException npe) {
			correctException = true;
		}
		assertTrue(correctException);

		// test for date/time format if data type is Masked Numeric
		try {
			dataTypeCd2 = codeSet.getCode("MSKNM");
			viewColumn.setDataTypeCode(dataTypeCd2);
			assertEquals(viewColumn.getDateTimeFormatCode(), null);
		} catch (NullPointerException npe) {
			assertTrue(false);
		}

		// test for date/time format if data type is Edited Numeric
		try {
			dataTypeCd2 = codeSet.getCode("EDNUM");
			viewColumn.setDataTypeCode(dataTypeCd2);
			assertEquals(viewColumn.getDateTimeFormatCode(), null);
		} catch (NullPointerException npe) {
			assertTrue(false);
		}

	}

	public void testGetSetDataAlignmentCode() {
		dbStartup();
		ViewColumn viewColumn = null;
		View view = null;
		CodeSet codeSet = null;
		Code dataAlignCd = null;
		Code dataAlignCd1 = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("JUSTIFY");
			dataAlignCd = codeSet.getCode("LEFT");
			dataAlignCd1 = codeSet.getCode("RIGHT");
			assertEquals(dataAlignCd, viewColumn.getDataAlignmentCode());

			viewColumn.setDataAlignmentCode(dataAlignCd1);
			assertEquals(dataAlignCd1, viewColumn.getDataAlignmentCode());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetVisible() {
		dbStartup();
		ViewColumn viewColumn = null;
		View view = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
			assertEquals(new Boolean(true), viewColumn.isVisible());

			viewColumn.setVisible(false);
			assertEquals(new Boolean(false), viewColumn.isVisible());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetSpaceBeforeColumn() {
		dbStartup();
		ViewColumn viewColumn = null;
		View view = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
	        view.setOutputFormat(OutputFormat.Format_Fixed_Width_Fields);
			viewColumn.setSpacesBeforeColumn(3);
			assertEquals(new Integer(3), viewColumn.getSpacesBeforeColumn());
			assertEquals(new Integer(4), viewColumn.getStartPosition());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetSortKeyFooterLabel() {
		dbStartup();
		ViewColumn viewColumn = null;
		View view = null;
		View view1 = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
			assertEquals(new String("Subtotal,"), view.getViewColumns().get(0)
					.getSortkeyFooterLabel());
			viewColumn.setSortkeyFooterLabel("test");
			assertEquals(new String("test"), viewColumn.getSortkeyFooterLabel());

			viewColumn.setSortkeyFooterLabel(null);
			assertEquals(null, viewColumn.getSortkeyFooterLabel());

			// check if sortkey footer label gets populated
			// according to view column source value.
			view1 = SAFRApplication.getSAFRFactory().getView(8540);

			assertEquals(view1.getViewColumnSources().get(0).getLRField()
					.getSubtotalLabel(), view1.getViewColumns().get(0)
					.getSortkeyFooterLabel());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetViewColumnSource() {
		dbStartup();
		ViewColumn viewColumn = null;
		ViewColumnSource viewColumnSource = null;
		ViewSource viewSource = null;
		View view = null;
		Boolean correctException = false;
		Boolean correctException1 = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
			viewSource = view.getViewSources().get(0);
			viewColumnSource = viewColumn.getViewColumnSources().get(0);

			assertNotNull(viewColumnSource);

			try {
				viewColumn.addViewColumnSource(viewSource);
			} catch (IllegalArgumentException e) {
				correctException = true;
			}
			assertTrue(correctException);
		} catch (SAFRException e) {
			correctException1 = true;
		}
		assertFalse(correctException1);
	}

	public void testGetSetRecordAggregationCode() {
		dbStartup();
		ViewColumn viewColumn = null;
		View view = null;
		CodeSet codeSet = null;
		Code recAggrCd = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
			codeSet = SAFRApplication.getSAFRFactory().getCodeSet("RECORDAGGR");
			recAggrCd = codeSet.getCode("FIRST");
			viewColumn.setRecordAggregationCode(recAggrCd);

			assertEquals(recAggrCd, viewColumn.getRecordAggregationCode());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetGroupAggregationCode() {
		dbStartup();
		ViewColumn viewColumn = null;
		View view = null;
		CodeSet codeSet = null;
		Code grpAggrCd = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
			codeSet = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.GROUPAGGR);
			grpAggrCd = codeSet.getCode(Integer.valueOf(Codes.GRPAGGR_FIRST));
			viewColumn.setGroupAggregationCode(grpAggrCd);

			assertEquals(grpAggrCd, viewColumn.getGroupAggregationCode());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testIsSortKey() {
		dbStartup();
		ViewColumn viewColumn = null;
		ViewColumn viewColumn1 = null;
		ViewSortKey viewSortKey = null;
		View view = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
			viewColumn1 = view.getViewColumns().get(2);

			assertEquals(new Boolean(true), viewColumn.isSortKey());
			viewSortKey = viewColumn.getViewSortKey();
			assertEquals(new Boolean(false), viewColumn1.isSortKey());
			assertNotNull(viewSortKey);

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetFormatColumnCalculation() {
		dbStartup();
		ViewColumn viewColumn = null;
		View view = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
			viewColumn.setFormatColumnCalculation("test");
			assertEquals(new String("test"), viewColumn
					.getFormatColumnCalculation());

			viewColumn.setFormatColumnCalculation(null);
			assertEquals(null, viewColumn.getFormatColumnCalculation());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testValidateFormatColumnCalculation() {
		dbStartup();
		View view = null;
		ViewColumn viewColumn = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			viewColumn = view.getViewColumns().get(0);
			viewColumn.validateFormatColumnCalculation("test");
		} catch (SAFRViewActivationException se) {
			correctException = true;
		} catch (SAFRException se) {
			logger.log(Level.SEVERE, "", se);
			assertTrue(false);
		}

		assertTrue(correctException);
	}
	
	public void testCalculateSubtotalCodeUsingAggregationFunctions2() throws SAFRException {
		dbStartup();
		helper.setEnv(99);
		
		ViewColumn viewColumn = null;
		View view = null;
		
		
			view = SAFRApplication.getSAFRFactory().getView(8716);
			viewColumn = view.getViewColumns().get(2);
			viewColumn.getRecordAggregationCode();

			assertNull(viewColumn.getRecordAggregationCode());
	
	}
	
}
