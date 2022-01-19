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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class PhysicalFileDatabaseEditor {

    private static final int MAXDDNAME = 8;
    private static final int MAXSUBSYSTEM = 16;
    private static final int MAXSCHEMA = 128;
    private static final int MAXTABLENAME = 48;
    private static final int MAXSQL = 1280;
    
    private PhysicalFileMediator mediator;
    private Composite parent;
    private PhysicalFile physicalFile;
    private PhysicalFile.SQLDatabase pfSql;

    private Text textSubsystem;
    private Composite compositeDB2;
        
    private Section sectionSQL;
    private Composite compositeSQL;
    private Text textSQL;
    private Text textSQLDDName;
    
    private Section sectionVSAM;
    private Composite compositeVSAM;
    private Text textVSAMSchema;
    private Text textVSAMTable;
    private Combo comboVSAMRowFormat;
    private Button checkVSAMNull;
    
    private String selectedRowFormat = "";
    
    public PhysicalFileDatabaseEditor(PhysicalFileMediator mediator,
        Composite parent, PhysicalFile physicalFile) {
        super();
        this.mediator = mediator;
        this.parent = parent;
        this.physicalFile = physicalFile;
        this.pfSql = physicalFile.new SQLDatabase();
    }

    public Composite create() {
        compositeDB2 = mediator.getSAFRToolkit().createComposite(parent, SWT.NONE);
        compositeDB2.setLayout(new FormLayout());

        FormData dataComposite = new FormData();
        dataComposite.bottom = new FormAttachment(100, 0);
        compositeDB2.setLayoutData(dataComposite);
        
        Label labelSubSystem = mediator.getSAFRToolkit().createLabel(compositeDB2,SWT.NONE, "Db2 Subsystem:");
        FormData labelSubSystemData = new FormData();
        labelSubSystemData.top = new FormAttachment(0, 10);
        labelSubSystemData.left = new FormAttachment(0, 20);
        labelSubSystem.setLayoutData(labelSubSystemData);
        
        textSubsystem = mediator.getSAFRToolkit().createTextBox(compositeDB2, SWT.NONE);
        textSubsystem.setData(SAFRLogger.USER, "Db2 Subsystem"); 
        FormData textSubsystemData = new FormData();
        textSubsystemData.top = new FormAttachment(0, 10);
        textSubsystemData.left = new FormAttachment(labelSubSystem, 70);
        textSubsystemData.width = 140;
        textSubsystem.setLayoutData(textSubsystemData);
        textSubsystem.setTextLimit(MAXSUBSYSTEM);
        textSubsystem.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!UIUtilities.isEqualString(pfSql.getSubSystem(), textSubsystem.getText())) {
                    mediator.setDirty(true);
                    pfSql.setSubSystem(textSubsystem.getText());
                }
            }
        });
        
        createSectionSQL();
  //      createSectionVSAM();
        
        return compositeDB2;
        
    }
    
    private void createSectionSQL() {
        sectionSQL = mediator.getSAFRToolkit().createSection(compositeDB2, Section.TITLE_BAR, "Db2 via SQL");
        sectionSQL.setLayout(new FormLayout());
        FormData dataSectionDetails = new FormData();
        dataSectionDetails.top = new FormAttachment(textSubsystem, 10);
        sectionSQL.setLayoutData(dataSectionDetails);        
        
        compositeSQL = mediator.getSAFRToolkit().createComposite(sectionSQL, SWT.NONE);
        compositeSQL.setLayout(new FormLayout());
        sectionSQL.setClient(compositeSQL);     
        
        Label labelDDName = mediator.getSAFRToolkit().createLabel(compositeSQL,SWT.NONE, "&DD Name:");
        FormData labelDDNameData = new FormData();
        labelDDNameData.top = new FormAttachment(0, 10);
        labelDDNameData.left = new FormAttachment(0, 20);
        labelDDName.setLayoutData(labelDDNameData);
        
        textSQLDDName = mediator.getSAFRToolkit().createTextBox(compositeSQL, SWT.NONE);
        textSQLDDName.setData(SAFRLogger.USER, "DB2 DD Name"); 
        FormData textSQLDDNameData = new FormData();
        textSQLDDNameData.top = new FormAttachment(0, 10);
        textSQLDDNameData.left = new FormAttachment(labelDDName, 100);
        textSQLDDNameData.width = 100;
        textSQLDDName.setLayoutData(textSQLDDNameData);
        textSQLDDName.setTextLimit(MAXDDNAME);
        textSQLDDName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                Code accessMethod = physicalFile.getAccessMethodCode();
                if (accessMethod.getGeneralId().equals(Codes.DB2VIASQL) && 
                    !UIUtilities.isEqualString(pfSql.getInputDDName(), textSQLDDName.getText())) {
                    mediator.setDirty(true);
                    pfSql.setInputDDName(textSQLDDName.getText());
                }
            }
        });
        
        Label SQLLabel = mediator.getSAFRToolkit().createLabel(compositeSQL, SWT.NONE, "&SQL:");
        FormData SQLLabelData = new FormData();
        SQLLabelData.top = new FormAttachment(labelDDName, 10);
        SQLLabelData.left = new FormAttachment(0, 20); 
        SQLLabel.setLayoutData(SQLLabelData);
        
        textSQL = mediator.getSAFRToolkit().createCommentsTextBox(compositeSQL);
        textSQL.setData(SAFRLogger.USER, "SQL Statement");
        FormData SQLTextData = new FormData();
        SQLTextData.top = new FormAttachment(labelDDName, 10);
        SQLTextData.left = new FormAttachment(labelDDName, 100); 
        SQLTextData.width = 300;
        SQLTextData.height = 80;
        textSQL.setLayoutData(SQLTextData);  
        textSQL.setTextLimit(MAXSQL);
        textSQL.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                Code accessMethod = physicalFile.getAccessMethodCode();
                if (accessMethod.getGeneralId().equals(Codes.DB2VIASQL) && 
                    !UIUtilities.isEqualString(pfSql.getSqlStatement(), textSQL.getText())) {
                    mediator.setDirty(true);
                    pfSql.setSqlStatement(textSQL.getText());
                }
            }            
        });
        
    }

    private void createSectionVSAM() {
        sectionVSAM = mediator.getSAFRToolkit().createSection(compositeDB2, Section.TITLE_BAR, "DB2 via VSAM");
        sectionVSAM.setLayout(new FormLayout());
        FormData dataSectionDetails = new FormData();
        dataSectionDetails.top = new FormAttachment(sectionSQL, 10);
        dataSectionDetails.bottom = new FormAttachment(100, 0);
        sectionVSAM.setLayoutData(dataSectionDetails);        
        
        compositeVSAM = mediator.getSAFRToolkit().createComposite(sectionVSAM, SWT.NONE);
        compositeVSAM.setLayout(new FormLayout());
        sectionVSAM.setClient(compositeVSAM);   
        
        Label labelSchema = mediator.getSAFRToolkit().createLabel(compositeVSAM,SWT.NONE, "&Schema:");
        FormData labelSchemaData = new FormData();
        labelSchemaData.top = new FormAttachment(0, 10);
        labelSchemaData.left = new FormAttachment(0, 20);
        labelSchema.setLayoutData(labelSchemaData);
        
        textVSAMSchema = mediator.getSAFRToolkit().createTextBox(compositeVSAM, SWT.NONE);
        textVSAMSchema.setData(SAFRLogger.USER, "DB2 Schema"); 
        FormData textVSAMSchemaData = new FormData();
        textVSAMSchemaData.top = new FormAttachment(0, 10);
        textVSAMSchemaData.left = new FormAttachment(labelSchema, 110);
        textVSAMSchemaData.width = 300;
        textVSAMSchema.setLayoutData(textVSAMSchemaData);
        textVSAMSchema.setTextLimit(MAXSCHEMA);
        textVSAMSchema.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                
                Code accessMethod = physicalFile.getAccessMethodCode();
                if (accessMethod.getGeneralId().equals(Codes.DB2VIAVSAM) && 
                    !UIUtilities.isEqualString(pfSql.getSchema(), textVSAMSchema.getText())) {
                    mediator.setDirty(true);
                    pfSql.setSchema(textVSAMSchema.getText());
                }
            }
        });

        Label labelTable = mediator.getSAFRToolkit().createLabel(compositeVSAM,SWT.NONE, "&Table Name:");
        FormData labelTableData = new FormData();
        labelTableData.top = new FormAttachment(labelSchema, 10);
        labelTableData.left = new FormAttachment(0, 20);
        labelTable.setLayoutData(labelTableData);
        
        textVSAMTable = mediator.getSAFRToolkit().createTextBox(compositeVSAM, SWT.NONE);
        textVSAMTable.setData(SAFRLogger.USER, "DB2 Table"); 
        FormData textVSAMTableData = new FormData();
        textVSAMTableData.top = new FormAttachment(labelSchema, 10);
        textVSAMTableData.left = new FormAttachment(labelSchema, 110);
        textVSAMTableData.width = 300;
        textVSAMTable.setLayoutData(textVSAMTableData);
        textVSAMTable.setTextLimit(MAXTABLENAME);
        textVSAMTable.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                
                Code accessMethod = physicalFile.getAccessMethodCode();
                if (accessMethod.getGeneralId().equals(Codes.DB2VIAVSAM) && 
                    !UIUtilities.isEqualString(pfSql.getTableName(), textVSAMTable.getText())) {
                    mediator.setDirty(true);
                    pfSql.setTableName(textVSAMTable.getText());
                }
            }
        });
        
        Label labelSQLRowFormat = mediator.getSAFRToolkit().createLabel(compositeVSAM, SWT.NONE, "Row &Format:");
        FormData labelSQLRowFormatData = new FormData();
        labelSQLRowFormatData.top = new FormAttachment(labelTable, 10);
        labelSQLRowFormatData.left = new FormAttachment(0, 20);
        labelSQLRowFormat.setLayoutData(labelSQLRowFormatData);
        
        comboVSAMRowFormat = mediator.getSAFRToolkit().createComboBox(compositeVSAM, SWT.READ_ONLY, "");
        comboVSAMRowFormat.setData(SAFRLogger.USER, "SQL Row Format");
        FormData comboVSAMRowFormatData = new FormData();
        comboVSAMRowFormatData.top = new FormAttachment(labelTable, 10);
        comboVSAMRowFormatData.left = new FormAttachment(labelSchema, 110);
        comboVSAMRowFormat.setLayoutData(comboVSAMRowFormatData);
        UIUtilities.populateComboBox(comboVSAMRowFormat, CodeCategories.DBMSROWFMT,-1, true);
        comboVSAMRowFormat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Code accessMethod = physicalFile.getAccessMethodCode();
                if (accessMethod.getGeneralId().equals(Codes.DB2VIAVSAM) && 
                    !(comboVSAMRowFormat.getText().equals(selectedRowFormat))) {
                    mediator.setDirty(true);
                    selectedRowFormat = comboVSAMRowFormat.getText();
                    Code rowFormat = UIUtilities.getCodeFromCombo(comboVSAMRowFormat);
                    if (rowFormat != null) {
                        pfSql.setRowFormatCode(rowFormat);
                    }
                }
            }
        });
        
        checkVSAMNull = mediator.getSAFRToolkit().createCheckBox(compositeVSAM, "Return N&ull Ind");
        checkVSAMNull.setData(SAFRLogger.USER, "SQL Return Null Ind");            
        FormData checkVSAMNullData = new FormData();
        checkVSAMNullData.top = new FormAttachment(labelTable, 10);
        checkVSAMNullData.left = new FormAttachment(comboVSAMRowFormat, 10);
        checkVSAMNull.setLayoutData(checkVSAMNullData);
        checkVSAMNull.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Code accessMethod = physicalFile.getAccessMethodCode();
                if (accessMethod.getGeneralId().equals(Codes.DB2VIAVSAM)) { 
                    pfSql.setIncludeNullIndicators(checkVSAMNull.getSelection());
                    mediator.setDirty(true);
                }
            }
        });
    }    
    
    protected void doRefreshControls() {        
        UIUtilities.checkNullText(textSubsystem, pfSql.getSubSystem());
        
        Code accessMethod = physicalFile.getAccessMethodCode();
        if (accessMethod.getGeneralId().equals(Codes.DB2VIASQL)) {
            UIUtilities.checkNullText(textSQLDDName, pfSql.getInputDDName());
            UIUtilities.checkNullText(textSQL, pfSql.getSqlStatement());
//            textVSAMSchema.setText("");
//            textVSAMTable.setText("");
//            comboVSAMRowFormat.setText("");
            selectedRowFormat = "";
//            checkVSAMNull.setSelection(false);           
        } else if (accessMethod.getGeneralId().equals(Codes.DB2VIAVSAM)) {
            textSQLDDName.setText("");
            textSQL.setText("");
            UIUtilities.checkNullText(textVSAMSchema, pfSql.getSchema());
            UIUtilities.checkNullText(textVSAMTable, pfSql.getTableName());
            UIUtilities.checkNullCombo(comboVSAMRowFormat, pfSql.getRowFormatCode());
            if (pfSql.getRowFormatCode() != null) {
                comboVSAMRowFormat.setText(pfSql.getRowFormatCode().getDescription());
            } else {
                comboVSAMRowFormat.setText("");
            }            
            selectedRowFormat = comboVSAMRowFormat.getText();
            checkVSAMNull.setSelection(pfSql.isIncludeNullIndicators());
        }
    }


    protected void enableSQL() {
        textSQLDDName.setEnabled(true);
        textSQL.setEnabled(true);
//        textVSAMSchema.setEnabled(false);
//        textVSAMTable.setEnabled(false);
//        comboVSAMRowFormat.setEnabled(false);
//        checkVSAMNull.setEnabled(false);        
    }

    protected void enableVSAM() {
        textSQLDDName.setEnabled(false);
        textSQL.setEnabled(false);
        textVSAMSchema.setEnabled(true);
        textVSAMTable.setEnabled(true);
        comboVSAMRowFormat.setEnabled(true);
        checkVSAMNull.setEnabled(true);        
        
    }
    
}
