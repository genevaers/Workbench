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
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.ui.views.navigatortree.NavigatorView;

public class NewMetadataHandler extends AbstractHandler implements IHandler {

	private TreeItemId selectedItem;

	public Object execute(ExecutionEvent event) throws ExecutionException {

		NavigatorView view = (NavigatorView) 
		    HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView("SAFRWE.Treeview");
		if (view != null) {
			try {
				selectedItem = view.getSelectedItem().getId();

				IHandlerService handlerService = (IHandlerService) HandlerUtil.getActiveWorkbenchWindow(event)
				    .getService(IHandlerService.class);
				User user = SAFRApplication.getUserSession().getUser();

				if (selectedItem == TreeItemId.ENV) {
					if (user.isSystemAdmin()) {
						handlerService.executeCommand("SAFRWE.Environment",null);
					}
				} else if (selectedItem == TreeItemId.CONTROL) {
					if (UIUtilities.isSystemAdminOrEnvAdmin()) {
						handlerService.executeCommand("SAFRWE.ControlRecord",null);
					}
				} else if (selectedItem == TreeItemId.PHYSICALFILE) {
					if (SAFRApplication.getUserSession().hasPermission(Permissions.CreatePhysicalFile)) {
						handlerService.executeCommand("SAFRWE.PhysicalFile",null);
					}
				} else if (selectedItem == TreeItemId.USEREXITROUTINE) {
					if (SAFRApplication.getUserSession().hasPermission(Permissions.CreateUserExitRoutine)) {
						handlerService.executeCommand("SAFRWE.User-Exit Routine", null);
					}
				} else if (selectedItem == TreeItemId.LOGICALFILE) {
					if (SAFRApplication.getUserSession().hasPermission(Permissions.CreateLogicalFile)) {
						handlerService.executeCommand("SAFRWE.LogicalFile",null);
					}
				} else if (selectedItem == TreeItemId.LOGICALRECORD) {
					if (SAFRApplication.getUserSession().hasPermission(Permissions.CreateLogicalRecord)) {
						handlerService.executeCommand("SAFRWE.LogicalRecord",null);
					}
				} else if (selectedItem == TreeItemId.LOOKUP) {
					if (SAFRApplication.getUserSession().hasPermission(Permissions.CreateLookupPath)) {
						handlerService.executeCommand("SAFRWE.LookupPath", null);
					}
				} else if (selectedItem == TreeItemId.VIEWFOLDER) {
					if (SAFRApplication.getUserSession().hasPermission(Permissions.CreateViewFolder)) {
						handlerService.executeCommand("SAFRWE.ViewFolder", null);
					}
				} else if (selectedItem == TreeItemId.USER) {
					if (user.isSystemAdmin()) {
						handlerService.executeCommand("SAFRWE.User", null);
					}
				} else if (selectedItem == TreeItemId.GROUP) {
					if (user.isSystemAdmin()) {
						handlerService.executeCommand("SAFRWE.Group", null);
					}
				} else if (selectedItem == TreeItemId.VIEWFOLDERCHILD) {
					if (SAFRApplication.getUserSession().hasPermission(Permissions.CreateView)) {
						handlerService.executeCommand("SAFRWE.View", null);
					}
				}
			} catch (DAOException de) {
				UIUtilities.handleWEExceptions(de,
				    "Unexpected database error occurred while checking permission for the user.",
				    UIUtilities.titleStringDbException);
			} catch (Exception e) {
                UIUtilities.handleWEExceptions(e, "The menu item is not yet defined in the application.", null);			    
			}			
		}

		return null;
	}

}
