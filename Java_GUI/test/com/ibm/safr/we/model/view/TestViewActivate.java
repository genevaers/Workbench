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


import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.SAFRApplication;

public class TestViewActivate extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestView");

	static String VIEW_KEY = "e_viewtbl";
	static String TABLE_NAME = "VIEW";
	static String COL_ID = "VIEWID";
	static String DEP_TABLE_LIST[] = { "VIEWHEADERFOOTER",
			"VIEWLOGICDEPEND", "VIEWSORTKEY",
			"VIEWCOLUMNSOURCE", "VIEWCOLUMN", "VIEWSOURCE",
			TABLE_NAME };
	
	TestDataLayerHelper helper = new TestDataLayerHelper();

    public void setUp() {
        dbStartup();
    }
    
    public void dbStartup() {
        try {
            helper.initDataLayer(114);
        } catch (DAOException e) {
            assertFalse(true);
        }
    }

    
    public void tearDown() throws DAOException {
        helper.closeDataLayer();
    }       
    
    public void testActivateBadColumnFormula() throws SAFRException {
        
        View view = null;
        view = SAFRApplication.getSAFRFactory().getView(8658);
        try {
            view.activate();
            fail();
        } catch (SAFRViewActivationException e) {
            ViewActivationError err =  e.getActivationLogNew().get(0);
            String emsg = err.getErrorText();
            assertEquals(emsg.trim(), "ERROR: line 1:24: Unexpected token: test"); 
            assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);
            err =  e.getActivationLogOld().get(0);
            emsg = err.getErrorText();
            assertEquals(emsg, "Syntax Error (Line 1) : Unexpected token 'test'.");
            assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);            
        }
    }

    public void testActivateEmptyNumericConstant() throws SAFRException {
        
        View view = null;
        view = SAFRApplication.getSAFRFactory().getView(8652);
        try {
            view.activate();
            fail();
        } catch (SAFRViewActivationException e) {
            ViewActivationError err =  e.getActivationLogNew().get(0);
            String emsg = err.getErrorText();
            assertEquals(emsg, "ERROR: Cannot have blank value for non-alphanumeric column.");
            assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);
            err =  e.getActivationLogOld().get(0);
            emsg = err.getErrorText();
            assertEquals(emsg, "Constant field is missing a value.");
            assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);            
        }
    }
    
	public void testActivate() throws SAFRException {

		// To check validate with no errors.
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(1662);
 		view.activate();
 		view.store();

		view = SAFRApplication.getSAFRFactory().getView(1662);
		assertTrue(view.getStatusCode().equals(SAFRApplication.getSAFRFactory()
				.getCodeSet(CodeCategories.VIEWSTATUS).getCode(Codes.ACTIVE)));
		view.makeViewInactive();
		view.store();
	}

	public void testActivateConstants() throws SAFRException {

		// To check validate with no errors.
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8651);
 		view.activate();
 		view.store();

		view = SAFRApplication.getSAFRFactory().getView(8651);
		assertTrue(view.getStatusCode().equals(SAFRApplication.getSAFRFactory()
				.getCodeSet(CodeCategories.VIEWSTATUS).getCode(Codes.ACTIVE)));
		view.makeViewInactive();
		view.store();
	}
	
	public void testActivateCreateNotSaved() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().createView();
 		try {
			view.activate();
			fail();
		} catch (SAFRException e) {
			String emsg = e.getMessage();
			assertEquals(emsg, "A new View must be saved before activation.");
		}
	}
		
	public void testActivateNoSortKey() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8640);
 		try {
			view.activate();
			fail();
		} catch (SAFRViewActivationException e) {
			String emsg = e.getActivationLogNew().get(0).getErrorText();
			assertEquals(emsg, "This View must have at least one Sort Key.");
            emsg = e.getActivationLogOld().get(0).getErrorText();
            assertEquals(emsg, "This View must have at least one Sort Key.");
		}
	}
	
	public void testActivateNoViewSource() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8641);
 		try {
			view.activate();
			fail();
		} catch (SAFRViewActivationException e) {
			String emsg = e.getActivationLogNew().get(0).getErrorText();
			assertEquals(emsg, "This View must have at least one view source.");
            emsg = e.getActivationLogOld().get(0).getErrorText();
            assertEquals(emsg, "This View must have at least one view source.");
		}
	}

	public void testActivateNoSortKeyBreak() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8642);
 		try {
			view.activate();
			fail();
		} catch (SAFRViewActivationException e) {
			String emsg = e.getActivationLogNew().get(0).getErrorText();
			assertEquals(emsg, "Sort Key Footer Option must be 'Print' for the last sort key of a hard copy summary view.");
            emsg = e.getActivationLogOld().get(0).getErrorText();
            assertEquals(emsg, "Sort Key Footer Option must be 'Print' for the last sort key of a hard copy summary view.");
		}
	}

	public void testActivateDuplicateViewSource() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8643);
 		try {
			view.activate();
			fail();
		} catch (SAFRViewActivationException e) {
			String emsg = e.getActivationLogNew().get(0).getErrorText();
			assertEquals(emsg, "Duplicate View sources are not allowed.");
            emsg = e.getActivationLogOld().get(0).getErrorText();
            assertEquals(emsg, "Duplicate View sources are not allowed.");
		}
	}

	public void testActivateInvalidSortKey() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8644);
 		try {
			view.activate();
			fail();
		} catch (SAFRViewActivationException e) {
			String emsg = e.getActivationLogNew().get(0).getErrorText();
			assertEquals(emsg, "Sort Key# 1 Data type errors: Alphanumeric length must be between 1 and 256 (inclusive).");
		}
	}

	public void testActivateHardcopyBinary() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8645);
 		try {
			view.activate();
			fail();
		} catch (SAFRViewActivationException e) {
			String emsg = e.getActivationLogNew().get(0).getErrorText();
			assertEquals(emsg, "ERROR: Hardcopy and Delimited outputs cannot contain Binary visible columns.");
            emsg = e.getActivationLogOld().get(0).getErrorText();
            assertEquals(emsg, "Hardcopy, DrillDown and Delimited outputs cannot contain Binary visible columns.");
		}
	}
	
	public void testActivateInvalidColumn() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8646);
 		try {
			view.activate();
			fail();
		} catch (SAFRViewActivationException e) {
			ViewActivationError err =  e.getActivationLogNew().get(0);
			String emsg = err.getErrorText();
			assertEquals(emsg, "Data type errors: Alphanumeric length must be between 1 and 256 (inclusive).");
			assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);
            err =  e.getActivationLogOld().get(0);
            emsg = err.getErrorText();
            assertEquals(emsg, "Data type errors: Alphanumeric length must be between 1 and 256 (inclusive).");
            assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);
		}
	}
	
	public void testActivateBadNumericConstant() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8653);
 		try {
			view.activate();
			fail();
		} catch (SAFRViewActivationException e) {
			ViewActivationError err =  e.getActivationLogNew().get(0);
			String emsg = err.getErrorText();
			assertEquals(emsg, "ERROR: Cannot have alphanumeric value for non-alphanumeric column.");			
			assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);
            err =  e.getActivationLogOld().get(0);
            emsg = err.getErrorText();
            assertEquals(emsg, "Cannot have alphanumeric value for non-alphanumeric column.");          
            assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);
		}
	}

	public void testActivateBlankSourceField() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8654);
 		try {
			view.activate();
			fail();
		} catch (SAFRViewActivationException e) {
			ViewActivationError err =  e.getActivationLogNew().get(0);
			String emsg = err.getErrorText();
			assertEquals(emsg, "ERROR: No source field selected.");
			assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);
            err =  e.getActivationLogOld().get(0);
            emsg = err.getErrorText();
            assertEquals(emsg, "No source field selected.");
            assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);
		}
	}

	public void testActivateBlankLookupPath() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8655);
 		try {
			view.activate();
			fail();
		} catch (SAFRViewActivationException e) {
			ViewActivationError err =  e.getActivationLogNew().get(0);
			String emsg = err.getErrorText();
			assertEquals(emsg, "ERROR: No lookup field selected.");
			assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);
            err =  e.getActivationLogOld().get(0);
            emsg = err.getErrorText();
            assertEquals(emsg, "No lookup field selected.");
            assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);
		}
	}

	public void testActivateEffectiveDate() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8656);
 		view.activate();
 		view.store();

		view = SAFRApplication.getSAFRFactory().getView(8656);
		assertTrue(view.getStatusCode().equals(SAFRApplication.getSAFRFactory()
				.getCodeSet(CodeCategories.VIEWSTATUS).getCode(Codes.ACTIVE)));
		view.makeViewInactive();
		view.store();
	}
	
	public void testActivateBadEffectiveDate() throws SAFRException {
		
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(8657);
 		try {
			view.activate();
			fail();
		} catch (SAFRViewActivationException e) {
			ViewActivationError err =  e.getActivationLogNew().get(0);
			String emsg = err.getErrorText();
			assertEquals(emsg, "ERROR: No effective date field selected.");
			assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);
            err =  e.getActivationLogOld().get(0);
            emsg = err.getErrorText();
            assertEquals(emsg, "No effective date field selected.");
            assertEquals(err.getViewColumn().getColumnNo().intValue(), 1);
		}
	}

	
}
