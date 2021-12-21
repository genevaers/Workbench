package com.ibm.safr.we.model;

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


import java.io.InputStream;

import com.ibm.safr.we.TimingMap;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.transfer.EnvironmentTransfer;
import com.ibm.safr.we.data.transfer.UserTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.base.SAFRObject;
import com.ibm.safr.we.security.UserSession;

/**
 * This class maintains singleton objects of {@link SAFRFactory} and
 * {@link UserSession}
 * 
 */
public class SAFRApplication extends SAFRObject {

	static private SAFRFactory factory;
    static private UserSession userSession;
	static private String databaseSchema;
	static private SAFRModelCount modelCount;
	static private TimingMap timingMap;

    /**
	 * This method is used to get {@link SAFRFactory} object.
	 * 
	 * @return {@link SAFRFactory} object.
	 */
	static public SAFRFactory getSAFRFactory() {

		if (factory == null) {
			factory = new SAFRFactory();
		}
		return factory;
	}

	/**
	 * For unit testing
	 * @param factory
	 */
    public static void setFactory(SAFRFactory factory) {
        SAFRApplication.factory = factory;
    }
	
	/**
	 * This method is used to set current {@link UserSession}. It also sets the
	 * user session parameters in the data layer.
	 * 
	 * @param uSession
	 *            to set as current {@link UserSession}.
	 * @throws DAOException
	 * @throws NullPointerException
	 *             if the user session object is null.
	 */
	static public void setUserSession(UserSession uSession) throws DAOException {
		if (uSession == null) {
			throw new NullPointerException();
		}
		userSession = uSession;

		Group group = userSession.getGroup();
		User currentUser = userSession.getUser();
		Environment environment = userSession.getEnvironment();
		Integer envId = environment.getId();

		Integer groupId;
		if (group == null) {
			groupId = 0;
		} else {
			groupId = group.getId();
		}

		DAOFactoryHolder.getDAOFactory().setSAFRLogin(
				new UserSessionParameters(currentUser.getUserid(), envId,
						groupId));
	}

	static public void initDummyUserSession() {
	    UserTransfer utran = new UserTransfer();
	    utran.setUserid("");
	    utran.setAdmin(true);
        User user = new User(utran);
        EnvironmentTransfer etran = new EnvironmentTransfer();
        etran.setId(0);
        Environment env = new Environment(etran);
        UserSession uSession = new UserSession(user, env, null);
        SAFRApplication.setUserSession(uSession);
	}
	
    /**
     * This method is used to set current {@link UserSession}. It also sets the
     * user session parameters in the data layer.
     * 
     * @param uSession
     *            to set as current {@link UserSession}.
     */
    static public void setUserSessionModel(UserSession uSession) {
        userSession = uSession;
    }
	
	/**
	 * This method is used to get current {@link UserSession}.
	 * 
	 * @return current {@link UserSession}.
	 */
	static public UserSession getUserSession() {
		return userSession;
	}

	/**
	 * Returns the database schema being used by the application.
	 */
	static public String getDatabaseSchema() {
		return databaseSchema;
	}

	/**
	 * Sets the database schema being used by the application.
	 */
	static public void setDatabaseSchema(String dbSchema) {
		databaseSchema = dbSchema;
	}

    /**
     * Returns the stored procedure version used by the application.
     */
    static public String getStoredProcedureVersion() throws SAFRException {
        return DAOFactoryHolder.getDAOFactory().getStoredProcedureDAO().getVersion();
    }
	
	/**
	 * Get model counting utility
	 * @return
	 */
    public static SAFRModelCount getModelCount() {
        if (modelCount == null) {
            modelCount = new SAFRModelCount();
        }
        return modelCount;
    }

    /**
     * Get timing utility
     * @return
     */
    public static TimingMap getTimingMap() {
        if (timingMap == null) {
            timingMap = new TimingMap();
        }
        return timingMap;
    }
	
	/**
	 * Connect to the SAFR database using the properties defined in the
	 * specified file.
	 * 
	 * @param file
	 * @throws DAOException
	 */
	public static void initDBConnection(InputStream file)
			throws DAOException {
		DAOFactoryHolder.initWithPropertiesFile(file);
	}
	
}
