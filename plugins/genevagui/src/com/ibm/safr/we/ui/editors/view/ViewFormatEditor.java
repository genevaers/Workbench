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


import java.util.logging.Level;
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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.View.Property;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditor;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditorInput;
import com.ibm.safr.we.ui.utilities.DepCheckOpener;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ViewFormatEditor {
    
    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.view.ViewFormatEditor");

    // member variables
    private ViewMediator mediator;
    private CTabFolder parent;
    private View view;
    
    // widgets
    private CTabItem tabFormat;    
    private Composite compositeFormat;
    
    private Section sectionFOutputFile;
    private Label labelFLogicalFile;
    private Label labelFPhysicalFile;
    private TableCombo comboFLogicalFile;
    private TableComboViewer comboFLogicalFileViewer;
    private TableCombo comboFPhysicalFile;
    private TableComboViewer comboFPhysicalFileViewer;
    private Section sectionFOutputUserExit;
    private TableCombo comboFUserExitName;
    private TableComboViewer comboFUserExitNameViewer;
    private Text textFUserExitParameters;
    private Section sectionFRecordAggregation;
    private Button radioFDontAggregateRecords;
    private Button radioFAggregateRecords;
    private Section sectionFOutputLimit;
    private Button radioFWriteEligibleRecords;
    private Button radioFStopProcessing;
    private Text textFStopProcessing;
    private Section sectionFRecordFilter;
    private Button buttonFRecordFilter;
    private Button checkboxFRecordSuppression;

    // menu
    private MenuItem flfOpenEditorItem = null;
    private MenuItem flfDepCheckItem = null;
    private MenuItem fpfOpenEditorItem = null;
    private MenuItem fpfDepCheckItem = null;
    private MenuItem fueOpenEditorItem = null;
    private MenuItem fueDepCheckItem = null;
        
    // state
    private String selectedFLogicalFile = "";
    private String selectedFPhysicalFile = "";
    private String selectedFUserExit = "";

    private LogicalFileQueryBean prevFLF;
    private ComponentAssociation prevFPF;
    private UserExitRoutineQueryBean prevFUXR;

    private Boolean toggleAccKey_l_Fmt = true;
    private Boolean toggleAccKey_y_Fmt = true;
    private Boolean toggleAccKey_n = true;
    private Boolean toggleAccKey_s = true;
    private Boolean toggleAccKey_t = true;
    private Boolean toggleAccKey_e = true;
    
    protected ViewFormatEditor(ViewMediator mediator, CTabFolder parent, View view) {
        this.mediator = mediator;
        this.parent = parent;
        this.view = view;
    }    
    
    protected void create() {
        createCompositeFormat();
        tabFormat = new CTabItem(parent, SWT.NONE);
        tabFormat.setText("F&ormat Phase");
        tabFormat.setControl(compositeFormat);
    }
    
    private void createCompositeFormat() {
        compositeFormat = mediator.getGUIToolKit().createComposite(parent,SWT.NONE);
        FormLayout layoutComposite = new FormLayout();
        compositeFormat.setLayout(layoutComposite);

        FormData dataComposite = new FormData();
        dataComposite.left = new FormAttachment(0, 0);
        dataComposite.right = new FormAttachment(100, 0);
        dataComposite.top = new FormAttachment(0, 0);
        dataComposite.bottom = new FormAttachment(100, 0);
        compositeFormat.setLayoutData(dataComposite);

        Composite compositeFormatGroupsLeft = mediator.getGUIToolKit().createComposite(
                compositeFormat, SWT.NONE);
        compositeFormatGroupsLeft.setLayout(new FormLayout());

        FormData dataCompositeFormatGroups = new FormData();
        dataCompositeFormatGroups.left = new FormAttachment(0, 0);
        dataCompositeFormatGroups.right = new FormAttachment(100, -400);
        compositeFormatGroupsLeft.setLayoutData(dataCompositeFormatGroups);

        /*
         * section Output File
         */
        sectionFOutputFile = mediator.getGUIToolKit().createSection(
                compositeFormatGroupsLeft, Section.TITLE_BAR, "Output File");
        FormData dataSectionFOutputFile = new FormData();
        dataSectionFOutputFile.top = new FormAttachment(0, 10);
        dataSectionFOutputFile.left = new FormAttachment(0, 10);
        dataSectionFOutputFile.right = new FormAttachment(100, 0);
        sectionFOutputFile.setLayoutData(dataSectionFOutputFile);

        Composite compositeFOutputFile = mediator.getGUIToolKit().createComposite(
                sectionFOutputFile, SWT.NONE);
        compositeFOutputFile.setLayout(new FormLayout());

        sectionFOutputFile.setClient(compositeFOutputFile);

        labelFLogicalFile = mediator.getGUIToolKit().createLabel(compositeFOutputFile,
                SWT.NONE, "&Logical File: ");
        labelFLogicalFile.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_l_Fmt) {
                        e.doit = false;
                        toggleAccKey_l_Fmt = false;
                    } else {
                        toggleAccKey_l_Fmt = true;
                    }
                }
            }
        });

        comboFLogicalFileViewer = mediator.getGUIToolKit().createTableComboForComponents(
                compositeFOutputFile, ComponentType.LogicalFile);
        comboFLogicalFile = comboFLogicalFileViewer.getTableCombo();
        comboFLogicalFile.setData(SAFRLogger.USER, "Format Logical File");        
        addFLFOpenEditorMenu();
        
        labelFPhysicalFile = mediator.getGUIToolKit().createLabel(compositeFOutputFile,
                SWT.NONE, "Ph&ysical File: ");
        labelFPhysicalFile.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_y_Fmt) {
                        e.doit = false;
                        toggleAccKey_y_Fmt = false;
                    } else {
                        toggleAccKey_y_Fmt = true;
                    }
                }
            }
        });

        comboFPhysicalFileViewer = mediator.getGUIToolKit().createTableComboForAssociatedComponents(compositeFOutputFile);
        comboFPhysicalFile = comboFPhysicalFileViewer.getTableCombo();
        comboFPhysicalFile.setData(SAFRLogger.USER, "Format Physical File");      
        addFPFOpenEditorMenu();
        
        FormData dataLabelFLogicalFile = new FormData();
        dataLabelFLogicalFile.top = new FormAttachment(0, 10);
        dataLabelFLogicalFile.left = new FormAttachment(0, 10);
        labelFLogicalFile.setLayoutData(dataLabelFLogicalFile);

        FormData dataComboFLogicalFile = new FormData();
        dataComboFLogicalFile.top = new FormAttachment(0, 10);
        dataComboFLogicalFile.left = new FormAttachment(labelFPhysicalFile, 10);
        dataComboFLogicalFile.right = new FormAttachment(100, -10);
        comboFLogicalFile.setLayoutData(dataComboFLogicalFile);

        comboFLogicalFile.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!comboFLogicalFile.getText().equals(selectedFLogicalFile)) {
                    LogicalFileQueryBean bean = null;
                    if (comboFLogicalFile.getTable().getSelectionCount() > 0) {
                        bean = (LogicalFileQueryBean) comboFLogicalFile
                                .getTable().getSelection()[0].getData();
                        prevFLF = bean;
                    } else {
                        if (prevFLF != null) {
                            comboFLogicalFile.setText(UIUtilities
                                    .getComboString(prevFLF.getName(), prevFLF
                                            .getId()));
                        } else {
                            comboFLogicalFile.setText("");
                        }
                    }

                    mediator.populatePhysicalFileCombo(comboFLogicalFile,
                            comboFPhysicalFile, comboFPhysicalFileViewer);

                    LogicalFileQueryBean logicalFileBean = null;
                    if (comboFLogicalFile.getTable().getSelection().length > 0) {
                        logicalFileBean = (LogicalFileQueryBean) comboFLogicalFile
                                .getTable().getSelection()[0].getData();
                    }
                    if (logicalFileBean == null || logicalFileBean.getId() <= 0) {
                        view.setExtractFileAssociation(null);

                    }

                    mediator.setModified(true);
                    // reset Physical File index as this combo has been reloaded
                    selectedFPhysicalFile = "";
                    comboFPhysicalFile.select(-1);
                    selectedFLogicalFile = comboFLogicalFile.getText();
                }
            }

        });

        FormData dataLabelFPhysicalFile = new FormData();
        dataLabelFPhysicalFile.top = new FormAttachment(comboFLogicalFile, 10);
        dataLabelFPhysicalFile.left = new FormAttachment(0, 10);
        labelFPhysicalFile.setLayoutData(dataLabelFPhysicalFile);

        FormData dataComboFPhysicalFile = new FormData();
        dataComboFPhysicalFile.top = new FormAttachment(comboFLogicalFile, 10);
        dataComboFPhysicalFile.left = new FormAttachment(labelFPhysicalFile, 10);
        dataComboFPhysicalFile.right = new FormAttachment(100, -10);
        dataComboFPhysicalFile.bottom = new FormAttachment(100, -10);
        comboFPhysicalFile.setLayoutData(dataComboFPhysicalFile);
        comboFPhysicalFile.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);

                if (!comboFPhysicalFile.getText().equals(selectedFPhysicalFile)) {
                    ComponentAssociation assoc = null;
                    if (comboFPhysicalFile.getTable().getSelectionCount() > 0) {
                        assoc = (ComponentAssociation) comboFPhysicalFile
                                .getTable().getSelection()[0].getData();
                        prevFPF = assoc;
                    } else {
                        if (prevFPF != null) {
                            comboFPhysicalFile.setText(UIUtilities.getComboString(
                                prevFPF.getAssociatedComponentName(),prevFPF.getAssociatedComponentIdNum()));

                        } else {
                            comboFPhysicalFile.setText("");
                        }
                    }

                    FileAssociation physicalFile = null;
                    if (comboFPhysicalFile.getTable().getSelection().length > 0) {
                        physicalFile = (FileAssociation) comboFPhysicalFile
                                .getTable().getSelection()[0].getData();
                    }
                    if (physicalFile != null) {
                        view.setExtractFileAssociation(physicalFile);
                    }

                    mediator.setModified(true);
                    selectedFPhysicalFile = comboFPhysicalFile.getText();
                }

            }

        });

        /*
         * End of section Output File
         */

        makeFormatUserExitSection(compositeFormatGroupsLeft);
        /*
         * section Format-Phase Record Aggregation
         */
        sectionFRecordAggregation = mediator.getGUIToolKit().createSection(
                compositeFormatGroupsLeft, Section.TITLE_BAR,
                "Format-Phase Record Aggregation (FRA)");

        FormData dataSectionFRecordAggregation = new FormData();
        dataSectionFRecordAggregation.top = new FormAttachment(
                sectionFOutputUserExit, 10);
        dataSectionFRecordAggregation.left = new FormAttachment(0, 10);
        dataSectionFRecordAggregation.right = new FormAttachment(100, 0);
        sectionFRecordAggregation.setLayoutData(dataSectionFRecordAggregation);

        Composite compositeFRecordAggregation = mediator.getGUIToolKit().createComposite(
                sectionFRecordAggregation, SWT.NONE);
        compositeFRecordAggregation.setLayout(new FormLayout());

        sectionFRecordAggregation.setClient(compositeFRecordAggregation);

        radioFDontAggregateRecords = mediator.getGUIToolKit().createRadioButton(
                compositeFRecordAggregation, "Do not aggregate record&s");
        radioFDontAggregateRecords.setData(SAFRLogger.USER, "Format Do not aggregate records");               
        
        radioFDontAggregateRecords.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_s) {
                        e.doit = false;
                        toggleAccKey_s = false;
                    } else {
                        toggleAccKey_s = true;
                    }
                }
            }

        });
        FormData dataRadioFDontAggregateRecords = new FormData();
        dataRadioFDontAggregateRecords.top = new FormAttachment(0, 10);
        dataRadioFDontAggregateRecords.left = new FormAttachment(0, 10);
        radioFDontAggregateRecords
                .setLayoutData(dataRadioFDontAggregateRecords);
        radioFDontAggregateRecords.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (!UIUtilities.isEqual(view.isFormatPhaseRecordAggregationOn(), false)) {
                    mediator.setModified(true);
                    enableRecordAggregationRadioButtons(radioFDontAggregateRecords);
                    view.setFormatPhaseRecordAggregationOn(false);
                    mediator.updateColumnEditorElement(ViewColumnEditor.GROUPAGGREGATIONFUNCTION);
                    mediator.updateColumnEditorElement(ViewColumnEditor.RECORDAGGREGATIONFUNCTION);
                    mediator.refreshExtractRecordAggregationState();
                }
            }

        });

        radioFAggregateRecords = mediator.getGUIToolKit().createRadioButton(
                compositeFRecordAggregation,
                "Aggregate all records with identical sort ke&ys");
        radioFAggregateRecords.setData(SAFRLogger.USER, "Format Aggregate all records with identical sort keys");                     
        radioFAggregateRecords.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_y_Fmt) {
                        e.doit = false;
                        toggleAccKey_y_Fmt = false;
                    } else {
                        toggleAccKey_y_Fmt = true;
                    }
                }
            }
        });
        FormData dataRadioFAggregateRecords = new FormData();
        dataRadioFAggregateRecords.top = new FormAttachment(
                radioFDontAggregateRecords, 10);
        dataRadioFAggregateRecords.left = new FormAttachment(0, 10);
        dataRadioFAggregateRecords.bottom = new FormAttachment(100, -10);
        dataRadioFAggregateRecords.right = new FormAttachment(100, -10);
        radioFAggregateRecords.setLayoutData(dataRadioFAggregateRecords);
        radioFAggregateRecords.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (!UIUtilities.isEqual(view.isFormatPhaseRecordAggregationOn(), true)) {
                    mediator.setModified(true);
                    enableRecordAggregationRadioButtons(radioFAggregateRecords);
                    view.setFormatPhaseRecordAggregationOn(true);
                    mediator.updateColumnEditorElement(ViewColumnEditor.RECORDAGGREGATIONFUNCTION);
                    mediator.updateColumnEditorElement(ViewColumnEditor.GROUPAGGREGATIONFUNCTION);
                    mediator.refreshExtractRecordAggregationState();
                }
            }

        });
        /*
         * End of section Format-Phase Record Aggregation
         */

        /*
         * section Format-Phase Output Limit
         */
        sectionFOutputLimit = mediator.getGUIToolKit().createSection(
                compositeFormatGroupsLeft, Section.TITLE_BAR,
                "Format-Phase Output Limit");
        FormData dataSectionFOutputLimit = new FormData();
        dataSectionFOutputLimit.top = new FormAttachment(
                sectionFRecordAggregation, 10);
        dataSectionFOutputLimit.left = new FormAttachment(0, 10);
        dataSectionFOutputLimit.right = new FormAttachment(100, 0);
        sectionFOutputLimit.setLayoutData(dataSectionFOutputLimit);

        Composite compositeFOutputLimit = mediator.getGUIToolKit().createComposite(
                sectionFOutputLimit, SWT.NONE);
        compositeFOutputLimit.setLayout(new FormLayout());

        sectionFOutputLimit.setClient(compositeFOutputLimit);

        radioFWriteEligibleRecords = mediator.getGUIToolKit().createRadioButton(
                compositeFOutputLimit, "Write all eligi&ble records");
        radioFWriteEligibleRecords.setData(SAFRLogger.USER, "Format Write all eligible records");                     
        
        radioFWriteEligibleRecords.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_t) {
                        e.doit = false;
                        toggleAccKey_t = false;
                    } else {
                        toggleAccKey_t = true;
                    }
                }
            }

        });
        FormData dataRadioFWriteEligibleRecords = new FormData();
        dataRadioFWriteEligibleRecords.top = new FormAttachment(0, 10);
        dataRadioFWriteEligibleRecords.left = new FormAttachment(0, 10);
        radioFWriteEligibleRecords
                .setLayoutData(dataRadioFWriteEligibleRecords);
        radioFWriteEligibleRecords.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (!UIUtilities.isEqual(view.hasFormatPhaseOutputLimit(),false)) {
                    mediator.setModified(true);

                    view.setOutputMaxRecCount(0);
                    view.setFormatPhaseOutputLimit(false);
                    enableOutputLimitRadioButtons(radioFWriteEligibleRecords);
                }
            }
        });

        radioFStopProcessing = mediator.getGUIToolKit().createRadioButton(
                compositeFOutputLimit,
                "Sto&p Format-Phase processing for this view after ");
        radioFStopProcessing.setData(SAFRLogger.USER, "Stop Format-Phase processing");                        
        
        FormData dataRadioFStopProcessing = new FormData();
        dataRadioFStopProcessing.top = new FormAttachment(
                radioFWriteEligibleRecords, 10);
        dataRadioFStopProcessing.left = new FormAttachment(0, 10);
        dataRadioFStopProcessing.bottom = new FormAttachment(100, -10);
        radioFStopProcessing.setLayoutData(dataRadioFStopProcessing);
        radioFStopProcessing.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (!UIUtilities.isEqual(view.hasFormatPhaseOutputLimit(), true)) {
                    mediator.setModified(true);

                    view.setOutputMaxRecCount(UIUtilities
                            .stringToInteger(textFStopProcessing.getText()));
                    view.setFormatPhaseOutputLimit(true);
                    enableOutputLimitRadioButtons(radioFStopProcessing);
                }
            }
        });

        textFStopProcessing = mediator.getGUIToolKit().createIntegerTextBox(
                compositeFOutputLimit, SWT.NONE, false);
        textFStopProcessing.setData(SAFRLogger.USER, "Stop Format-Phase processing records");                     
        
        FormData dataTextFStopProcessing = new FormData();
        dataTextFStopProcessing.top = new FormAttachment(
                radioFWriteEligibleRecords, 10);
        dataTextFStopProcessing.left = new FormAttachment(radioFStopProcessing,
                0);
        dataTextFStopProcessing.bottom = new FormAttachment(100, -10);
        dataTextFStopProcessing.width = 60;
        textFStopProcessing.setLayoutData(dataTextFStopProcessing);
        textFStopProcessing.setTextLimit(ViewEditor.MAX_RECORDS);
        textFStopProcessing.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                mediator.setModified(true);
            }
        });
        textFStopProcessing.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqual(view.getOutputMaxRecCount(),
                        UIUtilities.stringToInteger(textFStopProcessing
                                .getText()))) {
                    view.setOutputMaxRecCount(UIUtilities
                            .stringToInteger(textFStopProcessing.getText()));
                    mediator.setModified(true);
                }

            }

        });

        Label labelFStopProcessing = mediator.getGUIToolKit().createLabel(
                compositeFOutputLimit, SWT.NONE, "record(s) are written");
        FormData dataLabelFStopProcessing = new FormData();
        dataLabelFStopProcessing.top = new FormAttachment(
                radioFWriteEligibleRecords, 10);
        dataLabelFStopProcessing.left = new FormAttachment(textFStopProcessing,
                10);
        dataLabelFStopProcessing.right = new FormAttachment(100, -10);
        dataLabelFStopProcessing.bottom = new FormAttachment(100, -10);
        labelFStopProcessing.setLayoutData(dataLabelFStopProcessing);

        selectFormatRadioButtons();
        /*
         * End of section Format-Phase Output Limit
         */

        Composite compositeFormatGroupsRight = mediator.getGUIToolKit().createComposite(
                compositeFormat, SWT.NONE);
        compositeFormatGroupsRight.setLayout(new FormLayout());
        FormData dataCompositeFormatGroupsRight = new FormData();
        dataCompositeFormatGroupsRight.left = new FormAttachment(
                compositeFormatGroupsLeft, 50);
        dataCompositeFormatGroupsRight.right = new FormAttachment(100, -20);
        compositeFormatGroupsRight
                .setLayoutData(dataCompositeFormatGroupsRight);

        /*
         * section Format-Phase Record Filter
         */
        sectionFRecordFilter = mediator.getGUIToolKit().createSection(
                compositeFormatGroupsRight, Section.TITLE_BAR,
                "Format-Phase Record Filter");
        FormData dataSectionFRecordFilter = new FormData();
        dataSectionFRecordFilter.top = new FormAttachment(0, 10);
        dataSectionFRecordFilter.left = new FormAttachment(0, 10);
        dataSectionFRecordFilter.right = new FormAttachment(100, 0);
        sectionFRecordFilter.setLayoutData(dataSectionFRecordFilter);

        Composite compositeFRecordFilter = mediator.getGUIToolKit().createComposite(
                sectionFRecordFilter, SWT.NONE);
        compositeFRecordFilter.setLayout(new FormLayout());

        sectionFRecordFilter.setClient(compositeFRecordFilter);

        buttonFRecordFilter = mediator.getGUIToolKit().createButton(compositeFRecordFilter,
                SWT.PUSH, "&Create");
        buttonFRecordFilter.setData(SAFRLogger.USER, "Format Record Filter Create");                              
        
        buttonFRecordFilter.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_e) {
                        e.doit = false;
                        toggleAccKey_e = false;
                    }
                }
            }

        });
        FormData dataButtonFCreateRecordFilter = new FormData();
        dataButtonFCreateRecordFilter.top = new FormAttachment(0, 10);
        dataButtonFCreateRecordFilter.left = new FormAttachment(0, 10);
        dataButtonFCreateRecordFilter.width = 75;
        buttonFRecordFilter.setLayoutData(dataButtonFCreateRecordFilter);
        buttonFRecordFilter.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                // Load format record filter.
                LogicTextEditorInput input = new LogicTextEditorInput(view,
                        mediator.getEditor(), mediator.getEditorInput().getEditRights());
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage().openEditor(input,
                                    LogicTextEditor.ID);
                } catch (PartInitException pie) {
                    UIUtilities.handleWEExceptions(pie,"Unexpected error occurred while opening logic text editor.",null);
                }
            }

        });

        checkboxFRecordSuppression = mediator.getGUIToolKit().createCheckBox(
                compositeFRecordFilter, "&Zero-Value Record Suppression");
        checkboxFRecordSuppression.setData(SAFRLogger.USER, "Zero-Value Record Suppression");                                     
        FormData dataCheckboxFRecordSuppression = new FormData();
        dataCheckboxFRecordSuppression.top = new FormAttachment(
                buttonFRecordFilter, 10);
        dataCheckboxFRecordSuppression.left = new FormAttachment(0, 10);
        dataCheckboxFRecordSuppression.bottom = new FormAttachment(100, -10);
        checkboxFRecordSuppression.setLayoutData(dataCheckboxFRecordSuppression);
        checkboxFRecordSuppression.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                view.setSuppressZeroRecords(checkboxFRecordSuppression.getSelection());
                mediator.setModified(true);
            }

        });
        /*
         * End of section Format-Phase Record Filter
         */
    }

	private void makeFormatUserExitSection(Composite compositeFormatGroupsLeft) {
		sectionFOutputUserExit = mediator.getGUIToolKit().createSection(
                compositeFormatGroupsLeft, Section.TITLE_BAR,
                "Format User-Exit Routine");
        FormData dataSectionFOutputUserExit = new FormData();
        dataSectionFOutputUserExit.top = new FormAttachment(sectionFOutputFile,
                10);
        dataSectionFOutputUserExit.left = new FormAttachment(0, 10);
        dataSectionFOutputUserExit.right = new FormAttachment(100, 0);
        sectionFOutputUserExit.setLayoutData(dataSectionFOutputUserExit);

        Composite compositeFOutputUserExit = mediator.getGUIToolKit().createComposite(
                sectionFOutputUserExit, SWT.NONE);
        compositeFOutputUserExit.setLayout(new FormLayout());

        sectionFOutputUserExit.setClient(compositeFOutputUserExit);

        Label labelFUserExitName = mediator.getGUIToolKit().createLabel(
                compositeFOutputUserExit, SWT.NONE, "Na&me: ");
        labelFUserExitName.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_n) {
                        e.doit = false;
                        toggleAccKey_n = false;
                    }
                }
            }

        });

        comboFUserExitNameViewer = mediator.getGUIToolKit().createTableComboForExits(
                compositeFOutputUserExit, ComponentType.UserExitRoutine);
        comboFUserExitName = comboFUserExitNameViewer.getTableCombo();
        comboFUserExitName.setData(SAFRLogger.USER, "Format User-Exit Routine");      
        
        addFUEOpenEditorMenu();
        
        Label labelFUserExitParameters = mediator.getGUIToolKit().createLabel(
                compositeFOutputUserExit, SWT.NONE, "Parame&ters: ");

        textFUserExitParameters = mediator.getGUIToolKit().createTextBox(
                compositeFOutputUserExit, SWT.NONE);
        textFUserExitParameters.setData(SAFRLogger.USER, "Format User-Exit Param");               

        FormData dataLabelFName = new FormData();
        dataLabelFName.top = new FormAttachment(0, 10);
        dataLabelFName.left = new FormAttachment(0, 10);
        labelFUserExitName.setLayoutData(dataLabelFName);

        FormData dataComboFName = new FormData();
        dataComboFName.top = new FormAttachment(0, 10);
        dataComboFName.left = new FormAttachment(labelFUserExitParameters, 10);
        dataComboFName.right = new FormAttachment(100, -10);
        comboFUserExitName.setLayoutData(dataComboFName);
        // CQ 8459. Nikita. 26/08/2010.
        // To resolve issues with enabling/disabling and focus of UXR parameter
        // text-box
        comboFUserExitName.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                enableUserExitRoutineParams(comboFUserExitName,
                        textFUserExitParameters);
            }

        });
        comboFUserExitName.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);

                if (!comboFUserExitName.getText().equals(selectedFUserExit)) {
                    UserExitRoutineQueryBean bean = null;
                    if (comboFUserExitName.getTable().getSelectionCount() > 0) {
                        bean = (UserExitRoutineQueryBean) comboFUserExitName
                                .getTable().getSelection()[0].getData();
                        prevFUXR = bean;
                        disableUserExitRoutineParams(comboFUserExitName,
                                textFUserExitParameters);
                    } else {
                        if (prevFUXR != null) {
                            comboFUserExitName.setText(UIUtilities
                                    .getComboString(prevFUXR.getName(),
                                            prevFUXR.getId()));
                        } else {
                            if (selectedFUserExit.equals("")) {
                                comboFUserExitName.setText("");
                            } else {
                                comboFUserExitName.setText(selectedFUserExit);
                            }
                        }
                    }

                    UserExitRoutineQueryBean userExitRoutineBean = null;
                    if (comboFUserExitName.getTable().getSelection().length > 0) {
                        userExitRoutineBean = (UserExitRoutineQueryBean) comboFUserExitName
                                .getTable().getSelection()[0].getData();
                    }
                    UserExitRoutine userExitRoutine = null;
                    if (userExitRoutineBean != null
                            && userExitRoutineBean.getId() > 0) {
                        try {
                            userExitRoutine = SAFRApplication.getSAFRFactory()
                                    .getUserExitRoutine(
                                            userExitRoutineBean.getId());

                        } catch (SAFRNotFoundException e2) {
                            UIUtilities.handleWEExceptions(e2, "",UIUtilities.titleStringNotFoundException);
                        } catch (SAFRException e1) {
                            UIUtilities.handleWEExceptions(e1,"Error retrieving metadata component.",null);
                        }

                    }
                    view.setFormatExit(userExitRoutine);

                    mediator.setModified(true);
                    selectedFUserExit = comboFUserExitName.getText();

                }
            }

        });

        FormData dataLabelFParameters = new FormData();
        dataLabelFParameters.top = new FormAttachment(comboFUserExitName, 10);
        dataLabelFParameters.left = new FormAttachment(0, 10);
        labelFUserExitParameters.setLayoutData(dataLabelFParameters);

        FormData dataTextFParameters = new FormData();
        dataTextFParameters.top = new FormAttachment(comboFUserExitName, 10);
        dataTextFParameters.left = new FormAttachment(labelFUserExitParameters,
                10);
        dataTextFParameters.right = new FormAttachment(100, -10);
        dataTextFParameters.bottom = new FormAttachment(100, -10);
        textFUserExitParameters.setLayoutData(dataTextFParameters);
        textFUserExitParameters.setTextLimit(ViewEditor.MAX_USER_EXIT_PARAM);
        textFUserExitParameters.setEnabled(false);
        textFUserExitParameters.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                mediator.setModified(true);
            }
        });
        textFUserExitParameters.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqualString(view.getFormatExitParams(),
                    textFUserExitParameters.getText())) {
                    view.setFormatExitParams(textFUserExitParameters.getText());
                    mediator.setModified(true);
                }
            }

        });
	}

    private void addFLFOpenEditorMenu()
    {
        Text text = comboFLogicalFile.getTextControl();
        Menu menu = text.getMenu();
        flfOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        flfOpenEditorItem.setText("Open Editor");
        flfOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                LogicalFileQueryBean bean = (LogicalFileQueryBean)((StructuredSelection) comboFLogicalFileViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.LogicalFile);                        
                }                
            }
        });
        
        flfDepCheckItem = new MenuItem(menu, SWT.PUSH);
        flfDepCheckItem.setText("Dependency Checker");
        flfDepCheckItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                LogicalFileQueryBean bean = (LogicalFileQueryBean)((StructuredSelection) comboFLogicalFileViewer
                    .getSelection()).getFirstElement();
                if (bean != null) {
                    DepCheckOpener.open(bean);
                }                
            }
        });
        
        comboFLogicalFile.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    LogicalFileQueryBean bean = (LogicalFileQueryBean)((StructuredSelection) comboFLogicalFileViewer
                            .getSelection()).getFirstElement();
                    if (bean != null && bean.getId() != 0) {
                        flfOpenEditorItem.setEnabled(true);   
                        flfDepCheckItem.setEnabled(true);
                    }
                    else {
                        flfOpenEditorItem.setEnabled(false);
                        flfDepCheckItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }       

    private void addFPFOpenEditorMenu()
    {
        Text text = comboFPhysicalFile.getTextControl();
        Menu menu = text.getMenu();
        fpfOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        fpfOpenEditorItem.setText("Open Editor");
        fpfOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                FileAssociation ass = (FileAssociation)((StructuredSelection) comboFPhysicalFileViewer
                        .getSelection()).getFirstElement();
                if (ass != null) {   
                    EditorOpener.open(ass.getAssociatedComponentIdNum(), ComponentType.PhysicalFile);                        
                }                
            }
        });
        
        fpfDepCheckItem = new MenuItem(menu, SWT.PUSH);
        fpfDepCheckItem.setText("Dependency Checker");
        fpfDepCheckItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                FileAssociation ass = (FileAssociation)((StructuredSelection) comboFPhysicalFileViewer
                    .getSelection()).getFirstElement();
                if (ass != null) {
                    DepCheckOpener.open(new PhysicalFileQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(),
                        ass.getAssociatedComponentIdNum(), 
                        null, null, null, null, null, null, null, null, null, null, null, null));
                }                
            }
        });
        
        comboFPhysicalFile.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    FileAssociation ass = (FileAssociation)((StructuredSelection) comboFPhysicalFileViewer
                            .getSelection()).getFirstElement();
                    if (ass != null) {
                        fpfOpenEditorItem.setEnabled(true); 
                        fpfDepCheckItem.setEnabled(true);
                    }
                    else {
                        fpfOpenEditorItem.setEnabled(false);
                        fpfDepCheckItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }       
    

    private void addFUEOpenEditorMenu()
    {
        Text text = comboFUserExitName.getTextControl();
        Menu menu = text.getMenu();
        fueOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        fueOpenEditorItem.setText("Open Editor");
        fueOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean)((StructuredSelection) comboFUserExitNameViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.UserExitRoutine);                        
                }                
            }
        });
        
        fueDepCheckItem = new MenuItem(menu, SWT.PUSH);
        fueDepCheckItem.setText("Dependency Checker");
        fueDepCheckItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean)((StructuredSelection) comboFUserExitNameViewer
                    .getSelection()).getFirstElement();
                if (bean != null) {
                    DepCheckOpener.open(bean);
                }                
            }
        });
        
        comboFUserExitName.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean)((StructuredSelection) comboFUserExitNameViewer
                            .getSelection()).getFirstElement();
                    if (bean != null && bean.getId() != 0) {   
                        fueOpenEditorItem.setEnabled(true); 
                        fueDepCheckItem.setEnabled(true);
                    }
                    else {
                        fueOpenEditorItem.setEnabled(false);
                        fueDepCheckItem.setEnabled(false);
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
        // CQ 8459. Nikita. 26/08/2010.
        // CQ 9097. Nikita. 16/02/2011.
        // To resolve issues with enabling/disabling and focus of UXR parameter
        // text-box

        EnvironmentalQueryBean uxrBean = (EnvironmentalQueryBean) combo
                .getTable().getSelection()[0].getData();
        if (uxrBean != null && uxrBean.getId() <= 0) {
            textParam.setText("");
            textParam.setEnabled(false);
        }
    }

    /**
     * This method sets the default selection for the 'Format-Phase Record
     * Aggregation' and 'Format-Phase Output Limit' radio buttons
     */
    private void selectFormatRadioButtons() {
        enableRecordAggregationRadioButtons(radioFDontAggregateRecords);
        enableOutputLimitRadioButtons(radioFWriteEligibleRecords);
    }

    /**
     * This method enables and disables, as also selects and de-selects the
     * controls pertaining to the section 'Record Aggregation' in both, Extract
     * and Format Phases, depending on the radio button passed as parameter.
     * 
     * @param radioButton
     */
    private void enableRecordAggregationRadioButtons(Button radioButton) {
        if (radioButton == radioFDontAggregateRecords) {
            radioFDontAggregateRecords.setSelection(true);
            radioFAggregateRecords.setSelection(false);

        } else if (radioButton == radioFAggregateRecords) {
            radioFAggregateRecords.setSelection(true);
            radioFDontAggregateRecords.setSelection(false);
        }
    }

    /**
     * This method enables and disables, as also selects and de-selects the
     * controls pertaining to the section 'Output Limit' in both, Extract and
     * Format Phases, depending on the radio button passed as parameter.
     * 
     * @param radioButton
     */
    private void enableOutputLimitRadioButtons(Button radioButton) {
        if (radioButton == radioFWriteEligibleRecords) {
            radioFWriteEligibleRecords.setSelection(true);
            radioFStopProcessing.setSelection(false);
            textFStopProcessing.setEnabled(false);
            if (view.getOutputMaxRecCount() != null) {
                textFStopProcessing.setText(Integer.toString(view
                        .getOutputMaxRecCount()));
            }

        } else if (radioButton == radioFStopProcessing) {
            radioFStopProcessing.setSelection(true);
            textFStopProcessing.setEnabled(!mediator.getGUIToolKit().isReadOnly());
            if (view.getOutputMaxRecCount() != null) {
                textFStopProcessing.setText(Integer.toString(view
                        .getOutputMaxRecCount()));
            }
            radioFWriteEligibleRecords.setSelection(false);

        }
    }

    /**
     * Loads the controls pertaining to the Format Phase with values from the
     * model object.
     * 
     * @throws SAFRException
     */
    protected void loadFormatPhase() throws SAFRException {
        // for format.
        mediator.populateViewCombos(ComponentType.LogicalFile, comboFLogicalFile,
                comboFLogicalFileViewer, true);
        mediator.populateViewCombos(ComponentType.FormatUserExitRoutine,
                comboFUserExitName, comboFUserExitNameViewer, true);

        FileAssociation fileAssociation = view.getExtractFileAssociation();
        if (fileAssociation != null) {
            selectComponentInCombo(fileAssociation
                    .getAssociatingComponentName(), fileAssociation
                    .getAssociatingComponentId(), comboFLogicalFile);
            selectedFLogicalFile = comboFLogicalFile.getText();

            mediator.populatePhysicalFileCombo(comboFLogicalFile, comboFPhysicalFile,
                    comboFPhysicalFileViewer);
            selectPhysicalFileInCombo(comboFPhysicalFile);
            selectedFPhysicalFile = comboFPhysicalFile.getText();
        } else {
            comboFLogicalFile.getTable().deselectAll();
            comboFPhysicalFile.getTable().deselectAll();
        }

        if(view.hasFormatExit()) {
	        try {
	            UIUtilities.selectComponentInCombo(comboFUserExitName, view.getFormatExit());
	        } catch (SAFRNotFoundException snfe) {
	            MessageDialog
	                    .openWarning(
	                            Display.getCurrent().getActiveShell(),
	                            "Warning",
	                            "The User Exit Routine with id ["
	                                    + snfe.getComponentId()
	                                    + "] referred to by this View in the format phase does not exist. Please select another User Exit Routine if required.");
	            comboFUserExitName.setText("[" + snfe.getComponentId() + "]");
	        }
	        selectedFUserExit = comboFUserExitName.getText();
	
	        if (!comboFUserExitName.getText().equals("")) {
	            UIUtilities.checkNullText(textFUserExitParameters, view
	                    .getFormatExitParams());
	            enableUserExitRoutineParams(comboFUserExitName,
	                    textFUserExitParameters);
	        } else {
	            disableUserExitRoutineParams(comboFUserExitName,
	                    textFUserExitParameters);
	        }
        }

        if (view.isFormatPhaseRecordAggregationOn() != null) {
            if (view.isFormatPhaseRecordAggregationOn()) {
                radioFDontAggregateRecords.setSelection(false);
                radioFAggregateRecords.setSelection(true);
            } else {
                radioFDontAggregateRecords.setSelection(true);
                radioFAggregateRecords.setSelection(false);
            }
        }

        if (view.getOutputMaxRecCount() != null) {
            if (view.getOutputMaxRecCount() > 0) {
                radioFWriteEligibleRecords.setSelection(false);
                radioFStopProcessing.setSelection(true);
                textFStopProcessing.setEnabled(!mediator.getGUIToolKit().isReadOnly());
                UIUtilities.checkNullText(textFStopProcessing, Integer
                        .toString(view.getOutputMaxRecCount()));
            } else {
                radioFWriteEligibleRecords.setSelection(true);
                radioFStopProcessing.setSelection(false);
                textFStopProcessing.setEnabled(false);
                textFStopProcessing.setText("");
            }
        }

        updateFRFButtonState();

        if (view.isSuppressZeroRecords() != null) {
            if (view.isSuppressZeroRecords()) {
                checkboxFRecordSuppression.setSelection(true);
            } else {
                checkboxFRecordSuppression.setSelection(false);
            }
        }
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

    protected void updateFRFButtonState() {
        if (mediator.isViewPropVisible()) {
            buttonFRecordFilter.setText("E&dit");
            buttonFRecordFilter.setEnabled(true);
        }
    }
    
    protected void enableFormatOutputFile(boolean enable) {
        if (enable) {
            enable = !mediator.getGUIToolKit().isReadOnly();
        }
        labelFLogicalFile.setEnabled(enable);
        comboFLogicalFile.setEnabled(enable);
        labelFPhysicalFile.setEnabled(enable);
        comboFPhysicalFile.setEnabled(enable);
        if (!enable) {
            comboFLogicalFile.getTable().deselectAll();
            comboFPhysicalFile.getTable().deselectAll();
            mediator.deselectExtractLFPF();
        }
    }

    protected void refreshControlsFormat() {
        if (view.isFormatPhaseInUse()) {
            if (tabFormat.isDisposed()) {
                create();
            }
        } else {
            if (!tabFormat.isDisposed()) {
                tabFormat.dispose();
            }
            mediator.enableExtractOutputFile(true);
        }

        if (comboFLogicalFile.getTable().getSelection().length > 0) {
            prevFLF = (LogicalFileQueryBean) comboFLogicalFile.getTable()
                    .getSelection()[0].getData();
        } else {
            prevFLF = null;
        }
        if (comboFPhysicalFile.getTable().getSelection().length > 0) {
            prevFPF = (ComponentAssociation) comboFPhysicalFile.getTable()
                    .getSelection()[0].getData();
        } else {
            prevFPF = null;
        }
        if (comboFUserExitName.getTable().getSelection().length > 0) {
            prevFUXR = (UserExitRoutineQueryBean) comboFUserExitName
                    .getTable().getSelection()[0].getData();
        } else {
            // CQ 8589 Kanchan Rauthan 15/09/2010 Showing that user exit
            // routine
            // which is not of required type(bad data).
            try {
                if (view.getFormatExit() != null
                        && !(comboFUserExitName.getText().equals(""))) {
                    UserExitRoutineQueryBean uxer = new UserExitRoutineQueryBean(
                        view.getEnvironment().getId(), view
                            .getFormatExit().getId(), view
                            .getFormatExit().getName(), 
                        null, null, null, null, null, null, null, null);
                    prevFUXR = uxer;
                } else {
                    prevFUXR = null;
                }
            } catch (SAFRNotFoundException snfe) {
                logger.log(Level.SEVERE, "", snfe);
                // doesn't need to be handled as the SAFRNotFoundException
                // is already handled in the loadFormatPhase method and in
                // the validate method of the model
            }
        }        
    }

    protected CTabItem getFormatTab() {
        return tabFormat;
    }

    protected void hideFormatPhase() {
        if (!tabFormat.isDisposed()) {
            tabFormat.dispose();
            mediator.closeRelatedLogicTextEditors(view);
        }        
    }

    protected Control getControlFromProperty(Object property) {
        if (property == Property.FORMAT_USER_EXIT_ROTUINE) {
            return comboFUserExitName;
        } else {
            return null;
        }
    }

    
}
