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


import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

public class SAFRTableComboViewer extends TableComboViewer {
	long lastKeyTime;
	String lastKey = "";
	int searchIndex = 0;

	// this listener is added to provide the ability to search a combo item by
	// typing a key.
	// assumes the combo has only 2 columns and 2nd one is the name column.
	public SAFRTableComboViewer(Composite parent, int style) {
		super(parent, style);
		this.getTableCombo().getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (Character.isISOControl(e.character)) {
					// no need to handle control chars (Shift, Ctrl etc.)
					return;
				}
				Table table = SAFRTableComboViewer.this.getTableCombo()
						.getTable();
				if (!table.getSortColumn().equals(table.getColumn(0))) {
					// ignore the 1st column as its handled by OS
					String key = String.valueOf(e.character).toLowerCase();
					if ((lastKey.length() == 1) && key.equals(lastKey)) {
						// same key pressed again, search should start from the
						// last found item
						lastKey = key;
						searchIndex++;
						if (searchIndex >= table.getItemCount()) {
							searchIndex = 0; // reset index
						}
					} else {
						// reset search index
						searchIndex = 0;
						if ((System.currentTimeMillis() - lastKeyTime) <= 500) {
							// Pressed in half second, attach the key to the
							// last
							// one
							lastKey += key;
						} else {
							// pressed after half second
							lastKey = key;
						}
					}
					lastKeyTime = System.currentTimeMillis();
					for (int i = searchIndex; i < table.getItemCount(); i++, searchIndex++) {
						if (table.getItem(i).getText(1).toLowerCase()
								.startsWith(lastKey)) {
							SAFRTableComboViewer.this.setSelection(
									new StructuredSelection(table.getItem(i)
											.getData()), true);
							table.setSelection(i);
							// CQ 8865. Nikita. 18/11/2010
							// Check if the next item lies within the table
							// count in order to prevent an
							// IllegalArgumentException (index out of bounds)
							if ((i + 1) < table.getItemCount()) {
								if ((lastKey.length() == 1)
										&& !table.getItem(i + 1).getText(1)
												.toLowerCase().startsWith(
														lastKey)) {
									searchIndex = 0;// reset if next item
									// doesn't
									// match
								}
							}
							return;
						}
					}
				}
			}
		});
	}
}
