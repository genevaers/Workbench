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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.UserExitRoutineDAO;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.internal.data.PGSQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;

/**
 * This class implements interface <b>UserExitRoutineDAO</b>. 
 * It provides the database access for model class UserExitRoutine.
 */
public class PGUserExitRoutineDAO implements UserExitRoutineDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGUserExitRoutineDAO");

	private static final String TABLE_NAME = "EXIT";
	
	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "EXITID";
    private static final String COL_NAME = "NAME";
    private static final String COL_EXECUTABLE = "MODULEID";
	private static final String COL_TYPE = "EXITTYPECD";
	private static final String COL_LANGUAGE = "PROGRAMTYPECD";
    private static final String COL_OPTIMIZED = "OPTIMIZEIND";
	private static final String COL_COMMENT = "COMMENTS";
	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";
	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private PGSQLGenerator generator = new PGSQLGenerator();

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
	public PGUserExitRoutineDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	public UserExitRoutineTransfer getUserExitRoutine(Integer id,
			Integer environmentId) throws DAOException {

		UserExitRoutineTransfer result = null;
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			idNames.add(COL_ENVID);

			String selectString = generator.getSelectStatement(params
					.getSchema(), TABLE_NAME, idNames, null);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, id);
					pst.setInt(2, environmentId);
					rs = pst.executeQuery();
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
			if (rs.next()) {
				result = generateTransfer(rs);
			} else {
				logger.info("No such User Exit Routine in Env " + environmentId + " with ID : " + id);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the User Exit Routine with id ["+ id + "]", e);
		}
		return result;
	}

	public List<UserExitRoutineQueryBean> queryAllUserExitRoutines(
			Integer environmentId, SortType sortType) throws DAOException {
		List<UserExitRoutineQueryBean> result = new ArrayList<UserExitRoutineQueryBean>();

        boolean admin = SAFRApplication.getUserSession().isSystemAdministrator(); 
		
		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "A.EXITID";
		} else {
			orderString = "UPPER(A.NAME)";
		}

		try {
			String selectString = "";
			if (admin) {
				selectString = "SELECT A.EXITID, A.NAME, A.MODULEID, A.EXITTYPECD, A.PROGRAMTYPECD, "
						+ "A.CREATEDTIMESTAMP,A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID FROM "
						+ params.getSchema() + ".EXIT A "
						+ "WHERE A.ENVIRONID = ? "
						+ " AND A.EXITID > 0 ORDER BY " + orderString;
			} else {
				selectString = "SELECT A.EXITID, A.NAME, A.MODULEID, A.EXITTYPECD, A.PROGRAMTYPECD, L.RIGHTS, "
						+ "A.CREATEDTIMESTAMP,A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID FROM "
						+ params.getSchema() + ".EXIT A "
						+ "LEFT OUTER JOIN " + params.getSchema() + ".SECEXIT L "
						+ "ON A.ENVIRONID = L.ENVIRONID AND A.EXITID = L.EXITID "
						+ " AND L.GROUPID= ? "
						+ "WHERE A.ENVIRONID = ? "
						+ " AND A.EXITID > 0 " + " ORDER BY " + orderString;
			}

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					if (admin) {
						pst.setInt(1, environmentId );
					} else {
						pst.setInt(1, SAFRApplication.getUserSession().getGroup().getId() );
						pst.setInt(2, environmentId );
					}
					rs = pst.executeQuery();
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
			while (rs.next()) {
				UserExitRoutineQueryBean userExitRoutineQueryBean = new UserExitRoutineQueryBean(
					environmentId, rs.getInt(COL_ID), 
					DataUtilities.trimString(rs.getString(COL_NAME)),
					DataUtilities.trimString(rs.getString(COL_EXECUTABLE)),
					DataUtilities.trimString(rs.getString(COL_TYPE)),
					DataUtilities.trimString(rs.getString(COL_LANGUAGE)),
					admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.UserExitRoutine, environmentId), 
					rs.getDate(COL_CREATETIME), 
					DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
					rs.getDate(COL_MODIFYTIME), 
					DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

				result.add(userExitRoutineQueryBean);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying all User Exit Routines.",e);
		}
	}

	public List<UserExitRoutineQueryBean> queryUserExitRoutines(
			Integer environmentId, String typeCodeKey, SortType sortType)
			throws DAOException {
		List<UserExitRoutineQueryBean> result = new ArrayList<UserExitRoutineQueryBean>();
		try {
			boolean admin = SAFRApplication.getUserSession().isSystemAdministrator();

			String orderString = null;
			if (sortType.equals(SortType.SORT_BY_ID)) {
				orderString = "A.EXITID";
			} else {
				orderString = "UPPER(A.NAME)";
			}

			String selectString = "";
			if (admin) {
				selectString = "SELECT A.EXITID, A.NAME, A.MODULEID, A.EXITTYPECD, A.PROGRAMTYPECD, "
						+ "A.CREATEDTIMESTAMP,A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID FROM "
						+ params.getSchema() + ".EXIT A "
						+ "WHERE A.ENVIRONID = ? " 
						+ " AND A.EXITID > 0 AND A.EXITTYPECD = ? ORDER BY "
						+ orderString;
			} else {

				selectString = "SELECT A.EXITID, A.NAME, A.MODULEID, A.EXITTYPECD, A.PROGRAMTYPECD, L.RIGHTS, "
						+ "A.CREATEDTIMESTAMP,A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID FROM "
						+ params.getSchema() + ".EXIT A "
						+ "LEFT OUTER JOIN "
						+ params.getSchema() + ".SECEXIT L "
						+ "ON A.ENVIRONID = L.ENVIRONID AND A.EXITID = L.EXITID"
						+ " AND L.GROUPID = ? "
						+ "WHERE A.ENVIRONID = ? "
						+ " AND A.EXITID > 0  AND A.EXITTYPECD = ?"
						+ " ORDER BY " + orderString;
			}

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					if (admin == false) {
						pst.setInt(ndx++, SAFRApplication.getUserSession().getGroup().getId());
					}
					pst.setInt(ndx++, environmentId);
					pst.setString(ndx, typeCodeKey);
					rs = pst.executeQuery();
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
			while (rs.next()) {

				UserExitRoutineQueryBean userExitRoutineQueryBean = new UserExitRoutineQueryBean(
					environmentId, rs.getInt(COL_ID), 
					DataUtilities.trimString(rs.getString(COL_NAME)),
					DataUtilities.trimString(rs.getString(COL_EXECUTABLE)),
                    DataUtilities.trimString(rs.getString(COL_TYPE)),
                    DataUtilities.trimString(rs.getString(COL_LANGUAGE)),
                    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.UserExitRoutine, environmentId), 
					rs.getDate(COL_CREATETIME), 
					DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
					rs.getDate(COL_MODIFYTIME), 
					DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

				result.add(userExitRoutineQueryBean);
			}
			pst.close();
			rs.close();
			return result;
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying all User Exit Routines of a particular typecode.",e);
		}
	}

	/**
	 * This method is used to generate a transfer object for the User Exit
	 * Routine.
	 * 
	 * @param rs
	 *            The result set of a database query run on EXIT table
	 *            with which the values for the transfer objects are set.
	 * @return A transfer object for the User Exit Routine with values set
	 *         according to the result set.
	 * @throws SQLException
	 */
	private UserExitRoutineTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		UserExitRoutineTransfer userExitRoutineTransfer = new UserExitRoutineTransfer();
		userExitRoutineTransfer.setEnvironmentId(rs.getInt(COL_ENVID));
		userExitRoutineTransfer.setId(rs.getInt(COL_ID));
		userExitRoutineTransfer.setName(DataUtilities.trimString(rs.getString(COL_NAME)));
		userExitRoutineTransfer.setExecutable(DataUtilities.trimString(rs.getString(COL_EXECUTABLE)));
		userExitRoutineTransfer.setTypeCode(DataUtilities.trimString(rs.getString(COL_TYPE)));
		userExitRoutineTransfer.setLanguageCode(DataUtilities.trimString(rs.getString(COL_LANGUAGE)));
		userExitRoutineTransfer.setOptimize(DataUtilities.intToBoolean(rs.getInt(COL_OPTIMIZED)));
		userExitRoutineTransfer.setComments(DataUtilities.trimString(rs.getString(COL_COMMENT)));
		userExitRoutineTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
		userExitRoutineTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		userExitRoutineTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
		userExitRoutineTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

		return userExitRoutineTransfer;
	}

	public UserExitRoutineTransfer persistUserExitRoutine(
			UserExitRoutineTransfer userExitRoutineTransfer)
			throws DAOException, SAFRNotFoundException {

		if (!userExitRoutineTransfer.isPersistent()) {
			return (createUserExitRoutine(userExitRoutineTransfer));
		} else {
			return (updateUserExitRoutine(userExitRoutineTransfer));
		}

	}

	/**
	 * This method is used to create a User Exit Routine in EXIT in the
	 * database.
	 * 
	 * @param userExitRoutineTransfer
	 * @return The transfer object which contains the values retrieved from
	 *         EXIT for the User Exit Routine.
	 * @throws DAOException
	 */
	private UserExitRoutineTransfer createUserExitRoutine(
			UserExitRoutineTransfer userExitRoutineTransfer)
			throws DAOException {
		boolean isImportOrMigrate = userExitRoutineTransfer.isForImport()
				|| userExitRoutineTransfer.isForMigration() ? true : false;
		boolean useCurrentTS = !isImportOrMigrate;
		
		try {
			String[] columnNames = { COL_ENVID, COL_NAME, COL_TYPE,
					COL_LANGUAGE, COL_COMMENT, COL_EXECUTABLE, COL_OPTIMIZED, COL_CREATETIME,
					COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
            if (isImportOrMigrate) {
                names.add(1, COL_ID);
            }
			String statement = generator.getInsertStatement(params.getSchema(),TABLE_NAME, COL_ID, names, useCurrentTS);
			PreparedStatement pst = null;
            ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int i = 1;
					pst.setInt(i++, userExitRoutineTransfer.getEnvironmentId());
		            if (isImportOrMigrate) {
		                pst.setInt(i++, userExitRoutineTransfer.getId());
		            }
					pst.setString(i++, userExitRoutineTransfer.getName());
					pst.setString(i++, userExitRoutineTransfer.getTypeCode());
					pst.setString(i++, userExitRoutineTransfer
							.getLanguageCode());
					pst.setString(i++, userExitRoutineTransfer.getComments());
					pst.setString(i++, userExitRoutineTransfer.getExecutable());
					pst.setInt(i++, DataUtilities.booleanToInt(userExitRoutineTransfer.isOptimize()));
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(userExitRoutineTransfer.getCreateTime()));
					}
					pst.setString(i++,isImportOrMigrate ? userExitRoutineTransfer.getCreateBy() : safrLogin.getUserId());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(userExitRoutineTransfer.getModifyTime()));
					}
					pst.setString(i++,isImportOrMigrate ? userExitRoutineTransfer.getModifyBy() : safrLogin.getUserId());
					rs = pst.executeQuery();
                    rs.next();
                    int id = rs.getInt(1); 
                    userExitRoutineTransfer.setPersistent(true);
                    userExitRoutineTransfer.setId(id);
                    if (!isImportOrMigrate) {
                        userExitRoutineTransfer.setCreateBy(safrLogin.getUserId());
                        userExitRoutineTransfer.setCreateTime(rs.getDate(2));
                        userExitRoutineTransfer.setModifyBy(safrLogin.getUserId());
                        userExitRoutineTransfer.setModifyTime(rs.getDate(3));
                    }                   					
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
			throw DataUtilities.createDAOException("Database error occurred while creating a new User Exit Routine.",e);
		}

		return userExitRoutineTransfer;

	}

	/**
	 * This method is used to update a User Exit Routine in EXIT of the
	 * database.
	 * 
	 * @param userExitRoutineTransfer
	 *            The transfer object which contains the values to be set in the
	 *            fields for the corresponding User Exit Routine being updated.
	 * @return The transfer object which contains the values retrieved from
	 *         EXIT for the User Exit Routine.
	 * @throws DAOException
	 * @throws SAFRNotFoundException
	 */

	private UserExitRoutineTransfer updateUserExitRoutine(
			UserExitRoutineTransfer userExitRoutineTransfer)
			throws DAOException, SAFRNotFoundException {

		boolean isImportOrMigrate = userExitRoutineTransfer.isForImport()
				|| userExitRoutineTransfer.isForMigration() ? true : false;
		boolean useCurrentTS = !isImportOrMigrate;

		try {
			String[] columnNames = { COL_NAME, COL_TYPE, COL_LANGUAGE,
					COL_EXECUTABLE, COL_OPTIMIZED, COL_COMMENT };
			List<String> temp = Arrays.asList(columnNames);
			List<String> names = new ArrayList<String>();
			names.addAll(temp);

			if (isImportOrMigrate) {
				names.add(COL_CREATETIME);
				names.add(COL_CREATEBY);
				names.add(COL_MODIFYTIME);
				names.add(COL_MODIFYBY);
			} else {
				names.add(COL_MODIFYTIME);
				names.add(COL_MODIFYBY);
			}

			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			idNames.add(COL_ENVID);

			String statement = generator.getUpdateStatement(params.getSchema(),
					TABLE_NAME, names, idNames, useCurrentTS);
			PreparedStatement pst = null;
			while (true) {
				try {

					pst = con.prepareStatement(statement);

					int i = 1;

					pst.setString(i++, userExitRoutineTransfer.getName());
					pst.setString(i++, userExitRoutineTransfer.getTypeCode());
					pst.setString(i++, userExitRoutineTransfer.getLanguageCode());
					pst.setString(i++, userExitRoutineTransfer.getExecutable());
					pst.setInt(i++, DataUtilities.booleanToInt(userExitRoutineTransfer.isOptimize()));
					pst.setString(i++, userExitRoutineTransfer.getComments());
					if (isImportOrMigrate) {
						// createby and lastmod set from source component
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(userExitRoutineTransfer.getCreateTime()));
						pst.setString(i++, userExitRoutineTransfer.getCreateBy());
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(userExitRoutineTransfer.getModifyTime()));
						pst.setString(i++, userExitRoutineTransfer.getModifyBy());
					} else {
						// createby details are untouched
						// lastmodtimestamp is CURRENT_TIMESTAMP
						// lastmoduserid is logged in user
						pst.setString(i++, safrLogin.getUserId());
					}
					pst.setInt(i++, userExitRoutineTransfer.getId());
					pst.setInt(i++, userExitRoutineTransfer.getEnvironmentId());
                    if (useCurrentTS) {
                        ResultSet rs = pst.executeQuery();
                        rs.next();
                        userExitRoutineTransfer.setModifyTime(rs.getDate(1));  
                        userExitRoutineTransfer.setModifyBy(safrLogin.getUserId());
                        rs.close();                 
                        pst.close();
                    } else {
                        int count  = pst.executeUpdate();   
                        if (count == 0) {
                            throw new SAFRNotFoundException("No Rows updated.");
                        }                       
                        pst.close();
                    }                   					
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
			return (userExitRoutineTransfer);

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while updating the User Exit Routine.",e);
		}

	}

	public List<UserExitRoutineTransfer> getAllUserExitRoutines(
			Integer environmentId) throws DAOException {
		List<UserExitRoutineTransfer> result = new ArrayList<UserExitRoutineTransfer>();

		try {
			List<String> orderBy = new ArrayList<String>();
			orderBy.add(COL_ID);
			String selectString = generator.getAllMetadataComponent(params
					.getSchema(), TABLE_NAME, environmentId, orderBy, COL_ID);

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					rs = pst.executeQuery();
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

			while (rs.next()) {
				result.add(generateTransfer(rs));
			}
			pst.close();
			rs.close();

			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving all User Exit Routines.",e);
		}
	}

	public UserExitRoutineTransfer getDuplicateUserExitRoutine(
			String userExitRoutineName, Integer userExitRoutineId,
			Integer environmentId) throws DAOException {
		UserExitRoutineTransfer userExitRoutineTransfer = null;
		try {
			String statement = generator.getDuplicateComponent(params
					.getSchema(), TABLE_NAME, COL_ENVID, COL_NAME, COL_ID);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int i = 1;
					pst.setInt(i++, environmentId);
					pst.setString(i++, userExitRoutineName.toUpperCase());
					pst.setInt(i++, userExitRoutineId);

					rs = pst.executeQuery();
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
			if (rs.next()) {
				userExitRoutineTransfer = generateTransfer(rs);
				logger.info("Existing User Exit Routine with name '" + userExitRoutineName
						+ "' found in Environment [" + environmentId + "]");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving a duplicate User Exit Routine.",e);
		}
		return userExitRoutineTransfer;

	}

	public UserExitRoutineTransfer getDuplicateUserExitExecutable(
			String userExitRoutineExecutable, Integer userExitRoutineId,
			Integer environmentId) throws DAOException {
		UserExitRoutineTransfer userExitRoutineTransfer = null;
		try {
			String statement = generator
					.getDuplicateComponent(params.getSchema(), TABLE_NAME,
							COL_ENVID, COL_EXECUTABLE, COL_ID);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int i = 1;
					pst.setInt(i++, environmentId);
					pst.setString(i++, userExitRoutineExecutable.toUpperCase());
					pst.setInt(i++, userExitRoutineId);
					rs = pst.executeQuery();
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
			if (rs.next()) {
				userExitRoutineTransfer = generateTransfer(rs);
				logger.info("Existing User Exit Routine with Executable name '" + userExitRoutineExecutable
						+ "' found in Environment [" + environmentId + "]");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving a User-Exit Routine with the specified executable.",e);
		}
		return userExitRoutineTransfer;

	}

	public void removeUserExitRoutine(Integer id, Integer environmentId)
			throws DAOException {
		boolean success = false;
		try {
			while (!success) {
				try {
					// Begin Transaction
					DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
					List<String> idNames = new ArrayList<String>();
					idNames.add(COL_ID);
					idNames.add(COL_ENVID);
					// First remove its association with any group.

					String deleteAssocQuery = generator.getDeleteStatement(
							params.getSchema(), "SECEXIT", idNames);
					PreparedStatement pst = null;

					while (true) {
						try {
							pst = con.prepareStatement(deleteAssocQuery);
							pst.setInt(1, id);
							pst.setInt(2, environmentId);
							pst.execute();
							break;
						} catch (SQLException se) {
							if (con.isClosed()) {
								// lost database connection, so reconnect and
								// retry
								con = DAOFactoryHolder.getDAOFactory()
										.reconnect();
							} else {
								throw se;
							}
						}
					}

					pst.close();

					String statement = generator.getDeleteStatement(
							params.getSchema(), TABLE_NAME, idNames);
					pst = con.prepareStatement(statement);

					pst.setInt(1, id);
					pst.setInt(2, environmentId);
					pst.execute();
					pst.close();
					success = true;
				} catch (DAOUOWInterruptedException e) {
					// UOW interrupted so retry it
					if (DAOFactoryHolder.getDAOFactory().getDAOUOW()
							.isMultiComponentScope()) {
						throw e;
					} else {
						continue;
					}
				} catch (SQLException e) {
					throw DataUtilities.createDAOException("Database error occurred while deleting the User Exit Routine.",e);
				}
			} // end while(!success)
		} finally {
			if (success) {
				// End Transaction.
				DAOFactoryHolder.getDAOFactory().getDAOUOW().end();
			} else {
				// Rollback the transaction.
				DAOFactoryHolder.getDAOFactory().getDAOUOW().fail();
			}
		}
	}

	public Map<ComponentType, List<DependentComponentTransfer>> getUserExitRoutineDependencies(
			Integer environmentId, Integer userExitRoutineId)
			throws DAOException {
		Map<ComponentType, List<DependentComponentTransfer>> dependencies = new HashMap<ComponentType, List<DependentComponentTransfer>>();
		List<DependentComponentTransfer> dependentPFs = new ArrayList<DependentComponentTransfer>();
		List<DependentComponentTransfer> dependentLRs = new ArrayList<DependentComponentTransfer>();
		List<DependentComponentTransfer> dependentViews = new ArrayList<DependentComponentTransfer>();

		try {

			// Getting Physical Files dependent on specified User Exit Routine.
			String selectDependentPFs = "Select PHYFILEID, NAME From "
					+ params.getSchema() + ".PHYFILE Where ENVIRONID =? AND READEXITID =?";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectDependentPFs);
					pst.setInt(1, environmentId);
					pst.setInt(2, userExitRoutineId);
					rs = pst.executeQuery();
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

			while (rs.next()) {
				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rs.getInt("PHYFILEID"));
				depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
				dependentPFs.add(depCompTransfer);
			}
			if (!dependentPFs.isEmpty()) {
				dependencies.put(ComponentType.PhysicalFile, dependentPFs);
			}
			pst.close();
			rs.close();

			// Getting Logical Records dependent on specified User Exit Routine.
			String selectDependentLRs = "Select LOGRECID, NAME From "
					+ params.getSchema() + ".LOGREC Where ENVIRONID =? AND LOOKUPEXITID =?";
			pst = null;
			rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectDependentLRs);
					pst.setInt(1, environmentId);
					pst.setInt(2, userExitRoutineId);
					rs = pst.executeQuery();
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
			while (rs.next()) {
				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rs.getInt("LOGRECID"));
				depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
				dependentLRs.add(depCompTransfer);
			}
			if (!dependentLRs.isEmpty()) {
				dependencies.put(ComponentType.LogicalRecord, dependentLRs);
			}
			pst.close();
			rs.close();

			// Getting Views dependent on specified User Exit Routine.

			// Query 1 : Views in which User Exit Routine is used in View
			// Properties as Write Exit or Format Exit.
			String selectDependentViews = "Select VIEWID, NAME From "
					+ params.getSchema() + ".VIEW Where ENVIRONID =? AND (WRITEEXITID =? OR FORMATEXITID=?)";
			pst = null;
			rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectDependentViews);
					pst.setInt(1, environmentId);
					pst.setInt(2, userExitRoutineId);
					pst.setInt(3, userExitRoutineId);

					rs = pst.executeQuery();
					break;
				} catch (SQLException se) {
					if (con.isClosed()) {
						// lost database connection, so reconnect and retry
						con = DAOFactoryHolder.getDAOFactory

						().reconnect();
					} else {
						throw se;
					}
				}
			}
			while (rs.next()) {
				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rs.getInt("VIEWID"));
				depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
				depCompTransfer.setDependencyInfo("[View Properties]");
				dependentViews.add(depCompTransfer);
			}
			pst.close();
			rs.close();

			// Query 2 : Views in which User Exit Routine is used in View's
			// Logic text.
			selectDependentViews = "Select DISTINCT A.VIEWID, A.NAME, C.COLUMNNUMBER,C.HDRLINE1 From "
					+ params.getSchema() + ".VIEW A, "
					+ params.getSchema() + ".VIEWLOGICDEPEND B,"
					+ params.getSchema() + ".VIEWCOLUMN C,"
					+ params.getSchema() + ".VIEWCOLUMNSOURCE D "
					+ "Where A.ENVIRONID =B.ENVIRONID AND B.ENVIRONID = C.ENVIRONID AND C.ENVIRONID = D.ENVIRONID AND "
					+ "B.PARENTID = D.VIEWCOLUMNSOURCEID AND D.VIEWCOLUMNID=C.VIEWCOLUMNID AND "
					+ "B.VIEWID = C.VIEWID AND C.VIEWID = D.VIEWID AND B.LOGICTYPECD = 2 AND "
					+ "A.VIEWID = B.VIEWID AND A.ENVIRONID =? AND B.EXITID =?";
			pst = null;
			rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectDependentViews);
					pst.setInt(1, environmentId);
					pst.setInt(2, userExitRoutineId);

					rs = pst.executeQuery();
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
			while (rs.next()) {
				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rs.getInt("VIEWID"));
				depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
				String colName = "";
				String heading1 = rs.getString("HDRLINE1");
				if (heading1 != null && !heading1.equals("")) {
					colName = ", " + heading1;
				}
				depCompTransfer.setDependencyInfo("[Col " + rs.getInt("COLUMNNUMBER") + colName + ", Logic Text]");
				dependentViews.add(depCompTransfer);
			}
            pst.close();
            rs.close();
			
            // Getting Views dependent on specified User Exit Routine in output logic.
            selectDependentViews = "SELECT DISTINCT A.VIEWID, A.NAME FROM " +
                params.getSchema() + ".VIEW A, " +
                params.getSchema() + ".VIEWLOGICDEPEND B, " +
                params.getSchema() + ".VIEWSOURCE C " +
                "WHERE A.ENVIRONID=B.ENVIRONID " +
                "AND A.VIEWID=B.VIEWID " +
                "AND B.ENVIRONID=C.ENVIRONID " +
                "AND B.PARENTID=C.VIEWSOURCEID " +
                "AND B.VIEWID=C.VIEWID " +
                "AND B.LOGICTYPECD=5 " +
                "AND B.ENVIRONID=? " +
                "AND B.EXITID=?";
            pst = null;
            rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectDependentViews);
                    pst.setInt(1, environmentId);
                    pst.setInt(2, userExitRoutineId);
                    rs = pst.executeQuery();
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
            while (rs.next()) {
                DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
                depCompTransfer.setId(rs.getInt("VIEWID"));
                depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
                depCompTransfer.setDependencyInfo("[View Output]");
                dependentViews.add(depCompTransfer);
            }
            pst.close();
            rs.close();			
			
			if (!dependentViews.isEmpty()) {
				dependencies.put(ComponentType.View, dependentViews);
			}
			
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving dependencies of User Exit Routines with id [" + userExitRoutineId + "]", e);
		}
		return dependencies;

	}
	
    public List<DependentComponentTransfer> getUserExitRoutineLogicViewDeps(
            Integer environmentId, Integer userExitRoutineId)
            throws DAOException {
        List<DependentComponentTransfer> dependencies = new ArrayList<DependentComponentTransfer>();

        try {

            // Views in which User Exit Routine is used in View's
            // Logic text.
            String selectDependentViews = "Select DISTINCT A.VIEWID, A.NAME, C.COLUMNNUMBER,C.HDRLINE1 From "
                    + params.getSchema() + ".VIEW A, "
                    + params.getSchema() + ".VIEWLOGICDEPEND B,"
                    + params.getSchema() + ".VIEWCOLUMN C,"
                    + params.getSchema() + ".VIEWCOLUMNSOURCE D "
                    + "Where A.ENVIRONID =B.ENVIRONID AND B.ENVIRONID = C.ENVIRONID AND C.ENVIRONID = D.ENVIRONID AND "
                    + "B.PARENTID = D.VIEWCOLUMNSOURCEID AND D.VIEWCOLUMNID=C.VIEWCOLUMNID AND "
                    + "B.VIEWID = C.VIEWID AND C.VIEWID = D.VIEWID AND B.LOGICTYPECD = 2 AND "
                    + "A.VIEWID = B.VIEWID AND A.ENVIRONID =? AND B.EXITID =?";
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectDependentViews);
                    pst.setInt(1, environmentId);
                    pst.setInt(2, userExitRoutineId);

                    rs = pst.executeQuery();
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
            while (rs.next()) {
                DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
                depCompTransfer.setId(rs.getInt("VIEWID"));
                depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
                String colName = "";
                String heading1 = rs.getString("HDRLINE1");
                if (heading1 != null && !heading1.equals("")) {
                    colName = ", " + heading1;
                }
                depCompTransfer.setDependencyInfo("[Col " + rs.getInt("COLUMNNUMBER") + colName + ", Logic Text]");
                dependencies.add(depCompTransfer);
            }
            pst.close();
            rs.close();

            // Getting Views dependent on specified User Exit Routine in output logic.
            selectDependentViews = "SELECT DISTINCT A.VIEWID, A.NAME FROM " +
                params.getSchema() + ".VIEW A, " +
                params.getSchema() + ".VIEWLOGICDEPEND B, " +
                params.getSchema() + ".VIEWSOURCE C " +
                "WHERE A.ENVIRONID=B.ENVIRONID " +
                "AND A.VIEWID=B.VIEWID " +
                "AND B.ENVIRONID=C.ENVIRONID " +
                "AND B.PARENTID=C.VIEWSOURCEID " +
                "AND B.VIEWID=C.VIEWID " +
                "AND B.LOGICTYPECD=5 " +
                "AND B.ENVIRONID=? " +
                "AND B.EXITID=?";
            pst = null;
            rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectDependentViews);
                    pst.setInt(1, environmentId);
                    pst.setInt(2, userExitRoutineId);
                    rs = pst.executeQuery();
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
            while (rs.next()) {
                DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
                depCompTransfer.setId(rs.getInt("VIEWID"));
                depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
                depCompTransfer.setDependencyInfo("[View Output]");
                dependencies.add(depCompTransfer);
            }
            pst.close();
            rs.close();         
            
        } catch (SQLException e) {
            throw DataUtilities.createDAOException("Database error occurred while retrieving logic dependencies of User Exit Routines with id ["+ userExitRoutineId + "]", e);
        }
        return dependencies;
    }

	@Override
	public Integer getUserExitRoutine(String name, Integer environmentId, boolean procedure) {

		Integer result = null;
		try {
			List<String> idNames = new ArrayList<String>();
			if(procedure) {
				idNames.add(COL_EXECUTABLE);				
			} else {
				idNames.add(COL_NAME);
			}
			idNames.add(COL_ENVID);

			String selectString = generator.getSelectStatement(params
					.getSchema(), TABLE_NAME, idNames, null);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setString(1, name);
					pst.setInt(2, environmentId);
					rs = pst.executeQuery();
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
			if (rs.next()) {
				result = rs.getInt(2);
			} else {
				logger.info("No such User Exit Routine in Env " + environmentId + " with name : " + name);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the User Exit Routine ["+ name + "]", e);
		}
		return result;
	}
	
}
