package com.ibm.safr.we.model.utilities;

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
import java.util.List;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.view.View;

public class TestMigrationBase extends TestCase {

    protected TestDataLayerHelper helper = new TestDataLayerHelper();
    
    protected void dbsetup (){
        helper.initDataLayer();
    }
    
    protected void tearDown () throws SAFRException {
        // reset import environment        
        SAFRApplication.getSAFRFactory().clearEnvironment(104);
        
        // Migrate view folder 
        List<ViewFolderQueryBean> vfList = SAFRQuery.queryAllViewFolders(113, SortType.SORT_BY_NAME);
        ViewFolderQueryBean vfDef = null;
        for (ViewFolderQueryBean vf : vfList) {
            if (vf.getId().equals(5)) {
                vfDef = vf;
            }
        }
        Migration migration = new Migration(getEnvQB(113), getEnvQB(104), ComponentType.ViewFolder, 
                vfDef, false);
        MockConfirmWarningStrategy mockStrat = new MockConfirmWarningStrategy(true);        
        migration.setConfirmWarningStrategy(mockStrat);
        migration.migrate();
        
        helper.closeDataLayer();
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

    protected ViewFolderQueryBean getFolderQB (int id, int env) throws SAFRException{
        
        /* setup view folder */
        ViewFolder vf = null;
        vf = SAFRApplication.getSAFRFactory().getViewFolder(id, env);
        String vfName = vf.getName();
        Date vfCtime = vf.getCreateTime();
        String vfCuser = vf.getCreateBy();
        Date vfModtime = vf.getModifyTime();
        String vfModuser = vf.getModifyBy();
        ViewFolderQueryBean viewFolder = new ViewFolderQueryBean(env, id, vfName, null,
                vfCtime, vfCuser, vfModtime, vfModuser);
        
        return viewFolder;
    }
    
    protected LogicalRecordQueryBean getLRQB(int id, int env) throws SAFRException {
        LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(id, env);
        
        String status = lr.getLRStatusCode().toString();
        String name = lr.getName();
        Date ctime = lr.getCreateTime();
        String cuser = lr.getCreateBy();
        Date modtime = lr.getModifyTime();
        String moduser = lr.getModifyBy();
        Date acttime = lr.getActivatedTime();
        String actuser = lr.getActivatedBy();
                
        LogicalRecordQueryBean qb = new LogicalRecordQueryBean(env, id, name, status, null, null, null, null, 
                ctime, cuser, modtime, moduser, acttime, actuser);
        return qb;
    }

    protected LogicalFileQueryBean getLFQB(int id, int env) throws SAFRException {
        LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(id, env);
        
        String name = lf.getName();
        Date ctime = lf.getCreateTime();
        String cuser = lf.getCreateBy();
        Date modtime = lf.getModifyTime();
        String moduser = lf.getModifyBy();
                
        LogicalFileQueryBean qb = new LogicalFileQueryBean(env, id, name, null, 
                ctime, cuser, modtime, moduser);
        return qb;
    }

    protected LookupQueryBean getLookupQB(int id, int env) throws SAFRException {
        LookupPath lu = SAFRApplication.getSAFRFactory().getLookupPath(id, env);
        
        String srcLR = lu.getLookupPathSteps().get(0).getSourceLR().getName();
        String name = lu.getName();
        Date ctime = lu.getCreateTime();
        String cuser = lu.getCreateBy();
        Date modtime = lu.getModifyTime();
        String moduser = lu.getModifyBy();
                
        LookupQueryBean qb = new LookupQueryBean(env, id, name, srcLR, 1, 0, null, null, null, 
                ctime, cuser, modtime, moduser,lu.getActivatedTime(),lu.getActivatedBy());
        return qb;
    }
    
    protected ViewQueryBean getViewQB(int id, int env) throws SAFRException {
        View vw = SAFRApplication.getSAFRFactory().getView(id, env);
        
        String name = vw.getName();
        String status = vw.getStatusCode().toString();
        String output = vw.getOutputFormat().toString();
        String type = vw.getOutputFormat().toString();
        Date ctime = vw.getCreateTime();
        String cuser = vw.getCreateBy();
        Date modtime = vw.getModifyTime();
        String moduser = vw.getModifyBy();
                        
        ViewQueryBean qb = new ViewQueryBean(env, id, name, status, output, type, null, 
                ctime, cuser, modtime, moduser, vw.getCompilerVersion(),vw.getActivatedTime(),vw.getActivatedBy());
        return qb;
    }
    
    protected PhysicalFileQueryBean getPFQB(int id, int env) throws SAFRException {
        PhysicalFile pf = SAFRApplication.getSAFRFactory().getPhysicalFile(id, env);
        
        String name = pf.getName();
        Date ctime = pf.getCreateTime();
        String cuser = pf.getCreateBy();
        Date modtime = pf.getModifyTime();
        String moduser = pf.getModifyBy();
                
       PhysicalFileQueryBean qb = new PhysicalFileQueryBean(env, id, name, null, null 
           , null, null, null, null, null, ctime, cuser, modtime, moduser);
        return qb;
    }
    
}
