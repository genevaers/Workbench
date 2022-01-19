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

import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.ViewLogicDependencyDAO;
import com.ibm.safr.we.data.transfer.ViewLogicDependencyTransfer;
import com.ibm.safr.we.internal.data.PGSQLGenerator;

/**
 * This class implements the database access for ViewLogicDependency.
 */
public class PGViewLogicDependencyDAO implements ViewLogicDependencyDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGViewLogicDependencyDAO");

	private static final String TABLE_NAME = "VIEWLOGICDEPEND";

	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_VIEWID = "VIEWID";
	private static final String COL_PARENTTYPE = "LOGICTYPECD";
	private static final String COL_PARENTID = "PARENTID";
	private static final String COL_DEPENDID = "DEPENDID";
	private static final String COL_LOOKUPID = "LOOKUPID";
	private static final String COL_LRFIELDID = "LRFIELDID";
	private static final String COL_EXITID = "EXITID";
	private static final String COL_LFPFASSOCID = "LFPFASSOCID";

	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";

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
	public PGViewLogicDependencyDAO(Connection con,
			ConnectionParameters params, UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	private ViewLogicDependencyTransfer generateTransfer(ResultSet rs)
	throws SQLException {
		ViewLogicDependencyTransfer vldTransfer = new ViewLogicDependencyTransfer();
		vldTransfer.setEnvironmentId(rs.getInt(COL_ENVID));
		vldTransfer.setId(rs.getInt(COL_VIEWID)); // !! this is because there are no real unique ids!!
		vldTransfer.setViewId(rs.getInt(COL_VIEWID));

		vldTransfer.setLogicTextType(LogicTextType.intToEnum(rs.getInt(COL_PARENTTYPE)));
		vldTransfer.setParentId(rs.getInt(COL_PARENTID));
		vldTransfer.setSequenceNo(rs.getInt(COL_DEPENDID));
		vldTransfer.setLookupPathId(rs.getInt(COL_LOOKUPID));
		vldTransfer.setLrFieldId(rs.getInt(COL_LRFIELDID));
		vldTransfer.setUserExitRoutineId(rs.getInt(COL_EXITID));
		vldTransfer.setFileAssociationId(rs.getInt(COL_LFPFASSOCID));
		
		vldTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
		vldTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		vldTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
		vldTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));
		
		return vldTransfer;
	}

	public void persistViewLogicDependencies(
			List<ViewLogicDependencyTransfer> viewLogicDepTfrList,
			Integer viewId, Integer environmentId) throws DAOException {
		try {

			// First delete any entry in VIEWLOGICDEPEND table for this view
			String deleteStatement = "DELETE FROM " + params.getSchema()
					+ ".VIEWLOGICDEPEND WHERE environid = ? "
					+ " AND viewid = ? ";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(deleteStatement);
					pst.setInt(1, environmentId );
					pst.setInt(2,  viewId);
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

			// Store the values if present in list.
			if (viewLogicDepTfrList == null || viewLogicDepTfrList.isEmpty()) {
				return;
			}
			String[] columnNames = { COL_ENVID, COL_VIEWID, COL_PARENTTYPE,
					COL_PARENTID, COL_DEPENDID, COL_LOOKUPID, COL_LRFIELDID,
					COL_EXITID, COL_LFPFASSOCID, COL_CREATETIME, COL_CREATEBY,
					COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = Arrays.asList(columnNames);

			String statement = generator.getInsertStatementNoIdentifier(params.getSchema(),
					TABLE_NAME, names);
			PreparedStatement pst1 = null;

			while (true) {
				try {
					pst1 = con.prepareStatement(statement);

					for (ViewLogicDependencyTransfer vldTrans : viewLogicDepTfrList) {

						int i = 1;
						pst1.setInt(i++, vldTrans.getEnvironmentId());
						pst1.setInt(i++, vldTrans.getViewId());
						pst1.setInt(i++, vldTrans.getLogicTextType().getTypeValue());
						pst1.setInt(i++, vldTrans.getParentId());
						pst1.setInt(i++, vldTrans.getSequenceNo());
						if(vldTrans.getLookupPathId() == null) {
							pst1.setNull(i++, Types.INTEGER);							
						} else {
							pst1.setInt(i++, vldTrans.getLookupPathId());
						}
						if(vldTrans.getLrFieldId() == null) {
							pst1.setNull(i++, Types.INTEGER);							
						} else {
							pst1.setInt(i++, vldTrans.getLrFieldId());
						}
						if(vldTrans.getUserExitRoutineId() == null) {
							pst1.setNull(i++, Types.INTEGER);							
						} else {
							pst1.setInt(i++, vldTrans.getUserExitRoutineId());
						}
						if(vldTrans.getFileAssociationId() == null) {
							pst1.setNull(i++, Types.INTEGER);							
						} else {
							pst1.setInt(i++, vldTrans.getFileAssociationId());
						}
						pst1.setString(i++, safrLogin.getUserId());
						pst1.setString(i++, safrLogin.getUserId());
						pst1.executeUpdate();
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
			pst1.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while storing View Logic Dependencies.",e);
		}
	}

	public List<ViewLogicDependencyTransfer> getViewDependecies(Integer viewId,
			Integer environmentId) throws DAOException {
		List<ViewLogicDependencyTransfer> result = new ArrayList<ViewLogicDependencyTransfer>();
		try {
			List<String> idNames = new ArrayList<String>();
			if (viewId > 0) {
				idNames.add(COL_VIEWID);
			}
			idNames.add(COL_ENVID);

			String[] columnNames = { COL_ENVID, COL_VIEWID, COL_PARENTTYPE,
					COL_PARENTID, COL_DEPENDID, COL_LOOKUPID, COL_LRFIELDID,
					COL_EXITID, COL_LFPFASSOCID, COL_CREATETIME, COL_CREATEBY,
					COL_MODIFYTIME, COL_MODIFYBY };
			List<String> columns = Arrays.asList(columnNames);

			String[] orderByNames = { COL_PARENTTYPE };
			List<String> orderBy = Arrays.asList(orderByNames);

			String selectString = generator.getSelectColumnsStatement(columns,
					params.getSchema(), TABLE_NAME, idNames, orderBy);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, viewId);
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
				result.add(generateTransfer(rs));
			}
			pst.close();
			rs.close();

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the View Dependencies for the View with id ["+ viewId + "]", e);
		}
		return result;
	}

    public List<ViewLogicDependencyTransfer> getViewSourceFilterDependencies(Integer environmentId, 
        Integer viewId, Integer viewSourceId) throws DAOException {
        return getViewSourceDependencies(environmentId, viewId, viewSourceId,LogicTextType.Extract_Record_Filter);
    }

    public List<ViewLogicDependencyTransfer> getViewSourceOutputDependencies(Integer environmentId, 
        Integer viewId, Integer viewSourceId) throws DAOException {
        return getViewSourceDependencies(environmentId, viewId, viewSourceId,LogicTextType.Extract_Record_Output);
    }
    
    protected List<ViewLogicDependencyTransfer> getViewSourceDependencies(
        Integer environmentId, Integer viewId, Integer viewSourceId, LogicTextType logicType) {
        List<ViewLogicDependencyTransfer> result = new ArrayList<ViewLogicDependencyTransfer>();
        try {
            List<String> idNames = new ArrayList<String>();
            idNames.add(COL_ENVID);
            idNames.add(COL_VIEWID);
            idNames.add(COL_PARENTID);
            idNames.add(COL_PARENTTYPE);
    
            String[] columnNames = { COL_ENVID, COL_VIEWID, COL_PARENTTYPE,
                    COL_PARENTID, COL_DEPENDID, COL_LOOKUPID, COL_LRFIELDID,
                    COL_EXITID, COL_LFPFASSOCID, COL_CREATETIME, COL_CREATEBY,
                    COL_MODIFYTIME, COL_MODIFYBY };
            List<String> columns = Arrays.asList(columnNames);
    
            String[] orderByNames = { COL_PARENTTYPE };
            List<String> orderBy = Arrays.asList(orderByNames);
    
            String selectString = generator.getSelectColumnsStatement(columns,
                    params.getSchema(), TABLE_NAME, idNames, orderBy);
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, environmentId);
                    pst.setInt(2, viewId);
                    pst.setInt(3, viewSourceId);
                    pst.setInt(4, logicType.getTypeValue());
    
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
            throw DataUtilities.createDAOException("Database error occurred while retrieving the View Source Dependencies for the View with id ["+ viewId + "]", e);
        }
        return result;
    }

    public List<ViewLogicDependencyTransfer> getViewColumnSourceDependencies(
        Integer environmentId, Integer viewId, Integer viewColumnSourceId) {
        List<ViewLogicDependencyTransfer> result = new ArrayList<ViewLogicDependencyTransfer>();
        try {
            List<String> idNames = new ArrayList<String>();
            idNames.add(COL_ENVID);
            idNames.add(COL_VIEWID);
            idNames.add(COL_PARENTID);
            idNames.add(COL_PARENTTYPE);
    
            String[] columnNames = { COL_ENVID, COL_VIEWID, COL_PARENTTYPE,
                    COL_PARENTID, COL_DEPENDID, COL_LOOKUPID, COL_LRFIELDID,
                    COL_EXITID, COL_LFPFASSOCID, COL_CREATETIME, COL_CREATEBY,
                    COL_MODIFYTIME, COL_MODIFYBY };
            List<String> columns = Arrays.asList(columnNames);
    
            String[] orderByNames = { COL_PARENTTYPE };
            List<String> orderBy = Arrays.asList(orderByNames);
    
            String selectString = generator.getSelectColumnsStatement(columns,
                    params.getSchema(), TABLE_NAME, idNames, orderBy);
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, environmentId);
                    pst.setInt(2, viewId);
                    pst.setInt(3, viewColumnSourceId);
                    pst.setInt(4, LogicTextType.Extract_Column_Assignment.getTypeValue());
    
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
            throw DataUtilities.createDAOException("Database error occurred while retrieving the View Column Source Dependencies for the View with id ["+ viewId + "]", e);
        }
        return result;
    }
    
}
