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
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

public class NodeLogicalFile extends DiffNodeComp {

    private LogicalFile lf;
    private int otherEnv;
    
    
    public NodeLogicalFile(LogicalFile lhs, DiffNodeState state, int otherEnv) {
        super();
        this.lf = lhs;
        this.state = state;
        this.otherEnv = otherEnv;
    }

    protected DiffNode generateGeneral() {
        DiffNodeSection gi = new DiffNodeSection();
        gi.setName("General"); 
        gi.setParent(this);
        gi.addStringField("Name", lf.getName(), lf.getEnvironmentId(), state); 
        if (DiffNode.weFields) {
            gi.addStringField("Comment", lf.getComment(), lf.getEnvironmentId(), state); 
            gi.addStringField("Created By", lf.getCreateBy(), lf.getEnvironmentId(), state);
            gi.addDateField("Created Time", lf.getCreateTime(), lf.getEnvironmentId(), state);
            gi.addStringField("Modified By", lf.getModifyBy(), lf.getEnvironmentId(), state);
            gi.addDateField("Last Modified", lf.getModifyTime(), lf.getEnvironmentId(), state);
        }
        gi.setState(state);
        return gi;
    }

    protected DiffNode generatePhysicalFiles() throws SAFRException {
        DiffNodeSection pfs = new DiffNodeSection();
        pfs.setName("Physical Files");
        pfs.setParent(this);
        
        List<SAFREnvironmentalComponent> list = new ArrayList<SAFREnvironmentalComponent>();
        for (FileAssociation fal : lf.getPhysicalFileAssociations()) {
            if (fal.getAssociatedComponentIdNum() != 0) {            
                PhysicalFile comp = SAFRApplication.getSAFRFactory().getPhysicalFile(fal.getAssociatedComponentIdNum(), fal.getEnvironmentId());
                list.add(comp);
            }
        }

        List<DiffFieldReference> refList = nodeReferences(MetaType.PHYSICAL_FILES, "Physical File", list, state, otherEnv);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();        
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            pfs.getFields().add(ref);
        }
        pfs.setState(state);
        
        return pfs;
    }
    
    @Override
    protected void generateTree() throws SAFRException {
        NodeLogicalFile tree = (NodeLogicalFile) getGenerated(NodeLogicalFile.class, lf.getId());
        if (tree == null) {
            setId(lf.getId());
            setEnvID(lf.getEnvironmentId());            
            setName("Logical File");

            DiffNode gi = generateGeneral();
            addChild(gi);            

            DiffNode pfs = generatePhysicalFiles();
            addChild(pfs);            
            
            setState(state);
            
            storeGenerated(NodeLogicalFile.class, lf.getId(), this);
            addMetadataNode(MetaType.LOGICAL_FILES, this);         
        }

    }

}
