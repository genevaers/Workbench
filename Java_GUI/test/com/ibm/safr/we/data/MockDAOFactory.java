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

public class MockDAOFactory implements DAOFactory {

    public void setSAFRLogin(UserSessionParameters params) {
    }

    public void disconnect() throws DAOException {
    }

    public EnvironmentDAO getEnvironmentDAO() throws DAOException {
        return null;
    }

    public UserDAO getUserDAO() throws DAOException {
        return null;
    }

    public GroupDAO getGroupDAO() throws DAOException {
        return null;
    }

    public ViewDAO getViewDAO() throws DAOException {
        return null;
    }

    public ViewSourceDAO getViewSourceDAO() throws DAOException {
        return null;
    }

    public ViewColumnDAO getViewColumnDAO() throws DAOException {
        return null;
    }

    public ViewColumnSourceDAO getViewColumnSourceDAO() throws DAOException {
        return null;
    }

    public ViewSortKeyDAO getViewSortKeyDAO() throws DAOException {
        return null;
    }

    public ViewLogicDependencyDAO getViewLogicDependencyDAO()
        throws DAOException {
        return null;
    }

    public HeaderFooterDAO getHeaderFooterDAO() throws DAOException {
        return null;
    }

    public ViewFolderDAO getViewFolderDAO() throws DAOException {
        return null;
    }

    public ControlRecordDAO getControlRecordDAO() throws DAOException {
        return null;
    }

    public CodeSetDAO getCodeSetDAO() throws DAOException {
        return null;
    }

    public PhysicalFileDAO getPhysicalFileDAO() throws DAOException {
        return null;
    }

    public UserExitRoutineDAO getUserExitRoutineDAO() throws DAOException {
        return null;
    }

    public LogicalFileDAO getLogicalFileDAO() throws DAOException {
        return null;
    }

    public LogicalRecordDAO getLogicalRecordDAO() throws DAOException {
        return null;
    }

    public LRFieldDAO getLRFieldDAO() throws DAOException {
        return null;
    }

    public OldCompilerDAO getOldCompilerDAO() throws DAOException {
        return null;
    }

    public LookupDAO getLookupDAO() throws DAOException {
        return null;
    }

    public LookupPathStepDAO getLookupPathStepDAO() throws DAOException {
        return null;
    }

    public ExportDAO getExportDAO() throws DAOException {
        return null;
    }

    public MigrateDAO getMigrateDAO() throws DAOException {
        return null;
    }

    public DependencyCheckerDAO getDependencyCheckerDAO()
        throws DAOException {
        return null;
    }

    public NextKeyDAO getNextKeyDAO() throws DAOException {
        return null;
    }

    public StoredProcedureDAO getStoredProcedureDAO() throws DAOException {
        return null;
    }

    public ConnectionParameters getConnectionParameters() {
        return null;
    }

    public Connection reconnect() throws DAOException {
        return null;
    }

    public Connection getConnection() throws DAOException {
        return null;
    }

    public DAOUOW getDAOUOW() {
        return null;
    }

	@Override
	public UserSessionParameters getSAFRLogin() {
		// TODO Auto-generated method stub
		return null;
	}       
}
