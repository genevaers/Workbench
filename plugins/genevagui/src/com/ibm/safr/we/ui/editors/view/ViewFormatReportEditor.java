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


import java.util.List;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.view.HeaderFooterItem;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.ui.utilities.CommentsTraverseListener;
import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ViewFormatReportEditor {

    static final Logger logger = Logger
    .getLogger("com.ibm.safr.we.ui.editors.view.ViewFormatReportEditor");

    private static final int MAX_LINES_PER_PAGE = 9;
    private static final int MAX_REPORT_WIDTH = 9;
        
    protected enum HFFocus {
        NONE, HEADER_LEFT, HEADER_CENTER, HEADER_RIGHT, FOOTER_LEFT, FOOTER_CENTER, FOOTER_RIGHT
    }
    
    
    // member variables
    private FormToolkit toolkit;
    SAFRGUIToolkit safrToolkit;    
    private ViewMediator mediator;
    private CTabFolder parent;
    private View view;

    private Composite compositeFormatReport;
    private CTabItem tabFormatReport;
    
    private Composite compositeDetails;     
    private Label labelLinesPerPage;
    private Label labelReportWidth;
    private Text textLinesPerPage;
    private Text textReportWidth;
    
    private Composite compositeHeaderFooter;     
    private Button buttonProcessDate;
    private String stringProcessDate;
    private Button buttonProcessTime;
    private String stringProcessTime;
    private Button buttonPageNo;
    private String stringPageNo;
    private Button buttonViewId;
    private String stringViewId;
    private Combo comboHFFunctions;
    private Button buttonAddHFFunction;
    private StyledText textHeaderLeft;
    private StyledText textHeaderCenter;
    private StyledText textHeaderRight;
    private StyledText textFooterLeft;
    private StyledText textFooterCenter;
    private StyledText textFooterRight;
        
    View.HeaderFooterItems header = null;
    View.HeaderFooterItems footer = null;
    String[] headerText = null;
    String[] footerText = null;
    
    private HFFocus hfFocus = HFFocus.NONE;
    
    private Boolean toggleAccKey_l_select = true;
    private Boolean toggleAccKey_c_select = true;
    private Boolean toggleAccKey_r_select = true;
        
    protected ViewFormatReportEditor(ViewMediator mediator, CTabFolder parent, View view) {
        this.mediator = mediator;
        this.parent = parent;
        this.view = view;
    }
    
    private void createTabFormatReport() {
        toolkit = new FormToolkit(parent.getDisplay());
        safrToolkit = new SAFRGUIToolkit(toolkit);
        safrToolkit.setReadOnly(mediator.getViewInput().getEditRights() == EditRights.Read);
        
        compositeFormatReport = safrToolkit.createComposite(parent, SWT.NONE);
        FormLayout layoutComposite = new FormLayout();
        compositeFormatReport.setLayout(layoutComposite);

        FormData dataComposite = new FormData();
        dataComposite.left = new FormAttachment(0, 0);
        dataComposite.right = new FormAttachment(100, 0);
        dataComposite.top = new FormAttachment(0, 0);
        dataComposite.bottom = new FormAttachment(100, 0);
        compositeFormatReport.setLayoutData(dataComposite);

        createCompositeDetails();        
        createCompositeHeaderFooter();
        tabFormatReport = new CTabItem(parent, SWT.NONE);
        tabFormatReport.setText("&Report");
        tabFormatReport.setControl(compositeFormatReport);
    }
    
    // This method is used to set the focus the user selected before traversing
    // through the tabs.
    protected void setHFFocus() {
        if (hfFocus.equals(HFFocus.HEADER_LEFT)) {
            textHeaderLeft.setFocus();
        } else if (hfFocus.equals(HFFocus.HEADER_CENTER)) {
            textHeaderCenter.setFocus();
        } else if (hfFocus.equals(HFFocus.HEADER_RIGHT)) {
            textHeaderRight.setFocus();
        } else if (hfFocus.equals(HFFocus.FOOTER_LEFT)) {
            textFooterLeft.setFocus();
        } else if (hfFocus.equals(HFFocus.FOOTER_CENTER)) {
            textFooterCenter.setFocus();
        } else if (hfFocus.equals(HFFocus.FOOTER_RIGHT)) {
            textFooterRight.setFocus();
        }
    }

    protected void setHeaderFooterFocus(HFFocus focus) {
        hfFocus = focus;
    }
    
    /**
     * This method creates the Header/Footer tab (which is used only in the case
     * of Hardcopy Reports)
     */
    private void createCompositeHeaderFooter() {

        compositeHeaderFooter = mediator.getGUIToolKit().createComposite(
            compositeFormatReport, SWT.NONE);
        compositeHeaderFooter.setLayout(new FormLayout());
        FormData dataCompositeHeaderFooter = new FormData();
        dataCompositeHeaderFooter.top = new FormAttachment(compositeDetails, 10);
        compositeHeaderFooter.setLayoutData(dataCompositeHeaderFooter);
        
        /*
         * section Functions
         */
        Section sectionHeaderFooterFunctions = safrToolkit.createSection(
            compositeHeaderFooter, Section.TITLE_BAR | Section.DESCRIPTION,"Header/Footer");
        sectionHeaderFooterFunctions.setDescription(
            "To insert functions such as a date, time, page number, view id or text: position the insertion point in the edit box, then choose the appropriate button or select from the list.");
        sectionHeaderFooterFunctions.getDescriptionControl().setEnabled(false);
        FormData dataSectionFunctions = new FormData();
        dataSectionFunctions.top = new FormAttachment(0, 10);
        dataSectionFunctions.left = new FormAttachment(0, 10);
        dataSectionFunctions.right = new FormAttachment(100, 0);
        sectionHeaderFooterFunctions.setLayoutData(dataSectionFunctions);

        Composite compositeHeaderFooterFunctions = safrToolkit.createComposite(sectionHeaderFooterFunctions, SWT.NONE);
        compositeHeaderFooterFunctions.setLayout(new FormLayout());

        sectionHeaderFooterFunctions.setClient(compositeHeaderFooterFunctions);

        buttonProcessDate = safrToolkit.createButton(compositeHeaderFooterFunctions, SWT.PUSH, "Da&te");
        buttonProcessDate.setData(SAFRLogger.USER, "H/F Date");

        buttonProcessDate.setToolTipText("Insert Process Date");
        FormData dataProcessDate = new FormData();
        dataProcessDate.left = new FormAttachment(0, 10);
        dataProcessDate.top = new FormAttachment(0, 10);
        // dataProcessDate.width = FUNC_BUTTON_WIDTH;
        // dataProcessDate.height = FUNC_BUTTON_HEIGHT;
        buttonProcessDate.setLayoutData(dataProcessDate);
        buttonProcessDate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                insertHFFunction(stringProcessDate);
                if (hfFocus != HFFocus.NONE) {
                    mediator.setModified(true);
                }
            }
        });

        buttonProcessTime = safrToolkit.createButton(
                compositeHeaderFooterFunctions, SWT.PUSH, "Ti&me");
        buttonProcessTime.setData(SAFRLogger.USER, "H/F Time");        
        buttonProcessTime.setToolTipText("Insert Process Time");
        FormData dataProcessTime = new FormData();
        dataProcessTime.left = new FormAttachment(buttonProcessDate, 10);
        dataProcessTime.top = new FormAttachment(0, 10);
        // dataProcessTime.width = FUNC_BUTTON_WIDTH;
        // dataProcessTime.height = FUNC_BUTTON_HEIGHT;
        buttonProcessTime.setLayoutData(dataProcessTime);
        buttonProcessTime.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                insertHFFunction(stringProcessTime);
                if (hfFocus != HFFocus.NONE) {
                    mediator.setModified(true);
                }
            }
        });

        buttonPageNo = safrToolkit.createButton(compositeHeaderFooterFunctions,
                SWT.PUSH, "&Page");
        buttonPageNo.setData(SAFRLogger.USER, "H/F Page");        
        buttonPageNo.setToolTipText("Insert Page Number");
        FormData dataPageNo = new FormData();
        dataPageNo.left = new FormAttachment(buttonProcessTime, 10);
        dataPageNo.top = new FormAttachment(0, 10);
        // dataPageNo.width = FUNC_BUTTON_WIDTH;
        // dataPageNo.height = FUNC_BUTTON_HEIGHT;
        buttonPageNo.setLayoutData(dataPageNo);
        buttonPageNo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                insertHFFunction(stringPageNo);
                if (hfFocus != HFFocus.NONE) {
                    mediator.setModified(true);
                }
            }
        });

        buttonViewId = safrToolkit.createButton(compositeHeaderFooterFunctions,
                SWT.PUSH, "&View");
        buttonViewId.setData(SAFRLogger.USER, "H/F View");        
        
        buttonViewId.setToolTipText("Insert View ID");
        FormData dataViewId = new FormData();
        dataViewId.left = new FormAttachment(buttonPageNo, 10);
        dataViewId.top = new FormAttachment(0, 10);
        // dataViewId.width = FUNC_BUTTON_WIDTH;
        // dataViewId.height = FUNC_BUTTON_HEIGHT;
        buttonViewId.setLayoutData(dataViewId);
        buttonViewId.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                insertHFFunction(stringViewId);
                if (hfFocus != HFFocus.NONE) {
                    mediator.setModified(true);
                }
            }
        });

        comboHFFunctions = safrToolkit.createComboBox(
                compositeHeaderFooterFunctions, SWT.READ_ONLY, "");
        comboHFFunctions.setData(SAFRLogger.USER, "H/F Functions");        
        FormData dataComboFunctions = new FormData();
        dataComboFunctions.left = new FormAttachment(buttonViewId, 50);
        dataComboFunctions.top = new FormAttachment(buttonViewId, 10,
                SWT.CENTER);
        dataComboFunctions.width = 200;

        comboHFFunctions.setLayoutData(dataComboFunctions);
        // int height = (dataViewId.height/2)-(dataComboFunctions.height/2);

        buttonAddHFFunction = safrToolkit.createButton(
                compositeHeaderFooterFunctions, SWT.PUSH, "&Insert");
        buttonAddHFFunction.setData(SAFRLogger.USER, "H/F Insert");        
        
        buttonAddHFFunction.setToolTipText("Insert Function");
        FormData dataAddHFFunc = new FormData();
        dataAddHFFunc.left = new FormAttachment(comboHFFunctions, 10);
        dataAddHFFunc.top = new FormAttachment(comboHFFunctions, 10, SWT.CENTER);
        // dataAddHFFunc.width = FUNC_BUTTON_WIDTH;
        // dataAddHFFunc.height = FUNC_BUTTON_HEIGHT;
        buttonAddHFFunction.setLayoutData(dataAddHFFunc);
        buttonAddHFFunction.setImage(UIUtilities
                .getImageDescriptor(ImageKeys.INSERT));
        if (comboHFFunctions.getText().equals("")) {
            buttonAddHFFunction.setEnabled(false);
        }
        comboHFFunctions.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {

                if (!(comboHFFunctions.getText().equals(""))) {
                    buttonAddHFFunction.setEnabled(true);
                } else {
                    buttonAddHFFunction.setEnabled(false);
                }

            }
        });

        buttonAddHFFunction.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                String selectedFunction = comboHFFunctions.getText();

                if (!selectedFunction.equals("")) {
                    insertHFFunction(selectedFunction);
                    if (hfFocus != HFFocus.NONE) {
                        mediator.setModified(true);
                    }
                }
            }

        });

        

        /*
         * section Header
         */
        Section sectionHeader = safrToolkit.createSection(
                compositeHeaderFooter, Section.TITLE_BAR, "Header");        
        FormData dataSectionHeader = new FormData();
        dataSectionHeader.top = new FormAttachment(
                sectionHeaderFooterFunctions, 10);
        dataSectionHeader.left = new FormAttachment(0, 10);
        dataSectionHeader.right = new FormAttachment(100, 0);
        sectionHeader.setLayoutData(dataSectionHeader);

        Composite compositeHeader = safrToolkit.createComposite(sectionHeader,
                SWT.NONE);
        compositeHeader.setLayout(new FormLayout());

        sectionHeader.setClient(compositeHeader);

        Label labelHeaderLeft = safrToolkit.createLabel(compositeHeader,
                SWT.NONE, "&Left Selection: ");
        labelHeaderLeft.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_l_select) {
                        e.doit = false;
                        toggleAccKey_l_select = false;
                    } else {
                        toggleAccKey_l_select = true;
                    }
                }
            }

        });
        FormData dataLabelHeaderLeft = new FormData();
        dataLabelHeaderLeft.left = new FormAttachment(0, 10);
        dataLabelHeaderLeft.top = new FormAttachment(0, 10);
        labelHeaderLeft.setLayoutData(dataLabelHeaderLeft);

        textHeaderLeft = new StyledText(compositeHeader, SWT.MULTI | SWT.BORDER
                | SWT.V_SCROLL | SWT.WRAP);
        textHeaderLeft.setData(SAFRLogger.USER, "Header Left");        
        
        FormData dataTextHeaderLeft = new FormData();
        dataTextHeaderLeft.left = new FormAttachment(0, 10);
        dataTextHeaderLeft.top = new FormAttachment(labelHeaderLeft, 10);
        dataTextHeaderLeft.width = 250;
        dataTextHeaderLeft.height = 75;
        textHeaderLeft.setLayoutData(dataTextHeaderLeft);
        textHeaderLeft.setAlignment(SWT.LEFT);
        textHeaderLeft.addModifyListener(mediator.getViewEditor());
        textHeaderLeft.addTraverseListener(new CommentsTraverseListener());
        textHeaderLeft.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqual(headerText[0], textHeaderLeft
                        .getText())) {
                    mediator.setModified(true);
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                hfFocus = HFFocus.HEADER_LEFT;
            }

        });

        Label labelHeaderCenter = safrToolkit.createLabel(compositeHeader,
                SWT.NONE, "&Center Selection: ");
        labelHeaderCenter.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_c_select) {
                        e.doit = false;
                        toggleAccKey_c_select = false;
                    } else {
                        toggleAccKey_c_select = true;
                    }
                }

            }

        });
        FormData dataLabelHeaderCenter = new FormData();
        dataLabelHeaderCenter.left = new FormAttachment(textHeaderLeft, 10);
        dataLabelHeaderCenter.top = new FormAttachment(0, 10);
        labelHeaderCenter.setLayoutData(dataLabelHeaderCenter);

        textHeaderCenter = new StyledText(compositeHeader, SWT.MULTI
                | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        textHeaderCenter.setData(SAFRLogger.USER, "Header Center");        
        
        FormData dataTextHeaderCenter = new FormData();
        dataTextHeaderCenter.left = new FormAttachment(textHeaderLeft, 10);
        dataTextHeaderCenter.top = new FormAttachment(labelHeaderCenter, 10);
        dataTextHeaderCenter.width = 250;
        dataTextHeaderCenter.height = 75;
        textHeaderCenter.setLayoutData(dataTextHeaderCenter);
        textHeaderCenter.setAlignment(SWT.CENTER);
        textHeaderCenter.addModifyListener(mediator.getViewEditor());
        textHeaderCenter.addTraverseListener(new CommentsTraverseListener());
        textHeaderCenter.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqual(headerText[1], textHeaderCenter
                        .getText())) {
                    mediator.setModified(true);
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                hfFocus = HFFocus.HEADER_CENTER;
            }

        });

        Label labelHeaderRight = safrToolkit.createLabel(compositeHeader,
                SWT.NONE, "Right &Selection: ");
        labelHeaderRight.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_r_select) {
                        e.doit = false;
                        toggleAccKey_r_select = false;
                    } else {
                        toggleAccKey_r_select = true;
                    }
                }
            }
        });
        FormData dataLabelHeaderRight = new FormData();
        dataLabelHeaderRight.left = new FormAttachment(textHeaderCenter, 10);
        dataLabelHeaderRight.top = new FormAttachment(0, 10);
        labelHeaderRight.setLayoutData(dataLabelHeaderRight);

        textHeaderRight = new StyledText(compositeHeader, SWT.MULTI
                | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        textHeaderRight.setData(SAFRLogger.USER, "Header Right");        
        
        FormData dataTextHeaderRight = new FormData();
        dataTextHeaderRight.left = new FormAttachment(textHeaderCenter, 10);
        dataTextHeaderRight.top = new FormAttachment(labelHeaderRight, 10);
        dataTextHeaderRight.width = 250;
        dataTextHeaderRight.height = 75;
        textHeaderRight.setLayoutData(dataTextHeaderRight);
        textHeaderRight.setAlignment(SWT.RIGHT);
        textHeaderRight.addModifyListener(mediator.getViewEditor());
        textHeaderRight.addTraverseListener(new CommentsTraverseListener());
        textHeaderRight.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqual(headerText[2], textHeaderRight
                        .getText())) {
                    mediator.setModified(true);
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                hfFocus = HFFocus.HEADER_RIGHT;
            }

        });
        /*
         * end of section Header
         */

        /*
         * section Footer
         */
        Section sectionFooter = safrToolkit.createSection(
                compositeHeaderFooter, Section.TITLE_BAR, "Footer");
        FormData dataSectionFooter = new FormData();
        dataSectionFooter.top = new FormAttachment(sectionHeader, 10);
        dataSectionFooter.left = new FormAttachment(0, 10);
        dataSectionFooter.right = new FormAttachment(100, 0);
        sectionFooter.setLayoutData(dataSectionFooter);

        Composite compositeFooter = safrToolkit.createComposite(sectionFooter,
                SWT.NONE);
        compositeFooter.setLayout(new FormLayout());

        sectionFooter.setClient(compositeFooter);

        Label labelFooterLeft = safrToolkit.createLabel(compositeFooter,
                SWT.NONE, "&Left Selection: ");
        labelFooterLeft.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_l_select) {
                        e.doit = false;
                        toggleAccKey_l_select = false;
                    }
                }

            }
        });
        FormData dataLabelFooterLeft = new FormData();
        dataLabelFooterLeft.left = new FormAttachment(0, 10);
        dataLabelFooterLeft.top = new FormAttachment(0, 10);
        labelFooterLeft.setLayoutData(dataLabelFooterLeft);

        textFooterLeft = new StyledText(compositeFooter, SWT.MULTI | SWT.BORDER
                | SWT.V_SCROLL | SWT.WRAP);
        textFooterLeft.setData(SAFRLogger.USER, "Footer Left");        
        
        FormData dataTextFooterLeft = new FormData();
        dataTextFooterLeft.left = new FormAttachment(0, 10);
        dataTextFooterLeft.top = new FormAttachment(labelFooterLeft, 10);
        dataTextFooterLeft.width = 250;
        dataTextFooterLeft.height = 75;
        textFooterLeft.setLayoutData(dataTextFooterLeft);
        textFooterLeft.setAlignment(SWT.LEFT);
        textFooterLeft.addModifyListener(mediator.getViewEditor());
        textFooterLeft.addTraverseListener(new CommentsTraverseListener());
        textFooterLeft.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqual(footerText[0], textFooterLeft
                        .getText())) {
                    mediator.setModified(true);
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                hfFocus = HFFocus.FOOTER_LEFT;
            }

        });

        Label labelFooterCenter = safrToolkit.createLabel(compositeFooter,
                SWT.NONE, "&Center Selection: ");
        labelFooterCenter.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_c_select) {
                        e.doit = false;
                        toggleAccKey_c_select = false;
                    }
                }

            }

        });
        FormData dataLabelFooterCenter = new FormData();
        dataLabelFooterCenter.left = new FormAttachment(textFooterLeft, 10);
        dataLabelFooterCenter.top = new FormAttachment(0, 10);
        labelFooterCenter.setLayoutData(dataLabelFooterCenter);

        textFooterCenter = new StyledText(compositeFooter, SWT.MULTI
                | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        textFooterCenter.setData(SAFRLogger.USER, "Footer Center");                
        FormData dataTextFooterCenter = new FormData();
        dataTextFooterCenter.left = new FormAttachment(textFooterLeft, 10);
        dataTextFooterCenter.top = new FormAttachment(labelFooterCenter, 10);
        dataTextFooterCenter.width = 250;
        dataTextFooterCenter.height = 75;
        textFooterCenter.setLayoutData(dataTextFooterCenter);
        textFooterCenter.setAlignment(SWT.CENTER);
        textFooterCenter.addModifyListener(mediator.getViewEditor());
        textFooterCenter.addTraverseListener(new CommentsTraverseListener());
        textFooterCenter.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqual(footerText[1], textFooterCenter
                        .getText())) {
                    mediator.setModified(true);
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                hfFocus = HFFocus.FOOTER_CENTER;
            }

        });

        Label labelFooterRight = safrToolkit.createLabel(compositeFooter,
                SWT.NONE, "Right &Selection: ");
        labelFooterRight.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                    if (toggleAccKey_r_select) {
                        e.doit = false;
                        toggleAccKey_r_select = false;
                    }
                }
            }
        });
        FormData dataLabelFooterRight = new FormData();
        dataLabelFooterRight.left = new FormAttachment(textFooterCenter, 10);
        dataLabelFooterRight.top = new FormAttachment(0, 10);
        labelFooterRight.setLayoutData(dataLabelFooterRight);

        textFooterRight = new StyledText(compositeFooter, SWT.MULTI
                | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        textFooterRight.setData(SAFRLogger.USER, "Footer Right");                        
        FormData dataTextFooterRight = new FormData();
        dataTextFooterRight.left = new FormAttachment(textFooterCenter, 10);
        dataTextFooterRight.top = new FormAttachment(labelFooterRight, 10);
        dataTextFooterRight.width = 250;
        dataTextFooterRight.height = 75;
        textFooterRight.setLayoutData(dataTextFooterRight);
        textFooterRight.setAlignment(SWT.RIGHT);
        textFooterRight.addModifyListener(mediator.getViewEditor());
        textFooterRight.addTraverseListener(new CommentsTraverseListener());
        textFooterRight.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqual(footerText[2], textFooterRight
                        .getText())) {
                    mediator.setModified(true);
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                hfFocus = HFFocus.FOOTER_RIGHT;
            }

        });
        /*
         * end of section Footer
         */
    }

    private void createCompositeDetails() {
        compositeDetails = mediator.getGUIToolKit().createComposite(
            compositeFormatReport, SWT.NONE);
        compositeDetails.setLayout(new FormLayout());
        FormData dataCompositeDetails = new FormData();
        dataCompositeDetails.top = new FormAttachment(0, 10);
        compositeDetails.setLayoutData(dataCompositeDetails);

        Section sectionDetails = safrToolkit.createSection(
            compositeDetails, Section.TITLE_BAR | Section.DESCRIPTION,"Details");
        sectionDetails.getDescriptionControl().setEnabled(false);
        FormData dataSectionDetails = new FormData();
        dataSectionDetails.top = new FormAttachment(0, 10);
        dataSectionDetails.left = new FormAttachment(0, 10);
        dataSectionDetails.right = new FormAttachment(100, 0);
        sectionDetails.setLayoutData(dataSectionDetails);

        Composite compositeDetailsInside = safrToolkit.createComposite(sectionDetails, SWT.NONE);
        compositeDetailsInside.setLayout(new FormLayout());
        
        sectionDetails.setClient(compositeDetailsInside);
        
        labelLinesPerPage = mediator.getGUIToolKit().createLabel(compositeDetailsInside,SWT.NONE, "Lines Per Pa&ge:");
        FormData dataLabelLinesPerPage = new FormData();
        dataLabelLinesPerPage.top = new FormAttachment(0, 0);
        dataLabelLinesPerPage.left = new FormAttachment(0, 0);
        labelLinesPerPage.setLayoutData(dataLabelLinesPerPage);

        textLinesPerPage = mediator.getGUIToolKit().createIntegerTextBox(compositeDetailsInside, SWT.NONE, false);
        textLinesPerPage.setData(SAFRLogger.USER, "Lines Per Page");      
        FormData dataTextLinesPerPage = new FormData();
        dataTextLinesPerPage.top = new FormAttachment(0, 0);
        dataTextLinesPerPage.left = new FormAttachment(labelLinesPerPage, 10);
        dataTextLinesPerPage.width = 100;
        textLinesPerPage.setTextLimit(MAX_LINES_PER_PAGE);
        textLinesPerPage.setLayoutData(dataTextLinesPerPage);        
        textLinesPerPage.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                mediator.setModified(true);
            }
        });
        
        textLinesPerPage.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqual(view.getLinesPerPage(), 
                    UIUtilities.stringToInteger(textLinesPerPage.getText()))) {
                    view.setLinesPerPage(UIUtilities.stringToInteger(textLinesPerPage.getText()));
                    mediator.setModified(true);
                }
            }
        });

        labelReportWidth = mediator.getGUIToolKit().createLabel(compositeDetailsInside,
                SWT.NONE, "Report &Width:");
        FormData dataLabelReportWidth = new FormData();
        dataLabelReportWidth.top = new FormAttachment(0, 0);
        dataLabelReportWidth.left = new FormAttachment(textLinesPerPage, 10);
        labelReportWidth.setLayoutData(dataLabelReportWidth);

        textReportWidth = mediator.getGUIToolKit().createIntegerTextBox(compositeDetailsInside, SWT.NONE, false);
        textReportWidth.setData(SAFRLogger.USER, "Report Width");             
        FormData dataTextReportWidth = new FormData();
        dataTextReportWidth.top = new FormAttachment(0, 0);
        dataTextReportWidth.left = new FormAttachment(labelReportWidth, 10);
        dataTextReportWidth.width = 100;        
        textReportWidth.setTextLimit(MAX_REPORT_WIDTH);
        textReportWidth.setLayoutData(dataTextReportWidth);
        textReportWidth.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                mediator.setModified(true);
            }
        });
        
        textReportWidth.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!UIUtilities.isEqual(view.getReportWidth(),
                    UIUtilities.stringToInteger(textReportWidth.getText()))) {
                    view.setReportWidth(UIUtilities.stringToInteger(textReportWidth.getText()));
                    mediator.setModified(true);
                }
            }

        });
    }
    
    /**
     * This function is used to insert the string representing the selected
     * Header/Footer function from the combo box or by clicking the buttons in
     * the appropriate textbox.
     * 
     * @param selectedFunction
     *            the string representing the selected Header/Footer function
     */
    private void insertHFFunction(String selectedFunction) {
        switch (hfFocus) {
        case NONE:
            break;
        case HEADER_LEFT:
            insertHFText(textHeaderLeft, selectedFunction);
            break;
        case HEADER_CENTER:
            insertHFText(textHeaderCenter, selectedFunction);
            break;
        case HEADER_RIGHT:
            insertHFText(textHeaderRight, selectedFunction);
            break;
        case FOOTER_LEFT:
            insertHFText(textFooterLeft, selectedFunction);
            break;
        case FOOTER_CENTER:
            insertHFText(textFooterCenter, selectedFunction);
            break;
        case FOOTER_RIGHT:
            insertHFText(textFooterRight, selectedFunction);
            break;
        }
    }

    /**
     * This function inserts the text in the appropriate Header/Footer textbox
     * 
     * @param textBox
     * @param selectedFunction
     */
    private void insertHFText(StyledText textBox, String selectedFunction) {
        if (textBox.getSelectionText().equals("")) {
            // if user is simply inserting text/function without selecting any
            // text
            textBox.replaceTextRange(textBox.getCaretOffset(), 0,
                    selectedFunction);
            // reset the caret to the end of the inserted text
            textBox.setCaretOffset(textBox.getCaretOffset()
                    + selectedFunction.length());
        } else {
            // if user wants to select a particular string of text and replace
            // it with some other text/function
            textBox.replaceTextRange(textBox.getSelectionRange().x, textBox
                    .getSelectionRange().y, selectedFunction);
        }
    }

    /**
     * This function initializes the strings needed by the buttons for
     * Header/Footer functions.
     */
    private void initializeHFButtonStringValues() {
        CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.FUNCTION);
        Code codeProcessDate = codeSet.getCode(Codes.HF_PROCESSDATE);
        stringProcessDate = "&[" + codeProcessDate.getDescription() + "]";
        Code codeProcessTime = codeSet.getCode(Codes.HF_PROCESSTIME);
        stringProcessTime = "&[" + codeProcessTime.getDescription() + "]";
        Code codePageNo = codeSet.getCode(Codes.HF_PAGENUMBER);
        stringPageNo = "&[" + codePageNo.getDescription() + "]";
        Code codeViewId = codeSet.getCode(Codes.HF_VIEWID);
        stringViewId = "&[" + codeViewId.getDescription() + "]";
    }

    /**
     * This function populates the Header/Footer Functions combo with values
     * from the Code Table and displays the Code description in the format
     * &[...]
     */
    private void populateHFFunctionsCombo() {
        Integer counter = 0;
        comboHFFunctions.removeAll();
        // Allow user to select a blank value
        comboHFFunctions.add("");

        comboHFFunctions.setData(Integer.toString(counter++), null);

        String codeDescription = "";
        List<Code> codeList = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.FUNCTION).getCodes();
        for (Code code : codeList) {
            // CQ 7894. Nikita. 06/05/2010. "TEXT" function should not be shown
            // to the user in the combo box.
            if (code.getGeneralId() != Codes.HF_TEXT) {
                codeDescription = "&[" + code.getDescription() + "]";
                comboHFFunctions.add(codeDescription);
                comboHFFunctions.setData(Integer.toString(counter++), code);
            }
        }
    }

    /**
     * This function splits the string representing the Header/Footer text on
     * the basis of carriage return and line feed characters.
     * 
     * @param headerFooter
     *            the HeaderFooter object
     * @param stringLeft
     *            text from the left selection (header or footer) textbox
     * @param stringCenter
     *            text from the center selection (header or footer) textbox
     * @param stringRight
     *            text from the right selection (header or footer) textbox
     */
    private void splitHeaderFooterStringIntoRows(
            View.HeaderFooterItems headerFooter, String stringLeft,
            String stringCenter, String stringRight) {
        int colNumber = 1;

        CodeSet justifyCodeSet = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.JUSTIFY);
        Code justifyLeft = justifyCodeSet.getCode(Codes.LEFT);
        Code justifyCenter = justifyCodeSet.getCode(Codes.CENTER);
        Code justifyRight = justifyCodeSet.getCode(Codes.RIGHT);

        // regex is the string to be passed as regular expression in the
        // parameter to the split function
        String regEx = "\\r\\n";
        String[] leftRowArray = stringLeft.split(regEx);
        String[] centerRowArray = stringCenter.split(regEx);
        String[] rightRowArray = stringRight.split(regEx);

        String rowString = "";

        // the number of rows that will be stored will be the maximum of the
        // number of rows in the left, center and right sections
        int tempMax = leftRowArray.length > centerRowArray.length ? leftRowArray.length
                : centerRowArray.length;
        int numOfRows = tempMax > rightRowArray.length ? tempMax
                : rightRowArray.length;

        // CQ 7915. Nikita. 06/05/2010. Removal of redundant HF items (of type
        // Text of length 0) being stored in database.

        // don't store empty strings unless they are blank lines, in which case
        // the length of the array would be greater than 1 after splitting.
        // reset the arrays for this purpose.
        if (leftRowArray.length == 1 && leftRowArray[0].equals("")) {
            leftRowArray = new String[0];
        }
        if (centerRowArray.length == 1 && centerRowArray[0].equals("")) {
            centerRowArray = new String[0];
        }
        if (rightRowArray.length == 1 && rightRowArray[0].equals("")) {
            rightRowArray = new String[0];
        }

        // parse the entire text representing left, center and right selections
        // together. column number is carried over from one selection textbox to
        // another for each row.
        for (int rowNumber = 0; rowNumber < numOfRows; rowNumber++) {
            colNumber = 1;
            // if there is text in the current row for this selection
            if (leftRowArray.length > rowNumber) {
                rowString = leftRowArray[rowNumber];
                colNumber = parseHeaderFooterString(headerFooter, justifyLeft,
                        rowString, rowNumber, colNumber);
            }

            if (centerRowArray.length > rowNumber) {
                rowString = centerRowArray[rowNumber];
                colNumber = parseHeaderFooterString(headerFooter,
                        justifyCenter, rowString, rowNumber, colNumber);
            }

            if (rightRowArray.length > rowNumber) {
                rowString = rightRowArray[rowNumber];
                colNumber = parseHeaderFooterString(headerFooter, justifyRight,
                        rowString, rowNumber, colNumber);
            }
        }
    }

    /**
     * This function is used to parse the Header/Footer string and to split it
     * into tokens.
     * 
     * @param headerFooter
     *            the HeaderFooter object
     * @param justifyCode
     *            Code object that determines the alignment -Left,Center or
     *            Right
     * @param stringToBeParsed
     *            the string that is to be parsed
     * @param rowNumber
     *            the row number
     * @param columnNumber
     *            the column number
     * @return
     */
    private int parseHeaderFooterString(View.HeaderFooterItems headerFooter,
            Code justifyCode, String stringToBeParsed, int rowNumber,
            int columnNumber) {
        Code codeText = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.FUNCTION).getCode(Codes.HF_TEXT);

        String[] tokenArray;
        String tokenString = "";
        char tokenChar = ']';

        // the String array tokenArray will contain one entry
        // for each token in the current row separated on the basis of the
        // delimiter "&[".
        tokenArray = stringToBeParsed.split("&\\[");

        int tokenArrayLength = tokenArray.length;
        for (int j = 0; j < tokenArrayLength; j++) {
            tokenString = tokenArray[j];

            // CQ 7915. Nikita. 06/05/2010. Removal of redundant HF items (of
            // type Text of length 0) being stored in database.

            // don't store if it is an empty string unless it is a blank line
            if (tokenString.equals("") && tokenArray.length > 1) {
                continue;
            }

            int tokenIndex = tokenString.indexOf(tokenChar);
            if (tokenIndex > 0) {
                // token character found
                String codeString = tokenString.substring(0, tokenIndex);
                Code codeObj = findHFFunctionCode(codeString);
                if (codeObj == null) {
                    // no code found inside escape characters
                    // this means the full text is user text, including '&['
                    if (j > 0) {
                        tokenString = "&[" + tokenString;
                    }
                    headerFooter.addItem(codeText, justifyCode,
                            (rowNumber + 1), columnNumber++, tokenString);
                } else {
                    // code object found. if there is more text, consider
                    // that as user text.
                    headerFooter.addItem(codeObj, justifyCode, (rowNumber + 1),
                            columnNumber++, null);
                    // search for more text
                    if (tokenString.length() > tokenIndex + 1) {
                        String userString = tokenString.substring(
                                tokenIndex + 1, tokenString.length());
                        // store in header or footer as user text
                        headerFooter.addItem(codeText, justifyCode,
                                (rowNumber + 1), columnNumber++, userString);
                    }
                }
            } else {
                // token character not found
                if (j > 0) {
                    tokenString = "&[" + tokenString;
                }
                // store in header or footer as user text
                headerFooter.addItem(codeText, justifyCode, (rowNumber + 1),
                        columnNumber++, tokenString);
            }

        }
        return columnNumber;
    }

    /**
     * This function is used to return the Code object whose description is the
     * text passed as parameter.
     * 
     * @param codeText
     *            the string which is to be searched as the description of a
     *            Code object
     * @return the Code object whose description matches the text passed as
     *         parameter
     */
    private Code findHFFunctionCode(String codeText) {
        List<Code> codeList = SAFRApplication.getSAFRFactory().getCodeSet(
                CodeCategories.FUNCTION).getCodes();
        for (Code code : codeList) {
            if (codeText.equals(code.getDescription())) {
                return code;
            }
        }
        return null;
    }

    /**
     * This function is used to retrieve the Header/Footer items from the model
     * and convert it into a string array representing the Header/Footer text
     * for the Left, Center and Right selections.
     * 
     * @param headerFooter
     *            the HeaderFooter object
     * @return string array representing the combined Header/Footer text
     */
    private String[] loadHeaderFooter(View.HeaderFooterItems headerFooter) {
        List<HeaderFooterItem> hfItems = headerFooter.getItems();
        Code functionCode;
        Code justifyCode;
        int row;
        String itemText;

        int leftCounter = 1;
        int centerCounter = 1;
        int rightCounter = 1;
        String functionString = "";
        StringBuffer leftString = new StringBuffer();
        StringBuffer centerString = new StringBuffer();
        StringBuffer rightString = new StringBuffer();

        // combinedHFText will contain 3 items - one for the text of each
        // selection -Left, Center and Right
        String[] combinedHFText = new String[3];

        for (HeaderFooterItem item : hfItems) {
            functionCode = item.getFunctionCode();
            justifyCode = item.getJustifyCode();
            row = item.getRow();
            itemText = item.getItemText();

            if (functionCode.getGeneralId() == Codes.HF_TEXT) { // user text
                if (itemText != null) {
                    switch (justifyCode.getGeneralId()) {
                    case Codes.LEFT:
                        if (leftCounter != row) {
                            leftString.append(SAFRUtilities.LINEBREAK);
                        }
                        leftString.append(itemText);
                        leftCounter = row;
                        break;
                    case Codes.CENTER:
                        if (centerCounter != row) {
                            centerString.append(SAFRUtilities.LINEBREAK);
                        }
                        centerString.append(itemText);
                        centerCounter = row;
                        break;
                    case Codes.RIGHT:
                        if (rightCounter != row) {
                            rightString.append(SAFRUtilities.LINEBREAK);
                        }
                        rightString.append(itemText);
                        rightCounter = row;
                        break;
                    }
                }
            } else { // function code
                functionString = "&[" + functionCode.getDescription() + "]";
                switch (justifyCode.getGeneralId()) {
                case Codes.LEFT:
                    if (leftCounter != row) {
                        leftString.append(SAFRUtilities.LINEBREAK);
                    }
                    leftString.append(functionString);
                    leftCounter = row;
                    break;
                case Codes.CENTER:
                    if (centerCounter != row) {
                        centerString.append(SAFRUtilities.LINEBREAK);
                    }
                    centerString.append(functionString);
                    centerCounter = row;
                    break;
                case Codes.RIGHT:
                    if (rightCounter != row) {
                        rightString.append(SAFRUtilities.LINEBREAK);
                    }
                    rightString.append(functionString);
                    rightCounter = row;
                    break;
                }
            }
        }

        combinedHFText[0] = leftString.toString();
        combinedHFText[1] = centerString.toString();
        combinedHFText[2] = rightString.toString();
        return combinedHFText;
    }
        
    /**
     * This function is used to initialize the values needed for Header/Footer
     * when the Hardcopy Report radio button is selected.
     */
    protected void show() {
        if (tabFormatReport == null || tabFormatReport.isDisposed()) {
            createTabFormatReport();
        }
        
        // refresh details
        textLinesPerPage.setText(view.getLinesPerPage() == null ? "0" : view.getLinesPerPage().toString());
        textReportWidth.setText(view.getReportWidth() == null ? "0" : view.getReportWidth().toString());
        
        // refresh header footer
        initializeHFButtonStringValues();
        populateHFFunctionsCombo();

        header = view.getHeader();
        footer = view.getFooter();

        headerText = loadHeaderFooter(header);
        textHeaderLeft.setText(headerText[0]);
        textHeaderCenter.setText(headerText[1]);
        textHeaderRight.setText(headerText[2]);

        footerText = loadHeaderFooter(footer);
        textFooterLeft.setText(footerText[0]);
        textFooterCenter.setText(footerText[1]);
        textFooterRight.setText(footerText[2]);
    }
    
    protected void refreshModel() {
        // store the current header/footer values in model
        if (headerText != null && !textHeaderLeft.isDisposed()) {
            if (!headerText[0].equals(textHeaderLeft.getText())
                    || !headerText[1].equals(textHeaderCenter.getText())
                    || !headerText[2].equals(textHeaderRight.getText())) {
                header.clearItems();
                splitHeaderFooterStringIntoRows(header, textHeaderLeft
                        .getText(), textHeaderCenter.getText(),
                        textHeaderRight.getText());

            }
        }

        if (footerText != null && !textFooterLeft.isDisposed()) {
            if (!footerText[0].equals(textFooterLeft.getText())
                    || !footerText[1].equals(textFooterCenter.getText())
                    || !footerText[2].equals(textFooterRight.getText())) {
                footer.clearItems();
                splitHeaderFooterStringIntoRows(footer, textFooterLeft
                        .getText(), textFooterCenter.getText(),
                        textFooterRight.getText());
            }
        }
    }

    protected void hide() {
        // Header/Footer is applicable only for Hardcopy Report
        if (tabFormatReport != null) {
            if (!tabFormatReport.isDisposed()) {
                tabFormatReport.dispose();
            }
            tabFormatReport = null;
        }
    }

    protected CTabItem getHeaderFooterTab() {
        return tabFormatReport;
    }

}
