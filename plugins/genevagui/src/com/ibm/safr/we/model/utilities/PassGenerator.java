package com.ibm.safr.we.model.utilities;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import org.genevaers.runcontrolgenerator.workbenchinterface.RCDriver;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.utilities.ProfileLocation;

public class PassGenerator {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.utilities.PassGenerator");

    public static void run(Collection<BatchComponent> batchViewComponents, ConfirmWarningStrategy warningStrategy)
            throws SAFRException {

        // This loop is used to take the list of batchViewcomponent objects and
        // activate the views, adds to the valid list and stored to the database
        // else is added to
        // the invalid list and stored to the database.
        String viewIDs = "";
        Integer envId;
        
        RCDriver.initialise();
        Iterator<BatchComponent> bi = batchViewComponents.iterator();
        
        EnvironmentalQueryBean compo = bi.next().getComponent();
        viewIDs += compo.getIdLabel();
        envId = compo.getEnvironmentId();
        while(bi.hasNext()) {
            viewIDs += "," + bi.next().getComponent().getIdLabel();
        }
        RCDriver.setDbviews(viewIDs);
        RCDriver.setInputType("POSTGRES");
        RCDriver.setEnvironmentID(envId.toString());
        RCDriver.setSchema(DAOFactoryHolder.getDAOFactory().getConnectionParameters().getSchema());
        RCDriver.setPort(DAOFactoryHolder.getDAOFactory().getConnectionParameters().getPort());
        RCDriver.setServer(DAOFactoryHolder.getDAOFactory().getConnectionParameters().getServer());
        RCDriver.setDatabase(DAOFactoryHolder.getDAOFactory().getConnectionParameters().getDatabase());
        Path reportPath  = Paths.get(SAFRPreferences.getSAFRPreferences().get(UserPreferencesNodes.REPORTS_PATH, ProfileLocation.getProfileLocation().getLocalProfile()));
        reportPath = reportPath.resolve("passes").resolve("WBTest");
        if(reportPath.toFile().exists() == false) {
            reportPath.toFile().mkdirs();
        }
        RCDriver.setOutputPath(reportPath);
        RCDriver.runRCG("");
     }
    
    public static void runFromXML(String exportPath, String textType) throws SAFRException {
        RCDriver.setInputType("WBXML");
        RCDriver.setOutputPath(Paths.get(exportPath));
        RCDriver.setRCATextType(textType);
        RCDriver.runRCG("");
    }
    
    public static void clearOutputDirectory(String dir) {
        RCDriver.clearOutputPath(Paths.get(dir));
    }
    
    public static String getReportHtmlFile() {
        return RCDriver.getRCAreportFileName();
    }
    
}
