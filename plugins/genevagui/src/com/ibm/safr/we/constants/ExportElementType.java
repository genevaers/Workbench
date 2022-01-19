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


public enum ExportElementType {
	VIEWFOLDER("ViewFolder"), 
	VFVASSOC("VF-V-Association"), 
	VIEW("View"), 
	VIEW_COLUMN("View-Column"), 
	VIEW_SOURCE("View-Source"), 
	VIEW_COLUMN_SOURCE("View-Column-Source"), 
	VIEW_SORT_KEY("View-SortKey"), 
	VIEW_HEADER_FOOTER("View-HeaderFooter"), 
	LOOKUP("Lookup"), 
	LOOKUP_SRCKEY("Lookup-Source-Key"), 
	LOOKUP_STEP("Lookup-Step"), 
	LOGICAL_RECORD("LogicalRecord"), 
	LRFIELD("LRField"), 
	LRFIELD_ATTRIBUTE("LR-Field-Attribute"), 
	LR_INDEX("LR-Index"), 
	LR_INDEX_FIELD("LR-IndexField"), 
	LRLFASSOC("LR-LF-Association"), 
	LOGICAL_FILE("LogicalFile"), 
	LFPFASSOC("LF-PF-Association"), 
	PHYSICAL_FILE("PhysicalFile"), 
	EXIT("Exit"), 
	CONTROL_RECORD("ControlRecord");
	
	private String xmlString;
	
	ExportElementType(String xmlString) {
	    this.setXmlString(xmlString);
	}

    public String getXmlString() {
        return xmlString;
    }

    private void setXmlString(String xmlString) {
        this.xmlString = xmlString;
    }
}
