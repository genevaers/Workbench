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


import java.util.ArrayList;
import java.util.List;

import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.FindTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.SAFRQuery;


public class ViewLogicConverter {
        
    public static void main(String [ ] args)    
    {
        SAFRApplication.getSAFRFactory().getAllCodeSets();      
        SAFRApplication.initDummyUserSession();

        System.out.println("Converting all View logic");
        ViewLogicConverter.convertViewLogic();
        System.out.println("Finished converting all View logic");
    }
    
    protected static void convertViewLogic() {
        List<ViewTransfer> views = SAFRQuery.queryAllLogicBlocks();
        for (ViewTransfer view : views) {
            System.out.println("\tConverting View " + view.getId() + " in environment " + view.getEnvironmentId());
            byte logicBytes[] = DAOFactoryHolder.getDAOFactory().getViewDAO().getLogicTextBytes(view.getId(), view.getEnvironmentId());
            
            if (logicBytes == null) {
                continue;
            }
            // set the view info
           
            
            List<FindTransfer> changes = new ArrayList<FindTransfer>();
            
            // generate Format Record Filter change
            FindTransfer trans = new FindTransfer();
            trans.setViewId(view.getId());
            trans.setLogicTextType(LogicTextType.Format_Record_Filter);
            trans.setCellId(null);
            
            changes.add(trans);
            
            // generate Extract Record Filter change
            List<ViewSourceTransfer> viewSources = DAOFactoryHolder.getDAOFactory()
                .getViewSourceDAO().getViewSources(view.getId(),
                        view.getEnvironmentId());
            for (ViewSourceTransfer viewSource : viewSources) {
                trans = new FindTransfer();
                trans.setViewId(view.getId());
                trans.setLogicTextType(LogicTextType.Extract_Record_Filter);
                trans.setCellId(viewSource.getId());                
                changes.add(trans);
            }
            
            
            // generate Format Calc change
            List<ViewColumnTransfer> viewColumns = DAOFactoryHolder.getDAOFactory().getViewColumnDAO().
                getViewColumns(view.getId(), view.getEnvironmentId());
            for (ViewColumnTransfer column : viewColumns) {
                trans = new FindTransfer();
                trans.setViewId(view.getId());
                trans.setLogicTextType(LogicTextType.Format_Column_Calculation);
                trans.setCellId(column.getId());
                changes.add(trans);
            }
            
            // generate Extract Calc change
            List<ViewColumnSourceTransfer> colSources = DAOFactoryHolder.getDAOFactory().getViewColumnSourceDAO().
                getViewColumnSources(view.getId(), view.getEnvironmentId());
            for (ViewColumnSourceTransfer colSource : colSources) {
                trans = new FindTransfer();
                trans.setViewId(view.getId());
                trans.setLogicTextType(LogicTextType.Extract_Column_Assignment);
                trans.setCellId(colSource.getId());
            
                changes.add(trans);
            }                      
            
            DAOFactoryHolder.getDAOFactory().getViewDAO().replaceLogicText(view.getEnvironmentId(), changes);
        }
    }

}
