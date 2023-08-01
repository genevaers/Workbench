package com.ibm.safr.we.ui.editors.pf;

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


import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class PhysicalFileMediator {

    private PhysicalFileEditor physicalFileEditor;
    private PhysicalFileGeneralEditor physicalFileGeneralEditor;
    private PhysicalFileDatasetEditor physicalFileDatasetEditor;
    private PhysicalFileDatabaseEditor physicalFileDatabaseEditor;

    public PhysicalFileMediator() {
        super();
    }
    
    public PhysicalFileEditor getPhysicalFileEditor() {
        return physicalFileEditor;
    }

    public void setPhysicalFileEditor(PhysicalFileEditor physicalFileEditor) {
        this.physicalFileEditor = physicalFileEditor;
    }

    public PhysicalFileGeneralEditor getPhysicalFileGeneralEditor() {
        return physicalFileGeneralEditor;
    }

    public void setPhysicalFileGeneralEditor(
        PhysicalFileGeneralEditor physicalFileGeneralEditor) {
        this.physicalFileGeneralEditor = physicalFileGeneralEditor;
    }

    public PhysicalFileDatasetEditor getPhysicalFileFileEditor() {
        return physicalFileDatasetEditor;
    }

    public void setPhysicalFileDatasetEditor(
        PhysicalFileDatasetEditor physicalDatasetFileEditor) {
        this.physicalFileDatasetEditor = physicalDatasetFileEditor;
    }

    public PhysicalFileDatabaseEditor getPhysicalFileDatabaseEditor() {
        return physicalFileDatabaseEditor;
    }

    public void setPhysicalFileDatabaseEditor(
        PhysicalFileDatabaseEditor physicalFileDatabaseEditor) {
        this.physicalFileDatabaseEditor = physicalFileDatabaseEditor;
    }

    // calls on the top level editor
    public SAFRGUIToolkit getSAFRToolkit() {
        return physicalFileEditor.safrToolkit;
    }

    public FormToolkit getFormToolkit() {
        return physicalFileEditor.toolkit;
    }

    public void setDirty(boolean dirty) {
        physicalFileEditor.setDirty(dirty);        
    }

    public IWorkbenchPartSite getSite() {
        return physicalFileEditor.getSite();
    }

    public void enableDatasetTab() {
        physicalFileEditor.enableDatasetTab();
    }

    public void enableDatabaseTab() {
        physicalFileEditor.enableDatabaseTab();
    }
    
    public PhysicalFileEditorInput getPhysicalFileInput() {
        return physicalFileEditor.getPhysicalFileInput();
    }
    
    public void doRefreshControls() throws SAFRException {
        physicalFileGeneralEditor.doRefreshControls();
    }

    // calls on the general tab
    
    public ComponentAssociation getCurrentLF() {
        return physicalFileGeneralEditor.getCurrentLF();
    }

    public void setNameFocus() {
        physicalFileGeneralEditor.setNameFocus();
    }

    public Control getControlFromProperty(Object property) {
        return physicalFileGeneralEditor.getControlFromProperty(property);        
    }
    
    public void searchComponent(MetadataSearchCriteria searchCriteria, String searchText) {
        physicalFileGeneralEditor.searchComponent(searchCriteria,searchText);
        
    }    

    public String getComponentNameForSaveAs() {
        return physicalFileGeneralEditor.getComponentNameForSaveAs();
    }

    
    public ComponentType getComponentType() {
        return physicalFileGeneralEditor.getComponentType();
    }

    
    // calls on the dataset tab
    
    public void doRefreshDatasetTab() {
        physicalFileDatasetEditor.doRefreshControls();
    }

    public void enableDatasetName() {
        physicalFileDatasetEditor.enableDatasetName();
    }    
    
    public void disableDatasetName() {
        physicalFileDatasetEditor.disableDatasetName();
    }    

    public void enableRecordSettings() {
        physicalFileDatasetEditor.enableRecordSettings();
    }

    public void disableRecordSettings() {
        physicalFileDatasetEditor.disableRecordSettings();
    }

    // calls on the database tab
    
    public void doRefreshDatabaseTab() {
        physicalFileDatabaseEditor.doRefreshControls();
    }

    public void enableSQL() {
        physicalFileDatabaseEditor.enableSQL();
    }


}
