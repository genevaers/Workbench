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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.LookupPathStepTransfer;
import com.ibm.safr.we.data.transfer.LookupPathTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.SAFRApplication;

/**
 * This class will parse a &LT;LogicalRecord&GT; &LT;Record&GT; element into a
 * LogicalRecordTransfer object.
 */
public class LRRecordParser extends RecordParser {

	public LRRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//LogicalRecord/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;
		LogicalRecordTransfer trans = new LogicalRecordTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("LOGRECID", record);
		trans.setId(fieldToInteger("LOGRECID", fieldValue));

		fieldValue = parseField("NAME", record);
		trans.setName(fieldValue.trim());

		fieldValue = parseField("LRTYPECD", record);
		trans.setLrTypeCode(fieldValue.trim());

		fieldValue = parseField("LRSTATUSCD", record);
		trans.setLrStatusCode(fieldValue.trim());

		fieldValue = parseField("LOOKUPEXITID", record);
		trans.setLookupExitId(fieldToInteger("LOOKUPEXITID", fieldValue));

		fieldValue = parseField("LOOKUPEXITSTARTUP", record);
		trans.setLookupExitParams(fieldValue.trim());

		fieldValue = parseField("COMMENTS", record);
		trans.setComments(fieldValue.trim());

		fieldValue = parseField("CREATEDTIMESTAMP", record);
		trans.setCreateTime(fieldToDate("CREATEDTIMESTAMP", fieldValue));

		fieldValue = parseField("CREATEDUSERID", record);
		trans.setCreateBy(fieldValue.trim());

		fieldValue = parseField("LASTMODTIMESTAMP", record);
		trans.setModifyTime(fieldToDate("LASTMODTIMESTAMP", fieldValue));

		fieldValue = parseField("LASTMODUSERID", record);
		trans.setModifyBy(fieldValue.trim());

        fieldValue = parseField("LASTACTTIMESTAMP", record);
        trans.setActivatedTime(fieldToDate("LASTACTTIMESTAMP", fieldValue));

        fieldValue = parseField("LASTACTUSERID", record);
        trans.setActivatedBy(fieldValue.trim());
        
		return trans;
	}

	@Override
	public void checkReferentialIntegrity() throws SAFRValidationException {
		SAFRValidationException sve = new SAFRValidationException();
		Set<Integer> lrFKeys = new HashSet<Integer>();

		Map<Integer, SAFRTransfer> lrtMap = null;
		if (importer.records.containsKey(LogicalRecordTransfer.class)) {
			lrtMap = importer.records.get(LogicalRecordTransfer.class);
		} else {
			lrtMap = new HashMap<Integer, SAFRTransfer>(); // empty map
		}

		// Check for orphaned LR foreign keys.
		// SRCLRID in Join
		// LRID in LRIndex
		// OUTPUTLR in View
		// SRCLRID in Join-Target
		// LRID in LRFile
		// LRID in LRField

		switch (importer.getComponentType()) {
		case ViewFolder:
		case View:
			Map<Integer, SAFRTransfer> vwtMap = null;
			if (importer.records.containsKey(ViewTransfer.class)) {
				vwtMap = importer.records.get(ViewTransfer.class);
			} else {
				vwtMap = new HashMap<Integer, SAFRTransfer>(); // empty map
			}
			
			// OUTPUTLR in View just add id's for the orphan check
			for (SAFRTransfer tfr : vwtMap.values()) {
				ViewTransfer vwt = (ViewTransfer) tfr;
				Integer outputLRId = vwt.getOutputLRId();
				if (outputLRId != null && outputLRId > 0) {
					lrFKeys.add(outputLRId);
				}
			}

		case LookupPath:
			Map<Integer, SAFRTransfer> joinTMap = null;
			if (importer.records.containsKey(LookupPathTransfer.class)) {
				joinTMap = importer.records.get(LookupPathTransfer.class);
			} else {
				joinTMap = new HashMap<Integer, SAFRTransfer>();// empty map
			}

			// SRCLRID in Join Source
			Map<Integer, SAFRTransfer> lrTMap = null;

			if (importer.records.containsKey(LogicalRecordTransfer.class)) {
				lrTMap = importer.records.get(LogicalRecordTransfer.class);
			} else {
				lrTMap = new HashMap<Integer, SAFRTransfer>();// empty map
			}

			// check for the Join, the SRCLRID exists in the XML
			for (SAFRTransfer tfr : joinTMap.values()) {
				LookupPathTransfer lookupTrans = (LookupPathTransfer) tfr;
				Integer sourceLRId = lookupTrans.getSourceLRId();
				if (sourceLRId > 0) {
					if (lrTMap.get(sourceLRId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"Lookup Path Source["
												+ lookupTrans.getId()
												+ "] refers to Logical Record ["
												+ sourceLRId
												+ "] but this is not in the import file.");
						throw sve;
					}
					lrFKeys.add(sourceLRId);
				}
			}
			// SRCLRID in Join-Target
			Map<Integer, SAFRTransfer> joinTargetTMap = null;
			if (importer.records.containsKey(LookupPathStepTransfer.class)) {
				joinTargetTMap = importer.records
						.get(LookupPathStepTransfer.class);
			} else {
				joinTargetTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			for (SAFRTransfer tfr : joinTargetTMap.values()) {
				LookupPathStepTransfer lookupStepTrans = (LookupPathStepTransfer) tfr;
				Integer srcLRId = lookupStepTrans.getSourceLRId();
				if (srcLRId > 0) {
					if (lrTMap.get(srcLRId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"Lookup Path Step ["
												+ lookupStepTrans.getId()
												+ "] refers to Logical Record ["
												+ srcLRId
												+ "] as its source LR but this is not in the import file.");
						throw sve;
					}
					lrFKeys.add(srcLRId);
				}

			}
			// no break, continue
		case LogicalRecord:
			// LRID in LRFile
			Map<Integer, SAFRTransfer> catMap = null;
			if (importer.records
					.containsKey(ComponentAssociationTransfer.class)) {
				catMap = importer.records
						.get(ComponentAssociationTransfer.class);
			} else {
				catMap = new HashMap<Integer, SAFRTransfer>(); // empty map
			}
			for (SAFRTransfer tfr : catMap.values()) {
				ComponentAssociationTransfer cat = (ComponentAssociationTransfer) tfr;
				Integer lrId = cat.getAssociatingComponentId();
				if (lrId > 0) {
					if (!lrtMap.containsKey(lrId)) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"LRFile association ["
												+ cat.getAssociationId()
												+ "] refers to Logical Record ["
												+ lrId
												+ "] but this is not present in the import file.");
						throw sve;
					}
					lrFKeys.add(lrId);
				}
			}

			// LRID in LRIndex
			Map<Integer, SAFRTransfer> lritMap = null;
			if (importer.records.containsKey(LRIndexTransfer.class)) {
				lritMap = importer.records.get(LRIndexTransfer.class);
			} else {
				lritMap = new HashMap<Integer, SAFRTransfer>(); // empty map
			}

			for (SAFRTransfer tfr : lritMap.values()) {
				LRIndexTransfer lrit = (LRIndexTransfer) tfr;
				Integer lrId = lrit.getLrId();
				if (lrId > 0) {
					if (!lrtMap.containsKey(lrId)) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"LRIndex ["
												+ lrit.getId()
												+ "] refers to Logical Record ["
												+ lrId
												+ "] but this is not present in the import file.");
						throw sve;
					}
					lrFKeys.add(lrId);
				}
			}

			// LRID in LRField
			Map<Integer, SAFRTransfer> lrftMap = null;
			if (importer.records.containsKey(LRFieldTransfer.class)) {
				lrftMap = importer.records.get(LRFieldTransfer.class);
			} else {
				lrftMap = new HashMap<Integer, SAFRTransfer>(); // empty map
			}
			for (SAFRTransfer tfr : lrftMap.values()) {
				LRFieldTransfer lrft = (LRFieldTransfer) tfr;
				Integer lrId = lrft.getLrId();
				if (lrId > 0) {
					if (!lrtMap.containsKey(lrId)) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"LRField ["
												+ lrft.getId()
												+ "] refers to Logical Record ["
												+ lrId
												+ "] but this is not present in the import file.");
						throw sve;
					}
					lrFKeys.add(lrId);
				}
			}
			// no break, continue
		default:
			// LogicalFile or PhysicalFile - no R/I checks here
		} // end switch

		// Check that every LR primary key has a matching foreign key
		for (Integer lrPKey : lrtMap.keySet()) {
			LogicalRecordTransfer lrT = (LogicalRecordTransfer) lrtMap
					.get(lrPKey);
			// check only if LR is active.
			if (lrT.getLrStatusCode().equals(
					SAFRApplication.getSAFRFactory().getCodeSet(
							CodeCategories.LRSTATUS).getCode(Codes.ACTIVE)
							.getKey())) {
				if (!lrFKeys.contains(lrPKey)) {
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"Logical Record ["
											+ lrPKey
											+ "] is not referenced by any component in the import file.");
					throw sve;
				}
			}
		}
	}

}
