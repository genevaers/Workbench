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


import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.services.ISourceProviderService;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.GroupComponentAssociation;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.ui.editors.OpenEditorPopupState;
import com.ibm.safr.we.ui.editors.SAFREditorPart;
import com.ibm.safr.we.ui.utilities.ISearchablePart;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class EnvPermEditor extends SAFREditorPart implements ISearchablePart {

    public enum MouseHover {
        GRPTABLE,
        COMPTABLE,
        OTHER
      };
    
    public static String ID = "SAFRWE.GroupPermissionsByEnvironmentEditor";
    
    private FormToolkit toolkit;
    private SAFRGUIToolkit safrGuiToolkit;    
    private Form form;
    private EnvPermMediator mediator;
    
    @Override
    public void createPartControl(Composite parent) {
        toolkit = new FormToolkit(parent.getDisplay());
        safrGuiToolkit = new SAFRGUIToolkit(toolkit);        
        form = toolkit.createForm(parent);
        form.setText("Permissions by Environment");
        form.getBody().setLayout(new FormLayout());        
        form.getBody().setSize(new Point(100,100));
        
        // create subsections
        EnvPermEnvironment permEnv = new EnvPermEnvironment();
        EnvPermGroups permGrps = new EnvPermGroups();
        PermPermissions permPerm = new PermPermissions();
        PermEditRights permRight = new PermEditRights();
        
        mediator = new EnvPermMediator(this, permEnv, permGrps, permPerm, permRight);
        
        permEnv.setMediator(mediator);
        permGrps.setMediator(mediator);
        permPerm.setMediator(mediator);
        permRight.setMediator(mediator);
        
        mediator.create();
    }
    
    @Override
    public void storeModel() throws DAOException, SAFRException {
        mediator.getCurrentEnvironment().storeAssociatedGroups();
        mediator.getCurrentEnvironment().setAssociatedGroupsList(mediator.getGroupList());
        mediator.getCurrentEnvironment().storeComponentRights();
        mediator.onSave();
        UIUtilities.enableDisableMenuAsPerUserRights();
    }
    
    @Override
    public void validate() throws DAOException, SAFRException {
    }

    @Override
    public String getModelName() {
        return "Group Permission By Environment";
    }

    @Override
    public void doRefreshControls() throws SAFRException {
    }

    @Override
    public void refreshModel() {
    }

    @Override
    public SAFRPersistentObject getModel() {
        return null;
    }

    @Override
    public String getComponentNameForSaveAs() {
        return null;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }


    @Override
    public ComponentType getEditorCompType() {
        return null;
    }

    @Override
    public Boolean retrySaveAs(SAFRValidationException sve) {
        return null;
    }

    // search methods
    public void searchComponent(MetadataSearchCriteria searchCriteria, String searchText) {
        // TODO Auto-generated method stub

    }

    public ComponentType getComponentType() {
        return mediator.getComponentType();
    }

    protected Form getForm() {
        return form;
    }
    
    protected FormToolkit getFormToolkit() {
        return toolkit;
    }
    
    protected SAFRGUIToolkit getSafrGuiToolkit() {
        return safrGuiToolkit;
    }

    protected IWorkbenchPartSite getPermSite() {
        return this.getSite();
    }
    
    protected void setEditorDirty() {
        setDirty(true);
    }
    
    public MouseHover getMouseHover() {
        return mediator.getMouseHover();
    }
    
    public GroupComponentAssociation getCurrentCompSelection() {
        return mediator.getCurrentCompAss();
    }
    
    public GroupEnvironmentAssociation getCurrentGrpSelection() {
        return mediator.getCurrentEnvAss();
    }    
    
    protected void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.ENVPERM);
        service.setGrpPerm(enabled);        
    }
    
}
