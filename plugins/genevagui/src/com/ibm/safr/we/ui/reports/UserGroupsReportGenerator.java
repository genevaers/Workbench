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
import java.util.List;
import java.util.logging.Logger;

import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.query.SystemAdministorsBean;
import com.ibm.safr.we.model.query.UserGroupsReportBean;

public class UserGroupsReportGenerator implements IReportGenerator {
    
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.EnvironmentSecurityReportData");
    
    private EnvironmentSecurityHTMLReport envSecurity;
	private List<String> errorMsgList = new ArrayList<String>();

	private UserGroupsHTMLReport ugHtmlReport;

	public UserGroupsReportGenerator() throws SAFRException {
		loadUserData();
	}


	private void loadUserData() {
		List<SystemAdministorsBean> sas = DAOFactoryHolder.getDAOFactory().getReportsDAO().getsystemAdministrators();
		List<UserGroupsReportBean> ugs = DAOFactoryHolder.getDAOFactory().getReportsDAO().getUserGroupsReport();
		ugHtmlReport = new UserGroupsHTMLReport();
		ugHtmlReport.setUserGroupEntries(ugs);
		ugHtmlReport.setSystemAdminEntries(sas);
	}


	public List<String> getErrors() {
		return errorMsgList;
	}

	public boolean hasData() {
		return (envSecurity!=null);
	}

	@Override
	public String getHtmlUrl() {
		return ugHtmlReport.getUrl();
	}

	@Override
	public void writeReportFiles(Path path, String baseName) {
		ugHtmlReport.setFileName(path, baseName);
		ugHtmlReport.write();
	}

}
