package com.ibm.safr.we.ui.editors.batchview;

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
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.utilities.BatchComponent;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class ActivateViewsMediator {
    
    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.batchview.ActivateViewsMediator");

    private ActivateViewsEditor activateViewsEditor;
    private ActivateViewsCriteria activateViewsCriteria;
    private ActivateViewsViews activateViewsViews;
    private ActivateViewsError activateViewsErrors;
    
    public ActivateViewsMediator() {
    }

    public ActivateViewsEditor getActivateViewsEditor() {
        return activateViewsEditor;
    }

    public void setActivateViewsEditor(ActivateViewsEditor activateViewsEditor) {
        this.activateViewsEditor = activateViewsEditor;
    }

    public ActivateViewsError getActivateViewsErrors() {
        return activateViewsErrors;
    }

    public void setActivateViewsErrors(ActivateViewsError activateViewsErrors) {
        this.activateViewsErrors = activateViewsErrors;
    }

    public ActivateViewsViews getActivateViewsViews() {
        return activateViewsViews;
    }

    public void setActivateViewsViews(ActivateViewsViews activateViewsViews) {
        this.activateViewsViews = activateViewsViews;
    }

    public ActivateViewsCriteria getActivateViewsCriteria() {
        return activateViewsCriteria;
    }
    
    public void setActivateViewsCriteria(ActivateViewsCriteria activateViewsCriteria) {
        this.activateViewsCriteria = activateViewsCriteria;
    }    
    
    // calls on ActivateViewsEditor
    public SAFRGUIToolkit getGUIToolKit() {
        return activateViewsEditor.safrGuiToolkit;
    }

    public IWorkbenchPartSite getSite() {
        return activateViewsEditor.getSite();
    }

    // call on ActivateViewsCriteria
    public Control getEnvironmentSection() {
        return activateViewsCriteria.getEnvironmentSection();
    }
    
    public Environment getCurrentEnvironment() {
        return activateViewsCriteria.getCurrentEnvironment();
    }

    public ViewFolder getCurrentViewFolder() {
        return activateViewsCriteria.getCurrentViewFolder();
    }           
        
    // calls on ActivateViewsErrors
    public void clearErrors() {
        activateViewsErrors.clearErrors();
    }

    public void showSectionLoadErrors(boolean b) {
        activateViewsErrors.showSectionLoadErrors(b);        
    }

    // calls on ActivateViewsViews
    public void loadData() {
        activateViewsViews.loadData();
    }

    public void refreshViews() {
        activateViewsViews.refreshViews();
    }

    public void setFocusViews() {
        activateViewsViews.setFocusViews();
    }

    public void closeActivationLog() {
        activateViewsViews.closeActivationLog();
    }

    public void showActivationLog() {
        activateViewsViews.showActivationLog();
    }

    public ComponentType getComponentType() {
        return activateViewsViews.getComponentType();
    }

    public void searchComponent(MetadataSearchCriteria searchCriteria, String searchText) {
        activateViewsViews.searchComponent(searchCriteria, searchText);
    }

    public Control getTableSection() {
        return activateViewsViews.getTableSection();
    }

    public BatchComponent getSelectedView() {
        return activateViewsViews.getSelectedView();
    }

    public SAFRViewActivationException getViewActivationException() {
        return activateViewsViews.getViewActivationException();
    }

    public SAFRViewActivationException getViewActivationState(Integer id) {
        return activateViewsViews.getViewActivationState(id);
    }

}
