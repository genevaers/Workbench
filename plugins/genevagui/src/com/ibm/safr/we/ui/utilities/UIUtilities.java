package com.ibm.safr.we.ui.utilities;

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


import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.ISourceProviderService;
import org.osgi.framework.Bundle;
import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.NumericIdQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.SAFRQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.security.UserSession;
import com.ibm.safr.we.ui.Application;
import com.ibm.safr.we.ui.commands.SourceProvider;
import com.ibm.safr.we.ui.editors.ControlRecordEditor;
import com.ibm.safr.we.ui.editors.ControlRecordEditorInput;
import com.ibm.safr.we.ui.editors.EnvironmentEditor;
import com.ibm.safr.we.ui.editors.EnvironmentEditorInput;
import com.ibm.safr.we.ui.editors.GroupEditor;
import com.ibm.safr.we.ui.editors.GroupEditorInput;
import com.ibm.safr.we.ui.editors.LogicalFileEditor;
import com.ibm.safr.we.ui.editors.LogicalFileEditorInput;
import com.ibm.safr.we.ui.editors.LookupPathEditor;
import com.ibm.safr.we.ui.editors.LookupPathEditorInput;
import com.ibm.safr.we.ui.editors.UserEditor;
import com.ibm.safr.we.ui.editors.UserEditorInput;
import com.ibm.safr.we.ui.editors.UserExitRoutineEditor;
import com.ibm.safr.we.ui.editors.UserExitRoutineEditorInput;
import com.ibm.safr.we.ui.editors.ViewFolderEditor;
import com.ibm.safr.we.ui.editors.ViewFolderEditorInput;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditor;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditorInput;
import com.ibm.safr.we.ui.editors.pf.PhysicalFileEditor;
import com.ibm.safr.we.ui.editors.pf.PhysicalFileEditorInput;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.MainTableLabelProvider;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * A utility class which will be used by functions in the User Interface layer.
 * 
 */

public class UIUtilities {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.ui.utilities.UIUtilities");
	
	public static final int MAXNAMECHAR = 48;
	public static final int MAXCOMMENTCHAR = 254;
	public static final int VERTICALSPACING = 10;
	public static final int HORIZONTALSPACING = 10;
	public static final int TOPMARGIN = 10;
	public static final int BOTTOMMARGIN = 10;
	public static final int LEFTMARGIN = 10;
	public static final int RIGHTMARGIN = 10;
	public static final String BLANK_VALUE = "";
	public static final int TABLECELLLIMIT = 512;
	public static final String titleStringDbException = "Unexpected Database Error";
	public static final String titleStringNotFoundException = "Metadata Component Not Found";
	public static final String SORT_CATEGORY = "AssociatedComponent";
	private static Map<String, Image> images = new HashMap<String, Image>();

	/**
	 * Utility method to populate a combo box from a {@link CodeSet}. Every item
	 * in the combo box will have a {@link Code} object associated with it. This
	 * {@link Code} object can be retrieved using the <code>getData(Key)</code>
	 * method of the combo box. <code>Key</code> here is the selected index of
	 * the combo box.
	 * 
	 * @param <b>comboBox</b> the {@link Combo} control to be populated
	 * @param <b>category</b> the code category
	 * @param defaultSelected
	 *            the default value to be displayed in the {@link Combo}
	 * @param allowBlank
	 *            a boolean parameter which determines whether the user can
	 *            select a blank value in the {@link Combo}
	 */
	public static void populateComboBox(Combo comboBox, String category,
			int defaultSelected, Boolean allowBlank) {
		Integer counter = 0;
		comboBox.removeAll();
		if (allowBlank) {
			// Allow user to select a blank value
			comboBox.add("");
			comboBox.setData(Integer.toString(counter++), null);
		}
		List<Code> codeList = SAFRApplication.getSAFRFactory().getCodeSet(
				category).getCodes();
		for (Code code : codeList) {
			comboBox.add(code.getDescription());
			comboBox.setData(Integer.toString(counter++), code);
		}
		if (defaultSelected >= 0) {
			comboBox.select(defaultSelected);
		}
	}

	/**
	 * Utility method to populate a CCombo box from a {@link CodeSet}. Every
	 * item in the CCombo box will have a {@link Code} object associated with
	 * it. This {@link Code} object can be retrieved using the
	 * <code>getData(Key)</code> method of the CCombo box. <code>Key</code> here
	 * is the selected index of the CCombo box.
	 * 
	 * @param <b>comboBox</b> the {@link CCombo} control to be populated
	 * @param <b>category</b> the code category
	 * @param defaultSelected
	 *            the default value to be displayed in the {@link CCombo}
	 * @param allowBlank
	 *            a boolean parameter which determines whether the user can
	 *            select a blank value in the {@link CCombo}
	 */

	public static void populateComboBox(CCombo comboBox, String category,
			int defaultSelected, Boolean allowBlank) {
		Integer counter = 0;
		comboBox.removeAll();
		if (allowBlank) {
			// Allow user to select a blank value
			comboBox.add("");
			comboBox.setData(Integer.toString(counter++), null);
		}
		List<Code> codeList = SAFRApplication.getSAFRFactory().getCodeSet(
				category).getCodes();
		for (Code code : codeList) {
			comboBox.add(code.getDescription());
			comboBox.setData(Integer.toString(counter++), code);

		}
		if (defaultSelected >= 0) {
			comboBox.select(defaultSelected);
		}
	}

    /**
     * Populate DateTime format combo
     * @param comboBox
     * @param category
     * @param defaultSelected
     * @param allowBlank
     */
	
    public static void populateDateTimeComboBox(CCombo comboBox, boolean numeric, boolean allowBlank) {
        int counter = 0;
        comboBox.removeAll();
        if (allowBlank) {
            // Allow user to select a blank value
            comboBox.add("");
            comboBox.setData(Integer.toString(counter++), null);
        }
        List<Code> codeList = SAFRApplication.getSAFRFactory().
            getCodeSet(CodeCategories.FLDCONTENT).getCodes();
        for (Code code : codeList) {
            if (!numeric || 
                code.getDescription().matches("\\p{Alpha}+")) {
                comboBox.add(code.getDescription());
                comboBox.setData(Integer.toString(counter++), code);                    
            }
        }
    }

    public static void populateDateTimeComboBox(Combo comboBox, boolean numeric, boolean allowBlank) {
        int counter = 0;
        comboBox.removeAll();
        if (allowBlank) {
            // Allow user to select a blank value
            comboBox.add("");
            comboBox.setData(Integer.toString(counter++), null);
        }
        List<Code> codeList = SAFRApplication.getSAFRFactory().
            getCodeSet(CodeCategories.FLDCONTENT).getCodes();
        for (Code code : codeList) {
            if (!numeric || 
                code.getDescription().matches("\\p{Alpha}+")) {
                comboBox.add(code.getDescription());
                comboBox.setData(Integer.toString(counter++), code);                    
            }
        }
    }
    
	/**
	 * Utility method to populate a combo box with the list of environments
	 * 
	 * @param comboBox
	 *            the {@link Combo} control to be populated
	 * @param allowBlank
	 *            a boolean parameter which determines whether the user can
	 *            select a blank value in the {@link Combo}
	 */
	public static void populateEnvironment(Combo comboBox, Boolean allowBlank,
			User user) {
		Integer counter = 0;
		try {
			comboBox.removeAll();
			if (allowBlank) {
				// Allow user to select a blank value
				comboBox.add("");
				comboBox.setData(Integer.toString(counter++), null);
			}

			List<EnvironmentQueryBean> envList;
			if (user == null) {
				envList = SAFRQuery.queryAllEnvironments(SortType.SORT_BY_NAME);
			} else {
				envList = SAFRQuery.queryAllEnvironments(SortType.SORT_BY_NAME,
						user);
			}

			for (EnvironmentQueryBean environment : envList) {
				comboBox.add(getComboString(environment.getName(), environment
						.getId()));
				comboBox.setData(Integer.toString(counter++), environment);
			}
		} catch (DAOException e) {
			UIUtilities.handleWEExceptions(e, null,
					UIUtilities.titleStringDbException);
		}
	}

	/**
	 * Utility method to populate a combo box with the list of view folders
	 * 
	 * @param comboViewer
	 *            the {@link TableComboViewer} control to be populated
	 * @param environmentId
	 *            ID of the environment, whose view folders need to be displayed
	 *            in the {@link TableCombo}
	 */
	public static void populateViewFolder(TableComboViewer comboViewer,
			Integer environmentId) {
		TableCombo comboBox = comboViewer.getTableCombo();

		try {
			comboBox.getTable().removeAll();

			List<ViewFolderQueryBean> viewFolderList = SAFRQuery
					.queryAllViewFolders(environmentId, SortType.SORT_BY_NAME);

			comboViewer.setContentProvider(new ArrayContentProvider());

			MainTableLabelProvider labelProvider = new MainTableLabelProvider(
					ComponentType.ViewFolder) {
				public String getColumnText(Object element, int columnIndex) {

					switch (columnIndex) {
					case 2:
						NumericIdQueryBean bean = (NumericIdQueryBean) element;
						return UIUtilities.getComboString(bean.getName(), bean
								.getId());
					default:
						return super.getColumnText(element, columnIndex);
					}
				}
			};
			labelProvider.setInput();
			comboViewer.setLabelProvider(labelProvider);

			for (int iCounter = 0; iCounter < 2; iCounter++) {
				ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(iCounter, comboViewer, ComponentType.ViewFolder);
				comboBox.getTable().getColumn(iCounter).addSelectionListener(colListener);
			}

			comboViewer.setInput(viewFolderList);

		} catch (DAOException e) {
			UIUtilities.handleWEExceptions(e,
					"Error occurred while retrieving all view folders.",
					UIUtilities.titleStringDbException);
		}
	}

	/**
	 * Utility method to create a {@link TableWrapData} object. The
	 * {@link TableWrapData} object will have the following properties: 1.
	 * Horizontal alignment = FILL_GRAB 2. Maximum width = 200
	 * 
	 * @param colspan
	 *            int colspan for the returned {@link TableWrapData} object.
	 * @return {@link TableWrapData} object.
	 */
	public static TableWrapData textTableData(int colspan) {
		TableWrapData tempData = new TableWrapData(TableWrapData.FILL_GRAB);
		tempData.maxWidth = 200;
		tempData.colspan = colspan;
		return tempData;
	}

	/**
	 * Utility method to create a {@link TableWrapData} object for a multi-line
	 * textbox. The {@link TableWrapData} object will have the following
	 * properties: 1. Horizontal alignment = FILL 2. Vertical alignment = FILL
	 * 3. Height = as specified in the parameter passed. 4. Maximum width = 200
	 * 
	 * @param rowspan
	 *            int rowspan for the returned {@link TableWrapData} object.
	 * @param colspan
	 *            int colspan for the returned {@link TableWrapData} object.
	 * @param height
	 *            int height for the returned {@link TableWrapData} object.
	 * @return {@link TableWrapData} object.
	 */
	public static TableWrapData multiLineTextData(int rowspan, int colspan,
			int height) {
		TableWrapData tempData = new TableWrapData(TableWrapData.FILL,
				TableWrapData.FILL, rowspan, colspan);
		tempData.heightHint = height;
		tempData.maxWidth = 200;
		return tempData;

	}

	/**
	 * Utility method to create a {@link TableWrapLayout} object. The
	 * {@link TableWrapLayout} object will have the following properties: 1.
	 * Vertical spacing = as specified by the constant VERTICALSPACING. 2.
	 * Horizontal spacing = as specified by the constant HORIZONTALSPACING. 3.
	 * number of columns = as specified by the parameter passed. 4. Columns will
	 * be of either equal or unequal width depending on the boolean parameter
	 * passed.
	 * 
	 * @param numcols
	 *            the number of columns for the returned {@link TableWrapLayout}
	 *            object.
	 * @param equalWidth
	 *            boolean parameter which determines whether or not the columns
	 *            of the {@link TableWrapLayout} object must be of equal width.
	 * @return {@link TableWrapLayout} object.
	 */
	public static TableWrapLayout createTableLayout(int numcols,
			Boolean equalWidth) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.verticalSpacing = VERTICALSPACING;
		layout.horizontalSpacing = HORIZONTALSPACING;
		layout.numColumns = numcols;
		layout.makeColumnsEqualWidth = equalWidth;
		return layout;
	}

	/**
	 * Utility method to set the text of a textbox if the string to be set is
	 * not null
	 * 
	 * @param textBox
	 *            the textbox whose text is to be set
	 * @param value
	 *            the string to be set
	 */
	public static void checkNullText(Text textBox, String value) {
		if ((null != value)) {
			textBox.setText(value);
		} else {
			textBox.setText("");
		}
	}

	/**
	 * Utility method to set the text of a combobox if the Code object whose
	 * description is to be set is not null
	 * 
	 * @param comboBox
	 *            the combobox whose text is to be set
	 * @param code
	 *            the Code object whose description needs to be set as the
	 *            combobox's text
	 */
	public static void checkNullCombo(Combo comboBox, Code code) {
		if (null != code) {
			comboBox.setText(code.getDescription());
		} else {
			comboBox.deselectAll();
		}
	}

	/**
	 * Utility method to retrieve the {@link Code} object associated with the
	 * value selected in a combo box.
	 * 
	 * @param combo
	 *            the {@link Combo} control whose value is being selected
	 * @return {@link Code} object.
	 */
	public static Code getCodeFromCombo(Combo combo) {
		Code returnCode = null;
		if (combo.getSelectionIndex() >= 0) {
			String key;
			key = String.valueOf(combo.getSelectionIndex());
			returnCode = (Code) combo.getData(key);
		}
		return returnCode;
	}

	/**
	 * Retrieve {@link Date} from SWT {@link DateTime} control.
	 * 
	 * @param datePicker
	 *            the SWT {@link DateTime} control
	 * @return {@link Date}
	 */
	public static Date dateFromPicker(DateTime datePicker) {
		Date returnDate;
		Calendar calender = new GregorianCalendar();
		calender.set(datePicker.getYear(), datePicker.getMonth(), datePicker
				.getDay());
		returnDate = calender.getTime();
		return returnDate;
	}

	/**
	 * Sets the value of {@link DateTime} control to the supplied Date.
	 * 
	 * @param datePicker
	 *            the SWT {@link DateTime} control.
	 * @param date
	 *            the {@link Date} object.
	 */
	public static void setDateInPicker(DateTime datePicker, Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		datePicker.setDate(calendar.get(Calendar.YEAR), calendar
				.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * Convert string to {@link Integer}.
	 * 
	 * @param string
	 *            to be converted to {@link Integer}.
	 * @return {@link Integer}.
	 */
	public static Integer stringToInteger(String string) {
		Integer integerValue;
		try {
			integerValue = Integer.parseInt(string);
			return integerValue;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * @return the ID of the {@link Environment} used for current session.
	 */
	public static Integer getCurrentEnvironmentID() {
		return SAFRApplication.getUserSession().getEnvironment().getId();
	}

	/**
	 * This method formats specified date into dd-MMM-yyyy format.If specified
	 * date is null then returns BLANK_VALUE defined in {@link UIUtilities}.
	 * 
	 * @param date
	 *            to format.
	 * @return date in format dd-MMM-yyyy. if date is null then returns
	 *         BLANK_VALUE defined in {@link UIUtilities}.
	 */
	public static String formatDate(Date date) {
		if (date == null) {
			return BLANK_VALUE;
		} else {
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
			return formatter.format(date);
		}
	}

	/**
	 * Utility function to compare two String objects. Useful if any or both the
	 * Strings are null.
	 * 
	 * @param s1
	 *            String one could be null
	 * @param s2
	 *            String two could be null
	 * @return int comparision result. '0' if s1 is equal to s2, negative number
	 *         if s1 is less then s2, positive number if s1 is greater then s2
	 */
	public static int compareStrings(String s1, String s2) {
		int ret = compareNulls(s1, s2);
		if (ret == 2) { // no nulls, so compare
			ret = s1.compareToIgnoreCase(s2);
		}
		return ret;
	}

	/**
	 * Utility function to compare two Integer objects. Useful if any or both
	 * the Integer objects are null.
	 * 
	 * @param l1
	 *            Integer object one could be null
	 * @param l2
	 *            Integer object two could be null
	 * @return int comparision result. '0' if l1 is equal to l2, negative number
	 *         if l1 is less then l2, positive number if l1 is greater then l2
	 */
	public static int compareIntegers(Integer l1, Integer l2) {
		int ret = compareNulls(l1, l2);
		if (ret == 2) { // no nulls, so compare
			ret = l1.compareTo(l2);
		}
		return ret;
	}

	/**
	 * Utility function to compare two Date objects. Useful if any or both the
	 * Date objects are null.
	 * 
	 * @param d1
	 *            Date object one could be null
	 * @param d2
	 *            Date object two could be null
	 * @return int comparision result. '0' if d1 is equal to d2, negative number
	 *         if d1 is less then d2, positive number if d1 is greater then d2
	 */
	public static int compareDates(Date d1, Date d2) {
		int ret = compareNulls(d1, d2);
		if (ret == 2) { // no nulls, so compare
			ret = d1.compareTo(d2);
		}
		return ret;
	}

	/**
	 * Utility function to compare two Boolean objects. Useful if any or both
	 * the Boolean objects are null.
	 * 
	 * @param b1
	 *            Boolean object one could be null
	 * @param b2
	 *            Boolean object two could be null
	 * @return int comparision result. '0' if b1 is equal to b2, negative number
	 *         if b1 is less then b2, positive number if b1 is greater then b2
	 */
	public static int compareBooleans(Boolean b1, Boolean b2) {
		int ret = compareNulls(b1, b2);
		if (ret == 2) { // no nulls, so compare
			ret = b1.compareTo(b2);
		}
		return ret;
	}

	/**
	 * Utility function to compare two objects, if any or both of them are null.
	 * 
	 * @param o1
	 *            Object one could be null.
	 * @param o2
	 *            Object two could be null.
	 * @return int comparison result. '0' if both the objects are null, '-1' if
	 *         only o1 is null, '1' if only o2 is null and '2' if both are
	 *         non-null.
	 */
	private static int compareNulls(Object o1, Object o2) {
		int ret = 2;
		if (o1 == null && o2 == null) {
			ret = 0;
		} else if (o1 == null) {
			ret = -1;
		} else if (o2 == null) {
			ret = 1;
		}
		return ret;
	}

	/**
	 * Compares two objects and checks if they are equal or not.
	 * 
	 * @param o1
	 *            first object to compare.
	 * @param o2
	 * @return
	 */
	public static boolean isEqual(Object o1, Object o2) {
		if (o1 != null) {
			return o1.equals(o2);
		} else if (o2 == null) {
			return true; // both are null
		} else {
			return false; // o2 is not null
		}
	}

    /**
     * Compares two strings and checks if they are equal or not.
     * 
     * @param o1
     *            first string to compare.
     * @param o2
     * @return
     */
    public static boolean isEqualString(String o1, String o2) {
        if (o1 == null) {
            if (o2 == null) {
                return true;
            }
            else if (o2.isEmpty()) {
                return true;
            }
            else {
                return false;
            }            
        }
        else if (o2 == null) {
            if (o1.isEmpty()) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return o1.equals(o2);
        }
    }

    /**
     * Compares two integers and checks if they are equal or not.
     * 
     * @param o1
     *            first int to compare.
     * @param o2
     * @return
     */
    public static boolean isEqualInt(Integer o1, Integer o2) {
        if (o1 == null) {
            if (o2 == null) {
                return true;
            }
            else if (o2.equals(0)) {
                return true;
            }
            else {
                return false;
            }            
        }
        else if (o2 == null) {
            if (o1.equals(0)) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return o1.equals(o2);
        }
    }
    
	/**
	 * Compares two SAFRQueryBean objects and checks if they are equal or not.
	 * 
	 * @param o1
	 *            first object to compare.
	 * @param o2
	 * @return
	 */
	public static boolean isEqualSAFRQueryBean(SAFRQueryBean o1,
			SAFRQueryBean o2) {
		if (o1 == null && o2 == null) {
			return true;
		} else if (o1 != null && o2 != null) {
			return o1.getIdLabel().equalsIgnoreCase(o2.getIdLabel());
		} else {
			return false;
		}
	}

	/**
	 * Compares two SAFRComponent objects to check if they are equal or not
	 * based on their Ids
	 * 
	 * @param o1
	 *            first object to compare
	 * @param o2
	 *            second object to compare
	 * @return
	 */
	public static boolean isEqualSAFRComponent(SAFRComponent o1,
			SAFRComponent o2) {
		if (o1 == null && o2 == null) {
			return true;
		} else if (o1 != null && o2 != null) {
			return o1.getId().equals(o2.getId());
		} else {
			return false;
		}
	}


	/**
	 * Selects an item from combo box matching the SAFR Component. The ID and
	 * Name of the component are used to match an item in Combo.
	 * 
	 * @param combo
	 *            the combo to select an item from.
	 * @param component
	 *            the component to select in the combo.
	 */
	public static void selectComponentInCombo(TableCombo combo,SAFRComponent component) {
		if (component == null) {
			combo.setText("");
			return;
		}
		String comboStr = UIUtilities.getComboString(component.getName(), component.getId());	
        combo.setText(comboStr);		
	}

	public static String getComboString(String name, Integer id) {
		if (id != null && id > 0) {
			// CQ8888 Kanchan Rauthan 29/11/2010. Not to show null in the combo
			// text.
			if (name == null)
				name = BLANK_VALUE;
			return (name + " [" + Integer.toString(id) + "]");
		}
		return BLANK_VALUE;
	}

	public static Image getImageDescriptor(String key) {
		if (images.containsKey(key)) {
			return images.get(key);
		} else {
			images.put(key, AbstractUIPlugin.imageDescriptorFromPlugin(
					Application.PLUGIN_ID, key).createImage());
			return images.get(key);
		}
	}

	public static void disposeImages() {
		// dispose all cached images
		for (String key : images.keySet()) {
			images.get(key).dispose();
		}
	}

    public static void prepareTableViewerForShortList(TableViewer tabViewer, ComponentType type) {
        prepareTableViewerForShortList(tabViewer,type, null);
    }
	
	public static void prepareTableViewerForShortList(TableViewer tabViewer, ComponentType type, Integer nameWidth) {
		Table table = tabViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

        String[] columnHeaders = new String[] { "ID", "Name" };
        int[] columnWidths = new int[] { 100, 300 };
        if (nameWidth != null) {
            columnWidths[1] = nameWidth;
        }
		int length = columnWidths.length;
		for (int iCounter = 0; iCounter < length; iCounter++) {
			TableViewerColumn column = new TableViewerColumn(tabViewer,	SWT.NONE);
			column.getColumn().setText(columnHeaders[iCounter]);
			column.getColumn().setToolTipText(columnHeaders[iCounter]);
			column.getColumn().setWidth(columnWidths[iCounter]);
			column.getColumn().setResizable(true);
			ColumnSelectionListener colListener = new ColumnSelectionListener(
					tabViewer, iCounter, type);
			column.getColumn().addSelectionListener(colListener);
		}
	}

	private static class ColumnSelectionListener extends SelectionAdapter {
		private int colNumber;
		private TableViewer tabViewer;
		private ComponentType type;

		private ColumnSelectionListener(TableViewer tabViewer, int colNumber, ComponentType type) {
			this.colNumber = colNumber;
			this.tabViewer = tabViewer;
			this.type = type;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Shell shell = Display.getCurrent().getActiveShell();
			try {
				shell.setCursor(shell.getDisplay().getSystemCursor(
						SWT.CURSOR_WAIT));
				TableColumn sortColumn = tabViewer.getTable().getSortColumn();
				TableColumn currentColumn = (TableColumn) e.widget;
				int dir = tabViewer.getTable().getSortDirection();
				if (sortColumn == currentColumn) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					tabViewer.getTable().setSortColumn(currentColumn);
					// if the currentColumn is the column for ID field then
					// first
					// sorting direction is descending as its already sorted in
					// ascending order.
					if (currentColumn == tabViewer.getTable().getColumn(0)) {
						dir = SWT.DOWN;
					} else {
						dir = SWT.UP;
					}
				}

				tabViewer.getTable().setSortDirection(dir);
				tabViewer.setSorter(new ComponentAssociationTableSorter(colNumber, dir));
				
                Order order = Order.ASCENDING;
                if (dir == SWT.DOWN) {
                    order = Order.DESCENDING;
                }
                SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, type.name(), colNumber, order);
                prefs.store();                              
				
			} finally {
				shell.setCursor(null);
			}
		}
	}

	/**
	 * Queries JFaceResources register to get an Image specified by a 'key'. If
	 * the Image doesn't exists in the register, its created and added to
	 * JFaceResources.
	 * 
	 * @param key
	 *            String - path to the image. Also works as a key to query
	 *            JFaceResources register.
	 * @return Image
	 */
	public static Image getAndRegisterImage(String key) {
		if (JFaceResources.getImage(key) == null) {
			JFaceResources.getImageRegistry().put(key, getImageDescriptor(key));
		}
		return JFaceResources.getImage(key);
	}

	public static Color getColorForDisabledCell() {
		return Display.getCurrent().getSystemColor(
				SWT.COLOR_WIDGET_LIGHT_SHADOW);

	}

	/**
	 * Method to store the last focus control in the shell when the user switches away 
	 * from WE (e.g. ALT-TAB). This will mean that the control gains focus when WE is 
	 * activated again.  
	 * @param control - control that last has focus
	 */
	public static void rememberFocusControl(Control control) {
	    Shell shell = control.getShell();
        Method method;
        Class<?>[] classArray = { Control.class };
        try {
            method = Decorations.class.getDeclaredMethod("setSavedFocus", classArray);
            method.setAccessible(true);
            method.invoke(shell, control);
        }  catch (Exception e) {
            throw new IllegalArgumentException("Invalid call on Decorations.setSavedFocus",e);
        }	    
	}
	
	/**
	 * Method to show an error dialog displaying the exception that occurred in
	 * a user-friendly format.
	 * 
	 * @param e
	 *            {@link Exception} the exception that occurred
	 * @param msgString
	 *            the text to be shown as the error message. If this text is
	 *            blank or null, only the message contained in the exception
	 *            will be shown as the error message. Else, the text passed as
	 *            argument will be concatenated with the message contained in
	 *            the exception.
	 * @param titleString
	 *            the text to be shown as the title of the error dialog. If this
	 *            text is blank or null, the string "GenevaERS Workbench" will be
	 *            shown as the default title of the error dialog.
	 */
	public static void handleWEExceptions(Exception e, String msgString,
			String titleString) {

	    SAFRLogger.logAllStamp(logger, Level.SEVERE, (titleString != null ? titleString + SAFRUtilities.LINEBREAK
            + msgString : msgString), e);
		
		String checkLogFile = "Check trace file for details.";
		
		if (msgString == null || msgString.equals("")) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					titleString, e.getMessage() + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK + checkLogFile);
		} else {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					titleString, msgString + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK + e.getMessage() + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK
							+ checkLogFile);
		}
	}

	/**
	 * Method to show an error dialog displaying the exception that occurred in
	 * a user-friendly format
	 * 
	 * @param e
	 *            {@link Exception} the exception that occurred
	 */
	public static void handleWEExceptions(Exception e) {
		handleWEExceptions(e, null, null);
	}

	/**
	 * Method to open an Editor based on the component.
	 * 
	 * @param component
	 *            component for which the editor is to be opened.
	 * @param componentType
	 *            TreeItem selected in the navigator view.
	 * @throws PartInitException
	 */
	public static void openEditor(SAFRPersistentObject component,
			ComponentType componentType) throws PartInitException {
		IEditorInput input = null;
		String editorID = null;

		if (componentType == ComponentType.View) {
			input = new ViewEditorInput((View) component,
					EditRights.ReadModifyDelete);
			editorID = ViewEditor.ID;

		} else if (componentType == ComponentType.LogicalRecord) {
			input = new LogicalRecordEditorInput((LogicalRecord) component,
					EditRights.ReadModifyDelete);

			editorID = LogicalRecordEditor.ID;

		} else if (componentType == ComponentType.LookupPath) {
			input = new LookupPathEditorInput((LookupPath) component,
					EditRights.ReadModifyDelete);

			editorID = LookupPathEditor.ID;
		} else if (componentType == ComponentType.Environment) {
			input = new EnvironmentEditorInput((Environment) component,
					EditRights.ReadModifyDelete);

			editorID = EnvironmentEditor.ID;
		} else if (componentType == ComponentType.ControlRecord) {
			input = new ControlRecordEditorInput((ControlRecord) component,
					EditRights.ReadModifyDelete);

			editorID = ControlRecordEditor.ID;
		} else if (componentType == ComponentType.PhysicalFile) {
			input = new PhysicalFileEditorInput((PhysicalFile) component,
					EditRights.ReadModifyDelete);

			editorID = PhysicalFileEditor.ID;
		} else if (componentType == ComponentType.LogicalFile) {
			input = new LogicalFileEditorInput((LogicalFile) component,
					EditRights.ReadModifyDelete);

			editorID = LogicalFileEditor.ID;
		} else if (componentType == ComponentType.UserExitRoutine) {
			input = new UserExitRoutineEditorInput((UserExitRoutine) component,
					EditRights.ReadModifyDelete);

			editorID = UserExitRoutineEditor.ID;
		} else if (componentType == ComponentType.ViewFolder) {
			input = new ViewFolderEditorInput((ViewFolder) component,
					EditRights.ReadModifyDelete);

			editorID = ViewFolderEditor.ID;
		} else if (componentType == ComponentType.Group) {
			input = new GroupEditorInput((Group) component);

			editorID = GroupEditor.ID;
		} else if (componentType == ComponentType.User) {
			input = new UserEditorInput((User) component);

			editorID = UserEditor.ID;

		}
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.openEditor(input, editorID);
	}

	/**
	 * This method returns the source provider for the Workbench.
	 * 
	 * @return Source provider for the workbench.
	 * 
	 */
	public static SourceProvider getSourceProvider() {
		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
				.getWorkbench().getService(ISourceProviderService.class);
		SourceProvider service = (SourceProvider) sourceProviderService
				.getSourceProvider(SourceProvider.ALLOW_PASTE_VIEWCOLUMN);
		return service;
	}

	/**
	 * Utility function to compare two {@link Enum} objects. Useful if any or
	 * both the enum are null.
	 * 
	 * @param e1
	 *            enum one could be null
	 * @param e2
	 *            enum two could be null
	 * @return int comparision result. '0' if e1 is equal to e2, negative number
	 *         if e1 is less then e2, positive number if e1 is greater then e2
	 */
	@SuppressWarnings({ "rawtypes" })
    public static int compareEnums(Enum e1, Enum e2) {
		int ret = compareNulls(e1, e2);
		if (ret == 2) { // no nulls, so compare
			ret = e1.toString().compareTo(e2.toString());
		}
		return ret;
	}

	/**
	 * This function is used to set the position of the shell in middle of its
	 * parent
	 * 
	 * @param width
	 *            : width of the dialog
	 * @param height
	 *            : height of the dialog
	 * @param shell
	 *            : shell
	 */
	public static void setDialogBounds(int width, int height, Shell shell) {
		shell
				.setBounds(
						shell.getParent().getBounds().x
								+ ((shell.getParent().getBounds().width / 2) - (width / 2)),
						shell.getParent().getBounds().y
								+ ((shell.getParent().getBounds().height / 2) - (height / 2)),
						width, height);
	}

	private static void enableDisableEnvPermMenu() {
	    
	    SourceProvider sourceProvider = UIUtilities.getSourceProvider();
	    
        // is envadmin in any environment??
        List<EnvironmentQueryBean> envList = null;
        try {
            envList = SAFRQuery.queryEnvironmentsForLoggedInUser(
                    SortType.SORT_BY_NAME, true);
        } catch (DAOException e1) {
            UIUtilities.handleWEExceptions(e1,
                "Error occurred while retrieving all environments.",
                UIUtilities.titleStringDbException);
        }
        
        if (envList == null || envList.isEmpty()) {
            sourceProvider.setEnvironmentPermissionMenu(false);
        }
        else {
            sourceProvider.setEnvironmentPermissionMenu(true);                 
        }	    
	}
	
    private static void enableDisableMigrateMenu() {
        
        SourceProvider sourceProvider = UIUtilities.getSourceProvider();    
        
        try {
            // Get only envs on which user has Admin or Migrate-In rights.
            UserSession userSession = SAFRApplication.getUserSession();
            if (userSession.isSystemAdministrator()) {
                // user has Admin rights for all envs
                sourceProvider.setMigrateMenu(true);                                    
            } else {
                // include only migrate-in rights
                List<EnvironmentQueryBean> tempList = SAFRQuery.queryEnvironmentsForLoggedInUser(
                    SortType.SORT_BY_NAME, false);
                boolean found = false;
                for (EnvironmentQueryBean envQb : tempList) {
                    if (userSession.isAdminOrMigrateInUser(envQb.getId())) {
                        sourceProvider.setMigrateMenu(true);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    sourceProvider.setMigrateMenu(false);                    
                }
            }
        } catch (DAOException e1) {
            UIUtilities.handleWEExceptions(e1,
                "Error occurred while retrieving environments.",
                UIUtilities.titleStringDbException);
            sourceProvider.setMigrateMenu(false);                                                
        }
        
    }

    private static void enableDisableImportMenu() {
        
        SourceProvider sourceProvider = UIUtilities.getSourceProvider();    
        
        try {
            // Get only envs on which user has Admin or Migrate-In rights.
            UserSession userSession = SAFRApplication.getUserSession();
            if (userSession.isSystemAdministrator()) {
                // user has Admin rights for all envs
                sourceProvider.setImportMenu(true);                                    
            } else {
                List<EnvironmentQueryBean> tempList = SAFRQuery.queryEnvironmentsForLoggedInUser(
                    SortType.SORT_BY_ID, true);
                if (tempList.isEmpty()) {
                    sourceProvider.setImportMenu(false);                    
                }
                else {
                    sourceProvider.setImportMenu(true);                                        
                }
            }
        } catch (DAOException e1) {
            UIUtilities.handleWEExceptions(e1,
                "Error occurred while retrieving environments.",
                UIUtilities.titleStringDbException);
            sourceProvider.setImportMenu(false);                                                
        }
        
    }
    
    public static void enableDisableBatchLookupMenu() {
        SourceProvider sourceProvider = UIUtilities.getSourceProvider();    
     
        if (SAFRApplication.getUserSession().isSystemAdministrator()) {
            sourceProvider.setBatchLookupMenu(true);
            return;
        }
        
        // load the data
        List<EnvironmentQueryBean> envList = null;
        try {
            envList = SAFRQuery.queryEnvironmentsForBAL(SortType.SORT_BY_NAME);

        } catch (DAOException e1) {
            UIUtilities.handleWEExceptions(e1,
                "Unexpected database error occurred while getting BAL environments.",
                UIUtilities.titleStringDbException);
        }
        
        if (envList == null || envList.isEmpty()) {
            sourceProvider.setBatchLookupMenu(false);
        }
        else {
            sourceProvider.setBatchLookupMenu(true);            
        }
    }
        
    public static void enableDisableBatchViewMenu() {
        SourceProvider sourceProvider = UIUtilities.getSourceProvider();
        
        if (SAFRApplication.getUserSession().isSystemAdministrator()) {
            sourceProvider.setBatchViewMenu(true);
            return;
        }
        
        List<EnvironmentQueryBean> envList = null;
        try {
            envList = SAFRQuery.queryEnvironmentsForBAV(SortType.SORT_BY_NAME);
            
        } catch (DAOException de) {
            UIUtilities.handleWEExceptions(de,
                "Unexpected database error occurred while getting BAV environments.",
                UIUtilities.titleStringDbException);
        }

        if (envList == null || envList.isEmpty()) {
            sourceProvider.setBatchViewMenu(false);            
        }
        else {
            sourceProvider.setBatchViewMenu(true);
        }
    }
    
	/**
	 * This function is used to enable disable workbench menu according to
	 * rights granted to logged in user
	 * 
	 */	
	public static void enableDisableMenuAsPerUserRights() {
		SourceProvider sourceProvider = UIUtilities.getSourceProvider();
		User user = SAFRApplication.getUserSession().getUser();

		if (!(user.isSystemAdmin())) {
			try {
				// check if user is a Environment Admin
				if (SAFRApplication.getUserSession().isEnvironmentAdministrator()) {
					sourceProvider.setAdministrationMenu(true);
                    sourceProvider.setGroupMembershipMenu(false);
                    sourceProvider.setGroupPermissionMenu(false);
					sourceProvider.setEnvironmentMenu(false);
					sourceProvider.setControlRecordMenu(true);
					sourceProvider.setUserMenu(false);
					sourceProvider.setGroupMenu(false);
					sourceProvider.setClearEnvironment(true);

				} else {
					// user is General user
					sourceProvider.setAdministrationMenu(false);
					sourceProvider.setGroupMembershipMenu(false);
					sourceProvider.setGroupPermissionMenu(false);
					sourceProvider.setEnvironmentMenu(false);
					sourceProvider.setControlRecordMenu(false);
					sourceProvider.setUserMenu(false);
                    sourceProvider.setGroupMenu(false);
					sourceProvider.setClearEnvironment(false);
				}
				
				enableDisableEnvPermMenu();
				enableDisableMigrateMenu();
				enableDisableBatchLookupMenu();
				enableDisableBatchViewMenu();
                enableDisableImportMenu();
                
                sourceProvider.setUserExitRoutineMenu(SAFRApplication.getUserSession().hasPermission(Permissions.CreateUserExitRoutine));
                sourceProvider.setPhysicalFileMenu(SAFRApplication.getUserSession().hasPermission(Permissions.CreatePhysicalFile));
                sourceProvider.setLogicalFileMenu(SAFRApplication.getUserSession().hasPermission(Permissions.CreateLogicalFile));
                sourceProvider.setLogicalRecordMenu(SAFRApplication.getUserSession().hasPermission(Permissions.CreateLogicalRecord));
                sourceProvider.setLookupPathMenu(SAFRApplication.getUserSession().hasPermission(Permissions.CreateLookupPath));
                sourceProvider.setViewMenu(SAFRApplication.getUserSession().hasPermission(Permissions.CreateView));
                sourceProvider.setViewFolderMenu(SAFRApplication.getUserSession().hasPermission(Permissions.CreateViewFolder));
				
			} catch (DAOException e) {
				UIUtilities.handleWEExceptions(e);
			}

		} else {
			// user is a system admin
			sourceProvider.setAdministrationMenu(true);
			sourceProvider.setEnvironmentMenu(true);
			sourceProvider.setUserExitRoutineMenu(true);
			sourceProvider.setControlRecordMenu(true);
			sourceProvider.setPhysicalFileMenu(true);
			sourceProvider.setLogicalFileMenu(true);
			sourceProvider.setLogicalRecordMenu(true);
			sourceProvider.setLookupPathMenu(true);
			sourceProvider.setViewMenu(true);
			sourceProvider.setViewFolderMenu(true);
			sourceProvider.setUserMenu(true);
			sourceProvider.setGroupMenu(true);
			sourceProvider.setBatchLookupMenu(true);
			sourceProvider.setBatchViewMenu(true);
            sourceProvider.setMigrateMenu(true);			
            sourceProvider.setImportMenu(true);            
			sourceProvider.setGroupPermissionMenu(true);
			sourceProvider.setEnvironmentPermissionMenu(true);
			sourceProvider.setGroupMembershipMenu(true);
			sourceProvider.setClearEnvironment(true);

		}
	}

	
	/**
	 * This method is to check whether current user is System Admin or
	 * Environment Admin in the login environment.
	 * 
	 * @return true if user is Sys Admin or Env Admin else false.
	 * @throws SAFRException
	 */
	public static Boolean isSystemAdminOrEnvAdmin() {
		return isSystemAdminOrEnvAdmin(SAFRApplication.getUserSession().getEnvironment().getId());
	}

    /**
     * This method is to check whether current user is System Admin 
     * 
     * @return true if user is Sys Admin  else false.
     * @throws SAFRException
     */
    public static Boolean isSystemAdmin() {
        return SAFRApplication.getUserSession().isSystemAdministrator();
    }
	
	/** 
	 * Get edit rights of any component including checking admin access
	 */
	public static EditRights getEditRights(Integer id, ComponentType type) {
	    if (id == null || id.intValue() == 0 || type == null) {
	        return EditRights.Read;
	    }
	    else if (type == ComponentType.Group || type == ComponentType.User) {
	        if (SAFRApplication.getUserSession().isSystemAdministrator()) {
	            return EditRights.ReadModifyDelete;	            
	        }
	        else {
	            return EditRights.Read;
	        }	    	
	    }
	    else if (type == ComponentType.Environment) {
            EditRights rights = EditRights.Read;	    	
	    	try {
				if (SAFRApplication.getUserSession().isSystemAdministrator() ||
					SAFRApplication.getUserSession().isEnvironmentAdministrator(id)) {
				    rights = EditRights.ReadModifyDelete;	            
				}
				else {
					for (EnvironmentQueryBean bean : 
						SAFRQuery.queryEnvironmentsForLoggedInUser(SortType.SORT_BY_NAME, false)) {
						if (bean.getId().intValue() == id) {
							rights = EditRights.Read;
							break;
						}
					}
				}
				
			} catch (DAOException e) {
                UIUtilities.handleWEExceptions(e,
                        "Error in getting edit rights", null);
			}
	    	return rights;
	    }
        else if (type == ComponentType.LogicalRecordField) {
            EditRights rights = EditRights.Read;
            try {
                LRField field = SAFRApplication.getSAFRFactory().getLRField(id, false);                
                rights = SAFRApplication.getUserSession()
                .getEditRights(ComponentType.LogicalRecord, field.getLogicalRecord().getId());
            } catch (SAFRException e) {
                UIUtilities.handleWEExceptions(e,"Error in getting edit rights", null);
            } 
            return rights;                        
        }
        else {
            EditRights rights = EditRights.Read;
            try {
                rights = SAFRApplication.getUserSession().getEditRights(type, id);
            } catch (SAFRException e) {
                UIUtilities.handleWEExceptions(e,"Error in getting edit rights", null);
            }
            return rights;            
        }	    
	}

	/**
	 * Check whether current user is System Admin or Environment Admin on the
	 * specified environment.
	 * 
	 * @return true if user is Sys Admin or Env Admin else false.
	 * @throws SAFRException
	 */
	public static Boolean isSystemAdminOrEnvAdmin(Integer environId) {
		try {
			return SAFRApplication.getUserSession().isSystemAdminOrEnvAdmin(environId);
		} catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e,
			    "Database error occurred while getting rights for the current user on Environment " + environId + ".", 
			    UIUtilities.titleStringDbException);
			return false;
		}
	}

    /**
     * Check whether current user has migrate access on the
     * specified environment.
     * 
     * @return true if user is Sys Admin or MigrateIn else false.
     * @throws SAFRException
     */
    public static Boolean isAdminOrMigrateInUser(Integer environId) {
        try {
            return SAFRApplication.getUserSession().isAdminOrMigrateInUser(environId);
        } catch (DAOException e) {
            UIUtilities.handleWEExceptions(e,
                "Database error occurred while getting rights for the current user on Environment "+ environId + ".",
                UIUtilities.titleStringDbException);
            return false;
        }
    }

    /**
     * Check whether current user has migrate access on the
     * specified environment.
     * 
     * @return true if user is Sys Admin or Env Admin or MigrateIn else false.
     * @throws SAFRException
     */
    public static Boolean isAdminOrMigrateInUser() {
        return UIUtilities.isAdminOrMigrateInUser(SAFRApplication.getUserSession().getEnvironment().getId());
    }
    
	public static void setCommandState(String commandId, String stateId,
			boolean newState) {
		ICommandService commandService = (ICommandService) PlatformUI
				.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand(commandId);
		State state = command.getState(stateId);
		if (state != null) {
			boolean currentState = (Boolean) state.getValue();
			if (currentState != newState) {
				state.setValue(newState);
				commandService.refreshElements(command.getId(), null);
			}
		}

	}

	public static boolean getCommandState(String commandId, String stateId) {
		ICommandService commandService = (ICommandService) PlatformUI
				.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand(commandId);
		State state = command.getState(stateId);
		if (state != null) {
			return (Boolean) state.getValue();
		}
		return false;
	}

	public static void setTableComboBounds(TableCombo tableCombo) {
		Rectangle tBounds = tableCombo.getTable().getBounds();
		tBounds.width = tableCombo.getBounds().width;
		tableCombo.getTable().setBounds(tBounds);

		tableCombo.getTable().getColumn(1).setWidth(
				tableCombo.getTable().getBounds().width
						- tableCombo.getTable().getColumn(0).getWidth());
	}

    public static String getVersion() {
        String versionDetails = "Version ";

        try {
            Bundle bundle = Platform.getBundle("GenevaERS");
            if(bundle != null) {
            	versionDetails += bundle.getVersion().toString() + "_RC13 " + getTimeStamp();
            } else {
                logger.log(Level.SEVERE, "Null getting version");            	
            }
        }

        catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting version", e);
        }
        return versionDetails;
    }
	
	public static String getVersionDetails() {
		return getVersion();
	}

	public static String getTimeStamp() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
        String ver = "";
		try (InputStream resourceStream = loader.getResourceAsStream("wb.properties")) {
			properties.load(resourceStream);
            ver = " (" + properties.getProperty("builder") + " " + properties.getProperty("timestamp")  + ")";
		} catch (IOException e) {
			logger.log(Level.SEVERE, "unable to get timestamp", e);
		}
        return ver;
    }


	// Return the width of the widest button
	public static int computePreferredButtonWidth(List<Button> buttons) {
		int thisWidth, thatWidth = 0;
		for(Button button : buttons) {
			thisWidth = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			thatWidth = thisWidth > thatWidth ? thisWidth : thatWidth;
		}
		return thatWidth;
	}
	
	/**
	 * Split a message string to separate lines of a default length 80
	 * to improve output format.
	 * 
	 * @param message the message string
	 * @return the message string with line feed chars inserted
	 */
	public static String splitMessage(String message) {
		return splitMessage(message, 80); //default msg line len
	}
	
	/**
	 * Split a message string to separate lines of the specified length
	 * to improve output format.
	 *  
	 * @param message the message string
	 * @param lineLength length of each line
	 * @return the message string with line feed chars inserted
	 */
	public static String splitMessage(String message, int lineLength) {
		String outMsg = "";
		
		String[] array = message.split(SAFRUtilities.LINEBREAK);
		String msgPart = null;
		for (int x = 0; x < array.length; x++) {
			msgPart = array[x];
			int maxLen = msgPart.length();
			int i = 0;
			int j = lineLength-1;
 			while (j < maxLen) {
				if (msgPart.charAt(j) == ' ') {
					outMsg += msgPart.substring(i, j);
					outMsg += SAFRUtilities.LINEBREAK;
					i = j + 1;
					j += lineLength;
				} else {
					j++;
				}
			}
			outMsg += msgPart.substring(i);
			outMsg += SAFRUtilities.LINEBREAK;
		}
		return outMsg;
	}

    /**
     * Extracts an integer id from "name [id]" string 
     * @return
     */
    public static Integer extractId(String text) {
        // name [id]
        int left = text.indexOf('[');
        int right = text.indexOf(']');
        if (left != -1 && right != -1)
        {
            text = text.substring(text.indexOf('[')+1);
            text = text.substring(0, text.indexOf(']'));         
            return Integer.valueOf(text);
        }
        else {
            return null;
        }
    }
    
    public static void replaceMenuText(Text text) {
        // replace the default windows context menu
        Menu menu = new Menu(text);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Cut");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                text.cut();
            }
        });
        MenuItem item2 = new MenuItem(menu, SWT.PUSH);
        item2.setText("Copy");
        item2.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                text.copy();
            }
        });
        MenuItem item3 = new MenuItem(menu, SWT.PUSH);
        item3.setText("Paste");
        item3.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                text.paste();
            }
        }); 
        text.addListener(SWT.MouseUp, new Listener() {

            @Override
            public void handleEvent(Event event) {
                Text text = (Text) event.widget;

                String selection = text.getSelectionText();

                if(selection.length() > 0)
                {
                    item.setEnabled(true);
                    item2.setEnabled(true);                    
                } else {
                    item.setEnabled(false);                    
                    item2.setEnabled(false);                    
                }
            }
        });

        text.addListener(SWT.KeyUp, new Listener() {

            @Override
            public void handleEvent(Event event) {
                Text text = (Text) event.widget;

                String selection = text.getSelectionText();

                if(selection.length() > 0 && event.keyCode == SWT.SHIFT)
                {
                    item.setEnabled(true);
                    item2.setEnabled(true);                    
                } else {
                    item.setEnabled(false);
                    item2.setEnabled(false);                                        
                }
            }
        });      
        
        menu.addMenuListener(new MenuListener() {

            @Override
            public void menuHidden(MenuEvent e) {
            }

            @Override
            public void menuShown(MenuEvent e) {
                Clipboard clipboard = new Clipboard(Display.getCurrent());
                TextTransfer textTransfer = TextTransfer.getInstance();
                String textData = (String)clipboard.getContents(textTransfer);
                if (textData == null || textData.isEmpty()) {
                    item3.setEnabled(false);                    
                } else {
                    item3.setEnabled(true);                                        
                }
            }
            
        });
        text.setMenu(menu);         
    }

    public static void replaceMenuCombo(CCombo control) {
        // replace the default windows context menu
        Menu menu = new Menu(control);
        control.setMenu(menu);         
    }

    public static void replaceMenuCombo(Combo control) {
        // replace the default windows context menu
        Menu menu = new Menu(control);
        control.setMenu(menu);         
    }

    public static void replaceMenuTextCopy(Text text) {
        // replace the default windows context menu
        Menu menu = new Menu(text);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Copy");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                text.copy();
            }
        });
        text.addListener(SWT.MouseUp, new Listener() {

            @Override
            public void handleEvent(Event event) {
                Text text = (Text) event.widget;

                String selection = text.getSelectionText();

                if(selection.length() > 0) {
                    item.setEnabled(true);
                } else {
                    item.setEnabled(false);                    
                }
            }
        });

        text.addListener(SWT.KeyUp, new Listener() {

            @Override
            public void handleEvent(Event event) {
                Text text = (Text) event.widget;

                String selection = text.getSelectionText();

                if(selection.length() > 0 && event.keyCode == SWT.SHIFT)
                {
                    item.setEnabled(true);
                } else {
                    item.setEnabled(false);
                }
            }
        });      
        
        text.setMenu(menu);                 
    }
    
}
