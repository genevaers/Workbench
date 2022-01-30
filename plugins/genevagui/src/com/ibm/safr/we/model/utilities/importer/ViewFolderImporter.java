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
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.ViewFolderViewAssociation;
import com.ibm.safr.we.model.view.View;

public class ViewFolderImporter extends ViewImporter {

    protected ViewFolderRecordParser vfParser;
    protected VFVAssocRecordParser vfvParser;
    protected List<ViewFolder> vfs;
    
    public ViewFolderImporter(ImportUtility importUtility) {
        super(importUtility);
    }

    @Override
    protected void doImport() throws SAFRException, XPathExpressionException {
        SAFRValidationException sve = new SAFRValidationException();
        
        clearMaps();
        parseRecords();

        // Check that View Folder(s) was found
        if (!records.containsKey(ViewFolderTransfer.class)) {
            sve.setErrorMessage(getCurrentFile().getName(),"There are no <ViewFolder> <Record> elements.");
            throw sve;
        }

        // Check for orphaned foreign keys and unreferenced primary keys
        checkReferentialIntegrity();

        // if generated by MR91 generate redefines
        GenerationTransfer genTran = (GenerationTransfer)records.get(GenerationTransfer.class).get(0);
        if (genTran.getProgram().equalsIgnoreCase("MR91")) {
            generateOrdPos();
            generateRedefine();
            generateSources();
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
        crs = createControlRecords();
        views = createViews();
        List<ViewFolder> vfs = createViewFolders();

        // store all model objects within a DB transaction
        boolean success = false;
        while (!success) {
            try {
                // Begin Transaction
                DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
                DAOFactoryHolder.getDAOFactory().getDAOUOW().multiComponentScopeOn();
                SAFRApplication.getTimingMap().startRecording();    
                SAFRApplication.getModelCount().restartCount();
                
                for (ControlRecord cr : crs) {
                    cr.store();
                }
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
                for (View view : views) {
                    view.store();
                }
                for (ViewFolder viewFolder : vfs) {
                    viewFolder.store();
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
                SAFRApplication.getTimingMap().report("DAO Method timings");             
                SAFRApplication.getTimingMap().stopRecording();                             
                SAFRApplication.getModelCount().report();
            }
        } 
    }

    protected void parseRecords() throws SAFRException, XPathExpressionException {
        super.parseRecords();
        vfParser = new ViewFolderRecordParser(this);
        vfParser.parseRecords();
        vfvParser = new VFVAssocRecordParser(this);
        vfvParser.parseRecords();
    }

    protected void checkReferentialIntegrity() throws SAFRValidationException {
        super.checkReferentialIntegrity();
        vfParser.checkReferentialIntegrity();
        vfvParser.checkReferentialIntegrity();
    }

    protected void checkOutOfRangeIds() throws SAFRException {
        super.checkOutOfRangeIds();
        checkOutOfRangeIds(ViewFolderTransfer.class);
        checkOutOfRangeIds(ViewFolderViewAssociationTransfer.class);
    }

    protected List<ViewFolder> createViewFolders() throws SAFRException {
        
        Map<Integer, List<ViewFolderViewAssociation>> vfvModel = createVFVAssociations();

        Map<Integer, SAFRTransfer> map = records.get(ViewFolderTransfer.class);
        List<ViewFolder> vfs = new ArrayList<ViewFolder>();
        if (map != null) {
            for (SAFRTransfer tfr : map.values()) {
                ViewFolder vf = SAFRApplication.getSAFRFactory().initViewFolder((ViewFolderTransfer) tfr);
                if (vfvModel != null && !vfvModel.isEmpty() && vfvModel.containsKey(vf.getId())) {
                    for (ViewFolderViewAssociation assoc : vfvModel.get(vf.getId())) {
                        vf.addAssociatedView(assoc);
                    }
                }
                Map<Integer, List<ViewFolderViewAssociation>> assocsToBeDeleted = findAssocsToBeDeleted();
                if (assocsToBeDeleted.containsKey(vf.getId())) {
                    for (ViewFolderViewAssociation assoc : assocsToBeDeleted.get(vf.getId())) {
                        // add assoc to imported LF so it can be deleted
                        vf.addAssociatedView(assoc);
                        vf.removeViewAssociation(assoc);
                    }
                }
                vf.validate();
                vfs.add(vf);
            }
        }
        return vfs;
    }
    
    private Map<Integer, List<ViewFolderViewAssociation>> createVFVAssociations() throws SAFRException {
        
        Map<Integer, SAFRTransfer> vfvtMap = null;
        if (records.containsKey(ViewFolderViewAssociationTransfer.class)) {
            vfvtMap = records.get(ViewFolderViewAssociationTransfer.class);
        } else {
            vfvtMap = new HashMap<Integer, SAFRTransfer>();// empty map
        }
    
        Map<Integer, List<ViewFolderViewAssociation>> vfvModel = new HashMap<Integer, List<ViewFolderViewAssociation>>();
        for (SAFRTransfer tfr : vfvtMap.values()) {
            ViewFolderViewAssociationTransfer vfvTrans = (ViewFolderViewAssociationTransfer) tfr;
            if (vfvTrans.getAssociatingComponentId() > 0) {
                Integer parentFolderId = vfvTrans.getAssociatingComponentId();
                    
                // If an imported assoc already exists in target, use the
                // existing assoc instead.
                ViewFolderViewAssociation vfvAssoc = (ViewFolderViewAssociation) checkForExistingAssoc(
                        ViewFolderViewAssociation.class,
                        vfvTrans.getAssociatingComponentId(),
                        vfvTrans.getAssociatedComponentId());
                
                if (vfvAssoc == null) {
                    // Use imported association
                    vfvAssoc = new ViewFolderViewAssociation(vfvTrans);
                }
    
                // add association object to model map
                if (vfvModel.containsKey(parentFolderId)) {
                    vfvModel.get(parentFolderId).add(vfvAssoc);
                } else {
                    List<ViewFolderViewAssociation> assocs = new ArrayList<ViewFolderViewAssociation>();
                    assocs.add(vfvAssoc);
                    vfvModel.put(parentFolderId, assocs);
                }
            }
        }
        return vfvModel;
    }
    
    private Map<Integer, List<ViewFolderViewAssociation>> findAssocsToBeDeleted()
    {
        Map<Integer, SAFRTransfer> vfvTranMap = null;
        if (records.containsKey(ViewFolderViewAssociationTransfer.class)) {
            vfvTranMap = records.get(ViewFolderViewAssociationTransfer.class);
        } else {
            vfvTranMap = new HashMap<Integer, SAFRTransfer>(); // empty map
        }
        
        Map<Integer, List<ViewFolderViewAssociation>> assocsMap = new HashMap<Integer, List<ViewFolderViewAssociation>>();
        if (!vfvTranMap.isEmpty()) {
            if (existingAssociations.containsKey(ViewFolderViewAssociation.class)) {
                // check for View assocs to be deleted
                for (ComponentAssociation assoc : existingAssociations.get(ViewFolderViewAssociation.class).values()) {
                    ViewFolderViewAssociation trgAssoc = (ViewFolderViewAssociation) assoc;
                    // proceed if an existing assoc is not being imported
                    if (!isAssocImported(trgAssoc, vfvTranMap)) {
                        // proceed if parent VF is being imported
                        if (records.get(ViewFolderTransfer.class).containsKey(trgAssoc.getAssociatingComponentId())) {
                            // assoc must be deleted
                            if (assocsMap.containsKey(trgAssoc.getAssociatingComponentId())) {
                                assocsMap.get(trgAssoc.getAssociatingComponentId()).add(trgAssoc);
                            } else {
                                List<ViewFolderViewAssociation> assocs = new ArrayList<ViewFolderViewAssociation>();
                                assocs.add(trgAssoc);
                                assocsMap.put(trgAssoc.getAssociatingComponentId(),assocs);
                            }
                        }
                    }
                }
            }
        }
        return assocsMap; // assocs to be deleted
    }    
}