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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.ui.editors.LookupPathEditor;
import com.ibm.safr.we.ui.editors.LookupPathEditorInput;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class LookupPathHandler extends AbstractHandler implements IHandler {

	private static final String OPENLOOKUPEDITOR = "SAFRWE.LookupPath";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (event.getCommand().getId().equals(OPENLOOKUPEDITOR)) {

			LookupPath lookupPath;
			try {
				lookupPath = SAFRApplication.getSAFRFactory()
						.createLookupPath();
				LookupPathEditorInput input = new LookupPathEditorInput(
						lookupPath, EditRights.ReadModify);
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
						.openEditor(input, LookupPathEditor.ID);
			} catch (DAOException de) {
				UIUtilities.handleWEExceptions(de,
				    "Unexpected database error occurred while retrieving data for creating a lookuppath.",UIUtilities.titleStringDbException);
			} catch (SAFRException se) {
				UIUtilities.handleWEExceptions(se,"Unexpected error occurred while creating a lookuppath.",null);
			} catch (PartInitException e) {
				UIUtilities.handleWEExceptions(e,"Unexpected error occurred while opening lookup path editor.",null);
			}
		}
		return null;
	}
}
