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
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.ViewSortKeyDAO;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;
import com.ibm.safr.we.internal.data.SQLGenerator;

public class DB2ViewSortKeyDAO implements ViewSortKeyDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.DB2ViewSortKeyDAO");

    private static final String TABLE_NAME = "VIEWSORTKEY";

	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "VIEWSORTKEYID";
	private static final String COL_VIEWID = "VIEWID";
    private static final String COL_VIEWCOLUMNID = "VIEWCOLUMNID";
    private static final String COL_KEYSEQNBR = "KEYSEQNBR";
    private static final String COL_SORTSEQCD = "SORTSEQCD";
    private static final String COL_SORTBRKIND = "SORTBRKIND";
    private static final String COL_PAGEBRKIND = "PAGEBRKIND";
    private static final String COL_SORTKEYDISPLAYCD = "SORTKEYDISPLAYCD";
    private static final String COL_SORTKEYLABEL = "SORTKEYLABEL";
    private static final String COL_SKFLDFMTCD = "SKFLDFMTCD";
    private static final String COL_SKSIGNED = "SKSIGNED";
    private static final String COL_SKSTARTPOS = "SKSTARTPOS";
    private static final String COL_SKFLDLEN = "SKFLDLEN";
    private static final String COL_SKDECIMALCNT = "SKDECIMALCNT";
    private static final String COL_SKFLDCONTENTCD = "SKFLDCONTENTCD";
    private static final String COL_SORTTITLELRFIELDID = "SORTTITLELRFIELDID";
    private static final String COL_SORTTITLELENGTH = "SORTTITLELENGTH";

	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";

    private SQLGenerator generator = new SQLGenerator();	
	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;

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
	public DB2ViewSortKeyDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	private ViewSortKeyTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		ViewSortKeyTransfer vskTransfer = new ViewSortKeyTransfer();
		vskTransfer.setEnvironmentId(rs.getInt(COL_ENVID));
		vskTransfer.setId(rs.getInt(COL_ID));
		vskTransfer.setViewId(rs.getInt(COL_VIEWID));
		vskTransfer.setViewColumnId(rs.getInt(COL_VIEWCOLUMNID));
		vskTransfer.setKeySequenceNo(rs.getInt(COL_KEYSEQNBR));
		vskTransfer.setSortSequenceCode(DataUtilities.trimString(rs.getString(COL_SORTSEQCD)));
		vskTransfer.setFooterOptionCode(rs.getInt(COL_SORTBRKIND));
		vskTransfer.setHeaderOptionCode(rs.getInt(COL_PAGEBRKIND));
		vskTransfer.setSortkeyLabel(DataUtilities.trimString(rs.getString(COL_SORTKEYLABEL)));
		vskTransfer.setDisplayModeCode(DataUtilities.trimString(rs.getString(COL_SORTKEYDISPLAYCD)));
		vskTransfer.setDataTypeCode(DataUtilities.trimString(rs.getString(COL_SKFLDFMTCD)));
		vskTransfer.setSigned(DataUtilities.intToBoolean(rs.getInt(COL_SKSIGNED)));
		vskTransfer.setStartPosition(rs.getInt(COL_SKSTARTPOS));
		vskTransfer.setLength(rs.getInt(COL_SKFLDLEN));
		vskTransfer.setDecimalPlaces(rs.getInt(COL_SKDECIMALCNT));
		vskTransfer.setDateTimeFormatCode(DataUtilities.trimString(rs.getString(COL_SKFLDCONTENTCD)));
		vskTransfer.setTitleFieldId(rs.getInt(COL_SORTTITLELRFIELDID));
		vskTransfer.setTitleLength(rs.getInt(COL_SORTTITLELENGTH));

		vskTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
		vskTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		vskTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
		vskTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

		return vskTransfer;
	}

	public List<ViewSortKeyTransfer> getViewSortKeys(Integer viewId,
			Integer environmentId) throws DAOException {
		List<ViewSortKeyTransfer> vstTransferList = new ArrayList<ViewSortKeyTransfer>();
		try {
			String selectString = "SELECT A.ENVIRONID,A.VIEWSORTKEYID,A.VIEWID,A.VIEWCOLUMNID,"
					+ "A.KEYSEQNBR,A.SORTSEQCD,A.SORTBRKIND,A.PAGEBRKIND,"
					+ "A.SORTKEYDISPLAYCD,A.SORTKEYLABEL,A.SKFLDFMTCD,"
					+ "A.SKSIGNED,A.SKSTARTPOS,A.SKFLDLEN,A.SKDECIMALCNT,"
					+ "A.SKFLDCONTENTCD,A.SORTTITLELRFIELDID, A.SORTTITLELENGTH, "
					+ "A.CREATEDTIMESTAMP, A.LASTMODTIMESTAMP, "
					+ "A.CREATEDUSERID, A.LASTMODUSERID FROM "
					+ params.getSchema()+ "." + TABLE_NAME + " A INNER JOIN "
					+ params.getSchema() + ".VIEWCOLUMN B ON A.ENVIRONID = B.ENVIRONID AND "
					+ "A.VIEWID = B.VIEWID AND A.VIEWCOLUMNID = B.VIEWCOLUMNID "
					+ " WHERE ";
			if(viewId > 0) {
				selectString  += "A.VIEWID = ? AND "; 
			}
			selectString  +=  "A.ENVIRONID = ? ORDER BY A.KEYSEQNBR";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					if(viewId > 0) {
						pst.setInt(ndx++, viewId);
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
				ViewSortKeyTransfer vstTransfer = new ViewSortKeyTransfer();
				vstTransfer = generateTransfer(rs);
				vstTransferList.add(vstTransfer);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving all View Sort Keys for the View with id ["+ viewId + "]", e);
		}
		return vstTransferList;
	}

	public void persistViewSortKeys(List<ViewSortKeyTransfer> vskTransferList) throws DAOException {
		if (vskTransferList == null || vskTransferList.isEmpty()) {
			return;
		}
		List<ViewSortKeyTransfer> viewSortKeyCreateList = new ArrayList<ViewSortKeyTransfer>();
		List<ViewSortKeyTransfer> viewSortKeyUpdateList = new ArrayList<ViewSortKeyTransfer>();

		for (ViewSortKeyTransfer vskTrans : vskTransferList) {
			if (!vskTrans.isPersistent()) {
				viewSortKeyCreateList.add(vskTrans);
			} else {
				viewSortKeyUpdateList.add(vskTrans);
			}
		}
		if (viewSortKeyCreateList.size() > 0) {
			createViewSortKeys(viewSortKeyCreateList);
		}
		if (viewSortKeyUpdateList.size() > 0) {
			updateViewViewSortKeys(viewSortKeyUpdateList);
		}
	}

	private void createViewSortKeys(List<ViewSortKeyTransfer> viewSortKeyCreateList)
			throws DAOException {

		boolean isImportOrMigrate = viewSortKeyCreateList.get(0).isForImport()
		|| viewSortKeyCreateList.get(0).isForMigration() ? true : false;

		try {
            String[] columnNames = { COL_ENVID, COL_VIEWID, COL_VIEWCOLUMNID, COL_KEYSEQNBR, 
                COL_SORTSEQCD, COL_SORTBRKIND, COL_PAGEBRKIND, COL_SORTKEYDISPLAYCD, COL_SORTKEYLABEL,
                COL_SKFLDFMTCD, COL_SKSIGNED, COL_SKSTARTPOS, COL_SKFLDLEN, COL_SKDECIMALCNT,
                COL_SKFLDCONTENTCD, COL_SORTTITLELRFIELDID, COL_SORTTITLELENGTH, 
                COL_CREATETIME, COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
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
					for (ViewSortKeyTransfer vskTrans : viewSortKeyCreateList) {
						int i = 1;
                        pst.setInt(i++, vskTrans.getEnvironmentId());
                        if (isImportOrMigrate) {
                            pst.setInt(i++, vskTrans.getId());
                        }
						pst.setInt(i++, vskTrans.getViewId());
                        pst.setInt(i++, vskTrans.getViewColumnId());
						pst.setInt(i++, vskTrans.getKeySequenceNo());
						pst.setString(i++, vskTrans.getSortSequenceCode());
						pst.setInt(i++, vskTrans.getFooterOptionCode());
						pst.setInt(i++, vskTrans.getHeaderOptionCode());
                        pst.setString(i++, vskTrans.getDisplayModeCode());
						pst.setString(i++, vskTrans.getSortkeyLabel());
						pst.setString(i++, vskTrans.getDataTypeCode());
						pst.setInt(i++, DataUtilities.booleanToInt(vskTrans.isSigned()));
                        pst.setInt(i++, vskTrans.getStartPosition());
						pst.setInt(i++, vskTrans.getLength());
						pst.setInt(i++, vskTrans.getDecimalPlaces());
                        if (vskTrans.getDateTimeFormatCode() == null) {
                            pst.setNull(i++, Types.CHAR);
                        } else {
                            pst.setString(i++, vskTrans.getDateTimeFormatCode());
                        }                        
						pst.setInt(i++, vskTrans.getTitleFieldId());
						pst.setInt(i++, vskTrans.getTitleLength());
						if (isImportOrMigrate) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(vskTrans.getCreateTime()));
						}
						pst.setString(i++, isImportOrMigrate ? vskTrans.getCreateBy() : safrLogin.getUserId());
						if (isImportOrMigrate) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(vskTrans.getModifyTime()));
						}
						pst.setString(i++, isImportOrMigrate ? vskTrans.getModifyBy() : safrLogin.getUserId());
                        rs = pst.executeQuery();
                        rs.next();
                        int id = rs.getInt(1);          
                        vskTrans.setId(id);
                        vskTrans.setPersistent(true);
                        if (!isImportOrMigrate) {
                            vskTrans.setCreateBy(safrLogin.getUserId());
                            vskTrans.setCreateTime(rs.getDate(2));
                            vskTrans.setModifyBy(safrLogin.getUserId());
                            vskTrans.setModifyTime(rs.getDate(3));                            
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
			pst.close();

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while creating new View Sort Keys.",e);
		}
	}

	private void updateViewViewSortKeys(List<ViewSortKeyTransfer> viewSortKeyUpdateList)
			throws DAOException {
		boolean isImportOrMigrate = viewSortKeyUpdateList.get(0).isForImport()
		|| viewSortKeyUpdateList.get(0).isForMigration() ? true : false;

		try {
			String statement = "";
			if (!isImportOrMigrate) {
				statement = "UPDATE " + params.getSchema()
						+ ".VIEWSORTKEY SET KEYSEQNBR = ?, SORTSEQCD = ?, SORTBRKIND = ?, "
						+ "PAGEBRKIND = ?, SORTKEYLABEL = ?, SORTKEYDISPLAYCD = ?, SKFLDFMTCD = ?, "
						+ "SKSIGNED = ?, SKSTARTPOS = ?, SKFLDLEN = ?, SKDECIMALCNT = ?, "
						+ "SKFLDCONTENTCD = ?, SORTTITLELRFIELDID = ?, SORTTITLELENGTH =?, VIEWCOLUMNID = ?, "
						+ " LASTMODTIMESTAMP = CURRENT TIMESTAMP, LASTMODUSERID = ? WHERE "
						+ "VIEWSORTKEYID = ? AND ENVIRONID = ?";
			} else {
				statement = "UPDATE "
						+ params.getSchema()
						+ ".VIEWSORTKEY SET KEYSEQNBR = ?, SORTSEQCD = ?, SORTBRKIND = ?, "
						+ "PAGEBRKIND = ?, SORTKEYLABEL = ?, SORTKEYDISPLAYCD = ?, SKFLDFMTCD = ?, "
						+ "SKSIGNED = ?, SKSTARTPOS = ?, SKFLDLEN = ?, SKDECIMALCNT = ?, "
						+ "SKFLDCONTENTCD = ?, SORTTITLELRFIELDID = ?, SORTTITLELENGTH =?, VIEWCOLUMNID = ?, "
						+ "CREATEDTIMESTAMP = ?, CREATEDUSERID = ?, "
						+ " LASTMODTIMESTAMP = ?, LASTMODUSERID = ? WHERE "
						+ "VIEWSORTKEYID = ? AND ENVIRONID = ?";
			}
			PreparedStatement pst = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					for (ViewSortKeyTransfer vskTrans : viewSortKeyUpdateList) {
						int i = 1;
						pst.setInt(i++, vskTrans.getKeySequenceNo());
						pst.setString(i++, vskTrans.getSortSequenceCode());
						pst.setInt(i++, vskTrans.getFooterOptionCode());
						pst.setInt(i++, vskTrans.getHeaderOptionCode());
						pst.setString(i++, vskTrans.getSortkeyLabel());
						pst.setString(i++, vskTrans.getDisplayModeCode());
						pst.setString(i++, vskTrans.getDataTypeCode());
						pst.setInt(i++, DataUtilities.booleanToInt(vskTrans.isSigned()));
						pst.setInt(i++, vskTrans.getStartPosition());
						pst.setInt(i++, vskTrans.getLength());
						pst.setInt(i++, vskTrans.getDecimalPlaces());
                        if (vskTrans.getDateTimeFormatCode() == null) {
                            pst.setNull(i++, Types.CHAR);
                        } else {
                            pst.setString(i++, vskTrans.getDateTimeFormatCode());
                        }                        
						pst.setInt(i++, vskTrans.getTitleFieldId());
						pst.setInt(i++, vskTrans.getTitleLength());
						pst.setInt(i++, vskTrans.getViewColumnId());
						if (isImportOrMigrate) {
							// createby and lastmod set from import data
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(vskTrans.getCreateTime()));
							pst.setString(i++, vskTrans.getCreateBy());
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(vskTrans.getModifyTime()));
							pst.setString(i++, vskTrans.getModifyBy());
						} else {
							// createby details are untouched
							// lastmodtimestamp is CURRENT_TIMESTAMP
							// lastmoduserid is logged in user
							pst.setString(i++, safrLogin.getUserId());
						}

						pst.setInt(i++, vskTrans.getId());
						pst.setInt(i++, vskTrans.getEnvironmentId());
						pst.executeUpdate();
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
			throw DataUtilities.createDAOException("Database error occurred while updating View Sort Keys.",e);
		}
	}

	public void removeViewSortKeys(List<Integer> vskIdsList,
			Integer environmentId) throws DAOException {
		if (vskIdsList == null || vskIdsList.size() == 0) {
			return;
		}
		String placeholders = generator.getPlaceholders(vskIdsList.size());
		try {
			String statement = "DELETE FROM " + params.getSchema()
					+ ".VIEWSORTKEY Where VIEWSORTKEYID IN ( " + placeholders + " ) "
					+ " AND ENVIRONID = ? ";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int ndx = 1;
					for(int i =0; i<vskIdsList.size(); i++) {
						pst.setInt(ndx++,  vskIdsList.get(i));
					}
					pst.setInt(ndx, environmentId);
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
			throw DataUtilities.createDAOException("Database error occurred while deleting View Sort Keys.",e);
		}
	}
}
