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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.LogicalRecordDAO;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.LRIndexFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.internal.data.PGSQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LRIndexQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;

/**
 *This class is used to implement the unimplemented methods of
 * <b>LogicalRecordDAO</b>. This class contains the methods to related to
 * Logical Record metadata component which require database access.
 * 
 */
public class PGLogicalRecordDAO implements LogicalRecordDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGLogicalRecordDAO");

	private static final String TABLE_NAME = "LOGREC";
	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "LOGRECID";
	private static final String COL_NAME = "NAME";
	private static final String COL_TYPE = "LRTYPECD";
	private static final String COL_STATUS = "LRSTATUSCD";
	private static final String COL_EXIT = "LOOKUPEXITID";
	private static final String COL_STARTUP = "LOOKUPEXITSTARTUP";
	private static final String COL_COMMENTS = "COMMENTS";
	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";
    private static final String COL_ACTIVATETIME = "LASTACTTIMESTAMP";
    private static final String COL_ACTIVATEBY = "LASTACTUSERID";

	// Constants for LR indexes
	private static final String TABLE_LRINDEX = "LRINDEX";
	private static final String COL_INDEXID = "LRINDEXID";
	private static final String COL_EFFSTARTDATEID = "EFFDATESTARTFLDID";
	private static final String COL_EFFENDDATEID = "EFFDATEENDFLDID";
	
	private static final String TABLE_LRINDEXFLD = "LRINDEXFLD";
	private static final String COL_INDEXFLDID = "LRINDEXFLDID";
	private static final String COL_SEQNO = "FLDSEQNBR";
	private static final String COL_LRFLDID = "LRFIELDID";

	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private PGSQLGenerator generator = new PGSQLGenerator();
	
	private String[] indexNames = { 
			COL_ENVID,  
			COL_ID, 
			COL_EFFSTARTDATEID, 
			COL_EFFENDDATEID,
			COL_CREATETIME, 
			COL_CREATEBY, 
			COL_MODIFYTIME, 
			COL_MODIFYBY };

	
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
	public PGLogicalRecordDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	public LogicalRecordTransfer getLogicalRecord(Integer id,
			Integer environmentId) throws DAOException {
		LogicalRecordTransfer result = null;
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			idNames.add(COL_ENVID);

			String selectString = generator.getSelectStatement(params.getSchema(), TABLE_NAME, idNames, null);
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
				logger.info("No such Logical Record in Env " + environmentId + " with id : " + id);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
				"Database error occurred while retrieving the Logical Record with id ["+ id + "]", e);
		}
		return result;
	}

	public LogicalRecordTransfer getLogicalRecord(String name,	Integer environmentId) throws DAOException {
		LogicalRecordTransfer result = null;
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_NAME);
			idNames.add(COL_ENVID);

			String selectString = generator.getSelectStatement(params.getSchema(), TABLE_NAME, idNames, null);
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
				logger.info("No such Logical Record in Env " + environmentId + " with id : " + name);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
				"Database error occurred while retrieving the Logical Record with id ["+ name + "]", e);
		}
		return result;
	}

	public List<LogicalRecordQueryBean> queryAllLogicalRecords(
			Integer environmentId, SortType sortType) throws DAOException {
		List<LogicalRecordQueryBean> result = new ArrayList<LogicalRecordQueryBean>();

		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "A.LOGRECID";
		} else {
			orderString = "A.LOGRECID";
		}

		try {
			String selectString = "";
			if (SAFRApplication.getUserSession().isSystemAdministrator()) {
                selectString = "SELECT A.LOGRECID, A.NAME, A.LRSTATUSCD, MAX(B.FIXEDSTARTPOS+C.MAXLEN)-1 AS TOTLEN, A.LRTYPECD, "
                    + "A.CREATEDTIMESTAMP, A.CREATEDUSERID, A.LASTMODTIMESTAMP, A.LASTMODUSERID, A.LASTACTTIMESTAMP, A.LASTACTUSERID "
                    + "FROM " + params.getSchema() + ".LOGREC A "
                    + "LEFT OUTER JOIN " + params.getSchema() + ".LRFIELD B "
                    + "ON A.ENVIRONID=B.ENVIRONID AND A.LOGRECID=B.LOGRECID "
                    + "LEFT OUTER JOIN " + params.getSchema() + ".LRFIELDATTR C "
                    + "ON B.ENVIRONID=C.ENVIRONID AND B.LRFIELDID=C.LRFIELDID "
                    + "WHERE A.LOGRECID > 0 AND A.ENVIRONID = ? AND (B.REDEFINE=0 OR B.REDEFINE IS NULL) "
                    + "GROUP BY A.ENVIRONID, A.LOGRECID, A.NAME, A.LRSTATUSCD, A.LRTYPECD, A.CREATEDTIMESTAMP,"
                    + "A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.LASTACTTIMESTAMP,A.LASTACTUSERID "                     
                    + "ORDER BY " + orderString;
				
			} else {
				selectString = "SELECT A.LOGRECID, A.NAME, A.LRSTATUSCD, MAX(B.FIXEDSTARTPOS+C.MAXLEN)-1 AS TOTLEN, A.LRTYPECD, L.RIGHTS, "
						+ "A.CREATEDTIMESTAMP,A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.LASTACTTIMESTAMP,A.LASTACTUSERID "
						+ "FROM " + params.getSchema() + ".LOGREC A "
                        + "LEFT OUTER JOIN " + params.getSchema() + ".LRFIELD B "
                        + "ON A.ENVIRONID=B.ENVIRONID AND A.LOGRECID=B.LOGRECID "
                        + "LEFT OUTER JOIN " + params.getSchema() + ".LRFIELDATTR C "
                        + "ON B.ENVIRONID=C.ENVIRONID AND B.LRFIELDID=C.LRFIELDID "
						+ "LEFT OUTER JOIN " + params.getSchema()+ ".SECLOGREC L "
						+ "ON A.ENVIRONID = L.ENVIRONID AND A.LOGRECID = L.LOGRECID "
						+ " AND L.GROUPID=" + SAFRApplication.getUserSession().getGroup().getId() + " "
						+ "WHERE A.LOGRECID > 0 AND A.ENVIRONID = ? AND (B.REDEFINE=0 OR B.REDEFINE IS NULL) "
                        + "GROUP BY A.ENVIRONID, A.LOGRECID, A.NAME, A.LRSTATUSCD, A.LRTYPECD, L.RIGHTS, A.CREATEDTIMESTAMP,"
                        + "A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.LASTACTTIMESTAMP,A.LASTACTUSERID "                     
						+ "ORDER BY " + orderString;
			}
			result = generateQueryBeansList(selectString, environmentId, 
			    SAFRApplication.getUserSession().isSystemAdministrator());
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while querying all Logical Records.",e);
		}
	}

	public List<LogicalRecordQueryBean> queryAllActiveLogicalRecords(
			Integer environmentId, SortType sortType) throws DAOException {
		List<LogicalRecordQueryBean> result = new ArrayList<LogicalRecordQueryBean>();

		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "A.LOGRECID";
		} else {
			orderString = "A.LOGRECID";
		}

		String selectString;
		try {
			if (SAFRApplication.getUserSession().isSystemAdministrator()) {
				selectString = "SELECT A.LOGRECID, A.NAME, A.LRSTATUSCD, A.LRTYPECD, "
						+ "A.CREATEDTIMESTAMP,A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.LASTACTTIMESTAMP,A.LASTACTUSERID FROM "
						+ params.getSchema()
						+ ".LOGREC A "
						+ "WHERE A.LOGRECID > 0 AND A.LRSTATUSCD = 'ACTVE' AND A.ENVIRONID = ? "
						+ " ORDER BY " + orderString;
			} else {

				selectString = "SELECT A.LOGRECID, A.NAME, A.LRSTATUSCD, A.LRTYPECD, L.RIGHTS, "
						+ "A.CREATEDTIMESTAMP,A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.LASTACTTIMESTAMP,A.LASTACTUSERID FROM "
						+ params.getSchema()
						+ ".LOGREC A "
						+ "LEFT OUTER JOIN "
						+ params.getSchema()
						+ ".SECLOGREC L "
						+ "ON A.ENVIRONID = L.ENVIRONID AND A.LOGRECID = L.LOGRECID "
						+ " AND L.GROUPID="
						+ SAFRApplication.getUserSession().getGroup().getId()
						+ " WHERE A.LOGRECID > 0 AND LRSTATUSCD = 'ACTVE' AND A.ENVIRONID = ? "
						+ " ORDER BY " + orderString;
			}

			result = generateActiveQueryBeansList(selectString, environmentId, 
			    SAFRApplication.getUserSession().isSystemAdministrator());
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying all Active Logical Records.",e);
		}
	}

	/**
	 * This method is to generate a list of Query Bean objects.
	 * 
	 * @param selectString
	 *            : The Query which is to be run.
	 * @param environmentId
	 *            : The ID of the environment.
	 * @return A List of LogicalRecordQueryBean objects.
	 * @throws SQLException
	 * @throws DAOException
	 */
	private List<LogicalRecordQueryBean> generateQueryBeansList(
			String selectString, Integer environmentId, boolean isAdmin)
			throws SQLException, DAOException {	    	    
		List<LogicalRecordQueryBean> result = new ArrayList<LogicalRecordQueryBean>();

        // generate key length map
        Map<Integer, Integer> keyLenMap = new HashMap<Integer, Integer>();
        String sql2 = "SELECT A.LOGRECID, SUM(C.MAXLEN) AS KEYLEN FROM "
            + params.getSchema() + ".LRINDEX A "
            + "LEFT OUTER JOIN " + params.getSchema() + ".LRINDEXFLD B ON "
            + "A.ENVIRONID=B.ENVIRONID AND A.LRINDEXID=B.LRINDEXID "            
            + "LEFT OUTER JOIN " + params.getSchema() + ".LRFIELDATTR C ON "
            + "B.ENVIRONID=C.ENVIRONID AND B.LRFIELDID=C.LRFIELDID "  
            + "WHERE A.ENVIRONID= ? "
            + "GROUP BY A.LOGRECID";
        PreparedStatement pst2 = null;
        ResultSet rs2 = null;
        while (true) {
            try {
                pst2 = con.prepareStatement(sql2);
                pst2.setInt(1,  environmentId);
                rs2 = pst2.executeQuery();
                while (rs2.next()) {
                    int id = rs2.getInt(COL_ID);
                    int keyLen = rs2.getInt("KEYLEN");
                    keyLenMap.put(id, keyLen);
                }
                rs2.close();
                pst2.close();                
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
            
		// get the rest
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
		    int lrid = rs.getInt(COL_ID);
            int keyLen = 0;
            if (keyLenMap.containsKey(lrid)) {
                keyLen = keyLenMap.get(lrid);
            }
			LogicalRecordQueryBean logicalRecordQueryBean = new LogicalRecordQueryBean(
				environmentId, lrid, 
				DataUtilities.trimString(rs.getString(COL_NAME)), 
				DataUtilities.trimString(rs.getString(COL_STATUS)),
				rs.getInt("TOTLEN"), keyLen,
                DataUtilities.trimString(rs.getString(COL_TYPE)),
				isAdmin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                    rs.getInt("RIGHTS"), ComponentType.LogicalRecord, environmentId), 
				rs.getDate(COL_CREATETIME), 
				DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
                rs.getDate(COL_MODIFYTIME), 
                DataUtilities.trimString(rs.getString(COL_MODIFYBY)),
				rs.getDate(COL_ACTIVATETIME), 
				DataUtilities.trimString(rs.getString(COL_ACTIVATEBY)));
			result.add(logicalRecordQueryBean);
		}
		pst.close();
		rs.close();
		return result;

	}

    /**
     * This method is to generate a list of Query Bean objects.
     * 
     * @param selectString
     *            : The Query which is to be run.
     * @param environmentId
     *            : The ID of the environment.
     * @return A List of LogicalRecordQueryBean objects.
     * @throws SQLException
     * @throws DAOException
     */
    private List<LogicalRecordQueryBean> generateActiveQueryBeansList(
            String selectString, Integer environmentId, boolean isAdmin)
            throws SQLException, DAOException {
        List<LogicalRecordQueryBean> result = new ArrayList<LogicalRecordQueryBean>();

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
            LogicalRecordQueryBean logicalRecordQueryBean = new LogicalRecordQueryBean(
                environmentId, rs.getInt(COL_ID), 
                DataUtilities.trimString(rs.getString(COL_NAME)), 
                DataUtilities.trimString(rs.getString(COL_STATUS)),
                null, null, 
                DataUtilities.trimString(rs.getString(COL_TYPE)),                
                isAdmin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                    rs.getInt("RIGHTS"), ComponentType.LogicalRecord, environmentId), 
                rs.getDate(COL_CREATETIME), 
                DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
                rs.getDate(COL_MODIFYTIME), 
                DataUtilities.trimString(rs.getString(COL_MODIFYBY)),
                rs.getDate(COL_ACTIVATETIME), 
                DataUtilities.trimString(rs.getString(COL_ACTIVATEBY)));
            result.add(logicalRecordQueryBean);
        }
        pst.close();
        rs.close();
        return result;

    }
	
	/*
	 * This function is used to generate a transfer object for the Logical
	 * Record.
	 */
	private LogicalRecordTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		LogicalRecordTransfer trans = new LogicalRecordTransfer();
		trans.setEnvironmentId(rs.getInt(COL_ENVID));
		trans.setId(rs.getInt(COL_ID));
		trans.setName(DataUtilities.trimString(rs.getString(COL_NAME)));
		trans.setLrTypeCode(DataUtilities.trimString(rs.getString(COL_TYPE)));
		trans.setLrStatusCode(DataUtilities.trimString(rs.getString(COL_STATUS)));
		trans.setLookupExitId(rs.getInt(COL_EXIT));
		if(rs.wasNull())  {
			trans.setLookupExitId(null);
		}
		trans.setLookupExitParams(DataUtilities.trimString(rs.getString(COL_STARTUP)));
		trans.setComments(DataUtilities.trimString(rs.getString(COL_COMMENTS)));
		trans.setCreateTime(rs.getDate(COL_CREATETIME));
		trans.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		trans.setModifyTime(rs.getDate(COL_MODIFYTIME));
		trans.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));
        trans.setActivatedTime(rs.getDate(COL_ACTIVATETIME));
        trans.setActivatedBy(DataUtilities.trimString(rs.getString(COL_ACTIVATEBY)));

		return trans;
	}

	public LogicalRecordTransfer persistLogicalRecord(
			LogicalRecordTransfer logicalRecordTransfer) throws DAOException,
			SAFRNotFoundException {
		if (!logicalRecordTransfer.isPersistent()) {
			return (createLogicalRecord(logicalRecordTransfer));
		} else {
            return (updateLogicalRecord(logicalRecordTransfer));		    
		}

	}

    /**
	 * This function is used to create a Logical Record in LOGREC table
	 * 
	 * @param logicalRecordTransfer
	 *            : The transfer object which contains the values which are to
	 *            be set in the columns for the corresponding Logical Record
	 *            which is being created.
	 * @return The transfer object which contains the values which are received
	 *         from the LOGREC for the Logical Record which is created.
	 * @throws DAOException
	 */
	private LogicalRecordTransfer createLogicalRecord(
			LogicalRecordTransfer logicalRecordTransfer) throws DAOException {
		
		boolean isImportOrMigrate = logicalRecordTransfer.isForImport()
				|| logicalRecordTransfer.isForMigration() ? true : false;
		boolean useCurrentTS = !isImportOrMigrate;

		try {
			String[] columnNames = { COL_ENVID, COL_NAME, COL_TYPE,
					COL_STATUS, COL_EXIT, COL_STARTUP, COL_COMMENTS,
					COL_CREATETIME, COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
            
            List<String> names = new ArrayList<String>();
            names.addAll(Arrays.asList(columnNames));
            if (isImportOrMigrate) {
                names.add(1, COL_ID);
                names.add(COL_ACTIVATETIME);
                names.add(COL_ACTIVATEBY);
            } else {
                if (logicalRecordTransfer.getLrStatusCode().equals(SAFRApplication.getSAFRFactory().
                    getCodeSet(CodeCategories.LRSTATUS).getCode(Codes.ACTIVE).getKey())) {
                    names.add(COL_ACTIVATETIME);
                    names.add(COL_ACTIVATEBY);
                }                
            }
                        
			String statement = generator.getInsertStatement(params.getSchema(),
					TABLE_NAME, COL_ID, names, useCurrentTS);
			PreparedStatement pst = null;
            ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);

					int i = 1;
					pst.setInt(i++, logicalRecordTransfer.getEnvironmentId());
		            if (isImportOrMigrate) {
		                pst.setInt(i++, logicalRecordTransfer.getId());
		            }
					pst.setString(i++, logicalRecordTransfer.getName());
					pst.setString(i++, logicalRecordTransfer.getLrTypeCode());
					pst.setString(i++, logicalRecordTransfer.getLrStatusCode());
					Integer exId = logicalRecordTransfer.getLookupExitId();
					if(exId == null || exId == 0) {
						pst.setNull(i++, Types.INTEGER);
					} else {
						pst.setInt(i++, exId);
					}
					pst.setString(i++, logicalRecordTransfer.getLookupExitParams());
					pst.setString(i++, logicalRecordTransfer.getComments());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(logicalRecordTransfer.getCreateTime()));
					}
					pst.setString(i++,isImportOrMigrate ? logicalRecordTransfer.getCreateBy() : safrLogin.getUserId());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(logicalRecordTransfer.getModifyTime()));
					}
					pst.setString(i++,isImportOrMigrate ? logicalRecordTransfer.getModifyBy() : safrLogin.getUserId());
                    if (isImportOrMigrate) {
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(logicalRecordTransfer.getActivatedTime()));
                        pst.setString(i++, logicalRecordTransfer.getActivatedBy());
                    }
                    else  {
                        if (logicalRecordTransfer.getLrStatusCode().equals(SAFRApplication.getSAFRFactory().
                            getCodeSet(CodeCategories.LRSTATUS).getCode(Codes.ACTIVE).getKey())) {
                            pst.setString(i++, safrLogin.getUserId());
                        }
					}
                    rs = pst.executeQuery();
                    rs.next();
                    int id = rs.getInt(1);          
                    logicalRecordTransfer.setId(id);
                    logicalRecordTransfer.setPersistent(true);
                    if (!isImportOrMigrate) {
                        logicalRecordTransfer.setCreateBy(safrLogin.getUserId());
                        logicalRecordTransfer.setCreateTime(rs.getDate(2));
                        logicalRecordTransfer.setModifyBy(safrLogin.getUserId());
                        logicalRecordTransfer.setModifyTime(rs.getDate(3));   
                        if (logicalRecordTransfer.getLrStatusCode().equals(SAFRApplication.getSAFRFactory().
                            getCodeSet(CodeCategories.LRSTATUS).getCode(Codes.ACTIVE).getKey())) {
                            logicalRecordTransfer.setActivatedBy(safrLogin.getUserId());
                            logicalRecordTransfer.setActivatedTime(rs.getDate(4));                               
                        }                        
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
			throw DataUtilities.createDAOException("Database error occurred while creating a new Logical Record.",e);
		}

		return logicalRecordTransfer;
	}

	private void updateLRIndexInLR(LRIndexTransfer lrIndexTransfer) {
        
        try {	    
            // to update the PKindexid in LOGREC after creating indexes
            String updateLRTable = "Update " + params.getSchema() 
                 + ".LOGREC" + " Set PKINDEXID = " + lrIndexTransfer.getId() 
                 + " Where LRID = " + lrIndexTransfer.getLrId() 
                 + " AND ENVIRONID = " + lrIndexTransfer.getEnvironmentId();
    
            PreparedStatement pst1 = null;
    
            while (true) {
                try {
                    pst1 = con.prepareStatement(updateLRTable);
                    pst1.executeUpdate();
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
            pst1.close();
            
        } catch (SQLException e) {
            throw DataUtilities.createDAOException("Database error occurred while updating Logical Record Index.",e);
        }
	}
	
	/**
	 * This function is used to update a Logical Record in LOGREC table
	 * 
	 * @param logicalRecordTransfer
	 *            : The transfer object which contains the values which are to
	 *            be set in the columns for the corresponding Logical Record
	 *            which is being updated.
	 * @return The transfer object which contains the values which are received
	 *         from the LOGREC for the Logical Record which is updated.
	 * @throws DAOException
	 * @throws SAFRNotFoundException
	 */
	private LogicalRecordTransfer updateLogicalRecord(
			LogicalRecordTransfer logicalRecordTransfer) throws DAOException,
			SAFRNotFoundException {
		
		boolean isImportOrMigrate = logicalRecordTransfer.isForImport()
				|| logicalRecordTransfer.isForMigration() ? true : false;
		boolean useCurrentTS = !isImportOrMigrate;

		try {
			String[] columnNames = { COL_NAME, COL_TYPE, COL_STATUS, COL_EXIT,
					COL_STARTUP, COL_COMMENTS };
			List<String> temp = Arrays.asList(columnNames);
			List<String> names = new ArrayList<String>();
			names.addAll(temp);

			if (isImportOrMigrate) {
				names.add(COL_CREATETIME);
				names.add(COL_CREATEBY);
				names.add(COL_MODIFYTIME);
				names.add(COL_MODIFYBY);
                names.add(COL_ACTIVATETIME);
                names.add(COL_ACTIVATEBY);
			} else { 
			    if (logicalRecordTransfer.isUpdated()) {
	                names.add(COL_MODIFYTIME);
	                names.add(COL_MODIFYBY);
			    }
	            if (logicalRecordTransfer.isActivated()) {
	                names.add(COL_ACTIVATETIME);
	                names.add(COL_ACTIVATEBY);
	            }
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
					pst.setString(i++, logicalRecordTransfer.getName());
					pst.setString(i++, logicalRecordTransfer.getLrTypeCode());
					pst.setString(i++, logicalRecordTransfer.getLrStatusCode());
					Integer exId = logicalRecordTransfer.getLookupExitId();
					if(exId == null || exId == 0) {
						pst.setNull(i++, Types.INTEGER);
					} else {
						pst.setInt(i++, exId);
					}
					pst.setString(i++, logicalRecordTransfer.getLookupExitParams());
					pst.setString(i++, logicalRecordTransfer.getComments());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(logicalRecordTransfer.getCreateTime()));
						pst.setString(i++, logicalRecordTransfer.getCreateBy());
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(logicalRecordTransfer.getModifyTime()));
						pst.setString(i++, logicalRecordTransfer.getModifyBy());
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(logicalRecordTransfer.getActivatedTime()));
                        pst.setString(i++, logicalRecordTransfer.getActivatedBy());
					} else {
					    if (logicalRecordTransfer.isUpdated()){
	                        pst.setString(i++, safrLogin.getUserId());
					    }
                        if (logicalRecordTransfer.isActivated()) {
                            pst.setString(i++, safrLogin.getUserId());                            
                        }
					}

					pst.setInt(i++, logicalRecordTransfer.getId());
					pst.setInt(i++, logicalRecordTransfer.getEnvironmentId());
                    if ( useCurrentTS && 
                        (logicalRecordTransfer.isUpdated() || logicalRecordTransfer.isActivated())) {
                        ResultSet rs = pst.executeQuery();
                        rs.next();
                        int j=1;
                        if (logicalRecordTransfer.isUpdated()) {
                            logicalRecordTransfer.setModifyBy(safrLogin.getUserId());
                            logicalRecordTransfer.setModifyTime(rs.getDate(j++));
                            logicalRecordTransfer.setUpdated(false);
                        }
                        if (logicalRecordTransfer.isActivated()) {
                            logicalRecordTransfer.setActivatedBy(safrLogin.getUserId());
                            logicalRecordTransfer.setActivatedTime(rs.getDate(j++));
                            logicalRecordTransfer.setActivated(false);
                        }                        
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
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while updating the Logical Record.",e);
		}

		return logicalRecordTransfer;
	}

	public void removeLogicalRecord(Integer id, Integer environmentId)
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

					String schema = params.getSchema();

					// Deleting indexes...
					String statement = "DELETE FROM "
							+ schema + ".LRINDEXFLD A WHERE LRINDEXID IN (SELECT LRINDEXID FROM "
							+ schema + ".LRINDEX B WHERE LOGRECID = ? AND B.ENVIRONID = A.ENVIRONID) "
							+ "AND A.ENVIRONID = ?";
					PreparedStatement pst = null;

					while (true) {
						try {
							pst = con.prepareStatement(statement);
							pst.setInt(1, id);
							pst.setInt(2, environmentId);
							pst.execute();
							pst.close();
							break;
						} catch (SQLException se) {
							if (con.isClosed()) {
								// lost database connection, so reconnect and
								// retry
								con = DAOFactoryHolder.getDAOFactory().reconnect();
							} else {
								throw se;
							}
						}
					}
					// Deleting field attributes
					statement = "DELETE FROM "
							+ schema + ".LRFIELDATTR A WHERE LRFIELDID IN (SELECT LRFIELDID FROM "
							+ schema + ".LRFIELD B WHERE LOGRECID = ? AND B.ENVIRONID =A.ENVIRONID) "
							+ " AND A.ENVIRONID = ?";

					while (true) {
						try {
							pst = con.prepareStatement(statement);
							pst.setInt(1, id);
							pst.setInt(2, environmentId);
							pst.execute();
							pst.close();
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
					// delete from other related tables.
					String[] relatedTables = { "LRINDEX", "LRLFASSOC",
							"SECLOGREC", "LRFIELD", TABLE_NAME };
					while (true) {
						try {

							for (String table : relatedTables) {
								statement = generator.getDeleteStatement(
										params.getSchema(), table, idNames);
								pst = con.prepareStatement(statement);
								pst.setInt(1, id);
								pst.setInt(2, environmentId);
								pst.execute();

							}
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
					throw DataUtilities.createDAOException("Database error occurred while deleting the Logical Record.",e);
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

	public LogicalRecordTransfer getDuplicateLogicalRecord(
			String logicalRecordName, Integer logicalRecordId,
			Integer environmentId) throws DAOException {
		LogicalRecordTransfer result = null;
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
					pst.setString(i++, logicalRecordName.toUpperCase());
					pst.setInt(i++, logicalRecordId);
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
				logger.info("Existing Logical Record with name '" + logicalRecordName
						+ "' found in Environment [" + environmentId + "]");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving a duplicate Logical Record.",e);
		}
		return result;
	}

	public List<ComponentAssociationTransfer> getAssociatedLogicalFiles(
			Integer id, Integer environmentId) throws DAOException {
		List<ComponentAssociationTransfer> result = new ArrayList<ComponentAssociationTransfer>();

		try {
            boolean admin = SAFRApplication.getUserSession().isSystemAdministrator(); 
		    
			String schema = params.getSchema();
			String selectString = null;

			if (admin) {
				selectString = "Select A.NAME AS RECNAME, A.LOGRECID, B.LOGFILEID, C.NAME AS FILENAME, B.LRLFASSOCID, "
                        + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "                                                               				    
						+ "From "
						+ schema
						+ ".LOGREC A, "
						+ schema
						+ ".LRLFASSOC B, "
						+ schema
						+ ".LOGFILE C "
						+ "Where A.ENVIRONID = ?"
						+ " AND A.LOGRECID = ?"
						+ " AND A.ENVIRONID = B.ENVIRONID AND A.LOGRECID = B.LOGRECID"
						+ " AND B.ENVIRONID = C.ENVIRONID AND B.LOGFILEID = C.LOGFILEID "
						+ "Order By UPPER(C.NAME)";
			} else {
				selectString = "Select A.NAME AS RECNAME, A.LOGRECID, B.LOGFILEID, C.NAME AS FILENAME, B.LRLFASSOCID, D.RIGHTS, "
                        + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "
						+ "From "
						+ schema
						+ ".LOGREC A INNER JOIN "
						+ schema
						+ ".LRLFASSOC B ON A.ENVIRONID = B.ENVIRONID AND A.LOGRECID = B.LOGRECID INNER JOIN "
						+ schema
						+ ".LOGFILE C ON B.ENVIRONID = C.ENVIRONID AND B.LOGFILEID = C.LOGFILEID LEFT OUTER JOIN "
						+ schema
						+ ".SECLOGFILE D ON C.ENVIRONID = D.ENVIRONID AND C.LOGFILEID = D.LOGFILEID "
						+ " AND D.GROUPID = ?"
						+ " Where A.ENVIRONID = ?"
						+ " AND A.LOGRECID = ? Order By UPPER(C.NAME)";
			}
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					if (admin) {
						pst.setInt(1, environmentId );
						pst.setInt(2,  id);
					} else {
						pst.setInt(1, SAFRApplication.getUserSession().getGroup().getId() );
						pst.setInt(2, environmentId );
						pst.setInt(3,  id);						
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
				componentAssociationTransfer.setEnvironmentId(environmentId);
				componentAssociationTransfer.setAssociatingComponentId(rs.getInt("LOGRECID"));
				componentAssociationTransfer.setAssociatingComponentName(
				    DataUtilities.trimString(rs.getString("RECNAME")));
				componentAssociationTransfer.setAssociatedComponentId(rs.getInt("LOGFILEID"));
				componentAssociationTransfer.setAssociatedComponentName(
				    DataUtilities.trimString(rs.getString("FILENAME")));
				componentAssociationTransfer.setAssociationId(rs.getInt("LRLFASSOCID"));
				componentAssociationTransfer.setAssociatedComponentRights(
	                admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
	                    rs.getInt("RIGHTS"), ComponentType.LogicalFile, environmentId));
				componentAssociationTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
				componentAssociationTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
				componentAssociationTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
				componentAssociationTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));             
				
				result.add(componentAssociationTransfer);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while retrieving the Logical Files associated with the Logical Record.",e);
		}
	}

	public LRIndexTransfer getLRIndex(Integer lrId, Integer environmentId)
			throws DAOException {
		LRIndexTransfer result = null;
		try {
			String schema = params.getSchema();

			String selectString = "Select ENVIRONID, LRINDEXID, LOGRECID,"
					+ " CREATEDTIMESTAMP, CREATEDUSERID, LASTMODTIMESTAMP, LASTMODUSERID"
					+ " From " + schema + ".LRINDEX"  
					+ " Where ENVIRONID = ?" 
					+ " AND LOGRECID = ?"
					+ " ORDER BY LRINDEXID DESC";
			
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1,  environmentId);
					pst.setInt(2,  lrId);
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
				result = new LRIndexTransfer();
				result.setEnvironmentId(rs.getInt("ENVIRONID"));
				result.setId(rs.getInt("LRINDEXID"));
				result.setLrId(rs.getInt("LOGRECID"));
				result.setCreateTime(rs.getTimestamp("CREATEDTIMESTAMP"));
				result.setCreateBy(rs.getString("CREATEDUSERID"));
				result.setModifyTime(rs.getTimestamp("LASTMODTIMESTAMP"));
				result.setModifyBy(rs.getString("LASTMODUSERID"));
			} 
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while retrieving the LR Index of the Logical Record.",e);
		}
		return result;
	}

	// TODO  cannot find where getLRIndexes is being used. Code probably can be removed.
	public List<LRIndexTransfer> getLRIndexes(List<Integer> lrIndexIds,
			Integer environmentId) throws DAOException {
		String ids = null;
		List<LRIndexTransfer> result = new ArrayList<LRIndexTransfer>();

		try {
			String schema = params.getSchema();
			
			String placeholders = generator.getPlaceholders(lrIndexIds.size());
			String selectString = "Select ENVIRONID, LRINDEXID, LOGRECID "
					+ "From " + schema + ".LRINDEX "
					+ "Where ENVIRONID = ? ";
					if(lrIndexIds.size() > 0) {
						selectString +=  " AND LRINDEXID IN (" + placeholders + ") ";  
					}
					selectString +=   "ORDER BY LRINDEXID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					pst.setInt(ndx++,  environmentId);
					for(int i=0; i<lrIndexIds.size();  i++) {
						pst.setInt(ndx++, lrIndexIds.get(i));
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
				LRIndexTransfer trans = new LRIndexTransfer();
				trans.setEnvironmentId(rs.getInt("ENVIRONID"));
				trans.setId(rs.getInt("LRINDEXID"));
				trans.setLrId(rs.getInt("LOGRECID"));
				result.add(trans);
			}
			if (result.size() == 0) {
				logger.info("LRIndexes (" + ids + ") were not found in the database.");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			String msg = "Database error occurred while retrieving LR Indexes ("+ ids + ").";
			throw DataUtilities.createDAOException(msg, e);
		}
		return result;
	}
	
	public List<LRIndexFieldTransfer> getLRIndexFields(Integer lrIndexId,
			Integer environid) throws DAOException {
		List<LRIndexFieldTransfer> result = new ArrayList<LRIndexFieldTransfer>();
		try {
			String schema = params.getSchema();
			String selectString = "Select ENVIRONID, LRINDEXFLDID, LRINDEXID, LRFIELDID, "
					+ "CREATEDTIMESTAMP, CREATEDUSERID, LASTMODTIMESTAMP, LASTMODUSERID "
					+ "From " + schema + ".LRINDEXFLD "
					+ "Where ENVIRONID = ?"
					+ " AND LRINDEXID = ? ORDER BY LRINDEXFLDID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1,  environid);
					pst.setInt(2,  lrIndexId);
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
				LRIndexFieldTransfer trans = new LRIndexFieldTransfer();
				trans.setEnvironmentId(rs.getInt("ENVIRONID"));
				trans.setAssociationId(rs.getInt("LRINDEXFLDID"));
				trans.setAssociatingComponentId(rs.getInt("LRINDEXID"));
				trans.setAssociatedComponentId(rs.getInt("LRFIELDID"));
				trans.setCreateTime(rs.getTimestamp("CREATEDTIMESTAMP"));
				trans.setCreateBy(rs.getString("CREATEDUSERID"));
				trans.setModifyTime(rs.getTimestamp("LASTMODTIMESTAMP"));
				trans.setModifyBy(rs.getString("LASTMODUSERID"));
				result.add(trans);
			}
			if (result.size() == 0) {
				logger.info("No LR Index Fields exist for LR Index ID ["
						+ lrIndexId + "], Environment ID [" + environid + "].");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			String msg = "Database error occurred while retrieving LR Index Fields "
					+ "for LR Index ID [" + lrIndexId + "], Environment ID [" + environid + "].";
			throw DataUtilities.createDAOException(msg, e);
		}
		return result;	
	}

	public LRIndexTransfer persistLRIndex(LRIndexTransfer lrIndexTransfer)
			throws DAOException {
	    LRIndexTransfer trans = null;
		if (!lrIndexTransfer.isPersistent()) {
			trans = (createLRIndex(lrIndexTransfer));
		} else {
			trans = (updateLRIndex(lrIndexTransfer));
		}
		//updateLRIndexInLR(lrIndexTransfer);
		return trans;
	}

	private LRIndexTransfer createLRIndex(LRIndexTransfer lrIndexTransfer)
			throws DAOException {
		try {
			List<String> names = new ArrayList<String>(Arrays.asList(indexNames));
            if (lrIndexTransfer.isForImportOrMigration()) {
                names.add(1, COL_INDEXID);
            }

			String statement = generator.getInsertStatement(params.getSchema(),
					TABLE_LRINDEX, COL_INDEXID, names, !lrIndexTransfer.isForImportOrMigration());

			PreparedStatement pst = null;
            ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int i = 1;
					pst.setInt(i++, lrIndexTransfer.getEnvironmentId());
		            if (lrIndexTransfer.isForImportOrMigration()) {
		                pst.setInt(i++, lrIndexTransfer.getId());
		            }
					pst.setInt(i++, lrIndexTransfer.getLrId());
			//		pst.setInt(i++, lrIndexTransfer.getEffectiveStartDateLRFieldId());
			//		pst.setInt(i++, lrIndexTransfer.getEffectiveEndDateLRFieldId());
				
					if(lrIndexTransfer.getEffectiveStartDateLRFieldId() == null || lrIndexTransfer.getEffectiveStartDateLRFieldId() == 0) {
						pst.setNull(i++, Types.INTEGER);						
					} else {
						pst.setInt(i++, lrIndexTransfer.getEffectiveStartDateLRFieldId());
					}
					if(lrIndexTransfer.getEffectiveEndDateLRFieldId() == null || lrIndexTransfer.getEffectiveEndDateLRFieldId() == 0) {
						pst.setNull(i++, Types.INTEGER);						
					} else {
						pst.setInt(i++, lrIndexTransfer.getEffectiveEndDateLRFieldId());
					}
					
					if (lrIndexTransfer.isForImportOrMigration()) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lrIndexTransfer.getCreateTime()));
					}
					pst.setString(i++,lrIndexTransfer.isForImportOrMigration() ? 
					    lrIndexTransfer.getCreateBy() : safrLogin.getUserId());
					
					if (lrIndexTransfer.isForImportOrMigration()) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lrIndexTransfer.getModifyTime()));
					}
					pst.setString(i++,lrIndexTransfer.isForImportOrMigration() ?
					    lrIndexTransfer.getModifyBy() : safrLogin.getUserId());
					
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
            rs.next();
            int id = rs.getInt(1);          
            rs.close();
            lrIndexTransfer.setId(id);			
			pst.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while creating LR index for the Logical Record.",e);
		}
		return lrIndexTransfer;
	}

	private LRIndexTransfer updateLRIndex(LRIndexTransfer lrIndexTransfer)
			throws DAOException {
		try {
			boolean isForImport = lrIndexTransfer.isForImport();

			List<String> names = new ArrayList<String>();
			names.add(COL_EFFSTARTDATEID);
			names.add(COL_EFFENDDATEID);
			if (isForImport) {
				names.add(COL_CREATETIME);
				names.add(COL_CREATEBY);
				names.add(COL_MODIFYTIME);
				names.add(COL_MODIFYBY);
			} else {
				names.add(COL_MODIFYTIME);
				names.add(COL_MODIFYBY);
			}

			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ENVID);
			idNames.add(COL_INDEXID);

			String statement = generator.getUpdateStatement(params.getSchema(),
					TABLE_LRINDEX, names, idNames, !isForImport);
			PreparedStatement pst = null;

			while (true) {
				try {

					pst = con.prepareStatement(statement);

					int i = 1;
//					pst.setInt(i++, lrIndexTransfer.getEffectiveStartDateLRFieldId());
//					pst.setInt(i++, lrIndexTransfer.getEffectiveEndDateLRFieldId());
					if(lrIndexTransfer.getEffectiveStartDateLRFieldId() == null || lrIndexTransfer.getEffectiveStartDateLRFieldId() == 0) {
						pst.setNull(i++, Types.INTEGER);						
					} else {
						pst.setInt(i++, lrIndexTransfer.getEffectiveStartDateLRFieldId());
					}
					if(lrIndexTransfer.getEffectiveEndDateLRFieldId() == null || lrIndexTransfer.getEffectiveEndDateLRFieldId() == 0) {
						pst.setNull(i++, Types.INTEGER);						
					} else {
						pst.setInt(i++, lrIndexTransfer.getEffectiveEndDateLRFieldId());
					}
			
					if (isForImport) {
						// createby and lastmod set from import data
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lrIndexTransfer.getCreateTime()));
						pst.setString(i++, lrIndexTransfer.getCreateBy());
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lrIndexTransfer.getModifyTime()));
						pst.setString(i++, lrIndexTransfer.getModifyBy());
					} else {
						// createby details are untouched
						// lastmodtimestamp is CURRENT_TIMESTAMP
						// lastmoduserid is logged in user
						pst.setString(i++, safrLogin.getUserId());
					}

					pst.setInt(i++, lrIndexTransfer.getEnvironmentId());
					pst.setInt(i++, lrIndexTransfer.getId());
                    if (!isForImport) {
                        ResultSet rs = pst.executeQuery();
                        rs.next();
                        lrIndexTransfer.setModifyTime(rs.getDate(1));                      
                        lrIndexTransfer.setModifyBy(safrLogin.getUserId());                      
                        rs.close();                 
                    } else {
                        int count  = pst.executeUpdate();   
                        if (count == 0) {
                            throw new SAFRNotFoundException("No Rows updated.");
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
			throw DataUtilities.createDAOException(
			    "Database error occurred while updating LR index for the Logical Record.", e);
		}
		return lrIndexTransfer;
	}

	public void removeLRIndex(Integer lrIndexId, Integer lrId, Integer environmentId)
			throws DAOException {
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ENVID);
			idNames.add(COL_INDEXID);

			String statement = generator.getDeleteStatement(params.getSchema(),
					TABLE_LRINDEX, idNames);
			PreparedStatement pst = null;

			while (true) {
				try {

					pst = con.prepareStatement(statement);
					pst.setInt(1, environmentId);
					pst.setInt(2, lrIndexId);
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
			    "Database error occurred while deleting LR index for the Logical Record.",e);
		}
	}

	public void removeLRIndexForLR(Integer lrId, Integer environmentId)
			throws DAOException {
		try {
			String statement = "delete from " + params.getSchema()
					+ ".LRINDEX A " + "where A.LOGRECID IN "
					+ "(select B.LOGRECID from " + params.getSchema()
					+ ".LOGREC B " + "where B.ENVIRONID = ? and B.LOGRECID = ? "
					+ "and B.ENVIRONID = A.ENVIRONID and B.LOGRECID = A.LOGRECID)";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					pst.setInt(1, environmentId);
					pst.setInt(2, lrId);
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
			    "Database error occurred while deleting LR index for Logical Record "+ lrId + " in Environment " + environmentId + ".", e);
		}
	}

	public void persistLRIndexFields(List<LRIndexFieldTransfer> lrIndexFieldTransfers)
			throws DAOException {
		LRIndexFieldTransfer trans = lrIndexFieldTransfers.get(0);
		// Delete any existing LRIndexField rows
		removeLRIndexFields(trans.getAssociatingComponentId(), trans.getEnvironmentId());
		// Insert new rows for the current LRIndexFields
		createLRIndexFields(lrIndexFieldTransfers);
	}

	public void removeLRIndexFields(Integer lrIndexId, Integer environmentId)
			throws DAOException {
		try {
            SAFRApplication.getTimingMap().startTiming("PGLogicalRecordDAO.removeLRIndexFields");
		    
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ENVID);
			idNames.add(COL_INDEXID);

			String statement = generator.getDeleteStatement(params.getSchema(),
					TABLE_LRINDEXFLD, idNames);
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					pst.setInt(1, environmentId);
					pst.setInt(2, lrIndexId);
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
			    "Database error occurred while deleting LR index fields for the Logical Record.",e);
		}
		SAFRApplication.getTimingMap().stopTiming("PGLogicalRecordDAO.removeLRIndexFields");
	}

	public void removeLRIndexFieldsForLR(Integer lrId, Integer environmentId)
			throws DAOException {
		try {
			String statement = "delete from " + params.getSchema()
					+ ".LRINDEXFLD A " + "where A.LRINDEXID IN "
					+ "(select B.LRINDEXID from " + params.getSchema()
					+ ".LRINDEX B, " + params.getSchema() + ".LOGREC C "
					+ "where C.ENVIRONID = ? and C.LOGRECID = ? "
					+ "and C.ENVIRONID = B.ENVIRONID and C.LOGRECID = B.LOGRECID" 
					+ " AND A.ENVIRONID = B.ENVIRONID)";	
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					pst.setInt(1, environmentId);
					pst.setInt(2, lrId);
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
			    "Database error occurred while deleting LR index fields for Logical Record "
				+ lrId + " in Environment " + environmentId + ".",e);
		}
	}

	private void createLRIndexFields(
			List<LRIndexFieldTransfer> lrIndexFieldTransfers)
			throws DAOException {
		try {
		    SAFRApplication.getTimingMap().startTiming("PGLogicalRecordDAO.createLRIndexFields");
		    
			String[] columnNames = { COL_ENVID, COL_INDEXID,
					COL_SEQNO, COL_LRFLDID, COL_CREATETIME, COL_CREATEBY,
					COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
            if (!lrIndexFieldTransfers.isEmpty() && 
                lrIndexFieldTransfers.get(0).isForImportOrMigration()) {
                names.add(1, COL_INDEXFLDID);
            }
			PreparedStatement pst = null;
            ResultSet rs = null;
			while (true) {
				try {

					for (LRIndexFieldTransfer lrIndexFieldTransfer : lrIndexFieldTransfers) {
						if (pst == null) {
							String statement = generator.getInsertStatement(
									params.getSchema(), TABLE_LRINDEXFLD, COL_INDEXFLDID, 
									names, !lrIndexFieldTransfer.isForImportOrMigration());
							pst = con.prepareStatement(statement);
						}
						int i = 1;
						pst.setInt(i++, lrIndexFieldTransfer.getEnvironmentId());
						if (lrIndexFieldTransfer.isForImportOrMigration()) {
						    pst.setInt(i++, lrIndexFieldTransfer.getAssociationId()); // xlrindexfldid
						}
						pst.setInt(i++, lrIndexFieldTransfer.getAssociatingComponentId()); // lrindexid
						pst.setInt(i++, lrIndexFieldTransfer.getFldSeqNbr());
						pst.setInt(i++, lrIndexFieldTransfer.getAssociatedComponentId()); // xlrfldid
						if (lrIndexFieldTransfer.isForImportOrMigration()) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(lrIndexFieldTransfer.getCreateTime()));
						}
						pst.setString(i++, lrIndexFieldTransfer.isForImportOrMigration() ? 
						    lrIndexFieldTransfer.getCreateBy() : safrLogin.getUserId());
						if (lrIndexFieldTransfer.isForImportOrMigration()) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(
							    lrIndexFieldTransfer.getModifyTime()));
						}
						pst.setString(i++, lrIndexFieldTransfer
							.isForImportOrMigration() ? lrIndexFieldTransfer
							.getModifyBy() : safrLogin.getUserId());

						rs = pst.executeQuery();
			            rs.next();
			            int id = rs.getInt(1);          
			            rs.close();		
			            lrIndexFieldTransfer.setAssociationId(id);
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
			throw DataUtilities.createDAOException(
			    "Database error occurred while creating LR index fields for the Logical Record.",e);
		}
		SAFRApplication.getTimingMap().stopTiming("PGLogicalRecordDAO.createLRIndexFields");
		
	}

	public List<LogicalFileQueryBean> queryPossibleLFAssociations(
			Integer environmentId, List<Integer>  notInParam) throws DAOException {

		List<LogicalFileQueryBean> result = new ArrayList<LogicalFileQueryBean>();

		try {
            boolean admin = SAFRApplication.getUserSession().isSystemAdministrator(); 
            String placeholders = generator.getPlaceholders(notInParam.size());

			String selectString;
			if (admin) {
				selectString = "Select LOGFILEID, NAME From "
						+ params.getSchema() + ".LOGFILE"
						+ " Where ENVIRONID = ? ";
			} else {
				selectString = "Select A.LOGFILEID, A.NAME,L.RIGHTS From "
						+ params.getSchema()
						+ ".LOGFILE A "
						+ "LEFT OUTER JOIN "
						+ params.getSchema()
						+ ".SECLOGFILE L "
						+ "ON A.ENVIRONID = L.ENVIRONID AND A.LOGFILEID = L.LOGFILEID"
						+ " AND L.GROUPID = ?"
						+ " Where A.ENVIRONID = ? ";
			}
			if(notInParam.size() > 0) {
				selectString += " AND LOGFILEID > 0" + " AND LOGFILEID NOT IN (" + placeholders + ")";
			}
			selectString += " Order By LOGFILEID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx =1;
					if(admin) {
						pst.setInt(ndx++, environmentId);
						for(int i=0; i<notInParam.size(); i++) {
							pst.setInt(ndx++, notInParam.get(i));
						}
					} else {
						pst.setInt(ndx++, SAFRApplication.getUserSession().getGroup().getId());
						pst.setInt(ndx++, environmentId);
						for(int i=0; i<notInParam.size(); i++) {
							pst.setInt(ndx++, notInParam.get(i));
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
				LogicalFileQueryBean logicalFileQueryBean = new LogicalFileQueryBean(
					environmentId, rs.getInt(i++), 
					DataUtilities.trimString(rs.getString(i++)), 
                    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.LogicalFile, environmentId), 
					null, null, null, null);
				result.add(logicalFileQueryBean);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while retrieving all the possible Logical Files which can be associated with this Logical Record.",e);
		}
		return result;
	}

	public List<ComponentAssociationTransfer> persistAssociatedLF(
			List<ComponentAssociationTransfer> componentAssociationTransfers,
			Integer logicalRecordId) throws DAOException {

		List<ComponentAssociationTransfer> associatedLFCreate = new ArrayList<ComponentAssociationTransfer>();
		List<ComponentAssociationTransfer> associatedLFUpdate = new ArrayList<ComponentAssociationTransfer>();

		for (ComponentAssociationTransfer associateLF : componentAssociationTransfers) {
			if (!associateLF.isPersistent()) {
				associatedLFCreate.add(associateLF);
			} else {
				associatedLFUpdate.add(associateLF);
			}
		}
		if (associatedLFCreate.size() > 0) {
			associatedLFCreate = createAssociatedLFs(
					associatedLFCreate, logicalRecordId);
		}
		if (associatedLFUpdate.size() > 0) {
			associatedLFUpdate = updateAssociatedLFs(associatedLFUpdate);
		}
		componentAssociationTransfers = new ArrayList<ComponentAssociationTransfer>();
		componentAssociationTransfers.addAll(associatedLFCreate);
		componentAssociationTransfers.addAll(associatedLFUpdate);
		return componentAssociationTransfers;

	}

	private List<ComponentAssociationTransfer> updateAssociatedLFs(
			List<ComponentAssociationTransfer> associatedLFUpdates)
			throws DAOException {
		
		// data is either all imported or migrated or none of it is
		boolean isImportOrMigrate = associatedLFUpdates.get(0).isForImport()
				|| associatedLFUpdates.get(0).isForMigration() ? true : false;
		boolean useCurrentTS = !isImportOrMigrate;

		try {
			List<String> names = new ArrayList<String>();
			names.add("LOGFILEID");
			names.add("LOGRECID");
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
			idNames.add("LRLFASSOCID");
			idNames.add(COL_ENVID);

			PreparedStatement pst = null;
			while (true) {
				try {
					for (ComponentAssociationTransfer associatedLFtoUpdate : associatedLFUpdates) {
						if (pst == null) {
							String statement = generator.getUpdateStatement(
									params.getSchema(), "LRLFASSOC", names,
									idNames, useCurrentTS);
							pst = con.prepareStatement(statement);
						}

						int i = 1;
						pst.setInt(i++, associatedLFtoUpdate
								.getAssociatedComponentId());
						pst.setInt(i++, associatedLFtoUpdate
								.getAssociatingComponentId());
						if (isImportOrMigrate) {
							// created and lastmod details set from source component
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(associatedLFtoUpdate.getCreateTime()));
							pst.setString(i++, associatedLFtoUpdate.getCreateBy());
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(associatedLFtoUpdate.getModifyTime()));
							pst.setString(i++, associatedLFtoUpdate.getModifyBy());
						} else {
							// created details are untouched
							// lastmodtimestamp is CURRENT_TIMESTAMP
							// lastmoduserid is logged in user
							pst.setString(i++, safrLogin.getUserId());
						}
						pst.setInt(i++, associatedLFtoUpdate.getAssociationId());
						pst.setInt(i++, associatedLFtoUpdate.getEnvironmentId());
                        if (useCurrentTS) {
                            ResultSet rs = pst.executeQuery();
                            rs.next();
                            associatedLFtoUpdate.setModifyTime(rs.getDate(1));                      
                            associatedLFtoUpdate.setModifyBy(safrLogin.getUserId());                      
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
			throw DataUtilities.createDAOException("Database error occurred while updating associations of Logical Record with Logical Files.",e);
		}
		return associatedLFUpdates;
	}

	private List<ComponentAssociationTransfer> createAssociatedLFs(
			List<ComponentAssociationTransfer> associatedLFCreates,
			Integer logicalRecordId) throws DAOException {
		
		// data is either all imported or migrated or none of it is
		boolean isImportOrMigrate = associatedLFCreates.get(0).isForImport()
				|| associatedLFCreates.get(0).isForMigration() ? true : false;
		boolean useCurrentTS = !isImportOrMigrate;

		try {
			String[] columnNames = { COL_ENVID, "LOGRECID", "LOGFILEID",
					COL_CREATETIME, COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
            if (isImportOrMigrate) {
                names.add(1, "LRLFASSOCID");
            }
			PreparedStatement pst = null;
            ResultSet rs = null;
			while (true) {
				try {

					for (ComponentAssociationTransfer associatedLFtoCreate : associatedLFCreates) {

						if (pst == null) {
							String statement = generator.getInsertStatement(
									params.getSchema(), "LRLFASSOC", "LRLFASSOCID",  names,
									useCurrentTS);
							pst = con.prepareStatement(statement);
						}

						int i = 1;
						pst.setInt(i++, associatedLFtoCreate.getEnvironmentId());
			            if (isImportOrMigrate) {
			                pst.setInt(i++, associatedLFtoCreate.getAssociationId());
			            }
						pst.setInt(i++, logicalRecordId);
                        pst.setInt(i++, associatedLFtoCreate.getAssociatedComponentId());
						if (isImportOrMigrate) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(associatedLFtoCreate.getCreateTime()));
						}
						pst.setString(i++,isImportOrMigrate ? associatedLFtoCreate.getCreateBy() : safrLogin.getUserId());
						if (isImportOrMigrate) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(associatedLFtoCreate.getModifyTime()));
						}
						pst.setString(i++,isImportOrMigrate ? associatedLFtoCreate.getModifyBy() : safrLogin.getUserId());
						rs = pst.executeQuery();
			            rs.next();
			            int id = rs.getInt(1);          
			            rs.close();
			            associatedLFtoCreate.setAssociationId(id);
						associatedLFtoCreate.setPersistent(true);
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
			throw DataUtilities.createDAOException("Database error occurred while creating associations of Logical Record with Logical Files.",e);
		}
		return associatedLFCreates;
	}

	public void deleteAssociatedLF(Integer environmentId, List<Integer>  inList)
			throws DAOException {
		try {
			String placeholders = generator.getPlaceholders(inList.size());
			String statement = "Delete From " + params.getSchema()
					+ ".LRLFASSOC" + " Where ENVIRONID = ? "
					+ " AND LRLFASSOCID IN (" + placeholders + ")";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int ndx = 1;
					pst.setInt(ndx++,  environmentId);
					for(int i=0; i<inList.size(); i++) {
						pst.setInt(ndx++, inList.get(i));
					}
					pst.executeUpdate();
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
			throw DataUtilities.createDAOException("Database error occurred while deleting associations of Logical Record with Logical Files.",e);
		}
	}

	public Map<Integer, List<DependentComponentTransfer>> getFieldLookupDependencies(
			Integer environmentId, List<Integer>  fieldsToBeDeleted) throws DAOException {
		Map<Integer, List<DependentComponentTransfer>> lookupMap = new HashMap<Integer, List<DependentComponentTransfer>>();

		try {
			String placeholders =  generator.getPlaceholders(fieldsToBeDeleted.size());
			String schema = params.getSchema();

			// Query to Check if the LR is used as a Target in any of the
			// Lookups(active or inactive).But the check is only applicable if
			// the selected field is a Primary key (exists in LRINDEXFLD).
			String selectString1 = "Select DISTINCT A.LOOKUPID, A.NAME, B.LRFIELDID From "
					+ schema
					+ ".LOOKUP A, "
					+ schema
					+ ".LRFIELDATTR B, "
					+ schema
					+ ".LRFIELD C, "
					+ schema
					+ ".LRINDEXFLD D, "
					+ schema
					+ ".LRLFASSOC E, "
					+ schema
					+ ".LOOKUPSTEP F "
					+ "Where B.LRFIELDID IN (" + placeholders 
					+ ") AND B.ENVIRONID = ? "
					+ "AND C.LRFIELDID = B.LRFIELDID AND C.ENVIRONID = B.ENVIRONID "
					+ "AND C.LRFIELDID = D.LRFIELDID AND D.ENVIRONID = B.ENVIRONID "
					+ "AND E.LOGRECID=C.LOGRECID AND E.ENVIRONID = B.ENVIRONID "
					+ "AND F.LRLFASSOCID=E.LRLFASSOCID AND F.ENVIRONID = B.ENVIRONID "
					+ "AND A.LOOKUPID = F.LOOKUPID AND A.ENVIRONID = F.ENVIRONID ";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString1);
					int ndx = 1;
					for(int i = 0; i<fieldsToBeDeleted.size(); i++) {
						pst.setInt(ndx++, fieldsToBeDeleted.get(i));
					}
					pst.setInt(ndx, environmentId);
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

				if (lookupMap.containsKey(rs.getInt("LRFIELDID"))) {
					lookupMap.get(rs.getInt("LRFIELDID")).add(depCompTransfer);
				} else {
					List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
					depCompTransfers.add(depCompTransfer);
					lookupMap.put(rs.getInt("LRFIELDID"), depCompTransfers);
				}
			}
			pst.close();
			rs.close();

			// Query to Check if the selected field is being used as a Source
			// key in any lookup (valid or invalid).

			String selectString2 = "Select DISTINCT A.LOOKUPID, A.NAME, D.LRFIELDID From "
					+ schema
					+ ".LOOKUP A, "
					+ schema
					+ ".LOOKUPSTEP B, "
					+ schema
					+ ".LOOKUPSRCKEY C, "
					+ schema
					+ ".LRFIELD D "
					+ "Where D.LRFIELDID IN (" + placeholders 
					+ ") AND A.ENVIRONID = ?"
					+ " AND B.LOOKUPID = A.LOOKUPID AND B.ENVIRONID = A.ENVIRONID "
					+ "AND C.LOOKUPSTEPID = B.LOOKUPSTEPID AND C.ENVIRONID = B.ENVIRONID "
					+ "AND D.LRFIELDID = C.LRFIELDID AND D.ENVIRONID = C.ENVIRONID "
					+ "GROUP BY A.LOOKUPID, A.NAME, D.LRFIELDID";

			pst = null;
			rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString2);
					int ndx = 1;
					for(int i = 0; i<fieldsToBeDeleted.size(); i++) {
						pst.setInt(ndx++, fieldsToBeDeleted.get(i));
					}
					pst.setInt(ndx, environmentId);
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

				if (lookupMap.containsKey(rs.getInt("LRFIELDID"))) {
					lookupMap.get(rs.getInt("LRFIELDID")).add(depCompTransfer);
				} else {
					List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
					depCompTransfers.add(depCompTransfer);
					lookupMap.put(rs.getInt("LRFIELDID"), depCompTransfers);
				}
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving Lookup Path dependencies for LR Fields of the Logical Record.",e);
		}
		return lookupMap;
	}

	public Map<Integer, List<DependentComponentTransfer>> getFieldViewDependencies(
			Integer environmentId, List<Integer>  fieldsToBeDeleted) throws DAOException {

		Map<Integer, List<DependentComponentTransfer>> viewMap = new HashMap<Integer, List<DependentComponentTransfer>>();
		try {
			String placeholders =  generator.getPlaceholders(fieldsToBeDeleted.size());

			String schema = params.getSchema();
			// Query 1: Field used in a View Column Source as a Source File
			// Field or a Lookup Field
			String selectString1 = "Select DISTINCT A.VIEWID, A.NAME, C.LRFIELDID, E.COLUMNNUMBER, E.HDRLINE1 From "
					+ schema
					+ ".VIEW A, "
					+ schema
					+ ".LRFIELDATTR B, "
					+ schema
					+ ".LRFIELD C, "
					+ schema
					+ ".VIEWCOLUMNSOURCE D, "
					+ schema
					+ ".VIEWCOLUMN E "
					+ "Where C.LRFIELDID IN (" + placeholders 
					+ ") AND B.ENVIRONID = ? "
					+ " AND B.LRFIELDID = C.LRFIELDID AND B.ENVIRONID = C.ENVIRONID "
					+ "AND D.LRFIELDID = C.LRFIELDID AND D.ENVIRONID = C.ENVIRONID "
					+ "AND A.VIEWID = D.VIEWID AND A.ENVIRONID = D.ENVIRONID "
					+ "AND D.VIEWID = E.VIEWID AND D.ENVIRONID = E.ENVIRONID "
					+ "AND D.VIEWCOLUMNID = E.VIEWCOLUMNID";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString1);
					int ndx = 1;
					for(int i = 0; i<fieldsToBeDeleted.size(); i++) {
						pst.setInt(ndx++, fieldsToBeDeleted.get(i));
					}
					pst.setInt(ndx, environmentId);
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
				depCompTransfer.setDependencyInfo("[Col "
						+ rs.getInt("COLUMNNUMBER") + colName
						+ ", Column Source]");

				if (viewMap.containsKey(rs.getInt("LRFIELDID"))) {
					viewMap.get(rs.getInt("LRFIELDID")).add(depCompTransfer);
				} else {
					List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
					depCompTransfers.add(depCompTransfer);
					viewMap.put(rs.getInt("LRFIELDID"), depCompTransfers);
				}
			}

			// Query 2 - Logic Text : Field used in Extract Column Assignment
			String selectString2 = "SELECT DISTINCT C.VIEWCOLUMNID, A.VIEWID, A.NAME, C.COLUMNNUMBER, C.HDRLINE1, E.LRFIELDID FROM "
					+ schema
					+ ".VIEW A, "
					+ schema
					+ ".VIEWLOGICDEPEND B,"
					+ schema
					+ ".VIEWCOLUMN C,"
					+ schema
					+ ".VIEWCOLUMNSOURCE D, "
					+ schema
					+ ".LRFIELD E "
					+ "WHERE E.LRFIELDID IN  (" + placeholders 
					+ ") AND A.ENVIRONID = B.ENVIRONID AND B.ENVIRONID = C.ENVIRONID AND C.ENVIRONID = D.ENVIRONID AND "
					+ "B.LRFIELDID = E.LRFIELDID AND B.ENVIRONID = E.ENVIRONID AND "
					+ "B.LOGICTYPECD = 2 AND "
					+ "B.PARENTID = D.VIEWCOLUMNSOURCEID AND D.VIEWCOLUMNID=C.VIEWCOLUMNID AND D.SOURCETYPEID = 4 AND "
					+ "B.VIEWID = C.VIEWID AND C.VIEWID = D.VIEWID AND "
					+ "A.VIEWID = B.VIEWID AND A.ENVIRONID = ?";

			pst.close();
			pst = null;
			rs.close();
			rs = null;

			while (true) {
				try {
					pst = con.prepareStatement(selectString2);
					int ndx = 1;
					for(int i = 0; i<fieldsToBeDeleted.size(); i++) {
						pst.setInt(ndx++, fieldsToBeDeleted.get(i));
					}
					pst.setInt(ndx, environmentId);
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
				depCompTransfer.setDependencyInfo("[Col " + rs.getInt("COLUMNNUMBER")+ colName + ", Logic Text]");

				if (viewMap.containsKey(rs.getInt("LRFIELDID"))) {
					viewMap.get(rs.getInt("LRFIELDID")).add(depCompTransfer);
				} else {
					List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
					depCompTransfers.add(depCompTransfer);
					viewMap.put(rs.getInt("LRFIELDID"), depCompTransfers);
				}
			}

			// Query 3 - Logic Text : - Field used in Extract Record Filter
			selectString2 = "SELECT DISTINCT A.VIEWID, A.NAME, C.SRCSEQNBR, D.LRFIELDID FROM "
					+ schema
					+ ".VIEW A, "
					+ schema
					+ ".VIEWLOGICDEPEND B,"
					+ schema
					+ ".VIEWSOURCE C, "
					+ schema
					+ ".LRFIELD D "
					+ "WHERE D.LRFIELDID IN (" + placeholders 
					+ " ) AND A.ENVIRONID =B.ENVIRONID AND B.ENVIRONID = C.ENVIRONID AND "
					+ "B.PARENTID = C.VIEWSOURCEID AND B.LOGICTYPECD = 1 AND "
					+ "B.LRFIELDID = D.LRFIELDID AND B.ENVIRONID = D.ENVIRONID AND "
					+ "A.VIEWID = B.VIEWID AND B.VIEWID = C.VIEWID"
					+ " AND A.ENVIRONID = ?";

			pst.close();
			pst = null;
			rs.close();
			rs = null;

			while (true) {
				try {
					pst = con.prepareStatement(selectString2);
					int ndx = 1;
					for(int i = 0; i<fieldsToBeDeleted.size(); i++) {
						pst.setInt(ndx++, fieldsToBeDeleted.get(i));
					}
					pst.setInt(ndx, environmentId);
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
				depCompTransfer.setDependencyInfo("[Src "
						+ rs.getInt("SRCSEQNBR") + ", Logic Text]");

				if (viewMap.containsKey(rs.getInt("LRFIELDID"))) {
					viewMap.get(rs.getInt("LRFIELDID")).add(depCompTransfer);
				} else {
					List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
					depCompTransfers.add(depCompTransfer);
					viewMap.put(rs.getInt("LRFIELDID"), depCompTransfers);
				}
			}

            // Query 4 - Logic Text : - Field used in Extract Output 
            selectString2 = "SELECT DISTINCT A.VIEWID, A.NAME, C.SRCSEQNBR, D.LRFIELDID FROM " +
                params.getSchema() + ".VIEW A, " +
                params.getSchema() + ".VIEWLOGICDEPEND B, " +
                params.getSchema() + ".VIEWSOURCE C, " +
                params.getSchema() + ".LRFIELD D " +
                "WHERE A.ENVIRONID=B.ENVIRONID " +
                "AND A.VIEWID=B.VIEWID " +
                "AND B.ENVIRONID=C.ENVIRONID " +
                "AND B.PARENTID=C.VIEWSOURCEID " +
                "AND B.ENVIRONID=D.ENVIRONID " +
                "AND B.LRFIELDID=D.LRFIELDID " +
                "AND B.LOGICTYPECD=5 " +
                "AND D.ENVIRONID= ? " + 
                " AND D.LRFIELDID IN(" + placeholders + ")";

            pst.close();
            pst = null;
            rs.close();
            rs = null;

            while (true) {
                try {
                    pst = con.prepareStatement(selectString2);
					int ndx = 1;
					pst.setInt(ndx++, environmentId);
					for(int i = 0; i<fieldsToBeDeleted.size(); i++) {
						pst.setInt(ndx++, fieldsToBeDeleted.get(i));
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
                depCompTransfer.setDependencyInfo("[Output "
                        + rs.getInt("SRCSEQNBR") + ", Logic Text]");

                if (viewMap.containsKey(rs.getInt("LRFIELDID"))) {
                    viewMap.get(rs.getInt("LRFIELDID")).add(depCompTransfer);
                } else {
                    List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
                    depCompTransfers.add(depCompTransfer);
                    viewMap.put(rs.getInt("LRFIELDID"), depCompTransfers);
                }
            }
			
			// Query 5: Field used as a Sort Key Title field
			selectString1 = "Select DISTINCT A.VIEWID, A.NAME, C.LRFIELDID, E.COLUMNNUMBER, E.HDRLINE1 From "
					+ schema
					+ ".VIEW A, "
					+ schema
					+ ".LRFIELDATTR B, "
					+ schema
					+ ".LRFIELD C, "
					+ schema
					+ ".VIEWCOLUMNSOURCE D, "
					+ schema
					+ ".VIEWCOLUMN E "
					+ "Where C.LRFIELDID IN (" + placeholders 
					+ ") AND B.ENVIRONID = ?"
					+ " AND B.LRFIELDID = C.LRFIELDID AND B.ENVIRONID = C.ENVIRONID "
					+ "AND D.SORTTITLELRFIELDID = C.LRFIELDID AND D.ENVIRONID = C.ENVIRONID "
					+ "AND A.VIEWID = D.VIEWID AND A.ENVIRONID = D.ENVIRONID "
					+ "AND D.VIEWID = E.VIEWID AND D.ENVIRONID = E.ENVIRONID "
					+ "AND D.VIEWCOLUMNID = E.VIEWCOLUMNID";

			pst.close();
			pst = null;
			rs.close();
			rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString1);
					int ndx = 1;
					for(int i = 0; i<fieldsToBeDeleted.size(); i++) {
						pst.setInt(ndx++, fieldsToBeDeleted.get(i));
					}
					pst.setInt(ndx++, environmentId);
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
				depCompTransfer.setDependencyInfo("[Col "
						+ rs.getInt("COLUMNNUMBER") + colName
						+ ", Sort Key Title]");

				if (viewMap.containsKey(rs.getInt("LRFIELDID"))) {
					viewMap.get(rs.getInt("LRFIELDID")).add(depCompTransfer);
				} else {
					List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
					depCompTransfers.add(depCompTransfer);
					viewMap.put(rs.getInt("LRFIELDID"), depCompTransfers);
				}
			}

			// Query 6: Field used as an Effective Date Source File Field
			selectString1 = "Select DISTINCT A.VIEWID, A.NAME, C.LRFIELDID, E.COLUMNNUMBER, E.HDRLINE1 From "
					+ schema
					+ ".VIEW A, "
					+ schema
					+ ".LRFIELDATTR B, "
					+ schema
					+ ".LRFIELD C, "
					+ schema
					+ ".VIEWCOLUMNSOURCE D, "
					+ schema
					+ ".VIEWCOLUMN E "
					+ "Where C.LRFIELDID IN (" + placeholders 
					+ ") AND B.ENVIRONID = ?"
					+ " AND B.LRFIELDID = C.LRFIELDID AND B.ENVIRONID = C.ENVIRONID "
					+ "AND D.EFFDATELRFIELDID = C.LRFIELDID AND D.ENVIRONID = C.ENVIRONID "
					+ "AND A.VIEWID = D.VIEWID AND A.ENVIRONID = D.ENVIRONID "
					+ "AND D.VIEWID = E.VIEWID AND D.ENVIRONID = E.ENVIRONID "
					+ "AND D.VIEWCOLUMNID = E.VIEWCOLUMNID";

			pst.close();
			pst = null;
			rs.close();
			rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString1);
					int ndx = 1;
					for(int i = 0; i<fieldsToBeDeleted.size(); i++) {
						pst.setInt(ndx++, fieldsToBeDeleted.get(i));
					}
					pst.setInt(ndx++, environmentId);
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
				depCompTransfer.setName(DataUtilities.trimString(rs
						.getString("NAME")));
				String colName = "";
				String heading1 = rs.getString("HDRLINE1");
				if (heading1 != null && !heading1.equals("")) {
					colName = ", " + heading1;
				}
				depCompTransfer.setDependencyInfo("[Col "
						+ rs.getInt("COLUMNNUMBER") + colName
						+ ", Effective Date]");

				if (viewMap.containsKey(rs.getInt("LRFIELDID"))) {
					viewMap.get(rs.getInt("LRFIELDID")).add(depCompTransfer);
				} else {
					List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
					depCompTransfers.add(depCompTransfer);
					viewMap.put(rs.getInt("LRFIELDID"), depCompTransfers);
				}
			}

			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving View dependencies for LR Fields of the Logical Record.",e);
		}
		return viewMap;
	}

	public List<DependentComponentTransfer> getLRLookupDependencies(
			Integer environmentId, Integer logicalRecordId,
			Collection<Integer> exceptionList) throws DAOException {
		List<DependentComponentTransfer> depLookupsList = new ArrayList<DependentComponentTransfer>();

		String exceptionPlaceholders = generator.getPlaceholders(exceptionList.size());
		try {
			String schema = params.getSchema();
			String selectString = "SELECT A.LOOKUPID, A.NAME, A.VALIDIND " +
					"FROM " + schema + ".LOOKUP A, "
					+ schema + ".LOOKUPSTEP B "
					+ "WHERE A.ENVIRONID = B.ENVIRONID "
                    + "AND A.LOOKUPID = B.LOOKUPID "
					+ "AND B.ENVIRONID = ? " 
					+ "AND B.SRCLRID = ? " 
					+ "AND B.SRCLRID > 0 ";
			if(exceptionList.size() > 0) {
				selectString += "AND B.LOOKUPID NOT IN (" + exceptionPlaceholders + " ) ";
			}
			selectString +=  "UNION "
					+ "SELECT A.LOOKUPID, A.NAME, A.VALIDIND " 
					+ "FROM " + schema + ".LOOKUP A, "
					+ schema + ".LOOKUPSTEP B, "
					+ schema + ".LRLFASSOC C "
					+ "WHERE A.ENVIRONID = B.ENVIRONID "
                    + "AND A.LOOKUPID = B.LOOKUPID "
					+ "AND B.ENVIRONID = C.ENVIRONID "
                    + "AND B.LRLFASSOCID = C.LRLFASSOCID "
					+ "AND C.ENVIRONID = ? "
					+ "AND C.LOGRECID = ? ";
			if(exceptionList.size() > 0) {
				selectString += "AND B.LOOKUPID NOT IN (" + exceptionPlaceholders + " ) ";
			}
			
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					pst.setInt(ndx++, environmentId);
					pst.setInt(ndx++, logicalRecordId);
					Iterator<Integer> ei = exceptionList.iterator();
					while(ei.hasNext()) {
						pst.setInt(ndx++, ei.next());
					}
					pst.setInt(ndx++, environmentId);
					pst.setInt(ndx++, logicalRecordId);
					Iterator<Integer> ei2 = exceptionList.iterator();
					while(ei2.hasNext()) {
						pst.setInt(ndx++, ei2.next());
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
				DependentComponentTransfer depLookup = new DependentComponentTransfer();
				depLookup.setId(rs.getInt("LOOKUPID"));
				depLookup.setName(DataUtilities.trimString(rs.getString("NAME")));
				depLookup.setActive(DataUtilities.intToBoolean(rs.getInt("VALIDIND")));
				depLookupsList.add(depLookup);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while retrieving Lookup Paths where the Logical Record is being used as a Source or Target.",e);
		}
		return depLookupsList;
	}

	public List<DependentComponentTransfer> getLRViewDependencies(
			Integer environmentId, Integer logicalRecordId,
			Collection<Integer> lookupIdList, Collection<Integer> exceptionList)
			throws DAOException {
		List<DependentComponentTransfer> depViewsList = new ArrayList<DependentComponentTransfer>();

		String exceptionPlaceholders = generator.getPlaceholders(exceptionList.size())	;

		try {
			String schema = params.getSchema();

			PreparedStatement pst = null;
			ResultSet rs = null;
			
			if (lookupIdList.size() > 0) {
					
				// these queries are run for each Lookup where LR is being used as a
				// Source or LR is being used as a Target of any Lookup
				String luPlaceholders = generator.getPlaceholders(lookupIdList.size())	;
				
				// Query 1: those views where Lookup Path is used in a column source
				String selectString = "SELECT DISTINCT A.VIEWID, A.NAME, A.VIEWSTATUSCD, C.COLUMNNUMBER, C.HDRLINE1 FROM "
						+ schema
						+ ".VIEW A , "
						+ schema
						+ ".VIEWCOLUMNSOURCE B, "
						+ schema
						+ ".VIEWCOLUMN C "
						+ "WHERE ";
				if(exceptionList.size() > 0) {
					selectString += " A.VIEWID NOT IN (" + exceptionPlaceholders + ") AND ";
				}
				selectString += " A.VIEWID = B.VIEWID AND B.ENVIRONID = A.ENVIRONID AND "
						+ "B.VIEWID=C.VIEWID AND B.ENVIRONID=C.ENVIRONID AND B.VIEWCOLUMNID=C.VIEWCOLUMNID "
						+ "AND A.ENVIRONID = ?"
						+ " AND B.LOOKUPID IN (" + luPlaceholders + ")";
				while (true) {
					try {
						pst = con.prepareStatement(selectString);
						int ndx = 1;
						Iterator<Integer> ei = exceptionList.iterator();
						while(ei.hasNext()) {
							pst.setInt(ndx++, ei.next());
						}
						pst.setInt(ndx++, environmentId);
						Iterator<Integer> lui = lookupIdList.iterator();
						while(lui.hasNext()) {
							pst.setInt(ndx++, lui.next());
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
					DependentComponentTransfer depView = new DependentComponentTransfer();
					depView.setId(rs.getInt("VIEWID"));
					depView.setName(DataUtilities.trimString(rs.getString("NAME")));
					String colName = "";
					String heading1 = rs.getString("HDRLINE1");
					if (heading1 != null && !heading1.equals("")) {
						colName = ", " + heading1;
					}
					depView.setDependencyInfo("[Col "
							+ rs.getInt("COLUMNNUMBER") + colName
							+ ", Column Source]");
					String status = rs.getString("VIEWSTATUSCD");
					if ("ACTVE".equals(status)) {
						depView.setActive(true);
					} else {
						depView.setActive(false);
					}
					depViewsList.add(depView);
				}
	
				// Query 2: those views where Lookup Path is used in the sort
				// key title of a sort key
				selectString = "SELECT DISTINCT A.VIEWID, A.NAME, A.VIEWSTATUSCD, C.COLUMNNUMBER, C.HDRLINE1 FROM "
						+ schema
						+ ".VIEW A , "
						+ schema
						+ ".VIEWCOLUMNSOURCE B, "
						+ schema
						+ ".VIEWCOLUMN C "
						+ "WHERE";
				if(exceptionList.size() > 0) {
					selectString += " A.VIEWID NOT IN (" + exceptionPlaceholders + ") AND ";
				}
				selectString += " A.VIEWID = B.VIEWID AND B.ENVIRONID = A.ENVIRONID AND "
						+ "B.VIEWID=C.VIEWID AND B.ENVIRONID=C.ENVIRONID AND B.VIEWCOLUMNID=C.VIEWCOLUMNID "
						+ "AND A.ENVIRONID = ? "
						+ " AND B.SORTTITLELOOKUPID IN (" + luPlaceholders + ")";
				pst.close();
				pst = null;
				rs.close();
				rs = null;
				while (true) {
					try {
						pst = con.prepareStatement(selectString);
						int ndx = 1;
						Iterator<Integer> ei = exceptionList.iterator();
						while(ei.hasNext()) {
							pst.setInt(ndx++, ei.next());
						}
						pst.setInt(ndx++, environmentId);
						Iterator<Integer> lui = lookupIdList.iterator();
						while(lui.hasNext()) {
							pst.setInt(ndx++, lui.next());
						}
	
						// for Query 2
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
					DependentComponentTransfer depView = new DependentComponentTransfer();
					depView.setId(rs.getInt("VIEWID"));
					depView.setName(DataUtilities.trimString(rs.getString("NAME")));
					String colName = "";
					String heading1 = rs.getString("HDRLINE1");
					if (heading1 != null && !heading1.equals("")) {
						colName = ", " + heading1;
					}
					depView.setDependencyInfo("[Col "
							+ rs.getInt("COLUMNNUMBER") + colName
							+ ", Sort Key Title]");
					String status = rs.getString("VIEWSTATUSCD");
					if ("ACTVE".equals(status)) {
						depView.setActive(true);
					} else {
						depView.setActive(false);
					}
					depViewsList.add(depView);
				}
				
				// Query 3 : those Views in which this lookup path is used in a
				// View's Extract Column Assignment
				selectString = "SELECT DISTINCT A.VIEWID, A.NAME, A.VIEWSTATUSCD, C.COLUMNNUMBER,C.HDRLINE1 FROM "
						+ schema
						+ ".VIEW A, "
						+ schema
						+ ".VIEWLOGICDEPEND B,"
						+ schema
						+ ".VIEWCOLUMN C,"
						+ schema
						+ ".VIEWCOLUMNSOURCE D WHERE";
				if(exceptionList.size() > 0) {
					selectString += " A.VIEWID NOT IN (" + exceptionPlaceholders + ") AND ";
				} 
				selectString += " A.ENVIRONID =B.ENVIRONID AND B.ENVIRONID = C.ENVIRONID AND C.ENVIRONID = D.ENVIRONID AND "
						+ "B.PARENTID = D.VIEWCOLUMNSOURCEID AND D.VIEWCOLUMNID=C.VIEWCOLUMNID AND "
						+ "B.VIEWID = C.VIEWID AND C.VIEWID = D.VIEWID AND B.LRFIELDID>0 AND B.LOGICTYPECD = 2 AND "
						+ "A.VIEWID = B.VIEWID AND A.ENVIRONID = ?"
						+ " AND B.LOOKUPID IN (" + luPlaceholders + ")";
	
				pst.close();
				pst = null;
				rs.close();
				rs = null;
				while (true) {
					try {
	
						pst = con.prepareStatement(selectString);
						int ndx = 1;
						Iterator<Integer> ei = exceptionList.iterator();
						while(ei.hasNext()) {
							pst.setInt(ndx++, ei.next());
						}
						pst.setInt(ndx++, environmentId);
						Iterator<Integer> lui = lookupIdList.iterator();
						while(lui.hasNext()) {
							pst.setInt(ndx++, lui.next());
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
					DependentComponentTransfer depView = new DependentComponentTransfer();
					depView.setId(rs.getInt("VIEWID"));
					depView.setName(DataUtilities.trimString(rs.getString("NAME")));
					String colName = "";
					String heading1 = rs.getString("HDRLINE1");
					if (heading1 != null && !heading1.equals("")) {
						colName = ", " + heading1;
					}
					depView.setDependencyInfo("[Col "
							+ rs.getInt("COLUMNNUMBER") + colName
							+ ", Logic Text]");
					String status = rs.getString("VIEWSTATUSCD");
					if ("ACTVE".equals(status)) {
						depView.setActive(true);
					} else {
						depView.setActive(false);
					}
					depViewsList.add(depView);
				}
	
				// Query 4: those Views in which this lookup path is used in a
				// View's Extract Record Filter
				selectString = "SELECT DISTINCT A.VIEWID, A.NAME, A.VIEWSTATUSCD, C.SRCSEQNBR FROM "
						+ schema
						+ ".VIEW A, "
						+ schema
						+ ".VIEWLOGICDEPEND B,"
						+ schema
						+ ".VIEWSOURCE C "
						+ "WHERE";
						if(exceptionList.size() > 0) {
							selectString += " A.VIEWID NOT IN (" + exceptionPlaceholders + ") AND ";
						} 
						selectString +=  " A.ENVIRONID =B.ENVIRONID AND B.ENVIRONID = C.ENVIRONID AND "
						+ "B.PARENTID = C.VIEWSOURCEID AND B.LOGICTYPECD = 1 AND "
						+ "A.VIEWID = B.VIEWID AND B.VIEWID = C.VIEWID"
						+ " AND B.LRFIELDID = 0 AND A.ENVIRONID = ? "
						+ " AND B.LOOKUPID IN ( " + luPlaceholders + " )";
				pst.close();
				pst = null;
				rs.close();
				rs = null;
				while (true) {
					try {
	
						pst = con.prepareStatement(selectString);
						int ndx = 1;
						Iterator<Integer> ei = exceptionList.iterator();
						while(ei.hasNext()) {
							pst.setInt(ndx++, ei.next());
						}
						pst.setInt(ndx++, environmentId);
						Iterator<Integer> lui = lookupIdList.iterator();
						while(lui.hasNext()) {
							pst.setInt(ndx++, lui.next());
						}
						// for Query 4
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
					DependentComponentTransfer depView = new DependentComponentTransfer();
					depView.setId(rs.getInt("VIEWID"));
					depView.setName(DataUtilities.trimString(rs.getString("NAME")));
					depView.setDependencyInfo("[Src " + rs.getInt("SRCSEQNBR")+ ", Logic Text]");
					String status = rs.getString("VIEWSTATUSCD");
					if ("ACTVE".equals(status)) {
						depView.setActive(true);
					} else {
						depView.setActive(false);
					}
					depViewsList.add(depView);
				}
	
				pst.close();
				rs.close();
			
                // Query 5: those Views in which this lookup path is used in a
                // View's Extract Output
                selectString = "SELECT DISTINCT A.VIEWID, A.NAME, A.VIEWSTATUSCD, C.SRCSEQNBR FROM " + 
                    params.getSchema() + ".VIEW A, " +
                    params.getSchema() + ".VIEWLOGICDEPEND B, " +
                    params.getSchema() + ".VIEWSOURCE C " +
                    "WHERE A.ENVIRONID=B.ENVIRONID " +
                    "AND A.VIEWID=B.VIEWID " +
                    "AND B.ENVIRONID=C.ENVIRONID " +
                    "AND B.PARENTID=C.VIEWSOURCEID " +
                    "AND B.LOGICTYPECD=5 " +
                    "AND B.ENVIRONID=? " + 
                    " AND B.LOOKUPID IN (" + luPlaceholders +")";
                pst.close();
                pst = null;
                rs.close();
                rs = null;
                while (true) {
                    try {
    
                        pst = con.prepareStatement(selectString);
						int ndx = 1;
						pst.setInt(ndx++, environmentId);
						Iterator<Integer> lui = lookupIdList.iterator();
						while(lui.hasNext()) {
							pst.setInt(ndx++, lui.next());
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
                    DependentComponentTransfer depView = new DependentComponentTransfer();
                    depView.setId(rs.getInt("VIEWID"));
                    depView.setName(DataUtilities.trimString(rs.getString("NAME")));
                    depView.setDependencyInfo("[Output " + rs.getInt("SRCSEQNBR")+ ", Logic Text]");
                    String status = rs.getString("VIEWSTATUSCD");
                    if ("ACTVE".equals(status)) {
                        depView.setActive(true);
                    } else {
                        depView.setActive(false);
                    }
                    depViewsList.add(depView);
                }
    
                pst.close();
                rs.close();
            
            }
							
			
			// Selects all the views where the LR is used as a Data source. Note
			// that LR/LF pair is used as a Data source, hence the join to
			// LRLFASSOC is needed
			String selectString2 = "Select A.VIEWID, A.NAME, A.VIEWSTATUSCD, B.SRCSEQNBR From "
					+ schema
					+ ".VIEW A, "
					+ schema
					+ ".VIEWSOURCE B, "
					+ schema
					+ ".LRLFASSOC C "
					+ "Where B.INLRLFASSOCID = C.LRLFASSOCID AND A.VIEWID = B.VIEWID "
					+ "AND A.ENVIRONID = B.ENVIRONID AND B.ENVIRONID = C.ENVIRONID "
					+ "AND C.LOGRECID = ?"
					+ " AND A.ENVIRONID = ?";
					if(exceptionList.size() > 0) {
						selectString2 += " AND B.VIEWID NOT IN (" + exceptionPlaceholders + ") ";
					} 
			PreparedStatement pst2 = null;
			rs = null;
			while (true) {
				try {
					pst2 = con.prepareStatement(selectString2);
					int ndx = 1;
					pst2.setInt(ndx++, logicalRecordId);
					pst2.setInt(ndx++, environmentId);
					Iterator<Integer> ei = exceptionList.iterator();
					while(ei.hasNext()) {
						pst2.setInt(ndx++, ei.next());
					}
					rs = pst2.executeQuery();
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
				DependentComponentTransfer depView = new DependentComponentTransfer();
				depView.setId(rs.getInt("VIEWID"));
				depView.setName(DataUtilities.trimString(rs.getString("NAME")));
				depView.setDependencyInfo("[View Source "
						+ rs.getInt("SRCSEQNBR") + "]");
				String status = rs.getString("VIEWSTATUSCD");
				if ("ACTVE".equals(status)) {
					depView.setActive(true);
				} else {
					depView.setActive(false);
				}
				depViewsList.add(depView);
			}
			pst2.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving Views where the Logical Record is being used.",e);
		}
		return depViewsList;
	}

	public Map<Integer, List<DependentComponentTransfer>> getAssociatedLFLookupDependencies(
			Integer environmentId, List<Integer> LRLFAssociationIds)
			throws DAOException {

		Map<Integer, List<DependentComponentTransfer>> lookupMap = new HashMap<Integer, List<DependentComponentTransfer>>();
		if (LRLFAssociationIds.size() <= 0) {
			return lookupMap;
		}
		String placeholders = generator.getPlaceholders(LRLFAssociationIds.size());
		try {
			String schema = params.getSchema();
			String selectString = "Select A.LOOKUPID, A.NAME, A.DESTLRLFASSOCID From "
					+ schema + ".LOOKUP A "
					+ "Where A.ENVIRONID = ?"
					+ " AND A.DESTLRLFASSOCID IN (" + placeholders
					+ ") ORDER BY A.LOOKUPID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					pst.setInt(ndx++, environmentId);
					for(int i=0; i<LRLFAssociationIds.size(); i++) {
						pst.setInt(ndx++, LRLFAssociationIds.get(i));
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
				depCompTransfer.setId(rs.getInt("LOOKUPID"));
				depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));

				if (lookupMap.containsKey(rs.getInt("DESTLRLFASSOCID"))) {
					lookupMap.get(rs.getInt("DESTLRLFASSOCID")).add(depCompTransfer);
				} else {
					List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
					depCompTransfers.add(depCompTransfer);
					lookupMap.put(rs.getInt("DESTLRLFASSOCID"), depCompTransfers);
				}
			}

			String selectString1 = "Select A.LOOKUPID, A.NAME, B.LRLFASSOCID From "
					+ schema + ".LOOKUP A, "
					+ schema + ".LOOKUPSTEP B "
					+ "Where B.ENVIRONID = ?"
					+ " AND B.LRLFASSOCID IN (" + placeholders
					+ ") AND A.LOOKUPID = B.LOOKUPID AND A.ENVIRONID = ?"
					+ " ORDER BY A.LOOKUPID";

			PreparedStatement pst1 = null;
			ResultSet rs1 = null;
			while (true) {
				try {
					pst1 = con.prepareStatement(selectString1);
					int ndx = 1;
					pst1.setInt(ndx++, environmentId);
					for(int i=0; i<LRLFAssociationIds.size(); i++) {
						pst1.setInt(ndx++, LRLFAssociationIds.get(i));
					}
					pst1.setInt(ndx++, environmentId);
					rs1 = pst1.executeQuery();
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
			while (rs1.next()) {
				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rs1.getInt("LOOKUPID"));
				depCompTransfer.setName(DataUtilities.trimString(rs1.getString("NAME")));

				if (lookupMap.containsKey(rs1.getInt("LRLFASSOCID"))) {
					lookupMap.get(rs1.getInt("LRLFASSOCID")).add(depCompTransfer);
				} else {
					List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
					depCompTransfers.add(depCompTransfer);
					lookupMap.put(rs1.getInt("LRLFASSOCID"), depCompTransfers);
				}
			}

			String selectString2 = "Select A.LOOKUPID, A.NAME, B.LRLFASSOCID From "
					+ schema+ ".LOOKUP A, "
					+ schema+ ".LOOKUPSRCKEY B "
					+ "Where B.ENVIRONID = ?"
					+ " AND B.LRLFASSOCID IN ("+ placeholders
					+ ") AND A.LOOKUPID = B.LOOKUPID AND A.ENVIRONID = ?"
					+ " ORDER BY B.LOOKUPID";

			PreparedStatement pst2 = null;
			ResultSet rs2 = null;
			while (true) {
				try {
					pst2 = con.prepareStatement(selectString2);
					int ndx = 1;
					pst2.setInt(ndx++, environmentId);
					for(int i=0; i<LRLFAssociationIds.size(); i++) {
						pst2.setInt(ndx++, LRLFAssociationIds.get(i));
					}
					pst2.setInt(ndx++, environmentId);
					rs2 = pst2.executeQuery();
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
			while (rs2.next()) {

				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rs2.getInt("LOOKUPID"));
				depCompTransfer.setName(DataUtilities.trimString(rs2.getString("NAME")));

				if (lookupMap.containsKey(rs2.getInt("LRLFASSOCID"))) {
					lookupMap.get(rs2.getInt("LRLFASSOCID")).add(depCompTransfer);
				} else {
					List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
					depCompTransfers.add(depCompTransfer);
					lookupMap.put(rs2.getInt("LRLFASSOCID"), depCompTransfers);
				}

			}
			pst.close();
			rs.close();
			pst1.close();
			rs1.close();
			pst2.close();
			rs2.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving associated LF Lookup Path dependencies.",e);
		}
		return lookupMap;
	}

	public Map<Integer, List<DependentComponentTransfer>> getAssociatedLFViewDependencies(
			Integer environmentId, List<Integer> LRLFAssociationIds)
			throws DAOException {

		Map<Integer, List<DependentComponentTransfer>> viewMap = new HashMap<Integer, List<DependentComponentTransfer>>();
		if (LRLFAssociationIds.size() <= 0) {
			return viewMap;
		}
		String placeholders = generator.getPlaceholders(LRLFAssociationIds.size());
		try {
			String schema = params.getSchema();
			String selectString1 = "Select A.VIEWID, A.NAME, B.INLRLFASSOCID From "
					+ schema
					+ ".VIEW A, "
					+ schema
					+ ".VIEWSOURCE B "
					+ "Where B.ENVIRONID = ?"
					+ " AND B.INLRLFASSOCID IN ("
					+ placeholders
					+ ") AND A.VIEWID = B.VIEWID AND A.ENVIRONID = ?"
					+ " ORDER BY A.VIEWID";

			PreparedStatement pst1 = null;
			ResultSet rs1 = null;
			while (true) {
				try {
					pst1 = con.prepareStatement(selectString1);
					int ndx = 1;
					pst1.setInt(ndx++, environmentId);
					for(int i=0; i<LRLFAssociationIds.size(); i++) {
						pst1.setInt(ndx++, LRLFAssociationIds.get(i));
					}
					pst1.setInt(ndx++, environmentId);
					rs1 = pst1.executeQuery();
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
			while (rs1.next()) {

				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rs1.getInt("VIEWID"));
				depCompTransfer.setName(DataUtilities.trimString(rs1.getString("NAME")));

				if (viewMap.containsKey(rs1.getInt("INLRLFASSOCID"))) {
					viewMap.get(rs1.getInt("INLRLFASSOCID")).add(depCompTransfer);
				} else {
					List<DependentComponentTransfer> depCompTransfers = new ArrayList<DependentComponentTransfer>();
					depCompTransfers.add(depCompTransfer);
					viewMap.put(rs1.getInt("INLRLFASSOCID"), depCompTransfers);
				}

			}
			pst1.close();
			rs1.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving associated LF View dependencies.",e);
		}
		return viewMap;

	}

	public LogicalRecordTransfer getLogicalRecordFromLRLFAssociation(
			Integer LRLFAssociationId, Integer environmentId)
			throws DAOException {
		LogicalRecordTransfer logicalRecordTransfer = null;
		try {
			String selectString = "Select LOGRECID From " + params.getSchema()
					+ ".LRLFASSOC Where LRLFASSOCID = ?"
					+ " AND ENVIRONID = ?";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, LRLFAssociationId );
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
				Integer logicalRecordId = rs.getInt("LOGRECID");
				logicalRecordTransfer = getLogicalRecord(logicalRecordId,
						environmentId);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving Logical Record through an LR-LF association ID.",e);
		}
		return logicalRecordTransfer;
	}

	public ComponentAssociationTransfer getTargetLogicalFileAssociation(
			Integer LRLFAssociationId, Integer environmentId)
			throws DAOException {
		try {
			String schema = params.getSchema();
			String selectString = "Select A.LOGFILEID, B.NAME, A.LRLFASSOCID "
					+ "From " + schema + ".LRLFASSOC A, " + schema
					+ ".LOGFILE B " + "Where A.ENVIRONID = ?"
					+ " AND A.LRLFASSOCID = ?"
					+ " AND A.ENVIRONID = B.ENVIRONID AND A.LOGFILEID = B.LOGFILEID "
					+ "Order By A.LOGFILEID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, environmentId);
					pst.setInt(2, LRLFAssociationId);
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
			ComponentAssociationTransfer componentAssociationTransfer = new ComponentAssociationTransfer();
			if (rs.next()) {

				componentAssociationTransfer.setAssociatedComponentId(rs.getInt("LOGFILEID"));
				componentAssociationTransfer.setAssociatedComponentName(
				    DataUtilities.trimString(rs.getString("NAME")));
				componentAssociationTransfer.setAssociationId(rs.getInt("LRLFASSOCID"));
			}
			pst.close();
			rs.close();
			return componentAssociationTransfer;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving LR-LF association object through the association ID.",e);
		}
	}

	public ComponentAssociationTransfer getLRLFAssociation(
			Integer associationId, Integer environmentId) throws DAOException {
		ComponentAssociationTransfer transfer = new ComponentAssociationTransfer();
		try {
			String schema = params.getSchema();
			String selectString = "Select A.ENVIRONID, A.LRLFASSOCID, A.LOGRECID, "
					+ "C.NAME AS RECNAME, A.LOGFILEID, B.NAME AS FILENAME, " 
                    + "A.CREATEDTIMESTAMP, A.CREATEDUSERID, A.LASTMODTIMESTAMP, A.LASTMODUSERID "                                                                                   					
					+ "From " + schema
					+ ".LRLFASSOC A, " + schema + ".LOGFILE B, " + schema
					+ ".LOGREC C " + "Where A.ENVIRONID = ?"
					+ " AND A.LRLFASSOCID = ?"
					+ " AND A.ENVIRONID = B.ENVIRONID AND A.LOGFILEID = B.LOGFILEID "
					+ " AND A.ENVIRONID = C.ENVIRONID and A.LOGRECID = C.LOGRECID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, environmentId);
					pst.setInt(2, associationId);
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
				transfer.setAssociationId(rs.getInt("LRLFASSOCID"));
				transfer.setAssociatingComponentId(rs.getInt("LOGRECID"));
				transfer.setAssociatingComponentName(
				    DataUtilities.trimString(rs.getString("RECNAME")));
				transfer.setAssociatedComponentId(rs.getInt("LOGFILEID"));
				transfer.setAssociatedComponentName(
				    DataUtilities.trimString(rs.getString("FILENAME")));
                transfer.setCreateTime(rs.getDate(COL_CREATETIME));
                transfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
                transfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
                transfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));				
			}
			pst.close();
			rs.close();
			return transfer;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving LR-LF association.",e);
		}
	}

	public ComponentAssociationTransfer getLRLFAssociation(
			Integer LRId, 
			Integer LFId, 
			Integer environmentId) throws DAOException {
		ComponentAssociationTransfer transfer = null;
		try {
			String schema = params.getSchema();
			String selectString = "Select A.ENVIRONID, A.LRLFASSOCID, A.LOGRECID, "
					+ "C.NAME AS RECNAME, A.LOGFILEID, B.NAME AS FILENAME,  "
                    + "A.CREATEDTIMESTAMP, A.CREATEDUSERID, A.LASTMODTIMESTAMP, A.LASTMODUSERID " 					
					+ "From " + schema
					+ ".LRLFASSOC A, " + schema + ".LOGFILE B, " + schema
					+ ".LOGREC C " + "Where A.ENVIRONID = ?"
					+ " AND A.LOGRECID = ?"
					+ " AND A.LOGFILEID = ?"
					+ " AND A.ENVIRONID = B.ENVIRONID AND A.LOGFILEID = B.LOGFILEID "
					+ " AND A.ENVIRONID = C.ENVIRONID and A.LOGRECID = C.LOGRECID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, environmentId);
					pst.setInt(2, LRId);
					pst.setInt(3, LFId);
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
			    transfer = new ComponentAssociationTransfer();
				transfer.setEnvironmentId(rs.getInt("ENVIRONID"));
				transfer.setAssociationId(rs.getInt("LRLFASSOCID"));
				transfer.setAssociatingComponentId(rs.getInt("LOGRECID"));
				transfer.setAssociatingComponentName(
				    DataUtilities.trimString(rs.getString("RECNAME")));
				transfer.setAssociatedComponentId(rs.getInt("LOGFILEID"));
				transfer.setAssociatedComponentName(
				    DataUtilities.trimString(rs.getString("FILENAME")));
                transfer.setCreateTime(rs.getDate(COL_CREATETIME));
                transfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
                transfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
                transfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));             				
			}
			pst.close();
			rs.close();
			return transfer;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving LR-LF association.",e);
		}
	}
	
	public List<ComponentAssociationTransfer> getLRLFAssociations(
			Integer environmentId) throws DAOException {
		List<ComponentAssociationTransfer> result = new ArrayList<ComponentAssociationTransfer>();

		try {
			String schema = params.getSchema();
			String selectString = null;

	        boolean admin = SAFRApplication.getUserSession().isSystemAdministrator();
			
			if (admin) {
				selectString = "Select A.NAME AS RECNAME, A.LOGRECID, B.LOGFILEID, C.NAME AS FILENAME, B.LRLFASSOCID, "
                        + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "
						+ "From " 
                        + schema + ".LOGREC A, "
						+ schema + ".LRLFASSOC B, "
						+ schema + ".LOGFILE C "
						+ "Where A.ENVIRONID = ?"
						+ " AND A.ENVIRONID = B.ENVIRONID AND A.LOGRECID = B.LOGRECID"
						+ " AND B.ENVIRONID = C.ENVIRONID AND B.LOGFILEID = C.LOGFILEID "
						+ "Order By B.LRLFASSOCID";
			} else {
				selectString = "Select A.NAME AS RECNAME, A.LOGRECID, B.LOGFILEID, C.NAME AS FILENAME, B.LRLFASSOCID, D.RIGHTS, "
                        + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "
						+ "From "
						+ schema + ".LOGREC A INNER JOIN "
						+ schema + ".LRLFASSOC B ON A.ENVIRONID = B.ENVIRONID AND A.LOGRECID = B.LOGRECID INNER JOIN "
						+ schema + ".LOGFILE C ON B.ENVIRONID = C.ENVIRONID AND B.LOGFILEID = C.LOGFILEID LEFT OUTER JOIN "
						+ schema + ".SECLOGFILE D ON C.ENVIRONID = D.ENVIRONID AND C.LOGFILEID = D.LOGFILEID "
						+ " AND D.GROUPID = ?" 
						+ " Where A.ENVIRONID = ?"
						+ " Order By B.LRLFASSOCID";
			}
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					if(admin) {
						pst.setInt(1, environmentId);
					} else {
						pst.setInt(1, SAFRApplication.getUserSession().getGroup().getId());
						pst.setInt(2, environmentId);
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
				componentAssociationTransfer.setEnvironmentId(environmentId);
				componentAssociationTransfer.setAssociatingComponentId(rs.getInt("LOGRECID"));
				componentAssociationTransfer.setAssociatingComponentName(
				    DataUtilities.trimString(rs.getString("RECNAME")));
				componentAssociationTransfer.setAssociatedComponentId(rs.getInt("LOGFILEID"));
				componentAssociationTransfer.setAssociatedComponentName(
				    DataUtilities.trimString(rs.getString("FILENAME")));
				componentAssociationTransfer.setAssociationId(rs.getInt("LRLFASSOCID"));
                componentAssociationTransfer.setAssociatedComponentRights(
                    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.LogicalFile, environmentId));              
                componentAssociationTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
                componentAssociationTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
                componentAssociationTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
                componentAssociationTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));             				
				result.add(componentAssociationTransfer);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			String msg = "Database error occurred while retrieving LRFile associations for Environment ["+ environmentId + "].";
			throw DataUtilities.createDAOException(msg, e);
		}
	}

	public LogicalRecordQueryBean queryLogicalRecordByField(Integer LRFieldId,
			Integer environmentId) throws DAOException {
		LogicalRecordQueryBean logicalRecordQueryBean = null;
		try {
			String selectString = "Select B.LOGRECID, B.NAME From "
					+ params.getSchema() + ".LRFIELD A, "
					+ params.getSchema() + ".LOGREC B Where A.LRFIELDID = ? "
					+ " AND A.LOGRECID = B.LOGRECID AND A.ENVIRONID = B.ENVIRONID "
					+ "AND A.ENVIRONID = ? ";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1,  LRFieldId);
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
			if (rs.next()) {
				logicalRecordQueryBean = new LogicalRecordQueryBean(
					environmentId, rs.getInt("LOGRECID"), 
					DataUtilities.trimString(rs.getString("NAME")), 
					null, null, null, null, null, null, null, null, null, null, null);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the Logical Record to which the Field with specified ID belongs.",e);
		}
		return logicalRecordQueryBean;
	}

	public List<LRIndexQueryBean> queryLRIndexes(Integer environmentId)
			throws DAOException {
		List<LRIndexQueryBean> result = new ArrayList<LRIndexQueryBean>();

		try {
			String schema = params.getSchema();
			String selectString = "Select ENVIRONID, LRINDEXID, LOGRECID, "
					+ "CREATEDTIMESTAMP, CREATEDUSERID, LASTMODTIMESTAMP, LASTMODUSERID "
					+ "From "+ schema + ".LRINDEX "
					+ "Where ENVIRONID = ? "
					+ " ORDER BY LRINDEXID";
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
				LRIndexQueryBean lridx = new LRIndexQueryBean(rs
						.getInt("ENVIRONID"), rs.getInt("LRINDEXID"), rs
						.getInt("LOGRECID"), rs.getDate(COL_CREATETIME),
						DataUtilities.trimString(rs.getString(COL_CREATEBY)),
						rs.getDate(COL_MODIFYTIME), DataUtilities.trimString(rs
								.getString(COL_MODIFYBY)));
				result.add(lridx);
			}
			if (result.size() == 0) {
				logger.info("No LRIndexes were found in Environment [" + environmentId + "].");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			String msg = "Database error occurred while retrieving LR Indexes from Environment [" + environmentId + "].";
			throw DataUtilities.createDAOException(msg, e);
		}
		return result;
	}

    @Override
    public LogicalRecordQueryBean queryLogicalRecord(Integer lRID,
        Integer environmentId) {
        LogicalRecordQueryBean logicalRecordQueryBean = null;
        try {
            String selectString = "SELECT LOGRECID, NAME FROM "
                    + params.getSchema() + ".LOGREC  "
                    + "WHERE LOGRECID = ? "
                    + "AND ENVIRONID = ? ";
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, lRID);
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
            if (rs.next()) {
                logicalRecordQueryBean = new LogicalRecordQueryBean(
                    environmentId, rs.getInt("LOGRECID"), 
                    DataUtilities.trimString(rs.getString("NAME")), 
                    null, null, null, null, null, null, null, null, null, null, null);
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException("Database error occurred while retrieving the Logical Record to which the Field with specified ID belongs.",e);
        }
        return logicalRecordQueryBean;
    }

	@Override
	public Integer getNextKey() {
        try {
            String statement = "SELECT nextval(pg_get_serial_sequence('" + params.getSchema() + 
                    ".logrec', 'logrecid'))";
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(statement);
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
            rs.next();
            Integer result = rs.getInt(1);          
            rs.close();            
            pst.close();
            return result;
        } catch (SQLException e) {
            String msg = "Database error occurred while retrieving LR Field next id";
            throw DataUtilities.createDAOException(msg, e);
        }
    }

}
