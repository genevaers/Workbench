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


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.utilities.ConfirmWarningStrategy;
import com.ibm.safr.we.model.utilities.MockConfirmWarningStrategy;

public class TestImportUtility extends TestImport {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.utilities.importer.TestImportUtility");

    TestDataLayerHelper helper = new TestDataLayerHelper();

    Integer nextId = null;

    public void setUp() {
    }

    public void tearDown() {
        helper.closeDataLayer();
    }

    public void testGetSetTargetEnvironment() throws SAFRException {

        nextId = null;
        helper.initDataLayer();
        ImportUtility importUtility = new ImportUtility(null, null, null);

        Environment env = null;
        env = SAFRApplication.getSAFRFactory().getEnvironment(1);

        Integer env_id = env.getId();
        String env_name = env.getName();
        Date env_ctime = env.getCreateTime();
        String env_cuser = env.getCreateBy();
        Date env_modtime = env.getModifyTime();
        String env_moduser = env.getModifyBy();

        EnvironmentQueryBean envQB_set = new EnvironmentQueryBean(env_id, env_name, true, env_ctime,
                env_cuser, env_modtime, env_moduser);

        importUtility.setTargetEnvironment(envQB_set);
        EnvironmentQueryBean envQB_get = importUtility.getTargetEnvironment();
        // Integer envid_int = (envB.getId());

        // String envID = envid_int.toString();
        assertEquals(envQB_get.getId(), env_id);
        assertEquals(envQB_get.getName(), env_name);
        assertEquals(envQB_get.getCreateTime(), env_ctime);
        assertEquals(envQB_get.getCreateBy(), env_cuser);
        assertEquals(envQB_get.getModifyTime(), env_modtime);
        assertEquals(envQB_get.getModifyBy(), env_moduser);

    }

    public void testGetSetComponentType() {

        ImportUtility importUtility = new ImportUtility(null, null, null);

        ComponentType pf = ComponentType.PhysicalFile;
        ComponentType lr = ComponentType.LogicalRecord;
        ComponentType lp = ComponentType.LookupPath;
        ComponentType v = ComponentType.View;
        ComponentType lf = ComponentType.LogicalFile;

        importUtility.setComponentType(pf);
        ComponentType pf_Get = importUtility.getComponentType();
        assertEquals(pf_Get.name(), pf.name());

        importUtility.setComponentType(lr);
        ComponentType lr_Get = importUtility.getComponentType();
        assertEquals(lr_Get.name(), lr.name());

        importUtility.setComponentType(lp);
        ComponentType lp_Get = importUtility.getComponentType();
        assertEquals(lp_Get.name(), lp.name());

        importUtility.setComponentType(v);
        ComponentType v_Get = importUtility.getComponentType();
        assertEquals(v_Get.name(), v.name());

        importUtility.setComponentType(lf);
        ComponentType lf_Get = importUtility.getComponentType();
        assertEquals(lf_Get.name(), lf.name());

    }

    public void testGetSetFiles() {

        ImportUtility importUtility = new ImportUtility(null, null, null);

        // ImportFile importFile(importpath);

        String fp = "C:\\BuildForge\\WEFT_Export\\UC24\\PhysicalFiles\\UC24_PF[310].xml";

        File importpath = new File(fp);

        ImportFile importFile = new ImportFile(importpath);

        List<ImportFile> iF_list_set = new ArrayList<ImportFile>();

        iF_list_set.add(0, importFile);

        // iF_list_set.add(1, importFile);
        importUtility.setFiles(iF_list_set);

        List<ImportFile> if_list_get = importUtility.getFiles();
        ImportFile importFile_get = if_list_get.get(0);
        // System.out.println(importFile_get.getName());

        assertEquals(importFile_get.getName(), importFile.getName());

    }

    public void testImportPhysicalFile() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        ImportUtility importUtility = new ImportUtility(null, null, null);

        Environment env = null;
        env = SAFRApplication.getSAFRFactory().getEnvironment(101);

        Integer env_id = env.getId();
        String env_name = env.getName();
        Date env_ctime = env.getCreateTime();
        String env_cuser = env.getCreateBy();
        Date env_modtime = env.getModifyTime();
        String env_moduser = env.getModifyBy();

        EnvironmentQueryBean envQB_set = new EnvironmentQueryBean(env_id, env_name, true, env_ctime,
                env_cuser, env_modtime, env_moduser);

        importUtility.setTargetEnvironment(envQB_set);

        ComponentType pf = ComponentType.PhysicalFile;

        importUtility.setComponentType(pf);

        String XMLfile = "data/testImportMetadata_PF[8416].xml";

        URL fp_url = TestImportUtility.class.getResource(XMLfile);

        File fp_file = new File(URLDecoder.decode(fp_url.getFile(), "UTF-8"));
        String fp = fp_file.getAbsolutePath();

        File importpath = new File(fp);

        ImportFile importFile = new ImportFile(importpath);

        List<ImportFile> iF_list_set = new ArrayList<ImportFile>();

        iF_list_set.add(0, importFile);

        importUtility.setFiles(iF_list_set);

        importUtility.importMetadata();

        PhysicalFile pf_import = SAFRApplication.getSAFRFactory().getPhysicalFile(8416);

        // assertEquals (pf_import.getName(),"testImportMetadata_PF");
        // assertEquals
        // (pf_import.getComment(),"testImportMetadata Physical File");

        /* clean up environment */
        SAFRApplication.getSAFRFactory().removePhysicalFile(pf_import.getId());

        assertEquals(pf_import.getName(), "testImportMetadata_PF");
        assertEquals(pf_import.getComment(), "testImportMetadata Physical File");
    }

    public void testGetSetConfirmWarningStrategy() {

        MockConfirmWarningStrategy import_accept = new MockConfirmWarningStrategy(true);

        ImportUtility importUtility = new ImportUtility(null, null, null);

        importUtility.setConfirmWarningStrategy(import_accept);

        ConfirmWarningStrategy import_cws = importUtility.getConfirmWarningStrategy();

        String topic = null;
        String message = null;

        assertEquals(import_cws.confirmWarning(topic, message), true);

    }
   
}
