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


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.utilities.BatchActivateLookupPaths;
import com.ibm.safr.we.model.utilities.BatchComponent;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.editors.OpenEditorPopupState;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ActivateLookupPathsLookups {
    
    private class BatchActivateLookupPathsTableLabelProvider
        extends
            ColumnLabelProvider {

        private int columnIndex;

        public BatchActivateLookupPathsTableLabelProvider(int columnIndex) {
            super();
            this.columnIndex = columnIndex;
        }

        @Override
        public String getText(Object element) {
            if (!(element instanceof BatchComponent))
                return null;

            BatchComponent model = (BatchComponent) element;
            switch (columnIndex) {
                case 1 :
                    if (model.getResult() == ActivityResult.NONE) {
                        return "";
                    } else if (model.getResult() == ActivityResult.LOADERRORS) {
                        return "Load Error";
                    } else if (model.getResult().equals(ActivityResult.PASS)) {
                        return "Pass";
                    } else {
                        return "Fail";
                    }
                case 2 :
                    if (model.isActive()) {
                        return "Active";
                    } else
                        return "Inactive";
                case 3 :
                    return model.getComponent().getId().toString();
                case 4 :
                    return model.getComponent().getName();

            }
            return null;
        }

        @Override
        public Color getForeground(Object element) {

            BatchComponent item = (BatchComponent) element;
            switch (columnIndex) {
                case 1 :

                    if (item.getResult() == ActivityResult.LOADERRORS) {
                        return Display.getCurrent().getSystemColor(
                            SWT.COLOR_BLUE);
                    } else if (item.getResult() == ActivityResult.FAIL) {
                        return Display.getCurrent().getSystemColor(
                            SWT.COLOR_RED);
                    }
            }
            return null;

        }

    }


    /**
     * This class is used for column sorting in the Lookup paths Table.
     */
    private class ColumnSelectionListenerLookupTable extends SelectionAdapter {
        private int colNumber1;
        private TableViewer tabViewer1;

        ColumnSelectionListenerLookupTable(TableViewer tabViewer, int colNumber) {
            this.colNumber1 = colNumber;
            this.tabViewer1 = tabViewer;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                mediator.getSite().getShell().setCursor(
                        mediator.getSite().getShell().getDisplay()
                                .getSystemCursor(SWT.CURSOR_WAIT));
                TableColumn sortColumn = tabViewer1.getTable().getSortColumn();
                TableColumn currentColumn = (TableColumn) e.widget;
                int dir = tabViewer1.getTable().getSortDirection();
                if (sortColumn == currentColumn) {
                    dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                } else {
                    tabViewer1.getTable().setSortColumn(currentColumn);
                    if (currentColumn == tabViewer1.getTable().getColumn(0)) {
                        dir = SWT.DOWN;
                    } else {
                        dir = SWT.UP;
                    }

                }

                tabViewer1.getTable().setSortDirection(dir);
                tabViewer1.setSorter(new BatchActivateLookupPathsTableSorter(
                        colNumber1, dir));
                
                Order order = Order.ASCENDING;
                if (dir == SWT.DOWN) {
                    order = Order.DESCENDING;
                }
                SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE, colNumber1, order);
                prefs.store();                              
                
            } finally {
                mediator.getSite().getShell().setCursor(null);

            }

        }
    }

    private class StatusFilter extends ViewerFilter {

        private String status = null;

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement,
                Object element) {
            BatchComponent baComp = (BatchComponent) element;

            if (status != null) {
                if (baComp.isActive() && status.equals(ACTIVE)) {
                    return true;
                } else if (!baComp.isActive() && status.equals(INACTIVE)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
    }
    
    
    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.batchlookup.ActivateLookupPathsLookups");

    public static final int VALID = 1;
    public static String[] COLUMN_HEADERS = { "Select", "Result", "Status",
        "ID", "Name" };
    private final static String INACTIVE = "INACT";
    private final static String ACTIVE = "ACTVE";
    private static final String SORT_CATEGORY = "BatchActivate";
    private static final String SORT_TABLE = "Lookups";
    
    private ActivateLookupPathsMediator mediator;

    private Composite parent;
    private Section sectionTable;
    private Table tableLookup;
    private CheckboxTableViewer tableViewerLookups;
    private Composite compositeLookupPath;
    private ArrayList<BatchComponent> modelList = new ArrayList<BatchComponent>();;
    private Button buttonActivate;
    private StatusFilter filter;
    private int prevSelection = 0;    

    public ActivateLookupPathsLookups(ActivateLookupPathsMediator mediator, Composite parent) {
        super();
        this.mediator = mediator;
        this.parent = parent;
    }
    
    // Section to display the lookup paths
    protected void create() {

        sectionTable = mediator.getGUIToolKit().createSection(parent, Section.TITLE_BAR,
                "Lookup Paths");
        FormData dataSectionTable = new FormData();
        dataSectionTable.top = new FormAttachment(mediator.getSectionEnvironment(), 10);
        dataSectionTable.bottom = new FormAttachment(100, 0);
        dataSectionTable.left = new FormAttachment(0, 5);
        sectionTable.setLayoutData(dataSectionTable);

        compositeLookupPath = mediator.getGUIToolKit().createComposite(sectionTable,
                SWT.NONE);
        compositeLookupPath.setLayout(new FormLayout());
        compositeLookupPath.setLayoutData(new FormData());

        tableLookup = mediator.getGUIToolKit().createTable(compositeLookupPath, SWT.CHECK
                | SWT.FULL_SELECTION | SWT.BORDER, false);
        tableLookup.setData(SAFRLogger.USER, "Lookup Paths");
        tableViewerLookups = new CheckboxTableViewer(tableLookup);
        filter = new StatusFilter();
        tableViewerLookups.addFilter(filter);
        tableLookup.setHeaderVisible(true);
        tableLookup.setLinesVisible(true);
        FormData dataTable = new FormData();
        dataTable.left = new FormAttachment(0, 5);
        dataTable.right = new FormAttachment(100, 0);
        dataTable.top = new FormAttachment(0, 10);
        dataTable.bottom = new FormAttachment(100, -50);
        dataTable.height = 0;
        dataTable.width = 700;
        tableLookup.setLayoutData(dataTable);

        tableLookup.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              
              if (event.detail == SWT.CHECK) {                  
                  int selRow = tableLookup.indexOf((TableItem)event.item);  
                  int stateMask=event.stateMask;                  
                  if((stateMask & SWT.SHIFT)==SWT.SHIFT){
                      int prevRow = prevSelection;
                      
                      if((stateMask & SWT.CTRL)!=SWT.CTRL){
                          tableViewerLookups.setAllChecked(false);
                          for (Object obj : tableViewerLookups.getCheckedElements()) {
                              ((BatchComponent)obj).setSelected(false);
                          }                          
                      }
                      if (prevRow > selRow) {
                          for (int i=selRow ; i<=prevRow ; i++) {
                              Object element = tableViewerLookups.getElementAt(i);
                              tableViewerLookups.setChecked(element, true);
                              modelList.get(i).setSelected(true);
                          }
                      }
                      else {
                          for (int i=prevRow ; i<=selRow ; i++) {
                              Object element = tableViewerLookups.getElementAt(i);
                              tableViewerLookups.setChecked(element, true);
                              modelList.get(i).setSelected(true);                              
                          }                            
                      }
                  }   
                  else {
                      Object element = tableViewerLookups.getElementAt(selRow);
                      if (tableViewerLookups.getChecked(element)) {
                          prevSelection = tableLookup.indexOf((TableItem)event.item);
                      }
                      else {
                          prevSelection = 0;
                      }
                  }
              }                  
            }
          });
        
        Composite compositeButton = mediator.getGUIToolKit().createComposite(
                compositeLookupPath, SWT.NONE);

        compositeButton.setLayout(new FormLayout());
        FormData dataButtonBar = new FormData();
        dataButtonBar.left = new FormAttachment(0, 5);
        dataButtonBar.top = new FormAttachment(tableLookup, 10);
        dataButtonBar.bottom = new FormAttachment(100, 0);
        compositeButton.setLayoutData(dataButtonBar);

        // Button to select all the check boxes in the table
        Button selectAll = mediator.getGUIToolKit().createButton(compositeButton,
                SWT.PUSH, "&Select All");
        selectAll.setData(SAFRLogger.USER, "Select All");
        
        FormData dataSelectAll = new FormData();
        dataSelectAll.left = new FormAttachment(0, 0);
        selectAll.setLayoutData(dataSelectAll);

        selectAll.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
                tableViewerLookups.setAllChecked(true);
                if (modelList == null) {
                    return;
                }
                for (BatchComponent comp : modelList) {
                    comp.setSelected(true);
                }
                buttonActivate.setEnabled(true);
                prevSelection = 0;                              
            }
        });

        // Button to deselect all the selected checkboxes
        Button deselectAll = mediator.getGUIToolKit().createButton(compositeButton,
                SWT.PUSH, "&Deselect All");
        deselectAll.setData(SAFRLogger.USER, "Deselect All");
        
        FormData dataDeselectAll = new FormData();
        dataDeselectAll.left = new FormAttachment(selectAll, 10);
        deselectAll.setLayoutData(dataDeselectAll);

        deselectAll.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
                tableViewerLookups.setAllChecked(false);
                if (modelList == null) {
                    return;
                }
                for (BatchComponent comp : modelList) {
                    comp.setSelected(false);
                }
                buttonActivate.setEnabled(false);
                prevSelection = 0;
            }
        });

        // CQ9747 refresh button
        Button buttonRefresh = mediator.getGUIToolKit().createButton(compositeButton,
                SWT.PUSH, "&Refresh");
        buttonRefresh.setData(SAFRLogger.USER, "Refresh");        
        FormData dataRefresh = new FormData();
        dataRefresh.left = new FormAttachment(deselectAll, 10);
        buttonRefresh.setLayoutData(dataRefresh);
        
        buttonRefresh.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                // no op
            }

            public void widgetSelected(SelectionEvent e) {
                loadData();                 
                tableViewerLookups.refresh();
                tableViewerLookups.setAllChecked(false);
                mediator.showSectionErrors(false);
                buttonActivate.setEnabled(false);
                prevSelection = 0;
            }

        });     

        // Button to activate the Lookups selected
        buttonActivate = mediator.getGUIToolKit().createButton(compositeButton,
                SWT.PUSH, "Ac&tivate");
        buttonActivate.setData(SAFRLogger.USER, "Activate");              
        FormData dataActivate = new FormData();
        dataActivate.left = new FormAttachment(buttonRefresh, 10);
        buttonActivate.setLayoutData(dataActivate);
        buttonActivate.setEnabled(false);

        buttonActivate.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {

                Object[] checkedElements = null;
                checkedElements = tableViewerLookups.getCheckedElements();
                List<BatchComponent> list = new ArrayList<BatchComponent>();
                if (modelList == null) {
                    return;
                }
                // This loop is used to clear the results and errors before the
                // activation process.
                for (BatchComponent tmpList : modelList) {
                    tmpList.setResult(ActivityResult.NONE);
                    tmpList.setException(null);
                }
                tableViewerLookups.refresh(true, true);

                for (Object item : checkedElements) {
                    BatchComponent modelItem = (BatchComponent) item;
                    list.add(modelItem);
                }
                try {
                    mediator.getSite().getShell().setCursor(
                            mediator.getSite().getShell().getDisplay()
                                    .getSystemCursor(SWT.CURSOR_WAIT));
                    BatchActivateLookupPaths.activate(mediator.getCurrentEnvironment().getId(), list);
                    // refresh metadata view if it contains a list of lookup paths.
                    ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.LookupPath, null);
                } catch (DAOException de) {
                    UIUtilities.handleWEExceptions(de,
                        "Database error while activating the lookup path.",UIUtilities.titleStringDbException);
                } catch (SAFRException se) {
                    UIUtilities.handleWEExceptions(se,
                        "Error activating the lookup path.", null);
                } finally {
                    mediator.getSite().getShell().setCursor(null);
                }

                // check open/close the errors section
                TableItem[] items = tableLookup.getSelection();
                if (items.length > 0) {
                    BatchComponent bean = (BatchComponent) items[0].getData();
                    if (bean.getResult() == null || bean.getResult() == ActivityResult.PASS) {
                        mediator.showSectionErrors(false);
                    }
                    else {
                        mediator.showSectionErrors(true);                                
                    }
                }
                
                tableViewerLookups.refresh();

                
            }

        });

        // Set width of all buttons to the widest button
        List<Button> buttons = new ArrayList<Button>();
        buttons.add(selectAll);
        buttons.add(deselectAll);
        buttons.add(buttonRefresh);
        buttons.add(buttonActivate);
        int width = UIUtilities.computePreferredButtonWidth(buttons);
        dataSelectAll.width = width;
        dataDeselectAll.width = width;
        dataRefresh.width = width;
        dataActivate.width = width;
        
        // Add Active/Inactive filter checkboxes
        Group group = new Group(compositeLookupPath, SWT.SHADOW_IN);
        group.setLayout(new FormLayout());
        FormData dataGroup = new FormData();
        dataGroup.top = new FormAttachment(tableLookup, 0);
        dataGroup.right = new FormAttachment(100, 0);
        dataGroup.width = 200;
        Device device = PlatformUI.getWorkbench().getDisplay();        
        Point p = device.getDPI();
        if (p.x <= 100 && p.y <= 100) {
            dataGroup.width = 200;
            dataGroup.height = 18;
        } else {
            dataGroup.width = 230;
            dataGroup.height = 22;
        }        
        group.setLayoutData(dataGroup);
        group.setVisible(true); 
        
        Button radioActive = mediator.getGUIToolKit().createRadioButton(group, "Active");
        radioActive.setData(SAFRLogger.USER, "Filter Active");                      
        FormData dataActive = new FormData();
        dataActive.left = new FormAttachment(5, 5);
        radioActive.setLayoutData(dataActive);
        radioActive.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                filter.setStatus(ACTIVE);
                prevSelection = 0;                                              
                tableViewerLookups.refresh();
            }
        });

        Button radioInactive = mediator.getGUIToolKit().createRadioButton(group, "Inactive");
        radioInactive.setData(SAFRLogger.USER, "Filter Inactive");                      
        FormData dataInactive = new FormData();
        dataInactive.left = new FormAttachment(radioActive, 5);
        radioInactive.setLayoutData(dataInactive);
        radioInactive.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                filter.setStatus(INACTIVE);
                prevSelection = 0;                                              
                tableViewerLookups.refresh();
            }
        });

        Button radioBoth = mediator.getGUIToolKit().createRadioButton(group, "Both");
        radioBoth.setData(SAFRLogger.USER, "Filter Both");                              
        FormData dataBoth = new FormData();
        dataBoth.left = new FormAttachment(radioInactive, 5);
        radioBoth.setLayoutData(dataBoth);
        radioBoth.setSelection(true);
        radioBoth.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                filter.setStatus(null);
                prevSelection = 0;                                              
                tableViewerLookups.refresh();
            }
        });
        
        int iCounter = 0;
        int[] columnWidths = { 75, 80, 75, 65, 290 };
        int length = columnWidths.length;

        for (iCounter = 0; iCounter < length; iCounter++) {
            TableViewerColumn column = new TableViewerColumn(tableViewerLookups,
                    SWT.NONE);
            column.getColumn().setText(COLUMN_HEADERS[iCounter]);
            column.getColumn().setToolTipText(COLUMN_HEADERS[iCounter]);
            column.getColumn().setWidth(columnWidths[iCounter]);
            column.getColumn().setResizable(true);
            column.setLabelProvider(new BatchActivateLookupPathsTableLabelProvider(
                    iCounter));
            ColumnSelectionListenerLookupTable colListenerLookupTable = new ColumnSelectionListenerLookupTable(
                    tableViewerLookups, iCounter);
            column.getColumn().addSelectionListener(colListenerLookupTable);

        }

        tableViewerLookups.setContentProvider(new IStructuredContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }

            public void dispose() {

            }

            public Object[] getElements(Object inputElement) {
                if (mediator.getCurrentEnvironment() != null) {
                    return modelList.toArray();
                } else {
                    return new String[0];
                }
            }

        });

        tableViewerLookups.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                BatchComponent component = (BatchComponent) event.getElement();
                if (event.getChecked()) {
                    component.setSelected(true);
                } else {
                    component.setSelected(false);
                }
                
                if (tableViewerLookups.getCheckedElements().length != 0) {
                    buttonActivate.setEnabled(true);
                } else {
                    buttonActivate.setEnabled(false);
                }
            }
        });

        tableViewerLookups
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        BatchComponent currModel = (BatchComponent) ((IStructuredSelection) event
                                .getSelection()).getFirstElement();
                        if (currModel != null) {
                            if (currModel.getException() == null) {
                                mediator.showSectionErrors(false);
                            } else {
                                mediator.showSectionErrors(true);
                            }
                            EnvironmentQueryBean ebean = mediator.getCurrentEnvironmentBean();
                            if (ebean != null & ebean.getId() == UIUtilities.getCurrentEnvironmentID()) {
                                EnvironmentalQueryBean bean = currModel.getComponent();
                                if (bean == null) {
                                    setPopupEnabled(false);                                                                    
                                }
                                else {
                                    setPopupEnabled(true);                                                                    
                                }
                            }
                            else {
                                setPopupEnabled(false);                                                                                                                             
                            }                           
                        }
                        else {
                            setPopupEnabled(false);                                                                                             
                        }
                    }

                });
        // Code for tracking the focus on the table
        IFocusService service = (IFocusService) mediator.getSite().getService(
                IFocusService.class);
        service.addFocusTracker(tableLookup, "LookupSearchableTable");
        tableViewerLookups.setInput(1);
        
        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE);
        if (prefs.load()) {
            tableViewerLookups.getTable().setSortColumn(
                tableViewerLookups.getTable().getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                tableViewerLookups.getTable().setSortDirection(SWT.UP);
                tableViewerLookups.setSorter(new BatchActivateLookupPathsTableSorter(prefs.getColumn(), SWT.UP));
            }
            else {
                tableViewerLookups.getTable().setSortDirection(SWT.DOWN);
                tableViewerLookups.setSorter(new BatchActivateLookupPathsTableSorter(prefs.getColumn(), SWT.DOWN));
            }                   
        }
        else {
            tableViewerLookups.getTable().setSortColumn(
                tableViewerLookups.getTable().getColumn(4));
            tableViewerLookups.getTable().setSortDirection(SWT.UP);
            tableViewerLookups.setSorter(new BatchActivateLookupPathsTableSorter(4, SWT.UP));
        }       
        
        
        sectionTable.setClient(compositeLookupPath);
        
        // Code for Context menu
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableViewerLookups.getTable());
        tableViewerLookups.getTable().setMenu(menu);
        mediator.getSite().registerContextMenu(menuManager, tableViewerLookups);        
        setPopupEnabled(false);     
        
    }

    public BatchComponent getCurrentSelection() {
        return (BatchComponent)((StructuredSelection) tableViewerLookups.getSelection()).getFirstElement();
    }
    
    private void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.ACTLU);
        service.setActLU(enabled);
    }
    
    protected void loadData() {
        ArrayList<LookupQueryBean> list = null;
        
        if (mediator.getCurrentEnvironment() == null) {
            list = new ArrayList<LookupQueryBean>();
        }
        else {
            try {
                list = (ArrayList<LookupQueryBean>) SAFRQuery.queryLookupsForBAL(
                        mediator.getCurrentEnvironment().getId(),
                        SortType.SORT_BY_NAME);
            } catch (DAOException de) {
                UIUtilities.handleWEExceptions(de,
                    "Unexpected database error occurred while retrieving all Lookup Paths.",
                    UIUtilities.titleStringDbException);
            }
        }

        modelList.clear();
        for (LookupQueryBean lookup : list) {
            boolean status = (lookup.getValidInd()==1);
            BatchComponent model = new BatchComponent(lookup, status);
            modelList.add(model);
        }
        tableViewerLookups.setInput(1);
    }

    public void searchComponent(MetadataSearchCriteria searchCriteria,
        String searchText) {
        TableViewer tabViewer = null;
        // if the focus is on lookup table then apply search on that table.
        if (this.tableLookup.isFocusControl()) {
            tabViewer = this.tableViewerLookups;
        }
        if (tabViewer != null) {
            // if the search criteria ID, then sort the list of lookup paths
            // according to ID.
            if (searchCriteria == MetadataSearchCriteria.ID) {
                tabViewer.getTable().setSortColumn(
                    tabViewer.getTable().getColumn(3));
                tabViewer.getTable().setSortDirection(SWT.UP);
                tabViewer.setSorter(new BatchActivateLookupPathsTableSorter(3,
                    SWT.UP));
            } else {
                // if the search criteria name, then sort the list of lookup
                // paths
                // according to name.
                tabViewer.getTable().setSortColumn(
                    tabViewer.getTable().getColumn(4));
                tabViewer.getTable().setSortDirection(SWT.UP);
                tabViewer.setSorter(new BatchActivateLookupPathsTableSorter(4,
                    SWT.UP));
            }

            // get the items of the table.
            for (TableItem item : tabViewer.getTable().getItems()) {
                BatchComponent bean = (BatchComponent) item.getData();
                if (searchCriteria == MetadataSearchCriteria.ID) {
                    if (bean.getComponent().getIdLabel().startsWith(searchText)) {
                        tabViewer.setSelection(new StructuredSelection(bean),
                            true);
                        return;
                    }
                } else if (searchCriteria == MetadataSearchCriteria.NAME) {
                    if (bean.getComponent().getNameLabel() != null
                        && bean.getComponent().getNameLabel().toLowerCase()
                            .startsWith(searchText.toLowerCase())) {
                        tabViewer.setSelection(new StructuredSelection(bean),
                            true);
                        return;
                    }
                }
            }

            // if no component is found, show dialog box.
            MessageDialog
                .openInformation(mediator.getSite().getShell(), "Component not found",
                    "The component you are trying to search is not found in the list.");
        }
    }

    public ComponentType getComponentType() {
        if (tableLookup.isFocusControl()) {
            return ComponentType.LookupPath;
        }
        return null;
    }

    public Control getSectionLookups() {
        return sectionTable;
    }

    
    
}
