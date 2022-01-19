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


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class SaveAsDialog extends TitleAreaDialog implements Listener {

	private Label labelNewName;
	private Text textNewName;
	private Composite compositeSaveAs;
	private SAFRGUIToolkit safrGUIToolkit;
	private String oldName;
	private String newName;
	private String executableName;
	private Text textExecutableName;
	private Label labelExecutableName;
	private Boolean user;
	private static final int MAX_EXECUTABLE = 18;

	public SaveAsDialog(Shell parentShell, String oldName) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER
				| SWT.APPLICATION_MODAL | SWT.RESIZE);
		safrGUIToolkit = new SAFRGUIToolkit();
		this.oldName = oldName;
	}

	public SaveAsDialog(Shell parentShell, String oldName, String executableName) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER
				| SWT.APPLICATION_MODAL | SWT.RESIZE);
		safrGUIToolkit = new SAFRGUIToolkit();
		this.oldName = oldName;
		this.executableName = executableName;
	}

	public SaveAsDialog(Shell parentShell, String oldName, Boolean user) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER
				| SWT.APPLICATION_MODAL | SWT.RESIZE);
		safrGUIToolkit = new SAFRGUIToolkit();
		this.oldName = oldName;
		this.user = user;
	}

	protected Control createDialogArea(Composite parent) {

		createCompositeSaveAs(parent);

		return parent;
	}

	private void createCompositeSaveAs(Composite parent) {
		compositeSaveAs = safrGUIToolkit.createComposite(parent, SWT.NONE);
		compositeSaveAs.setLayout(new GridLayout(2, false));
		compositeSaveAs.setLayoutData(new GridData(GridData.FILL_BOTH));

		labelNewName = safrGUIToolkit.createLabel(compositeSaveAs, SWT.NONE,
				"New Name:");
		textNewName = safrGUIToolkit.createNameTextBox(compositeSaveAs, SWT.BORDER);
		textNewName.setText(oldName);
		textNewName.setTextLimit(UIUtilities.MAXNAMECHAR);
		textNewName.setData(SAFRLogger.USER, "Enter a new name to use for the copied component");
		textNewName.addListener(SWT.FocusIn, this);

		labelExecutableName = safrGUIToolkit.createLabel(compositeSaveAs,
				SWT.NONE, "Executable Name:");
		textExecutableName = safrGUIToolkit.createTextBox(compositeSaveAs,
				SWT.BORDER);
		// If the component to be copied is an User then set the label to New
		// User ID and set the text limit to 8.
		if (user != null) {
			if (user) {
				textNewName.setTextLimit(8);
				labelNewName.setText("New User ID:");
			}
		}

		if (executableName != null) {
			labelExecutableName.setEnabled(true);
			textExecutableName.setEnabled(true);

			textExecutableName.setText(executableName);
			textExecutableName.setTextLimit(MAX_EXECUTABLE);
			textExecutableName
					.setData(SAFRLogger.USER, "Enter a new executable name to use for the copied component");
			textExecutableName.addListener(SWT.FocusIn, this);

		} else {
			labelExecutableName.setVisible(false);
			textExecutableName.setVisible(false);
		}
		GridData gridc2 = new GridData(GridData.FILL_HORIZONTAL);
		textNewName.setLayoutData(gridc2);
		textExecutableName.setLayoutData(gridc2);

	}

	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		setTitle("Save As");

		setMessage("Enter a new name to use for the copied component",
				IMessageProvider.INFORMATION);
		getShell().setText("Save As");

		return contents;
	}

	protected void createButtonsForButtonBar(Composite parent) {

		final Button saveAs = createButton(parent, IDialogConstants.CLOSE_ID,
				"&Save", true);

		textNewName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {

				if (textNewName.getText() == "") {
					saveAs.setEnabled(false);

				} else {
					saveAs.setEnabled(true);
				}

			}

		});

		textExecutableName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (executableName != null) {
					if (textExecutableName.getText() == "") {
						saveAs.setEnabled(false);

					} else {
						saveAs.setEnabled(true);
					}
				}

			}

		});

		saveAs.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				newName = textNewName.getText();
				if (textExecutableName.getVisible()) {
					executableName = textExecutableName.getText();
				}
				close();
			}
		});

		final Button cancel = createButton(parent, IDialogConstants.CANCEL_ID,
				"&Cancel", false);
		cancel.addListener(SWT.FocusIn, this);

	}

	/**
	 * @return the executableName
	 */
	public String getExecutableName() {
		return executableName;
	}

	/**
	 * @return the newName
	 */
	public String getNewName() {
		return newName;
	}

	public void handleEvent(Event event) {
		if (event.type == SWT.FocusIn) {

			if (event.widget instanceof Button) {
				setMessage(null);
			} else if (event.widget.getData() == null) {
				setMessage(null);
			} else {
				setMessage(event.widget.getData().toString(),
						IMessageProvider.INFORMATION);
			}
		}
	}
}
