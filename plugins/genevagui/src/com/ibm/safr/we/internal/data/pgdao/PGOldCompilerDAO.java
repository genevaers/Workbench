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
import java.util.List;
import java.util.logging.Logger;

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.OldCompilerDAO;
import com.ibm.safr.we.model.query.OldCompilerJoinQueryBean;

public class PGOldCompilerDAO implements OldCompilerDAO {

	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.we.internal.data.dao.PGOldCompilerDAO");

	private static final String COL_NAME = "LOOKUPNAME";
	private static final String COL_ID = "LOOKUPID";
	private static final String COL_FNAME = "FLDNAME";
	private static final String COL_FID = "LRFIELDID";
	private static final String COL_FORMAT = "FLDFMTCD";
	private static final String COL_SIGNED = "SIGNEDIND";
	private static final String COL_LENGTH = "MAXLEN";
	private static final String COL_DECIMAL = "DECIMALCNT";
	private static final String COL_CONTENT = "FLDCONTENTCD";
	private static final String COL_ROUNDING = "ROUNDING";
	private static final String COL_JUSTIFY = "HDRJUSTIFYCD";
	private static final String COL_MASK = "INPUTMASK";
	private static final String COL_STARTPOS = "FIXEDSTARTPOS";
	private static final String COL_ORDPOS = "ORDINALPOS";
	 
	private Connection con;
	private ConnectionParameters params;
	
	public PGOldCompilerDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
	}
	
	public List<OldCompilerJoinQueryBean> getJoinFields(
			Integer envId,
			Integer srcLRId) 
	throws DAOException {
		
		List<OldCompilerJoinQueryBean> result = new ArrayList<OldCompilerJoinQueryBean>();

		try {
			String selectString = "SELECT A.NAME AS LOOKUPNAME, A.LOOKUPID, D.NAME AS FLDNAME, D.LRFIELDID, " +
						 "E.FLDFMTCD, E.SIGNEDIND, E.MAXLEN, E.DECIMALCNT, " +
						 "E.FLDCONTENTCD, E.ROUNDING, E.HDRJUSTIFYCD, E.INPUTMASK, " +
                         "D.FIXEDSTARTPOS, D.ORDINALPOS "
						+ "FROM "
						+ params.getSchema() + ".LOOKUP A, "
						+ params.getSchema() + ".LRLFASSOC B, "
						+ params.getSchema() + ".LOGREC C, "
						+ params.getSchema() + ".LRFIELD D, "
						+ params.getSchema() + ".LRFIELDATTR E "
						+ " WHERE (A.ENVIRONID = ? AND C.LRSTATUSCD = 'ACTVE' AND A.SRCLRID = ? )"
						+ " AND (B.ENVIRONID = ? AND A.DESTLRLFASSOCID = B.LRLFASSOCID )"
						+ " AND (C.ENVIRONID = ? AND B.LOGRECID = C.LOGRECID )"
						+ " AND (D.ENVIRONID = ? AND C.LOGRECID = D.LOGRECID )"
						+ " AND (E.ENVIRONID = ? AND D.LRFIELDID = E.LRFIELDID )";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1,  envId);
					pst.setInt(2,  srcLRId);
					pst.setInt(3,  envId);
					pst.setInt(4,  envId);
					pst.setInt(5,  envId);
					pst.setInt(6,  envId);
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

				boolean signed = false;
				if (rs.getInt(COL_SIGNED) > 0)
				{
					signed = true;
				}
				
				String format = rs.getString(COL_FORMAT) == null ? null : rs.getString(COL_FORMAT).trim(); 
				String content = rs.getString(COL_CONTENT) == null ? null : rs.getString(COL_CONTENT).trim(); 
				String justify = rs.getString(COL_JUSTIFY) == null ? null : rs.getString(COL_JUSTIFY).trim();
				
				OldCompilerJoinQueryBean joinBean = new OldCompilerJoinQueryBean(
					rs.getString(COL_NAME), rs.getInt(COL_ID), rs.getString(COL_FNAME), rs.getInt(COL_FID),
					format, signed, rs.getInt(COL_LENGTH), rs.getInt(COL_DECIMAL),
					content, rs.getInt(COL_ROUNDING), justify, 
					rs.getString(COL_MASK), rs.getInt(COL_STARTPOS), rs.getInt(COL_ORDPOS));
					
				result.add(joinBean);
				
			}
			pst.close();
			rs.close();
			return result;

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying Old Comnpiler Lookup Paths.",e);
		}
	}

}
