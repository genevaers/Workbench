package com.ibm.safr.we.ui.editors.view;

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


import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewSource;

public class WriteStatementGenerator {

    private View view; 
    private ViewSource viewSource;
    
    public WriteStatementGenerator(View view, ViewSource viewSource) {
        this.view = view;
        this.viewSource = viewSource;
    }
    
    public void generateWriteStatement() {
        if (viewSource.isExtractOutputOverriden()) {
            return;
        }
        // calculate WRITE logic 
        String writeLogic = "";
        boolean isFormatPhase = view.isFormatPhaseInUse();  
        if (isFormatPhase) {
            Integer workFileNo = view.getExtractWorkFileNo();
            if (workFileNo == null) {
                workFileNo = 0;
            }
            writeLogic = "WRITE(SOURCE=VIEW,DEST=EXT=" + String.format("%03d",workFileNo) + getWriteParm() + ")";
        } else if (view.getOutputFormat().equals(OutputFormat.Extract_Fixed_Width_Fields)) {
            if (viewSource.getExtractFileAssociation() == null || viewSource.getExtractFileAssociationId() == 0) { 
                writeLogic = "WRITE(SOURCE=DATA,DEST=DEFAULT)";
            } else {
                writeLogic = "WRITE(SOURCE=DATA,"+ getFileParm() +")";
            }    
        } else if (view.getOutputFormat().equals(OutputFormat.Extract_Source_Record_Layout)) {
            if (viewSource.getExtractFileAssociation() == null || viewSource.getExtractFileAssociationId() == 0) { 
                writeLogic = "WRITE(SOURCE=INPUT,DEST=DEFAULT)";
            } else {
                writeLogic = "WRITE(SOURCE=INPUT,"+ getFileParm() +")";
            }    
        }           
        viewSource.setExtractRecordOutput(writeLogic);
    }

    protected String getFileParm() {
        FileAssociation assoc = viewSource.getExtractFileAssociation();
        String lfName = assoc.getAssociatingComponentName();
        String pfName = assoc.getAssociatedComponentName();
        String result = "DEST=FILE={" + lfName + "." + pfName + "}" + getWriteParm();        
        return result;
    }           
    
    protected String getWriteParm() {        
        String result = "";
        if (viewSource.getWriteExit() != null) {
            UserExitRoutine userExittrans = viewSource.getWriteExit();
            String exitArg =  "{" + userExittrans.getName() + "}";            
            if (viewSource.getWriteExitParams() != null && viewSource.getWriteExitParams().length() > 0) {
                String parmArg = ",\"" + viewSource.getWriteExitParams() + "\"";
                exitArg = ",USEREXIT=(" + exitArg + parmArg + ")";
            } else {
                exitArg = ",USEREXIT=" + exitArg;
            }            
            result += exitArg;
        }
        return result;
    }           
}
