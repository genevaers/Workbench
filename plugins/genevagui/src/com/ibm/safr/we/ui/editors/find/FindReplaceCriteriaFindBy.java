package com.ibm.safr.we.ui.editors.find;

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


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordFieldQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.NumericIdQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.utilities.FindReplaceText;
import com.ibm.safr.we.ui.editors.find.dialog.FindReplaceCompDialog;
import com.ibm.safr.we.ui.utilities.ColumnSelectionListenerForTableCombo;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.MainTableLabelProvider;
import com.ibm.safr.we.utilities.SAFRLogger;

public class FindReplaceCriteriaFindBy {
    
    protected final static int WIDTH_FIND = 390;
    
    private FindReplaceCriteriaMediator mediator;

    // Data
    
    // UI
    private SAFRGUIToolkit safrGuiToolkit;    
    
    // search type
    private Group groupSearchType;    
    private Button radioFindText;
    private Button radioFindComp;
    private boolean isFindByText = true;        
    
    // search by text
    private Text textFind;
    private GridData textFindData;
    private Button buttonSelectComp;
    private GridData buttonSelectCompData;
    private Button checkMatchCase;
    private GridData checkMatchCaseData;
    private Button checkUsePattern;
    private GridData checkUsePatternData;

    // search by component
    private Label compTypeLabel;
    private GridData compTypeLabelData;    
    private Combo compTypeCombo;
    private GridData compTypeComboData;
    private EnvironmentalQueryBean curComp = null;
    private Label lableComp;
    private GridData lableCompData;
    private TableComboViewer comboCompViewer;
    private TableCombo comboComp;
    private GridData comboCompData;
    private int selectedComponentType = -1;
    protected ComponentType componentType;
    private ColumnSelectionListenerForTableCombo[] prvsListner = new ColumnSelectionListenerForTableCombo[2];
    private Label lableField;
    private GridData fldLabelData;
    private TableComboViewer comboFieldViewer;
    private TableCombo comboField; 
    private GridData fldComboData;
    private LogicalRecordFieldQueryBean curField = null;
    
    
    protected FindReplaceCriteriaFindBy() {
    }
    
    protected void setMediator(FindReplaceCriteriaMediator mediator) {
        this.mediator = mediator;
        this.safrGuiToolkit = mediator.getToolkit();                
    }

    protected void create(Composite body, Control top) {
        
        // start of radio group for search type
        groupSearchType = safrGuiToolkit.createGroup(
            body, SWT.NONE, "");
        FormData dataGroupSearchType = new FormData();
        dataGroupSearchType.top = new FormAttachment(top, 10);
        dataGroupSearchType.left = new FormAttachment(0, 5);   
        dataGroupSearchType.height = 135;
        groupSearchType.setLayoutData(dataGroupSearchType);
        GridLayout groupLayout = new GridLayout();
        groupLayout.numColumns = 3;
        groupLayout.verticalSpacing = 10;
        groupLayout.horizontalSpacing = 10;
        groupSearchType.setLayout(groupLayout);

        radioFindText = safrGuiToolkit.createButton(groupSearchType,
            SWT.RADIO, "Find By &Text");
        radioFindText.setData(SAFRLogger.USER, "Find By Text");    
        
        GridData radioFindTextData = new GridData();
        radioFindTextData.widthHint = 120;
        radioFindText.setLayoutData(radioFindTextData);
        radioFindText.setSelection(true); // default selected
        radioFindText.setEnabled(true);
        radioFindText.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                isFindByText = true;
                hideFindByComp(); 
                showFindByText(); 
                mediator.setSearchButtonState(getSearchButtonState());
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
            
        });
        
        radioFindComp = safrGuiToolkit.createButton(groupSearchType,
            SWT.RADIO, "Find By &Component");
        radioFindComp.setData(SAFRLogger.USER, "Find By Component");    
        GridData radioFindCompData = new GridData();
        radioFindCompData.horizontalSpan = 2;
        radioFindCompData.widthHint = WIDTH_FIND;
        radioFindComp.setLayoutData(radioFindCompData);
        radioFindComp.setEnabled(true);
        radioFindComp.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                isFindByText = false;                
                hideFindByText(); 
                showFindByComp();
                groupSearchType.pack();
                mediator.setSearchButtonState(getSearchButtonState());
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
            
        });        
        
        createFindByText();
        createFindByComp();
        
    }
    
    protected boolean getSearchButtonState() {
        if (isFindByText == true) {
            if (!textFind.getText().trim().equals("")) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            if (componentType == ComponentType.LogicalRecord) {
                if (curField == null) { 
                    return false;
                }
                else {
                    return true;
                }
            }
            else {
                if (curComp == null) {
                    return false;
                }                
                else {
                    return true;
                }
            }
        }        
    }
    
    private void hideFindByText() {
        textFind.setVisible(false);
        textFindData.exclude = true;
        buttonSelectComp.setVisible(false);
        buttonSelectCompData.exclude = true;
        checkMatchCase.setVisible(false);
        checkMatchCaseData.exclude = true;
        checkUsePattern.setVisible(false);
        checkUsePatternData.exclude = true;
    }

    private void showFindByText() {
        textFind.setVisible(true);
        textFindData.exclude = false;
        buttonSelectComp.setVisible(true);
        buttonSelectCompData.exclude = false;
        checkMatchCase.setVisible(true);
        checkMatchCaseData.exclude = false;
        checkUsePattern.setVisible(true);
        checkUsePatternData.exclude = false;
    }
    
    private void hideFindByComp() {
        compTypeLabel.setVisible(false);
        compTypeLabelData.exclude = true;
        compTypeCombo.setVisible(false);
        compTypeComboData.exclude = true;
        lableComp.setVisible(false);
        lableCompData.exclude = true;
        comboComp.setVisible(false);
        comboCompData.exclude = true;
        lableField.setVisible(false);
        fldLabelData.exclude = true;
        comboField.setVisible(false);
        fldComboData.exclude = true;
    }

    private void showFindByComp() {
        compTypeLabel.setVisible(true);
        compTypeLabelData.exclude = false;
        compTypeCombo.setVisible(true);
        compTypeComboData.exclude = false;
        lableComp.setVisible(true);
        lableCompData.exclude = false;
        comboComp.setVisible(true);
        comboCompData.exclude = false;        
        lableField.setVisible(true);
        fldLabelData.exclude = false;
        comboField.setVisible(true);
        fldComboData.exclude = false;
    }

    private void createFindByText() {
        textFind = safrGuiToolkit.createTextBox(groupSearchType,SWT.NONE);
        textFind.setData(SAFRLogger.USER, "Find What");                                     
    
        textFindData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textFindData.horizontalSpan = 2;
        textFind.setLayoutData(textFindData);
    
        textFind.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mediator.clearResults();
                mediator.setSearchButtonState(getSearchButtonState());
            }
        });
        textFind.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                mediator.setSearchButtonState(getSearchButtonState());
            }
        });
        buttonSelectComp = safrGuiToolkit.createButton(
            groupSearchType, SWT.PUSH, "...");
        buttonSelectComp.setData(SAFRLogger.USER, "Select Find Component");
        buttonSelectCompData = new GridData();        
        buttonSelectComp.setLayoutData(buttonSelectCompData);
        buttonSelectComp
                .setToolTipText("Click this button to select the component.");
        buttonSelectComp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FindReplaceCompDialog selectComponetNameDialog = new FindReplaceCompDialog(
                        mediator.getSite().getShell(), mediator.getCurentEnv().getId());
                selectComponetNameDialog.create();
                selectComponetNameDialog
                        .setTitle("Select a Component to use in the search text.");
                if (selectComponetNameDialog.open() == Window.OK) {
                    if (!selectComponetNameDialog.getSelectedComponentName()
                            .equals(null)) {
                        StringBuilder stringBuilder = new StringBuilder(
                                textFind.getText());
                        stringBuilder.insert(textFind.getCaretPosition(),
                                selectComponetNameDialog
                                        .getSelectedComponentName());
                        textFind.setText(stringBuilder.toString());
                    }
                }
            }
        });
        checkMatchCase = safrGuiToolkit.createButton(groupSearchType,
                SWT.CHECK, "Matc&h Case");
        checkMatchCase.setData(SAFRLogger.USER, "Match Case");                                          
        checkMatchCaseData = new GridData();            
        checkMatchCase.setLayoutData(checkMatchCaseData);
        checkMatchCase.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mediator.clearResults();
            }
        });
    
        checkUsePattern = safrGuiToolkit.createButton(
            groupSearchType, SWT.CHECK, "&Use Pattern Matching");
        checkUsePattern.setData(SAFRLogger.USER, "Use Pattern Matching");                                                 
        
        checkUsePatternData = new GridData();
        checkUsePatternData.horizontalSpan = 2;        
        checkUsePattern.setLayoutData(checkUsePatternData);
        checkUsePattern.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mediator.clearResults();
            }
        });                    
    }
    
    
    private void createFindByComp() {
        compTypeLabel = safrGuiToolkit.createLabel(groupSearchType, SWT.NONE, "Component T&ype:");
        compTypeLabelData = new GridData(GridData.FILL_HORIZONTAL);
        compTypeLabel.setLayoutData(compTypeLabelData);

        compTypeCombo = safrGuiToolkit.createComboBox(groupSearchType,
                SWT.DROP_DOWN | SWT.READ_ONLY, "");
        compTypeComboData = new GridData();
        compTypeComboData.horizontalSpan = 2;
        compTypeCombo.setLayoutData(compTypeComboData);

        int i = 0;
        compTypeCombo.add("Lookup Path");
        compTypeCombo.setData(String.valueOf(i++),
                ComponentType.LookupPath);
        compTypeCombo.add("Logical File");
        compTypeCombo.setData(String.valueOf(i++),
                ComponentType.LogicalFile);
        compTypeCombo.add("Logical Record Field");
        compTypeCombo.setData(String.valueOf(i++),
                ComponentType.LogicalRecord);
        compTypeCombo.add("Physical File");
        compTypeCombo.setData(String.valueOf(i++),
                ComponentType.PhysicalFile);
        compTypeCombo.add("User-Exit Routine");
        compTypeCombo.setData(String.valueOf(i++),
                ComponentType.UserExitRoutine);

        compTypeCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onTypeSelection();
            }            
        });
        
        compTypeCombo.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                onTypeSelection();
            }
        });
        
        lableComp = safrGuiToolkit.createLabel(groupSearchType,
            SWT.NONE, "Compo&nent:");
        lableCompData = new GridData();
        lableComp.setLayoutData(lableCompData);
        
        comboCompViewer = safrGuiToolkit.createTableComboForComponents(groupSearchType);
        comboComp = comboCompViewer.getTableCombo();
        comboCompData = new GridData();
        comboCompData.horizontalSpan = 2;
        comboCompData.widthHint = 350;
        comboComp.setLayoutData(comboCompData);        
        
        comboComp.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                onCompSelection();
            }
        });
        
        comboComp.getTable().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onCompSelection();
            }
            
        });
        
        lableField = safrGuiToolkit.createLabel(groupSearchType,
            SWT.NONE, "LR F&ield:");
        fldLabelData = new GridData();
        lableField.setLayoutData(fldLabelData);
        lableField.setEnabled(false);
        
        comboFieldViewer = safrGuiToolkit.createTableComboForComponents(groupSearchType, ComponentType.LogicalRecordField);
        comboField = comboFieldViewer.getTableCombo();
        fldComboData = new GridData();
        fldComboData.horizontalSpan = 2;
        fldComboData.widthHint = 350;
        comboField.setLayoutData(fldComboData);
        comboField.setEnabled(false);                    
        
        comboField.getTable().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onFieldSelection();
            }
            
        });
        
        comboField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                onFieldSelection();
            }
        });
        
        hideFindByComp();
    }

    private void onTypeSelection() {
        refreshComponent();
        if (componentType == ComponentType.LogicalRecord) {
            comboField.setEnabled(true);
            lableField.setEnabled(true);
        }
        else {
            comboField.setEnabled(false);  
            lableField.setEnabled(false);
        }        
        mediator.setSearchButtonState(getSearchButtonState());        
    }
    
    private void onFieldSelection() {
        int sel = comboField.getSelectionIndex();
        if (sel != -1) {
            curField = (LogicalRecordFieldQueryBean)comboField.getTable().getItem(sel).getData();     
        }
        else {
            curField = null;
        }
        mediator.setSearchButtonState(getSearchButtonState());        
    }
    
    private void onCompSelection() {
        int sel = comboComp.getSelectionIndex();
        if (sel != -1) {
            EnvironmentalQueryBean selComp = (EnvironmentalQueryBean)comboComp.getTable().getItem(sel).getData();
            
            // if selection has changed to a component
            if (selComp != null && 
               (curComp == null || selComp.getId() != curComp.getId()) ) {
                curComp = selComp;

                if (componentType == ComponentType.LogicalRecord) {
                    comboField.select(-1);                            
                    populateFieldList((LogicalRecordQueryBean) curComp);
                }
            }
        } else {
            curComp = null;
        }
        mediator.setSearchButtonState(getSearchButtonState());        
    }

    private void refreshComponent() {
        if (compTypeCombo.getSelectionIndex() != selectedComponentType) {
            componentType = (ComponentType) compTypeCombo.getData(
                String.valueOf(compTypeCombo.getSelectionIndex()));
            selectedComponentType = compTypeCombo.getSelectionIndex();
            
            comboComp.select(-1); 
            curComp = null;
            comboField.getTable().removeAll();
            comboField.select(-1);
            mediator.waitCursor();
            populateComponentList();
            mediator.normalCursor();
        }
    }
    
    private void populateComponentList() {
        comboComp.getTable().removeAll();
        comboCompViewer.setSorter(null);
        comboCompViewer.setContentProvider(new ArrayContentProvider());
        try {

            MainTableLabelProvider labelProvider = new MainTableLabelProvider(
                    (ComponentType) componentType) {

                public String getColumnText(Object element, int columnIndex) {

                    switch (columnIndex) {
                    case 2:
                        NumericIdQueryBean bean = (NumericIdQueryBean) element;
                        return UIUtilities.getComboString(bean.getName(), bean
                                .getId());
                    default:
                        return super.getColumnText(element, columnIndex);
                    }
                }
            };
            labelProvider.setInput();
            comboCompViewer.setLabelProvider(labelProvider);
            List<EnvironmentalQueryBean> list = new ArrayList<EnvironmentalQueryBean>();
            if (componentType == ComponentType.LogicalFile) {
                list.addAll(SAFRQuery.queryAllLogicalFiles(mediator.getCurentEnv().getId(),SortType.SORT_BY_NAME));
            } else if (componentType == ComponentType.LogicalRecord) {
                list.addAll(SAFRQuery.queryAllLogicalRecords(mediator.getCurentEnv().getId(), SortType.SORT_BY_NAME));
            } else if (componentType == ComponentType.LookupPath) {
                list.addAll(SAFRQuery.queryAllLookups(mediator.getCurentEnv().getId(), SortType.SORT_BY_NAME));
            } else if (componentType == ComponentType.PhysicalFile) {
                list.addAll(SAFRQuery.queryAllPhysicalFiles(mediator.getCurentEnv().getId(),SortType.SORT_BY_NAME));
            } else if (componentType == ComponentType.UserExitRoutine) {
                list.addAll(SAFRQuery.queryAllUserExitRoutines(mediator.getCurentEnv().getId(),SortType.SORT_BY_NAME));
            }

            comboCompViewer.setInput(list);
            
            // organize sorting for the table
            int iCounter;
            for (iCounter = 0; iCounter < 2; iCounter++) {
                if (prvsListner[iCounter] != null) {
                    comboComp.getTable().getColumn(iCounter).removeSelectionListener(prvsListner[iCounter]);
                }
                ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(
                        iCounter, comboCompViewer, componentType);
                prvsListner[iCounter] = colListener;
                comboComp.getTable().getColumn(iCounter).addSelectionListener(colListener);
            }
            comboCompViewer.refresh();

        } catch (DAOException e) {
            UIUtilities.handleWEExceptions(e,
                "Unexpected database error occured while retrieving metadata list of selected component.",
                UIUtilities.titleStringDbException);
        }
    }
    
    private void populateFieldList(LogicalRecordQueryBean lrBean) {
        comboField.getTable().removeAll();
        comboFieldViewer.setContentProvider(new ArrayContentProvider());
        try {

            MainTableLabelProvider labelProvider = new MainTableLabelProvider(ComponentType.LogicalRecordField) {

                public String getColumnText(Object element, int columnIndex) {

                    switch (columnIndex) {
                    case 2:
                        NumericIdQueryBean bean = (NumericIdQueryBean) element;
                        return UIUtilities.getComboString(bean.getName(), bean
                                .getId());
                    default:
                        return super.getColumnText(element, columnIndex);
                    }
                }
            };
            labelProvider.setInput();
            comboFieldViewer.setLabelProvider(labelProvider);
            List<LogicalRecordFieldQueryBean> list = SAFRQuery.queryAllLogicalRecordFields(
                mediator.getCurentEnv().getId(), lrBean.getId(), SortType.SORT_BY_NAME);

            comboFieldViewer.setInput(list);
            comboFieldViewer.refresh();

        } catch (DAOException e) {
            UIUtilities.handleWEExceptions(e,
                "Unexpected database error occured while retrieving metadata list of LR Fields.",
                UIUtilities.titleStringDbException);
        }
    }
    
    protected Control getControlFromProperty(Object property) {
        if (property == FindReplaceText.Property.FINDWHAT) {
            return textFind;
        }
        else {
            return null;
        }        
    }
    
    protected boolean isFindByText() {
        return isFindByText;
    }

    protected ComponentType getComponentType() {
        return componentType;
    }

    protected EnvironmentalQueryBean getCurComp() {
        return curComp;
    }

    protected LogicalRecordFieldQueryBean getCurField() {
        return curField;
    }
    
    protected String getFindText() {
        if (isFindByText) {
            if (textFind.getText() == null) {
                return "";
            }
            else {
                return textFind.getText().trim();
            }
        }
        else {
            if (componentType == ComponentType.LogicalRecord) {
                if (curField == null) {
                    return "";
                }
                else {
                    return curField.getNameLabel();                    
                }
            }            
            else {
                if (curComp == null) {
                    return "";                    
                }
                else {
                    return curComp.getNameLabel();                                        
                }                    
            }
        }
    }

    public boolean isMatchingCase() {
        return checkMatchCase.getSelection();
    }

    public boolean isPatternMatch() {
        return checkUsePattern.getSelection();
    }

    public Control getBottomFindBy() {
        return groupSearchType;
    }
    
    
}
