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


import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * This is a custom text cell editor. A focus lost listener has been
 * added so that the last focus widget (the grid) can be stored. The
 * grid will be focused when WE is activated. 
 */
public class SAFRTextCellEditor extends TextCellEditor {
    
    private static final int defaultStyle = SWT.SINGLE;

    public SAFRTextCellEditor(Composite parent) {
        this(parent, defaultStyle);
    }

    public SAFRTextCellEditor(Composite parent, int style) {
        super(parent, style);
    }
    
    @Override
    protected Control createControl(Composite parent) {
        Control control =  super.createControl(parent);
        control.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                UIUtilities.rememberFocusControl(text.getParent());
            }
        });

        control.addControlListener(new ControlListener() {

            @Override
            public void controlMoved(ControlEvent e) {
                Rectangle bound = control.getBounds();
                if (bound.x==0 && bound.y==0) {
                    fireCancelEditor();
                }
            }

            @Override
            public void controlResized(ControlEvent e) {
            }
        });
        
        UIUtilities.replaceMenuText((Text)control);
        
        return control;
    }

}
