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

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.ui.editors.UserEditor;
import com.ibm.safr.we.ui.editors.UserEditorInput;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class UpdateUserProfileHandler extends AbstractHandler implements
		IHandler {

	private UserEditorInput input;
	private String editorID;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			User loggedInUser = SAFRApplication.getUserSession().getUser();
			User user = SAFRApplication.getSAFRFactory().getUser(
					loggedInUser.getUserid());
			input = new UserEditorInput(user);
			editorID = UserEditor.ID;

			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().openEditor(input, editorID);
		} catch (PartInitException e) {
			UIUtilities.handleWEExceptions(e,"Unexpected error occurred while opening user editor.",null);
		} catch (SAFRNotFoundException snfe) {
			UIUtilities.handleWEExceptions(snfe,"Unable to retrieve the selected user details from the database.",null);
		} catch (SAFRException se) {
			UIUtilities.handleWEExceptions(se,"Unable to edit the selected user due to an unexpected error.",null);
		}
		return null;
	}

}
