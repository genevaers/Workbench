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
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.EnvRole;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.EnvironmentDAO;
import com.ibm.safr.we.data.transfer.EnvironmentTransfer;
import com.ibm.safr.we.data.transfer.GroupEnvironmentAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.internal.data.PGSQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.EnvComponentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.GroupQueryBean;

/**
 * This class is used to implement the unimplemented methods of
 * <b>EnvironmentDAO</b>. This class contains the methods to related to
 * environment metadata component which requires database access.
 * 
 */
public class PGEnvironmentDAO implements EnvironmentDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGEnvironmentDAO");

	private static final String TABLE_NAME = "ENVIRON";
	private static final String COL_ID = "ENVIRONID";
	private static final String COL_NAME = "NAME";
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
	public PGEnvironmentDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	public EnvironmentTransfer getEnvironment(Integer id) throws DAOException {

		EnvironmentTransfer result = null;
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
				logger.info("No such environment in database with ID : "+ id);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while retrieving the Environment with id ["
							+ id + "]", e);
		}
		return result;
	}

	public EnvironmentTransfer getEnvironment(String name) throws DAOException {

		EnvironmentTransfer result = null;
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);

			String selectString = "SELECT * " +
				"FROM " +params.getSchema() +".ENVIRON " +
			    "WHERE ENVIRON=?";
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
				logger.info("No such environment in database with name : "+ name);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
				"Database error occurred while retrieving the Environment with name ["+ name + "]", e);
		}
		return result;
	}
	
	public List<EnvironmentQueryBean> queryAllEnvironments(SortType sortType)
			throws DAOException {
		List<EnvironmentQueryBean> result = new ArrayList<EnvironmentQueryBean>();

		try {
			String selectString = "";
			if (sortType.equals(SortType.SORT_BY_ID)) {
				selectString = "Select " + COL_ID + ", " + COL_NAME + ", "
						+ COL_CREATETIME + ", "
						+ COL_CREATEBY + ", " + COL_MODIFYTIME + ", "
						+ COL_MODIFYBY + " From " + params.getSchema() + "."
						+ TABLE_NAME + " Where " + COL_ID + " > 0 Order By "
						+ COL_ID;
			} else {
				selectString = "Select " + COL_ID + ", " + COL_NAME + ", "
						+ COL_CREATETIME + ", "
						+ COL_CREATEBY + ", " + COL_MODIFYTIME + ", "
						+ COL_MODIFYBY + " From " + params.getSchema() + "."
						+ TABLE_NAME + " Where " + COL_ID + " > 0 Order By "
						+ "UPPER( " + COL_NAME + ")";
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
				EnvironmentQueryBean environmentBean = new EnvironmentQueryBean(
					rs.getInt(i++), 
					DataUtilities.trimString(rs.getString(i++)), 
					true, rs.getDate(i++),
					DataUtilities.trimString(rs.getString(i++)), 
					rs.getDate(i++), 
					DataUtilities.trimString(rs.getString(i++)));

				result.add(environmentBean);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while querying all Environments.",e);
		}
	}

	public List<EnvironmentQueryBean> queryAllEnvironments(SortType sortType,
			String userid, Boolean onlyWithEnvAdminRights) throws DAOException {

		List<EnvironmentQueryBean> result = new ArrayList<EnvironmentQueryBean>();
		String selectString = null;
		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "RES.ENVIRONID";
		} else {
			orderString = "UPPER(RES.NAME)";
		}

		if (onlyWithEnvAdminRights) {
			selectString = "SELECT RES.* FROM ("
					+ "SELECT DISTINCT A.ENVIRONID, A.NAME " + "FROM "
					+ params.getSchema() + ".ENVIRON A, "
					+ params.getSchema() + ".SECENVIRON L, "
					+ params.getSchema() + ".SECUSER B "
					+ "WHERE A.ENVIRONID = L.ENVIRONID"
					+ " AND L.GROUPID = B.GROUPID AND B.USERID = ? "
					+ " AND L.ENVROLE = ? ) RES ORDER BY "
					+ orderString;

		} else {
			selectString = "SELECT RES.* FROM ("
					+ "SELECT DISTINCT A.ENVIRONID, A.NAME " + "FROM "
					+ params.getSchema() + ".ENVIRON A, "
					+ params.getSchema() + ".SECENVIRON L, "
					+ params.getSchema() + ".SECUSER B "
					+ "WHERE A.ENVIRONID = L.ENVIRONID"
					+ " AND L.GROUPID = B.GROUPID AND B.USERID = ? ) RES ORDER BY " + orderString;
		}

		try {
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setString(1, userid);
					if (onlyWithEnvAdminRights) {
						pst.setString(2, EnvRole.ADMIN.getCode());
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
				EnvironmentQueryBean environmentBean = new EnvironmentQueryBean(
						rs.getInt("ENVIRONID"), 
						DataUtilities.trimString(rs.getString("NAME")), 
						false, null,
						null, null, null);
				result.add(environmentBean);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying all Environments, which are associated with all Groups that the specified User belongs to.",e);
		}

		return result;
	}

	public List<EnvironmentQueryBean> queryEnvironmentsForGroup(
			SortType sortType, Integer groupId, Boolean onlyWithAdminRights)
			throws DAOException {

		List<EnvironmentQueryBean> result = new ArrayList<EnvironmentQueryBean>();

		String orderString = null;
		String adminRights = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "A.ENVIRONID";
		} else {
			orderString = "UPPER(A.NAME)";
		}
		if (onlyWithAdminRights) {
			adminRights = " AND L.ENVROLE = ? ";
		} else {
			adminRights = "";
		}
		String selectString = "SELECT A.ENVIRONID, A.NAME, L.ENVROLE, "
				+ "A.CREATEDTIMESTAMP, A.CREATEDUSERID, A.LASTMODTIMESTAMP, A.LASTMODUSERID "
				+ "FROM "
				+ params.getSchema()
				+ ".ENVIRON A "
				+ "INNER JOIN "
				+ params.getSchema()
				+ ".SECENVIRON L "
				+ "ON A.ENVIRONID = L.ENVIRONID "
				+ "WHERE L.GROUPID = ? "
				+ adminRights + " ORDER BY " + orderString;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1,  groupId);
					if (onlyWithAdminRights) {
						pst.setString(2,  EnvRole.ADMIN.getCode());
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
			    boolean admin = false;
			    if (EnvRole.getEnvRoleFromCode(DataUtilities.trimString(rs.getString("ENVROLE"))) == EnvRole.ADMIN) {
			        admin = true;
			    }
				EnvironmentQueryBean environmentBean = new EnvironmentQueryBean(
					rs.getInt("ENVIRONID"), 
					DataUtilities.trimString(rs.getString("NAME")), 
					admin, 
					rs.getDate("CREATEDTIMESTAMP"), 
					DataUtilities.trimString(rs.getString("CREATEDUSERID")), 
					rs.getDate("LASTMODTIMESTAMP"), 
					DataUtilities.trimString(rs.getString("LASTMODUSERID")));

				result.add(environmentBean);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying all Environments associated with the specified Group.",e);
		}

		return result;
	}

	/**
	 * This function is used to generate a transfer object for the environment.
	 * 
	 * @param rs
	 *            : The result set of a database query run on ENVIRON table
	 *            with which the values for the transfer objects are set.
	 * @return a transfer object for the environment with values set according
	 *         to the result set.
	 * @throws SQLException
	 */
	private EnvironmentTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		EnvironmentTransfer environment = new EnvironmentTransfer();
		environment.setId(rs.getInt(COL_ID));
		environment.setName(DataUtilities.trimString(rs.getString(COL_NAME)));
		environment.setComments(DataUtilities.trimString(rs
				.getString(COL_COMMENT)));
		environment.setCreateTime(rs.getDate(COL_CREATETIME));
		environment.setCreateBy(DataUtilities.trimString(rs
				.getString(COL_CREATEBY)));
		environment.setModifyTime(rs.getDate(COL_MODIFYTIME));
		environment.setModifyBy(DataUtilities.trimString(rs
				.getString(COL_MODIFYBY)));

		return environment;
	}

	public EnvironmentTransfer persistEnvironment(
			EnvironmentTransfer environment) throws DAOException,
			SAFRNotFoundException {

		if (environment.getId() == 0) {
			return (createEnvironment(environment));
		} else {
			return (updateEnvironment(environment));
		}

	}

	/**
	 * This function is used to create an environment in ENVIRON table. It
	 * uses a stored procedure GP_INITVALUES to create the environment.
	 * 
	 * @param environment
	 *            : The transfer object which contains the values which are to
	 *            be set in the fields for the corresponding environment which
	 *            is being created.
	 * @return The transfer object which contains the values which are received
	 *         from the ENVIRON for the environment which is created.
	 * @throws DAOException
	 */
	private EnvironmentTransfer createEnvironment(
			EnvironmentTransfer environment) throws DAOException {
		try {
			Integer nextId = 0;
			String runFunction = "select newenvironment(?, ?, ?, ?)";
			try {
	            PreparedStatement cs = con.prepareStatement(runFunction, Statement.RETURN_GENERATED_KEYS);
	            cs.setString(1, environment.getName());
	            cs.setString(2, environment.getComments());
	            cs.setString(3, safrLogin.getUserId());
	            cs.setString(4, environment.getControlRecClientName());
	            ResultSet rs = cs.executeQuery();
	            while (rs.next()) {
	            	nextId = rs.getInt(1);;
	            }
	            rs.close();
	            cs.close();
			} catch (SQLException se) {
				if (con.isClosed()) {
					con = DAOFactoryHolder.getDAOFactory().reconnect();
				} else {
					throw se;
				}
			}
			environment.setId(nextId);
			environment = getEnvironment(environment.getId());
			return (environment);

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while creating a new Environment.",e);
		}
	}

	/**
	 * This function is used to update an environment in ENVIRON table.
	 * 
	 * @param environment
	 *            :The transfer object which contains the values which are to be
	 *            set in the fields for the corresponding environment which is
	 *            being updated.
	 * @return The transfer object which contains the values which are received
	 *         from the ENVIRON for the environment which is updated.
	 * @throws DAOException
	 * @throws SAFRNotFoundException
	 */
	private EnvironmentTransfer updateEnvironment(
			EnvironmentTransfer environment) throws DAOException,
			SAFRNotFoundException {
		try {
			String[] columnNames = { COL_NAME, COL_COMMENT,
					COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = Arrays.asList(columnNames);

			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);

			String statement = generator.getUpdateStatement(params.getSchema(),
					TABLE_NAME, names, idNames);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);

					int i = 1;
					pst.setString(i++, environment.getName());
					pst.setString(i++, environment.getComments());
					pst.setString(i++, safrLogin.getUserId());
					pst.setInt(i++, environment.getId());
                    rs = pst.executeQuery();
                    rs.next();
                    environment.setModifyTime(rs.getDate(1));                      
                    environment.setModifyBy(safrLogin.getUserId());
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
			return (environment);

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while updating the Environment.",e);
		}

	}

	public EnvironmentTransfer getDuplicateEnvironment(String name,
			Integer environmentId) throws DAOException {
		EnvironmentTransfer result = null;
		try {
			String statement = generator.getDuplicateComponent(params
					.getSchema(), TABLE_NAME, COL_NAME, COL_ID);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int i = 1;
					pst.setString(i++, name.toUpperCase());
					pst.setInt(i++, environmentId);
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
				logger.info("Existing Environment with name '" + name + "' found.");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving a duplicate Environment.",e);
		}
		return result;

	}

    public List<EnvironmentQueryBean> queryBALEnvironments(SortType sortType) throws DAOException
    {
        List<EnvironmentQueryBean> result = new ArrayList<EnvironmentQueryBean>();

        try {
            String schema = params.getSchema();
            
            // form map of environments that contain a join with default access
            String selectString =
                "SELECT DISTINCT J.ENVIRONID " +
                "FROM " + schema + ".LOOKUP J, " +
                "     " + schema + ".SECENVIRON E " +
                "WHERE J.ENVIRONID=E.ENVIRONID " +
                "AND E.GROUPID=? " +
                "AND NOT EXISTS (" +
                "SELECT * FROM " + schema + ".SECLOOKUP X " +
                "WHERE X.ENVIRONID=J.ENVIRONID " + 
                "AND X.GROUPID=E.GROUPID " +
                "AND X.LOOKUPID=J.LOOKUPID)";
                
            PreparedStatement pst = null;
            ResultSet rs = null;

            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, SAFRApplication.getUserSession().getGroup().getId());
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
            
            Set<Integer> defEnvs = new HashSet<Integer>();
            while (rs.next()) {
                defEnvs.add(rs.getInt(COL_ID));
            }
            
            pst.close();
            rs.close();            
            
            String sortClause = null;
            if (sortType.equals(SortType.SORT_BY_ID)) {
                sortClause = "ORDER BY E.ENVIRONID";
            }
            else {
                sortClause = "ORDER BY UPPER(E.NAME)";            
            }
            
            // loop through all accessible environments grabbing info on migratein and joinrights            
            selectString = 
                "SELECT E.ENVIRONID, E.NAME, X.MGRIGHTS, D.RIGHTS " +
                "FROM " + schema + ".SECENVIRON X " +     
                "JOIN " + schema + ".ENVIRON E " +
                "ON X.ENVIRONID = E.ENVIRONID " +
                "LEFT OUTER JOIN " + schema + ".SECLOOKUP D " + 
                "ON X.ENVIRONID=D.ENVIRONID " +           
                "AND X.GROUPID=D.GROUPID " +
                "WHERE X.GROUPID=? "
                + sortClause;               
            
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, SAFRApplication.getUserSession().getGroup().getId());
                    rs = pst.executeQuery();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        // lost database connection, so reconnect and retry
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        rs.close();
                        throw se;
                    }
                }
            }
            Integer cEnvId = null;
            String cName = null;
            boolean envBAL = false;
            Integer envId = null;
            String name = null;
            while (rs.next()) {
                envId = rs.getInt(COL_ID);
                name = DataUtilities.trimString(rs.getString(COL_NAME));
                if (cEnvId == null || !cEnvId.equals(envId)) {
                    if (cEnvId != null && !envBAL) {
                        // check the previous environment for default access on joins
                        EditRights defRights = SAFRApplication.getUserSession().getRoleEditRights(ComponentType.LookupPath, cEnvId);
                        // check for the existence of any join with default access in the environment
                        if ((defRights.equals(EditRights.ReadModify) || defRights.equals(EditRights.ReadModifyDelete)) &&
                            defEnvs.contains(cEnvId)) {
                            
                            EnvironmentQueryBean envBean = new EnvironmentQueryBean(
                                cEnvId, cName, true, null,null,null,null);
                            result.add(envBean);                        
                        }
                    }
                    
                    // new environment
                    cEnvId = envId;
                    cName = name;
                    envBAL = false;
                    
                    boolean migrateIn = DataUtilities.intToBoolean(rs.getInt("MGRIGHTS"));
                    if (migrateIn) {
                        // this is a BAL env
                        envBAL = true;                        
                        EnvironmentQueryBean envBean = new EnvironmentQueryBean(
                            envId, name, true, null,null,null,null);
                        result.add(envBean);
                    }
                    else {
                        // check specific lookup in env
                        int rightsVal = rs.getInt("RIGHTS");
                        if (rightsVal != 0) {
                            EditRights rights = SAFRApplication.getUserSession().getEditRights(
                                rightsVal, ComponentType.LookupPath, envId);
                            if (rights.equals(EditRights.ReadModify) ||
                                rights.equals(EditRights.ReadModifyDelete)) {
                                // this is a BAL env
                                envBAL = true;                                
                                EnvironmentQueryBean envBean = new EnvironmentQueryBean(
                                    envId, name, true, null,null,null,null);
                                result.add(envBean);                            
                            }
                        }
                    }
                }
                else {
                    // if have already added this environment
                    if (envBAL) {
                        continue;
                    }
                    else {
                        // check specific lookup in env
                        int rightsVal = rs.getInt("RIGHTS");
                        if (rightsVal != 0) {
                            EditRights rights = SAFRApplication.getUserSession().getEditRights(
                                rightsVal, ComponentType.LookupPath, envId);
                            if (rights.equals(EditRights.ReadModify) ||
                                rights.equals(EditRights.ReadModifyDelete)) {
                                // this is a BAL env
                                envBAL = true;
                                
                                EnvironmentQueryBean envBean = new EnvironmentQueryBean(
                                    envId, name, 
                                    true, null,null,null,null);
                                result.add(envBean);                                                        
                            }
                        }
                    }
                }
            }
            // check last row
            if (envId != null && !envBAL) {
                // check the previous environment for default access on joins
                EditRights defRights = SAFRApplication.getUserSession().getRoleEditRights(ComponentType.LookupPath, envId);
                // check for the existence of any join with default access in the environment
                if ((defRights.equals(EditRights.ReadModify) || defRights.equals(EditRights.ReadModifyDelete)) &&
                    defEnvs.contains(cEnvId)) {
                    
                    EnvironmentQueryBean envBean = new EnvironmentQueryBean(
                        envId, name, 
                        true, null,null,null,null);
                    result.add(envBean);                        
                }                
            }
            
            pst.close();
            rs.close();
            return result;

        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while querying all BAL Environments.",e);
        }
    }

    public List<EnvironmentQueryBean> queryBAVEnvironments(SortType sortType) throws DAOException
    {
        List<EnvironmentQueryBean> result = new ArrayList<EnvironmentQueryBean>();

        try {
            String schema = params.getSchema();
            
            // form map of environments that contain a view with default access
            String selectString =
                "SELECT DISTINCT V.ENVIRONID " +
                "FROM " + schema + ".VIEW V, " +
                "     " + schema + ".SECENVIRON E " +
                "WHERE V.ENVIRONID=E.ENVIRONID " +
                "AND E.GROUPID=? " +
                "AND NOT EXISTS (" +
                "SELECT * FROM " + schema + ".SECVIEW X " +
                "WHERE X.ENVIRONID=V.ENVIRONID " + 
                "AND X.GROUPID=E.GROUPID " +
                "AND X.VIEWID=V.VIEWID)";
                
            PreparedStatement pst = null;
            ResultSet rs = null;

            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, SAFRApplication.getUserSession().getGroup().getId());
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
            
            Set<Integer> defEnvs = new HashSet<Integer>();
            while (rs.next()) {
                defEnvs.add(rs.getInt(COL_ID));
            }
            
            pst.close();
            rs.close();            
            
            String sortClause = null;
            if (sortType.equals(SortType.SORT_BY_ID)) {
                sortClause = "ORDER BY E.ENVIRONID";
            }
            else {
                sortClause = "ORDER BY UPPER(E.NAME)";            
            }
            
            // loop through all accessible environments grabbing info on migratein and viewrights            
            selectString = 
                "SELECT E.ENVIRONID, E.NAME, X.MGRIGHTS, D.RIGHTS " +
                "FROM " + schema + ".SECENVIRON X " +     
                "JOIN " + schema + ".ENVIRON E " +
                "ON X.ENVIRONID = E.ENVIRONID " +
                "LEFT OUTER JOIN " + schema + ".SECVIEW D " + 
                "ON X.ENVIRONID=D.ENVIRONID " +           
                "AND X.GROUPID=D.GROUPID " +
                "WHERE X.GROUPID=? "
                + sortClause;               
            
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, SAFRApplication.getUserSession().getGroup().getId());
                    rs = pst.executeQuery();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        // lost database connection, so reconnect and retry
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        rs.close();
                        throw se;
                    }
                }
            }
            Integer cEnvId = null;
            String cName = null;
            boolean envBAV = false;
            Integer envId = null;
            String name = null;
            while (rs.next()) {
                envId = rs.getInt(COL_ID);
                name = DataUtilities.trimString(rs.getString(COL_NAME));
                if (cEnvId == null || !cEnvId.equals(envId)) {
                    if (cEnvId != null && !envBAV) {
                        // check the previous environment for default access on views
                        EditRights defRights = SAFRApplication.getUserSession().getRoleEditRights(ComponentType.View, cEnvId);
                        // check for the existence of any view with default access in the environment
                        if ((defRights.equals(EditRights.ReadModify) || defRights.equals(EditRights.ReadModifyDelete)) &&
                            defEnvs.contains(cEnvId)) {
                            
                            EnvironmentQueryBean envBean = new EnvironmentQueryBean(
                                cEnvId, cName, true, null,null,null,null);
                            result.add(envBean);                        
                        }
                    }
                    
                    // new environment
                    cEnvId = envId;
                    cName = name;
                    envBAV = false;
                    
                    boolean migrateIn = DataUtilities.intToBoolean(rs.getInt("MGRIGHTS"));
                    if (migrateIn) {
                        // this is a BAV env
                        envBAV = true;
                        EnvironmentQueryBean envBean = new EnvironmentQueryBean(
                            envId, name, true, null,null,null,null);
                        result.add(envBean);
                    }
                    else {
                        // check specific view in env
                        int rightsVal = rs.getInt("RIGHTS");
                        if (rightsVal != 0) {                        
                            EditRights rights = SAFRApplication.getUserSession().getEditRights(
                                rightsVal, ComponentType.View, envId);
                            if (rights.equals(EditRights.ReadModify) ||
                                rights.equals(EditRights.ReadModifyDelete)) {
                                // this is a BAV env
                                envBAV = true;
                                
                                EnvironmentQueryBean envBean = new EnvironmentQueryBean(
                                    envId, name, true, null,null,null,null);
                                result.add(envBean);                            
                            }
                        }
                    }
                }
                else {
                    // if have already added this environment
                    if (envBAV) {
                        continue;
                    }
                    else {
                        // check specific view in env
                        int rightsVal = rs.getInt("RIGHTS");
                        if (rightsVal != 0) {                                                
                            EditRights rights = SAFRApplication.getUserSession().getEditRights(
                                rightsVal, ComponentType.View, envId);
                            if (rights.equals(EditRights.ReadModify) ||
                                rights.equals(EditRights.ReadModifyDelete)) {
                                // this is a BAV env
                                envBAV = true;
                                
                                EnvironmentQueryBean envBean = new EnvironmentQueryBean(
                                    envId, name, true, null,null,null,null);
                                result.add(envBean);                                                        
                            }
                        }
                    }
                }
            }
            // check last row 
            if (envId != null && !envBAV) {
                // check the previous environment for default access on views
                EditRights defRights = SAFRApplication.getUserSession().getRoleEditRights(ComponentType.View, envId);
                // check for the existence of any view with default access in the environment
                if ((defRights.equals(EditRights.ReadModify) || defRights.equals(EditRights.ReadModifyDelete)) &&
                    defEnvs.contains(envId)) {
                    
                    EnvironmentQueryBean envBean = new EnvironmentQueryBean(
                        envId, name, true, null,null,null,null);
                    result.add(envBean);                        
                }
            }
            pst.close();
            rs.close();
            return result;

        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while querying all BAV Environments.",e);
        }
    }
    
	public List<GroupEnvironmentAssociationTransfer> getAssociatedGroups(
			Integer environmentId, SortType sortType) throws DAOException {
		List<GroupEnvironmentAssociationTransfer> result = new ArrayList<GroupEnvironmentAssociationTransfer>();
		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "A.GROUPID";
		} else {
			orderString = "UPPER(B.NAME)";
		}
		try {
			String schema = params.getSchema();
			String selectString = "Select A.GROUPID, B.NAME, "
					+ "A.ENVROLE, A.PFRIGHTS, A.LFRIGHTS, A.LRRIGHTS, "
					+ "A.EXITRIGHTS, A.LPRIGHTS, A.VWRIGHTS, A.VFRIGHTS, A.MGRIGHTS "
					+ " From " + schema
					+ ".SECENVIRON A, " + schema + ".GROUP B"
					+ " Where ENVIRONID = ?  "
					+ " AND A.GROUPID = B.GROUPID " + " ORDER BY "
					+ orderString;
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, environmentId);
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
				GroupEnvironmentAssociationTransfer grpEnvAssocTransfer = new GroupEnvironmentAssociationTransfer();
				grpEnvAssocTransfer.setEnvironmentId(environmentId);
				grpEnvAssocTransfer.setEnvRole(EnvRole.getEnvRoleFromCode(
				    DataUtilities.trimString(rs.getString("ENVROLE"))));
				grpEnvAssocTransfer.setAssociatingComponentId(rs.getInt("GROUPID"));
				grpEnvAssocTransfer.setAssociatingComponentName(
				    DataUtilities.trimString(rs.getString("NAME")));
				grpEnvAssocTransfer.setPhysicalFileCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("PFRIGHTS")));
				grpEnvAssocTransfer.setLogicalFileCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("LFRIGHTS")));
				grpEnvAssocTransfer.setLogicalRecordCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("LRRIGHTS")));
				grpEnvAssocTransfer.setUserExitRoutineCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("EXITRIGHTS")));
                grpEnvAssocTransfer.setLookupPathCreatePermissions(
                    DataUtilities.intToBoolean(rs.getInt("LPRIGHTS")));
                grpEnvAssocTransfer.setViewCreatePermissions(
                    DataUtilities.intToBoolean(rs.getInt("VWRIGHTS")));
				grpEnvAssocTransfer.setViewFolderCreatePermissions(
				    DataUtilities.intToBoolean(rs.getInt("VFRIGHTS")));
				grpEnvAssocTransfer.setMigrateInPermissions(
				    DataUtilities.intToBoolean(rs.getInt("MGRIGHTS")));
				result.add(grpEnvAssocTransfer);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the Groups associated with the Environment.",e);
		}
		return result;
	}

	public void clearEnvironment(Integer environmentId) throws DAOException {
		try {
            String statement = generator.getSelectFromFunction(params.getSchema(), 
								"clearEnvironment", 1);
			PreparedStatement proc = null;

			while (true) {
				try {
                    proc = con.prepareStatement(statement);
					proc.setInt(1, environmentId);
					proc.executeQuery();
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
			proc.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while clearing the components of an Environment.",e);
		}

	}

	public void removeEnvironment(Integer environmentId) throws DAOException {
		try {
            String statement = generator.getSelectFromFunction(params.getSchema(), 
								"deleteEnvironment", 1);
			PreparedStatement proc = null;

			while (true) {
				try {
                    proc = con.prepareStatement(statement);
					proc.setInt(1, environmentId);
					proc.executeQuery();
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
			proc.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while deleting an Environment.",e);
		}

	}

	public Boolean hasDependencies(Integer environmentId) throws DAOException {
		try {
			Integer state;
            String statement = generator.getSelectFromFunction(params.getSchema(), 
								"checkEnvironmentDependencies", 1);
			PreparedStatement proc = null;
			while (true) {
				try {
                    proc = con.prepareStatement(statement);
					proc.setInt(1, environmentId);
					ResultSet rs = proc.executeQuery();
					rs.next();	
					state = rs.getInt(1);
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
//			state = proc.getInt(2);

			proc.close();

			return (state != 0);

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while checking the dependencies of an Environment.",e);
		}
	}

	public List<GroupQueryBean> queryPossibleGroupAssociations(
			List<Integer> associatedGroupIds) throws DAOException {
		List<GroupQueryBean> result = new ArrayList<GroupQueryBean>();
		String notInList = "";
		try {
			notInList = DataUtilities.integerListToString(associatedGroupIds);
			String selectString = "Select GROUPID, NAME From "
					+ params.getSchema() + ".GROUP Where "
					+ "GROUPID NOT IN " + notInList + " Order By GROUPID";
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
				GroupQueryBean groupQueryBean = new GroupQueryBean(rs
						.getInt("GROUPID"), DataUtilities.trimString(rs
						.getString("NAME")), null, null, null, null);
				result.add(groupQueryBean);
			}
			pst.close();
			rs.close();

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying all the possible groups which can be associated with the environment.",e);
		}
		return result;
	}

	public List<EnvComponentQueryBean> queryComponentEnvironments(Integer id,
			ComponentType type) throws DAOException {
		List<EnvComponentQueryBean> result = new ArrayList<EnvComponentQueryBean>();

		String selectString = "";
		switch (type) {
		case LogicalRecordField:
			selectString = "Select A." + COL_ID + ", A." + COL_NAME + ", B.NAME, B."
					+ COL_CREATETIME + ", B."+ COL_CREATEBY  
					+ ", B." + COL_MODIFYTIME + ", B."+ COL_MODIFYBY 
					+ " From " + params.getSchema() + "."
					+ TABLE_NAME + " A, " + params.getSchema() + ".LRFIELD B" 
					+ " Where A." + COL_ID + "=B." + COL_ID 
					+ " AND B.LRFIELDID=" +id.toString();		
			break;		
		case LookupPath:
			selectString = "Select A." + COL_ID + ", A." + COL_NAME + ", B.NAME, B."
					+ COL_CREATETIME + ", B."+ COL_CREATEBY  
					+ ", B." + COL_MODIFYTIME + ", B."+ COL_MODIFYBY 
					+ " From " + params.getSchema() + "."
					+ TABLE_NAME + " A, " + params.getSchema() + ".LOOKUP B" 
					+ " Where A." + COL_ID + "=B." + COL_ID 
					+ " AND B.LOOKUPID=" +id.toString();		
			break;		
		case LogicalFile:
			selectString = "Select A." + COL_ID + ", A." + COL_NAME + ", B.NAME, B."
					+ COL_CREATETIME + ", B."+ COL_CREATEBY  
					+ ", B." + COL_MODIFYTIME + ", B."+ COL_MODIFYBY 
					+ " From " + params.getSchema() + "."
					+ TABLE_NAME + " A, " + params.getSchema() + ".LOGFILE B" 
					+ " Where A." + COL_ID + "=B." + COL_ID 
					+ " AND B.LOGFILEID=" +id.toString();		
			break;
		case LogicalRecord:
			selectString = "Select A." + COL_ID + ", A." + COL_NAME + ", B.NAME, B."
					+ COL_CREATETIME + ", B."+ COL_CREATEBY  
					+ ", B." + COL_MODIFYTIME + ", B."+ COL_MODIFYBY 
					+ " From " + params.getSchema() + "."
					+ TABLE_NAME + " A, " + params.getSchema() + ".LOGREC B" 
					+ " Where A." + COL_ID + "=B." + COL_ID 
					+ " AND B.LOGRECID=" +id.toString();		
			break;
		case PhysicalFile:
			selectString = "Select A." + COL_ID + ", A." + COL_NAME + ", B.NAME, B."
					+ COL_CREATETIME + ", B."+ COL_CREATEBY  
					+ ", B." + COL_MODIFYTIME + ", B."+ COL_MODIFYBY 
					+ " From " + params.getSchema() + "."
					+ TABLE_NAME + " A, " + params.getSchema() + ".PHYFILE B" 
					+ " Where A." + COL_ID + "=B." + COL_ID 
					+ " AND B.PHYFILEID=" +id.toString();		
			break;
		case UserExitRoutine:
			selectString = "Select A." + COL_ID + ", A." + COL_NAME + ", B.NAME, B."
					+ COL_CREATETIME + ", B."+ COL_CREATEBY  
					+ ", B." + COL_MODIFYTIME + ", B."+ COL_MODIFYBY 
					+ " From " + params.getSchema() + "."
					+ TABLE_NAME + " A, " + params.getSchema() + ".EXIT B" 
					+ " Where A." + COL_ID + "=B." + COL_ID 
					+ " AND B.EXITID=" +id.toString();		
			break;		
		case View:
			selectString = "Select A." + COL_ID + ", A." + COL_NAME + ", B.NAME, B."
					+ COL_CREATETIME + ", B."+ COL_CREATEBY  
					+ ", B." + COL_MODIFYTIME + ", B."+ COL_MODIFYBY 
					+ " From " + params.getSchema() + "."
					+ TABLE_NAME + " A, " + params.getSchema() + ".VIEW B" 
					+ " Where A." + COL_ID + "=B." + COL_ID 
					+ " AND B.VIEWID=" +id.toString();		
			break;					
		default:
			return null;		
		}
		try {
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
				EnvComponentQueryBean envCompBean = new EnvComponentQueryBean(
				        rs.getInt(i++), DataUtilities.trimString(rs.getString(i++)), 
				        DataUtilities.trimString(rs.getString(i++)), 
						rs.getDate(i++),DataUtilities.trimString(rs.getString(i++)), 
						rs.getDate(i++), DataUtilities.trimString(rs.getString(i++)));

				result.add(envCompBean);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while querying Components in Environments.",e);
		}
	}
}
