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
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public interface PermMediator {
    
    // Main Editor
    Form getForm();
    FormToolkit getFormToolkit();
    SAFRGUIToolkit getSAFRGUIToolkit();
    IWorkbenchPartSite getSite();
    void setDirty();
    void setPopupEnabled(boolean enabled);

    // information    
    Section getTopLeftSection();
    Section getMiddleLeftSection();    
    GroupEnvironmentAssociation getCurrentEnvAss();
    GroupComponentAssociation getCurrentCompAss();
    ComponentType getComponentType();
    Group getCurrentGroup();    
    boolean isDefault();
    
    // PermEditRights
    // Handlers    
    void create();    
    void onAssociationSelected();    
    void setToRoleDefault();    
    void onPermissionChanged();    
    void onEditRightsChanged();
    void onSave();
    
}
