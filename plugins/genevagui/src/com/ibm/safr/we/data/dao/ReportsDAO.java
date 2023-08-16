package com.ibm.safr.we.data.dao;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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

import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.model.query.EnvironmentSecurityReportBean;
import com.ibm.safr.we.model.query.LogicalRecordReportQueryBean;
import com.ibm.safr.we.model.query.LookupPrimaryKeysBean;
import com.ibm.safr.we.model.query.LookupReportQueryBean;
import com.ibm.safr.we.model.query.SystemAdministorsBean;
import com.ibm.safr.we.model.query.UserGroupsReportBean;
import com.ibm.safr.we.model.query.ViewColumnPICQueryBean;
import com.ibm.safr.we.model.query.ViewMappingsCSVReportQueryBean;
import com.ibm.safr.we.model.query.ViewMappingsReportQueryBean;
import com.ibm.safr.we.model.query.ViewPropertiesReportQueryBean;
import com.ibm.safr.we.model.query.ViewSortKeyReportQueryBean;
import com.ibm.safr.we.model.query.ViewSourcesReportQueryBean;

public interface ReportsDAO {

	public ViewPropertiesReportQueryBean getViewProperties(Integer id, Integer environmentId) throws DAOException;

	public List<ViewSourcesReportQueryBean> getViewSources(Integer id, Integer environmentId) throws DAOException;
	
	public List<ViewSortKeyReportQueryBean> getViewSortKeys(Integer id, Integer environmentId) throws DAOException;
	public List<ViewMappingsReportQueryBean> getViewColumnMappings(Integer id, Integer environmentId, String vsourcenum) throws DAOException;
	public List<ViewColumnPICQueryBean> getViewColumnPICData(Integer id, Integer environmentId) throws DAOException;
	
	public List<ViewMappingsCSVReportQueryBean> getViewColumnCSVMappings(List<Integer> ids, Integer environmentId) throws DAOException;
	
	public List<LogicalRecordReportQueryBean> getLogicalRecords(Integer id, Integer environmentId) throws DAOException;

	public List<EnvironmentSecurityReportBean> getEnvironmentSecurityDetails(List<Integer> ids) throws DAOException;
	public List<SystemAdministorsBean> getsystemAdministrators() throws DAOException;
	public List<UserGroupsReportBean> getUserGroupsReport() throws DAOException;
	
	public List<LookupReportQueryBean> getLookupReport(Integer id, Integer environmentId) throws DAOException;
	public List<LookupPrimaryKeysBean> getLookupPrimaryKeysReport(Integer id, Integer environmentId) throws DAOException;
}
