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
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ControlRecordQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.View.Property;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.utilities.CommentsTraverseListener;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationContentProvider;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationLabelProvider;
import com.ibm.safr.we.ui.views.metadatatable.ViewFolderTableLabelProvider;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ViewGeneralEditor {

    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.view.ViewGeneralEditor");

    class ViewFolderLabelProvider extends ViewFolderTableLabelProvider implements IColorProvider {
    
        public Color getBackground(Object element) {
            return null;
        }
        
        public Color getForeground(Object element) {
            return null;
        }
        
        public String getColumnText(Object element, int columnIndex) {
        
            switch (columnIndex) {
            case 2:
                ViewFolderQueryBean bean = (ViewFolderQueryBean) element;
                return UIUtilities.getComboString(bean.getName(), bean.getId());
            default:
                return super.getColumnText(element, columnIndex);
            }
        }
    
    }

    
    // member variables
    private ViewMediator mediator;
    private CTabFolder parent;
    private View view;

    // internal widgets
    private CTabItem tabGeneral;
    private Composite compositeGeneral;
    
    // section identity
    private Section sectionIdentity;
    private Text textID;
    private Text textName;
    
    // section output format
    private Section sectionOutputFormat;
    private Button extractFixedWidth;
    private Button extractSourceRecord;
    private Button formatFixedWidth;
    private Button formatDelimitedFields;
    private Button formatReport;
    
    // view folder section
    private Section sectionViewFolder;
    private TableViewer tableViewerViewFolders;
    private Table tableViewFolders;
    
    // general properties
    private Section sectionGeneralProperties;    
    private TableComboViewer comboControlRecordViewer;
    private TableCombo comboControlRecord;
    private Text textComments;
    private Label labelCreatedValue;
    private Label labelModifiedValue;
    private Label labelActivatedValue;
    private String defaultModStr = "-";
    private Section sectionOutputLR;
    private Button checkboxCreateView;
    private Label labelOutputLR;
    private TableComboViewer comboOutputLRViewer;
    private TableCombo comboOutputLR;

    // state
    private EnvironmentalQueryBean prevLR;
    
    private String selectedControlRecord = "";
    private String selectedLogicalRecord = "";
    
    private Boolean toggleAccKey_l = true;
    private Boolean toggleAccKey_m = true;
    private Boolean toggleAccKey_n = true;
    
    protected ViewGeneralEditor(ViewMediator mediator, CTabFolder parent, View view) {
        this.mediator = mediator;
        this.parent = parent;
        this.view = view;
    }
    
    protected void create() {
        createCompositeGeneral();
        tabGeneral = new CTabItem(parent, SWT.NONE);
        tabGeneral.setText("&General");
        tabGeneral.setControl(compositeGeneral);
    }
    
    private void createCompositeGeneral() {
        compositeGeneral = mediator.getGUIToolKit().createComposite(parent, SWT.NONE);
        FormLayout layoutComposite = new FormLayout();
        compositeGeneral.setLayout(layoutComposite);

        FormData dataComposite = new FormData();
        dataComposite.left = new FormAttachment(0, 0);
        dataComposite.right = new FormAttachment(100, 0);
        dataComposite.top = new FormAttachment(0, 0);
        dataComposite.bottom = new FormAttachment(100, 0);
        compositeGeneral.setLayoutData(dataComposite);

        createSectionIdentity();
        createSectionOutput();
        createSectionViewFolder();
        createSectionGeneral();
        createSectionOutputLR();
    }
    
    private void createSectionIdentity() {
        sectionIdentity = mediator.getGUIToolKit().createSection(compositeGeneral,
            Section.TITLE_BAR, "");
        FormData dataGroupIdentity = new FormData();
        dataGroupIdentity.top = new FormAttachment(0, 10);
        dataGroupIdentity.right = new FormAttachment(100, 0);
        dataGroupIdentity.left = new FormAttachment(0, 0);
        sectionIdentity.setLayoutData(dataGroupIdentity);
    
        Composite compositeIdentity = mediator.getGUIToolKit().createComposite(sectionIdentity, SWT.NONE);
        compositeIdentity.setLayout(new FormLayout());
    
        sectionIdentity.setClient(compositeIdentity);    
        
        Label labelId = mediator.getGUIToolKit().createLabel(compositeIdentity,
            SWT.NONE, "ID:");
        FormData dataLabelId = new FormData();
        dataLabelId.top = new FormAttachment(0, 10);
        dataLabelId.left = new FormAttachment(0, 10);
        labelId.setLayoutData(dataLabelId);
    
        textID = mediator.getGUIToolKit().createTextBox(compositeIdentity, SWT.NONE);
        textID.setEnabled(false);
        FormData dataTextId = new FormData();
        dataTextId.top = new FormAttachment(0, 10);
        dataTextId.left = new FormAttachment(labelId, 10);
        dataTextId.width = 150;
        textID.setLayoutData(dataTextId);
    
        Label labelName = mediator.getGUIToolKit().createLabel(compositeIdentity,
                SWT.NONE, "&Name:");
        labelName.addTraverseListener(new TraverseListener() {
    
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_n) {
                        e.doit = false;
                        toggleAccKey_n = false;
                    } else {
                        toggleAccKey_n = true;
                    }
                }
            }
    
        });
        FormData dataLabelName = new FormData();
        dataLabelName.top = new FormAttachment(0, 10);
        dataLabelName.left = new FormAttachment(textID, 100);
        labelName.setLayoutData(dataLabelName);
    
        textName = mediator.getGUIToolKit().createNameTextBox(compositeIdentity, SWT.NONE);
        textName.setData(SAFRLogger.USER, "Name");                
        FormData dataTextName = new FormData();
        dataTextName.top = new FormAttachment(0, 10);
        dataTextName.left = new FormAttachment(labelName, 10);
        dataTextName.width = 300;
        textName.setLayoutData(dataTextName);
        textName.setTextLimit(UIUtilities.MAXNAMECHAR);
        textName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!UIUtilities.isEqualString(view.getName(), textName.getText())) {
                    view.setName(textName.getText());
                    mediator.setModified(true);
                }
            }
            
        });
        
        UIUtilities.checkNullText(textName, view.getName());
        textName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqualString(view.getName(), textName.getText())) {
                    view.setName(textName.getText());
                    mediator.setModified(true);
                }
            }
        });
            
    }

    private void createSectionOutput() {
        sectionOutputFormat = mediator.getGUIToolKit().createSection(compositeGeneral,
                Section.TITLE_BAR, "Default Output Format");
        FormData dataGroupOutputFormat = new FormData();
        dataGroupOutputFormat.top = new FormAttachment(sectionIdentity, 20);
        dataGroupOutputFormat.bottom = new FormAttachment(100, 0);
        sectionOutputFormat.setLayoutData(dataGroupOutputFormat);

        Composite compositeOutputFormat = mediator.getGUIToolKit().createComposite(
                sectionOutputFormat, SWT.NONE);
        compositeOutputFormat.setLayout(new FormLayout());

        sectionOutputFormat.setClient(compositeOutputFormat);

        Label extractPhase = mediator.getGUIToolKit().createLabel(compositeOutputFormat, SWT.NONE, "Extract-Phase Output");
        FormData extractPhaseLayout = new FormData();
        extractPhaseLayout.top = new FormAttachment(0, 5);
        extractPhase.setLayoutData(extractPhaseLayout);

        extractFixedWidth = mediator.getGUIToolKit().createRadioButton(compositeOutputFormat,
            "Fixed-Width Fields");
        extractFixedWidth.setData(SAFRLogger.USER,"Fixed-Width Fields");      
        extractFixedWidth.setEnabled(!mediator.getGUIToolKit().isReadOnly());
        FormData extractFixedWidthLayout = new FormData();
        extractFixedWidthLayout.top = new FormAttachment(extractPhase, 5);
        extractFixedWidthLayout.left = new FormAttachment(0, 20);
        extractFixedWidth.setLayoutData(extractFixedWidthLayout);
        extractFixedWidth.addSelectionListener(new SelectionAdapter() {
    
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
    
                if (!UIUtilities.isEqual(view.getOutputFormat(),OutputFormat.Extract_Fixed_Width_Fields)) {
                    mediator.setModified(true);    
                    view.setOutputFormat(OutputFormat.Extract_Fixed_Width_Fields);
                    
                    refreshExtractFixed();     
                    
                    // update write statement
                    for (ViewSource viewSrc : view.getViewSources().getActiveItems()) {
                        WriteStatementGenerator generator = new WriteStatementGenerator(view, viewSrc);
                        generator.generateWriteStatement();
                    }                    
                }
            }

        });
        
        extractSourceRecord = mediator.getGUIToolKit().createRadioButton(compositeOutputFormat,
            "Source-Record Layout");
        extractSourceRecord.setData(SAFRLogger.USER, "Source-Record Layout");
        extractSourceRecord.setEnabled(!mediator.getGUIToolKit().isReadOnly());
        FormData extractSourceRecordLayout = new FormData();
        extractSourceRecordLayout.top = new FormAttachment(extractFixedWidth, 5);
        extractSourceRecordLayout.left = new FormAttachment(0, 20);
        extractSourceRecord.setLayoutData(extractSourceRecordLayout);
        extractSourceRecord.addSelectionListener(new SelectionAdapter() {
    
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (!UIUtilities.isEqual(view.getOutputFormat(), OutputFormat.Extract_Source_Record_Layout)) {
                    mediator.setModified(true);
                    view.setOutputFormat(OutputFormat.Extract_Source_Record_Layout);

                    refreshExtractSource();
                    
                    // update write statement
                    for (ViewSource viewSrc : view.getViewSources().getActiveItems()) {
                        WriteStatementGenerator generator = new WriteStatementGenerator(view, viewSrc);
                        generator.generateWriteStatement();
                    } 
                }
            }

        });        
        
        Label formatPhase = mediator.getGUIToolKit().createLabel(compositeOutputFormat, SWT.NONE, "Format-Phase Output");
        FormData formatPhaseLayout = new FormData();
        formatPhaseLayout.top = new FormAttachment(extractSourceRecord, 5);
        formatPhase.setLayoutData(formatPhaseLayout);
        
        formatFixedWidth = mediator.getGUIToolKit().createRadioButton(compositeOutputFormat,
            "Fixed-Width Fields");
        formatFixedWidth.setData(SAFRLogger.USER,"Fixed-Width Fields");               
        FormData formatFixedWidthLayout = new FormData();
        formatFixedWidthLayout.top = new FormAttachment(formatPhase, 5);
        formatFixedWidthLayout.left = new FormAttachment(0, 20);
        formatFixedWidth.setLayoutData(formatFixedWidthLayout);
        formatFixedWidth.addSelectionListener(new SelectionAdapter() {
    
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (!UIUtilities.isEqual(view.getOutputFormat(), OutputFormat.Format_Fixed_Width_Fields)) {
                    mediator.setModified(true);    
                    view.setOutputFormat(OutputFormat.Format_Fixed_Width_Fields);
                    
                    refreshFormatFixed();
                    
                    // update write statement
                    for (ViewSource viewSrc : view.getViewSources().getActiveItems()) {
                        WriteStatementGenerator generator = new WriteStatementGenerator(view, viewSrc);
                        generator.generateWriteStatement();
                    }   
                                        
                }
            }

        });

        formatDelimitedFields = mediator.getGUIToolKit().createRadioButton(compositeOutputFormat,
            "Delimited Fields");       
        formatDelimitedFields.setData(SAFRLogger.USER,"Delimited Fields");                     
        formatDelimitedFields.setEnabled(!mediator.getGUIToolKit().isReadOnly());
        FormData formatDelimitedFieldsLayout = new FormData();
        formatDelimitedFieldsLayout.top = new FormAttachment(formatFixedWidth, 5);
        formatDelimitedFieldsLayout.left = new FormAttachment(0, 20);
        formatDelimitedFields.setLayoutData(formatDelimitedFieldsLayout);
        formatDelimitedFields.addSelectionListener(new SelectionAdapter() {
    
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
    
                if (!UIUtilities.isEqual(view.getOutputFormat(), OutputFormat.Format_Delimited_Fields)) {
                    mediator.setModified(true);
                    view.setOutputFormat(OutputFormat.Format_Delimited_Fields);
        
                    refreshFormatDelimited();
                    
                    // update write statement
                    for (ViewSource viewSrc : view.getViewSources().getActiveItems()) {
                        WriteStatementGenerator generator = new WriteStatementGenerator(view, viewSrc);
                        generator.generateWriteStatement();
                    }
                    
                }
            }

        });
    
    
        formatReport = mediator.getGUIToolKit().createRadioButton(
                compositeOutputFormat, "Report");
        formatReport.setData(SAFRLogger.USER, "Report");
        FormData formatReportLayout = new FormData();
        formatReportLayout.top = new FormAttachment(formatDelimitedFields, 5);
        formatReportLayout.left = new FormAttachment(0, 20);
        formatReport.setLayoutData(formatReportLayout);
        formatReport.addSelectionListener(new SelectionAdapter() {
    
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
    
                comboOutputLR.setEnabled(true);
                if (!UIUtilities.isEqual(view.getOutputFormat(),OutputFormat.Format_Report)) {
                    mediator.setModified(true);
                    view.setOutputFormat(OutputFormat.Format_Report);
                    
                    refreshFormatReport(); 
                    
                    // update write statement
                    for (ViewSource viewSrc : view.getViewSources().getActiveItems()) {
                        WriteStatementGenerator generator = new WriteStatementGenerator(view, viewSrc);
                        generator.generateWriteStatement();
                    }                      
                }
    
            }
        });                    
    }
    

    private void createSectionViewFolder() {
        sectionViewFolder = mediator.getGUIToolKit().createSection(compositeGeneral, Section.TITLE_BAR, "View Folders");
        FormData dataSectionViewFolder = new FormData();
        dataSectionViewFolder.left = new FormAttachment(sectionOutputFormat, 10);
        dataSectionViewFolder.top = new FormAttachment(sectionIdentity, 20);
        sectionViewFolder.setLayoutData(dataSectionViewFolder);
        
        Composite compositeViewFolder = mediator.getGUIToolKit().createComposite(
            sectionViewFolder, SWT.NONE);
        FormLayout layoutViewFolder = new FormLayout();
        layoutViewFolder.marginTop = 5;
        layoutViewFolder.marginBottom = 5;
        layoutViewFolder.marginLeft = 5;
        layoutViewFolder.marginRight = 5;
        compositeViewFolder.setLayout(layoutViewFolder);
        sectionViewFolder.setClient(compositeViewFolder);
        
        tableViewerViewFolders = mediator.getGUIToolKit().createTableViewer(
            compositeViewFolder, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION, false);
        tableViewFolders = tableViewerViewFolders.getTable();
        FormData dataTableLogicalRecord = new FormData();
        dataTableLogicalRecord.left = new FormAttachment(0, 0);
        dataTableLogicalRecord.top = new FormAttachment(0, 10);
        dataTableLogicalRecord.bottom = new FormAttachment(100, 0);
        dataTableLogicalRecord.height = 120;
        tableViewFolders.setLayoutData(dataTableLogicalRecord);
        mediator.getGUIToolKit().createToolkit().adapt(tableViewFolders, false, false);
    
        UIUtilities.prepareTableViewerForShortList(tableViewerViewFolders,ComponentType.ViewFolder);
        tableViewerViewFolders.setContentProvider(new ComponentAssociationContentProvider());
        tableViewerViewFolders.setLabelProvider(new ComponentAssociationLabelProvider());
        tableViewerViewFolders.setInput(view.getViewFolderAssociations());
        
    }

    private void createSectionGeneral() {
        sectionGeneralProperties = mediator.getGUIToolKit().createSection(compositeGeneral, Section.TITLE_BAR, "");
        FormData dataSectionGeneralProperties = new FormData();
        dataSectionGeneralProperties.left = new FormAttachment(sectionViewFolder, 10);
        dataSectionGeneralProperties.top = new FormAttachment(sectionIdentity, 20);
        sectionGeneralProperties.setLayoutData(dataSectionGeneralProperties);

        Composite compositeGeneralProperties = mediator.getGUIToolKit().createComposite(
                sectionGeneralProperties, SWT.NONE);
        compositeGeneralProperties.setLayout(new FormLayout());

        sectionGeneralProperties.setClient(compositeGeneralProperties);
        
        Label labelControlRecord = mediator.getGUIToolKit().createLabel(
                compositeGeneralProperties, SWT.NONE, "Co&ntrol Record:");
        labelControlRecord.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_n) {
                        e.doit = false;
                        toggleAccKey_n = false;
                    }
                }
            }

        });

        comboControlRecordViewer = mediator.getGUIToolKit().createTableComboForComponents(
                compositeGeneralProperties, ComponentType.ControlRecord);
        comboControlRecord = comboControlRecordViewer.getTableCombo();
        comboControlRecord.setData(SAFRLogger.USER, "Control Record");                
        addControlRecOpenEditorMenu();
        
        Label labelComments = mediator.getGUIToolKit().createLabel(
                compositeGeneralProperties, SWT.NONE, "Co&mments:");
        labelComments.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_m) {
                        e.doit = false;
                        toggleAccKey_m = false;
                    }
                }
            }

        });

        textComments = mediator.getGUIToolKit()
                .createCommentsTextBox(compositeGeneralProperties);
        textComments.setData(SAFRLogger.USER, "Comments");                

        Label labelCreated = mediator.getGUIToolKit().createLabel(
                compositeGeneralProperties, SWT.NONE, "Created:");
        labelCreatedValue = mediator.getGUIToolKit().createLabel(compositeGeneralProperties,
                SWT.NONE, defaultModStr);

        FormData dataLabelCreated = new FormData();
        dataLabelCreated.top = new FormAttachment(textComments, 10);
        dataLabelCreated.left = new FormAttachment(0, 10);
        labelCreated.setLayoutData(dataLabelCreated);

        FormData dataLabelCreatedValue = new FormData();
        dataLabelCreatedValue.top = new FormAttachment(textComments, 10);
        dataLabelCreatedValue.left = new FormAttachment(labelControlRecord, 10);
        dataLabelCreatedValue.right = new FormAttachment(100, -10);
        labelCreatedValue.setLayoutData(dataLabelCreatedValue);
        
        Label labelModified = mediator.getGUIToolKit().createLabel(
                compositeGeneralProperties, SWT.NONE, "Last Modified:");
        labelModifiedValue = mediator.getGUIToolKit().createLabel(
                compositeGeneralProperties, SWT.NONE, defaultModStr);

        FormData dataLabelModified = new FormData();
        dataLabelModified.top = new FormAttachment(labelCreated, 10);
        dataLabelModified.left = new FormAttachment(0, 10);
        labelModified.setLayoutData(dataLabelModified);

        FormData dataLabelModifiedValue = new FormData();
        dataLabelModifiedValue.top = new FormAttachment(labelCreated, 10);
        dataLabelModifiedValue.left = new FormAttachment(labelControlRecord, 10);
        dataLabelModifiedValue.right = new FormAttachment(100, -10);
        labelModifiedValue.setLayoutData(dataLabelModifiedValue);

        Label labelActivated = mediator.getGUIToolKit().createLabel(
            compositeGeneralProperties, SWT.NONE, "Last De/active:");
        labelActivatedValue = mediator.getGUIToolKit().createLabel(
            compositeGeneralProperties, SWT.NONE, defaultModStr);

        FormData dataLabelActivated = new FormData();
        dataLabelActivated.top = new FormAttachment(labelModified, 10);
        dataLabelActivated.left = new FormAttachment(0, 10);
        labelActivated.setLayoutData(dataLabelActivated);

        FormData dataLabelActivatedValue = new FormData();
        dataLabelActivatedValue.top = new FormAttachment(labelModified, 10);
        dataLabelActivatedValue.left = new FormAttachment(labelControlRecord, 10);
        dataLabelActivatedValue.right = new FormAttachment(100, -10);
        labelActivatedValue.setLayoutData(dataLabelActivatedValue);
        
        FormData dataLabelControlRecord = new FormData();
        dataLabelControlRecord.left = new FormAttachment(0, 10);
        dataLabelControlRecord.top = new FormAttachment(0, 10);
        labelControlRecord.setLayoutData(dataLabelControlRecord);

        FormData dataComboControlRecord = new FormData();
        dataComboControlRecord.left = new FormAttachment(labelControlRecord, 10);
        dataComboControlRecord.top = new FormAttachment(0, 10);
        dataComboControlRecord.right = new FormAttachment(100, -10);
        comboControlRecord.setLayoutData(dataComboControlRecord);
        comboControlRecord.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);

                if (!(comboControlRecord.getText()
                        .equals(selectedControlRecord))) {
                    ControlRecordQueryBean controlRecordBean = null;
                    if (comboControlRecord.getTable().getSelection().length > 0) {
                        controlRecordBean = (ControlRecordQueryBean) comboControlRecord
                                .getTable().getSelection()[0].getData();
                    }
                    if (controlRecordBean != null) {
                        try {
                            ControlRecord controlRecord = SAFRApplication
                                    .getSAFRFactory().getControlRecord(
                                            controlRecordBean.getId());

                            if (controlRecord != null) {
                                view.setControlRecord(controlRecord);
                            }
                        } catch (SAFRNotFoundException e2) {
                            UIUtilities.handleWEExceptions(e2, "",
                                    UIUtilities.titleStringNotFoundException);
                        } catch (SAFRException e1) {
                            UIUtilities.handleWEExceptions(e1,
                                    "Error retrieving metadata component.",
                                    null);
                        }

                        mediator.setModified(true);
                        selectedControlRecord = comboControlRecord.getText();
                    }
                }
            }

        });

        FormData dataLabelComments = new FormData();
        dataLabelComments.left = new FormAttachment(0, 10);
        dataLabelComments.top = new FormAttachment(comboControlRecord, 10);
        labelComments.setLayoutData(dataLabelComments);

        FormData dataTextComments = new FormData();
        dataTextComments.left = new FormAttachment(labelControlRecord, 10);
        dataTextComments.top = new FormAttachment(comboControlRecord, 10);
        dataTextComments.width = 300;
        dataTextComments.height = 30;
        dataTextComments.right = new FormAttachment(100, -10);
        textComments.setLayoutData(dataTextComments);
        textComments.setTextLimit(UIUtilities.MAXCOMMENTCHAR);
        CommentsTraverseListener traverseListener = new CommentsTraverseListener();
        textComments.addTraverseListener(traverseListener);
        textComments.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!UIUtilities.isEqualString(view.getComment(), textComments.getText())) {
                    mediator.setModified(true);
                }
            }
        });
        
        textComments.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqualString(view.getComment(), textComments.getText())) {
                    view.setComment(textComments.getText());
                    mediator.setModified(true);
                }
            }
        });

    }

    private void createSectionOutputLR() {
        // The section sectionOutputLR is visible only for a new View, not while
        // editing an existing one.
        sectionOutputLR = mediator.getGUIToolKit().createSection(compositeGeneral,Section.TITLE_BAR, "");
        FormData dataSectionOutputLR = new FormData();
        dataSectionOutputLR.left = new FormAttachment(sectionOutputFormat, 10);
        dataSectionOutputLR.top = new FormAttachment(sectionGeneralProperties,10);
        dataSectionOutputLR.bottom = new FormAttachment(100, 0);
        dataSectionOutputLR.right = new FormAttachment(sectionGeneralProperties, 10);
        sectionOutputLR.setLayoutData(dataSectionOutputLR);

        Composite compositeOutputLR = mediator.getGUIToolKit().createComposite(sectionOutputLR, SWT.NONE);
        compositeOutputLR.setLayout(new FormLayout());

        sectionOutputLR.setClient(compositeOutputLR);
        sectionOutputLR.setText("Create View based on Output Logical Record");

        labelOutputLR = mediator.getGUIToolKit().createLabel(compositeOutputLR, SWT.NONE,
                "Output &Logical Record:");
        labelOutputLR.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_l) {
                        e.doit = false;
                        toggleAccKey_l = false;
                    }
                }
            }

        });
        FormData dataLabelOutputLR = new FormData();
        dataLabelOutputLR.top = new FormAttachment(checkboxCreateView, 10);
        dataLabelOutputLR.left = new FormAttachment(0, 10);
        labelOutputLR.setLayoutData(dataLabelOutputLR);

        comboOutputLRViewer = mediator.getGUIToolKit().createTableComboForComponents(
                compositeOutputLR, ComponentType.LogicalRecord);
        comboOutputLR = comboOutputLRViewer.getTableCombo();
        FormData dataComboOutputLR = new FormData();
        dataComboOutputLR.top = new FormAttachment(checkboxCreateView, 10);
        dataComboOutputLR.left = new FormAttachment(labelOutputLR, 10);
        dataComboOutputLR.right = new FormAttachment(100, -10);
        comboOutputLR.setLayoutData(dataComboOutputLR);

        comboOutputLR.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!comboOutputLR.getText().equals(selectedLogicalRecord)) {
                    LogicalRecordQueryBean logicalRecordBean = null;
                    if (comboOutputLR.getTable().getSelection().length > 0) {
                        logicalRecordBean = (LogicalRecordQueryBean) comboOutputLR
                                .getTable().getSelection()[0].getData();
                    }

                    if (logicalRecordBean == null) {
                        return;
                    }

                    EnvironmentalQueryBean bean = null;
                    if (comboOutputLR.getTable().getSelectionCount() > 0) {
                        bean = (EnvironmentalQueryBean) comboOutputLR
                                .getTable().getSelection()[0].getData();
                        prevLR = bean;
                    } else {
                        if (prevLR != null) {
                            comboOutputLR.setText(UIUtilities.getComboString(
                                prevLR.getName(), prevLR.getId()));
                        } else {
                            comboOutputLR.setText("");
                        }
                    }

                    if (!view.getViewColumns().getActiveItems().isEmpty()) {
                        MessageDialog dialog = new MessageDialog(
                                mediator.getSite().getShell(),
                                "Output Logical Record",
                                null,
                                "This action will remove existing View Columns from View.Do you want to continue?",
                                MessageDialog.QUESTION, new String[] { "&OK",
                                        "&Cancel" }, 0);
                        int confirm = dialog.open();
                        if (confirm == 1) {
                            if (comboOutputLR.indexOf(selectedLogicalRecord) < 0) {
                                comboOutputLR.getTable().deselectAll();
                            } else {
                                comboOutputLR.setText(selectedLogicalRecord);
                            }
                            return;
                        }
                    }
                    try {
                        LogicalRecord logicalRecord = SAFRApplication
                                .getSAFRFactory().getLogicalRecord(
                                        logicalRecordBean.getId());
                        view.createViewColumns(logicalRecord);
                    } catch (SAFRValidationException sve) {
                        MessageDialog.openError(mediator.getSite().getShell(),
                                "Create View based on Output Logical Record",
                                sve.getMessageString());
                        sve.printStackTrace();
                        if (comboOutputLR.indexOf(selectedLogicalRecord) < 0) {
                            comboOutputLR.getTable().deselectAll();
                        } else {
                            comboOutputLR.getTable().select(
                                    comboOutputLR
                                            .indexOf(selectedLogicalRecord));
                        }
                        return;
                    } catch (SAFRException e1) {
                        UIUtilities.handleWEExceptions(e1,"Create View based on Output Logical Record",null);
                        if (comboOutputLR.indexOf(selectedLogicalRecord) < 0) {
                            comboOutputLR.getTable().deselectAll();
                        } else {
                            comboOutputLR.getTable().select(
                                    comboOutputLR
                                            .indexOf(selectedLogicalRecord));
                        }
                        return;
                    }
                    mediator.setModified(true);
                    selectedLogicalRecord = comboOutputLR.getText();
                    mediator.refreshColumns();
                }
            }
        });
    }


    private void addControlRecOpenEditorMenu()
    {
        Text text = comboControlRecord.getTextControl();
        Menu menu = text.getMenu();
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Open Editor");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                ControlRecordQueryBean bean = (ControlRecordQueryBean)((StructuredSelection) comboControlRecordViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.ControlRecord);                        
                }                
            }
        });
    }       

    protected void populateCombos() {
        mediator.populateViewCombos(ComponentType.ControlRecord, comboControlRecord, comboControlRecordViewer, false);
        mediator.populateViewCombos(ComponentType.LogicalRecord, comboOutputLR, comboOutputLRViewer, false);
    }

    protected void refreshControlsGeneral() {
        UIUtilities.checkNullText(textID, Integer.toString(view.getId()));
        UIUtilities.checkNullText(textName, view.getName());
        UIUtilities.checkNullText(textComments, view.getComment());
        
        if (view.getId() > 0) {
            labelCreatedValue.setText(view.getCreateBy() + " on "
                    + view.getCreateTimeString());
            labelModifiedValue.setText(view.getModifyBy() + " on "
                    + view.getModifyTimeString());

            if (view.getActivatedBy() == null || view.getActivatedTimeString() == null || view.getCompilerVersion()==null) {
                labelActivatedValue.setText(defaultModStr);
            } else {
                labelActivatedValue.setText(view.getActivatedBy() + " on "
                    + view.getActivatedTimeString() + " version " + view.getCompilerVersion());                
            }
            sectionOutputLR.setVisible(false);
        } else {
            sectionOutputLR.setVisible(true);
        }

        loadSectionOutputFormat();

        // if this is a new view then select output LR in combo
        // this is required for the new toggle behaviour, so that the
        // previously selected output LR is retained if the user comes back
        // to view prop tab from the Grid tab.
        comboOutputLR.setText(selectedLogicalRecord);

        try {
            // setting the Control Record combo
            UIUtilities.selectComponentInCombo(comboControlRecord, view
                    .getControlRecord());
            selectedControlRecord = comboControlRecord.getText();
        } catch (SAFRNotFoundException snfe) {
            logger.log(Level.INFO, "", snfe);
            view.setControlRecord(null);
        }

        if (comboOutputLR.getTable().getSelection().length > 0) {
            prevLR = (EnvironmentalQueryBean) comboOutputLR.getTable()
                    .getSelection()[0].getData();
        } else {
            prevLR = null;
        }
        
        tableViewerViewFolders.setInput(view.getViewFolderAssociations());
        
    }

    private void loadSectionOutputFormat() {
        switch (view.getOutputFormat()) {
        case Extract_Fixed_Width_Fields:
            extractFixedWidth.setSelection(true);
            refreshExtractFixed();
            break;
        case Extract_Source_Record_Layout:
            extractSourceRecord.setSelection(true);
            refreshExtractSource();
            break;
        case Format_Delimited_Fields:
            formatDelimitedFields.setSelection(true);    
            refreshFormatDelimited();
            break;
        case Format_Fixed_Width_Fields:
            formatFixedWidth.setSelection(true);
            refreshFormatFixed();
            break;
        case Format_Report:
            formatReport.setSelection(true);
            refreshFormatReport();
            break;
        }
    }
    
    protected Control getControlFromProperty(Object property) {
        if (property == Property.NAME) {
            return textName;
        } else if (property == Property.CONTROL_RECORD) {
            return comboControlRecord;
        } else if (property == Property.OUTPUT_FORMAT) {
            return sectionOutputFormat; 
        } else {
            return null;
        }
    }

    protected CTabItem getGeneralTab() {
        return tabGeneral;
    }

    public void setFocus() {
        textName.setFocus();
    }

    protected void createFormatTab() {
        if (mediator.getFormatTab().isDisposed()) {
            mediator.createTabFormatPhase();
        }
        try {
            mediator.loadFormatPhase();
        } catch (SAFRException e1) {
            UIUtilities.handleWEExceptions(e1,"Error loading controls related to format phase.",null);
        }
    }

    protected void refreshExtractFixed() {
        mediator.hideFormatPhase();
        mediator.hideFormatReport();
        mediator.setDefaultWorkFileNumber();
        mediator.hideFormatDelimited();
        mediator.enableExtractOutputFile(true);

        // update view editor rows.
        mediator.updateOutputInformationChangeAffectedRows(false);
        mediator.refreshExtractRecordAggregationState();
    }

    protected void refreshExtractSource() {
        comboOutputLR.clearSelection();
        comboOutputLR.setEnabled(false);

        mediator.hideFormatPhase();
        mediator.hideFormatReport();
        mediator.hideFormatDelimited();
        mediator.enableExtractOutputFile(true);    
        mediator.setDefaultWorkFileNumber();

        // update view editor rows.
        mediator.updateOutputInformationChangeAffectedRows(false);
        mediator.refreshExtractRecordAggregationState();
    }    
    
    protected void refreshFormatFixed() {
        createFormatTab();
        mediator.hideFormatReport();
        mediator.enableExtractOutputFile(false);
        mediator.setEnableWorkFileNo(!mediator.getGUIToolKit().isReadOnly());
        mediator.hideFormatDelimited();
        
        // update view editor rows.
        mediator.updateOutputInformationChangeAffectedRows(false);
        mediator.refreshExtractRecordAggregationState();
    }
    
    protected void refreshFormatDelimited() {
        createFormatTab();
        mediator.hideFormatReport();
        mediator.enableExtractOutputFile(false);
        mediator.setEnableWorkFileNo(!mediator.getGUIToolKit().isReadOnly());
        mediator.showFormatDelimited();
        mediator.loadDelimitersSection();
        // update view editor rows.
        mediator.updateOutputInformationChangeAffectedRows(false);
        mediator.refreshExtractRecordAggregationState();
    }
    
    protected void refreshFormatReport() {
        createFormatTab();                    
        mediator.enableExtractOutputFile(false);
        mediator.showFormatReport();    
        mediator.enableFormatOutputFile(true);
        mediator.hideFormatDelimited();
        mediator.setEnableWorkFileNo(!mediator.getGUIToolKit().isReadOnly());
   
        // update view editor rows.
        mediator.updateOutputInformationChangeAffectedRows(true);        
        mediator.refreshExtractRecordAggregationState();
    }    
}
