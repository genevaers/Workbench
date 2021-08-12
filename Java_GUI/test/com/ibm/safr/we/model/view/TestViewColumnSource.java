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
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;

public class TestViewColumnSource extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestViewColumnSource");

	static String VIEW_COLUMN_SOURCE_KEY = "x_viewlrfld_lrfld";
	TestDataLayerHelper helper = new TestDataLayerHelper();
	Integer nextId = null;

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

	public void testGetViewColumn() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		ViewColumn viewColumn = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			viewColumn = viewColumnSource.getViewColumn();
		} catch (SAFRException e) {
			correctException = true;
		}
		assertEquals(viewColumn.getColumnNo(), new Integer(1));
		assertEquals(viewColumn.isSortKey(), new Boolean(true));
		assertFalse(correctException);
	}

	public void testGetViewSource() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		ViewSource viewSource = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			viewSource = viewColumnSource.getViewSource();
		} catch (SAFRException e) {
			correctException = true;
		}
		assertNotNull(viewSource);
		assertFalse(correctException);
	}

	public void testGetSetSourceType() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.COLSRCTYPE);
		Code code = codeSet.getCode(Integer.valueOf(Codes.SOURCE_FILE_FIELD));
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
		} catch (SAFRException e) {
			correctException = true;
		}
		assertEquals(viewColumnSource.getSourceType(), code);
		assertFalse(correctException);

		// test for null source type
		correctException = false;
		try {
			viewColumnSource.setSourceType(null);
		} catch (NullPointerException npe) {
			correctException = true;
		}
		assertTrue(correctException);

		// test for resetting values on changing source type
		correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8542);
			viewColumnSource = view.getViewColumnSources().get(0);
		} catch (SAFRException e) {
			correctException = true;
		}
		try {
			viewColumnSource.setSourceType(code);
			assertEquals(viewColumnSource.getSourceValue(), null);
			assertEquals(viewColumnSource.getLogicalRecordQueryBean(), null);
			assertEquals(viewColumnSource.getLookupQueryBean(), null);
			assertEquals(viewColumnSource.getLRField(), null);
			assertEquals(viewColumnSource.getEffectiveDateTypeCode(), null);
			assertEquals(viewColumnSource.getEffectiveDateValue(), null);
		} catch (NullPointerException npe) {
			assertTrue(false);
		} catch (DAOException e) {
			assertTrue(false);
			e.printStackTrace();
		} catch (SAFRException e) {
			assertTrue(false);
			e.printStackTrace();
		}
	}

	public void testGetSetSourceLRField() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		LRField lrField = null;
		ViewColumn viewColumn = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			viewColumn = viewColumnSource.getViewColumn();
			lrField = SAFRApplication.getSAFRFactory().getLRField(30056, false);
			viewColumnSource.setLRFieldColumn(lrField);
			assertEquals(viewColumn.getDataTypeCode(), lrField.getDataTypeCode());
			assertEquals(lrField, viewColumnSource.getLRField());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetSortKeyTitleLRField() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		ViewColumnSource viewColumnSource1 = null;
		LRField lrField = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			viewColumnSource1 = view.getViewColumnSources().get(1);
			lrField = SAFRApplication.getSAFRFactory().getLRField(30056, false);
			viewColumnSource.setSortKeyTitleLRField(lrField);
			viewColumnSource1.setSortKeyTitleLRField(lrField);
			assertEquals(lrField, viewColumnSource.getSortKeyTitleLRField());
			assertEquals(lrField, viewColumnSource1.getSortKeyTitleLRField());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetSourceValue() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			viewColumnSource.setSourceValue("test");
			assertEquals(new String("test"), viewColumnSource.getSourceValue());

			viewColumnSource.setSourceValue("abc");
			assertEquals(new String("abc"), viewColumnSource.getSourceValue());

			viewColumnSource.setSourceValue(null);
			assertEquals(null, viewColumnSource.getSourceValue());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetEffectiveDateTypeCode() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.EFFDATEKEY);
		Code code = codeSet.getCode(Integer.valueOf(Codes.RELPERIOD_RUNDATE));
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			viewColumnSource.setEffectiveDateTypeCode(code);
		} catch (SAFRException e) {
			correctException = true;
		}
		assertEquals(viewColumnSource.getEffectiveDateTypeCode(), code);
		assertFalse(correctException);
	}

	public void testGetSetEffectiveDateValue() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			viewColumnSource.setEffectiveDateValue("test");
			assertEquals(new String("test"), viewColumnSource
					.getEffectiveDateValue());

			viewColumnSource.setEffectiveDateValue("abc");
			assertEquals(new String("abc"), viewColumnSource
					.getEffectiveDateValue());

			viewColumnSource.setEffectiveDateValue(null);
			assertEquals(null, viewColumnSource.getEffectiveDateValue());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetEffectiveDateLRField() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		ViewColumnSource viewColumnSource1 = null;
		LRField lrField = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			viewColumnSource1 = view.getViewColumnSources().get(1);
			lrField = SAFRApplication.getSAFRFactory().getLRField(30056, false);
			viewColumnSource.setEffectiveDateLRField(lrField);
			viewColumnSource1.setEffectiveDateLRField(lrField);
			assertEquals(lrField, viewColumnSource.getEffectiveDateLRField());
			assertEquals(viewColumnSource.getViewColumn().isSortKey(),
					new Boolean(true));

			assertEquals(lrField, viewColumnSource1.getEffectiveDateLRField());

		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetExtractColumnAssignment() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			viewColumnSource.setExtractColumnAssignment("test");
			assertEquals(new String("test"), viewColumnSource
					.getExtractColumnAssignment());

			viewColumnSource.setExtractColumnAssignment("abc");
			assertEquals(new String("abc"), viewColumnSource
					.getExtractColumnAssignment());

			// changing column source type should reset the extract column
			// assignment to a default value.
			viewColumnSource.setSourceType(SAFRApplication.getSAFRFactory()
					.getCodeSet(CodeCategories.COLSRCTYPE).getCode(
							(Integer) Codes.LOOKUP_FIELD));
			assertEquals(viewColumnSource.getExtractColumnAssignment(),
					""); // CQ10096 removed the default LT comment

			viewColumnSource.setExtractColumnAssignment(null);
			assertEquals(null, viewColumnSource.getExtractColumnAssignment());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetLogicalRecordQueryBean() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		LogicalRecordQueryBean lrQueryBean = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			lrQueryBean = SAFRQuery.queryAllLogicalRecords(1,
					SortType.SORT_BY_ID).get(0);
			viewColumnSource.setLogicalRecordQueryBean(lrQueryBean);
			assertEquals(lrQueryBean, viewColumnSource
					.getLogicalRecordQueryBean());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetLookupQueryBean() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		LookupQueryBean lkQueryBean = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			lkQueryBean = SAFRQuery.queryAllLookups(1, SortType.SORT_BY_ID)
					.get(0);
			viewColumnSource.setLookupQueryBean(lkQueryBean);
			assertEquals(lkQueryBean, viewColumnSource.getLookupQueryBean());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetSortKeyTitleLookupPathQueryBean() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		ViewColumnSource viewColumnSource1 = null;
		LookupQueryBean lkQueryBean = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			viewColumnSource1 = view.getViewColumnSources().get(1);
			lkQueryBean = SAFRQuery.queryAllLookups(1, SortType.SORT_BY_ID)
					.get(0);
			viewColumnSource.setLookupQueryBean(lkQueryBean);
			viewColumnSource1.setLookupQueryBean(lkQueryBean);
			assertEquals(lkQueryBean, viewColumnSource.getLookupQueryBean());
			assertEquals(lkQueryBean, viewColumnSource1.getLookupQueryBean());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testGetSetSortKeyTitleLogicalRecordQueryBean() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		ViewColumnSource viewColumnSource1 = null;
		LogicalRecordQueryBean lrQueryBean = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			viewColumnSource1 = view.getViewColumnSources().get(1);
			lrQueryBean = SAFRQuery.queryAllLogicalRecords(1,
					SortType.SORT_BY_ID).get(0);
			viewColumnSource.setSortKeyTitleLogicalRecordQueryBean(lrQueryBean);
			viewColumnSource1
					.setSortKeyTitleLogicalRecordQueryBean(lrQueryBean);
			assertEquals(lrQueryBean, viewColumnSource
					.getSortKeyTitleLogicalRecordQueryBean());
			assertEquals(lrQueryBean, viewColumnSource1
					.getSortKeyTitleLogicalRecordQueryBean());
		} catch (SAFRException e) {
			correctException = true;
		}
		assertFalse(correctException);
	}

	public void testValidateExtractColumnAssignment() {
		dbStartup();
		View view = null;
		ViewColumnSource viewColumnSource = null;
		Boolean correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(8376);
			viewColumnSource = view.getViewColumnSources().get(0);
			viewColumnSource.getViewSource();
			viewColumnSource.validateExtractColumnAssignment("test");
		} catch (SAFRViewActivationException se) {
			correctException = true;
		} catch (SAFRException se) {
			logger.log(Level.SEVERE, "", se);
		}

		assertTrue(correctException);
	}
	
}
