package com.ibm.safr.we.ui.dialogs;

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



import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.model.utilities.importer.DependentComponentNode;
import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * Generate a dialog to get the user to decide to 
 * 	use an existing database component or 
 * 	rename the rename the component to be imported
 * 
 * The dialog also has a check box to remember the decision made 
 * and apply them to subsequent XML file imports.
 * 
 * Note that selecting to use an existing component also selects
 * its child components (those on which it depends)
 * 
 * The same hierarchical processing is applied to the remember selection
 *
 */
public class NewComponentNameDialog extends MessageDialog {

	/**
	 * A wrapper for the DependentComponentNode in the dialog
	 * This allows us to have title rows with no underlying DCN
	 * 
	 *
	 */
	private class TableRow {
		private DependentComponentNode dcn = null;
		private String title = null;
		
		public void setDcn(DependentComponentNode dcn) {
			this.dcn = dcn;
		}
		public DependentComponentNode getDcn() {
			return dcn;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getTitle() {
			return title;
		}
	}

	/**
	 * Inner Class to handle editing of the Use Existing check box
	 *
	 */
	private class UseExistingEditor extends EditingSupport {

		private final CheckboxCellEditor booleanCellEditor;

		public UseExistingEditor(ColumnViewer viewer) {
			super(viewer);
			booleanCellEditor = new CheckboxCellEditor(tv.getTable());
		}

		protected CellEditor getCellEditor(Object element) {
			return booleanCellEditor;
		}

		protected boolean canEdit(Object element) {
			Boolean retval = false;
			DependentComponentNode dcn = ((TableRow)element).getDcn();
			if (dcn != null) {
				retval = true;					
			} 
			return retval;
		}

		protected Object getValue(Object element) {
			Boolean retval = null;
			DependentComponentNode dcn = ((TableRow)element).getDcn();
			if (dcn != null) {
				retval = dcn.getUseExisting();					
			} 
			return retval;
		}

		protected void setValue(Object element, Object value) {
			((TableRow)element).getDcn().setUseExisting((Boolean) value);
			tv.refresh();
		}
	}
	
	/**
	 * Inner Class to handle editing of the Remember check box
	 *
	 */
	private class RememberEditor extends EditingSupport {

		private final CheckboxCellEditor booleanCellEditor;

		public RememberEditor(ColumnViewer viewer) {
			super(viewer);
			booleanCellEditor = new CheckboxCellEditor(tv.getTable());
		}

		protected CellEditor getCellEditor(Object element) {
			return booleanCellEditor;
		}

		protected boolean canEdit(Object element) {
			Boolean edit = false;
			if (((TableRow)element) != null) {
				edit = true;
			} 
			return edit;
		}

		protected Object getValue(Object element) {
			Boolean retval = false;
			DependentComponentNode dcn = ((TableRow)element).getDcn();

			if (dcn != null) {
				retval = dcn.getRemember();					
			} 
			return retval;
		}

		protected void setValue(Object element, Object value) {
			DependentComponentNode dcn = ((TableRow)element).getDcn();
			if (dcn!= null) {
				dcn.setRemember((Boolean) value);
				tv.refresh();
			}
		}
	}
	
	
	/**
	 * Inner Class to handle editing of the new name textEditor
	 *
	 */
	private class NameEditor extends EditingSupport {

		private CellEditor textEditor;

		public NameEditor(ColumnViewer viewer) {
			super(viewer);
			textEditor = new TextCellEditor(tv.getTable());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return textEditor;
		}

		@Override
		protected boolean canEdit(Object element) {
			boolean retval = false;
			DependentComponentNode dcn = ((TableRow)element).getDcn();
			if (dcn != null) {
				if (dcn.getUseExisting() != null && 
					dcn.getUseExisting() == false) {
						retval = true;
				}
			}
			return retval;
		}

		@Override
		protected Object getValue(Object element) {
			DependentComponentNode dcn = ((TableRow)element).getDcn();
			String retval = "";
			if (dcn.getTxfId() != null) {
				if (dcn.getNewName() != null && dcn.getUseExisting() == false) {
					retval = dcn.getNewName();
				}
			}
			return retval;
		}

		@Override
		protected void setValue(Object element, Object value) {
			((TableRow)element).getDcn().setNewName((String)value);
			tv.update(element, null);
		}
	}

	/**
	 * Inner class to order and supply the dialog entries
	 * for each component
	 * 
	 */
	private class ComponentContentProvider implements
	IStructuredContentProvider {

		List <TableRow> showThese = new ArrayList<TableRow>();
		List <TableRow> views = new ArrayList<TableRow>();
		List <TableRow> lkups = new ArrayList<TableRow>();
		List <TableRow> lfiles = new ArrayList<TableRow>();
		List <TableRow> lrs = new ArrayList<TableRow>();
		List <TableRow> pfs = new ArrayList<TableRow>();
		List <TableRow> exits = new ArrayList<TableRow>();
		List <TableRow> ctrls = new ArrayList<TableRow>();

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if(newInput != null) {
				duplicates = (DependentComponentNode)newInput;
			}
			if(oldInput != null) {
				duplicates = (DependentComponentNode)oldInput;
			}
		}

		public Object[] getElements(Object inputElement) {
			showThese.clear();
			duplicates.sweepBreadCrumbs();
			
			//Process the top level element
			if(duplicates.getNameClashesFound() == true) {
				TableRow componentEntry = new TableRow();
				componentEntry.setDcn(duplicates);
				addComponentType(componentEntry, duplicates.getComponentType());
			}
			//If there is a clash in any underlying component
			//find them to be displayed
			if(duplicates.getAnyNameClashesFound() == true) {
				buildTableElements(duplicates);
			}				
			
			if (!views.isEmpty()) {
				TableRow titleEntry = new TableRow();
				titleEntry.setTitle(ComponentType.View.getLabel());
				showThese.add(titleEntry);
				showThese.addAll(views);
				views.clear();
			}
			if (!ctrls.isEmpty()) {
				TableRow titleEntry = new TableRow();
				titleEntry.setTitle(ComponentType.ControlRecord.getLabel());
				showThese.add(titleEntry);
				showThese.addAll(ctrls);
				ctrls.clear();
			}
			if (!lkups.isEmpty()) {
				TableRow titleEntry = new TableRow();
				titleEntry.setTitle(ComponentType.LookupPath.getLabel());
				showThese.add(titleEntry);
				showThese.addAll(lkups);
				lkups.clear();
			}
			if (!lrs.isEmpty()) {
				TableRow titleEntry = new TableRow();
				titleEntry.setTitle(ComponentType.LogicalRecord.getLabel());
				showThese.add(titleEntry);
				showThese.addAll(lrs);
				lrs.clear();
			}
			if (!lfiles.isEmpty()) {
				TableRow titleEntry = new TableRow();
				titleEntry.setTitle(ComponentType.LogicalFile.getLabel());
				showThese.add(titleEntry);
				showThese.addAll(lfiles);
				lfiles.clear();
			}
			if (!pfs.isEmpty()) {
				TableRow titleEntry = new TableRow();
				titleEntry.setTitle(ComponentType.PhysicalFile.getLabel());
				showThese.add(titleEntry);
				showThese.addAll(pfs);
				pfs.clear();
			}
			if (!exits.isEmpty()) {
				TableRow titleEntry = new TableRow();
				titleEntry.setTitle(ComponentType.UserExitRoutine.getLabel());
				showThese.add(titleEntry);
				showThese.addAll(exits);
				exits.clear();
			}
			return showThese.toArray();
		}

		private void buildTableElements(DependentComponentNode dcn) {
			//First iterate through this level
			for (Integer childId : dcn.getChildren().keySet()) {
				DependentComponentNode child = dcn.getChildren().get(childId);
				if (child.getBreadCrumb() == false && child.getNameClashesFound()) {
					TableRow componentEntry = new TableRow();
					componentEntry.setDcn(child);
					child.setBreadCrumb(true);
					addComponentType(componentEntry, child.getComponentType());
				}
			}
			
			// now recurse to the children
			for (Integer childId : dcn.getChildren().keySet()) {
				DependentComponentNode child = dcn.getChildren().get(childId);
				TreeMap<Integer, DependentComponentNode> children = child.getChildren();
				if (children.size() > 0) {
					for (int i=0; i<children.size(); i++) {
						buildTableElements(child);
					}
				}
			}
		}
		
		private void addComponentType(TableRow row, ComponentType type) {
			switch(type) {
			case View:
				views.add(row);
				break;
			case LookupPath:
				lkups.add(row);
				break;
			case LogicalRecord:
				lrs.add(row);
				break;
			case LogicalFile:
				lfiles.add(row);
				break;
			case PhysicalFile:
				pfs.add(row);
				break;
			case UserExitRoutine:
				exits.add(row);
				break;
			case ControlRecord:
				ctrls.add(row);
				break;
			default:
				System.out.println("Well this is just weird!!!");
			}
		}
	}

	/**
	 * Inner Class to get the text or images to display
	 * in the table cells
	 * 
	 */
	private class ComponentLabelProvider 
	extends LabelProvider
	implements ITableLabelProvider {

		public ComponentLabelProvider() {
			super();
			// TODO Auto-generated constructor stub
		}

		public Image getColumnImage(Object element, int columnIndex) {

			Image img = null;
			DependentComponentNode dcn = ((TableRow)element).getDcn();
			if (dcn != null) {
				if (columnIndex == 2) {
					if ( dcn.getUseExisting()) {
						img = UIUtilities.getAndRegisterImage(ImageKeys.CHECKED);
					} else {
						img = UIUtilities.getAndRegisterImage(ImageKeys.UNCHECKED);
					}
				} 
				if (columnIndex == 3) {
					if ( dcn.getRemember()) {
						img = UIUtilities.getAndRegisterImage(ImageKeys.CHECKED);
					} else {
						img = UIUtilities.getAndRegisterImage(ImageKeys.UNCHECKED);
					}
				}
			}
			return img;
		}

		public String getColumnText(Object element, int columnIndex) {
			//Need to figure out which value to get based on the columnIndex

			String retval = "";
			DependentComponentNode dcn = ((TableRow)element).getDcn();
			switch(columnIndex) {
			case 0:
				if (dcn == null) {
					retval = ((TableRow)element).getTitle();
				} else {
					if ( dcn.getUserExec() == true) {
						retval = dcn.getOldName() + "(Exec)";
					}
					else {
						retval = dcn.getOldName();
					}
				}
				break;
			case 1:
				//if the new name has already been set then use it
				if (dcn != null) {
					if (dcn.getOldName() != null) {
						if (dcn.getNewName() != null && dcn.getUseExisting() == false) {
							retval = dcn.getNewName();
						}
					}
				}
				break;
			}
			return retval;
		}
	}

	// End of Inner Class declarations
	
	DependentComponentNode duplicates;
	Map<Class<? extends SAFRTransfer>, List<DependentComponentNode>> duplicatesMap;

	Text messageText;
	Table table;
	TableViewer tv;

	//keeping the columns here for resizing
	TableColumn oldNameCol;
	TableColumn newNameCol;
	TableColumn useExistingCol;

	private SAFRGUIToolkit guiToolkit = new SAFRGUIToolkit();

	public NewComponentNameDialog(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage,
			DependentComponentNode dcnRoot, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);

		this.duplicates = dcnRoot;

		Integer currentStyle = this.getShellStyle();
		this.setShellStyle(currentStyle|SWT.RESIZE);
	}

	protected Control createCustomArea(Composite parent) {
		createComposite(parent);
		return parent;
	}

	private void createComposite(Composite parent) {
		parent.setLayout(new GridLayout());

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace= true;

		final Composite gridTop = guiToolkit.createComposite(parent, SWT.BORDER);
		gridTop.setLayoutData(gridData);

		gridTop.setLayout(new FillLayout());
		
		final Composite composite = guiToolkit.createComposite(gridTop, SWT.BORDER);

		createTable(composite);
		createTableViewer();

		//LableDecoratorAdapter 
		tv.setContentProvider(new ComponentContentProvider());
		tv.setLabelProvider(new ComponentLabelProvider());
		tv.setInput(duplicates);
		tv.getTable().setLinesVisible(true);
		tv.getTable().setHeaderVisible(true);


		TableColumnLayout layout = new TableColumnLayout();
		composite.setLayout(layout);

		layout.setColumnData(table.getColumn(0), new ColumnWeightData(40));
		layout.setColumnData(table.getColumn(1), new ColumnWeightData(40));
		layout.setColumnData(table.getColumn(2), new ColumnWeightData(20));
		layout.setColumnData(table.getColumn(3), new ColumnWeightData(20));


		//helper area
		final Composite helperArea = guiToolkit.createComposite(parent, SWT.BORDER);

		helperArea.setLayout(new RowLayout());
		final Button chkRememberAll = new Button(helperArea, SWT.CHECK);
		chkRememberAll.setText("Remember All");
		chkRememberAll.setSelection(false); 
		chkRememberAll.setEnabled(true);

		chkRememberAll.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if (chkRememberAll.getSelection()) {
					duplicates.setRemember(true);
				} else {
					duplicates.setRemember(false);
				}
				tv.refresh();
			}
		});
	}


	private void createTable(Composite composite) {
		table = new Table(composite, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL|SWT.FULL_SELECTION);

		table.setBackground(composite.getBackground());
	}

	private void createTableViewer() {
		//
		tv = new TableViewer(table);		
		TableViewerColumn column = new TableViewerColumn(tv, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Old Name");
		column.getColumn().setMoveable(true);

		column = new TableViewerColumn(tv, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("New Name");
		column.getColumn().setMoveable(true);
		column.setEditingSupport(new NameEditor(tv));	

		column = new TableViewerColumn(tv, SWT.CHECK);
		column.getColumn().setWidth(100);
		column.getColumn().setText("Use Existing");
		column.getColumn().setMoveable(true);
		column.setEditingSupport(new UseExistingEditor(tv));	

		column = new TableViewerColumn(tv, SWT.CHECK);
		column.getColumn().setWidth(100);
		column.getColumn().setText("Remember");
		column.getColumn().setMoveable(true);
		column.setEditingSupport(new RememberEditor(tv));	
	}

	/**
	 * External static function called to by the Warning Strategy 
	 * to open the dialog
	 */
	public static int openDependencyDialog(Shell parentShell,
			String dialogTitle, String dialogMessage, DependentComponentNode dcnRoot,
			int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {

		NewComponentNameDialog dialog = new NewComponentNameDialog(
				parentShell, dialogTitle, null, dialogMessage, dcnRoot,
				dialogImageType, dialogButtonLabels, defaultIndex);

		int value = dialog.open();

		if (value == 1 || value == -1) {
			return 1;
		} else {
			return value;
		}
	}
}
