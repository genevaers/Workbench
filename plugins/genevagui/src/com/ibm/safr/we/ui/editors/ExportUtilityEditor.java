package com.ibm.safr.we.ui.editors;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.utilities.export.ExportComponent;
import com.ibm.safr.we.model.utilities.export.ExportUtility;
import com.ibm.safr.we.model.utilities.importer.ViewRecordParser;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.utilities.ISearchablePart;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ExportUtilityEditor extends SAFREditorPart implements ISearchablePart {

	public static String ID = "SAFRWE.ExportMetadataComponents";
	public static Shell shell;
    private static final String SORT_CATEGORY = "Export";
    private static final String SORT_TABLE = "Components";
	
    /**
     * This private class is used to provide sorting functionality for
     * Components table.
     */
    private class ComponentsTableSorter extends ViewerSorter {
        private int colNumber1;
        private int dir2;

        public ComponentsTableSorter(int colNumber1, int dir2) {
            super();
            this.colNumber1 = colNumber1;
            this.dir2 = dir2;

        }

        public int compare(Viewer viewer, Object obj1, Object obj2) {
            ExportComponent expComp1 = (ExportComponent) obj1;
            ExportComponent expComp2 = (ExportComponent) obj2;
            EnvironmentalQueryBean component1 = expComp1.getComponent();
            EnvironmentalQueryBean component2 = expComp2.getComponent();

            ExportComponent g1 = (ExportComponent) obj1;
            ExportComponent g2 = (ExportComponent) obj2;

            int rc = 0;

            switch (colNumber1) {

            case 0:

                rc = g1.isSelected().compareTo(g2.isSelected());
                break;
            case 1:

                if ((g1.getResult() == null) && (g2.getResult() == null)) {

                    rc = 0;
                } else if (g1.getResult() == g2.getResult()) {
                    rc = 0;
                } else {

                    String str1 = "";
                    if (g1.getResult() == ActivityResult.PASS) {
                        str1 = PASS;
                    } else if (g1.getResult() == ActivityResult.FAIL) {
                        str1 = FAIL;

                    } else if (g1.getResult() == ActivityResult.LOADERRORS) {
                        str1 = LOADERROR;
                    }

                    String str2 = "";
                    if (g2.getResult() == ActivityResult.PASS) {
                        str2 = PASS;
                    } else if (g2.getResult() == ActivityResult.FAIL) {
                        str2 = FAIL;

                    } else if (g2.getResult() == ActivityResult.LOADERRORS) {
                        str2 = LOADERROR;
                    }

                    rc = UIUtilities.compareStrings(str1, str2);
                }
                break;
            case 2:
                if (component1 instanceof LogicalRecordQueryBean
                        && component2 instanceof LogicalRecordQueryBean) {
                    LogicalRecordQueryBean lr1 = (LogicalRecordQueryBean) component1;
                    LogicalRecordQueryBean lr2 = (LogicalRecordQueryBean) component2;
                    rc = lr1.getStatus().compareTo(lr2.getStatus());
                } else if (component1 instanceof LookupQueryBean
                        && component2 instanceof LookupQueryBean) {
                    LookupQueryBean lukp1 = (LookupQueryBean) component1;
                    LookupQueryBean lukp2 = (LookupQueryBean) component2;
                    rc = lukp1.getValidInd().compareTo(lukp2.getValidInd());
                } else if (component1 instanceof ViewQueryBean
                        && component2 instanceof ViewQueryBean) {
                    ViewQueryBean view1 = (ViewQueryBean) component1;
                    ViewQueryBean view2 = (ViewQueryBean) component2;
                    rc = view1.getStatus().compareTo(view2.getStatus());
                }
                break;
            case 3:
                rc = g1.getComponent().getId().compareTo(
                        g2.getComponent().getId());
                break;
            case 4:
                rc = UIUtilities.compareStrings(g1.getComponent().getName(), g2
                        .getComponent().getName());
                break;
            default:
                rc = 0;
            }

            // If the direction of sorting was descending previously,
            // reverse the
            // direction of sorting
            if (dir2 == SWT.DOWN) {
                rc = -rc;
            }
            return rc;
        }
    }

    
    public class StatusFilter extends ViewerFilter {

        private String status;

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement,
                Object element) {
            ExportComponent expComp = (ExportComponent) element;
            EnvironmentalQueryBean component = expComp.getComponent();

            if (component instanceof LogicalFileQueryBean
                    || component instanceof PhysicalFileQueryBean) {
                return true;
            }

            if (component instanceof LogicalRecordQueryBean) {
                LogicalRecordQueryBean logicalRecord = (LogicalRecordQueryBean) component;
                if (status != null) {
                    return logicalRecord.getStatus().equals(status);
                } else {
                    return true;
                }

            } else if (component instanceof LookupQueryBean) {
                LookupQueryBean lookup = (LookupQueryBean) component;
                if (status != null) {
                    if (lookup.getValidInd() == 1 && status.equals(ACTIVE)) {
                        return true;
                    } else if (lookup.getValidInd() == 0
                            && status.equals(INACTIVE)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            } else if (component instanceof ViewQueryBean) {
                ViewQueryBean view = (ViewQueryBean) component;
                if (status != null) {
                    return view.getStatus().equals(status);
                } else {
                    return true;
                }
            }

            return true;
        }
    }
    
    /**
     * This private class is used for column sorting in the Components Table.
     */
    private class ColumnSelectionListenerComponentsTable extends SelectionAdapter {
        private int colNumber1;
        private TableViewer tabViewer1;

        ColumnSelectionListenerComponentsTable(TableViewer tabViewer,
                int colNumber) {
            this.colNumber1 = colNumber;
            this.tabViewer1 = tabViewer;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                getSite().getShell().setCursor(
                        getSite().getShell().getDisplay().getSystemCursor(
                                SWT.CURSOR_WAIT));
                TableColumn sortColumn = tabViewer1.getTable().getSortColumn();
                TableColumn currentColumn = (TableColumn) e.widget;
                int dir = tabViewer1.getTable().getSortDirection();
                if (sortColumn == currentColumn) {
                    dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                } else {
                    tabViewer1.getTable().setSortColumn(currentColumn);
                    if (currentColumn == tabViewer1.getTable().getColumn(0)) {
                        dir = SWT.DOWN;
                    } else {
                        dir = SWT.UP;
                    }

                }

                tabViewer1.getTable().setSortDirection(dir);
                tabViewer1.setSorter(new ComponentsTableSorter(colNumber1, dir));
                
                Order order = Order.ASCENDING;
                if (dir == SWT.DOWN) {
                    order = Order.DESCENDING;
                }
                SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE, colNumber1, order);
                prefs.store();              
            } finally {
                getSite().getShell().setCursor(null);
            }
        }

    }

    private class ColumnComponentLabelProvider extends ColumnLabelProvider {

        private int colIndex;

        public ColumnComponentLabelProvider(int colIndex) {
            this.colIndex = colIndex;
        }

        @Override
        public Color getForeground(Object element) {
            ExportComponent expComp = (ExportComponent) element;
            if (colIndex == 1) {
            	if (expComp.getResult() == ActivityResult.CANCEL) {
                    return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
                } 
            	else if (expComp.getResult() == ActivityResult.FAIL) {
                    return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
                } else if (expComp.getResult() == ActivityResult.LOADERRORS) {
                    return Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
                }
                
                else {
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        public Image getImage(Object element) {
            return null;
        }

        @Override
        public String getText(Object element) {

            ExportComponent expComp = (ExportComponent) element;
            EnvironmentalQueryBean component = expComp.getComponent();

            switch (colIndex) {
            case 0:
                return "";
            case 1:
                if (expComp.getResult() == ActivityResult.PASS) {
                    return PASS;
                } else if (expComp.getResult() == ActivityResult.FAIL) {
                    return FAIL;
                } else if (expComp.getResult() == ActivityResult.LOADERRORS) {
                    return LOADERROR;
                } 
                else if (expComp.getResult() == ActivityResult.CANCEL) {
                    return CANCEL;
                } 
                else {
                    return "";
                }
            case 2:
                if (component instanceof LogicalRecordQueryBean) {
                    LogicalRecordQueryBean logicalRecord = (LogicalRecordQueryBean) component;
                    if (logicalRecord.getStatus().equals(ACTIVE)) {
                        return "Active";
                    } else {
                        return "Inactive";
                    }
                } else if (component instanceof LookupQueryBean) {
                    LookupQueryBean lookup = (LookupQueryBean) component;
                    if (lookup.getValidInd() == 0) {
                        return "Inactive";
                    } else {
                        return "Active";
                    }

                } else if (component instanceof ViewQueryBean) {
                    ViewQueryBean view = (ViewQueryBean) component;
                    if (view.getStatus().equals(ACTIVE)) {
                        return "Active";
                    } else {
                        return "Inactive";
                    }
                } else {
                    return "";
                }
            case 3:
                return component.getId().toString();
            case 4:
                return component.getName();

            }
            return "";
        }
    }

    public static String[] componentColumnHeaders = { "Select", "Result", "Status", "ID", "Name" };
    public static int[] componentColumnWidths = {75, 80, 75, 65, 380 };    
    
	private ScrolledForm form;
	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;

	private Section sectionComponents;
    private Composite compositeComponents;  
    private Group groupComponents;
    private TableComboViewer comboEnvironmentViewer;
    private TableCombo comboEnvironment;
    private Combo comboComponentType;
    private Table tableComponents;
    private CheckboxTableViewer tableViewerComponents;
    private Button buttonSelectAll;
    private Button buttonDeSelectAll;
    private Button buttonRefresh;  
   
    private Button formatvid;
    private Button formatfid;
    private Button formatvnamevid;
    private Button formatfnamefid;
    private Text textLocation;
    private Button buttonLocation;
    private Button buttonDefault;
    private Text textFilename;
    private DirectoryDialog dialogLocation;
    private Button buttonExport;    
    public static String componentselected="";

	private Section sectionErrors;
    private TableViewer tableViewerErrors;
    private Table tableErrors;

	private ComponentType componentType;
	private EnvironmentQueryBean currentEnvironment;
	private Integer currentEnvID = 0;
	private StatusFilter filter;
	final private String INACTIVE = "INACT";
	final private String ACTIVE = "ACTVE";

	private String selEnv = "";
	private int selectedComponentType = -1;
	private String fileName = "";
	final private int STATUSCOL = 2;
	final private String PASS = "Pass";
	final private String FAIL = "Error";
	final private String LOADERROR = "Security Error";
	final private String CANCEL = "Cancelled";
	ExportUtility exportUtility;
    private int prevSelection = 0;
	
	private ExportComponent exportComponent;
	private List<ExportComponent> modelList;
	private List<EnvironmentQueryBean> envList;

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FormLayout());
		form.getBody().setLayoutData(new FormData());
		form.setText("Export Utility");

		createSectionComponent(form.getBody());
		createSectionErrors(form.getBody());

		form.reflow(true);
		ManagedForm mFrm = new ManagedForm(toolkit, form);
		setMsgManager(mFrm.getMessageManager());
        tableViewerComponents.refresh();
	}

	private void createSectionComponent(Composite body) {
		sectionComponents = safrGuiToolkit.createSection(body,
				Section.TITLE_BAR, "Components to Export");
		FormData dataSectionComp = new FormData();
		dataSectionComp.top = new FormAttachment(0, 10);
		dataSectionComp.left = new FormAttachment(0, 5);
		sectionComponents.setLayoutData(dataSectionComp);

		compositeComponents = safrGuiToolkit.createComposite(sectionComponents,
				SWT.NONE);
		compositeComponents.setLayout(new FormLayout());
		compositeComponents.setLayoutData(new FormData());

		Label labelComponentType = safrGuiToolkit.createLabel(
				compositeComponents, SWT.NONE, "Component &Type:");
		FormData dataLabelComponentType = new FormData();
		dataLabelComponentType.width = SWT.DEFAULT;
		dataLabelComponentType.top = new FormAttachment(0, 18);
		dataLabelComponentType.left = new FormAttachment(0, 5);
		labelComponentType.setLayoutData(dataLabelComponentType);
		
		

		comboComponentType = safrGuiToolkit.createComboBox(compositeComponents,
				SWT.READ_ONLY, "");
		comboComponentType.setData(SAFRLogger.USER, "Component Type");                              		
		FormData dataComboComponentType = new FormData();
		dataComboComponentType.left = new FormAttachment(labelComponentType, 10);
		dataComboComponentType.top = new FormAttachment(0, 10);
		dataComboComponentType.width = 350;
		comboComponentType.setLayoutData(dataComboComponentType);
		int i = 0;
		
		comboComponentType.add("View");
		comboComponentType.setData(String.valueOf(i++), 
		    ComponentType.View);
        comboComponentType.add("View Folder");
        comboComponentType.setData(String.valueOf(i++), 
            ComponentType.ViewFolder);

        
        comboComponentType.addFocusListener(new FocusAdapter() {

            public void focusLost(FocusEvent e) {      
                ApplicationMediator.getAppMediator().waitCursor();
                if (comboComponentType.getSelectionIndex() != selectedComponentType) {
                    ComponentType oldType = componentType;                    
                    componentType = (ComponentType) comboComponentType.getData(
                        String.valueOf(comboComponentType.getSelectionIndex()));
                    String select = componentType.getLabel();
                    componentselected = select;
                    String oldLocDef = ExportUtility.getDefaultLocation(oldType);
                    if (oldLocDef.equals(textLocation.getText())) {
                        // change to the new location
                        textLocation.setText(ExportUtility.getDefaultLocation(componentType));
                    }
                    setRadioGroup();
                    filter.setStatus(null);
                    if (currentEnvID > 0l) {
                        populateComponentTable();
                        buttonSelectAll.setEnabled(true);
                        buttonDeSelectAll.setEnabled(true);
                        buttonRefresh.setEnabled(true);
                        tableViewerComponents.refresh();
                    }
                    // CQ8807;Mustufa;clear errors if comp type changes.
                    showSectionErrors(false);
                    selectedComponentType = comboComponentType.getSelectionIndex();
                    componentselection(componentselected);
                }
                ApplicationMediator.getAppMediator().normalCursor();   
            }

			private void componentselection(String componentselected) {
				if(componentselected.equals("View") && formatvid.getSelection() ) {
					
					formatfid.setVisible(false);
					formatfnamefid.setVisible(false);
					formatvid.setVisible(true);
					formatvnamevid.setVisible(true);
					
					formatvid.setEnabled(true);
					formatvid.setSelection(false);
					formatvnamevid.setEnabled(true);
					formatvnamevid.setSelection(false);
					textFilename.setEnabled(true);

				}
				if(componentselected.equals("View")) {
					formatvid.setVisible(true);
					formatvnamevid.setVisible(true);
					formatfid.setVisible(false);
					formatfnamefid.setVisible(false);
				}
				if(componentselected.equals("View Folder")) {
					formatvid.setVisible(false);
					formatvnamevid.setVisible(false);
					formatfid.setVisible(true);
					formatfnamefid.setVisible(true);
					textFilename.setText("");
				}

			}
        });

        Label labelEnvironment = safrGuiToolkit.createLabel(
				compositeComponents, SWT.NONE, "&Environment:");

		FormData dataLabelEnvironment = new FormData();
		dataLabelEnvironment.width = SWT.DEFAULT;
		dataLabelEnvironment.left = new FormAttachment(0, 5);
		dataLabelEnvironment.top = new FormAttachment(labelComponentType, 10);
		labelEnvironment.setLayoutData(dataLabelEnvironment);
		
		comboEnvironmentViewer = safrGuiToolkit.createTableComboForComponents(
				compositeComponents, ComponentType.Environment);
		comboEnvironment = comboEnvironmentViewer.getTableCombo();
		comboEnvironment.setData(SAFRLogger.USER, "Environment");                              

		

		FormData dataComboEnvironment = new FormData();
		dataComboEnvironment.left = new FormAttachment(labelComponentType, 10);
		dataComboEnvironment.top = new FormAttachment(labelComponentType, 10);
		dataComboEnvironment.width = 375;
		comboEnvironment.setLayoutData(dataComboEnvironment);

		comboEnvironment.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				
				try {
					getSite().getShell().setCursor(
							getSite().getShell().getDisplay().getSystemCursor(
									SWT.CURSOR_WAIT));
					if (!(comboEnvironment.getText().equals(selEnv))) {
						currentEnvironment = (EnvironmentQueryBean) comboEnvironment
								.getTable().getSelection()[0].getData();

						if (currentEnvironment != null) {
							currentEnvID = currentEnvironment.getId();
		                    if (currentEnvID > 0l) {
		                        populateComponentTable();
		                        buttonSelectAll.setEnabled(true);
		                        buttonDeSelectAll.setEnabled(true);
		                        buttonRefresh.setEnabled(true);
		                        tableViewerComponents.refresh();
		                    }
						}
						if (componentType != null) {
							setRadioGroup();
							populateComponentTable();
							tableViewerComponents.refresh();
						}
						showSectionErrors(false);
						filter.setStatus(null);
						selEnv = comboEnvironment.getText();
					}
					
				} finally {
					getSite().getShell().setCursor(null);
					
				}
			}
		});

		// load the data
		try {

			envList = SAFRQuery.queryEnvironmentsForLoggedInUser(
					SortType.SORT_BY_NAME, false);
		} catch (DAOException e1) {
			UIUtilities.handleWEExceptions(e1,
					"Error occurred while retrieving all environments.",
					UIUtilities.titleStringDbException);
		}
		Integer count = 0;
		if (envList != null) {
			for (EnvironmentQueryBean enviromentQuerybean : envList) {
				comboEnvironment.setData(Integer.toString(count),
						enviromentQuerybean);
				count++;
			}
		}

		comboEnvironmentViewer.setInput(envList);
		Label labelComponents = safrGuiToolkit.createLabel(compositeComponents,
				SWT.NONE, "Com&ponent(s):");
		FormData dataLabelComponents = new FormData();
		dataLabelComponents.width = SWT.DEFAULT;
		dataLabelComponents.top = new FormAttachment(labelEnvironment, 18);
		dataLabelComponents.left = new FormAttachment(0, 5);
		labelComponents.setLayoutData(dataLabelComponents);

		tableComponents = safrGuiToolkit.createTable(compositeComponents,
				SWT.CHECK | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL
						| SWT.BORDER, false);
		tableComponents.setData(SAFRLogger.USER, "Components Table");                                    		
		tableViewerComponents = safrGuiToolkit
				.createCheckboxTableViewer(tableComponents);

		groupComponents = new Group(compositeComponents, SWT.SHADOW_NONE);
		groupComponents.setLayout(new FormLayout());
		FormData dataGroup = new FormData();
		dataGroup.left = new FormAttachment(0, 5);
		dataGroup.top = new FormAttachment(labelComponents, 10);
		dataGroup.width = SWT.DEFAULT;
		dataGroup.height = SWT.DEFAULT;
		groupComponents.setLayoutData(dataGroup);
		groupComponents.setVisible(false); // hide initially


	
		FormData dataTableComponents = new FormData();
		dataTableComponents.left = new FormAttachment(labelComponentType, 10);
		dataTableComponents.top = new FormAttachment(comboEnvironment, 10);
		dataTableComponents.height = 210;
		dataTableComponents.width = 700;

		tableComponents.setLayoutData(dataTableComponents);
		toolkit.adapt(tableComponents, false, false);
		tableComponents.setHeaderVisible(true);
		tableComponents.setLinesVisible(true);
		// Code for tracking the focus on the table
		IFocusService service = (IFocusService) getSite().getService(
				IFocusService.class);
		service.addFocusTracker(tableComponents, "ExportSearchableTable");

		tableComponents.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              
              if (event.detail == SWT.CHECK) {                  
                  int selRow = tableComponents.indexOf((TableItem)event.item);  
                  int stateMask=event.stateMask;                  
                  if((stateMask & SWT.SHIFT)==SWT.SHIFT){
                      int prevRow = prevSelection;
                      
                      if((stateMask & SWT.CTRL)!=SWT.CTRL){
                          tableViewerComponents.setAllChecked(false);
                          for (Object obj : tableViewerComponents.getCheckedElements()) {
                              ((ExportComponent)obj).setSelected(false);
                          }                          
                          
                      }
                      if (prevRow > selRow) {
                          for (int i=selRow ; i<=prevRow ; i++) {
                              Object element = tableViewerComponents.getElementAt(i);
                              tableViewerComponents.setChecked(element, true);
                              modelList.get(i).setSelected(true);
                          }
                      }
                      else {
                          for (int i=prevRow ; i<=selRow ; i++) {
                              Object element = tableViewerComponents.getElementAt(i);
                              tableViewerComponents.setChecked(element, true);
                              modelList.get(i).setSelected(true);
                          }                            
                      }
                  }   
                  else {
                      Object element = tableViewerComponents.getElementAt(selRow);
                      if (tableViewerComponents.getChecked(element)) {
                          prevSelection = tableComponents.indexOf((TableItem)event.item);
                      }
                      else {
                          prevSelection = 0;
                      }
                  }
              }                  
            }
          });
		
		int counter = 0;
		int length = componentColumnHeaders.length;
		for (counter = 0; counter < length; counter++) {
			TableViewerColumn column = new TableViewerColumn(
					tableViewerComponents, SWT.NONE);
			column.getColumn().setText(componentColumnHeaders[counter]);
			column.getColumn().setToolTipText(componentColumnHeaders[counter]);
			column.getColumn().setWidth(componentColumnWidths[counter]);
			column.getColumn().setResizable(true);
			column.setLabelProvider(new ColumnComponentLabelProvider(counter));

			ColumnSelectionListenerComponentsTable colListener = new ColumnSelectionListenerComponentsTable(
					tableViewerComponents, counter);
			column.getColumn().addSelectionListener(colListener);

		}
		tableViewerComponents.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {

				if (buttonExport != null) {
					buttonExport.setEnabled(false);
				}
				if (currentEnvID > 0l && componentType != null) {
					filter.setStatus(ACTIVE);
					return modelList.toArray();
				} else {
					return new String[0];
				}
			}

			public void dispose() {

			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}

		});
		
		filter = new StatusFilter();
		tableViewerComponents.addFilter(filter);
		ExportComponent modelItem = null;
		Object[] checkedElements = null;
		tableViewerComponents.getTable().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (tableViewerComponents.getCheckedElements().length > 0) {
					buttonExport.setEnabled(true);

				} else {
					buttonExport.setEnabled(false);
				}
			}
			
			

		});
		
		tableViewerComponents.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {

				ExportComponent currModel = (ExportComponent) ((IStructuredSelection) event
						.getSelection()).getFirstElement();
				
				if (currModel != null) {
					if (currModel.getErrors().isEmpty()) {
						showSectionErrors(false);
					} else {
						showSectionErrors(true);
						if (currModel.getResult() == ActivityResult.LOADERRORS) {
							tableErrors.getColumn(0).setText(
							    "User does not have read rights on the following dependent components");
						} else {
							tableErrors.getColumn(0).setText("Errors");
						}

					}
				}
			}

		});
		
		tableViewerComponents.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                refreshFilename();                
            }
		    
		});
		tableViewerComponents.setInput(1);
		
        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE);
        if (prefs.load()) {
            tableViewerComponents.getTable().setSortColumn(
                tableViewerComponents.getTable().getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                tableViewerComponents.getTable().setSortDirection(SWT.UP);
                tableViewerComponents.setSorter(new ComponentsTableSorter(prefs.getColumn(), SWT.UP));
            }
            else {
                tableViewerComponents.getTable().setSortDirection(SWT.DOWN);
                tableViewerComponents.setSorter(new ComponentsTableSorter(prefs.getColumn(), SWT.DOWN));
            }                   
        }
        else {
            tableViewerComponents.getTable().setSortColumn(
                tableViewerComponents.getTable().getColumn(4));
            tableViewerComponents.getTable().setSortDirection(SWT.UP);
            tableViewerComponents.setSorter(new ComponentsTableSorter(4, SWT.UP));
        }		
		
		buttonSelectAll = safrGuiToolkit.createButton(compositeComponents,
				SWT.PUSH, "&Select All");
		buttonSelectAll.setData(SAFRLogger.USER, "Select All");                                                            		
		FormData dataSelectAll = new FormData();
		dataSelectAll.left = new FormAttachment(labelComponentType, 10);
		dataSelectAll.top = new FormAttachment(tableComponents, 5);
		buttonSelectAll.setLayoutData(dataSelectAll);
		buttonSelectAll.setEnabled(false);

		buttonSelectAll.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (modelList == null || modelList.size() == 0) {
					return;
				}
				List<ExportComponent> selectedComps = new ArrayList<ExportComponent>();
				for (ExportComponent eComponent : modelList) {
					eComponent.setSelected(true);
					selectedComps.add(eComponent);
					
				}
				tableViewerComponents.setCheckedElements(selectedComps.toArray());
				buttonExport.setEnabled(true);
                prevSelection = 0;     
                refreshFilename();
			}

		});

		buttonDeSelectAll = safrGuiToolkit.createButton(compositeComponents,
				SWT.PUSH, "&Deselect All");
		buttonDeSelectAll.setData(SAFRLogger.USER, "Deselect All");                                                                   		
		FormData dataDeSelectAll = new FormData();
		dataDeSelectAll.left = new FormAttachment(buttonSelectAll, 5);
		dataDeSelectAll.top = new FormAttachment(tableComponents, 5);
		buttonDeSelectAll.setLayoutData(dataDeSelectAll);
		buttonDeSelectAll.setEnabled(false);

		buttonDeSelectAll.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (modelList == null || modelList.size() == 0) {
					return;
				}
				tableViewerComponents.setAllChecked(false);
				buttonExport.setEnabled(false);
				for (ExportComponent comp : modelList) {
					comp.setSelected(false);
				}
				prevSelection = 0;
				refreshFilename();
			}

		});
		
		buttonRefresh = safrGuiToolkit.createButton(compositeComponents,
				SWT.PUSH, "&Refresh");
		buttonRefresh.setData(SAFRLogger.USER, "Refresh");                                                                           		
		FormData dataRefresh = new FormData();
		dataRefresh.left = new FormAttachment(buttonDeSelectAll, 5);
		dataRefresh.top = new FormAttachment(tableComponents, 5);
		buttonRefresh.setLayoutData(dataRefresh);
		buttonRefresh.setEnabled(false);
		
		// Set width of all buttons to the widest button
		List<Button> buttons = new ArrayList<Button>();
		buttons.add(buttonSelectAll);
		buttons.add(buttonDeSelectAll);
		buttons.add(buttonRefresh);
		int width = UIUtilities.computePreferredButtonWidth(buttons);
		dataSelectAll.width = width;
		dataDeSelectAll.width = width;
		dataRefresh.width = width;

		buttonRefresh.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// no op
			}

			public void widgetSelected(SelectionEvent e) {
				populateComponentTable();
				tableViewerComponents.refresh();
				tableViewerComponents.setAllChecked(false);
				showSectionErrors(false);
				buttonExport.setEnabled(false);
                prevSelection = 0;				
			}

		});		


		sectionComponents.setClient(compositeComponents);

		Label labelLocation = safrGuiToolkit.createLabel(compositeComponents,
				SWT.NONE, "&Location:");
		FormData dataLabelLocation = new FormData();
		dataLabelLocation.top = new FormAttachment(buttonSelectAll, 10);
		dataLabelLocation.width = 75;
		dataLabelLocation.left = new FormAttachment(0, 5);
		labelLocation.setLayoutData(dataLabelLocation);

		
		
		textLocation = safrGuiToolkit
				.createTextBox(compositeComponents, SWT.NONE);
		textLocation.setData(SAFRLogger.USER, "Location");                                                                                		
		FormData dataLocation = new FormData();
		dataLocation.left = new FormAttachment(labelComponentType, 10);
		dataLocation.top = new FormAttachment(buttonSelectAll, 10);
		dataLocation.width = 580;
		textLocation.setLayoutData(dataLocation);
        Preferences preferences = SAFRPreferences.getSAFRPreferences(); 		
        String impPath = preferences.get(UserPreferencesNodes.EXPORT_PATH,"");
        if (impPath==null || impPath.equals("")) { 
            textLocation.setText(ExportUtility.getDefaultLocation(componentType));
        }
        else {
            textLocation.setText(impPath);            
        }

		buttonLocation = safrGuiToolkit.createButton(compositeComponents,
				SWT.NONE, "&Browse...");
		buttonLocation.setData(SAFRLogger.USER, "Browse");                                                                                        		
		FormData dataButtonLocation = new FormData();
		dataButtonLocation.left = new FormAttachment(textLocation, 5);
		dataButtonLocation.top = new FormAttachment(buttonSelectAll, 8);
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

        buttonDefault = safrGuiToolkit.createButton(compositeComponents,
            SWT.NONE, "Def&ault");
        buttonDefault.setData(SAFRLogger.USER, "Default");                                                                                                
        FormData dataButtonDefault = new FormData();
        dataButtonDefault.left = new FormAttachment(buttonLocation, 5);
        dataButtonDefault.top = new FormAttachment(buttonSelectAll, 8);
        buttonDefault.setLayoutData(dataButtonDefault);
        buttonDefault.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                textLocation.setText(ExportUtility.getDefaultLocation(componentType));
            }
            
        });
		
        Label labelmultiple = safrGuiToolkit.createLabel(compositeComponents,
				SWT.NONE, "&Export to multiple files:");
		FormData dataLabelmultiple = new FormData();
		dataLabelmultiple.top = new FormAttachment(labelLocation, 10);
		dataLabelmultiple.width = 150;
		dataLabelmultiple.left = new FormAttachment(0, 5);
		labelmultiple.setLayoutData(dataLabelmultiple);

		formatvid = safrGuiToolkit.createCheckBox(compositeComponents,
	            "FileName format: V<view-id>"+ "                                               " + "(suitable for input to MR91)");       
	    FormData dataformatvid = new FormData();
	    dataformatvid.top = new FormAttachment(labelmultiple, 5);
	    dataformatvid.left = new FormAttachment(0, 10);
	    formatvid.setLayoutData(dataformatvid);
		

	    formatvid.addSelectionListener(new SelectionAdapter() {

	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	refreshFilename();
	            }
			    
		});
		formatvnamevid = safrGuiToolkit.createCheckBox(compositeComponents,
	            "FileName format: <view-name>[view-id].xml" + "                    " + "(suitable for viewing in a browser)");       
	    FormData dataformatvnamevid = new FormData();
	    dataformatvnamevid.top = new FormAttachment(formatvid, 5);
	    dataformatvnamevid.left = new FormAttachment(0, 10);
	    formatvnamevid.setLayoutData(dataformatvnamevid);
		
	    formatvnamevid.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
            	refreshFilename();
            }
		});
	    
	    formatfid = safrGuiToolkit.createCheckBox(compositeComponents,
	            "FileName format: F<folder-id>"+ "                                               " + "(suitable for input to MR91)");       
	    FormData dataformatfid = new FormData();
	    dataformatfid.top = new FormAttachment(labelmultiple, 5);
	    dataformatfid.left = new FormAttachment(0, 10);
	    formatfid.setLayoutData(dataformatfid);
		

	    formatfid.addSelectionListener(new SelectionAdapter() {

	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	refreshFilename();
	            }
			    
		});
		formatfnamefid = safrGuiToolkit.createCheckBox(compositeComponents,
	            "FileName format: <folder-name>[folder-id].xml" + "                 " + "(suitable for viewing in a browser)");       
	    FormData dataformatfnamefid = new FormData();
	    dataformatfnamefid.top = new FormAttachment(formatvid, 5);
	    dataformatfnamefid.left = new FormAttachment(0, 10);
	    formatfnamefid.setLayoutData(dataformatfnamefid);
		
	    formatfnamefid.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
            	refreshFilename();
            }
		});
		Label labelsingle = safrGuiToolkit.createLabel(compositeComponents,
				SWT.NONE, "&Export to single file:");
		FormData dataLabelsingle = new FormData();
		dataLabelsingle.top = new FormAttachment(formatvnamevid, 10);
		dataLabelsingle.width = 150;
		dataLabelsingle.left = new FormAttachment(0, 5);
		labelsingle.setLayoutData(dataLabelsingle);
		
        Label labelFilename = safrGuiToolkit.createLabel(compositeComponents, SWT.NONE, "&Filename:");
        FormData dataLabelFilename = new FormData();
        dataLabelFilename.top = new FormAttachment(labelsingle, 10);
        dataLabelFilename.left = new FormAttachment(0, 5);
        labelFilename.setLayoutData(dataLabelFilename);
    
        textFilename = safrGuiToolkit.createTextBox(compositeComponents, SWT.NONE);
        textFilename.setData(SAFRLogger.USER, "Filename");                                                                                        
        FormData dataFilename = new FormData();
        dataFilename.left = new FormAttachment(labelComponentType, 10);
        dataFilename.top = new FormAttachment(labelsingle, 10);
        dataFilename.width = 375;
        textFilename.setLayoutData(dataFilename);
		
		buttonExport = safrGuiToolkit.createButton(compositeComponents, SWT.PUSH,"E&xport");
		buttonExport.setData(SAFRLogger.USER, "Export");                                                                                                		
		FormData dataExportButton = new FormData();
		dataExportButton.top = new FormAttachment(textFilename, 10);
		dataExportButton.left = new FormAttachment(labelComponentType, 10);
		dataExportButton.width = 100;
		buttonExport.setLayoutData(dataExportButton);
		buttonExport.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

                Preferences preferences = SAFRPreferences.getSAFRPreferences(); 
                preferences.put(UserPreferencesNodes.EXPORT_PATH, textLocation.getText());
                try {
                    preferences.flush();
                } catch (BackingStoreException e1) {
                    logger.log(Level.SEVERE, "Failed to save preferences", e1);
                    throw new SAFRFatalException("Failed to save preferences " + e1.getMessage());                    
                }       
			    
				ExportComponent modelItem = null;
				Object[] checkedElements = null;
				checkedElements = tableViewerComponents.getCheckedElements();
				List<ExportComponent> list = new ArrayList<ExportComponent>();
				for (Object item : checkedElements) {

					modelItem = (ExportComponent) item;
					list.add(modelItem);
				}
				boolean multiple = false;
				if(formatvid.getSelection() || formatvnamevid.getSelection() || formatfid.getSelection() || formatfnamefid.getSelection() ) {
					multiple=true;
				}

				shell = getSite().getShell();
				exportUtility = new ExportUtility(currentEnvironment,
						textLocation.getText(), textFilename.getText(), componentType, formatvid.getSelection() ,formatvnamevid.getSelection(), formatfid.getSelection(), formatfnamefid.getSelection(), multiple);
				
				try {
				    ApplicationMediator.getAppMediator().waitCursor();
					exportUtility.export(list,Display.getDefault().getActiveShell());
					getMsgManager().removeAllMessages();

				} catch (SAFRValidationException e1) {
					String title;
					if (SAFRValidationType.PARAMETER_ERROR.equals(e1
							.getSafrValidationType())) {
						title = "Export parameter error";
					} else {
						title = "Export error";
					}
					decorateEditor(e1);
					MessageDialog.openError(getSite().getShell(), title,
							e1.getMessageString());
				} finally {
                    ApplicationMediator.getAppMediator().normalCursor();
					tableViewerComponents.refresh();
				}

			}
		});
		buttonExport.setEnabled(false);

	}

    public void populateComponentTable() {

        try {
            List<?> componentList;
            modelList = new ArrayList<ExportComponent>();
            if (componentType == ComponentType.LogicalFile) {
                componentList = SAFRQuery.queryAllLogicalFiles(currentEnvID,
                        SortType.SORT_BY_NAME);
            } else if (componentType == ComponentType.LookupPath) {
                componentList = SAFRQuery.queryAllLookups(currentEnvID,
                        SortType.SORT_BY_NAME);
            } else if (componentType == ComponentType.LogicalRecord) {
                componentList = SAFRQuery.queryAllLogicalRecords(currentEnvID,
                        SortType.SORT_BY_NAME);
            } else if (componentType == ComponentType.PhysicalFile) {
                componentList = SAFRQuery.queryAllPhysicalFiles(currentEnvID,
                        SortType.SORT_BY_NAME);
            } else if (componentType == ComponentType.View) {
                componentList = SAFRQuery.queryAllViews(currentEnvID,
                        SortType.SORT_BY_NAME);
            } else if (componentType == ComponentType.ViewFolder) {
                componentList = SAFRQuery.queryAllViewFolders(currentEnvID,
                    SortType.SORT_BY_NAME);
            } else {
                componentList = null;
            }
            for (Object obj : componentList) {
                EnvironmentalQueryBean component = (EnvironmentalQueryBean) obj;
                exportComponent = new ExportComponent(component);
                modelList.add(exportComponent);
            }

        } catch (DAOException e1) {
            UIUtilities.handleWEExceptions(e1);
        }

    }

    private void showSectionErrors(Boolean visible) {
        sectionErrors.setExpanded(visible);
        sectionErrors.setEnabled(visible);
        if (visible) {
            tableViewerErrors.refresh();
        }
    }

	
    public void setRadioGroup() {
        if (componentType == ComponentType.LogicalRecord
                || componentType == ComponentType.LookupPath
                || componentType == ComponentType.View) {
            groupComponents.setVisible(true);
            groupComponents.setEnabled(true);
            formatvid.setSelection(false);
            formatvnamevid.setSelection(false);
            tableViewerComponents.getTable().getColumn(STATUSCOL).setWidth(75);
        } else {
            groupComponents.setEnabled(false);
            groupComponents.setVisible(false);
            formatvid.setSelection(false);
            formatvnamevid.setSelection(false);
            tableViewerComponents.getTable().getColumn(STATUSCOL).setWidth(0);
            tableViewerComponents.getTable().getColumn(STATUSCOL).setResizable(false);
        }
    }

	
	private void createSectionErrors(Composite body) {
		sectionErrors = safrGuiToolkit.createSection(body, Section.TITLE_BAR
				| Section.TWISTIE, "Errors");
		FormData dataErrors = new FormData();
		dataErrors.left = new FormAttachment(sectionComponents, 15);
		dataErrors.top = new FormAttachment(0, 10);
		dataErrors.right = new FormAttachment(100, -5);
		sectionErrors.setLayoutData(dataErrors);

		Composite compositeErrors = safrGuiToolkit.createComposite(
				sectionErrors, SWT.NONE);
		compositeErrors.setLayout(new FormLayout());
		compositeErrors.setLayoutData(new FormData());
		tableViewerErrors = safrGuiToolkit.createTableViewer(compositeErrors,
				SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER, false);
		tableErrors = tableViewerErrors.getTable();
		tableErrors.setHeaderVisible(true);
		tableErrors.setLinesVisible(true);
		tableErrors.setLayout(new FormLayout());
		FormData dataTableErrors = new FormData();
		dataTableErrors.left = new FormAttachment(0, 5);
		dataTableErrors.top = new FormAttachment(0, 5);
		dataTableErrors.right = new FormAttachment(100, 0);
		dataTableErrors.height = 337;
		tableErrors.setLayoutData(dataTableErrors);

		int COLUMNWIDTH = 450;
		String COLUMNHEADER = "Errors";

		TableViewerColumn column = new TableViewerColumn(tableViewerErrors,
				SWT.NONE);
		column.getColumn().setText(COLUMNHEADER);
		column.getColumn().setWidth(COLUMNWIDTH);
		column.getColumn().setResizable(true);

		tableViewerErrors.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				if (tableViewerComponents.getSelection() == null) {
					return new String[0];
				} else {
					IStructuredSelection selection = (IStructuredSelection) tableViewerComponents
							.getSelection();
					ExportComponent currModel = (ExportComponent) selection
							.getFirstElement();
					if (currModel == null) {
						return new String[0];
					} else if (currModel.getErrors() == null) {
						return new String[0];
					} else {
						return currModel.getErrors().toArray();
					}
				}

			}

			public void dispose() {
				// TODO Auto-generated method stub

			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// TODO Auto-generated method stub

			}

		});
		tableViewerErrors.setLabelProvider(new ColumnLabelProvider());
		tableViewerErrors.setInput(1);

		showSectionErrors(false);
		sectionErrors.setClient(compositeErrors);

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public String getModelName() {

		return null;
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
		comboComponentType.setFocus();
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {

	}

	@Override
	public void validate() throws DAOException, SAFRException {

	}

	/**
	 * This method is used to get the widget based on the property passed.
	 * 
	 * @param property
	 * @return the widget.
	 */
	protected Control getControlFromProperty(Object property) {
		if (property == ExportUtility.Property.COMPONENT_TYPE) {
			return comboComponentType;
		} else if (property == ExportUtility.Property.ENVIRONMENT) {
			return comboEnvironment;
		} else if (property == ExportUtility.Property.LOCATION) {
			return textLocation;
        } else if (property == ExportUtility.Property.FILENAME) {
            return textFilename;
        }
		return null;
	}

	public void searchComponent(MetadataSearchCriteria searchCriteria,
			String searchText) {
		TableViewer tabViewer = null;
		if (this.tableComponents.isFocusControl()) {
			tabViewer = this.tableViewerComponents;
		}
		if (tabViewer != null) {
			// if search criteria is id, then sort the list of components
			// according to id.
			if (searchCriteria == MetadataSearchCriteria.ID) {
				tabViewer.getTable().setSortColumn(
						tabViewer.getTable().getColumn(1));
				tabViewer.getTable().setSortDirection(SWT.UP);
				tabViewer.setSorter(new ComponentsTableSorter(3, SWT.UP));
			} else {
				// if search criteria is name, then sort the list of components
				// according to name.
				tabViewer.getTable().setSortColumn(
						tabViewer.getTable().getColumn(2));
				tabViewer.getTable().setSortDirection(SWT.UP);
				tabViewer.setSorter(new ComponentsTableSorter(4, SWT.UP));
			}

			// get the items of the table and apply search.
			for (TableItem item : tabViewer.getTable().getItems()) {
				ExportComponent bean = (ExportComponent) item.getData();
				if (searchCriteria == MetadataSearchCriteria.ID) {
					if (bean.getComponent().getIdLabel().startsWith(searchText)) {
						tabViewer.setSelection(new StructuredSelection(bean),
								true);
						return;
					}
				} else if (searchCriteria == MetadataSearchCriteria.NAME) {
					if (bean.getComponent().getNameLabel() != null
							&& bean.getComponent().getNameLabel().toLowerCase()
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
		if (tableComponents.isFocusControl()) {
			return componentType;
		}
		return null;
	}

    protected String generateFileName(List<ExportComponent> exportComponents, ComponentType componentType) {
    	String fileName = "";
        if (exportComponents.size() == 0) {
            return "";
        }
        else if (exportComponents.size() == 1) {
            fileName = exportComponents.get(0).getComponent().getName() + 
                "[" + exportComponents.get(0).getComponent().getId() + "].xml";
        }
        else {
            fileName = getCompName(componentType);
            for (ExportComponent exportComponent : exportComponents) {
                fileName += "[" + exportComponent.getComponent().getId() + "]";
            }
            fileName += ".xml";
        }
        return fileName;
    }

    protected static String getCompName(ComponentType componentType) {
        String compName = "";
        if (componentType == ComponentType.PhysicalFile) {
            compName = "PhysicalFile";
        } else if (componentType == ComponentType.LogicalFile) {
            compName = "LogicalFile";
        } else if (componentType == ComponentType.LogicalRecord) {
            compName = "LogicalRecord";
        } else if (componentType == ComponentType.LookupPath) {
            compName = "LookupPath";
        } else if (componentType == ComponentType.View) {
            compName = "View";
        } else if (componentType == ComponentType.ViewFolder) {
            compName = "ViewFolder";
        }
        return compName;
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
    public ComponentType getEditorCompType() {
        return null;
    }

    @Override
    public Boolean retrySaveAs(SAFRValidationException sve) {
        return null;
    }

    
    
    protected void refreshFilename() {
        if (formatvid.getSelection() || formatvnamevid.getSelection() || formatfid.getSelection() || formatfnamefid.getSelection()) {
            textFilename.setText("");
            textFilename.setEnabled(false);
        }
        else {
            textFilename.setEnabled(true);
            Object[] checkedElements = tableViewerComponents.getCheckedElements();
            List<ExportComponent> list = new ArrayList<ExportComponent>();
            for (Object item : checkedElements) {
                ExportComponent modelItem = (ExportComponent) item;
                list.add(modelItem);
            }    
            fileName = generateFileName(list,componentType);
            textFilename.setText(fileName);
        }
    }
    
}

	

