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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.utilities.VerifyNumericListener;
import com.ibm.safr.we.ui.utilities.WidgetType;

public class SearchMetadataDialog extends TitleAreaDialog implements Listener {

	private SAFRGUIToolkit safrGUIToolkit;
	private Button radioSearchById;
	private Button radioSearchByName;
	private Label searchTextLabel;
	VerifyNumericListener verifyNumeric = new VerifyNumericListener(
			WidgetType.INTEGER, false);

	private Text textSearch;
	private Composite compositeFindMetadata;
	private String searchText;
	private MetadataSearchCriteria searchCriteria;
	private static final int MAXIDTEXT = 10;
	private static final int MAXNAMETEXT = 48;
	private static final int MAXIDALPHANUMERIC = 8;
	private ComponentType componentType;

	/**
	 * Creates SearchMetadataDialog object
	 * 
	 * @param parentShell
	 *            : Shell
	 * @param componentType
	 *            : The type of the components in the list where search is
	 *            applied.
	 */
	public SearchMetadataDialog(Shell parentShell, ComponentType componentType) {
		super(parentShell);
		safrGUIToolkit = new SAFRGUIToolkit();
		this.componentType = componentType;

	}

	@Override
	protected void okPressed() {
		if (radioSearchById.getSelection()) {
			searchCriteria = MetadataSearchCriteria.ID;
		} else {
			searchCriteria = MetadataSearchCriteria.NAME;
		}
		if (textSearch.getText().equals("")) {
			searchText = "";
			this.close();
		} else {
			searchText = textSearch.getText();
			super.okPressed();
		}

	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		// CQ 7670. Nikita. To fix display issue when system font is increased.
		UIUtilities.setDialogBounds(getShell().getBounds().width, getShell()
				.getBounds().height, getShell());

		getShell().setText("Search Metadata Component");
		setTitle("Search component by ID or name");
		setMessage("", IMessageProvider.NONE);
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		createFindMetadataComposite(parent);
		return parent;
	}

	private void createFindMetadataComposite(Composite parent) {

		compositeFindMetadata = safrGUIToolkit
				.createComposite(parent, SWT.NONE);
		GridLayout grid1 = new GridLayout();

		compositeFindMetadata.setLayout(grid1);
		compositeFindMetadata.setLayout(new GridLayout(2, false));
		compositeFindMetadata.setLayoutData(new GridData(GridData.FILL_BOTH));
		radioSearchById = safrGUIToolkit.createRadioButton(
				compositeFindMetadata, "By &ID");
		radioSearchByName = safrGUIToolkit.createRadioButton(
				compositeFindMetadata, "By &Name");

		searchTextLabel = safrGUIToolkit.createLabel(compositeFindMetadata,
				SWT.NONE, "Search text:");

		searchTextLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false));
		GridData gridText = new GridData(SWT.FILL, SWT.NONE, true, true);

		textSearch = safrGUIToolkit.createTextBox(compositeFindMetadata,
				SWT.BORDER);
		textSearch.setTextLimit(MAXNAMETEXT);
		textSearch.setFocus();
		textSearch.setLayoutData(gridText);
		textSearch.addListener(SWT.FocusIn, this);
		radioSearchById.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				selectRadioSearchById();
			}
		});

		radioSearchByName.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (radioSearchByName.getSelection()) {
					if (verifyNumeric != null) {
						textSearch.removeVerifyListener(verifyNumeric);
					}
					textSearch.setText("");
					textSearch.setFocus();
					textSearch.setTextLimit(MAXNAMETEXT);
				}
			}
		});

		// CQ 9034. Nikita. Change the default radio button selection to 'By ID'.
		radioSearchById.setSelection(true);
		selectRadioSearchById();

	}

	private void selectRadioSearchById() {
		if (radioSearchById.getSelection()
				&& (componentType != null && !componentType
						.equals(ComponentType.User))) {
			textSearch.setText("");
			textSearch.setFocus();
			textSearch.addVerifyListener(verifyNumeric);
			textSearch.setTextLimit(MAXIDTEXT);
		} else {
			// if the table on which the search is applied contains a
			// list of users, then the text box should accept
			// alphanumeric values for ID and maximum allowable length
			// should be 8 characters.
			if (radioSearchById.getSelection()
					&& (componentType != null && componentType
							.equals(ComponentType.User))) {
				textSearch.setText("");
				textSearch.setFocus();
				textSearch.setTextLimit(MAXIDALPHANUMERIC);
			}
		}
	}

	public void handleEvent(Event event) {
		if (event.type == SWT.FocusIn) {
			if (event.widget instanceof Button) {
				setMessage(null);
			} else {
				setMessage(
						"Enter the full or partial ID or Name of the component to be searched.",
						IMessageProvider.INFORMATION);

			}
		}
	}

	/**
	 * This method is used to get the search text.s
	 * 
	 * @return search text
	 */
	public String getSearchText() {
		return searchText;
	}

	/**
	 * This method is used to get the search criteria.
	 * 
	 * @return search criteria
	 */
	public MetadataSearchCriteria getSearchCriteria() {
		return searchCriteria;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
	}
}
