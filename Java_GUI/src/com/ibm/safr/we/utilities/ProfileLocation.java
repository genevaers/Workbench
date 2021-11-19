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
    private String roamProfile;  // user roaming profile area
    private String allProfile;      // all users settings area  
    
    private static ProfileLocation profileLocation = null;
    
    private ProfileLocation() {
        
        // result variables
        // initialise java property and env vars
        String platform = System.getProperty("os.name");
        Map<String, String> env = System.getenv();
        String userArea = env.get("USERPROFILE");
        String allHome = env.get("ALLUSERSPROFILE");
        String osgiCArea = System.getProperty("osgi.configuration.area");
        String roamdata = env.get("APPDATA");        
        if (osgiCArea == null || osgiCArea.contains("Application data") || osgiCArea.contains("Local")) {
            roamdata = env.get("APPDATA");
        }
        else {
            if (osgiCArea.contains("file:/")) {
                osgiCArea = osgiCArea.substring(6);
            }
            roamdata = osgiCArea;
        }
            
        String safrpart = "\\SAFR\\Workbench Eclipse\\";
        
        // if using older Documents and Settings for settings
        if (platform.contains("Windows XP") ||
            platform.contains("Windows Server 2003"))  {

            String localXp = "\\Local Settings\\Application data" + safrpart;
            String roamXp = "\\Application data" + safrpart;
            
            roamProfile = roamdata + safrpart;                
            localProfile = userArea + localXp;                
            allProfile = allHome + roamXp;
        }
        // else using modern Users folder for settings
        else
        {
            String local7 = "\\AppData\\Local" + safrpart;
            
            roamProfile = roamdata + safrpart;                            
            localProfile = userArea + local7;                
            allProfile = allHome + safrpart;
        }   
        
        // create these paths
        File allPrefPath = new File(allProfile);
        allPrefPath.mkdirs();
        File roamPrefPath = new File(roamProfile);
        roamPrefPath.mkdirs();
        File localPrefPath = new File(localProfile);
        localPrefPath.mkdirs();        
    }
    
    /**
     * @return String - the Windows local user profile
     */
    public String getLocalProfile() {
        return localProfile;
    }
  
    public Path getGenevaPath() {
    	return genevaPath;
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
