package com.ibm.safr.we.ui.dialogs;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.ibm.safr.we.model.Folder;
import com.ibm.safr.we.model.Views;

public class ExportInactiveDialog extends TitleAreaDialog {

	private TableViewer customTableViewer;

	private List<Views> inputData;

	public ExportInactiveDialog(Shell parentShell, List<Views> createViewTableModel) {
		super(parentShell);
		this.inputData = createViewTableModel;
	}

	@Override
	public void create() {
		super.create();
		setTitleAreaColor(Display.getDefault().getSystemColor(SWT.COLOR_GRAY).getRGB());
		setTitle("Only active views can be exported.");
		setMessage("The following inactive views will not be exported.", IMessageProvider.WARNING);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		area.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));

		parent.getShell().setText("Export Confirmation");
		Composite container = new Composite(area, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint=250;
		data.widthHint=250;
		container.setLayoutData(data);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);

		createTableViewer(container);
		customTableViewer.setInput(inputData);

		return area;
	}
	
	private void createTableViewer(Composite container) {
		customTableViewer = new TableViewer(container,
				SWT.FULL_SELECTION | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);

		customTableViewer.setContentProvider(new ArrayContentProvider());
		customTableViewer.setLabelProvider(new ExportLabelProvider());
		final Table table = customTableViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		String[] columnNames = new String[] { "Folder ID       ","Folder Name             ","View ID       ", "View Name                                                      " };
		int[] columnWidths = new int[] { 200, 200, 200, 200 };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(columnWidths[i]);
		}

		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
			table.getColumn(i).pack();
		}
		
	}

	
	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

}

class ExportLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		
		Views view = (Views) element;

		switch (columnIndex) {
		case 0:
			return String.valueOf(view.getFolder().getFolderId());
		case 1:
			return view.getFolder().getFolderName();
		case 2:
			return String.valueOf(view.getViewId());
		case 3:
			return view.getViewtName();
			
		default:
			return "";
		}
	}

}
