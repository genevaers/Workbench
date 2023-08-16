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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
import com.ibm.safr.we.data.dao.LookupDAO;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.LookupPathTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.internal.data.PGSQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LookupQueryBean;

/**
 * This class is used to implement the unimplemented methods of
 * <b>LookupDAODAO</b>. This class contains the methods related to Lookup Path
 * which require database access.
 * 
 */
public class PGLookupDAO implements LookupDAO {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGLookupDAO");

	private static final String TABLE_NAME = "LOOKUP";
	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "LOOKUPID";
	private static final String COL_NAME = "NAME";
	private static final String COL_SOURCE = "SRCLRID";
	private static final String COL_DESTLRLFASSOCID = "DESTLRLFASSOCID";
	private static final String COL_VALID = "VALIDIND";
	private static final String COL_COMMENTS = "COMMENTS";
	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";
    private static final String COL_ACTIVATETIME = "LASTACTTIMESTAMP";
    private static final String COL_ACTIVATEBY = "LASTACTUSERID";

	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private PGSQLGenerator generator = new PGSQLGenerator();

	public PGLookupDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	public List<LookupQueryBean> queryAllLookups(Integer environmentId,
			SortType sortType) throws DAOException {
		List<LookupQueryBean> result = new ArrayList<LookupQueryBean>();
		
		try {
            // generate lr,lf names
            Map<Integer, String> lrMap = new HashMap<Integer, String>();
            Map<Integer, String> lfMap = new HashMap<Integer, String>();
		    
	        PreparedStatement pst2 = null;
	        String sql2 = "SELECT A.LOOKUPID, C.NAME AS RECNAME, D.NAME AS FILENAME FROM "
	            + params.getSchema() + ".LOOKUP A, "
	            + params.getSchema() + ".LRLFASSOC B, "
	            + params.getSchema() + ".LOGREC C, "
	            + params.getSchema() + ".LOGFILE D "
	            + "WHERE A.ENVIRONID=B.ENVIRONID "
	            + "AND A.DESTLRLFASSOCID=B.LRLFASSOCID "
	            + "AND A.ENVIRONID= ? "
	            + "AND B.ENVIRONID=C.ENVIRONID "
	            + "AND B.LOGRECID=C.LOGRECID "
	            + "AND B.ENVIRONID=D.ENVIRONID "
	            + "AND B.LOGFILEID=D.LOGFILEID";
	        while (true) {
	            try {	        
                    pst2 = con.prepareStatement(sql2);
                    pst2.setInt(1, environmentId);
                    ResultSet rs2 = pst2.executeQuery();
                    while (rs2.next()) {
                        int joinid = rs2.getInt(COL_ID);
                        String lrName = rs2.getString("RECNAME"); 
                        lrMap.put(joinid, lrName);
                        String lfName = rs2.getString("FILENAME"); 
                        lfMap.put(joinid, lfName);
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
	        		    
	        String sortClause = null;
	        if (sortType.equals(SortType.SORT_BY_ID)) {
	            sortClause = "ORDER BY A.LOOKUPID";
	        }
	        else {
	            sortClause = "ORDER BY UPPER(A.NAME)";            
	        }
	        	        
			String selectString = "";
            boolean admin = SAFRApplication.getUserSession().isSystemAdministrator(); 			
			if (admin) {
    			selectString = "SELECT A.LOOKUPID, A.NAME AS LOOKNAME, A.VALIDIND, C.NAME AS RECNAME, A.CREATEDTIMESTAMP,"
    			    + "A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.LASTACTTIMESTAMP,A.LASTACTUSERID,COUNT(*) AS NUMSTEP  "
    				+ "FROM " + params.getSchema()+ ".LOOKUP A "
                    + "LEFT OUTER JOIN " + params.getSchema()+ ".LOOKUPSTEP B "
                    + "ON A.ENVIRONID = B.ENVIRONID AND A.LOOKUPID=B.LOOKUPID "
    				+ "LEFT OUTER JOIN " + params.getSchema()+ ".LOGREC C "
    				+ "ON A.SRCLRID = C.LOGRECID AND A.ENVIRONID = C.ENVIRONID "
    				+ " WHERE A.ENVIRONID = ? "
                    + "GROUP BY A.LOOKUPID, A.NAME, A.VALIDIND, C.NAME, A.CREATEDTIMESTAMP,"
                    + "A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.LASTACTTIMESTAMP,A.LASTACTUSERID "
    				+ sortClause;
			} else {
                selectString = "SELECT A.LOOKUPID, A.NAME AS LOOKNAME, A.VALIDIND, C.NAME AS RECNAME, D.RIGHTS,A.CREATEDTIMESTAMP,"
                    + "A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.LASTACTTIMESTAMP,A.LASTACTUSERID,COUNT(*) AS NUMSTEP  "
                    + "FROM " + params.getSchema()+ ".LOOKUP A "
                    + "LEFT OUTER JOIN " + params.getSchema()+ ".LOOKUPSTEP B "
                    + "ON A.ENVIRONID = B.ENVIRONID AND A.LOOKUPID=B.LOOKUPID "
                    + "LEFT OUTER JOIN " + params.getSchema()+ ".LOGREC C "
                    + "ON A.SRCLRID = C.LOGRECID AND A.ENVIRONID = C.ENVIRONID "
                    + "LEFT OUTER JOIN " + params.getSchema()+ ".SECLOOKUP D "
                    + "ON A.LOOKUPID = D.LOOKUPID AND A.ENVIRONID = D.ENVIRONID AND D.GROUPID= ? "
                    + " WHERE A.ENVIRONID = ? "   
                    + " GROUP BY A.LOOKUPID, A.NAME, A.VALIDIND, C.NAME, D.RIGHTS, A.CREATEDTIMESTAMP,"
                    + "A.CREATEDUSERID,A.LASTMODTIMESTAMP,A.LASTMODUSERID,A.LASTACTTIMESTAMP,A.LASTACTUSERID "
                    + sortClause;			    
			}
			
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					if (admin) {
						pst.setInt(1,  environmentId);
					} else {
						pst.setInt(1,  SAFRApplication.getUserSession().getGroup().getId());
						pst.setInt(2,  environmentId);
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
                // fill in last info 
			    int joinid = rs.getInt(COL_ID);
                String lrName = "";
                if (lrMap.containsKey(joinid)) {
                    lrName = lrMap.get(joinid);
                }
                String lfName = "";
                if (lfMap.containsKey(joinid)) {
                    lfName = lfMap.get(joinid);
                }
				LookupQueryBean lookupQueryBean = new LookupQueryBean(
					environmentId, joinid, 
					DataUtilities.trimString(rs.getString("LOOKNAME")),
					DataUtilities.trimString(rs.getString("RECNAME")), 
					rs.getInt(COL_VALID), rs.getInt("NUMSTEP"), 
					lrName, lfName, 
					admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.LookupPath, environmentId),
					rs.getDate(COL_CREATETIME), 
					DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
					rs.getDate(COL_MODIFYTIME), 
					DataUtilities.trimString(rs.getString(COL_MODIFYBY)),
                    rs.getDate(COL_ACTIVATETIME), 
                    DataUtilities.trimString(rs.getString(COL_ACTIVATEBY)));
				result.add(lookupQueryBean);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
					"Database error occurred while querying all Lookup Paths.",e);
		}
	}

    public List<LookupQueryBean> queryLookupsForBAL(Integer environmentId,
        SortType sortType) throws DAOException {
        List<LookupQueryBean> result = new ArrayList<LookupQueryBean>();
        
        try {
                        
            String sortClause = null;
            if (sortType.equals(SortType.SORT_BY_ID)) {
                sortClause = "ORDER BY A.LOOKUPID";
            }
            else {
                sortClause = "ORDER BY UPPER(A.NAME)";            
            }
                        
            String selectString = "";
            selectString = "SELECT A.LOOKUPID, A.NAME, A.VALIDIND, D.RIGHTS "
                + "FROM " + params.getSchema()+ ".LOOKUP A "
                + "LEFT OUTER JOIN " + params.getSchema()+ ".SECLOOKUP D "
                + "ON A.LOOKUPID = D.LOOKUPID AND A.ENVIRONID = D.ENVIRONID AND D.GROUPID="
                + SAFRApplication.getUserSession().getGroup().getId()
                + " WHERE A.ENVIRONID = ? "  
                + sortClause;               
            
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
                // fill in last info 
                EditRights rights = SAFRApplication.getUserSession().getEditRights(
                    rs.getInt("RIGHTS"), ComponentType.LookupPath, environmentId);
                if (rights.equals(EditRights.ReadModify) || rights.equals(EditRights.ReadModifyDelete)) {
                    int joinid = rs.getInt(COL_ID);                
                    LookupQueryBean lookupQueryBean = new LookupQueryBean(
                        environmentId, joinid, 
                        DataUtilities.trimString(rs.getString(COL_NAME)),
                        null, 
                        rs.getInt(COL_VALID), 0, 
                        null, null, 
                        SAFRApplication.getUserSession().getEditRights(
                            rs.getInt("RIGHTS"), ComponentType.LookupPath, environmentId),
                        null, null, null, null, null, null);
                    result.add(lookupQueryBean);
                }
            }
            pst.close();
            rs.close();
            return result;
    
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                    "Database error occurred while querying all Lookup Paths.",e);
        }
    }
	
	/*
	 * This function is used to generate a transfer object for the Lookup Path.
	 */
	private LookupPathTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		LookupPathTransfer lkuptrans = new LookupPathTransfer();
		lkuptrans.setEnvironmentId(rs.getInt(COL_ENVID));
		lkuptrans.setId(rs.getInt(COL_ID));
		lkuptrans.setName(DataUtilities.trimString(rs.getString(COL_NAME)));
		lkuptrans.setSourceLRId(rs.getInt(COL_SOURCE));
		lkuptrans.setTargetXLRFileId(rs.getInt(COL_DESTLRLFASSOCID));
		lkuptrans.setValidInd(DataUtilities.intToBoolean(rs.getInt(COL_VALID)));
		lkuptrans.setComments(DataUtilities.trimString(rs.getString(COL_COMMENTS)));
		lkuptrans.setCreateTime(rs.getDate(COL_CREATETIME));
		lkuptrans.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		lkuptrans.setModifyTime(rs.getDate(COL_MODIFYTIME));
		lkuptrans.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));
        lkuptrans.setActivatedTime(rs.getDate(COL_ACTIVATETIME));
        lkuptrans.setActivatedBy(DataUtilities.trimString(rs.getString(COL_ACTIVATEBY)));

		return lkuptrans;
	}

	public LookupPathTransfer getLookupPath(Integer id, Integer environmentId)
			throws DAOException {
		LookupPathTransfer result = null;
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
					int i = 1;
					pst.setInt(i++, id);
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
			if (rs.next()) {
				result = generateTransfer(rs);
			} else {
				logger.info("No such Lookup in Env " + environmentId + " with id : " + id);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the Lookup Path with id ["+ id + "]", e);
		}
		return result;
	}

	public LookupPathTransfer persistLookupPath(LookupPathTransfer lkuptrans)
			throws DAOException, SAFRNotFoundException {
		if (!lkuptrans.isPersistent()) {
			return (createLookupPath(lkuptrans));
		} else {
			return (updateLookupPath(lkuptrans));
		}
	}

	/**
	 * This method is to create the Lookup Path in LOOKUP
	 * 
	 * @param lkuptrans
	 *            : The transfer object which contains the values which are to
	 *            be set in the columns for the corresponding Lookup Path which
	 *            is being created.
	 * @return The transfer object which contains the values which are received
	 *         from the LOOKUP for the Lookup Path which is created.
	 * @throws DAOException
	 */
	private LookupPathTransfer createLookupPath(LookupPathTransfer lkuptrans)
			throws DAOException {
		
		boolean isImportOrMigrate = lkuptrans.isForImport()
				|| lkuptrans.isForMigration() ? true : false;
		boolean useCurrentTS = !isImportOrMigrate;

		try {
			String[] columnNames = { COL_ENVID, COL_NAME, COL_SOURCE,
					COL_DESTLRLFASSOCID, COL_VALID, COL_COMMENTS, COL_CREATETIME,
					COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = new ArrayList<String>(Arrays.asList(columnNames));

            if (isImportOrMigrate) {
                names.add(1, COL_ID);
                names.add(COL_ACTIVATETIME);
                names.add(COL_ACTIVATEBY);
            } else {
                if (lkuptrans.isValidInd()) {
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
					pst.setInt(i++, lkuptrans.getEnvironmentId());
		            if (isImportOrMigrate) {
		                pst.setInt(i++, lkuptrans.getId());
		            }
					pst.setString(i++, lkuptrans.getName());
					pst.setInt(i++, lkuptrans.getSourceLRId());
					pst.setInt(i++, lkuptrans.getTargetXLRFileId());
					pst.setInt(i++, DataUtilities.booleanToInt(lkuptrans.isValidInd()));
					pst.setString(i++, lkuptrans.getComments());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lkuptrans.getCreateTime()));
					}
					pst.setString(i++,isImportOrMigrate ? lkuptrans.getCreateBy(): safrLogin.getUserId());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lkuptrans.getModifyTime()));
					}
					pst.setString(i++,isImportOrMigrate ? lkuptrans.getModifyBy(): safrLogin.getUserId());
                    if (isImportOrMigrate) {
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(lkuptrans.getActivatedTime()));
                        pst.setString(i++, lkuptrans.getActivatedBy());
                    }
                    else  {
                        if (lkuptrans.isValidInd()) {
                            pst.setString(i++, safrLogin.getUserId());
                        }
                    }
                    rs = pst.executeQuery();
                    rs.next();
                    int id = rs.getInt(1);          
                    lkuptrans.setId(id);
                    lkuptrans.setPersistent(true);
                    if (!isImportOrMigrate) {
                        lkuptrans.setCreateBy(safrLogin.getUserId());
                        lkuptrans.setCreateTime(rs.getDate(2));
                        lkuptrans.setModifyBy(safrLogin.getUserId());
                        lkuptrans.setModifyTime(rs.getDate(3));   
                        if (lkuptrans.isValidInd()) {
                            lkuptrans.setActivatedBy(safrLogin.getUserId());
                            lkuptrans.setActivatedTime(rs.getDate(4));                               
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
			throw DataUtilities.createDAOException("Database error occurred while creating a new Lookup Path.",e);
		}
		return lkuptrans;
	}

	/**
	 * This method is to update the Lookup Path in LOOKUP
	 * 
	 * @param lkuptrans
	 *            : The transfer object which contains the values which are to
	 *            be set in the columns for the corresponding Lookup Path which
	 *            is being updated.
	 * @return The transfer object which contains the values which are received
	 *         from the LOOKUP for the Lookup Path which is updated.
	 * @throws DAOException
	 * @throws SAFRNotFoundException
	 */

	private LookupPathTransfer updateLookupPath(LookupPathTransfer lkuptrans)
			throws DAOException, SAFRNotFoundException {
		
		boolean isImportOrMigrate = lkuptrans.isForImport()
				|| lkuptrans.isForMigration() ? true : false;
		boolean useCurrentTS = !isImportOrMigrate;

		try {
			String[] columnNames = { COL_NAME, COL_SOURCE, COL_DESTLRLFASSOCID,
					COL_VALID, COL_COMMENTS };
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
                if (lkuptrans.isUpdated()) {
                    names.add(COL_MODIFYTIME);
                    names.add(COL_MODIFYBY);
                }
                if (lkuptrans.isActivated()) {
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
					pst.setString(i++, lkuptrans.getName());
					pst.setInt(i++, lkuptrans.getSourceLRId());
					pst.setInt(i++, lkuptrans.getTargetXLRFileId());
					pst.setInt(i++, DataUtilities.booleanToInt(lkuptrans.isValidInd()));
					pst.setString(i++, lkuptrans.getComments());
                    if (isImportOrMigrate) {
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(lkuptrans.getCreateTime()));
                        pst.setString(i++, lkuptrans.getCreateBy());
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(lkuptrans.getModifyTime()));
                        pst.setString(i++, lkuptrans.getModifyBy());
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(lkuptrans.getActivatedTime()));
                        pst.setString(i++, lkuptrans.getActivatedBy());
                    } else {
                        if (lkuptrans.isUpdated()){
                            pst.setString(i++, safrLogin.getUserId());
                        }
                        if (lkuptrans.isActivated()) {
                            pst.setString(i++, safrLogin.getUserId());                            
                        }
                    }
					pst.setInt(i++, lkuptrans.getId());
					pst.setInt(i++, lkuptrans.getEnvironmentId());
                    if ( useCurrentTS && 
                        (lkuptrans.isUpdated() || lkuptrans.isActivated())) {
                        ResultSet rs = pst.executeQuery();
                        rs.next();
                        int j=1;
                        if (lkuptrans.isUpdated()) {
                            lkuptrans.setModifyBy(safrLogin.getUserId());
                            lkuptrans.setModifyTime(rs.getDate(j++));
                            lkuptrans.setUpdated(false);
                        }
                        if (lkuptrans.isActivated()) {
                            lkuptrans.setActivatedBy(safrLogin.getUserId());
                            lkuptrans.setActivatedTime(rs.getDate(j++));
                            lkuptrans.setActivated(false);
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
			throw DataUtilities.createDAOException("Database error occurred while updating the Lookup Path.",e);
		}
		return lkuptrans;
	}

	public void removeLookupPath(Integer id, Integer environmentId)
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
					String[] tables = { "LOOKUPSRCKEY", "SECLOOKUP", "LOOKUPSTEP", TABLE_NAME };
					for (String table : tables) {
						String statement = generator.getDeleteStatement(
								params.getSchema(), table, idNames);
						PreparedStatement pst = null;

						while (true) {
							try {
								pst = con.prepareStatement(statement);
								pst.setInt(1, id);
								pst.setInt(2, environmentId);
								pst.execute();
								break;
							} catch (SQLException se) {
								if (con.isClosed()) {
									// lost database connection, so reconnect
									// and retry
									con = DAOFactoryHolder.getDAOFactory()
											.reconnect();
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
					if (DAOFactoryHolder.getDAOFactory().getDAOUOW().isMultiComponentScope()) {
						throw e;
					} else {
						continue;
					}
				} catch (SQLException e) {
					throw DataUtilities.createDAOException("Database error occurred while deleting the Lookup Path.",e);
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

	public LookupPathTransfer getDuplicateLookupPath(String lookupName,
			Integer lookupId, Integer environmentId) throws DAOException {
		LookupPathTransfer result = null;
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
					pst.setString(i++, lookupName.toUpperCase());
					pst.setInt(i++, lookupId);
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
				logger.info("Existing Lookup with name '" + lookupName
						+ "' found in Environment [" + environmentId + "]");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving a duplicate Lookup Path.",e);
		}
		return result;
	}

	public List<DependentComponentTransfer> getLookupPathViewDependencies(
			Integer environmentId, Integer lookupPathId,
			Set<Integer> exceptionList) throws DAOException {
		List<DependentComponentTransfer> dependentViews = new ArrayList<DependentComponentTransfer>();
		try {

			String placeholders = "";
			if(exceptionList != null) {
				placeholders = generator.getPlaceholders(exceptionList.size());
			}

			String schema = params.getSchema();
			// CQ 8056. Nikita. 16/07/2010. Show location in dependency error
			// message

			// Query 1: those views where Lookup Path is used in a column source
			String selectString = "SELECT A.VIEWID, A.NAME, A.VIEWSTATUSCD, C.COLUMNNUMBER, C.HDRLINE1 FROM "
					+ schema + ".VIEW A , "
					+ schema + ".VIEWCOLUMNSOURCE B, "
					+ schema + ".VIEWCOLUMN C ";
			if(placeholders.length() > 0) {
				selectString += "WHERE A.VIEWID NOT IN ( " + placeholders + ") AND ";
			} else {
				selectString += "WHERE ";
			}
			selectString += " A.VIEWID = B.VIEWID AND B.ENVIRONID = A.ENVIRONID AND "
					+ "B.VIEWID=C.VIEWID AND B.ENVIRONID=C.ENVIRONID AND B.VIEWCOLUMNID=C.VIEWCOLUMNID "
					+ "AND A.ENVIRONID = ? "
					+ " AND (B.LOOKUPID = ? )";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					if(placeholders.length() > 0) {
						Iterator<Integer> ei = exceptionList.iterator();
						while(ei.hasNext()) {
							Integer e = ei.next();
							pst.setInt(ndx++,  e);
						}
					}					
					pst.setInt(ndx++,  environmentId);
					pst.setInt(ndx++,  lookupPathId);
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
				String status = rs.getString("VIEWSTATUSCD");
				if ("ACTVE".equals(status)) {
					depCompTransfer.setActive(true);
				} else {
					depCompTransfer.setActive(false);
				}
				dependentViews.add(depCompTransfer);
			}

			// ---------------------------------------------------------------------------------------------------------
			// Query 2 :those views where Lookup Path is used in the sort key
			// title of a sort key
			selectString = "SELECT DISTINCT C.VIEWCOLUMNID, A.VIEWID, A.NAME, A.VIEWSTATUSCD, C.COLUMNNUMBER, C.HDRLINE1 FROM "
					+ schema + ".VIEW A , "
					+ schema + ".VIEWCOLUMNSOURCE B, "
					+ schema + ".VIEWCOLUMN C ";
					if(placeholders.length() > 0) {
						selectString += "WHERE A.VIEWID NOT IN ( " + placeholders + ") AND ";
					} else {
						selectString += "WHERE ";
					}
					selectString +=  " A.VIEWID = B.VIEWID AND B.ENVIRONID = A.ENVIRONID AND "
					+ "B.VIEWID=C.VIEWID AND B.ENVIRONID=C.ENVIRONID AND B.VIEWCOLUMNID=C.VIEWCOLUMNID "
					+ "AND A.ENVIRONID = ? "
					+ " AND B.SORTTITLELOOKUPID = ? ";
			pst.close();
			pst = null;
			rs.close();
			rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					if(placeholders.length() > 0) {
						Iterator<Integer> ei = exceptionList.iterator();
						while(ei.hasNext()) {
							Integer e = ei.next();
							pst.setInt(ndx++,  e);
						}
					}					
					pst.setInt(ndx++,  environmentId);
					pst.setInt(ndx++,  lookupPathId);
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
				String status = rs.getString("VIEWSTATUSCD");
				if ("ACTVE".equals(status)) {
					depCompTransfer.setActive(true);
				} else {
					depCompTransfer.setActive(false);
				}
				dependentViews.add(depCompTransfer);
			}

			// Query 3 : those views in which this lookup path is used in a
			// View's Extract Column Assignment
			selectString = "SELECT DISTINCT C.VIEWCOLUMNID, A.VIEWID, A.NAME, A.VIEWSTATUSCD, C.COLUMNNUMBER,C.HDRLINE1 FROM "
					+ schema + ".VIEW A, "
					+ schema + ".VIEWLOGICDEPEND B,"
					+ schema + ".VIEWCOLUMN C,"
					+ schema + ".VIEWCOLUMNSOURCE D ";
					if(placeholders.length() > 0) {
						selectString += "WHERE A.VIEWID NOT IN ( " + placeholders + ") AND ";
					} else {
						selectString += "WHERE ";
					}
			selectString +=  " A.ENVIRONID =B.ENVIRONID AND B.ENVIRONID = C.ENVIRONID AND C.ENVIRONID = D.ENVIRONID AND "
					+ "B.PARENTID = D.VIEWCOLUMNSOURCEID AND D.VIEWCOLUMNID=C.VIEWCOLUMNID AND "
					+ "B.VIEWID = C.VIEWID AND C.VIEWID = D.VIEWID AND B.LOGICTYPECD = 2 AND "
					+ "A.VIEWID = B.VIEWID AND A.ENVIRONID = ? "
					+ " AND B.LOOKUPID = ? ";

			pst.close();
			pst = null;
			rs.close();
			rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					if(placeholders.length() > 0) {
						Iterator<Integer> ei = exceptionList.iterator();
						while(ei.hasNext()) {
							Integer e = ei.next();
							pst.setInt(ndx++,  e);
						}
					}					
					pst.setInt(ndx++,  environmentId);
					pst.setInt(ndx++,  lookupPathId);
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
				String status = rs.getString("VIEWSTATUSCD");
				if ("ACTVE".equals(status)) {
					depCompTransfer.setActive(true);
				} else {
					depCompTransfer.setActive(false);
				}
				dependentViews.add(depCompTransfer);
			}

			// Query 4 : those views in which this lookup path is used in a
			// View's Extract Record Filter
			selectString = "SELECT DISTINCT C.VIEWSOURCEID, A.VIEWID, A.NAME, A.VIEWSTATUSCD, C.SRCSEQNBR FROM "
					+ schema + ".VIEW A, "
					+ schema + ".VIEWLOGICDEPEND B,"
					+ schema + ".VIEWSOURCE C ";
					if(placeholders.length() > 0) {
						selectString += "WHERE A.VIEWID NOT IN ( " + placeholders + ") AND ";
					} else {
						selectString += "WHERE ";
					}
					selectString +=   " A.ENVIRONID =B.ENVIRONID AND B.ENVIRONID = C.ENVIRONID AND "
					+ "B.PARENTID = C.VIEWSOURCEID AND B.LOGICTYPECD = 1 AND "
					+ "A.VIEWID = B.VIEWID AND B.VIEWID = C.VIEWID"
					+ " AND A.ENVIRONID = ? "
					+ " AND B.LOOKUPID = ? ";

			pst.close();
			pst = null;
			rs.close();
			rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					if(placeholders.length() > 0) {
						Iterator<Integer> ei = exceptionList.iterator();
						while(ei.hasNext()) {
							Integer e = ei.next();
							pst.setInt(ndx++,  e);
						}
					}					
					pst.setInt(ndx++,  environmentId);
					pst.setInt(ndx++,  lookupPathId);
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
				depCompTransfer.setDependencyInfo("[Src " + rs.getInt("SRCSEQNBR") + ", Logic Text]");
				String status = rs.getString("VIEWSTATUSCD");
				if ("ACTVE".equals(status)) {
					depCompTransfer.setActive(true);
				} else {
					depCompTransfer.setActive(false);
				}
				dependentViews.add(depCompTransfer);
			}

            // Query 5 : those views in which this lookup path is used in a
            // View's Extract Output
            selectString = 
                "SELECT DISTINCT C.VIEWSOURCEID, A.VIEWID, A.NAME, A.VIEWSTATUSCD, C.SRCSEQNBR FROM " + 
                params.getSchema() + ".VIEW A, " +
                params.getSchema() + ".VIEWLOGICDEPEND B, " +
                params.getSchema() + ".VIEWSOURCE C " +
                "WHERE A.ENVIRONID=B.ENVIRONID " +
                "AND A.VIEWID=B.VIEWID " +
                "AND B.ENVIRONID=C.ENVIRONID " +
                "AND B.PARENTID=C.VIEWSOURCEID " +
                "AND B.LOGICTYPECD=5 " +
                "AND B.ENVIRONID= ? " +
                " AND B.LOOKUPID= ? ";

            pst.close();
            pst = null;
            rs.close();
            rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, environmentId );
                    pst.setInt(2, lookupPathId );
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
                depCompTransfer.setDependencyInfo("[Output " + rs.getInt("SRCSEQNBR") + ", Logic Text]");
                String status = rs.getString("VIEWSTATUSCD");
                if ("ACTVE".equals(status)) {
                    depCompTransfer.setActive(true);
                } else {
                    depCompTransfer.setActive(false);
                }
                dependentViews.add(depCompTransfer);
            }
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving Views dependent on the Lookup Path.",e);
		}
		return dependentViews;
	}

	public List<DependentComponentTransfer> getLookupPathInactiveLogicalRecordsDependencies(
			Integer environmentId, Integer lookupPathId) throws DAOException {
		List<DependentComponentTransfer> inactiveLogicalRecords = new ArrayList<DependentComponentTransfer>();
		try {
			String schema = params.getSchema();
			String selectString = "Select A.LOGRECID, A.NAME FROM "
					+ schema + ".LOGREC A , "
					+ schema + ".LOOKUPSTEP  B "
					+ "Where B.LOOKUPID = ? "
					+ " AND B.ENVIRONID = ? "
					+ " AND B.SRCLRID = A.LOGRECID AND A.ENVIRONID = ? "
					+ " AND UPPER(A.LRSTATUSCD) = 'INACT' "
					+ "UNION "
					+ "SELECT XL.LOGRECID, E.NAME FROM "
					+ schema + ".LRLFASSOC XL , "
					+ schema + ".LOGREC E , "
					+ schema + ".LOOKUPSTEP XJ WHERE XJ.LOOKUPID = ? "
					+ " AND XJ.LRLFASSOCID = XL.LRLFASSOCID "
					+ "AND E.LOGRECID = XL.LOGRECID AND XL.ENVIRONID = ? "
					+ " AND E.ENVIRONID = XL.ENVIRONID AND XL.ENVIRONID = XJ.ENVIRONID "
					+ "AND E.LRSTATUSCD = 'INACT'";

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
                    pst.setInt(1, lookupPathId );
                    pst.setInt(2, environmentId );
                    pst.setInt(3, environmentId );
                    pst.setInt(4, lookupPathId );
                    pst.setInt(5, environmentId );
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
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the Logical Records which are currently inactive and used in the Lookup Path.",e);
		}
		return inactiveLogicalRecords;
	}

	public void makeLookupPathsActive(List<Integer> lookupPathIds,
			Integer environmentId) throws DAOException {
		if (lookupPathIds == null || lookupPathIds.isEmpty()) {
			return;
		}
		String placeholders = generator.getPlaceholders(lookupPathIds.size());
		try {
			String statementLookups = "Update "
					+ params.getSchema()
					+ ".LOOKUP Set VALIDIND = 1, LASTACTTIMESTAMP = CURRENT_TIMESTAMP, LASTACTUSERID = ? "
					+ "Where LOOKUPID IN (" + placeholders + " ) AND ENVIRONID = ? ";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statementLookups);
					pst.setString(1, safrLogin.getUserId());
					int ndx = 2;
					for(int i=0; i<lookupPathIds.size(); i++) {
						pst.setInt(ndx++, lookupPathIds.get(i));
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

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while while making the Lookup Paths active.",e);
		}

	}

	public void makeLookupPathsInactive(Collection<Integer> lookupPathIds,
			Integer environmentId) throws DAOException {
		if (lookupPathIds == null || lookupPathIds.isEmpty()) {
			return;
		}
		String placeholders = generator.getPlaceholders(lookupPathIds.size());
		try {
			String statementLookups = "Update "
					+ params.getSchema()+ ".LOOKUP "
					+ "Set VALIDIND = 0, LASTACTTIMESTAMP = CURRENT_TIMESTAMP, LASTACTUSERID = ? "
					+ "Where LOOKUPID IN (" + placeholders 
					+ ") AND ENVIRONID = ? " 
					+ " AND VALIDIND = 1";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statementLookups);
					pst.setString(1, safrLogin.getUserId());
					int ndx = 2;
					Iterator<Integer> lpi = lookupPathIds.iterator();
					while(lpi.hasNext()) {
						Integer lp = lpi.next();
						pst.setInt(ndx++, lp);
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
			throw DataUtilities.createDAOException(
			    "Database error occurred while while making the Lookup Paths inactive.",e);
		}

	}

	public LookupQueryBean queryLookupPath(Integer lookupPathId,
			Integer environmentId) throws DAOException {
		LookupQueryBean lookupPathQueryBean = null;
		try {
		    String selectString;
            boolean admin = SAFRApplication.getUserSession().isSystemAdministrator();           
            if (admin) {
                selectString = "Select A.LOOKUPID, A.NAME From "
					+ params.getSchema() + ".LOOKUP A Where A.LOOKUPID = ? "
					+ " AND A.ENVIRONID = ? ";
            }
            else {
                selectString = "Select A.LOOKUPID, A.NAME, B.RIGHTS "
                    + "From " + params.getSchema() + ".LOOKUP A "
                    + "LEFT OUTER JOIN " + params.getSchema() + ".SECLOOKUP B "
                    + "ON A.LOOKUPID = B.LOOKUPID AND A.ENVIRONID = B.ENVIRONID AND B.GROUPID= ? "
                    + "Where A.LOOKUPID = ? "
                    + "AND A.ENVIRONID = ? ";                
            }
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					if(admin) {
						pst.setInt(1,  lookupPathId);
						pst.setInt(2,  environmentId);
					} else {
						pst.setInt(1,  SAFRApplication.getUserSession().getGroup().getId());
						pst.setInt(2,  lookupPathId);
						pst.setInt(3,  environmentId);
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
			if (rs.next()) {
				lookupPathQueryBean = new LookupQueryBean(environmentId, 
				    rs.getInt("LOOKUPID"), 
				    DataUtilities.trimString(rs.getString("NAME")), 
				    null, 0, 0, null, null, 
                    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.LookupPath, environmentId),
				    null, null, null, null, null, null);

			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while querying the Lookup Path with the specified ID.",e);
		}
		return lookupPathQueryBean;
	}

	public Boolean isSameTarget(List<Integer> lookupPathIds,
			Integer environmentId) throws DAOException {
		Boolean result = false;
		if (lookupPathIds == null || lookupPathIds.size() == 0) {
			return result;
		}
		try {
			String placeholders = generator.getPlaceholders(lookupPathIds.size());
			String selectString = "Select COUNT(DISTINCT DESTLRLFASSOCID) From "
					+ params.getSchema() + ".LOOKUP Where LOOKUPID IN ("
					+ placeholders + " ) AND ENVIRONID = ? ";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int ndx = 1;
					for(int i=0; i<lookupPathIds.size(); i++){
						pst.setInt(ndx++,  lookupPathIds.get(i));
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
			if (rs.next()) {
				if (rs.getInt(1) == 1) {
					result = true;
				}
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while checking if all the Lookup Paths are using the same LR-LF association as their target.",e);
		}
		return result;
	}

	@Override
	public Integer getLookupPath(String name, Integer environID) {
		Integer result = null;
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
					int i = 1;
					pst.setString(i++, name);
					pst.setInt(i++, environID);
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
				result = rs.getInt(2); //Note the lookup id is in the second column
			} else {
				logger.info("No such Lookup in Env " + environID + " with name : " + name);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the Lookup Path " + name, e);
		}
		return result;
	}

	@Override
	public Map<String, Integer> getTargetFields(String name, Integer environID) {
		try {
			Map<String, Integer> fields = new TreeMap<>();
			String schema = params.getSchema();
			String selectString = "select l.name, l.lookupid, f.LRFIELDID, f.NAME "
					+ "from " + schema + ".lookup l "
					+ "join " + schema + ".lrlfassoc a "
					+ "on a.environid=l.environid and l.destlrlfassocid=a.lrlfassocid "
					+ "join " + schema + ".LRFIELD f "
					+ "on l.environid=f.environid and a.logrecid=f.logrecid "
					+ "where l.name= ? and f.environid= ?" ;

			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int i = 1;
					pst.setString(i++, name);
					pst.setInt(i++, environID);
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
			int row = 0;
			while (rs.next()) {
				if(row == 0) {
					fields.put("Lookup_ID", rs.getInt(2));
				}
				fields.put(rs.getString(4), rs.getInt(3));
				row++;
			}
			pst.close();
			rs.close();
			return fields;
		} catch (SQLException e) {
			String msg = "Database error occurred while retrieving Lookup " + name + " fields from Environment [" + environID + "].";
			throw DataUtilities.createDAOException(msg, e);
		}
	}

}
