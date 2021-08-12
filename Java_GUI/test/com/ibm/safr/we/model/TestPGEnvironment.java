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
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactory;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.associations.GroupComponentAssociation;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;

public class TestPGEnvironment extends TestCase {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestEnvironment");

	static String TABLE_NAME = "ENVIRON";
	static String COL_ID = "ENVIRONID";

	static String DEP_TABLE_LIST[] = { "VIEWHEADERFOOTER",
			"SECVIEWFOLDER", "SECENVIRON", "VIEWCOLUMNSOURCE",
			"VIEWSORTKEY", "VIEWCOLUMN", "VIEWSOURCE",
			"LRLFASSOC", "VIEWLOGICDEPEND", "VIEW", "LRFIELDATTR",
			"LRFIELD", "LRINDEXFLD", "LRINDEX", "LOGREC",
			"LFPFASSOC", "LOGFILE", "PHYFILE",
			"VIEWFOLDER", "EXIT", "CONTROLREC",
			TABLE_NAME };

	TestDataLayerHelper helper = new TestDataLayerHelper();
    List<Integer> delIds = new ArrayList<Integer>();
    
    static boolean postgres = true;

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

	
	public void testIsDuplicate() throws SAFRException{
		dbStartup();
		Environment env = SAFRApplication.getSAFRFactory().createEnvironment();
		env.setName("Environment");
		assertTrue(env.isDuplicate());
		env.setName("testnameqq");
		assertFalse(env.isDuplicate());
	}
	
	public void removeEnvironment(Integer id) {

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
					String statement = generator.getDeleteStatement(params
							.getSchema(), tableName, idnames);
					PreparedStatement pst = fact.getConnection()
							.prepareStatement(statement);
					pst.setInt(1, id);
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
            for (Integer i : delIds) {
                try {
                    removeEnvironment(i);
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

	public void testStoreAssociatedGroups() throws SAFRException{
		dbStartup();
		Environment env = SAFRApplication.getSAFRFactory().getEnvironment(107);
		Group group = SAFRApplication.getSAFRFactory().getGroup(80);
	
		GroupEnvironmentAssociation grpAssociation = null;
		SAFRList<GroupEnvironmentAssociation> list_current = env.getAssociatedGroups();
		grpAssociation = new GroupEnvironmentAssociation(group, env.getName(), env.getId());
		grpAssociation.setEnvRole(EnvRole.ADMIN);
		list_current.add(grpAssociation);

		
		env.storeAssociatedGroups();
		
		/* setup assert testing string */
		
		SAFRList<GroupEnvironmentAssociation> list_store = env.getAssociatedGroups();
		String grp_Assoc_item1 = list_store.get(0).getAssociatingComponentName();
		String grp_Assoc_item2 = list_store.get(1).getAssociatingComponentName();
		
		/* remove associated group id 80 */

		list_store.get(1).setPersistence(SAFRPersistence.DELETED);
		env.storeAssociatedGroups();
		
		/*test result*/	
		
		assertEquals("Administrators",grp_Assoc_item1);
		assertEquals("test_env_group",grp_Assoc_item2);
		
	}
	
	public void testStoreComponentRights() throws SAFRException{
		TestDataLayerHelper.setPostgres(postgres);
		helper.initDataLayer(107);
		Environment env = SAFRApplication.getSAFRFactory().getEnvironment(107);
		Group group = SAFRApplication.getSAFRFactory().getGroup(80);
	
		GroupEnvironmentAssociation grpAssociation = null;
		SAFRList<GroupEnvironmentAssociation> list_current = env.getAssociatedGroups();
		grpAssociation = new GroupEnvironmentAssociation(group, env.getName(), env.getId());
		grpAssociation.setEnvRole(EnvRole.ADMIN);
		list_current.add(grpAssociation);

		//env.storeAssociatedGroups();
		
		PhysicalFile pf = SAFRApplication.getSAFRFactory().getPhysicalFile(8470);
		 	
		ComponentType componentType = ComponentType.PhysicalFile;
		EditRights editRights = EditRights.ReadModify;
			
		group.assignComponentEditRights(pf, componentType, editRights);
		
		List<Group> groupList = new ArrayList<Group>();
		
		groupList.add(group);
		env.setAssociatedGroupsList(groupList);
		env.storeAssociatedGroups();
		env.storeComponentRights();
		
		SAFRList<GroupComponentAssociation> group_pf_edit = group.getComponentRights(componentType, 107);
		
		String group_pf = group_pf_edit.get(0).getAssociatingComponentName();
		String pf_name = group_pf_edit.get(0).getAssociatedComponentName();
		EditRights pf_right = group_pf_edit.get(0).getRights();
		
	
		/* remove edit rights from group id80 */
		
		group_pf_edit.get(0).setPersistence(SAFRPersistence.DELETED);
		group.storeComponentRights();
		
		/* setup assert testing string */
		
		SAFRList<GroupEnvironmentAssociation> list_store = env.getAssociatedGroups();
		String grp_Assoc_item1 = list_store.get(0).getAssociatingComponentName();
		String grp_Assoc_item2 = list_store.get(1).getAssociatingComponentName();
		
		/* remove associated group id 80 */

		list_store.get(1).setPersistence(SAFRPersistence.DELETED);
		env.storeAssociatedGroups();
		
		/*test result for attaching group to env*/	
		
		assertEquals("Administrators",grp_Assoc_item1);
		assertEquals("test_env_group",grp_Assoc_item2);
		
		/* Test result for edit rights */
		assertEquals(group_pf,"test_env_group");
		assertEquals(pf_name,"test_store_component_rights");
		assertEquals(pf_right,EditRights.ReadModify);
		
		
	}
	
	public void testGetAssociatedGroups() throws SAFRException {
		dbStartup();
	
		Environment env = SAFRApplication.getSAFRFactory().getEnvironment(107);

		SAFRList<GroupEnvironmentAssociation> groups_env = env.getAssociatedGroups();
		List<GroupEnvironmentAssociation> groups_list_env = groups_env.getActiveItems();
		GroupEnvironmentAssociation group = groups_list_env.get(0);
	
	assertEquals(group.getAssociatingComponentName(),"Administrators");
	}

	public void testValidate() {
		boolean correctException = false;
		dbStartup();
		Environment env;
		env = SAFRApplication.getSAFRFactory().createEnvironment();

		// test with nothing set
		correctException = false;
		try {
			env.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (NullPointerException e2) {
			correctException = true;
		}

		assertTrue(correctException);

		correctException = false;

		env.setName(null);
		env.setComment("Test");
		env.initialize("controlRecClientName", "groupName");

		try {
			env.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		correctException = false;

		env.setName("123Test");
		env.setComment("Test");
		env.initialize("controlRecClientName", "groupName");

		try {
			env.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		correctException = false;

		env.setName("@#$%@#$");
		env.setComment("Test");
		env.initialize("controlRecClientName", "groupName");

		try {
			env.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		env.setName("Test1");
		env.initialize("controlRecClient", null);
		env.setComment("Test1 comment");
		correctException = false;
		try {
			env.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		env.setName("Test1");
		env.initialize("controlRecClient", "groupName");
		env.setComment("Test1 comment");
		correctException = false;
		try {
			env.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}
		assertTrue(correctException);

		env.setName("Test1");
		env.initialize(null, "groupName");
		env.setComment("Test1 comment");
		correctException = false;
		try {
			env.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		env.setName("");
		env.initialize("", "");
		env.setComment("Test1 comment");
		correctException = false;
		try {
			env.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		env.setName("testname");
		env.initialize("controlRecClient", "");
		env.setComment("Test1 comment");
		correctException = false;
		try {
			env.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		env.setName("testname");
		env.initialize("", "groupName");
		env.setComment("Test1 comment");
		correctException = false;
		try {
			env.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

		// test valid env
		env.setName("testname");
		env.initialize("controlRecClient", "groupName");
		boolean noException = false;
		correctException = false;
		try {
			env.validate();
			noException = true;
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(noException);
		assertFalse(correctException);

		// test duplicate (assumes existing DEVELOPMENT environment)
		env.setName("DEVELOPMENT");
		env.initialize("controlRecClient", "groupName");
		correctException = false;
		try {
			env.validate();
		} catch (SAFRException e1) {
			correctException = true;
		}

		assertTrue(correctException);

	}

	public void testCreate() throws DAOException, SAFRException {
		boolean correctException = false;

		Environment env = SAFRApplication.getSAFRFactory().createEnvironment();
		dbStartup();
		correctException = false;

		// test correctly stored environment
		boolean noException = false;
		correctException = false;
		env.setName("Test1");
		env.setComment("Test1 comment");
		env.initialize("", "");
		try {
			env.store();
			noException = true;
		} catch (DAOException e1) {
			correctException = true;
		} catch (SAFRException e) {
			correctException = true;
		}

		DAOFactory fact = null;
		try {
			if(postgres) {
				fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();				
			}
		} catch (DAOException e) {
			e.printStackTrace();
		}
		//delIds.add(env.getId());
		assertTrue(noException);
		assertFalse(correctException);
		assertEquals(env.getName(), "Test1");
		assertEquals(env.getComment(), "Test1 comment");
		assertEquals(env.getCreateBy(), fact.getSAFRLogin().getUserId());
		assertEquals(env.getModifyBy(), fact.getSAFRLogin().getUserId());
		assertNotNull(env.getCreateTime());
		assertNotNull(env.getModifyTime());
		noException = false;
		
//		//Check store with the same id.
//		//At least that's what I think it does...
//		env.setName("Test2");
//		env.setComment("Test2 comment");
//
//		try {
//			env.store();
//			noException = true;
//		} catch (DAOException e1) {
//			correctException = true;
//		} catch (SAFRException e) {
//			correctException = true;
//		}
//
//		assertTrue(noException);
//		assertFalse(correctException);
//		assertEquals(env.getName(), "Test2");
//		assertEquals(env.getComment(), "Test2 comment");
//		assertEquals(env.getCreateBy(), fact.getSAFRLogin().getUserId());
//		assertEquals(env.getModifyBy(), fact.getSAFRLogin().getUserId());
//		assertNotNull(env.getCreateTime());
//		assertNotNull(env.getModifyTime());
//
//		// Test for Exception when update fails due to non
//		// availability of component in DB
//
//		// store environment
//		env = SAFRApplication.getSAFRFactory().createEnvironment();
//		env.setName("NoEnvironment");
//		env.setComment("comment");
//		env.store();
//
//		Integer id = env.getId();
//
//		// delete the environment
//		removeEnvironment(id);
//
//		// try updating the deleted environment
//		correctException = false;
//		env.setName("NoEnvironment1");
//		try {
//			env.store();
//		} catch (SAFRException e) {
//			correctException = true;
//			e.printStackTrace();
//
//		}
//		assertTrue(correctException);
	}

	public void testUpdate() throws DAOException, SAFRException {
		boolean correctException = false;

		Environment env = SAFRApplication.getSAFRFactory().createEnvironment();
		dbStartup();
		correctException = false;

		// test correctly stored environment
		boolean noException = false;
		correctException = false;
		env.setName("Test1");
		env.setComment("Test1 comment");
		env.initialize("", "");
		try {
			env.store();
			noException = true;
		} catch (DAOException e1) {
			correctException = true;
		} catch (SAFRException e) {
			correctException = true;
		}

		DAOFactory fact = null;
		try {
			if(postgres) {
				fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();				
			}
		} catch (DAOException e) {
			e.printStackTrace();
		}
		delIds.add(env.getId());
		assertTrue(noException);
		assertFalse(correctException);
		assertEquals(env.getName(), "Test1");
		assertEquals(env.getComment(), "Test1 comment");
		assertEquals(env.getCreateBy(), fact.getSAFRLogin().getUserId());
		assertEquals(env.getModifyBy(), fact.getSAFRLogin().getUserId());
		assertNotNull(env.getCreateTime());
		assertNotNull(env.getModifyTime());
		noException = false;	
		env.setName("Ginger");
		try {
			env.store();
			noException = true;
		} catch (DAOException e1) {
			correctException = true;
		} catch (SAFRException e) {
			correctException = true;
		}
		assertTrue(noException);
		assertFalse(correctException);
		assertEquals(env.getName(), "Ginger");
	}

}
