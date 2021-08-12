package com.ibm.safr.we.query;

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


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ControlRecordQueryBean;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.GroupQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.query.UserQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;

public class TestSAFRQuery extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestSAFRQuery");
	TestDataLayerHelper helper = new TestDataLayerHelper();

	public void dbStartup() {
		helper.initDataLayer();
	}

	public void testQueryAllPhysicalFiles() throws SAFRException {
		dbStartup();
		boolean correctException = false;
		List<PhysicalFileQueryBean> physicalFileQueryBean = null;
		try {
			SAFRQuery.queryAllPhysicalFiles(null, SortType.SORT_BY_NAME);
		} catch (DAOException e) {
			correctException = true;
		}
		assertTrue(correctException);

		physicalFileQueryBean = SAFRQuery.queryAllPhysicalFiles(1,
				SortType.SORT_BY_NAME);
		assertNotNull(physicalFileQueryBean);
		assertFalse(physicalFileQueryBean.isEmpty());

		helper.setUser("NOPERM");
		physicalFileQueryBean = SAFRQuery.queryAllPhysicalFiles(1,
				SortType.SORT_BY_NAME);
		assertNotNull(physicalFileQueryBean);
		assertFalse(physicalFileQueryBean.isEmpty());
	}

	public void testQueryAllControlRecords() {
		dbStartup();
		boolean correctException = false;
		List<ControlRecordQueryBean> controlRecordQueryBean = null;
		try {
			controlRecordQueryBean = SAFRQuery.queryAllControlRecords(null,
					SortType.SORT_BY_NAME);
		} catch (DAOException e) {
			correctException = true;
		}
		assertTrue(correctException);

		try {
			controlRecordQueryBean = SAFRQuery.queryAllControlRecords(1,
					SortType.SORT_BY_NAME);
		} catch (DAOException e) {
			logger.log(Level.SEVERE, "", e);
		}
		assertNotNull(controlRecordQueryBean);
		assertTrue(!controlRecordQueryBean.isEmpty());

		correctException = false;
		try {
			controlRecordQueryBean = SAFRQuery.queryAllControlRecords(999,
					SortType.SORT_BY_NAME);
		} catch (DAOException e) {
			logger.log(Level.SEVERE, "", e);
		}
		assertNotNull(controlRecordQueryBean);
		assertTrue(controlRecordQueryBean.isEmpty());
	}

	public void testQueryAllEnvironments() {
		dbStartup();

		List<EnvironmentQueryBean> environmentQueryBean = null;
		try {
			environmentQueryBean = SAFRQuery
					.queryAllEnvironments(SortType.SORT_BY_NAME);
		} catch (DAOException e) {
			logger.log(Level.SEVERE, "", e);

		}
		assertNotNull(environmentQueryBean);
		assertTrue(!environmentQueryBean.isEmpty());
	}

	public void testQueryAllGroups() {
		dbStartup();

		List<GroupQueryBean> groupQueryBean = null;
		try {
			groupQueryBean = SAFRQuery.queryAllGroups(SortType.SORT_BY_NAME);
		} catch (DAOException e) {
			logger.log(Level.SEVERE, "", e);

		}
		assertNotNull(groupQueryBean);
		assertTrue(!groupQueryBean.isEmpty());
	}

	public void testQueryAllViewFolders() {
		dbStartup();
		boolean correctException = false;
		List<ViewFolderQueryBean> viewFolderQueryBean = null;
		try {
			viewFolderQueryBean = SAFRQuery.queryAllViewFolders(null,
					SortType.SORT_BY_NAME);
		} catch (DAOException e) {
			correctException = true;
		}
		assertTrue(correctException);

		try {
			viewFolderQueryBean = SAFRQuery.queryAllViewFolders(1,
					SortType.SORT_BY_NAME);
		} catch (DAOException e) {
			logger.log(Level.SEVERE, "", e);
		}
		assertNotNull(viewFolderQueryBean);
		assertTrue(!viewFolderQueryBean.isEmpty());

		correctException = false;
		try {
			viewFolderQueryBean = SAFRQuery.queryAllViewFolders(999,
					SortType.SORT_BY_NAME);
		} catch (DAOException e) {
			logger.log(Level.SEVERE, "", e);
		}
		assertNotNull(viewFolderQueryBean);
		assertTrue(viewFolderQueryBean.isEmpty());
	}

	public void testQueryAllUsers() {
		dbStartup();

		List<UserQueryBean> userQueryBean = null;
		try {
			userQueryBean = SAFRQuery.queryAllUsers();
		} catch (DAOException e) {
			logger.log(Level.SEVERE, "", e);

		}
		assertNotNull(userQueryBean);
		assertTrue(!userQueryBean.isEmpty());
	}

	public void testQueryAllUserExitRoutines() throws SAFRException {
		dbStartup();
		boolean correctException = false;
		List<UserExitRoutineQueryBean> userExitRoutineQueryBean = null;
		try {
			userExitRoutineQueryBean = SAFRQuery.queryAllUserExitRoutines(null,
					SortType.SORT_BY_NAME);
		} catch (DAOException e) {
			correctException = true;
		}
		assertTrue(correctException);

		userExitRoutineQueryBean = SAFRQuery.queryAllUserExitRoutines(1,
				SortType.SORT_BY_NAME);
		assertNotNull(userExitRoutineQueryBean);
		assertFalse(userExitRoutineQueryBean.isEmpty());

		helper.setUser("NOPERM");
		userExitRoutineQueryBean = SAFRQuery.queryAllUserExitRoutines(1,
				SortType.SORT_BY_NAME);
		assertNotNull(userExitRoutineQueryBean);
		assertFalse(userExitRoutineQueryBean.isEmpty());
	}

    public void testQueryAllLookupPaths() throws SAFRException {
        dbStartup();
        boolean correctException = false;
        List<LookupQueryBean> lookupPathQueryBean = null;
        try {
            lookupPathQueryBean = SAFRQuery.queryAllLookups(null,
                    SortType.SORT_BY_NAME);
        } catch (DAOException e) {
            correctException = true;
        }
        assertTrue(correctException);

        lookupPathQueryBean = SAFRQuery.queryAllLookups(1,
                SortType.SORT_BY_NAME);
        assertNotNull(lookupPathQueryBean);
        assertFalse(lookupPathQueryBean.isEmpty());

        helper.setUser("NOPERM");
        lookupPathQueryBean = SAFRQuery.queryAllLookups(1,
                SortType.SORT_BY_NAME);
        assertNotNull(lookupPathQueryBean);
        assertFalse(lookupPathQueryBean.isEmpty());
    }

	
	public void testQueryUserExitRoutines() throws SAFRException {
		dbStartup();
		boolean correctException = false;
		List<UserExitRoutineQueryBean> userExitRoutineQueryBean = null;
		try {
			userExitRoutineQueryBean = SAFRQuery.queryUserExitRoutines(null,
					null, SortType.SORT_BY_NAME);
		} catch (DAOException e) {
			correctException = true;
		}
		assertTrue(correctException);

		userExitRoutineQueryBean = SAFRQuery.queryUserExitRoutines(1,
				"READ", SortType.SORT_BY_NAME);
		assertNotNull(userExitRoutineQueryBean);
		assertTrue(!userExitRoutineQueryBean.isEmpty());

		helper.setUser("NOPERM");
		userExitRoutineQueryBean = SAFRQuery.queryUserExitRoutines(1,
				" ", SortType.SORT_BY_NAME);
		assertNotNull(userExitRoutineQueryBean);
		assertTrue(userExitRoutineQueryBean.isEmpty());
	}

    public void testQueryAllLogicalFiles() throws DAOException, SAFRException {
        dbStartup();
        boolean correctException = false;
        List<LogicalFileQueryBean> logicalFileQueryBean = null;
        try {
            logicalFileQueryBean = SAFRQuery.queryAllLogicalFiles(null,
                    SortType.SORT_BY_NAME);
        } catch (DAOException e) {
            correctException = true;
        }
        assertTrue(correctException);

        logicalFileQueryBean = SAFRQuery.queryAllLogicalFiles(1,
                SortType.SORT_BY_NAME);
        assertNotNull(logicalFileQueryBean);
        assertFalse(logicalFileQueryBean.isEmpty());

        helper.setUser("NOPERM");
        logicalFileQueryBean = SAFRQuery.queryAllLogicalFiles(1,
                SortType.SORT_BY_NAME);
        assertNotNull(logicalFileQueryBean);
        assertFalse(logicalFileQueryBean.isEmpty());
    }
	
	public void testQueryAllLogicalRecords() throws SAFRException {
		dbStartup();
		boolean correctException = false;
		List<LogicalRecordQueryBean> logicalRecordQueryBean = null;
		try {
			logicalRecordQueryBean = SAFRQuery.queryAllLogicalRecords(null,
					SortType.SORT_BY_NAME);
		} catch (DAOException e) {
			correctException = true;
		}
		assertTrue(correctException);

		logicalRecordQueryBean = SAFRQuery.queryAllLogicalRecords(1,
				SortType.SORT_BY_NAME);
		assertNotNull(logicalRecordQueryBean);
		assertFalse(logicalRecordQueryBean.isEmpty());

		helper.setUser("NOPERM");
		logicalRecordQueryBean = SAFRQuery.queryAllLogicalRecords(1,
				SortType.SORT_BY_NAME);
        assertNotNull(logicalRecordQueryBean);
        assertFalse(logicalRecordQueryBean.isEmpty());
	}

    public void testQueryAllActiveLogicalRecords() throws SAFRException {
        dbStartup();
        boolean correctException = false;
        List<LogicalRecordQueryBean> logicalRecordQueryBean = null;
        try {
            logicalRecordQueryBean = SAFRQuery.queryAllActiveLogicalRecords(null,
                    SortType.SORT_BY_NAME);
        } catch (DAOException e) {
            correctException = true;
        }
        assertTrue(correctException);

        logicalRecordQueryBean = SAFRQuery.queryAllActiveLogicalRecords(1,
                SortType.SORT_BY_NAME);
        assertNotNull(logicalRecordQueryBean);
        assertFalse(logicalRecordQueryBean.isEmpty());

        helper.setUser("NOPERM");
        logicalRecordQueryBean = SAFRQuery.queryAllActiveLogicalRecords(1,
                SortType.SORT_BY_NAME);
        assertNotNull(logicalRecordQueryBean);
        assertFalse(logicalRecordQueryBean.isEmpty());
    }
	
	public void testQueryPossiblePFAssociation() throws DAOException, SAFRException {
		dbStartup();
		boolean correctException = false;
		List<PhysicalFileQueryBean> physicalFileQueryBean = null;
		LogicalFile logicalFile = null;
		logicalFile = SAFRApplication.getSAFRFactory().getLogicalFile(2);

		try {
			physicalFileQueryBean = SAFRQuery.queryPossiblePFAssociations(
					logicalFile, null);
		} catch (DAOException e) {
			correctException = true;
		}
		assertTrue(correctException);

		physicalFileQueryBean = SAFRQuery.queryPossiblePFAssociations(
            logicalFile, 1);
		assertNotNull(physicalFileQueryBean);
		assertTrue(physicalFileQueryBean.size() > 0);

		helper.setUser("NOPERM");
        physicalFileQueryBean = SAFRQuery.queryPossiblePFAssociations(
                logicalFile, 1);
        assertNotNull(physicalFileQueryBean);
        assertTrue(physicalFileQueryBean.size() > 0);
		
	}
	
    public void testQueryPossibleLFAssociation() throws SAFRException {
        dbStartup();
        boolean correctException = false;
        List<LogicalFileQueryBean> logicalFileQueryBean = null;
        LogicalRecord logicalRecord = null;
        logicalRecord = SAFRApplication.getSAFRFactory().getLogicalRecord(1);

        try {
            logicalFileQueryBean = SAFRQuery.queryPossibleLFAssociations(
                    logicalRecord, null);
        } catch (DAOException e) {
            correctException = true;
        }
        assertTrue(correctException);

        logicalFileQueryBean = SAFRQuery.queryPossibleLFAssociations(
            logicalRecord, 1);
        assertNotNull(logicalFileQueryBean);
        assertTrue(logicalFileQueryBean.size() > 0);

        helper.setUser("NOPERM");
        logicalFileQueryBean = SAFRQuery.queryPossibleLFAssociations(
                logicalRecord, 1);
        assertNotNull(logicalFileQueryBean);
        assertTrue(logicalFileQueryBean.size() > 0);
        
    }
	
}
