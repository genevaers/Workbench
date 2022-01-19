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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.ui.editors.DiffUtilityEditor;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditor;


public class CollapseAllHandler extends AbstractHandler  implements IHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart part = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActiveEditor();
        if (part instanceof DiffUtilityEditor) {
            DiffUtilityEditor ed = (DiffUtilityEditor)part;
            ed.collapseAll();
        }
        else if (part instanceof LogicalRecordEditor) {
            LogicalRecordEditor ed = (LogicalRecordEditor)part;
            ed.collapseAll();
        }                
        return null;
    }
}
