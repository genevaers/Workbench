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
import java.util.List;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.utilities.Migration;
import com.ibm.safr.we.model.utilities.MockConfirmWarningStrategy;
import com.ibm.safr.we.model.view.View;


public class TestImportLR extends TestImport {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.utilities.importer.TestImportLR");

    TestDataLayerHelper helper = new TestDataLayerHelper();

    Integer nextId = null;

    public void setUp() {
    }

    public void tearDown() {
        helper.closeDataLayer();
    }

    public void testImportLogicalRecord() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        ImportUtility importUtility = new ImportUtility(null, null, null);

        EnvironmentQueryBean envQB_set = getEnvQB (101);

        importUtility.setTargetEnvironment(envQB_set);

        ComponentType lr = ComponentType.LogicalRecord;

        importUtility.setComponentType(lr);

        String XMLfile = "data/testImportMetadata_LR[1360].xml";

        URL fp_url = TestImportLR.class.getResource(XMLfile);

        File fp_file = new File(URLDecoder.decode(fp_url.getFile(), "UTF-8"));
        String fp = fp_file.getAbsolutePath();

        File importpath = new File(fp);

        ImportFile importFile = new ImportFile(importpath);

        List<ImportFile> iF_list_set = new ArrayList<ImportFile>();

        iF_list_set.add(0, importFile);

        importUtility.setFiles(iF_list_set);

        importUtility.importMetadata();

        LogicalRecord lr_import = SAFRApplication.getSAFRFactory().getLogicalRecord(1360);

        // assertEquals (lr_import.getName(),"testImportMetadata_LR");
        // assertEquals
        // (lr_import.getComment(),"testImportMetadata Logical Record");

        /* Logical File Association */
        SAFRList<ComponentAssociation> lr_list = lr_import.getLogicalFileAssociations();
        ComponentAssociation lr_fileAss = lr_list.get(0);

        /* Physical File Association */
        LogicalFile lf_import = SAFRApplication.getSAFRFactory().getLogicalFile(
                lr_fileAss.getAssociatedComponentIdNum());

        SAFRList<FileAssociation> lf_list = lf_import.getPhysicalFileAssociations();
        FileAssociation lf_fileAss = lf_list.get(0);

        /* clean up environment */
        SAFRApplication.getSAFRFactory().removeLogicalRecord(lr_import.getId());
        SAFRApplication.getSAFRFactory().removeLogicalFile(lr_fileAss.getAssociatedComponentIdNum());
        SAFRApplication.getSAFRFactory().removePhysicalFile(lf_fileAss.getAssociatedComponentIdNum());

        assertEquals(lr_import.getName(), "testImportMetadata_LR");
        assertEquals(lr_import.getComment(), "testImportMetadata Logical Record");
    }

    public void testImportLRFieldRemoved() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/RemoveFieldO[1].xml";
        URL fpUrl = TestImportLR.class.getResource(XMLfile);
        File fpFile = new File(URLDecoder.decode(fpUrl.getFile(), "UTF-8"));
        String fp = fpFile.getAbsolutePath();
        File importpath = new File(fp);
        ImportFile importFile = new ImportFile(importpath);
        List<ImportFile> iFListSet = new ArrayList<ImportFile>();
        iFListSet.add(0, importFile);

        // do the import
        MockConfirmWarningStrategy mockStrat = new MockConfirmWarningStrategy(true);
        
        ImportUtility importUtility = new ImportUtility(envQBTarg, ComponentType.LogicalRecord,  
                iFListSet); 
        importUtility.setConfirmWarningStrategy(mockStrat);
        importUtility.importMetadata();

        // import again with field removed
        URL fpUrl2 = TestImportLR.class.getResource("data/RemoveFieldM[1].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.LogicalRecord,  
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

    public void testImportLRFieldRemovedDep() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        // reset next key ids
        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/RemoveFieldView[8672].xml";
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
        View vw = SAFRApplication.getSAFRFactory().getView(8672);
        vw.activate();
        vw.store();

        // import again with field removed
        URL fpUrl2 = TestImportLR.class.getResource("data/RemoveFieldM[1].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.LogicalRecord,  
                iFListSet2);
        importUtility.setConfirmWarningStrategy(mockStrat);
        boolean pass = true;
        try {
            importUtility.importMetadata();
        } catch (Exception e) {
            e.printStackTrace();
            pass = false;
        }
        
        String msg = importFile2.getErrors().get(0);
        String pattern = "(?s).*Field 'NAME\\[2\\]':.*VIEWS:.*->RemoveField\\[8672\\] - \\[Col 1, NAME, Column Source\\].*$";
        assertTrue(msg.matches(pattern));
        String msg2 = importFile2.getErrors().get(1);
        String pattern2 = "(?s)LR Field dependency error.*" +
        		"When Logical Record 'RemoveField\\[1\\]' is replaced on import, the following LR Fields should.*" +
        		"be deleted as they are not included in the import data, but they can't be deleted as they.*" +
        		"are referenced by existing Lookup Paths or Views\\..*$";
        assertTrue(msg2.matches(pattern2));
        
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

    public void testImportAssRemoved() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/RemoveAssO[1].xml";
        URL fpUrl = TestImportLR.class.getResource(XMLfile);
        File fpFile = new File(URLDecoder.decode(fpUrl.getFile(), "UTF-8"));
        String fp = fpFile.getAbsolutePath();
        File importpath = new File(fp);
        ImportFile importFile = new ImportFile(importpath);
        List<ImportFile> iFListSet = new ArrayList<ImportFile>();
        iFListSet.add(0, importFile);

        // do the import
        MockConfirmWarningStrategy mockStrat = new MockConfirmWarningStrategy(true);
        
        ImportUtility importUtility = new ImportUtility(envQBTarg, ComponentType.LogicalRecord,  
                iFListSet); 
        importUtility.setConfirmWarningStrategy(mockStrat);
        importUtility.importMetadata();

        // import again with field removed
        URL fpUrl2 = TestImportLR.class.getResource("data/RemoveAssM[1].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.LogicalRecord,  
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
 
    public void testImportAssRemovedDep() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/RemoveAssDep[8673].xml";
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

        // import again with field removed
        URL fpUrl2 = TestImportLR.class.getResource("data/RemoveAssM[1].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.LogicalRecord,  
                iFListSet2);
        importUtility.setConfirmWarningStrategy(mockStrat);
        boolean pass = true;
        try {
            importUtility.importMetadata();
        } catch (Exception e) {
            e.printStackTrace();
            pass = false;
        }
        
        String msg = importFile2.getErrors().get(0);
        String pattern = "(?s)Logical File: SimpleLF2\\[1537\\].*VIEWS :.*RemoveAssDep\\[8673\\].*$";
        assertTrue(msg.matches(pattern));
        String msg2 = importFile2.getErrors().get(1);
        String pattern2 = "(?s)Logical Record/Logical File dependency error\\..*" +
        		"When Logical Record 'RemoveAss\\[1\\]' is replaced on import, the following Logical File associations.*" +
        		"should be removed as they are not included in the import data, but they can't be removed as.*" +
        		"they are referenced by existing Lookup Paths or Views\\..*$";
        assertTrue(msg2.matches(pattern2));
        
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
     
}
