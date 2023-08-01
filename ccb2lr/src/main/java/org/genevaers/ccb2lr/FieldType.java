package org.genevaers.ccb2lr;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


public enum FieldType {
    ALPHA("Alphanumeric", "ALNUM"),
    ZONED("Zoned Decimal", "NUMER"),
    PACKED("Packed Decimal", "PACKD"),
    BINARY("Binary", "BINRY"), 
    OCCURSGROUP("Alphanumeric", "ALNUM"), 
    GROUP("Alphanumeric", "ALNUM");

    private String dataType;
    private String code;
    private FieldType(String dt, String c) {
        dataType = dt;
        code = c;
    }

    public String getDataType() {
        return dataType;
    }

    public String getCode() {
        return code;
    }
}