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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.utilities.FindReplaceComponent;
import com.ibm.safr.we.model.utilities.FindReplaceText;
import com.ibm.safr.we.ui.editors.find.dialog.FindReplaceCompDialog;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.MetadataView;
import com.ibm.safr.we.utilities.SAFRLogger;

public class FindReplaceReplace {

    private FindReplaceMediator mediator;
    
    // Data
    private List<FindReplaceComponent> replaceComponentList = new ArrayList<FindReplaceComponent>();
    
    // UI
    private SAFRGUIToolkit safrGuiToolkit;    
    private Section sectionReplace;
    private Text textReplace;
    private Button buttonReplace;
    private Button buttonSelectReplaceComponent;
    
    protected FindReplaceReplace() {
    }

    public void setMediator(FindReplaceMediator mediator) {
        this.mediator = mediator;
        this.safrGuiToolkit = mediator.getToolkit();
    }
    
    protected void create(Composite body) {
        sectionReplace = safrGuiToolkit.createSection(body, Section.TITLE_BAR,
                "Replace");
        FormData dataSectionReplace = new FormData();
        dataSectionReplace.top = new FormAttachment(mediator.getCriteriaSection(), 10);
        dataSectionReplace.left = new FormAttachment(0, 5);
        sectionReplace.setLayoutData(dataSectionReplace);

        Composite compositeReplace = safrGuiToolkit.createComposite(
                sectionReplace, SWT.NONE);
        compositeReplace.setLayout(new FormLayout());
        compositeReplace.setLayoutData(new FormData());

        Label labelReplace = safrGuiToolkit
                .createLabel(compositeReplace, SWT.NONE,
                        "&Replace With: (select a component name and/or enter replacement text)");
        FormData dataLabelReplace = new FormData();
        dataLabelReplace.left = new FormAttachment(0, 5);
        dataLabelReplace.top = new FormAttachment(0, 10);
        labelReplace.setLayoutData(dataLabelReplace);

        textReplace = safrGuiToolkit.createTextBox(compositeReplace, SWT.NONE);
        textReplace.setData(SAFRLogger.USER, "Replace With");                                                 
        FormData dataTextReplace = new FormData();
        dataTextReplace.top = new FormAttachment(labelReplace, 5);
        dataTextReplace.left = new FormAttachment(0, 5);
        dataTextReplace.right = new FormAttachment(100, -35);
        dataTextReplace.width = FindReplaceCriteriaFindBy.WIDTH_FIND;
        textReplace.setLayoutData(dataTextReplace);
        textReplace.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {

                if (mediator.isResultsSelected() && (!(textReplace.getText().trim().equals("")))) {
                    buttonReplace.setEnabled(true);
                } else {
                    buttonReplace.setEnabled(false);
                }

            }

        });

        textReplace.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (mediator.isResultsSelected() 
                        && (!(textReplace.getText().trim().equals("")))) {
                    buttonReplace.setEnabled(true);
                } else {
                    buttonReplace.setEnabled(false);
                }
            }
        });

        buttonSelectReplaceComponent = safrGuiToolkit.createButton(
                compositeReplace, SWT.PUSH, "...");
        buttonSelectReplaceComponent.setData(SAFRLogger.USER, "Replace Component");                                                       
        FormData dataButtonSelectReplaceComponent = new FormData();
        dataButtonSelectReplaceComponent.right = new FormAttachment(100, -5);
        dataButtonSelectReplaceComponent.top = new FormAttachment(textReplace,
                0, SWT.CENTER);
        buttonSelectReplaceComponent
                .setLayoutData(dataButtonSelectReplaceComponent);
        buttonSelectReplaceComponent
                .setToolTipText("Click this button to select the component.");
        buttonSelectReplaceComponent.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FindReplaceCompDialog selectComponetNameDialog = new FindReplaceCompDialog(
                    mediator.getSite().getShell(), mediator.getCurrentEnv().getId());
                selectComponetNameDialog.create();
                selectComponetNameDialog.setTitle("Select a Component to use in the replace text. ");
                if (selectComponetNameDialog.open() == Window.OK) {
                    if (!selectComponetNameDialog.getSelectedComponentName().equals(null)) {
                        
                        StringBuilder stringBuilder = new StringBuilder(textReplace.getText());
                        stringBuilder.insert(textReplace.getCaretPosition(),
                            selectComponetNameDialog.getSelectedComponentName());
                        textReplace.setText(stringBuilder.toString());
                    }
                }
            }
        });

        buttonReplace = safrGuiToolkit.createButton(compositeReplace, SWT.PUSH,
                "Re&place");
        buttonReplace.setData(SAFRLogger.USER, "Replace");                                                            
        FormData databuttonReplace = new FormData();
        databuttonReplace.top = new FormAttachment(textReplace, 10);
        databuttonReplace.left = new FormAttachment(0, 5);
        databuttonReplace.width = 70;
        buttonReplace.setLayoutData(databuttonReplace);

        buttonReplace.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                final String messageString = "You are replacing: " + SAFRUtilities.LINEBREAK + "'"
                        + mediator.getFindText()
                        + "' with '"
                        + textReplace.getText().trim()
                        + "' in the selected views." + SAFRUtilities.LINEBREAK + "This operation cannot be undone and the affected views will be made inactive.";
                try {
                    MessageDialog dialog = new MessageDialog(Display
                            .getCurrent().getActiveShell(), "GenevaERS Workbench",
                            null, messageString, MessageDialog.WARNING,
                            new String[] { "&OK", "&Cancel" }, 0);
                    int openConfirm = dialog.open();
                    if (openConfirm == 0) {
                        mediator.waitCursor();
                        replaceComponentList.clear();
                        Object[] checkedist = mediator.getCheckedResults();
                        for (Object obj : checkedist) {
                            replaceComponentList.add((FindReplaceComponent) obj);
                        }
                        FindReplaceText findReplaceText = mediator.getSearch();
                        findReplaceText.setComponentsToReplace(replaceComponentList);
                        findReplaceText.setReplaceText(textReplace.getText().trim());
                        findReplaceText.replaceViews();
                        mediator.refreshResults();
                        // refresh the metadata view if the replaced views is
                        // present in it.
                        MetadataView metadataview = (MetadataView) (PlatformUI
                                .getWorkbench().getActiveWorkbenchWindow()
                                .getActivePage().findView(MetadataView.ID));
                        if (metadataview != null) {
                            List<Integer> views = new ArrayList<Integer>();
                            for (FindReplaceComponent comp : replaceComponentList) {
                                views.add(comp.getViewId());
                            }
                            metadataview.refreshViewList(views);
                        }
                    }
                } catch (SAFRValidationException e1) {
                    if (!(e1.getMessageString().equals(""))) {
                        mediator.setMessage(e1);
                        MessageDialog.openError(mediator.getSite().getShell(),
                                "Validation error", e1.getMessageString());
                    }
                } catch (SAFRException e1) {
                    UIUtilities.handleWEExceptions(e1);
                } finally {
                    mediator.normalCursor();
                }
            }
        });
        buttonReplace.setEnabled(false);
        sectionReplace.setClient(compositeReplace);
    }

    protected void clearReplace() {
        replaceComponentList.clear();        
    }

    protected void disableReplace() {
        buttonReplace.setEnabled(false);
    }
    
    protected void enableReplace() {
        buttonReplace.setEnabled(true);
    }    
    
    protected boolean isReplaceTextEmpty() {
        return textReplace.getText().trim().equals("");
    }
    
    protected Control getControlFromProperty(Object property) {
        if (property == FindReplaceText.Property.REPLACE) {
            return textReplace;
        }
        else {
            return null;
        }        
    }
    
}
