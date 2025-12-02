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
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.ibm.safr.we.data.dao.PhysicalFileDAO;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.internal.data.PGSQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;

/**
 * This class is used to implement the unimplemented methods of
 * <b>PhysicaFileDAO</b>. This class contains the methods related to Physical
 * File metadata component which requires database access.
 * 
 */

public class PGPhysicalFileDAO implements PhysicalFileDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGPhysicalFileDAO");

	private static final String TABLE_NAME = "PHYFILE";

	// Common fields
	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "PHYFILEID";
	private static final String COL_NAME = "NAME";
	private static final String COL_FILETYPE = "FILETYPECD";
    private static final String COL_DISKFILETYPE = "DISKFILETYPECD";
	private static final String COL_ACCESSMETHOD = "ACCESSMETHODCD";
	private static final String COL_EXITID = "READEXITID";
	private static final String COL_EXITPARAM = "READEXITSTARTUP";
	private static final String COL_COMMENT = "COMMENTS";
	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";

	// SQL fields
    private static final String COL_DBMSSUBSYS = "DBMSSUBSYS";
	private static final String COL_DBMSTABLE = "DBMSTABLE";
	private static final String COL_DBMSROWFMTCD = "DBMSROWFMTCD";
	private static final String COL_DBMSINCLNULLSIND = "DBMSINCLNULLSIND";
	private static final String COL_DBMSSQL = "DBMSSQL";

	// DataSet fields
	private static final String COL_DDNAMEINPUT = "DDNAMEINPUT";
    private static final String COL_DSN = "DSN";
    private static final String COL_MINRECORDLENGTH = "MINRECLEN";
    private static final String COL_MAXRECORDLENGTH = "MAXRECLEN";
    
    private static final String COL_DDNAMEOUTPUT = "DDNAMEOUTPUT";
    private static final String COL_LRECL = "LRECL";
	private static final String COL_RECFM = "RECFM";

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
	public PGPhysicalFileDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	public List<PhysicalFileQueryBean> queryAllPhysicalFiles(
			Integer environmentId, SortType sortType) throws DAOException {
		List<PhysicalFileQueryBean> result = new ArrayList<PhysicalFileQueryBean>();

        boolean admin = SAFRApplication.getUserSession().isSystemAdministrator();
        
		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "A.PHYFILEID";
		} else {
			orderString = "UPPER(A.NAME)";
		}

		try {
			String selectString = "";
			if (admin) {
				selectString = "SELECT A.PHYFILEID, A.NAME, A.FILETYPECD, A.DISKFILETYPECD, A.ACCESSMETHODCD,"
				        + "A.DDNAMEINPUT, A.DSN, A.DDNAMEOUTPUT, A.CREATEDTIMESTAMP, A.LASTMODTIMESTAMP,"
				        + "A.CREATEDUSERID,A.LASTMODUSERID FROM "
						+ params.getSchema() + ".PHYFILE A "
						+ "WHERE A.ENVIRONID = ? "
						+ " AND A.PHYFILEID > 0 " + "ORDER BY " + orderString;
			} else {
				selectString = "SELECT A.PHYFILEID, A.ENVIRONID, A.NAME, A.FILETYPECD, A.DISKFILETYPECD, A.ACCESSMETHODCD,"
				        + "A.DDNAMEINPUT, A.DSN, A.DDNAMEOUTPUT, L.GROUPID, L.RIGHTS, "
						+ "A.CREATEDTIMESTAMP, A.LASTMODTIMESTAMP,A.CREATEDUSERID,A.LASTMODUSERID FROM "
						+ params.getSchema() + ".PHYFILE A LEFT OUTER JOIN "
						+ params.getSchema() + ".SECPHYFILE L ON L.ENVIRONID = A.ENVIRONID AND L.PHYFILEID = A.PHYFILEID"
						+ " AND L.GROUPID= ? " 
						+ " WHERE A.ENVIRONID = ? "
						+ " AND A.PHYFILEID>0 ORDER BY "
						+ orderString;

			}
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					if (admin) {
						pst.setInt(ndx++, environmentId);
					} else {
						pst.setInt(ndx++, SAFRApplication.getUserSession().getGroup().getId());
						pst.setInt(ndx++, environmentId);
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

				PhysicalFileQueryBean physicalFileBean = new PhysicalFileQueryBean(
					environmentId, rs.getInt(COL_ID), 
					DataUtilities.trimString(rs.getString(COL_NAME)),
                    DataUtilities.trimString(rs.getString(COL_FILETYPE)),
                    DataUtilities.trimString(rs.getString(COL_DISKFILETYPE)),
                    DataUtilities.trimString(rs.getString(COL_ACCESSMETHOD)),
                    DataUtilities.trimString(rs.getString(COL_DDNAMEINPUT)),
                    DataUtilities.trimString(rs.getString(COL_DSN)),
                    DataUtilities.trimString(rs.getString(COL_DDNAMEOUTPUT)),
                    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.PhysicalFile, environmentId), 
					rs.getDate(COL_CREATETIME), 
					DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
					rs.getDate(COL_MODIFYTIME), 
					DataUtilities.trimString(rs.getString(COL_MODIFYBY)));
				result.add(physicalFileBean);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while querying all Physical Files.",e);
		}
	}

	public PhysicalFileTransfer getPhysicalFile(Integer id,
			Integer environmentId) throws DAOException {

		PhysicalFileTransfer result = null;
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
				logger.info("No such Physical File in Env " + environmentId + " with ID : " + id);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the Physical File with id ["+ id + "]", e);
		}
		return result;
	}

	public PhysicalFileTransfer getPhysicalFile(String name,
			Integer environmentId) throws DAOException {

		PhysicalFileTransfer result = null;
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_NAME);
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
				logger.info("No such Physical File in Env " + environmentId + " with ID : " + name);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the Physical File with id ["+ name + "]", e);
		}
		return result;
	}

	/**
	 * This function is used to generate a transfer object for the Physical
	 * File.
	 * 
	 * @param rs
	 *            : The result set of a database query run on PHYFILE
	 *            table with which the values for the transfer objects are set.
	 * @return A transfer object for the Physical File with values set according
	 *         to the result set.
	 * @throws SQLException
	 */
	private PhysicalFileTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		PhysicalFileTransfer physicalFile = new PhysicalFileTransfer();
		physicalFile.setEnvironmentId(rs.getInt(COL_ENVID));
		physicalFile.setPartitionId(rs.getInt(COL_ID));
		physicalFile.setPartitionName(DataUtilities.trimString(rs.getString(COL_NAME)));
		physicalFile.setFileTypeCode(DataUtilities.trimString(rs.getString(COL_FILETYPE)));
        physicalFile.setDiskFileTypeCode(DataUtilities.trimString(rs.getString(COL_DISKFILETYPE)));
		physicalFile.setAccessMethodCode(DataUtilities.trimString(rs.getString(COL_ACCESSMETHOD)));
		physicalFile.setReadExitId(rs.getInt(COL_EXITID));
		if(rs.wasNull())  {
			physicalFile.setReadExitId(null);
		}
		physicalFile.setReadExitParams(DataUtilities.trimString(rs.getString(COL_EXITPARAM)));
		physicalFile.setComments(DataUtilities.trimString(rs.getString(COL_COMMENT)));
		physicalFile.setTableName(DataUtilities.trimString(rs.getString(COL_DBMSTABLE)));
		physicalFile.setRowFormatCode(DataUtilities.trimString(rs.getString(COL_DBMSROWFMTCD)));
		physicalFile.setIncludeNullIndicators(DataUtilities.intToBoolean(rs.getInt(COL_DBMSINCLNULLSIND)));
		physicalFile.setSqlStatement(DataUtilities.trimString(rs.getString(COL_DBMSSQL)));
		physicalFile.setSubSystem(DataUtilities.trimString(rs.getString(COL_DBMSSUBSYS)));
		physicalFile.setInputDDName(DataUtilities.trimString(rs.getString(COL_DDNAMEINPUT)));
        physicalFile.setDatasetName(DataUtilities.trimString(rs.getString(COL_DSN)));
		physicalFile.setMinRecordLen(rs.getInt(COL_MINRECORDLENGTH));
		physicalFile.setMaxRecordLen(rs.getInt(COL_MAXRECORDLENGTH));
		physicalFile.setOutputDDName(DataUtilities.trimString(rs.getString(COL_DDNAMEOUTPUT)));
		physicalFile.setRecfm(DataUtilities.trimString(rs.getString(COL_RECFM)));
		physicalFile.setLrecl(rs.getInt(COL_LRECL));
		physicalFile.setCreateTime(rs.getDate(COL_CREATETIME));
		physicalFile.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		physicalFile.setModifyTime(rs.getDate(COL_MODIFYTIME));
		physicalFile.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

		return physicalFile;
	}

	public PhysicalFileTransfer persistPhysicalFile(
			PhysicalFileTransfer physicalFile) throws DAOException,
			SAFRNotFoundException {

		if (!physicalFile.isPersistent()) {
			return (createPhysicalFile(physicalFile));
		} else {
			return (updatePhysicalFile(physicalFile));
		}

	}

	/**
	 * This function is to create a Physical File in PHYFILE in database.
	 * 
	 * @param physicalFile
	 *            : The transfer object which contains the values which are to
	 *            be set in the fields for the corresponding Physical File which
	 *            is being created.
	 * @return The transfer object which contains the values which are received
	 *         from the PHYFILE for the Physical File which is created.
	 * @throws DAOException
	 */
	private PhysicalFileTransfer createPhysicalFile(
			PhysicalFileTransfer physicalFile) throws DAOException {
		
	    SAFRApplication.getTimingMap().startTiming("PGPhysicalFileDAO.createPhysicalFile");	    
		boolean isImportOrMigrate = physicalFile.isForImport()
				|| physicalFile.isForMigration() ? true : false;
		boolean useCurrentTS = !isImportOrMigrate;		
		try {
			String[] columnNames = { COL_ENVID, COL_NAME, COL_FILETYPE, COL_DISKFILETYPE,
					COL_ACCESSMETHOD, COL_EXITID, COL_EXITPARAM,
					COL_DDNAMEINPUT, COL_DSN, COL_MINRECORDLENGTH, COL_MAXRECORDLENGTH, 
					COL_DDNAMEOUTPUT, COL_RECFM, COL_LRECL,  
                    COL_DBMSSUBSYS, COL_DBMSSQL, COL_DBMSTABLE, 
                    COL_DBMSROWFMTCD, COL_DBMSINCLNULLSIND, COL_COMMENT,
					COL_CREATETIME, COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
            if (isImportOrMigrate) {
                names.add(1, COL_ID);
            }
			String statement = generator.getInsertStatement(params.getSchema(),
					TABLE_NAME, COL_ID, names, useCurrentTS);
			PreparedStatement pst = null;
            ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);

					int i = 1;
					pst.setInt(i++, physicalFile.getEnvironmentId());
		            if (isImportOrMigrate) {
		                pst.setInt(i++, physicalFile.getPartitionId());
		            }
					pst.setString(i++, physicalFile.getPartitionName());
					pst.setString(i++, physicalFile.getFileTypeCode());
                    pst.setString(i++, physicalFile.getDiskFileTypeCode());
					pst.setString(i++, physicalFile.getAccessMethodCode());
					Integer exId = physicalFile.getReadExitId();
					if(exId == null) {
						pst.setNull(i++, Types.INTEGER);
					} else {
						pst.setInt(i++, exId);
					}
					pst.setString(i++, physicalFile.getReadExitParams());
					pst.setString(i++, physicalFile.getInputDDName());
                    pst.setString(i++, physicalFile.getDatasetName());
					pst.setInt(i++, physicalFile.getMinRecordLen());
					pst.setInt(i++, physicalFile.getMaxRecordLen());
					pst.setString(i++, physicalFile.getOutputDDName());
					pst.setString(i++, physicalFile.getRecfm());
					pst.setInt(i++, physicalFile.getLrecl());
                    pst.setString(i++, physicalFile.getSubSystem());
                    pst.setString(i++, physicalFile.getSqlStatement());
                    pst.setString(i++, physicalFile.getTableName());
                    pst.setString(i++, physicalFile.getRowFormatCode());
                    pst.setInt(i++, DataUtilities.booleanToInt(physicalFile.isIncludeNullIndicators()));
                    pst.setString(i++, physicalFile.getComments());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(physicalFile.getCreateTime()));
					}
					pst.setString(i++,isImportOrMigrate ? physicalFile.getCreateBy() : safrLogin.getUserId());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(physicalFile.getModifyTime()));
					}
					pst.setString(i++, isImportOrMigrate ? physicalFile.getModifyBy() : safrLogin.getUserId());
					rs = pst.executeQuery();
                    rs.next();
                    int id = rs.getInt(1); 
                    physicalFile.setPersistent(true);
                    physicalFile.setId(id);
                    if (!isImportOrMigrate) {
                        physicalFile.setCreateBy(safrLogin.getUserId());
                        physicalFile.setCreateTime(rs.getDate(2));
                        physicalFile.setModifyBy(safrLogin.getUserId());
                        physicalFile.setModifyTime(rs.getDate(3));
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
			throw DataUtilities.createDAOException("Database error occurred while creating a new Physical File.",e);
		}
		SAFRApplication.getTimingMap().stopTiming("PGPhysicalFileDAO.createPhysicalFile");
		
		return physicalFile;

	}

	/**
	 * This function is to update a Physical File in PHYFILE in database.
	 * 
	 * @param physicalFile
	 *            : The transfer object which contains the values which are to
	 *            be set in the fields for the corresponding Physical File which
	 *            is being created.
	 * @return The transfer object which contains the values which are received
	 *         from the PHYFILE for the Physical File which is updated.
	 * @throws DAOException
	 * @throws SAFRNotFoundException
	 */
	private PhysicalFileTransfer updatePhysicalFile(
			PhysicalFileTransfer physicalFile) throws DAOException,
			SAFRNotFoundException {
	    SAFRApplication.getTimingMap().startTiming("PGPhysicalFileDAO.updatePhysicalFile");
		
		boolean isImportOrMigrate = physicalFile.isForImport()
				|| physicalFile.isForMigration() ? true : false;
		boolean useCurrentTS = !isImportOrMigrate;

		try {
            String[] columnNames = { COL_ENVID, COL_ID, COL_NAME, COL_FILETYPE, COL_DISKFILETYPE,
                COL_ACCESSMETHOD, COL_EXITID, COL_EXITPARAM,
                COL_DDNAMEINPUT, COL_DSN, COL_MINRECORDLENGTH, COL_MAXRECORDLENGTH, 
                COL_DDNAMEOUTPUT, COL_RECFM, COL_LRECL,  
                COL_DBMSSUBSYS, COL_DBMSSQL, COL_DBMSTABLE, 
                COL_DBMSROWFMTCD, COL_DBMSINCLNULLSIND, COL_COMMENT };		    
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

                    pst.setInt(i++, physicalFile.getEnvironmentId());
                    pst.setInt(i++, physicalFile.getPartitionId());
                    pst.setString(i++, physicalFile.getPartitionName());
                    pst.setString(i++, physicalFile.getFileTypeCode());
                    pst.setString(i++, physicalFile.getDiskFileTypeCode());
                    pst.setString(i++, physicalFile.getAccessMethodCode());
					Integer exId = physicalFile.getReadExitId();
					if(exId == null) {
						pst.setNull(i++, Types.INTEGER);
					} else {
						pst.setInt(i++, exId);
					}
                    pst.setString(i++, physicalFile.getReadExitParams());
                    pst.setString(i++, physicalFile.getInputDDName());
                    pst.setString(i++, physicalFile.getDatasetName());
                    pst.setInt(i++, physicalFile.getMinRecordLen());
                    pst.setInt(i++, physicalFile.getMaxRecordLen());
                    pst.setString(i++, physicalFile.getOutputDDName());
                    pst.setString(i++, physicalFile.getRecfm());
                    pst.setInt(i++, physicalFile.getLrecl());
                    pst.setString(i++, physicalFile.getSubSystem());
                    pst.setString(i++, physicalFile.getSqlStatement());
                    pst.setString(i++, physicalFile.getTableName());
                    pst.setString(i++, physicalFile.getRowFormatCode());
                    pst.setInt(i++, DataUtilities.booleanToInt(physicalFile.isIncludeNullIndicators()));
                    pst.setString(i++, physicalFile.getComments());
					if (isImportOrMigrate) {
						// createby and lastmod set from source component
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(physicalFile.getCreateTime()));
						pst.setString(i++, physicalFile.getCreateBy());
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(physicalFile.getModifyTime()));
						pst.setString(i++, physicalFile.getModifyBy());
					} else {
						// createby details are untouched
						// lastmodtimestamp is CURRENT_TIMESTAMP
						// lastmoduserid is logged in user
						pst.setString(i++, safrLogin.getUserId());
					}
					pst.setInt(i++, physicalFile.getPartitionId());
					pst.setInt(i++, physicalFile.getEnvironmentId());

                    if (useCurrentTS) {
                        ResultSet rs = pst.executeQuery();
                        rs.next();
                        physicalFile.setModifyTime(rs.getDate(1));                      
                        physicalFile.setModifyBy(safrLogin.getUserId());                      
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
			pst.close();
			
			SAFRApplication.getTimingMap().stopTiming("PGPhysicalFileDAO.updatePhysicalFile");
			
			return (physicalFile);

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while updating the Physical File.",e);
		}

	}

	public PhysicalFileTransfer getDuplicatePhysicalFile(
			String physicalFileName, Integer physicalFileId,
			Integer environmentId) throws DAOException {
		PhysicalFileTransfer result = null;
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
					pst.setString(i++, physicalFileName.toUpperCase());
					pst.setInt(i++, physicalFileId);
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
				logger.info("Existing Physical File with name '" + physicalFileName
						+ "' found in Environment [" + environmentId + "]");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving a duplicate Physical File.",e);
		}
		return result;

	}

	public void removePhysicalFile(Integer id, Integer environmentId)
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

					// deleting its association with any group and Physical File
					// itself.
					String[] relatedTables = { "LFPFASSOC", "SECPHYFILE", TABLE_NAME };

					for (String table : relatedTables) {
						String deleteAssocQuery = generator.getDeleteStatement(
								params.getSchema(), table, idNames);
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
					if (DAOFactoryHolder.getDAOFactory().getDAOUOW()
							.isMultiComponentScope()) {
						throw e;
					} else {
						continue;
					}
				} catch (SQLException e) {
					throw DataUtilities.createDAOException("Database error occurred while deleting the Physical File.",e);
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

	public List<FileAssociationTransfer> getAssociatedLogicalFiles(Integer id,
			Integer environmentId) throws DAOException {
		List<FileAssociationTransfer> result = new ArrayList<FileAssociationTransfer>();

		String selectString = "";
		try {
		    boolean admin = SAFRApplication.getUserSession().isSystemAdministrator();
			String schema = params.getSchema();

			if (admin) {
				selectString = "Select B.LOGFILEID, C.NAME, B.LFPFASSOCID "
						+ "From "
						+ schema + ".PHYFILE A, "
						+ schema + ".LFPFASSOC B, "
						+ schema + ".LOGFILE C "
						+ "Where A.ENVIRONID = ? "
						+ " AND A.PHYFILEID = ? "
						+ " AND A.ENVIRONID = B.ENVIRONID AND A.PHYFILEID = B.PHYFILEID"
						+ " AND B.ENVIRONID = C.ENVIRONID AND B.LOGFILEID = C.LOGFILEID "
						+ "Order By UPPER(C.NAME)";
			} else {
				selectString = "Select B.LOGFILEID, C.NAME, B.LFPFASSOCID, D.RIGHTS "
						+ "From "
						+ schema + ".PHYFILE A INNER JOIN "
						+ schema + ".LFPFASSOC B ON A.ENVIRONID = B.ENVIRONID AND A.PHYFILEID = B.PHYFILEID INNER JOIN "
						+ schema + ".LOGFILE C ON B.ENVIRONID = C.ENVIRONID AND B.LOGFILEID = C.LOGFILEID LEFT OUTER JOIN "
						+ schema + ".SECLOGFILE D ON C.ENVIRONID = D.ENVIRONID AND C.LOGFILEID = D.LOGFILEID "
						+ " AND D.GROUPID = ? "
						+ " Where A.ENVIRONID = ?  "
						+ " AND A.PHYFILEID = ? "
						+ " Order By UPPER(C.NAME)";
			}
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					if (admin) {
						pst.setInt(ndx++, environmentId);
						pst.setInt(ndx++, id);
					} else {
						pst.setInt(ndx++,  SAFRApplication.getUserSession().getGroup().getId());
						pst.setInt(ndx++, environmentId);
						pst.setInt(ndx++, id);
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
				FileAssociationTransfer fileAssociationTransfer = new FileAssociationTransfer();
				fileAssociationTransfer.setAssociatedComponentId(rs.getInt("LOGFILEID"));
				fileAssociationTransfer.setAssociatedComponentName(DataUtilities.trimString(rs.getString("NAME")));
				fileAssociationTransfer.setAssociationId(rs.getInt("LFPFASSOCID"));
				fileAssociationTransfer.setAssociatedComponentRights(
					admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.LogicalFile, environmentId));
				result.add(fileAssociationTransfer);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while retrieving Logical Files associated with the Physical File.",e);
		}
	}

	public List<DependentComponentTransfer> getViewDependencies(
			Integer environmentId, Integer physicalFileId) throws DAOException {
		List<DependentComponentTransfer> viewDependencies = new ArrayList<DependentComponentTransfer>();
		try {
			// Getting Views in which PF is used as output file.
			String selectDependentPFs = "Select A.VIEWID, A.NAME From "
					+ params.getSchema() + ".VIEW A,"
					+ params.getSchema() + ".LFPFASSOC B "
					+ "Where A.ENVIRONID =B.ENVIRONID "
					+ "AND A.LFPFASSOCID=B.LFPFASSOCID "
					+ "AND A.ENVIRONID =? AND B.PHYFILEID =? "
					+ "AND B.PHYFILEID>0";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectDependentPFs);
					pst.setInt(1, environmentId);
					pst.setInt(2, physicalFileId);
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
				depCompTransfer.setDependencyInfo("[View Properties]");
				viewDependencies.add(depCompTransfer);
			}

			pst.close();
			rs.close();

			// Query for getting views in which PF is used in Logic Text
			selectDependentPFs = "SELECT DISTINCT A.VIEWID, E.NAME, D.COLUMNNUMBER, D.HDRLINE1 FROM "
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
					+ " WHERE A.ENVIRONID= ? AND B.PHYFILEID= ? AND B.PHYFILEID > 0 AND " 
					+ "A.LOGICTYPECD = 2";

			pst = null;
			rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectDependentPFs);
					pst.setInt(1, environmentId);
					pst.setInt(2, physicalFileId);
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
				viewDependencies.add(depCompTransfer);
			}

			pst.close();
			rs.close();
			
            // retrieving Views where this LF is used in Extract Output Logic Text.
            selectDependentPFs = "SELECT DISTINCT A.VIEWID, D.NAME FROM " +
                params.getSchema() + ".VIEWLOGICDEPEND A, " +
                params.getSchema() + ".VIEWSOURCE B, " +
                params.getSchema() + ".LFPFASSOC C, " +
                params.getSchema() + ".PHYFILE D " +
                "WHERE A.ENVIRONID=B.ENVIRONID " +
                "AND A.PARENTID=B.VIEWSOURCEID " +
                "AND A.ENVIRONID=C.ENVIRONID " +
                "AND A.LFPFASSOCID=C.LFPFASSOCID " +
                "AND C.ENVIRONID=D.ENVIRONID " +
                "AND C.PHYFILEID=D.PHYFILEID " +
                "AND A.LOGICTYPECD=5 " +
                "AND D.ENVIRONID=? " +
                "AND D.PHYFILEID=?";
            while (true) {
                try {
                    pst = con.prepareStatement(selectDependentPFs);
                    pst.setInt(1, environmentId);
                    pst.setInt(2, physicalFileId);
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
			

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving View dependencies of Physical File.",e);
		}
		return viewDependencies;
	}

	public List<DependentComponentTransfer> getAssociatedLogicalFilesWithOneAssociatedPF(
			Integer environmentId, Integer physicalFileId) throws DAOException {
		List<DependentComponentTransfer> dependencies = new ArrayList<DependentComponentTransfer>();
		try {

			String query = "SELECT A.PHYFILEID, A.LOGFILEID, C.NAME FROM "
					+ params.getSchema() + ".LOGFILE C, "
					+ params.getSchema() + ".LFPFASSOC A  "
					+ "INNER JOIN (SELECT ENVIRONID,LOGFILEID FROM "
					+ params.getSchema() + ".LFPFASSOC "
					+ "GROUP BY LOGFILEID,ENVIRONID HAVING COUNT(LOGFILEID) = 1) B "
					+ "ON B.LOGFILEID = A.LOGFILEID AND B.ENVIRONID=A.ENVIRONID "
					+ "WHERE A.PHYFILEID=? AND A.ENVIRONID=C.ENVIRONID "
					+ "AND A.ENVIRONID =? AND C.LOGFILEID = A.LOGFILEID";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(query);

					pst.setInt(1, physicalFileId);
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
				depCompTransfer.setId(rs.getInt("LOGFILEID"));
				depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
				dependencies.add(depCompTransfer);
			}

			pst.close();
			rs.close();

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving Logical File dependencies of Physical File.",e);
		}

		return dependencies;
	}
}
