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

import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.associations.ViewFolderViewAssociation;
import com.ibm.safr.we.model.view.CompilerFactory;

public class TestViewFolder extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestViewFolder");

	static String TABLE_NAME = "VIEWFOLDER";
	static String COL_ID = "VIEWFOLDERID";

	static String DEP_TABLE_LIST[] = { "VFVASSOC", TABLE_NAME  };
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

	public void removeViewFolder(Integer id) {

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
			assertTrue(false);
		}
	}

	public void tearDown() {
        try {
            for (Integer i : delIds) {
                try {
                    removeViewFolder(i);
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
		dbStartup();
		ViewFolder vf = SAFRApplication.getSAFRFactory().createViewFolder();
		assertEquals(vf.getComment(), null);

		vf.setComment("testComment1");
		assertEquals(vf.getComment(), "testComment1");

		vf.setComment("testComment2");
		assertEquals(vf.getComment(), "testComment2");

		vf.setComment("");
		assertEquals(vf.getComment(), "");

		vf.setComment(null);
		assertEquals(vf.getComment(), null);

	}

	public void testGetCreateBy() {
		dbStartup();
		ViewFolder vf = null;
		try {
			vf = SAFRApplication.getSAFRFactory().getViewFolder(672);
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
					+ ".VIEWFOLDER WHERE VIEWFOLDERID=672 AND ENVIRONID=1";
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
				createdBy = DataUtilities.trimString(rs.getString("CREATEDUSERID"));
			}
			assertEquals(vf.getCreateBy(), createdBy);
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
		}

	}

	public void testGetCreateTime() {
		dbStartup();
		ViewFolder vf = null;
		try {
			vf = SAFRApplication.getSAFRFactory().getViewFolder(672);
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
					+ ".VIEWFOLDER WHERE VIEWFOLDERID=672 AND ENVIRONID=1";
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
			assertEquals(vf.getCreateTime(), createdTime);
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}

	public void testGetModifyTime() {
		dbStartup();
		ViewFolder vf = null;
		try {
			vf = SAFRApplication.getSAFRFactory().getViewFolder(672);
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
					+ ".VIEWFOLDER WHERE VIEWFOLDERID=672 AND ENVIRONID=1";
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
			assertEquals(vf.getModifyTime(), modifyTime);
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}

	public void testGetEnvironment() throws SAFRException {

		// Environment should not be null while creating a new View Folder.
		dbStartup();
		Environment env = null;
		ViewFolder vf = SAFRApplication.getSAFRFactory().createViewFolder();
		env = vf.getEnvironment();
		assertNotNull(env);
	}

	public void testGetId() {
		dbStartup();
		ViewFolder vf = SAFRApplication.getSAFRFactory().createViewFolder();
		assertEquals(Integer.valueOf(vf.getId()), Integer
				.valueOf(new Integer(0)));
	}

	public void testGetModifyBy() {
		dbStartup();
		ViewFolder vf = null;
		try {
			vf = SAFRApplication.getSAFRFactory().getViewFolder(672);
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
					+ ".VIEWFOLDER WHERE VIEWFOLDERID=672 AND ENVIRONID=1";
			con = fact.getConnection();

		} catch (DAOException e1) {
			e1.printStackTrace();
			assertFalse(true);
		}

		try {
			pst = con.prepareStatement(statement);
			ResultSet rs = pst.executeQuery();
			String modifiedBy = "";
			if (rs.next()) {
				modifiedBy = DataUtilities.trimString(rs
						.getString("LASTMODUSERID"));
			}
			assertEquals(vf.getModifyBy(), modifiedBy);
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
		}

	}

	public void testGetSetName() {
		dbStartup();
		ViewFolder vf = SAFRApplication.getSAFRFactory().createViewFolder();
		assertEquals(vf.getName(), null);

		vf.setName("testName1");
		assertEquals(vf.getName(), "testName1");

		vf.setName("testName2");
		assertEquals(vf.getName(), "testName2");

		vf.setName("");
		assertEquals(vf.getName(), "");

		vf.setName(null);
		assertEquals(vf.getName(), null);
	}

	public void testStore() throws SAFRException {
		dbStartup();
		boolean correctException = false;
		boolean noException = false;

		ViewFolder vf;
		vf = SAFRApplication.getSAFRFactory().createViewFolder();

		try {
			vf.store();
			noException = true;
		} catch (DAOException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (NullPointerException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(noException);

		correctException = false;
		vf.setName(null);

		try {
			vf.store();
			noException = true;
		} catch (DAOException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(noException);

		noException = false;
		vf = SAFRApplication.getSAFRFactory().createViewFolder();
		vf.setName("Test1");
		vf.setComment("Test1 comment");

		try {
			vf.store();
			noException = true;
		} catch (NullPointerException ne) {
			logger.log(Level.SEVERE, "", ne);
			assertTrue(false);
		} catch (DAOException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (Throwable e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		PGDAOFactory fact = null;
		try {
			fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
		} catch (DAOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue(noException);
		assertEquals(vf.getName(), "Test1");
		assertEquals(vf.getComment(), "Test1 comment");
		assertEquals(vf.getCreateBy(), fact.getSAFRLogin().getUserId());
		assertEquals(vf.getModifyBy(), fact.getSAFRLogin().getUserId());
		assertNotNull(vf.getCreateTime());
		assertNotNull(vf.getModifyTime());

		// delete the view folder
		SAFRApplication.getSAFRFactory().removeViewFolder(vf.getId(), null);

		// try updating deleted View Folder
		vf.setName("DeletedVF");
		correctException = false;
		try {
			vf.store();
		} catch (SAFRException e) {
			e.printStackTrace();
			correctException = true;
		}
		assertTrue(correctException);

	}

	public void testValidate() {
		boolean correctException = false;
		dbStartup();
		ViewFolder vf;
		vf = SAFRApplication.getSAFRFactory().createViewFolder();
		try {
			vf.validate();
		} catch (SAFRException se) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);

		vf.setName("123testname");
		correctException = false;
		try {
			vf.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);
		// check for length =33
		vf.setName("test12345678912345678912345678912");
		correctException = false;
		try {
			vf.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);

		// check for length =32
		vf.setName("test1234567891234567891234567891");
		boolean noException = false;
		try {
			vf.validate();
			noException = true;
		} catch (SAFRException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(noException);

		vf.setName("");
		correctException = false;
		try {
			vf.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);

		vf.setName("ss123_$#@");
		correctException = false;
		try {
			vf.validate();
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);

		vf.setName("Test$#");
		noException = false;
		correctException = false;
		try {
			vf.validate();
			noException = true;
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);

		vf.setName("Test$");
		noException = false;
		correctException = false;
		try {
			vf.validate();
			noException = true;
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(correctException);

		vf.setName("Test#");
		noException = false;
		correctException = false;
		try {
			vf.validate();
			noException = true;
		} catch (SAFRException e1) {
			correctException = true;
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(noException);

		vf.setName("Test");
		noException = false;
		correctException = false;
		try {
			vf.validate();
			noException = true;
		} catch (SAFRException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (Exception e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		}

		assertTrue(noException);

		noException = false;
		vf.setName("Test12");
		vf.setComment("Test comment");

		try {
			vf.validate();
			noException = true;

		} catch (DAOException e1) {
			logger.log(Level.SEVERE, "", e1);
			assertTrue(false);
		} catch (NullPointerException e2) {
			logger.log(Level.SEVERE, "", e2);
			assertTrue(false);
		} catch (Exception e3) {
			logger.log(Level.SEVERE, "", e3);
			assertTrue(false);
		}
		assertTrue(noException);

	}
	
    public void testAddView() throws SAFRException {
        dbStartup();

        ViewFolder vf = SAFRApplication.getSAFRFactory().createViewFolder();
        vf.setName("TestAddView");
        ViewFolderViewAssociation vass = new ViewFolderViewAssociation(vf, 1, "abc1", EditRights.ReadModifyDelete);
        vf.addAssociatedView(vass);
        vass = new ViewFolderViewAssociation(vf, 2, "Sample_SAN_1", EditRights.ReadModifyDelete);
        vf.addAssociatedView(vass);
        vf.store();
        
        Integer id = vf.getId();
        vf = SAFRApplication.getSAFRFactory().getViewFolder(id);
        assertEquals(vf.getViewAssociations().size(), 2);
    }
    
    public void testRemoveView() throws SAFRException {
        dbStartup();

        ViewFolder vf = SAFRApplication.getSAFRFactory().getViewFolder(227);
        ViewFolderViewAssociation vass = vf.getViewAssociations().get(0);
        vf.removeViewAssociation(vass);
        vf.store();
        
        Integer id = vf.getId();
        vf = SAFRApplication.getSAFRFactory().getViewFolder(id);
        assertEquals(vf.getViewAssociations().size(), 2);
        
        vf.addAssociatedView(new ViewFolderViewAssociation(vf, vass.getAssociatedComponentIdNum(), vass.getAssociatedComponentName(), 
            vass.getAssociatedComponentRights()));
        vf.store();        
    }
    
}
