package com.ibm.safr.we.data;

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


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.security.UserSession;

public class TestDataLayerHelper {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.data.TestDataLayerHelper");

	static String PROP_FILE = "connection.properties";
	static String POSTGRES_FILE = "postgres.properties";

	static String [] NEXT_KEY_TABLES = {
	    "c_environtbl",
	    "e_servertbl",
	    "c_secgroups",
	    "c_viewfolders",
	    "e_partitiontbl",
	    "e_filetbl",
	    "x_filepartitiontbl",
	    "c_controlrec",
	    "c_usertbl",
	    "e_fldtbl",
	    "e_joinsourcekeytbl",
	    "e_jointbl",
	    "e_lrindextbl",
	    "e_lrtbl",
	    "e_programtbl",
	    "e_vdpbatch",
	    "e_vdpbatchviews",
	    "e_viewheaderfooter",
	    "e_viewtbl",
	    "xov_lrfldattr",
	    "xov_viewlrfldattr",
	    "x_jointargetlrtbl",
	    "x_lrfiletbl",
	    "x_lrfldtbl",
	    "x_lrindexfldtbl",
	    "x_secgroupsexit",
	    "x_secgroupsfiles",
	    "x_secgroupslr",
	    "x_secgroupsparts",
        "x_secgroupsjoin",
	    "x_secgroupsview",
	    "x_secgroupsusers",
	    "x_secgrpsenvts",
	    "x_secgrpsvfldrs",
	    "x_viewlogicdepend",
	    "x_viewlrfld_lrfld",
	    "x_viewsortkeytbl",
	    "x_viewsrclrfiletbl"	    
	};
	
	static boolean conFailed = false;
	static boolean postgres = false;

	Map<String, Integer> nextKeySnapshot = new HashMap<String, Integer>();
	static public void  setPostgres(boolean p) {postgres=p;}
	
    public void initDataLayer() {
        if (conFailed) {
            logger.log(Level.SEVERE,
                    "Connection has previously failed so won't retry");
            throw new SAFRFatalException(
                    "Connection has previously failed so won't retry");
        }
        try {
        	InputStream file;
        	if(postgres) {
        		file = DAOFactoryHolder.class.getResource(POSTGRES_FILE).openStream();        		
        	} else {
        		file = DAOFactoryHolder.class.getResource(PROP_FILE).openStream();
        	}
            DAOFactoryHolder.initWithPropertiesFile(file);
            UserSessionParameters userSessionParams;
        	if(postgres) {
        		userSessionParams= ((PGDAOFactory) DAOFactoryHolder
                        .getDAOFactory()).getSAFRLogin();
        	}
            User user = SAFRApplication.getSAFRFactory().getUser(
                    userSessionParams.getUserId());
            Environment env = SAFRApplication.getSAFRFactory().getEnvironment(
                    userSessionParams.getEnvId());
            Group group;
            if (userSessionParams.getGroupId().equals(0)) {
                group = null;
            } else {
                group = SAFRApplication.getSAFRFactory().getGroup(
                        userSessionParams.getGroupId());
            }
            UserSession uSession = new UserSession(user, env, group);
            SAFRApplication.setUserSession(uSession);
            // getAllCodeSets will get all the code sets in the memory.
            SAFRApplication.getSAFRFactory().getAllCodeSets();
            file.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "", e);
            conFailed = true;
        }
    }
    
	public void initDataLayer(Integer envId) {
		if (conFailed) {
			logger.log(Level.SEVERE,
					"Connection has previously failed so won't retry");
			throw new SAFRFatalException(
					"Connection has previously failed so won't retry");
		}
		try {
        	InputStream file;
        	if(postgres) {
        		file = DAOFactoryHolder.class.getResource(POSTGRES_FILE) .openStream();        		
        	} else {
        		file = DAOFactoryHolder.class.getResource(PROP_FILE)
                    .openStream();
        	}
			DAOFactoryHolder.initWithPropertiesFile(file);
			UserSessionParameters userSessionParams;
        	if(postgres) {
        		userSessionParams = ((PGDAOFactory) DAOFactoryHolder.getDAOFactory()).getSAFRLogin();
        	}
			userSessionParams.setEnvId(envId);
			User user = SAFRApplication.getSAFRFactory().getUser(userSessionParams.getUserId());
			Environment env = SAFRApplication.getSAFRFactory().getEnvironment(envId);
			user.setDefaultEnvironment(env);			
			Group group;
			if (userSessionParams.getGroupId().equals(0)) {
				group = null;
			} else {
				group = SAFRApplication.getSAFRFactory().getGroup(
						userSessionParams.getGroupId());
			}
			UserSession uSession = new UserSession(user, env, group);
			SAFRApplication.setUserSession(uSession);
			// getAllCodeSets will get all the code sets in the memory.
			SAFRApplication.getSAFRFactory().getAllCodeSets();
			file.close();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
			conFailed = true;
		}
	}

	public void setUser(String userId) throws DAOException, SAFRException {
		
		Environment oldEnv = SAFRApplication.getUserSession().getEnvironment();
		User user = SAFRApplication.getSAFRFactory().getUser(userId);
		Group group;
		
		if (user.getDefaultGroup() == null) {
			group = null;
		} else {
			group = user.getDefaultGroup();
		}
		UserSession uSession = new UserSession(user, oldEnv, group);
		SAFRApplication.setUserSession(uSession);
	}
	
    public void setEnv(Integer envId) throws DAOException, SAFRException {
        
        UserSession oldSession = SAFRApplication.getUserSession();
        User user = oldSession.getUser();
        Group group = oldSession.getGroup();
        Environment env = SAFRApplication.getSAFRFactory().getEnvironment(envId);
        UserSession uSession = new UserSession(user, env, group);
        SAFRApplication.setUserSession(uSession);
    }
	
	public void closeDataLayer() {
		try {
        	if(postgres) {
    			((PGDAOFactory) DAOFactoryHolder.getDAOFactory()).disconnect();
        	}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	public ConnectionParameters getParams() {
		try {
			InputStream file = DAOFactoryHolder.class.getResource(PROP_FILE)
					.openStream();

			BufferedInputStream stream = new BufferedInputStream(file);
			Properties prop = new Properties();
			prop.load(stream);
			ConnectionParameters params = new ConnectionParameters();
			params.setType(DBType.valueOf((String) prop.get("TYPE")));
			params.setUrl((String) prop.get("URL"));
			params.setSchema((String) prop.get("SCHEMA"));
			params.setUserName((String) prop.get("USER"));
			params.setPassWord((String) prop.get("PASS"));
			params.setPort((String) prop.get("PORT"));
			params.setDatabase((String) prop.get("DATABASE"));
			params.setServer((String) prop.get("SERVER"));

			return params;

		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
			return null;
		}

	}

}
