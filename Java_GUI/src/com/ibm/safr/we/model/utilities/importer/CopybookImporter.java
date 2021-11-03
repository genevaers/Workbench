package com.ibm.safr.we.model.utilities.importer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.genevaers.ccb2lr.Copybook2LR;
import org.genevaers.ccb2lr.RecordField;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.model.SAFRApplication;

public class CopybookImporter {

	//limit the number transfer objects for the moment
	protected Map<Integer, SAFRTransfer> records = new HashMap<Integer, SAFRTransfer>();
	protected Map<Integer, SAFRTransfer> fieldTxfrs = new HashMap<Integer, SAFRTransfer>();
	
	private static int ENVID = 1;

	public void importCopybook(String filename) {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get(filename);
		try {
			ccb2lr.processCopybook(testPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ccb2lr.generateData();
		RecordField rf = ccb2lr.getRecordField();
		rf.resolvePositions();
		
		//Make the YAML object as if we were going to write it
		ccb2lr.addRecordFieldToYamlTree();
		//now that we have record make some transfer objects
		ObjectNode yamlRecord = ccb2lr.getRecord();
		addTransferObjectsToRecords(yamlRecord);
		
	}

	private void addTransferObjectsToRecords(ObjectNode yamlRecord) {
		// TODO Auto-generated method stub
		String rec = yamlRecord.get("recordName").asText();
		LogicalRecordTransfer lrt = makeLRTransfer(rec);
		ArrayNode flds = (ArrayNode) yamlRecord.get("fields");
		createFieldTransferObjects(flds);
		
	}

	private void createFieldTransferObjects(ArrayNode flds) {
		for(int i=0; i<flds.size(); i++) {
			LRFieldTransfer fx = makeFieldTransfer(flds.get(i));
			addFieldAttrTransfer(flds.get(i), fx);
			fieldTxfrs.put(fx.getId(), fx);
		}
	}

	private void addFieldAttrTransfer(JsonNode fieldNode, LRFieldTransfer trans) {
		trans.setDataType(fieldNode.get("datatype").asText());
		trans.setSigned(fieldNode.get("signed").asBoolean());
		trans.setLength(fieldNode.get("length").asInt());
		//Should make this one real
		trans.setDecimalPlaces(0);
		trans.setScalingFactor(0);
		trans.setDateTimeFormat("None");
		trans.setHeaderAlignment("None");
		trans.setColumnHeading1("");
		trans.setColumnHeading2("");
		trans.setColumnHeading3("");
        trans.setSubtotalLabel("");
		trans.setSortKeyLabel("");
		trans.setNumericMask("");
	}

	private LRFieldTransfer makeFieldTransfer(JsonNode fieldNode) {
		//Two parts - did we merge them? No is the answer
		LRFieldTransfer trans = new LRFieldTransfer();

		trans.setEnvironmentId(ENVID);
        trans.setId(SAFRApplication.getSAFRFactory().getNextLRFieldId());
		trans.setLrId(99);
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
		trans.setModifyBy("CCBLR");

		// set these default values for index.
		// Index parsers will set these values later.
		trans.setEffEndDate(false);
		trans.setEffStartDate(false);
		trans.setPkeySeqNo(0);

		return trans;
	}

	private LogicalRecordTransfer makeLRTransfer(String rec) {
		LogicalRecordTransfer trans = new LogicalRecordTransfer();

		//Need to get the target env id - hard code for moment
		trans.setEnvironmentId(ENVID);
		//We need to get the next LR id - again hard code 
		trans.setId(99);
		trans.setName(rec);

		trans.setLrTypeCode("FILE");
		trans.setLrStatusCode("ACTVE");
		trans.setLookupExitId(0);
		trans.setLookupExitParams("");
		trans.setComments("");
		trans.setCreateTime(new Date());
		trans.setCreateBy("CCB2LR");
		trans.setModifyTime(new Date());
		trans.setModifyBy("");
        trans.setActivatedTime(new Date());
        trans.setActivatedBy("CCB2LR");
        
		return trans;
	}
}
