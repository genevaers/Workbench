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

import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.associations.GroupComponentAssociation;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.ui.editors.permission.EnvPermEditor.MouseHover;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class EnvPermMediator implements PermMediator {

    private EnvPermEditor permEditor;
    private EnvPermEnvironment permEnvironment;
    private EnvPermGroups permGroups;
    private PermPermissions permPermissions;
    private PermEditRights permEditRights;
    
    public EnvPermMediator(EnvPermEditor permEditor, EnvPermEnvironment permEnvironment, EnvPermGroups permGroups,
        PermPermissions permPermissions, PermEditRights permEditRights) {
        this.permEditor = permEditor;
        this.permEnvironment = permEnvironment;
        this.permGroups = permGroups;
        this.permPermissions = permPermissions;
        this.permEditRights = permEditRights;        
    }
    
    // EnvPermEditor
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
        
    public Section getTopLeftSection() {
        return permEnvironment.getSection();
    }
    
    public Section getMiddleLeftSection() {
        return permGroups.getSection();
    }
    
    public Environment getCurrentEnvironment() {
        return permEnvironment.getCurrentEnvironment();
    }
    
    public GroupEnvironmentAssociation getCurrentEnvAss() {
        return permGroups.getCurrentEnvAss();
    }

    public Group getCurrentGroup() {
        return permGroups.getCurrentGroup();
    }

    public List<Group> getGroupList() {
        return permGroups.getGroupList();
    }
    
    public boolean isDefault() {
        return (permPermissions.isDefault() && permEditRights.isDefault());
    }

    // PermEditRights
    // Handlers    
    public void create() {
        permEnvironment.create();
        permGroups.create();
        permPermissions.create();
        permEditRights.create();
    }
    
    public void onEnvironmentSelected() {
        permGroups.onEnvironmentSelected();
        permPermissions.clearPermissions();
        permEditRights.clearRights();
    }
    
    public void onAssociationSelected() {
        permPermissions.onAssociationSelected();
        permEditRights.onAssociationSelected();
    }
    
    public void onGroupUnselected() {
        permEditRights.clearRights();        
        permPermissions.clearPermissions();        
    }    
    
    public void setToRoleDefault() {
        permPermissions.setToRoleDefault();
        permEditRights.setToRoleDefault();
    }
    
    public void onPermissionChanged() {
        permGroups.refreshDefaultButtonState();
    }
    
    public void onEditRightsChanged() {
        permGroups.refreshDefaultButtonState();
    }

    public GroupComponentAssociation getCurrentCompAss() {
        return permEditRights.getCurrentCompAss();
    }

    public MouseHover getMouseHover() {
        if (permGroups.isMouseHover()) {
            return MouseHover.GRPTABLE;
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
