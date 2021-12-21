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


import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.registry.EditorRegistry;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.SAFREnvProp;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.ui.editors.CompareTextView;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.logic.LogicTextView;
import com.ibm.safr.we.ui.views.vieweditor.ActivationLogViewNew;
import com.ibm.safr.we.ui.views.vieweditor.ActivationLogViewOld;
import com.ibm.safr.we.ui.views.vieweditor.ColumnSourceView;
import com.ibm.safr.we.ui.views.vieweditor.DataSourceView;
import com.ibm.safr.we.ui.views.vieweditor.SortKeyTitleView;
import com.ibm.safr.we.ui.views.vieweditor.SortKeyView;
import com.ibm.safr.we.utilities.ProfileLocation;

@SuppressWarnings("restriction")
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor implements IWorkbenchListener {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.ApplicationWorkbenchWindowAdvisor");
	
	public ApplicationWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

    public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		configurer.getMenuManager().remove("org.eclipse.ui.externaltools.ExternalToolsSet");		
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setTitle(SAFREnvProp.WORKBENCH);

	}

	@Override
	public void postWindowOpen() {
		super.postWindowOpen();
		// add shutdown listener
		PlatformUI.getWorkbench().addWorkbenchListener(this);
		
		// Maximize the application window
		getWindowConfigurer().getWindow().getShell().setMaximized(true);
		UIUtilities.enableDisableMenuAsPerUserRights();
		
		// grey out NewEditor option in editor context menu
        ICommandService commandService = (ICommandService)
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ICommandService.class);   
        commandService.getCommand("org.eclipse.ui.window.newEditor").setHandler(null);
        
        // hide menu and actions we don't want
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideActionSet("org.eclipse.search.searchActionSet");
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideActionSet("org.eclipse.ui.edit.text.actionSet.annotationNavigation");
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideActionSet("org.eclipse.ui.edit.text.actionSet.navigation");
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideActionSet("org.eclipse.ui.externaltools.ExternalToolsSet");
        
        // close left over tabs from last session
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewReference [] views = page.getViewReferences();
        for (IViewReference view : views) {
            if (view.getId().equals(SortKeyView.ID) || 
                view.getId().equals(LogicTextView.ID) ||
                view.getId().equals(ColumnSourceView.ID) ||
                view.getId().equals(DataSourceView.ID) ||
                view.getId().equals(SortKeyTitleView.ID) ||
                view.getId().equals(CompareTextView.ID)) {
                IViewPart part = (IViewPart)view.getPart(true);
                page.hideView(part);
            }            
        }   
        
        //  If there are existing busted portal/dashboard tabs, remove them
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorReference[] refs = activePage.getEditorReferences();
		for (IEditorReference ref : refs)
		{
			String editorId = ref.getId();
			if (EditorRegistry.EMPTY_EDITOR_ID.equals(editorId))
			{				
				activePage.closeEditors(new IEditorReference[] { ref }, false);
			}
		}        
		
        // let the model layer know the WE version
        SAFRUtilities.setWEVersion(UIUtilities.getVersion());
	}

	@Override
	public void postWindowClose() {
		super.postWindowClose();
				
		// delete the temp report files
		String outputPath = ProfileLocation.getProfileLocation().getLocalProfile() + "pdf\\";
		File outputDirs = new File(outputPath);
		if (outputDirs.exists()) {
			for (File file : outputDirs.listFiles()) {
				file.delete();
			}
		}
		
        try {
            SAFRApplication.getSAFRFactory().closeDatabaseConnection();
        } catch (SAFRException se) {
            logger.log(Level.SEVERE, "Unexpected Workbench Error", se);
        }
        
		
	}
	
    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        // close left over tabs
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewReference [] views = page.getViewReferences();
        for (IViewReference view : views) {
            if (view.getId().equals(SortKeyView.ID) || 
                view.getId().equals(LogicTextView.ID) ||
                view.getId().equals(ColumnSourceView.ID) ||
                view.getId().equals(DataSourceView.ID) ||
                view.getId().equals(SortKeyTitleView.ID) ||
                view.getId().equals(CompareTextView.ID) ||
                view.getId().equals(ActivationLogViewNew.ID) ||
                view.getId().equals(ActivationLogViewOld.ID)) {
                IViewPart part = (IViewPart)view.getPart(true);
                page.hideView(part);
            }            
        }   
        return true;
    }

    public void postShutdown(IWorkbench workbench) {
    }

	
}
