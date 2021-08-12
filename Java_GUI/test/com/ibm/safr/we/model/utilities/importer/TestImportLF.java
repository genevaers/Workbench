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
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.utilities.Migration;
import com.ibm.safr.we.model.utilities.MockConfirmWarningStrategy;
import com.ibm.safr.we.model.view.View;

public class TestImportLF extends TestImport {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.utilities.importer.TestImportLookup");

    TestDataLayerHelper helper = new TestDataLayerHelper();
    
    public void setUp() {
    }

    public void tearDown() {
        helper.closeDataLayer();
    }
    
    public void testImportLogicalFile() throws SAFRException, UnsupportedEncodingException {

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

        ComponentType lf = ComponentType.LogicalFile;

        importUtility.setComponentType(lf);

        String XMLfile = "data/testImportMetadata_LF[1357].xml";

        URL fp_url = TestImportUtility.class.getResource(XMLfile);

        File fp_file = new File(URLDecoder.decode(fp_url.getFile(), "UTF-8"));
        String fp = fp_file.getAbsolutePath();

        File importpath = new File(fp);

        ImportFile importFile = new ImportFile(importpath);

        List<ImportFile> iF_list_set = new ArrayList<ImportFile>();

        iF_list_set.add(0, importFile);

        importUtility.setFiles(iF_list_set);

        importUtility.importMetadata();

        LogicalFile lf_import = SAFRApplication.getSAFRFactory().getLogicalFile(1357);

        // assertEquals (lf_import.getName(),"testImportMetadata_LF");
        // assertEquals
        // (lf_import.getComment(),"testImportMetadata Logical File");

        SAFRList<FileAssociation> lf_list = lf_import.getPhysicalFileAssociations();
        FileAssociation LF_fileAss = lf_list.get(0);

        /* clean up environment */
        SAFRApplication.getSAFRFactory().removeLogicalFile(lf_import.getId());
        SAFRApplication.getSAFRFactory().removePhysicalFile(LF_fileAss.getAssociatedComponentIdNum());

        assertEquals(lf_import.getName(), "testImportMetadata_LF");
        assertEquals(lf_import.getComment(), "testImportMetadata Logical File");

    }

    public void testImportLFAssRemoved() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        // reset next key ids
        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/SimpleLFO[1].xml";
        URL fpUrl = TestImportLR.class.getResource(XMLfile);
        File fpFile = new File(URLDecoder.decode(fpUrl.getFile(), "UTF-8"));
        String fp = fpFile.getAbsolutePath();
        File importpath = new File(fp);
        ImportFile importFile = new ImportFile(importpath);
        List<ImportFile> iFListSet = new ArrayList<ImportFile>();
        iFListSet.add(0, importFile);

        // do the import
        MockConfirmWarningStrategy mockStrat = new MockConfirmWarningStrategy(true);
        
        ImportUtility importUtility = new ImportUtility(envQBTarg, ComponentType.LogicalFile,  
                iFListSet); 
        importUtility.setConfirmWarningStrategy(mockStrat);
        importUtility.importMetadata();
        
        // import again with field removed
        URL fpUrl2 = TestImportLR.class.getResource("data/SimpleLFM[1].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.LogicalFile, 
                iFListSet2);
        importUtility.setConfirmWarningStrategy(mockStrat);
        boolean pass = true;
        try {
            importUtility.importMetadata();
        } catch (Exception e) {
            e.printStackTrace();
            pass = false;
        }
        
        // reset import environment        
        SAFRApplication.getSAFRFactory().clearEnvironment(101);
        
        // Migrate view folder 
        List<ViewFolderQueryBean> vfList = SAFRQuery.queryAllViewFolders(113, SortType.SORT_BY_NAME);
        ViewFolderQueryBean vfMig = vfList.get(0);        
        Migration migration = new Migration(getEnvQB(113), envQBTarg, ComponentType.ViewFolder, 
                vfMig, false);        
        migration.setConfirmWarningStrategy(mockStrat);
        migration.migrate();
        assertTrue(pass);
    }

    public void testImportLFAssRemovedDep() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        // reset next key ids
        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/LFPFDep[1].xml";
        URL fpUrl = TestImportLR.class.getResource(XMLfile);
        File fpFile = new File(URLDecoder.decode(fpUrl.getFile(), "UTF-8"));
        String fp = fpFile.getAbsolutePath();
        File importpath = new File(fp);
        ImportFile importFile = new ImportFile(importpath);
        List<ImportFile> iFListSet = new ArrayList<ImportFile>();
        iFListSet.add(0, importFile);

        // do the import
        MockConfirmWarningStrategy mockStrat = new MockConfirmWarningStrategy(true);
        
        ImportUtility importUtility = new ImportUtility(envQBTarg, ComponentType.View,  
                iFListSet); 
        importUtility.setConfirmWarningStrategy(mockStrat);
        importUtility.importMetadata();
        
        // activate view
        View vw = SAFRApplication.getSAFRFactory().getView(1);
        vw.activate();
        vw.store();
        
        // import again with field removed
        URL fpUrl2 = TestImportLR.class.getResource("data/SimpleLFM[1].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.LogicalFile,  
                iFListSet2);
        importUtility.setConfirmWarningStrategy(mockStrat);
        boolean pass = true;
        try {
            importUtility.importMetadata();
        } catch (Exception e) {
            e.printStackTrace();
            pass = false;
        }

        String pattern1 = "(?s)^Physical File: SimplePFLU\\[20\\].*View:.*LFPFDep\\[1\\] - \\[View Properties\\].*$";
        assertTrue(mockStrat.getDetailMessage().matches(pattern1));
        String pattern2 = "(?s)^When Logical File 'SimpleLF\\[1\\]' is replaced on import, " +
        		"the following PF associations will be deleted as they are not included in the import data and the Views " +
        		"which reference them will become Inactive\\..*$";
        assertTrue(mockStrat.getShortMessage().matches(pattern2));
        
        // reset import environment        
        SAFRApplication.getSAFRFactory().clearEnvironment(101);
        
        // Migrate view folder 
        List<ViewFolderQueryBean> vfList = SAFRQuery.queryAllViewFolders(113, SortType.SORT_BY_NAME);
        ViewFolderQueryBean vfMig = vfList.get(0);        
        Migration migration = new Migration(getEnvQB(113), envQBTarg, ComponentType.ViewFolder, 
                vfMig, false);        
        migration.setConfirmWarningStrategy(mockStrat);
        migration.migrate();
        assertTrue(pass);
    }
    
    public void testImportLFAssRemovedDepCancel() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        // reset next key ids
        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/LFPFDep[1].xml";
        URL fpUrl = TestImportLR.class.getResource(XMLfile);
        File fpFile = new File(URLDecoder.decode(fpUrl.getFile(), "UTF-8"));
        String fp = fpFile.getAbsolutePath();
        File importpath = new File(fp);
        ImportFile importFile = new ImportFile(importpath);
        List<ImportFile> iFListSet = new ArrayList<ImportFile>();
        iFListSet.add(0, importFile);

        // do the import
        MockConfirmWarningStrategy mockStrat = new MockConfirmWarningStrategy(true);
        
        ImportUtility importUtility = new ImportUtility(envQBTarg, ComponentType.View,  
                iFListSet); 
        importUtility.setConfirmWarningStrategy(mockStrat);
        importUtility.importMetadata();
        
        // activate view
        View vw = SAFRApplication.getSAFRFactory().getView(1);
        vw.activate();
        vw.store();
        
        // import again with field removed
        URL fpUrl2 = TestImportLR.class.getResource("data/SimpleLFM[1].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.LogicalFile,  
                iFListSet2);
        mockStrat.setNumConfirm(1);
        mockStrat.reset();
        importUtility.setConfirmWarningStrategy(mockStrat);
        boolean pass = true;
        try {
            importUtility.importMetadata();
        } catch (Exception e) {
            e.printStackTrace();
            pass = false;
        }

        assertEquals(importFile2.getErrorMsg(), "Dependency error.");
        String mes1 = importFile2.getException().getErrorMessages().get(0);
        String pattern = "(?s)^Physical File: SimplePFLU\\[20\\].*View:.*LFPFDep\\[1\\] - \\[View Properties\\].*";
        assertTrue(mes1.matches(pattern));
        String mes2 = importFile2.getException().getErrorMessages().get(1);
        String pattern2 = "(?s)^Import cancelled on warning about View dependencies:.*" +
        		"When Logical File 'SimpleLF\\[1\\]' is replaced on import, the following PF.*" +
        		"associations will be deleted as they are not included in the import data.*" +
        		"and the Views which reference them will become Inactive\\..*$";
        assertTrue(mes2.matches(pattern2));
        
        // reset import environment        
        SAFRApplication.getSAFRFactory().clearEnvironment(101);
        
        // Migrate view folder 
        List<ViewFolderQueryBean> vfList = SAFRQuery.queryAllViewFolders(113, SortType.SORT_BY_NAME);
        ViewFolderQueryBean vfMig = vfList.get(0);        
        Migration migration = new Migration(getEnvQB(113), envQBTarg, ComponentType.ViewFolder, 
                vfMig, false);     
        mockStrat.setResult(true);        
        migration.setConfirmWarningStrategy(mockStrat);
        migration.migrate();
        assertTrue(pass);
    }

    
}
