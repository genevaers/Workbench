package com.ibm.safr.we.ui.commands;

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


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditor;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditorInput;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.logic.LogicTextView;
import com.ibm.safr.we.ui.views.logic.LogicTextViewTreeNode;

public class LogicTextViewOpenEditor extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            LogicTextView view = (LogicTextView)page.findView(LogicTextView.ID);
            if (view != null) {
                LogicTextViewTreeNode node = view.getCurrentSelection();                
                if (node != null) {
                    switch (node.getId()) {
                    case FILES_CHILDLF:
                        LogicalFileQueryBean lfbean = (LogicalFileQueryBean)node.getData();
                        if (lfbean == null) {
                            break;
                        }
                        EditorOpener.open(lfbean.getId(), ComponentType.LogicalFile);
                        break;
                    case FIELDS_CHILD:
                        openLREditor((ComponentAssociation)node.getData(), node.getTitleText(), node.getRights());
                        break;
                    case FILES_CHILDLF_CHILDPF:
                        FileAssociation fass = (FileAssociation)node.getData();
                        if (fass == null) {
                            break;
                        }
                        EditorOpener.open(fass.getAssociatedComponentIdNum(), ComponentType.PhysicalFile);
                        break;
                    case LOOKUPPATHS_CHILD:
                    case LOOKUPSYMBOLS_CHILD:
                        LookupQueryBean lubean = (LookupQueryBean)node.getData();
                        if (lubean == null) {
                            break;
                        }                        
                        EditorOpener.open(lubean.getId(), ComponentType.LookupPath);
                        break;
                    case PROCEDURES_CHILD:
                    case USEREXITROUTINES_CHILD:
                        UserExitRoutineQueryBean uebean = (UserExitRoutineQueryBean)node.getData();
                        if (uebean == null) {
                            break;
                        }                        
                        EditorOpener.open(uebean.getId(), ComponentType.UserExitRoutine);
                        break;
                    default:
                    }
                }
            }
        }
		return null;
	}

    private void openLREditor(ComponentAssociation ass, String title, EditRights rights) {
        if (ass == null) {
            return;
        }
        try {
            LogicalRecord file = SAFRApplication.getSAFRFactory().getLogicalRecord(ass.getAssociatingComponentId()); 
            LogicalRecordEditorInput input = new LogicalRecordEditorInput(file, rights);
            LogicalRecordEditor editor = (LogicalRecordEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().openEditor(input, LogicalRecordEditor.ID);
            // {TSTMP} [29945]
            editor.selectRowAtField(UIUtilities.extractId(title));
        } catch (Exception e) {
            UIUtilities.handleWEExceptions(e,
                "Unexpected error occurred while opening logical record editor.",null);
        }
    }	
}
