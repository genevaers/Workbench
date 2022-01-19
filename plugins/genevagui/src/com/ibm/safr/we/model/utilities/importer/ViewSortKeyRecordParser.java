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


import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;

public class ViewSortKeyRecordParser extends RecordParser {

	public ViewSortKeyRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//View-SortKey/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {
		String fieldValue;
		ViewSortKeyTransfer trans = new ViewSortKeyTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("VIEWSORTKEYID", record);
		trans.setId(fieldToInteger("VIEWSORTKEYID", fieldValue));

		fieldValue = parseField("VIEWID", record);
		trans.setViewId(fieldToInteger("VIEWID", fieldValue));

		fieldValue = parseField("VIEWCOLUMNID", record);
		trans.setViewColumnId(fieldToInteger("VIEWCOLUMNID", fieldValue));

		fieldValue = parseField("KEYSEQNBR", record);
		trans.setKeySequenceNo(fieldToInteger("KEYSEQNBR", fieldValue));

		fieldValue = parseField("SORTSEQCD", record);
		trans.setSortSequenceCode(fieldValue.trim());

		fieldValue = parseField("SORTBRKIND", record);
		trans.setFooterOptionCode(fieldToInteger("SORTBRKIND", fieldValue));

		fieldValue = parseField("PAGEBRKIND", record);
		trans.setHeaderOptionCode(fieldToInteger("PAGEBRKIND", fieldValue));

        fieldValue = parseField("SORTKEYDISPLAYCD", record);
        trans.setDisplayModeCode(fieldValue.trim());

		fieldValue = parseField("SORTKEYLABEL", record);
		trans.setSortkeyLabel(fieldValue.trim());

		fieldValue = parseField("SKFLDFMTCD", record);
		trans.setDataTypeCode(fieldValue.trim());

		fieldValue = parseField("SKSIGNED", record);
		trans.setSigned(DataUtilities.intToBoolean(fieldToInteger("SKSIGNED",fieldValue)));

		fieldValue = parseField("SKSTARTPOS", record);
		trans.setStartPosition(fieldToInteger("SKSTARTPOS", fieldValue));

		fieldValue = parseField("SKFLDLEN", record);
		trans.setLength(fieldToInteger("SKFLDLEN", fieldValue));

		fieldValue = parseField("SKDECIMALCNT", record);
		trans.setDecimalPlaces(fieldToInteger("SKDECIMALCNT", fieldValue));

		fieldValue = parseField("SKFLDCONTENTCD", record);
		trans.setDateTimeFormatCode(fieldValue.trim());

		fieldValue = parseField("SORTTITLELRFIELDID", record);
		trans.setTitleFieldId(fieldToInteger("SORTTITLELRFIELDID", fieldValue));

		fieldValue = parseField("SORTTITLELENGTH", record);
		trans.setTitleLength(fieldToInteger("SORTTITLELENGTH", fieldValue));

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
		// No-op. This record is not referenced anywhere as a fkey.

	}

	public void checkViewSortKeys() throws SAFRException {
		if (!importer.records.containsKey(ViewSortKeyTransfer.class)) {
			// no imported view sort keys
			return;
		}

		if (!importer.duplicateIdMap.containsKey(ViewSortKeyTransfer.class)) {
			// no imported view sort keys already exist
			return;
		}

		List<Integer> duplicateViewSKIds = importer.duplicateIdMap
				.get(ViewSortKeyTransfer.class);

		for (SAFRTransfer trans : importer.records.get(
				ViewSortKeyTransfer.class).values()) {
			ViewSortKeyTransfer importedViewSK = (ViewSortKeyTransfer) trans;
			if (duplicateViewSKIds.contains(importedViewSK.getId())) {
				// This view sort key already exists in DB.
				// Check that the View and View column ID fkeys match.
				ViewSortKeyTransfer existingViewSK = (ViewSortKeyTransfer) importer.existingComponentTransfers
						.get(ViewSortKeyTransfer.class).get(
								importedViewSK.getId());
				boolean checkVSK = (existingViewSK.getViewId()
						.equals(importedViewSK.getViewId()))
						&& (existingViewSK.getViewColumnId()
								.equals(importedViewSK.getViewColumnId()));
				if (!checkVSK) {
					SAFRValidationException sve = new SAFRValidationException();
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"View Sort Key ["
											+ importedViewSK.getId()
											+ "] from View ["
											+ importedViewSK.getViewId()
											+ "] already exists in the target Environment for View ["
											+ existingViewSK.getViewId()
											+ "] and View Column ["
											+ existingViewSK.getViewColumnId()
											+ "].");
					throw sve;
				}
			}
		}

	}

}
