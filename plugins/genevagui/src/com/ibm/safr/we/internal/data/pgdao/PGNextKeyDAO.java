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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
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
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
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
import com.ibm.safr.we.internal.data.PGSQLGenerator;

/**
 * This class is used to generate ID for a metadata component. It calls a store
 * procedure GP_INCID to generate the ID.
 */
public class PGNextKeyDAO implements NextKeyDAO {

    static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGNextKeyDAO");

	private PGSQLGenerator generator = new PGSQLGenerator();

	private static final String TABLE_NAME = "C_NEXTKEYID";
	private static final String COL_TABLE = "TABLENAME";
	private static final String COL_ID = "KEYID";

	// tablenames in NEXTKEYID table used for Import
	private static final String CONTROLREC = "CONTROLREC";
    private static final String ENVIRON = "ENVIRON";
	private static final String EXIT = "EXIT";
	private static final String LFPFASSOC = "LFPFASSOC";
	private static final String LOGFILE = "LOGFILE";
	private static final String LOGREC = "LOGREC";
	private static final String LOOKUP = "LOOKUP";
    //LOOKUPSRCKEY,
	private static final String LOOKUPSTEP = "LOOKUPSTEP";
    private static final String LRFIELD = "LRFIELD";
	private static final String LRINDEX = "LRINDEX";
	private static final String LRINDEXFLD = "LRINDEXFLD";
	private static final String LRLFASSOC = "LRLFASSOC";
	private static final String PHYFILE = "PHYFILE";
    private static final String SECUSER = "SECUSER";
    private static final String VFVASSOC = "VFVASSOC";
	private static final String VIEW = "VIEW";
	private static final String VIEWCOLUMN = "VIEWCOLUMN";
	private static final String VIEWCOLUMNSOURCE = "VIEWCOLUMNSOURCE";
	private static final String VIEWHEADERFOOTER = "VIEWHEADERFOOTER";
    private static final String VIEWFOLDER = "VIEWFOLDER";
	private static final String VIEWSORTKEYT = " VIEWSORTKEY";
	private static final String VIEWSOURCE = "VIEWSOURCE";


    private static final Map<String, String> ID_NAME_MAP = new HashMap<String, String>();
    static {
        ID_NAME_MAP.put(CONTROLREC, "CONTROLRECID");
        ID_NAME_MAP.put(ENVIRON, "ENVIRONID");
        ID_NAME_MAP.put(SECUSER, "GROUPID");
        ID_NAME_MAP.put(VIEWFOLDER, "VIEWFOLDERID");
        ID_NAME_MAP.put(LOGFILE, "LOGFILEID");
        ID_NAME_MAP.put(LRFIELD, "LRFIELDID");
        ID_NAME_MAP.put(LOOKUP, "LOOKUPID");
        ID_NAME_MAP.put(LRINDEX, "LRINDEXID");
        ID_NAME_MAP.put(LOGREC, "LOGRECID");
        ID_NAME_MAP.put(PHYFILE, "PHYFILEID");
        ID_NAME_MAP.put(EXIT, "EXITID");
        ID_NAME_MAP.put(VIEWHEADERFOOTER, "HEADERFOOTERID");
        ID_NAME_MAP.put(VIEW, "VIEWID");
        ID_NAME_MAP.put(LFPFASSOC, "LFPFASSOCID");
        ID_NAME_MAP.put(LOOKUPSTEP, "LOOKUPSTEPID");
        ID_NAME_MAP.put(LRLFASSOC, "LRLFASSOCID");
        ID_NAME_MAP.put(LRINDEXFLD, "LRINDEXFLDID");
        ID_NAME_MAP.put(VFVASSOC, "VFVASSOCID");
        ID_NAME_MAP.put(VIEWCOLUMNSOURCE, "VIEWCOLUMNSOURCEID");
        ID_NAME_MAP.put(VIEWSORTKEYT, "VIEWSORTKEYID");
        ID_NAME_MAP.put(VIEWSOURCE, "VIEWSOURCEID");
        ID_NAME_MAP.put(VIEWCOLUMN, "VIEWCOLUMNID");
    }

    private static final Map<String, Class<? extends SAFRTransfer>> TRANS_MAP = 
        new HashMap<String, Class<? extends SAFRTransfer>>();
    static {
        TRANS_MAP.put(CONTROLREC, ControlRecordTransfer.class);
        TRANS_MAP.put(ENVIRON, EnvironmentTransfer.class);
        TRANS_MAP.put(SECUSER, GroupTransfer.class);
        TRANS_MAP.put(VIEWFOLDER, ViewFolderTransfer.class);
        TRANS_MAP.put(LOGFILE, LogicalFileTransfer.class);
        TRANS_MAP.put(LOOKUP, LookupPathTransfer.class);
        TRANS_MAP.put(LRINDEX, LRIndexTransfer.class);
        TRANS_MAP.put(LOGREC, LogicalRecordTransfer.class);
        TRANS_MAP.put(PHYFILE, PhysicalFileTransfer.class);
        TRANS_MAP.put(EXIT, UserExitRoutineTransfer.class);
        TRANS_MAP.put(VIEWHEADERFOOTER, HeaderFooterItemTransfer.class);
        TRANS_MAP.put(VIEW, ViewTransfer.class);
        TRANS_MAP.put(LFPFASSOC, FileAssociationTransfer.class);
        TRANS_MAP.put(LOOKUPSTEP, LookupPathStepTransfer.class);
        TRANS_MAP.put(LRLFASSOC, ComponentAssociationTransfer.class);
        TRANS_MAP.put(LRFIELD, LRFieldTransfer.class);
        TRANS_MAP.put(LRINDEXFLD, LRIndexFieldTransfer.class);
        TRANS_MAP.put(VFVASSOC, ViewFolderViewAssociationTransfer.class);
        TRANS_MAP.put(VIEWCOLUMNSOURCE, ViewColumnSourceTransfer.class);
        TRANS_MAP.put(VIEWSORTKEYT, ViewSortKeyTransfer.class);
        TRANS_MAP.put(VIEWSOURCE, ViewSourceTransfer.class);
        TRANS_MAP.put(VIEWCOLUMN, ViewColumnTransfer.class);        
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
	public PGNextKeyDAO(Connection con, ConnectionParameters params) {
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
        String seqName = "";
        if (tableName.equals(LRFIELD)) {
            seqName = getLRFieldSeqId();
         } else {
            seqName = getTableSeqId(tableName, idColumn);
        }
        logger.info("Changing table sequence " + tableName + " for sequence name " + seqName + " setting its next key to " + nextKeyId);
        setKeySeqIdValue(seqName, nextKeyId);
    }

    // Obtain sequence name used for LRField and LRFieldAttr tables.    
    private String getLRFieldSeqId() throws SQLException{
        String result = "";
        logger.info("Get sequence name used for LRField & LRFieldAttr");
        String getSeqNameString = "SELECT sequence_name from information_schema.sequences WHERE sequence_schema = ?";
        PreparedStatement proc = null;
        ResultSet rs = null;
        while (true) {
            try {
                proc = con.prepareStatement(getSeqNameString);
                proc.setString(1, params.getSchema());
                rs = proc.executeQuery();
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
            result = rs.getString(1);
        } else {
            logger.info("No sequence name for LRField & LRFieldAttr tables ");
        }
        proc.close();
        rs.close();
        return result;
        
    }
// set sequence id values
    private void setKeySeqIdValue(String seqName,Integer nextKeyId) throws SQLException {
        String setValString = "SELECT setVal( ?, ?, true ) "; 
		PreparedStatement proc = null;

        while (true) {
            try {
                proc = con.prepareStatement(setValString);
                proc.setString(1, seqName);
                proc.setInt(2,nextKeyId);
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
    }

    private String getTableSeqId(String tableName, String idColumn) throws SQLException {
        String result = "";
        logger.info("Get table sequence name for " + tableName + " and column " + idColumn);
        String getSeqNameString = "SELECT pg_get_serial_sequence ( ?, ? )";
        PreparedStatement proc = null;
        ResultSet rs = null;
        while (true) {
            try {
                proc = con.prepareStatement(getSeqNameString);
                proc.setString(1, tableName);
                proc.setString(2, idColumn.toLowerCase());
                rs = proc.executeQuery();
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
            result = rs.getString(1);
        } else {
            logger.info("No sequence name for table : " + tableName + " and column id : " + idColumn);
        }
        proc.close();
        rs.close();
        return result;
    }

    @Override
    public Map<Class<? extends SAFRTransfer>, Integer> getNextKeyIds() {
        try {
            Map<Class<? extends SAFRTransfer>, Integer> result = 
                new HashMap<Class<? extends SAFRTransfer>, Integer>();
            
            String statement = generator.getFunction(params.getSchema(),"getMaxIds", 0);
            CallableStatement proc = null;
            ResultSet rs = null;
            while (true) {
            	try {
            		con.setAutoCommit(false);
            		proc = con.prepareCall(statement);
            		proc.registerOutParameter(1, Types.OTHER);
             		proc.execute();
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
            //This gets the content behind the cursor as a result set
            //We could use String cursorName = proc.getString(1);
            //to get the cursor name and then FETCH ... but using this for the moment
            rs = (ResultSet) proc.getObject(1);
            while (rs.next()) {
            	ResultSetMetaData rsmd = rs.getMetaData();
            	for(int c = 1; c < rsmd.getColumnCount(); c++) {
	            	String tableName = rsmd.getColumnName(c);
	            	int key = rs.getInt(c);
	                if (TRANS_MAP.containsKey(tableName.toUpperCase())) {
	                    result.put(TRANS_MAP.get(tableName.toUpperCase()), key);
	                }
            	}
            }
            rs.close();
            proc.close();
            

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
