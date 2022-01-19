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
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class provides sorting functionality for the Logical Record metadata.
 * Contains a function to sort the selected column of the table in the metadata
 * view in ascending/descending order.
 */

public class LogicalRecordTableSorter extends ViewerSorter {
	private int propertyIndex;
	private int dir;

	public LogicalRecordTableSorter(int propertyIndex, int dir) {
		super();
		this.propertyIndex = propertyIndex;
		this.dir = dir;
	}

	public int compare(Viewer viewer, Object obj1, Object obj2) {
		LogicalRecordQueryBean logicalRecordQueryBean1 = (LogicalRecordQueryBean) obj1;
		LogicalRecordQueryBean logicalRecordQueryBean2 = (LogicalRecordQueryBean) obj2;

		int rc = 0;

		switch (propertyIndex) {
		case 0:
			rc = logicalRecordQueryBean1.getId().compareTo(
					logicalRecordQueryBean2.getId());
			break;
		case 1:
			rc = UIUtilities.compareStrings(logicalRecordQueryBean1.getName(),
					logicalRecordQueryBean2.getName());
			break;
		case 2:
			rc = UIUtilities.compareStrings(
					logicalRecordQueryBean1.getStatus(),
					logicalRecordQueryBean2.getStatus());
			break;
		case 3:
		    rc = UIUtilities.compareIntegers(logicalRecordQueryBean1.getTotLen(), logicalRecordQueryBean2.getTotLen());
		    break;
        case 4:
            rc = UIUtilities.compareIntegers(logicalRecordQueryBean1.getKeyLen(), logicalRecordQueryBean2.getKeyLen());
            break;
		case 5:
			rc = UIUtilities.compareDates(logicalRecordQueryBean1
					.getCreateTime(), logicalRecordQueryBean2.getCreateTime());
			break;
		case 6:
			rc = UIUtilities.compareStrings(logicalRecordQueryBean1
					.getCreateBy(), logicalRecordQueryBean2.getCreateBy());
			break;
		case 7:
			rc = UIUtilities.compareDates(logicalRecordQueryBean1
					.getModifyTime(), logicalRecordQueryBean2.getModifyTime());
			break;
		case 8:
			rc = UIUtilities.compareStrings(logicalRecordQueryBean1
					.getModifyBy(), logicalRecordQueryBean2.getModifyBy());
			break;
        case 9:
            rc = UIUtilities.compareDates(logicalRecordQueryBean1
                    .getActivatedTime(), logicalRecordQueryBean2.getActivatedTime());
            break;
        case 10:
            rc = UIUtilities.compareStrings(logicalRecordQueryBean1
                    .getActivatedBy(), logicalRecordQueryBean2.getActivatedBy());
            break;
		case 11:
            Code typeCode1 = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.LRTYPE).getCode(logicalRecordQueryBean1.getType());           
            Code typeCode2 = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.LRTYPE).getCode(logicalRecordQueryBean2.getType());           
            rc = UIUtilities.compareStrings(typeCode1.getDescription(), typeCode2.getDescription());
            break;
        case 12:
            rc = UIUtilities.compareEnums(
                logicalRecordQueryBean1.getRights(), 
                logicalRecordQueryBean2.getRights());
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
