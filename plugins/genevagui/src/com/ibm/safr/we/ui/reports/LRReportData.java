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
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;

public class LRReportData implements IReportGenerator {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.LRReportData");
    
	Map<Integer, LRPropertiesRD> LRReportMap = new HashMap<Integer, LRPropertiesRD>();
	private List<String> errorMsgList = new ArrayList<String>();
	private List<Integer> lrIDs = new ArrayList<Integer>();

	public LRReportData(List<Integer> logicalRecordIds) throws SAFRException {
		for (int lrId : logicalRecordIds) {
			loadData(null, lrId);
		}
	}

	public LRReportData(LogicalRecord logicalRecord) throws SAFRException {
		loadData(logicalRecord, logicalRecord.getId());
	}

	private void loadData(LogicalRecord logicalRecord, Integer id) {
		try {

			if (logicalRecord == null) {
				logicalRecord = SAFRApplication.getSAFRFactory().getLogicalRecord(id);
			}
			LRPropertiesRD lrPropertiesReport = new LRPropertiesRD(logicalRecord);

			for (LRField lrField : logicalRecord.getLRFields().getActiveItems()) {
				LRFieldsReportWrapper lrFieldsReport = new LRFieldsReportWrapper(lrField);
				lrPropertiesReport.addField(lrFieldsReport);
			}
			LRReportMap.put(id, lrPropertiesReport);
			lrIDs.add(id);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Unable to load", ex);
			errorMsgList.add(
					"Logical Record: " + id + " - Unable to load due to below unexpected error:" + SAFRUtilities.LINEBREAK + "    " + ex.toString());
		}
	}

	public List<LRPropertiesRD> getLogicalRecordsReportData() {
		List<LRPropertiesRD> lrPropertiesList = new ArrayList<LRPropertiesRD>();
		for (Integer id : lrIDs) {
			lrPropertiesList.add(LRReportMap.get(id));
		}
		return lrPropertiesList;
	}

	public List<LRFieldsReportWrapper> getLogicalRecordFieldsReportData(Integer lrId) {
		if (!LRReportMap.isEmpty()) {
			LRPropertiesRD lrPropertyReport = LRReportMap.get(lrId);
			return lrPropertyReport.getLrFieldReportList();

		}
		return null;
	}


	public boolean hasData() {
		return (!this.LRReportMap.isEmpty());
	}

	public List<String> getErrors() {
		return errorMsgList;
	}

	@Override
	public String getHtmlUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeReportFiles(Path path, String bsseName) {
		// TODO Auto-generated method stub
		
	}

}
