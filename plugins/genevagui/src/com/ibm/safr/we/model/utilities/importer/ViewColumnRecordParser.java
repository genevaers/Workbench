package com.ibm.safr.we.model.utilities.importer;

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

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.utilities.FileUtils;

public class ViewColumnRecordParser extends RecordParser {

	public ViewColumnRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//View-Column/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;
		ViewColumnTransfer trans = new ViewColumnTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("VIEWCOLUMNID", record);
		trans.setId(fieldToInteger("VIEWCOLUMNID", fieldValue));

		fieldValue = parseField("VIEWID", record);
		Integer viewId = fieldToInteger("VIEWID", fieldValue);
		trans.setViewId(viewId);
		
		fieldValue = parseField("COLUMNNUMBER", record);
		trans.setColumnNo(fieldToInteger("COLUMNNUMBER", fieldValue));

		fieldValue = parseField("FLDFMTCD", record);
		trans.setDataType(fieldValue.trim());

		fieldValue = parseField("SIGNEDIND", record);
		trans.setSigned(DataUtilities.intToBoolean(fieldToInteger("SIGNEDIND",fieldValue)));

		fieldValue = parseField("STARTPOSITION", record);
		trans.setStartPosition(fieldToInteger("STARTPOSITION", fieldValue));

		fieldValue = parseField("MAXLEN", record);
		trans.setLength(fieldToInteger("MAXLEN", fieldValue));

		fieldValue = parseField("ORDINALPOSITION", record);
		trans.setOrdinalPosition(fieldToInteger("ORDINALPOSITION", fieldValue));

		fieldValue = parseField("DECIMALCNT", record);
		trans.setDecimalPlaces(fieldToInteger("DECIMALCNT", fieldValue));

		fieldValue = parseField("ROUNDING", record);
		trans.setScalingFactor(fieldToInteger("ROUNDING", fieldValue));

		fieldValue = parseField("FLDCONTENTCD", record);
		trans.setDateTimeFormat(fieldValue.trim());

		fieldValue = parseField("JUSTIFYCD", record);
		trans.setDataAlignmentCode(fieldValue.trim());

		fieldValue = parseField("DEFAULTVAL", record);
		trans.setDefaultValue(fieldValue.trim());

		fieldValue = parseField("VISIBLE", record);
		trans.setVisible(DataUtilities.intToBoolean(fieldToInteger("VISIBLE",fieldValue)));

		fieldValue = parseField("SUBTOTALTYPECD", record);
		trans.setSubtotalTypeCode(fieldValue.trim());

		fieldValue = parseField("SPACESBEFORECOLUMN", record);
		trans.setSpacesBeforeColumn(fieldToInteger("SPACESBEFORECOLUMN",fieldValue));

		fieldValue = parseField("EXTRACTAREACD", record);
		trans.setExtractAreaCode(fieldValue.trim());

		fieldValue = parseField("EXTRAREAPOSITION", record);
		trans.setExtractAreaPosition(fieldToInteger("EXTRAREAPOSITION",fieldValue));

		fieldValue = parseField("SUBTLABEL", record);
		trans.setSortkeyFooterLabel(fieldValue.trim());

		fieldValue = parseField("RPTMASK", record);
		trans.setNumericMask(fieldValue);

		fieldValue = parseField("HDRJUSTIFYCD", record);
		trans.setHeaderAlignment(fieldValue.trim());

		fieldValue = parseField("HDRLINE1", record);
		trans.setColumnHeading1(fieldValue.trim());

		fieldValue = parseField("HDRLINE2", record);
		trans.setColumnHeading2(fieldValue.trim());

		fieldValue = parseField("HDRLINE3", record);
		trans.setColumnHeading3(fieldValue.trim());

        fieldValue = parseField(LogicTextType.Format_Column_Calculation.getExportStr(), record);
        trans.setFormatColumnLogic(FileUtils.remBRLineEndings(fieldValue));
		
		fieldValue = parseField("CREATEDTIMESTAMP", record);
		trans.setCreateTime(fieldToDate("CREATEDTIMESTAMP", fieldValue));

		fieldValue = parseField("CREATEDUSERID", record);
		trans.setCreateBy(fieldValue.trim());

		fieldValue = parseField("LASTMODTIMESTAMP", record);
		trans.setModifyTime(fieldToDate("LASTMODTIMESTAMP", fieldValue));

		fieldValue = parseField("LASTMODUSERID", record);
		trans.setModifyBy(fieldValue.trim());

		return trans;
	}

	@Override
	public void checkReferentialIntegrity() throws SAFRValidationException {
		SAFRValidationException sve = new SAFRValidationException();
		List<Integer> viewColumnFKeys = new ArrayList<Integer>();

		Map<Integer, SAFRTransfer> viewColumnTMap = null;
		if (importer.records.containsKey(ViewTransfer.class)) {
			viewColumnTMap = importer.records.get(ViewColumnTransfer.class);
		} else {
			viewColumnTMap = new HashMap<Integer, SAFRTransfer>();// empty map

		}
		switch (importer.getComponentType()) {
        case ViewFolder:
		case View:
			// XVIEWLRFLDID in View Column Sources.
			Map<Integer, SAFRTransfer> viewColumnSourceTMap = null;
			if (importer.records.containsKey(ViewColumnSourceTransfer.class)) {
				viewColumnSourceTMap = importer.records
						.get(ViewColumnSourceTransfer.class);
			} else {
				viewColumnSourceTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			for (SAFRTransfer tfr : viewColumnSourceTMap.values()) {
				ViewColumnSourceTransfer viewColSrcTrans = (ViewColumnSourceTransfer) tfr;
				Integer viewColId = viewColSrcTrans.getViewColumnId();
				if (viewColId > 0) {
					if (viewColumnTMap.get(viewColId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Column Source ["
												+ viewColSrcTrans.getId()
												+ "] refers to View Column ["
												+ viewColId
												+ "] but this is not in the import file.");
						throw sve;
					}
					viewColumnFKeys.add(viewColId);
				}
			}

			// XVIEWLRFLDID) in View Sort keys
			Map<Integer, SAFRTransfer> viewSortKeyTMap = null;
			if (importer.records.containsKey(ViewSortKeyTransfer.class)) {
				viewSortKeyTMap = importer.records
						.get(ViewSortKeyTransfer.class);
			} else {
				viewSortKeyTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			for (SAFRTransfer tfr : viewSortKeyTMap.values()) {
				ViewSortKeyTransfer viewSortKeyTrans = (ViewSortKeyTransfer) tfr;
				Integer viewColId = viewSortKeyTrans.getViewColumnId();
				if (viewColId > 0) {
					if (viewColumnTMap.get(viewColId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Sort Key ["
												+ viewSortKeyTrans.getId()
												+ "] refers to View Column ["
												+ viewColId
												+ "] but this is not in the import file.");
						throw sve;
					}
					viewColumnFKeys.add(viewColId);
				}
			}
		default:
			// no RI checks
		}// end of switch

	}

	public void checkViewColumns() throws SAFRException {
		if (!importer.records.containsKey(ViewColumnTransfer.class)) {
			// no imported view columns
			return;
		}

		if (!importer.duplicateIdMap.containsKey(ViewColumnTransfer.class)) {
			// no imported view columns already exist
			return;
		}

		List<Integer> duplicateViewColIds = importer.duplicateIdMap
				.get(ViewColumnTransfer.class);

		for (SAFRTransfer trans : importer.records
				.get(ViewColumnTransfer.class).values()) {
			ViewColumnTransfer importedViewCol = (ViewColumnTransfer) trans;
			if (duplicateViewColIds.contains(importedViewCol.getId())) {
				// This view column already exists in DB.
				// Check that the View ID fkeys match.
				ViewColumnTransfer existingViewCol = (ViewColumnTransfer) importer.existingComponentTransfers
						.get(ViewColumnTransfer.class).get(
								importedViewCol.getId());

				if (!existingViewCol.getViewId().equals(
						importedViewCol.getViewId())) {
					SAFRValidationException sve = new SAFRValidationException();
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"View Column ["
											+ importedViewCol.getId()
											+ "] from View ["
											+ importedViewCol.getViewId()
											+ "] already exists in the target Environment for a different View ["
											+ existingViewCol.getViewId()
											+ "].");
					throw sve;
				}
			}
		}
	}
	
}
