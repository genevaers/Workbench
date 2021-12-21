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


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * FIXME
 */
public class CellSelection extends SelectionWithFocusRow {
	private List indicesList;
	private List elements;
	
    /**
	 * Creates a structured selection from the given <code>List</code> and
	 * element comparer. If an element comparer is provided, it will be used to
	 * determine equality between structured selection objects provided that
	 * they both are based on the same (identical) comparer. See bug 
	 * 
	 * @param elements
	 *            list of selected elements
	 * @param comparer
	 *            the comparer, or null
	 * @since 3.4
	 */
	public CellSelection(List elements, List indicesList, Object focusElement, IElementComparer comparer) {
        super(elements,focusElement,comparer);
        this.elements = new ArrayList(elements);
        this.indicesList = indicesList;
	}
	
	/**
	 * FIXME
	 * @param element
	 * @return the indices
	 */
	public List getIndices(Object element) {
		return (List) indicesList.get(elements.indexOf(element));
	}	
}
