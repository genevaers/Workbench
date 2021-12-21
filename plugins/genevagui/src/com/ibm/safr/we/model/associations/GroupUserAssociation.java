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
import com.ibm.safr.we.data.transfer.GroupUserAssociationTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.base.SAFRComponent;

//JAK: Use this for GR-USER associations

public class GroupUserAssociation extends Association {

	private String associatedUserId;

	// Use this ctor for creating a new association
    public GroupUserAssociation(Group associatingGroup, String name,
			String associatedUserId) {
		super(associatingGroup, name, null, SAFRPersistence.NEW);
		this.associatedUserId = associatedUserId;

	}

	// Use this ctor for instantiating an existing association
    public GroupUserAssociation(GroupUserAssociationTransfer trans,
			SAFRComponent associatingGroup) {
		super(trans, associatingGroup);
	}

	@Override
	protected void setObjectData(SAFRTransfer trans) {
		super.setObjectData(trans);
		GroupUserAssociationTransfer grpUserAssociationTrans = (GroupUserAssociationTransfer) trans;
		this.associatedUserId = grpUserAssociationTrans.getUserId();
	}

	@Override
	protected void setTransferData(SAFRTransfer trans) {
		super.setTransferData(trans);
		GroupUserAssociationTransfer grpUserAssociationTrans = (GroupUserAssociationTransfer) trans;
		grpUserAssociationTrans.setUserId(associatedUserId);
	}

	public Integer getEnvironmentId() {
		// this association does not belong to an environment
		return null;
	}

	public String getAssociatedComponentIdString() {
		return associatedUserId;
	}

	public Integer getAssociatedComponentIdNum() {
		return null; // the ID is not numeric
	}

	public void store() {
		// TODO
	}

}
