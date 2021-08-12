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


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class MessageDisableDialog extends MessageDialog {

    boolean buttonEnabled[] = null;
    
    public MessageDisableDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
            int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
    }

    public MessageDisableDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
            int dialogImageType, String[] dialogButtonLabels, boolean[] buttonEnabled, int defaultIndex) {
        this(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
        this.buttonEnabled = buttonEnabled;
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        if (buttonEnabled != null) {
            for (int i=0; i<buttonEnabled.length ; i++) {
                getButton(i).setEnabled(buttonEnabled[i]);                
            }            
        }
    }   
    
    public static int openError(Shell parentShell,
            String dialogTitle, String dialogMessage,
            String[] dialogButtonLabels, boolean[] buttonEnabled) {
        MessageDisableDialog dialog = new MessageDisableDialog(
                parentShell, dialogTitle, null, dialogMessage, 
                MessageDialog.ERROR, dialogButtonLabels, buttonEnabled, 0);
        return dialog.open();
    }    
    
}
