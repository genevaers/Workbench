package com.ibm.safr.we.model.view;

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
import java.util.List;

import com.ibm.safr.we.SAFRImmutableList;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.ModelUtilities;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRField;
import com.ibm.safr.we.model.base.SAFRComponent;

public class ViewColumn extends SAFRField {

	/*
	 * The following columns of xov_viewlrfldattr are represented in super
	 * classes of this class.
	 */
	// CREATEDTIMESTAMP
	// CREATEDUSERID
	// LASTMODTIMESTAMP
	// LASTMODUSERID
	// VIEWCOLUMNID
	// ENVIRONID
	// FLDFMTCD
	// MAXLEN
	// DECIMALCNT
	// SIGNEDIND
	// ROUNDING
	// FLDCONTENTCD
	// HDRJUSTIFYCD
	// HDRLINE1
	// HDRLINE2
	// HDRLINE3
	// RPTMASK
	// DEFAULTVAL
	// VIEWID
	// COLUMNNUMBER
	// STARTPOSITION
	// JUSTIFYCD
	// VISIBLE
	// SPACESBEFORECOLUMN
	// SUBTOTALTYPECD
	/* data visible at the UI */
	private View view; // VIEWID
	private Integer columnNo; // COLUMNNUMBER
	private Integer startPosition; // STARTPOSITION
	private Code dataAlignmentCode; // JUSTIFYCD
	private Boolean visible; // VISIBLE
	private Integer spacesBeforeColumn; // SPACESBEFORECOLUMN
	private String sortkeyFooterLabel; // SUBTLABEL (TBC also appears in VIEWSORTKEY)

	/* internal data (no public accessor methods) */
	private Integer ordinalPosition; // ORDINALPOSITION
	private Code subtotalTypeCode; // SUBTOTALTYPECD
	private Code extractAreaCode; // EXTRACTAREACD
	private Integer extractAreaPosition; // EXTRACTAREAPOSITION
	private Code recordAggrCode;
	private Code groupAggrCode;

	// will loaded in View.java using zipper
	private String formatColumnCalculation;
	// taken from compiler during view activation and will be stored in DB after
	// converting to blobs using SAFRBlobs.dll
	private byte[] compiledFormatColumnCalculation;

	private List<String> loadWarnings;

	/**
	 * This constructor is used when creating a new ViewColumn for the parent
	 * View. The Environment ID will be set to the same Environment as the
	 * parent View. The ViewColumn ID will be initialized to zero and then set
	 * to a unique value when the ViewColumn object is persisted via its
	 * <code>store()</code> method.
	 * 
	 * @throws SAFRException
	 */
	ViewColumn(View parentView) {
		super(parentView.getEnvironmentId());
		this.view = parentView;
		super.setLength(1);
		if (parentView.getOutputFormat() == OutputFormat.Format_Report) {
			this.spacesBeforeColumn = 2;
		} else {
			this.spacesBeforeColumn = 0;
		}
		this.visible = true;
		Code code = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.DATATYPE).getCode(Codes.ALPHANUMERIC);
		this.setDataTypeCode(code);
		this.recordAggrCode = null;
		this.groupAggrCode = null;
	}

	/**
	 * Create a ViewColumn object containing the data in the specified transfer
	 * object. Used to instantiate existing ViewColumn objects.
	 * 
	 * @param trans
	 *            the ViewColumnTransfer object
	 */
	ViewColumn(View parentView, ViewColumnTransfer trans) throws DAOException,
			SAFRException {
		super(trans);
		this.view = parentView;
		if (!isForImport()) {
			// the subtotal type code is broken into record aggregation and
			// Group
			// aggregation depending on the values of Output Format,if format
			// phase
			// record aggregation is on, the subtotal type code.

			this.breakSubtotalCodeIntoAggregationFunctions(parentView
					.getOutputFormat(), parentView
					.isFormatPhaseRecordAggregationOn(),
					subtotalTypeCode == null ? null : subtotalTypeCode
							.getGeneralId().intValue());

			// Get load warnings, if any, from super class.
			if (!super.getLoadWarningProperties().isEmpty()) {
				for (SAFRField.Property property : super
						.getLoadWarningProperties()) {
					loadWarnings.add("View Column " + this.columnNo
							+ " does not have a valid " + property.getText()
							+ ". Please select a valid " + property.getText()
							+ ", if required, before saving.");
				}
			}
		}
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		ViewColumnTransfer trans = (ViewColumnTransfer) safrTrans;

		loadWarnings = new ArrayList<String>();

		this.columnNo = trans.getColumnNo();
		this.startPosition = trans.getStartPosition();
		try {
			this.dataAlignmentCode = ModelUtilities.getCodeFromKey(
					CodeCategories.JUSTIFY, trans.getDataAlignmentCode());
		} catch (IllegalArgumentException iae) {
			loadWarnings
					.add("View Column "
							+ this.columnNo
							+ " does not have a valid data alignment. Please select a valid data alignment, if required, before saving.");
			this.dataAlignmentCode = null;
		}
		this.visible = trans.isVisible();
		this.spacesBeforeColumn = trans.getSpacesBeforeColumn();
		this.sortkeyFooterLabel = trans.getSortkeyFooterLabel();
		this.ordinalPosition = trans.getOrdinalPosition();
		try {
			this.subtotalTypeCode = ModelUtilities.getCodeFromKey(
					CodeCategories.SUBTOT, trans.getSubtotalTypeCode());
		} catch (IllegalArgumentException iae) {
			this.subtotalTypeCode = null;
		}

		try {
			this.extractAreaCode = ModelUtilities.getCodeFromKey(
					CodeCategories.EXTRACT, trans.getExtractAreaCode());
		} catch (IllegalArgumentException iae) {
			this.extractAreaCode = null;
		}
		this.extractAreaPosition = trans.getExtractAreaPosition();
	    this.formatColumnCalculation = trans.getFormatColumnLogic();	
	}

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		ViewColumnTransfer trans = (ViewColumnTransfer) safrTrans;
		trans.setViewId(view.getId());
		trans.setColumnNo(columnNo);
		trans.setStartPosition(startPosition);
		trans.setDataAlignmentCode(dataAlignmentCode == null ? null
				: dataAlignmentCode.getKey());
		trans.setVisible(visible);
		trans.setSpacesBeforeColumn(spacesBeforeColumn);
		if (this.isSortKey()) {
			trans.setSortkeyFooterLabel(sortkeyFooterLabel);
		}
		trans.setOrdinalPosition(ordinalPosition);
		if (!isForImport()) {
			this.calculateSubtotalCodeUsingAggregationFunctions(view
					.getOutputFormat(),
					view.isFormatPhaseRecordAggregationOn(),
					getRecordAggregationCode(),
					getGroupAggregationCode());
		}
		trans.setSubtotalTypeCode(subtotalTypeCode == null ? null
				: subtotalTypeCode.getKey());
		trans.setExtractAreaCode(extractAreaCode == null ? null
				: extractAreaCode.getKey());
		// if this column is a sort key, then extract area position is the start
		// position of sort key.
		trans.setExtractAreaPosition(isSortKey() ? getViewSortKey()
				.getStartPosition() : extractAreaPosition);
		trans.setFormatColumnLogic(formatColumnCalculation);
	}

	@Override
	public void setLength(Integer length) {
		super.setLength(length);
		view.calculateStartPosition();
		view.makeViewInactive();
        view.markUpdated();
	}

	@Override
	public void setDataTypeCode(Code dataType) {
		if (dataType == null) {
			throw new NullPointerException("Data Type cannot be null."); // mandatory
			// field
		}
		super.setDataTypeCode(dataType);
		if (dataType.getGeneralId() != Codes.MASKED_NUMERIC) {
			setNumericMaskCode(null);
		}

		if (dataType.getGeneralId() != Codes.ALPHANUMERIC) {
			if (getNumericMaskCode() == null) {
				Code code = SAFRApplication.getSAFRFactory().getCodeSet(
						CodeCategories.FORMATMASK).getCode(
						Codes.DEFAULTNUMERICMASK);
				setNumericMaskCode(code);
			}
		} else {
			setNumericMaskCode(null);
		}
		switch (dataType.getGeneralId().intValue()) {
		case Codes.ALPHANUMERIC:
			super.setDecimals(0);
			super.setSigned(false);
			super.setScaling(0);
			// formatColumnCalculation = null;
			setRecordAggregationCode(null);
			setGroupAggregationCode(null);
			break;
		case Codes.BINARY_CODED_DECIMAL:
			setSigned(false);
			break;
		case Codes.EDITED_NUMERIC:
			setSigned(true);
			setDateTimeFormatCode(null);
			break;
		// CQ 8056. Nikita. 17/06/2010. Date/Time Format should be cleared if
		// the data type is Masked Numeric.
		case Codes.MASKED_NUMERIC:
			setDateTimeFormatCode(null);
			break;
		case Codes.BINARY:
			if (this.getDateTimeFormatCode() != null) {
				setSigned(false);
			}
			break;
		}
		view.makeViewInactive();
        view.markUpdated();
		markModified();
	}

	@Override
	public void setDecimals(Integer decimals) {
		super.setDecimals(decimals);
		view.makeViewInactive();
        view.markUpdated();
	}

	@Override
	public void setDateTimeFormatCode(Code dateTimeFormat) {
		super.setDateTimeFormatCode(dateTimeFormat);

		if (dateTimeFormat != null
				&& this.getDataTypeCode().getGeneralId() == Codes.BINARY) {
			setSigned(false);
		}
		view.makeViewInactive();
        view.markUpdated();
	}

	@Override
	public void setHeading1(String heading1) {
		super.setHeading1(heading1);
		view.makeViewInactive();
        view.markUpdated();
	}

	@Override
	public void setHeading2(String heading2) {
		super.setHeading2(heading2);
		view.makeViewInactive();
        view.markUpdated();
	}

	@Override
	public void setHeading3(String heading3) {
		super.setHeading3(heading3);
		view.makeViewInactive();
        view.markUpdated();
	}

	@Override
	public void setNumericMaskCode(Code numericMask) {
		super.setNumericMaskCode(numericMask);
		view.makeViewInactive();
        view.markUpdated();
	}

	@Override
	public void setScaling(Integer scaling) {
		super.setScaling(scaling);
		view.makeViewInactive();
        view.markUpdated();
	}

	@Override
	public void setHeaderAlignmentCode(Code headerAlignment) {
		super.setHeaderAlignmentCode(headerAlignment);
		view.makeViewInactive();
        view.markUpdated();
	}

	@Override
	public void setSigned(Boolean signed) {
		super.setSigned(signed);
		view.makeViewInactive();
        view.markUpdated();
	}
	
	@Override
	public void setDefaultValue(String defaultValue) {
		// TC18596 only modify if value has changed
		if ((getDefaultValue() == null && defaultValue != null)
				|| (getDefaultValue() != null && !getDefaultValue().equals(
						defaultValue))) {
			super.setDefaultValue(defaultValue);
			view.makeViewInactive();
	        view.markUpdated();
		}
	}

	/**
	 * @return the view
	 */
	public View getView() {
		return view;
	}

	/**
	 * @return the columnNo
	 */
	public Integer getColumnNo() {
		return columnNo;
	}

	/**
	 * @param columnNo
	 *            the columnNo to set
	 */
	public void setColumnNo(Integer columnNo) {
		//TC18596 only modify if value has changed
		if ((this.columnNo == null && columnNo != null)
				|| (this.columnNo != null && !this.columnNo.equals(columnNo))) {
			this.columnNo = columnNo;
			markModified();
			view.makeViewInactive();
	        view.markUpdated();
		}
	}

	/**
	 * @return the startPosition
	 */
	public Integer getStartPosition() {
		return startPosition;
	}

	/**
	 * @param startPosition
	 *            the startPosition to set
	 */
	public void setStartPosition(Integer startPosition) {
		// TC18596 only modify if value has changed
		if ((this.startPosition == null && startPosition != null)
				|| (this.startPosition != null && !this.startPosition
						.equals(startPosition))) {
			this.startPosition = startPosition;
			markModified();
			view.makeViewInactive();
	        view.markUpdated();
		}
	}

	/**
	 * @return the dataAlignmentCode
	 */
	public Code getDataAlignmentCode() {
		return dataAlignmentCode;
	}

	/**
	 * @param dataAlignmentCode
	 *            the dataAlignmentCode to set
	 */
	public void setDataAlignmentCode(Code dataAlignmentCode) {
		this.dataAlignmentCode = dataAlignmentCode;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return true if visible, otherwise false
	 */
	public Boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible
	 *            the visible to set
	 */
	public void setVisible(Boolean visible) {
		Boolean flag = true;
		if (this.visible.equals(visible)) {
			flag = false;
		}
		this.visible = visible;
		if (flag) {
			view.calculateStartPosition();
		}
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the spacesBeforeColumn
	 */
	public Integer getSpacesBeforeColumn() {
		return spacesBeforeColumn;
	}

	/**
	 * @param spacesBeforeColumn
	 *            the spacesBeforeColumn to set
	 */
	public void setSpacesBeforeColumn(Integer spacesBeforeColumn) {
		// TC18596 only modify if value has changed
		if ((this.spacesBeforeColumn == null && spacesBeforeColumn != null)
				|| (this.spacesBeforeColumn != null && !this.spacesBeforeColumn
						.equals(spacesBeforeColumn))) {
			this.spacesBeforeColumn = spacesBeforeColumn;
			view.calculateStartPosition();
			markModified();
			view.makeViewInactive();
	        view.markUpdated();
		}
	}

	/**
	 * @return the sortkeyFooterLabel
	 */
	public String getSortkeyFooterLabel() {
		return sortkeyFooterLabel;
	}

	/**
	 * @param sortkeyFooterLabel
	 *            the sortkeyFooterLabel to set
	 */
	public void setSortkeyFooterLabel(String sortkeyFooterLabel) {
		this.sortkeyFooterLabel = sortkeyFooterLabel;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return a read-only list of the ViewColumnSources for this ViewColumn
	 */
	public SAFRImmutableList<ViewColumnSource> getViewColumnSources() {
		List<ViewColumnSource> result = new ArrayList<ViewColumnSource>();
		List<ViewColumnSource> vcss = view.getViewColumnSources()
				.getActiveItems();
		for (ViewColumnSource vcs : vcss) {
			if (vcs.getViewColumn().getId() == this.getId()) {
				result.add(vcs);
			}
		}
		return new SAFRImmutableList<ViewColumnSource>(result);
	}

	/**
	 * TODO determine if this method is needed.
	 * 
	 * Creates a ViewColumnSource to associate this ViewColumn with the
	 * specified ViewSource, adds it to the list of ViewColumnSources, then
	 * returns the object.
	 * 
	 */
	public ViewColumnSource addViewColumnSource(ViewSource viewSource)
			throws SAFRException {
		view.makeViewInactive();
		view.markUpdated();
		return view.addViewColumnSource(this, viewSource);
	}

	/**
	 * This method is used to get the record aggregation code.
	 * 
	 * @return the record aggregation code.
	 */
	public Code getRecordAggregationCode() {
		return recordAggrCode;
	}

	/**
	 * This method is used to set the record aggregation code.
	 * 
	 * @param recordAggrCode
	 *            : the code which is to be set as record aggregation code.
	 */
	public void setRecordAggregationCode(Code recordAggrCode) {
		this.recordAggrCode = recordAggrCode;
		// CQ7432, Neha, GroupAggrcode will have default value as "SUM" if
		// the RecordAggrCode is "SUM", however user can change this.
		if (view.isFormatPhaseRecordAggregationOn() && 
		    view.getOutputFormat() == OutputFormat.Format_Report) {
			// set the same code in group aggregation function.
		    if (recordAggrCode == null) {
		        groupAggrCode = null;
		    } else {
    			groupAggrCode = SAFRApplication.getSAFRFactory().getCodeSet(
    					CodeCategories.GROUPAGGR).getCode(recordAggrCode.getKey());
		    }
		}
		calculateSubtotalCodeUsingAggregationFunctions(view.getOutputFormat(),
            view.isFormatPhaseRecordAggregationOn(),
            getRecordAggregationCode(),
            getGroupAggregationCode());
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * This method is used to get the group aggregation code.
	 * 
	 * @return the group aggregation code.
	 */
	public Code getGroupAggregationCode() {
		return groupAggrCode;
	}

	/**
	 * This method is used to set the group aggregation code.
	 * 
	 * @param groupAggrCode
	 *            : the code which is to be set as group aggregation code.
	 */
	public void setGroupAggregationCode(Code groupAggrCode) {
		this.groupAggrCode = groupAggrCode;
        calculateSubtotalCodeUsingAggregationFunctions(view.getOutputFormat(),
            view.isFormatPhaseRecordAggregationOn(),
            getRecordAggregationCode(),
            getGroupAggregationCode());		
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * This method is used to get the subtotal type code.
	 * 
	 * @return the subtotal type code.
	 */
	public Code getSubtotalTypeCode() {
		return subtotalTypeCode;
	}

	/**
	 * This method is used to break the subtotal type code into aggregation
	 * functions depending on the values of output format of the parent view,
	 * and whether the format phase record aggregation is on, and the subtotal
	 * type code.CQ 4795, How to display existing subtotal codes.
	 * 
	 * @param outputFormat
	 *            : the output format of the view.
	 * @param isformatPhaseRecordAggregartionOn
	 *            : whether the format phase record aggregation is on.
	 * @param subTotalCodeId
	 *            : the general id of the subtotal type code.
	 */
	private void breakSubtotalCodeIntoAggregationFunctions(
			OutputFormat outputFormat,
			Boolean isformatPhaseRecordAggregartionOn, Integer subTotalCodeId) {
		this.recordAggrCode = null;
		this.groupAggrCode = null;
		if (subTotalCodeId == null) {
		    return;
		}
		if (outputFormat != OutputFormat.Format_Report) {
			switch (subTotalCodeId) {
			case Codes.BREAK_CALCULATION:
				this.recordAggrCode = (SAFRApplication.getSAFRFactory()
						.getCodeSet(CodeCategories.RECORDAGGR)
						.getCode("CALC"));
				break;
			case Codes.DETAIL_CALCULATION:
				this.recordAggrCode = (SAFRApplication.getSAFRFactory()
						.getCodeSet(CodeCategories.RECORDAGGR)
						.getCode("SUM"));
				break;
			case Codes.DETAIL_FIRST:
				this.recordAggrCode = (SAFRApplication.getSAFRFactory()
						.getCodeSet(CodeCategories.RECORDAGGR)
						.getCode("FIRST"));
				break;
			case Codes.DETAIL_LAST:
				this.recordAggrCode = (SAFRApplication.getSAFRFactory()
						.getCodeSet(CodeCategories.RECORDAGGR)
						.getCode("LAST"));
				break;
			case Codes.DETAIL_MAXIMUM:
				this.recordAggrCode = (SAFRApplication.getSAFRFactory()
						.getCodeSet(CodeCategories.RECORDAGGR)
						.getCode("MAX"));
				break;
			case Codes.DETAIL_MINIMUM:
				this.recordAggrCode = (SAFRApplication.getSAFRFactory()
						.getCodeSet(CodeCategories.RECORDAGGR)
						.getCode("MIN"));
				break;
			case Codes.FIRST:
				this.recordAggrCode = (SAFRApplication.getSAFRFactory()
						.getCodeSet(CodeCategories.RECORDAGGR)
						.getCode("SUM"));
				break;
			case Codes.LAST:
				this.recordAggrCode = (SAFRApplication.getSAFRFactory()
						.getCodeSet(CodeCategories.RECORDAGGR)
						.getCode("SUM"));
				break;
			case Codes.MAXIMUM:
				this.recordAggrCode = (SAFRApplication.getSAFRFactory()
						.getCodeSet(CodeCategories.RECORDAGGR)
						.getCode("SUM"));
				break;
			case Codes.MINIMUM:
				this.recordAggrCode = (SAFRApplication.getSAFRFactory()
						.getCodeSet(CodeCategories.RECORDAGGR)
						.getCode("SUM"));
				break;
			case Codes.SUM:
				this.recordAggrCode = (SAFRApplication.getSAFRFactory()
						.getCodeSet(CodeCategories.RECORDAGGR)
						.getCode("SUM"));
				break;
			}
			// behavior is same for hardcopy report and drill down file.
		} else if (outputFormat == OutputFormat.Format_Report) {
			if (!isformatPhaseRecordAggregartionOn) {
				switch (subTotalCodeId) {
				case Codes.BREAK_CALCULATION:
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("CALC"));
					break;
				case Codes.DETAIL_CALCULATION:
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("SUM"));
					break;
				case Codes.DETAIL_FIRST:
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("FIRST"));
					break;
				case Codes.DETAIL_LAST:
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("LAST"));
					break;
				case Codes.DETAIL_MAXIMUM:
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("MAX"));
					break;
				case Codes.DETAIL_MINIMUM:
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("MIN"));
					break;
				case Codes.FIRST:
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("FIRST"));
					break;
				case Codes.LAST:
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("LAST"));
					break;
				case Codes.MAXIMUM:
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("MAX"));
					break;
				case Codes.MINIMUM:
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("MIN"));
					break;
				case Codes.SUM:
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("SUM"));
					break;
				}
			} else {
				switch (subTotalCodeId) {
				case Codes.BREAK_CALCULATION:
					this.recordAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.RECORDAGGR)
							.getCode("CALC"));
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("CALC"));
					break;
				case Codes.DETAIL_CALCULATION:
					this.recordAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.RECORDAGGR)
							.getCode("SUM"));
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("SUM"));
					break;
				case Codes.DETAIL_FIRST:
					this.recordAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.RECORDAGGR)
							.getCode("FIRST"));
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("FIRST"));
					break;
				case Codes.DETAIL_LAST:
					this.recordAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.RECORDAGGR)
							.getCode("LAST"));
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("LAST"));
					break;
				case Codes.DETAIL_MAXIMUM:
					this.recordAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.RECORDAGGR)
							.getCode("MAX"));
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("MAX"));
					break;
				case Codes.DETAIL_MINIMUM:
					this.recordAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.RECORDAGGR)
							.getCode("MIN"));
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("MIN"));
					break;
				case Codes.FIRST:
					this.recordAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.RECORDAGGR)
							.getCode("SUM"));
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("FIRST"));
					break;
				case Codes.LAST:
					this.recordAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.RECORDAGGR)
							.getCode("SUM"));
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("LAST"));
					break;
				case Codes.MAXIMUM:
					this.recordAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.RECORDAGGR)
							.getCode("SUM"));
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("MAX"));
					break;
				case Codes.MINIMUM:
					this.recordAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.RECORDAGGR)
							.getCode("SUM"));
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("MIN"));
					break;
				case Codes.SUM:
					this.recordAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.RECORDAGGR)
							.getCode("SUM"));
					this.groupAggrCode = (SAFRApplication.getSAFRFactory()
							.getCodeSet(CodeCategories.GROUPAGGR)
							.getCode("SUM"));
					break;
				}
			}
		}
	}

	/**
	 * This method is used to calculate the subtotal type code depending on the
	 * values of output format of the view, whether the format phase record
	 * aggregation is on, the record aggregation code and the group aggregation
	 * code. CQ 4795, Proposed design for subtotal codes.
	 * 
	 * @param outputFormat
	 *            : the output format of the view.
	 * @param isFormatPhaseAggregationOn
	 *            : true if it is on.
	 * @param recAggrCodeId
	 *            : the general id of the record aggregation code.
	 * @param grpAggrCodeId
	 *            :the general id of the group aggregation code.
	 * @throws SAFRException
	 */
	private void calculateSubtotalCodeUsingAggregationFunctions(
			OutputFormat outputFormat, Boolean isFormatPhaseAggregationOn,
			Code recAggrCode, Code grpAggrCode) {
	    Integer recAggrCodeId = null;
	    if (recAggrCode != null) {
	        recAggrCodeId = recAggrCode.getGeneralId();
	    }
	    Integer grpAggrCodeId = null;
        if (grpAggrCode != null) {
            grpAggrCodeId = grpAggrCode.getGeneralId();
        }

        if (recAggrCodeId == null && grpAggrCodeId == null) {
            subtotalTypeCode = null;
            return;
        }
        
		if (outputFormat != OutputFormat.Format_Report) {
			if (isFormatPhaseAggregationOn) {
                if (grpAggrCodeId == null) {
                    switch (recAggrCodeId) {
                    case Codes.GROUP_CALCULATION:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory()
                                .getCodeSet(CodeCategories.SUBTOT).getCode(
                                        "BCALC");
                        break;
                    case Codes.RECAGGR_FIRST:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory()
                                .getCodeSet(CodeCategories.SUBTOT).getCode(
                                        "DFRST");
                        break;
                    case Codes.RECAGGR_LAST:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory()
                                .getCodeSet(CodeCategories.SUBTOT).getCode(
                                        "DLAST");
                        break;
                    case Codes.RECAGGR_MAX:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory()
                                .getCodeSet(CodeCategories.SUBTOT).getCode(
                                        "DMAX");
                        break;
                    case Codes.RECAGGR_MIN:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory()
                                .getCodeSet(CodeCategories.SUBTOT).getCode(
                                        "DMIN");
                        break;
                    case Codes.SUM:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory()
                                .getCodeSet(CodeCategories.SUBTOT).getCode(
                                        "SUM");
                        break;
                    }
                }
            }
            // behavior is same for hard copy and drill down.
        } else if (outputFormat == OutputFormat.Format_Report) {
            if (!isFormatPhaseAggregationOn) {
                if (recAggrCodeId == null) {
                    switch (grpAggrCodeId) {
                    case Codes.GRPAGGR_GROUP_CALCULATION:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SUBTOT)
                                .getCode("BCALC");
                        break;
                    case Codes.GRPAGGR_NONE:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SUBTOT)
                                .getCode("NONE");
                        break;
                    case Codes.GRPAGGR_MAX:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SUBTOT)
                                .getCode("MAX");
                        break;
                    case Codes.GRPAGGR_MIN:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SUBTOT)
                                .getCode("MIN");
                        break;
                    case Codes.GRPAGGR_SUM:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SUBTOT)
                                .getCode("SUM");
                        break;
                    }
                }
            }
            // if format phase record aggregation is on.
            else {
                if (recAggrCodeId == null || grpAggrCodeId == null) {
                    subtotalTypeCode = null;
                } else if (recAggrCodeId == Codes.GROUP_CALCULATION
                        && grpAggrCodeId == Codes.GRPAGGR_GROUP_CALCULATION) {
                    subtotalTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SUBTOT)
                            .getCode("BCALC");
                } else if (grpAggrCodeId == Codes.GRPAGGR_MAX) {
                    switch (recAggrCodeId) {
                    case Codes.RECAGGR_MAX:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SUBTOT)
                                .getCode("DMAX");
                        break;
                    case Codes.SUM:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SUBTOT)
                                .getCode("MAX");
                        break;
                    }
                } else if (grpAggrCodeId == Codes.GRPAGGR_MIN) {
                    switch (recAggrCodeId) {
                    case Codes.RECAGGR_MIN:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SUBTOT)
                                .getCode("DMIN");
                        break;
                    case Codes.SUM:
                        subtotalTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SUBTOT)
                                .getCode("MIN");
                        break;
                    }
                } else if (recAggrCodeId == Codes.SUM && grpAggrCodeId == Codes.GRPAGGR_SUM) {
                    subtotalTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.SUBTOT)
                            .getCode("SUM");
                }
            }
        }
    }

	/**
	 * Returns the logic text for the ViewColumn's Format Column Calculation.
	 * 
	 * @return a String of logic text
	 */
	public String getFormatColumnCalculation() {
		return formatColumnCalculation;
	}

	/**
	 * Sets the logic text for the ViewColumn's Format Column Calculation
	 * 
	 * @param formatColumnCalculation
	 *            a String of logic text
	 */
	public void setFormatColumnCalculation(String formatColumnCalculation) {
		this.formatColumnCalculation = formatColumnCalculation;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	public void setCompiledFormatColumnCalculation(
			byte[] compiledFormatColumnCalculation) {
		this.compiledFormatColumnCalculation = compiledFormatColumnCalculation;
	}

	public byte[] getCompiledFormatColumnCalculation() {
		return compiledFormatColumnCalculation;
	}

	/**
	 * @return true if this column is a sort key, otherwise false
	 */
	public Boolean isSortKey() {
		Boolean result = (getViewSortKey() != null ? true : false);
		return result;
	}

	/**
	 * @return the ViewSortKey if this column is a sort key, otherwise null
	 */
	public ViewSortKey getViewSortKey() {
		
		//No add to a map ?
		//although a list is quick to iterate and there will only be a few.
		//Use third part search though?
		
		ViewSortKey viewSortKey = null;
		List<ViewSortKey> vsks = view.getViewSortKeys().getActiveItems();
		for (ViewSortKey vsk : vsks) {
			if (id.equals(vsk.getViewColumn().getId())) {
				viewSortKey = vsk;
				break;
			}
		}
		return viewSortKey;
	}

	Code getExtractAreaCode() {
		return extractAreaCode;
	}

	void setExtractAreaCode(Code extractAreaCode) {
		// TC18596 only modify if value has changed
		if ((this.extractAreaCode == null && extractAreaCode != null)
				|| (this.extractAreaCode != null && !this.extractAreaCode
						.equals(extractAreaCode))) {
			this.extractAreaCode = extractAreaCode;
			view.makeViewInactive();
			markModified();
		}
	}

	Integer getOrdinalPosition() {
		return ordinalPosition;
	}

	void setOrdinalPosition(Integer ordinalPosition) {
		// TC18596 only modify if value has changed
		if ((this.ordinalPosition == null && ordinalPosition != null)
				|| (this.ordinalPosition != null && !this.ordinalPosition
						.equals(ordinalPosition))) {
			this.ordinalPosition = ordinalPosition;
			view.makeViewInactive();
	        view.markUpdated();
			markModified();
		}
	}

	Integer getExtractAreaPosition() {
		return extractAreaPosition;
	}

	void setExtractAreaPosition(Integer extractAreaPosition) {
		// TC18596 only modify if value has changed
		if ((this.extractAreaPosition == null && extractAreaPosition != null)
				|| (this.extractAreaPosition != null && !this.extractAreaPosition.equals(extractAreaPosition))) {
			this.extractAreaPosition = extractAreaPosition;
			view.makeViewInactive();
			markModified();
		}
	}

	@Override
	public void store() throws SAFRException, DAOException {
		// TODO Auto-generated method stub

	}

	/**
	 * Validate Format Column Calculation using this View.
	 * 
	 * @param logicText
	 *            The format record filter logic text to be validated.
	 * @throws DAOException
	 * @throws SAFRException
	 *             SAFRValidation exception will be thrown with a list of
	 *             validation errors.
	 */
	public void validateFormatColumnCalculation(String logicText)
			throws DAOException, SAFRException {
	    LogicTextSyntaxChecker.checkSyntaxFormatCalc(logicText, view, this);
	}

	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {
		// TODO Auto-generated method stub
		return null;
	}

	List<String> getLoadWarnings() {
		return loadWarnings;
	}

	String getInitialSortKeyLabel() throws SAFRException {
		// CQ8794;search for a sort key label in all the view column sources, if
		// found, set that as the sort key label, else set heading 1 of the
		// column as the sort key label. If heading 1 is not available, set it
		// to blank.
		String skLabel = null;
		for (ViewColumnSource vcsrc : this.getViewColumnSources()
				.getActiveItems()) {
			if (vcsrc.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
				if (vcsrc.getLRField() != null && 
				    vcsrc.getLRField().getSortKeyLabel() != null && 
				    !vcsrc.getLRField().getSortKeyLabel().equals("")) {
					skLabel = vcsrc.getLRField().getSortKeyLabel();
					break;
				}
			} else if (vcsrc.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
				if (vcsrc.getLRField() != null
						&& vcsrc.getLRField().getSortKeyLabel() != null
						&& !vcsrc.getLRField().getSortKeyLabel().equals("")) {
					skLabel = vcsrc.getLRField().getSortKeyLabel();
					break;
				}
			}
		}
		if (skLabel == null) {
			// label not found in view column sources
			skLabel = this.getHeading1();
		}
		if (skLabel == null) {
			return "";
		} else {
			return skLabel;
		}
	}
	
	/**
	 * @return String in format colheading[colno]
	 */
	public String getDescriptor() {
		String colDesc = "";
		if (getHeading1() != null && !getHeading1().equals("")) {
			colDesc = getHeading1();
		} else if (getHeading2() != null && !getHeading2().equals("")) {
			colDesc = getHeading2();
		} else if (getHeading3() != null && !getHeading3().equals("")) {
			colDesc = getHeading3();
		}
		colDesc += 	"[" + columnNo + "]";
		return colDesc;
	}
}
