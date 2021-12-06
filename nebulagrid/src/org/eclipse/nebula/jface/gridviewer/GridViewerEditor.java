/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    rmcamara@us.ibm.com                       - initial API and implementation
 *    Tom Schindl <tom.schindl@bestsolution.at> - various significant contributions
 *    											  bug fix in: 191216
 *    Jake fisher<fisherja@gmail.com>           - fixed minimum height (bug 263489)
 *******************************************************************************/

package org.eclipse.nebula.jface.gridviewer;

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

import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridEditor;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;

/**
 * FIXME
 */
public class GridViewerEditor extends ColumnViewerEditor {
	/** Editor support for tables. */
    private final GridEditor gridEditor;

    /**
     * The selection follows the editor
     */
    public static final int SELECTION_FOLLOWS_EDITOR = 1 << 30;
    
    private boolean selectionFollowsEditor = false;

    private int gfeature;
        
	GridViewerEditor(ColumnViewer viewer,
			ColumnViewerEditorActivationStrategy editorActivationStrategy,
			int feature) {
		super(viewer, editorActivationStrategy, feature);
		this.selectionFollowsEditor = (feature & SELECTION_FOLLOWS_EDITOR) == SELECTION_FOLLOWS_EDITOR;
		this.gridEditor = new GridEditor((Grid) viewer.getControl());
		this.gfeature = feature;		
	}

	/**
	 * FIXME
	 * {@inheritDoc}
	 */
    @Override
	protected void setEditor(Control w, Item item, int fColumnNumber)
    {
        gridEditor.setEditor(w, (GridItem) item, fColumnNumber);
    }

    /**
     * FIXME
     * {@inheritDoc}
     */
    @Override
	protected void setLayoutData(LayoutData layoutData)
    {
        gridEditor.grabHorizontal = layoutData.grabHorizontal;
        gridEditor.horizontalAlignment = layoutData.horizontalAlignment;
        gridEditor.minimumWidth = layoutData.minimumWidth;
        
		gridEditor.verticalAlignment = layoutData.verticalAlignment;

		if (layoutData.minimumHeight != SWT.DEFAULT) {
			gridEditor.minimumHeight = layoutData.minimumHeight;
		} else {
			gridEditor.minimumHeight = SWT.DEFAULT;
		}
    }

    /**
     * FIXME
     * {@inheritDoc}
     */
	@Override
	public ViewerCell getFocusCell() {
		Grid grid = (Grid)getViewer().getControl();

		if( grid.getCellSelectionEnabled() ) {
			Point p = grid.getFocusCell();

			if( p.x >= 0 && p.y >= 0 ) {
				GridItem item = grid.getItem(p.y);
				if( item != null ) {
					ViewerRow row = getViewerRowFromItem(item);
					return row.getCell(p.x);
				}
			}
		}

		return null;
	}

	private ViewerRow getViewerRowFromItem(GridItem item) {
		if( getViewer() instanceof GridTableViewer ) {
			return ((GridTableViewer)getViewer()).getViewerRowFromItem(item);
		} else {
			return ((GridTreeViewer)getViewer()).getViewerRowFromItem(item);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateFocusCell(ViewerCell focusCell, ColumnViewerEditorActivationEvent event) {
		Grid grid = ((Grid)getViewer().getControl());

		if (event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
				|| event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL) {
			grid.setFocusColumn(grid.getColumn(focusCell.getColumnIndex()));
			grid.setFocusItem((GridItem) focusCell.getItem());
			
			if( selectionFollowsEditor ) {
				grid.setCellSelection(new Point(focusCell.getColumnIndex(),((GridItem)focusCell.getItem()).getRowIndex()));
			}
		}
				
		grid.showColumn(grid.getColumn(focusCell.getColumnIndex()));
		grid.showItem((GridItem) focusCell.getItem()); 
	}

	/**
	 * FIXME
	 * @param viewer
	 * @param editorActivationStrategy
	 * @param feature
	 */
	public static void create(GridTableViewer viewer,
			ColumnViewerEditorActivationStrategy editorActivationStrategy,
			int feature) {
		viewer.setColumnViewerEditor(new GridViewerEditor(viewer,editorActivationStrategy,feature));
	}

	/**
	 * FIXME
	 * @param viewer
	 * @param editorActivationStrategy
	 * @param feature
	 */
	public static void create(GridTreeViewer viewer,
			ColumnViewerEditorActivationStrategy editorActivationStrategy,
			int feature) {
		viewer.setColumnViewerEditor(new GridViewerEditor(viewer,editorActivationStrategy,feature));
	}
	
	public ColumnViewer getGridViewer() {
	    return getViewer();
	}
	
	@Override
    protected void processTraverseEvent(int columnIndex, ViewerRow row,
            TraverseEvent event) {

        ViewerCell cell2edit = null;

        if (event.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
            event.doit = false;

            if ((gfeature & TABBING_VERTICAL) == TABBING_VERTICAL) {
                cell2edit = searchCellAboveBelow(this, row, getViewer(), columnIndex, true);
            } else if ((gfeature & TABBING_HORIZONTAL) == TABBING_HORIZONTAL) {
                cell2edit = searchPreviousCell(this, row, row.getCell(columnIndex), row.getCell(columnIndex), getViewer());
            }
        } else if (event.detail == SWT.TRAVERSE_TAB_NEXT) {
            event.doit = false;

            if ((gfeature & TABBING_VERTICAL) == TABBING_VERTICAL) {
                cell2edit = searchCellAboveBelow(this, row, getViewer(), columnIndex, false);
            } else if ((gfeature & TABBING_HORIZONTAL) == TABBING_HORIZONTAL) {
                cell2edit = searchNextCell(this, row, row.getCell(columnIndex), row.getCell(columnIndex), getViewer());                
            }
        }
        if (cell2edit == null
                && (event.detail == SWT.TRAVERSE_TAB_NEXT || event.detail == SWT.TRAVERSE_TAB_PREVIOUS)) {
            cell2edit = row.getCell(columnIndex);
        }

        if (cell2edit != null) {

            getViewer().getControl().setRedraw(false);
            ColumnViewerEditorActivationEvent acEvent = new ColumnViewerEditorActivationEvent(
                    cell2edit, event);
            triggerEditorActivationEvent(getViewer(), acEvent);                
            getViewer().getControl().setRedraw(true);
        }
    }
    
    private ViewerCell searchNextCell(Object obj, Object... params) {
        Method method;
        Object requiredObj = null;
        Object[] parameters = new Object[4];
        Class<?>[] classArray = {ViewerRow.class, ViewerCell.class, ViewerCell.class, ColumnViewer.class};
        for (int i = 0; i < 4; i++) {
            parameters[i] = params[i];
        }
        try {
            method = ColumnViewerEditor.class.getDeclaredMethod("searchNextCell", classArray);
            method.setAccessible(true);
            requiredObj = method.invoke(obj, params);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid call on ColumnViewerEditor.searchNextCell",e);
        } 

        return (ViewerCell)requiredObj;
    }    

    private ViewerCell searchPreviousCell(Object obj, Object... params) {
        Method method;
        Object requiredObj = null;
        Object[] parameters = new Object[4];
        Class<?>[] classArray = {ViewerRow.class, ViewerCell.class, ViewerCell.class, ColumnViewer.class};
        for (int i = 0; i < 4; i++) {
            parameters[i] = params[i];
        }
        try {
            method = ColumnViewerEditor.class.getDeclaredMethod("searchPreviousCell", classArray);
            method.setAccessible(true);
            requiredObj = method.invoke(obj, params);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid call on ColumnViewerEditor.searchPreviousCell",e);
        }

        return (ViewerCell)requiredObj;
    }    

    public ViewerCell searchCellAboveBelow(Object obj, Object... params) {
        Method method;
        Object requiredObj = null;
        Object[] parameters = new Object[4];
        Class<?>[] classArray = {ViewerRow.class, ColumnViewer.class, int.class, boolean.class };
        for (int i = 0; i < 4; i++) {
            parameters[i] = params[i];
        }
        try {
            method = ColumnViewerEditor.class.getDeclaredMethod("searchCellAboveBelow", classArray);
            method.setAccessible(true);
            requiredObj = method.invoke(obj, params);
            // if no edit-able cell above/below
            if (requiredObj == null) {
                // check neighbour column
                GridTableViewer viewer = (GridTableViewer)params[1];
                int colIdx = (Integer)params[2];
                boolean up = (Boolean)params[3];
                if (up) {
                    if (colIdx > 0) {
                        GridItem item = viewer.getGrid().getItem(viewer.doGetItemCount()-1);
                        params[0] = new GridViewerRow(item);                        
                        params[2] = colIdx-1;
                        requiredObj = method.invoke(obj, params);
                    }
                }
                else {
                    if (colIdx < viewer.doGetColumnCount()-1) {
                        GridItem item = viewer.getGrid().getItem(0);
                        params[0] = new GridViewerRow(item);                        
                        params[2] = colIdx+1;
                        requiredObj = method.invoke(obj, params);
                    }
                }
            }
        }  catch (Exception e) {
            throw new IllegalArgumentException("Invalid call on ColumnViewerEditor.searchCellAboveBelow",e);
        }

        return (ViewerCell)requiredObj;
    }    
    
    private void triggerEditorActivationEvent(Object obj, Object... params) {
        Method method;
        Object[] parameters = new Object[1];
        Class<?>[] classArray = {ColumnViewerEditorActivationEvent.class};
        for (int i = 0; i < 1; i++) {
            parameters[i] = params[i];
        }
        try {
            method = ColumnViewer.class.getDeclaredMethod("triggerEditorActivationEvent", classArray);
            method.setAccessible(true);
            method.invoke(obj, params);
        }  catch (Exception e) {
            throw new IllegalArgumentException("Invalid call on ColumnViewer.triggerEditorActivationEvent",e);
        }

        return;
    }    
    	
	
}
