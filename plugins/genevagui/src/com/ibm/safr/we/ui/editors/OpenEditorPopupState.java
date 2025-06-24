package com.ibm.safr.we.ui.editors;

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


import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

public class OpenEditorPopupState extends AbstractSourceProvider {

    public final static String LOGICTEXTVIEW = "com.ibm.safr.we.logictextview.enabled";    
    public final static String PHYSICALFILE = "com.ibm.safr.we.physicalfile.enabled";
    public final static String LOGICALFILE = "com.ibm.safr.we.logicalfile.enabled";
    public final static String LOGICALRECORD = "com.ibm.safr.we.logicalrecord.enabled";
    public final static String LOGICALRECORDV = "com.ibm.safr.we.logicalrecord.visible";
    public final static String DEPCHECK = "com.ibm.safr.we.depcheck.enabled";
    public final static String FINDREP = "com.ibm.safr.we.findrep.enabled";
    public final static String ACTLU = "com.ibm.safr.we.actlu.enabled";
    public final static String ACTVIEW = "com.ibm.safr.we.actview.enabled";
    public final static String MIGRATE = "com.ibm.safr.we.migrate.enabled";
    public final static String GRPMEM = "com.ibm.safr.we.grpmem.enabled";
    public final static String USRMEM = "com.ibm.safr.we.usrmem.enabled";
    public final static String GRPPERM = "com.ibm.safr.we.grpperm.enabled";
    public final static String ENVPERM = "com.ibm.safr.we.envperm.enabled";
    public final static String VIEWFOLDER = "com.ibm.safr.we.viewfolder.enabled";
    
    public final static String ENABLED = "ENABLED";
    public final static String DISABLED = "DISABLED";
    public final static String VISIBLE = "VISIBLE";
    public final static String INVISIBLE = "INVISIBLE";    
    
    public void dispose() {
    }

    public Map<String, String> getCurrentState() {
        return new HashMap<String, String>();
    }

    public String[] getProvidedSourceNames() {
        return new String[] { 
            LOGICTEXTVIEW, PHYSICALFILE, LOGICALFILE, LOGICALRECORD, LOGICALRECORDV,
            DEPCHECK, FINDREP, ACTLU, ACTVIEW, MIGRATE, GRPMEM, GRPPERM, ENVPERM, VIEWFOLDER, USRMEM
        };    
    }

    public void setLogicTextView(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, LOGICTEXTVIEW, value);        
    }

    public void setPhysicalFile(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, PHYSICALFILE, value);        
    }

    public void setLogicalFile(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, LOGICALFILE, value);        
    }
    
    public void setLogicalRecord(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, LOGICALRECORD, value);        
    }

    public void setLogicalRecordVisible(boolean enabled) {
        String value = enabled ? VISIBLE : INVISIBLE;        
        fireSourceChanged(ISources.WORKBENCH, LOGICALRECORDV, value);        
    }

    public void setDepCheck(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, DEPCHECK, value);        
    }

    public void setFindRep(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, FINDREP, value);        
    }

    public void setActLU(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, ACTLU, value);        
    }

    public void setActView(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, ACTVIEW, value);        
    }

    public void setMigrate(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, MIGRATE, value);        
    }

    public void setGrpMem(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, GRPMEM, value);        
    }
    
    public void setUsrMem(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, USRMEM, value);        
    }

    public void setGrpPerm(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, GRPPERM, value);        
    }

    public void setEnvPerm(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, ENVPERM, value);        
    }

    public void setViewFolder(boolean enabled) {
        String value = enabled ? ENABLED : DISABLED;
        fireSourceChanged(ISources.WORKBENCH, VIEWFOLDER, value);        
    }
    
}
