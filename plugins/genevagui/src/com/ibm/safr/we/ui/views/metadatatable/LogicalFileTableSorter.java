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

import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class provides sorting functionality for the Logical File metadata.
 * Contains a function to sort the selected column of the table in the metadata
 * view in ascending/descending order.
 */

public class LogicalFileTableSorter extends ViewerSorter {

	private int propertyIndex;
	private int dir;

	public LogicalFileTableSorter(int propertyIndex, int dir) {
		super();
		this.propertyIndex = propertyIndex;
		this.dir = dir;
	}

	public int compare(Viewer viewer, Object obj1, Object obj2) {
		LogicalFileQueryBean logicalFileQueryBean1 = (LogicalFileQueryBean) obj1;
		LogicalFileQueryBean logicalFileQueryBean2 = (LogicalFileQueryBean) obj2;

		int rc = 0;

		switch (propertyIndex) {
		case 0:
			rc = logicalFileQueryBean1.getId().compareTo(
					logicalFileQueryBean2.getId());
			break;
		case 1:
			rc = UIUtilities.compareStrings(logicalFileQueryBean1.getName(),
					logicalFileQueryBean2.getName());
			break;
		case 2:
			rc = UIUtilities.compareDates(
					logicalFileQueryBean1.getCreateTime(),
					logicalFileQueryBean2.getCreateTime());
			break;
		case 3:
			rc = UIUtilities.compareStrings(
					logicalFileQueryBean1.getCreateBy(), logicalFileQueryBean2
							.getCreateBy());
			break;
		case 4:
			rc = UIUtilities.compareDates(
					logicalFileQueryBean1.getModifyTime(),
					logicalFileQueryBean2.getModifyTime());
			break;
		case 5:
			rc = UIUtilities.compareStrings(
					logicalFileQueryBean1.getModifyBy(), logicalFileQueryBean2
							.getModifyBy());
			break;
        case 6:
            rc = UIUtilities.compareStrings(
                logicalFileQueryBean1.getRights().getDesc(), 
                logicalFileQueryBean2.getRights().getDesc());
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
