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

import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.view.ViewActivationError;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class ViewActivationLogTableLabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {

		ViewActivationError sae = (ViewActivationError) element;
		switch (columnIndex) {
		case 0:
			if (element instanceof SAFRCompilerErrorType) {

				return sae.toString();
			} else if (element instanceof ViewActivationError)
				return sae.getErrorText();

		case 1:
			if (element instanceof ViewActivationError) {
				String column = "Column" + sae.getViewColumn().getId() + "Name"
						+ sae.getViewColumn().getName();
				return column;
			}
		case 2:
			if (element instanceof ViewActivationError) {
				String source = null;
				try {
					source = UIUtilities
							.getComboString(sae.getViewSource()
									.getLrFileAssociation()
									.getAssociatingComponentName(), sae
									.getViewSource().getLrFileAssociation()
									.getAssociatingComponentId());
				} catch (DAOException e) {
					UIUtilities.handleWEExceptions(e,
							"Database error in getting Column Text",
							UIUtilities.titleStringDbException);
				} catch (SAFRException e) {
					UIUtilities.handleWEExceptions(e,
							"Error in getting Column Text", null);
				}

				return source;
			}
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
