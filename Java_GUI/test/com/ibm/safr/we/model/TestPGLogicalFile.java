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


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.SAFRImmutableList;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactory;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.data.transfer.LogicalFileTransfer;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.PhysicalFile.InputDataset;
import com.ibm.safr.we.model.PhysicalFile.OutputDataset;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;

public class TestPGLogicalFile extends TestCase {
	static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.TestLogicalFile");

	static String TABLE_NAME = "LOGFILE";
	static String COL_ID = "LOGFILEID";
	static boolean postgres = true;

	static String DEP_TABLE_LIST[] = { TABLE_NAME };
	TestDataLayerHelper helper = new TestDataLayerHelper();
    List<Integer> delIds = new ArrayList<Integer>();

	public void setUp() throws DAOException {
		TestDataLayerHelper.setPostgres(postgres);
        helper.initDataLayer();
	}

	public void removeLogicalFile(Integer id) throws SAFRException, SQLException {

		PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
		ConnectionParameters params = fact.getConnectionParameters();
		SQLGenerator generator = new SQLGenerator();

		List<String> idNamesAssoc = new ArrayList<String>();
		idNamesAssoc.add("LOGFILEID");
		String statement = generator.getDeleteStatement(params.getSchema(), "LFPFASSOC", idNamesAssoc);
		PreparedStatement pst = fact.getConnection().prepareStatement(statement);
		pst.setInt(1, id);
		pst.execute();
		pst.close();

		List<String> idNames = new ArrayList<String>();
		idNames.add(COL_ID);

		for (String tableName : DEP_TABLE_LIST) {
			statement = generator.getDeleteStatement(params.getSchema(), tableName, idNames);
			pst = fact.getConnection().prepareStatement(statement);
			pst.setInt(1, id);
			pst.execute();
			pst.close();
		}
	}

	public void tearDown() {
        try {
            for (Integer i : delIds) {
                try {
                    removeLogicalFile(i);
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

	public void testSaveAs() throws SAFRValidationException, SAFRException {
		LogicalFile lf1 = null;
		LogicalFile lf2 = null;
		
		lf1 = SAFRApplication.getSAFRFactory().getLogicalFile(1341);

		lf2 = (LogicalFile) lf1.saveAs("TestSaveAsCopy_Junit");
		delIds.add(lf2.getId());
		assertTrue(lf2.getId() > 0);
		assertEquals("TestSaveAsCopy_Junit", lf2.getName());
		assertEquals(lf1.getComment(), lf2.getComment());
		assertTrue((lf2.getPhysicalFileAssociations().get(0).getAssociatedComponentIdNum()).equals(8398));

	}

	public void testCreate() throws SAFRException {
		//Make a PF 
        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();
        InputDataset inputDatasetDataSource = pf.new InputDataset();
        OutputDataset outputDatasetDataSource = pf.new OutputDataset();
        //SQLDatabase sqlpf = getSQLPhysicalFile(1);

        CodeSet codeSet;
        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FILETYPE");
        Code fileTypeCode = codeSet.getCode("DISK");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
        Code accessMethodCode = codeSet.getCode("VSAM");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("RECFM");
        Code recfmCode = codeSet.getCode("FB");

        pf.setName("test4");
        pf.setFileTypeCode(fileTypeCode);
        pf.setAccessMethod(accessMethodCode);
        pf.setComment("comment1");
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

		DAOFactory fact = null;
		if(postgres) {
			fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();		
		}
		PhysicalFileTransfer dbpf = fact.getPhysicalFileDAO().getPhysicalFile(pf.getId(), pf.getEnvironmentId());	
		delIds.add(pf.getId());
        assertTrue(noException);

		/* Setup File Association and create logical file object */
		FileAssociation fileAssociation = null;
		LogicalFile lf = SAFRApplication.getSAFRFactory().createLogicalfile();

		fileAssociation = new FileAssociation(lf, pf.getId(), pf.getName(), null);

		/* Test storing of logical File with name , comment and pf file being associated, Fail if exception is thrown */
		
		lf.setName("TestLF");
		lf.setComment("test");
		lf.addAssociatedPhysicalFile(fileAssociation);

		lf.store();
		LogicalFileTransfer dblf = fact.getLogicalFileDAO().getLogicalFile(lf.getId(), lf.getEnvironmentId());	

		/* Get logical file fields and check on stored values */
		assertEquals(lf.getName(), dblf.getName());
		assertEquals(lf.getComment(), dblf.getComments());
		assertEquals(lf.getCreateBy(), fact.getSAFRLogin().getUserId());
		//assertEquals(lf.getPhysicalFileAssociations().get(0).getAssociationId(), dblf.);
		assertNotNull(lf.getCreateTime());
		assertNotNull(lf.getModifyTime());
	}

}
