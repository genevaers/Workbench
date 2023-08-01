package com.ibm.safr.we.ui.views.metadatatable;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.DBType;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRFactory;
import com.ibm.safr.we.model.SAFRValidationToken;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.query.ControlRecordQueryBean;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.GroupQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.NumericIdQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQueryBean;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.query.UserQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBeanConv;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.commands.SourceProvider;
import com.ibm.safr.we.ui.dialogs.DependencyMessageDialog;
import com.ibm.safr.we.ui.dialogs.MessageDisableDialog;
import com.ibm.safr.we.ui.dialogs.MetadataConfirmDialog;
import com.ibm.safr.we.ui.dialogs.MultiErrorMessageDialog;
import com.ibm.safr.we.ui.editors.ControlRecordEditor;
import com.ibm.safr.we.ui.editors.ControlRecordEditorInput;
import com.ibm.safr.we.ui.editors.EnvironmentEditor;
import com.ibm.safr.we.ui.editors.EnvironmentEditorInput;
import com.ibm.safr.we.ui.editors.GroupEditor;
import com.ibm.safr.we.ui.editors.GroupEditorInput;
import com.ibm.safr.we.ui.editors.LogicalFileEditor;
import com.ibm.safr.we.ui.editors.LogicalFileEditorInput;
import com.ibm.safr.we.ui.editors.LookupPathEditor;
import com.ibm.safr.we.ui.editors.LookupPathEditorInput;
import com.ibm.safr.we.ui.editors.SAFREditorPart;
import com.ibm.safr.we.ui.editors.UserEditor;
import com.ibm.safr.we.ui.editors.UserEditorInput;
import com.ibm.safr.we.ui.editors.UserExitRoutineEditor;
import com.ibm.safr.we.ui.editors.UserExitRoutineEditorInput;
import com.ibm.safr.we.ui.editors.ViewFolderEditor;
import com.ibm.safr.we.ui.editors.ViewFolderEditorInput;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditor;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditorInput;
import com.ibm.safr.we.ui.editors.pf.PhysicalFileEditor;
import com.ibm.safr.we.ui.editors.pf.PhysicalFileEditorInput;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;
import com.ibm.safr.we.ui.utilities.DepCheckOpener;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.ISearchablePart;
import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy;
import com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy.SAFRGUIContext;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.ui.views.navigatortree.NavigatorView;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * This class creates a view to display the list of the chosen metadata
 * component in a tabular format.
 * 
 */
public class MetadataView extends ViewPart implements ISelectionListener,
		ISearchablePart, IPartListener2 {
    
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.views.metadatatable.MetadataView");
    
	public static final String ID = "SAFRWE.Tableview";
	public static final String SKIP = "Skip";
    public static final String CAN_REM = "Cancel Remaining";

    private static final String SORT_CATEGORY = "MetaList";
    
	private Label label;
	private TableViewer tabViewer;
	private Table table;
	private int iCounter;
	MainTreeItem selectedItem;

	MainTreeItem previousItem = new MainTreeItem(TreeItemId.ABOUT, null, null,null);
	SAFRGUIToolkit safrToolkit;

	MainTableLabelProvider metadataLabelProvider;

	IStructuredSelection selectedMetadataComponent;
	ComponentType componentType;

	String MSGCHECKOPENEDITOR = "Component you are trying to delete is opened in editor and is modified." + SAFRUtilities.LINEBREAK + " If you click ok, editor will be closed and the component will be deleted.";
	String MSGCHECK_CLOSE_ALLEDITORS = "Clearing this environment will close all the currently opened editors and you will lose the modified data.";
	private boolean viewPropertiesReportAllowed;
	private boolean logicalRecordReportAllowed;
	private boolean lookupPathsReportAllowed;
	private boolean reportsMenuAllowed;
	private boolean environmentReportAllowed;
    private boolean clearEnvAllowed;
	private boolean depCheckMetadataAllowed;
	private MainTableFilter mainTableFilter;

	@Override
	public void createPartControl(Composite parent) {
		safrToolkit = new SAFRGUIToolkit();

		Composite composite = new Composite(parent, SWT.NONE);

		composite.setLayout(new FormLayout());
		composite.setLayoutData(new FormData());

		// CQ 8371. Nikita. 12/08/2010.
		// Hide the metadata table and show a message about its unavailability
		// if the item selected in the navigator view is "Administration"
		label = new Label(composite, SWT.NONE);
		label.setText("Metadata List unavailable");
		label.setVisible(false);
		FormData labelData = new FormData();
		labelData.top = new FormAttachment(0, 0);
		labelData.bottom = new FormAttachment(100, 0);
		labelData.left = new FormAttachment(0, 0);
		labelData.right = new FormAttachment(100, 0);
		label.setLayoutData(labelData);

		Integer SWTstyle = SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION;
		tabViewer = safrToolkit.createTableViewer(composite, SWTstyle);
		table = tabViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setData(SAFRLogger.USER, "Metadata Table");

		FormData tableData = new FormData();
		tableData.top = new FormAttachment(0, 0);
		tableData.bottom = new FormAttachment(100, 0);
		tableData.left = new FormAttachment(0, 0);
		tableData.right = new FormAttachment(100, 0);
		table.setLayoutData(tableData);

		if (tabViewer.getContentProvider() == null) {
			tabViewer.setContentProvider(new MainTableContentProvider());
		}

		MainTreeItem navItem = ApplicationMediator.getAppMediator().getCurrentNavItem();
		if (navItem != null) {
			createColumns(navItem.getId());
			metadataLabelProvider = new MainTableLabelProvider();
			metadataLabelProvider.setInput(navItem.getId());

			tabViewer.setLabelProvider(metadataLabelProvider);
			tabViewer.setInput(navItem);

			// CQ 9027. Nikita. 04/02/2011
			// Don't call sorter on the table if the previously selected item in
			// the Navigator is 'Administration', as no table is displayed in
			// this case.
			if (navItem.getId() != TreeItemId.ADMINISTRATION) {
			    sortTable(navItem.getId());
			}
			table.setVisible(true);
			getViewPartName(navItem.getId());
		} else {
			metadataLabelProvider = new MainTableLabelProvider();

		}
		mainTableFilter = new MainTableFilter();
		tabViewer.addFilter(mainTableFilter);

		// Code for tracking the focus on the table
		IFocusService service = (IFocusService) getSite().getService(
				IFocusService.class);
		service.addFocusTracker(table, "MetadataViewTable");

		// Code for Context menu
		// First we create a menu Manager
		MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(tabViewer.getTable());
		// Set the MenuManager
		tabViewer.getTable().setMenu(menu);
		getSite().registerContextMenu(menuManager, tabViewer);

		// Make the selection available
		getSite().setSelectionProvider(tabViewer);

		// Add selection listener to tabviewer
		getSite().getPage().addSelectionListener(this);

		tabViewer.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				super.mouseDown(e);
				Point pt = new Point(e.x, e.y);
			    TableItem item = table.getItem(pt);
			    if (item == null) {
			    	table.setData(SAFRLogger.USER, "Metadata Table");
			    }
			    else {
			    	table.setData(SAFRLogger.USER, "Metadata Table (" + item.getText(1) +")");
			    }
				getSite().getPage().activate(getViewSite().getPart());
			}
		});

		tabViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!(event.getSelection()).equals(null)) {
					if (event.getSelection() instanceof IStructuredSelection) {
						String err = "";
						try {
							IStructuredSelection selection = (IStructuredSelection) event
									.getSelection();
							IEditorInput input = null;
							String editorID = null;
							SAFRFactory factory = SAFRApplication
									.getSAFRFactory();

							// Displays the hourglass in between the process of
							// double clicking the metadata view element
							// and displaying it in the editor area.
							getSite().getShell().setCursor(
									getSite().getShell().getDisplay()
											.getSystemCursor(SWT.CURSOR_WAIT));

							if (selection.getFirstElement() instanceof EnvironmentQueryBean) {
								/*
								 * Code to set the parameters to open the
								 * Environment Editor in edit mode with the
								 * selected environment.
								 */
								EnvironmentQueryBean environment = (EnvironmentQueryBean) selection
										.getFirstElement();
								if (environment.hasAdminRights()) {
									input = new EnvironmentEditorInput(
											factory.getEnvironment(environment
													.getId()),
											EditRights.ReadModify);
								} else {
									input = new EnvironmentEditorInput(
											factory.getEnvironment(environment
													.getId()), EditRights.Read);
								}
								editorID = EnvironmentEditor.ID;
							} else if (selection.getFirstElement() instanceof ControlRecordQueryBean) {
								/*
								 * Code to set the parameters to open the
								 * Control Record Editor in edit mode with the
								 * selected control record.
								 */
								ControlRecordQueryBean controlRecord = (ControlRecordQueryBean) selection
										.getFirstElement();
								input = new ControlRecordEditorInput(
								    factory.getControlRecord(controlRecord.getId()), controlRecord.getRights());
								editorID = ControlRecordEditor.ID;

							} else if (selection.getFirstElement() instanceof PhysicalFileQueryBean) {
								// Set parameters to open physical file editor.
								PhysicalFileQueryBean physicalFileBean = 
								    (PhysicalFileQueryBean) selection.getFirstElement();
								PhysicalFile physicalFile = factory.getPhysicalFile(physicalFileBean.getId());
								input = new PhysicalFileEditorInput(physicalFile, physicalFileBean.getRights());
								editorID = PhysicalFileEditor.ID;

								// Retrieve the warnings, if present, to be
								// displayed after loading of component
								err = "";
								for (String msg : physicalFile
										.getLoadWarnings()) {
									err += msg + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK;
								}
							} else if (selection.getFirstElement() instanceof GroupQueryBean) {
								// Set parameters to open Group editor.
								GroupQueryBean groupQueryBean = (GroupQueryBean) selection
										.getFirstElement();
								input = new GroupEditorInput(factory
										.getGroup(groupQueryBean.getId()));
								editorID = GroupEditor.ID;
							} else if (selection.getFirstElement() instanceof ViewFolderQueryBean) {
								ViewFolderQueryBean viewFolderBean = (ViewFolderQueryBean) selection
										.getFirstElement();
								input = new ViewFolderEditorInput(factory
										.getViewFolder(viewFolderBean.getId()),
										viewFolderBean.getRights());
								editorID = ViewFolderEditor.ID;
							} else if (selection.getFirstElement() instanceof ViewQueryBeanConv) {
							    ViewQueryBeanConv viewBean = (ViewQueryBeanConv) selection.getFirstElement();
								View view = null;
								try {
									view = factory.getView(viewBean.getId());
									input = new ViewEditorInput(view, viewBean
											.getRights());
								} catch (SAFRDependencyException de) {
									DependencyMessageDialog.openDependencyDialog(
									    getSite().getShell(),"Inactive Dependencies",
									    "The View could not be loaded because the following component(s) are inactive."
									    + " Please reactivate these and try again.",
									    de.getDependencyString(),MessageDialog.ERROR,
									    new String[] { IDialogConstants.OK_LABEL },0);
									return;
								}
								editorID = ViewEditor.ID;

								// Retrieve the warnings, if present, to be
								// displayed after loading of component
								err = "";
								for (String msg : view.getLoadWarnings()) {
									err += msg + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK;
								}
							} else if (selection.getFirstElement() instanceof UserQueryBean) {
								UserQueryBean userBean = (UserQueryBean) selection
										.getFirstElement();
								input = new UserEditorInput(factory
										.getUser(userBean.getId()));
								editorID = UserEditor.ID;
							} else if (selection.getFirstElement() instanceof UserExitRoutineQueryBean) {
								UserExitRoutineQueryBean userExitRoutineQueryBean = (UserExitRoutineQueryBean) selection
										.getFirstElement();
								UserExitRoutine userExit = factory
										.getUserExitRoutine(userExitRoutineQueryBean
												.getId());
								userExit.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));
								input = new UserExitRoutineEditorInput(
										userExit, userExitRoutineQueryBean
												.getRights());
								editorID = UserExitRoutineEditor.ID;

								// Retrieve the warnings, if present, to be
								// displayed after loading of component
								err = "";
								for (String msg : userExit.getLoadWarnings()) {
									err += msg + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK;
								}

							} else if (selection.getFirstElement() instanceof LogicalFileQueryBean) {
								LogicalFileQueryBean logicalFileQueryBean = (LogicalFileQueryBean) selection
										.getFirstElement();
								input = new LogicalFileEditorInput(factory
										.getLogicalFile(logicalFileQueryBean
												.getId()), logicalFileQueryBean
										.getRights());
								editorID = LogicalFileEditor.ID;
							} else if (selection.getFirstElement() instanceof LogicalRecordQueryBean) {
								LogicalRecordQueryBean logicalRecordQueryBean = (LogicalRecordQueryBean) selection
										.getFirstElement();
								LogicalRecord logicalRecord = factory
										.getLogicalRecord(logicalRecordQueryBean
												.getId());
								input = new LogicalRecordEditorInput(
										logicalRecord, logicalRecordQueryBean
												.getRights());
								editorID = LogicalRecordEditor.ID;

								// Retrieve the warnings, if present, to be
								// displayed after loading of component
								err = "";
								for (String msg : logicalRecord
										.getLoadWarnings()) {
									err += msg + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK;
								}
							} else if (selection.getFirstElement() instanceof LookupQueryBean) {
								LookupQueryBean lookupQueryBean = (LookupQueryBean) selection
										.getFirstElement();
								LookupPath lookupPath = null;
								try {
									lookupPath = factory
											.getLookupPath(lookupQueryBean
													.getId());
									input = new LookupPathEditorInput(
											lookupPath, lookupQueryBean
													.getRights());
								} catch (SAFRDependencyException de) {
									DependencyMessageDialog
											.openDependencyDialog(
													getSite().getShell(),
													"Inactive Logical Records",
													"The Lookup Path could not be loaded due to the following Logical Record(s) are inactive."
															+ " Please reactivate the Logical Record(s) and try again.",
													de.getDependencyString(),
													MessageDialog.ERROR,
													new String[] { IDialogConstants.OK_LABEL },
													0);
									return;
								}
								editorID = LookupPathEditor.ID;

								// Retrieve the warnings, if present, to be
								// displayed after loading of component
								err = "";
								for (String msg : lookupPath.getLoadWarnings()) {
									err += msg + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK;
								}
							}

							getSite().getPage().openEditor(input, editorID);
							if (err != null && !err.equals("")) {
								MultiErrorMessageDialog
										.openMultiErrorMessageDialog(
												Display.getCurrent()
														.getActiveShell(),
												"Warnings",
												"Following problems were found while loading this component.",
												err, MessageDialog.WARNING,
												new String[] { "OK" }, 0);
							}
						} catch (PartInitException e1) {
							UIUtilities.handleWEExceptions(e1,"Unexpected error occurred while opening an editor.",null);
						} catch (SAFRException e) {
							UIUtilities.handleWEExceptions(e,"Unexpected error occurred while opening an editor.",null);
						}

						finally {
							getSite().getShell().setCursor(null);
						}

					}
				}
			}

		});
		tabViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			boolean deleteMetadataAllowed = false;

			public void selectionChanged(SelectionChangedEvent event) {

				selectedMetadataComponent = (IStructuredSelection) event.getSelection();

				reportsMenuAllowed = true;

				User user = SAFRApplication.getUserSession().getUser();
				deleteMetadataAllowed = false;
				environmentReportAllowed = false;
				viewPropertiesReportAllowed = false;
				logicalRecordReportAllowed = false;
				lookupPathsReportAllowed = false;
				depCheckMetadataAllowed = false;

				if (selectedMetadataComponent.getFirstElement() instanceof LogicalRecordQueryBean) {
			        logicalRecordReportAllowed = true;
				} else if (selectedMetadataComponent.getFirstElement() instanceof LookupQueryBean) {
                    lookupPathsReportAllowed = true;					
				} else if (selectedMetadataComponent.getFirstElement() instanceof ViewQueryBeanConv) {
                    viewPropertiesReportAllowed = true;
				} else if (selectedMetadataComponent.getFirstElement() instanceof EnvironmentQueryBean) {
					environmentReportAllowed = true;
				}

				try {

					if (user.isSystemAdmin()) {
						clearEnvAllowed = true;
						if (SAFRApplication.getUserSession().getEnvironment().getId().equals("")) {
							deleteMetadataAllowed = false;
						} else {
							// if user is a system admin then delete is allowed
							// for all components.
							if (!selectedMetadataComponent.isEmpty()) {
								deleteMetadataAllowed = true;
							} else {
								deleteMetadataAllowed = false;
							}
						}
					} else if (SAFRApplication.getUserSession().isEnvironmentAdministrator()) {
						// if user is environment admin then
						if ((selectedMetadataComponent.getFirstElement() instanceof UserQueryBean)
								|| (selectedMetadataComponent.getFirstElement() instanceof EnvironmentQueryBean)
								|| (selectedMetadataComponent.getFirstElement() instanceof GroupQueryBean)) {
							// delete not allowed for user, Group,Environment,
							deleteMetadataAllowed = false;
						} else {
							// delete is allowed on CR,GF
							if (!selectedMetadataComponent.isEmpty()) {
							    
		                        if (selectedMetadataComponent.getFirstElement() instanceof ControlRecordQueryBean) {
	                                deleteMetadataAllowed = true;							    
		                        } else {
                                    EnvironmentalQueryBean environmentalBean = (EnvironmentalQueryBean) selectedMetadataComponent.getFirstElement();
                                    if (environmentalBean.getRights() == EditRights.ReadModifyDelete) {
                                        deleteMetadataAllowed = true;
                                    } else {
                                        deleteMetadataAllowed = false;                                        
                                    }
		                        }
							} else {
								deleteMetadataAllowed = false;
							}
						}
						// Disable the clear Environment menu option if the
						// environment in the metadata view selected has no
						// admin rights or enable the menu option.
						if (!selectedMetadataComponent.isEmpty() && 
						    selectedMetadataComponent.getFirstElement() instanceof EnvironmentQueryBean) {
							EnvironmentQueryBean selectedComp = (EnvironmentQueryBean) selectedMetadataComponent.getFirstElement();
							if (selectedComp != null) {
								if (selectedComp.hasAdminRights()) {
									clearEnvAllowed = true;
								} else {
									clearEnvAllowed = false;
								}
							}
						}

					} else {
						// if user is neither System Admin nor Environment Admin

						// Deletion of Environment, CR, GF is not allowed.
						if ((selectedMetadataComponent.getFirstElement() instanceof EnvironmentQueryBean) || 
						    (selectedMetadataComponent.getFirstElement() instanceof ControlRecordQueryBean)) {
							deleteMetadataAllowed = false;

						} else {
							// deletion of other component is allowed based on the rights assigned.
							if (!selectedMetadataComponent.isEmpty()) {
								EnvironmentalQueryBean environmentalBean = (EnvironmentalQueryBean) selectedMetadataComponent.getFirstElement();
								if (environmentalBean.getRights() == EditRights.ReadModifyDelete) {
                                    deleteMetadataAllowed = true;                                           
								}
								else {
                                    deleteMetadataAllowed = false;								    
								}
							}
							else {
	                            deleteMetadataAllowed = false;							    
							}
						}
					}
				} catch (DAOException e) {
					UIUtilities.handleWEExceptions(e,
					    "Database error while getting permissions of user on the current environment",
					    UIUtilities.titleStringDbException);
				}

				// If the environment selected in the metadata list is same as
				// the environment in which the user is logged into, the delete
				// icon on the metadata toolbar gets disabled restricting the
				// user from delete env operation.

				if (!selectedMetadataComponent.isEmpty()
						&& selectedMetadataComponent.getFirstElement() instanceof EnvironmentQueryBean) {

					EnvironmentQueryBean bean = (EnvironmentQueryBean) selectedMetadataComponent.getFirstElement();

					if (bean != null && 
					    bean.getId().equals(SAFRApplication.getUserSession().getEnvironment().getId())) {
						deleteMetadataAllowed = false;						
					}
				}
				
                if (selectedMetadataComponent.getFirstElement() instanceof UserExitRoutineQueryBean ||
                    selectedMetadataComponent.getFirstElement() instanceof PhysicalFileQueryBean ||
                    selectedMetadataComponent.getFirstElement() instanceof LogicalFileQueryBean ||
                    selectedMetadataComponent.getFirstElement() instanceof LogicalRecordQueryBean || 
                    selectedMetadataComponent.getFirstElement() instanceof LookupQueryBean || 
                    selectedMetadataComponent.getFirstElement() instanceof ViewQueryBeanConv || 
                    selectedMetadataComponent.getFirstElement() instanceof ViewFolderQueryBean) {
                    depCheckMetadataAllowed = true;
                } 
				

				SourceProvider sourceProvider = UIUtilities.getSourceProvider();
				sourceProvider.setReportsMenuMetadataSelection(reportsMenuAllowed);
				sourceProvider.setLogicalRecordListMetadataview(logicalRecordReportAllowed);
				sourceProvider.setLookupPathListMetadataView(lookupPathsReportAllowed);
				sourceProvider.setViewListMetadataView(viewPropertiesReportAllowed);
				sourceProvider.setEnvironmentListMetadataView(environmentReportAllowed);
				sourceProvider.setDepCheckMenuMetadataSelection(depCheckMetadataAllowed);
				sourceProvider.setDeleteAllowed(deleteMetadataAllowed);
				sourceProvider.setClearEnvironment(clearEnvAllowed);

			}
		});

		getSite().getPage().addPartListener(this);
	}

	private void sortTable(TreeItemId id) { 
        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, id.name());
        if (prefs.load()) {
            tabViewer.getTable().setSortColumn(
                tabViewer.getTable().getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                tabViewer.getTable().setSortDirection(SWT.UP);
                setTableSorter(id, prefs.getColumn(), SWT.UP);
            }
            else {
                tabViewer.getTable().setSortDirection(SWT.DOWN);
                setTableSorter(id, prefs.getColumn(), SWT.DOWN);                        
            }                   
        }
        else {
            int defaultCol = 0;
            if (id != TreeItemId.USER) {
                defaultCol = 1;
            } 
            tabViewer.getTable().setSortColumn(
                tabViewer.getTable().getColumn(defaultCol));
            tabViewer.getTable().setSortDirection(SWT.UP);
            setTableSorter(id, defaultCol, SWT.UP);
        }
	}
	
	/**
	 * Method to refresh the table viewer when a metadata component is saved
	 */
	public void refreshView() {
		// set force refresh of content provider to get the latest data from DB
		((MainTableContentProvider) tabViewer.getContentProvider())
				.setForceRefresh(true);
		tabViewer.refresh();
		((MainTableContentProvider) tabViewer.getContentProvider())
				.setForceRefresh(false);
        ApplicationMediator.getAppMediator().refreshMetaBar();                
	}

	@Override
	public void setFocus() {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		if (page != null) {
    		IEditorPart activeEditor = page.getActiveEditor();
    		//Need to ensure the activeEditor is not a ReportEditor
    		//which is not derived from SAFREditorPart
    		if (activeEditor != null && 
    			activeEditor instanceof SAFREditorPart ) {
    			((SAFREditorPart) activeEditor).disableSaveAs();
    		}
    
    		tabViewer.getTable().setFocus();
		}
	}

	/**
	 * This method resets previous Item to {@link TreeItemId} ABOUT. It should
	 * be called only when changing of SAFR User Environment.
	 */
	public void resetPreviousItem() {
		// forcefully refresh the table contents.
		((MainTableContentProvider) tabViewer.getContentProvider())
				.resetPreviousItem();
		previousItem = new MainTreeItem(TreeItemId.ABOUT, null, null, null);
	}

	// Notify this listener that the selection has changed
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	    if (part instanceof NavigatorView) {
    		// Check if a tree element is selected
    		if (selection instanceof IStructuredSelection) {
    			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
    			if (structuredSelection.getFirstElement() instanceof MainTreeItem) {
    				try {
    
    					viewPropertiesReportAllowed = false;
    					logicalRecordReportAllowed = false;
    					lookupPathsReportAllowed = false;
    					reportsMenuAllowed = false;
    					environmentReportAllowed = false;
    					clearEnvAllowed = false;
    					/*
    					 * Get the selected item from the tree of metadata
    					 * components in the navigator view.
    					 */
    					selectedItem = ((MainTreeItem) structuredSelection.getFirstElement());
    					ApplicationMediator.getAppMediator().setCurrentNavItem(selectedItem);
    
    					// CQ 8371. Nikita. 12/08/2010
    					// Don't show metadata view if item selected in the
    					// navigator view is "Administration"
    					if (selectedItem != null) {
    						if (selectedItem.getId() == TreeItemId.ADMINISTRATION) {
    							table.setVisible(false);
    							label.setVisible(true);
    							// previousItem = selectedItem;
    						} else {
    							table.setVisible(true);
    							label.setVisible(false);
    
    							/*
    							 * Check if currently selected item is the same as
    							 * the previously selected item. If yes, do not
    							 * refresh the metadata view.
    							 */
    							if (!(selectedItem.equals(previousItem))) {
    								getSite().getShell().setCursor(
    										getSite()
    												.getShell()
    												.getDisplay()
    												.getSystemCursor(
    														SWT.CURSOR_WAIT));
    								table.setVisible(false);
    								tabViewer.setSorter(null);
    								createColumns(selectedItem.getId());
    
    								/*
    								 * Call method to create an instance of the
    								 * appropriate label provider on the basis of
    								 * the metadata component chosen.
    								 */
    								metadataLabelProvider.setInput(selectedItem.getId());
    								tabViewer.setLabelProvider(metadataLabelProvider);
    								tabViewer.setInput(selectedItem);
    								TreeItemId curNav = ApplicationMediator.getAppMediator().getCurrentNavItem().getId();
    								sortTable(curNav);
    								table.setVisible(true);
    
    								previousItem = selectedItem;
    
    					            Display.getDefault().asyncExec(new Runnable() {
    					                public void run() {	
    					                    if (!tabViewer.getTable().isDisposed()) {
    					                        tabViewer.getTable().setFocus();
    					                    }
    					                }
    					            });
    								UIUtilities.getSourceProvider().setDeleteAllowed(false);
    								
    							}
    						}
    						getViewPartName(selectedItem.getId());
    					}
    				} finally {
    					getSite().getShell().setCursor(null);
    				}
    			}
    		}
	    }
	}

	@Override
	public void dispose() {
		super.dispose();
		// Remove the added selection listener
		getSite().getPage().removeSelectionListener(this);
		getSite().getPage().removePartListener(this);
	}

	/**
	 * Method to create columns of the table in the metadata view
	 * 
	 * @param <b>TreeId</b> ID of the metadata component chosen in the navigator
	 */

	public void createColumns(TreeItemId treeId) {

		// Remove existing columns and populate the table with new columns
		removeColumns();

		String columnHeaders[] = MetadataTableInfo.getColumnHeaders(treeId);
		int length = columnHeaders.length;
		int columnWidths[] = MetadataTableInfo.getColumnWidths(treeId);
		for (iCounter = 0; iCounter < length; iCounter++) {
			TableViewerColumn column = new TableViewerColumn(tabViewer,SWT.NONE);
			column.getColumn().setText(columnHeaders[iCounter]);
			column.getColumn().setWidth(columnWidths[iCounter]);
			column.getColumn().setToolTipText(columnHeaders[iCounter]);
			column.getColumn().setResizable(true);
			ColumnSelectionListener colListener = new ColumnSelectionListener(iCounter, treeId);
			/*
			 * Add selection listener to each column so that the table is sorted
			 * when that column is selected.
			 */
			column.getColumn().addSelectionListener(colListener);
		}

	}

	/**
	 * Method to remove existing columns of the table
	 */
	public void removeColumns() {
		for (final TableColumn column : table.getColumns()) {
			column.dispose();
		}
	}

	/**
	 * This class extends SelectionAdapter class and contains a method that
	 * invokes the appropriate sorter class when a particular column is selected
	 * depending on the metadata component chosen.
	 * 
	 */
	private class ColumnSelectionListener extends SelectionAdapter {
		private int colNumber;
		private TreeItemId treeId;

		private ColumnSelectionListener(int colNumber, TreeItemId treeId) {
			this.colNumber = colNumber;
			this.treeId = treeId;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			try {

				getSite().getShell().setCursor(
						getSite().getShell().getDisplay()
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
					if (currentColumn == table.getColumn(0)) {
						dir = SWT.DOWN;
					} else {
						dir = SWT.UP;
					}
				}

				tabViewer.getTable().setSortDirection(dir);
				setTableSorter(treeId, colNumber, dir);
				Order order = Order.ASCENDING;
				if (dir == SWT.DOWN) {
				    order = Order.DESCENDING;
				}
                SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, treeId.name(), colNumber, order);
                prefs.store();
			} finally {
				getSite().getShell().setCursor(null);

			}

		}
	}

	/**
	 * This method is used to set the part name of the metadata view based upon
	 * the selection made in the Navigator view.
	 * 
	 * @param itemId
	 *            : The Tree item selected in navigator view.
	 */
	private void getViewPartName(TreeItemId itemId) {
	    if (itemId == TreeItemId.ENV) {
			this.setPartName("Metadata List: Environments");
			this.setTitleImage(UIUtilities
					.getAndRegisterImage(ImageKeys.ENVIRONMENT));
		} else if (itemId == TreeItemId.USEREXITROUTINE) {
			this.setPartName("Metadata List: User-Exit Routines");
			this.setTitleImage(UIUtilities
					.getAndRegisterImage(ImageKeys.USEREXITROUTINE));
		} else if (itemId == TreeItemId.CONTROL) {
			this.setPartName("Metadata List: Control Records");
			this.setTitleImage(UIUtilities
					.getAndRegisterImage(ImageKeys.CONTROLRECORD));
		} else if (itemId == TreeItemId.PHYSICALFILE) {
			this.setPartName("Metadata List: Physical Files");
			this.setTitleImage(UIUtilities
					.getAndRegisterImage(ImageKeys.PHYSICALFILE));
		} else if (itemId == TreeItemId.LOGICALFILE) {
			this.setPartName("Metadata List: Logical Files");
			this.setTitleImage(UIUtilities
					.getAndRegisterImage(ImageKeys.lOGICALFILE));
		} else if (itemId == TreeItemId.LOGICALRECORD) {
			this.setPartName("Metadata List: Logical Records");
			this.setTitleImage(UIUtilities
					.getAndRegisterImage(ImageKeys.LOGICALRECORD));
		} else if (itemId == TreeItemId.LOOKUP) {
			this.setPartName("Metadata List: Lookup Paths");
			this.setTitleImage(UIUtilities
					.getAndRegisterImage(ImageKeys.LOOKUPPATH));
		} else if (itemId == TreeItemId.VIEWFOLDER) {
			this.setPartName("Metadata List: View Folders");
			this.setTitleImage(UIUtilities
					.getAndRegisterImage(ImageKeys.FOLDER));
		} else if (itemId == TreeItemId.VIEWFOLDERCHILD) {
			this.setPartName("Metadata List: "
					+ UIUtilities.getComboString(ApplicationMediator.getAppMediator()
							.getCurrentNavItem().getName(), ApplicationMediator.getAppMediator()
							.getCurrentNavItem().getMetadataId()));
			this.setTitleImage(UIUtilities
					.getAndRegisterImage(ImageKeys.FOLDER));
		} else if (itemId == TreeItemId.USER) {
			this.setPartName("Metadata List: Users");
			this.setTitleImage(UIUtilities.getAndRegisterImage(ImageKeys.USER));
		} else if (itemId == TreeItemId.GROUP) {
			this.setPartName("Metadata List: Groups");
			this.setTitleImage(UIUtilities.getAndRegisterImage(ImageKeys.GROUP));
		} else if (itemId == TreeItemId.ADMINISTRATION) {
			this.setPartName("Metadata List");
			this.setTitleImage(UIUtilities
					.getAndRegisterImage(ImageKeys.ADMINISTRATOR));
		}
	}
	
	private void deleteEnvironments() {
	    
        // Display an hour glass till the Environment is deleted.
        Display.getCurrent()
                .getActiveShell()
                .setCursor(
                        Display.getCurrent().getActiveShell()
                                .getDisplay()
                                .getSystemCursor(SWT.CURSOR_WAIT));

        // Log of components actually deleted
        String deleteReport="Deleted environments:";
        
        List<NumericIdQueryBean> beans = getSelectedComponents();
        for (NumericIdQueryBean numFileBean : beans) {
            boolean lastBean = false;
            if (numFileBean == beans.get(beans.size()-1)) {
                lastBean = true;
            }
            boolean success = false;
            try {
                if (closeRelatedEditor(numFileBean)) {

                    while (!success) {
                        try {
                            // Begin Transaction
                            DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
                            SAFRApplication.getSAFRFactory().removeEnvironment(numFileBean.getId());    
                            success = true;
                            deleteReport += SAFRUtilities.LINEBREAK + numFileBean.getDescriptor();                            
                        } catch (DAOUOWInterruptedException e) {
                            // UOW interrupted so retry it
                            continue;
                        }
                    } // end while(!success)
                }
                else {
                    throw new SAFRException("Couldn't close Environment Editor.");                    
                }
                
            } catch (DAOException e) {
                UIUtilities.handleWEExceptions(e, deleteReport, UIUtilities.titleStringDbException);
                break;
            } catch (SAFRDependencyException sde) {
                String message = "The Environment " + numFileBean.getDescriptor() + " cannot be deleted as its not empty. Please use the "
                    + "'Clear Environment' utility to clear the environment before deleting.";                
                if (lastBean) {
                    MessageDisableDialog.openError(getSite().getShell(), "Environment Dependency found",
                            message,new String[] { IDialogConstants.OK_LABEL, CAN_REM }, new boolean[]{true, false});
                }
                else {
                    MessageDialog diag = new MessageDialog(
                            getSite().getShell(), 
                            "Environment Dependency found",
                            null,
                            message,
                            MessageDialog.QUESTION,
                            new String[] { SKIP, CAN_REM }, 0);    
                    if (diag.open() != 0) {
                        break;
                    }
                }
                
            } catch (SAFRException se) {
                if (lastBean) {
                    MessageDisableDialog.openError(getSite().getShell(), "Environment Delete",
                            numFileBean.getDescriptor() + ": " + se.getMessage(),
                            new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                            new boolean[]{true, false});
                }
                else {
                    MessageDialog diag = new MessageDialog(getSite().getShell(),"Environment Delete",
                            null,numFileBean.getDescriptor() + ": " + se.getMessage(),
                            MessageDialog.QUESTION,new String[] { SKIP, CAN_REM }, 0);    
                    if (diag.open() != 0) {
                        break;
                    }
                }
            } finally {
                if (success) {
                    // End Transaction.
                    try {
                        DAOFactoryHolder.getDAOFactory().getDAOUOW().end();
                    } catch (DAOException e) {
                        UIUtilities.handleWEExceptions(e, "Error occurred while Comitting the transaction.",
                                UIUtilities.titleStringDbException);
                    }

                } else {
                    // Rollback the transaction.
                    try {
                        DAOFactoryHolder.getDAOFactory().getDAOUOW().fail();
                    } catch (DAOException e) {
                        UIUtilities.handleWEExceptions(e, "Error occurred while rolling back the transaction.",
                                UIUtilities.titleStringDbException);
                    }

                }
            }
        }
        
        logger.log(Level.INFO, deleteReport);                   
        
        // Refresh display post delete
        this.refreshView();
        Display.getCurrent().getActiveShell().setCursor(null);
	}
	
	private void deleteUserExits() {
	    
        // Display an hour glass till User Exit Routine is deleted
        Display.getCurrent()
                .getActiveShell()
                .setCursor(
                        Display.getCurrent().getActiveShell()
                                .getDisplay()
                                .getSystemCursor(SWT.CURSOR_WAIT));

        // Log of components actually deleted
        String deleteReport="Deleted user exits:";
        
        List<NumericIdQueryBean> beans = getSelectedComponents();
        for (NumericIdQueryBean numFileBean : beans) {
            boolean lastBean = false;
            if (numFileBean == beans.get(beans.size() - 1)) {
                lastBean = true;
            }

            try {
                if (closeRelatedEditor(numFileBean)) {
                    SAFRApplication.getSAFRFactory().removeUserExitRoutine(numFileBean.getId());
                    deleteReport += SAFRUtilities.LINEBREAK + numFileBean.getDescriptor();
                } else {
                    throw new SAFRException("Couldn't close User Exit Editor.");
                }
            } catch (DAOException e) {
                UIUtilities.handleWEExceptions(e, deleteReport, UIUtilities.titleStringDbException);
                break;
            } catch (SAFRDependencyException sde) {
                String message = "The User Exit Routine " + numFileBean.getDescriptor() + 
                " cannot be deleted due to the following dependencies." +
                "You must first remove these dependencies.";
                String dependenciesString = sde.getDependencyString();
                if (lastBean) {                
                    DependencyMessageDialog.openDependencyDialog(
                            getSite().getShell(),
                            "User Exit Routine dependencies",
                            message,
                            dependenciesString, MessageDialog.ERROR, 
                            new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                            new boolean[]{true, false}, 0);
                }
                else {
                    int ret = DependencyMessageDialog
                    .openDependencyDialog(
                            getSite().getShell(),
                            "User Exit Routine dependencies",
                            message,
                            dependenciesString, MessageDialog.QUESTION, new String[] { SKIP, CAN_REM }, 0);
                    if (ret != 0) {
                        break;
                    }                    
                }

            } catch (SAFRException se) {
                logger.log(Level.SEVERE, "User Exit Delete", se);
                if (lastBean) {
                    MessageDisableDialog.openError(getSite().getShell(), "User Exit Delete",
                            numFileBean.getDescriptor() + ": " + se.getMessage(),
                            new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                            new boolean[]{true, false});
                }
                else {
                    MessageDialog diag = new MessageDialog(getSite().getShell(),"User Exit Delete",
                            null,numFileBean.getDescriptor() + ": " + se.getMessage(),
                            MessageDialog.QUESTION,new String[] { SKIP, CAN_REM }, 0);    
                    if (diag.open() != 0) {
                        break;
                    }
                }
            }
        }

        logger.log(Level.INFO, deleteReport);                   
        
        // Refresh display post delete
        this.refreshView();
        Display.getCurrent().getActiveShell().setCursor(null);        
	}

    private void deleteControlRecords() {
        // Display an hour glass till ControlRecord is deleted
        Display.getCurrent().getActiveShell()
                .setCursor(Display.getCurrent().getActiveShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

        // Log of components actually deleted
        String deleteReport = "Deleted control records:";

        List<NumericIdQueryBean> beans = getSelectedComponents();
        for (NumericIdQueryBean numFileBean : beans) {
            boolean lastBean = false;
            if (numFileBean == beans.get(beans.size() - 1)) {
                lastBean = true;
            }

            try {
                if (closeRelatedEditor(numFileBean)) {
                    SAFRApplication.getSAFRFactory().removeControlRecord(numFileBean.getId());
                    deleteReport += SAFRUtilities.LINEBREAK + numFileBean.getDescriptor();                    
                } else {
                    throw new SAFRException("Couldn't close Control Record Editor.");                    
                }
            } catch (DAOException e) {
                UIUtilities.handleWEExceptions(e, deleteReport, UIUtilities.titleStringDbException);
                break;
            } catch (SAFRDependencyException sde) {
                String message = "The Control Record " + numFileBean.getDescriptor() + 
                        " cannot be deleted due to the following dependencies." +
                		"You must first remove these dependencies.";
                String dependenciesString = sde.getDependencyString();
                if (lastBean) {                                
                    DependencyMessageDialog.openDependencyDialog(
                            getSite().getShell(),
                            "Control Record dependencies",
                            message,
                            dependenciesString, MessageDialog.ERROR, 
                            new String[] { IDialogConstants.OK_LABEL, CAN_REM }, new boolean[]{true, false}, 0);
                }
                else {
                    int ret = DependencyMessageDialog.openDependencyDialog(
                            getSite().getShell(),
                            "Control Record dependencies",
                            message,
                            dependenciesString, MessageDialog.QUESTION, new String[] { SKIP, CAN_REM }, 0);
                    if (ret != 0) {
                        break;
                    }                                        
                }
            } catch (SAFRException se) {
                logger.log(Level.SEVERE, "Control Record Delete", se);                
                if (lastBean) {
                    MessageDisableDialog.openError(getSite().getShell(), "Control Record Delete",
                            numFileBean.getDescriptor() + ": " + se.getMessage(),
                            new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                            new boolean[]{true, false});
                }
                else {
                    MessageDialog diag = new MessageDialog(getSite().getShell(),"Control Record Delete",
                            null,numFileBean.getDescriptor() + ": " + se.getMessage(),
                            MessageDialog.QUESTION,new String[] { SKIP, CAN_REM }, 0);    
                    if (diag.open() != 0) {
                        break;
                    }
                }
            }
        }

        logger.log(Level.INFO, deleteReport);

        // Refresh display post delete
        this.refreshView();
        Display.getCurrent().getActiveShell().setCursor(null);

    }
	
	private void deletePhysicalFiles() {
	    
            // Display an hour glass till Physical File is deleted
            Display.getCurrent().getActiveShell().setCursor(
                Display.getCurrent().getActiveShell()
                        .getDisplay()
                        .getSystemCursor(SWT.CURSOR_WAIT));
            
            // Log of components actually deleted
            String deleteReport="Deleted physical files:";
            
            List<NumericIdQueryBean> beans = getSelectedComponents();            
    	    for (NumericIdQueryBean numFileBean : beans) {
    	        boolean lastBean = false;
    	        if (numFileBean == beans.get(beans.size()-1)) {
    	            lastBean = true;
    	        }
    	        try {
        	        PhysicalFileQueryBean physicalFileBean = (PhysicalFileQueryBean)numFileBean; 
                    if (closeRelatedEditor(physicalFileBean)) {
                        SAFRApplication.getSAFRFactory().removePhysicalFile(
                                physicalFileBean.getId());
                        deleteReport += SAFRUtilities.LINEBREAK + numFileBean.getDescriptor();
                    } else {
                        throw new SAFRException("Couldn't close Physical File Editor.");
                    }
    	        } catch (DAOException e) {
    	            UIUtilities.handleWEExceptions(e, deleteReport,
    	                    UIUtilities.titleStringDbException);
    	            break;
    	        } catch (SAFRDependencyException sde) {
    	            String dependenciesString = sde.getDependencyString();
    	            String messageString = "";
    	            if (sde.getDependencyList().containsKey(
    	                    ComponentType.LogicalFile)) {
    	                messageString = "The Physical File " + numFileBean.getDescriptor()+ " cannot be deleted. " +
    	                		"You have to manually add another Physical File to the following " +
    	                		"Logical File(s) before deleting this Physical File.";
    	            } else {
    	                messageString = "The Physical File " + numFileBean.getDescriptor()+ " cannot be deleted " +
    	                		"due to the following dependencies." +
    	                		"You must first remove these dependencies.";
    	            }
    	            if (lastBean) {
                        DependencyMessageDialog.openDependencyDialog(getSite()
                                .getShell(), "Physical File dependencies",
                                messageString, dependenciesString,
                                MessageDialog.ERROR,
                                new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                                new boolean[] {true, false}, 0);    	                
    	            }
    	            else {
                        int ret = DependencyMessageDialog.openDependencyDialog(getSite()
                                .getShell(), "Physical File dependencies",
                                messageString, dependenciesString,
                                MessageDialog.QUESTION,
                                new String[] { SKIP, CAN_REM }, 0);    
                        if (ret != 0) {
                            break;
                        }
    	            }

    	        } catch (SAFRException se) {
                    logger.log(Level.SEVERE, "Physical File Delete", se);                    	            
                    if (lastBean) {
                        MessageDisableDialog.openError(getSite().getShell(), "Physical File Delete",
                                numFileBean.getDescriptor() + ": " + se.getMessage(),
                                new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                                new boolean[]{true, false});
                    }
                    else {
                        MessageDialog diag = new MessageDialog(getSite().getShell(),"Physical File Delete",
                                null,numFileBean.getDescriptor() + ": " + se.getMessage(),
                                MessageDialog.QUESTION,new String[] { SKIP, CAN_REM }, 0);    
                        if (diag.open() != 0) {
                            break;
                        }
                    }
    	        }                    
    	    }
    	    
            logger.log(Level.INFO, deleteReport);                   
    	    
    	    // Refresh display post delete
            this.refreshView();
            Display.getCurrent().getActiveShell().setCursor(null);
	}
	
	private void deleteLogicalFiles() {
	    
        // Display an hour glass till Logical File is deleted
        Display.getCurrent().getActiveShell()
                .setCursor(Display.getCurrent().getActiveShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

        // Log of components actually deleted
        String deleteReport = "Deleted logical files:";

        List<NumericIdQueryBean> beans = getSelectedComponents();
        for (NumericIdQueryBean numFileBean : beans) {
            boolean lastBean = false;
            if (numFileBean == beans.get(beans.size() - 1)) {
                lastBean = true;
            }
            try {
                if (closeRelatedEditor(numFileBean)) {
                    SAFRApplication.getSAFRFactory().removeLogicalFile(numFileBean.getId());
                    deleteReport += SAFRUtilities.LINEBREAK + numFileBean.getDescriptor();
                } else {
                    throw new SAFRException("Couldn't close Logical File Editor.");
                }
            } catch (DAOException e) {
                UIUtilities.handleWEExceptions(e, deleteReport, UIUtilities.titleStringDbException);
                break;
            } catch (SAFRDependencyException sde) {
                String dependenciesString = sde.getDependencyString();
                String messageString = "";
                if (sde.getDependencyList().containsKey(ComponentType.LogicalRecord)) {
                    messageString = "The Logical File " + numFileBean.getDescriptor() + " cannot be deleted. " +
                    		"You have to manually add another Logical File to the following " +
                    		"Logical Record(s) before deleting this Logical File.";
                } else {
                    messageString = "The Logical File " + numFileBean.getDescriptor() + " cannot be deleted due to the following " +
                    		"dependencies. You must first remove these dependencies.";
                }
                if (lastBean) {                
                    DependencyMessageDialog.openDependencyDialog(getSite().getShell(), 
                            "Logical File dependencies",
                            messageString, dependenciesString, MessageDialog.ERROR,
                            new String[] { IDialogConstants.OK_LABEL, CAN_REM }, new boolean[]{true, false}, 0);
                } else {
                    int ret = DependencyMessageDialog.openDependencyDialog(getSite().getShell(), 
                            "Logical File dependencies",
                            messageString, dependenciesString, MessageDialog.QUESTION,
                            new String[] { SKIP, CAN_REM }, 0);
                    if (ret != 0) {
                        break;
                    }                    
                }
            } catch (SAFRException se) {
                logger.log(Level.SEVERE, "Logical File Delete", se);                                                   
                if (lastBean) {
                    MessageDisableDialog.openError(getSite().getShell(), "Logical File Delete",
                            numFileBean.getDescriptor() + ": " + se.getMessage(),
                            new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                            new boolean[]{true, false});
                }
                else {
                    MessageDialog diag = new MessageDialog(getSite().getShell(),"Logical File Delete",
                            null,numFileBean.getDescriptor() + ": " + se.getMessage(),
                            MessageDialog.QUESTION,new String[] { SKIP, CAN_REM }, 0);    
                    if (diag.open() != 0) {
                        break;
                    }
                }
            }
        }

        logger.log(Level.INFO, deleteReport);

        // Refresh display post delete
        this.refreshView();
        Display.getCurrent().getActiveShell().setCursor(null);
    }

	private void deleteLogicalRecords() {
        // Display an hour glass till Logical Record is deleted
        Display.getCurrent().getActiveShell()
                .setCursor(Display.getCurrent().getActiveShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

        // Log of components actually deleted
        String deleteReport = "Deleted logical records:";

        List<NumericIdQueryBean> beans = getSelectedComponents();
        for (NumericIdQueryBean numFileBean : beans) {
            boolean lastBean = false;
            if (numFileBean == beans.get(beans.size() - 1)) {
                lastBean = true;
            }
            try {
                if (closeRelatedEditor(numFileBean)) {
                    SAFRApplication.getSAFRFactory().removeLogicalRecord(numFileBean.getId());
                    deleteReport += SAFRUtilities.LINEBREAK + numFileBean.getDescriptor();                    
                } else {
                    throw new SAFRException("Couldn't close Logical Record Editor.");
                }
            } catch (DAOException e) {
                UIUtilities.handleWEExceptions(e, deleteReport, UIUtilities.titleStringDbException);
                break;
            } catch (SAFRDependencyException sde) {
                String dependenciesString = sde.getDependencyString();
                String message = "The Logical Record " + numFileBean.getDescriptor() + 
                " cannot be deleted due to the following dependencies." +
                "You must first remove these dependencies.";

                if (lastBean) {                                
                    DependencyMessageDialog.openDependencyDialog(
                                getSite().getShell(),"Logical Record dependencies",
                                message, dependenciesString, MessageDialog.ERROR, 
                                new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                                new boolean[]{true, false}, 0);
                } else {
                    int ret = DependencyMessageDialog.openDependencyDialog(
                            getSite().getShell(),"Logical Record dependencies",
                            message, dependenciesString, MessageDialog.QUESTION, 
                            new String[] { SKIP, CAN_REM }, 0);
                    if (ret != 0) {
                        break;
                    }                                        
                }
                

            } catch (SAFRException se) {
                logger.log(Level.SEVERE, "Logical Record Delete", se);                                                                   
                if (lastBean) {
                    MessageDisableDialog.openError(getSite().getShell(), "Logical Record Delete",
                            numFileBean.getDescriptor() + ": " + se.getMessage(),
                            new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                            new boolean[]{true, false});
                }
                else {
                    MessageDialog diag = new MessageDialog(getSite().getShell(),"Logical Record Delete",
                            null,numFileBean.getDescriptor() + ": " + se.getMessage(),
                            MessageDialog.QUESTION,new String[] { SKIP, CAN_REM }, 0);    
                    if (diag.open() != 0) {
                        break;
                    }
                }
            }
        }

        logger.log(Level.INFO, deleteReport);

        // Refresh display post delete
        this.refreshView();
        Display.getCurrent().getActiveShell().setCursor(null);
    }

	private void deleteLookups() {
        // Display an hour glass till Lookup Path is deleted
        Display.getCurrent().getActiveShell()
                .setCursor(Display.getCurrent().getActiveShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

        // Log of components actually deleted
        String deleteReport = "Deleted lookups:";

        List<NumericIdQueryBean> beans = getSelectedComponents();
        for (NumericIdQueryBean numFileBean : beans) {
            boolean lastBean = false;
            if (numFileBean == beans.get(beans.size() - 1)) {
                lastBean = true;
            }
            try {
                if (closeRelatedEditor(numFileBean)) {
                    SAFRApplication.getSAFRFactory().removeLookupPath(numFileBean.getId());
                    deleteReport += SAFRUtilities.LINEBREAK + numFileBean.getDescriptor();
                } else {
                    throw new SAFRException("Couldn't close Lookup Editor.");
                }
            } catch (DAOException e) {
                UIUtilities.handleWEExceptions(e, deleteReport, UIUtilities.titleStringDbException);
                break;
            } catch (SAFRDependencyException sde) {
                String dependenciesString = sde.getDependencyString();
                String message = "The Lookup Path " + numFileBean.getDescriptor() + 
                " cannot be deleted due to the following dependencies. " +
                "You must first remove these dependencies.";

                if (lastBean) {                                
                    DependencyMessageDialog.openDependencyDialog(
                                getSite().getShell(),
                                "Lookup Path dependencies",
                                message,
                                dependenciesString, MessageDialog.ERROR, 
                                new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                                new boolean[]{true, false}, 0);
                } else {
                    int ret = DependencyMessageDialog.openDependencyDialog(
                            getSite().getShell(),
                            "Lookup Path dependencies",
                            message,
                            dependenciesString, MessageDialog.QUESTION, 
                            new String[] { SKIP, CAN_REM }, 0);
                    if (ret != 0) {
                        break;
                    }                                                            
                }

            } catch (SAFRException se) {
                logger.log(Level.SEVERE, "Lookup Delete", se);                                                                                   
                if (lastBean) {
                    MessageDisableDialog.openError(getSite().getShell(), "Lookup Delete",
                            numFileBean.getDescriptor() + ": " + se.getMessage(),
                            new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                            new boolean[]{true, false});
                }
                else {
                    MessageDialog diag = new MessageDialog(getSite().getShell(),"Lookup delete",
                            null,numFileBean.getDescriptor() + ": " + se.getMessage(),
                            MessageDialog.QUESTION,new String[] { SKIP, CAN_REM }, 0);    
                    if (diag.open() != 0) {
                        break;
                    }
                }
            }
        }

        logger.log(Level.INFO, deleteReport);

        // Refresh display post delete
        this.refreshView();
        Display.getCurrent().getActiveShell().setCursor(null);        
	}
	
	private void deleteViewFolders() {
        // Display an hour glass till View Folder is deleted
        Display.getCurrent().getActiveShell()
                .setCursor(Display.getCurrent().getActiveShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

        // Log of components actually deleted
        String deleteReport = "Deleted view folders:";

        List<NumericIdQueryBean> beans = getSelectedComponents();
        for (NumericIdQueryBean numFileBean : beans) {
            boolean lastBean = false;
            if (numFileBean == beans.get(beans.size() - 1)) {
                lastBean = true;
            }
            try {
                if (closeRelatedEditor(numFileBean)) {
                    SAFRApplication.getSAFRFactory().removeViewFolder(numFileBean.getId(), null);
                    ApplicationMediator.getAppMediator().refreshNavigator();
                    deleteReport += SAFRUtilities.LINEBREAK + numFileBean.getDescriptor();
                } else {
                    throw new SAFRException("Couldn't close View Folder editor.");
                }

            } catch (DAOException e) {
                UIUtilities.handleWEExceptions(e, deleteReport, UIUtilities.titleStringDbException);
                break;
            } catch (SAFRValidationException sve) {
                SAFRValidationToken safrValidationToken = null;
                String dependenciesString = ((SAFRDependencyException) sve.getCause()).getDependencyString();
                String message = "The View Folder " + numFileBean.getDescriptor()
                + " is used as a default view folder for following user(s). "
                + "This action will delete any reference of the View Folder from these user(s).";

                int response;                
                if (lastBean) {
                    response = DependencyMessageDialog.openDependencyDialog(getSite().getShell(),
                            "View Folder dependencies", message, dependenciesString, MessageDialog.QUESTION, new String[] {
                                    IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
                } else {
                    response = DependencyMessageDialog.openDependencyDialog(getSite().getShell(),
                            "View Folder dependencies", message, dependenciesString, MessageDialog.QUESTION_WITH_CANCEL
                            , new String[] {IDialogConstants.OK_LABEL, SKIP, CAN_REM}, 0);                    
                }
                
                safrValidationToken = sve.getSafrValidationToken();

                if (response == 0) {
                    // OK
                    try {
                        SAFRApplication.getSAFRFactory().removeViewFolder(numFileBean.getId(), safrValidationToken);
                        this.refreshView();
                        ApplicationMediator.getAppMediator().refreshNavigator();
                    } catch (SAFRException se1) {
                        UIUtilities.handleWEExceptions(se1, deleteReport, "Error occurred while removing View Folder.");
                        break;
                    }
                } else if (response == 2) {
                    // Cancel remaining
                    break;
                } else {
                    // Skip/Cancel
                }
            } catch (SAFRException se) {
                logger.log(Level.SEVERE, "View Folder Delete", se);                                                                                   
                if (lastBean) {
                    MessageDisableDialog.openError(getSite().getShell(), "View Folder Delete",
                            numFileBean.getDescriptor() + ": " + se.getMessage(),
                            new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                            new boolean[]{true, false});
                }
                else {
                    MessageDialog diag = new MessageDialog(getSite().getShell(),"View Folder Delete",
                            null,numFileBean.getDescriptor() + ": " + se.getMessage(),
                            MessageDialog.QUESTION,new String[] { SKIP, CAN_REM }, 0);    
                    if (diag.open() != 0) {
                        break;
                    }
                }
            }
        }

        logger.log(Level.INFO, deleteReport);

        // Refresh display post delete
        this.refreshView();
        Display.getCurrent().getActiveShell().setCursor(null);
	}
	
	private void deleteViews(List<ViewQueryBeanConv> beans) {
	    
        // Display an hour glass till view is deleted.
        Display.getCurrent().getActiveShell()
                .setCursor(Display.getCurrent().getActiveShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

        // Log of components actually deleted
        String deleteReport = "Deleted views:";

        for (ViewQueryBeanConv numFileBean : beans) {
            boolean lastBean = false;
            if (numFileBean == beans.get(beans.size() - 1)) {
                lastBean = true;
            }

            if (closeRelatedEditor(numFileBean)) {
                try {
                    SAFRApplication.getSAFRFactory().removeView(numFileBean.getId());
                    deleteReport += SAFRUtilities.LINEBREAK + numFileBean.getDescriptor();
                } catch (DAOException e) {
                    UIUtilities.handleWEExceptions(e, deleteReport, UIUtilities.titleStringDbException);
                    break;                    
                } catch (SAFRException se) {
                    logger.log(Level.SEVERE, "View Delete", se);                                                                                                       
                    if (lastBean) {
                        MessageDisableDialog.openError(getSite().getShell(), "View Delete",
                                numFileBean.getDescriptor() + ": " + se.getMessage(),
                                new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                                new boolean[]{true, false});
                    }
                    else {
                        MessageDialog diag = new MessageDialog(getSite().getShell(),"View Delete",
                                null,numFileBean.getDescriptor() + ": " + se.getMessage(),
                                MessageDialog.QUESTION,new String[] { SKIP, CAN_REM }, 0);    
                        if (diag.open() != 0) {
                            break;
                        }
                    }
                }
            }
        }

        logger.log(Level.INFO, deleteReport);

        // Refresh display post delete
        this.refreshView();
        Display.getCurrent().getActiveShell().setCursor(null);
	}
	
	private void deleteGroups() {
        // Display an hour glass till Group is deleted
        Display.getCurrent().getActiveShell()
                .setCursor(Display.getCurrent().getActiveShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

        // Log of components actually deleted
        String deleteReport="Deleted groups:";
        
        List<NumericIdQueryBean> beans = getSelectedComponents();
        for (NumericIdQueryBean numFileBean : beans) {
            boolean lastBean = false;
            if (numFileBean == beans.get(beans.size() - 1)) {
                lastBean = true;
            }
            try {
                if (closeRelatedEditor(numFileBean)) {
                    SAFRApplication.getSAFRFactory().removeGroup(numFileBean.getId());
                    deleteReport += SAFRUtilities.LINEBREAK + numFileBean.getDescriptor();
                } else {
                    throw new SAFRException("Couldn't close Group Editor.");
                }
            } catch (DAOException e) {
                UIUtilities.handleWEExceptions(e, deleteReport,UIUtilities.titleStringDbException);
                break;
            } catch (SAFRDependencyException sde) {
                String dependenciesString = sde.getDependencyString();
                String message = "The Group " + numFileBean.getDescriptor() + 
                " cannot be deleted due to the following dependencies." +
                " You must first remove these dependencies.";

                if (lastBean) {                                
                    DependencyMessageDialog.openDependencyDialog(
                            getSite().getShell(),"Group dependencies",message, dependenciesString,
                            MessageDialog.ERROR,new String[] { IDialogConstants.OK_LABEL, CAN_REM }, 
                            new boolean[]{true, false},0);
                }
                else {
                    int ret = DependencyMessageDialog.openDependencyDialog(
                            getSite().getShell(),"Group dependencies",message, dependenciesString,
                            MessageDialog.QUESTION,new String[] { SKIP, CAN_REM },0);                    
                    if (ret != 0) {
                        break;
                    }                    
                }
            } catch (SAFRException se) {
                logger.log(Level.SEVERE, "Group Delete", se);                                                                                                       
                if (lastBean) {
                    MessageDisableDialog.openError(getSite().getShell(), "Group Delete",
                            numFileBean.getDescriptor() + ": " + se.getMessage(),
                            new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                            new boolean[]{true, false});
                }
                else {
                    MessageDialog diag = new MessageDialog(getSite().getShell(),"Group Delete",
                            null,numFileBean.getDescriptor() + ": " + se.getMessage(),
                            MessageDialog.QUESTION,new String[] { SKIP, CAN_REM }, 0);    
                    if (diag.open() != 0) {
                        break;
                    }
                }
            } 
        }
        
        logger.log(Level.INFO, deleteReport);                   
        
        // Refresh display post delete
        this.refreshView();
        Display.getCurrent().getActiveShell().setCursor(null);        
	}
	
	private void deleteUsers() {
        Display.getCurrent()
        .getActiveShell()
        .setCursor(
                Display.getCurrent().getActiveShell()
                        .getDisplay()
                        .getSystemCursor(SWT.CURSOR_WAIT));
	    
        // Log of components actually deleted
        String deleteReport="Deleted users:";
        
        List<UserQueryBean> beans = getSelectedUsers();
        for (UserQueryBean userBean : beans) {
            boolean lastBean = false;
            if (userBean == beans.get(beans.size() - 1)) {
                lastBean = true;
            }
            try {
                if (closeRelatedEditor(userBean)) {
                    SAFRApplication.getSAFRFactory().removeUser(userBean.getId());
                    deleteReport += SAFRUtilities.LINEBREAK + userBean.getDescriptor();
                } else {
                    throw new SAFRException("Couldn't close User Editor.");
                }
            } catch (DAOException e) {
                UIUtilities.handleWEExceptions(e, deleteReport, UIUtilities.titleStringDbException);
                break;
            } catch (SAFRException se) {
                if (lastBean) {
                    MessageDisableDialog.openError(getSite().getShell(), "User Delete",
                            userBean.getDescriptor() + ": " + se.getMessage(),
                            new String[] { IDialogConstants.OK_LABEL, CAN_REM },
                            new boolean[]{true, false});
                }
                else {
                    MessageDialog diag = new MessageDialog(getSite().getShell(),"User Delete",
                            null,userBean.getDescriptor() + ": " + se.getMessage(),
                            MessageDialog.QUESTION,new String[] { SKIP, CAN_REM }, 0);    
                    if (diag.open() != 0) {
                        break;
                    }
                }
            }
        }
        
        logger.log(Level.INFO, deleteReport);                   
        
        // Refresh display post delete
        this.refreshView();
        Display.getCurrent().getActiveShell().setCursor(null);        
	}
	
    public void openMetadataComponent() {
        if (selectedMetadataComponent.getFirstElement() instanceof UserQueryBean) {
            for (UserQueryBean user :getSelectedUsers()) {
                EditorOpener.openUser(user.getId());
            }
        }
        else {
            for (NumericIdQueryBean bean : getSelectedComponents()) {
                EditorOpener.open(bean.getId(), bean.getComponentType());
            }
        }
        
    }
	
	/**
	 * This method calls the respective method from model to delete a metadata
	 * component based on the type of component selected in metadata view.
	 */
	public void deleteMetadataComponent() {

		int returnVal = 0;

		if (selectedMetadataComponent.getFirstElement() instanceof EnvironmentQueryBean) {
            returnVal = getMetadataConfirmDialog("This action will delete the selected Environment/s.");
            if (returnVal == 0) {
                deleteEnvironments();
            }
	    } else if (selectedMetadataComponent.getFirstElement() instanceof UserExitRoutineQueryBean) {
            returnVal = getMetadataConfirmDialog("This action will delete the selected User Exit Routine/s.");
            if (returnVal == 0) {
                deleteUserExits();
            }
        } else if (selectedMetadataComponent.getFirstElement() instanceof ControlRecordQueryBean) {
            returnVal = getMetadataConfirmDialog("This action will delete the selected Control Record/s.");
            if (returnVal == 0) {
                deleteControlRecords();
            }           
        }  else if (selectedMetadataComponent.getFirstElement() instanceof PhysicalFileQueryBean) {         
            returnVal = getMetadataConfirmDialog("This action will delete the following Physical File/s.");
            if (returnVal == 0) {
                deletePhysicalFiles();
            }
        }  else if (selectedMetadataComponent.getFirstElement() instanceof LogicalFileQueryBean) {
            returnVal = getMetadataConfirmDialog("This action will delete the selected Logical File/s.");
            if (returnVal == 0) {
                deleteLogicalFiles();                
            }
        }  else if (selectedMetadataComponent.getFirstElement() instanceof LogicalRecordQueryBean) {
            returnVal = getMetadataConfirmDialog("This action will delete the selected Logical Record/s.");
            if (returnVal == 0) {
                deleteLogicalRecords();
            }
        } else if (selectedMetadataComponent.getFirstElement() instanceof LookupQueryBean) {
            returnVal = getMetadataConfirmDialog("This action will delete the selected Lookup Path/s.");
            if (returnVal == 0) {
                deleteLookups();
            }
        } else if (selectedMetadataComponent.getFirstElement() instanceof ViewFolderQueryBean) {
            returnVal = getMetadataConfirmDialog("This action will delete the selected View Folder/s.");
            if (returnVal == 0) {
                deleteViewFolders();
            }

        } else if (selectedMetadataComponent.getFirstElement() instanceof ViewQueryBeanConv) {

            List<ViewQueryBeanConv> viewList = getSelectedViews();
            
            boolean retDelete = false;
            
            List<SAFRQueryBean> delList = new ArrayList<SAFRQueryBean>();
            delList.addAll(viewList);
            if (viewList.size() > 0) {
                MetadataConfirmDialog dialog = new MetadataConfirmDialog(
                        Display.getCurrent().getActiveShell(),
                        "This action will permanently delete the selected view/s and cannot be undone.", 
                        delList);
                returnVal = dialog.open();
                if (returnVal == 0) {
                    retDelete = true;
                }                                
            }
            
            if (retDelete) {
                deleteViews(viewList);
            }            
            
		}  else if (selectedMetadataComponent.getFirstElement() instanceof GroupQueryBean) {
			returnVal = getMetadataConfirmDialog("This action will delete the selected Group/s.");
			if (returnVal == 0) {
			    deleteGroups();
			}
		} else if (selectedMetadataComponent.getFirstElement() instanceof UserQueryBean) {
            List<SAFRQueryBean> delList = new ArrayList<SAFRQueryBean>();
		    delList.addAll(getSelectedUsers());
            MetadataConfirmDialog dialog = new MetadataConfirmDialog(
                    Display.getCurrent().getActiveShell(),
                    "This action will delete the selected User/s.", 
                    delList);
            returnVal = dialog.open();
            if (returnVal == 0) {
			    deleteUsers();
			}
		}
	}

	/**
	 * This method is used to close an editor which is of the same type as
	 * related to selected item to be deleted, if it is open
	 * 
	 * 
	 * @param bean
	 *            : Querybean object to be compared for editor close.
	 */
	private boolean closeRelatedEditor(SAFRQueryBean bean) {
		IEditorReference[] openEditors = getSite().getWorkbenchWindow()
				.getActivePage().getEditorReferences();

		int valueReturned = 1;
		boolean editorOpened = false;
		IEditorPart editor = null;
		boolean editorClosed = true;
		boolean forceDeleteConfirmation = true;
		for (int i = 0; i < openEditors.length; i++) {
			IEditorReference editorPart = openEditors[i];
			editor = editorPart.getEditor(false);
			if (checkEditorOpened(editor, bean)) {
				editorOpened = true;
				if (editor.isDirty()) {
					// if multiple instance of same editor are open show
					// confirmation message only once. If user clicks 'ok' close
					// all instances.
					if (forceDeleteConfirmation) {

						if (bean == null) {
							valueReturned = getDialogBox(MSGCHECK_CLOSE_ALLEDITORS);
						} else {
							valueReturned = getDialogBox(MSGCHECKOPENEDITOR);
						}

						if (valueReturned == 0) {
							forceDeleteConfirmation = false;
						}
					} else {
						valueReturned = 0;
					}
				} else {
					valueReturned = 0;
				}

			} else {
				editorOpened = false;
				valueReturned = 1;
			}

			if (editorOpened) {

				if (valueReturned == 0) {
					getSite().getPage().closeEditor(editor, false);
					editorClosed = true;

				} else {

					// Display.getCurrent().getActiveShell().setCursor(null);
					editorClosed = false;
					break;
				}

			} else {
				editorClosed = true;
			}
		}

		return editorClosed;
	}

	/**
	 * This method checks if the editor relates to the bean being deleted
	 * because if so, it must be closed. For Clear Environment, the bean is null
	 * indicating all open editors must be closed.
	 * 
	 * @param editor
	 *            : Editor currently opened in workbench.
	 * @param bean
	 *            : bean object to compare.
	 */
	private boolean checkEditorOpened(IEditorPart editor, SAFRQueryBean bean) {
		if (editor == null) {
			return false; // editor already closed
		}
		if (bean == null) {
			return true; // this is for Clear Environment
		}
		// the rest is for Delete Component
		if (bean instanceof ViewQueryBeanConv) {
		    ViewQueryBeanConv viewBean = (ViewQueryBeanConv) bean;
			if (editor instanceof ViewEditor) {
				editor = (ViewEditor) editor;
				if (((ViewEditorInput) editor.getEditorInput()).getView()
						.getId().equals(viewBean.getId())) {

					return true;
				}
			} else {
				return false;
			}
		} else if (bean instanceof UserExitRoutineQueryBean) {
			UserExitRoutineQueryBean userExitBean = (UserExitRoutineQueryBean) bean;
			if (editor instanceof UserExitRoutineEditor) {
				editor = (UserExitRoutineEditor) editor;

				if (((UserExitRoutineEditorInput) editor.getEditorInput())
						.getUserExitRoutine().getId()
						.equals(userExitBean.getId())) {
					return true;
				}
			} else {
				return false;
			}
		} else if (bean instanceof ControlRecordQueryBean) {
			ControlRecordQueryBean controlRecordBean = (ControlRecordQueryBean) bean;
			if (editor instanceof ControlRecordEditor) {
				editor = (ControlRecordEditor) editor;

				if (((ControlRecordEditorInput) editor.getEditorInput())
						.getControlRecord().getId()
						.equals(controlRecordBean.getId())) {
					return true;
				}
			} else {
				return false;
			}
		} else if (bean instanceof PhysicalFileQueryBean) {
			PhysicalFileQueryBean physicalFileBean = (PhysicalFileQueryBean) bean;
			if (editor instanceof PhysicalFileEditor) {
				editor = (PhysicalFileEditor) editor;

				if (((PhysicalFileEditorInput) editor.getEditorInput())
						.getPhysicalFile().getId()
						.equals(physicalFileBean.getId())) {
					return true;
				}
			} else {
				return false;
			}
		} else if (bean instanceof LookupQueryBean) {
			LookupQueryBean lookupBean = (LookupQueryBean) bean;
			if (editor instanceof LookupPathEditor) {
				editor = (LookupPathEditor) editor;

				if (((LookupPathEditorInput) editor.getEditorInput())
						.getLookupPath().getId().equals(lookupBean.getId())) {
					return true;
				}
			} else {
				return false;
			}
		} else if (bean instanceof ViewFolderQueryBean) {
			ViewFolderQueryBean viewFolderBean = (ViewFolderQueryBean) bean;
			if (editor instanceof ViewFolderEditor) {
				editor = (ViewFolderEditor) editor;

				if (((ViewFolderEditorInput) editor.getEditorInput())
						.getViewFolder().getId().equals(viewFolderBean.getId())) {
					return true;
				}
			} else {
				return false;
			}
		} else if (bean instanceof LogicalRecordQueryBean) {
			LogicalRecordQueryBean logicalRecordBean = (LogicalRecordQueryBean) bean;
			if (editor instanceof LogicalRecordEditor) {
				editor = (LogicalRecordEditor) editor;

				if (((LogicalRecordEditorInput) editor.getEditorInput())
						.getLogicalRecord().getId()
						.equals(logicalRecordBean.getId())) {
					return true;
				}
			} else {
				return false;
			}
		} else if (bean instanceof GroupQueryBean) {
			GroupQueryBean groupBean = (GroupQueryBean) bean;
			if (editor instanceof GroupEditor) {
				editor = (GroupEditor) editor;

				if (((GroupEditorInput) editor.getEditorInput()).getGroup()
						.getId().equals(groupBean.getId())) {
					return true;
				}
			} else {
				return false;
			}
		} else if (bean instanceof UserQueryBean) {
			UserQueryBean userBean = (UserQueryBean) bean;
			if (editor instanceof UserEditor) {
				editor = (UserEditor) editor;

				if (((UserEditorInput) editor.getEditorInput()).getUser()
						.getUserid().equals(userBean.getId())) {
					return true;
				}
			} else {
				return false;
			}
		} else if (bean instanceof EnvironmentQueryBean) {
			EnvironmentQueryBean envBean = (EnvironmentQueryBean) bean;
			if (editor instanceof EnvironmentEditor) {
				editor = (EnvironmentEditor) editor;

				if (((EnvironmentEditorInput) editor.getEditorInput())
						.getEnvironment().getId().equals(envBean.getId())) {
					return true;
				}
			} else {
				return false;
			}
		} else if (bean instanceof LogicalFileQueryBean) {
			LogicalFileQueryBean logicalFileBean = (LogicalFileQueryBean) bean;
			if (editor instanceof LogicalFileEditor) {
				editor = (LogicalFileEditor) editor;

				if (((LogicalFileEditorInput) editor.getEditorInput())
						.getLogicalFile().getId()
						.equals(logicalFileBean.getId())) {
					return true;
				}
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * This method is to generate a Warning dialog Box with "Ok" and "Cancel"
	 * buttons.
	 * 
	 * @param messageString
	 *            : The Message to be displayed as warning.
	 * @return The selection of user.
	 */
	public int getDialogBox(String messageString) {
		MessageDialog dialog = new MessageDialog(Display.getCurrent()
				.getActiveShell(), "GenevaERS Workbench", null, messageString,
				MessageDialog.WARNING, new String[] { "&OK", "&Cancel" }, 0);
		int returnVal = dialog.open();
		return returnVal;
	}

    /**
     * This method is to generate a Warning dialog Box with "Ok" and "Cancel"
     * buttons. Will include a table of selected components in name order
     * 
     * @param title
     *            : The Message to be displayed as warning.
     * @return The selection of user.
     */
    public int getMetadataConfirmDialog(String title) {
        List<SAFRQueryBean> beans = new ArrayList<SAFRQueryBean>();
        beans.addAll(getSelectedComponents());
        MetadataConfirmDialog dialog = new MetadataConfirmDialog(
                Display.getCurrent().getActiveShell(),
                title, 
                beans);
        int returnVal = dialog.open();
        return returnVal;
    }
	
	/**
	 * Refresh the metadata list if it is showing a list of views and any of the
	 * supplied views exist in the list.
	 * 
	 * @param views
	 *            - list of views to check.
	 */
	public void refreshViewList(List<Integer> views) {
		if(views.isEmpty()){
			return;
		}
		if (selectedItem.getId() == TreeItemId.VIEWFOLDERCHILD) {
			// check if any of the views in the supplied list exists in the
			// metadata table.
			for (TableItem item : tabViewer.getTable().getItems()) {
				if (item.getData() instanceof ViewQueryBeanConv) {
					ViewQueryBeanConv vqb = (ViewQueryBeanConv) item.getData();
					if (views.contains(vqb.getId())) {
						refreshView();
						return;
					}
				}
			}
		}
	}

	public void searchComponent(MetadataSearchCriteria searchCriteria,
			String searchText) {
		// if search criteria is id, then sort the list of metadata components
		// according to id.
		if (searchCriteria == MetadataSearchCriteria.ID) {
			tabViewer.getTable().setSortColumn(table.getColumn(0));
			tabViewer.getTable().setSortDirection(SWT.UP);
			setTableSorter(selectedItem.getId(), 0, SWT.UP);
		} else {
			// if search criteria is name, then sort the list of metadata
			// components
			// according to name.
			tabViewer.getTable().setSortColumn(table.getColumn(1));
			tabViewer.getTable().setSortDirection(SWT.UP);
			setTableSorter(selectedItem.getId(), 1, SWT.UP);
		}

		// get the items from the table.
		for (TableItem item : table.getItems()) {
			SAFRQueryBean bean = (SAFRQueryBean) item.getData();
			if (searchCriteria == MetadataSearchCriteria.ID) {
				if (bean.getIdLabel().toLowerCase()
						.startsWith(searchText.toLowerCase())) {
					tabViewer.setSelection(new StructuredSelection(bean), true);
					return;
				}
			} else if (searchCriteria == MetadataSearchCriteria.NAME) {
				if (bean.getName() != null
						&& bean.getName().toLowerCase()
								.startsWith(searchText.toLowerCase())) {
					tabViewer.setSelection(new StructuredSelection(bean), true);
					return;
				}
			}
		}

		// if no component is found, then show the dialog box.
		MessageDialog
				.openInformation(getSite().getShell(), "Component not found",
						"The component you are trying to search is not found in the list.");

	}

	private void setTableSorter(TreeItemId treeId, int colNumber, int dir) {
	    if (treeId == TreeItemId.VIEWFOLDERCHILD) {
			tabViewer.setSorter(new ViewTableSorter(colNumber, dir));
			componentType = ComponentType.View;
		} else if (treeId == TreeItemId.ENV) {
			tabViewer.setSorter(new EnvironmentTableSorter(colNumber, dir));
			componentType = ComponentType.Environment;
		} else if (treeId == TreeItemId.CONTROL) {
			tabViewer.setSorter(new ControlRecordTableSorter(colNumber, dir));
			componentType = ComponentType.ControlRecord;
		} else if (treeId == TreeItemId.PHYSICALFILE) {
			tabViewer.setSorter(new PhysicalFileTableSorter(colNumber, dir));
			componentType = ComponentType.PhysicalFile;
		} else if (treeId == TreeItemId.USER) {
			tabViewer.setSorter(new UserTableSorter(colNumber, dir));
			componentType = ComponentType.User;
		} else if (treeId == TreeItemId.VIEWFOLDER) {
			tabViewer.setSorter(new ViewFolderTableSorter(colNumber, dir));
			componentType = ComponentType.ViewFolder;
		} else if (treeId == TreeItemId.GROUP) {
			tabViewer.setSorter(new GroupTableSorter(colNumber, dir));
			componentType = ComponentType.Group;
		} else if (treeId == TreeItemId.USEREXITROUTINE) {
			tabViewer.setSorter(new UserExitRoutineTableSorter(colNumber, dir));
			componentType = ComponentType.UserExitRoutine;
		} else if (treeId == TreeItemId.LOGICALRECORD) {
			tabViewer.setSorter(new LogicalRecordTableSorter(colNumber, dir));
			componentType = ComponentType.LogicalRecord;
		} else if (treeId == TreeItemId.LOOKUP) {
			tabViewer.setSorter(new LookupTableSorter(colNumber, dir));
			componentType = ComponentType.LookupPath;
		} else if (treeId == TreeItemId.LOGICALFILE) {
			tabViewer.setSorter(new LogicalFileTableSorter(colNumber, dir));
			componentType = ComponentType.LogicalFile;
		}
	}

	public ComponentType getComponentType() {
        componentType = null;
		if (table.isFocusControl()) {
			TreeItemId treeId = selectedItem.getId();
			if (treeId == TreeItemId.VIEWFOLDERCHILD) {
				componentType = ComponentType.View;
			} else if (treeId == TreeItemId.ENV) {
				componentType = ComponentType.Environment;
			} else if (treeId == TreeItemId.CONTROL) {
				componentType = ComponentType.ControlRecord;
			} else if (treeId == TreeItemId.PHYSICALFILE) {
				componentType = ComponentType.PhysicalFile;
			} else if (treeId == TreeItemId.USER) {
				componentType = ComponentType.User;
			} else if (treeId == TreeItemId.VIEWFOLDER) {
				componentType = ComponentType.ViewFolder;
			} else if (treeId == TreeItemId.GROUP) {
				componentType = ComponentType.Group;
			} else if (treeId == TreeItemId.USEREXITROUTINE) {
				componentType = ComponentType.UserExitRoutine;
			} else if (treeId == TreeItemId.LOGICALRECORD) {
				componentType = ComponentType.LogicalRecord;
			} else if (treeId == TreeItemId.LOOKUP) {
				componentType = ComponentType.LookupPath;
			} else if (treeId == TreeItemId.LOGICALFILE) {
				componentType = ComponentType.LogicalFile;
			} 
		}
		return componentType;
	}

	public List<NumericIdQueryBean> getSelectedComponents() {
		List<NumericIdQueryBean> beans = new ArrayList<NumericIdQueryBean>();
		for (TableItem item : table.getSelection()) {
			beans.add((NumericIdQueryBean) item.getData());
		}
		return beans;
	}
	
    private List<UserQueryBean> getSelectedUsers() {
        List<UserQueryBean> beans = new ArrayList<UserQueryBean>();
        for (TableItem item : table.getSelection()) {
            beans.add((UserQueryBean) item.getData());
        }
        return beans;
    }
	
    private List<ViewQueryBeanConv> getSelectedViews() {
        List<ViewQueryBeanConv> beans = new ArrayList<ViewQueryBeanConv>();
        
        for (TableItem item : table.getSelection()) {
            ViewQueryBeanConv vbean = (ViewQueryBeanConv) item.getData();
            vbean = new ViewQueryBeanConv(
                    vbean.getEnvironmentId(), vbean.getId(), vbean.getName(),
                    vbean.getStatus(), vbean.getOldOutputFormat(), vbean.getOldType(),
                    vbean.getRights(),  
                    vbean.getCreateTime(), vbean.getCreateBy(),
                    vbean.getModifyTime(), vbean.getModifyBy(), vbean.getCompilerVersion(),
                    vbean.getActivatedTime(), vbean.getActivatedBy());                                
            beans.add(vbean);
        }
        return beans;
    }

	public void partActivated(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false).equals(this)) {
			MainTreeItem item = null;
			reportsMenuAllowed = true;
			if (selectedItem == null) {
				// try to get from navigator view
				NavigatorView view = (NavigatorView) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.findView("SAFRWE.Treeview");
				if (view != null) {
					item = view.getSelectedItem();
				} else {
					// get the last selected item from utilities class
					item = ApplicationMediator.getAppMediator().getCurrentNavItem();
				}
				selectedItem = item;
			} else {
				item = selectedItem;
			}

			if (item == null) {
			    return;
			}
			// CQ 8394 Kanchan Rauthan If condition is added to check if nothing
			// is selected in metadata view then the related menu should not be
			// enabled on closing and reopening metadata view.
			if (tabViewer.getTable().getSelectionCount() != 0) {
				if (item.getId() == TreeItemId.LOGICALRECORD) {
					logicalRecordReportAllowed = true;
				} else if (item.getId() == TreeItemId.LOOKUP) {
					lookupPathsReportAllowed = true;
				} else if (item.getId() == TreeItemId.VIEWFOLDERCHILD) {
					viewPropertiesReportAllowed = true;
				} else if (item.getId() == TreeItemId.ENV) {
					environmentReportAllowed = true;
					clearEnvAllowed = true;
				} else {
					reportsMenuAllowed = false;
					clearEnvAllowed = false;
				}
			}
			// CQ 8371. Nikita. 12/08/2010
			// Don't show metadata view if item selected in the
			// navigator view is "Administration" when metadata view is reopened
			// via Windows->Show MetadataView			
			if (item.getId() == TreeItemId.ADMINISTRATION) {
				tabViewer.getTable().setVisible(false);
				label.setVisible(true);
			} else {
				tabViewer.getTable().setVisible(true);
				label.setVisible(false);
			}

            // update the status bar
            ApplicationMediator.getAppMediator().refreshMetaBar();

			SourceProvider sourceProvider = UIUtilities.getSourceProvider();
			sourceProvider.setReportsMenuMetadataSelection(reportsMenuAllowed);
			sourceProvider.setLogicalRecordListMetadataview(logicalRecordReportAllowed);
			sourceProvider.setLookupPathListMetadataView(lookupPathsReportAllowed);
			sourceProvider.setViewListMetadataView(viewPropertiesReportAllowed);
			sourceProvider.setEnvironmentListMetadataView(environmentReportAllowed);
			sourceProvider.setClearEnvironment(clearEnvAllowed);
			
            ApplicationMediator.getAppMediator().updateStatusContribution(
                ApplicationMediator.STATUSBARMETA, null, true);
			
		}
	}

	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	public void partClosed(IWorkbenchPartReference partRef) {
	}

	public void partDeactivated(IWorkbenchPartReference partRef) {

		if (partRef.getPart(false).equals(this)) {
			SourceProvider sourceProvider = UIUtilities.getSourceProvider();
			sourceProvider.setReportsMenuMetadataSelection(false);
			sourceProvider.setLogicalRecordListMetadataview(false);
			sourceProvider.setLookupPathListMetadataView(false);
			sourceProvider.setViewListMetadataView(false);
			sourceProvider.setEnvironmentListMetadataView(false);
			sourceProvider.setClearEnvironment(false);
			
            ApplicationMediator.getAppMediator().updateStatusContribution(
                ApplicationMediator.STATUSBARMETA, null, false);			
		}
	}

	public void partHidden(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {	    
            SourceProvider service = UIUtilities.getSourceProvider();
            service.setMetadataVisible(false);
        }
	}

	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

	public void partOpened(IWorkbenchPartReference partRef) {
	}

	public void partVisible(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {
            SourceProvider service = UIUtilities.getSourceProvider();
            service.setMetadataVisible(true);
        }
	}

	public void clearEnvironment() {
		if (this.getComponentType() != null) {
			if (this.getComponentType().equals(ComponentType.Environment)) {
                EnvironmentQueryBean env = (EnvironmentQueryBean) this
                    .getSelectedComponents().get(0);
			    
				MessageDialog dialog = new MessageDialog(
					Display.getCurrent().getActiveShell(),
					"Clear Environment",null,
					"This action will remove all components and view definitions from the selected environment " +
					env.getName() +" [" + env.getId() + "].", 
					MessageDialog.WARNING,
					new String[] { IDialogConstants.OK_LABEL,IDialogConstants.CANCEL_LABEL }, 0);
				
				int returnVal = dialog.open();
				if (returnVal == 0) {
					// if user clicks OK

					boolean success = false;
					try {

						// Display an hour glass till all the component's data
						// is cleared.
						Display.getCurrent().getActiveShell().setCursor(
						    Display.getCurrent().getActiveShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

						while (!success) {
							try {
								// Begin Transaction
								DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();

								// close all the related editors before clearing
								// an environment.
								if (closeRelatedEditor(null)) {

									SAFRApplication.getSAFRFactory().clearEnvironment(env.getId());

									// CQ 8326 Kanchan Rauthan 06/08/2010
									// Deleted VFs was still showing in
									// Navigator View after Clear Environment

									// code to refresh navigator view if the
									// current login environment is cleared.

									if (env.getId().equals(UIUtilities.getCurrentEnvironmentID())) {
							            ApplicationMediator.getAppMediator().refreshNavigator();
									}
								}
								success = true;

							} catch (DAOUOWInterruptedException e) {
								// UOW interrupted so retry it
								continue;
							}

						} // end while(!success)

					} catch (DAOException daoE) {
						UIUtilities.handleWEExceptions(daoE,
						    "Error occurred while clearing the components data from the environment.",
						    UIUtilities.titleStringDbException);
					} catch (SAFRException e) {
						UIUtilities.handleWEExceptions(e);
					} finally {

						if (success) {
							// End Transaction.
							try {
								DAOFactoryHolder.getDAOFactory().getDAOUOW()
										.end();
							} catch (DAOException e) {
								UIUtilities.handleWEExceptions(e,
								    "Error occurred while Comitting the transaction.",
								    UIUtilities.titleStringDbException);
							}

						} else {
							// Rollback the transaction.
							try {
								DAOFactoryHolder.getDAOFactory().getDAOUOW()
										.fail();
							} catch (DAOException e) {
								UIUtilities.handleWEExceptions(e,
								    "Error occurred while rolling back the transaction.",
								    UIUtilities.titleStringDbException);
							}

						}
						// return cursor to normal
						Display.getCurrent().getActiveShell().setCursor(null);
					}
				}
			}

		} else {
			// if user clicks CANCEL
		}
	}

	public List<List<String>> getTableContents() {

	    List<List<String>> result = new ArrayList<List<String>>();
	    List<String> line = new ArrayList<String>();
	    
        // get headers
        Table table = tabViewer.getTable();
        final int[] columnOrder = table.getColumnOrder();
        for(int columnOrderIndex = 0; columnOrderIndex < columnOrder.length; columnOrderIndex++) {
            int columnIndex = columnOrder[columnOrderIndex];
            TableColumn tableColumn = table.getColumn(columnIndex);        
            line.add(tableColumn.getText());
        }
        result.add(line);

        // get the rest of the table
        final int itemCount = table.getItemCount();
        for(int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
            TableItem item = table.getItem(itemIndex);
            
            line = new ArrayList<String>();
            for(int columnOrderIndex = 0; 
                    columnOrderIndex < columnOrder.length; 
                    columnOrderIndex++) {
                int columnIndex = columnOrder[columnOrderIndex];
                line.add(item.getText(columnIndex));
            }
            result.add(line);
        }	    
	    return result;
	}
	
    public void filterByName(boolean byName) {
        mainTableFilter.setByName(byName);
        tabViewer.refresh();
        // update the status bar
        ApplicationMediator.getAppMediator().refreshMetaBar();        
    }
	
	public void filterSearchText(String searchText) {
	    mainTableFilter.setSearchText(searchText);
        tabViewer.refresh();
        // update the status bar
        ApplicationMediator.getAppMediator().refreshMetaBar();        
	}
	
	private int getRowCount() {
	    if (tabViewer == null || !tabViewer.getTable().isVisible()) {
	        return 0;
	    }
	    else {
    	    Table table = tabViewer.getTable();	    
    	    return table.getItemCount();
	    }
	}
	
    public String getMetaLine() {
        String statusLineMsg = "Row Count: ";
        Integer rowCount = getRowCount();
        statusLineMsg += rowCount;
        
        Integer activeCount=0; 

        if (tabViewer != null) {
            Table table = tabViewer.getTable();     
            // get activate/inactive
            if (componentType == ComponentType.LogicalRecord) {
                int itemCount = table.getItemCount();
                for(int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
                    TableItem item = table.getItem(itemIndex);                
                    if (item.getText(2).equalsIgnoreCase("Active")) {
                        activeCount++;
                    }                                
                }       
                Integer inactiveCount = rowCount-activeCount;
                statusLineMsg += "   Active: "+ activeCount + "   Inactive: " + inactiveCount;            
            }
            else if (componentType == ComponentType.LookupPath) {
                int itemCount = table.getItemCount();
                for(int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
                    TableItem item = table.getItem(itemIndex);                
                    if (item.getText(2).equalsIgnoreCase("Active")) {
                        activeCount++;
                    }                                
                }       
                Integer inactiveCount = rowCount-activeCount;
                statusLineMsg += "   Active: "+ activeCount + "   Inactive: " + inactiveCount;                        
            }
            else if (componentType == ComponentType.View) {
                int itemCount = table.getItemCount();
                for(int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
                    TableItem item = table.getItem(itemIndex);                
                    if (item.getText(2).equalsIgnoreCase("Active")) {
                        activeCount++;
                    }                                
                }       
                Integer inactiveCount = rowCount-activeCount;
                statusLineMsg += "   Active: "+ activeCount + "   Inactive: " + inactiveCount;                                    
            }
        }
        statusLineMsg += "     ";
        return statusLineMsg;
    }

    public void openDepChecker() {
		if(DAOFactoryHolder.getDAOFactory().getConnectionParameters().getType().equals(DBType.PostgresQL)) {
			MessageDialog.openInformation(getSite().getShell(),
					"Dependency Checker", "Dependency Checker not available via a Postgres database connection");
			return;
		}
       EnvironmentalQueryBean bean = (EnvironmentalQueryBean) selectedMetadataComponent.getFirstElement();
        
        DepCheckOpener.open(bean);        
    }

    public boolean isFiltered() {
        if (mainTableFilter == null) {
            return false;
        }
        return mainTableFilter.isFiltered();
    }
	

}
