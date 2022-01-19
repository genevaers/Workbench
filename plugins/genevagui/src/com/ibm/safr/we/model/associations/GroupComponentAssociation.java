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
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.GroupComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Group;

//JAK: use this for GR-PF, GR-LF, GR-LR, GR-UX, etc

public class GroupComponentAssociation extends Association {

	// private SAFREnvironmentalComponent associatedComponent;
	private EditRights rights;
	private Integer associatedComponentId;

    public GroupComponentAssociation(Integer environmentId, 
        Integer associatingGroupId,
        String associatingName,
        Integer associatedComponentId,
        String associatedName,
        EditRights rights) {
        super(environmentId, associatingGroupId, associatingName, associatedName);
        this.associatedComponentId = associatedComponentId;
        this.rights = rights;
    }
	
	// used for creating new association.
    public GroupComponentAssociation(Group associatingGroup,
			String associatedComponentName, Integer associatedComponentId,
			Integer environmentId) {
		super(associatingGroup, associatedComponentName, environmentId,
				SAFRPersistence.NEW);
		this.associatedComponentId = associatedComponentId;
	}

	// used for already created association.
    public GroupComponentAssociation(
			GroupComponentAssociationTransfer grpCmpAssociationTransfer,
			Group group) {
		super(grpCmpAssociationTransfer, group);
	}

	@Override
	public Integer getAssociatedComponentIdNum() {
		return associatedComponentId;
	}

	@Override
	public String getAssociatedComponentIdString() {
		return associatedComponentId.toString();
	}

	public EditRights getRights() {
		return rights;
	}

	public void setRights(EditRights rights) {
		this.rights = rights;
		markModified();
	}

	public boolean canRead() {
		if (rights == EditRights.Read || rights == EditRights.ReadModify
				|| rights == EditRights.ReadModifyDelete) {
			return true;
		}
		return false;
	}

	public boolean canModify() {
		if (rights == EditRights.ReadModify
				|| rights == EditRights.ReadModifyDelete) {
			return true;
		}
		return false;
	}

	public boolean canDelete() {
		if (rights == EditRights.ReadModifyDelete) {
			return true;
		}
		return false;
	}

	@Override
	protected void setObjectData(SAFRTransfer trans) {
		super.setObjectData(trans);
		GroupComponentAssociationTransfer grpCmpntAssociationTrans = (GroupComponentAssociationTransfer) trans;
		this.associatedComponentId = grpCmpntAssociationTrans.getComponentId();
		this.rights = grpCmpntAssociationTrans.getRights();
	}

	@Override
	protected void setTransferData(SAFRTransfer trans) {
		super.setTransferData(trans);
		GroupComponentAssociationTransfer grpCmpntAssociationTrans = (GroupComponentAssociationTransfer) trans;
		grpCmpntAssociationTrans.setComponentId(this.associatedComponentId);
		grpCmpntAssociationTrans.setRights(this.rights);
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
		if (!(obj instanceof GroupComponentAssociation)) {
			return false;
		}
		GroupComponentAssociation that = (GroupComponentAssociation) obj;
		// equal if associated and associating IDs are same
		if (this.getAssociatedComponentIdNum().equals(
				that.getAssociatedComponentIdNum())
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

	@Override
	public void store() throws SAFRException, DAOException {
		// TODO Auto-generated method stub

	}

}
