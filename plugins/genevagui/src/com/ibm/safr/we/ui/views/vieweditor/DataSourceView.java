package com.ibm.safr.we.ui.views.vieweditor;

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
import java.util.logging.Logger;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.jface.gridviewer.GridViewerEditor;
import org.eclipse.nebula.jface.gridviewer.GridViewerRow;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRAssociationList;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.editors.logic.ERFLogicTextEditorInput;
import com.ibm.safr.we.ui.editors.logic.EROLogicTextEditorInput;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditor;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditorInput;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;
import com.ibm.safr.we.ui.editors.view.WriteStatementGenerator;
import com.ibm.safr.we.ui.utilities.DepCheckOpener;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.RowEditorType;
import com.ibm.safr.we.ui.utilities.SAFRCheckboxCellEditor;
import com.ibm.safr.we.ui.utilities.SAFRComboBoxCellEditor;
import com.ibm.safr.we.ui.utilities.SAFRDialogCellEditor;
import com.ibm.safr.we.ui.utilities.SAFRTextCellEditor;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class DataSourceView extends ViewPart {

    static final Logger logger = Logger
    .getLogger("com.ibm.safr.we.ui.views.vieweditor.DataSourceView");
    
    public enum MouseHover {
        INLR,
        INLF,
        OUTLF,
        OUTPF,
        OUTEX,
        OTHER
      }

    private static final int EDITOR_ROW_HEIGHT = 20;
    
	public static String ID = "SAFRWE.DataSourceView";
	
    public enum RowType {
        INPUTHEADER("Extract-Phase Source:", true, RowEditorType.NONE),
        INLOGICALRECORD("        Logical Record", false, RowEditorType.NONE),
        INLOGICALFILE("        Logical File", false, RowEditorType.COMBO),
        RECORDFILTER("        Record Filter", false, RowEditorType.DIALOG),
        OUTPUTHEADER("Extract-Phase Output:", true, RowEditorType.NONE),
        OUTLOGICALFILE("        Logical File", false, RowEditorType.COMBO),
        OUTPHYSICALFILE("        Physical File", false, RowEditorType.COMBO),
        USEREXITNAME("        Write User-Exit Routine Name", false, RowEditorType.COMBO),
        USEREXITPARM("           Parameters", false, RowEditorType.TEXT),
        OVERRIDEOUTPUT("        Override default Record Logic?", false, RowEditorType.CHECKBOX),
        RECORDOUTPUT("        Record Logic", false, RowEditorType.DIALOG);
                
        final private String name;
        final private boolean expandable;
        final private RowEditorType editorType;
        
        RowType(String name, boolean expandable, RowEditorType editorType) {
            this.name = name;
            this.expandable = expandable;
            this.editorType = editorType;
        }
        
        public String getName() {
            return name;
        }

        public boolean isExpandable() {
            return expandable;
        }

        public RowEditorType getEditorType() {
            return editorType;
        }

        @Override
        public String toString() {
            return name;
        }
        
        public static RowType getRowTypeFromId(int id) {
            return RowType.values()[id];
        }
    }	
	private Composite composite;

	private GridTableViewer dataSourceGrid;
	private ViewSource viewSource;
	private final List<DataSourceRow> sourceFileRows = new ArrayList<DataSourceRow>();
	private Integer outLFIndex = null;
    private LogicalFileQueryBean outLFBean = null;
	
	private ViewEditor viewEditor;

	private CellEditor currentCellEditor;
	
	private MouseHover hover = MouseHover.OTHER;
	private MenuItem gridOpen = null;
    private MenuItem gridOpenDep = null;
    private MenuItem comboOpen = null;
    private MenuItem comboOpenDep = null;
	
    private Font boldFont = null;

    private static Color purple;
    
	@Override
	public void createPartControl(Composite parent) {
		// Add rows to be included in the table to a temp array.
		for (RowType row : RowType.values()) {
			sourceFileRows.add(new DataSourceRow(row));
		}
		
		// set visibility based on compiler
        SAFRPreferences preferences = new SAFRPreferences(); 
		
		composite = new Composite(parent, SWT.NONE);

		composite.setLayout(new FormLayout());
		composite.setLayoutData(new FormData());

		dataSourceGrid = new GridTableViewer(composite, SWT.FULL_SELECTION | SWT.V_SCROLL);

		dataSourceGrid.getGrid().setLinesVisible(true);
		dataSourceGrid.getGrid().setHeaderVisible(true);

		dataSourceGrid.setContentProvider(new ContentProvider());
		dataSourceGrid.getGrid().setRowHeaderVisible(true);
		dataSourceGrid.getGrid().setCellSelectionEnabled(true);
		dataSourceGrid.setRowHeaderLabelProvider(new RowLabelProviderImpl());
		addGridOpenEditorMenu();
		
		FormData tableData = new FormData();
		tableData.top = new FormAttachment(0, 0);
		tableData.bottom = new FormAttachment(100, 0);
		tableData.left = new FormAttachment(0, 0);
		tableData.right = new FormAttachment(100, 0);
		dataSourceGrid.getGrid().setLayoutData(tableData);
		dataSourceGrid.getGrid().setItemHeaderWidth(300);
		final GridViewerColumn column = new GridViewerColumn(dataSourceGrid,SWT.NONE);

		LabelProviderImpl labelProvider = new LabelProviderImpl();
		column.setLabelProvider(labelProvider);
		column.setEditingSupport(new EditingSupportImpl(dataSourceGrid));
		column.getColumn().setResizeable(true);

		// need to call this, otherwise the table won't work.
		dataSourceGrid.setInput(1);

		composite.addListener(SWT.Resize, new Listener() {
			// resize the table to occupy the whole view area, even if the user resizes it.
			public void handleEvent(Event event) {

				column.getColumn().setWidth(
				    composite.getSize().x-dataSourceGrid.getGrid().getItemHeaderWidth());

			}
		});
		
		dataSourceGrid.getGrid().addTraverseListener(new TraverseListener() {

            private void editNeighborCell(TraverseEvent e, Point pt, List<DataSourceRow> visibleRows, boolean up) {
                
                GridViewerEditor editor = (GridViewerEditor)dataSourceGrid.getColumnViewerEditor();
                GridItem item = dataSourceGrid.getGrid().getItem(pt.y);
                ViewerRow row = new GridViewerRow(item);
                ViewerCell cell = editor.searchCellAboveBelow(editor, row, editor.getGridViewer(), pt.x, up);
                if (cell == null) {
                    return;
                }
                boolean found = false;
                GridItem items[] = dataSourceGrid.getGrid().getItems();
                int rowIdx = 0;
                for ( ; rowIdx <items.length ; rowIdx++) {
                    if (((GridItem)cell.getViewerRow().getItem()) == items[rowIdx]) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    dataSourceGrid.editElement(visibleRows.get(rowIdx), cell.getColumnIndex());
                }
            }
            
            public void keyTraversed(TraverseEvent e) {
                e.doit = false;
                if (dataSourceGrid.getGrid().getItemCount() == 0 ||
                        dataSourceGrid.getGrid().getCellSelectionCount() == 0) {
                    return;
                }
                Point pt = dataSourceGrid.getGrid().getCellSelection()[0];
                if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
                    if (pt != null) {
                        editNeighborCell(e,pt,sourceFileRows,false);
                    }
                } 
                else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                    if (pt != null) {
                        editNeighborCell(e,pt,sourceFileRows,true);
                    }   
                } 
            }
        });

		dataSourceGrid.getGrid().addMouseListener(new MouseAdapter() {

		    @Override
            public void mouseDown(MouseEvent e) {
                Grid g = (Grid) e.getSource();
		        GridItem item = g.getItem(new Point(e.x, e.y));
                if (item == null) {
                    g.setData(SAFRLogger.USER, null); 
                    hover = MouseHover.OTHER;      
                    gridOpen.setEnabled(false);  
                    gridOpenDep.setEnabled(false);
                } 
                else {
                    DataSourceRow m = (DataSourceRow) item.getData();
                    g.setData(SAFRLogger.USER, m.getName());   
                    if (e.button == 3)
                    {
                        switch (m.getRowType()) {
                            case INLOGICALRECORD :
                                hover = MouseHover.INLR;
                                String lrname = dataSourceGrid.getGrid().getItem(1).getText(0);
                                if (lrname == null || lrname == "") {
                                    gridOpen.setEnabled(false);
                                    gridOpenDep.setEnabled(false);
                                }
                                else {
                                    gridOpen.setEnabled(true);
                                    gridOpenDep.setEnabled(true);
                                }
                                break;
                            case INLOGICALFILE :
                                hover = MouseHover.INLF;
                                String ilfname = dataSourceGrid.getGrid().getItem(2).getText(0);
                                if (ilfname == null || ilfname == "") {
                                    gridOpen.setEnabled(false);
                                    gridOpenDep.setEnabled(false);
                                }
                                else {
                                    gridOpen.setEnabled(true);
                                    gridOpenDep.setEnabled(true);                                    
                                }
                                break;
                            case OUTLOGICALFILE:
                                hover = MouseHover.OUTLF;
                                String olfname = dataSourceGrid.getGrid().getItem(5).getText(0);
                                if (olfname == null || olfname == "") {
                                    gridOpen.setEnabled(false);
                                    gridOpenDep.setEnabled(false);
                                }
                                else {
                                    gridOpen.setEnabled(true);
                                    gridOpenDep.setEnabled(true);                                    
                                }
                                break;
                            case OUTPHYSICALFILE:
                                hover = MouseHover.OUTPF;
                                String opfname = dataSourceGrid.getGrid().getItem(6).getText(0);
                                if (opfname == null || opfname == "") {
                                    gridOpen.setEnabled(false);
                                    gridOpenDep.setEnabled(false);
                                }
                                else {
                                    gridOpen.setEnabled(true);
                                    gridOpenDep.setEnabled(true);                                    
                                }
                                break;
                            case USEREXITNAME:
                                hover = MouseHover.OUTEX;
                                String exname = dataSourceGrid.getGrid().getItem(7).getText(0);
                                if (exname == null || exname == "") {
                                    gridOpen.setEnabled(false);
                                    gridOpenDep.setEnabled(false);
                                }
                                else {
                                    gridOpen.setEnabled(true);
                                    gridOpenDep.setEnabled(true);                                    
                                }
                                break;
                            case INPUTHEADER :
                            case OUTPUTHEADER :
                            case RECORDFILTER :
                            case RECORDOUTPUT :
                            case USEREXITPARM:
                            case OVERRIDEOUTPUT:
                                hover = MouseHover.OTHER;      
                                gridOpen.setEnabled(false);                        
                                gridOpenDep.setEnabled(false);
                                break;
                            default :
                                break;
                            
                        }
                    }
                }                    
            }

		    @Override
            public void mouseUp(MouseEvent e) {
		        if (e.button != 1)
		        {
		            return;
		        }
                Point to = dataSourceGrid.getGrid().getCell(new Point(e.x, e.y));
                Grid g = (Grid) e.getSource();
                Point from = g.getFocusCell();
                
                // if clicked right of row headers
                if (e.x > g.getItemHeaderWidth()) {
                	if (to != null)  {
                		if (from != null && !to.equals(from)) {
            		        if (currentCellEditor != null && currentCellEditor instanceof SAFRComboBoxCellEditor) {
            		        	SAFRComboBoxCellEditor combo = (SAFRComboBoxCellEditor) currentCellEditor;
            		        	combo.focusLost();
            		        }
                		}
                	} else {
                		// clicked beyond the grid boundary
        		        if (currentCellEditor != null && currentCellEditor instanceof SAFRComboBoxCellEditor) {
        		        	SAFRComboBoxCellEditor combo = (SAFRComboBoxCellEditor) currentCellEditor;
        		        	combo.focusLost();
        		        }
                	}
                } else if (e.x <= g.getItemHeaderWidth() && to == null) {
                	// clicked on the row headers
    		        if (currentCellEditor != null && currentCellEditor instanceof SAFRComboBoxCellEditor) {
    		        	SAFRComboBoxCellEditor combo = (SAFRComboBoxCellEditor) currentCellEditor;
    		        	combo.focusLost();
    		        }
                }
		        
		    }
		    
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Grid g = (Grid) e.getSource();
                // only if clicked on row headers.
                if (e.x < g.getItemHeaderWidth()) {
                    GridItem item = g.getItem(new Point(e.x, e.y));
                    if (item != null) {
                        DataSourceRow m = (DataSourceRow) item.getData();
                        switch (m.getRowType()) {
                            case INPUTHEADER :
                                if (m.isExpanded()) {
                                    m.setExpanded(false);
                                    sourceFileRows.get(RowType.INLOGICALRECORD.ordinal()).setVisible(false);
                                    sourceFileRows.get(RowType.INLOGICALFILE.ordinal()).setVisible(false);
                                    sourceFileRows.get(RowType.RECORDFILTER.ordinal()).setVisible(false);
                                } else {
                                    sourceFileRows.get(RowType.INLOGICALRECORD.ordinal()).setVisible(true);
                                    sourceFileRows.get(RowType.INLOGICALFILE.ordinal()).setVisible(true);
                                    sourceFileRows.get(RowType.RECORDFILTER.ordinal()).setVisible(true);
                                    m.setExpanded(true);
                                }
                                refresh();
                                break;
                            case OUTPUTHEADER :
                                if (m.isExpanded()) {
                                    m.setExpanded(false);
                                    sourceFileRows.get(RowType.OUTLOGICALFILE.ordinal()).setVisible(false);
                                    sourceFileRows.get(RowType.OUTPHYSICALFILE.ordinal()).setVisible(false);
                                    sourceFileRows.get(RowType.USEREXITNAME.ordinal()).setVisible(false);
                                    sourceFileRows.get(RowType.USEREXITPARM.ordinal()).setVisible(false);
                                    sourceFileRows.get(RowType.OVERRIDEOUTPUT.ordinal()).setVisible(false);
                                    sourceFileRows.get(RowType.RECORDOUTPUT.ordinal()).setVisible(false);
                                } else {
                                    m.setExpanded(true);
                                    sourceFileRows.get(RowType.OUTLOGICALFILE.ordinal()).setVisible(true);
                                    sourceFileRows.get(RowType.OUTPHYSICALFILE.ordinal()).setVisible(true);
                                    sourceFileRows.get(RowType.USEREXITNAME.ordinal()).setVisible(true);
                                    sourceFileRows.get(RowType.USEREXITPARM.ordinal()).setVisible(true);
                                    sourceFileRows.get(RowType.OVERRIDEOUTPUT.ordinal()).setVisible(true);
                                    sourceFileRows.get(RowType.RECORDOUTPUT.ordinal()).setVisible(true);
                                }
                                refresh();
                                break;
                            default :
                                break;
                        }
                    }
                }
            }
		});
		
        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
                dataSourceGrid) {
            protected boolean isEditorActivationEvent(
                    ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                        || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.character == ' '))
                        || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
                        || (event.sourceEvent instanceof MouseEvent)
                        && ((MouseEvent) event.sourceEvent).button == 1;
            }
        };

        GridViewerEditor.create(dataSourceGrid, actSupport,
                ColumnViewerEditor.KEYBOARD_ACTIVATION
                | ColumnViewerEditor.TABBING_VERTICAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                | GridViewerEditor.SELECTION_FOLLOWS_EDITOR);

        Display display = Display.getCurrent();
        FontDescriptor fontDescriptor = FontDescriptor.createFrom(dataSourceGrid.getGrid().getFont());
        boldFont = fontDescriptor.setStyle(SWT.BOLD).createFont(display);
               
		showGridForCurrentEditor(getSite().getPage().getActiveEditor());
	}

    private void addGridOpenEditorMenu()
    {        
        Menu menu = new Menu(dataSourceGrid.getGrid());        
        gridOpen = new MenuItem(menu, SWT.PUSH);
        gridOpen.setText("Open Editor");
        gridOpen.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                switch (hover) {
                case INLR :
                    String ilrname = dataSourceGrid.getGrid().getItem(1).getText(0);
                    EditorOpener.open(UIUtilities.extractId(ilrname), ComponentType.LogicalRecord);                    
                    break;
                case INLF :
                    String ilfname = dataSourceGrid.getGrid().getItem(2).getText(0);
                    EditorOpener.open(UIUtilities.extractId(ilfname), ComponentType.LogicalFile);                    
                    break;
                case OUTLF :
                    String olfname = dataSourceGrid.getGrid().getItem(5).getText(0);
                    EditorOpener.open(UIUtilities.extractId(olfname), ComponentType.LogicalFile);                    
                    break;
                case OUTPF :
                    String opfname = dataSourceGrid.getGrid().getItem(6).getText(0);
                    EditorOpener.open(UIUtilities.extractId(opfname), ComponentType.PhysicalFile);                    
                    break;
                case OUTEX :
                    String exname = dataSourceGrid.getGrid().getItem(7).getText(0);
                    EditorOpener.open(UIUtilities.extractId(exname), ComponentType.UserExitRoutine);                    
                    break;
                default:
                    break;
                }
            }
        });
        
        gridOpenDep = new MenuItem(menu, SWT.PUSH);
        gridOpenDep.setText("Dependency Checker");
        gridOpenDep.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                switch (hover) {
                case INLR :
                    String ilrname = dataSourceGrid.getGrid().getItem(1).getText(0);
                    Integer id = UIUtilities.extractId(ilrname);
                    DepCheckOpener.open(new LogicalRecordQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(), 
                        id, 
                        null, null, null, null, null, null, null, null, null, null, null, null));
                    break;
                case INLF :
                    String ilfname = dataSourceGrid.getGrid().getItem(2).getText(0);
                    id = UIUtilities.extractId(ilfname);
                    DepCheckOpener.open(new LogicalFileQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(), 
                        id, 
                        null, null, null, null, null, null));
                    break;
                case OUTLF :
                    String olfname = dataSourceGrid.getGrid().getItem(5).getText(0);
                    id = UIUtilities.extractId(olfname);
                    DepCheckOpener.open(new LogicalFileQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(), 
                        id, 
                        null, null, null, null, null, null));
                    break;
                case OUTPF :
                    String opfname = dataSourceGrid.getGrid().getItem(6).getText(0);
                    id = UIUtilities.extractId(opfname);
                    DepCheckOpener.open(new PhysicalFileQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(), 
                        id, 
                        null, null, null, null, null, null, null, null, null, null, null, null));
                    break;
                case OUTEX :
                    String exname = dataSourceGrid.getGrid().getItem(7).getText(0);
                    id = UIUtilities.extractId(exname);
                    DepCheckOpener.open(new UserExitRoutineQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(), 
                        id, 
                        null, null, null, null, null, null, null, null, null));
                    break;
                    
                default:
                    break;
                }
            }
        });        
        
        dataSourceGrid.getGrid().setMenu(menu);
        
        
    }       
	
    private void addComboOpenEditorMenu(CCombo combo)
    {
        Menu menu = combo.getMenu();
        comboOpen = new MenuItem(menu, SWT.PUSH);
        comboOpen.setText("Open Editor");
        comboOpen.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                CCombo combo = (CCombo)currentCellEditor.getControl();
                ComponentAssociation ass = (ComponentAssociation)combo.
                    getData(Integer.toString(combo.getSelectionIndex()));
                if (ass != null) {   
                    EditorOpener.open(ass.getAssociatedComponentIdNum(), ComponentType.LogicalFile);                        
                }                
            }
        });
        
        comboOpenDep = new MenuItem(menu, SWT.PUSH);
        comboOpenDep.setText("Dependency Checker");
        comboOpenDep.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                CCombo combo = (CCombo)currentCellEditor.getControl();
                ComponentAssociation ass = (ComponentAssociation)combo.
                    getData(Integer.toString(combo.getSelectionIndex()));
                if (ass != null) {   
                    DepCheckOpener.open(new LogicalFileQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(), 
                        ass.getAssociatedComponentIdNum(), 
                        null, null, null, null, null, null));
                }                
            }
        });
        
        combo.addMouseListener(new MouseListener() {

            public void mouseDoubleClick(MouseEvent e) {
            }

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {            
                    CCombo combo = (CCombo)currentCellEditor.getControl();
                    ComponentAssociation ass = (ComponentAssociation)combo.
                        getData(Integer.toString(combo.getSelectionIndex()));
                    if (ass == null) {
                        comboOpen.setEnabled(false); 
                        comboOpenDep.setEnabled(false);
                    }
                    else {
                        comboOpen.setEnabled(true);                            
                        comboOpenDep.setEnabled(true);
                    }
                }                
            }

            public void mouseUp(MouseEvent e) {
            }
            
        });
    }       
	
    public void selectFirstCell() 
    {
		dataSourceGrid.getGrid().setSelection(2); // Extract Record Filter
        dataSourceGrid.getGrid().setFocusItem(dataSourceGrid.getGrid().getItem(2));
        dataSourceGrid.getGrid().setFocus();
        dataSourceGrid.getGrid().redraw();
    }
	
	private class ContentProvider implements IStructuredContentProvider {
		public ContentProvider() {
		}

		public Object[] getElements(Object inputElement) {
			if (viewSource != null) {
			    List<DataSourceRow> visibleRows = new ArrayList<DataSourceRow>();
			    for (DataSourceRow row : sourceFileRows) {
			        if (row.isVisible()) {
			            visibleRows.add(row);
			        }
			    }
				return visibleRows.toArray();
			} else {
				return new String[] {};
			}
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class LabelProviderImpl extends ColumnLabelProvider {


        public LabelProviderImpl() {

		}

		@Override
		public String getText(Object element) {
            DataSourceRow m = (DataSourceRow) element;
            if (m.getRowEditorType() == RowEditorType.COMBO) {
                if (m.getRowType() == RowType.OUTLOGICALFILE) {
                    if (outLFBean != null) {
                        return outLFBean.getDescriptor();
                    }
                }
            }		    
			return getRowValue((DataSourceRow) element);
		}

		@Override
		public Color getBackground(Object element) {
            ViewEditorInput viewEditorInput = (ViewEditorInput) viewEditor.getEditorInput();
            if (viewEditorInput.getEditRights() == EditRights.Read) {
                return Display.getCurrent().getSystemColor(
                    SWT.COLOR_WIDGET_LIGHT_SHADOW);
            }           		    
			DataSourceRow m = (DataSourceRow) element;
            SAFRPreferences preferences = new SAFRPreferences();                
			switch (m.getRowType()) {
                case INPUTHEADER :
                    return Display.getCurrent().getSystemColor(
                        SWT.COLOR_WIDGET_LIGHT_SHADOW);
                case INLOGICALRECORD :
                    return Display.getCurrent().getSystemColor(
                        SWT.COLOR_WIDGET_LIGHT_SHADOW);
                case INLOGICALFILE :
                    break;
                case RECORDFILTER :
                    if (viewSource.getExtractRecordFilter() != null &&
                        !viewSource.getExtractRecordFilter().trim().isEmpty()) {
                        return getColorPurple();
                    } 
                    break;
                case OUTPUTHEADER :
                    return Display.getCurrent().getSystemColor(
                        SWT.COLOR_WIDGET_LIGHT_SHADOW);
                case OUTLOGICALFILE :
                        if(viewSource.isExtractOutputOverriden() ||
                        viewEditor.getView().isFormatPhaseInUse()) {
                        return Display.getCurrent().getSystemColor(
                            SWT.COLOR_WIDGET_LIGHT_SHADOW);
                        }
                    break;
                case OUTPHYSICALFILE :
                	if(viewSource.isExtractOutputOverriden() ||
                        viewEditor.getView().isFormatPhaseInUse()) {
                        return Display.getCurrent().getSystemColor(
                            SWT.COLOR_WIDGET_LIGHT_SHADOW);
                	}
                    break;
                case USEREXITNAME :
                	if(viewSource.isExtractOutputOverriden()) {
                        return Display.getCurrent().getSystemColor(
                            SWT.COLOR_WIDGET_LIGHT_SHADOW);
                	}
                    break;
                case USEREXITPARM :
                	if(viewSource.isExtractOutputOverriden()) {
                        return Display.getCurrent().getSystemColor(
                            SWT.COLOR_WIDGET_LIGHT_SHADOW);
                	}
                    break;
                case OVERRIDEOUTPUT :
                    break;
                    
                case RECORDOUTPUT :
                    if (viewSource.isExtractOutputOverrideBlocked()) {
                        return Display.getCurrent().getSystemColor(
                            SWT.COLOR_WIDGET_LIGHT_SHADOW);
                    } else {
                        if (viewSource.getExtractRecordOutput() != null &&
                            !viewSource.getExtractRecordOutput().trim().isEmpty()) {
                            return getColorPurple();
                        }
                    }
                default :
                    break;
			    
			}
			return super.getBackground(element);
		}
		
        @Override
        public Image getImage(Object element) {
            DataSourceRow m = (DataSourceRow) element;
            switch (m.getRowType()) {
                case INPUTHEADER :
                    break;
                case INLOGICALRECORD :
                    break;
                case INLOGICALFILE :
                    break;
                case RECORDFILTER :
                    break;
                case OUTPUTHEADER :
                    break;
                case OUTLOGICALFILE :
                    break;
                case OUTPHYSICALFILE :
                    break;
                case USEREXITNAME :
                    break;
                case USEREXITPARM :
                    break;
                case OVERRIDEOUTPUT :
                    if (viewSource.isExtractOutputOverriden()) {
                        return UIUtilities.getAndRegisterImage(ImageKeys.CHECKED);
                    } else {
                        return UIUtilities.getAndRegisterImage(ImageKeys.UNCHECKED);
                    }                    
                case RECORDOUTPUT :
                    break;
                default :
                    break;
                
            }
            return super.getImage(element);
        }
	}
	
	private class RowLabelProviderImpl extends ColumnLabelProvider {
	    
        @Override
        public Font getFont(Object element) {
            DataSourceRow m = (DataSourceRow) element;
            if (m.getRowType() == RowType.INPUTHEADER ||
                m.getRowType() == RowType.OUTPUTHEADER) {
                return boldFont;
            }
            else {
                return null;
            }
        }
        	    
        @Override
        public String getText(Object element) {
            DataSourceRow m = (DataSourceRow) element;
            return m.getName().toString();
        }
	    
        @Override
        public Image getImage(Object element) {
            DataSourceRow m = (DataSourceRow) element;
            if (m.isExpandable()) {
                if (m.isExpanded()) {
                    return UIUtilities.getAndRegisterImage(ImageKeys.MINUS_IMAGE);
                } else {
                    return UIUtilities.getAndRegisterImage(ImageKeys.PLUS_IMAGE);
                }
            }
            return null;
        }
	}
	
	@Override
	public void setFocus() {
	}

	public class DataSourceRow {
	    private RowType rowType;
        private boolean expanded = true;
        private boolean visible = true;

        public DataSourceRow(RowType rowType) {
            this.rowType = rowType;
        }

        public RowType getRowType() {
            return rowType;
        }
        
		public String getName() {
			return rowType.getName();
		}

		public RowEditorType getRowEditorType() {
			return rowType.getEditorType();
		}

        public boolean isExpandable() {
            return rowType.isExpandable();
        }
		
        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

	}

	private class EditingSupportImpl extends EditingSupport {

		private GridTableViewer viewer;
		private CellEditor editor;
		private CCombo editorCombo;

		public EditingSupportImpl(final GridTableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected boolean canEdit(Object element) {
			ViewEditorInput viewEditorInput = (ViewEditorInput) viewEditor.getEditorInput();
			if (viewEditorInput.getEditRights() == EditRights.Read) {
				return false;
			}		       			
            DataSourceRow m = (DataSourceRow) element;
            switch (m.getRowType()) {
                case INPUTHEADER :
                    return false;
                case INLOGICALRECORD :
                    return false;
                case INLOGICALFILE :
                    return true;
                case RECORDFILTER :
                    return true;
                case OUTPUTHEADER :
                    return false;
                case OUTLOGICALFILE :
                	return viewSource.isExtractOutputOverrideBlocked();
                case OUTPHYSICALFILE :
                	return viewSource.isExtractOutputOverrideBlocked();
                case USEREXITNAME :
                    return viewSource.isExtractOutputOverrideBlocked();
                case USEREXITPARM :
                	return viewSource.isExtractOutputOverrideBlocked();
                case OVERRIDEOUTPUT :
                        return true;
                case RECORDOUTPUT :
                    return viewSource.isExtractOutputOverriden();
                default :
                    break;                
            }
            return false;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			setCurrentCellEditor(null);
			DataSourceRow m = (DataSourceRow) element;
			editor = null;
			switch (m.getRowType()) {
                case INPUTHEADER :
                case INLOGICALRECORD :
                    break;
                case INLOGICALFILE :
                    editor = new SAFRComboBoxCellEditor(viewer.getGrid(),
                        new String[] {}, SWT.READ_ONLY);
                    ((ComboBoxCellEditor)editor).setActivationStyle(
                            ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
                            ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
                            ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
                            ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);   
                    editorCombo = (CCombo) editor.getControl();
                    loadLogicalFileCombo(editorCombo);
                    setCurrentCellEditor(editor);
                    addComboOpenEditorMenu(editorCombo);                    
                    break;
                case RECORDFILTER :
                    editor = new SAFRDialogCellEditor((Composite) viewer.getControl()) {

                        @Override
                        protected Object openDialogBox(Control cellEditorWindow) {
                            try {
                                getSite().getPage().openEditor(new ERFLogicTextEditorInput(viewSource, viewEditor), LogicTextEditor.ID);
                            } catch (PartInitException e) {
                                UIUtilities.handleWEExceptions(e,
                                    "Unexpected error occurred while opening Logic Text editor.",null);
                            }
                            return null;
                        }
                    };
                    addDoubleClickListener(((SAFRDialogCellEditor) editor).getContents());
                    break;                    
                case OUTPUTHEADER :
                    break;
                case OUTLOGICALFILE :
                    ApplicationMediator.getAppMediator().waitCursor();
                    editor = new SAFRComboBoxCellEditor(viewer.getGrid(),
                        new String[] {}, SWT.READ_ONLY);
                    ((ComboBoxCellEditor)editor).setActivationStyle(
                            ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
                            ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
                            ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
                            ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);                               
                    editorCombo = (CCombo) editor.getControl();                    
                    editorCombo.setVisibleItemCount(10);
                    List<EnvironmentalQueryBean> lfs = viewEditor.getViewPropertiesMap().get(ComponentType.LogicalFile);
                    editorCombo.add("");
                    for (EnvironmentalQueryBean lfBean : lfs) {                        
                        editorCombo.add(lfBean.getDescriptor());
                    }
                    outLFIndex = null;
                    outLFBean = null;
                    ApplicationMediator.getAppMediator().normalCursor();
                    break;
                case OUTPHYSICALFILE :
                    editor = new SAFRComboBoxCellEditor(viewer.getGrid(),
                        new String[] {}, SWT.READ_ONLY);
                    ((ComboBoxCellEditor)editor).setActivationStyle(
                            ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
                            ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
                            ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
                            ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);                               
                    editorCombo = (CCombo) editor.getControl();
                    editorCombo.setVisibleItemCount(10);
                    // get logical file bean
                    if (outLFIndex != null && outLFIndex != 0 ) {
                        SAFRAssociationList<FileAssociation> physicalFileAssociations = SAFRAssociationFactory
                            .getLogicalFileToPhysicalFileAssociations(outLFBean);
                        Integer index = 0;
                        for (FileAssociation ass : physicalFileAssociations) {
                            editorCombo.add(ass.getAssociatedDescriptor());
                            editorCombo.setData(index.toString(), ass);
                            index++;
                        }
                    } 
                    else if (viewSource.getExtractFileAssociation() != null) {
                        FileAssociation cass = viewSource.getExtractFileAssociation();
                        // create Logicalfile bean
                        if (cass.getAssociationId() != null) {
                            LogicalFileQueryBean lfBean = new LogicalFileQueryBean(
                                cass.getEnvironmentId(), cass.getAssociatingComponentId(), cass.getAssociatingComponentName(),
                                EditRights.ReadModifyDelete, null, null, null, null);
                            SAFRAssociationList<FileAssociation> physicalFileAssociations = SAFRAssociationFactory
                                .getLogicalFileToPhysicalFileAssociations(lfBean);
                            Integer index = 0;
                            for (FileAssociation ass : physicalFileAssociations) {
                                editorCombo.add(ass.getAssociatedDescriptor());
                                editorCombo.setData(index.toString(), ass);
                                index++;
                            }  
                        }
                    }
                    break;
                case USEREXITNAME :
                    ApplicationMediator.getAppMediator().waitCursor();
                    editor = new SAFRComboBoxCellEditor(viewer.getGrid(),
                        new String[] {}, SWT.READ_ONLY);
                    ((ComboBoxCellEditor)editor).setActivationStyle(
                            ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
                            ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
                            ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
                            ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);                               
                    editorCombo = (CCombo) editor.getControl();                    
                    editorCombo.setVisibleItemCount(10);
                    List<EnvironmentalQueryBean> exits = viewEditor.getViewPropertiesMap().get(ComponentType.WriteUserExitRoutine);
                    editorCombo.add("");
                    Integer index = 0;
                    if (exits != null) {
                        for (EnvironmentalQueryBean exitBean : exits) {                        
                            editorCombo.add(((UserExitRoutineQueryBean)exitBean).getComboString());
                            editorCombo.setData(index.toString(), exitBean);
                            index++;
                        }
                    }
                    ApplicationMediator.getAppMediator().normalCursor();                    
                    break;
                case USEREXITPARM :
                    editor = new SAFRTextCellEditor(viewer.getGrid());
                    ((Text) editor.getControl()).setTextLimit(32);                    
                    break;
                case OVERRIDEOUTPUT :
                    editor = new SAFRCheckboxCellEditor(viewer.getGrid());
                    break;
                case RECORDOUTPUT :
                    editor = new SAFRDialogCellEditor((Composite) viewer.getControl()) {

                        @Override
                        protected Object openDialogBox(Control cellEditorWindow) {
                            try {
                                getSite().getPage().openEditor(new EROLogicTextEditorInput(viewSource, viewEditor),LogicTextEditor.ID);
                            } catch (PartInitException e) {
                                UIUtilities.handleWEExceptions(e,
                                    "Unexpected error occurred while opening Logic Text editor.",null);
                            }
                            return null;
                        }
                    };
                    addDoubleClickListener(((SAFRDialogCellEditor) editor).getContents());
                    break;                    
                default :
                    break;
			    
			}
			return editor;
		}

		protected void addDoubleClickListener(Control editor) {
		    editor.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDown(MouseEvent e) {
                    if (e.button != 1)
                    {
                        return;
                    }               
                    Grid g = dataSourceGrid.getGrid();
                    
                    // Click was not in row header column.
                    GridItem item = g.getItem(g.getSelectionIndex());
                    if (item != null) {
                        DataSourceRow m = (DataSourceRow) item.getData();
                        LogicTextEditorInput input = null;
                        if (m.getRowType() == RowType.RECORDFILTER) {
                            input = new ERFLogicTextEditorInput(viewSource, viewEditor);                           
                        } else if (m.getRowType() == RowType.RECORDOUTPUT) {
                            input = new EROLogicTextEditorInput(viewSource, viewEditor);                                                     
                        }
                        if (input != null) {
                            try {
                                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                    .openEditor(input, LogicTextEditor.ID);
                            } catch (PartInitException pie) {
                                UIUtilities.handleWEExceptions(pie,
                                    "Unexpected error occurred while opening Logic Text editor.",null);
                            }
                        }
                    }
                }
		    });
		}
		
		@Override
		protected Object getValue(Object element) {
            DataSourceRow m = (DataSourceRow) element;
            switch (m.getRowType()) {
                case INPUTHEADER :
                    break;
                case INLOGICALFILE :
                    String value = getRowValue(element);
                    Integer index = editorCombo.indexOf((String) value);
                    return index;
                case INLOGICALRECORD :
                    break;
                case RECORDFILTER :
                    return getRowValue(element);
                case OUTPUTHEADER :
                    break;
                case OUTLOGICALFILE :
                    if (outLFIndex != null) {
                        return outLFIndex;
                    } 
                    else {
                        value = getRowValue(element);
                        index = editorCombo.indexOf((String) value);
                        return index;                       
                    }
                case OUTPHYSICALFILE :
                    value = getRowValue(element);
                    index = editorCombo.indexOf((String) value);
                    return index;
                case USEREXITNAME :
                    value = getRowValue(element);
                    index = editorCombo.indexOf((String) value);
                    return index;
                case USEREXITPARM :
                    String val = getRowValue(element);
                    return val;
                case OVERRIDEOUTPUT :
                    return viewSource.isExtractOutputOverriden();
                case RECORDOUTPUT :
                    return getRowValue(element);
                default :
                    break;
                
            }
            return null;
		}

		public void generateWriteStatement() {
		    WriteStatementGenerator generator = new WriteStatementGenerator(viewEditor.getView(), viewSource);
		    generator.generateWriteStatement();
		}
		
		@Override
		protected void setValue(Object element, Object value) {
			DataSourceRow m = (DataSourceRow) element;
			switch (m.getRowType()) {
                case INPUTHEADER :
                case INLOGICALRECORD :
                    break;			    
                case INLOGICALFILE :
                    Integer index = (Integer)value;
                    if (index != -1) {
                        try {
                            ComponentAssociation previousAssoc = viewSource.getLrFileAssociation();
                            ComponentAssociation currentAssoc = (ComponentAssociation) editorCombo.getData(index.toString());
                            if (!UIUtilities.isEqual(previousAssoc, currentAssoc)) {
                                viewSource.setLrFileAssociation(currentAssoc);
                                viewEditor.updateColumnSourceAffectedRows(viewSource);
                                viewEditor.setModified(true);
                            }
                        } catch (SAFRException e) {
                            UIUtilities.handleWEExceptions(e, "Error in setting values ", null);
                        }
                    }
                    break;
                case RECORDFILTER :
                    viewSource.setExtractRecordFilter((String) value);
                    break;
                case OUTPUTHEADER :
                    break;
                case OUTLOGICALFILE : 
                    outLFIndex = (Integer)value;
                    if (outLFIndex == -1) {
                        // do nothing
                    }
                    else if (outLFIndex == 0) {
                        viewSource.setExtractFileAssociation(null);
                        viewEditor.setModified(true);
                        generateWriteStatement();                    
                    }
                    else {
                        outLFBean = (LogicalFileQueryBean) viewEditor.getViewPropertiesMap().
                            get(ComponentType.LogicalFile).get(outLFIndex-1);
                        if (viewSource.getExtractFileAssociation() != null && 
                            !outLFBean.getId().equals(viewSource.getExtractFileAssociation().getAssociatingComponentId())) {
                            viewSource.setExtractFileAssociation(null);
                        }
                        viewEditor.setModified(true);
                        generateWriteStatement();                   
                    }
                    break;
                case OUTPHYSICALFILE :
                    Integer outPFIndex = (Integer)value;
                    if (outPFIndex != -1) {
                        FileAssociation ass = (FileAssociation) editorCombo.getData(outPFIndex.toString());
                        viewSource.setExtractFileAssociation(ass);
                        outLFIndex = null;
                        outLFBean = null;
                        viewEditor.setModified(true);
                        generateWriteStatement();                   
                    }
                    break;
                case USEREXITNAME :
                    Integer exitIndex = (Integer)value;
                    if (exitIndex == -1) {
                        // do nothing
                    }
                    else if (exitIndex == 0) {
                        viewSource.setWriteExit(null);
                        viewEditor.setModified(true);
                        generateWriteStatement();                   
                    }
                    else {
                        UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean) viewEditor.getViewPropertiesMap().
                            get(ComponentType.WriteUserExitRoutine).get(exitIndex-1);
                        try {
                            UserExitRoutine userExitRoutine = SAFRApplication.getSAFRFactory().getUserExitRoutine(
                                bean.getId());
                            viewSource.setWriteExit(userExitRoutine);
                            viewEditor.setModified(true);
                            generateWriteStatement();                   
                        } catch (SAFRException e1) {
                            UIUtilities.handleWEExceptions(e1,"Error retrieving metadata component.",null);
                        }                        
                    }
                    break;
                case USEREXITPARM :
                    viewSource.setWriteExitParams(((Text)editor.getControl()).getText());
                    viewEditor.setModified(true);
                    generateWriteStatement();                   
                    break;
                case OVERRIDEOUTPUT :
                    Boolean override = (Boolean)value;
                    viewSource.setExtractOutputOverride(override);
                    viewEditor.setModified(true);
                    generateWriteStatement();
                    break;
                case RECORDOUTPUT :
                    viewSource.setExtractRecordOutput((String) value);
                    break;
                default :
                    break;
			    
			}
			refresh();
		}
	}

	public void partActivated(IWorkbenchPartReference partRef) {
		// CQ 8551. Nikita. 02/09/2010
		// Removed code that was causing this view to be displayed even if the
		// focus was on some other editor (in the restored mode) instead of
		// showing the message "Properties Unavailable"
		if (!(partRef.getPart(false) instanceof IEditorPart)) {
			return;
		}
		showGridForCurrentEditor((IEditorPart) partRef.getPart(false));

	}

	public void showGridForCurrentEditor(IEditorPart editor) {
	    if (editor instanceof ViewEditor) {
	        viewEditor = (ViewEditor) editor;
	        this.viewSource = viewEditor.getCurrentViewSource();
	        refresh();
	    } 
	}
	
	private void loadLogicalFileCombo(CCombo combo) {
		try {
			Integer counter = 0;
			combo.removeAll();
			SAFRAssociationList<ComponentAssociation> lrlfAssociationList = SAFRAssociationFactory
					.getLogicalRecordToLogicalFileAssociations(
							viewSource.getLrFileAssociation()
									.getAssociatingComponentId(), viewSource
									.getLrFileAssociation().getEnvironmentId());
			for (ComponentAssociation association : lrlfAssociationList) {
				combo.add(UIUtilities
						.getComboString(association
								.getAssociatedComponentName(), Integer
								.parseInt(association
										.getAssociatedComponentIdString())));
				combo.setData(Integer.toString(counter++), association);
			}
		} catch (DAOException e) {
			UIUtilities.handleWEExceptions(e,
					"Database Error in loading Logical Files",
					UIUtilities.titleStringDbException);
		} catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e, "Error in loading Logical Files",null);
		}
	}

	private void refresh() {
        dataSourceGrid.refresh();
        dataSourceGrid.getGrid().setItemHeight(EDITOR_ROW_HEIGHT);
	}
	
	public void setCurrentCellEditor(CellEditor currentCellEditor) {
		this.currentCellEditor = currentCellEditor;
	}

    protected String getRowValue(Object element) {
        DataSourceRow m = (DataSourceRow) element;
        switch (m.getRowType()) {
            case INPUTHEADER :
                break;
            case INLOGICALRECORD :
                ComponentAssociation assoc = viewSource.getLrFileAssociation();                    
                if (assoc != null) {
                    String name = assoc.getAssociatingComponentName();
                    Integer id = assoc.getAssociatingComponentId();
                    return (UIUtilities.getComboString(name, id));
                }
                break;
            case INLOGICALFILE :
                assoc = viewSource.getLrFileAssociation();                    
                if (assoc != null) {
                    Integer lfId = assoc.getAssociatedComponentIdNum();
                    String lfName = assoc.getAssociatedComponentName();
                    return UIUtilities.getComboString(lfName, lfId);
                }
                break;
            case RECORDFILTER :
                return viewSource.getExtractRecordFilter();
            case OUTPUTHEADER :
                break;
            case OUTLOGICALFILE :
                if (viewSource.getExtractFileAssociation() == null || viewSource.getExtractFileAssociationId() == 0 ||
                    viewEditor.getView().isFormatPhaseInUse()) {
                    return "";
                } 
                else {
                    return (viewSource.getExtractFileAssociation().getAssociatingDescriptor());
                }
            case OUTPHYSICALFILE :
                if (viewSource.getExtractFileAssociation() == null || viewSource.getExtractFileAssociationId() == 0 ||
                    viewEditor.getView().isFormatPhaseInUse()) {
                    return "";
                } 
                else {
                    int id = viewSource.getExtractFileAssociation().getAssociatedComponentIdNum();
                    String name = viewSource.getExtractFileAssociation().getAssociatedComponentName();
                    return (UIUtilities.getComboString(name, id));
                }
            case USEREXITNAME :
                if (viewSource.getWriteExit() == null) {
                    return "";
                } 
                else {
                    return viewSource.getWriteExit().getComboString();
                }
            case USEREXITPARM :
                if (viewSource.getWriteExitParams() == null) {
                    return "";                    
                } else {
                    return viewSource.getWriteExitParams();
                }
            case OVERRIDEOUTPUT :
                break;
            case RECORDOUTPUT :
                return viewSource.getExtractRecordOutput();
            default :
                break;
        }
        return "";
    }
    
    protected Color getColorPurple() {
        if (purple == null) {
            purple = new Color(Display.getCurrent(), 230, 140, 255);            
        }
        return purple;
    }

    @Override
	public void dispose() {
    	if(boldFont != null) {
    		boldFont.dispose();
    	}
	}
    
}
