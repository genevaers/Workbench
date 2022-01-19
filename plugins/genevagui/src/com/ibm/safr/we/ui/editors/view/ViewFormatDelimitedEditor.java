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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ViewFormatDelimitedEditor {

    static final Logger logger = Logger
    .getLogger("com.ibm.safr.we.ui.editors.view.ViewFormatDelimitedEditor");
    
    // member variables
    private FormToolkit toolkit;
    SAFRGUIToolkit safrToolkit;    
    private ViewMediator mediator;
    private CTabFolder parent;
    private View view;
	private SAFRGUIToolkit safrGuiToolkit = null;
    private Composite compositeFormatDelimited;
    private CTabItem tabFormatDelimited;
    
    private Composite compositeDelimited;     
    private Label labelFField;
    private Label labelFString;
    private Combo comboFField;
    private Combo comboFString;
    
	private Button headerRow;
    private Boolean toggleAccKey_l_Fmt = true;
    private Boolean toggleAccKey_s = true;
    
    public ViewFormatDelimitedEditor(ViewMediator mediator, CTabFolder parent, View view) {
        this.mediator = mediator;
        this.parent = parent;
        this.view = view;
    }

    
    protected void show() {
        if (tabFormatDelimited == null || tabFormatDelimited.isDisposed()) {
            createTabFormatDelimited();
        }
        
        // refresh details
        //textLinesPerPage.setText(view.getLinesPerPage() == null ? "0" : view.getLinesPerPage().toString());
        //textReportWidth.setText(view.getReportWidth() == null ? "0" : view.getReportWidth().toString());
    }   
    
    private void createTabFormatDelimited() {
        toolkit = new FormToolkit(parent.getDisplay());
        safrToolkit = new SAFRGUIToolkit(toolkit);
        safrToolkit.setReadOnly(mediator.getViewInput().getEditRights() == EditRights.Read);
        
        compositeFormatDelimited = safrToolkit.createComposite(parent, SWT.NONE);
        FormLayout layoutComposite = new FormLayout();
        compositeFormatDelimited.setLayout(layoutComposite);

        FormData dataComposite = new FormData();
        dataComposite.left = new FormAttachment(0, 0);
        dataComposite.right = new FormAttachment(100, 0);
        dataComposite.top = new FormAttachment(0, 0);
        dataComposite.bottom = new FormAttachment(100, 0);
        compositeFormatDelimited.setLayoutData(dataComposite);

        createCompositeDelimiters();        
        tabFormatDelimited = new CTabItem(parent, SWT.NONE);
        tabFormatDelimited.setText("&Delimited");
        tabFormatDelimited.setControl(compositeFormatDelimited);
    }

    private void createCompositeDelimiters() {
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
        compositeDelimited = mediator.getGUIToolKit().createComposite(
            compositeFormatDelimited, SWT.NONE);
        compositeDelimited.setLayout(new FormLayout());
        FormData dataCompositeDetails = new FormData();
        dataCompositeDetails.top = new FormAttachment(0, 10);
        compositeDelimited.setLayoutData(dataCompositeDetails);

        headerRow =  mediator.getGUIToolKit().createCheckBox(compositeDelimited,"Include a header row");
        headerRow.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                System.out.println(headerRow.getSelection());
                view.setHeaderRow(headerRow.getSelection());
                mediator.setModified(true);
            }

        });

        Section sectionDelimiters = safrToolkit.createSection(
            compositeDelimited, Section.TITLE_BAR | Section.DESCRIPTION,"Delimiters");
        sectionDelimiters.getDescriptionControl().setEnabled(false);
        FormData dataSectionDelimiters = new FormData();
        dataSectionDelimiters.top = new FormAttachment(headerRow, 10);
        dataSectionDelimiters.left = new FormAttachment(0, 10);
        dataSectionDelimiters.right = new FormAttachment(100, 0);
        sectionDelimiters.setLayoutData(dataSectionDelimiters);

        Composite compositeDelimiters = safrToolkit.createComposite(sectionDelimiters, SWT.NONE);
        compositeDelimiters.setLayout(new FormLayout());
        
        sectionDelimiters.setClient(compositeDelimiters);
        
        labelFField = mediator.getGUIToolKit().createLabel(compositeDelimiters, SWT.NONE,"Fie&ld: ");
        labelFField.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_l_Fmt) {
                        e.doit = false;
                        toggleAccKey_l_Fmt = false;
                    }
                }
            }

        });

        comboFField = mediator.getGUIToolKit().createComboBox(compositeDelimiters,SWT.READ_ONLY, "");
        comboFField.setData(SAFRLogger.USER, "Delimiter Field");                              
        

        labelFString = mediator.getGUIToolKit().createLabel(compositeDelimiters, SWT.NONE,"&String: ");
        labelFString.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_s) {
                        e.doit = false;
                        toggleAccKey_s = false;
                    }
                }
            }

        });

        comboFString = mediator.getGUIToolKit().createComboBox(compositeDelimiters,
                SWT.READ_ONLY, "");
        comboFString.setData(SAFRLogger.USER, "Delimiter string");                                

        FormData dataLabelFField = new FormData();
        dataLabelFField.top = new FormAttachment(0, 10);
        dataLabelFField.left = new FormAttachment(0, 10);
        labelFField.setLayoutData(dataLabelFField);

        FormData dataComboFField = new FormData();
        dataComboFField.top = new FormAttachment(0, 10);
        dataComboFField.left = new FormAttachment(labelFString, 10);
        dataComboFField.right = new FormAttachment(100, -10);
        comboFField.setLayoutData(dataComboFField);
        comboFField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                Code code = (Code) comboFField.getData(Integer.toString(comboFField.getSelectionIndex()));
                if (!UIUtilities.isEqual(view.getFileFieldDelimiterCode(), code)) {
                    if (code != null) {
                        view.setFileFieldDelimiterCode(code);
                    }
                    mediator.setModified(true);
                }
            }
        });
        UIUtilities.populateComboBox(comboFField, CodeCategories.FLDDELIM, 0,
                false);

        FormData dataLabelFString = new FormData();
        dataLabelFString.top = new FormAttachment(comboFField, 10);
        dataLabelFString.left = new FormAttachment(0, 10);
        labelFString.setLayoutData(dataLabelFString);

        FormData dataComboFString = new FormData();
        dataComboFString.top = new FormAttachment(comboFField, 10);
        dataComboFString.left = new FormAttachment(labelFString, 10);
        dataComboFString.right = new FormAttachment(100, -10);
        dataComboFString.bottom = new FormAttachment(100, -10);
        comboFString.setLayoutData(dataComboFString);
        comboFString.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);

                Code code = (Code) comboFString.getData(Integer.toString(comboFString.getSelectionIndex()));
                if (!UIUtilities.isEqual(view.getFileStringDelimiterCode(),code)) {
                    if (code != null) {
                        view.setFileStringDelimiterCode(code);
                    }
                    mediator.setModified(true);
                }
            }

        });

        UIUtilities.populateComboBox(comboFString, CodeCategories.STRDELIM, 0, false);

        /*
         * End of section Delimiters
         */
    }
    
    protected void hide() {
        if (tabFormatDelimited != null) {
            if (!tabFormatDelimited.isDisposed()) {
                tabFormatDelimited.dispose();
            }
            tabFormatDelimited = null;
        }
    }
    
    protected void loadDelimitersSection() {
        UIUtilities.checkNullCombo(comboFString, view
                .getFileStringDelimiterCode());
        UIUtilities.checkNullCombo(comboFField, view
                .getFileFieldDelimiterCode());
        
        if (view.isHeaderRow() != null) {
            if (view.isHeaderRow()) {
                headerRow.setSelection(true);
            } else {
            	headerRow.setSelection(false);
            }
        }
    }

    
}
