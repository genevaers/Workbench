package com.ibm.safr.we.ui.editors.permission;

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
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.associations.GroupUserAssociation;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.GroupQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.SAFRQueryBean;
import com.ibm.safr.we.model.query.UserQueryBean;
import com.ibm.safr.we.ui.dialogs.MetadataListDialog;
import com.ibm.safr.we.ui.editors.OpenEditorPopupState;
import com.ibm.safr.we.ui.editors.SAFREditorPart;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.ISearchablePart;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * This class creates the editor area and also the controls for Group-User
 * Membership.
 * 
 */
public class GroupMembershipEditor extends SAFREditorPart implements
		ISearchablePart {

	public static String ID = "SAFRWE.GroupMembershipEditor";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;
	private Section sectionGroups;
	private TableCombo comboGroup;
	private CheckboxTableViewer tableViewerUsers;
	private Table tableUsers;
	private Composite compositeUsersButtonBar;
	private Button buttonAdd;
	private Button buttonRemove;
	private Section sectionUsers;
	private Composite compositeGroup;
	private Group currentGroup;
	HashMap<Group, Group> map = new HashMap<Group, Group>();
	private String selectedGroupIndex = "";
	private TableComboViewer comboGroupViewer;
	private List<GroupQueryBean> groupList;
    private int prevSelection = 0;
    private MenuItem grpOpenEditorItem = null;		
    
	@Override
	public void createPartControl(Composite parent) {

		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(UIUtilities.createTableLayout(1, false));
		try {
			createSectionGroups(form.getBody());
			createSectionUsers(form.getBody());
		} catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e);
		}

	}

	/**
	 * This method creates Groups section.
	 * 
	 * @param body
	 * @throws DAOException
	 * @throws SAFRException
	 */
	private void createSectionGroups(Composite body) throws SAFRException,
			DAOException {
		sectionGroups = safrGuiToolkit.createSection(body, Section.TITLE_BAR,
				"Groups:");
		sectionGroups.setLayoutData(new TableWrapData(TableWrapData.LEFT,
				TableWrapData.FILL_GRAB));

		compositeGroup = safrGuiToolkit
				.createComposite(sectionGroups, SWT.NONE);
		compositeGroup.setLayout(UIUtilities.createTableLayout(2, true));
		safrGuiToolkit.createLabel(compositeGroup, SWT.NONE,
				"&Group: ");

		comboGroupViewer = safrGuiToolkit.createTableComboForComponents(
				compositeGroup, ComponentType.Group);
		comboGroup = comboGroupViewer.getTableCombo();
		comboGroup.setData(SAFRLogger.USER, "Group");                                                        		
		addGrpOpenEditorMenu();		
		comboGroup.setLayoutData(UIUtilities.textTableData(1));

		Label filler = safrGuiToolkit.createLabel(compositeGroup, SWT.NONE, "");
		filler.setLayoutData(UIUtilities.textTableData(2));

		comboGroup.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (selectedGroupIndex.equals(comboGroup.getText())) {
					return;
				}
				buttonAdd.setEnabled(true);
				buttonRemove.setEnabled(false);
				if (isDirty()) {
					// Show confirmation message to the user.
					int userResponse = 0;
					MessageDialog dialog = new MessageDialog(
							getSite().getShell(),
							"Save Changes?",
							null,
							"Do you wish to save the changes made to the current group before selecting another group?",
							MessageDialog.QUESTION, new String[] { "&Yes",
									"&No", "&Cancel" }, 0);
					userResponse = dialog.open();
					// if user clicks CANCEL
					if (userResponse == 2) {
						comboGroup.select(comboGroup
								.indexOf(selectedGroupIndex));
						return;
					} else if (userResponse == 0) {
						// if user clicks YES
						try {
							storeModel();
						} catch (DAOException e1) {
							UIUtilities.handleWEExceptions(e1,"Error occurred while storing associated users.",UIUtilities.titleStringDbException);
						} catch (SAFRException e1) {
							UIUtilities.handleWEExceptions(e1,"Error occurred while storing associated users.",null);
						}

					}

					GroupQueryBean groupQueryBean = (GroupQueryBean) comboGroup
							.getTable().getSelection()[0].getData();

					try {
						currentGroup = SAFRApplication.getSAFRFactory()
								.getGroup(groupQueryBean.getId());
					} catch (SAFRException e1) {
						UIUtilities.handleWEExceptions(e1,"Error occurred while retrieving the group.",null);
					}
					setDirty(false);
					tableViewerUsers.refresh();
				} else {
					GroupQueryBean groupQueryBean = (GroupQueryBean) comboGroup
							.getTable().getSelection()[0].getData();

					try {
						currentGroup = SAFRApplication.getSAFRFactory()
								.getGroup(groupQueryBean.getId());
					} catch (SAFRException e1) {
						UIUtilities.handleWEExceptions(e1,"Error occurred while retrieving the group.",null);
					}
					tableViewerUsers.refresh();
				}
				selectedGroupIndex = comboGroup.getText();
			}
		});

		// load the data
		try {
			groupList = SAFRQuery.queryAllGroups(SortType.SORT_BY_NAME);

		} catch (DAOException e1) {
			UIUtilities.handleWEExceptions(e1);
		}
		Integer counter = 0;
		if (groupList != null) {
			for (GroupQueryBean groupQueryBean : groupList) {
				comboGroup.setData(Integer.toString(counter), groupQueryBean);
				counter++;
			}
		}

		comboGroupViewer.setInput(groupList);

		sectionGroups.setClient(compositeGroup);
	}

    private void addGrpOpenEditorMenu()
    {
        Text text = comboGroup.getTextControl();
        Menu menu = text.getMenu();
        grpOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        grpOpenEditorItem.setText("Open Editor");
        grpOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
            	GroupQueryBean bean = (GroupQueryBean)((StructuredSelection) comboGroupViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.Group);                        
                }                
            }
        });
        
        comboGroup.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                	GroupQueryBean bean = (GroupQueryBean)((StructuredSelection) comboGroupViewer
                            .getSelection()).getFirstElement();
                    if (bean != null) {   
                        grpOpenEditorItem.setEnabled(true);                            
                    }
                    else {
                        grpOpenEditorItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }       
	
	/**
	 * This method creates the Users section.
	 * 
	 * @param body1
	 * @throws DAOException
	 */
	private void createSectionUsers(Composite body1) throws DAOException {
		sectionUsers = safrGuiToolkit.createSection(body1, Section.TITLE_BAR,
				"Associated Users:");
		sectionUsers.setLayoutData(new TableWrapData(TableWrapData.LEFT,
				TableWrapData.FILL_GRAB));

		Composite compositeUsers = safrGuiToolkit.createComposite(sectionUsers,
				SWT.NONE);
		FormLayout layoutUsers = new FormLayout();
		layoutUsers.marginTop = 5;
		layoutUsers.marginBottom = 5;
		layoutUsers.marginLeft = 5;
		layoutUsers.marginRight = 5;
		compositeUsers.setLayout(layoutUsers);
		tableUsers = new Table(compositeUsers, SWT.CHECK | SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tableUsers.setData(SAFRLogger.USER, "User Table");                                                        		
		tableViewerUsers = new CheckboxTableViewer(tableUsers);

		FormData dataUsers = new FormData();
		dataUsers.left = new FormAttachment(0, 0);
		dataUsers.top = new FormAttachment(0, 10);
		dataUsers.bottom = new FormAttachment(100, 0);
		dataUsers.height = 250;
		tableUsers.setLayoutData(dataUsers);
		toolkit.adapt(tableUsers, false, false);
		tableUsers.setHeaderVisible(true);
		tableUsers.setLinesVisible(true);

		tableUsers.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              
              if (event.detail == SWT.CHECK) {                  
                  int selRow = tableUsers.indexOf((TableItem)event.item);  
                  int stateMask=event.stateMask;                  
                  if((stateMask & SWT.SHIFT)==SWT.SHIFT){
                      int prevRow = prevSelection;
                      
                      if((stateMask & SWT.CTRL)!=SWT.CTRL){
                          tableViewerUsers.setAllChecked(false);
                     }
                      if (prevRow > selRow) {
                          for (int i=selRow ; i<=prevRow ; i++) {
                              Object element = tableViewerUsers.getElementAt(i);
                              tableViewerUsers.setChecked(element, true);
                          }
                      }
                      else {
                          for (int i=prevRow ; i<=selRow ; i++) {
                              Object element = tableViewerUsers.getElementAt(i);
                              tableViewerUsers.setChecked(element, true);
                          }                            
                      }
                  }   
                  else {
                      Object element = tableViewerUsers.getElementAt(selRow);
                      if (tableViewerUsers.getChecked(element)) {
                          prevSelection = tableUsers.indexOf((TableItem)event.item);
                      }
                      else {
                          prevSelection = 0;
                      }
                  }
              }                  
            }
          });
		
		int iCounter = 0;
		String[] columnHeaders = { "User ID", "Full Name" };
		int[] columnWidths = { 100, 250 };
		int length = columnWidths.length;
		for (iCounter = 0; iCounter < length; iCounter++) {
			TableViewerColumn column = new TableViewerColumn(tableViewerUsers,
					SWT.NONE);
			column.getColumn().setText(columnHeaders[iCounter]);
			column.getColumn().setToolTipText(columnHeaders[iCounter]);
			column.getColumn().setWidth(columnWidths[iCounter]);
			column.getColumn().setResizable(true);

			ColumnSelectionListenerTable colListener = new ColumnSelectionListenerTable(
					tableViewerUsers, iCounter);
			column.getColumn().addSelectionListener(colListener);
		}

		tableViewerUsers.setContentProvider(new IStructuredContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}

			public void dispose() {

			}

			public Object[] getElements(Object inputElement) {
				try {
					getSite().getShell().setCursor(
							getSite().getShell().getDisplay().getSystemCursor(
									SWT.CURSOR_WAIT));
					if (currentGroup != null) {
						return currentGroup.getGroupUserAssociations()
								.getActiveItems().toArray();
					}
					return new String[0];
				} finally {
					getSite().getShell().setCursor(null);
				}
			}

		});

		tableViewerUsers.setLabelProvider(new GroupUserLabelProvider());
		tableViewerUsers.setInput(1);
		tableViewerUsers.getTable().setSortColumn(tableViewerUsers.getTable().getColumn(0));
		tableViewerUsers.getTable().setSortDirection(SWT.UP);
		tableViewerUsers.setSorter(new GroupUserRightTableSorter(0, SWT.UP));

		// CQ 8469 Kanchan Rauthan 3/09/2010 To disable remove button if nothing
		// is
		// selected to remove.
		tableViewerUsers.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				if (tableViewerUsers.getCheckedElements().length > 0) {
					buttonRemove.setEnabled(true);
				} else {
					buttonRemove.setEnabled(false);
				}
			}
		});

		tableViewerUsers.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	GroupUserAssociation node = (GroupUserAssociation)((StructuredSelection) event
                        .getSelection()).getFirstElement();
                if (node != null) {
                    setPopupEnabled(true);
                }
                else {
                    setPopupEnabled(false);                        
                }
            }
        });
		
		compositeUsersButtonBar = safrGuiToolkit.createComposite(
				compositeUsers, SWT.NONE);
		compositeUsersButtonBar.setLayout(new FormLayout());
		FormData dataButtonBar = new FormData();
		dataButtonBar.left = new FormAttachment(tableUsers, 10);
		dataButtonBar.top = new FormAttachment(0, 10);
		dataButtonBar.bottom = new FormAttachment(100, 0);
		compositeUsersButtonBar.setLayoutData(dataButtonBar);

		buttonAdd = toolkit.createButton(compositeUsersButtonBar, "A&dd",
				SWT.PUSH);
		buttonAdd.setData(SAFRLogger.USER, "Add");                                                                		
		FormData dataButtonAdd = new FormData();
		dataButtonAdd.left = new FormAttachment(0, 5);
		dataButtonAdd.width = 90;
		buttonAdd.setLayoutData(dataButtonAdd);
		buttonAdd.setEnabled(false);
		buttonAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentGroup != null) {
					// Display an hour glass till list of Users is populated.
					Display.getCurrent().getActiveShell().setCursor(
							Display.getCurrent().getActiveShell().getDisplay()
									.getSystemCursor(SWT.CURSOR_WAIT));

					MetadataListDialog dialog = new MetadataListDialog(
							getSite().getShell(), TreeItemId.GROUPUSER,
							currentGroup, UIUtilities.getCurrentEnvironmentID());
					dialog.open();

					List<SAFRQueryBean> returnList;
					if (dialog.getReturnCode() == Window.OK) {
						returnList = dialog.getReturnList();
						List<UserQueryBean> tmpList = new ArrayList<UserQueryBean>();
						GroupUserAssociation groupUser = null;
						SAFRList<GroupUserAssociation> currAssoc = currentGroup
								.getGroupUserAssociations();

						for (int i = 0; i < returnList.size(); i++) {
							UserQueryBean bean = (UserQueryBean) returnList
									.get(i);

							groupUser = new GroupUserAssociation(currentGroup,
									bean.getName(), bean.getId());
							currAssoc.add(groupUser);
							tmpList.add(bean);
							setDirty(true);

						}
						map.put(currentGroup, currentGroup);
						tableViewerUsers.refresh();
					}
					// return cursor to normal
					Display.getCurrent().getActiveShell().setCursor(null);
				}
			}
		});

		// Code for tracking the focus on the table
		IFocusService service = (IFocusService) getSite().getService(
				IFocusService.class);
		service.addFocusTracker(tableUsers, "UserSearchableTable");

		buttonRemove = toolkit.createButton(compositeUsersButtonBar, "Re&move",
				SWT.PUSH);
		buttonRemove.setData(SAFRLogger.USER, "Remove");                                                                        		
		FormData dataButtonRemove = new FormData();
		dataButtonRemove.left = new FormAttachment(0, 5);
		dataButtonRemove.top = new FormAttachment(buttonAdd, 5);
		dataButtonRemove.width = 90;
		buttonRemove.setLayoutData(dataButtonRemove);
		buttonRemove.setEnabled(false);
		buttonRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentGroup != null) {
					Object[] items = tableViewerUsers.getCheckedElements();
					if (items.length > 0) {
						List<GroupUserAssociation> tmpList = new ArrayList<GroupUserAssociation>();
						SAFRList<GroupUserAssociation> currList = currentGroup
								.getGroupUserAssociations();
						for (Object item : items) {
							GroupUserAssociation groupUserAssociation = (GroupUserAssociation) item;
							currList.remove(groupUserAssociation);
							tmpList.add(groupUserAssociation);
						}
						setDirty(true);
						map.put(currentGroup, currentGroup);
						tableViewerUsers.refresh();
					}
				}
				buttonRemove.setEnabled(false);
			}
		});
		sectionUsers.setClient(compositeUsers);
		
        // Code for Context menu
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableUsers);
        tableUsers.setMenu(menu);
        getSite().registerContextMenu(menuManager, tableViewerUsers);        
        setPopupEnabled(false);     
		
	}

    private void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.GRPMEM);
        service.setGrpMem(enabled);
    }
	
    public GroupUserAssociation getCurrentSelection() {
        return (GroupUserAssociation)((StructuredSelection)tableViewerUsers.getSelection()).getFirstElement();
    }    
	
	
	/**
	 * 
	 * This inner class is used for column sorting in the Table.
	 */
	private class ColumnSelectionListenerTable extends SelectionAdapter {
		private int colNumber;
		private TableViewer tabViewer;

		ColumnSelectionListenerTable(TableViewer tabViewer, int colNumber) {
			this.colNumber = colNumber;
			this.tabViewer = tabViewer;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			try {

				getSite().getShell().setCursor(
						getSite().getShell().getDisplay().getSystemCursor(
								SWT.CURSOR_WAIT));
				TableColumn sortColumn = tabViewer.getTable().getSortColumn();
				TableColumn currentColumn = (TableColumn) e.widget;
				int dir = tabViewer.getTable().getSortDirection();
				if (sortColumn == currentColumn) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					tabViewer.getTable().setSortColumn(currentColumn);
					if (currentColumn == tabViewer.getTable().getColumn(0)) {
						dir = SWT.DOWN;
					} else {
						dir = SWT.UP;
					}
				}

				tabViewer.getTable().setSortDirection(dir);
				tabViewer.setSorter(new GroupUserRightTableSorter(colNumber,
						dir));
			} finally {
				getSite().getShell().setCursor(null);

			}
		}
	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public String getModelName() {
		return "Group - User Membership";
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doRefreshControls() throws SAFRException {

	}

	@Override
	public void refreshModel() {

	}

	@Override
	public void setFocus() {
		comboGroup.setFocus();
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
		currentGroup.storeAssociatedUsers();
	}

	@Override
	public void validate() throws DAOException, SAFRException {

	}

	@Override
	public ComponentType getEditorCompType() {
		return null;
	}

	@Override
	public SAFRPersistentObject getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	public void searchComponent(MetadataSearchCriteria searchCriteria,
			String searchText) {
		TableViewer tabViewer = null;
		if (this.tableUsers.isFocusControl()) {
			tabViewer = this.tableViewerUsers;
		}
		if (tabViewer != null) {
			// if search criteria is id, then sort the list of components
			// according to id.
			if (searchCriteria == MetadataSearchCriteria.ID) {
				tabViewer.getTable().setSortColumn(
						tabViewer.getTable().getColumn(0));
				tabViewer.getTable().setSortDirection(SWT.UP);
				tabViewer.setSorter(new GroupUserRightTableSorter(0, SWT.UP));
			} else {
				// if search criteria is name, then sort the list of components
				// according to name.
				tabViewer.getTable().setSortColumn(
						tabViewer.getTable().getColumn(1));
				tabViewer.getTable().setSortDirection(SWT.UP);
				tabViewer.setSorter(new GroupUserRightTableSorter(1, SWT.UP));
			}

			// get the items of the table and apply search.
			for (TableItem item : tabViewer.getTable().getItems()) {
				GroupUserAssociation bean = (GroupUserAssociation) item
						.getData();
				if (searchCriteria == MetadataSearchCriteria.ID) {
					if (bean.getAssociatedComponentIdString().toLowerCase()
							.startsWith(searchText.toLowerCase())) {
						tabViewer.setSelection(new StructuredSelection(bean),
								true);
						return;
					}
				} else if (searchCriteria == MetadataSearchCriteria.NAME) {
					if (bean.getAssociatedComponentName() != null
							&& bean.getAssociatedComponentName().toLowerCase()
									.startsWith(searchText.toLowerCase())) {
						tabViewer.setSelection(new StructuredSelection(bean),
								true);
						return;
					}
				}
			}

			// if no component is found, then show the dialog box.
			MessageDialog
					.openInformation(getSite().getShell(),
							"Component not found",
							"The component you are trying to search is not found in the list.");

		}
	}

	public ComponentType getComponentType() {
		if (tableUsers.isFocusControl()) {
			return ComponentType.User;
		}
		return null;
	}

	@Override
	public String getComponentNameForSaveAs() {
		return null;
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		return null;
	}

}
