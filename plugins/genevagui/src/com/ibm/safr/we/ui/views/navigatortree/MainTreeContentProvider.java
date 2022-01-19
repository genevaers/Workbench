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


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;

/**
 * A content provider class for SAFR explorer tree.
 * 
 */
public class MainTreeContentProvider implements ITreeContentProvider {

	private MainTreeItem mainItemsRoot;

	public Object[] getChildren(Object parentElement) {
		MainTreeItem item = (MainTreeItem) parentElement;
		return item.getChildren().toArray();
	}

	public Object getParent(Object element) {
		MainTreeItem item = (MainTreeItem) element;
		return item.getParent();
	}

	public boolean hasChildren(Object element) {
		MainTreeItem item = (MainTreeItem) element;
		// if the element is view folder check whether it has children.
		if ((item.equals(new MainTreeItem(TreeItemId.VIEWFOLDER, null, null, null)))) {
		    
			List<ViewFolderQueryBean> viewFolderList = null;
			if (item.getChildren() == null || item.getChildren().isEmpty()) {
				try {
					viewFolderList = SAFRQuery.queryAllViewFolders(UIUtilities
					    .getCurrentEnvironmentID(), SortType.SORT_BY_ID);
				} catch (DAOException e) {
					UIUtilities.handleWEExceptions(e,
					    "Database error in getting contents for Navigator View.",
					    UIUtilities.titleStringDbException);
				}
				if (viewFolderList != null) {
					List<MainTreeItem> viewFolderChildrenList = new ArrayList<MainTreeItem>();
					for (ViewFolderQueryBean viewFolderQueryBean : viewFolderList) {
						MainTreeItem temp = new MainTreeItem(viewFolderQueryBean.getId(),
						    TreeItemId.VIEWFOLDERCHILD, viewFolderQueryBean.getName(), item, null);
						viewFolderChildrenList.add(temp);
					}
					item.setChildren(viewFolderChildrenList);
				} else {
					item.setChildren(null);
				}
			}

		}

		if (item.getChildren() == null || item.getChildren().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public Object[] getElements(Object inputElement) {
		mainItemsRoot = new MainTreeItem(TreeItemId.ROOT, "SAFR", null, null);

		MainTreeItem control = new MainTreeItem(TreeItemId.CONTROL,
				"Control Records", mainItemsRoot, null);
		MainTreeItem environments = new MainTreeItem(TreeItemId.ENV,
				"Environments", mainItemsRoot, null);
		MainTreeItem physicalFiles = new MainTreeItem(TreeItemId.PHYSICALFILE,
				"Physical Files", mainItemsRoot, null);
		MainTreeItem logicalFiles = new MainTreeItem(TreeItemId.LOGICALFILE,
				"Logical Files", mainItemsRoot, null);
		MainTreeItem logicalRecords = new MainTreeItem(
				TreeItemId.LOGICALRECORD, "Logical Records", mainItemsRoot,
				null);
		MainTreeItem lookups = new MainTreeItem(TreeItemId.LOOKUP,
				"Lookup Paths", mainItemsRoot, null);
		MainTreeItem viewFolders = new MainTreeItem(TreeItemId.VIEWFOLDER,
				"View Folders", mainItemsRoot, null);

		MainTreeItem userExitRoutines = new MainTreeItem(
				TreeItemId.USEREXITROUTINE, "User-Exit Routines",
				mainItemsRoot, null);
		User user = SAFRApplication.getUserSession().getUser();

		// Administration menus
		MainTreeItem mainAdminMenu = new MainTreeItem(
				TreeItemId.ADMINISTRATION, "Administration", mainItemsRoot,
				null);
		MainTreeItem users = new MainTreeItem(TreeItemId.USER, "Users",
				mainAdminMenu, null);
		MainTreeItem groups = new MainTreeItem(TreeItemId.GROUP, "Groups",
				mainAdminMenu, null);
		List<MainTreeItem> adminChildrens = new ArrayList<MainTreeItem>();
		adminChildrens.add(users);
		adminChildrens.add(groups);
		mainAdminMenu.setChildren(adminChildrens);

		// Root
		List<MainTreeItem> children = new ArrayList<MainTreeItem>();
		children.add(environments);
		children.add(userExitRoutines);
		children.add(control);
		children.add(physicalFiles);
		children.add(logicalFiles);
		children.add(logicalRecords);
		children.add(lookups);
		children.add(viewFolders);
		if (user.isSystemAdmin()) {
			children.add(mainAdminMenu);
		}

		mainItemsRoot.setChildren(children);
		return getChildren(mainItemsRoot);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
