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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.jface.gridviewer.GridViewerEditor;
import org.eclipse.nebula.jface.gridviewer.GridViewerRow;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordFieldQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditor;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditorInput;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;
import com.ibm.safr.we.ui.utilities.AssociatedLRFieldSorter;
import com.ibm.safr.we.ui.utilities.ColumnSelectionListenerForTableCombo;
import com.ibm.safr.we.ui.utilities.DepCheckOpener;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.RowEditorType;
import com.ibm.safr.we.ui.utilities.SAFRComboBoxCellEditor;
import com.ibm.safr.we.ui.utilities.SAFRTextCellEditor;
import com.ibm.safr.we.ui.utilities.TableComboViewerCellEditor;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.utilities.VerifyNumericListener;
import com.ibm.safr.we.ui.utilities.WidgetType;
import com.ibm.safr.we.ui.views.metadatatable.LogicalRecordTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.LookupTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.MainTableLabelProvider;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ColumnSourceView extends ViewPart implements ISelectionListener {

    static final Logger logger = Logger
    .getLogger("com.ibm.safr.we.ui.views.vieweditor.ColumnSourceView");
    
    public enum MouseHover {
        LRF,
        LR,
        LU,
        LUF,
        OTHER
      };
    
	public static String ID = "SAFRWE.ColumnSourceView";

	private GridTableViewer columnSourceTableViewer;

	private Button overwrite;
	private Composite composite;
	private ViewEditor vEditor;

	private ViewColumnSource viewColumnSource;

    public enum RowType {
        COLUMN_SOURCE_TYPE("Column Source Type", RowEditorType.COMBO),
        COLUMN_SOURCE_VALUE("Column Source Value", RowEditorType.TEXT),
        LOOKUP_LR("Lookup LR", RowEditorType.COMBO),
        LOOKUP_PATH("Lookup Path", RowEditorType.COMBO),
        LOOKUP_FIELD("Lookup Field", RowEditorType.COMBO),
        EFFECTIVE_DATE_TYPE("Effective Date Type", RowEditorType.COMBO),
        EFFECTIVE_DATE_VALUE("Effective Date Value", RowEditorType.TEXT),
        DATATYPE("Data Type", RowEditorType.NONE),
        DATE_TIME_FORMAT("Date/Time Format", RowEditorType.NONE),
        LENGTH("Length", RowEditorType.NONE),
        DATA_ALIGNMENT("Data Alignment", RowEditorType.NONE),
        DECIMAL_PLACES("Decimal Places", RowEditorType.NONE),
        SCALING_FACTOR("Scaling Factor", RowEditorType.NONE),
        SIGNED("Signed", RowEditorType.NONE),
        NUMERIC_MASK("Numeric Mask", RowEditorType.NONE),
        LR_START_POSITION("LR Start Position", RowEditorType.NONE);
        
        private String name;
        private RowEditorType type;
        
        RowType(String name, RowEditorType type) {
            this.name = name;
            this.type = type;
        }
        
        public String getName() {
            return name;
        }

        public RowEditorType getRowEditorType() {
            return type;
        }

        public void setRowEditorType(RowEditorType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return name;
        }
    }	

	public TableComboViewerCellEditor editor1;

	private EditingSupportImpl tableEditingSupport;

	private static final int MAX_COLUMN_SRC_VALUE = 255;
	private static final int MAX_EFFECTIVE_DATE_VALUE = 24;

	private CellEditor currentCellEditor;

	private MouseHover hover = MouseHover.OTHER;
    private MenuItem gridOpen = null;	
    private MenuItem gridOpenDep = null;   
	
	@Override
	public void createPartControl(Composite parent) {
    
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FormLayout());
		composite.setLayoutData(new FormData());

        overwrite = new Button(composite, SWT.CHECK);
        overwrite.setText("");
        FormData overData = new FormData();
        overData.top = new FormAttachment(0, 0);
        overData.right = new FormAttachment(100, -5);
        overwrite.setLayoutData(overData);
        overwrite.setSelection(true);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Overwrite column");
        FormData labData = new FormData();
        labData.top = new FormAttachment(0, 0);
        labData.right = new FormAttachment(overwrite, -10);
        label.setLayoutData(labData);
		

		columnSourceTableViewer = new GridTableViewer(composite,
				SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.BORDER);

		columnSourceTableViewer.getGrid().setLinesVisible(true);
		columnSourceTableViewer.getGrid().setHeaderVisible(true);
		columnSourceTableViewer.setContentProvider(new ContentProvider());
		columnSourceTableViewer.getGrid().setRowHeaderVisible(true);
		columnSourceTableViewer.getGrid().setCellSelectionEnabled(true);

		addGridOpenEditorMenu();
		
		columnSourceTableViewer.setRowHeaderLabelProvider(new ColumnLabelProvider() {
		    
			@Override
			public String getText(Object element) {
				RowType type = (RowType) element;
				return type.getName();

			}
		});

		FormData tableData = new FormData();
		tableData.top = new FormAttachment(overwrite, 0);
		tableData.bottom = new FormAttachment(100, 0);
		tableData.left = new FormAttachment(0, 0);
		tableData.right = new FormAttachment(100, 0);
		columnSourceTableViewer.getGrid().setLayoutData(tableData);

		final GridViewerColumn column = new GridViewerColumn(
				columnSourceTableViewer, SWT.NONE);
		LabelProviderImpl labelProvider = new LabelProviderImpl();
		column.setLabelProvider(labelProvider);
		tableEditingSupport = new EditingSupportImpl(columnSourceTableViewer);
		column.setEditingSupport(tableEditingSupport);

		column.setLabelProvider(new LabelProviderImpl());
		column.setEditingSupport(new EditingSupportImpl(columnSourceTableViewer));
		column.getColumn().setWidth(200);
		column.getColumn().setResizeable(true);

		columnSourceTableViewer.setInput(1);

		getSite().getPage().addSelectionListener(this);

		composite.addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event event) {
				column.getColumn().setWidth(
				    composite.getSize().x - columnSourceTableViewer.getGrid().getItemHeaderWidth());
			}
		});

		columnSourceTableViewer.getGrid().addTraverseListener(new TraverseListener() {

            private void editNeighborCell(TraverseEvent e, Point pt, boolean up) {
                
                GridViewerEditor editor = (GridViewerEditor)columnSourceTableViewer.getColumnViewerEditor();
                GridItem item = columnSourceTableViewer.getGrid().getItem(pt.y);
                ViewerRow row = new GridViewerRow(item);
                ViewerCell cell = editor.searchCellAboveBelow(editor, row, editor.getGridViewer(), pt.x, up);
                if (cell == null) {
                    return;
                }
                boolean found = false;
                GridItem items[] = columnSourceTableViewer.getGrid().getItems();
                int rowIdx = 0;
                for ( ; rowIdx <items.length ; rowIdx++) {
                    if (((GridItem)cell.getViewerRow().getItem()) == items[rowIdx]) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    columnSourceTableViewer.editElement(RowType.values()[rowIdx], cell.getColumnIndex());
                }
            }
            
            public void keyTraversed(TraverseEvent e) {
                e.doit = false;
                if (columnSourceTableViewer.getGrid().getItemCount() == 0 ||
                    columnSourceTableViewer.getGrid().getCellSelectionCount() == 0) {
                    return;
                }
                Point pt = columnSourceTableViewer.getGrid().getCellSelection()[0];
                if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
                    if (pt != null) {
                        editNeighborCell(e,pt,false);
                    }
                } 
                else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                    if (pt != null) {
                        editNeighborCell(e,pt,true);
                    }   
                } 
            }
        });
		
		columnSourceTableViewer.getGrid().addMouseListener(new MouseAdapter() {

		    boolean doubleClick = false;
		    
            @Override
            public void mouseDown(MouseEvent e) {
                Point pos = columnSourceTableViewer.getGrid().getCell(new Point(e.x, e.y));
                Grid g = (Grid) e.getSource();
                if (pos == null) {
	                g.setData(SAFRLogger.USER, null);                	
                }
                else {
	                g.setData(SAFRLogger.USER, (RowType.values()[pos.y]).getName());
                }
            	
                if (e.button == 3)
                {
                    Point pt = new Point(e.x, e.y);
                    Point cell = columnSourceTableViewer.getGrid().getCell(pt);
                    if (cell == null) {
                        hover = MouseHover.OTHER;                                                                        
                        gridOpen.setEnabled(false);    
                        gridOpenDep.setEnabled(false);
                    }
                    else if (cell.y == 1) {
                        Code sourceType = viewColumnSource.getSourceType();
                        if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                            hover = MouseHover.LRF;                        
                            String lrfname = columnSourceTableViewer.getGrid().getItem(1).getText(0);
                            if (lrfname == null || lrfname == "") {
                                gridOpen.setEnabled(false);
                                gridOpenDep.setEnabled(false);
                            }
                            else {
                                gridOpen.setEnabled(true);
                                gridOpenDep.setEnabled(true);
                            }
                        }
                        else {
                            hover = MouseHover.OTHER;                                                
                            gridOpen.setEnabled(false);                            
                            gridOpenDep.setEnabled(false);
                        }
                    }                    
                    else if (cell.y == 2) {
                        hover = MouseHover.LR;
                        String lrname = columnSourceTableViewer.getGrid().getItem(2).getText(0);
                        if (lrname == null || lrname == "") {
                            gridOpen.setEnabled(false);
                            gridOpenDep.setEnabled(false);
                        }
                        else {
                            gridOpen.setEnabled(true);
                            gridOpenDep.setEnabled(true);
                        }
                    }
                    else if (cell.y == 3) {
                        hover = MouseHover.LU;                        
                        String luname = columnSourceTableViewer.getGrid().getItem(3).getText(0);
                        if (luname == null || luname == "") {
                            gridOpen.setEnabled(false);
                            gridOpenDep.setEnabled(false);
                        }
                        else {
                            gridOpen.setEnabled(true);
                            gridOpenDep.setEnabled(true);
                        }
                    }
                    else if (cell.y == 4) {
                        hover = MouseHover.LUF;                        
                        String lufname = columnSourceTableViewer.getGrid().getItem(4).getText(0);
                        if (lufname == null || lufname == "") {
                            gridOpen.setEnabled(false);
                            gridOpenDep.setEnabled(false);
                        }
                        else {
                            gridOpen.setEnabled(true);
                            gridOpenDep.setEnabled(true);
                        }
                    }
                    else {
                        hover = MouseHover.OTHER;                                                
                        gridOpen.setEnabled(false);
                        gridOpenDep.setEnabled(false);
                    }
                    return;
                }               
            }
		    
		    @Override
            public void mouseUp(MouseEvent e) {
                if (e.button != 1)
                {
                    return;
                }		        
		        if (doubleClick) {
					// This method will be called twice on a double click
					// as it has 2 mouse up events. Ignore the 2nd one.
		            doubleClick = false;
		            return;
		        }
		        
                Point to = columnSourceTableViewer.getGrid().getCell(new Point(e.x, e.y));
                Grid g = (Grid) e.getSource();
                Point from = g.getFocusCell();
                
                if (e.x > g.getItemHeaderWidth()) {
                	// clicked right of the row headers
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
                	// clicked in the row headers
    		        if (currentCellEditor != null && currentCellEditor instanceof SAFRComboBoxCellEditor) {
    		        	SAFRComboBoxCellEditor combo = (SAFRComboBoxCellEditor) currentCellEditor;
    		        	combo.focusLost();
    		        }
                }
		        
		    }
		    
			@Override
			public void mouseDoubleClick(MouseEvent e) {
                if (e.button != 1)
                {
                    return;
                }			    
			    doubleClick = true;
				Grid g = (Grid) e.getSource();

				if (e.x > g.getItemHeaderWidth()) {
					// Click was not in row header column.
					GridItem item = g.getItem(new Point(e.x, e.y));
					if (item != null) {
					    RowType type = (RowType) item.getData();
						if (type == RowType.COLUMN_SOURCE_VALUE && 
						    viewColumnSource.getSourceType() != null && 
						    viewColumnSource.getSourceType().getGeneralId() == Codes.FORMULA) {

							// Open the Logic Text editor tab
							LogicTextEditorInput input = new LogicTextEditorInput(
									viewColumnSource, vEditor);
							try {
								PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow()
										.getActivePage()
										.openEditor(input, LogicTextEditor.ID);
							} catch (PartInitException pie) {
								UIUtilities.handleWEExceptions(pie,
								    "Unexpected error occurred while opening Logic Text editor.",null);
							}
						}
					}
				}
			}
		});

		// provide keyboard activation support. the editor will be activated
		// on 'Spacebar' from keyboard and on left mouse button double click.
		
        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
                columnSourceTableViewer) {
            protected boolean isEditorActivationEvent(
                    ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                        || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.character == ' '))
                        || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
                        || (event.sourceEvent instanceof MouseEvent)
                        && ((MouseEvent) event.sourceEvent).button == 1;
            }
        };

        GridViewerEditor.create(columnSourceTableViewer, actSupport,
                ColumnViewerEditor.KEYBOARD_ACTIVATION
                | ColumnViewerEditor.TABBING_VERTICAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                | GridViewerEditor.SELECTION_FOLLOWS_EDITOR);
		showGridForCurrentEditor(getSite().getPage().getActiveEditor());
	}

    private void addGridOpenEditorMenu()
    {        
        Menu menu = new Menu(columnSourceTableViewer.getGrid());        
        gridOpen = new MenuItem(menu, SWT.PUSH);
        gridOpen.setText("Open Editor");
        gridOpen.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                switch (hover) {
                case LRF :
                    String lrfname = columnSourceTableViewer.getGrid().getItem(1).getText(0);
                    // {lrfname} [29945]
                    EditorOpener.open(UIUtilities.extractId(lrfname), ComponentType.LogicalRecordField);                    
                    break;                    
                case LR :
                    String lrname = columnSourceTableViewer.getGrid().getItem(2).getText(0);
                    // lrname [29945]
                    EditorOpener.open(UIUtilities.extractId(lrname), ComponentType.LogicalRecord);                                        
                    break;
                case LU :
                    String luname = columnSourceTableViewer.getGrid().getItem(3).getText(0);
                    // {luname} [29945]
                    EditorOpener.open(UIUtilities.extractId(luname), ComponentType.LookupPath);                    
                    break;
                case LUF :
                    String lufname = columnSourceTableViewer.getGrid().getItem(4).getText(0);
                    // {lufname} [29945]
                    EditorOpener.open(UIUtilities.extractId(lufname), ComponentType.LogicalRecordField);                    
                    break;
                default:
                    break;
                }
            }
        });
        
        gridOpenDep = new MenuItem(menu, SWT.PUSH);
        gridOpenDep.setText("DependencyChecker");
        gridOpenDep.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                switch (hover) {
                case LRF :
                    String lrfname = columnSourceTableViewer.getGrid().getItem(1).getText(0);
                    Integer lrfId = UIUtilities.extractId(lrfname);
                    Integer lrId = viewColumnSource.getViewSource().getLrFileAssociation().getAssociatingComponentId();
                    DepCheckOpener.open(new LogicalRecordFieldQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(), 
                        lrfId, lrId, 
                        null, null, null, null, null));                        
                    break;                    
                case LR :
                    String lrname = columnSourceTableViewer.getGrid().getItem(2).getText(0);
                    lrId = UIUtilities.extractId(lrname);
                    DepCheckOpener.open(new LogicalRecordQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(), 
                        lrId, 
                        null, null, null, null, null, null, null, null, null, null, null, null));                        
                    break;
                case LU :
                    String luname = columnSourceTableViewer.getGrid().getItem(3).getText(0);
                    Integer luId = UIUtilities.extractId(luname);
                    DepCheckOpener.open(new LookupQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(), 
                        luId, 
                        null, null, 0, 0, null, null, null, null, null, null, null, null, null));                        
                    break;
                case LUF :
                    String lfname = columnSourceTableViewer.getGrid().getItem(4).getText(0);
                    Integer lfId = UIUtilities.extractId(lfname);
                    lrname = columnSourceTableViewer.getGrid().getItem(2).getText(0);
                    lrId = UIUtilities.extractId(lrname);
                    DepCheckOpener.open(new LogicalRecordFieldQueryBean(
                        SAFRApplication.getUserSession().getEnvironment().getId(), 
                        lfId, lrId, 
                        null, null, null, null, null));                        
                    break;
                default:
                    break;
                }
            }
        });        
        
        columnSourceTableViewer.getGrid().setMenu(menu);
    }       
    
    private void addLRComboOpenEditorMenu(final TableCombo combo)
    {
        Menu menu = combo.getTextControl().getMenu();
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Open Editor");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                String text = combo.getTextControl().getText();
                Integer id = UIUtilities.extractId(text);
                EditorOpener.open(id, ComponentType.LogicalRecord);                                                            
            }
        });
        MenuItem item2 = new MenuItem(menu, SWT.PUSH);
        item2.setText("DependencyChecker");
        item2.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                String text = combo.getTextControl().getText();
                Integer id = UIUtilities.extractId(text);
                DepCheckOpener.open(new LogicalRecordQueryBean(
                    SAFRApplication.getUserSession().getEnvironment().getId(), 
                    id, 
                    null, null, null, null, null, null, null, null, null, null, null, null));                        
            }
        });                
    }       

    private void addLUComboOpenEditorMenu(final TableCombo combo)
    {
        Menu menu = combo.getTextControl().getMenu();
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Open Editor");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                String text = combo.getTextControl().getText();
                Integer id = UIUtilities.extractId(text);
                EditorOpener.open(id, ComponentType.LookupPath);                                                            
            }
        });
        
        MenuItem item2 = new MenuItem(menu, SWT.PUSH);
        item2.setText("DependencyChecker");
        item2.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                String text = combo.getTextControl().getText();
                Integer id = UIUtilities.extractId(text);
                DepCheckOpener.open(new LookupQueryBean(
                    SAFRApplication.getUserSession().getEnvironment().getId(), 
                    id, 
                    null, null, 0, 0, null, null, null, null, null, null, null, null, null));                        
            }
        });                
        
    }       

    private void addLRFComboOpenEditorMenu(final TableCombo combo)
    {
        Menu menu = combo.getTextControl().getMenu();
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Open Editor");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                String text = combo.getTextControl().getText();
                Integer id = UIUtilities.extractId(text);
                EditorOpener.open(id, ComponentType.LogicalRecordField);                                                            
            }
        });    
        MenuItem item2 = new MenuItem(menu, SWT.PUSH);
        item2.setText("DependencyChecker");
        item2.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                String text = combo.getTextControl().getText();
                Integer id = UIUtilities.extractId(text);
                Integer lrId = viewColumnSource.getViewSource().getLrFileAssociation().getAssociatingComponentId();
                DepCheckOpener.open(new LogicalRecordFieldQueryBean(
                    SAFRApplication.getUserSession().getEnvironment().getId(), 
                    id, lrId, 
                    null, null, null, null, null));                        
            }
        });                
        
    }       

    private void addLUFComboOpenEditorMenu(final TableCombo combo)
    {
        Menu menu = combo.getTextControl().getMenu();
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Open Editor");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                String text = combo.getTextControl().getText();
                Integer id = UIUtilities.extractId(text);
                EditorOpener.open(id, ComponentType.LogicalRecordField);                                                            
            }
        });    
        
        MenuItem item2 = new MenuItem(menu, SWT.PUSH);
        item2.setText("DependencyChecker");
        item2.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                String text = combo.getTextControl().getText();
                Integer id = UIUtilities.extractId(text);
                String lrname = columnSourceTableViewer.getGrid().getItem(2).getText(0);
                Integer lrId = UIUtilities.extractId(lrname);                
                DepCheckOpener.open(new LogicalRecordFieldQueryBean(
                    SAFRApplication.getUserSession().getEnvironment().getId(), 
                    id, lrId, 
                    null, null, null, null, null));                        
            }
        });                
        
    }       
    
	@Override
	public void setFocus() {

	}

	public void editFirstCell() 
	{
	    columnSourceTableViewer.editElement(RowType.values()[0], 0);
	}
	
	private class ContentProvider implements IStructuredContentProvider {
		public ContentProvider() {
		}

		public Object[] getElements(Object inputElement) {
			if (viewColumnSource != null) {
				return RowType.values();
			} else {
				return new RowType[] {};
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
		    RowType type = (RowType) element;
			switch (type) {
			case SIGNED:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() == Codes.CONSTANT
							|| viewColumnSource.getSourceType().getGeneralId() == Codes.FORMULA) {
						return null;
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD ||
					           viewColumnSource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
						return "";
					}
				}
				break;
			case EFFECTIVE_DATE_TYPE:
				if (viewColumnSource.getSourceType() != null) {
					if ((viewColumnSource.getSourceType().getGeneralId() != Codes.LOOKUP_FIELD) || 
					    (viewColumnSource.getView().getOutputFormat() == OutputFormat.Extract_Source_Record_Layout)) {
						return null;
					} else if (viewColumnSource.getEffectiveDateTypeCode() != null) {
						return viewColumnSource.getEffectiveDateTypeCode().getDescription();
					}
				}
				break;
			case EFFECTIVE_DATE_VALUE:
				if (viewColumnSource.getSourceType() != null) {
					if ((viewColumnSource.getSourceType().getGeneralId() != Codes.LOOKUP_FIELD) || 
					    (viewColumnSource.getView().getOutputFormat() == OutputFormat.Extract_Source_Record_Layout)) {
						return null;
					} else {
						Code effectiveDateType = viewColumnSource.getEffectiveDateTypeCode();
						if (effectiveDateType != null) {
							if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_CONSTANT) {
								return viewColumnSource.getEffectiveDateValue();
							} else if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_RUNDATE) {
								return "Runday()"; 
							} else if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_SOURCE_FILE_FIELD) {
							    LRField effectiveDateLRField = viewColumnSource.getEffectiveDateLRField();
								if (effectiveDateLRField != null) {
									return UIUtilities.getComboString(
											effectiveDateLRField.getName(),
											effectiveDateLRField.getId());
								}
							}
						}
					}
				}
				break;

			case LOOKUP_LR:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() != Codes.LOOKUP_FIELD) {
						return null;

					} else if (viewColumnSource.getLogicalRecordQueryBean() != null) {
						return UIUtilities.getComboString(
						    viewColumnSource.getLogicalRecordQueryBean().getName(), 
						    viewColumnSource.getLogicalRecordQueryBean().getId());
					}
				}
				break;

			case LOOKUP_PATH:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() != Codes.LOOKUP_FIELD) {
						return null;

					} else if (viewColumnSource.getLookupQueryBean() != null) {
						return UIUtilities.getComboString(
						    viewColumnSource.getLookupQueryBean().getName(), 
						    viewColumnSource.getLookupQueryBean().getId());
					}
				}
				break;
			case LOOKUP_FIELD:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() != Codes.LOOKUP_FIELD) {
						return null;

					} else if (viewColumnSource.getLRField() != null) {
						return UIUtilities.getComboString(
						    viewColumnSource.getLRField().getName(), 
						    viewColumnSource.getLRField().getId());
					}
				}
				break;

			case DATE_TIME_FORMAT:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() == Codes.CONSTANT
							|| viewColumnSource.getSourceType().getGeneralId() == Codes.FORMULA) {
						return null;
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
					    LRField sourceLRField= viewColumnSource.getLRField();
						if (sourceLRField != null && 
						    sourceLRField.getDateTimeFormatCode() != null) {
							return sourceLRField.getDateTimeFormatCode().getDescription();
						}
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                        LRField lookupField= viewColumnSource.getLRField();
						if (lookupField != null && 
						    lookupField.getDateTimeFormatCode() != null) {
							return lookupField.getDateTimeFormatCode().getDescription();
						}
					}
				}
				break;
			case DATATYPE:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() == Codes.CONSTANT
							|| viewColumnSource.getSourceType().getGeneralId() == Codes.FORMULA) {
						return null;
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                        LRField sourceLRField= viewColumnSource.getLRField();
						if (sourceLRField != null) {
							return sourceLRField.getDataTypeCode()
									.getDescription();
						}
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                        LRField lookupField= viewColumnSource.getLRField();
						if (lookupField != null) {
							return lookupField.getDataTypeCode()
									.getDescription();
						}
					}
				}
				break;

			case LENGTH:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() == Codes.CONSTANT
							|| viewColumnSource.getSourceType().getGeneralId() == Codes.FORMULA) {
						return null;
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                        LRField sourceLRField= viewColumnSource.getLRField();
						if (sourceLRField != null) {
							return sourceLRField.getLength().toString();
						}
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                        LRField lookupField= viewColumnSource.getLRField();
						if (lookupField != null) {
							return lookupField.getLength().toString();
						}
					}
				}
				break;
			case DATA_ALIGNMENT:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() == Codes.CONSTANT
							|| viewColumnSource.getSourceType().getGeneralId() == Codes.FORMULA) {
						return null;
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                        LRField sourceLRField= viewColumnSource.getLRField();
						if (sourceLRField != null
								&& sourceLRField.getHeaderAlignmentCode() != null) {
							return sourceLRField.getHeaderAlignmentCode()
									.getDescription();
						}
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                        LRField lookupField= viewColumnSource.getLRField();
						if (lookupField != null
								&& lookupField.getHeaderAlignmentCode() != null) {
							return lookupField.getHeaderAlignmentCode()
									.getDescription();
						}
					}
				}
				break;
			case DECIMAL_PLACES:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                        LRField sourceLRField= viewColumnSource.getLRField();
						if (sourceLRField != null) {
							if (sourceLRField.getDataTypeCode() != null && 
							    sourceLRField.getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC) {
								return null;
							} else if (sourceLRField != null
									&& sourceLRField.getDecimals() != null) {
								return sourceLRField.getDecimals().toString();
							}
						}
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                        LRField lookupField= viewColumnSource.getLRField();
						if (lookupField != null) {
							if (lookupField.getDataTypeCode() != null && 
							    lookupField.getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC) {
								return null;
							} else if (lookupField != null && 
							    lookupField.getDecimals() != null) {
								return lookupField.getDecimals().toString();
							}
						}
					}
				}
				break;
			case SCALING_FACTOR:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                        LRField sourceLRField= viewColumnSource.getLRField();
						if (sourceLRField != null) {
							if (sourceLRField.getDataTypeCode() != null && 
							    sourceLRField.getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC) {
								return null;
							} else if (sourceLRField != null && 
							    sourceLRField.getScaling() != null) {
								return sourceLRField.getScaling().toString();
							}
						}
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                        LRField lookupField= viewColumnSource.getLRField();
						if (lookupField != null) {
							if (lookupField.getDataTypeCode() != null && 
							    lookupField.getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC) {
								return null;
							} else if (lookupField != null && 
							    lookupField.getScaling() != null) {
								return lookupField.getScaling().toString();
							}
						}
					}
				}
				break;

			case NUMERIC_MASK:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() == Codes.CONSTANT || 
					    viewColumnSource.getSourceType().getGeneralId() == Codes.FORMULA) {
						return null;
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                        LRField sourceLRField= viewColumnSource.getLRField();
						if (sourceLRField != null&& sourceLRField.getNumericMaskCode() != null) {
							return sourceLRField.getNumericMaskCode().getDescription();
						}
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                        LRField lookupField= viewColumnSource.getLRField();
						if (lookupField != null && lookupField.getNumericMaskCode() != null) {
							return lookupField.getNumericMaskCode().getDescription();
						}
					}
				}
				break;
			case LR_START_POSITION:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() == Codes.CONSTANT || 
					    viewColumnSource.getSourceType().getGeneralId() == Codes.FORMULA) {
						return null;
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                        LRField sourceLRField= viewColumnSource.getLRField();
						if (sourceLRField != null && sourceLRField.getPosition() != null) {
							return sourceLRField.getPosition().toString();
						}
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                        LRField lookupField= viewColumnSource.getLRField();
						if (lookupField != null && lookupField.getPosition() != null) {
							return lookupField.getPosition().toString();
						}
					}
				}
				break;
            case COLUMN_SOURCE_TYPE:
                Code sourceType = viewColumnSource.getSourceType();
                return sourceType.getDescription();
            case COLUMN_SOURCE_VALUE:
                return getColumnSourceValue();
            default:
                break;
			}
			return "";
		}

		@Override
		public Image getImage(Object element) {

			RowType type = (RowType) element;
			switch (type) {
			case SIGNED:
				if (viewColumnSource.getSourceType() != null) {
					if (viewColumnSource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                        LRField sourceLRField= viewColumnSource.getLRField();
						if (sourceLRField != null) {
							if ((sourceLRField.getDataTypeCode() != null) && 
							    (sourceLRField.getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC || 
							     sourceLRField.getDataTypeCode().getGeneralId() == Codes.BINARY_CODED_DECIMAL)) {
								return null;
							} else {
								if (sourceLRField.isSigned()) {
									return UIUtilities.getAndRegisterImage(ImageKeys.CHECKED);
								} else {
									return UIUtilities.getAndRegisterImage(ImageKeys.UNCHECKED);
								}
							}
						}
					} else if (viewColumnSource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                        LRField lookupField= viewColumnSource.getLRField();
						if (lookupField != null) {
							if ((lookupField.getDataTypeCode() != null) && 
							    (lookupField.getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC || 
							     lookupField.getDataTypeCode().getGeneralId() == Codes.BINARY_CODED_DECIMAL)) {
								return null;
							} else {
								if (lookupField.isSigned()) {
									return UIUtilities.getAndRegisterImage(ImageKeys.CHECKED);
								} else {
									return UIUtilities.getAndRegisterImage(ImageKeys.UNCHECKED);
								}
							}
						}
					}
				}
            default:
                break;

			}
			return null;

		}

		@Override
		public Color getBackground(Object element) {
			RowType type = (RowType) element;
			if (type.getRowEditorType() == RowEditorType.NONE) {
				// no editor means the cell is disabled. set its color to grey.
				return UIUtilities.getColorForDisabledCell();
			}

			Code sourceType = viewColumnSource.getSourceType();
			switch (type) {

			case COLUMN_SOURCE_VALUE:
				if (sourceType != null) {
					if ((sourceType.getGeneralId() == Codes.LOOKUP_FIELD)) {
						return UIUtilities.getColorForDisabledCell();
					}
				}
				break;
			case LOOKUP_LR:
			case LOOKUP_PATH:
			case LOOKUP_FIELD:
				if (sourceType != null) {
					if (!(sourceType.getGeneralId() == Codes.LOOKUP_FIELD)) {
						return UIUtilities.getColorForDisabledCell();
					}
				}
				break;
			case EFFECTIVE_DATE_TYPE:
				if (viewColumnSource.getView().getOutputFormat() == OutputFormat.Extract_Source_Record_Layout) {
					return UIUtilities.getColorForDisabledCell();
				} else {
					if (sourceType != null) {
						if (!(sourceType.getGeneralId() == Codes.LOOKUP_FIELD)) {
							return UIUtilities.getColorForDisabledCell();
						}
					}
				}
				break;
			case EFFECTIVE_DATE_VALUE:
				if (viewColumnSource.getView().getOutputFormat() == OutputFormat.Extract_Source_Record_Layout) {
					return UIUtilities.getColorForDisabledCell();
				} else {
					if (sourceType != null) {
						if (!(sourceType.getGeneralId() == Codes.LOOKUP_FIELD)) {
							return UIUtilities.getColorForDisabledCell();
						} else {
							Code effectiveDateType = viewColumnSource
									.getEffectiveDateTypeCode();
							if (effectiveDateType != null) {
								if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_RUNDATE) {
									return UIUtilities
											.getColorForDisabledCell();
								}
							}
						}
					}
				}
				break;
            default:
                break;
			}
			return super.getBackground(element);
		}

		@Override
		public Color getForeground(Object element) {
			return null;
		}
	}

	private class EditingSupportImpl extends EditingSupport {

		private GridTableViewer viewer;
		private CellEditor editor;
		private CCombo columnType;
        private CCombo effectiveDateType;
		private TableCombo editorTableCombo;
		private Text editorText;
		private TableComboViewer editorTableComboViewer;

		public EditingSupportImpl(final GridTableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected boolean canEdit(Object element) {
		    
            RowType type = (RowType) element;
            ViewEditorInput viewEditorInput = (ViewEditorInput) vEditor.getEditorInput();
            if (viewEditorInput.getEditRights() == EditRights.Read) {
                return false;
            }
            
            if (type.getRowEditorType() == RowEditorType.NONE) {
                return false;
            }

            Code curDateType = null;
            if (effectiveDateType != null) {
                curDateType = (Code) effectiveDateType.getData(new Integer(
                        effectiveDateType.getSelectionIndex()).toString());
            }
            if (curDateType == null) {
                curDateType = viewColumnSource.getEffectiveDateTypeCode();
            }
            Code curSourceType = null;
            if (columnType != null) {
                curSourceType = (Code) columnType.getData(new Integer(
                        columnType.getSelectionIndex()).toString());
            }
            if (curSourceType == null) {
                curSourceType = viewColumnSource.getSourceType();
            }

            switch (type) {

            case COLUMN_SOURCE_VALUE:
            if (curSourceType != null) {
                if (curSourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    return false;
                }
                else {
                    return true;
                }
            }
            break;
            case LOOKUP_LR:
            if (curSourceType != null && curSourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                return true;
            }
            case LOOKUP_PATH:
            case LOOKUP_FIELD:
            if (curSourceType != null) {
                if (!(curSourceType.getGeneralId() == Codes.LOOKUP_FIELD)) {
                    return false;
                }
            }
            break;
            case EFFECTIVE_DATE_TYPE:            
            if (viewColumnSource.getView().getOutputFormat() == OutputFormat.Extract_Source_Record_Layout) {
                return false;
            } else {
                if (curSourceType != null) {
                    if (!(curSourceType.getGeneralId() == Codes.LOOKUP_FIELD)) {
                        return false;
                    }
                }
            }
                break;
            case EFFECTIVE_DATE_VALUE:
            if (viewColumnSource.getView().getOutputFormat() == OutputFormat.Extract_Source_Record_Layout) {
                return false;
            } else {
                if (curSourceType != null) {
                    if (!(curSourceType.getGeneralId() == Codes.LOOKUP_FIELD)) {
                        return false;
                    } else {
                        if (curDateType != null) {
                            if (curDateType.getGeneralId() == Codes.RELPERIOD_RUNDATE) {
                                return false;
                            }
                        }
                    }
                }
            }
                break;
            default:
                break;
            }
            return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			setCurrentCellEditor(null);
			RowType type = (RowType) element;
			// changing editors based on Column Source Type
			if (type == RowType.COLUMN_SOURCE_VALUE) {
				try {
					Code sourceType = viewColumnSource.getSourceType();
					if (sourceType != null) {
						if (sourceType.getGeneralId() == Codes.CONSTANT) {
							editor = new SAFRTextCellEditor(viewer.getGrid());
							editorText = (Text) editor.getControl();
							((Text) editor.getControl()).setTextLimit(MAX_COLUMN_SRC_VALUE);
							type.setRowEditorType(RowEditorType.TEXT);
						} else if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
							editor = new TableComboViewerCellEditor(((GridTableViewer) viewer).getGrid());
							editorTableComboViewer = ((TableComboViewerCellEditor) editor).getViewer();
							editorTableCombo = editorTableComboViewer.getTableCombo();
							populateValueCombo(editorTableComboViewer,RowType.COLUMN_SOURCE_VALUE, false);							
							editorTableComboViewer.setSorter(new AssociatedLRFieldSorter(1,SWT.UP));
							type.setRowEditorType(RowEditorType.COMBO);
							editorTableCombo.addFocusListener(new FocusListener() {

                                @Override
                                public void focusGained(FocusEvent e) {
                                }

                                @Override
                                public void focusLost(FocusEvent e) {
                                    viewer.refresh();
                                }
							    
							});
							addLRFComboOpenEditorMenu(editorTableCombo);							
						} else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
							editor = null;
						} else if (sourceType.getGeneralId() == Codes.FORMULA) {
                            ViewEditor viewEditor = (ViewEditor) getSite().getPage().getActiveEditor();

                            // If View's editor is not currently active we have to search all open editors
                            // and find it.
                            if (viewEditor == null) {
                                IEditorReference refs[] = getSite().getPage().getEditorReferences();
                                for (IEditorReference ref : refs) {
                                    IEditorPart part = ref.getEditor(false);
                                    if (part instanceof ViewEditor) {
                                        ViewEditor edit = (ViewEditor)part;
                                        if (viewColumnSource.getView().getId() ==  edit.getView().getId()) {
                                            viewEditor = edit;
                                            break;
                                        }
                                    }
                                }  
                                if (viewEditor == null) {
                                    throw new SAFRFatalException("Open View Editor could not be found for this formula.");
                                }
                            }
							editor = new LogicTextDialogCellEditor(viewer
									.getGrid(), viewColumnSource, viewEditor);

							type.setRowEditorType(RowEditorType.DIALOG);
						}
					}
					return editor;
				} catch (DAOException de) {
					UIUtilities.handleWEExceptions(de,
					    "Unexpected database error occurred while retrieving Column Source Value.",
					    UIUtilities.titleStringDbException);
				} catch (SAFRException e) {
					UIUtilities.handleWEExceptions(e,
					    "Unexpected error occurred while retrieving Column Source Value.",null);
				}

			} else if (type == RowType.EFFECTIVE_DATE_VALUE) {
				// changing editors based on Effective Date Type
				try {
					Code effectiveDateType = viewColumnSource.getEffectiveDateTypeCode();
					if (effectiveDateType != null) {
						if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_RUNDATE) {
							editor = null;
						} else if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_SOURCE_FILE_FIELD) {
							editor = new TableComboViewerCellEditor(((GridTableViewer) viewer).getGrid());
							editorTableComboViewer = ((TableComboViewerCellEditor) editor).getViewer();
							editorTableCombo = editorTableComboViewer.getTableCombo();
							populateValueCombo(editorTableComboViewer,RowType.EFFECTIVE_DATE_VALUE, true);
							editorTableComboViewer.setSorter(new AssociatedLRFieldSorter(1,SWT.UP));
							type.setRowEditorType(RowEditorType.COMBO);
						} else if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_CONSTANT) {
							editor = new SAFRTextCellEditor(viewer.getGrid());
							editorText = (Text) editor.getControl();
							editorText.addVerifyListener(new VerifyNumericListener(WidgetType.INTEGER, false));
							((Text) editor.getControl()).setTextLimit(MAX_EFFECTIVE_DATE_VALUE);
							type.setRowEditorType(RowEditorType.TEXT);
						}

						return editor;
					}
				} catch (DAOException de) {
					UIUtilities.handleWEExceptions(de,
					    "Unexpected database error occurred while retrieving Effective Date Value.",
					    UIUtilities.titleStringDbException);
				} catch (SAFRException e) {
					UIUtilities.handleWEExceptions(e,
					    "Unexpected error occurred while retrieving Effective Date Value.",null);
				}
			}
			if (type.getRowEditorType() == RowEditorType.COMBO) {
				editor = new SAFRComboBoxCellEditor(viewer.getGrid(),
						new String[] {}, SWT.READ_ONLY);
                ((ComboBoxCellEditor)editor).setActivationStyle(
                        ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
                        ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
                        ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
                        ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);                
				editor1 = new TableComboViewerCellEditor(viewer.getGrid());
				editorTableComboViewer = ((TableComboViewerCellEditor) editor1).getViewer();
				editorTableCombo = editorTableComboViewer.getTableCombo();				

				try {
					switch (type) {
					case COLUMN_SOURCE_TYPE:
					    columnType = (CCombo) editor.getControl();              
						UIUtilities.populateComboBox(columnType,
								CodeCategories.COLSRCTYPE, 0, false);
						setCurrentCellEditor(editor);
						break;
					case EFFECTIVE_DATE_TYPE:
					    effectiveDateType = (CCombo) editor.getControl();
						UIUtilities.populateComboBox(effectiveDateType,
								CodeCategories.RELPERIOD, 0, false);
						break;
					case LOOKUP_LR:
						populateLookupLRCombo(editorTableComboViewer);
						editorTableComboViewer.setSorter(new LogicalRecordTableSorter(1,SWT.UP));
		                addLRComboOpenEditorMenu(editorTableCombo);						
                        setCurrentCellEditor(editor1);
                        editorTableCombo.addFocusListener(new FocusListener() {

                            public void focusGained(FocusEvent e) {
                            }

                            public void focusLost(FocusEvent e) {
                                // set the lookup path if not already set
                                Integer lookupId = viewColumnSource.getLookupPathId();
                                if (lookupId == null || lookupId == 0) {
                                    try {
                                        LogicalRecordQueryBean logicalRecordBean = viewColumnSource.getLogicalRecordQueryBean();
                                        if (logicalRecordBean != null) {
                                            List<LookupQueryBean> lookupPathList = viewColumnSource.getViewSource()
                                                    .getLookupPaths(logicalRecordBean.getId());
                                            if (lookupPathList != null && lookupPathList.size() > 0) {
                                                LookupQueryBean lookupPathBean = lookupPathList.get(0);
                                                setLookupPath(lookupPathBean);
                                            }
                                        }
                                    } catch (DAOException e1) {
                                        UIUtilities.handleWEExceptions(e1,
                                           "Unexpected database error occurred while retrieving Column Source Value.",
                                           UIUtilities.titleStringDbException);
                                    } catch (SAFRException e2) {
                                        UIUtilities.handleWEExceptions(e2,
                                            "Unexpected error occurred while retrieving Column Source Value.",
                                            null);
                                    }
                                }    
                            }
                            
                        });
						return editor1;

					case LOOKUP_PATH:
                        LogicalRecordQueryBean logicalRecordBean = viewColumnSource.getLogicalRecordQueryBean();
						if (logicalRecordBean != null) {
							populateLookupPathCombo(editorTableComboViewer,logicalRecordBean.getId());
							editorTableComboViewer.setSorter(new LookupTableSorter(1, SWT.UP));
	                        addLUComboOpenEditorMenu(editorTableCombo);                       
						}
                        setCurrentCellEditor(editor1);
						return editor1;
					case LOOKUP_FIELD:
					    LookupQueryBean lookupPathBean = viewColumnSource.getLookupQueryBean();
						if (lookupPathBean != null) {
	                        logicalRecordBean = viewColumnSource.getLogicalRecordQueryBean();
							populateLookupFieldCombo(
							    editorTableComboViewer,lookupPathBean.getId(), logicalRecordBean.getId());
							editorTableComboViewer.setSorter(new AssociatedLRFieldSorter(1,SWT.UP));
	                        addLUFComboOpenEditorMenu(editorTableCombo);                       
						}
                        setCurrentCellEditor(editor1);
						return editor1;
					default:
						editor = null;
						break;
					}
				} catch (DAOException e) {
					UIUtilities.handleWEExceptions(e,
					    "Unexpected database error occurred while retrieving Column Source Value.",null);
				} catch (SAFRException e) {
					UIUtilities.handleWEExceptions(e,
					    "Unexpected error occurred while retrieving Column Source Value.",UIUtilities.titleStringDbException);
				}
			} else if (type.getRowEditorType() == RowEditorType.TEXT
					|| type.getRowEditorType() == RowEditorType.NUMBER) {
				editor = new SAFRTextCellEditor(viewer.getGrid());
			} else if (type.getRowEditorType() == RowEditorType.CHECKBOX) {
				editor = new CheckboxCellEditor(viewer.getGrid(), SWT.CHECK);
			} else {
				editor = null;
			}
			return editor;
		}

		@Override
		protected Object getValue(Object element) {
			RowType rowType = (RowType) element;

	        Code sourceType = viewColumnSource.getSourceType();

	        switch (rowType) {
	        case COLUMN_SOURCE_TYPE:
                return ((CCombo)editor.getControl()).indexOf(((Code) sourceType).getDescription());
	        case COLUMN_SOURCE_VALUE:
                return getColumnSourceValue();
	        case LOOKUP_LR:
                if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    LogicalRecordQueryBean logicalRecordBean = viewColumnSource.getLogicalRecordQueryBean();
                    if (logicalRecordBean != null) {
                        return UIUtilities.getComboString(logicalRecordBean
                                .getName(), logicalRecordBean.getId());
                    }
                }
	            break;
	        case LOOKUP_PATH:
                if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    LookupQueryBean lookupPathBean = viewColumnSource.getLookupQueryBean();
                    if (lookupPathBean != null) {
                        return UIUtilities.getComboString(lookupPathBean
                                .getName(), lookupPathBean.getId());
                    }
                }
	            break;
	        case LOOKUP_FIELD:
                if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    LRField lookupField = viewColumnSource.getLRField();
                    if (lookupField != null) {
                        return UIUtilities.getComboString(lookupField
                                .getName(), lookupField.getId());
                    }
                }
	            break;
	        case EFFECTIVE_DATE_TYPE:
                if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    return ((CCombo) editor.getControl()).indexOf(
                        ((Code) viewColumnSource.getEffectiveDateTypeCode()).getDescription());
                }
	            break;
	        case EFFECTIVE_DATE_VALUE:
                if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    Code effDateType = viewColumnSource.getEffectiveDateTypeCode();
                    if (effDateType != null) {
                        if (effDateType.getGeneralId() == Codes.RELPERIOD_CONSTANT) {
                            String value =  viewColumnSource.getEffectiveDateValue();
                            if (value == null) {
                                return "";
                            } else {
                                return value;
                            }
                        } else if (effDateType.getGeneralId() == Codes.RELPERIOD_RUNDATE) {
                            viewColumnSource.setEffectiveDateValue("Runday()");
                            return viewColumnSource.getEffectiveDateValue();
                        } else if (effDateType.getGeneralId() == Codes.RELPERIOD_SOURCE_FILE_FIELD) {
                            LRField effectiveDateLRField = viewColumnSource.getEffectiveDateLRField();
                            if (effectiveDateLRField != null) {
                                return UIUtilities.getComboString(
                                        effectiveDateLRField.getName(),
                                        effectiveDateLRField.getId());
                            }
                        }
                    }
                }
	            break;
	        case DATATYPE:
                if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                    LRField sourceLRField = viewColumnSource.getLRField();
                    if (sourceLRField != null) {
                        return sourceLRField.getDataTypeCode();
                    }
                } else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    LRField lookupField = viewColumnSource.getLRField();
                    if (lookupField != null) {
                        return lookupField.getDataTypeCode();
                    }
                }
	            break;
	        case DATE_TIME_FORMAT:
                if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                    LRField sourceLRField = viewColumnSource.getLRField();
                    if (sourceLRField != null) {
                        return sourceLRField.getDateTimeFormatCode();
                    }
                } else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    LRField lookupField = viewColumnSource.getLRField();
                    if (lookupField != null) {
                        return lookupField.getDateTimeFormatCode();
                    }
                }
	            break;
	        case LENGTH:
                if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                    LRField sourceLRField = viewColumnSource.getLRField();
                    if (sourceLRField != null) {
                        return sourceLRField.getLength();
                    }
                } else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    LRField lookupField = viewColumnSource.getLRField();
                    if (lookupField != null) {
                        return lookupField.getLength();
                    }
                }
	            break;
	        case DATA_ALIGNMENT:
                if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                    LRField sourceLRField = viewColumnSource.getLRField();
                    if (sourceLRField != null) {
                        return sourceLRField.getHeaderAlignmentCode();
                    }
                } else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    LRField lookupField = viewColumnSource.getLRField();
                    if (lookupField != null) {
                        return lookupField.getHeaderAlignmentCode();
                    }
                }
	            break;
	        case DECIMAL_PLACES:
                if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                    LRField sourceLRField = viewColumnSource.getLRField();
                    if (sourceLRField != null) {
                        return sourceLRField.getDecimals();
                    }
                } else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    LRField lookupField = viewColumnSource.getLRField();
                    if (lookupField != null) {
                        return lookupField.getDecimals();
                    }
                }
	            break;
	        case SCALING_FACTOR:
                if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                    LRField sourceLRField = viewColumnSource.getLRField();
                    if (sourceLRField != null) {
                        return sourceLRField.getScaling();
                    }
                } else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    LRField lookupField = viewColumnSource.getLRField();
                    if (lookupField != null) {
                        return lookupField.getScaling();
                    }
                }
	            break;
	        case SIGNED:
                if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                    LRField sourceLRField = viewColumnSource.getLRField();
                    if (sourceLRField != null) {
                        return sourceLRField.isSigned();
                    }
                } else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    LRField lookupField = viewColumnSource.getLRField();
                    if (lookupField != null) {
                        return lookupField.isSigned();
                    }
                } else {
                    return false;
                }
	        case NUMERIC_MASK:
                if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                    LRField sourceLRField = viewColumnSource.getLRField();
                    if (sourceLRField != null) {
                        return sourceLRField.getNumericMaskCode();
                    }
                } else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    LRField lookupField = viewColumnSource.getLRField();
                    if (lookupField != null) {
                        return lookupField.getNumericMaskCode();
                    }
                }
	            break;
	        case LR_START_POSITION:
                if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                    LRField sourceLRField = viewColumnSource.getLRField();
                    if (sourceLRField != null) {
                        return sourceLRField.getPosition();
                    }
                } else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                    LRField lookupField = viewColumnSource.getLRField();
                    if (lookupField != null) {
                        return lookupField.getPosition();
                    }
                }
	            break;
	        default:
	            return null;
	        }
	        return null;			
		}

		@Override
		protected void setValue(Object element, Object value) {
		    RowType rowType = (RowType) element;

			Boolean dirtyFlag = false;

			if (value instanceof Integer && (Integer) value < 0) {
				value = null;
			}
			try {
				switch (rowType) {
				case COLUMN_SOURCE_TYPE:
				    dirtyFlag = setColumnSourceType(value);			                    
					break;
				case COLUMN_SOURCE_VALUE:
                    dirtyFlag = setColumnSourceValue(value);
					break;
				case LOOKUP_LR:
					dirtyFlag = setLookupLR();
					break;
				case LOOKUP_PATH:
					dirtyFlag = setLookupPath();
					break;
				case LOOKUP_FIELD:
					dirtyFlag = setLookupField();
					break;
				case EFFECTIVE_DATE_TYPE:
					dirtyFlag = setEffectiveDateType(value);
					break;
				case EFFECTIVE_DATE_VALUE:
                    dirtyFlag = setEffectiveDateValue(value);
					break;
				default:
					break;
				}
			} catch (SAFRException e) {
				UIUtilities.handleWEExceptions(e,"Error in setting values", null);
			}

			getViewer().update(element, null);

			if (dirtyFlag) {
				vEditor.setModified(true);
			}
		}

        protected Boolean setEffectiveDateValue(Object value) {
            Boolean dirtyFlag = false;
            Code effectiveDateType = viewColumnSource.getEffectiveDateTypeCode();
            if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_CONSTANT) {
                String previousDateValue = viewColumnSource.getEffectiveDateValue();
                String currentDateValue = (String) value;
                if (!UIUtilities.isEqual(previousDateValue,currentDateValue)) {
                    viewColumnSource.setEffectiveDateValue((String) value);
                    dirtyFlag = true;
                }
            } else if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_SOURCE_FILE_FIELD &&
                value instanceof LRField) {
                LRField previousEffectiveDateField = viewColumnSource.getEffectiveDateLRField();
                LRField currentEffectiveDateField = (LRField) value;
                if (!UIUtilities.isEqualSAFRComponent(previousEffectiveDateField,currentEffectiveDateField)) {
                    viewColumnSource.setEffectiveDateLRField(currentEffectiveDateField);
                    dirtyFlag = true;
                }
            }
            return dirtyFlag;
        }

        protected Boolean setEffectiveDateType(Object value) {
            Boolean dirtyFlag = false;
            Code previousDateType = viewColumnSource.getEffectiveDateTypeCode();
            Code currentDateType = (Code) effectiveDateType.getData(value.toString());

            if (!UIUtilities.isEqual(previousDateType, currentDateType)) {
                viewColumnSource.setEffectiveDateTypeCode(currentDateType);
                
            	// reset effective date value if user changes the date type
            	viewColumnSource.setEffectiveDateValue(null);
            	if (viewColumnSource.getEffectiveDateTypeCode() != null) {
            		if (viewColumnSource.getEffectiveDateTypeCode().getGeneralId() == Codes.RELPERIOD_RUNDATE) {
            			viewColumnSource.setEffectiveDateValue("Runday()");
            		}
            	}
            	getViewer().refresh();
            	dirtyFlag = true;
            }
            return dirtyFlag;
        }

        protected Boolean setLookupField() {
            Boolean dirtyFlag = false;
            LRField previousLookupField = viewColumnSource.getLRField();
            if (viewColumnSource.getLogicalRecordQueryBean() != null) {
                LRField currentLookupField = null;
                
                if (editorTableCombo.getTable().getSelectionCount() > 0) {
                    currentLookupField = (LRField) editorTableCombo
                    .getTable().getSelection()[0].getData();
                }
            	
            	if (!UIUtilities.isEqualSAFRComponent(previousLookupField, currentLookupField)) {
                    if (overwrite.getSelection()) {
                        viewColumnSource.setLRFieldColumn((LRField) currentLookupField);
                    } else {
                        viewColumnSource.setLRField((LRField) currentLookupField);                        
                    }
            		getViewer().refresh();
            		vEditor.updateColumnSourceAffectedRows(viewColumnSource.getViewSource());
            		vEditor.updateColumnSourceAffectedRows();    
            		dirtyFlag = true;
            	}
            }
            return dirtyFlag;
        }

        protected Boolean setLookupPath() {
            Boolean dirtyFlag = false;
            LookupQueryBean previousLookup = viewColumnSource.getLookupQueryBean();
            EnvironmentalQueryBean lookupbean = null;
            if (editorTableCombo.getTable().getSelectionCount() > 0) {
            	lookupbean = (EnvironmentalQueryBean) editorTableCombo
            			.getTable().getSelection()[0].getData();
            }
            if (!UIUtilities.isEqualSAFRQueryBean(previousLookup, lookupbean)) {
            	viewColumnSource.setLRFieldColumn(null);
                viewColumnSource.setLookupQueryBean((LookupQueryBean) lookupbean);
            	getViewer().refresh();
            	vEditor.updateColumnSourceAffectedRows(viewColumnSource.getViewSource());
            	dirtyFlag = true;
            }
            return dirtyFlag;
        }

        protected Boolean setLookupPath(EnvironmentalQueryBean lookupbean) {
            Boolean dirtyFlag = false;
            LookupQueryBean previousLookup = viewColumnSource.getLookupQueryBean();
            if (!UIUtilities.isEqualSAFRQueryBean(previousLookup, lookupbean)) {
                viewColumnSource.setLRFieldColumn(null);
                viewColumnSource.setLookupQueryBean((LookupQueryBean) lookupbean);
                getViewer().refresh();
                vEditor.updateColumnSourceAffectedRows(viewColumnSource.getViewSource());
                dirtyFlag = true;
            }
            return dirtyFlag;
        }
        
        protected Boolean setLookupLR() {
            Boolean dirtyFlag = false;
            LogicalRecordQueryBean previousLR = viewColumnSource.getLogicalRecordQueryBean();
            LogicalRecordQueryBean bean = null;
            if (editorTableCombo.getTable().getSelectionCount() > 0) {
            	bean = (LogicalRecordQueryBean) editorTableCombo.getTable().getSelection()[0].getData();
            }

            if (!UIUtilities.isEqualSAFRQueryBean(previousLR, bean)) {
            	viewColumnSource.setLookupQueryBean(null);
            	viewColumnSource.setLRFieldColumn(null);
                viewColumnSource.setLogicalRecordQueryBean((LogicalRecordQueryBean) bean);
            	getViewer().refresh();
            	vEditor.updateColumnSourceAffectedRows(viewColumnSource.getViewSource());
            	dirtyFlag = true;
            }
            return dirtyFlag;
        }

        protected Boolean setColumnSourceValue(Object value) {
            Boolean dirtyFlag = false;
            Code sourceType = viewColumnSource.getSourceType();
            if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
        		// If column source is of type Source File Field
        		LRField previousSourceValueField = viewColumnSource.getLRField();
                LRField currentSourceValueField = null;
                
                if (editorTableCombo.getTable().getSelectionCount() > 0) {
                    currentSourceValueField = (LRField) editorTableCombo.getTable().getSelection()[0].getData();
                }
        		
        		if (!UIUtilities.isEqualSAFRComponent(previousSourceValueField,currentSourceValueField)) {
        		    if (overwrite.getSelection()) {
        		        viewColumnSource.setLRFieldColumn((LRField) value);
        		    } else {
                        viewColumnSource.setLRField((LRField) value);        		        
        		    }
        			vEditor.updateColumnSourceAffectedRows(viewColumnSource.getViewSource());
        			vEditor.updateColumnSourceAffectedRows();
        			dirtyFlag = true;
        		}
            } else if (sourceType.getGeneralId() == Codes.CONSTANT) {
                if (sourceType.getGeneralId() == Codes.CONSTANT) {
                    String previousConstantValue = viewColumnSource.getSourceValue();
                    String currentConstantValue = (String) value;

                    if (!UIUtilities.isEqual(previousConstantValue, currentConstantValue)) {
                        viewColumnSource.setSourceValue((String) value);
                        dirtyFlag = true;
                        getViewer().refresh();
                        vEditor.updateColumnSourceAffectedRows(viewColumnSource.getViewSource());
                    }
                }
            } else if (sourceType.getGeneralId() == Codes.FORMULA) {
                viewColumnSource.setExtractColumnAssignment((String) value);		                
            }
            return dirtyFlag;
        }

        protected Boolean setColumnSourceType(Object value) {
            Boolean dirtyFlag = false;
            Code previousSrcType = viewColumnSource.getSourceType();
            Code currentSrcType = (Code) columnType.getData(value.toString());

            if (!UIUtilities.isEqual(previousSrcType,currentSrcType)) {
                if (viewColumnSource.getSourceType() != null) {
                    if (viewColumnSource.getSourceType().getGeneralId() == Codes.FORMULA) {
                        vEditor.closeRelatedLogicTextEditors(viewColumnSource);
                    }
                }
                if (value != null) {
                    viewColumnSource.setSourceType((Code) currentSrcType);
                }
                getViewer().refresh();
                vEditor.updateColumnSourceAffectedRows(viewColumnSource.getViewSource());
                vEditor.updateColumnSourceAffectedRows();
                dirtyFlag = true;
            }
            return dirtyFlag;
        }
	}

	/**
	 * Used to populate the Column Source Value and Effective Date Value combos
	 * if they are of type Source File Field
	 * 
	 * @param combo
	 *            the Combo to be populated
	 * @param comboType
	 *            constant indicating whether the Combo to be populated is
	 *            Column Source Value or Effective Date Value
	 * @param allowBlank
	 *            whether the user is allowed to select a blank value
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public void populateValueCombo(TableComboViewer comboViewer, RowType comboType,
			boolean allowBlank) throws DAOException, SAFRException {
		TableCombo combo = comboViewer.getTableCombo();
		ComponentAssociation association = viewColumnSource.getViewSource()
				.getLrFileAssociation();
		if (association != null) {
			Integer counter = 0;
			boolean addToCombo = false;
			combo.getTable().removeAll();
			Integer logicalRecordId = association.getAssociatingComponentId();

			List<LRField> lrFieldList = SAFRApplication.getSAFRFactory()
					.getLRFields(logicalRecordId);
			List<Object> fieldList = new ArrayList<Object>();

			if (allowBlank) {
				// Allow user to select a blank value
				// LogicalRecordFieldQueryBean bean = new
				// LogicalRecordFieldQueryBean(null,0,"",null,null,null,null);
				Object bean = new Object();
				fieldList.add(bean);
				combo.setData(Integer.toString(counter++), null);
			}

			for (LRField field : lrFieldList) {
				// Effective Date Value combo contains only those fields whose
				// Date Time Format code is not null
				if (comboType == RowType.EFFECTIVE_DATE_VALUE) {
					addToCombo = false;
					if (field.getDateTimeFormatCode() != null) {
						addToCombo = true;
					}
				} else if (comboType == RowType.COLUMN_SOURCE_VALUE) {
					addToCombo = true;
				}

				if (addToCombo) {

					fieldList.add(field);
					combo.setData(Integer.toString(counter), field);

					counter++;
				}
			}
			comboViewer.setContentProvider(new ArrayContentProvider());
			comboViewer.setLabelProvider(new LRFieldLableProvider() {
				public String getColumnText(Object element, int columnIndex) {

					switch (columnIndex) {
					case 2:
						if (element instanceof LRField)
							return UIUtilities.getComboString(
									((LRField) element).getName(),
									((LRField) element).getId());
						break;
					default:
						return super.getColumnText(element, columnIndex);
					}
					return UIUtilities.BLANK_VALUE;
				}
			});

			int iCounter;
			for (iCounter = 0; iCounter < 2; iCounter++) {
				ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(
						iCounter, comboViewer, "AssociatedLRField");
				combo.getTable().getColumn(iCounter).addSelectionListener(
						colListener);

			}
			comboViewer.setInput(fieldList);
			comboViewer.refresh();
		}
	}

	/**
	 * Used to populate the Lookup LR Combo
	 * 
	 * @param combo
	 *            the Combo to be populated
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public void populateLookupLRCombo(TableComboViewer comboViewer)
			throws DAOException, SAFRException {
		TableCombo combo = comboViewer.getTableCombo();
		combo.getTable().removeAll();

		List<LogicalRecordQueryBean> logicalRecordList = viewColumnSource
				.getViewSource().getLookupLogicalRecords();
		comboViewer.setContentProvider(new ArrayContentProvider());

		MainTableLabelProvider labelProvider = new MainTableLabelProvider(
				ComponentType.LogicalRecord) {

			public String getColumnText(Object element, int columnIndex) {

				switch (columnIndex) {
				case 2:
					LogicalRecordQueryBean bean = (LogicalRecordQueryBean) element;
					return UIUtilities.getComboString(bean.getName(), bean
							.getId());
				default:
					return super.getColumnText(element, columnIndex);
				}
			}
		};
		labelProvider.setInput();
		comboViewer.setLabelProvider(labelProvider);

		int iCounter;
		for (iCounter = 0; iCounter < 2; iCounter++) {
			ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(
					iCounter, comboViewer, ComponentType.LogicalRecord);
			combo.getTable().getColumn(iCounter).addSelectionListener(
					colListener);

		}

		comboViewer.setInput(logicalRecordList);
		comboViewer.refresh();

	}

	/**
	 * Used to populate the Lookup Path Combo
	 * 
	 * @param combo
	 *            the Combo to be populated
	 * @param logicalRecordId
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public void populateLookupPathCombo(TableComboViewer comboViewer,
			Integer logicalRecordId) throws DAOException, SAFRException {
		TableCombo combo = comboViewer.getTableCombo();
		Integer counter = 0;
		combo.getTable().removeAll();

		List<LookupQueryBean> lookupPathList = viewColumnSource.getViewSource()
				.getLookupPaths(logicalRecordId);

		if (lookupPathList != null) {
			for (LookupQueryBean lookupQueryBean : lookupPathList) {
				combo.setData(Integer.toString(counter), lookupQueryBean);

				counter++;
			}
		}
		comboViewer.setContentProvider(new ArrayContentProvider());

		MainTableLabelProvider labelProvider = new MainTableLabelProvider(
				ComponentType.LookupPath) {
			public String getColumnText(Object element, int columnIndex) {

				switch (columnIndex) {
				case 2:
					LookupQueryBean bean = (LookupQueryBean) element;
					return UIUtilities.getComboString(bean.getName(), bean
							.getId());
				default:
					return super.getColumnText(element, columnIndex);
				}
			}
		};
		labelProvider.setInput();
		comboViewer.setLabelProvider(labelProvider);

		int iCounter;
		for (iCounter = 0; iCounter < 2; iCounter++) {
			ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(
					iCounter, comboViewer, ComponentType.LookupPath);
			combo.getTable().getColumn(iCounter).addSelectionListener(
					colListener);
		}

		comboViewer.setInput(lookupPathList);

	}

	/**
	 * Used to populate the Lookup Field Combo
	 * 
	 * @param combo
	 *            the Combo to be populated
	 * @param lookupPathId
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public void populateLookupFieldCombo(TableComboViewer comboViewer,
			Integer lookupPathId, Integer logicalRecordId)
			throws SAFRException, DAOException {
		TableCombo combo = comboViewer.getTableCombo();
		Integer counter = 0;
		combo.getTable().removeAll();

		List<LRField> lookupFieldList = viewColumnSource.getViewSource()
				.getLookupFields(lookupPathId);

		if (lookupFieldList != null) {
			for (LRField field : lookupFieldList) {
				combo.setData(Integer.toString(counter), field);
				counter++;
			}
		}

		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LRFieldLableProvider() {
			public String getColumnText(Object element, int columnIndex) {

				switch (columnIndex) {
				case 2:
					LRField lField = (LRField) element;
					return UIUtilities.getComboString(lField.getName(), lField
							.getId());
				default:
					return super.getColumnText(element, columnIndex);
				}
			}
		});

		int iCounter;
		for (iCounter = 0; iCounter < 2; iCounter++) {
			ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(
					iCounter, comboViewer, "AssociatedLRField");
			combo.getTable().getColumn(iCounter).addSelectionListener(
					colListener);
		}
		comboViewer.setInput(lookupFieldList);

	}

	public void showGridForCurrentEditor(IEditorPart editor) {
		if (editor instanceof ViewEditor) {
			vEditor = (ViewEditor) editor;
			if (vEditor.isCurrentViewColumnSource()) {
				this.viewColumnSource = vEditor.getCurrentViewColumnSource();
				showGrid(true);
			} else {
				showGrid(false);
			}
		} else {
			showGrid(false);
		}
	}

	private void showGrid(boolean show) {
		if (overwrite.isDisposed()) {
			return;
		}
		columnSourceTableViewer.getGrid().setVisible(show);
		if (show) {
			columnSourceTableViewer.refresh();
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(part instanceof ViewEditor)) {
			return;
		}
		showGridForCurrentEditor((IEditorPart) part);
	}

	public class LRFieldLableProvider implements ITableLabelProvider,
			ITableColorProvider {
	    
		LRFieldLableProvider() {
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof LRField))
				return null;

			LRField lrField = (LRField) element;
			switch (columnIndex) {
			case 0:
				if (lrField.getId() == 0) {
					return "";
				}
				return Integer.toString(lrField.getId());
			case 1:
				return lrField.getName();
			}
			return null;
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

		public Color getBackground(Object element, int columnIndex) {
			return null;
		}

		public Color getForeground(Object element, int columnIndex) {
			return null;
		}

	}

	public void setCurrentCellEditor(CellEditor currentCellEditor) {
		this.currentCellEditor = currentCellEditor;
	}

    protected String getColumnSourceValue() {
        Code sourceType = viewColumnSource.getSourceType();
        if (sourceType.getGeneralId() == Codes.CONSTANT) {
            if (viewColumnSource.getSourceValue() == null) {
                return "";
            } else {
                return viewColumnSource.getSourceValue();
            }
        } else if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
            LRField sourceLRField = viewColumnSource.getLRField();
            if (sourceLRField != null) {
                return UIUtilities.getComboString(sourceLRField
                        .getName(), sourceLRField.getId());
            }
        } else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
            LogicalRecordQueryBean logicalRecordBean = viewColumnSource.getLogicalRecordQueryBean();
            LookupQueryBean lookupPathBean = viewColumnSource.getLookupQueryBean();
            LRField lookupField = viewColumnSource.getLRField();
            if (logicalRecordBean != null && lookupPathBean != null
                    && lookupField != null) {
                String comboString = UIUtilities.getComboString(
                    logicalRecordBean.getName(),logicalRecordBean.getId())+ "."+ 
                    UIUtilities.getComboString(lookupPathBean.getName(), lookupPathBean.getId())+ "."+ 
                    UIUtilities.getComboString(lookupField.getName(), lookupField.getId());
                return comboString;
            }
        } else if (sourceType.getGeneralId() == Codes.FORMULA) {
            if (viewColumnSource.getExtractColumnAssignment() != null &&
                viewColumnSource.getExtractColumnAssignment().length() > UIUtilities.TABLECELLLIMIT)
            {
                return (viewColumnSource.getExtractColumnAssignment().substring(0, UIUtilities.TABLECELLLIMIT-3) + "...");
            }
            else
            {
                return viewColumnSource.getExtractColumnAssignment();
            }
        }
        return "";
    }
	
}
