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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.dao.NextKeyDAO;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.ControlRecordTransfer;
import com.ibm.safr.we.data.transfer.EnvironmentTransfer;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.GroupTransfer;
import com.ibm.safr.we.data.transfer.HeaderFooterItemTransfer;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexTransfer;
import com.ibm.safr.we.data.transfer.LogicalFileTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.LookupPathStepTransfer;
import com.ibm.safr.we.data.transfer.LookupPathTransfer;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;

/**
 * This class is used to generate ID for a metadata component. It calls a store
 * procedure GP_INCID to generate the ID.
 */
public class DB2NextKeyDAO implements NextKeyDAO {

    static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.DB2NextKeyDAO");

	private static final String TABLE_NAME = "C_NEXTKEYID";
	private static final String COL_TABLE = "TABLENAME";
	private static final String COL_ID = "KEYID";

	// tablenames in NEXTKEYID table used for Import
	private static final String C_CONTROLREC = "C_CONTROLREC";
    private static final String C_ENVIRONTBL = "C_ENVIRONTBL";
    private static final String C_SECGROUPS = "C_SECGROUPS";
    private static final String C_VIEWFOLDERS = "C_VIEWFOLDERS";
	private static final String E_FILETBL = "E_FILETBL";
    private static final String E_FLDTBL = "E_FLDTBL";
	private static final String E_JOINTBL = "E_JOINTBL";
	private static final String E_LRINDEXTBL = "E_LRINDEXTBL";
	private static final String E_LRTBL = "E_LRTBL";
	private static final String E_PARTITIONTBL = "E_PARTITIONTBL";
	private static final String E_PROGRAMTBL = "E_PROGRAMTBL";
	private static final String E_VIEWHEADERFOOTER = "E_VIEWHEADERFOOTER";
	private static final String E_VIEWTBL = "E_VIEWTBL";
	private static final String X_FILEPARTITIONTBL = "X_FILEPARTITIONTBL";
	private static final String X_JOINTARGETLRTBL = "X_JOINTARGETLRTBL";
	private static final String X_LRFILETBL = "X_LRFILETBL";
	private static final String X_LRFLDTBL = "X_LRFLDTBL";
	private static final String X_LRINDEXFLDTBL = "X_LRINDEXFLDTBL";
    private static final String X_VFVTBL = "X_VFVTBL";
	private static final String X_VIEWLRFLD_LRFLD = "X_VIEWLRFLD_LRFLD";
	private static final String X_VIEWSORTKEYTBL = "X_VIEWSORTKEYTBL";
	private static final String X_VIEWSRCLRFILETBL = "X_VIEWSRCLRFILETBL";
	private static final String XOV_VIEWLRFLDATTR = "XOV_VIEWLRFLDATTR";

    private static final Map<String, String> ID_NAME_MAP = new HashMap<String, String>();
    static {
        ID_NAME_MAP.put(C_CONTROLREC, "CONTROLID");
        ID_NAME_MAP.put(C_ENVIRONTBL, "ENVIRONID");
        ID_NAME_MAP.put(C_SECGROUPS, "SECGROUPID");
        ID_NAME_MAP.put(C_VIEWFOLDERS, "VIEWFOLDERID");
        ID_NAME_MAP.put(E_FILETBL, "FILEID");
        ID_NAME_MAP.put(E_FLDTBL, "FLDID");
        ID_NAME_MAP.put(E_JOINTBL, "JOINID");
        ID_NAME_MAP.put(E_LRINDEXTBL, "LRINDEXID");
        ID_NAME_MAP.put(E_LRTBL, "LRID");
        ID_NAME_MAP.put(E_PARTITIONTBL, "PARTITIONID");
        ID_NAME_MAP.put(E_PROGRAMTBL, "PROGRAMID");
        ID_NAME_MAP.put(E_VIEWHEADERFOOTER, "HEADERFOOTERID");
        ID_NAME_MAP.put(E_VIEWTBL, "VIEWID");
        ID_NAME_MAP.put(X_FILEPARTITIONTBL, "XFILEPARTITIONID");
        ID_NAME_MAP.put(X_JOINTARGETLRTBL, "XJOINSTEPID");
        ID_NAME_MAP.put(X_LRFILETBL, "XLRFILEID");
        ID_NAME_MAP.put(X_LRFLDTBL, "XLRFLDID");
        ID_NAME_MAP.put(X_LRINDEXFLDTBL, "XLRINDEXFLDID");
        ID_NAME_MAP.put(X_VFVTBL, "XVFVID");
        ID_NAME_MAP.put(X_VIEWLRFLD_LRFLD, "XVIEWLRFLDLRFLDID");
        ID_NAME_MAP.put(X_VIEWSORTKEYTBL, "XVIEWSORTKEYID");
        ID_NAME_MAP.put(X_VIEWSRCLRFILETBL, "XVIEWSRCLRFILEID");
        ID_NAME_MAP.put(XOV_VIEWLRFLDATTR, "XVIEWLRFLDID");
    }

    private static final Map<String, Class<? extends SAFRTransfer>> TRANS_MAP = 
        new HashMap<String, Class<? extends SAFRTransfer>>();
    static {
        TRANS_MAP.put(C_CONTROLREC, ControlRecordTransfer.class);
        TRANS_MAP.put(C_ENVIRONTBL, EnvironmentTransfer.class);
        TRANS_MAP.put(C_SECGROUPS, GroupTransfer.class);
        TRANS_MAP.put(C_VIEWFOLDERS, ViewFolderTransfer.class);
        TRANS_MAP.put(E_FILETBL, LogicalFileTransfer.class);
        TRANS_MAP.put(E_JOINTBL, LookupPathTransfer.class);
        TRANS_MAP.put(E_LRINDEXTBL, LRIndexTransfer.class);
        TRANS_MAP.put(E_LRTBL, LogicalRecordTransfer.class);
        TRANS_MAP.put(E_PARTITIONTBL, PhysicalFileTransfer.class);
        TRANS_MAP.put(E_PROGRAMTBL, UserExitRoutineTransfer.class);
        TRANS_MAP.put(E_VIEWHEADERFOOTER, HeaderFooterItemTransfer.class);
        TRANS_MAP.put(E_VIEWTBL, ViewTransfer.class);
        TRANS_MAP.put(X_FILEPARTITIONTBL, FileAssociationTransfer.class);
        TRANS_MAP.put(X_JOINTARGETLRTBL, LookupPathStepTransfer.class);
        TRANS_MAP.put(X_LRFILETBL, ComponentAssociationTransfer.class);
        TRANS_MAP.put(X_LRFLDTBL, LRFieldTransfer.class);
        TRANS_MAP.put(X_LRINDEXFLDTBL, LRIndexFieldTransfer.class);
        TRANS_MAP.put(X_VFVTBL, ViewFolderViewAssociationTransfer.class);
        TRANS_MAP.put(X_VIEWLRFLD_LRFLD, ViewColumnSourceTransfer.class);
        TRANS_MAP.put(X_VIEWSORTKEYTBL, ViewSortKeyTransfer.class);
        TRANS_MAP.put(X_VIEWSRCLRFILETBL, ViewSourceTransfer.class);
        TRANS_MAP.put(XOV_VIEWLRFLDATTR, ViewColumnTransfer.class);        
    }
    
	private Connection con;
	private ConnectionParameters params;

	/**
	 * Constructor for this class.
	 * 
	 * @param con
	 *            : The connection set for database access.
	 * @param params
	 *            : The connection parameters which define the URL, userid and
	 *            other details of the connection.
	 */
	public DB2NextKeyDAO(Connection con, ConnectionParameters params) {
		this.con = con;
		this.params = params;

	}

    @Override
    public void convertKeyIds() {
        // get key id map
        try {
            String selectString = "SELECT UPPER(" + COL_TABLE + "), " + COL_ID 
                + " FROM  " + params.getSchema() + "." + TABLE_NAME;
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
            Map<String, Integer> nextKeyMap = new HashMap<String, Integer>();
            while (rs.next()) {
                String tableName = DataUtilities.trimString(rs.getString(1));
                Integer nextKeyId = rs.getInt(2);
                nextKeyMap.put(tableName, nextKeyId);
            }
            rs.close();
            pst.close();
            
            // use map to convert ID's
            for (Entry<String, String> entry : ID_NAME_MAP.entrySet()) {
                logger.info("Converting next key for " + entry.getKey());
                if (nextKeyMap.containsKey(entry.getKey())) {
                    convertKeyId(entry.getKey(),entry.getValue(),nextKeyMap.get(entry.getKey())+1);
                } else {
                    convertKeyId(entry.getKey(),entry.getValue(),1);
                }
            }
            
        } catch (SQLException e) {
            String msg = "Database error occurred while querying next key IDs.";
            throw DataUtilities.createDAOException(msg, e);
        }
    }
    private void convertKeyId(String tableName, String idColumn, Integer nextKeyId) throws SQLException {
        if (tableName.equals(X_LRFLDTBL)) {
            convertLRFieldId(nextKeyId);
                        
        } else {
            convertKeyIdOther(tableName, idColumn, nextKeyId);            
        }
    }

    protected void convertLRFieldId(Integer nextKeyId) throws SQLException {
        // convert the lrfield sequence
        logger.info("Converting next key for " + X_LRFLDTBL + " to " + nextKeyId);
        String alterString = "ALTER SEQUENCE " + params.getSchema() + "." + "LRFIELD " + 
            " RESTART WITH " + nextKeyId; 
        PreparedStatement pst = null;
        while (true) {
            try {
                pst = con.prepareStatement(alterString);  //NOSONAR
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
    }
    
    private void convertKeyIdOther(String tableName, String idColumn, Integer nextKeyId) throws SQLException {
        logger.info("Changing table sequence " + tableName + " with id column " + idColumn + " setting its next key to " + nextKeyId);
        String alterString = "ALTER TABLE " + params.getSchema() + "." + tableName +
            " ALTER COLUMN " + idColumn + " RESTART WITH " + nextKeyId; 
        PreparedStatement pst = null;
        while (true) {
            try {
                pst = con.prepareStatement(alterString); //NOSONAR
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
    }

    @Override
    public Map<Class<? extends SAFRTransfer>, Integer> getNextKeyIds() {
        try {
            Map<Class<? extends SAFRTransfer>, Integer> result = 
                new HashMap<Class<? extends SAFRTransfer>, Integer>();
            
            String selectString = "SELECT DNAME,RESTARTWITH,MAXASSIGNEDVAL " + 
                "FROM SYSIBM.SYSSEQUENCESDEP A, SYSIBM.SYSSEQUENCES B " +
                "WHERE A.BSEQUENCEID=B.SEQUENCEID " +
                "AND DCREATOR='" + params.getSchema().toUpperCase() + "'";
            PreparedStatement pst = null;
            ResultSet rs;
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
                String tableName = rs.getString(1);
                int nextKey;
                int restart = rs.getInt(2);
                int max = rs.getInt(3);
                if (max == 0) {
                    nextKey = restart;
                } else {
                    nextKey = max;
                }
                if (TRANS_MAP.containsKey(tableName)) {
                    result.put(TRANS_MAP.get(tableName), nextKey);
                }
            }
            rs.close();
            pst.close();
            
            // get the LRField sequence
            selectString = "SELECT RESTARTWITH,MAXASSIGNEDVAL FROM SYSIBM.SYSSEQUENCES " +
                "WHERE SCHEMA='" + params.getSchema().toUpperCase() + "' AND NAME='LRFIELD'";
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    rs = pst.executeQuery();
                    rs.next();
                    int lrfNext;
                    int restart = rs.getInt(1);
                    int max = rs.getInt(2);
                    if (max == 0) {
                        lrfNext = restart;
                    } else {
                        lrfNext = max;
                    }
                    
                    rs.close();
                    result.put(TRANS_MAP.get(X_LRFLDTBL), lrfNext);
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
            
            return result;
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while getting all next key ids.", e);            
        }
    }

    @Override
    public void setNextKeyId(Class<? extends SAFRTransfer> transferClass, Integer nextKeyId) {
        
        // find the table involved
        String tableName = null;
        // find transfer class
        for (Entry<String, Class<? extends SAFRTransfer>> ent : TRANS_MAP.entrySet()) {
            if (ent.getValue().equals(transferClass)) {
                tableName = ent.getKey();
                break;
            }
        }
        if (tableName == null) {
            throw DataUtilities.createDAOException(
                "Transfer class doesn't have a tablename.", null);            
        }
        
        try {
            convertKeyId(tableName, ID_NAME_MAP.get(tableName), nextKeyId);
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while setting next key id.", e);            
        }
    }
	
}
