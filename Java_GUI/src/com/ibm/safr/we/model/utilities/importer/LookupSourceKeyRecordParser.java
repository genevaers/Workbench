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
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.constants.LookupPathSourceFieldType;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.ComponentAssociation;

/**
 * This class will parse a &LT;Join-Source&GT; &LT;Record&GT; element into a
 * LookupPathSourceFieldTransfer object.
 */
public class LookupSourceKeyRecordParser extends RecordParser {

	public LookupSourceKeyRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//Lookup-Source-Key/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;
		LookupPathSourceFieldTransfer trans = new LookupPathSourceFieldTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("LOOKUPSTEPID", record);
		trans.setId(fieldToInteger("LOOKUPSTEPID", fieldValue));
		trans.setLookupPathStepId(fieldToInteger("LOOKUPSTEPID", fieldValue));

		fieldValue = parseField("KEYSEQNBR", record);
		trans.setKeySeqNbr(fieldToInteger("KEYSEQNBR", fieldValue));

		fieldValue = parseField("FLDTYPE", record);
		trans.setSourceFieldType(intToEnum(fieldToInteger("FLDTYPE",fieldValue)));

		fieldValue = parseField("LRFIELDID", record);
		trans.setSourceXLRFLDId(fieldToInteger("LRFIELDID", fieldValue));

		fieldValue = parseField("LRLFASSOCID", record);
		trans.setSourceXLRFileId(fieldToInteger("LRLFASSOCID", fieldValue));

		fieldValue = parseField("LOOKUPID", record);
		trans.setSourceJoinId(fieldToInteger("LOOKUPID", fieldValue));

		fieldValue = parseField("VALUEFMTCD", record);
		trans.setDataType(fieldValue.trim());

		fieldValue = parseField("SIGNED", record);
		trans.setSigned(DataUtilities.intToBoolean(fieldToInteger("SIGNED",fieldValue)));

		fieldValue = parseField("VALUELEN", record);
		trans.setLength(fieldToInteger("VALUELEN", fieldValue));

		fieldValue = parseField("DECIMALCNT", record);
		trans.setDecimalPlaces(fieldToInteger("DECIMALCNT", fieldValue));

		fieldValue = parseField("FLDCONTENTCD", record);
		trans.setDateTimeFormat(fieldValue.trim());

		fieldValue = parseField("ROUNDING", record);
		trans.setScalingFactor(fieldToInteger("ROUNDING", fieldValue));

		fieldValue = parseField("JUSTIFYCD", record);
		trans.setHeaderAlignment(fieldValue.trim());

		fieldValue = parseField("MASK", record);
		trans.setNumericMask(fieldValue.trim());

		fieldValue = parseField("SYMBOLICNAME", record);
		trans.setSymbolicName(fieldValue.trim());

		fieldValue = parseField("VALUE", record);
		trans.setSourceValue(fieldValue);

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
	
	private LookupPathSourceFieldType intToEnum(int sourceFieldTypeInt) {
		if (sourceFieldTypeInt == 0) {
			return LookupPathSourceFieldType.LRFIELD;
		} else if (sourceFieldTypeInt == 1) {
			return LookupPathSourceFieldType.CONSTANT;
		} else if (sourceFieldTypeInt == 3) {
			return LookupPathSourceFieldType.SYMBOL;
		} else {
			return null;
		}
	}

	@Override
	public void checkReferentialIntegrity() throws SAFRValidationException {
		// No-op. This record is not referenced anywhere as a fkey.
	}

	/**
	 * Replace association id's in the source records with association id's from the target (when
	 * they exist).
	 */
	public void replaceAssociationIds() throws SAFRException
	{
		// ------------ check Lookup Steps  ------------------
		Map<Integer, SAFRTransfer> luMap = importer.records.get(LookupPathSourceFieldTransfer.class);
		
		if (luMap == null)
		{
			return;
		}
		
		Collection<ComponentAssociation> existingAssocs = importer.existingAssociations
				.get(ComponentAssociation.class).values();

		// if we do this later then the envID is that of the target
		for(Map.Entry<Integer, SAFRTransfer> luEnt : luMap.entrySet())
		{
			LookupPathSourceFieldTransfer lusf = (LookupPathSourceFieldTransfer)luEnt.getValue();
			if (lusf.getSourceXLRFileId() != null && lusf.getSourceXLRFileId() != 0) {
				try {
					ComponentAssociationTransfer srcAssoc = (ComponentAssociationTransfer)
					importer.records.get(ComponentAssociationTransfer.class).get(lusf.getSourceXLRFileId());
					
					for(ComponentAssociation trgAssoc : existingAssocs) {
						if (trgAssoc.getAssociatingComponentId().equals(srcAssoc.getAssociatingComponentId())
								&& trgAssoc.getAssociatedComponentIdNum().equals(srcAssoc.getAssociatedComponentId())) {
							if (!srcAssoc.getAssociationId().equals(trgAssoc.getAssociationId())) {
								lusf.setSourceXLRFileId(trgAssoc.getAssociationId());
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
