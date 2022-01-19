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


import java.util.ArrayList;
import java.util.List;

import com.ibm.safr.we.constants.EnvRole;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.GroupEnvironmentAssociationTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.query.GroupQueryBean;

//JAK: use this for GR-ENV associations

public class GroupEnvironmentAssociation extends Association {

    private EnvRole envRole;
	private List<Permissions> permissions;

	// Use this ctor for creating a new association
    public GroupEnvironmentAssociation(SAFRComponent associatingComponent,
			String associatedComponentName, Integer associatedComponentID) {
		super(associatingComponent, associatedComponentName,
				associatedComponentID, SAFRPersistence.NEW);
		permissions = new ArrayList<Permissions>();
	}

	// Use this ctor for instantiating an existing association
    public GroupEnvironmentAssociation(
			GroupEnvironmentAssociationTransfer trans,
			SAFRComponent associatingComponent) {
		super(trans, associatingComponent);
	}

	public GroupEnvironmentAssociation(GroupEnvironmentAssociationTransfer trans) {
		super(trans);
	}

	public GroupEnvironmentAssociation(GroupQueryBean associatingComponent,
			String environmentName, Integer environmentId) {
		super(environmentId, associatingComponent.getId(), associatingComponent
				.getName(), environmentName);
		permissions = new ArrayList<Permissions>();
	}

	@Override
	public Integer getAssociatedComponentIdNum() {
		return getEnvironmentId();
	}

	@Override
	public String getAssociatedComponentIdString() {
		return getAssociatedComponentIdNum().toString();
	}

    public EnvRole getEnvRole() {
        return envRole;
    }

    public void setEnvRole(EnvRole envRole) {
        this.envRole = envRole;
    }

	
	/**
	 * Adds specified permission to the GroupEnvironmentAssociation.
	 * 
	 * @param permission
	 *            : Enum {@link Permissions} to be set to the
	 *            GroupEnvironmentAssociation.
	 */
	public void addPermission(Permissions permission) {
		if (!permissions.contains(permission)) {
			permissions.add(permission);
			markModified();
		}
	}

	/**
	 * Remove specified permission from the GroupEnvironmentAssociation.
	 * 
	 * @param permission
	 *            : Enum {@link Permissions} to be removed from the
	 *            GroupEnvironmentAssociation.
	 */
	public void removePermission(Permissions permission) {
		if (permissions.contains(permission)) {
			permissions.remove(permission);
			markModified();
		}
	}

    public void removeAllPermissions() {
        if (!permissions.isEmpty()) {
            permissions.clear();
            markModified();
        }
    }
	
	public Boolean hasPermission(Permissions permission) {
		return permissions.contains(permission);
	}

	public Boolean canCreatePhysicalFile() {
		if (permissions.contains(Permissions.CreatePhysicalFile)) {
			return true;
		}
		return false;
	}

	public Boolean canCreateLogicalFile() {
		if (permissions.contains(Permissions.CreateLogicalFile)) {
			return true;
		}
		return false;
	}

	public Boolean canCreateLogicalRecord() {
		if (permissions.contains(Permissions.CreateLogicalRecord)) {
			return true;
		}
		return false;
	}

	public Boolean canCreateUserExitRoutine() {
		if (permissions.contains(Permissions.CreateUserExitRoutine)) {
			return true;
		}
		return false;
	}

    public Boolean canCreateLookupPath() {
        if (permissions.contains(Permissions.CreateLookupPath)) {
            return true;
        }
        return false;
    }
	
    public Boolean canCreateView() {
        if (permissions.contains(Permissions.CreateView)) {
            return true;
        }
        return false;
    }
	
	public Boolean canCreateViewFolder() {
		if (permissions.contains(Permissions.CreateViewFolder)) {
			return true;
		}
		return false;
	}

	public Boolean canMigrateIn() {
		if (permissions.contains(Permissions.MigrateIn)) {
			return true;
		}
		return false;
	}

	@Override
	protected void setObjectData(SAFRTransfer trans) {
		super.setObjectData(trans);
		permissions = new ArrayList<Permissions>();
		GroupEnvironmentAssociationTransfer envGrpAssociationTrans = (GroupEnvironmentAssociationTransfer) trans;
		setEnvRole(envGrpAssociationTrans.getEnvRole());
		if (envGrpAssociationTrans.hasPhysicalFileCreatePermissions()) {
			permissions.add(Permissions.CreatePhysicalFile);
		}
		if (envGrpAssociationTrans.hasLogicalFileCreatePermissions()) {
			permissions.add(Permissions.CreateLogicalFile);
		}
		if (envGrpAssociationTrans.hasLogicalRecordCreatePermissions()) {
			permissions.add(Permissions.CreateLogicalRecord);
		}
		if (envGrpAssociationTrans.hasUserExitRoutineCreatePermissions()) {
			permissions.add(Permissions.CreateUserExitRoutine);
		}
        if (envGrpAssociationTrans.hasLookupPathCreatePermissions()) {
            permissions.add(Permissions.CreateLookupPath);
        }
        if (envGrpAssociationTrans.hasViewCreatePermissions()) {
            permissions.add(Permissions.CreateView);
        }
		if (envGrpAssociationTrans.hasViewFolderCreatePermissions()) {
			permissions.add(Permissions.CreateViewFolder);
		}
		if (envGrpAssociationTrans.hasMigrateInPermissions()) {
			permissions.add(Permissions.MigrateIn);
		}

	}

	@Override
	protected void setTransferData(SAFRTransfer trans) {
		super.setTransferData(trans);
		GroupEnvironmentAssociationTransfer envGrpAssociationTrans = (GroupEnvironmentAssociationTransfer) trans;
		envGrpAssociationTrans.setEnvRole(envRole);
		if (permissions.contains(Permissions.CreatePhysicalFile)) {
			envGrpAssociationTrans.setPhysicalFileCreatePermissions(true);
		}
		if (permissions.contains(Permissions.CreateLogicalFile)) {
			envGrpAssociationTrans.setLogicalFileCreatePermissions(true);
		}
		if (permissions.contains(Permissions.CreateLogicalRecord)) {
			envGrpAssociationTrans.setLogicalRecordCreatePermissions(true);
		}
		if (permissions.contains(Permissions.CreateUserExitRoutine)) {
			envGrpAssociationTrans.setUserExitRoutineCreatePermissions(true);
		}
        if (permissions.contains(Permissions.CreateLookupPath)) {
            envGrpAssociationTrans.setLookupPathCreatePermissions(true);
        }
        if (permissions.contains(Permissions.CreateView)) {
            envGrpAssociationTrans.setViewCreatePermissions(true);
        }
		if (permissions.contains(Permissions.CreateViewFolder)) {
			envGrpAssociationTrans.setViewFolderCreatePermissions(true);
		}
		if (permissions.contains(Permissions.MigrateIn)) {
			envGrpAssociationTrans.setMigrateInPermissions(true);
		}
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
		if (!(obj instanceof GroupEnvironmentAssociation)) {
			return false;
		}
		GroupEnvironmentAssociation that = (GroupEnvironmentAssociation) obj;
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
		// TODO

	}

}
