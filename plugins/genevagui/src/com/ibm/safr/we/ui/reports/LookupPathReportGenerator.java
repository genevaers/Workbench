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
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LookupPrimaryKeysBean;
import com.ibm.safr.we.model.query.LookupReportQueryBean;

public class LookupPathReportGenerator implements IReportGenerator {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.LookupPathReportData");

	private List<String> errorMsgList = new ArrayList<String>();
	private List<LookupReportQueryBean> lookupBeans = new ArrayList<>();
	private Map<Integer, List<LookupReportQueryBean>> lookupsById = new TreeMap<>();
	private Map<Integer, List<LookupPrimaryKeysBean>> primarysById = new TreeMap<>();

	private LookupPathHTMLReport htmlReport;
	private LookupPathCsvReport csvReport;

	private List<Integer> lookupIds;

	private List<LookupPrimaryKeysBean> primaryKeys;;

	
	public LookupPathReportGenerator(List<Integer> lookupPathIdsList) throws SAFRException {
		lookupIds = lookupPathIdsList;
		for (Integer lookupPathId : lookupPathIdsList) {
			loadData(lookupPathId);
		}
	}

	public LookupPathReportGenerator(LookupPath lookupPath) throws SAFRException {
		loadData(lookupPath.getId());
	}

	private void loadData(int id) {
		try {
			int envId = SAFRApplication.getUserSession().getEnvironment().getId();
			lookupBeans = DAOFactoryHolder.getDAOFactory().getReportsDAO().getLookupReport(id, envId);
			lookupsById.put(id, lookupBeans);
			primaryKeys = DAOFactoryHolder.getDAOFactory().getReportsDAO().getLookupPrimaryKeysReport(id, envId);
			primarysById.put(id, primaryKeys);
		} catch (SAFRDependencyException e) {
			errorMsgList
					.add("Lookup Path: "
							+ id
							+ "- Unable to load as below dependent components are inactive:" + SAFRUtilities.LINEBREAK
							+ e.getDependencyString(4));
		} catch (Exception ex) {
            logger.log(Level.SEVERE, "Unable to load", ex);
			errorMsgList.add("Lookup Path: " + id
					+ " - Unable to load due to below unexpected error:" + SAFRUtilities.LINEBREAK + "    "
					+ ex.toString());
		}
	}

	public List<String> getErrors() {
		return errorMsgList;
	}

	public boolean hasData() {
		return (!this.lookupsById.values().isEmpty());
	}

	@Override
	public String getHtmlUrl() {
		return htmlReport.getUrl();
	}

	@Override
	public void writeReportFiles(Path path, String baseName) {
		writeHtmlReport(path, baseName);
		writeCsvReport(path, baseName);
	}

	private void writeHtmlReport(Path path, String baseName) {
		htmlReport = new LookupPathHTMLReport();
		htmlReport.setFileName(path, baseName , lookupIds);
		htmlReport.addLookups(lookupsById);
		htmlReport.addPrimaryKeys(primarysById);
		htmlReport.write();
	}

	private void writeCsvReport(Path path, String baseName) {
		csvReport = new LookupPathCsvReport();
		csvReport.setFileName(path, baseName, lookupIds);
		csvReport.addLookups(lookupsById);
		csvReport.write();
	}
}
