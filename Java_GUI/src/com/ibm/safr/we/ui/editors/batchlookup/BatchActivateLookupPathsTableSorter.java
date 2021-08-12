package com.ibm.safr.we.ui.editors.batchlookup;

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

import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.model.utilities.BatchComponent;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class provides sorting functionality for the Batch Activate Lookup
 * Paths. Contains a function to sort the selected column of the table in the
 * Batch Activate Lookup Paths editor in ascending/descending order.
 */
public class BatchActivateLookupPathsTableSorter extends ViewerSorter {
	private static final String PASS = "Pass";
	private static final String FAIL = "Fail";
	private static final String LOADERROR = "Load Error";
	private int propertyIndex;
	private int dir;

	public BatchActivateLookupPathsTableSorter(int propertyIndex, int dir) {

		super();
		this.propertyIndex = propertyIndex;
		this.dir = dir;
	}

	public int compare(Viewer viewer, Object obj1, Object obj2) {
		BatchComponent batchComponent1 = (BatchComponent) obj1;
		BatchComponent batchComponent2 = (BatchComponent) obj2;
		int rc = 0;

		switch (propertyIndex) {
		case 0:
			rc = batchComponent1.isSelected().compareTo(
					batchComponent2.isSelected());
			break;

		case 1:
			if ((batchComponent1.getResult() == null)
					&& (batchComponent2.getResult() == null)) {

				rc = 0;
			} else if (batchComponent1.getResult() == batchComponent2
					.getResult()) {
				rc = 0;
			} else {

				String str1 = "";
				if (batchComponent1.getResult() == ActivityResult.PASS) {
					str1 = PASS;
				} else if (batchComponent1.getResult() == ActivityResult.FAIL) {
					str1 = FAIL;

				} else if (batchComponent1.getResult() == ActivityResult.LOADERRORS) {
					str1 = LOADERROR;
				}

				String str2 = "";
				if (batchComponent2.getResult() == ActivityResult.PASS) {
					str2 = PASS;
				} else if (batchComponent2.getResult() == ActivityResult.FAIL) {
					str2 = FAIL;

				} else if (batchComponent2.getResult() == ActivityResult.LOADERRORS) {
					str2 = LOADERROR;
				}

				rc = UIUtilities.compareStrings(str1, str2);
			}
			break;
		case 2:
			rc = batchComponent1.isActive().compareTo(
					batchComponent2.isActive());
			break;
		case 3:
			rc = UIUtilities.compareIntegers(batchComponent1.getComponent()
					.getId(), batchComponent2.getComponent().getId());
			break;
		case 4:
			rc = UIUtilities.compareStrings(batchComponent1.getComponent()
					.getName(), batchComponent2.getComponent().getName());
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
