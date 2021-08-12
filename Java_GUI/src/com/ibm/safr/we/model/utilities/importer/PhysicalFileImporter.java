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

import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;

/**
 * This subclass of ComponentImporter imports XML representing a Physical File.
 * It implements the abstract method doImport() which defines the import
 * behavior specific to Physical Files.
 * 
 */
public class PhysicalFileImporter extends ComponentImporter {

	// CQ10049 refactored variables
    protected GenerationRecordParser genParser;
	protected ExitRecordParser procParser;
	protected Map<Integer, UserExitRoutine> uxrs;
	protected PhysicalFileRecordParser pfParser;
	protected List<PhysicalFile> pfs;

	public PhysicalFileImporter(ImportUtility importUtility) {
		super(importUtility);
	}

	protected void doImport() throws SAFRException, XPathExpressionException {

		SAFRValidationException sve = new SAFRValidationException();
		clearMaps();
		
		parseRecords();

		// Check that the file contains at least one PF record
		if (!records.containsKey(PhysicalFileTransfer.class)) {
			sve.setErrorMessage(getCurrentFile().getName(),
			"There are no <PhysicalFile> <Record> elements.");
			throw sve;
		}

		// Check for orphaned foreign keys and unreferenced primary keys
		checkReferentialIntegrity();

		// Check if imported components already exist in DB
		checkDuplicateIds();

		// Check for import IDs > next key IDs (out of range)
		checkOutOfRangeIds();

		if (duplicateIdMap.size() > 0) {
			issueDuplicateIdsWarning();
		}
		
		// Create model objects and validate them.
		// Store all model objects within a DB transaction
		// create and validate model objects.
		uxrs = createUserExitRoutines();
		pfs = createPhysicalFiles();
		boolean success = false;
		while (!success) {
			try {
				// Begin Transaction
				DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
				DAOFactoryHolder.getDAOFactory().getDAOUOW().multiComponentScopeOn();
				for (UserExitRoutine uxr : uxrs.values()) {
					uxr.store();
				}
				for (PhysicalFile pf : pfs) {
					pf.store();
				}
				success = true;
			} catch (DAOUOWInterruptedException e) {
				// UOW interrupted so retry it
				continue;
			} finally {
				DAOFactoryHolder.getDAOFactory().getDAOUOW().multiComponentScopeOff();
				if (success) {
					// Complete the transaction.
					DAOFactoryHolder.getDAOFactory().getDAOUOW().end();

				} else {
					// Fail the transaction.
					DAOFactoryHolder.getDAOFactory().getDAOUOW().fail();
				}
			}
		} 

	}

	// CQ10049 refactored code follows...

	protected Map<Integer, UserExitRoutine> createUserExitRoutines()
	throws SAFRException {
		Map<Integer, UserExitRoutine> uxrs = new HashMap<Integer, UserExitRoutine>();
		Map<Integer, SAFRTransfer> map = records
		.get(UserExitRoutineTransfer.class);
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				UserExitRoutineTransfer uxrTrans = (UserExitRoutineTransfer) tfr;
				UserExitRoutine uxr = SAFRApplication.getSAFRFactory()
				.initUserExitRoutine(uxrTrans);
				uxr.setConfirmWarningStrategy(getConfirmWarningStrategy());
				uxr.validate();
				uxrs.put(uxr.getId(), uxr);
			}
		}
		return uxrs;
	}

	protected List<PhysicalFile> createPhysicalFiles() throws SAFRException {
		Map<Integer, SAFRTransfer> map = records
		.get(PhysicalFileTransfer.class);
		List<PhysicalFile> pfs = new ArrayList<PhysicalFile>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				PhysicalFileTransfer pfTrans = (PhysicalFileTransfer) tfr;
				PhysicalFile pf = SAFRApplication.getSAFRFactory()
				.initPhysicalFile(pfTrans);
				if (pfTrans.getReadExitId() > 0) {
					// get the UXR and add to PF model
					UserExitRoutine uxr = uxrs.get(pfTrans.getReadExitId());
					pf.setUserExitRoutine(uxr);
				}
				pf.validate();
				pfs.add(pf);
			}
		}
		return pfs;
	}

	protected void parseRecords() throws SAFRException,
	XPathExpressionException {
        genParser = new GenerationRecordParser(this);
        genParser.parseRecords();
		procParser = new ExitRecordParser(this);
		procParser.parseRecords();
		pfParser = new PhysicalFileRecordParser(this);
		pfParser.parseRecords();
	}

	protected void checkReferentialIntegrity() throws SAFRValidationException {
		pfParser.checkReferentialIntegrity();
		procParser.checkReferentialIntegrity();
	}

	protected void checkOutOfRangeIds() throws SAFRException {
		checkOutOfRangeIds(UserExitRoutineTransfer.class);
		checkOutOfRangeIds(PhysicalFileTransfer.class);
	}

	public void buildTransferMap()
	throws SAFRValidationException, XPathExpressionException {
		SAFRValidationException sve = new SAFRValidationException();

		clearMaps();
		// Check document element is in the form <PhysicalFile-nnn>
		checkDocumentElement("PhysicalFile");

		pfParser = new PhysicalFileRecordParser(this);
		pfParser.parseRecords();

		// Check that the file contains at least one PF record
		if (!records.containsKey(PhysicalFileTransfer.class)) {
			sve.setErrorMessage(getCurrentFile().getName(),
			"There are no <PhysicalFile> <Record> elements.");
			throw sve;
		}

		// Parse the sub components
		procParser = new ExitRecordParser(this);
		procParser.parseRecords();

		// Check for orphaned foreign keys and unreferenced primary keys
		pfParser.checkReferentialIntegrity();
		procParser.checkReferentialIntegrity();
	}
}
