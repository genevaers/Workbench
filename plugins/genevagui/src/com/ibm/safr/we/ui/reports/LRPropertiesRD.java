package com.ibm.safr.we.ui.reports;

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


import java.util.ArrayList;
import java.util.List;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class LRPropertiesRD extends ReportTableData {
	private LogicalRecord lr;
	private Integer lrId;
	private String lrName;
	private String lrType;
	private String status;
	List<LRFieldsReportWrapper> lrFieldReportList = new ArrayList<LRFieldsReportWrapper>();

	public LRPropertiesRD(LogicalRecord lr) throws SAFRException {
		this.lr = lr;
		lrName = lr.getName();
		if (lr.getLRTypeCode() != null) {
			lrType = lr.getLRTypeCode().getDescription();
		}
		if (lr.getLRStatusCode() != null) {
			status = lr.getLRStatusCode().getDescription();
		}
	}

	public Integer getLrId() {
		return lrId;
	}

	public String getLrName() {
		return UIUtilities.getComboString(lr.getName(), lr.getId());
	}

	public String getLrType() {
		return lrType;
	}

	public String getStatus() {
		return status;
	}

	public void addField(LRFieldsReportWrapper field) {
		lrFieldReportList.add(field);
	}

	public List<LRFieldsReportWrapper> getLrFieldReportList() {
		return lrFieldReportList;
	}

	@Override
	public String getHtmlHeaderRow() {
		String hdr ="<tr>"
				+ getHeaderEntry("Field Name")
				+ getHeaderEntry("Start Position")
				+ getHeaderEntry("DataType")
				+ "</tr>";
	return hdr;
	}

	@Override
	public String getHtmlDataRow() {
		String row ="<tr>"
				+ getRowEntry(UIUtilities.getComboString(lr.getName(), lr.getId()))
				+ getHeaderEntry(lr.getLRTypeCode().toString())
				+ getHeaderEntry(lr.getLRStatusCode().toString())
				+ "</tr>";
		return row;
	}
	
	public String getHtml() {
		String html = "";
		html += "<table>";
		int row = 0;
		for(LRFieldsReportWrapper f :  lrFieldReportList) {
			if(row == 0) {
				html += f.getHtmlHeaderRow();
			}
			html += f.getHtmlDataRow();
			row++;
		}
		html += "</table>";
		return html;
	}
}
