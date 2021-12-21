package com.ibm.safr.we.constants;

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


import com.ibm.safr.we.security.EnvAdminRights;
import com.ibm.safr.we.security.EnvDeveloperRights;
import com.ibm.safr.we.security.EnvGuestRights;
import com.ibm.safr.we.security.EnvRoleRights;

public enum EnvRole {
    GUEST("Guest", "GUEST",new EnvGuestRights()),
    DEVELOPER("Developer", "DEVELOPER",new EnvDeveloperRights()),
    ADMIN("Administrator", "ADMIN",new EnvAdminRights());
  
    private String code;
    private String desc;
    private EnvRoleRights rights;
  
    EnvRole(String desc, String code, EnvRoleRights rights) {
        this.desc = desc;
        this.code = code;
        this.rights = rights;
    }

    public String getDesc() {
        return desc;
    }
          
    public String getCode() {
        return code;
    }
      
    public EnvRoleRights getRights() {
        return rights;
    }

    public static EnvRole getEnvRoleFromCode(String code) {
        if (code.equals(ADMIN.getCode())) {
            return ADMIN;
        }
        else if (code.equals(DEVELOPER.getCode())) {
            return DEVELOPER;
        }
        else {
            return GUEST;
        }
    }
  
    public String toString() {
        return code;
    }
  
};
