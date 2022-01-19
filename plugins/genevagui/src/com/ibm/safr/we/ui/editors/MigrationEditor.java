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


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.services.ISourceProviderService;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRCancelException;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.ControlRecordQueryBean;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.utilities.Migration;
import com.ibm.safr.we.model.utilities.MigrationComponent;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.security.UserSession;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy;
import com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy.SAFRGUIContext;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class MigrationEditor extends SAFREditorPart {

	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.we.ui.editors.MigrationEditor");
	
	public static String ID = "SAFRWE.MigrationEditor";

	public static String[] HistoryTableColHeaders = { "Environment",
			"Checked Out?", "Version", "Checked Out By", "Date Checked Out",
			"Checked In By", "Date Checked In", "Comments" };

	public static int[] HistoryTableColWidths = { 130, 75, 50, 100, 130, 100,
			130, 150 };
	
	private static final String ACTIVE = "ACTVE";

    private static final String SORT_CATEGORY = "Migration";
    private static final String SORT_TABLE = "Components";
	
	private ScrolledForm form;
	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;

	private Composite compositeComponentSelectionSection;

	private Section sectionComponentSelection;

	private Label labelTargetEnv;
	private Label labelComponentType;
	private Label labelMigComp;

	private Integer currentSourceEnvID = 0;
	private EnvironmentQueryBean currentSourceEnv;
	private Integer currentTargetEnvID = 0;
	private EnvironmentQueryBean currentTargetEnv;
	private ComponentType currentComponentType;
	private Boolean currentMigrateRelated;

	private TableCombo comboSourceEnvironment;
	private TableCombo comboTargetEnvironment;
	private TableComboViewer comboSourceEnvironmentViewer;
	private TableComboViewer comboTargetEnvironmentViewer;

	private Combo comboComponentType;

	// Migration components table
	private Table migCompTable;
	private CheckboxTableViewer migCompTableViewer;
	private static String[] migCompColumnHeaders = { "Select", "Result", "ID", "Name" };
	private static int[] migCompColumnWidths = { 75, 80, 80, 220 };
	private int prevSelection = 0;
    
    private Button buttonSelectAll;
    private Button buttonDeSelectAll;
    private Button buttonRefresh;
    private Button checkboxMigrateRelated;
	private Button buttonMigrate;

	// Errors section
    private Section errorsSection;
    private Composite errorsComposite;
    private TableViewer errorsTableViewer;
    private Table errorsTable;
	
	private List<EnvironmentQueryBean> sourceEnvList = new ArrayList<EnvironmentQueryBean>();
	private List<EnvironmentQueryBean> targetEnvList = new ArrayList<EnvironmentQueryBean>();
	private List<EnvironmentQueryBean> subsetTargetEnvList = new ArrayList<EnvironmentQueryBean>();
    private List<MigrationComponent> migComps = new ArrayList<MigrationComponent>();

	protected Map<ComponentType, List<EnvironmentalQueryBean>> relatedComponents;
	private Migration migration;

	private int selectedSourceEnvIdx = -1;
	private int selectedTargetEnvIdx = -1;
	private int selectedComponentTypeIdx = -1;

	private MigrationObserver migrationProgress;
	
    private MenuItem srcEnvOpenEditorItem = null;
    private MenuItem trgEnvOpenEditorItem = null;
	
	private class MigrationObserver implements Observer {

		private class UpdateRunnable implements Runnable {
			private String msg;
			
			public UpdateRunnable(String str) {
				this.msg = str;
			}

			public void run() {
				monitor.subTask(msg);
				monitor.worked(1);
				Display.getDefault().update();
				
			}
		}
		
		private IProgressMonitor monitor;
		
		MigrationObserver() {
			monitor = ApplicationMediator.getAppMediator().getStatusLineManager().getProgressMonitor();
		    monitor.beginTask("Migrating", IProgressMonitor.UNKNOWN);
		}
		
		public void update(Observable obj, Object arg) {
			  Display.getDefault().syncExec(
					  new UpdateRunnable((String)arg));
		}
		
		public void endProgress() {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					monitor.done();					
				}
			});
		}		
	}
	
	private class MigrationProcess implements IRunnableWithProgress {

		private SAFRException me = null;
		
		public SAFRException getSAFRException() {
			return me;
		}

		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			try {
				migration.migrate();
			} catch (SAFRException e) {
				me = e;
			}			
		}
		
	}
	
	private class MigCompFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (currentSourceEnv != null) {
				return true;
			}
			return false;
		}

	}
	
    /**
     * This private class is used to provide sorting functionality for
     * Migration components table.
     */
    private class MigCompTableSorter extends ViewerSorter {

        public MigCompTableSorter() {
            super();
        }

        public int compare(Viewer viewer, Object obj1, Object obj2) {
            Table table = migCompTableViewer.getTable();
            MigrationComponent g1 = (MigrationComponent) obj1;
            MigrationComponent g2 = (MigrationComponent) obj2;
            int rc = 0;

            int index = Arrays.asList(table.getColumns()).indexOf(table.getSortColumn());
            switch (index) {
            case 0:
                rc = (new Boolean(g1.isSelected())).compareTo(g2.isSelected());
                break;
            case 1:
                String str1 = g1.getResult() != null ? g1.getResult()
                        .getLabel() : null;
                String str2 = g2.getResult() != null ? g2.getResult()
                        .getLabel() : null;
                rc = UIUtilities.compareStrings(str1, str2);
                break;
            case 2:
                Integer id1 = g1.getComponent().getId();
                Integer id2 = g2.getComponent().getId();
                rc = UIUtilities.compareIntegers(id1,id2);
                break;
            case 3:
                String str3 = g1.getComponent().getName();
                String str4 = g2.getComponent().getName();
                rc = UIUtilities.compareStrings(str3, str4);
                break;
            default:
                rc = 0;
            }

            // If the direction of sorting was descending previously,
            // reverse the direction of sorting
            if (table.getSortDirection() == SWT.DOWN) {
                rc = -rc;
            }
            return rc;
        }
    }
	
    /**
     * This private class is used for column sorting in the Migration component Table.
     */
    private class MigCompColSelectListen extends
            SelectionAdapter {

        public MigCompColSelectListen() {
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                getSite().getShell().setCursor(
                        getSite().getShell().getDisplay().getSystemCursor(
                                SWT.CURSOR_WAIT));
                TableColumn sortColumn = migCompTableViewer.getTable().getSortColumn();
                TableColumn currentColumn = (TableColumn) e.widget;
                int dir = migCompTableViewer.getTable().getSortDirection();
                if (sortColumn == currentColumn) {
                    dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                } else {
                    migCompTable.setSortColumn(currentColumn);
                    if (currentColumn == migCompTableViewer.getTable().getColumn(0)) {
                        dir = SWT.DOWN;
                    } else {
                        dir = SWT.UP;
                    }

                }
                migCompTable.setSortDirection(dir);
                migCompTableViewer.refresh();
                
                Order order = Order.ASCENDING;
                if (dir == SWT.DOWN) {
                    order = Order.DESCENDING;
                }
                SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE, 
                    (Integer)currentColumn.getData(SORT_TABLE), order);
                prefs.store();                                              
                
            } finally {
                getSite().getShell().setCursor(null);
            }
        }

    }

    private class MigCompContentProvider implements IStructuredContentProvider {
        
        private List<MigrationComponent> comps = new ArrayList<MigrationComponent>();
        
        public Object[] getElements(Object inputElement) {
            if (comps == null) {
                return new String[0];
            } else
                return comps.toArray();
        }

        public void dispose() {
        }

        @SuppressWarnings("unchecked")
        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
            comps = (List<MigrationComponent>)newInput; 
        }
       
    }
    
    private class MigCompLabelProvider extends ColumnLabelProvider implements ITableLabelProvider, ITableColorProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            MigrationComponent comp = (MigrationComponent) element;
            String text = null;

            switch (columnIndex) {
            case 0:
                text = "";
                break;
            case 1:
                if (comp.getResult() == null) {
                    text = "";
                } else {
                    text = comp.getResult().getLabel();
                }
                break;
            case 2:
                text = comp.getComponent().getIdLabel();
                break;
            case 3:
                text = comp.getComponent().getNameLabel();
                break;
            }

            return text;
        }

        public void addListener(ILabelProviderListener listener) {

        }

        public void dispose() {

        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {

        }
		@Override
		public Color getForeground(Object element, int columnIndex) {
			
        	System.out.println("venkatesh" + element.toString());
        	switch (columnIndex) {

            case 1:
            	if(element instanceof MigrationComponent){
            		MigrationComponent migcomp = (MigrationComponent) element;
            		if(migcomp!=null){
            			if(migcomp.getResult()!=null){
            				String str = migcomp.getResult().getLabel();
            				if(str!=null){
            					if(str.equals("Fail")){
                        			return new Color(Display.getDefault(),new RGB(255,0,0));
                        		}
            				}
            			}
            		}
            	}
            	break;
            }
        	
        	return new Color(Display.getDefault(),new RGB(0,0,0));
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}
    }    
	
	@Override
	public void createPartControl(Composite parent) {

		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FormLayout());
		form.setText("Migration Utility");

		createSectionComponentSelection(form.getBody());
        createSectionErrors(form.getBody());

		// Used to load the context sensitive help
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form.getBody(),
						"com.ibm.safr.we.help.ExportUtilityEditor");

		// Used to load the context sensitive help
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form.getBody(), "com.ibm.safr.we.help.MigrationEditor");

		form.reflow(true);		
	}

	private void createSectionComponentSelection(Composite body) {
		sectionComponentSelection = safrGuiToolkit.createSection(body,
				Section.TITLE_BAR, "Components to migrate");
		FormData dataSection = new FormData();
		
		dataSection.top = new FormAttachment(0, 10);
		dataSection.left = new FormAttachment(0, 5);
		// dataSectionComp.right = new FormAttachment(100, -5);
		dataSection.bottom = new FormAttachment(100, 0);
		
		sectionComponentSelection.setLayoutData(dataSection);

		compositeComponentSelectionSection = safrGuiToolkit.createComposite(
				sectionComponentSelection, SWT.NONE);
		compositeComponentSelectionSection.setLayout(new FormLayout());

		// create label Source Environment
		Label labelSourceEnv = safrGuiToolkit.createLabel(
				compositeComponentSelectionSection, SWT.NONE,
				"&Source Environment: ");
		FormData dataLabelSourceEnv = new FormData();
		dataLabelSourceEnv.top = new FormAttachment(0, 10);
		dataLabelSourceEnv.left = new FormAttachment(0, 10);
		dataLabelSourceEnv.width = 135;
		labelSourceEnv.setLayoutData(dataLabelSourceEnv);

		// create Nebula combo Source Environment
		comboSourceEnvironmentViewer = safrGuiToolkit
				.createTableComboForComponents(
						compositeComponentSelectionSection,
						ComponentType.Environment);
		comboSourceEnvironment = comboSourceEnvironmentViewer.getTableCombo();
		comboSourceEnvironment.setData(SAFRLogger.USER, "Source Environment");                              		
		FormData dataComboSourceEnvironment = new FormData();
		dataComboSourceEnvironment.top = new FormAttachment(0, 10);
		dataComboSourceEnvironment.left = new FormAttachment(labelSourceEnv, 10);
		dataComboSourceEnvironment.width = 375;
		comboSourceEnvironment.setLayoutData(dataComboSourceEnvironment);

		addSrcEnvOpenEditorMenu();
		
		// load data Source Environment
		try {
			// Show only envs which the user can access.
			sourceEnvList = SAFRQuery.queryEnvironmentsForLoggedInUser(
					SortType.SORT_BY_NAME, false);
		} catch (DAOException e1) {
			UIUtilities.handleWEExceptions(e1,
					"Error occurred while retrieving source environments.",
					UIUtilities.titleStringDbException);
		}

		comboSourceEnvironmentViewer.setInput(sourceEnvList);

		addListenerComboSourceEnvironment();

		// create label Target Environment
		labelTargetEnv = safrGuiToolkit.createLabel(
				compositeComponentSelectionSection, SWT.NONE,
				"&Target Environment: ");
		FormData dataLabelTargetEnv = new FormData();
		dataLabelTargetEnv.top = new FormAttachment(comboSourceEnvironment, 10);
		dataLabelTargetEnv.left = new FormAttachment(0, 10);
		dataLabelTargetEnv.width = 135;
		labelTargetEnv.setLayoutData(dataLabelTargetEnv);
		labelTargetEnv.setEnabled(false);

		// create Nebula combo Target Environment
		comboTargetEnvironmentViewer = safrGuiToolkit
				.createTableComboForComponents(
						compositeComponentSelectionSection,
						ComponentType.Environment);
		comboTargetEnvironment = comboTargetEnvironmentViewer.getTableCombo();
		comboTargetEnvironment.setData(SAFRLogger.USER, "Target Environment");                                    		
		FormData dataComboTargetEnvironment = new FormData();
		dataComboTargetEnvironment.top = new FormAttachment(
				comboSourceEnvironment, 10);
		dataComboTargetEnvironment.left = new FormAttachment(labelTargetEnv, 10);
		dataComboTargetEnvironment.width = 375;
		comboTargetEnvironment.setLayoutData(dataComboTargetEnvironment);
		comboTargetEnvironment.setEnabled(true);

		addTrgEnvOpenEditorMenu();
		
		// load data Target Environment list
		try {
			// Get only envs on which user has Admin or Migrate-In rights.
			List<EnvironmentQueryBean> tempList = SAFRQuery.queryEnvironmentsForLoggedInUser(
					SortType.SORT_BY_NAME, false);
			UserSession userSession = SAFRApplication.getUserSession();
			if (userSession.isSystemAdministrator()) {
				// user has Admin rights for all envs
				targetEnvList = tempList;
			} else {
				// include only EA or migrate-in rights
				for (EnvironmentQueryBean envQb : tempList) {
					if (userSession.isAdminOrMigrateInUser(envQb.getId())) {
						targetEnvList.add(envQb);
					}
				}
			}
		} catch (DAOException e1) {
			UIUtilities.handleWEExceptions(e1,
					"Error occurred while retrieving target environments.",
					UIUtilities.titleStringDbException);
		}

		addListenerComboTargetEnvironment();

		// create label Component Type
		labelComponentType = safrGuiToolkit.createLabel(
				compositeComponentSelectionSection, SWT.NONE,
				"&Component Type: ");
		FormData dataLabelComponentType = new FormData();
		dataLabelComponentType.top = new FormAttachment(comboTargetEnvironment,
				10);
		dataLabelComponentType.left = new FormAttachment(0, 10);
		dataLabelComponentType.width = 135;
		labelComponentType.setLayoutData(dataLabelComponentType);
		labelComponentType.setEnabled(false);

		// create SWT combo Component Type
		comboComponentType = safrGuiToolkit.createComboBox(
				compositeComponentSelectionSection, SWT.READ_ONLY, "");
		comboComponentType.setData(SAFRLogger.USER, "Component Type");                                            		
		FormData dataComboComponentType = new FormData();
		dataComboComponentType.top = new FormAttachment(comboTargetEnvironment,
				10);
		dataComboComponentType.left = new FormAttachment(labelComponentType, 10);
		dataComboComponentType.width = 350;
		comboComponentType.setLayoutData(dataComboComponentType);
		comboComponentType.setEnabled(false);

		addListenerComboComponentType();

		// create label Component
		labelMigComp = safrGuiToolkit.createLabel(
				compositeComponentSelectionSection, SWT.NONE, "Com&ponent: ");
		FormData dataLabelComponent = new FormData();
		dataLabelComponent.top = new FormAttachment(comboComponentType, 10);
		dataLabelComponent.left = new FormAttachment(0, 10);
		dataLabelComponent.width = 135;
		labelMigComp.setLayoutData(dataLabelComponent);
		labelMigComp.setEnabled(false);

        migCompTable = safrGuiToolkit.createTable(compositeComponentSelectionSection,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.CHECK | SWT.V_SCROLL, false);
        migCompTable.setData(SAFRLogger.USER, "Component Table");                                                                    
        migCompTableViewer = safrGuiToolkit
                .createCheckboxTableViewer(migCompTable);
        migCompTableViewer.addFilter(new MigCompFilter());
        FormData dataMigCompFiles = new FormData();
        dataMigCompFiles.left = new FormAttachment(labelMigComp, 10);
        dataMigCompFiles.top = new FormAttachment(comboComponentType, 10);
        dataMigCompFiles.right = new FormAttachment(100, 0);
        dataMigCompFiles.bottom = new FormAttachment(100, -40);
        dataMigCompFiles.height = 100;

        migCompTable.setLayoutData(dataMigCompFiles);
        migCompTable.setHeaderVisible(true);
        migCompTable.setLinesVisible(true);
        int counter = 0;
        
        migCompTable.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              
              if (event.detail == SWT.CHECK) {                  
                  int selRow = migCompTable.indexOf((TableItem)event.item);  
                  int stateMask=event.stateMask;                  
                  if((stateMask & SWT.SHIFT)==SWT.SHIFT){
                      int prevRow = prevSelection;
                      
                      if((stateMask & SWT.CTRL)!=SWT.CTRL){
                          migCompTableViewer.setAllChecked(false);
                          for (Object obj : migCompTableViewer.getCheckedElements()) {
                              ((MigrationComponent)obj).setSelected(false);
                          }                          
                      }
                      if (prevRow > selRow) {
                          for (int i=selRow ; i<=prevRow ; i++) {
                              Object element = migCompTableViewer.getElementAt(i);
                              migCompTableViewer.setChecked(element, true);
                              migComps.get(i).setSelected(true);
                          }
                      }
                      else {
                          for (int i=prevRow ; i<=selRow ; i++) {
                              Object element = migCompTableViewer.getElementAt(i);
                              migCompTableViewer.setChecked(element, true);
                              migComps.get(i).setSelected(true);                              
                          }                            
                      }
                  }   
                  else {
                      Object element = migCompTableViewer.getElementAt(selRow);
                      if (migCompTableViewer.getChecked(element)) {
                          prevSelection = migCompTable.indexOf((TableItem)event.item);
                      }
                      else {
                          prevSelection = 0;
                      }
                  }
              }                  
            }
          });

        migCompTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
            	EnvironmentQueryBean ebean = (EnvironmentQueryBean)((StructuredSelection) comboSourceEnvironmentViewer
                        .getSelection()).getFirstElement();                	
            	if (ebean.getId() == UIUtilities.getCurrentEnvironmentID()) {            	
	            	MigrationComponent node = (MigrationComponent)((StructuredSelection) event
	                        .getSelection()).getFirstElement();
	                if (node != null && node.getComponent() != null) {
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
        
        int length = migCompColumnHeaders.length;

        for (counter = 0; counter < length; counter++) {
            TableViewerColumn column = new TableViewerColumn(
                    migCompTableViewer, SWT.NONE);
            column.getColumn().setText(migCompColumnHeaders[counter]);
            column.getColumn().setToolTipText(migCompColumnHeaders[counter]);
            column.getColumn().setWidth(migCompColumnWidths[counter]);
            column.getColumn().setData(SORT_TABLE, counter);
            column.getColumn().setResizable(true);
            column.getColumn().addSelectionListener(new MigCompColSelectListen());
        }
		
        migCompTableViewer.setContentProvider(new MigCompContentProvider());
        migCompTableViewer.setLabelProvider(new MigCompLabelProvider());
        migCompTableViewer.setSorter(new MigCompTableSorter());
        
        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE);
        if (prefs.load()) {
            migCompTable.setSortColumn(migCompTable.getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                migCompTable.setSortDirection(SWT.UP);
            }
            else {
                migCompTable.setSortDirection(SWT.DOWN);
            }                   
        }
        else {
            migCompTable.setSortDirection(SWT.UP);
            migCompTable.setSortColumn(migCompTable.getColumn(3));
        }       
        
        addListenerMigCompComponent();
        
        // Add bottom row of buttons
        
        // create button Migrate
        buttonMigrate = safrGuiToolkit.createButton(
                compositeComponentSelectionSection, SWT.PUSH, "&Migrate");
        buttonMigrate.setData(SAFRLogger.USER, "Migrate");                                                                            
        FormData dataMigrateButton = new FormData();
        dataMigrateButton.top = new FormAttachment(migCompTable, 5);
        dataMigrateButton.bottom = new FormAttachment(100, -10);
        dataMigrateButton.right = new FormAttachment(100, 0);
        buttonMigrate.setLayoutData(dataMigrateButton);
        buttonMigrate.setSelection(false);
        buttonMigrate.setEnabled(false);

        addListenerButtonMigrate();
        
        // create checkbox Migrate Related
        checkboxMigrateRelated = safrGuiToolkit.createCheckBox(
                compositeComponentSelectionSection,
                "M&igrate Related");
        checkboxMigrateRelated.setData(SAFRLogger.USER, "Migrate Related");                                                                            
        FormData dataCheckboxMigrateRelated = new FormData();
        dataCheckboxMigrateRelated.top = new FormAttachment(migCompTable, 5);
        dataCheckboxMigrateRelated.bottom = new FormAttachment(100, -10);
        dataCheckboxMigrateRelated.right = new FormAttachment(buttonMigrate, -10);
        checkboxMigrateRelated.setLayoutData(dataCheckboxMigrateRelated);
        checkboxMigrateRelated.setSelection(false); 
        checkboxMigrateRelated.setEnabled(false);

        addListenerCheckboxMigrateRelated();
        
        // Refresh button
        buttonRefresh = safrGuiToolkit.createButton(compositeComponentSelectionSection,
                SWT.PUSH, "&Refresh");
        buttonRefresh.setData(SAFRLogger.USER, "Refresh");                                                                                    
        FormData dataRefresh = new FormData();
        dataRefresh.right = new FormAttachment(checkboxMigrateRelated, -80);
        dataRefresh.top = new FormAttachment(migCompTable, 5);
        dataRefresh.bottom = new FormAttachment(100, -10);
        buttonRefresh.setLayoutData(dataRefresh);
        buttonRefresh.setEnabled(false);

        addListenerButtonRefresh();
        
        buttonDeSelectAll = safrGuiToolkit.createButton(compositeComponentSelectionSection,
                SWT.PUSH, "&Deselect All");
        buttonDeSelectAll.setData(SAFRLogger.USER, "Deselect All");                                                                                            
        FormData dataDeSelectAll = new FormData();
        dataDeSelectAll.right = new FormAttachment(buttonRefresh, -10);
        dataDeSelectAll.top = new FormAttachment(migCompTable, 5);
        dataDeSelectAll.bottom = new FormAttachment(100, -10);
        buttonDeSelectAll.setLayoutData(dataDeSelectAll);
        buttonDeSelectAll.setEnabled(false);

        addListenerButtonDeSelectAll();
        
        buttonSelectAll = safrGuiToolkit.createButton(compositeComponentSelectionSection,
                SWT.PUSH, "&Select All");
        buttonSelectAll.setData(SAFRLogger.USER, "Select All");                                                                                                    
        FormData dataSelectAll = new FormData();
        dataSelectAll.right = new FormAttachment(buttonDeSelectAll, -10);
        dataSelectAll.top = new FormAttachment(migCompTable, 5);
        dataSelectAll.bottom = new FormAttachment(100, -10);
        dataSelectAll.left = new FormAttachment(labelMigComp, 10);
        buttonSelectAll.setLayoutData(dataSelectAll);
        buttonSelectAll.setEnabled(false);

        addListenerButtonSelectAll();
        
        // Set width of all buttons to the widest button
        List<Button> buttons = new ArrayList<Button>();
        buttons.add(buttonSelectAll);
        buttons.add(buttonDeSelectAll);
        buttons.add(buttonRefresh);
        buttons.add(buttonMigrate);
        int width = UIUtilities.computePreferredButtonWidth(buttons);
        dataSelectAll.width = width;
        dataDeSelectAll.width = width;
        dataRefresh.width = width;
        dataMigrateButton.width = width;

		sectionComponentSelection.setClient(compositeComponentSelectionSection);
		
        // Code for Context menu
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(migCompTableViewer.getTable());
        migCompTableViewer.getTable().setMenu(menu);
        getSite().registerContextMenu(menuManager, migCompTableViewer);        
        setPopupEnabled(false);     		
	}
	
    private void addSrcEnvOpenEditorMenu()
    {
        Text text = comboSourceEnvironment.getTextControl();
        Menu menu = text.getMenu();
        srcEnvOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        srcEnvOpenEditorItem.setText("Open Editor");
        srcEnvOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
            	EnvironmentQueryBean bean = (EnvironmentQueryBean)((StructuredSelection) comboSourceEnvironmentViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.Environment);                        
                }                
            }
        });
        
        comboSourceEnvironment.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                	EnvironmentQueryBean bean = (EnvironmentQueryBean)((StructuredSelection) comboSourceEnvironmentViewer
                            .getSelection()).getFirstElement();
                    if (bean != null) {   
                        srcEnvOpenEditorItem.setEnabled(true);                            
                    }
                    else {
                        srcEnvOpenEditorItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }       

    private void addTrgEnvOpenEditorMenu()
    {
        Text text = comboTargetEnvironment.getTextControl();
        Menu menu = text.getMenu();
        trgEnvOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        trgEnvOpenEditorItem.setText("Open Editor");
        trgEnvOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
            	EnvironmentQueryBean bean = (EnvironmentQueryBean)((StructuredSelection) comboTargetEnvironmentViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.Environment);                        
                }                
            }
        });
        
        comboTargetEnvironment.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                	EnvironmentQueryBean bean = (EnvironmentQueryBean)((StructuredSelection) comboTargetEnvironmentViewer
                            .getSelection()).getFirstElement();
                    if (bean != null) {   
                    	trgEnvOpenEditorItem.setEnabled(true);                            
                    }
                    else {
                    	trgEnvOpenEditorItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }       

    public MigrationComponent getCurrentSelection() {
        return (MigrationComponent)((StructuredSelection) migCompTableViewer.getSelection()).getFirstElement();
    }
    
	public ComponentType getCurrentComponentType() {
		return currentComponentType;
	}    
	
    private void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.MIGRATE);
        service.setMigrate(enabled);
    }
		
    private void createSectionErrors(Composite body) {

        errorsSection = safrGuiToolkit.createSection(body, Section.TITLE_BAR
                | Section.TWISTIE, "Errors");
        FormData dataErrors = new FormData();
        dataErrors.left = new FormAttachment(sectionComponentSelection, 5);
        dataErrors.top = new FormAttachment(0, 10);
        dataErrors.bottom = new FormAttachment(100, 0);
        dataErrors.right = new FormAttachment(100, -5);
        errorsSection.setLayoutData(dataErrors);

        errorsComposite = safrGuiToolkit.createComposite(errorsSection,
                SWT.NONE);
        errorsComposite.setLayout(new FormLayout());

        errorsTableViewer = safrGuiToolkit.createTableViewer(errorsComposite,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER, false);
        errorsTable = errorsTableViewer.getTable();
        errorsTable.setHeaderVisible(true);
        errorsTable.setLinesVisible(true);
        errorsTable.setLayout(new FormLayout());
        FormData dataTableErrors = new FormData();
        dataTableErrors.left = new FormAttachment(0, 5);
        dataTableErrors.top = new FormAttachment(0, 10);
        dataTableErrors.right = new FormAttachment(100, 0);
        dataTableErrors.bottom = new FormAttachment(100, -40);
        errorsTable.setLayoutData(dataTableErrors);

        TableViewerColumn column = new TableViewerColumn(errorsTableViewer,
                SWT.NONE);
        String columnHeaderErrors = "Errors";
        column.getColumn().setText(columnHeaderErrors);
        column.getColumn().setToolTipText(columnHeaderErrors);
        column.getColumn().setWidth(675);
        column.getColumn().setResizable(true);

        errorsTableViewer.setContentProvider(new IStructuredContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {

            }

            public void dispose() {

            }

            public Object[] getElements(Object inputElement) {
                if (migCompTableViewer.getSelection() == null) {
                    return new String[0];
                } else {
                    IStructuredSelection selection = (IStructuredSelection) migCompTableViewer
                            .getSelection();
                    MigrationComponent currModel = (MigrationComponent) selection
                            .getFirstElement();
                    if (currModel != null && currModel.hasError()) {
                    	return generateErrorMessage(currModel);
                    } else {
                    	return new String[0];
                    }
                }
            }
        });
        
        errorsTableViewer.setLabelProvider(new ColumnLabelProvider());
        errorsTableViewer.setInput(1);
        errorsSection.setClient(errorsComposite);
        showErrorsSection(false);
    }
	
    private String[] generateErrorMessage(MigrationComponent migComp) {
    	//List<String> msgList = new ArrayList<String>();
    	StringBuffer buffer = new StringBuffer();
    	
    	if (migComp.getMsgTopic() != null && migComp.getMsgTopic() != "") {
    		//msgList.add(migComp.getMsgTopic());
    		buffer.append(migComp.getMsgTopic() + SAFRUtilities.LINEBREAK);
    	}
    	if (migComp.getMainMsg() != null && migComp.getMainMsg() != "") {
    		//msgList.add(UIUtilities.splitMessage(migComp.getShortMsg()));
    		buffer.append(UIUtilities.splitMessage(migComp.getMainMsg()) + SAFRUtilities.LINEBREAK);
    	}
    	if (migComp.getDependencyMsg() != null && migComp.getDependencyMsg() != "") {
    		//msgList.add(migComp.getDetailMsg());
    		buffer.append(UIUtilities.splitMessage(migComp.getDependencyMsg()) + SAFRUtilities.LINEBREAK);
    	}
    	
    	String[] array = buffer.toString().split(SAFRUtilities.LINEBREAK);
    	return array;
    }
    
	private void addListenerComboSourceEnvironment() {

		comboSourceEnvironment.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (comboSourceEnvironment.getSelectionIndex() != selectedSourceEnvIdx) {
					selectedSourceEnvIdx = comboSourceEnvironment
							.getSelectionIndex();
					EnvironmentQueryBean environmentQueryBean = (EnvironmentQueryBean) comboSourceEnvironment
							.getTable().getSelection()[0].getData();
					if (environmentQueryBean != null) {
						currentSourceEnvID = environmentQueryBean.getId();
						currentSourceEnv = environmentQueryBean;
					}

					try {
						// Display hourglass cursor
						getSite().getShell().setCursor(
								getSite().getShell().getDisplay()
										.getSystemCursor(SWT.CURSOR_WAIT));

						// Exclude source env from the Target Env combo
						Integer countTgt = 0;
						if (targetEnvList != null) {
							subsetTargetEnvList.clear();
							for (EnvironmentQueryBean envQb : targetEnvList) {
								if (!envQb.getId().equals(currentSourceEnvID)) {
									subsetTargetEnvList.add(envQb);
								}
							}
							for (EnvironmentQueryBean enviromentQuerybean : subsetTargetEnvList) {
								comboTargetEnvironment.setData(
										Integer.toString(countTgt),
										enviromentQuerybean);
								countTgt++;
							}
						}
						comboTargetEnvironmentViewer
								.setInput(subsetTargetEnvList);
						comboTargetEnvironment.setEnabled(true);
						labelTargetEnv.setEnabled(true);
						
						// re-populate component list
						if (currentComponentType != null) {
							loadMigComp();
						} else {
							clearMigComp();
						}

					} finally {
						// restore original cursor
						getSite().getShell().setCursor(null);
					}

                    setButtonState();
				}
			}

		});
	}

	private void addListenerComboTargetEnvironment() {

		comboTargetEnvironment.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				// CQxxxxx test for selection change using env ID
				// rather than selection index because the list
				// data will change when a new src env is selected
				// so index comparison is no longer valid.
				Integer selectedTargetEnvId = null;
				if (comboTargetEnvironment.getSelectionIndex() > -1) {
					selectedTargetEnvId = ((EnvironmentQueryBean) comboTargetEnvironment
					.getTable().getSelection()[0].getData()).getId();
				}
				if (selectedTargetEnvId != currentTargetEnvID) {
					selectedTargetEnvIdx = comboTargetEnvironment
							.getSelectionIndex();
					EnvironmentQueryBean environmentQueryBean = null;
					// If source env is changed to same as current target
					// env, the target env combo will be reloaded and the
					// current selection cleared, so check for this.
					if (selectedTargetEnvIdx != -1) {
						environmentQueryBean = (EnvironmentQueryBean) comboTargetEnvironment
								.getTable().getSelection()[0].getData();
						currentTargetEnvID = environmentQueryBean.getId();
						currentTargetEnv = environmentQueryBean;
					} else {
						currentTargetEnvID = null;
						currentTargetEnv = null;
					}
					
					try {
						// Display hour glass cursor
						getSite().getShell().setCursor(
								getSite().getShell().getDisplay()
										.getSystemCursor(SWT.CURSOR_WAIT));

						if (currentSourceEnv != null && currentTargetEnv != null) {
							loadComponentTypeCombo();
						} else {
							clearComponentTypeCombo();
						}
						clearMigComp();

					} finally {
						// restore original cursor
						getSite().getShell().setCursor(null);
					}
					
					setButtonState();
				}
			}

		});
	}

	private void addListenerComboComponentType() {

		comboComponentType.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (comboComponentType.getSelectionIndex() != selectedComponentTypeIdx) {
					selectedComponentTypeIdx = comboComponentType.getSelectionIndex();
					currentComponentType = (ComponentType) comboComponentType.getData(
					    Integer.toString(comboComponentType.getSelectionIndex()));

					try {
						// Display hour glass cursor
						getSite().getShell().setCursor(
								getSite().getShell().getDisplay()
										.getSystemCursor(SWT.CURSOR_WAIT));

						loadMigComp();

					} finally {
						// restore original cursor
						getSite().getShell().setCursor(null);
					}
                    setButtonState();
				}
			}
		});
	}

    private void addListenerMigCompComponent() {

        migCompTable.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
            	// no op
            }
        });

        migCompTable.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                // no op
            }

        });
        
		migCompTableViewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                
                // select the new item
                MigrationComponent migComp = (MigrationComponent) event.getElement();
                if (event.getChecked()) {
                    migComp.setSelected(true);
                } else {
                    migComp.setSelected(false);
                }
                setButtonState();
            }
        });
        
		migCompTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

        	public void selectionChanged(SelectionChangedEvent event) {
        		MigrationComponent currModel = (MigrationComponent) ((IStructuredSelection) event
        				.getSelection()).getFirstElement();

        		if (currModel != null) {
        			showErrorsSection(currModel.hasError());
        		}
        	}
        });        
    }
	
	private void addListenerButtonMigrate() {
		buttonMigrate.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				for (Object obj : migCompTableViewer.getCheckedElements()) {
					migrate((MigrationComponent) obj);
				}
				MessageDialog.openInformation(getSite().getShell(),
						"Migration complete",
						"See the Result column.");
			}
		});
	}
	
	private void migrate(MigrationComponent migComp) {
		migration = new Migration(currentSourceEnv, currentTargetEnv,
				currentComponentType, migComp, currentMigrateRelated);
		migration.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(
				SAFRGUIContext.MIGRATE));
		migrationProgress = new MigrationObserver();
		migration.addObserver(migrationProgress);

		try {
			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			
			MigrationProcess migProc = new MigrationProcess();
			progressService.run(
				true,
				false,
				migProc);
			
			migCompTableViewer.refresh();
			
			SAFRException me = migProc.getSAFRException();
			if (me != null) {
				throw me;
			}
			
			migrationProgress.endProgress();
			
            // refresh navtree if view folder migrated
            if (currentComponentType.equals(ComponentType.ViewFolder)) {
                ApplicationMediator.getAppMediator().refreshNavigator();
            }
			migrateRefreshMetadataView();
	        UIUtilities.enableDisableMenuAsPerUserRights();
	        errorsTableViewer.refresh();

		} catch (SAFRCancelException sce) {
			// no op, Result is Cancel
		} catch (SAFRValidationException sve) {
			if (SAFRValidationType.PARAMETER_ERROR.equals(sve.getSafrValidationType())) {
				decorateEditor(sve);
				String msg = sve.getMessageString().replaceAll("&","&&");
				MessageDialog.openError(getSite().getShell(),
						"Migration parameter error", msg);
			}
			// else no op, Result is Fail
		} catch (SAFRDependencyException sde) {
            logger.log(Level.SEVERE, "Migration system error", sde);
			// no op, Result is Fail				
		} catch (SAFRException se) {
			// no op, Result is System Error
            logger.log(Level.SEVERE, "Migration system error", se);
		} catch (InvocationTargetException ie) {
            logger.log(Level.SEVERE, "Migration system error", ie);
			if ((ie.getCause()) != null) {
				if (ie.getCause() instanceof RuntimeException) {
					throw (RuntimeException)ie.getCause();
				} else {
					throw new RuntimeException(ie.getCause());
				}
			} else {
				throw new RuntimeException(ie);
			}
		} catch (InterruptedException ine) {
			logger.log(Level.SEVERE, "Migration system error", ine);
			MessageDialog.openError(getSite().getShell(),
					"Migration system error", "Operation interrupted. See log file for details.");
		} finally {
			migrationProgress.endProgress();
		}				
	}

    public void migrateRefreshMetadataView() {
        switch(currentComponentType){
        case ControlRecord:
            ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.ControlRecord, null);   
            break;
        case UserExitRoutine:
            ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.UserExitRoutine, null);
            break;
        case PhysicalFile:
            ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.PhysicalFile, null);
            break;
        case LogicalFile:
            ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.LogicalFile, null);
            break;
        case LogicalRecord:
            ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.LogicalRecord, null);
            break;
        case LookupPath:
            ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.LookupPath, null);
            break;
        case View:
            ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.View, 0);
            break;
        case ViewFolder:
            for (MigrationComponent migComp : migComps) {
                ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.ViewFolder, 
                    migComp.getComponent().getId());                
            }
            break;
        default:
        }            
    }
	
	private void addListenerCheckboxMigrateRelated() {

		checkboxMigrateRelated.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if (checkboxMigrateRelated.getSelection()) {
					currentMigrateRelated = true;
				} else {
					currentMigrateRelated = false;
				}
				setButtonState();				
			}

		});
	}

	private void addListenerButtonRefresh() {
		
        buttonRefresh.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                // no op
            }

            public void widgetSelected(SelectionEvent e) {
            	showErrorsSection(false);
            	loadMigComp();
            	migCompTableViewer.refresh();
            	migCompTableViewer.setAllChecked(false);
            	prevSelection = 0;
                setButtonState();            	
            }

        });
        
	}
	
	private void addListenerButtonDeSelectAll() {
		
        buttonDeSelectAll.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            	// no op
            }

            public void widgetSelected(SelectionEvent e) {

            	for (Object obj : migCompTableViewer.getCheckedElements()) {
            		// uncheck any checked elements
            		((MigrationComponent)obj).setSelected(false);
            	}
            	migCompTableViewer.setAllChecked(false);
            	prevSelection = 0;
            	setButtonState();
            }

        });        
	}
	
	private void addListenerButtonSelectAll() {
		
        buttonSelectAll.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            	// no op
            }

            public void widgetSelected(SelectionEvent e) {
            	for (MigrationComponent mcomp : migComps) {
            		mcomp.setSelected(true);
            	}
            	migCompTableViewer.setAllChecked(true);        	
                setButtonState();
                prevSelection = 0;                              
            }

        });
	}
	
	/*
	 * Load the Target View Folder combo with the View Folders in the Target
	 * Environment and reset it for display. Assumes that the target env has
	 * been selected and the comp type is VF.
	 */
	/**
	 * Populates the Component Type combo with the component types that can be
	 * migrated. A System Administrator or an Environment Administrator of the
	 * source environment can migrate all component types.  An ordinary user
	 * of the source environment cannot migrate Control Records
	 * or Lookup Paths.
	 * 
	 */
	private void loadComponentTypeCombo() {
		
		comboComponentType.clearSelection();
		comboComponentType.removeAll();

		// Must have admin/migrate in rights on target env to migrate CR, GF or LK		
		Integer counter = 0;
		boolean isAdminMigrate = UIUtilities.isAdminOrMigrateInUser(currentTargetEnv.getId()); 
		if (isAdminMigrate) {
			comboComponentType.add("Control Record");
			comboComponentType.setData(Integer.toString(counter++),
					ComponentType.ControlRecord);
		}
		
		comboComponentType.add("User-Exit Routine");
		comboComponentType.setData(Integer.toString(counter++),
				ComponentType.UserExitRoutine);

		comboComponentType.add("Physical File");
		comboComponentType.setData(Integer.toString(counter++),
				ComponentType.PhysicalFile);

		comboComponentType.add("Logical File");
		comboComponentType.setData(Integer.toString(counter++),
				ComponentType.LogicalFile);

		comboComponentType.add("Logical Record");
		comboComponentType.setData(Integer.toString(counter++),
				ComponentType.LogicalRecord);

		if (isAdminMigrate) {
			comboComponentType.add("Lookup Path");
			comboComponentType.setData(Integer.toString(counter++),
					ComponentType.LookupPath);
		}
		
		comboComponentType.add("View");
		comboComponentType.setData(Integer.toString(counter++),
				ComponentType.View);

		comboComponentType.add("View Folder");
		comboComponentType.setData(Integer.toString(counter++),
		ComponentType.ViewFolder);
		
		comboComponentType.deselectAll();
		comboComponentType.select(-1);
		selectedComponentTypeIdx = comboComponentType.getSelectionIndex();
		currentComponentType = null;
		comboComponentType.setEnabled(true);
		labelComponentType.setEnabled(true);

	}

	/**
	 * Clear the Component Type combo
	 */
	private void clearComponentTypeCombo() {
		comboComponentType.deselectAll();
		comboComponentType.select(-1);
		selectedComponentTypeIdx = comboComponentType.getSelectionIndex();
		comboComponentType.removeAll();
		currentComponentType = null;
		comboComponentType.setEnabled(false);
		labelComponentType.setEnabled(false);
	}

	/**
	 * Loads the Component combo with a list of components of a particular type
	 * 
	 */
	private void loadMigComp() {
		try {
	        migComps.clear();
	        
			switch (currentComponentType) {
			case ControlRecord:
				List<ControlRecordQueryBean> crList = SAFRQuery
						.queryAllControlRecords(currentSourceEnvID,	
								SortType.SORT_BY_NAME);
				for (ControlRecordQueryBean bean : crList) {
				    migComps.add(new MigrationComponent(bean));
				}
				break;
			case LookupUserExitRoutine:
			case ReadUserExitRoutine:
			case FormatUserExitRoutine:
			case WriteUserExitRoutine:
			case UserExitRoutine:
				List<UserExitRoutineQueryBean> uxrList = SAFRQuery
						.queryAllUserExitRoutines(currentSourceEnvID,
								SortType.SORT_BY_NAME);
                for (UserExitRoutineQueryBean bean : uxrList) {
                    migComps.add(new MigrationComponent(bean));
                }               
				break;
			case PhysicalFile:
				List<PhysicalFileQueryBean> pfList = SAFRQuery
						.queryAllPhysicalFiles(currentSourceEnvID,
								SortType.SORT_BY_NAME);
                for (PhysicalFileQueryBean bean : pfList) {
                    migComps.add(new MigrationComponent(bean));
                }               
				break;
			case LogicalFile:
				List<LogicalFileQueryBean> lfList = SAFRQuery
						.queryAllLogicalFiles(currentSourceEnvID,
								SortType.SORT_BY_NAME);
                for (LogicalFileQueryBean bean : lfList) {
                    migComps.add(new MigrationComponent(bean));
                }               
				break;
			case LogicalRecord:
				List<LogicalRecordQueryBean> lrList = SAFRQuery
						.queryAllActiveLogicalRecords(currentSourceEnvID,
								SortType.SORT_BY_NAME);
                for (LogicalRecordQueryBean bean : lrList) {
                    migComps.add(new MigrationComponent(bean));
                }               
				break;
			case LookupPath:
				List<LookupQueryBean> lkList = SAFRQuery.queryAllLookups(
						currentSourceEnvID, SortType.SORT_BY_NAME);
				for(LookupQueryBean lk : lkList) {
					if ( lk.getValidInd() > 0) {
						migComps.add(new MigrationComponent(lk));
					}
				}
				break;
			case View:
				List<ViewQueryBean> vwList = SAFRQuery.queryAllViews(
						currentSourceEnvID, SortType.SORT_BY_NAME);
				for(ViewQueryBean vw : vwList) {
					if ( vw.getStatus().equals(ACTIVE) ) {
						migComps.add(new MigrationComponent(vw));
					}
				}
				break;
			case ViewFolder:
				List<ViewFolderQueryBean> vfList = SAFRQuery
						.queryAllViewFolders(currentSourceEnvID,
								SortType.SORT_BY_NAME);
                for (ViewFolderQueryBean bean : vfList) {
                    migComps.add(new MigrationComponent(bean));
                }
			case AllComponents:
				break;
			case Environment:
				break;
			case Group:
				break;
			case LogicalRecordField:
				break;
			case User:
				break;
			default:
				break;               
			}

            migCompTableViewer.setInput(migComps);
            migCompTableViewer.refresh();
			
		} catch (DAOException e1) {
			UIUtilities.handleWEExceptions(e1,
			    "Error occurred while retrieving components from source environment.",UIUtilities.titleStringDbException);
		}

		migCompTable.setEnabled(true);
		labelMigComp.setEnabled(true);
		showErrorsSection(false);
	}  

    /**
     * Clear the MigComp table.
     */
    private void clearMigComp() {
		migCompTable.select(-1);
        migCompTableViewer.setInput(null);
        migCompTableViewer.refresh();
        labelMigComp.setEnabled(false);
        migComps.clear();
    }	   
	
	private void setButtonState() {

        // Migrate Related check box
	    // is important we sort out MigrateRelated first as it affects selection mode which we 
	    // use for other buttons.
        // is enabled when certain component types are selected i.e. PF, LF, LR, LU, View
        // when VF is selected and a target view folder is selected then select and disable the button
        // otherwise enable it
        boolean enabled;
		if (currentComponentType == ComponentType.PhysicalFile
				|| currentComponentType == ComponentType.LogicalFile
				|| currentComponentType == ComponentType.LogicalRecord
				|| currentComponentType == ComponentType.LookupPath
				|| currentComponentType == ComponentType.View
				|| currentComponentType == ComponentType.ViewFolder) {
			// Migrate Related is optional for these comp types
			enabled = true;
		} else {
			// Migrate Related is not applicable for CR, GF, UX
			checkboxMigrateRelated.setSelection(false);
			enabled = false;
		}
        checkboxMigrateRelated.setEnabled(enabled);
        currentMigrateRelated = checkboxMigrateRelated.getSelection();
        	    
        // Refresh buttons
        // enabled when component type has been selected
        // however if component type view is selected then view folder must be selected
		if (currentComponentType == null) {
			enabled = false;
		} else {
			enabled = true;
		}
		if (enabled) {
			buttonRefresh.setEnabled(true);
			buttonSelectAll.setEnabled(true);
			buttonDeSelectAll.setEnabled(true);
		} else {
			buttonRefresh.setEnabled(false);
            buttonSelectAll.setEnabled(false);
            buttonDeSelectAll.setEnabled(false);
		}
	    
	    // Migrate button
	    // is enabled when one or more components are checked 
	    // if component type is view then view folder must be selected as well
		if (currentComponentType == null
				|| migCompTableViewer.getCheckedElements().length == 0) {
			enabled = false;
		} else {
			enabled = true;
		}
		buttonMigrate.setEnabled(enabled);
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

	}

	@Override
	public void storeModel() throws DAOException, SAFRException {

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
		// Auto-generated method stub
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
	
	/**
	 * This method is used to display the errors section expanded or enabled
	 * based upon the value passed
	 * 
	 * @param visible
	 */
	private void showErrorsSection(Boolean visible) {
		errorsSection.setExpanded(visible);
		errorsSection.setEnabled(visible);
		if (visible) {
			errorsTableViewer.refresh();
		}
	}
	
}
