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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.LookupPathSourceFieldType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.LookupPathStepDAO;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.LookupPathStepTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.internal.data.PGSQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;

public class PGLookupPathStepDAO implements LookupPathStepDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGLookupPathStepDAO");

	private static final String TABLE_NAME_STEP = "LOOKUPSTEP";
	private static final String TABLE_NAME_SOURCEFIELD = "LOOKUPSRCKEY";
	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "LOOKUPSTEPID";
	private static final String COL_LOOKUPID = "LOOKUPID";
	private static final String COL_STEPSEQNBR = "STEPSEQNBR";
	private static final String COL_SOURCE = "SRCLRID";
	private static final String COL_LRLFASSOCID = "LRLFASSOCID";
	private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
	private static final String COL_CREATEBY = "CREATEDUSERID";
	private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
	private static final String COL_MODIFYBY = "LASTMODUSERID";

	private static final String COL_KEYSEQNBR = "KEYSEQNBR";

	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private PGSQLGenerator generator = new PGSQLGenerator();

	public PGLookupPathStepDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	public List<LookupPathStepTransfer> getAllLookUpPathSteps(
			Integer environmentId, Integer lookupPathId) throws DAOException {
		List<LookupPathStepTransfer> result = new ArrayList<LookupPathStepTransfer>();

		try {
			List<String> idNames = new ArrayList<String>();
			if (lookupPathId > 0) {
				idNames.add(COL_LOOKUPID);
			}
			idNames.add(COL_ENVID);

			List<String> orderBy = new ArrayList<String>();
			orderBy.add(COL_STEPSEQNBR);
			String selectString = generator.getSelectStatement(params
					.getSchema(), TABLE_NAME_STEP, idNames, orderBy);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int i = 1;
					if (lookupPathId > 0) {
						pst.setInt(i++, lookupPathId);
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
				result.add(generateTransfer(rs));
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving all Lookup Path Steps for Lookup Path with id ["+ lookupPathId + "]", e);
		}
		return result;
	}

    public DependentComponentTransfer getAssociatedLFDependency(
        Integer environmentId, Integer lrlfId) throws DAOException  {
        DependentComponentTransfer depCompTransfer = null;

        String schema = params.getSchema();
        
        String selectString =  "SELECT A.LOGFILEID,A.NAME " + 
                               "FROM " + 
                               schema + ".LOGFILE A, " +
                               schema + ".LRLFASSOC B, " +
                               schema + ".LOGREC C " +
                               "WHERE A.ENVIRONID=B.ENVIRONID " +
                               "AND A.LOGFILEID=B.LOGFILEID " +
                               "AND B.ENVIRONID=? " +
                               "AND B.LRLFASSOCID=? " +
                               "AND B.ENVIRONID=C.ENVIRONID " +
                               "AND B.LOGRECID=C.LOGRECID " +
                               "AND ( C.LOOKUPEXITID IS NULL OR " +
                               "      C.LOOKUPEXITID=0 ) " +
                               "AND ( SELECT COUNT(*) FROM " + 
                               schema + ".LFPFASSOC D " + 
                               "      WHERE B.ENVIRONID=D.ENVIRONID " +
                               "      AND B.LOGFILEID=D.LOGFILEID) > 1;";

        try {
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, environmentId);
                    pst.setInt(2, lrlfId);
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
                Integer joinId = rs.getInt("LOGFILEID");                
                depCompTransfer = new DependentComponentTransfer();
                depCompTransfer.setId(joinId);
                depCompTransfer.setName(DataUtilities.trimString(rs.getString("NAME")));
                depCompTransfer.setDependencyInfo("[Logical File Properties]");                
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving the list of dependencies of Lookup Path to an LF.",e);
        }
        
        return depCompTransfer;
    }
	
	private LookupPathStepTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		LookupPathStepTransfer lkupStepTrans = new LookupPathStepTransfer();
		lkupStepTrans.setEnvironmentId(rs.getInt(COL_ENVID));
		lkupStepTrans.setId(rs.getInt(COL_ID));
		lkupStepTrans.setJoinId(rs.getInt(COL_LOOKUPID));
		lkupStepTrans.setSequenceNumber(rs.getInt(COL_STEPSEQNBR));
		lkupStepTrans.setSourceLRId(rs.getInt(COL_SOURCE));
		lkupStepTrans.setTargetXLRFileId(rs.getInt(COL_LRLFASSOCID));
		lkupStepTrans.setCreateTime(rs.getDate(COL_CREATETIME));
		lkupStepTrans.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		lkupStepTrans.setModifyTime(rs.getDate(COL_MODIFYTIME));
		lkupStepTrans.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

		return lkupStepTrans;
	}

	public LookupPathStepTransfer getLookUpPathStep(Integer environmentId,
			Integer lookupPathStepId) throws DAOException {
		LookupPathStepTransfer result = null;
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			idNames.add(COL_ENVID);

			String selectString = generator.getSelectStatement(params
					.getSchema(), TABLE_NAME_STEP, idNames, null);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int i = 1;
					pst.setInt(i++, lookupPathStepId);
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
				logger.info("No such LookupStep in Env " + environmentId + " with id : "
						+ lookupPathStepId);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the Lookup Path Step.",e);
		}
		return result;
	}

    public int getTargetLookUpPathLrId(Integer environmentId, String lookupPathName) throws DAOException {
        int result = 0;
        try {
            String selectString = "SELECT A.LOGRECID FROM " +
                params.getSchema() + ".LRLFASSOC A," +
                params.getSchema() + ".LOOKUPSTEP B " +
                "WHERE A.ENVIRONID=B.ENVIRONID " +
                "AND A.LRLFASSOCID=B.LRLFASSOCID " +
                "AND B.STEPSEQNBR = (" +
                "SELECT MAX(C.STEPSEQNBR) FROM " +
                params.getSchema() + ".LOOKUPSTEP C," +
                params.getSchema() + ".LOOKUP D " +
                "WHERE C.ENVIRONID=D.ENVIRONID "+
                "AND C.LOOKUPID=D.LOOKUPID "+
                "AND B.LOOKUPID=D.LOOKUPID "+
                "AND B.ENVIRONID=D.ENVIRONID " +
                "AND D.NAME=? " +
                "AND D.ENVIRONID=? " +
                "GROUP BY D.LOOKUPID)";
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    int i = 1;
                    pst.setString(i++, lookupPathName);
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
                result = rs.getInt(1);
            } 
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving the Lookup Path Step.",e);
        }
        return result;
    }
	
		
	public LookupPathStepTransfer persistLookupPathStep(
			LookupPathStepTransfer lkupPathStepTransfer) throws DAOException {
		if (!lkupPathStepTransfer.isPersistent()) {
			return (createLookupPathStep(lkupPathStepTransfer));
		} else {
			return (updateLookupPathStep(lkupPathStepTransfer));
		}
	}

	/**
	 * This method is to create a Lookup Path Step in LOOKUPSTEP.
	 * 
	 * @param lkupPathStepTransfer
	 *            : The transfer object which has the values for the columns of
	 *            the Lookup Path Step which is being created.
	 * @return A LookupPathStepTransfer object.
	 * @throws DAOException
	 */
	private LookupPathStepTransfer createLookupPathStep(
			LookupPathStepTransfer lkupPathStepTransfer) throws DAOException {
		
	    SAFRApplication.getTimingMap().startTiming("PGLookupPathStepDAO.createLookupPathStep");
	    
		boolean isImportOrMigrate = lkupPathStepTransfer.isForImportOrMigration();
		boolean useCurrentTS = !isImportOrMigrate;

		try {
			String[] columnNames = { COL_ENVID, COL_LOOKUPID,
					COL_STEPSEQNBR, COL_SOURCE, COL_LRLFASSOCID,
					COL_CREATETIME, COL_CREATEBY, COL_MODIFYTIME, COL_MODIFYBY };
			List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
            if (isImportOrMigrate) {
                names.add(1, COL_ID);
            }
			String statement = generator.getInsertStatement(
			    params.getSchema(), TABLE_NAME_STEP, COL_ID, names, useCurrentTS);
			PreparedStatement pst = null;
            ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int i = 1;
					pst.setInt(i++, lkupPathStepTransfer.getEnvironmentId());
		            if (isImportOrMigrate) {
		                pst.setInt(i++, lkupPathStepTransfer.getId());
		            }
					pst.setInt(i++, lkupPathStepTransfer.getJoinId());
					pst.setInt(i++, lkupPathStepTransfer.getSequenceNumber());
					pst.setInt(i++, lkupPathStepTransfer.getSourceLRId());
					pst.setInt(i++, lkupPathStepTransfer.getTargetXLRFileId());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lkupPathStepTransfer.getCreateTime()));
					}
					pst.setString(i++,isImportOrMigrate ? lkupPathStepTransfer.getCreateBy() : safrLogin.getUserId());
					if (isImportOrMigrate) {
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lkupPathStepTransfer.getModifyTime()));
					}
					pst.setString(i++,isImportOrMigrate ? lkupPathStepTransfer.getModifyBy() : safrLogin.getUserId());
					rs = pst.executeQuery();
                    rs.next();
                    int id = rs.getInt(1); 
                    lkupPathStepTransfer.setPersistent(true);
                    lkupPathStepTransfer.setId(id);
                    if (!isImportOrMigrate) {
                        lkupPathStepTransfer.setCreateBy(safrLogin.getUserId());
                        lkupPathStepTransfer.setCreateTime(rs.getDate(2));
                        lkupPathStepTransfer.setModifyBy(safrLogin.getUserId());
                        lkupPathStepTransfer.setModifyTime(rs.getDate(3));
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
			throw DataUtilities.createDAOException("Database error occurred while creating a new Lookup Path Step.",e);
		}
		SAFRApplication.getTimingMap().stopTiming("PGLookupPathStepDAO.createLookupPathStep");

		return lkupPathStepTransfer;
	}

	/**
	 * This method is to update a Lookup Path Step in LOOKUPSTEP.
	 * 
	 * @param lkupPathStepTransfer
	 *            : The transfer object which has the values for the columns of
	 *            the Lookup Path Step which is being updated.
	 * @return A LookupPathStepTransfer object.
	 */
	private LookupPathStepTransfer updateLookupPathStep(
			LookupPathStepTransfer lkupPathStepTransfer) throws DAOException {
		
	    SAFRApplication.getTimingMap().startTiming("PGLookupPathStepDAO.updateLookupPathStep");
	    
		boolean isImportOrMigrate = lkupPathStepTransfer.isForImportOrMigration();
		boolean useCurrentTS = !isImportOrMigrate;

		try {
			List<String> names = new ArrayList<String>();
			names.add(COL_SOURCE);
			names.add(COL_LRLFASSOCID);
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
			idNames.add(COL_ID);
			idNames.add(COL_ENVID);

			String statement = generator.getUpdateStatement(params.getSchema(),
					TABLE_NAME_STEP, names, idNames, useCurrentTS);
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					int i = 1;
					pst.setInt(i++, lkupPathStepTransfer.getSourceLRId());
					pst.setInt(i++, lkupPathStepTransfer.getTargetXLRFileId());
					if (isImportOrMigrate) {
						// created and lastmod set from source component
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lkupPathStepTransfer.getCreateTime()));
						pst.setString(i++, lkupPathStepTransfer.getCreateBy());
						pst.setTimestamp(i++, DataUtilities.getTimeStamp(lkupPathStepTransfer.getModifyTime()));
						pst.setString(i++, lkupPathStepTransfer.getModifyBy());
					} else {
						// created details are untouched
						// lastmodtimestamp is CURRENT_TIMESTAMP
						// lastmoduserid is logged in user
						pst.setString(i++, safrLogin.getUserId());
					}

					pst.setInt(i++, lkupPathStepTransfer.getId());
					pst.setInt(i++, lkupPathStepTransfer.getEnvironmentId());
					
                    if ( useCurrentTS ) {
                        ResultSet rs = pst.executeQuery();
                        rs.next();
                        lkupPathStepTransfer.setModifyBy(safrLogin.getUserId());
                        lkupPathStepTransfer.setModifyTime(rs.getDate(1));
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
			throw DataUtilities.createDAOException("Database error occurred while updating the Lookup Path Step.",e);
		}
		SAFRApplication.getTimingMap().stopTiming("PGLookupPathStepDAO.updateLookupPathStep");
		
		return lkupPathStepTransfer;
	}

	public void removeLookupPathStep(Integer lkupPathStepId,
			Integer environmentId) throws DAOException {

		try {
			// first remove all the source Fields for that step from
			// LOOKUPSRCKEY
			removeLookupPathStepSourceField(lkupPathStepId, environmentId);

			// remove the Lookup Path Step
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			idNames.add(COL_ENVID);

			String statement = generator.getDeleteStatement(params.getSchema(),
					TABLE_NAME_STEP, idNames);
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					pst.setInt(1, lkupPathStepId);
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
			throw DataUtilities.createDAOException("Database error occurred while deleting the Lookup Path Step.",e);
		}

	}

	public List<LookupPathSourceFieldTransfer> getLookUpPathStepSourceFields(
			Integer environmentId, Integer lookupPathStepId)
			throws DAOException {
		List<LookupPathSourceFieldTransfer> result = new ArrayList<LookupPathSourceFieldTransfer>();
		try {
			List<String> idNames = new ArrayList<String>();
			if (lookupPathStepId > 0) {
				idNames.add(COL_ID);
			}
			idNames.add(COL_ENVID);
			List<String> orderBy = new ArrayList<String>();
			orderBy.add(COL_KEYSEQNBR);

			String selectString = generator.getSelectStatement(params
					.getSchema(), TABLE_NAME_SOURCEFIELD, idNames, orderBy);
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					int i = 1;
					if (lookupPathStepId > 0) {
						pst.setInt(i++, lookupPathStepId);
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
				result.add(generateTransferSourceFields(rs));
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving the source fields which belong to a particular step of a Lookup Path.",e);
		}
		return result;

	}

	private LookupPathSourceFieldTransfer generateTransferSourceFields(
			ResultSet rs) throws SQLException {
		LookupPathSourceFieldTransfer sourceField = new LookupPathSourceFieldTransfer();
		sourceField.setEnvironmentId(rs.getInt(COL_ENVID));
		sourceField.setId(rs.getInt(COL_ID));
		sourceField.setLookupPathStepId(rs.getInt(COL_ID));
		sourceField.setKeySeqNbr(rs.getInt(COL_KEYSEQNBR));
		sourceField.setSourceFieldType(intToEnum(rs.getInt("FLDTYPE")));
		sourceField.setSourceXLRFLDId(rs.getInt("LRFIELDID"));
		sourceField.setSourceXLRFileId(rs.getInt("LRLFASSOCID"));
		sourceField.setSourceJoinId(rs.getInt("LOOKUPID"));
		sourceField.setDataType(DataUtilities.trimString(rs.getString("VALUEFMTCD")));
		sourceField.setSigned(DataUtilities.intToBoolean(rs.getInt("SIGNED")));
		sourceField.setLength(rs.getInt("VALUELEN"));
		sourceField.setDecimalPlaces(rs.getInt("DECIMALCNT"));
		sourceField.setDateTimeFormat(DataUtilities.trimString(rs.getString("FLDCONTENTCD")));
		sourceField.setScalingFactor(rs.getInt("ROUNDING"));
		sourceField.setHeaderAlignment(DataUtilities.trimString(rs.getString("JUSTIFYCD")));
		sourceField.setNumericMask(DataUtilities.trimString(rs.getString("MASK")));
		sourceField.setSymbolicName(DataUtilities.trimString(rs.getString("SYMBOLICNAME")));
		sourceField.setSourceValue(rs.getString("VALUE"));
		sourceField.setCreateTime(rs.getDate(COL_CREATETIME));
		sourceField.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		sourceField.setModifyTime(rs.getDate(COL_MODIFYTIME));
		sourceField.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));

		return sourceField;
	}

    public void persistLookupPathStepsSourceFields(
            List<Integer> lookupPathStepIds,
            List<LookupPathSourceFieldTransfer> sourceFieldsTrans)
            throws DAOException {
        // Delete any existing SourceFields for list of Lookup Path Steps
        removeLookupPathStepsSourceField(lookupPathStepIds, sourceFieldsTrans
                .get(0).getEnvironmentId());

        List<LookupPathSourceFieldTransfer> csourceFields = new ArrayList<LookupPathSourceFieldTransfer>();        
        int countCreate = 0;
        for (LookupPathSourceFieldTransfer sourceField : sourceFieldsTrans) {
            countCreate++;
            csourceFields.add(sourceField);
            if (countCreate % 500 == 0) {
                createLookupPathStepSourceFields(csourceFields);
                csourceFields.clear();
            }            
        }
        if (csourceFields.size() > 0) {
            createLookupPathStepSourceFields(csourceFields);
        }        
    }
	
    private List<LookupPathSourceFieldTransfer> createLookupPathStepSourceFields(
            List<LookupPathSourceFieldTransfer> srcFlds) throws DAOException {
        try {    

			String statement = generator.getSelectFromFunction(params.getSchema(), "insertlookupsrcfieldWithId", 1);
			if (srcFlds.isEmpty() || !srcFlds.get(0).isForImportOrMigration()) {
            	statement = generator.getSelectFromFunction(params.getSchema(), "insertlookupsrcfield", 1);
			}
			PreparedStatement proc = null;
            
            proc = con.prepareStatement(statement);
            String xmlVal = getCreateArgs(srcFlds);            
            proc.setString(1, xmlVal);
            proc.execute();
            proc.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw DataUtilities.createDAOException("Database error occurred while inserting lookup fields.", e);
        }
        return srcFlds;        
    }
	
    private String getCreateArgs(List<LookupPathSourceFieldTransfer> srcFlds) throws SQLException {
        StringBuffer buf = new StringBuffer();
        buf.append("<Root>\n");
        for (LookupPathSourceFieldTransfer srcFld : srcFlds) {
            buf.append(" <Record>\n");
            buf.append("  <ENVIRONID>"+ srcFld.getEnvironmentId() + "</ENVIRONID>\n");
            buf.append("  <LOOKUPSTEPID>"+ srcFld.getLookupPathStepId() + "</LOOKUPSTEPID>\n");
            buf.append("  <KEYSEQNBR>"+ srcFld.getKeySeqNbr() + "</KEYSEQNBR>\n");
            buf.append("  <FLDTYPE>"+ enumToInt(srcFld.getSourceFieldType()) + "</FLDTYPE>\n");
            if(srcFld.getSourceXLRFLDId() != null) {
            	buf.append("  <LRFIELDID>"+ srcFld.getSourceXLRFLDId() + "</LRFIELDID>\n");
            }
            if(srcFld.getSourceXLRFileId() != null && srcFld.getSourceXLRFileId() != 0) {
            	buf.append("  <LRLFASSOCID>"+ srcFld.getSourceXLRFileId() + "</LRLFASSOCID>\n");
            }
            buf.append("  <LOOKUPID>"+ srcFld.getSourceJoinId() + "</LOOKUPID>\n");            
            if (srcFld.getDataType() != null) {
                buf.append("  <VALUEFMTCD>"+ srcFld.getDataType() + "</VALUEFMTCD>\n");
            }
            buf.append("  <SIGNED>"+ DataUtilities.booleanToInt(srcFld.isSigned()) + "</SIGNED>\n");
            buf.append("  <VALUELEN>"+ srcFld.getLength() + "</VALUELEN>\n");
            buf.append("  <DECIMALCNT>"+ srcFld.getDecimalPlaces() + "</DECIMALCNT>\n");
            if (srcFld.getDateTimeFormat() != null) {
                buf.append("  <FLDCONTENTCD>"+ srcFld.getDateTimeFormat() + "</FLDCONTENTCD>\n");
            }
            buf.append("  <ROUNDING>"+ srcFld.getScalingFactor() + "</ROUNDING>\n");
            buf.append("  <JUSTIFYCD>LEFT</JUSTIFYCD>\n");
            if (srcFld.getNumericMask() != null) {            
                buf.append("  <MASK>"+ srcFld.getNumericMask() + "</MASK>\n");
            }
            if (srcFld.getSymbolicName() != null) {  
                String str = generator.handleSpecialChars(srcFld.getSymbolicName());                                
                buf.append("  <SYMBOLICNAME>"+ str + "</SYMBOLICNAME>\n");
            }
            if (srcFld.getSourceValue() != null) {       
                String str = generator.handleSpecialChars(srcFld.getSourceValue());                
                buf.append("  <VALUE>"+ str + "</VALUE>\n");
            }            
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
    
    public void removeLookupPathStepsSourceField(List<Integer> lkupPathStepIds,
            Integer environmentId) throws DAOException {
        try {
            SAFRApplication.getTimingMap().startTiming("PGLookupPathStepDAO.removeLookupPathStepsSourceField");
            String placeholders = generator.getPlaceholders(lkupPathStepIds.size());
            
            String statement = "DELETE FROM "
                + params.getSchema()
                + "." + TABLE_NAME_SOURCEFIELD + " WHERE "
                + COL_ENVID + "= ? AND "
                + COL_ID + " IN (" + placeholders + " )"; 
            
            PreparedStatement pst = null;

            while (true) {
                try {
                    pst = con.prepareStatement(statement);
                    int ndx = 1;
                    pst.setInt(ndx++, environmentId);
            		for( int i = 0 ; i < lkupPathStepIds.size(); i++ ) {
                        pst.setInt(ndx++, lkupPathStepIds.get(i));
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
            throw DataUtilities.createDAOException("Database error occurred while deleting the source fields belonging to the specified Lookup Path Step.",e);
        }
        SAFRApplication.getTimingMap().stopTiming("PGLookupPathStepDAO.removeLookupPathStepsSourceField");
       
    }
	
	public void removeLookupPathStepSourceField(Integer lkupPathStepId,
			Integer environmentId) throws DAOException {
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			idNames.add(COL_ENVID);

			String statement = generator.getDeleteStatement(params.getSchema(),
					TABLE_NAME_SOURCEFIELD, idNames);
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);
					pst.setInt(1, lkupPathStepId);
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
			throw DataUtilities.createDAOException("Database error occurred while deleting the source fields belonging to the specified Lookup Path Step.",e);
		}
		
	}

	/**
	 * This method is to convert an integer into its equivalent enum value of
	 * sourceFieldType.
	 * 
	 * @param sourceFieldTypeInt
	 *            : The integer returned from the database
	 * @return : The equivalent enum value.
	 */
	private LookupPathSourceFieldType intToEnum(int sourceFieldTypeInt) {
		if (sourceFieldTypeInt == 0) {
			return LookupPathSourceFieldType.LRFIELD;
		} else if (sourceFieldTypeInt == 1) {
			return LookupPathSourceFieldType.CONSTANT;
		} else if (sourceFieldTypeInt == 3) {
			return LookupPathSourceFieldType.SYMBOL;
		} else {
			return null;
		}
	}

	/**
	 * This method is to convert enum value of edit rights into an integer.
	 * 
	 * @param sourceFieldTypeEnum
	 *            : The enum value of sourceFieldType.
	 * @return : The equivalent integer value.
	 */
	private int enumToInt(LookupPathSourceFieldType sourceFieldTypeEnum) {
		if (sourceFieldTypeEnum == LookupPathSourceFieldType.LRFIELD) {
			return 0;
		} else if (sourceFieldTypeEnum == LookupPathSourceFieldType.CONSTANT) {
			return 1;
		} else if (sourceFieldTypeEnum == LookupPathSourceFieldType.SYMBOL) {
			return 3;
		} else {
			return 10;
		}
	}

}
