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

import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.query.LRIndexQueryBean;

/**
 * This class will parse a &LT;LR-Index&GT; &LT;Record&GT; element into a
 * LRIndexTransfer object.
 */
public class LRIndexRecordParser extends RecordParser {

	public LRIndexRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//LR-Index/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;

		LRIndexTransfer trans = new LRIndexTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("LRINDEXID", record);
		trans.setId(fieldToInteger("LRINDEXID", fieldValue));

		fieldValue = parseField("LOGRECID", record);
		trans.setLrId(fieldToInteger("LOGRECID", fieldValue));

		fieldValue = parseField("EFFDATESTARTFLDID", record);
		trans.setEffectiveStartDateLRFieldId(fieldToInteger("EFFDATESTARTFLDID", fieldValue));
		// if effective start date field is available then update the LR field
		// transfer
		Integer effStart = trans.getEffectiveStartDateLRFieldId();
		if (effStart != null && effStart > 0 && 
		    importer.records.get(LRFieldTransfer.class).containsKey(effStart)) {
		    
			((LRFieldTransfer) importer.records.get(LRFieldTransfer.class).get(
					effStart)).setEffStartDate(true);
		}

		fieldValue = parseField("EFFDATEENDFLDID", record);
		trans.setEffectiveEndDateLRFieldId(fieldToInteger("EFFDATEENDFLDID",fieldValue));
		// if effective end date field is available then update the LR field
		// transfer
		Integer effEnd = trans.getEffectiveEndDateLRFieldId();
		if (effEnd != null && effEnd > 0 && 
		    importer.records.get(LRFieldTransfer.class).containsKey(effEnd)) {
		    
			((LRFieldTransfer) importer.records.get(LRFieldTransfer.class).get(
					effEnd)).setEffEndDate(true);
		}

		fieldValue = parseField("CREATEDTIMESTAMP", record);
		trans.setCreateTime(fieldToDate("CREATEDTIMESTAMP", fieldValue));

		fieldValue = parseField("CREATEDUSERID", record);
		trans.setCreateBy(fieldValue);

		fieldValue = parseField("LASTMODTIMESTAMP", record);
		trans.setModifyTime(fieldToDate("LASTMODTIMESTAMP", fieldValue));

		fieldValue = parseField("LASTMODUSERID", record);
		trans.setModifyBy(fieldValue);
		return trans;
	}

	@Override
	public void checkReferentialIntegrity() throws SAFRValidationException {
		SAFRValidationException sve = new SAFRValidationException();
		List<Integer> lriFKeys = new ArrayList<Integer>();

		Map<Integer, SAFRTransfer> lritMap = null;
		if (importer.records.containsKey(LRIndexTransfer.class)) {
			lritMap = importer.records.get(LRIndexTransfer.class);
		} else {
			lritMap = new HashMap<Integer, SAFRTransfer>(); // empty map
		}

		// Check for orphaned LRIndex foreign keys.
		// PKINDEXID in LR is a fkey, but not set from LR record in the xml
		// LRINDEXID in LRIndexField

		switch (importer.getComponentType()) {
        case ViewFolder:
		case View:
		case LookupPath:
		case LogicalRecord:
			// LRINDEXID in LRIndexField
			Map<Integer, SAFRTransfer> lriftMap = null;
			if (importer.records.containsKey(LRIndexFieldTransfer.class)) {
				lriftMap = importer.records.get(LRIndexFieldTransfer.class);
			} else {
				lriftMap = new HashMap<Integer, SAFRTransfer>(); // empty map
			}
			for (SAFRTransfer tfr : lriftMap.values()) {
				LRIndexFieldTransfer lrift = (LRIndexFieldTransfer) tfr;
				Integer lrindexId = lrift.getAssociatingComponentId();
				if (lrindexId > 0) {
					if (!lritMap.containsKey(lrindexId)) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"LRIndexField association ["
												+ lrift.getAssociationId()
												+ "] refers to LRIndex ["
												+ lrindexId
												+ "] but this is not present in the import file.");
						throw sve;
					}
					lriFKeys.add(lrindexId);
				}
			}
			// no break, continue
		default:
			// LogicalFile or PhysicalFile - no R/I checks here
		} // end switch

		// Check that every LRIndex primary key has a matching foreign key
		for (Integer lriPKey : lritMap.keySet()) {
			if (!lriFKeys.contains(lriPKey)) {
				sve
						.setErrorMessage(
								importer.getCurrentFile().getName(),
								"LRIndex ["
										+ lriPKey
										+ "] is not referenced by any component in the import file.");
				throw sve;
			}
		}
	}

	public void checkLRIndexes() throws SAFRException {

		if (!importer.records.containsKey(LRIndexTransfer.class)) {
			// no imported indexes to check
			return;
		}

		if (!importer.duplicateIdMap.containsKey(LRIndexTransfer.class)) {
			// no imported indexes already exist
			return;
		}

		List<Integer> duplicateLRIndexIds = importer.duplicateIdMap
				.get(LRIndexTransfer.class);

		for (SAFRTransfer trans : importer.records.get(LRIndexTransfer.class)
				.values()) {
			LRIndexTransfer importedLRIndex = (LRIndexTransfer) trans;
			if (duplicateLRIndexIds.contains(importedLRIndex.getId())) {
				// This index already exists in DB.
				// Check that the LRID fkeys match.
				LRIndexQueryBean existingLRIndex = (LRIndexQueryBean) importer.existingComponents
						.get(LRIndexQueryBean.class).get(
								importedLRIndex.getId());
				if (!existingLRIndex.getLrId()
						.equals(importedLRIndex.getLrId())) {
					SAFRValidationException sve = new SAFRValidationException();
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"LRIndex ["
											+ importedLRIndex.getId()
											+ "] from LR ["
											+ importedLRIndex.getLrId()
											+ "] already exists in the target Environment for a different LR ["
											+ existingLRIndex.getLrId() + "].");
					throw sve;
				}
			}
		}
	}
	
}
