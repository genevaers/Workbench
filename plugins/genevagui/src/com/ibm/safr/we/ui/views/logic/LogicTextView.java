package com.ibm.safr.we.ui.views.logic;

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


import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;

import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.ui.editors.OpenEditorPopupState;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditor;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditorInput;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;

/**
 * A view for displaying logic text tree.
 * 
 */
public class LogicTextView extends ViewPart {

	public static String ID = "SAFRWE.LogicTextView";

	private LogicTextEditor previousEditor = null;

	private TreeViewer treeViewer;
	private Label label;

	@Override
	public void createPartControl(Composite parent) {
		try {
			getSite().getShell().setCursor(
					getSite().getShell().getDisplay().getSystemCursor(
							SWT.CURSOR_WAIT));
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayout(new FormLayout());
			composite.setLayoutData(new FormData());
			Display.getDefault().getActiveShell().setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_WAIT));

			label = new Label(composite, SWT.NONE);
			label.setText("Logic Text Helper is being loaded... Please wait...");
			label.setVisible(false);

			FormData labelData = new FormData();
			labelData.top = new FormAttachment(0, 0);
			labelData.bottom = new FormAttachment(100, 0);
			labelData.left = new FormAttachment(0, 0);
			labelData.right = new FormAttachment(100, 0);
			label.setLayoutData(labelData);

			treeViewer = new TreeViewer(composite);
			treeViewer.setUseHashlookup(false);
			treeViewer
					.setContentProvider(new LogicTextViewTreeContentProvider());
			treeViewer.setLabelProvider(new LogicTextViewTreeLabelProvider());
			ColumnViewerToolTipSupport.enableFor(treeViewer,
					ToolTip.NO_RECREATE);

			treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    checkPopupEnabled();
                }
			    
			});
			treeViewer.addDoubleClickListener(new IDoubleClickListener() {

				public void doubleClick(DoubleClickEvent event) {
					if (getSite().getPage().getActiveEditor() != null) {
						if (getSite().getPage().getActiveEditor() instanceof LogicTextEditor) {

							LogicTextEditor lTEditor = (LogicTextEditor) getSite() .getPage().getActiveEditor();
							LogicTextViewTreeNode logicTextModel = 
							    (LogicTextViewTreeNode) ((TreeSelection) event.getSelection()).getFirstElement();
							if (((logicTextModel.getChildren() == null) || 
							       (logicTextModel.getTitleText().compareTo("WRITE()") == 0)) && 
							     (logicTextModel.getEditorText() != null)) {
								lTEditor.setEditorText(logicTextModel
										.getEditorText(), logicTextModel
										.getOffset());
								lTEditor.setFocus();
							}
						}
					} else {
						LogicTextEditor lTEditor = (LogicTextEditor) previousEditor;
						LogicTextViewTreeNode logicTextModel = (LogicTextViewTreeNode) ((TreeSelection) event
								.getSelection()).getFirstElement();
						if (logicTextModel.getRights() == null) {
							return;
						}
						if ((logicTextModel.getChildren() == null)
								|| (logicTextModel.getTitleText().compareTo(
										"write()") == 0)
								|| (logicTextModel.getEditorText() != null)) {
							lTEditor.setEditorText(logicTextModel
									.getEditorText(), logicTextModel
									.getOffset());
							lTEditor.setFocus();

						}
					}
				}
			});

			FormData treeData = new FormData();
			treeData.top = new FormAttachment(0, 0);
			treeData.bottom = new FormAttachment(100, 0);
			treeData.left = new FormAttachment(0, 0);
			treeData.right = new FormAttachment(100, 0);
			treeViewer.getTree().setLayoutData(treeData);
			
	        // Code for Context menu
	        // First we create a menu Manager
	        MenuManager menuManager = new MenuManager();
	        Menu menu = menuManager.createContextMenu(treeViewer.getTree());
	        // Set the MenuManager
	        treeViewer.getTree().setMenu(menu);
	        getSite().registerContextMenu(menuManager, treeViewer);
	        
	        setPopupEnabled(false);
			
		} finally {
			getSite().getShell().setCursor(null);
		}

		// CQ 8404 Kanchan Rauthan 19/08/2010 To show Logic Text helper view
		// contents when this view is closed and reopened again.
		showContentsForCurrentEditor(getSite().getPage().getActiveEditor());
	}

	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	public void showContentsForCurrentEditor(IEditorPart editor) {
		if (editor instanceof LogicTextEditor) {
			LogicTextEditor ltEditor = (LogicTextEditor) editor;
			ViewEditorInput viewInput = (ViewEditorInput) ((LogicTextEditorInput) (ltEditor
					.getEditorInput())).getViewEditor().getEditorInput();
			if (viewInput.getEditRights() != EditRights.Read) {
				if (previousEditor != null) {
					// store the currently expanded state
					((LogicTextEditorInput) previousEditor.getEditorInput())
							.setExpandedItems(treeViewer.getExpandedElements());
					((LogicTextEditorInput) previousEditor.getEditorInput())
							.setExpandedTreePaths(treeViewer
									.getExpandedTreePaths());
					((LogicTextEditorInput) previousEditor.getEditorInput())
							.setPreviousLogicTextTreeInput(((LogicTextViewTreeContentProvider) treeViewer
									.getContentProvider())
									.getLogicTextItemsRoot());

				}
				LogicTextEditor logicTextEditor = (LogicTextEditor) editor;
				if (previousEditor != null
						&& logicTextEditor.equals(previousEditor)) {
					treeViewer.getTree().setVisible(true);
					label.setVisible(false);
					return;
				} else {
					LogicTextEditorInput logicTextEditorInput = (LogicTextEditorInput) logicTextEditor
							.getEditorInput();
					previousEditor = logicTextEditor;

					((LogicTextViewTreeContentProvider) treeViewer
							.getContentProvider())
							.setLogicTextItemsRoot(logicTextEditorInput
									.getPreviousLogicTextTreeInput());

					// treeViewer.setInput(logicTextType);
					treeViewer.setInput(logicTextEditorInput);

					if (logicTextEditorInput.getExpandedItems() != null) {

						// restore the expanded state if there is one
						treeViewer.setExpandedElements(logicTextEditorInput
								.getExpandedItems());
						treeViewer.setExpandedTreePaths(logicTextEditorInput
								.getExpandedTreePaths());

					}
					treeViewer.refresh();
				}
				treeViewer.getTree().setVisible(true);
				label.setVisible(false);
			}
		} else {
			label.setVisible(true);
			treeViewer.getTree().setVisible(false);

		}
	}

	public void dispose() {
	}

    public void setFocusOn(String fldlu, LogicTextType type) {
        
        if (type == LogicTextType.Extract_Column_Assignment ||
            type == LogicTextType.Extract_Record_Filter ||
            type == LogicTextType.Extract_Record_Output) {
            setFocusOnExtract(fldlu, type);
        }
        else {
            setFocusOnFormat(fldlu, type);            
        }
        checkPopupEnabled();
    }
    
    public LogicTextViewTreeNode getCurrentSelection() {
        return (LogicTextViewTreeNode)((TreeSelection) treeViewer.getSelection()).getFirstElement();
    }

    private void setFocusOnExtract(String fldlu, LogicTextType type) {
        Object expands[] = treeViewer.getExpandedElements();
        
        TreeItem items[] = treeViewer.getTree().getItems();        
        
        TreeItem item = null;
        
        if (fldlu.contains(".")) {
            
            // expand lookups
            treeViewer.expandToLevel(items[2].getData(), 1);                                               
            
            // search lookup fields 
            item = findAssociation(items[2], fldlu, true);       
            if (item != null) {
                treeViewer.setExpandedElements(expands);
                treeViewer.getTree().setSelection(item);
                return;
            }            

            // search write files parameter 
            TreeItem write = items[3];
            treeViewer.expandToLevel(write.getData(), 1);
            
            // expand files
            treeViewer.expandToLevel(write.getItems()[2].getData(), 1);                                                                   
            
            item = findAssociation(write.getItems()[2], fldlu, false);       
            if (item != null) {
                treeViewer.setExpandedElements(expands);
                treeViewer.getTree().setSelection(item);
                return;
            }            
            
        }
        else {
            // expand fields
            treeViewer.expandToLevel(items[1].getData(), TreeViewer.ALL_LEVELS);                               
            
            // search fields 
            item = containsNode(items[1], "{" + fldlu + "}");       
            if (item != null) {
                treeViewer.setExpandedElements(expands);
                treeViewer.getTree().setSelection(item);
                return;
            }
            
            // expand lookups
            treeViewer.expandToLevel(items[2].getData(), 1);                                                               
            
            // search lookups 
            item = containsNode(items[2], "{" + fldlu + "}");       
            if (item != null) {
                treeViewer.setExpandedElements(expands);
                treeViewer.getTree().setSelection(item);
                return;
            }            
            
            // expand write procedures
            TreeItem write = items[3];
            treeViewer.expandToLevel(write.getData(), 1);
            treeViewer.expandToLevel(write.getItems()[0].getData(), TreeViewer.ALL_LEVELS);                               
            
            // search write procedures 
            item = containsNode(write.getItems()[0], "{" + fldlu + "}");       
            if (item != null) {
                treeViewer.setExpandedElements(expands);
                treeViewer.getTree().setSelection(item);
                return;
            }

            // expand write user exit
            treeViewer.expandToLevel(write.getItems()[1].getData(), TreeViewer.ALL_LEVELS);                               
            
            // search write user exit 
            item = containsNode(write.getItems()[1], "{" + fldlu + "}");       
            if (item != null) {
                treeViewer.setExpandedElements(expands);
                treeViewer.getTree().setSelection(item);
                return;
            }                
        }
        treeViewer.setExpandedElements(expands);                
    }

    private void setFocusOnFormat(String fldlu, LogicTextType type) {
        // no curly bracket symbols in format logic
        // so do nothing
    }        
    
    private TreeItem findAssociation(TreeItem node, String name, boolean brackets) {
        String both[] = name.split("\\.");
        
        // first find parent node
        if (brackets) {
            both[0] = "{" + both[0] + "}";
        }
            
        TreeItem parent = containsNode(node, both[0]);
        
        if (parent != null) {
            // expand at this level
            treeViewer.expandToLevel(parent.getData(), TreeViewer.ALL_LEVELS);                                               
            
            // find child of this node
            return containsNode(parent, "." + both[1] + " [");
        }
        return null;
    }
        
    private TreeItem containsNode(TreeItem node, String name) {
        
        // check current item
        String text = node.getText();
        if (text.toLowerCase().contains(name.toLowerCase())) {
            return node;
        }
        
        // check all children
        for (TreeItem child : node.getItems()) {
            TreeItem fnode = this.containsNode(child, name);
            if (fnode != null) {
                return fnode;
            }
        }
        return null;
    }

    private void checkPopupEnabled() {
        LogicTextViewTreeNode node = (LogicTextViewTreeNode)((TreeSelection)treeViewer.getSelection()).getFirstElement();
        if (node != null) {
            switch (node.getId()) {
            case FIELDS_CHILD:
            case FILES_CHILDLF:
            case FILES_CHILDLF_CHILDPF:
            case LOOKUPPATHS_CHILD:
            case LOOKUPSYMBOLS_CHILD:
            case PROCEDURES_CHILD:
            case USEREXITROUTINES_CHILD:
                setPopupEnabled(true);
                break;
            default:
                setPopupEnabled(false);
            };
        }                
    }
    
    private void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.LOGICTEXTVIEW);
        service.setLogicTextView(enabled);
    }
        
}
