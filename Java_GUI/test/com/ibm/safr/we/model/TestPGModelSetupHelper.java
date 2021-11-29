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


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.SAFRImmutableList;
import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.LRFieldKeyType;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactory;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.data.transfer.LogicalFileTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.PhysicalFile.InputDataset;
import com.ibm.safr.we.model.PhysicalFile.OutputDataset;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSource;

public class TestPGModelSetupHelper extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestLogicalRecord");

	static String TABLE_NAME = "LOGREC";
	static String COL_ID = "LOGRECID";
	static boolean postgres = true;

	static String DEP_TABLE_LIST[] = { "LRINDEX", "LRLFASSOC", "SECLOGREC", "LRFIELD", TABLE_NAME };
	TestDataLayerHelper helper = new TestDataLayerHelper();
    List<Integer> delIds = new ArrayList<Integer>();

	public void setUp() {
	}

	public void dbStartup() {
		try {
			TestDataLayerHelper.setPostgres(postgres);
            helper.initDataLayer();
		} catch (DAOException e) {
			assertFalse(true);
		}
	}

	public void removeLogicalRecord(Integer id) {

		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);

			PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
			ConnectionParameters params = fact.getConnectionParameters();
			SQLGenerator generator = new SQLGenerator();

			String schema = params.getSchema();
			try {
				String statement = "DELETE FROM "
						+ schema + ".LRINDEXFLD A WHERE A.LRINDEXID IN (SELECT B.LRINDEXID FROM "
						+ schema + ".LRINDEX B WHERE B.LOGRECID = ? AND B.ENVIRONID=A.ENVIRONID) ";
				PreparedStatement pst = fact.getConnection().prepareStatement(statement);
				pst.setInt(1, id);
				pst.execute();
				pst.close();
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}

            try {
                // Deleting field attributes
                String statement = "DELETE FROM "
                        + schema + ".LRFIELDATTR A WHERE LRFIELDID IN (SELECT LRFIELDID FROM "
                        + schema + ".LRFIELD B WHERE LOGRECID = ?)";

                PreparedStatement pst = fact.getConnection().prepareStatement(statement);
                pst.setInt(1, id);
                pst.execute();
                pst.close();
            } catch (Exception e1) {
                e1.printStackTrace();
                assertTrue(false);
            }
			
			for (String tableName : DEP_TABLE_LIST) {
				try {
					String statement = generator.getDeleteStatement(params
							.getSchema(), tableName, idNames);
					PreparedStatement pst = fact.getConnection()
							.prepareStatement(statement);
					pst.setInt(1, id);
					pst.execute();
					pst.close();
				} catch (Exception e) {
					e.printStackTrace();
					assertTrue(false);
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
                    //removeLogicalRecord(i);
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

	public LogicalRecord makeLR(String LRName, List<String> fields, LogicalFile lf) throws SAFRException {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(0); //0 to get rid of error - fix up later
		if(lr == null) {
			lr = SAFRApplication.getSAFRFactory().createLogicalRecord();
			Code lrTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.LRTYPE).getCode(Codes.LOGICAL_FILE);
			Code lrStatusCode = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.LRSTATUS).getCode(Codes.INACTIVE);
	
			lr.setName(LRName);
			lr.setComment("test");
			lr.setLRTypeCode(lrTypeCode);
			lr.setLRStatusCode(lrStatusCode);
			
			ComponentAssociation componentAssociation = new ComponentAssociation(lr, lf.getId(), lf.getName(),null);
			lr.addAssociatedLogicalFile(componentAssociation);
	
	        //If we're here the LR has no fields.
	        for(String f : fields) {
	        	LRField lrf = lr.addField();
	        	lrf.setName(f);
	        }
	        
	        boolean noException = true;;
			try {
	            lr.store();
	        } catch (DAOException e1) {
	        	noException  = false;
	        } catch (SAFRException e) {
	        	noException = false;
	        }
	        assertTrue(noException);
	        
		}
		return lr;
	}
	
	public LogicalFile makeLFPF(String LFName, String PFName) throws SAFRException {
		dbStartup();
		PhysicalFile pf =  makePF(PFName);
		
		LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(0);
		if(lf == null) {
			lf = SAFRApplication.getSAFRFactory().createLogicalfile();
			/* Setup File Association and create logical file object */
			FileAssociation fileAssociation = null;
	
			fileAssociation = new FileAssociation(lf, pf.getId(), pf.getName(), null);
	
			lf.setName(LFName);
			lf.setComment("test");
			lf.addAssociatedPhysicalFile(fileAssociation);
	
	        boolean noException = true;;
			try {
	            lf.store();
	        } catch (DAOException e1) {
	        	noException  = false;
	        } catch (SAFRException e) {
	        	noException = false;
	        }
	        assertTrue(noException);
		}
		return lf;
	}
	
	public PhysicalFile makePF(String PFName) throws SAFRException {
		dbStartup();
        PhysicalFile pf = SAFRApplication.getSAFRFactory().getPhysicalFile(PFName);
		if(pf == null) {
			pf = SAFRApplication.getSAFRFactory().createPhysicalFile();
	        pf.setName(PFName);
			InputDataset inputDatasetDataSource = pf.new InputDataset();
	        OutputDataset outputDatasetDataSource = pf.new OutputDataset();
	
	        CodeSet codeSet;
	        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FILETYPE");
	        Code fileTypeCode = codeSet.getCode("DISK");
	
	        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
	        Code accessMethodCode = codeSet.getCode("VSAM");
	
	        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("RECFM");
	        Code recfmCode = codeSet.getCode("FB");
	
	        pf.setFileTypeCode(fileTypeCode);
	        pf.setAccessMethod(accessMethodCode);
	        pf.setComment("Test setup");
	        inputDatasetDataSource.setDatasetName("dataset1");
	        inputDatasetDataSource.setInputDDName("inputDD1");
	        inputDatasetDataSource.setMinRecordLen(10);
	        inputDatasetDataSource.setMaxRecordLen(20);
	        outputDatasetDataSource.setOutputDDName("output1");
	        outputDatasetDataSource.setRecfm(recfmCode);
	        outputDatasetDataSource.setLrecl(50);
	        
	        boolean noException = true;
	        try {
	            pf.store();
	        } catch (DAOException e1) {
	        		noException = false;
	        } catch (SAFRException e) {
	        	noException = false;
	        }
	        assertTrue(noException);
		}
		return pf;
	}

	public View makeView(String name, SAFRList<ComponentAssociation> lfas) {
        View view = null;
        boolean noException = false;;
		try {

            view = SAFRApplication.getSAFRFactory().createView();

            CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet("VIEWSTATUS");
            Code statusCode = codeSet.getCode("ACTVE");

            ControlRecord cr = SAFRApplication.getSAFRFactory().getControlRecord(1);
            Object uer = null;

            view.setName(name);
            view.setExtractWorkFileNo(5);
            view.setExtractFileAssociation(null);
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
            view.setWriteExit(null);
            view.setFormatExit(null);
            view.setOutputFormat(OutputFormat.Extract_Fixed_Width_Fields);
            view.setStatusCode(statusCode);

            ViewSource vs = view.addViewSource();
            vs.setLrFileAssociation(lfas.get(0));
            ViewColumn vc = view.addViewColumn(0);
            vc.setHeading1("LogicTextTest");
            vc.setStartPosition(1);
            SAFRImmutableList<ViewColumnSource> vcss = vc.getViewColumnSources();
            CodeSet srccodeSet = SAFRApplication.getSAFRFactory().getCodeSet("COLSRCTYPE");
            Code colCode = srccodeSet.getCode("FRMLA");
            vcss.get(0).setSourceType(colCode);
            vcss.get(0).setExtractColumnAssignment("COLUMN = {Binary4}");
            
            view.store();
            delIds.add(view.getId());
            noException  = true;
        } catch (SAFRException e) {
            e.printStackTrace();
        }
        assertTrue(noException);
        return view;
	}

	public void makeTestView() {
     	LogicalFile lf = makeLFPF("SourceLF", "SoourcePF");
     	List<String> fieldNames = new ArrayList<String>();
     	fieldNames.add("SimpleLapha");
     	fieldNames.add("Binary4");
     	fieldNames.add("Zoned");
     	LogicalRecord lr = makeLR("SourceLR", fieldNames, lf);
     	SAFRList<ComponentAssociation> lfas = lr.getLogicalFileAssociations();
     	View vw = makeView("LogicTextView", lfas);
	}

}
