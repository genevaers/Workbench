package com.ibm.safr.we.ui.editors.find;

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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.model.logic.LogicTextParser;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.utilities.FindReplaceComponent;
import com.ibm.safr.we.model.utilities.FindReplaceText;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.editors.logic.LogicTextLineStyler;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class FindReplaceResults {

    public static String[] columnHeaders = { "Replace", "View ID", "View Name",
        "Found In", "Column", "Logic Text" };
    
    private static final String SORT_CATEGORY = "FindReplace";
    private static final String SORT_TABLE = "Results";
    
    private class SearchResultTableSorter extends ViewerSorter {

        public SearchResultTableSorter() {
            super();
        }

        public int compare(Viewer viewer, Object obj1, Object obj2) {
            FindReplaceComponent g1 = (FindReplaceComponent) obj1;
            FindReplaceComponent g2 = (FindReplaceComponent) obj2;
            int rc = 0;

            switch (sortCol) {

            case 1:
                rc = UIUtilities
                        .compareIntegers(g1.getViewId(), g2.getViewId());
                break;
            case 2:
                rc = UIUtilities.compareStrings(g1.getViewName(), g2
                        .getViewName());
                break;
            case 3:
                rc = UIUtilities.compareEnums(g1.getLogicTextType(), g2
                        .getLogicTextType());
                break;
            case 4:
                rc = UIUtilities.compareIntegers(g1.getColumnId(), g2.getColumnId());
                break;
            case 5:
                rc = UIUtilities.compareStrings(g1.getLogicText(), g2
                        .getLogicText());
                break;
            default:
                rc = 0;
            }

            // If the direction of sorting was descending previously,
            // reverse the
            // direction of sorting
            if (sortDir == SWT.DOWN) {
                rc = -rc;
            }
            return rc;
        }
    }
    
    /**
     * Private inner class for displaying data in the Lookup paths table.
     */
    private class FindReplaceResultTableLabelProvider extends
            ColumnLabelProvider {

        private int columnIndex;

        public FindReplaceResultTableLabelProvider(int columnIndex) {
            super();
            this.columnIndex = columnIndex;
        }

        @Override
        public String getText(Object element) {
            if (!(element instanceof FindReplaceComponent))
                return null;

            FindReplaceComponent findReplaceComponent = (FindReplaceComponent) element;
            switch (columnIndex) {
            case 1:

                return Integer.toString(findReplaceComponent.getViewId());

            case 2:

                return findReplaceComponent.getViewName();
            case 3:
                if (findReplaceComponent.getLogicTextType() == LogicTextType.Extract_Column_Assignment) {
                    return "Extract-Phase Column Logic";
                } else if (findReplaceComponent.getLogicTextType() == LogicTextType.Extract_Record_Output) {
                    return "Extract-Phase Record Logic";
                } else if (findReplaceComponent.getLogicTextType() == LogicTextType.Extract_Record_Filter) {
                    return "Extract-Phase Record Filter";
                } else if (findReplaceComponent.getLogicTextType() == LogicTextType.Format_Column_Calculation) {
                    return "Format-Phase Column Logic";
                } else if (findReplaceComponent.getLogicTextType() == LogicTextType.Format_Record_Filter) {
                    return "Format-Phase Record Filter";
                }
            case 4:
                if (findReplaceComponent.getColumnId() != null 
                    && findReplaceComponent.getColumnId() > 0) {
                    return Integer.toString(findReplaceComponent.getColumnId());                    
                }
                else {
                    return "N/A";
                }
            case 5:
                return findReplaceComponent.getLogicText();

            }
            return null;
        }
        
        @Override
        public Color getForeground(Object element) {
            if (!(element instanceof FindReplaceComponent))
                return super.getForeground(element);

            FindReplaceComponent comp = (FindReplaceComponent) element;
            if (comp.getRights().equals(EditRights.ReadModify) || 
                comp.getRights().equals(EditRights.ReadModifyDelete)) {
                return super.getForeground(element);                
            }
            else {
                return PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);                
            }
        }
        
    }

    private class ColumnSelectionListenerFindReplaceTable extends
            SelectionAdapter {
        private int colNumber;

        ColumnSelectionListenerFindReplaceTable(int colNumber) {
            this.colNumber = colNumber;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                mediator.waitCursor();
                TableColumn sortColumn = tableViewerSearchResult.getTable().getSortColumn();
                TableColumn currentColumn = (TableColumn) e.widget;
                sortDir = tableViewerSearchResult.getTable().getSortDirection();
                if (sortColumn == currentColumn) {
                    sortDir = sortDir == SWT.UP ? SWT.DOWN : SWT.UP;
                } else {
                    tableViewerSearchResult.getTable().setSortColumn(currentColumn);
                    if (currentColumn == tableViewerSearchResult.getTable().getColumn(0)) {
                        sortDir = SWT.DOWN;
                    } else {
                        sortDir = SWT.UP;
                    }

                }
                tableViewerSearchResult.getTable().setSortDirection(sortDir);
                sortCol = colNumber;
                
                tableViewerSearchResult.refresh();
                
                Order order = Order.ASCENDING;
                if (sortDir == SWT.DOWN) {
                    order = Order.DESCENDING;
                }
                SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE, 
                    (Integer)sortCol, order);
                prefs.store();                              
                
            } finally {
                mediator.normalCursor();
            }
        }
    }

    
    private FindReplaceMediator mediator;
    
    // Data
    private List<FindReplaceComponent> findReplaceComponentList = new ArrayList<FindReplaceComponent>();
    private String logicText;
    
    // UI
    private SAFRGUIToolkit safrGuiToolkit;        
    private Section sectionSearchResults;
    private CheckboxTableViewer tableViewerSearchResult;
    private Table tableSearchResults;    
    private int sortCol=2;
    private int sortDir=SWT.UP;
    private Button buttonSelectAll;
    private Button buttonDeSelectAll;
    private StyledText text = null;    
    private LogicTextLineStyler lineStyler;
    
    protected FindReplaceResults() {
    }

    public void setMediator(FindReplaceMediator mediator) {
        this.mediator = mediator;
        this.safrGuiToolkit = mediator.getToolkit();
    }
    
    protected void create(Composite body) {
        sectionSearchResults = safrGuiToolkit.createSection(body,
                Section.TITLE_BAR | Section.DESCRIPTION, "Search Results");
        
        FormData dataSectionSearchResults = new FormData();
        dataSectionSearchResults.top = new FormAttachment(0, 20);
        dataSectionSearchResults.left = new FormAttachment(
            mediator.getCriteriaSection(), 30);
        dataSectionSearchResults.bottom = new FormAttachment(100, -5);
        dataSectionSearchResults.right = new FormAttachment(95,0);
        sectionSearchResults.setLayoutData(dataSectionSearchResults);

        Composite compositeSearchResults = safrGuiToolkit.createComposite(
                sectionSearchResults, SWT.NONE);
        compositeSearchResults.setLayout(new FormLayout());
        compositeSearchResults.setLayoutData(new FormData());

        Composite compositeResultsTable = safrGuiToolkit.createComposite(
            compositeSearchResults, SWT.NONE);
        FormData dataTable = new FormData();
        dataTable.left = new FormAttachment(0, 0);
        dataTable.right = new FormAttachment(100, -5);
        dataTable.top = new FormAttachment(0, 10);
        dataTable.bottom = new FormAttachment(80, -50);
        dataTable.height = 0;
        compositeResultsTable.setLayoutData(dataTable);
        
        tableSearchResults = safrGuiToolkit.createTable(compositeResultsTable,
                SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER, false);
        tableSearchResults.setData(SAFRLogger.USER,"Search Results");
        tableViewerSearchResult = new CheckboxTableViewer(tableSearchResults);
        tableSearchResults.setHeaderVisible(true);
        tableSearchResults.setLinesVisible(true);

        int iCounter = 0;
        int[] columnWidths = { 80, 80, 100, 85, 80, 120 };
        int length = columnWidths.length;

        TableColumnLayout columnLayout = new TableColumnLayout();
        
        for (iCounter = 0; iCounter < length; iCounter++) {
            TableViewerColumn column = new TableViewerColumn(
                    tableViewerSearchResult, SWT.NONE);
            column.getColumn().setText(columnHeaders[iCounter]);
            column.getColumn().setToolTipText(columnHeaders[iCounter]);
            column.getColumn().setWidth(columnWidths[iCounter]);
            if (iCounter == length-1) {
                columnLayout.setColumnData(column.getColumn(), new ColumnWeightData(columnWidths[iCounter], 100));
            }
            else {
                columnLayout.setColumnData(column.getColumn(), new ColumnPixelData(columnWidths[iCounter]));                
            }
            column.getColumn().setResizable(true);
            column.setLabelProvider(new FindReplaceResultTableLabelProvider(iCounter));
            ColumnSelectionListenerFindReplaceTable colListenerFindReplaceTable = 
                new ColumnSelectionListenerFindReplaceTable(iCounter);
            column.getColumn().addSelectionListener(colListenerFindReplaceTable);

        }
        compositeResultsTable.setLayout(columnLayout);

        tableViewerSearchResult.setContentProvider(new IStructuredContentProvider() {

                    public Object[] getElements(Object inputElement) {

                        if (mediator.getCurrentEnv() != null) {
                            return findReplaceComponentList.toArray();
                        } else {
                            return new String[0];
                        }
                    }

                    public void dispose() {
                    }

                    public void inputChanged(Viewer viewer, Object oldInput,
                            Object newInput) {
                    }

                });

        tableViewerSearchResult
                .addCheckStateListener(new ICheckStateListener() {

                    public void checkStateChanged(CheckStateChangedEvent event) {
                        FindReplaceComponent component = (FindReplaceComponent) event.getElement();
                        if (component.getRights().equals(EditRights.ReadModify) ||
                            component.getRights().equals(EditRights.ReadModifyDelete)) {
                            if (event.getChecked()) {
                                component.setSelected(true);
                            } else {
                                component.setSelected(false);
                            }
                        }
                        else {
                            tableViewerSearchResult.setChecked(component, false);                           
                        }
                        if (tableViewerSearchResult.getCheckedElements().length > 0 &&
                            !mediator.isReplaceTextEmpty()) {
                            mediator.enableReplace();
                        } else {
                            mediator.disableReplace();
                        }                       
                    }
                });
        tableViewerSearchResult.addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        FindReplaceComponent findReplaceComponent = (FindReplaceComponent) ((IStructuredSelection) event
                                .getSelection()).getFirstElement();
                        if (findReplaceComponent != null) {
                            logicText = findReplaceComponent.getLogicText();
                            if (lineStyler != null) {
                                text.removeLineStyleListener(lineStyler);
                            }
                            LogicTextParser parser = LogicTextParser.generateParser(
                                findReplaceComponent.getLogicTextType());
                            parser.parse(logicText);
                            lineStyler = new LogicTextLineStyler(parser);
                            text.addLineStyleListener(lineStyler);
                            text.setText(logicText);
                        }
                        Object[] checkedElementList = tableViewerSearchResult
                                .getCheckedElements();
                        if (checkedElementList.length > 0
                                && (!mediator.isReplaceTextEmpty())) {
                            mediator.enableReplace();
                        } else {
                            mediator.disableReplace();
                        }                       
                        
                        // handle popup enablement
                        EnvironmentQueryBean ebean = mediator.getComboEnv();                    
                        if (ebean.getId() == UIUtilities.getCurrentEnvironmentID()) {                       
                            if (findReplaceComponent != null) {
                                mediator.setPopupEnabled(true);
                            }
                            else {
                                mediator.setPopupEnabled(false);                         
                            }
                        }
                        else {
                            mediator.setPopupEnabled(false);                                                     
                        }
                    }
                });
        
        tableViewerSearchResult.getTable().addListener(SWT.Selection, new Listener() {
              public void handleEvent(Event event) {
                  if (event.detail == SWT.CHECK) {
                      int idx = tableViewerSearchResult.getTable().indexOf((TableItem)event.item);                        
                      FindReplaceComponent comp = (FindReplaceComponent)tableViewerSearchResult.getElementAt(idx);
                      if (comp.getRights().equals(EditRights.Read)) {
                          TableItem item = (TableItem) event.item;
                          if (item.getChecked()) {
                              item.setChecked(false);
                          }
                      }
                  }
              }
            });     
        tableViewerSearchResult.setInput(1);

        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE);
        if (prefs.load()) {
            sortCol = prefs.getColumn();
            if (prefs.getOrder() == Order.ASCENDING) {
                sortDir = SWT.UP;
            }
            else {
                sortDir = SWT.DOWN;
            }                   
        }
        tableViewerSearchResult.getTable().setSortColumn(
            tableViewerSearchResult.getTable().getColumn(sortCol));
        tableViewerSearchResult.getTable().setSortDirection(sortDir);
        tableViewerSearchResult.setSorter(new SearchResultTableSorter());
        
        text = new StyledText(compositeSearchResults, SWT.MULTI | SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
        FormData dataText = new FormData();
        dataText.top = new FormAttachment(compositeResultsTable, 2);
        dataText.left = new FormAttachment(0, 0);
        dataText.right = new FormAttachment(100, -5);
        dataText.height = 85;
        text.setLayoutData(dataText);

        buttonSelectAll = safrGuiToolkit.createButton(compositeSearchResults,
                SWT.PUSH, "Select &All");
        buttonSelectAll.setData(SAFRLogger.USER,"Select All");        
        FormData databuttonSelectAll = new FormData();
        databuttonSelectAll.top = new FormAttachment(text, 10);
        databuttonSelectAll.width = 90;
        buttonSelectAll.setLayoutData(databuttonSelectAll);
        buttonSelectAll.setEnabled(false);
        buttonSelectAll.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                for (TableItem item : tableViewerSearchResult.getTable().getItems()) {
                    
                    int idx = tableViewerSearchResult.getTable().indexOf(item);
                    FindReplaceComponent comp = (FindReplaceComponent)tableViewerSearchResult.getElementAt(idx);
                    if (comp.getRights().equals(EditRights.ReadModify) || comp.getRights().equals(EditRights.ReadModifyDelete)) {                   
                        item.setChecked(true);
                    }
                }
                if ((!mediator.isReplaceTextEmpty())
                        && (tableViewerSearchResult.getCheckedElements().length > 0)) {
                    mediator.enableReplace();
                }
            }
        });

        buttonDeSelectAll = safrGuiToolkit.createButton(compositeSearchResults,
                SWT.PUSH, "&Deselect All");
        buttonDeSelectAll.setData(SAFRLogger.USER,"Deselect All");                
        FormData databuttonDeSelectAll = new FormData();
        databuttonDeSelectAll.top = new FormAttachment(text, 10);
        databuttonDeSelectAll.left = new FormAttachment(buttonSelectAll, 5);
        databuttonDeSelectAll.width = 90;
        buttonDeSelectAll.setLayoutData(databuttonDeSelectAll);
        buttonDeSelectAll.setEnabled(false);
        buttonDeSelectAll.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                tableViewerSearchResult.setAllChecked(false);
                mediator.disableReplace();
            }

        });

        sectionSearchResults.setClient(compositeSearchResults);
        
        // Code for Context menu
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableSearchResults);
        tableSearchResults.setMenu(menu);
        mediator.getSite().registerContextMenu(menuManager, tableViewerSearchResult);        
        mediator.setPopupEnabled(false);     
    }


    protected void clearResults() {
        if (!findReplaceComponentList.isEmpty()) {
            findReplaceComponentList.clear();
            tableViewerSearchResult.refresh();
        }
        sectionSearchResults.setDescription("");
        text.setText("");
    }

    protected Object[] getCheckedResults() {
        return tableViewerSearchResult.getCheckedElements();
    }
    
    protected void disableResults() {
        buttonDeSelectAll.setEnabled(false);
        buttonSelectAll.setEnabled(false);
    }    
    
    protected void setResults(List<FindReplaceComponent> results) {
        findReplaceComponentList = results;
        if (findReplaceComponentList == null || 
            findReplaceComponentList.isEmpty()) {
            sectionSearchResults.setDescription("No results found");
        }        
    }

    protected void refreshResults() {
        tableViewerSearchResult.refresh();
        if (tableViewerSearchResult.getTable().getItemCount() > 0) {
            buttonSelectAll.setEnabled(true);
            buttonDeSelectAll.setEnabled(true);
        } else {
            buttonSelectAll.setEnabled(false);
            buttonDeSelectAll.setEnabled(false);
        }        
    }

    protected boolean isResultsSelected() {
        Object[] checkedElements = tableViewerSearchResult
            .getCheckedElements();
        return checkedElements.length > 0;
    }

    protected FindReplaceComponent getCurrentSelection() {
        return (FindReplaceComponent)((StructuredSelection)tableViewerSearchResult.getSelection()).getFirstElement();
    }    
        
    protected Control getControlFromProperty(Object property) {
        if (property == FindReplaceText.Property.COMPONENTNAMESTOREPLACE) {
            return tableSearchResults;
        }           
        else {
            return null;
        }        
    }
    
}
