package com.ibm.safr.we.ui.editors;

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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.commands.SourceProvider;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.MetadataView;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * This class creates the editor area and also the controls for a new/existing
 * environment.
 * 
 */
public class EnvironmentEditor extends SAFREditorPart implements IPartListener2 {

	public static String ID = "SAFRWE.EnvironmentEditor";
	private Text textID;
	private Text textName;
	private Text textComments;
	private Text textControlRecordName;
	private Text textGroupName;
	private Label labelCreatedValue;
	private Label labelModifiedValue;

	private Section sectionNewEnv;
	private Section sectionDefaultInitParams;

	private String defaultControlRecordName = "Default_Install";
	private String defaultGroupName = "Administrators";
	private String defaultCreated = "-";
	private String defaultModified = "-";
	public Button enableCR;
	private ScrolledForm form;
	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;

	EnvironmentEditorInput environmentInput;
	Environment environment;

	@Override
	public void createPartControl(Composite parent) {
		// Current environment
		environmentInput = (EnvironmentEditorInput) getEditorInput();
		environment = environmentInput.getEnvironment();

		// Code to make the editor area scrollable
		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		safrGuiToolkit.setReadOnly(environmentInput.getEditRights() == EditRights.Read);
		form = toolkit.createScrolledForm(parent);

		form.getBody().setLayout(UIUtilities.createTableLayout(1, false));
		createGroupNewEnv(form.getBody());
		createGroupDefaultInitParams(form.getBody());

		// Load the values from model object
		refreshControls();
		setDirty(false);

		ManagedForm mFrm = new ManagedForm(toolkit, form);
		setMsgManager(mFrm.getMessageManager());
		getSite().getPage().addPartListener(this);
	}

	/**
	 * Method to create the controls in the "General Information" section of the
	 * environment editor
	 */
	private void createGroupNewEnv(Composite compositeValues) {
		sectionNewEnv = safrGuiToolkit.createSection(compositeValues,
				Section.TITLE_BAR, "General Information");

		sectionNewEnv.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		sectionNewEnv.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});

		Composite groupNewEnv = safrGuiToolkit.createComposite(sectionNewEnv,
				SWT.NONE);
		groupNewEnv.setLayout(UIUtilities.createTableLayout(3, true));

		safrGuiToolkit.createLabel(groupNewEnv, SWT.NONE, "ID:");

		textID = safrGuiToolkit.createTextBox(groupNewEnv, SWT.NONE);
		textID.setEnabled(false);
		textID.setLayoutData(UIUtilities.textTableData(1));

		safrGuiToolkit.createLabel(groupNewEnv, SWT.NONE, "");

		safrGuiToolkit.createLabel(groupNewEnv, SWT.NONE,"&Name:");

		textName = safrGuiToolkit.createNameTextBox(groupNewEnv, SWT.NONE);
		textName.setData(SAFRLogger.USER, "Name");																				
		textName.setLayoutData(UIUtilities.textTableData(2));
		textName.setTextLimit(UIUtilities.MAXNAMECHAR);
		textName.addModifyListener(this);

		safrGuiToolkit.createLabel(groupNewEnv, SWT.NONE,"C&omments:");
		textComments = safrGuiToolkit.createCommentsTextBox(groupNewEnv);
		textComments.setData(SAFRLogger.USER, "Comments");																						
		textComments.setLayoutData(UIUtilities.multiLineTextData(1, 2,
				textComments.getLineHeight() * 3));
		textComments.addModifyListener(this);

		safrGuiToolkit.createLabel(groupNewEnv, SWT.NONE,"Created:");
		labelCreatedValue = safrGuiToolkit.createLabel(groupNewEnv, SWT.NONE, defaultCreated);
		labelCreatedValue.setLayoutData(UIUtilities.textTableData(2));

		safrGuiToolkit.createLabel(groupNewEnv, SWT.NONE,"Last Modified:");
		labelModifiedValue = safrGuiToolkit.createLabel(groupNewEnv, SWT.NONE, defaultModified);
		labelModifiedValue.setLayoutData(UIUtilities.textTableData(2));

		sectionNewEnv.setClient(groupNewEnv);
	}

	/**
	 * Method to create the controls in the "Default Initialization Parameters"
	 * section of the environment editor
	 */
	private void createGroupDefaultInitParams(Composite compositeValues) {
		sectionDefaultInitParams = safrGuiToolkit.createSection(
				compositeValues, Section.DESCRIPTION | Section.TITLE_BAR,
				"Default Initialization Parameters");
		sectionDefaultInitParams
				.setDescription("Specify default parameters to initialize the new environment with:");
		sectionDefaultInitParams.getDescriptionControl().setEnabled(false);
		sectionDefaultInitParams.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));

		sectionDefaultInitParams.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});

		Composite groupDefaultInitParams = safrGuiToolkit.createComposite(sectionDefaultInitParams, SWT.NONE);

		groupDefaultInitParams.setLayout(UIUtilities.createTableLayout(3, true));

		enableCR = safrGuiToolkit.createCheckBox(groupDefaultInitParams,  "Generate a Control Record");
        enableCR.setSelection(true);
        enableCR.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {   
            	if(enableCR.getSelection()){
            		textControlRecordName.setEnabled(true);
            	}
            	else{
            		textControlRecordName.setEnabled(false);
            	}
            }
        });

	    
	    safrGuiToolkit.createTextBox(groupDefaultInitParams, SWT.NONE).setVisible(false);
	    safrGuiToolkit.createTextBox(groupDefaultInitParams, SWT.NONE).setVisible(false);

		safrGuiToolkit.createLabel(groupDefaultInitParams, SWT.NONE, "&Control Record Name:");

		textControlRecordName = safrGuiToolkit.createNameTextBox(
				groupDefaultInitParams, SWT.NONE);
		textControlRecordName.setText(defaultControlRecordName);
		textControlRecordName.setTextLimit(UIUtilities.MAXNAMECHAR);
		textControlRecordName.setLayoutData(UIUtilities.textTableData(2));
		textControlRecordName.addModifyListener(this);

		safrGuiToolkit.createLabel(groupDefaultInitParams, SWT.NONE, "&Group Name:");

		textGroupName = safrGuiToolkit.createNameTextBox(groupDefaultInitParams,
				SWT.NONE);
		textGroupName.setText(defaultGroupName);
		textGroupName.setTextLimit(UIUtilities.MAXNAMECHAR);
		textGroupName.setLayoutData(UIUtilities.textTableData(2));
		textGroupName.addModifyListener(this);
		textGroupName.setEnabled(false);
		
	    sectionDefaultInitParams.setClient(groupDefaultInitParams);
	}

	public void setFocus() {
		textName.setFocus();

	}

	/**
	 * Method to refresh the editor controls with the model object.
	 */

	@Override
	public void doRefreshControls() {
		UIUtilities
				.checkNullText(textID, Integer.toString(environment.getId()));
		if (environment.getId() > 0) {
			UIUtilities.checkNullText(textName, environment.getName());
			UIUtilities.checkNullText(textComments, environment.getComment());
			labelCreatedValue.setText(environment.getCreateBy() + " on "
					+ environment.getCreateTimeString());
			labelModifiedValue.setText(environment.getModifyBy() + " on "
					+ environment.getModifyTimeString());
			sectionDefaultInitParams.setVisible(false);
		} 
        if (environment.getName() == null) {
            form.setText("");           
        } else {
            form.setText(environment.getName());         
        }		
	}

	@Override
	public void refreshModel() {

		environment.setName(textName.getText());
		environment.setComment(textComments.getText());
		if (environment.getId() == 0) {
			environment.initialize(textControlRecordName.getText(),
					textGroupName.getText());
		}

	}

	@Override
	public String getModelName() {
		return "Environment";
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
		environment.setCrRequired(enableCR.getSelection());
		environment.store();

	}

	@Override
	public void validate() throws DAOException, SAFRException {
		environment.validate();
		getMsgManager().removeAllMessages();
	}

	/**
	 * This method is used to get the widget based on the property passed.
	 * 
	 * @param property
	 * @return the widget.
	 */
	protected Control getControlFromProperty(Object property) {
		if (property == Environment.Property.NAME) {
			return textName;
		} else if (property == Environment.Property.CR_NAME) {
			return textControlRecordName;
		} else if (property == Environment.Property.GROUP_NAME) {
			return textGroupName;
		} else if (property == Environment.Property.COMMENT) {
			return textComments;
		}
		return null;
	}

	@Override
	public ComponentType getEditorCompType() {
		return ComponentType.Environment;

	}

	@Override
	public SAFRPersistentObject getModel() {
		return environment;
	}

	@Override
	public String getComponentNameForSaveAs() {
		return textName.getText();
	}

	public void partActivated(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false).equals(this)) {
			SourceProvider sourceProvider = UIUtilities.getSourceProvider();
			sourceProvider.setEditorFocusLR(false);
			sourceProvider.setEditorFocusLookupPath(false);
			sourceProvider.setEditorFocusView(false);
			sourceProvider.setEditorFocusEnv(true);
		}

	}

	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void partClosed(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void partDeactivated(IWorkbenchPartReference partRef) {
		if (getSite().getWorkbenchWindow().getActivePage()
				.getEditorReferences() != null) {
			IEditorReference[] openEditors = getSite().getWorkbenchWindow()
					.getActivePage().getEditorReferences();

			if (partRef.getPart(false).equals(this)) {
				SourceProvider sourceProvider = UIUtilities.getSourceProvider();
				sourceProvider.setEditorFocusEnv(false);
				if (openEditors.length == 0) {
					sourceProvider.setEditorFocusLR(false);
					sourceProvider.setEditorFocusView(false);
					sourceProvider.setEditorFocusLookupPath(false);
				}

			}
		}

	}

	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void partOpened(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {

		super.dispose();
		getSite().getPage().removePartListener(this);
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		return null;
	}

	@Override
	public void refreshMetadataView() {
		TreeItemId metadataViewSelectionId = ApplicationMediator.getAppMediator()
				.getCurrentNavItem().getId();
		MetadataView metadataview = (MetadataView) (PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.findView(MetadataView.ID));

		if (metadataview != null) {
			if (metadataViewSelectionId.equals(TreeItemId.GROUP)
					|| metadataViewSelectionId.equals(TreeItemId.ENV)) {
				metadataview.refreshView();
			}
		}
	}
}
