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


import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.ViewColumnDAO;
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;

public class DB2ViewColumnDAO implements ViewColumnDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.DB2ViewColumnDAO");

	@SuppressWarnings("unused")
    private static final String TABLE_NAME = "VIEWCOLUMN";
    
	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "VIEWCOLUMNID";
	private static final String COL_VIEWID = "VIEWID";

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
	public DB2ViewColumnDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	private ViewColumnTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		ViewColumnTransfer vcTransfer = new ViewColumnTransfer();
		vcTransfer.setEnvironmentId(rs.getInt(COL_ENVID));
		vcTransfer.setId(rs.getInt(COL_ID));
		vcTransfer.setViewId(rs.getInt(COL_VIEWID));
		vcTransfer.setColumnNo(rs.getInt("COLUMNNUMBER"));
		vcTransfer.setDataType(DataUtilities.trimString(rs.getString("FLDFMTCD")));
		vcTransfer.setSigned(DataUtilities.intToBoolean(rs.getInt("SIGNEDIND")));
		vcTransfer.setStartPosition(rs.getInt("STARTPOSITION"));
		vcTransfer.setLength(rs.getInt("MAXLEN"));
		vcTransfer.setOrdinalPosition(rs.getInt("ORDINALPOSITION"));
		vcTransfer.setDecimalPlaces(rs.getInt("DECIMALCNT"));
		vcTransfer.setScalingFactor(rs.getInt("ROUNDING"));
		vcTransfer.setDateTimeFormat(DataUtilities.trimString(rs.getString("FLDCONTENTCD")));
		vcTransfer.setDataAlignmentCode(DataUtilities.trimString(rs.getString("JUSTIFYCD")));
		vcTransfer.setDefaultValue(DataUtilities.trimString(rs.getString("DEFAULTVAL")));
		vcTransfer.setVisible(DataUtilities.intToBoolean(rs.getInt("VISIBLE")));
		vcTransfer.setSubtotalTypeCode(DataUtilities.trimString(rs.getString("SUBTOTALTYPECD")));
		vcTransfer.setSpacesBeforeColumn(rs.getInt("SPACESBEFORECOLUMN"));
		vcTransfer.setExtractAreaCode(DataUtilities.trimString(rs.getString("EXTRACTAREACD")));
		vcTransfer.setExtractAreaPosition(rs.getInt("EXTRAREAPOSITION"));
		vcTransfer.setSortkeyFooterLabel(DataUtilities.trimString(rs.getString("SUBTLABEL")));
		vcTransfer.setNumericMask(rs.getString("RPTMASK"));
		vcTransfer.setHeaderAlignment(DataUtilities.trimString(rs.getString("HDRJUSTIFYCD")));
		vcTransfer.setColumnHeading1(DataUtilities.trimString(rs.getString("HDRLINE1")));
		vcTransfer.setColumnHeading2(DataUtilities.trimString(rs.getString("HDRLINE2")));
		vcTransfer.setColumnHeading3(DataUtilities.trimString(rs.getString("HDRLINE3")));
        Clob clob = rs.getClob("FORMATCALCLOGIC");
        if (clob != null) {
            vcTransfer.setFormatColumnLogic(clob.getSubString(1, (int) clob.length()));
        }		
		vcTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
		vcTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		vcTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
		vcTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

		return vcTransfer;
	}

	public List<ViewColumnTransfer> getViewColumns(Integer viewId,
			Integer environmentId) throws DAOException {
		List<ViewColumnTransfer> vcTransferList = new ArrayList<ViewColumnTransfer>();
		try {

			String whereStatement = " Where ";
			if (viewId > 0) {
				whereStatement += "VIEWID = ? AND "; 
			}
			whereStatement += " ENVIRONID = ?";

			String selectString = "Select ENVIRONID, VIEWCOLUMNID, VIEWID, COLUMNNUMBER, "
					+ "FLDFMTCD, SIGNEDIND, STARTPOSITION, MAXLEN, ORDINALPOSITION, "
					+ "DECIMALCNT, ROUNDING, FLDCONTENTCD, JUSTIFYCD, DEFAULTVAL, "
					+ "VISIBLE, SUBTOTALTYPECD, SPACESBEFORECOLUMN, EXTRACTAREACD, EXTRAREAPOSITION, "
					+ "SUBTLABEL, RPTMASK,HDRJUSTIFYCD, HDRLINE1, HDRLINE2, HDRLINE3, FORMATCALCLOGIC, "
					+ "CREATEDTIMESTAMP, LASTMODTIMESTAMP, CREATEDUSERID, LASTMODUSERID FROM "
					+ params.getSchema() + ".VIEWCOLUMN "
					+ whereStatement + " ORDER BY COLUMNNUMBER";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int i = 1;
					if (viewId > 0) {
						pst.setInt(i++, viewId);
					}
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
			while (rs.next()) {
				ViewColumnTransfer vcTransfer = new ViewColumnTransfer();
				vcTransfer = generateTransfer(rs);
				vcTransferList.add(vcTransfer);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving all View Columns for View with id [" + viewId + "]", e);
		}
		return vcTransferList;
	}

	public List<ViewColumnTransfer> persistViewColumns(
			List<ViewColumnTransfer> viewColTransferList) throws DAOException {

		if (viewColTransferList == null || viewColTransferList.isEmpty()) {
			return viewColTransferList;
		}
		
		List<ViewColumnTransfer> viewColCreate = new ArrayList<ViewColumnTransfer>();
		List<ViewColumnTransfer> viewColUpdate = new ArrayList<ViewColumnTransfer>();
        List<ViewColumnTransfer> ret = new ArrayList<ViewColumnTransfer>();

        int countCreate = 0;
        boolean fCreProc = false;
        int countUpdate = 0;
        boolean fUpProc = false;
        for (ViewColumnTransfer viewCol : viewColTransferList) {
            if (countCreate % 500 == 0 && fCreProc) {
                viewColCreate = createViewColumns(viewColCreate);
                ret.addAll(viewColCreate);               
                viewColCreate.clear();
                fCreProc = false;
            }
            if (countUpdate % 500 == 0 && fUpProc) {
                viewColUpdate = updateViewColumns(viewColUpdate);
                ret.addAll(viewColUpdate);               
                viewColUpdate.clear();
                fUpProc = false;                
            }
            if (!viewCol.isPersistent()) {
                fCreProc = true;
                countCreate++;
                viewColCreate.add(viewCol);
            } else {
                fUpProc = true;
                countUpdate++;
                viewColUpdate.add(viewCol);
            }
        }
        if (viewColCreate.size() > 0) {
            viewColCreate = createViewColumns(viewColCreate);
        }
        if (viewColUpdate.size() > 0) {
            viewColUpdate = updateViewColumns(viewColUpdate);
            ret.addAll(viewColUpdate);               
        }     
        //this should be for all of the columns not
        //just the viewColCreated which will be left at the chunk size
        if (viewColCreate.size() > 0) {
            fixUpCreatedColumns(viewColTransferList);                    
            ret.addAll(viewColTransferList);
        }
		return ret;
	}

	/*
	 * We need this do be done after the updates
	 * Consider the case where we add a column before an existing
	 * We get a new column to add
	 * And an old one to change to column number on
	 * This fixup happens AFTER the update 
	 * so we do not get two entries with the same column number
	 * That then messes things up later for the view column sources
	 */
    protected void fixUpCreatedColumns(List<ViewColumnTransfer> viewColCreate) {
        Map<Integer, Integer> idMap;
        try {
            idMap = getColIDMap(viewColCreate);
            for (ViewColumnTransfer col : viewColCreate) {
                col.setPersistent(true);
                if (!col.isForImportOrMigration()) {
                    col.setId(idMap.get(col.getColumnNo()));
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	
    private void getXml(ViewColumnTransfer srcFld, StringBuffer buf) throws SQLException, DAOException {
        
        buf.append("  <VIEWID>"+ srcFld.getViewId() + "</VIEWID>\n");
        buf.append("  <COLUMNNUMBER>"+ srcFld.getColumnNo() + "</COLUMNNUMBER>\n");
        if (srcFld.getDataType() != null) {
            buf.append("  <FLDFMTCD>"+ srcFld.getDataType() + "</FLDFMTCD>\n");
        }
        buf.append("  <SIGNEDIND>"+ DataUtilities.booleanToInt(srcFld.isSigned()) + "</SIGNEDIND>\n");
        if (srcFld.getStartPosition() != null) {        
            buf.append("  <STARTPOSITION>"+ srcFld.getStartPosition() + "</STARTPOSITION>\n");
        }        
        buf.append("  <MAXLEN>"+ srcFld.getLength() + "</MAXLEN>\n");
        if (srcFld.getOrdinalPosition() != null) {
            buf.append("  <ORDINALPOSITION>"+ srcFld.getOrdinalPosition() + "</ORDINALPOSITION>\n");                
        }
        buf.append("  <DECIMALCNT>"+ srcFld.getDecimalPlaces() + "</DECIMALCNT>\n");
        buf.append("  <ROUNDING>"+ srcFld.getScalingFactor() + "</ROUNDING>\n");
        if (srcFld.getDateTimeFormat() != null) {
            buf.append("  <FLDCONTENTCD>"+ srcFld.getDateTimeFormat() + "</FLDCONTENTCD>\n");
        }
        if (srcFld.getDataAlignmentCode() != null) {
            buf.append("  <JUSTIFYCD>" + srcFld.getDataAlignmentCode() + "</JUSTIFYCD>\n");
        }
        if (srcFld.getDefaultValue() != null) {
            String str = generator.handleSpecialChars(srcFld.getDefaultValue());
            buf.append("  <DEFAULTVAL>"+ str + "</DEFAULTVAL>\n");                
        }
        buf.append("  <VISIBLE>"+ DataUtilities.booleanToInt(srcFld.isVisible()) + "</VISIBLE>\n");
        if (srcFld.getSubtotalTypeCode() != null) {
            buf.append("  <SUBTOTALTYPECD>"+ srcFld.getSubtotalTypeCode() + "</SUBTOTALTYPECD>\n");
        }
        buf.append("  <SPACESBEFORECOLUMN>"+ srcFld.getSpacesBeforeColumn() + "</SPACESBEFORECOLUMN>\n");
        if (srcFld.getExtractAreaCode() != null) {
            buf.append("  <EXTRACTAREACD>"+ srcFld.getExtractAreaCode() + "</EXTRACTAREACD>\n");
        }
        if (srcFld.getExtractAreaPosition()!= null) {
            buf.append("  <EXTRAREAPOSITION>"+ srcFld.getExtractAreaPosition() + "</EXTRAREAPOSITION>\n");
        }
        if (srcFld.getSortkeyFooterLabel() != null) {
            buf.append("  <SUBTLABEL>"+ srcFld.getSortkeyFooterLabel() + "</SUBTLABEL>\n");
        }            
        if (srcFld.getNumericMask() != null) {            
            buf.append("  <RPTMASK>"+ srcFld.getNumericMask() + "</RPTMASK>\n");
        }
        if (srcFld.getHeaderAlignment() != null) {
            buf.append("  <HDRJUSTIFYCD>" + srcFld.getHeaderAlignment() + "</HDRJUSTIFYCD>\n");
        }
        if (srcFld.getColumnHeading1() != null) {
            String heading = generator.handleSpecialChars(srcFld.getColumnHeading1());
            buf.append("  <HDRLINE1>"+ heading + "</HDRLINE1>\n");                                
        }                                        
        if (srcFld.getColumnHeading2() != null) {
            String heading = generator.handleSpecialChars(srcFld.getColumnHeading2());
            buf.append("  <HDRLINE2>"+ heading + "</HDRLINE2>\n");                                
        }                                        
        if (srcFld.getColumnHeading3() != null) {
            String heading = generator.handleSpecialChars(srcFld.getColumnHeading3());            
            buf.append("  <HDRLINE3>"+ heading + "</HDRLINE3>\n");                                
        }                
        if (srcFld.getFormatColumnLogic() != null) {            
            buf.append("  <FORMATCALCLOGIC><![CDATA["+ srcFld.getFormatColumnLogic() + "]]></FORMATCALCLOGIC>\n");
        }
    }
    
	
	
    private String getUpdateXml(List<ViewColumnTransfer> srcFlds) throws SQLException, DAOException {
        StringBuffer buf = new StringBuffer();
        buf.append("<Root>\n");
        for (ViewColumnTransfer srcFld : srcFlds) {
            buf.append(" <Record>\n");
            buf.append("  <ENVIRONID>"+ srcFld.getEnvironmentId() + "</ENVIRONID>\n");
            buf.append("  <VIEWCOLUMNID>"+ srcFld.getId() + "</VIEWCOLUMNID>\n");
            getXml(srcFld, buf);
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
	
    private String getCreateXml(List<ViewColumnTransfer> srcFlds) throws SQLException, DAOException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("<Root>\n");
        for (ViewColumnTransfer srcFld : srcFlds) {
            buf.append(" <Record>\n");
            buf.append("  <ENVIRONID>"+ srcFld.getEnvironmentId() + "</ENVIRONID>\n");
            if (srcFld.isForImportOrMigration()) {
                buf.append("  <VIEWCOLUMNID>"+ srcFld.getId() + "</VIEWCOLUMNID>\n");
            }
            getXml(srcFld, buf);
            if (srcFld.isForImportOrMigration()) {
                buf.append("  <CREATEDTIMESTAMP>"+ generator.genTimeParm(srcFld.getCreateTime()) + "</CREATEDTIMESTAMP>\n");
                buf.append("  <CREATEDUSERID>"+ srcFld.getCreateBy() + "</CREATEDUSERID>\n");
                buf.append("  <LASTMODTIMESTAMP>"+ generator.genTimeParm(srcFld.getModifyTime()) + "</LASTMODTIMESTAMP>\n");
                buf.append("  <LASTMODUSERID>"+ srcFld.getModifyBy() + "</LASTMODUSERID>\n");
            }
            else {
                buf.append("  <CREATEDUSERID>"+ safrLogin.getUserId() + "</CREATEDUSERID>\n");
                buf.append("  <LASTMODUSERID>"+ safrLogin.getUserId() + "</LASTMODUSERID>\n");
            }            
            buf.append(" </Record>\n");            
        }
        buf.append("</Root>");
        return buf.toString();
    }   
	

	private List<ViewColumnTransfer> createViewColumns(
			List<ViewColumnTransfer> viewColCreateList) throws DAOException {
		try {
		    SAFRApplication.getTimingMap().startTiming("DB2ViewColumnDAO.createViewColumns");

			String statement = generator.getReturnStoredProcedure(params.getSchema(), "INSVIEWCOL", 2);

			CallableStatement proc = null;

            while (true) {
                try {
                    proc = con.prepareCall(statement);
                    proc.registerOutParameter(1, Types.INTEGER);
                    String xml = getCreateXml(viewColCreateList);
                    proc.setString(2, xml);
                    if (viewColCreateList.isEmpty() || !viewColCreateList.get(0).isForImportOrMigration()) {
                        proc.setInt(3, 0);                
                    }
                    else {
                        proc.setInt(3, 1);                                
                    }                   
                    proc.execute();
                    proc.close();
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
					"Database error occurred while creating View Columns.", e);
		}
		SAFRApplication.getTimingMap().stopTiming("DB2ViewColumnDAO.createViewColumns");
		
		return viewColCreateList;
	}

    protected Map<Integer, Integer> getColIDMap(List<ViewColumnTransfer> viewColCreateList) throws SQLException {
        
        Map<Integer, Integer> idMap = new HashMap<Integer, Integer>();
        List<Integer> colNoList = new ArrayList<Integer>();
        for (ViewColumnTransfer col : viewColCreateList) {
            colNoList.add(col.getColumnNo());
        }        
        String colNoStr = DataUtilities.integerListToString(colNoList);
        String chkSt = "select columnnumber,viewcolumnid " +
            "from " + params.getSchema() + ".viewcolumn " +
            "where environid=" + viewColCreateList.get(0).getEnvironmentId() + " " +
            "and viewid=" + viewColCreateList.get(0).getViewId() + " " +
            "and columnnumber in " + colNoStr;                    
        PreparedStatement pst = null;
        ResultSet rs = null;
        pst = con.prepareCall(chkSt);
        rs = pst.executeQuery();                    
        // form map of column number to id
        while (rs.next()) {
            Integer colNo = rs.getInt(1);
            Integer id = rs.getInt(2);
            idMap.put(colNo, id);
        }
        rs.close();
        pst.close();
        
        return idMap;
    }

	private List<ViewColumnTransfer> updateViewColumns(
			List<ViewColumnTransfer> viewColUpdateList) throws DAOException {
		try {
		    SAFRApplication.getTimingMap().startTiming("DB2ViewColumnDAO.updateViewColumns");
		    
			String statement = generator.getStoredProcedure(params.getSchema(), "UPDVIEWCOL", 2);
			CallableStatement proc = null;

            while (true) {
                try {
                    proc = con.prepareCall(statement);
                    String xml = getUpdateXml(viewColUpdateList);
                    proc.setString("DOC", xml);
                    if (viewColUpdateList.isEmpty() || !viewColUpdateList.get(0).isForImportOrMigration()) {
                        proc.setInt("IMPORT", 0);                
                    }
                    else {
                        proc.setInt("IMPORT", 1);                                
                    }                   
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
			proc.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while updating View Columns.", e);
		}
		SAFRApplication.getTimingMap().stopTiming("DB2ViewColumnDAO.updateViewColumns");
		
		return viewColUpdateList;
	}

	public void removeViewColumns(List<Integer> vwColumnIds,
			Integer environmentId) throws DAOException {
		if (vwColumnIds == null || vwColumnIds.size() == 0) {
			return;
		}
		try {
			String placeholders = generator.getPlaceholders(vwColumnIds.size());
			// deleting the column sources related to these View Columns.
			String deleteColSourcesQuery = "Delete From " + params.getSchema()
					+ ".VIEWCOLUMNSOURCE Where VIEWCOLUMNID IN ("
					+ placeholders + " ) AND ENVIRONID = ? ";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(deleteColSourcesQuery);
					int ndx = 1;
					for(int i=0; i<vwColumnIds.size(); i++) {
						pst.setInt(ndx++, vwColumnIds.get(i));
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

			// deleting Sort Keys related to these View Columns
			String deleteSortKeysQuery = "Delete From " + params.getSchema()
					+ ".VIEWSORTKEY Where VIEWCOLUMNID IN ("
					+ placeholders + " ) AND ENVIRONID = ? ";
			PreparedStatement pst1 = null;

			while (true) {
				try {
					pst1 = con.prepareStatement(deleteSortKeysQuery);
					int ndx = 1;
					for(int i=0; i<vwColumnIds.size(); i++) {
						pst1.setInt(ndx++, vwColumnIds.get(i));
					}
					pst1.setInt(ndx, environmentId);
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

			// deleting the View Columns
			String deleteViewColumnQuery = "Delete From " + params.getSchema()
					+ ".VIEWCOLUMN Where VIEWCOLUMNID IN ( "
					+ placeholders + " ) AND ENVIRONID = ? ";
			PreparedStatement pst2 = null;

			while (true) {
				try {
					pst2 = con.prepareStatement(deleteViewColumnQuery);
					int ndx = 1;
					for(int i=0; i<vwColumnIds.size(); i++) {
						pst2.setInt(ndx++, vwColumnIds.get(i));
					}
					pst2.setInt(ndx, environmentId);
					pst2.executeUpdate();
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
			pst2.close();

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while deleting View Columns.", e);
		}

	}

}
