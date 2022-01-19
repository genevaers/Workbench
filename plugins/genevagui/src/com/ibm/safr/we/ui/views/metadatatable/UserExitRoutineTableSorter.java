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
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * This class provides sorting functionality for the UserExitRoutine metadata.
 * Contains a function to sort the selected column of the table in the metadata
 * view in ascending/descending order.
 */
public class UserExitRoutineTableSorter extends ViewerSorter {
	private int propertyIndex;
	private int dir;

	public UserExitRoutineTableSorter(int propertyIndex, int dir) {
		super();
		this.propertyIndex = propertyIndex;
		this.dir = dir;
	}

	@Override
	public int compare(Viewer viewer, Object obj1, Object obj2) {
		UserExitRoutineQueryBean ue1 = (UserExitRoutineQueryBean) obj1;
		UserExitRoutineQueryBean ue2 = (UserExitRoutineQueryBean) obj2;

		int rc = 0;

		switch (propertyIndex) {
		case 0:
			rc = ue1.getId().compareTo(
					ue2.getId());
			break;
		case 1:
			rc = UIUtilities.compareStrings(
					ue1.getName(),
					ue2.getName());
			break;
		case 2:
			rc = UIUtilities.compareStrings(ue1
					.getProgram(), ue2.getProgram());
			break;
        case 3:
            Code typeCode1 = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.EXITTYPE).getCode(ue1.getType());
            Code typeCode2 = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.EXITTYPE).getCode(ue2.getType());            
            rc = UIUtilities.compareStrings(typeCode1.getDescription(), typeCode2.getDescription());            
            break;			
        case 4:
            Code lanCode1 = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.PROGTYPE).getCode(ue1.getProgramType());
            Code lanCode2 = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.PROGTYPE).getCode(ue2.getProgramType());            
            rc = UIUtilities.compareStrings(lanCode1.getDescription(), lanCode2.getDescription());            
            break;          
		case 5:
			rc = UIUtilities
					.compareDates(ue1.getCreateTime(),
							ue2.getCreateTime());
			break;
		case 6:
			rc = UIUtilities.compareStrings(ue1
					.getCreateBy(), ue2.getCreateBy());
			break;
		case 7:
			rc = UIUtilities
					.compareDates(ue1.getModifyTime(),
							ue2.getModifyTime());
			break;
		case 8:
			rc = UIUtilities.compareStrings(ue1
					.getModifyBy(), ue2.getModifyBy());
			break;
        case 9:
            rc = UIUtilities.compareEnums(
                    ue1.getRights(),
                    ue2.getRights());
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
