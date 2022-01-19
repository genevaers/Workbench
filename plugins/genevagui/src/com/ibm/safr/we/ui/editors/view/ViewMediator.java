package com.ibm.safr.we.ui.editors.view;

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


import java.util.List;
import java.util.logging.Logger;

import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSortKey;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.editors.view.ViewColumnEditor.PropertyViewType;
import com.ibm.safr.we.ui.editors.view.ViewFormatReportEditor.HFFocus;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class ViewMediator {

    static final Logger logger = Logger
    .getLogger("com.ibm.safr.we.ui.editors.view.ViewMediator");
    
    private ViewEditor viewEditor = null;
    private ViewColumnEditor viewColumnEditor = null;
    private ViewGeneralEditor viewGeneralEditor = null;
    private ViewExtractEditor viewExtractEditor = null;    
    private ViewFormatEditor viewFormatEditor = null;    
    private ViewFormatReportEditor viewFormatReportEditor = null;
    private ViewFormatDelimitedEditor viewFormatDelimitedEditor = null;    
    
    protected ViewMediator() 
    {
    }

    protected ViewEditor getViewEditor() {
        return viewEditor;
    }

    protected void setViewEditor(ViewEditor viewEditor) {
        this.viewEditor = viewEditor;
    }

    protected ViewColumnEditor getViewColumnEditor() {
        return viewColumnEditor;
    }

    protected void setViewColumnEditor(ViewColumnEditor viewColumnEditor) {
        this.viewColumnEditor = viewColumnEditor;
    }

    protected ViewGeneralEditor getViewGeneralEditor() {
        return viewGeneralEditor;
    }

    protected void setViewGeneralEditor(ViewGeneralEditor viewGeneralEditor) {
        this.viewGeneralEditor = viewGeneralEditor;
    }

    protected ViewExtractEditor getViewExtractEditor() {
        return viewExtractEditor;
    }

    protected void setViewExtractEditor(ViewExtractEditor viewExtractEditor) {
        this.viewExtractEditor = viewExtractEditor;
    }

    protected ViewFormatEditor getViewFormatEditor() {
        return viewFormatEditor;
    }

    protected void setViewFormatEditor(ViewFormatEditor viewFormatEditor) {
        this.viewFormatEditor = viewFormatEditor;
    }
    
    protected ViewFormatReportEditor getViewHeaderFooterEditor() {
        return viewFormatReportEditor;
    }

    protected void setViewFormatReportEditor(ViewFormatReportEditor viewHeaderFooterEditor) {
        this.viewFormatReportEditor = viewHeaderFooterEditor;
    }
    
    public ViewFormatDelimitedEditor getViewFormatDelimitedEditor() {
        return viewFormatDelimitedEditor;
    }

    public void setViewFormatDelimitedEditor(ViewFormatDelimitedEditor viewFormatDelimitedEditor) {
        this.viewFormatDelimitedEditor = viewFormatDelimitedEditor;
    }
    
    // calls on ViewEditor
    // calls on the logical record editor
    protected SAFRGUIToolkit getGUIToolKit() {
        return viewEditor.getGUIToolKit();
    }
    
    protected IWorkbenchPartSite getSite() {
        return viewEditor.getSite();
    }
    
    protected void refreshToolbar() {
        viewEditor.refreshToolbar();
    }
    
    protected ViewEditor getEditor() {
        return viewEditor;
    }
    
    protected boolean isViewPropVisible() {
        return viewEditor.isViewPropVisible();
    }
    
    protected void setModified(Boolean dirty) {
        viewEditor.setModified(dirty);
    }
    
    protected void updateSortKeyViews() {
        viewEditor.updateSortKeyViews();
    }
    
    protected void updateDataSourceViews() {
        viewEditor.updateDataSourceViews();        
    }
    
    protected void formReflow(boolean flushCache) {
        viewEditor.getForm().reflow(flushCache);
    }
    
    protected void refreshRelatedLogicTextEditorsHeaders() throws SAFRException {
        viewEditor.refreshRelatedLogicTextEditorsHeaders();
    }
    
    protected ViewEditorInput getEditorInput() {
        return (ViewEditorInput) viewEditor.getEditorInput();
    }
    
    protected void closeRelatedLogicTextEditors(
            SAFREnvironmentalComponent component)
    {
        viewEditor.closeRelatedLogicTextEditors(component);
    }
    
    protected ViewEditorInput getViewInput() {
        return viewEditor.getViewInput();
    }
    
    protected void populatePhysicalFileCombo(TableCombo comboLF,
        TableCombo comboPF, TableComboViewer comboPFViewer) {
        viewEditor.populatePhysicalFileCombo(comboLF, comboPF, comboPFViewer);
    }

    protected void populateViewCombos(ComponentType componentType,
        TableCombo combo, TableComboViewer comboViewer, boolean b) {
        viewEditor.populateViewCombos(componentType, combo, comboViewer, b);
    }
        
    // calls on general
    
    public void setFocus() {
        viewGeneralEditor.setFocus();
    }

    
    protected void populateGeneralCombos() {
        viewGeneralEditor.populateCombos();
    }

    protected void refreshControlsGeneral() {
        viewGeneralEditor.refreshControlsGeneral();
    }

    protected Control getControlFromProperty(Object property) {
        Control control = viewGeneralEditor.getControlFromProperty(property);
        if (control == null) {
            control = viewExtractEditor.getControlFromProperty(property);
            if (control == null) {
                return viewFormatEditor.getControlFromProperty(property);
            }
            else {
                return control;                
            }
        }
        else {
            return control;
        }
    }

    protected CTabItem getGeneralTab() {
        return viewGeneralEditor.getGeneralTab();
    }    
            
    // extract
    protected void setEnableWorkFileNo(boolean b) {
        viewExtractEditor.setEnableWorkFileNo(b);        
    }

    protected void setDefaultWorkFileNumber() {
        viewExtractEditor.setDefaultWorkFileNumber();
    }
    
    protected void enableExtractOutputFile(boolean b) {
        viewExtractEditor.enableExtractOutputFile(b);        
    }    
    
    protected void deselectExtractLFPF() {
        viewExtractEditor.deselectExtractLFPF();        
    }    
    
    protected void loadExtractPhase() {
        viewExtractEditor.loadExtractPhase();
    }

    protected void populateExtractCombos() {
        viewExtractEditor.populateExtractCombos();
    }
    
    protected void refreshControlsExtract() {
        viewExtractEditor.refreshControlsExtract();
    }
    
    protected void refreshExtractRecordAggregationState() {
        viewExtractEditor.refreshExtractRecordAggregationState();
    }
    
    // format
    protected CTabItem getFormatTab() {
        return viewFormatEditor.getFormatTab();
    }
    
    protected void enableFormatOutputFile(boolean b) {
        viewFormatEditor.enableFormatOutputFile(b);        
    }
    
    protected void createTabFormatPhase() {
        viewFormatEditor.create();
    }
    
    protected void loadFormatPhase() {
        viewFormatEditor.loadFormatPhase();
    }

    protected void hideFormatPhase() {
        viewFormatEditor.hideFormatPhase();
    }
    
    protected void refreshControlsFormat() {
        viewFormatEditor.refreshControlsFormat();        
    }
    
    protected void updateFRFButtonState() {
        viewFormatEditor.updateFRFButtonState();
    }

    
    
    // calls on Column Editor
    protected boolean isCurrentViewSource() {
        return viewColumnEditor.isCurrentViewSource();
    }
    
    protected ViewSource getCurrentViewSource() {
        return viewColumnEditor.getCurrentViewSource();
    }

    protected boolean isCurrentViewColumnSource() {
        return viewColumnEditor.isCurrentViewColumnSource();
    }
    
    protected ViewColumnSource getCurrentViewColumnSource() {
        return viewColumnEditor.getCurrentViewColumnSource();
    }

    protected ViewSortKey getCurrentSortKey() {
        return viewColumnEditor.getCurrentSortKey();
    }

    protected boolean isCurrentSortKey() {
        return viewColumnEditor.isCurrentSortKey();
    }
    
    protected int getCurrentColIndex() {
        return viewColumnEditor.getCurrentColIndex();
    }
    
    protected ViewColumn getCurrentColumn() {
        return viewColumnEditor.getCurrentColumn();
    }
    
    protected List<ViewColumn> getSelectedColumns() {
        return viewColumnEditor.getSelectedColumns();
    }
    
    protected ViewColumn addNewColumn(int insertIndex, Boolean refresh) {
        return viewColumnEditor.addNewColumn(insertIndex, refresh);
    }
    
    protected void addSortKey() {
        viewColumnEditor.addSortKey();
    }

    protected void removeSortKey() {
        viewColumnEditor.removeSortKey();
    }
    
    protected void closePropertyView(PropertyViewType viewType) {
        viewColumnEditor.closePropertyView(viewType);
    }

    protected void openFocusedView() {
        viewColumnEditor.openFocusedView();
    }
    
    protected void copyViewColumns() {
        viewColumnEditor.copyViewColumns();
    }
    
    protected void deleteColumnEditorItem() {
        viewColumnEditor.delete();              
    }
    
    protected void refreshColumns() {
        viewColumnEditor.refreshColumns();                      
    }

    protected void refreshSortKeyRows() {
        viewColumnEditor.refreshSortKeyRows();                      
    }
    
    protected void insertDataSource() {
        viewColumnEditor.insertDataSource();                      
    }

    protected void copyDataSource() {
        viewColumnEditor.copyDataSource();                      
    }

    protected void columnGenerator() {
        viewColumnEditor.columnGenerator();                      
    }
    
    protected ViewColumn insertColumnBefore(Boolean refresh) {
        return viewColumnEditor.insertColumnBefore(refresh);
    }
    
    protected ViewColumn insertColumnAfter(boolean refresh) {
        return viewColumnEditor.insertColumnAfter(refresh);
    }
    
    
    protected void moveColumnLeft() {
        viewColumnEditor.moveColumnLeft();
    }

    protected void moveColumnRight() {
        viewColumnEditor.moveColumnRight();
    }
    
    protected void pasteViewColumnsLeft() {
        viewColumnEditor.pasteViewColumnsLeft();
    }

    protected void pasteViewColumnsRight() {
        viewColumnEditor.pasteViewColumnsRight();
    }
    
    protected void showColumn(final Integer colNum) {
        viewColumnEditor.showColumn(colNum);
    }

    protected void updateColumnSourceAffectedRows() {
        viewColumnEditor.updateColumnSourceAffectedRows();
    }

    protected void updateColumnSourceAffectedRows(ViewSource viewSource) {
        viewColumnEditor.updateColumnSourceAffectedRows(viewSource);
    }
    
    protected void updateOutputInformationChangeAffectedRows(
            boolean updatePropertyViews) {
        viewColumnEditor.updateOutputInformationChangeAffectedRows(updatePropertyViews);
    }
    
    protected void updateColumnEditorElement(int propertyId) {
        viewColumnEditor.updateElement(propertyId);
    }

    protected void updateSortKeyRows() {
        viewColumnEditor.updateSortKeyRows();
    }
    
    protected boolean copiedViewColumnsIsEmpty() {
        return viewColumnEditor.copiedViewColumnsIsEmpty();
    }
    
    protected void setColumnEditorFocus() {
        viewColumnEditor.setFocus();
    }
    
    protected int getColumnCount() {
        return viewColumnEditor.getColumnCount();
    }
    
    protected Composite getColumnEditorComposite() {
        return viewColumnEditor.composite;
    }
        
    // calls on ViewFormatReport
    protected void showFormatReport() {
        viewFormatReportEditor.show();
    }    
    
    protected void setHFFocus() {
        viewFormatReportEditor.setHFFocus();
    }
    
    protected void setHeaderFooterFocus(HFFocus focus) {
        viewFormatReportEditor.setHeaderFooterFocus(focus);
    }
 
    protected void refreshModel() {
        viewFormatReportEditor.refreshModel();
    }

    protected void hideFormatReport() {
        viewFormatReportEditor.hide();
    }

    protected CTabItem getHeaderFooterTab() {
        return viewFormatReportEditor.getHeaderFooterTab();
    }

    // calls on ViewFormatDelimited
    protected void showFormatDelimited() {
        viewFormatDelimitedEditor.show();
    }
    
    protected void hideFormatDelimited() {
        viewFormatDelimitedEditor.hide();
    }
    
    protected void loadDelimitersSection() {
        viewFormatDelimitedEditor.loadDelimitersSection();        
    }    
    
    
}
