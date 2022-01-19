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


import java.util.logging.Logger;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.editors.view.ViewColumnEditor;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class ViewGenDialog extends TitleAreaDialog {

    static transient Logger logger = Logger
        .getLogger("com.ibm.safr.we.ui.dialogs.viewgen.ViewGenDialog");
    
    private SAFRGUIToolkit safrGUIToolkit;
    private Composite compositeTopLevel;
    private View view;
    private ViewSource viewSource;
    private ViewColumnEditor viewColumnEditor;
    private FontRegistry fontRegistry = new FontRegistry();
    
    private ViewGenMediator mediator = new ViewGenMediator();
    
    public ViewGenDialog(Shell parentShell, View view, ViewSource viewSource, ViewColumnEditor viewColumnEditor) {
        super(parentShell);
        this.safrGUIToolkit = new SAFRGUIToolkit();
        this.view = view;
        this.viewSource = viewSource;    
        this.viewColumnEditor = viewColumnEditor;
        fontRegistry.put("viewgen", new FontData[]{new FontData("Segoe UI", 8, SWT.NORMAL)});      
        fontRegistry.put("viewgenb", new FontData[]{new FontData("Segoe UI", 8, SWT.BOLD)});      
    }

    @Override
    protected Point getInitialSize() {
        return new Point(1280, 720);
    }
    
    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        getShell().setText("View Column Generator");
        setTitle("View Column Generator");
        return contents;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        this.getShell().addListener(SWT.Close, new Listener() {
            public void handleEvent(Event event) {
                refreshViewEditor();
            }
          });
        mediator.setViewGenDialog(this);      
        
        compositeTopLevel = safrGUIToolkit.createComposite(parent, SWT.NONE);
        compositeTopLevel.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(2, false);
        compositeTopLevel.setLayout(layout);
        
        ViewGenCriteria criteria = new ViewGenCriteria(mediator, compositeTopLevel, viewSource);
        mediator.setViewGenCriteria(criteria);
        criteria.create();

        // create filler
        safrGUIToolkit.createLabel(compositeTopLevel, SWT.NONE, "");        

        Composite compositeBottom = safrGUIToolkit.createComposite(compositeTopLevel, SWT.NONE);
        compositeBottom.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout blayout = new GridLayout(2, false);
        compositeBottom.setLayout(blayout);
        
        ViewGenField field = new ViewGenField(mediator, compositeBottom, viewSource);
        mediator.setViewGenField(field);
        field.create();

        ViewGenColumn column = new ViewGenColumn(mediator, compositeBottom, view, viewSource);
        mediator.setViewGenColumn(column);
        column.create();
        
        return parent;
    }

    public SAFRGUIToolkit getSafrGuiToolkit() {
        return safrGUIToolkit;
    }
    
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        if (id == IDialogConstants.CANCEL_ID) 
            return null;
        return super.createButton(parent, id, label, defaultButton);
    }

    @Override
    protected void okPressed() {
        super.okPressed();
        refreshViewEditor();
    }

    protected void refreshViewEditor() {
        if (view.isUpdated()) { 
            viewColumnEditor.refreshColumns();
            viewColumnEditor.setModified();
        }
    }

    public Font getFont() {
        return fontRegistry.get("viewgen");
    }

    public Font getBoldFont() {
        return fontRegistry.get("viewgenb");
    }
    
}
