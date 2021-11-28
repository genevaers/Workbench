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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.genevaers.ccb2lr.Copybook2LR;

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


public class TestImportCopybook extends TestImport {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.utilities.importer.TestImportCopybook");

    TestDataLayerHelper helper = new TestDataLayerHelper();

    Integer nextId = null;

    public void setUp() {
    }

    public void tearDown() {
        helper.closeDataLayer();
    }

    public void testImportCopybook() throws SAFRException, UnsupportedEncodingException {

//        helper.initDataLayer(101);
//
//        ImportUtility importUtility = new ImportUtility(null, null, null);
//
//        EnvironmentQueryBean envQB_set = getEnvQB (101);
//
//        importUtility.setTargetEnvironment(envQB_set);
//
//        ComponentType lr = ComponentType.LogicalRecord;
//
//        importUtility.setComponentType(lr);
//
//        String XMLfile = "data/testImportMetadata_LR[1360].xml";
//
//        URL fp_url = TestImportCopybook.class.getResource(XMLfile);
//
//        File fp_file = new File(URLDecoder.decode(fp_url.getFile(), "UTF-8"));
//        String fp = fp_file.getAbsolutePath();
//
//        File importpath = new File(fp);
//
//        ImportFile importFile = new ImportFile(importpath);
        
    	CopybookImporter ci = new CopybookImporter();
        ci.importCopybook("test/resources/simple.cb");
//
//        List<ImportFile> iF_list_set = new ArrayList<ImportFile>();
//
//        iF_list_set.add(0, importFile);
//
//        importUtility.setFiles(iF_list_set);
//
//        importUtility.importMetadata();
//
//        LogicalRecord lr_import = SAFRApplication.getSAFRFactory().getLogicalRecord(1360);
//
//        // assertEquals (lr_import.getName(),"testImportMetadata_LR");
//        // assertEquals
//        // (lr_import.getComment(),"testImportMetadata Logical Record");
//
//        /* Logical File Association */
//        SAFRList<ComponentAssociation> lr_list = lr_import.getLogicalFileAssociations();
//        ComponentAssociation lr_fileAss = lr_list.get(0);
//
//        /* Physical File Association */
//        LogicalFile lf_import = SAFRApplication.getSAFRFactory().getLogicalFile(
//                lr_fileAss.getAssociatedComponentIdNum());
//
//        SAFRList<FileAssociation> lf_list = lf_import.getPhysicalFileAssociations();
//        FileAssociation lf_fileAss = lf_list.get(0);
//
//        /* clean up environment */
//        SAFRApplication.getSAFRFactory().removeLogicalRecord(lr_import.getId());
//        SAFRApplication.getSAFRFactory().removeLogicalFile(lr_fileAss.getAssociatedComponentIdNum());
//        SAFRApplication.getSAFRFactory().removePhysicalFile(lf_fileAss.getAssociatedComponentIdNum());
//
//        assertEquals(lr_import.getName(), "testImportMetadata_LR");
//        assertEquals(lr_import.getComment(), "testImportMetadata Logical Record");
    }

}
