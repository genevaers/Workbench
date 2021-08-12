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
import com.ibm.safr.we.model.view.ViewSource;

public class TestImportView extends TestImport {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.utilities.importer.TestImportView");

    TestDataLayerHelper helper = new TestDataLayerHelper();

    Integer nextId = null;

    public void setUp() {
    }

    public void tearDown() {
        helper.closeDataLayer();
    }

    public void testImportView() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        MockConfirmWarningStrategy import_accept = new MockConfirmWarningStrategy(true);

        ImportUtility importUtility = new ImportUtility(null, null, null);

        importUtility.setConfirmWarningStrategy(import_accept);

        EnvironmentQueryBean envQBTarg = getEnvQB(101);

        importUtility.setTargetEnvironment(envQBTarg);

        ComponentType v = ComponentType.View;

        importUtility.setComponentType(v);

        String XMLfile = "data/testImportMetadata_V[8551].xml";

        URL fp_url = TestImportView.class.getResource(XMLfile);

        File fp_file = new File(URLDecoder.decode(fp_url.getFile(), "UTF-8"));
        String fp = fp_file.getAbsolutePath();

        File importpath = new File(fp);

        ImportFile importFile = new ImportFile(importpath);

        List<ImportFile> iF_list_set = new ArrayList<ImportFile>();

        iF_list_set.add(0, importFile);

        importUtility.setFiles(iF_list_set);

        importUtility.importMetadata();

        View v_import = SAFRApplication.getSAFRFactory().getView(8551);

        /* Logical Record Association */
        SAFRList<ViewSource> v_list = v_import.getViewSources();
        ViewSource v_sourceAssoc = v_list.get(0);
        ComponentAssociation v_lrAssoc = v_sourceAssoc.getLrFileAssociation();

        /* Logical File Association */
        LogicalRecord lr_import = SAFRApplication.getSAFRFactory().getLogicalRecord(
                v_lrAssoc.getAssociatingComponentId());
        SAFRList<ComponentAssociation> lr_list = lr_import.getLogicalFileAssociations();
        ComponentAssociation lr_fileAss = lr_list.get(0);

        /* Physical File Association */
        LogicalFile lf_import = SAFRApplication.getSAFRFactory().getLogicalFile(
                lr_fileAss.getAssociatedComponentIdNum());

        SAFRList<FileAssociation> lf_list = lf_import.getPhysicalFileAssociations();
        FileAssociation lf_fileAss = lf_list.get(0);

        /* clean up environment */

        SAFRApplication.getSAFRFactory().removeView(v_import.getId());
        SAFRApplication.getSAFRFactory().removeLogicalRecord(lr_import.getId());
        SAFRApplication.getSAFRFactory().removeLogicalFile(lr_fileAss.getAssociatedComponentIdNum());
        SAFRApplication.getSAFRFactory().removePhysicalFile(lf_fileAss.getAssociatedComponentIdNum());

        assertEquals(v_import.getName(), "testImportMetadata_V");
        assertEquals(v_import.getComment(), "testImportMetadata View");

    }
  
    public void testImportLargeView() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/Profit_And_Loss_Allocations_View[2725].xml";
        URL fpUrl = TestImportView.class.getResource(XMLfile);
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

        // import it again
        importUtility = new ImportUtility(envQBTarg, ComponentType.View,  
                iFListSet);
        importUtility.setConfirmWarningStrategy(mockStrat);        
        importUtility.importMetadata();
        
        // reset import environment
        
        SAFRApplication.getSAFRFactory().clearEnvironment(101);
        
        // Migrate view folder 
        List<ViewFolderQueryBean> vfList = SAFRQuery.queryAllViewFolders(113, SortType.SORT_BY_NAME);
        ViewFolderQueryBean vfMig = vfList.get(0);        
        Migration migration = new Migration(getEnvQB(113), envQBTarg, ComponentType.ViewFolder, 
                vfMig, false);        
        migration.setConfirmWarningStrategy(mockStrat);
        migration.migrate();        
    }

    public void testImportViewSourceRemoved() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        // reset next key ids
        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/MultiSourceO[1].xml";
        URL fpUrl = TestImportView.class.getResource(XMLfile);
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

        // import again with view source removed
        URL fpUrl2 = TestImportView.class.getResource("data/MultiSourceM[1].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.View, 
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

    public void testImportViewSortKeyRemoved() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        // reset next key ids
        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/MultiSortKeyO[1].xml";
        URL fpUrl = TestImportView.class.getResource(XMLfile);
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

        // import again with view source removed
        URL fpUrl2 = TestImportView.class.getResource("data/MultiSortKeyM[1].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.View,  
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

    public void testImportViewHeaderFooterRemoved() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        // reset next key ids
        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/HeaderFooterO[1].xml";
        URL fpUrl = TestImportView.class.getResource(XMLfile);
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

        // import again with view source removed
        URL fpUrl2 = TestImportView.class.getResource("data/HeaderFooterM[1].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.View,  
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
    
    public void testImportViewColumnRemoved() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        // reset next key ids
        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/MultiSourceO[1].xml";
        URL fpUrl = TestImportView.class.getResource(XMLfile);
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

        // import again with view source removed
        URL fpUrl2 = TestImportView.class.getResource("data/MultiSourceC[1].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.View,  
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
    
    public void testImportLRLFDepLU() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        
        // Set files to import
        String XMLfile = "data/AssDepend[1].xml";
        URL fpUrl = TestImportView.class.getResource(XMLfile);
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
        
        // activate the view
        View vw = SAFRApplication.getSAFRFactory().getView(1);
        vw.activate();
        vw.store();

        // import a dependent LR
        URL fpUrl2 = TestImportView.class.getResource("data/AssDependLRLU[10].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.LogicalRecord,  
                iFListSet2);
        importUtility.setConfirmWarningStrategy(mockStrat);        
        importUtility.importMetadata();
        String msg = importFile2.getErrors().get(0);
        String pattern = "(?s)Logical File: SimpleLFLU\\[20\\].*LOOKUP PATHS :.*SimpleLU \\[2\\].*$";
        assertTrue(msg.matches(pattern));
        String msg2 = importFile2.getErrors().get(1);
        String pattern2 = "(?s)^File 'AssDependLRLU\\[10\\]\\.xml' contains LR-LF record \\[23\\] which associates LR \\[10\\] and LF \\[200\\]\\. " +
        		"LRLF association \\[23\\] already exists in the target environment, but it associates LR \\[10\\] and LF \\[20\\]\\. " +
        		"This association cannot be replaced with the imported details due to following dependencies\\..*$";
        assertTrue(msg2.matches(pattern2));
                
        // reset import environment
        SAFRApplication.getSAFRFactory().clearEnvironment(101);
        
        /* Migrate view folder */
        List<ViewFolderQueryBean> vfList = SAFRQuery.queryAllViewFolders(113, SortType.SORT_BY_NAME);
        ViewFolderQueryBean vfMig = vfList.get(0);        
        Migration migration = new Migration(getEnvQB(113), envQBTarg, ComponentType.ViewFolder, 
                vfMig, false);        
        migration.setConfirmWarningStrategy(mockStrat);
        migration.migrate();        
    }

    public void testImportLRLFDepView() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        MockConfirmWarningStrategy mockStrat = new MockConfirmWarningStrategy(true);
        
        // Set files to import
        String XMLfile = "data/AssDepend[1].xml";
        URL fpUrl = TestImportView.class.getResource(XMLfile);
        File fpFile = new File(URLDecoder.decode(fpUrl.getFile(), "UTF-8"));
        String fp = fpFile.getAbsolutePath();
        File importpath = new File(fp);
        ImportFile importFile = new ImportFile(importpath);
        List<ImportFile> iFListSet = new ArrayList<ImportFile>();
        iFListSet.add(0, importFile);
        
        ImportUtility importUtility = new ImportUtility(envQBTarg, ComponentType.View,  
                iFListSet); 
        importUtility.setConfirmWarningStrategy(mockStrat);
        importUtility.importMetadata();
        
        // activate the view
        View vw = SAFRApplication.getSAFRFactory().getView(1);
        vw.activate();
        vw.store();

        // import a dependent LR
        URL fpUrl2 = TestImportView.class.getResource("data/AssDependLRLU[1].xml");
        File fpFile2 = new File(URLDecoder.decode(fpUrl2.getFile(), "UTF-8"));
        File importpath2 = new File(fpFile2.getAbsolutePath());
        ImportFile importFile2 = new ImportFile(importpath2);
        List<ImportFile> iFListSet2 = new ArrayList<ImportFile>();
        iFListSet2.add(0, importFile2);        
        importUtility = new ImportUtility(envQBTarg, ComponentType.LogicalRecord,  
                iFListSet2);
        importUtility.setConfirmWarningStrategy(mockStrat);        
        importUtility.importMetadata();
        String msg = importFile2.getErrors().get(0);
        String pattern = "(?s)Logical File: SimpleLF\\[1\\].*VIEWS :.*AssDepend \\[1\\].*$";
        assertTrue(msg.matches(pattern));
        String msg2 = importFile2.getErrors().get(1);
        String pattern2 = "(?s)^File 'AssDependLRLU\\[1\\]\\.xml' contains LR-LF record \\[1\\] which associates LR \\[1\\] and LF \\[10\\]\\. " +
                "LRLF association \\[1\\] already exists in the target environment, but it associates LR \\[1\\] and LF \\[1\\]\\. " +
                "This association cannot be replaced with the imported details due to following dependencies\\..*$";
        assertTrue(msg2.matches(pattern2));
        
        // reset import environment
        SAFRApplication.getSAFRFactory().clearEnvironment(101);
        
        /* Migrate view folder */
        List<ViewFolderQueryBean> vfList = SAFRQuery.queryAllViewFolders(113, SortType.SORT_BY_NAME);
        ViewFolderQueryBean vfMig = vfList.get(0);        
        Migration migration = new Migration(getEnvQB(113), envQBTarg, ComponentType.ViewFolder, 
                vfMig, false);        
        migration.setConfirmWarningStrategy(mockStrat);
        migration.migrate();        
    }
    
    public void testImportCQ9806() throws UnsupportedEncodingException, SAFRException {

        helper.initDataLayer(101);

        MockConfirmWarningStrategy import_accept = new MockConfirmWarningStrategy(true);
        ImportUtility importUtility = new ImportUtility(null, null, null);
        importUtility.setConfirmWarningStrategy(import_accept);
        importUtility.setComponentType(ComponentType.View);

        // set target env
        EnvironmentQueryBean envQBTarg = getEnvQB(101);
        importUtility.setTargetEnvironment(envQBTarg);

        // set import files
        String XMLfile = "data/CQ9806[587].xml";
        URL fp_url = TestImportView.class.getResource(XMLfile);
        File fp_file = new File(URLDecoder.decode(fp_url.getFile(), "UTF-8"));
        String fp = fp_file.getAbsolutePath();
        File importpath = new File(fp);
        ImportFile importFile = new ImportFile(importpath);
        List<ImportFile> iF_list_set = new ArrayList<ImportFile>();
        iF_list_set.add(0, importFile);
        importUtility.setFiles(iF_list_set);

        // attempt import
        importUtility.importMetadata();
        ImportFile import_cfile = importUtility.getCurrentFile();
        String emsg = import_cfile.getErrorMsg();
        emsg = emsg.trim();
        assertEquals(emsg, "View Source[614] refers to LR-LF association with id [95] but this is not in the import file.");

        helper.closeDataLayer();
    }
    
}
