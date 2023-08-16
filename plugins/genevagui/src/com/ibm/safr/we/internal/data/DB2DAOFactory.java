package com.ibm.safr.we.internal.data;

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


import java.sql.Connection;

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactory;
import com.ibm.safr.we.data.DAOUOW;
import com.ibm.safr.we.data.UserSessionParameters;
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
import com.ibm.safr.we.internal.data.dao.DB2CodeSetDAO;
import com.ibm.safr.we.internal.data.dao.DB2ControlRecordDAO;
import com.ibm.safr.we.internal.data.dao.DB2DependencyCheckerDAO;
import com.ibm.safr.we.internal.data.dao.DB2EnvironmentDAO;
import com.ibm.safr.we.internal.data.dao.DB2ExportDAO;
import com.ibm.safr.we.internal.data.dao.DB2GroupDAO;
import com.ibm.safr.we.internal.data.dao.DB2HeaderFooterDAO;
import com.ibm.safr.we.internal.data.dao.DB2LRFieldDAO;
import com.ibm.safr.we.internal.data.dao.DB2LogicalFileDAO;
import com.ibm.safr.we.internal.data.dao.DB2LogicalRecordDAO;
import com.ibm.safr.we.internal.data.dao.DB2LookupDAO;
import com.ibm.safr.we.internal.data.dao.DB2LookupPathStepDAO;
import com.ibm.safr.we.internal.data.dao.DB2MigrateDAO;
import com.ibm.safr.we.internal.data.dao.DB2NextKeyDAO;
import com.ibm.safr.we.internal.data.dao.DB2OldCompilerDAO;
import com.ibm.safr.we.internal.data.dao.DB2PhysicalFileDAO;
import com.ibm.safr.we.internal.data.dao.DB2ReportsDAO;
import com.ibm.safr.we.internal.data.dao.DB2StoredProcedureDAO;
import com.ibm.safr.we.internal.data.dao.DB2UserDAO;
import com.ibm.safr.we.internal.data.dao.DB2UserExitRoutineDAO;
import com.ibm.safr.we.internal.data.dao.DB2ViewColumnDAO;
import com.ibm.safr.we.internal.data.dao.DB2ViewColumnSourceDAO;
import com.ibm.safr.we.internal.data.dao.DB2ViewDAO;
import com.ibm.safr.we.internal.data.dao.DB2ViewFolderDAO;
import com.ibm.safr.we.internal.data.dao.DB2ViewLogicDependencyDAO;
import com.ibm.safr.we.internal.data.dao.DB2ViewSortKeyDAO;
import com.ibm.safr.we.internal.data.dao.DB2ViewSourceDAO;

import java.util.logging.Level;
import java.util.logging.Logger;


public class DB2DAOFactory implements DAOFactory {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.DB2DAOFactory");

	ConnectionFactory _conFact;
	UserSessionParameters _safrLogin;
	DB2DAOUOW _uow;

	public DB2DAOFactory(ConnectionParameters params) throws DAOException {
		_conFact = new ConnectionFactory(params);
	}

	public EnvironmentDAO getEnvironmentDAO() throws DAOException {
		return new DB2EnvironmentDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public UserDAO getUserDAO() throws DAOException {
		// return the user DAO to handle a SAFR User
		return new DB2UserDAO(_conFact.getConnection(), _conFact.getParams(),
				_safrLogin);
	}

	public void setSAFRLogin(UserSessionParameters safrLogin) {
		_safrLogin = safrLogin;
	}

	public GroupDAO getGroupDAO() throws DAOException {
		return new DB2GroupDAO(_conFact.getConnection(), _conFact.getParams(),
				_safrLogin);
	}

	public ViewDAO getViewDAO() throws DAOException {
		return new DB2ViewDAO(_conFact.getConnection(), _conFact.getParams(),
				_safrLogin);
	}

	public ViewSourceDAO getViewSourceDAO() throws DAOException {
		return new DB2ViewSourceDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ViewColumnDAO getViewColumnDAO() throws DAOException {
		return new DB2ViewColumnDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ViewColumnSourceDAO getViewColumnSourceDAO() throws DAOException {
		return new DB2ViewColumnSourceDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ViewSortKeyDAO getViewSortKeyDAO() throws DAOException {
		return new DB2ViewSortKeyDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ViewLogicDependencyDAO getViewLogicDependencyDAO()
			throws DAOException {
		return new DB2ViewLogicDependencyDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ViewFolderDAO getViewFolderDAO() throws DAOException {
		return new DB2ViewFolderDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ControlRecordDAO getControlRecordDAO() throws DAOException {
		return new DB2ControlRecordDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public CodeSetDAO getCodeSetDAO() throws DAOException {
		return new DB2CodeSetDAO(_conFact.getConnection(),
				_conFact.getParams(), _safrLogin);
	}

	public PhysicalFileDAO getPhysicalFileDAO() throws DAOException {
		return new DB2PhysicalFileDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ConnectionParameters getConnectionParameters() {
		return _conFact.getParams();
	}

	public Connection getConnection() throws DAOException {
		return _conFact.getConnection();
	}

	public UserSessionParameters getSAFRLogin() {
		return _safrLogin;
	}

	public Connection reconnect() throws DAOException {
		return _conFact.reconnect();
	}

	public UserExitRoutineDAO getUserExitRoutineDAO() throws DAOException {
		// return the user DAO to handle a SAFR User
		return new DB2UserExitRoutineDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public LogicalRecordDAO getLogicalRecordDAO() throws DAOException {
		// return the logical record DAO to handle a SAFR Logical record.
		return new DB2LogicalRecordDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public LogicalFileDAO getLogicalFileDAO() throws DAOException {
		// return the Logical File DAO to handle a SAFR LogicalFile.
		return new DB2LogicalFileDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public LookupDAO getLookupDAO() throws DAOException {
		// return the lookup DAO to handle a SAFR lookup.
		return new DB2LookupDAO(_conFact.getConnection(), _conFact.getParams(),
				_safrLogin);
	}

	public OldCompilerDAO getOldCompilerDAO() throws DAOException {
		// return the lookup DAO to handle a SAFR lookup.
		return new DB2OldCompilerDAO(_conFact.getConnection(), _conFact.getParams(),
				_safrLogin);
	}
	
	public LookupPathStepDAO getLookupPathStepDAO() throws DAOException {
		// return the lookup DAO to handle a SAFR lookup.
		return new DB2LookupPathStepDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public LRFieldDAO getLRFieldDAO() throws DAOException {
		// return the LRField DAO to handle a SAFR LRField
		return new DB2LRFieldDAO(_conFact.getConnection(),
				_conFact.getParams(), _safrLogin);
	}

	public ExportDAO getExportDAO() throws DAOException {
		// return the Export DAO to handle a SAFR Export Utility.
		return new DB2ExportDAO(_conFact.getConnection(), _conFact.getParams(),
				_safrLogin);
	}

	public MigrateDAO getMigrateDAO() throws DAOException {
		// return the Migrate DAO to handle SAFR Component Migration.
		return new DB2MigrateDAO(_conFact.getConnection(),
				_conFact.getParams(), _safrLogin);
	}

	public HeaderFooterDAO getHeaderFooterDAO() throws DAOException {
		// return the Header Footer DAO to handle the View header footer items
		return new DB2HeaderFooterDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public DependencyCheckerDAO getDependencyCheckerDAO() throws DAOException {
		// return the DependencyChecker DAO to handle Dependency checker
		return new DB2DependencyCheckerDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public NextKeyDAO getNextKeyDAO() throws DAOException {
		// return the NextKey DAO to access next key id info
		return new DB2NextKeyDAO(_conFact.getConnection(), _conFact
				.getParams());
	}

    public StoredProcedureDAO getStoredProcedureDAO() throws DAOException {
        // return the Stored Procedure DAO to access stored procedure info
        return new DB2StoredProcedureDAO(_conFact.getConnection(), _conFact
                .getParams());
    }

	
	public void disconnect() throws DAOException {
		_conFact.disconnect();
	}

	public DAOUOW getDAOUOW() {
		if (_uow == null) {
			_uow = new DB2DAOUOW();
		}
		return _uow;
	}

	@Override
	public ReportsDAO getReportsDAO() {
		return new DB2ReportsDAO(_conFact.getConnection(), _conFact.getParams(), _safrLogin);
	}

}
