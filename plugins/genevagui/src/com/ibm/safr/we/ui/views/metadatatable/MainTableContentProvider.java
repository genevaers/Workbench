package com.ibm.safr.we.ui.views.metadatatable;

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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.SAFRQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;

public class MainTableContentProvider implements IStructuredContentProvider {
	private List<SAFRQueryBean> componentList = new ArrayList<SAFRQueryBean>();
	private MainTreeItem prevTreeItem;
	private boolean forceRefresh;

	public Object[] getElements(Object inputElement) {
		try {
			MainTreeItem currId = (MainTreeItem) inputElement;
			TreeItemId inputElementID = ((MainTreeItem) inputElement).getId();
			if (!currId.equals(prevTreeItem) || forceRefresh) {
				prevTreeItem = currId;
				if (inputElementID.equals(TreeItemId.VIEWFOLDERCHILD)) {
					// SAFRQuery will return a list of all views.
					componentList.clear();
					componentList.addAll(SAFRQuery.queryAllViewsConv(
					    UIUtilities.getCurrentEnvironmentID(),
					    ((MainTreeItem) inputElement).getMetadataId(),
					    SortType.SORT_BY_NAME));
				} else if ((inputElementID).equals(TreeItemId.ENV)) {
					// SAFRQuery will return a list of all environments.
					componentList.clear();
					componentList.addAll(SAFRQuery.queryEnvironmentsForLoggedInUser(
					    SortType.SORT_BY_NAME, false));
				} else if ((inputElementID).equals(TreeItemId.CONTROL)) {
					// SAFRQuery will return a list of all control records.
					componentList.clear();
					componentList.addAll(SAFRQuery.queryAllControlRecords(
							UIUtilities.getCurrentEnvironmentID(),
							SortType.SORT_BY_NAME));
				} else if ((inputElementID).equals(TreeItemId.PHYSICALFILE)) {
					// SAFRQuery will return a list of all physical files.
					componentList.clear();
					componentList.addAll(SAFRQuery.queryAllPhysicalFiles(
							UIUtilities.getCurrentEnvironmentID(),
							SortType.SORT_BY_NAME));
				} else if ((inputElementID).equals(TreeItemId.GROUP)) {
					// SAFRQuery will return a list of all Groups.
					componentList.clear();
					componentList.addAll(SAFRQuery
							.queryAllGroups(SortType.SORT_BY_NAME));
				} else if ((inputElementID).equals(TreeItemId.VIEWFOLDER)) {
					// SAFRQuery will return a list of all View Folders.
					componentList.clear();
					componentList.addAll(SAFRQuery.queryAllViewFolders(
							UIUtilities.getCurrentEnvironmentID(),
							SortType.SORT_BY_NAME));
				} else if ((inputElementID).equals(TreeItemId.USER)) {
					// SAFRQuery will return a list of all users
					componentList.clear();
					componentList.addAll(SAFRQuery.queryAllUsers());
				} else if ((inputElementID).equals(TreeItemId.USEREXITROUTINE)) {
					// SAFRQuery will return a list of all USEREXITROUTINE.
					componentList.clear();
					componentList.addAll(SAFRQuery.queryAllUserExitRoutines(
							UIUtilities.getCurrentEnvironmentID(),
							SortType.SORT_BY_NAME));
				} else if ((inputElementID).equals(TreeItemId.LOGICALRECORD)) {
					// SAFRQuery will return a list of all LOGICALRECORD.
					componentList.clear();
					componentList.addAll(SAFRQuery.queryAllLogicalRecords(
							UIUtilities.getCurrentEnvironmentID(),
							SortType.SORT_BY_NAME));
				} else if ((inputElementID).equals(TreeItemId.LOOKUP)) {
					// SAFRQuery will return a list of all LOOKUP.
					componentList.clear();
					componentList.addAll(SAFRQuery.queryAllLookups(UIUtilities
							.getCurrentEnvironmentID(), SortType.SORT_BY_NAME));
				} else if ((inputElementID).equals(TreeItemId.LOGICALFILE)) {
					// SAFRQuery will return a list of all LOGICALFILE.
					componentList.clear();
					componentList.addAll(SAFRQuery.queryAllLogicalFiles(
							UIUtilities.getCurrentEnvironmentID(),
							SortType.SORT_BY_NAME));
				} else {
					componentList.clear();
					return new String[0];
				}
			}
			return componentList.toArray();
		} catch (DAOException e) {
			UIUtilities
					.handleWEExceptions(
							e,
							"Unexpected database error occurred while getting elements for Metadata table.",
							UIUtilities.titleStringDbException);
		}
		return new String[0];
	}

	public void dispose() {
	}

	public void setForceRefresh(boolean forceRefresh) {
		this.forceRefresh = forceRefresh;
	}

	public void resetPreviousItem() {
		prevTreeItem = null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
