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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.model.utilities.NameConfirmWarningStrategy;
import com.ibm.safr.we.model.utilities.importer.DependentComponentNode;
import com.ibm.safr.we.ui.dialogs.NewComponentNameDialog;

/**
 * Provides a SAFR GUI-specific JFace implementation of the
 * ConfirmWarningStrategy interface. This implementation will display a
 * MessageDialog prompt to show the warning message and request confirmation to
 * proceed (OK/Cancel).
 * 
 */
public class SAFRGUINameConfirmWarningStrategy extends
		SAFRGUIConfirmWarningStrategy implements NameConfirmWarningStrategy {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy");

	private class UINewNamesRunnable implements Runnable {

		private String topic;
		private String shortMessage;
		private DependentComponentNode dcnRoot;
		private boolean okPressed = false;

		public UINewNamesRunnable(String topic, String shortMessage,
				DependentComponentNode dcnRoot) {
			this.topic = topic;
			this.shortMessage = shortMessage;
			this.dcnRoot = dcnRoot;
		}

		public void run() {
			okPressed = showDepMessageDialog(topic, shortMessage, dcnRoot);
		}

		public boolean isOkPressed() {
			return okPressed;
		}

		private boolean showDepMessageDialog(String topic, String shortMessage,
				DependentComponentNode dcnRoot) {

			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell();
			int confirm = 0;
			Cursor cursor = shell.getCursor();
			shell.setCursor(null);
			confirm = NewComponentNameDialog.openDependencyDialog(shell, topic,
					shortMessage, dcnRoot, MessageDialog.WARNING, new String[] {
							"&OK", "&Cancel" }, 0);
			shell.setCursor(cursor);

			return confirm == 0 ? true : false;
		}
	}

	public SAFRGUINameConfirmWarningStrategy(SAFRGUIContext context) {
		super(context);
	}

	public boolean correctNamesWarning(String topic, String shortMessage,
			DependentComponentNode dcnRoot) {
		UINewNamesRunnable warn = new UINewNamesRunnable(topic, shortMessage,
				dcnRoot);
		Display.getDefault().syncExec(warn);
		return warn.isOkPressed();
	}

}
