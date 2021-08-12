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

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class is used as a Label Provider for the Logical Record metadata.
 * Contains method to provide the text for each column of the table for the
 * Logical Record metadata.
 * 
 */
public class LogicalRecordTableLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof LogicalRecordQueryBean))
			return null;

		LogicalRecordQueryBean logicalRecordQueryBean = (LogicalRecordQueryBean) element;
		switch (columnIndex) {
		case 0:
			return Integer.toString(logicalRecordQueryBean.getId());
		case 1:
			return logicalRecordQueryBean.getName();
		case 2:
			String status = logicalRecordQueryBean.getStatus();
			if (status == null) {
				return "";
			}
			try {
				return SAFRApplication.getSAFRFactory().getCodeSet(
						CodeCategories.LRSTATUS).getCode(status).getDescription();
			} catch (IllegalArgumentException e) {
				//Absorb the message here and defer until the offending view is opened
				return "";
			}
		case 3:
		    return logicalRecordQueryBean.getTotLen().toString();
        case 4:
            return logicalRecordQueryBean.getKeyLen().toString();
		case 5:
			String createTime = UIUtilities.formatDate(logicalRecordQueryBean.getCreateTime());
			return createTime;
		case 6:
			return logicalRecordQueryBean.getCreateBy();
		case 7:
			String modifyTime = UIUtilities.formatDate(logicalRecordQueryBean.getModifyTime());
			return modifyTime;
		case 8:
			return logicalRecordQueryBean.getModifyBy();
        case 9:
            String actTime = UIUtilities.formatDate(logicalRecordQueryBean.getActivatedTime());
            return actTime;
        case 10:
            return logicalRecordQueryBean.getActivatedBy();
		case 11:
            Code typeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.LRTYPE).getCode(logicalRecordQueryBean.getType());		    
		    return typeCode.getDescription();
        case 12:
            return logicalRecordQueryBean.getRights().getDesc();
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
