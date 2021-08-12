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
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;

public class TestLogicalFile extends TestCase {
	static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.TestLogicalFile");

	static String TABLE_NAME = "LOGFILE";
	static String COL_ID = "LOGFILEID";

	static String DEP_TABLE_LIST[] = { TABLE_NAME };
	TestDataLayerHelper helper = new TestDataLayerHelper();
    List<Integer> delIds = new ArrayList<Integer>();

	public void setUp() throws DAOException {
		helper.initDataLayer(110);
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

	public void testGetAddPhysicalFileAssociation() throws SAFRException {
		LogicalFile LF = SAFRApplication.getSAFRFactory().createLogicalfile();
		PhysicalFile PF;

		PF = SAFRApplication.getSAFRFactory().getPhysicalFile(8415);
		LF.addAssociatedPhysicalFile(new FileAssociation(PF, 8415, PF.getName(), null));
		SAFRList<FileAssociation> LF_LIST = LF.getPhysicalFileAssociations();

		for (FileAssociation LF_fileAss : LF_LIST.getActiveItems()) {

			// System.out.println(LF_FileASS.getAssociatedComponentIdString());
			assertEquals(LF_fileAss.getAssociatedComponentName(), "TestLogicalFile_01_03");
			assertEquals(LF_fileAss.getAssociatedComponentIdString(), "8415");
		}
	}

	public void testgetLogicalFileAssociations() throws SAFRException {
			LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(1352);
			SAFRImmutableList<ComponentAssociation> lf_lr_Association_List = lf.getLogicalRecordAssociations();
			ComponentAssociation lf_fileAss = lf_lr_Association_List.get(0);

			assertEquals(lf_fileAss.getAssociatedComponentName(), "Test_LF_LR_Association");

		}

	public void testRemoveAssociatedPhysicalFile() throws SAFRException {
		LogicalFile LF = SAFRApplication.getSAFRFactory().getLogicalFile(1354);
		PhysicalFile PF = SAFRApplication.getSAFRFactory().getPhysicalFile(8415);

		SAFRValidationToken Token = null;

		SAFRList<FileAssociation> LF_List = LF.getPhysicalFileAssociations();
		FileAssociation LF_fileAss = LF_List.get(0);

		LF.removeAssociatedPhysicalFile(LF_fileAss, Token);

		LF.store();

		assertTrue(LF_List.isEmpty());

		LF.addAssociatedPhysicalFile(new FileAssociation(PF, 8415, PF.getName(), null));
		LF.store();

	}

	public void testRemoveAssociatedPhysicalFileDepView() throws SAFRException {
		LogicalFile LF = SAFRApplication.getSAFRFactory().getLogicalFile(1336);

		SAFRValidationToken Token = null;

		SAFRList<FileAssociation> LF_List = LF.getPhysicalFileAssociations();
		FileAssociation LF_fileAss = LF_List.get(0);

		try {
			LF.removeAssociatedPhysicalFile(LF_fileAss, Token);
			fail();
		} catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "(?s)^Physical File:\\s*Test_LF_Dependencies\\s*\\[8391\\].*View\\s*:.*Test_LF_PF_Dependency_VIEW\\s*\\[8549\\]\\s*-\\s*\\[View Properties\\].*$";
			assertTrue(errMsg.matches(pattern));			
		}


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

	public void testStoreNoPermissionCreate() throws SAFRException {
		
		helper.setUser("NOPERM");
		
		LogicalFile lf = SAFRApplication.getSAFRFactory().createLogicalfile();

		FileAssociation fileAssociation = new FileAssociation(lf, 8374, "testPhysicalFile", null);
		FileAssociation fileAssociation1 = new FileAssociation(lf, 8361, "fdfd", null);
		FileAssociation fileAssociation2 = new FileAssociation(lf, 8369, "shruti_pf1", null);

		lf.setName("Test_Junit_LF_12345");
		lf.setComment("test");
		lf.addAssociatedPhysicalFile(fileAssociation);
		lf.addAssociatedPhysicalFile(fileAssociation1);
		lf.addAssociatedPhysicalFile(fileAssociation2);

		try {
			lf.store();
			fail();
		} catch (SAFRException e) {
			String mes = e.getMessage();
			assertEquals(mes, "The user is not authorized to create a new logical file.");
		}

	}

	public void testStoreNoPermissionUpdate() throws SAFRException {
		
		helper.setUser("NOPERM");
		
		LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(1354);

		lf.setName("Test_Modify_NoPerm");

		try {
			lf.store();
			fail();
		} catch (SAFRException e) {
			String mes = e.getMessage();
			assertEquals(mes, "The user is not authorized to update this logical file.");
		}
	}
	
	public void testStore() throws SAFRException {

		/* Setup File Association and create logical file object */
		FileAssociation fileAssociation = null;
		FileAssociation fileAssociation1 = null;
		FileAssociation fileAssociation2 = null;
		LogicalFile lf = SAFRApplication.getSAFRFactory().createLogicalfile();

		fileAssociation = new FileAssociation(lf, 8374, "testPhysicalFile", null);
		fileAssociation1 = new FileAssociation(lf, 8361, "fdfd", null);
		fileAssociation2 = new FileAssociation(lf, 8369, "shruti_pf1", null);

		/* Test storing an empty "Null" logical file object . Fail if no exception is thrown */
		try {
			lf.store();
			fail();
		} catch (SAFRException e) {
		}

		/* Test storing of logical File with name , comment and pf file being associated, Fail if exception is thrown */
		
		lf.setName("Test_Junit_LF_12345");
		lf.setComment("test");
		lf.addAssociatedPhysicalFile(fileAssociation);
		lf.addAssociatedPhysicalFile(fileAssociation1);
		lf.addAssociatedPhysicalFile(fileAssociation2);

		lf.store();

		PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();

		/* Get logical file fields and check on stored values */
		
		assertEquals(lf.getName(), "Test_Junit_LF_12345");
		assertEquals(lf.getComment(), "test");
		assertEquals(lf.getCreateBy(), fact.getSAFRLogin().getUserId());
		assertEquals(lf.getPhysicalFileAssociations().get(0).getAssociationId(), fileAssociation.getAssociationId());
		assertNotNull(lf.getCreateTime());
		assertNotNull(lf.getModifyTime());

		// Delete the stored LF
		SAFRApplication.getSAFRFactory().removeLogicalFile(lf.getId());		
		// try storing a deleted LF. Fail if no exception is thrown //

		lf.setName("DeletedLF");
		try {
			lf.store();
			fail();
		} catch (SAFRException e) {
		}
	}

	public void testValidate_LF_Name_Empty() {
		// dbStartup();
		LogicalFile lf = SAFRApplication.getSAFRFactory().createLogicalfile();

		try {
			lf.validate();
			fail();			
		} 
		catch (SAFRException e) {
			String emsg = e.toString().trim();
			assertEquals(emsg.substring(0, emsg.indexOf(".", 0)),
			        "Logical File name cannot be empty");

		}
	}

	public void testValidate_PF_Association() {
		// dbStartup();
		LogicalFile lf = SAFRApplication.getSAFRFactory().createLogicalfile();
		lf.setName("Test_ERROR_msg4");

		try {
			lf.validate();
			fail();			
		} 
		catch (SAFRException e) {
			String emsg = e.toString().trim();
			assertEquals(emsg.substring(0, emsg.indexOf(".", 0)),
			        "There must be at least one corresponding physical file");
		}
	}

	public void testValidate_PF_AssociationMarked() throws SAFRException{
		
		LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(1491);

		for (FileAssociation fa : lf.getPhysicalFileAssociations()) {
			lf.getPhysicalFileAssociations().remove(fa);
		}

		try {
			lf.validate();
			fail();			
		}
		catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			assertEquals(errMsg, "There must be at least one corresponding physical file.");
		}
	}
	
	public void testValidate_PF_AssociationDepView() throws SAFRException {

		LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(1336);

		for (FileAssociation fa : lf.getPhysicalFileAssociations()) {
			if (fa.getAssociatedComponentIdNum() == 8391) {
				lf.getPhysicalFileAssociations().remove(fa);
				break;
			}
		}
		try {
			lf.validate();
			fail();			
		}
		catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "(?s)^Physical File:\\s*Test_LF_Dependencies\\s*\\[8391\\].*VIEWS\\s*:.*Test_LF_PF_Dependency_VIEW\\s*\\[8549\\]\\s*.*$";
			assertTrue(errMsg.matches(pattern));
		}
	}
	
	public void testValidate_LF_Naming_rules() {
		LogicalFile lf = SAFRApplication.getSAFRFactory().createLogicalfile();
		lf.setName("#1234");

		try {
			lf.validate();
			fail();			
		} 
		catch (SAFRException e) {
			String emsg = e.toString().trim();
			assertEquals(emsg.substring(0, emsg.indexOf(".", 0)),
			        "The Logical File name '#1234' should begin with a letter and should comprise of letters, numbers, pound sign (#) and underscores only");

		}
	}

	public void testValidate_LF_Naming_Max() throws SAFRException {
		LogicalFile lf = SAFRApplication.getSAFRFactory().createLogicalfile();
		lf.setName("Test01234567890123456789012345678901234567890123456789");

		try {
			lf.validate();
			fail();			
		} 
		catch (SAFRValidationException e) {
			String errMsg = e.getErrorMessages().get(0);
			String pattern = "(?s)The length of Logical File name 'Test01234567890123456789012345678901234567890123456789' cannot exceed 48 characters.$";
			assertTrue(errMsg.matches(pattern));
		}
	}
	
	public void testValidate_LF_Comment_Length() {
		// dbStartup();
		LogicalFile lf = SAFRApplication.getSAFRFactory().createLogicalfile();
		lf.setName("Comment_Length");
		lf.setComment(
				"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" +
				"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" +
				"0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");

		try {
			lf.validate();
			fail();			
		} 
		catch (SAFRException e) {
			String emsg = e.toString().trim();
			String message  = emsg.substring(0, emsg.indexOf(".", 0));
			assertEquals(message, "Comment cannot be more than 254 characters");
		}
	}

	public void testValidate_LF_Unique_Name() {
		// dbStartup();
		LogicalFile lf = SAFRApplication.getSAFRFactory().createLogicalfile();
		lf.setName("Test_LF");

		try {
			lf.validate();
			fail();
		}
		catch (SAFRException e) {
			String emsg = e.toString().trim();
			assertEquals(emsg.substring(0, emsg.indexOf(".", 0)),
			        "The Logical File name 'Test_LF' already exists");
		}
	}
	
}
