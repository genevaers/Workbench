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

import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.LogicalFileTransfer;
import com.ibm.safr.we.data.transfer.SAFREnvironmentalComponentTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;

/**
 * This class will parse a &LT;LogicalFile&GT; &LT;Record&GT; element into a
 * LogicalFileTransfer object.
 */
public class LogicalFileRecordParser extends RecordParser {

	public LogicalFileRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//LogicalFile/Record";
	}

	@Override
	protected SAFREnvironmentalComponentTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;
		LogicalFileTransfer trans = new LogicalFileTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("LOGFILEID", record);
		trans.setId(fieldToInteger("LOGFILEID", fieldValue));

		fieldValue = parseField("NAME", record);
		trans.setName(fieldValue.trim());

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
		List<Integer> lfFKeys = new ArrayList<Integer>();

		Map<Integer, SAFRTransfer> lftMap = null;
		if (importer.records.containsKey(LogicalFileTransfer.class)) {
			lftMap = importer.records.get(LogicalFileTransfer.class);
		} else {
			lftMap = new HashMap<Integer, SAFRTransfer>(); // empty map
		}

		// Check for orphaned LF foreign keys.
		// PARENTFILEID in LFPF assoc
		// FILEID in LRLF assoc

		switch (importer.getComponentType()) {
		case ViewFolder:
		case View:
		case LookupPath:
		case LogicalRecord:
			// FILEID in LRLF assoc
			Map<Integer, SAFRTransfer> catMap = null;
			if (importer.records
					.containsKey(ComponentAssociationTransfer.class)) {
				catMap = importer.records
						.get(ComponentAssociationTransfer.class);
			} else {
				catMap = new HashMap<Integer, SAFRTransfer>(); // empty map
			}
			for (SAFRTransfer tfr : catMap.values()) {
				ComponentAssociationTransfer cat = (ComponentAssociationTransfer) tfr;
				Integer fileId = cat.getAssociatedComponentId();
				if (fileId > 0) {
					if (!lftMap.containsKey(fileId)) {
						sve.setErrorMessage(importer.getCurrentFile().getName(),"LRFile association ["+ cat.getAssociationId()+ 
						    "] refers to Logical File ["+ fileId+ "] but this is not present in the import file.");
						throw sve;
					}
					lfFKeys.add(fileId);
				}
			}
			// no break, continue
		default:
			// LogicalFile or PhysicalFile
			// PARENTFILEID in LFPF assoc
			Map<Integer, SAFRTransfer> fatMap = null;
			if (importer.records.containsKey(FileAssociationTransfer.class)) {
				fatMap = importer.records.get(FileAssociationTransfer.class);
			} else {
				fatMap = new HashMap<Integer, SAFRTransfer>(); // empty map
			}
			for (SAFRTransfer tfr : fatMap.values()) {
				FileAssociationTransfer fat = (FileAssociationTransfer) tfr;
				Integer parentFileId = fat.getAssociatingComponentId();
				if (parentFileId > 0) {
					if (!lftMap.containsKey(parentFileId)) {
						sve.setErrorMessage(importer.getCurrentFile().getName(),
						    "File-Partition association ["+ fat.getAssociatedComponentName()+ "] refers to Logical File ["+ 
						    parentFileId+ "] but this is not present in the import file.");
						throw sve;
					}
					lfFKeys.add(parentFileId);
				}
			}
		} // end switch

		// Check that every LF primary key has a matching foreign key
		for (Integer lfPKey : lftMap.keySet()) {
			if (!lfFKeys.contains(lfPKey)) {
				sve.setErrorMessage(importer.getCurrentFile().getName(),
				    "Logical File ["+ lfPKey+ "] is not referenced by any component in the import file.");
				throw sve;
			}
		}
	}

}
