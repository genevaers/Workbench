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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.safr.we.constants.ComponentType;

/**
 * This class represents a node in the Dependency Checker tree. This tree shows
 * direct relationships (dependencies) between metadata components. The tree
 * starts with some chosen component as the root node. This root node will
 * contain child nodes representing metadata components that are directly
 * related to the root node. Each child node will in turn contain its own child
 * nodes representing metadata components directly related to it and so on.
 * <p>
 * Note the following characteristics of the tree nodes, as represented in this
 * class:
 * <ul>
 * <li>the tree has one root node
 * <li>the root node has no parent node
 * <li>all non-root nodes have a parent node
 * <li>a node whose list of child nodes is empty is a leaf node
 * <li>in SAFR terms, there are 3 types of node:
 * <ul>
 * <li>a component node, which represents a SAFR component
 * <li>a component type node, which represents one of the types 
 * Field, Logical Record, Logical File, Physical File, User Exit Routine, Lookup
 * Path or View
 * <li>a usage type node, which represents how or where a component is used
 * within a View or Lookup Path (Source, Target, Output, Effective Date, Sort
 * Key Title, Properties).
 * </ul>
 * <li>Child components of a given component node will be grouped by component
 * type and if they are used within a Lookup or View will be then sub-grouped by
 * usage type.
 * </ul>
 * <p>
 * Component nodes will have an ID and a name. Component type nodes and usage
 * type nodes will have a name only (as shown above), but no ID. This means
 * their ID value is null, not zero.
 */

public class DependencyCheckerNode {

	// Component type names - see enum ComponentType.getLabel()

	private Integer id = 0; //avoid the NPEs
	private String name =""; //avoid the NPEs
	private DependencyCheckerNode parentNode;
	private List<DependencyCheckerNode> childNodes = new ArrayList<DependencyCheckerNode>();
	private ComponentType type;

	DependencyCheckerNode(Integer id, String name,
			DependencyCheckerNode parentNode, ComponentType type) {
		this.id = id;
		this.name = name;
		this.parentNode = parentNode;
		this.type = type;
	}

	/**
	 * @return an Integer id if the node represents a component or null if it
	 *         represents a component type or usage type.
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * If the node represents a component, this method provides the component
	 * name. If the node represents a component type or usage type, then this
	 * method provides the type name.
	 * 
	 * @return the String component name, component type name or usage type
	 *         name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return a DependencyCheckerNode representing the parent of this node or
	 *         null if this node is the root node.
	 */
	public DependencyCheckerNode getParentNode() {
		return parentNode;
	}

	/**
	 * @return a List of DependencyCheckerNodes representing the children of
	 *         this node or an empty List if this node is a leaf node.
	 */
	public List<DependencyCheckerNode> getChildNodes() {
		return childNodes;
	}

	/**
	 * @param node
	 *            the DependencyCheckerNode to be added to the list of child
	 *            nodes
	 */
	public void addChildNode(DependencyCheckerNode node) {
		//Avoid duplicates
		if(findChild(node.getId(), node.getName()) == null) {
			childNodes.add(node);
		}
	}
	
	public DependencyCheckerNode findChild(Integer id, String name){
		Iterator<DependencyCheckerNode> kiddyIterator = childNodes.iterator();
		while(kiddyIterator.hasNext() ) {
			DependencyCheckerNode kid = kiddyIterator.next();
			if(kid.getName().equals(name)) {
				if(kid.getId() != null && kid.getId().equals(id)){ //apparently kid id can be null? (Lookups)
					return kid;
				}
			}
		}
		return null;
	}

	/**
	 * @return true if this node represents a component, otherwise false
	 */
	public boolean isComponent() {
		return id == null ? false : true;
	}

	/**
	 * Each node has a descriptive label. For a component node it is the node's
	 * name and ID concatenated as 'name [ID]'. For a component type or usage
	 * type node it is just the node's name (these nodes have no ID).
	 * 
	 * @return a String that describes the node
	 */
	public String getLabel() {
		name = (name == null ? "" : name);
		return id == null ? name : name + " [" + id + "]";
	}

	/**
	 * Return the type of Component
	 * @return
	 */
    public ComponentType getType() {
        return type;
    }

	
}
