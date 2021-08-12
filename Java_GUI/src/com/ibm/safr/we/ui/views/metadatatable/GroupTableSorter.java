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

import com.ibm.safr.we.model.query.GroupQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class GroupTableSorter extends ViewerSorter {
	private int propertyIndex;
	private int dir;

	public GroupTableSorter(int propertyIndex, int dir) {
		super();
		this.propertyIndex = propertyIndex;
		this.dir = dir;
	}

	public int compare(Viewer viewer, Object obj1, Object obj2) {
		GroupQueryBean group1 = (GroupQueryBean) obj1;
		GroupQueryBean group2 = (GroupQueryBean) obj2;

		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = group1.getId().compareTo(group2.getId());
			break;
		case 1:
			rc = UIUtilities.compareStrings(group1.getName(), group2.getName());
			break;
		case 2:
			rc = UIUtilities.compareDates(group1.getCreateTime(), group2
					.getCreateTime());
			break;
		case 3:
			rc = UIUtilities.compareStrings(group1.getCreateBy(), group2
					.getCreateBy());
			break;
		case 4:
			rc = UIUtilities.compareDates(group1.getModifyTime(), group2
					.getModifyTime());
			break;
		case 5:
			rc = UIUtilities.compareStrings(group1.getModifyBy(), group2
					.getModifyBy());
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
