package com.ibm.safr.we.internal.data;

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
import com.ibm.safr.we.internal.data.pgdao.PGCodeSetDAO;
import com.ibm.safr.we.internal.data.pgdao.PGControlRecordDAO;
import com.ibm.safr.we.internal.data.pgdao.PGDependencyCheckerDAO;
import com.ibm.safr.we.internal.data.pgdao.PGEnvironmentDAO;
import com.ibm.safr.we.internal.data.pgdao.PGExportDAO;
import com.ibm.safr.we.internal.data.pgdao.PGGroupDAO;
import com.ibm.safr.we.internal.data.pgdao.PGHeaderFooterDAO;
import com.ibm.safr.we.internal.data.pgdao.PGLRFieldDAO;
import com.ibm.safr.we.internal.data.pgdao.PGLogicalFileDAO;
import com.ibm.safr.we.internal.data.pgdao.PGLogicalRecordDAO;
import com.ibm.safr.we.internal.data.pgdao.PGLookupDAO;
import com.ibm.safr.we.internal.data.pgdao.PGLookupPathStepDAO;
import com.ibm.safr.we.internal.data.pgdao.PGMigrateDAO;
import com.ibm.safr.we.internal.data.pgdao.PGNextKeyDAO;
import com.ibm.safr.we.internal.data.pgdao.PGOldCompilerDAO;
import com.ibm.safr.we.internal.data.pgdao.PGPhysicalFileDAO;
import com.ibm.safr.we.internal.data.pgdao.PGStoredProcedureDAO;
import com.ibm.safr.we.internal.data.pgdao.PGUserDAO;
import com.ibm.safr.we.internal.data.pgdao.PGUserExitRoutineDAO;
import com.ibm.safr.we.internal.data.pgdao.PGViewColumnDAO;
import com.ibm.safr.we.internal.data.pgdao.PGViewColumnSourceDAO;
import com.ibm.safr.we.internal.data.pgdao.PGViewDAO;
import com.ibm.safr.we.internal.data.pgdao.PGViewFolderDAO;
import com.ibm.safr.we.internal.data.pgdao.PGViewLogicDependencyDAO;
import com.ibm.safr.we.internal.data.pgdao.PGViewSortKeyDAO;
import com.ibm.safr.we.internal.data.pgdao.PGViewSourceDAO;

public class PGDAOFactory implements DAOFactory {

	ConnectionFactory _conFact;
	UserSessionParameters _safrLogin;
	PGDAOUOW _uow;

	public PGDAOFactory(ConnectionParameters params) throws DAOException {
		_conFact = new ConnectionFactory(params);
	}

	public EnvironmentDAO getEnvironmentDAO() throws DAOException {
		return new PGEnvironmentDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public UserDAO getUserDAO() throws DAOException {
		// return the user DAO to handle a SAFR User
		return new PGUserDAO(_conFact.getConnection(), _conFact.getParams(),
				_safrLogin);
	}

	public void setSAFRLogin(UserSessionParameters safrLogin) {
		_safrLogin = safrLogin;
	}

	public GroupDAO getGroupDAO() throws DAOException {
		return new PGGroupDAO(_conFact.getConnection(), _conFact.getParams(),
				_safrLogin);
	}

	public ViewDAO getViewDAO() throws DAOException {
		return new PGViewDAO(_conFact.getConnection(), _conFact.getParams(),
				_safrLogin);
	}

	public ViewSourceDAO getViewSourceDAO() throws DAOException {
		return new PGViewSourceDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ViewColumnDAO getViewColumnDAO() throws DAOException {
		return new PGViewColumnDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ViewColumnSourceDAO getViewColumnSourceDAO() throws DAOException {
		return new PGViewColumnSourceDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ViewSortKeyDAO getViewSortKeyDAO() throws DAOException {
		return new PGViewSortKeyDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ViewLogicDependencyDAO getViewLogicDependencyDAO()
			throws DAOException {
		return new PGViewLogicDependencyDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ViewFolderDAO getViewFolderDAO() throws DAOException {
		return new PGViewFolderDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public ControlRecordDAO getControlRecordDAO() throws DAOException {
		return new PGControlRecordDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public CodeSetDAO getCodeSetDAO() throws DAOException {
		return new PGCodeSetDAO(_conFact.getConnection(),
				_conFact.getParams(), _safrLogin);
	}

	public PhysicalFileDAO getPhysicalFileDAO() throws DAOException {
		return new PGPhysicalFileDAO(_conFact.getConnection(), _conFact
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
		return new PGUserExitRoutineDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public LogicalRecordDAO getLogicalRecordDAO() throws DAOException {
		// return the logical record DAO to handle a SAFR Logical record.
		return new PGLogicalRecordDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public LogicalFileDAO getLogicalFileDAO() throws DAOException {
		// return the Logical File DAO to handle a SAFR LogicalFile.
		return new PGLogicalFileDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public LookupDAO getLookupDAO() throws DAOException {
		// return the lookup DAO to handle a SAFR lookup.
		return new PGLookupDAO(_conFact.getConnection(), _conFact.getParams(),
				_safrLogin);
	}

	public OldCompilerDAO getOldCompilerDAO() throws DAOException {
		// return the lookup DAO to handle a SAFR lookup.
		return new PGOldCompilerDAO(_conFact.getConnection(), _conFact.getParams(),
				_safrLogin);
	}
	
	public LookupPathStepDAO getLookupPathStepDAO() throws DAOException {
		// return the lookup DAO to handle a SAFR lookup.
		return new PGLookupPathStepDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public LRFieldDAO getLRFieldDAO() throws DAOException {
		// return the LRField DAO to handle a SAFR LRField
		return new PGLRFieldDAO(_conFact.getConnection(),
				_conFact.getParams(), _safrLogin);
	}

	public ExportDAO getExportDAO() throws DAOException {
		// return the Export DAO to handle a SAFR Export Utility.
		return new PGExportDAO(_conFact.getConnection(), _conFact.getParams(),
				_safrLogin);
	}

	public MigrateDAO getMigrateDAO() throws DAOException {
		// return the Migrate DAO to handle SAFR Component Migration.
		return new PGMigrateDAO(_conFact.getConnection(),
				_conFact.getParams(), _safrLogin);
	}

	public HeaderFooterDAO getHeaderFooterDAO() throws DAOException {
		// return the Header Footer DAO to handle the View header footer items
		return new PGHeaderFooterDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public DependencyCheckerDAO getDependencyCheckerDAO() throws DAOException {
		// return the DependencyChecker DAO to handle Dependency checker
		return new PGDependencyCheckerDAO(_conFact.getConnection(), _conFact
				.getParams(), _safrLogin);
	}

	public NextKeyDAO getNextKeyDAO() throws DAOException {
		// return the NextKey DAO to access next key id info
		return new PGNextKeyDAO(_conFact.getConnection(), _conFact
				.getParams());
	}

    public StoredProcedureDAO getStoredProcedureDAO() throws DAOException {
        // return the Stored Procedure DAO to access stored procedure info
        return new PGStoredProcedureDAO(_conFact.getConnection(), _conFact
                .getParams());
    }

	
	public void disconnect() throws DAOException {
		_conFact.disconnect();
	}

	public DAOUOW getDAOUOW() {
		if (_uow == null) {
			_uow = new PGDAOUOW();
		}
		return _uow;
	}

}
