package com.ibm.safr.we.ui;

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


import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.view.ViewActivator;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.editors.LookupPathEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.MetadataView;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.ui.views.navigatortree.NavigatorView;

public class ApplicationMediator {
    
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.ApplicationMediator");
    
    private static ApplicationMediator mediator = new ApplicationMediator();
        
    public static ApplicationMediator getAppMediator() {
        return mediator;
    }

    private MainTreeItem currentNavItem= null;
    private Text filterText = null;

    public static final String STATUSBARLOGIC = "STATUSBARLOGIC";
    public static final String STATUSBARVIEW = "STATUSBARVIEW";
    public static final String STATUSBARMETA = "STATUSBARMETA";
    public static final String STATUSBARMAIN = "STATUSBARMAIN";
    
    public NavigatorView getNavView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            return (NavigatorView) page.findView(NavigatorView.ID);
        }
        else {
            return null;
        }
    }

    public IEditorReference[] getEditors() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            return page.getEditorReferences();
        } else {
            return null;            
        }
    }
    
    public MetadataView getMetaView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            return (MetadataView) page.findView(MetadataView.ID);
        }
        else {
            return null;
        }
    }
    
    public void waitCursor() {
        if (Display.getCurrent().getActiveShell() != null) {
            Display.getCurrent().getActiveShell().setCursor(
                Display.getCurrent().getActiveShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
        }
    }
    
    public void normalCursor() {
        if (Display.getCurrent().getActiveShell() != null) {
            Display.getCurrent().getActiveShell().setCursor(null);
        }
    }
    
    public void refreshNavigator() {        
        try {
            waitCursor();
            NavigatorView navView =  getNavView();
            if (navView != null) {
                navView.refreshNavigator();
            }            
        }
        finally {
            normalCursor();
        }        
    }
    
    public ComponentType getNavigatorSelType() {
        return  getNavView().getSelectedItem().getComponentType();
    }

    public void setNavigatorSelType(ComponentType componentType) {
        if (componentType.equals(ComponentType.View)) {
            MainTreeItem item = getMainTreeItem(componentType, 0);
            getNavView().setSelectedItem(item);             
        } else {
            MainTreeItem item = getMainTreeItem(componentType, null);
            getNavView().setSelectedItem(item);
        }
    }
        
    public void setCurrentNavItem(MainTreeItem navigator) {
        currentNavItem = navigator;
    }

    public MainTreeItem getCurrentNavItem() {
        return currentNavItem;
    }
    
    private MainTreeItem getMainTreeItem(ComponentType componentType, Integer metadataId) {
        switch (componentType) {
        // refresh the metadata view for the component selected to be imported
        // as well as for all dependent components
        case Environment:
            return new MainTreeItem(TreeItemId.ENV, null, null, null);
        case UserExitRoutine:
            return new MainTreeItem(TreeItemId.USEREXITROUTINE, null, null, null);
        case ControlRecord:
            return new MainTreeItem(TreeItemId.CONTROL, null, null, null);
        case PhysicalFile:
            return new MainTreeItem(TreeItemId.PHYSICALFILE, null, null, null);
        case LogicalFile:
            return new MainTreeItem(TreeItemId.LOGICALFILE, null, null, null);
        case LogicalRecord:
            return new MainTreeItem(TreeItemId.LOGICALRECORD, null, null, null);
        case LookupPath:
            return new MainTreeItem(TreeItemId.LOOKUP,null, null, null);
        case ViewFolder:
            return new MainTreeItem(TreeItemId.VIEWFOLDER,null, null, null);            
        case View:
            return new MainTreeItem(metadataId, TreeItemId.VIEWFOLDERCHILD, null, null, null);                
        case User:
            return new MainTreeItem(TreeItemId.USER,null, null, null);            
        case Group:
            return new MainTreeItem(TreeItemId.GROUP,null, null, null);            
        default:
            return null;
        }
    }

    public void refreshMetadataView(ComponentType componentType, Integer metadataId) {
        if (componentType == null) {
            return;
        }
        MainTreeItem item = getMainTreeItem(componentType, metadataId);
        if (item == null) {
            return;
        }
        MetadataView metadataview = getMetaView();
        if (metadataview != null) {
            if (item.getId() == currentNavItem.getId()) {
                if (item.getId() == TreeItemId.VIEWFOLDERCHILD) {
                    if (item.getMetadataId() == null || 
                        item.getMetadataId().equals(currentNavItem.getMetadataId())) {
                        metadataview.refreshView();
                    }
                } else {
                    metadataview.refreshView();
                }
            }
        }
    }
    
    public IStatusLineManager getStatusLineManager() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            return null;
        }
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return null;
        }
        IWorkbenchPartSite site = part.getSite();
        IActionBars actionBars = null;
        if (site instanceof IViewSite) {
            IViewSite vSite = ( IViewSite ) page.getActivePart().getSite();
            actionBars = vSite.getActionBars();
        }
        else if (site instanceof IEditorSite) {
            IEditorSite eSite = ( IEditorSite ) page.getActivePart().getSite();
            actionBars = eSite.getActionBars();            
        }
        if (actionBars == null) {
            return null;
        }
        IStatusLineManager statusLineManager = actionBars.getStatusLineManager();
        
        return statusLineManager;
    }    
        
    public void refreshStatusBar() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            
            public void run() {

                IStatusLineManager statusLineManager = getStatusLineManager();                
                if (statusLineManager == null) {
                    return;
                }
                StatusLineContributionItem item = ((StatusLineContributionItem) statusLineManager.find(ApplicationMediator.STATUSBARMAIN));
                String statusLineMsg = getStatusLine();        
                item.setText(statusLineMsg);
                statusLineManager.update(false);
            }
        });     
    }

    public void refreshMetaBar() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            
            public void run() {

                IStatusLineManager statusLineManager = getStatusLineManager();                
                if (statusLineManager == null) {
                    return;
                }
                StatusLineContributionItem item = ((StatusLineContributionItem) statusLineManager.find(ApplicationMediator.STATUSBARMETA));
                String statusLineMsg = getMetaLine();        
                item.setText(statusLineMsg);
                statusLineManager.update(false);
            }
        });     
    }

    public void updateStatusContribution(final String id, final String text, final boolean visible) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                IStatusLineManager statusLineManager = getStatusLineManager();
                if (statusLineManager == null) {
                    return;
                }
                StatusLineContributionItem item = ((StatusLineContributionItem) statusLineManager.find(id));
                if (text != null) {
                    item.setText(text + "     ");
                }
                item.setVisible(visible);
                statusLineManager.update(true);
            }
        });
    }
    
    public String getMetaLine() {
        MetadataView meta = getMetaView();
        String statusLineMsg;
        if (meta == null) {
            statusLineMsg = "Row Count: 0     ";
        }
        else {
            statusLineMsg = meta.getMetaLine(); 
        }
        return statusLineMsg;
    }
    
    public String getStatusLine() {
        String statusLineMsg = "Connection: "
            + SAFRPreferences.getSAFRPreferences().get(UserPreferencesNodes.LAST_CONNECTION, "");
        
        statusLineMsg += "  Database: "
                + SAFRApplication.getDatabaseSchema() + "   User: "
                + SAFRApplication.getUserSession().getUser().getUserid();

        Group currentGroup = SAFRApplication.getUserSession().getGroup();
        if (currentGroup != null) {
            statusLineMsg += "   Group: "
                    + UIUtilities.getComboString(currentGroup.getName(),
                            currentGroup.getId());
        } else {
            statusLineMsg += "   Group: " + "none";
        }

        Environment curEnvironment = SAFRApplication.getUserSession()
                .getEnvironment();
        statusLineMsg += "   Environment: "
                + UIUtilities.getComboString(curEnvironment.getName(),
                        curEnvironment.getId());

        statusLineMsg += "   Compiler: Sycada"; //+ ViewActivator.getCompilerVersion(); sort later                
        statusLineMsg += "     ";
        return statusLineMsg;
    }

    public void setMetadataFilterText(Text filterText) {
        this.filterText = filterText;
    }
    
    public void setFocusOnMetadataFilter() {
        if (filterText != null) {
            filterText.setFocus();
        }
    }

    public void refreshDeactiveViews(Set<Integer> deactivatedViews) {
        if (deactivatedViews.size() > 0) {
            // refresh View editors
            IEditorReference[] refs = getEditors();
            for (IEditorReference ref : refs) {
                if (ref.getId().equals(ViewEditor.ID)) {
                    // get view id of this editor 
                    Integer id = UIUtilities.extractId(ref.getName());
                    if (id != null && deactivatedViews.contains(id)) {
                        // refresh this view editor
                        ViewEditor editor = (ViewEditor)ref.getEditor(false);
                        editor.makeViewInactive();
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        page.closeEditor(editor, true);
                    }
                }
            }
            
            // refresh metadata list
            refreshMetadataView(ComponentType.View, null);
            
            // refresh status bar 
            refreshMetaBar();                      
        }
    }

    public void refreshDeactiveLookups(Set<Integer> deactivatedLookups) {
        if (deactivatedLookups.size() > 0) {
            // refresh View editors
            IEditorReference[] refs = getEditors();
            for (IEditorReference ref : refs) {
                if (ref.getId().equals(LookupPathEditor.ID)) {
                    // get view id of this editor 
                    Integer id = UIUtilities.extractId(ref.getName());
                    if (id != null && deactivatedLookups.contains(id)) {
                        // refresh this view editor
                        LookupPathEditor editor = (LookupPathEditor)ref.getEditor(false);
                        editor.makeLookupInactive();
                        editor.refreshControls();
                    }
                }
            }
            
            // refresh metadata list
            refreshMetadataView(ComponentType.LookupPath, null);
            
            // refresh status bar 
            refreshMetaBar();                      
        }
    }
}
