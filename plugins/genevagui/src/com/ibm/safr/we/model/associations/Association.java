package com.ibm.safr.we.model.associations;

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


import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.transfer.AssociationTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFRPersistentObject;

/**
 * Represents an association between two SAFR components. This abstraction
 * defines the common characteristics shared by all associations. It can be used
 * to describe the association and to identify the components at each end of the
 * association without needing to refer to the concrete subclass types. However,
 * these concrete subclasses will need to be referenced explicitly to access any
 * additional properties of the association.
 * <p>
 * Associations are defined in this abstraction as have an 'associating
 * component' (the 'from' end of the association) and an 'associated component'
 * (the 'to' end).
 * 
 */
abstract public class Association extends SAFRPersistentObject {

	private Integer environmentId;
	private Integer associatingComponentId;
	private String associatingComponentName;
	private String associatedComponentName;
	//CQ9682 Migration
	private boolean forMigration = false; 

	public Association() {
        super();	    
	}

	// for unit tests
    public Association(Integer associatingComponentId) {
        this.associatingComponentId = associatingComponentId;
    }
	
	/**
	 */
	public Association(SAFRComponent associatingComponent,
			String associatedComponentName, Integer environmentId,
			SAFRPersistence persistence) {

		super();
		this.associatingComponentId = associatingComponent.getId();
		this.associatingComponentName = associatingComponent.getName();
		this.associatedComponentName = associatedComponentName;
		this.environmentId = environmentId;
	}

	public Association(Integer environmentId, Integer associatingComponentId,
			String associatingComponentName, String associatedComponentName) {

		super();
		this.environmentId = environmentId;
		this.associatingComponentId = associatingComponentId;
		this.associatingComponentName = associatingComponentName;
		this.associatedComponentName = associatedComponentName;
	}

	/**
	 */
	public Association(AssociationTransfer trans,
			SAFRComponent associatingComponent) {
		super(trans);
		this.associatingComponentId = associatingComponent.getId();
		this.associatingComponentName = associatingComponent.getName();
	}

	public Association(AssociationTransfer trans) {
		super(trans);
	}

	/**
	 * Returns the ID of the Environment that this association exists in or null
	 * if it is independent of any particular Environment. An association exists
	 * within an Environment when one or both components in the association
	 * belong to an environment (they have the same Environment ID). If neither
	 * component belongs to an Environment, the association is independent of
	 * any Environment as well.
	 */
	public Integer getEnvironmentId() {
	    return environmentId;
	}

	/**
	 * Returns the ID of the associating component.
	 */
	public Integer getAssociatingComponentId() {
		return associatingComponentId;
	}

	/**
	 * @return the associatingComponentName
	 */
	public String getAssociatingComponentName() {
		return associatingComponentName;
	}

	/**
	 * Returns the ID of the associated component as a String.
	 */
	abstract public String getAssociatedComponentIdString();

	/**
	 * Returns the numeric ID of the associated component or null if its ID is
	 * not numeric.
	 */
	abstract public Integer getAssociatedComponentIdNum();

	/**
	 * Returns the name of the associated component as a String.
	 */
	public String getAssociatedComponentName() {
		return associatedComponentName;
	}

	@Override
	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		AssociationTransfer trans = (AssociationTransfer) safrTrans;
		this.associatingComponentId = trans.getAssociatingComponentId();
		this.associatingComponentName = trans.getAssociatingComponentName();
		this.associatedComponentName = trans.getAssociatedComponentName();
		this.environmentId = trans.getEnvironmentId();
	}

	@Override
	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		AssociationTransfer trans = (AssociationTransfer) safrTrans;
		trans.setAssociatingComponentId(associatingComponentId);
		trans.setAssociatingComponentName(associatingComponentName);
		trans.setAssociatedComponentName(associatedComponentName);
		trans.setEnvironmentId(environmentId);
		trans.setForMigration(forMigration);
	}

	public void setTransferFromObject(SAFRTransfer trans) {
		setTransferData(trans);
	}

	public void setObjectFromTransfer(SAFRTransfer trans) throws SAFRException {
		setObjectData(trans);
	}
	
	/**
	 * To be used for migration to change the object's environment ID to the
	 * target environment and specify if the object already exists there.
	 * 
	 * @param targetEnvironmentId
	 */
	public void migrateToEnvironment(Integer targetEnvironmentId,
			SAFRPersistence persistence) {
		this.environmentId = targetEnvironmentId;
		setPersistence(persistence);
		forMigration = true;
	}

	/**
	 * Indicates if this object is being migrated.
	 * 
	 * @return the forMigration boolean flag
	 */
	public boolean isForMigration() {
		return forMigration;
	}
	
	/**
	 * @return a String concatenation of associating component's name and id in the format Name[Id].
	 */
	public String getAssociatingDescriptor() {
		if (associatingComponentName == null && associatingComponentId == null) {
			return "";
		}
		String n = (associatingComponentName != null ? associatingComponentName : "");
		String i = (associatingComponentId != null ? associatingComponentId.toString() : "");
		return n + "[" + i + "]";
	}
	
	/**
	 * @return a String concatenation of associated component's name and id in the format Name[Id].
	 */
	public String getAssociatedDescriptor() {
		if (associatedComponentName == null
				&& (getAssociatedComponentIdString() == null || getAssociatedComponentIdString() == "")) {
			return "";
		}
		String n = (associatedComponentName != null ? associatedComponentName : "");
		String i = (getAssociatedComponentIdString() != null ? getAssociatedComponentIdString() : "");
		return n + "[" + i + "]";
	}
	
}
