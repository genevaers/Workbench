package com.ibm.safr.we.model.utilities.importer;

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


import java.util.Date;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;

public class TestImport extends TestCase {
	
	protected Integer getGeneratedComponentKey(int id,ComponentType type) {
		return (id * 100) + type.ordinal();
	}
	
    protected EnvironmentQueryBean getEnvQB (int id) throws SAFRException{
        
        Environment env = null;
        env = SAFRApplication.getSAFRFactory().getEnvironment(id);
        
        Integer env_id = env.getId();
        String env_name = env.getName();
        Date env_ctime = env.getCreateTime();
        String env_cuser = env.getCreateBy();
        Date env_modtime = env.getModifyTime();
        String env_moduser = env.getModifyBy();
        
        EnvironmentQueryBean envQB_set = new EnvironmentQueryBean(env_id, env_name, true, env_ctime, env_cuser, env_modtime, env_moduser);
        
        return envQB_set;
    }

    protected ViewFolderQueryBean getFolderQB (int id) throws SAFRException{
        
        /* setup view folder */
        ViewFolder vf = null;
        vf = SAFRApplication.getSAFRFactory().getViewFolder(id);
        Integer venvId = (vf.getEnvironment().getId());
        Integer vfId = vf.getId();
        String vfName = vf.getName();
        Date vfCtime = vf.getCreateTime();
        String vfCuser = vf.getCreateBy();
        Date vfModtime = vf.getModifyTime();
        String vfModuser = vf.getModifyBy();
        ViewFolderQueryBean viewFolder = new ViewFolderQueryBean(venvId, vfId, vfName, null,
                vfCtime, vfCuser, vfModtime, vfModuser);
        
        return viewFolder;
    }
    
  

	
}
