package com.ibm.safr.we.ui.reports;

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

public abstract class  ReportTableData {
	public abstract  String getHtmlHeaderRow();
	public abstract String getHtmlDataRow() ;
	
	protected  String getHeaderEntry(String h) {
		return  "<th bgcolor=\"AliceBlue\">" + h + "</th>";
	}

	protected  String getLeftAlignHeaderEntry(String h) {
		return  "<th bgcolor=\"AliceBlue\" align=\"left\">" + h + "</th>";
	}

	protected  String getHeaderEntry(String h, String col) {
		return  "<th bgcolor=\"" + col + "\">" + h + "</th>";
	}

	protected String getRowEntry(String e) {
		return  "<td>" + e + "</td>";
	}

	protected String getColoredRowEntry(String e, String colour) {
		return  "<td bgcolor=\"" + colour + "\">" + e + "</td>";
	}

}
