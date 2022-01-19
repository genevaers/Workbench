package com.ibm.safr.we.ui.dialogs.viewgen;

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

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.dialogs.viewgen.ViewGenCriteria.EditMode;

public class ViewGenColumn {
    
    public enum ColumnType {
        COLUMNNO("No", 65),
        SOURCETYPE("Type", 110),
        FIELDID("Field", 160),
        DATATYPE("Data Type", 100),
        LENGTH("Len", 40),
        HEADING("Heading", 110);
        
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
    
    class ColumnContentGenerator implements IStructuredContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return view.getViewColumns().getActiveItems().toArray();
        }        
    }

    private class ViewColumnLabelProvider extends ColumnLabelProvider {

        @Override
        public void update(ViewerCell cell) {
            
            ViewColumn column = (ViewColumn)cell.getElement();
            ViewColumnSource colSrc = column.getViewColumnSources().get(viewSource.getSequenceNo()-1);
            cell.setText("");
            cell.setFont(mediator.getFont());
            if (cell.getColumnIndex() == ColumnType.SOURCETYPE.ordinal()) {
                cell.setText(colSrc.getSourceType().getDescription());
            } else if (cell.getColumnIndex() == ColumnType.FIELDID.ordinal()) {
                if (colSrc.getSourceType().getGeneralId().equals(Codes.SOURCE_FILE_FIELD)) {
                    cell.setText(colSrc.getLRField().getDescriptor());
                }
                else if (colSrc.getSourceType().getGeneralId().equals(Codes.LOOKUP_FIELD)) {
                    if (colSrc.getLRField() != null) {
                        cell.setText(colSrc.getLRField().getDescriptor());
                    } else {
                        cell.setText("");
                    }
                }
            } else if (cell.getColumnIndex() == ColumnType.COLUMNNO.ordinal()) {
                cell.setText(column.getColumnNo().toString());
            } else if (cell.getColumnIndex() == ColumnType.HEADING.ordinal()) {
                String heading = "";
                if (column.getHeading1() != null && !column.getHeading1().isEmpty()) {
                    heading += column.getHeading1() + " ";
                }
                if (column.getHeading2() != null && !column.getHeading2().isEmpty()) {
                    heading += column.getHeading2() + " ";
                }
                if (column.getHeading3() != null && !column.getHeading3().isEmpty()) {
                    heading += column.getHeading3() + " ";
                }
                if (!heading.isEmpty()) {
                    heading.substring(0, heading.length()-1);
                }
                cell.setText(heading);
            } else if (cell.getColumnIndex() == ColumnType.DATATYPE.ordinal()) {
                cell.setText(column.getDataTypeCode().getDescription());
            } else if (cell.getColumnIndex() == ColumnType.LENGTH.ordinal()) {
                cell.setText(column.getLength().toString());
            }
        }
        
    }
    
    private ViewGenMediator mediator;    
    private View view;
    private ViewSource viewSource;
    
    private Composite parent;
    private Composite compositeColumn;
    private Button constant;
    private Button formula;
    private Button remove;    
    private Button up;    
    private Button down;    
    private Table tableColumn;
    private CheckboxTableViewer tableViewerColumn;
    private int prevSelection=-1;

    public ViewGenColumn(
        ViewGenMediator mediator, 
        Composite parent,
        View view, 
        ViewSource viewSource) {
        this.mediator = mediator;
        this.parent = parent;
        this.view = view;
        this.viewSource = viewSource;
    }

    public void create() {
        compositeColumn = mediator.getGUIToolKit().createComposite(parent,SWT.NONE);
        compositeColumn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
        
        GridLayout layout = new GridLayout(2, false);
        compositeColumn.setLayout(layout);
        
        // field label
        Label fieldName = mediator.getGUIToolKit().createLabel(compositeColumn, SWT.NONE,"Column:");
        fieldName.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        
        createButtons();
        
        createTable();        
    }

    protected void createButtons() {
        Composite compositeButtons = mediator.getGUIToolKit().createComposite(compositeColumn,SWT.NONE);
        compositeButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
        
        RowLayout butLayout = new RowLayout();
        butLayout.wrap = false;
        butLayout.pack = false;
        butLayout.justify = false;
        butLayout.spacing = 2;
        
        compositeButtons.setLayout(butLayout);
        
        constant = mediator.getGUIToolKit().createButton(compositeButtons, SWT.PUSH, "  C  ");
        constant.setEnabled(false);
        constant.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                putConstant();
            }

        });
        formula = mediator.getGUIToolKit().createButton(compositeButtons, SWT.PUSH, " fx ");
        formula.setEnabled(false);
        formula.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                putFormula();
            }

        });
        up = mediator.getGUIToolKit().createButton(compositeButtons, SWT.PUSH, "Up");
        up.setEnabled(false);
        up.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveUp();
            }

        });
        down = mediator.getGUIToolKit().createButton(compositeButtons, SWT.PUSH, "Down");
        down.setEnabled(false);
        down.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveDown();
            }

        });
        
        remove = mediator.getGUIToolKit().createButton(compositeButtons, SWT.PUSH, "Remove");
        remove.setEnabled(false);
        remove.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                removeColumns();
            }

        });
    }
    
    protected void createTable() {
        tableColumn = mediator.getGUIToolKit().createTable(compositeColumn,
            SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.CHECK, false);
        GridData tabData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
        tabData.minimumWidth = 600;
        tabData.widthHint = 600;
        tabData.minimumHeight = 340;
        tabData.heightHint = 340;
        tabData.horizontalSpan = 2;
        tableColumn.setLayoutData(tabData);
        tableViewerColumn = new CheckboxTableViewer(tableColumn);
        tableColumn.setHeaderVisible(true);
        tableColumn.setLinesVisible(true);
        
        for (ColumnType type : ColumnType.values()) {
            TableViewerColumn column = new TableViewerColumn(tableViewerColumn, SWT.NONE);
            column.setLabelProvider(new ColumnLabelProvider() {

                @Override
                public Font getFont(Object element) {
                    return mediator.getBoldFont();
                }
                
            });
            column.getColumn().setText(type.getName());
            column.getColumn().setToolTipText(type.getName());
            column.getColumn().setWidth(type.getWidth());
            column.getColumn().setResizable(true);
        }

        tableViewerColumn.setContentProvider(new ColumnContentGenerator());
        tableViewerColumn.setLabelProvider(new ViewColumnLabelProvider());
        
        tableColumn.addListener(SWT.Selection, new Listener() {
            
            public void handleEvent(Event e) {
                if (e.detail == SWT.CHECK) {                  
                    int selRow = tableColumn.indexOf((TableItem)e.item);  
                    int stateMask=e.stateMask;                  
                    if((stateMask & SWT.SHIFT)==SWT.SHIFT){
                        int prevRow = prevSelection;
                        
                        if((stateMask & SWT.CTRL)!=SWT.CTRL){
                            tableViewerColumn.setAllChecked(false);
                        }
                        if (prevRow > selRow) {
                            for (int i=selRow ; i<=prevRow ; i++) {
                                Object element = tableViewerColumn.getElementAt(i);
                                tableViewerColumn.setChecked(element, true);
                            }
                        }
                        else {
                            for (int i=prevRow ; i<=selRow ; i++) {
                                Object element = tableViewerColumn.getElementAt(i);
                                tableViewerColumn.setChecked(element, true);
                            }                            
                        }
                    }   
                    else {
                        Object element = tableViewerColumn.getElementAt(selRow);
                        if (tableViewerColumn.getChecked(element)) {
                            prevSelection = tableColumn.indexOf((TableItem)e.item);
                        }
                        else {
                            prevSelection = 0;
                        }
                    }
                }                  
              
            }
        });
        
        tableColumn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                constant.setEnabled(true);
                formula.setEnabled(true);
                up.setEnabled(true);
                down.setEnabled(true);
                mediator.refreshAddButtonState();                
            }

        });
        
        tableViewerColumn.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (tableViewerColumn.getCheckedElements().length == 0) {
                    remove.setEnabled(false);
                } else {
                    remove.setEnabled(true);                    
                }
            }
            
        });
        tableViewerColumn.setInput(1);
    }

    public boolean isColumnSelected() {
        return tableColumn.getSelectionIndex() != -1;
    }

    public boolean viewHasNoColumns() {
        return view.getViewColumns().getActiveItems().isEmpty();
    }

    public int numberColumnsFromSelection() {        
        return tableColumn.getItemCount() - tableColumn.getSelectionIndex();
    }    

    public void putFields(
        List<LRField> lrFields,
        List<FieldTreeNodeLeaf> lpFields) {
        
        // get the edit mode
        EditMode mode = mediator.getEditMode();
        
        switch (mode) {
            case INSERTBEFORE :
                insertBefore(lrFields, lpFields);
                break;
            case INSERTAFTER :
                insertAfter(lrFields, lpFields);
                break;
            case OVERALL :
                overAll(lrFields, lpFields);
                break;
            case OVERSOURCE :
                overSource(lrFields, lpFields);
                break;
            default :
                break;                    
        }
        
    }

    private void insertBefore(List<LRField> lrFields, List<FieldTreeNodeLeaf> lpFields) {
        
        // insert source file fields
        int position;        
        if (viewHasNoColumns()) {
            position = 0;
        } else {
            position = tableColumn.getSelectionIndex();
        }
        
        insertColumns(lrFields, lpFields, position);
    }

    
    private void insertAfter(List<LRField> lrFields, List<FieldTreeNodeLeaf> lpFields) {
        
        // insert source file fields
        int position;
        if (viewHasNoColumns()) {
            position = 0;
        } else {
            position = tableColumn.getSelectionIndex() + 1;
        }
        
        insertColumns(lrFields, lpFields, position);
    }

    protected void insertColumns(List<LRField> lrFields,
        List<FieldTreeNodeLeaf> lpFields, int position) {
        int newPos = view.genFieldsAsColumns(lrFields, position, viewSource);
        
        // insert lookup path fields
        for (FieldTreeNodeLeaf lpLeaf : lpFields) {
            FieldTreeNodeLR lrNode = (FieldTreeNodeLR)lpLeaf.getParent();
            FieldTreeNodeLP lpNode = (FieldTreeNodeLP)lrNode.getParent();
            
            view.genLPFieldAsColumn(lpLeaf.getField(), newPos, viewSource,
                lrNode.getLrBean(), lpNode.getLPBean());            
            newPos++;
        }
        tableViewerColumn.setInput(1);
        tableColumn.setSelection(newPos-1);
    }

    private void overAll(List<LRField> lrFields, List<FieldTreeNodeLeaf> lpFields) {
        
        int position = tableColumn.getSelectionIndex();
        
        int newPos = view.overAllFieldsAsColumns(lrFields, position, viewSource);
        
        // insert lookup path fields
        for (FieldTreeNodeLeaf lpLeaf : lpFields) {
            FieldTreeNodeLR lrNode = (FieldTreeNodeLR)lpLeaf.getParent();
            FieldTreeNodeLP lpNode = (FieldTreeNodeLP)lrNode.getParent();
            
            view.overAllLPFieldAsColumn(lpLeaf.getField(), newPos, viewSource,
                lrNode.getLrBean(), lpNode.getLPBean());            
            newPos++;
        }
        tableViewerColumn.setInput(1);
        tableColumn.setSelection(newPos-1);        
    }
    
    private void overSource(List<LRField> lrFields, List<FieldTreeNodeLeaf> lpFields) {
        
        int position = tableColumn.getSelectionIndex();
        
        int newPos = view.overSourceFieldsAsColumns(lrFields, position, viewSource);
        
        // insert lookup path fields
        for (FieldTreeNodeLeaf lpLeaf : lpFields) {
            FieldTreeNodeLR lrNode = (FieldTreeNodeLR)lpLeaf.getParent();
            FieldTreeNodeLP lpNode = (FieldTreeNodeLP)lrNode.getParent();
            
            view.overSourceLPFieldAsColumn(lpLeaf.getField(), newPos, viewSource,
                lrNode.getLrBean(), lpNode.getLPBean());            
            newPos++;
        }
        tableViewerColumn.setInput(1);
        tableColumn.setSelection(newPos-1);        
    }
    
    protected void putConstant() {
        int position = tableColumn.getSelectionIndex();
        
        position = generateConstant(position);
        tableViewerColumn.setInput(1);
        if (position >= tableViewerColumn.getTable().getItemCount()-1) {
            tableColumn.setSelection(position);                    
        } else {
            tableColumn.setSelection(position+1);                                
        }
    }

    private void putFormula() {
        int position = tableColumn.getSelectionIndex();
        
        position = generateConstant(position);
        
        ViewColumn col = view.getViewColumns().getActiveItems().get(position);        
        ViewColumnSource colSrc = col.getViewColumnSources().get(viewSource.getSequenceNo()-1);
        colSrc.setSourceType(SAFRApplication.getSAFRFactory().
            getCodeSet(CodeCategories.COLSRCTYPE).getCode(Codes.FORMULA));
        
        tableViewerColumn.setInput(1);
        if (position >= tableViewerColumn.getTable().getItemCount()-1) {
            tableColumn.setSelection(position);                    
        } else {
            tableColumn.setSelection(position+1);                                
        }
    }

    protected int generateConstant(int position) {
        // get the edit mode
        EditMode mode = mediator.getEditMode();
        
        switch (mode) {
            case INSERTBEFORE :
                view.addViewColumn(position+1);
                break;
            case INSERTAFTER :
                view.addViewColumn(position+2);
                position++;
                break;
            case OVERALL :
                view.overAllAsConstant(position, viewSource);
                break;
            case OVERSOURCE :
                view.overSourceAsConstant(position, viewSource);
                break;
            default :
                break;                    
        }
        return position;
    }

    protected void moveDown() {
        int position = tableColumn.getSelectionIndex();        
        if (position < view.getViewColumns().getActiveItems().size()-1) {
            view.moveColumnRight(view.getViewColumns().getActiveItems().get(position));
            tableViewerColumn.setInput(1);
            tableColumn.setSelection(position+1);
        }
    }

    protected void moveUp() {
        int position = tableColumn.getSelectionIndex();
        if (position > 0) { 
            view.moveColumnLeft(view.getViewColumns().getActiveItems().get(position));
            tableViewerColumn.setInput(1);
            tableColumn.setSelection(position-1);
        }
    }

    
    protected void removeColumns() {
        Object[] checked = tableViewerColumn.getCheckedElements();
        for (Object obj : checked) {
            ViewColumn col = (ViewColumn)obj;
            view.removeViewColumn(col);
        }
        tableColumn.removeAll();
        tableViewerColumn.setInput(1);
        remove.setEnabled(false);
    }
    
    
    
}
