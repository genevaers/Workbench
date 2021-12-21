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


import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;

/**
 * A Label provider class for SAFR explorer tree.
 * 
 * 
 */
public class MainTreeLabelProvider extends LabelProvider implements
		IColorProvider {

	public String getText(Object element) {
		return ((MainTreeItem) element).getName();
	}

	public Image getImage(Object element) {

		if (((MainTreeItem) element).getId().equals(TreeItemId.CONTROL)) {
			return UIUtilities.getAndRegisterImage(ImageKeys.CONTROLRECORD);
		} else if (((MainTreeItem) element).getId().equals(TreeItemId.ENV)) {
			return UIUtilities.getAndRegisterImage(ImageKeys.ENVIRONMENT);
		} else if (((MainTreeItem) element).getId().equals(
				TreeItemId.PHYSICALFILE)) {
			return UIUtilities.getAndRegisterImage(ImageKeys.PHYSICALFILE);
		} else if (((MainTreeItem) element).getId().equals(
				TreeItemId.USEREXITROUTINE)) {
			return UIUtilities.getAndRegisterImage(ImageKeys.USEREXITROUTINE);
		} else if (((MainTreeItem) element).getId().equals(
				TreeItemId.LOGICALFILE)) {
			return UIUtilities.getAndRegisterImage(ImageKeys.lOGICALFILE);
		} else if (((MainTreeItem) element).getId().equals(
				TreeItemId.LOGICALRECORD)) {
			return UIUtilities.getAndRegisterImage(ImageKeys.LOGICALRECORD);
		} else if (((MainTreeItem) element).getId().equals(TreeItemId.LOOKUP)) {
			return UIUtilities.getAndRegisterImage(ImageKeys.LOOKUPPATH);
		} else if (((MainTreeItem) element).getId().equals(TreeItemId.VIEWFOLDER) || 
		          ((MainTreeItem) element).getId().equals(TreeItemId.VIEWFOLDERCHILD)) {
			return UIUtilities.getAndRegisterImage(ImageKeys.FOLDER);
		} else if (((MainTreeItem) element).getId().equals(
				TreeItemId.ADMINISTRATION)) {
			return UIUtilities.getAndRegisterImage(ImageKeys.ADMINISTRATOR);
		} else if (((MainTreeItem) element).getId().equals(TreeItemId.USER)) {
			return UIUtilities.getAndRegisterImage(ImageKeys.USER);
		} else if (((MainTreeItem) element).getId().equals(TreeItemId.GROUP)) {
			return UIUtilities.getAndRegisterImage(ImageKeys.GROUP);
		} else {
			return null;
		}
	}

	public Color getBackground(Object element) {
		return null;
	}

	public Color getForeground(Object element) {
		return null;
	}

}
