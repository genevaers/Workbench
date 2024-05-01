package com.ibm.safr.we.model.utilities.importer;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.genevaers.ccb2lr.CobolCollection;
import org.genevaers.ccb2lr.Copybook2LR;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LogicalRecord.Property;
import com.ibm.safr.we.model.SAFRApplication;

public class CopybookImporter extends LogicalRecordImporter {

	public CopybookImporter(ImportUtility importUtility) {
		super(importUtility);
		// TODO Auto-generated constructor stub
	}

	public CopybookImporter() {
		super(null);
		// TODO Auto-generated constructor stub
	}

	//limit the number transfer objects for the moment
	protected Map<Integer, SAFRTransfer> lrrecords = new HashMap<Integer, SAFRTransfer>();
	protected Map<Integer, SAFRTransfer> fieldTxfrs = new HashMap<Integer, SAFRTransfer>();
	private int lrID = 2300; //Need to manage this properly - init with max id + 1
	private int fieldID = 2300; //Need to manage this properly - init with max id + 1
	private ImportFile cbFile;;
	
	private int environementId;

	public void importCopybook(ImportFile file, Integer envid) {
		try {
			cbFile = file;
			environementId = envid;
			doImport();
			file.setResult(ActivityResult.PASS);
		}  catch (SAFRValidationException e) {
			String msg = e.getMessage()
					+ ". "
					+ (e.getCause() != null ? e.getCause().getMessage()
							: "");
			file.setResult(ActivityResult.FAIL);
			file.setErrorMsg(msg);
		}
	}

	private void addTransferObjectsToRecords(ObjectNode yamlRecord) {
		// TODO Auto-generated method stub
		String rec = yamlRecord.get("recordName").asText();
		LogicalRecordTransfer lrt = makeLRTransfer(rec);
		lrrecords.put(lrt.getId(), lrt);
		records.put(LogicalRecordTransfer.class, lrrecords);
		ArrayNode flds = (ArrayNode) yamlRecord.get("fields");
		createFieldTransferObjects(flds);
		records.put(LRFieldTransfer.class, fieldTxfrs);
		
	}

	private void createFieldTransferObjects(ArrayNode flds) {
		for(int i=0; i<flds.size(); i++) {
			LRFieldTransfer txfr = makeFieldTransfer(flds.get(i));
			fieldTxfrs.put(txfr.getId(), txfr);
		}
	}

	private LRFieldTransfer makeFieldTransfer(JsonNode fieldNode) {
		//Two parts - field and attr
		LRFieldTransfer trans = new LRFieldTransfer();

		trans.setEnvironmentId(environementId);

        trans.setId(SAFRApplication.getSAFRFactory().getNextLRFieldId());
		trans.setLrId(lrID);
		trans.setName(fieldNode.get("name").asText());
		trans.setDbmsColName("");
		trans.setFixedStartPos(fieldNode.get("position").asInt());
		trans.setOrdinalPos(0);
		trans.setOrdinalOffset(0);
        trans.setRedefine(0);
		trans.setComments("");
		trans.setCreateTime(new Date());
		trans.setCreateBy("CCB2LR");
		trans.setModifyTime(new Date());
		trans.setModifyBy("");
		trans.setLength(fieldNode.get("length").asInt());
		trans.setDataType(fieldNode.get("datatypeCode").asText());
		trans.setDecimalPlaces(fieldNode.get("decimalPlaces").asInt());
		trans.setSigned(fieldNode.get("signed").asBoolean());
		trans.setDateTimeFormat(null);
		trans.setScalingFactor(0);

		// set these default values for index.
		// Index parsers will set these values later.
		trans.setEffEndDate(false);
		trans.setEffStartDate(false);
		trans.setPkeySeqNo(0);
        trans.setPersistent(false);
        trans.setForImport(true);

		return trans;
	}

	private LogicalRecordTransfer makeLRTransfer(String rec) {
		LogicalRecordTransfer trans = new LogicalRecordTransfer();

		trans.setEnvironmentId(environementId);
		lrID = SAFRApplication.getSAFRFactory().getNextLRId();
		trans.setId(lrID);
		trans.setName(rec);

		trans.setLrTypeCode("FILE");
		trans.setLrStatusCode("INACT");
		trans.setLookupExitId(0);
		trans.setLookupExitParams("");
		trans.setComments("");
		trans.setCreateTime(new Date());
		trans.setCreateBy("CCB2LR");
		trans.setModifyTime(new Date());
		trans.setModifyBy("");
        trans.setActivatedTime(new Date());
        trans.setActivatedBy("CCB2LR");
        trans.setPersistent(false);
        trans.setForImport(true);
        
		return trans;
	}

	@Override
	protected void doImport() throws SAFRValidationException {
		clearMaps();
		Copybook2LR ccb2lr = new Copybook2LR();
		try {
			ccb2lr.processCopybook(cbFile.getFile().toPath());
		} catch (IOException e) {
			SAFRValidationException sve = new SAFRValidationException();
			String err = "Unable to process copybook. IOException";
			sve.setErrorMessage(Property.LR_NAME, err);
			throw sve;
		}
		if(ccb2lr.hasErrors()) {
			SAFRValidationException sve = new SAFRValidationException();
			String err = "Copybook parsing error(s) \n";
			Iterator<String> ei = ccb2lr.getErrors().iterator();
			while(ei.hasNext()) {
				err += ei.next() +"\n"; 
			}
			sve.setErrorMessage(Property.LR_NAME, err);
			throw sve;
		} else {
			//Make the YAML object as if we were going to write it
			ccb2lr.addRecordFieldToYamlTree();
			//now that we have record make some transfer objects
			ObjectNode yamlRecord = ccb2lr.getRecord();
			addTransferObjectsToRecords(yamlRecord);
	        generateOrdPos();
	        generateRedefine();
	
			
			List<LogicalRecord> lrs = createLogicalRecords();
			for (LogicalRecord lr : lrs) {
				if(lr.getPersistence() != SAFRPersistence.OLD ) {
					lr.isForImport();
					lr.store();
				}
			}
		}
	}
}
