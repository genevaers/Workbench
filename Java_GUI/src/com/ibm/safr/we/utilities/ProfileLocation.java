package com.ibm.safr.we.utilities;

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

    
    private static ProfileLocation profileLocation = null;
	private Path genevaPath;
    
    private ProfileLocation() {
        
        // result variables
        // initialise java property and env vars
        String platform = System.getProperty("os.name");
        Map<String, String> env = System.getenv();
        String home = env.get("HOME");
        String homepath = env.get("HOMEPATH");
        String safrpart = ".genevaers";
        Path whereToHoldStuff = null;
        if (homepath != null) {
        	whereToHoldStuff = Paths.get(homepath);
        }
        if (home != null) {
        	whereToHoldStuff = Paths.get(home);        	
        }
        // create these paths
        if(whereToHoldStuff != null) {
        	genevaPath = whereToHoldStuff.resolve(safrpart);
        	genevaPath.toFile().mkdirs();
        }
    }
    
    /**
     * @return String - the Windows local user profile
     */
    public String getLocalProfile() {
        return genevaPath.toString();
    }

    /**
     * @return String - the Windows roaming user profile
     */    
    public String getRoamProfile() {
        return genevaPath.toString();
    }

    /**
     * @return String - the Windows all user profile
     */        
    public String getAllProfile() {
        return genevaPath.toString();
    }

    /**
     * @return ProfileLocation - access to the ProfileLocation singleton
     */        
    public static ProfileLocation getProfileLocation() {
        if (profileLocation == null) {
            profileLocation = new ProfileLocation();
        }
        return profileLocation;
    }
}
