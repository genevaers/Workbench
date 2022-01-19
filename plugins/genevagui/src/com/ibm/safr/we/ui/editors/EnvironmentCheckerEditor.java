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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
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
import com.ibm.safr.we.model.query.EnvComponentQueryBean;
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
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.preferences.SAFRPreferences;
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
import com.ibm.safr.we.ui.views.metadatatable.ViewTableSorter;
import com.ibm.safr.we.utilities.ProfileLocation;

public class EnvironmentCheckerEditor extends SAFREditorPart {
	
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.EnvironmentCheckerEditor");
	
	public enum EnvCheckerColumn {
		ENV_ID(0, "Env ID", 80), 
        ENAME(1, "Env Name", 150), 
		CNAME(2, "Comp Name", 300), 
		CREATE_DATE(3, "Created Date", 110), 
		CREATE_BY(4, "Created By", 100), 
		MODIFY_DATE(5, "Last Updated", 110), 
		MODIFY_BY(6, "Updated By", 100);
		
		int column;
		String title;
		int width;
		
		EnvCheckerColumn(int column, String title, int width) {
			this.column = column;
			this.title = title;
			this.width = width;
		}
		
		static EnvCheckerColumn getByColumn(int column) {
			switch (column) {
			case 0: return ENV_ID;
			case 1: return ENAME;
            case 2: return CNAME;
			case 3: return CREATE_DATE;
			case 4: return CREATE_BY;
			case 5: return MODIFY_DATE;
			case 6: return MODIFY_BY;
			default:
			}
			return null;
			
		}
	
		public int getColumn() {
			return column;
		}

		public String getTitle() {
			return title;
		}

		public int getWidth() {
			return width;
		}

	}
	
	public class EnvironmentTableLabelProvider implements ITableLabelProvider {

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			
			EnvComponentQueryBean bean = (EnvComponentQueryBean)element;
			switch (EnvCheckerColumn.getByColumn(columnIndex)) {
			case ENV_ID: 
				return bean.getIdLabel();
			case ENAME:
				return bean.getEnameLabel();			
            case CNAME:
                return bean.getNameLabel();             
			case CREATE_BY:
				return bean.getCreateByLabel();
			case CREATE_DATE:
				return bean.getCreateTimeLabel();
			case MODIFY_BY:
				return bean.getModifyByLabel();
			case MODIFY_DATE:
				return bean.getModifyTimeLabel();
			default:
			}
			return null;
		}

	}

	public class EnvironmentTableContentProvider implements
			IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			return ((List<EnvComponentQueryBean>) inputElement).toArray();
		}

	}

	public final static String ID = "SAFRWE.EnvironmentCheckerEditor";

	private ScrolledForm form;
	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;
	private String headerNote = "Use this utility to get a list of environments containing a component.";
	
	// criteria
	private Section sectionEnvironmentCriteria;
	private TableCombo comboEnvironment;
	private String selectedEnvironment = "";
	protected EnvironmentQueryBean currentEnvironment;
	protected Integer currentEnvID;
	private Combo comboComponentType;
	private TableCombo comboComponent;
	private TableComboViewer comboEnvironmentViewer;
	private TableComboViewer comboComponentViewer;
	private Button radioShowAllFields;
	private Button radioShowFieldsFromLR;
	private Combo comboShowFieldsFromLR;
	protected ComponentType componentType;
	private Group groupOutput;
	private Button buttonCheck;
	private Button buttonExport;
	protected int selectedComponentType = -1;
	protected int selectedLogicalRecord = -1;
	protected LogicalRecordQueryBean currentLogicalRecord;
	protected String selectedComponent = "";
	protected EnvironmentalQueryBean currentComponent;
	private ColumnSelectionListenerForTableCombo[] prvsListner = new ColumnSelectionListenerForTableCombo[2];
	private Text textLocation;
	private Button buttonLocation;
	protected DirectoryDialog dialogLocation;
	private Section sectionExport;
    private MenuItem compOpenEditorItem = null;
		
	// results
	private Section sectionEnvironmentResults;
	private TableViewer tableViewerResults;
	private Table tableResults;
	private List<EnvComponentQueryBean> list;

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FormLayout());
		form.setText("Environment Checker");

		Label labelHeaderNote = safrGuiToolkit.createLabel(form.getBody(),
				SWT.NONE, headerNote);
		labelHeaderNote.setForeground(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_GRAY));
		FormData dataLabelHeaderNote = new FormData();
		dataLabelHeaderNote.top = new FormAttachment(0, 0);
		dataLabelHeaderNote.left = new FormAttachment(0, 5);
		labelHeaderNote.setLayoutData(dataLabelHeaderNote);

		createSectionEnvironmentCriteria(form.getBody());
		createSectionEnvironmentResult(form.getBody());
		form.reflow(true);

	}

	private void createSectionEnvironmentCriteria(Composite body) {
		sectionEnvironmentCriteria = safrGuiToolkit.createSection(body,
				Section.TITLE_BAR, "Environment Criteria");
		FormData dataSectionEnvironmentCriteria = new FormData();
		dataSectionEnvironmentCriteria.top = new FormAttachment(0, 20);
		dataSectionEnvironmentCriteria.left = new FormAttachment(0, 5);
		dataSectionEnvironmentCriteria.right = new FormAttachment(35, -5);
		sectionEnvironmentCriteria.setLayoutData(dataSectionEnvironmentCriteria);

		Composite compositeEnvironmentCriteria = safrGuiToolkit.createComposite(
				sectionEnvironmentCriteria, SWT.NONE);
		compositeEnvironmentCriteria.setLayout(new FormLayout());
		FormData dataCompositeEnvironmentCriteria = new FormData();
		dataCompositeEnvironmentCriteria.top = new FormAttachment(0, 0);
		dataCompositeEnvironmentCriteria.left = new FormAttachment(0, 0);
		dataCompositeEnvironmentCriteria.right = new FormAttachment(0, 0);
		compositeEnvironmentCriteria
				.setLayoutData(dataCompositeEnvironmentCriteria);
		sectionEnvironmentCriteria.setClient(compositeEnvironmentCriteria);

		Label labelEnvironments = safrGuiToolkit.createLabel(
				compositeEnvironmentCriteria, SWT.NONE, "&Environment: ");
		FormData dataLabelEnvironments = new FormData();
		dataLabelEnvironments.top = new FormAttachment(0, 10);
		dataLabelEnvironments.left = new FormAttachment(0, 5);
		labelEnvironments.setLayoutData(dataLabelEnvironments);

		comboEnvironmentViewer = safrGuiToolkit.createTableComboForComponents(
				compositeEnvironmentCriteria, ComponentType.Environment);

		comboEnvironment = comboEnvironmentViewer.getTableCombo();
		FormData dataComboEnvironments = new FormData();
		dataComboEnvironments.top = new FormAttachment(0, 10);
		dataComboEnvironments.left = new FormAttachment(labelEnvironments, 40);
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
					if (!(comboEnvironment.getText().equals(selectedEnvironment))) {
						currentEnvironment = (EnvironmentQueryBean) comboEnvironment
								.getTable().getSelection()[0].getData();
						if (currentEnvironment != null) {
							currentEnvID = currentEnvironment.getId();
							comboShowFieldsFromLR.removeAll();
							comboComponent.getTable().removeAll();
							comboComponent.select(-1);
							comboComponent.redraw();
							enableExport(false);
							buttonCheck.setEnabled(false);
		                    sectionEnvironmentResults.setText("Component");                 							
						}
						getSite().getShell().setCursor(getSite().getShell().getDisplay()
							.getSystemCursor(SWT.CURSOR_WAIT));
						selectedEnvironment = comboEnvironment.getText();
						if (componentType != null) {
							if (componentType.equals(ComponentType.LogicalRecordField)) {
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
					tableViewerResults.setInput(null);
					tableViewerResults.refresh();
					enableExport(false);
					comboComponent.getTable().removeAll();
					comboComponent.select(-1);
					comboComponent.redraw();
                    sectionEnvironmentResults.setText("Component");                 					
					
				}
			}
		});
		selectedEnvironment = comboEnvironment.getText();
		currentEnvironment = (EnvironmentQueryBean) comboEnvironment.getTable()
				.getSelection()[0].getData();
		currentEnvID = currentEnvironment.getId();
		
		Label labelComponentType = safrGuiToolkit.createLabel(
				compositeEnvironmentCriteria, SWT.NONE, "Component &Type:");
		FormData dataLabelComponentType = new FormData();
		dataLabelComponentType.width = SWT.DEFAULT;
		dataLabelComponentType.top = new FormAttachment(comboEnvironment, 10);
		dataLabelComponentType.left = new FormAttachment(0, 5);
		labelComponentType.setLayoutData(dataLabelComponentType);

		comboComponentType = safrGuiToolkit.createComboBox(
				compositeEnvironmentCriteria, SWT.READ_ONLY, "");
		FormData dataComboComponentType = new FormData();
		dataComboComponentType.left = new FormAttachment(labelEnvironments, 40);
		dataComboComponentType.top = new FormAttachment(comboEnvironment, 10);
		dataComboComponentType.width = 350;
		comboComponentType.setLayoutData(dataComboComponentType);

		int i = 0;
		comboComponentType.add("LR Field");
		comboComponentType.setData(String.valueOf(i++),
				ComponentType.LogicalRecordField);
		comboComponentType.add("Lookup Path");
		comboComponentType.setData(String.valueOf(i++),
				ComponentType.LookupPath);
		comboComponentType.add("Logical File");
		comboComponentType.setData(String.valueOf(i++),
				ComponentType.LogicalFile);
		comboComponentType.add("Logical Record");
		comboComponentType.setData(String.valueOf(i++),
				ComponentType.LogicalRecord);
		comboComponentType.add("Physical File");
		comboComponentType.setData(String.valueOf(i++),
				ComponentType.PhysicalFile);
		comboComponentType.add("User-Exit Routine");
		comboComponentType.setData(String.valueOf(i++),
				ComponentType.UserExitRoutine);
		comboComponentType.add("View");
		comboComponentType.setData(String.valueOf(i++), ComponentType.View);

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
						
						buttonCheck.setEnabled(false);						
						comboComponent.getTable().removeAll();
						comboComponent.select(-1);						
						comboComponent.setEnabled(true);
                        sectionEnvironmentResults.setText("Component");                 
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
					tableViewerResults.setInput(null);
					tableViewerResults.refresh();
					enableExport(false);
			        sectionEnvironmentResults.setText("Component");					
				}
			}
		});

		groupOutput = safrGuiToolkit.createGroup(compositeEnvironmentCriteria,
				SWT.BORDER_SOLID, "");
		groupOutput.setVisible(false);
		groupOutput.setLayout(new FormLayout());

		FormData dataGroupOutput = new FormData();
		dataGroupOutput.left = new FormAttachment(labelEnvironments, 40);
		dataGroupOutput.top = new FormAttachment(comboComponentType, 5);
		dataGroupOutput.width = 370;
		groupOutput.setLayoutData(dataGroupOutput);
		
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
				        sectionEnvironmentResults.setText("Component");						
						getSite().getShell().setCursor(
								getSite().getShell().getDisplay()
										.getSystemCursor(SWT.CURSOR_WAIT));
						populateComponentList();
						comboComponent.setEnabled(true);
						comboComponent.setFocus();
						buttonCheck.setEnabled(false);
						tableViewerResults.setInput(null);
						tableViewerResults.refresh();
						enableExport(false);
					}

				} finally {
					getSite().getShell().setCursor(null);
				}
			}
		});
		
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
				        sectionEnvironmentResults.setText("Component");						
						getSite().getShell().setCursor(
								getSite().getShell().getDisplay()
										.getSystemCursor(SWT.CURSOR_WAIT));

						if (comboShowFieldsFromLR.getSelectionIndex() != -1) {
							populateComponentList();
						} else {
							populateComboShowFieldsFromLR();
							comboShowFieldsFromLR.setFocus();
						}
						buttonCheck.setEnabled(false);
						tableViewerResults.setInput(null);
						tableViewerResults.refresh();
						enableExport(false);
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
			}

			public void focusLost(FocusEvent e) {
				try {
					if (comboShowFieldsFromLR.getSelectionIndex() != selectedLogicalRecord) {
						LogicalRecordQueryBean previousLR = currentLogicalRecord;
						if (!comboShowFieldsFromLR.getData(String.valueOf(comboShowFieldsFromLR
								.getSelectionIndex())).equals(previousLR)) {
							tableViewerResults.setInput(null);
							tableViewerResults.refresh();
							enableExport(false);
							comboComponent.getTable().removeAll();
							comboComponent.select(-1);
	                        sectionEnvironmentResults.setText("Component");                 							
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
						}

					}
				} finally {
					getSite().getShell().setCursor(null);
				}
			}
		});

		Label labelComponent = safrGuiToolkit.createLabel(
				compositeEnvironmentCriteria, SWT.NONE, "Compo&nent:");
		FormData dataLabelComponent = new FormData();
		dataLabelComponent.left = new FormAttachment(0, 5);
		dataLabelComponent.top = new FormAttachment(groupOutput, 10);
		labelComponent.setLayoutData(dataLabelComponent);

		comboComponentViewer = safrGuiToolkit
				.createTableComboForComponents(compositeEnvironmentCriteria);
		comboComponent = comboComponentViewer.getTableCombo();
		FormData dataComboFind = new FormData();
		dataComboFind.top = new FormAttachment(groupOutput, 10);
		dataComboFind.left = new FormAttachment(labelEnvironments, 40);
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
						if (!comboComponent.getTable().getSelection()[0]
										.getData().equals(previousComponent)) {
							tableViewerResults.setInput(null);
							tableViewerResults.refresh();
						}
				        sectionEnvironmentResults.setText("Component");						
						currentComponent = (EnvironmentalQueryBean) comboComponent
								.getTable().getSelection()[0].getData();
						if (!currentComponent.equals(previousComponent)) {
							enableExport(false);
							buttonCheck.setEnabled(true);
						}
					}

				} finally {
					getSite().getShell().setCursor(null);
				}
			}
		});
		
		buttonCheck = safrGuiToolkit.createButton(compositeEnvironmentCriteria,
				SWT.PUSH, "Chec&k");
		FormData dataButtonFindEnvironment = new FormData();
		dataButtonFindEnvironment.top = new FormAttachment(comboComponent, 10);
		dataButtonFindEnvironment.right = new FormAttachment(comboComponent, 380);
		dataButtonFindEnvironment.width = 100;
		buttonCheck.setLayoutData(dataButtonFindEnvironment);
		buttonCheck.setEnabled(false);
		buttonCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					getSite().getShell().setCursor(
							getSite().getShell().getDisplay().getSystemCursor(
									SWT.CURSOR_WAIT));
					// get data and fill in results
					try {
						list = SAFRQuery.queryComponentsInEnvironments(currentComponent.getId(), componentType);
						tableViewerResults.setInput(list);
						tableViewerResults.refresh();
						sectionEnvironmentResults.setText("Component [" + currentComponent.getId() + "]");
						if (tableViewerResults.getTable().getItems().length != 0) {
							enableExport(true);
						}						
						
					} catch (DAOException e1) {
						UIUtilities.handleWEExceptions(e1,
							"Unexpected error occurred while getting the environments.",null);
					}
				} finally {
					getSite().getShell().setCursor(null);

				}
			}
		});

		sectionExport = safrGuiToolkit.createSection(
				compositeEnvironmentCriteria, Section.TITLE_BAR, "Export");
		FormData dataSectionExport = new FormData();
		dataSectionExport.top = new FormAttachment(buttonCheck, 20);
		dataSectionExport.left = new FormAttachment(0, 5);
		dataSectionExport.right = new FormAttachment(100, -5);
		sectionExport.setLayoutData(dataSectionExport);

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

		buttonLocation = safrGuiToolkit.createButton(groupExport, SWT.NONE,
				"&Browse...");
		FormData dataButtonLocation = new FormData();
		dataButtonLocation.right = new FormAttachment(100, -5);
		dataButtonLocation.top = new FormAttachment(0, 8);
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

		textLocation = safrGuiToolkit.createTextBox(groupExport, SWT.NONE);
		FormData dataLocation = new FormData();
		dataLocation.left = new FormAttachment(0, 10);
		dataLocation.top = new FormAttachment(0, 10);
		dataLocation.bottom = new FormAttachment(100, -5);
		dataLocation.right = new FormAttachment(buttonLocation, -5);
		dataLocation.width = 400;
		textLocation.setLayoutData(dataLocation);
		
        Preferences preferences = SAFRPreferences.getSAFRPreferences(); 
        String envPath = preferences.get(UserPreferencesNodes.ENV_CHECK_PATH,"");
        if (envPath==null || envPath.equals("")) { 
            textLocation.setText(ProfileLocation.getProfileLocation().getLocalProfile() + "csv");
        }
        else {
            textLocation.setText(envPath);            
        }
		

		buttonExport = safrGuiToolkit.createButton(compositeExport, SWT.PUSH,
				"E&xport");
		FormData dataButtonExport = new FormData();
		dataButtonExport.top = new FormAttachment(groupExport, 10);
		dataButtonExport.right = new FormAttachment(100, -10);
		dataButtonExport.width = 100;
		buttonExport.setLayoutData(dataButtonExport);
		buttonExport.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				exportCSV();
			}

		});

		enableExport(false);
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
	
	private void enableExport(boolean enable) {
		sectionExport.setEnabled(enable);
		buttonLocation.setEnabled(enable);
		textLocation.setEnabled(enable);
		buttonExport.setEnabled(enable);
	}
	
	private void createSectionEnvironmentResult(Composite body) {

		sectionEnvironmentResults = safrGuiToolkit.createSection(body,
				Section.TITLE_BAR, "Component");
		FormData datasectionEnvironmentResults = new FormData();
		datasectionEnvironmentResults.top = new FormAttachment(0, 20);
		datasectionEnvironmentResults.left = new FormAttachment(35, 0);
		datasectionEnvironmentResults.right = new FormAttachment(100, -5);
		datasectionEnvironmentResults.bottom = new FormAttachment(100, -10);
		sectionEnvironmentResults.setLayoutData(datasectionEnvironmentResults);

		Composite compositeEnvironmentResults = safrGuiToolkit.createComposite(
				sectionEnvironmentResults, SWT.NONE);
		compositeEnvironmentResults.setLayout(new FormLayout());
		FormData dataCompositeEnvironmentResults = new FormData();
		dataCompositeEnvironmentResults.top = new FormAttachment(0, 0);
		dataCompositeEnvironmentResults.left = new FormAttachment(0, 0);
		dataCompositeEnvironmentResults.right = new FormAttachment(0, 0);
		dataCompositeEnvironmentResults.bottom = new FormAttachment(0, 0);
		compositeEnvironmentResults
				.setLayoutData(dataCompositeEnvironmentResults);
		sectionEnvironmentResults.setClient(compositeEnvironmentResults);
		
		tableViewerResults = safrGuiToolkit.createTableViewer(
			compositeEnvironmentResults, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION 
			| SWT.H_SCROLL | SWT.V_SCROLL, false);
		tableResults = tableViewerResults.getTable();
		FormData dataTableResults = new FormData();
		dataTableResults.left = new FormAttachment(0, 0);
		dataTableResults.top = new FormAttachment(0, 10);
		dataTableResults.bottom = new FormAttachment(100, 0);
		dataTableResults.right = new FormAttachment(100, 0);
		tableResults.setLayoutData(dataTableResults);
				
		// add result columns
		tableResults.setHeaderVisible(true);
		tableResults.setLinesVisible(true);
		for (EnvCheckerColumn val : EnvCheckerColumn.values()) {
			TableViewerColumn column = new TableViewerColumn(tableViewerResults,
					SWT.NONE);
			column.getColumn().setText(val.getTitle());
			column.getColumn().setWidth(val.getWidth());
			column.getColumn().setToolTipText(val.getTitle());
			column.getColumn().setResizable(true);
		}		
		tableViewerResults.setContentProvider(new EnvironmentTableContentProvider());
		tableViewerResults.setLabelProvider(new EnvironmentTableLabelProvider());			
		tableViewerResults.setInput(null);		
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
        preferences.put(UserPreferencesNodes.ENV_CHECK_PATH, textLocation.getText());
        try {
            preferences.flush();
        } catch (BackingStoreException e1) {
            logger.log(Level.SEVERE, "", e1);
            throw new SAFRFatalException(e1);                    
        }       
		
		try {
			getSite().getShell().setCursor(
					getSite().getShell().getDisplay().getSystemCursor(
							SWT.CURSOR_WAIT));
			String exportPath = textLocation.getText() + File.separatorChar
					+ componentType.toString() + "-"
					+ currentComponent.getName()+"-[" + currentComponent.getIdLabel() +"]"
					+ ".csv";

			// create the output stream
			try {
				OutputStream csvStream = new FileOutputStream(exportPath);
				// create the root row
				csvStream.write(("Env ID,Env Name,Comp Name,Created Time,Created By,Modify Time,Modify By" + SAFRUtilities.LINEBREAK)
						.getBytes());
				for (EnvComponentQueryBean bean : list) {
					String str = bean.getCSVString() + SAFRUtilities.LINEBREAK;
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
		List<EnvironmentQueryBean> envList;
		try {

			envList = SAFRQuery.queryEnvironmentsForLoggedInUser(
					SortType.SORT_BY_NAME, false);

			comboEnvironmentViewer.setInput(envList);
			comboEnvironmentViewer.refresh();
			for (EnvironmentQueryBean environment : envList) {

				comboEnvironment.setData(Integer.toString(counter++),
						environment);
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
			MainTableLabelProvider labelProvider = new MainTableLabelProvider(
					(ComponentType) componentType) {
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
				List<LogicalFileQueryBean> logicalFileList = SAFRQuery
						.queryAllLogicalFiles(currentEnvID,
								SortType.SORT_BY_NAME);
				comboComponentViewer.setInput(logicalFileList);
				comboComponentViewer.setSorter(new LogicalFileTableSorter(1,
						SWT.UP));

			} else if (componentType == ComponentType.LookupPath) {
				List<LookupQueryBean> lookuPathList = SAFRQuery
						.queryAllLookups(currentEnvID, SortType.SORT_BY_NAME);
				comboComponentViewer.setInput(lookuPathList);
				comboComponentViewer.setSorter(new LookupTableSorter(1, SWT.UP));

			} else if (componentType == ComponentType.LogicalRecord) {
				List<LogicalRecordQueryBean> logicalRecordList = SAFRQuery
						.queryAllLogicalRecords(currentEnvID,
								SortType.SORT_BY_NAME);
				comboComponentViewer.setInput(logicalRecordList);
				comboComponentViewer.setSorter(new LogicalRecordTableSorter(1,SWT.UP));
			} else if (componentType == ComponentType.PhysicalFile) {
				List<PhysicalFileQueryBean> physicalFileList = SAFRQuery
						.queryAllPhysicalFiles(currentEnvID,
								SortType.SORT_BY_NAME);
				comboComponentViewer.setInput(physicalFileList);
				comboComponentViewer.setSorter(new PhysicalFileTableSorter(1,
						SWT.UP));
			} else if (componentType == ComponentType.UserExitRoutine) {
				List<UserExitRoutineQueryBean> userExitRoutineList = SAFRQuery
						.queryAllUserExitRoutines(currentEnvID,
								SortType.SORT_BY_NAME);
				comboComponentViewer.setInput(userExitRoutineList);
				comboComponentViewer.setSorter(new UserExitRoutineTableSorter(
						1, SWT.UP));
			} else if (componentType == ComponentType.View) {
				List<ViewQueryBean> viewList = SAFRQuery.queryAllViews(
						currentEnvID, SortType.SORT_BY_NAME);
				comboComponentViewer.setInput(viewList);
				comboComponentViewer.setSorter(new ViewTableSorter(1, SWT.UP));
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
	public void storeModel() throws DAOException, SAFRException {
	}

	@Override
	public void validate() throws DAOException, SAFRException {
	}

	@Override
	public String getModelName() {
		return null;
	}

	@Override
	public void doRefreshControls() throws SAFRException {
	}

	@Override
	public void refreshModel() {
	}

	@Override
	public SAFRPersistentObject getModel() {
		return null;
	}

	@Override
	public String getComponentNameForSaveAs() {
		return null;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public ComponentType getEditorCompType() {
		return null;
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		return null;
	}
}
