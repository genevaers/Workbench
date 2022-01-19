package com.ibm.safr.we.ui.editors.batchview;

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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.utilities.BatchComponent;

public class ActivateViewsError {
    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.batchview.ActivateViewsErrors");
    
    private ActivateViewsMediator mediator;

    // section errors
    private Composite parent;
    private Section sectionErrors;
    private Composite compositeErrors;
    private Text textErrors;
        
    public ActivateViewsError(ActivateViewsMediator mediator, Composite parent) {
        super();
        this.mediator = mediator;
        this.parent = parent;
    }
    
    protected void create() {
        
        sectionErrors = mediator.getGUIToolKit().createSection(parent, Section.TITLE_BAR
            | Section.TWISTIE, "Error");
        FormData dataErrors = new FormData();
        dataErrors.left = new FormAttachment(mediator.getTableSection(), 5);
        dataErrors.top = new FormAttachment(mediator.getEnvironmentSection(), 10);
        dataErrors.bottom = new FormAttachment(100, -50);
        dataErrors.right = new FormAttachment(100, -5);
        sectionErrors.setLayoutData(dataErrors);
    
        compositeErrors = mediator.getGUIToolKit().createComposite(sectionErrors, SWT.NONE);
        compositeErrors.setLayout(new GridLayout(1, false));
    
        textErrors = new Text(compositeErrors, SWT.MULTI | SWT.BORDER | SWT.WRAP | 
            SWT.V_SCROLL | SWT.READ_ONLY);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        textErrors.setLayoutData(data);
        textErrors.setBackground(textErrors.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        
        showSectionLoadErrors(false);
        sectionErrors.setClient(compositeErrors);
        
    }
    
    /**
     * This method is used to display the errors section expanded or enabled
     * based upon the value passed
     * 
     * @param visible
     */
    protected void showSectionLoadErrors(Boolean visible) {
        BatchComponent view = mediator.getSelectedView();
        if (view != null) {
            SAFRException ex = view.getException();
            if (ex == null) {
                textErrors.setText("");            
            } else {
                textErrors.setText(ex.toString());                        
            }
        } else {
            textErrors.setText("");                        
        }
        sectionErrors.setExpanded(visible);
        sectionErrors.setEnabled(visible);
    }

    protected void clearErrors() {
        textErrors.setText("");
    }

    
}
