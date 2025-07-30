package com.ibm.safr.we.ui.dialogs.viewgen;

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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;

import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.ui.dialogs.viewgen.ViewGenCriteria.EditMode;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class ViewGenMediator {

    private ViewGenDialog viewGenDialog;
    private ViewGenCriteria viewGenCriteria;
    private ViewGenField viewGenField;
    private ViewGenColumn viewGenColumn;
    
    public ViewGenMediator() {        
    }

    public void setViewGenDialog(ViewGenDialog viewGenDialog) {
        this.viewGenDialog = viewGenDialog;
    }

    public void setViewGenCriteria(ViewGenCriteria viewGenCriteria) {
        this.viewGenCriteria = viewGenCriteria;
    }

    public void setViewGenField(ViewGenField viewGenField) {
        this.viewGenField = viewGenField;
    }
    
    public void setViewGenColumn(ViewGenColumn viewGenColumn) {
        this.viewGenColumn = viewGenColumn;
    }

    
    // calls on the viewGen dialog
    
    protected SAFRGUIToolkit getGUIToolKit() {
        return viewGenDialog.getSafrGuiToolkit();
    }

    public void setMessage(String string) {
        viewGenDialog.setMessage(string);
    }
    
    public void setInfoMessage(String string) {
        viewGenDialog.setErrorMessage(null);
        viewGenDialog.setMessage(string, SWT.ICON_INFORMATION);
    }
    
    public void setErrorMessage(String string) {
        viewGenDialog.setErrorMessage(string);
   }
    
    public Font getFont() {
        return viewGenDialog.getFont();
    }

    public Font getBoldFont() {
        return viewGenDialog.getBoldFont();
    }
    
    // calls on viewcriteria
    
    public EditMode getEditMode() {
        return viewGenCriteria.getEditMode();
    }    
    
    // calls on viewfield
    
    public void refreshAddButtonState() {
        viewGenField.refreshAddButtonState();
    }
    
    public void refreshColGenButtonState() {
        viewGenColumn.refreshColumnGenButtons();
    }
    

    // calls on viewGenColumn
    
    public boolean isColumnSelected() {
        return viewGenColumn.isColumnSelected();
    }

    public void putFields(List<LRField> lrFields, List<FieldTreeNodeLeaf> lpFields) {
        viewGenColumn.putFields(lrFields,lpFields);
    }

    public boolean viewHasNoColumns() {
        return viewGenColumn.viewHasNoColumns();
    }

    public int numberColumnsSelected() {
        return viewGenColumn.numberColumnsFromSelection();
    }
    
    public void refreshDialog() {
    	//viewGenDialog.refresh();
    }
    
    public boolean isCorFxAdded() {
    	return viewGenColumn.isCorFxAdded();
    }

	public boolean isSelectedOneColumn() {
		return viewGenColumn.isSelectedOneColumn();
	}



}
