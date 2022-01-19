package com.ibm.safr.we.ui.utilities;

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


import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.MetadataSearchCriteria;

/**
 * A searchable part is an application window (Editor, View or Dialog) which
 * provides a facility to search a metadata component. For example, the
 * application window can have a list of metadata components, by implementing
 * this interface, the application window specifies how it will search the list
 * for a metadata component.
 */
public interface ISearchablePart {
	/**
	 * This method is used to search a metadata component.The search is based on
	 * the search criteria which can be either id or name of the component to be
	 * searched.
	 * 
	 * @param searchCriteria
	 *            : can be either id or name of the component to be searched.
	 * @param searchText
	 *            : the text to be searched.
	 */
	public void searchComponent(MetadataSearchCriteria searchCriteria,
			String searchText);

	/**
	 * This method will return the type of component to be searched.
	 * 
	 * @return the component type.
	 */
	public ComponentType getComponentType();
}
