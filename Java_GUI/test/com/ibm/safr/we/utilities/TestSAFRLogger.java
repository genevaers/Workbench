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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.preferences.SAFRPreferences;

public class TestSAFRLogger extends TestCase {

    @Override
    protected void tearDown() throws Exception {
        SAFRLogger.teardownLogger();
    }

    public void testSetup() throws Exception {
        
        // ensure that no log path is set
        SAFRPreferences.getSAFRPreferences().remove(UserPreferencesNodes.LOG_FILE_PATH); 
 
        // setup logger
        SAFRLogger.setupLogger();
        
        // check that path is what we expect
        String expectedPath = ProfileLocation.getProfileLocation().getLocalProfile() +"log\\";
        assertEquals(expectedPath, SAFRLogger.getLogPath());
        
        final Logger logger = Logger.getLogger("com.ibm.safr.we.utilities.TestSAFRLogger");
        logger.info("Test logger");
        
        // check contents of log 
        File currentLog = new File(SAFRLogger.getLogPath() + SAFRLogger.getTraceLogFileName());
        BufferedReader reader = new BufferedReader(new FileReader(currentLog));
        String line = "";
        line = reader.readLine();
        assertTrue(line.contains("com.ibm.safr.we.utilities.TestSAFRLogger"));
        line = reader.readLine();
        assertTrue(line.contains("INFO: Test logger"));
        reader.close();
    }
    
    public void testChange() throws Exception {
        
        // ensure that no log path is set
        SAFRPreferences.getSAFRPreferences().remove(UserPreferencesNodes.LOG_FILE_PATH); 
 
        // setup logger
        SAFRLogger.setupLogger();
        
        String newPath = "C:\\temp\\";
        SAFRLogger.changeLogPath(newPath);
        
        assertEquals(SAFRLogger.getLogPath(), newPath);
        
        final Logger logger = Logger.getLogger("com.ibm.safr.we.utilities.TestSAFRLogger");
        logger.info("Test logger change");
        
        File currentLog = new File(SAFRLogger.getLogPath() + SAFRLogger.getCurrentLogFileName());
        BufferedReader reader = new BufferedReader(new FileReader(currentLog));
        String line = "";
        line = reader.readLine();
        assertTrue(line.contains("Changed log path to C:\\temp\\"));
        reader.close();        
    }
    
    public void testExistingChange() throws Exception {
        
        // ensure that no log path is set
        SAFRPreferences.getSAFRPreferences().remove(UserPreferencesNodes.LOG_FILE_PATH); 
 
        // setup logger
        SAFRLogger.setupLogger();
        final Logger logger = Logger.getLogger("com.ibm.safr.we.utilities.TestSAFRLogger");

        logger.info("Test Logging");
        
        String newPath = "C:\\temp\\";
        SAFRLogger.changeLogPath(newPath);
        
        assertEquals(SAFRLogger.getLogPath(), newPath);
        
        logger.info("Test logger change");
        
        File currentLog = new File(SAFRLogger.getLogPath() + SAFRLogger.getCurrentLogFileName());
        BufferedReader reader = new BufferedReader(new FileReader(currentLog));
        String line = "";
        line = reader.readLine();
        assertTrue(line.contains("Changed log path to C:\\temp\\"));
        reader.close();        
    }
    
    
}
