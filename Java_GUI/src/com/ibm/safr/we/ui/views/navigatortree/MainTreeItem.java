package com.ibm.safr.we.ui.views.navigatortree;

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

import com.ibm.safr.we.constants.ComponentType;

/**
 * A class user for creating elements for SAFR Explorer tree.
 * 
 */
public class MainTreeItem {

	/**
	 * Enum for components in the SAFR explorer tree.
	 * 
	 */
	public enum TreeItemId {
		NONE, ROOT, ABOUT, CONTROL, ENV, PHYSICALFILE, VIEWFOLDER, VIEWFOLDERCHILD, ADMINISTRATION, USER, GROUP, METADATASHORTVIEW, LRFIELD, LOGICALFILE, USEREXITROUTINE, LOGICALRECORD, LOOKUP, GROUPENV, ENVGROUP, GROUPUSER, USERGROUP, GROUPCOMPONENT
	};

	private Integer metadataId = new Integer(0);
	private TreeItemId id = null;
	private String name = null;
	private MainTreeItem parent = null;
	private List<MainTreeItem> children = new ArrayList<MainTreeItem>();

	/**
	 * 
	 * Constructor for MatadataTreeItem. This constructor should be used for
	 * SAFR tree components which don't have metadata id.It is set to zero as
	 * default eg.environment,Physical file, logical file.
	 * 
	 * @param id
	 *            {@link TreeItemId} of the SAFR tree component.
	 * @param name
	 *            Name of metadata component.
	 * @param parent
	 *            Parent {@link MainTreeItem} in SAFR tree.
	 * @param children
	 *            List of children of this {@link MainTreeItem} .
	 */
	public MainTreeItem(TreeItemId id, String name, MainTreeItem parent,
			List<MainTreeItem> children) {
		this.id = id;
		this.parent = parent;
		this.name = name;
		this.children = children;
	}

	/**
	 * Constructor for MatadataTreeItem. This constructor should be used for
	 * SAFR tree components which have metadata id. eg.view folder children.
	 * 
	 * @param metadataId
	 *            of the SAFR tree component.
	 * @param id
	 *            {@link TreeItemId} of the SAFR tree component.
	 * @param name
	 *            Name of metadata component.
	 * @param parent
	 *            Parent {@link MainTreeItem} in SAFR tree.
	 * @param children
	 *            List of children of this {@link MainTreeItem} .
	 */
	public MainTreeItem(Integer metadataId, TreeItemId id, String name,
			MainTreeItem parent, List<MainTreeItem> children) {
		super();
		this.metadataId = metadataId;
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.children = children;
	}

	public ComponentType getComponentType() {
	    switch (id) {
	    case ENV:
	        return ComponentType.Environment;
        case USEREXITROUTINE:
            return ComponentType.UserExitRoutine;
        case CONTROL:
            return ComponentType.ControlRecord;
        case PHYSICALFILE:
            return ComponentType.PhysicalFile;
        case LOGICALFILE:
            return ComponentType.LogicalFile;
        case LOGICALRECORD:
            return ComponentType.LogicalRecord;
        case LOOKUP:
            return ComponentType.LookupPath;
        case VIEWFOLDERCHILD:
            return ComponentType.View;
        case VIEWFOLDER:
            return ComponentType.ViewFolder;
        case USER:
            return ComponentType.User;
        case GROUP:
            return ComponentType.Group;
	    default:
	        return null;	        
	    }
	}
	/**
	 * This method returns metadata id is for the component.
	 * 
	 * @return metadata id for component.
	 */
	public Integer getMetadataId() {
		return metadataId;
	}

	/**
	 * This method sets metadata id for the component.
	 * 
	 * @param metadataId
	 *            to set for the component.
	 */
	public void setMetadataId(Integer metadataId) {
		this.metadataId = metadataId;
	}

	/**
	 * This method returns {@link TreeItemId} for the component.
	 * 
	 * @return {@link TreeItemId} for the component.
	 */
	public TreeItemId getId() {
		return id;
	}

	/**
	 * This method sets {@link TreeItemId} for the component.
	 * 
	 * @param id
	 *            {@link TreeItemId} to set for the component.
	 */
	public void setId(TreeItemId id) {
		this.id = id;
	}

	/**
	 * This method returns name for the component.
	 * 
	 * @return name for the component.
	 */
	public String getName() {
		return name;
	}

	/**
	 * This method sets metadata name for the component.
	 * 
	 * @param metadata
	 *            name to set for the component.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * This method returns {@link MainTreeItem} parent of the component.
	 * 
	 * @return {@link MainTreeItem} parent of the component.
	 */
	public MainTreeItem getParent() {
		return parent;
	}

	/**
	 * This method sets {@link MainTreeItem} parent of the component.
	 * 
	 * @param parent
	 *            sets {@link MainTreeItem} parent of the component.
	 */
	public void setParent(MainTreeItem parent) {
		this.parent = parent;
	}

	/**
	 * This method returns list of children of the component.
	 * 
	 * @return list of children of the component.
	 */
	public List<MainTreeItem> getChildren() {
		return children;
	}

	/**
	 * This method sets list of children of the component.
	 * 
	 * @param children
	 *            list of children of the component to set.
	 */
	public void setChildren(List<MainTreeItem> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MainTreeItem)) {
			return false;
		}
		MainTreeItem that = (MainTreeItem) obj;
		// equal if metadata id and treeItem id are same.
		if (this.getMetadataId().equals(that.getMetadataId())
				&& (this.id == that.getId())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31
				* hash
				+ (null == this.getMetadataId() ? 0 : this.getMetadataId()
						.hashCode());
		hash = 31 * hash + (null == this.getId() ? 0 : this.getId().hashCode());
		return hash;
	}

}
