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
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.PhysicalFile.InputDataset;
import com.ibm.safr.we.model.PhysicalFile.OutputDataset;
import com.ibm.safr.we.model.PhysicalFile.Property;
import com.ibm.safr.we.model.PhysicalFile.SQLDatabase;
import com.ibm.safr.we.model.associations.FileAssociation;

public class TestPhysicalFile extends TestCase {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.TestPhysicalFile");

    static String TABLE_NAME = "PHYFILE";
    static String COL_ID = "PHYFILEID";

    static String DEP_TABLE_LIST[] = { TABLE_NAME };

    TestDataLayerHelper helper = new TestDataLayerHelper();
    List<Integer> delIds = new ArrayList<Integer>();

    public void setUp() {
        try {
            helper.initDataLayer(112);
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

            PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
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

    public void testGetSetFileTypeCode() throws SAFRException {
        //dbStartup();
        PhysicalFile physicalFile = SAFRApplication.getSAFRFactory().createPhysicalFile();
        Code fileTypeCode = null;

        physicalFile.getFileTypeCode();

        assertNull(fileTypeCode);

        Code code1 = new Code("DISK", "Disk File", 2);
        Code code2 = new Code("TAPE ", "Tape File", 3);

        physicalFile.setFileTypeCode(code1);

        assertEquals(physicalFile.getFileTypeCode(), code1);

        physicalFile.setFileTypeCode(code2);
        //try {
        assertEquals(physicalFile.getFileTypeCode(), code2);
    //  } catch (SAFRException e) {
    //      throw new RuntimeException();
    //  }

        // test for null File Type Code

        try {
            physicalFile.setFileTypeCode(null);
            fail();
        } catch (NullPointerException npe) {    
        }
    }

    public void testGetSetAccessMethodCode() {
    //  dbStartup();
        PhysicalFile physicalFile = SAFRApplication.getSAFRFactory().createPhysicalFile();

        Code code1 = new Code("VSAM", "VSAM - Unordered", 2);
        Code code2 = new Code("KSDS", "VSAM - Ordered", 3);

        physicalFile.setAccessMethod(code1);
        assertEquals(physicalFile.getAccessMethodCode(), code1);

        physicalFile.setAccessMethod(code2);
        assertEquals(physicalFile.getAccessMethodCode(), code2);

        // test for null Access Method Code
    //  boolean correctException = false;
        try {
            physicalFile.setAccessMethod(null);
            fail();
        } catch (NullPointerException npe) {
        //  correctException = true;
        }
        //assertTrue(correctException);
    }

    public void testgetLogicalFileAssociations() throws SAFRException {
    //  dbStartup();
        PhysicalFile physicalfile = SAFRApplication.getSAFRFactory().getPhysicalFile(8410);
        SAFRImmutableList<FileAssociation> pf_lf_Association_List = physicalfile.getLogicalFileAssociations();
        
        for (FileAssociation pf_fileAss : pf_lf_Association_List.getActiveItems()) {
            // System.out.println(PF_fileAss.getAssociatedComponentName());
            assertEquals(pf_fileAss.getAssociatedComponentName(), "Test_PF_LF_Association");
        }
    }

    public void testGetSetUserExitRoutine() throws SAFRException {
        UserExitRoutine uer = SAFRApplication.getSAFRFactory().createUserExitRoutine();
        CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.EXITTYPE);

        Code typeCode = codeSet.getCode(Integer.valueOf(Codes.READ));
        uer.setTypeCode(typeCode);

        PhysicalFile physicalFile = SAFRApplication.getSAFRFactory().createPhysicalFile();
        physicalFile.setUserExitRoutine(uer);
        assertEquals(physicalFile.getUserExitRoutine(), uer);

        physicalFile.setUserExitRoutine(uer);
        assertEquals(physicalFile.getUserExitRoutine(), uer);
        
        UserExitRoutine uer2 = SAFRApplication.getSAFRFactory().getUserExitRoutine(233);
        
        physicalFile.setUserExitRoutine(uer2);
        assertEquals(physicalFile.getUserExitRoutine(), uer2);      
    }

    public void testGetSetUserExitRoutineParams() {
    //  dbStartup();

        PhysicalFile physicalFile = SAFRApplication.getSAFRFactory().createPhysicalFile();
        assertEquals(physicalFile.getUserExitRoutineParams(), null);

        physicalFile.setUserExitRoutineParams("test1");
        assertEquals(physicalFile.getUserExitRoutineParams(), "test1");

        physicalFile.setUserExitRoutineParams("test2");
        assertEquals(physicalFile.getUserExitRoutineParams(), "test2");

        physicalFile.setUserExitRoutineParams("");
        assertEquals(physicalFile.getUserExitRoutineParams(), "");

        physicalFile.setUserExitRoutineParams(null);
        assertEquals(physicalFile.getUserExitRoutineParams(), null);
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

    public void testStore() throws SAFRException {

        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();
        InputDataset inputDatasetDataSource = getInputDatasetPhysicalFile(1);
        OutputDataset outputDatasetDataSource = getOutputDatasetPhysicalFile(1);
        SQLDatabase sqlpf = getSQLPhysicalFile(1);

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

        try {
            UserExitRoutine uer = SAFRApplication.getSAFRFactory().createUserExitRoutine();
            CodeSet codeSet1 = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.EXITTYPE);
            Code typeCode = codeSet1.getCode(Integer.valueOf(Codes.READ));
            uer.setTypeCode(typeCode);
            pf.setUserExitRoutine(uer);
            pf.store();
        } catch (DAOException e1) {
            fail();
        } catch (SAFRException e) {
            fail();
        }

        PGDAOFactory fact = null;
        fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
        assertEquals(pf.getName(), "test4");
        assertEquals(pf.getComment(), "comment1");
        assertEquals(inputDatasetDataSource.getDatasetName(), "dataset1");
        assertEquals(inputDatasetDataSource.getInputDDName(), "inputDD1");
        assertEquals(inputDatasetDataSource.getMinRecordLen(), 10);
        assertEquals(inputDatasetDataSource.getMaxRecordLen(), 20);
        assertEquals(outputDatasetDataSource.getOutputDDName(), "output1");
        assertEquals(outputDatasetDataSource.getLrecl(), 50);

        assertEquals(pf.getFileTypeCode(), fileTypeCode);
        assertEquals(pf.getAccessMethodCode(), accessMethodCode);
        assertEquals(outputDatasetDataSource.getRecfm(), recfmCode);
        assertEquals(pf.getCreateBy(), fact.getSAFRLogin().getUserId());
        assertEquals(pf.getModifyBy(), fact.getSAFRLogin().getUserId());
        assertNotNull(pf.getCreateTime());
        assertNotNull(pf.getModifyTime());

        // test for storing modified physical file

        // test correctly stored DatasetDataSource Physical file.
        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FILETYPE");
        fileTypeCode = codeSet.getCode("DISK");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
        accessMethodCode = codeSet.getCode("VSAM");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("RECFM");
        recfmCode = codeSet.getCode("FB");

        CodeSet rowFormatCodeSet = SAFRApplication.getSAFRFactory().getCodeSet("DBMSROWFMT");
        Code rowFormatCode = rowFormatCodeSet.getCode("SQL");

        pf.setName("test5");
        pf.setFileTypeCode(fileTypeCode);
        pf.setAccessMethod(accessMethodCode);
        pf.setComment("comment2");
        inputDatasetDataSource.setDatasetName("dataset2");
        inputDatasetDataSource.setInputDDName("inputDD2");
        inputDatasetDataSource.setMinRecordLen(10);
        inputDatasetDataSource.setMaxRecordLen(20);
        outputDatasetDataSource.setOutputDDName("output2");
        outputDatasetDataSource.setRecfm(recfmCode);
        outputDatasetDataSource.setLrecl(50);
        sqlpf.setTableName("Test Table");
        sqlpf.setRowFormatCode(rowFormatCode);
        sqlpf.setIncludeNullIndicators(false);
        sqlpf.setSqlStatement("Test SQL");
        sqlpf.setSubSystem("Test");

        try {
            pf.store();
        //  noException = true;
        } catch (DAOException e1) {
        //  correctException = true;
            fail();
        } catch (Throwable e2) {
        //  correctException = true;
            fail();
        }

        fact = null;
//      try {
        fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
//      } catch (DAOException e) {
    //      e.printStackTrace();
    //  }

    //  assertTrue(noException);
    //  assertFalse(correctException);
        assertEquals(pf.getName(), "test5");
        assertEquals(pf.getComment(), "comment2");
        assertEquals(inputDatasetDataSource.getDatasetName(), "dataset2");
        assertEquals(inputDatasetDataSource.getInputDDName(), "inputDD2");
        assertEquals(inputDatasetDataSource.getMinRecordLen(), 10);
        assertEquals(inputDatasetDataSource.getMaxRecordLen(), 20);
        assertEquals(outputDatasetDataSource.getOutputDDName(), "output2");
        assertEquals(outputDatasetDataSource.getLrecl(), 50);
        assertEquals(sqlpf.getTableName(), "Test Table");
        assertEquals(sqlpf.getSubSystem(), "Test");
        assertEquals(sqlpf.getSqlStatement(), "Test SQL");
        assertEquals(sqlpf.isIncludeNullIndicators(), false);

        assertEquals(pf.getFileTypeCode(), fileTypeCode);
        assertEquals(pf.getAccessMethodCode(), accessMethodCode);
        assertEquals(outputDatasetDataSource.getRecfm(), recfmCode);
        assertEquals(sqlpf.getRowFormatCode(), rowFormatCode);

        assertEquals(pf.getCreateBy(), fact.getSAFRLogin().getUserId());
        assertEquals(pf.getModifyBy(), fact.getSAFRLogin().getUserId());
        assertNotNull(pf.getCreateTime());
        assertNotNull(pf.getModifyTime());

        // delete the pf
        SAFRApplication.getSAFRFactory().removePhysicalFile(pf.getId());

    }

    public void testStoreNoPermissionCreate() throws SAFRException {
        
        helper.setUser("NOPERM");
        
        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();
        InputDataset inputDatasetDataSource = pf.new InputDataset();

        CodeSet codeSet;
        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FILETYPE");
        Code fileTypeCode = codeSet.getCode("DISK");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
        Code accessMethodCode = codeSet.getCode("VSAM");

        pf.setName("NoPermCreate");
        pf.setFileTypeCode(fileTypeCode);
        pf.setAccessMethod(accessMethodCode);
        pf.setComment("comment1");
        inputDatasetDataSource.setDatasetName("dataset1");
        inputDatasetDataSource.setInputDDName("inputDD1");
        inputDatasetDataSource.setMinRecordLen(10);
        inputDatasetDataSource.setMaxRecordLen(20);

        pf.validate();
        try {
            pf.store();
            fail();
        } catch (SAFRException e) {
            String errMsg = e.getMessage();
            assertEquals(errMsg,"The user is not authorized to create a new physical file.");
        }           
    }
    
    
    public void testStoreNoPermissionUpdate() throws SAFRException {
        helper.setUser("NOPERM");
        PhysicalFile pf = SAFRApplication.getSAFRFactory().getPhysicalFile(8410);
        pf.setName("No_Update_Perm");
        pf.validate();
        try {
            pf.store();
            fail();
        } catch (SAFRException e) {
            String errMsg = e.getMessage();
            assertEquals(errMsg,"The user is not authorized to update this physical file.");
        }
    }

    public void testValidate() {
    //  dbStartup();
        //boolean correctException = false;
        boolean noException = false;
        
        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();
        InputDataset inds = getInputDatasetPhysicalFile(1);
        OutputDataset outds = getOutputDatasetPhysicalFile(1);
        
        CodeSet codeSet;
        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FILETYPE");
        Code fileTypeCode = codeSet.getCode("DISK");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
        Code accessMethodCode = codeSet.getCode("VSAM");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("RECFM");
        Code recfmCode = codeSet.getCode("FB");

        pf.setName("Test_junit_12345");
        pf.setAccessMethod(accessMethodCode);
        inds.setDatasetName("Test1");
        pf.setFileTypeCode(fileTypeCode);
        inds.setInputDDName("Test");
        outds.setLrecl(2);
        inds.setMaxRecordLen(2);
        outds.setOutputDDName("test");
        outds.setLrecl(2);
        outds.setRecfm(recfmCode);
    
        try {
            pf.validate();
            noException = true;
        } catch (SAFRException e1) {
            fail();
        }

        assertTrue(noException);
        //assertFalse(correctException);
    }

    public void testValidate_AccessMethod_Empty() {

        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();

        CodeSet codeSet;
        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FILETYPE");
        Code fileTypeCode = codeSet.getCode("DISK");

        /* Test error message 1 */
        pf.setName("testerrormsg1");
        pf.setFileTypeCode(fileTypeCode);

        try {
            pf.validate();
        } catch (SAFRException e1) {
            fail();
        }

    }

    public void testValidate_FileType_Empty() {

        //dbStartup();
    //  boolean correctException = false;
        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();
        CodeSet codeSet;

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
        Code accessMethodCode = codeSet.getCode("VSAM");

        /* Test error message 2 */
        pf.setName("testerrormsg2");
        pf.setAccessMethod(accessMethodCode);

        try {
            pf.validate();
        } catch (SAFRException e1) {
            fail();
        }

    }

    public void testValidate_PhysicalFile_Empty() {

        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();

        CodeSet codeSet;
        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FILETYPE");
        Code fileTypeCode = codeSet.getCode("DISK");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
        Code accessMethodCode = codeSet.getCode("VSAM");

        pf.setName("");
        pf.setFileTypeCode(fileTypeCode);
        pf.setAccessMethod(accessMethodCode);

        try {
            pf.validate();
            fail();
        } catch (SAFRException e1) {
            String emsg = e1.toString();
            emsg = emsg.trim();
            assertEquals(emsg, "Physical File name cannot be empty");
        }
    }

    public void testValidate_PhysicalFile_Exist() {

        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();

        CodeSet codeSet;
        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FILETYPE");
        Code fileTypeCode = codeSet.getCode("DISK");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
        Code accessMethodCode = codeSet.getCode("VSAM");

        pf.setName("Test_PhysicalFile");
        pf.setFileTypeCode(fileTypeCode);
        pf.setAccessMethod(accessMethodCode);

        try {
            pf.validate();
            fail();
        } catch (SAFRException e1) {
            String emsg = e1.toString();
            emsg = emsg.trim();
            assertEquals(emsg,
                    "The Physical File name 'Test_PhysicalFile' already exists. Please specify a different name.");
        }
    }

    public void testValidate_PhysicalFile_ValidNames() {

    //  dbStartup();
    //  boolean correctException = false;
        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();

        CodeSet codeSet;
        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FILETYPE");
        Code fileTypeCode = codeSet.getCode("DISK");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
        Code accessMethodCode = codeSet.getCode("VSAM");

        /* Test error message 5 */

        pf.setName("#1234");
        pf.setFileTypeCode(fileTypeCode);
        pf.setAccessMethod(accessMethodCode);
    //  correctException = false;

        try {
            pf.validate();
            fail();
        } catch (SAFRException e1) {
            String emsg = e1.toString();
            emsg = emsg.trim();
            assertEquals(
                    emsg,
                    "The Physical File name '#1234' should begin  with a letter and should comprise of letters, numbers, pound sign (#) and underscores only.");
        //  correctException = true;
        }
        //assertTrue(correctException);
    }

    public void testValidate_PF_Naming_Max() throws SAFRException {
        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();
        pf.setName("Test01234567890123456789012345678901234567890123456789");

        try {
            pf.validate();
            fail();         
        } 
        catch (SAFRValidationException e) {
            String errMsg = e.getErrorMessages().get(0);
            String pattern = "(?s)The length of Physical File name 'Test01234567890123456789012345678901234567890123456789' cannot exceed 48 characters.$";
            assertTrue(errMsg.matches(pattern));
        }
    }
    
    public void testValidate_PF_Comment_Length() throws SAFRException {
        // dbStartup();
        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();
        pf.setName("Comment_Length");
        pf.setComment(
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" +
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" +
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");

        try {
            pf.validate();
            fail();         
        } 
        catch (SAFRValidationException e) {
            String emsg = e.getMessageString(Property.COMMENT);
            assertEquals(emsg, "Comment cannot be more than 254 characters.");
        }
    }
    
    public void testValidate_UER_TypeError() {

        PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();

        CodeSet codeSet;
        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("FILETYPE");
        Code fileTypeCode = codeSet.getCode("DISK");

        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
        Code accessMethodCode = codeSet.getCode("VSAM");

        /* Test error message 6 */
        UserExitRoutine uer = SAFRApplication.getSAFRFactory().createUserExitRoutine();
        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("EXITTYPE");
        Code uerTypeCode = codeSet.getCode("WRITE");
        uer.setTypeCode(uerTypeCode);
        uer.setName("UER_Testmsg6");

        pf.setName("testerrormsg6");
        pf.setFileTypeCode(fileTypeCode);
        pf.setAccessMethod(accessMethodCode);
        pf.setUserExitRoutine(uer);

        try {
            pf.validate();
        } catch (SAFRException e1) {
            fail();
        }
    }


    public void testGetSetDatasetName() {
    //  dbStartup();
        InputDataset inds = getInputDatasetPhysicalFile(1);

        assertEquals(inds.getDatasetName(), null);

        inds.setDatasetName("testDatasetName1");
        assertEquals(inds.getDatasetName(), "testDatasetName1");

        inds.setDatasetName("testDatasetName2");
        assertEquals(inds.getDatasetName(), "testDatasetName2");

        inds.setDatasetName("");
        assertEquals(inds.getDatasetName(), "");

        inds.setDatasetName(null);
        assertEquals(inds.getDatasetName(), null);
    }

    public void testGetSetInputDDName() {
    //  dbStartup();
        InputDataset inds = getInputDatasetPhysicalFile(1);
        assertEquals(inds.getInputDDName(), null);

        inds.setInputDDName("testInputDDName1");
        assertEquals(inds.getInputDDName(), "testInputDDName1");

        inds.setInputDDName("testInputDDName2");
        assertEquals(inds.getInputDDName(), "testInputDDName2");

        inds.setInputDDName("");
        assertEquals(inds.getInputDDName(), "");

        inds.setInputDDName(null);
        assertEquals(inds.getInputDDName(), null);
    }

    public void testGetSetMaxRecordLen() {
        //dbStartup();
        InputDataset inds = getInputDatasetPhysicalFile(1);
        assertEquals(inds.getMaxRecordLen(), 0);

        inds.setMaxRecordLen(1);
        assertEquals(inds.getMaxRecordLen(), 1);

        inds.setMaxRecordLen(2);
        assertEquals(inds.getMaxRecordLen(), 2);

        inds.setMaxRecordLen(0);
        assertEquals(inds.getMaxRecordLen(), 0);
    }

    public void testGetSetMinRecordLen() {
        InputDataset inds = getInputDatasetPhysicalFile(1);
        assertEquals(inds.getMinRecordLen(), 0);

        inds.setMinRecordLen(1);
        assertEquals(inds.getMinRecordLen(), 1);

        inds.setMinRecordLen(2);
        assertEquals(inds.getMinRecordLen(), 2);

        inds.setMinRecordLen(0);
        assertEquals(inds.getMinRecordLen(), 0);
    }

    public void testGetDatasetName_Ouput() throws SAFRException {
        OutputDataset outds = getOutputDatasetPhysicalFile(1);
        assertEquals(outds.getDatasetName(), null);

        PhysicalFile PF = SAFRApplication.getSAFRFactory().getPhysicalFile(8410);
        PhysicalFile.OutputDataset PF_OUTDS = PF.new OutputDataset();

        assertEquals(PF_OUTDS.getDatasetName(), "test1");
    }

    public void testGetSetLrecl() {
        OutputDataset outds = getOutputDatasetPhysicalFile(1);
        assertEquals(outds.getLrecl(), 0);

        outds.setLrecl(1);
        assertEquals(outds.getLrecl(), 1);

        outds.setLrecl(2);
        assertEquals(outds.getLrecl(), 2);

        outds.setLrecl(0);
        assertEquals(outds.getLrecl(), 0);
    }

    public void testGetSetOutputDDName() {
        OutputDataset outds = getOutputDatasetPhysicalFile(1);
        assertEquals(outds.getOutputDDName(), null);

        outds.setOutputDDName("testOutputDDName1");
        assertEquals(outds.getOutputDDName(), "testOutputDDName1");

        outds.setOutputDDName("testOutputDDName2");
        assertEquals(outds.getOutputDDName(), "testOutputDDName2");

        outds.setOutputDDName("");
        assertEquals(outds.getOutputDDName(), "");

        outds.setOutputDDName(null);
        assertEquals(outds.getOutputDDName(), null);
    }

    public void testGetSetRecfmCode() throws SAFRException {
        OutputDataset outds = getOutputDatasetPhysicalFile(1);
        assertEquals(outds.getRecfm(), null);

        CodeSet codeSet;
        codeSet = SAFRApplication.getSAFRFactory().getCodeSet("RECFM");
        Code code1 = codeSet.getCode("VB");
        Code code2 = codeSet.getCode("FBA");
        outds.setRecfm(code1);
        assertEquals(outds.getRecfm(), code1);
        outds.setRecfm(code2);
        assertEquals(outds.getRecfm(), code2);
    }

    public void testGetLoadWarnings() throws SAFRException {

        PhysicalFile pf;

        pf = SAFRApplication.getSAFRFactory().getPhysicalFile(8411);

        List<String> Warning_List = pf.getLoadWarnings();

        assertEquals(Warning_List.get(0),
                "This Physical File does not have a valid file type. Please select a valid file type before saving.");
        assertEquals(Warning_List.get(1),
                "This Physical File does not have a valid access method. Please select a valid access method before saving.");
        assertEquals(Warning_List.get(2),
                "This Physical File does not have a valid RECFM. Please select a valid RECFM, if required, before saving.");
        assertEquals(Warning_List.get(3),
                "This Physical File does not have a valid row format. Please select a valid row format, if required, before saving.");

    }
}
