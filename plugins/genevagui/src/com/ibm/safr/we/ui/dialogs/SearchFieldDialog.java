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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.query.LogicalRecordFieldQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.ui.editors.batchlookup.ActivateLookupPathsEditor;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditor;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditorInput;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordFieldEditor;
import com.ibm.safr.we.ui.utilities.LogicalRecordFieldTableSorter;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class SearchFieldDialog extends TitleAreaDialog implements Listener{


	private SAFRGUIToolkit safrGuiToolkit;
	private Composite composite;
    private TableComboViewer comboLRFieldViewer; 
    private TableCombo comboLRField;


	public SearchFieldDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER
				| SWT.APPLICATION_MODAL | SWT.RESIZE);
		safrGuiToolkit = new SAFRGUIToolkit();
	        
	}

	protected Control createDialogArea(Composite parent) {

		composite = safrGuiToolkit.createComposite(parent,SWT.BORDER);
		
        GridLayout grid1 = new GridLayout();
        grid1.numColumns = 4;
        grid1.makeColumnsEqualWidth = false;
        composite.setLayout(grid1);
        
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
	   	 Label databaseType1 = safrGuiToolkit.createLabel(composite, SWT.NONE,"Field Name");
	     databaseType1.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,false));
	     comboLRFieldViewer = safrGuiToolkit.createTableComboForComponents(
					composite, ComponentType.LogicalRecordField);
	     comboLRField = comboLRFieldViewer.getTableCombo();     
	
	     GridData gridCombo = new GridData(GridData.FILL_HORIZONTAL);    
	     comboLRField.setLayoutData(gridCombo);
	     comboLRField.addListener(SWT.FocusIn, this);
	     comboLRField.addListener(SWT.Modify, this);
	     comboLRFieldViewer.setSorter(new LogicalRecordFieldTableSorter(1,
					SWT.UP));        
	     
	     comboLRFieldViewer.addSelectionChangedListener(new ISelectionChangedListener() {

	            public void selectionChanged(SelectionChangedEvent event) {
	                LogicalRecordFieldQueryBean node = (LogicalRecordFieldQueryBean)((StructuredSelection) event.getSelection()).getFirstElement();
	            	LogicalRecordFieldEditor.tableViewerLRFields.setSelection((StructuredSelection) event.getSelection(),true);
	            	 boolean found=false;
	                 int row=0;
	            	 for (GridItem item : LogicalRecordFieldEditor.tableViewerLRFields.getGrid().getItems()) {
	                     LRField field = (LRField)item.getData();
	                     if (field.getId().equals(node.getId())) {
	                    	 LogicalRecordFieldEditor.tableViewerLRFields.getGrid().setFocusItem(item);
	                    	 LogicalRecordFieldEditor.tableViewerLRFields.getGrid().showItem(item);
	                    	 found = true;
	                         break;
	                     }
	                     row++;
	                 }	       
	            	 if (found) {
	            		 LogicalRecordFieldEditor.tableViewerLRFields.getGrid().select(row);
	            		 LogicalRecordFieldEditor.tableViewerLRFields.getGrid().setFocusColumn( LogicalRecordFieldEditor.tableViewerLRFields.getGrid().getColumn(0));
	                 }
	            	 }
	        });
	     
	     
		 composite.setLayoutData(data);
		 
		 
	        populateLogicalRecord(comboLRField);
	
			return parent;
	}
	private void populateLogicalRecord(TableCombo comboSourceLR) throws DAOException {

		Integer counter = 0;

		comboLRField.getTable().removeAll();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

    	LogicalRecordEditorInput editor = (LogicalRecordEditorInput)page.getActiveEditor().getEditorInput();
		List<LogicalRecordFieldQueryBean> logicalFieldList = SAFRQuery.queryAllLogicalRecordFields(UIUtilities.getCurrentEnvironmentID(),editor.getLogicalRecord().getId() ,SortType.SORT_BY_NAME);
				
		List<LogicalRecordFieldQueryBean> listWithoutDuplicates = new ArrayList<>(new HashSet<>(logicalFieldList));

		comboLRFieldViewer.setInput(listWithoutDuplicates);
		comboLRFieldViewer.refresh();
		for (LogicalRecordFieldQueryBean logicalRecordBean : listWithoutDuplicates) {
			comboLRFieldViewer.setData(Integer.toString(counter), logicalRecordBean);
			counter++;
		}
	}
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		setTitle("Search Field Dialog");

		setMessage("Select Field to Search :",
				IMessageProvider.INFORMATION);
		getShell().setText("GenevaERS Field Search");
		
		return contents;
	}
	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		
	}
}
