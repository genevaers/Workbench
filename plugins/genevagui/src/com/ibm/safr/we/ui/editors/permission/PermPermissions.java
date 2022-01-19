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


import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.utilities.SAFRLogger;

public class PermPermissions {
    
    protected class PermissionsContent {
        protected Permissions permission;
        protected boolean hasPermission;
        
        public Permissions getPermission() {
            return permission;
        }
        public boolean isHasPermission() {
            return hasPermission;
        }

        public void setPermission(Permissions permission) {
            this.permission = permission;
        }
        public void setHasPermission(boolean hasPermission) {
            this.hasPermission = hasPermission;
        }
        
    };
    
    private class PermissionsLabelProvider extends CellLabelProvider   {

        @Override
        public void update(ViewerCell cell) {
            PermissionsContent content = (PermissionsContent)cell.getElement();
            switch (cell.getColumnIndex()) {
            case 0:
                // add asterisk and change colour if nondefault
                if (isDefault(content.getPermission())) {
                    cell.setText("");
                    cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
                }
                else {
                    cell.setText("*");
                    cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
                }
                break;
            case 1:
                cell.setText(content.getPermission().getDesc());
                // add asterisk and change colour if nondefault
                if (isDefault(content.getPermission())) {
                    cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));                    
                }   
                else {
                    cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
                }
                break;
            default:
            }
        }
    }
    
    private class PermissionsContentProvider implements IStructuredContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            
            // form array of permissions
            PermissionsContent contArr[] = new PermissionsContent[Permissions.values().length];

            contArr[0] = new PermissionsContent();
            contArr[0].setPermission(Permissions.CreateUserExitRoutine);
            contArr[0].setHasPermission(envAss == null ? false : envAss.canCreateUserExitRoutine());            
            contArr[1] = new PermissionsContent();
            contArr[1].setPermission(Permissions.CreatePhysicalFile);
            contArr[1].setHasPermission(envAss == null ? false : envAss.canCreatePhysicalFile());
            contArr[2] = new PermissionsContent();
            contArr[2].setPermission(Permissions.CreateLogicalFile);
            contArr[2].setHasPermission(envAss == null ? false : envAss.canCreateLogicalFile());
            contArr[3] = new PermissionsContent();
            contArr[3].setPermission(Permissions.CreateLogicalRecord);
            contArr[3].setHasPermission(envAss == null ? false : envAss.canCreateLogicalRecord());
            contArr[4] = new PermissionsContent();
            contArr[4].setPermission(Permissions.CreateLookupPath);
            contArr[4].setHasPermission(envAss == null ? false : envAss.canCreateLookupPath());
            contArr[5] = new PermissionsContent();
            contArr[5].setPermission(Permissions.CreateView);
            contArr[5].setHasPermission(envAss == null ? false : envAss.canCreateView());
            contArr[6] = new PermissionsContent();
            contArr[6].setPermission(Permissions.CreateViewFolder);
            contArr[6].setHasPermission(envAss == null ? false : envAss.canCreateViewFolder());            
            contArr[7] = new PermissionsContent();
            contArr[7].setPermission(Permissions.MigrateIn);
            contArr[7].setHasPermission(envAss == null ? false : envAss.canMigrateIn());            
            
            return contArr;
        }
        
    }
    
    private PermMediator mediator;
    
    // UI Elements
    private Section section;    
    private CheckboxTableViewer tableViewerPermissions;
    private Table tablePermissions;
    
    // Model
    private GroupEnvironmentAssociation envAss = null;
    
    public PermPermissions() {
    }
    
    protected void setMediator(PermMediator mediator) {
        this.mediator = mediator;
    }
 
    protected void create() {
        
        section = mediator.getFormToolkit().createSection(mediator.getForm().getBody(), 
            Section.TITLE_BAR|Section.EXPANDED);
        FormData formData = new FormData();
        formData.top = new FormAttachment(mediator.getMiddleLeftSection(),0);
        formData.bottom = new FormAttachment(100,0);
        formData.left = new FormAttachment(0,10);
        formData.right = new FormAttachment(35,2);
        section.setLayoutData(formData);
        section.setText("Permissions");        
        
        Composite sectionClient = mediator.getFormToolkit().createComposite(section);
        sectionClient.setLayout(new FormLayout());
        
        tablePermissions = new Table(sectionClient, SWT.CHECK
            | SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        tablePermissions.setData(SAFRLogger.USER, "Permissions Table");                                                                                   
        tablePermissions.setHeaderVisible(true);
        tablePermissions.setLinesVisible(true);        
        tableViewerPermissions = new CheckboxTableViewer(tablePermissions);
        
        FormData tabData = new FormData();
        tabData.top = new FormAttachment(0,0);
        tabData.left = new FormAttachment(0,10);
        tabData.right = new FormAttachment(55,10);
        tabData.bottom = new FormAttachment(85,10);
        tablePermissions.setLayoutData(tabData);
        
        String[] columnHeaders = { "Allow", "Functions" };
        int[] columnWidths = { 50, 190 };
        int length = columnWidths.length;
        for (int i = 0; i < length; i++) {
            TableViewerColumn column = new TableViewerColumn(
                    tableViewerPermissions, SWT.NONE);
            column.getColumn().setText(columnHeaders[i]);
            column.getColumn().setToolTipText(columnHeaders[i]);
            column.getColumn().setWidth(columnWidths[i]);
            column.getColumn().setResizable(true);
        }

        tableViewerPermissions.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                Object[] items = tableViewerPermissions.getCheckedElements();
                
                envAss.removeAllPermissions();
                for (Object item : items) {
                    PermissionsContent content = (PermissionsContent)item;
                    envAss.addPermission(Permissions.values()[content.getPermission().ordinal()]);
                } 
                mediator.onPermissionChanged();
                mediator.setDirty();
                tableViewerPermissions.refresh();
                refreshPermissionTable();
            }
            
        });
        
        tableViewerPermissions.setLabelProvider(new PermissionsLabelProvider());
        tableViewerPermissions.setContentProvider(new PermissionsContentProvider());
        tableViewerPermissions.setInput(1);
        tablePermissions.setEnabled(false);
        section.setClient(sectionClient);
        section.setEnabled(false);
    }

    protected void clearPermissions() {
        envAss = null;
        tableViewerPermissions.refresh();
        refreshPermissionTable();        
        section.setEnabled(false);
        tablePermissions.setEnabled(false);
    }
    
    protected void onAssociationSelected() {
        section.setEnabled(true);
        tablePermissions.setEnabled(true);
        envAss = mediator.getCurrentEnvAss();
        tableViewerPermissions.refresh();
        refreshPermissionTable();
    }
    
    protected void setToRoleDefault() {
        envAss.removeAllPermissions();
        for (Permissions perm : envAss.getEnvRole().getRights().getPermissions()) {
            envAss.addPermission(perm);
        }
        tableViewerPermissions.refresh();
        refreshPermissionTable();
    }

    protected boolean isDefault() {
        boolean isDefault = true;
        if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreatePhysicalFile) != 
            envAss.canCreatePhysicalFile()) {
            isDefault = false;
        }
        if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreateLogicalFile) != 
            envAss.canCreateLogicalFile()) {
            isDefault = false;
        }
        if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreateLogicalRecord) != 
            envAss.canCreateLogicalRecord()) {
            isDefault = false;
        }
        if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreateLookupPath) != 
            envAss.canCreateLookupPath()) {
            isDefault = false;
        }
        if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreateUserExitRoutine) != 
            envAss.canCreateUserExitRoutine()) {
            isDefault = false;
        }
        if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreateView) != 
            envAss.canCreateView()) {
            isDefault = false;
        }
        if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreateViewFolder) != 
            envAss.canCreateViewFolder()) {
            isDefault = false;
        }
        if (envAss.getEnvRole().getRights().hasPermission(Permissions.MigrateIn) != 
            envAss.canMigrateIn()) {
            isDefault = false;
        }
        return isDefault;
    }

    protected boolean isDefault(Permissions perm) {
        boolean isDefault = true;
        if (envAss == null) {
            return isDefault;
        }
        switch (perm) {
        case CreateUserExitRoutine:
            if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreateUserExitRoutine) != 
                envAss.canCreateUserExitRoutine()) {
                isDefault = false;
            }            
            break;
        case CreatePhysicalFile:
            if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreatePhysicalFile) != 
                envAss.canCreatePhysicalFile()) {
                isDefault = false;
            }
            break;
        case CreateLogicalFile:
            if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreateLogicalFile) != 
                envAss.canCreateLogicalFile()) {
                isDefault = false;
            }
            break;
        case CreateLogicalRecord:
            if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreateLogicalRecord) != 
                envAss.canCreateLogicalRecord()) {
                isDefault = false;
            }
            break;
        case CreateLookupPath:
            if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreateLookupPath) != 
                envAss.canCreateLookupPath()) {
                isDefault = false;
            }
            break;
        case CreateView:
            if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreateView) != 
                envAss.canCreateView()) {
                isDefault = false;
            }
            break;
        case CreateViewFolder:
            if (envAss.getEnvRole().getRights().hasPermission(Permissions.CreateViewFolder) != 
                envAss.canCreateViewFolder()) {
                isDefault = false;
            }
            break;
        case MigrateIn:
            if (envAss.getEnvRole().getRights().hasPermission(Permissions.MigrateIn) != 
                envAss.canMigrateIn()) {
                isDefault = false;
            }
        default:
            break;
        }
        return isDefault;
    }
    
    private void refreshPermissionTable() {        
        tableViewerPermissions.getTable().getItem(Permissions.CreatePhysicalFile.ordinal()).setChecked(
            envAss == null ? false : envAss.canCreatePhysicalFile());
        tableViewerPermissions.getTable().getItem(Permissions.CreateLogicalFile.ordinal()).setChecked(
            envAss == null ? false : envAss.canCreateLogicalFile());
        tableViewerPermissions.getTable().getItem(Permissions.CreateLogicalRecord.ordinal()).setChecked(
            envAss == null ? false : envAss.canCreateLogicalRecord());
        tableViewerPermissions.getTable().getItem(Permissions.CreateLookupPath.ordinal()).setChecked(
            envAss == null ? false : envAss.canCreateLookupPath());
        tableViewerPermissions.getTable().getItem(Permissions.CreateUserExitRoutine.ordinal()).setChecked(
            envAss == null ? false : envAss.canCreateUserExitRoutine());
        tableViewerPermissions.getTable().getItem(Permissions.CreateView.ordinal()).setChecked(
            envAss == null ? false : envAss.canCreateView());
        tableViewerPermissions.getTable().getItem(Permissions.CreateViewFolder.ordinal()).setChecked(
            envAss == null ? false : envAss.canCreateViewFolder());
        tableViewerPermissions.getTable().getItem(Permissions.MigrateIn.ordinal()).setChecked(
            envAss == null ? false : envAss.canMigrateIn());
    }
    
}
