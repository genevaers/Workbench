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
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class ViewColumnPICReport implements IReportData {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.ViewColumnPICReport");

    private Map<Integer, GeneralViewRD> viewColumnLayoutMap = new HashMap<Integer, GeneralViewRD>();
	private List<String> errorMsgList = new ArrayList<String>();
	private List<Integer> viewIDs = new ArrayList<Integer>();

	public ViewColumnPICReport(List<Integer> viewIDs) throws SAFRException {
		for (int id : viewIDs) {
			loadData(null, id);

		}

	}

	public ViewColumnPICReport(View view) throws SAFRException {
		loadData(view, view.getId());
	}

	private void loadData(View view, Integer id) {
		try {
			if (view == null) {
				view = SAFRApplication.getSAFRFactory().getView(id);
			}

			GeneralViewRD generalViewProperties = new GeneralViewRD(view);
			// loop on view columns
			for (ViewColumn viewColumn : view.getViewColumns().getActiveItems()) {
				ViewColumnPropertiesRD viewColProperties = new ViewColumnPropertiesRD(
						viewColumn);
				generalViewProperties.addColumn(viewColProperties);
			}
			// add to map
			viewColumnLayoutMap.put(view.getId(), generalViewProperties);
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

	public class GeneralViewRD {
		private String viewName;
		private Integer viewId;
		private Code status;
		List<ViewColumnPropertiesRD> viewColumnList = new ArrayList<ViewColumnPropertiesRD>();
		private String name;

		public GeneralViewRD(View view) {

			name = (view.getName() == null ? "" : view.getName());
			viewId = view.getId();
			viewName = UIUtilities.getComboString(name, viewId);
			status = view.getStatusCode();
		}

		public String getViewName() {
			return viewName;
		}

		public void setViewName(String viewName) {
			this.viewName = viewName;
		}

		public Integer getViewId() {
			return viewId;
		}

		public void setViewId(Integer viewId) {
			this.viewId = viewId;
		}

		public String getStatus() {
			if (status.getGeneralId() == Codes.INACTIVE)
				return "INACTIVE";
			else if (status.getGeneralId() == Codes.ACTIVE) {
				return "ACTIVE";
			}
			return null;
		}

		public void setStatus(Code status) {
			this.status = status;
		}

		public void addColumn(ViewColumnPropertiesRD viewCol) {
			viewColumnList.add(viewCol);
		}

		public List<ViewColumnPropertiesRD> getViewColumnList() {
			return viewColumnList;
		}

	}

	/************************************************************************************************************************/

	public class ViewColumnPropertiesRD {

		private String description;
		private Integer columnno;
		private String pictureClause;

		public ViewColumnPropertiesRD(ViewColumn column) {
			columnno = column.getColumnNo();
			String desc = "";

			// desc = column.getHeading1()== null ? "":column.getHeading1()
			// + (column.getHeading2() == null||column.getHeading2().equals("")
			// ? "" : " ")
			// + column.getHeading2() + (column.getHeading3() ==
			// null||column.getHeading3().equals("") ? "" : " ") +
			// column.getHeading3();

			desc = "";
			if (column.getHeading1() != null
					&& !column.getHeading1().equals("")) {
				desc = desc + column.getHeading1() + " ";
			}
			if (column.getHeading2() != null
					&& !column.getHeading2().equals("")) {
				desc = desc + column.getHeading2() + " ";
			}
			if (column.getHeading3() != null
					&& !column.getHeading3().equals("")) {
				desc = desc + column.getHeading3();
			}
			description = desc.trim();
			pictureClause = getPictureClauseForViewColumn(column);
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Integer getColumnno() {
			return columnno;
		}

		public void setColumnno(Integer columnno) {
			this.columnno = columnno;
		}

		public String getPictureClause() {
			return pictureClause;
		}

		public void setPictureClause(String pictureClause) {
			this.pictureClause = pictureClause;
		}

	}

	/************************************************************************************************************************/

	/**
	 * This method is used to get view column properties and basic view
	 * information of all the selected view.
	 * 
	 * @return a list of GeneralViewPropertiesRD
	 */
	public List<GeneralViewRD> getViewsReportData() {
		List<GeneralViewRD> viewColumnLayoutList = new ArrayList<GeneralViewRD>();
		// CQ 8165. Nikita. 01/07/2010
		// generate report based on original order in which View ids were passed
		for (Integer id : viewIDs) {
			viewColumnLayoutList.add(viewColumnLayoutMap.get(id));
		}
		return viewColumnLayoutList;
	}

	/**
	 * This method is used to get the properties of the columns of specified
	 * view.
	 * 
	 * @param ViewId
	 *            : the id of the view for which the column properties are to be
	 *            retrieved.
	 * @return a list of .
	 * 
	 */
	public List<ViewColumnPropertiesRD> getViewColumnReportData(Integer viewId) {
		if (!viewColumnLayoutMap.isEmpty()) {
			GeneralViewRD viewColumnLayoutList = viewColumnLayoutMap
					.get(viewId);

			return viewColumnLayoutList.getViewColumnList();

		}
		return null;
	}

	public String getPictureClauseForViewColumn(ViewColumn viewColumn) {

		String picClause = "PIC ";
		Code dataType = viewColumn.getDataTypeCode();
		Integer decimalPlaces = viewColumn.getDecimals();
		Integer length = viewColumn.getLength();
		Boolean signed = viewColumn.isSigned();

		switch (dataType.getGeneralId().intValue()) {
		case Codes.ALPHANUMERIC:
			picClause += "X" + "(" + length + ")";
			break;
		case Codes.PACKED:
		case Codes.PACKED_SORTABLE:
			// double the length as its packed.
			length *= 2;
			// reserve one nibble for sign for both signed and unsigned for
			// packed number.
			length -= 1;

			picClause += (signed ? "S" : "")
					+ adjustLengthAndDecimals(length, decimalPlaces)
					+ " COMP-3";
			break;
		case Codes.BINARY:
		case Codes.BINARY_SORTABLE:
			if (length == 0) {
				picClause = "Length is zero";
			} else if (length > 8) {
				picClause = "Length is greater than 8 bytes";
			} else {
				if (length >= 1 && length <= 2) {
					length = 4;
				} else if (length >= 3 && length <= 4) {
					length = 9;
				} else if (length >= 5 && length <= 8) {
					length = 18;
				}

				boolean decimals = false;
				if (decimalPlaces > 0) {
					if (decimalPlaces > length) {
						picClause = "Too many decimal places for length";
						break;
					} else {
						decimals = true;
						length -= decimalPlaces;
					}
				}

				picClause += (signed ? "S" : "") + "9(" + length + ")"
						+ (decimals ? "V9(" + decimalPlaces + ")" : "")
						+ " COMP";
			}
			break;
		default:
			picClause += (signed ? "S" : "")
					+ adjustLengthAndDecimals(length, decimalPlaces);
			break;
		}

		return picClause;

	}

	public String adjustLengthAndDecimals(Integer length, Integer decimals) {

		String str = "";
		if (length >= decimals) {
			str += "9" + "(" + (length - decimals) + ")";
		} else {
			str += "9" + "(" + (length) + ")";
		}
		if (decimals > 0) {
			str += "V9" + "(" + decimals + ")";
		}
		return str;
	}

	public List<String> getErrors() {

		return errorMsgList;
	}

	public boolean hasData() {

		return (!this.viewColumnLayoutMap.isEmpty());
	}

}
