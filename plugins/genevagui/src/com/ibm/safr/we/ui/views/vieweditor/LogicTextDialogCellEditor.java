package com.ibm.safr.we.ui.views.vieweditor;

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


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.ui.editors.logic.ECLLogicTextEditorInput;
import com.ibm.safr.we.ui.editors.logic.FCCLogicTextEditorInput;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditor;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditorInput;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.utilities.SAFRDialogCellEditor;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class LogicTextDialogCellEditor extends SAFRDialogCellEditor {

	private ViewEditor viewEditor;
	private ViewColumnSource viewColumnSource;
	private LogicTextType logicTextType;

	/**
	 * Use this constructor for View Dialog Cell editor of type <b>Format Phase
	 * Calculation </b> .
	 * 
	 * @param viewColumn
	 *            object for which View Dialog Cell editor is created.
	 * @param viewEditor
	 *            View editor from which View Dialog Cell editor is be opened.
	 */
	public LogicTextDialogCellEditor(Composite parent, ViewColumn viewColumn,
			ViewEditor viewEditor) {
		super(parent);
		this.logicTextType = LogicTextType.Format_Column_Calculation;
		this.viewEditor = viewEditor;
	}

	/**
	 * 
	 * Use this constructor for View Dialog Cell editor of type <b>Extract
	 * Column Assignment </b> .
	 * 
	 * @param viewColumnSource
	 *            object for which View Dialog Cell editor is created.
	 * @param viewEditor
	 *            View editor from which View Dialog Cell editor is be opened.
	 */

	public LogicTextDialogCellEditor(Composite parent,
			ViewColumnSource viewColumnSource, ViewEditor viewEditor) {
		super(parent);
		this.logicTextType = LogicTextType.Extract_Column_Assignment;
		this.viewColumnSource = viewColumnSource;
		this.viewEditor = viewEditor;

	}

	@Override
	protected void updateContents(Object value) {
		// TODO Auto-generated method stub
		super.updateContents(value);
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {

		LogicTextEditorInput input = null;
		if (logicTextType == LogicTextType.Format_Column_Calculation) {
			// for format column calculation logic text.
			input = new FCCLogicTextEditorInput(viewEditor.getCurrentColumn(), viewEditor);
		} else if (logicTextType == LogicTextType.Extract_Column_Assignment) {
			input = new ECLLogicTextEditorInput(viewColumnSource, viewEditor);
		}
		input.setLogicTextDialogCellEditor(this);
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().openEditor(input, LogicTextEditor.ID);
		} catch (PartInitException pie) {
			UIUtilities.handleWEExceptions(pie,"Unexpected error occurred while opening Logic Text editor.",null);
		}

		return null;
	}

	public void update(Object value) {
		updateContents(value);
	}
}
