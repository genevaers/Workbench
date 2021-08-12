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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.MockDAOFactory;
import com.ibm.safr.we.data.dao.ViewColumnDAO;
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.TestModelBase;
import com.ibm.safr.we.model.associations.ComponentAssociation;

public class TestViewActivator extends TestModelBase {
        
    @Test
    public void testCheckUnsavedView() {
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        ViewActivator activator = new ViewActivator(view);
        try {
            activator.checkSavedView(true);
            fail();
        } catch (SAFRException e) {
        }
    }
    
    @Test
    public void testCheckSavedView() {
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        view.setId(1);
        ViewActivator activator = new ViewActivator(view);
        try {
            activator.checkSavedView(true);
        } catch (SAFRException e) {
            fail();
        }               
    }
    
    @Test 
    public void testCheckFormatHasntSortKey() {
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        ViewActivator activator = new ViewActivator(view);
        activator.checkFormatHasSortKey(true);        
        assertTrue(activator.getVaException().hasErrorOccured());
        assertEquals(getFirstLogEntry(activator), "This View must have at least one Sort Key.");        
    }

    @Test 
    public void testCheckFormatHasSortKey() throws SAFRException {
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        ViewColumn col = view.addViewColumn(0);
        view.addSortKey(col);
        ViewActivator activator = new ViewActivator(view);
        activator.checkFormatHasSortKey(true);        
        assertFalse(activator.getVaException().hasErrorOccured());
    }
        
    @Test
    public void testCheckHasntViewSources() {
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        ViewActivator activator = new ViewActivator(view);
        activator.checkHasViewSources(true);           
        assertTrue(activator.getVaException().hasErrorOccured());
        assertEquals(getFirstLogEntry(activator), "This View must have at least one view source.");                
    }

    @Test
    public void testCheckHasViewSources() throws SAFRException {
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        view.addViewSource();
        ViewActivator activator = new ViewActivator(view);        
        activator.checkHasViewSources(true);           
        assertFalse(activator.getVaException().hasErrorOccured());
    }
    
    @Test 
    public void testCheckHardcopySortKey() {
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        view.setOutputFormat(OutputFormat.Format_Report);
        view.setFormatPhaseRecordAggregationOn(true);
        ViewColumn col = view.addViewColumn(0);
        ViewSortKey sortKey = view.addSortKey(col);
        sortKey.setDisplayModeCode(SAFRApplication.getSAFRFactory().
            getCodeSet(CodeCategories.SORTDSP).getCode((Codes.CATEGORIZE)));        
        sortKey.setFooterOption(SAFRApplication.getSAFRFactory().
            getCodeSet(CodeCategories.SORTBRKFTR).getCode((Codes.PRINT)));
        ViewActivator activator = new ViewActivator(view);
        activator.checkConsistentHardcopy(true);
        assertFalse(activator.getVaException().hasErrorOccured());        
    }

    @Test 
    public void testCheckHardcopyBadSortKey() {
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        view.setOutputFormat(OutputFormat.Format_Report);
        view.setFormatPhaseRecordAggregationOn(true);
        ViewColumn col = view.addViewColumn(0);
        ViewSortKey sortKey = view.addSortKey(col);
        sortKey.setDisplayModeCode(SAFRApplication.getSAFRFactory().
            getCodeSet(CodeCategories.SORTDSP).getCode((Codes.CATEGORIZE)));
        sortKey.setFooterOption(SAFRApplication.getSAFRFactory().
            getCodeSet(CodeCategories.SORTBRKFTR).getCode((Codes.SUPPRESS_PRINT)));
        ViewActivator activator = new ViewActivator(view);
        activator.checkConsistentHardcopy(true);
        assertTrue(activator.getVaException().hasErrorOccured());     
        assertEquals(getFirstLogEntry(activator), "Sort Key Footer Option must be 'Print' for the last sort key of a hard copy summary view.");                        
    }

    @Test
    public void testCheckDuplicateViewSource() {
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        ViewSource vs1 = view.addViewSource();
        LogicalRecord rec = SAFRApplication.getSAFRFactory().createLogicalRecord();
        ComponentAssociation fileAssoc = new ComponentAssociation(rec, 1,"", EditRights.ReadModifyDelete);     
        fileAssoc.setAssociationId(1);
        vs1.setLrFileAssociation(fileAssoc);
        ViewSource vs2 = view.addViewSource();
        vs2.setLrFileAssociation(fileAssoc);
        ViewActivator activator = new ViewActivator(view);        
        activator.checkDuplicateViewSource(true);           
        assertTrue(activator.getVaException().hasErrorOccured());        
        assertEquals(getFirstLogEntry(activator), "Duplicate View sources are not allowed.");                        
    }

    @Test
    public void testCheckNoDuplicateViewSource() {
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        ViewSource vs1 = view.addViewSource();
        LogicalRecord rec = SAFRApplication.getSAFRFactory().createLogicalRecord();
        ComponentAssociation fileAssoc1 = new ComponentAssociation(rec, 1,"", EditRights.ReadModifyDelete);
        fileAssoc1.setAssociationId(1);
        vs1.setLrFileAssociation(fileAssoc1);
        ViewSource vs2 = view.addViewSource();
        ComponentAssociation fileAssoc2 = new ComponentAssociation(rec, 2,"", EditRights.ReadModifyDelete);        
        fileAssoc2.setAssociationId(2);
        vs2.setLrFileAssociation(fileAssoc2);
        ViewActivator activator = new ViewActivator(view);        
        activator.checkDuplicateViewSource(true);           
        assertFalse(activator.getVaException().hasErrorOccured());        
    }

    @Test
    public void testCheckSortKeyTitles() {
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        view.addViewSource();
        ViewColumn col = view.addViewColumn(0);
        view.addSortKey(col);        
        ViewColumnSource vcs = col.getViewColumnSources().get(0);
        vcs.setSortKeyTitleLookupPathId(1);
        vcs.setSortKeyTitleLRFieldId(1);        
        ViewActivator activator = new ViewActivator(view);        
        activator.checkSortKeyTitles(true);        
        assertFalse(activator.getVaException().hasErrorOccured());        
    }
    
    protected String getFirstLogEntry(ViewActivator activator) {
        return activator.getVaException().getActivationLogNew().get(0).getErrorText();
    }
       
    class ViewColumnMockDAOFactory extends MockDAOFactory {

        @Override
        public ViewColumnDAO getViewColumnDAO() throws DAOException {
            return new ViewColumnDAO() {

                public List<ViewColumnTransfer> getViewColumns(Integer viewId,
                    Integer environmentId) throws DAOException {
                    return null;
                }

                public List<ViewColumnTransfer> persistViewColumns(
                    List<ViewColumnTransfer> viewColTransferList)
                    throws DAOException {
                    return null;
                }

                public void removeViewColumns(List<Integer> vwColumnIds,
                    Integer environmentId) throws DAOException {
                }

            };
        }
        
    }
        
    @Test
    public void testGetActualColumnIds() {
        DAOFactoryHolder.setDaoFactory(new ViewColumnMockDAOFactory());
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        view.addViewColumn(0);
        view.addViewColumn(0);
        view.addViewColumn(0);        
        
        ViewActivator activator = new ViewActivator(view);
        activator.getTempColumnIds();
        
        assertEquals(view.getViewColumns().size(), 3);
        assertEquals(view.getViewColumns().get(0).getId(), new Integer(2));
        assertEquals(view.getViewColumns().get(1).getId(), new Integer(3));
        assertEquals(view.getViewColumns().get(2).getId(), new Integer(4));        
    }
    
    @Test
    public void testCalculateOrdinalPositions() {
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        view.addViewColumn(0);
        view.addViewColumn(0);
        ViewColumn col = view.addViewColumn(0);
        col.setVisible(false);
        view.addViewColumn(0);
        
        ViewActivator activator = new ViewActivator(view);
        activator.calculateOrdinalPositions();
        

        assertEquals(view.getViewColumns().size(), 4);
        assertEquals(view.getViewColumns().get(0).getOrdinalPosition(), new Integer(1));
        assertEquals(view.getViewColumns().get(1).getOrdinalPosition(), new Integer(2));
        assertEquals(view.getViewColumns().get(2).getOrdinalPosition(), new Integer(0));                
        assertEquals(view.getViewColumns().get(3).getOrdinalPosition(), new Integer(3));                        
    }
    
    
        
}
