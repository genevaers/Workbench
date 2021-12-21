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

import com.ibm.safr.we.model.utilities.FindReplaceComponent;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class provides sorting functionality for the Find Replace LogicText.
 * Contains a function to sort the selected column of the table in the Find
 * Replace LogicText editor in ascending/descending order.
 */

public class FindReplaceTableSorter extends ViewerSorter {

	private int propertyIndex;
	private int dir;

	public FindReplaceTableSorter(int propertyIndex, int dir) {

		super();
		this.propertyIndex = propertyIndex;
		this.dir = dir;
	}

	public int compare(Viewer viewer, Object obj1, Object obj2) {
		FindReplaceComponent findReplaceComponent1 = (FindReplaceComponent) obj1;
		FindReplaceComponent findReplaceComponent2 = (FindReplaceComponent) obj2;

		int rc = 0;

		switch (propertyIndex) {
		case 0:

			rc = UIUtilities.compareBooleans(
					findReplaceComponent1.isSelected(), findReplaceComponent2
							.isSelected());
			break;

		case 1:
			rc = findReplaceComponent1.getViewId().compareTo(
					findReplaceComponent2.getViewId());
			break;
		case 2:

			rc = findReplaceComponent1.getViewName().compareTo(
					findReplaceComponent2.getViewName());
			break;

		case 3:
			rc = findReplaceComponent1.getLogicTextType().compareTo(
					findReplaceComponent2.getLogicTextType());
			break;
		case 4:
			rc = findReplaceComponent1.getLogicText().compareTo(
					findReplaceComponent2.getLogicText());
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
