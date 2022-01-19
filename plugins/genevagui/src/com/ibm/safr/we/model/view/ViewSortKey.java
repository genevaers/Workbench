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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.LookupPathTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.ModelUtilities;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.utilities.importer.ModelTransferProvider;
import com.ibm.safr.we.model.view.View.Property;

public class ViewSortKey extends SAFREnvironmentalComponent {
	
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.view.ViewSortKey");

	/*
	 * The following columns of VIEWSORTKEY are not represented in this
	 * class because they are redundant.
	 */
	// SUBTOTALCNTIND
	// FLDLABEL
	// SUBTLABEL - xov_viewlrfldattr column with same name used instead
	// FLDFMTCD
	// SIGNEDIND
	// MAXLEN
	// FLDCONTENTCD
	// ROUNDING
	// SKORDINALPOS
	// SKORDINALOFFSET
	// SKROUNDING
	// SKJUSTIFYCD
	// SKMASK
	// STFLDFMTCD
	// STSIGNED
	// STSTARTPOS
	// STFLDLEN
	// STORDINALPOS
	// STORDINALOFFSET
	// STDECIMALCNT
	// STROUNDING
	// STFLDCONTENTCD
	// STJUSTIFYCD
	// STMASK
	// DESCFLDFMTCD
	// DESCSIGNED
	// DESCSTARTPOS
	// DESCORDINALPOS
	// DESCORDINALOFFSET
	// DESCDECIMALCNT
	// DESCROUNDING
	// DESCFLDCONTENTCD
	// DESCJUSTIFYCD
	// DESCMASK
	private View view; // VIEWID
	private Integer viewColumnId; // VIEWCOLUMNID
	private ViewColumn viewColumn;
	private Integer keySequenceNo; // KEYSEQNBR
	private Code sortSequenceCode; // SORTSEQCD
	private Code footerOptionCode; // SORTBRKIND
	private Code headerOptionCode; // PAGEBRKIND
	private Code displayModeCode; // SORTKEYDISPLAYCD
	private String sortkeyLabel; // SORTKEYLABEL
	private Code dataTypeCode; // SKFLDFMTCD
	private Boolean signed; // SKSIGNED
	private Integer startPosition; // SKSTARTPOS
	private Integer length; // SKFLDLEN
	private Integer decimalPlaces; // SKDECIMALCNT
	private Code dateTimeFormatCode; // SKFLDCONTENTCD
	private Integer titleFieldId; // SORTTITLELRFIELDID
	private LRField titleField;
	private Integer titleLength; // SORTTITLELENGTH

	private List<String> loadWarnings;

	private ModelTransferProvider provider;

	/**
	 * This constructor is used when creating a new ViewSortKey. The ViewSortKey
	 * ID will be initialized to zero and then set to a unique value when the
	 * ViewSortKey object is persisted via its <code>store()</code> method.
	 * 
	 * @param viewColumn
	 *            the ViewColumn this sort key applies to
	 */
	ViewSortKey(ViewColumn viewColumn) {
		super(viewColumn.getEnvironmentId());
		this.view = viewColumn.getView();
		this.viewColumn = viewColumn;
		this.viewColumnId = viewColumn.getId();
		this.setName(getSortKeyName());
	}

	/**
	 * Create a ViewSortKey object containing the data in the specified transfer
	 * object. Used to instantiate existing ViewSortKey objects.
	 * <p>
	 * For Import only, if the View parameter is null the object will be marked
	 * for deletion.
	 * 
	 * @param parentView
	 *            the View containing this sort key
	 * @param trans
	 *            the ViewSortKeyTransfer object
	 */
	ViewSortKey(View parentView, ViewSortKeyTransfer trans) throws DAOException, SAFRException {
		super(trans);
		this.view = parentView;
		if (!isForImport()) {
			this.setName(getSortKeyName());
		}
		
		if (view == null && isForImport()) {
			markDeleted();
		} else {
			// CQ9628 check that the parent View contains the View Column referenced
			// by this View Sort Key.
			ViewColumn vc = getViewColumn();
			if (vc == null) {
				String msg = "ViewColumn [" + viewColumnId
						+ "] for ViewSortKey [" + this.getId()
						+ "] does not exist in View [" + this.getView().getId()
						+ "].";
				logger.log(
						Level.SEVERE,
						msg
								+ SAFRUtilities.LINEBREAK + "A View Sort Key refers to a View Column which is not referenced by the parent View.");
				throw new SAFRNotFoundException(msg);
			}
		}		
		
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		ViewSortKeyTransfer trans = (ViewSortKeyTransfer) safrTrans;

		loadWarnings = new ArrayList<String>();

		this.viewColumnId = trans.getViewColumnId();
		if (viewColumn != null && viewColumn.getId() != trans.getViewColumnId()) {
			this.viewColumn = null;
		}
		this.keySequenceNo = trans.getKeySequenceNo();

		try {
			this.sortSequenceCode = SAFRApplication.getSAFRFactory()
					.getCodeSet(CodeCategories.SORTSEQ).getCode(
							trans.getSortSequenceCode()); // non-null
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			loadWarnings
					.add("Sort Key "
							+ this.keySequenceNo
							+ " does not have a valid sort sequence. Please select a valid sort sequence before saving.");
			this.sortSequenceCode = null;
		}
		try {
			this.footerOptionCode = SAFRApplication.getSAFRFactory()
					.getCodeSet(CodeCategories.SORTBRKFTR).getCode(
							trans.getFooterOptionCode()); // non-null
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			loadWarnings
					.add("Sort Key "
							+ this.keySequenceNo
							+ " does not have a valid sort key footer option. Please select a valid sort key footer option before saving.");
			this.footerOptionCode = null;
		}
		try {
			this.headerOptionCode = SAFRApplication.getSAFRFactory()
					.getCodeSet(CodeCategories.SORTBRKHDR).getCode(
							trans.getHeaderOptionCode()); // non-null
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			loadWarnings
					.add("Sort Key "
							+ this.keySequenceNo
							+ " does not have a valid sort key header option. Please select a valid sort key header option before saving.");
			this.headerOptionCode = null;
		}
		try {
			this.displayModeCode = ModelUtilities.getCodeFromKey(
					CodeCategories.SORTDSP, trans.getDisplayModeCode());
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			loadWarnings
					.add("Sort Key "
							+ this.keySequenceNo
							+ " does not have a valid display mode. Please select a valid display mode, if required, before saving.");
			this.displayModeCode = null;
		}
		this.sortkeyLabel = trans.getSortkeyLabel();
		try {
			this.dataTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.DATATYPE).getCode(trans.getDataTypeCode()); // non-null
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			loadWarnings
					.add("Sort Key "
							+ this.keySequenceNo
							+ " does not have a valid data type. Please select a valid data type before saving.");
			this.dataTypeCode = null;
		}
		this.signed = trans.isSigned();
		this.startPosition = trans.getStartPosition();
		this.length = trans.getLength();
		this.decimalPlaces = trans.getDecimalPlaces();
		try {
			this.dateTimeFormatCode = ModelUtilities.getCodeFromKey(
					CodeCategories.FLDCONTENT, trans.getDateTimeFormatCode());
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			loadWarnings
					.add("Sort Key "
							+ this.keySequenceNo
							+ " does not have a valid date time format. Please select a valid date time format, if required, before saving.");
			this.dateTimeFormatCode = null;
		}
		this.titleFieldId = trans.getTitleFieldId();
		if (titleField != null && titleField.getId() != trans.getTitleFieldId()) {
			this.titleField = null;
		}
		this.titleLength = trans.getTitleLength();
	}

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		ViewSortKeyTransfer trans = (ViewSortKeyTransfer) safrTrans;
		trans.setViewId(view.getId());
		trans.setViewColumnId(getViewColumn().getId());
		trans.setKeySequenceNo(keySequenceNo);
		trans.setSortSequenceCode(sortSequenceCode.getKey());
		trans.setFooterOptionCode(footerOptionCode.getGeneralId().intValue());
		trans.setHeaderOptionCode(headerOptionCode.getGeneralId().intValue());
		trans.setDisplayModeCode(displayModeCode == null ? null
				: displayModeCode.getKey());
		trans.setSortkeyLabel(sortkeyLabel);
		trans.setDataTypeCode(dataTypeCode.getKey());
		trans.setSigned(signed);
		trans.setStartPosition(startPosition);
		trans.setLength(length);
		trans.setDecimalPlaces(decimalPlaces);
		trans.setDateTimeFormatCode(dateTimeFormatCode == null ? null
				: dateTimeFormatCode.getKey());
		trans.setTitleFieldId(titleFieldId != null ? titleFieldId : 0);
		trans.setTitleLength(titleLength != null ? titleLength : 0);
	}

	/**
	 * @return the view
	 */
	public View getView() {
		return view;
	}

	/**
	 * @param view
	 *            the view to set
	 */
	public void setView(View view) {
		this.view = view;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the viewColumn
	 */
	public ViewColumn getViewColumn() {
		if (viewColumn == null) {
			// lazy init from view col id
			List<ViewColumn> vcs = view.getViewColumns().getActiveItems();
			for (ViewColumn vc : vcs) {
				if (vc.getId().equals(viewColumnId)) {
					this.viewColumn = vc;
					break;
				}
			}
		}
		return viewColumn;
	}

	/**
	 * @return the keySequenceNo
	 */
	public Integer getKeySequenceNo() {
		return keySequenceNo;
	}

	/**
	 * This method is used to set the sort key sequence number.This method also
	 * moves other sort keys to left or right depending on the sequence number
	 * to be set.
	 * 
	 * @param keySequenceNo
	 *            the keySequenceNo to set
	 */
	public void setKeySequenceNo(Integer keySequenceNo) {
		List<ViewSortKey> activeItems = view.getViewSortKeys().getActiveItems();

		if (this.keySequenceNo < keySequenceNo) {
			int index = this.getKeySequenceNo();
			if (activeItems.isEmpty()) {
				return;
			}
			if (keySequenceNo > (activeItems.size()) || keySequenceNo < 0) {
				return;
			}
			ViewSortKey tmpKey = activeItems.get(index - 1);
			int moveFromIndex = view.getViewSortKeys().indexOf(tmpKey);
			view.getViewSortKeys().remove(moveFromIndex);
			view.getViewSortKeys().add(keySequenceNo - 1, tmpKey);
		} else {
			int index = this.getKeySequenceNo();
			if (activeItems.isEmpty()) {
				return;
			}
			if (keySequenceNo > (activeItems.size()) || keySequenceNo < 0) {
				return;
			}
			ViewSortKey tmpKey = activeItems.get(index - 1);
			int moveToIndex = view.getViewSortKeys().indexOf(tmpKey);
			view.getViewSortKeys().remove(moveToIndex);
			view.getViewSortKeys().add(keySequenceNo - 1, tmpKey);
		}
		this.keySequenceNo = keySequenceNo;
		view.reCalcSortKeySequence();
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * This method is used to set the sequence number of the sort key.
	 * 
	 * @param keySequenceNo
	 *            : the sequence number to be set.
	 */
	void setKeySequenceNumber(Integer keySequenceNo) {
		// TC18596 only modify if value has changed
		if ((this.keySequenceNo == null && keySequenceNo != null)
				|| (this.keySequenceNo != null && !this.keySequenceNo
						.equals(keySequenceNo))) {
			this.keySequenceNo = keySequenceNo;
			view.makeViewInactive();
	        view.markUpdated();
			markModified();
		}
	}

	/**
	 * @return the sortSequenceCode
	 */
	public Code getSortSequenceCode() {
		return sortSequenceCode;
	}

	/**
	 * @param sortSequenceCode
	 *            the sortSequenceCode to set
	 * @throws NullPointerException
	 *             if the parameter is null
	 */
	public void setSortSequenceCode(Code sortSequenceCode) {
		if (sortSequenceCode == null) {
			throw new NullPointerException("Sort Sequence code cannot be null.");
		}
		this.sortSequenceCode = sortSequenceCode;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the footerOptionCode
	 */
	public Code getFooterOptionCode() {
		return footerOptionCode;
	}

	/**
	 * @param footerOptionCode
	 *            the footerOptionCode to set
	 * @throws NullPointerException
	 *             if the parameter is null
	 */
	public void setFooterOption(Code footerOptionCode) {
		if (footerOptionCode == null) {
			throw new NullPointerException(
					"Sort Key Footer Option code cannot be null.");
		}
		this.footerOptionCode = footerOptionCode;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the headerOptionCode
	 */
	public Code getHeaderOptionCode() {
		return headerOptionCode;
	}

	/**
	 * @param headerOptionCode
	 *            the headerOptionCode to set
	 * @throws NullPointerException
	 *             if the parameter is null
	 */
	public void setHeaderOption(Code headerOptionCode) {
		if (headerOptionCode == null) {
			throw new NullPointerException(
					"Sort Key Header Option code cannot be null.");
		}
		this.headerOptionCode = headerOptionCode;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the displayModeCode
	 */
	public Code getDisplayModeCode() {
		return displayModeCode;
	}

	/**
	 * @param displayModeCode
	 *            the displayModeCode to set
	 * @throws SAFRException
	 */
	public void setDisplayModeCode(Code displayModeCode) throws SAFRException {
		this.displayModeCode = displayModeCode;
		// CQ 8056. Nikita. 16/06/2010.
		// Reset values as applicable according to the display mode
		if (view.getOutputFormat() != OutputFormat.Format_Report) {
			setSortkeyLabel(null);
			removeSortKeyTitleField();
			viewColumn.setSortkeyFooterLabel(null);

		} else {
			if (displayModeCode != null &&
		        displayModeCode.getGeneralId() == Codes.ASDATA) {
				setSortkeyLabel(null);
				removeSortKeyTitleField();
			} else {
				if (sortkeyLabel == null) {
					// re-init sort key label
					sortkeyLabel = viewColumn.getInitialSortKeyLabel();
				}
			}
			// clear if display mode is As Data for output format Hardcopy Report
			if ( (displayModeCode != null && displayModeCode.getGeneralId() == Codes.ASDATA) ) {
				viewColumn.setSortkeyFooterLabel(null);
			}
			else {
				if (viewColumn.getSortkeyFooterLabel() == null) {
					viewColumn.setSortkeyFooterLabel("Subtotal,");
				}
			}
            setHeaderOption(SAFRApplication.getSAFRFactory()
                    .getCodeSet(CodeCategories.SORTBRKHDR).getCode(
                            (Codes.PSAME)));
            if (isLastSortKey() && getView().isFormatPhaseRecordAggregationOn()) {
                setFooterOption(SAFRApplication.getSAFRFactory()
                    .getCodeSet(CodeCategories.SORTBRKFTR).getCode(
                            (Codes.PRINT)));
            }
		}
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the sortkeyLabel
	 */
	public String getSortkeyLabel() {
		return sortkeyLabel;
	}

	/**
	 * @param sortkeyLabel
	 *            the sortkeyLabel to set
	 */
	public void setSortkeyLabel(String sortkeyLabel) {
		this.sortkeyLabel = sortkeyLabel;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the dataTypeCode
	 */
	public Code getDataTypeCode() {
		return dataTypeCode;
	}

	/**
	 * @param dataTypeCode
	 *            the dataTypeCode to set
	 * @throws NullPointerException
	 *             if the parameter is null
	 */
	public void setDataTypeCode(Code dataTypeCode) {
		if (dataTypeCode == null) {
			throw new NullPointerException("Data Type code cannot be null.");
		}
		this.dataTypeCode = dataTypeCode;
		switch (dataTypeCode.getGeneralId().intValue()) {
		case Codes.ALPHANUMERIC:
			setDecimalPlaces(0);
			setSigned(false);
			break;
		case Codes.BINARY_CODED_DECIMAL:
			setSigned(false);
			break;
		case Codes.EDITED_NUMERIC:
			setSigned(true);
			setDateTimeFormatCode(null);
			break;
		// CQ 8056. Nikita. 15/06/2010.
		// Date/Time Format should be cleared if the data type is Masked
		// Numeric.
		case Codes.MASKED_NUMERIC:
			setDateTimeFormatCode(null);
			break;
		case Codes.BINARY:
			if (this.getDateTimeFormatCode() != null) {
				setSigned(false);
			}
			break;
		}
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the signed
	 */
	public Boolean isSigned() {
		return signed;
	}

	/**
	 * @param signed
	 *            the signed to set
	 */
	public void setSigned(Boolean signed) {
		this.signed = signed;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
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
	 * @return the length
	 */
	public Integer getLength() {
		return length;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(Integer length) {
		this.length = length;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the decimalPlaces
	 */
	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}

	/**
	 * @param decimalPlaces
	 *            the decimalPlaces to set
	 */
	public void setDecimalPlaces(Integer decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the dateTimeFormatCode
	 */
	public Code getDateTimeFormatCode() {
		return dateTimeFormatCode;
	}

	/**
	 * @param dateTimeFormatCode
	 *            the dateTimeFormatCode to set
	 */
	public void setDateTimeFormatCode(Code dateTimeFormatCode) {
		this.dateTimeFormatCode = dateTimeFormatCode;
		if (dateTimeFormatCode != null
				&& this.getDataTypeCode().getGeneralId() == Codes.BINARY) {
			setSigned(false);
		}

		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * Field containing the Sort Key Title
	 * 
	 * @return the titleField
	 */
	public LRField getTitleField() throws SAFRException {
		if (titleField == null) {
			if (titleFieldId != null && titleFieldId > 0) {
				this.titleField = SAFRApplication.getSAFRFactory().getLRField(
						titleFieldId, getEnvironmentId(), false);
			}
		}
		return titleField;
	}

	/**
	 * @param titleField
	 *            the titleField to set
	 */
	public void setTitleField(LRField titleField) {
		this.titleField = titleField;
		if (titleField != null) {
			this.titleFieldId = titleField.getId();
		} else {
			this.titleFieldId = null;
		}
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * Length of the Sort Key Title
	 * 
	 * @return the titleLength
	 */
	public Integer getTitleLength() {
		return titleLength;
	}

	/**
	 * @param titleLength
	 *            the titleLength to set
	 */
	public void setTitleLength(Integer titleLength) {
		this.titleLength = titleLength;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	public Code getTitleAlignment() {
		if (dataTypeCode != null) {
			switch (dataTypeCode.getGeneralId().intValue()) {
			case Codes.ALPHANUMERIC:
				return SAFRApplication.getSAFRFactory().getCodeSet(
						CodeCategories.JUSTIFY).getCode(Codes.LEFT);
			case Codes.EDITED_NUMERIC:
				return SAFRApplication.getSAFRFactory().getCodeSet(
						CodeCategories.JUSTIFY).getCode(Codes.RIGHT);
			default:
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Validates the sort key title fields of this sort key. This includes below
	 * checks:<br>
	 * 1. The LR field selected as a sort key title filed from all the view
	 * sources should be same.<br>
	 * 2. The target logical file, of the lookup from which the sort key title
	 * field was selected, should be the same for all View Sources. <br>
	 * <br>
	 * Note: If the display mode of this sort key is 'AS DATA' or the type of
	 * View is Flat file, the sort key titles will be deleted.
	 * 
	 * 
	 * @throws DAOException
	 * @throws SAFRValidationException
	 *             if any of the above checks fail.
	 */
	public void validateTitleField() throws DAOException, SAFRException {
		SAFRValidationException sve = new SAFRValidationException();
		if (view.getOutputFormat() != OutputFormat.Format_Report || 
		    (this.displayModeCode != null && this.displayModeCode.getGeneralId() == Codes.ASDATA)) {
			// delete titles
			removeSortKeyTitleField();
			return;
		}
		List<Integer> lookupList = new ArrayList<Integer>();
		List<ViewColumnSource> sources = getViewColumn().getViewColumnSources().getActiveItems();

		// Checked size of the list before retrieving data from it to fix the
		// ArrayIndexOutOfBoundsException.
		Integer firstField = 0;
		if (sources.size() > 0) {
			firstField = sources.get(0).getSortKeyTitleLRFieldId();
		}

		Integer length = 0;

		// if first field is null or 0, no title exists. Else, set it true right
		// now,
		// if all title fields are same, then the flow will move ahead, else
		// this variable will be never used.
		boolean titleExists = (firstField == null || firstField <= 0) ? false : true;

		// first check if all the sources are having the same sort key title
		// field.
		for (ViewColumnSource source : sources) {
			// add the title field lookup to temp list.
			if (source.getSortKeyTitleLookupPathId() != null
					&& source.getSortKeyTitleLookupPathId() > 0) {
				lookupList.add(source.getSortKeyTitleLookupPathId());
			}
			Integer thisSourceTFieldId = source.getSortKeyTitleLRFieldId() == null ? 0
					: source.getSortKeyTitleLRFieldId();
			Integer firstSourceTFieldId = sources.get(0)
					.getSortKeyTitleLRFieldId() == null ? 0 : sources.get(0)
					.getSortKeyTitleLRFieldId();
			if (!thisSourceTFieldId.equals(firstSourceTFieldId)) {
				// exception. Sort key title fields are not the same.
				sve.setErrorMessage(Property.TITLE_FIELD,"Sort Key # " + getKeySequenceNo() + 
				    ": Sort key title field selected for all the View Sources should be same.");
				throw sve;
			}
			// store for later use.
			if (titleExists) {
				length = source.getSortKeyTitleLRField().getLength();
			}
		}

		// if sort key titles exists and are validated, check for length and
		// target LFs.
		if (titleExists) {
			// now check if the title length is proper.
			if (this.titleLength == null || this.titleLength <= 0) {
				sve.setErrorMessage(Property.TITLE_FIELD, "Sort Key # "
						+ getKeySequenceNo()
						+ ": Sort key title length should be between 1 and "
						+ length + " (inclusive)");
			} else if (this.titleLength > length) {
				// sort key length cannot be greater than the actual title
				// field length.
				sve.setErrorMessage(Property.TITLE_FIELD, "Sort Key # "
						+ getKeySequenceNo()
						+ ": Sort key title length cannot be greater than "
						+ length + ".");
			}

			if (!lookupList.isEmpty()) {
				// check if all the sort key title lookups are using the same
				// target LR/LF pair.
				boolean valid = true;
				if (isForImport()) {
					// this is called from import, use the model provider get
					// transfer objects and compare the target LFs.
					Map<Integer, Object> tempMap = new HashMap<Integer, Object>();
					for (Integer id : lookupList) {
						LookupPathTransfer pathTrans = (LookupPathTransfer) provider
								.get(LookupPathTransfer.class, id);
						tempMap.put(pathTrans.getTargetXLRFileId(), pathTrans);
					}
					if (tempMap.size() > 1) {
						// more then one values found, so the list of lookup
						// paths doesn't have same Target LR/LF pair.
						valid = false;
					}
				} else if (getView().isForMigration()
						&& getView().isMigrateRelatedComponents()) {
					// CQ9830
					// View must be 'Active' in source env to be migrated and
					// related Lookups will be migrated too, so no need to do
					// this check.
				} else {
					if (!DAOFactoryHolder.getDAOFactory().getLookupDAO()
							.isSameTarget(lookupList, getEnvironmentId())) {
						valid = false;
					}
				}
				if (!valid) {
					sve
							.setErrorMessage(
									Property.TITLE_FIELD,
									"Sort Key # "
											+ getKeySequenceNo()
											+ ": Target Logical File of the Lookup paths, used to select sort key title field for all the View Sources, should be same.");
				}
			}
		}
		if (!sve.getErrorMessages().isEmpty()) {
			throw sve;
		}
	}

	/**
	 * Validates the sort key to determine if mandatory fields have been set
	 * 
	 * @throws SAFRException
	 */
	public void validate() throws SAFRException {
		SAFRValidationException safrValidationException = new SAFRValidationException();

		if (this.dataTypeCode == null) {
			safrValidationException.setErrorMessage(Property.SORT_KEY,
					"Sort Key # " + getKeySequenceNo()
							+ ": Please specify a data type.");
		}
		if (this.sortSequenceCode == null) {
			safrValidationException.setErrorMessage(Property.SORT_KEY,
					"Sort Key # " + getKeySequenceNo()
							+ ": Please specify a sort sequence.");
		}
		if (this.headerOptionCode == null) {
			safrValidationException.setErrorMessage(Property.SORT_KEY,
					"Sort Key # " + getKeySequenceNo()
							+ ": Please specify a sort key header option.");
		}
		if (this.footerOptionCode == null) {
			safrValidationException.setErrorMessage(Property.SORT_KEY,
					"Sort Key # " + getKeySequenceNo()
							+ ": Please specify a sort key footer option.");
		}
		if (!safrValidationException.getErrorMessages().isEmpty())
			throw safrValidationException;
	}

	/**
	 * This method is used to remove the sort key title field. This will set the
	 * sort key title field and sort key title lookup path to null and will set
	 * effective date type and effective date value to RunDate and Runday
	 * respectively.
	 */
	public void removeSortKeyTitleField() {
		boolean flag = false;
		for (ViewColumnSource viewColumnSource : this.getViewColumn()
				.getViewColumnSources()) {
			if (viewColumnSource.getSortKeyTitleLRFieldId() != null
					&& viewColumnSource.getSortKeyTitleLRFieldId() > 0) {
				viewColumnSource.setSortKeyTitleLRField(null);
				flag = true;
			}
			if (viewColumnSource.getSortKeyTitleLookupPathId() != null
					&& viewColumnSource.getSortKeyTitleLookupPathId() > 0) {
				viewColumnSource.setSortKeyTitleLookupPathQueryBean(null);
				flag = true;
			}
			// reset the effective date type to RunDate if the column source is
			// not of type lookup field.
			if (viewColumnSource.getEffectiveDateTypeCode() != null
					&& viewColumnSource.getEffectiveDateTypeCode()
							.getGeneralId() != Codes.RELPERIOD_RUNDATE
					&& viewColumnSource.getSourceType().getGeneralId() != Codes.LOOKUP_FIELD) {
				viewColumnSource.setEffectiveDateTypeCode(SAFRApplication
						.getSAFRFactory().getCodeSet(CodeCategories.RELPERIOD)
						.getCode(Codes.RELPERIOD_RUNDATE));
				viewColumnSource.setEffectiveDateValue("Runday()");
				flag = true;
			}
		}
		if (flag) {
			this.titleLength = 0;
			view.makeViewInactive();
	        view.markUpdated();
		}
	}

	@Override
	public void store() throws SAFRException, DAOException {
		// TODO Auto-generated method stub

	}

	private String getSortKeyName() {
		ViewColumn vColumn = this.getViewColumn();
		String sortKeyname = "";
		if ((vColumn.getHeading1() != null)
				&& (!vColumn.getHeading1().equals(""))) {
			sortKeyname += vColumn.getHeading1();
			if ((vColumn.getHeading2() != null)
					&& (!vColumn.getHeading2().equals(""))) {
				sortKeyname = sortKeyname + " " + vColumn.getHeading2();
			}
			if ((vColumn.getHeading3() != null)
					&& (!vColumn.getHeading3().equals(""))) {
				sortKeyname = sortKeyname + " " + vColumn.getHeading3();
			}
		} else {
			sortKeyname = "Column " + vColumn.getColumnNo();
		}
		return sortKeyname;
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

	public void setModelTransferProvider(ModelTransferProvider provider) {
		this.provider = provider;
	}
	
	/**
	 * Returns whether this sort key is the last one in the sort key sequence
	 * @return
	 */
	public boolean isLastSortKey() {
	    int numkeys = view.getViewSortKeys().size();
	    if (keySequenceNo == null) {
	        return false;
	    }
	    if (numkeys == keySequenceNo) {
	        return true;
	    }
	    else {
	        return false;
	    }
	}
}
