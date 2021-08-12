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
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.constants.OutputPhase;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRCancelException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.utilities.MockConfirmWarningStrategy;

public class TestView extends TestCase {

    static transient Logger logger = Logger
            .getLogger("com.ibm.safr.we.model.TestView");

    static String TABLE_NAME = "VIEW";
    static String COL_ID = "VIEWID";
    static String DEP_TABLE_LIST[] = { "E_LOGICTBL", "VIEWHEADERFOOTER", 
            "VIEWLOGICDEPEND", "VIEWSORTKEY",
            "VIEWCOLUMNSOURCE", "VIEWCOLUMN", "VIEWSOURCE",
            TABLE_NAME };
    static int ENVIRONID=113;
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

    public void removeView(Integer id) throws DAOException, SQLException {
        SAFRApplication.getSAFRFactory().removeView(id);
    }

    public void tearDown() throws DAOException, SQLException {
        try {
            for (Integer i : delIds) {
                try {
                    removeView(i);
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

    public void testGetId() {
        View view = null;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(8453);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertNotNull(view);
        assertEquals(new Integer(8453), view.getId());
    }

    public void testTransaction() throws SAFRException {
        dbStartup();
        View view = null;

        // Check name and comment when the transaction fails as
        // they were before starting the transaction i.e the View gets saved in
        // the DB and as the View Columns length is null the transactions gets
        // failed
        // and the whole transaction is rolled back.
        try {

            view = SAFRApplication.getSAFRFactory().getView(2041);

            view.setName("Viewchangedname");
            view.setComment("changed comment");
            ViewColumn viewCol = view.getViewColumns().get(0);
            viewCol.setLength(null);
            view.store();
            fail();

        } catch (NullPointerException e) {
            assertEquals("val_format_filter", SAFRApplication.getSAFRFactory()
                    .getView(2041).getName());
            assertEquals("updatedcomment", SAFRApplication.getSAFRFactory()
                    .getView(2041).getComment());
        }

        View view1 = null;
        try {
            // Check the name when the transaction is successful.
            view1 = SAFRApplication.getSAFRFactory().getView(2041);

            view1.setComment("updatedcomment");
            view1.setExtractAggregateBufferSize(1);
            view1.setExtractAggregateBySortKey(true);
            view1.store();
            assertEquals("val_format_filter", SAFRApplication.getSAFRFactory()
                    .getView(2041).getName());

        } catch (NullPointerException e) {
            fail();
        }
    }

    public void testAddViewColumn() {
        View view = null;
        ViewColumn viewCol = null;
        ViewColumn viewCol1 = null;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            viewCol = view.addViewColumn(0);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        // add the column in the end.

        assertEquals(5, view.getViewColumns().size());
        assertEquals(viewCol, view.getViewColumns().get(4));

        try {
            viewCol1 = view.addViewColumn(2);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertEquals(viewCol1, view.getViewColumns().get(1));
        ViewColumnSource viewCol1Source = viewCol1.getViewColumnSources()
                .get(0);
        CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.COLSRCTYPE);
        Code code = codeSet.getCode(Integer.valueOf(Codes.CONSTANT));
        assertEquals(viewCol1Source.getSourceType(), code);

    }

    public void testAddViewSource() {
        View view = null;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }

        assertEquals(1, view.getViewSources().size());
        view.addViewSource();
        assertEquals(2, view.getViewSources().size());
    }

    public void testAddViewColumnSource() {
        View view = null;
        ViewColumn viewCol = null;
        ViewSource viewSource = null;
        ViewColumn newViewCol = null;
        ViewSource newViewSource = null;
        Boolean correctException = false;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        viewCol = view.getViewColumns().get(0);
        viewSource = view.getViewSources().get(0);
        try {
            view.addViewColumnSource(viewCol, viewSource);
        } catch (IllegalArgumentException e) {
            correctException = true;
        }

        assertTrue(correctException);

        ViewColumnSource viewColSource = null;
        newViewSource = view.addViewSource();
        newViewCol = new ViewColumn(view);
        viewColSource = view.addViewColumnSource(newViewCol, newViewSource);
        if (!view.getViewColumnSources().getActiveItems()
                .contains(viewColSource)) {
            assertTrue(false);
        }

    }

    public void testChangeSortKeySequence() {
        View view = null;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertEquals(2, view.getViewSortKeys().size());
        ViewSortKey sortKey1 = view.getViewSortKeys().get(0);
        ViewSortKey sortKey2 = view.getViewSortKeys().get(1);

        view.changeSortKeySequence(2, 1);
        assertEquals(sortKey2, view.getViewSortKeys().get(0));
        assertEquals(sortKey1, view.getViewSortKeys().get(1));

        sortKey2 = view.getViewSortKeys().get(0);
        view.changeSortKeySequence(1, 2);
        assertEquals(sortKey2, view.getViewSortKeys().get(1));
    }

    public void testRemoveSortKey() {
        View view = null;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        ViewSortKey sortKey = view.getViewSortKeys().get(1);
        ViewColumn viewCol = sortKey.getViewColumn();
        view.removeSortKey(viewCol);
        assertEquals(2, view.getViewSortKeys().size());

    }

    public void testMoveColumnRight() {
        View view = null;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        ViewColumn viewCol = view.getViewColumns().get(0);
        view.moveColumnRight(viewCol);
        assertEquals(viewCol, view.getViewColumns().get(1));
    }

    public void testMoveColumnLeft() {
        View view = null;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        ViewColumn viewCol = view.getViewColumns().get(2);
        view.moveColumnLeft(viewCol);
        assertEquals(viewCol, view.getViewColumns().get(1));
    }

    public void testCalculateStartPosition() {
        View view = null;
        dbStartup();
        view = SAFRApplication.getSAFRFactory().getView(2206);
        ViewColumn viewCol = view.getViewColumns().get(0);
        viewCol.setLength(11);
        ViewColumn viewCol1 = view.getViewColumns().get(1);
        assertEquals(12, viewCol1.getStartPosition().intValue());

        viewCol.setVisible(false);
        assertEquals(1, viewCol1.getStartPosition().intValue());

        view.setOutputFormat(OutputFormat.Format_Fixed_Width_Fields);
        viewCol.setVisible(true);
        viewCol.setSpacesBeforeColumn(8);
        assertEquals(9, viewCol.getStartPosition().intValue());
        assertEquals(20, viewCol1.getStartPosition().intValue());
    }

    public void testGetSetStatusCode() {
        View view = null;
        Code statusCode = null;
        Code inactCode = null;
        CodeSet codeSet = null;
        boolean correctException = false;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            codeSet = SAFRApplication.getSAFRFactory().getCodeSet("VIEWSTATUS");
            inactCode = codeSet.getCode("INACT");
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        statusCode = view.getStatusCode();
        assertEquals(statusCode, inactCode);

        try {
            view.setStatusCode(inactCode);
        } catch (NullPointerException npe) {
            logger.log(Level.SEVERE, "", npe);
            assertTrue(false);
        }
        assertEquals(view.getStatusCode(), inactCode);

        // test for null Status Code
        try {
            view.setStatusCode(null);
        } catch (NullPointerException npe) {
            correctException = true;
        }
        assertTrue(correctException);
    }

    public void testGetSetExtractWorkFileQty() {
        dbStartup();
        View view = null;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        view.setExtractWorkFileNo(5);
        assertEquals(new Integer(5), view.getExtractWorkFileNo());
    }

    public void testGetSetExtractFileAssociation() {
        dbStartup();
        View view = null;
        FileAssociation fileAssoc = null;
        try {
            fileAssoc = SAFRAssociationFactory
                    .getLogicalFileToPhysicalFileAssociation(1, 1);
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        view.setExtractFileAssociation(fileAssoc);
        try {
            assertEquals(fileAssoc, view.getExtractFileAssociation());
        } catch (DAOException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }

    }

    public void testGetSetLinesPerPage() {
        dbStartup();
        View view = null;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        view.setLinesPerPage(5);
        assertEquals(new Integer(5), view.getLinesPerPage());
    }

    public void testGetSetReportWidth() {
        dbStartup();
        View view = null;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        view.setReportWidth(5);
        assertEquals(new Integer(5), view.getReportWidth());

        view.setReportWidth(null);
        assertEquals(null, view.getReportWidth());
    }

    public void testGetSetSupressZeroRecords() {
        dbStartup();
        View view = null;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            assertEquals(new Boolean(false), view.isSuppressZeroRecords());
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        view.setSuppressZeroRecords(true);
        assertEquals(new Boolean(true), view.isSuppressZeroRecords());

        view.setSuppressZeroRecords(false);
        assertEquals(new Boolean(false), view.isSuppressZeroRecords());
    }

    public void testGetSetExtractMaxRecords() {
        dbStartup();
        View view = null;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        view.setExtractMaxRecords(5);
        assertEquals(new Integer(5), view.getExtractMaxRecords());

        view.setExtractMaxRecords(null);
        assertEquals(null, view.getExtractMaxRecords());
    }

    public void testGetSetAggregateBySortKey() {
        dbStartup();
        View view = null;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            assertEquals(new Boolean(false), view.isExtractAggregateBySortKey());
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        view.setExtractAggregateBySortKey(true);
        assertEquals(new Boolean(true), view.isExtractAggregateBySortKey());

        view.setExtractAggregateBySortKey(false);
        assertEquals(new Boolean(false), view.isExtractAggregateBySortKey());
    }

    public void testGetSetAggregateBufferSize() {
        dbStartup();
        View view = null;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        view.setExtractAggregateBufferSize(5);
        assertEquals(new Integer(5), view.getExtractAggregateBufferSize());

        view.setExtractAggregateBufferSize(null);
        assertEquals(null, view.getExtractAggregateBufferSize());
    }

    public void testGetSetOutputMaxRecCount() {
        dbStartup();
        View view = null;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        view.setOutputMaxRecCount(5);
        assertEquals(new Integer(5), view.getOutputMaxRecCount());

        view.setOutputMaxRecCount(null);
        assertEquals(new Integer(0), view.getOutputMaxRecCount());
    }

    public void testGetSetControlRecord() {
        dbStartup();
        View view = null;
        ControlRecord controlRecord = null;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            controlRecord = SAFRApplication.getSAFRFactory()
                    .getControlRecord(1);
            view.setControlRecord(controlRecord);
            assertEquals(controlRecord, view.getControlRecord());
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
    }

    public void testGetSetFileFieldDelimiterCode() {
        dbStartup();
        View view = null;
        Code fileFieldDelim = null;
        Code fileFieldDelimCode1 = null;
        Code fileFieldDelimCode2 = null;
        CodeSet codeSet = null;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FLDDELIM");
            fileFieldDelimCode1 = codeSet.getCode("COMMA");
            fileFieldDelimCode2 = codeSet.getCode("PIPE");
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        fileFieldDelim = view.getFileFieldDelimiterCode();
        assertEquals(fileFieldDelim, null);

        view.setFileFieldDelimiterCode(fileFieldDelimCode1);
        assertEquals(view.getFileFieldDelimiterCode(), fileFieldDelimCode1);

        view.setFileFieldDelimiterCode(fileFieldDelimCode2);
        assertEquals(view.getFileFieldDelimiterCode(), fileFieldDelimCode2);
    }

    public void testGetSetWriteExit() {
        dbStartup();
        View view = null;
        UserExitRoutine uer = null;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            uer = SAFRApplication.getSAFRFactory().getUserExitRoutine(1);
            view.setWriteExit(uer);
            assertEquals(uer, view.getWriteExit());
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
    }

    public void testGetSetWriteExitParams() {
        View view = null;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            view.setWriteExitParams("test");
            assertEquals(new String("test"), view.getWriteExitParams());
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
    }

    public void testGetSetFormatExit() {
        View view = null;
        UserExitRoutine uer = null;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            uer = SAFRApplication.getSAFRFactory().getUserExitRoutine(1);
            view.setFormatExit(uer);
            assertEquals(uer, view.getFormatExit());
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
    }

    public void testGetSetFormatExitParams() {
        View view = null;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            view.setFormatExitParams("test");
            assertEquals(new String("test"), view.getFormatExitParams());
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
    }

    public void testGetSetFormatRecordFilter() {
        View view = null;
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            view.setFormatRecordFilter("test");
            assertEquals(new String("test"), view.getFormatRecordFilter());
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
    }

    public void testGetSetFormatPhaseRecordAggregationOn() {
        View view = null;

        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            assertEquals(new Boolean(true),
                    view.isFormatPhaseRecordAggregationOn());
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        view.setFormatPhaseRecordAggregationOn(true);
        assertEquals(new Boolean(true), view.isFormatPhaseRecordAggregationOn());

        view.setFormatPhaseRecordAggregationOn(false);
        assertEquals(new Boolean(false),
                view.isFormatPhaseRecordAggregationOn());

        // test for Record Aggregation Function if Format Phase Record
        // Aggregation is OFF
        for (ViewColumn col : view.getViewColumns().getActiveItems()) {
            assertNull(col.getRecordAggregationCode());
        }
    }

    public void testGetSetOutputFormat() {
        View view = null;
        CodeSet codeSet = null;
        Code code, code1 = null;
        ViewColumn col, col1 = null;

        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            assertEquals(OutputPhase.Format, view.getOutputPhase());
            view.setOutputFormat(OutputFormat.Format_Fixed_Width_Fields);
            assertEquals(OutputFormat.Format_Fixed_Width_Fields, view.getOutputFormat());
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }

        // test for resetting fields when output format is changed
        try {
            view = SAFRApplication.getSAFRFactory().getView(8546);

            // for Hardcopy Report
            view.setOutputFormat(OutputFormat.Format_Report);
            col = view.getViewColumns().getActiveItems().get(0);
            codeSet = SAFRApplication.getSAFRFactory().getCodeSet("SORTDSP");
            code = codeSet.getCode("CAT");
            assertEquals(col.getViewSortKey().getDisplayModeCode(), code);
            assertEquals(col.getSortkeyFooterLabel(), "Subtotal,");

            col1 = view.getViewColumns().getActiveItems().get(1);
            code1 = codeSet.getCode("ASDTA");
            assertEquals(col1.getViewSortKey().getDisplayModeCode(), code1);
            assertEquals(col1.getSortkeyFooterLabel(), null);

            // for Flat-File
            view.setOutputFormat(OutputFormat.Extract_Fixed_Width_Fields);
            assertEquals(col.getViewSortKey().getDisplayModeCode(), null);
            assertEquals(col.getSortkeyFooterLabel(), null);
            assertEquals(col.getSpacesBeforeColumn(), new Integer(0));
            assertEquals(col.getHeaderAlignmentCode(), null);
            assertNull(col.getGroupAggregationCode());

        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }

    }

    public void testGetSetOutputFormatType() {
        View view = null;
        
        dbStartup();
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            assertEquals(OutputFormat.Format_Fixed_Width_Fields,view.getOutputFormat());
            view.setOutputFormat(OutputFormat.Format_Delimited_Fields);
            assertEquals(OutputFormat.Format_Delimited_Fields, view.getOutputFormat());

            for (ViewColumn col : view.getViewColumns().getActiveItems()) {
                assertEquals(null, col.getDataAlignmentCode());
                assertEquals(null, col.getGroupAggregationCode());
            }
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
    }

    public void testGetHeader() throws SAFRException {
        View view = null;
        dbStartup();
        view = SAFRApplication.getSAFRFactory().getView(8518);
        CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.FUNCTION);
        Code functionCode = codeSet.getCode(Codes.HF_TEXT);
        View.HeaderFooterItems hf = view.getHeader();
        HeaderFooterItem hfItem = hf.getItems().get(0);
        assertNotNull(hf);
        assertEquals(functionCode, hfItem.getFunctionCode());
    }

    public void testGetFooter() throws SAFRException {
        View view = null;
        dbStartup();
        view = SAFRApplication.getSAFRFactory().getView(8518);
        CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.FUNCTION);
        Code functionCode = codeSet.getCode(Codes.HF_TEXT);
        View.HeaderFooterItems hf = view.getFooter();
        HeaderFooterItem hfItem = hf.getItems().get(0);
        assertNotNull(hf);
        assertEquals(functionCode, hfItem.getFunctionCode());
    }

    public void testAddItem() throws SAFRException {
        dbStartup();
        View view = SAFRApplication.getSAFRFactory().getView(8518, 1);
        CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.FUNCTION);
        Code funtionCode = codeSet.getCode(Codes.HF_FISCALDATE);
        CodeSet codeSet1 = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.JUSTIFY);
        Code justifyCode = codeSet1.getCode(Codes.RIGHT);
        View.HeaderFooterItems hf = view.new HeaderFooterItems(view, true);
        HeaderFooterItem hfItem = hf.addItem(funtionCode, justifyCode, 1, 1,
                "Test");
        assertTrue(hf.getItems().contains(hfItem));

    }

    public void testGetItems() throws SAFRException {
        dbStartup();
        View view = SAFRApplication.getSAFRFactory().getView(8518, 1);
        View.HeaderFooterItems hf = view.new HeaderFooterItems(view, true);
        CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.FUNCTION);
        Code funtionCode = codeSet.getCode(Codes.HF_FISCALDATE);
        CodeSet codeSet1 = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.JUSTIFY);
        Code justifyCode = codeSet1.getCode(Codes.RIGHT);
        hf.addItem(funtionCode, justifyCode, 1, 1, "Test");
        assertEquals(1, hf.getItems().size());

        Code funtionCode1 = codeSet.getCode(Codes.HF_COMPANYNAME);
        Code justifyCode1 = codeSet1.getCode(Codes.LEFT);
        hf.addItem(funtionCode1, justifyCode1, 1, 1, "Test");
        assertEquals(2, hf.getItems().size());

        Code funtionCode2 = codeSet.getCode(Codes.HF_PAGENUMBER);
        Code justifyCode2 = codeSet1.getCode(Codes.LEFT);
        hf.addItem(funtionCode2, justifyCode2, 1, 1, "Test");
        assertEquals(3, hf.getItems().size());
    }

    public void testClearItems() throws SAFRException {
        dbStartup();
        View view = SAFRApplication.getSAFRFactory().getView(8518, 1);
        View.HeaderFooterItems hf = view.new HeaderFooterItems(view, true);
        CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.FUNCTION);
        Code funtionCode = codeSet.getCode(Codes.HF_FISCALDATE);
        CodeSet codeSet1 = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.JUSTIFY);
        Code justifyCode = codeSet1.getCode(Codes.RIGHT);
        hf.addItem(funtionCode, justifyCode, 1, 1, "Test");
        Code funtionCode1 = codeSet.getCode(Codes.HF_COMPANYNAME);
        Code justifyCode1 = codeSet1.getCode(Codes.LEFT);
        hf.addItem(funtionCode1, justifyCode1, 1, 1, "Test");
        Code funtionCode2 = codeSet.getCode(Codes.HF_PAGENUMBER);
        Code justifyCode2 = codeSet1.getCode(Codes.LEFT);
        hf.addItem(funtionCode2, justifyCode2, 1, 1, "Test");
        hf.clearItems();

        assertEquals(0, hf.getItems().size());
    }

    public void testStoreActivateUpdate() throws SAFRException, SQLException {
        dbStartup();
        View rec = SAFRApplication.getSAFRFactory().getView(8719);
        assertNull(rec.getActivatedTime());
        assertNull(rec.getActivatedBy());
        Date upDate = rec.getModifyTime();
        String upBy = rec.getModifyBy();
        CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet("VIEWSTATUS");
        Code statusCode = codeSet.getCode("ACTVE");
        rec.setStatusCode(statusCode);
        rec.store();
        assertNotNull(rec.getActivatedTime());
        assertNotNull(rec.getActivatedBy());        
        assertEquals(upDate,rec.getModifyTime());
        assertEquals(upBy,rec.getModifyBy());
        
        // cleanup
        Connection con = ((PGDAOFactory) DAOFactoryHolder.getDAOFactory()).getConnection();
        ConnectionParameters params = ((PGDAOFactory) DAOFactoryHolder.getDAOFactory()).getConnectionParameters();
        String updateStr = 
            "UPDATE " + params.getSchema() + ".VIEW " +
            "SET LASTACTTIMESTAMP=NULL,LASTACTUSERID=NULL,VIEWSTATUSCD='INACT' " +
            "WHERE ENVIRONID=? AND VIEWID=?";
        PreparedStatement pst = con.prepareStatement(updateStr);
        pst.setInt(1, ENVIRONID);
        pst.setInt(2, 8719);
        pst.executeUpdate();
    }
    
    public void testStoreSource() throws SAFRException {
        dbStartup();
        View view = null;

        view = SAFRApplication.getSAFRFactory().createView();
        ViewSource source = view.addViewSource();
        LogicalRecord rec = SAFRApplication.getSAFRFactory().getLogicalRecord(652);
        source.setLrFileAssociation(rec.getLogicalFileAssociations().get(0));
        view.store();
        
        SAFRApplication.getSAFRFactory().removeView(view.getId());
    }
        
    public void testStore() throws SAFRException {
        dbStartup();
        View view = null;
        CodeSet codeSet = null;
        Code statusCode = null;
        CodeSet codeSet1 = null;
        Code functionCode = null;
        Code functionCode1 = null;
        Code functionCode2 = null;
        CodeSet codeSet2 = null;
        Code justifyCode = null;
        Code justifyCode1 = null;
        Code justifyCode2 = null;
        HeaderFooterItem hfItem1 = null;
        HeaderFooterItem hfItem2 = null;
        HeaderFooterItem hfItem3 = null;
        Boolean correctException = false;
        Boolean noException = false;

        try {
            view = SAFRApplication.getSAFRFactory().createView();
            // nothing is set.
            view.store();
            delIds.add(view.getId());
            correctException = true;
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertTrue(correctException);
        assertNull(view.getActivatedTime());
        assertNull(view.getActivatedBy());

        ControlRecord cr = null;
        UserExitRoutine uer = null;
        correctException = false;
        FileAssociation fileAssoc = null;
        View view1 = null;
        try {

            view1 = SAFRApplication.getSAFRFactory().createView();

            codeSet = SAFRApplication.getSAFRFactory().getCodeSet("VIEWSTATUS");
            statusCode = codeSet.getCode("ACTVE");

            cr = SAFRApplication.getSAFRFactory().getControlRecord(1);
            uer = SAFRApplication.getSAFRFactory().getUserExitRoutine(1);

            fileAssoc = SAFRAssociationFactory
                    .getLogicalFileToPhysicalFileAssociation(1, 1);
            view1.setName("Test22334");
            view1.setExtractWorkFileNo(5);
            view1.setExtractFileAssociation(fileAssoc);
            view1.setLinesPerPage(5);
            view1.setExtractMaxRecords(1);
            view1.setExtractAggregateBufferSize(1);
            view1.setExtractAggregateBySortKey(true);
            view1.setFormatPhaseRecordAggregationOn(false);
            view1.setFormatRecordFilter("test");
            view1.setReportWidth(1);
            view1.setSuppressZeroRecords(true);
            view1.setOutputMaxRecCount(3);
            view1.setControlRecord(cr);
            view1.setWriteExit(uer);
            view1.setFormatExit(uer);
            view1.setOutputFormat(OutputFormat.Format_Fixed_Width_Fields);
            view1.setStatusCode(statusCode);

            view1.store();
            delIds.add(view1.getId());
            noException = true;
        } catch (SAFRException e) {
            correctException = true;
            e.printStackTrace();
        }
        assertFalse(correctException);
        assertTrue(noException);
        assertEquals(statusCode, view1.getStatusCode());
        try {
            assertEquals(new Integer(1), view1.getControlRecord().getId());
            assertEquals(new Integer(5), view1.getExtractWorkFileNo());
            assertEquals(new Integer(5), view1.getLinesPerPage());
            assertEquals(new Integer(1), view1.getExtractMaxRecords());
            assertEquals(new Integer(0), view1.getExtractAggregateBufferSize());
            assertEquals(new Boolean(false), view1.isExtractAggregateBySortKey());
            assertEquals(new Boolean(false),
                    view1.isFormatPhaseRecordAggregationOn());
            assertEquals(new String("test"), view1.getFormatRecordFilter());
            assertEquals(new Boolean(true), view1.isFormatPhaseInUse());
            assertEquals(new Integer(1), view1.getReportWidth());
            assertEquals(new Boolean(true), view1.isSuppressZeroRecords());
            assertEquals(new Integer(3), view1.getOutputMaxRecCount());
            assertEquals(new Integer(1), view1.getWriteExit().getId());
            assertEquals(new Integer(1), view1.getFormatExit().getId());
            assertEquals(OutputPhase.Format, view1.getOutputPhase());
            assertEquals(OutputFormat.Format_Fixed_Width_Fields,view1.getOutputFormat());
            assertEquals(fileAssoc, view1.getExtractFileAssociation());
            assertNotNull(view1.getActivatedTime());
            assertNotNull(view1.getActivatedBy());

        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
        }

        // test for header/footer
        ControlRecord cr1 = null;
        UserExitRoutine uer1 = null;
        correctException = false;
        FileAssociation fileAssoc1 = null;
        View view2 = null;
        codeSet1 = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.FUNCTION);
        codeSet2 = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.JUSTIFY);
        functionCode = codeSet1.getCode(Codes.HF_FISCALDATE);
        justifyCode = codeSet2.getCode(Codes.RIGHT);
        functionCode1 = codeSet1.getCode(Codes.HF_COMPANYNAME);
        justifyCode1 = codeSet2.getCode(Codes.LEFT);
        functionCode2 = codeSet1.getCode(Codes.HF_PROCESSDATE);
        justifyCode2 = codeSet2.getCode(Codes.CENTER);
        try {
            view2 = SAFRApplication.getSAFRFactory().createView();

            codeSet = SAFRApplication.getSAFRFactory().getCodeSet("VIEWSTATUS");
            statusCode = codeSet.getCode("ACTVE");

            cr1 = SAFRApplication.getSAFRFactory().getControlRecord(1);
            uer1 = SAFRApplication.getSAFRFactory().getUserExitRoutine(1);

            fileAssoc1 = SAFRAssociationFactory
                    .getLogicalFileToPhysicalFileAssociation(1, 1);
            view2.setName("Test22334");
            view2.setExtractWorkFileNo(5);
            view2.setExtractFileAssociation(fileAssoc1);
            view2.setLinesPerPage(5);
            view2.setExtractMaxRecords(1);
            view2.setExtractAggregateBufferSize(1);
            view2.setExtractAggregateBySortKey(true);
            view2.setFormatPhaseRecordAggregationOn(false);
            view2.setFormatRecordFilter("test");
            view2.setReportWidth(1);
            view2.setSuppressZeroRecords(true);
            view2.setOutputMaxRecCount(3);
            view2.setControlRecord(cr1);
            view2.setWriteExit(uer1);
            view2.setFormatExit(uer1);
            view2.setOutputFormat(OutputFormat.Format_Report);
            view2.setStatusCode(statusCode);
            hfItem1 = view2.getHeader().addItem(functionCode, justifyCode, 1,
                    1, "test");
            hfItem2 = view2.getHeader().addItem(functionCode1, justifyCode1, 1,
                    1, "test");
            hfItem3 = view2.getHeader().addItem(functionCode2, justifyCode2, 1,
                    1, "test");
            view2.store();
            noException = true;
        } catch (SAFRException e) {
            correctException = true;
            e.printStackTrace();
        }
        assertFalse(correctException);
        assertTrue(noException);
        assertEquals(statusCode, view2.getStatusCode());
        try {
            assertEquals(new Integer(1), view2.getControlRecord().getId());
            assertEquals(new Integer(5), view2.getExtractWorkFileNo());
            assertEquals(new Integer(60), view2.getLinesPerPage());
            assertEquals(new Integer(1), view2.getExtractMaxRecords());
            assertEquals(new Integer(0), view2.getExtractAggregateBufferSize());
            assertEquals(new Boolean(false), view2.isExtractAggregateBySortKey());
            assertEquals(new Boolean(false),
                    view2.isFormatPhaseRecordAggregationOn());
            assertEquals(new String("test"), view2.getFormatRecordFilter());
            assertEquals(new Boolean(true), view2.isFormatPhaseInUse());
            assertEquals(new Integer(132), view2.getReportWidth());
            assertEquals(new Boolean(true), view2.isSuppressZeroRecords());
            assertEquals(new Integer(3), view2.getOutputMaxRecCount());
            assertEquals(new Integer(1), view2.getWriteExit().getId());
            assertEquals(new Integer(1), view2.getFormatExit().getId());
            assertEquals(OutputFormat.Format_Report, view2.getOutputFormat());
            assertEquals(fileAssoc, view2.getExtractFileAssociation());
            assertEquals(hfItem1, view2.getHeader().getItems().get(0));
            assertEquals(hfItem2, view2.getHeader().getItems().get(1));
            assertEquals(hfItem3, view2.getHeader().getItems().get(2));
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
        }

        // delete the view
        SAFRApplication.getSAFRFactory().removeView(view2.getId());

        // try updating the deleted view

        view2.setName("NoView");
        correctException = false;

        try {
            view2.store();
        } catch (SAFRException e) {
            e.printStackTrace();
            correctException = true;
        }
        assertTrue(correctException);
    }

    public void testStoreNoPermCreate() throws SAFRException {
        dbStartup();
        helper.setUser("NOPERM");

        CodeSet codeSet = null;
        Code statusCode = null;
        ControlRecord cr = null;
        UserExitRoutine uer = null;
        FileAssociation fileAssoc = null;
        View view = null;

        view = SAFRApplication.getSAFRFactory().createView();

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("VIEWSTATUS");
        statusCode = codeSet.getCode("ACTVE");

        cr = SAFRApplication.getSAFRFactory().getControlRecord(1);
        uer = SAFRApplication.getSAFRFactory().getUserExitRoutine(1);

        fileAssoc = SAFRAssociationFactory
                .getLogicalFileToPhysicalFileAssociation(1, 1);
        view.setName("NoPermCreate");
        view.setExtractWorkFileNo(5);
        view.setExtractFileAssociation(fileAssoc);
        view.setLinesPerPage(5);
        view.setExtractMaxRecords(1);
        view.setExtractAggregateBufferSize(1);
        view.setExtractAggregateBySortKey(true);
        view.setFormatPhaseRecordAggregationOn(false);
        view.setFormatRecordFilter("test");
        view.setReportWidth(1);
        view.setSuppressZeroRecords(true);
        view.setOutputMaxRecCount(3);
        view.setControlRecord(cr);
        view.setWriteExit(uer);
        view.setFormatExit(uer);
        view.setOutputFormat(OutputFormat.Format_Fixed_Width_Fields);
        view.setStatusCode(statusCode);

        try {
            view.store();
            fail();
        } catch (SAFRException e) {
            String mes = e.getMessage();
            assertEquals(mes,
                    "The user is not authorized to create a new view.");
        }
    }

    public void testStoreNoPermUpdate() throws SAFRException {
        dbStartup();
        helper.setUser("NOPERM");

        View view = SAFRApplication.getSAFRFactory().getView(8512);
        view.setName("TestNoPermUpdate");

        try {
            view.store();
            fail();
        } catch (SAFRException e) {
            String mes = e.getMessage();
            assertEquals(mes, "The user is not authorized to update this view.");
        }
    }

    public void testStoreAllLogicTypes() throws SAFRException {

        dbStartup();

        View view = SAFRApplication.getSAFRFactory().getView(1662);
        view.makeViewInactive();
        view.store();
        view.activate();
        view.store();
        view = SAFRApplication.getSAFRFactory().getView(1662);
        assertTrue(view.getStatusCode().equals(
                SAFRApplication.getSAFRFactory()
                        .getCodeSet(CodeCategories.VIEWSTATUS)
                        .getCode(Codes.ACTIVE)));
    }

    public void testStoreRemoveViewSource() throws SAFRException {

        dbStartup();

        View view = SAFRApplication.getSAFRFactory().getView(8662);
        view.removeViewSource(view.getViewSources().get(0));
        view.store();
        view = SAFRApplication.getSAFRFactory().getView(8662);
        assertEquals(view.getViewSources().size(), 0);
        ViewSource src = view.addViewSource();
        LogicalRecord rec = SAFRApplication.getSAFRFactory().getLogicalRecord(652);
        src.setLrFileAssociation(rec.getLogicalFileAssociations().get(0));
        view.store();
    }

    public void testStoreRemoveViewColumnSource() throws SAFRException {

        dbStartup();

        View view = SAFRApplication.getSAFRFactory().getView(8664);
        view.removeViewColumn(view.getViewColumns().get(0));
        view.store();
        view = SAFRApplication.getSAFRFactory().getView(8664);
        assertEquals(view.getViewColumns().size(), 0);
        ViewColumn src = view.addViewColumn(0);
        assertNotNull(src);
        ViewColumnSource colsrc = src.getViewColumnSources().get(0);
        Code sourceFieldType = SAFRApplication.getSAFRFactory().getCodeSet("COLSRCTYPE").getCode(Codes.SOURCE_FILE_FIELD);
        colsrc.setSourceType(sourceFieldType);
        LogicalRecord rec = SAFRApplication.getSAFRFactory().getLogicalRecord(652);        
        colsrc.setLRFieldColumn(rec.getLRFields().get(2));
        view.store();
    }

    public void testStoreEmptyColumnFormula() throws SAFRException {

        dbStartup();

        MockConfirmWarningStrategy viewStrat = new MockConfirmWarningStrategy(false);

        View view = SAFRApplication.getSAFRFactory().getView(8665);
        view.setConfirmWarningStrategy(viewStrat);
        try {
            view.store();
            fail();
        } catch (SAFRCancelException e) {
            assertTrue(viewStrat.isConfirmed());
            assertEquals(viewStrat.getTopic(), "Saving View");
            assertEquals(viewStrat.getShortMessage(), "Extract Column Assignment in Column: 1, View Source: 1 contains no logic text. Continue saving?");
        }
    }

    public void testStoreHeaderFooter() throws SAFRException {

        dbStartup();

        View view = SAFRApplication.getSAFRFactory().getView(8518);
        view.store();
    }
    
    public void testStoreRemoveSortKey() throws SAFRException {

        dbStartup();

        View view = SAFRApplication.getSAFRFactory().getView(8666);
        view.removeSortKey(view.getViewColumns().get(0));
        view.store();
        view = SAFRApplication.getSAFRFactory().getView(8666);
        assertEquals(view.getViewSortKeys().size(), 0);
        ViewSortKey sk = view.addSortKey(view.getViewColumns().get(0));
        assertNotNull(sk);
        view.store();
    }
    
    public void testValidate() throws SAFRException {
        dbStartup();
        View view = null;
        view = SAFRApplication.getSAFRFactory().createView();
        Boolean correctException = false;
        try {
            view.validate();
        } catch (SAFRValidationException e) {
            correctException = true;
        } catch (DAOException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }

        assertTrue(correctException);
        correctException = false;
        view.setName("122#$$SFD");
        try {
            view.validate();
        } catch (SAFRValidationException e) {
            correctException = true;
        } catch (DAOException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }

        assertTrue(correctException);
        correctException = false;
        view.setName("nupur_s5");
        try {
            view.validate();
        } catch (SAFRValidationException e) {
            correctException = true;
        } catch (DAOException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }

        assertTrue(correctException);
        correctException = false;
        view.setName("");
        try {
            view.validate();
        } catch (SAFRValidationException e) {
            correctException = true;
        } catch (DAOException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }

        assertTrue(correctException);
        View view1 = null;
        ViewColumn viewCol = null;
        ViewColumnSource viewColSource = null;
        LookupQueryBean lookupQueryBean = null;
        LogicalRecord lr = null;
        LRField lrField = null;
        correctException = false;
        try {
            lr = SAFRApplication.getSAFRFactory().getLogicalRecord(20);
            lrField = SAFRApplication.getSAFRFactory().getLRFields(lr).get(0);
            view1 = SAFRApplication.getSAFRFactory().getView(2498);
            lookupQueryBean = SAFRQuery.queryLookupPath(11, 1);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        view1.setName("Test_123");
        viewCol = view1.getViewColumns().get(0);
        viewColSource = viewCol.getViewColumnSources().get(0);
        viewColSource.setSortKeyTitleLookupPathQueryBean(lookupQueryBean);
        viewColSource.setSortKeyTitleLRField(lrField);
        assertEquals(new Boolean(true), viewCol.isSortKey());
        assertEquals(1, viewCol.getViewColumnSources().size());
        assertNotNull(lookupQueryBean);
        boolean noException = false;

        try {
            view1.validate();
            noException = true;
        } catch (SAFRValidationException e) {
            correctException = true;
        } catch (DAOException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }

        assertTrue(noException);
    }

    public void testValidateFormatRecordFilter() {
        dbStartup();
        View view = null;
        Boolean correctException = false;
        Boolean noException = false;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2206);
            view.validateFormatRecordFilter("SELECTIF(2>1)");
            noException = true;
        } catch (SAFRViewActivationException se) {
            correctException = true;
        } catch (SAFRException se) {
            logger.log(Level.SEVERE, "", se);
            assertTrue(false);
        }
        assertTrue(noException);

        try {
            view.validateFormatRecordFilter("ABC");
        } catch (SAFRViewActivationException se) {
            correctException = true;
        } catch (SAFRException se) {
            logger.log(Level.SEVERE, "", se);
            assertTrue(false);
        }
        assertTrue(correctException);
    }

    public void testValidateNamingMax() throws SAFRException {
        dbStartup();
        View view = SAFRApplication.getSAFRFactory().createView();
        view.setName("Test01234567890123456789012345678901234567890123456789");

        try {
            view.validate();
            fail();         
        } 
        catch (SAFRValidationException e) {
            String errMsg = e.getErrorMessages().get(0);
            String pattern = "The length of view name 'Test01234567890123456789012345678901234567890123456789' cannot exceed 48 characters.";
            assertEquals(errMsg, pattern);
        }
    }

    public void testValidateControlRecordEmpty() throws SAFRException {
        dbStartup();
        View view = SAFRApplication.getSAFRFactory().createView();
        view.setName("CREmpty");
        view.setControlRecord(null);

        try {
            view.validate();
            fail();         
        } 
        catch (SAFRValidationException e) {
            String errMsg = e.getErrorMessages().get(0);
            String pattern = "Control Record cannot be empty.";
            assertEquals(errMsg, pattern);
        }
    }

    public void testValidateLinesPerPage() throws SAFRException {
        dbStartup();
        View view = SAFRApplication.getSAFRFactory().createView();
        view.setName("LinesPerPage");
        view.setOutputFormat(OutputFormat.Format_Report);
        view.setLinesPerPage(50);
        
        try {
            view.validate();
            fail();         
        } 
        catch (SAFRValidationException e) {
            String errMsg = e.getErrorMessages().get(0);
            String pattern = "Lines per page must be greater than or equal to 54.";
            assertEquals(errMsg, pattern);
        }        
    }
    
    public void testValidateBadWriteExit() throws SAFRException {
        dbStartup();
        View view = SAFRApplication.getSAFRFactory().createView();
        view.setName("BadWriteExit");
        UserExitRoutine ue = SAFRApplication.getSAFRFactory().getUserExitRoutine(82);
        view.setWriteExit(ue);
        
        try {
            view.validate();
            fail();         
        } 
        catch (SAFRValidationException e) {
            String errMsg = e.getErrorMessages().get(0);
            String pattern = "The user exit routine 'REPORT_HEADING_FORMAT_EXIT[82]' is not of type 'Write'. Please select a valid user exit routine.";
            assertEquals(errMsg, pattern);
        }
    }

    public void testValidateBadFormatExit() throws SAFRException {
        dbStartup();
        View view = SAFRApplication.getSAFRFactory().createView();
        view.setName("BadWriteExit");
        UserExitRoutine ue = SAFRApplication.getSAFRFactory().getUserExitRoutine(314);
        view.setFormatExit(ue);
        
        try {
            view.validate();
            fail();         
        } 
        catch (SAFRValidationException e) {
            String errMsg = e.getErrorMessages().get(0);
            String pattern = "The user exit routine 'WRITEEXIT[314]' is not of type 'Format'. Please select a valid user exit routine.";
            assertEquals(errMsg, pattern);
        }
    }
    
    public void testAddSortKey() {
        View view = null;
        dbStartup();
        boolean noException = false;
        try {
            view = SAFRApplication.getSAFRFactory().getView(8512);
            ViewColumn vc = view.getViewColumns().get(1);
            view.addSortKey(vc);
            noException = true;
        } catch (SAFRViewActivationException e1) {
            logger.log(Level.SEVERE, "", e1);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertTrue(noException);
    }

    public void testGetSetFileStringDelimiterCode() {
        View view = null;
        dbStartup();
        Code code = null;
        try {
            CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(
                    "FLDDELIM");
            code = codeSet.getCode("COMMA");
            view = SAFRApplication.getSAFRFactory().getView(2041);
            view.setFileStringDelimiterCode(code);
        } catch (SAFRViewActivationException e1) {
            logger.log(Level.SEVERE, "", e1);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertEquals(code, view.getFileStringDelimiterCode());
    }

    public void testGetviewcolumns() {
        View view = null;
        dbStartup();
        boolean noException = false;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2041);
            assertNotNull(view.getViewColumns());
            noException = true;
        } catch (SAFRViewActivationException e1) {
            logger.log(Level.SEVERE, "", e1);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertTrue(noException);
    }

    public void testGetViewSources() {
        View view = null;
        dbStartup();
        boolean noException = false;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2041);
            assertNotNull(view.getViewSources());
            noException = true;
        } catch (SAFRViewActivationException e1) {
            logger.log(Level.SEVERE, "", e1);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertTrue(noException);
    }

    public void testGetViewColumnSources() {
        View view = null;
        dbStartup();
        boolean noException = false;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2041);
            assertNotNull(view.getViewColumnSources());
            noException = true;
        } catch (SAFRViewActivationException e1) {
            logger.log(Level.SEVERE, "", e1);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertTrue(noException);
    }

    public void testGetViewSortKeys() {
        View view = null;
        dbStartup();
        boolean noException = false;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2041);
            assertNotNull(view.getViewSortKeys());
            noException = true;
        } catch (SAFRViewActivationException e1) {
            logger.log(Level.SEVERE, "", e1);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertTrue(noException);
    }

    public void testHasExtractPhaseOutputLimit() {
        View view = null;
        dbStartup();
        boolean noException = false;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2041);
            view.setExtractPhaseOutputLimit(true);
            noException = true;
        } catch (SAFRViewActivationException e1) {
            logger.log(Level.SEVERE, "", e1);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertTrue(view.hasExtractPhaseOutputLimit());
        assertTrue(noException);
    }

    public void testSetFormatPhaseOutputLimit() {
        View view = null;
        dbStartup();
        boolean noException = false;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2041);
            view.setFormatPhaseOutputLimit(true);
            noException = true;
        } catch (SAFRViewActivationException e1) {
            logger.log(Level.SEVERE, "", e1);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertTrue(view.hasFormatPhaseOutputLimit());
        assertTrue(noException);
    }

    public void testIsAggregateBySortKey() {
        View view = null;
        dbStartup();
        boolean noException = false;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2041);
            view.setExtractAggregateBySortKey(true);
            noException = true;
        } catch (SAFRViewActivationException e1) {
            logger.log(Level.SEVERE, "", e1);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertTrue(view.isExtractAggregateBySortKey());
        assertTrue(noException);
    }

    public void testIsFormatPhaseInUse() {
        View view = null;
        dbStartup();
        boolean noException = false;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2041);
            view.setOutputFormat(OutputFormat.Format_Fixed_Width_Fields);
            noException = true;
        } catch (SAFRViewActivationException e1) {
            logger.log(Level.SEVERE, "", e1);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertTrue(view.isFormatPhaseInUse());
        assertTrue(noException);
    }

    public void testIsFormatPhaseRecordAggregationOn() {
        View view = null;
        dbStartup();
        boolean noException = false;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2041);
            view.setFormatPhaseRecordAggregationOn(true);
            noException = true;
        } catch (SAFRViewActivationException e1) {
            logger.log(Level.SEVERE, "", e1);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertTrue(view.isFormatPhaseRecordAggregationOn());
        assertTrue(noException);
    }

    public void testIsSuppressZeroRecords() {
        View view = null;
        dbStartup();
        boolean noException = false;
        try {
            view = SAFRApplication.getSAFRFactory().getView(2041);
            view.setSuppressZeroRecords(true);
            noException = true;
        } catch (SAFRViewActivationException e1) {
            logger.log(Level.SEVERE, "", e1);
            assertTrue(false);
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "", e);
            assertTrue(false);
        }
        assertTrue(view.isSuppressZeroRecords());
        assertTrue(noException);
    }

    public void testMakeViewInactive() {
        View view = null;
        dbStartup();
        view = SAFRApplication.getSAFRFactory().createView();
        view.makeViewInactive();
        if (view.getStatusCode().getGeneralId() != (Codes.INACTIVE)) {
            assertTrue(false);
        }
        View view1 = null;
        try {
            view1 = SAFRApplication.getSAFRFactory().getView(2041);
        } catch (SAFRException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        view1.makeViewInactive();
        if (view1.getStatusCode().getGeneralId() != (Codes.INACTIVE)) {
            assertTrue(false);
        }
    }

    public void testRemoveViewColumn() {
        View view = null;
        dbStartup();
        boolean noException = false;
        view = SAFRApplication.getSAFRFactory().createView();
        ViewColumn vc = null;
        try {
            view.addViewSource();
            vc = view.addViewColumn(0);
            noException = true;
        } catch (SAFRException e1) {
            e1.printStackTrace();
            assertTrue(false);
        }
        assertTrue(noException);
        view.removeViewColumn(vc);
        if (view.getViewColumns().getActiveItems().contains(vc)) {
            assertTrue(false);
        }

        View view1 = null;
        noException = false;

        try {
            view1 = SAFRApplication.getSAFRFactory().getView(2041);
            noException = true;
        } catch (SAFRException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        assertTrue(noException);
        ViewColumn vc1 = view1.getViewColumns().getActiveItems().get(1);
        view1.removeViewColumn(vc1);
        if (view1.getViewColumns().getActiveItems().contains(vc1)) {
            assertTrue(false);
        }
    }

    public void testRemoveViewSource() {
        View view = null;
        dbStartup();
        boolean noException = false;
        view = SAFRApplication.getSAFRFactory().createView();
        ViewSource vs = null;
        vs = view.addViewSource();
        noException = true;
        assertTrue(noException);
        view.removeViewSource(vs);
        if (view.getViewSources().getActiveItems().contains(vs)) {
            assertTrue(false);
        }

        View view1 = null;
        noException = false;

        try {
            view1 = SAFRApplication.getSAFRFactory().getView(2041);
            noException = true;
        } catch (SAFRException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        assertTrue(noException);
        ViewSource vs1 = view1.getViewSources().getActiveItems().get(0);
        view1.removeViewSource(vs1);
        if (view1.getViewSources().getActiveItems().contains(vs1)) {
            assertTrue(false);
        }
    }

    public void testSetExtractPhaseOutputLimit() {
        View view = null;
        dbStartup();
        view = SAFRApplication.getSAFRFactory().createView();
        view.setExtractPhaseOutputLimit(false);
        assertEquals(new Integer(0), view.getExtractMaxRecords());
    }

    public void testSaveAs() throws SAFRValidationException, SAFRException {
        dbStartup();
        View view1 = null;
        View view2 = null;
        Boolean correctException = false;
        try {
            view1 = SAFRApplication.getSAFRFactory().getView(8539);
        } catch (SAFRException se) {
            correctException = true;
        }
        assertFalse(correctException);

        view2 = (View) view1.saveAs("Copied_View");
        delIds.add(view2.getId());
        assertEquals(view1.getOutputFormatCode(), view2.getOutputFormatCode());
        assertEquals(view1.getExtractAggregateBufferSize(),
                view2.getExtractAggregateBufferSize());
        assertEquals(view1.getComment(), view2.getComment());
        assertEquals(view1.getFormatExitParams(), view2.getFormatExitParams());
        assertEquals(view1.getFormatRecordFilter(),
                view2.getFormatRecordFilter());
        assertEquals(view1.getWriteExitParams(), view2.getWriteExitParams());
        assertEquals(view1.getExtractMaxRecords(), view2.getExtractMaxRecords());
        assertEquals(view1.getExtractWorkFileNo(),
                view2.getExtractWorkFileNo());
        assertEquals(view1.getFileFieldDelimiterCode(),
                view2.getFileFieldDelimiterCode());
        assertEquals(view1.getFileStringDelimiterCode(),
                view2.getFileStringDelimiterCode());
        assertEquals(view1.getLinesPerPage(), view2.getLinesPerPage());
        assertEquals(view1.getReportWidth(), view2.getReportWidth());

        // test view sources..
        assertEquals(2, view2.getViewSources().size());
        assertTrue(1342 == view2.getViewSources().get(0).getLrFileAssociation()
                .getAssociatedComponentIdNum());
        assertTrue(1349 == view2.getViewSources().get(0).getLrFileAssociation()
                .getAssociatingComponentId());
        assertTrue(1342 == view2.getViewSources().get(1).getLrFileAssociation()
                .getAssociatedComponentIdNum());
        assertTrue(1350 == view2.getViewSources().get(1).getLrFileAssociation()
                .getAssociatingComponentId());

        // test View Columns.
        assertEquals(4, view2.getViewColumns().size());

        // test Sort Keys
        assertEquals(1, view2.getViewSortKeys().size());
        assertTrue(1 == view2.getViewSortKeys().get(0).getViewColumn()
                .getColumnNo());
    }
    
}
