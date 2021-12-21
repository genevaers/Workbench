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

import com.ibm.safr.we.model.utilities.MigrationComponent;
import com.ibm.safr.we.ui.editors.MigrationEditor;
import com.ibm.safr.we.ui.utilities.EditorOpener;

public class MigrationOpenEditor extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
        	MigrationEditor editor = (MigrationEditor)page.getActiveEditor();
            if (editor != null) {
            	MigrationComponent node = editor.getCurrentSelection();
                if (node != null && node.getComponent() != null) {
                    EditorOpener.open(node.getComponent().getId(), editor.getCurrentComponentType());                    
                }
            }
        }
        return null;
	}

}
