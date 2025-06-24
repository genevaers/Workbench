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


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
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
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRFactory;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.diff.DiffBaseNode;
import com.ibm.safr.we.model.diff.DiffBaseNode.DiffNodeState;
import com.ibm.safr.we.model.diff.DiffField;
import com.ibm.safr.we.model.diff.DiffFieldReference;
import com.ibm.safr.we.model.diff.DiffFieldValue;
import com.ibm.safr.we.model.diff.DiffFieldValue.OtherValue;
import com.ibm.safr.we.model.diff.DiffNode;
import com.ibm.safr.we.model.diff.DiffNodeComp;
import com.ibm.safr.we.model.diff.DiffNodeFactory;
import com.ibm.safr.we.model.diff.DiffNodeSection;
import com.ibm.safr.we.model.query.ControlRecordQueryBean;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.NumericIdQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.Application;
import com.ibm.safr.we.ui.dialogs.DependencyMessageDialog;
import com.ibm.safr.we.ui.utilities.ColumnSelectionListenerForTableCombo;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.ControlRecordTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.LogicalFileTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.LogicalRecordTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.LookupTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.MainTableLabelProvider;
import com.ibm.safr.we.ui.views.metadatatable.PhysicalFileTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.UserExitRoutineTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.ViewFolderTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.ViewTableSorter;
import com.ibm.safr.we.utilities.ProfileLocation;

public class DiffUtilityEditor extends SAFREditorPart implements IPartListener2 {

    public class CollapseAction extends Action {

        public CollapseAction() {
            super("Collapse All", AS_PUSH_BUTTON);
            setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL));
            setEnabled(false);
        }
        
        @Override
        public void run() {
            diffTreeViewer.collapseAll();                
        }        
    }

    public class ExpandAction extends Action {

        public ExpandAction() {
            super("Expand All", AS_PUSH_BUTTON);
            setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID, ImageKeys.EXPANDALL));
            setEnabled(false);
        }
        
        @Override
        public void run() {
            diffTreeViewer.expandAll();                
        }        
    }
    
    public enum BackColourState {
        LIGHTFIRST, LIGHTSECOND, DARKFIRST, DARKSECOND;
    };
    
    public class DiffTreeLabelProvider extends CellLabelProvider  
    {
        BackColourState backState = BackColourState.LIGHTFIRST;
        FontRegistry registry = new FontRegistry();
        Color lightGrey = new Color(Display.getCurrent(), 245, 245, 245);

        public DiffTreeLabelProvider() {
            registry.put("code", new FontData[]{new FontData("Courier New", 10, SWT.NORMAL)});        
        }
        
        @Override
        public void update(ViewerCell cell) {
            DiffBaseNode node = (DiffBaseNode)cell.getElement();
            if (node instanceof DiffField) {
                if (node.getState().equals(DiffNodeState.Different)) {
                    switch (backState) {
                    case LIGHTFIRST:
                        cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
                        backState = BackColourState.LIGHTSECOND;
                        break;
                    case LIGHTSECOND:
                        cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
                        backState = BackColourState.DARKFIRST;
                        break;
                    case DARKFIRST:
                        cell.setBackground(lightGrey);                        
                        backState = BackColourState.DARKSECOND;
                        break;
                    case DARKSECOND:
                        cell.setBackground(lightGrey);                        
                        backState = BackColourState.LIGHTFIRST;
                        break;
                    default: 
                    }
                }                
            }
            if (node instanceof DiffNode) {                
                cell.setText(node.toString());
                if (node.getState().equals(DiffNodeState.Added) || node.getState().equals(DiffNodeState.Removed)) {
                    cell.setFont(registry.getBold("code"));                                        
                }
                else {
                    cell.setFont(registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName()));                    
                }
            }
            else if (node instanceof DiffFieldValue) {
                DiffFieldValue val = (DiffFieldValue)node;
                String text = "";
                if (val.isLarge()) {
                    // get first line of large string
                    text = node.toString();
                    if (text.contains("\n")) {
                        text = text.substring(0, text.indexOf('\n')-1);
                    }
                    if (!val.getValue().isEmpty()) {
                        text += " ...";
                    }
                }
                else {
                    text = node.toString();
                }
                if (val.getState().equals(DiffNodeState.Different)) {
                    cell.setText(String.format("%1$-85s", text));  
                    cell.setFont(registry.get("code"));
                }
                else if (val.getState().equals(DiffNodeState.Added) || val.getState().equals(DiffNodeState.Removed)) {
                    cell.setText(text);
                    cell.setFont(registry.get("code"));                    
                }
                else {
                    cell.setText(text);
                    cell.setFont(Display.getCurrent().getSystemFont());
                    cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));                    
                }
            }
            else if (node instanceof DiffFieldReference && !node.getState().equals(DiffNodeState.Same)) {
                if (node.getState().equals(DiffNodeState.Different)) {
                    cell.setText(String.format("%1$-85s", node.toString()));                      
                }
                else {
                    cell.setText(node.toString());                    
                }
                cell.setFont(registry.get("code"));                                    
            }
            else {
                cell.setText(node.toString());
                cell.setFont(Display.getCurrent().getSystemFont());
                cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));                                    
            }
            switch (node.getState()) {
            case Same:
                cell.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
                break;
            case Added:
                cell.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
                break;
            case Removed:
                cell.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
                break;
            case Different:
                cell.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
                break;
            default:
                break;
            }                            
        }
    }

    public class DiffTreeContentProvider implements ITreeContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            DiffNode node = (DiffNode)inputElement;
            return node.getChildObjects();
        }

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof DiffNode) {
                DiffNode node = (DiffNode)parentElement;
                List<Object> result = new ArrayList<Object>();
                for (Object childObj : node.getChildObjects()) {
                    if (childObj instanceof DiffNode) {
                        DiffNode dnode = (DiffNode)childObj;
                        if (dnode.childHasState(filter)) {
                            result.add(childObj);                            
                        }
                    }
                    else {
                        if (((DiffBaseNode)childObj).hasState(filter)) {
                            result.add(childObj);
                        }
                    }
                }
                return result.toArray();
            }
            else {
                return null;
            }
        }

        public Object getParent(Object element) {
            DiffBaseNode node = (DiffBaseNode)element;
            return node.getParent();
        }

        public boolean hasChildren(Object element) {
            if (element instanceof DiffNode) {
                DiffNode node = (DiffNode)element;
                if (node.getChildObjects().length == 0) {
                    return false;
                }
                else {
                    return node.childHasState(filter);
                }
            }
            else {
                return false;
            }
        }
    }

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.DiffUtilityEditor");
    
    public final static String ID = "SAFRWE.DiffUtilityEditor";
    
	private ScrolledForm form;
	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;
	private String headerNote = "Use this utility to compare two components.";
	
	// criteria
    private Combo comboComponentType;
	private Section sectionDiffCriteria;
	private TableCombo comboEnvironmentLHS;
	protected EnvironmentQueryBean currentEnvironmentLHS;
	protected Integer currentEnvIDLHS;
    private TableComboViewer comboEnvironmentViewerLHS;
    private TableCombo comboEnvironmentRHS;
    protected EnvironmentQueryBean currentEnvironmentRHS;
    protected Integer currentEnvIDRHS;
    private TableComboViewer comboEnvironmentViewerRHS;
	private TableCombo comboComponent;
	private TableComboViewer comboComponentViewer;
	protected ComponentType componentType;
    private Button buttonDiff;
    private Button buttonRelated;
    private Button buttonWE;
	private Button buttonExport;
	protected int selectedComponentType = -1;
	protected EnvironmentalQueryBean currentComponent;
	private ColumnSelectionListenerForTableCombo[] prvsListner = new ColumnSelectionListenerForTableCombo[2];
	private Text textLocation;
	private Button buttonLocation;
	protected DirectoryDialog dialogLocation;
	private Section sectionExport;
    private MenuItem compOpenEditorItem = null;
		
	// results
	private Section sectionDiffResults;
    private TreeViewer diffTreeViewer;
    private DiffNode root = null;
    private CollapseAction colAct = null;
    private ExpandAction expAct = null;
    
    // filter
    private Section sectionFilter;
	private Set<DiffNodeState> filter; 
    private Button chkboxSame;
    private Button chkboxChg;
    private Button chkboxAdd;
    private Button chkboxRem;
	

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FormLayout());
		form.setText("Compare Utility");

		Label labelHeaderNote = safrGuiToolkit.createLabel(form.getBody(),
				SWT.NONE, headerNote);
		labelHeaderNote.setForeground(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_GRAY));
		FormData dataLabelHeaderNote = new FormData();
		dataLabelHeaderNote.top = new FormAttachment(0, 0);
		dataLabelHeaderNote.left = new FormAttachment(0, 5);
		labelHeaderNote.setLayoutData(dataLabelHeaderNote);

		createSectionDiffCriteria(form.getBody());
		createSectionDiffResult(form.getBody());
		form.reflow(true);
		
        getSite().getPage().addPartListener(this);
	}

	private void createSectionDiffCriteria(Composite body) {
		sectionDiffCriteria = safrGuiToolkit.createSection(body,
				Section.TITLE_BAR, "Compare Criteria");
		FormData dataSectionDiffCriteria = new FormData();
		dataSectionDiffCriteria.top = new FormAttachment(0, 20);
		dataSectionDiffCriteria.left = new FormAttachment(0, 5);
		dataSectionDiffCriteria.right = new FormAttachment(38, -5);
		sectionDiffCriteria.setLayoutData(dataSectionDiffCriteria);

		Composite compositeDiffCriteria = safrGuiToolkit.createComposite(
				sectionDiffCriteria, SWT.NONE);
		compositeDiffCriteria.setLayout(new FormLayout());
		FormData dataCompositeDiffCriteria = new FormData();
		dataCompositeDiffCriteria.top = new FormAttachment(0, 0);
		dataCompositeDiffCriteria.left = new FormAttachment(0, 0);
		dataCompositeDiffCriteria.right = new FormAttachment(0, 0);
		compositeDiffCriteria
				.setLayoutData(dataCompositeDiffCriteria);
		sectionDiffCriteria.setClient(compositeDiffCriteria);

		Label labelEnvironmentsLHS = safrGuiToolkit.createLabel(
				compositeDiffCriteria, SWT.NONE, "Environment &1: ");
		FormData dataLabelEnvironmentsLHS = new FormData();
		dataLabelEnvironmentsLHS.top = new FormAttachment(0, 10);
		dataLabelEnvironmentsLHS.left = new FormAttachment(0, 5);
		labelEnvironmentsLHS.setLayoutData(dataLabelEnvironmentsLHS);

		comboEnvironmentViewerLHS = safrGuiToolkit.createTableComboForComponents(
				compositeDiffCriteria, ComponentType.Environment);

		comboEnvironmentLHS = comboEnvironmentViewerLHS.getTableCombo();
		FormData dataComboEnvironmentsLHS = new FormData();
		dataComboEnvironmentsLHS.top = new FormAttachment(0, 10);
		dataComboEnvironmentsLHS.left = new FormAttachment(labelEnvironmentsLHS, 40);
		dataComboEnvironmentsLHS.width = 375;
		comboEnvironmentLHS.setLayoutData(dataComboEnvironmentsLHS);

		populateEnvironment(comboEnvironmentViewerLHS, comboEnvironmentLHS, SAFRApplication.getUserSession()
				.getUser());
		comboEnvironmentLHS.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				try {
					if (comboEnvironmentLHS.getTable().getSelection().length > 0) {
						currentEnvironmentLHS = (EnvironmentQueryBean) comboEnvironmentLHS
							.getTable().getSelection()[0].getData();
					}
					else {
						currentEnvironmentLHS = null;
					}
					if (currentEnvironmentLHS != null) {
						currentEnvIDLHS = currentEnvironmentLHS.getId();
						comboComponent.getTable().removeAll();
						comboComponent.select(-1);
						comboComponent.redraw();
						setResultsState(false);
						buttonDiff.setEnabled(false);
					}
					getSite().getShell().setCursor(getSite().getShell().getDisplay()
						.getSystemCursor(SWT.CURSOR_WAIT));
					if (componentType != null) {
						populateComponentList();
					}
				} finally {
					getSite().getShell().setCursor(null);
				}
			}

		});

		comboEnvironmentLHS.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                diffTreeViewer.setSelection(null);				    
                diffTreeViewer.setInput(null);
                diffTreeViewer.refresh();				    
				setResultsState(false);
				comboComponent.getTable().removeAll();
				comboComponent.select(-1);
				comboComponent.redraw();
			}
		});

        Label labelEnvironmentsRHS = safrGuiToolkit.createLabel(
            compositeDiffCriteria, SWT.NONE, "Environment &2: ");
        FormData dataLabelEnvironmentsRHS = new FormData();
        dataLabelEnvironmentsRHS.top = new FormAttachment(comboEnvironmentLHS, 10);
        dataLabelEnvironmentsRHS.left = new FormAttachment(0, 5);
        labelEnvironmentsRHS.setLayoutData(dataLabelEnvironmentsRHS);

        comboEnvironmentViewerRHS = safrGuiToolkit.createTableComboForComponents(
            compositeDiffCriteria, ComponentType.Environment);

        comboEnvironmentRHS = comboEnvironmentViewerRHS.getTableCombo();
        FormData dataComboEnvironmentsRHS = new FormData();
        dataComboEnvironmentsRHS.top = new FormAttachment(comboEnvironmentLHS, 10);
        dataComboEnvironmentsRHS.left = new FormAttachment(labelEnvironmentsLHS, 40);
        dataComboEnvironmentsRHS.width = 375;
        comboEnvironmentRHS.setLayoutData(dataComboEnvironmentsRHS);

        populateEnvironment(comboEnvironmentViewerRHS, comboEnvironmentRHS, SAFRApplication.getUserSession().getUser());
        comboEnvironmentRHS.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                try {
                	if (comboEnvironmentRHS.getTable().getSelection().length > 0) {
                		currentEnvironmentRHS = (EnvironmentQueryBean) comboEnvironmentRHS
                            .getTable().getSelection()[0].getData();
                	}
                	else {
                		currentEnvironmentRHS = null;
                	}
                    if (currentEnvironmentRHS != null) {
                        currentEnvIDRHS = currentEnvironmentRHS.getId();
                        comboComponent.getTable().removeAll();
                        comboComponent.select(-1);
                        comboComponent.redraw();
                        setResultsState(false);
                        buttonDiff.setEnabled(false);
                    }
                    getSite().getShell().setCursor(getSite().getShell().getDisplay()
                        .getSystemCursor(SWT.CURSOR_WAIT));
                    if (componentType != null) {
                        populateComponentList();
                    }
                } finally {
                    getSite().getShell().setCursor(null);
                }
            }
    
        });
    
        comboEnvironmentRHS.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                diffTreeViewer.setSelection(null);                    
                diffTreeViewer.setInput(null);
                diffTreeViewer.refresh();
                setResultsState(false);
                comboComponent.getTable().removeAll();
                comboComponent.select(-1);
                comboComponent.redraw();
            }
        });
        
		Label labelComponentType = safrGuiToolkit.createLabel(
				compositeDiffCriteria, SWT.NONE, "Component &Type:");
		FormData dataLabelComponentType = new FormData();
		dataLabelComponentType.width = SWT.DEFAULT;
		dataLabelComponentType.top = new FormAttachment(comboEnvironmentRHS, 10);
		dataLabelComponentType.left = new FormAttachment(0, 5);
		labelComponentType.setLayoutData(dataLabelComponentType);

		comboComponentType = safrGuiToolkit.createComboBox(
				compositeDiffCriteria, SWT.READ_ONLY, "");
		FormData dataComboComponentType = new FormData();
		dataComboComponentType.left = new FormAttachment(labelEnvironmentsLHS, 40);
		dataComboComponentType.top = new FormAttachment(comboEnvironmentRHS, 10);
		dataComboComponentType.width = 350;
		comboComponentType.setLayoutData(dataComboComponentType);

		int i = 0;
        comboComponentType.add("Control Record");
        comboComponentType.setData(String.valueOf(i++),ComponentType.ControlRecord);		
		comboComponentType.add("Logical File");
		comboComponentType.setData(String.valueOf(i++),ComponentType.LogicalFile);
		comboComponentType.add("Logical Record");
		comboComponentType.setData(String.valueOf(i++),ComponentType.LogicalRecord);
        comboComponentType.add("Lookup Path");
        comboComponentType.setData(String.valueOf(i++),ComponentType.LookupPath);
		comboComponentType.add("Physical File");
		comboComponentType.setData(String.valueOf(i++),ComponentType.PhysicalFile);
		comboComponentType.add("User-Exit Routine");
		comboComponentType.setData(String.valueOf(i++),ComponentType.UserExitRoutine);
		comboComponentType.add("View");
		comboComponentType.setData(String.valueOf(i++), ComponentType.View);
        comboComponentType.add("View Folder");
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
						
						buttonDiff.setEnabled(false);						
						comboComponent.getTable().removeAll();
						comboComponent.select(-1);
						comboComponent.setEnabled(true);
					    populateComponentList();

						buttonExport.setEnabled(false);

						comboComponent.setFocus();
						selectedComponentType = comboComponentType.getSelectionIndex();
					}
				} finally {
					getSite().getShell().setCursor(null);
				}
			}
		});
		comboComponentType.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!(comboComponentType.getText().equals(selectedComponentType))) {
                    diffTreeViewer.setSelection(null);				    
                    diffTreeViewer.setInput(null);
                    diffTreeViewer.refresh();
					setResultsState(false);
				}
			}
		});

        buttonRelated = safrGuiToolkit.createButton(compositeDiffCriteria,
            SWT.CHECK, "C&ompare Related Components");
        FormData dataCompareRelated = new FormData();
        dataCompareRelated.top = new FormAttachment(comboComponentType, 10);
        dataCompareRelated.left = new FormAttachment(labelEnvironmentsLHS, 40);
        buttonRelated.setLayoutData(dataCompareRelated);
        buttonRelated.setSelection(DiffNode.isRelated());
        buttonRelated.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                DiffNode.setRelated(buttonRelated.getSelection());
            }
        });
                		
        buttonWE = safrGuiToolkit.createButton(compositeDiffCriteria,
            SWT.CHECK, "Compare Non &VDP Fields");
        FormData dataCompareWE = new FormData();
        dataCompareWE.top = new FormAttachment(buttonRelated, 10);
        dataCompareWE.left = new FormAttachment(labelEnvironmentsLHS, 40);
        buttonWE.setLayoutData(dataCompareWE);
        buttonWE.setSelection(DiffNode.isWeFields());
        buttonWE.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                DiffNode.setWEFields(buttonWE.getSelection());
            }
        });
        
		Label labelComponent = safrGuiToolkit.createLabel(
				compositeDiffCriteria, SWT.NONE, "Compo&nent:");
		FormData dataLabelComponent = new FormData();
		dataLabelComponent.left = new FormAttachment(0, 5);
		dataLabelComponent.top = new FormAttachment(buttonWE, 10);
		labelComponent.setLayoutData(dataLabelComponent);

		comboComponentViewer = safrGuiToolkit
				.createTableComboForComponents(compositeDiffCriteria);
		comboComponent = comboComponentViewer.getTableCombo();
		FormData dataComboFind = new FormData();
		dataComboFind.top = new FormAttachment(buttonWE, 10);
		dataComboFind.left = new FormAttachment(labelEnvironmentsLHS, 40);
		dataComboFind.width = 375;
		comboComponent.setLayoutData(dataComboFind);
		comboComponent.setEnabled(false);
		addCompOpenEditorMenu();
		
		comboComponent.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					getSite().getShell().setCursor(getSite().getShell().getDisplay()
							.getSystemCursor(SWT.CURSOR_WAIT));
					
					if (comboComponent.getTable().getSelection().length > 0) {
						if (!comboComponent.getTable().getSelection()[0].getData().equals(currentComponent)) {
						    diffTreeViewer.setSelection(null);
		                    diffTreeViewer.setInput(null);
		                    diffTreeViewer.refresh();
						}
	                    EnvironmentalQueryBean previousComponent = currentComponent;
						currentComponent = (EnvironmentalQueryBean) comboComponent.getTable().getSelection()[0].getData();
						if (!currentComponent.equals(previousComponent)) {
							setResultsState(false);
							buttonDiff.setEnabled(true);
						}
					}

				} finally {
					getSite().getShell().setCursor(null);
				}
			}
		});

		buttonDiff = safrGuiToolkit.createButton(compositeDiffCriteria,
				SWT.PUSH, "&Compare");
		FormData dataCompare = new FormData();
		dataCompare.top = new FormAttachment(comboComponent, 10);
		dataCompare.right = new FormAttachment(comboComponent, 380);
		dataCompare.width = 100;
		buttonDiff.setLayoutData(dataCompare);
		buttonDiff.setEnabled(false);
		buttonDiff.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getSite().getShell().setCursor(
				    getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
									
				// get data and fill in results
				try {
                    SAFREnvironmentalComponent lhs = SAFRFactory.getComponent(
                        componentType, currentComponent.getId(), currentEnvIDLHS);

                    SAFREnvironmentalComponent rhs = SAFRFactory.getComponent(
                        componentType, currentComponent.getId(), currentEnvIDRHS);
                    
                    DiffNode.initGenerated();
                    DiffNodeComp node = DiffNodeFactory.generateDiffComp(lhs, rhs);
                    root = node.generateWholeTree();
                    diffTreeViewer.setSelection(null);
                    diffTreeViewer.setInput(root);
                    diffTreeViewer.refresh();                    
                    setResultsState(true);             
			    } catch (SAFRDependencyException de) {
			        DependencyMessageDialog.openDependencyDialog(
                        getSite().getShell(),"Inactive Dependencies",
                        "One View could not be loaded because the following component(s) are inactive."
                        + " Please reactivate these and try again.",
                        de.getDependencyString(),MessageDialog.ERROR,new String[] { IDialogConstants.OK_LABEL },0);
			    } catch (SAFRException e1) {
                    UIUtilities.handleWEExceptions(e1,
                        "Unexpected error occurred while getting the differences.", e1.getMessage());
                } finally {
                    getSite().getShell().setCursor(null);    
                }
			}
		});

        sectionFilter = safrGuiToolkit.createSection(compositeDiffCriteria, Section.TITLE_BAR, "Results Filter");
        FormData dataSectionFilter = new FormData();
        dataSectionFilter.top = new FormAttachment(buttonDiff, 20);
        dataSectionFilter.left = new FormAttachment(0, 5);
        dataSectionFilter.right = new FormAttachment(100, -5);
        sectionFilter.setLayoutData(dataSectionFilter);
    
        Composite compositeFilter = safrGuiToolkit.createComposite(sectionFilter, SWT.NONE);
        compositeFilter.setLayout(new FormLayout());
        FormData dataCompositeFilter = new FormData();
        dataCompositeFilter.top = new FormAttachment(0, 0);
        dataCompositeFilter.left = new FormAttachment(0, 0);
        dataCompositeFilter.right = new FormAttachment(0, 0);
        compositeFilter.setLayoutData(dataCompositeFilter);
        sectionFilter.setClient(compositeFilter);
    
        Group groupFilter = safrGuiToolkit.createGroup(compositeFilter,SWT.BORDER_SOLID, "");
        groupFilter.setLayout(new FormLayout());
        FormData dataGroupFilter = new FormData();
        dataGroupFilter.left = new FormAttachment(0, 5);
        dataGroupFilter.top = new FormAttachment(0, 5);
        dataGroupFilter.right = new FormAttachment(100, -10);
        dataGroupFilter.height = 48;
        groupFilter.setLayoutData(dataGroupFilter);
        
        chkboxSame = safrGuiToolkit.createButton(groupFilter, SWT.CHECK, null);
        FormData dataChkboxSame = new FormData();
        dataChkboxSame.top = new FormAttachment(0, 0);
        dataChkboxSame.left = new FormAttachment(0, 25);
        chkboxSame.setLayoutData(dataChkboxSame);
        chkboxSame.setSelection(true);
        chkboxSame.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                if (chkboxSame.getSelection()) {
                    filter.add(DiffNodeState.Same);
                }
                else {
                    filter.remove(DiffNodeState.Same);                    
                }
                diffTreeViewer.refresh();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
            
        });

        Label labelChkboxSame = safrGuiToolkit.createLabel(groupFilter, SWT.NONE, "Matching Only");
        FormData dataLabelChkboxSame = new FormData();
        dataLabelChkboxSame.top = new FormAttachment(0, 0);
        dataLabelChkboxSame.left = new FormAttachment(chkboxSame, 5);
        labelChkboxSame.setLayoutData(dataLabelChkboxSame);
        labelChkboxSame.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));        
        
        chkboxChg = safrGuiToolkit.createButton(groupFilter, SWT.CHECK, null);
        FormData dataChkboxChg = new FormData();
        dataChkboxChg.top = new FormAttachment(0, 0);
        dataChkboxChg.left = new FormAttachment(labelChkboxSame, 100);
        chkboxChg.setLayoutData(dataChkboxChg);
        chkboxChg.setSelection(true);
        chkboxChg.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                if (chkboxChg.getSelection()) {
                    filter.add(DiffNodeState.Different);
                }
                else {
                    filter.remove(DiffNodeState.Different);                    
                }
                diffTreeViewer.refresh();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
            
        });

        Label labelChkboxChg = safrGuiToolkit.createLabel(groupFilter, SWT.NONE, "Differences Only");
        FormData dataLabelChkboxChg = new FormData();
        dataLabelChkboxChg.top = new FormAttachment(0, 0);
        dataLabelChkboxChg.left = new FormAttachment(chkboxChg, 5);
        labelChkboxChg.setLayoutData(dataLabelChkboxChg);
        labelChkboxChg.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));                
        
        chkboxAdd = safrGuiToolkit.createButton(groupFilter, SWT.CHECK,null);
        FormData dataChkboxAdd = new FormData();
        dataChkboxAdd.top = new FormAttachment(chkboxSame, 0);
        dataChkboxAdd.left = new FormAttachment(0, 25);
        chkboxAdd.setLayoutData(dataChkboxAdd);
        chkboxAdd.setSelection(true);
        chkboxAdd.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                if (chkboxAdd.getSelection()) {
                    filter.add(DiffNodeState.Removed);
                }
                else {
                    filter.remove(DiffNodeState.Removed);                    
                }
                diffTreeViewer.refresh();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
            
        });
           
        Label labelChkboxAdd = safrGuiToolkit.createLabel(groupFilter, SWT.NONE, "Environment 1 Only");
        FormData dataLabelChkboxAdd = new FormData();
        dataLabelChkboxAdd.top = new FormAttachment(chkboxSame, 0);
        dataLabelChkboxAdd.left = new FormAttachment(chkboxAdd, 5);
        labelChkboxAdd.setLayoutData(dataLabelChkboxAdd);
        labelChkboxAdd.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));        
        
        chkboxRem = safrGuiToolkit.createButton(groupFilter, SWT.CHECK, null);
        FormData dataChkboxRem = new FormData();
        dataChkboxRem.top = new FormAttachment(chkboxSame, 0);
        dataChkboxRem.left = new FormAttachment(labelChkboxSame, 100);
        chkboxRem.setLayoutData(dataChkboxRem);
        chkboxRem.setSelection(true);
        chkboxRem.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                if (chkboxRem.getSelection()) {
                    filter.add(DiffNodeState.Added);
                }
                else {
                    filter.remove(DiffNodeState.Added);                    
                }
                diffTreeViewer.refresh();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
            
        });

        Label labelChkboxRem = safrGuiToolkit.createLabel(groupFilter, SWT.NONE, "Environment 2 Only");
        FormData dataLabelChkboxRem = new FormData();
        dataLabelChkboxRem.top = new FormAttachment(chkboxSame, 0);
        dataLabelChkboxRem.left = new FormAttachment(chkboxRem, 5);
        labelChkboxRem.setLayoutData(dataLabelChkboxRem);
        labelChkboxRem.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));                
        
        filter = new HashSet<DiffNodeState>();
        filter.add(DiffNodeState.Added);
        filter.add(DiffNodeState.Removed);
        filter.add(DiffNodeState.Same);
        filter.add(DiffNodeState.Different);        
        
		sectionExport = safrGuiToolkit.createSection(
				compositeDiffCriteria, Section.TITLE_BAR, "Export");
		FormData dataSectionExport = new FormData();
		dataSectionExport.top = new FormAttachment(sectionFilter, 20);
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
        String comPath = preferences.get(UserPreferencesNodes.COMPARE_PATH,"");
        if (comPath==null || comPath.equals("")) { 
            textLocation.setText(ProfileLocation.getProfileLocation().getLocalProfile() + "diff");
        }
        else {
            textLocation.setText(comPath);            
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
			    exportDiff();
			}

		});
        
        setResultsState(false);
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
                	if (currentEnvIDLHS != UIUtilities.getCurrentEnvironmentID()) {
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
	
	private void setResultsState(boolean enable) {
	    if (!enable) {
	        root = null;
	    }
	    
		sectionExport.setEnabled(enable);
		buttonLocation.setEnabled(enable);
		textLocation.setEnabled(enable);
		buttonExport.setEnabled(enable);
		
        sectionFilter.setEnabled(enable);
        chkboxSame.setEnabled(enable);
        chkboxChg.setEnabled(enable);
        chkboxAdd.setEnabled(enable);       
        chkboxRem.setEnabled(enable);  
        
        if (colAct != null) {
            colAct.setEnabled(enable);
            expAct.setEnabled(enable);
        }
        
        IViewPart viewPart = getSite().getPage().findView(CompareTextView.ID);
        if (viewPart != null) {
            getSite().getPage().hideView(viewPart);
        }        
	}
	
	private void createSectionDiffResult(Composite body) {
		sectionDiffResults = safrGuiToolkit.createSection(body,
				Section.TITLE_BAR, "Result:");
		FormData datasectionDiffResults = new FormData();
		datasectionDiffResults.top = new FormAttachment(0, 20);
		datasectionDiffResults.left = new FormAttachment(38, 0);
		datasectionDiffResults.right = new FormAttachment(100, -5);
		datasectionDiffResults.bottom = new FormAttachment(100, -10);
		sectionDiffResults.setLayoutData(datasectionDiffResults);

	    ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
	    ToolBar toolbar = toolBarManager.createControl(sectionDiffResults);
	    toolbar.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_HAND));
        expAct = new ExpandAction();
        toolBarManager.add(expAct);
	    colAct = new CollapseAction();
	    toolBarManager.add(colAct);
	    toolBarManager.update(true);
	    sectionDiffResults.setTextClient(toolbar);
	    
		Composite compositeDiffResults = safrGuiToolkit.createComposite(
				sectionDiffResults, SWT.NONE);
		compositeDiffResults.setLayout(new FormLayout());
		FormData dataCompositeDiffResults = new FormData();
		dataCompositeDiffResults.top = new FormAttachment(0, 0);
		dataCompositeDiffResults.left = new FormAttachment(0, 0);
		dataCompositeDiffResults.right = new FormAttachment(0, 0);
		dataCompositeDiffResults.bottom = new FormAttachment(0, 0);
		compositeDiffResults.setLayoutData(dataCompositeDiffResults);
		sectionDiffResults.setClient(compositeDiffResults);
		
        diffTreeViewer = safrGuiToolkit.createTreeViewer(compositeDiffResults, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        FormData dataDependencyTreeViewer = new FormData();
        dataDependencyTreeViewer.top = new FormAttachment(0, 10);
        dataDependencyTreeViewer.left = new FormAttachment(0, 10);
        dataDependencyTreeViewer.right = new FormAttachment(100, -10);
        dataDependencyTreeViewer.height = 480;
        dataDependencyTreeViewer.width = 500;        
        dataDependencyTreeViewer.bottom = new FormAttachment(100, -10);
        diffTreeViewer.getTree().setLayoutData(dataDependencyTreeViewer);
        diffTreeViewer.setUseHashlookup(false);
        diffTreeViewer.setContentProvider(new DiffTreeContentProvider());
        diffTreeViewer.setLabelProvider(new DiffTreeLabelProvider());
        ColumnViewerToolTipSupport.enableFor(diffTreeViewer,ToolTip.NO_RECREATE);
        
        diffTreeViewer.getTree().addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
                if (event.button == 1) {
                    IViewPart viewPart = getSite().getPage().findView(CompareTextView.ID);
                    if (viewPart != null) {
                        getSite().getPage().hideView(viewPart);
                    }                        
                    
                    Point point = new Point(event.x, event.y);
                    TreeItem item = diffTreeViewer.getTree().getItem(point);
                    showDiffOfItem(item);
                }
            }        
        });		
	}
		
	private void showDiffOfItem(TreeItem item) {
        if (item != null) {
            DiffBaseNode node = (DiffBaseNode)item.getData();
            String lLabel = currentEnvironmentLHS.getDescriptor();
            String rLabel = currentEnvironmentRHS.getDescriptor();
            if (node instanceof DiffFieldValue &&
                ((DiffFieldValue)node).isLarge()) {
                try {
                    DiffFieldValue fldVal = (DiffFieldValue)node;
                    OtherValue val = fldVal.getOtherValue();
                    if (val == null) {
                        if (fldVal.getValue() != null && fldVal.getValue().length() > 0) {
                            CompareTextView vw = (CompareTextView)getSite().getPage().showView(CompareTextView.ID);
                            vw.showCompareFor(fldVal.getValue(), fldVal.getValue(), lLabel, rLabel);
                        }
                    }
                    else if (val.isLeft) {
                        CompareTextView vw = (CompareTextView)getSite().getPage().showView(CompareTextView.ID);
                        vw.showCompareFor(val.value, fldVal.getValue(), lLabel, rLabel);                                    
                    }
                    else {
                        CompareTextView vw = (CompareTextView)getSite().getPage().showView(CompareTextView.ID);
                        vw.showCompareFor(fldVal.getValue(), val.value, lLabel, rLabel);                                                                        
                    }
                } catch (PartInitException e1) {
                    UIUtilities.handleWEExceptions(e1, "Unexpected error occurred while opening view.", null);
                }
            }
            else if (node instanceof DiffNodeSection) {
                DiffNodeSection section = (DiffNodeSection)node;
                try {
                    ByteArrayOutputStream leftstream = new ByteArrayOutputStream();
                    section.dumpEnv(leftstream, currentEnvIDLHS);
                    String leftStr =  new String(leftstream.toByteArray(), "UTF-8");

                    ByteArrayOutputStream rightstream = new ByteArrayOutputStream();
                    section.dumpEnv(rightstream, currentEnvIDRHS);
                    String rightStr =  new String(rightstream.toByteArray(), "UTF-8");
                    
                    CompareTextView vw = (CompareTextView)getSite().getPage().showView(CompareTextView.ID);
                    vw.showCompareFor(leftStr, rightStr, lLabel, rLabel);                                                        
                    
                } catch (IOException e) {
                    UIUtilities.handleWEExceptions(e, "Unexpected error occurred while getting difference text.", null);
                } catch (PartInitException e) {
                    UIUtilities.handleWEExceptions(e, "Unexpected error occurred while opening view.", null);
                }
            }
        }	    
	}

    private void exportDiff() {
        if (root == null) {
            UIUtilities.handleWEExceptions(new SAFRException(
                "No result to export."), null,
                "No result");            
        }
        if (textLocation.getText().equals("")
                || !isFilePathCorrect(textLocation.getText())) {
            UIUtilities.handleWEExceptions(new SAFRException(
                    "Please specify a valid export location."), null,
                    "Path not valid");
            return;
        }
        Preferences preferences = SAFRPreferences.getSAFRPreferences(); 
        preferences.put(UserPreferencesNodes.COMPARE_PATH, textLocation.getText());
        try {
            preferences.flush();
        } catch (BackingStoreException e1) {
            logger.log(Level.INFO, "Failed to save preferences", e1);
            throw new SAFRFatalException("Failed to save preferences " + e1.getMessage());                    
        }               
        try {
            getSite().getShell().setCursor(
                    getSite().getShell().getDisplay().getSystemCursor(
                            SWT.CURSOR_WAIT));
            String exportPath = textLocation.getText() + File.separatorChar
                    + componentType.toString() + "-"
                    + currentComponent.getName()
                    + ".txt";

            // create the output stream
            try {
                OutputStream outStream = new FileOutputStream(exportPath);
                // create the root row
                root.dumper(outStream, filter);
                outStream.flush();
                outStream.close(); 
            } catch (IOException e) {
                UIUtilities.handleWEExceptions(e, null, "Error writing to file");
                return;
            }
            MessageDialog.openInformation(getSite().getShell(),
                    "Diff file exported",
                    "The diff file is exported successfully to this location:" + SAFRUtilities.LINEBREAK
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

	private void populateEnvironment(TableComboViewer comboViewer, TableCombo comboEnvironment, User user) {
		Integer counter = 0;
		List<EnvironmentQueryBean> envList;
		try {
			envList = SAFRQuery.queryEnvironmentsForLoggedInUser(
					SortType.SORT_BY_NAME, false);
			comboViewer.setInput(envList);
			comboViewer.refresh();
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

        if (currentEnvIDLHS == null || currentEnvIDLHS == 0 ||
            currentEnvIDRHS == null || currentEnvIDRHS == 0)
        {
            return;
        }
		
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
						if (bean.getId() == null) {
						    return "";
						} else {
						    return (bean.getName() + " [" + Integer.toString(bean.getId()) + "]");
						}
					default:
						return super.getColumnText(element, columnIndex);
					}
				}
			};
			
			labelProvider.setInput();
			comboComponentViewer.setLabelProvider(labelProvider);
			if (componentType == ComponentType.ControlRecord) {
			    
			    // get LHS
				List<ControlRecordQueryBean> crListLHS = SAFRQuery.
				    queryAllControlRecords(currentEnvIDLHS, SortType.SORT_BY_NAME);
                
                // get RHS
                List<ControlRecordQueryBean> crListRHS = SAFRQuery.
                    queryAllControlRecords(currentEnvIDRHS, SortType.SORT_BY_NAME);
                
                // find list of components that exit in both environments
                List<ControlRecordQueryBean> crList = new ArrayList<ControlRecordQueryBean>();
                for (ControlRecordQueryBean crBeanLHS : crListLHS) {
                    for (ControlRecordQueryBean crBeanRHS : crListRHS) {
                        if (crBeanLHS.getId().equals(crBeanRHS.getId())) {
                            crList.add(crBeanLHS);
                            break;
                        }
                    }
                }
                
				comboComponentViewer.setInput(crList);
				comboComponentViewer.setSorter(new ControlRecordTableSorter(1,SWT.UP));

			} else if (componentType == ComponentType.LogicalFile) {
                
                // get LHS
                List<LogicalFileQueryBean> logicalFileListLHS = SAFRQuery
                        .queryAllLogicalFiles(currentEnvIDLHS,SortType.SORT_BY_NAME);
                
                // get RHS
                List<LogicalFileQueryBean> logicalFileListRHS = SAFRQuery
                        .queryAllLogicalFiles(currentEnvIDRHS,SortType.SORT_BY_NAME);
                
                // find list of components that exit in both environments
                List<LogicalFileQueryBean> lfList = new ArrayList<LogicalFileQueryBean>();
                for (LogicalFileQueryBean lfBeanLHS : logicalFileListLHS) {
                    for (LogicalFileQueryBean lfBeanRHS : logicalFileListRHS) {
                        if (lfBeanLHS.getId().equals(lfBeanRHS.getId())) {
                            lfList.add(lfBeanLHS);
                            break;
                        }
                    }
                }
                
                comboComponentViewer.setInput(lfList);
                comboComponentViewer.setSorter(new LogicalFileTableSorter(1,SWT.UP));

            } else if (componentType == ComponentType.LookupPath) {
				List<LookupQueryBean> lookuPathListLHS = SAFRQuery
						.queryAllLookups(currentEnvIDLHS, SortType.SORT_BY_NAME);

                List<LookupQueryBean> lookuPathListRHS = SAFRQuery
                    .queryAllLookups(currentEnvIDRHS, SortType.SORT_BY_NAME);

                // find list of components that exit in both environments
                List<LookupQueryBean> lpList = new ArrayList<LookupQueryBean>();
                for (LookupQueryBean lpBeanLHS : lookuPathListLHS) {
                    for (LookupQueryBean lpBeanRHS : lookuPathListRHS) {
                        if (lpBeanLHS.getId().equals(lpBeanRHS.getId())) {
                            lpList.add(lpBeanLHS);
                            break;
                        }
                    }
                }
                
				comboComponentViewer.setInput(lpList);
				comboComponentViewer.setSorter(new LookupTableSorter(1, SWT.UP));

			} else if (componentType == ComponentType.LogicalRecord) {
				List<LogicalRecordQueryBean> logicalRecordListLHS = SAFRQuery
						.queryAllLogicalRecords(currentEnvIDLHS,SortType.SORT_BY_NAME);

                List<LogicalRecordQueryBean> logicalRecordListRHS = SAFRQuery
                    .queryAllLogicalRecords(currentEnvIDRHS,SortType.SORT_BY_NAME);

                // find list of components that exit in both environments
                List<LogicalRecordQueryBean> lrList = new ArrayList<LogicalRecordQueryBean>();
                for (LogicalRecordQueryBean lrBeanLHS : logicalRecordListLHS) {
                    for (LogicalRecordQueryBean lrBeanRHS : logicalRecordListRHS) {
                        if (lrBeanLHS.getId().equals(lrBeanRHS.getId())) {
                            lrList.add(lrBeanLHS);
                            break;
                        }
                    }
                }
                
				comboComponentViewer.setInput(lrList);
				comboComponentViewer.setSorter(new LogicalRecordTableSorter(1,SWT.UP));
				
			} else if (componentType == ComponentType.PhysicalFile) {
				List<PhysicalFileQueryBean> physicalFileListLHS = SAFRQuery
					.queryAllPhysicalFiles(currentEnvIDLHS, SortType.SORT_BY_NAME);

                List<PhysicalFileQueryBean> physicalFileListRHS = SAFRQuery
                    .queryAllPhysicalFiles(currentEnvIDRHS, SortType.SORT_BY_NAME);

                // find list of components that exit in both environments
                List<PhysicalFileQueryBean> pfList = new ArrayList<PhysicalFileQueryBean>();
                for (PhysicalFileQueryBean pfBeanLHS : physicalFileListLHS) {
                    for (PhysicalFileQueryBean pfBeanRHS : physicalFileListRHS) {
                        if (pfBeanLHS.getId().equals(pfBeanRHS.getId())) {
                            pfList.add(pfBeanLHS);
                            break;
                        }
                    }
                }
                
				comboComponentViewer.setInput(pfList);
				comboComponentViewer.setSorter(new PhysicalFileTableSorter(1,SWT.UP));
				
			} else if (componentType == ComponentType.UserExitRoutine) {
				List<UserExitRoutineQueryBean> userExitRoutineListLHS = SAFRQuery
					.queryAllUserExitRoutines(currentEnvIDLHS,SortType.SORT_BY_NAME);
				
                List<UserExitRoutineQueryBean> userExitRoutineListRHS = SAFRQuery
                    .queryAllUserExitRoutines(currentEnvIDRHS,SortType.SORT_BY_NAME);

                // find list of components that exit in both environments
                List<UserExitRoutineQueryBean> ueList = new ArrayList<UserExitRoutineQueryBean>();
                for (UserExitRoutineQueryBean ueBeanLHS : userExitRoutineListLHS) {
                    for (UserExitRoutineQueryBean ueBeanRHS : userExitRoutineListRHS) {
                        if (ueBeanLHS.getId().equals(ueBeanRHS.getId())) {
                            ueList.add(ueBeanLHS);
                            break;
                        }
                    }
                }
                
				comboComponentViewer.setInput(ueList);
				comboComponentViewer.setSorter(new UserExitRoutineTableSorter(1, SWT.UP));
				
			} else if (componentType == ComponentType.View) {
				List<ViewQueryBean> viewListLHS = SAFRQuery.queryAllViews(
					currentEnvIDLHS, SortType.SORT_BY_NAME);

                List<ViewQueryBean> viewListRHS = SAFRQuery.queryAllViews(
                    currentEnvIDRHS, SortType.SORT_BY_NAME);

                // find list of components that exit in both environments
                List<ViewQueryBean> vList = new ArrayList<ViewQueryBean>();
                for (ViewQueryBean vBeanLHS : viewListLHS) {
                    for (ViewQueryBean vBeanRHS : viewListRHS) {
                        if (vBeanLHS.getId().equals(vBeanRHS.getId())) {
                            vList.add(vBeanLHS);
                            break;
                        }
                    }
                }
                
				comboComponentViewer.setInput(vList);
				comboComponentViewer.setSorter(new ViewTableSorter(1, SWT.UP));
			} else if (componentType == ComponentType.ViewFolder) {
                List<ViewFolderQueryBean> vfListLHS = SAFRQuery.queryAllViewFolders(
                    currentEnvIDLHS, SortType.SORT_BY_NAME);

                List<ViewFolderQueryBean> vfListRHS = SAFRQuery.queryAllViewFolders(
                    currentEnvIDRHS, SortType.SORT_BY_NAME);

                // find list of components that exit in both environments
                List<ViewFolderQueryBean> vfList = new ArrayList<ViewFolderQueryBean>();
                for (ViewFolderQueryBean vfBeanLHS : vfListLHS) {
                    for (ViewFolderQueryBean vfBeanRHS : vfListRHS) {
                        if (vfBeanLHS.getId().equals(vfBeanRHS.getId())) {
                            vfList.add(vfBeanLHS);
                            break;
                        }
                    }
                }
                
                comboComponentViewer.setInput(vfList);
                comboComponentViewer.setSorter(new ViewFolderTableSorter(1, SWT.UP));
            }
			
			for (int iCounter = 0; iCounter < 2; iCounter++) {
				if (prvsListner[iCounter] != null) {
					comboComponent.getTable().getColumn(iCounter).removeSelectionListener(prvsListner[iCounter]);
				}
				ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(iCounter, comboComponentViewer,	(ComponentType) componentType);
				prvsListner[iCounter] = colListener;
				comboComponent.getTable().getColumn(iCounter).addSelectionListener(colListener);
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

    public void partActivated(IWorkbenchPartReference partRef) {
    }

    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    public void partClosed(IWorkbenchPartReference partRef) {
    }

    public void partDeactivated(IWorkbenchPartReference partRef) {
    }

    public void partOpened(IWorkbenchPartReference partRef) {
    }

    public void partHidden(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) { 
              Composite parent1 = null;
              if (form.getParent() != null && 
                  form.getParent().getParent() != null) {
                  parent1 = form.getParent().getParent().getParent();
              }
              final CompareTextView viewPart = (CompareTextView)getSite().getPage().findView(CompareTextView.ID);
              if (viewPart != null) {
                  Composite parent2 = null;
                  if (viewPart.getPane().getParent() != null && 
                      viewPart.getPane().getParent().getParent() != null &&
                      viewPart.getPane().getParent().getParent().getParent() != null) {                  
                      parent2 = viewPart.getPane().getParent().getParent().getParent().getParent();
                  }
                  if (parent1 != null && parent1 != parent2) {
                      Display.getCurrent().asyncExec(new Runnable() {
                          public void run() {
                        	  viewPart.getSite().getPage().hideView(viewPart);
                          }
                      });                              
                  }
              }                              
        }
    }

    public void partVisible(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {   
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {            
                    IViewPart viewPart = getSite().getPage().findView(CompareTextView.ID);
                    if (viewPart == null && diffTreeViewer.getTree().getSelection().length > 0) {
                        TreeItem item = diffTreeViewer.getTree().getSelection()[0];
                        showDiffOfItem(item);
                    }
                }
            });
        }
    }

    public void partInputChanged(IWorkbenchPartReference partRef) {
    }
    
    public void collapseAll() {
        if (colAct.isEnabled()) {
            diffTreeViewer.collapseAll();
        }
    }
    
    public void expandAll() {
        if (expAct.isEnabled()) {
            diffTreeViewer.expandAll();
        }
    }
    
}
