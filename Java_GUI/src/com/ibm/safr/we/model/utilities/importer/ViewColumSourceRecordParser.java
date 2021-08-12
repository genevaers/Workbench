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
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.utilities.FileUtils;

public class ViewColumSourceRecordParser extends RecordParser {

	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.we.model.utilities.importer.ViewColumSourceRecordParser");
	
	public ViewColumSourceRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//View-Column-Source/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {
		String fieldValue;
		ViewColumnSourceTransfer trans = new ViewColumnSourceTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("VIEWCOLUMNSOURCEID", record);
		trans.setId(fieldToInteger("VIEWCOLUMNSOURCEID", fieldValue));

		fieldValue = parseField("VIEWCOLUMNID", record);
		trans.setViewColumnId(fieldToInteger("VIEWCOLUMNID", fieldValue));

		fieldValue = parseField("VIEWSOURCEID", record);
		trans.setViewSourceId(fieldToInteger("VIEWSOURCEID", fieldValue));

        fieldValue = parseField("VIEWID", record);
        Integer viewId = fieldToInteger("VIEWID", fieldValue);
        trans.setViewId(viewId);
		
		fieldValue = parseField("SOURCETYPEID", record);
		trans.setSourceTypeId(fieldToInteger("SOURCETYPEID", fieldValue));

        fieldValue = parseField("CONSTVAL", record);
        trans.setSourceValue(fieldValue);

        fieldValue = parseField("LOOKUPID", record);
        trans.setLookupPathId(fieldToInteger("LOOKUPID", fieldValue));

		fieldValue = parseField("LRFIELDID", record);
		trans.setSourceLRFieldId(fieldToInteger("LRFIELDID", fieldValue));

		fieldValue = parseField("EFFDATEVALUE", record);
		trans.setEffectiveDateValue(fieldValue.trim());

		fieldValue = parseField("EFFDATETYPE", record);
		trans.setEffectiveDateTypeCode(fieldValue.trim());

		fieldValue = parseField("EFFDATELRFIELDID", record);
		trans.setEffectiveDateLRFieldId(fieldToInteger("EFFDATELRFIELDID",fieldValue));

		fieldValue = parseField("SORTTITLELOOKUPID", record);
		trans.setSortKeyTitleLookupPathId(fieldToInteger("SORTTITLELOOKUPID",fieldValue));

        fieldValue = parseField("SORTTITLELRFIELDID", record);
        trans.setSortKeyTitleLRFieldId(fieldToInteger("SORTTITLELRFIELDID", fieldValue));
		
        fieldValue = parseField(LogicTextType.Extract_Column_Assignment.getExportStr(), record);
        trans.setExtractColumnLogic(FileUtils.remBRLineEndings(fieldValue));
        
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

	public void checkViewColumnSources() throws SAFRException {
		if (!importer.records.containsKey(ViewColumnSourceTransfer.class)) {
			// no imported view column sources
			return;
		}

		if (!importer.duplicateIdMap
				.containsKey(ViewColumnSourceTransfer.class)) {
			// no imported view column sources already exist
			return;
		}

		List<Integer> duplicateViewColSrcIds = importer.duplicateIdMap
				.get(ViewColumnSourceTransfer.class);

		for (SAFRTransfer trans : importer.records.get(
				ViewColumnSourceTransfer.class).values()) {
			ViewColumnSourceTransfer importedViewColSrc = (ViewColumnSourceTransfer) trans;
			if (duplicateViewColSrcIds.contains(importedViewColSrc.getId())) {
				// This view column source already exists in DB.
				// Check that the View, View Source and View Column ID fkeys
				// match.
				ViewColumnSourceTransfer existingViewColSrc = (ViewColumnSourceTransfer) importer.existingComponentTransfers
						.get(ViewColumnSourceTransfer.class).get(
								importedViewColSrc.getId());

				boolean checkVCS = (existingViewColSrc.getViewId()
						.equals(importedViewColSrc.getViewId()))
						&& (existingViewColSrc.getViewSourceId()
								.equals(importedViewColSrc.getViewSourceId()))
						&& (existingViewColSrc.getViewColumnId()
								.equals(importedViewColSrc.getViewColumnId()));
				if (!checkVCS) {
					SAFRValidationException sve = new SAFRValidationException();
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"View Column Source ["
											+ importedViewColSrc.getId()
											+ "] from View ["
											+ importedViewColSrc.getViewId()
											+ "] already exists in the target Environment for View ["
											+ existingViewColSrc.getViewId()
											+ "], View Column ["
											+ existingViewColSrc
													.getViewColumnId()
											+ "] and View Source ["
											+ existingViewColSrc
													.getViewSourceId() + "].");
					throw sve;
				}
			}
		}
	}
}
