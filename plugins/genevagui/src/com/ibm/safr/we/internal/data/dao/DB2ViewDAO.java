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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.SearchCriteria;
import com.ibm.safr.we.constants.SearchPeriod;
import com.ibm.safr.we.constants.SearchViewsIn;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.ViewDAO;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.FindTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ControlRecordQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;

/**
 * This class is used to implement the unimplemented methods of <b>ViewDAO</b>.
 * This class contains the methods to related to View metadata component which
 * require database access.
 * 
 */
public class DB2ViewDAO implements ViewDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.DB2ViewDAO");

	private static final String TABLE_NAME = "VIEW";

	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "VIEWID";
	private static final String COL_NAME = "NAME";
	private static final String COL_STATUS = "VIEWSTATUSCD";
	private static final String COL_EFFDATE = "EFFDATE";
	private static final String COL_TYPE = "VIEWTYPECD";
	private static final String COL_WRKFILENBR = "EXTRACTFILEPARTNBR";
	private static final String COL_OUTPUTFORMAT = "OUTPUTMEDIACD";
	private static final String COL_OUTPUTLRID = "OUTPUTLRID";
	private static final String COL_EXTFILEID = "LFPFASSOCID";
	private static final String COL_PAGESZ = "PAGESIZE";
	private static final String COL_LINESZ = "LINESIZE";
	private static final String COL_ZEROSUPP = "ZEROSUPPRESSIND";
	private static final String COL_EXTMAXREC = "EXTRACTMAXRECCNT";
	private static final String COL_AGGBYSORTKEY = "EXTRACTSUMMARYIND";
	private static final String COL_AGGBUFSIZE = "EXTRACTSUMMARYBUF";
	private static final String COL_OUTPUTMAXREC = "OUTPUTMAXRECCNT";
	private static final String COL_CTLID = "CONTROLRECID";
	private static final String COL_WRITEXID = "WRITEEXITID";
	private static final String COL_WRITEXSTART = "WRITEEXITSTARTUP";
	private static final String COL_FORMEXID = "FORMATEXITID";
	private static final String COL_FORMEXSTART = "FORMATEXITSTARTUP";
	private static final String COL_HEADERROW = "DELIMHEADERROWIND";
	private static final String COL_FLDDELIM = "FILEFLDDELIMCD";
	private static final String COL_STRDELIM = "FILESTRDELIMCD";
    private static final String COL_FORMATFILTLOGIC = "FORMATFILTLOGIC";
	private static final String COL_COMMENTS = "COMMENTS";
	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";
    private static final String COL_COMPILER = "COMPILER";
    private static final String COL_ACTIVATETIME = "LASTACTTIMESTAMP";
    private static final String COL_ACTIVATEBY = "LASTACTUSERID";

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
	public DB2ViewDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	private ViewTransfer generateTransfer(ResultSet rs) throws SQLException {
		ViewTransfer viewTransfer = new ViewTransfer();
		viewTransfer.setEnvironmentId(rs.getInt(COL_ENVID));
		viewTransfer.setId(rs.getInt(COL_ID));
		viewTransfer.setName(DataUtilities.trimString(rs.getString(COL_NAME)));

		viewTransfer.setStatusCode(DataUtilities.trimString(rs.getString(COL_STATUS)));
		viewTransfer.setEffectiveDate(rs.getDate(COL_EFFDATE));
		viewTransfer.setTypeCode(DataUtilities.trimString(rs.getString(COL_TYPE)));
		viewTransfer.setWorkFileNumber(rs.getInt(COL_WRKFILENBR));
		viewTransfer.setOutputFormatCode(DataUtilities.trimString(rs.getString(COL_OUTPUTFORMAT)));
		viewTransfer.setOutputLRId(rs.getInt(COL_OUTPUTLRID));
		viewTransfer.setExtractFileAssocId(rs.getInt(COL_EXTFILEID));
		viewTransfer.setPageSize(rs.getInt(COL_PAGESZ));
		viewTransfer.setLineSize(rs.getInt(COL_LINESZ));
		viewTransfer.setZeroSuppressInd(DataUtilities.intToBoolean(rs.getInt(COL_ZEROSUPP)));
		viewTransfer.setHeaderRow(DataUtilities.intToBoolean(rs.getInt(COL_HEADERROW)));
		viewTransfer.setExtractMaxRecCount(rs.getInt(COL_EXTMAXREC));
		viewTransfer.setExtractSummaryIndicator(DataUtilities.intToBoolean(rs.getInt(COL_AGGBYSORTKEY)));
		viewTransfer.setExtractSummaryBuffer(rs.getInt(COL_AGGBUFSIZE));
		viewTransfer.setOutputMaxRecCount(rs.getInt(COL_OUTPUTMAXREC));
		viewTransfer.setControlRecId(rs.getInt(COL_CTLID));
		viewTransfer.setWriteExitId(rs.getInt(COL_WRITEXID));
		viewTransfer.setWriteExitParams(DataUtilities.trimString(rs.getString(COL_WRITEXSTART)));
		viewTransfer.setFormatExitId(rs.getInt(COL_FORMEXID));
		viewTransfer.setFormatExitParams(DataUtilities.trimString(rs.getString(COL_FORMEXSTART)));
		viewTransfer.setFieldDelimCode(DataUtilities.trimString(rs.getString(COL_FLDDELIM)));
		viewTransfer.setHeaderRow(DataUtilities.intToBoolean(rs.getInt(COL_HEADERROW)));
		viewTransfer.setStringDelimCode(DataUtilities.trimString(rs.getString(COL_STRDELIM)));
        Clob clob = rs.getClob(COL_FORMATFILTLOGIC);
        if (clob != null) {
            viewTransfer.setFormatFilterlogic(clob.getSubString(1, (int) clob.length()));
        }		
		viewTransfer.setComments(DataUtilities.trimString(rs.getString(COL_COMMENTS)));
		viewTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
		viewTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		viewTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
		viewTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));
        viewTransfer.setCompilerVersion(DataUtilities.trimString(rs.getString(COL_COMPILER)));
        viewTransfer.setActivatedTime(rs.getDate(COL_ACTIVATETIME));
        viewTransfer.setActivatedBy(DataUtilities.trimString(rs.getString(COL_ACTIVATEBY)));

		return viewTransfer;
	}

	public ViewTransfer getView(Integer id, Integer environmentId)
			throws DAOException {

		ViewTransfer result = null;
		try {

			String selectString = "Select A.ENVIRONID, A.VIEWID, A.NAME, "
					+ "A.VIEWSTATUSCD, A.EFFDATE, A.VIEWTYPECD, A.EXTRACTFILEPARTNBR, "
					+ "A.OUTPUTMEDIACD, A.OUTPUTLRID, A.LFPFASSOCID, A.PAGESIZE, "
					+ "A.LINESIZE, A.ZEROSUPPRESSIND, A.EXTRACTMAXRECCNT, "
					+ "A.EXTRACTSUMMARYIND, A.EXTRACTSUMMARYBUF, A.OUTPUTMAXRECCNT, "
					+ "A.CONTROLRECID, A.WRITEEXITID, A.WRITEEXITSTARTUP, A.FORMATEXITID, "
					+ "A.FORMATEXITSTARTUP, A.FILEFLDDELIMCD, A.FILESTRDELIMCD, A.DELIMHEADERROWIND, "
					+ "A.COMMENTS, A.FORMATFILTLOGIC, "
					+ "A.CREATEDTIMESTAMP, A.CREATEDUSERID, A.LASTMODTIMESTAMP, A.LASTMODUSERID,"
					+ "A.COMPILER,A.LASTACTTIMESTAMP, A.LASTACTUSERID From "
					+ params.getSchema()
					+ ".VIEW A "
					+ "Where A.VIEWID=? AND A.ENVIRONID=?";

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
				logger.info("No such View in Env " + environmentId + " with ID : " + id);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the View with id ["+ id + "]", e);
		}
		return result;
	}

	public List<ViewTransfer> queryAllLogicBlocks() {

	    List<ViewTransfer> views = new ArrayList<ViewTransfer>();
        try {        
            String selectString = "Select ENVIRONID,VIEWID "
                + "From " + params.getSchema() + ".E_LOGICTBL   "
                + "WHERE TYPECD = 1";                    
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
                ViewTransfer view = new ViewTransfer();
                view.setEnvironmentId(rs.getInt("ENVIRONID"));
                view.setId(rs.getInt("VIEWID"));
                views.add(view);
            } 
            pst.close();
            rs.close();
        
        } catch (SQLException e) {
            throw DataUtilities.createDAOException("Database error occurred while retrieving Logic blocks", e);
        }
        return views;
	}
	
    public byte [] getLogicTextBytes(Integer id, Integer environmentId) {
        
        byte[] logicTextBytes = null;        
        try {        
            String selectString = "Select B.LOGIC "
                + "From " + params.getSchema() + ".E_LOGICTBL B  "
                + "Where B.VIEWID=? AND B.ENVIRONID=? AND B.TYPECD = 1";                    
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
                logicTextBytes = rs.getBytes("LOGIC");
            } else {
                logger.info("No such View Logic in Env " + environmentId + " with ID : " + id);
            }
            pst.close();
            rs.close();
        
        } catch (SQLException e) {
            throw DataUtilities.createDAOException("Database error occurred while retrieving the View Logic with id ["+ id + "]", e);
        }
        return logicTextBytes;      
    }
    
    public List<ViewQueryBean> queryAllViews(SortType sortType,
        Integer environmentId, Integer viewFolderId) throws DAOException {
        List<ViewQueryBean> result = new ArrayList<ViewQueryBean>();
    
        boolean admin = SAFRApplication.getUserSession().isSystemAdministrator();
    
        String orderString = null;
        if (sortType.equals(SortType.SORT_BY_ID)) {
            orderString = " ORDER BY A.VIEWID";
        } else {
            orderString = " ORDER BY UPPER(A.NAME)";
        }
        try {
            String selectString = "";
            if (viewFolderId == -1L) {
                if (admin) {
                    selectString = "SELECT A.VIEWID, A.NAME, A.VIEWSTATUSCD, A.OUTPUTMEDIACD, "
                            + "A.VIEWTYPECD, A.CREATEDTIMESTAMP, A.CREATEDUSERID, "
                            + "A.LASTMODTIMESTAMP, A.LASTMODUSERID,A.COMPILER,A.LASTACTTIMESTAMP,A.LASTACTUSERID FROM "
                            + params.getSchema() + ".VIEW A "
                            + "WHERE A.VIEWID > 0 AND A.ENVIRONID = ? "  + orderString;
                } else {
                    selectString = "SELECT A.VIEWID, A.NAME, A.VIEWSTATUSCD, A.OUTPUTMEDIACD, "
                            + "A.VIEWTYPECD, C.RIGHTS, A.CREATEDTIMESTAMP, A.CREATEDUSERID, "
                            + "A.LASTMODTIMESTAMP, A.LASTMODUSERID,A.COMPILER,A.LASTACTTIMESTAMP,A.LASTACTUSERID FROM "
                            + params.getSchema() + ".VIEW A LEFT OUTER JOIN "
                            + params.getSchema() + ".SECVIEW C ON C.ENVIRONID=A.ENVIRONID AND A.VIEWID=C.VIEWID AND C.GROUPID = ? "
                            + " WHERE A.VIEWID > 0 AND A.ENVIRONID = ? " + orderString;
                }
    
            } else {
                if (admin) {
                    selectString = "SELECT A.VIEWID, A.NAME, A.VIEWSTATUSCD, A.OUTPUTMEDIACD, "
                            + "A.VIEWTYPECD, A.CREATEDTIMESTAMP, A.CREATEDUSERID, "
                            + "A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.COMPILER,A.LASTACTTIMESTAMP,A.LASTACTUSERID FROM "
                            + params.getSchema() + ".VIEW A, "
                            + params.getSchema() + ".VFVASSOC B "
                            + "WHERE A.ENVIRONID=B.ENVIRONID "
                            + "AND A.VIEWID=B.VIEWID "
                            + "AND A.VIEWID > 0 AND A.ENVIRONID = ? "
                            + "AND B.VIEWFOLDERID = ? " 
                            + orderString;
                } else {
                    selectString = "SELECT A.VIEWID, A.NAME, A.VIEWSTATUSCD, A.OUTPUTMEDIACD, "
                            + "A.VIEWTYPECD, C.RIGHTS, A.CREATEDTIMESTAMP, A.CREATEDUSERID, "
                            + "A.LASTMODTIMESTAMP, A.LASTMODUSERID,A.COMPILER,A.LASTACTTIMESTAMP,A.LASTACTUSERID FROM "
                            + params.getSchema() + ".VIEW A INNER JOIN "
                            + params.getSchema() + ".VFVASSOC B ON A.ENVIRONID=B.ENVIRONID AND A.VIEWID=B.VIEWID LEFT OUTER JOIN "
                            + params.getSchema() + ".SECVIEW C ON B.ENVIRONID=C.ENVIRONID AND B.VIEWID=C.VIEWID "
                            + "AND C.GROUPID = ? "
                            + "WHERE A.VIEWID > 0 AND A.ENVIRONID = ? "
                            + "AND B.VIEWFOLDERID = ? "
                            + orderString;
                }
    
            }
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    if (viewFolderId == -1L) {
                        if (admin) {
                        	pst.setInt(1,  environmentId);
                        } else {
                        	pst.setInt(1,  SAFRApplication.getUserSession().getGroup().getId());
                        	pst.setInt(2,  environmentId);
                        }
                    } else {
                        if (admin) {
                        	pst.setInt(1,  environmentId);
                        	pst.setInt(2,  viewFolderId);
                        } else {
                        	pst.setInt(1,  SAFRApplication.getUserSession().getGroup().getId());
                        	pst.setInt(2,  environmentId);
                        	pst.setInt(3,  viewFolderId);                        	
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
                ViewQueryBean viewQueryBean = new ViewQueryBean(environmentId,
                    rs.getInt(COL_ID), 
                    DataUtilities.trimString(rs.getString(COL_NAME)), 
                    DataUtilities.trimString(rs.getString(COL_STATUS)),
                    DataUtilities.trimString(rs.getString(COL_OUTPUTFORMAT)),
                    DataUtilities.trimString(rs.getString(COL_TYPE)),
                    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.View, environmentId), 
                    rs.getDate(COL_CREATETIME), 
                    DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
                    rs.getDate(COL_MODIFYTIME), 
                    DataUtilities.trimString(rs.getString(COL_MODIFYBY)),
                    DataUtilities.trimString(rs.getString(COL_COMPILER)),
                    rs.getDate(COL_ACTIVATETIME), 
                    DataUtilities.trimString(rs.getString(COL_ACTIVATEBY)));
                result.add(viewQueryBean);
            }
            pst.close();
            rs.close();
            return result;
    
        } catch (SQLException e) {
            throw DataUtilities.createDAOException("Database error occurred while querying all Views.", e);
        }
    }
	
	public List<ViewQueryBean> queryAllViewsOld(SortType sortType,
			Integer environmentId, Integer viewFolderId) throws DAOException {
		List<ViewQueryBean> result = new ArrayList<ViewQueryBean>();

		boolean admin = SAFRApplication.getUserSession().isSystemAdministrator();

		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = " ORDER BY A.VIEWID";
		} else {
			orderString = " ORDER BY UPPER(A.VIEWNAME)";
		}
		try {
			String selectString = "";
			if (viewFolderId == -1L) {
				if (admin) {
					selectString = "SELECT A.VIEWID, A.VIEWNAME, A.VIEWSTATUSCD, A.OUTPUTMEDIACD, "
							+ "A.VIEWTYPECD, A.CREATEDTIMESTAMP, A.CREATEDUSERID, "
							+ "A.LASTMODTIMESTAMP, A.LASTMODUSERID FROM "
							+ params.getSchema() + ".E_VIEWTBL A "
							+ "WHERE A.VIEWID > 0 AND A.ENVIRONID = ?" + orderString;
				} else {
					selectString = "SELECT A.VIEWID, A.VIEWNAME, A.VIEWSTATUSCD, A.OUTPUTMEDIACD, "
							+ "A.VIEWTYPECD, C.VIEWRIGHTS, A.CREATEDTIMESTAMP, A.CREATEDUSERID, "
							+ "A.LASTMODTIMESTAMP, A.LASTMODUSERID FROM "
							+ params.getSchema() + ".E_VIEWTBL A LEFT OUTER JOIN "
							+ params.getSchema() + ".X_SECGROUPSVIEW C ON C.ENVIRONID=A.ENVIRONID AND A.VIEWID=C.VIEWID AND C.SECGROUPID= ?"
							+ " WHERE A.VIEWID > 0 AND A.ENVIRONID = ? " + orderString;
				}
			} else {
				if (admin) {
					selectString = "SELECT A.VIEWID, A.VIEWNAME, A.VIEWSTATUSCD, A.OUTPUTMEDIACD, "
							+ "A.VIEWTYPECD, A.CREATEDTIMESTAMP, A.CREATEDUSERID, "
							+ "A.LASTMODTIMESTAMP,A.LASTMODUSERID FROM "
							+ params.getSchema() + ".E_VIEWTBL A "
							+ "WHERE A.VIEWFOLDERID = ? "
							+ " AND A.VIEWID > 0 AND A.ENVIRONID = ?" + orderString;
				} else {
					selectString = "SELECT A.VIEWID, A.VIEWNAME, A.VIEWSTATUSCD, A.OUTPUTMEDIACD, "
							+ "A.VIEWTYPECD, C.VIEWRIGHTS, A.CREATEDTIMESTAMP, A.CREATEDUSERID, "
							+ "A.LASTMODTIMESTAMP, A.LASTMODUSERID FROM "
							+ params.getSchema() + ".E_VIEWTBL A LEFT OUTER JOIN "
							+ params.getSchema() + ".X_SECGROUPSVIEW C ON C.ENVIRONID=A.ENVIRONID AND A.VIEWID = C.VIEWID "
							+ "AND C.SECGROUPID = ? "
							+ " WHERE A.VIEWFOLDERID = ? "
							+ " AND A.VIEWID > 0 AND A.ENVIRONID = ? " + orderString;
				}
			}
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
                    if (viewFolderId == -1L) {
                        if (admin) {
                        	pst.setInt(1,  environmentId);
                        } else {
                        	pst.setInt(1,  SAFRApplication.getUserSession().getGroup().getId());
                        	pst.setInt(2,  environmentId);
                        }
                    } else {
                        if (admin) {
                        	pst.setInt(1,  viewFolderId);
                        	pst.setInt(2,  environmentId);
                        } else {
                        	pst.setInt(1,  SAFRApplication.getUserSession().getGroup().getId());
                        	pst.setInt(2,  viewFolderId);                        	
                        	pst.setInt(3,  environmentId);
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
				ViewQueryBean viewQueryBean = new ViewQueryBean(environmentId,
					rs.getInt(COL_ID), 
					DataUtilities.trimString(rs.getString("VIEWNAME")), 
					DataUtilities.trimString(rs.getString(COL_STATUS)),
					DataUtilities.trimString(rs.getString(COL_OUTPUTFORMAT)),
					DataUtilities.trimString(rs.getString(COL_TYPE)),
                    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("VIEWRIGHTS"), ComponentType.View, environmentId), 
					rs.getDate(COL_CREATETIME), 
					DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
					rs.getDate(COL_MODIFYTIME), 
					DataUtilities.trimString(rs.getString(COL_MODIFYBY)),
                    null,
                    null, 
                    null);
				result.add(viewQueryBean);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying all Views.", e);
		}
	}

    public List<ViewQueryBean> queryAllViews(SortType sortType,
        Integer environmentId, boolean admin)
        throws DAOException {
        List<ViewQueryBean> result = new ArrayList<ViewQueryBean>();
        try {
            String selectString = "";
            String orderBy = "";
            if (sortType.equals(SortType.SORT_BY_ID)) {
                orderBy = " ORDER BY A.VIEWID";
            } else {
                orderBy = " ORDER BY UPPER(A.NAME)";
            }
    
            if (admin) {
                selectString = "SELECT A.VIEWID, A.NAME AS VIEWNAME, A.VIEWSTATUSCD, A.OUTPUTMEDIACD, "
                    + "A.VIEWTYPECD, A.CREATEDTIMESTAMP, A.CREATEDUSERID, "
                    + "A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.COMPILER,A.LASTACTTIMESTAMP,A.LASTACTUSERID FROM "
                    + params.getSchema() + ".VIEW A "
                    + "WHERE A.VIEWID > 0 AND A.ENVIRONID = ? " + orderBy;
            } else {
                selectString = "SELECT A.VIEWID, A.NAME AS VIEWNAME, A.VIEWSTATUSCD, A.OUTPUTMEDIACD, "
                    + "A.VIEWTYPECD, C.RIGHTS, A.CREATEDTIMESTAMP, A.CREATEDUSERID, "
                    + "A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.COMPILER,A.LASTACTTIMESTAMP,A.LASTACTUSERID FROM "
                    + params.getSchema() + ".VIEW A LEFT OUTER JOIN "
                    + params.getSchema() + ".SECVIEW C ON C.ENVIRONID=A.ENVIRONID AND A.VIEWID=C.VIEWID "
                    + "AND C.GROUPID = ?"
                    + " WHERE A.VIEWID > 0 AND A.ENVIRONID = ? " + orderBy;           
            }
    
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    if(admin) {
                    	pst.setInt(1, environmentId);
                    } else {
                    	pst.setInt(1,  SAFRApplication.getUserSession().getGroup().getId());
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
                ViewQueryBean viewQueryBean = new ViewQueryBean(environmentId,
                    rs.getInt(COL_ID), 
                    DataUtilities.trimString(rs.getString("VIEWNAME")), 
                    DataUtilities.trimString(rs.getString(COL_STATUS)),
                    DataUtilities.trimString(rs.getString(COL_OUTPUTFORMAT)),
                    DataUtilities.trimString(rs.getString(COL_TYPE)), 
                    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.View, environmentId), 
                    rs.getDate(COL_CREATETIME), 
                    DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
                    rs.getDate(COL_MODIFYTIME), 
                    DataUtilities.trimString(rs.getString(COL_MODIFYBY)),
                    DataUtilities.trimString(rs.getString(COL_COMPILER)),
                    rs.getDate(COL_ACTIVATETIME), 
                    DataUtilities.trimString(rs.getString(COL_ACTIVATEBY)));
                result.add(viewQueryBean);
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                    "Database error occurred while querying all Views.", e);
        }
        return result;
    }

	public ViewTransfer persistView(ViewTransfer viewTransfer)
			throws DAOException, SAFRNotFoundException {
		if (!viewTransfer.isPersistent()) {
			return (createView(viewTransfer));
		} else {
			return (updateView(viewTransfer));
		}
	}

	private ViewTransfer createView(ViewTransfer viewTransfer) throws DAOException {
        try {
            String[] columnNames = { COL_ENVID, COL_NAME, COL_STATUS,
                    COL_TYPE, COL_WRKFILENBR, COL_OUTPUTFORMAT, COL_OUTPUTLRID, COL_EXTFILEID,
                    COL_PAGESZ, COL_LINESZ, COL_ZEROSUPP, COL_EXTMAXREC, COL_AGGBYSORTKEY,
                    COL_AGGBUFSIZE, COL_OUTPUTMAXREC, COL_CTLID, COL_WRITEXID, COL_WRITEXSTART,
                    COL_FORMEXID, COL_FORMEXSTART, COL_FLDDELIM, COL_STRDELIM, COL_HEADERROW, COL_FORMATFILTLOGIC,
                    COL_COMMENTS, COL_CREATETIME,
                    COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
            List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
            if (viewTransfer.isForImportOrMigration()) {
                names.add(1, COL_ID);
                names.add(COL_EFFDATE);
                names.add(COL_COMPILER);
                names.add(COL_ACTIVATETIME);
                names.add(COL_ACTIVATEBY);
            } else {
                if (viewTransfer.getStatusCode().equals(SAFRApplication.getSAFRFactory()
                        .getCodeSet(CodeCategories.VIEWSTATUS).getCode(Codes.ACTIVE).getKey())) {
                    names.add(COL_COMPILER);
                    names.add(COL_ACTIVATETIME);
                    names.add(COL_ACTIVATEBY);
                }
            }
            String statement = generator.getInsertStatement(params.getSchema(),
                    TABLE_NAME, COL_ID, names, !viewTransfer.isForImportOrMigration());
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(statement);

                    int i = 1;
                    pst.setInt(i++, viewTransfer.getEnvironmentId());
                    if (viewTransfer.isForImportOrMigration()) {
                        pst.setInt(i++, viewTransfer.getId());
                    }
                    pst.setString(i++, viewTransfer.getName());
                    pst.setString(i++, viewTransfer.getStatusCode());
                    pst.setString(i++, viewTransfer.getTypeCode());
                    pst.setInt(i++, viewTransfer.getWorkFileNumber());
                    pst.setString(i++, viewTransfer.getOutputFormatCode());
                    Integer lrId = viewTransfer.getOutputLRId();
                    if (lrId == null || lrId == 0) {
                        pst.setNull(i++, Types.INTEGER);
                    } else {
                        pst.setInt(i++, lrId);
                    }
					pst.setInt(i++, viewTransfer.getExtractFileAssocId() != null ? viewTransfer.getExtractFileAssocId() : 0); //this is tragic
                    pst.setInt(i++, viewTransfer.getPageSize());
                    pst.setInt(i++, viewTransfer.getLineSize());
                    pst.setInt(i++, DataUtilities.booleanToInt(viewTransfer.isSuppressZeroRecords()));
                    pst.setInt(i++, viewTransfer.getExtractMaxRecCount());
                    pst.setInt(i++, DataUtilities.booleanToInt(viewTransfer.isAggregateBySortKey()));
                    pst.setInt(i++, viewTransfer.getExtractSummaryBuffer());
                    pst.setInt(i++, viewTransfer.getOutputMaxRecCount());
                    pst.setInt(i++, viewTransfer.getControlRecId());
                    Integer exId = viewTransfer.getWriteExitId();
                    if (exId == null || exId == 0) {
                        pst.setNull(i++, Types.INTEGER);
                    } else {
                        pst.setInt(i++, exId);
                    }
                    pst.setString(i++, viewTransfer.getWriteExitParams());
                    Integer fexId = viewTransfer.getFormatExitId();
                    if (fexId == null || fexId == 0) {
                        pst.setNull(i++, Types.INTEGER);
                    } else {
                        pst.setInt(i++, fexId);
                    }
                    pst.setString(i++, viewTransfer.getFormatExitParams());
                    if (viewTransfer.getFieldDelimCode() == null) {
                        pst.setNull(i++, Types.CHAR);
                    } else {
                        pst.setString(i++, viewTransfer.getFieldDelimCode());
                    }
                    pst.setString(i++, viewTransfer.getStringDelimCode());
                    pst.setInt(i++, DataUtilities.booleanToInt(viewTransfer.isHeaderRow()));
                    Clob fflClob = con.createClob();
                    if (viewTransfer.getFormatFilterlogic() == null) {
                        fflClob.setString(1, "");
                    } else {
                        fflClob.setString(1, viewTransfer.getFormatFilterlogic());
                    }
                    pst.setClob(i++, fflClob);
                    pst.setString(i++, viewTransfer.getComments());
                    if (viewTransfer.isForImportOrMigration()) {
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewTransfer.getCreateTime()));
                    }
                    pst.setString(i++,
                            viewTransfer.isForImportOrMigration() ? viewTransfer.getCreateBy() : safrLogin.getUserId());
                    if (viewTransfer.isForImportOrMigration()) {
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewTransfer.getModifyTime()));
                    }
                    pst.setString(i++,
                            viewTransfer.isForImportOrMigration() ? viewTransfer.getModifyBy() : safrLogin.getUserId());
                    if (viewTransfer.isForImportOrMigration()) {
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewTransfer.getEffectiveDate()));
                        pst.setString(i++, viewTransfer.getCompilerVersion());
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewTransfer.getActivatedTime()));
                        pst.setString(i++, viewTransfer.getActivatedBy());
                    } else {
                        if (viewTransfer.getStatusCode().equals(SAFRApplication.getSAFRFactory()
                                .getCodeSet(CodeCategories.VIEWSTATUS).getCode(Codes.ACTIVE).getKey())) {
                            pst.setString(i++, viewTransfer.getCompilerVersion());
                            pst.setString(i++, safrLogin.getUserId());
                        }
                    }
                    rs = pst.executeQuery();
                    rs.next();
                    int id = rs.getInt(1);
                    viewTransfer.setId(id);
                    viewTransfer.setPersistent(true);
                    if (!viewTransfer.isForImportOrMigration()) {
                        viewTransfer.setCreateBy(safrLogin.getUserId());
                        viewTransfer.setCreateTime(rs.getDate(2));
                        viewTransfer.setModifyBy(safrLogin.getUserId());
                        viewTransfer.setModifyTime(rs.getDate(3));
                        if (viewTransfer.getStatusCode().equals(SAFRApplication.getSAFRFactory()
                                .getCodeSet(CodeCategories.VIEWSTATUS).getCode(Codes.ACTIVE).getKey())) {
                            viewTransfer.setActivatedBy(safrLogin.getUserId());
                            viewTransfer.setActivatedTime(rs.getDate(4));
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
            throw DataUtilities.createDAOException("Database error occurred while creating a new View.", e);
        }
        return viewTransfer;
	}

    private ViewTransfer updateView(ViewTransfer viewTransfer)
            throws DAOException, SAFRNotFoundException {

        try {
            String[] columnNames = { COL_NAME, COL_STATUS, COL_EFFDATE,
                    COL_TYPE, COL_WRKFILENBR, COL_OUTPUTFORMAT, COL_OUTPUTLRID, COL_EXTFILEID,
                    COL_PAGESZ, COL_LINESZ, COL_ZEROSUPP, COL_EXTMAXREC, COL_AGGBYSORTKEY,
                    COL_AGGBUFSIZE, COL_OUTPUTMAXREC, COL_CTLID, COL_WRITEXID, COL_WRITEXSTART,
                    COL_FORMEXID, COL_FORMEXSTART, COL_FLDDELIM, COL_STRDELIM, COL_HEADERROW,
                    COL_FORMATFILTLOGIC, COL_COMMENTS };
            List<String> names = new ArrayList<String>();
            names.addAll(Arrays.asList(columnNames));

            if (viewTransfer.isForImportOrMigration()) {
                names.add(COL_CREATETIME);
                names.add(COL_CREATEBY);
                names.add(COL_MODIFYTIME);
                names.add(COL_MODIFYBY);
                names.add(COL_COMPILER);
                names.add(COL_ACTIVATETIME);
                names.add(COL_ACTIVATEBY);
            } else {
                if (viewTransfer.isUpdated()) {
                    names.add(COL_MODIFYTIME);
                    names.add(COL_MODIFYBY);
                }
                if (viewTransfer.isActivated()) {
                    names.add(COL_COMPILER);
                    names.add(COL_ACTIVATETIME);
                    names.add(COL_ACTIVATEBY);
                }
            }

            List<String> idNames = new ArrayList<String>();
            idNames.add(COL_ID);
            idNames.add(COL_ENVID);

            String statement = generator.getUpdateStatement(params.getSchema(),
                    TABLE_NAME, names, idNames, !viewTransfer.isForImportOrMigration());
            PreparedStatement pst = null;
            while (true) {
                try {
                    pst = con.prepareStatement(statement);

                    int i = 1;
                    pst.setString(i++, viewTransfer.getName());
                    pst.setString(i++, viewTransfer.getStatusCode());
                    pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewTransfer.getEffectiveDate()));
                    pst.setString(i++, viewTransfer.getTypeCode());
                    pst.setInt(i++, viewTransfer.getWorkFileNumber());
                    pst.setString(i++, viewTransfer.getOutputFormatCode());
                    Integer lrId = viewTransfer.getOutputLRId();
                    if (lrId == null || lrId == 0) {
                        pst.setNull(i++, Types.INTEGER);
                    } else {
                        pst.setInt(i++, lrId);
                    }
					pst.setInt(i++, viewTransfer.getExtractFileAssocId() != null ? viewTransfer.getExtractFileAssocId() : 0); //this is tragic
                    pst.setInt(i++, viewTransfer.getPageSize());
                    pst.setInt(i++, viewTransfer.getLineSize());
                    pst.setInt(i++, DataUtilities.booleanToInt(viewTransfer.isSuppressZeroRecords()));
                    pst.setInt(i++, viewTransfer.getExtractMaxRecCount());
                    pst.setInt(i++, DataUtilities.booleanToInt(viewTransfer.isAggregateBySortKey()));
                    pst.setInt(i++, viewTransfer.getExtractSummaryBuffer());
                    pst.setInt(i++, viewTransfer.getOutputMaxRecCount());
                    pst.setInt(i++, viewTransfer.getControlRecId());
                    Integer exId = viewTransfer.getWriteExitId();
                    if (exId == null || exId == 0) {
                        pst.setNull(i++, Types.INTEGER);
                    } else {
                        pst.setInt(i++, exId);
                    }
                    pst.setString(i++, viewTransfer.getWriteExitParams());
                    Integer fexId = viewTransfer.getFormatExitId();
                    if (fexId == null || fexId == 0) {
                        pst.setNull(i++, Types.INTEGER);
                    } else {
                        pst.setInt(i++, fexId);
                    }
                    pst.setString(i++, viewTransfer.getFormatExitParams());
                    if (viewTransfer.getFieldDelimCode() == null) {
                        pst.setNull(i++, Types.CHAR);
                    } else {
                        pst.setString(i++, viewTransfer.getFieldDelimCode());
                    }
                    pst.setString(i++, viewTransfer.getStringDelimCode());
                    pst.setInt(i++, DataUtilities.booleanToInt(viewTransfer.isHeaderRow()));

                    Clob fflClob = con.createClob();
                    if (viewTransfer.getFormatFilterlogic() == null) {
                        fflClob.setString(1, "");
                    } else {
                        fflClob.setString(1, viewTransfer.getFormatFilterlogic());
                    }
                    pst.setClob(i++, fflClob);
                    pst.setString(i++, viewTransfer.getComments());
                    if (viewTransfer.isForImportOrMigration()) {
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewTransfer.getCreateTime()));
                        pst.setString(i++, viewTransfer.getCreateBy());
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewTransfer.getModifyTime()));
                        pst.setString(i++, viewTransfer.getModifyBy());
                        pst.setString(i++, viewTransfer.getCompilerVersion());
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewTransfer.getActivatedTime()));
                        pst.setString(i++, viewTransfer.getActivatedBy());
                    } else {
                        if (viewTransfer.isUpdated()) {
                            pst.setString(i++, safrLogin.getUserId());
                        }
                        if (viewTransfer.isActivated()) {
                            pst.setString(i++, viewTransfer.getCompilerVersion());
                            pst.setString(i++, safrLogin.getUserId());
                        }
                    }
                    pst.setInt(i++, viewTransfer.getId());
                    pst.setInt(i++, viewTransfer.getEnvironmentId());
                    if (!viewTransfer.isForImportOrMigration() &&
                            (viewTransfer.isUpdated() || viewTransfer.isActivated())) {
                        ResultSet rs = pst.executeQuery();
                        rs.next();
                        int j = 1;
                        if (viewTransfer.isUpdated()) {
                            viewTransfer.setModifyBy(safrLogin.getUserId());
                            viewTransfer.setModifyTime(rs.getDate(j++));
                            viewTransfer.setUpdated(false);
                        }
                        if (viewTransfer.isActivated()) {
                            viewTransfer.setActivatedBy(safrLogin.getUserId());
                            viewTransfer.setActivatedTime(rs.getDate(j++));
                            viewTransfer.setActivated(false);
                        }
                        rs.close();
                        pst.close();
                    } else {
                        int count = pst.executeUpdate();
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

        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                    "Database error occurred while updating the View.", e);
        }
        return viewTransfer;
    }

	public Map<ComponentType, List<EnvironmentalQueryBean>> queryViewPropertiesLists(
			Integer environmentId) throws DAOException {
		Map<ComponentType, List<EnvironmentalQueryBean>> result = new HashMap<ComponentType, List<EnvironmentalQueryBean>>();
		List<EnvironmentalQueryBean> innerList = null;
        boolean success = false;
        while (!success) {                                                                              		
    		try {
                // start a transaction
                DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();                               
    			boolean admin = SAFRApplication.getUserSession().isSystemAdministrator();
    
    			String statement = generator.getStoredProcedure(params.getSchema(),
    					"GETVIEWPROPS", 3);
    			CallableStatement proc = null;
    			ResultSet rs = null;
    			while (true) {
    				try {
    					proc = con.prepareCall(statement);
    					int i = 1;
    					proc.setInt(i++, environmentId);
    					proc.setInt(i++, DataUtilities.booleanToInt(admin));
    					proc.setInt(i++, safrLogin.getGroupId());
    					proc.execute();
    					rs = proc.getResultSet();
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
    				String entityType = rs.getString(1);
    				EnvironmentalQueryBean environmentalQueryBean = null;
    				ComponentType compType = DataUtilities.getComponentTypeFromString(entityType);
    
    				if (compType == null) {
    					continue;
    
    				}
    				if (compType == ComponentType.ViewFolder) {
    					environmentalQueryBean = new ViewFolderQueryBean(
    							environmentId, rs.getInt(2), 
    							DataUtilities.trimString(rs.getString(3)),
    							SAFRApplication.getUserSession().getEditRights(
    							    rs.getInt(4), ComponentType.ViewFolder, environmentId),    							
    							null, null, null, null);
    
    				} else if (compType == ComponentType.ControlRecord) {
    					environmentalQueryBean = new ControlRecordQueryBean(
    							environmentId, rs.getInt(2), 
    							DataUtilities.trimString(rs.getString(3)), 
    							SAFRApplication.getUserSession().getEditRights(
    							    rs.getInt(4), ComponentType.ControlRecord, environmentId),
                                null, null, null, null);
    
    				} else if (compType == ComponentType.LogicalFile) {
    					environmentalQueryBean = new LogicalFileQueryBean(
   							environmentId, rs.getInt(2), 
   							DataUtilities.trimString(rs.getString(3)), 
                            SAFRApplication.getUserSession().getEditRights(
                                rs.getInt(4), ComponentType.LogicalFile, environmentId),
   							null, null, null, null);
    
    				} else if (compType == ComponentType.LogicalRecord) {
    					environmentalQueryBean = new LogicalRecordQueryBean(
   							environmentId, rs.getInt(2), 
   							DataUtilities.trimString(rs.getString(3)), 
   							null, null, null, null, 
                            SAFRApplication.getUserSession().getEditRights(
                                rs.getInt(4), ComponentType.LogicalRecord, environmentId),
   							null, null, null, null, null, null);
    
    				} else if (compType == ComponentType.WriteUserExitRoutine
    						|| compType == ComponentType.FormatUserExitRoutine) {
    					environmentalQueryBean = new UserExitRoutineQueryBean(
    							environmentId, rs.getInt(2), 
    							DataUtilities.trimString(rs.getString(3)),
    							DataUtilities.trimString(rs.getString(5)), null, null, 
                                SAFRApplication.getUserSession().getEditRights(
                                    rs.getInt(4), ComponentType.UserExitRoutine, environmentId),
    							null, null, null, null);
    				}
    
    				if (result.containsKey(compType)) {
    					result.get(compType).add(environmentalQueryBean);
    
    				} else {
    					innerList = new ArrayList<EnvironmentalQueryBean>();
    					innerList.add(environmentalQueryBean);
    					result.put(compType, innerList);
    				}
    			}
    
    			proc.close();
    			rs.close();
                success=true;
            } catch (DAOUOWInterruptedException e) {
                // UOW interrupted so retry it
                continue;                                                                                                                               
    		} catch (SQLException e) {
    			throw DataUtilities.createDAOException(
                    "Database error occurred while retrieving different metadata components which are used in View Properties.",e);
            } finally {
                DAOFactoryHolder.getDAOFactory().getDAOUOW().end();
            } // end transaction try
        } // end transaction while loop   
        
		return result;
	}

	public Map<ComponentType, List<DependentComponentTransfer>> getInactiveDependenciesOfView(
			Integer environmentId, Integer viewId) throws DAOException {
		Map<ComponentType, List<DependentComponentTransfer>> result = new HashMap<ComponentType, List<DependentComponentTransfer>>();
		List<DependentComponentTransfer> inactiveLogicalRecords = new ArrayList<DependentComponentTransfer>();
		List<DependentComponentTransfer> inactiveLookupPaths = new ArrayList<DependentComponentTransfer>();

		try {

			// This query is used to select the dependent inactive LRID and LR
			// Name of the view.
			String selectString = "SELECT A.LOGRECID, A.NAME FROM "
					+ params.getSchema() + ".LOGREC A,"
					+ params.getSchema() + ". LRLFASSOC B,"
					+ params.getSchema() + ". VIEWSOURCE C, "
					+ params.getSchema() + ".VIEW D WHERE B.LOGRECID = A.LOGRECID AND "
					+ "B.ENVIRONID = A.ENVIRONID AND C.INLRLFASSOCID = B.LRLFASSOCID AND C.ENVIRONID = B.ENVIRONID AND "
					+ "D.VIEWID = C.VIEWID AND D.ENVIRONID = C.ENVIRONID AND "
					+ "A.LRSTATUSCD = 'INACT' AND  A.ENVIRONID = ? "+ " AND D.VIEWID = ? "
					+ " GROUP BY A.NAME, A.LOGRECID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, environmentId );
					pst.setInt(2, viewId );
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
				inactiveLogicalRecords.add(depCompTransfer);
			}
			if (!inactiveLogicalRecords.isEmpty()) {
				result.put(ComponentType.LogicalRecord, inactiveLogicalRecords);
			}
			pst.close();
			rs.close();

			// This query is used to select all the inactive Lookup Paths
			// depending upon the view.
			String selectStr = "SELECT A.LOOKUPID, A.NAME FROM "
					+ params.getSchema() + ".LOOKUP A," + params.getSchema()
					+ ".VIEWCOLUMNSOURCE B WHERE "
					+ "B.LOOKUPID = A.LOOKUPID AND B.ENVIRONID = A.ENVIRONID "
					+ "AND A.VALIDIND = 0 AND A.ENVIRONID = ? "
					+ " AND B.VIEWID = ? "
					+ " GROUP BY A.NAME, A.LOOKUPID";
			PreparedStatement pstLkpPath = null;
			ResultSet rsLkpPath = null;
			while (true) {
				try {
					pstLkpPath = con.prepareStatement(selectStr);
					pstLkpPath.setInt(1, environmentId );
					pstLkpPath.setInt(2, viewId );
					rsLkpPath = pstLkpPath.executeQuery();
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
			while (rsLkpPath.next()) {
				DependentComponentTransfer depCompTransfer = new DependentComponentTransfer();
				depCompTransfer.setId(rsLkpPath.getInt("LOOKUPID"));
				depCompTransfer.setName(DataUtilities.trimString(rsLkpPath.getString("NAME")));
				inactiveLookupPaths.add(depCompTransfer);

			}
			if (!inactiveLookupPaths.isEmpty()) {
				result.put(ComponentType.LookupPath, inactiveLookupPaths);
			}
			pstLkpPath.close();
			rsLkpPath.close();

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving all the Inactive Logical Record and Lookup Path dependencies of a View.",e);
		}
		return result;
	}

	public void removeView(Integer id, Integer environmentId)
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

					int i = 1;
					pst.setInt(i++, id);
					pst.setInt(i++, environmentId);
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
					"Database error occurred while deleting the View.", e);
		}
	}

	public ViewTransfer getDuplicateView(String viewName, Integer viewId,
			Integer environmentId) throws DAOException {
		ViewTransfer result = null;
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
					pst.setString(i++, viewName.toUpperCase());
					pst.setInt(i++, viewId);
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
				result = new ViewTransfer();
				result.setId(rs.getInt("VIEWID"));
				result.setName(viewName);
				logger.info("Existing View with name '" + viewName
						+ "' found in Environment [" + environmentId + "]");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving a duplicate View.",e);
		}
		return result;
	}

	public void persistViewLogic(ViewTransfer viewTransfer,
			boolean saveCompiledLogicText) throws DAOException {
		try {
			String table = "E_LOGICTBL";
			// delete the existing LT first.
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			idNames.add(COL_ENVID);

			int i = 1;

			String statement = generator.getDeleteStatement(params.getSchema(),
					table, idNames);
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);

					i = 1;
					pst.setInt(i++, viewTransfer.getId());
					pst.setInt(i++, viewTransfer.getEnvironmentId());
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
			if (viewTransfer.getLogicTextBytes() == null) {
				return;
			}
			pst.close();
			// insert the logic text
			String[] items = { COL_ENVID, COL_ID, "TYPECD", "LOGIC",
					COL_CREATETIME, COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
			idNames.clear();
			idNames = Arrays.asList(items);

			statement = generator.getInsertStatementNoIdentifier(params.getSchema(), table,
					idNames, !viewTransfer.isForImport());
			pst = null;

			while (true) {
				try {

					pst = con.prepareStatement(statement);
					i = 1;
					pst.setInt(i++, viewTransfer.getEnvironmentId());
					pst.setInt(i++, viewTransfer.getId());
					pst.setInt(i++, 1);
					pst.setBytes(i++, viewTransfer.getLogicTextBytes());
					if (viewTransfer.isForImport()) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewTransfer.getCreateTime()));
					}
					pst.setString(i++,viewTransfer.isForImport() ? viewTransfer.getCreateBy() : safrLogin.getUserId());
					if (viewTransfer.isForImport()) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewTransfer.getModifyTime()));
					}
					pst.setString(i++,viewTransfer.isForImport() ? viewTransfer.getModifyBy() : safrLogin.getUserId());
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
			if (saveCompiledLogicText) {
				// insert compiled LT snippet.
				while (true) {
					try {
						i = 1;
						pst.setInt(i++, viewTransfer.getEnvironmentId());
						pst.setInt(i++, viewTransfer.getId());
						pst.setInt(i++, 2);
						pst.setBytes(i++, viewTransfer.getCompiledLogicTextBytes());
						if (viewTransfer.isForImport()) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewTransfer.getCreateTime()));
						}
						pst.setString(i++,viewTransfer.isForImport() ? viewTransfer.getCreateBy() : safrLogin.getUserId());
						if (viewTransfer.isForImport()) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewTransfer.getModifyTime()));
						}
						pst.setString(i++,viewTransfer.isForImport() ? viewTransfer.getModifyBy() : safrLogin.getUserId());
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
			}
			pst.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while storing the logic text bytes contained in a View.",e);
		}
	}

	public void makeViewsInactive(Collection<Integer> viewIds, Integer environmentId)
			throws DAOException {
		if (viewIds == null || viewIds.isEmpty()) {
			return;
		}
		try {
		    // determine existing state
			String placeholders = generator.getPlaceholders(viewIds.size());
			String statementViews = "UPDATE " + params.getSchema() + ".VIEW " + 
			    "SET VIEWSTATUSCD = 'INACT'," + 
			    "LASTACTTIMESTAMP=CURRENT TIMESTAMP," +
			    "LASTACTUSERID='" + safrLogin.getUserId() + "' " +
			    "WHERE VIEWID IN ( " + placeholders + " )" +
			    "AND ENVIRONID = ?" +  " " +
			    "AND VIEWSTATUSCD != 'INACT'";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statementViews);
					int ndx = 1;
					Iterator<Integer> si = viewIds.iterator();
					while(si.hasNext()) {
						Integer v = si.next(); 
						pst.setInt(ndx++, v);
					}
					pst.setInt(ndx, environmentId );
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
			
			// delete logic dependency rows
            String deleteStatement = "DELETE FROM " + params.getSchema()
            + ".VIEWLOGICDEPEND WHERE environid = ? "
            + " AND viewid in (" + placeholders + ")";

            while (true) {
                try {
                    pst = con.prepareStatement(deleteStatement);
					int ndx = 1;
					pst.setInt(ndx++, environmentId );
					Iterator<Integer> si = viewIds.iterator();
					while(si.hasNext()) {
						Integer v = si.next(); 
						pst.setInt(ndx++, v);
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
			throw DataUtilities.createDAOException("Database error occurred while making the Views inactive.",e);
		}
	}

    private class ViewInfo {
        public Integer id;
        public String name;
        public EditRights rights;
    };
	
    public List<FindTransfer> searchViewsToReplaceLogicText(
        Integer environmentId, SearchViewsIn searchViewsIn,
        List<Integer> componentsToSearchViewList,
        SearchCriteria searchCriteria, Date dateToSearch,
        SearchPeriod searchPeriod) throws DAOException {
        
        List<ViewInfo> views = findViews(environmentId, searchViewsIn, componentsToSearchViewList, 
                searchCriteria, dateToSearch, searchPeriod);
        
        List<FindTransfer> list = new ArrayList<FindTransfer>();
        for (ViewInfo viewInfo : views) {
            list.addAll(getViewFindTransfer(environmentId,viewInfo));
        }
        return list;
    }

    
    protected List<ViewInfo> findViews(Integer environmentId, SearchViewsIn searchViewsIn,
        List<Integer> componentsToSearchViewList,
        SearchCriteria searchCriteria, Date dateToSearch, SearchPeriod searchPeriod) 
    {
        boolean admin = SAFRApplication.getUserSession().isSystemAdministrator();
        List<ViewInfo> views = new ArrayList<ViewInfo>();
        String statement = "";
        String secjoin = "";
        if (admin) {
            statement = "SELECT A.VIEWID, A.NAME FROM ";            
        } else {
            statement = "SELECT A.VIEWID, A.NAME, C.RIGHTS FROM ";                        
            secjoin = " LEFT OUTER JOIN " + 
                params.getSchema()+ ".SECVIEW C ON A.ENVIRONID=C.ENVIRONID " +
                "AND A.VIEWID=C.VIEWID AND C.GROUPID=" + 
                SAFRApplication.getUserSession().getGroup().getId() + " ";
        }
        switch (searchViewsIn) {
            case SearchAllViews :
                statement += params.getSchema() + ".VIEW A " +
                secjoin + "WHERE A.ENVIRONID = ? ";
                break;
            case SearchInSpecificViews :
                statement += params.getSchema() + ".VIEW A " + 
                    secjoin + "WHERE A.VIEWID IN (" +
                    integerListToString(componentsToSearchViewList) +
                    ") AND A.ENVIRONID = ? ";
                break;
            case SearchInViewFolders :
                statement += params.getSchema() + ".VIEW A " + 
                    "JOIN " + params.getSchema() + ".VFVASSOC B " + 
                    " ON A.ENVIRONID=B.ENVIRONID AND A.VIEWID=B.VIEWID " +
                    secjoin + " WHERE B.VIEWFOLDERID IN (" + 
                    integerListToString(componentsToSearchViewList) +
                    ") AND B.ENVIRONID = ? ";                
                break;
            default :
                break;            
        }
        switch (searchCriteria) {
            case None :
                break;
            case SearchCreated :
                switch (searchPeriod) {
                    case After :
                        statement += " AND A.CREATEDTIMESTAMP > ? ";
                        break;
                    case Before :
                        statement += " AND A.CREATEDTIMESTAMP < ? ";
                        break;
                    case On :
                        statement += " AND DAYS(A.CREATEDTIMESTAMP) = ? ";
                        break;
                    default :
                        break;                    
                }
                break;
            case SearchModified :
                switch (searchPeriod) {
                    case After :
                        statement += " AND A.LASTMODTIMESTAMP > ? ";
                        break;
                    case Before :
                        statement += " AND A.LASTMODTIMESTAMP < ? ";
                        break;
                    case On :
                        statement += " AND DAYS(A.LASTMODTIMESTAMP) = ? ";
                        break;
                    default :
                        break;                    
                }
                break;
            default :
                break;
            
        }
        try {
            PreparedStatement pst = null;
            ResultSet rs;
            while (true) {
                try {
                    pst = con.prepareStatement(statement);
                    pst.setInt(1, environmentId);
                    if (searchCriteria.equals(SearchCriteria.SearchCreated) ||
                        searchCriteria.equals(SearchCriteria.SearchModified)) {
                        if (searchPeriod.equals(SearchPeriod.On)) {
                            Integer days = (int) (dateToSearch.getTime()/(24*60*60*1000)) + 719163;
                            pst.setInt(2,  days);                            
                        } else {
                            pst.setTimestamp(2,  new java.sql.Timestamp(dateToSearch.getTime()));
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
                ViewInfo vi = new ViewInfo();
                vi.id =  rs.getInt(1);
                vi.name = rs.getString(2);
                vi.rights = admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                    rs.getInt(3), ComponentType.View, environmentId); 
                views.add(vi);
            }
            rs.close();
            pst.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException("Database error getting view ids to search. ",e);
        }
        return views;
    }

    public List<FindTransfer> getViewFindTransfer(Integer envId, ViewInfo vi) {
        
        List<FindTransfer> total;
        try {
            total = new ArrayList<FindTransfer>();
            total.addAll(getFormatFilterViewLogic(envId, vi));
            total.addAll(getFormatColumnViewLogic(envId, vi));
            total.addAll(getExtractFilterandOutputViewLogic(envId, vi));
            total.addAll(getExtractColumnViewLogic(envId, vi));
        } catch (SQLException e) {
            throw DataUtilities.createDAOException("Database error getting view find transfers.",e);
        }
        return total;
    }

    private List<FindTransfer> getFormatFilterViewLogic(Integer envId, ViewInfo vi) 
        throws DAOException, SQLException {
        List<FindTransfer> list = new ArrayList<FindTransfer>();
        String formatFiltStmt = "SELECT FORMATFILTLOGIC FROM  "
            + params.getSchema() + ".VIEW "
            + "WHERE ENVIRONID = ? " 
            + "AND VIEWID = ? ";
        ResultSet rs = null;
        PreparedStatement pformatFiltStmt;
        while (true) {
            try {
                pformatFiltStmt = con.prepareStatement(formatFiltStmt);
                pformatFiltStmt.setInt(1, envId);
                pformatFiltStmt.setInt(2, vi.id);                
                rs = pformatFiltStmt.executeQuery();
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
            FindTransfer trans = new FindTransfer();
            trans.setViewId(vi.id);
            trans.setViewName(vi.name);
            trans.setRights(vi.rights);
            trans.setLogicTextType(LogicTextType.Format_Record_Filter);
            trans.setCellId(vi.id);
            trans.setReferenceId(0);
            Clob clob = rs.getClob("FORMATFILTLOGIC");
            if (clob != null) {                
                trans.setLogicText(clob.getSubString(1, (int) clob.length()));
                if (!trans.getLogicText().isEmpty()) {
                    list.add(trans);
                }
            }    
        }
        rs.close();
        pformatFiltStmt.close();
        return list;
    }

    private List<FindTransfer> getFormatColumnViewLogic(Integer envId, ViewInfo vi) 
        throws DAOException, SQLException {
        List<FindTransfer> list = new ArrayList<FindTransfer>();
        String formatColStmt = "SELECT VIEWCOLUMNID, FORMATCALCLOGIC FROM "
            + params.getSchema() + ".VIEWCOLUMN "
            + "WHERE ENVIRONID = ? " 
            + "AND VIEWID = ?";
        ResultSet rs = null;
        PreparedStatement pformatColStmt;
        while (true) {
            try {
                pformatColStmt = con.prepareStatement(formatColStmt);
                pformatColStmt.setInt(1, envId);
                pformatColStmt.setInt(2, vi.id);                
                rs = pformatColStmt.executeQuery();
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
            FindTransfer trans = new FindTransfer();
            trans.setViewId(vi.id);
            trans.setViewName(vi.name);            
            trans.setRights(vi.rights);
            trans.setLogicTextType(LogicTextType.Format_Column_Calculation);
            trans.setReferenceId(0);
            trans.setCellId(rs.getInt("VIEWCOLUMNID"));
            Clob clob = rs.getClob("FORMATCALCLOGIC");
            if (clob != null) {
                trans.setLogicText(clob.getSubString(1, (int) clob.length()));
                if (!trans.getLogicText().isEmpty()) {
                    list.add(trans);
                }
            }   
        }
        rs.close();
        pformatColStmt.close();
        return list;
    }

    private List<FindTransfer> getExtractFilterandOutputViewLogic(Integer envId, ViewInfo vi) 
        throws DAOException, SQLException {
        List<FindTransfer> list = new ArrayList<FindTransfer>();
        String extractFiltStmt = "SELECT VIEWSOURCEID, EXTRACTFILTLOGIC, EXTRACTOUTPUTLOGIC FROM  "
            + params.getSchema() + ".VIEWSOURCE B "
            + "WHERE ENVIRONID = ? " 
            + "AND VIEWID = ?";
        ResultSet rs = null;
        PreparedStatement pextractFiltStmt;
        while (true) {
            try {
                pextractFiltStmt = con.prepareStatement(extractFiltStmt);
                pextractFiltStmt.setInt(1, envId);
                pextractFiltStmt.setInt(2, vi.id);                
                rs = pextractFiltStmt.executeQuery();
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
            int viewSourceId = rs.getInt("VIEWSOURCEID");
            Clob clob = rs.getClob("EXTRACTFILTLOGIC");
            if (clob != null) {
                String logicText = clob.getSubString(1, (int) clob.length());
                if (!logicText.isEmpty()) {
                    FindTransfer trans = new FindTransfer();
                    trans.setViewId(vi.id);
                    trans.setViewName(vi.name);            
                    trans.setRights(vi.rights);
                    trans.setLogicTextType(LogicTextType.Extract_Record_Filter);
                    trans.setLogicText(logicText);
                    trans.setReferenceId(0);
                    trans.setCellId(viewSourceId);
                    list.add(trans);
                }
            }       
            clob = rs.getClob("EXTRACTOUTPUTLOGIC");
            if (clob != null) {
                String logicText = clob.getSubString(1, (int) clob.length());
                if (!logicText.isEmpty()) {
                    FindTransfer trans = new FindTransfer();
                    trans.setViewId(vi.id);
                    trans.setViewName(vi.name);            
                    trans.setRights(vi.rights);
                    trans.setLogicTextType(LogicTextType.Extract_Record_Output);
                    trans.setLogicText(logicText);
                    trans.setReferenceId(0);
                    trans.setCellId(viewSourceId);
                    list.add(trans);
                }
            }       
        }
        rs.close();
        pextractFiltStmt.close();
        return list;
    }

    private List<FindTransfer> getExtractColumnViewLogic(Integer envId, ViewInfo vi) 
        throws DAOException, SQLException {
        List<FindTransfer> list = new ArrayList<FindTransfer>();
        String extractColStmt = "SELECT B.COLUMNNUMBER,C.VIEWCOLUMNSOURCEID,C.EXTRACTCALCLOGIC FROM  "
            + params.getSchema() + ".VIEWCOLUMN B, "
            + params.getSchema() + ".VIEWCOLUMNSOURCE C "
            + "WHERE B.ENVIRONID = C.ENVIRONID " 
            + "AND B.VIEWCOLUMNID = C.VIEWCOLUMNID " 
            + "AND B.ENVIRONID = ? " 
            + "AND B.VIEWID = ?";
        ResultSet rs = null;
        PreparedStatement pextractColStmt;
        while (true) {
            try {
                pextractColStmt = con.prepareStatement(extractColStmt);
                pextractColStmt.setInt(1, envId);
                pextractColStmt.setInt(2, vi.id);                
                rs = pextractColStmt.executeQuery();
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
            FindTransfer trans = new FindTransfer();
            trans.setViewId(vi.id);
            trans.setViewName(vi.name);            
            trans.setRights(vi.rights);
            trans.setLogicTextType(LogicTextType.Extract_Column_Assignment);
            trans.setReferenceId(rs.getInt("COLUMNNUMBER"));
            trans.setCellId(rs.getInt("VIEWCOLUMNSOURCEID"));
            Clob clob = rs.getClob("EXTRACTCALCLOGIC");
            if (clob != null) {
                trans.setLogicText(clob.getSubString(1, (int) clob.length()));
                if (!trans.getLogicText().isEmpty()) {
                    list.add(trans);
                }
            }       
        }
        rs.close();
        pextractColStmt.close();
        return list;
    }

    public void replaceLogicText(Integer environmentId,
        List<FindTransfer> replacements) throws DAOException {
        boolean success = false;
        while (!success) {                                                                              
            try {
                // start a transaction
                DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();                               
                String statement = generator.getStoredProcedure(params.getSchema(), "UPDVWLOGIC", 2);
                CallableStatement proc = null;
                while (true) {
                    try {
                        proc = con.prepareCall(statement);
                        proc.setInt(1, environmentId);
                        String xml = generateReplaceXml(replacements);
                        proc.setString("DOC", xml);
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
                success=true;
            } catch (DAOUOWInterruptedException e) {
                // UOW interrupted so retry it
                continue;                                                                                                                           
            } catch (SQLException e) {
                throw DataUtilities.createDAOException(
                    "Database error occurred while replacing logic text.",e);
            } finally {
                DAOFactoryHolder.getDAOFactory().getDAOUOW().end();
            } // end transaction try
        } // end transaction while loop              
        
    }
	
	private String generateReplaceXml(List<FindTransfer> replacements) {
        StringBuffer xml = new StringBuffer();
        xml.append("<Root>\n");        
        for (FindTransfer trans : replacements) {
            xml.append(" <Operation>\n");
            xml.append("  <LOGTYPE>" + trans.getLogicTextType().getOpStr() + "</LOGTYPE>\n");
            xml.append("  <VIEWID>" + trans.getViewId() + "</VIEWID>\n");
            if (trans.getCellId() != null) {
                xml.append("  <CELLID>" + trans.getCellId() + "</CELLID>\n");
            }
            xml.append("  <LOGIC><![CDATA[" + trans.getLogicText() + "]]></LOGIC>\n");
            xml.append(" </Operation>\n");
        }
        xml.append("</Root>");        
        return xml.toString();
    }

    private static String integerListToString(
			List<Integer> listOfIntegerVariables) {
		String commaDelimitedString = "";
		if ((listOfIntegerVariables != null)
				&& (!listOfIntegerVariables.isEmpty())) {
			for (Integer integerVariable : listOfIntegerVariables) {
				commaDelimitedString += integerVariable.toString() + ",";
			}
			commaDelimitedString = commaDelimitedString.substring(0,
					commaDelimitedString.length() - 1);
		} else {
			commaDelimitedString = "0";
		}
		return commaDelimitedString;

	}

	public void deleteView(Integer viewId, Integer environmentId)
			throws DAOException {
		boolean success = false;
		try {

			while (!success) {
				List<String> idNames = new ArrayList<String>();
				idNames.add("VIEWID");
				idNames.add("ENVIRONID");
				int i = 1;
				try {
					// Begin Transaction
					DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
					// Deleting View Logic Dependencies...
					String deleteQuery = generator.getDeleteStatement(
							params.getSchema(), "VIEWLOGICDEPEND", idNames);
					PreparedStatement pst = null;

					while (true) {
						try {
							pst = con.prepareStatement(deleteQuery);
							i = 1;

							pst.setInt(i++, viewId);
							pst.setInt(i++, environmentId);
							pst.executeUpdate();
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

                    // Deleting Old Logic Table Rows
                    deleteQuery = generator.getDeleteStatement(
                            params.getSchema(), "E_LOGICTBL", idNames);
                    pst = null;

                    while (true) {
                        try {
                            pst = con.prepareStatement(deleteQuery);
                            i = 1;

                            pst.setInt(i++, viewId);
                            pst.setInt(i++, environmentId);
                            pst.executeUpdate();
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
					
					// Deleting View Header Footer...
					deleteQuery = generator.getDeleteStatement(
							params.getSchema(), "VIEWHEADERFOOTER", idNames);
					pst = null;

					while (true) {
						try {

							pst = con.prepareStatement(deleteQuery);
							i = 1;

							pst.setInt(i++, viewId);
							pst.setInt(i++, environmentId);
							pst.executeUpdate();
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

					// Deleting the View Sort Keys...
					deleteQuery = generator.getDeleteStatement(
							params.getSchema(), "VIEWSORTKEY", idNames);
					pst = null;

					while (true) {
						try {

							pst = con.prepareStatement(deleteQuery);
							i = 1;

							pst.setInt(i++, viewId);
							pst.setInt(i++, environmentId);
							pst.executeUpdate();
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
					// Deleting the Column Sources...
					deleteQuery = generator.getDeleteStatement(
							params.getSchema(), "VIEWCOLUMNSOURCE", idNames);
					pst = null;
					while (true) {
						try {

							pst = con.prepareStatement(deleteQuery);
							i = 1;

							pst.setInt(i++, viewId);
							pst.setInt(i++, environmentId);
							pst.executeUpdate();
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

					// Deleting the columns...
					deleteQuery = generator.getDeleteStatement(
							params.getSchema(), "VIEWCOLUMN", idNames);
					pst = null;
					while (true) {
						try {
							pst = con.prepareStatement(deleteQuery);
							i = 1;

							pst.setInt(i++, viewId);
							pst.setInt(i++, environmentId);
							pst.executeUpdate();
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

					// deleting View Sources...
					deleteQuery = generator.getDeleteStatement(
							params.getSchema(), "VIEWSOURCE", idNames);
					pst = null;
					while (true) {
						try {
							pst = con.prepareStatement(deleteQuery);
							i = 1;

							pst.setInt(i++, viewId);
							pst.setInt(i++, environmentId);
							pst.executeUpdate();
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

                    // Deleting the security rows
                    String deleteAssocQuery = generator.getDeleteStatement(
                        params.getSchema(), "SECVIEW", idNames);
                    while (true) {
                        try {
                            pst = con.prepareStatement(deleteAssocQuery);
                            pst.setInt(1, viewId);
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

                    // Deleting the view folder associations
                    String deleteFolderAssocQuery = generator.getDeleteStatement(
                        params.getSchema(), "VFVASSOC", idNames);
                    while (true) {
                        try {
                            pst = con.prepareStatement(deleteFolderAssocQuery);
                            pst.setInt(1, viewId);
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
                    
					// Deleting the View...
					deleteQuery = generator.getDeleteStatement(
							params.getSchema(), "VIEW", idNames);
					pst = null;
					while (true) {
						try {
							pst = con.prepareStatement(deleteQuery);
							i = 1;

							pst.setInt(i++, viewId);
							pst.setInt(i++, environmentId);
							pst.executeUpdate();
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
					throw DataUtilities.createDAOException("Database error occurred while permanently deleting the View and its components.",e);
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

    public List<ViewFolderViewAssociationTransfer> getVVFAssociation(Integer environmentId, Integer id, boolean admin) {
        List<ViewFolderViewAssociationTransfer> result = new ArrayList<ViewFolderViewAssociationTransfer>();

        try {
            String schema = params.getSchema();
            String selectString = null;

            if (!admin) { 
                admin = SAFRApplication.getUserSession().isSystemAdministrator();
            }
                
            if (admin) {
                selectString = "Select A.NAME AS VIEWNAME, A.VIEWID, B.VIEWFOLDERID, C.NAME AS VFNAME, B.VFVASSOCID, "
                        + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "
                        + "From " 
                        + schema + ".VIEW A, "
                        + schema + ".VFVASSOC B, "
                        + schema + ".VIEWFOLDER C "
                        + "Where A.ENVIRONID = ? "
                        + " AND A.VIEWID = ? "
                        + " AND A.ENVIRONID = B.ENVIRONID AND A.VIEWID = B.VIEWID"
                        + " AND B.ENVIRONID = C.ENVIRONID AND B.VIEWFOLDERID = C.VIEWFOLDERID "
                        + "Order By B.VFVASSOCID";
            } else {
                selectString = "Select A.NAME AS VIEWNAME, A.VIEWID, B.VIEWFOLDERID, C.NAME AS VFNAME, B.VFVASSOCID, D.RIGHTS, "
                        + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "
                        + "From "
                        + schema + ".VIEW A INNER JOIN "
                        + schema + ".VFVASSOC B ON A.ENVIRONID = B.ENVIRONID AND A.VIEWID = B.VIEWID INNER JOIN "
                        + schema + ".VIEWFOLDER C ON B.ENVIRONID = C.ENVIRONID AND B.VIEWFOLDERID = C.VIEWFOLDERID LEFT OUTER JOIN "
                        + schema + ".SECVIEWFOLDER D ON C.ENVIRONID = D.ENVIRONID AND C.VIEWFOLDERID = D.VIEWFOLDERID "
                        + " AND D.GROUPID = ?"
                        + " Where A.ENVIRONID = ?" 
                        + " AND A.VIEWID = ? "
                        + " Order By B.VFVASSOCID";
            }
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    if(admin) {
                    	pst.setInt(1,  environmentId);
                    	pst.setInt(2,  id);
                    } else {
                    	pst.setInt(1,  SAFRApplication.getUserSession().getGroup().getId());
                    	pst.setInt(2,  environmentId);
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
                ViewFolderViewAssociationTransfer vfvAssociationTransfer = new ViewFolderViewAssociationTransfer();
                vfvAssociationTransfer.setEnvironmentId(environmentId);
                vfvAssociationTransfer.setAssociatingComponentId(rs.getInt("VIEWID"));
                vfvAssociationTransfer.setAssociatingComponentName(
                    DataUtilities.trimString(rs.getString("VIEWNAME")));
                vfvAssociationTransfer.setAssociatedComponentId(rs.getInt("VIEWFOLDERID"));
                vfvAssociationTransfer.setAssociatedComponentName(
                    DataUtilities.trimString(rs.getString("VFNAME")));
                vfvAssociationTransfer.setAssociationId(rs.getInt("VFVASSOCID"));
                vfvAssociationTransfer.setAssociatedComponentRights(
                    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.View, environmentId));              
                vfvAssociationTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
                vfvAssociationTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
                vfvAssociationTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
                vfvAssociationTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));                             
                result.add(vfvAssociationTransfer);
            }
            pst.close();
            rs.close();
            return result;

        } catch (SQLException e) {
            String msg = "Database error occurred while retrieving view associations for Environment ["+ environmentId + "].";
            throw DataUtilities.createDAOException(msg, e);
        }
    }

    public ViewQueryBean queryView(Integer environmentId, Integer viewId) {
        ViewQueryBean result = null;
        try {
            String selectString = "SELECT A.VIEWID, A.NAME AS VIEWNAME, A.VIEWSTATUSCD, A.OUTPUTMEDIACD, "
                + "A.VIEWTYPECD, A.CREATEDTIMESTAMP, A.CREATEDUSERID, "
                + "A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.COMPILER,A.LASTACTTIMESTAMP,A.LASTACTUSERID FROM "
                + params.getSchema() + ".VIEW A "
                + "WHERE A.VIEWID = ? " 
                + "AND A.ENVIRONID = ? ";
    
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                	pst.setInt(1,  viewId);
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
            while (rs.next()) {
                result = new ViewQueryBean(environmentId,
                    rs.getInt(COL_ID), 
                    DataUtilities.trimString(rs.getString("VIEWNAME")), 
                    DataUtilities.trimString(rs.getString(COL_STATUS)),
                    DataUtilities.trimString(rs.getString(COL_OUTPUTFORMAT)),
                    DataUtilities.trimString(rs.getString(COL_TYPE)), 
                    EditRights.Read, 
                    rs.getDate(COL_CREATETIME), 
                    DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
                    rs.getDate(COL_MODIFYTIME), 
                    DataUtilities.trimString(rs.getString(COL_MODIFYBY)),
                    DataUtilities.trimString(rs.getString(COL_COMPILER)),
                    rs.getDate(COL_ACTIVATETIME), 
                    DataUtilities.trimString(rs.getString(COL_ACTIVATEBY)));
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                    "Database error occurred while querying View.", e);
        }
        return result;
    }

    public List<String> getViewSourceLogic(Integer envId, Integer viewId, Integer viewSourceId) {
        List<String> list;
        try {
            list = new ArrayList<String>();
            String extractFiltStmt = "SELECT EXTRACTFILTLOGIC FROM  "
                + params.getSchema() + ".VIEWSOURCE B "
                + "WHERE ENVIRONID = ? " 
                + "AND VIEWID = ? "
                + "AND VIEWSOURCEID = ?";
            ResultSet rs = null;
            PreparedStatement pextractFiltStmt;
            while (true) {
                try {
                    pextractFiltStmt = con.prepareStatement(extractFiltStmt);
                    pextractFiltStmt.setInt(1, envId);
                    pextractFiltStmt.setInt(2, viewId);                
                    pextractFiltStmt.setInt(3, viewSourceId);                
                    rs = pextractFiltStmt.executeQuery();
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
                Clob clob = rs.getClob("EXTRACTFILTLOGIC");
                if (clob != null) {
                    String logicText = clob.getSubString(1, (int) clob.length());
                    if (!logicText.isEmpty()) {
                        list.add(logicText);
                    }
                }       
            }
            rs.close();
            pextractFiltStmt.close();
                        
            String extractColStmt = "SELECT EXTRACTCALCLOGIC FROM  "
                + params.getSchema() + ".VIEWCOLUMNSOURCE "
                + "WHERE ENVIRONID = ? " 
                + "AND VIEWID = ? "
                + "AND VIEWSOURCEID = ?";
            rs = null;
            PreparedStatement pextractColStmt;
            while (true) {
                try {
                    pextractColStmt = con.prepareStatement(extractColStmt);
                    pextractColStmt.setInt(1, envId);
                    pextractColStmt.setInt(2, viewId);                
                    pextractColStmt.setInt(3, viewSourceId);                
                    rs = pextractColStmt.executeQuery();
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
                Clob clob = rs.getClob("EXTRACTCALCLOGIC");
                if (clob != null) {
                    String logicText = clob.getSubString(1, (int) clob.length());
                    if (!logicText.isEmpty()) {
                        list.add(logicText);
                    }
                }       
            }
            rs.close();
            pextractColStmt.close();
            
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while querying View Source Logic.", e);
        }
        return list;        
    }
    
}
