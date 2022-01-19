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


import java.util.List;

import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.associations.ViewFolderViewAssociation;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;

public class ViewFolderConverter {

    public static void main(String[] args) {
        SAFRApplication.getSAFRFactory().getAllCodeSets();      
        SAFRApplication.initDummyUserSession();
        System.out.println("Converting all View folder associations");
        convertViewFolders();
        System.out.println("Finished converting all View folder associations");
    }

    protected static void convertViewFolders() {
        // clear all existing associations
        DAOFactoryHolder.getDAOFactory().getViewFolderDAO().clearAssociations();
        
        // loop through all environments 
        List<EnvironmentQueryBean> envBeans = 
            DAOFactoryHolder.getDAOFactory().getEnvironmentDAO().queryAllEnvironments(SortType.SORT_BY_ID);
        for (EnvironmentQueryBean envBean : envBeans) {
            System.out.println("\tConverting environment " + envBean.getId());
            // loop through all View folders in environment 
            List<ViewFolderQueryBean> vfBeans = 
            DAOFactoryHolder.getDAOFactory().getViewFolderDAO().queryAllViewFolders(envBean.getId(), SortType.SORT_BY_ID);
            for (ViewFolderQueryBean vfBean : vfBeans) {
                System.out.println("\t\tConverting view folder " + vfBean.getId());
                convertViewFolder(vfBean);
            }
        }
    }

    protected static void convertViewFolder(ViewFolderQueryBean vfBean) {
        // query Views of the View folder
        List<ViewQueryBean> viewBeans = 
            DAOFactoryHolder.getDAOFactory().getViewDAO().queryAllViewsOld(SortType.SORT_BY_ID, vfBean.getEnvironmentId(), vfBean.getId());
        
        // grab the View folder
        ViewFolder viewFolder = SAFRApplication.getSAFRFactory().getViewFolder(vfBean.getId(), vfBean.getEnvironmentId());
        viewFolder.removeViewAssociations(viewFolder.getViewAssociations());
        
        // add associations to the View folder
        for (ViewQueryBean viewBean : viewBeans) {
            ViewFolderViewAssociation association = new ViewFolderViewAssociation(
                viewFolder, viewBean.getId(),
                viewBean.getName(), viewBean.getRights());
            viewFolder.addAssociatedView(association);
        }
        
        // store the view folder with its new associations
        viewFolder.store();
    }
    
}
