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

import com.ibm.safr.we.model.query.ControlRecordQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class provides sorting functionality for the Control Record metadata.
 * Contains a function to sort the selected column of the table in the metadata
 * view in ascending/descending order.
 */
public class ControlRecordTableSorter extends ViewerSorter {

	private int propertyIndex;
	private int dir;

	public ControlRecordTableSorter(int propertyIndex, int dir) {
		super();
		this.propertyIndex = propertyIndex;
		this.dir = dir;
	}

	@Override
	public int compare(Viewer viewer, Object obj1, Object obj2) {
		ControlRecordQueryBean controlRecordBean1 = (ControlRecordQueryBean) obj1;
		ControlRecordQueryBean controlRecordBean2 = (ControlRecordQueryBean) obj2;

		int rc = 0;

		switch (propertyIndex) {
		case 0:
			rc = controlRecordBean1.getId().compareTo(
					controlRecordBean2.getId());
			break;
		case 1:
			rc = UIUtilities.compareStrings(controlRecordBean1.getName(),
					controlRecordBean2.getName());
			break;
		case 2:
			rc = UIUtilities.compareDates(controlRecordBean1.getCreateTime(),
					controlRecordBean2.getCreateTime());
			break;
		case 3:
			rc = UIUtilities.compareStrings(controlRecordBean1.getCreateBy(),
					controlRecordBean2.getCreateBy());
			break;
		case 4:
			rc = UIUtilities.compareDates(controlRecordBean1.getModifyTime(),
					controlRecordBean2.getModifyTime());
			break;
		case 5:
			rc = UIUtilities.compareStrings(controlRecordBean1.getModifyBy(),
					controlRecordBean2.getModifyBy());
			break;
        case 6:
            rc = controlRecordBean1.getRights().compareTo(
                    controlRecordBean2.getRights());
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
