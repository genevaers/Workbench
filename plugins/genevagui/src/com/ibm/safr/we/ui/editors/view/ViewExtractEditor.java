package com.ibm.safr.we.ui.editors.view;

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


import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.View.Property;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.utilities.DepCheckOpener;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ViewExtractEditor {
    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.view.ViewExtractEditor");
    
    private static final int MAX_EXTRACT_RECORD_BUFFER = 9;
    private static final int MAX_EXTRACT_WORK_FILE_NO = 9;
    
    // member variables
    private ViewMediator mediator;
    private CTabFolder parent;
    private View view;

    // widgets
    private Composite compositeExtract;
    private CTabItem tabExtract;
    private Section sectionEOutputFile;
    private Label labelELogicalFile;
    private Label labelEPhysicalFile;
    private TableCombo comboELogicalFile;
    private TableComboViewer comboELogicalFileViewer;
    private TableCombo comboEPhysicalFile;
    private TableComboViewer comboEPhysicalFileViewer;
    private Section sectionEOutputUserExit;
    private Label labelEUserExitName;
    private TableCombo comboEUserExitName;
    private TableComboViewer comboEUserExitNameViewer;
    private Label labelEUserExitParameters;
    private Text textEUserExitParameters;
    private Section sectionERecordAggregation;
    private Button radioEDontAggregateRecords;
    private Button radioEAggregateRecords;
    private Text textEAggregateRecords;
    private Section sectionEOutputLimit;
    private Button radioEWriteEligibleRecords;
    private Button radioEStopProcessing;
    private Text textEStopProcessing;
    private Section sectionExtractWorkFileNo;
    private Text textExtractWorkFileNo;

    // enable/disable open editor menu 
    private MenuItem elfOpenEditorItem = null;
    private MenuItem elfDepCheckItem = null;
    private MenuItem epfOpenEditorItem = null;
    private MenuItem epfDepCheckItem = null;
    private MenuItem eueOpenEditorItem = null;
    private MenuItem eueDepCheckItem = null;
    
    // state
    private String selectedELogicalFile = "";
    private String selectedEPhysicalFile = "";
    private String selectedEUserExit = "";
    
    private Boolean toggleAccKey_y_Ext = true;
    private Boolean toggleAccKey_n = true;
    
    protected ViewExtractEditor(ViewMediator mediator, CTabFolder parent, View view) {
        this.mediator = mediator;
        this.parent = parent;
        this.view = view;
    }    
    
    protected void create() {
        createCompositeExtract();
        tabExtract = new CTabItem(parent, SWT.NONE);
        tabExtract.setText("E&xtract Phase");
        tabExtract.setControl(compositeExtract);
    }
    
    
    private void createCompositeExtract() {
        compositeExtract = mediator.getGUIToolKit().createComposite(parent, SWT.NONE);
        FormLayout layoutComposite = new FormLayout();
        compositeExtract.setLayout(layoutComposite);

        FormData dataComposite = new FormData();
        dataComposite.left = new FormAttachment(0, 0);
        dataComposite.right = new FormAttachment(100, 0);
        dataComposite.top = new FormAttachment(0, 0);
        dataComposite.bottom = new FormAttachment(100, 0);
        compositeExtract.setLayoutData(dataComposite);

        Composite compositeExtractGroups = mediator.getGUIToolKit().createComposite(
                compositeExtract, SWT.NONE);
        compositeExtractGroups.setLayout(new FormLayout());
        FormData dataCompositeExtractGroupsLeft = new FormData();
        dataCompositeExtractGroupsLeft.left = new FormAttachment(0, 0);
        dataCompositeExtractGroupsLeft.right = new FormAttachment(100, -400);
        compositeExtractGroups.setLayoutData(dataCompositeExtractGroupsLeft);

        /*
         * section Extract-Phase Record Aggregation
         */
        sectionERecordAggregation = mediator.getGUIToolKit().createSection(
                compositeExtractGroups, Section.TITLE_BAR,
                "Extract-Phase Record Aggregation (ERA)");
        FormData dataSectionERecordAggregation = new FormData();

        dataSectionERecordAggregation.top = new FormAttachment(0, 10);
        dataSectionERecordAggregation.left = new FormAttachment(0, 10);
        dataSectionERecordAggregation.right = new FormAttachment(100, 0);
        sectionERecordAggregation.setLayoutData(dataSectionERecordAggregation);

        Composite compositeERecordAggregation = mediator.getGUIToolKit().createComposite(
                sectionERecordAggregation, SWT.NONE);
        compositeERecordAggregation.setLayout(new FormLayout());

        sectionERecordAggregation.setClient(compositeERecordAggregation);

        radioEDontAggregateRecords = mediator.getGUIToolKit().createRadioButton(
                compositeERecordAggregation, "Do not aggregate record&s");
        radioEDontAggregateRecords.setData(SAFRLogger.USER, "Extract Do not aggregate records");              
        FormData dataRadioEDontAggregateRecords = new FormData();
        dataRadioEDontAggregateRecords.top = new FormAttachment(0, 10);
        dataRadioEDontAggregateRecords.left = new FormAttachment(0, 10);
        radioEDontAggregateRecords
                .setLayoutData(dataRadioEDontAggregateRecords);
        radioEDontAggregateRecords.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);

                if (radioEDontAggregateRecords.getSelection() && view.isExtractAggregateBySortKey() ) {
                    mediator.setModified(true);
                    view.setExtractAggregateBufferSize(0);
                    view.setExtractAggregateBySortKey(false);
                    textEAggregateRecords.setText("0");                    
                    textEAggregateRecords.setEnabled(false);
                }
            }

        });

        radioEAggregateRecords = mediator.getGUIToolKit()
                .createRadioButton(compositeERecordAggregation,
                        "Aggregate records with identical sort ke&ys using a buffer of ");
        radioEAggregateRecords.setData(SAFRLogger.USER, "Extract Aggregate records with identical sort keys");                        
        radioEAggregateRecords.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_y_Ext) {
                        e.doit = false;
                        toggleAccKey_y_Ext = false;
                    } else {
                        toggleAccKey_y_Ext = true;
                    }
                }
            }
        });
        FormData dataRadioEAggregateRecords = new FormData();
        dataRadioEAggregateRecords.top = new FormAttachment(
                radioEDontAggregateRecords, 10);
        dataRadioEAggregateRecords.left = new FormAttachment(0, 10);
        dataRadioEAggregateRecords.bottom = new FormAttachment(100, -10);
        radioEAggregateRecords.setLayoutData(dataRadioEAggregateRecords);
        radioEAggregateRecords.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (radioEAggregateRecords.getSelection() && !view.isExtractAggregateBySortKey()) {
                    mediator.setModified(true);
                    view.setExtractAggregateBufferSize(
                        UIUtilities.stringToInteger(textEAggregateRecords.getText()));
                    view.setExtractAggregateBySortKey(true);
                    textEAggregateRecords.setText(view.getExtractAggregateBufferSize().toString());                    
                    textEAggregateRecords.setEnabled(true);
                }
            }

        });

        textEAggregateRecords = mediator.getGUIToolKit().createIntegerTextBox(
                compositeERecordAggregation, SWT.NONE, false);
        textEAggregateRecords.setData(SAFRLogger.USER, "Extract Aggregate records with identical sort keys buffer");                      
        
        FormData dataTextEAggregateRecords = new FormData();
        dataTextEAggregateRecords.top = new FormAttachment(radioEDontAggregateRecords, 10);
        dataTextEAggregateRecords.left = new FormAttachment(radioEAggregateRecords, 0);
        dataTextEAggregateRecords.bottom = new FormAttachment(100, -10);
        dataTextEAggregateRecords.width = 60;
        textEAggregateRecords.setLayoutData(dataTextEAggregateRecords);
        textEAggregateRecords.setTextLimit(MAX_EXTRACT_RECORD_BUFFER);
        textEAggregateRecords.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                mediator.setModified(true);
            }
        });
        textEAggregateRecords.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqual(view.getExtractAggregateBufferSize(),
                        UIUtilities.stringToInteger(textEAggregateRecords
                                .getText()))) {
                    view.setExtractAggregateBufferSize(UIUtilities
                            .stringToInteger(textEAggregateRecords.getText()));
                    mediator.setModified(true);
                }
            }

        });

        Label labelEAggregateRecords = mediator.getGUIToolKit().createLabel(
                compositeERecordAggregation, SWT.NONE, "record(s)");
        FormData dataLabelEAggregateRecords = new FormData();
        dataLabelEAggregateRecords.top = new FormAttachment(
                radioEDontAggregateRecords, 10);
        dataLabelEAggregateRecords.left = new FormAttachment(
                textEAggregateRecords, 10);
        dataLabelEAggregateRecords.right = new FormAttachment(100, -10);
        dataLabelEAggregateRecords.bottom = new FormAttachment(100, -10);
        labelEAggregateRecords.setLayoutData(dataLabelEAggregateRecords);
        /*
         * End of section Extract-Phase Record Aggregation
         */

        /*
         * section Extract-Phase Output Limit
         */
        sectionEOutputLimit = mediator.getGUIToolKit().createSection(compositeExtractGroups,
                Section.TITLE_BAR, "Extract-Phase Output Limit");
        FormData dataSectionEOutputLimit = new FormData();
        dataSectionEOutputLimit.top = new FormAttachment(
                sectionERecordAggregation, 10);
        dataSectionEOutputLimit.left = new FormAttachment(0, 10);
        dataSectionEOutputLimit.right = new FormAttachment(100, 0);
        sectionEOutputLimit.setLayoutData(dataSectionEOutputLimit);

        Composite compositeEOutputLimit = mediator.getGUIToolKit().createComposite(
                sectionEOutputLimit, SWT.NONE);
        compositeEOutputLimit.setLayout(new FormLayout());

        sectionEOutputLimit.setClient(compositeEOutputLimit);

        radioEWriteEligibleRecords = mediator.getGUIToolKit().createRadioButton(
                compositeEOutputLimit, "Write all eligi&ble records");
        radioEWriteEligibleRecords.setData(SAFRLogger.USER, "Extract Write all eligible records");                        
        
        FormData dataRadioEWriteEligibleRecords = new FormData();
        dataRadioEWriteEligibleRecords.top = new FormAttachment(0, 10);
        dataRadioEWriteEligibleRecords.left = new FormAttachment(0, 10);
        radioEWriteEligibleRecords
                .setLayoutData(dataRadioEWriteEligibleRecords);
        radioEWriteEligibleRecords.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (!UIUtilities.isEqual(view.hasExtractPhaseOutputLimit(),false)) {
                    mediator.setModified(true);
                    view.setExtractMaxRecords(0);
                    view.setExtractPhaseOutputLimit(false);
                    selectExractRadioButtons();
                }
            }

        });

        radioEStopProcessing = mediator.getGUIToolKit().createRadioButton(
                compositeEOutputLimit,
                "Sto&p Extract-Phase processing for this view after ");
        radioEStopProcessing.setData(SAFRLogger.USER, "Stop Extract-Phase processing");                               
        FormData dataRadioEStopProcessing = new FormData();
        dataRadioEStopProcessing.top = new FormAttachment(
                radioEWriteEligibleRecords, 10);
        dataRadioEStopProcessing.left = new FormAttachment(0, 10);
        dataRadioEStopProcessing.bottom = new FormAttachment(100, -10);
        radioEStopProcessing.setLayoutData(dataRadioEStopProcessing);
        radioEStopProcessing.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (!UIUtilities.isEqual(view.hasExtractPhaseOutputLimit(), true)) {
                    mediator.setModified(true);
                    view.setExtractMaxRecords(UIUtilities
                            .stringToInteger(textEStopProcessing.getText()));
                    view.setExtractPhaseOutputLimit(true);
                    selectExractRadioButtons();
                }
            }
        });

        textEStopProcessing = mediator.getGUIToolKit().createIntegerTextBox(
                compositeEOutputLimit, SWT.NONE, false);
        textEStopProcessing.setData(SAFRLogger.USER, "Stop Extract-Phase processing records");                                        
        FormData dataTextEStopProcessing = new FormData();
        dataTextEStopProcessing.top = new FormAttachment(
                radioEWriteEligibleRecords, 10);
        dataTextEStopProcessing.left = new FormAttachment(radioEStopProcessing,
                0);
        dataTextEStopProcessing.bottom = new FormAttachment(100, -10);
        dataTextEStopProcessing.width = 60;
        textEStopProcessing.setLayoutData(dataTextEStopProcessing);
        textEStopProcessing.setTextLimit(ViewEditor.MAX_RECORDS);
        textEStopProcessing.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                mediator.setModified(true);
            }
        });
        textEStopProcessing.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqual(view.getExtractMaxRecords(),
                        UIUtilities.stringToInteger(textEStopProcessing
                                .getText()))) {
                    view.setExtractMaxRecords(UIUtilities
                            .stringToInteger(textEStopProcessing.getText()));
                    mediator.setModified(true);
                }
            }

        });

        Label labelEStopProcessing = mediator.getGUIToolKit().createLabel(
                compositeEOutputLimit, SWT.NONE, "record(s) are written");
        FormData dataLabelEStopProcessing = new FormData();
        dataLabelEStopProcessing.top = new FormAttachment(
                radioEWriteEligibleRecords, 10);
        dataLabelEStopProcessing.left = new FormAttachment(textEStopProcessing,
                10);
        dataLabelEStopProcessing.right = new FormAttachment(100, -10);
        dataLabelEStopProcessing.bottom = new FormAttachment(100, -10);
        labelEStopProcessing.setLayoutData(dataLabelEStopProcessing);

        selectExractRadioButtons();
        /*
         * End of section Extract-Phase Output Limit
         */

        /*
         * section Extract-Phase Work File No
         */
        sectionExtractWorkFileNo = mediator.getGUIToolKit().createSection(compositeExtract,
                Section.TITLE_BAR, "");
        FormData dataSectionExtractWorkFileNo = new FormData();
        dataSectionExtractWorkFileNo.left = new FormAttachment(
                compositeExtractGroups, 50);
        dataSectionExtractWorkFileNo.top = new FormAttachment(0, 10);
        dataSectionExtractWorkFileNo.right = new FormAttachment(100, -30);
        sectionExtractWorkFileNo.setLayoutData(dataSectionExtractWorkFileNo);

        Composite compositeExtractWorkFileNo = mediator.getGUIToolKit().createComposite(
                sectionExtractWorkFileNo, SWT.NONE);
        compositeExtractWorkFileNo.setLayout(new FormLayout());

        sectionExtractWorkFileNo.setClient(compositeExtractWorkFileNo);

        Label labelExtractWorkfileNo = mediator.getGUIToolKit().createLabel(
                compositeExtractWorkFileNo, SWT.NONE,
                "File N&umber:");
        FormData dataLabelExtractWorkFileNo = new FormData();
        dataLabelExtractWorkFileNo.top = new FormAttachment(0, 10);
        dataLabelExtractWorkFileNo.left = new FormAttachment(
                compositeExtractGroups, 10);
        labelExtractWorkfileNo.setLayoutData(dataLabelExtractWorkFileNo);

        textExtractWorkFileNo = mediator.getGUIToolKit().createIntegerTextBox(
                compositeExtractWorkFileNo, SWT.NONE, false);
        textExtractWorkFileNo.setData(SAFRLogger.USER, "Extract Work File Number");                               
        
        FormData dataTextExtractWorkFileNo = new FormData();
        dataTextExtractWorkFileNo.top = new FormAttachment(0, 10);
        dataTextExtractWorkFileNo.left = new FormAttachment(
                labelExtractWorkfileNo, 10);
        dataTextExtractWorkFileNo.right = new FormAttachment(100, -10);
        textExtractWorkFileNo.setLayoutData(dataTextExtractWorkFileNo);
        textExtractWorkFileNo.setTextLimit(MAX_EXTRACT_WORK_FILE_NO);
        textExtractWorkFileNo.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                mediator.setModified(true);
            }
        });
        textExtractWorkFileNo.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqualInt(
                        view.getExtractWorkFileNo(), 
                        UIUtilities.stringToInteger(textExtractWorkFileNo.getText()))) {
                    view.setExtractWorkFileNo(UIUtilities
                            .stringToInteger(textExtractWorkFileNo.getText()));
                    // update write statement
                    for (ViewSource viewSrc : view.getViewSources().getActiveItems()) {
                        WriteStatementGenerator generator = new WriteStatementGenerator(view, viewSrc);
                        generator.generateWriteStatement();
                    }                    
                    mediator.setModified(true);
                }

            }

        });
        /*
         * End of section Extract-Phase Work File No
         */

        mediator.refreshExtractRecordAggregationState();
        
    }

    protected void createExtractOutput(Composite compositeExtractGroups) {
        /*
         * section Output File
         */
        sectionEOutputFile = mediator.getGUIToolKit().createSection(compositeExtractGroups,
                Section.TITLE_BAR, "Output File");
        FormData dataSectionEOutputFile = new FormData();
        dataSectionEOutputFile.top = new FormAttachment(0, 10);
        dataSectionEOutputFile.left = new FormAttachment(0, 10);
        dataSectionEOutputFile.right = new FormAttachment(100, 0);
        sectionEOutputFile.setLayoutData(dataSectionEOutputFile);
   
        Composite compositeEOutputFile = mediator.getGUIToolKit().createComposite(
                sectionEOutputFile, SWT.NONE);
        compositeEOutputFile.setLayout(new FormLayout());
   
        sectionEOutputFile.setClient(compositeEOutputFile);
   
        labelELogicalFile = mediator.getGUIToolKit().createLabel(compositeEOutputFile,
                SWT.NONE, "&Logical File:");
   
        comboELogicalFileViewer = mediator.getGUIToolKit().createTableComboForComponents(
                compositeEOutputFile, ComponentType.LogicalFile);
        comboELogicalFile = comboELogicalFileViewer.getTableCombo();
        comboELogicalFile.setData(SAFRLogger.USER, "Extract Logical File");
        addELFOpenEditorMenu();
        
        labelEPhysicalFile = mediator.getGUIToolKit().createLabel(compositeEOutputFile,
                SWT.NONE, "Ph&ysical File:");
        labelEPhysicalFile.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_y_Ext) {
                        e.doit = false;
                        toggleAccKey_y_Ext = false;
                    } else {
                        toggleAccKey_y_Ext = true;
                    }
                }
            }
        });
   
        comboEPhysicalFileViewer = mediator.getGUIToolKit().createTableComboForAssociatedComponents(compositeEOutputFile);
        comboEPhysicalFile = comboEPhysicalFileViewer.getTableCombo();
        comboEPhysicalFile.setData(SAFRLogger.USER, "Extract Physical File");     
        addEPFOpenEditorMenu();
        
        FormData dataLabelELogicalFile = new FormData();
        dataLabelELogicalFile.top = new FormAttachment(0, 10);
        dataLabelELogicalFile.left = new FormAttachment(0, 10);
        labelELogicalFile.setLayoutData(dataLabelELogicalFile);
   
        FormData dataComboELogicalFile = new FormData();
        dataComboELogicalFile.top = new FormAttachment(0, 10);
        dataComboELogicalFile.left = new FormAttachment(labelEPhysicalFile, 10);
        dataComboELogicalFile.right = new FormAttachment(100, -10);
        comboELogicalFile.setLayoutData(dataComboELogicalFile);
   
        comboELogicalFile.addFocusListener(new FocusAdapter() {
   
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!(comboELogicalFile.getText().equals(selectedELogicalFile))) {   
                    mediator.populatePhysicalFileCombo(
                        comboELogicalFile,comboEPhysicalFile, comboEPhysicalFileViewer);
   
                    LogicalFileQueryBean logicalFileBean = null;
                    if (comboELogicalFile.getTable().getSelection().length > 0) {
                        logicalFileBean = (LogicalFileQueryBean) comboELogicalFile
                                .getTable().getSelection()[0].getData();
                    }
                    if (logicalFileBean == null || logicalFileBean.getId() <= 0) {
                        view.setExtractFileAssociation(null);   
                    }
   
                    // reset Physical File index as this combo has been reloaded
                    selectedEPhysicalFile = "";
                    comboEPhysicalFile.select(-1);
                    mediator.setModified(true);
                    selectedELogicalFile = comboELogicalFile.getText();
                }
            }
   
        });
   
        FormData dataLabelEPhysicalFile = new FormData();
        dataLabelEPhysicalFile.top = new FormAttachment(comboELogicalFile, 10);
        dataLabelEPhysicalFile.left = new FormAttachment(0, 10);
        labelEPhysicalFile.setLayoutData(dataLabelEPhysicalFile);
   
        FormData dataComboEPhysicalFile = new FormData();
        dataComboEPhysicalFile.top = new FormAttachment(comboELogicalFile, 10);
        dataComboEPhysicalFile.left = new FormAttachment(labelEPhysicalFile, 10);
        dataComboEPhysicalFile.right = new FormAttachment(100, -10);
        dataComboEPhysicalFile.bottom = new FormAttachment(100, -10);
        comboEPhysicalFile.setLayoutData(dataComboEPhysicalFile);
        comboEPhysicalFile.addFocusListener(new FocusAdapter() {
   
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
   
                if (!comboEPhysicalFile.getText().equals(selectedEPhysicalFile)) {
                    FileAssociation physicalFile = null;
                    if (comboEPhysicalFile.getTable().getSelection().length > 0) {
                        physicalFile = (FileAssociation) comboEPhysicalFile.getTable().getSelection()[0].getData();
                    }
                    if (physicalFile != null) {
                        view.setExtractFileAssociation(physicalFile);
                    }
                    mediator.setModified(true);
                    selectedEPhysicalFile = comboEPhysicalFile.getText();
                }
            }
   
        });
        /*
         * End of section Output File
         */
   
        /*
         * section Output User-Exit Routine
         */
        sectionEOutputUserExit = mediator.getGUIToolKit().createSection(
                compositeExtractGroups, Section.TITLE_BAR,
                "Write User-Exit Routine");
        FormData dataSectionEOutputUserExit = new FormData();
        dataSectionEOutputUserExit.top = new FormAttachment(sectionEOutputFile,
                10);
        dataSectionEOutputUserExit.left = new FormAttachment(0, 10);
        dataSectionEOutputUserExit.right = new FormAttachment(100, 0);
        sectionEOutputUserExit.setLayoutData(dataSectionEOutputUserExit);
   
        Composite compositeEOutputUserExit = mediator.getGUIToolKit().createComposite(
                sectionEOutputUserExit, SWT.NONE);
        compositeEOutputUserExit.setLayout(new FormLayout());
   
        sectionEOutputUserExit.setClient(compositeEOutputUserExit);
   
        labelEUserExitName = mediator.getGUIToolKit().createLabel(compositeEOutputUserExit,
                SWT.NONE, "Na&me:");
        labelEUserExitName.addTraverseListener(new TraverseListener() {
   
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_n) {
                        e.doit = false;
                        toggleAccKey_n = false;
                    }
                }
            }
   
        });
   
        comboEUserExitNameViewer = mediator.getGUIToolKit().createTableComboForExits(
                compositeEOutputUserExit, ComponentType.UserExitRoutine);
        comboEUserExitName = comboEUserExitNameViewer.getTableCombo();
        comboEUserExitName.setData(SAFRLogger.USER, "Extract User-Exit Routine");     
        addEUEOpenEditorMenu();
        
        labelEUserExitParameters = mediator.getGUIToolKit().createLabel(
                compositeEOutputUserExit, SWT.NONE, "Parame&ter:");
   
        textEUserExitParameters = mediator.getGUIToolKit().createTextBox(
                compositeEOutputUserExit, SWT.NONE);
        textEUserExitParameters.setData(SAFRLogger.USER, "Extract User-Exit Param");      
        
        FormData dataLabelEName = new FormData();
        dataLabelEName.top = new FormAttachment(0, 10);
        dataLabelEName.left = new FormAttachment(0, 10);
        labelEUserExitName.setLayoutData(dataLabelEName);
   
        FormData dataComboEName = new FormData();
        dataComboEName.top = new FormAttachment(0, 10);
        dataComboEName.left = new FormAttachment(labelEUserExitParameters, 10);
        dataComboEName.right = new FormAttachment(100, -10);
        comboEUserExitName.setLayoutData(dataComboEName);
        // CQ 8459. Nikita. 26/08/2010.
        // To resolve issues with enabling/disabling and focus of UXR parameter
        // text-box
        comboEUserExitName.addSelectionListener(new SelectionAdapter() {
   
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                enableUserExitRoutineParams(comboEUserExitName, textEUserExitParameters);
            }
        });
        comboEUserExitName.addFocusListener(new FocusAdapter() {
   
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
   
                if (!comboEUserExitName.getText().equals(selectedEUserExit)) {
                    textEUserExitParameters.setText("");
   
                    UserExitRoutineQueryBean userExitRoutineBean = null;
                    if (comboEUserExitName.getTable().getSelection().length > 0) {
                        userExitRoutineBean = (UserExitRoutineQueryBean) 
                            comboEUserExitName.getTable().getSelection()[0].getData();
                    }
   
                    UserExitRoutine userExitRoutine = null;
                    if (userExitRoutineBean != null&& userExitRoutineBean.getId() > 0) {
                        try {
                            userExitRoutine = SAFRApplication.getSAFRFactory().getUserExitRoutine(userExitRoutineBean.getId());
                        } catch (SAFRNotFoundException e2) {
                            UIUtilities.handleWEExceptions(e2, "",UIUtilities.titleStringNotFoundException);
                        } catch (SAFRException e1) {
                            UIUtilities.handleWEExceptions(e1,"Error retrieving metadata component.",null);
                        }
                    }
                    view.setWriteExit(userExitRoutine);
                    mediator.setModified(true);
                    selectedEUserExit = comboEUserExitName.getText();
                }
            }
   
        });
   
        FormData dataLabelEParameters = new FormData();
        dataLabelEParameters.top = new FormAttachment(comboEUserExitName, 10);
        dataLabelEParameters.left = new FormAttachment(0, 10);
        labelEUserExitParameters.setLayoutData(dataLabelEParameters);
   
        FormData dataTextEParameters = new FormData();
        dataTextEParameters.top = new FormAttachment(comboEUserExitName, 10);
        dataTextEParameters.left = new FormAttachment(labelEUserExitParameters, 10);
        dataTextEParameters.right = new FormAttachment(100, -10);
        dataTextEParameters.bottom = new FormAttachment(100, -10);
        textEUserExitParameters.setLayoutData(dataTextEParameters);
        textEUserExitParameters.setTextLimit(ViewEditor.MAX_USER_EXIT_PARAM);
        textEUserExitParameters.setEnabled(false);
        textEUserExitParameters.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                mediator.setModified(true);
            }
        });
        
        textEUserExitParameters.addFocusListener(new FocusAdapter() {
   
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
   
                if (!UIUtilities.isEqualString(view.getWriteExitParams(),
                    textEUserExitParameters.getText())) {
                    view.setWriteExitParams(textEUserExitParameters.getText());
                    mediator.setModified(true);
                }
            }
   
        });
        /*
         * End of section Output User-Exit Routine
         */
    }

    private void addELFOpenEditorMenu()
    {
        Text text = comboELogicalFile.getTextControl();
        Menu menu = text.getMenu();
        elfOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        elfOpenEditorItem.setText("Open Editor");
        elfOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                LogicalFileQueryBean bean = (LogicalFileQueryBean)((StructuredSelection) comboELogicalFileViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.LogicalFile);                        
                }                
            }
        });
        
        elfDepCheckItem = new MenuItem(menu, SWT.PUSH);
        elfDepCheckItem.setText("Dependency Checker");
        elfDepCheckItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                LogicalFileQueryBean bean = (LogicalFileQueryBean)((StructuredSelection) comboELogicalFileViewer
                    .getSelection()).getFirstElement();
                if (bean != null) {
                    DepCheckOpener.open(bean);
                }                
            }
        });
                
        comboELogicalFile.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    LogicalFileQueryBean bean = (LogicalFileQueryBean)((StructuredSelection) comboELogicalFileViewer
                            .getSelection()).getFirstElement();
                    if (bean != null && bean.getId() != 0) {
                        elfDepCheckItem.setEnabled(true);
                        elfOpenEditorItem.setEnabled(true);                            
                    }
                    else {
                        elfDepCheckItem.setEnabled(false);
                        elfOpenEditorItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }       
    
    private void addEPFOpenEditorMenu()
    {
        Text text = comboEPhysicalFile.getTextControl();
        Menu menu = text.getMenu();
        epfOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        epfOpenEditorItem.setText("Open Editor");
        epfOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                FileAssociation ass = (FileAssociation)((StructuredSelection) comboEPhysicalFileViewer
                        .getSelection()).getFirstElement();
                if (ass != null) {   
                    EditorOpener.open(ass.getAssociatedComponentIdNum(), ComponentType.PhysicalFile);                        
                }                
            }
        });
        
        epfDepCheckItem = new MenuItem(menu, SWT.PUSH);
        epfDepCheckItem.setText("Dependency Checker");
        epfDepCheckItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                FileAssociation ass = (FileAssociation)((StructuredSelection) comboEPhysicalFileViewer
                    .getSelection()).getFirstElement();
                if (ass != null) {
                    DepCheckOpener.open(new PhysicalFileQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(),
                        ass.getAssociatedComponentIdNum(), 
                        null, null, null, null, null, null, null, null, null, null, null, null));
                }                
            }
        });
        
        comboEPhysicalFile.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    FileAssociation ass = (FileAssociation)((StructuredSelection) comboEPhysicalFileViewer
                            .getSelection()).getFirstElement();
                    if (ass != null) {
                        epfOpenEditorItem.setEnabled(true); 
                        epfDepCheckItem.setEnabled(true);
                    }
                    else {
                        epfOpenEditorItem.setEnabled(false);
                        epfDepCheckItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }       
    
    private void addEUEOpenEditorMenu()
    {
        Text text = comboEUserExitName.getTextControl();
        Menu menu = text.getMenu();
        eueOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        eueOpenEditorItem.setText("Open Editor");
        eueOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean)((StructuredSelection) comboEUserExitNameViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.UserExitRoutine);                        
                }                
            }
        });
        
        eueDepCheckItem = new MenuItem(menu, SWT.PUSH);
        eueDepCheckItem.setText("Dependency Checker");
        eueDepCheckItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean)((StructuredSelection) comboEUserExitNameViewer
                    .getSelection()).getFirstElement();
                if (bean != null) {
                    DepCheckOpener.open(bean);
                }                
            }
        });
        
        comboEUserExitName.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean)((StructuredSelection) comboEUserExitNameViewer
                            .getSelection()).getFirstElement();
                    if (bean != null && bean.getId() != 0) {   
                        eueOpenEditorItem.setEnabled(true); 
                        eueDepCheckItem.setEnabled(true);
                    }
                    else {
                        eueOpenEditorItem.setEnabled(false);
                        eueDepCheckItem.setEnabled(false);
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
     * This method disables the User-Exit Routine Parameters text-box if no
     * value has been selected in the User-Exit Routine combo-box for both,
     * Extract and Format Phases.
     * 
     * @param combo
     *            the Extract or Format Phase User-Exit Routine combo-box
     * @param textParam
     *            the Extract or Format Phase User-Exit Routine Parameters
     *            text-box
     */
    protected void disableUserExitRoutineParams(TableCombo combo, Text textParam) {
    	EnvironmentalQueryBean uxrBean = null;
    	 if(combo.getTable().isSelected(0)) {
    		 uxrBean = (EnvironmentalQueryBean) combo.getTable().getSelection()[0].getData();
    	 }
        if (uxrBean != null && uxrBean.getId() <= 0) {
            textParam.setText("");
            textParam.setEnabled(false);
        }
    }
    
    /**
     * This method enables the User-Exit Routine Parameters text-box if a value
     * has been selected in the User-Exit Routine combo-box for both, Extract
     * and Format Phases.
     * 
     * @param combo
     *            the Extract or Format Phase User-Exit Routine combo-box
     * @param textParam
     *            the Extract or Format Phase User-Exit Routine Parameters
     *            text-box
     */
    protected void enableUserExitRoutineParams(TableCombo combo, Text textParam) {
        // CQ 8459. Nikita. 26/08/2010.
        // To resolve issues with enabling/disabling and focus of UXR parameter
        // text-box

        if (combo.getText().length() > 0) {
            textParam.setEnabled(!mediator.getGUIToolKit().isReadOnly());
        }
    }

    public void refreshExtractRecordAggregationState() {
        switch (view.getOutputFormat()) {
        case Extract_Fixed_Width_Fields:
        case Extract_Source_Record_Layout:
            disableAggregation();
            break;
        case Format_Fixed_Width_Fields:
        case Format_Delimited_Fields:
        case Format_Report:
            if (view.isFormatPhaseRecordAggregationOn()) {
                enableAggregation();
            } else {
                disableAggregation();                
            }
            break;
        }
    }

    protected void enableAggregation() {
        sectionERecordAggregation.setEnabled(true);    
        radioEAggregateRecords.setEnabled(true);
        radioEDontAggregateRecords.setEnabled(true);
        if (view.isExtractAggregateBySortKey()) {
            radioEAggregateRecords.setSelection(true);
            radioEDontAggregateRecords.setSelection(false);
            textEAggregateRecords.setText(view.getExtractAggregateBufferSize().toString());
            textEAggregateRecords.setEnabled(true);
        } else {
            radioEAggregateRecords.setSelection(false);
            radioEDontAggregateRecords.setSelection(true);
            textEAggregateRecords.setText("0");
            textEAggregateRecords.setEnabled(false);            
        }
    }

    protected void disableAggregation() {
        radioEAggregateRecords.setSelection(false);
        radioEDontAggregateRecords.setSelection(true);
        radioEAggregateRecords.setEnabled(false);
        radioEDontAggregateRecords.setEnabled(false);
        textEAggregateRecords.setText("0");
        textEAggregateRecords.setEnabled(false);
        sectionERecordAggregation.setEnabled(false);        
    }
    
    /**
     * This method sets the default selection for the 'Extract-Phase Record
     * Aggregation' and 'Extract-Phase Output Limit' radio buttons
     */
	private void selectExractRadioButtons() {
		Integer maxRecords = view.getExtractMaxRecords();
		if (maxRecords != null && maxRecords > 0) {
			radioEStopProcessing.setSelection(true);
			textEStopProcessing.setEnabled(!mediator.getGUIToolKit().isReadOnly());
			textEStopProcessing.setText(Integer.toString(view.getExtractMaxRecords()));
			radioEWriteEligibleRecords.setSelection(false);
		} else {
			radioEWriteEligibleRecords.setSelection(true);
			radioEStopProcessing.setSelection(false);
			textEStopProcessing.setEnabled(false);
			textEStopProcessing.setText("0");
		}
	}

    /**
     * The 'Extract Output File' section in the Extract Phase is used only when
     * the Format Phase is not in use. This method enables or disables the
     * controls in the section 'Extract Output File' depending on the boolean
     * value passed as parameter.
     * 
     * @param enable
     *            Boolean value indicating whether the controls need to be
     *            enabled (enable=true) or disabled (enable=false)
     */
    protected void enableExtractOutputFile(boolean enable) {   
    	return;            
    }


    protected void deselectExtractLFPF() {
        comboELogicalFile.getTable().deselectAll();
        comboEPhysicalFile.getTable().deselectAll();
    }
    
    /**
     * Loads the controls pertaining to the Extract Phase with values from the
     * model object.
     * 
     * @throws SAFRException
     */

    protected void loadExtractPhase() throws SAFRException {
            return;            
    }

    /**
     * This method sets the selection of the combo-box based on the ID of the
     * component passed as parameter.
     * 
     * @param id
     *            the ID of the component which is to be selected in the
     *            combo-box
     * @param combo
     *            the combo-box whose selection is to be set
     * @throws DAOException
     * @throws SAFRException
     */
    private void selectComponentInCombo(String name, Integer id,
            TableCombo combo) throws DAOException, SAFRException {
        // CQ 8786. Nikita. 25/10/2010
        // Use name-id pair to search for the component in the combo
        combo.select(combo.indexOf(UIUtilities.getComboString(name, id)));
    }

    /**
     * This method sets the selection of the Physical File combo-box as per
     * value obtained from the model object.
     * 
     * @param combo
     *            the combo-box whose selection is to be set
     * @throws DAOException
     * @throws SAFRException
     */
    private void selectPhysicalFileInCombo(TableCombo combo)
            throws DAOException, SAFRException {
        FileAssociation fileAssociation = view.getExtractFileAssociation();
        if (fileAssociation == null) {
            return;
        }

        // CQ 8786. Nikita. 25/10/2010
        // Use name-id pair to search for the component in the combo
        String searchString = UIUtilities.getComboString(fileAssociation
                .getAssociatedComponentName(), fileAssociation
                .getAssociatedComponentIdNum());
        int index = combo.indexOf(searchString);
        if (index >= 0) {
            combo.select(index);
        }
    }

    protected void populateExtractCombos() {
            return;            
    }

    protected void refreshControlsExtract() {
        if (view.getExtractWorkFileNo() != null) {
            UIUtilities.checkNullText(textExtractWorkFileNo, Integer
                    .toString(view.getExtractWorkFileNo()));
        }  
    }

    protected Control getControlFromProperty(Object property) {
        if (property == Property.WRITE_USER_EXIT_ROTUINE) {
          return comboEUserExitName;
        } else {
          return null;
        }
    }

    protected void setEnableWorkFileNo(boolean b) {
        textExtractWorkFileNo.setEnabled(b);
    }

    protected void setDefaultWorkFileNumber() {
        // When format phase is not used, default value for the
        // field 'Extract Work File No.' is 1
        if (view.getExtractWorkFileNo() != null) {
            textExtractWorkFileNo.setText(Integer.toString(view.getExtractWorkFileNo()));
        }
        textExtractWorkFileNo.setEnabled(false);
    }

    
}
