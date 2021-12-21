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
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.SAFRQueryBean;
import com.ibm.safr.we.ui.utilities.ISearchablePart;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.EnvironmentTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.GroupTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.LogicalFileTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.LogicalRecordTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.MainTableLabelProvider;
import com.ibm.safr.we.ui.views.metadatatable.PhysicalFileTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.UserExitRoutineTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.UserTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.ViewFolderTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.ViewTableSorter;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;

/**
 */
public class MetadataListDialog extends Dialog implements ISearchablePart {

	private SAFRGUIToolkit safrGUIToolkit = new SAFRGUIToolkit();
	private TreeItemId treeId;
	private SAFRPersistentObject object;
	private MainTableLabelProvider labelProvider;
	List<SAFRQueryBean> returnList = new ArrayList<SAFRQueryBean>();
	private CheckboxTableViewer tableViewerAssociations;
	Table tableAssociations;
	private int environmentId;
	private ComponentType component;
	List<SAFRQueryBean> beans = new ArrayList<SAFRQueryBean>();
	
	private String title = "Select components to be associated";

    private int prevSelection = 0; 


	/**
	 * Create an MetadataListDialog object.
	 * 
	 * @param parentShell
	 *            : shell.
	 * @param treeId
	 *            : one of the type of enum TreeItemId representing component
	 *            for which list need to be populated.
	 * 
	 * @param Object
	 *            : SafrPersistentObject required in case of retrieving list for
	 *            association otherwise optional.
	 * @param environmentId
	 *            : ID of the environment from which the list need to be
	 *            retrieved.
	 */
	public MetadataListDialog(Shell parentShell, TreeItemId treeId,
			SAFRPersistentObject object, int environmentId) {
		super(parentShell);
		this.treeId = treeId;
		this.object = object;
		this.environmentId = environmentId;
		setShellStyle(getShellStyle() | SWT.RESIZE); 
	}

	public MetadataListDialog(Shell parentShell, TreeItemId treeId,
			SAFRPersistentObject object, int environmentId,
			ComponentType component) {
		super(parentShell);
		this.treeId = treeId;
		this.object = object;
		this.environmentId = environmentId;
		this.component = component;
		setShellStyle(getShellStyle() | SWT.RESIZE); 
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		UIUtilities.setDialogBounds(550, 400, getShell());
		getShell().setText(title);
		getShell().setSize(550, 400);
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		ToolBar coolbar = new ToolBar(parent, SWT.FLAT);
		ToolBarManager coolManager = new ToolBarManager(coolbar);
		IMenuService m = (IMenuService) PlatformUI.getWorkbench().getService(
				IMenuService.class);

		m.populateContributionManager(coolManager,
				"toolbar:SAFRWE.metadataListDialogToolBar");
		coolManager.update(true);
		createAssociationsComposite(parent);
		return parent;
	}

	private void createAssociationsComposite(Composite parent) {
		Composite compositeAssociations = safrGUIToolkit.createComposite(
				parent, SWT.NONE);
		compositeAssociations.setLayout(new GridLayout());
		compositeAssociations.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite compositeTable = safrGUIToolkit.createComposite(
				compositeAssociations, SWT.NONE);
		compositeTable.setLayout(new GridLayout());
		compositeTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		tableAssociations = new Table(compositeTable, SWT.CHECK
				| SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tableViewerAssociations = new CheckboxTableViewer(tableAssociations);
		tableAssociations.setLayout(new FillLayout());
		tableAssociations.setLayoutData(new GridData(GridData.FILL_BOTH));
		createTable(tableViewerAssociations);
		if (treeId.equals(TreeItemId.GROUPCOMPONENT)) {
			labelProvider = new MainTableLabelProvider(component);
		} else {
			labelProvider = new MainTableLabelProvider();
		}
		
		tableAssociations.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              
              if (event.detail == SWT.CHECK) {                  
                  int selRow = tableAssociations.indexOf((TableItem)event.item);  
                  int stateMask=event.stateMask;                  
                  if((stateMask & SWT.SHIFT)==SWT.SHIFT){
                      int prevRow = prevSelection;
                      
                      if((stateMask & SWT.CTRL)!=SWT.CTRL){
                          tableViewerAssociations.setAllChecked(false);
                      }
                      if (prevRow > selRow) {
                          for (int i=selRow ; i<=prevRow ; i++) {
                              Object element = tableViewerAssociations.getElementAt(i);
                              tableViewerAssociations.setChecked(element, true);
                          }
                      }
                      else {
                          for (int i=prevRow ; i<=selRow ; i++) {
                              Object element = tableViewerAssociations.getElementAt(i);
                              tableViewerAssociations.setChecked(element, true);
                          }                            
                      }
                  }   
                  else {
                      Object element = tableViewerAssociations.getElementAt(selRow);
                      if (tableViewerAssociations.getChecked(element)) {
                          prevSelection = tableAssociations.indexOf((TableItem)event.item);
                      }
                      else {
                          prevSelection = 0;
                      }
                  }
              }                  
            }
          });
		
		
		tableViewerAssociations
				.setContentProvider(new AssociationContentProvider());
		labelProvider.setInput(treeId);
		tableViewerAssociations.setLabelProvider(labelProvider);

		tableViewerAssociations.setInput(treeId);
		if (treeId == TreeItemId.GROUPUSER) {
			tableViewerAssociations.getTable().setSortColumn(
					tableViewerAssociations.getTable().getColumn(0));
			tableViewerAssociations.getTable().setSortDirection(SWT.UP);
			setTableSorter(treeId, 0, SWT.UP);
		} else {
			tableViewerAssociations.getTable().setSortColumn(
					tableViewerAssociations.getTable().getColumn(1));
			tableViewerAssociations.getTable().setSortDirection(SWT.UP);
			setTableSorter(treeId, 1, SWT.UP);
		}
		// Code for tracking the focus on the table
		IFocusService service = (IFocusService) PlatformUI.getWorkbench()
				.getService(IFocusService.class);
		service.addFocusTracker(tableAssociations, "DialogSearchableTable");
		
	}

	private void createTable(TableViewer tabViewer) {
		Table table = tabViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		int iCounter = 0;
		String[] columnHeaders = new String[] { "ID", "Name" };
		int[] columnWidths = new int[] { 100, 300 };
		int length = columnWidths.length;
		for (iCounter = 0; iCounter < length; iCounter++) {
			TableViewerColumn column = new TableViewerColumn(tabViewer,
					SWT.NONE);
			column.getColumn().setText(columnHeaders[iCounter]);
			column.getColumn().setToolTipText(columnHeaders[iCounter]);
			column.getColumn().setWidth(columnWidths[iCounter]);
			column.getColumn().setResizable(true);
			ColumnSelectionListener colListener = new ColumnSelectionListener(
					tabViewer, iCounter);
			column.getColumn().addSelectionListener(colListener);
		}

	}

	public List<SAFRQueryBean> getReturnList() {
		return returnList;
	}

	private class ColumnSelectionListener extends SelectionAdapter {
		private int colNumber;
		private TableViewer tabViewer;

		private ColumnSelectionListener(TableViewer tabViewer, int colNumber) {
			this.colNumber = colNumber;
			this.tabViewer = tabViewer;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				getShell().setCursor(
						getShell().getDisplay()
								.getSystemCursor(SWT.CURSOR_WAIT));
				TableColumn sortColumn = tabViewer.getTable().getSortColumn();
				TableColumn currentColumn = (TableColumn) e.widget;
				int dir = tabViewer.getTable().getSortDirection();
				if (sortColumn == currentColumn) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					tabViewer.getTable().setSortColumn(currentColumn);
					// if the currentColumn is the column for ID field then
					// first
					// sorting direction is descending as its already sorted in
					// ascending order.
					if (currentColumn == tabViewer.getTable().getColumn(0)) {
						dir = SWT.DOWN;
					} else {
						dir = SWT.UP;
					}
				}

				tabViewer.getTable().setSortDirection(dir);
				setTableSorter(treeId, colNumber, dir);
			} finally {
				getShell().setCursor(null);

			}
		}
	}

	private class AssociationContentProvider implements IStructuredContentProvider {
		TreeItemId prevTreeItem;

		public Object[] getElements(Object inputElement) {
			try {

				if (!((TreeItemId) inputElement).equals(prevTreeItem)) {
					prevTreeItem = (TreeItemId) inputElement;
					beans.clear();
					if (((TreeItemId) inputElement).equals(TreeItemId.PHYSICALFILE)) {
						beans.addAll(SAFRQuery.queryPossiblePFAssociations(
						    (LogicalFile) object, environmentId));
						return beans.toArray();
					} else if (((TreeItemId) inputElement).equals(TreeItemId.LOGICALFILE)) {
						beans.addAll(SAFRQuery.queryPossibleLFAssociations(
						    (LogicalRecord) object, environmentId));
						return beans.toArray();
					} else if (((TreeItemId) inputElement).equals(TreeItemId.VIEWFOLDERCHILD)) {
						beans.addAll(SAFRQuery.queryPossibleViewAssociations(
						    (ViewFolder) object, environmentId));
						return beans.toArray();
					} else if (((TreeItemId) inputElement).equals(TreeItemId.VIEWFOLDER)) {
						beans.addAll(SAFRQuery.queryAllViewFolders(
						    environmentId, SortType.SORT_BY_NAME));
						return beans.toArray();
					} else if (((TreeItemId) inputElement).equals(TreeItemId.GROUPUSER)) {
						beans.addAll(SAFRQuery.queryPossibleUserAssociations((Group) object));
						return beans.toArray();
                    } else if (((TreeItemId) inputElement).equals(TreeItemId.USERGROUP)) {
                        beans.addAll(SAFRQuery.queryPossibleUserGroupAssociations((User) object));
                        return beans.toArray();
					} else if (((TreeItemId) inputElement).equals(TreeItemId.GROUPENV)) {
						beans.addAll(SAFRQuery.queryPossibleEnvironmentAssociations((Group) object));
						return beans.toArray();
					} else if (((TreeItemId) inputElement).equals(TreeItemId.GROUPCOMPONENT)) {
						beans.addAll(SAFRQuery.queryPossibleComponentAssociation(
						    component,(Group) object, environmentId));
						return beans.toArray();
					} else if (((TreeItemId) inputElement).equals(TreeItemId.ENVGROUP)) {
						beans.addAll(SAFRQuery.queryPossibleGroupAssociations((Environment) object));
						return beans.toArray();
					}
				} else {
					return beans.toArray();
				}
			} catch (DAOException e) {
				UIUtilities.handleWEExceptions(e);
			} catch (SAFRException e) {
				UIUtilities.handleWEExceptions(e);
			}
			return new String[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public void searchComponent(MetadataSearchCriteria searchCriteria,
			String searchText) {
		if (!beans.isEmpty()) {
			if (searchCriteria == MetadataSearchCriteria.ID) {
				// if search criteria is id, then sort the list of components
				// according to id.
				tableViewerAssociations.getTable().setSortColumn(
						tableAssociations.getColumn(0));
				tableViewerAssociations.getTable().setSortDirection(SWT.UP);
				setTableSorter(treeId, 0, SWT.UP);
			} else {
				// if search criteria is name, then sort the list of components
				// according to name.
				tableViewerAssociations.getTable().setSortColumn(
						tableAssociations.getColumn(1));
				tableViewerAssociations.getTable().setSortDirection(SWT.UP);

				setTableSorter(treeId, 1, SWT.UP);
			}
		}

		// get the items from the table and apply search.
		for (TableItem item : tableAssociations.getItems()) {
			SAFRQueryBean bean = (SAFRQueryBean) item.getData();
			if (searchCriteria == MetadataSearchCriteria.ID) {
				if (bean.getIdLabel().toLowerCase().startsWith(
						searchText.toLowerCase())) {
					tableViewerAssociations.setSelection(
							new StructuredSelection(bean), true);
					return;
				}
			} else if (searchCriteria == MetadataSearchCriteria.NAME) {
				if (!bean.getNameLabel().equals(null)
						&& bean.getNameLabel().toLowerCase().startsWith(
								searchText.toLowerCase())) {
					tableViewerAssociations.setSelection(
							new StructuredSelection(bean), true);
					return;
				}
			}
		}

		// if no component is found, then show the dialog box.
		MessageDialog
				.openInformation(getShell(), "Component not found",
						"The component you are trying to search is not found in the list.");

	}

	private void setTableSorter(TreeItemId treeId, int colNumber, int dir) {
		Object[] selItems = tableViewerAssociations.getCheckedElements();
		if (treeId == TreeItemId.PHYSICALFILE) {
			tableViewerAssociations.setSorter(new PhysicalFileTableSorter(colNumber, dir));
		} else if (treeId == TreeItemId.LOGICALFILE) {
			tableViewerAssociations.setSorter(new LogicalFileTableSorter(colNumber, dir));
		} else if (treeId == TreeItemId.VIEWFOLDERCHILD) {
			tableViewerAssociations.setSorter(new ViewTableSorter(colNumber,dir));
		} else if (treeId == TreeItemId.VIEWFOLDER) {
			tableViewerAssociations.setSorter(new ViewFolderTableSorter(colNumber, dir));
		} else if (treeId == TreeItemId.GROUPUSER) {
			tableViewerAssociations.setSorter(new UserTableSorter(colNumber,dir));
        } else if (treeId == TreeItemId.USERGROUP) {
            tableViewerAssociations.setSorter(new GroupTableSorter(colNumber,dir));
		} else if (treeId == TreeItemId.GROUPENV) {
			tableViewerAssociations.setSorter(new EnvironmentTableSorter(colNumber, dir));
		} else if (treeId == TreeItemId.ENVGROUP) {
			tableViewerAssociations.setSorter(new GroupTableSorter(colNumber,dir));
		} else if (treeId == TreeItemId.GROUPCOMPONENT) {
			if (component != null && component == ComponentType.LogicalFile) {
				tableViewerAssociations.setSorter(new LogicalFileTableSorter(colNumber, dir));
			} else if (component != null && component == ComponentType.LogicalRecord) {
				tableViewerAssociations.setSorter(new LogicalRecordTableSorter(colNumber, dir));
			} else if (component != null && component == ComponentType.PhysicalFile) {
				tableViewerAssociations.setSorter(new PhysicalFileTableSorter(colNumber, dir));
			} else if (component != null && component == ComponentType.UserExitRoutine) {
				tableViewerAssociations.setSorter(new UserExitRoutineTableSorter(colNumber,dir));
			} else if (component != null && component == ComponentType.ViewFolder) {
				tableViewerAssociations.setSorter(new ViewFolderTableSorter(colNumber, dir));
			}
		}
		SAFRQueryBean[] sBeans = new SAFRQueryBean[selItems.length];
		int i = 0;
		for (Object object : selItems) {
			sBeans[i++] = (SAFRQueryBean) object;
		}
		setCheckedElements(sBeans);
	}

	public ComponentType getComponentType() {
		if (treeId == TreeItemId.PHYSICALFILE) {
			return ComponentType.PhysicalFile;
		} else if (treeId == TreeItemId.LOGICALFILE) {
			return ComponentType.LogicalFile;
		} else if (treeId == TreeItemId.VIEWFOLDERCHILD) {
			return ComponentType.View;
		} else if (treeId == TreeItemId.VIEWFOLDER) {
			return ComponentType.ViewFolder;
		} else if (treeId == TreeItemId.GROUPUSER) {
			return ComponentType.User;
        } else if (treeId == TreeItemId.USERGROUP) {
            return ComponentType.User;
		} else if (treeId == TreeItemId.GROUPENV) {
			return ComponentType.Environment;
		} else if (treeId == TreeItemId.ENVGROUP) {
			return ComponentType.Group;
		} else if (treeId == TreeItemId.GROUPCOMPONENT) {
			if (component != null && component == ComponentType.LogicalFile) {
				return ComponentType.LogicalFile;
			} else if (component != null
					&& component == ComponentType.LogicalRecord) {
				return ComponentType.LogicalRecord;
			} else if (component != null
					&& component == ComponentType.PhysicalFile) {
				return ComponentType.PhysicalFile;
			} else if (component != null
					&& component == ComponentType.UserExitRoutine) {
				return ComponentType.UserExitRoutine;
			} else if (component != null
					&& component == ComponentType.ViewFolder) {
				return ComponentType.ViewFolder;
			}
		}
		return null;
	}

	/**
	 * 
	 * This method sets the specified table items in {@link MetadataListDialog}
	 * checked.The array contains the {@link SAFRQueryBean} objects that are to
	 * be checked in the table.
	 * 
	 * @param checkedElements
	 *            array of {@link SAFRQueryBean} objects that are to be checked
	 *            in the table.
	 */
	public void setCheckedElements(SAFRQueryBean[] checkedElements) {
		Set<String> set = new TreeSet<String>();
		for (SAFRQueryBean sQueryBean : checkedElements) {
			set.add(sQueryBean.getIdLabel());
		}
		TableItem[] items = tableViewerAssociations.getTable().getItems();
		for (int i = 0; i < items.length; ++i) {
			TableItem item = items[i];
			item.setChecked(set.contains(item.getText(0)));
		}
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.SELECT_ALL_ID, "&Select All", false);
		createButton(parent, IDialogConstants.DESELECT_ALL_ID, "&DeSelect All", false);
		createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
	}
	
	protected void buttonPressed(int buttonId) {
		boolean set = false;
		boolean process = false;
		switch(buttonId) {
		case IDialogConstants.SELECT_ALL_ID: {
			set = true;	
			process = true;
			prevSelection = 0;
			break;
		}
		case IDialogConstants.DESELECT_ALL_ID: {
			set = false;			
			process = true;
            prevSelection = 0;
			break;
		}
		case IDialogConstants.CANCEL_ID: {
			super.close();
			break;
		}
		case IDialogConstants.OK_ID: {
			for (Object obj : tableViewerAssociations.getCheckedElements()) {
				returnList.add((SAFRQueryBean) obj);
			}
			super.okPressed();
			break;
		}
		}
		
		if (process == true) {
			TableItem[] items = tableViewerAssociations.getTable().getItems();
			for (int i = 0; i < items.length; ++i) {
				TableItem item = items[i];
                item.setChecked(set);
			}
			tableViewerAssociations.refresh();
		}
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
}
