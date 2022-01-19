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

import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class provides sorting functionality for the Lookup metadata. Contains a
 * function to sort the selected column of the table in the metadata view in
 * ascending/descending order.
 */
public class LookupTableSorter extends ViewerSorter {

	private int propertyIndex;
	private int dir;

	public LookupTableSorter(int propertyIndex, int dir) {
		super();
		this.propertyIndex = propertyIndex;
		this.dir = dir;
	}

	public int compare(Viewer viewer, Object obj1, Object obj2) {
		LookupQueryBean lookupQueryBean1 = (LookupQueryBean) obj1;
		LookupQueryBean lookupQueryBean2 = (LookupQueryBean) obj2;

		int rc = 0;

		switch (propertyIndex) {
		case 0:
			rc = lookupQueryBean1.getId().compareTo(lookupQueryBean2.getId());
			break;
		case 1:
			rc = UIUtilities.compareStrings(lookupQueryBean1.getName(),
					lookupQueryBean2.getName());
			break;
		case 2:
			rc = -lookupQueryBean1.getValidInd().compareTo(
					lookupQueryBean2.getValidInd());
			break;
		case 3:
            rc = UIUtilities.compareIntegers(lookupQueryBean1.getnSteps(), 
                lookupQueryBean2.getnSteps());
		    break;
        case 4:
            rc = UIUtilities.compareStrings(lookupQueryBean1.getSourceLR(),
                lookupQueryBean2.getSourceLR());
            break;
        case 5:
            rc = UIUtilities.compareStrings(lookupQueryBean1.getTargetLR(),
                lookupQueryBean2.getTargetLR());
            break;
        case 6:
            rc = UIUtilities.compareStrings(lookupQueryBean1.getTargetLF(),
                lookupQueryBean2.getTargetLF());
            break;
		case 7:
			rc = UIUtilities.compareDates(lookupQueryBean1.getCreateTime(),
					lookupQueryBean2.getCreateTime());
			break;
		case 8:
			rc = UIUtilities.compareStrings(lookupQueryBean1.getCreateBy(),
					lookupQueryBean2.getCreateBy());
			break;
		case 9:
			rc = UIUtilities.compareDates(lookupQueryBean1.getModifyTime(),
					lookupQueryBean2.getModifyTime());
			break;
		case 10:
			rc = UIUtilities.compareStrings(lookupQueryBean1.getModifyBy(),
					lookupQueryBean2.getModifyBy());
			break;
        case 11:
            rc = UIUtilities.compareDates(lookupQueryBean1.getActivatedTime(),
                    lookupQueryBean2.getActivatedTime());
            break;
        case 12:
            rc = UIUtilities.compareStrings(lookupQueryBean1.getActivatedBy(),
                    lookupQueryBean2.getActivatedBy());
            break;
        case 13:
            rc = UIUtilities.compareEnums(
                lookupQueryBean1.getRights(), 
                lookupQueryBean2.getRights());
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
