package com.ibm.safr.we.ui.editors.logic;

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


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;
import com.ibm.safr.we.ui.views.logic.LogicTextViewTreeNode;
import com.ibm.safr.we.ui.views.vieweditor.LogicTextDialogCellEditor;

/**
 * An editor Input class for the Logic Text Editor.
 * 
 */
public abstract class LogicTextEditorInput implements IEditorInput {
	protected LogicTextType logicTextType = null;
	protected Object[] expandedItems;
	protected TreePath[] expandedTreePaths;
	private LogicTextViewTreeNode previousLogicTextTreeInput;

	protected View view;
	protected ViewEditor viewEditor;
	private LogicTextDialogCellEditor logicTextDialogCellEditor;
	protected EditRights editRights;

	public EditRights getEditRights() {
		return editRights;
	}

	public void setEditRights(EditRights editRights) {
		this.editRights = editRights;
	}

	public View getView() {
		return view;
	}

	public LogicTextType getLogicTextType() {
		return logicTextType;
	}

	public void setLogicTextType(LogicTextType logicTextType) {
		this.logicTextType = logicTextType;
	}

	public Object[] getExpandedItems() {
		return expandedItems;
	}

	public void setExpandedItems(Object[] expandedItems) {
		this.expandedItems = expandedItems;
	}

	public TreePath[] getExpandedTreePaths() {
		return expandedTreePaths;
	}

	public void setExpandedTreePaths(TreePath[] expandedTreePaths) {
		this.expandedTreePaths = expandedTreePaths;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public abstract String getLogicText(); //{

	public abstract String getName(); //{

	public IPersistableElement getPersistable() {
		return null;
	}

	public abstract String getToolTipText();

	@SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public abstract boolean equals(Object obj); 

	/**
	 * This method returns previousLogicTextTreeInput which is set using .
	 * 
	 * @return
	 */
	public LogicTextViewTreeNode getPreviousLogicTextTreeInput() {
		return previousLogicTextTreeInput;
	}

	/**
	 * This method sets LogicText model used in the tree in the view for this
	 * {@link LogicTextEditorInput}
	 * 
	 * @param previousLogicTextTreeInput
	 *            Logic Text Model used for tree in the view for this type of
	 *            Logic text editorInput.
	 */
	public void setPreviousLogicTextTreeInput(
			LogicTextViewTreeNode previousLogicTextTreeInput) {
		this.previousLogicTextTreeInput = previousLogicTextTreeInput;
	}

	public ViewEditor getViewEditor() {
		return viewEditor;
	}

	/**
	 * Returns the {@link LogicTextDialogCellEditor} if it is set otherwise
	 * null.
	 * 
	 * @return {@link LogicTextDialogCellEditor} for the current
	 *         {@link LogicTextEditorInput}.
	 */
	public LogicTextDialogCellEditor getLogicTextDialogCellEditor() {
		return logicTextDialogCellEditor;
	}

	/**
	 * Sets the {@link LogicTextDialogCellEditor} for the current
	 * {@link LogicTextEditorInput}. It is only required if the Logic text
	 * editor opens after clicking on the {@link LogicTextDialogCellEditor} on
	 * view grid.
	 * 
	 * @param logicTextDialogCellEditor
	 *            to set for the current {@link LogicTextEditorInput}.
	 */
	public void setLogicTextDialogCellEditor(
			LogicTextDialogCellEditor logicTextDialogCellEditor) {
		this.logicTextDialogCellEditor = logicTextDialogCellEditor;
	}

	protected abstract String getFormHeader();

	protected abstract void saveLogicText(String text);

	protected abstract void validateLogicText(String text) throws SAFRException;

	protected abstract String getLogicTextFormTitle();

	public abstract boolean matches(SAFREnvironmentalComponent component);

}
