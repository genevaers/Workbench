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


import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;

/**
 * An editor Input class for the Logic Text Editor.
 * 
 */
public class FCCLogicTextEditorInput extends LogicTextEditorInput {
	private ViewColumn viewColumn = null;
	public FCCLogicTextEditorInput(ViewColumn viewColumn, ViewEditor viewEditor) {
		super();
		this.logicTextType = LogicTextType.Format_Column_Calculation;
		this.viewColumn = viewColumn;
		super.view = viewColumn.getView();
		super.viewEditor = viewEditor;
		super.editRights = ((ViewEditorInput) viewEditor.getEditorInput()).getEditRights();
	}

	public String getLogicText() {
		return viewColumn.getFormatColumnCalculation();
	}

	public String getName() {
		return "FCL [" + view.getId() + "] - Column (" + viewColumn.getColumnNo() + ")";
	}

	public String getToolTipText() {
		return "Format-Phase Column Logic [" + view.getId() + "]- Column ("	+ viewColumn.getColumnNo() + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}
		if (!(obj instanceof FCCLogicTextEditorInput))
			return false;
		FCCLogicTextEditorInput other = (FCCLogicTextEditorInput) obj;

		if ((view.getId().equals(other.getView().getId()))
				&& (this.logicTextType == other.logicTextType)) {
			if (logicTextType == LogicTextType.Format_Column_Calculation) {
				return this.viewColumn.equals(other.viewColumn);
			}
		}
		return false;
	}

	public ViewColumn getViewColumn() {
		return viewColumn;
	}

	@Override
	public String getFormHeader() {
		return "Format-Phase Column Logic";
	}

	@Override
	public void saveLogicText(String text) {
		viewColumn.setFormatColumnCalculation(text);
	}

	@Override
	public void validateLogicText(String text) throws SAFRException {
		viewColumn.validateFormatColumnCalculation(text);
	}

	@Override
	protected String getLogicTextFormTitle() {
		return "View: " + view.getName() + "["+ view.getId() + "] - Column: "+ viewColumn.getColumnNo();
	}

	@Override
	public boolean matches(SAFREnvironmentalComponent component) {
		return viewColumn.equals(component);
	}

}
