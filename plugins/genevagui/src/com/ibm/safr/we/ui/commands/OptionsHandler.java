package com.ibm.safr.we.ui.commands;

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


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.dialogs.OptionsDialog;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class OptionsHandler extends AbstractHandler implements IHandler {

    OptionsDialog optionsDialog;
    ComponentType componentType;
    
	public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchSite site = HandlerUtil.getActiveSite(event);
        SAFRPreferences preferences = new SAFRPreferences();
		optionsDialog = new OptionsDialog(HandlerUtil.getActiveShell(event), SAFRLogger.getLogPath(),
				preferences.getReportsPath(),
				preferences.isIgnoreMigrateWarnings(),
				preferences.isIgnoreImportWarnings());
        optionsDialog.open();
        if (optionsDialog.getReturnCode() == IDialogConstants.OK_ID) {
            try {
                ApplicationMediator.getAppMediator().waitCursor();
                
                // set preferences and store
                // new path has been set
                String newLogPath = optionsDialog.getLogPathStr();
                if (!newLogPath.endsWith("\\") && !newLogPath.endsWith("/")) {
                    newLogPath += "\\";
                }
                newLogPath = newLogPath.replace('/', '\\');            
                try {
                    SAFRLogger.changeLogPath(newLogPath);
                } catch (SAFRException e) {
					UIUtilities.handleWEExceptions(e, "Error occurred while changing log path", "Change Log Path");
                }
                
                preferences.setIgnoreMigrateWarnings(optionsDialog.isIgnoreMigrateWarnings());
                preferences.setIgnoreImportWarnings(optionsDialog.isIgnoreImportWarnings());
                preferences.setReportsPath(optionsDialog.getReportsPath());

                // close all open View editors
                site.getPage().closeAllEditors(false);
                
                // refresh state from preferences
                ApplicationMediator.getAppMediator().refreshStatusBar();
                
            } finally {
                ApplicationMediator.getAppMediator().normalCursor();
            }
        }
        return null;
	}

}
