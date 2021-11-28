package com.ibm.safr.we.preferences;

import java.io.File;

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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.core.runtime.Platform;

import com.ibm.safr.we.constants.SAFREnvProp;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.utilities.ProfileLocation;

/**
 * This class is used to get/set SAFR preferences. eg. login dialog shows last user login
 * id. System wide preferences will be stored in a file in 
 * the Windows All Users profile. User preferences will be stored in the 
 * Windows user roaming profile. 
 * Existing non static methods from an older version of this file
 * have been kept the same. This was done to limit changes to calling code.
 */
public class SAFRPreferences {

    static transient final Logger logger = Logger
    .getLogger("com.ibm.safr.we.SAFRPreferences");
    
    public static final String SAFRWE_PREFS = "SAFRWE.prefs";
       
    private static Preferences preferences = null;

    public static Preferences getSAFRPreferences() {
        
        if (preferences == null) {
        	String prefsLocation = ProfileLocation.getProfileLocation().getLocalProfile() + "/prefs/";
        	makePrefsDirectory(prefsLocation);
        	String allPref = prefsLocation + SAFRPreferences.SAFRWE_PREFS;
        	String userPref;        	
        	if (System.getProperties().get(SAFREnvProp.DEV) == null) {
                userPref = prefsLocation + SAFRPreferences.SAFRWE_PREFS;    
        	}
        	else {
        		String location = Platform.getInstanceLocation().getURL().getPath();
        		userPref = location + SAFRPreferences.SAFRWE_PREFS;        		
        	}
            System.setProperty(OverridePreferencesFactory.BASE_PROP, allPref); 
            System.setProperty(OverridePreferencesFactory.OVER_PROP, userPref);
            System.setProperty("java.util.prefs.PreferencesFactory", OverridePreferencesFactory.class.getName());
            
            preferences = Preferences.userRoot();
            try {
                preferences.sync();
            } catch (BackingStoreException e) {
                // log and continue with preference load errors
                // preferences will reset empty on load failure
                logger.log(Level.WARNING, "Failed to load preferences", e);
            }
        }
        return preferences;
    }
    
    private static void makePrefsDirectory(String userPref) {
        File upf = new File(userPref);
        upf.mkdirs();
	}

	public static Preferences getConnectionPreferences(String connectionName) {
    	Preferences connPrefs = getConnectionPreferencesMap().get(connectionName);
    	return connPrefs;
    }
    
	private static Preferences getConnectionPreferences() {
		Preferences connPrefs = null;
		try {
			if (getSAFRPreferences().nodeExists(UserPreferencesNodes.SAVED_CONNECTION)) {
				connPrefs = getSAFRPreferences().node(UserPreferencesNodes.SAVED_CONNECTION);
			}
		} catch (BackingStoreException e) {
			logger.log(Level.WARNING, "Failed to load preferences", e);
		}
		return connPrefs;
	}

	private static Map<String, Preferences> getConnectionPreferencesMap() {
		Map<String, Preferences> connMap = new LinkedHashMap<String, Preferences>();
		Preferences connPrefsNode = getConnectionPreferences();
		if (connPrefsNode != null) {
			List<String> connNames;
			try {
				connNames = Arrays.asList(connPrefsNode.childrenNames());
				for (String connName : connNames) {
					Preferences conn = connPrefsNode.node(connName);
					connMap.put(connName, conn);
				}
			} catch (BackingStoreException e) {
				logger.log(Level.WARNING, "Failed to load preferences", e);
			}
		}
		return connMap;
	}

	/**
	 * Returns the connection properties to be used to connect to the database.
	 * These are the currently chosen (or last used) connection Preferences. The
	 * method will return the preferences specified by the last-connection
	 * property. If this is not specified, it will return the first connection.
	 * 
	 * @return the Preferences node for the default connection
	 */
	public static Preferences getDefaultConnectionPreferences() {
		String lastConn = getSAFRPreferences().get(UserPreferencesNodes.LAST_CONNECTION, "");
		Preferences defaultConnPrefs = null;
		if (!lastConn.equals("")) {
			defaultConnPrefs = SAFRPreferences.getConnectionPreferences(lastConn);
		}
		if (defaultConnPrefs == null) {
			// if last conn is not specified use the first connection
			Iterator<Preferences> i = getConnectionPreferencesMap().values().iterator();
			if (i.hasNext()) {
				defaultConnPrefs = (Preferences) i.next();
			}
		}
		return defaultConnPrefs;
	}
	
	public static String getDefaultConnectionName() {
		String defConnName = null;
		Preferences defConn = getDefaultConnectionPreferences();
		for (Entry<String, Preferences> entry : getConnectionPreferencesMap().entrySet()) {
			if (entry.getValue().equals(defConn)) {
				defConnName = entry.getKey();
			}
		}
		return defConnName;
	}
	
	public static List<String> getConnectionNames() {
		List<String> list = new ArrayList<String>();
		list.addAll(getConnectionPreferencesMap().keySet());
		return list;
	}
	
    /**
     * Sets last user id used in login to the preferences storage.
     * 
     * @param LastUser
     *            Login id of the last user to save in preference storage .
     */
    public void setLastUser(String LastUser) {

        getSAFRPreferences().put(UserPreferencesNodes.LAST_USER, LastUser);
        try {
            getSAFRPreferences().flush();
        } catch (BackingStoreException e) {
            throw new SAFRFatalException(e);
        }
    }

    /**
     * @return Login id of the last user saved in preference storage .
     * 
     */
    public String getLastUser() {
        return getSAFRPreferences().get(UserPreferencesNodes.LAST_USER, "");
    }

    /**
     * Sets flag to ignore import warnings
     * 
     * @param ignore
     *            Boolean
     */
    public void setIgnoreImportWarnings(Boolean ignore) {

        String ignoreStr = "N";
        if (ignore != null && ignore) {
            ignoreStr = "Y";
        }
        getSAFRPreferences().put(UserPreferencesNodes.IGNORE_IMPORT_WARNINGS, ignoreStr);
        try {
            getSAFRPreferences().flush();
        } catch (BackingStoreException e) {
            throw new SAFRFatalException(e);
        }
    }

    /**
     * Sets flag to ignore migrate warnings
     * 
     * @param ignore
     *            Boolean
     */
    public void setIgnoreMigrateWarnings(Boolean ignore) {

        String ignoreStr = "N";
        if (ignore != null && ignore) {
            ignoreStr = "Y";
        }
        getSAFRPreferences().put(UserPreferencesNodes.IGNORE_MIGRATE_WARNINGS, ignoreStr);
        try {
            getSAFRPreferences().flush();
        } catch (BackingStoreException e) {
            throw new SAFRFatalException(e);
        }
    }

    /**
     * @return whether to ignore import warnings
     */
    public Boolean isIgnoreImportWarnings() {

        String ignoreStr = getSAFRPreferences().get(
                UserPreferencesNodes.IGNORE_IMPORT_WARNINGS, "");
        if (ignoreStr != null && ignoreStr.equalsIgnoreCase("Y")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return whether to ignore migrate warnings
     */
    public Boolean isIgnoreMigrateWarnings() {

        String ignoreStr = getSAFRPreferences().get(
                UserPreferencesNodes.IGNORE_MIGRATE_WARNINGS, "");
        if (ignoreStr != null && ignoreStr.equalsIgnoreCase("Y")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Is node in system preferences
     */
    public static boolean isNodeSystem(String path) {
       return  ((OverridePreferences)getSAFRPreferences()).isNodeinBase(path);
    }

    /**
     * Is node in user preferences
     */
    public static boolean isNodeUser(String path) {
        return  ((OverridePreferences)getSAFRPreferences()).isNodeinOver(path);
    }
}
