package com.ibm.safr.we.ui.reports;

import java.nio.file.Path;

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
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRAssociationList;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.query.EnvironmentSecurityReportBean;
import com.ibm.safr.we.model.query.SystemAdministorsBean;
import com.ibm.safr.we.model.query.UserGroupsReportBean;
import com.ibm.safr.we.model.query.ViewMappingsReportQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class EnvironmentSecurityReportGenerator implements IReportGenerator {
    
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.EnvironmentSecurityReportData");
    
    private EnvironmentSecurityHTMLReport envSecurityReport;
	private List<String> errorMsgList = new ArrayList<String>();
	private List<Integer> envIDs = new ArrayList<Integer>();

	private SortType sortType;

	private UserGroupsReportGenerator ugw;

	public EnvironmentSecurityReportGenerator(List<Integer> envIds) throws SAFRException {
		loadData(envIds);
	}

	private void loadData(List<Integer> envIds) {
		try {
			List<EnvironmentSecurityReportBean> esbs = DAOFactoryHolder.getDAOFactory().getReportsDAO().getEnvironmentSecurityDetails(envIds);
			envSecurityReport = new EnvironmentSecurityHTMLReport();
			envSecurityReport.setSecurityentires(esbs);
		} catch (Exception se) {
			logger.log(Level.SEVERE, "Unable to load", se);
		}
	}
	
	public List<String> getErrors() {
		return errorMsgList;
	}

	public boolean hasData() {
		return (envSecurityReport!=null );
	}
	/************************************************************************************************************************/

	@Override
	public String getHtmlUrl() {
		return envSecurityReport.getUrl();
	}


	@Override
	public void writeReportFiles(Path path, String baseName) {
		envSecurityReport.setFileName(path, baseName);
		envSecurityReport.write();
	}

}
