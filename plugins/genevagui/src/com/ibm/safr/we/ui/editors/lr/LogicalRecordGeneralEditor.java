package com.ibm.safr.we.ui.editors.lr;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.ui.utilities.CommentsTraverseListener;
import com.ibm.safr.we.ui.utilities.DepCheckOpener;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class LogicalRecordGeneralEditor {
    
    static final Logger logger = Logger
    .getLogger("com.ibm.safr.we.ui.editors.lr.LogicalRecordGeneralEditor");
    
    public static final int MAXUSEREXITPARAM = 32;
    
    private LogicalRecordMediator mediator;
    private CTabFolder tabFolder;
    private LogicalRecord logicalRecord = null;
    
    protected LogicalRecordGeneralEditor(LogicalRecordMediator mediator, CTabFolder tabFolder, LogicalRecord logicalRecord) {
        this.mediator = mediator;
        this.tabFolder = tabFolder;
        this.logicalRecord = logicalRecord;
    }

    /* Internal Widgets */
    private Composite compositeLRGeneral;
    private Composite compositeLRProperties;
    private Text textID;
    private Text textName;
    private Combo comboType;
    private Combo comboStatus;
    
    private TableComboViewer comboGenUserExitViewer;
    private TableCombo comboUserExitRoutine;
    private MenuItem ueOpenEditorItem = null;
    private MenuItem ueDepCheckItem = null;
    
    private Text textUserExitRoutineParam;
    private Text textComments;
    private Label labelCreatedValue;
    private Label labelModifiedValue;
    private Label labelActivatedValue;
    
    /* State */
    private String selectedType = "";
    private String selectedStatus = "";
    
    private String selectedUserExit = "";
    private List<UserExitRoutineQueryBean> userExitRoutineList;
    private EnvironmentalQueryBean prevUXR;
    
    private String defaultModStr = "-";
    
    /**
     * This function creates the composite and its child elements for displaying
     * the LR Properties section
     * 
     * @throws SAFRException
     */
    protected void create() throws SAFRException {
        compositeLRGeneral = mediator.getGUIToolKit().createComposite(tabFolder, SWT.NONE);
        compositeLRGeneral.setLayout(createCompositeLayout(1, true));
        compositeLRGeneral.setLayoutData(new TableWrapData(
                TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));

        compositeLRProperties = mediator.getGUIToolKit().createComposite(compositeLRGeneral, SWT.NONE);
        compositeLRProperties.setLayout(createCompositeLayout(3, false));
        TableWrapData dataLRProperties = new TableWrapData(
            TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB);
        compositeLRProperties.setLayoutData(dataLRProperties);

        mediator.getFormToolKit().createLabel(compositeLRProperties, "ID:");

        textID = mediator.getGUIToolKit().createTextBox(compositeLRProperties, SWT.NONE);
        textID.setEnabled(false);
        textID.setLayoutData(UIUtilities.textTableData(1));

        Label filler1 = mediator.getGUIToolKit().createLabel(compositeLRProperties,
                SWT.NONE, "");
        filler1.setLayoutData(UIUtilities.textTableData(1));

        mediator.getGUIToolKit().createLabel(compositeLRProperties,SWT.NONE, "&Name:");
        textName = mediator.getGUIToolKit().createNameTextBox(compositeLRProperties, SWT.NONE);
        textName.setData(SAFRLogger.USER, "Name");                
        textName.setLayoutData(UIUtilities.textTableData(2));
        textName.setTextLimit(UIUtilities.MAXNAMECHAR);
        textName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                mediator.setDirty(true);
            }
        });

        mediator.getGUIToolKit().createLabel(compositeLRProperties,SWT.NONE, "&Type:");
        comboType = mediator.getGUIToolKit().createComboBox(compositeLRProperties, SWT.READ_ONLY, "");
        comboType.setData(SAFRLogger.USER, "Type");                       
        comboType.setLayoutData(UIUtilities.textTableData(1));
        UIUtilities.populateComboBox(comboType, CodeCategories.LRTYPE, 0, false);
        selectedType = comboType.getText();
        comboType.setEnabled(false);
        comboType.addFocusListener(new FocusAdapter() {

            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!(comboType.getText().equals(selectedType))) {
                    mediator.setDirty(true);
                    selectedType = comboType.getText();
                }
            }
        });

        Label filler3 = mediator.getGUIToolKit().createLabel(compositeLRProperties,SWT.NONE, "");
        filler3.setLayoutData(UIUtilities.textTableData(1));

        mediator.getGUIToolKit().createLabel(compositeLRProperties, SWT.NONE, "&Status:");
        comboStatus = mediator.getGUIToolKit().createComboBox(compositeLRProperties,SWT.READ_ONLY, "");
        comboStatus.setData(SAFRLogger.USER, "Status");                               
        comboStatus.setLayoutData(UIUtilities.textTableData(1));
        UIUtilities.populateComboBox(comboStatus, CodeCategories.LRSTATUS, 1,false);
        selectedStatus = comboStatus.getText();
        comboStatus.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!(comboStatus.getText().equals(selectedStatus))) {
                    mediator.setDirty(true);
                    selectedStatus = comboStatus.getText();
                }
            }
        });
        comboStatus.setText(SAFRApplication.getSAFRFactory().getCodeSet(
            CodeCategories.LRSTATUS).getCode(Codes.ACTIVE).getDescription());

        Label filler4 = mediator.getGUIToolKit().createLabel(compositeLRProperties,SWT.NONE, "");
        filler4.setLayoutData(UIUtilities.textTableData(1));

        createUserExitRoutineItems();

        mediator.getGUIToolKit().createLabel(compositeLRProperties,
                SWT.NONE, "C&omments:");

        textComments = mediator.getGUIToolKit()
                .createCommentsTextBox(compositeLRProperties);
        textComments.setData(SAFRLogger.USER, "Comments");                                                
        textComments.setLayoutData(UIUtilities.multiLineTextData(1, 2,
                textComments.getLineHeight() * 3));
        textComments.setTextLimit(UIUtilities.MAXCOMMENTCHAR);
        CommentsTraverseListener traverseListener = new CommentsTraverseListener();
        textComments.addTraverseListener(traverseListener);
        textComments.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                mediator.setDirty(true);
            }
        });

        mediator.getGUIToolKit().createLabel(compositeLRProperties,SWT.NONE, "Created:");
        labelCreatedValue = mediator.getGUIToolKit().createLabel(compositeLRProperties,SWT.NONE, defaultModStr);
        labelCreatedValue.setLayoutData(UIUtilities.textTableData(2));

        mediator.getGUIToolKit().createLabel(compositeLRProperties,SWT.NONE, "Last Modified:");
        labelModifiedValue = mediator.getGUIToolKit().createLabel(compositeLRProperties,SWT.NONE, defaultModStr);
        labelModifiedValue.setLayoutData(UIUtilities.textTableData(2));

        mediator.getGUIToolKit().createLabel(compositeLRProperties,SWT.NONE, "Last De/active:");
        labelActivatedValue = mediator.getGUIToolKit().createLabel(compositeLRProperties,SWT.NONE, defaultModStr);
        labelActivatedValue.setLayoutData(UIUtilities.textTableData(2));
    }

	private void createUserExitRoutineItems() {
		Label UXLabel = mediator.getGUIToolKit().createLabel(compositeLRProperties, SWT.NONE, "Lookup User-E&xit Routine:");
        comboGenUserExitViewer = mediator.getGUIToolKit().createTableComboForExits(
                compositeLRProperties, ComponentType.UserExitRoutine);      
        comboUserExitRoutine = comboGenUserExitViewer.getTableCombo();
        comboUserExitRoutine.setData(SAFRLogger.USER, "Lookup User-Exit");                                        
        comboUserExitRoutine.setLayoutData(UIUtilities.textTableData(1));

        addUEOpenEditorMenu();
        
        // load the data
        try {
            userExitRoutineList = SAFRQuery.queryUserExitRoutines(UIUtilities
                    .getCurrentEnvironmentID(), "LKUP", SortType.SORT_BY_NAME);
        } catch (DAOException e1) {
            UIUtilities.handleWEExceptions(e1,
                    "Error occurred while retrieving all User Exit Routines.",
                    UIUtilities.titleStringDbException);
        }
        Integer counter = 0;
        if (userExitRoutineList != null) {
            for (UserExitRoutineQueryBean userExitRoutineQueryBean : userExitRoutineList) {
                comboUserExitRoutine.setData(Integer.toString(counter),
                        userExitRoutineQueryBean);
                counter++;
            }
            UserExitRoutineQueryBean blankElement = new UserExitRoutineQueryBean(
                    null, 0, "", null, null, null, EditRights.ReadModifyDelete, null, null, null, null);
            userExitRoutineList.add(0, blankElement);
        }
        comboGenUserExitViewer.setInput(userExitRoutineList);

        // CQ 8459. Nikita. 26/08/2010.
        // To resolve issues with enabling/disabling and focus of UXR parameter
        // text-box
        comboUserExitRoutine.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                enableUserExitRoutineParams();
            }
        });
        comboUserExitRoutine.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {

                if (!comboUserExitRoutine.getText().equals(selectedUserExit)) {
					UserExitRoutineQueryBean bean = null;

                    if (comboUserExitRoutine.getTable().getSelectionCount() > 0) {
						bean = (UserExitRoutineQueryBean) comboUserExitRoutine
                                .getTable().getSelection()[0].getData();
                        prevUXR = bean;
                        mediator.setDirty(true);
                        disableUserExitRoutineParams();
                    } else {
                        if (prevUXR != null) {
							comboUserExitRoutine.setText(logicalRecord
									.getLookupExitRoutine().getComboString());
                        } else {
                            if (selectedUserExit.equals("")) {
                                comboUserExitRoutine.setText("");
                            } else {
                                comboUserExitRoutine.setText(selectedUserExit);
                            }
                        }
                    }
                    selectedUserExit = comboUserExitRoutine.getText();
                }
            }

        });
        
        Label filler5 = mediator.getGUIToolKit().createLabel(compositeLRProperties,
                SWT.NONE, "");
        filler5.setLayoutData(UIUtilities.textTableData(1));

        Label userParamLabel = mediator.getGUIToolKit().createLabel(compositeLRProperties, SWT.NONE,
            "    Para&meters:");
        //Manual indentation above works since the stuff below causes a runtime error?
        /*FormData userParamLabelData = new FormData();
        userParamLabelData.top = new FormAttachment(UXLabel, 15);
        userParamLabelData.left = new FormAttachment(0, 50);   
        userParamLabel.setLayoutData(userParamLabelData);*/
        
        textUserExitRoutineParam = mediator.getGUIToolKit().createTextBox(compositeLRProperties, SWT.NONE);
        textUserExitRoutineParam.setData(SAFRLogger.USER, "User-Exit Param");                                     
        textUserExitRoutineParam.setLayoutData(UIUtilities.textTableData(2));
        textUserExitRoutineParam.setTextLimit(MAXUSEREXITPARAM);
        textUserExitRoutineParam.setEnabled(false);
        textUserExitRoutineParam.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                mediator.setDirty(true);
            }
        });
	}

    /**
     * Method to create a {@link TableWrapLayout} object. The
     * {@link TableWrapLayout} object will have the following properties:
     * 1.Vertical spacing = as specified by the constant VERTICALSPACING.
     * 2.Horizontal spacing = as specified by the constant HORIZONTALSPACING.
     * 3.number of columns = as specified by the parameter passed. 4.Columns
     * will be of either equal or unequal width depending on the boolean
     * parameter passed. 5.Top margin = as specified by the constant TOPMARGIN.
     * 6.Bottom margin = as specified by the constant BOTTOMMARGIN.
     * 
     * @param numCols
     *            the number of columns for the returned {@link TableWrapLayout}
     *            object.
     * @param equalWidth
     *            boolean parameter which determines whether or not the columns
     *            of the {@link TableWrapLayout} object must be of equal width.
     * @return {@link TableWrapLayout} object.
     */
    private static TableWrapLayout createCompositeLayout(int numCols,
            Boolean equalWidth) {
        TableWrapLayout layout = new TableWrapLayout();
        layout.verticalSpacing = UIUtilities.VERTICALSPACING;
        layout.horizontalSpacing = UIUtilities.HORIZONTALSPACING;
        layout.numColumns = numCols;
        layout.makeColumnsEqualWidth = equalWidth;
        layout.topMargin = UIUtilities.TOPMARGIN;
        layout.bottomMargin = UIUtilities.BOTTOMMARGIN;
        return layout;
    }

    private void addUEOpenEditorMenu()
    {
        Text text = comboUserExitRoutine.getTextControl();
        Menu menu = text.getMenu();
        ueOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        ueOpenEditorItem.setText("Open Editor");
        ueOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean)((StructuredSelection) comboGenUserExitViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.UserExitRoutine);                        
                }                
            }
        });
        
        ueDepCheckItem = new MenuItem(menu, SWT.PUSH);
        ueDepCheckItem.setText("Dependency Checker");
        ueDepCheckItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean)((StructuredSelection) comboGenUserExitViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    DepCheckOpener.open(bean);                        
                }                
            }
        });
        
        comboUserExitRoutine.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean)((StructuredSelection) comboGenUserExitViewer
                            .getSelection()).getFirstElement();
                    if (bean != null && bean.getId() != 0) {   
                        ueOpenEditorItem.setEnabled(true);
                        ueDepCheckItem.setEnabled(true);
                    }
                    else {
                        ueOpenEditorItem.setEnabled(false);
                        ueDepCheckItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }       
    
    /**
     * This method enables the text box for entering the user-exit routine
     * parameters if a user-exit routine has been selected from the combo-box
     */
    public void enableUserExitRoutineParams() {
        // CQ 8459. Nikita. 26/08/2010.
        // To resolve issues with enabling/disabling and focus of UXR parameter
        // text-box

        if (comboUserExitRoutine.getText().length() > 0) {
            textUserExitRoutineParam.setEnabled(!mediator.getGUIToolKit().isReadOnly());
        }
    }
    
    /**
     * This method disables the text box for entering the user-exit routine
     * parameters if no user-exit routine has been selected from the combo-box
     */
    public void disableUserExitRoutineParams() {
        // CQ 8459. Nikita. 26/08/2010.
        // CQ 9097. Nikita. 16/02/2011.
        // To resolve issues with enabling/disabling and focus of UXR parameter
        // text-box

        EnvironmentalQueryBean uxrBean = (EnvironmentalQueryBean) comboUserExitRoutine
                .getTable().getSelection()[0].getData();
        if (uxrBean != null && uxrBean.getId() <= 0) {
            textUserExitRoutineParam.setText("");
            textUserExitRoutineParam.setEnabled(false);
        }
    }

    public Control getCompositeGeneral() {
        return compositeLRGeneral;
    }

    public void setNameFocus() {
        textName.setFocus();
    }

	public void refreshControlGeneral() {
		UIUtilities.checkNullText(textID,
				Integer.toString(logicalRecord.getId()));
		if (logicalRecord.getId() > 0) {
			UIUtilities.checkNullText(textName, logicalRecord.getName());
			UIUtilities
					.checkNullCombo(comboType, logicalRecord.getLRTypeCode());
			selectedType = comboType.getText();
			UIUtilities.checkNullCombo(comboStatus,
					logicalRecord.getLRStatusCode());
			selectedStatus = comboStatus.getText();

			refreshUserExitAndParm();
			UIUtilities.checkNullText(textComments, logicalRecord.getComment());
			labelCreatedValue.setText(logicalRecord.getCreateBy() + " on "
					+ logicalRecord.getCreateTimeString());
			labelModifiedValue.setText(logicalRecord.getModifyBy() + " on "
					+ logicalRecord.getModifyTimeString());
			if (logicalRecord.getActivatedBy() == null
					|| logicalRecord.getActivatedTimeString() == null) {
				labelActivatedValue.setText(defaultModStr);
			} else {
				labelActivatedValue.setText(logicalRecord.getActivatedBy()
						+ " on " + logicalRecord.getActivatedTimeString());
			}

		}
	}

	private void refreshUserExitAndParm() {
		if (logicalRecord.hasLookupExit()) {
			try {
				// Set the comb text to match that of the lookup exit
				UIUtilities.selectComponentInCombo(comboUserExitRoutine,
						logicalRecord.getLookupExitRoutine());
				// Not sure what this bit is...
				if (comboUserExitRoutine.getTable().getSelection().length > 0) {
					prevUXR = (UserExitRoutineQueryBean) comboUserExitRoutine
							.getTable().getSelection()[0].getData();
				} else {
					if (logicalRecord.getLookupExitRoutine() != null
							&& !(comboUserExitRoutine.getText().equals(""))) {
						UserExitRoutineQueryBean uxer = new UserExitRoutineQueryBean(
								logicalRecord.getEnvironment().getId(),
								logicalRecord.getLookupExitRoutine().getId(),
								logicalRecord.getLookupExitRoutine().getName(),
								null, null, null, null, null, null, null, null);
						selectedUserExit = logicalRecord.getLookupExitRoutine()
								.getComboString();
						prevUXR = uxer;
					} else {
						prevUXR = null;
					}
				}
			} catch (SAFRNotFoundException snfe) {
				MessageDialog
						.openWarning(
								Display.getCurrent().getActiveShell(),
								"Warning",
								"The User Exit Routine with id ["
										+ snfe.getComponentId()
										+ "] referred to by this Logical Record does not exist. Please select another User Exit Routine if required.");
				comboUserExitRoutine.setText("[" + snfe.getComponentId() + "]");

			} catch (SAFRException se) {
				UIUtilities
						.handleWEExceptions(
								se,
								"Unexpected error occurred while retrieving Lookup Exit Routine for Logical Record.",
								null);
			}
			comboUserExitRoutine.setText(selectedUserExit);

			if (!comboUserExitRoutine.getText().equals("")) {
				UIUtilities.checkNullText(textUserExitRoutineParam,
						logicalRecord.getLookupExitParams());
				enableUserExitRoutineParams();
			} else {
				disableUserExitRoutineParams();
			}
		}
	}

	public void refreshModel() {
        if (logicalRecord.getId() > 0 && !mediator.isSaveAs()) {
            try {
                if (logicalRecord.getId() > 0) {
                    if (logicalRecord.getLRTypeCode() != null) {
                        if (!(logicalRecord.getLRTypeCode().equals(UIUtilities
                                .getCodeFromCombo(comboType)))) {
                            logicalRecord.setCheckViewDependencies(true);
                        }
                    }
                }
            } catch (SAFRException se) {
                UIUtilities.handleWEExceptions(se,
                    "Unexpected error occurred while retrieving Logical Record Type code.",null);
            }
        }
        
        if (!UIUtilities.isEqualString(logicalRecord.getName(), textName.getText())) {
            logicalRecord.setName(textName.getText());
        }

        // mandatory Code field. call setter only if value from combo is not
        // null, else a NPE will be thrown.
        Code codeType = UIUtilities.getCodeFromCombo(comboType);
        if (codeType != null &&
            codeType != logicalRecord.getLRTypeCode()) {
            logicalRecord.setLRTypeCode(codeType);
        }
        Code codeStatus = UIUtilities.getCodeFromCombo(comboStatus);
        if (codeStatus != null &&
            codeStatus != logicalRecord.getLRStatusCode()) {
            logicalRecord.setLRStatusCode(codeStatus);
        }

        boolean flag = false;
        try {
            UserExitRoutine userExitRoutine = null;
            UserExitRoutineQueryBean userExitRoutineBean = null;
            if (comboUserExitRoutine.getSelectionIndex() >= 0) {
                userExitRoutineBean = (UserExitRoutineQueryBean) comboUserExitRoutine
                        .getTable().getSelection()[0].getData();
                // CQ 9086. Nikita. 14/02/2011
                // On sorting the UXR combo, the index of the blank item no
                // longer remains 0. Hence, check the id of the bean retrieved
                // to determine whether it is a valid UXR
                if (userExitRoutineBean != null
                        && userExitRoutineBean.getId() > 0) {
                    userExitRoutine = SAFRApplication.getSAFRFactory()
                            .getUserExitRoutine(userExitRoutineBean.getId());

                    // CQ 8596. Nikita. 12/10/2010
                    // If LR contains an invalid UXR, and the user then selects
                    // a
                    // valid UXR and tries to save, the call to
                    // getLookupExitRoutine
                    // will throw a SAFRNotFoundException. Hence it needs to be
                    // enclosed in a try-catch block
                    try {
                        if (logicalRecord.getLookupExitRoutine() == null
                                && userExitRoutine != null) {
                            flag = true;
                        } else if (!logicalRecord.getLookupExitRoutine()
                                .getId().equals(userExitRoutine.getId())) {
                            flag = true;
                        }
                    } catch (SAFRNotFoundException snfe) {
                        logger.log(Level.INFO, "", snfe);
                        // doesn't need to be handled as the
                        // SAFRNotFoundException
                        // is already handled in the doRefreshControls method
                        // and in
                        // the validate method of the model
                    }
                    if (flag) {
                        logicalRecord.setLookupExitRoutine(userExitRoutine);
                    }
                } else {
                    if (comboUserExitRoutine.getText().equals("")) {
                        logicalRecord.setLookupExitRoutine(null);
                    }
                }
            } else {
                // CQ 8596. Nikita. 12/10/2010
                // If LR contains an invalid UXR, and the user tries to save
                // without selecting a valid UXR or by selecting a blank value,
                // the call to getLookupExitRoutine will throw a
                // SAFRNotFoundException. Hence it needs to be enclosed in a
                // try-catch block
                try {
                    if (logicalRecord.getLookupExitRoutine() != null) {
                        flag = true;
                    }
                } catch (SAFRNotFoundException snfe) {
                    logger.log(Level.INFO, "", snfe);
                    // doesn't need to be handled as the SAFRNotFoundException
                    // is already handled in the doRefreshControls method and in
                    // the validate method of the model
                }
                if (flag && comboUserExitRoutine.getText().equals("")) {
                    logicalRecord.setLookupExitRoutine(null);
                }
            }
            // if user-exit changed, check view dependencies at SAVE
            if (flag && logicalRecord.getId() > 0) {
                logicalRecord.setCheckViewDependencies(true);
            }
        } catch (SAFRException e) {
            UIUtilities.handleWEExceptions(e);
        }

        if (!UIUtilities.isEqualString(logicalRecord.getLookupExitParams(), textUserExitRoutineParam.getText())) {
            logicalRecord.setLookupExitParams(textUserExitRoutineParam.getText());
        }
        if (!UIUtilities.isEqualString(logicalRecord.getComment(),textComments.getText())) {
            logicalRecord.setComment(textComments.getText());
        }
    }

    public Control getControlFromProperty(Object property) {
        if (property == LogicalRecord.Property.LR_NAME) {
            return textName;
        } else if (property == LogicalRecord.Property.LOOKUP_USER_EXIT_ROUTINE) {
            return comboUserExitRoutine;
        } else if (property == LogicalRecord.Property.TYPE) {
            return comboType;
        } else if (property == LogicalRecord.Property.STATUS) {
            return comboStatus;
        } else {
            return null;
        }
    }

    public String getName() {
        return textName.getText();
    }
    
}
