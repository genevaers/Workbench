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


import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * Label Provider for the UserExitRoutine metadata. Contains method to provide
 * the text for each column of the table for the UserExitRoutine metadata.
 * 
 */
public class UserExitRoutineTableLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof UserExitRoutineQueryBean))
			return null;

		UserExitRoutineQueryBean userExitRoutine = (UserExitRoutineQueryBean) element;
		switch (columnIndex) {
		case 0:
			if (userExitRoutine.getId() == 0) {
				return "";
			}
			return Integer.toString(userExitRoutine.getId());
		case 1:
			return userExitRoutine.getName();
		case 2:
			return userExitRoutine.getProgram();
        case 3:
            Code typeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.EXITTYPE).getCode(userExitRoutine.getType());
            return typeCode.getDescription();
        case 4:
            Code lanCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.PROGTYPE).getCode(userExitRoutine.getProgramType());
            return lanCode.getDescription();
		case 5:
			String createDate = UIUtilities.formatDate(userExitRoutine
					.getCreateTime());
			return createDate;
		case 6:
			return userExitRoutine.getCreateBy();
		case 7:
			String modifyDate = UIUtilities.formatDate(userExitRoutine
					.getModifyTime());
			return modifyDate;
		case 8:
			return userExitRoutine.getModifyBy();
        case 9:
            return userExitRoutine.getRights().getDesc();
		}
		return null;
	}

	public void addListener(ILabelProviderListener listener) {

	}

	public void dispose() {

	}

	public boolean isLabelProperty(Object element, String property) {

		return false;
	}

	public void removeListener(ILabelProviderListener listener) {

	}

}
