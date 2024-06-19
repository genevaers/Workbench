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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
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
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSortKey;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.commands.SourceProvider;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;
import com.ibm.safr.we.ui.utilities.AbstractTableComboViewerCellEditor;
import com.ibm.safr.we.ui.utilities.AssociatedLRFieldSorter;
import com.ibm.safr.we.ui.utilities.ColumnSelectionListenerForTableCombo;
import com.ibm.safr.we.ui.utilities.IRowEditingSupport;
import com.ibm.safr.we.ui.utilities.RowEditorType;
import com.ibm.safr.we.ui.utilities.SAFRComboBoxCellEditor;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.SAFRTextCellEditor;
import com.ibm.safr.we.ui.utilities.TableComboViewerCellEditor;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.utilities.VerifyNumericListener;
import com.ibm.safr.we.ui.utilities.WidgetType;
import com.ibm.safr.we.utilities.SAFRLogger;

public class SortKeyTitleView extends ViewPart implements IPartListener2, ISelectionListener {
	public static String ID = "SAFRWE.SortKeyTitleView";

	// Row Ids:
	private static final int VIEWSOURCES = 0;
	private static final int TITLEFIELD = 1;
	private static final int EFFECTIVEDATETYPE = 2;
	private static final int EFFECTIVEDATEVALUE = 3;
	private static final int TITLELENGTH = 4;

	private Label label;
	private Composite composite;

	boolean sameSortKeyFlag = false;
	ViewEditor vEditor;

	private GridTableViewer sortKeyTitleTableViewer;
	ViewSortKey sortKey;
	ViewSortKey previousSortKey;
	ViewColumnSource viewColumnSource;
	static ViewSource currentViewSource;
	List<ViewSource> viewSources;

	private final List<SortKeyTitleRow> sortKeyTitleRows = new ArrayList<SortKeyTitleRow>();

	final int[] rowsToInclude = { VIEWSOURCES, TITLEFIELD, EFFECTIVEDATETYPE,
			EFFECTIVEDATEVALUE, TITLELENGTH };
	private final String[] rowHeaders = { "View Source", "Title Field",
			"Effective Date Type", "Effective Date Value", "Title Length" };

	public TableComboViewer editorTableComboViewer;

	public TableCombo editorTableCombo;

	public SKTTableComboViewerCellEditor editor1;

	private CellEditor currentCellEditor;

	private static final int MAX_TITLE_LENGTH = 5;
	private static final int MAX_EFFECTIVE_DATE_VALUE = 24;

	@Override
	public void createPartControl(Composite parent) {
		for (int row : rowsToInclude) {
			sortKeyTitleRows.add(new SortKeyTitleRow(null, row));
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

		sortKeyTitleTableViewer = new GridTableViewer(composite, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sortKeyTitleTableViewer.getGrid().setLinesVisible(true);
		sortKeyTitleTableViewer.getGrid().setHeaderVisible(true);
		sortKeyTitleTableViewer.setContentProvider(new ContentProvider());
		sortKeyTitleTableViewer.getGrid().setRowHeaderVisible(true);

		sortKeyTitleTableViewer.getGrid().setCellSelectionEnabled(true);

		sortKeyTitleTableViewer
				.setRowHeaderLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						SortKeyTitleRow m = (SortKeyTitleRow) element;
						return m.getPropertyName();
					}
				});

		FormData tableData = new FormData();
		tableData.top = new FormAttachment(0, 0);
		tableData.bottom = new FormAttachment(100, 0);
		tableData.left = new FormAttachment(0, 0);
		tableData.right = new FormAttachment(100, 0);
		tableData.width = 0;
		tableData.height = 0;
		sortKeyTitleTableViewer.getGrid().setLayoutData(tableData);

		final GridViewerColumn column = new GridViewerColumn(
				sortKeyTitleTableViewer, SWT.NONE);

		column.setLabelProvider(new LabelProviderImpl());
		column
				.setEditingSupport(new EditingSupportImpl(
						sortKeyTitleTableViewer));
		column.getColumn().setWidth(200);
		column.getColumn().setResizeable(true);

		// need to call this, otherwise the table won't work.
		sortKeyTitleTableViewer.setInput(1);

		// listen to workbench selections. This will allow the view to refresh
		// based on which sort key is selected on View editor.
		getSite().getPage().addSelectionListener(this);

		composite.addListener(SWT.Resize, new Listener() {
			// resize the table to occupy the whole view area, even if the user
			// resizes it.
			public void handleEvent(Event event) {

				column.getColumn().setWidth(
						composite.getSize().x
								- sortKeyTitleTableViewer.getGrid()
										.getItemHeaderWidth());

			}
		});

		sortKeyTitleTableViewer.getGrid().addTraverseListener(new TraverseListener() {

            private void editNeighborCell(TraverseEvent e, Point pt, List<SortKeyTitleRow> visibleRows, boolean up) {
                
                GridViewerEditor editor = (GridViewerEditor)sortKeyTitleTableViewer.getColumnViewerEditor();
                GridItem item = sortKeyTitleTableViewer.getGrid().getItem(pt.y);
                ViewerRow row = new GridViewerRow(item);
                ViewerCell cell = editor.searchCellAboveBelow(editor, row, editor.getGridViewer(), pt.x, up);
                if (cell == null) {
                    return;
                }
                boolean found = false;
                GridItem items[] = sortKeyTitleTableViewer.getGrid().getItems();
                int rowIdx = 0;
                for ( ; rowIdx <items.length ; rowIdx++) {
                    if (((GridItem)cell.getViewerRow().getItem()) == items[rowIdx]) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    sortKeyTitleTableViewer.editElement(visibleRows.get(rowIdx), cell.getColumnIndex());
                }
            }
            
            public void keyTraversed(TraverseEvent e) {
                e.doit = false;
                if (sortKeyTitleTableViewer.getGrid().getItemCount() == 0 ||
                        sortKeyTitleTableViewer.getGrid().getCellSelectionCount() == 0) {
                    return;
                }
                Point pt = sortKeyTitleTableViewer.getGrid().getCellSelection()[0];
                if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
                    if (pt != null) {
                        editNeighborCell(e,pt,sortKeyTitleRows,false);
                    }
                } 
                else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                    if (pt != null) {
                        editNeighborCell(e,pt,sortKeyTitleRows,true);
                    }   
                } 
            }
        });
		
		sortKeyTitleTableViewer.getGrid().addMouseListener(new MouseAdapter() {

		    @Override
			public void mouseDown(MouseEvent e) {
                Point pos = sortKeyTitleTableViewer.getGrid().getCell(new Point(e.x, e.y));
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
	        
                Point to = sortKeyTitleTableViewer.getGrid().getCell(new Point(e.x, e.y));
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
                sortKeyTitleTableViewer) {
            protected boolean isEditorActivationEvent(
                    ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                        || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.character == ' '))
                        || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
                        || (event.sourceEvent instanceof MouseEvent)
                        && ((MouseEvent) event.sourceEvent).button == 1;
            }
        };

        GridViewerEditor.create(sortKeyTitleTableViewer, actSupport,
                ColumnViewerEditor.KEYBOARD_ACTIVATION
                | ColumnViewerEditor.TABBING_VERTICAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                | GridViewerEditor.SELECTION_FOLLOWS_EDITOR);
        
		showGridForCurrentEditor(getSite().getPage().getActiveEditor(), true);

		// Code for tracking the focus on the grid
		IFocusService service = (IFocusService) getSite().getService(
				IFocusService.class);
		service.addFocusTracker(sortKeyTitleTableViewer.getGrid(),
				"SortKeyTitleGrid");
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
	private class SortKeyTitleRow implements IRowEditingSupport {
		private int propertyID;
		// the column source this mediator will work on.
		private String propertyDescription;
		private RowEditorType editorType;

		public SortKeyTitleRow(ViewSource viewSource, int propertyID) {
			this.propertyID = propertyID;
			switch (propertyID) {
			case VIEWSOURCES:
			case TITLEFIELD:
			case EFFECTIVEDATETYPE:
				this.editorType = RowEditorType.COMBO;
				break;
			case EFFECTIVEDATEVALUE:
				this.editorType = RowEditorType.TEXT;
				break;
			case TITLELENGTH:
				this.editorType = RowEditorType.NUMBER;
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

		public void setRowEditorType(RowEditorType editorType) {
			this.editorType = editorType;
		}

		@SuppressWarnings("unchecked")
        public void setValue(int index, Object value) {
			switch (propertyID) {
			case VIEWSOURCES:
				if (value != null) {
					// Set the selected view source in a variable. This will be
					// used
					// to load title fields.
					currentViewSource = (ViewSource) value;
					// get the column source represented by the selected View
					// source and the
					// column on which this sort key is defined.
					viewColumnSource = sortKey.getViewColumn()
							.getViewColumnSources().get(
									currentViewSource.getSequenceNo() - 1);
				}
				break;
			case TITLEFIELD:
				if (viewColumnSource != null) {
					if (value != null) {
						// value in this case is a list, having first item as
						// Sort
						// key
						// title lookup and 2nd item as Sort key title field.
						LookupQueryBean lookup = (LookupQueryBean) ((List<Object>) value)
								.get(0);
						viewColumnSource
								.setSortKeyTitleLookupPathQueryBean(lookup);
						LRField field = (LRField) ((List<Object>) value).get(2);
						viewColumnSource.setSortKeyTitleLRField(field);
	                    // must also set this in the sort key table
	                    sortKey.setTitleField(field);
					}
				}
				break;
			case EFFECTIVEDATETYPE:
				viewColumnSource.setEffectiveDateTypeCode((Code) value);
				if (value != null && ((Code) value).getGeneralId() == Codes.RELPERIOD_RUNDATE) {
					viewColumnSource.setEffectiveDateValue("Runday()");
				}
				break;
			case EFFECTIVEDATEVALUE:
				Code effectiveDateType = viewColumnSource
						.getEffectiveDateTypeCode();
				if (effectiveDateType != null) {
					if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_CONSTANT) {
						viewColumnSource.setEffectiveDateValue((String) value);
					} else if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_RUNDATE) {
						viewColumnSource.setEffectiveDateValue("Runday()");
					} else if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_SOURCE_FILE_FIELD) {
						if (value != null)
							viewColumnSource
									.setEffectiveDateLRField((LRField) value);
					}
				}
				break;
			case TITLELENGTH:
				sortKey.setTitleLength(UIUtilities
						.stringToInteger((String) value));
				break;
			default:
				break;
			}

		}

		public Object getValue(int index) {
			try {
				switch (propertyID) {
				case VIEWSOURCES:
					if (currentViewSource != null) {
						return getAssocString(currentViewSource
								.getLrFileAssociation());
					}
					break;
				case TITLEFIELD:
					if (viewColumnSource != null) {
						return getLrLookupFieldString(viewColumnSource
								.getSortKeyTitleLogicalRecordQueryBean(),
								viewColumnSource
										.getSortKeyTitleLookupPathQueryBean(),
								viewColumnSource.getSortKeyTitleLRField());

					}
					break;
				case EFFECTIVEDATETYPE:
					return viewColumnSource.getEffectiveDateTypeCode();
				case EFFECTIVEDATEVALUE:
					Code effectiveDateType = viewColumnSource
							.getEffectiveDateTypeCode();
					if (effectiveDateType != null) {
						if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_CONSTANT) {
							return viewColumnSource.getEffectiveDateValue();
						} else if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_RUNDATE) {
							return viewColumnSource.getEffectiveDateValue();
						} else if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_SOURCE_FILE_FIELD) {
							LRField effectiveDateLRField = viewColumnSource
									.getEffectiveDateLRField();
							if (effectiveDateLRField != null) {
								return UIUtilities.getComboString(
										effectiveDateLRField.getName(),
										effectiveDateLRField.getId());
							}
						}
					}
					break;
				case TITLELENGTH:
					if (sortKey.getTitleLength() != null) {
						return sortKey.getTitleLength().toString();
					}
					break;
				default:
					break;
				}
			} catch (SAFRException e) {
				UIUtilities.handleWEExceptions(e);
			}
			return null;
		}

		public Object getData() {
			return null;
		}

	}

	private class ContentProvider implements IStructuredContentProvider {
		public ContentProvider() {
		}

		public Object[] getElements(Object inputElement) {
			if (viewSources != null && !viewSources.isEmpty()) {
				if (currentViewSource == null || sameSortKeyFlag == false) {
					// sameSortKeyFlag is used to check if user has clicked on
					// same sort key or another one

					// this is the first time this view is loaded.
					// by default make the first view source as the current
					// source and also initialize the view column source.
					currentViewSource = viewSources.get(0);
					// get the view column source of the sortkey column, from
					// the first view source.
					viewColumnSource = sortKey.getViewColumn()
							.getViewColumnSources().get(
									currentViewSource.getSequenceNo() - 1);
					sameSortKeyFlag = true;
				}
				enableDisableDeleteButton();
				return sortKeyTitleRows.toArray();
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
			SortKeyTitleRow m = (SortKeyTitleRow) element;
			if (!(enableCells(m.getPropertyID()))) {
				return null;
			}
			switch (m.getPropertyID()) {
			// case EFFECTIVEDATETYPE:
			// if (!(enableDisableCells(m.getPropertyID()))) {
			// return null;
			// } else if (viewColumnSource.getEffectiveDateTypeCode() != null) {
			// return viewColumnSource.getEffectiveDateTypeCode()
			// .getDescription().toString();
			// }
			// break;
			// case EFFECTIVEDATEVALUE:
			// if (!(enableDisableCells(m.getPropertyID()))) {
			// return null;
			// } else if (viewColumnSource.getEffectiveDateValue() != null) {
			// return viewColumnSource.getEffectiveDateValue().toString();
			// }
			// break;
			// case TITLELENGTH:
			// if (!(enableDisableCells(m.getPropertyID()))) {
			// return null;
			// } else if (sortKey.getTitleLength() != null) {
			// return sortKey.getTitleLength().toString();
			// }
			// break;
			default:
				if (m.getValue(0) == null) {
					return "";
				} else if (m.getValue(0) instanceof Code) {
					return ((Code) m.getValue(0)).getDescription();
				} else {
					return m.getValue(0).toString();
				}
			}
		}

		public Image getImage(Object element) {
			return null;

		}

		@Override
		public Color getBackground(Object element) {
			SortKeyTitleRow m = (SortKeyTitleRow) element;
			if (m.getRowEditorType() == RowEditorType.NONE) {
				// no editor means the cell is disabled. set its color to grey.
				return Display.getCurrent().getSystemColor(
						SWT.COLOR_WIDGET_LIGHT_SHADOW);
			}
			if (m.getPropertyID() == EFFECTIVEDATEVALUE) {
				Code effectiveDateType = viewColumnSource
						.getEffectiveDateTypeCode();
				if (effectiveDateType != null) {
					if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_RUNDATE
							|| !(enableCells(m.getPropertyID()))) {
						return UIUtilities.getColorForDisabledCell();
					}
				} else {
					if (!(enableCells(m.getPropertyID()))) {
						return UIUtilities.getColorForDisabledCell();
					}
				}
			} else {
				if (!(enableCells(m.getPropertyID()))) {
					return UIUtilities.getColorForDisabledCell();
				}
			}
			return super.getBackground(element);
		}

		@Override
		public Color getForeground(Object element) {
			return null;
		}


	}

    private boolean enableCells(int propertyID) {
        try {
            return enableCells(
                    propertyID,
                    viewColumnSource.getSortKeyTitleLogicalRecordQueryBean() != null);
        } catch (DAOException e) {
            UIUtilities.handleWEExceptions(e,
                "Error occurred while retrieving LR field information.",UIUtilities.titleStringDbException);
            return false;
        }
    }

	private boolean enableCells(int propertyID, boolean fieldChosen) {
        switch (propertyID) {
        case EFFECTIVEDATETYPE:
        case EFFECTIVEDATEVALUE:
        case TITLELENGTH:
                if (fieldChosen) {
                    return true;
                } else {
                    return false;
                }
        default:
            return true;
        }

    }	
	private class EditingSupportImpl extends EditingSupport {

		private GridTableViewer viewer;
		private CellEditor editor;

		private CCombo editorCombo;
        private CCombo effectiveDateType;
        private TableCombo titleField;        
		private Text editorText;

		public EditingSupportImpl(final GridTableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected boolean canEdit(Object element) {
			ViewEditorInput viewEditorInput = (ViewEditorInput) vEditor.getEditorInput();
			if (viewEditorInput.getEditRights() == EditRights.Read) {
				return false;
			}
            SortKeyTitleRow m = (SortKeyTitleRow) element;
            if (m.getRowEditorType() == RowEditorType.NONE) {
                return false;
            }
            if ((m.getPropertyID() == EFFECTIVEDATETYPE ||
                 m.getPropertyID() == EFFECTIVEDATEVALUE || 
                 m.getPropertyID() == TITLELENGTH) &&
               (viewColumnSource.getSortKeyTitleLRField() == null || 
                viewColumnSource.getSortKeyTitleLRField().equals(0)) ) {
                return false;
            }
            if (m.getPropertyID() == EFFECTIVEDATEVALUE) {
                Code effectiveDateTypeCode = null;
                if (effectiveDateType != null) {
                    effectiveDateTypeCode = (Code) effectiveDateType.getData(
                        new Integer(effectiveDateType.getSelectionIndex()).toString());
                }
                if (effectiveDateTypeCode == null) {
                    effectiveDateTypeCode = viewColumnSource.getEffectiveDateTypeCode();
                }
                if (effectiveDateTypeCode != null) {
                    if (effectiveDateTypeCode.getGeneralId() == Codes.RELPERIOD_RUNDATE || 
                        !(enableCells(m.getPropertyID()))) {
                        return false;
                    }
                } else {
                    if (!(enableCells(m.getPropertyID()))) {
                        return false;
                    }
                }
            } else {
                if (titleField == null) {
                    if (!(enableCells(m.getPropertyID()))) {
                        return false;
                    }                    
                }
                else {
                    int index = titleField.getSelectionIndex();
                    if (!(enableCells(m.getPropertyID(), index != -1))) {
                        return false;
                    }
                }
            }
            return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			setCurrentCellEditor(null);
			SortKeyTitleRow m = (SortKeyTitleRow) element;
			if (m.getPropertyID() == EFFECTIVEDATEVALUE) {
				// changing editors based on Effective Date Type
				Code effectiveDateType = viewColumnSource
						.getEffectiveDateTypeCode();
				if (effectiveDateType != null) {
					if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_RUNDATE) {
						editor = null;
					} else if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_SOURCE_FILE_FIELD) {
						editor = new TableComboViewerCellEditor(viewer
								.getGrid());
						editorTableComboViewer = ((TableComboViewerCellEditor) editor)
								.getViewer();
						editorTableCombo = editorTableComboViewer
								.getTableCombo();
						try {
							populateValueCombo(editorTableComboViewer, true);
						} catch (DAOException e) {
							UIUtilities.handleWEExceptions(e,
							    "Unexpected database error occurred while retrieving Effective Date Value.",UIUtilities.titleStringDbException);
						} catch (SAFRException e) {
							UIUtilities.handleWEExceptions(e,
							    "Unexpected error occurred while retrieving Effective Date Value.",null);
						}
						m.setRowEditorType(RowEditorType.COMBO);
						editorTableComboViewer.setSorter(new AssociatedLRFieldSorter(1,SWT.UP));
					} else if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_CONSTANT) {
						editor = new SAFRTextCellEditor(viewer.getGrid());
						editorText = ((Text) editor.getControl());
						editorText.addVerifyListener(new VerifyNumericListener(WidgetType.INTEGER, false));
						editorText.setTextLimit(MAX_EFFECTIVE_DATE_VALUE);
						m.setRowEditorType(RowEditorType.TEXT);
					}

					return editor;
				}
			}
			if (m.getRowEditorType() == RowEditorType.COMBO) {
				editor = new SAFRComboBoxCellEditor(viewer.getGrid(),
						new String[] {}, SWT.READ_ONLY);
                ((ComboBoxCellEditor)editor).setActivationStyle(
                        ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | 
                        ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
                        ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
                        ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION);                               				
				editorCombo = (CCombo) editor.getControl();
				editor1 = new SKTTableComboViewerCellEditor(viewer.getGrid());
				editorTableComboViewer = ((SKTTableComboViewerCellEditor) editor1)
						.getViewer();
				editorTableCombo = editorTableComboViewer.getTableCombo();
				switch (m.getPropertyID()) {
				case VIEWSOURCES:
					try {
						loadViewSources(editorTableComboViewer);
					} catch (DAOException e) {
						UIUtilities.handleWEExceptions(e,
						    "Unexpected database error occurred while retrieving Logical Record to Logical File associations.",
						    UIUtilities.titleStringDbException);
					} catch (SAFRException e) {
						UIUtilities.handleWEExceptions(e,
						    "Unexpected error occurred while retrieving Logical Record to Logical File associations.",null);
					}
					return editor1;
				case TITLEFIELD:
					try {
						loadTitleFields(editorTableComboViewer,
								currentViewSource);
					} catch (DAOException e) {
						UIUtilities.handleWEExceptions(e,
						    "Unexpected database error occurred while retrieving Title Field.",UIUtilities.titleStringDbException);
					} catch (SAFRException e) {
						UIUtilities.handleWEExceptions(e,
						    "Unexpected error occurred while retrieving Title Field.",null);
					}
					titleField = editorTableCombo;
					return editor1;
				case EFFECTIVEDATETYPE:
					UIUtilities.populateComboBox(editorCombo,
							CodeCategories.RELPERIOD, 0, false);
					effectiveDateType = editorCombo;
					break;
				default:
					editor = null;
					break;
				}
				setCurrentCellEditor(editor);
			} else if (m.getRowEditorType() == RowEditorType.TEXT
					|| m.getRowEditorType() == RowEditorType.NUMBER) {
				editor = new SAFRTextCellEditor(viewer.getGrid());
				if (m.getRowEditorType() == RowEditorType.NUMBER) {
					if (m.getPropertyID() == TITLELENGTH) {
						((Text) editor.getControl())
								.addVerifyListener(new VerifyNumericListener(
										WidgetType.INTEGER, false));
						((Text) editor.getControl())
								.setTextLimit(MAX_TITLE_LENGTH);
					}
				}
			} else {
				editor = null;
			}
			return editor;
		}

		private void loadViewSources(TableComboViewer comboViewer)
				throws DAOException, SAFRException {
			TableCombo combo = comboViewer.getTableCombo();
			combo.setShowTableHeader(false);
			combo.defineColumns(1);
			combo.setDisplayColumnIndex(0);

			if (viewSources != null) {
				Integer counter = 0;
				ArrayList<ViewSource> viewSrcList = new ArrayList<ViewSource>();
				for (ViewSource source : viewSources) {
					viewSrcList.add(source);
					// viewSrcList.add(getAssocString(source.getLrFileAssociation()));
					combo.setData(Integer.toString(counter++), source);
				}
				comboViewer.setContentProvider(new ArrayContentProvider());
				comboViewer
						.setLabelProvider(new ViewSourceTableLabelProvider());
				comboViewer.setInput(viewSrcList);
				comboViewer.refresh();

			}
		}

		@SuppressWarnings("rawtypes")
        private void loadTitleFields(TableComboViewer comboViewer,
				ViewSource viewSource) throws DAOException, SAFRException {
			TableCombo combo = comboViewer.getTableCombo();
			combo.setShowTableHeader(false);
			combo.defineColumns(1);
			combo.setDisplayColumnIndex(0);
			// load the combo box with the fields in the Lookup Path
			if (viewSource == null) {
				return;
			}
			Integer counter = 0;
            ArrayList<List> titleFieldList = new ArrayList<List>();
			
            // Allow user to select a blank value
            List<Object> bean = new ArrayList<Object>();
            bean.add(null);
            bean.add(null);
            bean.add(null);
            titleFieldList.add(bean);
            combo.setData(Integer.toString(counter++), null);
			
			List<LogicalRecordQueryBean> logicalRecordQueryBeans = viewSource.getLookupLogicalRecords();
			for (LogicalRecordQueryBean lrQueryBean : logicalRecordQueryBeans) {
				List<LookupQueryBean> lookupQueryBeans = viewSource.getLookupPaths(lrQueryBean.getId());
				for (LookupQueryBean lkupQueryBean : lookupQueryBeans) {
					List<LRField> lookupFields = viewSource.getLookupFields(lkupQueryBean.getId());
					for (LRField lrField : lookupFields) {
						List<Object> list = new ArrayList<Object>();
						list.add(lkupQueryBean);
						list.add(lrQueryBean);
						list.add(lrField);
						titleFieldList.add(list);
						combo.setData(Integer.toString(counter++), list);
					}
				}
			}

			comboViewer.setContentProvider(new ArrayContentProvider());
			comboViewer.setLabelProvider(new TitleFieldTableLabelProvider());
			comboViewer.setInput(titleFieldList);
			comboViewer.refresh();
		}

		@Override
		protected Object getValue(Object element) {
			SortKeyTitleRow m = (SortKeyTitleRow) element;
			Object returnVal = m.getValue(0);
			if (m.getRowEditorType() == RowEditorType.COMBO) {

				if ((m.getPropertyID() == VIEWSOURCES)
						|| (m.getPropertyID() == EFFECTIVEDATEVALUE)
						|| (m.getPropertyID() == TITLEFIELD)) {
					if (returnVal == null) {
						return "";
					} else {
						return returnVal.toString();
					}
				} else {
					if (returnVal == null) {
						return -1;
					} else if (returnVal instanceof Code) {
						return editorCombo.indexOf(((Code) returnVal)
								.getDescription());
					} else {
						return editorTableCombo.indexOf(returnVal.toString());
					}
				}

			}
			if (returnVal == null) {
				return "";
			}
			return returnVal;
		}

		@SuppressWarnings("unchecked")
        @Override
		protected void setValue(Object element, Object value) {
			SortKeyTitleRow row = (SortKeyTitleRow) element;
			Boolean dirtyFlag = false;
			if (row.getRowEditorType() == RowEditorType.COMBO) {
				if (value instanceof Integer && (Integer) value < 0) {
					value = null;
				}
				if (value != null) {
					try {
						switch (row.getPropertyID()) {
						case VIEWSOURCES:
							ViewSource previousSource = viewColumnSource.getViewSource();
							
                            ViewSource currentSource = null;
                            if (editorTableCombo.getTable().getSelectionCount() > 0) {
                                currentSource = (ViewSource) editorTableCombo
                                .getTable().getSelection()[0].getData();
                            }

							if (!UIUtilities.isEqual(previousSource,currentSource)) {
								row.setValue(VIEWSOURCES, currentSource);
								dirtyFlag = true;
							}
							break;
						case TITLEFIELD:
							LRField previousTitleField = viewColumnSource.getSortKeyTitleLRField();
                            List<Object> currentList = null;
                            if (editorTableCombo.getTable().getSelectionCount() > 0) {                                
                                currentList = (List<Object>) editorTableCombo
                                .getTable().getSelection()[0].getData();
                            }
							if (currentList != null) {
							    if (currentList.get(0) == null) {
							        if (previousTitleField != null) {
							            row.setValue(TITLEFIELD, currentList);
							            sortKey.setTitleLength(0);
                                        dirtyFlag = true;
							        }
							    } else {							    
        							LRField currentTitleField = (LRField) currentList.get(2);    
                                    if (!UIUtilities.isEqualSAFRComponent(previousTitleField, currentTitleField)) {
                                        row.setValue(TITLEFIELD, currentList);
                                        LRField field = viewColumnSource.getSortKeyTitleLRField();
                                        if (field != null) {
                                            // change the Sort Key Title Length based on
                                            // the Sort Key Title LR Field
                                            sortKey.setTitleLength(field.getLength().intValue());
                                        } else {
                                            sortKey.setTitleLength(0);
                                        }
                                        dirtyFlag = true;
                                    }
    							}							
							}
							break;

						case EFFECTIVEDATETYPE:
							Code previousDateType = viewColumnSource
									.getEffectiveDateTypeCode();
							Code currentDateType = (Code) editorCombo
									.getData(value.toString());
							if (!UIUtilities.isEqual(previousDateType,
									currentDateType)) {
								row.setValue(EFFECTIVEDATETYPE, (Code) editorCombo.getData(value.toString()));

								// reset effective date value if user changes
								// the date type
								viewColumnSource.setEffectiveDateValue(null);

								if (viewColumnSource.getEffectiveDateTypeCode()
										.getGeneralId() == Codes.RELPERIOD_RUNDATE) {
									viewColumnSource
											.setEffectiveDateValue("Runday()");
								}

								dirtyFlag = true;
							}
							break;
						case EFFECTIVEDATEVALUE:
							// if Effective Date is of type Source File Field
							LRField previousEffectiveDateField = viewColumnSource
									.getEffectiveDateLRField();
							if (value instanceof LRField) {
								LRField currentEffectiveDateField = (LRField) value;

								if (!UIUtilities.isEqual(
										previousEffectiveDateField,
										currentEffectiveDateField)) {
									row.setValue(EFFECTIVEDATEVALUE, currentEffectiveDateField);

									dirtyFlag = true;
								}
							}

							break;
						default:
							break;
						}
					} catch (SAFRException e) {
						UIUtilities.handleWEExceptions(e,
								"Error in setting values", null);
					}
				} else {
					// nothing selected in combo
					row.setValue(0, null);
				}
			} else if (row.getPropertyID() == EFFECTIVEDATEVALUE) {
				Code effectiveDateType = viewColumnSource
						.getEffectiveDateTypeCode();
				if (effectiveDateType != null) {
					if (effectiveDateType.getGeneralId() == Codes.RELPERIOD_CONSTANT) {
						String previousDateValue = viewColumnSource
								.getEffectiveDateValue();
						String currentDateValue = (String) value;

						if (!UIUtilities.isEqual(previousDateValue,
								currentDateValue)) {
							row.setValue(0, value);
							dirtyFlag = true;
						}
					}
				}
			} else if (row.getPropertyID() == TITLELENGTH) {
				Integer previousTitleLength = sortKey.getTitleLength();
				Integer currentTitleLength = UIUtilities.stringToInteger(value.toString());

				if (!UIUtilities.isEqual(previousTitleLength, currentTitleLength)) {
					row.setValue(TITLELENGTH, value);
					dirtyFlag = true;
				}
			} else {
				// set the value typed by user
				row.setValue(TITLELENGTH, value);
			}
			getViewer().refresh();
			getViewer().update(element, null);

			if (dirtyFlag) {
				vEditor.setModified(true);
			}
		}
	}

	public void enableDisableDeleteButton() {
		SourceProvider service = UIUtilities.getSourceProvider();
		service.setAllowDeleteSortKeyTitleView(true);
		if (viewColumnSource != null) {
			try {
				if (viewColumnSource.getSortKeyTitleLogicalRecordQueryBean() != null) {
					service.setSortKeyTitleExist(true);
				} else {
					service.setSortKeyTitleExist(false);
				}
			} catch (DAOException e) {
				UIUtilities.handleWEExceptions(e);
			}
		}
	}

	public void populateValueCombo(TableComboViewer comboViewer,
			boolean allowBlank) throws DAOException, SAFRException {
		TableCombo combo = comboViewer.getTableCombo();
		ComponentAssociation association = viewColumnSource.getViewSource()
				.getLrFileAssociation();
		if (association != null) {
			Integer counter = 0;
			combo.getTable().removeAll();

			Integer logicalRecordId = association.getAssociatingComponentId();
			List<LRField> lrFieldList = SAFRApplication.getSAFRFactory()
					.getLRFields(logicalRecordId);
			List<Object> fieldList = new ArrayList<Object>();
			if (allowBlank) {
				// Allow user to select a blank value
				Object bean = new Object();
				fieldList.add(bean);
				combo.setData(Integer.toString(counter++), null);
				combo.setData(Integer.toString(counter++), null);
			}

			for (LRField field : lrFieldList) {
				// Effective Date Value combo contains only those fields whose
				// Date Time Format code is not null
				if (field.getDateTimeFormatCode() != null) {
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
						if (element instanceof LRField) {
							LRField lField = (LRField) element;
							return UIUtilities.getComboString(lField.getName(),
									lField.getId());
						}
						break;
					default:
						return super.getColumnText(element, columnIndex);
					}
					return UIUtilities.BLANK_VALUE;
				}
			});

			for (int iCounter = 0; iCounter < 2; iCounter++) {
				ColumnSelectionListenerForTableCombo colListener = new ColumnSelectionListenerForTableCombo(iCounter, comboViewer, "AssociatedLRField");
				combo.getTable().getColumn(iCounter).addSelectionListener(colListener);
			}
			comboViewer.setInput(fieldList);
			comboViewer.refresh();
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
		// TODO Auto-generated method stub

	}

	public void partClosed(IWorkbenchPartReference partRef) {
		if (!(partRef.getPart(false) instanceof IEditorPart)) {
			return;
		}

		if (partRef.getPart(false) instanceof ViewEditor) {
			// if a view editor is closed then hide the table.
			showGrid(false, true);

			// releasing resources when view editor is closed
			vEditor = null;
			sortKey = null;
			currentViewSource = null;
			viewColumnSource = null;
			viewSources = null;
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
			vEditor = (ViewEditor) editor;
			if (vEditor.isCurrentSortKey()) {
				if (checkPrevious
						&& UIUtilities.isEqual(this.sortKey, vEditor
								.getCurrentSortKey())) {
					sameSortKeyFlag = true;
					// check if the user has clicked on the same sort key or on
					// another one
				} else {
					sameSortKeyFlag = false;
				}
				previousSortKey = this.sortKey;
				this.sortKey = vEditor.getCurrentSortKey();
				this.viewSources = sortKey.getView().getViewSources()
						.getActiveItems();
				View view = this.sortKey.getView();
				Code asDataCode = SAFRApplication.getSAFRFactory().getCodeSet(
						CodeCategories.SORTDSP).getCode(Codes.ASDATA);
				if (view.getOutputFormat().equals(OutputFormat.Format_Report) && 
				    (!asDataCode.equals(this.sortKey.getDisplayModeCode())) && 
				    (!view.getViewSources().getActiveItems().isEmpty())) {
					// display grid only if below conditions are true
					// 1. the output format is either drilldown file or hardcopy
					// report
					// 2. if display mode code of the sort key is not ASDATA
					// 3. if there is at least one view source available
					showGrid(true, checkPrevious);
				} else {
					showGrid(false, checkPrevious);
				}

			} else {
				showGrid(false, checkPrevious);
			}
		} else {
			showGrid(false, checkPrevious);
		}
	}

	private void showGrid(boolean show, boolean checkPrevious) {
		// CQ 8551. Nikita. 31/08/2010.
		// Added extra parameter 'checkPrevious' to forcefully refresh the grid
		// without checking if user has clicked on another sort key if output
		// format of the View is changed
		label.setVisible(!show);
		sortKeyTitleTableViewer.getGrid().setVisible(show);
		/* CQ 7489. Nikita. 02/02/2010. */
		// Refresh the grid only if the Sort Key has changed.
		if (show
				|| (checkPrevious && UIUtilities.isEqual(previousSortKey,
						sortKey))) {
			sortKeyTitleTableViewer.refresh();
		} else {
			enableDisableDeleteButton();
		}

	}

	public void refreshView() {
		sortKeyTitleTableViewer.refresh();
	}

	private static String getLrLookupFieldString(LogicalRecordQueryBean lr,
			LookupQueryBean lkup, LRField field) {
		if (lr != null && lkup != null && field != null) {
			String sLR = UIUtilities.getComboString(lr.getName(), lr.getId());
			String sLKP = UIUtilities.getComboString(lkup.getName(), lkup
					.getId());
			String sField = UIUtilities.getComboString(field.getName(), field
					.getId());
			return sLR + "." + sLKP + "." + sField;
		}
		return null;
	}

	private static String getAssocString(ComponentAssociation assoc) {
		if (assoc != null) {
			String str1 = UIUtilities.getComboString(assoc
					.getAssociatingComponentName(), assoc
					.getAssociatingComponentId());
			String str2 = UIUtilities.getComboString(assoc
					.getAssociatedComponentName(), assoc
					.getAssociatedComponentIdNum());
			return str1 + "." + str2;
		}
		return null;
	}

	public void deleteSortKeyTitleField() {
		if (sortKey != null) {
			sortKey.removeSortKeyTitleField();
			sortKeyTitleTableViewer.refresh();
			vEditor.setModified(true);
		}
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
			// TODO Auto-generated method stub
			return null;
		}

		public Color getForeground(Object element, int columnIndex) {
			return null;
		}

	}

	private static class ViewSourceTableLabelProvider implements
			ITableLabelProvider, ITableColorProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof ViewSource))
				return null;
			ViewSource source = (ViewSource) element;
			switch (columnIndex) {
			case 0:
				String sourceString;
				try {
					sourceString = getAssocString(source.getLrFileAssociation());
					return sourceString;
				} catch (DAOException e) {
					UIUtilities.handleWEExceptions(e);
				} catch (SAFRException e) {
					UIUtilities.handleWEExceptions(e);
				}

			}
			return "";
		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		public void dispose() {
			// TODO Auto-generated method stub

		}

		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		public Color getBackground(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public Color getForeground(Object element, int columnIndex) {
			try {
				ViewSource source = (ViewSource) element;
				ComponentAssociation association = source
						.getLrFileAssociation();
				Integer logicalRecordId = association
						.getAssociatingComponentId();

				if ((!SAFRApplication.getUserSession().isSystemAdministrator())) {
					EditRights rights = SAFRApplication.getUserSession()
							.getEditRights(ComponentType.LogicalRecord,
									logicalRecordId);
					if (rights == EditRights.None) {
						return PlatformUI.getWorkbench().getDisplay()
								.getSystemColor(SWT.COLOR_DARK_GRAY);

					}
				} else {
					return null;
				}
			} catch (SAFRException e) {
				UIUtilities.handleWEExceptions(e);
			}
			return null;
		}

	}


	private static class TitleFieldTableLabelProvider extends LabelProvider
			implements ITableLabelProvider, ITableColorProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@SuppressWarnings("unchecked")
        public String getColumnText(Object element, int columnIndex) {

			List<Object> list = (List<Object>) element;
			switch (columnIndex) {
			case 0:
			    if (list.isEmpty()) {
		            return "";			        
			    } else {
    				return getLrLookupFieldString((LogicalRecordQueryBean) list
    						.get(1), (LookupQueryBean) list.get(0), (LRField) list
    						.get(2));
			    }
			}
			return "";
		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		public void dispose() {
			// TODO Auto-generated method stub

		}

		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		public Color getBackground(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@SuppressWarnings("unchecked")
        public Color getForeground(Object element, int columnIndex) {

			List<Object> list = (List<Object>) element;
			if ((!SAFRApplication.getUserSession().isSystemAdministrator())) {

				if (((LookupQueryBean) list.get(0)).getRights() == EditRights.None
						|| ((LogicalRecordQueryBean) list.get(1)).getRights() == EditRights.None) {
					return PlatformUI.getWorkbench().getDisplay()
							.getSystemColor(SWT.COLOR_DARK_GRAY);
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	public class SKTTableComboViewerCellEditor extends
			AbstractTableComboViewerCellEditor {
		public SKTTableComboViewerCellEditor(Composite parent) {
			super(parent);
		}

		protected void doCreateWidget(Composite parent) {
			SAFRGUIToolkit safrToolkit = new SAFRGUIToolkit();
			this.viewer = safrToolkit.createTableCombo(parent);
		}
	}

	public void filterPart(boolean state) {
		sortKeyTitleTableViewer.cancelEditing();
	}

	public void setCurrentCellEditor(CellEditor currentCellEditor) {
		this.currentCellEditor = currentCellEditor;
	}
}
