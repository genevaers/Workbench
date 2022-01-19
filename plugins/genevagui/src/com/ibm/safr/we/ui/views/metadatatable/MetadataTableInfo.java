package com.ibm.safr.we.ui.views.metadatatable;

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


import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;

public class MetadataTableInfo {

	public static String[] getColumnHeaders(TreeItemId TreeId) {
		String[] columnHeaders = new String[] {};
		if (TreeId == TreeItemId.VIEWFOLDERCHILD) {
			columnHeaders = new String[] { "ID", "Name", "Status", "Phase",
					"Output Format", "FRA", "Created Date", "Created By", "Last Updated",
					"Updated By", "Last De/activated", "De/activated By", "Compiler", "Rights" };
		} else if (TreeId == TreeItemId.ENV) {
			columnHeaders = new String[] { "ID", "Description", "Created Date",
					"Created By", "Last Updated", "Updated By", "Admin Rights" };
		} else if (TreeId == TreeItemId.CONTROL) {
			columnHeaders = new String[] { "ID", "Name",
					"Created Date", "Created By", "Last Updated", "Updated By", "Rights" };
		} else if (TreeId == TreeItemId.PHYSICALFILE) {
			columnHeaders = new String[] { "ID", "Name", "File Type", "Access Method", 
			    "Input DD", "Input DSN", "Output DD", "Created Date", "Created By",
					"Last Updated", "Updated By", "Rights" };
		} else if (TreeId == TreeItemId.GROUP) {
			columnHeaders = new String[] { "ID", "Name", "Created Date",
					"Created By", "Last Updated", "Updated By" };
		} else if (TreeId == TreeItemId.VIEWFOLDER) {
			columnHeaders = new String[] { "ID", "Name", "Created Date", "Created By",
					"Last Updated", "Updated By", "Rights" };
		} else if (TreeId == TreeItemId.USER) {
			columnHeaders = new String[] { "User", "Name", "Access", "E-mail",
					"Created Date", "Created By", "Last Updated", "Updated By" };
		} else if (TreeId == TreeItemId.USEREXITROUTINE) {
			columnHeaders = new String[] { "ID", "Name", "Executable", "Type", "Language", "Created Date", "Created By", 
			    "Last Updated", "Updated By", "Rights" };
		} else if (TreeId == TreeItemId.LOGICALRECORD) {
			columnHeaders = new String[] { "ID", "Name", "Status", "Tot Length", "Key Length", "Created Date", 
			    "Created By", "Last Updated", "Updated By", "Last De/activated", "De/activated By", "Type", "Rights" };
		} else if (TreeId == TreeItemId.LOOKUP) {
			columnHeaders = new String[] { "ID", "Name", "Status", "Steps", "Source LR", "Last Target LR", "Last Target LF", 
			    "Created Date", "Created By", "Last Updated", "Updated By", "Last De/activated", "De/activated By", "Rights" };
		} else if (TreeId == TreeItemId.LOGICALFILE) {
			columnHeaders = new String[] { "ID", "Name", "Created Date", "Created By",
					"Last Updated", "Updated By", "Rights" };
		}
		return columnHeaders;

	}

	public static int[] getColumnWidths(TreeItemId TreeId) {
		int[] columnWidths = null;
		if (TreeId == TreeItemId.VIEWFOLDERCHILD) {
			columnWidths = new int[] { 60, 250, 80, 70, 120, 70, 120, 100, 120, 100, 120, 100, 100, 80 };
		} else if (TreeId == TreeItemId.ENV) {
			columnWidths = new int[] { 60, 250, 120, 100, 120, 100, 110 };
		} else if (TreeId == TreeItemId.CONTROL) {
			columnWidths = new int[] { 60, 250, 120, 100, 120, 100, 80 };
		} else if (TreeId == TreeItemId.PHYSICALFILE) {
			columnWidths = new int[] { 60, 250, 90, 120, 90, 90, 90, 120, 100, 120, 100, 80 };
		} else if (TreeId == TreeItemId.GROUP) {
			columnWidths = new int[] { 60, 250, 120, 100, 120, 100 };
		} else if (TreeId == TreeItemId.VIEWFOLDER) {
			columnWidths = new int[] { 60, 250, 120, 100, 120, 100, 80 };
		} else if (TreeId == TreeItemId.USER) {
			columnWidths = new int[] { 70, 250, 80, 70, 120, 100, 120, 100 };
		} else if (TreeId == TreeItemId.USEREXITROUTINE) {
			columnWidths = new int[] { 60, 250, 90, 80, 100, 120, 100, 120, 100, 80 };
		} else if (TreeId == TreeItemId.LOGICALRECORD) {
			columnWidths = new int[] { 60, 250, 80, 110, 110, 120, 100, 120, 100, 120, 100, 80, 80 };
		} else if (TreeId == TreeItemId.LOOKUP) {
			columnWidths = new int[] { 60, 250, 80, 60, 140, 140, 140, 120, 100, 120, 100, 120, 100, 80 };
		} else if (TreeId == TreeItemId.LOGICALFILE) {
			columnWidths = new int[] { 60, 250, 120, 100, 120, 100, 80 };
		}

		return columnWidths;
	}
}
