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


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.ibm.safr.we.model.view.ViewSource;

public class ViewGenCriteria {

    enum EditMode {INSERTBEFORE,INSERTAFTER,OVERSOURCE};
    
    private ViewGenMediator mediator;    
    private ViewSource source;
    private EditMode editMode;

    private Composite parent;
    private Composite compositeCriteria;
    
    public ViewGenCriteria(
        ViewGenMediator mediator, 
        Composite parent,
        ViewSource source) {
        this.mediator = mediator;
        this.parent = parent;
        this.source = source;
    }
    
    protected void create() {
        compositeCriteria = mediator.getGUIToolKit().createComposite(parent,SWT.NONE);
        
        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 10;
        layout.marginTop = 0;
        layout.marginRight = 0;
        compositeCriteria.setLayout(layout);
        
        // source label
        Label sourceName = mediator.getGUIToolKit().createLabel(compositeCriteria, SWT.NONE,"Source:");
        sourceName.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        Label sourceLabel = mediator.getGUIToolKit().createLabel(compositeCriteria, SWT.NONE,source.getDescriptor());
        sourceLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        
        // edit mode
        Label editModeLabel= mediator.getGUIToolKit().createLabel(compositeCriteria, SWT.NONE,"Edit Mode:");
        editModeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        
        Group eGroup = mediator.getGUIToolKit().createGroup(compositeCriteria, SWT.None, "");
        eGroup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        GridLayout elayout = new GridLayout(2, true);
        eGroup.setLayout(elayout);
        Button insBefBut = mediator.getGUIToolKit().createRadioButton(eGroup, "Insert Before");
        insBefBut.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editMode = EditMode.INSERTBEFORE;
                mediator.refreshAddButtonState();
                mediator.refreshColGenButtonState();
            }
        });
        Button insAftBut = mediator.getGUIToolKit().createRadioButton(eGroup, "Insert After");
        insAftBut.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editMode = EditMode.INSERTAFTER;
                mediator.refreshAddButtonState();
                mediator.refreshColGenButtonState();
            }
        });
        Button overColSrc = mediator.getGUIToolKit().createRadioButton(eGroup, "Overwrite only Column Source");
        overColSrc.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editMode = EditMode.OVERSOURCE;
                mediator.refreshAddButtonState();
                mediator.refreshColGenButtonState();
            }
        });        
   }
    
    public EditMode getEditMode() {
        return editMode;
    }

    
}
