package com.ibm.safr.we.model.utilities;

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
import java.util.Map;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.DependencyUsageType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;

/**
 * Dependency checker utility class. This class is used to retrieve the direct
 * dependencies of a metadata component. The inputs to this class will be the
 * SAFR environment, component type and the root component to retrieve the
 * dependency of. The 'getDependency' method will return a
 * {@link DependencyCheckerNode} item which will be a self contained tree of all
 * the direct dependencies of the root component.
 * 
 * Users can also retrieve dependencies of a dependent component by using
 * 'getDependency(DependencyCheckerNode component)' method.
 * 
 */
public class DependencyChecker {
	private int environmentId;
	private String environmentName;
	private int componentId; // root component id
	private ComponentType componentType; // root component type
	private String componentName; // root component name
	private boolean directDepsOnly; // show direct dependencies only

	/**
	 * Creates an instance of the Dependency Checker Utility.
	 * 
	 * @param environmentId
	 *            the environment id of the root component.
	 * @param componentId
	 *            the root component id.
	 * @param componentType
	 *            the root component type.
	 * @param componentName
	 *            the root component name.
	 */
	public DependencyChecker(int environmentId, String environmentName,
			int componentId, ComponentType componentType, String componentName,
			boolean directDepsOnly) {
		super();
		this.environmentId = environmentId;
		this.environmentName = environmentName;
		this.componentId = componentId;
		this.componentType = componentType;
		this.componentName = componentName;
		this.directDepsOnly = directDepsOnly;
	}

	public int getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(int environmentId) {
		this.environmentId = environmentId;
	}

	public String getEnvironmentName() {
		return environmentName;
	}

	public int getComponentId() {
		return componentId;
	}

	public void setComponentId(int componentId) {
		this.componentId = componentId;
	}

	public ComponentType getComponentType() {
		return componentType;
	}

	public void setComponentType(ComponentType componentType) {
		this.componentType = componentType;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	/**
	 * Generate a dependency tree of the root element.
	 * 
	 * @return the {@link DependencyCheckerNode} element representing the
	 *         dependency tree.
	 * @throws DAOException
	 */
	public DependencyCheckerNode getDependency() throws DAOException {
		// return the tree by calling the generateTree method and passing the
		// parameters for root

		// treeRoot is the root which holds the Dep tree.
		DependencyCheckerNode treeRoot = new DependencyCheckerNode(componentId,
				componentName, null, null);
		// create a parent Node from where the actual Dep tree starts.
		DependencyCheckerNode parentNode = new DependencyCheckerNode(
				componentId, componentName, treeRoot, null);
		treeRoot.addChildNode(parentNode);
		generateTree(componentType, environmentId, componentId, parentNode,
				componentName);
		return treeRoot;
	}

	/**
	 * Generate a dependency tree of a component.
	 * 
	 * @param component
	 *            the {@link DependencyCheckerNode} of type 'Component' using
	 *            which the dependency is to be generated.
	 * @return the {@link DependencyCheckerNode} element representing the
	 *         dependency tree. The root of this tree will be the node passed in
	 *         as parameter.
	 * @throws DAOException
	 * @throws IllegalArgumentException
	 *             if the parameter doesn't represent a component node.
	 */
	public DependencyCheckerNode getDependency(DependencyCheckerNode component)
			throws DAOException {
		// return the tree by calling the generateTree method and passing the
		// parameters from component.
		// get the component type from the node
		if (!component.isComponent()) {
			throw new IllegalArgumentException(
					"The node passed in as a parameter should represent a component");
		}
		ComponentType type = null;
		DependencyCheckerNode node = component.getParentNode();
		while (type == null) {
			// iterate over all the parent nodes until a component type is found
			type = getTypeFromLabel(node.getName());
			node = node.getParentNode();
		}
		return generateTree(type, environmentId, component.getId(), component,
				component.getName());
	}

	private DependencyCheckerNode generateTree(ComponentType type,
			int environmentId, int componentId,
			DependencyCheckerNode parentNode, String componentName)
			throws DAOException {
		Map<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>> dependentComponentsMap;
		// calls the DAO layer to retrieve the dependency and creates a tree

		dependentComponentsMap = DAOFactoryHolder.getDAOFactory()
				.getDependencyCheckerDAO().getDependentComponents(type,
						componentId, environmentId, directDepsOnly);
		
		//showDepCompsMap(dependentComponentsMap); // for debug, dont delete
		
		// loop through the map to generate the tree.
		for (ComponentType comType : dependentComponentsMap.keySet()) {
			DependencyCheckerNode typeNode = new DependencyCheckerNode(null,
					comType.getLabel(), parentNode, comType);
			parentNode.addChildNode(typeNode);
			for (DependencyUsageType usageType : dependentComponentsMap.get(comType).keySet()) {
			    
				DependencyCheckerNode usageNode = new DependencyCheckerNode(
						null, usageType.getLabel(), typeNode, comType);
				// if the usage node is NONE, then the list of child components
				// will directly go under type node.
				if (usageType != DependencyUsageType.NONE) {
					typeNode.addChildNode(usageNode);
				}
				for (DependentComponentTransfer depComponentTransfer : dependentComponentsMap.get(comType).get(usageType)) {
					if (usageType != DependencyUsageType.NONE) {
						DependencyCheckerNode childComponentNode = new DependencyCheckerNode(
								depComponentTransfer.getId(),
								depComponentTransfer.getName(), usageNode, comType);
						usageNode.addChildNode(childComponentNode);
					} else {
						DependencyCheckerNode childComponentNode = new DependencyCheckerNode(
								depComponentTransfer.getId(),
								depComponentTransfer.getName(), typeNode, comType);
						typeNode.addChildNode(childComponentNode);
					}
				}
			}

		}
		return parentNode;
	}

	private ComponentType getTypeFromLabel(String label) {
		for (ComponentType type : ComponentType.values()) {
			if (type.getLabel().equals(label)) {
				return type;
			}
		}
		return null;
	}
	
	// This is a debug method. Don't remove it.
/*	private void showDepCompsMap(Map<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>> depCompsMap) {
		System.out.println("----------");
		for (ComponentType cType : depCompsMap.keySet()) {
			System.out.println(cType.toString());
			for (DependencyUsageType uType : depCompsMap.get(cType).keySet()) {
				System.out.println(uType.toString());
				for (DependentComponentTransfer tfr : depCompsMap.get(cType).get(uType)) {
					System.out.println(tfr.getName() + " [" + tfr.getId() + "]");
				}
			}
		}
	}*/

}
