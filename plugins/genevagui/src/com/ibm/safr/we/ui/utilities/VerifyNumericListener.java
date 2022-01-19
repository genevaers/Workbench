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


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

/**
 * A class implementing {@link VerifyListener}.It is used to add SAFR
 * application specific verify listener for Text widgets.It checks whether text
 * in textbox is according to business logic.If textbox is of type:
 * <b>INTEGER</b> :It will accept only integer values. <b>DECIMAL</b> :It will
 * accept integer values and decimal point. <b>STRING</b> :It will accept
 * string.
 */
public class VerifyNumericListener implements VerifyListener {
	WidgetType type;
	Boolean allowNegatives;

	/**
	 * Constructor of {@link VerifyNumericListener#} with specified WidgetType
	 * as defined in {@link UIUtilities}.
	 * 
	 * @param type
	 *            of widget on which listener is to be added.
	 * @param allowNegatives
	 *            if negative values are allowed.
	 */
	public VerifyNumericListener(WidgetType type, Boolean allowNegatives) {
		this.type = type;
		this.allowNegatives = allowNegatives;
	}

	public void verifyText(VerifyEvent event) {
		event.doit = false;
		// check if the text to be set is numeric.
		String myText = event.text;
		String textData = ((Text) event.widget).getText();

		try {
			Double.parseDouble(myText);
			if ((this.type == WidgetType.INTEGER) && (myText.contains("."))) {
				event.doit = false;
			} else {
				event.doit = true;
			}
		} catch (NumberFormatException nfe) {
		}
		// Get the character typed
		char myChar = event.character;
		// check for type of widget.
		if (this.type == WidgetType.DECIMAL) {
			// Allow decimal point
			if (myChar == '.' && (!textData.contains("."))) {
				event.doit = true;
			}
		}
		// check for negative values too
		if (myChar == '-') {
			if (allowNegatives && (!textData.contains("-"))) {
				event.doit = true;
			}
		}
		// Allow 0-9,backspace,delete,left arrow,right arrow.
		if (Character.isDigit(myChar) || myChar == SWT.BS || myChar == SWT.DEL
				|| event.keyCode == SWT.ARROW_LEFT
				|| event.keyCode == SWT.ARROW_RIGHT
				|| event.keyCode == SWT.HOME || event.keyCode == SWT.END)
			event.doit = true;
	}

}
