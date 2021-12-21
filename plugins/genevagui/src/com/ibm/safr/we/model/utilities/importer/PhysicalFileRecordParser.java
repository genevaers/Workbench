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

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.data.transfer.SAFREnvironmentalComponentTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.utilities.FileUtils;

/**
 * This class will parse a &LT;PhysicalFile&GT; &LT;Record&GT; element into a
 * PhysicalFileTransfer object.
 */
public class PhysicalFileRecordParser extends RecordParser {

	public PhysicalFileRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//PhysicalFile/Record";
	}

	@Override
	protected SAFREnvironmentalComponentTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;
		PhysicalFileTransfer trans = new PhysicalFileTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("PHYFILEID", record);
		trans.setPartitionId(fieldToInteger("PHYFILEID", fieldValue));

		fieldValue = parseField("NAME", record);
		trans.setPartitionName(fieldValue.trim());

		fieldValue = parseField("FILETYPECD", record);
		trans.setFileTypeCode(fieldValue.trim());

        fieldValue = parseField("DISKFILETYPECD", record);
        trans.setDiskFileTypeCode(fieldValue.trim());
		
		fieldValue = parseField("ACCESSMETHODCD", record);
		// translate old access codes		
		if (fieldValue.equals("EXCP ") || fieldValue.equals("SEQEX")) {
		    fieldValue = "SEQIN";
		}
		trans.setAccessMethodCode(fieldValue.trim());

		fieldValue = parseField("READEXITID", record);
		Integer exitId = fieldToInteger("READEXITID", fieldValue);
		if (exitId == null) {
			exitId = 0;
		}
		trans.setReadExitId(exitId);

		fieldValue = parseField("READEXITSTARTUP", record);
		trans.setReadExitParams(fieldValue.trim());

        fieldValue = parseField("DDNAMEINPUT", record);
        trans.setInputDDName(fieldValue.trim());

        fieldValue = parseField("DSN", record);
        trans.setDatasetName(fieldValue.trim());

        fieldValue = parseField("MINRECLEN", record);
        trans.setMinRecordLen(fieldToInteger("MINRECLEN", fieldValue, 0));

        fieldValue = parseField("MAXRECLEN", record);
        trans.setMaxRecordLen(fieldToInteger("MAXRECLEN", fieldValue, 0));
        
        fieldValue = parseField("DDNAMEOUTPUT", record);
        trans.setOutputDDName(fieldValue.trim());

        fieldValue = parseField("RECFM", record);
        trans.setRecfm(fieldValue.trim());
        
        fieldValue = parseField("LRECL", record);
        trans.setLrecl(fieldToInteger("CLRECL", fieldValue, 0));        
		
		fieldValue = parseField("DBMSSUBSYS", record);
		trans.setSubSystem(fieldValue.trim());

        fieldValue = parseField("DBMSSQL", record);
        trans.setSqlStatement(FileUtils.remBRLineEndings(fieldValue.trim()));
		
		fieldValue = parseField("DBMSTABLE", record);
		trans.setTableName(fieldValue.trim());

		fieldValue = parseField("DBMSROWFMTCD", record);
		trans.setRowFormatCode(fieldValue.trim());

		fieldValue = parseField("DBMSINCLNULLSIND", record);
		trans.setIncludeNullIndicators(DataUtilities
				.intToBoolean(fieldToInteger("DBMSINCLNULLSIND", fieldValue)));

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
		List<Integer> pfFKeys = new ArrayList<Integer>();

		Map<Integer, SAFRTransfer> pfMap = null;
		if (importer.records.containsKey(PhysicalFileTransfer.class)) {
			pfMap = importer.records.get(PhysicalFileTransfer.class);
		} else {
			pfMap = new HashMap<Integer, SAFRTransfer>(); // empty map
		}

		// Check for orphaned PF foreign keys.
		// CHILDPARTITIONID in LFPF association

		Map<Integer, SAFRTransfer> fatMap = null;
		if (importer.records.containsKey(FileAssociationTransfer.class)) {
			fatMap = importer.records.get(FileAssociationTransfer.class);
		} else {
			fatMap = new HashMap<Integer, SAFRTransfer>(); // empty map
		}
		for (SAFRTransfer tfr : fatMap.values()) {
			FileAssociationTransfer fat = (FileAssociationTransfer) tfr;
			Integer childPartitionId = fat.getAssociatedComponentId();
			if (childPartitionId > 0) {
				if (!pfMap.containsKey(childPartitionId)) {
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"File-Partition association ["
											+ fat.getAssociationId()
											+ "] refers to Physical File ["
											+ childPartitionId
											+ "] but this is not present in the import file.");
					throw sve;
				}
				pfFKeys.add(childPartitionId);
			}
		}

		// Check the pkey of every non-top level PF is referenced by a fkey
		if (!importer.getComponentType().equals(ComponentType.PhysicalFile)) {
			for (Integer pfPKey : pfMap.keySet()) {
				if (!pfFKeys.contains(pfPKey)) {
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"Physical File ["
											+ pfPKey
											+ "] is not referenced by any component in the import file.");
					throw sve;
				}
			}
		}

	}

	public void replaceForeignKeyIds(
			Map<Class<? extends SAFRTransfer>, List<Integer>> outOfRangeIdMap) {
		if (outOfRangeIdMap.containsKey(PhysicalFileTransfer.class)) {
			for (Integer oldId : outOfRangeIdMap
					.get(PhysicalFileTransfer.class)) {

				// For each old pkey ID, get the new pkey
				Map<Integer, SAFRTransfer> tfrMap = importer.records
						.get(PhysicalFileTransfer.class);
				PhysicalFileTransfer tfr = (PhysicalFileTransfer) tfrMap
						.get(oldId);
				Integer newId = tfr.getId();

				// Replace all fkey refs to old pkey with the pkey

				if (importer.records.containsKey(FileAssociationTransfer.class)) {
					for (SAFRTransfer trans : importer.records.get(
							FileAssociationTransfer.class).values()) {
						FileAssociationTransfer fat = (FileAssociationTransfer) trans;
						if (oldId.equals(fat.getAssociatedComponentId())) {
							fat.setAssociatedComponentId(newId); // CHILDPARTITIONID
						}
					}
				}
			}
		}// end for oldid
	}

}
