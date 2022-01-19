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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;

public class ViewSourceConverter {

    public static void main(String[] args) {
        SAFRApplication.getSAFRFactory().getAllCodeSets();      
        SAFRApplication.initDummyUserSession();
        System.out.println("Converting all View sources");
        convertAllViewSources();
        System.out.println("Finished converting all View sources");
    }

    protected static void convertAllViewSources() {        
        // loop through all environments 
        List<EnvironmentQueryBean> envBeans = 
            DAOFactoryHolder.getDAOFactory().getEnvironmentDAO().queryAllEnvironments(SortType.SORT_BY_ID);
        for (EnvironmentQueryBean envBean : envBeans) {
            System.out.println("\tConverting environment " + envBean.getId());
            List<ViewQueryBean> vBeans = 
            DAOFactoryHolder.getDAOFactory().getViewDAO().queryAllViews(SortType.SORT_BY_ID, envBean.getId(), true);
            for (ViewQueryBean vBean : vBeans) {
                System.out.println("\t\tConverting view " + vBean.getId() + " sources");
                convertView(vBean);
            }
        }
    }

    protected static void convertView(ViewQueryBean vBean) {
                
        List<ViewSourceTransfer> srcs = DAOFactoryHolder.getDAOFactory().getViewSourceDAO().getViewSources(
            vBean.getId(), vBean.getEnvironmentId());
        
        // loop sources and set new data
        for (ViewSourceTransfer src : srcs) {
            convertViewSource(src);
        }
        DAOFactoryHolder.getDAOFactory().getViewSourceDAO().persistViewSources(srcs);
    }
    
    protected static void convertViewSource(ViewSourceTransfer src) {
        
        // check for existing WRITE
        List<String> logics = DAOFactoryHolder.getDAOFactory().getViewDAO().getViewSourceLogic(
            src.getEnvironmentId(), src.getViewId(), src.getId());
        
        boolean writeExists = false;
        for (String logic : logics) {
            if (isWriteStatement(logic)) {
                writeExists = true;
                break;
            }
        }
        
        if (writeExists) {
            src.setExtractFileAssociationId(0);
            src.setWriteExitId(0);
            src.setWriteExitParams("");
            src.setExtractRecordOutput("");
            src.setExtractOutputOverride(true);
        }
        else {
            generateSourceOutputLogic(src);            
        }
        
    }
    
    protected static boolean isWriteStatement(String logic) {
        String[] lines = logic.split("\\n");
        for (String line : lines) {
            line = removeComments(line);
            line = removeStrings(line);
            line = line.trim().toUpperCase();
            if (line.matches(".*WRITE\\s*\\(.*\\).*")) {
                return true;
            }
        }
        return false;
    }

    private static String removeComments(String input) {
        Pattern commentPattern = Pattern.compile("(.*?)(\\\\|').*");
        Matcher nonComment = commentPattern.matcher(input);
        if (nonComment.find()) {
            return nonComment.group(1);            
        } 
        else {
            return input;
        }
    }
    
    private static String removeStrings(String input) {
        String removed = input;
        Pattern stringPattern = Pattern.compile("(.*)\".*?\"(.*)");
        Matcher remStr = stringPattern.matcher(removed);
        while (remStr.find()) {
            removed =  remStr.group(1) + remStr.group(2);     
            remStr = stringPattern.matcher(removed);
        } 
        return removed;
    }
    
    protected static void generateSourceOutputLogic(ViewSourceTransfer src) {
        
        ViewTransfer viewTrans = DAOFactoryHolder.getDAOFactory().getViewDAO().getView(
            src.getViewId(), src.getEnvironmentId());
        
        // calculate WRITE logic 
        String writeLogic = "";
        boolean isFormatPhase = !(viewTrans.getTypeCode().equals("EXTR") || viewTrans.getTypeCode().equals("COPY"));  
        if (isFormatPhase) {
            Integer workFileNo = viewTrans.getWorkFileNumber();
            writeLogic = "WRITE(SOURCE=VIEW,DEST=EXT=" + String.format("%03d",workFileNo) + getWriteParm(viewTrans) + ")";
        } else if (viewTrans.getTypeCode().equals("COPY")) {
            if (viewTrans.getExtractFileAssocId() == null || viewTrans.getExtractFileAssocId()==0) { 
                writeLogic = "WRITE(SOURCE=INPUT,DEST=DEFAULT)";
            } else {
                writeLogic = "WRITE(SOURCE=INPUT," + getFileParm(viewTrans) +")";
            }    
        } else if (viewTrans.getTypeCode().equals("EXTR")) {
            if (viewTrans.getExtractFileAssocId() == null || viewTrans.getExtractFileAssocId()==0) { 
                writeLogic = "WRITE(SOURCE=DATA,DEST=DEFAULT)";
            } else {
                writeLogic = "WRITE(SOURCE=DATA," + getFileParm(viewTrans) +")";
            }    
        }        
        src.setExtractFileAssociationId(viewTrans.getExtractFileAssocId());
        src.setWriteExitId(viewTrans.getWriteExitId());
        src.setWriteExitParams(viewTrans.getWriteExitParams());
        src.setExtractRecordOutput(writeLogic);
        src.setExtractOutputOverride(false);
    }

    protected static String getFileParm(ViewTransfer viewTrans) {
        FileAssociationTransfer assoc = DAOFactoryHolder.getDAOFactory().getLogicalFileDAO().getLFPFAssociation(
            viewTrans.getExtractFileAssocId(), viewTrans.getEnvironmentId());
        String lfName = assoc.getAssociatingComponentName();
        String pfName = assoc.getAssociatedComponentName();
        String result = "DEST=FILE={" + lfName + "." + pfName + "}" + getWriteParm(viewTrans);
        return result;
    }
    
    protected static String getWriteParm(ViewTransfer viewTrans) {
        String result = "";
        if (viewTrans.getWriteExitId() != null && viewTrans.getWriteExitId() > 0) {
            UserExitRoutineTransfer userExittrans = DAOFactoryHolder.getDAOFactory().getUserExitRoutineDAO().getUserExitRoutine(
                viewTrans.getWriteExitId(), viewTrans.getEnvironmentId());
            String exitArg =  "{" + userExittrans.getName() + "}";            
            if (viewTrans.getWriteExitParams() != null && viewTrans.getWriteExitParams().length() > 0) {
                String parmArg = ",\"" + viewTrans.getWriteExitParams() + "\"";
                exitArg = ",USEREXIT=(" + exitArg + parmArg + ")";
            } else {
                exitArg = ",USEREXIT=" + exitArg;
            }            
            result += exitArg;
        }
        return result;
    }
    
}
