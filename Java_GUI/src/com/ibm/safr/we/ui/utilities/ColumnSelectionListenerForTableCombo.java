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


import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationTableSorter;

public class ColumnSelectionListenerForTableCombo extends SelectionAdapter {

    private int colNumber;
	private TableComboViewer tableComboViewer;
	private ViewerSorter sorter;
	private ComponentType componentType = null;
	private String associatedComponent = "";
	private SAFRGUIToolkit safrGuiToolkit = new SAFRGUIToolkit();

	public ColumnSelectionListenerForTableCombo(int colNumber,
			TableComboViewer tableComboViewer, ComponentType componentType) {
		this.colNumber = colNumber;
		this.tableComboViewer = tableComboViewer;
		this.componentType = componentType;

	}

	public ColumnSelectionListenerForTableCombo(int colNumber,
			TableComboViewer tableComboViewer, String associatedComponent) {
		this.colNumber = colNumber;
		this.tableComboViewer = tableComboViewer;
		this.associatedComponent = associatedComponent;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		try {
		    ApplicationMediator.getAppMediator().waitCursor();
			TableCombo tableCombo = tableComboViewer.getTableCombo();
			Table table = tableCombo.getTable();
			TableColumn sortColumn = table.getSortColumn();
			TableColumn currentColumn = (TableColumn) e.widget;
			int dir = table.getSortDirection();
			if (sortColumn == currentColumn) {
				dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
			} else {
				table.setSortColumn(currentColumn);
				// if the currentColumn is the column for ID field then first
				// sorting direction is descending as its already sorted in
				// ascending order.
				if (currentColumn == table.getColumn(colNumber)) {
					dir = SWT.DOWN;
				} else {
					dir = SWT.UP;
				}
			}

			table.setSortDirection(dir);

			if (!associatedComponent.equals("")) {
				if (associatedComponent.equals("AssociatedComponent")) {
					sorter = new ComponentAssociationTableSorter(colNumber, dir);
					tableComboViewer.setSorter(sorter);
				} else if (associatedComponent.equals("AssociatedLRField")) {
					sorter = new AssociatedLRFieldSorter(colNumber, dir);
					tableComboViewer.setSorter(sorter);
				}
			} else {
				safrGuiToolkit.setSorter(componentType, tableComboViewer,
						colNumber, dir, sorter);
			}

            Order order = Order.ASCENDING;
            if (dir == SWT.DOWN) {
                order = Order.DESCENDING;
            }
            if (componentType != null) {
                SortOrderPrefs prefs = new SortOrderPrefs(SAFRGUIToolkit.SORT_CATEGORY, componentType.name(), colNumber, order);                
                prefs.store();                              
            }
            else if (associatedComponent != null) {
                SortOrderPrefs prefs = new SortOrderPrefs(SAFRGUIToolkit.SORT_CATEGORY, associatedComponent, colNumber, order);                
                prefs.store();                                              
            }
			
		} finally {
            ApplicationMediator.getAppMediator().normalCursor();
		}
	}
}
