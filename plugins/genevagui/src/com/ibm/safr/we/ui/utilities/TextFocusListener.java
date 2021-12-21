package com.ibm.safr.we.ui.utilities;

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


import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

public class TextFocusListener implements FocusListener {

	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.we.ui.utilities.TextFocusListener");
	
	private Text text;
	boolean select = false;
	boolean already = false;
		
	public TextFocusListener(Text text, boolean select) {
		super();
		this.text = text;
		this.select = select;
	}

	public void focusGained(FocusEvent e) {
		if (select && !already) {
		    text.setSelection(0, text.getText().length());
		}
		already=false;
	}

	public void focusLost(FocusEvent e) {
		String str = text.getText();		
		Pattern p = Pattern.compile("[\\x00-\\x19|\\x7f-\\uffff&&[^\n\r]]+");
		Matcher m = p.matcher(str);
		if (m.find()) {
			text.setSelection(m.start(), m.end());
			already = true;
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), 
				"Invalid characters entered", 
				"The text field contains invalid characters that will cause the Performance Engine to fail, please change them");
			Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    text.setFocus();
                }			    
			});
		}
	}

}
