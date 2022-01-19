package com.ibm.safr.we.ui.editors.permission;

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
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EnvRole;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRAssociationList;
import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.associations.GroupComponentAssociation;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.SAFRQueryBean;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.dialogs.MetadataListDialog;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.utilities.SAFRLogger;

public class GroupPermEnvironments {

    private static final String SORT_CATEGORY = "Security";
    private static final String SORT_TABLE = "Environments";
    
    /**
     * This private class is used to provide the labels required to display in
     * the Environment Right Table table viewer.
     * 
     */
    private class PermEnvLabelProvider implements ITableLabelProvider {

        public PermEnvLabelProvider(CheckboxTableViewer viewer) {
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            if (!(element instanceof GroupEnvironmentAssociation))
                return null;

            GroupEnvironmentAssociation groupenvAssociation = (GroupEnvironmentAssociation) element;

            switch (columnIndex) {
            case 0:
                return groupenvAssociation.getAssociatedComponentIdString();
            case 1:
                return groupenvAssociation.getAssociatedComponentName();
            case 2:
                return groupenvAssociation.getEnvRole().getDesc();
            }
            return null;
        }

        public void addListener(ILabelProviderListener listener) {

        }

        public void dispose() {

        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {

        }
    }
    
    private class PermEnvRoleEditingSupport extends EditingSupport {

        private final TableViewer viewer;
        
        public PermEnvRoleEditingSupport(TableViewer viewer) {
            super(viewer);
            this.viewer = viewer;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            GroupEnvironmentAssociation ass = (GroupEnvironmentAssociation)element;            
            String[] roles = new String[3];
            roles[EnvRole.GUEST.ordinal()] = EnvRole.GUEST.getDesc();
            roles[EnvRole.DEVELOPER.ordinal()] = EnvRole.DEVELOPER.getDesc();
            roles[EnvRole.ADMIN.ordinal()] = EnvRole.ADMIN.getDesc();
            
            ComboBoxCellEditor editor = new ComboBoxCellEditor(viewer.getTable(), roles);
            editor.setValue(ass.getEnvRole().ordinal());
            return editor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            GroupEnvironmentAssociation ass = (GroupEnvironmentAssociation)element;
            return new Integer(ass.getEnvRole().ordinal());
        }

        @Override
        protected void setValue(Object element, Object value) {
            GroupEnvironmentAssociation ass = (GroupEnvironmentAssociation)element;   
            Integer val = (Integer)value;
            if (ass.getEnvRole().ordinal() == val) {
                return;
            }
            mediator.setDirty();
            if (val == EnvRole.ADMIN.ordinal()) {
                ass.setEnvRole(EnvRole.ADMIN);
            }
            else if (val == EnvRole.DEVELOPER.ordinal()) {
                ass.setEnvRole(EnvRole.DEVELOPER);
            }
            else {
                ass.setEnvRole(EnvRole.GUEST);                
            }
            viewer.update(element, null);   
            mediator.setToRoleDefault();
            mediator.setDirty();            
        }
        
    }
    
    class PermEnvTableSorter extends ViewerSorter {
        
        private int propertyIndex;
        private int dir;

        public PermEnvTableSorter(int propertyIndex, int dir) {
            super();
            this.propertyIndex = propertyIndex;
            this.dir = dir;
        }

        public int compare(Viewer viewer, Object obj1, Object obj2) {
            GroupEnvironmentAssociation groupEnvironmentAssociation1 = (GroupEnvironmentAssociation) obj1;
            GroupEnvironmentAssociation groupEnvironmentAssociation2 = (GroupEnvironmentAssociation) obj2;

            int rc = 0;

            switch (propertyIndex) {
            case 0:
                rc = groupEnvironmentAssociation1.getAssociatedComponentIdNum().compareTo(
                    groupEnvironmentAssociation2.getAssociatedComponentIdNum());
                break;
            case 1:
                rc = UIUtilities.compareStrings(
                    groupEnvironmentAssociation1.getAssociatedComponentName(), 
                    groupEnvironmentAssociation2.getAssociatedComponentName());
                break;
            case 2:
                rc = UIUtilities.compareEnums(groupEnvironmentAssociation1.getEnvRole(), 
                    groupEnvironmentAssociation2.getEnvRole());
                break;          
            default:
                rc = 0;
            }

            // If the direction of sorting was descending previously, reverse the
            // direction of sorting
            if (dir == SWT.DOWN) {
                rc = -rc;
            }
            return rc;
        }
    }
    
    private class PermEnvColumnSelection extends SelectionAdapter {
        private int colNumber;
        
        public PermEnvColumnSelection(int colNumber) {
            this.colNumber = colNumber;
        }

        public void widgetSelected(SelectionEvent e) {
            try {
                mediator.getSite().getShell().setCursor(
                    mediator.getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
                TableColumn sortColumn = tableViewerAssociatedEnvironment.getTable().getSortColumn();
                TableColumn currentColumn = (TableColumn) e.widget;
                int dir = tableViewerAssociatedEnvironment.getTable().getSortDirection();
                if (sortColumn == currentColumn) {
                    dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                } else {
                    tableViewerAssociatedEnvironment.getTable().setSortColumn(currentColumn);
                    if (currentColumn == tableViewerAssociatedEnvironment.getTable().getColumn(0)) {
                        dir = SWT.DOWN;
                    } else {
                        dir = SWT.UP;
                    }

                }

                tableViewerAssociatedEnvironment.getTable().setSortDirection(dir);
                tableViewerAssociatedEnvironment.setSorter(new PermEnvTableSorter(colNumber,dir));
                
                Order order = Order.ASCENDING;
                if (dir == SWT.DOWN) {
                    order = Order.DESCENDING;
                }
                SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE, colNumber, order);
                prefs.store();        
            } finally {
                mediator.getSite().getShell().setCursor(null);
            }
        }
    }
    
    private GroupPermMediator mediator;
    
    // UI Elements
    private Section section;
    private Table tableAssociatedEnvironment;
    private CheckboxTableViewer tableViewerAssociatedEnvironment;
    private Button buttonAdd;
    private Button buttonRem;
    private Button buttonDef;
    
    // Model
    SAFRList<GroupEnvironmentAssociation> envList = null;
    
    public GroupPermEnvironments() {
    }
    
    protected void setMediator(GroupPermMediator mediator) {
        this.mediator = mediator;
    }
 
    public Section getSection() {
        return section;
    }
    
    protected boolean isMouseHover() {
        Control control = Display.getCurrent().getFocusControl();
        return control == tableAssociatedEnvironment;
    }

    public GroupEnvironmentAssociation getCurrentEnvAss() {
        if (tableViewerAssociatedEnvironment.getTable().getSelectionCount() <= 0) {
            return null;
        }
        else {
            return (GroupEnvironmentAssociation) tableViewerAssociatedEnvironment
                .getTable().getSelection()[0].getData();
        }
    }
    
    protected void create() {
        
        section = mediator.getFormToolkit().createSection(mediator.getForm().getBody(), 
            Section.TITLE_BAR|Section.EXPANDED);
        FormData formData = new FormData();
        formData.top = new FormAttachment(mediator.getTopLeftSection(),0);
        formData.bottom = new FormAttachment(50,0);
        formData.left = new FormAttachment(0,10);
        formData.right = new FormAttachment(35,2);
        section.setLayoutData(formData);
        section.setText("Associated Environments");
        
        Composite sectionClient = mediator.getFormToolkit().createComposite(section);
        sectionClient.setLayout(new FormLayout());
        
        tableAssociatedEnvironment = new Table(sectionClient, SWT.CHECK |
            SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        tableAssociatedEnvironment.setHeaderVisible(true);
        tableAssociatedEnvironment.setLinesVisible(true);        
        tableAssociatedEnvironment.setData(SAFRLogger.USER,"Environments Table");
        tableViewerAssociatedEnvironment = new CheckboxTableViewer(tableAssociatedEnvironment);
        
        FormData tabData = new FormData();
        tabData.top = new FormAttachment(0,0);
        tabData.left = new FormAttachment(0,10);
        tabData.right = new FormAttachment(80,10);
        tabData.bottom = new FormAttachment(80,10);
        tableAssociatedEnvironment.setLayoutData(tabData);
        
        String[] columnHeadersEnvTable = { "ID", "Name", "Role"  };
        int[] columnWidthsEnvTable = { 90, 210, 100 };
        int lengthRightTable = columnWidthsEnvTable.length;
        for (int i = 0; i < lengthRightTable; i++) {
            TableViewerColumn columnEnvTable = new TableViewerColumn(
                    tableViewerAssociatedEnvironment, SWT.NONE);
            columnEnvTable.getColumn().setText(columnHeadersEnvTable[i]);
            columnEnvTable.getColumn().setToolTipText(columnHeadersEnvTable[i]);
            columnEnvTable.getColumn().setWidth(columnWidthsEnvTable[i]);
            columnEnvTable.getColumn().setResizable(true);
            if (i == 2) {
                columnEnvTable.setEditingSupport(new PermEnvRoleEditingSupport(tableViewerAssociatedEnvironment));
            }
            
            PermEnvColumnSelection colListener = new PermEnvColumnSelection(i);
            columnEnvTable.getColumn().addSelectionListener(colListener);            
        }
        tableViewerAssociatedEnvironment.setContentProvider(new IStructuredContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput,Object newInput) {
            }

            public void dispose() {
            }

            public Object[] getElements(Object inputElement) {
                @SuppressWarnings("unchecked")
                SAFRList<GroupEnvironmentAssociation> in = (SAFRList<GroupEnvironmentAssociation>)inputElement;
                if (in == null) {
                    return new String[0];
                }
                else {
                    return in.getActiveItems().toArray();
                }
            }
        });
        
        tableViewerAssociatedEnvironment.setLabelProvider(
            new PermEnvLabelProvider(tableViewerAssociatedEnvironment));

        tableViewerAssociatedEnvironment.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                Object[] items = tableViewerAssociatedEnvironment.getCheckedElements();
                if (items.length > 0) {
                    buttonRem.setEnabled(true);
                }
                else {
                    buttonRem.setEnabled(false);                    
                }
            }
        });
        
        tableViewerAssociatedEnvironment.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {
                    if (tableViewerAssociatedEnvironment.getTable().getSelectionCount() <= 0) {
                        mediator.setPopupEnabled(false);                        
                        return;
                    }
                    else {                        
                        mediator.setPopupEnabled(true);
                        mediator.onAssociationSelected();
                        refreshDefaultButtonState();
                    }
                }
        });
        
        tableViewerAssociatedEnvironment.setInput(envList);
        
        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE);
        if (prefs.load()) {
            int sortDir;
            if (prefs.getOrder() == Order.ASCENDING) {
                sortDir = SWT.UP;
            }
            else {
                sortDir = SWT.DOWN;
            }
            tableViewerAssociatedEnvironment.getTable().setSortColumn(
                tableViewerAssociatedEnvironment.getTable().getColumn(prefs.getColumn()));            
            tableViewerAssociatedEnvironment.getTable().setSortDirection(sortDir);
            tableViewerAssociatedEnvironment.setSorter(new PermEnvTableSorter(prefs.getColumn(),sortDir));            
        }
        else {
            tableViewerAssociatedEnvironment.getTable().setSortDirection(SWT.UP);
            tableViewerAssociatedEnvironment.setSorter(new PermEnvTableSorter(1,SWT.UP));            
            tableViewerAssociatedEnvironment.getTable().setSortColumn(
                tableViewerAssociatedEnvironment.getTable().getColumn(1));            
        }
        
        buttonAdd = mediator.getFormToolkit().createButton(sectionClient, "&Add", SWT.PUSH);
        buttonAdd.setEnabled(false);
        buttonAdd.setData(SAFRLogger.USER,"Add Environment");     
        FormData addData = new FormData();
        addData.top = new FormAttachment(0,0);
        addData.left = new FormAttachment(tableAssociatedEnvironment,10);
        addData.right = new FormAttachment(95,10);
        buttonAdd.setLayoutData(addData);

        buttonAdd.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                MetadataListDialog dialog = new MetadataListDialog(
                        mediator.getSite().getShell(), TreeItemId.GROUPENV,
                        mediator.getCurrentGroup(), UIUtilities.getCurrentEnvironmentID());
                dialog.open();

                List<SAFRQueryBean> returnList;
                if (dialog.getReturnCode() == Window.OK) {
                    
                    returnList = dialog.getReturnList();
                    for (int i = 0; i < returnList.size(); i++) {
                        EnvironmentQueryBean environment = (EnvironmentQueryBean) returnList.get(i);
                        GroupEnvironmentAssociation environmentAssociation = new GroupEnvironmentAssociation(
                            mediator.getCurrentGroup(), environment.getName(),environment.getId());
                        environmentAssociation.setEnvRole(EnvRole.GUEST);
                        envList.add(environmentAssociation);
                    }
                    if (!returnList.isEmpty()) {
                        mediator.setDirty();
                    }
                    tableViewerAssociatedEnvironment.setInput(envList);                    
                    tableViewerAssociatedEnvironment.refresh();
                }
            }
        });
        
        buttonRem = mediator.getFormToolkit().createButton(sectionClient, "Re&move", SWT.PUSH);
        buttonRem.setEnabled(false);
        buttonRem.setData(SAFRLogger.USER,"Remove Environment");     
        FormData remData = new FormData();
        remData.top = new FormAttachment(buttonAdd,5);
        remData.left = new FormAttachment(tableAssociatedEnvironment,10);
        remData.right = new FormAttachment(95,10);
        buttonRem.setLayoutData(remData);

        buttonRem.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                Object[] items = tableViewerAssociatedEnvironment.getCheckedElements();

                if (items.length > 0) {
                    for (Object item : items) {
                        GroupEnvironmentAssociation ass = (GroupEnvironmentAssociation) item;
                        // clear out edit rights for this environment association
                        try {
                            Map<ComponentType, SAFRAssociationList<GroupComponentAssociation>> editRights;
                            editRights = mediator.getCurrentGroup().getEnvironmentComponentRights(ass.getEnvironmentId());
                            for (Entry<ComponentType, SAFRAssociationList<GroupComponentAssociation>> entry : editRights.entrySet()) {
                                // remove all from association list
                                entry.getValue().removeAll();
                            }
                        } catch (SAFRException e1) {
                            UIUtilities.handleWEExceptions(e1,
                                "Error occurred while retrieving the associated components of the group.",
                                UIUtilities.titleStringDbException);
                        }
                        envList.remove(ass);
                    }
                    buttonRem.setEnabled(false);
                    buttonDef.setEnabled(false);
                    mediator.setDirty();
                    tableViewerAssociatedEnvironment.setInput(envList);                    
                    tableViewerAssociatedEnvironment.refresh();
                    mediator.onEnvironmentUnselected();
                }
            }
        });
        
        buttonDef = mediator.getFormToolkit().createButton(sectionClient, "&Default", SWT.PUSH);
        buttonDef.setEnabled(false);
        buttonDef.setData(SAFRLogger.USER,"Default Environment");     
        FormData defData = new FormData();
        defData.top = new FormAttachment(buttonRem,5);
        defData.left = new FormAttachment(tableAssociatedEnvironment,10);
        defData.right = new FormAttachment(95,10);
        buttonDef.setLayoutData(defData);
        
        buttonDef.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                mediator.setToRoleDefault();
                mediator.setDirty();   
                buttonDef.setEnabled(false);
            }
        });
        
        // Code for Context menu
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableAssociatedEnvironment);
        tableAssociatedEnvironment.setMenu(menu);
        mediator.getSite().registerContextMenu(menuManager, tableViewerAssociatedEnvironment);        
        mediator.setPopupEnabled(false);     
        
        section.setClient(sectionClient);
        
    }

    protected void onGroupSelected() {
        refreshEnvList();
        tableViewerAssociatedEnvironment.setInput(envList);
        tableViewerAssociatedEnvironment.refresh();   
        buttonAdd.setEnabled(true);
    }
    
    protected void refreshDefaultButtonState() {
        if (mediator.isDefault()) {
            buttonDef.setEnabled(false);
        }
        else {
            buttonDef.setEnabled(true);                            
        }        
    }
    
    private void refreshEnvList() {
        try {
            mediator.getSite().getShell().setCursor(
                mediator.getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
            if (mediator.getCurrentGroup() != null) {
                try {
                    envList =  mediator.getCurrentGroup().getAssociatedEnvironments();
                } catch (DAOException e) {
                    UIUtilities.handleWEExceptions(e,
                        "Error occurred while retrieving the associated environments of the group.",
                        UIUtilities.titleStringDbException);
                }
            }
            else {
                envList = null;
            }
        } finally {
            mediator.getSite().getShell().setCursor(null);
        }        
    }

}
