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
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.ViewMappingsCSVReportQueryBean;
import com.ibm.safr.we.model.query.ViewMappingsReportQueryBean;
import com.ibm.safr.we.model.query.ViewPropertiesReportQueryBean;
import com.ibm.safr.we.model.query.ViewSortKeyReportQueryBean;
import com.ibm.safr.we.model.query.ViewSourcesReportQueryBean;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class ViewPropertiesReportGenerator implements IReportGenerator {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.ViewPropertiesReportData");
    
	private Map<Integer, ViewReportData> viewReportDataById = new TreeMap<>();
	private List<String> errorMsgList = new ArrayList<String>();

	private Map<Integer, ViewSortKeyReportQueryBean> viewSortKeys;

	private ViewCSVWrapper csvwrapper;
	
	private ViewHTMLReport htmlReport = new ViewHTMLReport();
	private ViewCsvReport csvReport = new ViewCsvReport();

	private List<Integer> viewIDs;

	public ViewPropertiesReportGenerator(List<Integer> viewIDs) throws SAFRException {
		this.viewIDs = viewIDs;
		for (int id : viewIDs) {
			loadData(id);
		}
		getCSVColumMappings(viewIDs);
	}

	private void getCSVColumMappings(List<Integer> viewIDs) {
		int envId = SAFRApplication.getUserSession().getEnvironment().getId();
		List<ViewMappingsCSVReportQueryBean> csvmaps = DAOFactoryHolder.getDAOFactory().getReportsDAO().getViewColumnCSVMappings(viewIDs, envId);
		csvwrapper = new ViewCSVWrapper(csvmaps);
	}

	public ViewPropertiesReportGenerator(View view) throws SAFRException {
		loadData( view.getId());
	}

	// A view has some inherent properties
	// And a number of sources from which data is mapped to each view column
	//
	// View columns may also have an associated sort key
	private void loadData( Integer viewId) {
		try {
			
			int envId = SAFRApplication.getUserSession().getEnvironment().getId();
			ViewPropertiesReportQueryBean vp = DAOFactoryHolder.getDAOFactory().getReportsDAO().getViewProperties(viewId, envId);
			ViewReportData viewData = new ViewReportData(vp);
			
			//get sortkey data to be available later
			viewSortKeys = getSortKeysReportDataByColumnID(viewId, envId);
			
			List<ViewSourcesReportQueryBean> viewSources = getViewSourcesReportData(viewId, envId);		
			List<ViewSourceReportData> viewSourcesData = getColumnMappingsForEachSource(viewId, envId, viewSources);
			viewData.setViewSourcesData(viewSourcesData);
			
//			List<ViewReportData> viewPropertiesList = new ArrayList<>();
//			viewPropertiesList.add(viewData);

			viewReportDataById.put(viewId, viewData);
		} catch (SAFRDependencyException e) {
			errorMsgList
					.add("View: "
							+ viewId
							+ "- Unable to load as below dependent components are inactive:" + SAFRUtilities.LINEBREAK
							+ e.getDependencyString(4));
		} catch (Exception ex) {
            logger.log(Level.SEVERE, "Unable to load", ex);         
			errorMsgList.add("View: " + viewId
					+ " - Unable to load due to below unexpected error:" + SAFRUtilities.LINEBREAK + "    "
					+ ex.toString());
		}
	}

	private List<ViewSourceReportData> getColumnMappingsForEachSource(Integer id, int envId, List<ViewSourcesReportQueryBean> viewSources) {
		List<ViewSourceReportData> viewSourcesData = new ArrayList();
		for(ViewSourcesReportQueryBean vs : viewSources) {
			ViewSourceReportData vsrd = new ViewSourceReportData(vs);
			List<ViewMappingsReportQueryBean> vcms = getViewColumnMappingsForSource(id, envId, vs);
			addAssociatedSortkeysToColumns(vcms);
			vsrd.setColumnMappings(vcms);
			viewSourcesData.add(vsrd);
		}
		return viewSourcesData;
	}

	private List<ViewSourcesReportQueryBean> getViewSourcesReportData(int viewID, int envid) throws DAOException, SAFRException {
		return DAOFactoryHolder.getDAOFactory().getReportsDAO().getViewSources(viewID, envid);
	}

	private  Map<Integer, ViewSortKeyReportQueryBean> getSortKeysReportDataByColumnID(int viewId, int envID) throws DAOException, SAFRException {
		Map<Integer, ViewSortKeyReportQueryBean> skws = new HashMap<>(); 
		List<ViewSortKeyReportQueryBean> vskqbs = DAOFactoryHolder.getDAOFactory().getReportsDAO().getViewSortKeys(viewId, envID );
		for(ViewSortKeyReportQueryBean vsk : vskqbs) {
			skws.put(Integer.valueOf(vsk.getViewcolumnid()), vsk);
		}
		return skws;
	}
	
	private  List<ViewMappingsReportQueryBean> getViewColumnMappingsForSource(int viewID, int envid, ViewSourcesReportQueryBean vs) throws DAOException, SAFRException {
		return DAOFactoryHolder.getDAOFactory().getReportsDAO().getViewColumnMappings(viewID, envid, vs.getSrcseqnbr() );
	}
	
	private void addAssociatedSortkeysToColumns(List<ViewMappingsReportQueryBean> vcms) {
		for(ViewMappingsReportQueryBean vcm : vcms) {
			ViewSortKeyReportQueryBean sk = viewSortKeys.get(Integer.valueOf(vcm.getViewcolumnid()));
			if(sk != null) {
				vcm.setSortKey(sk);
			}
		}		
	}

	public List<String> getErrors() {
		return errorMsgList;
	}

	public boolean hasData() {
		return (!this.viewReportDataById.isEmpty());
	}

	public String getPropsCsv() {
		String csv = "";
		if(viewReportDataById.size() > 0) {
			boolean headerNotDone = true;
//			for(List<Object> vplist : viewPropertiesMap.values()) {
//				ViewHTLMREport vrw= (ViewHTLMREport) vplist.get(0);
//				if(headerNotDone) {
//					csv += vrw.getViewPropertiesHeaderCsv();
//					headerNotDone = false;
//				}
//				csv += vrw.getViewPropertiesDataCsv();
//			}
		}
		return csv;
	}

	@Override
	public String getHtmlUrl() {
		return htmlReport.getUrl();
	}

	@Override
	public void writeReportFiles(Path path, String baseName) {
		writeHtmlReport(path, baseName);
		writeCsvReports(path, baseName);
	}

	private void writeCsvReports(Path path, String baseName) {
		csvReport.setFileName(path, baseName , viewIDs);
		csvReport.addViewData(viewReportDataById);
		csvReport.writeProperties();
		csvReport.writeSourcesAndMappings();
	}

	private void writeHtmlReport(Path path, String baseName) {
		htmlReport.setFileName(path, baseName , viewIDs);
		for (int id : viewIDs) {
			loadData(id);
		}
		htmlReport.addViewData(viewReportDataById);
		htmlReport.write();
	}
}
