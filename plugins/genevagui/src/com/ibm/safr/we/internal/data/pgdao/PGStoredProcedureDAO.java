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
import java.util.logging.Logger;

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.dao.StoredProcedureDAO;
import com.ibm.safr.we.internal.data.PGSQLGenerator;

/**
 * This class is used to implement the unimplemented methods of
 * <b>StoredProcedureDAO</b>. 
 */
public class PGStoredProcedureDAO implements StoredProcedureDAO {

    static transient Logger logger = 
        Logger.getLogger("com.ibm.safr.we.internal.data.dao.PGStoredProcedureDAO");

    private Connection con;
    private ConnectionParameters params;
    private PGSQLGenerator generator = new PGSQLGenerator();

    /**
     * Constructor for this class
     * 
     * @param con
     *            : The connection set for database access.
     * @param params
     *            : The connection parameters which define the URL, userId and
     *            other details of the connection.
     */
    public PGStoredProcedureDAO(Connection con, ConnectionParameters params) {
        this.con = con;
        this.params = params;
    }

    /**
     * 
     */
    public String getVersion() throws DAOException {

        String storedProcedureName = "getversion";
        String result = "";

        //While true I think is to allow for broken database connection
        while (true) {
            try {
                String statement = "select " + params.getSchema() + ".getversion()";
                PreparedStatement proc;
                try {
                    proc = con.prepareStatement(statement);
                    ResultSet rs = proc.executeQuery();
    	            while (rs.next()) {
    	            	result = rs.getString(1);;
    	            }
    	            rs.close();
    	            proc.close();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        // lost database connection, so reconnect and retry
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                }
            } catch (SQLException e) {
                throw DataUtilities.createDAOException(
                        "Database error occurred while getting stored procedure version.", e);
            }
        }
        return result;
    }

}
