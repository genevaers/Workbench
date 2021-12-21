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

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.HeaderFooterDAO;
import com.ibm.safr.we.data.transfer.HeaderFooterItemTransfer;
import com.ibm.safr.we.internal.data.PGSQLGenerator;

public class PGHeaderFooterDAO implements HeaderFooterDAO {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGHeaderFooterDAO");

	private static final String TABLE_NAME = "VIEWHEADERFOOTER";
	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "HEADERFOOTERID";
	private static final String COL_VIEWID = "VIEWID";
	private static final String COL_FUNCTION = "STDFUNCCD";
	private static final String COL_JUSTIFY = "JUSTIFYCD";
	private static final String COL_ROWNUMBER = "ROWNUMBER";
	private static final String COL_COLNUMBER = "COLNUMBER";
	private static final String COL_LENGTH = "LENGTH";
	private static final String COL_ITEMTEXT = "ITEMTEXT";
	private static final String COL_HEADERFOOTERIND = "HEADERFOOTERIND";
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
	public PGHeaderFooterDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	/**
	 * This function is used to generate a transfer object for the
	 * Header/Footer.
	 * 
	 * @param rs
	 *            : The result set of a database query run on VIEWHEADERFOOTER
	 *            table with which the values for the transfer objects are set.
	 * @return A transfer object for the Header/Footer with values set according
	 *         to the result set.
	 * @throws SQLException
	 */
	private HeaderFooterItemTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		HeaderFooterItemTransfer headerFooter = new HeaderFooterItemTransfer();
		headerFooter.setEnvironmentId(rs.getInt(COL_ENVID));
		headerFooter.setHeaderFooterId(rs.getInt(COL_ID));
		headerFooter.setViewId(rs.getInt(COL_VIEWID));
		headerFooter.setStdFuctionCode(DataUtilities.trimString(rs
				.getString(COL_FUNCTION)));
		headerFooter.setJustifyCode(DataUtilities.trimString(rs
				.getString(COL_JUSTIFY)));
		headerFooter.setRowNumber(rs.getInt(COL_ROWNUMBER));
		headerFooter.setColNumber(rs.getInt(COL_COLNUMBER));
		headerFooter.setLength(rs.getInt(COL_LENGTH));
		headerFooter.setItemText(rs.getString(COL_ITEMTEXT));
		headerFooter.setHeader(DataUtilities.intToBoolean(rs
				.getInt(COL_HEADERFOOTERIND)));
		headerFooter.setCreateTime(rs.getDate(COL_CREATETIME));
		headerFooter.setCreateBy(DataUtilities.trimString(rs
				.getString(COL_CREATEBY)));
		headerFooter.setModifyTime(rs.getDate(COL_MODIFYTIME));
		headerFooter.setModifyBy(DataUtilities.trimString(rs
				.getString(COL_MODIFYBY)));

		return headerFooter;
	}

	public List<HeaderFooterItemTransfer> persistHeaderFooter(
			List<HeaderFooterItemTransfer> headerFooterItemTransferList,
			Integer viewId, Integer environmentId) throws DAOException {

		removeHeaderFooter(viewId, environmentId);
		if (headerFooterItemTransferList == null
				|| headerFooterItemTransferList.isEmpty()) {
			return headerFooterItemTransferList;
		}

		headerFooterItemTransferList = createHeaderFooter(
				headerFooterItemTransferList, environmentId);
		return headerFooterItemTransferList;
	}

	/**
	 * This function is to create Header/Footer in VIEWHEADERFOOTER in
	 * database.
	 * 
	 * @param headerFooterItemTransferList
	 *            : A list of transfer objects which contains the values which
	 *            are to be set in the fields for the corresponding
	 *            Header/Footer which is to be created.
	 * @return A list of transfer object which contains the values which are
	 *         received from the VIEWHEADERFOOTER for the Header/Footer which
	 *         is created.
	 * @throws DAOException
	 */
	private List<HeaderFooterItemTransfer> createHeaderFooter(
			List<HeaderFooterItemTransfer> headerFooterItemTransferList,
			Integer environmentId) throws DAOException {
		for (HeaderFooterItemTransfer headerFooterItemTransfer : headerFooterItemTransferList) {
			try {
				String[] columnNames = { COL_ENVID, COL_VIEWID,
						COL_FUNCTION, COL_JUSTIFY, COL_ROWNUMBER,
						COL_COLNUMBER, COL_LENGTH, COL_ITEMTEXT,
						COL_HEADERFOOTERIND, COL_CREATETIME, COL_CREATEBY,
						COL_MODIFYTIME, COL_MODIFYBY };
				List<String> names = new ArrayList<String>(Arrays.asList(columnNames));
	            if (headerFooterItemTransfer.isForImportOrMigration()) {
	                names.add(1, COL_ID);
	            }

				String statement = generator.getInsertStatement(params
						.getSchema(), TABLE_NAME, COL_ID, names,
						!headerFooterItemTransfer.isForImportOrMigration());
				PreparedStatement pst = null;
	            ResultSet rs = null;

				while (true) {
					try {
						pst = con.prepareStatement(statement);
						int i = 1;
						pst.setInt(i++, environmentId);
						if (headerFooterItemTransfer.isForImportOrMigration()) {
						    pst.setInt(i++, headerFooterItemTransfer.getHeaderFooterId());
						}
						pst.setInt(i++, headerFooterItemTransfer.getViewId());
						pst.setString(i++, headerFooterItemTransfer.getStdFuctionCode());
						pst.setString(i++, headerFooterItemTransfer.getJustifyCode());
						pst.setInt(i++, headerFooterItemTransfer.getRowNumber());
						pst.setInt(i++, headerFooterItemTransfer.getColNumber());
						pst.setInt(i++, headerFooterItemTransfer.getLength());
						pst.setString(i++, headerFooterItemTransfer.getItemText());
						pst.setInt(i++, DataUtilities.booleanToInt(headerFooterItemTransfer.isHeader()));
						if (headerFooterItemTransfer.isForImportOrMigration()) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(headerFooterItemTransfer.getCreateTime()));
						}
						pst.setString(i++, headerFooterItemTransfer.isForImportOrMigration() ? 
						    headerFooterItemTransfer.getCreateBy() : safrLogin.getUserId());
						if (headerFooterItemTransfer.isForImportOrMigration()) {
							pst.setTimestamp(i++, DataUtilities.getTimeStamp(headerFooterItemTransfer.getModifyTime()));
						}
						pst.setString(i++, headerFooterItemTransfer.isForImportOrMigration() ? 
						    headerFooterItemTransfer.getModifyBy() : safrLogin.getUserId());

						rs = pst.executeQuery();
                        rs.next();
                        int id = rs.getInt(1);          
                        headerFooterItemTransfer.setId(id);
                        headerFooterItemTransfer.setPersistent(true);
                        if (!headerFooterItemTransfer.isForImportOrMigration()) {
                            headerFooterItemTransfer.setCreateBy(safrLogin.getUserId());
                            headerFooterItemTransfer.setCreateTime(rs.getDate(2));
                            headerFooterItemTransfer.setModifyBy(safrLogin.getUserId());
                            headerFooterItemTransfer.setModifyTime(rs.getDate(3));                            
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
				throw DataUtilities.createDAOException("Database error occurred while creating new Header/Footer.",e);
			}
		}

		return headerFooterItemTransferList;

	}

	public void removeHeaderFooter(Integer viewId, Integer environmentId)
			throws DAOException {
		try {
			List<String> idNames = new ArrayList<String>();
			idNames.add(COL_ID);
			idNames.add(COL_ENVID);

			String statement = "Delete From " + params.getSchema()
					+ ".VIEWHEADERFOOTER Where VIEWID = ? AND ENVIRONID = ?";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(statement);

					int i = 1;
					pst.setInt(i++, viewId);
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
			throw DataUtilities.createDAOException("Database error occurred while deleting the Header/Footer.",e);
		}

	}

	public List<HeaderFooterItemTransfer> getAllHeaderFooterItems(
			Integer viewId, Integer environmentId) throws DAOException {
		List<HeaderFooterItemTransfer> hfItemTransferList = new ArrayList<HeaderFooterItemTransfer>();
		try {
            String[] columns = { COL_ENVID, COL_ID, COL_VIEWID,
                COL_FUNCTION, COL_JUSTIFY, COL_ROWNUMBER, COL_COLNUMBER, 
                COL_LENGTH, COL_ITEMTEXT, COL_HEADERFOOTERIND, 
                COL_CREATETIME, COL_MODIFYTIME, COL_CREATEBY, 
                COL_MODIFYBY };
			
			List<String> columnNames = Arrays.asList(columns);

			List<String> idNames = new ArrayList<String>();
			if (viewId > 0) {
				idNames.add(COL_VIEWID);
			}
			idNames.add(COL_ENVID);

			List<String> orderBy = new ArrayList<String>();
			orderBy.add(COL_HEADERFOOTERIND);
			orderBy.add(COL_ROWNUMBER);
			orderBy.add(COL_COLNUMBER);

			String selectString = generator.getSelectColumnsStatement(
					columnNames, params.getSchema(), TABLE_NAME, idNames,
					orderBy);
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
					while (rs.next()) {
						HeaderFooterItemTransfer hfItemTransfer = new HeaderFooterItemTransfer();
						hfItemTransfer = generateTransfer(rs);
						hfItemTransferList.add(hfItemTransfer);
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
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving all Header/Footer(s).",e);
		}
		return hfItemTransferList;
	}

}
