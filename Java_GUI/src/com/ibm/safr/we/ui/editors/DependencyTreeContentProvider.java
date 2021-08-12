package com.ibm.safr.we.ui.editors;

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


import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.safr.we.model.utilities.DependencyCheckerNode;

public class DependencyTreeContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		if (((DependencyCheckerNode) parentElement).getChildNodes() != null) {
			return ((DependencyCheckerNode) parentElement).getChildNodes()
					.toArray();
		}
		return null;
	}

	public Object getParent(Object element) {
		return ((DependencyCheckerNode) element).getParentNode();
	}

	public boolean hasChildren(Object element) {
		if (((DependencyCheckerNode) element).getChildNodes() != null) {
			return !((DependencyCheckerNode) element).getChildNodes().isEmpty();
		}
		return false;

	}

	public Object[] getElements(Object inputElement) {
		DependencyCheckerNode dependencyCheckerModelRoot = ((DependencyCheckerNode) inputElement);
		return getChildren(dependencyCheckerModelRoot);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
