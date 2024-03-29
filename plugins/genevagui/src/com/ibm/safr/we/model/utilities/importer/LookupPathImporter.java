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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.LookupPathStepTransfer;
import com.ibm.safr.we.data.transfer.LookupPathTransfer;
import com.ibm.safr.we.data.transfer.SAFREnvironmentalComponentTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.LookupPath.Property;
import com.ibm.safr.we.model.LookupPathSourceField;
import com.ibm.safr.we.model.LookupPathStep;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRValidationToken;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.ComponentAssociation;

public class LookupPathImporter extends LogicalRecordImporter implements
		LRInformationProvider {

	// CQ10049 refactored variables
	protected LookupRecordParser joinParser;
	protected LookupSourceKeyRecordParser joinSrcParser;
	protected LookupStepRecordParser joinTrgParser;
	protected List<LookupPath> lks;
	
	public LookupPathImporter(ImportUtility importUtility) {
		super(importUtility);
	}

	@Override
	protected void doImport() throws SAFRException, XPathExpressionException {
		SAFRValidationException sve = new SAFRValidationException();
		clearMaps();
		
		parseRecords();

		// Check that the file contains at least 1 Join record
		if (!records.containsKey(LookupPathTransfer.class)) {
			sve.setErrorMessage(getCurrentFile().getName(),
					"There are no <Join> <Record> elements.");
			throw sve;
		}

		// Check for orphaned foreign keys and unreferenced primary keys
		checkReferentialIntegrity();

        // if generated by MR91 generate redefines
        GenerationTransfer genTran = (GenerationTransfer)records.get(GenerationTransfer.class).get(0);
        if (genTran.getProgram().equalsIgnoreCase("MR91")) {
            generateRedefine();
        }
		
		// Check if imported components already exist in DB
		checkDuplicateIds();

		// Check for import IDs > next key IDs (out of range)
		checkOutOfRangeIds();

		if (duplicateIdMap.size() > 0) {
			issueDuplicateIdsWarning();
		}

		checkAssociationsAndSubComponents();
		checkAssociationsWithDifferentId();

		// create and validate model objects.
		uxrs = createUserExitRoutines();
		pfs = createPhysicalFiles();
		lfs = createLogicalFiles();
		lrs = createLogicalRecords();
		lks = createLookupPaths();

        // check LR-LF associations with negative ID's. These come from MR91 generated WE xml 
        // and require us to generate new id's for them
        fixupNegativeIds();        
		
		// store all model objects within a DB transaction
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
				for (LogicalFile lf : lfs) {
					lf.store();
				}
				for (LogicalRecord lr : lrs) {
					lr.store();
				}
				for (LookupPath lookup : lks) {
					lookup.store();
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
	
    protected void fixupNegativeIds() {
        super.fixupNegativeIds();
        
        // fixup lookup references to these Id's
        
        // lookup
        for (LookupPath lp : lks) {
            
            // fix target dest lr-lf assoc
            if (lrlfFixup.containsKey(lp.getTargetLrFileAssociationId())) {
                lp.setTargetLrFileAssociationId(
                    lrlfFixup.get(lp.getTargetLrFileAssociationId()));
            }
            
            // loop steps
            for (LookupPathStep lps : lp.getLookupPathSteps()) {
                
                // fix step target lr-lf assoc
                if (lrlfFixup.containsKey(lps.getTargetLRLFAssociationId())) {
                    lps.setTargetLRLFAssociationId(
                        lrlfFixup.get(lps.getTargetLRLFAssociationId()));
                }
                
                // loop source fields
                for (LookupPathSourceField lpf : lps.getSourceFields()) {                    
                    if (lrlfFixup.containsKey(lpf.getSourceFieldLRLFAssociationId())) {
                        lpf.setSourceFieldLRLFAssociationId(
                            lrlfFixup.get(lpf.getSourceFieldLRLFAssociationId()));
                    }
                }
            }
            
        }
    }
	
	protected List<LookupPath> createLookupPaths() throws SAFRException {
		Map<Integer, SAFRTransfer> map;
		
		// create a map of lookup source field transfers with id as lookup step
		map = records.get(LookupPathSourceFieldTransfer.class);
		Map<Integer, List<LookupPathSourceFieldTransfer>> lkSrcFlds = new HashMap<Integer, List<LookupPathSourceFieldTransfer>>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				LookupPathSourceFieldTransfer trans = (LookupPathSourceFieldTransfer) tfr;
				if (trans.getLookupPathStepId() > 0) {
					Integer stepId = trans.getLookupPathStepId();
					// add src field transfer to map
					if (lkSrcFlds.containsKey(stepId)) {
						lkSrcFlds.get(stepId).add(trans);
					} else {
						List<LookupPathSourceFieldTransfer> fields = new ArrayList<LookupPathSourceFieldTransfer>();
						fields.add(trans);
						lkSrcFlds.put(stepId, fields);
					}
				}
			}
		}
		// create a map of lookup step transfers with id as lookup path
		map = records.get(LookupPathStepTransfer.class);
		Map<Integer, List<LookupPathStepTransfer>> lkSteps = new HashMap<Integer, List<LookupPathStepTransfer>>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				LookupPathStepTransfer trans = (LookupPathStepTransfer) tfr;
				if (trans.getJoinId() > 0) {
					Integer joinId = trans.getJoinId();
					// add src field transfer to map
					if (lkSteps.containsKey(joinId)) {
						lkSteps.get(joinId).add(trans);
					} else {
						List<LookupPathStepTransfer> steps = new ArrayList<LookupPathStepTransfer>();
						steps.add(trans);
						lkSteps.put(joinId, steps);
					}
				}
			}
		}

		Map<Integer, List<LookupPathStep>> existingLKSteps = new HashMap<Integer, List<LookupPathStep>>();
		// find existing steps only if there is at least one Lookup Path in
		// XML;CQ8960
		if (records.get(LookupPathTransfer.class) != null) {
			if (existingComponentTransfers
					.containsKey(LookupPathStepTransfer.class)) {
				// pick up existing steps of all lookups in the XML and
				// arrange in a map.
				for (SAFREnvironmentalComponentTransfer tfr : existingComponentTransfers.get(
						LookupPathStepTransfer.class).values()) {
					// proceed only if not present in the current XML file
					if (!map.containsKey(tfr.getId())) {
						// check if the lookup referred in this trans is present
						// in
						// XML
						LookupPathStepTransfer stepTrans = (LookupPathStepTransfer) tfr;
						if (records.get(LookupPathTransfer.class).containsKey(
								stepTrans.getJoinId())) {
							// get model
							LookupPathStep tmpStep = SAFRApplication
									.getSAFRFactory().initLookupPathStep(
											stepTrans, null, null, null, null);
							// add to map
							tmpStep.markDeleted();
							Integer lkId = stepTrans.getJoinId();
							if (existingLKSteps.containsKey(lkId)) {
								// add to existing list
								existingLKSteps.get(lkId).add(tmpStep);
							} else {
								List<LookupPathStep> tmpSteps = new ArrayList<LookupPathStep>();
								tmpSteps.add(tmpStep);
								existingLKSteps.put(lkId, tmpSteps);
							}
						}
					}
				}
			}
		}

		// create the model object of Lookup path, attach the step and source
		// fields, and validate.
		map = records.get(LookupPathTransfer.class);
		List<LookupPath> lookups = new ArrayList<LookupPath>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				LookupPathTransfer trans = (LookupPathTransfer) tfr;
				LookupPath lookup = SAFRApplication.getSAFRFactory()
						.initLookupPath(trans);
				// add steps to this lookup path
				if (lkSteps.containsKey(lookup.getId())) {
					List<LookupPathStep> steps = new ArrayList<LookupPathStep>();
					for (LookupPathStepTransfer stpTrans : lkSteps.get(lookup
							.getId())) {
						LogicalRecord sourceLr = null;
						LogicalRecord targetLr = null;
						ComponentAssociation targetLrLfAssoc = null;
						for (LogicalRecord lr : lrs) {
							if (sourceLr == null
									&& lr.getId().equals(
											stpTrans.getSourceLRId())) {
								sourceLr = lr;
							}
							if (targetLr == null) {
								ComponentAssociationTransfer cat = (ComponentAssociationTransfer) lrlfAtMap
										.get(stpTrans.getTargetXLRFileId());
								if (cat != null) {
									Integer targetLrId = cat
											.getAssociatingComponentId();

									if (lr.getId().equals(targetLrId)) {
										targetLr = lr;

										Integer targetLrLfAssocId = cat
												.getAssociationId();
										for (ComponentAssociation assoc : lrlfModel
												.get(targetLrId)) {
											if (assoc.getAssociationId()
													.equals(targetLrLfAssocId)) {
												targetLrLfAssoc = assoc;
											}
										}
									}
								}

							}
							if (sourceLr != null && targetLr != null) {
								// exit the for loop
								break;
							}
						}
						LookupPathStep step = SAFRApplication.getSAFRFactory()
								.initLookupPathStep(stpTrans, lookup, sourceLr,
										targetLr, targetLrLfAssoc);
						// add source fields to this step
						if (lkSrcFlds.containsKey(step.getId())) {
							List<LookupPathSourceField> srcFields = new ArrayList<LookupPathSourceField>();
							for (LookupPathSourceFieldTransfer sTrans : lkSrcFlds
									.get(step.getId())) {
								LookupPathSourceField sourceField = SAFRApplication
										.getSAFRFactory()
										.initLookupPathSourceField(sTrans, step);
								srcFields.add(sourceField);
							}
							step.getSourceFields().addAll(srcFields);
						}
						steps.add(step);
					}
					Collections.sort(steps, new Comparator<LookupPathStep>() {
						public int compare(LookupPathStep o1, LookupPathStep o2) {
							return o1.getSequenceNumber().compareTo(
									o2.getSequenceNumber());
						}
					});
					lookup.getLookupPathSteps().addAll(steps);
					// add the steps to be deleted on save
					if (existingLKSteps.containsKey(lookup.getId())) {
						lookup.getLookupPathSteps().addAll(
								existingLKSteps.get(lookup.getId()));
					}
				}

				// validate lookup path
		        Map<Integer, SAFRTransfer> eviews = records.get(ViewTransfer.class);
		        if (eviews != null) {
		            for (Integer exComp : eviews.keySet() ) {
		                lookup.getMigExViewList().add(exComp);
		            }
		        }
				boolean done = false;
				boolean flag = false;
				SAFRValidationToken token = null;
				String messageTitle = "";
				while (!done) {
					try {
						lookup.validate(token);
						done = true;
					} catch (SAFRValidationException sve1) {
						SAFRValidationType failureType = sve1
								.getSafrValidationToken()
								.getValidationFailureType();
						if (failureType == SAFRValidationType.ERROR) {
							// process lookup and step errors. Throw back to the
							// calling function.
							throw sve1;
						} else if (failureType == SAFRValidationType.WARNING) {
							// process lookup step warnings. Ask for user
							// confirmation to continue importing.
							// If the user confirms, validate again to continue
							// from where it was left.
							if (!getConfirmWarningStrategy()
									.confirmWarning(
											"Lookup Path Validation",
											"The Lookup Path \""
													+ lookup.getName()
													+ " ["
													+ lookup.getId()
													+ "]\" will be imported as 'Inactive' due to the following errors.",
											sve1.getMessageString())) {
								SAFRValidationException sve = new SAFRValidationException();
								sve.setErrorMessage(getCurrentFile().getName(),
										"Import stopped after 'Inactivate' warning on Lookup Path \""
												+ lookup.getName() + " ["
												+ lookup.getId() + "]\".");
								throw sve; // stop importing this file
							} else {
								// get token from Exception to pass to validate
								// again.
								token = sve1.getSafrValidationToken();
								lookup.setValid(false);
							}
						} else if (failureType == SAFRValidationType.DEPENDENCY_LOOKUP_WARNING) {
							// CQ10067 If the lookup is part of a view import,
							// don't include the view in the inactivated
							// dependency warning message as it will be
							// inactive anyway after the import. Only show
							// existing dependent views which are NOT part
							// of the import.
							if (getComponentType() == ComponentType.View || getComponentType() == ComponentType.ViewFolder) {
								Integer viewId = records
										.get(ViewTransfer.class).keySet()
										.iterator().next();
								if (sve1.hasDependency(ComponentType.View, viewId)) {
									sve1.removeDependency(ComponentType.View, viewId);
									if (sve1.getDependencies(ComponentType.View).size() == 0) {
										// No other dependencies so no warning required
										break;
									}
									// recreate the warning msg without the imported view
									sve1.createDependencyErrorMessage(Property.VIEW_DEP); 
								}
							}
							
							// process Lookup path dependencies. Ask user for
							// confirmation to continue importing.
							if (flag == false) {
								messageTitle = "The following Views are dependent on Lookup Path \""
										+ lookup.getName()
										+ " ["
										+ lookup.getId()
										+ "]\". If they are Active, importing this Lookup Path will make these Views inactive.";
							} else {
								messageTitle = "A new View dependency has been created on Lookup Path \""
										+ lookup.getName()
										+ " ["
										+ lookup.getId()
										+ "]\" since the last warning was issued. Importing this Lookup Path will make this View(s) inactive too.";
							}
							
							if (!getConfirmWarningStrategy().confirmWarning(
									"Lookup Path dependencies", messageTitle,
									sve1.getMessageString())) {
								SAFRValidationException sve = new SAFRValidationException();
								sve.setErrorMessage(getCurrentFile().getName(),
										"Import stopped after View dependency warning on Lookup Path \""
												+ lookup.getName() + " ["
												+ lookup.getId() + "]\".");
								flag = true;
								throw sve; // stop importing this file
							} else {
								// get token from Exception to pass to validate
								// again.
								token = sve1.getSafrValidationToken();
								flag = true;
							}
						}
					}
				}
                lookup.getMigExViewList().clear();
				lookups.add(lookup);
			}
		}
		return lookups;
	}	

    protected void parseRecords() throws SAFRException,
			XPathExpressionException {
		super.parseRecords();
		joinParser = new LookupRecordParser(this);
		joinParser.parseRecords();
		joinSrcParser = new LookupSourceKeyRecordParser(this);
		joinSrcParser.parseRecords();
		joinTrgParser = new LookupStepRecordParser(this);
		joinTrgParser.parseRecords();
	}
	
	protected void checkReferentialIntegrity() throws SAFRValidationException {
		super.checkReferentialIntegrity();
		joinParser.checkReferentialIntegrity();
		joinTrgParser.checkReferentialIntegrity();
        lrFileParser.checkReferentialIntegrity();       		
	}
	
	protected void checkOutOfRangeIds() throws SAFRException {
		super.checkOutOfRangeIds();
		checkOutOfRangeIds(LookupPathTransfer.class);
		checkOutOfRangeIds(LookupPathStepTransfer.class);
		// no check for lkup source fields as they use the join step ID
	}
	
	/**
	 * @see com.ibm.safr.we.model.utilities.importer.LogicalRecordImporter#checkAssociationsAndSubComponents()
	 */
	protected void checkAssociationsAndSubComponents() throws SAFRException {
		super.checkAssociationsAndSubComponents();
		
		// Check that if lookup path steps already exists in the DB, they relate
		// to the imported lookup path and not some other lookup path.
		joinTrgParser.checkJoinTargets();
	}
	
	/**
	 * If a component association already exists in the target database with a
	 * different association ID use this existing association ID as the foreign
	 * key in the imported component.
	 */
	protected void checkAssociationsWithDifferentId() throws SAFRException {
		// replace association ids to match target database
		joinParser.replaceAssociationIds();
		joinSrcParser.replaceAssociationIds();
		joinTrgParser.replaceAssociationIds();
	}
	// CQ10049 end of refactored code
	
	protected void checkDuplicateTransferIds(
			Class<? extends SAFRTransfer> transferClass,
			List<? extends SAFREnvironmentalComponentTransfer> existingTrans,
			Map<Class<? extends SAFRTransfer>, List<Integer>> duplicateIdMap) {

		if (transferClass != LookupPathSourceFieldTransfer.class) {
			super.checkDuplicateTransferIds(transferClass, existingTrans, duplicateIdMap);
			return;
		}
		
		// Override behavior for join source field's composite key.
		// Composite key is joinstepid + keyseqno
		
		List<int[]> existingLkupSrcs = new ArrayList<int[]>();
		for (SAFREnvironmentalComponentTransfer trans : existingTrans) {
			LookupPathSourceFieldTransfer lkupSrcTfr = (LookupPathSourceFieldTransfer) trans;
			int[] compositeKey = new int[2];
			int i = lkupSrcTfr.getLookupPathStepId();
			compositeKey[0] = i;
			compositeKey[1] = lkupSrcTfr.getKeySeqNbr();
			existingLkupSrcs.add(compositeKey);
		}
		
		List<Integer> duplicateIds = new ArrayList<Integer>();
		if (records.containsKey(LookupPathSourceFieldTransfer.class)) {
			Map<Integer, SAFRTransfer> importedLkupSrcMap = records.get(LookupPathSourceFieldTransfer.class);
			for (Integer id : importedLkupSrcMap.keySet()) {
				LookupPathSourceFieldTransfer importedLkupSrc = (LookupPathSourceFieldTransfer)importedLkupSrcMap.get(id);
				for (int[] existingKey : existingLkupSrcs) {
					if (existingKey[0] == importedLkupSrc.getLookupPathStepId() &&
							existingKey[1] == importedLkupSrc.getKeySeqNbr()) {
						duplicateIds.add(id); //note, id is stepId*100+keyseqnbr
						
						// set the persistent boolean in transfer to true so that
						// DATA layer can update it
						importedLkupSrc.setPersistent(true);
					}
				}
			}
		}
		if (duplicateIds.size() > 0) {
			duplicateIdMap.put(transferClass, duplicateIds);
		}
	}

	protected String getId(Class<? extends SAFRTransfer> tfrClass, Integer id) {
		
		if (tfrClass != LookupPathSourceFieldTransfer.class) {
			return super.getId(tfrClass, id);
		}
		
		// Overriding here to handle joinsource composite key.
		// return key in format joinstepid.keyseqno
		Map<Integer, SAFRTransfer> map = records.get(LookupPathSourceFieldTransfer.class);
		LookupPathSourceFieldTransfer lkupSrcTfr = (LookupPathSourceFieldTransfer) map.get(id);
		String compositeId = lkupSrcTfr.getLookupPathStepId() + "." + lkupSrcTfr.getKeySeqNbr();
		
		return compositeId;
	}
	
}
