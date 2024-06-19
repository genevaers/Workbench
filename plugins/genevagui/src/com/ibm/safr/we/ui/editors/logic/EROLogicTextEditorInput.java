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
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;

/**
 * An editor Input class for the Logic Text Editor.
 * 
 */
public class EROLogicTextEditorInput extends LogicTextEditorInput {
	private ViewSource viewSource = null;
	
	public EROLogicTextEditorInput(ViewSource viewSource, ViewEditor viewEditor) {
		super();
		this.logicTextType = LogicTextType.Extract_Record_Output;
		this.viewSource = viewSource;
		super.view = viewSource.getView();
		super.viewEditor = viewEditor;
		super.editRights = ((ViewEditorInput) viewEditor.getEditorInput()).getEditRights();
	}

	public String getLogicText() {
		return viewSource.getExtractRecordOutput();
	}

	public String getName() {
        return "ERL [" + view.getId() + "] - Source (" + viewSource.getSequenceNo() + ")";
	}

	public String getToolTipText() {
            return "Extract-Phase Record Logic [" + view.getId() + "] - Source ("
                    + viewSource.getSequenceNo() + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}
		if (!(obj instanceof EROLogicTextEditorInput))
			return false;
		EROLogicTextEditorInput other = (EROLogicTextEditorInput) obj;

		if ((view.getId().equals(other.getView().getId()))
				&& (this.logicTextType == other.logicTextType)) {
			logicTextType = getLogicTextType();
			if (logicTextType == LogicTextType.Extract_Record_Output) {
				return this.viewSource.equals(other.viewSource);
			}
		}
		return false;
	}

	public ViewSource getViewSource() {
		return viewSource;
	}

	@Override
	public String getFormHeader() {
		return "Extract-Phase Record Logic";
	}

	@Override
	public void saveLogicText(String text) {
		viewSource.setExtractRecordOutput(text);
	}

	@Override
	public void validateLogicText(String text) throws SAFRException {
		viewSource.validateExtractRecordOutput(text);		
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
