package com.ibm.safr.we.constants;

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


public enum OutputFormat {
	Extract_Fixed_Width_Fields("Fixed Width Fields", "Fixed-Width"), 
	Extract_Source_Record_Layout("Source Record Layout", "Source-Record"), 
	Format_Fixed_Width_Fields("Fixed Width Fields", "Fixed-Width"), 
	Format_Delimited_Fields("Delimited Fields", "Delimited"), 
	Format_Report("Report", "Report");
	
    private String desc;
	private String colName;
	
	private OutputFormat(String desc, String colName) {
	    this.desc = desc;
        this.colName = colName;
	}

    public String getDesc() {
        return desc;
    }

    public String getColName() {
        return colName;
    }
	
}
