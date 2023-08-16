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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOAuthorizationException;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOW;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.DBType;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.utilities.SAFRLogger;

import java.net.URL;
import java.net.URLClassLoader;

public class ConnectionFactory {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.ConnectionFactory");

	private ConnectionParameters _params;
	private Connection _con = null;

	public ConnectionFactory(ConnectionParameters params) throws DAOException {
		_params = params;
		loadDriver();
	}

	Connection getConnection() throws DAOException {
		try {
			if (_con == null) {
				if (_params.getType() == DBType.PostgresQL) {
					String url = "jdbc:postgresql://" 
							+ _params.getServer() +":" + _params.getPort() + "/" + _params.getDatabase()
							+ "?user=" + _params.getUserName() 
							+ "&password=" +_params.getPassWord() 
							+ "&ssl=false"
							+ "&currentSchema=" + _params.getSchema();
						return _con  = DriverManager.getConnection(url);
				} else if(_params.getType() == DBType.Db2) {
					String s = "Workbench Database Connection Opened" + SAFRUtilities.LINEBREAK
							+ "URL    " +_params.getUrl() + SAFRUtilities.LINEBREAK 
							+ "Schema " + _params.getSchema() + SAFRUtilities.LINEBREAK
							+ "Userid " + _params.getUserName();
						SAFRLogger.logAllSeparator(logger,Level.INFO, s);
						_con = DriverManager.getConnection(_params.getUrl(), _params
								.getUserName(), _params.getPassWord());
						SAFRApplication.setDatabaseSchema(_params.getSchema());
				} else {
					throw new DAOException("Unknown Database type "
							+ _params.getType());
				}
			}
			return _con;
		} catch (SQLException e) {
			// SQLState 28000 is ISO standard authorization error
			if (e.getSQLState() != null && e.getSQLState().equals("28000")) {
				throw new DAOAuthorizationException(e.getMessage(), e);
			} else {
				throw DataUtilities.createDAOException(e.getMessage(), e);
			}
		} catch (Exception e) {
			throw DataUtilities.createDAOException(
					"Cannot connect to database.", e);
		}
	}

	void loadDriver() throws DAOException {
		try {
			if (_params.getType() == DBType.Db2) {
				// Load the DB2 JDBC Type 4 Driver with DriverManager
				Class.forName("com.ibm.db2.jcc.DB2Driver");
			} else if(_params.getType() == DBType.PostgresQL)  {
			} else {
				throw new DAOException("Unknown Database type "
						+ _params.getType());
			}
		} catch (ClassNotFoundException e) {
			throw DataUtilities
					.createDAOException(
							"Database error occurred while loading Db2 JDBC Driver.",
							e);
		}
	}

	public ConnectionParameters getParams() {
		return _params;
	}

	Connection reconnect() throws DAOException {
		DAOUOW uow = DAOFactoryHolder.getDAOFactory().getDAOUOW();
		_con = null;

		int count = 0;
		// try to reconnect three times.
		while (count < 3) {
			if (count == 2) {
				// for the third attempt wait for 5
				// seconds before reconnecting
				try {
					Thread.sleep(5000l);
				} catch (InterruptedException e) {
					logger.severe(e.getMessage());;
				}
			}
			count++;
			try {
				getConnection();
				if (uow.inProgress()) {
					uow.stopProgress();
					throw new DAOUOWInterruptedException(
							"Database transaction interrupted by a reconnect.");
				}
				break;
			} catch (DAOAuthorizationException de) {
				// don't retry with authorization exceptions
				uow.stopProgress(); // reset the state of any unit of work
				throw new DAOAuthorizationException(
						"Cannot reconnect to database.", de);
			} catch (DAOException de) {
				if (de instanceof DAOUOWInterruptedException) {
					throw de;
				}
				// if not able to connect after three attempts
				// then throw exception.
				if (count == 3) {
					uow.stopProgress(); // reset the state of any unit of work
					throw DataUtilities.createDAOException(
							"Cannot reconnect to database.", de);
				}
			}
		}

		return _con;
	}

	void disconnect() throws DAOException {
		if (_con != null) {
			try {
                SAFRLogger.logAllSeparator(logger,Level.INFO,"Previous Workbench Database Connection Closed");
				_con.close();
				SAFRLogger.logEnd(logger);
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "", e);
				throw DataUtilities
						.createDAOException(
								"Error occurred while disconnecting the active database connection.",
								e);
			}
		}
	}
}
