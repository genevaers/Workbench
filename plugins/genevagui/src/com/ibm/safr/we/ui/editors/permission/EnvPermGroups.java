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


import java.util.ArrayList;
import java.util.HashMap;
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
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRAssociationList;
import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.associations.GroupComponentAssociation;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.model.query.GroupQueryBean;
import com.ibm.safr.we.model.query.SAFRQueryBean;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.dialogs.MetadataListDialog;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.utilities.SAFRLogger;

public class EnvPermGroups {

    private static final String SORT_CATEGORY = "Security";
    private static final String SORT_TABLE = "Groups";
    
    /**
     * This private class is used to provide the labels required to display in
     * the Environment Right Table table viewer.
     * 
     */
    private class PermGroupLabelProvider implements ITableLabelProvider {

        public PermGroupLabelProvider(CheckboxTableViewer viewer) {
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
                return groupenvAssociation.getAssociatingComponentId().toString();
            case 1:
                return groupenvAssociation.getAssociatingComponentName();
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
    
    private class PermGroupEditingSupport extends EditingSupport {

        private final TableViewer viewer;
        
        public PermGroupEditingSupport(TableViewer viewer) {
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
    
    class PermGroupTableSorter extends ViewerSorter {
        private int propertyIndex;
        private int dir;

        public PermGroupTableSorter(int propertyIndex, int dir) {

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
                rc = groupEnvironmentAssociation1.getAssociatingComponentId().compareTo(
                    groupEnvironmentAssociation2.getAssociatingComponentId());
                break;
            case 1:
                rc = UIUtilities.compareStrings(
                    groupEnvironmentAssociation1.getAssociatingComponentName(),
                    groupEnvironmentAssociation2.getAssociatingComponentName());
                break;
            case 2:
                rc = UIUtilities.compareEnums(groupEnvironmentAssociation1.getEnvRole(), 
                    groupEnvironmentAssociation2.getEnvRole());
                break;                          
            default:
                rc = 0;
            }

            // If the direction of sorting was descending previously, reverse the direction of sorting
            if (dir == SWT.DOWN) {
                rc = -rc;
            }
            return rc;
        }
    }    
    
    private class PermGroupColumnSelection extends SelectionAdapter {
        private int colNumber;
        
        public PermGroupColumnSelection(int colNumber) {
            this.colNumber = colNumber;
        }

        public void widgetSelected(SelectionEvent e) {
            try {
                mediator.getSite().getShell().setCursor(
                    mediator.getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
                TableColumn sortColumn = tableViewerAssociatedGroup.getTable().getSortColumn();
                TableColumn currentColumn = (TableColumn) e.widget;
                int dir = tableViewerAssociatedGroup.getTable().getSortDirection();
                if (sortColumn == currentColumn) {
                    dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                } else {
                    tableViewerAssociatedGroup.getTable().setSortColumn(currentColumn);
                    if (currentColumn == tableViewerAssociatedGroup.getTable().getColumn(0)) {
                        dir = SWT.DOWN;
                    } else {
                        dir = SWT.UP;
                    }

                }

                tableViewerAssociatedGroup.getTable().setSortDirection(dir);
                tableViewerAssociatedGroup.setSorter(new PermGroupTableSorter(colNumber,dir));
                
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
    
    private EnvPermMediator mediator;
    
    // UI Elements
    private Section section;
    private Table tableAssociatedGroup;
    private CheckboxTableViewer tableViewerAssociatedGroup;
    private Button buttonAdd;
    private Button buttonRem;
    private Button buttonDef;
    
    // Model
    private SAFRList<GroupEnvironmentAssociation> groupList = null;
    private Map<Integer,Group> groupMap = new HashMap<Integer,Group>();
    
    public EnvPermGroups() {
    }
    
    protected void setMediator(EnvPermMediator mediator) {
        this.mediator = mediator;
    }
 
    public Section getSection() {
        return section;
    }

    public GroupEnvironmentAssociation getCurrentEnvAss() {
        if (tableViewerAssociatedGroup.getTable().getSelectionCount() <= 0) {
            return null;
        }
        else {
            return (GroupEnvironmentAssociation) tableViewerAssociatedGroup
                .getTable().getSelection()[0].getData();
        }
    }
    
    public Group getCurrentGroup() {
        if (tableViewerAssociatedGroup.getTable().getSelectionCount() <= 0) {
            return null;
        }
        else {
            GroupEnvironmentAssociation ass =  (GroupEnvironmentAssociation) tableViewerAssociatedGroup
                .getTable().getSelection()[0].getData();
            if (ass != null) {
                return getGroup(ass.getAssociatingComponentId());
            }
            else {
                return null;
            }
        }                
    }
    
    private Group getGroup(Integer id) {
        if (groupMap.containsKey(id)) {
            return groupMap.get(id);
        }
        else {
            Group group = null;
            try {
                group = SAFRApplication.getSAFRFactory().getGroup(id);
                groupMap.put(group.getId(), group);
            } catch (SAFRException e) {
                UIUtilities.handleWEExceptions(e,
                    "Error occurred while retrieving the group.",
                    UIUtilities.titleStringDbException);
            }                    
            return group;
        }
    }
    
    public List<Group> getGroupList() {
        List<Group> list = new ArrayList<Group>(groupMap.values());
        return list;
    }
    
    protected boolean isMouseHover() {
        Control control = Display.getCurrent().getFocusControl();
        return control == tableAssociatedGroup;
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
        section.setText("Associated Groups");
        
        Composite sectionClient = mediator.getFormToolkit().createComposite(section);
        sectionClient.setLayout(new FormLayout());
        
        tableAssociatedGroup = new Table(sectionClient, SWT.CHECK |
            SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        tableAssociatedGroup.setHeaderVisible(true);
        tableAssociatedGroup.setLinesVisible(true);        
        tableAssociatedGroup.setData(SAFRLogger.USER,"Groups Table");
        tableViewerAssociatedGroup = new CheckboxTableViewer(tableAssociatedGroup);
        
        FormData tabData = new FormData();
        tabData.top = new FormAttachment(0,0);
        tabData.left = new FormAttachment(0,10);
        tabData.right = new FormAttachment(80,10);
        tabData.bottom = new FormAttachment(80,10);
        tableAssociatedGroup.setLayoutData(tabData);
        
        String[] columnHeadersEnvTable = { "ID", "Name", "Role"  };
        int[] columnWidthsEnvTable = { 90, 210, 100 };
        int lengthRightTable = columnWidthsEnvTable.length;
        for (int i = 0; i < lengthRightTable; i++) {
            TableViewerColumn columnEnvTable = new TableViewerColumn(
                    tableViewerAssociatedGroup, SWT.NONE);
            columnEnvTable.getColumn().setText(columnHeadersEnvTable[i]);
            columnEnvTable.getColumn().setToolTipText(columnHeadersEnvTable[i]);
            columnEnvTable.getColumn().setWidth(columnWidthsEnvTable[i]);
            columnEnvTable.getColumn().setResizable(true);
            if (i == 2) {
                columnEnvTable.setEditingSupport(new PermGroupEditingSupport(tableViewerAssociatedGroup));
            }
            PermGroupColumnSelection colListener = new PermGroupColumnSelection(i);
            columnEnvTable.getColumn().addSelectionListener(colListener);            

        }
        tableViewerAssociatedGroup.setContentProvider(new IStructuredContentProvider() {

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
        
        tableViewerAssociatedGroup.setLabelProvider(
            new PermGroupLabelProvider(tableViewerAssociatedGroup));

        tableViewerAssociatedGroup.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                Object[] items = tableViewerAssociatedGroup.getCheckedElements();

                if (items.length > 0) {
                    buttonRem.setEnabled(true);
                }
                else {
                    buttonRem.setEnabled(false);                    
                }
                
            }
            
        });
        
        tableViewerAssociatedGroup.addSelectionChangedListener(new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    if (tableViewerAssociatedGroup.getTable().getSelectionCount() <= 0) {
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
        
        tableViewerAssociatedGroup.setInput(groupList);
        
        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE);
        if (prefs.load()) {
            int sortDir;
            if (prefs.getOrder() == Order.ASCENDING) {
                sortDir = SWT.UP;
            }
            else {
                sortDir = SWT.DOWN;
            }
            tableViewerAssociatedGroup.getTable().setSortColumn(
                tableViewerAssociatedGroup.getTable().getColumn(prefs.getColumn()));            
            tableViewerAssociatedGroup.getTable().setSortDirection(sortDir);
            tableViewerAssociatedGroup.setSorter(new PermGroupTableSorter(prefs.getColumn(),sortDir));            
        }
        else {
            tableViewerAssociatedGroup.getTable().setSortDirection(SWT.UP);
            tableViewerAssociatedGroup.setSorter(new PermGroupTableSorter(1,SWT.UP));            
            tableViewerAssociatedGroup.getTable().setSortColumn(
                tableViewerAssociatedGroup.getTable().getColumn(1));            
        }
                
        buttonAdd = mediator.getFormToolkit().createButton(sectionClient, "&Add", SWT.PUSH);
        buttonAdd.setEnabled(false);
        buttonAdd.setData(SAFRLogger.USER,"Add Group");     
        FormData addData = new FormData();
        addData.top = new FormAttachment(0,0);
        addData.left = new FormAttachment(tableAssociatedGroup,10);
        addData.right = new FormAttachment(95,10);
        buttonAdd.setLayoutData(addData);

        buttonAdd.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            
                MetadataListDialog dialog = new MetadataListDialog(
                        mediator.getSite().getShell(), TreeItemId.ENVGROUP,
                        mediator.getCurrentEnvironment(), UIUtilities.getCurrentEnvironmentID());
                dialog.open();
    
                List<SAFRQueryBean> returnList;
                if (dialog.getReturnCode() == Window.OK) {
                    returnList = dialog.getReturnList();
                    Environment currentEnv = mediator.getCurrentEnvironment();

                    for (int i = 0; i < returnList.size(); i++) {
                        GroupQueryBean group = (GroupQueryBean) returnList.get(i);
                        GroupEnvironmentAssociation grpAssociation = 
                            new GroupEnvironmentAssociation(group, currentEnv.getName(), currentEnv.getId());
                        grpAssociation.setEnvRole(EnvRole.GUEST);
                        groupList.add(grpAssociation);
                    }
                    if (!returnList.isEmpty()) {
                        mediator.setDirty();
                    }
                    tableViewerAssociatedGroup.setInput(groupList);
                    tableViewerAssociatedGroup.refresh();
                }
            }
        });
        
        
        buttonRem = mediator.getFormToolkit().createButton(sectionClient, "Re&move", SWT.PUSH);
        buttonRem.setEnabled(false);
        buttonRem.setData(SAFRLogger.USER,"Remove Group");     
        FormData remData = new FormData();
        remData.top = new FormAttachment(buttonAdd,5);
        remData.left = new FormAttachment(tableAssociatedGroup,10);
        remData.right = new FormAttachment(95,10);
        buttonRem.setLayoutData(remData);

        buttonRem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Object[] items = tableViewerAssociatedGroup.getCheckedElements();
                
                if (items.length > 0) {
                    for (Object item : items) {
                        GroupEnvironmentAssociation ass = (GroupEnvironmentAssociation) item;
                        // clear out edit rights for this association
                        try {
                            Map<ComponentType, SAFRAssociationList<GroupComponentAssociation>> editRights;
                            Group group = getGroup(ass.getAssociatingComponentId());
                            editRights = group.getEnvironmentComponentRights(ass.getEnvironmentId());
                            for (Entry<ComponentType, SAFRAssociationList<GroupComponentAssociation>> entry : editRights.entrySet()) {
                                // remove all from association list
                                entry.getValue().removeAll();
                            }
                        } catch (SAFRException e1) {
                            UIUtilities.handleWEExceptions(e1,
                                "Error occurred while retrieving the associated components of the group.",
                                UIUtilities.titleStringDbException);
                        }                        
                        groupList.remove(ass);
                    }
                    buttonRem.setEnabled(false);
                    buttonDef.setEnabled(false);
                    mediator.setDirty();
                    tableViewerAssociatedGroup.setInput(groupList);
                    tableViewerAssociatedGroup.refresh();
                    mediator.onGroupUnselected();                        
                }
            }
        });
        
        buttonDef = mediator.getFormToolkit().createButton(sectionClient, "&Default", SWT.PUSH);
        buttonDef.setEnabled(false);
        buttonDef.setData(SAFRLogger.USER,"Default Group");     
        FormData defData = new FormData();
        defData.top = new FormAttachment(buttonRem,5);
        defData.left = new FormAttachment(tableAssociatedGroup,10);
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
        Menu menu = menuManager.createContextMenu(tableAssociatedGroup);
        tableAssociatedGroup.setMenu(menu);
        mediator.getSite().registerContextMenu(menuManager, tableViewerAssociatedGroup);        
        mediator.setPopupEnabled(false);     
        
        section.setClient(sectionClient);        
    }

    protected void onEnvironmentSelected() {
        groupMap.clear();
        refreshGroupList();
        tableViewerAssociatedGroup.setInput(groupList);
        tableViewerAssociatedGroup.refresh();   
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
    
    private void refreshGroupList() {
        try {
            mediator.getSite().getShell().setCursor(
                mediator.getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
            if (mediator.getCurrentEnvironment() != null) {
                try {
                    groupList = mediator.getCurrentEnvironment().getAssociatedGroups();
                    
                } catch (DAOException e) {
                    UIUtilities.handleWEExceptions(e,
                        "Error occurred while retrieving the associated groups of the environment.",
                        UIUtilities.titleStringDbException);
                }
            }
            else {
                groupList = null;
            }
        } finally {
            mediator.getSite().getShell().setCursor(null);
        }        
    }
}
