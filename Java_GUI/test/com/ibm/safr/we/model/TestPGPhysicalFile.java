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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.SAFRImmutableList;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactory;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.PhysicalFile.InputDataset;
import com.ibm.safr.we.model.PhysicalFile.OutputDataset;
import com.ibm.safr.we.model.PhysicalFile.Property;
import com.ibm.safr.we.model.PhysicalFile.SQLDatabase;
import com.ibm.safr.we.model.associations.FileAssociation;

public class TestPGPhysicalFile extends TestCase {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.TestPhysicalFile");

    static String TABLE_NAME = "PHYFILE";
    static String COL_ID = "PHYFILEID";
	static boolean postgres = true;

    static String DEP_TABLE_LIST[] = { TABLE_NAME };

    TestDataLayerHelper helper = new TestDataLayerHelper();
    List<Integer> delIds = new ArrayList<Integer>();

    public void setUp() {
        try {
			TestDataLayerHelper.setPostgres(postgres);
            helper.initDataLayer();
        } catch (DAOException e) {
            assertFalse(true);
        }
    }

    public InputDataset getInputDatasetPhysicalFile(Integer environmentId) {
    //  dbStartup();
        PhysicalFile physicalFile = SAFRApplication.getSAFRFactory().createPhysicalFile();
        PhysicalFile.InputDataset inDs = physicalFile.new InputDataset();
        return inDs;
    }

    public OutputDataset getOutputDatasetPhysicalFile(Integer environmentId) {
    //  dbStartup();
        PhysicalFile physicalFile = SAFRApplication.getSAFRFactory().createPhysicalFile();
        PhysicalFile.OutputDataset outDs = physicalFile.new OutputDataset();
        return outDs;
    }

    public void removePhysicalFile(Integer id) {

        try {
            List<String> idnames = new ArrayList<String>();
            idnames.add(COL_ID);

			DAOFactory fact;
			if(postgres) {
				fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();				
			}
            ConnectionParameters params = fact.getConnectionParameters();
            SQLGenerator generator = new SQLGenerator();

            for (String tableName : DEP_TABLE_LIST) {
                try {
                    String statement = generator.getDeleteStatement(params.getSchema(), tableName, idnames);
                    PreparedStatement pst = fact.getConnection().prepareStatement(statement);
                    pst.setInt(1, id);
                    pst.execute();
                    pst.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE, "", e);
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "", e);
        }
    }

    public SQLDatabase getSQLPhysicalFile(Integer environmentId) {
    //  dbStartup();
        PhysicalFile physicalFile = SAFRApplication.getSAFRFactory().createPhysicalFile();
        PhysicalFile.SQLDatabase sqlPf = physicalFile.new SQLDatabase();
        return sqlPf;
    }

    public void tearDown() {
        try {
            for (Integer i : delIds) {
                try {
                    removePhysicalFile(i);
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
    //  dbStartup();
        PhysicalFile pf1 = null;
        PhysicalFile pf2 = null;
        //Boolean correctException = false;
        //try {
        pf1 = SAFRApplication.getSAFRFactory().getPhysicalFile(8400);
    //  } catch (SAFRException se) {
        //  correctException = true;
    //  }
    //  assertFalse(correctException);

        pf2 = (PhysicalFile) pf1.saveAs("TestSaveAsCopy_Junit");
        delIds.add(pf2.getId());
        
        // test General Properties.
        assertTrue(pf2.getId() > 0);
        assertEquals("TestSaveAsCopy_Junit", pf2.getName());
        assertEquals(pf1.getComment(), pf2.getComment());
        assertEquals(pf1.getFileTypeCode(), pf2.getFileTypeCode());
        assertEquals(pf1.getAccessMethodCode(), pf2.getAccessMethodCode());
        assertEquals(pf1.getUserExitRoutine().getId(), pf2.getUserExitRoutine().getId());
        assertEquals(pf1.getUserExitRoutineParams(), pf2.getUserExitRoutineParams());

        // Test SQL dataset...
        PhysicalFile.SQLDatabase sqlDatabasePF1 = pf1.new SQLDatabase();
        PhysicalFile.SQLDatabase sqlDatabasePF2 = pf2.new SQLDatabase();

        assertEquals(sqlDatabasePF1.getSubSystem(), sqlDatabasePF2.getSubSystem());
        assertEquals(sqlDatabasePF1.isIncludeNullIndicators(), sqlDatabasePF2.isIncludeNullIndicators());
        assertEquals(sqlDatabasePF1.getInputDDName(), sqlDatabasePF2.getInputDDName());
        assertEquals(sqlDatabasePF1.getRowFormatCode(), sqlDatabasePF2.getRowFormatCode());
        assertEquals(sqlDatabasePF1.getSqlStatement(), sqlDatabasePF2.getSqlStatement());
        assertEquals(sqlDatabasePF1.getTableName(), sqlDatabasePF2.getTableName());

        // Test Input dataset
        PhysicalFile.InputDataset inputDatasetPF1 = pf1.new InputDataset();
        PhysicalFile.InputDataset inputDatasetPF2 = pf2.new InputDataset();

        assertEquals(inputDatasetPF1.getInputDDName(), inputDatasetPF2.getInputDDName());
        assertEquals(inputDatasetPF1.getDatasetName(), inputDatasetPF2.getDatasetName());
        assertEquals(inputDatasetPF1.getMinRecordLen(), inputDatasetPF2.getMinRecordLen());
        assertEquals(inputDatasetPF1.getMaxRecordLen(), inputDatasetPF2.getMaxRecordLen());

        // Test output dataset
        PhysicalFile.OutputDataset outputDataSetPF1 = pf1.new OutputDataset();
        PhysicalFile.OutputDataset outputDataSetPF2 = pf2.new OutputDataset();

        assertEquals(outputDataSetPF1.getLrecl(), outputDataSetPF2.getLrecl());
        assertEquals(outputDataSetPF1.getOutputDDName(), outputDataSetPF2.getOutputDDName());
        assertEquals(outputDataSetPF1.getRecfm(), outputDataSetPF2.getRecfm());
    }

    public void testStoreNoExit() throws SAFRException {

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
//            UserExitRoutine uer = SAFRApplication.getSAFRFactory().createUserExitRoutine();
//            CodeSet codeSet1 = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.EXITTYPE);
//            Code typeCode = codeSet1.getCode(Integer.valueOf(Codes.READ));
//            uer.setTypeCode(typeCode);
//            pf.setUserExitRoutine(uer);
            pf.store();
        } catch (DAOException e1) {
        	noException = false;
        } catch (SAFRException e) {
        	noException = false;
        }

		DAOFactory fact;
		if(postgres) {
			fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();		
		}
		PhysicalFileTransfer dbpf = fact.getPhysicalFileDAO().getPhysicalFile(pf.getId(), pf.getEnvironmentId());	
		delIds.add(pf.getId());
        assertTrue(noException);
        assertEquals(pf.getName(), dbpf.getName());
        assertEquals(pf.getComment(), dbpf.getComments());
        assertEquals(inputDatasetDataSource.getInputDDName(), dbpf.getInputDDName());
        assertEquals(inputDatasetDataSource.getMinRecordLen(), dbpf.getMinRecordLen());
        assertEquals(inputDatasetDataSource.getMaxRecordLen(), dbpf.getMaxRecordLen());
        assertEquals(outputDatasetDataSource.getOutputDDName(), dbpf.getOutputDDName());
        assertEquals(outputDatasetDataSource.getLrecl(), dbpf.getLrecl());

        assertEquals(pf.getFileTypeCode().getKey(), dbpf.getFileTypeCode());
        assertEquals(pf.getAccessMethodCode().getKey(), dbpf.getAccessMethodCode());
        assertEquals(outputDatasetDataSource.getRecfm().getKey(), dbpf.getRecfm());

        assertEquals(pf.getCreateBy(), fact.getSAFRLogin().getUserId());
        assertEquals(pf.getModifyBy(), fact.getSAFRLogin().getUserId());
        assertNotNull(pf.getCreateTime());
        assertNotNull(pf.getModifyTime());
        
        assertNull(dbpf.getReadExitId());
    }

    public void testUpdateNoExit() throws SAFRException {

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
//            UserExitRoutine uer = SAFRApplication.getSAFRFactory().createUserExitRoutine();
//            CodeSet codeSet1 = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.EXITTYPE);
//            Code typeCode = codeSet1.getCode(Integer.valueOf(Codes.READ));
//            uer.setTypeCode(typeCode);
//            pf.setUserExitRoutine(uer);
            pf.store();
        } catch (DAOException e1) {
        	noException = false;
        } catch (SAFRException e) {
        	noException = false;
        }
		DAOFactory fact;
		if(postgres) {
			fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();		
		}
		PhysicalFileTransfer dbpf = fact.getPhysicalFileDAO().getPhysicalFile(pf.getId(), pf.getEnvironmentId());	

		delIds.add(pf.getId());
        assertTrue(noException);
        assertEquals(pf.getName(), dbpf.getName());
        assertNull(dbpf.getReadExitId());
        
      // test for storing modified physical file
      pf.setName("test5");
      try {
          pf.store();
      	} catch (DAOException e1) {
      		noException = false;
      	} catch (Throwable e2) {
      		noException = false;
      }
		PhysicalFileTransfer dbpf2 = fact.getPhysicalFileDAO().getPhysicalFile(pf.getId(), pf.getEnvironmentId());	
        assertTrue(noException);
        assertEquals(pf.getName(), dbpf2.getName());
        assertNull(dbpf2.getReadExitId());
    }
}
