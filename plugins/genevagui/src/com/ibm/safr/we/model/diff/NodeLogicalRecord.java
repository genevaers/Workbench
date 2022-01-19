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
import java.util.List;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

public class NodeLogicalRecord extends DiffNodeComp {

    private LogicalRecord lr;
    int otherEnv;
    
    public NodeLogicalRecord(LogicalRecord lhs, DiffNodeState state, int otherEnv) {
        super();
        this.lr = lhs;
        this.state = state;
        this.otherEnv = otherEnv;
    }

    protected DiffNode generateGeneral() throws SAFRException {
        DiffNodeSection gi = new DiffNodeSection();
        gi.setName("General");
        gi.setParent(this);
        gi.setState(state);        
        gi.addStringField("Name", lr.getName(), lr.getEnvironmentId(), state); 
        gi.addCodeField("Type", lr.getLRTypeCode(), lr.getEnvironmentId(), state); 
        gi.addCodeField("Status", lr.getLRStatusCode(), lr.getEnvironmentId(), state);         
        gi.getFields().add(nodeSingleReference(MetaType.USER_EXIT_ROUTINES, "Lookup Exit", lr.getLookupExitRoutine(), state, otherEnv));
        
        gi.addStringField("Lookup Exit Param", lr.getLookupExitParams(), lr.getEnvironmentId(), state);
        if (DiffNode.weFields) {
            gi.addStringField("Comment", lr.getComment(), lr.getEnvironmentId(), state); 
            gi.addStringField("Created By", lr.getCreateBy(), lr.getEnvironmentId(), state);
            gi.addDateField("Created Time", lr.getCreateTime(), lr.getEnvironmentId(), state);
            gi.addStringField("Modified By", lr.getModifyBy(), lr.getEnvironmentId(), state);
            gi.addDateField("Last Modified", lr.getModifyTime(), lr.getEnvironmentId(), state);
            gi.addStringField("De/activated By", lr.getActivatedBy(), lr.getEnvironmentId(), state);
            gi.addDateField("Last De/activated", lr.getActivatedTime(), lr.getEnvironmentId(), state);
        }
        return gi;
    }

    @SuppressWarnings("unchecked")
    protected DiffNode generateFields() throws SAFRException {
        DiffNode fields = new DiffNodeSection();
        fields.setName("Fields");
        fields.setParent(this);
        fields.setState(state);
        
        List<SAFREnvironmentalComponent> list = (List<SAFREnvironmentalComponent>)(List<?>)lr.getLRFields();
        List<DiffNodeComp> flist = nodeChildren(list, state, otherEnv);

        for (DiffNodeComp node : flist) {
            node.setParent(fields);
            fields.addChild(node);
        }
        
        return fields;
    }
    
    protected DiffNode generateLogicalFiles() throws SAFRException {
        DiffNodeSection lfs = new DiffNodeSection();
        lfs.setName("Logical Files");
        lfs.setParent(this);
        lfs.setState(state);
        List<SAFREnvironmentalComponent> list = new ArrayList<SAFREnvironmentalComponent>();
        for (ComponentAssociation ca : lr.getLogicalFileAssociations()) {
            if (ca.getAssociatedComponentIdNum() != 0) {                        
                LogicalFile comp = SAFRApplication.getSAFRFactory().getLogicalFile(ca.getAssociatedComponentIdNum(), ca.getEnvironmentId());
                list.add(comp);
            }
        }

        List<DiffFieldReference> refList = nodeReferences(MetaType.LOGICAL_FILES, "Logical File", list, state, otherEnv); 
        for (DiffFieldReference ref :refList) {         
            lfs.getFields().add(ref);
        }
        return lfs;
    }
    
    @Override
    protected void generateTree() throws SAFRException {
        NodeLogicalRecord tree = (NodeLogicalRecord) getGenerated(NodeLogicalRecord.class, lr.getId());
        if (tree == null) {
            setId(lr.getId());
            setEnvID(lr.getEnvironmentId());            
            setName("Logical Record");

            DiffNode gi = generateGeneral();
            addChild(gi);            

            DiffNode fields = generateFields();
            addChild(fields);            
            
            DiffNode lfs = generateLogicalFiles();
            addChild(lfs);            
            
            storeGenerated(NodeLogicalRecord.class, lr.getId(), this);
            addMetadataNode(MetaType.LOGICAL_RECORDS, this);         
        }
    }

}
