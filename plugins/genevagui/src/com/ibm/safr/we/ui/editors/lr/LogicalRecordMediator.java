package com.ibm.safr.we.ui.editors.lr;

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


import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class LogicalRecordMediator {

    private LogicalRecordEditor logicalRecordEditor;
    private LogicalRecordFieldEditor logicalRecordFieldEditor;
    private LogicalRecordGeneralEditor logicalRecordGeneralEditor;
    private LogicalRecordLogFileEditor logicalRecordLogFileEditor;
    
    protected LogicalRecordMediator() {
    }

    protected void setLogicalRecordEditor(LogicalRecordEditor logicalRecordEditor) {
        this.logicalRecordEditor = logicalRecordEditor;
    }

    protected void setLogicalRecordFieldEditor(LogicalRecordFieldEditor logicalRecordFieldEditor) {
        this.logicalRecordFieldEditor = logicalRecordFieldEditor;
    }
    
    public void setLogicalRecordGeneralEditor(LogicalRecordGeneralEditor logicalRecordGeneralEditor) {
        this.logicalRecordGeneralEditor = logicalRecordGeneralEditor;
    }

    public void setLogicalRecordLogFileEditor(LogicalRecordLogFileEditor logicalRecordLogFileEditor) {
        this.logicalRecordLogFileEditor = logicalRecordLogFileEditor;
    }

    
    // calls on the logical record editor
    protected SAFRGUIToolkit getGUIToolKit() {
        return logicalRecordEditor.getSafrGuiToolkit();
    }

    protected FormToolkit getFormToolKit() {
        return logicalRecordEditor.getToolkit();
    }

    protected EditRights getEditRights() {
        return ((LogicalRecordEditorInput)logicalRecordEditor.getEditorInput()).getEditRights();
    }

    protected void setDirty(boolean b) {
        logicalRecordEditor.setDirty(b);        
    }

    protected IWorkbenchPartSite getSite() {
        return logicalRecordEditor.getSite();
    }

    protected void refreshToolbar() {
        logicalRecordFieldEditor.refreshToolbar();        
    }

    protected void setTabLRFields() {
        logicalRecordEditor.setTabLRFields();        
    }
    
    public boolean isSaveAs() {
        return logicalRecordEditor.isSaveAs();
    }
    
    public LogicalRecordEditorInput getEditorInput() {
        return (LogicalRecordEditorInput)logicalRecordEditor.getEditorInput();
    }
    
    protected Control getControlFromProperty(Object property) {        
        if (property == LogicalRecord.Property.LR_FIELD) {
            return logicalRecordFieldEditor.getControlFromProperty();
        } else {
            return logicalRecordGeneralEditor.getControlFromProperty(property);
        }        
    }

    // calls on the logical record general editor    
    public Control getCompositeGeneral() {
        return logicalRecordGeneralEditor.getCompositeGeneral();
    }

    public void setNameFocus() {
        logicalRecordGeneralEditor.setNameFocus();
    }
    
    public void refreshControlGeneral() {
        logicalRecordGeneralEditor.refreshControlGeneral();
    }

    public void refreshModelGeneral() {
        logicalRecordGeneralEditor.refreshModel();
    }    
    
    public String getComponentNameForSaveAs() {
        return logicalRecordGeneralEditor.getName();
    }    
    
    // calls on the logical record field editor    
    protected Control getCompositeLRFields() {
        return logicalRecordFieldEditor.getCompositeLRFields();
    }

    protected void setLRFieldsFocus() {
        logicalRecordFieldEditor.setLRFieldsFocus();
    }

    protected void refreshLRFields() {
        logicalRecordFieldEditor.refreshLRFields();        
    }

    protected void addRow() {
        logicalRecordFieldEditor.addRow(true);
    }

    protected void editCalculateAllRows() {
        logicalRecordFieldEditor.editCalculateAllRows();
    }

    protected void editCalculateFromHighlightedRows() {
        logicalRecordFieldEditor.editCalculateFromHighlightedRows();
    }

    protected void editCalculateOnlyHighlightedRows() {
        logicalRecordFieldEditor.editCalculateOnlyHighlightedRows();
    }

    public void editCalculateWithinHighlightedRow() {
        logicalRecordFieldEditor.editCalculateWithinHighlightedRow();
    }
    
    public void editCalculateRedefines() {
        logicalRecordFieldEditor.editCalculateRedefines();      
    }
    
    protected void editCopyRow() {
        logicalRecordFieldEditor.editCopyRow();
    }

    protected void editFindField() {
        logicalRecordFieldEditor.editFindField();
    }

    protected void editInsertFieldBefore() {
        logicalRecordFieldEditor.editInsertFieldBefore();
    }

    protected void editInsertFieldAfter() {
        logicalRecordFieldEditor.editInsertFieldAfter();
    }
    
    protected void editPasteRowAboveSelected() {
        logicalRecordFieldEditor.editPasteRowAboveSelected();
    }

    protected void editPasteRowBelowSelected() {
        logicalRecordFieldEditor.editPasteRowBelowSelected();
    }

    protected void editRemoveRow() {
        logicalRecordFieldEditor.editRemoveRow();
    }

    protected void moveUpRow() {
        logicalRecordFieldEditor.moveUpRow();
    }

    protected void moveDownRow() {
        logicalRecordFieldEditor.moveDownRow();
    }

    protected void selectRowAtField(Integer id) {
        logicalRecordFieldEditor.selectRowAtField(id);
    }

    public void expandAll() {
        logicalRecordFieldEditor.expandAll();        
    }

    public void collapseAll() {
        logicalRecordFieldEditor.collapseAll();        
    }

    public LRField getCurrentLRField() {
        return logicalRecordFieldEditor.getCurrentLRField();
    }

    // calls on the logical record log file editor        
    public ComponentAssociation getCurrentLF() {
        return logicalRecordLogFileEditor.getCurrentLF();
    }

    public Control getCompositeLogFile() {
        return logicalRecordLogFileEditor.getCompositeLogFile();
    }

    public void refreshLogFiles() {
        logicalRecordLogFileEditor.refreshLogFiles();
    }

    public void searchLFAssociation(MetadataSearchCriteria searchCriteria, String searchText) {
        logicalRecordLogFileEditor.searchLFAssociation(searchCriteria, searchText);
    }

    public ComponentType getComponentType() {
        return logicalRecordLogFileEditor.getComponentType();
    }

}
