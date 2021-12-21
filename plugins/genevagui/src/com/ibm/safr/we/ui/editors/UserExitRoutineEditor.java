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


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.dialogs.DependencyMessageDialog;
import com.ibm.safr.we.ui.dialogs.SaveAsDialog;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class UserExitRoutineEditor extends SAFREditorPart {

	public static String ID = "SAFRWE.UserExitRoutineEditor";
	private String defaultCreated = "-";
	private String defaultModified = "-";

	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;
	private ScrolledForm form;
	private Section sectionGeneral;
	private Composite compositeGeneral;
	private Text textID;
	private Combo comboType;
	private Text textName;
	private Combo comboLanguage;
	private Text textExecutable;
	private Button checkOptimize;
	private Text textComments;
	private Label labelCreatedValue;
	private Label labelModifiedValue;

	private String selectedType = "";
	private String selectedLangauge = "";

	private static final int MAX_EXECUTABLE = 18;
	private static final int MAX_NAME = 48;

	UserExitRoutineEditorInput userExitRoutineInput;
	UserExitRoutine userExitRoutine;
	String copyName = null;
	String copyExecutable = null;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
	}

	@Override
	public boolean isSaveAsAllowed() {
		boolean retval = false;
		
		//if not dealing with a new component 
		//check with parent based on permissions
		if(userExitRoutine.getId() > 0) {
			retval = isSaveAsAllowed(Permissions.CreateUserExitRoutine);
		}
		return retval;
	}

	@Override
	public SAFRPersistentObject saveComponentCopy()
			throws SAFRValidationException, SAFRException {
		SAFRComponent copy = null;
		SaveAsDialog saveAsDialog;
		// open the save as dialog with the last name and executable entered by
		// the user.
		if (copyName == null || copyExecutable == null) {
			saveAsDialog = new SaveAsDialog(this.getSite().getShell(), textName
					.getText(), textExecutable.getText());
		} else {
			saveAsDialog = new SaveAsDialog(this.getSite().getShell(),
					copyName, copyExecutable);
		}
		int returnCode = saveAsDialog.open();

		if (returnCode == IDialogConstants.OK_ID) {
			try {
				getSite().getShell().setCursor(
						getSite().getShell().getDisplay().getSystemCursor(
								SWT.CURSOR_WAIT));
				copyName = saveAsDialog.getNewName();
				copyExecutable = saveAsDialog.getExecutableName();
				copy = userExitRoutine.saveAs(copyName, copyExecutable);
			} finally {
				getSite().getShell().setCursor(null);
			}
		}

		// set the name ans executable as null once the save as is done.
		copyName = null;
		copyExecutable = null;
		return copy;

	}

	@Override
	public void createPartControl(Composite parent) {

		userExitRoutineInput = (UserExitRoutineEditorInput) getEditorInput();
		userExitRoutine = userExitRoutineInput.getUserExitRoutine();

		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		safrGuiToolkit.setReadOnly(userExitRoutineInput.getEditRights() == EditRights.Read);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(UIUtilities.createTableLayout(1, false));
		createSectionGeneral(form.getBody());
		refreshControls();
		setDirty(false);
		// Used to load the context sensitive help
		if (userExitRoutine.getId() > 0) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(),
					"com.ibm.safr.we.help.User-ExitRoutineEditor");
		} else {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(),
					"com.ibm.safr.we.help.NewUser-Exit Routine");
		}
		ManagedForm mFrm = new ManagedForm(toolkit, form);
		setMsgManager(mFrm.getMessageManager());
	}

	private void createSectionGeneral(Composite body) {
		sectionGeneral = safrGuiToolkit.createSection(body, Section.TITLE_BAR,"General Information");
		sectionGeneral.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		compositeGeneral = safrGuiToolkit.createComposite(sectionGeneral,SWT.NONE);
		compositeGeneral.setLayout(UIUtilities.createTableLayout(6, false));

		safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE,"ID:");

		textID = safrGuiToolkit.createTextBox(compositeGeneral, SWT.NONE);
		textID.setEnabled(false);
		textID.setLayoutData(UIUtilities.textTableData(1));
		textID.addModifyListener(this);

		Label filler1 = safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE, "");
		filler1.setLayoutData(UIUtilities.textTableData(4));

		safrGuiToolkit.createLabel(compositeGeneral,SWT.NONE, "&Name:");

		textName = safrGuiToolkit.createNameTextBox(compositeGeneral, SWT.NONE);
		textName.setData(SAFRLogger.USER, "Name");										
		textName.setLayoutData(UIUtilities.textTableData(3));
		textName.setTextLimit(MAX_NAME);
		textName.addModifyListener(this);

		Label filler3 = safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE, "");
		filler3.setLayoutData(UIUtilities.textTableData(2));

		safrGuiToolkit.createLabel(compositeGeneral,SWT.NONE, "&Type:");

		comboType = safrGuiToolkit.createComboBox(compositeGeneral, SWT.READ_ONLY, "");
		comboType.setData(SAFRLogger.USER, "Type");												
		comboType.setLayoutData(UIUtilities.textTableData(1));
		UIUtilities.populateComboBox(comboType, CodeCategories.EXITTYPE, -1, false);
		comboType.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				super.focusLost(e);
				if (!(comboType.getText().equals(selectedType))) {
					setDirty(true);
					selectedType = comboType.getText();
				}

			}

		});

		checkOptimize = safrGuiToolkit.createCheckBox(compositeGeneral, "Optimi&ze");
		checkOptimize.setData(SAFRLogger.USER, "Optimize");	
		checkOptimize.setLayoutData(new TableWrapData(TableWrapData.RIGHT));
		checkOptimize.setEnabled(false);
		checkOptimize.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
			}

		});

		Label filler2 = safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE, "");
		filler2.setLayoutData(UIUtilities.textTableData(3));

		safrGuiToolkit.createLabel(compositeGeneral,SWT.NONE, "&Language:");

		comboLanguage = safrGuiToolkit.createComboBox(compositeGeneral, SWT.READ_ONLY, "");
		comboLanguage.setData(SAFRLogger.USER, "Language");	
		comboLanguage.setLayoutData(UIUtilities.textTableData(1));
		UIUtilities.populateComboBox(comboLanguage, CodeCategories.PROGTYPE, -1, false);
		comboLanguage.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				super.focusLost(e);
				if (!(comboLanguage.getText().equals(selectedLangauge))) {
					setDirty(true);
					selectedLangauge = comboLanguage.getText();
				}

			}

		});
		
        Label filler4 = safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE, "");
        filler4.setLayoutData(UIUtilities.textTableData(4));		

		safrGuiToolkit.createLabel(compositeGeneral,SWT.NONE, "&Executable:");

		textExecutable = safrGuiToolkit.createTextBox(compositeGeneral, SWT.NONE);
		textExecutable.setData(SAFRLogger.USER, "Executable");										
		textExecutable.setLayoutData(UIUtilities.textTableData(3));
		textExecutable.setTextLimit(MAX_EXECUTABLE);
		textExecutable.addModifyListener(this);

		Label filler6 = safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE, "");
		filler6.setLayoutData(UIUtilities.textTableData(2));

		safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE, "C&omments:");

		textComments = safrGuiToolkit.createCommentsTextBox(compositeGeneral);
		textComments.setData(SAFRLogger.USER, "Comments");												
		textComments.setLayoutData(UIUtilities.multiLineTextData(1, 3,
				textComments.getLineHeight() * 3));
		textComments.setTextLimit(UIUtilities.MAXCOMMENTCHAR);
		textComments.addModifyListener(this);

		Label filler7 = safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE, "");
		filler7.setLayoutData(UIUtilities.textTableData(2));

		safrGuiToolkit.createLabel(compositeGeneral,SWT.NONE, "Created:");

		labelCreatedValue = safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE, defaultCreated);
		labelCreatedValue.setLayoutData(UIUtilities.textTableData(3));

		Label filler8 = safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE, "");
		filler8.setLayoutData(UIUtilities.textTableData(2));

		safrGuiToolkit.createLabel(compositeGeneral,SWT.NONE, "Last Modified:");

		labelModifiedValue = safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE, defaultModified);
		labelModifiedValue.setLayoutData(UIUtilities.textTableData(3));

		Label filler9 = safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE,"");
		filler9.setLayoutData(UIUtilities.textTableData(2));

		comboType.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Code code = UIUtilities.getCodeFromCombo(comboType);
				if (code.getGeneralId() == Codes.LOOKUP) {
					checkOptimize.setEnabled(!safrGuiToolkit.isReadOnly());
				} else {
					checkOptimize.setEnabled(false);
					checkOptimize.setSelection(false);
				}

			}
		});

		sectionGeneral.setClient(compositeGeneral);
	}

	@Override
	public void setFocus() {
		super.setFocus();
		textName.setFocus();
	}

	@Override
	public void refreshModel() {
		userExitRoutine.setName(textName.getText());

		// mandatory Code field. call setter only if value from combo is not
		// null, else a NPE will be thrown.
		Code codeTypeCode = UIUtilities.getCodeFromCombo(comboType);
		if (codeTypeCode != null) {
			userExitRoutine.setTypeCode(codeTypeCode);
		}
		userExitRoutine.setOptimize(checkOptimize.getSelection());
		Code codeLanguage = UIUtilities.getCodeFromCombo(comboLanguage);
		if (codeLanguage != null) {
			userExitRoutine.setLanguageCode(codeLanguage);
		}
		userExitRoutine.setExecutable(textExecutable.getText());
		userExitRoutine.setComment(textComments.getText());

	}

	@Override
	public String getModelName() {

		return "User-Exit Routine";
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
		userExitRoutine.store();
        ApplicationMediator.getAppMediator().refreshDeactiveViews(userExitRoutine.getDeactivatedViews());
	}

	@Override
	public void validate() throws DAOException, SAFRException {
		// Defect 7405 , Neha,Try catch added to catch dependency exception
		// occured when user exit routine having dependency is tried to change
		// by the user.
		try {
			userExitRoutine.validate();
			getMsgManager().removeAllMessages();
		} catch (SAFRDependencyException sde) {
			String dependenciesString = sde.getDependencyString();
			DependencyMessageDialog
					.openDependencyDialog(
							getSite().getShell(),
							"User Exit Routine dependencies",
							"The User Exit Routine detail cannot be changed due to the following dependencies. You must first remove these dependencies.",
							dependenciesString, MessageDialog.ERROR,
							new String[] { IDialogConstants.OK_LABEL }, 0);
			throw new SAFRValidationException();

		}

	}

	@Override
	public void doRefreshControls() throws SAFRException {
		UIUtilities.checkNullText(textID, Integer.toString(userExitRoutine
				.getId()));
		if (userExitRoutine.getId() > 0) {
			UIUtilities.checkNullText(textName, userExitRoutine.getName());
			UIUtilities
					.checkNullCombo(comboType, userExitRoutine.getTypeCode());
			selectedType = comboType.getText();
			String key;
			key = String.valueOf(comboType.getSelectionIndex());
			Code value = (Code) comboType.getData(key);

			if (value != null) {
				if (value.getGeneralId() == Codes.LOOKUP) {
					checkOptimize.setEnabled(!safrGuiToolkit.isReadOnly());
				}
			}
			checkOptimize.setSelection(userExitRoutine.isOptimize());
			UIUtilities.checkNullCombo(comboLanguage, userExitRoutine
					.getLanguageCode());
			selectedLangauge = comboLanguage.getText();
			UIUtilities.checkNullText(textExecutable, userExitRoutine
					.getExecutable());
			UIUtilities.checkNullText(textComments, userExitRoutine
					.getComment());
			labelCreatedValue.setText(userExitRoutine.getCreateBy() + " on "
					+ userExitRoutine.getCreateTimeString());
			labelModifiedValue.setText(userExitRoutine.getModifyBy() + " on "
					+ userExitRoutine.getModifyTimeString());
		} 
        if (userExitRoutine.getName() == null) {
            form.setText("");           
        } else {
            form.setText(userExitRoutine.getName());         
        }       
	}

	/**
	 * This method is used to get the widget based on the property passed.
	 * 
	 * @param property
	 * @return the widget.
	 */
	protected Control getControlFromProperty(Object property) {
		if (property == UserExitRoutine.Property.NAME) {
			return textName;
		} else if (property == UserExitRoutine.Property.EXECUTABLE) {
			return textExecutable;
		} else if (property == UserExitRoutine.Property.LANGUAGE) {
			return comboLanguage;
		} else if (property == UserExitRoutine.Property.TYPE) {
			return comboType;
		} else if (property == UserExitRoutine.Property.COMMENT) {
			return textComments;
		} 
		return null;
	}

	@Override
	public ComponentType getEditorCompType() {
		return ComponentType.UserExitRoutine;
	}

	@Override
	public SAFRPersistentObject getModel() {
		return userExitRoutine;
	}

	@Override
	public String getComponentNameForSaveAs() {
		return textName.getText();
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		if (sve.getErrorMessageMap().containsKey(
				com.ibm.safr.we.model.UserExitRoutine.Property.NAME)
				|| sve
						.getErrorMessageMap()
						.containsKey(
								com.ibm.safr.we.model.UserExitRoutine.Property.EXECUTABLE)) {
			return true;
		}
		return false;
	}

}
