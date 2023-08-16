package com.ibm.safr.we.ui.editors;

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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.SAFRValidationToken;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQueryBean;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.dialogs.DependencyMessageDialog;
import com.ibm.safr.we.ui.dialogs.MetadataListDialog;
import com.ibm.safr.we.ui.utilities.ISearchablePart;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationContentProvider;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationLabelProvider;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationTableSorter;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.utilities.SAFRLogger;

public class LogicalFileEditor extends SAFREditorPart implements
		ISearchablePart {

    public enum MouseHover {
      PFTABLE,
      LRTABLE,
      OTHER
    };
    
	public static String ID = "SAFRWE.LogicalFileEditor";

	private Text textID;
	private Text textName;
	private Text textComments;
	private Label labelCreatedValue;
	private Label labelModifiedValue;

	private Button buttonAdd;
	private Button buttonRemove;

	private String defaultCreated = "-";
	private String defaultModified = "-";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;

	private Section sectionGeneral;
	private Section sectionPhysicalfiles;
	private Section sectionLogicalRecords;

	private TableViewer tableViewerLogicalRecord;
	private CheckboxTableViewer tableViewerPhysicalFile;

	private Table tableLogicalRecord;
	private Table tablePhysicalFile;

	LogicalFile logicalFile;
	LogicalFileEditorInput logicalFileInput;

    private int prevSelection = 0;	

    @Override
	public boolean isSaveAsAllowed() {
		boolean retval = false;
		
		//if not dealing with a new component 
		//check with parent based on permissions
		if(logicalFile.getId() > 0) {
			retval = isSaveAsAllowed(Permissions.CreateLogicalFile);
		}
		return retval;
	}

	@Override
	public void createPartControl(Composite parent) {
		logicalFileInput = (LogicalFileEditorInput) getEditorInput();
		logicalFile = logicalFileInput.getLogicalFile();

		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		safrGuiToolkit
				.setReadOnly(logicalFileInput.getEditRights() == EditRights.Read);
		form = toolkit.createScrolledForm(parent);

		form.getBody().setLayout(UIUtilities.createTableLayout(2, false));
		createSectionGeneral(form.getBody());
		createAssociatedLogicalRecord(form.getBody());
		createAssociatedPhysicalFile(form.getBody());

		refreshControls();
		setDirty(false);
		ManagedForm mFrm = new ManagedForm(toolkit, form);
		setMsgManager(mFrm.getMessageManager());		
        setPopupEnabled(false);        		
	}

	private void createAssociatedPhysicalFile(Composite body) {
		sectionPhysicalfiles = safrGuiToolkit.createSection(body,
				Section.TITLE_BAR, "Associated Physical Files");
		sectionPhysicalfiles.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));

		Composite compositePhysicalFile = safrGuiToolkit.createComposite(
				sectionPhysicalfiles, SWT.NONE);
		FormLayout layoutPhysicalFile = new FormLayout();
		layoutPhysicalFile.marginTop = 5;
		layoutPhysicalFile.marginBottom = 5;
		layoutPhysicalFile.marginLeft = 5;
		layoutPhysicalFile.marginRight = 5;
		compositePhysicalFile.setLayout(layoutPhysicalFile);

		tablePhysicalFile = new Table(compositePhysicalFile, SWT.CHECK
				| SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tablePhysicalFile.setData(SAFRLogger.USER, "Associated Physical Files");		
		tableViewerPhysicalFile = new CheckboxTableViewer(tablePhysicalFile);

		FormData dataTablePhysicalFile = new FormData();
		dataTablePhysicalFile.left = new FormAttachment(0, 0);
		dataTablePhysicalFile.top = new FormAttachment(0, 10);
		dataTablePhysicalFile.bottom = new FormAttachment(100, 0);
		dataTablePhysicalFile.height = 100;
		tablePhysicalFile.setLayoutData(dataTablePhysicalFile);
		toolkit.adapt(tablePhysicalFile, false, false);

		tablePhysicalFile.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              
              if (event.detail == SWT.CHECK) {                  
                  int selRow = tablePhysicalFile.indexOf((TableItem)event.item);  
                  int stateMask=event.stateMask;                  
                  if((stateMask & SWT.SHIFT)==SWT.SHIFT){
                      int prevRow = prevSelection;
                      
                      if((stateMask & SWT.CTRL)!=SWT.CTRL){
                          tableViewerPhysicalFile.setAllChecked(false);
                      }
                      if (prevRow > selRow) {
                          for (int i=selRow ; i<=prevRow ; i++) {
                              Object element = tableViewerPhysicalFile.getElementAt(i);
                              tableViewerPhysicalFile.setChecked(element, true);
                          }
                      }
                      else {
                          for (int i=prevRow ; i<=selRow ; i++) {
                              Object element = tableViewerPhysicalFile.getElementAt(i);
                              tableViewerPhysicalFile.setChecked(element, true);
                          }                            
                      }
                  }   
                  else {
                      Object element = tableViewerPhysicalFile.getElementAt(selRow);
                      if (tableViewerPhysicalFile.getChecked(element)) {
                          prevSelection = tablePhysicalFile.indexOf((TableItem)event.item);
                      }
                      else {
                          prevSelection = 0;
                      }
                  }
              }                  
            }
          });

        
		UIUtilities.prepareTableViewerForShortList(tableViewerPhysicalFile, ComponentType.PhysicalFile);

		tableViewerPhysicalFile
				.setContentProvider(new ComponentAssociationContentProvider());
		tableViewerPhysicalFile
				.setLabelProvider(new ComponentAssociationLabelProvider());
		tableViewerPhysicalFile.setInput(logicalFile
				.getPhysicalFileAssociations());
		
        SortOrderPrefs prefs = new SortOrderPrefs(UIUtilities.SORT_CATEGORY, ComponentType.PhysicalFile.name());
        if (prefs.load()) {
            tableViewerPhysicalFile.getTable().setSortColumn(
                tableViewerPhysicalFile.getTable().getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                tableViewerPhysicalFile.getTable().setSortDirection(SWT.UP);
                tableViewerPhysicalFile.setSorter(new ComponentAssociationTableSorter(prefs.getColumn(), SWT.UP));
            }
            else {
                tableViewerPhysicalFile.getTable().setSortDirection(SWT.DOWN);
                tableViewerPhysicalFile.setSorter(new ComponentAssociationTableSorter(prefs.getColumn(), SWT.DOWN));
            }                   
        }
        else {
            tableViewerPhysicalFile.getTable().setSortColumn(
                tableViewerPhysicalFile.getTable().getColumn(1));
            tableViewerPhysicalFile.getTable().setSortDirection(SWT.UP);
            tableViewerPhysicalFile.setSorter(new ComponentAssociationTableSorter(1, SWT.UP));
        }       
		

		// CQ 8469 Kanchan Rauthan. To disable remove button if nothing is
		// selected to remove.
		tableViewerPhysicalFile
				.addCheckStateListener(new ICheckStateListener() {

					public void checkStateChanged(CheckStateChangedEvent event) {
						if (tableViewerPhysicalFile.getCheckedElements().length > 0 && 
						    logicalFileInput.getEditRights() != EditRights.Read) {
							buttonRemove.setEnabled(true);
						} else {
							buttonRemove.setEnabled(false);
						}
					}
				});

		Composite buttonBar = safrGuiToolkit.createComposite(
				compositePhysicalFile, SWT.NONE);
		buttonBar.setLayout(new FormLayout());

		FormData dataButtonBar = new FormData();
		dataButtonBar.left = new FormAttachment(tablePhysicalFile, 10);
		dataButtonBar.right = new FormAttachment(100, 0);
		dataButtonBar.top = new FormAttachment(0, 10);
		dataButtonBar.bottom = new FormAttachment(100, 0);
		buttonBar.setLayoutData(dataButtonBar);

		buttonAdd = safrGuiToolkit.createButton(buttonBar, SWT.PUSH, "A&dd");
		buttonAdd.setData(SAFRLogger.USER, "Add Physical File");		
		
		FormData dataButtonAdd = new FormData();
		dataButtonAdd.left = new FormAttachment(0, 5);
		dataButtonAdd.width = 50;
		buttonAdd.setLayoutData(dataButtonAdd);
		buttonAdd.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				// Display an hour glass till list of PFs is populated.
				Display.getCurrent().getActiveShell().setCursor(
						Display.getCurrent().getActiveShell().getDisplay()
								.getSystemCursor(SWT.CURSOR_WAIT));

				MetadataListDialog dialog = new MetadataListDialog(getSite()
						.getShell(), TreeItemId.PHYSICALFILE, logicalFile,
						UIUtilities.getCurrentEnvironmentID());
				dialog.create();
				dialog.open();
				List<SAFRQueryBean> returnList;
				if (dialog.getReturnCode() == Window.OK) {
					returnList = dialog.getReturnList();
					for (int i = 0; i < returnList.size(); i++) {
						PhysicalFileQueryBean bean = (PhysicalFileQueryBean) returnList.get(i);
						try {
							FileAssociation association = new FileAssociation(
							    logicalFile, bean.getId(), bean.getName(),bean.getRights());
							logicalFile.addAssociatedPhysicalFile(association);
							setDirty(true);
						} catch (SAFRException se) {
							UIUtilities.handleWEExceptions(se);
						}
					}
					tableViewerPhysicalFile.refresh();
				}
				// return cursor to normal
				Display.getCurrent().getActiveShell().setCursor(null);

			}

		});
		// Code for tracking the focus on the table
		IFocusService service = (IFocusService) getSite().getService(
				IFocusService.class);
		service.addFocusTracker(tablePhysicalFile, "PFSearchableTable");

		buttonRemove = safrGuiToolkit.createButton(buttonBar, SWT.PUSH,
				"Re&move");
		buttonRemove.setData(SAFRLogger.USER, "Remove Physical File");		
		FormData dataButtonRemove = new FormData();
		dataButtonRemove.left = new FormAttachment(0, 5);
		dataButtonRemove.top = new FormAttachment(buttonAdd, 5);
		dataButtonRemove.width = 50;
		buttonRemove.setLayoutData(dataButtonRemove);
		buttonRemove.setEnabled(false);
		// gives a warning dialog if user tries to remove a Physical file
		// associated to View. Event added to the remove button.
		buttonRemove.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				int result = 0;
				int tableViewerSize = 0;
				FileAssociation association = null;
				SAFRValidationToken token = null;
				// Display an hour glass till associations are removed.
				Display.getCurrent().getActiveShell().setCursor(
						Display.getCurrent().getActiveShell().getDisplay()
								.getSystemCursor(SWT.CURSOR_WAIT));

				for (Object obj : tableViewerPhysicalFile.getCheckedElements()) {
					tableViewerSize = tableViewerPhysicalFile.getTable()
							.getItemCount();
					association = (FileAssociation) obj;
					try {
						logicalFile.removeAssociatedPhysicalFile(association,
								null);
						buttonRemove.setEnabled(false);
					} catch (SAFRValidationException e1) {
						// DEPENDENCY_PF_ASSOCIATION_WARNING
						result = DependencyMessageDialog
								.openDependencyDialog(
										getSite().getShell(),
										"Logical File/Physical File Dependencies",
										"If this Physical File association is removed, the Views which reference it will become inactive.",
										e1.getMessageString(),
										MessageDialog.WARNING, new String[] {
												"&OK", "&Cancel" }, 0);
						token = e1.getSafrValidationToken();
						if (result == 0) {
							// User selected OK, remove the associated physical
							// file.
							try {
								logicalFile.removeAssociatedPhysicalFile(
										association, token);
								buttonRemove.setEnabled(false);
							} catch (SAFRValidationException sve) {
								UIUtilities.handleWEExceptions(sve);
							} catch (DAOException sve) {
								UIUtilities.handleWEExceptions(sve);
							}
						}
					}

					catch (DAOException e1) {
						UIUtilities.handleWEExceptions(e1, null,
								UIUtilities.titleStringDbException);
					}
				}
				tableViewerPhysicalFile.refresh();

				// return cursor to normal
				Display.getCurrent().getActiveShell().setCursor(null);

				int tableViewerSize1 = tableViewerPhysicalFile.getTable()
						.getItemCount();
				if (tableViewerSize != tableViewerSize1) {
					setDirty(true);
				}
			}
		});

		tableViewerPhysicalFile.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                    setPopupEnabled(true);
                ComponentAssociation ass = (ComponentAssociation) ((StructuredSelection)tableViewerPhysicalFile.
                        getSelection()).getFirstElement();
                if (ass != null) {
                    setPopupEnabled(true);
                }
                else {
                    setPopupEnabled(false);                    
                }
            }		    
		});
		
		tablePhysicalFile.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent arg0) {
                ComponentAssociation ass = (ComponentAssociation) ((StructuredSelection)tableViewerPhysicalFile.
                        getSelection()).getFirstElement();                
                if (ass == null)
                {
                    setPopupEnabled(false);                    
                }
                else {
                    setPopupEnabled(true);
                }
            }

            public void focusLost(FocusEvent arg0) {
                setPopupEnabled(false);                                    
            }
		    
		});
		
		
		// align buttons width using preferred width of largest button.
		Point computeSize = buttonRemove.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		dataButtonRemove.width = computeSize.x;
		dataButtonAdd.width = computeSize.x;
		sectionPhysicalfiles.setClient(compositePhysicalFile);
		
        // Code for Context menu
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tablePhysicalFile);
        tablePhysicalFile.setMenu(menu);
        getSite().registerContextMenu(menuManager, tableViewerPhysicalFile);  		
	}

	private void createAssociatedLogicalRecord(Composite body) {
		sectionLogicalRecords = safrGuiToolkit.createSection(body,
				Section.TITLE_BAR, "Associated Logical Records");
		sectionLogicalRecords.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));

		Composite compositeLogicalRecord = safrGuiToolkit.createComposite(
				sectionLogicalRecords, SWT.NONE);
		FormLayout layoutLogicalFile = new FormLayout();
		layoutLogicalFile.marginTop = 5;
		layoutLogicalFile.marginBottom = 5;
		layoutLogicalFile.marginLeft = 5;
		layoutLogicalFile.marginRight = 5;

		compositeLogicalRecord.setLayout(layoutLogicalFile);

		tableViewerLogicalRecord = safrGuiToolkit.createTableViewer(
				compositeLogicalRecord, SWT.MULTI | SWT.BORDER
						| SWT.FULL_SELECTION, false);
		tableLogicalRecord = tableViewerLogicalRecord.getTable();
		FormData dataTableLogicalRecord = new FormData();
		dataTableLogicalRecord.left = new FormAttachment(0, 0);
		dataTableLogicalRecord.top = new FormAttachment(0, 10);
		dataTableLogicalRecord.bottom = new FormAttachment(100, 0);
		dataTableLogicalRecord.height = 100;
		tableLogicalRecord.setLayoutData(dataTableLogicalRecord);
		toolkit.adapt(tableLogicalRecord, false, false);

		UIUtilities.prepareTableViewerForShortList(tableViewerLogicalRecord,ComponentType.LogicalRecord);
		tableViewerLogicalRecord
				.setContentProvider(new ComponentAssociationContentProvider());
		tableViewerLogicalRecord
				.setLabelProvider(new ComponentAssociationLabelProvider());
		tableViewerLogicalRecord.setInput(logicalFile
				.getLogicalRecordAssociations());
		
        SortOrderPrefs prefs = new SortOrderPrefs(UIUtilities.SORT_CATEGORY, ComponentType.LogicalRecord.name());
        if (prefs.load()) {
            tableViewerLogicalRecord.getTable().setSortColumn(
                tableViewerLogicalRecord.getTable().getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                tableViewerLogicalRecord.getTable().setSortDirection(SWT.UP);
                tableViewerLogicalRecord.setSorter(new ComponentAssociationTableSorter(prefs.getColumn(), SWT.UP));
            }
            else {
                tableViewerLogicalRecord.getTable().setSortDirection(SWT.DOWN);
                tableViewerLogicalRecord.setSorter(new ComponentAssociationTableSorter(prefs.getColumn(), SWT.DOWN));
            }                   
        }
        else {
            tableViewerLogicalRecord.getTable().setSortColumn(
                tableViewerLogicalRecord.getTable().getColumn(1));
            tableViewerLogicalRecord.getTable().setSortDirection(SWT.UP);
            tableViewerLogicalRecord.setSorter(new ComponentAssociationTableSorter(1, SWT.UP));
        }       
		
		sectionLogicalRecords.setClient(compositeLogicalRecord);
		sectionLogicalRecords.setVisible(false);

		// Code for tracking the focus on the table
		IFocusService service = (IFocusService) getSite().getService(
				IFocusService.class);
		service.addFocusTracker(tableLogicalRecord, "LRSearchableTable");
		
		tableLogicalRecord.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                setPopupEnabled(true);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
		    
		});
		
        tableLogicalRecord.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent arg0) {
                ComponentAssociation ass = (ComponentAssociation) ((StructuredSelection)tableViewerLogicalRecord.
                        getSelection()).getFirstElement();
                if (ass == null) {
                    setPopupEnabled(false);                    
                }
                else {
                    setPopupEnabled(true);
                }
            }

            public void focusLost(FocusEvent arg0) {
                setPopupEnabled(false);                                    
            }
            
        });
        
		
        // Code for Context menu
        // First we create a menu Manager
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableLogicalRecord);
        // Set the MenuManager
        tableLogicalRecord.setMenu(menu);
        getSite().registerContextMenu(menuManager, tableViewerLogicalRecord);        
        setPopupEnabled(false);		
	}

	private void createSectionGeneral(Composite compositeValues) {
		sectionGeneral = safrGuiToolkit.createSection(compositeValues,
				Section.TITLE_BAR, "General Information");
		sectionGeneral
				.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Composite compositeGeneral = safrGuiToolkit.createComposite(
				sectionGeneral, SWT.NONE);
		compositeGeneral.setLayout(UIUtilities.createTableLayout(3, true));

		safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE,
				"ID:");

		textID = safrGuiToolkit.createTextBox(compositeGeneral, SWT.NONE);
		textID.setEnabled(false);
		textID.setLayoutData(UIUtilities.textTableData(1));

		safrGuiToolkit.createLabel(compositeGeneral, SWT.NONE,
				"");

		safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, "&Name:");

		textName = safrGuiToolkit.createNameTextBox(compositeGeneral, SWT.NONE);
		textName.setData(SAFRLogger.USER, "Name");		
		textName.setLayoutData(UIUtilities.textTableData(2));
		textName.setTextLimit(UIUtilities.MAXNAMECHAR);
		textName.addModifyListener(this);
		
		safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, "C&omments:");

		textComments = safrGuiToolkit.createCommentsTextBox(compositeGeneral);
		textComments.setData(SAFRLogger.USER, "Comments");		
		textComments.setLayoutData(UIUtilities.multiLineTextData(1, 2,
				textComments.getLineHeight() * 3));
		textComments.setTextLimit(UIUtilities.MAXCOMMENTCHAR);
		textComments.addModifyListener(this);

		safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, "Created:");

		labelCreatedValue = safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, defaultCreated);
		labelCreatedValue.setLayoutData(UIUtilities.textTableData(2));

		safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, "Last Modified:");

		labelModifiedValue = safrGuiToolkit.createLabel(compositeGeneral,
				SWT.NONE, defaultModified);
		labelModifiedValue.setLayoutData(UIUtilities.textTableData(2));

		sectionGeneral.setClient(compositeGeneral);
	}

    private void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.LOGICALFILE);
        service.setLogicalFile(enabled);
    }

	@Override
	public void setFocus() {
		super.setFocus();
		textName.setFocus();
	}

	@Override
	public void doRefreshControls() {
		UIUtilities
				.checkNullText(textID, Integer.toString(logicalFile.getId()));
		if (logicalFile.getId() > 0) {
			UIUtilities.checkNullText(textName, logicalFile.getName());
			UIUtilities.checkNullText(textComments, logicalFile.getComment());
			labelCreatedValue.setText(logicalFile.getCreateBy() + " on "
					+ logicalFile.getCreateTimeString());
			labelModifiedValue.setText(logicalFile.getModifyBy() + " on "
					+ logicalFile.getModifyTimeString());
			tableViewerPhysicalFile.refresh();
			sectionLogicalRecords.setVisible(true);
		} 
        if (logicalFile.getName() == null) {
            form.setText("");           
        } else {
            form.setText(logicalFile.getName());         
        }       
	}

	@Override
	public void refreshModel() {
		logicalFile.setName(textName.getText());
		logicalFile.setComment(textComments.getText());
	}

	@Override
	public String getModelName() {
		return "Logical file";
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
		logicalFile.store();
	}

	@Override
	public void validate() throws DAOException, SAFRException {
		try {
			logicalFile.validate();
			getMsgManager().removeAllMessages();
		} catch (SAFRValidationException sve) {
			if (sve.getSafrValidationToken().getValidationFailureType() == SAFRValidationType.DEPENDENCY_PF_ASSOCIATION_ERROR) {
				// dependency found. generate a error message.
				DependencyMessageDialog.openDependencyDialog(getSite().getShell(),"Error saving Logical File",
				    "A new View dependency has been created on a deleted physical file association since the last warning issued, during deletion. These deleted associations have now been restored. Please review and save again.",
					sve.getMessageString(), MessageDialog.ERROR,new String[] { IDialogConstants.OK_LABEL }, 0);
				throw new SAFRValidationException();
			}
			else if (sve.getSafrValidationToken().getValidationFailureType() == SAFRValidationType.DEPENDENCY_LOOKUP_ERROR) {
	                // dependency found. generate a error message.
	                DependencyMessageDialog.openDependencyDialog(getSite().getShell(), "Error saving Logical File",
	                    "Cannot have more than one physical file associated with this logical file, it has lookup path target dependencies, and the lookup doesn't use a lookup exit. Please review and save again",
	                    sve.getMessageString(), MessageDialog.ERROR,new String[] { IDialogConstants.OK_LABEL }, 0);
	                throw new SAFRValidationException();
			} else {
				throw sve;
			}
		}
	}

	/**
	 * This method is used to get the widget based on the property passed.
	 * 
	 * @param property
	 * @return the widget.
	 */
	protected Control getControlFromProperty(Object property) {
		if (property == LogicalFile.Property.NAME) {
			return textName;
		} else if (property == LogicalFile.Property.COMMENT) {
			return textComments;
		} else if (property == LogicalFile.Property.PF_ASSOCIATION) {
			return tableViewerPhysicalFile.getControl();
		}
		return null;
	}

	@Override
	public ComponentType getEditorCompType() {
		return ComponentType.LogicalFile;
	}

	@Override
	public SAFRPersistentObject getModel() {
		return logicalFile;
	}

	public void searchComponent(MetadataSearchCriteria searchCriteria,
			String searchText) {
		TableViewer tabViewer = null;
		// if focus is on associated PF table, then apply search on that table
		// or if the focus is on associated LR table, then apply search on that
		// table.
		if (this.tablePhysicalFile.isFocusControl()) {
			tabViewer = this.tableViewerPhysicalFile;
		} else if (this.tableLogicalRecord.isFocusControl()) {
			tabViewer = this.tableViewerLogicalRecord;
		}
		if (tabViewer != null) {
			// if search criteria is id, then sort the list of components
			// according to id.
			if (searchCriteria == MetadataSearchCriteria.ID) {
				tabViewer.getTable().setSortColumn(
						tabViewer.getTable().getColumn(0));
				tabViewer.getTable().setSortDirection(SWT.UP);
				tabViewer.setSorter(new ComponentAssociationTableSorter(0,
						SWT.UP));
			} else {
				// if search criteria is name, then sort the list of components
				// according to name.
				tabViewer.getTable().setSortColumn(
						tabViewer.getTable().getColumn(1));
				tabViewer.getTable().setSortDirection(SWT.UP);
				tabViewer.setSorter(new ComponentAssociationTableSorter(1,
						SWT.UP));
			}

			// get the items from the table
			for (TableItem item : tabViewer.getTable().getItems()) {
				ComponentAssociation bean = (ComponentAssociation) item
						.getData();
				if (searchCriteria == MetadataSearchCriteria.ID) {
					if (bean.getAssociatedComponentIdString().startsWith(
							searchText)) {
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
		if (tableLogicalRecord.isFocusControl()) {
			return ComponentType.LogicalRecord;
		} else if (tablePhysicalFile.isFocusControl()) {
			return ComponentType.PhysicalFile;
		}
		return null;
	}

	@Override
	public String getComponentNameForSaveAs() {
		return textName.getText();
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		if (sve.getErrorMessageMap().containsKey(
				com.ibm.safr.we.model.LogicalFile.Property.NAME)) {
			return true;
		}
		return false;
	}

    public ComponentAssociation getCurrentPF() {
        return (ComponentAssociation)((StructuredSelection)tableViewerPhysicalFile.getSelection()).getFirstElement();
    }

    public ComponentAssociation getCurrentLR() {
        return (ComponentAssociation)((StructuredSelection)tableViewerLogicalRecord.getSelection()).getFirstElement();
    }
    
    public MouseHover getMouseHover() {
        Control control = Display.getCurrent().getFocusControl();
        if (control == tablePhysicalFile) {
            return MouseHover.PFTABLE;
        }
        else if (control == tableLogicalRecord) {
            return MouseHover.LRTABLE;            
        }
        else {
            return MouseHover.OTHER;
        }
    }
    
}
