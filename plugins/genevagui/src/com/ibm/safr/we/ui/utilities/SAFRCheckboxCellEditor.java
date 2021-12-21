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



import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This is a custom checkbox cell editor. Unlike the 'fake' one provided by
 * JFACE this one does create a Control object (a Button of type SWT.CHECK) and
 * uses it via the standard CellEditor API to store the boolean state of the
 * checkbox.
 */
public class SAFRCheckboxCellEditor extends CellEditor {
	
	private Button checkbox;
	
    public SAFRCheckboxCellEditor(Composite parent) {
        super(parent, SWT.CHECK);
    }
    
    protected Control createControl(Composite parent) {
        checkbox = new Button(parent, SWT.CHECK);
        
        checkbox.addKeyListener(new KeyAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
             */
            public void keyReleased(KeyEvent e) {
                keyReleaseOccured(e);
            }
        }); 
        
        checkbox.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                UIUtilities.rememberFocusControl(checkbox.getParent());
            }
        });
        return checkbox;
    }
    
    protected Object doGetValue() {
        return checkbox.getSelection();
    }

    protected void doSetFocus() {
        checkbox.setFocus();
    }

    protected void doSetValue(Object value) {
        Assert.isTrue(value instanceof Boolean);
        checkbox.setSelection(((Boolean) value).booleanValue());
    }
    
}
