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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.ViewLogicDependencyTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.ViewSource;

public class DiffViewSource extends DiffNodeComp {

    private ViewSource lhs;
    private ViewSource rhs;
    
    public DiffViewSource(ViewSource lhs, ViewSource rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    protected void generateTree() throws SAFRException {
        DiffViewSource tree = (DiffViewSource) getGenerated(DiffViewSource.class, lhs.getId());
        if (tree == null) {
            setId(lhs.getId());
            setDispId((new Integer(Math.max(lhs.getSequenceNo(), rhs.getSequenceNo()))).toString());
            setName("View Source");
            
            Set<DiffNodeState> diff = new HashSet<DiffNodeState>();                
            
            DiffNode gi = generateInput();
            addChild(gi);      
            diff.add(gi.getState());

            DiffNode ex = generateOutput();
            addChild(ex);            
            diff.add(ex.getState());
            
            if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
                setState(DiffNodeState.Different);
            }
            else {
                setState(DiffNodeState.Same);
            }
            
            storeGenerated(DiffViewSource.class, lhs.getId(), this);            
        }      
        
    }
    
    protected DiffNode generateInput() throws SAFRException {
        DiffNodeSection input = new DiffNodeSection();
        input.setName("Extract-Phase Input"); 
        input.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        
        SAFREnvironmentalComponent lhsComp = null;
        if (lhs.getLrFileAssociation() != null && lhs.getLrFileAssociation().getAssociationId() != 0) {
            // referenced LR source
            lhsComp = SAFRApplication.getSAFRFactory().getLogicalRecord(
                lhs.getLrFileAssociation().getAssociatingComponentId(),lhs.getEnvironment().getId());            
        }
        SAFREnvironmentalComponent rhsComp = null;
        if (rhs.getLrFileAssociation() != null && rhs.getLrFileAssociation().getAssociationId() != 0) {
            rhsComp = SAFRApplication.getSAFRFactory().getLogicalRecord(
                rhs.getLrFileAssociation().getAssociatingComponentId(),rhs.getEnvironment().getId());            
        }
        List<DiffFieldReference> refList = diffSingleReferences(MetaType.LOGICAL_RECORDS, 
            "Logical Record", lhsComp, rhsComp, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            input.getFields().add(ref);
        }

        // referenced LF source
        lhsComp = null;
        if (lhs.getLrFileAssociation() != null && lhs.getLrFileAssociation().getAssociationId() != 0) {                
            lhsComp = SAFRApplication.getSAFRFactory().getLogicalFile(
                lhs.getLrFileAssociation().getAssociatedComponentIdNum(),lhs.getEnvironment().getId());
        }
        rhsComp = null;    
        if (rhs.getLrFileAssociation() != null && rhs.getLrFileAssociation().getAssociationId() != 0) {                
            rhsComp = SAFRApplication.getSAFRFactory().getLogicalFile(
                rhs.getLrFileAssociation().getAssociatedComponentIdNum(),rhs.getEnvironment().getId());
        }
        refList = diffSingleReferences(MetaType.LOGICAL_FILES, "Logical File", lhsComp, rhsComp, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            input.getFields().add(ref);
        }            
        
        // add logic references
        DiffNode erf = generateExtractRecordFilter();
        input.addChild(erf);      
        diff.add(erf.getState());        
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            input.setState(DiffNodeState.Different);
        }
        else {
            input.setState(DiffNodeState.Same);
        }
        return input;
        
    }
    
    protected DiffNode generateExtractRecordFilter() {
        DiffNodeSection filt = new DiffNodeSection();
        filt.setName("Extract Record Filter"); 
        filt.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        
        diff.add(filt.addLargeStringField("Logic Text", lhs.getExtractRecordFilter(), rhs.getExtractRecordFilter(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        List<ViewLogicDependencyTransfer> lhsDeps = DAOFactoryHolder.getDAOFactory().
            getViewLogicDependencyDAO().getViewSourceFilterDependencies(
            lhs.getEnvironmentId(), lhs.getView().getId(), lhs.getId());
        
        List<ViewLogicDependencyTransfer> rhsDeps = DAOFactoryHolder.getDAOFactory().
            getViewLogicDependencyDAO().getViewSourceFilterDependencies(
            lhs.getEnvironmentId(), lhs.getView().getId(), lhs.getId());
        
        DiffLogicDependencies deps = new DiffLogicDependencies(lhs.getEnvironmentId(), 
            rhs.getEnvironmentId(), lhsDeps, rhsDeps);
        deps.setParent(filt);
        deps.generateTree();
        filt.addChild(deps);
        diff.add(deps.getState());
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            filt.setState(DiffNodeState.Different);
        }
        else {
            filt.setState(DiffNodeState.Same);
        }
        return filt;
    }

    protected DiffNode generateExtractRecordOutput() {
        DiffNodeSection out = new DiffNodeSection();
        out.setName("Extract Record Output"); 
        out.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        
        diff.add(out.addLargeStringField("Logic Text", lhs.getExtractRecordOutput(), rhs.getExtractRecordOutput(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        List<ViewLogicDependencyTransfer> lhsDeps = DAOFactoryHolder.getDAOFactory().
            getViewLogicDependencyDAO().getViewSourceOutputDependencies(
            lhs.getEnvironmentId(), lhs.getView().getId(), lhs.getId());
        
        List<ViewLogicDependencyTransfer> rhsDeps = DAOFactoryHolder.getDAOFactory().
            getViewLogicDependencyDAO().getViewSourceOutputDependencies(
            lhs.getEnvironmentId(), lhs.getView().getId(), lhs.getId());
        
        DiffLogicDependencies deps = new DiffLogicDependencies(lhs.getEnvironmentId(), 
            rhs.getEnvironmentId(), lhsDeps, rhsDeps);
        deps.setParent(out);
        deps.generateTree();
        out.addChild(deps);
        diff.add(deps.getState());
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            out.setState(DiffNodeState.Different);
        }
        else {
            out.setState(DiffNodeState.Same);
        }
        return out;
    }
    
    protected DiffNode generateOutput() throws SAFRException {
        DiffNodeSection output = new DiffNodeSection();
        output.setName("Extract-Phase Output"); 
        output.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        
        // referenced LF
        SAFREnvironmentalComponent lhsComp = null;
        if (lhs.getExtractFileAssociation() != null && lhs.getExtractFileAssociation().getAssociationId() != 0) {                
            lhsComp = SAFRApplication.getSAFRFactory().getLogicalFile(
                lhs.getExtractFileAssociation().getAssociatingComponentId(),lhs.getEnvironment().getId());
        }
        SAFREnvironmentalComponent rhsComp = null;        
        if (rhs.getExtractFileAssociation() != null && rhs.getExtractFileAssociation().getAssociationId() != 0) {                
            rhsComp = SAFRApplication.getSAFRFactory().getLogicalFile(
                rhs.getExtractFileAssociation().getAssociatingComponentId(),rhs.getEnvironment().getId());
        }
        List<DiffFieldReference> refList =  diffSingleReferences(MetaType.LOGICAL_FILES, "Logical File", lhsComp, rhsComp, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            output.getFields().add(ref);
        }            

        // referenced PF
        lhsComp = null;
        if (lhs.getExtractFileAssociation() != null && lhs.getExtractFileAssociation().getAssociationId() != 0) {                
            lhsComp = SAFRApplication.getSAFRFactory().getPhysicalFile(
                lhs.getExtractFileAssociation().getAssociatedComponentIdNum(),lhs.getEnvironment().getId());
        }
        rhsComp = null;        
        if (rhs.getExtractFileAssociation() != null && rhs.getExtractFileAssociation().getAssociationId() != 0) {                
            rhsComp = SAFRApplication.getSAFRFactory().getPhysicalFile(
                rhs.getExtractFileAssociation().getAssociatedComponentIdNum(),rhs.getEnvironment().getId());
        }
        refList =  diffSingleReferences(MetaType.PHYSICAL_FILES, "Physical File", lhsComp, rhsComp, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            output.getFields().add(ref);
        }            
        
        // referenced exit
        refList = diffSingleReferences(MetaType.USER_EXIT_ROUTINES, "Write Exit", 
            lhs.getWriteExit(), rhs.getWriteExit(), 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            output.getFields().add(ref);
        }        
        diff.add(output.addStringField("Write Exit Param", lhs.getWriteExitParams(), rhs.getWriteExitParams(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(output.addBoolField("Override Output Logic", lhs.isExtractOutputOverriden(), rhs.isExtractOutputOverriden(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        // add logic references
        DiffNode ero = generateExtractRecordOutput();
        output.addChild(ero);      
        diff.add(ero.getState());        
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            output.setState(DiffNodeState.Different);
        }
        else {
            output.setState(DiffNodeState.Same);
        }
        return output;
        
    }
    

}
