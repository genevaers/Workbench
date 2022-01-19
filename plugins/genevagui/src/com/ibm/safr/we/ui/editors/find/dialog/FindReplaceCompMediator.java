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


import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Composite;

import com.ibm.safr.we.constants.ComponentType;

public class FindReplaceCompMediator {
    
    protected FindReplaceCompDialog compDialog;
    protected FindReplaceCompComp compComp;
    protected FindReplaceCompLRField compLRField;    

    public FindReplaceCompMediator(
        FindReplaceCompDialog compDialog,
        FindReplaceCompComp compComp,
        FindReplaceCompLRField compLRField) {
        this.compDialog = compDialog;
        this.compComp = compComp;
        this.compLRField = compLRField;
    }
    
    // dialog access methods
    protected Composite getComposite() {
        return compDialog.getComposite();
    }
    
    protected boolean isComponentTypeSelected() {
        return compDialog.isComponentTypeSelected();
    }
    
    protected void setMessage(String message) {
        compDialog.setMessage(message, IMessageProvider.INFORMATION);
    }
    
    protected void setErrorMessage(String message) {
        compDialog.setErrorMessage(message);
    }
    
    protected ComponentType getComponentType() {
        return compDialog.getComponentType();
    }

    protected int getEnvironmentId() {
        return compDialog.getEnvironmentId();
    }
    
    // Component handler
    protected void clearComponent() {
        compComp.clearComponent();
    }
    
    protected void populateComponentList() {
        compComp.populateComponentList();
    }
    
    protected void hideComponent() {
        compComp.hideComponent();
    }

    protected void showComponent() {
        compComp.showComponent();
    }
    
    // LR Field Handler
    protected void clearLRField() {
        compLRField.clearLRField();
    }
    
    protected void populateLRList() {
        compLRField.populateLRList();
    }
    
    protected void hideLRField() {
        compLRField.hideLRField();
    }

    protected void showLRField() {
        compLRField.showLRField();
    }
    
    
    // handlers
    protected void create() {
        compComp.create();
        compLRField.create();
        compLRField.hideLRField();
    }

    protected String getSelectedComponentName() {
        if (getComponentType().equals(ComponentType.LogicalRecord)) {
            return compLRField.getSelectedLRFieldName();
        }
        else {
            return compComp.getSelectedComponentName();            
        }
    }
        
}
