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


import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewSortKey;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.editors.logic.FCCLogicTextEditorInput;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditor;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditorInput;
import com.ibm.safr.we.ui.utilities.IRowEditingSupport;
import com.ibm.safr.we.ui.utilities.RowEditorType;
import com.ibm.safr.we.ui.utilities.SAFRCheckboxCellEditor;
import com.ibm.safr.we.ui.utilities.SAFRComboBoxCellEditor;
import com.ibm.safr.we.ui.utilities.SAFRDialogCellEditor;
import com.ibm.safr.we.ui.utilities.SAFRTextCellEditor;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.utilities.VerifyNumericListener;
import com.ibm.safr.we.ui.utilities.WidgetType;
import com.ibm.safr.we.ui.views.vieweditor.LogicTextDialogCellEditor;

public class ViewColumnEditingSupport extends EditingSupport {

	private int colIndex;
	private GridTableViewer viewer;
	private CellEditor editor;
	private ViewColumnEditor viewColumnEditor;
	private ViewColumn viewColumn;
	private View view;
	private ViewMediator mediator;

	private CCombo comboDataType;
	private CCombo comboDateTimeFormat;
	private CCombo comboAlignHeading;
	private CCombo comboAlignData;
	private CCombo comboNumericMask;
	private CCombo comboGroupAggregation;
	private CCombo comboRecordAggregation;

	private static final int MAX_HEADING = 254;
	private static final int MAX_DECIMAL_PLACES = 1;
	private static final int MAX_SCALING = 2;
	private static final int MAX_LENGTH = 5;
	private static final int MAX_SPACE_BEFORE_COL = 5;

	public ViewColumnEditingSupport(final GridTableViewer viewer, int colIndex,
			ViewColumnEditor viewEditor, ViewColumn viewColumn, ViewMediator mediator) {
		super(viewer);
		this.colIndex = colIndex;
		this.viewer = viewer;
		this.viewColumn = viewColumn;
		this.view = viewColumn.getView();
		this.viewColumnEditor = viewEditor;
		this.mediator = mediator;
	}

	@Override
	protected boolean canEdit(Object element) {
        
        ViewEditorInput viewEditorInput = mediator.getEditorInput();
        if (viewEditorInput.getEditRights() == EditRights.Read) {
                return false;
        }        
        
        IRowEditingSupport m = (IRowEditingSupport) element;
        switch (m.getPropertyID()) {
        case ViewColumnEditor.COLUMN_HEADING_HEADER:
            return false;
        case ViewColumnEditor.HEAD1:
        case ViewColumnEditor.HEAD2:
        case ViewColumnEditor.HEAD3:
            return true;
        case ViewColumnEditor.SORTKEY_HEADER:
        case ViewColumnEditor.SORTKEY:
        case ViewColumnEditor.COLUMN_PROP_HEADER:
            return false;
        case ViewColumnEditor.STARTPOS:
        case ViewColumnEditor.DATATYPE:
        case ViewColumnEditor.DATETIMEFORMAT:
        case ViewColumnEditor.LENGTH:
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
        if (m.getRowEditorType() != RowEditorType.NONE &&
            viewColumnEditor.enableViewEditorCells(viewColumn, m.getPropertyID())) {
            return true;
        }
        else {
            return false;                
        }        
        case ViewColumnEditor.DATA_SOURCE_HEADER:
        case ViewColumnEditor.DATASOURCE:
        default:
            return false;
        }
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		viewColumnEditor.setCurrentCellEditor(null);
		IRowEditingSupport m = (IRowEditingSupport) element;
		editor = null;
		if (m.getRowEditorType() == RowEditorType.TEXT
				|| m.getRowEditorType() == RowEditorType.NUMBER) {

			if (m.getRowEditorType() == RowEditorType.TEXT) {
				switch (m.getPropertyID()) {
				case ViewColumnEditor.HEAD1:
				case ViewColumnEditor.HEAD2:
				case ViewColumnEditor.HEAD3:
				    editor = new SAFRTextCellEditor(viewer.getGrid());
					((Text) editor.getControl()).setTextLimit(MAX_HEADING);
					break;
				}
			} else if (m.getRowEditorType() == RowEditorType.NUMBER) {
				switch (m.getPropertyID()) {
				case ViewColumnEditor.LENGTH:
				    editor = new SAFRTextCellEditor(viewer.getGrid());
					((Text) editor.getControl())
							.addVerifyListener(new VerifyNumericListener(
									WidgetType.INTEGER, false));
					((Text) editor.getControl()).setTextLimit(MAX_LENGTH);
					break;
				case ViewColumnEditor.SPACEBEFORECOLUMN:
                    editor = new SAFRTextCellEditor(viewer.getGrid());				
					((Text) editor.getControl())
							.addVerifyListener(new VerifyNumericListener(
									WidgetType.INTEGER, false));
					((Text) editor.getControl())
							.setTextLimit(MAX_SPACE_BEFORE_COL);
					break;
				case ViewColumnEditor.DECIMALPLACES:
                    editor = new SAFRTextCellEditor(viewer.getGrid());              
					((Text) editor.getControl())
							.addVerifyListener(new VerifyNumericListener(
									WidgetType.INTEGER, false));
					((Text) editor.getControl())
							.setTextLimit(MAX_DECIMAL_PLACES);
					break;
				case ViewColumnEditor.SCALINGFACTOR:
				    editor = new SAFRTextCellEditor(viewer.getGrid());              
					((Text) editor.getControl())
							.addVerifyListener(new VerifyNumericListener(
									WidgetType.INTEGER, true));
					((Text) editor.getControl()).setTextLimit(MAX_SCALING);
					break;

				}
			}
		} else if (m.getRowEditorType() == RowEditorType.COMBO) {
			int style = ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION
					| ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION
					| ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION
					| ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION;
			switch (m.getPropertyID()) {
			case ViewColumnEditor.DATATYPE:
                editor = new SAFRComboBoxCellEditor(viewer.getGrid(), new String[] {},
                    SWT.READ_ONLY);
                ((SAFRComboBoxCellEditor)editor).setActivationStyle(style);                
				comboDataType = (CCombo) editor.getControl();
				comboDataType.setVisibleItemCount(9); // CQ10094
				UIUtilities.populateComboBox(comboDataType,
						CodeCategories.DATATYPE, 0, false);
				break;
			case ViewColumnEditor.HEADERALIGNMENT:
                editor = new SAFRComboBoxCellEditor(viewer.getGrid(), new String[] {},
                    SWT.READ_ONLY);
                ((SAFRComboBoxCellEditor)editor).setActivationStyle(style);                
				comboAlignHeading = (CCombo) editor.getControl();
				UIUtilities.populateComboBox(comboAlignHeading,
						CodeCategories.JUSTIFY, 0, true);
				break;
			case ViewColumnEditor.DATAALIGNMENT:
                editor = new SAFRComboBoxCellEditor(viewer.getGrid(), new String[] {},
                    SWT.READ_ONLY);
                ((SAFRComboBoxCellEditor)editor).setActivationStyle(style);                
				comboAlignData = (CCombo) editor.getControl();
				UIUtilities.populateComboBox(comboAlignData,
						CodeCategories.JUSTIFY, 0, true);
				break;
			case ViewColumnEditor.DATETIMEFORMAT:
                editor = new SAFRComboBoxCellEditor(viewer.getGrid(), new String[] {},
                    SWT.READ_ONLY);
                ((SAFRComboBoxCellEditor)editor).setActivationStyle(style);                
				comboDateTimeFormat = (CCombo) editor.getControl();
				UIUtilities.populateDateTimeComboBox(comboDateTimeFormat, viewColumn.isNumeric(), true);
				break;
			case ViewColumnEditor.RECORDAGGREGATIONFUNCTION:
                editor = new SAFRComboBoxCellEditor(viewer.getGrid(), new String[] {},
                    SWT.READ_ONLY);
                ((SAFRComboBoxCellEditor)editor).setActivationStyle(style);                
				comboRecordAggregation = (CCombo) editor.getControl();
				UIUtilities.populateComboBox(comboRecordAggregation,
						CodeCategories.RECORDAGGR, 0, true);
				break;
			case ViewColumnEditor.GROUPAGGREGATIONFUNCTION:
                editor = new SAFRComboBoxCellEditor(viewer.getGrid(), new String[] {},
                    SWT.READ_ONLY);
                ((SAFRComboBoxCellEditor)editor).setActivationStyle(style);                
				// CQ7432, Neha, Customized populate combo method added as the
				// list will not have code of type "None" and
				// "Group Calculation")
				comboGroupAggregation = (CCombo) editor.getControl();
				if ((view.getOutputFormat() == OutputFormat.Format_Report) && 
				    (view.isFormatPhaseRecordAggregationOn())) {
					populateGroupAggrComboFormatPhaseOn(comboGroupAggregation);
				} else {
					UIUtilities.populateComboBox(comboGroupAggregation,
							CodeCategories.GROUPAGGR, 0, false);
				}
				break;
			case ViewColumnEditor.NUMERICMASK:
			    editor = new SAFRComboBoxCellEditor(viewer.getGrid(), new String[] {},
                    SWT.READ_ONLY);
                ((SAFRComboBoxCellEditor)editor).setActivationStyle(style);                
				comboNumericMask = (CCombo) editor.getControl();
				UIUtilities.populateComboBox(comboNumericMask,
						CodeCategories.FORMATMASK, 0, false);
				break;
			default:
				editor = null;
				break;
			}
			viewColumnEditor.setCurrentCellEditor(editor); 
		} else if (m.getRowEditorType() == RowEditorType.CHECKBOX) {
			editor = new SAFRCheckboxCellEditor(viewer.getGrid());
		} else if (m.getRowEditorType() == RowEditorType.DIALOG) {
			editor = new LogicTextDialogCellEditor(viewer.getGrid(), viewColumnEditor
					.getCurrentColumn(), mediator.getEditor());
			addDoubleClickListener(((SAFRDialogCellEditor) editor).getContents());
		} else {
			editor = null;
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
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .openEditor(new FCCLogicTextEditorInput(viewColumn, mediator.getEditor()), LogicTextEditor.ID);
                } catch (PartInitException pie) {
                    UIUtilities.handleWEExceptions(pie,
                        "Unexpected error occurred while opening Logic Text editor.",null);
                }
            }
        });
    }
    
	
	private void populateGroupAggrComboFormatPhaseOn(
			CCombo comboGroupAggregation) {

		Integer counter = 0;
		comboGroupAggregation.removeAll();
		Integer defaultIndex = 0;

		List<Code> codeList = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.GROUPAGGR).getCodes();
		for (Code code : codeList) {
			if (code.getGeneralId() != Codes.GRPAGGR_GROUP_CALCULATION) {
				comboGroupAggregation.add(code.getDescription());
				comboGroupAggregation.setData(Integer.toString(counter++), code);
				if (code.getGeneralId() == Codes.GRPAGGR_SUM) {
					defaultIndex = counter;
				}
			}
		}
		comboGroupAggregation.select(defaultIndex);
	}

	@Override
	protected Object getValue(Object element) {
		IRowEditingSupport m = (IRowEditingSupport) element;
		Object returnVal = m.getValue(colIndex);
		if (m.getRowEditorType() == RowEditorType.COMBO) {
			Code val;
			CCombo combo;
			val = (Code) returnVal;
			if (val == null) {
				return -1;
			} else {
				combo = (CCombo) editor.getControl();
				return combo.indexOf(val.getDescription());
			}
		}
		if (returnVal == null) {
			return "";
		}
		return returnVal;
	}

	@Override
	protected void setValue(Object element, Object value) {
		IRowEditingSupport m = (IRowEditingSupport) element;
		Boolean dirtyFlag = false;

		if (m.getRowEditorType() == RowEditorType.COMBO) {
			if ((Integer) value >= 0) {
				Code code = (Code) ((CCombo) editor.getControl()).getData(value
						.toString());
				if (!UIUtilities.isEqual(m.getValue(colIndex), code)) {
					dirtyFlag = true;
					m.setValue(colIndex, code);
				}

				if (m.getPropertyID() == ViewColumnEditor.DATATYPE
						|| m.getPropertyID() == ViewColumnEditor.DATETIMEFORMAT) {
					// update Signed field. Signed field not applicable for
					// Binary with Date/Time Format
					viewColumnEditor.updateElement(ViewColumnEditor.SIGNED);

					if (m.getPropertyID() == ViewColumnEditor.DATATYPE) {
						// update Date/Time Format field. Date/Time Format field
						// not applicable for data type Edited Numeric.
						viewColumnEditor.updateElement(ViewColumnEditor.DATETIMEFORMAT);
					}
				}
			} else {
				// nothing selected in combo
				m.setValue(colIndex, null);
			}
		} else if (m.getPropertyID() == ViewColumnEditor.LENGTH
				|| m.getPropertyID() == ViewColumnEditor.SPACEBEFORECOLUMN
				|| m.getPropertyID() == ViewColumnEditor.DECIMALPLACES
				|| m.getPropertyID() == ViewColumnEditor.SCALINGFACTOR) {
			Integer previousIntValue = UIUtilities.stringToInteger(m.getValue(
					colIndex).toString());
			Integer currentIntValue = UIUtilities.stringToInteger(value
					.toString());
			if (!UIUtilities.isEqual(previousIntValue, currentIntValue)) {
				dirtyFlag = true;
				m.setValue(colIndex, value);
			}
		} else if (m.getPropertyID() == ViewColumnEditor.VISIBLE
				|| m.getPropertyID() == ViewColumnEditor.SIGNED) {
			// check whether flag has changed before setting dirty
			if (m.getValue(colIndex) != value) {
				dirtyFlag = true;
			}
			m.setValue(colIndex, value);
		} else if (m.getPropertyID() == ViewColumnEditor.HEAD1
				|| m.getPropertyID() == ViewColumnEditor.HEAD2
				|| m.getPropertyID() == ViewColumnEditor.HEAD3) {
			String previousStringValue = (String) m.getValue(colIndex);
			String currentStringValue = (String) value;
			if (!UIUtilities.isEqual(previousStringValue, currentStringValue) && 
			    (previousStringValue != null || currentStringValue != "") && 
			    (previousStringValue != "" || currentStringValue != null) ) {
				dirtyFlag = true;
				m.setValue(colIndex, value);
				// update sort key header if the column has sort key.
				if (viewColumn.isSortKey()) {
					ViewSortKey viewSortKey = viewColumn.getViewSortKey();
					viewSortKey.setName(getSortKeyHeaderName(viewSortKey));
					viewColumnEditor.updateSortKeyHeader(viewSortKey);
				}
			}
		} else {
			m.setValue(colIndex, value);
		}

		getViewer().update(element, null);

		if (dirtyFlag) {
			mediator.setModified(true);
			if (m.getPropertyID() == ViewColumnEditor.DATATYPE) {
				// update enable/disable state of other rows
				viewColumnEditor.updateDataTypeChangeAffectedRow();
			} else if (m.getPropertyID() == ViewColumnEditor.RECORDAGGREGATIONFUNCTION) {
				if (view.getOutputFormat() == OutputFormat.Format_Report) {
					// update enable/disable state of group aggr function.
					viewColumnEditor.updateElement(ViewColumnEditor.GROUPAGGREGATIONFUNCTION);
				}
			} else if (m.getPropertyID() == ViewColumnEditor.LENGTH
					|| m.getPropertyID() == ViewColumnEditor.SPACEBEFORECOLUMN
					|| m.getPropertyID() == ViewColumnEditor.VISIBLE) {
				// update start position.
				viewColumnEditor.updateElement(ViewColumnEditor.COLUMN_PROP_HEADER);
				viewColumnEditor.updateElement(ViewColumnEditor.STARTPOS);
		        ApplicationMediator.getAppMediator().updateStatusContribution(
		            ApplicationMediator.STATUSBARVIEW, "View Length: " + Integer.toString(view.getViewLength()), true);                
			}
		}
	}

	private String getSortKeyHeaderName(ViewSortKey viewSortKey) {
		ViewColumn vColumn = viewSortKey.getViewColumn();
		String sortKeyname = "";
		if ((vColumn.getHeading1() != null)
				&& (!vColumn.getHeading1().equals(""))) {
			sortKeyname += vColumn.getHeading1();
			if ((vColumn.getHeading2() != null)
					&& (!vColumn.getHeading2().equals(""))) {
				sortKeyname = sortKeyname + " " + vColumn.getHeading2();
			}
			if ((vColumn.getHeading3() != null)
					&& (!vColumn.getHeading3().equals(""))) {
				sortKeyname = sortKeyname + " " + vColumn.getHeading3();
			}
		} else {
			sortKeyname = "Column " + vColumn.getColumnNo();
		}
		return sortKeyname;
	}

	public void setColIndex(int colIndex) {
		this.colIndex = colIndex;
		// change the actual column too.
		this.viewColumn = view.getViewColumns().getActiveItems().get(colIndex);
	}


}
