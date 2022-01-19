package com.ibm.safr.we.model.logic;

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


import java.util.Arrays;

import com.ibm.safr.we.constants.LogicTextType;

public class LogicTextParserExtractCalc extends LogicTextParser {

    public LogicTextParserExtractCalc() {
        super();
        this.logicType = LogicTextType.Extract_Column_Assignment;
        initializeKeywordArray();
    }

    private void initializeKeywordArray() {
        String[] arr = new String[] { "IF", "THEN", "ELSE", "ENDIF",
            "ISFOUND", "COLUMN", "COL", "ALL", "TIMESTAMP", "BATCHDATE", "REPEAT",
            "FISCALYEAR", "FISCALMONTH", "FISCALDAY",
            "CURRENT", "PRIOR", "DATE", 
            "DAYSBETWEEN", "MONTHSBETWEEN", "YEARSBETWEEN",  
            "RUNYEAR", "RUNMONTH", "RUNDAY", 
            "ISFOUND", "ISNOTFOUND", "ISNULL", "ISNOTNULL", "ISNUMERIC",
            "ISNOTNUMERIC", "ISSPACES", "ISNOTSPACES", "WRITE", "SOURCE", "VIEW",
            "INPUT", "DATA", "DEST", "DESTINATION", "EXTRACT", "EXT",
            "PROCEDURE", "USEREXIT", "FILE" };
        keywords.addAll(Arrays.asList(arr));
    }
}
