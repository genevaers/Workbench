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

 package com.ibm.safr.we.ui.utilities;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * This extends ComboBoxCellEditor and overrides the focusLost method to change
 * its visibility from protected to public so that it can be called by listeners
 * in other classes to handle combo box behaviour correctly. It also replaces a
 * constructor which is package private higher up in the inheritance hierarchy.
 * Because the combo box control is also package private in the parent class,
 * this variable is also replicated here.
 */
public class SAFRComboBoxCellEditor extends ComboBoxCellEditor {

	/**
	 * The list of items to present in the combo box.
	 */
	private String[] items;

	/**
	 * The zero-based index of the selected item.
	 */
	int selection;

	/**
	 * The custom combo box control.
	 */
	CCombo comboBox;
	
	public SAFRComboBoxCellEditor(Composite parent, String[] items, int style) {
		super(parent, items, style);
	}
	
	@Override
	protected Control createControl(Composite parent) {

		comboBox = (CCombo) super.createControl(parent);
		
        // replace the default windows context menu
        UIUtilities.replaceMenuCombo(comboBox);
		
        comboBox.addControlListener(new ControlListener() {

            @Override
            public void controlMoved(ControlEvent e) {
                Rectangle bound = comboBox.getBounds();
                if (bound.x==0 && bound.y==0) {
                    fireCancelEditor();
                }
            }

            @Override
            public void controlResized(ControlEvent e) {
            }
        });

        comboBox.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                Event ev = new Event();
                ev.detail = e.detail;
                parent.notifyListeners(SWT.Traverse, ev);
            }
            
        });
		return comboBox;
	}

	/**
	 * Method copied here as the original is package private in the super class
	 * so not visible in this class. This method must be called other methods in
	 * this class which won't have visibility of the original method.
	 */
	protected void applyEditorValueAndDeactivateOver() {
		// must set the selection before getting value
		selection = comboBox.getSelectionIndex();
		Object newValue = doGetValue();
		markDirty();
		boolean isValid = isCorrect(newValue);
		setValueValid(isValid);

		if (!isValid) {
			// Only format if the 'index' is valid
			if (items.length > 0 && selection >= 0 && selection < items.length) {
				// try to insert the current value into the error message.
				setErrorMessage(MessageFormat.format(getErrorMessage(),
						new Object[] { items[selection] }));
			} else {
				// Since we don't have a valid index, assume we're using an
				// 'edit'
				// combo so format using its text value
				setErrorMessage(MessageFormat.format(getErrorMessage(),
						new Object[] { comboBox.getText() }));
			}
		}

		fireApplyEditorValue();
		deactivate();
	}

	/**
	 * Overrides the inherited method to change its visibility from protected to
	 * public as it must be called by listeners in other classes.
	 */
	public void focusLost() {
		if (isActivated()) {
			applyEditorValueAndDeactivateOver();
		}
	}

}
