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


import java.util.List;

import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.IMenuService;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRAssociationList;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationTableSorter;

public class NewViewSourceDialog extends TitleAreaDialog {

	private SAFRGUIToolkit safrGUIToolkit;
	private TableCombo comboLogicalRecord;
	private TableCombo comboLogicalFile;
	private Composite compositeViewSource;

	private String selectedLogicalRecord = "";

	ComponentAssociation LRLFAssoc = null;
	private SAFRAssociationList<ComponentAssociation> logicalFileAssociations = new SAFRAssociationList<ComponentAssociation>();
	private TableComboViewer comboLogicalRecordViewer;

	private List<LogicalRecordQueryBean> logicalRecords;
	private TableComboViewer comboLogicalfileViewer;

	public NewViewSourceDialog(Shell parentShell) {
		super(parentShell);
		safrGUIToolkit = new SAFRGUIToolkit();
		LRLFAssoc = null;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		// CQ 7670. Nikita. To fix display issue when system font is increased.
		UIUtilities.setDialogBounds(getShell().getBounds().width, getShell()
				.getBounds().height, getShell());

		getShell().setText("Add View Source");
		setTitle("Select Logical Record/ Logical File Pair");
		setMessage(
				"Select a Logical Record / Logical File pair to create a new View Source.",
				IMessageProvider.NONE);
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		CoolBar coolbar = new CoolBar(parent, SWT.FLAT);
		CoolBarManager coolManager = new CoolBarManager(coolbar);
		IMenuService m = (IMenuService) PlatformUI.getWorkbench().getService(
				IMenuService.class);

		// CQ 8370. Nikita. 11/08/2010. Search button should not appear in
		// Insert View Source dialog as its not applicable here.
		m.populateContributionManager(coolManager,
				"toolbar:SAFRWE.FilterDialogCoolBar");
		coolManager.update(true);
		createViewSourceComposite(parent);
		return parent;
	}

	private void createViewSourceComposite(Composite parent) {
		compositeViewSource = safrGUIToolkit.createComposite(parent, SWT.NONE);
		compositeViewSource.setLayout(new GridLayout(2, false));
		compositeViewSource.setLayoutData(new GridData(GridData.FILL_BOTH));
		safrGUIToolkit.createLabel(compositeViewSource,
				SWT.NONE, "Logical &Record:");

		comboLogicalRecordViewer = safrGUIToolkit
				.createTableComboForComponents(compositeViewSource,
						ComponentType.LogicalRecord);
		comboLogicalRecord = comboLogicalRecordViewer.getTableCombo();

		GridData gridc1 = new GridData(GridData.FILL_HORIZONTAL);
		gridc1.horizontalSpan = 1;
		comboLogicalRecord.setLayoutData(gridc1);

		// load the data
		try {
			logicalRecords = SAFRQuery.queryAllActiveLogicalRecords(UIUtilities
					.getCurrentEnvironmentID(), SortType.SORT_BY_NAME);

		} catch (DAOException e1) {
			UIUtilities.handleWEExceptions(e1,
					"Error occurred while retrieving all logical records.",
					UIUtilities.titleStringDbException);
		}
		Integer counter = 0;
		if (logicalRecords != null) {
			for (LogicalRecordQueryBean logicalRecordQueryBean : logicalRecords) {
				comboLogicalRecord.setData(Integer.toString(counter),
						logicalRecordQueryBean);
				counter++;
			}
		}

		comboLogicalRecordViewer.setInput(logicalRecords);

		safrGUIToolkit.createLabel(compositeViewSource,
				SWT.NONE, "Logical &File:");

		comboLogicalfileViewer = safrGUIToolkit
				.createTableComboForAssociatedComponents(compositeViewSource);

		comboLogicalFile = comboLogicalfileViewer.getTableCombo();

		GridData gridc2 = new GridData(GridData.FILL_HORIZONTAL);
		gridc2.horizontalSpan = 1;
		comboLogicalFile.setLayoutData(gridc2);

		comboLogicalRecord.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				if (!(comboLogicalRecord.getText().equals(selectedLogicalRecord))) {
					populateLogicalFileCombo();
					comboLogicalfileViewer.setSorter(new ComponentAssociationTableSorter(1,SWT.UP));
					selectedLogicalRecord = comboLogicalRecord.getText();
				}
			}
		});
	}

	@Override
	protected void okPressed() {
		if (comboLogicalFile.getTable().getSelection().length > 0) {
			LRLFAssoc = (ComponentAssociation) comboLogicalFile.getTable()
					.getSelection()[0].getData();
		}
		if (LRLFAssoc == null) {
			MessageDialog
					.openError(getShell(), "Error",
							"Please select a Logical File/Logical Record pair to continue");
		} else {
			super.okPressed();
		}
	}

	public ComponentAssociation getLRLFAssociation() {
		return LRLFAssoc;
	}

	private void populateLogicalFileCombo() {
		try {
			comboLogicalFile.getTable().deselectAll();

			LogicalRecordQueryBean logicalRecordBean = (LogicalRecordQueryBean) comboLogicalRecord
					.getTable().getSelection()[0].getData();
			if (logicalRecordBean != null) {

				logicalFileAssociations = SAFRAssociationFactory
						.getLogicalRecordToLogicalFileAssociations(
								logicalRecordBean.getId(), UIUtilities
										.getCurrentEnvironmentID());

				Integer counter = 0;
				comboLogicalFile.getTable().removeAll();
				comboLogicalFile.select(-1);
				comboLogicalfileViewer.setInput(logicalFileAssociations);
				comboLogicalfileViewer.refresh();

				for (ComponentAssociation association : logicalFileAssociations) {
					// This method stores the ComponentAssociation object
					comboLogicalFile.setData(Integer.toString(counter),
							association);
					counter++;
				}
			}
		} catch (DAOException de) {
			UIUtilities.handleWEExceptions(de,
					"Unexpected database error occurred while retrieving data for logical record"
							+ " to logical file association.",
					UIUtilities.titleStringDbException);
		} catch (SAFRException se) {
			UIUtilities.handleWEExceptions(se);
		}

	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
	}
}
