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
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.LookupPathStep;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.utilities.MockConfirmWarningStrategy;

public class TestImportLookup extends TestImport {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.utilities.importer.TestImportLookup");

    TestDataLayerHelper helper = new TestDataLayerHelper();

    Integer nextId = null;

    public void setUp() {
    }

    public void tearDown() {
        helper.closeDataLayer();
    }

    public void testImportLookupPath() throws SAFRException, UnsupportedEncodingException {

        helper.initDataLayer(101);

        MockConfirmWarningStrategy import_accept = new MockConfirmWarningStrategy(true);

        ImportUtility importUtility = new ImportUtility(null, null, null);

        importUtility.setConfirmWarningStrategy(import_accept);

        EnvironmentQueryBean envQB_set = getEnvQB (101);

        importUtility.setTargetEnvironment(envQB_set);

        ComponentType lp = ComponentType.LookupPath;

        importUtility.setComponentType(lp);

        String XMLfile = "data/testImportMetadata_LP[2008].xml";

        URL fp_url = TestImportLookup.class.getResource(XMLfile);

        File fp_file = new File(URLDecoder.decode(fp_url.getFile(), "UTF-8"));
        String fp = fp_file.getAbsolutePath();

        File importpath = new File(fp);

        ImportFile importFile = new ImportFile(importpath);

        List<ImportFile> iF_list_set = new ArrayList<ImportFile>();

        iF_list_set.add(0, importFile);

        importUtility.setFiles(iF_list_set);

        importUtility.importMetadata();

        LookupPath lp_import = SAFRApplication.getSAFRFactory().getLookupPath(2008);

        /* Logical Record Association */

        SAFRList<LookupPathStep> lp_list = lp_import.getLookupPathSteps();
        LookupPathStep lp_pathstep = lp_list.get(0);

        /* Logical File Association */
        LogicalRecord lr_import = SAFRApplication.getSAFRFactory().getLogicalRecord(lp_pathstep.getSourceLRId());
        SAFRList<ComponentAssociation> lr_list = lr_import.getLogicalFileAssociations();
        ComponentAssociation lr_fileAss = lr_list.get(0);

        /* Physical File Association */
        LogicalFile lf_import = SAFRApplication.getSAFRFactory().getLogicalFile(
                lr_fileAss.getAssociatedComponentIdNum());

        SAFRList<FileAssociation> lf_list = lf_import.getPhysicalFileAssociations();
        FileAssociation lf_fileAss = lf_list.get(0);

        /* clean up environment */

        SAFRApplication.getSAFRFactory().removeLookupPath(lp_import.getId());
        SAFRApplication.getSAFRFactory().removeLogicalRecord(lr_import.getId());
        SAFRApplication.getSAFRFactory().removeLogicalFile(lr_fileAss.getAssociatedComponentIdNum());
        SAFRApplication.getSAFRFactory().removePhysicalFile(lf_fileAss.getAssociatedComponentIdNum());

        assertEquals(lp_import.getName(), "testImportMetadata_LP");
        assertEquals(lp_import.getComment(), "testImportMetadata Lookup Paths");

    }

    public void testImportCQ9762() throws UnsupportedEncodingException, SAFRException {

        helper.initDataLayer(101);

        MockConfirmWarningStrategy import_accept = new MockConfirmWarningStrategy(true);

        ImportUtility importUtility = new ImportUtility(null, null, null);

        importUtility.setConfirmWarningStrategy(import_accept);

        EnvironmentQueryBean envQB_set = getEnvQB (101);

        importUtility.setTargetEnvironment(envQB_set);

        ComponentType lp = ComponentType.LookupPath;

        importUtility.setComponentType(lp);

        String XMLfile = "data/CQ9762[2009].xml";

        URL fp_url = TestImportLookup.class.getResource(XMLfile);

        File fp_file = new File(URLDecoder.decode(fp_url.getFile(), "UTF-8"));
        String fp = fp_file.getAbsolutePath();

        File importpath = new File(fp);

        ImportFile importFile = new ImportFile(importpath);

        List<ImportFile> iF_list_set = new ArrayList<ImportFile>();

        iF_list_set.add(0, importFile);

        importUtility.setFiles(iF_list_set);

        importUtility.importMetadata();
        ImportFile import_cfile = importUtility.getCurrentFile();

        String emsg = import_cfile.getErrorMsg();
        emsg = emsg.trim();
        // System.out.print(emsg);
        assertEquals("Lookup Path 'CQ9762 [2009]' ", emsg.substring(0, emsg.indexOf(":")));
        assertEquals("Step 1", emsg.substring(emsg.indexOf(":") + 5, emsg.indexOf(":") + 11));
        assertEquals("Field 1: Constant source value cannot be empty", emsg.substring(emsg.lastIndexOf(":") - 7));
    }   
}
