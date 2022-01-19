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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.dialogs.SAFRLogin;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.MetadataView;
import com.ibm.safr.we.ui.views.navigatortree.NavigatorView;

public class ReturnToLoginHandler extends AbstractHandler
		implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (PlatformUI.getWorkbench().saveAllEditors(true)) {

            SAFRApplication.getSAFRFactory().backupDatabaseConnection();
			Shell shell = HandlerUtil.getActiveShell(event);
			SAFRLogin sAFRLoginDialoge = new SAFRLogin(shell);
			sAFRLoginDialoge.create();
			int result = sAFRLoginDialoge.open();
			if (result == Window.CANCEL) {
				try {
					SAFRApplication.getSAFRFactory().restoreDatabaseConnection();
				} catch (SAFRException e) {
					UIUtilities.handleWEExceptions(e,"Unexpected error occurred while restoring DB connection.",null);
				}
			}
			else {
				try {
					// Displays the hour glass
					Display.getCurrent().getActiveShell().setCursor(
							Display.getCurrent().getActiveShell().getDisplay()
									.getSystemCursor(SWT.CURSOR_WAIT));

					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().closeAllEditors(false);
					// show metadata view.
					MetadataView metadataview = null;
					try {
						metadataview = (MetadataView) PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage()
								.showView(MetadataView.ID);
					} catch (PartInitException e1) {
						UIUtilities.handleWEExceptions(e1,"Unexpected error occurred while opening metadata view.",null);
					}
					metadataview.resetPreviousItem();
					// show SAFR explorer view.
					NavigatorView safrExplorerView = null;
					// check if the view is closed or not.If it is closed then
					// View
					// ref id id null.
					if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().findViewReference(NavigatorView.ID) == null) {
						// view is closed so create and show the view.
						try {
							safrExplorerView = (NavigatorView) PlatformUI
									.getWorkbench().getActiveWorkbenchWindow()
									.getActivePage().showView(NavigatorView.ID);

						} catch (PartInitException e) {
							UIUtilities.handleWEExceptions(
							    e,"Unexpected error occurred while opening Navigator view.",null);
						}
					} else {
						// view is not closed.So get the ref of the view.
						safrExplorerView = (NavigatorView) PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage().findViewReference(
										NavigatorView.ID).getView(true);
					}
					safrExplorerView.setDefaultTreeSelection(true);
					UIUtilities.enableDisableMenuAsPerUserRights();
					ApplicationMediator.getAppMediator().refreshStatusBar();
					
				} finally {
					Display.getCurrent().getActiveShell().setCursor(null);
				}
				try {
					SAFRApplication.getSAFRFactory().closeBackupDatabaseConnection();
				} catch (SAFRException e) {
					UIUtilities.handleWEExceptions(e,"Unexpected error occurred while closing backup DB connection.",null);
				}				
			}
		}
		return null;
	}

}
