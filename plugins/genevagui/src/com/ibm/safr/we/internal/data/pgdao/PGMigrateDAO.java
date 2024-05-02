package com.ibm.safr.we.internal.data.pgdao;

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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.MigrateDAO;

public class PGMigrateDAO implements MigrateDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGMigrateDAO");

	private static final String TABLE_NAME = "MIGAUDIT";
	
	private static final String COL_SRCENVIRONID = "SRCENVIRONID";
	private static final String COL_DESTENVIRONID = "DESTENVIRONID";
	private static final String COL_ENTITYTYPE = "ENTITYTYPE";
	private static final String COL_ENTITYID = "ENTITYID";
	private static final String COL_ENTITYNAME = "ENTITYNAME";
	private static final String COL_MIGRATIONDATE = "MIGRATIONDATE";
	private static final String COL_MIGRATEDBY = "MIGRATEDBY";
	private static final String COL_MESSAGETEXT = "MESSAGETEXT";
	
	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;

	/**
	 * Constructor for this class
	 * 
	 * @param con
	 *            : The connection set for database access.
	 * @param params
	 *            : The connection parameters which define the URL, userId and
	 *            other details of the connection.
	 * @param safrLogin
	 *            : The parameters related to the user who has logged into the
	 *            workbench.
	 */
	public PGMigrateDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	private String getComponentDBLabel(ComponentType type)
	{
		switch (type)
		{
		case ControlRecord :
			return "CONTROL RECORD"; 
		case UserExitRoutine : 
			return "PROCEDURE";
		case LookupPath :
			return "JOIN";
		case LogicalFile :
			return "LOGICAL FILE";
		case LogicalRecord :
			return "LOGICAL RECORD";
		case PhysicalFile :
			return "PHYSICAL FILE";
		case ViewFolder :
			return "VIEW FOLDER";
		case View :
			return "VIEW";
		default : {
			logger.log(Level.SEVERE, "Invalid ComponentType for migration");
			return null;
		}
		}
	}
	
	public void logMigration(Integer srcEnv, Integer destEnv,
			ComponentType compType, Integer componentId, String componentName)
			throws DAOException {
		try {
			String statement = "Insert Into " + params.getSchema() + "." + TABLE_NAME +
					" (" + COL_SRCENVIRONID + "," + COL_DESTENVIRONID + "," + COL_ENTITYTYPE +
					"," + COL_ENTITYID + "," + COL_ENTITYNAME + "," + COL_MIGRATIONDATE +
					"," + COL_MIGRATEDBY + "," + COL_MESSAGETEXT + 
					") Values (?,?,?,?,?,CURRENT_TIMESTAMP,?,?)";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);

					int i = 1;
					pst.setInt(i++, srcEnv);
					pst.setInt(i++, destEnv);
					pst.setString(i++, getComponentDBLabel(compType));
					pst.setInt(i++, componentId);
					pst.setString(i++, componentName);
					pst.setString(i++, safrLogin.getUserId());
					pst.setString(i++, "");
					pst.executeUpdate();
					break;
				} catch (SQLException se) {
					if (con.isClosed()) {
						// lost database connection, so reconnect and retry
						con = DAOFactoryHolder.getDAOFactory().reconnect();
					} else {
						throw se;
					}
				}
			}
			pst.close();

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while creating a Migration log.",e);
		}
	}
		
}
