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

import com.ibm.safr.we.model.query.ControlRecordQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * Label Provider for the Control Record metadata. Contains method to provide
 * the text for each column of the table for the Control Record metadata.
 * 
 */
public class ControlRecordTableLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof ControlRecordQueryBean))
			return null;

		ControlRecordQueryBean record = (ControlRecordQueryBean) element;
		switch (columnIndex) {
		case 0:
			return Integer.toString(record.getId());
		case 1:
			return record.getName();
		case 2:
			String createDate = UIUtilities.formatDate(record.getCreateTime());
			return createDate;
		case 3:
			return record.getCreateBy();
		case 4:
			String modifyDate = UIUtilities.formatDate(record.getModifyTime());
			return modifyDate;
		case 5:
			return record.getModifyBy();
        case 6:
            return record.getRights().getDesc();            
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
