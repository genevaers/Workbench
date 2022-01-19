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


import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class ReloadSecuritySettings extends AbstractHandler implements IHandler {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.commands.ReloadSecuritySettings");
    
    public Object execute(ExecutionEvent event) throws ExecutionException {
        
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page.closeEditors( page.getEditorReferences(), true)) {
            try {
                SAFRApplication.getUserSession().reload();
            } catch (SAFRException e) {
                logger.log(Level.SEVERE, "Session no longer valid", e);
                MessageDialog.openError(Display.getCurrent().getActiveShell(), "Session no longer valid", e.getMessage() 
                    +SAFRUtilities.LINEBREAK + "Please start Workbench again");
                PlatformUI.getWorkbench().close();
                return null;
            }

            UIUtilities.enableDisableMenuAsPerUserRights();
            
            ApplicationMediator.getAppMediator().refreshNavigator();
            ApplicationMediator.getAppMediator().setNavigatorSelType(ComponentType.View);
            ApplicationMediator.getAppMediator().refreshStatusBar();
        }
        return null;        
    }

}
