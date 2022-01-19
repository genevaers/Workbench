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


import org.eclipse.swt.custom.StyledText;

/**
 * This class does special handling for the LogicTextEditor
 * so that undo operations are not recorded from LineStyler 
 * driven modifications.  
 */

public class LogicTextEditorLineStyler extends LogicTextLineStyler {

	LogicTextEditor editor = null;
	
	public LogicTextEditorLineStyler(LogicTextEditor editor) {
		super(editor.getParser());
		this.editor = editor;
	}
	
	@Override
	protected void replaceTextRange(StyledText sText, int start, int length, String word) {
		editor.setRecording(false);
		super.replaceTextRange(sText, start, length, word);
		editor.setRecording(true);
	}
}
