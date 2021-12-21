package com.ibm.safr.we.ui.reports;

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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.EnvRole;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRAssociationList;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class EnvironmentSecurityReportData implements IReportData {
    
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.EnvironmentSecurityReportData");
    
	Map<Integer, EnvironmentPropertiesRD> envPropertiesMap = new HashMap<Integer, EnvironmentPropertiesRD>();
	private List<String> errorMsgList = new ArrayList<String>();
	private SortType sortType;
	private List<Integer> envIDs = new ArrayList<Integer>();

	public EnvironmentSecurityReportData(List<Integer> envIds, SortType sortType)
			throws SAFRException {
		this.sortType = sortType;
		for (int envId : envIds) {
			loadData(null, envId);
		}
	}

	/**
	 * Constructor for class EnvironmentSecurityReportData which accepts a
	 * Environment model object of the Environment whose report is to be
	 * generated.
	 * 
	 * @param environment
	 *            : Environment whose report is to be generated.
	 * @throws SAFRException
	 */
	public EnvironmentSecurityReportData(Environment environment,
			SortType sortType) throws SAFRException {
		this.sortType = sortType;
		loadData(environment, environment.getId());
	}

	private void loadData(Environment environment, Integer id) {
		try {
			if (environment == null) {
				environment = SAFRApplication.getSAFRFactory().getEnvironment(
						id);
			}
			// set the properties of Environment.
			EnvironmentPropertiesRD envPropertiesReport = new EnvironmentPropertiesRD(
					environment);
			// get the associated groups to that environment.
			SAFRAssociationList<GroupEnvironmentAssociation> grpEnvAssocList = SAFRAssociationFactory
					.getEnvironmentToGroupAssociations(environment,
							this.sortType);

			// loop through all the environment group associations.
			if (!grpEnvAssocList.isEmpty()) {
				for (GroupEnvironmentAssociation grpEnvAssoc : grpEnvAssocList) {
					AssociatedGroupsRD grpsReport = new AssociatedGroupsRD(
							grpEnvAssoc);
					envPropertiesReport.addGroup(grpsReport);
				}
			}
			// add to map
			envPropertiesMap.put(id, envPropertiesReport);
			// CQ 8165. Nikita. 01/07/2010
			// retain the original order in which the list of Environment ids
			// was
			// passed
			envIDs.add(id);
		} catch (Exception se) {
		    logger.log(Level.SEVERE, "Unable to load", se);
			errorMsgList.add("Environment: " + id
					+ " - Unable to load due to below error:" + SAFRUtilities.LINEBREAK + "    "
					+ se.getMessage());
		} 
	}

	/************************************************************************************************************************/

	/**
	 * This method is used to get the properties of all the selected
	 * environments.
	 * 
	 * @return a list of EvnironmentPropertiesRD.
	 */
	public List<EnvironmentPropertiesRD> getEnvironmentsReportData() {
		List<EnvironmentPropertiesRD> envPropertiesList = new ArrayList<EnvironmentPropertiesRD>();
		// CQ 8165. Nikita. 01/07/2010
		// generate report based on original order in which Environment ids were
		// passed
		for (Integer id : envIDs) {
			envPropertiesList.add(envPropertiesMap.get(id));
		}
		return envPropertiesList;
	}

	/************************************************************************************************************************/

	/**
	 * This method is used to get the properties of the groups associated to the
	 * specified environment.
	 * 
	 * @param envId
	 *            : the id of the environment for which the properties of the
	 *            associated groups are to be retrieved.
	 * @return a list of AssociatedGroupsRD.
	 * 
	 */
	public List<AssociatedGroupsRD> getAssociatedGroupsReportData(Integer envId) {
		if (!envPropertiesMap.isEmpty()) {
			EnvironmentPropertiesRD envPropertyReport = envPropertiesMap
					.get(envId);
			return envPropertyReport.getAssociatedGroupReportList();

		}
		return null;
	}

	/************************************************************************************************************************/

	public class EnvironmentPropertiesRD {
		private Integer envId;
		private String envName;
		List<AssociatedGroupsRD> associatedGroupReportList = new ArrayList<AssociatedGroupsRD>();

		public EnvironmentPropertiesRD(Environment env) throws SAFRException {
			envId = env.getId();
			envName = env.getName();
		}

		public Integer getEnvId() {
			return envId;
		}

		public String getEnvName() {
			if (this.envName == null) {
				this.envName = "";
			}
			return UIUtilities.getComboString(envName, envId);
		}

		public void addGroup(AssociatedGroupsRD group) {
			associatedGroupReportList.add(group);
		}

		public List<AssociatedGroupsRD> getAssociatedGroupReportList() {
			return associatedGroupReportList;
		}

	}

	/************************************************************************************************************************/

	public class AssociatedGroupsRD {
		private Integer groupId;
		private String groupName;
		private String admin;

		public AssociatedGroupsRD(GroupEnvironmentAssociation grpEnvAssociation) {
			if (grpEnvAssociation != null) {
				groupId = grpEnvAssociation.getAssociatingComponentId();
				groupName = grpEnvAssociation.getAssociatingComponentName();
				if (grpEnvAssociation.getEnvRole().equals(EnvRole.ADMIN)) {
					admin = "Admin";
				} else {
					admin = "";
				}
			}
		}

		public Integer getGroupId() {
			return groupId;
		}

		public String getGroupName() {
			return groupName;
		}

		public String getAdmin() {
			return admin;
		}
	}

	/************************************************************************************************************************/

	public List<String> getErrors() {
		return errorMsgList;
	}

	public boolean hasData() {
		return (!this.envPropertiesMap.isEmpty());
	}
	/************************************************************************************************************************/

}
