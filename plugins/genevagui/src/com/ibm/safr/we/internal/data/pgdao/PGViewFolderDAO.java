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
import java.util.List;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.ViewFolderDAO;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.internal.data.PGSQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;

/**
 * This class is used to implement the unimplemented methods of
 * <b>ViewFolderDAO</b>. This class contains the methods to related to View
 * Folders which require database access.
 */
public class PGViewFolderDAO implements ViewFolderDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGViewFolderDAO");

	private static final String TABLE_NAME = "VIEWFOLDER";
    
	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "VIEWFOLDERID";
	private static final String COL_NAME = "NAME";
	private static final String COL_COMMENT = "COMMENTS";
	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";

	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private PGSQLGenerator generator = new PGSQLGenerator();

	public PGViewFolderDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrlogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrlogin;
	}

	public List<ViewFolderQueryBean> queryAllViewFolders(Integer environmentId,
			SortType sortType)
			throws DAOException {
		List<ViewFolderQueryBean> result = new ArrayList<ViewFolderQueryBean>();

		boolean admin = SAFRApplication.getUserSession().isSystemAdministrator();

		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "VF.VIEWFOLDERID";
		} else {
			orderString = "UPPER(VF.NAME)";
		}

		try {
			String selectString = "";
			if (admin) {

				selectString = "SELECT VF.VIEWFOLDERID,VF.NAME,  "
						+ "VF.CREATEDTIMESTAMP, VF.CREATEDUSERID, VF.LASTMODTIMESTAMP, VF.LASTMODUSERID FROM "
						+ params.getSchema() + ".VIEWFOLDER VF "
						+ "WHERE VF.ENVIRONID = ? " 
						+ " ORDER BY " + orderString;
			} else {
				selectString = "SELECT VF.VIEWFOLDERID,VF.NAME, L.RIGHTS, "
						+ "VF.CREATEDTIMESTAMP, VF.CREATEDUSERID, VF.LASTMODTIMESTAMP, VF.LASTMODUSERID FROM "
						+ params.getSchema() + ".VIEWFOLDER VF "
						+ "LEFT OUTER JOIN "
						+ params.getSchema() + ".SECVIEWFOLDER L "
						+ "ON VF.ENVIRONID = L.ENVIRONID AND VF.VIEWFOLDERID = L.VIEWFOLDERID "
						+ " AND L.GROUPID=" + SAFRApplication.getUserSession().getGroup().getId() + " "
						+ "WHERE VF.ENVIRONID = ? " 
						+ " ORDER BY " + orderString;
			}
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
				ViewFolderQueryBean viewFolderBean = new ViewFolderQueryBean(
					environmentId, rs.getInt(COL_ID), 
					DataUtilities.trimString(rs.getString(COL_NAME)),
                    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.ViewFolder, environmentId),					
					rs.getDate(COL_CREATETIME), 
					DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
					rs.getDate(COL_MODIFYTIME), 
					DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

				result.add(viewFolderBean);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while querying all View Folders.",e);
		}

	}

	public List<ViewFolderQueryBean> queryViewFolders(Integer environmentId,
			Integer groupId, boolean isSystemAdmin, SortType sortType)
			throws DAOException {
		List<ViewFolderQueryBean> result = new ArrayList<ViewFolderQueryBean>();

		String orderString = null;
		if (sortType.equals(SortType.SORT_BY_ID)) {
			orderString = "VF.VIEWFOLDERID";
		} else {
			orderString = "UPPER(VF.NAME)";
		}

		try {
			String selectString = "";
			if (isSystemAdmin) {
				selectString = "SELECT VF.VIEWFOLDERID,VF.NAME, "
						+ "VF.CREATEDTIMESTAMP, VF.CREATEDUSERID, VF.LASTMODTIMESTAMP, VF.LASTMODUSERID FROM "
						+ params.getSchema() + ".VIEWFOLDER VF "
						+ "WHERE VF.ENVIRONID = ? "
						+ " ORDER BY " + orderString;
			} else {
				selectString = "SELECT VF.VIEWFOLDERID,VF.NAME, L.RIGHTS, "
						+ "VF.CREATEDTIMESTAMP, VF.CREATEDUSERID, VF.LASTMODTIMESTAMP, VF.LASTMODUSERID FROM "
						+ params.getSchema() + ".VIEWFOLDER VF "
						+ "LEFT OUTER JOIN "
						+ params.getSchema() + ".SECVIEWFOLDER L "
						+ "ON VF.ENVIRONID = L.ENVIRONID AND VF.VIEWFOLDERID = L.VIEWFOLDERID "
						+ " AND L.GROUPID = ? "
						+ "WHERE VF.ENVIRONID = ? " 
						+ " ORDER BY "
						+ orderString;
			}
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					if(isSystemAdmin) {
						pst.setInt(1, environmentId);
					}
					else {
						pst.setInt(1, groupId);
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
				ViewFolderQueryBean viewFolderBean = new ViewFolderQueryBean(
					environmentId, rs.getInt(COL_ID), 
					DataUtilities.trimString(rs.getString(COL_NAME)),
					isSystemAdmin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.ViewFolder, environmentId),                    
					rs.getDate(COL_CREATETIME), 
					DataUtilities.trimString(rs.getString(COL_CREATEBY)), 
					rs.getDate(COL_MODIFYTIME), 
					DataUtilities.trimString(rs.getString(COL_MODIFYBY)));
				result.add(viewFolderBean);
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying View Folders for the specified Group and/or Environment.",e);
		}
	}

	public ViewFolderTransfer getViewFolder(Integer id, Integer environmentId)
			throws DAOException {
		ViewFolderTransfer result = null;
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
				logger.info("No such View Folder in Env " + environmentId + " with id : "+ id);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the View Folder with id ["+ id + "]", e);
		}
		return result;
	}

	/**
	 * This method is used to generate a transfer object for the View Folder
	 * 
	 * @param rs
	 *            : The resultset of a database query run on VIEWFOLDER table
	 *            with which the values for the transfer objects are set.
	 * @return A transfer object for the View Folder with values set according
	 *         to the resultset.
	 * @throws SQLException
	 */
	private ViewFolderTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		ViewFolderTransfer viewFolder = new ViewFolderTransfer();
		viewFolder.setEnvironmentId(rs.getInt(COL_ENVID));
		viewFolder.setId(rs.getInt(COL_ID));
		viewFolder.setName(DataUtilities.trimString(rs.getString(COL_NAME)));
		viewFolder.setComments(DataUtilities.trimString(rs.getString(COL_COMMENT)));
		viewFolder.setCreateTime(rs.getDate(COL_CREATETIME));
		viewFolder.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		viewFolder.setModifyTime(rs.getDate(COL_MODIFYTIME));
		viewFolder.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

		return viewFolder;
	}

	public ViewFolderTransfer persistViewFolder(ViewFolderTransfer viewFolder)
			throws DAOException, SAFRNotFoundException {
		if (!viewFolder.isPersistent()) {
			return (createViewFolder(viewFolder));
		} else {
			return (updateViewFolder(viewFolder));
		}
	}

	/**
	 * This method is used to create a View Folder in VIEWFOLDER table
	 * 
	 * @param viewFolder
	 *            : The transfer object which contains the values which are to
	 *            be set in the columns for the corresponding View Folder which
	 *            is being created.
	 * @return The transfer object which contains the values which are received
	 *         from the VIEWFOLDER for the View Folder which is created.
	 * @throws DAOException
	 */
	private ViewFolderTransfer createViewFolder(ViewFolderTransfer viewFolder)
			throws DAOException {
		try {
			String[] columnNames = { COL_ENVID, COL_NAME, COL_COMMENT, 
			        COL_CREATETIME, COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
            if (viewFolder.isForImportOrMigration()) {
                names.add(1, COL_ID);
            }
			String statement = generator.getInsertStatement(params.getSchema(),
					TABLE_NAME, COL_ID, names, !viewFolder.isForImportOrMigration());
			PreparedStatement pst = null;
            ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);

					int i = 1;
					pst.setInt(i++, viewFolder.getEnvironmentId());
		            if (viewFolder.isForImportOrMigration()) {
		                pst.setInt(i++, viewFolder.getId());
		            }
					pst.setString(i++, viewFolder.getName());
					pst.setString(i++, viewFolder.getComments());
                    if (viewFolder.isForImportOrMigration()) {
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewFolder.getCreateTime()));
                    } 
                    pst.setString(i++, viewFolder.isForImportOrMigration() ? viewFolder.getCreateBy() : safrLogin.getUserId());
                    if (viewFolder.isForImportOrMigration()) {
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewFolder.getModifyTime()));
                    }
                    pst.setString(i++,viewFolder.isForImportOrMigration() ? viewFolder.getModifyBy() : safrLogin.getUserId());					
                    rs = pst.executeQuery();
                    rs.next();
                    int id = rs.getInt(1); 
                    viewFolder.setPersistent(true);
                    viewFolder.setId(id);
                    if (!viewFolder.isForImportOrMigration()) {
                        viewFolder.setCreateBy(safrLogin.getUserId());
                        viewFolder.setCreateTime(rs.getDate(2));
                        viewFolder.setModifyBy(safrLogin.getUserId());
                        viewFolder.setModifyTime(rs.getDate(3));
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
			throw DataUtilities.createDAOException("Database error occurred while creating a new View Folder.",e);
		}
		return viewFolder;
	}

	/**
	 * This method is used to update a View Folder in VIEWFOLDER table
	 * 
	 * @param viewFolder
	 *            : The transfer object which contains the values which are to
	 *            be set in the columns for the corresponding View Folder which
	 *            is being updated.
	 * @return The transfer object which contains the values which are received
	 *         from the VIEWFOLDER for the View Folder which is updated.
	 * @throws DAOException
	 * @throws SAFRNotFoundException
	 */
	private ViewFolderTransfer updateViewFolder(ViewFolderTransfer viewFolder)
			throws DAOException, SAFRNotFoundException {
        boolean isImportOrMigrate = viewFolder.isForImport()
            || viewFolder.isForMigration() ? true : false;
        boolean useCurrentTS = !isImportOrMigrate;
		try {
			String[] columnNames = { COL_NAME, COL_COMMENT, COL_MODIFYTIME,
					COL_MODIFYBY };
			List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
			
            if (isImportOrMigrate) {
                names.add(COL_CREATETIME);
                names.add(COL_CREATEBY);
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
					pst.setString(i++, viewFolder.getName());
					pst.setString(i++, viewFolder.getComments());
                    if (isImportOrMigrate) {
                        // createby and lastmod set from source component
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewFolder.getCreateTime()));
                        pst.setString(i++, viewFolder.getCreateBy());
                        pst.setTimestamp(i++, DataUtilities.getTimeStamp(viewFolder.getModifyTime()));
                        pst.setString(i++, viewFolder.getModifyBy());
                    } else {
                        // createby details are untouched
                        // lastmodtimestamp is CURRENT_TIMESTAMP
                        // lastmoduserid is logged in user
                        pst.setString(i++, safrLogin.getUserId());
                    }
					
					pst.setInt(i++, viewFolder.getId());
					pst.setInt(i++, viewFolder.getEnvironmentId());
					
                    if (useCurrentTS) {
                        ResultSet rs = pst.executeQuery();
                        rs.next();
                        viewFolder.setModifyTime(rs.getDate(1)); 
                        viewFolder.setModifyBy(safrLogin.getUserId());
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
			throw DataUtilities.createDAOException("Database error occurred while updating the View Folder.",e);
		}
		return viewFolder;
	}

	public void removeViewFolder(Integer id, Integer environmentId)
			throws DAOException {
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			idNames.add(COL_ENVID);

			// removing its association with any Group
			String deleteAssocQuery = generator.getDeleteStatement(params
					.getSchema(), "SECVIEWFOLDER", idNames);
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(deleteAssocQuery);

					pst.setInt(1, id);
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

            // removing its association with any Group
            String deleteViewQuery = generator.getDeleteStatement(params
                    .getSchema(), "VFVASSOC", idNames);

            while (true) {
                try {
                    pst = con.prepareStatement(deleteViewQuery);

                    pst.setInt(1, id);
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
			
			String statement = generator.getDeleteStatement(params.getSchema(),
					TABLE_NAME, idNames);
			pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);

					pst.setInt(1, id);
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

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while deleting the View Folder.",e);
		}
	}

	public ViewFolderTransfer getDuplicateViewFolder(String viewFolderName,
			Integer viewFolderId, Integer environmentId) throws DAOException {
		ViewFolderTransfer result = null;
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
					pst.setString(i++, viewFolderName.toUpperCase());
					pst.setInt(i++, viewFolderId);
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
				logger.info("Existing View Folder with name '" + viewFolderName
						+ "' found in Environment [" + environmentId + "]");
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving a duplicate View Folder.",e);
		}
		return result;
	}

	public Integer getCountOfViewsInViewFolder(Integer viewFolderId,
			Integer environmentId) throws DAOException {
		Integer count = 0;
		try {

			String statement = "Select Count(VIEWID) from "
					+ params.getSchema() + ".VFVASSOC where ENVIRONID =? AND VIEWFOLDERID =?";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int i = 1;
					pst.setInt(i++, environmentId);
					pst.setInt(i++, viewFolderId);
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
				count = rs.getInt(1);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving count of views in View Folder.",e);
		}
		return count;
	}

	public List<DependentComponentTransfer> getUserDependencies(
			Integer viewFolderId) throws DAOException {
		List<DependentComponentTransfer> userDependencies = new ArrayList<DependentComponentTransfer>();
		try {

			String selectDependentLRs = "Select A.USERID From "
					+ params.getSchema() + ".USER A where A.DEFFOLDERID = ?";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectDependentLRs);
					pst.setInt(1, viewFolderId);

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
				depCompTransfer.setId(-1);
				depCompTransfer.setName(DataUtilities.trimString(rs.getString("USERID")));
				userDependencies.add(depCompTransfer);
			}

			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving user dependencies of View Folder.",e);
		}
		return userDependencies;
	}

    @Override
    public void deleteAssociatedViews(Integer environmentId, List<Integer> deletionIds) {
    	
    	String placeHolders = generator.getPlaceholders(deletionIds.size());       
    	try {
    		
    		if (deletionIds.isEmpty()) {
    			deletionIds.add(0);
    		}
    		
            String deleteAssocViews = 
                "DELETE FROM " + params.getSchema() + ".VFVASSOC A " + 
                "WHERE A.ENVIRONID = ? " +
                "AND A.VFVASSOCID IN ( " + placeHolders + " )";
            PreparedStatement pst = null;
            while (true) {
                try {
                    pst = con.prepareStatement(deleteAssocViews);
                    pst.setInt(1, environmentId);
                    int ndx = 2;
                    for( int i = 0 ; i < deletionIds.size(); i++ ) {
                     pst.setInt(ndx++, deletionIds.get(i));
                    }
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
            throw DataUtilities.createDAOException("Database error occurred while deleting associated Views of View Folder.",e);
        }        
    }

    public void persistAssociatedViews(
        List<ViewFolderViewAssociationTransfer> componentAssociationTransfers) throws DAOException {

        List<ViewFolderViewAssociationTransfer> associatedViewCreate = new ArrayList<ViewFolderViewAssociationTransfer>();
        List<ViewFolderViewAssociationTransfer> associatedViewUpdate = new ArrayList<ViewFolderViewAssociationTransfer>();
    
        for (ViewFolderViewAssociationTransfer associatedView : componentAssociationTransfers) {
            if (!associatedView.isPersistent()) {
                associatedViewCreate.add(associatedView);
            } else {
                associatedViewUpdate.add(associatedView);
            }
        }
        if (associatedViewCreate.size() > 0) {
            createAssociatedViews(associatedViewCreate);
        }
        if (associatedViewUpdate.size() > 0) {
            updateAssociatedViews(associatedViewUpdate);
        }
    }

    private void createAssociatedViews(List<ViewFolderViewAssociationTransfer> associatedViewsCreate) {
        
        // data is either all imported or migrated or none of it is
        boolean isImportOrMigrate = associatedViewsCreate.get(0).isForImport()
                || associatedViewsCreate.get(0).isForMigration() ? true : false;
        boolean useCurrentTS = !isImportOrMigrate;

        try {
            String[] columnNames = { COL_ENVID, "VIEWFOLDERID", "VIEWID",
                COL_CREATETIME, COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
            List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
            if (isImportOrMigrate) {
                names.add(1, "VFVASSOCID");
            }
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {

                    for (ViewFolderViewAssociationTransfer associatedViewtoCreate : associatedViewsCreate) {
                        if (pst == null) {
                            String statement = generator.getInsertStatement(
                                params.getSchema(), "VFVASSOC", "VFVASSOCID", names,useCurrentTS);
                            pst = con.prepareStatement(statement);
                        }
                        int i = 1;
                        pst.setInt(i++, associatedViewtoCreate.getEnvironmentId());
                        if (isImportOrMigrate) {
                            pst.setInt(i++, associatedViewtoCreate.getAssociationId());
                        }
                        pst.setInt(i++, associatedViewtoCreate.getAssociatingComponentId());
                        pst.setInt(i++, associatedViewtoCreate.getAssociatedComponentId());
                        if (isImportOrMigrate) {
                            pst.setTimestamp(i++, DataUtilities.getTimeStamp(associatedViewtoCreate.getCreateTime()));
                        }
                        pst.setString(i++,isImportOrMigrate ? associatedViewtoCreate.getCreateBy() : safrLogin.getUserId());
                        if (isImportOrMigrate) {
                            pst.setTimestamp(i++, DataUtilities.getTimeStamp(associatedViewtoCreate.getModifyTime()));
                        }
                        pst.setString(i++,isImportOrMigrate ? associatedViewtoCreate.getModifyBy() : safrLogin.getUserId());
                        rs = pst.executeQuery();
                        rs.next();
                        int id = rs.getInt(1);          
                        rs.close();     
                        associatedViewtoCreate.setAssociationId(id);
                        associatedViewtoCreate.setPersistent(true);
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
            throw DataUtilities.createDAOException("Database error occurred while creating associations of View Folder with Views.",e);
        }
    }

    private void updateAssociatedViews(List<ViewFolderViewAssociationTransfer> associatedViewUpdate) {
        // data is either all imported or migrated or none of it is
        boolean isImportOrMigrate = associatedViewUpdate.get(0).isForImport()
                || associatedViewUpdate.get(0).isForMigration() ? true : false;
        boolean useCurrentTS = !isImportOrMigrate;

        try {
            List<String> names = new ArrayList<String>();
            names.add("VIEWFOLDERID");
            names.add("VIEWID");
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
            idNames.add("VFVASSOCID");
            idNames.add(COL_ENVID);

            PreparedStatement pst = null;
            while (true) {
                try {
                    for (ComponentAssociationTransfer associatedViewtoUpdate : associatedViewUpdate) {
                        if (pst == null) {
                            String statement = generator.getUpdateStatement(
                                    params.getSchema(), "VFVASSOC", names,
                                    idNames, useCurrentTS);
                            pst = con.prepareStatement(statement);
                        }

                        int i = 1;
                        pst.setInt(i++, associatedViewtoUpdate.getAssociatingComponentId());
                        pst.setInt(i++, associatedViewtoUpdate.getAssociatedComponentId());
                        if (isImportOrMigrate) {
                            // created and lastmod details set from source component
                            pst.setTimestamp(i++, DataUtilities.getTimeStamp(associatedViewtoUpdate.getCreateTime()));
                            pst.setString(i++, associatedViewtoUpdate.getCreateBy());
                            pst.setTimestamp(i++, DataUtilities.getTimeStamp(associatedViewtoUpdate.getModifyTime()));
                            pst.setString(i++, associatedViewtoUpdate.getModifyBy());
                        } else {
                            pst.setString(i++, safrLogin.getUserId());
                        }
                        pst.setInt(i++, associatedViewtoUpdate.getAssociationId());
                        pst.setInt(i++, associatedViewtoUpdate.getEnvironmentId());
                        if ( useCurrentTS ) {
                            ResultSet rs = pst.executeQuery();
                            rs.next();
                            associatedViewtoUpdate.setModifyBy(safrLogin.getUserId());
                            associatedViewtoUpdate.setModifyTime(rs.getDate(1));
                            rs.close();                 
                            pst.close();
                        } else {
                            int count  = pst.executeUpdate();   
                            if (count == 0) {
                                throw new SAFRNotFoundException("No Rows updated.");
                            }                       
                            pst.close();
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
            throw DataUtilities.createDAOException("Database error occurred while updating associations of View Folder with Views.",e);
        }        
    }

    @Override
    public List<ViewFolderViewAssociationTransfer> getVFVAssociation(Integer environmentId, Integer id) {
        List<ViewFolderViewAssociationTransfer> result = new ArrayList<ViewFolderViewAssociationTransfer>();

        try {
            String schema = params.getSchema();
            String selectString = null;

            boolean admin = SAFRApplication.getUserSession().isSystemAdministrator();
            
            if (admin) {
                selectString = "Select A.NAME AS VFNAME, A.VIEWFOLDERID, B.VIEWID, C.NAME AS VIEWNAME, B.VFVASSOCID, "
                        + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "
                        + "From " 
                        + schema + ".VIEWFOLDER A, "
                        + schema + ".VFVASSOC B, "
                        + schema + ".VIEW C "
                        + "Where A.ENVIRONID = ? " 
                        + " AND A.VIEWFOLDERID = ? " 
                        + " AND A.ENVIRONID = B.ENVIRONID AND A.VIEWFOLDERID = B.VIEWFOLDERID"
                        + " AND B.ENVIRONID = C.ENVIRONID AND B.VIEWID = C.VIEWID "
                        + "Order By B.VFVASSOCID";
            } else {
                selectString = "Select A.NAME AS VFNAME, A.VIEWFOLDERID, B.VIEWID, C.NAME AS VIEWNAME, B.VFVASSOCID, D.RIGHTS, "
                        + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "
                        + "From "
                        + schema + ".VIEWFOLDER A INNER JOIN "
                        + schema + ".VFVASSOC B ON A.ENVIRONID = B.ENVIRONID AND A.VIEWFOLDERID = B.VIEWFOLDERID INNER JOIN "
                        + schema + ".VIEW C ON B.ENVIRONID = C.ENVIRONID AND B.VIEWID = C.VIEWID LEFT OUTER JOIN "
                        + schema + ".SECVIEW D ON C.ENVIRONID = D.ENVIRONID AND C.VIEWID = D.VIEWID "
                        + " AND D.GROUPID = " + SAFRApplication.getUserSession().getGroup().getId()
                        + " Where A.ENVIRONID = ? " 
                        + " AND A.VIEWFOLDERID = ? "
                        + " Order By B.VFVASSOCID";
            }
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, environmentId);
                    pst.setInt(2, id);
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
                vfvAssociationTransfer.setAssociatingComponentId(rs.getInt("VIEWFOLDERID"));
                vfvAssociationTransfer.setAssociatingComponentName(
                    DataUtilities.trimString(rs.getString("VFNAME")));
                vfvAssociationTransfer.setAssociatedComponentId(rs.getInt("VIEWID"));
                vfvAssociationTransfer.setAssociatedComponentName(
                    DataUtilities.trimString(rs.getString("VIEWNAME")));
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

    @Override
    public List<ViewFolderViewAssociationTransfer> getVFVAssociations(Integer environmentId) {
        List<ViewFolderViewAssociationTransfer> result = new ArrayList<ViewFolderViewAssociationTransfer>();

        try {
            String schema = params.getSchema();
            String selectString = null;

            boolean admin = SAFRApplication.getUserSession().isSystemAdministrator();
            
            if (admin) {
                selectString = "Select A.NAME AS VFNAME, A.VIEWFOLDERID, B.VIEWID, C.NAME AS VIEWNAME, B.VFVASSOCID, "
                        + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "
                        + "From " 
                        + schema + ".VIEWFOLDER A, "
                        + schema + ".VFVASSOC B, "
                        + schema + ".VIEW C "
                        + "Where A.ENVIRONID = ? " 
                        + " AND A.ENVIRONID = B.ENVIRONID AND A.VIEWFOLDERID = B.VIEWFOLDERID"
                        + " AND B.ENVIRONID = C.ENVIRONID AND B.VIEWID = C.VIEWID "
                        + "Order By B.VFVASSOCID";
            } else {
                selectString = "Select A.NAME AS VFNAME, A.VIEWFOLDERID, B.VIEWID, C.NAME AS VIEWNAME, B.VFVASSOCID, D.RIGHTS, "
                        + "B.CREATEDTIMESTAMP, B.CREATEDUSERID, B.LASTMODTIMESTAMP, B.LASTMODUSERID "
                        + "From "
                        + schema + ".VIEWFOLDER A INNER JOIN "
                        + schema + ".VFVASSOC B ON A.ENVIRONID = B.ENVIRONID AND A.VIEWFOLDERID = B.VIEWFOLDERID INNER JOIN "
                        + schema + ".VIEW C ON B.ENVIRONID = C.ENVIRONID AND B.VIEWID = C.VIEWID LEFT OUTER JOIN "
                        + schema + ".SECVIEW D ON C.ENVIRONID = D.ENVIRONID AND C.VIEWID = D.VIEWID "
                        + " AND D.GROUPID = " + SAFRApplication.getUserSession().getGroup().getId()
                        + " Where A.ENVIRONID = ? " 
                        + " Order By B.VFVASSOCID";
            }
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
                ViewFolderViewAssociationTransfer vfvAssociationTransfer = new ViewFolderViewAssociationTransfer();
                vfvAssociationTransfer.setEnvironmentId(environmentId);
                vfvAssociationTransfer.setAssociatingComponentId(rs.getInt("VIEWFOLDERID"));
                vfvAssociationTransfer.setAssociatingComponentName(
                    DataUtilities.trimString(rs.getString("VFNAME")));
                vfvAssociationTransfer.setAssociatedComponentId(rs.getInt("VIEWID"));
                vfvAssociationTransfer.setAssociatedComponentName(
                    DataUtilities.trimString(rs.getString("VIEWNAME")));
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

    
    @Override
    public List<ViewQueryBean> queryPossibleViewAssociations(int environmentId, List<Integer> notInParam) {
    	
    	String placeHolders = generator.getPlaceholders(notInParam.size());   
        List<ViewQueryBean> result = new ArrayList<ViewQueryBean>();

        try {
            boolean admin = SAFRApplication.getUserSession().isSystemAdministrator(); 
            
            if (notInParam.isEmpty()) 
                notInParam.add(0);

            String selectString;
            if (admin) {
                selectString = "Select VIEWID, NAME From "
                        + params.getSchema() + ".VIEW"
                        + " Where ENVIRONID = ? " 
                        + " AND VIEWID > 0" + " AND VIEWID NOT IN ( " + placeHolders + " )"
                        + " Order By VIEWID";
            } else {
                selectString = "Select A.VIEWID, A.NAME,L.RIGHTS From "
                        + params.getSchema()
                        + ".VIEW A "
                        + "LEFT OUTER JOIN "
                        + params.getSchema()
                        + ".SECVIEW L "
                        + "ON A.ENVIRONID = L.ENVIRONID AND A.VIEWID = L.VIEWID"
                        + " AND L.GROUPID = " + SAFRApplication.getUserSession().getGroup().getId()
                        + " Where A.ENVIRONID = ? " 
                        + " AND A.VIEWID > 0" + " AND A.VIEWID NOT IN ( " + placeHolders + " )"
                        + " Order By A.VIEWID";
            }
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, environmentId);
                    int ndx = 2;
                    for( int i = 0 ; i < notInParam.size(); i++ ) {
                     pst.setInt(ndx++, notInParam.get(i));
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
                ViewQueryBean viewQueryBean = new ViewQueryBean(
                    environmentId, rs.getInt(i++),
                    DataUtilities.trimString(rs.getString(i++)), 
                    null,null,null,
                    admin ? EditRights.ReadModifyDelete : SAFRApplication.getUserSession().getEditRights(
                        rs.getInt("RIGHTS"), ComponentType.View, environmentId), 
                    null, null, null, null, null, null, null);
                result.add(viewQueryBean);
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving all the possible Views which can be associated with this View Folder.",e);
        }
        return result;
    }

    @Override
    public void clearAssociations() {
        try {
            String deleteString = "DELETE FROM " + params.getSchema() + ".x_vfvtbl";
            PreparedStatement pst = null;
            while (true) {
                try {
                    pst = con.prepareStatement(deleteString);
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
                "Database error occurred while clearing View Folder associations",e);
        }
    }

    @Override
    public void addAllViewsAssociation(Integer viewId, Integer environmentId) {
        try {
            String[] columnNames = { COL_ENVID, "VIEWFOLDERID", "VIEWID",
                COL_CREATETIME, COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
            List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    String statement = generator.getInsertStatement(
                        params.getSchema(), "VFVASSOC", "VFVASSOCID", names, true);
                    pst = con.prepareStatement(statement);
                    int i = 1;
                    pst.setInt(i++, environmentId);
                    pst.setInt(i++, 0);
                    pst.setInt(i++, viewId);
                    pst.setString(i++, safrLogin.getUserId());
                    pst.setString(i++, safrLogin.getUserId());
                    rs = pst.executeQuery();
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
            throw DataUtilities.createDAOException("Database error occurred while creating associations of View Folder with Views.",e);
        }        
    }

}
