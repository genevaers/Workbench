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
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class OpenComponentDialog extends TitleAreaDialog {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.dialogs.OpenComponentDialog");
    private static final String TYPE = "TYPE";
    
    private class ButSelectionAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            super.widgetSelected(e);
            curType = (ComponentType) ((Button)e.getSource()).getData(TYPE);
            if (arrowKeyPressedOn == null) {
                textOpen.setFocus();
            }   
            if (arrowKeyPressedOn != curType) {
                arrowKeyPressedOn = null;
            }
        }        
    }
    
    private class ButTraverseListener implements TraverseListener {

        private ComponentType type;
        
        public  ButTraverseListener(ComponentType type) {
            this.type = type;
        }
        
        public void keyTraversed(TraverseEvent e) {
            if (e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT ||
                e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN) {
                arrowKeyPressedOn = type;
            }
        }
        
    };
 
    
    private SAFRGUIToolkit safrGUIToolkit;
    private Label openTextLabel;
    private Text textOpen;
    private Composite compositeOpenMetadata;
    private String openText;
    private ComponentType curType;
    private Button envBut;
    private Button ueBut;
    private Button crBut;
    private Button pfBut;
    private Button lfBut;
    private Button lrBut;
    private Button lpBut;
    private Button vBut;
    private Button vfBut;
    private Button uBut;
    private Button gBut;
    private ComponentType arrowKeyPressedOn = null;

    public OpenComponentDialog(Shell parentShell, ComponentType defaultType) {
        super(parentShell);
        safrGUIToolkit = new SAFRGUIToolkit();
        this.curType = defaultType;
    }

    @Override
    protected void okPressed() {
        if (textOpen.getText().equals("")) {
            openText = "";
            this.close();
        } else {
            openText = textOpen.getText();
            super.okPressed();
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        getShell().setText("Open Component");
        setTitle("Open Component by ID");
        setMessage("", IMessageProvider.NONE);
        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        compositeOpenMetadata = safrGUIToolkit.createComposite(parent, SWT.NONE);
        GridLayout grid1 = new GridLayout();

        compositeOpenMetadata.setLayout(grid1);
        compositeOpenMetadata.setLayout(new GridLayout(2, false));
        compositeOpenMetadata.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group compGroup = safrGUIToolkit.createGroup(compositeOpenMetadata, SWT.None, "Component");
        GridLayout layout = new GridLayout(4, true);
        layout.marginLeft = 30;
        compGroup.setLayout(layout);
        envBut = safrGUIToolkit.createRadioButton(compGroup, "&Environment");
        envBut.setData(TYPE, ComponentType.Environment);
        envBut.addSelectionListener(new ButSelectionAdapter());
        envBut.addTraverseListener(new ButTraverseListener(ComponentType.Environment));
        crBut = safrGUIToolkit.createRadioButton(compGroup, "Co&ntrol Record");
        crBut.setData(TYPE, ComponentType.ControlRecord);
        crBut.addSelectionListener(new ButSelectionAdapter());
        crBut.addTraverseListener(new ButTraverseListener(ComponentType.ControlRecord));
        lrBut = safrGUIToolkit.createRadioButton(compGroup, "&Logical Record");
        lrBut.setData(TYPE, ComponentType.LogicalRecord);
        lrBut.addSelectionListener(new ButSelectionAdapter());
        lrBut.addTraverseListener(new ButTraverseListener(ComponentType.LogicalRecord));
        vfBut = safrGUIToolkit.createRadioButton(compGroup, "View Fol&der");
        vfBut.setData(TYPE, ComponentType.ViewFolder);
        vfBut.addSelectionListener(new ButSelectionAdapter());
        vfBut.addTraverseListener(new ButTraverseListener(ComponentType.ViewFolder));        
        pfBut = safrGUIToolkit.createRadioButton(compGroup, "&Physical File");
        pfBut.setData(TYPE, ComponentType.PhysicalFile);
        pfBut.addSelectionListener(new ButSelectionAdapter());
        pfBut.addTraverseListener(new ButTraverseListener(ComponentType.PhysicalFile));
        lpBut = safrGUIToolkit.createRadioButton(compGroup, "Loo&kup Path");
        lpBut.setData(TYPE, ComponentType.LookupPath);
        lpBut.addSelectionListener(new ButSelectionAdapter());
        lpBut.addTraverseListener(new ButTraverseListener(ComponentType.LookupPath));
        uBut = safrGUIToolkit.createRadioButton(compGroup, "&User");
        uBut.setData(TYPE, ComponentType.User);
        uBut.addSelectionListener(new ButSelectionAdapter());
        uBut.addTraverseListener(new ButTraverseListener(ComponentType.User));
        ueBut = safrGUIToolkit.createRadioButton(compGroup, "User-E&xit Routine");
        ueBut.setData(TYPE, ComponentType.UserExitRoutine);
        ueBut.addSelectionListener(new ButSelectionAdapter());
        ueBut.addTraverseListener(new ButTraverseListener(ComponentType.UserExitRoutine));
        lfBut = safrGUIToolkit.createRadioButton(compGroup, "Logical &File");
        lfBut.setData(TYPE, ComponentType.LogicalFile);
        lfBut.addSelectionListener(new ButSelectionAdapter());
        lfBut.addTraverseListener(new ButTraverseListener(ComponentType.LogicalFile));
        vBut = safrGUIToolkit.createRadioButton(compGroup, "&View");
        vBut.setData(TYPE, ComponentType.View);
        vBut.addSelectionListener(new ButSelectionAdapter());
        vBut.addTraverseListener(new ButTraverseListener(ComponentType.View));
        gBut = safrGUIToolkit.createRadioButton(compGroup, "G&roup");
        gBut.setData(TYPE, ComponentType.Group);
        gBut.addSelectionListener(new ButSelectionAdapter());        
        gBut.addTraverseListener(new ButTraverseListener(ComponentType.Group));
        GridData compGroupData = new GridData(GridData.FILL_BOTH);
        compGroupData.horizontalSpan = 2;
        compGroup.setLayoutData(compGroupData);
        selectType(curType);
        
        openTextLabel = safrGUIToolkit.createLabel(compositeOpenMetadata, SWT.NONE, "Co&mponent ID:");
        openTextLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        GridData gridText = new GridData(SWT.FILL, SWT.NONE, true, true);

        textOpen = safrGUIToolkit.createTextBox(compositeOpenMetadata, SWT.BORDER);
        textOpen.setFocus();
        textOpen.setLayoutData(gridText);
        textOpen.addVerifyListener(new VerifyListener() {

            public void verifyText(VerifyEvent e) {
                if (curType.equals(ComponentType.User)) {
                    String myText = e.text;
                    e.text = myText.toUpperCase();

                    // Get the character typed
                    char myChar = e.character;
                    e.character = Character.toUpperCase(myChar);
                }
                else {
                    String txt = e.text;
                    if (txt.isEmpty()) {
                        return;
                    }
                    try {
                        Integer.parseInt(txt);
                    } catch (NumberFormatException ex) {
                        e.doit = false;
                    }                    
                }
            }

        });
        
        setMessage("Enter a component ID.",IMessageProvider.INFORMATION);
        
        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "&OK", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
    }
    
    private void selectType(ComponentType type) {
        switch (type) {
        case Environment:
            envBut.setSelection(true);
            break;
        case UserExitRoutine:
            ueBut.setSelection(true);
            break;
        case ControlRecord:
            crBut.setSelection(true);
            break;
        case PhysicalFile:
            pfBut.setSelection(true);
            break;
        case LogicalFile:
            lfBut.setSelection(true);
            break;
        case LogicalRecord:
            lrBut.setSelection(true);
            break;
        case LookupPath:
            lpBut.setSelection(true);
            break;
        case View:
            vBut.setSelection(true);
            break;
        case ViewFolder:
            vfBut.setSelection(true);
            break;
        case User:
            uBut.setSelection(true);
            break;
        case Group:
            gBut.setSelection(true);
            break;
        default:
            break;
        }
    }
    
    /**
     * This method is used to get the open text
     * 
     * @return open text
     */
    public String getOpenText() {
        return openText;
    }

    public Integer getOpenId() {
        return new Integer(openText);
    }
    
    public ComponentType getCurType() {
        return curType;
    }    
}
