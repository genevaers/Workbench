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
public class LogicTextEditorInput implements IEditorInput {
	private LogicTextType logicTextType = null;
	private Object[] expandedItems;
	private TreePath[] expandedTreePaths;
	private LogicTextViewTreeNode previousLogicTextTreeInput;

	private View view;
	private ViewColumn viewColumn = null;
	private ViewSource viewSource = null;
	private ViewColumnSource viewColumnSource = null;
	private ViewEditor viewEditor;
	private LogicTextDialogCellEditor logicTextDialogCellEditor;
	private EditRights editRights;

	/**
	 * Use this constructor for Logic text editor of type <b>Format Record
	 * Filter </b> .
	 * 
	 * @param view
	 *            object for which Logic text editor is created.
	 * @param viewEditor
	 *            for which Logic text editor is created.
	 */
	public LogicTextEditorInput(View view, ViewEditor viewEditor,
			EditRights editRights) {
		this.logicTextType = LogicTextType.Format_Record_Filter;
		this.view = view;
		this.viewEditor = viewEditor;
		this.editRights = editRights;
	}

	public EditRights getEditRights() {
		return editRights;
	}

	public void setEditRights(EditRights editRights) {
		this.editRights = editRights;
	}

	/**
	 * Use this constructor for Logic text editor of type <b>Extract Record
	 * Filter </b> .
	 * 
	 * @param viewSource
	 *            object for which Logic text editor is created.
	 * @param viewEditor
	 *            for which Logic text editor is created.
	 */
	public LogicTextEditorInput(ViewSource viewSource, ViewEditor viewEditor, LogicTextType logicTextType) {
		super();
		this.logicTextType = logicTextType;
		this.viewSource = viewSource;
		this.view = viewSource.getView();
		this.viewEditor = viewEditor;
		this.editRights = ((ViewEditorInput) this.viewEditor.getEditorInput()).getEditRights();
	}

	/**
	 * Use this constructor for Logic text editor of type <b>Format Phase
	 * Calculation </b> .
	 * 
	 * @param viewColumn
	 *            object for which Logic text editor is created.
	 * @param viewEditor
	 *            View editor from which Logic text editor is be opened.
	 */
	public LogicTextEditorInput(ViewColumn viewColumn, ViewEditor viewEditor) {
		super();
		this.logicTextType = LogicTextType.Format_Column_Calculation;
		this.viewColumn = viewColumn;
		this.view = viewColumn.getView();
		this.viewEditor = viewEditor;
		this.editRights = ((ViewEditorInput) this.viewEditor.getEditorInput())
				.getEditRights();
	}

	/**
	 * 
	 * Use this constructor for Logic text editor of type <b>Extract Column
	 * Assignment </b> .
	 * 
	 * @param viewColumnSource
	 *            object for which Logic text editor is created.
	 * @param viewEditor
	 *            View editor from which Logic text editor is be opened.
	 */

	public LogicTextEditorInput(ViewColumnSource viewColumnSource,
			ViewEditor viewEditor) {
		super();
		this.logicTextType = LogicTextType.Extract_Column_Assignment;
		this.viewColumnSource = viewColumnSource;
		this.view = viewColumnSource.getView();
		this.viewEditor = viewEditor;
		this.editRights = ((ViewEditorInput) this.viewEditor.getEditorInput())
				.getEditRights();
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

	public String getName() {
		logicTextType = getLogicTextType();
		if (logicTextType == LogicTextType.Extract_Record_Filter) {
			return "ERF [" + view.getId() + "] - Source ("
					+ viewSource.getSequenceNo() + ")";
		} else if (logicTextType == LogicTextType.Extract_Record_Output) {
            return "ERL [" + view.getId() + "] - Source ("
                    + viewSource.getSequenceNo() + ")";
		} else if (logicTextType == LogicTextType.Extract_Column_Assignment) {
			return "ECL [" + view.getId() + "] - Column ("
					+ viewColumnSource.getViewColumn().getColumnNo()
					+ ") Source ("
					+ viewColumnSource.getViewSource().getSequenceNo() + ")";
		} else if (logicTextType == LogicTextType.Format_Column_Calculation) {
			return "FCL [" + view.getId() + "] - Column ("
					+ viewColumn.getColumnNo() + ")";
		} else if (logicTextType == LogicTextType.Format_Record_Filter) {
			return "FRF [" + getView().getId() + "]";
		}
		return "";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {

		logicTextType = getLogicTextType();
		if (logicTextType == LogicTextType.Extract_Record_Filter) {
			return "Extract-Phase Record Filter [" + view.getId() + "] - Source ("
					+ viewSource.getSequenceNo() + ")";
		} else if (logicTextType == LogicTextType.Extract_Record_Output) {
            return "Extract-Phase Record Logic [" + view.getId() + "] - Source ("
                    + viewSource.getSequenceNo() + ")";
		} else if (logicTextType == LogicTextType.Extract_Column_Assignment) {
			return "Extract-Phase Column Logic [" + view.getId()
					+ "] - Column ("
					+ viewColumnSource.getViewColumn().getColumnNo()
					+ ") Source ("
					+ viewColumnSource.getViewSource().getSequenceNo() + ")";
		} else if (logicTextType == LogicTextType.Format_Column_Calculation) {
			return "Format-Phase Column Logic [" + view.getId() + "]- Column ("
					+ viewColumn.getColumnNo() + ")";
		} else if (logicTextType == LogicTextType.Format_Record_Filter) {
			return "Format-Phase Record Filter [" + getView().getId() + "]";
		}
		return "";
	}

	@SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}
		if (super.equals(obj))
			return true;
		if (!(obj instanceof LogicTextEditorInput))
			return false;
		LogicTextEditorInput other = (LogicTextEditorInput) obj;

		if ((view.getId().equals(other.getView().getId()))
				&& (this.logicTextType == other.logicTextType)) {
			logicTextType = getLogicTextType();
			if (logicTextType == LogicTextType.Extract_Record_Filter || 
			    logicTextType == LogicTextType.Extract_Record_Output) {
				return this.viewSource.equals(other.viewSource);
			} else if (logicTextType == LogicTextType.Extract_Column_Assignment) {
				return (this.viewColumnSource.getViewColumn()
						.equals(other.viewColumnSource.getViewColumn()))
						&& (this.viewColumnSource.getViewSource()
								.equals(other.viewColumnSource.getViewSource()));
			} else if (logicTextType == LogicTextType.Format_Column_Calculation) {
				return this.viewColumn.equals(other.viewColumn);
			} else if (logicTextType == LogicTextType.Format_Record_Filter) {
				return true;
			}
		}
		return false;
	}

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

	public ViewColumn getViewColumn() {
		return viewColumn;
	}

	public ViewSource getViewSource() {
		return viewSource;
	}

	public ViewColumnSource getViewColumnSource() {
		return viewColumnSource;
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

}
