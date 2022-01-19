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


import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.UserGroupAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.User;

public class UserGroupAssociation extends Association {

	private Integer associatedGroupId;

	// Use this ctor for creating a new association
	public UserGroupAssociation(User associatingUser,
			Integer associatedgroupId, String associatedComponentName) {

		super(null, associatedgroupId, associatingUser.getUserid(),
				associatedComponentName);
		this.associatedGroupId = associatedgroupId;

	}

	// Use this ctor for instantiating an existing association
	public UserGroupAssociation(UserGroupAssociationTransfer trans) {
		super(trans);
	}

	@Override
	protected void setObjectData(SAFRTransfer trans) {
		super.setObjectData(trans);
		UserGroupAssociationTransfer userGroupAssociationTrans = (UserGroupAssociationTransfer) trans;
		this.associatedGroupId = userGroupAssociationTrans.getGroupId();

	}

	@Override
	protected void setTransferData(SAFRTransfer trans) {
		super.setTransferData(trans);
		UserGroupAssociationTransfer userGroupAssociationTrans = (UserGroupAssociationTransfer) trans;
		userGroupAssociationTrans.setGroupId(associatedGroupId);

	}

	@Override
	public Integer getAssociatedComponentIdNum() {
		return associatedGroupId;
	}

	@Override
	public String getAssociatedComponentIdString() {
		return null;
	}

	@Override
	public Integer getEnvironmentId() {
		return null;
	}

	@Override
	public void store() throws SAFRException, DAOException {

	}

}
