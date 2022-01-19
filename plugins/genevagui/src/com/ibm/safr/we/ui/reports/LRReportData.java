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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.LRFieldKeyType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class LRReportData implements IReportData {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.LRReportData");
    
	Map<Integer, LRPropertiesRD> LRReportMap = new HashMap<Integer, LRPropertiesRD>();
	private List<String> errorMsgList = new ArrayList<String>();
	private List<Integer> lrIDs = new ArrayList<Integer>();

	public LRReportData(List<Integer> logicalRecordIds) throws SAFRException {
		for (int lrId : logicalRecordIds) {
			loadData(null, lrId);
		}
	}

	/**
	 * Constructor for class LRReportData which accepts a LR model object of the
	 * Logical Record whose report is to be generated.
	 * 
	 * @param logicalRecord
	 *            : LogicalRecord whose report is to be generated.
	 * @throws SAFRException
	 */
	public LRReportData(LogicalRecord logicalRecord) throws SAFRException {
		loadData(logicalRecord, logicalRecord.getId());
	}

	private void loadData(LogicalRecord logicalRecord, Integer id) {
		try {

			if (logicalRecord == null) {
				logicalRecord = SAFRApplication.getSAFRFactory()
						.getLogicalRecord(id);
			}
			// set the properties of LR.
			LRPropertiesRD lrPropertiesReport = new LRPropertiesRD(
					logicalRecord);

			// loop through all the lr fields.
			for (LRField lrField : logicalRecord.getLRFields().getActiveItems()) {
				LRFieldsRD lrFieldsReport = new LRFieldsRD(lrField);
				lrPropertiesReport.addField(lrFieldsReport);
			}
			// add to map
			LRReportMap.put(id, lrPropertiesReport);
			// CQ 8165. Nikita. 01/07/2010
			// retain the original order in which the list of LR ids was
			// passed
			lrIDs.add(id);
		} catch (Exception ex) {
            logger.log(Level.SEVERE, "Unable to load", ex);
			errorMsgList.add("Logical Record: " + id
					+ " - Unable to load due to below unexpected error:" + SAFRUtilities.LINEBREAK + "    "
					+ ex.toString());
		}
	}

	/************************************************************************************************************************/
	/**
	 * This method is used to get the list of properties of all the selected
	 * logical records.
	 * 
	 * @return list of LRPropertyReport.
	 */
	public List<LRPropertiesRD> getLogicalRecordsReportData() {
		List<LRPropertiesRD> lrPropertiesList = new ArrayList<LRPropertiesRD>();
		// CQ 8165. Nikita. 01/07/2010
		// generate report based on original order in which LR ids were
		// passed
		for (Integer id : lrIDs) {
			lrPropertiesList.add(LRReportMap.get(id));
		}
		return lrPropertiesList;
	}

	/************************************************************************************************************************/

	/**
	 * This method is used to get the properties of LR fields.
	 * 
	 * @param lrId
	 *            : The ID of the LR for which the properties are to be
	 *            retrieved.
	 * @return a list of LRFieldReport.
	 */
	public List<LRFieldsRD> getLogicalRecordFieldsReportData(Integer lrId) {
		if (!LRReportMap.isEmpty()) {
			LRPropertiesRD lrPropertyReport = LRReportMap.get(lrId);
			return lrPropertyReport.getLrFieldReportList();

		}
		return null;
	}

	/************************************************************************************************************************/

	public class LRPropertiesRD {
		private Integer lrId;
		private String lrName;
		private String lrType;
		private String status;
		List<LRFieldsRD> lrFieldReportList = new ArrayList<LRFieldsRD>();

		public LRPropertiesRD(LogicalRecord lr) throws SAFRException {
			lrId = lr.getId();
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
			if (this.lrName == null) {
				this.lrName = "";
			}
			return UIUtilities.getComboString(lrName, lrId);
		}

		public String getLrType() {
			return lrType;
		}

		public String getStatus() {
			return status;
		}

		public void addField(LRFieldsRD field) {
			lrFieldReportList.add(field);
		}

		public List<LRFieldsRD> getLrFieldReportList() {
			return lrFieldReportList;
		}
	}

	public class LRFieldsRD {
		private Integer fieldId;
		private String fieldName;
		private Integer startPosition;
		private String redefine;
		private String primaryKey;
		private String dataType;
		private Integer length;
		private Integer decimal;
		private String dateTimeFormat;

		public LRFieldsRD(LRField lrField) {
			fieldId = lrField.getId();
			fieldName = lrField.getName();
			startPosition = lrField.getPosition();
			if (lrField.getRedefine() == null) {
                redefine = "";
			} else {
                redefine = lrField.getRedefine().toString();
			}
			if (lrField.getKeyType().equals(LRFieldKeyType.PRIMARYKEY)) {
				primaryKey = "P";
			} else {
				primaryKey = "";
			}
			if (lrField.getDataTypeCode() != null) {
				dataType = lrField.getDataTypeCode().getDescription();
			}
			length = lrField.getLength();
			decimal = lrField.getDecimals();
			if (lrField.getDateTimeFormatCode() != null) {
				dateTimeFormat = lrField.getDateTimeFormatCode().getDescription();
			}
		}

		public Integer getFieldId() {
			return fieldId;
		}

		public String getFieldName() {
			return fieldName;
		}

		public Integer getStartPosition() {
			return startPosition;
		}

		public String getRedefine() {
			return redefine;
		}

		public String getPrimaryKey() {
			return primaryKey;
		}

		public String getDataType() {
			return dataType;
		}

		public Integer getLength() {
			return length;
		}

		public Integer getDecimal() {
			return decimal;
		}

		public String getDateTimeFormat() {
			return dateTimeFormat;
		}

	}

	/************************************************************************************************************************/

	public boolean hasData() {
		return (!this.LRReportMap.isEmpty());
	}

	public List<String> getErrors() {
		return errorMsgList;
	}
	/************************************************************************************************************************/

}
