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

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ViewColumnPICQueryBean;

public class ViewColumnPICReportGenerator implements IReportGenerator {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.ViewColumnPICReport");

	private List<String> errorMsgList = new ArrayList<String>();
	private List<Integer> viewIDs;

	private List<ViewColumnPICQueryBean> viewColumns;
	
	private ViewColumnPICHTMLReport htmlreport = new ViewColumnPICHTMLReport();
	private ViewColumnPICCsvReport csvreport = new ViewColumnPICCsvReport();

	public ViewColumnPICReportGenerator(List<Integer> viewIDs) throws SAFRException {
		this.viewIDs = viewIDs;
	}

	private void loadData(Integer id) {
		try {
			int envId = SAFRApplication.getUserSession().getEnvironment().getId();
			viewColumns = DAOFactoryHolder.getDAOFactory().getReportsDAO().getViewColumnPICData(id, envId);
			//Create a View Column PIC Report
			// how will we manage it writing more than on view?
			
			// need the html frame work ... the report
			// add view columns and so add view
			// 
			
		} catch (SAFRDependencyException e) {
			errorMsgList
					.add("View: "
							+ id
							+ "- Unable to load as below dependent components are inactive:" + SAFRUtilities.LINEBREAK
							+ e.getDependencyString(4));
		} catch (Exception ex) {
            logger.log(Level.SEVERE, "Unable to load", ex);		    
			errorMsgList.add("View: " + id
					+ " - Unable to load due to below unexpected error:" + SAFRUtilities.LINEBREAK + "    "
					+ ex.toString());
		}
	}

	public List<String> getErrors() {
		return errorMsgList;
	}

	public boolean hasData() {
		return (!this.viewColumns.isEmpty());
	}

	@Override
	public void writeReportFiles(Path path, String baseName) {
		getReportData();
		wrthHtmlReport(path, baseName);
		writeCsvReport(path, baseName);
	}

	private void writeCsvReport(Path path, String baseName) {
		csvreport.setFileName(path, baseName, viewIDs);
		csvreport.write();
	}

	private void wrthHtmlReport(Path path, String baseName) {
		htmlreport.setFileName(path, baseName ,viewIDs);
		htmlreport.write();
	}

	private void getReportData() {
		for (int id : viewIDs) {
			loadData(id);
			htmlreport.addViewColumns(id, viewColumns);
			csvreport.addViewColumns(id, viewColumns);
		}
	}

	@Override
	public String getHtmlUrl() {
		return htmlreport.getUrl();
	}

}
