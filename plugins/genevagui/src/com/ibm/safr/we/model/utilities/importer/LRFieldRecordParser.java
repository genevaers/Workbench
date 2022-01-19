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

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexFieldTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.query.LogicalRecordFieldQueryBean;

/**
 * This class will parse a &LT;LRField&GT; &LT;Record&GT; element into a
 * LRFieldTransfer object.
 */
public class LRFieldRecordParser extends RecordParser {

	public LRFieldRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//LRField/Record";
	}

    final public void parseRecords() throws SAFRValidationException, XPathExpressionException {

        // get all of the <Record> elements
        NodeList recordNodes = (NodeList) importer.getXPath().evaluate(getRecordExpression(), importer.getDocument(),
            XPathConstants.NODESET);

        // set map capacity so dynamic allocation is not necessary
        int numRecs = recordNodes.getLength();
        int initCapacity = (int) (numRecs * 1.25);
        Map<Integer, SAFRTransfer> map = new HashMap<Integer, SAFRTransfer>(initCapacity, 1);

        // parse each element into a transfer object
        
        Node record;
        int lrId = 0;
        for (int i = 0; i < recordNodes.getLength(); i++) {
            record = recordNodes.item(i);
            LRFieldTransfer tfr = parseRecord(record);
            if (lrId == 0 || lrId != tfr.getLrId().intValue()) {
                lrId = tfr.getLrId().intValue();
            }
            tfr.setPersistent(false); // set to false so that its inserted in DB
            // by default.
            tfr.setForImport(true); // indicate that this object is used for
            // import.
            putToMap(map,tfr);
        }
        if (!map.isEmpty()) {
            // save the map of transfer objects
            SAFRTransfer tfr = (SAFRTransfer) map.values().toArray()[0];
            importer.records.put(tfr.getClass(), map);
        }
    }
	
    @Override
	protected LRFieldTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;
		LRFieldTransfer trans = new LRFieldTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

        fieldValue = parseField("LRFIELDID", record);
        trans.setId(fieldToInteger("LRFIELDID", fieldValue));

		fieldValue = parseField("LOGRECID", record);
		trans.setLrId(fieldToInteger("LOGRECID", fieldValue));

		fieldValue = parseField("NAME", record);
		trans.setName(fieldValue.trim());

		fieldValue = parseField("DBMSCOLNAME", record);
		trans.setDbmsColName(fieldValue.trim());

		fieldValue = parseField("FIXEDSTARTPOS", record);
		trans.setFixedStartPos(fieldToInteger("FIXEDSTARTPOS", fieldValue));

		fieldValue = parseField("ORDINALPOS", record);
		trans.setOrdinalPos(fieldToInteger("ORDINALPOS", fieldValue));

		fieldValue = parseField("ORDINALOFFSET", record);
		trans.setOrdinalOffset(fieldToInteger("ORDINALOFFSET", fieldValue));

        fieldValue = parseField("REDEFINE", record);
        trans.setRedefine(fieldToInteger("REDEFINE", fieldValue));
		
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

		// set these default values for index.
		// Index parsers will set these values later.
		trans.setEffEndDate(false);
		trans.setEffStartDate(false);
		trans.setPkeySeqNo(0);

		return trans;
	}

	@Override
	public void checkReferentialIntegrity() throws SAFRValidationException {
		SAFRValidationException sve = new SAFRValidationException();
		List<Integer> lrfFKeys = new ArrayList<Integer>();

		Map<Integer, SAFRTransfer> lrftMap = null;
		if (importer.records.containsKey(LRFieldTransfer.class)) {
			lrftMap = importer.records.get(LRFieldTransfer.class);
		} else {
			lrftMap = new HashMap<Integer, SAFRTransfer>(); // empty map
		}

		// Check for orphaned LRField foreign keys.
		// SRCXLRFLDID in View Column Source
		// XLRFLDID in View Column Source
		// EFFDATEXLRFLDID in View Column Source
		// STXLRFLDID in View Sort Key
		// SRCXLRFLDID in Join Source
		// XLRFLDID in LRIndex Field

		switch (importer.getComponentType()) {
        case ViewFolder:
		case View:
			// SRCXLRFLDID in View Column Source
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
				if (viewColSrcTrans.getSourceTypeId() == Codes.SOURCE_FILE_FIELD) {
					Integer srcFieldId = viewColSrcTrans.getSourceLRFieldId();
					if (srcFieldId != null && srcFieldId > 0) {
						if (lrftMap.get(srcFieldId) == null) {
							sve.setErrorMessage(importer.getCurrentFile()
									.getName(), "View Column Source ["
									+ viewColSrcTrans.getId()
									+ "] refers to LR Field [" + srcFieldId
									+ "] but this is not in the import file.");
							throw sve;
						}
						lrfFKeys.add(srcFieldId);
					}
				}
			}
			// XLRFLDID in View Column Source
			for (SAFRTransfer tfr : viewColumnSourceTMap.values()) {
				ViewColumnSourceTransfer viewColSrcTrans = (ViewColumnSourceTransfer) tfr;
				Integer sortKeyTitleLRFldId = viewColSrcTrans
						.getSortKeyTitleLRFieldId();
				if ((sortKeyTitleLRFldId != null) && sortKeyTitleLRFldId > 0) {
					if (lrftMap.get(sortKeyTitleLRFldId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Column Source ["
												+ viewColSrcTrans.getId()
												+ "] refers to LR Field ["
												+ sortKeyTitleLRFldId
												+ "] but this is not in the import file.");
						throw sve;
					}
					lrfFKeys.add(sortKeyTitleLRFldId);
				}
			}
			// EFFDATEXLRFLDID in View Column Source
			for (SAFRTransfer tfr : viewColumnSourceTMap.values()) {
				ViewColumnSourceTransfer viewColSrcTrans = (ViewColumnSourceTransfer) tfr;
				Integer effDateXLRFldId = viewColSrcTrans
						.getEffectiveDateLRFieldId();
				if (effDateXLRFldId != null && effDateXLRFldId > 0) {
					if (lrftMap.get(effDateXLRFldId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Column Source ["
												+ viewColSrcTrans.getId()
												+ "] refers to LR Field ["
												+ effDateXLRFldId
												+ "] but this is not in the import file.");
						throw sve;
					}
					lrfFKeys.add(effDateXLRFldId);
				}
			}
			// STXLRFLDID in View Sort Key
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
				Integer sortKeyTitleFieldId = viewSortKeyTrans
						.getTitleFieldId();
				if (sortKeyTitleFieldId != null && sortKeyTitleFieldId > 0) {
					if (lrftMap.get(sortKeyTitleFieldId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Sort Key ["
												+ viewSortKeyTrans.getId()
												+ "] refers to LR Field ["
												+ sortKeyTitleFieldId
												+ "] but this is not in the import file.");
						throw sve;
					}
					lrfFKeys.add(sortKeyTitleFieldId);
				}
			}

		case LookupPath:

			// check that for every Join-source the LR Fields specified as
			// SRCXLRFLDID exist in XML
			Map<Integer, SAFRTransfer> joinSourceTMap = null;
			if (importer.records
					.containsKey(LookupPathSourceFieldTransfer.class)) {
				joinSourceTMap = importer.records
						.get(LookupPathSourceFieldTransfer.class);
			} else {
				joinSourceTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			for (SAFRTransfer tfr : joinSourceTMap.values()) {
				LookupPathSourceFieldTransfer lookupSrcFieldTrans = (LookupPathSourceFieldTransfer) tfr;
				Integer srcLRFieldId = lookupSrcFieldTrans.getSourceXLRFLDId();
				if (srcLRFieldId != null && srcLRFieldId > 0) {
					if (lrftMap.get(srcLRFieldId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"Lookup Path Source ["
												+ lookupSrcFieldTrans.getId()
												+ "] refers to LR Field ["
												+ srcLRFieldId
												+ "] but this is not in the import file.");
						throw sve;
					}
					lrfFKeys.add(srcLRFieldId);
				}

			}

		case LogicalRecord:
			// XLRFLDID in LRIndex Field
			Map<Integer, SAFRTransfer> lriftMap = null;
			if (importer.records.containsKey(LRIndexFieldTransfer.class)) {
				lriftMap = importer.records.get(LRIndexFieldTransfer.class);
			} else {
				lriftMap = new HashMap<Integer, SAFRTransfer>(); // empty map
			}
			for (SAFRTransfer tfr : lriftMap.values()) {
				LRIndexFieldTransfer lrift = (LRIndexFieldTransfer) tfr;
				Integer lrfId = lrift.getAssociatedComponentId();
				if (lrfId > 0) {
					if (lrftMap.get(lrfId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"LRIndexField["
												+ lrift.getAssociationId()
												+ "] refers to LRField ["
												+ lrfId
												+ "] but this is not present in the import file.");
						throw sve;
					}
					lrfFKeys.add(lrfId);
				}
			}
			// no break, continue
		default:
			// LogicalFile or PhysicalFile - no R/I checks here
		} // end switch

		// reverse check is not required as there may be some LR Fields
		// which are not referred to by any component.
	}

	public void checkLRFields() throws SAFRException {

		if (!importer.records.containsKey(LRFieldTransfer.class)) {
			// no imported fields
			return;
		}

		if (!importer.duplicateIdMap.containsKey(LRFieldTransfer.class)) {
			// no imported fields already exist
			return;
		}

		List<Integer> duplicateLRFieldIds = importer.duplicateIdMap
				.get(LRFieldTransfer.class);

		for (SAFRTransfer trans : importer.records.get(LRFieldTransfer.class)
				.values()) {
			LRFieldTransfer importedLRField = (LRFieldTransfer) trans;
			if (duplicateLRFieldIds.contains(importedLRField.getId())) {
				// This field already exists in DB.
				// Check that the LRID fkeys match.
				LogicalRecordFieldQueryBean existingLRField = (LogicalRecordFieldQueryBean) importer.existingComponents
						.get(LogicalRecordFieldQueryBean.class).get(
								importedLRField.getId());
				if (!existingLRField.getLrId()
						.equals(importedLRField.getLrId())) {
					SAFRValidationException sve = new SAFRValidationException();
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"LRField ["
											+ importedLRField.getId()
											+ "] from LR ["
											+ importedLRField.getLrId()
											+ "] already exists in the target Environment for a different LR ["
											+ existingLRField.getLrId() + "].");
					throw sve;
				}
			}
		}
	}

}
