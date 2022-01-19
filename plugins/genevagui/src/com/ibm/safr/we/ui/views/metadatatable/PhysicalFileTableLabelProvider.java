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
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class is used as a Label Provider for the Physical File metadata.
 * Contains method to provide the text for each column of the table for the
 * Physical File metadata.
 * 
 */
public class PhysicalFileTableLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {

		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof PhysicalFileQueryBean))
			return null;

		PhysicalFileQueryBean physicalFileBean = (PhysicalFileQueryBean) element;

		switch (columnIndex) {
		case 0:
			return Integer.toString(physicalFileBean.getId());
		case 1:
			return physicalFileBean.getName();
		case 2:
            Code typeCode = PhysicalFile.getModelFileTypeCode(
                physicalFileBean.getFileType(), physicalFileBean.getDiskFileType());
		    return typeCode.getDescription();
        case 3:
            return SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.ACCMETHOD).
                getCode(physicalFileBean.getAccessMethod()).getDescription();                
        case 4:
            return physicalFileBean.getInputDD();
        case 5:
            return physicalFileBean.getInputDSN();
        case 6:
            return physicalFileBean.getOutputDD();
		case 7:
			String createTime = UIUtilities.formatDate(physicalFileBean
					.getCreateTime());
			return createTime;
		case 8:
			return physicalFileBean.getCreateBy();
		case 9:
			String modifyTime = UIUtilities.formatDate(physicalFileBean
					.getModifyTime());
			return modifyTime;
		case 10:
			return physicalFileBean.getModifyBy();
        case 11:
            return physicalFileBean.getRights().getDesc();
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
