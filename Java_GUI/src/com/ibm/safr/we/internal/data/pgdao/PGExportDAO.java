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
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.DateFormat;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.ExportElementType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.ExportDAO;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.XMLTableDataTransfer;
import com.ibm.safr.we.internal.data.PGSQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;

public class PGExportDAO implements ExportDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.PGExportDAO");
	
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
	public PGExportDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	public Map<ComponentType, List<DependentComponentTransfer>> getComponentDependencies(
			ComponentType compType, Integer componentId, Integer environmentId)
			throws DAOException {
		Map<ComponentType, List<DependentComponentTransfer>> dependenciesMap = new LinkedHashMap<ComponentType, List<DependentComponentTransfer>>();
		
		if (compType == ComponentType.ViewFolder) {
            getComponentDependenciesVF(compType, componentId, environmentId,
                dependenciesMap);		    
		}
		else {		
            getComponentDependenciesOther(compType, componentId, environmentId,
                dependenciesMap);
		}
    		
		return dependenciesMap;
	}

    private void getComponentDependenciesVF(ComponentType compType,
        Integer vfId, Integer environmentId,
        Map<ComponentType, List<DependentComponentTransfer>> dependenciesMap) {

        // Get View ID's
        List<Integer> viewIds = new ArrayList<Integer>();
        try {
            PreparedStatement pst = null;                            
            ResultSet rs = null;        
            String selectString = "SELECT VIEWID "
                + "FROM " + params.getSchema()+ ".VFVASSOC "
                + "WHERE VIEWFOLDERID = ?  "
                + "AND ENVIRONID = ? "            
                + "ORDER BY VIEWFOLDERID, VIEWID";            
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, vfId);
                    pst.setInt(2, environmentId);
                    pst.execute();
                    rs = pst.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            while (rs.next()) {
                int viewId = rs.getInt("VIEWID");
                viewIds.add(viewId);
            }        
            rs.close();  
            rs = null;
            pst.close();                        
        }
        catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving the data for View Folder(s).",e);
        }
            
        for (Integer viewId : viewIds) {
            boolean success = false;        
            while (!success) {                                                  
                try {
                    // start a transaction
                    DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
                    callGetViewDeps(compType, viewId, environmentId,
                            dependenciesMap, "VIEW");   
                    success=true;
                } catch (SQLException e) {
                    throw DataUtilities.createDAOException(
                        "Database error occurred while retrieving all the dependencies of a metadata component in terms of other metadata components.",e);
                } finally {
                    DAOFactoryHolder.getDAOFactory().getDAOUOW().end();
                } // end transaction try
            } // end transaction while loop
        }
        
    }

    protected void getComponentDependenciesOther(ComponentType compType,
        Integer componentId, Integer environmentId,
        Map<ComponentType, List<DependentComponentTransfer>> dependenciesMap) {
        boolean success = false;        
        while (!success) {                                                  
    		try {
                DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
    			String entityType = "";       			
    			if (compType == ComponentType.PhysicalFile) {
    				entityType = "PARTITION";
    			} else if (compType == ComponentType.LogicalFile) {
    				entityType = "FILE";
    			} else if (compType == ComponentType.LogicalRecord) {
    				entityType = "LOGICAL RECORD";
    			} else if (compType == ComponentType.LookupPath) {
    				entityType = "JOIN";
    			} else if (compType == ComponentType.View) {
    				entityType = "VIEW";
    				callGetViewDeps(compType, componentId, environmentId,
                            dependenciesMap, entityType);                
    			} 
//                callGetCompDeps(compType, componentId, environmentId,
//                    dependenciesMap, entityType);                
                success=true;
            } catch (DAOUOWInterruptedException e) {
                // UOW interrupted so retry it
                continue;                                                               			
    		} catch (SQLException e) {
    			throw DataUtilities.createDAOException(
                    "Database error occurred while retrieving all the dependencies of a metadata component in terms of other metadata components.",e);
            } finally {
                DAOFactoryHolder.getDAOFactory().getDAOUOW().end();
            } // end transaction try
        } // end transaction while loop
    }

    protected void callGetViewDeps(ComponentType compType, Integer componentId, Integer environmentId,
            Map<ComponentType, List<DependentComponentTransfer>> dependenciesMap, String entityType) throws SQLException {
            List<DependentComponentTransfer> dependentComponents;
            // start a transaction
            String schema = params.getSchema();    			
            String statement = generator.getFunction(schema,"getViewDependencies", 3);
            CallableStatement proc = null;
            ResultSet rs = null;
            while (true) {
            	try {
            		proc = con.prepareCall(statement);
            		int i = 1;
            		proc.registerOutParameter(i++, Types.OTHER);
            		proc.setInt(i++, environmentId);
            		proc.setInt(i++, componentId);
            		proc.setInt(i++, safrLogin.getGroupId());
             		proc.execute();
            		//rs = proc.getResultSet();
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
            //This gets the content behind the cursor as a result set
            //We could use String cursorName = proc.getString(1);
            //to get the cursor name and then FETCH ... but using this for the moment
            rs = (ResultSet) proc.getObject(1);
            while (rs.next()) {
            	DependentComponentTransfer depComp = new DependentComponentTransfer();
       
            	String componentType = DataUtilities.trimString((rs.getString(1)).toUpperCase());
            	ComponentType componentTypeSP = DataUtilities.getComponentTypeFromString(componentType);
            	depComp.setName(DataUtilities.trimString(rs.getString(4)));
            	depComp.setId(rs.getInt(3));
//            	int rights = rs.getInt(4);
//            	if (rights < 1) {
//            		depComp.setEditRights(SAFRApplication.getUserSession().getRoleEditRights(compType, environmentId));
//            	} else {
//            		depComp.setEditRights(EditRights.intToEnum(rights));
//            	}
       
            	if (dependenciesMap.containsKey(componentTypeSP)) {        	    
            	    // check for already existing dependency
            	    List<DependentComponentTransfer> deps = dependenciesMap.get(componentTypeSP);
            	    boolean found = false;
            	    for (DependentComponentTransfer dep : deps) {
            	        if (dep.getId().equals(depComp.getId())) {
            	            found = true;
            	            break;
            	        }
            	    }
            	    if (!found) {
                		dependenciesMap.get(componentTypeSP).add(depComp);
            	    }   
            	} else {
            		dependentComponents = new ArrayList<DependentComponentTransfer>();
            		dependentComponents.add(depComp);
            		dependenciesMap.put(componentTypeSP, dependentComponents);
            	}
            }
            proc.close();
            rs.close();
        }

    protected void callGetCompDeps(ComponentType compType, Integer componentId, Integer environmentId,
            Map<ComponentType, List<DependentComponentTransfer>> dependenciesMap, String entityType) throws SQLException {
            List<DependentComponentTransfer> dependentComponents;
            // start a transaction
            String schema = params.getSchema();    			
            String statement = generator.getFunction(schema,"getComponentDependencies", 5);
            CallableStatement proc = null;
            ResultSet rs = null;
            while (true) {
            	try {
            		proc = con.prepareCall(statement);
            		int i = 1;
            		proc.registerOutParameter(i++, Types.OTHER);
            		proc.setString(i++, entityType);
            		proc.setInt(i++, componentId);
            		proc.setInt(i++, environmentId);
            		proc.setInt(i++, safrLogin.getGroupId());
            		proc.setString(i++, "Stuff");
            		proc.execute();
            		//rs = proc.getResultSet();
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
            //This gets the content behind the cursor as a result set
            //We could use String cursorName = proc.getString(1);
            //to get the cursor name and then FETCH ... but using this for the moment
            rs = (ResultSet) proc.getObject(1);
            while (rs.next()) {
            	DependentComponentTransfer depComp = new DependentComponentTransfer();
       
            	String componentType = DataUtilities.trimString((rs.getString(1)).toUpperCase());
            	ComponentType componentTypeSP = DataUtilities.getComponentTypeFromString(componentType);
            	depComp.setName(DataUtilities.trimString(rs.getString(2)));
            	depComp.setId(rs.getInt(3));
            	int rights = rs.getInt(4);
            	if (rights < 1) {
            		depComp.setEditRights(SAFRApplication.getUserSession().getRoleEditRights(compType, environmentId));
            	} else {
            		depComp.setEditRights(EditRights.intToEnum(rights));
            	}
       
            	if (dependenciesMap.containsKey(componentTypeSP)) {        	    
            	    // check for already existing dependency
            	    List<DependentComponentTransfer> deps = dependenciesMap.get(componentTypeSP);
            	    boolean found = false;
            	    for (DependentComponentTransfer dep : deps) {
            	        if (dep.getId().equals(depComp.getId())) {
            	            found = true;
            	            break;
            	        }
            	    }
            	    if (!found) {
                		dependenciesMap.get(componentTypeSP).add(depComp);
            	    }   
            	} else {
            		dependentComponents = new ArrayList<DependentComponentTransfer>();
            		dependentComponents.add(depComp);
            		dependenciesMap.put(componentTypeSP, dependentComponents);
            	}
            }
            proc.close();
            rs.close();
        }

    public Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getPhysicalFileData(
        Integer environmentId, List<Integer> physicalFileIds)
        throws DAOException {
        Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> physicalFileDataMap = new LinkedHashMap<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>>();
    
        if (physicalFileIds == null || physicalFileIds.isEmpty()) {
            return physicalFileDataMap;
        }
        
        String placeHolders = generator.getPlaceholders(physicalFileIds.size());
        
        try {        
            PreparedStatement pst = null;
            ResultSet rs = null;
            
            String selectString = "SELECT * "
                + "FROM " + params.getSchema()+ ".PHYFILE "
                + "WHERE PHYFILEID IN ( " + placeHolders + " )"
                + "AND ENVIRONID = ?  "
                + "ORDER BY PHYFILEID";
            
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    int ndx = 1;
            		for( int i = 0 ; i < physicalFileIds.size(); i++ ) {
                        pst.setInt(ndx++, physicalFileIds.get(i));
            		}
                    pst.setInt(ndx, environmentId);
                    pst.execute();
                    rs = pst.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        // lost database connection, so reconnect and retry
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
        
            int recordNbr;                
            Map<Integer, List<XMLTableDataTransfer>> innerMap = null;            
            
            if (physicalFileDataMap.containsKey(ExportElementType.PHYSICAL_FILE)) {
                recordNbr = physicalFileDataMap.get(ExportElementType.PHYSICAL_FILE).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                physicalFileDataMap.get(ExportElementType.PHYSICAL_FILE).putAll(innerMap);
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                physicalFileDataMap.put(ExportElementType.PHYSICAL_FILE,innerMap);
            }
            rs.close();
            pst.close();
        }
        catch (SQLException e) {
            throw DataUtilities.createDAOException(
                 "Database error occurred while retrieving the data for Physical File(s).",e);
        }        
        return physicalFileDataMap;        
    }
	
    public Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getLogicalFileData(
        Integer environmentId, List<Integer> logicalFileIds)
        throws DAOException {
        Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> logicalFileDataMap = new LinkedHashMap<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>>();
    
        if (logicalFileIds == null || logicalFileIds.isEmpty()) {
            return logicalFileDataMap;
        }
        
        String placeHolders = generator.getPlaceholders(logicalFileIds.size());
        try {        
            ResultSet rs = null;
            PreparedStatement pst1;
            PreparedStatement pst2;
            
            String selectString = "SELECT * "
                + "FROM " + params.getSchema()+ ".LOGFILE "
                + "WHERE LOGFILEID IN ( " + placeHolders + " )"
                + "AND ENVIRONID = ?  "
                + "ORDER BY LOGFILEID";

            while (true) {
                try {
                    pst1 = con.prepareStatement(selectString);
                    int ndx = 1;
            		for( int i = 0 ; i < logicalFileIds.size(); i++ ) {
                        pst1.setInt(ndx++, logicalFileIds.get(i));
            		}
                    pst1.setInt(ndx, environmentId);
                    pst1.execute();
                    rs = pst1.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            int recordNbr;
            Map<Integer, List<XMLTableDataTransfer>> innerMap = null;
            
            if (logicalFileDataMap.containsKey(ExportElementType.LOGICAL_FILE)) {
                recordNbr = logicalFileDataMap.get(ExportElementType.LOGICAL_FILE).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                logicalFileDataMap.get(ExportElementType.LOGICAL_FILE).putAll(innerMap);
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                logicalFileDataMap.put(ExportElementType.LOGICAL_FILE, innerMap);
            }
            pst1.close();
            rs.close();                
            rs = null;
            
            String selectString2 = "SELECT * "
                + "FROM " + params.getSchema() + ".LFPFASSOC " 
                + "WHERE ENVIRONID= ?  " 
                + "AND LOGFILEID IN ( " + placeHolders + " )"            
                + "ORDER BY LOGFILEID,PARTSEQNBR";            
            
            while (true) {
                try {
                    pst2 = con.prepareStatement(selectString2);
                    int ndx = 1;
                    pst2.setInt(ndx++, environmentId);
            		for( int i = 0 ; i < logicalFileIds.size(); i++ ) {
                        pst2.setInt(ndx++, logicalFileIds.get(i));
            		}
                    pst2.execute();
                    rs = pst2.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            if (logicalFileDataMap.containsKey(ExportElementType.LFPFASSOC)) {
                recordNbr = logicalFileDataMap.get(ExportElementType.LFPFASSOC).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                logicalFileDataMap.get(ExportElementType.LFPFASSOC).putAll(innerMap);
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                logicalFileDataMap.put(ExportElementType.LFPFASSOC, innerMap);
            }
            rs.close();                
            pst2.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving the data for Logical File(s) and their Physical File associations.",e);
        }        
        return logicalFileDataMap;
    }

    public Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getUserExitRoutineData(
        Integer environmentId, List<Integer> userExitIds)
        throws DAOException {

        Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> userExitDataMap = new LinkedHashMap<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>>();
    
        if (userExitIds == null || userExitIds.isEmpty()) {
            return userExitDataMap;
        }
        
        String placeHolders = generator.getPlaceholders(userExitIds.size());
        
        try {        
            PreparedStatement pst = null;
            ResultSet rs = null;
            
            String selectString = "SELECT * "
                + "FROM " + params.getSchema()+ ".EXIT "
                + "WHERE EXITID IN ( " + placeHolders + " )"
                + "AND ENVIRONID = ?  "
                + "ORDER BY EXITID";
            
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    int ndx = 1;
            		for( int i = 0 ; i < userExitIds.size(); i++ ) {
                        pst.setInt(ndx++, userExitIds.get(i));
            		}
                    pst.setInt(ndx, environmentId);
                    pst.execute();
                    rs = pst.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        // lost database connection, so reconnect and retry
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            Map<Integer, List<XMLTableDataTransfer>> innerMap = null;
            int recordNbr;            
            if (userExitDataMap.containsKey(ExportElementType.EXIT)) {
                recordNbr = userExitDataMap.get(ExportElementType.EXIT).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                userExitDataMap.get(ExportElementType.EXIT).putAll(innerMap);
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                userExitDataMap.put(ExportElementType.EXIT, innerMap);
            }
                            
            rs.close();                
            pst.close();
                        
        }  catch (SQLException e) {
            throw DataUtilities.createDAOException(
                 "Database error occurred while retrieving the data for User Exit Routine(s).",e);
        }
        
        return userExitDataMap;        
    }

    public Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getLogicalRecordData(
        Integer environmentId, List<Integer> logicalRecordIds)
        throws DAOException {
            Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> logicalRecordDataMap = new LinkedHashMap<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>>();
            if (logicalRecordIds == null || logicalRecordIds.isEmpty()) {
                return logicalRecordDataMap;
            }
            
            String placeHolders = generator.getPlaceholders(logicalRecordIds.size());
            try {      
                PreparedStatement pst1 = null;
                PreparedStatement pst2 = null;
                PreparedStatement pst3 = null;            
                PreparedStatement pst4 = null;
                PreparedStatement pst5 = null;
                PreparedStatement pst6 = null;                            
                ResultSet rs = null;
                
                String selectString = "SELECT * "
                    + "FROM " + params.getSchema()+ ".LOGREC "
                    + "WHERE LOGRECID IN ( " + placeHolders + " )"
                    + "AND ENVIRONID = ?  "            
                    + "ORDER BY LOGRECID";            

                while (true) {
                    try {
                        pst1 = con.prepareStatement(selectString);
                        int ndx = 1;
                		for( int i = 0 ; i < logicalRecordIds.size(); i++ ) {
                            pst1.setInt(ndx++, logicalRecordIds.get(i));
                		}
                        pst1.setInt(ndx, environmentId);
                        pst1.execute();
                        rs = pst1.getResultSet();
                        break;
                    } catch (SQLException se) {
                        if (con.isClosed()) {
                            con = DAOFactoryHolder.getDAOFactory().reconnect();
                        } else {
                            throw se;
                        }
                    } // end reconnect try
                }
                
                int recordNbr;
                Map<Integer, List<XMLTableDataTransfer>> innerMap = null;
                
                if (logicalRecordDataMap.containsKey(ExportElementType.LOGICAL_RECORD)) {
                    recordNbr = logicalRecordDataMap.get(ExportElementType.LOGICAL_RECORD).values().size() + 1;
                    innerMap = getInnerMap(rs, recordNbr);
                    logicalRecordDataMap.get(ExportElementType.LOGICAL_RECORD).putAll(innerMap);
                } else {
                    recordNbr = 1;
                    innerMap = getInnerMap(rs, recordNbr);
                    logicalRecordDataMap.put(ExportElementType.LOGICAL_RECORD, innerMap);
                }
                rs.close();  
                rs = null;
                pst1.close();                

                String selectString2 = "SELECT * "
                    + "FROM " + params.getSchema() + ".LRFIELD " 
                    + "WHERE ENVIRONID= ? " 
                    + "AND LOGRECID IN ( " + placeHolders + " )"            
                    + "ORDER BY LOGRECID,ORDINALPOS";            
                
                while (true) {
                    try {
                        pst2 = con.prepareStatement(selectString2);
                        int ndx = 1;
                        pst2.setInt(ndx++, environmentId);
                		for( int i = 0 ; i < logicalRecordIds.size(); i++ ) {
                            pst2.setInt(ndx++, logicalRecordIds.get(i));
                		}
                        pst2.execute();
                        rs = pst2.getResultSet();
                        break;
                    } catch (SQLException se) {
                        if (con.isClosed()) {
                            con = DAOFactoryHolder.getDAOFactory().reconnect();
                        } else {
                            throw se;
                        }
                    } // end reconnect try
                }
                
                if (logicalRecordDataMap.containsKey(ExportElementType.LRFIELD)) {
                    recordNbr = logicalRecordDataMap.get(ExportElementType.LRFIELD).values().size() + 1;
                    innerMap = getInnerMap(rs, recordNbr);
                    logicalRecordDataMap.get(ExportElementType.LRFIELD).putAll(innerMap);
                } else {
                    recordNbr = 1;
                    innerMap = getInnerMap(rs, recordNbr);
                    logicalRecordDataMap.put(ExportElementType.LRFIELD,innerMap);
                }
                rs.close();
                rs = null;
                pst2.close();

                String selectString3 = "SELECT A.* "
                    + "FROM " + params.getSchema() + ".LRFIELDATTR A, " 
                    + params.getSchema() + ".LRFIELD B "
                    + "WHERE A.ENVIRONID=B.ENVIRONID "
                    + "AND A.LRFIELDID=B.LRFIELDID "
                    + "AND B.ENVIRONID= ?  " 
                    + "AND B.LOGRECID IN ( " + placeHolders + " )"            
                    + "ORDER BY B.LOGRECID,B.ORDINALPOS";            
                
                while (true) {
                    try {
                        pst3 = con.prepareStatement(selectString3);
                        int ndx = 1;
                        pst3.setInt(ndx++, environmentId);
                		for( int i = 0 ; i < logicalRecordIds.size(); i++ ) {
                            pst3.setInt(ndx++, logicalRecordIds.get(i));
                		}
                        pst3.execute();
                        rs = pst3.getResultSet();
                        break;
                    } catch (SQLException se) {
                        if (con.isClosed()) {
                            con = DAOFactoryHolder.getDAOFactory().reconnect();
                        } else {
                            throw se;
                        }
                    } // end reconnect try
                }
                
                if (logicalRecordDataMap.containsKey(ExportElementType.LRFIELD_ATTRIBUTE)) {
                    recordNbr = logicalRecordDataMap.get(ExportElementType.LRFIELD_ATTRIBUTE).values().size() + 1;
                    innerMap = getInnerMap(rs, recordNbr);
                    logicalRecordDataMap.get(ExportElementType.LRFIELD_ATTRIBUTE).putAll(innerMap);
                } else {
                    recordNbr = 1;
                    innerMap = getInnerMap(rs, recordNbr);
                    logicalRecordDataMap.put(ExportElementType.LRFIELD_ATTRIBUTE,innerMap);
                }
                rs.close();
                rs = null;
                pst3.close();

                String selectString4 = "SELECT * "
                    + "FROM " + params.getSchema()+ ".LRINDEX "
                    + "WHERE LOGRECID IN ( " + placeHolders + " )"
                    + "AND ENVIRONID = ? "
                    + "ORDER BY LOGRECID";            

                while (true) {
                    try {
                        pst4 = con.prepareStatement(selectString4);
                        int ndx = 1;
                		for( int i = 0 ; i < logicalRecordIds.size(); i++ ) {
                            pst4.setInt(ndx++, logicalRecordIds.get(i));
                		}
                        pst4.setInt(ndx, environmentId);
                        pst4.execute();
                        rs = pst4.getResultSet();
                        break;
                    } catch (SQLException se) {
                        if (con.isClosed()) {
                            con = DAOFactoryHolder.getDAOFactory().reconnect();
                        } else {
                            throw se;
                        }
                    } // end reconnect try
                }
                
                if (logicalRecordDataMap.containsKey(ExportElementType.LR_INDEX)) {
                    recordNbr = logicalRecordDataMap.get(ExportElementType.LR_INDEX).values().size() + 1;
                    innerMap = getInnerMap(rs, recordNbr);
                    logicalRecordDataMap.get(ExportElementType.LR_INDEX).putAll(innerMap);
                } else {
                    recordNbr = 1;
                    innerMap = getInnerMap(rs, recordNbr);
                    logicalRecordDataMap.put(ExportElementType.LR_INDEX, innerMap);
                }
                rs.close();                
                rs = null;
                pst4.close();
                
                String selectString5 = "SELECT B.* "
                    + "FROM " + params.getSchema() + ".LRINDEX A, " 
                    + params.getSchema() + ".LRINDEXFLD B "
                    + "WHERE A.ENVIRONID=B.ENVIRONID "
                    + "AND A.LRINDEXID=B.LRINDEXID "
                    + "AND A.ENVIRONID= ?  " 
                    + "AND A.LOGRECID IN ( " + placeHolders + " )"            
                    + "ORDER BY A.LRINDEXID,B.FLDSEQNBR";            
                
                while (true) {
                    try {
                        pst5 = con.prepareStatement(selectString5);
                        int ndx = 1;
                        pst5.setInt(ndx++, environmentId);
                		for( int i = 0 ; i < logicalRecordIds.size(); i++ ) {
                            pst5.setInt(ndx++, logicalRecordIds.get(i));
                		}
                        pst5.execute();
                        rs = pst5.getResultSet();
                        break;
                    } catch (SQLException se) {
                        if (con.isClosed()) {
                            con = DAOFactoryHolder.getDAOFactory().reconnect();
                        } else {
                            throw se;
                        }
                    } // end reconnect try
                }
                
                if (logicalRecordDataMap.containsKey(ExportElementType.LR_INDEX_FIELD)) {
                    recordNbr = logicalRecordDataMap.get(ExportElementType.LR_INDEX_FIELD).values().size() + 1;
                    innerMap = getInnerMap(rs, recordNbr);
                    logicalRecordDataMap.get(ExportElementType.LR_INDEX_FIELD).putAll(innerMap);
                } else {
                    recordNbr = 1;
                    innerMap = getInnerMap(rs, recordNbr);
                    logicalRecordDataMap.put(ExportElementType.LR_INDEX_FIELD,innerMap);
                }
                rs.close();
                rs = null;
                pst5.close();

                String selectString6 = "SELECT * "
                    + "FROM " + params.getSchema()+ ".LRLFASSOC "
                    + "WHERE LOGRECID IN ( " + placeHolders + " )"
                    + "AND ENVIRONID = ?  "
                    + "ORDER BY LOGRECID,LOGFILEID";            
                                
                while (true) {
                    try {
                        pst6 = con.prepareStatement(selectString6);
                        int ndx = 1;
                		for( int i = 0 ; i < logicalRecordIds.size(); i++ ) {
                            pst6.setInt(ndx++, logicalRecordIds.get(i));
                		}
                        pst6.setInt(ndx, environmentId);
                        pst6.execute();
                        rs = pst6.getResultSet();
                        break;
                    } catch (SQLException se) {
                        if (con.isClosed()) {
                            con = DAOFactoryHolder.getDAOFactory().reconnect();
                        } else {
                            throw se;
                        }
                    } // end reconnect try
                }
                
                if (logicalRecordDataMap.containsKey(ExportElementType.LRLFASSOC)) {
                    recordNbr = logicalRecordDataMap.get(ExportElementType.LRLFASSOC).values().size() + 1;
                    innerMap = getInnerMap(rs, recordNbr);
                    logicalRecordDataMap.get(ExportElementType.LRLFASSOC).putAll(innerMap);
                } else {
                    recordNbr = 1;
                    innerMap = getInnerMap(rs, recordNbr);
                    logicalRecordDataMap.put(ExportElementType.LRLFASSOC,innerMap);
                }                    
                rs.close();                
                pst6.close();                
            }
            catch (SQLException e) {
                throw DataUtilities.createDAOException(
                     "Database error occurred while retrieving the data for Logical Record(s).",e);
            }             
            return logicalRecordDataMap;
    }
    
    public Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getLookupPathData(
        Integer environmentId, List<Integer> lookupPathIds)
        throws DAOException {
        
        Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> lookupPathDataMap = new LinkedHashMap<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>>();
        if (lookupPathIds == null || lookupPathIds.isEmpty()) {
            return lookupPathDataMap;
        }
        
        String placeHolders = generator.getPlaceholders(lookupPathIds.size());
        try {       
            PreparedStatement pst1 = null;
            PreparedStatement pst2 = null;
            PreparedStatement pst3 = null;            
            ResultSet rs = null;
            
            String selectString = "SELECT * "
                + "FROM " + params.getSchema()+ ".LOOKUP "
                + "WHERE LOOKUPID IN ( " + placeHolders + " )"
                + "AND ENVIRONID = ?  "
                + "ORDER BY LOOKUPID";

            while (true) {
                try {
                    pst1 = con.prepareStatement(selectString);
                    int ndx = 1;
            		for( int i = 0 ; i < lookupPathIds.size(); i++ ) {
                        pst1.setInt(ndx++, lookupPathIds.get(i));
            		}
                    pst1.setInt(ndx, environmentId);
                    pst1.execute();
                    rs = pst1.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            int recordNbr;
            Map<Integer, List<XMLTableDataTransfer>> innerMap = null;
            
            if (lookupPathDataMap.containsKey(ExportElementType.LOOKUP)) {
                recordNbr = lookupPathDataMap.get(ExportElementType.LOOKUP).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                lookupPathDataMap.get(ExportElementType.LOOKUP).putAll(innerMap);
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                lookupPathDataMap.put(ExportElementType.LOOKUP,innerMap);
            }
            rs.close();                
            rs = null;
            pst1.close();

            String selectString2 = "SELECT * "
                + "FROM " + params.getSchema() + ".LOOKUPSRCKEY " 
                + "WHERE ENVIRONID= ?  " 
                + "AND LOOKUPID IN ( " + placeHolders + " )"            
                + "ORDER BY LOOKUPID,LOOKUPSTEPID,KEYSEQNBR";            

            while (true) {
                try {
                    pst2 = con.prepareStatement(selectString2);
                    int ndx = 1;
                    pst2.setInt(ndx++, environmentId);
            		for( int i = 0 ; i < lookupPathIds.size(); i++ ) {
                        pst2.setInt(ndx++, lookupPathIds.get(i));
            		}
                    pst2.execute();
                    rs = pst2.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            if (lookupPathDataMap.containsKey(ExportElementType.LOOKUP_SRCKEY)) {
                recordNbr = lookupPathDataMap.get(ExportElementType.LOOKUP_SRCKEY).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                lookupPathDataMap.get(ExportElementType.LOOKUP_SRCKEY).putAll(innerMap);
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                lookupPathDataMap.put(ExportElementType.LOOKUP_SRCKEY, innerMap);
            }
            rs.close();                
            rs = null;
            pst2.close();            

            String selectString3 = "SELECT * "
                + "FROM " + params.getSchema() + ".LOOKUPSTEP " 
                + "WHERE ENVIRONID= ?  " 
                + "AND LOOKUPID IN ( " + placeHolders + " )"            
                + "ORDER BY LOOKUPID,LOOKUPSTEPID";            
            
            while (true) {
                try {
                    pst3 = con.prepareStatement(selectString3);
                    int ndx = 1;
                    pst3.setInt(ndx++, environmentId);
            		for( int i = 0 ; i < lookupPathIds.size(); i++ ) {
                        pst3.setInt(ndx++, lookupPathIds.get(i));
            		}
                    pst3.execute();
                    rs = pst3.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            if (lookupPathDataMap.containsKey(ExportElementType.LOOKUP_STEP)) {
                recordNbr = lookupPathDataMap.get(ExportElementType.LOOKUP_STEP).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                lookupPathDataMap.get(ExportElementType.LOOKUP_STEP).putAll(innerMap);
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                lookupPathDataMap.put(ExportElementType.LOOKUP_STEP, innerMap);
            }                
            rs.close();            
            pst3.close();
        }
        catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving the data for Lookup Path(s).",e);
        }
        
        return lookupPathDataMap;        
    }

    public Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getViewData(
        Integer environmentId, List<Integer> viewIds) throws DAOException {
        Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> viewDataMap = new LinkedHashMap<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>>();
        if (viewIds == null || viewIds.isEmpty()) {
            return viewDataMap;
        }
        
        String placeHolders = generator.getPlaceholders(viewIds.size());
		try {      
            PreparedStatement pst1 = null;
            PreparedStatement pst2 = null;
            PreparedStatement pst3 = null;            
            PreparedStatement pst4 = null;
            PreparedStatement pst5 = null;
            PreparedStatement pst6 = null;                            
            ResultSet rs = null;
            
            String selectString = "SELECT * "
                + "FROM " + params.getSchema()+ ".VIEW "
                + "WHERE VIEWID IN ( " + placeHolders + " ) "
                + "AND ENVIRONID = ?  "            
                + "ORDER BY VIEWID";            
            
            while (true) {
                try {
                    pst1 = con.prepareStatement(selectString);
                    int ndx = 1;
            		for( int i = 0 ; i < viewIds.size(); i++ ) {
                        pst1.setInt(ndx++, viewIds.get(i));
            		}
                    pst1.setInt(ndx, environmentId);
                    pst1.execute();
                    rs = pst1.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            int recordNbr;
            Map<Integer, List<XMLTableDataTransfer>> innerMap = null;
            if (viewDataMap.containsKey(ExportElementType.VIEW)) {
                recordNbr = viewDataMap.get(ExportElementType.VIEW).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                viewDataMap.get(ExportElementType.VIEW).putAll(innerMap);

            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                viewDataMap.put(ExportElementType.VIEW, innerMap);
            }            
            rs.close();  
            rs = null;
            pst1.close();                

            String selectString2 = "SELECT * "
                + "FROM " + params.getSchema() + ".VIEWCOLUMN "
                + "WHERE ENVIRONID= ? " 
                + "AND VIEWID IN ( " + placeHolders + " )"            
                + "ORDER BY VIEWID,COLUMNNUMBER";            
            
            while (true) {
                try {
                    pst2 = con.prepareStatement(selectString2);
                    int ndx = 1;
                    pst2.setInt(ndx++, environmentId);
            		for( int i = 0 ; i < viewIds.size(); i++ ) {
                        pst2.setInt(ndx++, viewIds.get(i));
            		}
                    pst2.execute();
                    rs = pst2.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            if (viewDataMap.containsKey(ExportElementType.VIEW_COLUMN)) {
                recordNbr = viewDataMap.get(ExportElementType.VIEW_COLUMN).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                viewDataMap.get(ExportElementType.VIEW_COLUMN).putAll(innerMap);
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                viewDataMap.put(ExportElementType.VIEW_COLUMN,innerMap);
            }
            rs.close();
            rs = null;
            pst2.close();
            
            String selectString3 = "SELECT * "
                + "FROM " + params.getSchema() + ".VIEWSOURCE " 
                + "WHERE ENVIRONID= ? " 
                + "AND VIEWID IN ( " + placeHolders + " )"            
                + "ORDER BY VIEWID,SRCSEQNBR";            
            
            while (true) {
                try {
                    pst3 = con.prepareStatement(selectString3);
                    int ndx = 1;
                    pst3.setInt(ndx++, environmentId);
            		for( int i = 0 ; i < viewIds.size(); i++ ) {
                        pst3.setInt(ndx++, viewIds.get(i));
            		}
                    pst3.execute();
                    rs = pst3.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            if (viewDataMap.containsKey(ExportElementType.VIEW_SOURCE)) {
                recordNbr = viewDataMap.get(ExportElementType.VIEW_SOURCE).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                viewDataMap.get(ExportElementType.VIEW_SOURCE).putAll(innerMap);
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                viewDataMap.put(ExportElementType.VIEW_SOURCE,innerMap);        
            }
            rs.close();
            rs = null;
            pst3.close();

            String selectString4 = "SELECT * "
                + "FROM " + params.getSchema()+ ".VIEWCOLUMNSOURCE "
                + "WHERE VIEWID IN ( " + placeHolders + " )"
                + "AND ENVIRONID = ? "
                + "ORDER BY VIEWID,VIEWCOLUMNSOURCEID";            

            while (true) {
                try {
                    pst4 = con.prepareStatement(selectString4);
                    int ndx = 1;
            		for( int i = 0 ; i < viewIds.size(); i++ ) {
                        pst4.setInt(ndx++, viewIds.get(i));
            		}
                    pst4.setInt(ndx, environmentId);
                    pst4.execute();
                    rs = pst4.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            if (viewDataMap.containsKey(ExportElementType.VIEW_COLUMN_SOURCE)) {
                recordNbr = viewDataMap.get(ExportElementType.VIEW_COLUMN_SOURCE).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                viewDataMap.get(ExportElementType.VIEW_COLUMN_SOURCE).putAll(innerMap);
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                viewDataMap.put(ExportElementType.VIEW_COLUMN_SOURCE,innerMap);
            }
            rs.close();                
            rs = null;
            pst4.close();
            
            String selectString5 = "SELECT * "
                + "FROM " + params.getSchema()+ ".VIEWSORTKEY "
                + "WHERE VIEWID IN ( " + placeHolders + " )"
                + "AND ENVIRONID = ?  "
                + "ORDER BY VIEWID,KEYSEQNBR";            
            
            while (true) {
                try {
                    pst5 = con.prepareStatement(selectString5);
                    int ndx = 1;
            		for( int i = 0 ; i < viewIds.size(); i++ ) {
                        pst5.setInt(ndx++, viewIds.get(i));
            		}
                    pst5.setInt(ndx, environmentId);
                    pst5.execute();
                    rs = pst5.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            if (viewDataMap.containsKey(ExportElementType.VIEW_SORT_KEY)) {
                recordNbr = viewDataMap.get(ExportElementType.VIEW_SORT_KEY).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                viewDataMap.get(ExportElementType.VIEW_SORT_KEY).putAll(innerMap);
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                viewDataMap.put(ExportElementType.VIEW_SORT_KEY,innerMap);
            }
            rs.close();
            rs = null;
            pst5.close();

            String selectString6 = "SELECT A.* "
                + "FROM " + params.getSchema()+ ".VIEWHEADERFOOTER A,"
                + params.getSchema()+ ".VIEW B "
                + "WHERE A.ENVIRONID=B.ENVIRONID "
                + "AND A.VIEWID=B.VIEWID "
                + "AND B.VIEWID IN ( " + placeHolders + " )"
                + "AND B.ENVIRONID = ? "
                + "AND B.VIEWTYPECD IN ('SUMRY','DETL') " 
                + "AND B.OUTPUTMEDIACD = 'HCOPY' "
                + "ORDER BY VIEWID,ROWNUMBER,COLNUMBER";            
                            
            while (true) {
                try {
                    pst6 = con.prepareStatement(selectString6);
                    int ndx = 1;
            		for( int i = 0 ; i < viewIds.size(); i++ ) {
                        pst6.setInt(ndx++, viewIds.get(i));
            		}
                    pst6.setInt(ndx, environmentId);
                    pst6.execute();
                    rs = pst6.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            if (viewDataMap.containsKey(ExportElementType.VIEW_HEADER_FOOTER)) {
                recordNbr = viewDataMap.get(ExportElementType.VIEW_HEADER_FOOTER).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                viewDataMap.get(ExportElementType.VIEW_HEADER_FOOTER).putAll(innerMap);
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                viewDataMap.put(ExportElementType.VIEW_HEADER_FOOTER,innerMap);
            }
            rs.close();                
            pst6.close();                            
        }
        catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving the data for View(s).",e);
        }
        
        return viewDataMap;
    }
    
    @Override
    public Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getViewFolderData(
        Integer environmentId, List<Integer> viewFolderIds) throws DAOException {
        
        Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> vfDataMap = 
            new LinkedHashMap<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>>();
        if (viewFolderIds == null || viewFolderIds.isEmpty()) {
            return vfDataMap;
        }
        
        try {
            
        	String placeHolders = generator.getPlaceholders(viewFolderIds.size());
            
            PreparedStatement pst = null;                            
            ResultSet rs = null;
            
            String selectString = "SELECT * "
                + "FROM " + params.getSchema()+ ".VIEWFOLDER "
                + "WHERE VIEWFOLDERID IN ( " + placeHolders + " )"
                + "AND ENVIRONID = ? "            
                + "ORDER BY VIEWFOLDERID";            
    
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    int ndx = 1;
            		for( int i = 0 ; i < viewFolderIds.size(); i++ ) {
                        pst.setInt(ndx++, viewFolderIds.get(i));
            		}
                    pst.setInt(ndx, environmentId);
                    pst.execute();
                    rs = pst.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            int recordNbr;
            Map<Integer, List<XMLTableDataTransfer>> innerMap = null;
            if (vfDataMap.containsKey(ExportElementType.VIEWFOLDER)) {
                recordNbr = vfDataMap.get(ExportElementType.VIEWFOLDER).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                vfDataMap.get(ExportElementType.VIEWFOLDER).putAll(innerMap);
    
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                vfDataMap.put(ExportElementType.VIEWFOLDER, innerMap);
            }            
            rs.close();  
            rs = null;
            pst.close();                

            selectString = "SELECT * "
                + "FROM " + params.getSchema()+ ".VFVASSOC "
                + "WHERE VIEWFOLDERID IN ( " + placeHolders + " )"
                + "AND ENVIRONID = ? "            
                + "ORDER BY VIEWFOLDERID, VIEWID";            
    
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    int ndx = 1;
            		for( int i = 0 ; i < viewFolderIds.size(); i++ ) {
                        pst.setInt(ndx++, viewFolderIds.get(i));
            		}
                    pst.setInt(ndx, environmentId);
                    pst.execute();
                    rs = pst.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            innerMap = null;
            if (vfDataMap.containsKey(ExportElementType.VFVASSOC)) {
                recordNbr = vfDataMap.get(ExportElementType.VFVASSOC).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                vfDataMap.get(ExportElementType.VFVASSOC).putAll(innerMap);
    
            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                vfDataMap.put(ExportElementType.VFVASSOC, innerMap);
            }            
            rs.close();  
            rs = null;
            pst.close();                
            
        }
        catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving the data for View Folder(s).",e);
        }
        
        return vfDataMap;
    }
    
    public Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getControlRecordData(
        Integer environmentId, List<Integer> controlRecordIds)
        throws DAOException {
        
        Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> controlRecordDataMap = new LinkedHashMap<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>>();
        if (controlRecordIds == null || controlRecordIds.isEmpty()) {
            return controlRecordDataMap;
        }
        
        String placeHolders = generator.getPlaceholders(controlRecordIds.size());
        
        try {        
            PreparedStatement pst = null;
            ResultSet rs = null;
            
            String selectString = "SELECT * "
                + "FROM " + params.getSchema()+ ".CONTROLREC "
                + "WHERE CONTROLRECID IN ( " + placeHolders + " )"
                + "AND ENVIRONID = ? "
                + "ORDER BY CONTROLRECID";
            
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    int ndx = 1;
            		for( int i = 0 ; i < controlRecordIds.size(); i++ ) {
                        pst.setInt(ndx++, controlRecordIds.get(i));
            		}
                    pst.setInt(ndx, environmentId);
                    pst.execute();
                    rs = pst.getResultSet();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        // lost database connection, so reconnect and retry
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                } // end reconnect try
            }
            
            int recordNbr;                
            Map<Integer, List<XMLTableDataTransfer>> innerMap = null;            
            if (controlRecordDataMap.containsKey(ExportElementType.CONTROL_RECORD)) {
                recordNbr = controlRecordDataMap.get(
                    ExportElementType.CONTROL_RECORD).values().size() + 1;
                innerMap = getInnerMap(rs, recordNbr);
                controlRecordDataMap.get(ExportElementType.CONTROL_RECORD).putAll(innerMap);

            } else {
                recordNbr = 1;
                innerMap = getInnerMap(rs, recordNbr);
                controlRecordDataMap.put(ExportElementType.CONTROL_RECORD,innerMap);    
            }                
            rs.close();                
            pst.close();
        }
        catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving the data for Control Record(s).",e);
        }   
        return controlRecordDataMap;        
    }
	

	private Map<Integer, List<XMLTableDataTransfer>> getInnerMap(ResultSet rs,
			int recordNbr) throws DAOException {
		Map<Integer, List<XMLTableDataTransfer>> innerMap = new LinkedHashMap<Integer, List<XMLTableDataTransfer>>();
		try {
			while (rs.next()) {
				int columnCount = rs.getMetaData().getColumnCount();
				List<XMLTableDataTransfer> record = new ArrayList<XMLTableDataTransfer>();
				for (int i = 1; i <= columnCount; i++) {
					String columnValue = null;
					String columnName = rs.getMetaData().getColumnName(i);
					int dataType = rs.getMetaData().getColumnType(i);
					int size = rs.getMetaData().getColumnDisplaySize(i);
                    if (dataType == Types.CLOB)  {
                        Clob clob = rs.getClob(i);                        
                        if (clob != null) {
                            columnValue = clob.getSubString(1, (int) clob.length());
                        }                                                
                    }
                    else if (dataType == Types.TIMESTAMP)  {
                        Timestamp ts = rs.getTimestamp(i);
                        if (ts != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat(DateFormat.WE_XML_DATE_FORMAT);
                            Date date = new Date(ts.getTime());
                            columnValue = sdf.format(date);
                        }
					}
					else {
						// convert the object value to string.
						Object value = rs.getObject(i);
						if (value != null) {
							    columnValue = value.toString();
						}
					}
                    if ( dataType == Types.CLOB || 
                        (dataType == Types.VARCHAR && (size == 30000 || columnName.equalsIgnoreCase("DBMSSQL") ||
                        columnName.equalsIgnoreCase("formatfiltlogic") || 
                        columnName.equalsIgnoreCase("formatcalclogic") || 
                        columnName.equalsIgnoreCase("extractcalclogic") || 
                        columnName.equalsIgnoreCase("extractfiltlogic") ||
                        columnName.equalsIgnoreCase("extractoutputlogic"))) ) {
    					XMLTableDataTransfer tableDataTrans = new XMLTableDataTransfer(columnValue, columnName, true);
    					record.add(tableDataTrans);
                    }
                    else {
                        XMLTableDataTransfer tableDataTrans = new XMLTableDataTransfer(columnValue, columnName);
                        record.add(tableDataTrans);                        
                    }
				}
				innerMap.put(recordNbr, record);
				recordNbr++;
			}
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while retrieving data for a metadata component.",e);
		}
		return innerMap;
	}

	/**
	 * This method is used to convert a list of <code>Integer</code> variables
	 * into a comma delimited String of all the <code>Integer</code> variables.
	 * This is used to convert a list of ids to a comma delimited String
	 * 
	 * @param listOfIntegerVariables
	 *            : The List of <code>Integer</code> variables.
	 * @return : A comma delimited String
	 */
	private String idsListToString(List<Integer> listOfIntegerVariables) {
		String commaDelimitedString = "";
		for (Integer integerVariable : listOfIntegerVariables) {
			commaDelimitedString += integerVariable.toString() + ",";
		}
		commaDelimitedString = commaDelimitedString.substring(0,
				commaDelimitedString.length() - 1);

		return commaDelimitedString;

	}

}
