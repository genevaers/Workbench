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


import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexFieldTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;

/**
 * This class will parse a &LT;LR-IndexField&GT; &LT;Record&GT; element into a
 * LRIndexFieldTransfer object.
 */
public class LRIndexFieldRecordParser extends RecordParser {

	public LRIndexFieldRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//LR-IndexField/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;

		LRIndexFieldTransfer trans = new LRIndexFieldTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("LRINDEXFLDID", record);
		trans.setAssociationId(fieldToInteger("LRINDEXFLDID", fieldValue));

		fieldValue = parseField("LRINDEXID", record);
		trans.setAssociatingComponentId(fieldToInteger("LRINDEXID",fieldValue));

		fieldValue = parseField("FLDSEQNBR", record);
		trans.setFldSeqNbr(fieldToInteger("FLDSEQNBR", fieldValue));

		fieldValue = parseField("LRFIELDID", record);
		trans.setAssociatedComponentId(fieldToInteger("LRFIELDID", fieldValue));

		// if XLRFLDID is available then update the LR field
		// transfer
		Integer fieldId = trans.getAssociatedComponentId();
		if (fieldId > 0 && 
		    importer.records.get(LRFieldTransfer.class).containsKey(fieldId)) {
		    
			((LRFieldTransfer) importer.records.get(LRFieldTransfer.class).get(fieldId)).
			    setPkeySeqNo(trans.getFldSeqNbr());
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
		// No-op. This record is not referenced anywhere as a fkey.
	}

}
