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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class GroupEditor extends SAFREditorPart {

	public static String ID = "SAFRWE.GroupEditor";

	private Text textID;
	private Text textName;
	private Text textComments;
	Label labelCreatedValue;
	Label labelModifiedValue;

	private String defaultCreated = "-";
	private String defaultModified = "-";

	ScrolledForm form;
	FormToolkit toolkit;

	GroupEditorInput grp;
	Group group;
	Section sectionGroup;
	SAFRGUIToolkit safrToolkit;

	public boolean isSaveAsAllowed() {
		boolean retval = false;
		
		//if not dealing with a new component 
		//check with parent based on permissions
		if(group.getId() > 0) {
			retval = SAFRApplication.getUserSession().isSystemAdministrator() && saveAsEnabled;
		}
		return retval;
	}

	public void createPartControl(Composite parent) {
		grp = (GroupEditorInput) getEditorInput();
		group = grp.getGroup();

		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		safrToolkit = new SAFRGUIToolkit(toolkit);
		form.getBody().setLayout(UIUtilities.createTableLayout(1, false));

		createCompositeValues(form.getBody());
		refreshControls();
		dirty = false;
		// Used to load the context sensitive help
		if (group.getId() > 0) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(),
					"com.ibm.safr.we.help.GroupEditor");
		} else {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(),
					"com.ibm.safr.we.help.NewGroup");
		}
		ManagedForm mFrm = new ManagedForm(toolkit, form);
		setMsgManager(mFrm.getMessageManager());
	}

	private void createCompositeValues(Composite compositeValues) {

		sectionGroup = safrToolkit.createSection(compositeValues,
				Section.TITLE_BAR, "General Information");
		TableWrapData newEnvData = new TableWrapData(TableWrapData.FILL_GRAB);
		sectionGroup.setLayoutData(newEnvData);

		Composite groupNew = safrToolkit
				.createComposite(sectionGroup, SWT.NONE);
		groupNew.setLayout(UIUtilities.createTableLayout(3, true));

		safrToolkit.createLabel(groupNew, SWT.NONE, "ID:");

		textID = safrToolkit.createTextBox(groupNew, SWT.NONE);
		textID.setEnabled(false);

		textID.setLayoutData(UIUtilities.textTableData(1));

		safrToolkit.createLabel(groupNew, SWT.NONE, "");

		safrToolkit.createLabel(groupNew, SWT.NONE, "&Name:");

		textName = safrToolkit.createNameTextBox(groupNew, SWT.NONE);
        textName.setData(SAFRLogger.USER, "Name");                                        		
		textName.setLayoutData(UIUtilities.textTableData(2));
		textName.setTextLimit(UIUtilities.MAXNAMECHAR);
		textName.addModifyListener(this);

		safrToolkit.createLabel(groupNew, SWT.NONE,"C&omments:");

		textComments = safrToolkit.createCommentsTextBox(groupNew);
		textComments.setData(SAFRLogger.USER, "Comments");                                                		
		textComments.setLayoutData(UIUtilities.multiLineTextData(1, 2,
				textComments.getLineHeight() * 3));
		textComments.setTextLimit(UIUtilities.MAXCOMMENTCHAR);
		textComments.addModifyListener(this);

		safrToolkit.createLabel(groupNew, SWT.NONE,"Created:");

		labelCreatedValue = safrToolkit.createLabel(groupNew, SWT.NONE,
				defaultCreated);
		labelCreatedValue.setLayoutData(UIUtilities.textTableData(2));

		safrToolkit.createLabel(groupNew, SWT.NONE,"Last Modified:");

		labelModifiedValue = safrToolkit.createLabel(groupNew, SWT.NONE,
				defaultModified);
		labelModifiedValue.setLayoutData(UIUtilities.textTableData(2));

		sectionGroup.setClient(groupNew);

	}

	public void setFocus() {
		super.setFocus();
		textName.setFocus();
	}

	@Override
	public void doRefreshControls() {
		textID.setText(Integer.toString(group.getId()));
		if (group.getId() > 0) {
			textName.setText(group.getName());

			textComments.setText(group.getComment());
			labelCreatedValue.setText(group.getCreateBy() + " on "
					+ group.getCreateTimeString());
			labelModifiedValue.setText(group.getModifyBy() + " on "
					+ group.getModifyTimeString());

			form.setText("Edit Group");
		} else
			form.setText("New Group");

	}

	@Override
	public void refreshModel() {

		group.setName(textName.getText());

		group.setComment(textComments.getText());

	}

	@Override
	public String getModelName() {
		return "Group";
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
		group.store();
	}

	@Override
	public void validate() throws DAOException, SAFRException {
		group.validate();
		getMsgManager().removeAllMessages();
	}

	protected Control getControlFromProperty(Object property) {
		if (property == Group.Property.NAME) {
			return textName;
		} else if (property == Group.Property.COMMENT) {
			return textComments;
		}
		return null;
	}

	@Override
	public ComponentType getEditorCompType() {
		return ComponentType.Group;
	}

	@Override
	public SAFRPersistentObject getModel() {
		return group;
	}

	@Override
	public String getComponentNameForSaveAs() {
		return textName.getText();
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		if (sve.getErrorMessageMap().containsKey(
				com.ibm.safr.we.model.Group.Property.NAME)) {
			return true;
		}
		return false;
	}

}
