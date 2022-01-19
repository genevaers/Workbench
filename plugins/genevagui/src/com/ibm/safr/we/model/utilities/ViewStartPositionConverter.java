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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;

public class ViewStartPositionConverter {

    public static void main(String[] args) {
        SAFRApplication.getSAFRFactory().getAllCodeSets();      
        SAFRApplication.initDummyUserSession();
        System.out.println("Converting all View column start positions");
        convertAllViewColumnPositions();
        System.out.println("Finished converting all View column start positions");
    }

    protected static void convertAllViewColumnPositions() {        
        // loop through all environments 
        List<EnvironmentQueryBean> envBeans = 
            DAOFactoryHolder.getDAOFactory().getEnvironmentDAO().queryAllEnvironments(SortType.SORT_BY_ID);
        for (EnvironmentQueryBean envBean : envBeans) {
            System.out.println("\tConverting environment " + envBean.getId());
            List<ViewQueryBean> vBeans = 
            DAOFactoryHolder.getDAOFactory().getViewDAO().queryAllViews(SortType.SORT_BY_ID, envBean.getId(), true);
            for (ViewQueryBean vBean : vBeans) {
                convertView(vBean);
            }
        }
    }

    protected static void convertView(ViewQueryBean vBean) {
        List<ViewSortKeyTransfer> sortkeys =
            DAOFactoryHolder.getDAOFactory().getViewSortKeyDAO().getViewSortKeys(vBean.getId(), vBean.getEnvironmentId());
                
        if (vBean.getOldOutputFormat().equals("FILE") && vBean.getOldType().equals("EXTR") && 
            sortkeys.size() > 0) {
            System.out.println("\t\tConverting view " + vBean.getId() + " column start positions");

            List<ViewColumnTransfer> cols = 
                DAOFactoryHolder.getDAOFactory().getViewColumnDAO().getViewColumns(vBean.getId(), vBean.getEnvironmentId());
                        
            // form set of sortkeys
            Set<Integer> sortKeyMap = new HashSet<Integer>();
            for (ViewSortKeyTransfer sortKey : sortkeys) {
                sortKeyMap.add(sortKey.getViewColumnId());
            }
            
            int prevStartPosition = 1;
            int prevLength = 0;
            for (ViewColumnTransfer col : cols) {
                if (sortKeyMap.contains(col.getId())) {
                    col.setStartPosition(0);
                }// if column is sort key
                else {
                    int currentStartPos = prevStartPosition + prevLength;
                    col.setStartPosition(currentStartPos);
                    prevStartPosition = currentStartPos;
                    prevLength = col.getLength().intValue();
                }                
            }
            
            DAOFactoryHolder.getDAOFactory().getViewColumnDAO().persistViewColumns(cols);            
        }
    }

    
}
