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


import java.util.List;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

public class NodeLookupPath extends DiffNodeComp {

    private LookupPath lp;
    private int otherEnv;
    
    public NodeLookupPath(LookupPath lp, DiffNodeState state, int otherEnv) {
        super();
        this.lp = lp;
        this.state = state;
        this.otherEnv = otherEnv;
    }

    protected DiffNode generateGeneral() throws SAFRException {
        DiffNodeSection gi = new DiffNodeSection();
        gi.setName("General");
        gi.setParent(this);
        gi.setState(state);
        gi.addStringField("Name", lp.getName(), lp.getEnvironmentId(), state); 
        gi.addBoolField("Active", lp.isValid(), lp.getEnvironmentId(), state);
        if (DiffNode.weFields) {
            gi.addStringField("Comment", lp.getComment(), lp.getEnvironmentId(), state); 
            gi.addStringField("Created By", lp.getCreateBy(), lp.getEnvironmentId(), state);
            gi.addDateField("Created Time", lp.getCreateTime(), lp.getEnvironmentId(), state);
            gi.addStringField("Modified By", lp.getModifyBy(), lp.getEnvironmentId(), state);
            gi.addDateField("Last Modified", lp.getModifyTime(), lp.getEnvironmentId(), state);
            gi.addStringField("De/activated By", lp.getActivatedBy(), lp.getEnvironmentId(), state);
            gi.addDateField("Last De/activated", lp.getActivatedTime(), lp.getEnvironmentId(), state);
        }
        return gi;
    }
    
    @SuppressWarnings("unchecked")
    protected DiffNode generateSteps() throws SAFRException {
        DiffNode steps = new DiffNodeSection();
        steps.setName("Steps");
        steps.setParent(this);
        steps.setState(state);
        List<SAFREnvironmentalComponent> lhsList = (List<SAFREnvironmentalComponent>)(List<?>)lp.getLookupPathSteps();
        List<DiffNodeComp> flist = nodeChildren(lhsList, state, otherEnv); 

        for (DiffNodeComp node : flist) {
            node.setParent(steps);
            steps.addChild(node);
        }
        
        return steps;
    }
    
    @Override
    protected void generateTree() throws SAFRException {
        NodeLookupPath tree = (NodeLookupPath) getGenerated(NodeLookupPath.class, lp.getId());
        if (tree == null) {
            setId(lp.getId());
            setEnvID(lp.getEnvironmentId());
            setName("Lookup Path");

            DiffNode gi = generateGeneral();
            addChild(gi);            

            DiffNode steps = generateSteps();
            addChild(steps);            
            
            storeGenerated(NodeLookupPath.class, lp.getId(), this);
            addMetadataNode(MetaType.LOOKUP_PATHS, this);         
        }        
    }

}
