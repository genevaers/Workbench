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

import com.ibm.safr.we.data.transfer.ControlRecordTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;

public class CRRecordParser extends RecordParser {

	public CRRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//ControlRecord/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {
		String fieldValue;
		ControlRecordTransfer trans = new ControlRecordTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("CONTROLRECID", record);
		trans.setId(fieldToInteger("CONTROLRECID", fieldValue));

		fieldValue = parseField("NAME", record);
		trans.setName(fieldValue.trim());

		fieldValue = parseField("FIRSTMONTH", record);
		trans.setFirstFiscalMonth(fieldToInteger("FIRSTMONTH", fieldValue));

		fieldValue = parseField("LOWVALUE", record);
		trans.setBeginPeriod(fieldToInteger("LOWVALUE", fieldValue));

		fieldValue = parseField("HIGHVALUE", record);
		trans.setEndPeriod(fieldToInteger("HIGHVALUE", fieldValue));

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

		return trans;
	}

	@Override
	public void checkReferentialIntegrity() throws SAFRValidationException {
		SAFRValidationException sve = new SAFRValidationException();
		List<Integer> crFKeys = new ArrayList<Integer>();

		Map<Integer, SAFRTransfer> crtMap = null;
		if (importer.records.containsKey(ControlRecordTransfer.class)) {
			crtMap = importer.records.get(ControlRecordTransfer.class);
		} else {
			crtMap = new HashMap<Integer, SAFRTransfer>(); // empty map
		}
		switch (importer.getComponentType()) {
        case ViewFolder:
		case View:
			// CONTROLID in View.
			Map<Integer, SAFRTransfer> viewTMap = null;
			if (importer.records.containsKey(ViewTransfer.class)) {
				viewTMap = importer.records.get(ViewTransfer.class);
			} else {
				viewTMap = new HashMap<Integer, SAFRTransfer>();// empty map
			}
			for (SAFRTransfer tfr : viewTMap.values()) {
				ViewTransfer viewTrans = (ViewTransfer) tfr;
				Integer controlId = viewTrans.getControlRecId();
				if (controlId > 0) {
					if (crtMap.get(controlId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View["
												+ viewTrans.getId()
												+ "] refers to Control Record ["
												+ controlId
												+ "] but this is not in the import file.");
						throw sve;
					}
					crFKeys.add(controlId);
				}

			}
		default:
			// no RI checks

		}// end of switch

		// Check that every CR primary key has a matching foreign key
		for (Integer crPKey : crtMap.keySet()) {
			if (!crFKeys.contains(crPKey)) {
				sve
						.setErrorMessage(
								importer.getCurrentFile().getName(),
								"Control Record ["
										+ crPKey
										+ "] is not referenced by any component in the import file.");
				throw sve;
			}
		}

	}

}
