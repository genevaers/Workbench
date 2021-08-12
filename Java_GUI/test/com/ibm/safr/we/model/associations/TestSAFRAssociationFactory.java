package com.ibm.safr.we.model.associations;

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


import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.EnvRole;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRAssociationList;

public class TestSAFRAssociationFactory extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestSAFRAssociationFactory");
	TestDataLayerHelper helper = new TestDataLayerHelper();

	public void dbStartup() {
		helper.initDataLayer();
	}

	public void testGetPhysicalFileToLogicalFileAssociations() {
		dbStartup();
		SAFRAssociationList<FileAssociation> safrAssociationList = null;
		PhysicalFile physicalFile;
		boolean correctException = false;
		try {
			physicalFile = SAFRApplication.getSAFRFactory().getPhysicalFile(
					8361);
			safrAssociationList = SAFRAssociationFactory
					.getPhysicalFileToLogicalFileAssociations(physicalFile);
		} catch (SAFRException se) {
			correctException = true;
		}
		assertNotNull(safrAssociationList);
		assertFalse(correctException);
	}

	public void testGetLogicalFileToPhysicalFileAssociations() {
		dbStartup();
		SAFRAssociationList<FileAssociation> safrAssociationList = null;
		LogicalFile logicalFile;
		boolean correctException = false;
		try {
			logicalFile = SAFRApplication.getSAFRFactory().getLogicalFile(5);
			safrAssociationList = SAFRAssociationFactory
					.getLogicalFileToPhysicalFileAssociations(logicalFile);
		} catch (SAFRException se) {
			correctException = true;
		}
		assertNotNull(safrAssociationList);
		assertFalse(correctException);
	}

    public void testGetLogicalFileToPhysicalFileAssociationsNoPerm() throws DAOException, SAFRException {
        dbStartup();
        helper.setUser("NOPERM");
        SAFRAssociationList<FileAssociation> safrAssociationList = null;
        LogicalFile logicalFile;
        boolean correctException = false;
        try {
            logicalFile = SAFRApplication.getSAFRFactory().getLogicalFile(5);
            safrAssociationList = SAFRAssociationFactory
                    .getLogicalFileToPhysicalFileAssociations(logicalFile);
        } catch (SAFRException se) {
            correctException = true;
        }
        assertNotNull(safrAssociationList);
        assertFalse(correctException);
    }
	
	public void testGetLogicalFileToLogicalRecordAssociations() {
		dbStartup();
		SAFRAssociationList<ComponentAssociation> safrAssociationList = null;
		LogicalFile logicalFile;
		boolean correctException = false;
		try {
			logicalFile = SAFRApplication.getSAFRFactory().getLogicalFile(5);
			safrAssociationList = SAFRAssociationFactory
					.getLogicalFileToLogicalRecordAssociations(logicalFile);
		} catch (SAFRException se) {
			correctException = true;
		}
		assertNotNull(safrAssociationList);
		assertFalse(correctException);
	}

	public void testGetLogicalRecordToLogicalFileAssociations() {
		dbStartup();
		SAFRAssociationList<ComponentAssociation> safrAssociationList = null;
		LogicalRecord logicalRecord;
		boolean correctException = false;
		try {
			logicalRecord = SAFRApplication.getSAFRFactory().getLogicalRecord(
					1288);
			safrAssociationList = SAFRAssociationFactory
					.getLogicalRecordToLogicalFileAssociations(logicalRecord);
		} catch (SAFRException se) {
			correctException = true;
		}
		assertNotNull(safrAssociationList);
		assertFalse(correctException);
	}

    public void testGetLogicalRecordToLogicalFileAssociationsNoPerm() throws SAFRException {
        dbStartup();
        SAFRAssociationList<ComponentAssociation> safrAssociationList = null;
        LogicalRecord logicalRecord;
        helper.setUser("NOPERM");
        logicalRecord = SAFRApplication.getSAFRFactory().getLogicalRecord(
                1288);
        safrAssociationList = SAFRAssociationFactory
                .getLogicalRecordToLogicalFileAssociations(logicalRecord);
        assertNotNull(safrAssociationList);
    }
	
	public void testGetGroupToUserAssociations() {
		dbStartup();
		SAFRAssociationList<GroupUserAssociation> safrAssociationList = null;
		Group group;
		boolean correctException = false;
		try {
			group = SAFRApplication.getSAFRFactory().getGroup(1);
			safrAssociationList = SAFRAssociationFactory
					.getGroupToUserAssociations(group);
		} catch (SAFRException se) {
			correctException = true;
		}
		assertNotNull(safrAssociationList);
		assertFalse(correctException);
	}

	// public void testGetAssociatedGroups() {
	// dbStartup();
	// SAFRAssociationList<EnvironmentGroupAssociation> safrAssociationList =
	// null;
	// Environment env;
	// try {
	// env = SAFRApplication.getSAFRFactory().getEnvironment(1l);
	// safrAssociationList = SAFRAssociationFactory
	// .getAssociatedGroups(env);
	// } catch (SAFRException se) {
	// logger.log(Level.SEVERE, "", se);
	// } catch (DAOException e) {
	// logger.log(Level.SEVERE, "", e);
	// }
	// assertNotNull(safrAssociationList);
	// }

	public void testGetAssociatedEnvironments() {
		dbStartup();
		SAFRAssociationList<GroupEnvironmentAssociation> safrAssociationList = null;
		Group grp;
		boolean correct = false;
		boolean correctException = false;
		try {
			grp = SAFRApplication.getSAFRFactory().getGroup(78);
			safrAssociationList = SAFRAssociationFactory
					.getGroupToEnvironmentAssociations(grp);
			for (GroupEnvironmentAssociation grpEnvAssociation : safrAssociationList) {
				if (grpEnvAssociation.getAssociatedComponentIdNum() == 3) {
					if (grpEnvAssociation.getEnvRole().equals(EnvRole.ADMIN)) {
						correct = true;
					}
				}
			}

		} catch (SAFRException se) {
			correctException = true;
		}
        assertFalse(correctException);
		assertTrue(correct);
		assertNotNull(safrAssociationList);
	}

	public void testGetEnvironmentToGroupAssociations() {
		dbStartup();
		SAFRAssociationList<GroupEnvironmentAssociation> safrAssociationList = null;
		Environment env;
		boolean correct = false;
		try {
			env = SAFRApplication.getSAFRFactory().getEnvironment(97);
			safrAssociationList = SAFRAssociationFactory
					.getEnvironmentToGroupAssociations(env, SortType.SORT_BY_ID);
			for (GroupEnvironmentAssociation grpEnvAssociation : safrAssociationList) {
				if (grpEnvAssociation.getAssociatingComponentId() == 76) {
					correct = true;
				} else if (grpEnvAssociation.getAssociatingComponentId() == 77) {
					correct = true;
				} else {
					assertTrue(false);
				}
			}
		} catch (SAFRException se) {
			assertTrue(false);
		}
		assertTrue(correct);
		assertNotNull(safrAssociationList);
	}

}
