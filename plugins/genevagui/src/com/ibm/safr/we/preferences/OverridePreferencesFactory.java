package com.ibm.safr.we.preferences;

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
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;


/**
 * An implementation of the PreferencesFactory interface using the 
 * SAFR OverridePreferences implementation.
 */
public class OverridePreferencesFactory implements PreferencesFactory {
    
    public static final String BASE_PROP = "com.ibm.safr.we.utilities.OverridePreferences.BASE";
    public static final String OVER_PROP = "com.ibm.safr.we.utilities.OverridePreferences.OVER";

    private Preferences preferences;
    
    public Preferences systemRoot() {
        return getPreferences();
    }

    public Preferences userRoot() {
        return getPreferences();
    }
    
    private Preferences getPreferences() {
        if (preferences == null) {            
            preferences = new OverridePreferences( 
                    new File(System.getProperty(BASE_PROP)), 
                    new File(System.getProperty(OVER_PROP)));
        }
        return preferences;        
    }
}
