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
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.LookupPathTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LogicalRecord.Property;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRValidationToken;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordFieldQueryBean;

public class LogicalRecordImporter extends LogicalFileImporter implements
		LRInformationProvider {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.utilities.importer.LogicalRecordImporter");
    
	// CQ10049 refactored variables
	protected LRRecordParser lrParser;
	protected LRFieldRecordParser lrFieldParser;
	protected LRFieldAttributeRecordParser lrFieldAttrParser;
	protected LRIndexRecordParser lrIndexParser;
	protected LRIndexFieldRecordParser lrIndexFieldParser;
	protected LRLFAssocRecordParser lrFileParser;
	protected Map<Integer, LRIndexTransfer> lrIndexTrans = new HashMap<Integer, LRIndexTransfer>();
	protected Map<Integer, List<LRIndexFieldTransfer>> lrIndexFieldTrans = new HashMap<Integer, List<LRIndexFieldTransfer>>();
	protected Map<Integer, List<ComponentAssociation>> lrlfModel;
	protected List<LogicalRecord> lrs;
	protected Map<Integer, SAFRTransfer> lrlfAtMap;
	protected ModelTransferProvider provider;
	protected Map<Integer,Integer> lrlfFixup = new HashMap<Integer,Integer>();

	public LogicalRecordImporter(ImportUtility importUtility) {
		super(importUtility);
	}

	public void generateOrdPos() {
        int ordPos = 1;
        int lrId = 0;	    
        List<LRFieldTransfer> flds = new ArrayList<LRFieldTransfer>();
        for (SAFRTransfer trans : records.get(LRFieldTransfer.class).values()) {
            flds.add((LRFieldTransfer) trans);
        }
        Collections.sort(flds, new Comparator<LRFieldTransfer>() {
            public int compare(LRFieldTransfer o1, LRFieldTransfer o2) {
                int c = o1.getLrId().compareTo(o2.getLrId());
                if (c==0) {
                    c = o1.getFixedStartPos().compareTo(o2.getFixedStartPos());
                    if (c==0) {
                        c = o2.getLength().compareTo(o1.getLength());
                        if (c==0) {
                            c = new Boolean(o1.isRedefinesInd()).compareTo(new Boolean(o2.isRedefinesInd()));
                            if (c==0) {
                                c =o1.getName().compareTo(o2.getName());
                            }
                        }
                    }
                }
                return c;
            }                
        });
        // now sorted properly so set ordinal position for all fields
        ordPos = 1; 
        lrId = 0;
        for (LRFieldTransfer trans : flds) {
            if (lrId == 0 || lrId != trans.getLrId().intValue()) {
                lrId = trans.getLrId().intValue();
                ordPos = 1;
            }
            trans.setOrdinalPos(ordPos++);
        }	    
	}

    public void generateRedefine() {
        List<LRFieldTransfer> flds = new ArrayList<LRFieldTransfer>();
        for (SAFRTransfer trans : records.get(LRFieldTransfer.class).values()) {
            flds.add((LRFieldTransfer) trans);
        }
        Collections.sort(flds, new Comparator<LRFieldTransfer>() {
            public int compare(LRFieldTransfer o1, LRFieldTransfer o2) {
                int c = o1.getLrId().compareTo(o2.getLrId());
                if (c==0) {
                    c = o1.getOrdinalPos().compareTo(o2.getOrdinalPos());
                }
                return c;                
            }
        });
        
        
        // loop through every LR
        int curLR=0;
        List<LRFieldTransfer> lrFlds = new ArrayList<LRFieldTransfer>();
        for (SAFRTransfer trans : flds) {
            LRFieldTransfer fld = (LRFieldTransfer)trans;
            if (curLR != fld.getLrId()) {
                if (lrFlds.size() > 0) {
                    generateRedefineLR(lrFlds);
                }
                lrFlds.clear();
            }
            curLR = fld.getLrId();
            lrFlds.add(fld);
        }  
        
        // do last LR
        if (lrFlds.size() > 0) {
            generateRedefineLR(lrFlds);
        }        
    }
        
    private List<LRFieldTransfer> getAncestors(List<LRFieldTransfer> allFlds, LRFieldTransfer fld) {
        List<LRFieldTransfer> ancs = new ArrayList<LRFieldTransfer>();
        ancs.add(fld);
        Integer redefine = fld.getRedefine();
        while (redefine != null && !redefine.equals(0)) {
            // find parent
            for (LRFieldTransfer cfld : allFlds) {
                if (cfld.getId().equals(redefine)) {                    
                    ancs.add(cfld);
                    redefine = cfld.getRedefine();
                    break;
                }
            }
            
        }
        return ancs;
    }
    
    private void generateRedefineLR(List<LRFieldTransfer> lrFlds) {
        LRFieldTransfer prevTrans = null;
        for (SAFRTransfer otrans : lrFlds) {
            LRFieldTransfer child = (LRFieldTransfer)otrans;
            
            if (prevTrans != null) {
                List<LRFieldTransfer> ancs = getAncestors(lrFlds, prevTrans);
                    
                int chdEnd = child.getFixedStartPos()+child.getLength();
                // for each field find the smallest parent that contains it
                    
                LRFieldTransfer parent = null;
                for (LRFieldTransfer chkPar : ancs) {
                    int parEnd = chkPar.getFixedStartPos()+chkPar.getLength();
                    if (chkPar.getFixedStartPos() <= child.getFixedStartPos() &&
                        parEnd >= chdEnd) {
                        if (parent == null || parent.getLength() > chkPar.getLength()) {
                            parent = chkPar;
                        }
                    }
                }
                // if parent exists set the redefines
                if (parent != null) {
                    child.setRedefine(parent.getId());
                }
            }
            prevTrans = child;
        }        
    }
    	
	@Override
	protected void doImport() throws SAFRException, XPathExpressionException {

		SAFRValidationException sve = new SAFRValidationException();
		clearMaps();

		parseRecords();

		// Check that the file contains at least one LR record
		if (!records.containsKey(LogicalRecordTransfer.class)) {
			sve.setErrorMessage(getCurrentFile().getName(),
					"There are no <LR> <Record> elements.");
			throw sve;
		}

		// Check for orphaned foreign keys and unreferenced primary keys
		checkReferentialIntegrity();

        // if generated by MR91 generate redefines
        GenerationTransfer genTran = (GenerationTransfer)records.get(GenerationTransfer.class).get(0);
        if (genTran.getProgram().equalsIgnoreCase("MR91")) {
            generateRedefine();
        }
        
		checkDuplicateIds();

		// Check for import IDs > next key IDs (out of range)
		checkOutOfRangeIds();

		if (duplicateIdMap.size() > 0) {
			issueDuplicateIdsWarning();
		}

		checkAssociationsAndSubComponents();

		// create and validate model objects.
		uxrs = createUserExitRoutines();
		pfs = createPhysicalFiles();		
		lfs = createLogicalFiles();
		lrs = createLogicalRecords();

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
					if (uxr.getPersistence() != SAFRPersistence.OLD) {
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
				for (LogicalRecord lr : lrs) {
					if(lr.getPersistence() != SAFRPersistence.OLD ) {
						lr.store();
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
	
	protected void fixupNegativeIds() {
	    // check all lrs assocs
	    Integer nextId = nextKeyIdMap.get(ComponentAssociationTransfer.class);
	    for (List<ComponentAssociation> assocList : lrlfModel.values()) {
	        for (ComponentAssociation assoc : assocList) {
	            if (assoc.getAssociationId() < 0) {
	                // preallocate a new id
	                lrlfFixup.put(assoc.getAssociationId(), nextId);
	                assoc.setAssociationId(nextId++);
	            }
	        }
	    }
	    
	    // set new next id
	    DAOFactoryHolder.getDAOFactory().getNextKeyDAO().setNextKeyId(ComponentAssociationTransfer.class, nextId);
    }

    private Map<Integer, List<ComponentAssociation>> createLRLFAssociations()
			throws SAFRException {
		if (records.containsKey(ComponentAssociationTransfer.class)) {
			lrlfAtMap = records.get(ComponentAssociationTransfer.class);
		} else {
			lrlfAtMap = new HashMap<Integer, SAFRTransfer>(); // empty map
		}

		Map<Integer, List<ComponentAssociation>> lrlfModel = new HashMap<Integer, List<ComponentAssociation>>();
		for (SAFRTransfer tfr : lrlfAtMap.values()) {
			ComponentAssociationTransfer compAssocTrans = (ComponentAssociationTransfer) tfr;
			if (compAssocTrans.getAssociatingComponentId() > 0) {
				Integer parentCompId = compAssocTrans
						.getAssociatingComponentId();

				// If an imported assoc already exists in target, use the
				// existing assoc instead.
				ComponentAssociation compAssoc = (ComponentAssociation) checkForExistingAssoc(
						ComponentAssociation.class,
						compAssocTrans.getAssociatingComponentId(),
						compAssocTrans.getAssociatedComponentId());
				
				if (compAssoc == null) {
					// Use imported association
					compAssoc = new ComponentAssociation(compAssocTrans);
				}
				
				// add association object to model map
				if (lrlfModel.containsKey(parentCompId)) {
					lrlfModel.get(parentCompId).add(compAssoc);
				} else {
					List<ComponentAssociation> assoc = new ArrayList<ComponentAssociation>();
					assoc.add(compAssoc);
					lrlfModel.put(parentCompId, assoc);
				}
			}
		}
		return lrlfModel;
	}
	
	/*
	 * For all imported LRs find any existing LF associations which are not
	 * present in the imported XML so they can be deleted from the target
	 * environment.
	 */
	private Map<Integer, List<ComponentAssociation>> findAssocsToBeDeleted()
	{
		Map<Integer, SAFRTransfer> lrlfAtMap = null;
		if (records.containsKey(ComponentAssociationTransfer.class)) {
			lrlfAtMap = records.get(ComponentAssociationTransfer.class);
		} else {
			lrlfAtMap = new HashMap<Integer, SAFRTransfer>();// empty map
		}
		
		Map<Integer, List<ComponentAssociation>> assocsMap = new HashMap<Integer, List<ComponentAssociation>>();
		if (!lrlfAtMap.isEmpty()) {
			if (existingAssociations.containsKey(ComponentAssociation.class)) {
				// check for LF assocs to be deleted
				for (ComponentAssociation trgAssoc : existingAssociations.get(ComponentAssociation.class).values()) {
					// proceed if an existing assoc is not being imported
					if (!isAssocImported(trgAssoc, lrlfAtMap)) {
						// proceed if parent LR is being imported
						if (records.get(LogicalRecordTransfer.class).containsKey(trgAssoc.getAssociatingComponentId())) {
							// assoc must be deleted
							if (assocsMap.containsKey(trgAssoc.getAssociatingComponentId())) {
								assocsMap.get(trgAssoc.getAssociatingComponentId()).add(trgAssoc);
							} else {
								List<ComponentAssociation> assocs = new ArrayList<ComponentAssociation>();
								assocs.add(trgAssoc);
								assocsMap.put(trgAssoc.getAssociatingComponentId(), assocs);
							}
						}
					}

				}
			}
		}
		return assocsMap; // assocs to be deleted
	}
	
	private Map<Integer, List<LRField>> createLRFields()
			throws SAFRException {
		
		// create LR field model objects and store it in a map with LR id as key
		Map<Integer, SAFRTransfer> map = records.get(LRFieldTransfer.class);
		Map<Integer, List<LRField>> lrFieldModel = new HashMap<Integer, List<LRField>>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				LRFieldTransfer trans = (LRFieldTransfer) tfr;
				if (trans.getLrId() > 0) {
					Integer lrId = trans.getLrId();
					// add LR field object to model map
					LRField field = SAFRApplication.getSAFRFactory()
							.initLRField(trans);
					if (lrFieldModel.containsKey(lrId)) {
						lrFieldModel.get(lrId).add(field);
					} else {
						List<LRField> fields = new ArrayList<LRField>();
						fields.add(field);
						lrFieldModel.put(lrId, fields);
					}
				}
			}
		}
		
		// sort by ordinal position
		for (List<LRField> fields : lrFieldModel.values()) {
		    Collections.sort(fields, new Comparator<LRField>() {

                public int compare(LRField o1, LRField o2) {
                    return o1.getOrdinalPosition() > o2.getOrdinalPosition() ? 1 : 
                        o1.getOrdinalPosition() < o2.getOrdinalPosition() ? -1 : 0;
                }
		        
		    });
		}
		return lrFieldModel;
	}

	private Map<Integer, List<LRField>> findExistingLRFields()
			throws SAFRException {
		List<LRField> lrFields = new ArrayList<LRField>();
		Map<Integer, List<LRField>> existingLRFields = new HashMap<Integer, List<LRField>>();
		Map<Integer, SAFRTransfer> map = records.get(LRFieldTransfer.class);
		if (map != null) {
			if (existingComponents
					.containsKey(LogicalRecordFieldQueryBean.class)) {
				List<Integer> lrFieldIds = new ArrayList<Integer>();

				// pick up existing LR fields of all LRs in the XML and
				// arrange in a map.
				for (EnvironmentalQueryBean qb : existingComponents.get(
						LogicalRecordFieldQueryBean.class).values()) {
					// proceed only if not present in the current XML file
					if (!map.containsKey(qb.getId())) {
						// check if the LR referred in this trans is present in
						// XML
						LogicalRecordFieldQueryBean fieldBean = (LogicalRecordFieldQueryBean) qb;
						if (records.get(LogicalRecordTransfer.class)
								.containsKey(fieldBean.getLrId())) {
							lrFieldIds.add(fieldBean.getId());
						}
					}
				}
				lrFields = SAFRApplication.getSAFRFactory().getLRFields(
						getTargetEnvironmentId(), lrFieldIds);
				// add to map
				for (LRField field : lrFields) {
					field.markDeleted();
					Integer lrId = field.getLogicalRecord().getId();
					if (existingLRFields.containsKey(lrId)) {
						// add to existing list
						existingLRFields.get(lrId).add(field);
					} else {
						List<LRField> tmpFields = new ArrayList<LRField>();
						tmpFields.add(field);
						existingLRFields.put(lrId, tmpFields);
					}
				}
			}
		}
		return existingLRFields;
	}
	
	protected List<LogicalRecord> createLogicalRecords() throws SAFRException {
		
		Map<Integer, List<LRField>> lrFieldModel = createLRFields();
		
		Map<Integer, List<LRField>> existingLRFields = findExistingLRFields();

		lrlfModel = createLRLFAssociations();
		
		Map<Integer, List<ComponentAssociation>> assocsToBeDeleted = findAssocsToBeDeleted();
		
		// arrange LR index by LR id
		Map<Integer, SAFRTransfer> map = records.get(LRIndexTransfer.class);

		lrIndexTrans.clear();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				LRIndexTransfer trans = (LRIndexTransfer) tfr;
				if (trans.getLrId() > 0) {
					lrIndexTrans.put(trans.getLrId(), trans);
				}
			}
		}

		// arrange LR index fields by LR index id in a map
		lrIndexFieldTrans.clear();
		map = records.get(LRIndexFieldTransfer.class);
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				LRIndexFieldTransfer trans = (LRIndexFieldTransfer) tfr;
				if (trans.getAssociatingComponentId() > 0) {
					Integer indexId = trans.getAssociatingComponentId();
					if (lrIndexFieldTrans.containsKey(indexId)) {
						lrIndexFieldTrans.get(indexId).add(trans);
					} else {
						List<LRIndexFieldTransfer> fields = new ArrayList<LRIndexFieldTransfer>();
						fields.add(trans);
						lrIndexFieldTrans.put(indexId, fields);
					}
				}
			}
		}

		// create LR objects and attach the Fields, LF and UXR to it, validate
		// all.
		map = records.get(LogicalRecordTransfer.class);
		List<LogicalRecord> lrs = new ArrayList<LogicalRecord>();
		provider = new ModelTransferProvider(this);
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				LogicalRecordTransfer lrTrans = (LogicalRecordTransfer) tfr;
				LogicalRecord lr = SAFRApplication.getSAFRFactory()
						.initLogicalRecord(lrTrans);
				lr.setModelTransferProvider(provider);
				// get the associated LFs and add to LR model
				if (lrlfModel.containsKey(lr.getId())) {
					for (ComponentAssociation assoc : lrlfModel.get(lr.getId())) {
						lr.addAssociatedLogicalFile(assoc);
					}
				}

				if (lrTrans.getLookupExitId() != null && lrTrans.getLookupExitId() > 0) {
					// get the UXR and add to LR model
					UserExitRoutineTransfer uxrTrans = (UserExitRoutineTransfer) records
							.get(UserExitRoutineTransfer.class).get(
									lrTrans.getLookupExitId());
					UserExitRoutine uxr = SAFRApplication.getSAFRFactory()
							.initUserExitRoutine(uxrTrans);
					uxr.validate();
					uxrs.put(uxr.getId(), uxr);
					lr.setLookupExitRoutine(uxr);
				}

				// add fields
				if (lrFieldModel.get(lr.getId()) != null) {
					lr.getLRFields().addAll(lrFieldModel.get(lr.getId()));
					// set lrfield lr
					for (LRField fld : lr.getLRFields()) {
					    fld.setLogicalRecord(lr);
					}
				}

				// do an add-remove for fields to be deleted
				if (existingLRFields.containsKey(lr.getId())) {
					lr.getLRFields().addAll(existingLRFields.get(lr.getId()));
					try {
						lr.removeFields(existingLRFields.get(lr.getId()));
					} catch (SAFRValidationException ve) {
						SAFRValidationType failureType = ve
								.getSafrValidationToken()
								.getValidationFailureType();
						if (failureType == SAFRValidationType.DEPENDENCY_LR_FIELDS_ERROR) {
							// Set the context message then re-throw
							String msg = "When Logical Record '"
									+ lr.getDescriptor()
									+ "' is replaced on import, the following LR Fields "
									+ "should be deleted as they are not included in the import data, "
									+ "but they can't be deleted as they are referenced by existing "
									+ "Lookup Paths or Views.";
							msg = splitMessage(msg, 90);
							msg = "LR Field dependency error." + SAFRUtilities.LINEBREAK + msg;
							ve.setErrorMessage(
									LogicalRecord.Property.VIEW_LOOKUP_DEP_IMPORT,
									msg);
						}
						throw ve;
					}
				}

				// Delete existing LRLF associations not present in the XML
				if (assocsToBeDeleted.containsKey(lr.getId())) {
					for (ComponentAssociation assoc : assocsToBeDeleted.get(lr.getId())) {
						// add assoc to imported LR so it can be deleted
						lr.addAssociatedLogicalFile(assoc);
					}
					try {
						lr.removeAssociatedLogicalFiles(assocsToBeDeleted
								.get(lr.getId()));
					} catch (SAFRValidationException ve) {
						SAFRValidationType failureType = ve
								.getSafrValidationToken()
								.getValidationFailureType();
						if (failureType == SAFRValidationType.DEPENDENCY_LF_ASSOCIATION_ERROR) {
							// Set the context message then re-throw
							String msg = "When Logical Record '"
									+ lr.getDescriptor()
									+ "' is replaced on import, the following Logical File associations "
									+ "should be removed as they are not included in the import data, "
									+ "but they can't be removed as they are referenced by existing "
									+ "Lookup Paths or Views.";
							msg = splitMessage(msg, 90);
							msg = "Logical Record/Logical File dependency error." + SAFRUtilities.LINEBREAK + msg;
							ve.setErrorMessage(
									LogicalRecord.Property.LF_ASSOCIATION_DEP_IMPORT,
									msg);
						}
						throw ve;
					}
				}

				// check for validation warnings
                Map<Integer, SAFRTransfer> eviews = records.get(ViewTransfer.class);
                if (eviews != null) {
                    for (Integer exComp : eviews.keySet() ) {
                        lr.getMigExViewList().add(exComp);
                    }
                }
                Map<Integer, SAFRTransfer> elookups = records.get(LookupPathTransfer.class);
                if (elookups != null) {
                    for (Integer exComp : elookups.keySet() ) {
                        lr.getMigExLookupList().add(exComp);
                    }
                }
				boolean done = false;
				boolean flag = false;
				SAFRValidationToken token = null;
				String messageTitle = "";
				if (lr.isPersistent()) {
					lr.setCheckLookupDependencies(true);
					lr.setCheckViewDependencies(true);
				}
				while (!done) {
					try {
					   lr.validate(token);
					   done = true;
					} catch (SAFRValidationException sve1) {
						SAFRValidationType failureType = sve1
								.getSafrValidationToken()
								.getValidationFailureType();
						if (failureType == SAFRValidationType.ERROR) {
							throw sve1;
						} else if (failureType == SAFRValidationType.DEPENDENCY_LR_FIELDS_ERROR) {
							// Set the context message then re-throw
							String msg = "When Logical Record '"
									+ lr.getDescriptor()
									+ "' is replaced on import, the following LR Fields "
									+ "should be deleted as they are not included in the import data, "
									+ "but they can't be deleted as they are referenced by existing "
									+ "Lookup Paths or Views.";
							msg = splitMessage(msg, 90);
							msg = "LR Field dependency error." + SAFRUtilities.LINEBREAK + msg;
							sve1.setErrorMessage(
									LogicalRecord.Property.VIEW_LOOKUP_DEP_IMPORT,
									msg);
							throw sve1;
						} else if (failureType == SAFRValidationType.DEPENDENCY_LF_ASSOCIATION_ERROR) {
							// Set the context message then re-throw
							String msg = "When Logical Record '"
									+ lr.getDescriptor()
									+ "' is replaced on import, the following LF associations "
									+ "should be deleted as they are not included in the import data, "
									+ "but they can't be deleted as they are referenced by existing "
									+ "Lookup Paths or Views.";
							msg = splitMessage(msg, 90);
							msg = "Logical Record/Logical File dependency error." + SAFRUtilities.LINEBREAK + msg;
							sve1.setErrorMessage(
									LogicalRecord.Property.LF_ASSOCIATION_DEP_IMPORT,
									msg);
							throw sve1;
						} else if (failureType == SAFRValidationType.WARNING) {
							// process LR and LR field warnings. Ask for user
							// confirmation to continue importing.
							// If the user confirms, validate again to continue
							// from where it was left.
							if (!getConfirmWarningStrategy()
									.confirmWarning(
											"Logical Record validation",
											"The Logical Record '"
													+ lr.getDescriptor()
													+ "' will be imported as Inactive due to the following errors.",
											sve1.getMessageString())) {
								SAFRValidationException sve = new SAFRValidationException();
								sve.setErrorMessage(
										getCurrentFile().getName(),
										"Import cancelled on warning about inactivation of Logical Record '"
												+ lr.getDescriptor()
												+ "' due to validation errors.");
								throw sve; // stop importing this file
							} else {
								// get token from Exception to pass to validate
								// again.
								token = sve1.getSafrValidationToken();
								lr.setActive(false);
							}
						} else if (failureType == SAFRValidationType.DEPENDENCY_LR_WARNING) {
							// CQ10067 The warning msg about dependent Lookups
							// and Views which will be inactivated by importing
							// this LR should only specify existing Lookups and
							// Views which are NOT part of the import scope.
							// An imported View will always be made inactive
							// and an imported Lookup will not be inactivated
							// unless there is a problem with it, in which 
							// case a separate warning msg will be issued.
							boolean dependencyChange = false;
							if (getComponentType() == ComponentType.View || getComponentType() == ComponentType.ViewFolder) {
								// remove the imported view from the dependency msg
								Integer viewId = records
										.get(ViewTransfer.class).keySet()
										.iterator().next();
								if (sve1.hasDependency(ComponentType.View, viewId)) {
									sve1.removeDependency(ComponentType.View, viewId);
									dependencyChange = true;
								}
							}
							Map<Integer, SAFRTransfer> lookups = records.get(LookupPathTransfer.class);
							if (lookups != null) {
								// remove the imported lookups from the dependency msg
								for (Integer lookupId : lookups.keySet()) {
									if (sve1.hasDependency(ComponentType.LookupPath, lookupId)) {
										sve1.removeDependency(ComponentType.LookupPath, lookupId);
										dependencyChange = true;
									}
								}
							}
							if (dependencyChange) {
								if ((sve1.getDependencies(ComponentType.View) == null || sve1
										.getDependencies(ComponentType.View)
										.size() == 0)
										&& (sve1.getDependencies(ComponentType.LookupPath) == null || sve1
												.getDependencies(
														ComponentType.LookupPath)
												.size() == 0)) {
									// No dependencies remain so no warning msg
									break;
								} else {
									// recreate the warning msg without the
									// imported view and lookups
									sve1.createDependencyErrorMessage(Property.VIEW_LOOKUP_DEP);
								}
							}
							
							// process LR dependencies. Ask user for
							// confirmation to continue importing.
							if (flag == false) {
								messageTitle = "The following Lookup Paths and/or Views are dependent on Logical Record '"
										+ lr.getDescriptor()
										+ "'. If they are Active, importing this Logical Record will make these components inactive.";
							} else {
								messageTitle = "A new Lookup Path or View dependency has been created on Logical Record '"
										+ lr.getDescriptor()
										+ "' since the last warning was issued. Importing this Logical Record will make this component(s) inactive too.";
							}
							if (!getConfirmWarningStrategy().confirmWarning(
									"Logical Record dependencies",
									messageTitle, sve1.getMessageString())) {
								SAFRValidationException sve = new SAFRValidationException();
								sve.setErrorMessage(
										getCurrentFile().getName(),
										"Import cancelled on warning about new Lookup Path or View dependencies on Logical Record '"
												+ lr.getDescriptor()
												+ "'.");
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
                lr.getMigExViewList().clear();
                lr.getMigExLookupList().clear();
				lrs.add(lr);
			}
		}
		return lrs;
	}
	
	protected void parseRecords() throws SAFRException,
			XPathExpressionException {
		super.parseRecords();
		lrParser = new LRRecordParser(this);
		lrParser.parseRecords();
		lrFieldParser = new LRFieldRecordParser(this);
		lrFieldParser.parseRecords();
		lrFieldAttrParser = new LRFieldAttributeRecordParser(this);
		lrFieldAttrParser.parseRecords();
		lrIndexParser = new LRIndexRecordParser(this);
		lrIndexParser.parseRecords();
		lrIndexFieldParser = new LRIndexFieldRecordParser(this);
		lrIndexFieldParser.parseRecords();
		lrFileParser = new LRLFAssocRecordParser(this);
		lrFileParser.parseRecords();
	}
	
	protected void checkReferentialIntegrity() throws SAFRValidationException {
		super.checkReferentialIntegrity();
		lrParser.checkReferentialIntegrity();
		lrFieldParser.checkReferentialIntegrity();
		lrIndexParser.checkReferentialIntegrity();
	}
	
	protected void checkOutOfRangeIds() throws SAFRException {
		super.checkOutOfRangeIds();
		checkOutOfRangeIds(LogicalRecordTransfer.class);
		checkOutOfRangeIds(LRFieldTransfer.class);
		checkOutOfRangeIds(LRIndexTransfer.class);
		checkOutOfRangeIds(LRIndexFieldTransfer.class);
		checkOutOfRangeIds(ComponentAssociationTransfer.class);
	}
	
	/**
	 * @see com.ibm.safr.we.model.utilities.importer.LogicalFileImporter#checkAssociationsAndSubComponents()
	 */
	protected void checkAssociationsAndSubComponents() throws SAFRException {
		super.checkAssociationsAndSubComponents();
		
		// Check that if any LR subcomponents already exist in the DB,
		// they relate to the imported LR not some other LR.
		lrFieldParser.checkLRFields();
		lrIndexParser.checkLRIndexes();
		lrFileParser.checkLRLFAssociations();
	}
	
	public LRIndexTransfer getLRIndex(Integer lrId) {
		return lrIndexTrans.get(lrId);
	}

	public List<LRIndexFieldTransfer> getLRIndexFields(Integer lrIndexId) {
		return lrIndexFieldTrans.get(lrIndexId);
	}
	
}
	
