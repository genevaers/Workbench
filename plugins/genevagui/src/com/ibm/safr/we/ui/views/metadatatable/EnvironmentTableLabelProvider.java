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

import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * Label Provider for the Environment metadata. Contains method to provide the
 * text for each column of the table for the Environment metadata.
 * 
 */
public class EnvironmentTableLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof EnvironmentQueryBean))
			return null;

		EnvironmentQueryBean environment = (EnvironmentQueryBean) element;

		switch (columnIndex) {
		case 0:
			if (environment.getId() == 0) {
				return "";
			}
			return Integer.toString(environment.getId());
		case 1:
			return environment.getName();
		case 2:
			String createTime = UIUtilities.formatDate(environment
					.getCreateTime());
			return createTime;
		case 3:
			return environment.getCreateBy();
		case 4:
			String modifyTime = UIUtilities.formatDate(environment
					.getModifyTime());
			return modifyTime;
		case 5:
			return environment.getModifyBy();
		case 6:
			if (environment.hasAdminRights()) {
				return "Yes";
			} else {
				return "No";
			}
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
