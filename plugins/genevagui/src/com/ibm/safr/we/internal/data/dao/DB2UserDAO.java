package com.ibm.safr.we.internal.data.dao;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.UserDAO;
import com.ibm.safr.we.data.transfer.UserGroupAssociationTransfer;
import com.ibm.safr.we.data.transfer.UserTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.query.UserQueryBean;

/**
 * This class is used to implement the unimplemented methods of <b>UserDAO</b>.
 * This class contains the methods to related to User which requires database
 * access.
 */
public class DB2UserDAO implements UserDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.DB2UserDAO");

	private static final String TABLE_NAME = "USER";
	
	private static final String COL_USER_ID = "USERID";
	private static final String COL_PD = "PASSWORD";
	private static final String COL_FIRST_NAME = "FIRSTNAME";
	private static final String COL_MIDDLE_NAME = "MIDDLEINIT";
	private static final String COL_LAST_NAME = "LASTNAME";
	private static final String COL_EMAIL = "EMAIL";
    private static final String COL_DEFAULT_ENVIRONMENT = "DEFENVIRONID";
	private static final String COL_DEFAULT_VIEW_FOLDER = "DEFFOLDERID";
	private static final String COL_DEFAULT_GROUP = "DEFGROUPID";
    private static final String COL_ISADMIN = "SYSADMIN";
    private static final String COL_LOG_LEVEL = "LOGLEVEL";
    private static final String COL_MAX_COMPILE_ERRORS = "MAXCOMPILEERRORS";
	private static final String COL_COMMENTS = "COMMENTS";
	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";
	
	private static final String SECGROUP_ID = "GROUPID";
	
	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private SQLGenerator generator = new SQLGenerator();

	public DB2UserDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrlogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrlogin;
	}

	public List<UserQueryBean> queryAllUsers() throws DAOException {
		List<UserQueryBean> result = new ArrayList<UserQueryBean>();

		try {
			String selectString = "Select " + COL_USER_ID + ", "
					+ COL_FIRST_NAME + ", " + COL_MIDDLE_NAME + ", "
					+ COL_LAST_NAME + ", " + COL_ISADMIN + ", " + COL_EMAIL
					+ ", " + COL_CREATETIME + ", " + COL_CREATEBY + ", "
					+ COL_MODIFYTIME + ", " + COL_MODIFYBY + " From "
					+ params.getSchema() + "." + TABLE_NAME + " Order By "
					+ "UPPER(" + COL_USER_ID + ")";
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
				int i = 1;
				UserQueryBean userBean = new UserQueryBean(DataUtilities
						.trimString(rs.getString(i++)), DataUtilities
						.trimString(rs.getString(i++)), DataUtilities
						.trimString(rs.getString(i++)), DataUtilities
						.trimString(rs.getString(i++)), rs.getBoolean(i++),
						DataUtilities.trimString(rs.getString(i++)), rs
								.getDate(i++), DataUtilities.trimString(rs
								.getString(i++)), rs.getDate(i++),
						DataUtilities.trimString(rs.getString(i++)));

				result.add(userBean);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while querying all Users.", e);
		}

	}

	public UserTransfer getUser(String id) throws DAOException {
		UserTransfer userTransfer = null;
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_USER_ID);
			String selectString = generator.getSelectStatement(params
					.getSchema(), TABLE_NAME, idNames, null);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setString(1, id);
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
				userTransfer = generateTransfer(rs);
			} else {
				logger.info("No such User in database with ID : " + id);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while retrieving the User with id [" + id + "]", e);
		}
		return userTransfer;
	}

	/**
	 * This method is used to generate a transfer object for the User.
	 * 
	 * @param rs
	 *            : The resultset of a database query run on USER table
	 *            from which the values for the transfer objects are set.
	 * @return A transfer object for the User with values set according to the
	 *         resultset.
	 * @throws SQLException
	 */
	private UserTransfer generateTransfer(ResultSet rs) throws SQLException {
		UserTransfer userTransfer = new UserTransfer();
		userTransfer.setUserid(DataUtilities.trimString(rs.getString(COL_USER_ID)));
		userTransfer.setPassword(DataUtilities.trimString(rs.getString(COL_PD)));
		userTransfer.setFirstName(DataUtilities.trimString(rs.getString(COL_FIRST_NAME)));
		userTransfer.setMiddleInitial(DataUtilities.trimString(rs.getString(COL_MIDDLE_NAME)));
		userTransfer.setLastName(DataUtilities.trimString(rs.getString(COL_LAST_NAME)));
		userTransfer.setEmail(DataUtilities.trimString(rs.getString(COL_EMAIL)));
		userTransfer.setLogLevel(rs.getInt(COL_LOG_LEVEL));
		userTransfer.setMaxCompileErrors(rs.getInt(COL_MAX_COMPILE_ERRORS));
		userTransfer.setDefaultViewFolderId(rs.getInt(COL_DEFAULT_VIEW_FOLDER));
		userTransfer.setDefaultEnvironmentId(rs.getInt(COL_DEFAULT_ENVIRONMENT));
		userTransfer.setDefaultGroupId(rs.getInt(COL_DEFAULT_GROUP));
		userTransfer.setAdmin(rs.getBoolean(COL_ISADMIN));
		userTransfer.setComments(rs.getString(COL_COMMENTS));
		userTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
		userTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		userTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
		userTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

		return userTransfer;
	}

	public UserTransfer persistUser(UserTransfer userTransfer)
			throws DAOException, SAFRNotFoundException {

		if (userTransfer.isPersistent()) {
			return (updateUser(userTransfer));
		} else {
			return (createUser(userTransfer));
		}

	}

	/**
	 * This method is used to create a User in USER table.
	 * 
	 * @param usertransfer
	 *            : The transfer object which contains the values which are to
	 *            be set in the columns for the corresponding User which is
	 *            being created.
	 * @return The transfer object which contains the values which are received
	 *         from the USER for the User which is created.
	 * @throws DAOException
	 */
	private UserTransfer createUser(UserTransfer userTransfer)
			throws DAOException {
		try {
			String[] columnNames = { COL_USER_ID, COL_PD, COL_FIRST_NAME,
					COL_MIDDLE_NAME, COL_LAST_NAME, COL_EMAIL, COL_LOG_LEVEL,
					COL_MAX_COMPILE_ERRORS, COL_DEFAULT_ENVIRONMENT,
					COL_DEFAULT_GROUP, COL_DEFAULT_VIEW_FOLDER, COL_ISADMIN,
					COL_COMMENTS, COL_CREATETIME,
					COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = Arrays.asList(columnNames);

			String statement = generator.getInsertStatementNoIdentifier(params.getSchema(),
					TABLE_NAME, names);
			PreparedStatement pst = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int i = 1;
					pst.setString(i++, userTransfer.getUserid());
					pst.setString(i++, userTransfer.getPassword());
					pst.setString(i++, userTransfer.getFirstName());
					pst.setString(i++, userTransfer.getMiddleInitial());
					pst.setString(i++, userTransfer.getLastName());
					pst.setString(i++, userTransfer.getEmail());
					pst.setInt(i++, userTransfer.getLogLevel());
					pst.setInt(i++, userTransfer.getMaxCompileErrors());
					pst.setInt(i++, userTransfer.getDefaultEnvironmentId());
					pst.setInt(i++, userTransfer.getDefaultGroupId());
					pst.setInt(i++, userTransfer.getDefaultViewFolderId());
					pst.setInt(i++, DataUtilities.booleanToInt(userTransfer.isAdmin()));
					pst.setString(i++, userTransfer.getComments());
					pst.setString(i++, safrLogin.getUserId());
					pst.setString(i++, safrLogin.getUserId());
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
			userTransfer = getUser(userTransfer.getUserid());

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while creating a new User.", e);
		}
		return userTransfer;

	}

	/**
	 * This method is used to update a User in USER table
	 * 
	 * @param userTransfer
	 *            : The transfer object which contains the values which are to
	 *            be set in the columns for the corresponding User which is
	 *            being updated.
	 * @return The transfer object which contains the values which are received
	 *         from the USER for the User which has been updated.
	 * @throws DAOException
	 * @throws SAFRNotFoundException
	 */
	private UserTransfer updateUser(UserTransfer userTransfer)
			throws DAOException, SAFRNotFoundException {
		try {
			String[] columnNames = { COL_PD, COL_FIRST_NAME,
					COL_MIDDLE_NAME, COL_LAST_NAME, COL_EMAIL, COL_LOG_LEVEL,
					COL_MAX_COMPILE_ERRORS, COL_DEFAULT_ENVIRONMENT,
					COL_DEFAULT_GROUP, COL_DEFAULT_VIEW_FOLDER, COL_ISADMIN,
					COL_COMMENTS, COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = Arrays.asList(columnNames);
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_USER_ID);

			String statement = generator.getUpdateStatement(params.getSchema(),
					TABLE_NAME, names, idNames);
			PreparedStatement pst = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int i = 1;
					pst.setString(i++, userTransfer.getPassword());
					pst.setString(i++, userTransfer.getFirstName());
					pst.setString(i++, userTransfer.getMiddleInitial());
					pst.setString(i++, userTransfer.getLastName());
					pst.setString(i++, userTransfer.getEmail());
					pst.setInt(i++, userTransfer.getLogLevel());
					pst.setInt(i++, userTransfer.getMaxCompileErrors());
					pst.setInt(i++, userTransfer.getDefaultEnvironmentId());
					pst.setInt(i++, userTransfer.getDefaultGroupId());
					pst.setInt(i++, userTransfer.getDefaultViewFolderId());
					pst.setInt(i++, DataUtilities.booleanToInt(userTransfer.isAdmin()));
					pst.setString(i++, userTransfer.getComments());
					pst.setString(i++, safrLogin.getUserId());
					pst.setString(i++, userTransfer.getUserid());
                    ResultSet rs = pst.executeQuery();
                    rs.next();
                    userTransfer.setModifyTime(rs.getDate(1));                      
                    rs.close();                 
                    pst.close();
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
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while updating the User.", e);
		}
		return userTransfer;

	}

	public void removeUser(String id) throws DAOException {
		boolean success = false;
		try {
			while (!success) {
				try {
					// Begin Transaction
					DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
					List<String> idNames = new ArrayList<String>();
					idNames.add(COL_USER_ID);
					String[] relatedTables = { "SECUSER", TABLE_NAME };
					for (String table : relatedTables) {
						String statement = generator.getDeleteStatement(
								params.getSchema(), table, idNames);
						PreparedStatement pst = null;

						while (true) {
							try {
								pst = con.prepareStatement(statement);
								pst.setString(1, id);
								pst.execute();
								break;
							} catch (SQLException se) {
								if (con.isClosed()) {
									// lost database connection, so reconnect
									// and retry
									con = DAOFactoryHolder.getDAOFactory()
											.reconnect();
								} else {
									throw se;
								}
							}
						}
						pst.close();
					}
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
					throw DataUtilities.createDAOException(
							"Database error occurred while deleting the User.",e);
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

	public UserTransfer getDuplicateUser(String userId) throws DAOException {
		UserTransfer result = null;
		try {
			String statement = "Select * From " + params.getSchema() + "."
					+ TABLE_NAME + " Where Upper(" + COL_USER_ID + ") = ?";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					pst.setString(1, userId.toUpperCase());
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
				logger.info("No duplicate User in database with ID : " + userId);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving a duplicate User.",e);
		}
		return result;
	}

	public void setUserPreferences(String userId, Integer environmentId,
			Integer groupId) throws DAOException {
		try {
			String[] columnNames = { COL_DEFAULT_ENVIRONMENT,
					COL_DEFAULT_GROUP, COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = Arrays.asList(columnNames);
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_USER_ID);
			String statement = generator.getUpdateStatement(params.getSchema(),
					TABLE_NAME, names, idNames);
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int i = 1;
					pst.setInt(i++, environmentId);
					pst.setInt(i++, groupId);
					pst.setString(i++, safrLogin.getUserId());
					pst.setString(i++, userId);
					pst.execute();
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
			throw DataUtilities.createDAOException("Database error occurred while setting user preferences for the default Environment and default Group.",e);
		}

	}

	public List<UserGroupAssociationTransfer> getAssociatedGroups(String userId)
			throws DAOException {
		List<UserGroupAssociationTransfer> result = new ArrayList<UserGroupAssociationTransfer>();

		try {
			String schema = params.getSchema();
			String selectString = "Select B.GROUPID, B.NAME From "
					+ schema + ".SECUSER A, " + schema
					+ ".GROUP B" + " Where USERID = ? "
					+ " AND A.GROUPID = B.GROUPID";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setString(1,  userId);
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
				UserGroupAssociationTransfer userGroupAssociationTransfer = new UserGroupAssociationTransfer();
				userGroupAssociationTransfer.setGroupId(rs.getInt(("GROUPID")));
				userGroupAssociationTransfer.setAssociatedComponentName(rs.getString("NAME"));
				result.add(userGroupAssociationTransfer);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving list of Groups associated with the User.",e);
		}
		return result;
	}

	public void removeViewFolderReferences(Integer viewFolderId)
			throws DAOException {
		try {
			List<String> names = new ArrayList<String>();
			names.add(COL_DEFAULT_VIEW_FOLDER);

			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_DEFAULT_VIEW_FOLDER);
			String statement = generator.getUpdateStatement(params.getSchema(),
					TABLE_NAME, names, idNames);
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					pst.setInt(1, 0);
					pst.setInt(2, viewFolderId);
					pst.execute();
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
			throw DataUtilities.createDAOException(
			    "Database error occurred while deleting the references of View Folder to user.",e);
		}
	}

	public List<UserGroupAssociationTransfer> persistAssociatedGroups(
			List<UserGroupAssociationTransfer> list, String userid)
			throws DAOException {

		list = createAssociatedGroups(list, userid);

		return list;
	}
	
    public void deleteAssociatedGroups(String userId,  List<Integer> deletionIds)
            throws DAOException {
        try {
        	String placeholders = generator.getPlaceholders(deletionIds.size());
            String statement = "Delete From " + params.getSchema()
                    + ".SECUSER" + " Where USERID = ? "
                    + "AND GROUPID IN ( " + placeholders + " )";
            PreparedStatement pst = null;
    
            while (true) {
                try {
                    pst = con.prepareStatement(statement);
                    int ndx = 1;
                    pst.setString(ndx++, userId);
                    for(int i=0; i<deletionIds.size(); i++) {
                    	pst.setInt(ndx++,  deletionIds.get(i));
                    }
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
            throw DataUtilities.createDAOException("Database error occurred while deleting Group associations with the User.",e);
        }
    }	

	private List<UserGroupAssociationTransfer> createAssociatedGroups(
			List<UserGroupAssociationTransfer> userGroupAssociationTransfer,
			String userId) throws DAOException {
		try {
			String[] columnNames = { SECGROUP_ID, COL_USER_ID, COL_CREATETIME,
					COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = Arrays.asList(columnNames);
			String statement = generator.getInsertStatementNoIdentifier(params.getSchema(),
					"SECUSER", names);

			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					for (UserGroupAssociationTransfer associatedGrouptoCreate : userGroupAssociationTransfer) {
						int i = 1;
						pst.setInt(i++, associatedGrouptoCreate.getGroupId());
						pst.setString(i++, userId);
						pst.setString(i++, safrLogin.getUserId());
						pst.setString(i++, safrLogin.getUserId());
						pst.executeUpdate();
			            associatedGrouptoCreate.setPersistent(true);
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
			throw DataUtilities.createDAOException("Database error occurred while creating User associations with the Group.",e);
		}
		return userGroupAssociationTransfer;
	}

}
