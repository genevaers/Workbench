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

import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class ViewFolderTableLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof ViewFolderQueryBean))
			return null;

		ViewFolderQueryBean viewFolderQueryBean = (ViewFolderQueryBean) element;

		switch (columnIndex) {
		case 0:
			// CQ 8884. Nikita. 30/11/2010
			// This was added to show a blank option in optional combo boxes as
			// part of CQ 8292. A dummy View Folder Query Bean is added to the
			// list for this purpose, whose id is 0 and env id is null. 
			if (viewFolderQueryBean.getId() == 0 && viewFolderQueryBean.getEnvironmentId() == null) {
				return "";
			}
			return Integer.toString(viewFolderQueryBean.getId());
		case 1:
			return viewFolderQueryBean.getName();
		case 2:
			String createTime = UIUtilities.formatDate(viewFolderQueryBean
					.getCreateTime());
			return createTime;
		case 3:
			return viewFolderQueryBean.getCreateBy();
		case 4:
			String modifyTime = UIUtilities.formatDate(viewFolderQueryBean
					.getModifyTime());
			return modifyTime;
		case 5:
			return viewFolderQueryBean.getModifyBy();
        case 6:
            return viewFolderQueryBean.getRights().getDesc();
		}

		return null;
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

}
