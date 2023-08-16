package com.ibm.safr.we.preferences;

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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.UserPreferencesNodes;

/**
 *  Utility Class to encrypt the DB password inside a SAFRWE.prefs file.  
 *
 */
public class EncryptPrefs {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        // check arguments
        if (args.length < 1) {
            System.out.println("Usage java com.ibm.safr.we.utility.EncryptPrefs <SAFRWE prefs file>");
            System.exit(1);
        }
        
        // check filename
        File filename = new File(args[0]);
        if (!filename.exists()) {
            System.out.println("Error: Cannot open file " +filename);
            System.exit(2);
        }
        
        // read the preferences file
        Properties props = new Properties();
        try {
            FileReader reader = new FileReader(filename);
            props.load(reader);
            reader.close();
            
        } catch (FileNotFoundException e) {
            System.out.println("Error: Cannot open preferences file " +filename);
            e.printStackTrace();
            System.exit(3);
        } catch (IOException e) {
            System.out.println("Error: Invalid format of preferences file " +filename);
            e.printStackTrace();
            System.exit(4);
        }
        
        // encrypt password
        String key = UserPreferencesNodes.SAVED_CONNECTION + "/" + UserPreferencesNodes.PD;
        String pass = (String)props.get(key);
        if (pass == null) {
            System.out.println("Error: No DB password setting in the file");
            System.exit(5);            
        }
        String encpass = SAFRUtilities.encrypt(pass);
        props.put(key, encpass);
        
        // store preferences
        try {
            FileWriter writer = new FileWriter(filename);
            props.store(writer, null);
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: Cannot open preferences file " +filename + " for writing.");
            e.printStackTrace();
            System.exit(6);            
        } catch (IOException e) {
            System.out.println("Error: Cannot write preferences file " +filename);
            e.printStackTrace();
            System.exit(7);            
        }
        System.out.println("Successfully encrypted password in file " + filename);        
    }
}
