package com.ibm.safr.we.ui.editors.batchlookup;

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


import java.util.logging.Logger;

import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.utilities.BatchComponent;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class ActivateLookupPathsMediator {
    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.batchlookup.ActivateLookupPathsMediator");

    private ActivateLookupPathsEditor activateLookupPathsEditor;
    private ActivateLookupPathsError activateLookupPathsError;
    private ActivateLookupPathsLookups activateLookupPathsLookups;
    
    public ActivateLookupPathsMediator() {
        
    }
    
    public ActivateLookupPathsEditor getActivateLookupPathsEditor() {
        return activateLookupPathsEditor;
    }

    public void setActivateLookupPathsEditor(
        ActivateLookupPathsEditor activateLookupPathsEditor) {
        this.activateLookupPathsEditor = activateLookupPathsEditor;
    }

    public ActivateLookupPathsError getActivateLookupPathsError() {
        return activateLookupPathsError;
    }

    public void setActivateLookupPathsError(ActivateLookupPathsError activateLookupPathsError) {
        this.activateLookupPathsError = activateLookupPathsError; 
    }
    
    public ActivateLookupPathsLookups getActivateLookupPathsLookups() {
        return activateLookupPathsLookups;
    }

    public void setActivateLookupPathsLookups(ActivateLookupPathsLookups activateLookupPathsLookups) {
        this.activateLookupPathsLookups = activateLookupPathsLookups;
    }        
    
    // calls on ActivateLookupPathsEditor
    public SAFRGUIToolkit getGUIToolKit() {
        return activateLookupPathsEditor.safrGuiToolkit;
    }

    public Environment getCurrentEnvironment() {
        return activateLookupPathsEditor.getCurrentEnvironment();
    }

    public IWorkbenchPartSite getSite() {
        return activateLookupPathsEditor.getSite();
    }

    public Control getSectionEnvironment() {
        return activateLookupPathsEditor.getSectionEnvironment();
    }

    public EnvironmentQueryBean getCurrentEnvironmentBean() {
        return activateLookupPathsEditor.getCurrentEnvironmentBean();
    }
    
    // calls on ActivateLookupPathsError    
    public void clearErrors() {
        activateLookupPathsError.clearErrors();
    }

    public void showSectionErrors(boolean b) {
        activateLookupPathsError.showSectionErrors(b);
        
    }

    // calls on ActivateLookupPathsLookups    
    public void loadData() {
        activateLookupPathsLookups.loadData();
    }

    public void searchComponent(MetadataSearchCriteria searchCriteria, String searchText) {
        activateLookupPathsLookups.searchComponent(searchCriteria, searchText);
    }

    public ComponentType getComponentType() {
        return activateLookupPathsLookups.getComponentType();
    }

    public Control getSectionLookups() {
        return activateLookupPathsLookups.getSectionLookups();
    }

    public BatchComponent getLookupSelection() {
        return activateLookupPathsLookups.getCurrentSelection();
    }

    
}
