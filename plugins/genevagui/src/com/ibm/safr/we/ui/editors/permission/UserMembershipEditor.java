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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.model.associations.UserGroupAssociation;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.GroupQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.SAFRQueryBean;
import com.ibm.safr.we.model.query.UserQueryBean;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.dialogs.MetadataListDialog;
import com.ibm.safr.we.ui.editors.OpenEditorPopupState;
import com.ibm.safr.we.ui.editors.SAFREditorPart;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.ISearchablePart;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.SAFRTableComboViewer;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * This class creates the editor area and also the controls for Group-User
 * Membership.
 * 
 */
public class UserMembershipEditor extends SAFREditorPart implements
		ISearchablePart {

	public static String ID = "SAFRWE.UserMembershipEditor";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;
	
	private Section sectionUsers;
    private Composite compositeUser;
    private TableComboViewer comboUserViewer;
	private TableCombo comboUser;
    private User currentUser;
    private String selectedUserIndex = "";
    private List<UserQueryBean> userList;
	
    private Section sectionGroups;
	private CheckboxTableViewer tableViewerGroups;
	private Table tableGroups;
	private Composite compositeGroupsButtonBar;
	private Button buttonAdd;
	private Button buttonRemove;
	HashMap<Group, Group> map = new HashMap<Group, Group>();
    private int prevSelection = 0;
    private MenuItem userOpenEditorItem = null;		
    
	@Override
	public void createPartControl(Composite parent) {

		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(UIUtilities.createTableLayout(1, false));
		createSectionUsers(form.getBody());
		createSectionGroups(form.getBody());
	}

	/**
	 * This method creates Groups section.
	 * 
	 * @param body
	 * @throws DAOException
	 * @throws SAFRException
	 */
	private void createSectionUsers(Composite body) throws SAFRException,
			DAOException {
		sectionUsers = safrGuiToolkit.createSection(body, Section.TITLE_BAR,"Users:");
		sectionUsers.setLayoutData(new TableWrapData(TableWrapData.LEFT,TableWrapData.FILL_GRAB));

		compositeUser = safrGuiToolkit.createComposite(sectionUsers, SWT.NONE);
		compositeUser.setLayout(UIUtilities.createTableLayout(2, true));
		safrGuiToolkit.createLabel(compositeUser, SWT.NONE,"&User: ");

		comboUserViewer = createTableComboForUsers(compositeUser);
		comboUser = comboUserViewer.getTableCombo();
		comboUser.setData(SAFRLogger.USER, "User");                                                        		
		addUserOpenEditorMenu();		
		comboUser.setLayoutData(UIUtilities.textTableData(1));

		Label filler = safrGuiToolkit.createLabel(compositeUser, SWT.NONE, "");
		filler.setLayoutData(UIUtilities.textTableData(2));

		comboUser.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (selectedUserIndex.equals(comboUser.getText())) {
					return;
				}
				buttonAdd.setEnabled(true);
				buttonRemove.setEnabled(false);
				if (isDirty()) {
					// Show confirmation message to the user.
					int userResponse = 0;
					MessageDialog dialog = new MessageDialog(getSite().getShell(),"Save Changes?",null,
							"Do you wish to save the changes made to the current user before selecting another user?",
							MessageDialog.QUESTION, new String[] { "&Yes","&No", "&Cancel" }, 0);
					userResponse = dialog.open();
					// if user clicks CANCEL
					if (userResponse == 2) {
						comboUser.select(comboUser.indexOf(selectedUserIndex));
						return;
					} else if (userResponse == 0) {
						// if user clicks YES
						try {
							storeModel();
						} catch (DAOException e1) {
							UIUtilities.handleWEExceptions(e1,
							    "Error occurred while storing associated groups.",UIUtilities.titleStringDbException);
						} catch (SAFRException e1) {
							UIUtilities.handleWEExceptions(e1,"Error occurred while storing associated groups.",null);
						}
					}

					UserQueryBean userQueryBean = (UserQueryBean) comboUser.getTable().getSelection()[0].getData();

					try {
						currentUser = SAFRApplication.getSAFRFactory().getUser(userQueryBean.getId());
					} catch (SAFRException e1) {
						UIUtilities.handleWEExceptions(e1,"Error occurred while retrieving the group.",null);
					}
					setDirty(false);
					tableViewerGroups.refresh();
				} else {
					UserQueryBean groupQueryBean = (UserQueryBean) comboUser.getTable().getSelection()[0].getData();

					try {
						currentUser = SAFRApplication.getSAFRFactory().getUser(groupQueryBean.getId());
					} catch (SAFRException e1) {
						UIUtilities.handleWEExceptions(e1,"Error occurred while retrieving the group.",null);
					}
					tableViewerGroups.refresh();
				}
				selectedUserIndex = comboUser.getText();
			}
		});

		// load the data
		try {
			userList = SAFRQuery.queryAllUsers();

		} catch (DAOException e1) {
			UIUtilities.handleWEExceptions(e1);
		}
		Integer counter = 0;
		if (userList != null) {
			for (UserQueryBean groupQueryBean : userList) {
				comboUser.setData(Integer.toString(counter), groupQueryBean);
				counter++;
			}
		}
		comboUserViewer.setInput(userList);
		sectionUsers.setClient(compositeUser);
	}

    public TableComboViewer createTableComboForUsers(Composite composite) {
        comboUserViewer = new SAFRTableComboViewer(composite, SWT.READ_ONLY | SWT.BORDER);
        comboUser = comboUserViewer.getTableCombo();
        comboUser.setShowImageWithinSelection(false);
        comboUser.setVisibleItemCount(10);
        comboUser.setShowTableHeader(true);
        comboUser.setTableWidthPercentage(93);
        comboUser.defineColumns(new String[] { "ID", "Name" },
                new int[] { 80, 180});
        comboUser.setDisplayColumnIndex(2);
        comboUserViewer.setContentProvider(new ArrayContentProvider());    
        comboUserViewer.setLabelProvider(new ITableLabelProvider() {

            @Override
            public void addListener(ILabelProviderListener listener) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            @Override
            public void removeListener(ILabelProviderListener listener) {
            }

            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            @Override
            public String getColumnText(Object element, int columnIndex) {
                UserQueryBean bean = (UserQueryBean) element;
                switch (columnIndex) {
                case 0:
                    return bean.getId();
                case 1:
                    return bean.getNameLabel();
                default:
                    return "";
                }
            }

        });
        for (int iCounter = 0; iCounter < 2; iCounter++) {
            comboUser.getTable().getColumn(iCounter).addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    int dir = comboUser.getTable().getSortDirection();
                    TableColumn sortColumn = comboUser.getTable().getSortColumn();
                    TableColumn currentColumn = (TableColumn) e.widget;
                    if (sortColumn == currentColumn) {
                        dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                    } else {
                        comboUser.getTable().setSortColumn(currentColumn);
                        dir = SWT.UP;
                    }

                    comboUser.getTable().setSortDirection(dir);
                    comboUserViewer.refresh();
                }

            });
        }        
        comboUser.getTable().setSortColumn(comboUser.getTable().getColumn(1));
        comboUser.getTable().setSortDirection(SWT.UP);
        comboUserViewer.setSorter(new ViewerSorter() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                UserQueryBean user1 = (UserQueryBean) e1;
                UserQueryBean user2 = (UserQueryBean) e2;
                
                int mult = 1;
                if (comboUser.getTable().getSortDirection() == SWT.DOWN) {
                    mult = -1;
                }
                if (comboUser.getTable().getSortColumn().getText().equals("ID")) {
                    return (mult) * user1.getId().compareTo(user2.getId());
                } else if (comboUser.getTable().getSortColumn().getText().equals("Name")) {
                    return (mult) * user1.getNameLabel().compareTo(user2.getNameLabel());                    
                } else {
                    return 0;
                }
            }
            
        });
        
        // replace the default windows context menu
        UIUtilities.replaceMenuText(comboUser.getTextControl());
        
        return comboUserViewer;
    
    }
	
    private void addUserOpenEditorMenu()
    {
        Text text = comboUser.getTextControl();
        Menu menu = text.getMenu();
        userOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        userOpenEditorItem.setText("Open Editor");
        userOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
            	UserQueryBean bean = (UserQueryBean)((StructuredSelection) 
            	    comboUserViewer.getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.openUser(bean.getId());                        
                }                
            }
        });
        
        comboUser.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                	UserQueryBean bean = (UserQueryBean)((StructuredSelection) comboUserViewer.getSelection()).getFirstElement();
                    if (bean != null) {   
                        userOpenEditorItem.setEnabled(true);                            
                    }
                    else {
                        userOpenEditorItem.setEnabled(false);
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
	private void createSectionGroups(Composite body1) throws DAOException {
		sectionGroups = safrGuiToolkit.createSection(body1, Section.TITLE_BAR, "Associated Groups:");
		sectionGroups.setLayoutData(new TableWrapData(TableWrapData.LEFT,TableWrapData.FILL_GRAB));

		Composite compositeUsers = safrGuiToolkit.createComposite(sectionGroups,SWT.NONE);
		FormLayout layoutUsers = new FormLayout();
		layoutUsers.marginTop = 5;
		layoutUsers.marginBottom = 5;
		layoutUsers.marginLeft = 5;
		layoutUsers.marginRight = 5;
		compositeUsers.setLayout(layoutUsers);
		tableGroups = new Table(compositeUsers, SWT.CHECK | SWT.FULL_SELECTION | 
		    SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tableGroups.setData(SAFRLogger.USER, "Group Table");                                                        		
		tableViewerGroups = new CheckboxTableViewer(tableGroups);

		FormData dataGroups = new FormData();
		dataGroups.left = new FormAttachment(0, 0);
		dataGroups.top = new FormAttachment(0, 10);
		dataGroups.bottom = new FormAttachment(100, 0);
		dataGroups.height = 250;
		tableGroups.setLayoutData(dataGroups);
		toolkit.adapt(tableGroups, false, false);
		tableGroups.setHeaderVisible(true);
		tableGroups.setLinesVisible(true);

		tableGroups.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              
              if (event.detail == SWT.CHECK) {                  
                  int selRow = tableGroups.indexOf((TableItem)event.item);  
                  int stateMask=event.stateMask;                  
                  if((stateMask & SWT.SHIFT)==SWT.SHIFT){
                      int prevRow = prevSelection;
                      
                      if((stateMask & SWT.CTRL)!=SWT.CTRL){
                          tableViewerGroups.setAllChecked(false);
                     }
                      if (prevRow > selRow) {
                          for (int i=selRow ; i<=prevRow ; i++) {
                              Object element = tableViewerGroups.getElementAt(i);
                              tableViewerGroups.setChecked(element, true);
                          }
                      }
                      else {
                          for (int i=prevRow ; i<=selRow ; i++) {
                              Object element = tableViewerGroups.getElementAt(i);
                              tableViewerGroups.setChecked(element, true);
                          }                            
                      }
                  }   
                  else {
                      Object element = tableViewerGroups.getElementAt(selRow);
                      if (tableViewerGroups.getChecked(element)) {
                          prevSelection = tableGroups.indexOf((TableItem)event.item);
                      }
                      else {
                          prevSelection = 0;
                      }
                  }
              }                  
            }
          });
		
		String[] columnHeaders = { "Group ID", "Name" };
		int[] columnWidths = { 100, 250 };
		int length = columnWidths.length;
		for (int iCounter = 0; iCounter < length; iCounter++) {
			TableViewerColumn column = new TableViewerColumn(tableViewerGroups, SWT.NONE);
			column.getColumn().setText(columnHeaders[iCounter]);
			column.getColumn().setToolTipText(columnHeaders[iCounter]);
			column.getColumn().setWidth(columnWidths[iCounter]);
			column.getColumn().setResizable(true);

			ColumnSelectionListenerTable colListener = new ColumnSelectionListenerTable(tableViewerGroups, iCounter);
			column.getColumn().addSelectionListener(colListener);
		}

		tableViewerGroups.setContentProvider(new IStructuredContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput,Object newInput) {
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				try {
				    ApplicationMediator.getAppMediator().waitCursor();
					if (currentUser != null && currentUser.getAssociatedGroups() != null) {
						return currentUser.getAssociatedGroups().getActiveItems().toArray();
					}
					return new String[0];
				} finally {
                    ApplicationMediator.getAppMediator().normalCursor();
				}
			}
		});

		tableViewerGroups.setLabelProvider(new UserGroupLabelProvider());
		tableViewerGroups.setInput(1);
		tableViewerGroups.getTable().setSortColumn(tableViewerGroups.getTable().getColumn(0));
		tableViewerGroups.getTable().setSortDirection(SWT.UP);
		tableViewerGroups.setSorter(new UserGroupRightTableSorter(0, SWT.UP));
		tableViewerGroups.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				if (tableViewerGroups.getCheckedElements().length > 0) {
					buttonRemove.setEnabled(true);
				} else {
					buttonRemove.setEnabled(false);
				}
			}
		});

		tableViewerGroups.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	UserGroupAssociation node = (UserGroupAssociation)((StructuredSelection) event.getSelection()).getFirstElement();
                if (node != null) {
                    setPopupEnabled(true);
                }
                else {
                    setPopupEnabled(false);                        
                }
            }
        });
		
		compositeGroupsButtonBar = safrGuiToolkit.createComposite(compositeUsers, SWT.NONE);
		compositeGroupsButtonBar.setLayout(new FormLayout());
		FormData dataButtonBar = new FormData();
		dataButtonBar.left = new FormAttachment(tableGroups, 10);
		dataButtonBar.top = new FormAttachment(0, 10);
		dataButtonBar.bottom = new FormAttachment(100, 0);
		compositeGroupsButtonBar.setLayoutData(dataButtonBar);

		buttonAdd = toolkit.createButton(compositeGroupsButtonBar, "A&dd",SWT.PUSH);
		buttonAdd.setData(SAFRLogger.USER, "Add");                                                                		
		FormData dataButtonAdd = new FormData();
		dataButtonAdd.left = new FormAttachment(0, 5);
		dataButtonAdd.width = 90;
		buttonAdd.setLayoutData(dataButtonAdd);
		buttonAdd.setEnabled(false);
		buttonAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentUser != null) {
					// Display an hour glass till list of Users is populated.
                    ApplicationMediator.getAppMediator().waitCursor();

					MetadataListDialog dialog = new MetadataListDialog(
							getSite().getShell(), TreeItemId.USERGROUP,
							currentUser, UIUtilities.getCurrentEnvironmentID());
					dialog.open();

					List<SAFRQueryBean> returnList;
					if (dialog.getReturnCode() == Window.OK) {
						returnList = dialog.getReturnList();
						List<GroupQueryBean> tmpList = new ArrayList<GroupQueryBean>();
						UserGroupAssociation groupUser = null;
						SAFRList<UserGroupAssociation> currAssoc = currentUser.getAssociatedGroups();

						for (int i = 0; i < returnList.size(); i++) {
						    GroupQueryBean bean = (GroupQueryBean) returnList.get(i);
							groupUser = new UserGroupAssociation(currentUser, bean.getId(), bean.getName());
							currAssoc.add(groupUser);
							tmpList.add(bean);
							setDirty(true);

						}
						tableViewerGroups.refresh();
					}
					// return cursor to normal
                    ApplicationMediator.getAppMediator().normalCursor();
				}
			}
		});

		// Code for tracking the focus on the table
		IFocusService service = (IFocusService) getSite().getService(
				IFocusService.class);
		service.addFocusTracker(tableGroups, "UserSearchableTable");

		buttonRemove = toolkit.createButton(compositeGroupsButtonBar, "Re&move",
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
				if (currentUser != null) {
					Object[] items = tableViewerGroups.getCheckedElements();
					if (items.length > 0) {
						List<UserGroupAssociation> tmpList = new ArrayList<UserGroupAssociation>();
						SAFRList<UserGroupAssociation> currList = currentUser.getAssociatedGroups();
						for (Object item : items) {
							UserGroupAssociation groupUserAssociation = (UserGroupAssociation) item;
							currList.remove(groupUserAssociation);
							tmpList.add(groupUserAssociation);
						}
						setDirty(true);
						tableViewerGroups.refresh();
					}
				}
				buttonRemove.setEnabled(false);
			}
		});
		sectionGroups.setClient(compositeUsers);
		
        // Code for Context menu
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableGroups);
        tableGroups.setMenu(menu);
        getSite().registerContextMenu(menuManager, tableViewerGroups);        
        setPopupEnabled(false);     
		
	}

    private void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService.
            getSourceProvider(OpenEditorPopupState.USRMEM);
        service.setUsrMem(enabled);
    }
	
    public UserGroupAssociation getCurrentSelection() {
        return (UserGroupAssociation)((StructuredSelection)tableViewerGroups.getSelection()).getFirstElement();
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
			    ApplicationMediator.getAppMediator().waitCursor();
			    
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
				tabViewer.setSorter(new UserGroupRightTableSorter(colNumber,dir));
			} finally {
                ApplicationMediator.getAppMediator().normalCursor();
			}
		}
	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public String getModelName() {
		return "User - Group Membership";
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
		comboUser.setFocus();
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
		currentUser.storeGroupAssociations();
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
		if (this.tableGroups.isFocusControl()) {
			tabViewer = this.tableViewerGroups;
		}
		if (tabViewer != null) {
			// if search criteria is id, then sort the list of components
			// according to id.
			if (searchCriteria == MetadataSearchCriteria.ID) {
				tabViewer.getTable().setSortColumn(
						tabViewer.getTable().getColumn(0));
				tabViewer.getTable().setSortDirection(SWT.UP);
				tabViewer.setSorter(new UserGroupRightTableSorter(0, SWT.UP));
			} else {
				// if search criteria is name, then sort the list of components
				// according to name.
				tabViewer.getTable().setSortColumn(
						tabViewer.getTable().getColumn(1));
				tabViewer.getTable().setSortDirection(SWT.UP);
				tabViewer.setSorter(new UserGroupRightTableSorter(1, SWT.UP));
			}

			// get the items of the table and apply search.
			for (TableItem item : tabViewer.getTable().getItems()) {
				UserGroupAssociation bean = (UserGroupAssociation) item.getData();
				if (searchCriteria == MetadataSearchCriteria.ID) {
					if (bean.getAssociatedComponentIdString().toLowerCase().startsWith(
					    searchText.toLowerCase())) {
						tabViewer.setSelection(new StructuredSelection(bean),true);
						return;
					}
				} else if (searchCriteria == MetadataSearchCriteria.NAME) {
					if (bean.getAssociatedComponentName() != null && 
					    bean.getAssociatedComponentName().toLowerCase().startsWith(
					        searchText.toLowerCase())) {
						tabViewer.setSelection(new StructuredSelection(bean),true);
						return;
					}
				}
			}

			// if no component is found, then show the dialog box.
			MessageDialog.openInformation(getSite().getShell(),"Component not found",
			    "The component you are trying to search is not found in the list.");

		}
	}

	public ComponentType getComponentType() {
		if (tableGroups.isFocusControl()) {
			return ComponentType.Group;
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
