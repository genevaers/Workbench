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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.ui.dialogs.SearchMetadataDialog;
import com.ibm.safr.we.ui.utilities.ISearchablePart;

public class SearchMetadataHandler extends AbstractHandler implements IHandler {
	SearchMetadataDialog searchMetadataDialog;
	ISearchablePart searchablePart;
	ComponentType componentType;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (HandlerUtil.getActiveShell(event).getData() instanceof ISearchablePart) {
			searchablePart = ((ISearchablePart) HandlerUtil.getActiveShell(
					event).getData());
		} else if (HandlerUtil.getActivePart(event) instanceof ISearchablePart) {
			searchablePart = ((ISearchablePart) HandlerUtil
					.getActivePart(event));
		}
		if (searchablePart != null) {
			componentType = searchablePart.getComponentType();
			searchMetadataDialog = new SearchMetadataDialog(HandlerUtil
					.getActiveShell(event), componentType);
			searchMetadataDialog.open();
			if (searchMetadataDialog.getReturnCode() == IDialogConstants.OK_ID) {
				try {
					HandlerUtil.getActiveShell(event).setCursor(
							Display.getCurrent().getSystemCursor(
									SWT.CURSOR_WAIT));
					if (!searchMetadataDialog.getSearchText().equals("")) {
						searchablePart.searchComponent(searchMetadataDialog
								.getSearchCriteria(), searchMetadataDialog
								.getSearchText());

					}
				} finally {
					HandlerUtil.getActiveShell(event).setCursor(null);
				}
			}
		}
		return null;
	}

}
