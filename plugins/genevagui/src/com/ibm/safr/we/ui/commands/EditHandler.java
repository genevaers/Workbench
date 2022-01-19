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


import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.State;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditor;

public class EditHandler extends AbstractHandler implements IHandler,
		IElementUpdater {

	private static boolean isSelected;

	private static final String COPY = "org.eclipse.ui.edit.copy";
	private static final String PASTE = "org.eclipse.ui.edit.paste";
	private static final String PASTEBELOW = "SAFRWE.pastebelow-right";
	private static final String DELETE = "org.eclipse.ui.edit.delete";
	private static final String INSERTFIELDBEFORE = "SAFRWE.InsertFieldBefore";
    private static final String INSERTFIELDAFTER = "SAFRWE.InsertFieldAfter";
	private static final String ALLROWS = "SAFRWE.allrows";
	private static final String FROMHIGHLIGHTEDROW = "SAFRWE.fromHighlightedRow";
    private static final String WITHINHIGHLIGHTEDROW = "SAFRWE.withinHighlightedRow";
	private static final String ONLYHIGHLIGHTEDROWS = "SAFRWE.onlyhighlightedrows";
    private static final String REGENREDEFINES = "SAFRWE.regenredefine";
	private static final String FINDFIELD = "org.eclipse.ui.edit.findReplace";
	private static final String MOVEUP = "SAFRWE.moveup";
	private static final String MOVEDOWN = "SAFRWE.movedown";

	State state;
	static ICommandService service;
	LogicalRecordEditor lrEditor;

	public Object execute(ExecutionEvent event) throws ExecutionException {

	    ApplicationMediator.getAppMediator().waitCursor();
		lrEditor = (LogicalRecordEditor) HandlerUtil.getActiveWorkbenchWindow(
				event).getActivePage().getActiveEditor();

        if (event.getCommand().getId().equals(COPY)) {
			lrEditor.editCopyRow();
		} else if (event.getCommand().getId().equals(PASTE)) {
			lrEditor.editPasteRowAboveSelected();
		} else if (event.getCommand().getId().equals(PASTEBELOW)) {
			lrEditor.editPasteRowBelowSelected();
		} else if (event.getCommand().getId().equals(DELETE)) {
			lrEditor.editRemoveRow();
		} else if (event.getCommand().getId().equals(INSERTFIELDBEFORE)) {
			lrEditor.editInsertFieldBefore();
        } else if (event.getCommand().getId().equals(INSERTFIELDAFTER)) {
            lrEditor.editInsertFieldAfter();
		} else if (event.getCommand().getId().equals(ALLROWS)) {
			lrEditor.editCalculateAllRows();
		} else if (event.getCommand().getId().equals(FROMHIGHLIGHTEDROW)) {
			lrEditor.editCalculateFromHighlightedRows();
        } else if (event.getCommand().getId().equals(WITHINHIGHLIGHTEDROW)) {
            lrEditor.editCalculateWithinHighlightedRow();
		} else if (event.getCommand().getId().equals(ONLYHIGHLIGHTEDROWS)) {
			lrEditor.editCalculateOnlyHighlightedRows();
        } else if (event.getCommand().getId().equals(REGENREDEFINES)) {
            lrEditor.editCalculateRedefines();
		} else if (event.getCommand().getId().equals(FINDFIELD)) {
			lrEditor.editFindField();
		} else if (event.getCommand().getId().equals(MOVEUP)) {
			lrEditor.moveUpRow();
		} else if (event.getCommand().getId().equals(MOVEDOWN)) {
			lrEditor.moveDownRow();
		}
        ApplicationMediator.getAppMediator().normalCursor();
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void updateElement(UIElement element, Map parameters) {
		element.setChecked(isSelected);

	}
}
