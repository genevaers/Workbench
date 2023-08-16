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
import java.util.logging.Logger;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
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
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRAssociationList;
import com.ibm.safr.we.model.associations.GroupComponentAssociation;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class PermEditRights {

    private static final String SORT_CATEGORY = "Security";
    private static final String SORT_TABLE = "Rights";
    
    public enum RightsColumn {
        READ(2),
        MODIFY(3),
        DELETE(4);
        
        private int colNum;
        
        private RightsColumn(int colNum) {
            this.colNum = colNum;
        }
        
        public int getColNum() {
            return colNum;
        }        
    }
    
    private class EditRightsSelectionListener implements ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            GroupEnvironmentAssociation gass = mediator.getCurrentEnvAss();              
            if (gass != null && gass.getAssociatedComponentIdNum().equals(UIUtilities.getCurrentEnvironmentID())) {                                      
                GroupComponentAssociation node = (GroupComponentAssociation)((StructuredSelection) event
                        .getSelection()).getFirstElement();
                if (node != null) {
                    mediator.setPopupEnabled(true);
                }
                else {
                    mediator.setPopupEnabled(false);                        
                }
            }
            else {
                mediator.setPopupEnabled(false);                                         
            }            
            setTabState(tabPermissions.getSelection());
        }
        
    }
    
    private class EditRightsContentProvider implements IStructuredContentProvider {

        ComponentType type;
        
        public EditRightsContentProvider(ComponentType type) {
            this.type = type;
        }
        
        public void inputChanged(Viewer viewer, Object oldInput,Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object inputElement) {
                       
            @SuppressWarnings("unchecked")
            Map<ComponentType, SAFRAssociationList<GroupComponentAssociation>> in = 
                (Map<ComponentType, SAFRAssociationList<GroupComponentAssociation>>)inputElement;
            try {
                mediator.getSite().getShell().setCursor(
                    mediator.getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
                
                if (in == null) {
                    return new String[0];
                }
                
                // form edit rights into a map
                Map<Integer,GroupComponentAssociation> compAsses = new HashMap<Integer,GroupComponentAssociation>();
                for (GroupComponentAssociation compAss : in.get(type).getActiveItems()) {
                    compAsses.put(compAss.getAssociatedComponentIdNum(), compAss);
                }
    
                // query all beans of the component type
                List<EnvironmentalQueryBean> beans = new ArrayList<EnvironmentalQueryBean>();
                switch (type) {
                case UserExitRoutine:
                    for (UserExitRoutineQueryBean uBean : SAFRQuery.queryAllUserExitRoutines(
                        mediator.getCurrentEnvAss().getEnvironmentId(), SortType.SORT_BY_ID)) {
                        beans.add(uBean);
                    }
                    break;
                case PhysicalFile:
                    for (PhysicalFileQueryBean pBean : SAFRQuery.queryAllPhysicalFiles(
                        mediator.getCurrentEnvAss().getEnvironmentId(), SortType.SORT_BY_ID)) {
                        beans.add(pBean);
                    }
                    break;
                case LogicalFile:
                    for (LogicalFileQueryBean pBean : SAFRQuery.queryAllLogicalFiles(
                        mediator.getCurrentEnvAss().getEnvironmentId(), SortType.SORT_BY_ID)) {
                        beans.add(pBean);
                    }
                    break;
                case LogicalRecord:
                    for (LogicalRecordQueryBean pBean : SAFRQuery.queryAllLogicalRecords(
                        mediator.getCurrentEnvAss().getEnvironmentId(), SortType.SORT_BY_ID)) {
                        beans.add(pBean);
                    }
                    break;
                case LookupPath:
                    for (LookupQueryBean pBean : SAFRQuery.queryAllLookups(
                        mediator.getCurrentEnvAss().getEnvironmentId(), SortType.SORT_BY_ID)) {
                        beans.add(pBean);
                    }
                    break;
                case View:
                    for (ViewQueryBean pBean : SAFRQuery.queryAllViews(
                        mediator.getCurrentEnvAss().getEnvironmentId(), SortType.SORT_BY_ID)) {
                        beans.add(pBean);
                    }
                    break;
                case ViewFolder:
                    for (ViewFolderQueryBean pBean : SAFRQuery.queryAllViewFolders(
                        mediator.getCurrentEnvAss().getEnvironmentId(), SortType.SORT_BY_ID)) {
                        if (pBean.getId() != 0) {
                            beans.add(pBean);
                        }
                    }
                    break;
                default:
                    break;
                }
                // merge into one array of GroupComponentAssociations
                List<GroupComponentAssociation> result = new ArrayList<GroupComponentAssociation>();
                for (EnvironmentalQueryBean bean : beans) {
                    // associations from the database are used as is
                    if (compAsses.containsKey(bean.getId())) {
                        result.add(compAsses.get(bean.getId()));
                    }
                    // else it must be a default setting 
                    else {
                        GroupComponentAssociation compAss = new GroupComponentAssociation(mediator.getCurrentGroup(), 
                            bean.getName(), bean.getId(), bean.getEnvironmentId());
                        EditRights right = mediator.getCurrentEnvAss().getEnvRole().getRights().getComponentRight(type);
                        compAss.setRights(right);
                        result.add(compAss);
                    }
                }
                return result.toArray();
                
            } catch (DAOException e) {
                UIUtilities.handleWEExceptions(e);
            } finally {
                mediator.getSite().getShell().setCursor(null);                
            }
            return new String[0];
        }
    }

    private class EditRightsLabelProvider extends CellLabelProvider {

        @Override
        public void update(ViewerCell cell) {
            if (!(cell.getElement() instanceof GroupComponentAssociation))
                return;
        
            GroupComponentAssociation groupComponentAssociation = (GroupComponentAssociation) cell.getElement();
        
            boolean isDefault = isDefault(groupComponentAssociation);
            Color background; 
            if (isDefault) {
                background = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
            }
            else {
                background = Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
            }
            switch (cell.getColumnIndex()) {
            case 0:
                String idStr = Integer.toString(groupComponentAssociation.getAssociatedComponentIdNum()); 
                if (!isDefault) {
                    idStr += "*";
                }
                cell.setText(idStr);
                cell.setBackground(background);
                break;
            case 1:
                cell.setText(groupComponentAssociation.getAssociatedComponentName());
                cell.setBackground(background);
                break;
            case 2:
                cell.setImage(UIUtilities.getAndRegisterImage(ImageKeys.CHECKED_DISABLED));
                break;
            case 3:
                if (groupComponentAssociation.canModify()) {
                    cell.setImage(UIUtilities.getAndRegisterImage(ImageKeys.CHECKED));
                }
                else {
                    cell.setImage(UIUtilities.getAndRegisterImage(ImageKeys.UNCHECKED));                    
                }
                break;
            case 4:
                if (groupComponentAssociation.canDelete()) {
                    cell.setImage(UIUtilities.getAndRegisterImage(ImageKeys.CHECKED));
                }
                else {
                    cell.setImage(UIUtilities.getAndRegisterImage(ImageKeys.UNCHECKED));                    
                }
                break;
            }
        }
    }
    
    private class EditRightsCellEditor extends EditingSupport {
        private RightsColumn column;
        private CellEditor editor;

        public EditRightsCellEditor(TableViewer tableViewer, RightsColumn column) {
            super(tableViewer);
            this.column = column;

            switch (column) {
            case READ:
            case MODIFY:
            case DELETE:
                editor = new CheckboxCellEditor(null, SWT.CHECK);
                break;
            default:
                break;
            }
        }

        @Override
        protected boolean canEdit(Object element) {
            switch (column) {
            case READ:
                return false;
            case MODIFY:
            case DELETE:
                return true;
            default:
                return true;
            }            
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        @Override
        protected Object getValue(Object element) {
            GroupComponentAssociation groupAssociation = (GroupComponentAssociation) element;
            switch (column) {
            case READ:
                return (groupAssociation.canRead());
            case MODIFY:
                return (groupAssociation.canModify());
            case DELETE:
                return (groupAssociation.canDelete());

            }
            return false;
        }

        @Override
        protected void setValue(Object element, Object value) {
            GroupComponentAssociation editedAssociation = (GroupComponentAssociation) element;
            EditRights original = editedAssociation.getRights();
            boolean flag = (Boolean) value;
            switch (column) {
            case MODIFY:
                if (flag) {
                    editedAssociation.setRights(EditRights.ReadModify);
                } else {
                    editedAssociation.setRights(EditRights.Read);
                }
                break;
            case DELETE:
                if (flag) {
                    editedAssociation.setRights(EditRights.ReadModifyDelete);
                } else {
                    editedAssociation.setRights(EditRights.ReadModify);
                }
                break;
            default:
                break;
            }
            
            // update the model
            SAFRAssociationList<GroupComponentAssociation> rights = editRights.get(currentType);
            EditRights def = mediator.getCurrentEnvAss().getEnvRole().getRights().getComponentRight(currentType);
            // remove rights where we are now the default
            if (editedAssociation.getRights().equals(def)) {
                // find right and remove it
                for (GroupComponentAssociation compAss : rights) {
                    if (compAss.getAssociatedComponentIdNum().equals(editedAssociation.getAssociatedComponentIdNum())) {
                        rights.remove(compAss);
                        break;
                    }
                }
                getViewer().update(element, null);
            }
            // if moving from a default to non default
            else if (original.equals(def)) {
                GroupComponentAssociation newAss = new GroupComponentAssociation(
                    editedAssociation.getEnvironmentId(),
                    editedAssociation.getAssociatingComponentId(),
                    editedAssociation.getAssociatingComponentName(),
                    editedAssociation.getAssociatedComponentIdNum(),
                    editedAssociation.getAssociatedComponentName(),
                    editedAssociation.getRights()
                    );
                rights.add(newAss);                
                getViewer().update(newAss, null);
            } else {
                getViewer().update(element, null);                
            }
            mediator.setDirty();
            setCheckButtonState();
            mediator.onEditRightsChanged();
        }
    }
    
    private class EditRightsTableSorter extends ViewerSorter {
        private int colNumber;
        private int dir;

        public EditRightsTableSorter(int colNumber1, int dir) {
            super();
            this.colNumber = colNumber1;
            this.dir = dir;

        }

        public int compare(Viewer viewer, Object obj1, Object obj2) {
            GroupComponentAssociation g1 = (GroupComponentAssociation) obj1;
            GroupComponentAssociation g2 = (GroupComponentAssociation) obj2;
            int rc = 0;

            switch (colNumber) {

            case 0:
                rc = g1.getAssociatedComponentIdNum().compareTo(g2.getAssociatedComponentIdNum());
                break;
            case 1:
                rc = UIUtilities.compareStrings(g1.getAssociatedComponentName(), g2.getAssociatedComponentName());
                break;
            case 2:
                rc = UIUtilities.compareBooleans(g1.canRead(), g2.canRead());
                break;
            case 3:
                rc = UIUtilities.compareBooleans(g1.canModify(), g2.canModify());
                break;
            case 4:
                rc = UIUtilities.compareBooleans(g1.canDelete(), g2.canDelete());
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
    
    private class EditRightsColumnSelection extends SelectionAdapter {
        private int colNumber;
        private TableViewer tabViewer;

        EditRightsColumnSelection(TableViewer tabViewer, int colNumber) {
            this.colNumber = colNumber;
            this.tabViewer = tabViewer;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                mediator.getSite().getShell().setCursor(
                    mediator.getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
                Table table = tabViewer.getTable();
                TableColumn sortColumn = table.getSortColumn();
                TableColumn currentColumn = (TableColumn) e.widget;
                int dir = table.getSortDirection();
                if (sortColumn == currentColumn) {
                    dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                } else {
                    table.setSortColumn(currentColumn);
                    if (currentColumn == table.getColumn(0)) {
                        dir = SWT.DOWN;
                    } else {
                        dir = SWT.UP;
                    }
                }

                table.setSortDirection(dir);
                tabViewer.setSorter(new EditRightsTableSorter(colNumber,dir));
                
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
    
    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.ui.editors.permission.PermEditRights");
    
    private PermMediator mediator;
    
    // UI Elements
    private Section section;
    private CTabFolder tabPermissions;
    private Map<ComponentType, TableViewer> tables = new HashMap<ComponentType, TableViewer>(); 
    private Map<ComponentType, CTabItem> tabs = new HashMap<ComponentType, CTabItem>(); 
    private Button checkAllModify;
    private Button checkAllDelete;
    
    // Model     
    private Map<ComponentType, SAFRAssociationList<GroupComponentAssociation>> editRights;
    private ComponentType currentType = null;
    private boolean checkModify = true;
    private boolean checkDelete = true;
	private MenuItem compoOpenEditorItem;
	private Menu menu;
	private TableViewer tableViewerRights;
    
    public PermEditRights() {
    }
    
    protected void setMediator(PermMediator mediator) {
        this.mediator = mediator;
    }
 
    protected boolean isMouseHover() {
        Control control = Display.getCurrent().getFocusControl();
        return control == tables.get(currentType).getTable();
    }
    
    protected ComponentType getComponentType() {
        return currentType;
    }
    
    protected void create() {
        
        section = mediator.getFormToolkit().createSection(mediator.getForm().getBody(), 
            Section.TITLE_BAR|Section.EXPANDED);
        FormData formData = new FormData();
        formData.top = new FormAttachment(0,0);
        formData.bottom = new FormAttachment(100,0);
        formData.left = new FormAttachment(mediator.getTopLeftSection(),2);
        formData.right = new FormAttachment(100,0);
        section.setLayoutData(formData);
        section.setText("Edit Rights");
        
        Composite sectionClient = mediator.getFormToolkit().createComposite(section);
        sectionClient.setLayout(new FormLayout());
        
        tabPermissions = new CTabFolder(sectionClient, SWT.TOP);
        tabPermissions.setLayout(new FillLayout());
        mediator.getFormToolkit().adapt(tabPermissions, true, true);
        
        FormData tabData = new FormData();
        tabData.top = new FormAttachment(0,0);
        tabData.left = new FormAttachment(0,10);
        tabData.right = new FormAttachment(95,10);
        tabData.bottom = new FormAttachment(80,10);
        tabPermissions.setLayoutData(tabData);
        
        createTabItem("User Exit", ComponentType.UserExitRoutine);
        createTabItem("Physical File", ComponentType.PhysicalFile);
        createTabItem("Logical File", ComponentType.LogicalFile);
        createTabItem("Logical Record", ComponentType.LogicalRecord);
        createTabItem("Lookup Path", ComponentType.LookupPath);
        createTabItem("View", ComponentType.View);
        createTabItem("View Folder", ComponentType.ViewFolder);
        
        tabPermissions.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                currentType = (ComponentType)e.item.getData();
                tables.get(currentType).setInput(editRights);
                tables.get(currentType).refresh();
                setCheckButtonState();                
            }
            
        });
        
        Group checkAllGroup = new Group(sectionClient, SWT.SHADOW_IN);    
        checkAllGroup.setText("Un/Check All");
        mediator.getFormToolkit().adapt(checkAllGroup, true, true);
        checkAllGroup.setLayout(new FormLayout());
        
        FormData chData = new FormData();
        chData.top = new FormAttachment(tabPermissions,0);
        chData.left = new FormAttachment(0,10);
        checkAllGroup.setLayoutData(chData);
        
        checkAllModify = mediator.getFormToolkit().createButton(checkAllGroup, "Check All Modify", SWT.PUSH);
        checkAllModify.setData(SAFRLogger.USER, "Check All Modify");                                                                                                

        FormData chmData = new FormData();
        chmData.top = new FormAttachment(0,10);
        chmData.left = new FormAttachment(0,10);
        chmData.bottom = new FormAttachment(80,0);
        chmData.width = 200;
        checkAllModify.setLayoutData(chmData);

        checkAllModify.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                EditRights def = mediator.getCurrentEnvAss().getEnvRole().getRights().getComponentRight(currentType);
                
                // loop through all table rows and determine what to change in the model
                Table table = tables.get(currentType).getTable();
                
                TableItem [] items = table.getItems ();
                for (TableItem item : items) {
                    GroupComponentAssociation ass = (GroupComponentAssociation) item.getData();
                    if (checkModify) {
                        if (def.equals(EditRights.ReadModify)) {
                            // clear all non ReadModify rights  
                            if (ass.getRights().equals(EditRights.Read)) {
                                editRights.get(currentType).remove(ass);                        
                            }
                        }
                        else {
                            // must add a Read/Modify right for every non assigned component
                            if (ass.getRights().equals(def)) {
                                // we are equal to default so must add to model
                                editRights.get(currentType).add(ass);
                                editRights.get(currentType).get(ass).setRights(EditRights.ReadModify);
                            }
                            else {
                                if (ass.getRights().equals(EditRights.Read)) {
                                    editRights.get(currentType).get(ass).setRights(EditRights.ReadModify);
                                }
                            }                        
                        }
                    }
                    else {
                        if (def.equals(EditRights.Read)) {
                            // must remove all rights
                            editRights.get(currentType).remove(ass);                            
                        }
                        else {
                            // make sure all rights are Read
                            if (ass.getRights().equals(def)) {
                                editRights.get(currentType).add(ass);                        
                                editRights.get(currentType).get(ass).setRights(EditRights.Read);
                            }
                            else {
                                editRights.get(currentType).get(ass).setRights(EditRights.Read);
                            }
                        }                        
                    }
                }
                mediator.setDirty();
                
                // refresh the table from the model
                tables.get(currentType).setInput(editRights);
                tables.get(currentType).refresh();
                
                setCheckButtonState();
                setTabState(tabPermissions.getSelection());                
                mediator.onEditRightsChanged();
            }
        });
        
        checkAllDelete = mediator.getFormToolkit().createButton(checkAllGroup, "Check All Modify/Delete   ", SWT.PUSH);
        checkAllDelete.setData(SAFRLogger.USER, "Check All Delete");                                                                                                

        FormData chdData = new FormData();
        chdData.top = new FormAttachment(0,10);
        chdData.left = new FormAttachment(checkAllModify,10);
        chdData.right = new FormAttachment(95);
        chdData.bottom = new FormAttachment(80,0);
        chdData.width = 200;
        checkAllDelete.setLayoutData(chdData);
        
        checkAllDelete.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                EditRights def = mediator.getCurrentEnvAss().getEnvRole().getRights().getComponentRight(currentType);
                
                // loop through all table rows and determine what to change in the model
                Table table = tables.get(currentType).getTable();
                
                TableItem [] items = table.getItems ();
                for (TableItem item : items) {
                    GroupComponentAssociation ass = (GroupComponentAssociation) item.getData();
                    if (checkDelete) {
                        if (def.equals(EditRights.ReadModifyDelete)) {
                            // clear all rights non ReadModifyDelete
                            editRights.get(currentType).remove(ass);                        
                        }
                        else {
                            // must add a ReadModifyDelete right for every non assigned component
                            if (ass.getRights().equals(def)) {
                                // not assigned so add
                                editRights.get(currentType).add(ass);
                                editRights.get(currentType).get(ass).setRights(EditRights.ReadModifyDelete);                                
                            }       
                            else {
                                editRights.get(currentType).get(ass).setRights(EditRights.ReadModifyDelete);                                
                            }
                        }
                    }
                    else {
                        if (def.equals(EditRights.ReadModify)) {
                            // must remove all rights
                            editRights.get(currentType).remove(ass);
                        }
                        else {
                            // make sure all rights are ReadModify
                            if (ass.getRights().equals(def)) {
                                editRights.get(currentType).add(ass);                        
                                editRights.get(currentType).get(ass).setRights(EditRights.ReadModify);                                
                            }
                            else {
                                editRights.get(currentType).get(ass).setRights(EditRights.ReadModify);                                
                            }
                        }                        
                    }
                }
                mediator.setDirty();
                
                // refresh the table from the model
                tables.get(currentType).setInput(editRights);                
                tables.get(currentType).refresh();
                
                setCheckButtonState();
                setTabState(tabPermissions.getSelection());                
                mediator.onEditRightsChanged();
            }
        });
        
        checkAllModify.setEnabled(false);
        checkAllDelete.setEnabled(false);
        section.setEnabled(false);        
        section.setClient(sectionClient);
        
        mediator.setPopupEnabled(false);
    }
    
    private void createTabItem(String name, ComponentType type) {
        Composite comp = new Composite(tabPermissions, SWT.NONE);
        comp.setLayout(new FillLayout());
        
        CTabItem tabItem = new CTabItem(tabPermissions, SWT.NONE);
        tabItem.setText(name);            
        tabItem.setControl(comp);
        tabItem.setData(type);
        tabItem.setData("NAME", name);
        tabs.put(type, tabItem);
        tabPermissions.setSelection(tabItem);        

        Table tableRights = mediator.getSAFRGUIToolkit().createTable(comp,
            SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, false);
        tableRights.setData(SAFRLogger.USER, name + "edit rights table");                                                                                        
        TableViewer tableViewerRights = new TableViewer(tableRights);
        tableRights.setHeaderVisible(true);
        tableRights.setLinesVisible(true);
        
        int[] columnWidths = { 80, 300, 70, 80, 80 };
        String[] columnHeaders = { "ID", "Name", "Read","Modify", "Delete" };
        for (int i = 0; i < columnWidths.length; i++) {
            TableViewerColumn column = new TableViewerColumn(tableViewerRights, SWT.NONE);
            column.getColumn().setText(columnHeaders[i]);
            column.getColumn().setToolTipText(columnHeaders[i]);
            column.getColumn().setWidth(columnWidths[i]);
            column.getColumn().setResizable(true);
            if (i==RightsColumn.READ.getColNum()) {
                column.setEditingSupport(new EditRightsCellEditor(tableViewerRights,RightsColumn.READ));
            }
            else if (i==RightsColumn.MODIFY.getColNum()) {
                column.setEditingSupport(new EditRightsCellEditor(tableViewerRights,RightsColumn.MODIFY));                
            }
            else if (i==RightsColumn.DELETE.getColNum()) {
                column.setEditingSupport(new EditRightsCellEditor(tableViewerRights,RightsColumn.DELETE));                
            }
            
            EditRightsColumnSelection colListener = 
                new EditRightsColumnSelection(tableViewerRights, i);
            column.getColumn().addSelectionListener(colListener);            
        }

        tableViewerRights.setContentProvider(new EditRightsContentProvider(type));
        tableViewerRights.setLabelProvider(new EditRightsLabelProvider());
        
        EditRightsSelectionListener selectListener = new EditRightsSelectionListener();
        tableViewerRights.addSelectionChangedListener(selectListener);
        
        // Code for Context menu
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableRights);
        tableRights.setMenu(menu);
        mediator.getSite().registerContextMenu(menuManager, tableViewerRights); 
        
        tableViewerRights.setInput(editRights);
        tables.put(type, tableViewerRights);
        
        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE);
        if (prefs.load()) {
            int sortDir;
            if (prefs.getOrder() == Order.ASCENDING) {
                sortDir = SWT.UP;
            }
            else {
                sortDir = SWT.DOWN;
            }
            tableViewerRights.getTable().setSortColumn(
                tableViewerRights.getTable().getColumn(prefs.getColumn()));            
            tableViewerRights.getTable().setSortDirection(sortDir);
            tableViewerRights.setSorter(new EditRightsTableSorter(prefs.getColumn(),sortDir));            
        }
        else {
            tableViewerRights.getTable().setSortDirection(SWT.UP);
            tableViewerRights.setSorter(new EditRightsTableSorter(1,SWT.UP));            
            tableViewerRights.getTable().setSortColumn(
                tableViewerRights.getTable().getColumn(1));            
        }
        
    }

    protected void clearRights() {
        editRights = null;
        checkAllModify.setEnabled(false);
        checkAllDelete.setEnabled(false);
        section.setEnabled(false);
        currentType = (ComponentType)tabPermissions.getSelection().getData();
        tables.get(currentType).setInput(editRights);
        tables.get(currentType).refresh();        
    }
    
    
    protected void onAssociationSelected() {
        section.setEnabled(true);
        try {
            if (mediator.getCurrentEnvAss() == null) {
                editRights = null;
            }
            else {
                editRights = mediator.getCurrentGroup().getEnvironmentComponentRights(
                    mediator.getCurrentEnvAss().getEnvironmentId());
            }
        } catch (SAFRException e) {
            UIUtilities.handleWEExceptions(e);
        }
        currentType = (ComponentType)tabPermissions.getSelection().getData();
        tables.get(currentType).setInput(editRights);        
        tables.get(currentType).refresh();
        setCheckButtonState();
        // set all tab states
        for (Entry<ComponentType, CTabItem> entry : tabs.entrySet()) {
            setTabState(entry.getValue());
        }
    }

    protected void setToRoleDefault() {
        // clear out all rights in the model
        for (Entry<ComponentType, SAFRAssociationList<GroupComponentAssociation>> entry : editRights.entrySet()) {
            // remove all from association list
            entry.getValue().removeAll();
        }
        tables.get(currentType).setInput(editRights);        
        tables.get(currentType).refresh();
        
        setCheckButtonState();
        
        for (CTabItem item : tabPermissions.getItems()) {
            setTabState(item);
        }
    }

    protected boolean isDefault() {
        boolean isDefault = true;
        if (editRights != null) {            
            for (Entry<ComponentType, SAFRAssociationList<GroupComponentAssociation>> entry : editRights.entrySet()) {
                if (!entry.getValue().getActiveItems().isEmpty()) {
                    isDefault = false;
                    break;
                }
            }
        }
        return isDefault;
    }

    protected boolean isTypeDefault(ComponentType type) {
        boolean isDefault = true;
        if (editRights != null) {            
            SAFRAssociationList<GroupComponentAssociation> assList = editRights.get(type);
            if (!assList.getActiveItems().isEmpty()) {
                isDefault = false;
            }
        }
        return isDefault;
    }
    
    protected boolean isDefault(GroupComponentAssociation compAss) {
        boolean isDefault = true;
        EditRights def = mediator.getCurrentEnvAss().getEnvRole().getRights().getComponentRight(currentType);
        if (!compAss.getRights().equals(def)) {
            isDefault = false;
        }
        return isDefault;
    }
    
    protected GroupComponentAssociation getCurrentCompAss() {
        if (tables.get(currentType).getTable().getSelectionCount() <= 0) {
            return null;
        }
        else {
            return (GroupComponentAssociation) tables.get(currentType).
                getTable().getSelection()[0].getData();
        }
        
    }

    protected void setTabState(CTabItem tabItem) {
        
        String name = (String)tabItem.getData("NAME");
        ComponentType type=(ComponentType)tabItem.getData();
        if (isTypeDefault(type)) {
            tabItem.setText(name);            
        }
        else {
            tabItem.setText(name + " *");            
        }        
    }
    
    protected void setCheckButtonState() {
        Table table = tables.get(currentType).getTable();
        
        // check modify button state
        TableItem [] items = table.getItems ();
        boolean needChecked = false;        
        for (TableItem item : items) {
            GroupComponentAssociation ass = (GroupComponentAssociation) item.getData();
            if (ass.getRights() == EditRights.Read) {
                needChecked = true;
                break;
            }
        }
        if (needChecked) {
            checkAllModify.setText("Check All Modify");
            checkModify = true;
        }
        else {
            checkAllModify.setText("Uncheck All Modify/Delete");            
            checkModify = false;
        }

        // check delete button state
        needChecked = false;
        for (TableItem item : items) {
            GroupComponentAssociation ass = (GroupComponentAssociation) item.getData();
            if (ass.getRights() == EditRights.Read ||
                ass.getRights() == EditRights.ReadModify) {
                needChecked = true;
                break;
            }
        }
        if (needChecked) {
            checkAllDelete.setText("Check All Modify/Delete");
            checkDelete = true;
        }
        else {
            checkAllDelete.setText("Uncheck All Delete");            
            checkDelete = false;
        }
        
        if (items.length == 0) {
            checkAllModify.setEnabled(false);
            checkAllDelete.setEnabled(false);
        }
        else {
            checkAllModify.setEnabled(true);
            checkAllDelete.setEnabled(true);            
        }
    }
    
  
}
