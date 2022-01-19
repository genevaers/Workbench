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
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.State;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.ui.editors.view.ViewEditor;

public class EditViewHandler extends AbstractHandler implements IHandler {

	private static final String VIEWCOPY = "org.eclipse.ui.edit.copy";
	private static final String VIEWPASTELEFT = "org.eclipse.ui.edit.paste";
	private static final String VIEWPASTERIGHT = "SAFRWE.pastebelow-right";
	private static final String VIEWMAKESORTKEYCOL = "SAFRWE.viewMakeSortKeyCol";
	private static final String VIEWDELETE = "org.eclipse.ui.edit.delete";
	private static final String VIEWINSERTCOLUMNBEFORE = "SAFRWE.InsertColumnBefore";
    private static final String VIEWINSERTCOLUMNAFTER = "SAFRWE.InsertColumnAfter";
	private static final String VIEWINSERTDATASOURCE = "SAFRWE.viewInsertDataSource";
    private static final String VIEWCOPYDATASOURCE = "SAFRWE.viewCopyDataSource";
    private static final String VIEWCOLUMNGENERATOR = "SAFRWE.viewColumnGenerator";
	private static final String VIEWMOVELEFT = "SAFRWE.viewMoveLeft";
	private static final String VIEWMOVERIGHT = "SAFRWE.viewMoveRight";
	private static final String VIEWACTIVATE = "SAFRWE.viewActivate";
	private static final String VIEWTOGGLEGRID = "SAFRWE.toggleVETabs";
    private static final String VIEWGENLRFROM = "SAFRWE.genLRFromView";
	static ICommandService service;
	ViewEditor viewEditor;

	public Object execute(ExecutionEvent event) throws ExecutionException {
	    IEditorPart part = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActiveEditor();
	    if (part != null && part instanceof ViewEditor) {
	        viewEditor = (ViewEditor) part;
            if (event.getCommand().getId().equals(VIEWCOPY)) {
                viewEditor.copyViewColumns();
            } else if (event.getCommand().getId().equals(VIEWDELETE)) {
                viewEditor.delete();
            } else if (event.getCommand().getId().equals(VIEWINSERTCOLUMNBEFORE)) {
                viewEditor.insertColumnBefore(true);
            } else if (event.getCommand().getId().equals(VIEWINSERTCOLUMNAFTER)) {
                viewEditor.insertColumnAfter(true);
            } else if (event.getCommand().getId().equals(VIEWINSERTDATASOURCE)) {
                viewEditor.insertDataSource();
            } else if (event.getCommand().getId().equals(VIEWCOPYDATASOURCE)) {
                viewEditor.copyDataSource();
            } else if (event.getCommand().getId().equals(VIEWCOLUMNGENERATOR)) {
                viewEditor.columnGenerator();
            } else if (event.getCommand().getId().equals(VIEWMAKESORTKEYCOL)) {
                // state = getMakeSortKeyCol();
                // isSelected = !(Boolean) state.getValue();
                // state.setValue(isSelected);
                // service.refreshElements(event.getCommand().getId(), null);
                viewEditor.addRemoveSortKey();
    
            } else if (event.getCommand().getId().equals(VIEWPASTELEFT)) {
                viewEditor.pasteViewColumnsLeft();
            } else if (event.getCommand().getId().equals(VIEWPASTERIGHT)) {
                viewEditor.pasteViewColumnsRight();
            } else if (event.getCommand().getId().equals(VIEWMOVERIGHT)) {
                viewEditor.moveColumnRight();
    
            } else if (event.getCommand().getId().equals(VIEWMOVELEFT)) {
                viewEditor.moveColumnLeft();
    
            } else if (event.getCommand().getId().equals(VIEWACTIVATE)) {
                viewEditor.activateView();
            } else if (event.getCommand().getId().equals(VIEWTOGGLEGRID)) {
                viewEditor.toggleViewEditorTab();
            } else if (event.getCommand().getId().equals(VIEWGENLRFROM)) {
                viewEditor.genLRFromView();
            }
	    }
		return null;
	}

	public static State getMakeSortKeyCol() {
		service = (ICommandService) PlatformUI.getWorkbench().getService(
				ICommandService.class);
		Command command = service.getCommand("SAFRWE.viewMakeSortKeyCol");
		State state = command.getState("SAFRWE.MakeSortKeyColToggleState");
		return state;
	}

}
