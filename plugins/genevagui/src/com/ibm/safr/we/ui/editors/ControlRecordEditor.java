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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Combo;
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
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.ControlRecord.Property;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * This class creates the editor area and also the controls for a new/existing
 * Control Record
 */
public class ControlRecordEditor extends SAFREditorPart {
	public static String ID = "SAFRWE.ControlRecordEditor";

	private Text textID;
	private Text textName;
	private Text textComments;
	private Combo comboFirst;
	private Text textBegin;
	private Text textEnd;
	private Label labelCreatedValue;
	private Label labelModifiedValue;

	private String defaultCreated = "-";
	private String defaultModified = "-";

	private ScrolledForm form;
	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;
	private Section sectionGeneral;
	private Section sectionFiscal;

	private static final int MAXBEGINNINGPERIOD = 9;
	private static final int MAXENDINGPERIOD = 9;

	ControlRecordEditorInput cntRcd;
	ControlRecord controlRecord;

	private String selectedFirstFisaclMonth;

	@Override
	public boolean isSaveAsAllowed() {
		boolean retval = false;
		
		//if not dealing with a new component 
		//check with parent based on permissions
		if(controlRecord.getId() > 0) {
			retval = isSaveAsAllowed(null);
		}
		return retval;
	}

	@Override
	public void createPartControl(Composite parent) {
		cntRcd = (ControlRecordEditorInput) getEditorInput();
		controlRecord = cntRcd.getControlRecord();

		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		safrGuiToolkit.setReadOnly(cntRcd.getEditRights() == EditRights.Read);
		form = toolkit.createScrolledForm(parent);

		form.getBody().setLayout(UIUtilities.createTableLayout(1, false));
		createSectionGeneral(form.getBody());
		createSectionFiscalparam(form.getBody());
		refreshControls();
		setDirty(false);
		// Used to load the context sensitive help
		if (controlRecord.getId() > 0) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(),
					"com.ibm.safr.we.help.ControlRecordEditor");
		} else {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(),
					"com.ibm.safr.we.help.NewControlRecord");
		}
		ManagedForm mFrm = new ManagedForm(toolkit, form);
		setMsgManager(mFrm.getMessageManager());
	}

	/**
	 * Method to create the controls in the Fiscal Parameters section of the
	 * Control Record editor.
	 * 
	 * @param compositeValues
	 */
	private void createSectionFiscalparam(Composite compositeValues) {

		sectionFiscal = safrGuiToolkit.createSection(compositeValues,
				Section.TITLE_BAR, "Fiscal Parameters");

		sectionFiscal.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Composite fiscal = safrGuiToolkit.createComposite(sectionFiscal,
				SWT.NONE);

		fiscal.setLayout(UIUtilities.createTableLayout(6, false));

		safrGuiToolkit.createLabel(fiscal, SWT.NONE,
				"F&irst Fiscal Month:");

		comboFirst = safrGuiToolkit.createComboBox(fiscal, SWT.READ_ONLY, "");
		comboFirst.setData(SAFRLogger.USER, "First Fiscal Month");			
		comboFirst.add("1");
		comboFirst.add("2");
		comboFirst.add("3");
		comboFirst.add("4");
		comboFirst.add("5");
		comboFirst.add("6");
		comboFirst.add("7");
		comboFirst.add("8");
		comboFirst.add("9");
		comboFirst.add("10");
		comboFirst.add("11");
		comboFirst.add("12");

		comboFirst.setLayoutData(UIUtilities.textTableData(1));
		// comboFirst.addModifyListener(this);
		comboFirst.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				super.focusLost(e);
				if (!(comboFirst.getText().equals(selectedFirstFisaclMonth))) {
					setDirty(true);
					selectedFirstFisaclMonth = comboFirst.getText();
				}

			}

		});

		Label filler1 = safrGuiToolkit.createLabel(fiscal, SWT.NONE, "");
		filler1.setLayoutData(UIUtilities.textTableData(4));
		safrGuiToolkit.createLabel(fiscal, SWT.NONE,"&Beginning Period:");

		textBegin = safrGuiToolkit.createIntegerTextBox(fiscal, SWT.NONE, false);
		textBegin.setData(SAFRLogger.USER, "Beginning Period");					
		textBegin.setTextLimit(MAXBEGINNINGPERIOD);
		textBegin.setLayoutData(UIUtilities.textTableData(1));
		textBegin.addModifyListener(this);

		Label filler2 = safrGuiToolkit.createLabel(fiscal, SWT.NONE, "");
		filler2.setLayoutData(UIUtilities.textTableData(4));
		safrGuiToolkit.createLabel(fiscal, SWT.NONE,"&Ending Period:");

		textEnd = safrGuiToolkit.createIntegerTextBox(fiscal, SWT.NONE, false);
		textEnd.setData(SAFRLogger.USER, "Ending Period");					
		textEnd.setTextLimit(MAXENDINGPERIOD);
		textEnd.setLayoutData(UIUtilities.textTableData(1));
		textEnd.addModifyListener(this);

		Label filler3 = safrGuiToolkit.createLabel(fiscal, SWT.NONE, "");
		filler3.setLayoutData(UIUtilities.textTableData(4));

		sectionFiscal.setClient(fiscal);
	}

	/**
	 * Method to create the controls in the general section of the Control
	 * Record editor.
	 * 
	 * @param compositeValues
	 */
	private void createSectionGeneral(Composite compositeValues) {
		sectionGeneral = safrGuiToolkit.createSection(compositeValues,
				Section.TITLE_BAR, "General Information");

		sectionGeneral.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Composite general = safrGuiToolkit.createComposite(sectionGeneral,SWT.NONE);
		general.setLayout(UIUtilities.createTableLayout(6, true));

		safrGuiToolkit.createLabel(general, SWT.NONE, "ID:");

		textID = safrGuiToolkit.createTextBox(general, SWT.NONE);
		textID.setEnabled(false);
		textID.setLayoutData(UIUtilities.textTableData(1));

		Label filler1 = safrGuiToolkit.createLabel(general, SWT.NONE, "");
		filler1.setLayoutData(UIUtilities.textTableData(4));

		safrGuiToolkit.createLabel(general, SWT.NONE, "&Name:");

		textName = safrGuiToolkit.createNameTextBox(general, SWT.LEFT);
		textName.setLayoutData(UIUtilities.textTableData(2));
		textName.setTextLimit(UIUtilities.MAXNAMECHAR);
		textName.addModifyListener(this);		
		textName.setData(SAFRLogger.USER, "Name");										
		
		Label filler3 = safrGuiToolkit.createLabel(general, SWT.NONE, "");
		filler3.setLayoutData(UIUtilities.textTableData(3));

		safrGuiToolkit.createLabel(general, SWT.NONE,"C&omments:");

		textComments = safrGuiToolkit.createCommentsTextBox(general);
		textComments.setLayoutData(UIUtilities.multiLineTextData(1, 2,textComments.getLineHeight() * 3));
		textComments.addModifyListener(this);
		textComments.setData(SAFRLogger.USER, "Comments");										

		Label filler4 = safrGuiToolkit.createLabel(general, SWT.NONE, "");
		filler4.setLayoutData(UIUtilities.textTableData(3));

		safrGuiToolkit.createLabel(general, SWT.NONE,"Created:");

		labelCreatedValue = safrGuiToolkit.createLabel(general, SWT.NONE,defaultCreated);
		labelCreatedValue.setLayoutData(UIUtilities.textTableData(5));

		safrGuiToolkit.createLabel(general, SWT.NONE,"Last Modified:");

		labelModifiedValue = safrGuiToolkit.createLabel(general, SWT.NONE,defaultModified);
		labelModifiedValue.setLayoutData(UIUtilities.textTableData(5));

		sectionGeneral.setClient(general);
	}

	@Override
	public void setFocus() {
		super.setFocus();
		textName.setFocus();
	}

	@Override
	public void doRefreshControls() {
		UIUtilities.checkNullText(textID, Integer.toString(controlRecord
				.getId()));
		if (controlRecord.getId() > 0) {
			UIUtilities.checkNullText(textName, controlRecord.getName());
			UIUtilities.checkNullText(textComments, controlRecord.getComment());
			labelCreatedValue.setText(controlRecord.getCreateBy() + " on "
					+ controlRecord.getCreateTimeString());
			labelModifiedValue.setText(controlRecord.getModifyBy() + " on "
					+ controlRecord.getModifyTimeString());
			comboFirst.setText(controlRecord.getFirstFiscalMonth().toString());
			selectedFirstFisaclMonth = comboFirst.getText();
			UIUtilities.checkNullText(textBegin, Integer.toString(controlRecord
					.getBeginPeriod()));
			UIUtilities.checkNullText(textEnd, Integer.toString(controlRecord
					.getEndPeriod()));

            textName.setSelection(0);
		}
        if (controlRecord.getName() == null) {
            form.setText("");           
        } else {
            form.setText(controlRecord.getName());         
        }
		
	}

	@Override
	public void refreshModel() {
		if (comboFirst.getSelectionIndex() == -1) {
			comboFirst.setText("0");
		}

		controlRecord.setName(textName.getText());
		controlRecord.setComment(textComments.getText());
		controlRecord.setFirstFiscalMonth(UIUtilities
				.stringToInteger(comboFirst.getText()));
		controlRecord.setBeginPeriod(UIUtilities.stringToInteger(textBegin
				.getText()));
		controlRecord.setEndPeriod(UIUtilities.stringToInteger(textEnd
				.getText()));
	}

	@Override
	public String getModelName() {
		return "Control Record";
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
		controlRecord.store();
	}

	@Override
	public void validate() throws DAOException, SAFRException {
		controlRecord.validate();
		getMsgManager().removeAllMessages();
	}

	/**
	 * This method is used to get the widget based on the property passed.
	 * 
	 * @param property
	 * @return the widget.
	 */
	protected Control getControlFromProperty(Object property) {
		if (property == Property.NAME) {
			return textName;
		} else if (property == Property.BEGIN_PERIOD) {
			return textBegin;
		} else if (property == Property.COMMENT) {
			return textComments;
		} else if (property == Property.END_PERIOD) {
			return textEnd;
		} else if (property == Property.FIRST_FISCAL_MONTH) {
			return comboFirst;
		}
		return null;
	}

	@Override
	public ComponentType getEditorCompType() {
		return ComponentType.ControlRecord;
	}

	@Override
	public SAFRPersistentObject getModel() {
		return controlRecord;
	}

	@Override
	public String getComponentNameForSaveAs() {
		return textName.getText();
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		if (sve.getErrorMessageMap().containsKey(Property.NAME)) {
			return true;
		}
		return false;
	}
}
