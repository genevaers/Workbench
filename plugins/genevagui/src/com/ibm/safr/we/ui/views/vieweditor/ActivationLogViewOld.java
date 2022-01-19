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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.ViewPart;

import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.view.ViewActivationError;
import com.ibm.safr.we.ui.editors.batchview.ActivateViewsEditor;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class ActivationLogViewOld extends ViewPart {

	public static String ID = "SAFRWE.ActivationLogViewOld";
	private Composite composite;
	private Label label;
	private SAFRViewActivationException viewActivationLog;
	Map<SAFRCompilerErrorType, List<ViewActivationError>> errorMap = new HashMap<SAFRCompilerErrorType, List<ViewActivationError>>();;

	public String[] columnLabels = { "Message", "Column", "Source" };
	double[] columnWidths = { 0.55, 0.15, 0.3 };
	private TreeViewer tableTreeViewer;
	private boolean resizeDone = false;
	private boolean viewEditor = false;

    @Override
	public void createPartControl(Composite parent) {

		// listen to workbench part lifecycle activities. This is necessary so
		// that this view can react when the View editor closed or when other
		// editor gets focus.
		composite = new Composite(parent, SWT.NONE);

		composite.setLayout(new FormLayout());
		composite.setLayoutData(new FormData());

		label = new Label(composite, SWT.NONE);
		label.setText("Activation Log unavailable");
		label.setVisible(false);

		FormData labelData = new FormData();
		labelData.top = new FormAttachment(0, 0);
		labelData.bottom = new FormAttachment(100, 0);
		labelData.left = new FormAttachment(0, 0);
		labelData.right = new FormAttachment(100, 0);
		label.setLayoutData(labelData);

		tableTreeViewer = new TreeViewer(composite, SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tableTreeViewer.getTree().setLinesVisible(true);
		tableTreeViewer.getTree().setHeaderVisible(true);

		tableTreeViewer.getTree().setLayoutData(
				new GridData(GridData.FILL_BOTH));

		FormData tableData = new FormData();
		tableData.top = new FormAttachment(0, 0);
		tableData.bottom = new FormAttachment(100, 0);
		tableData.left = new FormAttachment(0, 0);
		tableData.right = new FormAttachment(100, 0);
		tableTreeViewer.getTree().setLayoutData(tableData);

		int iCounter = 0;

		int length = columnWidths.length;
		for (iCounter = 0; iCounter < length; iCounter++) {
			TreeColumn column = new TreeColumn(tableTreeViewer.getTree(),
					SWT.NONE);
			column.setText(columnLabels[iCounter]);
			column.setResizable(true);
		}
		
        Tree tree = tableTreeViewer.getTree();
        tree.addListener(SWT.Resize, new Listener() {
              public void handleEvent(Event event) {
                  if (!resizeDone) {
                      int wid = tableTreeViewer.getControl().getBounds().width;
                      tableTreeViewer.getTree().getColumn(0).setWidth((int) (wid*columnWidths[0]));
                      tableTreeViewer.getTree().getColumn(1).setWidth((int) (wid*columnWidths[1]));
                      tableTreeViewer.getTree().getColumn(2).setWidth((int) (wid*columnWidths[2]));
                      resizeDone = true;
                  }
              }
        });
		
		tableTreeViewer.setContentProvider(new ITreeContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}

			public void dispose() {

			}

			public Object[] getElements(Object inputElement) {
				if (!errorMap.isEmpty()) {
					return errorMap.keySet().toArray();
				} else {
					return new String[] {};
				}
			}

			public boolean hasChildren(Object element) {
				if (element instanceof SAFRCompilerErrorType) {
					return errorMap
							.containsKey((SAFRCompilerErrorType) element);
				} else {
					return false;
				}
			}

			public Object getParent(Object element) {
				if (element instanceof ViewActivationError) {
					return ((ViewActivationError) element).getErrorType();
				} else {
					return null;
				}
			}

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof SAFRCompilerErrorType) {
					if (errorMap
							.containsKey((SAFRCompilerErrorType) parentElement)) {
						return errorMap.get(
								(SAFRCompilerErrorType) parentElement)
								.toArray();
					} else {
						return null;
					}
				} else {
					return null;
				}
			}
		});
		tableTreeViewer
				.setLabelProvider(new ViewActivationLogTableLabelProvider());

		tableTreeViewer.setInput(errorMap);
		showGridForCurrentEditor(getSite().getPage().getActiveEditor());
	}

	@Override
	public void setFocus() {
		tableTreeViewer.getTree().setFocus();
	}

	public void showGridForCurrentEditor(IEditorPart editor) {
		// show the table only if the current editor is View editor.
		if ((editor instanceof ViewEditor) || 
		    (editor instanceof LogicTextEditor) || 
		    (editor instanceof ActivateViewsEditor)) {
			ViewEditor vEditor = null;
            LogicTextEditor logicTextEditor = null;
			ActivateViewsEditor activateViewsEditor = null;
			errorMap.clear();
			viewActivationLog = null;
			if (editor instanceof ViewEditor) {
				label.setText("Activation Log unavailable");
				vEditor = (ViewEditor) editor;
				if (vEditor.viewActivationMessageExistsOld()) {
					this.viewActivationLog = vEditor
							.getViewActivationException();
				}
			} else if (editor instanceof LogicTextEditor) {
                logicTextEditor = (LogicTextEditor) editor;           
                label.setText("Logic Text Validation Log unavailable");
                if (logicTextEditor.isLogicTextValidationMessageExistsOld()) {
                    this.viewActivationLog = logicTextEditor
                            .getLogicTextValidationErrors();
                }
            } else if (editor instanceof ActivateViewsEditor) {             
				label.setText("Activation Log unavailable");
				activateViewsEditor = (ActivateViewsEditor) editor;
				this.viewActivationLog = activateViewsEditor
						.getViewActivationException();
			}

			if (viewActivationLog != null) {
				for (ViewActivationError error : viewActivationLog.getActivationLogOld()) {
					if (errorMap.containsKey(error.getErrorType())) {
						errorMap.get(error.getErrorType()).add(error);
					} else {
						List<ViewActivationError> errors = new ArrayList<ViewActivationError>();
						errors.add(error);
						errorMap.put(error.getErrorType(), errors);
					}
				}
			}
			if (!errorMap.isEmpty()) {
				// there are errors to show.
				showGrid(true);
			} else {
				showGrid(false);
			}
		} else {
			showGrid(false);
		}
	}

	private void showGrid(boolean show) {
		label.setVisible(!show);
		tableTreeViewer.getTree().setVisible(show);
		if (show) {
			tableTreeViewer.refresh();
		}
	}

	public Object[] getExpands() {
		return tableTreeViewer.getExpandedElements();
	}

	public void setExpands(Object [] expands) {
		if (expands != null) {
			tableTreeViewer.setExpandedElements(expands);
		}
	}
	
    public boolean isViewEditor() {
        return viewEditor;
    }

    public void setViewEditor(boolean viewEditor) {
        this.viewEditor = viewEditor;
    }

	
	/**
	 * This class is used as a Label Provider for the ViewAvtivationErrors.
	 * Contains method to provide the text for each column of the table in the
	 * ViewActivationLog View.
	 * 
	 */
	public class ViewActivationLogTableLabelProvider implements
			ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof SAFRCompilerErrorType) {
				if (columnIndex == 0) {
					return ((SAFRCompilerErrorType) element).getText();
				}
			} else {
				ViewActivationError sae = (ViewActivationError) element;
				switch (columnIndex) {
				case 0:
					return sae.getErrorText();
				case 1:
					if (sae.getViewColumn() != null) {
						String heading = "";
						if ((sae.getViewColumn().getHeading1() != null)
								&& (!sae.getViewColumn().getHeading1().equals(
										""))) {
							heading = sae.getViewColumn().getHeading1();
						} else if ((sae.getViewColumn().getHeading2() != null)
								&& (!sae.getViewColumn().getHeading2().equals(
										""))) {
							heading = sae.getViewColumn().getHeading2();
						} else if ((sae.getViewColumn().getHeading3() != null)
								&& (!sae.getViewColumn().getHeading3().equals(
										""))) {
							heading = sae.getViewColumn().getHeading3();
						}

						String column = UIUtilities.getComboString(heading, sae
								.getViewColumn().getColumnNo());
						return column;
					}
					break;
				case 2:
					if (sae.getViewSource() != null) {
						String source = "";
						try {
							source = UIUtilities.getComboString(sae
									.getViewSource().getLrFileAssociation()
									.getAssociatingComponentName(), sae
									.getViewSource().getLrFileAssociation()
									.getAssociatingComponentId())
									+ "."
									+ UIUtilities.getComboString(sae
											.getViewSource()
											.getLrFileAssociation()
											.getAssociatedComponentName(), sae
											.getViewSource()
											.getLrFileAssociation()
											.getAssociatedComponentIdNum());
						} catch (DAOException e) {
							UIUtilities.handleWEExceptions(e,"Database error in getting the LR File Association for view source.",UIUtilities.titleStringDbException);
						} catch (SAFRException e) {
							UIUtilities.handleWEExceptions(e,"Error in getting the LR File Association for view source.",null);
						}

						return source;
					}
					break;
				}
			}
			return "";
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

	}

}
