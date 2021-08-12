package com.ibm.safr.we.data.transfer;

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


import com.ibm.safr.we.constants.EnvRole;

public class GroupEnvironmentAssociationTransfer extends AssociationTransfer {

    private EnvRole envRole = EnvRole.GUEST; 
	private Boolean physicalFileCreatePermissions = false;
	private Boolean logicalFileCreatePermissions = false;
	private Boolean logicalRecordCreatePermissions = false;
	private Boolean userExitRoutineCreatePermissions = false;
    private Boolean lookupPathCreatePermissions = false;
    private Boolean viewCreatePermissions = false;
	private Boolean viewFolderCreatePermissions = false;
	private Boolean migrateInPermissions = false;

	public EnvRole getEnvRole() {
        return envRole;
    }

    public void setEnvRole(EnvRole envRole) {
        this.envRole = envRole;
    }

    public Boolean hasPhysicalFileCreatePermissions() {
		return physicalFileCreatePermissions;
	}

	public void setPhysicalFileCreatePermissions(
			Boolean physicalFileCreateRights) {
		this.physicalFileCreatePermissions = physicalFileCreateRights;
	}

	public Boolean hasLogicalFileCreatePermissions() {
		return logicalFileCreatePermissions;
	}

	public void setLogicalFileCreatePermissions(Boolean logicalFileCreateRights) {
		this.logicalFileCreatePermissions = logicalFileCreateRights;
	}

	public Boolean hasLogicalRecordCreatePermissions() {
		return logicalRecordCreatePermissions;
	}

	public void setLogicalRecordCreatePermissions(
			Boolean logicalRecordCreateRights) {
		this.logicalRecordCreatePermissions = logicalRecordCreateRights;
	}

	public Boolean hasUserExitRoutineCreatePermissions() {
		return userExitRoutineCreatePermissions;
	}

	public void setUserExitRoutineCreatePermissions(
			Boolean userExitRoutineCreateRights) {
		this.userExitRoutineCreatePermissions = userExitRoutineCreateRights;
	}

	public Boolean hasViewFolderCreatePermissions() {
		return viewFolderCreatePermissions;
	}

	public void setViewFolderCreatePermissions(Boolean viewFolderCreateRights) {
		this.viewFolderCreatePermissions = viewFolderCreateRights;
	}

	public Boolean hasMigrateInPermissions() {
		return migrateInPermissions;
	}

	public void setMigrateInPermissions(Boolean migrateInRights) {
		this.migrateInPermissions = migrateInRights;
	}

    public Boolean hasLookupPathCreatePermissions() {
        return lookupPathCreatePermissions;
    }

    public void setLookupPathCreatePermissions(Boolean lookupPathCreatePermissions) {
        this.lookupPathCreatePermissions = lookupPathCreatePermissions;
    }

    public Boolean hasViewCreatePermissions() {
        return viewCreatePermissions;
    }

    public void setViewCreatePermissions(Boolean viewCreatePermissions) {
        this.viewCreatePermissions = viewCreatePermissions;
    }

	
}
