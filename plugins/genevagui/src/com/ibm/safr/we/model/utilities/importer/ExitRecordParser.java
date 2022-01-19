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


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.data.transfer.SAFREnvironmentalComponentTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;

/**
 * This class will parse a &LT;Procedure&GT; &LT;Record&GT; element into a
 * UserExitRoutineTransfer object.
 */
public class ExitRecordParser extends RecordParser {

	public ExitRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//Exit/Record";
	}

	@Override
	protected SAFREnvironmentalComponentTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;
		UserExitRoutineTransfer trans = new UserExitRoutineTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("EXITID", record);
		trans.setId(fieldToInteger("EXITID", fieldValue));

		fieldValue = parseField("NAME", record);
		trans.setName(fieldValue.trim());

		fieldValue = parseField("MODULEID", record);
		trans.setExecutable(fieldValue.trim());

		fieldValue = parseField("EXITTYPECD", record);
		trans.setTypeCode(fieldValue.trim());

		fieldValue = parseField("PROGRAMTYPECD", record);
		trans.setLanguageCode(fieldValue.trim());

		fieldValue = parseField("OPTIMIZEIND", record);
		trans.setOptimize(DataUtilities.intToBoolean(fieldToInteger(
				"OPTIMIZEIND", fieldValue)));

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

	/**
	 * Checks that all user exit routine foreign keys have a matching user exit
	 * routine record in the XML (no orphaned foreign keys) and that all user
	 * exit routine records are referenced by foreign keys in the XML (no
	 * independent user exit routines).
	 * 
	 * @throws SAFRValidationException
	 */
	public void checkReferentialIntegrity() throws SAFRValidationException {
		SAFRValidationException sve = new SAFRValidationException();
		Set<Integer> uxrFKeys = new HashSet<Integer>();

		Map<Integer, SAFRTransfer> uxtMap = null;
		if (importer.records.containsKey(UserExitRoutineTransfer.class)) {
			uxtMap = importer.records.get(UserExitRoutineTransfer.class);
		} else {
			uxtMap = new HashMap<Integer, SAFRTransfer>(); // empty map
		}

		// Check for orphaned user exit foreign keys.
		// WRITEEXITID and FORMATEXITID in View
		// LOOKUPEXITID in LR
		// READEXITID in PF

		switch (importer.getComponentType()) {
	    case ViewFolder:
	    case View:
			// WRITEEXITID and FORMATEXITID in View
			Map<Integer, SAFRTransfer> vwtMap = null;
			if (importer.records.containsKey(ViewTransfer.class)) {
				vwtMap = importer.records.get(ViewTransfer.class);
			} else {
				vwtMap = new HashMap<Integer, SAFRTransfer>(); // empty map
			}
			for (SAFRTransfer tfr : vwtMap.values()) {
				ViewTransfer vwt = (ViewTransfer) tfr;
				Integer writeExitId = vwt.getWriteExitId();
				Integer formatExitId = vwt.getFormatExitId();
				if (writeExitId != null && writeExitId > 0) {
					if (!uxtMap.containsKey(writeExitId)) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View ["
												+ vwt.getId()
												+ "] refers to 'Write' User Exit Routine ["
												+ writeExitId
												+ "] but this is not present in the import file.");
						throw sve;
					}
					uxrFKeys.add(writeExitId);
				}
				if (formatExitId != null && formatExitId > 0) {
					if (!uxtMap.containsKey(formatExitId)) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View ["
												+ vwt.getId()
												+ "] refers to 'Format' User Exit Routine ["
												+ formatExitId
												+ "] but this is not present in the import file.");
						throw sve;
					}
					uxrFKeys.add(formatExitId);
				}
			}
			// no break, continue
		case LookupPath:
		case LogicalRecord:
			// LOOKUPEXITID in LR
			Map<Integer, SAFRTransfer> lrtMap = null;
			if (importer.records.containsKey(LogicalRecordTransfer.class)) {
				lrtMap = importer.records.get(LogicalRecordTransfer.class);
			} else {
				lrtMap = new HashMap<Integer, SAFRTransfer>(); // empty map
			}
			for (SAFRTransfer tfr : lrtMap.values()) {
				LogicalRecordTransfer lrt = (LogicalRecordTransfer) tfr;
				Integer lookupExitId = lrt.getLookupExitId();
				if (lookupExitId != null && lookupExitId > 0) {
					if (!uxtMap.containsKey(lookupExitId)) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"Logical Record ["
												+ lrt.getId()
												+ "] refers to 'Lookup' User Exit Routine ["
												+ lookupExitId
												+ "] but this is not present in the import file.");
						throw sve;
					}
					uxrFKeys.add(lookupExitId);
				}
			}
			// no break, continue
		default:
			// LogicalFile or PhysicalFile
			// READEXITID in PF
			Map<Integer, SAFRTransfer> pftMap = null;
			if (importer.records.containsKey(PhysicalFileTransfer.class)) {
				pftMap = importer.records.get(PhysicalFileTransfer.class);
			} else {
				pftMap = new HashMap<Integer, SAFRTransfer>(); // empty map
			}
			for (SAFRTransfer tfr : pftMap.values()) {
				PhysicalFileTransfer pft = (PhysicalFileTransfer) tfr;
				Integer readExitId = pft.getReadExitId();
				if (readExitId != null && readExitId > 0) {
					if (!uxtMap.containsKey(readExitId)) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"Physical File ["
												+ pft.getId()
												+ "] refers to 'Read' User Exit Routine ["
												+ readExitId
												+ "] but this is not in the import file.");
						throw sve;
					}
					uxrFKeys.add(readExitId);
				}
			}
		} // end switch

		// Check for foreign keys in logic text when importing a view
		if (importer.getComponentType() == ComponentType.View || importer.getComponentType() == ComponentType.ViewFolder)
		{
			findLogicTextFK(uxtMap, uxrFKeys);
		}
		
		// Check that every User Exit primary key has a matching foreign key
		for (Integer uxrPKey : uxtMap.keySet()) {
			if (!uxrFKeys.contains(uxrPKey)) {
				sve
						.setErrorMessage(
								importer.getCurrentFile().getName(),
								"User Exit Routine ["
										+ uxrPKey
										+ "] is not referenced by any component in the import file.");
				throw sve;
			}
		}
	}

	private void findLogicTextFK(
			Map<Integer, SAFRTransfer> uxtMap, 
			Set<Integer> uxrFKeys)	{
		
		// look through logic text in each View Source (extract filter)
		Map<Integer,SAFRTransfer> srcMap = importer.records.get(ViewSourceTransfer.class);
		if (srcMap != null) {
			for (Integer i : srcMap.keySet()) {

				// get extract filter logic
				ViewSourceTransfer trans = (ViewSourceTransfer) srcMap.get(i);
				String extFilText = trans.getExtractFilterLogic();
				if (extFilText != null) {
	                checkExitContained(uxtMap, uxrFKeys, extFilText);
				}

				// get extract source logic 
                String extSrcText = trans.getExtractRecordOutput();
                if (extSrcText != null) {
                    checkExitContained(uxtMap, uxrFKeys, extSrcText);
                }
			}
		}
		// loop through logic text in each View Column 
		Map<Integer,SAFRTransfer> colSrcMap = importer.records.get(ViewColumnSourceTransfer.class);
		if (colSrcMap != null) {
			for (Integer i : colSrcMap.keySet()) {

				ViewColumnSourceTransfer trans = (ViewColumnSourceTransfer) colSrcMap.get(i);
				
				// get extract column logic
				String extColText = trans.getExtractColumnLogic();
				
				checkExitContained(uxtMap, uxrFKeys, extColText);
			}
		}		
	}

    protected void checkExitContained(Map<Integer, SAFRTransfer> uxtMap,
        Set<Integer> uxrFKeys, String extFilText) {
        // check if exit is contained in logic text (WRITE function)
        for (Map.Entry<Integer, SAFRTransfer> ent : uxtMap.entrySet()) {

        	UserExitRoutineTransfer exitTrans = (UserExitRoutineTransfer) ent
        			.getValue();
        	if (uxrFKeys.contains(exitTrans.getId())
        			|| !exitTrans.getTypeCode().equals("WRITE")) {
        		continue;
        	}
        	Pattern patternName = Pattern.compile(
        			".*\\{" + exitTrans.getName() + "\\}.*",
        			Pattern.DOTALL);
        	Matcher matcherName = patternName.matcher(extFilText);
        	Pattern patternExec = Pattern.compile(
        			".*\\{" + exitTrans.getExecutable() + "\\}.*",
        			Pattern.DOTALL);
        	Matcher matcherExec = patternExec.matcher(extFilText);
        	if (matcherName.matches() || matcherExec.matches()) {
        		uxrFKeys.add(exitTrans.getId());
        	}
        }
    }
	
	public void replaceForeignKeyIds(
			Map<Class<? extends SAFRTransfer>, List<Integer>> outOfRangeIdMap) {
		if (outOfRangeIdMap.containsKey(UserExitRoutineTransfer.class)) {
			for (Integer oldId : outOfRangeIdMap
					.get(UserExitRoutineTransfer.class)) {

				// Get the new pkey ID
				Map<Integer, SAFRTransfer> uxtMap = importer.records
						.get(UserExitRoutineTransfer.class);
				UserExitRoutineTransfer uxt = (UserExitRoutineTransfer) uxtMap
						.get(oldId);
				Integer newId = uxt.getId();

				// Change fkey refs from old pkey to new pkey

				if (importer.records.containsKey(PhysicalFileTransfer.class)) {
					for (SAFRTransfer trans : importer.records.get(
							PhysicalFileTransfer.class).values()) {
						PhysicalFileTransfer pft = (PhysicalFileTransfer) trans;
						if (oldId.equals(pft.getReadExitId())) {
							pft.setReadExitId(newId); // READEXITID
						}
					}
				}

				if (importer.records.containsKey(LogicalRecordTransfer.class)) {
					for (SAFRTransfer trans : importer.records.get(
							LogicalRecordTransfer.class).values()) {
						LogicalRecordTransfer lrt = (LogicalRecordTransfer) trans;
						if (oldId.equals(lrt.getLookupExitId())) {
							lrt.setLookupExitId(newId); // LOOKUPEXITID
						}
					}
				}

				if (importer.records.containsKey(ViewTransfer.class)) {
					for (SAFRTransfer trans : importer.records.get(
							ViewTransfer.class).values()) {
						ViewTransfer vt = (ViewTransfer) trans;
						if (oldId.equals(vt.getWriteExitId())) {
							vt.setWriteExitId(newId); // WRITEEXITID
						}
						if (oldId.equals(vt.getFormatExitId())) {
							vt.setFormatExitId(newId); // FORMATEXITID
						}
					}
				}
			}
		}// end for oldid
	}

}
