package com.ibm.safr.we.utilities;

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


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Utility class to determine/create the locations of Windows profile folders to
 * store SAFR preferences and output files. This location is correct regardless
 * of the version of Windows. The following locations are calculated
 * 
 *  localProfile - the Windows local user profile
 *  roamProfile - the Windows roaming user profile
 *  allProfile - the Windows all users profile
 */
public class ProfileLocation {

    private String localProfile;    // user local settings area
    private String globalProfile;      // all users settings area  
    private static ProfileLocation profileLocation = null;
	private Path genevaPath;
    
    private ProfileLocation() {
        String os = System.getProperty("os.name");
        if(os.startsWith("Windows")) {
        	makeWindowsPaths(System.getenv());
        } else {
        	makeUnixPaths(System.getenv());
        }
    }
    
    private void makeWindowsPaths(Map<String, String> env) {
        String userArea = env.get("USERPROFILE");
        String allHome = env.get("ALLUSERSPROFILE");
            
        String safrpart = "\\SAFR\\Workbench Eclipse\\";

        String local7 = "\\AppData\\Roaming" + safrpart;
            
        localProfile = userArea + local7;                
        globalProfile = allHome + safrpart;
        // create these paths
        File allPrefPath = new File(globalProfile);
        allPrefPath.mkdirs();
        File localPrefPath = new File(localProfile);
        localPrefPath.mkdirs();        	
        
        makeProfileDirIfDoesNotExist("logs");
        makeProfileDirIfDoesNotExist("prefs");
    }

	private void makeProfileDirIfDoesNotExist(String d) {
		File dir = new File(localProfile + "/" + d );
		if(dir.exists() == false) {
			dir.mkdirs();
		}
	}

	private void makeUnixPaths(Map<String, String> env) {
        String home = env.get("HOME");
        String safrpart = ".genevaers";
        Path whereToHoldStuff = null;
        if (home != null) {
            whereToHoldStuff = Paths.get(home);
        }
        if(whereToHoldStuff != null) {
            genevaPath = whereToHoldStuff.resolve(safrpart);
            genevaPath.toFile().mkdirs();
            localProfile = genevaPath.toString();
        }    		
	}

    /**
     * @return String - the Windows local user profile
     */
    public String getLocalProfile() {
        return localProfile;
    }

    
    public String getGlobalProfile() {
        return globalProfile;
    }


    public static ProfileLocation getProfileLocation() {
        if (profileLocation == null) {
            profileLocation = new ProfileLocation();
        }
        return profileLocation;
    }
}
