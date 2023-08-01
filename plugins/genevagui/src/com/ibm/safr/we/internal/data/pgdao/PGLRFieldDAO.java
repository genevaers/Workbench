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
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.LRFieldDAO;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.internal.data.PGSQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LogicalRecordFieldQueryBean;

public class PGLRFieldDAO implements LRFieldDAO {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGLRFieldDAO");

    private static final String TABLE_LRFIELD = "LRFIELD";

	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "LRFIELDID";
    private static final String COL_LRID = "LOGRECID";
	private static final String COL_FIELDNAME = "NAME";
	private static final String COL_DBMSCOLNAME = "DBMSCOLNAME";
	private static final String COL_FIXEDSTARTPOS = "FIXEDSTARTPOS";
	private static final String COL_ORDINALPOS = "ORDINALPOS";
	private static final String COL_ORDINALOFFSET = "ORDINALOFFSET";
    private static final String COL_REDEFINE = "REDEFINE";
    private static final String COL_COMMENTS = "COMMENTS";
    private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
    private static final String COL_CREATEBY = "CREATEDUSERID";
    private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
    private static final String COL_MODIFYBY = "LASTMODUSERID";
	
    private static final String TABLE_LRFIELD_ATTR = "LRFIELDATTR";
    
	private static final String COL_DATATYPE = "FLDFMTCD";
	private static final String COL_SIGNEDIND = "SIGNEDIND";
	private static final String COL_MAXLEN = "MAXLEN";
    private static final String COL_DECIMALCOUNT = "DECIMALCNT";
    private static final String COL_SCALING = "ROUNDING";
    private static final String COL_DATETIMEFORMAT = "FLDCONTENTCD";
	private static final String COL_HDRJUSTIFYCD = "HDRJUSTIFYCD";
	private static final String COL_HDRLINE1 = "HDRLINE1";
	private static final String COL_HDRLINE2 = "HDRLINE2";
	private static final String COL_HDRLINE3 = "HDRLINE3";
	private static final String COL_SUBTLABEL = "SUBTLABEL";
    private static final String COL_SORTKEYLABEL = "SORTKEYLABEL";
    private static final String COL_INPUTMASK = "INPUTMASK";
    
	private String[] lfFieldNames = { 
			COL_ENVID, 
			COL_ID, 
			COL_LRID, 
	        COL_FIELDNAME, 
	        COL_DBMSCOLNAME, 
			COL_FIXEDSTARTPOS, 
			COL_ORDINALPOS, 
			COL_ORDINALOFFSET,
			COL_REDEFINE, 
			COL_COMMENTS, 
			COL_CREATETIME,
			COL_CREATEBY, 
			COL_MODIFYTIME, 
			COL_MODIFYBY };

	private String[] lrFieldAttributes = { 
			COL_ENVID, 
			COL_ID, 
			COL_DATATYPE,
			COL_SIGNEDIND, 
			COL_MAXLEN, 
			COL_DECIMALCOUNT, 
			COL_SCALING,
			COL_DATETIMEFORMAT, 
			COL_HDRJUSTIFYCD, 
			COL_HDRLINE1, 
			COL_HDRLINE2, 
			COL_HDRLINE3, 
			COL_SUBTLABEL, 
			COL_SORTKEYLABEL, 
			COL_INPUTMASK, 
			COL_CREATETIME, 
			COL_CREATEBY, 
			COL_MODIFYTIME, 
			COL_MODIFYBY };

    // INDEX FIELDS
    private static final String COL_EFFDATESTARTFLDID = "EFFDATESTARTFLDID";
    private static final String COL_EFFDATEENDFLDID = "EFFDATEENDFLDID";
    private static final String COL_FLDSEQNBR = "FLDSEQNBR";
    
	// standard data layer objects
	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private PGSQLGenerator generator = new PGSQLGenerator();

	private Integer effStartdate = 0;
	private Integer effEndDate = 0;
	private Map<Integer, Integer> pKey = new HashMap<Integer, Integer>();

	// DB constants

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
	public PGLRFieldDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrlogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrlogin;
	}

	private LRFieldTransfer generateTransfer(ResultSet rs,
			Integer effStartDate, Integer effEndDate, Map<Integer, Integer> key)
			throws SQLException {
		LRFieldTransfer lrField = new LRFieldTransfer();

		if (rs.getInt(COL_ID) == effStartDate) {
			lrField.setEffStartDate(true);
			lrField.setEffEndDate(false);
			lrField.setPkeySeqNo(0);
		} else if (rs.getInt(COL_ID) == effEndDate) {
			lrField.setEffStartDate(false);
			lrField.setEffEndDate(true);
			lrField.setPkeySeqNo(0);
		} else if (key.containsKey(rs.getInt(COL_ID))) {
			lrField.setEffStartDate(false);
			lrField.setEffEndDate(false);
			lrField.setPkeySeqNo(key.get(rs.getInt(COL_ID)));
		} else {
			lrField.setEffStartDate(false);
			lrField.setEffEndDate(false);
			lrField.setPkeySeqNo(0);
		}
		lrField.setEnvironmentId(rs.getInt(COL_ENVID));
		lrField.setLrId(rs.getInt(COL_LRID));
		lrField.setId(rs.getInt(COL_ID));
		lrField.setName(DataUtilities.trimString(rs.getString(COL_FIELDNAME)));
		lrField.setDbmsColName(DataUtilities.trimString(rs.getString(COL_DBMSCOLNAME)));
		lrField.setRedefine(rs.getInt(COL_REDEFINE));
        if (rs.wasNull()) {
            lrField.setRedefine(null);
        }
		lrField.setFixedStartPos(rs.getInt(COL_FIXEDSTARTPOS));
        if (rs.wasNull()) {
            lrField.setFixedStartPos(null);
        }
		lrField.setOrdinalPos(rs.getInt(COL_ORDINALPOS));
		if (rs.wasNull()) {
			lrField.setOrdinalPos(null);
		}
		lrField.setOrdinalOffset(rs.getInt(COL_ORDINALOFFSET));
		if (rs.wasNull()) {
			lrField.setOrdinalOffset(null);
		}
		lrField.setDataType(DataUtilities
				.trimString(rs.getString(COL_DATATYPE)));
		lrField.setSigned(DataUtilities.intToBoolean(rs.getInt(COL_SIGNEDIND)));
		lrField.setLength(rs.getInt(COL_MAXLEN));
		lrField.setDecimalPlaces(rs.getInt(COL_DECIMALCOUNT));
		lrField.setScalingFactor(rs.getInt(COL_SCALING));
		lrField.setDateTimeFormat(DataUtilities.trimString(rs.getString(COL_DATETIMEFORMAT)));
		lrField.setHeaderAlignment(DataUtilities.trimString(rs.getString(COL_HDRJUSTIFYCD)));
		lrField.setColumnHeading1(DataUtilities.trimString(rs.getString(COL_HDRLINE1)));
		lrField.setColumnHeading2(DataUtilities.trimString(rs.getString(COL_HDRLINE2)));
		lrField.setColumnHeading3(DataUtilities.trimString(rs.getString(COL_HDRLINE3)));
		lrField.setSortKeyLabel(DataUtilities.trimString(rs.getString(COL_SORTKEYLABEL)));
		lrField.setNumericMask(DataUtilities.trimString(rs.getString(COL_INPUTMASK)));
		lrField.setSubtotalLabel(DataUtilities.trimString(rs.getString(COL_SUBTLABEL)));
		lrField.setComments(DataUtilities.trimString(rs.getString(COL_COMMENTS)));

		lrField.setCreateTime(rs.getDate(COL_CREATETIME));
		lrField.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		lrField.setModifyTime(rs.getDate(COL_MODIFYTIME));
		lrField.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));
		
		return lrField;
	}

	public List<LRFieldTransfer> getLRFields(Integer environmentId,
			Integer logicalRecordId) throws DAOException {

		List<LRFieldTransfer> result = new ArrayList<LRFieldTransfer>();

		try {
			String schema = params.getSchema();

			String selectString = "Select A.ENVIRONID, A.LRFIELDID, A.LOGRECID, "
			        + "A.NAME, A.DBMSCOLNAME, A.FIXEDSTARTPOS, "
					+ "A.ORDINALPOS, A.ORDINALOFFSET, A.REDEFINE, A.COMMENTS, "
                    + "A.CREATEDTIMESTAMP, A.CREATEDUSERID, A.LASTMODTIMESTAMP, A.LASTMODUSERID, "                   
					+ "B.FLDFMTCD, B.SIGNEDIND, B.MAXLEN, B.DECIMALCNT, B.ROUNDING, "
					+ "B.FLDCONTENTCD, B.HDRJUSTIFYCD, B.HDRLINE1, B.HDRLINE2, B.HDRLINE3, "
					+ "B.SUBTLABEL, B.SORTKEYLABEL, B.INPUTMASK "
					+ "From " 
					+ schema+ ".LRFIELD A, "
					+ schema+ ".LRFIELDATTR B "
					+ "Where A.ENVIRONID = ? "
					+ " AND A.LOGRECID = ? "
					+ " AND A.ENVIRONID = B.ENVIRONID"
					+ " AND A.LRFIELDID = B.LRFIELDID "
					+ "Order By A.ORDINALPOS";

			PreparedStatement pst1 = null;
			ResultSet rs1 = null;
			while (true) {
				try {
					pst1 = con.prepareStatement(selectString);
					int i = 1;
					pst1.setInt(i++, environmentId);
					pst1.setInt(i++, logicalRecordId);
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
			String selectString1 = "Select B.EFFDATESTARTFLDID, B.EFFDATEENDFLDID, C.FLDSEQNBR,"
					+ " C.LRFIELDID FROM "
					+ schema + ".LOGREC A inner join "
					+ schema + ".LRINDEX B on A.LOGRECID = B.LOGRECID "
					+ "AND A.ENVIRONID = B.ENVIRONID left join "
					+ schema + ".LRINDEXFLD C on B.LRINDEXID = C.LRINDEXID "
					+ "AND B.ENVIRONID = C.ENVIRONID "
					+ "Where A.LOGRECID = ? "
					+ "AND A.ENVIRONID = ? ";
			PreparedStatement pst2 = null;
			ResultSet rs2 = null;
			while (true) {
				try {
					pst2 = con.prepareStatement(selectString1);
					int i2 = 1;
					pst2.setInt(i2++, logicalRecordId);
					pst2.setInt(i2++, environmentId);
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
				if (rs2.getInt(COL_EFFDATESTARTFLDID) > 0) {
					effStartdate = rs2.getInt(COL_EFFDATESTARTFLDID);
				}
				if (rs2.getInt(COL_EFFDATEENDFLDID) > 0) {
					effEndDate = rs2.getInt(COL_EFFDATEENDFLDID);
				}
				if (rs2.getInt(COL_ID) > 0) {
					pKey.put(rs2.getInt(COL_ID), rs2.getInt(COL_FLDSEQNBR));
				}
			}

			while (rs1.next()) {
				result.add(generateTransfer(rs1, effStartdate, effEndDate, pKey));
			}
			pst1.close();
			pst2.close();
			rs1.close();
			rs2.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while retrieving all LR Fields for Logical Record with id ["+ logicalRecordId + "]", e);
		}
	}

	public LRFieldTransfer getLRField(Integer environmentId, Integer lrFieldId,
			Boolean retrieveKeyInfo) throws DAOException {
		LRFieldTransfer result = null;

		try {
			String schema = params.getSchema();

			String selectString = "Select A.ENVIRONID, A.LRFIELDID, A.LOGRECID, A.NAME, A.DBMSCOLNAME, "
					+ "A.FIXEDSTARTPOS, A.ORDINALPOS, A.ORDINALOFFSET, A.REDEFINE, A.COMMENTS, "
                    + "A.CREATEDTIMESTAMP, A.CREATEDUSERID, A.LASTMODTIMESTAMP, A.LASTMODUSERID, "
					+ "B.FLDFMTCD, B.SIGNEDIND, B.MAXLEN, B.DECIMALCNT, B.ROUNDING, "
					+ "B.FLDCONTENTCD, B.HDRJUSTIFYCD, B.HDRLINE1, B.HDRLINE2, B.HDRLINE3, "
					+ "B.SUBTLABEL, B.SORTKEYLABEL, B.INPUTMASK "
					+ "From "
					+ schema + ".LRFIELD A, "
					+ schema + ".LRFIELDATTR B "
					+ "Where A.ENVIRONID = ? "
					+ " AND A.LRFIELDID = ? "
					+ " AND A.ENVIRONID = B.ENVIRONID"
					+ " AND A.LRFIELDID = B.LRFIELDID "
					+ "Order By A.ORDINALPOS";

			PreparedStatement pst1 = null;
			ResultSet rs1 = null;
			while (true) {
				try {
					pst1 = con.prepareStatement(selectString);
					int i = 1;
					pst1.setInt(i++, environmentId);
					pst1.setInt(i++, lrFieldId);
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
			Integer effStartDateId = 0;
			Integer effEndDateId = 0;
			Map<Integer, Integer> keyMap = new HashMap<Integer, Integer>();

	// TODO retrieveKeyInfo is always set to false everywhere this is used, so I
	// believe this code is dead code. This could probably be removed.
			if (retrieveKeyInfo) {
				String selectString1 = "Select B.EFFDATESTARTFLDID, B.EFFDATEENDFLDID, C.FLDSEQNBR,"
						+ " C.LRFIELDID FROM "
						+ schema + ".LOGREC A inner join "
						+ schema + ".LRINDEX B on A.LOGRECID = B.LOGRECID "
						+ "AND A.ENVIRONID = B.ENVIRONID left join "
						+ schema + ".LRINDEXFLD C on B.LRINDEXID = C.LRINDEXID "
						+ "AND B.ENVIRONID = C.ENVIRONID Where (C.LRFIELDID = ? "
						+ " OR B.EFFDATESTARTFLDID = ? "
						+ " OR B.EFFDATEENDFLDID = ? "
						+ ") AND A.ENVIRONID = ? ";

				PreparedStatement pst2 = null;
				ResultSet rs2 = null;
				while (true) {
					try {
						pst2 = con.prepareStatement(selectString1);
						int i2 = 1;
						pst2.setInt(i2++, lrFieldId);
						pst2.setInt(i2++, lrFieldId);
						pst2.setInt(i2++, lrFieldId);
						pst2.setInt(i2++, environmentId);
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
					if (rs2.getInt(COL_EFFDATESTARTFLDID) > 0) {
						effStartDateId = rs2.getInt(COL_EFFDATESTARTFLDID);
					}
					if (rs2.getInt(COL_EFFDATEENDFLDID) > 0) {
						effEndDateId = rs2.getInt(COL_EFFDATEENDFLDID);
					}
					if (rs2.getInt(COL_ID) > 0) {
						keyMap.put(rs2.getInt(COL_ID), rs2.getInt(COL_FLDSEQNBR));
					}
				}
				pst2.close();
				rs2.close();
			}
			while (rs1.next()) {
				result = generateTransfer(rs1, effStartDateId, effEndDateId, keyMap);
			}
			pst1.close();
			rs1.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the LR Field with id [" + lrFieldId + "].", e);
		}
	}

	public List<LRFieldTransfer> getLRFields(Integer environmentId,
			List<Integer> ids) throws DAOException {

		List<LRFieldTransfer> result = new ArrayList<LRFieldTransfer>();
		String sqlInParams = "";
		try {
			String schema = params.getSchema();
			sqlInParams = generator.getPlaceholders(ids.size());

			String selectString = "Select A.ENVIRONID, A.LRFIELDID, A.LOGRECID, A.NAME, A.DBMSCOLNAME, "
					+ "A.FIXEDSTARTPOS, A.ORDINALPOS, A.ORDINALOFFSET, A.REDEFINE, A.COMMENTS, "
                    + "A.CREATEDTIMESTAMP, A.CREATEDUSERID, A.LASTMODTIMESTAMP, A.LASTMODUSERID, "                                       
					+ "B.FLDFMTCD, B.SIGNEDIND, B.MAXLEN, B.DECIMALCNT, B.ROUNDING, "
					+ "B.FLDCONTENTCD, B.HDRJUSTIFYCD, B.HDRLINE1, B.HDRLINE2, B.HDRLINE3, "
					+ "B.SUBTLABEL, B.SORTKEYLABEL, B.INPUTMASK "
					+ "From "
					+ schema + ".LRFIELD A, "
					+ schema + ".LRFIELDATTR B "
					+ "Where A.ENVIRONID = ? "
					+ " AND A.ENVIRONID = B.ENVIRONID"
					+ " AND A.LRFIELDID = B.LRFIELDID";
					if (ids.size() > 0) {
					 selectString += " AND A.LRFIELDID IN ( " + sqlInParams + " )";
					}
					selectString += " Order By A.ORDINALPOS";

			PreparedStatement pst1 = null;
			ResultSet rs1 = null;
			while (true) {
				try {
					pst1 = con.prepareStatement(selectString);
					int i = 1;
					pst1.setInt(i++, environmentId);
					for (int i1 = 0; i1 < ids.size(); i1++) {
						pst1.setInt(i++, ids.get(i1));
						}
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
			String selectString1 = "Select B.EFFDATESTARTFLDID, B.EFFDATEENDFLDID, C.FLDSEQNBR,"
					+ " C.LRFIELDID FROM "
					+ schema + ".LOGREC A inner join "
					+ schema + ".LRINDEX B on A.LOGRECID = B.LOGRECID "
					+ "AND A.ENVIRONID = B.ENVIRONID left join "
					+ schema + ".LRINDEXFLD C on B.LRINDEXID = C.LRINDEXID "
					+ "AND B.ENVIRONID = C.ENVIRONID Where A.ENVIRONID = ? ";
					if (ids.size() > 0) {
						selectString1 += " And C.LRFIELDID IN ( " + sqlInParams + " )";
					}
			PreparedStatement pst2 = null;
			ResultSet rs2 = null;
			while (true) {
				try {
					pst2 = con.prepareStatement(selectString1);
					int i2 = 1;
					pst2.setInt(i2++, environmentId);
					for (int i3 = 0; i3 < ids.size(); i3++) {
						pst2.setInt(i2++, ids.get(i3));
					}
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
				if (rs2.getInt(COL_EFFDATESTARTFLDID) > 0) {
					effStartdate = rs2.getInt(COL_EFFDATESTARTFLDID);
				}
				if (rs2.getInt(COL_EFFDATEENDFLDID) > 0) {
					effEndDate = rs2.getInt(COL_EFFDATEENDFLDID);
				}
				if (rs2.getInt(COL_ID) > 0) {
					pKey.put(rs2.getInt(COL_ID), rs2.getInt(COL_FLDSEQNBR));
				}
			}

			while (rs1.next()) {
				result.add(generateTransfer(rs1, effStartdate, effEndDate,pKey));
			}
			pst1.close();
			pst2.close();
			rs1.close();
			rs2.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while retrieving LR Fields", e);
		}
	}

	public List<LRFieldTransfer> persistLRField(
			List<LRFieldTransfer> lrFieldTransfer) throws DAOException {
	    
		List<LRFieldTransfer> lrFieldCreate = new ArrayList<LRFieldTransfer>();
		List<LRFieldTransfer> lrFieldUpdate = new ArrayList<LRFieldTransfer>();
		List<LRFieldTransfer> ret = new ArrayList<LRFieldTransfer>();
		
        int countCreate = 0;
        boolean fCreProc = false;
        int countUpdate = 0;
        boolean fUpProc = false;
        for (LRFieldTransfer lrField : lrFieldTransfer) {
            if (countCreate % 500 == 0 && fCreProc) {
                lrFieldCreate = createLRFields(lrFieldCreate);
                ret.addAll(lrFieldCreate);               
                lrFieldCreate.clear();
                fCreProc = false;
            }
            if (countUpdate % 500 == 0 && fUpProc) {
                lrFieldUpdate = updateLRField(lrFieldUpdate);
                ret.addAll(lrFieldUpdate);               
                lrFieldUpdate.clear();
                fUpProc = false;                
            }
            if (!lrField.isPersistent()) {
                fCreProc = true;
                countCreate++;
                lrFieldCreate.add(lrField);
            } else {
                fUpProc = true;
                countUpdate++;
                lrFieldUpdate.add(lrField);
            }
        }
        if (lrFieldCreate.size() > 0) {
            lrFieldCreate = createLRFields(lrFieldCreate);
            ret.addAll(lrFieldCreate);               
        }
        if (lrFieldUpdate.size() > 0) {
            lrFieldUpdate = updateLRField(lrFieldUpdate);
            ret.addAll(lrFieldUpdate);               
        }		
		
		return lrFieldTransfer;
	}

    private void getXml(LRFieldTransfer srcFld, StringBuffer buf) throws SQLException, DAOException {
        buf.append("  <LOGRECID>"+ srcFld.getLrId() + "</LOGRECID>\n");
        if (srcFld.getName() != null) {
            String str = generator.handleSpecialChars(srcFld.getName());            
            buf.append("  <NAME>"+ str + "</NAME>\n");                
        }
        if (srcFld.getDbmsColName() != null) {
            buf.append("  <DBMSCOLNAME>"+ srcFld.getDbmsColName() + "</DBMSCOLNAME>\n");                                
        }
        if (srcFld.getFixedStartPos() != null) {
            buf.append("  <FIXEDSTARTPOS>"+ srcFld.getFixedStartPos() + "</FIXEDSTARTPOS>\n");
        }
        if (srcFld.getOrdinalPos() != null) { 
            buf.append("  <ORDINALPOS>"+ srcFld.getOrdinalPos() + "</ORDINALPOS>\n");
        }
        if (srcFld.getOrdinalOffset() != null) { 
            buf.append("  <ORDINALOFFSET>"+ srcFld.getOrdinalOffset() + "</ORDINALOFFSET>\n");
        }
        if (srcFld.getRedefine() != null) {
          buf.append("  <REDEFINE>"+ srcFld.getRedefine() + "</REDEFINE>\n");            
        }
        if (srcFld.getComments() == null) {
            buf.append("  <COMMENTS></COMMENTS>\n");                
        }
        else {
            String str = generator.handleSpecialChars(srcFld.getComments());
            buf.append("  <COMMENTS>"+ str + "</COMMENTS>\n");                                
        }                                        
        if (srcFld.getDataType() != null) {
            buf.append("  <FLDFMTCD>"+ srcFld.getDataType() + "</FLDFMTCD>\n");
        }
        buf.append("  <SIGNEDIND>"+ DataUtilities.booleanToInt(srcFld.isSigned()) + "</SIGNEDIND>\n");
        if (srcFld.getLength() != null) {
            buf.append("  <MAXLEN>"+ srcFld.getLength() + "</MAXLEN>\n");
        }
        buf.append("  <DECIMALCNT>"+ srcFld.getDecimalPlaces() + "</DECIMALCNT>\n");
        buf.append("  <ROUNDING>"+ srcFld.getScalingFactor() + "</ROUNDING>\n");
        if (srcFld.getDateTimeFormat() != null) {
            buf.append("  <FLDCONTENTCD>"+ srcFld.getDateTimeFormat() + "</FLDCONTENTCD>\n");
        }
        if (srcFld.getHeaderAlignment() != null) {
            buf.append("  <HDRJUSTIFYCD>" + srcFld.getHeaderAlignment() + "</HDRJUSTIFYCD>\n");
        }
        if (srcFld.getColumnHeading1() != null) {
            String str = generator.handleSpecialChars(srcFld.getColumnHeading1());                        
            buf.append("  <HDRLINE1>"+ str + "</HDRLINE1>\n");                                
        }                                        
        if (srcFld.getColumnHeading2() != null) {
            String str = generator.handleSpecialChars(srcFld.getColumnHeading2());                        
            buf.append("  <HDRLINE2>"+ str + "</HDRLINE2>\n");                                
        }                                        
        if (srcFld.getColumnHeading3() != null) {
            String str = generator.handleSpecialChars(srcFld.getColumnHeading3());                        
            buf.append("  <HDRLINE3>"+ str + "</HDRLINE3>\n");                                
        }                                        
        if (srcFld.getSubtotalLabel() != null) {
            String str = generator.handleSpecialChars(srcFld.getSubtotalLabel());                        
            buf.append("  <SUBTLABEL>"+ str + "</SUBTLABEL>\n");                                
        }                                        
        if (srcFld.getSortKeyLabel() != null) {
            String str = generator.handleSpecialChars(srcFld.getSortKeyLabel());                        
            buf.append("  <SORTKEYLABEL>"+ str + "</SORTKEYLABEL>\n");                                
        }                                        
        if (srcFld.getNumericMask() != null) {            
            buf.append("  <INPUTMASK>"+ srcFld.getNumericMask() + "</INPUTMASK>\n");
        }
    }
	
    private String getCreateXml(List<LRFieldTransfer> srcFlds) throws SQLException, DAOException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("<Root>\n");
        for (LRFieldTransfer srcFld : srcFlds) {
            buf.append(" <Record>\n");
            buf.append("  <ENVIRONID>"+ srcFld.getEnvironmentId() + "</ENVIRONID>\n");
            buf.append("  <LRFIELDID>"+ srcFld.getId() + "</LRFIELDID>\n");
            getXml(srcFld,buf);
            
            if (srcFld.isForImportOrMigration()) {
                buf.append("  <CREATEDTIMESTAMP>"+ generator.genTimeParm(srcFld.getCreateTime()) + "</CREATEDTIMESTAMP>\n");
                buf.append("  <CREATEDUSERID>"+ srcFld.getCreateBy() + "</CREATEDUSERID>\n");
                buf.append("  <LASTMODTIMESTAMP>"+ generator.genTimeParm(srcFld.getModifyTime()) + "</LASTMODTIMESTAMP>\n");
                buf.append("  <LASTMODUSERID>"+ srcFld.getModifyBy() + "</LASTMODUSERID>\n");
            }
            else {
    //        	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    //            buf.append("  <CREATEDTIMESTAMP>"+ timestamp.toString() + "</CREATEDTIMESTAMP>\n");
                buf.append("  <CREATEDUSERID>"+ srcFld.getCreateBy() + "</CREATEDUSERID>\n");
    //            buf.append("  <LASTMODTIMESTAMP>"+ timestamp.toString() + "</LASTMODTIMESTAMP>\n");
                buf.append("  <LASTMODUSERID>"+ srcFld.getModifyBy() + "</LASTMODUSERID>\n");
            }            
            buf.append(" </Record>\n");            
        }
        buf.append("</Root>");
        return buf.toString();
    }	

    private String getUpdateXml(List<LRFieldTransfer> srcFlds) throws SQLException, DAOException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("<Root>\n");
        for (LRFieldTransfer srcFld : srcFlds) {
            buf.append(" <Record>\n");
            buf.append("  <ENVIRONID>"+ srcFld.getEnvironmentId() + "</ENVIRONID>\n");
            buf.append("  <LRFIELDID>"+ srcFld.getId() + "</LRFIELDID>\n");
            getXml(srcFld,buf);
            if (srcFld.isForImportOrMigration()) {
                buf.append("  <CREATEDTIMESTAMP>"+ generator.genTimeParm(srcFld.getCreateTime()) + "</CREATEDTIMESTAMP>\n");
                buf.append("  <CREATEDUSERID>"+ srcFld.getCreateBy() + "</CREATEDUSERID>\n");
                buf.append("  <LASTMODTIMESTAMP>"+ generator.genTimeParm(srcFld.getModifyTime()) + "</LASTMODTIMESTAMP>\n");
                buf.append("  <LASTMODUSERID>"+ srcFld.getModifyBy() + "</LASTMODUSERID>\n");
            }
            else {
                buf.append("  <LASTMODUSERID>"+ safrLogin.getUserId() + "</LASTMODUSERID>\n");
            }            
            buf.append(" </Record>\n");            
        }
        buf.append("</Root>");
        return buf.toString();
    }   
    
	private List<LRFieldTransfer> createLRFields(
			List<LRFieldTransfer> lrFieldCreateList) throws DAOException {
		try {
    
//			String runFunction = "select :schemaV.INSLRFIELD(?, ?)";
			String runFunction = "select " + params.getSchema() + ".INSLRFIELDWITHID(?)";
			if (lrFieldCreateList.isEmpty() || !lrFieldCreateList.get(0).isForImportOrMigration()) {
				runFunction = "select " + params.getSchema() + ".INSLRFIELD(?)";
			}		

			while (true) {
				try {
		            PreparedStatement cs = con.prepareStatement(runFunction, Statement.RETURN_GENERATED_KEYS);
					String xml = getCreateXml(lrFieldCreateList);
					cs.setString(1, xml);
		            ResultSet rs = cs.executeQuery();
		            rs.close();
		            cs.close();
                    for (LRFieldTransfer fld : lrFieldCreateList) {
                        fld.setPersistent(true);
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
			throw DataUtilities.createDAOException(
					"Database error occurred while creating LR Fields.", e);
		}
		
		return lrFieldCreateList;
	}

    private List<LRFieldTransfer> updateLRField(
            List<LRFieldTransfer> lrFieldUpdate) throws DAOException {
        try {       
			String statement = generator.getSelectFromFunction(params.getSchema(), "updatelrfieldwithid", 1);     
			if (lrFieldUpdate.isEmpty() || !lrFieldUpdate.get(0).isForImportOrMigration()) {
	            statement = generator.getSelectFromFunction(params.getSchema(), "updatelrfield", 1);
			}

            PreparedStatement stmnt = null;
            
            while (true) {
                try {
                    stmnt = con.prepareStatement(statement);
                    String xml = getUpdateXml(lrFieldUpdate);
                    stmnt.setString(1, xml);
                    stmnt.executeQuery();
                    for (LRFieldTransfer fld : lrFieldUpdate) {
                        fld.setPersistent(true);
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
            stmnt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw DataUtilities.createDAOException(
                    "Database error occurred while updating LR Fields.", e);
        }
        
        return lrFieldUpdate;        
    }
    
	public LRFieldTransfer persistLRField(LRFieldTransfer lrFieldTransfer)
			throws DAOException {
		if (lrFieldTransfer.getId() == 0) {
			return (createLRField(lrFieldTransfer));
		} else {
			return null;
		}
	}

	private LRFieldTransfer createLRField(LRFieldTransfer lrFieldTransfer)
			throws DAOException {
		try {
			List<String> names = Arrays.asList(lfFieldNames);
			String statement = generator.getInsertStatementNoIdentifier(params.getSchema(),
					TABLE_LRFIELD, names, lrFieldTransfer.isForImportOrMigration() == false);
			PreparedStatement pst = null;
			int i = 1;
			while (true) {
				try {
					pst = con.prepareStatement(statement);

					i = 1;
					pst.setInt(i++, lrFieldTransfer.getEnvironmentId());
					pst.setInt(i++, lrFieldTransfer.getId());
					pst.setInt(i++, lrFieldTransfer.getLrId());
					pst.setString(i++, lrFieldTransfer.getName());
                    pst.setString(i++, lrFieldTransfer.getDbmsColName());
					pst.setInt(i++, lrFieldTransfer.getFixedStartPos());
					pst.setInt(i++, lrFieldTransfer.getOrdinalPos());
					pst.setInt(i++, lrFieldTransfer.getOrdinalOffset());
                    pst.setInt(i++, lrFieldTransfer.getRedefine());
					pst.setString(i++, lrFieldTransfer.getComments());
					if (lrFieldTransfer.isForImportOrMigration()) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lrFieldTransfer.getCreateTime()));
					}
					pst.setString(i++,lrFieldTransfer.isForImportOrMigration() ? lrFieldTransfer.getCreateBy() : safrLogin.getUserId());
					if (lrFieldTransfer.isForImportOrMigration()) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lrFieldTransfer.getModifyTime()));
					}
					pst.setString(i++,lrFieldTransfer.isForImportOrMigration() ? lrFieldTransfer.getModifyBy() : safrLogin.getUserId());
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

			List<String> attrnames = Arrays.asList(lrFieldAttributes);
			String statement1 = generator.getInsertStatementNoIdentifier(
					params.getSchema(), TABLE_LRFIELD_ATTR, attrnames,
					!lrFieldTransfer.isForImportOrMigration());
			PreparedStatement pst1 = null;

			while (true) {
				try {

					pst1 = con.prepareStatement(statement1);

					i = 1;
					pst1.setInt(i++, lrFieldTransfer.getEnvironmentId());
					pst1.setInt(i++, lrFieldTransfer.getId());
					pst1.setString(i++, lrFieldTransfer.getDataType());
					pst1.setInt(i++, DataUtilities.booleanToInt(lrFieldTransfer.isSigned()));
					pst1.setInt(i++, lrFieldTransfer.getLength());
					pst1.setInt(i++, lrFieldTransfer.getDecimalPlaces());
					pst1.setInt(i++, lrFieldTransfer.getScalingFactor());
					if (lrFieldTransfer.getDateTimeFormat() == null) {
					    pst1.setNull(i++, Types.CHAR);
					} else {
					    pst1.setString(i++, lrFieldTransfer.getDateTimeFormat());
					}
					pst1.setString(i++, lrFieldTransfer.getHeaderAlignment());
					pst1.setString(i++, lrFieldTransfer.getColumnHeading1());
					pst1.setString(i++, lrFieldTransfer.getColumnHeading2());
					pst1.setString(i++, lrFieldTransfer.getColumnHeading3());
					pst1.setString(i++, lrFieldTransfer.getSubtotalLabel());
					pst1.setString(i++, lrFieldTransfer.getSortKeyLabel());
                    pst1.setString(i++, lrFieldTransfer.getNumericMask());
					if (lrFieldTransfer.isForImportOrMigration()) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lrFieldTransfer.getCreateTime()));
					}
					pst.setString(i++,lrFieldTransfer.isForImportOrMigration() ? lrFieldTransfer.getCreateBy() : safrLogin.getUserId());
					if (lrFieldTransfer.isForImportOrMigration()) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lrFieldTransfer.getModifyTime()));
					}
					pst.setString(i++,lrFieldTransfer.isForImportOrMigration() ? lrFieldTransfer.getModifyBy() : safrLogin.getUserId());
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
			e.printStackTrace();
			throw DataUtilities.createDAOException("Database error occurred while creating a new LR Field.",e);
		}
		return lrFieldTransfer;
	}



	public void removeLRField(List<Integer> fieldIds, Integer environmentId)
			throws DAOException {
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			idNames.add(COL_ENVID);

			String statement = generator.getDeleteStatement(params.getSchema(), TABLE_LRFIELD, idNames);

			String statement1 = generator.getDeleteStatement(params.getSchema(), TABLE_LRFIELD_ATTR, idNames);

			String statement2 = generator.getDeleteStatement(params.getSchema(), "LRINDEXFLD", idNames);

			PreparedStatement pst = null;
			PreparedStatement pst1 = null;
			PreparedStatement pst2 = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					pst1 = con.prepareStatement(statement1);
					pst2 = con.prepareStatement(statement2);

					for (Integer fieldId : fieldIds) {
						pst2.setInt(1, fieldId);
						pst2.setInt(2, environmentId);
						pst2.execute();

						pst1.setInt(1, fieldId);
						pst1.setInt(2, environmentId);
						pst1.execute();

						pst.setInt(1, fieldId);
						pst.setInt(2, environmentId);
						pst.execute();
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
			pst1.close();
			pst2.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException( "Database error occurred while deleting the LR Fields.", e);
		}
	}

	public List<LogicalRecordFieldQueryBean> queryAllLogicalRecordFields(
			SortType sortType, Integer environmentId, Integer logicalRecordId)
			throws DAOException {
		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "LRFIELDID";
		} else {
			orderString = "UPPER(NAME)";
		}
		try {
			String schema = params.getSchema();
			String selectString = "Select ENVIRONID, LRFIELDID, LOGRECID, NAME ,"
					+ COL_CREATETIME + "," + COL_CREATEBY + ","
					+ COL_MODIFYTIME + "," + COL_MODIFYBY + " From " + schema
					+ ".LRFIELD " + " Where ENVIRONID = ? "
					+ " AND LOGRECID = ? " + " Order By " + orderString;
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {

					pst = con.prepareStatement(selectString);
					int i = 1;
					pst.setInt(i++, environmentId);
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
			List<LogicalRecordFieldQueryBean> result = generateQueryBeanList(
					rs, environmentId);
			pst.close();
			rs.close();
			return result;
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving all LR Fields for specific LR.",e);
		}
	}

	private List<LogicalRecordFieldQueryBean> generateQueryBeanList(
			ResultSet rs, Integer environmentId) throws SQLException {
		List<LogicalRecordFieldQueryBean> result = new ArrayList<LogicalRecordFieldQueryBean>();
		while (rs.next()) {
			LogicalRecordFieldQueryBean lFieldQueryBean = new LogicalRecordFieldQueryBean(
					environmentId, rs.getInt("LRFIELDID"), rs.getInt("LOGRECID"),
					DataUtilities.trimString(rs.getString("NAME")), rs
							.getDate(COL_CREATETIME), DataUtilities
							.trimString(rs.getString(COL_CREATEBY)), rs
							.getDate(COL_MODIFYTIME), DataUtilities
							.trimString(rs.getString(COL_MODIFYBY)));
			result.add(lFieldQueryBean);
		}
		return result;
	}

	public List<LogicalRecordFieldQueryBean> queryAllLogicalRecordFields(
			SortType sortType, Integer environmentId) throws DAOException {
		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "LRFIELDID";
		} else {
			orderString = "UPPER(NAME)";
		}
		try {
			String schema = params.getSchema();
			String selectString = "Select ENVIRONID, LRFIELDID, LOGRECID, NAME ,"
					+ COL_CREATETIME + "," + COL_CREATEBY + ","
					+ COL_MODIFYTIME + "," + COL_MODIFYBY + " From " + schema
					+ ".LRFIELD " + " Where ENVIRONID = ? "
					+ " Order By " + orderString;

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int i = 1;
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
			List<LogicalRecordFieldQueryBean> result = generateQueryBeanList(
					rs, environmentId);
			pst.close();
			rs.close();
			return result;
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving all LR Fields.",e);
		}
	}

	public List<LogicalRecordFieldQueryBean> queryLRFields(SortType sortType,
			Integer environmentId) throws DAOException {
		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "LRFIELDID";
		} else {
			orderString = "UPPER(NAME)";
		}
		try {
			String schema = params.getSchema();
			String selectString = "Select ENVIRONID, LRFIELDID, LOGRECID, NAME ,"
					+ COL_CREATETIME + "," + COL_CREATEBY + ","
					+ COL_MODIFYTIME + "," + COL_MODIFYBY + " From " + schema
					+ ".LRFIELD " + " Where ENVIRONID = ? "
					+ " Order By " + orderString;

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int i = 1;
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
			List<LogicalRecordFieldQueryBean> result = generateQueryBeanList(
					rs, environmentId);
			pst.close();
			rs.close();
			return result;
		} catch (SQLException e) {
			String msg = "Database error occurred while retrieving LR Fields from Environment [" + environmentId + "].";
			throw DataUtilities.createDAOException(msg, e);
		}
	}

    public Integer getNextKey() {
        try {
            String statement = "SELECT nextval('" + params.getSchema() + 
                    ".lrfield_id')";
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

	@Override
	public Integer getLRField(Integer environID, Integer sourceLRID,
			String fieldName) {
		try {
			Integer result = null;
			String schema = params.getSchema();

			String selectString = "Select LRFIELDID FROM "
					+ schema + ".lrfield f where f.environid= ? " 
					+ " and f.logrecid= ? " 
					+ " and f.name = ?"; 
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, environID);
					pst.setInt(2, sourceLRID);
					pst.setString(3, fieldName);
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
				result = rs.getInt(1);
			}

			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the LR Field " + fieldName, e);
		}
	}

	@Override
	public Integer getLookupLRField(Integer environID, int lookupid,
			String targetFieldName) {
		try {
			Integer result = null;
			String schema = params.getSchema();

			String selectString = "select lrfieldid from " + schema + ".lrfield f"
					+ " join " +schema + ".lrlfassoc a on a.environid = f.environid and f.logrecid = a.logrecid"
					+ " join " + schema +".lookup l on l.environid = a.environid and l.destlrlfassocid = a.lrlfassocid"
					+ " where l.lookupid = ? and f.name = ? and f.environid = ?";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, lookupid);
					pst.setString(2, targetFieldName);
					pst.setInt(3, environID);
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
				result = rs.getInt(1);
			}

			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the LR Field " + targetFieldName + " for lookup id " + lookupid, e);
		}
	}

	@Override
	public Map<String, Integer> getFields(int lrid, Integer environID) {
		try {
			Map<String, Integer> fields = new TreeMap<>();
			String schema = params.getSchema();
			String selectString = "Select NAME , LRFIELDID "
					+ " From " + schema+ ".LRFIELD " 
					+ " Where lrfield.environid = ? AND lrfield.logrecid = ? ";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int i = 1;
					pst.setInt(i++, environID);
					pst.setInt(i++, lrid);
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
				fields.put(rs.getString(1), rs.getInt(2));
			}
			pst.close();
			rs.close();
			return fields;
		} catch (SQLException e) {
			String msg = "Database error occurred while retrieving LR " + lrid + " fields from Environment [" + environID + "].";
			throw DataUtilities.createDAOException(msg, e);
		}
	}
}
