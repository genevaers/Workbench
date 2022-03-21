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

public class LogicTextParserExtractFilter extends LogicTextParser {

    public LogicTextParserExtractFilter() {
        super();
        this.logicType = LogicTextType.Extract_Record_Filter;
        initializeKeywordArray();
    }

    private void initializeKeywordArray() {
        String[] arr  = new String[] { "SELECTIF", "SKIPIF", "SELECT", "SKIP",
            "IF", "THEN", "ELSE", "ENDIF", "ALL", "TIMESTAMP", "BATCHDATE","SUBSTR","LEFT","RIGHT",
            "FISCALDAY", "FISCALMONTH", "FISCALYEAR", 
            "CURRENT", "PRIOR",  "DATE", 
            "DAYSBETWEEN", "MONTHSBETWEEN", "YEARSBETWEEN",                 
            "ISFOUND", "ISNOTFOUND", "ISNULL", "ISNOTNULL", "ISNUMERIC",
            "ISNOTNUMERIC", "ISSPACES", "ISNOTSPACES", "REPEAT",
            "RUNDAY", "RUNMONTH", "RUNYEAR" };
        keywords.addAll(Arrays.asList(arr));
    }
    
}

