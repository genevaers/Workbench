package com.ibm.safr.we.ui.editors;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.safr.we.constants.EditRights;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.swt.IFocusService;
import org.hamcrest.Condition.Step;
import org.hamcrest.core.IsInstanceOf;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LRFieldKeyType;
import com.ibm.safr.we.constants.LookupPathSourceFieldType;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.LookupPathSourceField;
import com.ibm.safr.we.model.LookupPathStep;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRField;
import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.SAFRValidationToken;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordFieldQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.commands.SourceProvider;
import com.ibm.safr.we.ui.dialogs.DependencyMessageDialog;
import com.ibm.safr.we.ui.utilities.CommentsTraverseListener;
import com.ibm.safr.we.ui.utilities.DepCheckOpener;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.LogicalRecordFieldTableSorter;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class LookupPathEditor extends SAFREditorPart implements IPartListener2 {
	public LookupPathEditor() {
	}
	private static final int LR_DATA_WIDTH = 400;

	public static String ID = "SAFRWE.LookupPathEditor";
    private TableComboViewer comboSourceLRViewerGeneral; 
    private TableCombo comboSourceLRGeneral; 
    private Text textSLR; 
	private FormToolkit toolkit;
	private ScrolledForm form;
	private CTabFolder tabFolder;
	private Composite compositeGeneral;
	private CTabItem tabGeneral;
	private Composite compositeDefinition;
	private CTabItem tabDefinition;

	public LogicalRecord selectedGeneralLR;
	public static String entrystr;
	static int count=1;
    private String selectedStatus = "";
	private Text textID;
	private Text textName;
	private Text textComments;
	private Text textConstantValue;
	private Text textSymbolName;
	private Text textSymbolDefaultValue;
	private Text textLength;
	private Text textScalingFactor;
	private Text textDecimalPlaces;

	LogicalRecord step1generallr;
	private Combo checkboxChangeStatus;
	private Button checkboxSigned;
	private Button buttonAddStep;
	private Button buttonRemoveStep;
	private Button radioConstant;
	private Button radioSymbol;

	private Label labelStatus;
	private Label labelCreatedValue;
	private Label labelModifiedValue;
    private Label labelActivatedValue;
	private Label labelSourceLR;
	private Label labelSourceLF;
	private Label labelSourceLFText;
	private Label labelSourceLRText;

	private String defaultModStr = "-";
	private Section sectionStepsList;

	private Section sectionSource;
	private Section sectionTarget;
	private Section sectionSourceFieldProperties;
	private Section sectionFieldSource;
	private Section sectionDataAttributes;

	private TableViewer tableViewerStepsList;
	private TableViewer tableViewerSource;
	private TableViewer tableViewerTarget;
	private Table tableStepsList;
	private Table tableSource;
	private Table tableTarget;
	String step1lrname;
	Integer step1lrid;
	LogicalRecord step1lr;
	private TableCombo comboTargetLF;
	private TableCombo comboTargetLR;
	private TableCombo comboSourceLR;
	private TableCombo comboLRField;

	private Combo comboDataType;
	private Combo comboDateTimeFormat;
	private Combo comboNumericMask;

	private static String[] targetColumnHeaders = { "Target Primary Keys",
			"Length" };
	private static int[] targetColumnWidths = { 550, 50 };
	private static String[] sourceColumnHeaders = { "Selected Source Fields",
			"Length" };
	private static int[] sourceColumnWidths = { 550, 50 };

	private static final int MAXLENGTH = 5;
	private static final int MAXDECIMALPLACES = 1;
	private static final int MAXSCALING = 2;
	private static final int MAXSOURCEVALUE = 254;
	private static final int MAXSYMBOLNAME = 254;

	private static final String DEFAULT_VALUE = "0";
	private static final int TARGET_DATA_OFFSET = 170;

	private LookupPathEditorInput lookupPathInput;
	private LookupPath lookupPath;
	private LookupPathStep currentStep;
	private String selectedTargetLR = "";
	private String selectedSourceLR = "";
	private String selectedTargetLF = "";
	private String selectedSrcLRField = "";
	private String selectedSrcLR = "";
	private String selectedSrcFieldDataType = "";
	private String selectedSrcFieldDateTimeFormat = "";
	private String selectedSrcFieldNumericMask = "";
	private SAFRGUIToolkit safrGuiToolkit = null;
	private Boolean warningsShown = false;
	private Button radiofield;
	private TableComboViewer comboSourceLRViewer;

	private TableComboViewer comboTargetLRViewer;

	private TableComboViewer comboTargetLFViewer;

	private TableComboViewer comboLRFieldViewer;

	private EnvironmentalQueryBean prevSrcLR;
	private EnvironmentalQueryBean prevTrgLR;
	private ComponentAssociation prevTrgLF;

	private Boolean toggleAccKey_e = true;
	private Boolean toggleAccKey_i = true;

    private MenuItem opEdSourceFldItem = null;
    private MenuItem depChkSourceFldItem = null;
	private MenuItem opEdSourceLRItem = null;
    private MenuItem depChkSourceLRItem = null;
    private MenuItem opEdTargetLRItem = null;
    private MenuItem depChkTargetLRItem = null;
    private MenuItem opEdTargetLFItem = null;
    private MenuItem depChkTargetLFItem = null;

	private Label labelUserExit;

	private Text textUserExitRoutine;

	private Text textUserExitRoutineParam;

	private Label userParamLabel;

	
	@Override
	public void createPartControl(Composite parent) {
		lookupPathInput = (LookupPathEditorInput) getEditorInput();
		lookupPath = lookupPathInput.getLookupPath();

		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		safrGuiToolkit
				.setReadOnly(lookupPathInput.getEditRights() == EditRights.Read);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FillLayout());
		try {
			createTabFolder(form.getBody());
			refreshControls();
			// Show the step definition for the first step by default
			LookupPathStep firstStep = (LookupPathStep) lookupPath
					.getLookupPathSteps().getActiveItems().get(0);
			loadStepDefinition(firstStep);
			setDirty(false);
			if (lookupPath.getId() > 0) {
				checkAndDisplayLookupState();
				
				// Lookup up path tab
				tabFolder.setSelection(1);				
			}
			ManagedForm mFrm = new ManagedForm(toolkit, form);
			setMsgManager(mFrm.getMessageManager());
		} catch (DAOException de) {
			UIUtilities.handleWEExceptions(de,
			    "Unexpected database error occurred while loading step definition.",
			    UIUtilities.titleStringDbException);
		} catch (SAFRException se) {
			UIUtilities.handleWEExceptions(se,
			    "Unexpected error occurred while loading step definition.",null);
		}
		getSite().getPage().addPartListener(this);
	}

	private void createTabFolder(Composite body) throws SAFRException,
			DAOException {
		tabFolder = new CTabFolder(body, SWT.TOP);
		tabFolder.setLayout(UIUtilities.createTableLayout(1, false));
		toolkit.adapt(tabFolder, true, true);

		createCompositeGeneral();
		tabGeneral = new CTabItem(tabFolder, SWT.NONE);
		tabGeneral.setText("&General");
		tabGeneral.setControl(compositeGeneral);
		tabFolder.setSelection(tabGeneral);

		createCompositeDefinition();
		tabDefinition = new CTabItem(tabFolder, SWT.NONE);
		tabDefinition.setText("&Lookup Path Definition");
		tabDefinition.setControl(compositeDefinition);
	}

	private void createCompositeGeneral() {
		compositeGeneral = safrGuiToolkit.createComposite(tabFolder, SWT.NONE);
		compositeGeneral.setLayout(new FormLayout());
		compositeGeneral.setLayoutData(new FormData());
		compositeGeneral.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));

		Label dummmyLabel = safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, "Last Modified: ");
		FormData dataDummyLabel = new FormData();
		dataDummyLabel.top = new FormAttachment(0, 10);
		dataDummyLabel.left = new FormAttachment(0, 10);
		dataDummyLabel.width = SWT.DEFAULT;
		dummmyLabel.setLayoutData(dataDummyLabel);
		dummmyLabel.setVisible(false);

		Label labelId = safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE, "ID:");
		FormData dataLabelId = new FormData();
		dataLabelId.top = new FormAttachment(0, 10);
		dataLabelId.left = new FormAttachment(0, 10);
		dataLabelId.width = SWT.DEFAULT;
		labelId.setLayoutData(dataLabelId);

		textID = safrGuiToolkit.createTextBox(compositeGeneral, SWT.NONE);
		textID.setEnabled(false);
		FormData dataTextId = new FormData();
		dataTextId.top = new FormAttachment(0, 10);
		dataTextId.left = new FormAttachment(dummmyLabel, 80);
		dataTextId.width = 530;
		textID.setLayoutData(dataTextId);

		Label labelName = safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, "&Name:");
		FormData dataLabelName = new FormData();
		dataLabelName.top = new FormAttachment(textID, 10);
		dataLabelName.left = new FormAttachment(0, 10);
		dataLabelName.width = SWT.DEFAULT;
		labelName.setLayoutData(dataLabelName);

		textName = safrGuiToolkit.createNameTextBox(compositeGeneral, SWT.NONE);
		textName.setData(SAFRLogger.USER, "Name");        
		
		FormData dataTextName = new FormData();
		dataTextName.top = new FormAttachment(textID, 10);
		dataTextName.left = new FormAttachment(dummmyLabel, 80);
		dataTextName.width = 530;
		textName.setLayoutData(dataTextName);
		textName.setTextLimit(UIUtilities.MAXNAMECHAR);
		textName.addModifyListener(this);

		Label labelSLR = safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, "&Source Logical Record:");
		FormData dataLabelSLR = new FormData();
		dataLabelSLR.top = new FormAttachment(textName, 10);
		dataLabelSLR.left = new FormAttachment(0, 10);
		dataLabelSLR.width = SWT.DEFAULT;
		labelSLR.setLayoutData(dataLabelSLR);
		
		comboSourceLRViewerGeneral = safrGuiToolkit.createTableComboForComponents(
				compositeGeneral, ComponentType.LogicalRecord);
		comboSourceLRGeneral = comboSourceLRViewerGeneral.getTableCombo();
		comboSourceLRGeneral.setData(SAFRLogger.USER, "Source Logical Record");        		
		FormData dataTextSLR = new FormData();
		dataTextSLR.left = new FormAttachment(dummmyLabel, 80);
		dataTextSLR.top = new FormAttachment(textName, 5);
		dataTextSLR.width = 535;
		comboSourceLRGeneral.setLayoutData(dataTextSLR);	
		comboSourceLRGeneral.setVisible(true);
//		addSourceLROpenEditorMenu();
		
		comboSourceLRGeneral.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				try {
					getSite().getShell().setCursor(
							getSite().getShell().getDisplay().getSystemCursor(
									SWT.CURSOR_WAIT));

					if (selectedSourceLR.equals(comboSourceLRGeneral.getText())) {
						return;
					}
					EnvironmentalQueryBean bean = null;

					if (comboSourceLRGeneral.getTable().getSelectionCount() > 0) {
					    
						bean = (EnvironmentalQueryBean) comboSourceLRGeneral
								.getTable().getSelection()[0].getData();
						boolean flag = true;
						try {
							// only the first step's source LR is editable.
							selectedGeneralLR = getLogicalRecordFromCombo(comboSourceLRGeneral);
							//if first step
							if(currentStep.getSequenceNumber()==1){
								lookupPath.setSourceLR(getLogicalRecordFromCombo(comboSourceLRGeneral),currentStep);
							}
						} 
						catch (SAFRValidationException sve) {
							flag = false;
							DependencyMessageDialog.openDependencyDialog(
								getSite().getShell(),
								"GenevaERS Workbench",
								"You cannot change this source Logical Record as it is being used in the source fields of the current step and/or subsequent steps as indicated below:",
								sve.getMessageString(),
								MessageDialog.ERROR,
								new String[] { IDialogConstants.OK_LABEL },
								0);
							// condition to select back the grayed out
							// element
							// CQ8755
							if(step1generallr!=null){
								comboSourceLRGeneral.setText(step1generallr.getName() + " ["+step1generallr.getId()+ "]");
							}
						} 
					catch (SAFRException e1) {
							UIUtilities.handleWEExceptions(e1);
						}
						if (flag) {
							prevSrcLR = bean;
						}

						populateLRField(currentStep,comboSourceLRGeneral.getText());
						//populateSourceLogicalRecord(currentStep);
						tableViewerStepsList.refresh();
						setDirty(true);
					} else {
						if (prevSrcLR != null) {
							comboSourceLRGeneral.setText(UIUtilities.getComboString(
									prevSrcLR.getName(), prevSrcLR.getId()));
						} else {
							comboSourceLRGeneral.setText("");
						}
					}

					selectedSourceLR = comboSourceLR.getText();
				} finally {
					getSite().getShell().setCursor(null);
				}
			}
		});
		labelStatus = safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE,
				"Status:");
		FormData dataLabelStatus = new FormData();
		dataLabelStatus.top = new FormAttachment(comboSourceLRGeneral, 10);
		dataLabelStatus.width = SWT.DEFAULT;
		dataLabelStatus.left = new FormAttachment(0, 10);
		labelStatus.setLayoutData(dataLabelStatus);

		checkboxChangeStatus = safrGuiToolkit.createComboBox(compositeGeneral, SWT.READ_ONLY, "");
		checkboxChangeStatus.setData(SAFRLogger.USER, "Status");                               
		checkboxChangeStatus.setLayoutData(UIUtilities.textTableData(1));
        UIUtilities.populateComboBox(checkboxChangeStatus, CodeCategories.LRSTATUS, 1,false);
        selectedStatus = checkboxChangeStatus.getText();
        checkboxChangeStatus.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!(checkboxChangeStatus.getText().equals(selectedStatus))) {
                    setDirty(true);
                    selectedStatus = checkboxChangeStatus.getText();
                }
            }
        });
        checkboxChangeStatus.setText(SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.LRSTATUS).getCode(Codes.INACTIVE).getDescription());       
		
		FormData dataCheckboxChangeStatus = new FormData();
		dataCheckboxChangeStatus.top = new FormAttachment(comboSourceLRGeneral, 10);
	    dataCheckboxChangeStatus.left = new FormAttachment(labelStatus, 120);
	    dataCheckboxChangeStatus.width=535;
		checkboxChangeStatus.setLayoutData(dataCheckboxChangeStatus);
		checkboxChangeStatus.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
				if (checkboxChangeStatus.getText().equals("Inactive")) {
					lookupPath.setValid(false);
				} else 
				{
					lookupPath.setValid(true);
				}
			}

		});

		Label labelComments = safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, "C&omments:");
		FormData dataLabelComments = new FormData();
		dataLabelComments.top = new FormAttachment(checkboxChangeStatus, 10);
		dataLabelComments.width = SWT.DEFAULT;
		dataLabelComments.left = new FormAttachment(0, 10);
		labelComments.setLayoutData(dataLabelComments);

		textComments = safrGuiToolkit.createCommentsTextBox(compositeGeneral);
		textComments.setData(SAFRLogger.USER, "Comments");        
		
		FormData dataTextComments = new FormData();
		dataTextComments.top = new FormAttachment(checkboxChangeStatus, 10);
		dataTextComments.left = new FormAttachment(dummmyLabel, 80);
		dataTextComments.height = 30;
		dataTextComments.width = 535;
		textComments.setLayoutData(dataTextComments);
		textComments.setTextLimit(UIUtilities.MAXCOMMENTCHAR);
		CommentsTraverseListener traverseListener = new CommentsTraverseListener();
		textComments.addTraverseListener(traverseListener);
		textComments.addModifyListener(this);

		Label labelCreated = safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, "Created:");
		FormData dataLabelCreated = new FormData();
		dataLabelCreated.top = new FormAttachment(textComments, 10);
		dataLabelCreated.width = SWT.DEFAULT;
		dataLabelCreated.left = new FormAttachment(0, 10);
		labelCreated.setLayoutData(dataLabelCreated);

		labelCreatedValue = safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, defaultModStr);
		FormData dataLabelCreatedValue = new FormData();
		dataLabelCreatedValue.top = new FormAttachment(textComments, 10);
		dataLabelCreatedValue.left = new FormAttachment(dummmyLabel, 80);
		dataLabelCreatedValue.width = 200;
		labelCreatedValue.setLayoutData(dataLabelCreatedValue);

		Label labelModified = safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, "Last Modified:");
		FormData dataLabelModified = new FormData();
		dataLabelModified.top = new FormAttachment(labelCreated, 10);
		dataLabelModified.width = SWT.DEFAULT;
		dataLabelModified.left = new FormAttachment(0, 10);
		labelModified.setLayoutData(dataLabelModified);

		labelModifiedValue = safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, defaultModStr);
		FormData dataLabelModifiedValue = new FormData();
		dataLabelModifiedValue.top = new FormAttachment(labelCreated, 10);
		dataLabelModifiedValue.left = new FormAttachment(dummmyLabel, 80);
		dataLabelModifiedValue.width = 200;
		labelModifiedValue.setLayoutData(dataLabelModifiedValue);

        Label labelActivated = safrGuiToolkit.createLabel(compositeGeneral,
            SWT.NONE, "Last De/active:");
        FormData dataLabelActivated = new FormData();
        dataLabelActivated.top = new FormAttachment(labelModified, 10);
        dataLabelActivated.width = SWT.DEFAULT;
        dataLabelActivated.left = new FormAttachment(0, 10);
        labelActivated.setLayoutData(dataLabelActivated);
    
        labelActivatedValue = safrGuiToolkit.createLabel(compositeGeneral,
                SWT.NONE, defaultModStr);
        FormData dataLabelActivatedValue = new FormData();
        dataLabelActivatedValue.top = new FormAttachment(labelModified, 10);
        dataLabelActivatedValue.left = new FormAttachment(dummmyLabel, 80);
        dataLabelActivatedValue.width = 200;
        labelActivatedValue.setLayoutData(dataLabelActivatedValue);
        populateLogicalRecord(comboSourceLRGeneral);
	}

	private void populateLogicalRecord(TableCombo comboSourceLR) throws DAOException {
		Integer counter = 0;

		comboSourceLRGeneral.getTable().removeAll();

		List<LogicalRecordQueryBean> logicalRecordList = SAFRQuery
				.queryAllActiveLogicalRecords(UIUtilities
						.getCurrentEnvironmentID(), SortType.SORT_BY_NAME);
		List<LogicalRecordQueryBean> listWithoutDuplicates = new ArrayList<>(new HashSet<>(logicalRecordList));

		comboSourceLRViewerGeneral.setInput(listWithoutDuplicates);
		comboSourceLRViewerGeneral.refresh();
		for (LogicalRecordQueryBean logicalRecordBean : listWithoutDuplicates) {
			comboSourceLRGeneral.setData(Integer.toString(counter), logicalRecordBean);
			counter++;
		}
	}


	private void createCompositeDefinition() throws DAOException {
		compositeDefinition = safrGuiToolkit.createComposite(tabFolder,
				SWT.NONE);

		FormLayout layoutCompositeDefinition = new FormLayout();
		layoutCompositeDefinition.marginTop = UIUtilities.TOPMARGIN;
		layoutCompositeDefinition.marginBottom = UIUtilities.BOTTOMMARGIN;
		layoutCompositeDefinition.marginLeft = UIUtilities.LEFTMARGIN;
		layoutCompositeDefinition.marginRight = UIUtilities.RIGHTMARGIN;
		compositeDefinition.setLayout(layoutCompositeDefinition);
		compositeDefinition.setLayoutData(new TableWrapData());

		createSectionStepsList(compositeDefinition);

		createSectionTarget(compositeDefinition);
		createSectionSource(compositeDefinition);
		//refreshToolbar();
		// populates combos for both, target and source LR, at once
		populateLogicalRecord(comboTargetLR, comboSourceLR);

	}

	private void createSectionStepsList(Composite compositeDefintion) {
		sectionStepsList = safrGuiToolkit.createSection(compositeDefintion,
				Section.TITLE_BAR, "S&teps");

		sectionStepsList.setLayoutData(new FormData());

		Composite compositeStepsList = safrGuiToolkit.createComposite(
				sectionStepsList, SWT.NONE);
		compositeStepsList.setLayout(new FormLayout());

		tableViewerStepsList = safrGuiToolkit.createTableViewer(
				compositeStepsList, SWT.FULL_SELECTION | SWT.BORDER, false);
		tableStepsList = tableViewerStepsList.getTable();
		tableStepsList.setData(SAFRLogger.USER, "Steps List");        		
		tableStepsList.setLinesVisible(true);
		FormData dataTableStepsList = new FormData();
		dataTableStepsList.left = new FormAttachment(0, 0);
		dataTableStepsList.top = new FormAttachment(0, 13);
		dataTableStepsList.width = 40;
		dataTableStepsList.height = 342;
		tableStepsList.setLayoutData(dataTableStepsList);

		buttonAddStep = safrGuiToolkit.createButton(compositeStepsList,
				SWT.PUSH, "A&dd");
		buttonAddStep.setData(SAFRLogger.USER, "Add Step");        		
		
		FormData dataAddStep = new FormData();
		dataAddStep.left = new FormAttachment(0, 0);
		dataAddStep.top = new FormAttachment(tableStepsList, 10);
		// dataAddStep.width = 60;
		buttonAddStep.setLayoutData(dataAddStep);
		buttonAddStep.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				setDirty(true);

				try {
					// allow user to add new step only if current step is not
					// incomplete
					// if (!checkNullLRLF(currentStep)) {
					LookupPathStep lastStep = (LookupPathStep) lookupPath
							.getLookupPathSteps().getActiveItems().get(
									tableStepsList.getItemCount() - 1);
					if (!checkNullLRLF(lastStep)) {
						LookupPathStep newStep = lookupPath.createStep();
						tableViewerStepsList.refresh();
						tableStepsList
								.select(tableStepsList.getItemCount() - 1);
						loadStepDefinition(newStep);
					}
					if(lookupPath.getLookupPathSteps().size()==1){
						comboSourceLRGeneral.setEnabled(true);
					}
					else{
						comboSourceLRGeneral.setEnabled(false);
					}
					refreshControls();

				} catch (DAOException de) {
					UIUtilities
							.handleWEExceptions(
									de,
									"Unexpected database error occurred while adding a step.",
									UIUtilities.titleStringDbException);
				} catch (SAFRException se) {
					UIUtilities.handleWEExceptions(se,
							"Unexpected error occurred while adding a step.",
							null);
				}
			}

		});

		buttonRemoveStep = safrGuiToolkit.createButton(compositeStepsList,
				SWT.PUSH, "Re&move");
		buttonRemoveStep.setData(SAFRLogger.USER, "Remove Step");        		
		FormData dataRemoveStep = new FormData();
		dataRemoveStep.left = new FormAttachment(0, 0);
		dataRemoveStep.top = new FormAttachment(buttonAddStep, 5);
		// dataRemoveStep.width = 60;
		buttonRemoveStep.setLayoutData(dataRemoveStep);
		buttonRemoveStep.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				removeSelectedStep();
				if(currentStep.getSequenceNumber()==1){
					comboSourceLRGeneral.setEnabled(true);
				}
				else{
					comboSourceLRGeneral.setEnabled(false);
				}
				refreshControls();

			}

		});

		tableViewerStepsList.setContentProvider(new StepsListContentProvider());

		// compute preferred size of the largest button and set width of the
		// other buttons accordingly.
		Point computeSize = buttonRemoveStep.computeSize(SWT.DEFAULT,
				SWT.DEFAULT);
		dataAddStep.width = computeSize.x;
		dataRemoveStep.width = computeSize.x;

		// dummy column created in order to set the alignment for the Steps
		// column, as alignment is not supported for first column of table
		TableViewerColumn dummyColumn = new TableViewerColumn(
				tableViewerStepsList, SWT.NONE);
		dummyColumn.getColumn().setWidth(0);
		dummyColumn.getColumn().setText("");
		dummyColumn.setLabelProvider(new StepsListLabelProvider());

		TableViewerColumn column = new TableViewerColumn(tableViewerStepsList,
				SWT.NONE);
		column.getColumn().setWidth(55);
		column.getColumn().setAlignment(SWT.CENTER);
		column.setLabelProvider(new StepsListLabelProvider());
		tableViewerStepsList.setInput(lookupPath);

		tableStepsList.select(0);

		tableStepsList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				try {
					LookupPathStep step = (LookupPathStep) lookupPath
							.getLookupPathSteps().getActiveItems().get(
									tableStepsList.getSelectionIndex());
					if (currentStep != step) {
						loadStepDefinition(step);
						refreshSourceTable(currentStep);
						refreshTargetTable(currentStep);
					}
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1,"Error occurred while loading step for the lookup path.",null);
				}
			}
		});

		tableStepsList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.DEL) {
					removeSelectedStep();
				}
			}
		});

		sectionStepsList.setClient(compositeStepsList);

	}

	private void createSectionSource(Composite compositeSourceTarget) {
		sectionSource = safrGuiToolkit.createSection(compositeSourceTarget,
				Section.TITLE_BAR, "Source - Total Width: 0                ");
		FormData dataSectionSource = new FormData();
		dataSectionSource.top = new FormAttachment(sectionTarget, 20);
		dataSectionSource.left = new FormAttachment(sectionStepsList, 20);
		sectionSource.setLayoutData(dataSectionSource);

		Composite compositeSource = safrGuiToolkit.createComposite(
				sectionSource, SWT.NONE);
		compositeSource.setLayout(new FormLayout());

		
		labelSourceLRText = safrGuiToolkit.createLabel(compositeSource,
				SWT.NONE, "");
		FormData dataLabelSourceLRText = new FormData();
		dataLabelSourceLRText.left = new FormAttachment(labelSourceLR, 10);
		dataLabelSourceLRText.top = new FormAttachment(0, 10);
		dataLabelSourceLRText.width = 350;
		labelSourceLRText.setLayoutData(dataLabelSourceLRText);
		labelSourceLRText.setVisible(false);

		labelSourceLF = safrGuiToolkit.createLabel(compositeSource, SWT.NONE,
				"Logical File:");
		FormData dataLabelSourceLF = new FormData();
		dataLabelSourceLF.width = SWT.DEFAULT;
		dataLabelSourceLF.top = new FormAttachment(comboSourceLR, 10);
		labelSourceLF.setLayoutData(dataLabelSourceLF);
		labelSourceLF.setVisible(false);

		labelSourceLFText = safrGuiToolkit.createLabel(compositeSource,
				SWT.NONE, "");
		FormData dataLabelSourceLFText = new FormData();
		dataLabelSourceLFText.left = new FormAttachment(labelSourceLR, 10);
		dataLabelSourceLFText.top = new FormAttachment(comboSourceLR, 10);
		dataLabelSourceLFText.width = 350;
		labelSourceLFText.setLayoutData(dataLabelSourceLFText);
		labelSourceLFText.setVisible(false);

		tableViewerSource = safrGuiToolkit.createTableViewer(compositeSource,
				SWT.FULL_SELECTION | SWT.BORDER, false);
		tableSource = tableViewerSource.getTable();
		tableSource.setData(SAFRLogger.USER, "Source Field");        				
		tableSource.setHeaderVisible(true);
		tableSource.setLinesVisible(true);
		FormData dataTableSource = new FormData();
		dataTableSource.left = new FormAttachment(0, 0);
		dataTableSource.top = new FormAttachment(labelSourceLFText, 10);
		dataTableSource.width = 600;
		dataTableSource.height = 80;
		tableSource.setLayoutData(dataTableSource);

		tableViewerSource.setContentProvider(new SourceTableContentProvider());

		int numberOfCol = sourceColumnHeaders.length;
		for (int counter = 0; counter < numberOfCol; counter++) {
			TableViewerColumn column = new TableViewerColumn(tableViewerSource,
					SWT.NONE);
			column.getColumn().setText(sourceColumnHeaders[counter]);
			column.getColumn().setToolTipText(sourceColumnHeaders[counter]);
			column.getColumn().setWidth(sourceColumnWidths[counter]);
			column.getColumn().setResizable(true);
		}

		tableViewerSource.setLabelProvider(new SourceTableLabelProvider());


		tableSource.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				showSectionSourceFieldProperties(true);
				LookupPathSourceField field = (LookupPathSourceField) currentStep
						.getSourceFields().getActiveItems().get(
								tableSource.getSelectionIndex());
				try {
				    if (field == null) {
                        tableSource.setData(SAFRLogger.USER, "Source Field");				        
				    }
				    else {
				        tableSource.setData(SAFRLogger.USER, "Source Field " + getSourceFieldName(field));
				    }
					loadSourceFieldDefinition(field);
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1,"Unexpected error occurred while loading source field definition.",null);
				}
				refreshToolbar();
			}
		});

		tableViewerSource.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				// set focus on first available active control in source field
				// properties. This will work faster than taking the source
				// field out of table and comparing its type.
				comboLRField.setFocus();
				comboSourceLR.setFocus();
				textConstantValue.setFocus();
				textSymbolName.setFocus();	
			}
		});
		// Code for tracking the focus on the table
		IFocusService service = (IFocusService) getSite().getService(
				IFocusService.class);
		service.addFocusTracker(tableSource, "SourceFieldTable");

		// Code for Context menu
		// First we create a menu Manager
		MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(tableViewerSource
				.getControl());
		// Set the MenuManager
		tableViewerSource.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, tableViewerSource);
		// Make the selection available
		getSite().setSelectionProvider(tableViewerSource);

		// Code to manage the section toolbar.
		final ToolBar toolbar = new ToolBar(sectionSource, SWT.FLAT);
		ToolBarManager toolManager = new ToolBarManager(toolbar);
		IMenuService m = (IMenuService) getSite()
				.getService(IMenuService.class);
		m.populateContributionManager(toolManager,
				"toolbar:SAFRWE.com.ibm.safr.we.ui.editors.LookupPathEditor");
		toolManager.update(true);
		toolbar.setData(SAFRLogger.USER, "Toolbar");
		toolbar.addMouseListener(new MouseAdapter() {

			public void mouseDown(MouseEvent e) {
				ToolItem item = toolbar.getItem(new Point(e.x, e.y));
				if (item == null) {
					toolbar.setData(SAFRLogger.USER, "Toolbar");					
				}
				else {
					toolbar.setData(SAFRLogger.USER, "Toolbar " + item.getToolTipText());					
				}
			}
			
		});

		
		sectionSource.setTextClient(toolbar);
		sectionSource.setClient(compositeSource);

		createSectionSourceFieldProperties(compositeSourceTarget);
		getSite().setSelectionProvider(tableViewerSource);
	}

	private void addSourceLROpenEditorMenu()
	{
        Text text = comboSourceLR.getTextControl();
	    Menu menu = text.getMenu();
	    opEdSourceLRItem = new MenuItem(menu, SWT.PUSH);
	    opEdSourceLRItem.setText("Open Editor");
	    opEdSourceLRItem.addListener(SWT.Selection, new Listener()
	    {
	        public void handleEvent(Event event)
	        {
                LogicalRecordQueryBean bean = (LogicalRecordQueryBean)((StructuredSelection) comboSourceLRViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.LogicalRecord);                        
                }                
	        }
	    });
	    
	    depChkSourceLRItem = new MenuItem(menu, SWT.PUSH);
	    depChkSourceLRItem.setText("Dependency Checker");
	    depChkSourceLRItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                LogicalRecordQueryBean bean = (LogicalRecordQueryBean)((StructuredSelection) comboSourceLRViewer
                    .getSelection()).getFirstElement();
                if (bean != null && bean.getId() != 0) {
                    DepCheckOpener.open(bean);
                }                
            }
        });
	    
	    comboSourceLR.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    LogicalRecordQueryBean bean = (LogicalRecordQueryBean) ((StructuredSelection) comboSourceLRViewer.getSelection()).getFirstElement();
                    if (bean != null) {   
                        opEdSourceLRItem.setEnabled(true); 
                        depChkSourceLRItem.setEnabled(true);
                    }
                    else {
                        opEdSourceLRItem.setEnabled(false);
                        depChkSourceLRItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
	    });
	}    	

    private void addTargetLROpenEditorMenu()
    {
        Text text = comboTargetLR.getTextControl();
        Menu menu = text.getMenu();
        opEdTargetLRItem = new MenuItem(menu, SWT.PUSH);
        opEdTargetLRItem.setText("Open Editor");
        opEdTargetLRItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                LogicalRecordQueryBean bean = (LogicalRecordQueryBean)((StructuredSelection) comboTargetLRViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {                    
                    EditorOpener.open(bean.getId(), ComponentType.LogicalRecord);                        
                }
            }
        });
        
        depChkTargetLRItem = new MenuItem(menu, SWT.PUSH);
        depChkTargetLRItem.setText("Dependency Checker");
        depChkTargetLRItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                LogicalRecordQueryBean bean = (LogicalRecordQueryBean)((StructuredSelection) comboTargetLRViewer
                    .getSelection()).getFirstElement();
                if (bean != null && bean.getId() != 0) {                    
                    DepCheckOpener.open(bean);
                }                
            }
        });
        
        comboTargetLR.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    LogicalRecordQueryBean bean = (LogicalRecordQueryBean) ((StructuredSelection) comboTargetLRViewer.getSelection()).getFirstElement();
                    if (bean != null) {   
                        opEdTargetLRItem.setEnabled(true);
                        depChkTargetLRItem.setEnabled(true);
                    }
                    else {
                        opEdTargetLRItem.setEnabled(false);
                        depChkTargetLRItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
        });        
    }       

    private void addTargetLFOpenEditorMenu()
    {
        Text text = comboTargetLF.getTextControl();
        Menu menu = text.getMenu();
        opEdTargetLFItem = new MenuItem(menu, SWT.PUSH);
        opEdTargetLFItem.setText("Open Editor");
        opEdTargetLFItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                ComponentAssociation ass = (ComponentAssociation)((StructuredSelection) comboTargetLFViewer
                        .getSelection()).getFirstElement();
                if (ass != null) {                    
                    EditorOpener.open(ass.getAssociatedComponentIdNum(), ComponentType.LogicalFile);                        
                }
            }
        });
        
        depChkTargetLFItem = new MenuItem(menu, SWT.PUSH);
        depChkTargetLFItem.setText("Dependency Checker");
        depChkTargetLFItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                ComponentAssociation ass = (ComponentAssociation)((StructuredSelection) comboTargetLFViewer
                    .getSelection()).getFirstElement();
                if (ass != null) {                    
                    DepCheckOpener.open(new LogicalFileQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(), 
                        ass.getAssociatedComponentIdNum(), 
                        null, null, null, null, null, null));
                }                
            }
        });
        
        comboTargetLF.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    ComponentAssociation bean = (ComponentAssociation) ((StructuredSelection) comboTargetLFViewer.getSelection()).getFirstElement();
                    if (bean != null) {   
                        opEdTargetLFItem.setEnabled(true); 
                        depChkTargetLFItem.setEnabled(true);
                    }
                    else {
                        opEdTargetLFItem.setEnabled(false);
                        depChkTargetLFItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
        });        
    }       
    
	private void createSectionTarget(Composite compositeSourceTarget) {
		sectionTarget = safrGuiToolkit.createSection(compositeSourceTarget,
				Section.TITLE_BAR, "Target - Total Width: 0                ");
		FormData dataSectionTarget = new FormData();
		dataSectionTarget.left = new FormAttachment(sectionStepsList, 20);
		sectionTarget.setLayoutData(dataSectionTarget);

		Composite compositeTarget = safrGuiToolkit.createComposite(
				sectionTarget, SWT.NONE);
		compositeTarget.setLayout(new FormLayout());

		Label labelTargetLR = safrGuiToolkit.createLabel(compositeTarget,
				SWT.NONE, "Logical R&ecord:");
		FormData dataLabelTargetLR = new FormData();
		dataLabelTargetLR.width = SWT.DEFAULT;
		dataLabelTargetLR.left = new FormAttachment(0, 0);
		dataLabelTargetLR.top = new FormAttachment(0, 10);
		labelTargetLR.setLayoutData(dataLabelTargetLR);
		labelTargetLR.addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_MNEMONIC) {
					if (toggleAccKey_e) {
						e.doit = false;
						toggleAccKey_e = false;
					}
				}
			}

		});

		comboTargetLRViewer = safrGuiToolkit.createTableComboForComponents(
				compositeTarget, ComponentType.LogicalRecord);		
		comboTargetLR = comboTargetLRViewer.getTableCombo();
		comboTargetLR.setData(SAFRLogger.USER, "Target Logical Record");        		
		FormData dataComboTargetLR = new FormData();
		dataComboTargetLR.left = new FormAttachment(0, TARGET_DATA_OFFSET);
		dataComboTargetLR.top = new FormAttachment(0, 10);
		dataComboTargetLR.width = LR_DATA_WIDTH;
		comboTargetLR.setLayoutData(dataComboTargetLR);
		addTargetLROpenEditorMenu();
		
		Label labelTargetLF = safrGuiToolkit.createLabel(compositeTarget,
				SWT.NONE, "Logical F&ile:");
		FormData dataLabelTargetLF = new FormData();
		dataLabelTargetLF.width = SWT.DEFAULT;
		dataLabelTargetLF.top = new FormAttachment(comboTargetLR, 10);
		labelTargetLF.setLayoutData(dataLabelTargetLF);
		labelTargetLF.addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				if (toggleAccKey_i) {
					e.doit = false;
					toggleAccKey_i = false;
				}
			}

		});

		comboTargetLFViewer = safrGuiToolkit
				.createTableComboForAssociatedComponents(compositeTarget);
		comboTargetLF = comboTargetLFViewer.getTableCombo();
		comboTargetLF.setData(SAFRLogger.USER, "Target Logical File");        				
		FormData dataComboTargetLF = new FormData();
		dataComboTargetLF.left = new FormAttachment(0, TARGET_DATA_OFFSET);
		dataComboTargetLF.top = new FormAttachment(comboTargetLR, 10);
		dataComboTargetLF.width = LR_DATA_WIDTH;
		comboTargetLF.setLayoutData(dataComboTargetLF);
        addTargetLFOpenEditorMenu();

		labelUserExit = safrGuiToolkit.createLabel(compositeTarget, SWT.NONE, "Lookup User-Exit Routine:");
		FormData dataUserExitLbl = new FormData();
		dataUserExitLbl.top = new FormAttachment(comboTargetLF, 10);
		dataUserExitLbl.width = SWT.DEFAULT;
		labelUserExit.setLayoutData(dataUserExitLbl);
		
        textUserExitRoutine = safrGuiToolkit.createTextBox(compositeTarget, SWT.NONE);
        textUserExitRoutine.setData(SAFRLogger.USER, "User-Exit Param");                                     
		FormData dataUserExit = new FormData();
		dataUserExit.left = new FormAttachment(0, TARGET_DATA_OFFSET);
		dataUserExit.top = new FormAttachment(comboTargetLF, 10);
		dataUserExit.width = LR_DATA_WIDTH;
        textUserExitRoutine.setLayoutData(dataUserExit);
        textUserExitRoutine.setEnabled(false);
        textUserExitRoutine.setText(getExitText(currentStep));
        
        
        userParamLabel = safrGuiToolkit.createLabel(compositeTarget, SWT.NONE, "    Parameters:");
		FormData dataUserParamLbl = new FormData();
		dataUserParamLbl.top = new FormAttachment(textUserExitRoutine, 10);
		dataUserParamLbl.left = new FormAttachment(0, 20);
		dataUserParamLbl.width = SWT.DEFAULT;
		userParamLabel.setLayoutData(dataUserParamLbl);
        
        textUserExitRoutineParam = safrGuiToolkit.createTextBox(compositeTarget, SWT.NONE);
        textUserExitRoutineParam.setData(SAFRLogger.USER, "User-Exit Param");                                     
		FormData dataUserExitParam = new FormData();
		dataUserExitParam.left = new FormAttachment(0, TARGET_DATA_OFFSET);
		dataUserExitParam.top = new FormAttachment(textUserExitRoutine, 10);
		dataUserExitParam.width = LR_DATA_WIDTH;
		textUserExitRoutineParam.setLayoutData(dataUserExitParam);
        textUserExitRoutineParam.setEnabled(false);
        textUserExitRoutineParam.setText(getExitParams(currentStep));
		
		tableViewerTarget = safrGuiToolkit.createTableViewer(compositeTarget,
				SWT.FULL_SELECTION | SWT.BORDER, false);
		tableTarget = tableViewerTarget.getTable();
		tableTarget.setHeaderVisible(true);
		tableTarget.setLinesVisible(true);
		FormData dataTableTarget = new FormData();
		dataTableTarget.left = new FormAttachment(0, 0);
		dataTableTarget.top = new FormAttachment(textUserExitRoutineParam, 10);
		dataTableTarget.width = 600;
		dataTableTarget.height = 105;
		tableTarget.setLayoutData(dataTableTarget);

		tableViewerTarget.setContentProvider(new TargetTableContentProvider());

		int numberOfCol = targetColumnHeaders.length;
		for (int counter = 0; counter < numberOfCol; counter++) {
			TableViewerColumn column = new TableViewerColumn(tableViewerTarget,
					SWT.NONE);
			column.getColumn().setText(targetColumnHeaders[counter]);
			column.getColumn().setToolTipText(targetColumnHeaders[counter]);
			column.getColumn().setWidth(targetColumnWidths[counter]);
			column.getColumn().setResizable(true);
		}

		tableViewerTarget.setLabelProvider(new TargetTableLabelProvider());

		tableTarget.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                LRField field = (LRField)e.item.getData();
                if (field == null) {
                    tableTarget.setData(SAFRLogger.USER, "Target Primary Key");
                }
                else {
                    tableTarget.setData(SAFRLogger.USER, "Target Primary Key " + field.getName());                    
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
		    
		});
		
		comboTargetLR.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				if (comboTargetLR.getTable().getSelectionCount() > 0) {
					prevTrgLR = (EnvironmentalQueryBean) comboTargetLR
							.getTable().getSelection()[0].getData();
				} else {
					prevTrgLR = null;
				}
			}

			public void focusLost(FocusEvent e) {
				try {
					getSite().getShell().setCursor(
							getSite().getShell().getDisplay().getSystemCursor(
									SWT.CURSOR_WAIT));

					if (selectedTargetLR.equals(comboTargetLR.getText())) {
						return;
					}

					EnvironmentalQueryBean bean = null;
					if (comboTargetLR.getTable().getSelectionCount() > 0) {
						bean = (EnvironmentalQueryBean) comboTargetLR
								.getTable().getSelection()[0].getData();
						LogicalRecord logicalRecord = getLogicalRecordFromCombo(comboTargetLR);
						if (logicalRecord != null) {
							if (logicalRecord.getPrimayKeyLength() > 0) {
								try {
									currentStep.setTargetLR(logicalRecord);
									populateLogicalFile(comboTargetLF,
											logicalRecord);
									// reset target LF index as its
									// refreshed now.
									comboTargetLF.select(-1);
									selectedTargetLF = "";
									 comboTargetLFViewer.refresh();
									refreshTargetTable(currentStep);
									
									tableViewerStepsList.refresh();
									setDirty(true);
								} catch (SAFRValidationException sve) {
									DependencyMessageDialog.openDependencyDialog(
										getSite().getShell(),
										"GenevaERS Workbench",
										"You cannot change this target Logical Record as it is being used in the source fields of subsequent steps as indicated below:",
										sve.getMessageString(),
										MessageDialog.ERROR,
										new String[] { IDialogConstants.OK_LABEL },
										0);
									// condition to select back the grayed
									// out
									// element CQ8755
									if ((comboTargetLR
											.indexOf(selectedTargetLR)) > 0) {
										comboTargetLR.select(comboTargetLR
												.indexOf(selectedTargetLR));
									} else {
										comboTargetLR.setText(selectedTargetLR);
									}

								} catch (SAFRException se) {
									UIUtilities.handleWEExceptions(se);
								}
							} else {

								MessageDialog.openError(getSite().getShell(),
								    "GenevaERS Workbench",
								    "The Logical Record you have selected does not have a primary key defined.");

								if ((comboTargetLR.indexOf(selectedTargetLR)) > 0) {
									comboTargetLR.select(comboTargetLR.indexOf(selectedTargetLR));
								} else {
									comboTargetLR.setText(selectedTargetLR);
								}
								comboTargetLR.setFocus();
							}
							selectedTargetLR = comboTargetLR.getText();
							textUserExitRoutine.setText(getExitText(currentStep));
							textUserExitRoutineParam.setText(getExitParams(currentStep));
							if (comboTargetLR.getTable().getSelection().length > 0) {
								prevTrgLR = (EnvironmentalQueryBean) comboTargetLR.getTable().
								    getSelection()[0].getData();
							}
						}

						prevTrgLR = bean;

					} else {
						if (prevTrgLR != null) {
							comboTargetLR.setText(UIUtilities.getComboString(
									prevTrgLR.getName(), prevTrgLR.getId()));
							textUserExitRoutine.setText(getExitText(currentStep));
							textUserExitRoutineParam.setText(getExitParams(currentStep));
						} else {
							comboTargetLR.setText("");
						}
					}

				} finally {
					getSite().getShell().setCursor(null);
				}
			}

		});

		comboTargetLF.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				if (comboTargetLF.getTable().getSelectionCount() > 0) {
					prevTrgLF = (ComponentAssociation) comboTargetLF.getTable()
							.getSelection()[0].getData();
				} else {
					prevTrgLF = null;
				}
			}

			public void focusLost(FocusEvent e) {
				if (selectedTargetLF.equals(comboTargetLF.getText())) {
					return;
				}

				ComponentAssociation assoc = null;
				if (comboTargetLF.getTable().getSelectionCount() > 0) {
					assoc = (ComponentAssociation) comboTargetLF.getTable()
							.getSelection()[0].getData();
					try {
						currentStep.setTargetLRLFAssociation(
						    (ComponentAssociation) comboTargetLF.getTable().getSelection()[0].getData());
						checkValidLookupPath(currentStep);
						tableViewerStepsList.refresh();
						setDirty(true);

					} catch (SAFRValidationException sve) {
						DependencyMessageDialog
								.openDependencyDialog(
										getSite().getShell(),
										"GenevaERS Workbench",
										"You cannot change this target Logical File as it is being used in the source fields of subsequent steps as indicated below:",
										sve.getMessageString(),
										MessageDialog.ERROR,
										new String[] { IDialogConstants.OK_LABEL },
										0);
						if ((comboTargetLF.indexOf(selectedTargetLF)) > 0) {
							comboTargetLF.select(comboTargetLF
									.indexOf(selectedTargetLF));
						} else {
							comboTargetLF.setText(selectedTargetLF);
						}
					} catch (SAFRException e1) {
						UIUtilities.handleWEExceptions(e1);
					}
					prevTrgLF = assoc;
				} else {
					if (prevTrgLF != null) {
						comboTargetLF.setText(UIUtilities.getComboString(
								prevTrgLF.getAssociatedComponentName(),
								prevTrgLF.getAssociatedComponentIdNum()));
					} else {
						comboTargetLF.setText("");
					}
				}

				selectedTargetLF = comboTargetLF.getText();
			}
		});

		sectionTarget.setClient(compositeTarget);
	}

	private String getExitParams(LookupPathStep step) {
		if(step != null && step.getTargetLR() != null) {
			return step.getTargetLR().getLookupExitParams();
		} else {
			return "";
		}
	}

	private String getExitText(LookupPathStep step) {
		if(step != null && step.getTargetLR() != null) {
			UserExitRoutine ler = step.getTargetLR().getLookupExitRoutine();
			return ler != null ? ler.getComboString() : "";
		} else {
			return "";
		}
	}

	private void createSectionSourceFieldProperties(
			Composite compositeDefinition) {
		sectionSourceFieldProperties = safrGuiToolkit.createSection(
				compositeDefinition, Section.TITLE_BAR | Section.TWISTIE,
				"Source Field Properties");
		FormData dataSectionSourceFieldProperties = new FormData();
		dataSectionSourceFieldProperties.left = new FormAttachment(
				sectionSource, 20);
		dataSectionSourceFieldProperties.width = 600;
		sectionSourceFieldProperties
				.setLayoutData(dataSectionSourceFieldProperties);

		Composite compositeSourceFieldProperties = safrGuiToolkit
				.createComposite(sectionSourceFieldProperties, SWT.NONE);
		FormLayout layoutCompositeSourceFieldProperties = new FormLayout();
		layoutCompositeSourceFieldProperties.marginTop = UIUtilities.TOPMARGIN;
		layoutCompositeSourceFieldProperties.marginBottom = UIUtilities.BOTTOMMARGIN;
		layoutCompositeSourceFieldProperties.marginLeft = UIUtilities.LEFTMARGIN;
		layoutCompositeSourceFieldProperties.marginRight = UIUtilities.RIGHTMARGIN;
		
		compositeSourceFieldProperties
				.setLayout(layoutCompositeSourceFieldProperties);

		createSectionFieldSource(compositeSourceFieldProperties);
		createSectionDataAttributes(compositeSourceFieldProperties);

		showSectionSourceFieldProperties(true);
		sectionSourceFieldProperties.setClient(compositeSourceFieldProperties);
	}

	private void createSectionFieldSource(
			Composite compositeSourceFieldProperties) {
		sectionFieldSource = safrGuiToolkit.createSection(
				compositeSourceFieldProperties, Section.TITLE_BAR, "Source");
		sectionFieldSource.setLayoutData(new FormData());

		Composite compositeFieldSource = safrGuiToolkit.createComposite(
				sectionFieldSource, SWT.NONE);
		compositeFieldSource.setLayout(new FormLayout());

		// dummmy label for alignment.
		Label labelDummy = safrGuiToolkit.createLabel(compositeFieldSource,
				SWT.NONE, "Dummy Fields H");
		FormData dataLabelDateTimeFormat = new FormData();
		// dataLabelDate	TimeFormat.top = new FormAttachment(textLength, 10);
		dataLabelDateTimeFormat.width = SWT.DEFAULT;
		labelDummy.setLayoutData(dataLabelDateTimeFormat);
		labelDummy.setVisible(false);

		radiofield = safrGuiToolkit.createRadioButton(compositeFieldSource,"LR Field:");
		FormData lrfieldform = new FormData();
		lrfieldform.width = SWT.DEFAULT;
		lrfieldform.top = new FormAttachment(0, 5);
		radiofield.setLayoutData(lrfieldform);
		
		Label labelSourceLR = safrGuiToolkit.createLabel(compositeFieldSource,SWT.NONE, "LR:");
		FormData helloformdata = new FormData();
		helloformdata.width = SWT.DEFAULT;
		helloformdata.left = new FormAttachment(0, 20);
		helloformdata.top = new FormAttachment(labelDummy, 5);
		labelSourceLR.setLayoutData(helloformdata);
		
		labelSourceLR.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_MNEMONIC) {
					if (toggleAccKey_e) {
						e.doit = false;
						toggleAccKey_e = false;
					} else {
						toggleAccKey_e = true;
					}
				}
			}

		});
		
		comboSourceLRViewer = safrGuiToolkit.createTableComboForComponents(
				compositeFieldSource, ComponentType.LogicalRecord);
		comboSourceLR = comboSourceLRViewer.getTableCombo();
		comboSourceLR.setData(SAFRLogger.USER, "Source Logical Record");        		
		FormData dataComboSourceLR = new FormData();
		dataComboSourceLR.left = new FormAttachment(labelSourceLR, 65);
		dataComboSourceLR.top = new FormAttachment(radiofield, 5);
		dataComboSourceLR.width = 305;
		comboSourceLR.setLayoutData(dataComboSourceLR);	
		comboSourceLR.setVisible(true);
		addSourceLROpenEditorMenu();
		
		Label radioLRField1 = safrGuiToolkit.createLabel(compositeFieldSource,SWT.NONE, "Field:");

		radioLRField1.setData(SAFRLogger.USER, "Source Field Type LR Field");        
		
		FormData dataRadioLRField = new FormData();
		dataRadioLRField.top = new FormAttachment(labelSourceLR, 15);
		dataRadioLRField.left = new FormAttachment(0, 20);
		dataRadioLRField.width = 80;
		radioLRField1.setLayoutData(dataRadioLRField);

		comboLRFieldViewer = safrGuiToolkit
				.createTableComboForComponents(compositeFieldSource);

		comboLRFieldViewer.setContentProvider(new ArrayContentProvider());
		comboLRFieldViewer.setLabelProvider(new LRComboLabelProvider());
		
		comboLRField = comboLRFieldViewer.getTableCombo();
		comboLRField.setData(SAFRLogger.USER, "LR Field Selection");        		
		comboLRField.setShowTableHeader(true);
		
		FormData dataComboLRField = new FormData();
		dataComboLRField.top = new FormAttachment(labelSourceLR, 17);
		dataComboLRField.left = new FormAttachment(labelDummy, 12);
		dataComboLRField.width = 305;
		comboLRField.setLayoutData(dataComboLRField);

		addLRFieldOpenEditorMenu();

		
		comboLRField.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			@SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent e) {
				try {
					if (selectedSrcLRField.equals(comboLRField.getText())) {
						return;

					}
					if (comboLRField.getSelectionIndex() >= 0) {

						
						LookupPathSourceField sourceField = (LookupPathSourceField) currentStep
								.getSourceFields().getActiveItems().get(
										tableSource.getSelectionIndex());
						List<SAFRPersistentObject> list = (List<SAFRPersistentObject>) comboLRField
								.getTable().getSelection()[0].getData();

						LogicalRecord logicalRecord = (LogicalRecord) list
								.get(0);
						ComponentAssociation LRLFAssociation = (ComponentAssociation) list
								.get(1);
						LRField field = (LRField) list.get(2);

						sourceField.setSourceLRField(field);
						sourceField.setSourceFieldSourceLR(logicalRecord);
						// if (LRLFAssociation != null) {
						sourceField
								.setSourceFieldLRLFAssociation(LRLFAssociation);
						// }

						refreshSourceTable(currentStep);
						tableViewerStepsList.refresh();
						if (field != null) {
							loadDataAttributes(field);
						}
						setDirty(true);
					}
					selectedSrcLRField = comboLRField.getText();
					selectedSrcLR = comboSourceLR.getText();
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1,"Unexpected error occurred while loading data atttibutes.",null);
				}

			}

		});

		comboSourceLR.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				try {
					getSite().getShell().setCursor(
							getSite().getShell().getDisplay().getSystemCursor(
									SWT.CURSOR_WAIT));

					if (selectedSourceLR.equals(comboSourceLR.getText())) {
						return;
					}
					EnvironmentalQueryBean bean = null;

					if (comboSourceLR.getTable().getSelectionCount() > 0) {
					    
						bean = (EnvironmentalQueryBean) comboSourceLR
								.getTable().getSelection()[0].getData();
						boolean flag = true;
						try {
							// only the first step's source LR is editable.
							if(currentStep.getSequenceNumber()==1){
								lookupPath.setSourceLR(selectedGeneralLR,currentStep);
							}
							else{
								lookupPath.setSourceLR(getLogicalRecordFromCombo(comboSourceLR),currentStep);
							}
						} catch (SAFRValidationException sve) {
							flag = false;
							DependencyMessageDialog.openDependencyDialog(
								getSite().getShell(),
								"GenevaERS Workbench",
								"You cannot change this source Logical Record as it is being used in the source fields of the current step and/or subsequent steps as indicated below:",
								sve.getMessageString(),
								MessageDialog.ERROR,
								new String[] { IDialogConstants.OK_LABEL },
								0);
							// condition to select back the grayed out
							// element
							// CQ8755

							if ((comboSourceLR.indexOf(selectedSourceLR)) > 0 && comboSourceLR!=null && prevSrcLR!=null) {
								comboSourceLR.select(comboSourceLR.indexOf(
								    UIUtilities.getComboString(prevSrcLR.getName(),prevSrcLR.getId())));

							} else {
								if(comboSourceLR!=null && step1lr!=null) {
									comboSourceLR.setText(UIUtilities.getComboString(step1lrname,step1lrid));
								}
								
							}

						} catch (SAFRException e1) {
							UIUtilities.handleWEExceptions(e1);
						}
						if (flag) {
							prevSrcLR = bean;
						}

						populateLRField(currentStep,comboSourceLR.getText());
						//populateSourceLogicalRecord(currentStep);
						tableViewerStepsList.refresh();
						setDirty(true);
					} else {
						if (prevSrcLR != null) {
							comboSourceLR.setText(UIUtilities.getComboString(
									prevSrcLR.getName(), prevSrcLR.getId()));
						} else {
							comboSourceLR.setText("");
						}
					}

					selectedSourceLR = comboSourceLR.getText();
				} finally {
					getSite().getShell().setCursor(null);
				}
			}
		});
		radioConstant = safrGuiToolkit.createRadioButton(compositeFieldSource,
				"Con&stant:");
		radioConstant.setData(SAFRLogger.USER, "Source Field Type Constant");        				
		FormData dataRadioConstant = new FormData();
		dataRadioConstant.top = new FormAttachment(comboLRField, 10);
		radioConstant.setLayoutData(dataRadioConstant);

		Label labelConstantValue = safrGuiToolkit.createLabel(
				compositeFieldSource, SWT.NONE, "&Value: ");
		FormData dataLabelConstantValue = new FormData();
		dataLabelConstantValue.top = new FormAttachment(radioConstant, 5);
		dataLabelConstantValue.left = new FormAttachment(0, 17);
		dataLabelConstantValue.width = 75;
		labelConstantValue.setLayoutData(dataLabelConstantValue);

		textConstantValue = safrGuiToolkit.createTextBox(compositeFieldSource, SWT.NONE);
		textConstantValue.setData(SAFRLogger.USER, "Constant Value");        				
		
		FormData dataTextConstantValue = new FormData();
		dataTextConstantValue.top = new FormAttachment(radioConstant, 5);
		dataTextConstantValue.left = new FormAttachment(labelDummy, 10);
		dataTextConstantValue.right = new FormAttachment(100, 0);
		textConstantValue.setLayoutData(dataTextConstantValue);
		textConstantValue.setTextLimit(MAXSOURCEVALUE);

		textConstantValue.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				setDirty(true);
			}
		});
		textConstantValue.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				LookupPathSourceField sourceField = (LookupPathSourceField) currentStep
						.getSourceFields().getActiveItems().get(
								tableSource.getSelectionIndex());

				sourceField.setSourceValue(textConstantValue.getText());
				int constantLength = textConstantValue.getText().length();

				UIUtilities.checkNullText(textLength, Integer
						.toString(constantLength));
				sourceField.setLength(UIUtilities.stringToInteger(textLength
						.getText()));
				try {
					refreshSourceTable(currentStep);
				} catch (SAFRException e2) {
					UIUtilities.handleWEExceptions(e2);
				}
				tableViewerStepsList.refresh();

				try {
					refreshSourceTable(currentStep);
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1);
				}
			}

		});

		radioSymbol = safrGuiToolkit.createRadioButton(compositeFieldSource,
				"Sym&bol:");
		radioSymbol.setData(SAFRLogger.USER, "Source Field Type Symbol");        						
		FormData dataRadioSymbol = new FormData();
		dataRadioSymbol.top = new FormAttachment(textConstantValue, 10);
		radioSymbol.setLayoutData(dataRadioSymbol);

		Label labelSymbolName = safrGuiToolkit.createLabel(
				compositeFieldSource, SWT.NONE, "N&ame: ");
		FormData dataLabelSymbolName = new FormData();
		dataLabelSymbolName.top = new FormAttachment(radioSymbol, 5);
		dataLabelSymbolName.left = new FormAttachment(0, 17);
		dataLabelSymbolName.width = 83;
		labelSymbolName.setLayoutData(dataLabelSymbolName);

		textSymbolName = safrGuiToolkit.createTextBox(compositeFieldSource,SWT.NONE);
		textSymbolName.setData(SAFRLogger.USER, "Symbol Name");        						
		FormData dataTextSymbolName = new FormData();
		dataTextSymbolName.top = new FormAttachment(radioSymbol, 5);
		dataTextSymbolName.left = new FormAttachment(labelDummy, 10);
		dataTextSymbolName.right = new FormAttachment(100, 0);
		textSymbolName.setLayoutData(dataTextSymbolName);
		textSymbolName.setTextLimit(MAXSYMBOLNAME);
		textSymbolName.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				setDirty(true);
			}
		});
		textSymbolName.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				LookupPathSourceField sourceField = (LookupPathSourceField) currentStep
						.getSourceFields().getActiveItems().get(
								tableSource.getSelectionIndex());

				sourceField.setSymbolicName(textSymbolName.getText());
				try {
					refreshSourceTable(currentStep);
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1);
				}
			}

		});

		Label labelSymbolDefaultValue = safrGuiToolkit.createLabel(
				compositeFieldSource, SWT.NONE, "Default &Value:");
		FormData dataLabelSymbolDefaultValue = new FormData();
		dataLabelSymbolDefaultValue.top = new FormAttachment(textSymbolName, 10);
		dataLabelSymbolDefaultValue.left = new FormAttachment(0, 17);
		dataLabelSymbolDefaultValue.width = SWT.DEFAULT;
		labelSymbolDefaultValue.setLayoutData(dataLabelSymbolDefaultValue);

		textSymbolDefaultValue = safrGuiToolkit.createTextBox(
				compositeFieldSource, SWT.NONE);
		textSymbolDefaultValue.setData(SAFRLogger.USER, "Symbol Default Value");        						
		
		FormData dataTextSymbolDefaultValue = new FormData();
		dataTextSymbolDefaultValue.top = new FormAttachment(textSymbolName, 10);
		dataTextSymbolDefaultValue.left = new FormAttachment(labelDummy, 10);
		dataTextSymbolDefaultValue.right = new FormAttachment(100, 0);
		textSymbolDefaultValue.setLayoutData(dataTextSymbolDefaultValue);
		textSymbolDefaultValue.setTextLimit(MAXSOURCEVALUE);
		textSymbolDefaultValue.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				setDirty(true);
			}
		});

		textSymbolDefaultValue.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				LookupPathSourceField sourceField = (LookupPathSourceField) currentStep
						.getSourceFields().getActiveItems().get(
								tableSource.getSelectionIndex());

				sourceField.setSourceValue(textSymbolDefaultValue.getText());
				try {
					refreshSourceTable(currentStep);
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1);
				}
			}

		});

		radiofield.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
				LookupPathSourceField sourceField = (LookupPathSourceField) currentStep
						.getSourceFields().getActiveItems().get(
								tableSource.getSelectionIndex());
				try {
					sourceField
							.setSourceFieldType(LookupPathSourceFieldType.LRFIELD);
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1,"Error occurred while setting the source field type.",null);
				}
				selectRadioButton(radiofield);
			}
		});

		radioConstant.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
				LookupPathSourceField sourceField = (LookupPathSourceField) currentStep
						.getSourceFields().getActiveItems().get(
								tableSource.getSelectionIndex());
				try {
					sourceField
							.setSourceFieldType(LookupPathSourceFieldType.CONSTANT);
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1,"Error occurred while setting the source field type.",null);
				}
				selectRadioButton(radioConstant);
			}
		});

		radioSymbol.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
				LookupPathSourceField sourceField = (LookupPathSourceField) currentStep
						.getSourceFields().getActiveItems().get(
								tableSource.getSelectionIndex());
				try {
					sourceField
							.setSourceFieldType(LookupPathSourceFieldType.SYMBOL);
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1,"Error occurred while setting the source field type.",null);
				}
				selectRadioButton(radioSymbol);
			}
		});

		sectionFieldSource.setClient(compositeFieldSource);

	}

    private void addLRFieldOpenEditorMenu()
    {
        Text text = comboLRField.getTextControl();
        Menu menu = text.getMenu();
        opEdSourceFldItem = new MenuItem(menu, SWT.PUSH);
        opEdSourceFldItem.setText("Open Editor");
        opEdSourceFldItem.addListener(SWT.Selection, new Listener()
        {
            @SuppressWarnings("unchecked")
			public void handleEvent(Event event)
            {
            	List<SAFRPersistentObject> list = (List<SAFRPersistentObject>) comboLRField
						.getTable().getSelection()[0].getData();
                if (list != null) {   
    				LRField field = (LRField) list.get(2);            	
                    EditorOpener.open(field.getId(), ComponentType.LogicalRecordField);                        
                }                
            }
        });
        
        depChkSourceFldItem = new MenuItem(menu, SWT.PUSH);
        depChkSourceFldItem.setText("Dependency Checker");
        depChkSourceFldItem.addListener(SWT.Selection, new Listener()
        {
            @SuppressWarnings("unchecked")
            public void handleEvent(Event event)
            {
                List<SAFRPersistentObject> list = (List<SAFRPersistentObject>) comboLRField
                    .getTable().getSelection()[0].getData();
                if (list != null) {   
                    LRField field = (LRField) list.get(2);              
                    DepCheckOpener.open(new LogicalRecordFieldQueryBean(
                        field.getEnvironmentId(), field.getId(), 
                        field.getLogicalRecord().getId(), 
                        null, null, null, null, null));                        
                }                
            }
        });
        
        comboLRField.addMouseListener(new MouseListener() {

            @SuppressWarnings("unchecked")
			public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                	List<SAFRPersistentObject> list = (List<SAFRPersistentObject>) comboLRField
    						.getTable().getSelection()[0].getData();
                    if (list != null) {   
                        opEdSourceFldItem.setEnabled(true);
                        depChkSourceFldItem.setEnabled(true);
                    }
                    else {
                        opEdSourceFldItem.setEnabled(false);
                        depChkSourceFldItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }       
	
	private void createSectionDataAttributes(Composite compositeAddField) {
		sectionDataAttributes = safrGuiToolkit.createSection(compositeAddField,
				Section.TITLE_BAR, "Data Attributes");
		FormData dataSectionDataAttributes = new FormData();
		dataSectionDataAttributes.top = new FormAttachment(sectionFieldSource,
				23);
		sectionDataAttributes.setLayoutData(dataSectionDataAttributes);

		Composite compositeDataAttributes = safrGuiToolkit.createComposite(
				sectionDataAttributes, SWT.NONE);
		compositeDataAttributes.setLayout(new FormLayout());

		// dummy label for alignment.
		Label labelDummy = safrGuiToolkit.createLabel(compositeDataAttributes,
				SWT.NONE, "Date/Time Format:");
		FormData dataLabelDummy = new FormData();
		// dataLabelDataType.top = new FormAttachment(0, 10);
		dataLabelDummy.width = SWT.DEFAULT;
		labelDummy.setLayoutData(dataLabelDummy);
		labelDummy.setVisible(false);

		Label labelDataType = safrGuiToolkit.createLabel(
				compositeDataAttributes, SWT.NONE, "Data T&ype:");
		FormData dataLabelDataType = new FormData();
		dataLabelDataType.top = new FormAttachment(0, 10);
		dataLabelDataType.width = 100;
		labelDataType.setLayoutData(dataLabelDataType);

		comboDataType = safrGuiToolkit.createComboBox(compositeDataAttributes,
				SWT.READ_ONLY, "");
		FormData dataComboDataType = new FormData();
		dataComboDataType.top = new FormAttachment(0, 10);
		dataComboDataType.left = new FormAttachment(labelDummy, 10);
		dataComboDataType.width = 305;
		comboDataType.setLayoutData(dataComboDataType);
		UIUtilities.populateComboBox(comboDataType, CodeCategories.DATATYPE, 0, false);
		comboDataType.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (selectedSrcFieldDataType.equals(comboDataType.getText())) {
					return;
				}
				LookupPathSourceField field = (LookupPathSourceField) currentStep
						.getSourceFields().getActiveItems().get(
								tableSource.getSelectionIndex());
				Code dataType = UIUtilities.getCodeFromCombo(comboDataType);
				field.setDataTypeCode(dataType);
				dataTypeSettings(dataType,UIUtilities.getCodeFromCombo(comboDateTimeFormat));
				selectedSrcFieldDataType = comboDataType.getText();
		        UIUtilities.populateDateTimeComboBox(comboDateTimeFormat, field.isNumeric() , true);
				setDirty(true);
			}

		});

		Label labelLength = safrGuiToolkit.createLabel(compositeDataAttributes,
				SWT.NONE, "Le&ngth:");
		FormData dataLabelLength = new FormData();
		dataLabelLength.top = new FormAttachment(comboDataType, 10);
		dataLabelLength.width = 100;
		labelLength.setLayoutData(dataLabelLength);

		textLength = safrGuiToolkit.createIntegerTextBox(
				compositeDataAttributes, SWT.NONE, false);
		textLength.setTextLimit(MAXLENGTH);
		FormData dataTextLength = new FormData();
		dataTextLength.top = new FormAttachment(comboDataType, 10);
		dataTextLength.left = new FormAttachment(labelDummy, 10);
		dataTextLength.right = new FormAttachment(100, 0);
		textLength.setLayoutData(dataTextLength);
		textLength.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				setDirty(true);
			}
		});

		textLength.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				try {
					LookupPathSourceField field = (LookupPathSourceField) currentStep
							.getSourceFields().getActiveItems().get(
									tableSource.getSelectionIndex());
					field.setLength(UIUtilities.stringToInteger(textLength
							.getText()));
					refreshSourceTable(currentStep);
					tableViewerStepsList.refresh();
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1);
				}
			}

		});

		Label labelDateTimeFormat = safrGuiToolkit.createLabel(
				compositeDataAttributes, SWT.NONE, "Date/Time &Format:");

		FormData dataLabelDateTimeFormat = new FormData();
		dataLabelDateTimeFormat.top = new FormAttachment(textLength, 10);
		dataLabelDateTimeFormat.width = SWT.DEFAULT;
		labelDateTimeFormat.setLayoutData(dataLabelDateTimeFormat);

		comboDateTimeFormat = safrGuiToolkit.createComboBox(
				compositeDataAttributes, SWT.READ_ONLY, "");
		FormData dataComboDateTimeFormat = new FormData();
		dataComboDateTimeFormat.top = new FormAttachment(textLength, 10);
		dataComboDateTimeFormat.left = new FormAttachment(labelDummy, 10);
		dataComboDateTimeFormat.right = new FormAttachment(100, 0);
		comboDateTimeFormat.setLayoutData(dataComboDateTimeFormat);
		UIUtilities.populateDateTimeComboBox(comboDateTimeFormat, false, true);
		comboDateTimeFormat.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (selectedSrcFieldDateTimeFormat.equals(comboDateTimeFormat
						.getText())) {
					return;
				}
				LookupPathSourceField field = (LookupPathSourceField) currentStep
						.getSourceFields().getActiveItems().get(
								tableSource.getSelectionIndex());
				field.setDateTimeFormatCode(UIUtilities
						.getCodeFromCombo(comboDateTimeFormat));
				dataTypeSettings(UIUtilities.getCodeFromCombo(comboDataType),
						UIUtilities.getCodeFromCombo(comboDateTimeFormat));
				selectedSrcFieldDateTimeFormat = comboDateTimeFormat.getText();
				setDirty(true);
			}
		});

		Label labelScalingFactor = safrGuiToolkit.createLabel(
				compositeDataAttributes, SWT.NONE, "Scaling Fact&or:");
		FormData dataLabelScalingFactor = new FormData();
		dataLabelScalingFactor.top = new FormAttachment(comboDateTimeFormat, 10);
		dataLabelScalingFactor.width = 100;
		labelScalingFactor.setLayoutData(dataLabelScalingFactor);

		textScalingFactor = safrGuiToolkit.createIntegerTextBox(
				compositeDataAttributes, SWT.NONE, false);
		textScalingFactor.setTextLimit(MAXSCALING);
		FormData dataTextScalingFactor = new FormData();
		dataTextScalingFactor.top = new FormAttachment(comboDateTimeFormat, 10);
		dataTextScalingFactor.left = new FormAttachment(labelDummy, 10);
		dataTextScalingFactor.right = new FormAttachment(100, 0);
		textScalingFactor.setLayoutData(dataTextScalingFactor);
		textScalingFactor.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				setDirty(true);
			}
		});

		textScalingFactor.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {

				if (textScalingFactor.getText() == "") {
					textScalingFactor.setText("0");
				}
				try{
					LookupPathSourceField field = (LookupPathSourceField) currentStep
							.getSourceFields().getActiveItems().get(
									tableSource.getSelectionIndex());
					field.setScaling(UIUtilities.stringToInteger(textScalingFactor
							.getText()));
				}
				catch(ArrayIndexOutOfBoundsException ex){
					
				}

			}

		});

		Label labelDecimalPlaces = safrGuiToolkit.createLabel(
				compositeDataAttributes, SWT.NONE, "Decimal &Places:");
		FormData dataLabelDecimalPlaces = new FormData();
		dataLabelDecimalPlaces.top = new FormAttachment(textScalingFactor, 10);
		dataLabelDecimalPlaces.width = SWT.DEFAULT;
		labelDecimalPlaces.setLayoutData(dataLabelDecimalPlaces);

		textDecimalPlaces = safrGuiToolkit.createIntegerTextBox(
				compositeDataAttributes, SWT.NONE, false, "0");
		textDecimalPlaces.setTextLimit(MAXDECIMALPLACES);
		FormData dataTextDecimalPlaces = new FormData();
		dataTextDecimalPlaces.top = new FormAttachment(textScalingFactor, 10);
		dataTextDecimalPlaces.left = new FormAttachment(labelDummy, 10);
		dataTextDecimalPlaces.right = new FormAttachment(100, 0);
		textDecimalPlaces.setLayoutData(dataTextDecimalPlaces);
		textDecimalPlaces.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				setDirty(true);
			}

		});

		textDecimalPlaces.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {

				if (textDecimalPlaces.getText() == "") {
					textDecimalPlaces.setText("0");
				}
				LookupPathSourceField field = (LookupPathSourceField) currentStep
						.getSourceFields().getActiveItems().get(
								tableSource.getSelectionIndex());
				field.setDecimals(UIUtilities.stringToInteger(textDecimalPlaces
						.getText()));

			}

		});

		Label labelSigned = safrGuiToolkit.createLabel(compositeDataAttributes,
				SWT.NONE, "S&igned:");
		FormData dataLabelSigned = new FormData();
		dataLabelSigned.top = new FormAttachment(textDecimalPlaces, 10);
		dataLabelSigned.width = 100;
		labelSigned.setLayoutData(dataLabelSigned);
		labelSigned.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {

				if (toggleAccKey_i) {
					e.doit = false;
					toggleAccKey_i = false;
				} else {
					toggleAccKey_i = true;
				}
			}

		});

		checkboxSigned = safrGuiToolkit.createCheckBox(compositeDataAttributes,
				"");
		FormData dataCheckboxSigned = new FormData();
		dataCheckboxSigned.top = new FormAttachment(textDecimalPlaces, 10);
		dataCheckboxSigned.left = new FormAttachment(labelDummy, 10);
		dataCheckboxSigned.width = 172;
		checkboxSigned.setLayoutData(dataCheckboxSigned);
		checkboxSigned.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				LookupPathSourceField field = (LookupPathSourceField) currentStep
						.getSourceFields().getActiveItems().get(
								tableSource.getSelectionIndex());
				field.setSigned(checkboxSigned.getSelection());
				setDirty(true);
			}

		});

		Label labelNumericMask = safrGuiToolkit.createLabel(
				compositeDataAttributes, SWT.NONE, "Numeric Mas&k:");
		FormData dataLabelNumericMask = new FormData();
		dataLabelNumericMask.top = new FormAttachment(checkboxSigned, 10);
		dataLabelNumericMask.width = 100;
		labelNumericMask.setLayoutData(dataLabelNumericMask);

		comboNumericMask = safrGuiToolkit.createComboBox(
				compositeDataAttributes, SWT.READ_ONLY, "");
		FormData dataComboNumericMask = new FormData();
		dataComboNumericMask.top = new FormAttachment(checkboxSigned, 10);
		dataComboNumericMask.left = new FormAttachment(labelDummy, 10);
		dataComboNumericMask.right = new FormAttachment(100, 0);
		comboNumericMask.setLayoutData(dataComboNumericMask);
		UIUtilities.populateComboBox(comboNumericMask,
				CodeCategories.FORMATMASK, 0, true);
		comboNumericMask.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (selectedSrcFieldNumericMask.equals(comboNumericMask
						.getText())) {
					return;
				}
				LookupPathSourceField field = (LookupPathSourceField) currentStep
						.getSourceFields().getActiveItems().get(
								tableSource.getSelectionIndex());
				field.setNumericMaskCode(UIUtilities
						.getCodeFromCombo(comboNumericMask));
				selectedSrcFieldNumericMask = comboNumericMask.getText();
				setDirty(true);
			}

		});

		sectionDataAttributes.setClient(compositeDataAttributes);

	}

	@Override
	public void setFocus() {
		super.setFocus();
		textName.setFocus();
	}

	@Override
	public String getModelName() {
		return "Lookup Path";
	}

	@Override
	public void doRefreshControls() throws SAFRException {
		step1generallr = lookupPath.getLookupPathSteps().get(0).getSourceLR();
	    boolean dirty = isDirty();
		UIUtilities.checkNullText(textID, Integer.toString(lookupPath.getId()));
		
		// Refresh active status
		String status = "[";
		if (lookupPath.isValid()) {
			status += "Active] ";
            checkboxChangeStatus.setText("Active");
		} else {
            checkboxChangeStatus.setText("Inactive");            
			status += "Inactive] ";
		}
        if (lookupPath.getName() == null) {
            form.setText(status);           
        } else {
            form.setText(status + lookupPath.getName());         
        }		
		
		if (lookupPath.getId() >= 0) {
			LogicalRecordTransfer lr = DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO().getLogicalRecord(lookupPath.getSourceLRId(), lookupPath.getEnvironmentId());
			if(lr!=null){
				comboSourceLRGeneral.setText(lr.getName() + " ["+lr.getId()+"]");
			}
			UIUtilities.checkNullText(textName, lookupPath.getName());
			UIUtilities.checkNullText(textComments, lookupPath.getComment());
			labelCreatedValue.setText(lookupPath.getCreateBy() + " on "
					+ lookupPath.getCreateTimeString());
			labelModifiedValue.setText(lookupPath.getModifyBy() + " on "
					+ lookupPath.getModifyTimeString());
			if (lookupPath.getActivatedBy() == null || 
			    lookupPath.getActivatedTimeString() == null) {
			    labelActivatedValue.setText(defaultModStr);
			}  else {
			    labelActivatedValue.setText(lookupPath.getActivatedBy() + " on "
			     + lookupPath.getActivatedTimeString());
			}
			tableViewerStepsList.refresh();

		}
        setDirty(dirty);
	}

	private void checkAndDisplayLookupState() throws SAFRException {
		// if any of the lookup path step is invalid. Display a message to
		// user specifying the reason.
		SAFRValidationException sve = new SAFRValidationException();
		for (int i = 0; i < lookupPath.getLookupPathSteps().size(); i++) {
			try {
				lookupPath.getLookupPathSteps().get(i).checkValid();
			} catch (SAFRValidationException e) {
				// Jaydeep ,1st June 2009,CQ 8060: Lookup path state not
				// displayed properly.
				Map<Object, ArrayList<String>> errorMessageMap = e
						.getErrorMessageMap();
				for (Object key : errorMessageMap.keySet()) {
					sve.setErrorMessages(key, errorMessageMap.get(key));
				}
			}
		}
		if (!sve.getErrorMessages().isEmpty()) {
			if (lookupPath.isValid()) {
				// show information message to user
				MessageDialog
						.openInformation(
								getSite().getShell(),
								"GenevaERS Workbench",
								"This Lookup path was active when it was saved "
										+ "but it is inactive now because of the following reason(s):" + SAFRUtilities.LINEBREAK
										+ sve.getMessageString());
				lookupPath.setValid(false);
				checkboxChangeStatus.setEnabled(false);
			} else {
				// show information message to user
				MessageDialog.openInformation(getSite().getShell(),
						"GenevaERS Workbench",
						"This Lookup path is inactive because of the following reason(s):" + SAFRUtilities.LINEBREAK
								+ sve.getMessageString());
			}
		} else {
			if (lookupPath.isValid()) {
				checkboxChangeStatus.setData(false);
			} else {
				// this means the user manually set it to invalid or it was
				// set to invalid by a dependency check of another metadata.
				// In any case, allow user to manually set it to active
				// using the checkbox
				checkboxChangeStatus.setData(true);
			}
			checkboxChangeStatus.setEnabled(!safrGuiToolkit.isReadOnly());
		}
	}

	private void populateLogicalRecord(TableCombo comboTargetLR,
			TableCombo comboSourceLR) throws DAOException {
		Integer counter = 0;

		comboTargetLR.getTable().removeAll();
		comboSourceLR.getTable().removeAll();

		List<LogicalRecordQueryBean> logicalRecordList = SAFRQuery
				.queryAllActiveLogicalRecords(UIUtilities
						.getCurrentEnvironmentID(), SortType.SORT_BY_NAME);
		List<LogicalRecordQueryBean> listWithoutDuplicates = new ArrayList<>(new HashSet<>(logicalRecordList));

		comboSourceLRViewer.setInput(listWithoutDuplicates);
		comboTargetLRViewer.setInput(listWithoutDuplicates);
		comboSourceLRViewer.refresh();
		comboTargetLRViewer.refresh();
		for (LogicalRecordQueryBean logicalRecordBean : listWithoutDuplicates) {
			comboTargetLR.setData(Integer.toString(counter), logicalRecordBean);
			comboSourceLR.setData(Integer.toString(counter), logicalRecordBean);
			counter++;
		}

	}

	private void populateSourceLogicalRecord(LookupPathStep step){
		comboSourceLR.getTable().removeAll();
		int numberOfSteps = step.getSequenceNumber().intValue();
		List<LogicalRecordQueryBean> logicalRecordList = null;
		List<LogicalRecordQueryBean> list2 = new ArrayList<LogicalRecordQueryBean>();
		List<String> alllrs = new ArrayList<>();
		if(step.getSequenceNumber()==1){
			logicalRecordList = SAFRQuery
					.queryAllActiveLogicalRecords(UIUtilities
							.getCurrentEnvironmentID(), SortType.SORT_BY_NAME);
		}
		else{
			for (int i = 0; i < numberOfSteps; i++) {
				
				step = (LookupPathStep) lookupPath.getLookupPathSteps().getActiveItems().get(i);
				LogicalRecord sourceLR = step.getSourceLR();
				LogicalRecord targetLR = step.getTargetLR();
				if(targetLR!=null && !alllrs.contains(targetLR.getName())) {
					alllrs.add(targetLR.getName());
					LogicalRecordQueryBean lrfq = new LogicalRecordQueryBean(targetLR.getEnvironmentId(), targetLR.getId(), targetLR.getName(),"", targetLR.getTotalLength(), targetLR.getPrimayKeyLength(),
								"", null , targetLR.getCreateTime(), targetLR.getCreateBy(), targetLR.getModifyTime(), targetLR.getModifyBy(), targetLR.getActivatedTime(), targetLR.getActivatedBy());
					list2.add(lrfq);
				}
				
				if(sourceLR!=null && !alllrs.contains(sourceLR.getName())) {
					alllrs.add(sourceLR.getName());
					LogicalRecordQueryBean lrfq = new LogicalRecordQueryBean(sourceLR.getEnvironmentId(), sourceLR.getId(), sourceLR.getName(),"", sourceLR.getTotalLength(), sourceLR.getPrimayKeyLength(),
							"", null , sourceLR.getCreateTime(), sourceLR.getCreateBy(), sourceLR.getModifyTime(), sourceLR.getModifyBy(), sourceLR.getActivatedTime(), sourceLR.getActivatedBy());
					list2.add(lrfq);
				}
				
		}
		}
			if(step.getSequenceNumber()==1) {
				comboSourceLRViewer.setInput(logicalRecordList);
			}else {
				comboSourceLRViewer.setInput(list2);
			}
	}
	
	private void populateLogicalFile(TableCombo comboBox,
			LogicalRecord logicalRecord) {
		Integer counter = 0;

		comboBox.getTable().removeAll();

		SAFRList<ComponentAssociation> logicalFileAssociations = logicalRecord
				.getLogicalFileAssociations();
		comboTargetLFViewer.setInput(logicalFileAssociations);
		comboTargetLFViewer.refresh();
		for (ComponentAssociation association : logicalFileAssociations) {
			comboBox.setData(Integer.toString(counter++), association);
		}
	}

	private void resetSectionSourceFieldProperties() {
		radiofield.setSelection(true);
		radioConstant.setSelection(false);
		radioSymbol.setSelection(false);
		comboLRField.getTable().deselectAll();
		comboSourceLR.getTable().deselectAll();
		comboLRField.setText("");
		comboSourceLR.setText("");
		selectRadioButton(radiofield);
		comboDataType.deselectAll();
		comboDateTimeFormat.deselectAll();
		comboNumericMask.deselectAll();
		UIUtilities.checkNullText(textLength, "0");
		UIUtilities.checkNullText(textDecimalPlaces, "0");
		UIUtilities.checkNullText(textScalingFactor, "0");
		checkboxSigned.setSelection(false);
	}

	private void selectRadioButton(Button radioButton) {
		if (radioButton == radiofield) {
			comboLRField.setEnabled(!safrGuiToolkit.isReadOnly());
			comboSourceLR.setEnabled(true);
			textConstantValue.setEnabled(false);
			textConstantValue.setText("");
			textSymbolName.setEnabled(false);
			selectedSrcLRField = "";
			selectedSrcLR="";
			textSymbolName.setText("");
			textSymbolDefaultValue.setEnabled(false);
			textSymbolDefaultValue.setText("");
		} else if (radioButton == radioConstant) {
			textConstantValue.setEnabled(!safrGuiToolkit.isReadOnly());
			comboSourceLR.setEnabled(false);
			comboLRField.setEnabled(false);
			comboLRField.getTable().deselectAll();
			comboLRField.setText("");
			if(currentStep.getSequenceNumber()!=1){
				comboSourceLR.setText("");
			}
			selectedSrcLRField = "";
			selectedSrcLR="";
			textSymbolName.setEnabled(false);
			textSymbolName.setText("");
			textSymbolDefaultValue.setEnabled(false);
			textSymbolDefaultValue.setText("");
		} else if (radioButton == radioSymbol) {
			textSymbolName.setEnabled(!safrGuiToolkit.isReadOnly());
			textSymbolDefaultValue.setEnabled(!safrGuiToolkit.isReadOnly());
			comboSourceLR.setEnabled(false);
			comboLRField.setEnabled(false);
			comboLRField.getTable().deselectAll();
			comboLRField.setText("");
			if(currentStep.getSequenceNumber()!=1){
				comboSourceLR.setText("");
			}
			selectedSrcLRField = "";
			selectedSrcLR="";
			textConstantValue.setEnabled(false);
			textConstantValue.setText("");
		}
	}

	private Boolean checkNullSource(LookupPathStep step) throws SAFRException,
			DAOException {
		if (step.getSequenceNumber() == 1l) {
			if (step.getSourceLR() == null) {
				MessageDialog
						.openError(
								getSite().getShell(),
								"GenevaERS Workbench",
								"The current step must have a valid Source [Logical Record] before you can proceed.");
				return true;
			}
		} 
		else {
			if (step.getSourceLR() == null
					|| step.getSourceLRLFAssociation() == null) {
				MessageDialog
						.openError(
								getSite().getShell(),
								"GenevaERS Workbench",
								"The current step must have a valid Source [Logical Record/Logical File] before you can proceed.");
				return true;
			}
		}
		return false;

	}

	private Boolean checkNullLRLF(LookupPathStep step) throws SAFRException,
			DAOException {
		if (step.getSequenceNumber() == 1l) {
			if (step.getSourceLR() == null || step.getTargetLR() == null
					|| step.getTargetLRLFAssociation() == null) {
				MessageDialog
						.openError(
								getSite().getShell(),
								"GenevaERS Workbench",
								"The first step must have a valid Source [Logical Record] and Target [Logical Record/Logical File] before you can proceed.");
				return true;
			}
		} else {
			if (step.getSourceLRLFAssociation() == null
					|| step.getTargetLR() == null
					|| step.getTargetLRLFAssociation() == null) {
				MessageDialog
						.openError(
								getSite().getShell(),
								"GenevaERS Workbench",
								"The last step must have a valid Target [Logical Record/Logical File] before you can proceed.");
				return true;
			}
		}
		return false;
	}

	private void showSectionSourceFieldProperties(Boolean visible) {
		sectionSourceFieldProperties.setExpanded(visible);
		sectionSourceFieldProperties.setEnabled(visible);
	}

	private void removeSelectedSourceField() {
		try {
			if (tableSource.getSelectionIndex() == -1) {
				return;
			}
			LookupPathSourceField sourceField = (LookupPathSourceField) currentStep
					.getSourceFields().getActiveItems().get(
							tableSource.getSelectionIndex());
			String fieldName = getSourceFieldName(sourceField);
			if (!fieldName.equals("")) {
				fieldName = "'" + fieldName + "' ";
			}
			MessageDialog dialog = new MessageDialog(getSite().getShell(),
					"Confirm deletion", null,
					"Are you sure you want to delete the selected source field "
							+ fieldName + "from the list?",
					MessageDialog.QUESTION, new String[] { "&OK", "&Cancel" },
					0);
			int confirm = dialog.open();
			if (confirm == 0) {
				currentStep.deleteSourceField(sourceField);
				resetSectionSourceFieldProperties();
				refreshSourceTable(currentStep);
				// auto select the last item in list
				List<LookupPathSourceField> activeItems = currentStep
						.getSourceFields().getActiveItems();
				if (!activeItems.isEmpty()) {
					tableViewerSource.setSelection(new StructuredSelection(
							activeItems.get(activeItems.size() - 1)));
				}
				setDirty(true);
			}
		} catch (SAFRException se) {
			UIUtilities.handleWEExceptions(se,"Unexpected error occurred while retrieving source field.",null);

		}
	}

	private void removeSelectedStep() {
		try {
			int selectedStep = tableStepsList.getSelectionIndex();
			int selection = selectedStep + 1; // step sequence number
			// first step cannot be deleted
			if (selectedStep == 0) {
				MessageDialog
						.openError(getSite().getShell(), "GenevaERS Workbench",
								"You cannot delete the first step. A Lookup Path must have at least one step.");
				return;
			}

			MessageDialog dialog = new MessageDialog(getSite().getShell(),
					"GenevaERS Workbench", null,
					"Are you sure you want to delete step " + selection
							+ " and all steps after that?",
					MessageDialog.QUESTION, new String[] { "&OK", "&Cancel" },
					0);
			int confirm = dialog.open();
			if (confirm == 0) {
				lookupPath.removeStep(selectedStep);
				tableViewerStepsList.refresh();
				tableStepsList.select(selectedStep - 1);
				LookupPathStep stepAfterRemoval = (LookupPathStep) lookupPath
						.getLookupPathSteps().getActiveItems().get(
								selectedStep - 1);
				loadStepDefinition(stepAfterRemoval);
				showSectionSourceFieldProperties(false);
				setDirty(true);
			}

		} catch (DAOException de) {
			UIUtilities.handleWEExceptions(de,
			    "Unexpected database error occurred while deleting the step.",UIUtilities.titleStringDbException);
		} catch (SAFRException se) {
			UIUtilities.handleWEExceptions(se,"Unexpected error occurred while loading the step definition.",null);
		}
	}

	private void dataTypeSettings(Code dataTypeCode, Code dateTimeCode) {
		if (dataTypeCode == null) {
			return;
		}
		if (dataTypeCode.getGeneralId() == Codes.ALPHANUMERIC) {
			textDecimalPlaces.setText(DEFAULT_VALUE);
			textDecimalPlaces.setEnabled(false);
			checkboxSigned.setSelection(false);
			checkboxSigned.setEnabled(false);
			textScalingFactor.setText(DEFAULT_VALUE);
			textScalingFactor.setEnabled(false);
			comboDateTimeFormat.setEnabled(!safrGuiToolkit.isReadOnly());
			comboNumericMask.setEnabled(false);
			comboNumericMask.select(0);
		} else if (dataTypeCode.getGeneralId() == Codes.BINARY
				&& dateTimeCode != null) {
			checkboxSigned.setSelection(false);
			checkboxSigned.setEnabled(false);
			textDecimalPlaces.setEnabled(!safrGuiToolkit.isReadOnly());
			textScalingFactor.setEnabled(!safrGuiToolkit.isReadOnly());
			comboDateTimeFormat.setEnabled(!safrGuiToolkit.isReadOnly());
			comboNumericMask.setEnabled(false);
			comboNumericMask.select(0);
		} else if (dataTypeCode.getGeneralId() == Codes.EDITED_NUMERIC) {
			comboDateTimeFormat.select(0);
			comboDateTimeFormat.setEnabled(false);
			checkboxSigned.setEnabled(!safrGuiToolkit.isReadOnly());
			textDecimalPlaces.setEnabled(!safrGuiToolkit.isReadOnly());
			textScalingFactor.setEnabled(!safrGuiToolkit.isReadOnly());
			comboNumericMask.setEnabled(false);
			comboNumericMask.select(0);
		} else if (dataTypeCode.getGeneralId() == Codes.MASKED_NUMERIC) {
			comboNumericMask.setEnabled(!safrGuiToolkit.isReadOnly());
			checkboxSigned.setEnabled(!safrGuiToolkit.isReadOnly());
			textDecimalPlaces.setEnabled(!safrGuiToolkit.isReadOnly());
			textScalingFactor.setEnabled(!safrGuiToolkit.isReadOnly());
			comboDateTimeFormat.setEnabled(!safrGuiToolkit.isReadOnly());
		} else {
			checkboxSigned.setEnabled(!safrGuiToolkit.isReadOnly());
			textDecimalPlaces.setEnabled(!safrGuiToolkit.isReadOnly());
			textScalingFactor.setEnabled(!safrGuiToolkit.isReadOnly());
			comboDateTimeFormat.setEnabled(!safrGuiToolkit.isReadOnly());
			comboNumericMask.setEnabled(false);
			comboNumericMask.select(0);
		}
	}

	private void loadStepDefinition(LookupPathStep step) throws SAFRException,
			DAOException {
		// load step definition only if current step is not same as
		// previously selected step
		step1generallr = lookupPath.getLookupPathSteps().get(0).getSourceLR();
		if(lookupPath.getLookupPathSteps().size()==1){
			comboSourceLRGeneral.setEnabled(true);
		}
		else{
			comboSourceLRGeneral.setEnabled(false);
		}
		refreshControls();
		if (currentStep != step) {
			showSectionSourceFieldProperties(false);
			resetSectionSourceFieldProperties();
			populateLRField(step,null);
			populateSourceLogicalRecord(step);
			if (step.getSequenceNumber() == 1) {
				// for first step

				comboSourceLR.setVisible(true);
			} else {
				// for second steps onwards
				// source LR is a label, so remove accelerator key
				toggleAccKey_e = true;
				comboSourceLR.setVisible(true);
				if (step.getSourceLR() != null) {
					labelSourceLRText.setText(UIUtilities.getComboString(step
							.getSourceLR().getName(), step.getSourceLR()
							.getId()));
					textUserExitRoutine.setText(getExitText(step));
					textUserExitRoutineParam.setText(getExitParams(step));
				} else {
					labelSourceLRText.setText("");
				}
			}

			if (step.getTargetLR() != null) {
				UIUtilities.selectComponentInCombo(comboTargetLR, step
						.getTargetLR());
				populateLogicalFile(comboTargetLF, step.getTargetLR());
				if (step.getTargetLRLFAssociation() != null) {
					selectLogicalFileInCombo(comboTargetLF, step
							.getTargetLRLFAssociation());
				} else {
					// CQ 8882 Kanchan Rauthan 25/1/2010 Target LF automatically
					// set to the one in previous step.
					comboTargetLF.setText("");
				}
			} else {
				// for new step
				comboTargetLR.select(-1);
				// comboTargetLR.deselectAll();
				comboTargetLFViewer.setInput(new Object[0]);
				comboTargetLFViewer.refresh();
				comboTargetLF.select(-1);
			}
			// to display the selected target logical file for an
			// existing step

			if (step.getSequenceNumber() > 1) {
				// source LF is not defined for 1st step.
				if (step.getSourceLRLFAssociation() != null) {
					labelSourceLFText.setText(UIUtilities.getComboString(step
							.getSourceLRLFAssociation()
							.getAssociatedComponentName(), step
							.getSourceLRLFAssociation()
							.getAssociatedComponentIdNum()));
				} else {
					labelSourceLFText.setText("");
				}
			}

			selectedSourceLR = comboSourceLR.getText();
			selectedTargetLR = comboTargetLR.getText();
			selectedTargetLF = comboTargetLF.getText();
			textUserExitRoutine.setText(getExitText(step));
			textUserExitRoutineParam.setText(getExitParams(step));

			if (comboSourceLR.getTable().getSelection().length > 0) {
				prevSrcLR = (EnvironmentalQueryBean) comboSourceLR.getTable()
						.getSelection()[0].getData();
			}
			if (comboTargetLR.getTable().getSelection().length > 0) {
				prevTrgLR = (EnvironmentalQueryBean) comboTargetLR.getTable()
						.getSelection()[0].getData();
			}
			if (comboTargetLF.getTable().getSelection().length > 0) {
				prevTrgLF = (ComponentAssociation) comboTargetLF.getTable()
						.getSelection()[0].getData();
			}

			tableViewerTarget.setInput(step);
			tableViewerSource.setInput(step);

			refreshTargetTable(step);
			refreshSourceTable(step);

			currentStep = step;
		}
	}

	private void refreshTargetTable(LookupPathStep step) throws SAFRException {
		tableViewerTarget.refresh();
		sectionTarget.setText("Target - Total Width: "
				+ Integer.toString(step.getTargetLength()));
		checkValidLookupPath(step);
	}

	private void refreshSourceTable(LookupPathStep step) throws SAFRException {
		tableViewerSource.refresh();
		sectionSource.setText("Source - Total Width: "
				+ Integer.toString(step.getSourceLength()));
		checkValidLookupPath(step);
	}

	private void checkValidLookupPath(LookupPathStep step) throws SAFRException {
		// if any step is invalid, lookup path becomes invalid
		if (!checkValidSteps()) {
			lookupPath.setValid(false);
			//labelStatusText.setText("Inactive");
			//labelStatusText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			checkboxChangeStatus.setEnabled(false);
			try {
				step.checkValid();
				sectionSource.setTitleBarForeground(sectionTarget
						.getTitleBarForeground());
			} catch (SAFRValidationException sve) {
				sectionSource.setTitleBarForeground(Display.getCurrent()
						.getSystemColor(SWT.COLOR_RED));
			}
		} else {

			sectionSource.setTitleBarForeground(sectionTarget
					.getTitleBarForeground());
			checkboxChangeStatus.setEnabled(!safrGuiToolkit.isReadOnly());
		}
	}

	private boolean checkValidSteps() throws SAFRException {
		List<LookupPathStep> lookupPathSteps = lookupPath.getLookupPathSteps()
				.getActiveItems();
		for (LookupPathStep step : lookupPathSteps) {
			try {
				step.checkValid();
			} catch (SAFRValidationException sve) {
				return false;
			}
		}
		return true;
	}

	private void loadSourceFieldDefinition(LookupPathSourceField field)
			throws SAFRException {

		if (field.getSourceFieldType() == LookupPathSourceFieldType.LRFIELD) {
			radiofield.setSelection(true);
			radioConstant.setSelection(false);
			radioSymbol.setSelection(false);
			selectRadioButton(radiofield);
			selectLRFieldInCombo(field);
			selectLRInCombo(field);
			selectedSrcLRField = comboLRField.getText();
			selectedSrcLR = comboSourceLR.getText();
		} else if (field.getSourceFieldType() == LookupPathSourceFieldType.CONSTANT) {
			radiofield.setSelection(false);
			radioConstant.setSelection(true);
			radioSymbol.setSelection(false);
			if(currentStep.getSequenceNumber()!=1){
				comboSourceLR.setText("");
			}
			comboLRField.setText("");
			selectLRInCombo(field);
			selectRadioButton(radioConstant);
			UIUtilities.checkNullText(textConstantValue, field.getSourceValue());
			// Jaydeep August 26,2010, CQ 8281 : Cleared the combo at loading of
			// field definition.
			comboLRFieldViewer.getTableCombo().setText("");

		} else if (field.getSourceFieldType() == LookupPathSourceFieldType.SYMBOL) {
			radiofield.setSelection(false);
			radioConstant.setSelection(false);
			radioSymbol.setSelection(true);
			selectRadioButton(radioSymbol);
			if(currentStep.getSequenceNumber()!=1){
				comboSourceLR.setText("");
			}			
			comboLRField.setText("");
			selectLRInCombo(field);
			UIUtilities.checkNullText(textSymbolName, field.getSymbolicName());
			UIUtilities.checkNullText(textSymbolDefaultValue, field
					.getSourceValue());
			// Jaydeep August 26,2010, CQ 8281 : Cleared the combo at loading of
			// field definition.
			comboLRFieldViewer.getTableCombo().setText("");
		}
		loadDataAttributes(field);
	}

	private void loadDataAttributes(SAFRField field) {

		comboDataType.deselectAll();
		comboDateTimeFormat.deselectAll();
		comboNumericMask.deselectAll();
		UIUtilities.checkNullCombo(comboDataType, field.getDataTypeCode());
		UIUtilities.checkNullText(textLength, Integer.toString(field
				.getLength()));
		UIUtilities.populateDateTimeComboBox(comboDateTimeFormat, field.isNumeric() , true);
		UIUtilities.checkNullCombo(comboDateTimeFormat, field
				.getDateTimeFormatCode());
		UIUtilities.checkNullText(textScalingFactor, Integer.toString(field
				.getScaling()));
		UIUtilities.checkNullText(textDecimalPlaces, Integer.toString(field
				.getDecimals()));
		checkboxSigned.setSelection(field.isSigned());
		UIUtilities
				.checkNullCombo(comboNumericMask, field.getNumericMaskCode());
		dataTypeSettings(UIUtilities.getCodeFromCombo(comboDataType),
				UIUtilities.getCodeFromCombo(comboDateTimeFormat));
		selectedSrcFieldDataType = comboDataType.getText();
		selectedSrcFieldDateTimeFormat = comboDateTimeFormat.getText();
		selectedSrcFieldNumericMask = comboNumericMask.getText();
		
	}

	private void populateLRField(LookupPathStep step, String selectedStep) {
		comboLRField.getTable().removeAll();
		int numberOfSteps = step.getSequenceNumber().intValue();
		List<LRField> lrFieldList;
		ArrayList<ArrayList<SAFRPersistentObject>> lrFieldComboInputList= new ArrayList<ArrayList<SAFRPersistentObject>>();
		try {
			List<Integer> assocIDs = new ArrayList<Integer>();
			for (int i = 0; i < numberOfSteps; i++) {
				step = (LookupPathStep) lookupPath.getLookupPathSteps()
						.getActiveItems().get(i);
				if (step.getSourceLR() != null) {
					
					if (step.getSourceLRLFAssociation() != null) {
						if (assocIDs.contains(step.getSourceLRLFAssociation()
								.getAssociationId())) {
							// fields already added to list in a prev step
							continue;
						} else {
							assocIDs.add(step.getSourceLRLFAssociation()
									.getAssociationId());
						}
					}

					
					lrFieldList = step.getSourceLR().getLRFields();
				
					String logicalRecordNameMatcher = selectedStep== null ?step.getSourceLR().getName():selectedStep.replaceAll("  *\\[.*", "");
					for (LRField field : lrFieldList) {
						if(field.getLogicalRecord().getName().equals(logicalRecordNameMatcher)){

							ArrayList<SAFRPersistentObject> list = new ArrayList<SAFRPersistentObject>();
							if(!list.contains(step.getSourceLR()));
								list.add(step.getSourceLR());
							if(!list.contains(step.getSourceLRLFAssociation()));
								list.add(null);
							if(!list.contains(field));
							{
								list.add(field);
							}
							if(!lrFieldComboInputList.contains(list)) {
								lrFieldComboInputList.add(list);
							}
						}
						
					}
				}
//QA219_Premium_Account_Element
//QA219_Premium_Account_Element				
			}
			
			comboLRFieldViewer.setInput(lrFieldComboInputList);
			comboLRFieldViewer.refresh();
		} catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e,"Unexpected error occurred while retrieving the source Logical Record.",null);
		}
		

	}

	/**
	 * Method to get the string to be displayed in the LR Field combo or the
	 * Source Field table
	 * 
	 * @param logicalRecord
	 * @param LRLFAssociation
	 * @param field
	 * @param displayLength
	 * @return
	 */
	private String getFieldString(LogicalRecord logicalRecord,
			ComponentAssociation LRLFAssociation, SAFRField field) {
		String result = "";

		if (LRLFAssociation != null) {
		}
		if (logicalRecord != null) {
		}
		if (field instanceof LookupPathSourceField) {
			try {
				if (((LookupPathSourceField) field).getSourceLRField() != null) {
					result += ((LookupPathSourceField) field)
							.getSourceLRField().getName();
				}
			} catch (SAFRException e) {
				UIUtilities.handleWEExceptions(e,"Unexpected error occurred while retrieving the source Logical Record Field.",null);
			}
		} else if (field instanceof LRField) {
			if (field.getName() != null && field.getLength() != null) {
				result += field.getName() + " ["
						+ Integer.toString(field.getLength()) + "]";
			}
		}
		return result;
	}
	
	private String getLRString(LogicalRecord logicalRecord,
	ComponentAssociation LRLFAssociation, SAFRField field) {
		if(logicalRecord!=null){
			return logicalRecord.getName();
		}
		else{
			return "";
		}
	}

	private Integer getLRID(LogicalRecord logicalRecord,
			ComponentAssociation LRLFAssociation, SAFRField field) {
				if(logicalRecord!=null){
					return logicalRecord.getId();
				}
				else{
					return null;
				}
			}
	
	private LogicalRecord getLR(LogicalRecord logicalRecord,
			ComponentAssociation LRLFAssociation, SAFRField field) {
				if(logicalRecord!=null){
					return logicalRecord;
				}
				else{
					return null;
				}
			}
	
	private String getSourceFieldName(LookupPathSourceField sourceField)
			throws SAFRException {
		
		String result = "";
		String sourceValue = "";
		String symbolicName = "";
		if (sourceField.getSourceFieldType() == LookupPathSourceFieldType.LRFIELD && sourceField!=null) {
			
			if(sourceField.getSourceFieldSourceLR()!=null){
				result = sourceField.getSourceFieldSourceLR().getName()+"."+
						getFieldString(sourceField.getSourceFieldSourceLR(),
						sourceField.getSourceFieldLRLFAssociation(), sourceField);
			}
		} else if (sourceField.getSourceFieldType() == LookupPathSourceFieldType.CONSTANT) {
			if (sourceField.getSourceValue() != null) {
				sourceValue = sourceField.getSourceValue();
			}
			result = "CONSTANT." + sourceValue;
		} else if (sourceField.getSourceFieldType() == LookupPathSourceFieldType.SYMBOL) {
			if (sourceField.getSymbolicName() != null) {
				symbolicName = sourceField.getSymbolicName();
			}
			if (sourceField.getSourceValue() != null) {
				sourceValue = sourceField.getSourceValue();
			}
			result = symbolicName + "." + sourceValue;
		}
		return result;
	}

	private void selectLRFieldInCombo(LookupPathSourceField sourceField)
			throws SAFRException {
		if (sourceField.getSourceLRField() == null) {
			comboLRField.getTable().deselectAll();
		}
		try {
			String searchString = getFieldString(sourceField
					.getSourceFieldSourceLR(), sourceField
					.getSourceFieldLRLFAssociation(), sourceField
					.getSourceLRField());
			comboLRFieldViewer.getTableCombo().setText(searchString);

		} catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e,
			    "Unexpected error occurred while retrieving the source Logical Record Field.",null);
		}
	}

	private void selectLRInCombo(LookupPathSourceField sourceField) throws SAFRException {
		if(sourceField.getSourceFieldSourceLR()==null){
			comboSourceLR.getTable().deselectAll();
		}
		try {
			String searchString = getLRString(sourceField
					.getSourceFieldSourceLR(), sourceField
					.getSourceFieldLRLFAssociation(), sourceField
					.getSourceLRField());
			Integer searchID = getLRID(sourceField
					.getSourceFieldSourceLR(), sourceField
					.getSourceFieldLRLFAssociation(), sourceField
					.getSourceLRField());
			
			LogicalRecord searchLR = getLR(sourceField
					.getSourceFieldSourceLR(), sourceField
					.getSourceFieldLRLFAssociation(), sourceField
					.getSourceLRField());
			
			String str="";
			if(searchID!=null) {
				str = searchString+ "[" + searchID +"]";
			}
			if(currentStep.getSequenceNumber()==1){
				comboSourceLRViewer.getTableCombo().setText(comboSourceLRGeneral.getText());
				comboSourceLRViewer.getTableCombo().setEnabled(false);
			}
			else{
				comboSourceLRViewer.getTableCombo().setText(str);

			}

			if(currentStep.getSequenceNumber()==1 && count==1) {
				entrystr=searchString;
				step1lrname = searchString;
				step1lrid = searchID;
				step1lr = searchLR;
				count++;
			}
		} catch(SAFRException e){
			UIUtilities.handleWEExceptions(e,
				    "Unexpected error occurred while retrieving the source Logical Record.",null);
		}
	}
	
	private static void selectLogicalFileInCombo(TableCombo combo,
			ComponentAssociation logicalFileAssociation) {
		if (logicalFileAssociation == null) {
			return;
		}
		int index = combo.indexOf(UIUtilities.getComboString(
				logicalFileAssociation.getAssociatedComponentName(),
				logicalFileAssociation.getAssociatedComponentIdNum()));
		if (index >= 0) {
			combo.select(index);
		}
	}

	private LogicalRecord getLogicalRecordFromCombo(TableCombo combo) {
		// String key = String.valueOf(combo.getSelectionIndex());
		if (combo.getTable().getSelection().length < 1) {
			return null;
		}
		LogicalRecordQueryBean logicalRecordBean = (LogicalRecordQueryBean) combo
				.getTable().getSelection()[0].getData();

		LogicalRecord logicalRecord = null;
		try {
			if (logicalRecordBean != null) {
				logicalRecord = SAFRApplication.getSAFRFactory()
						.getLogicalRecord(logicalRecordBean.getId());
			}
		} catch (SAFRNotFoundException snfe) {
			UIUtilities.handleWEExceptions(snfe, null,
					UIUtilities.titleStringNotFoundException);
		} catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e,
			    "Unexpected error occurred while retrieving the Logical Record.",null);
		}

		return logicalRecord;
	}

	@Override
	public void refreshModel() {
	    if (!UIUtilities.isEqualString(lookupPath.getName(), textName.getText())) {
	        lookupPath.setName(textName.getText());
	    }
        if (!UIUtilities.isEqualString(lookupPath.getComment(), textComments.getText())) {
            lookupPath.setComment(textComments.getText());
        }
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
        boolean newLP = lookupPath.getId() <= 0;
		lookupPath.store();
		if (newLP) {
		    UIUtilities.enableDisableBatchLookupMenu();
		}
		ApplicationMediator.getAppMediator().refreshDeactiveViews(lookupPath.getDeactivatedViews());
	}

	@Override
	public void validate() throws DAOException, SAFRException {
		boolean done = false;
		boolean flag = false;
		String messageTitle = "";
		SAFRValidationToken token = null;
		while (!done) {
			try {
				lookupPath.validate(token);
				// Modified by Shruti Shukla(1/6/2010), CQ 8057, Used variable
				// warningsShown for form decoration bug.
				if (token == null || 
				    (token != null && 
				     !token.getValidationFailureType().equals(SAFRValidationType.WARNING) && 
				     warningsShown == false)) {
					getMsgManager().removeAllMessages();
				}
				done = true;
				warningsShown = false;
			} catch (SAFRValidationException sve) {
				SAFRValidationType failureType = sve.getSafrValidationToken().getValidationFailureType();
				if (failureType == SAFRValidationType.ERROR) {
				} else if (failureType == SAFRValidationType.WARNING) {
					warningsShown = true;
				}
				if (failureType == SAFRValidationType.ERROR) {
					// process lookup and step errors. Throw back to the calling
					// function.
					// Errors are handled in doSave.
					throw sve;
				} else if (failureType == SAFRValidationType.WARNING) {
					// process lookup step warnings. Ask for user
					// confirmation to continue.
					// If the user confirms, Validate again to continue from
					// where it was left.

					int confirm = 1;
					decorateEditor(sve);
					MessageDialog dialog = new MessageDialog(getSite()
							.getShell(), "Validations", null,
							"This Lookup Path will be saved as 'Inactive' due to below errors." + SAFRUtilities.LINEBREAK
									+ sve.getMessageString(),
							MessageDialog.WARNING, new String[] { "&OK",
									"&Cancel" }, 0);
					confirm = dialog.open();
					if (confirm == 1) {
						// Stop save.
						throw new SAFRValidationException();
					} else {
						// get token from Exception to pass to validate again.
						token = sve.getSafrValidationToken();
					}
				} else if (failureType == SAFRValidationType.DEPENDENCY_LOOKUP_WARNING) {
					// process Lookup path dependencies. Ask user for
					// confirmation to
					// continue.
					// If the user confirms, continue to SAVE else stop the
					// SAVE process.
					int confirm1 = 0;
					if (flag == false) {
						messageTitle = "The following View(s) are dependent on this Lookup Path. Saving this Lookup Path will make these View(s) inactive.";
					} else {
						messageTitle = "A new View dependency has been created on this Lookup Path since the last warning was issued. Saving this Lookup Path will make these View(s) inactive.";
					}
					confirm1 = DependencyMessageDialog.openDependencyDialog(
							getSite().getShell(), "Lookup Path dependencies",
							messageTitle, sve.getMessageString(),
							MessageDialog.WARNING, new String[] { "&OK",
									"&Cancel" }, 0);
					flag = true;
					// get token from Exception to pass to validate again.
					token = sve.getSafrValidationToken();
					if (confirm1 == 1) {
						throw new SAFRValidationException();
					}
				}
			}
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		boolean retval = false;
		
		//if not dealing with a new component 
		//check with parent based on permissions
		if(lookupPath.getId() > 0) {
			retval = isSaveAsAllowed(Permissions.CreateLookupPath);
		}
		return retval;
	}

	private class StepsListContentProvider implements
			IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((LookupPath) inputElement).getLookupPathSteps()
					.getActiveItems().toArray();
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}
	}

	private class StepsListLabelProvider extends ColumnLabelProvider {

		public StepsListLabelProvider() {

		}

		@Override
		public Color getForeground(Object element) {
			LookupPathStep lookupPathStep = (LookupPathStep) element;

			try {
				lookupPathStep.checkComplete();
			} catch (SAFRException e) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
			}

			try {
				lookupPathStep.checkValid();
			} catch (SAFRException e) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			}

			return null;
		}

		@Override
		public String getText(Object element) {
			LookupPathStep lookupPathStep = (LookupPathStep) element;
			return Integer.toString(lookupPathStep.getSequenceNumber());
		}

	}

	// Target fields should be sorted by Primary key seq number
	// assumes all LRfields are keys and therefore always have a PKeySeqNo
	private class TargetFieldSorter implements Comparator<LRField> {

        public int compare(LRField object1, LRField object2) {
            return  (object1.getPkeySeqNo().compareTo(object2.getPkeySeqNo()));
        }

	}
	
	private class TargetTableContentProvider implements
			IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			LookupPathStep step = (LookupPathStep) inputElement;
			SAFRList<LRField> primaryKeyFields = new SAFRList<LRField>();
			try {
				if (step.getTargetLR() != null) {
					List<LRField> fields = step.getTargetLR().getLRFields()
							.getActiveItems();
					for (LRField field : fields) {
						if (field.getKeyType()
								.equals(LRFieldKeyType.PRIMARYKEY)) {
							primaryKeyFields.add((LRField) field);
						}
					}
					Collections.sort(primaryKeyFields, new TargetFieldSorter());
				}

			} catch (SAFRException e) {
				UIUtilities.handleWEExceptions(e,"Unexpected error occurred while retrieving target Logical Record.",null);
			}
			return primaryKeyFields.toArray();
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

	}

	private class TargetTableLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			LRField field = (LRField) element;
			String result = "";
			switch (columnIndex) {
			case 0:
				result = field.getName();
				break;
			case 1:
				result = Integer.toString(field.getLength());
				break;
			}
			return result;
		}

		public void addListener(ILabelProviderListener listener) {

		}

		public void dispose() {

		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {

		}

	}

	private class SourceFieldSorter implements Comparator<LRField> {

        public int compare(LRField object1, LRField object2) {
            return  (object1.getPkeySeqNo().compareTo(object2.getPkeySeqNo()));
        }

	}
	private class SourceTableContentProvider implements
			IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			LookupPathStep step = (LookupPathStep) inputElement;
			SAFRList<LRField> primaryKeyFields = new SAFRList<LRField>();
			try {
				if (step.getSourceLR()!=null && step.getSourceFields() != null) {
					List<LRField> fields = step.getSourceLR().getLRFields()
							.getActiveItems();
					for (LRField field : fields) {
						if (field.getKeyType()
								.equals(LRFieldKeyType.PRIMARYKEY)) {
							primaryKeyFields.add((LRField) field);
						}
					}
					Collections.sort(primaryKeyFields, new SourceFieldSorter());
				}

			} catch (SAFRException e) {
				UIUtilities.handleWEExceptions(e,"Unexpected error occurred while retrieving target Logical Record.",null);
			}
			return step.getSourceFields().getActiveItems().toArray();

		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

	}

	private class SourceTableLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			LookupPathSourceField sourceField = (LookupPathSourceField) element;
			String result = "";

			switch (columnIndex) {
			case 0:
				try {
					if(sourceField!=null){
						result = getSourceFieldName(sourceField).toString();
					}
					if(result=="") {
						result=step1lrname;
					}
					
				} catch (SAFRException se) {
					UIUtilities.handleWEExceptions(se);
				}

				break;
			case 1:
				result = Integer.toString(sourceField.getLength());
				break;
			}
			return result;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

	}

	private class LRComboLabelProvider implements ITableLabelProvider,
			ITableColorProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@SuppressWarnings("unchecked")
        public String getColumnText(Object element, int columnIndex) {
			String returnValue = UIUtilities.BLANK_VALUE;
			List<SAFRPersistentObject> list = (List<SAFRPersistentObject>) element;

			switch(columnIndex){
			
			case 0:
				if(list.get(2) instanceof LRField){
					return Integer.toString(((SAFRField)list.get(2)).getId());
				}
				break;
			case 1:
				return getFieldString((LogicalRecord) list.get(0),
						(ComponentAssociation) list.get(1), (SAFRField) list.get(2));		
			}
			if (returnValue == null) {
				return UIUtilities.BLANK_VALUE;
			} else {
				return getFieldString((LogicalRecord) list.get(0),
					(ComponentAssociation) list.get(1), (SAFRField) list.get(2));	
			}

		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Color getBackground(Object element, int columnIndex) {
			return null;
		}

		public Color getForeground(Object element, int columnIndex) {
			return null;
		}

	}

	// Function to add a source field to the table
	public void commandAddSourceField() {
		try {
				showSectionSourceFieldProperties(true);
				resetSectionSourceFieldProperties();

				LookupPathSourceField sourceField = currentStep
						.addSourceField(LookupPathSourceFieldType.LRFIELD);
				refreshSourceTable(currentStep);
				loadSourceFieldDefinition(sourceField);
				tableViewerSource.setSelection(new StructuredSelection(
						sourceField));
			
		} catch (DAOException de) {
			UIUtilities.handleWEExceptions(de,"Unexpected database error occurred while adding source field.",UIUtilities.titleStringDbException);
		} catch (SAFRException se) {
			UIUtilities.handleWEExceptions(se,"Unexpected error occurred while adding source field.",null);
		}
		refreshToolbar();
	}

	// Function to remove a source field to the table
	public void commandRemoveSourceField() {
		removeSelectedSourceField();
		if (tableSource.getItemCount() <= 0) {
			showSectionSourceFieldProperties(true);
		}
		refreshToolbar();
	}

	// Function to move source field up in the table.
	public void commandMoveFieldUp() {
		if (tableSource.getSelectionIndex() >= 0) {
			currentStep.moveFieldUp(tableSource.getSelectionIndex());
			tableViewerSource.refresh();
			setDirty(true);
		}
		refreshToolbar();
	}

	// Function to move source field down in the table.
	public void commandMoveFieldDown() {
		if (tableSource.getSelectionIndex() >= 0) {
			currentStep.moveFieldDown(tableSource.getSelectionIndex());
			tableViewerSource.refresh();
			setDirty(true);
		}
		refreshToolbar();
	}

	/**
	 * This method is used to get the widget based on the property passed.
	 * 
	 * @param property
	 * @return the widget.
	 */
	protected Control getControlFromProperty(Object property) {
		if (property == LookupPath.Property.NAME) {
			return textName;
		} else if (property == LookupPath.Property.STEP) {
			return tableViewerStepsList.getControl();
		}
		return null;
	}

	@Override
	public ComponentType getEditorCompType() {
		return ComponentType.LookupPath;
	}

	@Override
	public SAFRPersistentObject getModel() {

		return lookupPath;
	}

	private void refreshToolbar() {
		SourceProvider service = UIUtilities.getSourceProvider();
		int selection = tableSource.getSelectionIndex();
		if (selection != -1) {
			if (selection == 0) {
				service.setAllowMoveLookUpSourceUp(false);
			} else {
				service.setAllowMoveLookUpSourceUp(true);
			}

			if (selection == (tableSource.getItemCount() - 1)) {
				service.setAllowMoveLookUpSourceDown(false);
			} else {
				service.setAllowMoveLookUpSourceDown(true);
			}
		} else {
			service.setAllowMoveLookUpSourceUp(false);
			service.setAllowMoveLookUpSourceDown(false);
		}
	}

	private void refreshEditRights() {
		SourceProvider sourceProvider = UIUtilities.getSourceProvider();
		if (lookupPathInput.getEditRights() == EditRights.Read) {
			sourceProvider.setAllowEdit(false);
		} else {
			sourceProvider.setAllowEdit(true);
		}
	}

	/*
	 * Jaydeep April 13, 2010 CQ 7615 : Added Source provider Implementation.
	 */
	public void partActivated(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false).equals(this)) {
			refreshEditRights();
			refreshToolbar();
			// Neha, Added for menu enable disable for report menu
			SourceProvider sourceProvider = UIUtilities.getSourceProvider();
			sourceProvider.setEditorFocusLR(false);
			sourceProvider.setEditorFocusLookupPath(true);
			sourceProvider.setEditorFocusView(false);
			sourceProvider.setEditorFocusEnv(false);
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
				sourceProvider.setEditorFocusLookupPath(false);
				if (openEditors.length == 0) {
					sourceProvider.setEditorFocusLR(false);
					sourceProvider.setEditorFocusView(false);
					sourceProvider.setEditorFocusEnv(false);
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
	public String getComponentNameForSaveAs() {
		return textName.getText();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		super.dispose();
		getSite().getPage().removePartListener(this);
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		if (sve.getErrorMessageMap().containsKey(
				com.ibm.safr.we.model.LookupPath.Property.NAME)) {
			return true;
		}
		return false;
	}

    public void makeLookupInactive() {
        lookupPath.setValid(false);        
    }

}

