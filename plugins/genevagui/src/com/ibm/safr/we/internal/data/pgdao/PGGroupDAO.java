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


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EnvRole;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.GroupDAO;
import com.ibm.safr.we.data.transfer.GroupComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.GroupEnvironmentAssociationTransfer;
import com.ibm.safr.we.data.transfer.GroupTransfer;
import com.ibm.safr.we.data.transfer.GroupUserAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.internal.data.PGSQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.GroupQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.query.UserQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;

/**
 * This class is used to implement the unimplemented methods of <b>GroupDAO</b>.
 * This class contains the methods related to Groups which require database
 * access.
 * 
 */
public class PGGroupDAO implements GroupDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGGroupDAO");

	private static final String TABLE_NAME = "GROUP";
	private static final String COL_ID = "GROUPID";
	private static final String COL_DESC = "NAME";
	private static final String COL_COMMENT = "COMMENTS";
	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";

	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private PGSQLGenerator generator = new PGSQLGenerator();

	public PGGroupDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	public List<GroupQueryBean> queryAllGroups(SortType sortType)
			throws DAOException {
		List<GroupQueryBean> result = new ArrayList<GroupQueryBean>();
		try {
			String selectString = "";
			String orderString = "";
			if (sortType.equals(SortType.SORT_BY_ID)) {
				orderString += COL_ID;
			} else {
				orderString += "UPPER( " + COL_DESC + ")";
			}

			if (SAFRApplication.getUserSession().isSystemAdministrator()) {
				selectString = "Select " + COL_ID + ", " + COL_DESC + ", "
						+ COL_CREATETIME + ", " + COL_CREATEBY + ", "
						+ COL_MODIFYTIME + ", " + COL_MODIFYBY + " From "
						+ params.getSchema() + "." + TABLE_NAME + " Order By "
						+ orderString;
			} else if (SAFRApplication.getUserSession().isEnvironmentAdministrator()) {
				selectString = "Select DISTINCT A.GROUPID, A.NAME, A.CREATEDTIMESTAMP, A.CREATEDUSERID, "
						+ "A.LASTMODTIMESTAMP, A.LASTMODUSERID From "
						+ params.getSchema()
						+ ".GROUP A, "
						+ params.getSchema()
						+ ".SECENVIRON B, "
						+ params.getSchema()
						+ ".SECUSER C Where A.GROUPID = B.GROUPID AND "
						+ "B.GROUPID = C.GROUPID AND B.ENVROLE='ADMIN' AND C.USERID = '"
						+ safrLogin.getUserId() + "'";
			} else {
				// CQ 8898. Nikita. 01/12/2010
				// Return empty list for general user.
				return result;
			}
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
				GroupQueryBean groupBean = new GroupQueryBean(rs.getInt(i++),
						DataUtilities.trimString(rs.getString(i++)), rs
								.getDate(i++), DataUtilities.trimString(rs
								.getString(i++)), rs.getDate(i++),
						DataUtilities.trimString(rs.getString(i++)));

				result.add(groupBean);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while querying all Groups.", e);
		}

	}

	public GroupTransfer getGroup(Integer id) throws DAOException {
		GroupTransfer result = null;
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);

			String selectString = generator.getSelectStatement(params
					.getSchema(), TABLE_NAME, idNames, null);
			PreparedStatement pst = null;
			ResultSet rs = null;

			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, id);
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
				logger.info("No such Group in database with ID : " + id);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while retrieving the Group with id ["
							+ id + "]", e);
		}
		return result;
	}

	public GroupTransfer getGroup(String name) throws DAOException {
		GroupTransfer result = null;
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);

			String selectString = "SELECT * " +
			    "FROM " +params.getSchema() +".GROUP " +
			    "WHERE NAME=?";
			PreparedStatement pst = null;
			ResultSet rs = null;

			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setString(1, name);
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
				logger.info("No such Group in database with name : " + name);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while retrieving the Group with name ["
							+ name + "]", e);
		}
		return result;
	}
	
	/**
	 * This function is used to generate a transfer object for the Group.
	 * 
	 * @param rs
	 *            : The resultset of a database query run on GROUP table
	 *            with which the values for the transfer objects are set.
	 * @return A transfer object for the Group with values set according to the
	 *         resultset.
	 * @throws SQLException
	 */
	private GroupTransfer generateTransfer(ResultSet rs) throws SQLException {
		GroupTransfer group = new GroupTransfer();
		group.setId(rs.getInt(COL_ID));
		group.setName(DataUtilities.trimString(rs.getString(COL_DESC)));
		group.setComments(DataUtilities.trimString(rs.getString(COL_COMMENT)));
		group.setCreateTime(rs.getDate(COL_CREATETIME));
		group.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		group.setModifyTime(rs.getDate(COL_MODIFYTIME));
		group.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

		return group;
	}

	public GroupTransfer persistGroup(GroupTransfer group) throws DAOException,
			SAFRNotFoundException {
		if (group.getId() == 0) {
			return (createGroup(group));
		} else {
			return (updateGroup(group));
		}
	}

	/**
	 * This function is used to create a Group in GROUP table
	 * 
	 * @param group
	 *            : The transfer object which contains the values which are to
	 *            be set in the columns for the corresponding Group which is
	 *            being created.
	 * @return The transfer object which contains the values which are received
	 *         from the GROUP for the Group which is created.
	 * @throws DAOException
	 */
	private GroupTransfer createGroup(GroupTransfer group) throws DAOException {
		try {
			String[] columnNames = { COL_DESC, COL_COMMENT,
					COL_CREATETIME, COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = Arrays.asList(columnNames);
			String statement = generator.getInsertStatement(params.getSchema(),
					TABLE_NAME, COL_ID, names, true);
			PreparedStatement pst = null;
            ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);

					int i = 1;
					pst.setString(i++, group.getName());
					pst.setString(i++, group.getComments());
					pst.setString(i++, safrLogin.getUserId());
					pst.setString(i++, safrLogin.getUserId());
					rs = pst.executeQuery();
                    rs.next();
                    int id = rs.getInt(1); 
                    group.setPersistent(true);
                    group.setId(id);
                    group.setCreateBy(safrLogin.getUserId());
                    group.setCreateTime(rs.getDate(2));
                    group.setModifyBy(safrLogin.getUserId());
                    group.setModifyTime(rs.getDate(3));
                    rs.close();
					
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
					"Database error occurred while creating a new Group.", e);
		}
		return group;

	}

	/**
	 * This function is used to update a Group in GROUP table.
	 * 
	 * @param group
	 *            : The transfer object which contains the values which are to
	 *            be set in the columns for the corresponding Group which is
	 *            being updated.
	 * @return The transfer object which contains the values which are received
	 *         from the GROUP for the Group which is updated recently.
	 * @throws DAOException
	 * @throws SAFRNotFoundException
	 */
	private GroupTransfer updateGroup(GroupTransfer group) throws DAOException,
			SAFRNotFoundException {
		try {
			String[] columnNames = { COL_DESC, COL_COMMENT, COL_MODIFYTIME, COL_MODIFYBY };

			List<String> names = Arrays.asList(columnNames);

			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			String statement = generator.getUpdateStatement(params.getSchema(), TABLE_NAME, names, idNames);
			PreparedStatement pst = con.prepareStatement(statement);
			while (true) {
				try {
					int i = 1;
					pst.setString(i++, group.getName());
					pst.setString(i++, group.getComments());
					pst.setString(i++, safrLogin.getUserId());
					pst.setInt(i++, group.getId());
                    ResultSet rs = pst.executeQuery();
                    rs.next();
                    group.setModifyTime(rs.getDate(1));                      
                    group.setModifyBy(safrLogin.getUserId());                      
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
					"Database error occurred while updating the Group.", e);
		}
		return group;
	}

	public GroupTransfer getDuplicateGroup(String groupName, Integer groupId)
			throws DAOException {
		GroupTransfer result = null;
		try {
			String statement = generator.getDuplicateComponent(params
					.getSchema(), TABLE_NAME, COL_DESC, COL_ID);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int i = 1;
					pst.setString(i++, groupName.toUpperCase());
					pst.setInt(i++, groupId);
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
				logger.info("Existing Group with name '" + groupName + "' found.");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving a duplicate Group.",e);
		}
		return result;

	}

	public void removeGroup(Integer id) throws DAOException {
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			String statement = generator.getDeleteStatement(params.getSchema(),
					TABLE_NAME, idNames);
			PreparedStatement pst = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					pst.setInt(1, id);
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
					"Database error occurred while deleting the Group.", e);
		}
	}

	public List<GroupUserAssociationTransfer> getAssociatedUsers(Integer groupId)
			throws DAOException {
		List<GroupUserAssociationTransfer> result = new ArrayList<GroupUserAssociationTransfer>();

		try {
			String schema = params.getSchema();
			String selectString = "Select A.USERID, B.FIRSTNAME, B.MIDDLEINIT, B.LASTNAME From "
					+ schema
					+ ".SECUSER A, "
					+ schema
					+ ".USER B"
					+ " Where GROUPID = ? "
					+ " AND A.USERID = B.USERID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, groupId);
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

			String lastName;
			String firstName;
			String middleInitial;
			String fullName = "";

			while (rs.next()) {
				fullName = "";
				firstName = DataUtilities.trimString(rs.getString("FIRSTNAME"));
				middleInitial = DataUtilities.trimString(rs
						.getString("MIDDLEINIT"));
				lastName = DataUtilities.trimString(rs.getString("LASTNAME"));
				if (lastName != null && !lastName.equals("")) {
					fullName = lastName + ",";
				}
				if (firstName != null) {
					fullName += firstName + " ";
				}
				if (middleInitial != null) {
					fullName += middleInitial;
				}
				GroupUserAssociationTransfer groupUserAssociationTransfer = new GroupUserAssociationTransfer();
				groupUserAssociationTransfer.setUserId(DataUtilities
						.trimString(rs.getString("USERID")));
				groupUserAssociationTransfer
						.setAssociatedComponentName(fullName);
				result.add(groupUserAssociationTransfer);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving list of Users associated with the Group.",e);
		}
		return result;
	}

	public List<GroupUserAssociationTransfer> persistAssociatedUsers(
			List<GroupUserAssociationTransfer> groupUserAssociationTransfer,
			Integer groupId) throws DAOException {
		groupUserAssociationTransfer = createAssociatedUsers(
				groupUserAssociationTransfer, groupId);

		return groupUserAssociationTransfer;

	}

	private List<GroupUserAssociationTransfer> createAssociatedUsers(
			List<GroupUserAssociationTransfer> groupUserAssociationTransfer,
			Integer groupId) throws DAOException {
		try {
			String[] columnNames = { COL_ID, "USERID", COL_CREATETIME,
					COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = Arrays.asList(columnNames);
			String statement = generator.getInsertStatementNoIdentifier(params.getSchema(),
					"SECUSER", names);
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);

					for (GroupUserAssociationTransfer associatedUsertoCreate : groupUserAssociationTransfer) {

						int i = 1;
						pst.setInt(i++, groupId);
						pst.setString(i++, associatedUsertoCreate.getUserId());
						pst.setString(i++, safrLogin.getUserId());
						pst.setString(i++, safrLogin.getUserId());
						pst.executeUpdate();
						associatedUsertoCreate.setPersistent(true);

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
		return groupUserAssociationTransfer;
	}

	public void deleteAssociatedUser(Integer secGroupId, List<String> deletionIds)
			throws DAOException {
		try {
			
			String statement = "Delete From " + params.getSchema()
					+ ".SECUSER" + " Where GROUPID = ? "
					+ " AND USERID IN (" + generator.getPlaceholders(deletionIds.size()) +")";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int ndx = 1;
					pst.setInt(ndx++,  secGroupId);
					for(int i=0; i<deletionIds.size(); i++) {
						pst.setString(ndx++,  deletionIds.get(i));
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
			throw DataUtilities.createDAOException("Database error occurred while deleting User associations with the Group.",e);
		}
	}

	public List<UserQueryBean> queryPossibleUserAssociations(List<String> notInParam)
			throws DAOException {
		List<UserQueryBean> result = new ArrayList<UserQueryBean>();

		try {
			String selectString = "Select USERID, FIRSTNAME, MIDDLEINIT, LASTNAME From "
					+ params.getSchema()
					+ ".USER "; 
					if(notInParam.size() > 0) {
						selectString += " Where USERID NOT IN (" + generator.getPlaceholders(notInParam.size()) + ") ";
					}
					selectString += " Order By USERID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					for(int i=0; i<notInParam.size(); i++) {
							pst.setString(ndx++, notInParam.get(i));
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
				int i = 1;
				UserQueryBean userQueryBean = new UserQueryBean(DataUtilities
						.trimString(rs.getString(i++)), DataUtilities
						.trimString(rs.getString(i++)), DataUtilities
						.trimString(rs.getString(i++)), DataUtilities
						.trimString(rs.getString(i++)), false, null, null,
						null, null, null);
				result.add(userQueryBean);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving all the possible Users which can be associated with the Group.",e);
		}
		return result;
	}

	public List<GroupEnvironmentAssociationTransfer> getAssociatedEnvironments(
			Integer groupId) throws DAOException {
		List<GroupEnvironmentAssociationTransfer> result = new ArrayList<GroupEnvironmentAssociationTransfer>();

		try {
			String schema = params.getSchema();

			String selectString = "";
			if (SAFRApplication.getUserSession().isSystemAdministrator()) {
				selectString = "Select A.ENVIRONID, B.NAME, "
						+ "A.ENVROLE, A.PFRIGHTS, A.LFRIGHTS, A.LRRIGHTS, "
						+ "A.EXITRIGHTS, A.LPRIGHTS, A.VWRIGHTS, A.VFRIGHTS, A.MGRIGHTS "
						+ " From " + schema
						+ ".SECENVIRON A, " + schema + ".ENVIRON B"
						+ " Where GROUPID = ? "
						+ " AND A.ENVIRONID = B.ENVIRONID";
			} else {
				selectString = "Select A.ENVIRONID, B.NAME, "
						+ "A.ENVROLE, A.PFRIGHTS, A.LFRIGHTS, A.LRRIGHTS, "
						+ "A.EXITRIGHTS, A.LPRIGHTS, A.VWRIGHTS, A.VFRIGHTS, A.MGRIGHTS "
						+ " From " + schema
						+ ".SECENVIRON A, " + schema + ".ENVIRON B"
						+ " Where GROUPID = ? "
						+ " AND A.ENVIRONID = B.ENVIRONID AND A.ENVROLE='ADMIN'";
			}
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {

					pst = con.prepareStatement(selectString);
					pst.setInt(1,  groupId);
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
				GroupEnvironmentAssociationTransfer envGroupAssocTransfer = new GroupEnvironmentAssociationTransfer();
                envGroupAssocTransfer.setAssociatingComponentId(groupId);				
				envGroupAssocTransfer.setEnvironmentId(rs.getInt("ENVIRONID"));
				envGroupAssocTransfer.setAssociatedComponentName(
				    DataUtilities.trimString(rs.getString("NAME")));
                envGroupAssocTransfer.setEnvRole(
                    EnvRole.getEnvRoleFromCode(DataUtilities.trimString(rs.getString("ENVROLE"))));
				envGroupAssocTransfer.setPhysicalFileCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("PFRIGHTS")));
				envGroupAssocTransfer.setLogicalFileCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("LFRIGHTS")));
				envGroupAssocTransfer.setLogicalRecordCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("LRRIGHTS")));
				envGroupAssocTransfer.setUserExitRoutineCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("EXITRIGHTS")));
                envGroupAssocTransfer.setLookupPathCreatePermissions(
                    DataUtilities.intToBoolean(rs.getInt("LPRIGHTS")));
                envGroupAssocTransfer.setViewCreatePermissions(
                    DataUtilities.intToBoolean(rs.getInt("VWRIGHTS")));
				envGroupAssocTransfer.setViewFolderCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("VFRIGHTS")));
				envGroupAssocTransfer.setMigrateInPermissions(
				    DataUtilities.intToBoolean(rs.getInt("MGRIGHTS")));
				result.add(envGroupAssocTransfer);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while retrieving the Environments associated with the Group.",e);
		}
		return result;
	}

	public GroupEnvironmentAssociationTransfer getAssociatedEnvironment(
			Integer groupId, Integer environmentId) throws DAOException {
        GroupEnvironmentAssociationTransfer envGroupAssocTransfer = null;

		try {
			String schema = params.getSchema();
			String selectString = "Select A.ENVIRONID, B.NAME, "
					+ "A.ENVROLE, A.PFRIGHTS, A.LFRIGHTS, A.LRRIGHTS, "
					+ "A.EXITRIGHTS, A.LPRIGHTS, A.VWRIGHTS, A.VFRIGHTS, A.MGRIGHTS"
					+ " From " + schema
					+ ".SECENVIRON A, " + schema + ".ENVIRON B"
					+ " Where GROUPID = ? "
					+ " AND A.ENVIRONID = B.ENVIRONID AND A.ENVIRONID = ? ";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1,  groupId);
					pst.setInt(2,  environmentId);
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
		        envGroupAssocTransfer = new GroupEnvironmentAssociationTransfer();
				envGroupAssocTransfer.setAssociatingComponentId(groupId);
				envGroupAssocTransfer.setEnvironmentId(environmentId);
				envGroupAssocTransfer.setAssociatedComponentName(
				    DataUtilities.trimString(rs.getString("NAME")));
                envGroupAssocTransfer.setEnvRole(
                    EnvRole.getEnvRoleFromCode(DataUtilities.trimString(rs.getString("ENVROLE"))));
				envGroupAssocTransfer.setPhysicalFileCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("PFRIGHTS")));
				envGroupAssocTransfer.setLogicalFileCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("LFRIGHTS")));
				envGroupAssocTransfer.setLogicalRecordCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("LRRIGHTS")));
				envGroupAssocTransfer.setUserExitRoutineCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("EXITRIGHTS")));
                envGroupAssocTransfer.setLookupPathCreatePermissions(
                    DataUtilities.intToBoolean(rs.getInt("LPRIGHTS")));
                envGroupAssocTransfer.setViewCreatePermissions(
                    DataUtilities.intToBoolean(rs.getInt("VWRIGHTS")));
				envGroupAssocTransfer.setViewFolderCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("VFRIGHTS")));
				envGroupAssocTransfer.setMigrateInPermissions(
				    DataUtilities.intToBoolean(rs.getInt("MGRIGHTS")));
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while retrieving the Environments association with the Group.",e);
		}
		return envGroupAssocTransfer;
	}

	public List<EnvironmentQueryBean> queryPossibleEnvironmentAssociations(
			List<Integer> associatedEnvIds) throws DAOException {
		List<EnvironmentQueryBean> result = new ArrayList<EnvironmentQueryBean>();
		String notInList = "";
		try {
			notInList = DataUtilities.integerListToString(associatedEnvIds);
			String selectString = "Select ENVIRONID, NAME From "
					+ params.getSchema() + ".ENVIRON Where "
					+ "ENVIRONID NOT IN " + notInList + " Order By ENVIRONID";
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
				EnvironmentQueryBean envQueryBean = new EnvironmentQueryBean(rs
						.getInt("ENVIRONID"), DataUtilities.trimString(rs
						.getString("NAME")), true, null, null, null,
						null);
				result.add(envQueryBean);
			}
			pst.close();
			rs.close();

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying all the possible Environments which can be associated with the Group.",e);
		}
		return result;
	}

	private void createGroupEnvironmentAssociations(List<GroupEnvironmentAssociationTransfer> createList)
        throws DAOException, SQLException {
        String schema = params.getSchema();
        PreparedStatement pst = null;
        String createStatement = "INSERT INTO " + schema +".SECENVIRON" +
                " (GROUPID,ENVIRONID,ENVROLE,PFRIGHTS,LFRIGHTS,LRRIGHTS,EXITRIGHTS,LPRIGHTS,VWRIGHTS,"
                + "VFRIGHTS,MGRIGHTS,CREATEDTIMESTAMP,CREATEDUSERID,LASTMODTIMESTAMP,LASTMODUSERID)" +
                " VALUES (?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,?)";
            
        if (createList == null) {
            return;
        }    
       
        while (true) {
            try {
                pst = con.prepareStatement(createStatement);
                for (GroupEnvironmentAssociationTransfer trans : createList) {
                    int i = 1;
                    pst.setInt(i++, trans.getAssociatingComponentId());
                    pst.setInt(i++, trans.getEnvironmentId());
                    pst.setString(i++, trans.getEnvRole().getCode());
                    if (trans.hasPhysicalFileCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasLogicalFileCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasLogicalRecordCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasUserExitRoutineCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasLookupPathCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasViewCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasViewFolderCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasMigrateInPermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    pst.setString(i++,safrLogin.getUserId());
                    pst.setString(i++,safrLogin.getUserId());
                    pst.executeUpdate();
                    trans.setPersistent(true);                        
                }
                pst.close();
                break;
            }
            catch (SQLException se) {
                if (con.isClosed()) {
                    con = DAOFactoryHolder.getDAOFactory().reconnect();
                } else {
                    throw se;
                }
            }
        }
	}

    private void updateGroupEnvironmentAssociations(List<GroupEnvironmentAssociationTransfer> updateList)
        throws DAOException, SQLException {
        String schema = params.getSchema();
        PreparedStatement pst = null;
        String updateStatement = "UPDATE " + schema +".SECENVIRON SET " + 
            "ENVROLE=?,PFRIGHTS=?,LFRIGHTS=?,LRRIGHTS=?,EXITRIGHTS=?," +
            "LPRIGHTS=?,VWRIGHTS=?,VFRIGHTS=?,MGRIGHTS=?," +
            "LASTMODTIMESTAMP=CURRENT_TIMESTAMP,LASTMODUSERID=? " +
            "WHERE GROUPID=? AND ENVIRONID=?";
        
        if (updateList == null) {
            return;
        }    
        
        while (true) {
            try {
                pst = con.prepareStatement(updateStatement);
                for (GroupEnvironmentAssociationTransfer trans : updateList) {
                    int i = 1;
                    pst.setString(i++, trans.getEnvRole().getCode());
                    if (trans.hasPhysicalFileCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasLogicalFileCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasLogicalRecordCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasUserExitRoutineCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasLookupPathCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasViewCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasViewFolderCreatePermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    if (trans.hasMigrateInPermissions()) {
                        pst.setInt(i++, 1);
                    } else {
                        pst.setInt(i++, 0);                            
                    }
                    pst.setString(i++,safrLogin.getUserId());
                    pst.setInt(i++, trans.getAssociatingComponentId());
                    pst.setInt(i++, trans.getEnvironmentId());                    
                    pst.executeUpdate();
                    trans.setPersistent(true);                        
                }
                pst.close();                
                break;
            }
            catch (SQLException se) {
                if (con.isClosed()) {
                    con = DAOFactoryHolder.getDAOFactory().reconnect();
                } else {
                    throw se;
                }
            }
        }        
    }
    
    private void deleteGroupEnvironmentAssociations(List<GroupEnvironmentAssociationTransfer> deleteList)
        throws DAOException, SQLException {
        String schema = params.getSchema();
        String deleteStatementP1 = "DELETE FROM " + schema +".SECEXIT " + 
            "WHERE GROUPID=? AND ENVIRONID=?";        
        String deleteStatementP2 = "DELETE FROM " + schema +".SECPHYFILE " + 
            "WHERE GROUPID=? AND ENVIRONID=?";        
        String deleteStatementP3 = "DELETE FROM " + schema +".SECLOGFILE " + 
            "WHERE GROUPID=? AND ENVIRONID=?";        
        String deleteStatementP4 = "DELETE FROM " + schema +".SECLOGREC " + 
            "WHERE GROUPID=? AND ENVIRONID=?";        
        String deleteStatementP5 = "DELETE FROM " + schema +".SECLOOKUP " + 
            "WHERE GROUPID=? AND ENVIRONID=?";        
        String deleteStatementP6 = "DELETE FROM " + schema +".SECVIEW " + 
            "WHERE GROUPID=? AND ENVIRONID=?";        
        String deleteStatementP7 = "DELETE FROM " + schema +".SECVIEWFOLDER " + 
            "WHERE GROUPID=? AND ENVIRONID=?";        
        String deleteStatement = "DELETE FROM " + schema +".SECENVIRON " + 
            "WHERE GROUPID=? AND ENVIRONID=?";
        
        if (deleteList == null) {
            return;
        }

        while (true) {
            try {
                PreparedStatement pstP1 = con.prepareStatement(deleteStatementP1);
                PreparedStatement pstP2 = con.prepareStatement(deleteStatementP2);
                PreparedStatement pstP3 = con.prepareStatement(deleteStatementP3);
                PreparedStatement pstP4 = con.prepareStatement(deleteStatementP4);
                PreparedStatement pstP5 = con.prepareStatement(deleteStatementP5);
                PreparedStatement pstP6 = con.prepareStatement(deleteStatementP6);
                PreparedStatement pstP7 = con.prepareStatement(deleteStatementP7);
                PreparedStatement pst = con.prepareStatement(deleteStatement);
                for (GroupEnvironmentAssociationTransfer trans : deleteList) {
                    int i = 1;                    
                    pstP1.setInt(i++, trans.getAssociatingComponentId());
                    pstP1.setInt(i++, trans.getEnvironmentId());                    
                    pstP1.executeUpdate();

                    i = 1;                    
                    pstP2.setInt(i++, trans.getAssociatingComponentId());
                    pstP2.setInt(i++, trans.getEnvironmentId());                    
                    pstP2.executeUpdate();
                    
                    i = 1;                    
                    pstP3.setInt(i++, trans.getAssociatingComponentId());
                    pstP3.setInt(i++, trans.getEnvironmentId());                    
                    pstP3.executeUpdate();
                    
                    i = 1;                    
                    pstP4.setInt(i++, trans.getAssociatingComponentId());
                    pstP4.setInt(i++, trans.getEnvironmentId());                    
                    pstP4.executeUpdate();
                    
                    i = 1;                    
                    pstP5.setInt(i++, trans.getAssociatingComponentId());
                    pstP5.setInt(i++, trans.getEnvironmentId());                    
                    pstP5.executeUpdate();
                    
                    i = 1;                    
                    pstP6.setInt(i++, trans.getAssociatingComponentId());
                    pstP6.setInt(i++, trans.getEnvironmentId());                    
                    pstP6.executeUpdate();
                    
                    i = 1;                    
                    pstP7.setInt(i++, trans.getAssociatingComponentId());
                    pstP7.setInt(i++, trans.getEnvironmentId());                    
                    pstP7.executeUpdate();
                    
                    i = 1;                    
                    pst.setInt(i++, trans.getAssociatingComponentId());
                    pst.setInt(i++, trans.getEnvironmentId());                    
                    pst.executeUpdate();
                    trans.setPersistent(false);                        
                }
                pstP1.close();
                pstP2.close();
                pstP3.close();
                pstP4.close();
                pstP5.close();
                pstP6.close();
                pstP7.close();
                pst.close();                
                break;
            }
            catch (SQLException se) {
                if (con.isClosed()) {
                    con = DAOFactoryHolder.getDAOFactory().reconnect();
                } else {
                    throw se;
                }
            }
        }        
    }
    
	public void persistGroupEnvironmentAssociations(
			List<GroupEnvironmentAssociationTransfer> createList,
			List<GroupEnvironmentAssociationTransfer> updateList,
			List<GroupEnvironmentAssociationTransfer> deleteList)
			throws DAOException {
		try {
		    createGroupEnvironmentAssociations(createList);
            updateGroupEnvironmentAssociations(updateList);
            deleteGroupEnvironmentAssociations(deleteList);
            
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while creating, updating or deleting the Group to Environment associations.",e);
		}
	}

    public List<GroupComponentAssociationTransfer> getComponentEditRights(
        ComponentType compType, Integer environmentId, Integer groupId) throws DAOException
    {
        List<GroupComponentAssociationTransfer> componentEditRights = new ArrayList<GroupComponentAssociationTransfer>();
        // return empty list if no component type is passed.
        if (compType == null) {
            return componentEditRights;
        }
        try {
            String schema = params.getSchema();
            String selectString = "";
            if (compType == ComponentType.PhysicalFile) {

                selectString = "Select A.PHYFILEID, B.NAME, A.RIGHTS "
                        + "From "+ schema+ ".PHYFILE B INNER JOIN "
                        + schema + ".SECPHYFILE A ON B.ENVIRONID = A.ENVIRONID"
                        + " Where"+ " A.PHYFILEID = B.PHYFILEID AND A.ENVIRONID = ?  AND B.ENVIRONID = ? "
                        + " AND A.GROUPID = ? ";

            } else if (compType == ComponentType.LogicalFile) {

                selectString = "Select A.LOGFILEID, B.NAME, A.RIGHTS "
                        + "From " + schema + ".LOGFILE B INNER JOIN "
                        + schema+ ".SECLOGFILE A ON B.ENVIRONID = A.ENVIRONID"
                        + " Where " + " A.LOGFILEID = B.LOGFILEID AND A.ENVIRONID = ?  AND B.ENVIRONID = ? "
                        + " AND A.GROUPID = ? ";

            } else if (compType == ComponentType.LogicalRecord) {

                selectString = "Select A.LOGRECID, B.NAME, A.RIGHTS " + "From "
                        + schema + ".LOGREC B INNER JOIN " + schema
                        + ".SECLOGREC A ON B.ENVIRONID = A.ENVIRONID"
                        + " Where " + " A.LOGRECID = B.LOGRECID AND A.ENVIRONID = ?  AND B.ENVIRONID = ? "
                        + " AND A.GROUPID = ? ";

            } else if (compType == ComponentType.UserExitRoutine) {

                selectString = "Select A.EXITID, B.NAME, A.RIGHTS "
                        + "From "+ schema+ ".EXIT B INNER JOIN "
                        + schema+ ".SECEXIT A ON B.ENVIRONID = A.ENVIRONID"
                        + " Where " + " A.EXITID = B.EXITID AND A.ENVIRONID = ?  AND B.ENVIRONID = ? "
                        + " AND A.GROUPID = ? ";

            } else if (compType == ComponentType.LookupPath) {

                selectString = "Select A.LOOKUPID, A.NAME, B.RIGHTS "
                    + "From "+ schema+ ".LOOKUP A INNER JOIN "
                    + schema + ".SECLOOKUP B "
                    + "ON A.ENVIRONID = B.ENVIRONID"
                    + " AND A.LOOKUPID = B.LOOKUPID"
                    + " WHERE A.ENVIRONID = ?  AND B.ENVIRONID = ? "
                    + " AND B.GROUPID = ? ";
                
            } else if (compType == ComponentType.View) {

                selectString = "Select A.VIEWID, A.NAME, B.RIGHTS "
                    + "From "+ schema+ ".VIEW A INNER JOIN "
                    + schema + ".SECVIEW B "
                    + "ON A.ENVIRONID = B.ENVIRONID"
                    + " AND A.VIEWID = B.VIEWID"
                    + " WHERE B.ENVIRONID = ? "
                    + " AND B.GROUPID = ? ";
                
            } else if (compType == ComponentType.ViewFolder) {

                selectString = "Select A.VIEWFOLDERID, B.NAME, A.RIGHTS "
                        + "From "+ schema+ ".VIEWFOLDER B INNER JOIN "
                        + schema+ ".SECVIEWFOLDER A ON B.ENVIRONID = A.ENVIRONID"
                        + " Where "+ " A.VIEWFOLDERID = B.VIEWFOLDERID AND A.ENVIRONID = ? "
                        + " AND B.ENVIRONID = ? "
                        + " AND A.GROUPID = ? ";

            }
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, environmentId);
                    if (compType == ComponentType.View) {
                        pst.setInt(2, groupId);
                    }
                    else {
                    	pst.setInt(2, environmentId);
                    	pst.setInt(3, groupId);
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
                GroupComponentAssociationTransfer grpCompTransfer = new GroupComponentAssociationTransfer();
                int i = 1;
                grpCompTransfer.setComponentId(rs.getInt(i++));
                grpCompTransfer.setAssociatedComponentName(DataUtilities.trimString(rs.getString(i++)));
                grpCompTransfer.setEnvironmentId(environmentId);
                grpCompTransfer.setRights(SAFRApplication.getUserSession().getEditRightsNoUser(rs.getInt(i++), compType, environmentId));
                componentEditRights.add(grpCompTransfer);
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving a list of a components along with their edit rights for a particular Environment-Group association.",e);
        }
        return componentEditRights;
    
    }

	public List<EnvironmentalQueryBean> queryPossibleComponentAssociations(
			ComponentType compType, Integer environmentId,
			List<Integer> associatedCompIds) throws DAOException {

		List<EnvironmentalQueryBean> result = new ArrayList<EnvironmentalQueryBean>();
		String notInList = "";
		try {
			String placeholders = generator.getPlaceholders(associatedCompIds.size());
			String selectString = "";
			PreparedStatement pst;
			ResultSet rs;
			while (true) {
				try {

					if (compType == ComponentType.PhysicalFile) {

						selectString = "Select PHYFILEID, NAME From "
								+ params.getSchema()
								+ ".PHYFILE Where ENVIRONID = ? "
								+ " AND PHYFILEID >0";
						if(associatedCompIds.size() > 0) {
							selectString += " AND PHYFILEID NOT IN (" + placeholders + " ) ";
						}
						selectString += " Order By PHYFILEID";
						pst = con.prepareStatement(selectString);
						int ndx = 1;
						pst.setInt(ndx++,  environmentId);
						for(int i=0; i<associatedCompIds.size(); i++) {
							pst.setInt(ndx++,  associatedCompIds.get(i));
						}
						rs = pst.executeQuery();

						while (rs.next()) {
							EnvironmentalQueryBean queryBean = new PhysicalFileQueryBean(
								environmentId, rs.getInt("PHYFILEID"),
								DataUtilities.trimString(rs.getString("NAME")), 
			                    null, null, null, null, null, null, 								
								null, null, null, null, null);
							result.add(queryBean);
						}
						pst.close();
						rs.close();

					} else if (compType == ComponentType.LogicalFile) {

						selectString = "Select LOGFILEID, NAME From "
								+ params.getSchema()
								+ ".LOGFILE Where ENVIRONID = ? "
								+ " AND LOGFILEID >0 "; 
								if(associatedCompIds.size() > 0) {
									selectString += " AND LOGFILEID NOT IN (" + placeholders + " ) ";
								}
								selectString += " Order By LOGFILEID";

						pst = con.prepareStatement(selectString);
						int ndx = 1;
						pst.setInt(ndx++,  environmentId);
						for(int i=0; i<associatedCompIds.size(); i++) {
							pst.setInt(ndx++,  associatedCompIds.get(i));
						}
						rs = pst.executeQuery();

						while (rs.next()) {
							EnvironmentalQueryBean queryBean = new LogicalFileQueryBean(
								environmentId, rs.getInt("LOGFILEID"),
								DataUtilities.trimString(rs.getString("NAME")), 
								null, null, null, null, null);
							result.add(queryBean);
						}
						pst.close();
						rs.close();

					} else if (compType == ComponentType.LogicalRecord) {

						selectString = "Select LOGRECID, NAME From "
								+ params.getSchema()
								+ ".LOGRECID Where ENVIRONID = ? "
								+ " AND LOGRECID >0 "; 
								if(associatedCompIds.size() > 0) {
									selectString += " AND LOGRECID NOT IN (" + placeholders + " ) ";
								}
								selectString += " Order By LOGRECID";

						pst = con.prepareStatement(selectString);
						int ndx = 1;
						rs = pst.executeQuery();

						while (rs.next()) {
							EnvironmentalQueryBean queryBean = new LogicalRecordQueryBean(
								environmentId, rs.getInt("LOGRECID"),
								DataUtilities.trimString(rs.getString("NAME")),
								null, null, null, null, null, null, null, null, null, null, null);
							result.add(queryBean);
						}
						pst.close();
						rs.close();
					} else if (compType == ComponentType.UserExitRoutine) {

						selectString = "Select EXITID, NAME From "
								+ params.getSchema()
								+ ".EXITID Where ENVIRONID = ? "
								+ " AND EXITID >0 "; 
								if(associatedCompIds.size() > 0) {
									selectString += " AND EXITID NOT IN (" + placeholders + " ) ";
								}
								selectString += " Order By EXITID";

						pst = con.prepareStatement(selectString);
						int ndx = 1;
						pst.setInt(ndx++,  environmentId);
						for(int i=0; i<associatedCompIds.size(); i++) {
							pst.setInt(ndx++,  associatedCompIds.get(i));
						}
						rs = pst.executeQuery();

						while (rs.next()) {
							EnvironmentalQueryBean queryBean = new UserExitRoutineQueryBean(
								environmentId, rs.getInt("EXITID"),
								DataUtilities.trimString(rs.getString("NAME")), 
								null, null, null, null, null, null, null, null);
							result.add(queryBean);
						}
						pst.close();
						rs.close();
					} else if (compType == ComponentType.ViewFolder) {
						if (associatedCompIds.size() == 0) {
							selectString = "Select VIEWFOLDERID, NAME From "
									+ params.getSchema()
									+ ".VIEWFOLDER Where ENVIRONID = ? "
									+ " Order By VIEWFOLDERID";
						} else {
							selectString = "Select VIEWFOLDERID, NAME From "
									+ params.getSchema()
									+ ".VIEWFOLDER Where ENVIRONID = ? "
									+ " AND VIEWFOLDERID NOT IN (" + placeholders + " ) " 
									+ " Order By VIEWFOLDERID";
						}
						pst = con.prepareStatement(selectString);
						int ndx = 1;
						pst.setInt(ndx++,  environmentId);
						for(int i=0; i<associatedCompIds.size(); i++) {
							pst.setInt(ndx++,  associatedCompIds.get(i));
						}
						rs = pst.executeQuery();

						while (rs.next()) {
							EnvironmentalQueryBean queryBean = new ViewFolderQueryBean(
									environmentId, rs.getInt("VIEWFOLDERID"),
									DataUtilities.trimString(rs.getString("NAME")), 
									null, null, null, null, null);
							result.add(queryBean);
						}

						pst.close();
						rs.close();
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
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying all the possible components which can be associated with the Group.",e);
		}
		return result;
	}

	public List<GroupQueryBean> queryGroups(String userId, Integer environmentId)
			throws DAOException {
		List<GroupQueryBean> result = new ArrayList<GroupQueryBean>();
		try {
			String selectString = "SELECT A.GROUPID, A.NAME, "
					+ "A.CREATEDTIMESTAMP, A.CREATEDUSERID, A.LASTMODTIMESTAMP, A.LASTMODUSERID "
					+ "FROM " + params.getSchema() + ".GROUP A, "
					+ params.getSchema() + ".SECUSER B, "
					+ params.getSchema() + ".SECENVIRON C "
					+ "WHERE A.GROUPID = B.GROUPID AND B.USERID = ? "
					+ " AND A.GROUPID = C.GROUPID AND C.ENVIRONID = ? "
					+ " ORDER BY UPPER(A.NAME)";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setString(1,  userId);
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
			while (rs.next()) {
				GroupQueryBean groupBean = new GroupQueryBean(rs
						.getInt("GROUPID"), DataUtilities.trimString(rs
						.getString("NAME")), rs
						.getDate("CREATEDTIMESTAMP"), DataUtilities
						.trimString(rs.getString("CREATEDUSERID")), rs
						.getDate("LASTMODTIMESTAMP"), DataUtilities
						.trimString(rs.getString("LASTMODUSERID")));

				result.add(groupBean);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying the Groups that the specified User belongs to, which are also associated with the specified Environment.",e);
		}

	}

    public void persistComponentEditRights(ComponentType compType,
        Integer groupId,
        List<GroupComponentAssociationTransfer> createList,
        List<GroupComponentAssociationTransfer> updateList,
        List<GroupComponentAssociationTransfer> deleteList)
        throws DAOException {

        try {
    
            String schema = params.getSchema();
            String entityType = "";
            String xml = "";
    
            if (compType == ComponentType.PhysicalFile) {
                entityType = "PF";
            } else if (compType == ComponentType.LogicalFile) {
                entityType = "LF";
            } else if (compType == ComponentType.LogicalRecord) {
                entityType = "LR";
            } else if (compType == ComponentType.UserExitRoutine) {
                entityType = "EXIT";
            } else if (compType == ComponentType.LookupPath) {
                entityType = "LP";
            } else if (compType == ComponentType.View) {
                entityType = "VIEW";
            } else if (compType == ComponentType.ViewFolder) {
                entityType = "VF";
            }
    
            xml += "<Root>\n" 
                    + getXmlForComponentRights(createList, "IN")
                    + getXmlForComponentRights(updateList, "UP")
                    + getXmlForComponentRights(deleteList, "DE") 
                    + "</Root>";
    
            if (!xml.equals("")) {
                String statement = generator.getSelectFromFunction(schema,
                        "inssecgrprights", 4);
//                CallableStatement proc = null;
				  PreparedStatement proc = null;
                while (true) {
                    try {
						proc = con.prepareStatement(statement);
//                        proc = con.prepareCall(statement);
						int i = 1;
						proc.setString(i++, entityType);
						proc.setInt(i++, groupId);
                        proc.setString(i++, safrLogin.getUserId());
                        proc.setString(i++, xml);
//                        proc.setString("P_ENTTYPE", entityType);
//                        proc.setInt("P_SECGROUP", groupId);
//                        proc.setString("P_USERID", safrLogin.getUserId());
//                        proc.setString("P_DOC", xml);
//                        proc.execute();
						proc.executeQuery();
						proc.close();
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
                proc.close();
            }
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while storing the edit rights on the components.",e);
        }
    }
    
	/**
	 * This method is for creating the xml which is the input
	 * parameter for the Stored Procedure GP_INSSECGRPRGHT which is used to
	 * store edit rights of a Group over the different metadata components of an
	 * Environment.
	 * 
	 * @param list
	 *            : The list of GroupEnvironmentAssociationTransfer objects.
	 * @param action
	 *            : The integer which is responsible for the action which is to
	 *            be performed through the SP. It can be one of create, update
	 *            and delete.
	 * @return xml which in one of input
	 *         parameter for the Stored Procedure GP_INSSECGRPRGHT for a
	 *         particular type of action.
	 */
	private String getXmlForComponentRights(
			List<GroupComponentAssociationTransfer> list, String action) {
	    StringBuffer xml = new StringBuffer();
		if (list != null && !list.isEmpty()) {
    		for (GroupComponentAssociationTransfer grpCompAssocTran : list) {
    		    xml.append(" <Operation>");
                xml.append("  <OPTYPE>" + action + "</OPTYPE>");
                xml.append("  <ENVID>" + grpCompAssocTran.getEnvironmentId() + "</ENVID>");
                xml.append("  <ENTID>" + grpCompAssocTran.getComponentId() + "</ENTID>");
                xml.append("  <RIGHTS>" + grpCompAssocTran.getRights().getCode() + "</RIGHTS>");
                xml.append(" </Operation>");
    		}
		}
		return xml.toString();
	}

}
