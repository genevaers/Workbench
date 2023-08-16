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


import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.query.NumericIdQueryBean;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationLabelProvider;
import com.ibm.safr.we.ui.views.metadatatable.ControlRecordTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.EnvironmentTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.GroupTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.LogicalFileTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.LogicalRecordTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.LookupTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.MainTableLabelProvider;
import com.ibm.safr.we.ui.views.metadatatable.PhysicalFileTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.UserExitRoutineTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.ViewFolderTableSorter;
import com.ibm.safr.we.ui.views.metadatatable.ViewTableSorter;

/**
 * A class for creating SAFR application specific GUI controls.It will create
 * controls of type ONLY_SWT or ELIPSE_FORM. ONLY_SWT type will create controls
 * that will be used as swt controls. ECLIPSE_FORM type will create controls
 * which will be created using FORMToolkit.
 * 
 */
public class SAFRGUIToolkit {
	public static final Integer ONLY_SWT = 0;
	public static final Integer ECLIPSE_FORM = 1;
	
	
	private FormToolkit toolkit;
	private Integer controlType = ONLY_SWT;
	// specifies if the controls created should be read only. All the controls
	// can be read only if the user doesn't have appropriate rights to edit
	// them.
	private boolean readOnly;
    static final String SORT_CATEGORY = "TableCombo";

	/**
	 * SAFRGUIToolkit class constructor.This constructor will be used to create
	 * controls only SWT controls.
	 */
	public SAFRGUIToolkit() {
		this.controlType = ONLY_SWT;
	}

	/**
	 * This constructor is used only for creating Eclipse form controls. For
	 * using this constructor instance of FormToolkit should be already created.
	 * If toolkit is null then SAFRGUIToolkit for SWT is created.
	 * 
	 * @param toolkit
	 *            {@link FormToolkit} to use for creating controls.If null then
	 *            SAFRGUIToolkit for SWT is created.
	 */
	public SAFRGUIToolkit(FormToolkit toolkit) {
		if (toolkit != null) {
			this.toolkit = toolkit;
			this.controlType = ECLIPSE_FORM;
		} else {
			this.controlType = ONLY_SWT;
		}
	}

	/**
	 * This method creates Textbox of specific SWTstyle. It creates textbox with
	 * attached focus listener to it.Focus listener selects existing text in
	 * textbox when textbox gets focus.
	 * 
	 * @param composite
	 *            on which Textbox to be created.
	 * @param SWTstyle
	 *            of the Textbox to create.It is defined in class {@link SWT}.
	 * @return The text widget.
	 */
	public Text createTextBox(final Composite composite, int SWTstyle) {
		Text text;
		if (controlType == ECLIPSE_FORM) {
			text = toolkit.createText(composite, "", SWTstyle | SWT.BORDER);
		} else {
			text = new Text(composite, SWTstyle | SWT.BORDER);
		}
		text.setEnabled(!readOnly);
		text.addFocusListener(new TextFocusListener(text, true));
        UIUtilities.replaceMenuText(text);              
		return text;
	}

	/**
	 * This method creates Textbox of specific SWTstyle. It creates textbox with
	 * attached focus listener to it.Focus listener selects existing text in
	 * textbox when textbox gets focus.
	 * 
	 * @param composite
	 *            on which Textbox to be created.
	 * @param SWTstyle
	 *            of the Textbox to create.It is defined in class {@link SWT}.
	 * @return The text widget.
	 */
	public Text createNameTextBox(final Composite composite, int SWTstyle) {
		Text text;
		if (controlType == ECLIPSE_FORM) {
			text = toolkit.createText(composite, "", SWTstyle | SWT.BORDER);
		} else {
			text = new Text(composite, SWTstyle | SWT.BORDER);
		}
		text.setEnabled(!readOnly);
		text.addModifyListener(new TextModifyListener(text));		
		text.addFocusListener(new TextFocusListener(text, true));
        UIUtilities.replaceMenuText(text);              
		return text;
	}
	
	/**
	 * This method creates Textbox of specific SWTstyle. It creates textbox with
	 * attached verify and focus listener to it. Verify listener checks whether
	 * value in textbox is only integer. It allows to enter only numeric
	 * value.Focus listener selects existing text in textbox when textbox gets
	 * focus.Sets default value to Zero
	 * 
	 * @param composite
	 *            on which Textbox to be created.
	 * @param SWTstyle
	 *            of the Textbox to create.It is defined in class {@link SWT}.
	 * @param allowNegatives
	 *            <code>true</code> if the Textbox can contain negative values,
	 *            <code>false</code> if only positives are allowed.
	 * @return The text widget.
	 */
	public Text createIntegerTextBox(final Composite composite, int SWTstyle,
			Boolean allowNegatives) {

		return createIntegerTextBox(composite, SWTstyle, allowNegatives, "0");

	}

	/**
	 * This method creates Textbox of specific SWTstyle. It creates textbox with
	 * attached verify and focus listener to it. Verify listener checks whether
	 * value in textbox is only integer. It allows to enter only numeric
	 * value.Focus listener selects existing text in textbox when textbox gets
	 * focus.
	 * 
	 * @param composite
	 *            on which Textbox to be created.
	 * @param SWTstyle
	 *            of the Textbox to create.It is defined in class {@link SWT}.
	 * @param allowNegatives
	 *            <code>true</code> if the Textbox can contain negative values,
	 *            <code>false</code> if only positives are allowed.
	 * @param defaultValue
	 *            value to be set as default
	 * @return The text widget.
	 */
	public Text createIntegerTextBox(final Composite composite, int SWTstyle,
			Boolean allowNegatives, String defaultValue) {
		Text text = this.createTextBox(composite, SWTstyle);

		if (defaultValue != null) {
			text.setText(defaultValue); // set default value
		}
		text.addVerifyListener((new VerifyNumericListener(WidgetType.INTEGER,
				allowNegatives)));
		text.setEnabled(!readOnly);
        UIUtilities.replaceMenuText(text);              
		return text;
	}

	/**
	 * This method creates Textbox of specific SWTstyle. It creates textbox with
	 * attached verify and focus listener to it. Verify listener checks whether
	 * value in textbox is only Decimal integer.It allows to enter only number
	 * and single decimal point.Focus listener selects existing text in textbox
	 * when textbox gets focus.
	 * 
	 * @param composite
	 *            on which Textbox to be created.
	 * @param SWTstyle
	 *            of the Textbox to create.It is defined in class {@link SWT}.
	 * @param allowNegatives
	 *            <code>true</code> if the Textbox can contain negative values,
	 *            <code>false</code> if only positives are allowed.
	 * @return The text widget.
	 */
	public Text createDecimalTextBox(final Composite composite, int SWTstyle,
			Boolean allowNegatives) {
		Text text = this.createTextBox(composite, SWTstyle);
		text.setText("0"); // default should be zero
		text.addVerifyListener((new VerifyNumericListener(WidgetType.DECIMAL,
				allowNegatives)));
		text.setEnabled(!readOnly);
		return text;
	}

	/**
	 * This method creates Textbox for SAFR comments.It is specifically used for
	 * creating textbox which contain comments.This textbox has text limit of
	 * 254 characters and has {@link TraverseListener} added to it so that when
	 * user press TAB key focus goes to another control.
	 * 
	 * @param composite
	 *            on which Textbox to be created.
	 * 
	 * @return The text widget for SAFR comments.
	 */
	public Text createCommentsTextBox(Composite composite) {
		Text text;
		if (controlType == ECLIPSE_FORM) {
			text = toolkit.createText(composite, "", SWT.MULTI | SWT.WRAP
					| SWT.V_SCROLL | SWT.BORDER);
		} else {
			text = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		}
		text.setTextLimit(UIUtilities.MAXCOMMENTCHAR);
		text.addTraverseListener(new CommentsTraverseListener());
		text.setEnabled(!readOnly);
		text.addFocusListener(new TextFocusListener(text, false));
        UIUtilities.replaceMenuText(text);				
		return text;
	}

	/**
	 * This method is used to create Text Box that converts all characters types
	 * in it to uppercase.It creates Textbox of specific SWTstyle. It creates
	 * textbox with attached verify and focus listener to it. Verify listener
	 * checks whether value in textbox is only upper case.It converts characters
	 * typed in it to upper case.Focus listener selects existing text in textbox
	 * when textbox gets focus.
	 * 
	 * @param composite
	 *            on which Textbox to be created.
	 * @param SWTstyle
	 *            of the Textbox to create.It is defined in class {@link SWT}.
	 * @return The text widget.
	 */
	public Text createUpperCaseTextBox(Composite composite, int SWTstyle) {
		Text text = this.createTextBox(composite, SWTstyle);
		text.addVerifyListener((new VerifyUpperCaseListener()));
		text.setEnabled(!readOnly);
		text.addModifyListener(new TextModifyListener(text));		
		text.addFocusListener(new TextFocusListener(text, false));		
        UIUtilities.replaceMenuText(text);              
		return text;
	}

	/**
	 * This method creates Button of SWTstyle SWT.CHECK .
	 * 
	 * @param composite
	 *            on which Button to be created.
	 * @param text
	 *            an optional text for the button.
	 * @return textBox of type SWT.CHECK.
	 */
	public Button createCheckBox(Composite composite, String text) {

		Button button;
		Integer SWTstyle = SWT.CHECK;
		if (controlType == ECLIPSE_FORM) {
			button = toolkit.createButton(composite, text, SWTstyle);
		} else {
			button = new Button(composite, SWTstyle);
			button.setText(text);
		}
		button.setEnabled(!readOnly);
		return button;
	}

	/**
	 * This method creates Button of specific SWTstyle.
	 * 
	 * @param composite
	 *            on which Button to be created.
	 * @param SWTstyle
	 *            of the Button to create.It is defined in class {@link SWT}.
	 * @param text
	 *            an optional text for the button .
	 * @return button of specific SWT style.
	 */
	public Button createButton(Composite composite, int SWTstyle, String text) {
		Button button;
		if (controlType == ECLIPSE_FORM) {
			button = toolkit.createButton(composite, text, SWTstyle);
		} else {
			button = new Button(composite, SWTstyle);
			button.setText(text);
		}
		button.setEnabled(!readOnly);
		return button;
	}

	/**
	 * This method creates Button of type SWT style SWT.RADIO.
	 * 
	 * @param composite
	 *            on which Button to be created.
	 * @param text
	 *            an optional text for the button .
	 * @return Button of type SWT style SWT.RADIO
	 */
	public Button createRadioButton(Composite composite, String text) {

		Button button;
		Integer SWTstyle = SWT.RADIO;
		if (controlType == ECLIPSE_FORM) {
			button = toolkit.createButton(composite, text, SWTstyle);
		} else {
			button = new Button(composite, SWTstyle);
			button.setText(text);
		}
		button.setEnabled(!readOnly);
		return button;
	}

	/**
	 * This method creates Label of specific SWTstyle.
	 * 
	 * @param composite
	 *            on which Label to be created.
	 * @param SWTstyle
	 *            of the Label to create.It is defined in class {@link SWT}.
	 * @param text
	 *            the label text.
	 * @return Label of specific SWT style.
	 */
	public Label createLabel(Composite composite, int SWTstyle, String text) {
		Label label;
		if (controlType == ECLIPSE_FORM) {
			label = toolkit.createLabel(composite, text, SWTstyle);
		} else {
			label = new Label(composite, SWTstyle);
			label.setText(text);
		}
		return label;
	}

	/**
	 * This method creates Table of specific SWTstyle.If Table is of type
	 * ECLIPSE_FORM then this method adapts it to be used in a form that is
	 * associated with this toolkit. This involves adjusting colors and
	 * optionally adding handlers to ensure focus tracking and keyboard
	 * management.
	 * 
	 * @param composite
	 *            on which the Table is to be created.
	 * @param SWTStyle
	 *            of the Table to create.It is defined in class {@link SWT}.
	 * @param adapt
	 *            whether this Table must be adapted to be used in a form that
	 *            is associated with this toolkit.
	 * @return Table of specified SWT style.
	 */
	public Table createTable(Composite composite, int SWTStyle, boolean adapt) {
		Table table;
		table = new Table(composite, SWTStyle);
		if (controlType == ECLIPSE_FORM && adapt) {
			toolkit.adapt(table, true, true);
		}
		return table;
	}

	/**
	 * This method creates a checkbox TableViewer. If TableViewer is of type
	 * ECLIPSE_FORM then this method adapts it to be used in a form that is
	 * associated with this toolkit. This involves adjusting colors and
	 * optionally adding handlers to ensure focus tracking and keyboard
	 * management.
	 * 
	 * @param table
	 *            the control on which the TableViewer is to be created
	 * @return the checkbox TableViewer that has been created.
	 */
	public CheckboxTableViewer createCheckboxTableViewer(Table table) {

		CheckboxTableViewer tableViewer;
		tableViewer = new CheckboxTableViewer(table);
		if (controlType == ECLIPSE_FORM) {
			toolkit.adapt(tableViewer.getTable(), true, true);
		}
		return tableViewer;
	}

	/**
	 * This method creates TableViewer of SWT style SWT.MULTI | SWT.H_SCROLL |
	 * SWT.V_SCROLL| SWT.FULL_SELECTION.If TableViewer is of type ECLIPSE_FORM
	 * then this method adapts it to be used in a form that is associated with
	 * this toolkit. This involves adjusting colors and optionally adding
	 * handlers to ensure focus tracking and keyboard management.
	 * 
	 * @param composite
	 *            on which TableViewer to be created.
	 * @return TableViewer of SWT style SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
	 *         | SWT.FULL_SELECTION.
	 */
	public TableViewer createTableViewer(Composite composite) {

		TableViewer tableViewer;
		Integer SWTstyle = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION;
		tableViewer = new TableViewer(composite, SWTstyle);
		if (controlType == ECLIPSE_FORM) {
			toolkit.adapt(tableViewer.getTable(), true, true);
		}
		return tableViewer;
	}

	public TableViewer createTableViewer(Composite composite, boolean adapt) {

		TableViewer tableViewer;
		Integer SWTstyle = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION;
		tableViewer = new TableViewer(composite, SWTstyle);
		if (controlType == ECLIPSE_FORM && adapt) {
			toolkit.adapt(tableViewer.getTable(), true, true);
		}
		return tableViewer;
	}

	/**
	 * This method creates TableViewer of specific SWTstyle.If TableViewer is of
	 * type ECLIPSE_FORM then this method adapts it to be used in a form that is
	 * associated with this toolkit. This involves adjusting colors and
	 * optionally adding handlers to ensure focus tracking and keyboard
	 * management.
	 * 
	 * @param composite
	 *            on which TableViewer to be created.
	 * @param SWTstyle
	 *            of the TableViewer to create.It is defined in class
	 *            {@link SWT}.
	 * @return TableViewer of specific SWT style.
	 */
	public TableViewer createTableViewer(Composite composite, int SWTstyle) {

		TableViewer tableViewer;
		tableViewer = new TableViewer(composite, SWTstyle);
		if (controlType == ECLIPSE_FORM) {
			toolkit.adapt(tableViewer.getTable(), true, true);
		}
		return tableViewer;
	}

	public TableViewer createTableViewer(Composite composite, int SWTstyle,
			boolean adapt) {

		TableViewer tableViewer;
		tableViewer = new TableViewer(composite, SWTstyle);
		if (controlType == ECLIPSE_FORM && adapt) {
			toolkit.adapt(tableViewer.getTable(), true, true);
		}
		return tableViewer;
	}

	/**
	 * Creates a tree viewer on a newly-created tree control under the given
	 * parent. The tree control is created using the SWT style bits MULTI,
	 * H_SCROLL, V_SCROLL, and BORDER. If TreeViewer is of type ECLIPSE_FORM
	 * then this method adapts it to be used in a form that is associated with
	 * this toolkit. This involves adjusting colors and optionally adding
	 * handlers to ensure focus tracking and keyboard management.
	 * 
	 * @param composite
	 *            on which TreeViewer to be created.
	 * @return TreeViewer of SWT style SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL |
	 *         SWT.BORDER.
	 */

	public TreeViewer createTreeViewer(Composite composite) {

		TreeViewer treeViewer;
		treeViewer = new TreeViewer(composite);
		if (controlType == ECLIPSE_FORM) {
			toolkit.adapt(treeViewer.getTree(), true, true);
		}
		return treeViewer;
	}

    /**
     * Creates a tree viewer on a newly-created tree control under the given
     * parent. If TreeViewer is of type ECLIPSE_FORM
     * then this method adapts it to be used in a form that is associated with
     * this toolkit. This involves adjusting colors and optionally adding
     * handlers to ensure focus tracking and keyboard management.
     * 
     * @param composite
     *            on which TreeViewer to be created.
     *        style
     * @return TreeViewer.
     */

    public TreeViewer createTreeViewer(Composite composite, int style) {

        TreeViewer treeViewer;
        treeViewer = new TreeViewer(composite, style);
        if (controlType == ECLIPSE_FORM) {
            toolkit.adapt(treeViewer.getTree(), true, true);
        }
        return treeViewer;
    }
	
	/**
	 * This method creates Combo of specific SWTstyle.If Combo is of type
	 * ECLIPSE_FORM then this method adapts it to be used in a form that is
	 * associated with this toolkit. This involves adjusting colors and
	 * optionally adding handlers to ensure focus tracking and keyboard
	 * management.
	 * 
	 * @param composite
	 *            on which Combo to be created.
	 * @param SWTstyle
	 *            of the Combo to create.It is defined in class {@link SWT}.
	 * @param text
	 *            the combo text.
	 * @return Combo of specific SWT style.
	 */
	public Combo createComboBox(Composite composite, int SWTstyle, String text) {

		Combo combo = new Combo(composite, SWTstyle);
		if (controlType == ECLIPSE_FORM) {
			toolkit.adapt(combo, true, true);
		}
		combo.setEnabled(!readOnly);
        UIUtilities.replaceMenuCombo(combo);              
		return combo;
	}

    public TableComboViewer createTableComboForExits(Composite composite,
        ComponentType componentType) {
        ViewerSorter sorter = null;
        TableComboViewer tableComboViewer = (TableComboViewer) new SAFRTableComboViewer(composite, SWT.READ_ONLY | SWT.BORDER | SWT.NO_SCROLL);
        final TableCombo tableCombo = tableComboViewer.getTableCombo();
        tableCombo.setShowImageWithinSelection(false);
        tableCombo.setVisibleItemCount(10);
        tableCombo.setShowTableHeader(true);
        tableCombo.setTableWidthPercentage(100);
        tableCombo.defineColumns(
            new String[] { "ID", "Name", "Executable", "Combo Text" },
            new int[] { 50, 300, 100, 0 });
        tableCombo.setDisplayColumnIndex(3);
        tableComboViewer.setContentProvider(new ArrayContentProvider());
    
        MainTableLabelProvider labelProvider = new MainTableLabelProvider(componentType) {
            public String getColumnText(Object element, int columnIndex) {
                switch (columnIndex) {
                case 3:
                	UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean) element;
                	String moreInfoString = "";
                	if(bean.getName().length() > 0) {
                		moreInfoString = bean.getName() + " [" + Integer.toString(bean.getId()) + "]   Exec: " + bean.getProgram();
                	}
                    return moreInfoString;
                default:
                    return super.getColumnText(element, columnIndex);
                }
            }
        };
        labelProvider.setInput();
        tableComboViewer.setLabelProvider(labelProvider);
        int iCounter;
        for (iCounter = 0; iCounter < 4; iCounter++) {
            ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(
                iCounter, tableComboViewer, componentType);
            tableCombo.getTable().getColumn(iCounter).addSelectionListener(colListener);
        }
        tableCombo.getTable().getColumn(3).setResizable(false);
        tableCombo.setEnabled(!readOnly);
        
        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, componentType.name());
        if (prefs.load()) {
            tableCombo.getTable().setSortColumn(
                tableCombo.getTable().getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                tableCombo.getTable().setSortDirection(SWT.UP);
                setSorter(componentType, tableComboViewer, prefs.getColumn(), SWT.UP, sorter);
            }
            else {
                tableCombo.getTable().setSortDirection(SWT.DOWN);
                setSorter(componentType, tableComboViewer, prefs.getColumn(), SWT.DOWN, sorter);
            }                   
        }
        else {
            tableCombo.getTable().setSortColumn(tableCombo.getTable().getColumn(1));
            tableCombo.getTable().setSortDirection(SWT.UP);
            setSorter(componentType, tableComboViewer, 1, SWT.UP, sorter);
        }       
        
        tableCombo.getTextControl().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                Rectangle tBounds = tableCombo.getTable().getBounds();
                tBounds.width = tableCombo.getBounds().width;
                tableCombo.getTable().setBounds(tBounds);

                int total = tableCombo.getTable().getBounds().width
                    - tableCombo.getTable().getColumn(0).getWidth(); 
                tableCombo.getTable().getColumn(1).setWidth((int) (total * 0.7));
                tableCombo.getTable().getColumn(2).setWidth((int) (total * 0.3));
                tableCombo.getTable().getColumn(3).setWidth(0);
            }

        });
        
        // replace the default windows context menu
        UIUtilities.replaceMenuTextCopy(tableCombo.getTextControl());
        
        return tableComboViewer;
    
    }
	
	public TableComboViewer createTableComboForComponents(Composite composite,
			ComponentType componentType) {
		ViewerSorter sorter = null;
		TableComboViewer tableComboViewer = new SAFRTableComboViewer(composite,
				SWT.READ_ONLY | SWT.BORDER);
		final TableCombo tableCombo = tableComboViewer.getTableCombo();
		tableCombo.setShowImageWithinSelection(false);
		tableCombo.setVisibleItemCount(10);
		tableCombo.setShowTableHeader(true);
		tableCombo.setTableWidthPercentage(93);
		tableCombo.defineColumns(new String[] { "ID", "Name", "Combo Text" },
				new int[] { 60, 100, 0 });
		tableCombo.setDisplayColumnIndex(2);
		tableComboViewer.setContentProvider(new ArrayContentProvider());

		MainTableLabelProvider labelProvider = new MainTableLabelProvider(
				componentType) {
			public String getColumnText(Object element, int columnIndex) {

				switch (columnIndex) {
				case 2:
					NumericIdQueryBean bean = (NumericIdQueryBean) element;
					return UIUtilities.getComboString(bean.getName(), bean.getId());
				default:
					return super.getColumnText(element, columnIndex);
				}
			}
		};
		labelProvider.setInput();
		tableComboViewer.setLabelProvider(labelProvider);
		int iCounter;
		for (iCounter = 0; iCounter < 3; iCounter++) {
			ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(
					iCounter, tableComboViewer, componentType);
			tableCombo.getTable().getColumn(iCounter).addSelectionListener(
					colListener);
		}
		tableCombo.getTextControl().addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				UIUtilities.setTableComboBounds(tableCombo);
			}

		});
		tableCombo.getTable().getColumn(2).setResizable(false);
		tableCombo.setEnabled(!readOnly);
		
        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, componentType.name());
        if (prefs.load()) {
            tableCombo.getTable().setSortColumn(
                tableCombo.getTable().getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                tableCombo.getTable().setSortDirection(SWT.UP);
                setSorter(componentType, tableComboViewer, prefs.getColumn(), SWT.UP, sorter);
            }
            else {
                tableCombo.getTable().setSortDirection(SWT.DOWN);
                setSorter(componentType, tableComboViewer, prefs.getColumn(), SWT.DOWN, sorter);
            }                   
        }
        else {
            tableCombo.getTable().setSortColumn(tableCombo.getTable().getColumn(1));
            tableCombo.getTable().setSortDirection(SWT.UP);
            setSorter(componentType, tableComboViewer, 1, SWT.UP, sorter);
        }       
		
        // replace the default windows context menu
        UIUtilities.replaceMenuTextCopy(tableCombo.getTextControl());
                
		return tableComboViewer;

	}

	public TableComboViewer createTableComboForComponents(Composite composite) {

		TableComboViewer tableComboViewer = new SAFRTableComboViewer(composite,
				SWT.READ_ONLY | SWT.BORDER);
		final TableCombo tableCombo = tableComboViewer.getTableCombo();
		tableCombo.setShowImageWithinSelection(false);
		tableCombo.setVisibleItemCount(10);
		tableCombo.setShowTableHeader(true);
		tableCombo.setTableWidthPercentage(93);
		tableCombo.defineColumns(new String[] { "ID", "Name", "Combo Text" },
				new int[] { 60, 100, 0 });
		tableCombo.getTable().setSortColumn(tableCombo.getTable().getColumn(1));
		tableCombo.getTable().setSortDirection(SWT.UP);
		tableCombo.setDisplayColumnIndex(2);

		tableCombo.getTextControl().addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				UIUtilities.setTableComboBounds(tableCombo);
			}
		});
		tableCombo.getTable().getColumn(2).setResizable(false);
		tableCombo.setEnabled(!readOnly);

        // replace the default windows context menu
        UIUtilities.replaceMenuTextCopy(tableCombo.getTextControl());
		
		return tableComboViewer;

	}

	public TableComboViewer createTableCombo(Composite composite) {

		TableComboViewer tableComboViewer = new TableComboViewer(composite,
				SWT.READ_ONLY | SWT.BORDER);
		final TableCombo tableCombo = tableComboViewer.getTableCombo();
		tableCombo.setShowImageWithinSelection(false);
		tableCombo.setVisibleItemCount(10);
		tableCombo.setShowTableHeader(false);
		tableCombo.setTableWidthPercentage(93);
		tableCombo.defineColumns(new String[] { "ID" });
		tableCombo.setDisplayColumnIndex(1);
		tableCombo.setEnabled(!readOnly);

        // replace the default windows context menu
        UIUtilities.replaceMenuTextCopy(tableCombo.getTextControl());
		
		return tableComboViewer;

	}

	public TableComboViewer createTableComboForAssociatedComponents(
			Composite composite) {

		TableComboViewer tableComboViewer = new SAFRTableComboViewer(composite,
				SWT.READ_ONLY | SWT.BORDER);
		final TableCombo tableCombo = tableComboViewer.getTableCombo();
		tableCombo.setShowImageWithinSelection(false);
		tableCombo.setLayoutData(new GridData(125, SWT.DEFAULT));
		tableCombo.setVisibleItemCount(10);
		tableCombo.setShowTableHeader(true);
		tableCombo.setTableWidthPercentage(93);
		tableCombo.defineColumns(new String[] { "ID", "Name", "Combo Text" },
				new int[] { 60, 100, 0 });
		tableCombo.setDisplayColumnIndex(2);

		tableComboViewer.setContentProvider(new ArrayContentProvider());

		tableComboViewer.setLabelProvider(new ComponentAssociationLabelProvider() {
			public String getColumnText(Object element, int columnIndex) {

				switch (columnIndex) {
				case 2:
					ComponentAssociation componentAssociation = (ComponentAssociation) element;
					return UIUtilities.getComboString(
					    componentAssociation.getAssociatedComponentName(),
					    componentAssociation.getAssociatedComponentIdNum());
				default:
					return super.getColumnText(element, columnIndex);
				}
			}

		});

		int iCounter;
		for (iCounter = 0; iCounter < 3; iCounter++) {
			ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(
					iCounter, tableComboViewer, "AssociatedComponent");
			tableCombo.getTable().getColumn(iCounter).addSelectionListener(
					colListener);

		}

		tableCombo.getTextControl().addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				UIUtilities.setTableComboBounds(tableCombo);
			}
		});
		tableCombo.getTable().getColumn(2).setResizable(false);
		tableCombo.setEnabled(!readOnly);

        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, "AssociatedComponent");
        if (prefs.load()) {
            tableCombo.getTable().setSortColumn(
                tableCombo.getTable().getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                tableCombo.getTable().setSortDirection(SWT.UP);
            }
            else {
                tableCombo.getTable().setSortDirection(SWT.DOWN);
            }                   
        }
        else {
            tableCombo.getTable().setSortColumn(tableCombo.getTable().getColumn(1));
            tableCombo.getTable().setSortDirection(SWT.UP);
        }       
		
        // replace the default windows context menu
        UIUtilities.replaceMenuTextCopy(tableCombo.getTextControl());
		
		return tableComboViewer;

	}

	/**
	 * This method creates Group of specific SWTstyle.If Group is of type
	 * ECLIPSE_FORM then this method adapts it to be used in a form that is
	 * associated with this toolkit. This involves adjusting colors and
	 * optionally adding handlers to ensure focus tracking and keyboard
	 * management.
	 * 
	 * @param composite
	 *            on which Group to be created.
	 * @param SWTstyle
	 *            of the Group to create.It is defined in class {@link SWT}.
	 * @param text
	 *            Title for the Group.
	 * @return Group of specific SWT style.
	 */

	public Group createGroup(Composite composite, int SWTstyle, String text) {

		Group group = new Group(composite, SWT.NONE);

		if (controlType == ECLIPSE_FORM) {
			toolkit.adapt(group, true, true);
		}
		group.setText(text);
		return group;
	}

	/**
	 * This method creates DateTime of specific SWTstyle.If DateTime is of type
	 * ECLIPSE_FORM then this method adapts it to be used in a form that is
	 * associated with this toolkit. This involves adjusting colors and
	 * optionally adding handlers to ensure focus tracking and keyboard
	 * management.
	 * 
	 * @param composite
	 *            on which DateTime to be created.
	 * @param SWTstyle
	 *            of the DateTime to create.It is defined in class {@link SWT}.
	 * @return DateTime of specific SWT style.
	 */

	public DateTime createDateTime(Composite composite, int SWTstyle) {

		DateTime dateTime = new DateTime(composite, SWTstyle);

		if (controlType == ECLIPSE_FORM) {
			toolkit.adapt(dateTime, true, false);
		}
		return dateTime;
	}

	/**
	 * This method creates a Eclipse Form Section of specific sectionStyle.
	 * Sections are only used on Eclipse forms, for ONLY_SWT type, this function
	 * just returns <code>null</code>.
	 * 
	 * @param composite
	 *            on which Section to be created.
	 * @param sectionStyle
	 *            SWT style of the Section to be created.
	 * @param text
	 *            the section text.
	 * @return Section of specific sectionStyle or <code>null</code> if the type
	 *         is ONLY_SWT.
	 */
	public Section createSection(Composite composite, int sectionStyle,
			String text) {

		Section section = null;

		if (controlType == ECLIPSE_FORM) {
			section = toolkit.createSection(composite, sectionStyle);
			section.setText(text);
		}
		return section;
	}

	/**
	 * This method creates Composite of specific Style.The style value is either
	 * one of the style constants defined in class {@link SWT} which is
	 * applicable to instances of this class.
	 * 
	 * @param parent
	 *            the composite parent.
	 * @param style
	 *            the composite style.
	 * @return the composite widget.
	 */
	public Composite createComposite(Composite parent, int style) {

		Composite composite = null;
		if (controlType == ECLIPSE_FORM) {
			composite = toolkit.createComposite(parent, style);
		} else {
			composite = new Composite(parent, style);
		}
		return composite;
	}

	/**
	 * This method is used to get current FormToolkit. This method is applicable
	 * only for SAFRGUIToolkit having FormToolkit. It returns null if
	 * SAFRGUIToolkit is of type ONLY_SWT.
	 * 
	 * @return current FormToolkit.
	 */
	public FormToolkit createToolkit() {
		return toolkit;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * A class implementing {@link VerifyListener}.It is used to add SAFR
	 * application specific verify listener for Text widgets.It checks whether
	 * text in text box is according to business logic.This listener should be
	 * added if the text box has WidgetType of String. Then text box will
	 * convert all characters or string into upper case.
	 * 
	 */
	class VerifyUpperCaseListener implements VerifyListener {

		public void verifyText(VerifyEvent event) {
			String myText = event.text;
			event.text = myText.toUpperCase();

			// Get the character typed
			char myChar = event.character;
			event.character = Character.toUpperCase(myChar);

		}

	}

	public void setSorter(ComponentType componentType,
			TableComboViewer tableComboViewer, int colNumber, int dir,
			ViewerSorter sorter) {
		if (componentType == ComponentType.ViewFolder) {
			sorter = new ViewFolderTableSorter(colNumber, dir);
		} else if (componentType == ComponentType.ControlRecord) {
			sorter = new ControlRecordTableSorter(colNumber, dir);
		} else if (componentType == ComponentType.UserExitRoutine) {
			sorter = new UserExitRoutineTableSorter(colNumber, dir);
		} else if (componentType == ComponentType.LogicalFile) {
			sorter = new LogicalFileTableSorter(colNumber, dir);
		} else if (componentType == ComponentType.PhysicalFile) {
			sorter = new PhysicalFileTableSorter(colNumber, dir);
		} else if (componentType == ComponentType.LogicalRecord) {
			sorter = new LogicalRecordTableSorter(colNumber, dir);
		} else if (componentType == ComponentType.Environment) {
			sorter = new EnvironmentTableSorter(colNumber, dir);
		} else if (componentType == ComponentType.Group) {
			sorter = new GroupTableSorter(colNumber, dir);
		} else if (componentType == ComponentType.LookupPath) {
			sorter = new LookupTableSorter(colNumber, dir);
		} else if (componentType == ComponentType.LogicalRecordField) {
			sorter = new LogicalRecordFieldTableSorter(colNumber, dir);
		} else if (componentType == ComponentType.View) {
			sorter = new ViewTableSorter(colNumber, dir);
		}
		tableComboViewer.setSorter(sorter);
	}

	public static void dumpActivePage(String context) {
		System.out.print("\n" + context + "\n");
		IWorkbenchWindow aw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(aw != null) { 
			IWorkbenchPage page = aw.getActivePage();
			System.out.print("Page\n" +page.toString()+ "\n");
			System.out.print("Editors\n");
			IEditorReference[] erefs = page.getEditorReferences();
			for(int i=0; i<erefs.length; i++) {
				System.out.print(erefs[i].getName() +"\n");
			}
			System.out.print("Views\n");
			IViewReference[] vrefs = page.getViewReferences();
			for(int i=0; i<vrefs.length; i++) {
				System.out.print(vrefs[i].getPartName() +"\n");
			}
		}
	}
}
