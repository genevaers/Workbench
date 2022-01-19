package com.ibm.safr.we.ui.editors.lr;

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


import java.util.List;

import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.ui.utilities.SAFRTextCellEditor;

/**
 * This subclass of TextCellEditor provides additional TRAVERSE (TAB) logic for
 * the Comments cell of the LR Field row (the last cell) to move cell focus to
 * the Field Name cell of the next row and scroll the display left if necessary
 * so the LR Field grid is fully displayed from the left most column (the Field
 * ID column).
 */
public class LRFieldCommentsTextCellEditor extends SAFRTextCellEditor {

	private GridTableViewer gridTableViewer;
	private LogicalRecordFieldEditor logicalRecordFieldEditor;
	private LogicalRecord logicalRecord = null;

	protected LRFieldCommentsTextCellEditor(Composite parent,
			GridTableViewer gridTableViewer,
			LogicalRecordFieldEditor logicalRecordFieldEditor, LogicalRecord logicalRecord) {
		super(parent);
		this.gridTableViewer = gridTableViewer;
		this.logicalRecordFieldEditor = logicalRecordFieldEditor;
		this.logicalRecord = logicalRecord;
	}

	protected Control createControl(Composite parent) {
		super.createControl(parent);
		final Grid tableLRFields = (Grid) parent;

		text.addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {

				final int numRows = tableLRFields.getItemCount();
				final Point pt = tableLRFields.getCellSelection()[0];
				List<LRField> list = logicalRecord.getLRFields().getActiveItems();
				final LRField[] fields = list.toArray(new LRField[list.size()]);

				if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
					if (pt.y == numRows - 1) {
						// on the last row so add a new row
						logicalRecordFieldEditor.addRow(true);
					} else {
						// Display grid from left hand side.
						tableLRFields.showColumn(tableLRFields.getColumn(0));
						// tab fwd to Field Name of next row
						gridTableViewer.editElement(fields[pt.y + 1], 1);
					}
				}
			}
		});

		return text;
	}

}
