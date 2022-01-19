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
import com.ibm.safr.we.model.utilities.FindReplaceComponent;
import com.ibm.safr.we.ui.editors.find.FindReplaceTextEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class FindReplaceOpenEditor extends AbstractHandler implements IHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            FindReplaceTextEditor editor = (FindReplaceTextEditor)page.getActiveEditor();
            try {
                FindReplaceComponent comp = editor.getCurrentSelection();
                if (comp != null) {
                    ViewEditor ed = (ViewEditor)EditorOpener.open(comp.getViewId(), ComponentType.View);     
                    // only show a valid column
                    if (comp.getColumnId() > 0) {
                    	ed.showColumn(comp.getColumnId());
                    }
                }
            } catch (Exception e) {
                UIUtilities.handleWEExceptions(e,"Unexpected error occurred while opening view editor.",null);
            }            
        }
        return null;
    }
}
