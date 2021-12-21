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

import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.ui.editors.UserEditor;
import com.ibm.safr.we.ui.editors.UserEditorInput;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class UserHandler extends AbstractHandler implements IHandler {

	private static final String NEWUSER = "SAFRWE.User";
	private static final String REFRESHDEFAULTS = "SAFRWE.sectionDefaultsRefresh";

	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (event.getCommand().getId().equals(NEWUSER)) {
			User user = SAFRApplication.getSAFRFactory().createUser();

			UserEditorInput input = new UserEditorInput(user);
			try {
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
						.openEditor(input, UserEditor.ID);
			} catch (PartInitException e) {
				UIUtilities.handleWEExceptions(e,"Unexpected error occurred while opening user editor.",null);
			}
		} else if (event.getCommand().getId().equals(REFRESHDEFAULTS)) {
			UserEditor userEditor = (UserEditor) HandlerUtil
					.getActiveWorkbenchWindow(event).getActivePage()
					.getActiveEditor();
			userEditor.refreshDefaults();
		}
		return null;
	}
}
