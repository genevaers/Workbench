package com.ibm.safr.we.ui.editors.view;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.jface.gridviewer.GridViewerEditor;
import org.eclipse.nebula.jface.gridviewer.GridViewerRow;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSortKey;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.commands.SourceProvider;
import com.ibm.safr.we.ui.dialogs.NewViewSourceDialog;
import com.ibm.safr.we.ui.dialogs.viewgen.ViewGenDialog;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditor;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditorInput;
import com.ibm.safr.we.ui.utilities.IRowEditingSupport;
import com.ibm.safr.we.ui.utilities.IRowEditingSupportExtended;
import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.RowEditorType;
import com.ibm.safr.we.ui.utilities.SAFRComboBoxCellEditor;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.vieweditor.ColumnSourceView;
import com.ibm.safr.we.ui.views.vieweditor.DataSourceView;
import com.ibm.safr.we.ui.views.vieweditor.SortKeyTitleView;
import com.ibm.safr.we.ui.views.vieweditor.SortKeyView;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ViewColumnEditor {

    // statics
    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.view.ViewColumnEditor");

    private static final String[] rowHeaders = { "Column Headers:", "Heading 1",
            "Heading 2", "Heading 3", "Sort Keys:", "Column Output:",
            "Start-End  Position", "Data Type", "Date/Time Format", "Length",
            "Data Alignment", "Visible", "Spaces before column",
            "Header Alignment", "Decimal Places", "Scaling Factor", "Signed",
            "Numeric Mask", "Format-Phase Column Logic",
            "Record Aggregation Function", "Group Aggregation Function",
            "View Sources:" };

    final int[] rowsToInclude = { ViewColumnEditor.COLUMN_HEADING_HEADER, ViewColumnEditor.HEAD1, ViewColumnEditor.HEAD2, ViewColumnEditor.HEAD3,
            ViewColumnEditor.SORTKEY_HEADER, ViewColumnEditor.COLUMN_PROP_HEADER, ViewColumnEditor.STARTPOS, ViewColumnEditor.DATATYPE,
            ViewColumnEditor.DATETIMEFORMAT, ViewColumnEditor.LENGTH, ViewColumnEditor.DATAALIGNMENT, ViewColumnEditor.VISIBLE, ViewColumnEditor.SPACEBEFORECOLUMN,
            ViewColumnEditor.HEADERALIGNMENT, ViewColumnEditor.DECIMALPLACES, ViewColumnEditor.SCALINGFACTOR, ViewColumnEditor.SIGNED, ViewColumnEditor.NUMERICMASK,
            ViewColumnEditor.FORMATPHASECALCULATION, ViewColumnEditor.RECORDAGGREGATIONFUNCTION,
            ViewColumnEditor.GROUPAGGREGATIONFUNCTION, ViewColumnEditor.DATA_SOURCE_HEADER };
    
    private static final String COL_ID = "ID";
    private static final String VALUE = "VALUE";
    private static final String LABEL_PROVIDER = "LABEL_PROVIDER";
    private static final String EDITING_SUPPORT = "EDITING_SUPPORT";        
    
    public static final int MAX_ROW_HEADER_WIDTH = 300;
    public static final int MIN_ROW_HEADER_WIDTH = 180;
    public static final int EDITOR_ROW_HEIGHT = 18;    
    private static final int VIEW_COL_WIDTH = 100;
    
    private static Color orange = null;
    private static Color purple = null;
    
    // inner classes
        
    private enum ViewRowType {
        COLUMN_HEADER, HEADING, SORTKEY_HEADER, SORTKEY, COLUMN_PROPERTY, COLUMN_PROPERTY_HEADER, DATA_SOURCE, DATA_SOURCE_HEADER;
    }
    
    protected enum PropertyViewType {
        DATASOURCE(DataSourceView.ID), SORTKEY(SortKeyView.ID), COLUMNSOURCE(
                ColumnSourceView.ID), NONE("None");
        private String viewId;

        private PropertyViewType(String viewId) {
            this.viewId = viewId;
        }

        public String getViewId() {
            return viewId;
        }
    }
    
    private class ViewRow implements IRowEditingSupport, IRowEditingSupportExtended {
        private Integer propertyID;
        private String propertyDescription;
        private RowEditorType editorType;
        private ViewRowType rowType;
        private boolean expandable = false;
        private boolean expanded = false;
        private boolean visible = true;
        private Object data;

        /**
         * Creates a new Mediator object representing a row in View grid.
         * 
         * @param viewColumns
         *            The list of view columns.
         * @param propertyID
         *            The property ID of this Mediator.
         * @param data
         *            Additional data information. This parameter cannot be null
         *            for dynamic rows like Sortkey and Datasource.
         * @throws IllegalArgumentException
         *             If the data is null for dynamic rows like SORTKEY and
         *             DATASOURCE.
         */
        public ViewRow(int propertyID, Object data) {
            if (propertyID == ViewColumnEditor.SORTKEY || propertyID == ViewColumnEditor.DATASOURCE) {
                if (data == null) {
                    throw new IllegalArgumentException("Data cannot be null for dynamic rows.");
                }
            }
            this.propertyID = propertyID;
            this.data = data;
            switch (propertyID) {
            case ViewColumnEditor.HEAD1:
            case ViewColumnEditor.HEAD2:
            case ViewColumnEditor.HEAD3:
                this.editorType = RowEditorType.TEXT;
                this.rowType = ViewRowType.HEADING;
                break;
            case ViewColumnEditor.STARTPOS:
                this.rowType = ViewRowType.COLUMN_PROPERTY;
                this.editorType = RowEditorType.NONE;
                break;

            case ViewColumnEditor.LENGTH:
            case ViewColumnEditor.SPACEBEFORECOLUMN:
            case ViewColumnEditor.DECIMALPLACES:
            case ViewColumnEditor.SCALINGFACTOR:
                this.editorType = RowEditorType.NUMBER;
                this.rowType = ViewRowType.COLUMN_PROPERTY;
                break;

            case ViewColumnEditor.DATAALIGNMENT:
            case ViewColumnEditor.DATATYPE:
            case ViewColumnEditor.DATETIMEFORMAT:
            case ViewColumnEditor.HEADERALIGNMENT:
            case ViewColumnEditor.NUMERICMASK:
            case ViewColumnEditor.RECORDAGGREGATIONFUNCTION:
            case ViewColumnEditor.GROUPAGGREGATIONFUNCTION:
                this.editorType = RowEditorType.COMBO;
                this.rowType = ViewRowType.COLUMN_PROPERTY;
                break;

            case ViewColumnEditor.FORMATPHASECALCULATION:
                this.rowType = ViewRowType.COLUMN_PROPERTY;
                this.editorType = RowEditorType.DIALOG;
                break;

            case ViewColumnEditor.VISIBLE:
            case ViewColumnEditor.SIGNED:
                this.editorType = RowEditorType.CHECKBOX;
                this.rowType = ViewRowType.COLUMN_PROPERTY;
                break;
            case ViewColumnEditor.COLUMN_PROP_HEADER:
                this.rowType = ViewRowType.COLUMN_PROPERTY_HEADER;
                this.editorType = RowEditorType.NONE;
                this.expandable = true;
                this.expanded = true;
                break;
            case ViewColumnEditor.COLUMN_HEADING_HEADER:
                this.rowType = ViewRowType.COLUMN_HEADER;
                this.editorType = RowEditorType.NONE;
                this.expandable = true;
                this.expanded = true;
                break;
            case ViewColumnEditor.DATA_SOURCE_HEADER:
                this.rowType = ViewRowType.DATA_SOURCE_HEADER;
                this.editorType = RowEditorType.NONE;
                this.expandable = true;
                this.expanded = true;
                break;
            case ViewColumnEditor.DATASOURCE:
                this.rowType = ViewRowType.DATA_SOURCE;
                this.editorType = RowEditorType.NONE;
                break;
            case ViewColumnEditor.SORTKEY_HEADER:
                this.rowType = ViewRowType.SORTKEY_HEADER;
                this.editorType = RowEditorType.NONE;
                this.expandable = true;
                this.expanded = false;
                break;
            case ViewColumnEditor.SORTKEY:
                this.rowType = ViewRowType.SORTKEY;
                this.editorType = RowEditorType.NONE;
                break;
            default:
                break;
            }
        }

        public String getPropertyName() {
            switch (propertyID) {
            case ViewColumnEditor.HEAD1:
            case ViewColumnEditor.HEAD2:
            case ViewColumnEditor.HEAD3:
            case ViewColumnEditor.STARTPOS:
            case ViewColumnEditor.LENGTH:
            case ViewColumnEditor.DATATYPE:
            case ViewColumnEditor.DATETIMEFORMAT:
            case ViewColumnEditor.DATAALIGNMENT:
            case ViewColumnEditor.VISIBLE:
            case ViewColumnEditor.SPACEBEFORECOLUMN:
            case ViewColumnEditor.HEADERALIGNMENT:
            case ViewColumnEditor.DECIMALPLACES:
            case ViewColumnEditor.SCALINGFACTOR:
            case ViewColumnEditor.SIGNED:
            case ViewColumnEditor.NUMERICMASK:
            case ViewColumnEditor.FORMATPHASECALCULATION:
            case ViewColumnEditor.RECORDAGGREGATIONFUNCTION:
            case ViewColumnEditor.GROUPAGGREGATIONFUNCTION:
            case ViewColumnEditor.COLUMN_PROP_HEADER:
            case ViewColumnEditor.COLUMN_HEADING_HEADER:
            case ViewColumnEditor.DATA_SOURCE_HEADER:
            case ViewColumnEditor.SORTKEY_HEADER:
                propertyDescription = rowHeaders[propertyID];
                break;

            case ViewColumnEditor.SORTKEY:
                propertyDescription = "Sort Key";
                break;
            default:
                propertyDescription = "";
                break;
            }

            return propertyDescription;
        }

        public RowEditorType getRowEditorType() {
            return editorType;
        }

        public ViewRowType getRowType() {
            return rowType;
        }

        public Object getValue(int index) {
            ViewColumn column = view.getViewColumns().getActiveItems().get(index);
            switch (propertyID) {
            case ViewColumnEditor.COLUMN_HEADING_HEADER:
                if (this.isExpanded()) {
                    return null;
                } else {
                    return column.getHeading1();
                }
            case ViewColumnEditor.HEAD1:
                return column.getHeading1();
            case ViewColumnEditor.HEAD2:
                return column.getHeading2();
            case ViewColumnEditor.HEAD3:
                return column.getHeading3();
            case ViewColumnEditor.COLUMN_PROP_HEADER:
                if (this.isExpanded()) {
                    return null;
                } else {
                    return getStartPositionLabel(column);
                }
            case ViewColumnEditor.STARTPOS:
                return getStartPositionLabel(column);
            case ViewColumnEditor.DATATYPE:
                return column.getDataTypeCode();
            case ViewColumnEditor.DATETIMEFORMAT:
                return column.getDateTimeFormatCode();
            case ViewColumnEditor.LENGTH:
                return column.getLength().toString();
            case ViewColumnEditor.DATAALIGNMENT:
                return column.getDataAlignmentCode();
            case ViewColumnEditor.VISIBLE:
                return column.isVisible();
            case ViewColumnEditor.SPACEBEFORECOLUMN:
                return column.getSpacesBeforeColumn().toString();
            case ViewColumnEditor.HEADERALIGNMENT:
                return column.getHeaderAlignmentCode();
            case ViewColumnEditor.DECIMALPLACES:
                return column.getDecimals().toString();
            case ViewColumnEditor.SCALINGFACTOR:
                return column.getScaling().toString();
            case ViewColumnEditor.SIGNED:
                return column.isSigned();
            case ViewColumnEditor.NUMERICMASK:
                return column.getNumericMaskCode();
            case ViewColumnEditor.FORMATPHASECALCULATION:
                return column.getFormatColumnCalculation();
            case ViewColumnEditor.RECORDAGGREGATIONFUNCTION:
                return column.getRecordAggregationCode();
            case ViewColumnEditor.GROUPAGGREGATIONFUNCTION:
                return column.getGroupAggregationCode();
            case ViewColumnEditor.SORTKEY_HEADER:
                return column.isSortKey();
            default:
                return null;
            }
        }

        protected String getStartPositionLabel(ViewColumn column) {
            if (view.getOutputFormat().equals(OutputFormat.Format_Fixed_Width_Fields)) {
                if (column.isVisible() ) {
                    Integer end = column.getStartPosition() + column.getLength()-1;
                    String pos = column.getStartPosition().toString() + "-" + end.toString();
                    return pos;
                }
                else {
                    return "0";
                }
            } else if (view.getOutputFormat().equals(OutputFormat.Extract_Fixed_Width_Fields)){
                if (column.isSortKey()) {
                    return "0";
                }
                else {
                    Integer end = column.getStartPosition() + column.getLength()-1;
                    String pos = column.getStartPosition().toString() + "-" + end.toString();
                    return pos;
                }
                
            } else {
                return "";
            }
        }

        public void setValue(int index, Object value) {
            ViewColumn column = view.getViewColumns().getActiveItems().get(index);

            switch (propertyID) {
            case ViewColumnEditor.HEAD1:
                column.setHeading1((String) value);
                break;
            case ViewColumnEditor.HEAD2:
                column.setHeading2((String) value);
                break;
            case ViewColumnEditor.HEAD3:
                column.setHeading3((String) value);
                break;
            case ViewColumnEditor.STARTPOS:
                column.setStartPosition((Integer) value);
                break;
            case ViewColumnEditor.DATATYPE:
                // mandatory Code field. call setter only if value is not
                // null,
                // else a NPE will be thrown.
                if (value != null) {
                    column.setDataTypeCode((Code) value);
                }
                break;
            case ViewColumnEditor.DATETIMEFORMAT:
                column.setDateTimeFormatCode((Code) value);
                break;
            case ViewColumnEditor.LENGTH:
                column.setLength(UIUtilities.stringToInteger((String) value));
                break;
            case ViewColumnEditor.DATAALIGNMENT:
                column.setDataAlignmentCode((Code) value);
                break;
            case ViewColumnEditor.VISIBLE:
                column.setVisible((Boolean) value);
                break;
            case ViewColumnEditor.SPACEBEFORECOLUMN:
                column.setSpacesBeforeColumn(UIUtilities.stringToInteger((String) value));
                break;
            case ViewColumnEditor.HEADERALIGNMENT:
                column.setHeaderAlignmentCode((Code) value);
                break;
            case ViewColumnEditor.DECIMALPLACES:
                column.setDecimals(UIUtilities.stringToInteger((String) value));
                break;
            case ViewColumnEditor.SCALINGFACTOR:
                column.setScaling(UIUtilities.stringToInteger((String) value));
                break;
            case ViewColumnEditor.SIGNED:
                column.setSigned((Boolean) value);
                break;
            case ViewColumnEditor.NUMERICMASK:
                column.setNumericMaskCode((Code) value);
                break;
            case ViewColumnEditor.FORMATPHASECALCULATION:
                column.setFormatColumnCalculation((String) value);
                break;
            case ViewColumnEditor.RECORDAGGREGATIONFUNCTION:
                column.setRecordAggregationCode((Code) value);
                break;
            case ViewColumnEditor.GROUPAGGREGATIONFUNCTION:
                column.setGroupAggregationCode((Code) value);
                break;
            default:
                break;
            }
        }

        public int getPropertyID() {
            return propertyID;
        }

        public boolean isExpandable() {
            return expandable;
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isVisible() {
            return visible;
        }

        public Object getData() {
            return data;
        }

        @Override
        public boolean equals(Object obj) {

            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ViewRow)) {
                return false;
            }
            ViewRow other = (ViewRow) obj;
            if (this.propertyID != other.getPropertyID()) {
                // property id not same so the rows are not equal
                return false;
            } else {
                if (other.getPropertyID() == ViewColumnEditor.SORTKEY || other.getPropertyID() == ViewColumnEditor.DATASOURCE) {
                    // these are dynamic rows, check if the data is equal as
                    // every dynamic row has a data object.
                    return this.data.equals(other.getData());
                } else {
                    // property Ids are same so its equal.
                    return true;
                }
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (null == this.propertyID ? 0 : this.propertyID.hashCode());
            if ((this.propertyID == ViewColumnEditor.SORTKEY) || (this.propertyID == ViewColumnEditor.DATASOURCE)) {
                hash = 31 * hash + (null == this.data ? 0 : this.data.hashCode());
            }
            return hash;
        }

    }

    private class ViewEditorContentProvider implements IStructuredContentProvider {
        public ViewEditorContentProvider() {
        }

        public Object[] getElements(Object inputElement) {
            List<ViewRow> visibleRows = new ArrayList<ViewRow>();
            for (ViewRow row : viewEditorRows) {
                if (row.isVisible()) {
                    visibleRows.add(row);
                }
            }
            return visibleRows.toArray();
        }

        public void dispose() {

        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    private class LabelProviderImpl extends ColumnLabelProvider {
        private int colIndex;
        private ViewColumn column;

        public LabelProviderImpl(int colIndex, ViewColumn column) {
            this.colIndex = colIndex;
            this.column = column;
        }

        public void setColumnIndex(int index) {
            colIndex = index;
            // change the actual column object too.
            this.column = view.getViewColumns().getActiveItems().get(index);
        }

        @Override
        public String getText(Object element) {
            ViewRow m = (ViewRow) element;
            switch (m.getPropertyID()) {
            case ViewColumnEditor.SORTKEY_HEADER:
                if ((Boolean) m.getValue(colIndex) == true) {
                    ViewSortKey key = view.getViewColumns().getActiveItems()
                            .get(colIndex).getViewSortKey();
                    return "#" + key.getKeySequenceNo();
                }
                break;
            case ViewColumnEditor.DATASOURCE:
                String returnStr = "";

                ViewSource source = (ViewSource) m.getData();
                // get the view source of current column.
                ViewColumnSource vsource = view.getViewColumns()
                        .getActiveItems().get(colIndex).getViewColumnSources()
                        .get(source.getSequenceNo() - 1);
                try {
                    if (vsource.getSourceType() != null) {
                        if (vsource.getSourceType().getGeneralId() == Codes.CONSTANT) {
                            returnStr = vsource.getSourceValue();
                        } else if (vsource.getSourceType().getGeneralId() == Codes.FORMULA) {
                            returnStr = vsource.getExtractColumnAssignment();
                            if (returnStr != null && returnStr.length() > UIUtilities.TABLECELLLIMIT)
                            {
                                returnStr = returnStr.substring(0, UIUtilities.TABLECELLLIMIT-3) + "...";
                            }
                        } else if (vsource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                            returnStr = vsource.getLRField() == null ? null
                                    : vsource.getLRField().getName();
                        } else if (vsource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                            returnStr = vsource.getLRField() == null ? null
                                    : vsource.getLRField().getName();
                        }
                    }
                } catch (SAFRException se) {
                    UIUtilities.handleWEExceptions(se);
                }
                return returnStr == null ? "" : returnStr;
                // CQ 8056. Santhosh. 21/06/2010
                // Clear the cells at UI if they donot apply to the current
                // context.
            case ViewColumnEditor.DATETIMEFORMAT:
                if (!enableViewEditorCells(column, m.getPropertyID())) {

                    return null;

                } else if (column.getDateTimeFormatCode() != null) {
                    return column.getDateTimeFormatCode().getDescription();
                }
                break;
            case ViewColumnEditor.DATAALIGNMENT:
                if (!enableViewEditorCells(column, m.getPropertyID())) {
                    return null;
                } else if (column.getDataAlignmentCode() != null) {
                    return column.getDataAlignmentCode().getDescription();
                }
                break;
            case ViewColumnEditor.VISIBLE:

                if (!enableViewEditorCells(column, m.getPropertyID())) {
                    return null;
                } else if (column.isVisible() != null) {
                    return "";
                }
                break;
            case ViewColumnEditor.SPACEBEFORECOLUMN:
                if (!enableViewEditorCells(column, m.getPropertyID())) {
                    return null;
                } else if (column.getSpacesBeforeColumn() != null) {
                    return column.getSpacesBeforeColumn().toString();
                }
                break;
            case ViewColumnEditor.SIGNED:
                if (!enableViewEditorCells(column, m.getPropertyID())) {
                    return null;
                } else if (column.isSigned() != null) {

                    return "";

                }
                break;
            case ViewColumnEditor.HEADERALIGNMENT:

                if (!enableViewEditorCells(column, m.getPropertyID())) {
                    return null;
                } else if (column.getHeaderAlignmentCode() != null) {

                    return column.getHeaderAlignmentCode().getDescription();
                }
                break;
            case ViewColumnEditor.DECIMALPLACES:
                if (!enableViewEditorCells(column, m.getPropertyID())) {
                    return null;
                } else if (column.getDecimals() != null) {
                    return column.getDecimals().toString();
                }
                break;
            case ViewColumnEditor.SCALINGFACTOR:
                if (!enableViewEditorCells(column, m.getPropertyID())) {
                    return null;
                } else if (column.getScaling() != null) {
                    return column.getScaling().toString();
                }
                break;

            case ViewColumnEditor.NUMERICMASK:
                if (!enableViewEditorCells(column, m.getPropertyID())) {
                    return null;
                } else if (column.getNumericMaskCode() != null) {
                    return column.getNumericMaskCode().getDescription();
                }
                break;
            case ViewColumnEditor.FORMATPHASECALCULATION:
                if (!enableViewEditorCells(column, m.getPropertyID())) {
                    return null;
                } else if (column.getFormatColumnCalculation() != null) {
                    return column.getFormatColumnCalculation().toString();
                }

                break;
            case ViewColumnEditor.RECORDAGGREGATIONFUNCTION:
                if (!enableViewEditorCells(column, m.getPropertyID())) {
                    return null;
                } else if (column.getRecordAggregationCode() != null) {

                    return column.getRecordAggregationCode().getDescription();
                }
                break;
            case ViewColumnEditor.GROUPAGGREGATIONFUNCTION:
                if (!enableViewEditorCells(column, m.getPropertyID())) {
                    if (view.getOutputFormat() != null) {
                        if (view.getOutputFormat() == OutputFormat.Format_Report) {
                            if ((column.getDataTypeCode() != null && 
                                 column.getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC) || 
                                column.isSortKey()) {
                                // display blank for data-type = Alnum or if
                                // column is a sort key
                                return null;
                            }
                            if (view.isFormatPhaseRecordAggregationOn() && 
                                (column.getRecordAggregationCode() == null ||
                                column.getRecordAggregationCode().getGeneralId() != Codes.SUM)) {
                                if (column.getGroupAggregationCode() != null) {
                                    return column.getGroupAggregationCode().getDescription();
                                }
                            }
                        }
                    } else {
                        // display blank for Flat-File
                        return null;
                    }
                } else if (column.getGroupAggregationCode() != null) {
                    return column.getGroupAggregationCode().getDescription();
                }
                break;
            default:
                if (m.getValue(colIndex) == null) {
                    return "";
                } else if (m.getValue(colIndex) instanceof Code) {
                    return ((Code) m.getValue(colIndex)).getDescription();
                } else {
                    return m.getValue(colIndex).toString();
                }
            }
            return "";
        }

        @Override
        public Image getImage(Object element) {
            ViewRow m = (ViewRow) element;
            switch (m.getPropertyID()) {
            case ViewColumnEditor.SORTKEY_HEADER:
                if ((Boolean) m.getValue(colIndex) == true) {
                    ViewSortKey key = view.getViewColumns().getActiveItems()
                            .get(colIndex).getViewSortKey();
                    if (key.getSortSequenceCode() != null
                            && key.getSortSequenceCode().getGeneralId() == Codes.ASCENDING) {
                        return UIUtilities
                                .getAndRegisterImage(ImageKeys.SKEYASC_IMAGE);
                    } else {
                        return UIUtilities
                                .getAndRegisterImage(ImageKeys.SKEYDESC_IMAGE);
                    }
                }
                break;
            case ViewColumnEditor.DATASOURCE:
                ViewSource source = (ViewSource) m.getData();
                // get the column source from current column and view
                // source.
                ViewColumnSource vsource = view.getViewColumns()
                        .getActiveItems().get(colIndex).getViewColumnSources()
                        .getActiveItems().get(source.getSequenceNo() - 1);
                if (vsource.getSourceType() != null) {
                    if (vsource.getSourceType().getGeneralId() == Codes.CONSTANT) {
                        return UIUtilities
                                .getAndRegisterImage(ImageKeys.CONSTANT);
                    } else if (vsource.getSourceType().getGeneralId() == Codes.FORMULA) {
                        return UIUtilities
                                .getAndRegisterImage(ImageKeys.FORMULA);
                    } else if (vsource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                        return UIUtilities
                                .getAndRegisterImage(ImageKeys.LOOKUPPATH);
                    } else if (vsource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                        return UIUtilities
                                .getAndRegisterImage(ImageKeys.SOURCE_FIELD);
                    }
                }
                break;
            case ViewColumnEditor.SIGNED:

                if (enableViewEditorCells(column, m.getPropertyID())) {
                    if (column.isSigned()) {
                        return UIUtilities
                                .getAndRegisterImage(ImageKeys.CHECKED);
                    } else {
                        return UIUtilities
                                .getAndRegisterImage(ImageKeys.UNCHECKED);
                    }
                } else if (column.getDataTypeCode() != null
                        && column.getDataTypeCode().getGeneralId() == Codes.EDITED_NUMERIC) {
                    return UIUtilities.getAndRegisterImage(ImageKeys.CHECKED);
                } else {
                    return null;
                }
            case ViewColumnEditor.VISIBLE:
                if (!enableViewEditorCells(column, m.getPropertyID())) {
                    if (column.isSortKey()) {
                        return UIUtilities.getAndRegisterImage(ImageKeys.CHECKED);
                    }
                } else {

                    if (column.isVisible()) {
                        return UIUtilities.getAndRegisterImage(ImageKeys.CHECKED);
                    } else {
                        return UIUtilities.getAndRegisterImage(ImageKeys.UNCHECKED);
                    }

                }
            default:
                return null;
            }
            return null;
        }

        @Override
        public Color getBackground(Object element) {
            ViewRow m = (ViewRow) element;
            if (m.getRowType() == ViewRowType.SORTKEY_HEADER) {
                // if a sort key is defined for a col, set its color to yellow,
                // else to grey.
                if ((Boolean) m.getValue(colIndex) == true) {
                    return Display.getCurrent()
                            .getSystemColor(SWT.COLOR_YELLOW);
                } else {
                    return UIUtilities.getColorForDisabledCell();
                }
            } else if (m.getRowType() == ViewRowType.DATA_SOURCE) {
                ViewSource source = (ViewSource) m.getData();
                ViewColumnSource colSrc = view.getViewColumns()
                    .getActiveItems().get(colIndex).getViewColumnSources()
                    .getActiveItems().get(source.getSequenceNo() - 1);
                if (colSrc.getSourceType() != null && 
                    colSrc.getSourceType().getGeneralId() == Codes.FORMULA) {
                    if (isWriteStatement(colSrc.getExtractColumnAssignment())) {
                        return getColorOrange();                        
                    } else if (colSrc.getExtractColumnAssignment() != null &&
                               !colSrc.getExtractColumnAssignment().trim().isEmpty()) {
                        return getColorPurple();                                            
                    } else {
                        return Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);                                            
                    }
                }  else {
                    return Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);                    
                }
            } else if (m.getRowEditorType() == RowEditorType.NONE) {
                // no editor means the cell is disabled. set its color to grey.
                return UIUtilities.getColorForDisabledCell();
            }

            if ((m.getRowEditorType() != RowEditorType.NONE)
                    && !enableViewEditorCells(column, m.getPropertyID())) {
                return UIUtilities.getColorForDisabledCell();
            } else if (m.getPropertyID() == FORMATPHASECALCULATION) {
                if (column.getFormatColumnCalculation() != null &&
                    !column.getFormatColumnCalculation().trim().isEmpty()) {
                    return getColorPurple();
                }
            }

            return super.getBackground(element);
        }

        protected boolean isWriteStatement(String logic) {
            if (logic == null) {
                return false;
            }
            String[] lines = logic.split("\\n");
            for (String line : lines) {
                line = removeComments(line);
                line = removeStrings(line);
                line = line.trim().toUpperCase();
                if (line.matches(".*WRITE\\s*\\(.*\\).*")) {
                    return true;
                }
            }
            return false;
        }

        private String removeComments(String input) {
            Pattern commentPattern = Pattern.compile("(.*?)(\\\\|').*");
            Matcher nonComment = commentPattern.matcher(input);
            if (nonComment.find()) {
                return nonComment.group(1);            
            } 
            else {
                return input;
            }
        }
        
        private String removeStrings(String input) {
            String removed = input;
            Pattern stringPattern = Pattern.compile("(.*)\".*?\"(.*)");
            Matcher remStr = stringPattern.matcher(removed);
            while (remStr.find()) {
                removed =  remStr.group(1) + remStr.group(2);     
                remStr = stringPattern.matcher(removed);
            } 
            return removed;
        }
        
    }
    
    // A class which has the properties as that of a View Column.
    private class ViewColumnCopy implements Comparable<ViewColumnCopy> {

        private Integer columnNo;
        private Code dataAlignmentCode;
        private Boolean visible;
        private Integer spacesBeforeColumn;
        private String sortkeyFooterLabel;

        private Code recordAggrCode;
        private Code groupAggrCode;
        private String formatColumnCalculation;

        private String name;
        private Code dataType;
        private Integer length;
        private Integer decimals;
        private Boolean signed;
        private Integer scaling;
        private Code dateTimeFormat;
        private Code headerAlignment;
        private String heading1;
        private String heading2;
        private String heading3;
        private Code numericMask;
        private String sortKeyLabel;
        private String defaultValue;
        private String subtotalLabel;
        private Boolean isSortKey;

        // Sort Key properties
        private Code sortSequenceCode;
        private Code footerOptionCode;
        private Code headerOptionCode;
        private Code displayModeCode;
        private Code sortKeyDataTypeCode;
        private Boolean sortKeySigned;
        private Integer sortKeyLength;
        private Integer sortKeyDecimalPlaces;
        private Code sortKeyDateTimeFormatCode;
        private LRField sortKeyTitleField;
        private Integer sortKeyTitleLength;

        List<ViewColumnSourceCopy> viewColumnSourcesCopy = new ArrayList<ViewColumnSourceCopy>();

        public ViewColumnCopy(ViewColumn viewColumn) throws SAFRException {

            this.columnNo = viewColumn.getColumnNo();
            this.dataAlignmentCode = viewColumn.getDataAlignmentCode();
            this.visible = viewColumn.isVisible();
            this.spacesBeforeColumn = viewColumn.getSpacesBeforeColumn();
            this.sortkeyFooterLabel = viewColumn.getSortkeyFooterLabel();
            this.recordAggrCode = viewColumn.getRecordAggregationCode();
            this.groupAggrCode = viewColumn.getGroupAggregationCode();
            this.formatColumnCalculation = viewColumn
                    .getFormatColumnCalculation();
            this.name = viewColumn.getName();
            this.dataType = viewColumn.getDataTypeCode();
            this.length = viewColumn.getLength();
            this.decimals = viewColumn.getDecimals();
            this.signed = viewColumn.isSigned();
            this.scaling = viewColumn.getScaling();
            this.dateTimeFormat = viewColumn.getDateTimeFormatCode();
            this.headerAlignment = viewColumn.getHeaderAlignmentCode();
            this.heading1 = viewColumn.getHeading1();
            this.heading2 = viewColumn.getHeading2();
            this.heading3 = viewColumn.getHeading3();
            this.numericMask = viewColumn.getNumericMaskCode();
            this.defaultValue = viewColumn.getDefaultValue();
            this.subtotalLabel = viewColumn.getSubtotalLabel();
            this.isSortKey = viewColumn.isSortKey();

            if (this.isSortKey) {
                ViewSortKey viewSortKey = viewColumn.getViewSortKey();
                this.sortSequenceCode = viewSortKey.getSortSequenceCode();
                this.footerOptionCode = viewSortKey.getFooterOptionCode();
                this.headerOptionCode = viewSortKey.getHeaderOptionCode();
                this.displayModeCode = viewSortKey.getDisplayModeCode();
                this.sortKeyDataTypeCode = viewSortKey.getDataTypeCode();
                this.sortKeyLength = viewSortKey.getLength();
                this.sortKeySigned = viewSortKey.isSigned();
                this.sortKeyDecimalPlaces = viewSortKey.getDecimalPlaces();
                this.sortKeyDateTimeFormatCode = viewSortKey
                        .getDateTimeFormatCode();
                this.sortKeyTitleField = viewSortKey.getTitleField();

                this.sortKeyTitleLength = viewSortKey.getTitleLength();
                this.sortKeyLabel = viewSortKey.getSortkeyLabel();
            }

            List<ViewColumnSource> viewColumnSources = viewColumn
                    .getViewColumnSources();
            for (ViewColumnSource viewColSource : viewColumnSources) {
                ViewColumnSourceCopy viewColSourceCopy = new ViewColumnSourceCopy(
                        viewColSource);
                this.viewColumnSourcesCopy.add(viewColSourceCopy);
            }

        }

        // This method sets values for a View Column passed to it with the
        // values of the object of ViewColumnCopy class
        public ViewColumn getViewColumnFromCopy(ViewColumn vwColumn)
                throws SAFRException {

            if (this.dataAlignmentCode != null) {
                vwColumn.setDataAlignmentCode(this.dataAlignmentCode);
            }
            vwColumn.setVisible(this.visible);
            vwColumn.setSpacesBeforeColumn(this.spacesBeforeColumn);
            vwColumn.setSortkeyFooterLabel(this.sortkeyFooterLabel);
            if (this.recordAggrCode != null) {
                vwColumn.setRecordAggregationCode(this.recordAggrCode);
            }
            if (this.groupAggrCode != null) {
                vwColumn.setGroupAggregationCode(this.groupAggrCode);
            }
            vwColumn.setFormatColumnCalculation(this.formatColumnCalculation);
            vwColumn.setName(this.name);
            if (this.dataType != null) {
                vwColumn.setDataTypeCode(this.dataType);
            }
            vwColumn.setLength(this.length);
            vwColumn.setDecimals(this.decimals);
            vwColumn.setSigned(this.signed);
            vwColumn.setScaling(this.scaling);
            if (this.dateTimeFormat != null) {
                vwColumn.setDateTimeFormatCode(this.dateTimeFormat);
            }
            vwColumn.setHeaderAlignmentCode(this.headerAlignment);
            vwColumn.setHeading1(this.heading1);
            vwColumn.setHeading2(this.heading2);
            vwColumn.setHeading3(this.heading3);
            if (this.numericMask != null) {
                vwColumn.setNumericMaskCode(this.numericMask);
            }
            vwColumn.setSortKeyLabel(this.sortKeyLabel);
            vwColumn.setDefaultValue(this.defaultValue);
            vwColumn.setSubtotalLabel(this.subtotalLabel);

            // Copy Sort Key properties
            if (this.isSortKey) {
                ViewSortKey viewSortKey = view.addSortKey(vwColumn);

                if (this.sortSequenceCode != null) {
                    viewSortKey.setSortSequenceCode(this.sortSequenceCode);
                }
                if (this.displayModeCode != null) {
                    viewSortKey.setDisplayModeCode(this.displayModeCode);
                }
                if (this.footerOptionCode != null) {
                    viewSortKey.setFooterOption(this.footerOptionCode);
                }
                if (this.headerOptionCode != null) {
                    viewSortKey.setHeaderOption(this.headerOptionCode);
                }
                if (this.sortKeyDataTypeCode != null) {
                    viewSortKey.setDataTypeCode(this.sortKeyDataTypeCode);
                }
                if (this.sortKeyLabel != null) {
                    viewSortKey.setSortkeyLabel(this.sortKeyLabel);
                }
                viewSortKey.setSigned(this.sortKeySigned);
                viewSortKey.setLength(this.sortKeyLength);
                viewSortKey.setDecimalPlaces(this.sortKeyDecimalPlaces);
                if (this.sortKeyDateTimeFormatCode != null) {
                    viewSortKey
                            .setDateTimeFormatCode(this.sortKeyDateTimeFormatCode);
                }
                viewSortKey.setTitleField(this.sortKeyTitleField);
                viewSortKey.setTitleLength(this.sortKeyTitleLength);
            }

            // copy View Column sources.
            int i = 0;
            for (ViewColumnSourceCopy vwColSourceCopy : this.viewColumnSourcesCopy) {
                vwColSourceCopy.getViewColumnSourceFromCopy(
                    vwColumn.getViewColumnSources().get(i));
                i++;
            }

            return vwColumn;
        }

        public int compareTo(ViewColumnCopy right) {
            if (this.columnNo < right.columnNo) {
                return -1;
            }
            else if (this.columnNo > right.columnNo) {
                return 1;                
            }
            else {
                return 0;
            }
        }
    }

    // A class which has the same properites as that of a View Column Source.
    private class ViewColumnSourceCopy {

        private Code sourceType;
        private String sourceValue;
        private LRField sourceLRField;
        private String extractColumnAssignment;
        private LRField sortKeyTitleLRField;
        private LookupQueryBean sortKeyTitleLookupPathQueryBean;
        private LogicalRecordQueryBean sortKeyTitleLogicalRecordQueryBean;
        private Code effectiveDateTypeCode;
        private String effectiveDateValue;
        private LRField effectiveDateLRField;
        private LRField lookupSourceField;
        private LookupQueryBean columnSourceLookupQueryBean;

        public ViewColumnSourceCopy(ViewColumnSource viewColumnSource)
                throws SAFRException {

            this.sourceType = viewColumnSource.getSourceType();

            this.sourceValue = viewColumnSource.getSourceValue();

            this.sourceLRField = viewColumnSource.getLRField();
            this.lookupSourceField = viewColumnSource.getLRField();

            this.columnSourceLookupQueryBean = viewColumnSource
                    .getLookupQueryBean();
            this.extractColumnAssignment = viewColumnSource
                    .getExtractColumnAssignment();
            this.sortKeyTitleLRField = viewColumnSource
                    .getSortKeyTitleLRField();
            this.sortKeyTitleLogicalRecordQueryBean = viewColumnSource
                    .getSortKeyTitleLogicalRecordQueryBean();
            this.sortKeyTitleLookupPathQueryBean = viewColumnSource
                    .getSortKeyTitleLookupPathQueryBean();
            this.effectiveDateTypeCode = viewColumnSource
                    .getEffectiveDateTypeCode();
            this.effectiveDateValue = viewColumnSource.getEffectiveDateValue();
            this.effectiveDateLRField = viewColumnSource
                    .getEffectiveDateLRField();

        }

        // This method sets the values for a ViewColumnSource object with the
        // values which are contained in the object of ViewColumnSourceCopy
        // class.
        public ViewColumnSource getViewColumnSourceFromCopy(
                ViewColumnSource viewColSource) throws SAFRException {

            if (this.sourceType != null) {
                viewColSource.setSourceType(this.sourceType);
            }
            viewColSource.setSourceValue(this.sourceValue);

            if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                viewColSource.setLRFieldPaste(this.sourceLRField);                
            } else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
                viewColSource.setLRFieldPaste(this.lookupSourceField);                
            }
            viewColSource.setLookupQueryBean(this.columnSourceLookupQueryBean);
            viewColSource.setExtractColumnAssignment(this.extractColumnAssignment);
            viewColSource.setSortKeyTitleLRField(this.sortKeyTitleLRField);
            viewColSource.setSortKeyTitleLogicalRecordQueryBean(this.sortKeyTitleLogicalRecordQueryBean);
            viewColSource.setSortKeyTitleLookupPathQueryBean(this.sortKeyTitleLookupPathQueryBean);
            if (this.effectiveDateTypeCode != null) {
                viewColSource.setEffectiveDateTypeCode(this.effectiveDateTypeCode);
            }
            viewColSource.setEffectiveDateValue(this.effectiveDateValue);
            viewColSource.setEffectiveDateLRField(this.effectiveDateLRField);
            return viewColSource;

        }
    }
        
    // member variables
    private FormToolkit toolkit;
    private ViewMediator mediator;
    private Composite parent;
    private View view;
    
    protected Composite composite;
    private final List<ViewRow> viewEditorRows = new ArrayList<ViewRow>();
    private GridTableViewer viewEditorGrid;
    private CellEditor currentCellEditor;    
    private ViewSortKey currentSortKey;
    private ViewSource currentViewSource;
    private ViewColumnSource currentViewColumnSource;
    private ViewRow currentHeaderRow = null;
    private Point fromLoc = null;
    private PropertyViewType fromViewType = null;    
    private Cursor magnifyCursor = null;    
    private List<ViewColumnCopy> copiedViewColumnsList = new ArrayList<ViewColumnCopy>();
    private Font boldFont = null;
    
    // Property IDs
    // Heading
    public static final int COLUMN_HEADING_HEADER = 0;
    public static final int HEAD1 = 1;
    public static final int HEAD2 = 2;
    public static final int HEAD3 = 3;

    // Sort keys
    public static final int SORTKEY_HEADER = 4;
    public static final int SORTKEY = -1; // dynamic
    
    // Column output properties
    public static final int COLUMN_PROP_HEADER = 5;
    public static final int STARTPOS = 6;    
    public static final int DATATYPE = 7;
    public static final int DATETIMEFORMAT = 8;
    public static final int LENGTH = 9;
    public static final int DATAALIGNMENT = 10;
    public static final int VISIBLE = 11;
    public static final int SPACEBEFORECOLUMN = 12;
    public static final int HEADERALIGNMENT = 13;
    public static final int DECIMALPLACES = 14;
    public static final int SCALINGFACTOR = 15;
    public static final int SIGNED = 16;
    public static final int NUMERICMASK = 17;
    public static final int FORMATPHASECALCULATION = 18;
    public static final int RECORDAGGREGATIONFUNCTION = 19;
    public static final int GROUPAGGREGATIONFUNCTION = 20;

    // Data source
    public static final int DATA_SOURCE_HEADER = 21;
    public static final int DATASOURCE = -2; // dynamic
    
    // End property IDs
    
    protected ViewColumnEditor(ViewMediator mediator, Composite parent, View view) {
        this.mediator = mediator;
        this.parent = parent;
        this.view = view;
        magnifyCursor = new Cursor(Display.getCurrent(), UIUtilities
                .getAndRegisterImage(ImageKeys.MAGNIFY).getImageData(), 0, 0);
    }

    protected Color getColorOrange() {
        if (orange == null) {
            orange = new Color(Display.getCurrent(), 255, 155, 0);            
        }
        return orange;
    }

    protected Color getColorPurple() {
        if (purple == null) {
            purple = new Color(Display.getCurrent(), 230, 140, 255);            
        }
        return purple;
    }
    
    protected void create() {

        toolkit = new FormToolkit(parent.getDisplay());
        composite = toolkit.createComposite(parent, SWT.NONE);
        FormLayout layoutComposite = new FormLayout();
        composite.setLayout(layoutComposite);

        FormData dataComposite = new FormData();
        dataComposite.left = new FormAttachment(0, 0);
        dataComposite.right = new FormAttachment(100, 0);
        dataComposite.top = new FormAttachment(0, 0);
        dataComposite.bottom = new FormAttachment(100, 0);
        composite.setLayoutData(dataComposite);
        // add basic rows
        viewEditorRows.clear();
        for (int row : rowsToInclude) {
            ViewRow m = new ViewRow(row, null);
            viewEditorRows.add(m);
        }
        // add sort keys rows. These are invisible by default.
        int keyCounter = 0;
        for (ViewSortKey key : view.getViewSortKeys().getActiveItems()) {
            ViewRow m = new ViewRow(ViewColumnEditor.SORTKEY, key);
            m.setVisible(false);
            viewEditorRows.add(ViewColumnEditor.SORTKEY_HEADER + 1 + keyCounter++, m);
        }
        for (ViewSource source : view.getViewSources().getActiveItems()) {
            ViewRow m = new ViewRow(ViewColumnEditor.DATASOURCE, source);
            // viewEditorRows.add(ViewEditor.DATA_SOURCE_HEADER + 1 + getSortKeyCount(),
            // m);
            // View source rows are always at the end. No need to specify an
            // index.
            viewEditorRows.add(m);
        }

        createGrid();
    }

    protected void createGrid() {
        viewEditorGrid = new GridTableViewer(composite, SWT.V_SCROLL | SWT.H_SCROLL);
        toolkit.adapt(viewEditorGrid.getGrid(), true, true);

        viewEditorGrid.getGrid().setData(SAFRLogger.USER, "View Grid");
        viewEditorGrid.getGrid().setLayout(new FormLayout());
        FormData dataGrid = new FormData();
        dataGrid.top = new FormAttachment(0, 0);
        dataGrid.left = new FormAttachment(0, 0);
        dataGrid.right = new FormAttachment(100, 0);
        dataGrid.bottom = new FormAttachment(100, 0);
        dataGrid.width = 0;
        dataGrid.height = 0;
        viewEditorGrid.getGrid().setLayoutData(dataGrid);

        viewEditorGrid.getGrid().setLinesVisible(true);
        viewEditorGrid.getGrid().setHeaderVisible(true);
        viewEditorGrid.setContentProvider(new ViewEditorContentProvider());
        viewEditorGrid.getGrid().setRowHeaderVisible(true);
        viewEditorGrid.getGrid().setItemHeaderWidth(MIN_ROW_HEADER_WIDTH);
        viewEditorGrid.getGrid().setCellSelectionEnabled(true);

        viewEditorGrid.getGrid().addTraverseListener(new TraverseListener() {

            private void editNeighborCell(TraverseEvent e, Point pt, List<ViewRow> visibleRows, boolean up) {
                GridViewerEditor editor = (GridViewerEditor) viewEditorGrid.getColumnViewerEditor();
                GridItem item = viewEditorGrid.getGrid().getItem(pt.y);
                ViewerRow row = new GridViewerRow(item);
                ViewerCell cell = editor.searchCellAboveBelow(editor, row, editor.getGridViewer(), pt.x, up);
                if (cell == null) {
                    return;
                }
                boolean found = false;
                GridItem items[] = viewEditorGrid.getGrid().getItems();
                int rowIdx = 0;
                for (; rowIdx < items.length; rowIdx++) {
                    if (((GridItem) cell.getViewerRow().getItem()) == items[rowIdx]) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    viewEditorGrid.editElement(visibleRows.get(rowIdx), cell.getColumnIndex());
                }
            }

            public void keyTraversed(TraverseEvent e) {
                e.doit = false;
                if (viewEditorGrid.getGrid().getItemCount() == 0
                        || viewEditorGrid.getGrid().getCellSelectionCount() == 0) {
                    return;
                }
                Point pt = viewEditorGrid.getGrid().getCellSelection()[0];
                List<ViewRow> visibleRows = new ArrayList<ViewRow>();
                for (ViewRow row : viewEditorRows) {
                    if (row.isVisible()) {
                        visibleRows.add(row);
                    }
                }
                if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
                    if (pt != null) {
                        editNeighborCell(e, pt, visibleRows, false);
                    }
                } else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                    if (pt != null) {
                        editNeighborCell(e, pt, visibleRows, true);
                    }
                }
            }
        });

        mediator.getSite().setSelectionProvider(viewEditorGrid);

        /* Jaydeep March 9, 2010 CQ 7615 : Added Source provider Implementation. */
        viewEditorGrid.getGrid().addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                // check if any part of a view source is selected
                Grid g = (Grid) e.getSource();
                GridItem item = g.getFocusItem();
                ViewRow row = (ViewRow)item.getData();
                if (row.propertyID == ViewColumnEditor.DATASOURCE) {
                    ViewEditorInput viewEditorInput = mediator.getEditorInput();
                    if (viewEditorInput.getEditRights() == EditRights.Read) {
                        UIUtilities.getSourceProvider().setAllowCopyViewSource(false);
                    }
                    else {
                        UIUtilities.getSourceProvider().setAllowCopyViewSource(true);                        
                    }
                }
                else {
                    if (view.getViewSources().getActiveItems().size() == 1) {
                        UIUtilities.getSourceProvider().setAllowCopyViewSource(true);                                            
                    } else {
                        UIUtilities.getSourceProvider().setAllowCopyViewSource(false);
                    }
                }
                mediator.refreshToolbar();
            }
        });

        viewEditorGrid.getGrid().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == ' ') { // space
                    mediator.getSite().getPage().activate(mediator.getEditor());
                    Grid g = (Grid) e.getSource();
                    int col = viewEditorGrid.getGrid().getFocusCell().x;
                    IViewPart part = openCellRelatedView(g.getFocusItem(), col, true, true);
                    if (part != null) {
                        part.setFocus();
                        if (part instanceof ColumnSourceView) {
                            ((ColumnSourceView) part).editFirstCell();
                        } else if (part instanceof SortKeyView) {
                            ((SortKeyView) part).editFirstCell();
                        }
                    }
                }
            }

            public void keyReleased(KeyEvent e) {
            }
        });

        viewEditorGrid.getGrid().addMouseListener(new MouseAdapter() {

			boolean doubleClick = false;

            @Override
			public void mouseDown(MouseEvent e) {
				Point pt = new Point(e.x, e.y);
			    GridItem item = viewEditorGrid.getGrid().getItem(pt);
			    GridColumn column = viewEditorGrid.getGrid().getColumn(pt);
			    if (item == null) {
			    	if (column == null) {
			    		viewEditorGrid.getGrid().setData(SAFRLogger.USER, "View Grid");
			    	}
			    	else {
				    	Integer colIdx = viewEditorGrid.getGrid().indexOf(column)+1;
			    		viewEditorGrid.getGrid().setData(SAFRLogger.USER, "View Grid (" + colIdx + ")");			    						    	
			    	}
			    }
			    else {
			    	Integer rowIdx = viewEditorGrid.getGrid().indexOf(item);
			    	int i=0;
			    	ViewRow row = null;
		            for (ViewRow rowIt : viewEditorRows) {
		                if (rowIt.isVisible()) {
		                	i++;
		                	if (i > rowIdx) {
		                		row = rowIt;
		                		break;
		                	}
		                }	
		            }
			    	if (column == null) {				    	
			    		viewEditorGrid.getGrid().setData(SAFRLogger.USER, "View Grid (" + getRowHeaderText(row).trim() + ")");
			    	}
			    	else {
				    	Integer colIdx = viewEditorGrid.getGrid().indexOf(column)+1;
			    		viewEditorGrid.getGrid().setData(SAFRLogger.USER, "View Grid (" + getRowHeaderText(row).trim() +"," + colIdx+ ")");
			    	}
			    }            	
			}

			
            @Override
            public void mouseUp(MouseEvent e) {
                if (doubleClick) {
                    // This method will be called twice on a double click
                    // as it has 2 mouse up events. Ignore the 2nd one.
                    doubleClick = false;
                    return;
                }
                Grid g = (Grid) e.getSource();
                Point toLoc = g.getCell(new Point(e.x, e.y));
                GridItem toItem = g.getItem(new Point(e.x, e.y));

                // Determine visible rows
                List<ViewRow> visibleRows = new ArrayList<ViewRow>();
                for (ViewRow row : viewEditorRows) {
                    if (row.isVisible()) {
                        visibleRows.add(row);
                    }
                }

                // Identify columns with a combo box
                Set<Integer> comboRows = new HashSet<Integer>();
                for (int i = 0; i < visibleRows.size(); i++) {
                    if (visibleRows.get(i).getRowEditorType() == RowEditorType.COMBO) {
                        comboRows.add(i);
                    }
                }

                if (e.x > g.getItemHeaderWidth()) {
                    // deal with click on grid
                    if (toLoc != null) {
                        // Set selection and focus for cases where from combo
                        // cell
                        if (fromLoc != null && !toLoc.equals(fromLoc) && comboRows.contains(fromLoc.y)) {
                            g.setCellSelection(toLoc);
                            g.setFocusColumn(g.getColumn(toLoc.x));
                            g.setFocusItem(toItem);
                        }
                        openCellRelatedView(toItem, toLoc.x, true, e.button == 1);
                        
                        // edit cell when coming from combo cell
                        if (fromLoc != null && !toLoc.equals(fromLoc) && comboRows.contains(fromLoc.y)) {
                            viewEditorGrid.editElement(visibleRows.get(toLoc.y), toLoc.x);
                        }
                    } else {
                        if (currentCellEditor != null) {
                            // clicked outside visible rows/cols
                            SAFRComboBoxCellEditor combo = (SAFRComboBoxCellEditor) currentCellEditor;
                            combo.focusLost();
                        }
    		            mediator.closePropertyView(PropertyViewType.NONE);
                    }
                } else if (e.x <= g.getItemHeaderWidth() && toLoc == null) {
                    
                    // if mouse button is right select first column cell
                    if (e.button == 3 && viewEditorGrid.getGrid().getColumnCount() > 0 && toItem != null) {
                        viewEditorGrid.getGrid().setFocusItem(toItem);
                        viewEditorGrid.getGrid().setFocusColumn(viewEditorGrid.getGrid().getColumn(0));
                        viewEditorGrid.getGrid().setCellSelection(new Point(0,viewEditorGrid.getGrid().indexOf(toItem)));
                        UIUtilities.getSourceProvider().setAllowCopyViewSource(true);
                    }
                    
                    IViewPart view = null;
                    view = (IViewPart) openCellRelatedView(toItem, -1, false, e.button == 1);
                    if (view != null && view instanceof DataSourceView) {
                        // view source row header clicked
                        ((DataSourceView) view).selectFirstCell();
                    } else {
                        // non-view source row header clicked
                        if (currentCellEditor != null && fromLoc != null && comboRows.contains(fromLoc.y)) {
                            SAFRComboBoxCellEditor combo = (SAFRComboBoxCellEditor) currentCellEditor;
                            combo.focusLost();
                            if (toItem != null) {
                                g.setFocusItem(toItem);
                            }
                        }
                    }
                }
                fromLoc = toLoc;
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                doubleClick = true;
                Grid g = (Grid) e.getSource();
                if (e.x < g.getItemHeaderWidth()) {
                    // only if clicked on row headers.
                    GridItem item = g.getItem(new Point(e.x, e.y));
                    if (item != null) {
                        ViewRow m = (ViewRow) item.getData();
                        if (m.isExpandable()) {
                            if (m.isExpanded()) {
                                // collapse
                                expandCollapseRows(m, false);
                                m.setExpanded(false);
                                
                                refreshGrid();
                            } else {
                                // expand
                                expandCollapseRows(m, true);
                                m.setExpanded(true);
                                refreshGrid();
                            }
                            return;
                        }
                        // check if the click is on data source cell.
                        if (m.propertyID == ViewColumnEditor.DATASOURCE) {
                            ViewSource source = (ViewSource) m.getData();

                            // the click is on datasource cell so open extract record filter.
                            // load extract record filter using Source.
                            LogicTextEditorInput input = new LogicTextEditorInput(source, mediator.getEditor(),
                                LogicTextType.Extract_Record_Filter);
                            try {

                                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                        .openEditor(input, LogicTextEditor.ID);
                            } catch (PartInitException pie) {
                                UIUtilities.handleWEExceptions(pie,
                                        "Unexpected error occurred while opening logic text editor.", null);
                            }

                        }
                    }
                } else {

                    // if not clicked on row headers.
                    GridItem item = g.getItem(new Point(e.x, e.y));
                    if (item != null) {
                        ViewRow m = (ViewRow) item.getData();
                        if (getCurrentColIndex() < 0) {
                            return;
                        }
                        // check if the mouse click is on data source row.
                        if (m.propertyID == ViewColumnEditor.DATASOURCE) {
                            ViewSource source = (ViewSource) m.getData();

                            // get Column source using sequence no of source.
                            // column sequence no= source sequence no-1
                            ViewColumnSource colSource = getCurrentColumn().getViewColumnSources().get(
                                    source.getSequenceNo() - 1);

                            // if the column source has type formula then load
                            // extract column assignment.
                            if (colSource.getSourceType() != null) {
                                if (colSource.getSourceType().getGeneralId() == Codes.FORMULA) {

                                    LogicTextEditorInput input = new LogicTextEditorInput(colSource, mediator.getEditor());
                                    try {

                                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                                .openEditor(input, LogicTextEditor.ID);
                                    } catch (PartInitException pie) {
                                        UIUtilities.handleWEExceptions(pie,
                                                "Unexpected error occurred while opening Logic Text editor.", null);
                                    }
                                }
                            }
                        } else if (m.propertyID == ViewColumnEditor.FORMATPHASECALCULATION
                                && enableViewEditorCells(getCurrentColumn(), ViewColumnEditor.FORMATPHASECALCULATION)) {
                            // Open the Logic Text editor tab
                            LogicTextEditorInput input = new LogicTextEditorInput(getCurrentColumn(), mediator.getEditor());
                            try {

                                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                        .openEditor(input, LogicTextEditor.ID);
                            } catch (PartInitException pie) {
                                UIUtilities.handleWEExceptions(pie,
                                        "Unexpected error occurred while opening Logic Text editor.", null);
                            }
                        }
                    }
                }
            }
        });

        viewEditorGrid.getGrid().addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
                Grid g = (Grid) e.getSource();
                Point point = new Point(e.x, e.y);
                GridItem item = g.getItem(point);

                if (item == null) {
                    g.setCursor(null);
                    return;
                }
                ViewRow m = (ViewRow) item.getData();
                if (m == null) {
                    g.setCursor(null);
                    return;
                }
                switch (m.getPropertyID()) {
                case ViewColumnEditor.DATASOURCE:
                    // if the mouse is on row headers or if a column exists
                    // under the mouse
                    if (e.x < g.getItemHeaderWidth() || g.getColumn(point) != null) {
                        g.setCursor(magnifyCursor);

                    } else {
                        g.setCursor(null);

                    }
                    break;
                case ViewColumnEditor.SORTKEY:
                    if (e.x < g.getItemHeaderWidth()) {
                        // if on row headers
                        g.setCursor(magnifyCursor);
                    } else {
                        g.setCursor(null);
                    }
                    break;
                case ViewColumnEditor.SORTKEY_HEADER:
                    // if mouse is not on row headers and a column exists
                    // under
                    // the mouse.
                    if (e.x > g.getItemHeaderWidth() && g.getColumn(point) != null) {
                        // if not on row headers
                        ViewColumn col = (ViewColumn) g.getColumn(point).getData(VALUE);
                        if (col.isSortKey()) {
                            g.setCursor(magnifyCursor);
                        } else {
                            g.setCursor(null);
                        }

                    } else {
                        g.setCursor(null);
                    }
                    break;
                default:
                    g.setCursor(null);
                    break;
                }
            }
        });

        viewEditorGrid.setRowHeaderLabelProvider(new ColumnLabelProvider() {
                        
            
            @Override
            public Font getFont(Object element) {
                ViewRow m = (ViewRow) element;
                if (m.getPropertyID() == SORTKEY_HEADER || 
                    m.getPropertyID() == COLUMN_PROP_HEADER ||
                    m.getPropertyID() == DATA_SOURCE_HEADER ||
                    m.getPropertyID() == COLUMN_HEADING_HEADER) {
                    return boldFont;
                }
                else {
                    return null;
                }
            }
            
            @Override
            public String getText(Object element) {

                ViewRow m = (ViewRow) element;
                return getRowHeaderText(m);
            }

            @Override
            public Image getImage(Object element) {
                ViewRow m = (ViewRow) element;
                if (m.isExpandable()) {
                    if (m.getPropertyID() == ViewColumnEditor.SORTKEY_HEADER && getSortKeyCount() <= 0) {
                        // no sort keys available yet, not need to show + -
                        // sign.
                        return null;
                    }
                    if (m.isExpanded()) {
                        return UIUtilities.getAndRegisterImage(ImageKeys.MINUS_IMAGE);
                    } else {
                        return UIUtilities.getAndRegisterImage(ImageKeys.PLUS_IMAGE);
                    }
                } else if (m.getPropertyID() == ViewColumnEditor.SORTKEY) {
                    if (((ViewSortKey) m.getData()).getSortSequenceCode() != null
                            && ((ViewSortKey) m.getData()).getSortSequenceCode().getGeneralId() == Codes.ASCENDING) {
                        return UIUtilities.getAndRegisterImage(ImageKeys.SKEYASC_IMAGE);
                    } else {
                        return UIUtilities.getAndRegisterImage(ImageKeys.SKEYDESC_IMAGE);
                    }
                }
                return null;
            }

            @Override
            public Color getBackground(Object element) {
                ViewRow m = (ViewRow) element;
                switch (m.getPropertyID()) {
                case ViewColumnEditor.SORTKEY:
                    return Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
                case ViewColumnEditor.DATASOURCE:
                    return Display.getCurrent().getSystemColor(SWT.COLOR_CYAN);
                default:
                    return Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
                }
            }
        });

        int i = 0;
        for (ViewColumn viewColumn : view.getViewColumns().getActiveItems()) {
            GridViewerColumn column = new GridViewerColumn(viewEditorGrid, SWT.NONE);
            ViewColumnEditingSupport editingSupp = new ViewColumnEditingSupport(viewEditorGrid, i, this, viewColumn, mediator);
            column.setEditingSupport(editingSupp);

            LabelProviderImpl labelProvider = new LabelProviderImpl(i, viewColumn);
            column.setLabelProvider(labelProvider);

            column.getColumn().setData(COL_ID, i + 1);
            column.getColumn().setData(VALUE, viewColumn);
            column.getColumn().setData(LABEL_PROVIDER, labelProvider);
            column.getColumn().setData(EDITING_SUPPORT, editingSupp);

            column.getColumn().setText("Column " + (i + 1));
            column.getColumn().setHeaderTooltip("Column " + (i + 1));
            column.getColumn().setWidth(VIEW_COL_WIDTH);
            // column.getColumn().setMoveable(true);

            // Add handler to select whole column when column header is selected
            column.getColumn().addSelectionListener(new SelectionListener() {

                public void widgetSelected(SelectionEvent e) {
                    GridColumn col = (GridColumn) e.getSource();
                    int idx = 0;
                    for (GridColumn lcol : viewEditorGrid.getGrid().getColumns()) {
                        if (lcol == col) {
                            break;
                        }
                        idx++;
                    }
                    viewEditorGrid.getGrid().setSelection(new GridItem[0]);
                    viewEditorGrid.getGrid().selectColumn(idx);
                    viewEditorGrid.getGrid().setFocusItem(viewEditorGrid.getGrid().getItem(0));
                    viewEditorGrid.getGrid().setFocusColumn(col);
                    mediator.refreshToolbar();
                }

                public void widgetDefaultSelected(SelectionEvent e) {
                }

            });

            i++;
        }
       
        // provide keyboard activation support. the editor will be activated
        // on 'Spacebar' from keyboard and on left mouse button double click.
        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(viewEditorGrid) {
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                        || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.character == ' '))
                        || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
                        || (event.sourceEvent instanceof MouseEvent) && ((MouseEvent) event.sourceEvent).button == 1;
            }
        };

        GridViewerEditor.create(viewEditorGrid, actSupport, ColumnViewerEditor.KEYBOARD_ACTIVATION
                | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                | GridViewerEditor.SELECTION_FOLLOWS_EDITOR);

        Display display = Display.getCurrent();
        FontDescriptor fontDescriptor = FontDescriptor.createFrom(viewEditorGrid.getGrid().getFont());
        boldFont = fontDescriptor.setStyle(SWT.BOLD).createFont(display);
        
        // Code for tracking the focus on the grid
        IFocusService service = (IFocusService) mediator.getSite().getService(IFocusService.class);
        service.addFocusTracker(viewEditorGrid.getGrid(), "ViewGrid");

        viewEditorGrid.setInput(view.getViewColumns().getActiveItems());
        viewEditorGrid.getGrid().setItemHeight(EDITOR_ROW_HEIGHT);
        viewEditorGrid.getGrid().pack();

        // Code for Context menu
        // First we create a menu Manager
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(viewEditorGrid.getGrid());
        // Set the MenuManager
        viewEditorGrid.getGrid().setMenu(menu);
        mediator.getSite().registerContextMenu(menuManager, viewEditorGrid);
        
        UIUtilities.getSourceProvider().setAllowCopyViewSource(false);   
    }

    private String getRowHeaderText(ViewRow m) {

        String propertyName;
        if (m.getPropertyID() == ViewColumnEditor.SORTKEY) {
            return "#" + ((ViewSortKey) m.getData()).getKeySequenceNo() + " "
                    + ((ViewSortKey) m.getData()).getName();
        }
        if (m.getPropertyID() == ViewColumnEditor.DATASOURCE) {
            ViewSource source = (ViewSource) m.getData();
            try {
                String lr = UIUtilities.getComboString(source.getLrFileAssociation()
                        .getAssociatingComponentName(), source.getLrFileAssociation()
                        .getAssociatingComponentId());
                String lf = UIUtilities.getComboString(source.getLrFileAssociation()
                        .getAssociatedComponentName(), source.getLrFileAssociation()
                        .getAssociatedComponentIdNum());
                String rowHeaderString = "       " + lr + "." + lf;
                // code to set row header width.
                int previousWidth;
                int currentWidth;
                GC gc = new GC(Display.getCurrent());
                currentWidth = (gc.getFontMetrics().getAverageCharWidth()) * (rowHeaderString.length());
                gc.dispose();
                previousWidth = viewEditorGrid.getGrid().getItemHeaderWidth();
                if (previousWidth < currentWidth) {
                    setRowHeaderWidth(currentWidth);

                }
                return rowHeaderString;
            } catch (SAFRException se) {
                UIUtilities.handleWEExceptions(se);
                return "";
            }
        }
        if (m.isExpandable()) {
            propertyName = m.getPropertyName();
        } else {
            propertyName = m.getPropertyName();
            propertyName = "       " + propertyName;
        }
        return propertyName;
    }
    
    public void refreshGrid() {
        viewEditorGrid.refresh();
        // this is required to keep row height in control.
        viewEditorGrid.getGrid().setItemHeight(EDITOR_ROW_HEIGHT);
    }
    
    protected int getCurrentColIndex() {
        int index = -1;

        try {
            index = viewEditorGrid.getGrid().getFocusCell().x;
        } catch (SWTException e) {
            // if there is SWT exception then this method will return index as
            // -1;
            // swt exception may occur due to call to get FocusCell after
            // dispose of column.
            return index;
        }
        if (index >= 0) {
            // get the actual column number
            index = (Integer) viewEditorGrid.getGrid().getColumn(index).getData(COL_ID);
        }

        return index;
    }
    
    protected ViewColumn getCurrentColumn() {
        int index = viewEditorGrid.getGrid().getFocusCell().x;
        ViewColumn col = (ViewColumn) viewEditorGrid.getGrid().getColumn(index)
                .getData(VALUE);
        return col;
    }
    
    /**
     * Checks if the selected cell is a Sort Key.
     * 
     * @return true if sort key cell is selected.
     */
    protected boolean isCurrentSortKey() {
        if (mediator.isViewPropVisible()) {
            return false;
        }
        currentSortKey = null;
        IStructuredSelection selection = (IStructuredSelection) viewEditorGrid
                .getSelection();
        if (selection != null) {
            ViewRow row = (ViewRow) selection.getFirstElement();
            if (row != null) {
                if (row.getPropertyID() == ViewColumnEditor.SORTKEY_HEADER) {
                    // check if the selected column has a sortkey
                    currentSortKey = getCurrentColumn().getViewSortKey();
                } else if (row.getPropertyID() == ViewColumnEditor.SORTKEY) {
                    // SORTKEY row always has a sort key assigned to it.
                    currentSortKey = (ViewSortKey) row.getData();
                }
            }
        }
        return currentSortKey != null;
    }
    
    /**
     * Returns the currently selected sort key.
     * 
     * @return current sort key or null if no sort key is selected.
     */
    protected ViewSortKey getCurrentSortKey() {
        isCurrentSortKey(); // will set the sort key variable.
        return currentSortKey;
    }
    
    /**
     * Checks if the selected cell is a View Source.
     * 
     * @return true if view source cell is selected.
     */
    protected boolean isCurrentViewSource() {
        if (mediator.isViewPropVisible()) {
            return false;
        }
        currentViewSource = null;
        IStructuredSelection selection = (IStructuredSelection) viewEditorGrid
                .getSelection();
        if (selection != null) {
            ViewRow row = (ViewRow) selection.getFirstElement();
            if (row != null) {
                if (row.getPropertyID() == ViewColumnEditor.DATASOURCE) {
                    // Datasource row always has a View source assigned to it.
                    currentViewSource = (ViewSource) row.getData();
                }
            } else {
                // if no columns are present, than we won't get a 'row'
                // information, in this case use 'currentHeaderRow' variable to
                // check which row was clicked.
                if (currentHeaderRow != null) {
                    if (currentHeaderRow.getData() instanceof ViewSource) {
                        currentViewSource = (ViewSource) currentHeaderRow
                                .getData();
                    }
                }
            }
        }
        return currentViewSource != null;
    }

    /**
     * Returns the currently selected view source.
     * 
     * @return current view source or null if no view source is selected.
     */
    protected ViewSource getCurrentViewSource() {
        isCurrentViewSource();
        return currentViewSource;
    }
    
    /**
     * Checks if the selected cell is a View Column Source.
     * 
     * @return true if View Column Source cell is selected.
     */
    protected boolean isCurrentViewColumnSource() {
        if (mediator.isViewPropVisible()) {
            return false;
        }
        currentViewColumnSource = null;
        IStructuredSelection selection = (IStructuredSelection) viewEditorGrid
                .getSelection();

        if (selection != null) {
            ViewRow row = (ViewRow) selection.getFirstElement();
            if (row != null) {
                if (row.getPropertyID() == ViewColumnEditor.DATASOURCE) {
                    int index = viewEditorGrid.getGrid().getFocusCell().x;
                    if (index > -1) {
                        // not in row header
                        ViewSource source = (ViewSource) row.getData();
                        currentViewColumnSource = getCurrentColumn()
                                .getViewColumnSources().get(
                                        source.getSequenceNo() - 1);
                    }
                }
            }
        }

        return currentViewColumnSource != null;
    }

    /**
     * Returns the currently selected View Column Source.
     * 
     * @return current View Column Source or null if no View Column Source is
     *         selected.
     */
    protected ViewColumnSource getCurrentViewColumnSource() {
        isCurrentViewColumnSource();
        return currentViewColumnSource;
    }
 
    protected void resetColumnHeaders() {
        List<ViewColumn> activeCols = view.getViewColumns().getActiveItems();
        for (int i = 0; i < viewEditorGrid.getGrid().getColumnCount(); i++) {
            viewEditorGrid.getGrid().getColumn(i).setText("Column " + (i + 1));
            viewEditorGrid.getGrid().getColumn(i).setData(COL_ID, i + 1);
            viewEditorGrid.getGrid().getColumn(i).setData(VALUE, activeCols.get(i));
            // reset column number in label provider and content provider of
            // this column
            ((LabelProviderImpl) viewEditorGrid.getGrid().getColumn(i).getData(
                    LABEL_PROVIDER)).setColumnIndex(i);
            ((ViewColumnEditingSupport) viewEditorGrid.getGrid().getColumn(i).getData(
                    EDITING_SUPPORT)).setColIndex(i);
        }
        viewEditorGrid.refresh();       
    }
    
    protected void copyViewColumns() {
        try {
            ApplicationMediator.getAppMediator().waitCursor();
            
            // get selected points in grid where selection is done.
            Point[] p = viewEditorGrid.getGrid().getCellSelection();

            if (p.length > 0) {
                // clear previously copied contents if any.
                copiedViewColumnsList.clear();
                UIUtilities.getSourceProvider().setPasteViewColumnAllowed(true);
            }

            // add the selected columns to be copied into a map
            Map<Integer,ViewColumnCopy> copiedSet = new HashMap<Integer,ViewColumnCopy>(); 
            for (Point currentPoint : p) {
                if (currentPoint.x < 0) {
                    continue;
                }                
                ViewColumn colToCopy = (ViewColumn) viewEditorGrid.getGrid()
                        .getColumn(currentPoint.x).getData(VALUE);
                if (copiedSet.containsKey(colToCopy.getId())) {
                    continue;
                }
                copiedSet.put(colToCopy.getId(),new ViewColumnCopy(colToCopy));
            }

            copiedViewColumnsList.addAll(copiedSet.values());
            Collections.sort(copiedViewColumnsList);
            
        } catch (SAFRException e) {
            UIUtilities.handleWEExceptions(e,
                    "Error occurred while copying View Column.", null);
        } finally {
            ApplicationMediator.getAppMediator().normalCursor();
        }
    }
        
    protected void addSortKey() {
        int index = getCurrentColIndex();

        if (index < 1) {
            return;
        }
        // add sort key to model
        ViewSortKey key = null;
        try {
            key = view.addSortKey(getCurrentColumn());
        } catch (SAFRException e) {
            UIUtilities.handleWEExceptions(e, "Error adding sort key.", null);
        }
        if (key == null) {
            return;
        }
        // add to list of models
        ViewRow m = new ViewRow(ViewColumnEditor.SORTKEY, key);
        int thisKeyRow = ViewColumnEditor.SORTKEY_HEADER + getSortKeyCount();
        viewEditorRows.add(thisKeyRow, m);
        // refresh sortkey header row
        viewEditorGrid.update(viewEditorRows.get(ViewColumnEditor.SORTKEY_HEADER), null);

        if (viewEditorRows.get(ViewColumnEditor.SORTKEY_HEADER).isExpanded()) {
            viewEditorGrid.insert(m, thisKeyRow);
            // set the row height.
            // viewEditorGrid.getGrid().setItemHeight(EDITOR_ROW_HEIGHT);
            m.setVisible(true);
        } else {
            m.setVisible(false);
        }
        // this can also affect column's start positions, so update start pos
        // rows too
        updateElement(ViewColumnEditor.STARTPOS);
        updateElement(ViewColumnEditor.COLUMN_PROP_HEADER);

        viewEditorGrid.getGrid().setItemHeight(EDITOR_ROW_HEIGHT);
        
        ApplicationMediator.getAppMediator().updateStatusContribution(
            ApplicationMediator.STATUSBARVIEW, "View Length: " + Integer.toString(view.getViewLength()), true);
        
        mediator.setModified(true);
    }
    
    protected void removeSortKey() {
        int index = getCurrentColIndex();

        if (index < 0) {
            return;
        }
        // remove sort key from model
        ViewSortKey key = view.removeSortKey(getCurrentColumn());
        if (key == null) {
            return;
        }
        // refresh sort key rows.
        refreshSortKeyRows();
        // this can also affect column's start positions, so update start pos
        // rows too
        updateElement(ViewColumnEditor.STARTPOS);
        updateElement(ViewColumnEditor.COLUMN_PROP_HEADER);

        ApplicationMediator.getAppMediator().updateStatusContribution(
            ApplicationMediator.STATUSBARVIEW, "View Length: " + Integer.toString(view.getViewLength()), true);        
        
        mediator.setModified(true);
    }
    
    /**
     * Refreshes the sort key rows. this will first remove all sort key rows and
     * will than add again using the latest sort key list from View model. This
     * function will also update the sort key header row.
     */
    protected void refreshSortKeyRows() {

        ViewRow elementSortKeyHeader = viewEditorRows.get(ViewColumnEditor.SORTKEY_HEADER);
        // remove all the sort key rows first.
        for (int i = ViewColumnEditor.SORTKEY_HEADER + 1;;) {
            if (viewEditorRows.get(i).isExpandable()) {
                // exit if this mediator is expandable
                // in this case it will be column output properties header.
                break;
            }
            // remove the sort key row and from grid too if sort keys are
            // expanded.
            if (elementSortKeyHeader.isExpanded()) {
                viewEditorGrid.remove(viewEditorRows.get(i));
            }
            viewEditorRows.remove(i);
        }

        // insert active sort key rows
        int position = ViewColumnEditor.SORTKEY_HEADER + 1;
        for (ViewSortKey sortKey : view.getViewSortKeys().getActiveItems()) {

            ViewRow elementSortKey = new ViewRow(ViewColumnEditor.SORTKEY, sortKey);
            if (!elementSortKeyHeader.isExpanded()) {
                elementSortKey.setVisible(false);
            }
            viewEditorRows.add(position, elementSortKey);
            if (elementSortKeyHeader.isExpanded()) {
                viewEditorGrid.insert(elementSortKey, position);
            }
            position++;
        }
        updateElement(ViewColumnEditor.SORTKEY_HEADER);
        // this is required to keep row height in control.
        viewEditorGrid.getGrid().setItemHeight(EDITOR_ROW_HEIGHT);
    }
    
    protected ViewColumn addNewColumn(int insertIndex, Boolean refresh) {
        // get current focus
        int focusY = viewEditorGrid.getGrid().getFocusCell().y;
        
        int index = insertIndex;
        ViewColumn tmpcolumn = null;
        try {
            tmpcolumn = view.addViewColumn(index);
            index = tmpcolumn.getColumnNo();
        } catch (SAFRValidationException sve) {
            MessageDialog.openError(mediator.getSite().getShell(), "New Column", sve.getMessageString());
            sve.printStackTrace();
            return null;
        } catch (SAFRException e) {
            UIUtilities.handleWEExceptions(e);
            return null;
        }

        GridViewerColumn column;
        if (insertIndex < 0) {
            column = new GridViewerColumn(viewEditorGrid, SWT.NONE);
        } else {
            // grid viewer accepts 1 based column number
            column = new GridViewerColumn(viewEditorGrid, SWT.NONE, index - 1);
        }
        // editing support and label provider needs zero based column number
        ViewColumnEditingSupport editingSupp = new ViewColumnEditingSupport(
            viewEditorGrid, index - 1, this, tmpcolumn, mediator);
        column.setEditingSupport(editingSupp);
        LabelProviderImpl labelProvider = new LabelProviderImpl(index - 1,tmpcolumn);
        column.setLabelProvider(labelProvider);

        column.getColumn().setData(COL_ID, index);
        column.getColumn().setData(VALUE, tmpcolumn);
        column.getColumn().setData(LABEL_PROVIDER, labelProvider);
        column.getColumn().setData(EDITING_SUPPORT, editingSupp);

        column.getColumn().setText("Column " + (index + 1));
        column.getColumn().setWidth(VIEW_COL_WIDTH);

        // Add handler to select whole column when column header is selected
        column.getColumn().addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                GridColumn col = (GridColumn)e.getSource();
                int idx = 0;
                for (GridColumn lcol : viewEditorGrid.getGrid().getColumns()) {
                    if (lcol == col) {
                        break;
                    }
                    idx++;
                }
                viewEditorGrid.getGrid().setSelection(new GridItem[0]);
                viewEditorGrid.getGrid().selectColumn(idx);
                viewEditorGrid.getGrid().setFocusItem(viewEditorGrid.getGrid().getItem(0));
                viewEditorGrid.getGrid().setFocusColumn(col);
                mediator.refreshToolbar();                
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
            
        });
        
        // set focus on the new column
        viewEditorGrid.setSelection((new StructuredSelection(viewEditorGrid.getGrid().getItem(focusY).getData())));
        viewEditorGrid.getGrid().setFocusItem(viewEditorGrid.getGrid().getItem(focusY));
        viewEditorGrid.getGrid().setFocusColumn(column.getColumn());
        viewEditorGrid.getGrid().setCellSelection(new Point(index - 1, focusY));
        viewEditorGrid.getGrid().showSelection();                
        openCellRelatedView(viewEditorGrid.getGrid().getFocusItem(), index - 1, true, true);
        
        if (refresh) {
            if (index >= 0) {
                resetColumnHeaders();
            }

            refreshGrid();
            mediator.formReflow(true);
        }
        mediator.setModified(true);
        
        ApplicationMediator.getAppMediator().updateStatusContribution(
            ApplicationMediator.STATUSBARVIEW, "View Length: " + Integer.toString(view.getViewLength()), true);
                
        return tmpcolumn;

    }

    /**
     * Closes a property view based on the enum value;
     * 
     * @param viewType
     *            the type of property view to close.
     */
    protected void closePropertyView(PropertyViewType viewType) {
        if (fromViewType != null && fromViewType != viewType) {
        	if(mediator.getSite().getPage() != null) {
	            IViewPart viewPart = mediator.getSite().getPage().findView(fromViewType.getViewId());
	            if (viewPart != null) {
	                // stop editing on our cell
	                viewEditorGrid.cancelEditing();
	            	mediator.getSite().getPage().hideView(viewPart);
	            }
        	}
        }        
        
        boolean sortView = false;
        if (viewType == PropertyViewType.SORTKEY) {
            // show the sort key title view too if applicable.
            if (view.getOutputFormat() == OutputFormat.Format_Report) {
                if (getCurrentSortKey() != null &&
                    getCurrentSortKey().getDisplayModeCode() != null
                        && getCurrentSortKey().getDisplayModeCode()
                                .getGeneralId() != Codes.ASDATA) {
                    // show only if the view type if hardcopy or drilldown
                    // and the sortkey's display mode is not AS DATA.
                    sortView = true;
                }
            }
        }
        if (sortView == false) {
            // close the sort key title view too
        	IWorkbenchPage page = mediator.getSite().getPage();
        	if(page != null) {
	            IViewPart viewPartTitle = page.findView(SortKeyTitleView.ID);
	            if (viewPartTitle != null) {
		            mediator.getSite().getPage().hideView(viewPartTitle);
	            }
        	}
        }
        
        fromViewType = viewType;
    }

    protected void openFocusedView() {
        if (!viewEditorGrid.getGrid().isDisposed() && viewEditorGrid.getGrid().getFocusCell() != null) {
            int col = viewEditorGrid.getGrid().getFocusCell().x;
            openCellRelatedView(viewEditorGrid.getGrid().getFocusItem(), col, true, true);
        }

    }
    
    /**
     * This method deletes an item from view editor grid depending upon where
     * the mouse is clicked.Used in command handler of Delete popup menu.
     */
    protected void delete() {
        try {
            mediator.getSite().getShell().setCursor(
                    mediator.getSite().getShell().getDisplay().getSystemCursor(
                            SWT.CURSOR_WAIT));

            if (currentHeaderRow != null) {
                // delete header row ie. sort key or data source.
                if (currentHeaderRow.getPropertyID() == ViewColumnEditor.DATASOURCE) {
                    // delete data source.
                    MessageDialog dialog = new MessageDialog(Display.getCurrent()
                            .getActiveShell(), "Delete View Source", null,
                            "This action will delete the selected View Source.",
                            MessageDialog.WARNING, new String[] { "&OK", "&Cancel" }, 0);
                    int confirm = dialog.open();
                    if (confirm == 0) {
                        viewEditorRows.remove(currentHeaderRow);
                        ViewRow dataSourceHeader = viewEditorRows
                        .get(viewEditorRows.indexOf(new ViewRow(ViewColumnEditor.DATA_SOURCE_HEADER, null)));
                        if (dataSourceHeader.isExpanded()) {
                            // refresh ViewEditor.DATA_SOURCE_HEADER row
                            viewEditorGrid.update(dataSourceHeader, null);
                            viewEditorGrid.remove(currentHeaderRow);
                        }
                        // code to adjust the row header width
                        int previousWidth;
                        int currentWidth;

                        previousWidth = MIN_ROW_HEADER_WIDTH;
                        int rowHeaderWidth = previousWidth;
                        for (GridItem item : viewEditorGrid.getGrid().getItems()) {
                            String headerText = item.getHeaderText();
                            GC gc = new GC(Display.getCurrent());
                            currentWidth = (gc.getFontMetrics()
                                    .getAverageCharWidth())
                                    * (headerText.length());
                            gc.dispose();
                            // to get the largest width of header text
                            if (previousWidth < currentWidth) {
                                rowHeaderWidth = currentWidth;
                                previousWidth = currentWidth;
                            }

                        }

                        setRowHeaderWidth(rowHeaderWidth);

                        // delete from model
                        view.removeViewSource((ViewSource) currentHeaderRow
                                .getData());
                        mediator.closeRelatedLogicTextEditors((ViewSource) currentHeaderRow
                                .getData());
                        try {
                            mediator.refreshRelatedLogicTextEditorsHeaders();
                        } catch (SAFRException e) {
                            UIUtilities.handleWEExceptions(e);
                        }
                        // set this variable to null to refresh view properly.

                        currentHeaderRow = null;

                        mediator.updateDataSourceViews();
                        mediator.setModified(true);
                    }
                } else if (currentHeaderRow.getPropertyID() == ViewColumnEditor.SORTKEY) {
                    // remove the clicked sort key

                    // remove sort key from model
                    ViewColumn sortKeyCol = ((ViewSortKey) currentHeaderRow
                            .getData()).getViewColumn();
                    ViewSortKey key = view.removeSortKey(sortKeyCol);
                    if (key == null) {
                        return;
                    }
                    // refresh sort key rows.
                    refreshSortKeyRows();
                    mediator.updateSortKeyViews();
                    mediator.setModified(true);
                } else {
                    // if the current header row is not data source or sort key
                    // then delete selected column on grid.
                    this.removeColumn();
                    mediator.updateDataSourceViews();
                    // CQ 8551. Nikita. 03/09/2010
                    // Sort Key views should become unavailable on deleting a
                    // column which is a sort key
                    mediator.updateSortKeyViews();
                }

            } else {
                this.removeColumn();
                mediator.updateDataSourceViews();
                mediator.updateSortKeyViews();
            }
        } finally {
            mediator.getSite().getShell().setCursor(null);
        }
        /* Jaydeep March 9, 2010 CQ 7615 : Added Source provider Implementation. */
        mediator.refreshToolbar();
    }
    
    protected List<ViewColumn> getSelectedColumns() {
        List<ViewColumn> columns = new ArrayList<ViewColumn>();
        Set<Integer> colIds = new HashSet<Integer>();
        
        Point[] selectedCellPoints = viewEditorGrid.getGrid().getCellSelection();        
        viewEditorGrid.getGrid().deselectAll();
        

        // add the selected columns to be deleted into a map
        for (Point currentPoint : selectedCellPoints) {
            if (currentPoint.x < 0) {
                continue;
            }
            ViewColumn colToRemove = (ViewColumn) viewEditorGrid.getGrid()
                .getColumn(currentPoint.x).getData(VALUE);
            if (!colIds.contains(colToRemove.getId())) {
                columns.add(colToRemove);
                colIds.add(colToRemove.getId());
            } 
        }
        return columns;
        
    }
    
    protected void removeColumn() {
        
        List<ViewColumn> columns = getSelectedColumns();
        if (columns.size() > 0) {
            MessageDialog dialog = new MessageDialog(Display.getCurrent()
                .getActiveShell(), "Delete Columns", null,
                "This action will delete the selected " + columns.size() + " column(s).",
                MessageDialog.WARNING, new String[] { "&OK", "&Cancel" }, 0);
            int confirm = dialog.open();
            if (confirm == 1 || confirm == -1) {
                return;
            }
        } else {
            return;
        }

        // Work around to avoid SWTException that occurs when getFocusCell is
        // called after a widget has been disposed. This is a bug in Nebula
        // (https://bugs.eclipse.org/bugs/show_bug.cgi?id=243996), and
        // the following piece of code should be removed once the bug is
        // fixed.
        Event mEvent = new Event();
        mEvent.x = viewEditorGrid.getGrid().getRowHeaderWidth() - 1;
        mEvent.y = viewEditorGrid.getGrid().getFocusCell().y;
        mEvent.button = 1;

        // remove the selected columns from the model and the UI
        boolean flag = false;
        for (ViewColumn currentColumn : columns) {
            // if any of the columns to be deleted is a sort key, sort key rows
            // should be refreshed
            if (!flag) {
                flag = currentColumn.isSortKey();
            }
            view.removeViewColumn(currentColumn);

            // remove column from UI
            int index = getColumnIndex(currentColumn);
            // index is 1-based while grid columns are 0-based.
            viewEditorGrid.getGrid().getColumn(index - 1).dispose();

            resetColumnHeaders();
            mediator.closeRelatedLogicTextEditors(currentColumn);
        }
        if (flag) {
            refreshSortKeyRows();
        }        
        viewEditorGrid.getGrid().showSelection();
        refreshGrid();

        mediator.formReflow(true);
        mediator.setModified(true);
        try {
            mediator.refreshRelatedLogicTextEditorsHeaders();
        } catch (SAFRException e) {
            UIUtilities.handleWEExceptions(e);
        }
        mediator.refreshToolbar();
        
        ApplicationMediator.getAppMediator().updateStatusContribution(
            ApplicationMediator.STATUSBARVIEW, 
            "View Length: " + Integer.toString(view.getViewLength()), true);        
    }
    
    public void refreshColumns() {
        
        if (viewEditorGrid != null) {
            // remove existing view columns from view.
            GridColumn[] columns = viewEditorGrid.getGrid().getColumns();
            for (int i = 0; i < columns.length; i++) {
                columns[i].dispose();
            }
            // add View columns from Model.
            int i = 0;
            for (ViewColumn viewColumn : view.getViewColumns().getActiveItems()) {
                GridViewerColumn column = new GridViewerColumn(viewEditorGrid, SWT.NONE);
                ViewColumnEditingSupport editingSupp = new ViewColumnEditingSupport(
                    viewEditorGrid, i, this, viewColumn, mediator);
                column.setEditingSupport(editingSupp);

                LabelProviderImpl labelProvider = new LabelProviderImpl(i, viewColumn);
                column.setLabelProvider(labelProvider);

                column.getColumn().setData(COL_ID, i + 1);
                column.getColumn().setData(VALUE, viewColumn);
                column.getColumn().setData(LABEL_PROVIDER,labelProvider);
                column.getColumn().setData(EDITING_SUPPORT,editingSupp);

                column.getColumn().setText("Column " + (i + 1));
                column.getColumn().setWidth(VIEW_COL_WIDTH); // column.getColumn().setMoveable(true);

                i++;
            }
            viewEditorGrid.refresh();            
        }
    }
    
    public void setModified() {
        mediator.setModified(true);        
    }
    
    /**
     * This method is used as command handler of insert new View Source.
     */
    protected void insertDataSource() {
        try {
            mediator.getSite().getShell().setCursor(
                    mediator.getSite().getShell().getDisplay().getSystemCursor(
                            SWT.CURSOR_WAIT));

            NewViewSourceDialog newViewSourceDialog = new NewViewSourceDialog(
                    mediator.getSite().getShell());
            newViewSourceDialog.create();

            if (newViewSourceDialog.open() == NewViewSourceDialog.OK) {
                ViewSource newViewSource = null;
                try {
                    newViewSource = this.view.addViewSource();
                } catch (SAFRException e) {
                    UIUtilities.handleWEExceptions(e);
                }
                if (newViewSource == null) {
                    return;
                }
                newViewSource.setLrFileAssociation(newViewSourceDialog
                        .getLRLFAssociation());
                WriteStatementGenerator generator = new WriteStatementGenerator(view, newViewSource);
                generator.generateWriteStatement();                
                // add to list of models
                ViewRow m = new ViewRow(ViewColumnEditor.DATASOURCE, newViewSource);
                viewEditorRows.add(m);
                ViewRow dataSourceHeader = viewEditorRows.get(viewEditorRows.indexOf(
                    new ViewRow(ViewColumnEditor.DATA_SOURCE_HEADER, null)));            
                if (dataSourceHeader.isExpanded()) {
                    m.setVisible(true);
                    viewEditorGrid.update(dataSourceHeader, null);
                    viewEditorGrid.insert(m, -1);
                } else {
                    m.setVisible(false);
                }
                mediator.setModified(true);
            } else {
                return;
            }

        } finally {
            mediator.getSite().getShell().setCursor(null);
        }
    }

    /**
     * This method is used as command handler of copy View Source.
     */
    protected void copyDataSource() {
        try {
            ApplicationMediator.getAppMediator().waitCursor();

            ViewSource currentViewSource = null;
            // first check for single view source 
            if (view.getViewSources().getActiveItems().size() == 1) {
                currentViewSource = view.getViewSources().getActiveItems().get(0);
            } else {
                // get selected ViewSource
                IStructuredSelection selection = (IStructuredSelection) viewEditorGrid.getSelection();
                if (selection != null) {
                    ViewRow row = (ViewRow) selection.getFirstElement();
                    if (row != null) {
                        if (row.getPropertyID() == ViewColumnEditor.DATASOURCE) {
                            // not in row header
                            currentViewSource = (ViewSource) row.getData();
                        }
                    }
                }
            }
            if (currentViewSource == null) {
                return;
            }

            // make a new view source
            ViewSource copyViewSource = this.view.addViewSource();
            copyViewSource.setLrFileAssociation(currentViewSource.getLrFileAssociation());
            copyViewSource.setExtractRecordFilter(currentViewSource.getExtractRecordFilter());
            copyViewSource.setExtractFileAssociation(currentViewSource.getExtractFileAssociation());
            copyViewSource.setWriteExitId(currentViewSource.getWriteExitId());
            copyViewSource.setExtractOutputOverride(currentViewSource.isExtractOutputOverriden());
            copyViewSource.setExtractRecordOutput(currentViewSource.getExtractRecordOutput());
            
            Map<Integer,ViewColumnSource> lhsMap = new HashMap<Integer,ViewColumnSource>();
            for (ViewColumnSource lhs : copyViewSource.getViewColumnSources()) {
                lhsMap.put(lhs.getViewColumn().getColumnNo(), lhs);
            }
            
            // copy all View Column Sources
            for (ViewColumnSource rhs : currentViewSource.getViewColumnSources()) {
                ViewColumnSource lhs = lhsMap.get(rhs.getViewColumn().getColumnNo());
                lhs.copyFromViewColumnSource(rhs);
            }

            // add to list of models
            ViewRow m = new ViewRow(ViewColumnEditor.DATASOURCE, copyViewSource);            
            viewEditorRows.add(m);
            ViewRow dataSourceHeader = viewEditorRows.get(viewEditorRows.indexOf(
                new ViewRow(ViewColumnEditor.DATA_SOURCE_HEADER, null)));            
            if (dataSourceHeader.isExpanded()) {
                m.setVisible(true);
                viewEditorGrid.update(dataSourceHeader, null);
                viewEditorGrid.insert(m, -1);
            } else {
                m.setVisible(false);
            }
            mediator.setModified(true);

        } catch (SAFRException e) {
            UIUtilities.handleWEExceptions(e);
        } finally {
            ApplicationMediator.getAppMediator().normalCursor();
        }
    }

    protected void columnGenerator() {
        try {
            ApplicationMediator.getAppMediator().waitCursor();
            
            // get selected ViewSource
            ViewSource currentViewSource = null;
            // first check for single view source 
            if (view.getViewSources().getActiveItems().size() == 1) {
                currentViewSource = view.getViewSources().getActiveItems().get(0);
            } else {
                currentViewSource = getCurrentViewSource();
            }
            
            if (currentViewSource == null) {
                return;
            }
            
            ViewGenDialog viewGen = new ViewGenDialog(
                mediator.getSite().getShell(),
                view,
                currentViewSource,
                this);
            viewGen.open();
            
        } catch (SAFRException e) {
            UIUtilities.handleWEExceptions(e);
        } finally {
            ApplicationMediator.getAppMediator().normalCursor();
        }
    }
    
    protected CellEditor getCurrentCellEditor() {
        return currentCellEditor;
    }

    protected void setCurrentCellEditor(CellEditor currentCellEditor) {
        this.currentCellEditor = currentCellEditor;
    }

    /**
     * This method updates row with given propertyId. It only works on static
     * rows. This method cannot be used with dynamic rows
     * 
     * @param propertyId
     *            : property ID of the row to be updated.
     */
    protected void updateElement(int propertyId) {
        if (viewEditorGrid != null) {
            if (propertyId == ViewColumnEditor.SORTKEY || propertyId == ViewColumnEditor.DATASOURCE) {
                return;
            }
            ViewRow element = new ViewRow(propertyId, null);
            element = viewEditorRows.get(viewEditorRows.indexOf(element));
            viewEditorGrid.update(element, null);
        }
    }
    
    /**
     * Updates sort key row for the specified view sort key.
     * 
     * @param viewSortKey
     *            of the row to be updated.
     */
    protected void updateSortKeyHeader(ViewSortKey viewSortKey) {
        viewEditorGrid.update(new ViewRow(ViewColumnEditor.SORTKEY, viewSortKey), null);
    }
    
    /**
     * Updates all sort key rows. This includes sort key header and sort key
     * rows below it.
     */
    protected void updateSortKeyRows() {
        viewEditorGrid
                .update(viewEditorGrid.getElementAt(ViewColumnEditor.SORTKEY_HEADER), null);
        for (ViewSortKey sortKey : view.getViewSortKeys().getActiveItems()) {
            ViewRow elementSortKey = new ViewRow(ViewColumnEditor.SORTKEY, sortKey);
            viewEditorGrid.update(elementSortKey, null);
        }
        viewEditorGrid.getGrid().setItemHeight(EDITOR_ROW_HEIGHT);
    }
    
    /**
     * Updates editor rows affected by data type change.
     */
    protected void updateDataTypeChangeAffectedRow() {
        updateElement(ViewColumnEditor.DECIMALPLACES);
        updateElement(ViewColumnEditor.SCALINGFACTOR);
        updateElement(ViewColumnEditor.SIGNED);
        updateElement(ViewColumnEditor.NUMERICMASK);
        updateElement(ViewColumnEditor.FORMATPHASECALCULATION);
        updateElement(ViewColumnEditor.RECORDAGGREGATIONFUNCTION);
        updateElement(ViewColumnEditor.GROUPAGGREGATIONFUNCTION);
    }
    
    protected void moveColumnRight() {
        try {
            mediator.getSite().getShell().setCursor(
                    mediator.getSite().getShell().getDisplay().getSystemCursor(
                            SWT.CURSOR_WAIT));

            int index = getCurrentColIndex();

            if (index < 0
                    || index >= view.getViewColumns().getActiveItems().size()) {
                // last column can't be moved right.
                return;
            }
            // move column in model
            view.moveColumnRight(getCurrentColumn());

            // reset header text
            resetColumnHeaders();

            // set focus on moved column
            viewEditorGrid.getGrid().setFocusColumn(viewEditorGrid.getGrid().getColumn(index));
            viewEditorGrid.getGrid().showColumn(viewEditorGrid.getGrid().getColumn(index));            
            Point points[] = viewEditorGrid.getGrid().getCellSelection();
            if (points.length > 0) {
                for (int i=0 ; i<points.length ; i++) {
                    points[i].x = getCurrentColumn().getColumnNo()-1;
                }
                viewEditorGrid.getGrid().setCellSelection(points);              
            }           
            
            updateElement(ViewColumnEditor.STARTPOS);
            updateElement(ViewColumnEditor.COLUMN_PROP_HEADER);
            try {
                mediator.refreshRelatedLogicTextEditorsHeaders();
            } catch (SAFRException e) {
                UIUtilities.handleWEExceptions(e);
            }
            mediator.setModified(true);

        } finally {
            mediator.getSite().getShell().setCursor(null);
        }
        mediator.refreshToolbar();
    }
    
    protected void moveColumnLeft() {
        try {
            mediator.getSite().getShell().setCursor(
                    mediator.getSite().getShell().getDisplay().getSystemCursor(
                            SWT.CURSOR_WAIT));

            int index = getCurrentColIndex();

            if (index < 2) {
                // first column can't be moved left
                return;
            }
            // move column in model
            view.moveColumnLeft(getCurrentColumn());

            // reset header text
            resetColumnHeaders();

            // set focus on moved column
            viewEditorGrid.getGrid().setFocusColumn(viewEditorGrid.getGrid().getColumn(index-2));
            viewEditorGrid.getGrid().showColumn(viewEditorGrid.getGrid().getColumn(index-2));
            
            Point points[] = viewEditorGrid.getGrid().getCellSelection();
            if (points.length > 0) {
                for (int i=0 ; i<points.length ; i++) {
                    points[i].x = getCurrentColumn().getColumnNo()-1;
                }
                viewEditorGrid.getGrid().setCellSelection(points);              
            }
            
            updateElement(ViewColumnEditor.STARTPOS);
            updateElement(ViewColumnEditor.COLUMN_PROP_HEADER);
            try {
                mediator.refreshRelatedLogicTextEditorsHeaders();
            } catch (SAFRException e) {
                UIUtilities.handleWEExceptions(e);
            }
            mediator.setModified(true);
        } finally {
            mediator.getSite().getShell().setCursor(null);
        }
        mediator.refreshToolbar();
    }
      
    /**
     * Inserts column to left of current highlighted column.
     * 
     * @param refresh
     *            pass true if the grid is to be refreshed after inserting
     *            viewColumn.
     * @return newly added viewColumn reference.
     */
    protected ViewColumn insertColumnBefore(Boolean refresh) {
        try {
            ApplicationMediator.getAppMediator().waitCursor();

            int index = getCurrentColIndex();
            if (index == -1) {
                index = 0;
            }
            ViewColumn column = mediator.addNewColumn(index, refresh);
            if (refresh) {
                try {
                    mediator.refreshRelatedLogicTextEditorsHeaders();
                } catch (SAFRException e) {
                    UIUtilities.handleWEExceptions(e);
                }
            }
            mediator.refreshToolbar();
            return column;

        } finally {
            ApplicationMediator.getAppMediator().normalCursor();
        }
    }

    public ViewColumn insertColumnAfter(boolean refresh) {
        try {
            ApplicationMediator.getAppMediator().waitCursor();

            int index = getCurrentColIndex();
            ViewColumn column = mediator.addNewColumn(index+1, refresh);
            if (refresh) {
                try {
                    mediator.refreshRelatedLogicTextEditorsHeaders();
                } catch (SAFRException e) {
                    UIUtilities.handleWEExceptions(e);
                }
            }
            mediator.refreshToolbar();
            return column;

        } finally {
            ApplicationMediator.getAppMediator().normalCursor();
        }
    }    
    
    
    protected void pasteViewColumnsLeft() {
        try {
            ApplicationMediator.getAppMediator().waitCursor();
            
            if (copiedViewColumnsList.size() > 0) {
                
                int currentColIndex = getCurrentColIndex();
                
                for (ViewColumnCopy copiedViewColumn : copiedViewColumnsList) {
                    
                    ViewColumn vc = mediator.addNewColumn(currentColIndex++, false);
                    if (vc == null) {
                        // no view column added. no need to proceed.
                        return;
                    }
                    copiedViewColumn.getViewColumnFromCopy(vc);
                }
                resetColumnHeaders();
                refreshGrid();
                mediator.formReflow(true);
                mediator.refreshRelatedLogicTextEditorsHeaders();
            }
        } catch (SAFRException e) {
            UIUtilities.handleWEExceptions(e,
                    "Error occurred while pasting View Column.", null);
        }

        finally {
            ApplicationMediator.getAppMediator().normalCursor();
        }
        ApplicationMediator.getAppMediator().updateStatusContribution(
            ApplicationMediator.STATUSBARVIEW, "View Length: " + Integer.toString(view.getViewLength()), true);                
        
        mediator.refreshToolbar();
    }
       
    protected void pasteViewColumnsRight() {
        try {
            ApplicationMediator.getAppMediator().waitCursor();
            
            if (copiedViewColumnsList.size() > 0) {
                // get current column index for paste operation.
                int currentColIndex = getCurrentColIndex() + 1;
                for (ViewColumnCopy copiedViewColumn : copiedViewColumnsList) {
                    ViewColumn vc = mediator.addNewColumn(currentColIndex++, false);
                    if (vc == null) {
                        // no view column added. no need to proceed further.
                        return;
                    }
                    copiedViewColumn.getViewColumnFromCopy(vc);
                }
                resetColumnHeaders();
                refreshGrid();
                mediator.formReflow(true);
                mediator.refreshRelatedLogicTextEditorsHeaders();
            }

        } catch (SAFRException e) {
            UIUtilities.handleWEExceptions(e,
                    "Error occurred while pasting View Column.", null);
        } finally {
            ApplicationMediator.getAppMediator().normalCursor();
        }
        ApplicationMediator.getAppMediator().updateStatusContribution(
            ApplicationMediator.STATUSBARVIEW, "View Length: " + Integer.toString(view.getViewLength()), true);                
        
        mediator.refreshToolbar();
    }
    
    protected void showColumn(final Integer colNum) {
        
        Display.getCurrent().asyncExec(new Runnable() {
            
            public void run() {
                viewEditorGrid.getGrid().showColumn(viewEditorGrid.getGrid().getColumn(colNum-1));   
                viewEditorGrid.getGrid().setSelection(new int[0]);
                viewEditorGrid.getGrid().selectColumn(colNum-1);
            }
            
        });
    }    
    
    /**
     * Updates the rows of the View affected by changing the Source LR Field or
     * the Lookup Field of the Column Source, after these values are copied into
     * the View Column.
     */
    protected void updateColumnSourceAffectedRows() {
        updateElement(ViewColumnEditor.HEAD1);
        updateElement(ViewColumnEditor.HEAD2);
        updateElement(ViewColumnEditor.HEAD3);
        updateElement(ViewColumnEditor.DATATYPE);
        updateElement(ViewColumnEditor.DATETIMEFORMAT);
        updateElement(ViewColumnEditor.LENGTH);
        updateElement(ViewColumnEditor.HEADERALIGNMENT);
        updateElement(ViewColumnEditor.DECIMALPLACES);
        updateElement(ViewColumnEditor.SCALINGFACTOR);
        updateElement(ViewColumnEditor.SIGNED);
        updateElement(ViewColumnEditor.NUMERICMASK);
        updateElement(ViewColumnEditor.FORMATPHASECALCULATION);
        updateElement(ViewColumnEditor.RECORDAGGREGATIONFUNCTION);
        updateElement(ViewColumnEditor.GROUPAGGREGATIONFUNCTION);
    }
    
    /**
     * Updates editor rows affected by a change made in output format, output
     * format type and format phase usage.
     */
    protected void updateOutputInformationChangeAffectedRows(
            boolean updatePropertyViews) {
        updateElement(ViewColumnEditor.DATAALIGNMENT);
        updateElement(ViewColumnEditor.VISIBLE);
        updateElement(ViewColumnEditor.SPACEBEFORECOLUMN);
        updateElement(ViewColumnEditor.HEADERALIGNMENT);
        updateElement(ViewColumnEditor.FORMATPHASECALCULATION);
        updateElement(ViewColumnEditor.RECORDAGGREGATIONFUNCTION);
        updateElement(ViewColumnEditor.GROUPAGGREGATIONFUNCTION);
        updateElement(ViewColumnEditor.STARTPOS);

        // CQ 8551. Nikita. 01/09/2010.
        // Forcefully refresh the Column Source View if output format has
        // changed (Effective Date Type and Value are disabled for Source Record
        // Structure)
        ColumnSourceView colView = (ColumnSourceView) mediator.getSite().getPage()
                .findView(ColumnSourceView.ID);
        if (colView != null) {
            colView.showGridForCurrentEditor(mediator.getEditor());
        }

        // refresh SortKeyView and SortKeyTitleView
        if (updatePropertyViews) {
            SortKeyTitleView sktView = (SortKeyTitleView) mediator.getSite().getPage()
                    .findView(SortKeyTitleView.ID);
            if (sktView != null) {
                sktView.showGridForCurrentEditor(mediator.getEditor(), false);
            }
            SortKeyView skView = (SortKeyView) mediator.getSite().getPage().findView(
                    SortKeyView.ID);
            if (skView != null) {
                skView.showGridForCurrentEditor(mediator.getEditor(), false);
            }
        }
    }    
    
    /**
     * Updates the data source row.
     * 
     * @param viewSource
     *            the data source whose row is to be updated.
     */
    protected void updateColumnSourceAffectedRows(ViewSource viewSource) {
        ViewRow elementColumnSource = new ViewRow(ViewColumnEditor.DATASOURCE, viewSource);
        viewEditorGrid.update(elementColumnSource, null);
        updateElement(ViewColumnEditor.STARTPOS);        
    }
    
    protected boolean copiedViewColumnsIsEmpty() {
        return copiedViewColumnsList.isEmpty();        
    }
    
    protected void setFocus() {
        viewEditorGrid.getGrid().setFocus();
    }
    
    protected int getColumnCount() {
        return viewEditorGrid.getGrid().getColumnCount();
    }
    
    // private methods
    
    // Method to set row header width in range Maximum and minimum row header
    // width.
    private void setRowHeaderWidth(int rowHeaderWidth) {
        if (rowHeaderWidth > MAX_ROW_HEADER_WIDTH) {
            viewEditorGrid.getGrid().setItemHeaderWidth(MAX_ROW_HEADER_WIDTH);

        } else if (rowHeaderWidth < MIN_ROW_HEADER_WIDTH) {
            viewEditorGrid.getGrid().setItemHeaderWidth(MIN_ROW_HEADER_WIDTH);

        } else {
            viewEditorGrid.getGrid().setItemHeaderWidth(rowHeaderWidth);

        }

    }
    
    private int getSortKeyCount() {
        return this.view.getViewSortKeys().getActiveItems().size();

    }
    
    
    private IViewPart openCellRelatedView(GridItem item, int col,
            boolean inGrid, boolean isOpenEvent) {
        IViewPart ret = null;
        Grid g = viewEditorGrid.getGrid();
        PropertyViewType openView = null;

        SourceProvider service = UIUtilities.getSourceProvider();
        service.setAllowDeleteForDataSource(false);

        if (item != null) {
            // if an item found on this cursor location
            ViewRow row = (ViewRow) item.getData();
            if (inGrid) {
                // only if selected outside row headers.
                // get column list index (zero based)
                if (col < 0) {
                    return ret;
                }
                
                if (row.getPropertyID() == ViewColumnEditor.SORTKEY_HEADER
                        && g.getColumn(col) != null) {
                    // if selected on a Sort Key header and provided a
                    // column exists where the cursor was clicked
                    if ((view.getViewColumns().getActiveItems().get(
                            col).isSortKey())) {
                        openView = PropertyViewType.SORTKEY;
                    }
                } else if (row.getPropertyID() == ViewColumnEditor.DATASOURCE
                        && g.getColumn(col) != null) {
                    // if selected on a Column Source and provided a
                    // column exists where the cursor was clicked
                    openView = PropertyViewType.COLUMNSOURCE;
                }
                currentHeaderRow = null;

            } else {
                // if selected inside row header
                if (row.getPropertyID() == ViewColumnEditor.SORTKEY) {
                    openView = PropertyViewType.SORTKEY;
                } else if (row.getPropertyID() == ViewColumnEditor.DATASOURCE) {
                    service.setAllowDeleteForDataSource(true);
                    openView = PropertyViewType.DATASOURCE;
                }
                currentHeaderRow = (ViewRow) item.getData();
            }            
            closePropertyView(openView);
            if (openView != null && isOpenEvent) {
                // open events are SPACE key or left mouse click
                ret = showPropertyView(openView);
            }
        } else {
            closePropertyView(openView);            
            currentHeaderRow = null;
        }

        // data source view should be refreshed as this will affect
        // this property view if its a new SAFR View.
        DataSourceView dsView = (DataSourceView) mediator.getSite()
                .getPage().findView(DataSourceView.ID);
        if (dsView != null) {
            dsView.showGridForCurrentEditor(mediator.getEditor());
        }
        return ret;
    }
    
    
    /**
     * Shows a property view based on the enum value;
     * 
     * @param viewType
     *            the type of property view to open.
     */
    private IViewPart showPropertyView(PropertyViewType viewType) {
        try {
            
            if (viewType == PropertyViewType.SORTKEY) {
                // show the sort key title view too if applicable.
                if (view.getOutputFormat() == OutputFormat.Format_Report) {
                    if (getCurrentSortKey().getDisplayModeCode() != null
                            && getCurrentSortKey().getDisplayModeCode()
                                    .getGeneralId() != Codes.ASDATA) {
                        // show only if the view type if hardcopy or drilldown
                        // and the sortkey's display mode is not AS DATA.
                        mediator.getSite().getPage().showView(SortKeyTitleView.ID);
                    }
                }
            }
            IViewPart viewPart = mediator.getSite().getPage().showView(viewType.getViewId());
            mediator.getSite().getPage().activate(mediator.getEditor());
            return viewPart;
        } catch (PartInitException e1) {
            UIUtilities.handleWEExceptions(e1,
                    "Unexpected error occurred while opening view.", null);
            return null;
        }
    }
    
    private void expandCollapseRows(ViewRow fromRow, boolean expand) {
        ViewRow row;
        int index = viewEditorRows.indexOf(fromRow);
        for (int i = index + 1; i < viewEditorRows.size(); i++) {
            row = viewEditorRows.get(i);
            if (row.isExpandable()) {
                break;
            }
            row.setVisible(expand);
        }
    }
    
    protected boolean enableViewEditorCells(ViewColumn viewColumn,
            int propertyID) {
        View view = viewColumn.getView();
        switch (propertyID) {
        case ViewColumnEditor.STARTPOS:
            return false;
        case ViewColumnEditor.DATATYPE:
            return true;
        case ViewColumnEditor.DATETIMEFORMAT:
            if (viewColumn.getDataTypeCode() != null) {
                if (viewColumn.getDataTypeCode().getGeneralId() == Codes.EDITED_NUMERIC
                        || viewColumn.getDataTypeCode().getGeneralId() == Codes.MASKED_NUMERIC) {
                    return false;
                } else {
                    return true;
                }
            }
        case ViewColumnEditor.LENGTH:
            return true;
        case ViewColumnEditor.DATAALIGNMENT:
            if (view.getOutputFormat() != null && 
                view.getOutputFormat() == OutputFormat.Format_Delimited_Fields) {
                // disable for delimited fields.
                return false;
            } else {
                return true;
            }
        case ViewColumnEditor.VISIBLE:
            if (!view.isFormatPhaseInUse()) {
                return false;
            } else {
                return true;
            }
        case ViewColumnEditor.SPACEBEFORECOLUMN:
            if (view.getOutputFormat() != null && view.getOutputFormat() == OutputFormat.Format_Report) {
                return true;
            } else {
                return false;
            }
        case ViewColumnEditor.HEADERALIGNMENT:
            if (view.getOutputFormat() != null) {
                if (view.getOutputFormat() == OutputFormat.Format_Report) {
                    return true;
                } else {
                    return false;
                }
            }
            break;
        case ViewColumnEditor.DECIMALPLACES:
        case ViewColumnEditor.SCALINGFACTOR:
            if (viewColumn.getDataTypeCode() != null) {
                if (viewColumn.getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC) {
                    return false;
                } else {
                    return true;
                }
            }
            break;
        case ViewColumnEditor.SIGNED:
            if (viewColumn.getDataTypeCode() != null) {
                if (viewColumn.getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC
                        || viewColumn.getDataTypeCode().getGeneralId() == Codes.BINARY_CODED_DECIMAL
                        || viewColumn.getDataTypeCode().getGeneralId() == Codes.EDITED_NUMERIC
                        || (viewColumn.getDataTypeCode().getGeneralId() == Codes.BINARY && viewColumn
                                .getDateTimeFormatCode() != null)) {
                    return false;
                } else {
                    return true;
                }
            }
            break;
        case ViewColumnEditor.NUMERICMASK:
            if (viewColumn.getDataTypeCode() != null) {
                if (viewColumn.getDataTypeCode().getGeneralId() == Codes.MASKED_NUMERIC) {
                    return true;
                } else {
                    return false;
                }
            }
            break;
        case ViewColumnEditor.FORMATPHASECALCULATION:
            if (view.isFormatPhaseInUse()) {
                // only applicable if format phase is used.
                if ((viewColumn.getDataTypeCode() != null && viewColumn
                        .getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC)
                        || viewColumn.isSortKey()) {
                    // not applicable if data type is alpha or
                    // column is sortkey.
                    mediator.closeRelatedLogicTextEditors(viewColumn);
                    return false;
                } else {
                    return true;
                }
            } else {
                mediator.closeRelatedLogicTextEditors(viewColumn);
                return false;
            }
        case ViewColumnEditor.RECORDAGGREGATIONFUNCTION:
            if (view.isFormatPhaseInUse()) {
                // only applicable if format phase is in use
                if ((viewColumn.getDataTypeCode() != null && viewColumn
                        .getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC)
                        || viewColumn.isSortKey()
                        || !view.isFormatPhaseRecordAggregationOn()) {
                    // disable if record aggregation is off or the
                    // column is alpha or the column is sortkey.
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        case ViewColumnEditor.GROUPAGGREGATIONFUNCTION:
            if (view.getOutputFormat() != null) {
                if (view.getOutputFormat() == OutputFormat.Format_Report) {
                    // only applicable for drilldown and hardcopy.
                    if ((viewColumn.getDataTypeCode() != null && 
                         viewColumn.getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC) || 
                        viewColumn.isSortKey() || 
                        (view.isFormatPhaseRecordAggregationOn() &&
                         viewColumn.getRecordAggregationCode() != null &&
                         viewColumn.getRecordAggregationCode().getGeneralId() != Codes.SUM)) {
                        // disable if view col is alpha or view col is
                        // sortkey or (record aggregation is on and
                        // record aggregation code is not equal to SUM).
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
        return true;
    }

    /**
     * Get the grid index (one-based) of the selected view column
     * 
     * @param currentColumn
     *            View Column whose index is to be retrieved.
     * @return index of the column
     */
    private int getColumnIndex(ViewColumn currentColumn) {
        GridColumn[] gridColumns = viewEditorGrid.getGrid().getColumns();
        for (GridColumn gridCol : gridColumns) {
            if (((ViewColumn) gridCol.getData(VALUE)).equals(currentColumn)) {
                return (Integer) gridCol.getData(COL_ID);
            }
        }
        return -1;
    }

}
