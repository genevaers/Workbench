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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.EnvRole;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.associations.GroupComponentAssociation;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.model.associations.GroupUserAssociation;

public class TestGroup extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestGroup");

	static String TABLE_NAME = "GROUP";
	static String COL_ID = "GROUPID";
	static String DEP_TABLE_LIST[] = { "SECUSER", "SECENVIRON",
			"SECPHYFILE", "SECLOGFILE", "SECLOGREC",
			"SECEXIT", "SECVIEWFOLDER", "GROUP" };

	TestDataLayerHelper helper = new TestDataLayerHelper();
    List<Integer> delIds = new ArrayList<Integer>();

	public void setUp() {
	}

	public void dbStartup() {
		try {
			helper.initDataLayer();
		} catch (DAOException e) {
			assertFalse(true);
		}
	}

	public void removeGroup(Integer id) {

		try {
			List<String> idnames = new ArrayList<String>();
			idnames.add(COL_ID);

			PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder
					.getDAOFactory();
			ConnectionParameters params = fact.getConnectionParameters();
			SQLGenerator generator = new SQLGenerator();

			for (String tableName : DEP_TABLE_LIST) {
				try {
					String statement = generator.getDeleteStatement(params
							.getSchema(), tableName, idnames);
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
			logger.log(Level.SEVERE, "", e);
		}
	}

	public void tearDown() {
        try {
            for (Integer i : delIds) {
                try {
                    removeGroup(i);
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

	public void testGetSetComment() {
		Group grp = SAFRApplication.getSAFRFactory().createGroup();
		assertEquals(grp.getComment(), null);

		grp.setComment("testComment1");
		assertEquals(grp.getComment(), "testComment1");

		grp.setComment("testComment2");
		assertEquals(grp.getComment(), "testComment2");

		grp.setComment("");
		assertEquals(grp.getComment(), "");

		grp.setComment(null);
		assertEquals(grp.getComment(), null);
	}

	public void testGetCreateBy() {
		dbStartup();
		Group grp = null;
		try {
			grp = SAFRApplication.getSAFRFactory().getGroup(72);
		} catch (SAFRException e1) {
			e1.printStackTrace();
			assertFalse(true);
		}
		PGDAOFactory fact;
		Connection con = null;
		PreparedStatement pst;
		String statement = "";
		try {
			fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
			ConnectionParameters params = fact.getConnectionParameters();
			statement = "select CREATEDUSERID FROM " + params.getSchema()
					+ ".GROUP  WHERE GROUPID=72";
			con = fact.getConnection();

		} catch (DAOException e1) {
			e1.printStackTrace();
			assertFalse(true);
		}

		try {
			pst = con.prepareStatement(statement);
			ResultSet rs = pst.executeQuery();
			String createdBy = "";
			if (rs.next()) {
				createdBy = DataUtilities.trimString(rs
						.getString("CREATEDUSERID"));
			}
			assertEquals(grp.getCreateBy(), createdBy);
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
		}

	}

	public void testGetSetCreateTime() {
		dbStartup();
		Group grp = null;
		try {
			grp = SAFRApplication.getSAFRFactory().getGroup(72);
		} catch (SAFRException e1) {
			e1.printStackTrace();
			assertFalse(true);
		}
		PGDAOFactory fact;
		Connection con = null;
		PreparedStatement pst;
		String statement = "";
		try {
			fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
			ConnectionParameters params = fact.getConnectionParameters();
			statement = "select CREATEDTIMESTAMP FROM " + params.getSchema()
					+ ".GROUP WHERE GROUPID=72";
			con = fact.getConnection();

		} catch (DAOException e1) {
			e1.printStackTrace();
			assertFalse(true);
		}

		try {
			pst = con.prepareStatement(statement);
			ResultSet rs = pst.executeQuery();
			java.sql.Date createdTime = null;
			if (rs.next()) {
				createdTime = rs.getDate("CREATEDTIMESTAMP");
			}
			assertEquals(grp.getCreateTime(), createdTime);
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
		}

	}

	public void testGetSetModifyBy() {
		dbStartup();
		Group grp = null;
		try {
			grp = SAFRApplication.getSAFRFactory().getGroup(72);
		} catch (SAFRException e1) {
			e1.printStackTrace();
			assertFalse(true);
		}
		PGDAOFactory fact;
		Connection con = null;
		PreparedStatement pst;
		String statement = "";
		try {
			fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
			ConnectionParameters params = fact.getConnectionParameters();
			statement = "select LASTMODUSERID FROM " + params.getSchema()
					+ ".GROUP  WHERE groupid=72";
			con = fact.getConnection();

		} catch (DAOException e1) {
			e1.printStackTrace();
			assertFalse(true);
		}

		try {
			pst = con.prepareStatement(statement);
			ResultSet rs = pst.executeQuery();
			String modifyBy = "";
			if (rs.next()) {
				modifyBy = DataUtilities.trimString(rs
						.getString("LASTMODUSERID"));
			}
			assertEquals(grp.getModifyBy(), modifyBy);
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}

	public void testGetSetModifyTime() {
		dbStartup();
		Group grp = null;
		try {
			grp = SAFRApplication.getSAFRFactory().getGroup(72);
		} catch (SAFRException e1) {
			e1.printStackTrace();
			assertFalse(true);
		}
		PGDAOFactory fact;
		Connection con = null;
		PreparedStatement pst;
		String statement = "";
		try {
			fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
			ConnectionParameters params = fact.getConnectionParameters();
			statement = "select LASTMODTIMESTAMP FROM " + params.getSchema()
					+ ".GROUP WHERE GROUPID=72";
			con = fact.getConnection();

		} catch (DAOException e1) {
			e1.printStackTrace();
			assertFalse(true);
		}

		try {
			pst = con.prepareStatement(statement);
			ResultSet rs = pst.executeQuery();
			java.sql.Date modifyTime = null;
			if (rs.next()) {
				modifyTime = rs.getDate("LASTMODTIMESTAMP");
			}
			assertEquals(grp.getCreateTime(), modifyTime);
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
		}

	}

	public void testGetSetName() {
		Group grp = SAFRApplication.getSAFRFactory().createGroup();
		assertEquals(grp.getName(), null);

		grp.setName("testName1");
		assertEquals(grp.getName(), "testName1");

		grp.setName("testName2");
		assertEquals(grp.getName(), "testName2");

		grp.setName("");
		assertEquals(grp.getName(), "");

		grp.setName(null);
		assertEquals(grp.getName(), null);
	}

	public void testStore() throws DAOException, SAFRException {
		dbStartup();
		boolean noException = false;
		Group grp;
		grp = SAFRApplication.getSAFRFactory().createGroup();

		grp.setName(null);
		grp.setComment("");
		try {
			grp.store();
			noException = true;
		} catch (DAOException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		} catch (Throwable e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}

		assertTrue(noException);

		noException = false;
		grp.setName("Test1");
		grp.setComment("Test1 comment");
		try {
			grp.store();
			noException = true;
		} catch (NullPointerException ne) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", ne);
		} catch (DAOException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		} catch (Throwable e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}
		delIds.add(grp.getId());
		PGDAOFactory fact = null;
		try {
			fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
		} catch (DAOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue(noException);
		assertEquals(grp.getName(), "Test1");
		assertEquals(grp.getComment(), "Test1 comment");
		assertEquals(grp.getCreateBy().trim(), fact.getSAFRLogin().getUserId());
		assertEquals(grp.getModifyBy().trim(), fact.getSAFRLogin().getUserId());
		assertNotNull(grp.getCreateTime());
		assertNotNull(grp.getModifyTime());

		// Test for Exception when update fails due to non
		// availability of component in DB

		// create the group
		grp = SAFRApplication.getSAFRFactory().createGroup();
		grp.setName("NoGroup");
		grp.setComment("comment");
		grp.store();
		// delete the group
		SAFRApplication.getSAFRFactory().removeGroup(grp.getId());

		// try updating the deleted group
		grp.setName("Nogroup1");
		try {
			grp.store();
			assertFalse(true);
		} catch (SAFRException e) {
			e.printStackTrace();
			assertFalse(false);

		}

	}

	public void testValidate() {
		boolean correctException = false;
		dbStartup();
		Group grp = SAFRApplication.getSAFRFactory().createGroup();

		try {
			grp.validate();
		} catch (SAFRException se) {
			correctException = true;
		} catch (Exception e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}

		assertTrue(correctException);

		correctException = false;
		grp.setName(null);
		grp.setComment("");
		try {
			grp.validate();
		} catch (SAFRException se) {
			correctException = true;
		} catch (Exception e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}

		assertTrue(correctException);
		correctException = false;
		grp.setName("123nfj");
		grp.setComment("");
		try {
			grp.validate();
		} catch (SAFRException se) {
			correctException = true;
		} catch (Exception e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}

		assertTrue(correctException);

		correctException = false;
		grp.setName("$%^^&*");
		grp.setComment("");
		try {
			grp.validate();
		} catch (SAFRException se) {
			correctException = true;
		} catch (Exception e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}

		assertTrue(correctException);

		// test for duplicate group
		correctException = false;
		grp.setName("Administrators");
		grp.setComment("");
		try {
			grp.validate();
		} catch (SAFRException se) {
			correctException = true;
		} catch (Exception e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}

		assertTrue(correctException);

		Boolean noException = false;
		grp.setName("Test1234");
		grp.setComment("Test1");
		try {
			grp.validate();
			noException = true;
		} catch (SAFRException se) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", se);
		} catch (Exception e2) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e2);
		}

		assertTrue(noException);
	}
	public void testGetPFAssociation() throws SAFRException{
		dbStartup();
		ComponentType componentType = ComponentType.PhysicalFile;
		Group group = SAFRApplication.getSAFRFactory().getGroup(29);
		SAFRList<GroupComponentAssociation> pf_list = group.getComponentRights(componentType, 1);
		
		assertEquals(pf_list.size(), 0);
	}
	
	public void testGetGroupUserAssociations() throws SAFRException{
		dbStartup();
		Group group = SAFRApplication.getSAFRFactory().getGroup(79);
		String user = group.getGroupUserAssociations().get(0).getAssociatedComponentIdString();
		
		/* test stored data */
		assertEquals(user,"QUINNO2");
	}
	public void testStoreAssociatedUsers() throws DAOException, SAFRException{
		dbStartup();
		
		Group group = SAFRApplication.getSAFRFactory().getGroup(79);
		
		SAFRList<GroupUserAssociation> groupAssociationList = null;
		GroupUserAssociation grp_user = new GroupUserAssociation(group, "", "QUINNO");
	
		groupAssociationList = group.getGroupUserAssociations();
 
        groupAssociationList.add(grp_user);
		
		group.storeAssociatedUsers();
		
		String user_added = group.getGroupUserAssociations().get(1).getAssociatedComponentIdString();
		
		/* mark delete on group user association */
		SAFRPersistence Delete_user = SAFRPersistence.DELETED;
		group.getGroupUserAssociations().get(1).setPersistence(Delete_user);
		
		/* remove user association */
		group.storeAssociatedUsers();
		
		/* test stored data */
		assertEquals(user_added,"QUINNO");
		
	}
	public void testStoreAssociatedEnvironments() {

		dbStartup();
		Group grp = SAFRApplication.getSAFRFactory().createGroup();
		Integer groupId = 0;
		SAFRList<GroupEnvironmentAssociation> groupEnvironmentAssociationList = null;
		boolean noException = false;
		try {
			grp.setName("Test1289113e11");
			grp.setComment("Test12");
			grp.store();
			delIds.add(grp.getId());
			noException = true;
		} catch (SAFRException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		}
		assertTrue(noException);

		GroupEnvironmentAssociation grpEnvAsso1 = new GroupEnvironmentAssociation(
				grp, "JUnitTestEnv", 46);
		grpEnvAsso1.setEnvRole(EnvRole.ADMIN);
		GroupEnvironmentAssociation grpEnvAsso2 = new GroupEnvironmentAssociation(
				grp, "JUnitTestEnv", 43);
        grpEnvAsso2.setEnvRole(EnvRole.ADMIN);
		grpEnvAsso2.addPermission(Permissions.CreatePhysicalFile);
		grpEnvAsso2.addPermission(Permissions.CreateLogicalFile);
		grpEnvAsso2.addPermission(Permissions.CreateLogicalRecord);
		grpEnvAsso2.addPermission(Permissions.CreateUserExitRoutine);
		grpEnvAsso2.addPermission(Permissions.CreateViewFolder);
		grpEnvAsso2.addPermission(Permissions.CreateView);
		grpEnvAsso2.addPermission(Permissions.CreateLookupPath);
		grpEnvAsso2.addPermission(Permissions.MigrateIn);

		try {
			groupEnvironmentAssociationList = grp.getAssociatedEnvironments();
			if (groupEnvironmentAssociationList.isEmpty()) {
				assertTrue(true);
			}
			groupEnvironmentAssociationList.add(grpEnvAsso1);
			groupEnvironmentAssociationList.add(grpEnvAsso2);
			grp.storeAssociatedEnvironments();
			groupId = grp.getId();
			noException = true;
		} catch (DAOException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(noException);

		// test if the association are stored correctly or not.
		SAFRList<GroupEnvironmentAssociation> groupEnvironmentAssociationList1 = null;
		Group grp1 = null;
		noException = false;
		boolean correctSize = false;
		boolean correctRights = false;
		boolean correctAllRights = false;
		try {
			grp1 = SAFRApplication.getSAFRFactory().getGroup(groupId);
			groupEnvironmentAssociationList1 = grp1.getAssociatedEnvironments();
			if (groupEnvironmentAssociationList1.size() == 2) {
				correctSize = true;
				 
				GroupEnvironmentAssociation gEAsso1 = groupEnvironmentAssociationList1
						.get(0);
				GroupEnvironmentAssociation gEAsso2 = groupEnvironmentAssociationList1
						.get(1);
                if (!gEAsso1.getAssociatedComponentIdNum().equals(46)) {
                    GroupEnvironmentAssociation tmp;
                    tmp = gEAsso1;
                    gEAsso1 = gEAsso2;
                    gEAsso2 = tmp;
                }
				if (gEAsso1.getEnvRole().equals(EnvRole.ADMIN) || 
				    gEAsso2.getEnvRole().equals(EnvRole.ADMIN)) {
					correctRights = true;
				}

				
				if (gEAsso2.canCreatePhysicalFile()
						&& gEAsso2.canCreateLogicalFile()
						&& gEAsso2.canCreateLogicalRecord()
						&& gEAsso2.canCreateView()
						&& gEAsso2.canCreateLookupPath()
						&& gEAsso2.canCreateUserExitRoutine()
						&& gEAsso2.canCreateViewFolder()
						&& gEAsso2.canMigrateIn()) {      
					correctAllRights = true;
				}
			}
			noException = true;
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(noException);
		assertTrue(correctRights);
		assertTrue(correctAllRights);
		assertTrue(correctSize);

		// check for modified environments (store and retrieve).
		Group grp3 = null;
		try {
			grp3 = SAFRApplication.getSAFRFactory().getGroup(groupId);
		} catch (SAFRException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		}
		Integer groupId3 = 69;
		boolean noException3 = false;

		GroupEnvironmentAssociation grpEnvAsso31 = new GroupEnvironmentAssociation(
				grp3, "JUnitTestEnv", 46);
		grpEnvAsso31.setEnvRole(EnvRole.ADMIN);

		GroupEnvironmentAssociation grpEnvAsso32 = new GroupEnvironmentAssociation(
				grp3, "JUnitTestEnv", 43);
		grpEnvAsso32.addPermission(Permissions.CreatePhysicalFile);

		SAFRList<GroupEnvironmentAssociation> groupEnvironmentAssociationList3 = null;
		try {
			groupEnvironmentAssociationList3 = grp3.getAssociatedEnvironments();

			groupEnvironmentAssociationList3.add(grpEnvAsso31);
			groupEnvironmentAssociationList3.add(grpEnvAsso32);

			grp3.storeAssociatedEnvironments();
			groupId3 = grp3.getId();
			noException3 = true;
		} catch (DAOException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(noException3);

		// test if the association are stored correctly or not.
		SAFRList<GroupEnvironmentAssociation> groupEnvironmentAssociationList31 = null;
		Group grp31 = null;
		noException3 = false;
		correctSize = false;
		correctRights = false;
		correctAllRights = false;
		try {
			grp31 = SAFRApplication.getSAFRFactory().getGroup(groupId3);
			groupEnvironmentAssociationList31 = grp31
					.getAssociatedEnvironments();
			if (groupEnvironmentAssociationList31.size() == 2) {
				correctSize = true;
				GroupEnvironmentAssociation gEAsso1 = groupEnvironmentAssociationList1
						.get(0);
				GroupEnvironmentAssociation gEAsso2 = groupEnvironmentAssociationList1
						.get(1);
                if (!gEAsso1.getAssociatedComponentIdNum().equals(46)) {
                    GroupEnvironmentAssociation tmp;
                    tmp = gEAsso1;
                    gEAsso1 = gEAsso2;
                    gEAsso2 = tmp;
                }
				if (gEAsso1.getEnvRole().equals(EnvRole.ADMIN) || 
				    gEAsso2.getEnvRole().equals(EnvRole.ADMIN)) {
					correctRights = true;
				}

				if (gEAsso2.canCreatePhysicalFile()
						&& gEAsso2.canCreateLogicalFile()
						&& gEAsso2.canCreateLogicalRecord()
						&& gEAsso2.canCreateView()
						&& gEAsso2.canCreateLookupPath()
						&& gEAsso2.canCreateUserExitRoutine()
						&& gEAsso2.canCreateViewFolder()
						&& gEAsso2.canMigrateIn()) {
					correctAllRights = true;
				}
			}
			noException3 = true;
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(noException3);
		assertTrue(correctRights);
		assertTrue(correctAllRights);
		assertTrue(correctSize);

	}
	
	public void testassignComponenetFullRights() throws SAFRException{
		dbStartup();
        helper.setUser("NOPERM");		
		Group group = SAFRApplication.getSAFRFactory().getGroup(79);
		PhysicalFile pf = SAFRApplication.getSAFRFactory().getPhysicalFile(8469);
		
		ComponentType componentType = ComponentType.PhysicalFile;
		group.assignComponentFullRights(pf, componentType);
		
		SAFRList<GroupComponentAssociation> group_pf_edit = group.getComponentRights(componentType, 1);
		

		String group_pf = group_pf_edit.get(0).getAssociatingComponentName();
		String pf_name = group_pf_edit.get(0).getAssociatedComponentName();
		EditRights pf_right = group_pf_edit.get(0).getRights();
		
		/* remove edit rights */
		
		group_pf_edit.get(0).setPersistence(SAFRPersistence.DELETED);
		
		group.storeComponentRights();
		
		/* Test result */
		assertEquals(group_pf,"Group");
		assertEquals(pf_name,"Test_Group_EditRight");
		assertEquals(pf_right,EditRights.ReadModifyDelete);

		
	}
	public void testAssignComponentEditRights() throws SAFRException{
		dbStartup();
		Group group = SAFRApplication.getSAFRFactory().getGroup(79);
		 PhysicalFile pf = SAFRApplication.getSAFRFactory().getPhysicalFile(8469);
		 	
		ComponentType componentType = ComponentType.PhysicalFile;
		EditRights editRights = EditRights.ReadModify;
		
		group.assignComponentEditRights(pf, componentType, editRights);
		
		SAFRList<GroupComponentAssociation> group_pf_edit = group.getComponentRights(componentType, 1);
		
		String group_pf = group_pf_edit.get(0).getAssociatingComponentName();
		String pf_name = group_pf_edit.get(0).getAssociatedComponentName();
		EditRights pf_right = group_pf_edit.get(0).getRights();
		
	
		/* remove edit rights */
		
		group_pf_edit.get(0).setPersistence(SAFRPersistence.DELETED);
		
		group.storeComponentRights();
		
		/* Test result */
		assertEquals(group_pf,"Group");
		assertEquals(pf_name,"Test_Group_EditRight");
		assertEquals(pf_right,EditRights.ReadModify);
		
		
	}

	public void testGetComponentRights() {

		dbStartup();
		Group grp = SAFRApplication.getSAFRFactory().createGroup();
		boolean noException = false;
		try {
			grp.setName("TestABC");
			grp.store();
			delIds.add(grp.getId());
			noException = true;
		} catch (SAFRException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		}
		assertTrue(noException);

		Integer environmentId = 1;
		ComponentType componentType = ComponentType.PhysicalFile;
		List<GroupComponentAssociation> groupComponentAssociationList = null;
		noException = false;
		try {
			groupComponentAssociationList = grp.getComponentRights(
					componentType, environmentId);
			noException = true;
		} catch (DAOException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		} catch (SAFRException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		}
		assertTrue(noException);
		assertTrue(groupComponentAssociationList.isEmpty());

		noException = false;
		Group grp1 = null;
		try {
			grp1 = SAFRApplication.getSAFRFactory().getGroup(72);
			noException = true;
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(noException);
		// for physical file
		environmentId = 1;
		componentType = ComponentType.PhysicalFile;
		groupComponentAssociationList = null;
		noException = false;
		try {
			groupComponentAssociationList = grp1.getComponentRights(
					componentType, environmentId);
			noException = true;
		} catch (DAOException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		} catch (SAFRException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		}
		assertTrue(noException);
		assertFalse(groupComponentAssociationList.isEmpty());
		assertTrue(groupComponentAssociationList.get(0).canDelete());

		// for logical file
		environmentId = 1;
		componentType = ComponentType.LogicalFile;
		groupComponentAssociationList = null;
		noException = false;
		try {
			groupComponentAssociationList = grp1.getComponentRights(
					componentType, environmentId);
			noException = true;
		} catch (DAOException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		} catch (SAFRException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		}
		assertTrue(noException);
		assertFalse(groupComponentAssociationList.isEmpty());
		assertTrue(groupComponentAssociationList.get(0).canDelete());

		// for logical record
		environmentId = 1;
		componentType = ComponentType.LogicalRecord;
		groupComponentAssociationList = null;
		noException = false;
		try {
			groupComponentAssociationList = grp1.getComponentRights(
					componentType, environmentId);
			noException = true;
		} catch (DAOException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		} catch (SAFRException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		}
		assertTrue(noException);
		assertFalse(groupComponentAssociationList.isEmpty());
		assertTrue(groupComponentAssociationList.get(0).canDelete());

		// ** check for different environment id
		// for physical file
		environmentId = 23;
		componentType = ComponentType.PhysicalFile;
		groupComponentAssociationList = null;
		noException = false;
		try {
			groupComponentAssociationList = grp1.getComponentRights(
					componentType, environmentId);
			noException = true;
		} catch (DAOException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		} catch (SAFRException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		}
		assertTrue(noException);
		assertTrue(groupComponentAssociationList.isEmpty());
	}

	public void testStoreComponentRights() {
		dbStartup();
		Group grp = SAFRApplication.getSAFRFactory().createGroup();
		SAFRList<GroupComponentAssociation> groupComponentAssociationList = null;
		boolean noException = false;
		try {
			grp.setName("Test1289113e11ab");
			grp.setComment("Test12");
			grp.store();
			delIds.add(grp.getId());
			noException = true;
		} catch (SAFRException e1) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e1);
		}
		assertTrue(noException);

		GroupComponentAssociation grpCmpAsso1 = new GroupComponentAssociation(
				grp, "test_junit1", 8395, 1);
		grpCmpAsso1.setRights(EditRights.ReadModifyDelete);
		GroupComponentAssociation grpCmpAsso2 = new GroupComponentAssociation(
				grp, "test_junit2", 8396, 1);
		grpCmpAsso2.setRights(EditRights.ReadModify);

		GroupComponentAssociation grpCmpAsso3 = new GroupComponentAssociation(
				grp, "test_junit3", 8397, 1);
		grpCmpAsso3.setRights(EditRights.Read);

		noException = false;
		try {
			groupComponentAssociationList = grp.getComponentRights(
					ComponentType.PhysicalFile, 1);
			if (groupComponentAssociationList.isEmpty()) {
				assertTrue(true);
			}
			groupComponentAssociationList.add(grpCmpAsso1);
			groupComponentAssociationList.add(grpCmpAsso2);
			groupComponentAssociationList.add(grpCmpAsso3);
			grp.storeComponentRights();
			noException = true;
		} catch (DAOException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(noException);

		// test if the association are stored correctly or not.
		SAFRList<GroupComponentAssociation> groupComponentAssociationList1 = null;
		noException = false;
		boolean correctSize = false;

		try {

			groupComponentAssociationList1 = grp.getComponentRights(
					ComponentType.PhysicalFile, 1);
			if (groupComponentAssociationList1.size() == 3) {
				correctSize = true;
				GroupComponentAssociation gCAsso1 = groupComponentAssociationList1
						.get(0);
				if ((gCAsso1.getAssociatedComponentName() == "test_junit1")
						&& !gCAsso1.canDelete()) {
					assertTrue(false);
				}
				GroupComponentAssociation gCAsso2 = groupComponentAssociationList1
						.get(1);
				if ((gCAsso2.getAssociatedComponentName() == "test_junit2")
						&& !gCAsso1.canModify()) {
					assertTrue(false);
				}
				GroupComponentAssociation gCAsso3 = groupComponentAssociationList1
						.get(2);
				if ((gCAsso3.getAssociatedComponentName() == "test_junit3")
						&& !gCAsso1.canRead()) {
					assertTrue(false);
				}

			}
			noException = true;
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(noException);
		assertTrue(correctSize);

		// check for modified Components (store and retrieve).
		Group grp2 = null;
		grp2 = SAFRApplication.getSAFRFactory().createGroup();
		grp2.setName("Test_jay345");
		Integer groupId2 = 0;
		SAFRList<GroupComponentAssociation> groupComponentAssociationList2 = null;

		grpCmpAsso1 = new GroupComponentAssociation(grp2, "test", 3, 1);
		grpCmpAsso1.setRights(EditRights.ReadModifyDelete);
		grpCmpAsso2 = new GroupComponentAssociation(grp2, "test2", 4, 1);
		grpCmpAsso2.setRights(EditRights.ReadModify);

		grpCmpAsso3 = new GroupComponentAssociation(grp2, "test3", 5, 1);
		grpCmpAsso3.setRights(EditRights.Read);

		noException = false;
		try {
			grp2.store();
			delIds.add(grp2.getId());
			groupComponentAssociationList2 = grp2.getComponentRights(
					ComponentType.LogicalRecord, 1);
			if (groupComponentAssociationList2.isEmpty()) {
				assertTrue(true);
			}
			groupComponentAssociationList2.add(grpCmpAsso1);
			groupComponentAssociationList2.add(grpCmpAsso2);
			groupComponentAssociationList2.add(grpCmpAsso3);
			grp2.storeComponentRights();
			groupId2 = grp2.getId();
			noException = true;
		} catch (DAOException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(noException);

		// test if the association are stored correctly or not.
		SAFRList<GroupComponentAssociation> groupComponentAssociationList3 = null;
		Group grp3 = null;
		noException = false;
		correctSize = false;

		try {

			grp3 = SAFRApplication.getSAFRFactory().getGroup(groupId2);
			groupComponentAssociationList3 = grp3.getComponentRights(
					ComponentType.LogicalRecord, 1);
			if (groupComponentAssociationList3.size() == 3) {
				correctSize = true;
				GroupComponentAssociation gCAsso1 = groupComponentAssociationList3
						.get(0);
				if ((gCAsso1.getAssociatedComponentName() == "test")
						&& !gCAsso1.canDelete()) {
					assertTrue(false);
				}
				GroupComponentAssociation gCAsso2 = groupComponentAssociationList3
						.get(1);
				if ((gCAsso2.getAssociatedComponentName() == "test2")
						&& !gCAsso1.canModify()) {
					assertTrue(false);
				}
				GroupComponentAssociation gCAsso3 = groupComponentAssociationList3
						.get(2);
				if ((gCAsso3.getAssociatedComponentName() == "test3")
						&& !gCAsso1.canRead()) {
					assertTrue(false);
				}

			}
			noException = true;
		} catch (SAFRException e) {
			assertTrue(false);
			logger.log(Level.SEVERE, "", e);
		}
		assertTrue(noException);
		assertTrue(correctSize);

	}

	public void testSaveAs() throws SAFRException {
		dbStartup();
		Group grp1 = null;
		Group grp2 = null;
		Boolean correctException = false;
		try {
			grp1 = SAFRApplication.getSAFRFactory().getGroup(1);

		} catch (SAFRException se) {
			correctException = true;
		}
		assertFalse(correctException);

		// If the user selects to store the User Group associations for the
		// newly created user.
		try {
			grp2 = (Group) grp1.saveAs("GROUP_SAVEAS1");
			delIds.add(grp2.getId());
		} catch (SAFRValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAFRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Test for Associated Users.
		assertEquals("GROUP_SAVEAS1", grp2.getName());
		assertEquals(grp1.getComment(), grp2.getComment());
		assertEquals(grp1.getGroupUserAssociations().getActiveItems().get(0)
				.getAssociatedComponentName(), grp2.getGroupUserAssociations()
				.getActiveItems().get(0).getAssociatedComponentName());
		assertEquals(grp1.getGroupUserAssociations().getActiveItems().get(0)
				.getPersistence(), grp2.getGroupUserAssociations()
				.getActiveItems().get(0).getPersistence());
		assertEquals(grp1.getGroupUserAssociations().get(0)
				.getAssociatedComponentIdNum(), grp2.getGroupUserAssociations()
				.get(0).getAssociatedComponentIdNum());
		assertEquals(grp1.getGroupUserAssociations().get(0)
				.getAssociatedComponentIdString(), grp2
				.getGroupUserAssociations().get(0)
				.getAssociatedComponentIdString());
		// Test for Associated Environments
		assertEquals(grp1.getAssociatedEnvironments().get(0).getPersistence(),
				grp2.getAssociatedEnvironments().get(0).getPersistence());
		assertEquals(grp1.getAssociatedEnvironments().get(0)
				.getAssociatedComponentIdNum(), grp2
				.getAssociatedEnvironments().get(0)
				.getAssociatedComponentIdNum());
		assertEquals(grp1.getAssociatedEnvironments().get(0)
				.getAssociatedComponentName(), grp2.getAssociatedEnvironments()
				.get(0).getAssociatedComponentName());
		assertEquals(grp1.getAssociatedEnvironments().get(0)
				.getAssociatedComponentIdString(), grp2
				.getAssociatedEnvironments().get(0)
				.getAssociatedComponentIdString());

		// Test for Associated ComponentRights for the component Physical File
		assertEquals(grp1.getComponentAssociation(ComponentType.PhysicalFile,
				1, 1).getPersistence(), grp2.getComponentAssociation(
				ComponentType.PhysicalFile, 1, 1).getPersistence());
		assertEquals(grp1.getComponentAssociation(ComponentType.PhysicalFile,
				1, 1).getAssociatedComponentIdNum(), grp2
				.getComponentAssociation(ComponentType.PhysicalFile, 1, 1)
				.getAssociatedComponentIdNum());
		assertEquals(grp1.getComponentAssociation(ComponentType.PhysicalFile,
				1, 1).getAssociatedComponentName(), grp2
				.getComponentAssociation(ComponentType.PhysicalFile, 1, 1)
				.getAssociatedComponentName());
		assertEquals(grp1.getComponentAssociation(ComponentType.PhysicalFile,
				1, 1).getAssociatedComponentIdString(), grp2
				.getComponentAssociation(ComponentType.PhysicalFile, 1, 1)
				.getAssociatedComponentIdString());
		assertEquals(grp1.getComponentAssociation(ComponentType.PhysicalFile,
				1, 1).getRights(), grp2.getComponentAssociation(
				ComponentType.PhysicalFile, 1, 1).getRights());
		assertEquals(grp1.getComponentAssociation(ComponentType.PhysicalFile,
				1, 1).getEnvironmentId(), grp2.getComponentAssociation(
				ComponentType.PhysicalFile, 1, 1).getEnvironmentId());

		// Test for Associated ComponentRights for the component Logical File
		assertEquals(grp1.getComponentAssociation(ComponentType.LogicalFile, 1,
				1).getPersistence(), grp2.getComponentAssociation(
				ComponentType.LogicalFile, 1, 1).getPersistence());
		assertEquals(grp1.getComponentAssociation(ComponentType.LogicalFile, 1,
				1).getAssociatedComponentIdNum(), grp2.getComponentAssociation(
				ComponentType.LogicalFile, 1, 1).getAssociatedComponentIdNum());
		assertEquals(grp1.getComponentAssociation(ComponentType.LogicalFile, 1,
				1).getAssociatedComponentName(), grp2.getComponentAssociation(
				ComponentType.LogicalFile, 1, 1).getAssociatedComponentName());
		assertEquals(grp1.getComponentAssociation(ComponentType.LogicalFile, 1,
				1).getAssociatedComponentIdString(), grp2
				.getComponentAssociation(ComponentType.LogicalFile, 1, 1)
				.getAssociatedComponentIdString());
		assertEquals(grp1.getComponentAssociation(ComponentType.LogicalFile, 1,
				1).getRights(), grp2.getComponentAssociation(
				ComponentType.LogicalFile, 1, 1).getRights());
		assertEquals(grp1.getComponentAssociation(ComponentType.LogicalFile, 1,
				1).getEnvironmentId(), grp2.getComponentAssociation(
				ComponentType.LogicalFile, 1, 1).getEnvironmentId());

		// Test for Associated ComponentRights for the component LogicalRecord
		assertEquals(grp1.getComponentAssociation(ComponentType.LogicalRecord,
				1, 1).getPersistence(), grp2.getComponentAssociation(
				ComponentType.LogicalRecord, 1, 1).getPersistence());
		assertEquals(grp1.getComponentAssociation(ComponentType.LogicalRecord,
				1, 1).getAssociatedComponentIdNum(), grp2
				.getComponentAssociation(ComponentType.LogicalRecord, 1, 1)
				.getAssociatedComponentIdNum());
		assertEquals(grp1.getComponentAssociation(ComponentType.LogicalRecord,
				1, 1).getAssociatedComponentName(), grp2
				.getComponentAssociation(ComponentType.LogicalRecord, 1, 1)
				.getAssociatedComponentName());
		assertEquals(grp1.getComponentAssociation(ComponentType.LogicalRecord,
				1, 1).getAssociatedComponentIdString(), grp2
				.getComponentAssociation(ComponentType.LogicalRecord, 1, 1)
				.getAssociatedComponentIdString());
		assertEquals(grp1.getComponentAssociation(ComponentType.LogicalRecord,
				1, 1).getRights(), grp2.getComponentAssociation(
				ComponentType.LogicalRecord, 1, 1).getRights());
		assertEquals(grp1.getComponentAssociation(ComponentType.LogicalRecord,
				1, 1).getEnvironmentId(), grp2.getComponentAssociation(
				ComponentType.LogicalRecord, 1, 1).getEnvironmentId());

		// Test for Associated ComponentRights for the component UserExitRoutine
		assertEquals(grp1.getComponentAssociation(
				ComponentType.UserExitRoutine, 1, 1).getPersistence(), grp2
				.getComponentAssociation(ComponentType.UserExitRoutine, 1, 1)
				.getPersistence());
		assertEquals(grp1.getComponentAssociation(
				ComponentType.UserExitRoutine, 1, 1)
				.getAssociatedComponentIdNum(), grp2.getComponentAssociation(
				ComponentType.UserExitRoutine, 1, 1)
				.getAssociatedComponentIdNum());
		assertEquals(grp1.getComponentAssociation(
				ComponentType.UserExitRoutine, 1, 1)
				.getAssociatedComponentName(), grp2.getComponentAssociation(
				ComponentType.UserExitRoutine, 1, 1)
				.getAssociatedComponentName());
		assertEquals(grp1.getComponentAssociation(
				ComponentType.UserExitRoutine, 1, 1)
				.getAssociatedComponentIdString(), grp2
				.getComponentAssociation(ComponentType.UserExitRoutine, 1, 1)
				.getAssociatedComponentIdString());
		assertEquals(grp1.getComponentAssociation(
				ComponentType.UserExitRoutine, 1, 1).getRights(), grp2
				.getComponentAssociation(ComponentType.UserExitRoutine, 1, 1)
				.getRights());
		assertEquals(grp1.getComponentAssociation(
				ComponentType.UserExitRoutine, 1, 1).getEnvironmentId(), grp2
				.getComponentAssociation(ComponentType.UserExitRoutine, 1, 1)
				.getEnvironmentId());

		// Test for Associated ComponentRights for the component ViewFolder
		assertEquals(grp1.getComponentAssociation(ComponentType.ViewFolder, 1,
				1).getPersistence(), grp2.getComponentAssociation(
				ComponentType.ViewFolder, 1, 1).getPersistence());
		assertEquals(grp1.getComponentAssociation(ComponentType.ViewFolder, 1,
				1).getAssociatedComponentIdNum(), grp2.getComponentAssociation(
				ComponentType.ViewFolder, 1, 1).getAssociatedComponentIdNum());
		assertEquals(grp1.getComponentAssociation(ComponentType.ViewFolder, 1,
				1).getAssociatedComponentName(), grp2.getComponentAssociation(
				ComponentType.ViewFolder, 1, 1).getAssociatedComponentName());
		assertEquals(grp1.getComponentAssociation(ComponentType.ViewFolder, 1,
				1).getAssociatedComponentIdString(), grp2
				.getComponentAssociation(ComponentType.ViewFolder, 1, 1)
				.getAssociatedComponentIdString());
		assertEquals(grp1.getComponentAssociation(ComponentType.ViewFolder, 1,
				1).getRights(), grp2.getComponentAssociation(
				ComponentType.ViewFolder, 1, 1).getRights());
		assertEquals(grp1.getComponentAssociation(ComponentType.ViewFolder, 1,
				1).getEnvironmentId(), grp2.getComponentAssociation(
				ComponentType.ViewFolder, 1, 1).getEnvironmentId());

	}
}
