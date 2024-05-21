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

import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;

/**
 * An editor Input class for the Logic Text Editor.
 * 
 */
public class ERFLogicTextEditorInput extends LogicTextEditorInput {
	private ViewSource viewSource = null;

	public ERFLogicTextEditorInput(ViewSource viewSource, ViewEditor viewEditor) {
		super();
		this.logicTextType = LogicTextType.Extract_Record_Filter;
		this.viewSource = viewSource;
		super.view = viewSource.getView();
		super.viewEditor = viewEditor;
		((ViewEditorInput) viewEditor.getEditorInput()).getEditRights();
	}

	public String getLogicText() {
		return viewSource.getExtractRecordFilter();
	}

	@Override
	public String getName() {
		return "ERF [" + view.getId() + "] - Source (" + viewSource.getSequenceNo() + ")";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
			return "Extract-Phase Record Filter [" + view.getId() + "] - Source ("
					+ viewSource.getSequenceNo() + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}
		if (!(obj instanceof ERFLogicTextEditorInput))
			return false;
		ERFLogicTextEditorInput other = (ERFLogicTextEditorInput) obj;

		if ((view.getId().equals(other.getView().getId()))
				&& (this.logicTextType == other.logicTextType)) {
			if (logicTextType == LogicTextType.Extract_Record_Filter) {
				return this.viewSource.equals(other.viewSource);
			}
		}
		return false;
	}

	public ViewSource getViewSource() {
		return viewSource;
	}

	@Override
	protected String getFormHeader() {
		return "Extract-Phase Record Filter";
	}

	@Override
	protected void saveLogicText(String text) {
		viewSource.setExtractRecordFilter(text);
	}

	@Override
	protected void validateLogicText(String text) throws SAFRException {
		viewSource.validateExtractRecordFilter(text);
	}

	@Override
	protected String getLogicTextFormTitle() {
		return "View: "
				+ view.getName()
				+ "["
				+ view.getId()
				+ "] - Source: "
				+ viewSource.getLrFileAssociation().getAssociatingComponentName()
				+ "."
				+ viewSource.getLrFileAssociation().getAssociatedComponentName() + " ("
				+ viewSource.getSequenceNo()
				+ ")";
	}

	@Override
	public boolean matches(SAFREnvironmentalComponent component) {
		return viewSource.equals(component);
	}

}
