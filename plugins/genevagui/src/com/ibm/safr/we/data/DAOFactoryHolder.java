package com.ibm.safr.we.data;

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


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.internal.data.DB2DAOFactory;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.preferences.SAFRPreferences;

public class DAOFactoryHolder {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.data.DAOFactoryHolder");

	static DAOFactory daoFactory = null;
    static DAOFactory backupDaoFactory = null;

	static void initWithPreferences() throws DAOException {
		// Get Connection settings from preferences
		Preferences preferences = SAFRPreferences.getDefaultConnectionPreferences();
		if (preferences == null) {
			throw new NullPointerException(
					"User Preferences file for connection is either absent or corrupted.");
		}
		Preferences connection = preferences;
		try {

			ConnectionParameters params = new ConnectionParameters();
			String dbtype = connection.get(UserPreferencesNodes.DATABASETYPE, "");
			String enableSSL = SAFRPreferences.getSAFRPreferences().get(UserPreferencesNodes.ENABLE_SSL, "");
			if (dbtype != null && dbtype.length()>0) {
			    params.setType(DBType.valueOf(dbtype));
			}
			if(params.getType() == DBType.Db2) {
				String url = "jdbc:"
						+ connection.get(UserPreferencesNodes.DATABASETYPE, "")
								.toLowerCase() + "://"
						+ connection.get(UserPreferencesNodes.SERVER, "") + ":"
						+ connection.get(UserPreferencesNodes.PORT, "") + "/"
						+ connection.get(UserPreferencesNodes.DATABASENAME, "");
				if(enableSSL.equalsIgnoreCase("Y")) {
					String trustStore = SAFRPreferences.getSAFRPreferences().get(UserPreferencesNodes.SSL_TRUST_STORELOCATION, "");
					url += ":sslConnection=true;sslTrustStoreLocation=" + trustStore + ";";
				}
						// Uncomment for JDBC trace 
	/*            		+ ":traceDirectory=c:\\temp"
	            		+ ";traceFile=trace"
	            		+ ";traceFileAppend=false"
	            		+ ";traceLevel=" + com.ibm.db2.jcc.DB2BaseDataSource.TRACE_STATEMENT_CALLS + ";";*/
	
				params.setUrl(url);
	
				params.setDatabase(connection.get(
						UserPreferencesNodes.DATABASENAME, ""));
				params.setPort(connection.get(UserPreferencesNodes.PORT, ""));
				params.setServer(connection.get(UserPreferencesNodes.SERVER, ""));
	
				params.setSchema(connection.get(UserPreferencesNodes.SCHEMA, ""));
				params.setUserName(connection.get(UserPreferencesNodes.USERID, ""));
				params.setPassWord(SAFRUtilities.decrypt(connection.get(UserPreferencesNodes.PD, "")));
			} else if(params.getType() == DBType.PostgresQL) {
				if (dbtype != null && dbtype.length()>0) {
				    params.setType(DBType.valueOf(dbtype));
				}
				String url = "jdbc:"
						+ connection.get(UserPreferencesNodes.DATABASETYPE, "")
								.toLowerCase() + "://"
						+ connection.get(UserPreferencesNodes.SERVER, "") + ":"
						+ connection.get(UserPreferencesNodes.PORT, "") + "/"
						+ connection.get(UserPreferencesNodes.DATABASENAME, "") + "?currentSchema="
						+ connection.get(UserPreferencesNodes.SCHEMA, "");

				params.setUrl(url);

				params.setDatabase(connection.get(
						UserPreferencesNodes.DATABASENAME, ""));
				params.setPort(connection.get(UserPreferencesNodes.PORT, ""));
				params.setServer(connection.get(UserPreferencesNodes.SERVER, ""));

				params.setSchema(connection.get(UserPreferencesNodes.SCHEMA, ""));
				params.setUserName(connection.get(UserPreferencesNodes.USERID, ""));				
			}
			genDAOFactory(params);

		} catch (IllegalStateException e) {
			throw new IllegalStateException(
					"User Preferences file for connection is either absent or corrupted.");
		}
	}

	public static void initWithPropertiesFile(InputStream file) throws DAOException {

		try {
			BufferedInputStream stream = new BufferedInputStream(file);
			Properties prop = new Properties();
			prop.load(stream);
			ConnectionParameters params = new ConnectionParameters();
			params.setType(DBType.valueOf((String) prop.get("TYPE")));
			params.setServer((String) prop.get("SERVER"));
			params.setPort((String) prop.get("PORT"));
			params.setDatabase((String) prop.get("DATABASE"));
			params.setSchema((String) prop.get("SCHEMA"));
			params.setUserName((String) prop.get("USER"));
			params.setPassWord((String) prop.get("PASS"));

            String url = "jdbc:"
                + ((String)prop.get("TYPE")).toLowerCase() + "://"
                + prop.get("SERVER") + ":"
                + prop.get("PORT") + "/"
                + prop.get("DATABASE");
            if(params.getType() == DBType.PostgresQL) {
            	url += "?currentSchema=" + prop.get("SCHEMA");
            }
                // Uncomment for JDBC trace 
/*                  + ":traceDirectory=c:\\temp"
                + ";traceFile=trace"
                + ";traceFileAppend=false"
                + ";traceLevel=" + com.ibm.db2.jcc.DB2BaseDataSource.TRACE_STATEMENT_CALLS + ";";*/
			
            params.setUrl(url);
			genDAOFactory(params);

			String userId = prop.getProperty("SAFRUSER");
			Integer envId = Integer.valueOf(prop.getProperty("SAFRENV"));
			Integer groupId = Integer.valueOf(prop.getProperty("SAFRGRP"));
			if (groupId == null) {
				groupId = 0;
			}
			UserSessionParameters uparams = new UserSessionParameters(userId,
					envId, groupId);
			daoFactory.setSAFRLogin(uparams);
		} catch (IOException e) {
			throw new DAOException(
					"Unable to load properies for the connection." + SAFRUtilities.LINEBREAK
							+ e.toString());
		}

	}

    public static void initFromEnvironmeent() {
        // Instead of using a file get the data from some environment variables
        // That way we do not need to store passwords in the code base
        //try {
            ConnectionParameters params = null;
            String dbType = System.getenv("DBTYPE");
            if(dbType == null) { //Prerequisite
                logger.severe("No Database type defined");
                return;
            }
            if (dbType.equalsIgnoreCase("PostgresQl")) {
                params = new ConnectionParameters();
                params.setType(DBType.PostgresQL);
                params.setPort(System.getenv("DBPORT") != null ? System.getenv("DBPORT") : "5432");
                params.setUserName(System.getenv("DBUSER") != null ? System.getenv("DBUSER") : "postgres");
                params.setPassWord(System.getenv("DBPASS") != null ? System.getenv("DBPASS") : "postgres");
                params.setSchema(System.getenv("DBSCHEMA") != null ? System.getenv("DBSCHEMA") : "gendev");
                params.setDatabase(System.getenv("DBNAME") != null ? System.getenv("DBNAME") : "genevaers");
                params.setServer(System.getenv("DBSERVER") != null ? System.getenv("DBSERVER") : "localhost");
            } else if (dbType.equalsIgnoreCase("DB2")) {
                params = new ConnectionParameters();
                params.setType(DBType.Db2);
                params.setPort(System.getenv("DBPORT") != null ? System.getenv("DBPORT") : "5432");
                params.setUserName(System.getenv("DBUSER") != null ? System.getenv("DBUSER") : "postgres");
                params.setPassWord(System.getenv("DBPASS") != null ? System.getenv("DBPASS") : "postgres");
                params.setSchema(System.getenv("DBSCHEMA") != null ? System.getenv("DBSCHEMA") : "gendev");
                params.setDatabase(System.getenv("DBNAME") != null ? System.getenv("DBNAME") : "genevaers");
                params.setServer(System.getenv("DBSERVER") != null ? System.getenv("DBSERVER") : "genevaers");
            }

            if (params == null) {
                logger.warning("Database type invalid");
            } else {

                String url = "jdbc:"
                        + params.getType().toString().toLowerCase() + "://"
                        + params.getServer() + ":"
                        + params.getPort() + "/"
                        + params.getDatabase() ;
                if (params.getType() == DBType.PostgresQL) {
                    url += "?currentSchema=" + params.getSchema();
                }
                // Uncomment for JDBC trace
                /*
                 * + ":traceDirectory=c:\\temp"
                 * + ";traceFile=trace"
                 * + ";traceFileAppend=false"
                 * + ";traceLevel=" +
                 * com.ibm.db2.jcc.DB2BaseDataSource.TRACE_STATEMENT_CALLS +
                 * ";";
                 */

                params.setUrl(url);
                genDAOFactory(params);

                String userId = System.getenv("GERSUSER") != null ? System.getenv("GERSUSER") : "postgres";
                Integer envId = Integer.valueOf(System.getenv("GERSENV") != null ? System.getenv("GERSENV") : "1");
                Integer groupId = Integer.valueOf(System.getenv("GERSGRP") != null ? System.getenv("GERSGRP") : "0");
                UserSessionParameters uparams = new UserSessionParameters(userId,
                        envId, groupId);
                daoFactory.setSAFRLogin(uparams);
            }
//        } catch (IOException e) {
//            throw new DAOException(
//                    "Unable to load properies for the connection." + SAFRUtilities.LINEBREAK
//                            + e.toString());
//        }

    }

    static void genDAOFactory(ConnectionParameters params) throws DAOException {
		if (daoFactory != null) {
			if (params.getType() == DBType.Db2) {
				((DB2DAOFactory) daoFactory).reconnect();			
			} else if (params.getType() == DBType.PostgresQL) {
				((PGDAOFactory) daoFactory).reconnect();
			} else {
				logger.severe("Invalid Database type" + params.getType().toString());
				throw new DAOException("Invalid Database type");
			}
		} else {
			if (params.getType() == DBType.Db2) {
				daoFactory = new DB2DAOFactory(params);
			}else if (params.getType() == DBType.PostgresQL) {
				daoFactory = new PGDAOFactory(params);
			} else {
				logger.severe("Invalid Database type" + params.getType().toString());
				throw new DAOException("Invalid Database type");
			}
		}
	}

	public static DAOFactory getDAOFactory() throws DAOException {
		if (daoFactory == null) {
			initWithPreferences();
		}
		return daoFactory;
	}
	
    public static void setDaoFactory(DAOFactory daoFactory) {
        DAOFactoryHolder.daoFactory = daoFactory;
    }	

	/**
	 * Get the ODBC connection string for the selected database type. This
	 * string is prepared based on the configuration done in Connection Manager.
	 * 
	 * @return the ODBC string for the current DB connection or null if the
	 *         selected database type is not DB2.<br>
	 * <br>
	 *         Example: for DB2 server the string returned by this function
	 *         could be:<br>
	 *         <code>Driver={IBM DB2 ODBC DRIVER};Hostname=bmsdev2.boulder.ibm.com;Port=5050;Protocol=TCPIP;UID=kaputin;PWD=mypswd;Database=BMS2DB2Z</code>
	 */
	public static String getODBCConnectionString() {
		String returnStr = null;
		ConnectionParameters params = daoFactory.getConnectionParameters();
		if (params.getType() == DBType.Db2) {
			returnStr = "Driver=";
			returnStr += "{IBM DB2 ODBC SAFR WE DRIVER};";
			returnStr += "Hostname=" + params.getServer() + ";";
			returnStr += "Port=" + params.getPort() + ";";
			returnStr += "Protocol=TCPIP;";
			returnStr += "UID=" + params.getUserName() + ";";
			returnStr += "PWD=" + params.getPassWord() + ";";
			returnStr += "Database=" + params.getDatabase();
		}
		return returnStr;
	}

	/**
	 * Get the ODBC connection string for the selected database type for the old compiler. This
	 * string is prepared based on the configuration done in Connection Manager.
	 * 
	 * @return the ODBC string for the current DB connection or null if the
	 *         selected database type is not DB2.<br>
	 * <br>
	 *         Example: for DB2 server the string returned by this function
	 *         could be:<br>
	 *         <code>Driver={IBM DB2 ODBC SAFR WE DRIVER};Hostname=bmsdev2.boulder.ibm.com;Port=5050;Protocol=TCPIP;UID=kaputin;PWD=mypswd;Database=BMS2DB2Z</code>
	 */
	public static String getODBCConnectionStringOld() {
		String returnStr = null;
		ConnectionParameters params = daoFactory.getConnectionParameters();
		if (params.getType() == DBType.Db2) {
			returnStr = "Driver=";
			returnStr += "{IBM DB2 ODBC SAFR WE DRIVER};";
			returnStr += "Hostname=" + params.getServer() + ";";
			returnStr += "Port=" + params.getPort() + ";";
			returnStr += "Protocol=TCPIP;";
			returnStr += "UID=" + params.getUserName() + ";";
			returnStr += "PWD=" + params.getPassWord() + ";";
			returnStr += "Database=" + params.getDatabase();
		}
		return returnStr;
	}
	
	/**
	 * Disconnects the active connection.
	 * 
	 * @throws DAOException
	 *             if an error occurs.
	 */
	public static void closeDAOFactory() throws DAOException {
		if (daoFactory != null) {
			try {
				daoFactory.disconnect();
			} finally {
				// set the factory reference to null so that it can retry
				// connection using latest preference entries.
				daoFactory = null;
			}
		}
	}

	/**
	 * Disconnects the backup connection.
	 * 
	 * @throws DAOException
	 *             if an error occurs.
	 */
	public static void closeBackupDAOFactory() throws DAOException {
		if (backupDaoFactory != null) {
			try {
				backupDaoFactory.disconnect();
			} finally {
				// set the factory reference to null so that it can retry
				// connection using latest preference entries.
				backupDaoFactory = null;
			}
		}
	}
	
	/**
	 * backupDaoFactory	 * 
	 */
	public static void backupDAOFactory() {
		backupDaoFactory = daoFactory;
		daoFactory = null;
	}

	/**
	 * restoreDaoFactory
	 * 
	 * @throws DAOException
	 *             if an error occurs.
	 */
	public static void restoreDAOFactory() throws DAOException {
		if (daoFactory != null) {
			daoFactory.disconnect();			
		}
		daoFactory = backupDaoFactory;
		backupDaoFactory = null;
	}
	
}
