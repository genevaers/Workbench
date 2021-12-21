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


import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

//JAK: Use this for LR-LF associations

public class ComponentAssociation extends Association {

    private Integer associationId;
    private Integer associatedComponentId;
	private EditRights associatedComponentRights;

	// use for unit tests
    public ComponentAssociation(Integer associationId,
        Integer associatingComponentId,  Integer associatedComponentId) {
        super(associatingComponentId);
        this.associationId = associationId;
        this.associatedComponentId = associatedComponentId;
    }
	
	// Use this ctor for creating a new association (e.g. LR ADD LF)
	public ComponentAssociation(
			SAFREnvironmentalComponent associatingComponent,
			Integer associatedComponentId, String associatedComponentName,
			EditRights associatedCompRights) throws SAFRException {
		super(associatingComponent.getEnvironment().getId(),
				associatingComponent.getId(), associatingComponent.getName(),
				associatedComponentName);
		this.associationId = new Integer(0);
		this.associatedComponentId = associatedComponentId;
		this.associatedComponentRights = associatedCompRights;
	}

	// Use this ctor for instantiating an existing association
    public ComponentAssociation(ComponentAssociationTransfer trans,
			SAFREnvironmentalComponent associatingComponent) {
		super(trans, associatingComponent);
	}

	// Use this ctor for instantiating an existing association
	public ComponentAssociation(ComponentAssociationTransfer trans) {
		super(trans);
	}

	@Override
	public Integer getAssociatedComponentIdNum() {
		return associatedComponentId;
	}
	
	@Override
	public String getAssociatedComponentIdString() {
		return associatedComponentId.toString();
	}

	public Integer getAssociationId() {
		return associationId;
	}

    public void setAssociationId(Integer associationId) {
        this.associationId = associationId;
    }
    	
	public EditRights getAssociatedComponentRights() {
		return associatedComponentRights;
	}

	// No setter for associationId.

	@Override
	protected void setObjectData(SAFRTransfer trans) {
		super.setObjectData(trans);
		ComponentAssociationTransfer caTrans = (ComponentAssociationTransfer) trans;
		this.associationId = caTrans.getAssociationId();
		this.associatedComponentId = caTrans.getAssociatedComponentId();
		this.associatedComponentRights = caTrans.getAssociatedComponentRights();
	}

	@Override
	protected void setTransferData(SAFRTransfer trans) {
		super.setTransferData(trans);
		ComponentAssociationTransfer caTrans = (ComponentAssociationTransfer) trans;
		caTrans.setAssociationId(associationId);
		caTrans.setAssociatedComponentId(associatedComponentId);
		caTrans.setAssociatedComponentRights(associatedComponentRights);
	}

	@Override
	public void store() throws SAFRException, DAOException {
		// TODO
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object) Two objects of this type
	 * are equal if their associating component id and associated component id
	 * are same.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ComponentAssociation)) {
			return false;
		}
		ComponentAssociation that = (ComponentAssociation) obj;
		// equal if associated and associating IDs are same
		if (this.associatedComponentId.equals(that
				.getAssociatedComponentIdNum())
				&& this.getAssociatingComponentId().equals(
						that.getAssociatingComponentId())) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode() Combination of associating component Id
	 * and associated component ID make up a unique identified of this
	 * association.
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31
				* hash
				+ (null == this.getAssociatedComponentIdNum() ? 0 : this
						.getAssociatedComponentIdNum().hashCode());
		hash = 31
				* hash
				+ (null == this.getAssociatingComponentId() ? 0 : this
						.getAssociatingComponentId().hashCode());
		return hash;
	}

}
