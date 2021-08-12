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

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.associations.UserGroupAssociation;

public class TestUser extends TestCase {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestUser");

	static String TABLE_NAME = "USER";
	static String COL_ID = "USERID";
	private final String ID = "Test1";
	private final String ID2 = "Test2";
	private final String ID3 = "Test3";
	static String DEP_TABLE_LIST[] = { "SECUSER", TABLE_NAME };

	TestDataLayerHelper helper = new TestDataLayerHelper();

	public void setUp() {
	}

	public void dbStartup() {
		try {
			helper.initDataLayer();
		} catch (DAOException e) {
			assertFalse(true);
		}
	}

	public void removeUser(String id) {

		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);

			PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder
					.getDAOFactory();
			ConnectionParameters params = fact.getConnectionParameters();
			SQLGenerator generator = new SQLGenerator();

			for (String tableName : DEP_TABLE_LIST) {
				try {
					String statement = generator.getDeleteStatement(params
							.getSchema(), tableName, idNames);
					PreparedStatement pst = fact.getConnection()
							.prepareStatement(statement);
					pst.setString(1, id);
					pst.execute();
					pst.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	public void tearDown() {

		try {
			removeUser(ID);
			removeUser(ID2);
			removeUser(ID3);

		} catch (Exception e) {
			// log errors
			logger.log(Level.SEVERE, "", e);
		}

		helper.closeDataLayer();

	}

	public User getUser() {
		dbStartup();
		User user;
		user = SAFRApplication.getSAFRFactory().createUser();
		return user;
	}

	public void testTransaction() throws SAFRException {
		dbStartup();
		User user = null;

		// Check userid and comment when the transaction fails as
		// they were before starting the transaction i.e the User gets saved in
		// the DB and as the Group association is null the transactions gets
		// failed
		// and the whole transaction is rolled back.
		try {

			user = SAFRApplication.getSAFRFactory().getUser("SBHUKYA");

			User saveas = user.saveAs("Test2", true);
			saveas.setComment("updatedcomment");
			saveas.setEmail("email");

			SAFRAssociationList<UserGroupAssociation> assocGrp = saveas
					.getAssociatedGroups();
			assocGrp.set(0, null);
			saveas.store();

		} catch (NullPointerException e) {
			assertTrue(true);
		}
		User usr1 = null;
		try {
			// Check the name when the transaction is successful.

			usr1 = SAFRApplication.getSAFRFactory().getUser("SSSSS");

			User saveas1 = usr1.saveAs("Test3", true);
			saveas1.setComment("updatedcomment");
			saveas1.setFirstName("FirstName");
			saveas1.setEmail("email");

			saveas1.store();

			assertEquals("FirstName", SAFRApplication.getSAFRFactory().getUser(
					"Test3").getFirstName());

		} catch (NullPointerException e) {
			assertTrue(true);

		}

		User user2 = null;
		try {
			// Check the transaction and the undo usage to reset the model
			// objects if the transaction fails in between.

			user2 = SAFRApplication.getSAFRFactory().createUser();

			User saveas3 = user2.saveAs("Test3", true);
			user2.setUserid("NEWUSR");
			user2.setComment("updatedcomment");
			SAFRAssociationList<UserGroupAssociation> assocGrp = saveas3
					.getAssociatedGroups();
			assocGrp.set(0, null);
			user2.store();

		} catch (NullPointerException e) {
			assertTrue(true);
			assertTrue(user2.getUserid() == "");

		}

	}

	public void testGetSetDefaultEnvironment() {
		dbStartup();
		// for general user
		User user = getUser();
		user.setSystemAdmin(false);
		Environment env = null;
		Environment env1;
		boolean correctException = false;
		try {
			env = user.getDefaultEnvironment();
		} catch (SAFRException e) {
			correctException = true;
		}
		assertNull(env);
		assertFalse(correctException);
		correctException = false;
		try {
			env1 = SAFRApplication.getSAFRFactory().getEnvironment(1);
			user.setDefaultEnvironment(env1);
			env = user.getDefaultEnvironment();
		} catch (SAFRException e) {
			correctException = true;
		}
		assertNotNull(env);
		assertFalse(correctException);

		// for system admin
		user = getUser();
		user.setSystemAdmin(true);
		try {
			env = user.getDefaultEnvironment();
		} catch (SAFRException e) {
			throw new RuntimeException(e);
		}
		assertNull(env);
		try {
			env1 = SAFRApplication.getSAFRFactory().getEnvironment(1);
			user.setDefaultEnvironment(env1);
			env = user.getDefaultEnvironment();
		} catch (SAFRException e) {
			logger.log(Level.SEVERE, "", e);
			assertTrue(false);
		}

		assertNotNull(env);
	}

	public void testGetSetDefaultViewFolder() {
		dbStartup();
		// for general user
		User user = getUser();
		user.setSystemAdmin(false);
		ViewFolder vf = null;
		ViewFolder vf1;
		boolean correctException = false;
		try {
			vf = user.getDefaultViewFolder();
		} catch (SAFRException e) {
			correctException = true;
		}
		assertNull(vf);
		assertFalse(correctException);
		correctException = false;
		try {
			vf1 = SAFRApplication.getSAFRFactory().getViewFolder(1, 1);
			user.setDefaultViewFolder(vf1);
			vf = user.getDefaultViewFolder();
		} catch (SAFRException e) {
			correctException = true;
		}
		assertNotNull(vf);
		assertFalse(correctException);

		// for system admin
		user = getUser();
		user.setSystemAdmin(true);
		try {
			vf = user.getDefaultViewFolder();
		} catch (SAFRException e) {
			throw new RuntimeException(e);
		}
		assertNull(vf);

		try {
			vf1 = SAFRApplication.getSAFRFactory().getViewFolder(1, 1);
			user.setDefaultViewFolder(vf1);
			vf = user.getDefaultViewFolder();
		} catch (SAFRException e) {
			throw new RuntimeException(e);
		}
		assertNotNull(vf);

	}

	public void testGetSetDefaultGroup() {
		dbStartup();
		// for general user
		boolean correctException = false;
		User user = getUser();
		user.setSystemAdmin(false);
		Group grp = null;
		Group grp1;
		try {
			grp = user.getDefaultGroup();
		} catch (SAFRException se) {
			correctException = true;
		}
		assertNull(grp);
		assertFalse(correctException);

		correctException = false;

		try {
			grp1 = SAFRApplication.getSAFRFactory().getGroup(2);
			user.setDefaultGroup(grp1);
			grp = user.getDefaultGroup();
		} catch (SAFRException e) {
			correctException = true;
		}

		assertNotNull(grp);
		assertFalse(correctException);

		// for system admin

		correctException = false;
		user = getUser();
		user.setSystemAdmin(true);
		try {

			grp1 = SAFRApplication.getSAFRFactory().getGroup(2);
			user.setDefaultGroup(grp1);
			grp = user.getDefaultGroup();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			correctException = true;
		} catch (SAFRException e) {
			e.printStackTrace();
			assertFalse(true);
		}

		assertTrue(correctException);

		correctException = false;
		user = getUser();
		user.setSystemAdmin(true);
		try {
			grp = user.getDefaultGroup();
		} catch (SAFRException se) {
			correctException = true;
		}
		assertNull(grp);
		assertFalse(correctException);

	}

	public void testGetSetEmail() {
		dbStartup();
		User user = getUser();
		user.setSystemAdmin(false);
		assertEquals(user.getEmail(), null);
		user.setEmail("Test1");
		assertEquals(user.getEmail(), "Test1");
		user.setEmail("Test2");
		assertEquals(user.getEmail(), "Test2");
		user.setEmail("");
		assertEquals(user.getEmail(), "");
		user.setEmail(null);
		assertEquals(user.getEmail(), null);
	}

	public void testGetSetFirstName() {
		dbStartup();
		User user = getUser();
		user.setSystemAdmin(false);
		assertEquals(user.getFirstName(), null);
		user.setFirstName("Test1");
		assertEquals(user.getFirstName(), "Test1");
		user.setFirstName("Test2");
		assertEquals(user.getFirstName(), "Test2");
		user.setFirstName("");
		assertEquals(user.getFirstName(), "");
		user.setFirstName(null);
		assertEquals(user.getFirstName(), null);
	}

	public void testGetId() {
		dbStartup();
		User user = getUser();
		user.setSystemAdmin(false);
		assertEquals(user.getUserid(), "");
		user.setUserid("Test1");
		assertEquals(user.getUserid(), "Test1");
		user.setUserid("Test2");
		assertEquals(user.getUserid(), "Test2");
		user.setUserid("");
		assertEquals(user.getUserid(), "");
		user.setUserid(null);
		assertEquals(user.getUserid(), null);
	}

	public void testGetSetLastName() {
		dbStartup();
		User user = getUser();
		user.setSystemAdmin(false);
		assertEquals(user.getLastName(), null);
		user.setLastName("Test1");
		assertEquals(user.getLastName(), "Test1");
		user.setLastName("Test2");
		assertEquals(user.getLastName(), "Test2");
		user.setLastName("");
		assertEquals(user.getLastName(), "");
		user.setLastName(null);
		assertEquals(user.getLastName(), null);
	}

	public void testGetSetLogLevel() {
		dbStartup();
		User user = getUser();
		user.setSystemAdmin(false);
		assertEquals(user.getLogLevel(), 0);
		user.setLogLevel(1);
		assertEquals(user.getLogLevel(), 1);
		user.setLogLevel(2);
		assertEquals(user.getLogLevel(), 2);
		user.setLogLevel(-1);
		assertEquals(user.getLogLevel(), -1);
		user.setLogLevel(0);
		assertEquals(user.getLogLevel(), 0);
	}

	public void testGetSetMaxCompileErrors() {
		dbStartup();
		User user = getUser();
		user.setSystemAdmin(false);
		assertEquals(user.getMaxCompileErrors(), 0);
		user.setMaxCompileErrors(1);
		assertEquals(user.getMaxCompileErrors(), 1);
		user.setMaxCompileErrors(2);
		assertEquals(user.getMaxCompileErrors(), 2);
		user.setMaxCompileErrors(-1);
		assertEquals(user.getMaxCompileErrors(), -1);
		user.setMaxCompileErrors(0);
		assertEquals(user.getMaxCompileErrors(), 0);
	}

	public void testGetSetMiddleInitial() {
		dbStartup();
		User user = getUser();
		user.setSystemAdmin(false);
		assertEquals(user.getMiddleInitial(), null);
		user.setMiddleInitial("Test1");
		assertEquals(user.getMiddleInitial(), "Test1");
		user.setMiddleInitial("Test2");
		assertEquals(user.getMiddleInitial(), "Test2");
		user.setMiddleInitial("");
		assertEquals(user.getMiddleInitial(), "");
		user.setMiddleInitial(null);
		assertEquals(user.getMiddleInitial(), null);
	}

	public void testGetSetPassword() {
		dbStartup();
		User user = getUser();
		user.setSystemAdmin(false);
		assertEquals(user.getPassword(), null);
		user.setPassword("Test1");
		assertEquals(user.getPassword(), "Test1");
		user.setPassword("Test2");
		assertEquals(user.getPassword(), "Test2");
		user.setPassword("");
		assertEquals(user.getPassword(), "");
		user.setPassword(null);
		assertEquals(user.getPassword(), null);
	}

	public void testIsAuthenticated() {
		dbStartup();
		User user = getUser();
		user.setSystemAdmin(false);
		assertEquals(user.isAuthenticated(), false);
	}

	public void testGetSetSystemAdmin() {
		dbStartup();
		// for general user
		User user = getUser();
		user.setSystemAdmin(false);
		assertEquals(user.isSystemAdmin(), false);

		// for system admin
		user = getUser();
		user.setSystemAdmin(true);
		assertEquals(user.isSystemAdmin(), true);
	}

	public void testStore() {
		dbStartup();
		// for general user
		boolean correctException = false;
		User user = getUser();
		user.setSystemAdmin(false);

		correctException = false;
		Environment env = null;
		ViewFolder vf = null;
		Group grp = null;
		try {
			env = SAFRApplication.getSAFRFactory().getEnvironment(1);
			vf = SAFRApplication.getSAFRFactory().getViewFolder(1, 1);
			grp = SAFRApplication.getSAFRFactory().getGroup(2);
			user.setDefaultEnvironment(env);
			user.setUserid(null);
			user.setFirstName("Test1");
			user.setLastName("Test");
			user.setLogLevel(1);
			user.setMaxCompileErrors(1);
			user.setMiddleInitial("s");
			user.setDefaultViewFolder(vf);
			user.setDefaultGroup(grp);
			user.setEmail("Test");
			user.setPassword("Test");
			user.store();
		} catch (DAOException e1) {
			correctException = true;
		} catch (SAFRException e) {
			correctException = true;
		}
		assertTrue(correctException);

		Boolean noException = false;
		correctException = false;

		try {
			env = SAFRApplication.getSAFRFactory().getEnvironment(1);
			vf = SAFRApplication.getSAFRFactory().getViewFolder(1, 1);
			grp = SAFRApplication.getSAFRFactory().getGroup(2);
			user.setDefaultEnvironment(env);
			user.setUserid("Test1");
			user.setFirstName("Test");
			user.setLastName("Test");
			user.setLogLevel(1);
			user.setMaxCompileErrors(1);
			user.setMiddleInitial("s");
			user.setDefaultViewFolder(vf);
			user.setDefaultGroup(grp);
			user.setEmail("Test");
			user.setPassword("Test");
			user.store();
			noException = true;
		} catch (DAOException e1) {
			correctException = true;
		} catch (SAFRException e) {
			correctException = true;
		}
		PGDAOFactory fact = null;
		try {
			fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
		} catch (DAOException e) {
			e.printStackTrace();
		}
		assertTrue(noException);
		assertFalse(correctException);
		assertEquals(user.getUserid(), "Test1");
		assertEquals(user.getFirstName(), "Test");
		assertEquals(user.getLastName(), "Test");
		assertEquals(user.getEmail(), "Test");
		assertEquals(user.getPassword(), "Test");
		assertEquals(user.getLogLevel(), 1);
		assertEquals(user.getMaxCompileErrors(), 1);
		assertEquals(user.getMiddleInitial(), "s");
		assertEquals(user.getCreateBy(), fact.getSAFRLogin().getUserId());
		assertEquals(user.getModifyBy(), fact.getSAFRLogin().getUserId());
		assertNotNull(user.getCreateTime());
		assertNotNull(user.getModifyTime());

		try {
			assertEquals(user.getDefaultViewFolder(), vf);
			assertEquals(user.getDefaultEnvironment(), env);
			assertEquals(user.getDefaultGroup(), grp);
		} catch (SAFRException se) {
			correctException = true;
		}
		assertFalse(correctException);

		// for system Admin

		correctException = false;
		user = getUser();
		user.setSystemAdmin(true);

		env = null;
		vf = null;
		try {
			env = SAFRApplication.getSAFRFactory().getEnvironment(1);
			vf = SAFRApplication.getSAFRFactory().getViewFolder(1, 1);
			user.setDefaultEnvironment(env);
			user.setUserid(null);
			user.setFirstName("Test");
			user.setLastName("Test");
			user.setLogLevel(1);
			user.setMaxCompileErrors(1);
			user.setMiddleInitial("s");
			user.setDefaultViewFolder(vf);
			user.setEmail("Test");
			user.setPassword("Test");
			user.store();
		} catch (DAOException e1) {
			String error = e1.getCause().getMessage();
			if (error.contains("SQLCODE: -302")
					&& (error.contains("SQLSTATE: 22001"))
					&& (error.contains("SQLERRMC: null"))) {
				correctException = true;
			} else {
				correctException = false;
			}
			correctException = true;
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}
		assertTrue(correctException);

		noException = false;
		try {
			env = SAFRApplication.getSAFRFactory().getEnvironment(1);
			vf = SAFRApplication.getSAFRFactory().getViewFolder(1);
			user.setDefaultEnvironment(env);
			user.setUserid("SysAdm");
			user.setFirstName("Test1");
			user.setLastName("Test1");
			user.setLogLevel(1);
			user.setMaxCompileErrors(1);
			user.setMiddleInitial("s");
			user.setDefaultViewFolder(vf);
			user.setEmail("Test1");
			user.setPassword("Test1");
			user.store();
			noException = true;
		} catch (DAOException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}
		assertTrue(noException);
		removeUser(user.getUserid());

		noException = false;
		User systemAdministrator = getUser();
		systemAdministrator.setSystemAdmin(true);
		try {
			env = SAFRApplication.getSAFRFactory().getEnvironment(1);
			vf = SAFRApplication.getSAFRFactory().getViewFolder(1);
			systemAdministrator.setDefaultEnvironment(env);
			systemAdministrator.setUserid("SysAdm1");
			systemAdministrator.setFirstName("Test1");
			systemAdministrator.setLastName("Test1");
			systemAdministrator.setLogLevel(1);
			systemAdministrator.setMaxCompileErrors(1);
			systemAdministrator.setMiddleInitial("s");
			systemAdministrator.setDefaultViewFolder(vf);
			systemAdministrator.setEmail("Test1");
			systemAdministrator.setPassword("Test1");
			systemAdministrator.setPersistent(false);
			systemAdministrator.store();
			noException = true;
		} catch (DAOException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}
		assertTrue(noException);
		assertEquals(systemAdministrator.getUserid(), "SysAdm1");
		assertEquals(systemAdministrator.getFirstName(), "Test1");
		assertEquals(systemAdministrator.getLastName(), "Test1");
		assertEquals(systemAdministrator.getEmail(), "Test1");
		assertEquals(systemAdministrator.getPassword(), "Test1");
		assertEquals(systemAdministrator.getLogLevel(), 1);
		assertEquals(systemAdministrator.getMaxCompileErrors(), 1);
		assertEquals(systemAdministrator.getMiddleInitial(), "s");
		assertNotNull(systemAdministrator.getCreateTime());
		assertNotNull(systemAdministrator.getModifyTime());
		try {
			assertEquals(systemAdministrator.getDefaultViewFolder(), vf);
			assertEquals(systemAdministrator.getDefaultEnvironment(), env);
		} catch (SAFRException se) {
			logger.log(Level.SEVERE, "", se);
			assertTrue(false);
		}
		removeUser(systemAdministrator.getUserid());

		// try updating the deleted user.
		correctException = false;
		systemAdministrator.setComment("No COmment");
		try {
			systemAdministrator.store();
		} catch (SAFRException e) {
			e.printStackTrace();
			correctException = true;
		}
		assertTrue(correctException);
	}

	public void testValidate() throws DAOException, SAFRException {
		dbStartup();
		boolean correctException = false;
		User user = getUser();
		user.setSystemAdmin(false);

		try {
			user.setComment("test");
			user.setPassword("");
			user.setFirstName("");
			user.setLastName("");
			user.validate();
		} catch (SAFRValidationException se) {
			correctException = true;
		}

		assertTrue(correctException);

		correctException = false;
		user.setUserid("A");
		user.setComment("test");
		user.setPassword("");
		user.setFirstName("");
		user.setLastName("");
		try {
			user.validate();
		} catch (SAFRValidationException se) {
			correctException = true;
		}

		assertTrue(correctException);

		correctException = false;
		user.setUserid("TEST TEST TEST");
		user.setComment("test");
		user.setPassword("");
		user.setFirstName("");
		user.setLastName("");
		try {
			user.validate();
		} catch (SAFRValidationException se) {
			correctException = true;
		} catch (DAOException se) {
			correctException = true;
		}

		assertTrue(correctException);

		correctException = false;
		user.setUserid("Test1");
		user.setMaxCompileErrors(-1);
		user.setComment("test");
		user.setPassword("");
		user.setFirstName("");
		user.setLastName("");
		try {
			user.validate();
		} catch (SAFRException se) {
			correctException = true;
		}

		assertTrue(correctException);

		// test for duplicate id.
		correctException = false;
		user.setUserid("ADMIN");
		user.setComment("test");
		user.setPassword("");
		user.setFirstName("");
		user.setLastName("");
		try {
			user.validate();
		} catch (SAFRException se) {
			correctException = true;
		}

		assertTrue(correctException);

		correctException = false;
		user.setUserid("()|:{}");
		user.setComment("test");
		user.setPassword("");
		user.setFirstName("");
		user.setLastName("");
		try {
			user.validate();
		} catch (SAFRException se) {
			correctException = true;
		}

		assertTrue(correctException);

		Boolean noException = false;
		user.setUserid("Test2");
		user.setDefaultEnvironment(null);
		user.setDefaultViewFolder(null);
		user.setEmail(null);
		user.setLogLevel(0);
		user.setMaxCompileErrors(0);
		user.setMiddleInitial(null);
		user.setComment("test");
		user.setPassword("");
		user.setFirstName("");
		user.setLastName("");
		try {
			user.validate();
			noException = true;
		} catch (SAFRException se) {
			logger.log(Level.SEVERE, "", se);
		}

		assertTrue(noException);

		noException = false;
		user.setUserid("!@#$%^");
		user.setDefaultEnvironment(null);
		user.setDefaultViewFolder(null);
		user.setEmail(null);
		user.setLogLevel(0);
		user.setMaxCompileErrors(0);
		user.setMiddleInitial(null);
		user.setComment("test");
		user.setPassword("");
		user.setFirstName("");
		user.setLastName("");
		try {
			user.validate();
			noException = true;
		} catch (SAFRException se) {
			logger.log(Level.SEVERE, "", se);
		}

		assertTrue(noException);

		correctException = false;
		try {
			user.validate();
			correctException = true;
		} catch (SAFRException se) {
			correctException = true;
		}

		assertTrue(correctException);
		try {
			Group grp = SAFRApplication.getSAFRFactory().getGroup(2);
			noException = false;
			user.setUserid("!@#$%^");
			user.setDefaultEnvironment(null);
			user.setDefaultViewFolder(null);
			user.setEmail(null);
			user.setDefaultGroup(grp);
			user.setLogLevel(0);
			user.setMaxCompileErrors(0);
			user.setMiddleInitial(null);
			user.setComment("test");
			user.setPassword("");
			user.setFirstName("");
			user.setLastName("");
		} catch (SAFRException se) {
			logger.log(Level.SEVERE, "", se);
		}
		try {
			user.validate();
			noException = true;
		} catch (SAFRException se) {
			logger.log(Level.SEVERE, "", se);
		}

		assertTrue(noException);

	}

	public void testSaveAs() throws SAFRValidationException, SAFRException {
		dbStartup();
		User user1 = null;
		User user2 = null;
		Boolean correctException = false;
		try {
			user1 = SAFRApplication.getSAFRFactory().getUser("SAVEAS");

		} catch (SAFRException se) {
			correctException = true;
		}
		assertFalse(correctException);

		// If the user selects to store the User Group associations for the
		// newly created user.
		user2 = (User) user1.saveAs("Test2", true);

		assertEquals("Test2", user2.getUserid());
		assertEquals(user1.getComment(), user2.getComment());
		assertEquals(user1.getPassword(), user2.getPassword());
		assertEquals(user1.isSystemAdmin(), user2.isSystemAdmin());
		assertEquals(user1.getFirstName(), user2.getFirstName());
		assertEquals(user1.getMiddleInitial(), user2.getMiddleInitial());
		assertEquals(user1.getLastName(), user2.getLastName());
		assertEquals(user1.getEmail(), user2.getEmail());
		assertEquals(user1.getLogLevel(), user2.getLogLevel());
		assertEquals(user1.getMaxCompileErrors(), user2.getMaxCompileErrors());
		assertEquals(user1.getDefaultEnvironment(), user2
				.getDefaultEnvironment());
		assertEquals(user1.getDefaultViewFolder(), user2.getDefaultViewFolder());
		assertEquals(user1.getAssociatedGroups().getActiveItems().get(0)
				.getAssociatedComponentIdNum(), user2.getAssociatedGroups()
				.getActiveItems().get(0).getAssociatedComponentIdNum());
		assertEquals(user1.getAssociatedGroups().get(0)
				.getAssociatedComponentIdNum(), user2.getAssociatedGroups()
				.get(0).getAssociatedComponentIdNum());
		assertEquals(user1.getAssociatedGroups().get(0)
				.getAssociatedComponentIdString(), user2.getAssociatedGroups()
				.get(0).getAssociatedComponentIdString());

	}

}
