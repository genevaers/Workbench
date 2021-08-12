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
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPathSourceField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;

public class TestViewSource extends TestCase {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestViewSource");

	static String VIEWSOURCE_KEY = "x_viewsrclrfiletbl";
	TestDataLayerHelper helper = new TestDataLayerHelper();
	Integer nextId = null;
	Boolean correctException = false;

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
			e.printStackTrace();
		}
	}

	public void testGetId() {
		ViewSource viewSource = null;
		dbStartup();
		correctException = false;
		try {
			View view = SAFRApplication.getSAFRFactory().getView(1);
			List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
			viewSource = viewsrcs.get(0);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
		assertNotNull(viewSource);
		assertEquals(new Integer(1), viewSource.getId());
	}

	public void testGetView() {
		ViewSource viewSource = null;
		View view = null;
		dbStartup();
		correctException = false;
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
			viewSource = viewsrcs.get(0);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
		assertEquals(view, viewSource.getView());
	}

	public void testGetSetSequenceNo() {
		ViewSource viewSource = null;
		View view = null;
		correctException = false;
		dbStartup();
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
			viewSource = viewsrcs.get(0);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
		assertEquals(new Integer(1), viewSource.getSequenceNo());

		viewSource.setSequenceNo(2);
		assertEquals(new Integer(2), viewSource.getSequenceNo());

		viewSource.setSequenceNo(null);
		assertEquals(null, viewSource.getSequenceNo());
	}

	public void testGetSetLrFileAssociation() {
		ViewSource viewSource = null;
		View view = null;
		correctException = false;
		ComponentAssociation fileAssoc = null;
		ComponentAssociation fileAssoc1 = null;
		LogicalRecord lr = null;
		dbStartup();
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
			viewSource = viewsrcs.get(0);
			fileAssoc = viewSource.getLrFileAssociation();
			lr = SAFRApplication.getSAFRFactory().getLogicalRecord(1206);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		// assertFalse(correctException);
		assertNotNull(fileAssoc);
		correctException = false;
		try {
			fileAssoc1 = SAFRAssociationFactory
					.getLogicalRecordToLogicalFileAssociations(lr).get(0);
			viewSource.setLrFileAssociation(fileAssoc1);
			assertEquals(fileAssoc1, viewSource.getLrFileAssociation());
		} catch (DAOException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		// assertFalse(correctException);
	}

	public void testGetSetExtractRecordFilter() {
		ViewSource viewSource = null;
		View view = null;
		correctException = false;
		dbStartup();
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
			viewSource = viewsrcs.get(0);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
		viewSource.setExtractRecordFilter("test");
		assertEquals(new String("test"), viewSource.getExtractRecordFilter());

		viewSource.setExtractRecordFilter(null);
		assertEquals(null, viewSource.getExtractRecordFilter());
	}

	public void testGetViewColumnSources() {
		ViewSource viewSource = null;
		View view = null;

		List<ViewColumnSource> vcs = null;
		correctException = false;
		dbStartup();
		try {
			view = SAFRApplication.getSAFRFactory().getView(2206);
			List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
			viewSource = viewsrcs.get(0);
			vcs = viewSource.getViewColumnSources();
			assertEquals(4, vcs.size());
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
	}

	public void testGetLookupLogicalRecords() {
		ViewSource viewSource = null;
		View view = null;
		LogicalRecordQueryBean lr = null;
		correctException = false;
		dbStartup();
		try {
			view = SAFRApplication.getSAFRFactory().getView(8513);
			List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
			viewSource = viewsrcs.get(0);
			lr = viewSource.getLookupLogicalRecords().get(0);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
		assertNotNull(lr);
		assertEquals(new Integer(189), lr.getId());

	}

	public void testGetLookupPaths() {
		ViewSource viewSource = null;
		View view = null;
		LogicalRecordQueryBean lr = null;
		LookupQueryBean lk = null;
		correctException = false;
		dbStartup();
		try {
			view = SAFRApplication.getSAFRFactory().getView(8513);
			List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
			viewSource = viewsrcs.get(0);
			lr = viewSource.getLookupLogicalRecords().get(0);
			lk = viewSource.getLookupPaths(lr.getId()).get(0);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
		assertNotNull(lk);
		assertEquals(new Integer(1992), lk.getId());
	}

	public void testGetAllLookupPaths() {
		ViewSource viewSource = null;
		View view = null;
		List<LookupQueryBean> lkList = null;
		correctException = false;
		dbStartup();
		try {
			view = SAFRApplication.getSAFRFactory().getView(8513);
			List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
			viewSource = viewsrcs.get(0);
			lkList = viewSource.getAllLookupPaths();
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
		assertNotNull(lkList);
		assertEquals(new Integer(1992), lkList.get(0).getId());
	}

	public void testGetLookupFields() {
		ViewSource viewSource = null;
		View view = null;
		LogicalRecordQueryBean lr = null;
		LookupQueryBean lk = null;
		LRField lrField = null;
		correctException = false;
		dbStartup();
		try {
			view = SAFRApplication.getSAFRFactory().getView(8513);
			List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
			viewSource = viewsrcs.get(0);
			lr = viewSource.getLookupLogicalRecords().get(0);
			lk = viewSource.getLookupPaths(lr.getId()).get(0);
			lrField = viewSource.getLookupFields(lk.getId()).get(0);
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
		assertNotNull(lrField);
		assertEquals(new Integer(4105), lrField.getId());
	}

	public void testGetLookupSymbolicFields() {
		ViewSource viewSource = null;
		View view = null;
		LogicalRecordQueryBean lr = null;
		LookupQueryBean lk = null;
		List<LookupPathSourceField> lkFields = null;
		correctException = false;
		dbStartup();
		try {
			view = SAFRApplication.getSAFRFactory().getView(2136);
			List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
			viewSource = viewsrcs.get(0);
			lr = viewSource.getLookupLogicalRecords().get(0);
			lk = viewSource.getLookupPaths(lr.getId()).get(0);
			lkFields = viewSource.getLookupSymbolicFields(lk.getId());
		} catch (SAFRException e) {
			correctException = true;
			logger.log(Level.SEVERE, "", e);
		}
		assertFalse(correctException);
		assertNotNull(lkFields);
		assertEquals(3, lkFields.size());

	}

    public void testGetLookupSymbolicFields_CQ9569() throws SAFRException {
        ViewSource viewSource = null;
        View view = null;
        List<LookupPathSourceField> lkFields = null;
        correctException = false;
        helper.initDataLayer(100);
        view = SAFRApplication.getSAFRFactory().getView(2604);
        List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
        viewSource = viewsrcs.get(0);
        lkFields = viewSource.getLookupSymbolicFields(253);        
        assertEquals(lkFields.size(), 2);
        assertEquals(lkFields.get(0).getSymbolicName(), "TSPAN_DATE");
        assertEquals(lkFields.get(1).getSymbolicName(), "TSPAN_KEYWORD");
        lkFields = viewSource.getLookupSymbolicFields(1959);        
        assertEquals(lkFields.size(), 2);
        String symName1 = lkFields.get(0).getSymbolicName();
        String symName2 = lkFields.get(1).getSymbolicName();
        if ((symName1.equals("DATETYPE") || symName1.equals("KEYWORD")) &&
            (symName2.equals("DATETYPE") || symName2.equals("KEYWORD"))) {
        }
        else {
            fail();
        }
        helper.closeDataLayer();
    }
	
	public void testValidateExtractRecordFilter() throws SAFRException {
		dbStartup();
		helper.setEnv(100);

		ViewSource viewSource = null;
		View view = null;
		correctException = false;

		view = SAFRApplication.getSAFRFactory().getView(2604, 100);
		List<ViewSource> viewsrcs = ViewFactory.getViewSources(view);
		viewSource = viewsrcs.get(0);

		try {
			viewSource.validateExtractRecordFilter("SKIPIF({PROD_ID}=" + "+AAA" + "");

		} catch (SAFRException e) {
			correctException = true;
		}
		assertTrue(correctException);
	}

}
