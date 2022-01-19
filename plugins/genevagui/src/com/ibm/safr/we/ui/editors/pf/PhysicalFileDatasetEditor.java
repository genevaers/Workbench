package com.ibm.safr.we.ui.editors.pf;

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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class PhysicalFileDatasetEditor {

    private static final int MAXDDNAME = 8;
    private static final int MAXDSN = 44;
    private static final int MAXRECLEN = 9;
    private static final int MAXLRECL = 9;
    
    private PhysicalFileMediator mediator;
    private Composite parent;
    PhysicalFile physicalFile;
    private PhysicalFile.InputDataset pfInput;
    private PhysicalFile.OutputDataset pfOutput;
    
    private Composite compositeDataset;
    
    private Composite compositeInput;    
    private Section sectionInput;
    private Text textInputDDName;
    private Text textInputDSN;
    private Text textMinLen;
    private Text textMaxLen;
    
    private Composite compositeOutput;
    private Section sectionOutput;
    private Text textOutputDDName;
    private Combo comboOutputRECFM;
    private Text textOutputLRECL;
        
    private String selectedRECFM = "";
    
    public PhysicalFileDatasetEditor(PhysicalFileMediator mediator,
        Composite parent, PhysicalFile physicalFile) {
        super();
        this.mediator = mediator;
        this.parent = parent;
        this.physicalFile = physicalFile;
        this.pfInput = physicalFile.new InputDataset();
        this.pfOutput = physicalFile.new OutputDataset();
    }

    public Composite create() {
        compositeDataset = mediator.getSAFRToolkit().createComposite(parent, SWT.NONE);
        compositeDataset.setLayout(new FormLayout());

        FormData dataComposite = new FormData();
        dataComposite.bottom = new FormAttachment(100, 0);
        compositeDataset.setLayoutData(dataComposite);
        
        createSectionInput();
        createSectionOutput();
        
        return compositeDataset;
    }
    
    private void createSectionInput() {
        sectionInput = mediator.getSAFRToolkit().createSection(compositeDataset, Section.TITLE_BAR, "Input");
        sectionInput.setLayout(new FormLayout());
        FormData dataSectionDetails = new FormData();
        dataSectionDetails.top = new FormAttachment(0, 10);
        sectionInput.setLayoutData(dataSectionDetails);        
        
        compositeInput = mediator.getSAFRToolkit().createComposite(sectionInput, SWT.NONE);
        compositeInput.setLayout(new FormLayout());
        sectionInput.setClient(compositeInput);                

        Label labelInputDDName = mediator.getSAFRToolkit().createLabel(compositeInput,SWT.NONE, "&DD Name:");
        FormData inputDDLabelData = new FormData();
        inputDDLabelData.top = new FormAttachment(0, 10);
        inputDDLabelData.left = new FormAttachment(0, 20);
        labelInputDDName.setLayoutData(inputDDLabelData);
        
        textInputDDName = mediator.getSAFRToolkit().createTextBox(compositeInput, SWT.NONE);
        textInputDDName.setData(SAFRLogger.USER, "Input DD Name"); 
        FormData inputDDTextData = new FormData();
        inputDDTextData.top = new FormAttachment(0, 10);
        inputDDTextData.left = new FormAttachment(labelInputDDName, 120);
        inputDDTextData.width = 100;
        textInputDDName.setLayoutData(inputDDTextData);
        textInputDDName.setTextLimit(MAXDDNAME);
        textInputDDName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!UIUtilities.isEqualString(pfInput.getInputDDName(), textInputDDName.getText())) {
                    mediator.setDirty(true);
                    pfInput.setInputDDName(textInputDDName.getText());
                }
            }
        });

        Label labelInputDSN = mediator.getSAFRToolkit().createLabel(compositeInput, SWT.NONE, "D&ata Set Name:");
        FormData inputDSNLabelData = new FormData();
        inputDSNLabelData.top = new FormAttachment(labelInputDDName, 10);
        inputDSNLabelData.left = new FormAttachment(0, 20);
        labelInputDSN.setLayoutData(inputDSNLabelData);
        
        textInputDSN = mediator.getSAFRToolkit().createTextBox(compositeInput, SWT.NONE);
        textInputDSN.setData(SAFRLogger.USER, "Input DSN");       
        FormData inputDSNTextData = new FormData();
        inputDSNTextData.top = new FormAttachment(labelInputDDName, 10);
        inputDSNTextData.left = new FormAttachment(labelInputDDName, 120);
        inputDSNTextData.width = 300;
        textInputDSN.setLayoutData(inputDSNTextData);
        textInputDSN.setTextLimit(MAXDSN);
        textInputDSN.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                Code fileType = physicalFile.getFileTypeCode();
                if (fileType.getGeneralId().equals(Codes.FILE_DISK) && 
                    !UIUtilities.isEqualString(pfInput.getDatasetName(), textInputDSN.getText())) {
                    mediator.setDirty(true);
                    pfInput.setDatasetName(textInputDSN.getText());
                }
            }
        });

        Label labelMinLen = mediator.getSAFRToolkit().createLabel(compositeInput, SWT.NONE, "M&in. Record Length:");
        FormData minLenLabelData = new FormData();
        minLenLabelData.top = new FormAttachment(labelInputDSN, 10);
        minLenLabelData.left = new FormAttachment(0, 20);
        labelMinLen.setLayoutData(minLenLabelData);
        
        textMinLen = mediator.getSAFRToolkit().createIntegerTextBox(compositeInput, SWT.NONE, false);
        textMinLen.setData(SAFRLogger.USER, "Minimum Record Length");       
        FormData minLenTextData = new FormData();
        minLenTextData.top = new FormAttachment(labelInputDSN, 10);
        minLenTextData.left = new FormAttachment(labelInputDDName, 120);
        minLenTextData.width = 300;
        textMinLen.setLayoutData(minLenTextData);
        textMinLen.setTextLimit(MAXRECLEN);
        textMinLen.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                Code fileType = physicalFile.getFileTypeCode();
                if (fileType.getGeneralId().equals(Codes.FILE_PIPE) && 
                    !UIUtilities.isEqualString(Integer.toString(pfInput.getMinRecordLen()), textMinLen.getText()) &&
                    textMinLen.getText().length() >0) {
                    mediator.setDirty(true);
                    pfInput.setMinRecordLen(Integer.parseInt(textMinLen.getText()));
                }
            }
        });

        Label labelMaxLen = mediator.getSAFRToolkit().createLabel(compositeInput, SWT.NONE, "&Max Record Length:");
        FormData maxLenLabelData = new FormData();
        maxLenLabelData.top = new FormAttachment(labelMinLen, 10);
        maxLenLabelData.left = new FormAttachment(0, 20);
        labelMaxLen.setLayoutData(maxLenLabelData);
        
        textMaxLen = mediator.getSAFRToolkit().createIntegerTextBox(compositeInput, SWT.NONE, false);
        textMaxLen.setData(SAFRLogger.USER, "Maximum Record Length");       
        FormData maxLenTextData = new FormData();
        maxLenTextData.top = new FormAttachment(labelMinLen, 10);
        maxLenTextData.left = new FormAttachment(labelInputDDName, 120);
        maxLenTextData.width = 300;
        textMaxLen.setLayoutData(maxLenTextData);
        textMaxLen.setTextLimit(MAXRECLEN);
        textMaxLen.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                Code fileType = physicalFile.getFileTypeCode();
                if (fileType.getGeneralId().equals(Codes.FILE_PIPE) && 
                    !UIUtilities.isEqualString(Integer.toString(pfInput.getMaxRecordLen()), textMaxLen.getText()) &&
                    textMaxLen.getText().length() >0) {
                    mediator.setDirty(true);
                    pfInput.setMaxRecordLen(Integer.parseInt(textMaxLen.getText()));
                }
            }
        });
        
    }
    
    private void createSectionOutput() {
        sectionOutput = mediator.getSAFRToolkit().createSection(compositeDataset, Section.TITLE_BAR, "Output");
        sectionOutput.setLayout(new FormLayout());
        FormData dataSectionDetails = new FormData();
        dataSectionDetails.top = new FormAttachment(sectionInput, 10);
        dataSectionDetails.bottom = new FormAttachment(100, 0);
        sectionOutput.setLayoutData(dataSectionDetails);        
        
        compositeOutput = mediator.getSAFRToolkit().createComposite(sectionOutput, SWT.NONE);
        compositeOutput.setLayout(new FormLayout());
        sectionOutput.setClient(compositeOutput);
        
        Label labelOutputDDName = mediator.getSAFRToolkit().createLabel(compositeOutput,SWT.NONE, "&DD Name:");
        FormData outputDDLabelData = new FormData();
        outputDDLabelData.top = new FormAttachment(0, 10);
        outputDDLabelData.left = new FormAttachment(0, 20);
        labelOutputDDName.setLayoutData(outputDDLabelData);
        
        textOutputDDName = mediator.getSAFRToolkit().createTextBox(compositeOutput, SWT.NONE);
        textOutputDDName.setData(SAFRLogger.USER, "Output DD Name"); 
        FormData outputDDTextData = new FormData();
        outputDDTextData.top = new FormAttachment(0, 10);
        outputDDTextData.left = new FormAttachment(labelOutputDDName, 120);
        outputDDTextData.width = 100;
        textOutputDDName.setLayoutData(outputDDTextData);
        textOutputDDName.setTextLimit(MAXDDNAME);
        textOutputDDName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!UIUtilities.isEqualString(pfOutput.getOutputDDName(), textOutputDDName.getText())) {
                    mediator.setDirty(true);
                    pfOutput.setOutputDDName(textOutputDDName.getText());
                }
            }
        });
        
        Label labelOutputRECFM = mediator.getSAFRToolkit().createLabel(compositeOutput, SWT.NONE, "R&ECFM:");
        FormData outputRECFMData = new FormData();
        outputRECFMData.top = new FormAttachment(labelOutputDDName, 10);
        outputRECFMData.left = new FormAttachment(0, 20);
        labelOutputRECFM.setLayoutData(outputRECFMData);
        
        comboOutputRECFM = mediator.getSAFRToolkit().createComboBox(compositeOutput,SWT.READ_ONLY, "");
        comboOutputRECFM.setData(SAFRLogger.USER, "Output RECFM");   
        FormData outputRECFMComboData = new FormData();
        outputRECFMComboData.top = new FormAttachment(labelOutputDDName, 10);
        outputRECFMComboData.left = new FormAttachment(labelOutputDDName, 120);
        outputRECFMComboData.width = 100;
        comboOutputRECFM.setLayoutData(outputRECFMComboData);
        
        UIUtilities.populateComboBox(comboOutputRECFM, CodeCategories.RECFM,-1, true);
        comboOutputRECFM.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                Code fileType = physicalFile.getFileTypeCode();
                if (fileType.getGeneralId().equals(Codes.FILE_PIPE) && 
                    !(comboOutputRECFM.getText().equals(selectedRECFM))) {
                    mediator.setDirty(true);
                    selectedRECFM = comboOutputRECFM.getText();
                    Code codeRECFM = UIUtilities.getCodeFromCombo(comboOutputRECFM);
                    if (codeRECFM != null) {
                        pfOutput.setRecfm(codeRECFM);
                    }
                }
            }
        });
    
        Label labelOutputLRECL = mediator.getSAFRToolkit().createLabel(compositeOutput, SWT.NONE, "&LRECL:");
        FormData outputLRECLData = new FormData();
        outputLRECLData.top = new FormAttachment(labelOutputRECFM, 10);
        outputLRECLData.left = new FormAttachment(0, 20);
        labelOutputLRECL.setLayoutData(outputLRECLData);
    
        textOutputLRECL = mediator.getSAFRToolkit().createIntegerTextBox(compositeOutput, SWT.NONE, false);
        textOutputLRECL.setData(SAFRLogger.USER, "Output LRECL");                                                                                             
        textOutputLRECL.setTextLimit(MAXLRECL);
        FormData outputLRECLTextData = new FormData();
        outputLRECLTextData.top = new FormAttachment(labelOutputRECFM, 10);
        outputLRECLTextData.left = new FormAttachment(labelOutputDDName, 120);
        outputLRECLTextData.width = 100;
        textOutputLRECL.setLayoutData(outputLRECLTextData);
        textOutputLRECL.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                Code fileType = physicalFile.getFileTypeCode();
                if (fileType.getGeneralId().equals(Codes.FILE_PIPE) && 
                    !UIUtilities.isEqualString(Integer.toString(pfOutput.getLrecl()), textOutputLRECL.getText()) && 
                    textOutputLRECL.getText().length()>0) {
                    mediator.setDirty(true);
                    pfOutput.setLrecl(Integer.parseInt(textOutputLRECL.getText()));
                }
            }
        });
            
        Label filler  = mediator.getSAFRToolkit().createLabel(compositeOutput, SWT.NONE, "");
        FormData fillerData = new FormData();
        fillerData.top = new FormAttachment(labelOutputLRECL, 10);
        fillerData.left = new FormAttachment(labelOutputDDName, 120);
        fillerData.width = 300;
        filler.setLayoutData(fillerData);
        
    }
    
    public void doRefreshControls() throws SAFRException {
        
        UIUtilities.checkNullText(textInputDDName, pfInput.getInputDDName());
        
        Code fileType = physicalFile.getFileTypeCode();
        if (fileType.getGeneralId().equals(Codes.FILE_PIPE)) {
            textMinLen.setText(Integer.toString(pfInput.getMinRecordLen()));
            textMaxLen.setText(Integer.toString(pfInput.getMaxRecordLen()));
            UIUtilities.checkNullCombo(comboOutputRECFM, pfOutput.getRecfm());
            if (pfOutput.getRecfm() != null) {
                comboOutputRECFM.setText(pfOutput.getRecfm().getDescription());
            } else {
                comboOutputRECFM.setText("");
            }
            textOutputLRECL.setText(Integer.toString(pfOutput.getLrecl()));
            selectedRECFM = comboOutputRECFM.getText();        
        } else {
            textMinLen.setText("0");
            textMaxLen.setText("0");
            comboOutputRECFM.setText("");
            textOutputLRECL.setText("0");
            selectedRECFM = "";        
        }

        if (fileType.getGeneralId().equals(Codes.FILE_DISK)) {
            UIUtilities.checkNullText(textInputDSN, pfInput.getDatasetName());            
        } else {
            textInputDSN.setText("");                        
        }
        
        UIUtilities.checkNullText(textOutputDDName, pfOutput.getOutputDDName());
        compositeDataset.redraw();
    }
    
    public void enableRecordSettings() {
        textMinLen.setEnabled(true);
        textMaxLen.setEnabled(true);
        comboOutputRECFM.setEnabled(true);
        textOutputLRECL.setEnabled(true);
    }

    public void disableRecordSettings() {
        if (textMinLen != null) {
            textMinLen.setEnabled(false);
            textMaxLen.setEnabled(false);
            comboOutputRECFM.setEnabled(false);
            textOutputLRECL.setEnabled(false);
        }
    }

    public void enableDatasetName() {
        textInputDSN.setEnabled(true);
    }

    public void disableDatasetName() {
        textInputDSN.setEnabled(false);
    }
    
}
