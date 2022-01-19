package com.ibm.safr.we.model.diff;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.safr.we.data.transfer.ViewLogicDependencyTransfer;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;


public class DiffLogicDependencies extends DiffNodeSection {
    
    private Integer lhsEnvId;
    private Integer rhsEnvId;
    private List<ViewLogicDependencyTransfer> lhsDeps;
    private List<ViewLogicDependencyTransfer> rhsDeps;
    
    public DiffLogicDependencies(
        Integer lhsEnvId, 
        Integer rhsEnvId, 
        List<ViewLogicDependencyTransfer> lhsDeps,
        List<ViewLogicDependencyTransfer> rhsDeps) {
        this.lhsEnvId = lhsEnvId;
        this.rhsEnvId = rhsEnvId;
        this.lhsDeps = lhsDeps;
        this.rhsDeps = rhsDeps;
    }
    
    private DiffNode generateLookupDeps() {
        DiffNodeSection lps = new DiffNodeSection();
        lps.setName("Lookup Paths");
        lps.setParent(this);
        
        List<SAFREnvironmentalComponent> lhsList = new ArrayList<SAFREnvironmentalComponent>();
        Set<Integer> lookups = new HashSet<Integer>();
        for (ViewLogicDependencyTransfer lhsDep : lhsDeps) {
            if (lhsDep.getLookupPathId() != 0 && !lookups.contains(lhsDep.getLookupPathId())) {            
                LookupPath lhsComp = SAFRApplication.getSAFRFactory().getLookupPath(
                    lhsDep.getLookupPathId(), lhsDep.getEnvironmentId());
                lhsList.add(lhsComp);
                lookups.add(lhsDep.getLookupPathId());
            }
        }
        lookups.clear();

        List<SAFREnvironmentalComponent> rhsList = new ArrayList<SAFREnvironmentalComponent>();
        for (ViewLogicDependencyTransfer rhsDep : rhsDeps) {
            if (rhsDep.getLookupPathId() != 0  && !lookups.contains(rhsDep.getLookupPathId())) {            
                LookupPath lhsComp = SAFRApplication.getSAFRFactory().getLookupPath(
                    rhsDep.getLookupPathId(), rhsDep.getEnvironmentId());
                rhsList.add(lhsComp);
                lookups.add(rhsDep.getLookupPathId());
            }
        }
        
        List<DiffFieldReference> refList = diffReferences(MetaType.LOOKUP_PATHS, "Lookup Paths", lhsList, rhsList, 
            lhsEnvId, rhsEnvId);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();        
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            lps.getFields().add(ref);
        }
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            lps.setState(DiffNodeState.Different);
        }
        else {
            lps.setState(DiffNodeState.Same);
        }
        
        return lps;
    }

    private DiffNodeState generateLFPFDeps() {
        
        List<SAFREnvironmentalComponent> lhsLFList = new ArrayList<SAFREnvironmentalComponent>();
        List<SAFREnvironmentalComponent> lhsPFList = new ArrayList<SAFREnvironmentalComponent>();
        Set<Integer> assocs = new HashSet<Integer>();
        for (ViewLogicDependencyTransfer lhsDep : lhsDeps) {
            if (lhsDep.getFileAssociationId() != 0 && !assocs.contains(lhsDep.getFileAssociationId())) { 
                FileAssociation fileAss = SAFRAssociationFactory.getLogicalFileToPhysicalFileAssociation(
                    lhsDep.getFileAssociationId(), lhsEnvId);
                assocs.add(lhsDep.getFileAssociationId());
                LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(
                    fileAss.getAssociatingComponentId(), lhsEnvId);
                lhsLFList.add(lf);
                PhysicalFile pf = SAFRApplication.getSAFRFactory().getPhysicalFile(
                    fileAss.getAssociatedComponentIdNum(), lhsEnvId);
                lhsPFList.add(pf);
            }
        }

        List<SAFREnvironmentalComponent> rhsLFList = new ArrayList<SAFREnvironmentalComponent>();
        List<SAFREnvironmentalComponent> rhsPFList = new ArrayList<SAFREnvironmentalComponent>();
        assocs.clear();
        for (ViewLogicDependencyTransfer rhsDep : rhsDeps) {
            if (rhsDep.getFileAssociationId() != 0 && !assocs.contains(rhsDep.getFileAssociationId())) { 
                FileAssociation fileAss = SAFRAssociationFactory.getLogicalFileToPhysicalFileAssociation(
                    rhsDep.getFileAssociationId(), rhsEnvId);
                assocs.add(rhsDep.getFileAssociationId());
                LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(
                    fileAss.getAssociatingComponentId(), rhsEnvId);
                rhsLFList.add(lf);
                PhysicalFile pf = SAFRApplication.getSAFRFactory().getPhysicalFile(
                    fileAss.getAssociatedComponentIdNum(), rhsEnvId);
                rhsPFList.add(pf);
            }
        }
        
        
        DiffNodeSection lfs = new DiffNodeSection();
        lfs.setName("Logical Files");
        lfs.setParent(this);
        
        List<DiffFieldReference> refList = diffReferences(MetaType.LOOKUP_PATHS, "Logical Files", lhsLFList, rhsLFList, 
            lhsEnvId, rhsEnvId);
        Set<DiffNodeState> lfsdiff = new HashSet<DiffNodeState>();        
        for (DiffFieldReference ref :refList) {         
            lfsdiff.add(ref.getState());
            lfs.getFields().add(ref);
        }
        if (lfsdiff.contains(DiffNodeState.Different) || lfsdiff.contains(DiffNodeState.Added) || lfsdiff.contains(DiffNodeState.Removed)) {
            lfs.setState(DiffNodeState.Different);
        }
        else {
            lfs.setState(DiffNodeState.Same);
        }        
        addChild(lfs);            
        
        DiffNodeSection pfs = new DiffNodeSection();
        pfs.setName("Physical Files");
        pfs.setParent(this);
        
        refList = diffReferences(MetaType.LOOKUP_PATHS, "Physical Files", lhsPFList, rhsPFList, 
            lhsEnvId, rhsEnvId);
        Set<DiffNodeState> pfsdiff = new HashSet<DiffNodeState>();        
        for (DiffFieldReference ref :refList) {         
            pfsdiff.add(ref.getState());
            pfs.getFields().add(ref);
        }
        if (pfsdiff.contains(DiffNodeState.Different) || pfsdiff.contains(DiffNodeState.Added) || pfsdiff.contains(DiffNodeState.Removed)) {
            pfs.setState(DiffNodeState.Different);
        }
        else {
            pfs.setState(DiffNodeState.Same);
        }        
        addChild(pfs);            
        
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        diff.addAll(lfsdiff);
        diff.addAll(pfsdiff);
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            return DiffNodeState.Different;
        }
        else {
            return DiffNodeState.Same;
        }
        
    }
    
    private DiffNode generateExitDeps() {
        DiffNodeSection exs = new DiffNodeSection();
        exs.setName("Exits");
        exs.setParent(this);
        
        List<SAFREnvironmentalComponent> lhsList = new ArrayList<SAFREnvironmentalComponent>();
        Set<Integer> exits = new HashSet<Integer>();
        for (ViewLogicDependencyTransfer lhsDep : lhsDeps) {
            if (lhsDep.getUserExitRoutineId() != 0 && !exits.contains(lhsDep.getUserExitRoutineId())) {            
                UserExitRoutine lhsComp = SAFRApplication.getSAFRFactory().getUserExitRoutine(
                    lhsDep.getUserExitRoutineId(), lhsDep.getEnvironmentId());
                lhsList.add(lhsComp);
                exits.add(lhsDep.getUserExitRoutineId());
            }
        }
        exits.clear();

        List<SAFREnvironmentalComponent> rhsList = new ArrayList<SAFREnvironmentalComponent>();
        for (ViewLogicDependencyTransfer rhsDep : rhsDeps) {
            if (rhsDep.getUserExitRoutineId() != 0  && !exits.contains(rhsDep.getUserExitRoutineId())) {            
                UserExitRoutine lhsComp = SAFRApplication.getSAFRFactory().getUserExitRoutine(
                    rhsDep.getUserExitRoutineId(), rhsDep.getEnvironmentId());
                rhsList.add(lhsComp);
                exits.add(rhsDep.getUserExitRoutineId());
            }
        }
        
        List<DiffFieldReference> refList = diffReferences(MetaType.USER_EXIT_ROUTINES, "Exits", lhsList, rhsList, 
            lhsEnvId, rhsEnvId);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();        
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            exs.getFields().add(ref);
        }
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            exs.setState(DiffNodeState.Different);
        }
        else {
            exs.setState(DiffNodeState.Same);
        }
        return exs;
    }
    
    public void generateTree() {
        setName("Dependencies");
        
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        
        DiffNode li = generateLookupDeps();
        addChild(li);
        diff.add(li.getState());
        
        diff.add(generateLFPFDeps());

        DiffNode ei = generateExitDeps();
        addChild(ei);
        diff.add(ei.getState());
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            setState(DiffNodeState.Different);
        }
        else {
            setState(DiffNodeState.Same);
        }        
    }
}
