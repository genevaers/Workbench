package com.ibm.safr.we.model.base;

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
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.transfer.SAFREnvironmentalComponentTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.SAFRApplication;

/**
 * This abstract class represents all SAFR metadata components which belong to a
 * specific environment.
 */
abstract public class SAFREnvironmentalComponent extends SAFRComponent {

	private Integer environmentId; // Set when object is created
	private Environment environment; // Lazily initialized
	
	//CQ9682 Migration
	private boolean forMigration = false;

	/**
	 * This ctor is called by subclasses to instantiate a new SAFR component for
	 * the specified environment ID which may later be persisted to the
	 * database.
	 * 
	 * @param environmentId
	 *            the ID of the owning environment. If the environment ID is
	 *            null then it throws a Null pointer Exception.
	 */
	protected SAFREnvironmentalComponent(Integer environmentId) {
		super();
		if (environmentId == null) {
			throw new NullPointerException("Environment ID cannot be null");
		}
		this.environmentId = environmentId;
		this.environment = null;
	}

	/**
	 * This ctor is called by subclasses to instantiate a new SAFR component for
	 * the specified Environment which may later be persisted to the database.
	 * 
	 * @param environment
	 *            the owning Environment.If the environment object is null then
	 *            it throws a Null pointer Exception.
	 */
	protected SAFREnvironmentalComponent(Environment environment) {
		super();
		if (environment == null) {
			throw new NullPointerException("Environment cannot be null");
		}
		this.environmentId = environment.getId();
		this.environment = environment;
	}

	/**
	 * This ctor is called by subclasses to instantiate an existing SAFR
	 * component from the database. The object's state is obtained from the
	 * specified component transfer object.
	 * 
	 * @param trans
	 *            the SAFREnvironmentalComponentTransfer object.If the
	 *            environment ID in the SAFREnvironmentalComponentTransfer
	 *            object is null then it throws a Null pointer Exception.
	 */
	protected SAFREnvironmentalComponent(
			SAFREnvironmentalComponentTransfer trans) {
		super(trans);
		if (trans.getEnvironmentId() == null) {
			throw new NullPointerException(
					"Environment ID is null in the database");
		}
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		SAFREnvironmentalComponentTransfer trans = (SAFREnvironmentalComponentTransfer) safrTrans;
		this.environmentId = trans.getEnvironmentId();
		if (environment != null
				&& environment.getId() != trans.getEnvironmentId()) {
			this.environment = null;
		}
	}

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		SAFREnvironmentalComponentTransfer trans = (SAFREnvironmentalComponentTransfer) safrTrans;
		trans.setEnvironmentId(environmentId);
		trans.setForMigration(forMigration);
	}

	/**
	 * A protected helper method for use by subclasses to get the Environment ID
	 * without the need to instantiate the Environment object.
	 * 
	 * @return the Environment ID string
	 */
	public Integer getEnvironmentId() {
		return this.environmentId;
	}

	/**
	 * This method is used to get the Environment to which this component
	 * belongs.
	 * 
	 * @return environment
	 * @throws SAFRException
	 */
	public Environment getEnvironment() throws SAFRException {
		if (environment == null) {
			if (environmentId != null) {
				this.environment = SAFRApplication.getSAFRFactory()
						.getEnvironment(environmentId);
			}
		}
		return environment;
	}

	// No setter method for environment is required.

	/**
	 * This method is used to check whether the current logged in user has
	 * specified permission.
	 * 
	 */
	protected boolean hasCreatePermission()
			throws SAFRException {
	    switch (getComponentType()) {
	    case ControlRecord:
             return SAFRApplication.getUserSession().isSystemAdminOrEnvAdmin(environmentId); 
        case UserExitRoutine:
            return SAFRApplication.getUserSession().hasPermission(Permissions.CreateUserExitRoutine, environmentId); 
        case PhysicalFile:
            return SAFRApplication.getUserSession().hasPermission(Permissions.CreatePhysicalFile, environmentId); 
        case LogicalFile:
            return SAFRApplication.getUserSession().hasPermission(Permissions.CreateLogicalFile, environmentId); 
        case LogicalRecord:
            return SAFRApplication.getUserSession().hasPermission(Permissions.CreateLogicalRecord, environmentId); 
        case LookupPath:
            return SAFRApplication.getUserSession().hasPermission(Permissions.CreateLookupPath, environmentId); 
        case View:
            return SAFRApplication.getUserSession().hasPermission(Permissions.CreateView, environmentId); 
        case ViewFolder:
            return SAFRApplication.getUserSession().hasPermission(Permissions.CreateViewFolder, environmentId); 
	    default:
	        return false;
	    }
	}

	/**
	 * This method is used to check whether the user has update rights on the
	 * specified component. 
	 * 
	 * @return true if the user has update rights on the component.
	 * @throws SAFRException
	 */
	protected boolean hasUpdateRights()
			throws SAFRException {
		if (SAFRApplication.getUserSession().getEditRights(getComponentType(), id, environmentId) == EditRights.ReadModify ||
		    SAFRApplication.getUserSession().getEditRights(getComponentType(), id, environmentId) == EditRights.ReadModifyDelete) {
			return true;
		}
		return false;
	}

    /**
     * This method is used to check whether the user has read rights on the
     * specified component. 
     * 
     */
    protected boolean hasReadRights()
            throws SAFRException {
        if (SAFRApplication.getUserSession().getEditRights(getComponentType(), id, environmentId) != EditRights.None) {
            return true;
        }
        return false;
    }
	
	/**
	 * To be used for migration to change the object's environment ID to the
	 * target environment and specify if the object already exists there.
	 * 
	 * @param targetEnvironmentId
	 * @param persistence
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

	@Override
    public boolean equals(Object rightObj) {
        if (rightObj instanceof SAFREnvironmentalComponent) {
            SAFREnvironmentalComponent rightComp = (SAFREnvironmentalComponent) rightObj;
            boolean equals = environmentId.equals(rightComp.environmentId) && id.equals(rightComp.id);
            return equals;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int res = Math.max(environmentId, id);
        res = (res << 16) | (res >>> 16);  
        res = res ^ Math.min(environmentId, id);
        return res;        
    }	
}
