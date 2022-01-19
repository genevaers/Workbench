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

import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class is used as a Label Provider for the Logical File metadata.
 * Contains method to provide the text for each column of the table for the
 * Logical File metadata.
 * 
 */

public class LogicalFileTableLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof LogicalFileQueryBean))
			return null;

		LogicalFileQueryBean logicalFileQueryBean = (LogicalFileQueryBean) element;
		switch (columnIndex) {
		case 0:
			if (logicalFileQueryBean.getId() == 0) {
				return "";
			}
			return Integer.toString(logicalFileQueryBean.getId());
		case 1:
			return logicalFileQueryBean.getName();
		case 2:
			String createTime = UIUtilities.formatDate(logicalFileQueryBean
					.getCreateTime());
			return createTime;
		case 3:
			return logicalFileQueryBean.getCreateBy();
		case 4:
			String modifyTime = UIUtilities.formatDate(logicalFileQueryBean
					.getModifyTime());
			return modifyTime;
		case 5:
			return logicalFileQueryBean.getModifyBy();
        case 6:
            return logicalFileQueryBean.getRights().getDesc();
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
