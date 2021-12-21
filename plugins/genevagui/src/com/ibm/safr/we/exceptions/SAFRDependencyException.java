package com.ibm.safr.we.exceptions;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;

public class SAFRDependencyException extends SAFRException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<ComponentType, List<String>> dependencyList;
	private String contextMessage;

	/**
	 * Use this constructor to create a dependency exception using a list of
	 * dependencies arranged by component type. The client can than retrieve
	 * these dependencies either in a form of the original map or as a formatted
	 * string.
	 * 
	 * @param dependencyComponentList
	 *            : a map that contains component type as key and dependent
	 *            component transfer as value.
	 */
	public SAFRDependencyException(
			Map<ComponentType, List<DependentComponentTransfer>> dependencyComponentList) {
		if (dependencyComponentList != null) {
			dependencyList = new HashMap<ComponentType, List<String>>();
			for (ComponentType componentType : dependencyComponentList.keySet()) {
				List<String> stringList = new ArrayList<String>();
				for (DependentComponentTransfer depComTransfer : dependencyComponentList
						.get(componentType)) {
					Integer id = depComTransfer.getId();
					String name = depComTransfer.getName();
					String dependencyInfo = "";
					if (depComTransfer.getDependencyInfo() != null
							&& !depComTransfer.getDependencyInfo().equals("")) {
						dependencyInfo = " - "
								+ depComTransfer.getDependencyInfo();
					}

					stringList.add(name + ((id > 0) ? ("[" + id + "]") : "")
							+ dependencyInfo);
				}
				dependencyList.put(componentType, stringList);
			}
		}
	}

	/**
	 * This method will return a map which has Component type as the key and a
	 * list of string which contains the id and name of the components.
	 * 
	 * @return the map.
	 */
	public Map<ComponentType, List<String>> getDependencyList() {
		return dependencyList;
	}

	/**
	 * This method is used to get a formatted string containing Component type
	 * and the dependent components of that type. Returns <code>null</code> if
	 * no dependencies are found.
	 * 
	 * @return a formatted string.
	 */
	public String getDependencyString() {
		String dependencyString = null;
		if (dependencyList != null) {
			dependencyString = "";
			for (ComponentType componentType : dependencyList.keySet()) {
				dependencyString += componentType.getLabel() + ":" + LINEBREAK;
				for (String strList : dependencyList.get(componentType)) {
					dependencyString += "    " + strList + LINEBREAK;
				}
			}
		}
		return dependencyString;
	}

	/**
	 * This method is used to get a formatted string with specified indent
	 * amount containing component type and the dependent components of that
	 * type. Returns <code>null</code> if no dependencies are found.
	 * 
	 * @param indentAmount
	 *            it is number of spaces of indentation for the string.
	 * @return a formatted string with specified indentation.
	 */
	public String getDependencyString(int indentAmount) {
		String dependencyString = null;
		if (dependencyList != null) {
			String indentation = "";
			while (indentAmount > 0) {
				indentation += " ";
				indentAmount--;
			}

			dependencyString = "";
			for (ComponentType componentType : dependencyList.keySet()) {
				dependencyString = dependencyString + indentation
						+ componentType.getLabel() + ":" + LINEBREAK;
				for (String strList : dependencyList.get(componentType)) {
					dependencyString += indentation + "    " + strList + LINEBREAK;
				}
			}
			// cut off the trailing line break.
			if (dependencyString.length() > 1) {
				dependencyString = dependencyString.substring(0,
						dependencyString.length() - 1);
			}
		}
		return dependencyString;
	}
	
	/**
	 * Returns additional context information for the dependencies
	 * returned by the getDependencyString methods.
	 * 
	 * @return the contextMessage
	 */
	public String getContextMessage() {
		return contextMessage;
	}

	/**
	 * Sets additional context information for the dependencies
	 * returned by the getDependencyString methods.
	 * 
	 * @param contextMessage
	 *            the contextMessage to set
	 */
	public void setContextMessage(String contextMessage) {
		this.contextMessage = contextMessage;
	}

    @Override
    public String toString() {
        String str = "Inactive Dependencies" + LINEBREAK + LINEBREAK;
        str += "The View could not be loaded because the following component(s) are inactive." + LINEBREAK
                + "Please reactivate these and try again." + LINEBREAK + LINEBREAK;
        str += getDependencyString();
        return str;
    }
	
	
	
}
