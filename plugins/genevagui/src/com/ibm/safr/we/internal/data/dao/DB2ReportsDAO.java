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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.ReportsDAO;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.query.LogicalRecordReportQueryBean;

import com.ibm.safr.we.model.query.EnvironmentSecurityReportBean;
import com.ibm.safr.we.model.query.LookupPrimaryKeysBean;
import com.ibm.safr.we.model.query.LookupReportQueryBean;
import com.ibm.safr.we.model.query.SystemAdministorsBean;
import com.ibm.safr.we.model.query.UserGroupsReportBean;
import com.ibm.safr.we.model.query.ViewColumnPICQueryBean;
import com.ibm.safr.we.model.query.ViewMappingsCSVReportQueryBean;
import com.ibm.safr.we.model.query.ViewMappingsReportQueryBean;
import com.ibm.safr.we.model.query.ViewPropertiesReportQueryBean;
import com.ibm.safr.we.model.query.ViewSortKeyReportQueryBean;
import com.ibm.safr.we.model.query.ViewSourcesReportQueryBean;

public class DB2ReportsDAO implements ReportsDAO {

	static transient Logger logger = Logger.getLogger("com.ibm.safr.we.internal.data.dao.DB2ReportsDAO");

    private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private SQLGenerator generator = new SQLGenerator();
	
	public DB2ReportsDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}
	
	@Override
	public ViewPropertiesReportQueryBean getViewProperties(Integer viewId, Integer environmentId) throws DAOException {
		ViewPropertiesReportQueryBean result = null;
	        try {
	            String selectString = "SELECT V.VIEWID,"
	            		+ "V.NAME,"
	            		+ "V.VIEWTYPECD,"
	            		+ "V.EXTRACTFILEPARTNBR,"
	            		+ "V.OUTPUTMEDIACD,"
	            		+ "V.PAGESIZE,"
	            		+ "V.LINESIZE,"
	            		+ "V.ZEROSUPPRESSIND,"
	            		+ "V.EXTRACTMAXRECCNT,"
	            		+ "V.EXTRACTSUMMARYIND,"
	            		+ "V.EXTRACTSUMMARYBUF,"
	            		+ "V.OUTPUTMAXRECCNT,"
	            		+ "V.CONTROLRECID,"
	            		+ "C.NAME AS CONTROLNAME,"
	            		+ "V.FORMATEXITID,"
	            		+ "E.NAME AS FRMTEXIT,"
	            		+ "V.FORMATEXITSTARTUP,"
	            		+ "V.FILEFLDDELIMCD,"
	            		+ "V.FILESTRDELIMCD,"
	            		+ "V.DELIMHEADERROWIND,"
	            		+ "V.FORMATFILTLOGIC,"
	            		+ "V.COMPILER,"
	            		+ "V.LASTACTTIMESTAMP,"
	            		+ "V.LASTACTUSERID, "
	            		+ "L.NAME AS LFNAME,"
	            		+ "P.NAME AS PFNAME "
	            		+ "FROM " + params.getSchema() + ".VIEW V "
	            		+ "LEFT JOIN " + params.getSchema() + ".CONTROLREC C "
	            		+ "ON C.ENVIRONID=V.ENVIRONID AND C.CONTROLRECID=V.CONTROLRECID "
	            		+ "LEFT JOIN " + params.getSchema() + ".EXIT E "
	            		+ "ON E.ENVIRONID=V.ENVIRONID AND E.EXITID=V.FORMATEXITID "
	            		+ "LEFT JOIN " + params.getSchema() + ".LFPFASSOC I "
	            		+ "ON I.ENVIRONID=V.ENVIRONID AND I.LFPFASSOCID=V.LFPFASSOCID "
	            		+ "LEFT JOIN " + params.getSchema() + ".LOGFILE L "
	            		+ "ON L.ENVIRONID=I.ENVIRONID AND L.LOGFILEID=I.LOGFILEID "
	            		+ "LEFT JOIN " + params.getSchema() + ".PHYFILE P "
	            		+ "ON P.ENVIRONID=L.ENVIRONID AND P.PHYFILEID=I.PHYFILEID "
	            		+ "WHERE V.ENVIRONID=? AND VIEWID=?;" ;

	    
	            PreparedStatement pst = null;
	            ResultSet rs = null;
	            while (true) {
	                try {
	                    pst = con.prepareStatement(selectString);
	                	pst.setInt(1,  environmentId);
	                	pst.setInt(2,  viewId);
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
	                result = new ViewPropertiesReportQueryBean(
	                    DataUtilities.trimString(rs.getString("VIEWID")), 
	                    DataUtilities.trimString(rs.getString("NAME")), 
	                    DataUtilities.trimString(rs.getString("VIEWTYPECD")), 
	                    DataUtilities.trimString(rs.getString("EXTRACTFILEPARTNBR")), 
	                    DataUtilities.trimString(rs.getString("OUTPUTMEDIACD")), 
	                    DataUtilities.trimString(rs.getString("PAGESIZE")), 
	                    DataUtilities.trimString(rs.getString("LINESIZE")), 
	                    DataUtilities.trimString(rs.getString("ZEROSUPPRESSIND")), 
	                    DataUtilities.trimString(rs.getString("EXTRACTMAXRECCNT")), 
	                    DataUtilities.trimString(rs.getString("EXTRACTSUMMARYIND")), 
	                    DataUtilities.trimString(rs.getString("EXTRACTSUMMARYBUF")), 
	                    DataUtilities.trimString(rs.getString("OUTPUTMAXRECCNT")), 
	                    DataUtilities.trimString(rs.getString("CONTROLRECID")), 
	                    DataUtilities.trimString(rs.getString("CONTROLNAME")), 
	                    DataUtilities.trimString(rs.getString("FORMATEXITID")), 
	                    DataUtilities.trimString(rs.getString("FRMTEXIT")), 
	                    DataUtilities.trimString(rs.getString("FORMATEXITSTARTUP")), 
	                    DataUtilities.trimString(rs.getString("FILEFLDDELIMCD")), 
	                    DataUtilities.trimString(rs.getString("FILESTRDELIMCD")), 
	                    DataUtilities.trimString(rs.getString("DELIMHEADERROWIND")), 
	                    DataUtilities.trimString(rs.getString("FORMATFILTLOGIC")), 
	                    DataUtilities.trimString(rs.getString("COMPILER")), 
	                    DataUtilities.trimString(rs.getString("LASTACTTIMESTAMP")), 
	                    DataUtilities.trimString(rs.getString("LASTACTUSERID")), 
	                    DataUtilities.trimString(rs.getString("LFNAME")), 
	                    DataUtilities.trimString(rs.getString("PFNAME")) );
	            }
	            pst.close();
	            rs.close();
	        } catch (SQLException e) {
	            throw DataUtilities.createDAOException(
	                    "Database error occurred while querying View.", e);
	        }
	        return result;
	}
	@Override
	public List<ViewSourcesReportQueryBean>getViewSources(Integer viewId, Integer environmentId) throws DAOException {
		List<ViewSourcesReportQueryBean> result = new ArrayList<>();
        try {
            String selectString = "SELECT 	S.VIEWID,"
            		+ "		S.SRCSEQNBR,"
            		+ "		S.INLRLFASSOCID,"
            		+ "		S.EXTRACTFILTLOGIC,"
            		+ "		S.OUTLFPFASSOCID,"
            		+ "		S.WRITEEXITID,"
            		+ "		S.WRITEEXITPARM,"
            		+ "		S.EXTRACTOUTPUTLOGIC,"
            		+ "		I.LOGFILEID,"
            		+ "		I.LOGRECID,"
            		+ "		R.NAME AS LRNAME,"
            		+ "		L.NAME AS LFNAME, "
            		+ "		O.LOGFILEID AS LOFID,"
            		+ "		O.PHYFILEID,"
            		+ "		LO.NAME AS LONAME,"
            		+ "		P.NAME AS PFNAME,"
            		+ "		E.NAME AS WRITEEXIT "
            		+ "FROM " + params.getSchema() + ".VIEW V "
            		+ "JOIN " + params.getSchema() + ".VIEWSOURCE S "
            		+ "ON S.ENVIRONID = V.ENVIRONID AND S.VIEWID=V.VIEWID "
            		+ "JOIN " + params.getSchema() + ".LRLFASSOC I "
            		+ "ON I.ENVIRONID=S.ENVIRONID AND I.LRLFASSOCID=S.INLRLFASSOCID "
            		+ "JOIN " + params.getSchema() + ".LOGREC R "
            		+ "ON R.ENVIRONID=I.ENVIRONID AND I.LOGRECID=R.LOGRECID "
            		+ "JOIN " + params.getSchema() + ".LOGFILE L "
            		+ "ON L.ENVIRONID=S.ENVIRONID AND L.LOGFILEID=I.LOGFILEID "
            		+ "JOIN " + params.getSchema() + ".LFPFASSOC O "
            		+ "ON O.ENVIRONID=S.ENVIRONID AND O.LFPFASSOCID=S.OUTLFPFASSOCID "
            		+ "JOIN " + params.getSchema() + ".LOGFILE LO "
            		+ "ON LO.ENVIRONID=S.ENVIRONID AND LO.LOGFILEID=O.LOGFILEID "
            		+ "JOIN " + params.getSchema() + ".PHYFILE P "
            		+ "ON P.ENVIRONID=S.ENVIRONID AND P.PHYFILEID=O.PHYFILEID "
            		+ "LEFT JOIN " + params.getSchema() + ".EXIT E "
            		+ "ON E.ENVIRONID=S.ENVIRONID AND E.EXITID=S.WRITEEXITID "
            		+ "WHERE V.ENVIRONID=? AND V.VIEWID=? "
            		+ "ORDER BY SRCSEQNBR;";
    
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                	pst.setInt(1,  environmentId);
                	pst.setInt(2,  viewId);
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
                result.add(new ViewSourcesReportQueryBean(
								rs.getString("VIEWID"),
								rs.getString("SRCSEQNBR"),
								rs.getString("INLRLFASSOCID"),
								rs.getString("EXTRACTFILTLOGIC"),
								rs.getString("OUTLFPFASSOCID"),
								rs.getString("WRITEEXITID"),
								rs.getString("WRITEEXITPARM"),
								rs.getString("EXTRACTOUTPUTLOGIC"),
								rs.getString("LOGFILEID"),
								rs.getString("LOGRECID"),
								rs.getString("LRNAME"),
								rs.getString("LFNAME"),
								rs.getString("LOFID"),
								rs.getString("PHYFILEID"),
								rs.getString("LONAME"),
								rs.getString("PFNAME"),
								rs.getString("WRITEEXIT")
							 ));
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                    "Database error occurred while querying View.", e);
        }
        return result;
	}
	@Override
	public List<ViewSortKeyReportQueryBean> getViewSortKeys(Integer viewId, Integer environmentId) throws DAOException {
		List<ViewSortKeyReportQueryBean> result = new ArrayList<>();
        try {
            String selectString = "SELECT C.VIEWID,"
            		+ "C.VIEWCOLUMNID,"
            		+ "S.SRCSEQNBR,"
            		+ "K.KEYSEQNBR,"
            		+ "K.SORTSEQCD,"
            		+ "K.SORTBRKIND,"
            		+ "K.PAGEBRKIND,"
            		+ "K.SORTKEYDISPLAYCD,"
            		+ "K.SORTKEYLABEL,"
            		+ "N.SUBTLABEL,"
            		+ "K.SKFLDFMTCD,"
            		+ "K.SKSIGNED,"
            		+ "K.SKSTARTPOS,"
            		+ "K.SKFLDLEN,"
            		+ "K.SKDECIMALCNT,"
            		+ "K.SKFLDCONTENTCD,"
            		+ "C.SORTTITLELOOKUPID,"
            		+ "L.NAME AS LKNAME,"
            		+ "C.SORTTITLELRFIELDID AS SRCSKTFIELDID,"
            		+ "K.SORTTITLELRFIELDID AS SKSKTFIELDID,"
            		+ "F.NAME AS SKTFIELDNAME,"
            		+ "C.EFFDATEVALUE,"
            		+ "C.EFFDATETYPE,"
            		+ "C.EFFDATELRFIELDID "
            		+ "FROM " + params.getSchema() + ".VIEWCOLUMNSOURCE C "
            		+ "JOIN " + params.getSchema() + ".VIEWSOURCE S "
            		+ "ON C.ENVIRONID=S.ENVIRONID AND C.VIEWSOURCEID=S.VIEWSOURCEID "
            		+ "LEFT JOIN " + params.getSchema() + ".LRFIELD F "
            		+ "ON F.ENVIRONID=C.ENVIRONID AND F.LRFIELDID=C.SORTTITLELRFIELDID "
            		+ "LEFT JOIN " + params.getSchema() + ".LOOKUP L "
            		+ "ON L.ENVIRONID=C.ENVIRONID AND L.LOOKUPID=C.LOOKUPID "
            		+ "JOIN " + params.getSchema() + ".VIEWSORTKEY K "
            		+ "ON K.ENVIRONID=C.ENVIRONID AND K.VIEWCOLUMNID=C.VIEWCOLUMNID "
            		+ "JOIN " + params.getSchema() + ".VIEWCOLUMN N "
            		+ "ON K.ENVIRONID=N.ENVIRONID AND K.VIEWCOLUMNID=N.VIEWCOLUMNID "
            		+ "WHERE C.ENVIRONID=? AND C.VIEWID=? "
            		+ "ORDER BY S.SRCSEQNBR ;";
    
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                	pst.setInt(1,  environmentId);
                	pst.setInt(2,  viewId);
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
                result.add(new ViewSortKeyReportQueryBean(
						rs.getString("VIEWID"),
						rs.getString("VIEWCOLUMNID"),
						rs.getString("SRCSEQNBR"),
						rs.getString("KEYSEQNBR"),
						rs.getString("SORTSEQCD"),
						rs.getString("SORTBRKIND"),
						rs.getString("PAGEBRKIND"),
						rs.getString("SORTKEYDISPLAYCD"),
						rs.getString("SORTKEYLABEL"),
						rs.getString("SUBTLABEL"),
						rs.getString("SKFLDFMTCD"),
						rs.getString("SKSIGNED"),
						rs.getString("SKSTARTPOS"),
						rs.getString("SKFLDLEN"),
						rs.getString("SKDECIMALCNT"),
						rs.getString("SKFLDCONTENTCD"),
						rs.getString("SORTTITLELOOKUPID"),
						rs.getString("LKNAME"),
						rs.getString("SRCSKTFIELDID"),
						rs.getString("SKSKTFIELDID"),
						rs.getString("SKTFIELDNAME"),
						rs.getString("EFFDATEVALUE"),
						rs.getString("EFFDATETYPE"),
						rs.getString("EFFDATELRFIELDID")
						 ));
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                    "Database error occurred while querying View Sortkey.", e);
        }
        return result;
	}
	@Override
	public List<ViewMappingsReportQueryBean> getViewColumnMappings(Integer viewId, Integer environmentId, String sourceNum) throws DAOException {
		List<ViewMappingsReportQueryBean> result = new ArrayList<>();
        try {
            String selectString = "SELECT C.VIEWID,"
            		+ "N.VIEWCOLUMNID,"
            		+ "N.COLUMNNUMBER,"
            		+ "S.SRCSEQNBR,"
            		+ "C.SOURCETYPEID,"
            		+ "C.CONSTVAL,"
            		+ "C.LOOKUPID,"
            		+ "L.NAME AS LKNAME,"
            		+ "C.LRFIELDID,"
            		+ "F.NAME AS FIELDNAME,"
            		+ "C.EXTRACTCALCLOGIC,"
            		+ "N.SPACESBEFORECOLUMN,"
            		+ "N.STARTPOSITION,"
            		+ "N.FLDFMTCD,"
            		+ "N.FLDCONTENTCD,"
            		+ "N.MAXLEN,"
            		+ "N.JUSTIFYCD,"
            		+ "N.DECIMALCNT,"
            		+ "N.ROUNDING,"
            		+ "N.SIGNEDIND,"
            		+ "N.VISIBLE,"
            		+ "N.FORMATCALCLOGIC "
            		+ "FROM " + params.getSchema() + ".VIEWCOLUMNSOURCE C "
            		+ "JOIN " + params.getSchema() + ".VIEWSOURCE S "
            		+ "ON C.ENVIRONID=S.ENVIRONID AND C.VIEWSOURCEID=S.VIEWSOURCEID "
            		+ "JOIN " + params.getSchema() + ".VIEWCOLUMN N "
            		+ "ON S.ENVIRONID=N.ENVIRONID AND C.VIEWCOLUMNID=N.VIEWCOLUMNID "
            		+ "LEFT JOIN " + params.getSchema() + ".LRFIELD F "
            		+ "ON F.ENVIRONID=N.ENVIRONID AND F.LRFIELDID=C.LRFIELDID "
            		+ "LEFT JOIN " + params.getSchema() + ".LOOKUP L "
            		+ "ON L.ENVIRONID=C.ENVIRONID AND L.LOOKUPID=C.LOOKUPID "
            		+ "WHERE C.ENVIRONID=? AND C.VIEWID=?  and s.srcseqnbr=? "
            		+ "ORDER BY S.SRCSEQNBR, N.COLUMNNUMBER ;";
    
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                	pst.setInt(1,  environmentId);
                	pst.setInt(2,  viewId);
                	pst.setString(3, sourceNum);
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
                result.add(new ViewMappingsReportQueryBean(
                								rs.getString("VIEWID"),
                								rs.getString("VIEWCOLUMNID"),
                								rs.getString("COLUMNNUMBER"),
                								rs.getString("SRCSEQNBR"),
                								rs.getInt("SOURCETYPEID"),
                								rs.getString("CONSTVAL"),
                								rs.getString("LOOKUPID"),
                								rs.getString("LKNAME"),
                								rs.getString("LRFIELDID"),
                								rs.getString("FIELDNAME"),
                								rs.getString("EXTRACTCALCLOGIC"),
                								rs.getString("SPACESBEFORECOLUMN"),
                								rs.getString("STARTPOSITION"),
                								rs.getString("FLDFMTCD"),
                								rs.getString("FLDCONTENTCD"),
                								rs.getString("MAXLEN"),
                								rs.getString("JUSTIFYCD"),
                								rs.getString("DECIMALCNT"),
                								rs.getString("ROUNDING"),
                								rs.getString("SIGNEDIND"),
                								rs.getString("VISIBLE"),
                								rs.getString("FORMATCALCLOGIC")
						 ));
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                    "Database error occurred while querying View Sortkey.", e);
        }
        return result;
	}

	@Override
	public List<ViewMappingsCSVReportQueryBean> getViewColumnCSVMappings(List<Integer> viewIds, Integer environmentId) throws DAOException {
		List<ViewMappingsCSVReportQueryBean> result = new ArrayList<>();
		String placeholders = generator.getPlaceholders(viewIds.size());

        try {
            String selectString =		"SELECT C.VIEWID,"
            		+ "		S.SRCSEQNBR,"
            		+ "		N.COLUMNNUMBER,"
            		+ "		C.SOURCETYPEID,"
            		+ "		C.CONSTVAL,"
            		+ "		C.LOOKUPID,"
            		+ "		L.NAME AS LKNAME,"
            		+ "		C.LRFIELDID,"
            		+ "		F.NAME AS FIELDNAME,"
            		+ "		C.EXTRACTCALCLOGIC,"
            		+ "		N.SPACESBEFORECOLUMN,"
            		+ "		N.STARTPOSITION,"
            		+ "		N.FLDFMTCD,"
            		+ "		N.FLDCONTENTCD,"
            		+ "		N.MAXLEN,"
            		+ "		N.JUSTIFYCD,"
            		+ "		N.DECIMALCNT,"
            		+ "		N.ROUNDING,"
            		+ "		N.SIGNEDIND,"
            		+ "		N.VISIBLE,"
            		+ "		N.SUBTLABEL,"
            		+ "		N.FORMATCALCLOGIC,"
            		+ "		K.SORTSEQCD,"
            		+ "		K.SORTBRKIND,"
            		+ "		K.PAGEBRKIND,"
            		+ "		K.SORTKEYDISPLAYCD,"
            		+ "		K.SORTKEYLABEL,"
            		+ "		K.SKFLDFMTCD,"
            		+ "		K.SKSIGNED,"
            		+ "		K.SKSTARTPOS,"
            		+ "		K.SKFLDLEN,"
            		+ "		K.SKDECIMALCNT,"
            		+ "		K.SKFLDCONTENTCD,"
            		+ "		K.SORTTITLELRFIELDID,"
            		+ "		F.NAME AS SORTTITLEFIELDNAME,"
            		+ "		SORTTITLELENGTH  "
            		+ "		FROM " + params.getSchema() + ".VIEWCOLUMNSOURCE C "
            		+ "		JOIN " + params.getSchema() + ".VIEWSOURCE S "
            		+ "		ON C.ENVIRONID=S.ENVIRONID AND C.VIEWSOURCEID=S.VIEWSOURCEID "
            		+ "		JOIN " + params.getSchema() + ".VIEWCOLUMN N "
            		+ "		ON S.ENVIRONID=N.ENVIRONID AND C.VIEWCOLUMNID=N.VIEWCOLUMNID "
            		+ "		LEFT JOIN " + params.getSchema() + ".LRFIELD F "
            		+ "		ON F.ENVIRONID=N.ENVIRONID AND F.LRFIELDID=C.LRFIELDID "
            		+ "		LEFT JOIN " + params.getSchema() + ".LOOKUP L "
            		+ "		ON L.ENVIRONID=C.ENVIRONID AND L.LOOKUPID=C.LOOKUPID "
            		+ "		LEFT JOIN " + params.getSchema() + ".VIEWSORTKEY K "
            		+ "		ON K.ENVIRONID=C.ENVIRONID AND K.VIEWCOLUMNID=C.VIEWCOLUMNID "
            		+ "		LEFT JOIN " + params.getSchema() + ".LRFIELD F "
            		+ "		ON F.ENVIRONID=C.ENVIRONID AND F.LRFIELDID=C.SORTTITLELRFIELDID "
            		+ "		WHERE C.ENVIRONID=? AND C.VIEWID IN ( " + placeholders + " )  "
            		+ "		ORDER BY C.VIEWID, S.SRCSEQNBR, N.COLUMNNUMBER ;";
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                	pst.setInt(1,  environmentId);
					int ndx = 2;
					Iterator<Integer> si = viewIds.iterator();
					while(si.hasNext()) {
						Integer v = si.next(); 
						pst.setInt(ndx++, v);
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
                result.add(new ViewMappingsCSVReportQueryBean(
						rs.getString("VIEWID"),
						rs.getString("SRCSEQNBR"),
						rs.getString("COLUMNNUMBER"),
						rs.getString("SOURCETYPEID"),
						rs.getString("CONSTVAL"),
						rs.getString("LOOKUPID"),
						rs.getString("LKNAME"),
						rs.getString("LRFIELDID"),
						rs.getString("FIELDNAME"),
						rs.getString("EXTRACTCALCLOGIC"),
						rs.getString("SPACESBEFORECOLUMN"),
						rs.getString("STARTPOSITION"),
						rs.getString("FLDFMTCD"),
						rs.getString("FLDCONTENTCD"),
						rs.getString("MAXLEN"),
						rs.getString("JUSTIFYCD"),
						rs.getString("DECIMALCNT"),
						rs.getString("ROUNDING"),
						rs.getString("SIGNEDIND"),
						rs.getString("VISIBLE"),
						rs.getString("SUBTLABEL"),
						rs.getString("FORMATCALCLOGIC"),
						rs.getString("SORTSEQCD"),
						rs.getString("SORTBRKIND"),
						rs.getString("PAGEBRKIND"),
						rs.getString("SORTKEYDISPLAYCD"),
						rs.getString("SORTKEYLABEL"),
						rs.getString("SKFLDFMTCD"),
						rs.getString("SKSIGNED"),
						rs.getString("SKSTARTPOS"),
						rs.getString("SKFLDLEN"),
						rs.getString("SKDECIMALCNT"),
						rs.getString("SKFLDCONTENTCD"),
						rs.getString("SORTTITLELRFIELDID"),
						rs.getString("SORTTITLEFIELDNAME"),
		                rs.getString("SORTTITLELENGTH")
                		));
            }
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                    "Database error occurred while querying View Sortkey.", e);
        }
        return result;
	}

	@Override
	public List<EnvironmentSecurityReportBean> getEnvironmentSecurityDetails(List<Integer> ids) throws DAOException {
		List<EnvironmentSecurityReportBean> result = new ArrayList<>();
		String placeholders = generator.getPlaceholders(ids.size());

        try {
            String selectString =		"SELECT S.ENVIRONID,"
            		+ "E.NAME AS ENVNAME,"
            		+ "S.GROUPID,"
            		+ "G.NAME AS GRPNAME,"
            		+ "ENVROLE,"
            		+ "U.LASTNAME, "
            		+ "U.FIRSTNAME,"
            		+ "U.USERID "
            		+ "FROM " + params.getSchema() + ".SECENVIRON S "
            		+ "JOIN " + params.getSchema() + ".ENVIRON E "
            		+ "ON  S.ENVIRONID=E.ENVIRONID "
            		+ "JOIN " + params.getSchema() + ".GROUP G "
            		+ "ON  S.GROUPID=G.GROUPID "
            		+ "JOIN " + params.getSchema() + ".SECUSER C "
            		+ "ON G.GROUPID=C.GROUPID "
            		+ "JOIN " + params.getSchema() + ".USER U "
            		+ "ON C.USERID=U.USERID "
            		+ "WHERE S.ENVIRONID IN ( " + placeholders + " )  "
            		+ "ORDER BY ENVIRONID, ENVROLE, GRPNAME, LASTNAME;";
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
					int ndx = 1;
					Iterator<Integer> si = ids.iterator();
					while(si.hasNext()) {
						Integer v = si.next(); 
						pst.setInt(ndx++, v);
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
				result.add(new EnvironmentSecurityReportBean(
						rs.getString("ENVIRONID"), 
						rs.getString("ENVNAME"), 
						rs.getString("GROUPID"),
						rs.getString("GRPNAME"), 
						rs.getString("ENVROLE"), 
						rs.getString("USERID"),
						rs.getString("FIRSTNAME"),
						rs.getString("LASTNAME")));
            }
        } catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying View Sortkey.", e);
        }
        return result;
	}

	@Override
	public List<SystemAdministorsBean> getsystemAdministrators() throws DAOException {
		List<SystemAdministorsBean> result = new ArrayList<>();

        try {
            String selectString =		"SELECT USERID, "
            		+ "FIRSTNAME, "
            		+ "LASTNAME, "
            		+ "PASSWORD "
            		+ "FROM " + params.getSchema() + ".USER U "
            		+ "WHERE SYSADMIN=1 "
            		+ "ORDER BY USERID;";
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
				result.add(new SystemAdministorsBean(
						rs.getString("USERID"), 
						rs.getString("FIRSTNAME"), 
						rs.getString("LASTNAME"),
						rs.getString("PASSWORD")));
            }
        } catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying System Administors.", e);
        }
        return result;	
   }

	@Override
	public List<UserGroupsReportBean> getUserGroupsReport() throws DAOException {
		List<UserGroupsReportBean> result = new ArrayList<>();

        try {
            String selectString =		"SELECT "
            		+ "U.LASTNAME, "
            		+ "U.FIRSTNAME, "
            		+ "U.USERID, "
            		+ "U.SYSADMIN, "
            		+ "U.PASSWORD, "
            		+ "G.GROUPID, "
            		+ "G.NAME AS GRPNAME "
            		+ "FROM " + params.getSchema() + ".USER U  "
            		+ "JOIN " + params.getSchema() + ".SECUSER S "
            		+ "ON S.USERID=U.USERID "
            		+ "JOIN " + params.getSchema() + ".GROUP G "
            		+ "ON  S.GROUPID=G.GROUPID "
            		+ "ORDER BY U.USERID, GRPNAME; ";
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
				result.add(new UserGroupsReportBean(
						rs.getString("LASTNAME"),
						rs.getString("FIRSTNAME"), 
						rs.getString("USERID"), 
						rs.getString("SYSADMIN"), 
						rs.getString("PASSWORD"),
						rs.getString("GROUPID"),
						rs.getString("GRPNAME")
						));
            }
        } catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying User Groups Report.", e);
        }
        return result;	
        }

	@Override
	public List<ViewColumnPICQueryBean> getViewColumnPICData(Integer viewId, Integer environmentId) throws DAOException {
		List<ViewColumnPICQueryBean> result = new ArrayList<>();
        try {
            String selectString = "SELECT C.ENVIRONID, "
            		+ "	C.VIEWID, "
            		+ "	V.NAME, "
            		+ "	COLUMNNUMBER, "
            		+ "	FLDFMTCD, "
            		+ "	MAXLEN, "
            		+ "	SIGNEDIND, "
            		+ "	DECIMALCNT, "
            		+ "	HDRLINE1, "
            		+ "	HDRLINE2, "
            		+ "	HDRLINE3 "
            		+ "	FROM " + params.getSchema() + ".VIEWCOLUMN C "
            		+ "	JOIN " + params.getSchema() + ".VIEW V "
            		+ "	ON V.ENVIRONID=C.ENVIRONID AND V.VIEWID=C.VIEWID "
            		+ " WHERE C.ENVIRONID=? AND C.VIEWID=? "
            		+ "	ORDER BY C.COLUMNNUMBER;";
            
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                	pst.setInt(1,  environmentId);
                	pst.setInt(2,  viewId);
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
				result.add(new ViewColumnPICQueryBean(
						rs.getString("ENVIRONID"),
						rs.getString("VIEWID"), 
						rs.getString("NAME"), 
						rs.getString("COLUMNNUMBER"), 
						rs.getString("FLDFMTCD"), 
						rs.getInt("MAXLEN"),
						rs.getBoolean("SIGNEDIND"),
						rs.getInt("DECIMALCNT"),
						rs.getString("HDRLINE1"),
						rs.getString("HDRLINE2"),
						rs.getString("HDRLINE3")
						));
            }
        } catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying User Groups Report.", e);
        }
        return result;	
	}

	@Override
	public List<LookupReportQueryBean> getLookupReport(Integer id, Integer environmentId) throws DAOException {
		List<LookupReportQueryBean> result = new ArrayList<>();
        try {
            String selectString = "SELECT "
            		+ "L.SRCLRID, "
            		+ "L.LOOKUPID, "
            		+ "L.NAME, "
            		+ "S.STEPSEQNBR, "
            		+ "S.SRCLRID AS STEPSRCLRID, "
            		+ "SR.NAME AS STEPSRCLR, "
            		+ "K.KEYSEQNBR, "
            		+ "K.FLDTYPE,"
            		+ "R.NAME AS STEPTARGLR,"
            		+ "KR.LOGRECID AS KEYLRID,"
            		+ "KR.NAME AS KEYLR,"
            		+ "K.LRFIELDID,"
            		+ "FLD.NAME AS FLDNAME,"
            		+ "K.VALUEFMTCD,"
            		+ "K.SIGNED,"
            		+ "K.VALUELEN,"
            		+ "K.DECIMALCNT,"
            		+ "K.FLDCONTENTCD,"
            		+ "K.ROUNDING,"
            		+ "K.JUSTIFYCD,"
            		+ "K.SYMBOLICNAME,"
            		+ "K.VALUE,"
            		+ "K.CREATEDTIMESTAMP,"
            		+ "K.CREATEDUSERID,"
            		+ "K.LASTMODTIMESTAMP,"
            		+ "K.LASTMODUSERID,"
            		+ "SA.LOGRECID AS TARGLRID,"
            		+ "SA.LOGFILEID AS TARGLFLD,"
            		+ "F.NAME AS STEPTARGLF,"
            		+ "KF.NAME AS KEYLF, "
            		+ "R.LOOKUPEXITID, "
            		+ "E.NAME AS EXITNAME, "
            		+ "R.LOOKUPEXITSTARTUP "
            		+ "FROM " + params.getSchema() + ".LOOKUP L "
            		+ "JOIN " + params.getSchema() + ".LOOKUPSTEP S "
            		+ "ON S.ENVIRONID=L.ENVIRONID AND S.LOOKUPID=L.LOOKUPID "
            		+ "JOIN " + params.getSchema() + ".LOOKUPSRCKEY K "
            		+ "ON K.ENVIRONID=L.ENVIRONID AND K.LOOKUPID=L.LOOKUPID AND K.LOOKUPSTEPID=S.LOOKUPSTEPID "
            		+ "JOIN " + params.getSchema() + ".LRLFASSOC SA "
            		+ "ON SA.ENVIRONID=L.ENVIRONID AND SA.LRLFASSOCID=S.LRLFASSOCID "
            		+ "JOIN " + params.getSchema() + ".LOGREC R "
            		+ "ON R.ENVIRONID=L.ENVIRONID AND R.LOGRECID=SA.LOGRECID "
            		+ "JOIN " + params.getSchema() + ".LOGFILE F "
            		+ "ON F.ENVIRONID=L.ENVIRONID AND F.LOGFILEID=SA.LOGFILEID "
            		+ "LEFT JOIN " + params.getSchema() + ".LRLFASSOC KA "
            		+ "ON KA.ENVIRONID=L.ENVIRONID AND KA.LRLFASSOCID=K.LRLFASSOCID "
            		+ "LEFT JOIN " + params.getSchema() + ".LOGREC KR "
            		+ "ON KR.ENVIRONID=L.ENVIRONID AND KR.LOGRECID=KA.LOGRECID "
            		+ "LEFT JOIN " + params.getSchema() + ".LOGFILE KF "
            		+ "ON KF.ENVIRONID=L.ENVIRONID AND KF.LOGFILEID=KA.LOGFILEID "
            		+ "LEFT JOIN " + params.getSchema() + ".LRFIELD AS FLD "
            		+ "ON FLD.ENVIRONID=L.ENVIRONID AND  FLD.LRFIELDID=K.LRFIELDID "
            		+ "LEFT JOIN " + params.getSchema() + ".EXIT E "
            		+ "ON E.ENVIRONID=L.ENVIRONID AND E.EXITID=R.LOOKUPEXITID "
            		+ "LEFT JOIN " + params.getSchema() + ".LOGREC SR "
            		+ "ON SR.ENVIRONID=S.ENVIRONID AND SR.LOGRECID=S.SRCLRID "
            		+ "WHERE L.ENVIRONID=? AND L.LOOKUPID=? "
            		+ "ORDER BY S.STEPSEQNBR, K.KEYSEQNBR ; ";
            
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                	pst.setInt(1,  environmentId);
                	pst.setInt(2,  id);
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
				result.add(new LookupReportQueryBean(
						rs.getString("SRCLRID"),
						rs.getInt("LOOKUPID"),
						rs.getString("NAME"),
						rs.getInt("STEPSEQNBR"),
						rs.getString("STEPSRCLRID"),
						rs.getString("STEPSRCLR"),
						rs.getInt("KEYSEQNBR"),
						rs.getString("FLDTYPE"),
						rs.getString("STEPTARGLR"),
						rs.getString("KEYLRID"),
						rs.getString("KEYLR"),
						rs.getString("LRFIELDID"),
						rs.getString("FLDNAME"),
						rs.getString("VALUEFMTCD"),
						rs.getString("SIGNED"),
						rs.getString("VALUELEN"),
						rs.getString("DECIMALCNT"),
						rs.getString("FLDCONTENTCD"),
						rs.getString("ROUNDING"),
						rs.getString("JUSTIFYCD"),
						rs.getString("SYMBOLICNAME"),
						rs.getString("VALUE"),
						rs.getString("CREATEDTIMESTAMP"),
						rs.getString("CREATEDUSERID"),
						rs.getString("LASTMODTIMESTAMP"),
						rs.getString("LASTMODUSERID"),
						rs.getString("TARGLRID"),
						rs.getString("TARGLFLD"),
						rs.getString("STEPTARGLF"),
						rs.getString("KEYLF"),
						rs.getString("LOOKUPEXITID"),
						rs.getString("EXITNAME"),
						rs.getString("LOOKUPEXITSTARTUP")
						));
            }
        } catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying User Groups Report.", e);
        }
        return result;	
	}

	@Override
	public List<LookupPrimaryKeysBean> getLookupPrimaryKeysReport(Integer id, Integer environmentId)
			throws DAOException {
		List<LookupPrimaryKeysBean> result = new ArrayList<>();
        try {
            String selectString = "SELECT  "
            		+ "L.LOOKUPID,  "
            		+ "S.STEPSEQNBR,  "
            		+ "SA.LOGRECID, "
            		+ "XF.LRFIELDID, "
            		+ "XF.FLDSEQNBR, "
            		+ "LRF.NAME AS FLDNAME, "
            		+ "LRFA.FLDFMTCD, "
            		+ "LRFA.MAXLEN, "
            		+ "LRFA.DECIMALCNT, "
            		+ "LRFA.FLDCONTENTCD "
            		+ "FROM " + params.getSchema() + ".LOOKUP L "
            		+ "JOIN " + params.getSchema() + ".LOOKUPSTEP S "
            		+ "ON S.ENVIRONID=L.ENVIRONID AND S.LOOKUPID=L.LOOKUPID "
            		+ "JOIN " + params.getSchema() + ".LRLFASSOC SA "
            		+ "ON SA.ENVIRONID=L.ENVIRONID AND SA.LRLFASSOCID=S.LRLFASSOCID "
            		+ "JOIN " + params.getSchema() + ".LOGREC R "
            		+ "ON R.ENVIRONID=L.ENVIRONID AND R.LOGRECID=SA.LOGRECID "
            		+ "JOIN " + params.getSchema() + ".LRINDEX I "
            		+ "ON I.ENVIRONID=L.ENVIRONID AND I.LOGRECID=R.LOGRECID "
            		+ "JOIN " + params.getSchema() + ".LRINDEXFLD XF "
            		+ "ON XF.ENVIRONID=I.ENVIRONID AND XF.LRINDEXID=I.LRINDEXID "
            		+ "JOIN " + params.getSchema() + ".LRFIELD LRF "
            		+ "ON LRF.ENVIRONID=I.ENVIRONID AND LRF.LRFIELDID=XF.LRFIELDID "
            		+ "JOIN " + params.getSchema() + ".LRFIELDATTR LRFA "
            		+ "ON LRFA.ENVIRONID=LRF.ENVIRONID AND LRFA.LRFIELDID=LRF.LRFIELDID "
            		+ "WHERE L.ENVIRONID=? AND L.LOOKUPID=? "
            		+ "order by s.stepseqnbr, xf.fldseqnbr; ";
            		            
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                	pst.setInt(1,  environmentId);
                	pst.setInt(2,  id);
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
				result.add(new LookupPrimaryKeysBean(
						rs.getInt("LOOKUPID"), 
						rs.getInt("STEPSEQNBR"), 
						rs.getString("LOGRECID"),
						rs.getString("LRFIELDID"), 
						rs.getString("FLDSEQNBR"), 
						rs.getString("FLDNAME"),
						rs.getString("FLDFMTCD"), 
						rs.getString("MAXLEN"), 
						rs.getString("DECIMALCNT"),
						rs.getString("FLDCONTENTCD")));
            }
        } catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while querying User Groups Report.", e);
        }
        return result;	
	}
	
	@Override
	public List<LogicalRecordReportQueryBean> getLogicalRecords(Integer LRid, Integer environmentId) throws DAOException {
		List<LogicalRecordReportQueryBean> result = new ArrayList<>();
		
        try {
            String selectString = "SELECT L.ENVIRONID," 
        	        + "L.LOGRECID," 
        	        + "L.NAME AS LRNAME," 
        	        + "L.LRTYPECD," 
        	        + "L.LRSTATUSCD," 
        	        + "L.LOOKUPEXITID,"
        	        + "E.NAME AS EXITNAME,"
        	        + "L.LOOKUPEXITSTARTUP,"  
         	        + "F.LRFIELDID,"
        	        + "F.NAME AS FIELDNAME,"
        	        + "D.FLDSEQNBR AS PRIMARY, "
        	        + "F.DBMSCOLNAME," 
        	        + "F.FIXEDSTARTPOS,"
        	        + "F.ORDINALPOS," 
        	        + "F.REDEFINE as REDEFINEDFIELDID," 
        	        + "A.FLDFMTCD," 
        	        + "A.SIGNEDIND," 
        	        + "A.MAXLEN," 
        	        + "A.DECIMALCNT,"
        	        + "A.ROUNDING," 
        	        + "A.FLDCONTENTCD," 
        	        + "A.JUSTIFYCD,"
        	        + "A.SUBTLABEL," 
        	        + "A.SORTKEYLABEL," 
        	        + "A.INPUTMASK,"
        	        + "I.EFFDATESTARTFLDID," 
        	        + "I.EFFDATEENDFLDID," 
        	        + "D.FLDSEQNBR "
        	        + "FROM " + params.getSchema() + ".LOGREC L "
        	        + "JOIN " + params.getSchema() + ".LRFIELD F "
        	        + "ON F.ENVIRONID=L.ENVIRONID AND F.LOGRECID=L.LOGRECID "
        	        + "JOIN " + params.getSchema() + ".LRFIELDATTR A "
        	        + "ON A.ENVIRONID=F.ENVIRONID AND A.LRFIELDID=F.LRFIELDID "
        	        + "LEFT OUTER JOIN " + params.getSchema() + ".LRINDEX I "
        	        + "ON I.ENVIRONID=L.ENVIRONID AND I.LOGRECID=L.LOGRECID "
        	        + "LEFT OUTER JOIN " + params.getSchema() + ".LRINDEXFLD D "
          	        + "ON D.ENVIRONID=I.ENVIRONID AND D.LRINDEXID=I.LRINDEXID AND D.LRFIELDID=F.LRFIELDID "
          	        + "LEFT JOIN " + params.getSchema() + ".EXIT E "
          	        + "ON E.ENVIRONID=L.ENVIRONID AND E.EXITID=L.LOOKUPEXITID "
          	        + "WHERE L.ENVIRONID=? AND L.LOGRECID=?  " 
            		+ "ORDER BY F.FIXEDSTARTPOS;";
           
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                	pst.setInt(1,  environmentId);
                	pst.setInt(2,LRid);
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
                result.add(new LogicalRecordReportQueryBean(
                    DataUtilities.trimString(rs.getString("ENVIRONID")), 
                	DataUtilities.trimString(rs.getString("LOGRECID")), 
                	DataUtilities.trimString(rs.getString("LRNAME")), 
                	DataUtilities.trimString(rs.getString("LRTYPECD")), 
                	DataUtilities.trimString(rs.getString("LRSTATUSCD")), 
                	DataUtilities.trimString(rs.getString("LOOKUPEXITID")),
                	DataUtilities.trimString(rs.getString("EXITNAME")),
                	DataUtilities.trimString(rs.getString("LOOKUPEXITSTARTUP")),  
                 	DataUtilities.trimString(rs.getString("LRFIELDID")),
                	DataUtilities.trimString(rs.getString("FIELDNAME")), 
                	DataUtilities.trimString(rs.getString("PRIMARY")), 
                	DataUtilities.trimString(rs.getString("DBMSCOLNAME")), 
                	DataUtilities.trimString(rs.getString("FIXEDSTARTPOS")),
                	DataUtilities.trimString(rs.getString("ORDINALPOS")), 
                	DataUtilities.trimString(rs.getString("REDEFINEDFIELDID")), 
                	DataUtilities.trimString(rs.getString("FLDFMTCD")), 
                	DataUtilities.trimString(rs.getString("SIGNEDIND")), 
                	DataUtilities.trimString(rs.getString("MAXLEN")), 
                	DataUtilities.trimString(rs.getString("DECIMALCNT")),
                	DataUtilities.trimString(rs.getString("ROUNDING")), 
                	DataUtilities.trimString(rs.getString("FLDCONTENTCD")), 
                	DataUtilities.trimString(rs.getString("JUSTIFYCD")),
                	DataUtilities.trimString(rs.getString("SUBTLABEL")), 
                	DataUtilities.trimString(rs.getString("SORTKEYLABEL")), 
                	DataUtilities.trimString(rs.getString("INPUTMASK")),
                	DataUtilities.trimString(rs.getString("EFFDATESTARTFLDID")), 
                	DataUtilities.trimString(rs.getString("EFFDATEENDFLDID")), 
                	DataUtilities.trimString(rs.getString("FLDSEQNBR"))
                    ));
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                    "Database error occurred while querying View.", e);
        }
        return result;
	}

}
