package com.ibm.safr.we.ui.utilities;

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

import com.ibm.safr.we.model.LRField;

public class AssociatedLRFieldSorter extends ViewerSorter {
	private int propertyIndex;
	private int dir;

	public AssociatedLRFieldSorter(int colNumber, int dir) {
		this.propertyIndex = colNumber;
		this.dir = dir;
	}

	public int compare(Viewer viewer, Object obj1, Object obj2) {
		int rc = 0;
		if (!((obj1 instanceof LRField) && (obj2 instanceof LRField))) {
			return rc;
		}
		LRField logicalRecordField1 = (LRField) obj1;
		LRField logicalRecordField2 = (LRField) obj2;

		switch (propertyIndex) {
		case 0:
			rc = logicalRecordField1.getId().compareTo(
					logicalRecordField2.getId());
			break;
		case 1:
			rc = UIUtilities.compareStrings(logicalRecordField1.getName(),
					logicalRecordField2.getName());
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
