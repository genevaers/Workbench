package com.ibm.safr.we.model.utilities.importer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.genevaers.ccb2lr.Copybook2LR;
import org.genevaers.ccb2lr.RecordField;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LogicalRecord;
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

	public void importCopybook(ImportFile file, Integer envid) throws SAFRException, XPathExpressionException {
		cbFile = file;
		environementId = envid;
		doImport();
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
		trans.setDecimalPlaces(0);
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
        trans.setPersistent(false);
        trans.setForImport(true);
        
		return trans;
	}

	@Override
	protected void doImport() throws SAFRException, XPathExpressionException {
		clearMaps();
		Copybook2LR ccb2lr = new Copybook2LR();
		try {
			ccb2lr.processCopybook(cbFile.getFile().toPath());
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
