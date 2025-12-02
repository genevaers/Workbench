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

import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.ControlRecordDAO;
import com.ibm.safr.we.data.transfer.ControlRecordTransfer;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ControlRecordQueryBean;

/**
 * This class is used to implement the unimplemented methods of
 * <b>ControlRecordDAO</b>. This class contains the methods to related to
 * Control Record metadata component which require database access.
 * 
 */
public class DB2ControlRecordDAO implements ControlRecordDAO {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.DB2ControlRecordDAO");

	private static final String TABLE_NAME = "CONTROLREC";
	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "CONTROLRECID";
	private static final String COL_NAME = "NAME";
	private static final String COL_FIRSTMONTH = "FIRSTMONTH";
	private static final String COL_LOWVAL = "LOWVALUE";
	private static final String COL_HIGHVAL = "HIGHVALUE";
	private static final String COL_COMMENT = "COMMENTS";
	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";

	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private SQLGenerator generator = new SQLGenerator();

	/**
	 * Constructor for this class.
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
	public DB2ControlRecordDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrlogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrlogin;
	}

	/*
	 * This function is used to generate a transfer object for the Control
	 * Record.
	 */
	private ControlRecordTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		ControlRecordTransfer crec = new ControlRecordTransfer();
		crec.setEnvironmentId(rs.getInt(COL_ENVID));
		crec.setId(rs.getInt(COL_ID));
		crec.setName(DataUtilities.trimString(rs.getString(COL_NAME)));
		crec.setFirstFiscalMonth(rs.getInt(COL_FIRSTMONTH));
		crec.setBeginPeriod(rs.getInt(COL_LOWVAL));
		crec.setEndPeriod(rs.getInt(COL_HIGHVAL));
		crec.setComments(DataUtilities.trimString(rs.getString(COL_COMMENT)));
		crec.setCreateTime(rs.getDate(COL_CREATETIME));
		crec.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		crec.setModifyTime(rs.getDate(COL_MODIFYTIME));
		crec.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

		return crec;
	}

	public List<ControlRecordQueryBean> queryAllControlRecords(
			Integer environmentId, SortType sortType) throws DAOException {
		List<ControlRecordQueryBean> result = new ArrayList<ControlRecordQueryBean>();

		boolean admin = SAFRApplication.getUserSession().isSystemAdministrator()
				|| SAFRApplication.getUserSession().isEnvironmentAdministrator();

		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "A.CONTROLRECID";
		} else {
			orderString = "UPPER(A.NAME)";
		}

		try {
			String selectString = "SELECT A.CONTROLRECID, A.NAME, "
					+ "A.CREATEDTIMESTAMP,A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID "
					+ " FROM "
					+ params.getSchema()
					+ ".CONTROLREC A "
					+ "WHERE A.CONTROLRECID > 0 AND A.ENVIRONID = ? "
					+ " ORDER BY " + orderString;
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
				ControlRecordQueryBean controlRecordBean = new ControlRecordQueryBean(
					environmentId,
					rs.getInt(COL_ID),
					DataUtilities.trimString(rs.getString(COL_NAME)),
					(admin ? EditRights.ReadModifyDelete : EditRights.Read),
					rs.getDate(COL_CREATETIME), 
					DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
					rs.getDate(COL_MODIFYTIME), 
					DataUtilities.trimString(rs.getString(COL_MODIFYBY)));
				result.add(controlRecordBean);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying all Control Records.",e);
		}
	}

	public void deleteAllControlRecord(Integer environmentId)throws DAOException {
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ENVID);

			String statement = generator.getDeleteStatement(params.getSchema(),
					TABLE_NAME, idNames);
			PreparedStatement pst = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					pst.setInt(1, environmentId);
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
			throw DataUtilities.createDAOException("Database error occurred while deleting the Control Record.",e);
		}
	}
	
	public ControlRecordTransfer getControlRecord(Integer id,
			Integer environmentId) throws DAOException {
		ControlRecordTransfer result = null;
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
				logger.info("No such Control Record in Env " + environmentId + " with id : "+ id);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while retrieving the Control Record with id ["+ id + "]", e);
		}
		return result;
	}

	   public ControlRecordTransfer getControlRecord(String id,
	            Integer environmentId) throws DAOException {
	        ControlRecordTransfer result = null;
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
	                    pst.setString(1, id);
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
	                logger.info("No such Control Record in Env " + environmentId + " with id : "+ id);
	            }
	            pst.close();
	            rs.close();
	        } catch (SQLException e) {
	            throw DataUtilities.createDAOException(
	                    "Database error occurred while retrieving the Control Record with id ["+ id + "]", e);
	        }
	        return result;
	    }


	public ControlRecordTransfer persistControlRecord(ControlRecordTransfer crec)
			throws DAOException, SAFRNotFoundException {
		if (!crec.isPersistent()) {
			return (createControlRecord(crec));
		} else {
			return (updateControlRecord(crec));
		}
	}

	/**
	 *This function is used to create a Control Record in CONTROLREC table
	 * 
	 * @param crec
	 *            : The transfer object which contains the values which are to
	 *            be set in the fields for the corresponding Control Record
	 *            which is being created.
	 * @return The transfer object which contains the values which are received
	 *         from the CONTROLREC for the Control Record which is created.
	 * @throws DAOException
	 */
	private ControlRecordTransfer createControlRecord(ControlRecordTransfer crec)
			throws DAOException {
		try {
			boolean isImportOrMigrate = crec.isForImport() || crec.isForMigration() ? true : false;			
            String[] columnNames = { COL_ENVID, COL_NAME,
                COL_FIRSTMONTH, COL_LOWVAL, COL_HIGHVAL,
                COL_COMMENT, COL_CREATETIME, COL_CREATEBY, COL_MODIFYTIME,
                COL_MODIFYBY };
            List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
			if (isImportOrMigrate) {
			    names.add(1, COL_ID);
			}
            String statement = generator.getInsertStatement(params.getSchema(), TABLE_NAME, COL_ID, names, !isImportOrMigrate);

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);

					int i = 1;
					pst.setInt(i++, crec.getEnvironmentId());
		            if (isImportOrMigrate) {
		                pst.setInt(i++, crec.getId());
		            }
					pst.setString(i++, crec.getName());
					pst.setInt(i++, crec.getFirstFiscalMonth());
					pst.setInt(i++, crec.getBeginPeriod());
					pst.setInt(i++, crec.getEndPeriod());
					pst.setString(i++, crec.getComments());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(crec.getCreateTime()));
					}
					pst.setString(i++, isImportOrMigrate ? crec.getCreateBy(): safrLogin.getUserId());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(crec.getModifyTime()));
					}
					pst.setString(i++, isImportOrMigrate ? crec.getModifyBy(): safrLogin.getUserId());
					rs = pst.executeQuery();
		            rs.next();
		            int id = rs.getInt(1); 
		            crec.setPersistent(true);
		            crec.setId(id);
                    if (!isImportOrMigrate) {
                        crec.setCreateBy(safrLogin.getUserId());
                        crec.setCreateTime(rs.getDate(2));
                        crec.setModifyBy(safrLogin.getUserId());
                        crec.setModifyTime(rs.getDate(3));
                    }		            
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
			throw DataUtilities.createDAOException("Database error occurred while creating a new Control Record.",e);
		}
		return crec;
	}

	/**
	 * This function is used to update a Control Record in CONTROLREC table.
	 * 
	 * @param crec
	 *            : The transfer object which contains the values which are to
	 *            be set in the fields for the corresponding Control Record
	 *            which is being updated.
	 * @return The transfer object which contains the values which are received
	 *         from the CONTROLREC for the Control Record which is updated
	 *         recently.
	 * @throws DAOException
	 * @throws SAFRNotFoundException
	 */
	private ControlRecordTransfer updateControlRecord(ControlRecordTransfer crec)
			throws DAOException, SAFRNotFoundException {
		try {
			boolean isImportOrMigrate = crec.isForImport() || crec.isForMigration() ? true : false;
			boolean useCurrentTS = !isImportOrMigrate;

			String[] columnNames = { COL_NAME, COL_FIRSTMONTH, COL_LOWVAL,
					COL_HIGHVAL, COL_COMMENT };
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
					pst.setString(i++, crec.getName());
					pst.setInt(i++, crec.getFirstFiscalMonth());
					pst.setInt(i++, crec.getBeginPeriod());
					pst.setInt(i++, crec.getEndPeriod());
					pst.setString(i++, crec.getComments());
					if (isImportOrMigrate) {
						// createby and lastmod set from import or migrate data
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(crec.getCreateTime()));
						pst.setString(i++, crec.getCreateBy());
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(crec.getModifyTime()));
						pst.setString(i++, crec.getModifyBy());
					} else {
						// createby details are untouched
						// lastmodtimestamp is CURRENT_TIMESTAMP
						// lastmoduserid is logged in user
						pst.setString(i++, safrLogin.getUserId());
					}
					pst.setInt(i++, crec.getId());
					pst.setInt(i++, crec.getEnvironmentId());
					if (useCurrentTS) {
	                    ResultSet rs = pst.executeQuery();
	                    rs.next();
                        crec.setModifyTime(rs.getDate(1));	
                        crec.setModifyBy(safrLogin.getUserId());
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
			return (crec);

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while updating the Control Record.",e);
		}

	}

	public void removeControlRecord(Integer id, Integer environmentId)
			throws DAOException {
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			idNames.add(COL_ENVID);

			String statement = generator.getDeleteStatement(params.getSchema(),
					TABLE_NAME, idNames);
			PreparedStatement pst = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					pst.setInt(1, id);
					pst.setInt(2, environmentId);
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
			throw DataUtilities.createDAOException("Database error occurred while deleting the Control Record.",e);
		}
	}

	public ControlRecordTransfer getDuplicateControlRecord(
			String controlRecordName, Integer controlId, Integer environmentId)
			throws DAOException {
		ControlRecordTransfer result = null;
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
					pst.setString(i++, controlRecordName.toUpperCase());
					pst.setInt(i++, controlId);
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
				logger.info("Existing Control Record with name '" + controlRecordName
						+ "' found in Environment  [" + environmentId + "]");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving a duplicate Control Record.",e);
		}
		return result;
	}

	public List<DependentComponentTransfer> getControlRecordViewDependencies(
			Integer environmentId, Integer controlRecordId) throws DAOException {
		List<DependentComponentTransfer> dependencies = new ArrayList<DependentComponentTransfer>();

		try {
			// Getting Views dependent on specified Control Record.
			String selectDependentPFs = "Select VIEWID, NAME From "
					+ params.getSchema()
					+ ".VIEW Where ENVIRONID =? AND CONTROLRECID =?";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectDependentPFs);
					pst.setInt(1, environmentId);
					pst.setInt(2, controlRecordId);
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
				dependencies.add(depCompTransfer);
			}

			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving View dependencies of Control Record with id ["+ controlRecordId + "]", e);
		}
		return dependencies;

	}

	@Override
	public void deleteAllControlRecordsFromEnvironment(Integer environmentId)throws DAOException {
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ENVID);

			String statement = generator.getDeleteStatement(params.getSchema(),
					TABLE_NAME, idNames);
			PreparedStatement pst = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					pst.setInt(1, environmentId);
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
			throw DataUtilities.createDAOException("Database error occurred while deleting the Control Record.",e);
		}
	}

}
