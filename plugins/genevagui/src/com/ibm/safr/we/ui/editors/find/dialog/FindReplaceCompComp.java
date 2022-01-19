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


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.NumericIdQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.ui.utilities.ColumnSelectionListenerForTableCombo;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.MainTableLabelProvider;

public class FindReplaceCompComp {
    
    private FindReplaceCompMediator mediator;
    private SAFRGUIToolkit safrGUIToolkit;
    private TableComboViewer comboComponentViewer;
    private TableCombo comboComp;    
    private GridData compComboData;
    private Label lableComp;
    private GridData compLabelData;
    private String selectedCompName = null;
    private ColumnSelectionListenerForTableCombo[] prvsListner = new ColumnSelectionListenerForTableCombo[2];
    
    public FindReplaceCompComp() {
        safrGUIToolkit = new SAFRGUIToolkit();      
    }

    public void setMediator(FindReplaceCompMediator mediator) {
        this.mediator = mediator;
    }    
    
    protected void create() {
        lableComp = safrGUIToolkit.createLabel(mediator.getComposite(),
            SWT.NONE, "Compo&nent:");
        compLabelData = new GridData();
        lableComp.setLayoutData(compLabelData);
        
        comboComponentViewer = safrGUIToolkit.createTableComboForComponents(mediator.getComposite());
        comboComp = comboComponentViewer.getTableCombo();
        compComboData = new GridData(GridData.FILL_HORIZONTAL);
        compComboData.horizontalSpan = 1;
        comboComp.setLayoutData(compComboData);
        
        comboComp.addFocusListener(new FocusAdapter() {
    
            @Override
            public void focusLost(FocusEvent e) {
                selectedCompName = comboComp.getText();
            }
    
            @Override
            public void focusGained(FocusEvent e) {
    
                if (mediator.isComponentTypeSelected()) {
                    mediator.setErrorMessage("Select component type first.");
                } else {
                    mediator.setMessage("Select component.");
                }
            }
        });
    }
    
    public void populateComponentList() {
        comboComp.getTable().removeAll();
        comboComponentViewer.setSorter(null);
        comboComponentViewer.setContentProvider(new ArrayContentProvider());
        try {

            MainTableLabelProvider labelProvider = new MainTableLabelProvider(
                    (ComponentType) mediator.getComponentType()) {

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
            comboComponentViewer.setLabelProvider(labelProvider);
            List<EnvironmentalQueryBean> list = new ArrayList<EnvironmentalQueryBean>();
            if (mediator.getComponentType() == ComponentType.LogicalFile) {
                list.addAll(SAFRQuery.queryAllLogicalFiles(mediator.getEnvironmentId(),SortType.SORT_BY_NAME));
            } else if (mediator.getComponentType() == ComponentType.LookupPath) {
                list.addAll(SAFRQuery.queryAllLookups(mediator.getEnvironmentId(), SortType.SORT_BY_NAME));
            } else if (mediator.getComponentType() == ComponentType.PhysicalFile) {
                list.addAll(SAFRQuery.queryAllPhysicalFiles(mediator.getEnvironmentId(),SortType.SORT_BY_NAME));
            } else if (mediator.getComponentType() == ComponentType.UserExitRoutine) {
                list.addAll(SAFRQuery.queryAllUserExitRoutines(mediator.getEnvironmentId(),SortType.SORT_BY_NAME));
            }

            comboComponentViewer.setInput(list);
            
            // organize sorting for the table
            int iCounter;
            for (iCounter = 0; iCounter < 2; iCounter++) {
                if (prvsListner[iCounter] != null) {
                    comboComp.getTable().getColumn(iCounter).removeSelectionListener(prvsListner[iCounter]);
                }
                ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(
                        iCounter, comboComponentViewer, mediator.getComponentType());
                prvsListner[iCounter] = colListener;
                comboComp.getTable().getColumn(iCounter).addSelectionListener(colListener);
            }
            comboComponentViewer.refresh();

        } catch (DAOException e) {
            UIUtilities.handleWEExceptions(e,
                "Unexpected database error occured while retrieving metadata list of selected component.",
                UIUtilities.titleStringDbException);
        }
    }

    protected String getSelectedComponentName() {
        return selectedCompName;
    }

    protected void clearComponent() {
        comboComp.select(-1);
        selectedCompName = null;
    }
    
    protected void hideComponent() {
        compLabelData.exclude = true;
        lableComp.setVisible(false);
        compComboData.exclude = true;
        comboComp.setVisible(false);
        mediator.getComposite().pack();        
    }

    protected void showComponent() {
        compLabelData.exclude = false;
        lableComp.setVisible(true);
        compComboData.exclude = false;
        comboComp.setVisible(true);
        mediator.getComposite().pack();        
    }
    
}
