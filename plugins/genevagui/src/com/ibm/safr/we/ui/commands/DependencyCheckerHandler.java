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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DBType;
import com.ibm.safr.we.ui.editors.DependencyCheckerEditor;
import com.ibm.safr.we.ui.editors.DependencyCheckerEditorInput;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class DependencyCheckerHandler extends AbstractHandler implements
		IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(DAOFactoryHolder.getDAOFactory().getConnectionParameters().getType().equals(DBType.PostgresQL)) {
			MessageDialog.openInformation(HandlerUtil.getActiveWorkbenchWindow(event).getShell(),
					"Dependency Checker", "Dependency Checker not available via a Postgres database connection");
			return null;
		}
		DependencyCheckerEditorInput input = new DependencyCheckerEditorInput();
		try {
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().openEditor(input, DependencyCheckerEditor.ID);
		} catch (PartInitException e) {
			UIUtilities.handleWEExceptions(e,
			    "Unexpected error occurred while opening Dependency Checker editor.",null);
		}
		return null;
	}

}
