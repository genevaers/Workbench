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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewSortKey;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;
import com.ibm.safr.we.ui.utilities.IRowEditingSupport;
import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.RowEditorType;
import com.ibm.safr.we.ui.utilities.SAFRCheckboxCellEditor;
import com.ibm.safr.we.ui.utilities.SAFRComboBoxCellEditor;
import com.ibm.safr.we.ui.utilities.SAFRTextCellEditor;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.utilities.VerifyNumericListener;
import com.ibm.safr.we.ui.utilities.WidgetType;
import com.ibm.safr.we.utilities.SAFRLogger;

public class SortKeyView extends ViewPart implements IPartListener2,
		ISelectionListener {

	public static String ID = "SAFRWE.SortKeyView";

	// Row Ids:
	private static final int COLUMN = 0;
	private static final int STARTPOSITION = 1;
	private static final int SORTKEYNUMBER = 2;
	private static final int SORTSEQUENCE = 3;
	private static final int DATATYPE = 4;
	private static final int DATETIMEFORMAT = 5;
	private static final int LENGTH = 6;
	private static final int DECIMALPLACES = 7;
	private static final int SIGNED = 8;
	private static final int DISPLAYMODE = 9;
	private static final int SORTKEYLABEL = 10;
	private static final int SORTKEYFOOTERLABEL = 11;
	private static final int SORTKEYHEADEROPTION = 12;
	private static final int SORTKEYFOOTEROPTION = 13;

	private Label label;
	private Composite composite;

	private GridTableViewer sortKeyTableViewer;

	ViewSortKey sortKey;
	ViewSortKey previousSortKey;
	ViewEditor viewEditor;

	private CellEditor currentCellEditor;

	private final List<Mediator> sortKeyRows = new ArrayList<Mediator>();

	final int[] rowsToInclude = { COLUMN, STARTPOSITION, SORTKEYNUMBER,
			SORTSEQUENCE, DATATYPE, DATETIMEFORMAT, LENGTH, DECIMALPLACES,
			SIGNED, DISPLAYMODE, SORTKEYLABEL, SORTKEYFOOTERLABEL,
			SORTKEYHEADEROPTION, SORTKEYFOOTEROPTION };

	private final String[] rowHeaders = { "Column", "Start Position",
			"Sort Key Number", "Sort Sequence", "Data Type",
			"Date/Time Format", "Length", "Decimal Places", "Signed",
			"Display Mode", "Sort Key Label", "Sort Key Footer Label",
			"Sort Key Header Option", "Sort Key Footer Option" };

	private static final int MAX_LENGTH = 5;
	private static final int MAX_DECIMAL_PLACES = 1;
	private static final int MAX_LABEL = 48;

	@Override
	public void createPartControl(Composite parent) {
		// Add rows to be included in the table to a temp array.
		for (int row : rowsToInclude) {
			sortKeyRows.add(new Mediator(sortKey, row));
		}

		// listen to workbench part lifecycle activities. This is necessary so
		// that this view can react when the View editor closed or when other
		// editor gets focus.
		getSite().getPage().addPartListener(this);

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FormLayout());
		composite.setLayoutData(new FormData());

		label = new Label(composite, SWT.NONE);
		label.setText("Properties unavailable");
		label.setVisible(false);
		FormData labelData = new FormData();
		labelData.top = new FormAttachment(0, 0);
		labelData.bottom = new FormAttachment(100, 0);
		labelData.left = new FormAttachment(0, 0);
		labelData.right = new FormAttachment(100, 0);
		label.setLayoutData(labelData);

		sortKeyTableViewer = new GridTableViewer(composite, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sortKeyTableViewer.getGrid().setLinesVisible(true);
		sortKeyTableViewer.getGrid().setHeaderVisible(true);
		sortKeyTableViewer.setContentProvider(new ContentProvider());
		sortKeyTableViewer.getGrid().setRowHeaderVisible(true);

		sortKeyTableViewer.getGrid().setCellSelectionEnabled(true);

		sortKeyTableViewer.setRowHeaderLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String propertyName;
				Mediator m = (Mediator) element;
				propertyName = m.getPropertyName();
				return propertyName;
			}
		});

		FormData tableData = new FormData();
		tableData.top = new FormAttachment(0, 0);
		tableData.bottom = new FormAttachment(100, 0);
		tableData.left = new FormAttachment(0, 0);
		tableData.right = new FormAttachment(100, 0);
		tableData.width = 0;
		tableData.height = 0;
		sortKeyTableViewer.getGrid().setLayoutData(tableData);

		final GridViewerColumn column = new GridViewerColumn(
				sortKeyTableViewer, SWT.NONE);

		column.setLabelProvider(new LabelProviderImpl());
		column.setEditingSupport(new EditingSupportImpl(sortKeyTableViewer));
		column.getColumn().setWidth(200);
		column.getColumn().setResizeable(true);

		// need to call this, otherwise the table won't work.
		sortKeyTableViewer.setInput(1);

		// listen to workbench selections. This will allow the view to refresh
		// based on which sort key is selected on View editor.
		getSite().getPage().addSelectionListener(this);

		composite.addListener(SWT.Resize, new Listener() {
			// resize the table to occupy the whole view area, even if the user
			// resizes it.
			public void handleEvent(Event event) {

				column.getColumn().setWidth(
						composite.getSize().x
								- sortKeyTableViewer.getGrid()
										.getItemHeaderWidth());

			}
		});
		
		sortKeyTableViewer.getGrid().addTraverseListener(new TraverseListener() {

            private void editNeighborCell(TraverseEvent e, Point pt, List<Mediator> visibleRows, boolean up) {
                
                GridViewerEditor editor = (GridViewerEditor)sortKeyTableViewer.getColumnViewerEditor();
                GridItem item = sortKeyTableViewer.getGrid().getItem(pt.y);
                ViewerRow row = new GridViewerRow(item);
                ViewerCell cell = editor.searchCellAboveBelow(editor, row, editor.getGridViewer(), pt.x, up);
                if (cell == null) {
                    return;
                }
                boolean found = false;
                GridItem items[] = sortKeyTableViewer.getGrid().getItems();
                int rowIdx = 0;
                for ( ; rowIdx <items.length ; rowIdx++) {
                    if (((GridItem)cell.getViewerRow().getItem()) == items[rowIdx]) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    sortKeyTableViewer.editElement(visibleRows.get(rowIdx), cell.getColumnIndex());
                }
            }
            
            public void keyTraversed(TraverseEvent e) {
                e.doit = false;
                if (sortKeyTableViewer.getGrid().getItemCount() == 0 ||
                        sortKeyTableViewer.getGrid().getCellSelectionCount() == 0) {
                    return;
                }
                Point pt = sortKeyTableViewer.getGrid().getCellSelection()[0];
                if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
                    if (pt != null) {
                        editNeighborCell(e,pt,sortKeyRows,false);
                    }
                } 
                else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                    if (pt != null) {
                        editNeighborCell(e,pt,sortKeyRows,true);
                    }   
                } 
            }
        });
		
		sortKeyTableViewer.getGrid().addMouseListener(new MouseAdapter() {

		    @Override
			public void mouseDown(MouseEvent e) {
                Point pos = sortKeyTableViewer.getGrid().getCell(new Point(e.x, e.y));
                Grid g = (Grid) e.getSource();
                if (pos == null) {
	                g.setData(SAFRLogger.USER, null);                	
                }
                else {
	                g.setData(SAFRLogger.USER, rowHeaders[pos.y]);
                }
		    }

			@Override
            public void mouseUp(MouseEvent e) {
	        
                Point to = sortKeyTableViewer.getGrid().getCell(new Point(e.x, e.y));
                Grid g = (Grid) e.getSource();
                Point from = g.getFocusCell();
                
                if (e.x > g.getItemHeaderWidth()) {
                	// clicked right of row headers
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
		});
		
        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
                sortKeyTableViewer) {
            protected boolean isEditorActivationEvent(
                    ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                        || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.character == ' '))
                        || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
                        || (event.sourceEvent instanceof MouseEvent)
                        && ((MouseEvent) event.sourceEvent).button == 1;
            }
        };

        GridViewerEditor.create(sortKeyTableViewer, actSupport,
                ColumnViewerEditor.KEYBOARD_ACTIVATION
                | ColumnViewerEditor.TABBING_VERTICAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                | GridViewerEditor.SELECTION_FOLLOWS_EDITOR);
        
		showGridForCurrentEditor(getSite().getPage().getActiveEditor(), true);
	}

    public void editFirstCell() 
    {
        sortKeyTableViewer.editElement(sortKeyRows.get(2), 0);
    }
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	/**
	 * Represents a row in the sort key properties table. Every row in this
	 * table will have an element of this class attached to it.
	 * 
	 */
	private class Mediator implements IRowEditingSupport {
		private int propertyID;
		private String propertyDescription;
		private RowEditorType editorType;

		public Mediator(ViewSortKey sortKey, int propertyID) {
			this.propertyID = propertyID;

			switch (propertyID) {
			case COLUMN:
			case STARTPOSITION:
				this.editorType = RowEditorType.NONE;
				break;
			case SORTKEYNUMBER:
			case SORTSEQUENCE:
			case DATATYPE:
			case DATETIMEFORMAT:
			case DISPLAYMODE:
			case SORTKEYHEADEROPTION:
			case SORTKEYFOOTEROPTION:
				this.editorType = RowEditorType.COMBO;
				break;
			case LENGTH:
			case DECIMALPLACES:
				this.editorType = RowEditorType.NUMBER;
				break;
			case SORTKEYLABEL:
			case SORTKEYFOOTERLABEL:
				this.editorType = RowEditorType.TEXT;
				break;
			case SIGNED:
				this.editorType = RowEditorType.CHECKBOX;
				break;
			default:
				break;
			}
		}

		public int getPropertyID() {
			return propertyID;
		}

		public String getPropertyName() {
			propertyDescription = rowHeaders[propertyID];
			return propertyDescription;
		}

		public RowEditorType getRowEditorType() {
			return editorType;
		}

		public void setValue(int index, Object value) {

			switch (propertyID) {
			case COLUMN:
			case STARTPOSITION:
				break;
			case SORTKEYNUMBER:
				sortKey.setKeySequenceNo((Integer) value);
				break;
			case SORTSEQUENCE:
				// mandatory Code field. call setter only if value is not null,
				// else a NPE will be thrown.
				if (value != null) {
					sortKey.setSortSequenceCode((Code) value);
				}
				break;
			case DATATYPE:
				// mandatory Code field. call setter only if value is not null,
				// else a NPE will be thrown.
				if (value != null) {
					sortKey.setDataTypeCode((Code) value);
				}
				break;
			case DATETIMEFORMAT:
				sortKey.setDateTimeFormatCode((Code) value);
				break;
			case LENGTH:
				sortKey.setLength(UIUtilities.stringToInteger((String) value));
				break;
			case DECIMALPLACES:
				sortKey.setDecimalPlaces(UIUtilities
						.stringToInteger((String) value));
				break;
			case SIGNED:
				sortKey.setSigned((Boolean) value);
				break;
			case DISPLAYMODE:
				try {
					sortKey.setDisplayModeCode((Code) value);
				} catch (SAFRException e) {
					UIUtilities.handleWEExceptions(e,"Error occurred while setting display mode value.",null);
				}
				break;
			case SORTKEYLABEL:
				sortKey.setSortkeyLabel((String) value);
				break;
			case SORTKEYFOOTERLABEL:
				sortKey.getViewColumn().setSortkeyFooterLabel((String) value);
				break;
			case SORTKEYHEADEROPTION:
				// mandatory Code field. call setter only if value is not null,
				// else a NPE will be thrown.
				if (value != null) {
					sortKey.setHeaderOption((Code) value);
				}
				break;
			case SORTKEYFOOTEROPTION:
				// mandatory Code field. call setter only if value is not null,
				// else a NPE will be thrown.
				if (value != null) {
					sortKey.setFooterOption((Code) value);
				}
				break;

			default:
				break;
			}
		}

		public Object getValue(int index) {
			switch (propertyID) {
			case COLUMN:
				return sortKey.getViewColumn().getColumnNo().toString();
			case STARTPOSITION:
				return sortKey.getStartPosition().toString();
			case SORTKEYNUMBER:
				return sortKey.getKeySequenceNo().toString();
			case SORTSEQUENCE:
				return sortKey.getSortSequenceCode();
			case DATATYPE:
				return sortKey.getDataTypeCode();
			case DATETIMEFORMAT:
				return sortKey.getDateTimeFormatCode();
			case LENGTH:
				return sortKey.getLength().toString();
			case DECIMALPLACES:
				return sortKey.getDecimalPlaces().toString();
			case SIGNED:
				return sortKey.isSigned();
			case DISPLAYMODE:
				return sortKey.getDisplayModeCode();
			case SORTKEYLABEL:
				return sortKey.getSortkeyLabel();
			case SORTKEYFOOTERLABEL:
				return sortKey.getViewColumn().getSortkeyFooterLabel();
			case SORTKEYHEADEROPTION:
				return sortKey.getHeaderOptionCode();
			case SORTKEYFOOTEROPTION:
				return sortKey.getFooterOptionCode();
			default:
				return null;
			}
		}

		public Object getData() {
			return null;
		}

	}

	private class ContentProvider implements IStructuredContentProvider {
		public ContentProvider() {
		}

		public Object[] getElements(Object inputElement) {
			if (sortKey != null) {
				return sortKeyRows.toArray();
			} else {
				return new String[] {};
			}
		}

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub

		}
	}

	private class LabelProviderImpl extends ColumnLabelProvider {
		public LabelProviderImpl() {

		}

		@Override
		public String getText(Object element) {
			Mediator m = (Mediator) element;
			switch (m.getPropertyID()) {
			// CQ 8056. Santhosh. 21/06/2010
			// Clear the cells at UI if they don't apply to the current
			// context.
			case SIGNED:
				if (!enableViewEditorCells(m.getPropertyID())) {
					return null;
				} else if (sortKey.isSigned() != null) {

					return "";

				}
				break;
			case DATETIMEFORMAT:
				if (!enableViewEditorCells(m.getPropertyID())) {

					return null;

				} else if (sortKey.getDateTimeFormatCode() != null) {
					return sortKey.getDateTimeFormatCode().getDescription();
				}
				break;
			case DECIMALPLACES:
				if (!enableViewEditorCells(m.getPropertyID())) {

					return null;

				} else if (sortKey.getDecimalPlaces() != null) {
					return sortKey.getDecimalPlaces().toString();
				}
				break;

			case DISPLAYMODE:
				if (!enableViewEditorCells(m.getPropertyID())) {

					return null;

				} else if (sortKey.getDisplayModeCode() != null) {
					return sortKey.getDisplayModeCode().getDescription();
				}
				break;
			case SORTKEYLABEL:
				if (!enableViewEditorCells(m.getPropertyID())) {

					return null;

				} else if (sortKey.getSortkeyLabel() != null) {
					return sortKey.getSortkeyLabel().toString();
				}
				break;
			case SORTKEYFOOTERLABEL:
				if (!enableViewEditorCells(m.getPropertyID())) {

					return null;

				} else if (sortKey.getViewColumn().getSortkeyFooterLabel() != null) {
					return sortKey.getViewColumn().getSortkeyFooterLabel()
							.toString();
				}
				break;

			case SORTKEYHEADEROPTION:
				if (!enableViewEditorCells(m.getPropertyID())) {

					return null;

				} else if (sortKey.getHeaderOptionCode() != null) {
					return sortKey.getHeaderOptionCode().getDescription();
				}
				break;

			case SORTKEYFOOTEROPTION:
				if (!enableViewEditorCells(m.getPropertyID())) {

					return null;

				} else if (sortKey.getFooterOptionCode() != null) {
					return sortKey.getFooterOptionCode().getDescription();
				}
				break;

			default:
				if (m.getValue(0) == null) {
					return "";
				} else if (m.getValue(0) instanceof Code) {
					return ((Code) m.getValue(0)).getDescription();
				} else {
					return m.getValue(0).toString();
				}
			}
			return "";
		}

		@Override
		public Image getImage(Object element) {
			Mediator m = (Mediator) element;
			switch (m.getPropertyID()) {

			case SIGNED:
				if (enableViewEditorCells(m.getPropertyID())) {
					if (sortKey.isSigned()) {
						return UIUtilities
								.getAndRegisterImage(ImageKeys.CHECKED);
					} else {
						return UIUtilities
								.getAndRegisterImage(ImageKeys.UNCHECKED);
					}
				} else if (sortKey.getDataTypeCode() != null
						&& sortKey.getDataTypeCode().getGeneralId() == Codes.EDITED_NUMERIC) {
					return UIUtilities.getAndRegisterImage(ImageKeys.CHECKED);
				} else {
					return null;
				}

				// if (enableDisableViewEditorCells(m.getPropertyID())
				// || sortKey.getDataTypeCode().getGeneralId() ==
				// Codes.EDITED_NUMERIC) {
				// if (sortKey.isSigned()) {
				//
				// return UIUtilities
				// .getAndRegisterImage(ImageKeys.CHECKED);
				// } else {
				// return UIUtilities
				// .getAndRegisterImage(ImageKeys.UNCHECKED);
				// }
				// }
				// else {
				// return null;
				// }

			default:
				return null;
			}
		}

		@Override
		public Color getBackground(Object element) {
			Mediator m = (Mediator) element;

			if (m.getRowEditorType() == RowEditorType.NONE) {
				// no editor means the cell is disabled. set its color to grey.
				return UIUtilities.getColorForDisabledCell();
			}

			if ((m.getRowEditorType() != RowEditorType.NONE)
					&& !enableViewEditorCells(m.getPropertyID())) {
				return UIUtilities.getColorForDisabledCell();
			}
			return super.getBackground(element);
		}

	}

    private boolean enableViewEditorCells(int propertyID) {
        return enableViewEditorCells(propertyID, null, null);
    }
	
    private boolean enableViewEditorCells(int propertyID, Code dataTypeCode, Code displayMode) {
        if (dataTypeCode == null) {
            dataTypeCode = sortKey.getDataTypeCode();
        }
        if (dataTypeCode != null) {
            Integer dataType = dataTypeCode.getGeneralId();
            OutputFormat outputFormat = sortKey.getView().getOutputFormat();
            if (displayMode == null) {
                displayMode = sortKey.getDisplayModeCode();
            }
            switch (propertyID) {
            case COLUMN:
            case STARTPOSITION:
                return false;
            case SORTKEYNUMBER:
            case SORTSEQUENCE:
            case DATATYPE:
            case LENGTH:
                return true;
            case DATETIMEFORMAT:
                if (dataType == Codes.EDITED_NUMERIC
                        || dataType == Codes.MASKED_NUMERIC) {
                    return false;
                } else {
                    return true;
                }
            case DECIMALPLACES:
                if (dataType == Codes.ALPHANUMERIC) {
                    return false;
                } else {
                    return true;
                }
            case SIGNED:
                if (dataType == Codes.ALPHANUMERIC
                        || dataType == Codes.BINARY_CODED_DECIMAL
                        || dataType == Codes.EDITED_NUMERIC
                        || (dataType == Codes.BINARY && sortKey
                                .getDateTimeFormatCode() != null)) {
                    return false;
                } else {
                    return true;
                }
            case DISPLAYMODE:
                if (outputFormat != null) {
                    if (outputFormat == OutputFormat.Format_Report) {
                        return true;
                    } else {
                        return false;
                    }
                }
                break;
            case SORTKEYLABEL:
                if (outputFormat != null) {
                    if (outputFormat == OutputFormat.Format_Report) {
                        if (displayMode != null 
                                && displayMode.getGeneralId() == Codes.ASDATA) {
                            return false;
                        } else {
                            return true;
                        }
                    } else {
                        return false;
                    }

                }
                break;
            case SORTKEYHEADEROPTION:
                if (outputFormat != null && outputFormat == OutputFormat.Format_Report) {
                    if ((displayMode != null
                        && displayMode.getGeneralId() == Codes.ASDATA)) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }

            case SORTKEYFOOTERLABEL:
                if (outputFormat != null && outputFormat == OutputFormat.Format_Report) {
                    if ( (displayMode != null 
                            && displayMode.getGeneralId() == Codes.ASDATA) ) {
                        return false;
                    }
                    else {
                        return true;
                    }
                } else {
                    return false;
                }
                
            case SORTKEYFOOTEROPTION:
                if (outputFormat != null && 
                    outputFormat == OutputFormat.Format_Report) {
                    return true;                                
                } else {
                    return false;                                
                }                   
            }
        }
        return true;

    }
	
	private class EditingSupportImpl extends EditingSupport {

		private GridTableViewer viewer;
		private CellEditor editor;
		private CCombo displayMode;
		private CCombo dataTypeCombo;
		private CCombo editorCombo;

		public EditingSupportImpl(final GridTableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected boolean canEdit(Object element) {
			ViewEditorInput viewEditorInput = (ViewEditorInput) viewEditor
					.getEditorInput();
			if (viewEditorInput.getEditRights() == EditRights.Read) {
				return false;
			}
            Mediator m = (Mediator) element;

            if (m.getRowEditorType() == RowEditorType.NONE) {
                // no editor means the cell is disabled. set its color to grey.
                return false;
            }

            Code dataTypeCode = null;
            if (dataTypeCombo != null) {
                dataTypeCode = (Code) dataTypeCombo.getData(new Integer(
                        dataTypeCombo.getSelectionIndex()).toString());                
            }
            Code displayModeCode = null;
            if (displayMode != null) {
                displayModeCode = (Code) displayMode.getData(new Integer(
                        displayMode.getSelectionIndex()).toString());
            }
            if ((m.getRowEditorType() != RowEditorType.NONE)
                    && !enableViewEditorCells(m.getPropertyID(), dataTypeCode, displayModeCode)) {
                return false;
            }
            return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			setCurrentCellEditor(null);
			Mediator m = (Mediator) element;
			if (m.getRowEditorType() == RowEditorType.COMBO) {
				editor = new SAFRComboBoxCellEditor(viewer.getGrid(),
						new String[] {}, SWT.READ_ONLY);
                ((ComboBoxCellEditor)editor).setActivationStyle(
                        ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
                        ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
                        ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
                        ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);  
				editorCombo = (CCombo) editor.getControl();
				switch (m.getPropertyID()) {
				case SORTKEYNUMBER:
					loadSortKeyNumbers(editorCombo);
					break;
				case SORTSEQUENCE:
					UIUtilities.populateComboBox(editorCombo,
							CodeCategories.SORTSEQ, 0, false);
					break;
				case DATATYPE:
					UIUtilities.populateComboBox(editorCombo,
							CodeCategories.DATATYPE, 0, false);
					dataTypeCombo = editorCombo;
					break;
				case DATETIMEFORMAT:
					UIUtilities.populateComboBox(editorCombo,
							CodeCategories.FLDCONTENT, 0, true);
					break;
				case DISPLAYMODE:
					UIUtilities.populateComboBox(editorCombo,
							CodeCategories.SORTDSP, 0, true);
					displayMode = editorCombo;
					break;
				case SORTKEYHEADEROPTION:
					UIUtilities.populateComboBox(editorCombo,
							CodeCategories.SORTBRKHDR, 1, false);
					break;
				case SORTKEYFOOTEROPTION:
					UIUtilities.populateComboBox(editorCombo,
							CodeCategories.SORTBRKFTR, 0, false);
					break;
				default:
					editor = null;
					break;
				}
				setCurrentCellEditor(editor);
			} else if (m.getRowEditorType() == RowEditorType.TEXT
					|| m.getRowEditorType() == RowEditorType.NUMBER) {
				editor = new SAFRTextCellEditor(viewer.getGrid());

				if (m.getRowEditorType() == RowEditorType.TEXT) {
					if (m.getPropertyID() == SORTKEYFOOTERLABEL
							|| m.getPropertyID() == SORTKEYLABEL) {
						((Text) editor.getControl()).setTextLimit(MAX_LABEL);
					}
				} else if (m.getRowEditorType() == RowEditorType.NUMBER) {
					((Text) editor.getControl())
							.addVerifyListener(new VerifyNumericListener(
									WidgetType.INTEGER, false));

					if (m.getPropertyID() == LENGTH) {
						((Text) editor.getControl()).setTextLimit(MAX_LENGTH);
					} else if (m.getPropertyID() == DECIMALPLACES) {
						((Text) editor.getControl())
								.setTextLimit(MAX_DECIMAL_PLACES);
					}
				}
			} else if (m.getRowEditorType() == RowEditorType.CHECKBOX) {
				editor = new SAFRCheckboxCellEditor(viewer.getGrid());
			} else {
				editor = null;
			}

			return editor;
		}

		private void loadSortKeyNumbers(CCombo combo) {
			// load the combo box with sort key numbers available in this view.
			View view = sortKey.getView();
			for (Integer i = 1; i <= view.getViewSortKeys().getActiveItems()
					.size(); i++) {
				combo.add(i.toString());
			}
		}

		@Override
		protected Object getValue(Object element) {
			IRowEditingSupport m = (IRowEditingSupport) element;
			Object returnVal = m.getValue(0);
			if (m.getRowEditorType() == RowEditorType.COMBO) {
				if (returnVal == null) {
					return -1;
				} else if (returnVal instanceof Code) {
					return editorCombo.indexOf(((Code) returnVal)
							.getDescription());
				} else {
					return editorCombo.indexOf(returnVal.toString());
				}
			}
			if (returnVal == null) {
				return "";
			}
			return returnVal;
		}

		@Override
		protected void setValue(Object element, Object value) {
			Mediator m = (Mediator) element;
			Boolean dirtyFlag = false;

			if (m.getRowEditorType() == RowEditorType.COMBO) {
				if ((Integer) value >= 0) {
					if (m.getPropertyID() == SORTKEYNUMBER) {
						// for sort key numbers, the index of item selected in
						// combo is one less than the actual value.
						Integer previousSeqNo = sortKey.getKeySequenceNo();
						Integer currentSeqNo = ((Integer) value) + 1;
						if (!UIUtilities.isEqual(previousSeqNo, currentSeqNo)) {
							dirtyFlag = true;
							m.setValue(0, ((Integer) value) + 1);
							viewEditor.refreshSortKeyRows();
						}
					} else if (m.getPropertyID() == SORTSEQUENCE) {

						Code code = (Code) editorCombo
								.getData(value.toString());
						if (!UIUtilities.isEqual(m.getValue(0), code)) {
							m.setValue(0, code);
							viewEditor.updateSortKeyRows();
							dirtyFlag = true;
						}
					} else if (m.getPropertyID() == DISPLAYMODE) {
						Code code = (Code) editorCombo
								.getData(value.toString());
						if (!UIUtilities.isEqual(m.getValue(0), code)) {
							dirtyFlag = true;
							m.setValue(0, code);
							// activate enable/disable cell logic
							getViewer().refresh();
							// CQ 7356 : Jaydeep : 27 January 2010 .Replaced
							// showView() method by findView().
							// refresh the sort key title view.
							SortKeyTitleView sortKTV = (SortKeyTitleView) getSite()
									.getPage().findView(SortKeyTitleView.ID);
							if (sortKTV != null) {
								sortKTV.showGridForCurrentEditor(viewEditor,
										true);
							}

						}
					} else {
						Code code = (Code) editorCombo
								.getData(value.toString());
						if (!UIUtilities.isEqual(m.getValue(0), code)) {
							dirtyFlag = true;
							m.setValue(0, code);
							// activate enable/disable cell logic
							getViewer().refresh();
						}
					}
				} else {
					// nothing selected in combo
					m.setValue(0, null);
				}
			} else if (m.getPropertyID() == LENGTH
					|| m.getPropertyID() == DECIMALPLACES) {
				Integer previousIntValue = UIUtilities.stringToInteger(m
						.getValue(0).toString());
				Integer currentIntValue = UIUtilities.stringToInteger(value
						.toString());
				if (!UIUtilities.isEqual(previousIntValue, currentIntValue)) {
					dirtyFlag = true;
					m.setValue(0, value);
				}
			} else if (m.getPropertyID() == SIGNED) {
				dirtyFlag = true;
				m.setValue(0, value);
			} else if (m.getPropertyID() == SORTKEYLABEL
					|| m.getPropertyID() == SORTKEYFOOTERLABEL) {
				String previousStringValue = (String) m.getValue(0);
				String currentStringValue = (String) value;
				if (!UIUtilities.isEqual(previousStringValue,
						currentStringValue)) {
					dirtyFlag = true;
					m.setValue(0, value);
				}
			}
			getViewer().update(element, null);

			if (dirtyFlag) {
				viewEditor.setModified(true);
			}
		}

	}

	public void dispose() {
		super.dispose();
		// Its important to remove these listeners when the view is closed.
		getSite().getPage().removeSelectionListener(this);
		getSite().getPage().removePartListener(this);
	}

	public void partActivated(IWorkbenchPartReference partRef) {

		if (!(partRef.getPart(false) instanceof IEditorPart)) {
			return;
		}
		showGridForCurrentEditor((IEditorPart) partRef.getPart(false), true);
	}

	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		partActivated(partRef);
	}

	public void partClosed(IWorkbenchPartReference partRef) {
		if (!(partRef.getPart(false) instanceof IEditorPart)) {
			return;
		}

		if (partRef.getPart(false) instanceof ViewEditor) {
			// if a view editor is closed then hide the table.
			showGrid(false, true);

			// releasing resources when view editor is closed
			viewEditor = null;
			sortKey = null;
			previousSortKey = null;
		}

	}

	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void partOpened(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(part instanceof ViewEditor)) {
			return;
		}
		showGridForCurrentEditor((IEditorPart) part, true);
	}

	public void showGridForCurrentEditor(IEditorPart editor,
			boolean checkPrevious) {
		// show the table only if the current selection is a sort key and the
		// current editor is View editor.
		if (editor instanceof ViewEditor) {
			this.viewEditor = (ViewEditor) editor;

			if (viewEditor.isCurrentSortKey()) {
				previousSortKey = this.sortKey;
				this.sortKey = viewEditor.getCurrentSortKey();
				showGrid(true, checkPrevious);
			} else {
				showGrid(false, checkPrevious);
			}
		} else {
			showGrid(false, checkPrevious);
		}
	}

	private void showGrid(boolean show, boolean checkPrevious) {
		// Added extra parameter 'checkPrevious' to forcefully refresh the grid
		// without checking if user has clicked on another sort key if output
		// format of the View is changed
		if(label.isDisposed() == false) {
			label.setVisible(!show);
			sortKeyTableViewer.getGrid().setVisible(show);
			// Refresh the grid only if the Sort Key has changed.
			if (show || (checkPrevious && UIUtilities.isEqual(sortKey, previousSortKey))) {
				sortKeyTableViewer.refresh();
			}
		}
	}

	public void setCurrentCellEditor(CellEditor currentCellEditor) {
		this.currentCellEditor = currentCellEditor;
	}
	
}
