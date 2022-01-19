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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.ui.dialogs.DependencyMessageDialog;
import com.ibm.safr.we.ui.dialogs.MultiErrorMessageDialog;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class OpenRecentView extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Event e = (Event) event.getTrigger();
		String viewClicked = ((MenuItem) e.widget).getText();
		String viewId = viewClicked.substring(viewClicked.indexOf('[') + 1,
				viewClicked.indexOf(']')).trim(); // parse the view id from the
		// string
		int id = Integer.parseInt(viewId);
		// open view
		View view = null;
		ViewEditorInput input = null;
		try {
			view = SAFRApplication.getSAFRFactory().getView(id);
			if (SAFRApplication.getUserSession().isSystemAdministrator()) {
                input = new ViewEditorInput(view, EditRights.ReadModifyDelete);
			} else {
                EditRights rights = SAFRApplication.getUserSession()
                    .getEditRights(ComponentType.View, id);
                input = new ViewEditorInput(view, rights);
			}
			// Retrieve the warnings, if present, to be
			// displayed after loading of component
			String err = "";
			for (String msg : view.getLoadWarnings()) {
				err += msg + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK;
			}
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().openEditor(input,
					ViewEditor.ID);
			if (err != null && !err.equals("")) {
				MultiErrorMessageDialog
						.openMultiErrorMessageDialog(
								Display.getCurrent().getActiveShell(),
								"Warnings",
								"Following problems were found while loading this component.",
								err, MessageDialog.WARNING,
								new String[] { "OK" }, 0);
			}
		} catch (SAFRDependencyException de) {
			DependencyMessageDialog.openDependencyDialog(HandlerUtil
					.getActiveShell(event), "Inactive Dependencies",
					"The View could not be loaded because the following component(s) are inactive."
							+ " Please reactivate these and try again.", de
							.getDependencyString(), MessageDialog.ERROR,
					new String[] { IDialogConstants.OK_LABEL }, 0);
		} catch (SAFRException se) {
			UIUtilities.handleWEExceptions(se,
					"Unexpected error occurred while opening the view.", null);
		} catch (PartInitException pe) {
			UIUtilities.handleWEExceptions(pe,
					"Unexpected error occurred while opening the view.", null);
		}
		return null;
	}

}
