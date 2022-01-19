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


import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.ibm.safr.we.model.query.GroupQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class GroupTableLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof GroupQueryBean))
			return null;

		GroupQueryBean groupFileBean = (GroupQueryBean) element;

		switch (columnIndex) {
		case 0:
			if (groupFileBean.getId() == 0) {
				return "";
			}
			return Integer.toString(groupFileBean.getId());
		case 1:
			return groupFileBean.getName();
		case 2:
			String createTime = UIUtilities.formatDate(groupFileBean
					.getCreateTime());
			return createTime;
		case 3:
			return groupFileBean.getCreateBy();
		case 4:
			String modifyTime = UIUtilities.formatDate(groupFileBean
					.getModifyTime());
			return modifyTime;
		case 5:
			return groupFileBean.getModifyBy();
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

}
