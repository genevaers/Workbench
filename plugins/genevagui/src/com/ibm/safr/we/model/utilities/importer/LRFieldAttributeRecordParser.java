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


import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;

// TODO figure out overlap of LRFieldTransfer between LR Field and LR Field attributes tables

/**
 * This class will parse a &LT;LR-Field-Attributes&GT; &LT;Record&GT; element
 * into a LRFieldTransfer object.
 */
public class LRFieldAttributeRecordParser extends RecordParser {
	
	public LRFieldAttributeRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//LR-Field-Attribute/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;

		fieldValue = parseField("LRFIELDID", record);
		Integer xlrfldid = fieldToInteger("LRFIELDID", fieldValue);
		
		// Reuse transfer object used to parse the matching LRField record.
		// Note the constraint - the LRField record must have been parsed
		// before this LRFieldAttribute record.
		
		LRFieldTransfer trans = (LRFieldTransfer) importer.records.get(
				LRFieldTransfer.class).get(xlrfldid);
		
		// Error if there is no matching LR Field record
		if (trans == null) {
			SAFRValidationException sve = new SAFRValidationException();
			sve.setErrorMessage("XLRFLDID", "LR-Field-Attribute with XLRFLDID [" + xlrfldid +
					"] does not have a matching LRField.");
			throw sve;
		}

		// xlrfldid and envId have already been set from LRField record
		// populate trans with values from LRFieldAttribute

		fieldValue = parseField("FLDFMTCD", record);
		trans.setDataType(fieldValue.trim());

		fieldValue = parseField("SIGNEDIND", record);
		trans.setSigned(DataUtilities.intToBoolean(fieldToInteger("SIGNEDIND",
				fieldValue)));

		fieldValue = parseField("MAXLEN", record);
		trans.setLength(fieldToInteger("MAXLEN", fieldValue));

		fieldValue = parseField("DECIMALCNT", record);
		trans.setDecimalPlaces(fieldToInteger("DECIMALCNT", fieldValue));

		fieldValue = parseField("ROUNDING", record);
		trans.setScalingFactor(fieldToInteger("ROUNDING", fieldValue));

		fieldValue = parseField("FLDCONTENTCD", record);
		trans.setDateTimeFormat(fieldValue.trim());

		fieldValue = parseField("HDRJUSTIFYCD", record);
		trans.setHeaderAlignment(fieldValue.trim());

		fieldValue = parseField("HDRLINE1", record);
		trans.setColumnHeading1(fieldValue.trim());

		fieldValue = parseField("HDRLINE2", record);
		trans.setColumnHeading2(fieldValue.trim());

		fieldValue = parseField("HDRLINE3", record);
		trans.setColumnHeading3(fieldValue.trim());

        fieldValue = parseField("SUBTLABEL", record);
        trans.setSubtotalLabel(fieldValue.trim());
        		
		fieldValue = parseField("SORTKEYLABEL", record);
		trans.setSortKeyLabel(fieldValue.trim());

		fieldValue = parseField("INPUTMASK", record);
		trans.setNumericMask(fieldValue.trim());

		if (trans.getCreateTime() == null) {
	        fieldValue = parseField("CREATEDTIMESTAMP", record);
		    trans.setCreateTime(fieldToDate("CREATEDTIMESTAMP", fieldValue));
		}
		 
		if (trans.getCreateBy() == null) {
    		fieldValue = parseField("CREATEDUSERID", record);
    		trans.setCreateBy(fieldValue.trim());
		}
    	
		if (trans.getModifyTime() == null) {
    		fieldValue = parseField("LASTMODTIMESTAMP", record);
    		trans.setModifyTime(fieldToDate("LASTMODTIMESTAMP", fieldValue));
		}
    	
		if (trans.getModifyBy() == null) {
    		fieldValue = parseField("LASTMODUSERID", record);
    		trans.setModifyBy(fieldValue.trim());
		}
    		
		return trans;
	}

	@Override
	public void checkReferentialIntegrity() throws SAFRValidationException {
		// No-op. This record is not referenced anywhere as a fkey.
	}

	protected void putToMap(Map<Integer, SAFRTransfer> map, SAFRTransfer tfr) {
		// no op. LRFieldTransfer already in map.
	}

}
