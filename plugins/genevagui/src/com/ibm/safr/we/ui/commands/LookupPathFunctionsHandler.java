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

import com.ibm.safr.we.ui.editors.LookupPathEditor;

public class LookupPathFunctionsHandler extends AbstractHandler implements
		IHandler {
	private static final String ADDSOURCEFIELD = "SAFRWE.addSourceField";
	private static final String REMOVESOURCEFIELD = "org.eclipse.ui.edit.delete";
	private static final String MOVESOURCEFIELDUP = "SAFRWE.movelookupfieldup";
	private static final String MOVESOURCEFIELDDOWN = "SAFRWE.movelookupfielddown";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
				.getActiveEditor() instanceof LookupPathEditor) {
			LookupPathEditor lookupEditor = (LookupPathEditor) HandlerUtil
					.getActiveWorkbenchWindow(event).getActivePage()
					.getActiveEditor();
			if (event.getCommand().getId().equals(ADDSOURCEFIELD)) {
				lookupEditor.commandAddSourceField();
			} else if (event.getCommand().getId().equals(REMOVESOURCEFIELD)) {
				lookupEditor.commandRemoveSourceField();
			} else if (event.getCommand().getId().equals(MOVESOURCEFIELDUP)) {
				lookupEditor.commandMoveFieldUp();
			} else if (event.getCommand().getId().equals(MOVESOURCEFIELDDOWN)) {
				lookupEditor.commandMoveFieldDown();
			}

		}

		return null;
	}

}
