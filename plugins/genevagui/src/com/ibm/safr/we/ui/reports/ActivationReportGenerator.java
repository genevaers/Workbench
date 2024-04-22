package com.ibm.safr.we.ui.reports;

import java.nio.file.Path;

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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class ActivationReportGenerator implements IReportGenerator {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.LogicTableReportGenerator");
	private ActivationHTMLReport htmlReport = new ActivationHTMLReport();
    
	private List<String> errorMsgList = new ArrayList<String>();
	private List<Integer> ids;
	
	private String logicTable;

	public ActivationReportGenerator(List<Integer> inputids) throws SAFRException {
		ids = inputids;
	}

	/**
	 * To create report for View that has been opened in the editor
	 * 
	 * @param view
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public ActivationReportGenerator(View view) throws DAOException, SAFRException {
	}


	public boolean hasData() {
		return true;
	}

	@Override
	public void writeReportFiles(Path path, String baseName) {
		writeHtmlReport(path, baseName);
	}

	private void writeHtmlReport(Path path, String baseName) {
		htmlReport.setFileName(path, baseName , ids);
		//htmlReport.addViewData(viewReportDataById);
		htmlReport.write();
	}

	@Override
	public List<String> getErrors() {
		return errorMsgList;
	}

	@Override
	public String getHtmlUrl() {
		return htmlReport.getUrl();
	}
	
	public void setLogicTable(String lt) {
		logicTable = lt;
	}
	
	public String getLogicTable() {
		return logicTable;
	}

}
