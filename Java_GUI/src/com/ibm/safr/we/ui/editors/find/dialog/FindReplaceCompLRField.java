package com.ibm.safr.we.ui.editors.find.dialog;

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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.model.query.LogicalRecordFieldQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.NumericIdQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.ui.utilities.ColumnSelectionListenerForTableCombo;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.MainTableLabelProvider;

public class FindReplaceCompLRField {

    private FindReplaceCompMediator mediator;
    private SAFRGUIToolkit safrGUIToolkit;
    private Label lableLR;
    private GridData lrLabelData;
    private TableComboViewer comboLRViewer;
    private TableCombo comboLR; 
    private GridData lrComboData;
    
    private Label lableField;
    private GridData fldLabelData;
    private TableComboViewer comboFieldViewer;
    private TableCombo comboField; 
    private GridData fldComboData;
    private String selectedLRFieldName = null;
    
    public FindReplaceCompLRField() {
        safrGUIToolkit = new SAFRGUIToolkit();        
    }
    
    public void setMediator(FindReplaceCompMediator mediator) {
        this.mediator = mediator;
    }    
    
    protected void create() {
        lableLR = safrGUIToolkit.createLabel(mediator.getComposite(),
            SWT.NONE, "Logical &Record:");
        lrLabelData = new GridData();
        lableLR.setLayoutData(lrLabelData);

        comboLRViewer = safrGUIToolkit.createTableComboForComponents(mediator.getComposite(), ComponentType.LogicalRecord);
        comboLR = comboLRViewer.getTableCombo();
        lrComboData = new GridData(GridData.FILL_HORIZONTAL);
        lrComboData.horizontalSpan = 1;
        comboLR.setLayoutData(lrComboData);
    
        // add listener
        comboLR.getTable().addSelectionListener(new SelectionListener() {
    
            public void widgetDefaultSelected(SelectionEvent e) {
            }
    
            public void widgetSelected(SelectionEvent e) {
                // populate the LR field list
                LogicalRecordQueryBean bean = (LogicalRecordQueryBean)e.item.getData();
                if (bean != null) {
                    populateFieldList(bean);
                }
            }
    
        });
    
        comboLR.addFocusListener(new FocusAdapter() {
    
            @Override
            public void focusLost(FocusEvent e) {
            }
    
            @Override
            public void focusGained(FocusEvent e) {
    
                if (mediator.isComponentTypeSelected()) {
                    mediator.setErrorMessage("Select component type first.");
                } else {
                    mediator.setMessage("Select logical record.");
                }
            }
        });

        int iCounter;
        for (iCounter = 0; iCounter < 2; iCounter++) {
            ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(
                iCounter, comboLRViewer, ComponentType.LogicalRecord);
            comboLR.getTable().getColumn(iCounter).addSelectionListener(colListener);
        }
                
        lableField = safrGUIToolkit.createLabel(mediator.getComposite(),
            SWT.NONE, "LR &Field:");
        fldLabelData = new GridData();
        lableField.setLayoutData(fldLabelData);
        
        comboFieldViewer = safrGUIToolkit.createTableComboForComponents(mediator.getComposite(), ComponentType.LogicalRecordField);
        comboField = comboFieldViewer.getTableCombo();
        fldComboData = new GridData(GridData.FILL_HORIZONTAL);
        fldComboData.horizontalSpan = 1;
        comboField.setLayoutData(fldComboData);
        
        comboField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                selectedLRFieldName = comboField.getText();
            }
        });
    }    
    
    public void populateLRList() {
        comboLR.getTable().removeAll();
        comboLRViewer.setContentProvider(new ArrayContentProvider());
        try {

            MainTableLabelProvider labelProvider = new MainTableLabelProvider(ComponentType.LogicalRecord) {

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
            comboLRViewer.setLabelProvider(labelProvider);
            List<LogicalRecordQueryBean> list = SAFRQuery
                 .queryAllLogicalRecords(mediator.getEnvironmentId(),SortType.SORT_BY_NAME);            
            comboLRViewer.setInput(list);
            comboLRViewer.refresh();

        } catch (DAOException e) {
            UIUtilities.handleWEExceptions(e,
                "Unexpected database error occured while retrieving metadata list of logical records.",
                UIUtilities.titleStringDbException);
        }
    }

    public void populateFieldList(LogicalRecordQueryBean lrBean) {
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
                mediator.getEnvironmentId(), lrBean.getId(), SortType.SORT_BY_NAME);

            comboFieldViewer.setInput(list);
            comboFieldViewer.refresh();

        } catch (DAOException e) {
            UIUtilities.handleWEExceptions(e,
                "Unexpected database error occured while retrieving metadata list of LR Fields.",
                UIUtilities.titleStringDbException);
        }
    }
    
    protected void clearLRField() {
        comboLR.select(-1);        
        comboField.getTable().removeAll();
        comboField.select(-1);
        selectedLRFieldName = null;
    }
    
    protected void hideLRField() {
        lrLabelData.exclude = true;
        lableLR.setVisible(false);
        lrComboData.exclude = true;
        comboLR.setVisible(false);
        fldLabelData.exclude = true;
        lableField.setVisible(false);
        fldComboData.exclude = true;
        comboField.setVisible(false);        
        mediator.getComposite().pack();        
    }

    protected void showLRField() {
        lrLabelData.exclude = false;
        lableLR.setVisible(true);
        lrComboData.exclude = false;
        comboLR.setVisible(true);
        fldLabelData.exclude = false;
        lableField.setVisible(true);
        fldComboData.exclude = false;
        comboField.setVisible(true);        
        mediator.getComposite().pack();        
    }

    protected String getSelectedLRFieldName() {
        return selectedLRFieldName;
    }
}
