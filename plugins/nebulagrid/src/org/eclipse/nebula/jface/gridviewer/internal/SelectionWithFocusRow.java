package org.eclipse.nebula.jface.gridviewer.internal;

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


import java.util.List;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * FIXME
 */
public class SelectionWithFocusRow extends StructuredSelection {
	private Object focusElement;

	/**
	 * FIXME
	 * @param elements
	 * @param focusElement
	 * @param comparer
	 */
	public SelectionWithFocusRow(List elements, Object focusElement, IElementComparer comparer) {
        super(elements,comparer);
        this.focusElement = focusElement;
	}
	
	/**
	 * FIXME
	 * @return the focus element
	 */
	public Object getFocusElement() {
		return focusElement;
	}

}
