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


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQueryBean;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.dialogs.DependencyMessageDialog;
import com.ibm.safr.we.ui.dialogs.MetadataListDialog;
import com.ibm.safr.we.ui.editors.OpenEditorPopupState;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationContentProvider;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationLabelProvider;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationTableSorter;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.utilities.SAFRLogger;

public class LogicalRecordLogFileEditor {
    
    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.lr.LogicalRecordLogFileEditor");

    private LogicalRecordMediator mediator;
    private CTabFolder tabFolder;
    private LogicalRecord logicalRecord = null;
    
    protected LogicalRecordLogFileEditor(LogicalRecordMediator mediator, CTabFolder tabFolder, LogicalRecord logicalRecord) {
        this.mediator = mediator;
        this.tabFolder = tabFolder;
        this.logicalRecord = logicalRecord;
    }
    
    /* Internal Widgets */    
    private Composite compositeAssociatedLF;
    private CheckboxTableViewer tableViewerAssociatedLF;
    private Table tableAssociatedLF;
    private Composite compositeLFButtonBar;
    private Button buttonAdd;
    private Button buttonRemove;
    
    private int prevSelection = 0;
    
    /**
     * This function creates the composite and its child elements for displaying
     * the Associated Logical Files section.
     */
    protected void create() {
        compositeAssociatedLF = mediator.getGUIToolKit().createComposite(tabFolder,
                SWT.NONE);
        compositeAssociatedLF.setLayout(UIUtilities.createTableLayout(2, true));
        compositeAssociatedLF.setLayoutData(new TableWrapData(
                TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));

        Composite compositeLFContent = mediator.getGUIToolKit().createComposite(
                compositeAssociatedLF, SWT.NONE);
        compositeLFContent.setLayout(new FormLayout());
        compositeLFContent.setLayoutData(new TableWrapData(
                TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));

        Composite compositeFiller = mediator.getGUIToolKit().createComposite(
                compositeAssociatedLF, SWT.NONE);
        compositeFiller.setLayout(new TableWrapLayout());
        compositeFiller.setLayoutData(new TableWrapData(
                TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));

        tableAssociatedLF = mediator.getGUIToolKit().createTable(compositeLFContent,
                SWT.CHECK | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL
                        | SWT.BORDER, false);
        tableAssociatedLF.setData(SAFRLogger.USER, "Associated Logical Files");
        
        tableViewerAssociatedLF = mediator.getGUIToolKit()
                .createCheckboxTableViewer(tableAssociatedLF);

        FormData dataTableLogicalFile = new FormData();
        dataTableLogicalFile.left = new FormAttachment(0, 0);
        dataTableLogicalFile.top = new FormAttachment(0, 10);
        dataTableLogicalFile.bottom = new FormAttachment(100, 0);
        dataTableLogicalFile.height = 250;
        tableAssociatedLF.setLayoutData(dataTableLogicalFile);
        mediator.getFormToolKit().adapt(tableAssociatedLF, false, false);

        tableAssociatedLF.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              
              if (event.detail == SWT.CHECK) {                  
                  int selRow = tableAssociatedLF.indexOf((TableItem)event.item);  
                  int stateMask=event.stateMask;                  
                  if((stateMask & SWT.SHIFT)==SWT.SHIFT){
                      int prevRow = prevSelection;
                      
                      if((stateMask & SWT.CTRL)!=SWT.CTRL){
                          tableViewerAssociatedLF.setAllChecked(false);
                      }
                      if (prevRow > selRow) {
                          for (int i=selRow ; i<=prevRow ; i++) {
                              Object element = tableViewerAssociatedLF.getElementAt(i);
                              tableViewerAssociatedLF.setChecked(element, true);
                          }
                      }
                      else {
                          for (int i=prevRow ; i<=selRow ; i++) {
                              Object element = tableViewerAssociatedLF.getElementAt(i);
                              tableViewerAssociatedLF.setChecked(element, true);
                          }                            
                      }
                  }   
                  else {
                      Object element = tableViewerAssociatedLF.getElementAt(selRow);
                      if (tableViewerAssociatedLF.getChecked(element)) {
                          prevSelection = tableAssociatedLF.indexOf((TableItem)event.item);
                      }
                      else {
                          prevSelection = 0;
                      }
                  }
              }                  
            }
          });
        
        
        UIUtilities.prepareTableViewerForShortList(tableViewerAssociatedLF,ComponentType.LogicalFile);
        tableViewerAssociatedLF
                .setContentProvider(new ComponentAssociationContentProvider());
        tableViewerAssociatedLF
                .setLabelProvider(new ComponentAssociationLabelProvider());
        tableViewerAssociatedLF.setInput(logicalRecord
                .getLogicalFileAssociations());
        
        SortOrderPrefs prefs = new SortOrderPrefs(UIUtilities.SORT_CATEGORY, ComponentType.LogicalFile.name());
        if (prefs.load()) {
            tableViewerAssociatedLF.getTable().setSortColumn(
                tableViewerAssociatedLF.getTable().getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                tableViewerAssociatedLF.getTable().setSortDirection(SWT.UP);
                tableViewerAssociatedLF.setSorter(new ComponentAssociationTableSorter(prefs.getColumn(), SWT.UP));
            }
            else {
                tableViewerAssociatedLF.getTable().setSortDirection(SWT.DOWN);
                tableViewerAssociatedLF.setSorter(new ComponentAssociationTableSorter(prefs.getColumn(), SWT.DOWN));
            }                   
        }
        else {
            tableViewerAssociatedLF.getTable().setSortColumn(
                tableViewerAssociatedLF.getTable().getColumn(1));
            tableViewerAssociatedLF.getTable().setSortDirection(SWT.UP);
            tableViewerAssociatedLF.setSorter(new ComponentAssociationTableSorter(1, SWT.UP));
        }       
        
        // CQ 8469 Kanchan Rauthan 3/09/201. To disable remove button if nothing
        // is selected to remove.
        tableViewerAssociatedLF.addCheckStateListener(new ICheckStateListener() {

                    public void checkStateChanged(CheckStateChangedEvent event) {
                        if (tableViewerAssociatedLF.getCheckedElements().length > 0 &&
                            mediator.getEditorInput().getEditRights() != EditRights.Read) {
                            buttonRemove.setEnabled(true);
                        } else {
                            buttonRemove.setEnabled(false);
                        }
                    }
                });

        compositeLFButtonBar = mediator.getGUIToolKit().createComposite(
                compositeLFContent, SWT.NONE);
        compositeLFButtonBar.setLayout(new FormLayout());

        // Code for tracking the focus on the table
        IFocusService service = (IFocusService) mediator.getSite().getService(
                IFocusService.class);
        service.addFocusTracker(tableAssociatedLF,
                "AssociatedLFSearchableTable");
        FormData dataButtonBar = new FormData();
        dataButtonBar.left = new FormAttachment(tableAssociatedLF, 10);
        dataButtonBar.right = new FormAttachment(100, 0);
        dataButtonBar.top = new FormAttachment(0, 10);
        dataButtonBar.bottom = new FormAttachment(100, 0);
        compositeLFButtonBar.setLayoutData(dataButtonBar);

        buttonAdd = mediator.getGUIToolKit().createButton(compositeLFButtonBar, SWT.PUSH,
                "A&dd");
        buttonAdd.setData(SAFRLogger.USER, "Add Logical File");       
        FormData dataButtonAdd = new FormData();
        dataButtonAdd.left = new FormAttachment(0, 5);
        dataButtonAdd.width = 50;
        buttonAdd.setLayoutData(dataButtonAdd);
        buttonAdd.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                // Display an hour glass till list of LFs is populated.
                Display.getCurrent().getActiveShell().setCursor(
                        Display.getCurrent().getActiveShell().getDisplay()
                                .getSystemCursor(SWT.CURSOR_WAIT));

                MetadataListDialog dialog = new MetadataListDialog(mediator.getSite()
                        .getShell(), TreeItemId.LOGICALFILE, logicalRecord,
                        UIUtilities.getCurrentEnvironmentID());
                dialog.create();
                dialog.open();
                
                List<SAFRQueryBean> returnList;
                if (dialog.getReturnCode() == Window.OK) {
                    returnList = dialog.getReturnList();
                    for (int i = 0; i < returnList.size(); i++) {
                        LogicalFileQueryBean bean = (LogicalFileQueryBean) returnList
                                .get(i);
                        try {
                            ComponentAssociation association = new ComponentAssociation(
                                    logicalRecord, bean.getId(),
                                    bean.getName(), bean.getRights());
                            logicalRecord.addAssociatedLogicalFile(association);
                            mediator.setDirty(true);
                        } catch (SAFRException se) {
                            UIUtilities.handleWEExceptions(se);
                        }
                    }
                    tableViewerAssociatedLF.refresh();
                }
                // return cursor to normal
                Display.getCurrent().getActiveShell().setCursor(null);
            }

        });

        buttonRemove = mediator.getGUIToolKit().createButton(compositeLFButtonBar,
                SWT.PUSH, "Re&move");
        buttonRemove.setData(SAFRLogger.USER, "Remove Logical File");             
        FormData dataButtonRemove = new FormData();
        dataButtonRemove.left = new FormAttachment(0, 5);
        dataButtonRemove.top = new FormAttachment(buttonAdd, 5);
        dataButtonRemove.width = 50;
        buttonRemove.setLayoutData(dataButtonRemove);
        buttonRemove.setEnabled(false);
        // align buttons width using preferred width of largest button.
        Point computeSize = buttonRemove.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dataButtonRemove.width = computeSize.x;
        dataButtonAdd.width = computeSize.x;

        buttonRemove.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                // Display an hour glass till list of LFs is populated.
                Display.getCurrent().getActiveShell().setCursor(
                        Display.getCurrent().getActiveShell().getDisplay()
                                .getSystemCursor(SWT.CURSOR_WAIT));

                List<ComponentAssociation> associationList = new ArrayList<ComponentAssociation>();
                for (Object obj : tableViewerAssociatedLF.getCheckedElements()) {
                    ComponentAssociation association = (ComponentAssociation) obj;
                    associationList.add(association);
                }
                int sizeBeforeDeleting = tableAssociatedLF.getItemCount();
                try {
                    if (!associationList.isEmpty()) {
                        logicalRecord
                                .removeAssociatedLogicalFiles(associationList);
                    }
                    buttonRemove.setEnabled(false);
                } catch (SAFRValidationException sve) {
                    DependencyMessageDialog
                            .openDependencyDialog(
                                    mediator.getSite().getShell(),
                                    "Logical Record/Logical File Dependencies",
                                    "The below logical files cannot be dissociated as they are used in a Lookup Path and/or View.",
                                    sve.getMessageString(),
                                    MessageDialog.ERROR,
                                    new String[] { IDialogConstants.OK_LABEL },
                                    0);
                } catch (DAOException sve) {
                    UIUtilities.handleWEExceptions(sve);
                }
                tableViewerAssociatedLF.refresh();
                int sizeAfterDeleting = tableAssociatedLF.getItemCount();
                if (sizeBeforeDeleting != sizeAfterDeleting) {
                    mediator.setDirty(true);
                }
                // return cursor to normal
                Display.getCurrent().getActiveShell().setCursor(null);
            }
        });
        
        tableViewerAssociatedLF.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                ComponentAssociation node = (ComponentAssociation)((StructuredSelection) event
                        .getSelection()).getFirstElement();
                if (node == null) {                    
                    setPopupEnabled(false);
                }
                else {
                    setPopupEnabled(true);
                }
            }
            
        });
        
        tableAssociatedLF.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent arg0) {
                setOpenPopupVisible(true);
                ComponentAssociation node = (ComponentAssociation) ((StructuredSelection)tableViewerAssociatedLF.
                        getSelection()).getFirstElement();                
                if (node == null)
                {
                    setPopupEnabled(false);                    
                }
                else {
                    setPopupEnabled(true);
                }
            }

            public void focusLost(FocusEvent arg0) {
                setOpenPopupVisible(false);
                setPopupEnabled(false);                                    
            }            
        });
        
        // Code for Context menu
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableAssociatedLF);
        // Set the MenuManager
        tableAssociatedLF.setMenu(menu);
        mediator.getSite().registerContextMenu(menuManager, tableViewerAssociatedLF);        
        setOpenPopupVisible(false);
        setPopupEnabled(false);                                         
    }

    private void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.LOGICALRECORD);
        service.setLogicalRecord(enabled);
    }

    private void setOpenPopupVisible(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.LOGICALRECORDV);
        service.setLogicalRecordVisible(enabled);
    }

    public ComponentAssociation getCurrentLF() {
        return (ComponentAssociation)((StructuredSelection)tableViewerAssociatedLF.getSelection()).getFirstElement();
    }

    public Control getCompositeLogFile() {
        return compositeAssociatedLF;
    }

    public void refreshLogFiles() {
        tableViewerAssociatedLF.refresh();
    }

    public void searchLFAssociation(MetadataSearchCriteria searchCriteria, String searchText) {
        if (this.tableAssociatedLF.isFocusControl()) {
            // if search criteria is id, then sort the list of components
            // according to id.
            if (searchCriteria == MetadataSearchCriteria.ID) {
                tableViewerAssociatedLF.getTable().setSortColumn(
                    tableViewerAssociatedLF.getTable().getColumn(0));
                tableViewerAssociatedLF.getTable().setSortDirection(SWT.UP);
                tableViewerAssociatedLF.setSorter(new ComponentAssociationTableSorter(0,
                        SWT.UP));
            } else {
                // if search criteria is name, then sort the list of components
                // according to name.
                tableViewerAssociatedLF.getTable().setSortColumn(
                    tableViewerAssociatedLF.getTable().getColumn(1));
                tableViewerAssociatedLF.getTable().setSortDirection(SWT.UP);
                tableViewerAssociatedLF.setSorter(new ComponentAssociationTableSorter(1,
                        SWT.UP));
            }

            // get the items of the table and apply search.
            for (TableItem item : tableViewerAssociatedLF.getTable().getItems()) {
                ComponentAssociation bean = (ComponentAssociation) item.getData();
                if (searchCriteria == MetadataSearchCriteria.ID) {
                    if (bean.getAssociatedComponentIdString().startsWith(searchText)) {
                        tableViewerAssociatedLF.setSelection(new StructuredSelection(bean),true);
                        return;
                    }
                } else if (searchCriteria == MetadataSearchCriteria.NAME) {
                    if (bean.getAssociatedComponentName() != null && 
                        bean.getAssociatedComponentName().toLowerCase().startsWith(searchText.toLowerCase())) {
                        tableViewerAssociatedLF.setSelection(new StructuredSelection(bean),true);
                        return;
                    }
                }
            }

            // if no component is found, then show the dialog box.
            MessageDialog.openInformation(mediator.getSite().getShell(),
                "Component not found",
                "The component you are trying to search is not found in the list.");
            
        }
    }
    
    public ComponentType getComponentType() {
        if (tableAssociatedLF.isFocusControl()) {
            return ComponentType.LogicalFile;
        }
        return null;
    }
}
