package com.ibm.safr.we.data;

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

import com.ibm.safr.we.data.dao.CodeSetDAO;
import com.ibm.safr.we.data.dao.ControlRecordDAO;
import com.ibm.safr.we.data.dao.DependencyCheckerDAO;
import com.ibm.safr.we.data.dao.EnvironmentDAO;
import com.ibm.safr.we.data.dao.ExportDAO;
import com.ibm.safr.we.data.dao.GroupDAO;
import com.ibm.safr.we.data.dao.HeaderFooterDAO;
import com.ibm.safr.we.data.dao.LRFieldDAO;
import com.ibm.safr.we.data.dao.LogicalFileDAO;
import com.ibm.safr.we.data.dao.LogicalRecordDAO;
import com.ibm.safr.we.data.dao.LookupDAO;
import com.ibm.safr.we.data.dao.LookupPathStepDAO;
import com.ibm.safr.we.data.dao.MigrateDAO;
import com.ibm.safr.we.data.dao.NextKeyDAO;
import com.ibm.safr.we.data.dao.OldCompilerDAO;
import com.ibm.safr.we.data.dao.PhysicalFileDAO;
import com.ibm.safr.we.data.dao.ReportsDAO;
import com.ibm.safr.we.data.dao.StoredProcedureDAO;
import com.ibm.safr.we.data.dao.UserDAO;
import com.ibm.safr.we.data.dao.UserExitRoutineDAO;
import com.ibm.safr.we.data.dao.ViewColumnDAO;
import com.ibm.safr.we.data.dao.ViewColumnSourceDAO;
import com.ibm.safr.we.data.dao.ViewDAO;
import com.ibm.safr.we.data.dao.ViewFolderDAO;
import com.ibm.safr.we.data.dao.ViewLogicDependencyDAO;
import com.ibm.safr.we.data.dao.ViewSortKeyDAO;
import com.ibm.safr.we.data.dao.ViewSourceDAO;

public interface DAOFactory {

	void setSAFRLogin(UserSessionParameters params);

	/**
	 * Disconnect the active connection.
	 * 
	 * @throws DAOException
	 *             if an error occurs while disconnecting.
	 */
	void disconnect() throws DAOException;

	EnvironmentDAO getEnvironmentDAO() throws DAOException;

	UserDAO getUserDAO() throws DAOException;

	GroupDAO getGroupDAO() throws DAOException;

	ViewDAO getViewDAO() throws DAOException;

	ViewSourceDAO getViewSourceDAO() throws DAOException;

	ViewColumnDAO getViewColumnDAO() throws DAOException;

	ViewColumnSourceDAO getViewColumnSourceDAO() throws DAOException;

	ViewSortKeyDAO getViewSortKeyDAO() throws DAOException;

	ViewLogicDependencyDAO getViewLogicDependencyDAO() throws DAOException;

	HeaderFooterDAO getHeaderFooterDAO() throws DAOException;

	ViewFolderDAO getViewFolderDAO() throws DAOException;

	ControlRecordDAO getControlRecordDAO() throws DAOException;

	CodeSetDAO getCodeSetDAO() throws DAOException;

	PhysicalFileDAO getPhysicalFileDAO() throws DAOException;

	UserExitRoutineDAO getUserExitRoutineDAO() throws DAOException;

	LogicalFileDAO getLogicalFileDAO() throws DAOException;

	LogicalRecordDAO getLogicalRecordDAO() throws DAOException;

	LRFieldDAO getLRFieldDAO() throws DAOException;

	OldCompilerDAO getOldCompilerDAO() throws DAOException;
	
	LookupDAO getLookupDAO() throws DAOException;

	LookupPathStepDAO getLookupPathStepDAO() throws DAOException;

	ExportDAO getExportDAO() throws DAOException;

	MigrateDAO getMigrateDAO() throws DAOException;

	DependencyCheckerDAO getDependencyCheckerDAO() throws DAOException;
	
	NextKeyDAO getNextKeyDAO() throws DAOException;
	
    StoredProcedureDAO getStoredProcedureDAO() throws DAOException;
	
	ConnectionParameters getConnectionParameters();

	Connection reconnect() throws DAOException;

	Connection getConnection() throws DAOException;

	DAOUOW getDAOUOW();

	UserSessionParameters getSAFRLogin();
	
	ReportsDAO getReportsDAO();
}
