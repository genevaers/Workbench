package com.ibm.safr.we.model.query;

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


import java.util.Date;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.constants.OutputPhase;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;

public class ViewQueryBeanConv extends ViewQueryBean {


    private String phase;
    private String outputFormat;
    private String aggrLevel;

    public ViewQueryBeanConv(Integer environmentId, Integer id, String name, String status, String outputFormat,
        String type, EditRights rights, Date createTime, String createBy, Date modifyTime, String modifyBy,
        String compilerVersion, Date activatedTime, String activatedBy) {
        super(environmentId, id, name, status, outputFormat, type, rights, createTime, createBy, modifyTime, modifyBy,
            compilerVersion, activatedTime, activatedBy);
        convertTypeCodeToOutputFormat();
    }

    public ViewQueryBeanConv(ViewQueryBean bean) {
        super(bean.getEnvironmentId(), 
            bean.getId(), 
            bean.getName(), 
            bean.getStatus(), 
            bean.getOldOutputFormat(), 
            bean.getOldType(), 
            bean.getRights(), 
            bean.getCreateTime(), 
            bean.getCreateBy(), 
            bean.getModifyTime(), 
            bean.getModifyBy(),
            bean.getCompilerVersion(),
            bean.getActivatedTime(), 
            bean.getActivatedBy());
        convertTypeCodeToOutputFormat();
    }
    

    private void convertTypeCodeToOutputFormat() {
        
        Code typeCode = SAFRApplication.getSAFRFactory()
            .getCodeSet(CodeCategories.VIEWTYPE)
            .getCode(getOldType());
        Code outputFormatCode = SAFRApplication.getSAFRFactory()
                .getCodeSet(CodeCategories.OUTPUTMED)
                .getCode(getOldOutputFormat());
        
        switch (typeCode.getGeneralId()) {
        case Codes.SUMMARY:
            phase = OutputPhase.Format.name();
            switch (outputFormatCode.getGeneralId()) {
            case Codes.DELIMITED:
                outputFormat = OutputFormat.Format_Delimited_Fields.getColName();
                break;
            case Codes.FILE:
                outputFormat = OutputFormat.Format_Fixed_Width_Fields.getColName();
                break;
            case Codes.HARDCOPY:
                outputFormat = OutputFormat.Format_Report.getColName();
                break;
            }
            aggrLevel = "Summary";
            break;
        case Codes.DETAIL:
            phase = OutputPhase.Format.name();
            switch (outputFormatCode.getGeneralId()) {
            case Codes.DELIMITED:
                outputFormat = OutputFormat.Format_Delimited_Fields.getColName();
                break;
            case Codes.FILE:
                outputFormat = OutputFormat.Format_Fixed_Width_Fields.getColName();
                break;
            case Codes.HARDCOPY:
                outputFormat = OutputFormat.Format_Report.getColName();
                break;
            }
            // For Detail view types, format phase is on but record aggregation
            // is off by default.
            aggrLevel = "Detail";
            break;
        case Codes.EXTRACT_ONLY:
            switch (outputFormatCode.getGeneralId()) {
            case Codes.FILE:
                phase = OutputPhase.Extract.name();
                outputFormat = OutputFormat.Extract_Fixed_Width_Fields.getColName();
            }
            // Format phase and Record Aggregation are always off by default for
            // Extract Only View types.
            aggrLevel = "Detail";
            break;
        case Codes.COPY_INPUT:
            switch (outputFormatCode.getGeneralId()) {
            case Codes.FILE:
                phase = OutputPhase.Extract.name();
                outputFormat = OutputFormat.Extract_Source_Record_Layout.getColName();
            }
            // Format phase and Record Aggregation are always off by default for
            // Copy Input View types.
            aggrLevel = "Detail";
            break;
        }
    }

    public String getPhase() {
        return phase;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public String getAggrLevel() {
        return aggrLevel;
    }

    
}
