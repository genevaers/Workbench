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


import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class provides sorting functionality for the PhysicalFile metadata.
 * Contains a function to sort the selected column of the table in the metadata
 * view in ascending/descending order.
 */

public class PhysicalFileTableSorter extends ViewerSorter {
	private int propertyIndex;
	private int dir;

	public PhysicalFileTableSorter(int propertyIndex, int dir) {
		super();
		this.propertyIndex = propertyIndex;
		this.dir = dir;
	}

	public int compare(Viewer viewer, Object obj1, Object obj2) {
		PhysicalFileQueryBean physicalFile1 = (PhysicalFileQueryBean) obj1;
		PhysicalFileQueryBean physicalFile2 = (PhysicalFileQueryBean) obj2;

		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = physicalFile1.getId().compareTo(physicalFile2.getId());
			break;
		case 1:
			rc = UIUtilities.compareStrings(physicalFile1.getName(),
					physicalFile2.getName());
			break;
        case 2:
            Code typeCode1 = PhysicalFile.getModelFileTypeCode(physicalFile1.getFileType(), physicalFile1.getDiskFileType());
            Code typeCode2 = PhysicalFile.getModelFileTypeCode(physicalFile2.getFileType(), physicalFile2.getDiskFileType());            
            rc = UIUtilities.compareStrings(typeCode1.getDescription(), typeCode2.getDescription());
            break;
        case 3:
            Code accCode1 = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.ACCMETHOD).getCode(physicalFile1.getAccessMethod());
            Code accCode2 = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.ACCMETHOD).getCode(physicalFile2.getAccessMethod());            
            rc = UIUtilities.compareStrings(accCode1.getDescription(), accCode2.getDescription());
            break;
        case 4:
            rc = UIUtilities.compareStrings(physicalFile1.getInputDD(),
                physicalFile2.getInputDD());
            break;
        case 5:
            rc = UIUtilities.compareStrings(physicalFile1.getInputDSN(),
                physicalFile2.getInputDSN());
            break;
        case 6:
            rc = UIUtilities.compareStrings(physicalFile1.getOutputDD(),
                physicalFile2.getOutputDD());
            break;			
		case 7:
			rc = UIUtilities.compareDates(physicalFile1.getCreateTime(),
					physicalFile2.getCreateTime());
			break;
		case 8:
			rc = UIUtilities.compareStrings(physicalFile1.getCreateBy(),
					physicalFile2.getCreateBy());
			break;
		case 9:
			rc = UIUtilities.compareDates(physicalFile1.getModifyTime(),
					physicalFile2.getModifyTime());
			break;
		case 10:
			rc = UIUtilities.compareStrings(physicalFile1.getModifyBy(),
					physicalFile2.getModifyBy());
			break;
        case 11:
            rc = UIUtilities.compareEnums(physicalFile1.getRights(),
                    physicalFile2.getRights());
            break;
		default:
			rc = 0;
		}

		// If the direction of sorting was descending previously, reverse the
		// direction of sorting
		if (dir == SWT.DOWN) {
			rc = -rc;
		}
		return rc;
	}
}
