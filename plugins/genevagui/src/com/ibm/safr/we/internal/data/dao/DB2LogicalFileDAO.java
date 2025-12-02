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
import com.ibm.safr.we.data.dao.LogicalFileDAO;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.LogicalFileTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;

public class DB2LogicalFileDAO implements LogicalFileDAO {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.DB2LogicalFileDAO");

    private static final String TABLE_NAME = "LOGFILE";
	private static final String TABLE_LF_PF_ASSOC = "LFPFASSOC";
	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "LOGFILEID";
	private static final String COL_NAME = "NAME";
	private static final String COL_COMMENT = "COMMENTS";
	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";

	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private SQLGenerator generator = new SQLGenerator();

	public DB2LogicalFileDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrlogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrlogin;
	}

	private LogicalFileTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		LogicalFileTransfer logicalFileTransfer = new LogicalFileTransfer();
		logicalFileTransfer.setEnvironmentId(rs.getInt(COL_ENVID));
		logicalFileTransfer.setId(rs.getInt(COL_ID));
		logicalFileTransfer.setName(DataUtilities.trimString(rs
				.getString(COL_NAME)));
		logicalFileTransfer.setComments(DataUtilities.trimString(rs
				.getString(COL_COMMENT)));
		logicalFileTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
		logicalFileTransfer.setCreateBy(DataUtilities.trimString(rs
				.getString(COL_CREATEBY)));
		logicalFileTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
		logicalFileTransfer.setModifyBy(DataUtilities.trimString(rs
				.getString(COL_MODIFYBY)));

		return logicalFileTransfer;
	}

	public LogicalFileTransfer getDuplicateLogicalFile(String logicalFileName,
			Integer logicalFileId, Integer environmentId) throws DAOException {
		LogicalFileTransfer result = null;
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
					pst.setString(i++, logicalFileName.toUpperCase());
					pst.setInt(i++, logicalFileId);
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
				logger.info("Existing Logical File with name '" + logicalFileName
						+ "' found in Environment [" + environmentId + "]");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving a duplicate Logical File.",e);
		}
		return result;
	}

    public LogicalFileTransfer getLogicalFile(Integer id, Integer environmentId)
            throws DAOException {
        LogicalFileTransfer result = null;
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
                    if(id!=null){
                        pst.setInt(1, id);
                    }
                    else{
                        continue;
                    }
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
                logger.info("No such Logical File in Env " + environmentId + " with id : " + id);
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                    "Database error occurred while retrieving the Logical File with id ["+ id + "]", e);
        }
        return result;
    }

    public LogicalFileTransfer getLogicalFile(String name, Integer environmentId)
            throws DAOException {
        LogicalFileTransfer result = null;
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
                result = generateTransfer(rs);
            } else {
                logger.info("No such Logical File in Env " + environmentId + " with id : " + name);
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                    "Database error occurred while retrieving the Logical File with id ["+ name + "]", e);
        }
        return result;
    }

	public LogicalFileTransfer persistLogicalFile(
			LogicalFileTransfer logicalFileTransfer) throws DAOException,
			SAFRNotFoundException {
		if (!logicalFileTransfer.isPersistent()) {
			return (createLogicalFile(logicalFileTransfer));
		} else {
			return (updateLogicalFile(logicalFileTransfer));
		}
	}

	private LogicalFileTransfer updateLogicalFile(
			LogicalFileTransfer logicalFileTransfer) throws DAOException,
			SAFRNotFoundException {
		
		boolean isImportOrMigrate = logicalFileTransfer.isForImport()
				|| logicalFileTransfer.isForMigration() ? true : false;
		boolean useCurrentTS = !isImportOrMigrate;

		try {
			List<String> names = new ArrayList<String>();
			names.add(COL_NAME);
			names.add(COL_COMMENT);
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
					pst.setString(i++, logicalFileTransfer.getName());
					pst.setString(i++, logicalFileTransfer.getComments());
					if (isImportOrMigrate) {
						// create and modify details set from source component
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(logicalFileTransfer.getCreateTime()));
						pst.setString(i++, logicalFileTransfer.getCreateBy());
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(logicalFileTransfer.getModifyTime()));
						pst.setString(i++, logicalFileTransfer.getModifyBy());
					} else {
						// create details are untouched
						// lastmodtimestamp is CURRENT_TIMESTAMP
						// lastmoduserid is logged in user
						pst.setString(i++, safrLogin.getUserId());
					}
					pst.setInt(i++, logicalFileTransfer.getId());
					pst.setInt(i++, logicalFileTransfer.getEnvironmentId());
                    if (useCurrentTS) {
                        ResultSet rs = pst.executeQuery();
                        rs.next();
                        logicalFileTransfer.setModifyTime(rs.getDate(1));                      
                        logicalFileTransfer.setModifyBy(safrLogin.getUserId());                      
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
			return (logicalFileTransfer);

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while updating the Logical File.",e);
		}

	}

	private LogicalFileTransfer createLogicalFile(
			LogicalFileTransfer logicalFileTransfer) throws DAOException {
		
		boolean isImportOrMigrate = logicalFileTransfer.isForImport()
				|| logicalFileTransfer.isForMigration() ? true : false;
		boolean useCurrentTS = !isImportOrMigrate;

		try {
			String[] columnNames = { COL_ENVID, COL_NAME, COL_COMMENT,
					COL_CREATETIME, COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
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
					pst.setInt(i++, logicalFileTransfer.getEnvironmentId());
		            if (isImportOrMigrate) {
		                pst.setInt(i++, logicalFileTransfer.getId());
		            }
					pst.setString(i++, logicalFileTransfer.getName());
					pst.setString(i++, logicalFileTransfer.getComments());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(logicalFileTransfer.getCreateTime()));
					}
					pst.setString(i++,isImportOrMigrate ? logicalFileTransfer.getCreateBy() : safrLogin.getUserId());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(logicalFileTransfer.getModifyTime()));
					}
					pst.setString(i++,isImportOrMigrate ? logicalFileTransfer.getModifyBy() : safrLogin.getUserId());
					rs = pst.executeQuery();
                    rs.next();
                    int id = rs.getInt(1);          
                    logicalFileTransfer.setId(id);
                    logicalFileTransfer.setPersistent(true);
                    if (!isImportOrMigrate) {
                        logicalFileTransfer.setCreateBy(safrLogin.getUserId());
                        logicalFileTransfer.setCreateTime(rs.getDate(2));
                        logicalFileTransfer.setModifyBy(safrLogin.getUserId());
                        logicalFileTransfer.setModifyTime(rs.getDate(3));                            
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
			throw DataUtilities.createDAOException("Database error occurred while creating a new Logical File.",e);
		}
		return logicalFileTransfer;
	}

	public List<LogicalFileQueryBean> queryAllLogicalFiles(
			Integer environmentId, SortType sortType) throws DAOException {
		List<LogicalFileQueryBean> result = new ArrayList<LogicalFileQueryBean>();

		String columnString = "";
		String joinString = "";
		String orderString = "";

        boolean admin = SAFRApplication.getUserSession().isSystemAdministrator(); 
		
		if (!admin) {
			// apply group-component security to the SQL
			columnString = "L.RIGHTS, ";
			joinString = "LEFT OUTER JOIN "
					+ params.getSchema()
					+ ".SECLOGFILE L ON A.ENVIRONID = L.ENVIRONID AND A.LOGFILEID = L.LOGFILEID "
					+ "AND L.GROUPID = "
					+ SAFRApplication.getUserSession().getGroup().getId();
		}

		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "A.LOGFILEID";
		} else {
			orderString = "UPPER(A.NAME)";
		}

		try {
			String selectString = "SELECT A.LOGFILEID, A.NAME,  "
					+ columnString
					+ "A.CREATEDTIMESTAMP,A.LASTMODTIMESTAMP,A.CREATEDUSERID,A.LASTMODUSERID FROM "
					+ params.getSchema()
					+ ".LOGFILE A "
					+ joinString + " "
					+ "WHERE A.ENVIRONID = ? "
					+ " AND A.LOGFILEID > 0" + " ORDER BY " + orderString;
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1,  environmentId);
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
				LogicalFileQueryBean logicalFileQueryBean = new LogicalFileQueryBean(
					environmentId, rs.getInt(COL_ID), 
					DataUtilities.trimString(rs.getString(COL_NAME)),
					admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.LogicalFile, environmentId), 
					rs.getDate(COL_CREATETIME), 
					DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
					rs.getDate(COL_MODIFYTIME), 
					DataUtilities.trimString(rs.getString(COL_MODIFYBY)));
				result.add(logicalFileQueryBean);
			}
			pst.close();
			rs.close();

			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while querying all Logical Files.",e);
		}
	}

	public void removeLogicalFile(Integer id, Integer environmentId)
			throws DAOException {
		boolean success = false;
		try {
			while (!success) {
				try {
					// Begin Transaction
					DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
					
					// Removing its association and Logical File itself.
					String[] relatedTables = { "LFPFASSOC", "LRLFASSOC", "SECLOGFILE", TABLE_NAME };
					List<String> idNames = new ArrayList<String>();
					idNames.add(COL_ID);
					idNames.add(COL_ENVID);

					for (String table : relatedTables) {
						String deleteLFAssocQuery = generator.getDeleteStatement(params.getSchema(), table,idNames);
						PreparedStatement pst = null;
						while (true) {
							try {
								pst = con.prepareStatement(deleteLFAssocQuery);

								pst.setInt(1, id);
								pst.setInt(2, environmentId);
								pst.execute();
								break;
							} catch (SQLException se) {
								if (con.isClosed()) {
									// lost database connection, so reconnect
									// and retry
									con = DAOFactoryHolder.getDAOFactory().reconnect();
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
					if (DAOFactoryHolder.getDAOFactory().getDAOUOW().isMultiComponentScope()) {
						throw e;
					} else {
						continue;
					}
				} catch (SQLException e) {
					throw DataUtilities.createDAOException("Database error occurred while deleting the Logical File. ",e);
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

	public List<FileAssociationTransfer> getAssociatedPhysicalFiles(Integer id,
			Integer environmentId) throws DAOException {
		List<FileAssociationTransfer> result = new ArrayList<FileAssociationTransfer>();

		try {
            boolean admin = SAFRApplication.getUserSession().isSystemAdministrator(); 
			String schema = params.getSchema();

			String selectString = null;
			if (admin) {
				selectString = "Select B.PHYFILEID, C.NAME AS PHYNAME, B.LFPFASSOCID, B.LOGFILEID, A.NAME AS LOGNAME, "
						+ "B.PARTSEQNBR, B.ENVIRONID, "
	                    + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "                    						
						+ "From "
						+ schema
						+ ".LOGFILE A, "
						+ schema
						+ ".LFPFASSOC B, "
						+ schema
						+ ".PHYFILE C "
						+ "Where A.ENVIRONID = ? "
						+ " AND A.LOGFILEID = ? "
						+ " AND A.ENVIRONID = B.ENVIRONID AND A.LOGFILEID = B.LOGFILEID"
						+ " AND B.ENVIRONID = C.ENVIRONID AND B.PHYFILEID = C.PHYFILEID "
						+ "Order By UPPER(C.NAME)";
			} else {
				selectString = "Select B.PHYFILEID, C.NAME AS PHYNAME, B.LFPFASSOCID, B.LOGFILEID, A.NAME AS LOGNAME, "
						+ "B.PARTSEQNBR, B.ENVIRONID, D.RIGHTS, "
                        + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "                                           						
						+ "From "
						+ schema
						+ ".LOGFILE A INNER JOIN "
						+ schema
						+ ".LFPFASSOC B ON A.ENVIRONID = B.ENVIRONID AND A.LOGFILEID = B.LOGFILEID "
						+ "INNER JOIN "
						+ schema
						+ ".PHYFILE C ON B.ENVIRONID = C.ENVIRONID AND B.PHYFILEID = C.PHYFILEID LEFT OUTER JOIN "
						+ schema
						+ ".SECPHYFILE D ON C.ENVIRONID = D.ENVIRONID AND C.PHYFILEID = D.PHYFILEID"
						+ " AND D.GROUPID = "
						+ SAFRApplication.getUserSession().getGroup().getId()
						+ " Where A.ENVIRONID = ? "
						+ " AND A.LOGFILEID = ? "
						+ " Order By UPPER(C.NAME)";

			}
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, environmentId);
					pst.setInt(2, id);
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
				FileAssociationTransfer fileAssociationTransfer = new FileAssociationTransfer();
				fileAssociationTransfer.setAssociatedComponentId(rs.getInt("PHYFILEID"));
				fileAssociationTransfer.setAssociatedComponentName(
				    DataUtilities.trimString(rs.getString("PHYNAME")));
				fileAssociationTransfer.setAssociatingComponentId(rs.getInt("LOGFILEID"));
				fileAssociationTransfer.setAssociatingComponentName(
				    DataUtilities.trimString(rs.getString("LOGNAME")));
				fileAssociationTransfer.setAssociationId(rs.getInt("LFPFASSOCID"));
				fileAssociationTransfer.setSequenceNo(rs.getInt("PARTSEQNBR"));
				fileAssociationTransfer.setEnvironmentId(rs.getInt("ENVIRONID"));
				fileAssociationTransfer.setAssociatedComponentRights(
                    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.PhysicalFile, environmentId));
                fileAssociationTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
                fileAssociationTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
                fileAssociationTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
                fileAssociationTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));             				
				result.add(fileAssociationTransfer);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while retrieving the Physical Files associated with the Logical File.",e);
		}
	}

	public List<ComponentAssociationTransfer> getAssociatedLogicalRecords(
			Integer id, Integer environmentId) throws DAOException {
		List<ComponentAssociationTransfer> result = new ArrayList<ComponentAssociationTransfer>();
		
		String selectString = "";
		try {
		    boolean admin = SAFRApplication.getUserSession().isSystemAdministrator(); 
			String schema = params.getSchema();
			if (admin) {
				selectString = "Select B.LOGRECID, C.NAME, B.LRLFASSOCID "
						+ "From "
						+ schema
						+ ".LOGFILE A, "
						+ schema
						+ ".LRLFASSOC B, "
						+ schema
						+ ".LOGREC C "
						+ "Where A.ENVIRONID = ? "
						+ " AND A.LOGFILEID = ? "
						+ " AND A.ENVIRONID = B.ENVIRONID AND A.LOGFILEID = B.LOGFILEID"
						+ " AND B.ENVIRONID = C.ENVIRONID AND B.LOGRECID = C.LOGRECID "
						+ "Order By UPPER(C.NAME)";
			} else {
				selectString = "Select B.LOGRECID, C.NAME, B.LRLFASSOCID, D.RIGHTS "
						+ "From "
						+ schema
						+ ".LOGFILE A INNER JOIN "
						+ schema
						+ ".LRLFASSOC B ON A.ENVIRONID = B.ENVIRONID AND A.LOGFILEID = B.LOGFILEID INNER JOIN "
						+ schema
						+ ".LOGREC C ON B.ENVIRONID = C.ENVIRONID AND B.LOGRECID = C.LOGRECID LEFT OUTER JOIN "
						+ schema
						+ ".SECLOGREC D ON C.ENVIRONID = D.ENVIRONID AND C.LOGRECID = D.LOGRECID "
						+ " AND D.GROUPID = ? "
						+ " Where A.ENVIRONID = ? "
						+ " AND A.LOGFILEID = ? Order By UPPER(C.NAME)";
			}
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					if(admin) {
						pst.setInt(1, environmentId);
						pst.setInt(2, id);
					} else {
						pst.setInt(1, SAFRApplication.getUserSession().getGroup().getId());
						pst.setInt(2, environmentId);
						pst.setInt(3, id);						
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
				ComponentAssociationTransfer componentAssociationTransfer = new ComponentAssociationTransfer();
				componentAssociationTransfer.setAssociatedComponentId(rs.getInt("LOGRECID"));
				componentAssociationTransfer.setAssociatedComponentName(DataUtilities.trimString(rs.getString("NAME")));
				componentAssociationTransfer.setAssociationId(rs.getInt("LRLFASSOCID"));				
				componentAssociationTransfer.setAssociatedComponentRights(
				    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
				        rs.getInt("RIGHTS"), ComponentType.LogicalRecord, environmentId));
				result.add(componentAssociationTransfer);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while retrieving the Logical Records associated with the Logical File.",e);
		}
	}

	public List<PhysicalFileQueryBean> queryPossiblePFAssociations(
			Integer environmentId, List<Integer> exceptionList) throws DAOException {

		List<PhysicalFileQueryBean> result = new ArrayList<PhysicalFileQueryBean>();

		try {
            boolean admin = SAFRApplication.getUserSession().isSystemAdministrator(); 

            String exceptionPlaceholders = generator.getPlaceholders(exceptionList.size());

			String selectString;
			if (admin) {
				selectString = "Select PHYFILEID, NAME From "
						+ params.getSchema()
						+ ".PHYFILE Where ENVIRONID = ? "
						+ " AND PHYFILEID > 0";
						if(exceptionList.size() > 0) {
							selectString += " AND PHYFILEID NOT IN (" + exceptionPlaceholders +")";
						}
						selectString += " Order By PHYFILEID";
			} else {
				selectString = "Select A.PHYFILEID, A.NAME,L.RIGHTS From "
						+ params.getSchema()
						+ ".PHYFILE A "
						+ "LEFT OUTER JOIN "
						+ params.getSchema()
						+ ".SECPHYFILE L "
						+ "ON A.ENVIRONID = L.ENVIRONID AND A.PHYFILEID = L.PHYFILEID "
						+ " AND L.GROUPID = ? "
						+ " Where A.ENVIRONID = ? "
						+ " AND A.PHYFILEID > 0";
						if(exceptionList.size() > 0) {
							selectString += " AND A.PHYFILEID NOT IN (" + exceptionPlaceholders +")";
						}
				selectString += " Order By A.PHYFILEID";
			}
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					if (admin) {
						pst.setInt(ndx++, environmentId );
						for(int i=0; i<exceptionList.size(); i++) {
							pst.setInt(ndx++, exceptionList.get(i));
						}
					} else {
						pst.setInt(ndx++, SAFRApplication.getUserSession().getGroup().getId() );
						pst.setInt(ndx++, environmentId );
						for(int i=0; i<exceptionList.size(); i++) {
							pst.setInt(ndx++, exceptionList.get(i));
						}						
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
				PhysicalFileQueryBean physicalFileQueryBean = new PhysicalFileQueryBean(
					environmentId, rs.getInt(i++), 
					DataUtilities.trimString(rs.getString(i++)), null, 
					null, null, null, null, null, 
                   admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.PhysicalFile, environmentId),
					null, null, null, null);
				result.add(physicalFileQueryBean);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while querying all the possible Physical Files which can be associated with the Logical File.",e);
		}
		return result;
	}

	public List<FileAssociationTransfer> persistAssociatedPFs(
			List<FileAssociationTransfer> fileAssociationTransfers,
			Integer logicalFileId) throws DAOException {

		List<FileAssociationTransfer> associatedPFCreates = new ArrayList<FileAssociationTransfer>();
		List<FileAssociationTransfer> associatedPFUpdates = new ArrayList<FileAssociationTransfer>();

		for (FileAssociationTransfer associatePF : fileAssociationTransfers) {
			if (!associatePF.isPersistent()) {
				associatedPFCreates.add(associatePF);
			} else {
				associatedPFUpdates.add(associatePF);
			}
		}
		if (associatedPFCreates.size() > 0) {
			associatedPFCreates = createAssociatedPFs(associatedPFCreates,
					logicalFileId);
		}
		if (associatedPFUpdates.size() > 0) {
			associatedPFUpdates = updateAssociatedPFs(associatedPFUpdates);
		}
		fileAssociationTransfers = new ArrayList<FileAssociationTransfer>();
		fileAssociationTransfers.addAll(associatedPFCreates);
		fileAssociationTransfers.addAll(associatedPFUpdates);
		return fileAssociationTransfers;

	}

	private List<FileAssociationTransfer> createAssociatedPFs(
			List<FileAssociationTransfer> associatedPFCreates,
			Integer logicalFileId) throws DAOException {
		
		// data is either all imported or migrated or none of it is
		boolean isImportOrMigrate = associatedPFCreates.get(0).isForImport()
				|| associatedPFCreates.get(0).isForMigration() ? true : false;

		try {
			String[] columnNames = { COL_ENVID, 
			        "LOGFILEID", "PHYFILEID", "PARTSEQNBR",
					COL_CREATETIME, COL_CREATEBY,
					COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
            if (isImportOrMigrate) {
                names.add(1, "LFPFASSOCID");
            }
			PreparedStatement pst = null;
            ResultSet rs = null;
			while (true) {
				try {
					for (FileAssociationTransfer associatedPFtoCreate : associatedPFCreates) {
						if (pst == null) {
							String statement = generator.getInsertStatement(
									params.getSchema(), TABLE_LF_PF_ASSOC, "LFPFASSOCID",
									names, !isImportOrMigrate);
							pst = con.prepareStatement(statement);
						}
						int i = 1;
						pst.setInt(i++, associatedPFtoCreate.getEnvironmentId());
			            if (isImportOrMigrate) {
			                pst.setInt(i++, associatedPFtoCreate.getAssociationId());
			            }
                        pst.setInt(i++, logicalFileId);
						pst.setInt(i++, associatedPFtoCreate.getAssociatedComponentId());
						pst.setInt(i++, associatedPFtoCreate.getSequenceNo());
						if (isImportOrMigrate) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(associatedPFtoCreate.getCreateTime()));
						}
						pst.setString(i++,isImportOrMigrate ? associatedPFtoCreate.getCreateBy() : safrLogin.getUserId());
						if (isImportOrMigrate) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(associatedPFtoCreate.getModifyTime()));
						}
						pst.setString(i++,isImportOrMigrate ? associatedPFtoCreate.getModifyBy() : safrLogin.getUserId());
						rs = pst.executeQuery();
			            rs.next();
			            int id = rs.getInt(1);          
			            associatedPFtoCreate.setAssociationId(id);
						associatedPFtoCreate.setPersistent(true);
                        if (!isImportOrMigrate) {
                            associatedPFtoCreate.setCreateBy(safrLogin.getUserId());
                            associatedPFtoCreate.setCreateTime(rs.getDate(2));
                            associatedPFtoCreate.setModifyBy(safrLogin.getUserId());
                            associatedPFtoCreate.setModifyTime(rs.getDate(3));                            
                        }
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
			if (pst != null) {
				pst.close();
			}

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while creating associations of Logical File with Physical Files.",e);
		}
		return associatedPFCreates;
	}

	private List<FileAssociationTransfer> updateAssociatedPFs(
			List<FileAssociationTransfer> associatedPFUpdates)
			throws DAOException {
		
		// data is either all imported or migrated or none of it is
		boolean isImportOrMigrate = associatedPFUpdates.get(0).isForImport()
				|| associatedPFUpdates.get(0).isForMigration() ? true : false;

		try {
			List<String> names = new ArrayList<String>();
			names.add("LOGFILEID");
            names.add("PHYFILEID");
			names.add("PARTSEQNBR");
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
			idNames.add("LFPFASSOCID");
			idNames.add(COL_ENVID);

			PreparedStatement pst = null;
			while (true) {
				try {
					for (FileAssociationTransfer associatedPFtoUpdate : associatedPFUpdates) {
						if (pst == null) {
							String statement = generator.getUpdateStatement(
									params.getSchema(), TABLE_LF_PF_ASSOC,
									names, idNames, !isImportOrMigrate);
							pst = con.prepareStatement(statement);
						}

						int i = 1;
                        pst.setInt(i++, associatedPFtoUpdate.getAssociatingComponentId());
						pst.setInt(i++, associatedPFtoUpdate.getAssociatedComponentId());
						pst.setInt(i++, associatedPFtoUpdate.getSequenceNo());
						if (isImportOrMigrate) {
							// create and modify details set from source component
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(associatedPFtoUpdate.getCreateTime()));
							pst.setString(i++, associatedPFtoUpdate.getCreateBy());
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(associatedPFtoUpdate.getModifyTime()));
							pst.setString(i++, associatedPFtoUpdate.getModifyBy());
						} else {
							// create details are untouched
							// lastmodtimestamp is CURRENT_TIMESTAMP
							// lastmoduserid is logged in user
							pst.setString(i++, safrLogin.getUserId());
						}
						pst.setInt(i++, associatedPFtoUpdate.getAssociationId());
						pst.setInt(i++, associatedPFtoUpdate.getEnvironmentId());
	                    if (!isImportOrMigrate) {
	                        ResultSet rs = pst.executeQuery();
	                        rs.next();
	                        associatedPFtoUpdate.setModifyTime(rs.getDate(1));                      
	                        associatedPFtoUpdate.setModifyBy(safrLogin.getUserId());                      
	                        rs.close();                 
	                    } else {
	                        int count  = pst.executeUpdate();   
	                        if (count == 0) {
	                            throw new SAFRNotFoundException("No Rows updated.");
	                        }                       
	                    }
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
			throw DataUtilities.createDAOException("Database error occurred while updating associations of Logical File with Physical Files.",e);
		}
		return associatedPFUpdates;
	}

	public void deleteAssociatedPFs(Integer environmentId, List<Integer> deletionIds)
			throws DAOException {
		try {
			String placeholders = generator.getPlaceholders(deletionIds.size());
			// to in-activate view and update view table before deleting
			// associating PF
			
			// Query 1: In-activate views where association used in view properties
			String updateViewQuery = "Update " + params.getSchema() + "."
					+ "VIEW Set LFPFASSOCID = 0, "
					+ "VIEWSTATUSCD = 'INACT' " + "WHERE ENVIRONID = ? "
					+ " AND LFPFASSOCID IN (" + placeholders + " )";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(updateViewQuery);
					int ndx = 1;
					pst.setInt(ndx++, environmentId);
					for(int i=0; i<deletionIds.size(); i++){
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
			
            // Query 2: In-activate views where association used in column logic text
			updateViewQuery = 
			"UPDATE " + params.getSchema() + ".VIEW " +
			"SET VIEWSTATUSCD = 'INACT' " +
            "WHERE ENVIRONID= ? " +
            " AND VIEWID IN " +
            "(SELECT A.VIEWID FROM " +
            params.getSchema() +
            ".VIEW A," +
            params.getSchema() +
            ".VIEWLOGICDEPEND B" +
            " WHERE A.ENVIRONID=B.ENVIRONID" +
            " AND A.VIEWID=B.VIEWID" +
            " AND B.ENVIRONID= ? " +
            " AND B.LFPFASSOCID IN  (" + placeholders+ " ))";
			
            while (true) {
                try {
                    pst = con.prepareStatement(updateViewQuery);
					int ndx = 1;
					pst.setInt(ndx++, environmentId);
					pst.setInt(ndx++, environmentId);
					for(int i=0; i<deletionIds.size(); i++){
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
			
			// to delete the association
			String statement = "Delete From " + params.getSchema() + "."
					+ TABLE_LF_PF_ASSOC + " Where ENVIRONID = ? "
					+ " AND LFPFASSOCID IN ( " + placeholders + " )";
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int ndx = 1;
					pst.setInt(ndx++, environmentId);
					for(int i=0; i<deletionIds.size(); i++){
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
			throw DataUtilities.createDAOException(
			    "Database error occurred while deleting associations of Logical File with Physical Files.",e);
		}
	}

    public List<DependentComponentTransfer> getAssociatedLookupDependencies(
        Integer environmentId, Integer lfId) throws DAOException  {
        List<DependentComponentTransfer> dependentComponents = new ArrayList<DependentComponentTransfer>();

        String schema = params.getSchema();
        
        String selectString =  "SELECT A.LOOKUPID, A.NAME " + 
                               "FROM " + schema + ".LOOKUP A, " +
                               "     " + schema + ".LOOKUPSTEP B, " +
                               "     " + schema + ".LRLFASSOC C, " +
                               "     " + schema + ".LOGREC D " +
                               "WHERE A.ENVIRONID=B.ENVIRONID " +
                               "AND A.LOOKUPID=B.LOOKUPID " +
                               "AND B.ENVIRONID=C.ENVIRONID " +
                               "AND B.LRLFASSOCID=C.LRLFASSOCID " +
                               "AND C.ENVIRONID=? " +
                               "AND C.LOGFILEID=? " +
                               "AND C.ENVIRONID=D.ENVIRONID " +
                               "AND C.LOGRECID=D.LOGRECID " +
                               "AND ( D.LOOKUPEXITID IS NULL OR " +
                               "      D.LOOKUPEXITID=0 )";

        try {
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, environmentId);
                    pst.setInt(2, lfId);
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
                Integer joinId = rs.getInt("LOOKUPID");                
                DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
                depCompTransfer.setId(joinId);
                depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
                depCompTransfer.setDependencyInfo("[Lookup Path Properties]");                
                dependentComponents.add(depCompTransfer);
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving the list of dependencies of LF to a Lookup Path.",e);
        }
        
        return dependentComponents;
    }
        
	
	public List<DependentComponentTransfer> getAssociatedPFViewDependencies(
			Integer environmentId, Integer LFPFAssociationId)
			throws DAOException {
		List<DependentComponentTransfer> dependentComponents = new ArrayList<DependentComponentTransfer>();
		// to get the views which are associated to the PF.
		String schema = params.getSchema();
		try {
		    // Query 1: Views using LF-PF in output properties
			String selectString = "Select VIEWID, NAME From " + schema
					+ ".VIEW " + "Where ENVIRONID = ? "
					+ " AND LFPFASSOCID = ? ORDER BY VIEWID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1,  environmentId);
					pst.setInt(2, LFPFAssociationId);
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
                Integer viewId = rs.getInt("VIEWID");                
                DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
                depCompTransfer.setId(viewId);
                depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
                depCompTransfer.setDependencyInfo("[View Properties]");                
                dependentComponents.add(depCompTransfer);
            }
			pst.close();
			rs.close();
			
            // Query 2: Views using LF-PF in column formula
            selectString = "SELECT A.VIEWID,A.NAME,B.COLUMNNUMBER,B.HDRLINE1 " +
            "FROM " +
            schema +
            ".VIEW A," +
            schema +
            ".VIEWCOLUMN B," +
            schema +
            ".VIEWCOLUMNSOURCE C," +
            schema +
            ".VIEWLOGICDEPEND D" +
            " WHERE A.ENVIRONID=B.ENVIRONID" +
            " AND A.VIEWID=B.VIEWID" +
            " AND B.ENVIRONID=C.ENVIRONID" +
            " AND B.VIEWID=C.VIEWID" +
            " AND B.VIEWCOLUMNID=C.VIEWCOLUMNID" +
            " AND C.ENVIRONID=D.ENVIRONID" +
            " AND C.VIEWID=D.VIEWID" +
            " AND C.VIEWCOLUMNSOURCEID=D.PARENTID" +
            " AND D.ENVIRONID= ? " +
            " AND D.LFPFASSOCID= ? " +
            " ORDER BY A.VIEWID";
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1,  environmentId);
                    pst.setInt(2, LFPFAssociationId);
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
            while (rs.next()) {
                Integer viewId = rs.getInt("VIEWID");
                DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
                depCompTransfer.setId(viewId);
                depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
                String colName = "";
                String heading1 = rs.getString("HDRLINE1");
                if (heading1 != null && !heading1.equals("")) {
                    colName = ", " + heading1;
                }                    
                depCompTransfer.setDependencyInfo("[Col " + rs.getInt("COLUMNNUMBER") + colName + ", Logic Text]");
                dependentComponents.add(depCompTransfer);                   
            }
            pst.close();
            rs.close();
            
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while retrieving the list of dependencies of LF-PF association to a View.",e);
		}
		return dependentComponents;
	}

	public Map<Integer, List<DependentComponentTransfer>> getAssociatedPFViewDependencies(
			Integer environmentId, List<Integer> LFPFAssociationIds,
			List<Integer> exceptionList) throws DAOException {
		String LFPFAssociationIdPlaceholders = "";
		String exceptionListPlaceholders = "";
		Map<Integer, List<DependentComponentTransfer>> nonExistentDependentComponents = new HashMap<Integer, List<DependentComponentTransfer>>();
		if (LFPFAssociationIds != null) {
			LFPFAssociationIdPlaceholders = generator.getPlaceholders(LFPFAssociationIds.size());
		} else {
			return nonExistentDependentComponents;
		}

		exceptionListPlaceholders = generator.getPlaceholders(exceptionList.size());
		// to get the views which are associated to the PF.
		String schema = params.getSchema();
		try {
		    // Query 1: Views using LF-PF in output properties
			String selectString = "Select VIEWID, NAME, LFPFASSOCID From "
					+ schema
					+ ".VIEW "
					+ "Where ENVIRONID = ? "
					+ " AND LFPFASSOCID IN ( " + LFPFAssociationIdPlaceholders + ")"; //if not null then assuming it has entries
					if(exceptionList.size() > 0) {
						selectString += " AND VIEWID NOT IN (" + exceptionListPlaceholders + " ) ";
					}
					selectString += " ORDER BY LFPFASSOCID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					pst.setInt(ndx++, environmentId);
					for(int i=0; i<LFPFAssociationIds.size(); i++) {
						pst.setInt(ndx++,  LFPFAssociationIds.get(i));
					}
					for(int i=0; i<exceptionList.size(); i++) {
						pst.setInt(ndx++,  exceptionList.get(i));
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
				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rs.getInt("VIEWID"));
				depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
				if (nonExistentDependentComponents.containsKey(rs.getInt("LFPFASSOCID"))) {
					nonExistentDependentComponents.get(
					    rs.getInt("LFPFASSOCID")).add(depCompTransfer);
				} else {
					List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
					depCompTransfers.add(depCompTransfer);
					nonExistentDependentComponents.put(
					    rs.getInt("LFPFASSOCID"), depCompTransfers);
				}
			}
			pst.close();
			rs.close();
			
            // Query 2: Views using LF-PF in column formula
            selectString = "SELECT A.VIEWID,A.NAME,B.LFPFASSOCID " +
            "FROM " +
            schema +
            ".VIEW A," +
            schema +
            ".VIEWLOGICDEPEND B" +
            " WHERE A.ENVIRONID=B.ENVIRONID" +
            " AND A.VIEWID=B.VIEWID" +
            " AND B.ENVIRONID= ? ";
			if(exceptionList.size() > 0) {
				selectString += " AND B.VIEWID NOT IN (" + exceptionListPlaceholders + " ) ";
			}
			selectString += " AND B.LFPFASSOCID IN ( " + LFPFAssociationIdPlaceholders + ")"; //if not null then assuming it has entries
			selectString += " ORDER BY B.LFPFASSOCID";
			
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
					int ndx = 1;
					pst.setInt(ndx++, environmentId);
					for(int i=0; i<exceptionList.size(); i++) {
						pst.setInt(ndx++,  exceptionList.get(i));
					}
					for(int i=0; i<LFPFAssociationIds.size(); i++) {
						pst.setInt(ndx++,  LFPFAssociationIds.get(i));
					}
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

            while (rs.next()) {
                DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
                depCompTransfer.setId(rs.getInt("VIEWID"));
                depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
                if (nonExistentDependentComponents.containsKey(rs.getInt("LFPFASSOCID"))) {
                    nonExistentDependentComponents.get(rs.getInt("LFPFASSOCID")).add(depCompTransfer);
                } else {
                    List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
                    depCompTransfers.add(depCompTransfer);
                    nonExistentDependentComponents.put(rs.getInt("LFPFASSOCID"), depCompTransfers);
                }
            }
            pst.close();
            rs.close();
            
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving list of dependencies of LF-PF association to a View which are newly created and not retrieved in previous dependency checks.",e);
		}
		return nonExistentDependentComponents;
	}

	public FileAssociationTransfer getLFPFAssociation(Integer associationId,
			Integer environmentId) throws DAOException {
		FileAssociationTransfer transfer = new FileAssociationTransfer();

		try {
			String schema = params.getSchema();

			String selectString = "Select A.ENVIRONID, A.LFPFASSOCID, "
					+ "A.LOGFILEID, C.NAME AS LOGNAME, A.PHYFILEID, "
					+ "B.NAME AS PHYNAME, A.PARTSEQNBR, "
                    + "A.CREATEDTIMESTAMP, A.CREATEDUSERID, A.LASTMODTIMESTAMP, A.LASTMODUSERID "                    
					+ "From "
					+ schema
					+ ".LFPFASSOC A, "
					+ schema
					+ ".PHYFILE B, "
					+ schema
					+ ".LOGFILE C "
					+ "Where A.ENVIRONID = ? "
					+ " AND A.LFPFASSOCID = ? "
					+ " AND A.ENVIRONID = B.ENVIRONID AND A.PHYFILEID = B.PHYFILEID"
					+ " AND A.ENVIRONID = C.ENVIRONID AND A.LOGFILEID = C.LOGFILEID";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, environmentId );
					pst.setInt(2, associationId );
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
				transfer.setEnvironmentId(rs.getInt("ENVIRONID"));
				transfer.setAssociationId(rs.getInt("LFPFASSOCID"));
				transfer.setAssociatingComponentId(rs.getInt("LOGFILEID"));
				transfer.setAssociatingComponentName(
				    DataUtilities.trimString(rs.getString("LOGNAME")));
				transfer.setAssociatedComponentId(rs.getInt("PHYFILEID"));
				transfer.setAssociatedComponentName(
				    DataUtilities.trimString(rs.getString("PHYNAME")));
				transfer.setSequenceNo(rs.getInt("PARTSEQNBR"));
				transfer.setCreateTime(rs.getDate(COL_CREATETIME));
				transfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
				transfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
				transfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));
			} else {
				logger.info("No LogicalFile-PhysicalFile association found in the database with the ID: "+ associationId);
			}
			pst.close();
			rs.close();
			return transfer;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred retrieving the LF-PF association with the specified association ID.",e);
		}
	}

	public FileAssociationTransfer getLFPFAssociation(Integer logicalFileId,
			Integer physicalFileId, Integer environmentId) throws DAOException {
		FileAssociationTransfer transfer = null;

		try {
			String schema = params.getSchema();

			String selectString = "Select A.ENVIRONID, A.LFPFASSOCID, "
					+ "A.LOGFILEID, C.NAME AS LOGNAME, A.PHYFILEID, "
					+ "B.NAME AS PHYNAME, A.PARTSEQNBR, "
                    + "A.CREATEDTIMESTAMP, A.CREATEDUSERID, A.LASTMODTIMESTAMP, A.LASTMODUSERID "                    					
					+ "From "
					+ schema
					+ ".LFPFASSOC A, "
					+ schema
					+ ".PHYFILE B, "
					+ schema
					+ ".LOGFILE C "
					+ "Where A.ENVIRONID = ? "
					+ " AND A.LOGFILEID = ? "
					+ " AND A.PHYFILEID = ? "
					+ " AND A.ENVIRONID = B.ENVIRONID AND A.PHYFILEID = B.PHYFILEID"
					+ " AND A.ENVIRONID = C.ENVIRONID AND A.LOGFILEID = C.LOGFILEID";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1,  environmentId);
					pst.setInt(2,  logicalFileId);
					pst.setInt(3,  physicalFileId);
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
				transfer = new FileAssociationTransfer();
				transfer.setEnvironmentId(rs.getInt("ENVIRONID"));
				transfer.setAssociationId(rs.getInt("LFPFASSOCID"));
				transfer.setAssociatingComponentId(rs.getInt("LOGFILEID"));
				transfer.setAssociatingComponentName(
				    DataUtilities.trimString(rs.getString("LOGNAME")));
				transfer.setAssociatedComponentId(rs.getInt("PHYFILEID"));
				transfer.setAssociatedComponentName(
				    DataUtilities.trimString(rs.getString("PHYNAME")));
				transfer.setSequenceNo(rs.getInt("PARTSEQNBR"));
                transfer.setCreateTime(rs.getDate(COL_CREATETIME));
                transfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
                transfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
                transfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));				
			} else {
				logger
						.info("No LogicalFile-PhysicalFile association found in Environment ["
								+ environmentId
								+ "] with parentfileid ["
								+ logicalFileId
								+ "] and childpartitionid ["
								+ physicalFileId + "]");
			}
			pst.close();
			rs.close();
			return transfer;

		} catch (SQLException e) {
			String msg = "Database error occurred retrieving the LogicalFile-PhysicalFile association from Environment ["
					+ environmentId
					+ "] with parentfileid ["
					+ logicalFileId
					+ "] and childpartitionid [" + physicalFileId + "]";
			throw DataUtilities.createDAOException(msg, e);
		}
	}

	public List<FileAssociationTransfer> getLFPFAssociations(
			Integer environmentId) throws DAOException {
		List<FileAssociationTransfer> result = new ArrayList<FileAssociationTransfer>();

		try {
            boolean admin = SAFRApplication.getUserSession().isSystemAdministrator(); 
			String schema = params.getSchema();

			String selectString = null;
			if (admin) {
				selectString = "Select B.PHYFILEID, C.NAME AS PHYNAME, B.LFPFASSOCID, B.LOGFILEID, A.NAME AS LOGNAME, "
						+ "B.PARTSEQNBR, B.ENVIRONID, "
	                    + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "                                       						
						+ "From "
						+ schema
						+ ".LOGFILE A, "
						+ schema
						+ ".LFPFASSOC B, "
						+ schema
						+ ".PHYFILE C "
						+ "Where A.ENVIRONID = ? "
						+ " AND A.ENVIRONID = B.ENVIRONID AND A.LOGFILEID = B.LOGFILEID"
						+ " AND B.ENVIRONID = C.ENVIRONID AND B.PHYFILEID = C.PHYFILEID"
						+ " AND B.LFPFASSOCID <> 0" // exclude dummy rows
						+ " Order By B.LFPFASSOCID";
			} else {
				selectString = "Select B.PHYFILEID, C.NAME AS PHYNAME, B.LFPFASSOCID, B.LOGFILEID, A.NAME AS LOGNAME, "
						+ "B.PARTSEQNBR, B.ENVIRONID, D.RIGHTS, "
	                    + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "                                       						
						+ "From "
						+ schema
						+ ".LOGFILE A INNER JOIN "
						+ schema
						+ ".LFPFASSOC B ON A.ENVIRONID = B.ENVIRONID AND A.LOGFILEID = B.LOGFILEID "
						+ "INNER JOIN "
						+ schema
						+ ".PHYFILE C ON B.ENVIRONID = C.ENVIRONID AND B.PHYFILEID = C.PHYFILEID LEFT OUTER JOIN "
						+ schema
						+ ".SECPHYFILE D ON C.ENVIRONID = D.ENVIRONID AND C.PHYFILEID = D.PHYFILEID"
						+ " AND D.GROUPID = "
						+ SAFRApplication.getUserSession().getGroup().getId()
						+ " Where A.ENVIRONID = ? "
						+ " AND B.LFPFASSOCID <> 0" // exclude dummy rows
						+ " Order By B.LFPFASSOCID";

			}
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, environmentId );
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
				FileAssociationTransfer fileAssociationTransfer = new FileAssociationTransfer();
				fileAssociationTransfer.setAssociatedComponentId(rs.getInt("PHYFILEID"));
				fileAssociationTransfer.setAssociatedComponentName(
				    DataUtilities.trimString(rs.getString("PHYNAME")));
				fileAssociationTransfer.setAssociatingComponentId(rs.getInt("LOGFILEID"));
				fileAssociationTransfer.setAssociatingComponentName(
				    DataUtilities.trimString(rs.getString("LOGNAME")));
				fileAssociationTransfer.setAssociationId(rs.getInt("LFPFASSOCID"));
				fileAssociationTransfer.setSequenceNo(rs.getInt("PARTSEQNBR"));
				fileAssociationTransfer.setEnvironmentId(rs.getInt("ENVIRONID"));
				fileAssociationTransfer.setAssociatedComponentRights(
				    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
				        rs.getInt("RIGHTS"), ComponentType.PhysicalFile, environmentId));
				fileAssociationTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
				fileAssociationTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
				fileAssociationTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
				fileAssociationTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));             
				
				result.add(fileAssociationTransfer);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			String msg = "Database error occurred while retrieving all LF/PF associations from Environment ["
					+ environmentId + "]. ";
			throw DataUtilities.createDAOException(msg, e);
		}
	}
	
	public List<DependentComponentTransfer> getAssociatedLogicalRecordsWithOneAssociatedLF(
			Integer environmentId, Integer logicalFileId) throws DAOException {
		List<DependentComponentTransfer> dependencies = new ArrayList<DependentComponentTransfer>();
		try {

			String query = "SELECT A.LOGFILEID, A.LOGRECID, C.NAME FROM "
					+ params.getSchema()
					+ ".LOGREC C, "
					+ params.getSchema()
					+ ".LRLFASSOC A  INNER JOIN (SELECT ENVIRONID,LOGRECID FROM "
					+ params.getSchema()
					+ ".LRLFASSOC GROUP BY LOGRECID,ENVIRONID HAVING COUNT(LOGRECID) = 1) B "
					+ "ON B.LOGRECID = A.LOGRECID AND B.ENVIRONID=A.ENVIRONID "
					+ "WHERE A.LOGFILEID=? AND A.ENVIRONID=C.ENVIRONID AND A.ENVIRONID =? AND C.LOGRECID = A.LOGRECID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {

					pst = con.prepareStatement(query);
					pst.setInt(1, logicalFileId);
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
				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rs.getInt("LOGRECID"));
				depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
				dependencies.add(depCompTransfer);
			}

			pst.close();
			rs.close();

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving Logical Record dependencies of Logical File.",e);
		}

		return dependencies;
	}

	public Map<ComponentType, List<DependentComponentTransfer>> getDependencies(
			Integer environmentId, Integer logicalFileId) throws DAOException {
		Map<ComponentType, List<DependentComponentTransfer>> dependencies = new HashMap<ComponentType, List<DependentComponentTransfer>>();

		List<DependentComponentTransfer> lookupPathDependencies = new ArrayList<DependentComponentTransfer>();
		List<DependentComponentTransfer> viewDependencies = new ArrayList<DependentComponentTransfer>();
		try {
			// retrieving Lookup Paths where this LF is used in target LR-LF
			// pair.
			String selectDependentLFs = "Select B.LOOKUPID, B.NAME From "
					+ params.getSchema()
					+ ".LOOKUP B INNER JOIN "
					+ params.getSchema()
					+ ".LOOKUPSTEP C ON B.ENVIRONID=C.ENVIRONID AND B.LOOKUPID=C.LOOKUPID INNER JOIN "
					+ params.getSchema()
					+ ".LRLFASSOC D ON C.ENVIRONID=D.ENVIRONID AND C.LRLFASSOCID=D.LRLFASSOCID WHERE D.ENVIRONID=? AND D.LOGFILEID=?";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectDependentLFs);
					pst.setInt(1, environmentId);
					pst.setInt(2, logicalFileId);
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
				depCompTransfer.setId(rs.getInt("LOOKUPID"));
				depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
				lookupPathDependencies.add(depCompTransfer);
			}

			if (lookupPathDependencies.size() > 0) {
				dependencies.put(ComponentType.LookupPath,
						lookupPathDependencies);
			}
			pst.close();
			rs.close();

			// retrieving Views where this LF is used as Source
			selectDependentLFs = "Select DISTINCT B.VIEWID, B.NAME From "
					+ params.getSchema()
					+ ".VIEW B WHERE B.VIEWID IN ( SELECT DISTINCT C.VIEWID FROM "
					+ params.getSchema()
					+ ".VIEWSOURCE C INNER JOIN "
					+ params.getSchema()
					+ ".LRLFASSOC D ON C.INLRLFASSOCID=D.LRLFASSOCID AND C.ENVIRONID = D.ENVIRONID "
					+ "WHERE D.ENVIRONID=? AND D.LOGFILEID=?)";
			pst = con.prepareStatement(selectDependentLFs);
			pst.setInt(1, environmentId);
			pst.setInt(2, logicalFileId);
			rs = pst.executeQuery();

			while (rs.next()) {
				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rs.getInt("VIEWID"));
				depCompTransfer.setName(DataUtilities.trimString(rs
						.getString("NAME")));
				depCompTransfer.setDependencyInfo("[View Source]");
				viewDependencies.add(depCompTransfer);
			}
			pst.close();
			rs.close();

			// retrieving Views where this LF is used in Extract Column Logic Text.
			selectDependentLFs = "SELECT DISTINCT A.VIEWID, E.NAME, D.COLUMNNUMBER, D.HDRLINE1 FROM "
					+ params.getSchema()
					+ ".VIEWLOGICDEPEND A"
					+ " INNER JOIN "
					+ params.getSchema()
					+ ".LFPFASSOC B ON A.LFPFASSOCID = B.LFPFASSOCID AND A.ENVIRONID=B.ENVIRONID"
					+ " INNER JOIN "
					+ params.getSchema()
					+ ".VIEWCOLUMNSOURCE C ON A.PARENTID=C.VIEWCOLUMNSOURCEID AND A.ENVIRONID=C.ENVIRONID"
					+ " INNER JOIN "
					+ params.getSchema()
					+ ".VIEWCOLUMN D ON C.VIEWCOLUMNID=D.VIEWCOLUMNID AND C.ENVIRONID=D.ENVIRONID"
					+ " INNER JOIN "
					+ params.getSchema()
					+ ".VIEW E ON A.VIEWID=E.VIEWID AND A.ENVIRONID=E.ENVIRONID"
					+ " WHERE A.ENVIRONID= ? AND B.LOGFILEID= ? AND B.LFPFASSOCID > 0" 
					+ " AND A.LOGICTYPECD = 2";
			while (true) {
				try {
					pst = con.prepareStatement(selectDependentLFs);
					pst.setInt(1, environmentId);
					pst.setInt(2, logicalFileId);
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
			while (rs.next()) {
				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rs.getInt("VIEWID"));
				depCompTransfer.setName(DataUtilities.trimString(rs
						.getString("NAME")));
				String colName = "";
				String heading1 = rs.getString("HDRLINE1");
				if (heading1 != null && !heading1.equals("")) {
					colName = ", " + heading1;
				}
				depCompTransfer.setDependencyInfo(
				    "[Col " + rs.getInt("COLUMNNUMBER") + colName + ", Logic Text]");
				viewDependencies.add(depCompTransfer);
			}
			pst.close();
			rs.close();			

            // retrieving Views where this LF is used in Extract Output Logic Text.
            selectDependentLFs = "SELECT DISTINCT A.VIEWID, D.NAME FROM " +
                params.getSchema() + ".VIEWLOGICDEPEND A, " +
                params.getSchema() + ".VIEWSOURCE B, " +
                params.getSchema() + ".LFPFASSOC C, " +
                params.getSchema() + ".LOGFILE D " +
                "WHERE A.ENVIRONID=B.ENVIRONID " +
                "AND A.PARENTID=B.VIEWSOURCEID " +
                "AND A.ENVIRONID=C.ENVIRONID " +
                "AND A.LFPFASSOCID=C.LFPFASSOCID " +
                "AND C.ENVIRONID=D.ENVIRONID " +
                "AND C.LOGFILEID=D.LOGFILEID " +
                "AND A.LOGICTYPECD=5 " +
                "AND D.ENVIRONID=?" +
                "AND D.LOGFILEID=?";
            while (true) {
                try {
                    pst = con.prepareStatement(selectDependentLFs);
                    pst.setInt(1, environmentId);
                    pst.setInt(2, logicalFileId);
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
            while (rs.next()) {
                DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
                depCompTransfer.setId(rs.getInt("VIEWID"));
                depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
                depCompTransfer.setDependencyInfo("[View Output]");
                viewDependencies.add(depCompTransfer);
            }
            pst.close();
            rs.close();         
			
			// retrieving Views where this LF is used in output.
			selectDependentLFs = "Select DISTINCT B.VIEWID, B.NAME From "
					+ params.getSchema()
					+ ".VIEW B INNER JOIN "
					+ params.getSchema()
					+ ".LFPFASSOC C ON B.LFPFASSOCID=C.LFPFASSOCID AND B.ENVIRONID=C.ENVIRONID "
					+ "WHERE C.ENVIRONID=? AND C.LOGFILEID=?";
			while (true) {
				try {

					pst = con.prepareStatement(selectDependentLFs);
					pst.setInt(1, environmentId);
					pst.setInt(2, logicalFileId);
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
			while (rs.next()) {
				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rs.getInt("VIEWID"));
				depCompTransfer.setName(DataUtilities.trimString(rs
						.getString("NAME")));
				depCompTransfer.setDependencyInfo("[View Properties]");
				viewDependencies.add(depCompTransfer);
			}

			if (viewDependencies.size() > 0) {
				dependencies.put(ComponentType.View, viewDependencies);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving dependencies of Logical File.",e);
		}

		return dependencies;

	}

	@Override
	public Integer getLFPFAssocID(int environid, String lfName, String pfName) {
		Integer result = null;
		try {
			String schema = params.getSchema();

			String selectString = "select lfpfassocid from " + schema + ".lfpfassoc a"
					+ " join " + schema + ".logfile l on l.environid=a.environid and l.logfileid = a.logfileid"
					+ " join " + schema + ".phyfile p on a.environid=p.environid and p.phyfileid = a.phyfileid"
					+ " where l.environid = ? and UPPER(l.name) = ? and UPPER(p.name) = ?";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, environid);
					pst.setString(2, lfName.toUpperCase());
					pst.setString(3, pfName.toUpperCase());
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
				result = rs.getInt(1);
			} else {
				logger
						.info("No LogicalFile-PhysicalFile association found in Environment ["
								+ environid
								+ "] with parentfileid "
								+ lfName
								+ "] and childpartitionid ["
								+ pfName + "]");
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			String msg = "No LogicalFile-PhysicalFile association found in Environment ["
					+ environid
					+ "] with parentfileid "
					+ lfName
					+ "] and childpartitionid ["
					+ pfName + "]";
			throw DataUtilities.createDAOException(msg, e);
		}
	}
}
