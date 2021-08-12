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
import com.ibm.safr.we.constants.Codes;
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
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class ViewColumnReport implements IReportData {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.ViewColumnReport");
    
	Map<Integer, ViewInfoRD> viewInfoMap = new HashMap<Integer, ViewInfoRD>();
	private List<String> errorMsgList = new ArrayList<String>();
	private List<Integer> viewIDs = new ArrayList<Integer>();

	public ViewColumnReport(List<Integer> viewIDs) throws SAFRException {
		for (int id : viewIDs) {
			loadData(null, id);
		}
	}

	/**
	 * To create report for View that has been opened in the editor
	 * 
	 * @param view
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public ViewColumnReport(View view) throws DAOException, SAFRException {
		loadData(view, view.getId());
	}

	private void loadData(View view, Integer id) {
		try {
			if (view == null) {
				view = SAFRApplication.getSAFRFactory().getView(id);
			}
			ViewInfoRD viewInfo = getViewInfoReportData(view);
			viewInfoMap.put(id, viewInfo);
			// CQ 8165. Nikita. 01/07/2010
			// retain the original order in which the list of View ids was
			// passed
			viewIDs.add(id);
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

	/************************************************************************************************************************/

	private ViewInfoRD getViewInfoReportData(View view) throws SAFRException {
		ViewInfoRD viewInfo = new ViewInfoRD();
		viewInfo.setView(UIUtilities.getComboString(view.getName() == null ? ""
				: view.getName(), view.getId()));
		viewInfo.setEnvironment(UIUtilities.getComboString(view
				.getEnvironment().getName(), view.getEnvironment().getId()));
		viewInfo.setDatabase(SAFRApplication.getDatabaseSchema());

		List<ViewColumnsInfoRD> viewColumnsInfoList = new ArrayList<ViewColumnsInfoRD>();
		List<ViewColumn> viewColumns = view.getViewColumns().getActiveItems();

		for (ViewColumn viewColumn : viewColumns) {
			ViewColumnsInfoRD viewColumnInfo = new ViewColumnsInfoRD();

			viewColumnInfo.setColumnNo(viewColumn.getColumnNo());
			if (viewColumn.getDataTypeCode() != null) {
				viewColumnInfo.setDataType(viewColumn.getDataTypeCode()
						.getDescription());
			} else {
				viewColumnInfo.setDataType("");
			}

			String desc = "";
			if (viewColumn.getHeading1() != null
					&& !viewColumn.getHeading1().equals("")) {
				desc = desc + viewColumn.getHeading1() + " ";
			}
			if (viewColumn.getHeading2() != null
					&& !viewColumn.getHeading2().equals("")) {
				desc = desc + viewColumn.getHeading2() + " ";
			}
			if (viewColumn.getHeading3() != null
					&& !viewColumn.getHeading3().equals("")) {
				desc = desc + viewColumn.getHeading3();
			}

			viewColumnInfo.setDescription(desc);

			viewColumnInfo.setVisible(viewColumn.isVisible());
			if (viewColumn.getFormatColumnCalculation() != null) {
				viewColumnInfo.setLogicText(true);
			} else {
				viewColumnInfo.setLogicText(false);
			}
			viewColumnInfo.setStartPosition(viewColumn.getStartPosition());
			viewColumnInfo.setLength(viewColumn.getLength());

			List<ViewColumnSourcesInfoRD> viewColSrcInfoList = new ArrayList<ViewColumnSourcesInfoRD>();
			for (ViewColumnSource viewColSrc : viewColumn
					.getViewColumnSources().getActiveItems()) {
				ViewColumnSourcesInfoRD viewColSrcInfo = new ViewColumnSourcesInfoRD();

				// CQ 8056. Nikita. 12/07/2010.
				// Separate getters for Lookup Field and Source LR Field
				LRField field = null;
				if (viewColSrc.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
					field = viewColSrc.getLRField();
				} else if (viewColSrc.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
					field = viewColSrc.getLRField();
				}
				if (field != null) {
					viewColSrcInfo.setField(UIUtilities.getComboString(field
							.getName(), field.getId()));
				} else {
					viewColSrcInfo.setField("[0]");
				}

				LogicalRecordQueryBean logicalRecord = viewColSrc
						.getLogicalRecordQueryBean();
				if (logicalRecord != null) {
					viewColSrcInfo.setLogicalRecord(UIUtilities.getComboString(
							logicalRecord.getName(), logicalRecord.getId()));
				} else {
					// If a Formula or Source File Field is used as column
					// source type, the data
					// source LR will be displayed
					if (viewColSrc.getSourceType().getGeneralId() == Codes.FORMULA
							|| viewColSrc.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
						ComponentAssociation assoc = viewColSrc.getViewSource()
								.getLrFileAssociation();
						viewColSrcInfo.setLogicalRecord(UIUtilities
								.getComboString(assoc
										.getAssociatingComponentName(), assoc
										.getAssociatingComponentId()));
					} else {
						viewColSrcInfo.setLogicalRecord("[0]");
					}
				}

				LookupQueryBean lookup = viewColSrc.getLookupQueryBean();
				if (lookup != null) {
					viewColSrcInfo.setLookup(UIUtilities.getComboString(lookup
							.getName(), lookup.getId()));
				} else {
					viewColSrcInfo.setLookup("[0]");
				}

				viewColSrcInfoList.add(viewColSrcInfo);
			}
			viewColumnInfo.setViewColSrcInfo(viewColSrcInfoList);

			viewColumnsInfoList.add(viewColumnInfo);
		}
		viewInfo.setViewColInfo(viewColumnsInfoList);

		return viewInfo;
	}

	/************************************************************************************************************************/

	public ViewInfoRD getViewInfoReportData(Integer viewId) {
		return viewInfoMap.get(viewId);
	}

	/************************************************************************************************************************/

	public List<ViewColumnsInfoRD> getViewColumnsInfoReportData(Integer viewId) {
		return viewInfoMap.get(viewId).getViewColInfo();
	}

	/************************************************************************************************************************/

	public List<ViewColumnSourcesInfoRD> getViewColumnSourcesInfoReportData(
			Integer viewId, Integer columnNo) {
		List<ViewColumnsInfoRD> viewCol = viewInfoMap.get(viewId)
				.getViewColInfo();
		return viewCol.get(columnNo - 1).getViewColSrcInfo();
	}

	/************************************************************************************************************************/

	public List<Integer> getViewIds() {
		// CQ 8165. Nikita. 01/07/2010
		// generate report based on original order in which View ids were passed
		return viewIDs;
	}

	/************************************************************************************************************************/

	public class ViewInfoRD {
		private String view;
		private String environment;
		private String database;
		private List<ViewColumnsInfoRD> viewColInfo = new ArrayList<ViewColumnsInfoRD>();

		public String getView() {
			return view;
		}

		public void setView(String view) {
			this.view = view;
		}

		public String getEnvironment() {
			return environment;
		}

		public void setEnvironment(String environment) {
			this.environment = environment;
		}

		public List<ViewColumnsInfoRD> getViewColInfo() {
			return viewColInfo;
		}

		public void setViewColInfo(List<ViewColumnsInfoRD> viewColInfo) {
			this.viewColInfo = viewColInfo;
		}

		public String getDatabase() {
			return database;
		}

		public void setDatabase(String database) {
			this.database = database;
		}

	}

	/************************************************************************************************************************/

	public class ViewColumnsInfoRD {
		private Integer columnNo;
		private String dataType;
		private String description;
		private Boolean visible;
		private Boolean logicText;
		private Integer startPosition;
		private Integer length;
		private List<ViewColumnSourcesInfoRD> viewColSrcInfo = new ArrayList<ViewColumnSourcesInfoRD>();

		public Integer getColumnNo() {
			return columnNo;
		}

		public void setColumnNo(Integer columnNo) {
			this.columnNo = columnNo;
		}

		public String getDataType() {
			return dataType;
		}

		public void setDataType(String dataType) {
			this.dataType = dataType;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String isVisible() {
			if (visible)
				return "Y";
			else
				return "N";
		}

		public void setVisible(Boolean visible) {
			this.visible = visible;
		}

		public String isLogicText() {
			if (logicText)
				return "Y";
			else
				return "N";
		}

		public void setLogicText(Boolean logicText) {
			this.logicText = logicText;
		}

		public Integer getStartPosition() {
			return startPosition;
		}

		public void setStartPosition(Integer startPosition) {
			this.startPosition = startPosition;
		}

		public Integer getLength() {
			return length;
		}

		public void setLength(Integer length) {
			this.length = length;
		}

		public List<ViewColumnSourcesInfoRD> getViewColSrcInfo() {
			return viewColSrcInfo;
		}

		public void setViewColSrcInfo(
				List<ViewColumnSourcesInfoRD> viewColSrcInfo) {
			this.viewColSrcInfo = viewColSrcInfo;
		}
	}

	/************************************************************************************************************************/

	public class ViewColumnSourcesInfoRD {
		private String field;
		private String logicalRecord;
		private String lookup;

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public String getLogicalRecord() {
			return logicalRecord;
		}

		public void setLogicalRecord(String logicalRecord) {
			this.logicalRecord = logicalRecord;
		}

		public String getLookup() {
			return lookup;
		}

		public void setLookup(String lookup) {
			this.lookup = lookup;
		}

	}

	/************************************************************************************************************************/

	public boolean hasData() {
		return (!this.viewInfoMap.isEmpty());
	}

	public List<String> getErrors() {
		return errorMsgList;
	}
	/************************************************************************************************************************/

}
