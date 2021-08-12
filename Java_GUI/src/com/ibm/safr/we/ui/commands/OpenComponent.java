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
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.dialogs.OpenComponentDialog;
import com.ibm.safr.we.ui.utilities.EditorOpener;

public class OpenComponent extends AbstractHandler implements IHandler {
	OpenComponentDialog openCompDialog = null;

	public Object execute(ExecutionEvent event) throws ExecutionException {
	    ComponentType curType = ComponentType.View;
	    while (true) {
	        if (openCompDialog == null) {
	            openCompDialog = new OpenComponentDialog(HandlerUtil.getActiveShell(event), curType);
	            openCompDialog.setBlockOnOpen(true);
	        }
    		openCompDialog.open();
    		if (openCompDialog.getReturnCode() == IDialogConstants.OK_ID) {
    		    curType = openCompDialog.getCurType();
    			try {
    			    ApplicationMediator.getAppMediator().waitCursor();
    				if (openCompDialog.getOpenText().equals("")) {
    				    break;
    				}
    				else {
    				    if (curType == ComponentType.User) {
    				        if (EditorOpener.openUser(openCompDialog.getOpenText()) != null) {
    				            break;
    				        }
    				    }
    				    else {
    				        if (EditorOpener.open(openCompDialog.getOpenId(), curType) != null) {
    				            break;
    				        }
    				    }
    				}
    			} finally {
                    ApplicationMediator.getAppMediator().normalCursor();
    			}
    		}
    		else {
    		    break;
    		}
	    }
		return null;
	}

}
