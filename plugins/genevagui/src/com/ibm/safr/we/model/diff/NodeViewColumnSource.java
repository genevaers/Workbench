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


import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.view.ViewColumnSource;

public class NodeViewColumnSource extends DiffNodeComp {

    private ViewColumnSource vcs;
    private int otherEnv;
    
    public NodeViewColumnSource(ViewColumnSource lhs, DiffNodeState state, int otherEnv) {
        super();
        this.vcs = lhs;
        this.state = state;
        this.otherEnv = otherEnv;
    }

    private String getComboString(String name, Integer id) {
        if (id != null && id > 0) {
            if (name == null)
                name = "";
            return (name + " [" + Integer.toString(id) + "]");
        }
        return "";
    }
    
    private String getLrLookupFieldString(LogicalRecordQueryBean lr, LookupQueryBean lkup, LRField field) {
        if (lr != null && lkup != null && field != null) {
            String sLR = getComboString(lr.getName(), lr.getId());
            String sLKP = getComboString(lkup.getName(), lkup.getId());
            String sField = getComboString(field.getName(), field.getId());
            return sLR + "." + sLKP + "." + sField;
        }
        return null;
    }        
    
    @Override
    protected void generateTree() throws SAFRException {
        NodeViewColumnSource tree = (NodeViewColumnSource) getGenerated(NodeViewColumnSource.class, vcs.getId());
        if (tree == null) {
            setId(vcs.getId());
            setDispId("");            
            setEnvID(vcs.getEnvironmentId());            
            setName("View Column Source");            
            addCodeField("Type", vcs.getSourceType(), vcs.getEnvironmentId(), state);
            if (vcs.getSourceType().getGeneralId().equals(Codes.LOOKUP_FIELD)) {
                // Lookup LR reference
                SAFREnvironmentalComponent comp = null;
                LogicalRecordQueryBean lrb = vcs.getLogicalRecordQueryBean();                
                if (lrb != null) {
                    comp = SAFRApplication.getSAFRFactory().getLogicalRecord(
                        lrb.getId(),lrb.getEnvironmentId());            
                }

                getFields().add(nodeSingleReference(MetaType.LOGICAL_RECORDS, "Lookup LR", comp, state, otherEnv)); 
                
                // Lookup Path reference                
                comp = null;
                LookupQueryBean lpb = vcs.getLookupQueryBean();
                if (lpb != null) {
                    comp = SAFRApplication.getSAFRFactory().getLookupPath(
                        lpb.getId(),lpb.getEnvironmentId());                         
                }
                
                getFields().add(nodeSingleReference(MetaType.LOOKUP_PATHS, "Lookup LP", comp, state, otherEnv)); 
                
                String lhsField = "";
                if (vcs.getLRFieldID() != null && vcs.getLRFieldID() != 0 && vcs.getLRField() != null) {
                    lhsField = vcs.getLRField().getDescriptor();
                }
                addStringField("Lookup Field", lhsField, vcs.getEnvironmentId(), state);                 
                addCodeField("Eff Date Type", vcs.getEffectiveDateTypeCode(), vcs.getEnvironmentId(), state);             
                addStringField("Eff Date Value", vcs.getEffectiveDateValue(), vcs.getEnvironmentId(), state);                             
            }
            else if (vcs.getSourceType().getGeneralId().equals(Codes.FORMULA)) {
                addLargeStringField("Extract Assignment", vcs.getExtractColumnAssignment(), vcs.getEnvironmentId(), state);                
            }
            else if (vcs.getSourceType().getGeneralId().equals(Codes.CONSTANT)) {
                addStringField("Value", vcs.getSourceValue(), vcs.getEnvironmentId(), state);                                                             
            }
            else if (vcs.getSourceType().getGeneralId().equals(Codes.SOURCE_FILE_FIELD)) {
                String lhsField = "";
                if (vcs.getLRFieldID() != null && vcs.getLRFieldID() != 0 && vcs.getLRField() != null) {
                    lhsField = vcs.getLRField().getDescriptor();
                }
                
                addStringField("LR Field", lhsField, vcs.getEnvironmentId(), state);                                                             
            }
            
            // sort title details
            String titleStr = getLrLookupFieldString(vcs.getSortKeyTitleLogicalRecordQueryBean(),
                vcs.getSortKeyTitleLookupPathQueryBean(),vcs.getSortKeyTitleLRField());
            
            if (titleStr != null) {
                addStringField("Sort Title Lookup Field", titleStr, vcs.getEnvironmentId(), state);
                addCodeField("Sort Title Eff Date Type", vcs.getEffectiveDateTypeCode(), vcs.getEnvironmentId(), state);
                addStringField("Sort Title Eff Date Value", vcs.getEffectiveDateValue(), vcs.getEnvironmentId(), state);
            }
            
            storeGenerated(NodeViewColumnSource.class, vcs.getId(), this);                                    
        }
    }

}
