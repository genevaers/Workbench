package com.ibm.safr.we.ui.editors.lr;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.jface.gridviewer.GridViewerEditor;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LRFieldKeyType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.ui.Application;
import com.ibm.safr.we.ui.commands.SourceProvider;
import com.ibm.safr.we.ui.dialogs.DependencyMessageDialog;
import com.ibm.safr.we.ui.editors.lr.LRFieldsExpandState.ExpandState;
import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.SAFRComboBoxCellEditor;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.utilities.SAFRLogger;

public class LogicalRecordFieldEditor {

    public enum ColumnType {
        FIELDID("Field ID", 60),
        EXPAND("", 25),
        LEVEL("Level", 80),
        FIELDNAME("Field Name", 200),
        DATATYPE("Data Type", 130),
        FIXEDPOSITION("Position", 80),
        LENGTH("Length", 60),
        REDEFINES("Redefines", 220),
        DATETIMEFORMAT("Date/Time Format", 100),
        SIGNED("Signed", 55),
        DECIMALPLACES("Decimal Places", 100),
        SCALING("Scaling", 40),
        NUMERICMASK("Numeric Mask", 100),        
        PRIMARYKEY("Primary Key",80),
        EFFECTIVEDATE("Effective Date",100),
        ALIGNHEADING("Align Heading",80),
        HEADING1("Heading 1",100),
        HEADING2("Heading 2",100),
        HEADING3("Heading 3",100),
        SORTKEYLABEL("Sort Key Label",100),        
        SORTKEYFOOTERLABEL("Sort Key Footer Label",100),        
        COMMENTS("Comments",100);
        
        private String name;
        private int width;
        
        ColumnType(String name, int width) {
            this.name = name;
            this.width = width;
        }
        
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }        
        
        @Override
        public String toString() {
            return name;
        }
        
        public static ColumnType getColumnTypeFromPosition(int pos) {
            return ColumnType.values()[pos];
        }
    }
    
    private class LRFieldContentProvider implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            List<LRField> flds = ((LogicalRecord) inputElement).getLRFields().getActiveItems(); 
            expandState.updateState(flds);            
            return flds.toArray();
        }


        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    private class LRFieldLabelProvider extends CellLabelProvider {

        public LRFieldLabelProvider(Grid table) {
        }
        
        @Override
        public void update(ViewerCell cell) {
            LRField lrField = (LRField) cell.getElement();

            int idx = 0;
            for (int i = 0; i < tableViewerLRFields.getGrid().getItemCount(); i++) {
                if (tableViewerLRFields.getGrid().getItem(i).getData() == null) {
                    continue;
                }
                else if (tableViewerLRFields.getGrid().getItem(i).getData().equals(lrField)) {
                    idx = i;
                    break;
                }
            }
            if (idx % 2 == 0) {
                cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            } else {
                cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
            }

            if (lrField.getKeyType() == LRFieldKeyType.PRIMARYKEY) {
                cell.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
            }            
            
            switch (ColumnType.getColumnTypeFromPosition(cell.getColumnIndex())) {
            case FIELDID:
                cell.setText(Integer.toString(lrField.getId()));
                break;
            case EXPAND:
                ExpandState state = expandState.getFieldState(lrField.getId());
                switch (state) {
                case COLLAPSED:
                    cell.setImage(collapseImage);
                    break;
                case EXPANDED:
                    cell.setImage(expandImage);
                    break;
                case NONE:
                default:
                    cell.setImage(null);                    
                    break;                
                }
                break;
            case LEVEL:
                if (lrField.getRedefine() != null && lrField.getRedefine() == -1) {
                    cell.setText("");
                }
                else {
                    // calculate indent
                    String indent = "";
                    for (int i=1 ; i<lrField.getLevel() ; i++) {
                        indent += "  ";
                    }
                    cell.setText(indent + lrField.getLevel().toString());
                }
                break;
            case FIELDNAME:
                if (lrField.getName() == null) {
                    cell.setText("");
                }
                else {
                    cell.setText(lrField.getName());
                }
                break;
            case DATATYPE:
                if (lrField.getDataTypeCode() != null) {
                    cell.setText(lrField.getDataTypeCode().getDescription());
                } else {
                    cell.setText("");
                }
                break;
            case REDEFINES:
                LRField fld = logicalRecord.findLRField(lrField.getRedefine());
                if (fld == null) {
                    cell.setText("");
                }
                else {
                    cell.setText(fld.getDescriptor());
                }                
                break;
            case FIXEDPOSITION:
                if (lrField.getPosition() == null) {
                    cell.setText("");
                }
                else {
                    cell.setText(Integer.toString(lrField.getPosition()));                    
                }
                break;
            case LENGTH:
                cell.setText(Integer.toString(lrField.getLength()));
                break;
            case DECIMALPLACES:
                cell.setText(Integer.toString(lrField.getDecimals()));
                break;
            case PRIMARYKEY:
                if (lrField.getKeyType().equals(LRFieldKeyType.PRIMARYKEY)) {
                    cell.setText(Integer.toString(lrField.getPkeySeqNo()));
                } else {
                    cell.setText("");
                }
                break;
            case EFFECTIVEDATE:
                cell.setText(lrField.getEffectiveDateString());
                break;
            case SCALING:
                cell.setText(Integer.toString(lrField.getScaling()));
                break;
            case DATETIMEFORMAT:
                if (lrField.getDateTimeFormatCode() != null) {
                    cell.setText(lrField.getDateTimeFormatCode().getDescription());
                } else {
                    cell.setText("");
                }
                break;
            case SIGNED:
                if (lrField.isSigned()) {
                    cell.setImage(UIUtilities.getImageDescriptor(ImageKeys.CHECKED));
                } else {
                    cell.setImage(UIUtilities.getImageDescriptor(ImageKeys.UNCHECKED));
                }
                break;
            case ALIGNHEADING:
                if (lrField.getHeaderAlignmentCode() != null) {
                    cell.setText(lrField.getHeaderAlignmentCode().getDescription());
                } else {
                    cell.setText("");
                }
                break;
            case HEADING1:
                cell.setText(lrField.getHeading1());
                break;
            case HEADING2:
                cell.setText(lrField.getHeading2());
                break;
            case HEADING3:
                cell.setText(lrField.getHeading3());
                break;
            case NUMERICMASK:
                if (lrField.getNumericMaskCode() != null) {
                    cell.setText(lrField.getNumericMaskCode().getDescription());
                } else {
                    cell.setText("");
                }
                break;
            case SORTKEYFOOTERLABEL:
                cell.setText(lrField.getSubtotalLabel());
                break;
            case SORTKEYLABEL:
                cell.setText(lrField.getSortKeyLabel());
                break;
            case COMMENTS:
                cell.setText(lrField.getComment());
                break;
            default:
                break;
            }
        }

    }

    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.lr.LogicalRecordFieldEditor");
    
    private Image expandImage = AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID, ImageKeys.MINUS_IMAGE).createImage();
    private Image collapseImage = AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID, ImageKeys.PLUS_IMAGE).createImage();
    private LogicalRecordMediator mediator;
    private CTabFolder tabFolder;
    private LogicalRecord logicalRecord = null;

    
    // internal widgets
    private Composite compositeLRFields;    
    private Label labelTotalKeyLength;
    private Label labelTotalLength;
    private Label labelFieldCount;
    
    // lrfields
    public static GridTableViewer tableViewerLRFields;
    private LRFieldLabelProvider labelProvider;
    private CellEditor currentCellEditor;
    private LRFieldCellEditor dateTimeFormatEditor = null;
    
    private List<LRField> pasteFldList = new ArrayList<LRField>();
    private LRField newField;
    private String searchText;
    
    private Font itFont;
    
    private LRFieldsExpandState expandState = null;
    private LRFieldEditorFilter filter = null;
    
    private Clipboard clipboard;
    
    protected LogicalRecordFieldEditor(LogicalRecordMediator mediator, CTabFolder tabFolder, LogicalRecord logicalRecord) {
        this.mediator = mediator;
        this.tabFolder = tabFolder;
        this.logicalRecord = logicalRecord;
        this.expandState = new LRFieldsExpandState(logicalRecord.getLRFields().getActiveItems());
    }

    public void dispose() {
      	itFont.dispose();
    }

   protected void create() {

        clipboard = new Clipboard(mediator.getSite().getShell().getDisplay());
        compositeLRFields = mediator.getGUIToolKit().createComposite(tabFolder, SWT.NONE);
        FormLayout layoutLRFields = new FormLayout();
        layoutLRFields.marginTop = 10;
        layoutLRFields.marginBottom = 10;
        layoutLRFields.marginLeft = 10;
        layoutLRFields.marginRight = 10;
        compositeLRFields.setLayout(layoutLRFields);

        Label label = new Label(compositeLRFields, SWT.NONE);
        FontDescriptor itDescriptor = FontDescriptor.createFrom(label.getFont()).setStyle(SWT.ITALIC);
        itFont = itDescriptor.createFont(label.getDisplay());        
        
        compositeLRFields.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));

        Composite compositeLRLabels = mediator.getGUIToolKit().createComposite(compositeLRFields, SWT.NONE);
        compositeLRLabels.setLayout(new FormLayout());
        compositeLRLabels.setLayoutData(new FormData());

        Label labelTotalLengthText = mediator.getGUIToolKit().createLabel(compositeLRLabels, SWT.NONE, "Total Length: ");
        FormData dataTotalLengthText = new FormData();
        dataTotalLengthText.left = new FormAttachment(0, 5);
        labelTotalLengthText.setLayoutData(dataTotalLengthText);

        labelTotalLength = mediator.getGUIToolKit().createLabel(compositeLRLabels, SWT.NONE, "");
        FormData dataTotalLength = new FormData();
        dataTotalLength.left = new FormAttachment(labelTotalLengthText, 2);
        dataTotalLength.width = 100;
        labelTotalLength.setLayoutData(dataTotalLength);

        Label labelTotalKeyLengthText = mediator.getGUIToolKit().createLabel(compositeLRLabels, SWT.NONE, "Total Key Length: ");
        labelTotalKeyLengthText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
        FormData dataTotalKeyLengthText = new FormData();
        dataTotalKeyLengthText.left = new FormAttachment(labelTotalLength, 10);
        labelTotalKeyLengthText.setLayoutData(dataTotalKeyLengthText);

        labelTotalKeyLength = mediator.getGUIToolKit().createLabel(compositeLRLabels, SWT.NONE, "");
        labelTotalKeyLength.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
        FormData dataTotalKeyLength = new FormData();
        dataTotalKeyLength.left = new FormAttachment(labelTotalKeyLengthText, 2);
        dataTotalKeyLength.width = 100;
        labelTotalKeyLength.setLayoutData(dataTotalKeyLength);

        Label labelFieldCountText = mediator.getGUIToolKit().createLabel(compositeLRLabels, SWT.NONE, "Field Count: ");
        FormData dataFieldCountText = new FormData();
        dataFieldCountText.left = new FormAttachment(labelTotalKeyLength, 10);
        labelFieldCountText.setLayoutData(dataFieldCountText);

        labelFieldCount = mediator.getGUIToolKit().createLabel(compositeLRLabels, SWT.NONE, "");
        FormData dataFieldCount = new FormData();
        dataFieldCount.left = new FormAttachment(labelFieldCountText, 2);
        dataFieldCount.width = 100;
        labelFieldCount.setLayoutData(dataFieldCount);

        tableViewerLRFields = new GridTableViewer(compositeLRFields, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER
            | SWT.MULTI);

        mediator.getFormToolKit().adapt(tableViewerLRFields.getGrid(), true, true);
        tableViewerLRFields.getGrid().setHeaderVisible(true);
        tableViewerLRFields.getGrid().setLinesVisible(true);
        tableViewerLRFields.getGrid().setRowHeaderVisible(true);
        tableViewerLRFields.getGrid().setCellSelectionEnabled(true);

        FormData dataTableLRFields = new FormData();
        dataTableLRFields.left = new FormAttachment(0, 0);
        dataTableLRFields.right = new FormAttachment(100, 0);
        dataTableLRFields.top = new FormAttachment(labelTotalKeyLengthText, 30);
        dataTableLRFields.bottom = new FormAttachment(100, 0);
        dataTableLRFields.width = 0;
        dataTableLRFields.height = 0;
        tableViewerLRFields.getGrid().setLayoutData(dataTableLRFields);

        tableViewerLRFields.getGrid().addListener(SWT.MeasureItem, new Listener() {
            boolean heightDone = false;

            public void handleEvent(Event event) {
                if (!heightDone) {
                    GC gc = new GC(Display.getCurrent());
                    FontMetrics fm = gc.getFontMetrics();
                    int height = fm.getHeight();
                    heightDone = true;
                    event.height = height + 5;
                    gc.dispose();
                }
            }
        });

        // filter according to parent expansion
        filter = new LRFieldEditorFilter(expandState);
        tableViewerLRFields.addFilter(filter);
        
        // Code for enabling and controlling editor activation via the keyboard.
        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(tableViewerLRFields) {
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                    || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.character == ' '))
                    || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
                    || (event.sourceEvent instanceof MouseEvent) && ((MouseEvent) event.sourceEvent).button == 1;
            }
        };

        GridViewerEditor.create(tableViewerLRFields, actSupport, ColumnViewerEditor.KEYBOARD_ACTIVATION
            | ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
            | GridViewerEditor.SELECTION_FOLLOWS_EDITOR);

        tableViewerLRFields.setContentProvider(new LRFieldContentProvider());
        labelProvider = new LRFieldLabelProvider(tableViewerLRFields.getGrid());
        createColumns(TreeItemId.LRFIELD, tableViewerLRFields, tableViewerLRFields.getGrid());
        tableViewerLRFields.setLabelProvider(labelProvider);
        // Jaydeep CQ 7994. set width to avoid row header width problem.
        GC gc = new GC(Display.getCurrent());
        tableViewerLRFields.getGrid().setItemHeaderWidth(gc.getAdvanceWidth('W') * 4);
        tableViewerLRFields.setInput(logicalRecord);
        gc.dispose();
        calculateFieldCount();

        // If the user presses the ARROW_RIGHT key if he is at the
        // last column of the last row, or the ARROW_DOWN key, a new
        // row must be inserted (only if the 'Name' field of the
        // current last row is not blank).
        // If he presses the ARROW_RIGHT key, focus must be shifted to the cell
        // at the first editable column of the next row.
        // If he presses the ARROW_LEFT key, focus must be shifted to the cell
        // at the last editable column of the previous row.
        tableViewerLRFields.getGrid().addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {                
            }

            public void keyReleased(KeyEvent e) {
                Point curCell = tableViewerLRFields.getGrid().getFocusCell();
                if (curCell != null) {
                    String result = "";
                    if (((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'c')) {
                        GridItem items[] = tableViewerLRFields.getGrid().getSelection();
                        for (GridItem item : items) {
                            result += item.getText(curCell.x) + ",";
                        }
                        if (!result.isEmpty()) {
                            result = result.substring(0, result.length()-1);
                        }
                        if (result.isEmpty()) {
                            result = " ";
                        }
                        clipboard.setContents(new Object[] {result}, 
                            new Transfer[] { TextTransfer.getInstance() });
                    }
                }                
            }
        });

        // Code for tracking the focus on the table
        IFocusService service = (IFocusService) mediator.getSite().getService(IFocusService.class);
        service.addFocusTracker(tableViewerLRFields.getGrid(), "LRFieldTable");

        // Code for Context menu
        // First we create a menu Manager
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableViewerLRFields.getGrid());
        // Set the MenuManager
        tableViewerLRFields.getGrid().setMenu(menu);
        mediator.getSite().registerContextMenu(menuManager, tableViewerLRFields);
        // Make the selection available
        mediator.getSite().setSelectionProvider(tableViewerLRFields);

        tableViewerLRFields.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                mediator.refreshToolbar();

            }
        });

        tableViewerLRFields.getGrid().addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                mediator.refreshToolbar();
            }

            public void focusLost(FocusEvent e) {

            }
        });

        tableViewerLRFields.getGrid().addMenuDetectListener(new MenuDetectListener() {

            public void menuDetected(MenuDetectEvent e) {
                SourceProvider service = UIUtilities.getSourceProvider();
                if (pasteFldList.isEmpty()) {
                    service.setAllowPasteLRField(false);
                } else {
                    service.setAllowPasteLRField(true);
                }
                if (logicalRecord.getLRFields().getActiveItems().isEmpty() ||
                    tableViewerLRFields.getGrid().getSelectionIndex() != -1) {
                    service.setAllowInsertLRField(true);                    
                }
                else {
                    service.setAllowInsertLRField(false);                                        
                }
            }
        });

        tableViewerLRFields.getGrid().addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (tableViewerLRFields.getGrid().getItemCount() == 0 || tableViewerLRFields.getGrid().getCellSelectionCount() == 0) {
                    // grid has no rows so no-op
                    return;
                }
                int numCols = tableViewerLRFields.getGrid().getColumnCount();
                int numRows = tableViewerLRFields.getGrid().getItemCount();
                Point pt = tableViewerLRFields.getGrid().getCellSelection()[0];
                LRField currField = (LRField) tableViewerLRFields.getGrid().getItem(pt.y).getData();
                int currDataType = currField.getDataTypeCode() != null ? currField.getDataTypeCode().getGeneralId() : 0;

                // moving right
                if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
                    if (pt != null) {
                        if (pt.x == numCols - 1) {
                            // At the end of row.
                            if (pt.y == numRows - 1) {
                                // on the last row so add a new row
                                addRow(true);
                            } else {
                                // Display grid from left hand side.
                                tableViewerLRFields.getGrid().showColumn(tableViewerLRFields.getGrid().getColumn(0));
                                // tab fwd to Field Name of next row
                                tableViewerLRFields.editElement(
                                    tableViewerLRFields.getGrid().getItem(pt.y + 1).getData(), ColumnType.FIELDNAME.ordinal());
                            }
                        // skip expand 
                        } else if (pt.x == ColumnType.FIELDID.ordinal()) {
                            tableViewerLRFields.editElement(currField, ColumnType.FIELDNAME.ordinal());                            
                        // skip redefine 
                        } else if (pt.x == ColumnType.LENGTH.ordinal()) {
                            if (currDataType == Codes.EDITED_NUMERIC) {
                                tableViewerLRFields.editElement(currField, ColumnType.SIGNED.ordinal());
                            }
                            else {
                                tableViewerLRFields.editElement(currField, ColumnType.DATETIMEFORMAT.ordinal());                                
                            }
                        // skip numeric columns for ALPHANUMERIC
                        } else if ((pt.x == ColumnType.DATETIMEFORMAT.ordinal() || 
                                    pt.x == ColumnType.SIGNED.ordinal() || 
                                    pt.x == ColumnType.DECIMALPLACES.ordinal() || 
                                    pt.x == ColumnType.SCALING.ordinal())
                            && currDataType == Codes.ALPHANUMERIC) {
                            // for alphanumeric skip Signed, Decimals, Scaling, 
                            // and Numeric Mask and go to Primary Key
                            tableViewerLRFields.editElement(currField, ColumnType.PRIMARYKEY.ordinal());
                        }
                       
                        else if (pt.x == ColumnType.REDEFINES.ordinal()  && 
                            currDataType == Codes.EDITED_NUMERIC) {
                            // skip DateTimeFormat for edited numeric and go to
                            // Signed
                            tableViewerLRFields.editElement(currField, ColumnType.SIGNED.ordinal());
                        } else if (pt.x == ColumnType.SCALING.ordinal() && 
                                   currDataType != Codes.MASKED_NUMERIC) {
                            // skip Numeric Mask if not masked numeric
                            tableViewerLRFields.editElement(currField, ColumnType.PRIMARYKEY.ordinal());
                        } else if (pt.x < numCols - 1) {
                            // tab fwd to the next cell on same row
                            tableViewerLRFields.editElement(currField, pt.x + 1);
                        }
                    }
                } else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                    if (pt != null) {
                        e.doit = false; // to trigger cell editor on shift-tab
                        if (pt.x <= ColumnType.FIELDNAME.ordinal() && pt.y > 0) {
                            // From Field ID or Field Name tab back to Comments
                            // in row above
                            tableViewerLRFields.editElement(
                                tableViewerLRFields.getGrid().getItem(pt.y - 1).getData(), ColumnType.COMMENTS.ordinal());
                        // skip redefine 
                        } else if (pt.x == ColumnType.DATETIMEFORMAT.ordinal()) {
                            tableViewerLRFields.editElement(currField, ColumnType.LENGTH.ordinal());                            
                        } else if ((pt.x == ColumnType.DECIMALPLACES.ordinal() || 
                            pt.x == ColumnType.SCALING.ordinal() ||
                            pt.x == ColumnType.NUMERICMASK.ordinal() ||
                            pt.x == ColumnType.PRIMARYKEY.ordinal())
                            && currDataType == Codes.ALPHANUMERIC) {
                            // for alphanumeric skip Decimals, Scaling, Signed
                            // and Numeric Mask and tab back to Date/Time Format
                            tableViewerLRFields.editElement(currField, ColumnType.DATETIMEFORMAT.ordinal());
                        } else if (pt.x == ColumnType.DECIMALPLACES.ordinal() && currDataType == Codes.BINARY
                            && currField.getDateTimeFormatCode() != null) {
                            // skip Signed for binary field with a date format
                            // and tab back to Date/Time Format
                            tableViewerLRFields.editElement(currField, ColumnType.DATETIMEFORMAT.ordinal());
                        } else if (pt.x == ColumnType.SIGNED.ordinal() && currDataType == Codes.EDITED_NUMERIC) {
                            // skip Date/Time Format for edited numeric and
                            // tab back to Length
                            tableViewerLRFields.editElement(currField, ColumnType.LENGTH.ordinal());
                        } else if (pt.x == ColumnType.PRIMARYKEY.ordinal() && currDataType != Codes.MASKED_NUMERIC) {
                            // skip Numeric Mask if data type not masked numeric
                            tableViewerLRFields.editElement(currField, ColumnType.SCALING.ordinal());
                        } else if (pt.x > 1) {
                            // tab bkwd to prev cell on same row
                            tableViewerLRFields.editElement(currField, pt.x - 1);
                        }
                    }
                }
            }
        });

        tableViewerLRFields.getGrid().addMouseListener(new MouseListener() {

            public void mouseDoubleClick(MouseEvent e) {
            }

            public void mouseDown(MouseEvent e) {
                
                // UI logger
                Point pt = new Point(e.x, e.y);
                GridItem item = tableViewerLRFields.getGrid().getItem(pt);
                GridColumn column = tableViewerLRFields.getGrid().getColumn(pt);
                if (item == null) {
                    if (column == null) {
                        tableViewerLRFields.getGrid().setData(SAFRLogger.USER, "LR Fields Grid");
                    } else {
                        Integer colIdx = tableViewerLRFields.getGrid().indexOf(column);
                        tableViewerLRFields.getGrid().setData(SAFRLogger.USER, "LR Fields Grid (" + 
                        ColumnType.getColumnTypeFromPosition(colIdx).getName() + ")");
                    }
                } else {
                    Integer rowIdx = tableViewerLRFields.getGrid().indexOf(item) + 1;
                    if (column == null) {
                        tableViewerLRFields.getGrid().setData(SAFRLogger.USER, "LR Fields Grid (" + rowIdx + ")");
                    } else {
                        Integer colIdx = tableViewerLRFields.getGrid().indexOf(column);
                        tableViewerLRFields.getGrid().setData(SAFRLogger.USER, "LR Fields Grid (" + 
                        ColumnType.getColumnTypeFromPosition(colIdx).getName() + ","
                            + rowIdx + ")");
                    }
                }
                
                // expand collapse
                if (tableViewerLRFields.getGrid().getSelectionIndex() != -1) {
                    Point gpt = tableViewerLRFields.getGrid().getCellSelection()[0];
                    if (gpt.x == ColumnType.EXPAND.ordinal() && item != null) {                        
                        LRField fld = (LRField) item.getData();
                        // toggle state if it exists
                        if (expandState.getFieldState(fld.getId()) == ExpandState.EXPANDED) {
                            expandState.setFieldState(fld.getId(), ExpandState.COLLAPSED);
                        }
                        else if (expandState.getFieldState(fld.getId()) == ExpandState.COLLAPSED) {
                            expandState.setFieldState(fld.getId(), ExpandState.EXPANDED);                        
                        }
                        tableViewerLRFields.refresh();                                    
                    }
                }
            }

            public void mouseUp(MouseEvent e) {
                Point from = tableViewerLRFields.getGrid().getFocusCell();
                Point to = tableViewerLRFields.getGrid().getCell(new Point(e.x, e.y));
                if (from == null || to == null) {
                    if (currentCellEditor != null && currentCellEditor instanceof SAFRComboBoxCellEditor) {
                        SAFRComboBoxCellEditor combo = (SAFRComboBoxCellEditor) currentCellEditor;
                        combo.focusLost();
                    }
                    return;
                }

                // Identify columns with a combo box (nebula or jface).
                // These are Data Type, Date/Time Format, Numeric Mask, Primary
                // Key, Effective Date, Align Header
                Set<Integer> comboCols = new HashSet<Integer>(Arrays.asList(
                    ColumnType.DATATYPE.ordinal(),
                    ColumnType.REDEFINES.ordinal(),
                    ColumnType.DATETIMEFORMAT.ordinal(),
                    ColumnType.NUMERICMASK.ordinal(),
                    ColumnType.PRIMARYKEY.ordinal(),
                    ColumnType.EFFECTIVEDATE.ordinal(),
                    ColumnType.ALIGNHEADING.ordinal()));

                // If clicking away from a column with a combo box, invoke
                // the cell editor on the new (clicked) cell
                if (comboCols.contains(from.x) && to.x != from.x) {
                    if (currentCellEditor != null && currentCellEditor instanceof SAFRComboBoxCellEditor) {
                        SAFRComboBoxCellEditor combo = (SAFRComboBoxCellEditor) currentCellEditor;
                        combo.focusLost();
                    }
                    List<LRField> list = logicalRecord.getLRFields().getActiveItems();
                    LRField[] fields = list.toArray(new LRField[list.size()]);
                    tableViewerLRFields.editElement(fields[to.y], to.x);
                }
            }
        });
        
        
        
    }
    

    /**
     * /** This function is used to create columns for table.
     * 
     * @param treeId
     *            of the meatadata component for which the table is being
     *            created.
     * @param tableViewerLRFields2
     *            of the table for which the columns are to be created.
     * @param tableLRFields2
     *            for which the columns are to be created.
     */
    private void createColumns(TreeItemId treeId, GridTableViewer tableViewerLRFields2, Grid tableLRFields2) {
        
        for (ColumnType type : ColumnType.values()) {
            GridViewerColumn column = new GridViewerColumn(
                tableViewerLRFields2, SWT.NONE);
            column.getColumn().setText(type.getName());
            column.getColumn().setHeaderTooltip(type.getName());
            column.getColumn().setWidth(type.getWidth());
            if (type == ColumnType.FIELDID || type == ColumnType.LEVEL || 
                type == ColumnType.REDEFINES) {
                column.getColumn().setHeaderFont(itFont);
            }
            column.getColumn().setResizeable(true);
            if (treeId == TreeItemId.LRFIELD && 
                type != ColumnType.FIELDID && 
                type !=  ColumnType.EXPAND) {
                
                LRFieldCellEditor cedit = new LRFieldCellEditor(
                    tableViewerLRFields2, type, mediator.getFormToolKit(), this);
                if (type == ColumnType.DATETIMEFORMAT) {
                    this.dateTimeFormatEditor = cedit;
                }
                column.setEditingSupport(cedit);
            }
            
        }        
    }

    /**
     * This function is used to add a new row to the LR table below the last
     * row.
     * 
     * @return the newly added LR Field
     */
    protected LRField addRow(boolean edit) {
        try {
            mediator.setTabLRFields();
            newField = logicalRecord.addField();
            expandToShowField(newField);
            tableViewerLRFields.setInput(logicalRecord);;
            // CQ10091: Display grid from left hand side
            // and set edit focus on Field Name
            tableViewerLRFields.getGrid().showColumn(
                    tableViewerLRFields.getGrid().getColumn(0));
            if (edit) {
                tableViewerLRFields.editElement(newField, ColumnType.FIELDNAME.ordinal());
            }
            calculateFieldCount();
            setDirty(true);
            mediator.refreshToolbar();
        } catch (SAFRException se) {
            UIUtilities.handleWEExceptions(se,"Unexpected error occurred while adding a Field.", null);
        }
        return newField;
    }

    protected LRField insertRowBefore(LRField field) {
        try {
            newField = logicalRecord.insertFieldBefore(field);
            expandToShowField(newField);
            tableViewerLRFields.setInput(logicalRecord);
            calculateFieldCount();
            focusOnField(newField);
            setDirty(true);
            mediator.refreshToolbar();

        } catch (SAFRException se) {
            UIUtilities.handleWEExceptions(se,
                "Unexpected error occurred while inserting a Field before.", null);
        }

        return newField;
    }

    protected LRField insertRowAfter(LRField field) {
        try {
            newField = logicalRecord.insertFieldAfter(field);
            expandToShowField(newField);
            tableViewerLRFields.setInput(logicalRecord);
            calculateFieldCount();
            focusOnField(newField);
            setDirty(true);
            mediator.refreshToolbar();

        } catch (SAFRException se) {
            UIUtilities.handleWEExceptions(se,
                "Unexpected error occurred while inserting a Field after.", null);
        }

        return newField;
    }
    
    protected void focusOnField(LRField field) {
        for (GridItem item : tableViewerLRFields.getGrid().getItems()) {
            LRField fld = (LRField)item.getData();
            if (fld.getId() == field.getId()) {
                tableViewerLRFields.getGrid().setFocusItem(item);
                tableViewerLRFields.getGrid().setFocusColumn(
                    tableViewerLRFields.getGrid().getColumn(ColumnType.FIELDNAME.ordinal()));
                int row = tableViewerLRFields.getGrid().getIndexOfItem(item);                
                tableViewerLRFields.getGrid().setCellSelection(
                    new Point(ColumnType.FIELDNAME.ordinal(), row));
            }
        }
    }
    
    /**
     * This function is used to insert a new row in the table above the selected
     * row
     */
    protected void editInsertFieldBefore() {
        int index = tableViewerLRFields.getGrid().getSelectionIndex();
        LRField newField;        
        if (index == -1) {
            newField = insertRowBefore(null);                    
        }
        else {
            GridItem item = tableViewerLRFields.getGrid().getItem(index);
            newField = insertRowBefore((LRField) item.getData());
        }
        tableViewerLRFields.editElement(newField, ColumnType.FIELDNAME.ordinal());        
    }

    /**
     * This function is used to insert a new row in the table above the selected
     * row
     */
    protected void editInsertFieldAfter() {
        int index = tableViewerLRFields.getGrid().getSelectionIndex();
        LRField newField;
        if (index == -1) {
            newField = insertRowAfter(null);                    
        }
        else {
            GridItem item = tableViewerLRFields.getGrid().getItem(index);
            newField = insertRowAfter((LRField) item.getData());            
        }
        tableViewerLRFields.editElement(newField, ColumnType.FIELDNAME.ordinal());
    }
    
    /**
     * This function is used to remove the selected row from table
     */
    protected void removeRow(List<LRField> selectedLrFieldsList) {

        if (selectedLrFieldsList.size() >= 0) {
            if (confirmRemoveRow()) {

                // Display an hour glass till row is removed.
                Display.getCurrent().getActiveShell().setCursor(
                        Display.getCurrent().getActiveShell().getDisplay()
                                .getSystemCursor(SWT.CURSOR_WAIT));

                try {
                    logicalRecord.removeFields(selectedLrFieldsList);
                    calculateFieldCount();
                    tableViewerLRFields.setInput(logicalRecord);
                    setDirty(true);
                } catch (SAFRValidationException e) {
                    DependencyMessageDialog.openDependencyDialog(mediator.getSite().getShell(),"Field dependencies",
                        "These LR Fields cannot be deleted because they are used in Lookup Paths or Views. You must first remove this dependency.",
                        e.getMessageString(), MessageDialog.ERROR,new String[] { IDialogConstants.OK_LABEL },0);
                } catch (DAOException e) {
                    UIUtilities.handleWEExceptions(e);
                }
            }
        }
        mediator.refreshToolbar();

        // return cursor to normal
        Display.getCurrent().getActiveShell().setCursor(null);
    }

    /**
     * This function is used to confirm whether the user actually wants to
     * delete the selected row
     * 
     * @return the user choice - either OK or CANCEL
     */
    private Boolean confirmRemoveRow() {
        int userResponse = 1;
        MessageDialog dialog = new MessageDialog(mediator.getSite().getShell(),
                "Confirm deletion", null,
                "Are you sure you want to delete selected row(s)?",
                MessageDialog.QUESTION, new String[] { "&OK", "&Cancel" }, 0);

        userResponse = dialog.open();
        if (userResponse == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This function is used to remove selected row from table
     */
    protected void editRemoveRow() {
        int[] selectedIndices = tableViewerLRFields.getGrid().getSelectionIndices();
        List<LRField> removeFldList = new ArrayList<LRField>();
        GridItem items[] = tableViewerLRFields.getGrid().getItems();
        for (int indice : selectedIndices) {
            removeFldList.add((LRField) items[indice].getData());
        }
        
        removeRow(removeFldList);
    }


    /**
     * This function is used to copy selected rows from table
     * 
     * @throws SAFRException
     */
    protected void editCopyRow() {
        pasteFldList.clear();
        int[] selectedIndices = tableViewerLRFields.getGrid().getSelectionIndices();
        GridItem items[] = tableViewerLRFields.getGrid().getItems();
        for (int indice : selectedIndices) {
            pasteFldList.add((LRField) items[indice].getData());
        }
    }

    /**
     * This function is used to paste cut or copied row to selected location in
     * the table
     */
    protected void editPasteRowAboveSelected() {
        LRField newField = null;
        int i = 0;
        int index = tableViewerLRFields.getGrid().getSelectionIndex();
        if (pasteFldList.size() > 0) {
            for (i = 0; i < pasteFldList.size(); i++) {
                if (tableViewerLRFields.getGrid().getItemCount() != 0) {
                    GridItem item = tableViewerLRFields.getGrid().getItem(index);
                    newField = insertRowBefore((LRField) item.getData());
                    index++;
                } else {
                    newField = addRow(false);
                }
                try {
                    setPasteValues(newField, i, pasteFldList);
                } catch (SAFRValidationException e) {
                    UIUtilities.handleWEExceptions(e);
                }
            }
            setDirty(true);
            logicalRecord.autocalcRedefine();
            tableViewerLRFields.setInput(logicalRecord);
        }
    }

    protected void editPasteRowBelowSelected() {
        LRField newField = null;
        int i = 0;
        int idx= tableViewerLRFields.getGrid().getSelectionIndex() + 1;
        if (pasteFldList.size() > 0) {
            for (i = 0; i < pasteFldList.size(); i++) {
                if (tableViewerLRFields.getGrid().getItemCount() != 0) {
                    if ((tableViewerLRFields.getGrid().getSelectionIndex() + 1) == tableViewerLRFields.getGrid().getItemCount()) {
                        newField = addRow(false);
                    } else {
                        GridItem item = tableViewerLRFields.getGrid().getItem(idx);
                        newField = insertRowBefore((LRField) item.getData());
                        idx++;
                    }
                } else {
                    newField = addRow(false);
                }

                try {
                    setPasteValues(newField, i, pasteFldList);
                } catch (SAFRValidationException e) {
                    UIUtilities.handleWEExceptions(e);
                }
            }
            setDirty(true);
            logicalRecord.autocalcRedefine();
            tableViewerLRFields.setInput(logicalRecord);
        }
    }

    protected void moveUpRow() {
        int index = tableViewerLRFields.getGrid().getSelectionIndex();
        if (index <= 0) {
            return;
        }
        LRField lrField = (LRField) tableViewerLRFields.getGrid().getItems()[index].getData();
        logicalRecord.moveFieldUp(lrField);
        expandToShowField(lrField);
        tableViewerLRFields.setInput(logicalRecord);
        focusOnField(lrField);        
        tableViewerLRFields.reveal(lrField);
        setDirty(true);
        mediator.refreshToolbar();
    }

    protected void expandToShowField(LRField lrField) {
        expandState.expandToShowField(lrField);
    }

    protected void moveDownRow() {
        int index = tableViewerLRFields.getGrid().getSelectionIndex();
        if (index >= (tableViewerLRFields.getGrid().getItemCount() - 1) || 
            index == -1) {
            return;
        }
        LRField lrField = (LRField) tableViewerLRFields.getGrid().getItems()[index].getData();
        logicalRecord.moveFieldDown(lrField);
        expandToShowField(lrField);
        tableViewerLRFields.setInput(logicalRecord);
        focusOnField(lrField);        
        tableViewerLRFields.reveal(lrField);
        setDirty(true);
        mediator.refreshToolbar();
    }

    protected void calculateFieldCount() {
        labelTotalLength.setText(Integer.toString(logicalRecord.getTotalLength()));
        labelTotalKeyLength.setText(Integer.toString(logicalRecord.getPrimayKeyLength()));
        labelFieldCount.setText(Integer.toString(logicalRecord.getLRFields().getActiveItems().size()));
    }    

    /**
     * This function sets the focus to a particular cell.
     * 
     * @param row
     *            the row number of the cell to which focus is to be set
     * @param column
     *            the column number of the cell to which focus is to be set
     * @param showColumnNumber
     *            the column number which must be shown in the editor
     */
    protected void setCellFocus(int row, int column, int showColumnNumber) {
        tableViewerLRFields.setSelection((new StructuredSelection(tableViewerLRFields.getGrid().getItem(row).getData())));
        tableViewerLRFields.getGrid().setFocusItem(tableViewerLRFields.getGrid().getItem(row));
        tableViewerLRFields.getGrid().setFocusColumn(tableViewerLRFields.getGrid().getColumn(column));
        tableViewerLRFields.getGrid().setCellSelection(new Point(column, row));

    }
    
    protected void selectRowAtField(Integer id) {
        // select  fields tab
        tabFolder.setSelection(1);
        
        // clear previous selection
        tableViewerLRFields.getGrid().setCellSelection(new Point[0]);
        
        // loop through ids in table
        boolean found=false;
        int row=0;
        for (GridItem item : tableViewerLRFields.getGrid().getItems()) {
            LRField field = (LRField)item.getData();
            if (field.getId().equals(id)) {
                tableViewerLRFields.getGrid().setFocusItem(item);
                tableViewerLRFields.getGrid().showItem(item);
                found = true;
                break;
            }
            row++;
        }
        if (found) {
            tableViewerLRFields.getGrid().select(row);
            tableViewerLRFields.getGrid().setFocusColumn(tableViewerLRFields.getGrid().getColumn(0));
        }
    }
    
    private void setPasteValues(LRField newField, int i, List<LRField> lrList) throws SAFRValidationException {

        newField.setName(lrList.get(i).getName());
        newField.setDataTypeCode(lrList.get(i).getDataTypeCode());
        newField.setLengthSimple(lrList.get(i).getLength());
        newField.setDecimals(lrList.get(i).getDecimals());
        newField.setPkeySeqNo(lrList.get(i).getPkeySeqNo());
        newField.setKeyType(lrList.get(i).getKeyType());
        newField.setOrdinalOffset(lrList.get(i).getOrdinalOffset());
        newField.setScaling(lrList.get(i).getScaling());
        newField.setDateTimeFormatCode(lrList.get(i).getDateTimeFormatCode());
        newField.setSigned(lrList.get(i).isSigned());
        newField.setHeaderAlignmentCode(lrList.get(i).getHeaderAlignmentCode());
        newField.setHeading1(lrList.get(i).getHeading1());
        newField.setHeading2(lrList.get(i).getHeading2());
        newField.setHeading3(lrList.get(i).getHeading3());
        newField.setNumericMaskCode(lrList.get(i).getNumericMaskCode());
        newField.setSubtotalLabel(lrList.get(i).getSubtotalLabel());
        newField.setSortKeyLabel(lrList.get(i).getSortKeyLabel());
        newField.setDefaultValue(lrList.get(i).getDefaultValue());
        newField.setDatabaseColumnName(lrList.get(i).getDatabaseColumnName());
        newField.setComment(lrList.get(i).getComment());
        newField.setPositionSimple(lrList.get(i).getPosition());
        newField.setOrdinalPosition(lrList.get(i).getOrdinalPosition());
    }    
    
    /**
     * This function is used to recalculates Fixed position and Ordinal position
     * from the selected row and below
     */
    protected void editCalculateFromHighlightedRows() {
        int selection = tableViewerLRFields.getGrid().getSelectionIndex();
        if (selection != -1) {
            LRField field =  (LRField) tableViewerLRFields.getGrid().getItem(selection).getData();
            logicalRecord.recalculateFields(field);
            tableViewerLRFields.setInput(logicalRecord);
            calculateFieldCount();
            setDirty(true);
        }
    }

    /**
     * This function is used to recalculates Fixed position and Ordinal position
     * of only selected rows
     */
    protected void editCalculateOnlyHighlightedRows() {
        List<LRField> selectedLrFieldsList = new ArrayList<LRField>();
        int[] selectedFieldIndexs = tableViewerLRFields.getGrid().getSelectionIndices();
        if (selectedFieldIndexs.length == 0) {
            return;
        }
        Arrays.sort(selectedFieldIndexs);
        for (int index : selectedFieldIndexs) {
            selectedLrFieldsList.add((LRField) tableViewerLRFields.getGrid().getItem(index).getData());
        }
        logicalRecord.recalculateFields(selectedLrFieldsList);
        tableViewerLRFields.setInput(logicalRecord);   
        calculateFieldCount();
        setDirty(true);
    }

    protected void editCalculateWithinHighlightedRow() {
        int selection = tableViewerLRFields.getGrid().getSelectionIndex();
        if (selection == -1) {
            return;
        }
        LRField field =  (LRField) tableViewerLRFields.getGrid().getItem(selection).getData();
        List<LRField> withinFields = new ArrayList<LRField>();
        withinFieldsR(field, withinFields);
        logicalRecord.recalculateFields(withinFields);
        tableViewerLRFields.setInput(logicalRecord);   
        calculateFieldCount();
        setDirty(true);
    }

    private void withinFieldsR(LRField field, List<LRField> withinFields) {
        withinFields.add(field);
        
        List<LRField> children =  field.getChildren();
        for (LRField child : children) {
            withinFieldsR(child, withinFields);
        }
    }
    
    
    /**
     * This function is used to recalculates Fixed position and Ordinal position
     * of all the rows
     */
    protected void editCalculateAllRows() {
        if (logicalRecord.getLRFields().getActiveItems().isEmpty()) {
            return;
        }
        LRField field = logicalRecord.getLRFields().getActiveItems().get(0);
        logicalRecord.recalculateFields(field);
        tableViewerLRFields.setInput(logicalRecord);
        calculateFieldCount();
        setDirty(true);
    }
    
    public void editCalculateRedefines() {
        if (logicalRecord.autocalcRedefine()) {
            tableViewerLRFields.setInput(logicalRecord);
            calculateFieldCount();
            setDirty(true);            
        }        
    }

    
    /**
     * This function is used to find a field in the table
     */
    protected void editFindField() {

        final InputDialog findDialog = new InputDialog(mediator.getSite()
                .getShell(), "Find Field",
                "Enter first few letters of field name :", "", null);
        findDialog.create();
        findDialog.open();
        searchText = findDialog.getValue();
        if (searchText == "" || searchText == null) {
            return;
        }

        List<LRField> lrFields = logicalRecord.getLRFields().getActiveItems();
        Boolean notFound = true;
        for (LRField field : lrFields) {
            if (field.getName() != null) {
                if (field.getName().toUpperCase().startsWith(searchText.toUpperCase())) {
                    focusOnField(field);
                    notFound = false;
                    break;
                }
            }
        }
        if (notFound) {
            MessageDialog.openInformation(mediator.getSite().getShell(), "", "Field '"
                    + searchText + "' not found");
        }
    }
    
    protected void setCurrentCellEditor(CellEditor currentCellEditor) {
        this.currentCellEditor = currentCellEditor;
    }

    protected LRFieldCellEditor getDateTimeFormatEditor() {
        return dateTimeFormatEditor;
    }   
        
    protected LogicalRecord getLogicalRecord() {
        return logicalRecord;
    }

    protected EditRights getEditRights() {
        return mediator.getEditRights();
    }

    protected void setDirty(boolean b) {
        mediator.setDirty(b);
    }

    protected Control getCompositeLRFields() {
        return compositeLRFields;
    }

    protected void setLRFieldsFocus() {
        tableViewerLRFields.getGrid().setFocus();
    }

    protected void refreshToolbar() {
        
        int[] index = tableViewerLRFields.getGrid().getSelectionIndices();
        // Get the source provider service
        SourceProvider service = UIUtilities.getSourceProvider();
                
        if (index.length == 0) {
            if (logicalRecord.getLRFields().getActiveItems().isEmpty()) {
                service.setAllowInsertLRField(true);
            }
            else {
                service.setAllowInsertLRField(false);                
            }
            service.setMoveUpAllowed(false);
            service.setMoveDownAllowed(false);
            service.setAllowRecalcFrom(false);
            service.setAllowRecalcOnly(false);                                
            service.setAllowRecalcWithin(false);                                
        }
        else if (index.length == 1) {
            service.setAllowInsertLRField(true);                    
            if (index[0] == 0) {
                service.setMoveUpAllowed(false);
            } else {
                service.setMoveUpAllowed(true);
            }

            if ((index[0] + 1) >= tableViewerLRFields.getGrid().getItemCount()) {
                service.setMoveDownAllowed(false);
            } else {
                service.setMoveDownAllowed(true);
            }         
            
            LRField field = (LRField) tableViewerLRFields.getGrid().getItem(index[0]).getData();
            if (field != null) {
                Integer level = field.getLevel();
                if (level != null && level == 1) {
                    service.setAllowRecalcFrom(true);
                }
                else {
                    service.setAllowRecalcFrom(false);                                    
                }
            }
            else {
                service.setAllowRecalcFrom(false);                
            }
            service.setAllowRecalcOnly(false);
            if (field.getChildren().isEmpty()) {
                service.setAllowRecalcWithin(false);                
            }
            else {
                service.setAllowRecalcWithin(true);
            }
        }
        else {
            service.setAllowInsertLRField(false);                                        
            service.setMoveUpAllowed(false);
            service.setMoveDownAllowed(false);
            service.setAllowRecalcFrom(false);
            service.setAllowRecalcOnly(true);                
            service.setAllowRecalcWithin(false);                
        }
    }
    
    protected void refreshLRFields() {
        tableViewerLRFields.refresh();
    }

    protected Control getControlFromProperty() {
        return tableViewerLRFields.getControl();
    }

    public LRField getCurrentLRField() {
        int index = tableViewerLRFields.getGrid().getSelectionIndex();
        if (index > (tableViewerLRFields.getGrid().getItemCount() - 1)
                || index < 0) {
            return null;
        }
        LRField lrField = (LRField) tableViewerLRFields.getGrid().getItems()[index].getData();
        return lrField;
    }

    public void expandAll() {
        expandState.expandAll();
        tableViewerLRFields.refresh();
    }

    public void collapseAll() {
        expandState.collapseAll();
        tableViewerLRFields.refresh();
    }

    public GridTableViewer getTableViewerLRFields() {
		return tableViewerLRFields;
	}

	public void setTableViewerLRFields(GridTableViewer tableViewerLRFields) {
		this.tableViewerLRFields = tableViewerLRFields;
	}

}
