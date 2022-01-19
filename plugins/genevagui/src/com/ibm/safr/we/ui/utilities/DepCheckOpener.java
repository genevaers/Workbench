package com.ibm.safr.we.ui.utilities;

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


import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.ui.editors.DependencyCheckerEditor;
import com.ibm.safr.we.ui.editors.DependencyCheckerEditorInput;

public class DepCheckOpener {

    public static IEditorPart open(EnvironmentalQueryBean bean) {
        DependencyCheckerEditorInput input = new DependencyCheckerEditorInput();
        try {
            DependencyCheckerEditor editor = (DependencyCheckerEditor)PlatformUI.getWorkbench().
                getActiveWorkbenchWindow().getActivePage().openEditor(input, DependencyCheckerEditor.ID);
            editor.setComponent(bean);
        } catch (PartInitException e) {
            UIUtilities.handleWEExceptions(e,
                "Unexpected error occurred while opening Dependency Checker editor.",null);
        }
        return null;        
    }
    
}
