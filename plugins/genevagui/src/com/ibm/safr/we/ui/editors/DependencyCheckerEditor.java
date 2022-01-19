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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.services.ISourceProviderService;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.DependencyUsageType;
import com.ibm.safr.we.constants.ReportType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordFieldQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.NumericIdQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.utilities.DependencyChecker;
import com.ibm.safr.we.model.utilities.DependencyCheckerNode;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.utilities.ColumnSelectionListenerForTableCombo;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.LogicalRecordFieldTableSorter;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.LogicalFileTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.LogicalRecordTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.LookupTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.MainTableLabelProvider;
import com.ibm.safr.we.ui.views.metadatatable.PhysicalFileTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.UserExitRoutineTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.ViewFolderTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.ViewTableSorter;
import com.ibm.safr.we.utilities.ProfileLocation;

public class DependencyCheckerEditor extends SAFREditorPart {
	public static String ID = "SAFRWE.DependencyCheckerEditor";

	private ScrolledForm form;
	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;

	private String headerNote = "Use this utility to get dependencies for a component of specified environment.";

	private Section sectionDependencyCriteria;

    List<EnvironmentQueryBean> envList;	
	private TableCombo comboEnvironment;
	private String selectedEnvironment = "";
	protected EnvironmentQueryBean currentEnvironment;
	protected Integer currentEnvID;
	private String currentEnvName;

	protected ComponentType componentType;
	private Combo comboComponentType;
	private TableCombo comboComponent;

	private Group groupOutput;
	private Button radioShowAllFields;
	private Button radioShowFieldsFromLR;
	private Combo comboShowFieldsFromLR;
	private Button chkboxDirectDepsOnly;
	
	private Button buttonCheck;
	private Button buttonExport;

	protected int selectedComponentType = -1;
	protected int selectedLogicalRecord = -1;
	protected LogicalRecordQueryBean currentLogicalRecord;
	protected String selectedComponent = "";
	protected EnvironmentalQueryBean currentComponent;
	private Section sectionDependencyResults;
	private TreeViewer dependencyTreeViewer;

	private DependencyChecker dpChecker;
	private String str;
	private int indent = 0;
	private int increment = 6;
	char vLine = '\u2502';
	char hLine = '\u251C';
	char lastLine = '\u2514';
	private Boolean proceedFlag = true;
	Map<ComponentType, List<DependencyCheckerNode>> nodeMap = new HashMap<ComponentType, List<DependencyCheckerNode>>();
	List<DependencyCheckerNode> nodeList = new ArrayList<DependencyCheckerNode>();

	private TableComboViewer comboEnvironmentViewer;
	private TableComboViewer comboComponentViewer;
	private ColumnSelectionListenerForTableCombo[] prvsListner = new ColumnSelectionListenerForTableCombo[2];
	private Button radioExportAsReport;
	private Button radioExportAsCSV;
	private Text textLocation;
	private Button buttonLocation;
	protected DirectoryDialog dialogLocation;
	private Section sectionExport;
	private int csvSequence;
    private MenuItem compOpenEditorItem = null;
	
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FormLayout());
		form.setText("Dependency Checker");

		Label labelHeaderNote = safrGuiToolkit.createLabel(form.getBody(),
				SWT.NONE, headerNote);
		labelHeaderNote.setForeground(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_GRAY));
		FormData dataLabelHeaderNote = new FormData();
		dataLabelHeaderNote.top = new FormAttachment(0, 0);
		dataLabelHeaderNote.left = new FormAttachment(0, 5);
		labelHeaderNote.setLayoutData(dataLabelHeaderNote);

		createSectionDependencyCriteria(form.getBody());
		createSectionDependencyResults(form.getBody());
		form.reflow(true);

	}

	private void createSectionDependencyResults(Composite body) {
		sectionDependencyResults = safrGuiToolkit.createSection(body,
				Section.TITLE_BAR, "Dependency Tree:");
		FormData dataSectionDependencyResults = new FormData();
		dataSectionDependencyResults.top = new FormAttachment(0, 20);
		dataSectionDependencyResults.left = new FormAttachment(50, 0);
		dataSectionDependencyResults.right = new FormAttachment(100, -5);
		dataSectionDependencyResults.bottom = new FormAttachment(100, -10);
		sectionDependencyResults.setLayoutData(dataSectionDependencyResults);

		Composite compositeDependencyResults = safrGuiToolkit.createComposite(
				sectionDependencyResults, SWT.NONE);
		compositeDependencyResults.setLayout(new FormLayout());
		FormData dataCompositeDependencyResults = new FormData();
		dataCompositeDependencyResults.top = new FormAttachment(0, 0);
		dataCompositeDependencyResults.left = new FormAttachment(0, 0);
		dataCompositeDependencyResults.right = new FormAttachment(0, 0);
		dataCompositeDependencyResults.bottom = new FormAttachment(0, 0);
		compositeDependencyResults
				.setLayoutData(dataCompositeDependencyResults);
		sectionDependencyResults.setClient(compositeDependencyResults);
		sectionDependencyResults.setEnabled(false);

		try {
			getSite().getShell().setCursor(
					getSite().getShell().getDisplay().getSystemCursor(
							SWT.CURSOR_WAIT));
			dependencyTreeViewer = safrGuiToolkit
					.createTreeViewer(compositeDependencyResults);
			FormData dataDependencyTreeViewer = new FormData();
			dataDependencyTreeViewer.top = new FormAttachment(0, 10);
			dataDependencyTreeViewer.left = new FormAttachment(0, 10);
			dataDependencyTreeViewer.right = new FormAttachment(100, -10);
			dataDependencyTreeViewer.bottom = new FormAttachment(100, -10);
			dependencyTreeViewer.getTree().setLayoutData(
					dataDependencyTreeViewer);

			dependencyTreeViewer.setUseHashlookup(false);

			dependencyTreeViewer
					.setContentProvider(new DependencyTreeContentProvider());
			dependencyTreeViewer
					.setLabelProvider(new DependencyTreeLabelProvider());
			ColumnViewerToolTipSupport.enableFor(dependencyTreeViewer,
					ToolTip.NO_RECREATE);

			dependencyTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    DependencyCheckerNode node = (DependencyCheckerNode)((TreeSelection) event
                            .getSelection()).getFirstElement();
                    if (node != null) {
                        if (node.isComponent() && node.getType() != null) {
                            setPopupEnabled(true);
                        }
                        else {
                            setPopupEnabled(false);
                        }
                    }
                    else {
                        setPopupEnabled(false);                        
                    }
                }
            });
			
			dependencyTreeViewer
					.addDoubleClickListener(new IDoubleClickListener() {

						public void doubleClick(DoubleClickEvent event) {
							proceedFlag = true;

							try {
								getSite().getShell().setCursor(
										getSite().getShell().getDisplay()
												.getSystemCursor(
														SWT.CURSOR_WAIT));
								DependencyCheckerNode selectedNode = (DependencyCheckerNode) ((TreeSelection) event
										.getSelection()).getFirstElement();
								if (selectedNode.isComponent()) {
									ComponentType selectedNodetype = null;
									DependencyCheckerNode node1 = selectedNode
											.getParentNode();
									while (selectedNodetype == null) {
										// iterate over all the parent nodes
										// until a component type is found
										if (node1 == null) {
											return;
										}
										selectedNodetype = getTypeFromLabel(node1
												.getName());
										node1 = node1.getParentNode();
									}
									if (selectedNodetype == ComponentType.ControlRecord) {
										return;
									}
									nodeList.add(selectedNode);
									nodeMap.put(selectedNodetype, nodeList);

									for (ComponentType type : nodeMap.keySet()) {
										if (selectedNodetype == type
												|| selectedNodetype == componentType) {
											for (DependencyCheckerNode node : nodeMap
													.get(type)) {
												if ((selectedNode.getId()
														.equals(node.getId()) && !node
														.getChildNodes()
														.isEmpty())
														|| (selectedNode
																.getName()
																.equals(
																		currentComponent
																				.getName())
																&& selectedNode
																		.getId()
																		.equals(
																				currentComponent
																						.getId()) && selectedNodetype

														== componentType)) {
													proceedFlag = false;
													break;
												}
											}
										}
									}
								}

								if (proceedFlag) {
									if (selectedNode.isComponent()) {
										DependencyChecker dpChecker = new DependencyChecker(
												currentEnvID, currentEnvName,
												currentComponent.getId(),
												(ComponentType) componentType,
												currentComponent.getName(),
												chkboxDirectDepsOnly
														.getSelection());
										try {
											dpChecker
													.getDependency(selectedNode);
											dependencyTreeViewer.refresh();
										} catch (DAOException e) {
											UIUtilities.handleWEExceptions(e,"Unexpected error occurred while getting the dependencies.",null);
										}
									}
								}

							} finally {
								getSite().getShell().setCursor(null);

							}

						}

					});

			FormData dependencyTreeData = new FormData();
			dependencyTreeData.top = new FormAttachment(0, 0);
			dependencyTreeData.bottom = new FormAttachment(100, 0);
			dependencyTreeData.left = new FormAttachment(0, 0);
			dependencyTreeData.right = new FormAttachment(100, 0);
			dependencyTreeViewer.getTree().setLayoutData(dependencyTreeData);
			
	        // Code for Context menu
	        // First we create a menu Manager
	        MenuManager menuManager = new MenuManager();
	        Menu menu = menuManager.createContextMenu(dependencyTreeViewer.getTree());
	        // Set the MenuManager
	        dependencyTreeViewer.getTree().setMenu(menu);
	        getSite().registerContextMenu(menuManager, dependencyTreeViewer);        
	        setPopupEnabled(false);     
			
		} finally {
			getSite().getShell().setCursor(null);
		}
	}

    public DependencyCheckerNode getCurrentSelection() {
        return (DependencyCheckerNode)((TreeSelection) dependencyTreeViewer.getSelection()).getFirstElement();
    }
	
    private void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.DEPCHECK);
        service.setDepCheck(enabled);
    }
	
	private void createSectionDependencyCriteria(Composite body) {
		sectionDependencyCriteria = safrGuiToolkit.createSection(body,
				Section.TITLE_BAR, "Dependency Criteria");
		FormData dataSectionDependencyCriteria = new FormData();
		dataSectionDependencyCriteria.top = new FormAttachment(0, 20);
		dataSectionDependencyCriteria.left = new FormAttachment(0, 5);
		dataSectionDependencyCriteria.right = new FormAttachment(50, -5);
		sectionDependencyCriteria.setLayoutData(dataSectionDependencyCriteria);

		Composite compositeDependencyCriteria = safrGuiToolkit.createComposite(
				sectionDependencyCriteria, SWT.NONE);
		compositeDependencyCriteria.setLayout(new FormLayout());
		FormData dataCompositeDependencyCriteria = new FormData();
		dataCompositeDependencyCriteria.top = new FormAttachment(0, 0);
		dataCompositeDependencyCriteria.left = new FormAttachment(0, 0);
		dataCompositeDependencyCriteria.right = new FormAttachment(0, 0);
		compositeDependencyCriteria
				.setLayoutData(dataCompositeDependencyCriteria);
		sectionDependencyCriteria.setClient(compositeDependencyCriteria);

		Label labelEnvironments = safrGuiToolkit.createLabel(
				compositeDependencyCriteria, SWT.NONE, "&Environment: ");
		FormData dataLabelEnvironments = new FormData();
		dataLabelEnvironments.top = new FormAttachment(0, 10);
		dataLabelEnvironments.left = new FormAttachment(0, 5);
		labelEnvironments.setLayoutData(dataLabelEnvironments);

		comboEnvironmentViewer = safrGuiToolkit.createTableComboForComponents(
				compositeDependencyCriteria, ComponentType.Environment);

		comboEnvironment = comboEnvironmentViewer.getTableCombo();
		FormData dataComboEnvironments = new FormData();
		dataComboEnvironments.top = new FormAttachment(0, 10);
		dataComboEnvironments.left = new FormAttachment(labelEnvironments, 25);
		// dataComboEnvironments.right = new FormAttachment(100, -200);
		dataComboEnvironments.width = 375;
		comboEnvironment.setLayoutData(dataComboEnvironments);

		populateEnvironment(comboEnvironment, SAFRApplication.getUserSession()
				.getUser());
		Environment environment = SAFRApplication.getUserSession()
				.getEnvironment();
		String currentEnv = environment.getName();
		comboEnvironment.select(comboEnvironment.indexOf(UIUtilities
				.getComboString(currentEnv, environment.getId())));
		comboEnvironment.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				try {
					if (!(comboEnvironment.getText()
							.equals(selectedEnvironment))) {
						currentEnvironment = (EnvironmentQueryBean) comboEnvironment
								.getTable().getSelection()[0].getData();
						if (currentEnvironment != null) {
							currentEnvID = currentEnvironment.getId();
							currentEnvName = currentEnvironment.getName();
							comboShowFieldsFromLR.removeAll();
							comboComponent.getTable().removeAll();
							comboComponent.select(-1);
							buttonExport.setEnabled(false);
							buttonCheck.setEnabled(false);
							chkboxDirectDepsOnly.setEnabled(false);
							chkboxDirectDepsOnly.setSelection(false);
						}
						getSite().getShell().setCursor(
								getSite().getShell().getDisplay()
										.getSystemCursor(SWT.CURSOR_WAIT));
						selectedEnvironment = comboEnvironment.getText();
						if (componentType != null) {
							chkboxDirectDepsOnly.setSelection(isDirectDepsTicked());
							if (componentType
									.equals(ComponentType.LogicalRecordField)) {
								if (radioShowAllFields.getSelection()) {
									populateComponentList();
								} else {
									populateComboShowFieldsFromLR();
								}
							} else {
								populateComponentList();
							}
						}
					}
				} finally {
					getSite().getShell().setCursor(null);
				}
			}

		});

		comboEnvironment.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!(comboEnvironment.getText().equals(selectedEnvironment))) {

					dependencyTreeViewer.setInput(null);
					dependencyTreeViewer.refresh();
					sectionDependencyResults.setEnabled(false);
				}
			}
		});
		selectedEnvironment = comboEnvironment.getText();
		currentEnvironment = (EnvironmentQueryBean) comboEnvironment.getTable()
				.getSelection()[0].getData();
		currentEnvID = currentEnvironment.getId();
		currentEnvName = currentEnvironment.getName();

		Label labelComponentType = safrGuiToolkit.createLabel(
				compositeDependencyCriteria, SWT.NONE, "Component &Type:");
		FormData dataLabelComponentType = new FormData();
		dataLabelComponentType.width = SWT.DEFAULT;
		dataLabelComponentType.top = new FormAttachment(comboEnvironment, 10);
		dataLabelComponentType.left = new FormAttachment(0, 5);
		labelComponentType.setLayoutData(dataLabelComponentType);

		comboComponentType = safrGuiToolkit.createComboBox(
				compositeDependencyCriteria, SWT.READ_ONLY, "");
		FormData dataComboComponentType = new FormData();
		dataComboComponentType.left = new FormAttachment(labelEnvironments, 25);
		dataComboComponentType.top = new FormAttachment(comboEnvironment, 10);
		dataComboComponentType.width = 350;
		comboComponentType.setLayoutData(dataComboComponentType);

		int i = 0;
		comboComponentType.add(ComponentType.LogicalRecordField.getLabel());
		comboComponentType.setData(String.valueOf(i++),ComponentType.LogicalRecordField);
		comboComponentType.add(ComponentType.LookupPath.getLabel());
		comboComponentType.setData(String.valueOf(i++),ComponentType.LookupPath);
		comboComponentType.add(ComponentType.LogicalFile.getLabel());
		comboComponentType.setData(String.valueOf(i++),ComponentType.LogicalFile);
		comboComponentType.add(ComponentType.LogicalRecord.getLabel());
		comboComponentType.setData(String.valueOf(i++),ComponentType.LogicalRecord);
		comboComponentType.add(ComponentType.PhysicalFile.getLabel());
		comboComponentType.setData(String.valueOf(i++),ComponentType.PhysicalFile);
		comboComponentType.add(ComponentType.UserExitRoutine.getLabel());
		comboComponentType.setData(String.valueOf(i++),ComponentType.UserExitRoutine);
		comboComponentType.add(ComponentType.View.getLabel());
		comboComponentType.setData(String.valueOf(i++), ComponentType.View);
        comboComponentType.add(ComponentType.ViewFolder.getLabel());
        comboComponentType.setData(String.valueOf(i++), ComponentType.ViewFolder);

		comboComponentType.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				try {
					if (comboComponentType.getSelectionIndex() != selectedComponentType) {
						componentType = (ComponentType) comboComponentType
								.getData(String.valueOf(comboComponentType
										.getSelectionIndex()));
						getSite().getShell().setCursor(
								getSite().getShell().getDisplay()
										.getSystemCursor(SWT.CURSOR_WAIT));
						
						chkboxDirectDepsOnly.setSelection(isDirectDepsTicked());
						chkboxDirectDepsOnly.setEnabled(false);
						buttonCheck.setEnabled(false);
						
						comboComponent.getTable().removeAll();
						comboComponent.select(-1);
						comboComponent.setEnabled(true);
						populateComponentList();

						buttonExport.setEnabled(false);

						if (componentType == ComponentType.LogicalRecordField) {
							groupOutput.setVisible(true);
							radioShowAllFields.setSelection(true);
							radioShowAllFields.forceFocus();
							radioShowFieldsFromLR.setSelection(false);
							comboShowFieldsFromLR.removeAll();
						} else {
							groupOutput.setVisible(false);
							comboComponent.setFocus();
						}
						selectedComponentType = comboComponentType
								.getSelectionIndex();
					}
				} finally {
					getSite().getShell().setCursor(null);
				}
			}
		});
		comboComponentType.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!(comboComponentType.getText()
						.equals(selectedComponentType))) {

					dependencyTreeViewer.setInput(null);
					dependencyTreeViewer.refresh();
					sectionDependencyResults.setEnabled(false);
				}
			}
		});

		groupOutput = safrGuiToolkit.createGroup(compositeDependencyCriteria,
				SWT.BORDER_SOLID, "");
		groupOutput.setVisible(false);
		groupOutput.setLayout(new FormLayout());

		FormData dataGroupOutput = new FormData();
		dataGroupOutput.left = new FormAttachment(labelEnvironments, 25);
		dataGroupOutput.top = new FormAttachment(comboComponentType, 5);
		// dataGroupOutput.right = new FormAttachment(100, -10);
		dataGroupOutput.width = 370;
		groupOutput.setLayoutData(dataGroupOutput);
		/*
		 * Jaydeep 13th August 2010, CQ 8358 : In Dep Checker change 'Field' to
		 * 'LR Field'
		 */
		radioShowAllFields = safrGuiToolkit.createRadioButton(groupOutput,
				"Show a&ll LR Fields");
		FormData dataRadioShowAllFields = new FormData();
		dataRadioShowAllFields.top = new FormAttachment(0, 10);
		dataRadioShowAllFields.left = new FormAttachment(0, 10);
		radioShowAllFields.setLayoutData(dataRadioShowAllFields);
		radioShowAllFields.setSelection(true);

		radioShowAllFields.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				try {
					if (radioShowAllFields.getSelection()) {
						comboShowFieldsFromLR.setEnabled(false);
						comboShowFieldsFromLR.removeAll();
						comboShowFieldsFromLR.select(-1);
						comboComponent.getTable().removeAll();
						comboComponent.select(-1);
						getSite().getShell().setCursor(
								getSite().getShell().getDisplay()
										.getSystemCursor(SWT.CURSOR_WAIT));
						populateComponentList();
						comboComponent.setEnabled(true);
						comboComponent.setFocus();
						buttonExport.setEnabled(false);
						buttonCheck.setEnabled(false);
						chkboxDirectDepsOnly.setEnabled(false);
						chkboxDirectDepsOnly.setSelection(isDirectDepsTicked());
						if (dependencyTreeViewer.getTree() != null) {
							dependencyTreeViewer.setInput(null);
							dependencyTreeViewer.refresh();
							sectionDependencyResults.setEnabled(false);
						}
					}

				} finally {
					getSite().getShell().setCursor(null);
				}
			}
		});
		/*
		 * Jaydeep 13th August 2010, CQ 8358 : In Dep Checker change 'Field' to
		 * 'LR Field'
		 */
		radioShowFieldsFromLR = safrGuiToolkit.createRadioButton(groupOutput,
				"Show LR Fields &only from the following LR:");
		FormData dataRadioShowFieldsFromLR = new FormData();
		dataRadioShowFieldsFromLR.top = new FormAttachment(radioShowAllFields,
				10);
		dataRadioShowFieldsFromLR.left = new FormAttachment(0, 10);
		radioShowFieldsFromLR.setLayoutData(dataRadioShowFieldsFromLR);
		radioShowFieldsFromLR.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				try {
					if (radioShowFieldsFromLR.getSelection()) {
						comboShowFieldsFromLR.setEnabled(true);
						comboComponent.getTable().removeAll();
						comboComponent.select(-1);
						comboComponent.setEnabled(false);
						getSite().getShell().setCursor(
								getSite().getShell().getDisplay()
										.getSystemCursor(SWT.CURSOR_WAIT));

						if (comboShowFieldsFromLR.getSelectionIndex() != -1) {
							populateComponentList();
						} else {
							populateComboShowFieldsFromLR();
							comboShowFieldsFromLR.setFocus();
						}
						buttonExport.setEnabled(false);
						buttonCheck.setEnabled(false);
						chkboxDirectDepsOnly.setEnabled(false);
						chkboxDirectDepsOnly.setSelection(isDirectDepsTicked());
						if (dependencyTreeViewer.getTree() != null) {
							dependencyTreeViewer.setInput(null);
							dependencyTreeViewer.refresh();
							sectionDependencyResults.setEnabled(false);
						}
					}
				} finally {
					getSite().getShell().setCursor(null);
				}
			}

		});

		comboShowFieldsFromLR = safrGuiToolkit.createComboBox(groupOutput,
				SWT.READ_ONLY, "");
		FormData dataShowFieldsFromLR = new FormData();
		dataShowFieldsFromLR.left = new FormAttachment(0, 15);
		dataShowFieldsFromLR.top = new FormAttachment(radioShowFieldsFromLR, 10);
		dataShowFieldsFromLR.bottom = new FormAttachment(100, -10);
		dataShowFieldsFromLR.right = new FormAttachment(100, -10);
		comboShowFieldsFromLR.setLayoutData(dataShowFieldsFromLR);
		comboShowFieldsFromLR.setEnabled(false);
		comboShowFieldsFromLR.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub

			}

			public void focusLost(FocusEvent e) {
				try {
					if (comboShowFieldsFromLR.getSelectionIndex() != selectedLogicalRecord) {
						LogicalRecordQueryBean previousLR = currentLogicalRecord;
						if (dependencyTreeViewer.getTree() != null
								&& !comboShowFieldsFromLR.getData(
										String.valueOf(comboShowFieldsFromLR
												.getSelectionIndex())).equals(
										previousLR)) {
							dependencyTreeViewer.setInput(null);
							dependencyTreeViewer.refresh();
							sectionDependencyResults.setEnabled(false);
							comboComponent.getTable().removeAll();
							comboComponent.select(-1);
						}

						currentLogicalRecord = (LogicalRecordQueryBean) comboShowFieldsFromLR
								.getData(String.valueOf(comboShowFieldsFromLR
										.getSelectionIndex()));
						getSite().getShell().setCursor(
								getSite().getShell().getDisplay()
										.getSystemCursor(SWT.CURSOR_WAIT));
						comboComponent.setEnabled(true);
						if (currentLogicalRecord != null
								&& !currentLogicalRecord.equals(previousLR)) {
							comboComponent.setEnabled(true);
							comboComponent.setFocus();
							populateComponentList();
							buttonExport.setEnabled(false);
							buttonCheck.setEnabled(false);
							chkboxDirectDepsOnly.setEnabled(false);
							chkboxDirectDepsOnly.setSelection(isDirectDepsTicked());
						}

					}
				} finally {
					getSite().getShell().setCursor(null);
				}
			}
		});

		Label labelComponent = safrGuiToolkit.createLabel(
				compositeDependencyCriteria, SWT.NONE, "Compo&nent:");
		FormData dataLabelComponent = new FormData();
		dataLabelComponent.left = new FormAttachment(0, 5);
		dataLabelComponent.top = new FormAttachment(groupOutput, 10);
		labelComponent.setLayoutData(dataLabelComponent);

		comboComponentViewer = safrGuiToolkit
				.createTableComboForComponents(compositeDependencyCriteria);
		comboComponent = comboComponentViewer.getTableCombo();
		FormData dataComboFind = new FormData();
		dataComboFind.top = new FormAttachment(groupOutput, 10);
		dataComboFind.left = new FormAttachment(labelEnvironments, 25);
		dataComboFind.width = 375;
		comboComponent.setLayoutData(dataComboFind);
		comboComponent.setEnabled(false);
		addCompOpenEditorMenu();
		
		comboComponent.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					if (!(comboComponent.getText().equals(selectedComponent))) {
						EnvironmentalQueryBean previousComponent = currentComponent;
						getSite().getShell().setCursor(
								getSite().getShell().getDisplay()
										.getSystemCursor(SWT.CURSOR_WAIT));
						if (dependencyTreeViewer.getTree() != null
								&& !comboComponent.getTable().getSelection()[0]
										.getData().equals(previousComponent)) {
							dependencyTreeViewer.setInput(null);
							dependencyTreeViewer.refresh();
							sectionDependencyResults.setEnabled(false);
						}
						currentComponent = (EnvironmentalQueryBean) comboComponent
								.getTable().getSelection()[0].getData();
						if (!currentComponent.equals(previousComponent)) {
							buttonExport.setEnabled(false);
							buttonCheck.setEnabled(true);
							chkboxDirectDepsOnly.setEnabled(isDirectDepsEnabled());
						}
					}

				} finally {
					getSite().getShell().setCursor(null);
				}
			}
		});
		
		chkboxDirectDepsOnly = safrGuiToolkit.createButton(compositeDependencyCriteria,
				SWT.CHECK, "Show &direct dependencies only");
		FormData dataButtonDirectDependency = new FormData();
		dataButtonDirectDependency.top = new FormAttachment(comboComponent, 10);
		dataButtonDirectDependency.left = new FormAttachment(labelEnvironments, 25);
		//dataButtonDirectDependency.width = 100;
		chkboxDirectDepsOnly.setLayoutData(dataButtonDirectDependency);
		chkboxDirectDepsOnly.setEnabled(false);
		chkboxDirectDepsOnly.setSelection(false);

		buttonCheck = safrGuiToolkit.createButton(compositeDependencyCriteria,
				SWT.PUSH, "Chec&k");
		FormData dataButtonFindDependency = new FormData();
		dataButtonFindDependency.top = new FormAttachment(comboComponent, 10);
		dataButtonFindDependency.right = new FormAttachment(comboComponent, 380);
		dataButtonFindDependency.width = 100;
		buttonCheck.setLayoutData(dataButtonFindDependency);
		buttonCheck.setEnabled(false);
		buttonCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkDependencies();
			}
		});

		sectionExport = safrGuiToolkit.createSection(
				compositeDependencyCriteria, Section.TITLE_BAR, "Export");
		FormData dataSectionExport = new FormData();
		dataSectionExport.top = new FormAttachment(buttonCheck, 20);
		dataSectionExport.left = new FormAttachment(0, 5);
		dataSectionExport.right = new FormAttachment(100, -5);
		sectionExport.setLayoutData(dataSectionExport);
		sectionExport.setEnabled(false);

		Composite compositeExport = safrGuiToolkit.createComposite(
				sectionExport, SWT.NONE);
		compositeExport.setLayout(new FormLayout());
		FormData dataCompositeExport = new FormData();
		dataCompositeExport.top = new FormAttachment(0, 0);
		dataCompositeExport.left = new FormAttachment(0, 0);
		dataCompositeExport.right = new FormAttachment(0, 0);
		compositeExport.setLayoutData(dataCompositeExport);
		sectionExport.setClient(compositeExport);

		Group groupExport = safrGuiToolkit.createGroup(compositeExport,
				SWT.BORDER_SOLID, "");
		groupExport.setLayout(new FormLayout());

		FormData dataGroupExport = new FormData();
		dataGroupExport.left = new FormAttachment(0, 5);
		dataGroupExport.top = new FormAttachment(0, 5);
		dataGroupExport.right = new FormAttachment(100, -10);
		groupExport.setLayoutData(dataGroupExport);

		radioExportAsReport = safrGuiToolkit.createRadioButton(groupExport,
				"Export as Re&port");
		FormData dataRadioExportReport = new FormData();
		dataRadioExportReport.top = new FormAttachment(0, 10);
		dataRadioExportReport.left = new FormAttachment(0, 10);
		radioExportAsReport.setLayoutData(dataRadioExportReport);
		radioExportAsReport.setSelection(true);

		radioExportAsReport.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				textLocation.setEnabled(false);
				buttonLocation.setEnabled(false);

			}
		});
		radioExportAsReport.setEnabled(false);

		radioExportAsCSV = safrGuiToolkit.createRadioButton(groupExport,
				"Export as C&SV file");
		FormData dataRadioExportCSV = new FormData();
		dataRadioExportCSV.top = new FormAttachment(radioExportAsReport, 10);
		dataRadioExportCSV.left = new FormAttachment(0, 10);
		radioExportAsCSV.setLayoutData(dataRadioExportCSV);

		radioExportAsCSV.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				textLocation.setEnabled(true);
				buttonLocation.setEnabled(true);

			}

		});
		radioExportAsCSV.setEnabled(false);

		buttonLocation = safrGuiToolkit.createButton(groupExport, SWT.NONE,
				"&Browse...");
		FormData dataButtonLocation = new FormData();
		// dataButtonLocation.left = new FormAttachment(textLocation, 5);
		dataButtonLocation.right = new FormAttachment(100, -5);
		dataButtonLocation.top = new FormAttachment(radioExportAsCSV, 8);
		buttonLocation.setLayoutData(dataButtonLocation);
		buttonLocation.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				dialogLocation = new DirectoryDialog(getSite().getShell());
				dialogLocation.setFilterPath(textLocation.getText());

				String dialogOpen = dialogLocation.open();
				if (dialogOpen != null) {
					textLocation.setText(dialogOpen);
				}
			}

		});
		buttonLocation.setEnabled(false);

		textLocation = safrGuiToolkit.createTextBox(groupExport, SWT.NONE);
		FormData dataLocation = new FormData();
		dataLocation.left = new FormAttachment(0, 10);
		dataLocation.top = new FormAttachment(radioExportAsCSV, 10);
		dataLocation.bottom = new FormAttachment(100, -5);
		dataLocation.right = new FormAttachment(buttonLocation, -5);
		dataLocation.width = 400;
		textLocation.setLayoutData(dataLocation);
		
	      // check for dep check path
        Preferences preferences = SAFRPreferences.getSAFRPreferences(); 
        String depPath = preferences.get(UserPreferencesNodes.DEP_CHECK_PATH,"");
        if (depPath==null || depPath.equals("")) { 
            textLocation.setText(ProfileLocation.getProfileLocation().getLocalProfile() + "csv");
        }
        else {
            textLocation.setText(depPath);            
        }
		textLocation.setEnabled(false);

		buttonExport = safrGuiToolkit.createButton(compositeExport, SWT.PUSH,
				"E&xport");
		FormData dataButtonExport = new FormData();
		dataButtonExport.top = new FormAttachment(groupExport, 10);
		dataButtonExport.right = new FormAttachment(100, -10);
		dataButtonExport.width = 100;
		buttonExport.setLayoutData(dataButtonExport);
		buttonExport.setEnabled(false);
		buttonExport.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (radioExportAsReport.getSelection()) {
					exportPDF();
				} else {
					exportCSV();
				}
			}

		});

	}

    protected void checkDependencies() {
        try {
            ApplicationMediator.getAppMediator().waitCursor();
            sectionDependencyResults.setEnabled(true);
            nodeList.clear();
            nodeMap.clear();
            dpChecker = new DependencyChecker(currentEnvID,
                    currentEnvName, currentComponent.getId(),
                    (ComponentType) componentType, currentComponent
                            .getName() == null ? "" : currentComponent
                            .getName(), chkboxDirectDepsOnly
                            .getSelection());
            try {
                DependencyCheckerNode dpCheckerNode = dpChecker.getDependency();
                dependencyTreeViewer.setInput(dpCheckerNode);
                dependencyTreeViewer.refresh();
                if (dependencyTreeViewer.getTree().getItems().length != 0) {
                    sectionExport.setEnabled(true);
                    radioExportAsCSV.setEnabled(true);
                    radioExportAsReport.setEnabled(true);
                    buttonExport.setEnabled(true);
                }
            } catch (DAOException e1) {
                UIUtilities.handleWEExceptions(e1,"Unexpected error occurred while getting the dependencies.",null);
            }

        } finally {
            ApplicationMediator.getAppMediator().normalCursor();
        }
    }
	
    private void addCompOpenEditorMenu()
    {
        Text text = comboComponent.getTextControl();
        Menu menu = text.getMenu();
        compOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        compOpenEditorItem.setText("Open Editor");
        compOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
            	EnvironmentalQueryBean bean = (EnvironmentalQueryBean)((StructuredSelection) comboComponentViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), componentType);                        
                }                
            }
        });
        
        comboComponent.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                	if (currentEnvID != UIUtilities.getCurrentEnvironmentID()) {
                        compOpenEditorItem.setEnabled(false);                                            		
                	}
                	else {
	                	EnvironmentalQueryBean bean = (EnvironmentalQueryBean)((StructuredSelection) comboComponentViewer
	                            .getSelection()).getFirstElement();
	                    if (bean != null) {   
                            compOpenEditorItem.setEnabled(true);                            
	                    }
	                    else {
	                        compOpenEditorItem.setEnabled(false);
	                    }
                	}
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }       
	
	private void exportPDF() {
		try {
			getSite().getShell().setCursor(
					getSite().getShell().getDisplay().getSystemCursor(
							SWT.CURSOR_WAIT));

			StringBuilder tree = new StringBuilder("");
			for (int i = 0; i < dependencyTreeViewer.getTree().getItemCount(); i++) {
				TreeItem item = dependencyTreeViewer.getTree().getItem(i);
				// The following map indicates whether a vertical
				// line
				// char is required at each indentation
				// level (whether there is a branch
				// continuation at each level).
				// Keyed by indentation level number.
				Map<Integer, Boolean> vLinesRequired = new HashMap<Integer, Boolean>();

				printTree(tree, item, i == dependencyTreeViewer.getTree()
						.getItemCount() - 1, vLinesRequired);
			}
			str = tree.toString();

			ReportEditorInput input = new ReportEditorInput(str,
					ReportType.DependencyChecker, dpChecker);

			// Check for all open editors if same report is already
			// opened in PDF which is requested, if its opened close that
			// and open that again.
			IEditorReference[] openEditors = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getEditorReferences();

			IEditorPart editor = null;
			for (int i = 0; i < openEditors.length; i++) {
				IEditorReference editorPart = openEditors[i];
				editor = editorPart.getEditor(false);
				if (input.equals(editor.getEditorInput())) {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().closeEditor(editor, false);
					break;
				}
			}

		} finally {
			getSite().getShell().setCursor(null);

		}
	}

	private void exportCSV() {
		if (textLocation.getText().equals("")
				|| !isFilePathCorrect(textLocation.getText())) {
			UIUtilities.handleWEExceptions(new SAFRException(
					"Please specify a valid export location."), null,
					"Path not valid");
			return;
		}
        Preferences preferences = SAFRPreferences.getSAFRPreferences(); 
        preferences.put(UserPreferencesNodes.DEP_CHECK_PATH, textLocation.getText());
        try {
            preferences.flush();
        } catch (BackingStoreException e1) {
            logger.log(Level.SEVERE, "Failed to save preferences", e1);
            throw new SAFRFatalException(e1);                    
        }		
		try {
			getSite().getShell().setCursor(
					getSite().getShell().getDisplay().getSystemCursor(
							SWT.CURSOR_WAIT));
			String exportPath = textLocation.getText() + File.separatorChar
					+ componentType.toString() + "-"
					+ dependencyTreeViewer.getTree().getItem(0).getText()
					+ ".csv";

			List<CSVRow> rows = new ArrayList<CSVRow>();
			loadCSVRows(rows);

			// create the output stream
			try {
				OutputStream csvStream = new FileOutputStream(exportPath);
				csvStream.write(("Seq,Level,Comp type,Usage,ID,Name" + SAFRUtilities.LINEBREAK)
						.getBytes());
				for (CSVRow csvRow : rows) {
					String str = csvRow.toString() + SAFRUtilities.LINEBREAK;
					csvStream.write(str.getBytes());
				}
				csvStream.flush();
				csvStream.close();
			} catch (IOException e) {
				UIUtilities.handleWEExceptions(e, null, "Error writing to file");
				return;
			}
			MessageDialog.openInformation(getSite().getShell(),
					"CSV file exported",
					"The CSV file is exported successfully to this location:" + SAFRUtilities.LINEBREAK
							+ exportPath);
		} finally {
			getSite().getShell().setCursor(null);
		}
	}

	private Boolean isFilePathCorrect(String filePath) {
		File file = new File(filePath);
		if (file.exists() || (!file.exists() && file.mkdirs())) {
			return true;
		} else {
			return false;
		}
	}

	private void loadCSVRows(List<CSVRow> rows) {
		// TODO: ask user for a location and create a CSV file from the current
		// tree state.
		int level = 0;
		csvSequence = 0;
		// create the root row
		CSVRow row = new CSVRow(++csvSequence, level, dpChecker
				.getComponentType().getLabel(), "", dpChecker.getComponentId(),
				dpChecker.getComponentName());
		rows.add(row); // parent row
		level++; // increment the level as all the items after this will be
		// below this level.
		TreeItem root = dependencyTreeViewer.getTree().getItem(0);
		// proceed further if root is expanded
		if (root.getExpanded()) {
			for (int i = 0; i < root.getItemCount(); i++) {
				TreeItem item = root.getItem(i);
				addCSVRow(item, level, rows);
			}
		}
	}

	private void addCSVRow(TreeItem item, int currLevel, List<CSVRow> rows) {
		DependencyCheckerNode node = (DependencyCheckerNode) item.getData();
		if (node.isComponent()) {
			// component found, add to list
			DependencyCheckerNode pnode = node.getParentNode();
			ComponentType type = null;
			DependencyUsageType utype = null;
			while (type == null) {
				// iterate over all the parent nodes until a component type is
				// found
				type = getTypeFromLabel(pnode.getName());
				if (utype == null) {
					utype = getUsageTypeFromLabel(pnode.getLabel());
				}
				pnode = pnode.getParentNode();
			}
			rows.add(new CSVRow(++csvSequence, currLevel, type.getLabel(),
					utype == null ? "" : utype.getLabel(), node.getId(), node
							.getName()));
		}
		// Drill down deeper
		if (item.getExpanded()) {
			if (node.isComponent()) {
				currLevel++; // expanded component means another level.
			}
			for (int i = 0; i < item.getItemCount(); i++) {
				TreeItem eItem = item.getItem(i);
				addCSVRow(eItem, currLevel, rows);
			}
		}
	}

	private DependencyUsageType getUsageTypeFromLabel(String label) {
		for (DependencyUsageType type : DependencyUsageType.values()) {
			if (type.getLabel().equals(label)) {
				return type;
			}
		}
		return null;
	}

	private class CSVRow {
		private int sequence;
		private int level;
		private String type;
		private String usageType;
		private int id;
		private String name;

		public CSVRow(int sequence, int level, String type, String usageType,
				int id, String name) {
			super();
			this.sequence = sequence;
			this.level = level;
			this.type = type;
			this.usageType = usageType;
			this.id = id;
			this.name = name;
		}

		@Override
		public String toString() {
			return sequence + "," + level + "," + type + "," + usageType + ","
					+ id + "," + name;
		}
	}

	private void populateComboShowFieldsFromLR() {
		Integer counter = 0;
		List<LogicalRecordQueryBean> lRQueryBeanList;
		try {
			lRQueryBeanList = SAFRQuery.queryAllLogicalRecords(currentEnvID,
					SortType.SORT_BY_NAME);

			for (LogicalRecordQueryBean logicalRecordQueryBean : lRQueryBeanList) {
				comboShowFieldsFromLR.add(UIUtilities.getComboString(
						logicalRecordQueryBean.getName(),
						logicalRecordQueryBean.getId()));
				comboShowFieldsFromLR.setData(Integer.toString(counter++),
						logicalRecordQueryBean);
			}
		} catch (DAOException e) {
			UIUtilities.handleWEExceptions(e,
			    "Unexpected database error occured while retrieving metadata list of selected component.",
			    UIUtilities.titleStringDbException);
		}
	}

	private void populateEnvironment(TableCombo comboEnvironment, User user) {
		Integer counter = 0;
		try {
			envList = SAFRQuery.queryEnvironmentsForLoggedInUser(
					SortType.SORT_BY_NAME, false);

			comboEnvironmentViewer.setInput(envList);
			comboEnvironmentViewer.refresh();
			for (EnvironmentQueryBean environment : envList) {
				comboEnvironment.setData(Integer.toString(counter++),environment);
			}
		} catch (DAOException e) {
			UIUtilities.handleWEExceptions(e,
			    "Unexpected database error occured while retrieving metadata list of selected component.",
			    UIUtilities.titleStringDbException);
		}

	}

	public void populateComponentList() {
		comboComponent.getTable().removeAll();
		comboComponentViewer.setSorter(null);

		try {
			comboComponentViewer.setContentProvider(new ArrayContentProvider());
			MainTableLabelProvider labelProvider = new MainTableLabelProvider((ComponentType) componentType) {
				@Override
				public Color getForeground(Object element, int columnIndex) {
					return null;
				}

				public String getColumnText(Object element, int columnIndex) {

					switch (columnIndex) {
					case 2:
						NumericIdQueryBean bean = (NumericIdQueryBean) element;
						return UIUtilities.getComboString(bean.getName(), bean
								.getId());
					default:
						return super.getColumnText(element, columnIndex);
					}
				}
			};
			labelProvider.setInput();
			comboComponentViewer.setLabelProvider(labelProvider);
			if (componentType == ComponentType.LogicalFile) {
				List<LogicalFileQueryBean> logicalFileList = SAFRQuery.queryAllLogicalFiles(
				    currentEnvID,SortType.SORT_BY_NAME);
				comboComponentViewer.setInput(logicalFileList);
				comboComponentViewer.setSorter(new LogicalFileTableSorter(1,
						SWT.UP));

			} else if (componentType == ComponentType.LookupPath) {
				List<LookupQueryBean> lookuPathList = SAFRQuery.queryAllLookups(
				    currentEnvID, SortType.SORT_BY_NAME);
                comboComponentViewer.setInput(lookuPathList);
                comboComponentViewer.setSorter(new LookupTableSorter(1, SWT.UP));

			} else if (componentType == ComponentType.LogicalRecord) {
				List<LogicalRecordQueryBean> logicalRecordList = SAFRQuery.queryAllLogicalRecords(
				    currentEnvID,SortType.SORT_BY_NAME);
				comboComponentViewer.setInput(logicalRecordList);
				comboComponentViewer.setSorter(new LogicalRecordTableSorter(1,SWT.UP));
			} else if (componentType == ComponentType.PhysicalFile) {
				List<PhysicalFileQueryBean> physicalFileList = SAFRQuery.queryAllPhysicalFiles(
				    currentEnvID,SortType.SORT_BY_NAME);
				comboComponentViewer.setInput(physicalFileList);
				comboComponentViewer.setSorter(new PhysicalFileTableSorter(1,SWT.UP));
			} else if (componentType == ComponentType.UserExitRoutine) {
				List<UserExitRoutineQueryBean> userExitRoutineList = SAFRQuery
						.queryAllUserExitRoutines(currentEnvID,
								SortType.SORT_BY_NAME);
				comboComponentViewer.setInput(userExitRoutineList);
				comboComponentViewer.setSorter(new UserExitRoutineTableSorter(1, SWT.UP));
			} else if (componentType == ComponentType.View) {
				List<ViewQueryBean> viewList = SAFRQuery.queryAllViews(
				    currentEnvID, SortType.SORT_BY_NAME);
				comboComponentViewer.setInput(viewList);
				comboComponentViewer.setSorter(new ViewTableSorter(1, SWT.UP));
			} else if (componentType == ComponentType.ViewFolder) {
                List<ViewFolderQueryBean> vfList = SAFRQuery.queryAllViewFolders(
                    currentEnvID, SortType.SORT_BY_NAME);
                comboComponentViewer.setInput(vfList);
                comboComponentViewer.setSorter(new ViewFolderTableSorter(1, SWT.UP));
			} else if (componentType == ComponentType.LogicalRecordField) {
				List<LogicalRecordFieldQueryBean> lrFieldList = null;
				if (radioShowAllFields.getSelection()) {
					lrFieldList = SAFRQuery.queryAllLogicalRecordFields(
							currentEnvID, SortType.SORT_BY_NAME);
				} else {
					if (comboShowFieldsFromLR.getSelectionIndex() != -1) {
						lrFieldList = SAFRQuery.queryAllLogicalRecordFields(
								currentEnvID, currentLogicalRecord.getId(),
								SortType.SORT_BY_NAME);
					}

				}
				comboComponentViewer.setInput(lrFieldList);
				comboComponentViewer.setSorter(new LogicalRecordFieldTableSorter(1, SWT.UP));
			}

			int iCounter;
			for (iCounter = 0; iCounter < 2; iCounter++) {
				if (prvsListner[iCounter] != null) {
					comboComponent.getTable().getColumn(iCounter)
							.removeSelectionListener(prvsListner[iCounter]);
				}
				ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(
						iCounter, comboComponentViewer,
						(ComponentType) componentType);
				prvsListner[iCounter] = colListener;
				comboComponent.getTable().getColumn(iCounter)
						.addSelectionListener(colListener);

			}
			comboComponentViewer.refresh();
		} catch (DAOException e) {
			UIUtilities.handleWEExceptions(e,
			    "Unexpected database error occured while retrieving metadata list of selected component.",
			    UIUtilities.titleStringDbException);
		}

	}

	@Override
	public void doRefreshControls() throws SAFRException {
		// TODO Auto-generated method stub

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

	@Override
	public String getModelName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void refreshModel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus() {
		comboEnvironment.setFocus();

	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
		// TODO Auto-generated method stub

	}

	@Override
	public void validate() throws DAOException, SAFRException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getComponentNameForSaveAs() {
		return null;
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		return null;
	}

	private void printTree(StringBuilder root, TreeItem item, boolean lastItem,
			Map<Integer, Boolean> vLinesRequired) {
		if (item.getParentItem() == null) {
			// for root item, no need to print the connectors.
			root.append(item.getText() + SAFRUtilities.LINEBREAK);
			indent = 0;
		} else {
			indent += increment;
			vLinesRequired.put(indent, lastItem ? false : true);
			root.append(getTreeConnector(false, false, vLinesRequired) + SAFRUtilities.LINEBREAK);
			root.append(getTreeConnector(true, lastItem, vLinesRequired)
					+ item.getText() + SAFRUtilities.LINEBREAK);
		}
		if (item.getExpanded()) {
			// print the expanded tree too
			for (int i = 0; i < item.getItemCount(); i++) {
				TreeItem citem = item.getItem(i);
				printTree(root, citem, i == item.getItemCount() - 1,
						vLinesRequired);
			}
		}
		if (indent > 0) {
			vLinesRequired.remove(indent);
			indent -= increment;
		}
	}

	private String getTreeConnector(boolean forItemText, boolean lastItem,
			Map<Integer, Boolean> vLinesRequired) {
		// String str = genChar(indent, " ");
		String str = genLine(indent, vLinesRequired);
		if (forItemText) {
			char s = lastItem ? lastLine : hLine;
			str += s;
		} else {
			str += vLine;
		}
		return str;
	}

	private String genLine(int indent, Map<Integer, Boolean> vLinesRequired) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			// initialize the current line to blanks
			buffer.append(' ');
		}
		int key = increment;
		while (key < indent) {
			// use a vertical line character at each indent levels of the
			// current line if needed (if there is a branch there)
			if (vLinesRequired.get(key)) {
				buffer.setCharAt(key, vLine);
			}
			key += increment;
		}
		return buffer.toString();
	}

	public String getStr() {
		return str;
	}

	private ComponentType getTypeFromLabel(String label) {
		for (ComponentType type : ComponentType.values()) {
			if (type.getLabel().equals(label)) {
				return type;
			}
		}
		return null;
	}
	
	private boolean isDirectDepsTicked() {
		if (componentType == ComponentType.UserExitRoutine || 
		    componentType == ComponentType.LogicalRecordField || 
		    componentType == ComponentType.PhysicalFile) {
			// Cannot choose All Deps so must tick the check box
			return true;
		} else {
			// for LF,LR,LK,VW user can choose All Deps
			// or Direct Deps, so let the user tick it
			return false;
		}
	}
	
	private boolean isDirectDepsEnabled() {
		if (componentType == ComponentType.UserExitRoutine || 
		    componentType == ComponentType.LogicalRecordField || 
		    componentType == ComponentType.PhysicalFile) {
			// Cannot choose All Deps, so disable the check box
			return false;
		} else {
			// for LF,LR,LK,VW user can choose All Deps
			// or Direct Deps, so enable the check box
			return true;
		}
	}


    public void setComponent(EnvironmentalQueryBean component) {
        ApplicationMediator.getAppMediator().waitCursor();
        setEnvironment(component.getEnvironmentId());
        setType(component.getComponentType());        
        if (component instanceof LogicalRecordFieldQueryBean) {
            groupOutput.setVisible(true);
            radioShowAllFields.setSelection(false);
            radioShowFieldsFromLR.setSelection(true);
            setSelectedLR(component);
        }
        else {
            groupOutput.setVisible(false);
        }
        setComponentSelection(component);
        chkboxDirectDepsOnly.setEnabled(isDirectDepsEnabled());
        chkboxDirectDepsOnly.setSelection(isDirectDepsTicked());
        buttonCheck.setEnabled(true);
        buttonExport.setEnabled(false);
        sectionExport.setEnabled(false);
        radioExportAsCSV.setEnabled(false);
        radioExportAsReport.setEnabled(false);        
        dependencyTreeViewer.setInput(null);
        dependencyTreeViewer.refresh();
        sectionDependencyResults.setEnabled(false);
        ApplicationMediator.getAppMediator().normalCursor();        
    }

    protected void setSelectedLR(EnvironmentalQueryBean component) {
        Integer lrId = ((LogicalRecordFieldQueryBean)component).getLrId();
        comboShowFieldsFromLR.removeAll();
        currentLogicalRecord = new LogicalRecordQueryBean(
            component.getEnvironmentId(), 
            lrId, 
            null, null, null, null, null, null, null, null, null, null, null, null);
        populateComboShowFieldsFromLR();
        comboShowFieldsFromLR.setEnabled(true);
        int index = -1;
        for (int i=0 ; i<comboShowFieldsFromLR.getItemCount() ; i++) {
            LogicalRecordQueryBean bean = (LogicalRecordQueryBean)
                comboShowFieldsFromLR.getData(String.valueOf(i));
            if (bean.getId().equals(lrId) ) {
                index = i;
                break;
            }            
        }
        comboShowFieldsFromLR.select(index);
    }

    private void setEnvironment(Integer environmentId) {
        currentEnvID = environmentId;
        currentEnvName = "";
        for (EnvironmentQueryBean bean : envList) {
            if (bean.getId().equals(environmentId)) {
                comboEnvironmentViewer.setSelection(new StructuredSelection(bean));
                break;
            }
        }
    }
    
    private void setType(ComponentType type) {
        componentType = type;
        int index = -1;
        for (int i=0 ; i<comboComponentType.getItemCount() ; i++) {
            if (comboComponentType.getItem(i).equals(type.getLabel())) {
                index = i;
                break;
            }            
        }
        if (index != -1) {
            comboComponentType.select(index);                    
        }
        comboComponent.getTable().removeAll();
        comboComponent.select(-1);
        comboComponent.setEnabled(true);
        populateComponentList();        
    }

    @SuppressWarnings("unchecked")
    protected void setComponentSelection(EnvironmentalQueryBean component) {
        List<EnvironmentalQueryBean> beans = (List<EnvironmentalQueryBean>) 
            comboComponentViewer.getInput();        
        for (EnvironmentalQueryBean bean : beans) {
            if (component.getEnvironmentId().equals(bean.getEnvironmentId()) && 
                component.getId().equals(bean.getId())) {
                currentComponent = bean;
                comboComponentViewer.setSelection(new StructuredSelection(bean));
                break;
            }
        }
    }
    
}
