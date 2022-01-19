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

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

public class DiffLogicalRecord extends DiffNodeComp {

    private LogicalRecord lhs;
    private LogicalRecord rhs;
    
    public DiffLogicalRecord(LogicalRecord lhs, LogicalRecord rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    protected DiffNode generateGeneral() throws SAFRException {
        DiffNodeSection gi = new DiffNodeSection();
        gi.setName("General"); 
        gi.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        diff.add(gi.addStringField("Name", lhs.getName(), rhs.getName(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
        diff.add(gi.addCodeField("Type", lhs.getLRTypeCode(), rhs.getLRTypeCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
        diff.add(gi.addCodeField("Status", lhs.getLRStatusCode(), rhs.getLRStatusCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
        
        List<DiffFieldReference> refList = diffSingleReferences(MetaType.USER_EXIT_ROUTINES, "Lookup Exit", 
            lhs.getLookupExitRoutine(), rhs.getLookupExitRoutine(), 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            gi.getFields().add(ref);
        }
        
        diff.add(gi.addStringField("Lookup Exit Param", lhs.getLookupExitParams(), rhs.getLookupExitParams(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        if (DiffNode.weFields) {
            diff.add(gi.addStringField("Comment", lhs.getComment(), rhs.getComment(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(gi.addStringField("Created By", lhs.getCreateBy(), rhs.getCreateBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addDateField("Created Time", lhs.getCreateTime(), rhs.getCreateTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addStringField("Modified By", lhs.getModifyBy(), rhs.getModifyBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addDateField("Last Modified", lhs.getModifyTime(), rhs.getModifyTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addStringField("De/activated By", lhs.getActivatedBy(), rhs.getActivatedBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addDateField("Last De/activated", lhs.getActivatedTime(), rhs.getActivatedTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        }
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            gi.setState(DiffNodeState.Different);
        }
        else {
            gi.setState(DiffNodeState.Same);
        }
        return gi;
    }

    @SuppressWarnings("unchecked")
    protected DiffNode generateFields() throws SAFRException {
        DiffNode fields = new DiffNodeSection();
        fields.setName("Fields");
        fields.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();

        List<SAFREnvironmentalComponent> lhsList = (List<SAFREnvironmentalComponent>)(List<?>)lhs.getLRFields();
        List<SAFREnvironmentalComponent> rhsList = (List<SAFREnvironmentalComponent>)(List<?>)rhs.getLRFields();
        List<DiffNodeComp> flist = diffChildren(lhsList, rhsList, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());

        for (DiffNodeComp node : flist) {
            node.setParent(fields);
            diff.add(node.getState());
            fields.addChild(node);
        }
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            fields.setState(DiffNodeState.Different);
        }
        else {
            fields.setState(DiffNodeState.Same);
        }
        
        return fields;
    }
    
    protected DiffNode generateLogicalFiles() throws SAFRException {
        DiffNodeSection lfs = new DiffNodeSection();
        lfs.setName("Logical Files");
        lfs.setParent(this);
        
        List<SAFREnvironmentalComponent> lhsList = new ArrayList<SAFREnvironmentalComponent>();
        for (ComponentAssociation cal : lhs.getLogicalFileAssociations()) {
            if (cal.getAssociatedComponentIdNum() != 0) {            
                LogicalFile lhsComp = SAFRApplication.getSAFRFactory().getLogicalFile(cal.getAssociatedComponentIdNum(), cal.getEnvironmentId());
                lhsList.add(lhsComp);
            }
        }

        List<SAFREnvironmentalComponent> rhsList = new ArrayList<SAFREnvironmentalComponent>();
        for (ComponentAssociation car : rhs.getLogicalFileAssociations()) {
            if (car.getAssociatedComponentIdNum() != 0) {            
                LogicalFile rhsComp = SAFRApplication.getSAFRFactory().getLogicalFile(car.getAssociatedComponentIdNum(), car.getEnvironmentId());
                rhsList.add(rhsComp);
            }
        }
        
        List<DiffFieldReference> refList = diffReferences(MetaType.LOGICAL_FILES, "Logical File", lhsList, rhsList, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();        
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            lfs.getFields().add(ref);
        }
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            lfs.setState(DiffNodeState.Different);
        }
        else {
            lfs.setState(DiffNodeState.Same);
        }
        
        return lfs;
    }
    
    @Override
    protected void generateTree() throws SAFRException {
        DiffLogicalRecord tree = (DiffLogicalRecord) getGenerated(DiffLogicalRecord.class, lhs.getId());
        if (tree == null) {
            setId(lhs.getId());
            setName("Logical Record");

            DiffNode gi = generateGeneral();
            addChild(gi);            

            DiffNode fields = generateFields();
            addChild(fields);            
            
            DiffNode lfs = generateLogicalFiles();
            addChild(lfs);            
            
            if (gi.getState() == DiffNodeState.Same &&
                fields.getState() == DiffNodeState.Same &&
                lfs.getState() == DiffNodeState.Same) {
                setState(DiffNodeState.Same);
            }
            else {
                setState(DiffNodeState.Different);
            }   
            
            storeGenerated(DiffLogicalRecord.class, lhs.getId(), this);
            addMetadataNode(MetaType.LOGICAL_RECORDS, this);         
        }
    }

}
