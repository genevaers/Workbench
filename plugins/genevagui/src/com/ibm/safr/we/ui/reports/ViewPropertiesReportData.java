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
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.constants.OutputPhase;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSortKey;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class ViewPropertiesReportData implements IReportData {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.ViewPropertiesReportData");
    
	private Map<Integer, List<Object>> viewPropertiesMap = new HashMap<Integer, List<Object>>();
	private List<String> errorMsgList = new ArrayList<String>();
	private List<Integer> viewIDs = new ArrayList<Integer>();

	public ViewPropertiesReportData(List<Integer> viewIDs) throws SAFRException {
		for (int id : viewIDs) {
			loadData(null, id);
		}
	}

	public ViewPropertiesReportData(View view) throws SAFRException {
		loadData(view, view.getId());
	}

	private void loadData(View view, Integer id) {
		try {
			if (view == null) {
				view = SAFRApplication.getSAFRFactory().getView(id);
			}
			GeneralViewPropertiesRD generalViewPropertiesRD = getGeneralViewPropertiesReportData(view);
			List<ViewSourcesRD> viewSourceRD = getViewSourcesReportData(view);
			List<ViewColumnAttributesRD> viewColumnAttributesRD = getViewColumnAttributesReportData(view);
			List<SortKeysRD> sortKeysRD = getSortKeysReportData(view);

			List<Object> viewPropertiesList = new ArrayList<Object>();
			viewPropertiesList.add(generalViewPropertiesRD);
			viewPropertiesList.add(viewSourceRD);
			viewPropertiesList.add(viewColumnAttributesRD);
			viewPropertiesList.add(sortKeysRD);

			viewPropertiesMap.put(id, viewPropertiesList);
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

	private GeneralViewPropertiesRD getGeneralViewPropertiesReportData(View view)
			throws SAFRException {
		GeneralViewPropertiesRD viewProperties = new GeneralViewPropertiesRD();
		viewProperties.setView(UIUtilities.getComboString(
				view.getName() == null ? "" : view.getName(), view.getId()));
		viewProperties.setStatus(view.getStatusCode() == null ? "" : view
				.getStatusCode().getDescription());

		// General Section
		OutputPhase outputFormat = view.getOutputPhase();
		if (outputFormat == null) {
			viewProperties.setOutputFormat("");
		} else if (outputFormat == OutputPhase.Extract) {
			viewProperties.setOutputFormat("Extract");
		} else if (outputFormat == OutputPhase.Format) {
			viewProperties.setOutputFormat("Format");
		}
		OutputFormat type = view.getOutputFormat();
		if (type == null) {
			viewProperties.setType("");
		} else if (type == OutputFormat.Extract_Fixed_Width_Fields ||
		    type == OutputFormat.Format_Fixed_Width_Fields) {
			viewProperties.setType("Fixed-Length Fields");
		} else if (type == OutputFormat.Extract_Source_Record_Layout) {
			viewProperties.setType("Source Record Layout");
		} else if (type == OutputFormat.Format_Report) {
            viewProperties.setType("Report");
        } else if (type == OutputFormat.Format_Delimited_Fields) {
            viewProperties.setType("Delimited Fields");
        }
		
		if (view.isFormatPhaseInUse()) {
			viewProperties.setFormatPhase("On");
		} else {
			viewProperties.setFormatPhase("Off");
		}

		// Output Section
		FileAssociation assoc = view.getExtractFileAssociation();
		viewProperties.setOutputLogicalFile(assoc == null ? "None"
				: UIUtilities.getComboString(assoc
						.getAssociatingComponentName(), assoc
						.getAssociatingComponentId()));
		// CQ 8138. Nikita. 24/06/2010. Display Output Physical File too.
		viewProperties.setOutputPhysicalFile(assoc == null ? "None"
				: UIUtilities.getComboString(
						assoc.getAssociatedComponentName(), assoc
								.getAssociatedComponentIdNum()));
		// CQ 8138. Nikita. 24/06/2010. Display Extract Phase Output Limit too.
		if (view.hasExtractPhaseOutputLimit() != null
				&& view.hasExtractPhaseOutputLimit()) {
			viewProperties.setExtractOutputRecordLimit(Integer.toString(view
					.getExtractMaxRecords()));
		} else {
			viewProperties.setExtractOutputRecordLimit("None");
		}
		if (view.isFormatPhaseInUse()) {
			if (view.hasFormatPhaseOutputLimit() != null
					&& view.hasFormatPhaseOutputLimit()) {
				viewProperties.setFormatOutputRecordLimit(Integer.toString(view
						.getOutputMaxRecCount()));
			} else {
				viewProperties.setFormatOutputRecordLimit("None");
			}
		} else {
			viewProperties.setFormatOutputRecordLimit("N/A");
		}

		// Format section
		// CQ 8138. Nikita. 24/06/2010. Should display "N/A" for extract-only
		// views.
		if (view.isFormatPhaseInUse()) {
			viewProperties
					.setLinesPerPage(view.getOutputFormat() != OutputFormat.Format_Report ? "N/A"
							: Integer.toString(view.getLinesPerPage()));
			viewProperties
					.setReportWidth(view.getOutputFormat() != OutputFormat.Format_Report ? "N/A"
							: Integer.toString(view.getReportWidth()));
		} else {
			viewProperties.setLinesPerPage("N/A");
			viewProperties.setReportWidth("N/A");
		}

		// Advanced section
		viewProperties
				.setControlRecord(view.getControlRecord() == null ? "None"
						: UIUtilities.getComboString(view.getControlRecord()
								.getName(), view.getControlRecord().getId()));
		if (view.getWriteExit() != null) {
			viewProperties.setWriteProcedure(UIUtilities.getComboString(view
					.getWriteExit().getName(), view.getWriteExit().getId()));
			viewProperties.setWriteParam(view.getWriteExitParams() == null ? ""
					: view.getWriteExitParams());
		} else {
			viewProperties.setWriteProcedure("None");
			viewProperties.setWriteParam("");
		}
		// CQ 8138. Nikita. 23/06/2010. Should display "N/A" for extract-only
		// views.
		if (view.isFormatPhaseInUse()) {
			if (view.getFormatExit() != null) {
				viewProperties.setFormatProcedure(UIUtilities.getComboString(
						view.getFormatExit().getName(), view.getFormatExit()
								.getId()));
				viewProperties
						.setFormatParam(view.getFormatExitParams() == null ? ""
								: view.getFormatExitParams());
			} else {
				viewProperties.setFormatProcedure("None");
				viewProperties.setFormatParam("");
			}
			if (view.isSuppressZeroRecords() != null) {
				if (view.isSuppressZeroRecords()) {
					viewProperties.setSuppressZeros("Yes");
				} else {
					viewProperties.setSuppressZeros("No");
				}
			} else {
				viewProperties.setSuppressZeros("No");
			}
		} else {
			viewProperties.setFormatProcedure("N/A");
			viewProperties.setFormatParam("N/A");
			viewProperties.setSuppressZeros("N/A");
		}
		if (view.isExtractAggregateBySortKey() != null && view.isExtractAggregateBySortKey()) {
			viewProperties.setExtractAggregationBufferSize(Integer
					.toString(view.getExtractAggregateBufferSize()));
		} else {
			viewProperties.setExtractAggregationBufferSize("None");
		}

		// Counts section
		viewProperties.setSourceCount(view.getViewSources().getActiveItems()
				.size());
		viewProperties.setSortKeyCount(view.getViewSortKeys().getActiveItems()
				.size());
		int visibleColCount = 0;
		List<ViewColumn> viewColumns = view.getViewColumns().getActiveItems();
		for (ViewColumn visibleCol : viewColumns) {
			if (visibleCol.isVisible()) {
				visibleColCount++;
			}
		}
		viewProperties.setColumnCount(viewColumns.size());
		viewProperties.setVisibleColumnCount(visibleColCount);

		return viewProperties;
	}

	/************************************************************************************************************************/
	private List<ViewSourcesRD> getViewSourcesReportData(View view)
			throws DAOException, SAFRException {
		List<ViewSourcesRD> viewSources = new ArrayList<ViewSourcesRD>();
		List<ViewSource> sources = view.getViewSources().getActiveItems();
		for (ViewSource source : sources) {
			ViewSourcesRD viewSource = new ViewSourcesRD();
			viewSource.setViewSourceNo(source.getSequenceNo());
			ComponentAssociation association;
			association = source.getLrFileAssociation();
			viewSource.setViewSource(association == null ? "" : "["
					+ association.getAssociatingComponentName() + "."
					+ association.getAssociatedComponentName() + "]");
			viewSource
					.setExtractRecordFilter(source.getExtractRecordFilter() == null ? ""
							: source.getExtractRecordFilter());

			List<ViewColumnMappingRD> columnMappings = new ArrayList<ViewColumnMappingRD>();
			for (ViewColumn col : view.getViewColumns().getActiveItems()) {
				ViewColumnSource viewColumnSource = col.getViewColumnSources()
						.getActiveItems().get(source.getSequenceNo() - 1);
				ViewColumnMappingRD columnMapping = new ViewColumnMappingRD();
				columnMapping.setColumnNo(col.getColumnNo());
				Code sourceType = viewColumnSource.getSourceType();
				columnMapping.setColumnType(sourceType == null ? ""
						: sourceType.getDescription());

				if (sourceType.getGeneralId() == Codes.CONSTANT) {
					columnMapping.setColumnValue(viewColumnSource
							.getSourceValue() == null ? "" : viewColumnSource
							.getSourceValue());
				} else if (sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD) {
					LRField sourceField = viewColumnSource.getLRField();
					columnMapping.setColumnValue(sourceField == null ? ""
							: UIUtilities.getComboString(sourceField.getName(),
									sourceField.getId()));
				} else if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
					LogicalRecordQueryBean sourceLogicalRecord = viewColumnSource
							.getLogicalRecordQueryBean();
					LookupQueryBean sourceLookup = viewColumnSource
							.getLookupQueryBean();
					// CQ 8056. Nikita. 12/07/2010.
					// Separate getters for Lookup field and Source LR field
					LRField sourceField = viewColumnSource.getLRField();

					if (sourceLogicalRecord != null && sourceLookup != null
							&& sourceField != null) {
						columnMapping.setColumnValue(UIUtilities
								.getComboString(sourceLogicalRecord.getName(),
										sourceLogicalRecord.getId())
								+ "."
								+ UIUtilities.getComboString(sourceLookup
										.getName(), sourceLookup.getId())
								+ "."
								+ UIUtilities.getComboString(sourceField
										.getName(), sourceField.getId()));
					} else {
						columnMapping.setColumnValue("");
					}
				} else if (sourceType.getGeneralId() == Codes.FORMULA) {
					columnMapping.setColumnValue(viewColumnSource
							.getExtractColumnAssignment() == null ? ""
							: viewColumnSource.getExtractColumnAssignment());
				}
				columnMappings.add(columnMapping);
			}
			viewSource.setViewColumnMaps(columnMappings);
			viewSources.add(viewSource);
		}
		return viewSources;
	}

	/************************************************************************************************************************/

	private List<ViewColumnAttributesRD> getViewColumnAttributesReportData(
			View view) {
		List<ViewColumnAttributesRD> columnAttributes = new ArrayList<ViewColumnAttributesRD>();
		List<ViewColumn> viewColumns = view.getViewColumns().getActiveItems();
		for (ViewColumn viewCol : viewColumns) {
			ViewColumnAttributesRD colAttribute = new ViewColumnAttributesRD();
			colAttribute.setColumnNo(viewCol.getColumnNo());
			colAttribute.setSpacesBeforeColumn(viewCol.getSpacesBeforeColumn());
			colAttribute.setStartPosition(viewCol.getStartPosition());
			colAttribute.setDataType(viewCol.getDataTypeCode() != null ? viewCol
					.getDataTypeCode().getDescription() : "");
			colAttribute
					.setDateTimeFormat(viewCol.getDateTimeFormatCode() != null ? viewCol
							.getDateTimeFormatCode().getDescription()
							: "");
			colAttribute.setLength(viewCol.getLength());
			colAttribute
					.setDataAlignment(viewCol.getDataAlignmentCode() != null ? viewCol
							.getDataAlignmentCode().getDescription()
							: "");
			colAttribute.setDecimalPlaces(viewCol.getDecimals());
			colAttribute.setScalingFactor(viewCol.getScaling());
			colAttribute.setSigned(viewCol.isSigned());
			colAttribute.setVisible(viewCol.isVisible());
			colAttribute.setSortKey(viewCol.isSortKey());
			if (viewCol.getFormatColumnCalculation() != null) {
				colAttribute.setFormatPhaseCalculation(viewCol
						.getFormatColumnCalculation());
			}
			columnAttributes.add(colAttribute);
		}
		return columnAttributes;
	}

	/************************************************************************************************************************/

	private List<SortKeysRD> getSortKeysReportData(View view)
			throws DAOException, SAFRException {
		List<SortKeysRD> sortKeys = new ArrayList<SortKeysRD>();
		List<ViewSortKey> viewSortKeys = view.getViewSortKeys()
				.getActiveItems();
		for (ViewSortKey viewSortKey : viewSortKeys) {
			SortKeysRD sortKey = new SortKeysRD();
			sortKey.setSequenceNo(viewSortKey.getKeySequenceNo());
			Code footerOptionCode = viewSortKey.getFooterOptionCode();
			if (footerOptionCode != null) {
				if (footerOptionCode.getGeneralId() == Codes.SUPPRESS_PRINT) {
					sortKey.setSortBrk(0);
				} else if (footerOptionCode.getGeneralId() == Codes.PRINT) {
					sortKey.setSortBrk(1);
				}
			}
			sortKey.setColumnNo(viewSortKey.getViewColumn().getColumnNo());
			sortKey.setStartPosition(viewSortKey.getStartPosition());
			sortKey
					.setSortSequence(viewSortKey.getSortSequenceCode() != null ? viewSortKey
							.getSortSequenceCode().getDescription()
							: "");
			sortKey.setSortKeyLabel(viewSortKey.getSortkeyLabel() == null ? ""
					: viewSortKey.getSortkeyLabel());
			sortKey
					.setDataType(viewSortKey.getDataTypeCode() != null ? viewSortKey
							.getDataTypeCode().getDescription()
							: "");
			sortKey
					.setDateTimeFormat(viewSortKey.getDateTimeFormatCode() != null ? viewSortKey
							.getDateTimeFormatCode().getDescription()
							: "");
			sortKey.setLength(viewSortKey.getLength());
			sortKey.setDecimalPlaces(viewSortKey.getDecimalPlaces());
			sortKey.setSigned(viewSortKey.isSigned());
			Boolean flag = false;

			// CQ 8121. Nikita. 22/06/2010. Sort Key Titles was not being set
			// properly in the report data
			List<SortKeyTitlesRD> sortKeyTitles = new ArrayList<SortKeyTitlesRD>();
			List<ViewColumnSource> columnSources = viewSortKey.getViewColumn()
					.getViewColumnSources().getActiveItems();
			int sourceNo = 0;
			for (ViewColumnSource columnSource : columnSources) {
				sourceNo++;
				if (columnSource.getSortKeyTitleLRField() != null) {
					if (!flag) {
						flag = true;
					}
					SortKeyTitlesRD sortKeyTitle = new SortKeyTitlesRD();
					sortKeyTitle.setViewSourceNo(sourceNo);

					if (columnSource.getSortKeyTitleLogicalRecordQueryBean() != null
							&& columnSource
									.getSortKeyTitleLookupPathQueryBean() != null
							&& columnSource.getSortKeyTitleLRField() != null) {
						sortKeyTitle
								.setSortKeyTitleFieldString(UIUtilities
										.getComboString(
												columnSource
														.getSortKeyTitleLogicalRecordQueryBean()
														.getName(),
												columnSource
														.getSortKeyTitleLogicalRecordQueryBean()
														.getId())
										+ "."
										+ UIUtilities
												.getComboString(
														columnSource
																.getSortKeyTitleLookupPathQueryBean()
																.getName(),
														columnSource
																.getSortKeyTitleLookupPathQueryBean()
																.getId())
										+ "."
										+ UIUtilities
												.getComboString(
														columnSource
																.getSortKeyTitleLRField()
																.getName(),
														columnSource
																.getSortKeyTitleLRField()
																.getId()));
					} else {
						sortKeyTitle.setSortKeyTitleFieldString("");
					}

					Code effectiveDateTypeCode = columnSource
							.getEffectiveDateTypeCode();
					sortKeyTitle
							.setEffectiveDateType(effectiveDateTypeCode != null ? effectiveDateTypeCode
									.getDescription()
									: "");
					if (effectiveDateTypeCode != null) {
						if (effectiveDateTypeCode.getGeneralId() == Codes.RELPERIOD_SOURCE_FILE_FIELD) {
							LRField effectiveDateLRField = columnSource
									.getEffectiveDateLRField();
							sortKeyTitle
									.setEffectiveDateValue(effectiveDateLRField == null ? ""
											: UIUtilities.getComboString(
													effectiveDateLRField
															.getName(),
													effectiveDateLRField
															.getId()));
						} else {
							sortKeyTitle.setEffectiveDateValue(columnSource
									.getEffectiveDateValue());
						}
					} else {
						sortKeyTitle.setEffectiveDateValue("");
					}

					sortKeyTitles.add(sortKeyTitle);
				}
			}
			sortKey.setSortKeyTitles(sortKeyTitles);
			if (flag) {
				sortKey.setTitle(true);
			} else {
				sortKey.setTitle(false);
			}

			sortKeys.add(sortKey);
		}
		return sortKeys;
	}

	/************************************************************************************************************************/
	/************************************************************************************************************************/

	public List<Integer> getViewIds() {
		// CQ 8165. Nikita. 01/07/2010
		// generate report based on original order in which View ids were passed
		return viewIDs;
	}

	public GeneralViewPropertiesRD getGeneralViewPropertiesReportData(
			Integer viewId) {
		return (GeneralViewPropertiesRD) viewPropertiesMap.get(viewId).get(0);
	}

	@SuppressWarnings("unchecked")
    public List<ViewSourcesRD> getViewSourcesReportData(Integer viewId) {
		return (List<ViewSourcesRD>) viewPropertiesMap.get(viewId).get(1);
	}

	@SuppressWarnings("unchecked")
    public List<ViewColumnMappingRD> getViewColumnMappingReportData(
			Integer viewId, Integer srcNo) {
        List<ViewSourcesRD> sources = (List<ViewSourcesRD>) viewPropertiesMap
				.get(viewId).get(1);
		return sources.get(srcNo - 1).getViewColumnMaps();
	}

	@SuppressWarnings("unchecked")
    public List<ViewColumnAttributesRD> getViewColumnAttributesReportData(
			Integer viewId) {
		return (List<ViewColumnAttributesRD>) viewPropertiesMap.get(viewId)
				.get(2);
	}

	@SuppressWarnings("unchecked")
    public List<ViewColumnAttributesRD> getFormatPhaseCalculationReportData(
			Integer viewId) {
		List<ViewColumnAttributesRD> columns = (List<ViewColumnAttributesRD>) viewPropertiesMap
				.get(viewId).get(2);
		List<ViewColumnAttributesRD> calcs = new ArrayList<ViewColumnAttributesRD>();
		for (ViewColumnAttributesRD col : columns) {
			if (col.getFormatPhaseCalculation() != null) {
				calcs.add(col);
			}
		}
		return calcs;
	}

	@SuppressWarnings("unchecked")
    public List<SortKeysRD> getSortKeysReportData(Integer viewId) {
		return (List<SortKeysRD>) viewPropertiesMap.get(viewId).get(3);
	}

	public List<SortKeysRD> getSortKeysForTitlesReportData(Integer viewId) {
		@SuppressWarnings("unchecked")
        List<SortKeysRD> sortKeys = (List<SortKeysRD>) viewPropertiesMap.get(
				viewId).get(3);
		List<SortKeysRD> sortKeysForTitles = new ArrayList<SortKeysRD>();
		for (SortKeysRD sortKey : sortKeys) {
			if (!sortKey.getTitle().equals("False")) {
				sortKeysForTitles.add(sortKey);
			}
		}
		return sortKeysForTitles;
	}

	@SuppressWarnings("unchecked")
    public List<SortKeyTitlesRD> getSortKeyTitlesReportData(Integer viewId,
			Integer sortKeyNo) {
		List<SortKeysRD> sortKeys = (List<SortKeysRD>) viewPropertiesMap.get(
				viewId).get(3);
		return sortKeys.get(sortKeyNo - 1).getSortKeyTitles();
	}

	/************************************************************************************************************************/
	/************************************************************************************************************************/

	public class GeneralViewPropertiesRD {

		private String view;
		private String status;

		private String viewFolder;
		private String outputFormat;
		private String type;
		private String formatPhase;
		private String outputLR;

		private String outputLogicalFile;
		private String outputPhysicalFile;
		private String extractOutputRecordLimit;
		private String formatOutputRecordLimit;

		private String linesPerPage;
		private String reportWidth;

		private String controlRecord;
		private String writeProcedure;
		private String writeParam;
		private String formatProcedure;
		private String formatParam;
		private String extractAggregationBufferSize;
		private String suppressZeros;

		private Integer sourceCount;
		private Integer columnCount;
		private Integer visibleColumnCount;
		private Integer sortKeyCount;

		public String getView() {
			return view;
		}

		public void setView(String view) {
			this.view = view;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getFormatPhase() {
			return formatPhase;
		}

		public void setFormatPhase(String formatPhase) {
			this.formatPhase = formatPhase;
		}

		public String getViewFolder() {
			return viewFolder;
		}

		public void setViewFolder(String viewFolder) {
			this.viewFolder = viewFolder;
		}

		public String getOutputFormat() {
			return outputFormat;
		}

		public void setOutputFormat(String outputFormat) {
			this.outputFormat = outputFormat;
		}

		public String getOutputLogicalFile() {
			return outputLogicalFile;
		}

		public void setOutputLogicalFile(String outputLogicalFile) {
			this.outputLogicalFile = outputLogicalFile;
		}

		public String getOutputPhysicalFile() {
			return outputPhysicalFile;
		}

		public void setOutputPhysicalFile(String outputPhysicalFile) {
			this.outputPhysicalFile = outputPhysicalFile;
		}

		public String getOutputLR() {
			return outputLR;
		}

		public void setOutputLR(String outputLR) {
			this.outputLR = outputLR;
		}

		public String getExtractOutputRecordLimit() {
			return extractOutputRecordLimit;
		}

		public void setExtractOutputRecordLimit(String extractOutputRecordLimit) {
			this.extractOutputRecordLimit = extractOutputRecordLimit;
		}

		public String getFormatOutputRecordLimit() {
			return formatOutputRecordLimit;
		}

		public void setFormatOutputRecordLimit(String formatOutputRecordLimit) {
			this.formatOutputRecordLimit = formatOutputRecordLimit;
		}

		public String getLinesPerPage() {
			return linesPerPage;
		}

		public void setLinesPerPage(String linesPerPage) {
			this.linesPerPage = linesPerPage;
		}

		public String getReportWidth() {
			return reportWidth;
		}

		public void setReportWidth(String reportWidth) {
			this.reportWidth = reportWidth;
		}

		public String getControlRecord() {
			return controlRecord;
		}

		public void setControlRecord(String controlRecord) {
			this.controlRecord = controlRecord;
		}

		public String getWriteProcedure() {
			return writeProcedure;
		}

		public void setWriteProcedure(String writeProcedure) {
			this.writeProcedure = writeProcedure;
		}

		public String getWriteParam() {
			return writeParam;
		}

		public void setWriteParam(String writeParam) {
			this.writeParam = writeParam;
		}

		public String getFormatProcedure() {
			return formatProcedure;
		}

		public void setFormatProcedure(String formatProcedure) {
			this.formatProcedure = formatProcedure;
		}

		public String getFormatParam() {
			return formatParam;
		}

		public void setFormatParam(String formatParam) {
			this.formatParam = formatParam;
		}

		public String getExtractAggregationBufferSize() {
			return extractAggregationBufferSize;
		}

		public void setExtractAggregationBufferSize(
				String extractAggregationBufferSize) {
			this.extractAggregationBufferSize = extractAggregationBufferSize;
		}

		public String getSuppressZeros() {
			return suppressZeros;
		}

		public void setSuppressZeros(String suppressZeros) {
			this.suppressZeros = suppressZeros;
		}

		public Integer getSourceCount() {
			return sourceCount;
		}

		public void setSourceCount(Integer sourceCount) {
			this.sourceCount = sourceCount;
		}

		public Integer getColumnCount() {
			return columnCount;
		}

		public void setColumnCount(Integer columnCount) {
			this.columnCount = columnCount;
		}

		public Integer getVisibleColumnCount() {
			return visibleColumnCount;
		}

		public void setVisibleColumnCount(Integer visibleColumnCount) {
			this.visibleColumnCount = visibleColumnCount;
		}

		public Integer getSortKeyCount() {
			return sortKeyCount;
		}

		public void setSortKeyCount(Integer sortKeyCount) {
			this.sortKeyCount = sortKeyCount;
		}

	}

	/************************************************************************************************************************/

	/************************************************************************************************************************/
	public class ViewSourcesRD {
		private Integer viewSourceNo;
		private String viewSource;
		private String extractRecordFilter;
		private List<ViewColumnMappingRD> viewColumnMaps = new ArrayList<ViewColumnMappingRD>();

		public Integer getViewSourceNo() {
			return viewSourceNo;
		}

		public void setViewSourceNo(Integer viewSourceNo) {
			this.viewSourceNo = viewSourceNo;
		}

		public String getViewSource() {
			if (viewSource != null) {
				return viewSource;
			}
			return "";
		}

		public void setViewSource(String viewSource) {
			this.viewSource = viewSource;
		}

		public String getExtractRecordFilter() {
			return extractRecordFilter;
		}

		public void setExtractRecordFilter(String extractRecordFilter) {
			this.extractRecordFilter = extractRecordFilter;
		}

		public List<ViewColumnMappingRD> getViewColumnMaps() {
			return viewColumnMaps;
		}

		public void setViewColumnMaps(List<ViewColumnMappingRD> viewColumnMaps) {
			this.viewColumnMaps = viewColumnMaps;
		}

	}

	/************************************************************************************************************************/

	public class ViewColumnMappingRD {
		private Integer columnNo;
		private String columnType;
		private String columnValue;

		public Integer getColumnNo() {
			return columnNo;
		}

		public void setColumnNo(Integer columnNo) {
			this.columnNo = columnNo;
		}

		public String getColumnType() {
			return columnType;
		}

		public void setColumnType(String columnType) {
			this.columnType = columnType;
		}

		public String getColumnValue() {
			if (columnValue != null) {
				return columnValue;
			}
			return "";
		}

		public void setColumnValue(String columnValue) {
			this.columnValue = columnValue;
		}

	}

	/************************************************************************************************************************/

	public class ViewColumnAttributesRD {
		private Integer columnNo;
		private Integer spacesBeforeColumn;
		private Integer startPosition;
		private String dataType;
		private String dateTimeFormat;
		private Integer length;
		private String dataAlignment;
		private Integer decimalPlaces;
		private Integer scalingFactor;
		private Boolean signed;
		private Boolean visible;
		private Boolean sortKey;
		private String formatPhaseCalculation;

		public Integer getColumnNo() {
			return columnNo;
		}

		public void setColumnNo(Integer columnNo) {
			this.columnNo = columnNo;
		}

		public Integer getSpacesBeforeColumn() {
			return spacesBeforeColumn;
		}

		public void setSpacesBeforeColumn(Integer spacesBeforeColumn) {
			this.spacesBeforeColumn = spacesBeforeColumn;
		}

		public Integer getStartPosition() {
			return startPosition;
		}

		public void setStartPosition(Integer startPosition) {
			this.startPosition = startPosition;
		}

		public String getDataType() {
			return dataType;
		}

		public void setDataType(String dataType) {
			this.dataType = dataType;
		}

		public String getDateTimeFormat() {
			return dateTimeFormat;
		}

		public void setDateTimeFormat(String dateTimeFormat) {
			this.dateTimeFormat = dateTimeFormat;
		}

		public Integer getLength() {
			return length;
		}

		public void setLength(Integer length) {
			this.length = length;
		}

		public String getDataAlignment() {
			return dataAlignment;
		}

		public void setDataAlignment(String dataAlignment) {
			this.dataAlignment = dataAlignment;
		}

		public Integer getDecimalPlaces() {
			return decimalPlaces;
		}

		public void setDecimalPlaces(Integer decimalPlaces) {
			this.decimalPlaces = decimalPlaces;
		}

		public Integer getScalingFactor() {
			return scalingFactor;
		}

		public void setScalingFactor(Integer scalingFactor) {
			this.scalingFactor = scalingFactor;
		}

		public String isSigned() {
			if (signed) {
				return "True";
			} else {
				return "False";
			}
		}

		public void setSigned(Boolean signed) {
			this.signed = signed;
		}

		public String isVisible() {
			if (visible) {
				return "True";
			} else {
				return "False";
			}
		}

		public void setVisible(Boolean visible) {
			this.visible = visible;
		}

		public String isSortKey() {
			if (sortKey) {
				return "True";
			} else {
				return "False";
			}
		}

		public void setSortKey(Boolean sortKey) {
			this.sortKey = sortKey;
		}

		public String getFormatPhaseCalculation() {
			return formatPhaseCalculation;
		}

		public void setFormatPhaseCalculation(String formatPhaseCalculation) {
			this.formatPhaseCalculation = formatPhaseCalculation;
		}
	}

	/************************************************************************************************************************/

	public class SortKeysRD {
		private Integer sequenceNo;
		private Integer columnNo;
		private Integer startPosition;
		private String sortSequence;
		private Integer sortBrk;
		private String sortKeyLabel;
		private String dataType;
		private String dateTimeFormat;
		private Integer length;
		private Integer decimalPlaces;
		private Boolean signed;
		private Boolean title;
		private List<SortKeyTitlesRD> sortKeyTitles = new ArrayList<SortKeyTitlesRD>();

		public Integer getSequenceNo() {
			return sequenceNo;
		}

		public void setSequenceNo(Integer sequenceNo) {
			this.sequenceNo = sequenceNo;
		}

		public Integer getColumnNo() {
			return columnNo;
		}

		public void setColumnNo(Integer columnNo) {
			this.columnNo = columnNo;
		}

		public Integer getStartPosition() {
			return startPosition;
		}

		public void setStartPosition(Integer startPosition) {
			this.startPosition = startPosition;
		}

		public String getSortSequence() {
			return sortSequence;
		}

		public void setSortSequence(String sortSequence) {
			this.sortSequence = sortSequence;
		}

		public Integer getSortBrk() {
			return sortBrk;
		}

		public void setSortBrk(Integer sortBrk) {
			this.sortBrk = sortBrk;
		}

		public String getSortKeyLabel() {
			return sortKeyLabel;
		}

		public void setSortKeyLabel(String sortKeyLabel) {
			this.sortKeyLabel = sortKeyLabel;
		}

		public String getDataType() {
			return dataType;
		}

		public void setDataType(String dataType) {
			this.dataType = dataType;
		}

		public String getDateTimeFormat() {
			return dateTimeFormat;
		}

		public void setDateTimeFormat(String dateTimeFormat) {
			this.dateTimeFormat = dateTimeFormat;
		}

		public Integer getLength() {
			return length;
		}

		public void setLength(Integer length) {
			this.length = length;
		}

		public Integer getDecimalPlaces() {
			return decimalPlaces;
		}

		public void setDecimalPlaces(Integer decimalPlaces) {
			this.decimalPlaces = decimalPlaces;
		}

		public String isSigned() {
			if (signed) {
				return "True";
			} else {
				return "False";
			}
		}

		public void setSigned(Boolean signed) {
			this.signed = signed;
		}

		public String getTitle() {
			if (title) {
				return "True";
			} else {
				return "False";
			}
		}

		public void setTitle(Boolean title) {
			this.title = title;
		}

		public List<SortKeyTitlesRD> getSortKeyTitles() {
			return sortKeyTitles;
		}

		public void setSortKeyTitles(List<SortKeyTitlesRD> sortKeyTitles) {
			this.sortKeyTitles = sortKeyTitles;
		}
	}

	public class SortKeyTitlesRD {
		private Integer viewSourceNo;
		private String sortKeyTitleFieldString;
		private String effectiveDateType;
		private String effectiveDateValue;

		public Integer getViewSourceNo() {
			return viewSourceNo;
		}

		public void setViewSourceNo(Integer viewSourceNo) {
			this.viewSourceNo = viewSourceNo;
		}

		public void setSortKeyTitleFieldString(String sortKeyTitleFieldString) {
			this.sortKeyTitleFieldString = sortKeyTitleFieldString;
		}

		public String getSortKeyTitleFieldString() {
			return sortKeyTitleFieldString;
		}

		public String getEffectiveDateType() {
			return effectiveDateType;
		}

		public void setEffectiveDateType(String effectiveDateType) {
			this.effectiveDateType = effectiveDateType;
		}

		public void setEffectiveDateValue(String effectiveDateValue) {
			this.effectiveDateValue = effectiveDateValue;
		}

		public String getEffectiveDateValue() {
			return effectiveDateValue;
		}
	}

	/************************************************************************************************************************/

	public List<String> getErrors() {
		return errorMsgList;
	}

	public boolean hasData() {
		return (!this.viewPropertiesMap.isEmpty());
	}

	/************************************************************************************************************************/
}
