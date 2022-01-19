package com.ibm.safr.we.ui.dialogs;

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


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

/**
 * This is a dialog class used for displaying error messages.
 * 
 */
public class MultiErrorMessageDialog extends MessageDialog {
	private String dependencyList;
	Text messageText;
	private SAFRGUIToolkit guiToolkit = new SAFRGUIToolkit();

	/**
	 * Constructor for the MultiErrorMessageDialog class.
	 * 
	 * @param parentShell
	 *            parent shell where dialog is opened.
	 * @param dialogTitle
	 *            title to display for the dialog.
	 * @param dialogMessage
	 *            message to display in title bar of the dialog.
	 * @param errorMessage
	 *            error message to display in the dialog.
	 * @param dialogImageType
	 *            one of the following values:<li>MessageDialog.NONE for a
	 *            dialog with no image <li>MessageDialog.ERROR for a dialog with
	 *            an error image <li>MessageDialog.INFORMATION for a dialog with
	 *            an information image <li>MessageDialog.QUESTION for a dialog
	 *            with a question image <li>MessageDialog.WARNING for a dialog
	 *            with a warning image
	 * @param dialogButtonLabels
	 *            an array of the string containing names to display on the
	 *            dialog buttons.
	 * @param defaultIndex
	 *            index of the button on which default selection will be made.
	 */
	public MultiErrorMessageDialog(Shell parentShell, String dialogTitle,
			String dialogMessage, String errorMessage, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, null, dialogMessage, dialogImageType,
				dialogButtonLabels, defaultIndex);
		this.dependencyList = errorMessage;
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		createComposite(parent);
		getShell()
				.setBounds(
						(getShell().getParent().getBounds().width / 2) - 200,
						(getShell().getParent().getBounds().height / 2) - 150,
						400, 300);
		return parent;
	}

	private void createComposite(Composite parent) {
		parent.setLayout(new FillLayout());
		Composite composite = guiToolkit.createComposite(parent, SWT.BORDER);
		composite.setLayout(new FillLayout());
		messageText = new Text(composite, SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.BORDER);
		messageText.setEditable(false);
		messageText.setText(dependencyList);
	}

	/**
	 * Creates and opens the {@link MultiErrorMessageDialog}.
	 * 
	 * @param parentShell
	 *            parent shell where dialog is opened.
	 * @param dialogTitle
	 *            title to display for the dialog.
	 * @param dialogMessage
	 *            message to display in title bar of the dialog.
	 * @param errorMessage
	 *            error message to display in the dialog.
	 * @param dialogImageType
	 *            one of the following values:<li>MessageDialog.NONE for a
	 *            dialog with no image <li>MessageDialog.ERROR for a dialog with
	 *            an error image <li>MessageDialog.INFORMATION for a dialog with
	 *            an information image <li>MessageDialog.QUESTION for a dialog
	 *            with a question image <li>MessageDialog.WARNING for a dialog
	 *            with a warning image
	 * @param dialogButtonLabels
	 *            an array of the string containing names to display on the
	 *            dialog buttons.
	 * @param defaultIndex
	 *            index of the button on which default selection will be made.
	 * @return the window specific return code.
	 */
	public static int openMultiErrorMessageDialog(Shell parentShell,
			String dialogTitle, String dialogMessage, String errorMessage,
			int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		MultiErrorMessageDialog dialog = new MultiErrorMessageDialog(
				parentShell, dialogTitle, dialogMessage, errorMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
		return dialog.open();

	}

}
