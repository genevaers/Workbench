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
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.views.logic.LogicTextViewTreeNode.TreeItemId;

/**
 * A view for displaying logic text tree.
 * 
 */
public class LogicTextView extends ViewPart {

	public static String ID = "SAFRWE.LogicTextView";

	private LogicTextEditor previousEditor = null;

	private TreeViewer treeViewer;
	private Label label;

	private Cursor cursor;

	@Override
	public void createPartControl(Composite parent) {
		try {
			getSite().getShell().setCursor(
					getSite().getShell().getDisplay().getSystemCursor(
							SWT.CURSOR_WAIT));
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayout(new FormLayout());
			composite.setLayoutData(new FormData());
			cursor = new Cursor(Display.getDefault(), SWT.CURSOR_WAIT);
			Display.getDefault().getActiveShell().setCursor(cursor);

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
		if(cursor != null) {
			cursor.dispose();
		}
	}

    public void setFocusOn(String selectedField, LogicTextType type) {
        
        if (type == LogicTextType.Extract_Column_Assignment ||
            type == LogicTextType.Extract_Record_Filter ||
            type == LogicTextType.Extract_Record_Output) {
            setFocusOnExtract(selectedField);
        }
        else {
            setFocusOnFormat(selectedField);            
        }
        checkPopupEnabled();
    }
    
    public LogicTextViewTreeNode getCurrentSelection() {
        return (LogicTextViewTreeNode)((TreeSelection) treeViewer.getSelection()).getFirstElement();
    }

    private void setFocusOnExtract(String selectedField) {
        Object expands[] = treeViewer.getExpandedElements();
        
        TreeItem items[] = treeViewer.getTree().getItems();        
        
        TreeItem item = null;
        
        if (selectedField.contains(".")) {
        	TreeItem base = expandNamedBaseRelativeTo(TreeItemId.LOOKUPPATHS, treeViewer.getTree().getItems());
        	if(expandSectionIfFieldFound(selectedField, base)){
        		return;
        	} else {
            	TreeItem write = expandNamedBaseRelativeTo(TreeItemId.WRITEPARAM, treeViewer.getTree().getItems());
            	TreeItem files = expandNamedBaseRelativeTo(TreeItemId.FILES, write.getItems());        		
        		if(expandSectionIfFieldFound(selectedField, files)){
               		return;
        		}
        	}
        }
        else {
            // expand fields
    		TreeItem base = expandNamedBaseRelativeTo(TreeItemId.FIELDS, treeViewer.getTree().getItems());                               
        	if(expandIfSelectionFoundInBase(selectedField, base)){
        		return;
        	}
    		base = expandNamedBaseRelativeTo(TreeItemId.LOOKUPPATHS, treeViewer.getTree().getItems());                               
    		if(expandIfSelectionFoundInBase(selectedField, base)){
               	return;
        	}
            
            // expand write procedures
        	TreeItem write = expandNamedBaseRelativeTo(TreeItemId.WRITEPARAM, treeViewer.getTree().getItems());
        	TreeItem procedures = expandNamedBaseRelativeTo(TreeItemId.PROCEDURES, write.getItems());
        	
    		if(expandIfSelectionFoundInBase(selectedField, procedures)){
               	return;
        	}
            
            // expand write user exit
        	TreeItem userExits = expandNamedBaseRelativeTo(TreeItemId.USEREXITROUTINES, write.getItems());
    		if(expandIfSelectionFoundInBase(selectedField, userExits)){
               	return;
        	}
            
        }
        treeViewer.setExpandedElements(expands);                
    }

	private boolean expandIfSelectionFoundInBase(String selectedField, TreeItem base) {
		boolean expand = false;
        Object expands[] = treeViewer.getExpandedElements();
		TreeItem item;
		
		item = containsNode(base, "{" + selectedField + "}");       
		if (item != null) {
		    treeViewer.setExpandedElements(expands);
		    treeViewer.getTree().setSelection(item);
		    expand = true;
		}
		return expand;
	}

	private TreeItem expandNamedBaseRelativeTo(TreeItemId id, TreeItem[] items) {
		TreeItem base = getBaseFromItems(items, id);
		treeViewer.expandToLevel(base.getData(), TreeViewer.ALL_LEVELS);
		return base;
	}

	private boolean expandSectionIfFieldFound(String fldlu, TreeItem base) {
		boolean expand = false;
        Object expands[] = treeViewer.getExpandedElements();
		TreeItem item;
		treeViewer.expandToLevel(base.getData(), 1);                                               
		
		item = findAssociation(base, fldlu);       
		if (item != null) {
		    treeViewer.setExpandedElements(expands);
		    treeViewer.getTree().setSelection(item);
		    expand = true;
		}
		return expand;
	}

    private TreeItem getBaseFromItems(TreeItem[] items, TreeItemId id) {
    	TreeItem base = null;
    	for (TreeItem child : items) {
            if(((LogicTextViewTreeNode)child.getData()).getId() == id) {
                return child;
            }
        }
    	return base;
    }

	private void setFocusOnFormat(String fldlu) {
        // no curly bracket symbols in format logic
        // so do nothing
    }        
    
    private TreeItem findAssociation(TreeItem node, String name) {
        String both[] = name.split("\\.");       
        TreeItem parent = containsNode(node, both[0]);
        if (parent != null) {
            treeViewer.expandToLevel(parent.getData(), TreeViewer.ALL_LEVELS);                                                           
            return containsNode(parent, "." + both[1] + " [");
        }
        return null;
    }
        
    private TreeItem containsNode(TreeItem node, String name) {
        
        String text = node.getText();
        if (text.toLowerCase().contains(name.toLowerCase())) {
            return node;
        }
        
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
