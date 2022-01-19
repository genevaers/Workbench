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


import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.associations.GroupComponentAssociation;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.ui.editors.permission.GroupPermEditor.MouseHover;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class GroupPermMediator implements PermMediator {

    private GroupPermEditor permEditor;
    private GroupPermGroup permGroup;
    private GroupPermEnvironments permEnvironments;
    private PermPermissions permPermissions;
    private PermEditRights permEditRights;
    
    public GroupPermMediator(GroupPermEditor permEditor, GroupPermGroup permGroup, GroupPermEnvironments permEnvironments, 
        PermPermissions permPermissions, PermEditRights permEditRights) {
        this.permEditor = permEditor;
        this.permGroup = permGroup;
        this.permEnvironments = permEnvironments;
        this.permPermissions = permPermissions;
        this.permEditRights = permEditRights;        
    }
    
    // PermEditor
    public Form getForm() {
        return permEditor.getForm();
    }
    
    public FormToolkit getFormToolkit() {
        return permEditor.getFormToolkit();
    }

    public SAFRGUIToolkit getSAFRGUIToolkit() {
        return permEditor.getSafrGuiToolkit();
    }
    
    public IWorkbenchPartSite getSite() {
        return permEditor.getPermSite();
    }
    
    public void setDirty() {
        permEditor.setEditorDirty();
    }
    
    
    // PermGroup
    public Section getTopLeftSection() {
        return permGroup.getSection();
    }
    
    public Group getCurrentGroup() {
        return permGroup.getCurrentGroup();
    }
    
    
    

    // PermEnvironments
    public Section getMiddleLeftSection() {
        return permEnvironments.getSection();
    }

    public GroupEnvironmentAssociation getCurrentEnvAss() {
        return permEnvironments.getCurrentEnvAss();
    }

    public boolean isDefault() {
        return (permPermissions.isDefault() && permEditRights.isDefault());
    }
    
    // PermEditRights
    // Handlers    
    public void create() {
        permGroup.create();
        permEnvironments.create();
        permPermissions.create();
        permEditRights.create();
    }
    
    public void onGroupSelected() {
        permEnvironments.onGroupSelected();
        permPermissions.clearPermissions();
        permEditRights.clearRights();
    }
    
    public void onAssociationSelected() {
        permPermissions.onAssociationSelected();
        permEditRights.onAssociationSelected();
    }
    
    public void onEnvironmentUnselected() {
        permEditRights.clearRights();        
        permPermissions.clearPermissions();
    }    
    
    public void setToRoleDefault() {
        permPermissions.setToRoleDefault();
        permEditRights.setToRoleDefault();
    }
    
    public void onPermissionChanged() {
        permEnvironments.refreshDefaultButtonState();
    }
    
    public void onEditRightsChanged() {
        permEnvironments.refreshDefaultButtonState();
    }
    
    public GroupComponentAssociation getCurrentCompAss() {
        return permEditRights.getCurrentCompAss();
    }   
    
    public MouseHover getMouseHover() {
        if (permEnvironments.isMouseHover()) {
            return MouseHover.ENVTABLE;
        }
        else if (permEditRights.isMouseHover()) {
            return MouseHover.COMPTABLE;            
        }
        else {
            return MouseHover.OTHER;
        }
    }

    public ComponentType getComponentType() {
        return permEditRights.getComponentType();
    }

    public void setPopupEnabled(boolean enabled) {
        permEditor.setPopupEnabled(enabled);        
    }

    public void onSave() {
        permEditRights.onAssociationSelected();
        permPermissions.onAssociationSelected();
    }

}
