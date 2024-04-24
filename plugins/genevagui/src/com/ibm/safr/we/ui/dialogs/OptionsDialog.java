package com.ibm.safr.we.ui.dialogs;

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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.safr.we.model.view.ViewActivator;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.utilities.SAFRLogger;

public class OptionsDialog extends TitleAreaDialog {

    static transient Logger logger = Logger
        .getLogger("com.ibm.safr.we.ui.dialogs.OptionsDialog");
    
    private SAFRGUIToolkit safrGUIToolkit;
    private Shell parentShell;
    private Composite compositeTopLevel;
    private Text logPath;
    private String logPathStr;
    private Button migrateWarnings;
    private Button importWarnings;
    private Button activationReportChkBox;
    private boolean isIgnoreMigrateWarnings = false;
    private boolean isIgnoreImportWarnings = false;
    private boolean isFullActivationReportEnabled = false;

	private Text reportPath;

	private String reportsPathStr;
    
	public OptionsDialog(Shell parentShell, String logPathStr, String reportsPath, boolean isIgnoreMigrateWarnings, boolean isIgnoreImportWarnings, boolean act) {
		super(parentShell);
		safrGUIToolkit = new SAFRGUIToolkit();
		this.parentShell = parentShell;
		this.logPathStr = logPathStr;
		this.isIgnoreMigrateWarnings = isIgnoreMigrateWarnings;
		this.isIgnoreImportWarnings = isIgnoreImportWarnings;
		this.reportsPathStr = reportsPath;
		this.isFullActivationReportEnabled = act;
	}

    @Override
    protected Point getInitialSize() {
        return new Point(700, 400);
    }

    
    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        getShell().setText("GenevaERS Options");
        setTitle("GenevaERS Options");
        return contents;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        compositeTopLevel = safrGUIToolkit.createComposite(parent, SWT.NONE);
        compositeTopLevel.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(3, false);
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 0;
        layout.marginTop = 10;
        layout.marginRight = 50;
        compositeTopLevel.setLayout(layout);

        
        addLogPath();
        addReportPath();

        Label migrateWarningsLabel = safrGUIToolkit.createLabel(compositeTopLevel, SWT.NONE, "Enable &Migrate Messages:   ");
        migrateWarningsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));        
        migrateWarnings = safrGUIToolkit.createCheckBox(compositeTopLevel, "");
        GridData migrateWarningsData = new GridData(SWT.NONE, SWT.CENTER, false, false);
        migrateWarningsData.horizontalSpan = 2;
        migrateWarnings.setLayoutData(migrateWarningsData);
        migrateWarnings.setSelection(!isIgnoreMigrateWarnings);
        migrateWarnings.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                isIgnoreMigrateWarnings = !migrateWarnings.getSelection();                                        
            }
        });

        Label importWarningsLabel = safrGUIToolkit.createLabel(compositeTopLevel, SWT.NONE, "Enable &Import Messages:   ");
        importWarningsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));        
        importWarnings = safrGUIToolkit.createCheckBox(compositeTopLevel, "");
        GridData importWarningsData = new GridData(SWT.NONE, SWT.CENTER, false, false);
        importWarningsData.horizontalSpan = 2;
        importWarnings.setLayoutData(importWarningsData);
        importWarnings.setSelection(!isIgnoreImportWarnings);
        importWarnings.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                isIgnoreImportWarnings = !importWarnings.getSelection();                                        
            }
        });
        
        addActivationReportOption();
        
        return parent;
    }

	private void addActivationReportOption() {
        Label label = safrGUIToolkit.createLabel(compositeTopLevel, SWT.NONE, "Enable Full &Activation Report:   ");
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));        
        activationReportChkBox = safrGUIToolkit.createCheckBox(compositeTopLevel, "");
        GridData importWarningsData = new GridData(SWT.NONE, SWT.CENTER, false, false);
        importWarningsData.horizontalSpan = 2;
        activationReportChkBox.setLayoutData(importWarningsData);
        activationReportChkBox.setSelection(isFullActivationReportEnabled);
        activationReportChkBox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	isFullActivationReportEnabled = activationReportChkBox.getSelection();                                        
            }
        });
	}

	private void addLogPath() {
		Label logPathLabel = safrGUIToolkit.createLabel(compositeTopLevel, SWT.NONE, "&Log Path:   ");
        logPathLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        logPath = safrGUIToolkit.createTextBox(compositeTopLevel, SWT.BORDER);
        GridData logPathData = new GridData(SWT.NONE, SWT.CENTER, true, false);
        logPathData.widthHint = 450;
        logPath.setLayoutData(logPathData);
        logPath.setText(logPathStr);
        logPath.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                logPathStr = ((Text)e.getSource()).getText();
            }
            
        });
        Button changeLogPath = safrGUIToolkit.createButton(compositeTopLevel, SWT.PUSH, " ... ");
        changeLogPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        changeLogPath.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(parentShell);
                String logPathStr = SAFRLogger.getLogPath();
                dialog.setFilterPath(logPathStr);
                dialog.setMessage("Please select a new destination for the log file");
                String newLogPath = dialog.open();
                if (newLogPath == null || newLogPath.length() == 0) {
                    // if cancelled
                }
                else {
                    logPath.setText(newLogPath);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            
        });
	}

	private void addReportPath() {
		Label reportPathLabel = safrGUIToolkit.createLabel(compositeTopLevel, SWT.NONE, "&Report Path:   ");
        reportPathLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        reportPath = safrGUIToolkit.createTextBox(compositeTopLevel, SWT.BORDER);
        GridData reportPathData = new GridData(SWT.NONE, SWT.CENTER, true, false);
        reportPathData.widthHint = 450;
        reportPath.setLayoutData(reportPathData);
        reportPath.setText(reportsPathStr);
        reportPath.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
            	reportsPathStr = ((Text)e.getSource()).getText();
            }
            
        });
        Button changeLogPath = safrGUIToolkit.createButton(compositeTopLevel, SWT.PUSH, " ... ");
        changeLogPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        changeLogPath.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(parentShell);
                String reportPathStr = SAFRLogger.getLogPath();
                dialog.setFilterPath(reportPathStr);
                dialog.setMessage("Select a new destination for report files");
                String newReportPath = dialog.open();
                if (newReportPath == null || newReportPath.length() == 0) {
                    // if cancelled
                }
                else {
                	reportPath.setText(newReportPath);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            
        });
	}
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "&OK", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
    }
    
    public String getLogPathStr() {
        return logPathStr;
    }

    public boolean isIgnoreMigrateWarnings() {
        return isIgnoreMigrateWarnings;
    }

    public boolean isIgnoreImportWarnings() {
        return isIgnoreImportWarnings;
    }

	public String getReportsPath() {
		return reportsPathStr;
	}
	
	public boolean isFullActivationReportEnabled() {
		return isFullActivationReportEnabled;
	}
    
}
