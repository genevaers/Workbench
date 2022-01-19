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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.LookupPathStepTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.ComponentAssociation;

/**
 * This class will parse a &LT;Join-Target&GT; &LT;Record&GT; element into a
 * LookupPathStepTransfer object.
 */
public class LookupStepRecordParser extends RecordParser {

	public LookupStepRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//Lookup-Step/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;
		LookupPathStepTransfer trans = new LookupPathStepTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("LOOKUPSTEPID", record);
		trans.setId(fieldToInteger("LOOKUPSTEPID", fieldValue));

		fieldValue = parseField("LOOKUPID", record);
		trans.setJoinId(fieldToInteger("LOOKUPID", fieldValue));

		fieldValue = parseField("STEPSEQNBR", record);
		trans.setSequenceNumber(fieldToInteger("STEPSEQNBR", fieldValue));

		fieldValue = parseField("SRCLRID", record);
		trans.setSourceLRId(fieldToInteger("SRCLRID", fieldValue));

		fieldValue = parseField("LRLFASSOCID", record);
		trans.setTargetXLRFileId(fieldToInteger("LRLFASSOCID", fieldValue));

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
		// TODO

		SAFRValidationException sve = new SAFRValidationException();
		List<Integer> joinTrgFKeys = new ArrayList<Integer>();

		Map<Integer, SAFRTransfer> joinTargetTMap = null;
		if (importer.records.containsKey(LookupPathStepTransfer.class)) {
			joinTargetTMap = importer.records.get(LookupPathStepTransfer.class);
		} else {
			joinTargetTMap = new HashMap<Integer, SAFRTransfer>();// empty map
		}

		switch (importer.getComponentType()) {
	    case ViewFolder:
		case View:
		case LookupPath:

			// check that for every Join-source, step exists specified by
			// XJOINSTEPID
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
				Integer stepId = lookupSrcFieldTrans.getLookupPathStepId();
				if (stepId != null && stepId > 0) {
					if (joinTargetTMap.get(stepId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"Lookup Path Source ["
												+ lookupSrcFieldTrans.getId()
												+ "] refers to Lookup Path step ["
												+ stepId
												+ "] but this is not in the import file.");
						throw sve;
					}
					joinTrgFKeys.add(stepId);
				}
			}
        default:
            break;

		}// end of switch

	}

	/**
	 * Replace association id's in the source records with association id's from the target (when
	 * they exist).
	 */
	public void replaceAssociationIds() throws SAFRException
	{
		// ------------ check Lookup Steps  ------------------
		Map<Integer, SAFRTransfer> luMap = importer.records.get(LookupPathStepTransfer.class);
		
		if (luMap == null)
		{
			return;
		}
		
		Collection<ComponentAssociation> existingAssocs = importer.existingAssociations
				.get(ComponentAssociation.class).values();

		// if we do this later then the envID is that of the target
		for(Map.Entry<Integer, SAFRTransfer> luEnt : luMap.entrySet())
		{
			LookupPathStepTransfer lust = (LookupPathStepTransfer)luEnt.getValue();
			if (lust.getTargetXLRFileId() != null && lust.getTargetXLRFileId() != 0) {
				try {
					ComponentAssociationTransfer srcAssoc = (ComponentAssociationTransfer)
					importer.records.get(ComponentAssociationTransfer.class).get(lust.getTargetXLRFileId());
					
					for(ComponentAssociation trgAssoc : existingAssocs) {
						if (trgAssoc.getAssociatingComponentId().equals(srcAssoc.getAssociatingComponentId())
								&& trgAssoc.getAssociatedComponentIdNum().equals(srcAssoc.getAssociatedComponentId())) {
							if (!srcAssoc.getAssociationId().equals(trgAssoc.getAssociationId())) {
								lust.setTargetXLRFileId(trgAssoc.getAssociationId());
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
	
	public void checkJoinTargets() throws SAFRException {

		if (!importer.records.containsKey(LookupPathStepTransfer.class)) {
			// no imported steps
			return;
		}

		if (!importer.duplicateIdMap.containsKey(LookupPathStepTransfer.class)) {
			// no imported steps already exist
			return;
		}

		List<Integer> duplicateStepIds = importer.duplicateIdMap
				.get(LookupPathStepTransfer.class);

		for (SAFRTransfer trans : importer.records.get(
				LookupPathStepTransfer.class).values()) {
			LookupPathStepTransfer importedStep = (LookupPathStepTransfer) trans;
			if (duplicateStepIds.contains(importedStep.getId())) {
				// This field already exists in DB.
				// Check that the Join Id fkeys match.
				LookupPathStepTransfer existingStep = (LookupPathStepTransfer) importer.existingComponentTransfers
						.get(LookupPathStepTransfer.class).get(
								importedStep.getId());
				if (!existingStep.getJoinId().equals(importedStep.getJoinId())) {
					SAFRValidationException sve = new SAFRValidationException();
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"Lookup Path Step ["
											+ importedStep.getId()
											+ "] from Lookup Path ["
											+ importedStep.getJoinId()
											+ "] already exists in the target Environment for a different Lookup Path ["
											+ existingStep.getJoinId() + "].");
					throw sve;
				}
			}
		}
	}
}
