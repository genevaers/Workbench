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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.model.view.View;

public class TestSAFRFactory extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestSAFRFactory");
	static String ENVIRONMENT_KEY = "c_environtbl";
	TestDataLayerHelper helper = new TestDataLayerHelper();

	// This boolean is added to decide whether the connection to DB should be
	// closed or not in tearDown method.
	boolean disconnectDB = true;

	public void dbStartup() {
		helper.initDataLayer();
	}

	public void tearDown() {
		if (disconnectDB) {
			helper.closeDataLayer();
		}
		disconnectDB = true;
	}

	public void testCloseDatabaseConnection() {
		dbStartup();
		Connection con = null;
		boolean isClosed = false;
		try {
			con = ((PGDAOFactory) DAOFactoryHolder.getDAOFactory())
					.getConnection();
			SAFRApplication.getSAFRFactory().closeDatabaseConnection();
			isClosed = con.isClosed();
			disconnectDB = false;
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		} catch (SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		assertTrue(isClosed);
	}

	public void testCreateUser() {
		dbStartup();
		User user = SAFRApplication.getSAFRFactory().createUser();
		assertNotNull(user);
	}

	public void testCreateLogicalFile() {
		dbStartup();
		LogicalFile lf = SAFRApplication.getSAFRFactory().createLogicalfile();
		assertNotNull(lf);

		LogicalFile lf1 = SAFRApplication.getSAFRFactory().createLogicalfile();
		assertNotNull(lf1);
	}

	public void testCreateLogicalRecord() {
		dbStartup();
		LogicalRecord lr = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		assertNotNull(lr);

		LogicalRecord lr1 = SAFRApplication.getSAFRFactory()
				.createLogicalRecord();
		assertNotNull(lr1);
	}

	public void testCreateUserExitRoutine() {
		dbStartup();
		UserExitRoutine uer = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		assertNotNull(uer);

		UserExitRoutine uer1 = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		assertNotNull(uer1);
	}

	public void testCreateView() throws SAFRException {
		dbStartup();
		View view = SAFRApplication.getSAFRFactory().createView();
		assertNotNull(view);

		View view1 = SAFRApplication.getSAFRFactory().createView();
		assertNotNull(view1);
	}

	public void testGetAllUserExitRoutines() throws SAFRException {
		dbStartup();

		List<UserExitRoutine> userExitRoutines = null;

		userExitRoutines = SAFRApplication.getSAFRFactory()
				.getAllUserExitRoutines();
		assertNotNull(userExitRoutines);

	}

	public void testGetLogicalFile() {
		dbStartup();
		LogicalFile logicalFile = null;
		try {
			logicalFile = SAFRApplication.getSAFRFactory().getLogicalFile(1288);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertNotNull(logicalFile);
	}

	public void testGetLogicalRecord() {
		dbStartup();
		LogicalRecord logicalRecord = null;
		try {
			logicalRecord = SAFRApplication.getSAFRFactory().getLogicalRecord(
					1288);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertNotNull(logicalRecord);
	}

	public void testGetLRField() {
		dbStartup();
		LogicalRecord logicalRecord = null;
		try {
			logicalRecord = SAFRApplication.getSAFRFactory().getLogicalRecord(
					1288);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		List<LRField> lrField = null;
		try {
			lrField = SAFRApplication.getSAFRFactory().getLRFields(
					logicalRecord);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertNotNull(lrField);
	}

	public void testGetUserExitRoutine() {
		dbStartup();
		UserExitRoutine userExitRoutine = null;
		try {
			userExitRoutine = SAFRApplication.getSAFRFactory()
					.getUserExitRoutine(1);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertNotNull(userExitRoutine);
	}

	public void testGetViewFolder() throws SAFRException {
		dbStartup();
		ViewFolder viewFolder = null;
		viewFolder = SAFRApplication.getSAFRFactory().getViewFolder(1);
		assertNotNull(viewFolder);

		ViewFolder viewFolder1 = SAFRApplication.getSAFRFactory()
				.getViewFolder(1, 1);
		assertNotNull(viewFolder1);

	}

	public void testCreateControlRecord() {
		dbStartup();
		ControlRecord cr = SAFRApplication.getSAFRFactory()
				.createControlRecord();
		assertNotNull(cr);
		ControlRecord cr1 = SAFRApplication.getSAFRFactory()
				.createControlRecord();
		assertNotNull(cr1);
	}

	public void testCreateEnvironment() {

		dbStartup();

		Environment env = SAFRApplication.getSAFRFactory().createEnvironment();

		assertNotNull(env);

	}

	public void testCreateGroup() {
		dbStartup();

		Group group = SAFRApplication.getSAFRFactory().createGroup();

		assertNotNull(group);

	}

	public void testCreateViewFolder() {
		dbStartup();

		ViewFolder viewFolder = SAFRApplication.getSAFRFactory()
				.createViewFolder();
		assertNotNull(viewFolder);
		ViewFolder viewFolder1 = SAFRApplication.getSAFRFactory()
				.createViewFolder();
		assertNotNull(viewFolder1);
		ViewFolder viewFolder2 = SAFRApplication.getSAFRFactory()
				.createViewFolder();
		assertNotNull(viewFolder2);

	}

	public void testGetAllCodeSets() {
		dbStartup();
		Map<String, CodeSet> codeSets = null;
		try {
			codeSets = SAFRApplication.getSAFRFactory().getAllCodeSets();
		} catch (SAFRException se) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", se);
		}
		assertNotNull(codeSets);
	}

	public void testGetCodeSet() {
		disconnectDB = false;
		CodeSet codeSet = null;
		codeSet = SAFRApplication.getSAFRFactory().getCodeSet("ACCMETHOD");
		assertNotNull(codeSet);
	}

	public void testGetControlRecord() {
		dbStartup();
		ControlRecord controlRecord = null;
		try {
			controlRecord = SAFRApplication.getSAFRFactory()
					.getControlRecord(1);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertNotNull(controlRecord);
	}

	public void testGetEnvironment() {
		dbStartup();
		Environment environment = null;
		try {
			environment = SAFRApplication.getSAFRFactory().getEnvironment(1);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertNotNull(environment);
	}

	public void testGetGroup() {
		dbStartup();
		Group group = null;
		try {
			group = SAFRApplication.getSAFRFactory().getGroup(1);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertNotNull(group);
	}

	public void testGetUser() {
		dbStartup();
		User user = null;
		try {
			user = SAFRApplication.getSAFRFactory().getUser("ADMIN");
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertNotNull(user);
	}

	public void testCreatePhysicalFile() {
		dbStartup();
		PhysicalFile pf = SAFRApplication.getSAFRFactory().createPhysicalFile();
		assertNotNull(pf);
		PhysicalFile pf1 = SAFRApplication.getSAFRFactory()
				.createPhysicalFile();
		assertNotNull(pf1);
		PhysicalFile pf2 = SAFRApplication.getSAFRFactory()
				.createPhysicalFile();
		assertNotNull(pf2);
		PhysicalFile pfl = SAFRApplication.getSAFRFactory()
				.createPhysicalFile();

		assertNotNull(pfl);
	}

//	public void testGetPhysicalFile() {
//		dbStartup();
//		boolean correctException = false;
//		PhysicalFile pf = null;
//
//		try {
//			SAFRApplication.getSAFRFactory().getPhysicalFile(null);
//		} catch (NullPointerException ne) {
//			correctException = true;
//		} catch (SAFRException e) {
//			assertTrue(false);
//			logger.log(Level.SEVERE, "", e);
//		}
//		assertTrue(correctException);
//		try {
//			pf = SAFRApplication.getSAFRFactory().getPhysicalFile(8374);
//		} catch (SAFRException e) {
//			assertTrue(false);
//			logger.log(Level.SEVERE, "", e);
//		}
//		assertNotNull(pf);
//	}

	public void testGetView() throws SAFRException {
		dbStartup();
		View view = null;
		view = SAFRApplication.getSAFRFactory().getView(2206);
		assertNotNull(view);
		View view1 = SAFRApplication.getSAFRFactory().getView(2206, 1);
		assertNotNull(view1);

	}

	public void testGetLookuppath() throws SAFRException {
		dbStartup();
		LookupPath lukp = null;
		lukp = SAFRApplication.getSAFRFactory().getLookupPath(11);
		assertNotNull(lukp);
		LookupPath lukp1 = null;
		lukp1 = SAFRApplication.getSAFRFactory().getLookupPath(1, 1);
		assertNotNull(lukp1);

	}

	public void testRemoveView() throws SAFRException {
		dbStartup();
        // test permanently deleting view.
		SAFRApplication.getSAFRFactory().removeView(8516);
	}

	public void testLoadHeaderFooterItems() throws SAFRException {
		View view = null;
		dbStartup();
		try {
			view = SAFRApplication.getSAFRFactory().getView(8518);
		} catch (SAFRException e) {
			e.printStackTrace();
			assertFalse(true);
		}
		// getting a view will load Header Footer items
		assertEquals(6, view.getHeader().getItems().size());
		assertEquals(6, view.getFooter().getItems().size());

	}

	public void testRemoveUserExitRoutine() throws SAFRException {
		dbStartup();
		Boolean correctException = false;

		// Testing for user exit which has dependency
		try {
			SAFRApplication.getSAFRFactory().removeUserExitRoutine(225);
		} catch (SAFRDependencyException sde) {
			sde.printStackTrace();
			correctException = true;
		}
		assertTrue(correctException);

		// Test removing a userExit which doesn't have any dependency.It should
		// be
		// deleted.
		SAFRApplication.getSAFRFactory().removeUserExitRoutine(228);

		// test if deleted or not
		correctException = false;
		try {
			SAFRApplication.getSAFRFactory().getUserExitRoutine(228);
		} catch (SAFRNotFoundException snfe) {
			snfe.printStackTrace();
			correctException = true;

		}
		assertTrue(correctException);
	}

	public void testRemoveControlRecord() throws SAFRException {
		dbStartup();
		Boolean correctException = false;

		// Testing for control record which has dependency
		try {
			SAFRApplication.getSAFRFactory().removeControlRecord(78);
		} catch (SAFRDependencyException sde) {
			sde.printStackTrace();
			correctException = true;
		}
		assertTrue(correctException);

		// Test removing a Control Record which doesn't have any dependency.It
		// should
		// be
		// deleted.
		SAFRApplication.getSAFRFactory().removeControlRecord(77);

		// test if deleted or not
		correctException = false;
		try {
			SAFRApplication.getSAFRFactory().getControlRecord(77);
		} catch (SAFRNotFoundException snfe) {
			snfe.printStackTrace();
			correctException = true;

		}
		assertTrue(correctException);
	}

	public void testRemovePhysicalFile() throws SAFRException {
		dbStartup();
		Boolean correctException = false;

		// Testing for Physical File with LF dependency
		try {
			SAFRApplication.getSAFRFactory().removePhysicalFile(8391);
		} catch (SAFRDependencyException sde) {
			sde.printStackTrace();
			assertTrue(sde.getDependencyList().containsKey(
					ComponentType.LogicalFile));
			correctException = true;
		}
		assertTrue(correctException);

		// Test for Physical File with view dependencies
		correctException = false;
		try {
			SAFRApplication.getSAFRFactory().removePhysicalFile(8390);
		} catch (SAFRDependencyException sde) {
			sde.printStackTrace();
			assertTrue(sde.getDependencyList().containsKey(ComponentType.View));
			correctException = true;
		}
		assertTrue(correctException);

		// Test removing a Physical File which doesn't have any dependency.It
		// should
		// be
		// deleted.
		SAFRApplication.getSAFRFactory().removePhysicalFile(8370);

		// test if deleted or not
		correctException = false;
		try {
			SAFRApplication.getSAFRFactory().getPhysicalFile(8370);
		} catch (SAFRNotFoundException snfe) {
			snfe.printStackTrace();
			correctException = true;

		}
		assertTrue(correctException);
	}

	public void testRemoveLogicalFile() throws SAFRException {
		dbStartup();
		Boolean correctException = false;

		// Testing for Logical File with LR dependency
		try {
			SAFRApplication.getSAFRFactory().removeLogicalFile(1338);
		} catch (SAFRDependencyException sde) {
			sde.printStackTrace();
			assertTrue(sde.getDependencyList().containsKey(
					ComponentType.LogicalRecord));
			correctException = true;
		}
		assertTrue(correctException);

		// Test for Logical File with view and Lookup dependencies
		correctException = false;
		try {
			SAFRApplication.getSAFRFactory().removeLogicalFile(1340);
		} catch (SAFRDependencyException sde) {
			sde.printStackTrace();
			assertTrue(sde.getDependencyList().containsKey(ComponentType.View));
			assertTrue(sde.getDependencyList().containsKey(
					ComponentType.LookupPath));
			correctException = true;
		}
		assertTrue(correctException);

		// Test removing a Logical File which doesn't have any dependency.It
		// should
		// be
		// deleted.
		SAFRApplication.getSAFRFactory().removeLogicalFile(1325);

		// test if deleted or not
		correctException = false;
		try {
			SAFRApplication.getSAFRFactory().getLogicalFile(1325);
		} catch (SAFRNotFoundException snfe) {
			snfe.printStackTrace();
			correctException = true;

		}
		assertTrue(correctException);
	}

	public void testRemoveLookupPath() throws SAFRException {
		dbStartup();
		Boolean correctException = false;

		// Testing for Lookup Path with view dependency
		try {
			SAFRApplication.getSAFRFactory().removeLookupPath(2003);
		} catch (SAFRDependencyException sde) {
			sde.printStackTrace();
			assertTrue(sde.getDependencyList().containsKey(ComponentType.View));
			correctException = true;
		}
		assertTrue(correctException);

		// Test removing a Lookup Path which doesn't have any dependency.It
		// should
		// be
		// deleted.
		SAFRApplication.getSAFRFactory().removeLookupPath(1996);

		// test if deleted or not
		correctException = false;
		try {
			SAFRApplication.getSAFRFactory().getLookupPath(1996);
		} catch (SAFRNotFoundException snfe) {
			snfe.printStackTrace();
			correctException = true;

		}
		assertTrue(correctException);
	}

	public void testRemoveViewFolder() throws SAFRException {
		dbStartup();

		SAFRApplication.getSAFRFactory().removeViewFolder(715, null);
        try {
            SAFRApplication.getSAFRFactory().getViewFolder(715);
            fail();
        } catch (SAFRNotFoundException snfe) {
        }
		
		SAFRApplication.getSAFRFactory().removeViewFolder(716, null);
		try {
			SAFRApplication.getSAFRFactory().getViewFolder(716);
			fail();
		} catch (SAFRNotFoundException snfe) {
		}
	}

	public void testRemoveLogicalRecord() throws SAFRException {
		dbStartup();
		Boolean correctException = false;

		// Testing for Logical Record which has dependency.
		try {
			SAFRApplication.getSAFRFactory().removeLogicalRecord(1348);
		} catch (SAFRDependencyException sde) {
			sde.printStackTrace();
			assertTrue(sde.getDependencyList().containsKey(ComponentType.View));
			assertTrue(sde.getDependencyList().containsKey(
					ComponentType.LookupPath));
			correctException = true;
		}
		assertTrue(correctException);

		// Test removing a Logical Record which has no dependency..It
		// should
		// be
		// deleted.
		SAFRApplication.getSAFRFactory().removeLogicalRecord(1325);

		// test if deleted or not
		correctException = false;
		try {
			SAFRApplication.getSAFRFactory().getLogicalRecord(1325);
		} catch (SAFRNotFoundException snfe) {
			snfe.printStackTrace();
			correctException = true;

		}
		assertTrue(correctException);
	}

	public void testRemoveGroup() throws SAFRException {
		dbStartup();
		Boolean correctException = false;

		// Testing for Group which has dependency.
		try {
			SAFRApplication.getSAFRFactory().removeGroup(28);
		} catch (SAFRDependencyException sde) {
			sde.printStackTrace();
			assertTrue(sde.getDependencyList().containsKey(ComponentType.User));
			assertTrue(sde.getDependencyList().containsKey(
					ComponentType.Environment));
			correctException = true;
		}
		assertTrue(correctException);

		// Test removing a Group which has no dependency..It
		// should
		// be
		// deleted.
		SAFRApplication.getSAFRFactory().removeGroup(10);

		// test if deleted or not
		correctException = false;
		try {
			SAFRApplication.getSAFRFactory().getGroup(10);
		} catch (SAFRNotFoundException snfe) {
			snfe.printStackTrace();
			correctException = true;

		}
		assertTrue(correctException);
	}

	public void testRemoveUser() throws SAFRException {
		dbStartup();
		Boolean correctException = false;
		SAFRApplication.getSAFRFactory().removeUser("TODELETE");

		// test if deleted or not
		correctException = false;
		try {
			SAFRApplication.getSAFRFactory().getUser("TODELETE");
		} catch (SAFRNotFoundException snfe) {
			snfe.printStackTrace();
			correctException = true;

		}
		assertTrue(correctException);
	}

	public void testGetLogicalRecordFromLRLFAssociation() {
		dbStartup();
		Boolean correctException = false;
		LogicalRecord lr = null;

		// check for existing LRLF association id
		try {
			lr = SAFRApplication.getSAFRFactory()
					.getLogicalRecordFromLRLFAssociation(5, 1);

		} catch (SAFRException sfe) {
			correctException = true;
			sfe.printStackTrace();
		}

		assertFalse(correctException);
		assertNotNull(lr);

		// check for non-existing LRLF association id
		lr = null;
		try {
			lr = SAFRApplication.getSAFRFactory()
					.getLogicalRecordFromLRLFAssociation(9999999, 1);

		} catch (SAFRException sfe) {
			correctException = true;
			sfe.printStackTrace();
		}

		assertTrue(correctException);
		assertNull(lr);
	}
	
	public void testRemoveViewFolderTokenError() throws SAFRException {
		dbStartup();
		//helper.setUser("ADMIN");
		helper.setEnv(121);
		
		ViewFolder vf = SAFRApplication.getSAFRFactory().createViewFolder();
		
	
		SAFRValidationToken token = new SAFRValidationToken(vf, SAFRValidationType.ERROR);
		
		try{
		SAFRApplication.getSAFRFactory().removeViewFolder(968, token);
		fail();
		} catch (IllegalArgumentException e){
			assertEquals("The validation token does not identify this",(e.getMessage().substring(0, 43)));
		}
	
	}

	public void testRemoveViewFolderTokenNull() throws SAFRException  {
		dbStartup();
		helper.setUser("TESTVF");
		helper.setEnv(121);
		
		ViewFolder vf = SAFRApplication.getSAFRFactory().getViewFolder(968);
		
		User user = SAFRApplication.getSAFRFactory().getUser("TESTVF");
		user.setDefaultViewFolder(vf);
		user.store();
		SAFRValidationToken token = null;
		
		boolean correctException = false;
		try {
	        SAFRApplication.getSAFRFactory().removeViewFolder(968, token);
	        fail();
        } catch (SAFRValidationException e) {
        	 correctException = true;
        	
        }
        assertTrue(correctException);
	
	}
	
	public void testRemoveViewFolderNoPerm() throws SAFRException {
		dbStartup();
		helper.setUser("NOPERM");
		helper.setEnv(121);
		
		ViewFolder vf = SAFRApplication.getSAFRFactory().createViewFolder();
		
	
		SAFRValidationToken token = new SAFRValidationToken(vf, SAFRValidationType.ERROR);
		
		try{
		SAFRApplication.getSAFRFactory().removeViewFolder(968, token);
		fail();
		} catch (SAFRException e){
			assertEquals("The user is not authorized to perform this deletion.",(e.getMessage()));
		}
	
	}
	
	public void testClearEnvironmentNotAdmin() throws SAFRException {
		dbStartup();
		helper.setUser("NOPERM");
	//	helper.setEnv(122);
		
		//Environment env = SAFRApplication.getSAFRFactory().getEnvironment(122);
		
		
		try{
		SAFRApplication.getSAFRFactory().clearEnvironment(122);
		fail();
		} catch (SAFRException e){
			assertEquals("The user should be an Admin of this environment to perform clear operation.",(e.getMessage()));
		}
	
	}
	
	public void testClearEnvironmentNoPermisson() throws SAFRException {
		dbStartup();
		helper.setUser("QUINNO");
		helper.setEnv(99);
		
		//Environment env = SAFRApplication.getSAFRFactory().getEnvironment(122);

		try{
		SAFRApplication.getSAFRFactory().clearEnvironment(122);
		fail();
		} catch (SAFRException e){
			assertEquals("The user should be an Admin of this environment to perform clear operation.",(e.getMessage()));
		}
	
	}
	
	public void testClearEnvironmentEnvAdmin() throws SAFRException {
		dbStartup();
		helper.setUser("TESTENV");
		helper.setEnv(122);
		
		boolean correctException = false;

		try{
		SAFRApplication.getSAFRFactory().clearEnvironment(122);
		correctException = true;
		} catch (SAFRException e){		
		}
		 assertTrue(correctException);
	}
	
	public void testClearEnvironmentEnvAdminDiffEnv() throws SAFRException {
		dbStartup();
		helper.setUser("TESTENV");
		helper.setEnv(104);
		
		boolean correctException = false;

		try{
		SAFRApplication.getSAFRFactory().clearEnvironment(122);
		correctException = true;
		} catch (SAFRException e){		
		}
		 assertTrue(correctException);
	}
	
	public void testRemoveEnvironment() throws SAFRException {
		dbStartup();
		helper.setUser("ADMIN");
	
		boolean correctException = false;
		
		Environment env = SAFRApplication.getSAFRFactory().createEnvironment();
	
		env.setName("qqhello");
		env.setComment("qq comment");
		env.initialize("", "");
		env.store();
		
		System.out.println("env ID created is " + env.getId());
		try{
		SAFRApplication.getSAFRFactory().clearEnvironment(env.getId());	
	 	SAFRApplication.getSAFRFactory().removeEnvironment(env.getId());
		correctException = true;
		} catch (SAFRException e){		
		}
		 assertTrue(correctException);
	}
	
	public void testRemoveEnvironmentNotAdmin() throws SAFRException {
		dbStartup();
		helper.setUser("NOPERM");
		
		try{	
	 	SAFRApplication.getSAFRFactory().removeEnvironment(122);
	 	fail();
		} catch (SAFRException e){	
			assertEquals("The user is not authorized to perform this deletion.",(e.getMessage()));
		}

	}
	
	public void testGetLRFieldLRID() throws SAFRException {
		dbStartup();
		 List<LRField> lrField_list = SAFRApplication.getSAFRFactory().getLRFields(1288);
		LRField lrfield = lrField_list.get(0);
		assertEquals("Id",lrfield.getName());
	}
	
	public void testRemoveViewNoPerm() throws DAOException, SAFRException   {
		dbStartup();
		helper.setUser("NOPERM");
		helper.setEnv(121);
	
		try {
	        SAFRApplication.getSAFRFactory().removeView(8718);
        } catch (SAFRException e) {
        	assertEquals("The user is not authorized to perform this deletion.",(e.getMessage()));
        }
	}
	
	public void testRemoveLogicalRecordNoPerm() throws DAOException, SAFRException   {
		dbStartup();
		helper.setUser("NOPERM");
		helper.setEnv(121);
	
		try {
	        SAFRApplication.getSAFRFactory().removeLogicalRecord(1492);
        } catch (SAFRException e) {
        	assertEquals("The user is not authorized to perform this deletion.",(e.getMessage()));
        }
	}
	
	public void testRemovePhysicalFileNoPerm() throws DAOException, SAFRException   {
		dbStartup();
		helper.setUser("NOPERM");
		helper.setEnv(121);
	
		try {
	        SAFRApplication.getSAFRFactory().removePhysicalFile(8685);
        } catch (SAFRException e) {
        	assertEquals("The user is not authorized to perform this deletion.",(e.getMessage()));
        }
               
	}
	
	public void testRemoveLogicalFileNoPerm() throws DAOException, SAFRException   {
		dbStartup();
		helper.setUser("NOPERM");
		helper.setEnv(121);
	
		try {
	        SAFRApplication.getSAFRFactory().removeLogicalFile(1599);
        } catch (SAFRException e) {
        	assertEquals("The user is not authorized to perform this deletion.",(e.getMessage()));
        }	
}
	
	public void testRemoveUserExitRoutineNoPerm() throws DAOException, SAFRException   {
		dbStartup();
		helper.setUser("NOPERM");
		helper.setEnv(121);
	
		try {
	        SAFRApplication.getSAFRFactory().removeUserExitRoutine(343);
        } catch (SAFRException e) {
        	assertEquals("The user is not authorized to perform this deletion.",(e.getMessage()));
        }	
}
	
	public void testRemoveLookupPathNoPerm() throws DAOException, SAFRException   {
		dbStartup();
		helper.setUser("NOPERM");
		helper.setEnv(121);
	
		try {
	        SAFRApplication.getSAFRFactory().removeLookupPath(2113);
        } catch (SAFRException e) {
        	assertEquals("The user is not authorized to remove this lookup path.",(e.getMessage()));
        }	
}
	
	public void testRemoveControlRecordNoPerm() throws DAOException, SAFRException   {
		dbStartup();
		helper.setUser("NOPERM");
		helper.setEnv(121);
	
		try {
	        SAFRApplication.getSAFRFactory().removeControlRecord(1);
        } catch (SAFRException e) {
        	assertEquals("The user is not authorized to perform this deletion.",(e.getMessage()));
        }	
}
	
	public void testRemoveUserNoPerm() throws DAOException, SAFRException   {
		dbStartup();
		helper.setUser("NOPERM");
		helper.setEnv(121);
	
		try {
	        SAFRApplication.getSAFRFactory().removeUser("REMOVEME");
        } catch (SAFRException e) {
        	assertEquals("The user is not authorized to perform this deletion.",(e.getMessage()));
        }	
		
	}
}
