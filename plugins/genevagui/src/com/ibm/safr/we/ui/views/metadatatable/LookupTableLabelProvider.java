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

import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class is used as a Label Provider for the Lookup metadata. Contains
 * method to provide the text for each column of the table for the Lookup
 * metadata.
 * 
 */
public class LookupTableLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof LookupQueryBean))
			return null;

		LookupQueryBean lookupQueryBean = (LookupQueryBean) element;
		switch (columnIndex) {
		case 0:
			return Integer.toString(lookupQueryBean.getId());
		case 1:
			return lookupQueryBean.getName();
		case 2:
			if (lookupQueryBean.getValidInd() == 0) {
				return "Inactive";
			} else {
				return "Active";
			}
        case 3:
            return lookupQueryBean.getnSteps().toString();
        case 4:
            return lookupQueryBean.getSourceLR();
        case 5:
            return lookupQueryBean.getTargetLR();
        case 6:
            return lookupQueryBean.getTargetLF();
		case 7:
			String createTime = UIUtilities.formatDate(lookupQueryBean
					.getCreateTime());
			return createTime;
		case 8:
			return lookupQueryBean.getCreateBy();
		case 9:
			String modifyTime = UIUtilities.formatDate(lookupQueryBean
					.getModifyTime());
			return modifyTime;
		case 10:
			return lookupQueryBean.getModifyBy();
        case 11:
            String actTime = UIUtilities.formatDate(lookupQueryBean.getActivatedTime());
            return actTime;
        case 12:
            return lookupQueryBean.getActivatedBy();
        case 13:
            return lookupQueryBean.getRights().getDesc();            
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
