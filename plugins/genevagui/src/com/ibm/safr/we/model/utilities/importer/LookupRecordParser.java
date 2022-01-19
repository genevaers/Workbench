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


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.LookupPathStepTransfer;
import com.ibm.safr.we.data.transfer.LookupPathTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.ComponentAssociation;

/**
 * This class will parse a &LT;Join&GT; &LT;Record&GT; element into a
 * LookupPathTransfer object.
 */
public class LookupRecordParser extends RecordParser {

	public LookupRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//Lookup/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;
		LookupPathTransfer trans = new LookupPathTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("LOOKUPID", record);
		trans.setId(fieldToInteger("LOOKUPID", fieldValue));

		fieldValue = parseField("NAME", record);
		trans.setName(fieldValue.trim());

		fieldValue = parseField("SRCLRID", record);
		trans.setSourceLRId(fieldToInteger("SRCLRID", fieldValue));

		fieldValue = parseField("DESTLRLFASSOCID", record);
		trans.setTargetXLRFileId(fieldToInteger("DESTLRLFASSOCID", fieldValue));

		fieldValue = parseField("VALIDIND", record);
		trans.setValidInd(DataUtilities.intToBoolean(fieldToInteger("VALIDIND",fieldValue)));

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
		Set<Integer> joinFKeys = new HashSet<Integer>();
		Map<Integer, SAFRTransfer> joinTMap = null;

		if (importer.records.containsKey(LookupPathTransfer.class)) {
			joinTMap = importer.records.get(LookupPathTransfer.class);
		} else {
			joinTMap = new HashMap<Integer, SAFRTransfer>();// empty map
		}

		switch (importer.getComponentType()) {
        case ViewFolder:
		case View:
			// SORTTITLEPATHID in View Column Source.
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
				Integer sortTitlePathId = viewColSrcTrans
						.getSortKeyTitleLookupPathId();
				if (sortTitlePathId != null && sortTitlePathId > 0) {
					if (joinTMap.get(sortTitlePathId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Column Source ["
												+ viewColSrcTrans.getId()
												+ "] refers to Lookup Path ["
												+ sortTitlePathId
												+ "] but this is not in the import file.");
						throw sve;
					}
					joinFKeys.add(sortTitlePathId);
				}
			}

			// JOINID in View Column Source.
			for (SAFRTransfer tfr : viewColumnSourceTMap.values()) {
				ViewColumnSourceTransfer viewColSrcTrans = (ViewColumnSourceTransfer) tfr;
				Integer joinId = viewColSrcTrans.getLookupPathId();
				if (joinId != null && joinId > 0) {
					if (joinTMap.get(joinId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Column Source ["
												+ viewColSrcTrans.getId()
												+ "] refers to Lookup Path ["
												+ joinId
												+ "] but this is not in the import file.");
						throw sve;
					}
					joinFKeys.add(joinId);
				}
			}
		case LookupPath:
			// JOINID for join targets
			Map<Integer, SAFRTransfer> joinTargetTMap = null;
			if (importer.records.containsKey(LookupPathStepTransfer.class)) {
				joinTargetTMap = importer.records
						.get(LookupPathStepTransfer.class);
			} else {
				joinTargetTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			if (!(joinTargetTMap.isEmpty())) {
				for (SAFRTransfer tfr : joinTargetTMap.values()) {
					LookupPathStepTransfer lookupStepTrans = (LookupPathStepTransfer) tfr;
					Integer srcJoinId = lookupStepTrans.getJoinId();
					if (srcJoinId != null && srcJoinId > 0) {
						if (joinTMap.get(srcJoinId) == null) {
							sve.setErrorMessage(importer.getCurrentFile()
									.getName(), "Lookup Path Step ["
									+ lookupStepTrans.getId()
									+ "] refers to Lookup Path [" + srcJoinId
									+ "] but this is not in the import file.");
							throw sve;
						}
						joinFKeys.add(srcJoinId);
					}
				}
			}

			// SRCJOINID for join source
			Map<Integer, SAFRTransfer> joinSourceTMap = null;
			if (importer.records
					.containsKey(LookupPathSourceFieldTransfer.class)) {
				joinSourceTMap = importer.records
						.get(LookupPathSourceFieldTransfer.class);
			} else {
				joinSourceTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			if (!(joinSourceTMap.isEmpty())) {
				for (SAFRTransfer tfr : joinSourceTMap.values()) {
					LookupPathSourceFieldTransfer lookupSrcFieldTrans = (LookupPathSourceFieldTransfer) tfr;
					Integer srcJoinId = lookupSrcFieldTrans.getSourceJoinId();
					if (srcJoinId != null && srcJoinId > 0) {
						if (joinTMap.get(srcJoinId) == null) {
							sve.setErrorMessage(importer.getCurrentFile()
									.getName(), "Lookup Path Source ["
									+ lookupSrcFieldTrans.getId()
									+ "] refers to Lookup Path [" + srcJoinId
									+ "] but this is not in the import file.");
							throw sve;
						}
						joinFKeys.add(srcJoinId);
					}
					// CQ 8984. Nikita 13/01/2011
					// Check if the join id referred in the source field is the
					// same as the join id referred in the parent step.
					LookupPathStepTransfer lookupStepTrans = (LookupPathStepTransfer) joinTargetTMap
							.get(lookupSrcFieldTrans.getLookupPathStepId());
					Integer stepJoinId = lookupStepTrans.getJoinId();
					if (!(srcJoinId.equals(stepJoinId))) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"Source Field with sequence number ["
												+ lookupSrcFieldTrans
														.getKeySeqNbr()
												+ "] contained in Lookup Path Step ["
												+ lookupStepTrans.getId()
												+ "] refers to Lookup Path ["
												+ lookupSrcFieldTrans
														.getSourceJoinId()
												+ "], but the Lookup Path Step refers to a different Lookup Path ["
												+ lookupStepTrans.getJoinId()
												+ "].");
						throw sve;
					}
				}
			}
		default:
			// for LR,LF,PF no checks
		}// end switch

		// Check that every Join primary key has a matching foreign key
		for (Integer joinPKey : joinTMap.keySet()) {
			if (!joinFKeys.contains(joinPKey)) {
				sve
						.setErrorMessage(
								importer.getCurrentFile().getName(),
								"Join ["
										+ joinPKey
										+ "] is not referenced by any component in the import file.");
				throw sve;
			}
		}
	}

	/**
	 * Replace association id's in the source records with association id's from the target (when
	 * they exist).
	 */
	public void replaceAssociationIds() throws SAFRException
	{
		// ------------ check Lookups  ------------------
		Map<Integer, SAFRTransfer> luMap = importer.records.get(LookupPathTransfer.class);
		
		if (luMap == null)
		{
			return;
		}
		
		Collection<ComponentAssociation> existingAssocs = importer.existingAssociations
				.get(ComponentAssociation.class).values();

		// if we do this later then the envID is that of the target
		for(Map.Entry<Integer, SAFRTransfer> luEnt : luMap.entrySet())
		{
			LookupPathTransfer lu = (LookupPathTransfer)luEnt.getValue();
			if (lu.getTargetXLRFileId() != null && lu.getTargetXLRFileId() != 0) {
				try {
					ComponentAssociationTransfer srcAssoc = (ComponentAssociationTransfer)
					importer.records.get(ComponentAssociationTransfer.class).get(lu.getTargetXLRFileId());
					
					for(ComponentAssociation trgAssoc : existingAssocs) {
						if (trgAssoc.getAssociatingComponentId().equals(srcAssoc.getAssociatingComponentId())
								&& trgAssoc.getAssociatedComponentIdNum().equals(srcAssoc.getAssociatedComponentId())) {
							if (!srcAssoc.getAssociationId().equals(trgAssoc.getAssociationId())) {
								lu.setTargetXLRFileId(trgAssoc.getAssociationId());
							}
						}
					}
						
				} catch (Exception e) {
					// Unexpected exception
					throw new SAFRException("Failed to find association", e);
				}
			}						
		}		
	}
	
}
