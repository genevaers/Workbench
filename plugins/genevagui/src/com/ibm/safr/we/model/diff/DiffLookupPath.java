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
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.LookupPathStep;

public class DiffLookupPath extends DiffNodeComp {

    private LookupPath lhs;
    private LookupPath rhs;
    
    public DiffLookupPath(LookupPath lhs, LookupPath rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    protected DiffNode generateGeneral() {
        DiffNodeSection gi = new DiffNodeSection();
        gi.setName("General"); 
        gi.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        diff.add(gi.addStringField("Name", lhs.getName(), rhs.getName(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
        diff.add(gi.addBoolField("Active", lhs.isValid(), rhs.isValid(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
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
    
    public List<DiffNodeComp> diffSteps(
        List<LookupPathStep> lhsList, 
        List<LookupPathStep> rhsList,
        int lhsEnvId,
        int rhsEnvId) throws SAFRException {

        List<DiffNodeComp> nodes = new ArrayList<DiffNodeComp>();
        
        // loop through lhs list
        for (LookupPathStep lhsComp  : lhsList) {
            boolean found = false;
            for (LookupPathStep rhsComp : rhsList) {
                // we have a match
                if (lhsComp.getSequenceNumber().equals(rhsComp.getSequenceNumber())) {
                    // check same or different
                    found = true;
                    DiffNodeComp node = DiffNodeFactory.generateDiffComp(lhsComp, rhsComp);
                    nodes.add(node);
                    break;
                }
            }
            
            // if not found then must have been removed
            if (!found) {
                DiffNodeComp node = DiffNodeFactory.generateNodeComp(lhsComp, DiffNodeState.Removed, rhsEnvId);
                nodes.add(node);                
            }
        }
        
        // loop through rhs list
        for (LookupPathStep rhsComp : rhsList) {
            boolean found = false;
            for (LookupPathStep lhsComp  : lhsList) {
                // we have a match
                if (lhsComp.getSequenceNumber().equals(rhsComp.getSequenceNumber())) {
                    // check same or different
                    found = true;
                    break;
                }
            }
            
            // if not found then must have been removed
            if (!found) {
                DiffNodeComp node = DiffNodeFactory.generateNodeComp(rhsComp, DiffNodeState.Added, lhsEnvId);
                nodes.add(node);                                
            }
        }
        return nodes;
    }
    
    protected DiffNode generateSteps() throws SAFRException {
        DiffNode steps = new DiffNodeSection();
        steps.setName("Steps"); 
        steps.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        List<LookupPathStep> lhsList = lhs.getLookupPathSteps();
        List<LookupPathStep> rhsList = rhs.getLookupPathSteps();
        List<DiffNodeComp> flist = diffSteps(lhsList, rhsList, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());

        for (DiffNodeComp node : flist) {
            node.setParent(steps);
            diff.add(node.getState());
            steps.addChild(node);
        }
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            steps.setState(DiffNodeState.Different);
        }
        else {
            steps.setState(DiffNodeState.Same);
        }
        
        
        return steps;
    }
    
    @Override
    protected void generateTree() throws SAFRException {
        DiffLookupPath tree = (DiffLookupPath) getGenerated(DiffLookupPath.class, lhs.getId());
        if (tree == null) {
            setId(lhs.getId());
            setName("Lookup Path");

            DiffNode gi = generateGeneral();
            addChild(gi);            

            DiffNode steps = generateSteps();
            addChild(steps);            
            
            if (gi.getState() == DiffNodeState.Same &&
                steps.getState() == DiffNodeState.Same) {
                setState(DiffNodeState.Same);
            }
            else {
                setState(DiffNodeState.Different);
            }   
            
            storeGenerated(DiffLookupPath.class, lhs.getId(), this);
            addMetadataNode(MetaType.LOOKUP_PATHS, this);         
        }        
    }

}
