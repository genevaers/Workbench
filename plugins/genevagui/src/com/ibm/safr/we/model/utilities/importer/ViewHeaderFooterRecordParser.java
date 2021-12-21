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
import com.ibm.safr.we.data.transfer.HeaderFooterItemTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;

public class ViewHeaderFooterRecordParser extends RecordParser {

	public ViewHeaderFooterRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//View-HeaderFooter/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {
		String fieldValue;
		HeaderFooterItemTransfer trans = new HeaderFooterItemTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("HEADERFOOTERID", record);
		trans.setHeaderFooterId(fieldToInteger("HEADERFOOTERID", fieldValue));

		fieldValue = parseField("VIEWID", record);
		trans.setViewId(fieldToInteger("VIEWID", fieldValue));

		fieldValue = parseField("STDFUNCCD", record);
		trans.setStdFuctionCode(fieldValue.trim());

		fieldValue = parseField("JUSTIFYCD", record);
		trans.setJustifyCode(fieldValue.trim());

		fieldValue = parseField("ROWNUMBER", record);
		trans.setRowNumber(fieldToInteger("ROWNUMBER", fieldValue));

		fieldValue = parseField("COLNUMBER", record);
		trans.setColNumber(fieldToInteger("COLNUMBER", fieldValue));

		fieldValue = parseField("LENGTH", record);
		trans.setLength(fieldToInteger("LENGTH", fieldValue));

		fieldValue = parseField("ITEMTEXT", record);
		trans.setItemText(fieldValue);

		fieldValue = parseField("HEADERFOOTERIND", record);
		trans.setHeader(DataUtilities.intToBoolean(fieldToInteger(
				"HEADERFOOTERIND", fieldValue)));

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
		// no-op

	}

	public void checkViewHeaderFooters() throws SAFRException {
		if (!importer.records.containsKey(HeaderFooterItemTransfer.class)) {
			// no imported view header footers
			return;
		}

		if (!importer.duplicateIdMap
				.containsKey(HeaderFooterItemTransfer.class)) {
			// no imported view HFs already exist
			return;
		}

		List<Integer> duplicateViewHFs = importer.duplicateIdMap
				.get(HeaderFooterItemTransfer.class);

		for (SAFRTransfer trans : importer.records.get(
				HeaderFooterItemTransfer.class).values()) {
			HeaderFooterItemTransfer importedViewHF = (HeaderFooterItemTransfer) trans;
			if (duplicateViewHFs.contains(importedViewHF.getId())) {
				// This view header footer already exists in DB.
				// Check that the View ID fkeys match.
				HeaderFooterItemTransfer existingViewHF = (HeaderFooterItemTransfer) importer.existingComponentTransfers
						.get(HeaderFooterItemTransfer.class).get(
								importedViewHF.getId());

				if (!existingViewHF.getViewId().equals(
						importedViewHF.getViewId())) {
					SAFRValidationException sve = new SAFRValidationException();
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"View Header/Footer ["
											+ importedViewHF.getId()
											+ "] from View ["
											+ importedViewHF.getViewId()
											+ "] already exists in the target Environment for a different View ["
											+ existingViewHF.getViewId() + "].");
					throw sve;
				}
			}
		}

	}

}
