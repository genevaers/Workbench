package com.ibm.safr.we.ui.views.navigatortree;

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

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.ui.commands.SourceProvider;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.MetadataView;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.utilities.SAFRLogger;

public class NavigatorView extends ViewPart {
    
    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.ui.views.navigatortree.NavigatorView");
    
	public static final String ID = "SAFRWE.Treeview";

	private TreeViewer treeViewer;
	SAFRGUIToolkit safrToolkit;
	private static Boolean flag = false;
	private static ISelection previousSelection;

	private MainTreeItem selectedItem;

	@Override
    public void saveState(IMemento memento) {
    }

    @Override
	public void createPartControl(Composite parent) {

		safrToolkit = new SAFRGUIToolkit();
		treeViewer = safrToolkit.createTreeViewer(parent);
		treeViewer.setContentProvider(new MainTreeContentProvider());
		treeViewer.setLabelProvider(new MainTreeLabelProvider());
		treeViewer.getTree().setData(SAFRLogger.USER, "Navigator Tree");
		treeViewer.setInput(1);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {

				previousSelection = event.getSelection();

				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();

				MainTreeItem selectedItemCur = (MainTreeItem) selection
						.getFirstElement();

				if (selectedItemCur != null) {
			        selectedItem = selectedItemCur;
				}

				// CQ 8572. Nikita. 06/09/2010
				// Commented this condition to fix the following issue: Metadata
				// view's selectionChanged doesn't get called on
				// logging in to another env if Metadata view is open but
				// Navigator view is minimized

				try {

                    MetadataView metadataview = (MetadataView) getSite()
                    .getPage().showView(MetadataView.ID);
                    metadataview.selectionChanged(getSite().getPart(), event
                            .getSelection());                       				    
				} catch (PartInitException pi) {
					UIUtilities.handleWEExceptions(pi,"Unexpected error occurred while opening Metadata List.",null);
				}
				// }

				boolean allowRefreshViewFolder = false;
				if (selectedItemCur != null) {
					if (selectedItemCur.getId().equals(TreeItemId.VIEWFOLDER)) {
						allowRefreshViewFolder = true;
					}

					// Get the source provider service
					ISourceProviderService sourceProviderService = (ISourceProviderService) (getSite()
							.getService(ISourceProviderService.class));
					// Now get the Workbench service
					SourceProvider sourceProvider = (SourceProvider) sourceProviderService
							.getSourceProvider(SourceProvider.REFRESH_VIEWFOLDERLIST);
					sourceProvider
							.setAllowRefreshViewFolder(allowRefreshViewFolder);
				}
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {

				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();

				MainTreeItem selectedItem = (MainTreeItem) selection
						.getFirstElement();

				if (selectedItem.getId().equals(TreeItemId.VIEWFOLDER)) {
					refreshNavigator();
				}

			}

		});
		treeViewer.getTree().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				super.mouseDown(e);
				Point pt = new Point(e.x, e.y);
			    TreeItem item = treeViewer.getTree().getItem(pt);
				if (item == null) {
					treeViewer.getTree().setData(SAFRLogger.USER, "Navigator Tree");					
				}
				else {
					treeViewer.getTree().setData(SAFRLogger.USER, "Navigator Tree (" + item.getText() + ")");										
				}			    
			}
		});
		
		treeViewer.setSorter(new SAFRTreeSorter());
		// Set the selection provider for this workbench site
		getSite().setSelectionProvider(treeViewer);
		// check whether the user has logged in for the first time. otherwise
		// set the tree viewer selection same as the previous selection.
		if (!flag) {
			setDefaultTreeSelection(false);
			flag = true;
		} else {
			treeViewer.setExpandedState(new MainTreeItem(TreeItemId.VIEWFOLDER,
					null, null, null), true);
			if (previousSelection != null) {
				treeViewer.setSelection(previousSelection);
			}
		}
		setFocus();
	}

	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	/**
	 * Method to refresh the SAFR Explorer view. Sets default selection in the
	 * SAFR explorer tree.Tree should be refreshed if this method is used at
	 * Login to another environment.
	 * 
	 * @param refresh
	 *            true if the tree is to be refreshed.
	 */
	public void setDefaultTreeSelection(boolean refresh) {
		if (refresh) {
			setViewFolderChildrenNull();
			treeViewer.refresh();
		}
		/*
		 * Selection of default view folder is done in following steps: 1. If
		 * the user has default view folder assigned and that view folder is
		 * present in the current environment then that view folder is selected.
		 * 2. If default view view folder is not selected in the first step then
		 * first view folder under the 'view folders' node is selected. 3. If
		 * there are no view folders in the current environment then node 'view
		 * folders' is selected.
		 */

		// set the default view folder selection for SAFR explorer tree.
		if (treeViewer.isExpandable(new MainTreeItem(TreeItemId.VIEWFOLDER,
				null, null, null))) {
			treeViewer.setExpandedState(new MainTreeItem(TreeItemId.VIEWFOLDER,
					null, null, null), true);
			ViewFolder defaultViewFolder = null;

			try {
				defaultViewFolder = SAFRApplication.getUserSession().getUser()
						.getDefaultViewFolder();
			} catch (SAFRException e) {
				UIUtilities.handleWEExceptions(e,"Unexpected error occurred while setting default tree selection.",null);
			}
			// check if there is default folder and default folder is exists
			// in
			// the current environment.
			if ((defaultViewFolder != null)
					&& (treeViewer.testFindItem(new MainTreeItem(
							defaultViewFolder.getId(),
							TreeItemId.VIEWFOLDERCHILD, null, null, null)) != null)) {
				treeViewer.setSelection(new StructuredSelection(
						new MainTreeItem(defaultViewFolder.getId(),
								TreeItemId.VIEWFOLDERCHILD, null, null, null)),
						true);
			} else {
				TreeItem ti = (TreeItem) treeViewer
						.testFindItem(new MainTreeItem(TreeItemId.VIEWFOLDER,
								null, null, null));

				MainTreeItem mti = (MainTreeItem) ti.getItem(0).getData();
				treeViewer.setSelection(new StructuredSelection(mti), true);
			}

		} else {
			treeViewer.setSelection(new StructuredSelection(new MainTreeItem(
					TreeItemId.VIEWFOLDER, null, null, null)), true);
		}
		setFocus();
	}

	/**
	 * Adds an item to SAFR explorer tree.If an element already exist then it
	 * update existing element else it add specified element as a child of
	 * specified parent node.
	 * 
	 * @param parentElement
	 *            node in the SAFR tree under which child element is to be
	 *            added.
	 * @param childElement
	 *            to add under specified parent node.
	 */
	public void addTreeElement(MainTreeItem parentElement,
			MainTreeItem childElement) {
		Widget w = treeViewer.testFindItem(childElement);
		if (w != null) {
			((MainTreeItem) w.getData()).setName(childElement.getName());
			treeViewer.update(childElement, new String[] { "name" });
		} else {
			treeViewer.add(parentElement, childElement);
		}
	}

	/**
	 * This method removes item from SAFR explorer tree.
	 * 
	 * @param parentElement
	 *            : node in the SAFR tree from which child element is to be
	 *            removed.
	 * @param childElement
	 *            : element to remove from under specified parent node.
	 */
	public void removeTreeElement(MainTreeItem parentElement,
			MainTreeItem childElement) {

		Object[] elements = { childElement };
		Widget w = treeViewer.testFindItem(childElement);
		if (w != null) {

			treeViewer.remove(parentElement, elements);
		}
	}

	/**
	 * A sorter class for SAFR tree viewer.
	 * 
	 */
	class SAFRTreeSorter extends ViewerSorter {

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
		    MainTreeItem t1 = (MainTreeItem) e1;
            MainTreeItem t2 = (MainTreeItem) e2;
			if (t1.getId() != t2.getId())
				return 0;
			if (t1.getId()==TreeItemId.VIEWFOLDERCHILD && 
			    t2.getId()==TreeItemId.VIEWFOLDERCHILD) {
			    if (t1.getMetadataId()==0 && t2.getMetadataId() != 0) {
			        return -1;
			    } else if (t1.getMetadataId()!=0 && t2.getMetadataId() == 0) {
                    return 1;			        
			    }
			}
			
			String name1, name2;
			if (viewer == null || !(viewer instanceof ContentViewer)) {
				name1 = e1.toString();
				name2 = e2.toString();
			} else {
				IBaseLabelProvider prov = ((ContentViewer) viewer).getLabelProvider();
				if (prov instanceof ILabelProvider) {
					ILabelProvider lprov = (ILabelProvider) prov;
					name1 = lprov.getText(e1);
					name2 = lprov.getText(e2);
				} else {
					name1 = e1.toString();
					name2 = e2.toString();
				}
			}
			if (name1 == null)
				name1 = "";
			if (name2 == null)
				name2 = "";
			return name1.compareToIgnoreCase(name2);

		}

		@Override
		public boolean isSorterProperty(Object element, String property) {
			if (property.compareTo("name") == 0) {
				return true;
			} else {
				return super.isSorterProperty(element, property);
			}
		}
	}

	public void setViewFolderChildrenNull() {
		TreeItem treeItem = (TreeItem) treeViewer
				.testFindItem(new MainTreeItem(TreeItemId.VIEWFOLDER, null,
						null, null));
		((MainTreeItem) treeItem.getData()).setChildren(null);
	}

	public MainTreeItem getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(MainTreeItem selectedItem) {
	    ISelection select = new StructuredSelection(selectedItem);
	    // needs a path if user or group
	    if (selectedItem.getId().equals(TreeItemId.USER) || selectedItem.getId().equals(TreeItemId.GROUP)) {
    	    MainTreeItem path[] = new MainTreeItem[2];
    	    path[0] = new MainTreeItem(TreeItemId.ADMINISTRATION, "Administration", null, null);
    	    path[1] = selectedItem;
            TreePath tpath = new TreePath(path);
    	    treeViewer.expandToLevel(tpath, 0);
	    }
	    treeViewer.setSelection(select, true);
	}

	// Method to refresh view folder list.
	public void refreshNavigator() {

		setViewFolderChildrenNull();
		treeViewer.refresh();
	}

}
