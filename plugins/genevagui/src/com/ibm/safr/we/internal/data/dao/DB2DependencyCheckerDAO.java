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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.DependencyUsageType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.DependencyCheckerDAO;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.internal.data.SQLGenerator;

/**
 * This class is used to implement the unimplemented methods of
 * <b>DependencyCheckerDAO</b>. This class contains the methods to related to
 * Dependency checker which require database access.
 * 
 */
public class DB2DependencyCheckerDAO implements DependencyCheckerDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.DB2DependencyCheckerDAO");

	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private SQLGenerator generator = new SQLGenerator();

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
	public DB2DependencyCheckerDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

    public Map<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>> getDependentComponents(
        ComponentType compType, Integer componentId, Integer environmentId, boolean directDepsOnly) {
        Map<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>> result = null;
        
        if (compType.equals(ComponentType.ViewFolder)) {
            result = getDependentComponentsViewFolder(compType, componentId, environmentId, directDepsOnly);
        }
        if (compType.equals(ComponentType.View)) {
            result = getDependentComponentsView(compType, componentId, environmentId, directDepsOnly);
        }
        else {
            result =  getDependentComponentsOther(compType, componentId, environmentId, directDepsOnly);            
        }
        
        return result;
    }
	
	private Map<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>> getDependentComponentsViewFolder(
        ComponentType compType, Integer componentId, Integer environmentId, boolean directDepsOnly) {
	    
        // Get View ID's
        List<Integer> viewIds = new ArrayList<Integer>();
        try {
            PreparedStatement pst = null;                            
            ResultSet rs = null;        
            String selectString = "SELECT VIEWID "
                + "FROM " + params.getSchema()+ ".VFVASSOC "
                + "WHERE VIEWFOLDERID = ?  "
                + "AND ENVIRONID = ?  "            
                + "ORDER BY VIEWFOLDERID, VIEWID";            
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, componentId);
                    pst.setInt(2, environmentId );
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
        
        Map<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>> dependentComponents = 
            new LinkedHashMap<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>>();
        
        for (Integer viewId : viewIds) {
            callDepsSP(viewId, environmentId, directDepsOnly,
                dependentComponents, true, "GETDEPSVIEW");
        }
        
        // remove view folder dependencies 
        dependentComponents.remove(ComponentType.ViewFolder);
        
        return dependentComponents;
    }

    private Map<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>> getDependentComponentsView(
        ComponentType compType, Integer componentId, Integer environmentId, boolean directDepsOnly) {
        
        Map<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>> result = 
            getDependentComponentsOther(compType, componentId, environmentId, directDepsOnly);
        // remove view folder dependencies 
        result.remove(ComponentType.View);
        return result;
    }
        
	
    public Map<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>> getDependentComponentsOther(
			ComponentType compType, Integer componentId, Integer environmentId, boolean directDepsOnly)
			throws DAOException {
		Map<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>> dependentComponents = 
		    new LinkedHashMap<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>>();
		Boolean extraInputParams = false;
		String storedProcedureName = "";

		if (compType == ComponentType.PhysicalFile) {
			storedProcedureName = "GETDEPSPF";
			extraInputParams = true;
		} else if (compType == ComponentType.LogicalFile) {
			storedProcedureName = "GETDEPSLF";
			extraInputParams = true;
		} else if (compType == ComponentType.LogicalRecord) {
			storedProcedureName = "GETDEPSLR";
			extraInputParams = true;
		} else if (compType == ComponentType.LookupPath) {
			storedProcedureName = "GETDEPSLP";
			extraInputParams = true;
		} else if (compType == ComponentType.View) {
			storedProcedureName = "GETDEPSVIEW";
			extraInputParams = true;
		} else if (compType == ComponentType.UserExitRoutine) {
			storedProcedureName = "GETDEPSEXIT";
			extraInputParams = false;
		} else if (compType == ComponentType.LogicalRecordField) {
			storedProcedureName = "GETDEPSFIELD";
			extraInputParams = false;
		} else {
			return dependentComponents;
		}
        callDepsSP(componentId, environmentId, directDepsOnly,
            dependentComponents, extraInputParams, storedProcedureName);
        
		return dependentComponents;
	}

    protected void callDepsSP(Integer componentId, Integer environmentId, boolean directDepsOnly,
        Map<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>> dependentComponents,
        Boolean extraInputParams, String storedProcedureName) {
        
        boolean success = false;
        while (!success) {                                                  		
    		try {
                // start a transaction
                DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();    		    
    			String statement = "";
    			CallableStatement proc;
    			ResultSet rs = null;
    			while (true) {
    				try {
    					if (extraInputParams) {
    						statement = generator.getStoredProcedure(params
    								.getSchema(), storedProcedureName, 5);
    						proc = con.prepareCall(statement);
    						int i = 1;
    						proc.setInt(i++, environmentId);
    						proc.setInt(i++, componentId);
    						proc.setString(i++, directDepsOnly ? "3" : "2");
    						proc.setInt(i++, safrLogin.getGroupId());
    						proc.setInt(i++, 0);
    					} else {
    						statement = generator.getStoredProcedure(params.getSchema(), storedProcedureName, 2);
    						proc = con.prepareCall(statement);
    						int i = 1;
    						proc.setInt(i++, environmentId);
    						proc.setInt(i++, componentId);
    					}
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
    				DependentComponentTransfer depComp = new DependentComponentTransfer();
    
    				String componentType = DataUtilities.trimString((rs.getString(1)).toUpperCase());
    				ComponentType componentTypeSP = DataUtilities.getComponentTypeFromString(componentType);
    
    				String childTypeString = DataUtilities.trimString((rs.getString(2)).toUpperCase());   				
    				DependencyUsageType childType = DataUtilities.getChildTypeFromString(childTypeString);
    
    				if (rs.getInt(3) > 0) {
    					depComp.setId(rs.getInt(3));
    					depComp.setName(DataUtilities.trimString(rs.getString(4)));
    
    					if (dependentComponents.containsKey(componentTypeSP)) {
    
    						if (dependentComponents.get(componentTypeSP).containsKey(childType)) {
    							dependentComponents.get(componentTypeSP).get(childType).add(depComp);
    
    						} else {
    					        List<DependentComponentTransfer> dependentComponentInnerList = new ArrayList<DependentComponentTransfer>();
    							dependentComponentInnerList.add(depComp);
    							dependentComponents.get(componentTypeSP).put(childType, dependentComponentInnerList);
    						}
    
    					} else {
    				        List<DependentComponentTransfer> dependentComponentInnerList = new ArrayList<DependentComponentTransfer>();
    						dependentComponentInnerList.add(depComp);
    						Map<DependencyUsageType, List<DependentComponentTransfer>> innerMap = 
    						    new HashMap<DependencyUsageType, List<DependentComponentTransfer>>();
    						innerMap.put(childType, dependentComponentInnerList);
    						dependentComponents.put(componentTypeSP, innerMap);
    					}
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
                    "Database error occurred while retrieving all the related components of a metadata component.",e);
            } finally {
                DAOFactoryHolder.getDAOFactory().getDAOUOW().end();
            } // end transaction try
        } // end transaction while loop              
    }
	
}
