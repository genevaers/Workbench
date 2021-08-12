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

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.LookupPathStepTransfer;
import com.ibm.safr.we.data.transfer.LookupPathTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LogicalRecord.Property;
import com.ibm.safr.we.model.associations.ComponentAssociation;

/**
 * This class will parse a &LT;LR-File&GT; &LT;Record&GT; element into a
 * FileAssociationTransfer object.
 */
public class LRLFAssocRecordParser extends RecordParser {

	public LRLFAssocRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//LR-LF-Association/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;
		ComponentAssociationTransfer trans = new ComponentAssociationTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("LRLFASSOCID", record);
		trans.setAssociationId(fieldToInteger("LRLFASSOCID", fieldValue));

		fieldValue = parseField("LOGFILEID", record);
		trans.setAssociatedComponentId(fieldToInteger("LOGFILEID", fieldValue));

		fieldValue = parseField("LOGRECID", record);
		trans.setAssociatingComponentId(fieldToInteger("LOGRECID", fieldValue));

		fieldValue = parseField("CREATEDTIMESTAMP", record);
		trans.setCreateTime(fieldToDate("CREATEDTIMESTAMP", fieldValue));

		fieldValue = parseField("CREATEDUSERID", record);
		trans.setCreateBy(fieldValue);

		fieldValue = parseField("LASTMODTIMESTAMP", record);
		trans.setModifyTime(fieldToDate("LASTMODTIMESTAMP", fieldValue));

		fieldValue = parseField("LASTMODUSERID", record);
		trans.setModifyBy(fieldValue);

		return trans;
	}

	@Override
	public void checkReferentialIntegrity() throws SAFRValidationException {
		SAFRValidationException sve = new SAFRValidationException();
		List<Integer> LRFileFKeys = new ArrayList<Integer>();
		Map<Integer, SAFRTransfer> lrlfAssocTMap = null;
		if (importer.records.containsKey(ComponentAssociationTransfer.class)) {
			lrlfAssocTMap = importer.records
					.get(ComponentAssociationTransfer.class);
		} else {
			lrlfAssocTMap = new HashMap<Integer, SAFRTransfer>();// empty
			// map
		}

		switch (importer.getComponentType()) {
        case ViewFolder:
		case View:
			// XVIEWSRCLRFILEID for View sources.
			Map<Integer, SAFRTransfer> viewSourceTMap = null;
			if (importer.records.containsKey(ViewSourceTransfer.class)) {
				viewSourceTMap = importer.records.get(ViewSourceTransfer.class);
			} else {
				viewSourceTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			for (SAFRTransfer tfr : viewSourceTMap.values()) {
				ViewSourceTransfer viewSrcTrans = (ViewSourceTransfer) tfr;
				Integer lrlfAssocId = viewSrcTrans.getLRFileAssocId();
				if (lrlfAssocId > 0) {
					if (lrlfAssocTMap.get(lrlfAssocId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Source["
												+ viewSrcTrans.getId()
												+ "] refers to LR-LF association with id ["
												+ lrlfAssocId
												+ "] but this is not in the import file.");
						throw sve;
					}
					LRFileFKeys.add(lrlfAssocId);
				}

			}

		case LookupPath:
			Map<Integer, SAFRTransfer> joinTMap = null;
			if (importer.records.containsKey(LookupPathTransfer.class)) {
				joinTMap = importer.records.get(LookupPathTransfer.class);
			} else {
				joinTMap = new HashMap<Integer, SAFRTransfer>();// empty map
			}
			// DESTXLRFILEID for join
			for (SAFRTransfer tfr : joinTMap.values()) {
				LookupPathTransfer lookupTrans = (LookupPathTransfer) tfr;
				Integer targetLRLFAssocId = lookupTrans.getTargetXLRFileId();
				if (targetLRLFAssocId > 0) {
					if (lrlfAssocTMap.get(targetLRLFAssocId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"Lookup Path ["
												+ lookupTrans.getId()
												+ "] refers to LR-LF association with id ["
												+ targetLRLFAssocId
												+ "] but this is not in the import file.");
						throw sve;
					}
					LRFileFKeys.add(targetLRLFAssocId);
				}

			}

			// DESTXLRFILEID for Join target
			Map<Integer, SAFRTransfer> joinTargetTMap = null;
			if (importer.records.containsKey(LookupPathStepTransfer.class)) {
				joinTargetTMap = importer.records
						.get(LookupPathStepTransfer.class);
			} else {
				joinTargetTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			for (SAFRTransfer tfr : joinTargetTMap.values()) {
				LookupPathStepTransfer lookupStepTrans = (LookupPathStepTransfer) tfr;
				Integer targetLRLFAssocId = lookupStepTrans
						.getTargetXLRFileId();
				if (targetLRLFAssocId > 0) {
					if (lrlfAssocTMap.get(targetLRLFAssocId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"Lookup Path Step ["
												+ lookupStepTrans.getId()
												+ "] refers to LR-LF association ["
												+ targetLRLFAssocId
												+ "] as its target but this is not in the import file.");
						throw sve;
					}
					LRFileFKeys.add(targetLRLFAssocId);
				}
			}

			// SRCXLRFILEID for Join Source
			Map<Integer, SAFRTransfer> joinSourceTMap = null;
			if (importer.records
					.containsKey(LookupPathSourceFieldTransfer.class)) {
				joinSourceTMap = importer.records
						.get(LookupPathSourceFieldTransfer.class);
			} else {
				joinSourceTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			for (SAFRTransfer tfr : joinSourceTMap.values()) {
				LookupPathSourceFieldTransfer lookupSrcTrans = (LookupPathSourceFieldTransfer) tfr;
				Integer srcLRLFAssocId = lookupSrcTrans.getSourceXLRFileId();
				if (srcLRLFAssocId != null && srcLRLFAssocId > 0) {
					if (lrlfAssocTMap.get(srcLRLFAssocId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"Lookup Path Source ["
												+ lookupSrcTrans.getId()
												+ "] refers to LR-LF association with id ["
												+ srcLRLFAssocId
												+ "] but this is not in the import file.");
						throw sve;
					}
					LRFileFKeys.add(srcLRLFAssocId);
				}
			}

		default:// no action for other component types
		} // end switch

		// reverse check is not required as there may be some orphan LR File
		// associations
		// which are not referred by any component.
	}

	public void checkLRLFAssociations() throws SAFRException {

	    // Setup association map to check
        Map<Integer, SAFRTransfer> catMap = null;
        if (importer.records.containsKey(ComponentAssociationTransfer.class)) {
            catMap = importer.records.get(ComponentAssociationTransfer.class);
        } else {
            catMap = new HashMap<Integer, SAFRTransfer>(); // empty map
        }
        	    
        // Check for duplicate association ids
        for (SAFRTransfer trans : catMap.values()) {

            ComponentAssociationTransfer importedLRLF = (ComponentAssociationTransfer) trans;

            if (importer.duplicateIdMap.containsKey(ComponentAssociationTransfer.class) && 
                importer.duplicateIdMap.get(ComponentAssociationTransfer.class).contains(importedLRLF.getAssociationId())) {
                // This assoc already exists in DB.
                // Check that the LRID fkeys match.
                ComponentAssociation existingLRLF = (ComponentAssociation) importer.existingAssociations.
                    get(ComponentAssociation.class).get(importedLRLF.getAssociationId());
                boolean sameLR = existingLRLF.getAssociatingComponentId().equals(importedLRLF.getAssociatingComponentId());
                if (!sameLR) {
                    SAFRValidationException sve = new SAFRValidationException();
                    sve
                            .setErrorMessage(
                                    importer.getCurrentFile().getName(),
                                    "LRFile association ["
                                            + importedLRLF.getAssociationId()
                                            + "] imported with LR ["+
                                            + importedLRLF
                                                    .getAssociatingComponentId()
                                            + "] and LF ["
                                            + importedLRLF
                                                    .getAssociatedComponentId()
                                            + "] already exists in the target Environment for LR '"
                                            + existingLRLF
                                                    .getAssociatingComponentName()
                                            + " ["
                                            + existingLRLF
                                                    .getAssociatingComponentId()
                                            + "]' and LF '"
                                            + existingLRLF
                                                    .getAssociatedComponentName()
                                            + " ["
                                            + existingLRLF
                                                    .getAssociatedComponentIdString()
                                            + "]'.");
                    throw sve;
                } else {
                    // Rule: If the imported LF id is different then it can be
                    // imported only if there are no Lookup or View dependencies
                    // on this Association.
                    if (!existingLRLF.getAssociatedComponentIdNum().equals(
                            importedLRLF.getAssociatedComponentId())) {
                        // different LF, check dependencies.
                        checkLFLRdeps(existingLRLF, importedLRLF);
                    }
                }
                // existing association should be updated
                importedLRLF.setPersistent(true);
            }
        }		    
		
        // Check for invalid LRLF foreign keys
        // XLRFILEID in View Source
        switch (importer.getComponentType()) {
        case ViewFolder:
        case View:
            // XLRFILEID in ViewSource
            Map<Integer, SAFRTransfer> vwsrcMap = null;
            if (importer.records.containsKey(ViewSourceTransfer.class)) {
                vwsrcMap = importer.records.get(ViewSourceTransfer.class);
            } else {
                vwsrcMap = new HashMap<Integer, SAFRTransfer>(); // empty map
            }
            for (SAFRTransfer tfr : vwsrcMap.values()) {
                ViewSourceTransfer vwsrc = (ViewSourceTransfer) tfr;
                Integer xLRLFId = vwsrc.getLRFileAssocId();
                if (xLRLFId > 0) {
                    if (!catMap.containsKey(xLRLFId)) {
                        SAFRValidationException sve = new SAFRValidationException();
                        sve.setErrorMessage(
                            importer.getCurrentFile().getName(),
                            "View-Source ["
                                    + vwsrc.getId()
                                    + "] refers to LR-File ["
                                    + xLRLFId
                                    + "] but this is not present in the import file.");
                        throw sve;
                    }
                }
            }
        default:
            // no action for other component types
        } // end switch
		
	}

	private void checkLFLRdeps(ComponentAssociation existingLFAssociation,
			ComponentAssociationTransfer importedLFAssociation)
			throws DAOException, SAFRValidationException {
		String dependencies = "";
		Map<Integer, List<DependentComponentTransfer>> dependentLookups = new HashMap<Integer, List<DependentComponentTransfer>>();
		Map<Integer, List<DependentComponentTransfer>> dependentViews = new HashMap<Integer, List<DependentComponentTransfer>>();
		Map<Integer, ComponentAssociation> temporaryMap = new HashMap<Integer, ComponentAssociation>();

		List<Integer> LRLFAssociationIds = new ArrayList<Integer>();
		if (existingLFAssociation.getAssociationId() > 0L) {
			temporaryMap.put(existingLFAssociation.getAssociationId(),
					existingLFAssociation);
			LRLFAssociationIds.add(existingLFAssociation.getAssociationId());
		}

		dependentLookups = DAOFactoryHolder.getDAOFactory()
				.getLogicalRecordDAO().getAssociatedLFLookupDependencies(
						existingLFAssociation.getEnvironmentId(),
						LRLFAssociationIds);

		dependentViews = DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
				.getAssociatedLFViewDependencies(
						existingLFAssociation.getEnvironmentId(),
						LRLFAssociationIds);

		if (!dependentLookups.isEmpty() || !dependentViews.isEmpty()) {
			Map<Integer, List<List<DependentComponentTransfer>>> dependentComponentMap = new HashMap<Integer, List<List<DependentComponentTransfer>>>();

			// Merging the maps of Lookups and Views into a single map
			if (!dependentLookups.isEmpty()) {
				for (Integer assocID : dependentLookups.keySet()) {
					List<List<DependentComponentTransfer>> compList = new ArrayList<List<DependentComponentTransfer>>();
					compList.add(dependentLookups.get(assocID));
					compList.add(new ArrayList<DependentComponentTransfer>());
					dependentComponentMap.put(assocID, compList);
				}
			}
			if (!dependentViews.isEmpty()) {
				for (Integer assocID : dependentViews.keySet()) {
					if (dependentComponentMap.containsKey(assocID)) {
						dependentComponentMap.get(assocID).add(1,
								dependentViews.get(assocID));
					} else {
						List<List<DependentComponentTransfer>> compList = new ArrayList<List<DependentComponentTransfer>>();
						compList
								.add(new ArrayList<DependentComponentTransfer>());
						compList.add(dependentViews.get(assocID));
						dependentComponentMap.put(assocID, compList);
					}
				}
			}

			for (Integer LRLFAssociationId : LRLFAssociationIds) {
				if (dependentComponentMap.containsKey(LRLFAssociationId)) {

					dependencies += "Logical File: ";
					ComponentAssociation componentAssoc = temporaryMap
							.get(LRLFAssociationId);

					dependencies += componentAssoc.getAssociatedComponentName();
					dependencies += "["
							+ componentAssoc.getAssociatedComponentIdNum()
							+ "]" + SAFRUtilities.LINEBREAK;

					if (dependentComponentMap.get(LRLFAssociationId).get(0)
							.size() > 0) {
						dependencies += "    LOOKUP PATHS :" + SAFRUtilities.LINEBREAK;
						List<DependentComponentTransfer> depLookupTransfers = dependentLookups
								.get(LRLFAssociationId);
						for (DependentComponentTransfer depLookup : depLookupTransfers) {
							dependencies += "        " + depLookup.getName()
									+ " [" + depLookup.getId() + "]" + SAFRUtilities.LINEBREAK;
						}
					}

					if (dependentComponentMap.get(LRLFAssociationId).get(1)
							.size() > 0) {
						dependencies += "    VIEWS :" + SAFRUtilities.LINEBREAK;
						List<DependentComponentTransfer> depViewTransfers = dependentViews
								.get(LRLFAssociationId);
						for (DependentComponentTransfer depView : depViewTransfers) {
							dependencies += "        " + depView.getName()
									+ " [" + depView.getId() + "]" + SAFRUtilities.LINEBREAK;
						}
					}
					dependencies += SAFRUtilities.LINEBREAK;
				}
			}
		}
		if (!dependencies.equals("")) {
			SAFRValidationException exception = new SAFRValidationException();
			exception
					.setSafrValidationType(SAFRValidationType.DEPENDENCY_LF_ASSOCIATION_ERROR);
			exception
					.setErrorMessage(Property.LF_ASSOCIATION_DEP, dependencies);
			// create the short message and attach to exception
			StringBuffer buffer = new StringBuffer();
			buffer.append("File '");
			buffer.append(importer.getCurrentFile().getName());
			buffer.append("' contains LR-LF record [");
			buffer.append(importedLFAssociation.getAssociationId());
			buffer.append("] which associates LR [");
			buffer.append(importedLFAssociation.getAssociatingComponentId());
			buffer.append("] and LF [");
			buffer.append(importedLFAssociation.getAssociatedComponentId());
			buffer.append("]. LRLF association [");
			buffer.append(existingLFAssociation.getAssociationId());
			buffer
					.append("] already exists in the target environment, but it associates LR [");
			buffer.append(existingLFAssociation.getAssociatingComponentId());
			buffer.append("] and LF [");
			buffer.append(existingLFAssociation.getAssociatedComponentIdNum());
			buffer
					.append("]. This association cannot be replaced with the imported details due to following dependencies.");
			String shortMsg = buffer.toString();
			exception.setErrorMessage(Property.LF_ASSOCIATION_DEP_IMPORT,
					shortMsg);
			throw exception;
		}
	}

}
