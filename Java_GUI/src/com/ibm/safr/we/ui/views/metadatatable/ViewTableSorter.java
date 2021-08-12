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

import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBeanConv;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class provides sorting functionality for the Views metadata. Contains a
 * function to sort the selected column of the table in the metadata view in
 * ascending/descending order.
 */
public class ViewTableSorter extends ViewerSorter {
	private int propertyIndex;
	private int dir;

	public ViewTableSorter(int propertyIndex, int dir) {
		super();
		this.propertyIndex = propertyIndex;
		this.dir = dir;
	}

	public int compare(Viewer viewer, Object obj1, Object obj2) {
	    
	    if (obj1 instanceof ViewQueryBeanConv && obj2 instanceof ViewQueryBeanConv) {
	        return compareConverted(obj1, obj2);
	    } else if (obj1 instanceof ViewQueryBean && obj2 instanceof ViewQueryBean) {
	        return compareNormal(obj1,obj2);
	    } else {
	        return 0;
	    }
	    
	}

    protected int compareConverted(Object obj1, Object obj2) {
        ViewQueryBeanConv view1 = (ViewQueryBeanConv) obj1;
		ViewQueryBeanConv view2 = (ViewQueryBeanConv) obj2;

		int rc = 0;

		switch (propertyIndex) {
		case 0:// id
			rc = view1.getId().compareTo(view2.getId());
			break;
		case 1:// name
			rc = UIUtilities.compareStrings(view1.getName(), view2.getName());
			break;
		case 2:// status
			rc = UIUtilities.compareStrings(view1.getStatus(), view2.getStatus());
			break;
        case 3:// phase
            rc = UIUtilities.compareStrings(view1.getPhase(), view2.getPhase());
            break;
		case 4:// output format
			rc = UIUtilities.compareStrings(view1.getOutputFormat(), view2.getOutputFormat());
			break;
		case 5:// aggr level
			rc = UIUtilities.compareStrings(view1.getAggrLevel(), view2.getAggrLevel());
			break;
		case 6:
			rc = UIUtilities.compareDates(view1.getCreateTime(), view2.getCreateTime());
			break;
		case 7:
			rc = UIUtilities.compareStrings(view1.getCreateBy(), view2.getCreateBy());
			break;
		case 8:
			rc = UIUtilities.compareDates(view1.getModifyTime(), view2.getModifyTime());
			break;
		case 9:
			rc = UIUtilities.compareStrings(view1.getModifyBy(), view2.getModifyBy());
			break;
        case 10:
            rc = UIUtilities.compareDates(view1.getActivatedTime(), view2.getActivatedTime());
            break;
        case 11:
            rc = UIUtilities.compareStrings(view1.getActivatedBy(), view2.getActivatedBy());
            break;
        case 12:
            rc = UIUtilities.compareStrings(view1.getCompilerVersion(), view2.getCompilerVersion());
            break;
        case 13:// rights
            rc = UIUtilities.compareEnums(view1.getRights(), view2.getRights());
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
    
    private int compareNormal(Object obj1, Object obj2) {
        ViewQueryBean view1 = (ViewQueryBean) obj1;
        ViewQueryBean view2 = (ViewQueryBean) obj2;

        int rc = 0;

        switch (propertyIndex) {
        case 0:// id
            rc = view1.getId().compareTo(view2.getId());
            break;
        case 1:// name
            rc = UIUtilities.compareStrings(view1.getName(), view2.getName());
            break;
        case 2:// status
            rc = UIUtilities.compareStrings(view1.getStatus(), view2.getStatus());
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
