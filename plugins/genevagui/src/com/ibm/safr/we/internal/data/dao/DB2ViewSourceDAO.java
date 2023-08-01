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


import java.sql.Clob;
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

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.ViewSourceDAO;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;

public class DB2ViewSourceDAO implements ViewSourceDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.DB2ViewSourceDAO");

	private static final String TABLE_NAME = "VIEWSOURCE";

	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "VIEWSOURCEID";
    private static final String COL_VIEWID = "VIEWID";
	private static final String COL_SEQNO = "SRCSEQNBR";
	private static final String COL_INLRLFASSOCID = "INLRLFASSOCID";
	private static final String COL_EXTRACTFILTLOGIC = "EXTRACTFILTLOGIC";
    private static final String COL_OUTLFPFASSOCID = "OUTLFPFASSOCID";
    private static final String COL_WRITEEXITID = "WRITEEXITID";
    private static final String COL_WRITEEXITPARM = "WRITEEXITPARM";
    private static final String COL_EXTRACTOUTPUTIND = "EXTRACTOUTPUTIND";
    private static final String COL_EXTRACTOUTPUTLOGIC = "EXTRACTOUTPUTLOGIC";
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
	public DB2ViewSourceDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	private ViewSourceTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		ViewSourceTransfer vsTransfer = new ViewSourceTransfer();
		vsTransfer.setEnvironmentId(rs.getInt(COL_ENVID));
		vsTransfer.setId(rs.getInt(COL_ID));
        vsTransfer.setViewId(rs.getInt(COL_VIEWID));
		vsTransfer.setSourceSeqNo(rs.getInt(COL_SEQNO));
		vsTransfer.setLRFileAssocId(rs.getInt(COL_INLRLFASSOCID));
		Clob clob = rs.getClob(COL_EXTRACTFILTLOGIC);
		if (clob != null) {
		    vsTransfer.setExtractFilterLogic(clob.getSubString(1, (int) clob.length()));
		}
        vsTransfer.setExtractFileAssociationId(rs.getInt(COL_OUTLFPFASSOCID));
        vsTransfer.setWriteExitId(rs.getInt(COL_WRITEEXITID));
        vsTransfer.setWriteExitParams(rs.getString(COL_WRITEEXITPARM));
        vsTransfer.setExtractOutputOverride(DataUtilities.intToBoolean(rs.getInt(COL_EXTRACTOUTPUTIND)));
        clob = rs.getClob(COL_EXTRACTOUTPUTLOGIC);
        if (clob != null) {
            vsTransfer.setExtractRecordOutput(clob.getSubString(1, (int) clob.length()));
        }       
		vsTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
		vsTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		vsTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
		vsTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

		return vsTransfer;
	}

    public int getViewSourceLrId(Integer viewSrcId, Integer environmentId) throws DAOException {
        int result = 0;
        try {
            String selectString = "SELECT A.LOGRECID FROM " + 
                params.getSchema()+ ".LRLFASSOC A," +
                params.getSchema()+ ".VIEWSOURCE B " +
                "WHERE A.ENVIRONID=B.ENVIRONID " + 
                "AND A.LRLFASSOCID=B.INLRLFASSOCID " +
                "AND B.VIEWSOURCEID=?" +
                "AND B.ENVIRONID=?";
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, viewSrcId);
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
                result = rs.getInt(1);
            }
            pst.close();
            rs.close();
    
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving the View Sources with id ["+ viewSrcId + "]", e);
        }
        return result;
    }
	
	public List<ViewSourceTransfer> getViewSources(Integer viewId,
			Integer environmentId) throws DAOException {
		List<ViewSourceTransfer> result = new ArrayList<ViewSourceTransfer>();
		try {
			List<String> idNames = new ArrayList<String>();
			if (viewId > 0) {
				idNames.add(COL_VIEWID);
			}
			idNames.add(COL_ENVID);

			String[] columnNames = { COL_ENVID, COL_ID, COL_VIEWID, COL_SEQNO,
					COL_INLRLFASSOCID, COL_EXTRACTFILTLOGIC, 
					COL_OUTLFPFASSOCID, COL_WRITEEXITID, COL_WRITEEXITPARM,
					COL_EXTRACTOUTPUTIND, COL_EXTRACTOUTPUTLOGIC, 
					COL_CREATETIME, COL_CREATEBY,
					COL_MODIFYTIME, COL_MODIFYBY };
			List<String> columns = Arrays.asList(columnNames);

			String[] orderByNames = { COL_SEQNO };
			List<String> orderBy = Arrays.asList(orderByNames);

			String selectString = generator.getSelectColumnsStatement(columns,
					params.getSchema(), TABLE_NAME, idNames, orderBy);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					if (viewId > 0) {
						pst.setInt(1, viewId);
						pst.setInt(2, environmentId);
					} else {
						pst.setInt(1, environmentId);
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
				result.add(generateTransfer(rs));
			}
			pst.close();
			rs.close();

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the View Sources for the View with id ["+ viewId + "]", e);
		}
		return result;
	}

	public void persistViewSources(List<ViewSourceTransfer> viewSrcTransferList) throws DAOException {

		if (viewSrcTransferList == null || viewSrcTransferList.isEmpty()) {
			return;
		}
		List<ViewSourceTransfer> viewSrcCreate = new ArrayList<ViewSourceTransfer>();
		List<ViewSourceTransfer> viewSrcUpdate = new ArrayList<ViewSourceTransfer>();

		for (ViewSourceTransfer viewSrcTrans : viewSrcTransferList) {
			if (!viewSrcTrans.isPersistent()) {
				viewSrcCreate.add(viewSrcTrans);
			} else {
				viewSrcUpdate.add(viewSrcTrans);
			}
		}
		if (viewSrcCreate.size() > 0) {
			createViewSources(viewSrcCreate);
		}
		if (viewSrcUpdate.size() > 0) {
			updateViewSources(viewSrcUpdate);
		}
	}

	private void createViewSources(List<ViewSourceTransfer> viewSrcCreate) throws DAOException {

		boolean isImportOrMigrate = viewSrcCreate.get(0).isForImport()
		|| viewSrcCreate.get(0).isForMigration() ? true : false;

		try {
            String[] columnNames = { COL_ENVID, COL_VIEWID, COL_SEQNO,   
                COL_INLRLFASSOCID, COL_EXTRACTFILTLOGIC, 
                COL_OUTLFPFASSOCID, COL_WRITEEXITID, COL_WRITEEXITPARM, 
                COL_EXTRACTOUTPUTIND, COL_EXTRACTOUTPUTLOGIC,
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

					for (ViewSourceTransfer viewSrcTrans : viewSrcCreate) {
						int i = 1;
                        pst.setInt(i++, viewSrcTrans.getEnvironmentId());
                        if (isImportOrMigrate) {
                            pst.setInt(i++, viewSrcTrans.getId());
                        }						
                        pst.setInt(i++, viewSrcTrans.getViewId());
						pst.setInt(i++, viewSrcTrans.getSourceSeqNo());
						pst.setInt(i++, viewSrcTrans.getLRFileAssocId());
						Clob eflClob = con.createClob();
						if (viewSrcTrans.getExtractFilterLogic() == null) {
	                        eflClob.setString(1, "");						    
						} else {
						    eflClob.setString(1, viewSrcTrans.getExtractFilterLogic());
						}
						pst.setClob(i++, eflClob);
						if (viewSrcTrans.getExtractFileAssociationId() == null) {
	                        pst.setInt(i++, DataUtilities.getInt(0));						    
						}
						else {						    
	                        pst.setInt(i++, DataUtilities.getInt(viewSrcTrans.getExtractFileAssociationId()));
						}
                        if (viewSrcTrans.getWriteExitId() == null) {
                            pst.setInt(i++, DataUtilities.getInt(0));                            
                        }
                        else {
                            pst.setInt(i++, DataUtilities.getInt(viewSrcTrans.getWriteExitId()));                            
                        }
                        pst.setString(i++, viewSrcTrans.getWriteExitParams());
                        pst.setInt(i++, DataUtilities.booleanToInt(viewSrcTrans.isExtractOutputOverride()));
                        Clob epcClob = con.createClob();
                        if (viewSrcTrans.getExtractRecordOutput() == null) {
                            epcClob.setString(1, "");                           
                        } else {
                            epcClob.setString(1, viewSrcTrans.getExtractRecordOutput());
                        }
                        pst.setClob(i++, epcClob);
						
						if (isImportOrMigrate) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewSrcTrans.getCreateTime()));
						}
						pst.setString(i++,isImportOrMigrate ? viewSrcTrans.getCreateBy() : safrLogin.getUserId());
						if (isImportOrMigrate) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewSrcTrans.getModifyTime()));
						}
						pst.setString(i++,isImportOrMigrate ? viewSrcTrans.getModifyBy() : safrLogin.getUserId());
                        rs = pst.executeQuery();
                        rs.next();
                        int id = rs.getInt(1);          
                        viewSrcTrans.setId(id);
                        viewSrcTrans.setPersistent(true);
                        if (!isImportOrMigrate) {
                            viewSrcTrans.setCreateBy(safrLogin.getUserId());
                            viewSrcTrans.setCreateTime(rs.getDate(2));
                            viewSrcTrans.setModifyBy(safrLogin.getUserId());
                            viewSrcTrans.setModifyTime(rs.getDate(3));                            
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
			throw DataUtilities.createDAOException("Database error occurred while creating new View Sources.",e);
		}
	}

	private void updateViewSources(List<ViewSourceTransfer> viewSrcUpdate) throws DAOException {

		boolean isImportOrMigrate = viewSrcUpdate.get(0).isForImport()
		|| viewSrcUpdate.get(0).isForMigration() ? true : false;
		try {
			String statement1 = "";
			if (!isImportOrMigrate) {
				statement1 = "Update "
						+ params.getSchema()
						+ ".VIEWSOURCE Set SRCSEQNBR = ?,"
						+ " INLRLFASSOCID=?, EXTRACTFILTLOGIC=?, OUTLFPFASSOCID=?,"
						+ " WRITEEXITID=?, WRITEEXITPARM=?,"
						+ " EXTRACTOUTPUTIND=?, EXTRACTOUTPUTLOGIC=?, "
						+ "LASTMODTIMESTAMP = CURRENT TIMESTAMP, LASTMODUSERID =? "
						+ "WHERE VIEWSOURCEID = ? AND ENVIRONID = ?";
			} else {
				statement1 = "Update "
						+ params.getSchema()
						+ ".VIEWSOURCE Set SRCSEQNBR = ?,"
                        + " INLRLFASSOCID=?, EXTRACTFILTLOGIC=?, OUTLFPFASSOCID=?,"
                        + " WRITEEXITID=?, WRITEEXITPARM=?,"
                        + " EXTRACTOUTPUTIND=?, EXTRACTOUTPUTLOGIC=?, "
						+ "CREATEDTIMESTAMP = ?, CREATEDUSERID = ?, "
						+ "LASTMODTIMESTAMP = ?, LASTMODUSERID =? "
						+ "WHERE VIEWSOURCEID = ? AND ENVIRONID = ?";
			}
			PreparedStatement pst = null;

			while (true) {
				try {

					pst = con.prepareStatement(statement1);

					for (ViewSourceTransfer viewSrcTrans : viewSrcUpdate) {
						int i = 1;

						pst.setInt(i++, viewSrcTrans.getSourceSeqNo());
						pst.setInt(i++, viewSrcTrans.getLRFileAssocId());
                        Clob eflClob = con.createClob();
                        if (viewSrcTrans.getExtractFilterLogic() == null) {
                            eflClob.setString(1, "");
                        } 
                        else {
                            eflClob.setString(1, viewSrcTrans.getExtractFilterLogic());                            
                        }
                        pst.setClob(i++, eflClob);
                        
                        if (viewSrcTrans.getExtractFileAssociationId() == null) {
                            pst.setInt(i++, DataUtilities.getInt(0));                           
                        }
                        else {                          
                            pst.setInt(i++, DataUtilities.getInt(viewSrcTrans.getExtractFileAssociationId()));
                        }
                        if (viewSrcTrans.getWriteExitId() == null) {
                            pst.setInt(i++, 0);                            
                        } 
                        else {
                            pst.setInt(i++, viewSrcTrans.getWriteExitId());                            
                        }
                        pst.setString(i++, viewSrcTrans.getWriteExitParams());
                        pst.setInt(i++, DataUtilities.booleanToInt(viewSrcTrans.isExtractOutputOverride()));
                        Clob epcClob = con.createClob();
                        if (viewSrcTrans.getExtractRecordOutput() == null) {
                            epcClob.setString(1, "");                           
                        } else {
                            epcClob.setString(1, viewSrcTrans.getExtractRecordOutput());
                        }
                        pst.setClob(i++, epcClob);                        
						if (isImportOrMigrate) {
							// createby and lastmod set from import data
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewSrcTrans.getCreateTime()));
							pst.setString(i++, viewSrcTrans.getCreateBy());
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewSrcTrans.getModifyTime()));
							pst.setString(i++, viewSrcTrans.getModifyBy());
						} else {
							// createby details are untouched
							// lastmodtimestamp is CURRENT_TIMESTAMP
							// lastmoduserid is logged in user
							pst.setString(i++, safrLogin.getUserId());
						}
						pst.setInt(i++, viewSrcTrans.getId());
						pst.setInt(i++, viewSrcTrans.getEnvironmentId());
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
			throw DataUtilities.createDAOException(
					"Database error occurred while updating View Sources.", e);
		}
	}

	public Map<Integer, List<EnvironmentalQueryBean>> getViewSourceLookupPathDetails(
			Integer logicalRecordId, Integer environmentId) throws DAOException {
		Map<Integer, List<EnvironmentalQueryBean>> result = new HashMap<Integer, List<EnvironmentalQueryBean>>();
		List<EnvironmentalQueryBean> innerList = null;
		try {
			String selectQuery = "SELECT A.LOOKUPID, A.NAME AS LOOKNAME, B.LOGRECID, C.NAME AS RECNAME "
			        + "FROM "
					+ params.getSchema() + ".LOOKUP A, "
					+ params.getSchema() + ".LRLFASSOC B, "
					+ params.getSchema() + ".LOGREC C "
					+ "WHERE (A.ENVIRONID = ? "
					+ " AND C.LRSTATUSCD = 'ACTVE' AND A.SRCLRID = ? "
					+ ") AND (B.ENVIRONID = ? "
					+ " AND A.DESTLRLFASSOCID = B.LRLFASSOCID ) "
					+ "AND (C.ENVIRONID = ? "
					+ " AND B.LOGRECID = C.LOGRECID ) "
					+ "AND A.VALIDIND = 1 ORDER BY A.DESTLRLFASSOCID";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectQuery);
					pst.setInt(1,  environmentId);
					pst.setInt(2,  logicalRecordId);
					pst.setInt(3,  environmentId);
					pst.setInt(4,  environmentId);
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
				LookupQueryBean lkupPathQueryBean = new LookupQueryBean(
					environmentId, rs.getInt("LOOKUPID"), 
					DataUtilities.trimString(rs.getString("LOOKNAME")), 
					null, 1, 0, null, null, null, null, null, null, null, null, null);
				LogicalRecordQueryBean logicalRecordQueryBean = new LogicalRecordQueryBean(
					environmentId, rs.getInt("LOGRECID"), 
					DataUtilities.trimString(rs.getString("RECNAME")), 
					null, null, null, null, null, null, null, null, null, null, null);

				Integer LRId = rs.getInt("LOGRECID");
				if (result.containsKey(LRId)) {
					result.get(LRId).add(lkupPathQueryBean);

				} else {
					innerList = new ArrayList<EnvironmentalQueryBean>();
					innerList.add(logicalRecordQueryBean);
					innerList.add(lkupPathQueryBean);
					result.put(LRId, innerList);
				}
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving all the Lookup Paths which has the Logical Record as its source LR and all the target LR for those Lookup Paths.",e);
		}
		return result;
	}

	public Map<Integer, List<LookupPathSourceFieldTransfer>> getLookupPathSymbolicFields(
			List<Integer> lkupPathIds, Integer environmentId)
			throws DAOException {
		Map<Integer, List<LookupPathSourceFieldTransfer>> lookupPathSymbolicFields = new HashMap<Integer, List<LookupPathSourceFieldTransfer>>();
		List<LookupPathSourceFieldTransfer> innerList = null;
		if (lkupPathIds == null || lkupPathIds.size() == 0) {
			return lookupPathSymbolicFields;
		}
		try {
			String placeholders = generator.getPlaceholders(lkupPathIds.size());
			String selectQuery = "SELECT ENVIRONID ,LOOKUPSTEPID, LOOKUPID, SYMBOLICNAME FROM "
					+ params.getSchema() + ".LOOKUPSRCKEY WHERE LOOKUPID IN (" + placeholders +")"
					+ " AND ENVIRONID = ? "
					+ " AND FLDTYPE = 3 AND SYMBOLICNAME IS NOT NULL";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectQuery);
					int ndx = 1;
					for(int i =0; i<lkupPathIds.size(); i++) {
						pst.setInt(ndx++,  lkupPathIds.get(i));
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

				LookupPathSourceFieldTransfer srcFldTrans = new LookupPathSourceFieldTransfer();
				srcFldTrans.setEnvironmentId(rs.getInt("ENVIRONID"));
				srcFldTrans.setId(rs.getInt("LOOKUPSTEPID"));
				srcFldTrans.setSymbolicName(DataUtilities.trimString(rs.getString("SYMBOLICNAME")));
				Integer lookupPathId = rs.getInt("LOOKUPID");

				if (lookupPathSymbolicFields.containsKey(lookupPathId)) {
					lookupPathSymbolicFields.get(lookupPathId).add(srcFldTrans);

				} else {
					innerList = new ArrayList<LookupPathSourceFieldTransfer>();
					innerList.add(srcFldTrans);
					lookupPathSymbolicFields.put(lookupPathId, innerList);
				}
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving all the source fields of the Lookup Paths which are of type Symbolic.",e);
		}
		return lookupPathSymbolicFields;
	}

	public void removeViewSources(List<Integer> vwSrcIds, Integer environmentId)
			throws DAOException {
		if (vwSrcIds == null || vwSrcIds.size() == 0) {
			return;
		}
		try {
			String placeholders = generator.getPlaceholders(vwSrcIds.size());
			String vwSrcIdsString = DataUtilities.integerListToString(vwSrcIds);

			// deleting the column sources related to these View Sources.
			String deleteColSourcesQuery = "DELETE FROM " + params.getSchema()
					+ ".VIEWCOLUMNSOURCE WHERE VIEWSOURCEID IN (" + placeholders + " )"
					+ " AND ENVIRONID = ? " ;
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(deleteColSourcesQuery);
					int ndx = 1;
					for(int i =0; i<vwSrcIds.size(); i++) {
						pst.setInt(ndx++,  vwSrcIds.get(i));
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

			// deleting the View Sources
			String deleteViewSourcesQuery = "Delete From " + params.getSchema()
					+ ".VIEWSOURCE Where VIEWSOURCEID IN (" + placeholders + " )"
					+ " AND ENVIRONID = ? " ;
			PreparedStatement pst1 = null;

			while (true) {
				try {
					pst1 = con.prepareStatement(deleteViewSourcesQuery);
					int ndx = 1;
					for(int i =0; i<vwSrcIds.size(); i++) {
						pst1.setInt(ndx++,  vwSrcIds.get(i));
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

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while deleting View Sources.", e);
		}

	}
}
