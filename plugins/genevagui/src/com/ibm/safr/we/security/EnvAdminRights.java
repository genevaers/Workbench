package com.ibm.safr.we.security;

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


import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.Permissions;

public class EnvAdminRights extends EnvRoleRights {

    public EnvAdminRights() {
        getPermissions().add(Permissions.CreateLogicalFile);
        getPermissions().add(Permissions.CreateLogicalRecord);
        getPermissions().add(Permissions.CreateLookupPath);
        getPermissions().add(Permissions.CreatePhysicalFile);
        getPermissions().add(Permissions.CreateUserExitRoutine);
        getPermissions().add(Permissions.CreateView);
        getPermissions().add(Permissions.CreateViewFolder);
        getPermissions().add(Permissions.MigrateIn);
    }
        
    public EditRights getComponentRight(ComponentType type) {
        if (type.equals(ComponentType.User) ||
            type.equals(ComponentType.Group)) {
            return EditRights.None;
        }
        else if (type.equals(ComponentType.Environment)) {
            return EditRights.Read;
        }        
        else {
            return EditRights.ReadModifyDelete;
        }
    }

}
