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


import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.ui.editors.view.ViewEditor;

/**
 * An editor Input class for the Logic Text Editor.
 * 
 */
public class FRFLogicTextEditorInput extends LogicTextEditorInput {
	public FRFLogicTextEditorInput(View view, ViewEditor viewEditor, EditRights editRights) {
		this.logicTextType = LogicTextType.Format_Record_Filter;
		super.view = view;
		super.viewEditor = viewEditor;
		super.editRights = editRights;
	}

	public String getLogicText() {
		return view.getFormatRecordFilter();
	}

	public String getName() {
		return "FRF [" + getView().getId() + "]";
	}

	public String getToolTipText() {
		return "Format-Phase Record Filter [" + getView().getId() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}
		if (!(obj instanceof FRFLogicTextEditorInput))
			return false;
		FRFLogicTextEditorInput other = (FRFLogicTextEditorInput) obj;

		if ((view.getId().equals(other.getView().getId()))
				&& (this.logicTextType == other.logicTextType)) {
			if (logicTextType == LogicTextType.Format_Record_Filter) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getFormHeader() {
		return "Format-Phase Record Filter";
	}

	@Override
	public void saveLogicText(String text) {
		view.setFormatRecordFilter(text);
	}

	@Override
	public void validateLogicText(String text) throws SAFRException {
		view.validateFormatRecordFilter(text);
	}

	@Override
	protected String getLogicTextFormTitle() {
		return "View: " + view.getName() + " [" + view.getId() + "]";
	}

	@Override
	public boolean matches(SAFREnvironmentalComponent component) {
		return view.equals(component);
	}

}
