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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.utilities.BatchActivateViews;
import com.ibm.safr.we.model.utilities.BatchComponent;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.editors.OpenEditorPopupState;
import com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy;
import com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy.SAFRGUIContext;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.MetadataView;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ActivateViewsViews {

    /**
     * Inner class for displaying data in the Views table.
     */
    class BatchActivateViewsTableLabelProvider extends ColumnLabelProvider {

        private int columnIndex;

        public BatchActivateViewsTableLabelProvider(int columnIndex) {
            super();
            this.columnIndex = columnIndex;
        }

        @Override
        public String getText(Object element) {
            if (!(element instanceof BatchComponent))
                return null;

            BatchComponent model = (BatchComponent) element;
            ViewQueryBean bean = (ViewQueryBean) model.getComponent();
            switch (columnIndex) {
            case 1:
                if (model.getResult() == ActivityResult.NONE) {
                    return "";
                } else if (model.getResult() == ActivityResult.LOADERRORS) {
                    return "Load Error";
                } else if (model.getResult().equals(ActivityResult.PASS)) {
                    return "Completed";
                } else if (model.getResult().equals(ActivityResult.WARNING)) {
                    return "Warnings";
                } else {
                    return "Errors";
                }
            case 2:
                if (model.isActive()) {
                    return "Active";
                } else {
                    return "Inactive";
                }
            case 3:
                return bean.getId().toString();
            case 4:
                return bean.getName();
            default:
                return null;
            }
        }

        @Override
        public Color getForeground(Object element) {
            return null;
        }

        @Override
        public Color getBackground(Object element) {
            BatchComponent item = (BatchComponent) element;
            switch (item.getResult()) {
            case FAIL:
                return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
            case LOADERRORS:
                return Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
            case PASS:
                return Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
            case SYSTEMERROR:
                break;
            case WARNING:
                return Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
            case NONE:
            case CANCEL:
            default:
                break;
            }
            return null;
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
    
    private class ColumnSelectionListenerViewTable extends SelectionAdapter {
        private int colNumber1;
        private TableViewer tabViewer1;

        ColumnSelectionListenerViewTable(TableViewer tabViewer, int colNumber) {
            this.colNumber1 = colNumber;
            this.tabViewer1 = tabViewer;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                mediator.getSite().getShell().setCursor(
                        mediator.getSite().getShell().getDisplay().getSystemCursor(
                                SWT.CURSOR_WAIT));
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
                tabViewer1.setSorter(new BatchActivateViewsTableSorter(
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
    
    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.batchview.ActivateViewsViews");
        
    private static final String ACTIVE = "ACTVE";
    private static final String INACTIVE = "INACT";
    
    private static final String SORT_CATEGORY = "BatchActivate";
    private static final String SORT_TABLE = "Views";
    
    private static String[] COLUMN_HEADERS = { "Select", "Result", "Status", 
        "ID", "Name" };
        
    private ActivateViewsMediator mediator;

    private Composite parent;
    private Section sectionTable;   
    private Composite compositeViews;
    private CheckboxTableViewer tableViewerViews;
    private Table tableView;
    private List<BatchComponent> modelList = new ArrayList<BatchComponent>();
    private BatchComponent currModel;
    private Button buttonActivate;
    private StatusFilter statusFilter;
    private int prevSelection = 0;
    
    public ActivateViewsViews(ActivateViewsMediator mediator, Composite parent) {
        super();
        this.mediator = mediator;
        this.parent = parent;
    }
    
    
    protected void create() {
        sectionTable = mediator.getGUIToolKit().createSection(parent, Section.TITLE_BAR ,
                "Views");
        FormData dataSectionTable = new FormData();
        dataSectionTable.top = new FormAttachment(mediator.getEnvironmentSection(), 10);
        dataSectionTable.bottom = new FormAttachment(100, 0);
        dataSectionTable.left = new FormAttachment(0, 5);
        sectionTable.setLayoutData(dataSectionTable);

        compositeViews = mediator.getGUIToolKit().createComposite(sectionTable, SWT.BORDER);
        compositeViews.setLayout(new FormLayout());
        compositeViews.setLayoutData(new FormData());

        tableView = mediator.getGUIToolKit().createTable(compositeViews, SWT.CHECK
                | SWT.FULL_SELECTION | SWT.BORDER, false);
        tableView.setData(SAFRLogger.USER, "Views");                                      
        tableViewerViews = new CheckboxTableViewer(tableView);
        statusFilter = new StatusFilter();
        tableViewerViews.addFilter(statusFilter);
        tableView.setHeaderVisible(true);
        tableView.setLinesVisible(true);
        FormData dataTable = new FormData();
        dataTable.left = new FormAttachment(0, 5);
        dataTable.right = new FormAttachment(100, 0);
        dataTable.top = new FormAttachment(0, 10);
        dataTable.bottom = new FormAttachment(100, -50);
        dataTable.height = 0;
        dataTable.width = 780;
        tableView.setLayoutData(dataTable);

        tableView.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              
              if (event.detail == SWT.CHECK) {                  
                  int selRow = tableView.indexOf((TableItem)event.item);  
                  int stateMask=event.stateMask;                  
                  if((stateMask & SWT.SHIFT)==SWT.SHIFT){
                      int prevRow = prevSelection;
                      
                      if((stateMask & SWT.CTRL)!=SWT.CTRL){
                          tableViewerViews.setAllChecked(false);
                          for (Object obj : tableViewerViews.getCheckedElements()) {
                              ((BatchComponent)obj).setSelected(false);
                          }                          
                      }
                      if (prevRow > selRow) {
                          for (int i=selRow ; i<=prevRow ; i++) {
                              Object element = tableViewerViews.getElementAt(i);
                              tableViewerViews.setChecked(element, true);
                              modelList.get(i).setSelected(true);
                          }
                      }
                      else {
                          for (int i=prevRow ; i<=selRow ; i++) {
                              Object element = tableViewerViews.getElementAt(i);
                              tableViewerViews.setChecked(element, true);
                              modelList.get(i).setSelected(true);
                          }                            
                      }
                  }   
                  else {
                      Object element = tableViewerViews.getElementAt(selRow);
                      if (tableViewerViews.getChecked(element)) {
                          prevSelection = tableView.indexOf((TableItem)event.item);
                      }
                      else {
                          prevSelection = 0;
                      }
                  }
              }                  
            }
          });
        
        Composite compositeButton = mediator.getGUIToolKit().createComposite(
                compositeViews, SWT.NONE);

        compositeButton.setLayout(new FormLayout());
        FormData dataButtonBar = new FormData();
        dataButtonBar.left = new FormAttachment(0, 5);
        dataButtonBar.top = new FormAttachment(tableView, 10);
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
                List<BatchComponent> selectedComps = new ArrayList<BatchComponent>();
                for (BatchComponent batchComponent : modelList) {
                    EditRights rights = batchComponent.getComponent().getRights();
                    if (UIUtilities.isAdminOrMigrateInUser(mediator.getCurrentEnvironment().getId()) ||
                        rights.equals(EditRights.ReadModify) || rights.equals(EditRights.ReadModifyDelete)) {
                        selectedComps.add(batchComponent);
                    }
                }
                tableViewerViews.setCheckedElements(selectedComps.toArray());
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
                tableViewerViews.setAllChecked(false);
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
                tableViewerViews.setInput(modelList);
                tableViewerViews.refresh();
                tableViewerViews.setAllChecked(false);
                mediator.showSectionLoadErrors(false);
                buttonActivate.setEnabled(false);
                prevSelection = 0;              
            }

        });     
        
        // Button to activate the Views selected
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
                Set<BatchComponent> viewSet = new HashSet<BatchComponent>();
                checkedElements = tableViewerViews.getCheckedElements();
                if (modelList == null) {
                    return;
                }
                for (Object item : checkedElements) {
                    BatchComponent modelItem = (BatchComponent) item;
                    viewSet.add(modelItem);
                }
                try {
                    mediator.getSite().getShell().setCursor(mediator.getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

                    try {
                        BatchActivateViews.activate(viewSet, new SAFRGUIConfirmWarningStrategy(
                                SAFRGUIContext.MODEL));
                        // refresh the metadata view
                        MetadataView metadataview = (MetadataView) (PlatformUI
                                .getWorkbench().getActiveWorkbenchWindow()
                                .getActivePage().findView(MetadataView.ID));
                        if (metadataview != null) {
                            List<Integer> views = new ArrayList<Integer>();
                            for (BatchComponent comp : viewSet) {
                                views.add(comp.getComponent().getId());
                            }
                            metadataview.refreshViewList(views);
                        }
                    } catch (SAFRException se) {
                        UIUtilities.handleWEExceptions(
                            se,"Unexpected error occurred while activating the views.",null);
                    }

                } finally {
                    mediator.getSite().getShell().setCursor(null);
                }
                tableViewerViews.setInput(modelList);
                tableViewerViews.refresh();
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
        Group group = new Group(compositeViews, SWT.SHADOW_IN);
        group.setLayout(new FormLayout());
        FormData dataGroup = new FormData();
        dataGroup.top = new FormAttachment(tableView, 0);
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
                statusFilter.setStatus(ACTIVE);
                prevSelection = 0;                
                tableViewerViews.setInput(modelList);
                tableViewerViews.refresh();
            }
        });

        Button radioInactive = mediator.getGUIToolKit().createRadioButton(group, "Inactive");
        radioInactive.setData(SAFRLogger.USER, "Filter Inactive");                                                                          
        FormData dataInactive = new FormData();
        dataInactive.left = new FormAttachment(radioActive, 5);
        radioInactive.setLayoutData(dataInactive);
        radioInactive.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                statusFilter.setStatus(INACTIVE);
                prevSelection = 0;                
                tableViewerViews.setInput(modelList);
                tableViewerViews.refresh();
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
                statusFilter.setStatus(null);
                prevSelection = 0;                
                tableViewerViews.setInput(modelList);
                tableViewerViews.refresh();
            }
        });
        
        int iCounter = 0;
        int[] columnWidths = { 75, 100, 100, 100, 400 };
        int length = columnWidths.length;

        for (iCounter = 0; iCounter < length; iCounter++) {
            TableViewerColumn column = new TableViewerColumn(tableViewerViews,
                    SWT.NONE);
            column.getColumn().setText(COLUMN_HEADERS[iCounter]);
            column.getColumn().setToolTipText(COLUMN_HEADERS[iCounter]);
            column.getColumn().setWidth(columnWidths[iCounter]);
            column.getColumn().setResizable(true);
            column.setLabelProvider(new BatchActivateViewsTableLabelProvider(
                    iCounter));
            ColumnSelectionListenerViewTable colListenerViewTable = new ColumnSelectionListenerViewTable(
                    tableViewerViews, iCounter);
            column.getColumn().addSelectionListener(colListenerViewTable);

        }

        tableViewerViews.setContentProvider(new IStructuredContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }

            public void dispose() {

            }

            @SuppressWarnings("unchecked")
            public Object[] getElements(Object inputElement) {
                List<BatchComponent> mList = (List<BatchComponent>)inputElement;
                if (mList != null) {
                    return mList.toArray();
                } else
                    return new String[0];
            }

        });
        tableViewerViews.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                BatchComponent component = (BatchComponent) event.getElement();
                if (component.getComponent().getRights() == EditRights.Read) {
                    event.getCheckable().setChecked(component, false);
                } else {
                    if (event.getChecked()) {
                        component.setSelected(true);
                    } else {
                        component.setSelected(false);
                    }
                }
                
                if (tableViewerViews.getCheckedElements().length != 0) {
                    buttonActivate.setEnabled(true);
                } else {
                    buttonActivate.setEnabled(false);
                }
            }
        });

        tableViewerViews.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                // first check for changed model
                BatchComponent selModel = (BatchComponent) ((IStructuredSelection) event
                    .getSelection()).getFirstElement();
                if (selModel != null) {
                    currModel = selModel;
                    showActivationResult();
                    
                    EnvironmentalQueryBean bean = currModel.getComponent();
                    if (bean != null) {
                        setPopupEnabled(true);
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
        service.addFocusTracker(tableView, "ViewSearchableTable");

        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE);
        if (prefs.load()) {
            tableViewerViews.getTable().setSortColumn(
                tableViewerViews.getTable().getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                tableViewerViews.getTable().setSortDirection(SWT.UP);
                tableViewerViews.setSorter(new BatchActivateViewsTableSorter(prefs.getColumn(), SWT.UP));
            }
            else {
                tableViewerViews.getTable().setSortDirection(SWT.DOWN);
                tableViewerViews.setSorter(new BatchActivateViewsTableSorter(prefs.getColumn(), SWT.DOWN));
            }                   
        }
        else {
            tableViewerViews.getTable().setSortColumn(
                tableViewerViews.getTable().getColumn(4));
            tableViewerViews.getTable().setSortDirection(SWT.UP);
            tableViewerViews.setSorter(new BatchActivateViewsTableSorter(4, SWT.UP));
        }       
        sectionTable.setClient(compositeViews);     
        
        tableViewerViews.setInput(modelList);
        tableViewerViews.refresh();
                
        // Code for Context menu
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableViewerViews.getTable());
        tableViewerViews.getTable().setMenu(menu);
        mediator.getSite().registerContextMenu(menuManager, tableViewerViews);        
        setPopupEnabled(false);     
        
    }
    
    private void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.ACTVIEW);
        service.setActView(enabled);
    }    

    protected void loadData() {
        List<ViewQueryBean> list = null;
        if (mediator.getCurrentEnvironment() == null) {
            list = new ArrayList<ViewQueryBean>();
        }
        else {
            try {
                ViewFolder vf = mediator.getCurrentViewFolder();
                if (vf == null) {
                    list = SAFRQuery.queryAllViews(mediator.getCurrentEnvironment().getId(),
                        SortType.SORT_BY_NAME, UIUtilities.isAdminOrMigrateInUser(mediator.getCurrentEnvironment().getId()));
                } else {
                    list = SAFRQuery.queryAllViews(mediator.getCurrentEnvironment().getId(), vf.getId(), 
                        SortType.SORT_BY_NAME);                    
                }
            } catch (DAOException de) {
                UIUtilities.handleWEExceptions(de,
                    "Unexpected database error occurred while getting all the views.",UIUtilities.titleStringDbException);
            }
        }

        modelList.clear();
        for (ViewQueryBean view : list) {
            if (UIUtilities.isAdminOrMigrateInUser(mediator.getCurrentEnvironment().getId()) || 
                view.getRights().equals(EditRights.ReadModify) || 
                view.getRights().equals(EditRights.ReadModifyDelete)) {
                String viewStatus = (view.getStatus());
                boolean status = false;
                if (viewStatus.trim().equalsIgnoreCase(ACTIVE)) {
                    status = true;
                } else if (viewStatus.trim().equalsIgnoreCase(INACTIVE)) {
                    status = false;
                }
                BatchComponent model = new BatchComponent(view, status);
                modelList.add(model);
            }
        }
    }
    
    protected void showActivationResult() {
        switch (currModel.getResult()) {
            case LOADERRORS :
                mediator.showSectionLoadErrors(true);
                break;
            case WARNING :
            case FAIL :
                mediator.showSectionLoadErrors(true);
                break;
            case PASS :
                mediator.showSectionLoadErrors(true);
                break;
            case CANCEL :
            case SYSTEMERROR :
                mediator.showSectionLoadErrors(true);
                break;
            case NONE :
            default :
                break;            
        }
    }
    
    public void refreshViews() {
        tableViewerViews.setInput(modelList);
        tableViewerViews.refresh();
    }
    
    public BatchComponent getCurrentSelection() {
        return (BatchComponent)((StructuredSelection) tableViewerViews.getSelection()).getFirstElement();
    }

    public void setFocusViews() {
        tableViewerViews.getTable().setFocus();        
    }
    
    public SAFRViewActivationException getViewActivationException() {
        if ( currModel != null && 
            (currModel.getResult() == ActivityResult.FAIL ||
             currModel.getResult() == ActivityResult.WARNING)) {
            return (SAFRViewActivationException)currModel.getException();
        } else {
            return null;
        }
    }

    public void searchComponent(MetadataSearchCriteria searchCriteria,
        String searchText) {
        TableViewer tabViewer = null;
        // if the focus is on view table then apply search on that table.
        if (this.tableView.isFocusControl()) {
            tabViewer = this.tableViewerViews;
        }
        if (tabViewer != null) {
            // if the search criteria ID, then sort the list of views
            // according to ID.
            if (searchCriteria == MetadataSearchCriteria.ID) {
                tabViewer.getTable().setSortColumn(
                        tabViewer.getTable().getColumn(3));
                tabViewer.getTable().setSortDirection(SWT.UP);
                tabViewer
                        .setSorter(new BatchActivateViewsTableSorter(3, SWT.UP));
            } else {
                // if the search criteria name, then sort the list of views
                // according to name.
                tabViewer.getTable().setSortColumn(
                        tabViewer.getTable().getColumn(4));
                tabViewer.getTable().setSortDirection(SWT.UP);
                tabViewer
                        .setSorter(new BatchActivateViewsTableSorter(4, SWT.UP));
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
            MessageDialog.openInformation(mediator.getSite().getShell(),
                "Component not found","The component you are trying to search is not found in the list.");
    
        }
    }   
    
    public ComponentType getComponentType() {
        if (tableView.isFocusControl()) {
            return ComponentType.View;
        }
        return null;
    }
    
    public Control getTableSection() {
        return sectionTable;
    }

    public SAFRViewActivationException getViewActivationState(int id) {
        for (BatchComponent comp : modelList) {
            BatchComponent vComp = (BatchComponent)comp;
            if (vComp.getComponent().getId().equals(id) &&
                (vComp.getResult().equals(ActivityResult.FAIL) ||
                vComp.getResult().equals(ActivityResult.WARNING))) {
                return (SAFRViewActivationException) vComp.getException();
            }
        }
        return null;        
    }

    public BatchComponent getSelectedView() {
        IStructuredSelection selection = (IStructuredSelection) tableViewerViews.getSelection();
        if (selection == null) {
            return null;
        } else {
            BatchComponent currModel = (BatchComponent) selection.getFirstElement();
            return currModel;
        }
    }    
    
}
    
    
