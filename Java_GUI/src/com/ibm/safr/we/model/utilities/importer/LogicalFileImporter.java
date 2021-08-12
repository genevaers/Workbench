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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.LogicalFileTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.SAFRValidationToken;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;

public class LogicalFileImporter extends PhysicalFileImporter {
	
	// CQ10049 refactored variables
	protected LogicalFileRecordParser lfParser;
	protected LFPFAssocRecordParser lfPfParser;
	protected List<LogicalFile> lfs;
	
	public LogicalFileImporter(ImportUtility importUtility) {
		super(importUtility);
	}

	@Override
	protected void doImport() throws SAFRException, XPathExpressionException {
		SAFRValidationException sve = new SAFRValidationException();
		clearMaps();
		
		parseRecords();

		// Check that the file contains at least one Logical File record
		if (!records.containsKey(LogicalFileTransfer.class)) {
			sve.setErrorMessage(getCurrentFile().getName(),
					"There are no <LogicalFile> <Record> elements.");
			throw sve;
		}

		// Check for orphaned foreign keys and unreferenced primary keys
		checkReferentialIntegrity();

		// Check if imported components already exist in DB
		// Do this even in replaceIDs case to get existingAssocs etc
		checkDuplicateIds();

		// Check for import IDs > next key IDs (out of range)
		checkOutOfRangeIds();

		if (duplicateIdMap.size() > 0) {
			issueDuplicateIdsWarning();
		}

		checkAssociationsAndSubComponents();

		// create and validate model objects
		uxrs = createUserExitRoutines();
		pfs = createPhysicalFiles();		
		lfs = createLogicalFiles();
		
		// store all model objects within a DB transaction
		boolean success = false;
		while (!success) {
			try {
				// Begin Transaction
				DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
				DAOFactoryHolder.getDAOFactory().getDAOUOW().multiComponentScopeOn();
				for (UserExitRoutine uxr : uxrs.values()) {
					if (uxr.getPersistence() != SAFRPersistence.OLD ) {
						uxr.store();
					}
				}
				for (PhysicalFile pf : pfs) {
					if(pf.getPersistence() != SAFRPersistence.OLD ) {
						pf.store();
					}
				}
				for (LogicalFile lf : lfs) {
					if(lf.getPersistence() != SAFRPersistence.OLD ) {
						lf.store();
					}
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
	
	private Map<Integer, List<FileAssociation>> createLFPFAssociations()
			throws SAFRException {
		Map<Integer, SAFRTransfer> lfpfAtMap = null;
		if (records.containsKey(FileAssociationTransfer.class)) {
			lfpfAtMap = records.get(FileAssociationTransfer.class);
		} else {
			lfpfAtMap = new HashMap<Integer, SAFRTransfer>();// empty map
		}

		Map<Integer, List<FileAssociation>> lfpfModel = new HashMap<Integer, List<FileAssociation>>();
		for (SAFRTransfer tfr : lfpfAtMap.values()) {
			FileAssociationTransfer fileAssocTrans = (FileAssociationTransfer) tfr;
			if (fileAssocTrans.getAssociatingComponentId() > 0) {
				Integer parentFileId = fileAssocTrans
						.getAssociatingComponentId();

				// If an imported assoc already exists in target, use the
				// existing assoc instead.
				FileAssociation fileAssoc = (FileAssociation) checkForExistingAssoc(
						FileAssociation.class,
						fileAssocTrans.getAssociatingComponentId(),
						fileAssocTrans.getAssociatedComponentId());
				
				if (fileAssoc == null) {
					// Use imported association
					fileAssoc = new FileAssociation(fileAssocTrans);
				}

				// add association object to model map
				if (lfpfModel.containsKey(parentFileId)) {
					lfpfModel.get(parentFileId).add(fileAssoc);
				} else {
					List<FileAssociation> assocs = new ArrayList<FileAssociation>();
					assocs.add(fileAssoc);
					lfpfModel.put(parentFileId, assocs);
				}
			}
		}
		return lfpfModel;
	}

	/*
	 * For all imported LFs find any existing PF associations which are not
	 * present in the imported XML so they can be deleted from the target
	 * environment.
	 */
	private Map<Integer, List<FileAssociation>> findAssocsToBeDeleted()
	{
		Map<Integer, SAFRTransfer> lfpfAtMap = null;
		if (records.containsKey(FileAssociationTransfer.class)) {
			lfpfAtMap = records.get(FileAssociationTransfer.class);
		} else {
			lfpfAtMap = new HashMap<Integer, SAFRTransfer>(); // empty map
		}
		
		Map<Integer, List<FileAssociation>> assocsMap = new HashMap<Integer, List<FileAssociation>>();
		if (!lfpfAtMap.isEmpty()) {
			if (existingAssociations.containsKey(FileAssociation.class)) {
				// check for PF assocs to be deleted
				for (ComponentAssociation assoc : existingAssociations.get(
						FileAssociation.class).values()) {
					FileAssociation trgAssoc = (FileAssociation) assoc;
					// proceed if an existing assoc is not being imported
					if (!isAssocImported(trgAssoc, lfpfAtMap)) {
						// proceed if parent LF is being imported
						if (records.get(LogicalFileTransfer.class).containsKey(
								trgAssoc.getAssociatingComponentId())) {
							// assoc must be deleted
							if (assocsMap.containsKey(trgAssoc
									.getAssociatingComponentId())) {
								assocsMap.get(
										trgAssoc.getAssociatingComponentId())
										.add(trgAssoc);
							} else {
								List<FileAssociation> assocs = new ArrayList<FileAssociation>();
								assocs.add(trgAssoc);
								assocsMap.put(
										trgAssoc.getAssociatingComponentId(),
										assocs);
							}
						}
					}
				}
			}
		}
		return assocsMap; // assocs to be deleted
	}
	
	private List<FileAssociation> RIfindNewLFPFAssocs(Integer lfID,
													  SAFRList<FileAssociation> lfFileAssociations)
	{
		Map<Integer, SAFRTransfer> lfpfAtMap = records.get(FileAssociationTransfer.class);
		
		List<FileAssociation> newLFPFAssocs = new ArrayList<FileAssociation>();
		if (lfpfAtMap!= null) {	
			// Iterate through the records and create new File Associations 
			// for those not already associated with the LF
			Iterable<SAFRTransfer> recordsAssocs = lfpfAtMap.values();
			
			//Iterate through the record associations
			Iterator<SAFRTransfer> recIt = recordsAssocs.iterator();
			while (recIt.hasNext()) {
				
				//Does the association already exist in the LF
				FileAssociationTransfer recAssoc = (FileAssociationTransfer) recIt.next();
				
				// only care if the association is from this component 
				if (lfID.equals(recAssoc.getAssociatingComponentId()))  {
					boolean found = false;
					Iterator<FileAssociation> lfIt = lfFileAssociations.iterator();
					while (found == false && lfIt.hasNext()) {
						FileAssociation lfAssoc = lfIt.next();

						if (lfAssoc.getAssociatedComponentIdNum().intValue() == recAssoc.getAssociatedComponentId().intValue() &&
								lfAssoc.getAssociatingComponentId().intValue() == recAssoc.getAssociatingComponentId().intValue())
						{
							found = true;
						}
					}

					// Association not found
					if (found == false) {
						newLFPFAssocs.add(
								new FileAssociation(recAssoc));
					}
				}
			}
		}
		return newLFPFAssocs;
	}
	
	// check if an existing assoc from the target env is being imported
	protected boolean isAssocImported(ComponentAssociation trgAssoc, Map<Integer, SAFRTransfer> srcAssocs)
	{
		for (SAFRTransfer tfr : srcAssocs.values())
		{
			ComponentAssociationTransfer srcAssoc = (ComponentAssociationTransfer) tfr;
			if (trgAssoc.getAssociatedComponentIdNum().intValue() == srcAssoc.getAssociatedComponentId().intValue() &&
				trgAssoc.getAssociatingComponentId().intValue() == srcAssoc.getAssociatingComponentId().intValue())
			{
				return true;
			}
		}
		return false;
	}	

	/**
	 * Check if an imported association already exists in the target
	 * environment. That is, check if the same two components are already
	 * associated.
	 * 
	 * @param assocClass
	 *            association Class type
	 * @param associatingId
	 *            the ID of the associating (parent) component
	 * @param associatedId
	 *            the ID of the associated (child) component
	 * @return an association object if one exists, otherwise null
	 */
	protected ComponentAssociation checkForExistingAssoc(
			Class<? extends ComponentAssociation> assocClass,
			Integer associatingId, Integer associatedId) {
		ComponentAssociation assoc = null;
		if (!existingAssociations.isEmpty()) {
			for (ComponentAssociation trgAssoc : existingAssociations.get(
					assocClass).values()) {
				if (trgAssoc.getAssociatedComponentIdNum().equals(associatedId)
						&& trgAssoc.getAssociatingComponentId().equals(
								associatingId)) {
					assoc = trgAssoc; // use the existing association
				}
			}
		}
		return assoc;
	}

	/**
	 * create LF objects, add new associated PFs and validate.
	 * Note any existing LFs will be in the records map with Persistence OLD
	 * 
	 * @return
	 * @throws SAFRException
	 */
	protected List<LogicalFile> RIcreateLogicalFiles() throws SAFRException {
		
		Map<Integer, SAFRTransfer> map = records.get(LogicalFileTransfer.class);
		List<LogicalFile> lfs = new ArrayList<LogicalFile>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				LogicalFile lf = SAFRApplication.getSAFRFactory()
						.initLogicalFile((LogicalFileTransfer) tfr);
				
				// Want to find  new associations - ignore existing
				List<FileAssociation> newLFPFAssocs = RIfindNewLFPFAssocs(lf.getId(),
																		  lf.getPhysicalFileAssociations());
				
				if (!newLFPFAssocs.isEmpty()) {
					for (FileAssociation assoc : newLFPFAssocs) {
						lf.addAssociatedPhysicalFile(assoc);
					}
				}
				
				lf.validate();
				lfs.add(lf);
			}
		}
		return lfs;
	}
	
	protected List<LogicalFile> createLogicalFiles() throws SAFRException {
		
		// create File-Partition association objects
		Map<Integer, List<FileAssociation>> lfpfModel = createLFPFAssociations();

		// create LF objects, add associated PFs and validate.
		Map<Integer, SAFRTransfer> map = records.get(LogicalFileTransfer.class);
		List<LogicalFile> lfs = new ArrayList<LogicalFile>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				LogicalFile lf = SAFRApplication.getSAFRFactory()
						.initLogicalFile((LogicalFileTransfer) tfr);
				if (lfpfModel != null && !lfpfModel.isEmpty()) {
					for (FileAssociation assoc : lfpfModel.get(lf.getId())) {
						lf.addAssociatedPhysicalFile(assoc);
					}
				}
				// Delete existing LFPF associations not present in the XML
				String deps = "";
				Map<Integer, List<FileAssociation>> assocsToBeDeleted = findAssocsToBeDeleted();
				if (assocsToBeDeleted.containsKey(lf.getId())) {
					for (FileAssociation assoc : assocsToBeDeleted.get(lf
							.getId())) {
						// add assoc to imported LF so it can be deleted
						lf.addAssociatedPhysicalFile(assoc);
						SAFRValidationToken token = null;
						boolean done = false;
						while (!done) {
							try {
								lf.removeAssociatedPhysicalFile(assoc, token);
								done = true;
							} catch (SAFRValidationException ve) {
								token = ve.getSafrValidationToken();
								if (token.getValidationFailureType() == SAFRValidationType.DEPENDENCY_PF_ASSOCIATION_WARNING) {
									// add dependencies to the string
									deps += ve.getMessageString();
								} else {
									throw ve;
								}
							}
						}
					}
				}
				if (!deps.equals("")) {
					// there were deps from deleted PF assocs, ask user
					String shrtMsg = "When Logical File '"
							+ lf.getDescriptor()
							+ "' is replaced on import, the following PF associations will be deleted "
							+ "as they are not included in the import data "
							+ "and the Views which reference them will become Inactive.";
					if (!getConfirmWarningStrategy().confirmWarning(
							"Confirm Dependencies", shrtMsg, deps)) {
						// user doesn't want to continue with this import
						SAFRValidationException e1 = new SAFRValidationException();
						e1.setSafrValidationType(SAFRValidationType.DEPENDENCY_PF_ASSOCIATION_WARNING);
						String formattedMsg = splitMessage(shrtMsg);
						e1.setErrorMessage(
								LogicalFile.Property.PF_ASSOCIATION_DEP_IMPORT,
								"Import cancelled on warning about View dependencies:" + SAFRUtilities.LINEBREAK
										+ formattedMsg);
						e1.setErrorMessage(
								LogicalFile.Property.PF_ASSOCIATION_DEP, deps);
						throw e1;
					}
				}
				lf.validate();
				lfs.add(lf);
			}
		}
		return lfs;
	}
	
	protected void parseRecords() throws SAFRException,
			XPathExpressionException {
		super.parseRecords();
		lfParser = new LogicalFileRecordParser(this);
		lfParser.parseRecords();
		lfPfParser = new LFPFAssocRecordParser(this);
		lfPfParser.parseRecords();
	}
	
	protected void checkReferentialIntegrity() throws SAFRValidationException {
		super.checkReferentialIntegrity();
		lfParser.checkReferentialIntegrity();
	}
	
	protected void checkOutOfRangeIds() throws SAFRException {
		super.checkOutOfRangeIds();
		checkOutOfRangeIds(LogicalFileTransfer.class);
		checkOutOfRangeIds(FileAssociationTransfer.class);
	}
	
	/**
	 * Check any specific import rules that apply to related sub components or
	 * associations. These are defined in the parser-specfic methods called by
	 * this method.
	 * 
	 * @throws SAFRException
	 */
	protected void checkAssociationsAndSubComponents() throws SAFRException {
		// Check against any existing File-Partition associations
		lfPfParser.checkLFPFAssociations();
	}
	
	// CQ10049 end of refactored code
	
	public void buildTransferMap() 
	throws SAFRValidationException, XPathExpressionException {

		SAFRValidationException sve = new SAFRValidationException();

		clearMaps();
		// Check document element is in the form <LogicalFile-nnn>
		checkDocumentElement("LogicalFile");

		lfParser = new LogicalFileRecordParser(this);
		lfParser.parseRecords();

		// Check that the file contains at least one Logical File record
		if (!records.containsKey(LogicalFileTransfer.class)) {
			sve.setErrorMessage(getCurrentFile().getName(),
			"There are no <LogicalFile> <Record> elements.");
			throw sve;
		}

		// Parse the sub components
		lfPfParser = new LFPFAssocRecordParser(
				this);
		lfPfParser.parseRecords();
		pfParser = new PhysicalFileRecordParser(this);
		pfParser.parseRecords();
		procParser = new ExitRecordParser(this);
		procParser.parseRecords();

		// Check for orphaned foreign keys and unreferenced primary keys
		lfParser.checkReferentialIntegrity();
		pfParser.checkReferentialIntegrity();
		procParser.checkReferentialIntegrity();

	}
	
}
