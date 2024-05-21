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


import org.eclipse.ui.IPersistableElement;

import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;

/**
 * An editor Input class for the Logic Text Editor.
 * 
 */
public class ECLLogicTextEditorInput extends LogicTextEditorInput {
	private ViewColumnSource viewColumnSource = null;
	
	public ECLLogicTextEditorInput(ViewColumnSource viewColumnSource, ViewEditor viewEditor) {
		super();
		this.logicTextType = LogicTextType.Extract_Column_Assignment;
		this.viewColumnSource = viewColumnSource;
		super.view = viewColumnSource.getView();
		super.viewEditor = viewEditor;
		((ViewEditorInput) this.viewEditor.getEditorInput())
				.getEditRights();
	}

	public LogicTextType getLogicTextType() {
		return logicTextType;
	}
	
	public String getLogicText() {
		return viewColumnSource.getExtractColumnAssignment();
	}

	public String getName() {
			return "ECL [" + view.getId() + "] - Column ("
					+ viewColumnSource.getViewColumn().getColumnNo()
					+ ") Source ("
					+ viewColumnSource.getViewSource().getSequenceNo() + ")";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "Extract-Phase Column Logic [" + view.getId()
				+ "] - Column ("
				+ viewColumnSource.getViewColumn().getColumnNo()
				+ ") Source ("
				+ viewColumnSource.getViewSource().getSequenceNo() + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}
		if (!(obj instanceof ECLLogicTextEditorInput))
			return false;
		ECLLogicTextEditorInput other = (ECLLogicTextEditorInput) obj;

		if ((view.getId().equals(other.getView().getId()))
				&& (this.logicTextType == other.logicTextType)) {
				return (this.viewColumnSource.getViewColumn()
						.equals(other.viewColumnSource.getViewColumn()))
						&& (this.viewColumnSource.getViewSource()
								.equals(other.viewColumnSource.getViewSource()));
		}
		return false;
	}

	public ViewSource getViewSource() {
		return viewColumnSource.getViewSource();
	}

	public ViewColumnSource getViewColumnSource() {
		return viewColumnSource;
	}

	@Override
	protected String getFormHeader() {
		return "Extract-Phase Column Logic";
	}

	@Override
	protected void saveLogicText(String text) {
		viewColumnSource.setExtractColumnAssignment(text);
	}

	@Override
	protected void validateLogicText(String text) throws SAFRException {
		viewColumnSource.validateExtractColumnAssignment(text);
	}

	@Override
	protected String getLogicTextFormTitle() {
		return "View: "
				+ view.getName()
				+ "["
				+ view.getId()
				+ "] - Column: "
				+ viewColumnSource.getViewColumn().getColumnNo()
				+ " Source: "
				+ viewColumnSource.getViewSource().getLrFileAssociation().getAssociatingComponentName()
				+ "."
				+ viewColumnSource.getViewSource().getLrFileAssociation().getAssociatedComponentName()
				+ " ("
				+ viewColumnSource.getViewSource().getSequenceNo() + ")";
	}

	@Override
	public boolean matches(SAFREnvironmentalComponent component) {
		return (viewColumnSource.getViewColumn().equals(component))
                || (viewColumnSource.getViewSource().equals(component))
                || (viewColumnSource.equals(component));
	}


}
