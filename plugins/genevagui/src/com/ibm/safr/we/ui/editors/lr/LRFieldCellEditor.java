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


import java.util.logging.Logger;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LRFieldKeyType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordFieldEditor.ColumnType;
import com.ibm.safr.we.ui.utilities.SAFRCheckboxCellEditor;
import com.ibm.safr.we.ui.utilities.SAFRComboBoxCellEditor;
import com.ibm.safr.we.ui.utilities.SAFRTextCellEditor;
import com.ibm.safr.we.ui.utilities.TextFocusListener;
import com.ibm.safr.we.ui.utilities.TextModifyListener;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.utilities.VerifyNumericListener;
import com.ibm.safr.we.ui.utilities.WidgetType;

public class LRFieldCellEditor extends EditingSupport {
    
    static final Logger logger = Logger
    .getLogger("com.ibm.safr.we.ui.editors.lr.LRFieldCellEditor");
    
	private CellEditor editor;
	private ColumnType column;
	private GridTableViewer tableViewer;
	private LogicalRecordFieldEditor logicalRecordFieldEditor;
    private CCombo comboRedefines;
	private CCombo comboDataType;
	private CCombo comboDateTimeFormat;
	private CCombo comboAlignHeading;
	private CCombo comboNumericMask;
	private CCombo comboPrimaryKey;
	LogicalRecord logicalRecord = null;
	Text textName;

	private static final int MAXFIELDNAME = 48;
	private static final int MAXFIXEDPOSITION = 9;
	private static final int MAXLENGTH = 5;
	private static final int MAXDECIMALPLACES = 1;
	private static final int MAXSCALING = 2;
	private static final int MAXHEADING = 254;
	private static final int MAXSUBTOTAL = 48;
	private static final int MAXSORTKEYLABEL = 48;
	private static final int MAXCOMMENTS = 254;

	protected LRFieldCellEditor(final GridTableViewer tableViewer, ColumnType type,
			FormToolkit toolkit, LogicalRecordFieldEditor logicalRecordFieldEditor) {
		super(tableViewer);
		this.column = type;
		this.tableViewer = tableViewer;
		this.logicalRecordFieldEditor = logicalRecordFieldEditor;
		logicalRecord = logicalRecordFieldEditor.getLogicalRecord();

		switch (type) {
        case LEVEL:
            // not editable
            break;
		case FIELDNAME:
			editor = new SAFRTextCellEditor(tableViewer.getGrid());
			((Text) editor.getControl()).setTextLimit(MAXFIELDNAME);
			textName = (Text) editor.getControl();
			textName.addModifyListener(new TextModifyListener(textName));
			textName.addFocusListener(new TextFocusListener(textName, false));			
			break;
		case DATATYPE:
			editor = new SAFRComboBoxCellEditor(tableViewer.getGrid(),
					new String[] {}, SWT.READ_ONLY);
			((ComboBoxCellEditor)editor).setActivationStyle(
			        ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
			        ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
			        ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
			        ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);
			comboDataType = (CCombo) editor.getControl();
			UIUtilities.populateComboBox(comboDataType,
					CodeCategories.DATATYPE, 0, true);
			break;
        case REDEFINES:
            // recalc redefines editor each time
            break;
		case FIXEDPOSITION:
			editor = new SAFRTextCellEditor(tableViewer.getGrid());
			((Text) editor.getControl())
					.addVerifyListener(new VerifyNumericListener(
							WidgetType.INTEGER, false));
			((Text) editor.getControl()).setTextLimit(MAXFIXEDPOSITION);
			break;
		case LENGTH:
			editor = new SAFRTextCellEditor(tableViewer.getGrid());
			((Text) editor.getControl())
					.addVerifyListener(new VerifyNumericListener(
							WidgetType.INTEGER, false));
			((Text) editor.getControl()).setTextLimit(MAXLENGTH);
			break;
		case DECIMALPLACES:
			editor = new SAFRTextCellEditor(tableViewer.getGrid());
			((Text) editor.getControl())
					.addVerifyListener(new VerifyNumericListener(
							WidgetType.INTEGER, false));
			((Text) editor.getControl()).setTextLimit(MAXDECIMALPLACES);
			break;
		case PRIMARYKEY:
			editor = new SAFRComboBoxCellEditor(tableViewer.getGrid(),
					new String[] {}, SWT.READ_ONLY);
            ((ComboBoxCellEditor)editor).setActivationStyle(
                    ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
                    ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
                    ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
                    ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);		
			comboPrimaryKey = (CCombo) editor.getControl();
			break;
		case EFFECTIVEDATE:
			editor = new SAFRComboBoxCellEditor(tableViewer.getGrid(),
					LRField.EffectiveDate, SWT.READ_ONLY);
            ((ComboBoxCellEditor)editor).setActivationStyle(
                    ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
                    ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
                    ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
                    ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);			
			break;
		case SCALING:
			editor = new SAFRTextCellEditor(tableViewer.getGrid());
			((Text) editor.getControl())
					.addVerifyListener(new VerifyNumericListener(
							WidgetType.INTEGER, true));
			((Text) editor.getControl()).setTextLimit(MAXSCALING);
			break;
		case DATETIMEFORMAT:
			editor = new SAFRComboBoxCellEditor(tableViewer.getGrid(),
					new String[] {}, SWT.READ_ONLY);
            ((ComboBoxCellEditor)editor).setActivationStyle(
                    ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
                    ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
                    ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
                    ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);			
			comboDateTimeFormat = (CCombo) editor.getControl();
			break;
		case SIGNED:
			editor = new SAFRCheckboxCellEditor(tableViewer.getGrid());
			break;
		case ALIGNHEADING:
			editor = new SAFRComboBoxCellEditor(tableViewer.getGrid(),
					new String[] {}, SWT.READ_ONLY);
            ((ComboBoxCellEditor)editor).setActivationStyle(
                    ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
                    ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
                    ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
                    ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);			
			comboAlignHeading = (CCombo) editor.getControl();
			UIUtilities.populateComboBox(comboAlignHeading,
					CodeCategories.JUSTIFY, 0, true);
			break;
		case HEADING1:
			editor = new SAFRTextCellEditor(tableViewer.getGrid());
			((Text) editor.getControl()).setTextLimit(MAXHEADING);
			break;
		case HEADING2:
			editor = new SAFRTextCellEditor(tableViewer.getGrid());
			((Text) editor.getControl()).setTextLimit(MAXHEADING);
			break;
		case HEADING3:
			editor = new SAFRTextCellEditor(tableViewer.getGrid());
			((Text) editor.getControl()).setTextLimit(MAXHEADING);
			break;
		case NUMERICMASK:
			editor = new SAFRComboBoxCellEditor(tableViewer.getGrid(),
					new String[] {}, SWT.READ_ONLY);
            ((ComboBoxCellEditor)editor).setActivationStyle(
                    ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
                    ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
                    ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
                    ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);			
			comboNumericMask = (CCombo) editor.getControl();
			UIUtilities.populateComboBox(comboNumericMask,
					CodeCategories.FORMATMASK, 0, true);
			break;
		case SORTKEYFOOTERLABEL:
			editor = new SAFRTextCellEditor(tableViewer.getGrid());
			((Text) editor.getControl()).setTextLimit(MAXSUBTOTAL);
			break;
		case SORTKEYLABEL:
			editor = new SAFRTextCellEditor(tableViewer.getGrid());
			((Text) editor.getControl()).setTextLimit(MAXSORTKEYLABEL);
			break;
		case COMMENTS:
			editor = new LRFieldCommentsTextCellEditor(tableViewer.getGrid(),
					tableViewer, logicalRecordFieldEditor, logicalRecord);
			((Text) editor.getControl()).setTextLimit(MAXCOMMENTS);
			break;
        default:
            break;
		}
		if (editor != null && editor.getControl() != null) {
			toolkit.adapt(editor.getControl(), true, true);
		}

	}
	
	@Override
	protected boolean canEdit(Object element) {
		LRField lrField = (LRField) element;
		if (logicalRecordFieldEditor.getEditRights() == EditRights.Read) {
			return false;
		}
		boolean flag = true;
		if (lrField.getDataTypeCode() == null) {
            switch (column) {
            case LEVEL:
            case REDEFINES:
                flag = false;
                break;
            default:
                break;
            }
		}
		else {
			Integer generalId = lrField.getDataTypeCode().getGeneralId();
			switch (column) {
			case DECIMALPLACES:
				flag = (generalId != Codes.ALPHANUMERIC);
				break;
			case SCALING:
				flag = (generalId != Codes.ALPHANUMERIC);
				break;
			case DATETIMEFORMAT:
				flag = (generalId != Codes.EDITED_NUMERIC);
				break;
			case SIGNED:
			    // determine date/time format code
			    Code dateTimeFormat = null;
			    CCombo comboDateTimeFormat = this.logicalRecordFieldEditor.getDateTimeFormatEditor().comboDateTimeFormat;
			    if (comboDateTimeFormat == null || comboDateTimeFormat.getVisible() == false) {
                    dateTimeFormat = lrField.getDateTimeFormatCode();
			    }
			    else {
                    dateTimeFormat = (Code)comboDateTimeFormat.getData(new Integer(
                            comboDateTimeFormat.getSelectionIndex()).toString());
			    }
			    // determine can edit
				if (generalId == Codes.ALPHANUMERIC
						|| (generalId == Codes.BINARY && dateTimeFormat != null)) {
					flag = false;
				}
				break;
			// CQ 6989. Nikita. 20/04/2010. Numeric Mask should be enabled only
			// for Masked Numeric.
			case NUMERICMASK:
				flag = (generalId == Codes.MASKED_NUMERIC);
				break;
            case LEVEL:
            case REDEFINES:
                flag = false;
                break;
            default:
                break;
			}
		}
		return flag;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {	    
        logicalRecordFieldEditor.setCurrentCellEditor(editor);
        if (column == ColumnType.DATETIMEFORMAT) {
            LRField field = (LRField)element;
            UIUtilities.populateDateTimeComboBox((CCombo)editor.getControl(), field.isNumeric() , true);            
        }
		return editor;
	}

	@Override
	protected Object getValue(Object element) {
		LRField lrField = (LRField) element;
		String result = "";
			switch (column) {
			case FIELDNAME:
				result = lrField.getName();
				break;
			case DATATYPE:
				if (lrField.getDataTypeCode() != null) {
					return comboDataType.indexOf(lrField.getDataTypeCode()
							.getDescription());
				}
				return 0;
            case REDEFINES:
                if (lrField.getRedefine() != null) {
                    LRField fld = logicalRecord.findLRField(lrField.getRedefine());
                    if (fld == null) {
                        return 0;                    
                    }
                    else {
                        return comboRedefines.indexOf(fld.getDescriptor());
                    }
                }
                return 0;                
			case FIXEDPOSITION:
			    if (lrField.getPosition() == null) {
			        result = "";
			    }
			    else {
			        result = Integer.toString(lrField.getPosition());
			    }
				break;
			case LENGTH:
				result = Integer.toString(lrField.getLength());
				break;
			case DECIMALPLACES:
				result = Integer.toString(lrField.getDecimals());
				break;
			case PRIMARYKEY:
				populatePrimaryKeyCombo();
				return lrField.getPkeySeqNo();
			case EFFECTIVEDATE:
				if (lrField.getKeyType() == LRFieldKeyType.NONE
						|| lrField.getKeyType() == LRFieldKeyType.PRIMARYKEY) {
					return 0;
				} else if (lrField.getKeyType() == LRFieldKeyType.EFFSTARTDATE) {
					return 1;
				} else if (lrField.getKeyType() == LRFieldKeyType.EFFENDDATE) {
					return 2;
				}
				break;
			case SCALING:
				result = Integer.toString(lrField.getScaling());
				break;
			case DATETIMEFORMAT:
				if (lrField.getDateTimeFormatCode() != null) {
					return comboDateTimeFormat.indexOf(lrField
							.getDateTimeFormatCode().getDescription());
				}
				return comboDateTimeFormat.indexOf("");
			case SIGNED:
				if (lrField.isSigned() == null) {
					return false;
				}
				return lrField.isSigned();
			case ALIGNHEADING:
				if (lrField.getHeaderAlignmentCode() != null) {
					return comboAlignHeading.indexOf(lrField
							.getHeaderAlignmentCode().getDescription());
				}
				return comboAlignHeading.indexOf("");
			case HEADING1:
				result = lrField.getHeading1();
				break;
			case HEADING2:
				result = lrField.getHeading2();
				break;
			case HEADING3:
				result = lrField.getHeading3();
				break;
			case NUMERICMASK:
				if (lrField.getNumericMaskCode() != null) {
					return comboNumericMask.indexOf(lrField
							.getNumericMaskCode().getDescription());
				}
				return comboNumericMask.indexOf("");
			case SORTKEYFOOTERLABEL:
				result = lrField.getSubtotalLabel();
				break;
			case SORTKEYLABEL:
				result = lrField.getSortKeyLabel();
				break;
			case COMMENTS:
				result = lrField.getComment();
				break;
            default:
                break;
			}
		if (result == null) {
			return "";
		}
		return result;
	}

	private void populatePrimaryKeyCombo() {
		comboPrimaryKey.removeAll();			
		int numberOfFields = logicalRecord.getLRFields().getActiveItems()
				.size();
		comboPrimaryKey.add("");
		for (int counter = 0; counter < numberOfFields; counter++) {
			comboPrimaryKey.add(Integer.toString(counter + 1));
		}
	}

	@Override
	protected void setValue(Object element, Object value) {
		LRField editedField = (LRField) element;
		int val = 0;
		int index = tableViewer.getGrid().getSelectionIndex();
		Boolean dirtyFlag = false;
		try {
			switch (column) {
			case FIELDNAME:
				if (editedField.getName() == null && value != "") {
					dirtyFlag = true;
				} else if (editedField.getName() == null && value == "") {
					dirtyFlag = false;
				} else {
					if (!editedField.getName().equals((String) value)) {
						dirtyFlag = true;
					}
				}
				if (dirtyFlag == true) {
					editedField.setName((String) value);
                    tableViewer.refresh();
				}
				break;
			case DATATYPE:
				val = (Integer) value;
				if (val < 0) {
					val = 0;
				}
				if (editedField.getDataTypeCode() == null && val > 0) {
					dirtyFlag = true;
				} else if (editedField.getDataTypeCode() == null && val == 0) {
					dirtyFlag = false;
					editedField.setDataTypeCode(null);
				} else {

					Code dataType = ((Code) comboDataType.getData(Integer.toString(val)));
					if (!editedField.getDataTypeCode().equals(dataType)) {
						dirtyFlag = true;

					}
				}
				if (dirtyFlag == true) {
					Code dataType = ((Code) comboDataType.getData(Integer.toString(val)));
					if (dataType != null) {
						if (dataType.getGeneralId() == Codes.ALPHANUMERIC) {
							editedField.setDecimals(0);
							editedField.setScaling(0);
							editedField.setSigned(false);
						} else if (dataType.getGeneralId() == Codes.EDITED_NUMERIC) {
							editedField.setDateTimeFormatCode(null);
						} else if (dataType.getGeneralId() == Codes.BINARY
								&& editedField.getDateTimeFormatCode() != null) {
							editedField.setSigned(false);
						}
						// CQ 6989. Nikita. 20/04/2010. Numeric Mask should be
						// enabled only for Masked Numeric.
						if (dataType.getGeneralId() != Codes.MASKED_NUMERIC) {
							editedField.setNumericMaskCode(null);
						}
					}
					editedField.setDataTypeCode(dataType);
				}
				break;
            case REDEFINES:
                val = (Integer) value;
                if (val < 0) {
                    return;                                        
                }           
                String valStr = Integer.toString(val);
                if (editedField.isRedefineSet()) {
                    LRField fld = (LRField) comboRedefines.getData(valStr);
                    if (fld == null) {
                        editedField.setRedefine(null);
                        dirtyFlag = true;
                    } else {                    
                        if (!editedField.getRedefine().equals(fld.getId())) {
                            editedField.setRedefine(fld.getId());
                            dirtyFlag = true;
                        }
                    }
                    
                } else {
                    LRField fld = ((LRField) comboRedefines.getData(valStr));                
                    if (fld != null) {
                        editedField.setRedefine(fld.getId());
                        dirtyFlag = true;
                    }
                }
                logicalRecordFieldEditor.expandToShowField(editedField);
                tableViewer.refresh();
                logicalRecordFieldEditor.calculateFieldCount();                
                break;
            case FIXEDPOSITION:
                if (editedField.getPosition() == null && value != "") {
                    dirtyFlag = true;
                } else if (editedField.getPosition() != null || value != "") {
                    if (value == "") {
                        value = 0;
                    }
                    if (!editedField.getPosition().equals(
                            Integer.parseInt(value.toString()))) {
                        dirtyFlag = true;
                    }
                }
                if (dirtyFlag) {
                    editedField.setPosition(Integer.parseInt(value.toString()));
                    logicalRecordFieldEditor.calculateFieldCount();
                    tableViewer.refresh();
                }
                else {
                    dirtyFlag = logicalRecord.autocalcRedefine();
                    if (dirtyFlag) {
                        logicalRecordFieldEditor.calculateFieldCount();
                        tableViewer.refresh();                        
                    }
                }
                break;
			case LENGTH:
                if (editedField.getLength() == null && value != "") {
                    dirtyFlag = true;
                } else if (editedField.getLength() != null || value != "") {
                    if (value == "") {
                        value = 0;
                    }
                    if (!editedField.getLength().equals(
                            Integer.parseInt(value.toString()))) {
                        dirtyFlag = true;
                    }
                }
                if (dirtyFlag) {
                    editedField.setLength(Integer.parseInt(value.toString()));
                    logicalRecordFieldEditor.calculateFieldCount();
                    tableViewer.refresh();                
                }
                else {
                    dirtyFlag = logicalRecord.autocalcRedefine();
                    if (dirtyFlag) {
                        logicalRecordFieldEditor.calculateFieldCount();
                        tableViewer.refresh();                        
                    }
                }
				break;
			case DECIMALPLACES:
				if (editedField.getDecimals() == null && value != "") {
					dirtyFlag = true;
				} else if (editedField.getDecimals() == null && value == "") {
					dirtyFlag = false;
				} else {
					if (value == "") {
						value = 0;
					}
					if (!editedField.getDecimals().equals(
							Integer.parseInt(value.toString()))) {
						dirtyFlag = true;
					}
				}
				if (dirtyFlag == true) {
					editedField.setDecimals(Integer.parseInt(value.toString()));
				}
				break;
			case PRIMARYKEY:
				val = (Integer) value;
				if (val < 0) {
					val = 0;
				}
				if (!editedField.getPkeySeqNo().equals(val)) {
					if (val > 0) {
						editedField.setKeyType(LRFieldKeyType.PRIMARYKEY);
						tableViewer.getGrid().getItem(index).setForeground(
								Display.getCurrent().getSystemColor(
										SWT.COLOR_RED));
					} else {
						editedField.setKeyType(LRFieldKeyType.NONE);
						tableViewer.getGrid().getItem(index)
								.setForeground(null);
					}
					editedField.setPkeySeqNo(val);
					dirtyFlag = true;
					logicalRecordFieldEditor.calculateFieldCount();
				}
				break;
			case EFFECTIVEDATE:
				val = (Integer) value;
				if (val < 0) {
					val = 0;
				}
				if (val == 0
						&& editedField.getKeyType() == LRFieldKeyType.PRIMARYKEY) {

				} else if (editedField.getKeyType().ordinal() != val) {
					editedField.setKeyType(LRFieldKeyType.values()[val]);
					tableViewer.getGrid().getItem(index).setForeground(null);
					dirtyFlag = true;
				}
				tableViewer.refresh();
				break;
			case SCALING:
				if (editedField.getScaling() == null && value != "") {
					dirtyFlag = true;
				} else if (editedField.getScaling() == null && value == "") {
					dirtyFlag = false;
				} else {
					if (value == "") {
						value = 0;
					}
					if (!editedField.getScaling().equals(
							Integer.parseInt(value.toString()))) {
						dirtyFlag = true;
					}
				}
				if (dirtyFlag == true) {
					editedField.setScaling(Integer.parseInt(value.toString()));
				}
				break;
			case DATETIMEFORMAT:
			    val = (Integer) value;
				if (val < 0) {
					val = 0;
				}
				if (editedField.getDateTimeFormatCode() == null && val > 0) {
					dirtyFlag = true;
				} else if (editedField.getDateTimeFormatCode() == null && val == 0) {
					dirtyFlag = false;
					editedField.setDateTimeFormatCode(null);
				} else {
					if (!editedField.getDateTimeFormatCode().equals(
							(Code) comboDateTimeFormat.getData(Integer
									.toString(val)))) {
						dirtyFlag = true;
					}
				}
				if (dirtyFlag == true) {
					if (editedField.getDataTypeCode() != null) {
						if ((editedField.getDataTypeCode().getGeneralId() == Codes.BINARY)
								&& ((Code) comboDateTimeFormat.getData(Integer.toString(val)) != null)) {
							editedField.setSigned(false);
						}
					}
					editedField
							.setDateTimeFormatCode((Code) comboDateTimeFormat
									.getData(Integer.toString(val)));
				}
				break;
			case SIGNED:
				if (editedField.isSigned() != (Boolean) value) {
					editedField.setSigned((Boolean) value);
					dirtyFlag = true;
				}
				break;
			case ALIGNHEADING:
				val = (Integer) value;
				if (editedField.getHeaderAlignmentCode() == null && val > 0) {
					dirtyFlag = true;
				} else if (editedField.getHeaderAlignmentCode() == null
						&& (Integer) value <= 0) {
					dirtyFlag = false;
					editedField.setHeaderAlignmentCode(null);
				} else {
					if (!editedField.getHeaderAlignmentCode().equals(
							(Code) comboAlignHeading.getData(Integer
									.toString(val)))) {
						dirtyFlag = true;
					}
				}
				if (dirtyFlag == true) {
					editedField.setHeaderAlignmentCode((Code) comboAlignHeading
							.getData(Integer.toString(val)));
				}
				break;
			case HEADING1:
				if (editedField.getHeading1() == null && value != "") {
					dirtyFlag = true;
				} else if (editedField.getHeading1() == null && value == "") {
					dirtyFlag = false;
				} else {
					if (!editedField.getHeading1().equals((String) value)) {
						dirtyFlag = true;
					}
				}
				if (dirtyFlag == true) {
					editedField.setHeading1((String) value);
				}
				break;
			case HEADING2:
				if (editedField.getHeading2() == null && value != "") {
					dirtyFlag = true;
				} else if (editedField.getHeading2() == null && value == "") {
					dirtyFlag = false;
				} else {
					if (!editedField.getHeading2().equals((String) value)) {
						dirtyFlag = true;
					}
				}
				if (dirtyFlag == true) {
					editedField.setHeading2((String) value);
				}
				break;
			case HEADING3:
				if (editedField.getHeading3() == null && value != "") {
					dirtyFlag = true;
				} else if (editedField.getHeading3() == null && value == "") {
					dirtyFlag = false;
				} else {
					if (!editedField.getHeading3().equals((String) value)) {
						dirtyFlag = true;
					}
				}
				if (dirtyFlag == true) {
					editedField.setHeading3((String) value);
				}
				break;
			case NUMERICMASK:
				val = (Integer) value;
				if (editedField.getNumericMaskCode() == null && val > 0) {
					dirtyFlag = true;
				} else if (editedField.getNumericMaskCode() == null
						&& (Integer) value <= 0) {
					dirtyFlag = false;
					editedField.setNumericMaskCode(null);
				} else {
					if (!editedField.getNumericMaskCode().equals(
							(Code) comboNumericMask.getData(Integer
									.toString(val)))) {
						dirtyFlag = true;
					}
				}
				if (dirtyFlag == true) {
					editedField.setNumericMaskCode((Code) comboNumericMask
							.getData(Integer.toString(val)));
				}
				break;
			case SORTKEYFOOTERLABEL:
				if (editedField.getSubtotalLabel() == null && value != "") {
					dirtyFlag = true;
				} else if (editedField.getSubtotalLabel() == null
						&& value == "") {
					dirtyFlag = false;
				} else {
					if (!editedField.getSubtotalLabel().equals((String) value)) {
						dirtyFlag = true;
					}
				}
				if (dirtyFlag == true) {
					editedField.setSubtotalLabel((String) value);
				}
				break;
			case SORTKEYLABEL:
				if (editedField.getSortKeyLabel() == null && value != "") {
					dirtyFlag = true;
				} else if (editedField.getSortKeyLabel() == null && value == "") {
					dirtyFlag = false;
				} else {
					if (!editedField.getSortKeyLabel().equals((String) value)) {
						dirtyFlag = true;
					}
				}
				if (dirtyFlag == true) {
					editedField.setSortKeyLabel((String) value);
				}
				break;
			case COMMENTS:
				if (editedField.getComment() == null && value != "") {
					dirtyFlag = true;
				} else if (editedField.getComment() == null && value == "") {
					dirtyFlag = false;
				} else {
					if (!editedField.getComment().equals((String) value)) {
						dirtyFlag = true;
					}
				}
				if (dirtyFlag == true) {
					editedField.setComment((String) value);
				}
				break;
            default:
                break;
			}
		} catch (SAFRValidationException e) {
            UIUtilities.handleWEExceptions(e,"Validation exception changing Logical Record Field",null);
        } catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e,"Error getting field values � DateTimeFormat,HeaderAlignment,NumericMask.",null);
		}
		getViewer().update(element, null);
		if (dirtyFlag) {
			logicalRecordFieldEditor.setDirty(true);
		}
	}	
}
