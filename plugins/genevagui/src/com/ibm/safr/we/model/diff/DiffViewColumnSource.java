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

import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.ViewLogicDependencyTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.view.ViewColumnSource;

public class DiffViewColumnSource extends DiffNodeComp {

    private ViewColumnSource lhs;
    private ViewColumnSource rhs;
    
    public DiffViewColumnSource(ViewColumnSource lhs, ViewColumnSource rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
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
    
    protected DiffNode generateExtractAssignment() {
        DiffNodeSection out = new DiffNodeSection();
        out.setName("Extract Assignment"); 
        out.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        
        diff.add(out.addLargeStringField("Logic Text", lhs.getExtractColumnAssignment(), rhs.getExtractColumnAssignment(), 
            lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        List<ViewLogicDependencyTransfer> lhsDeps = DAOFactoryHolder.getDAOFactory().
            getViewLogicDependencyDAO().getViewColumnSourceDependencies(
            lhs.getEnvironmentId(), lhs.getView().getId(), lhs.getId());
        
        List<ViewLogicDependencyTransfer> rhsDeps = DAOFactoryHolder.getDAOFactory().
            getViewLogicDependencyDAO().getViewColumnSourceDependencies(
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
    
    @Override
    protected void generateTree() throws SAFRException {
        DiffViewColumnSource tree = (DiffViewColumnSource) getGenerated(DiffViewColumnSource.class, lhs.getId());
        if (tree == null) {
            setId(lhs.getId());
            setDispId("");
            setName("View Column Source");
            
            Set<DiffNodeState> diff = new HashSet<DiffNodeState>();                
            diff.add(addCodeField("Type", lhs.getSourceType(), rhs.getSourceType(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            if (lhs.getSourceType().getGeneralId().equals(Codes.LOOKUP_FIELD)) {
                // Lookup LR reference
                SAFREnvironmentalComponent lhsComp = null;
                LogicalRecordQueryBean lrb = lhs.getLogicalRecordQueryBean();                
                if (lrb != null) {
                    lhsComp = SAFRApplication.getSAFRFactory().getLogicalRecord(
                        lrb.getId(),lrb.getEnvironmentId());            
                }

                SAFREnvironmentalComponent rhsComp = null;
                lrb = rhs.getLogicalRecordQueryBean();                
                if (lrb != null) {
                    rhsComp = SAFRApplication.getSAFRFactory().getLogicalRecord(
                        lrb.getId(),lrb.getEnvironmentId());            
                }
                
                List<DiffFieldReference> refList = diffSingleReferences(MetaType.LOGICAL_RECORDS, 
                    "Lookup LR", lhsComp, rhsComp, 
                    lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
                for (DiffFieldReference ref :refList) {         
                    diff.add(ref.getState());
                    getFields().add(ref);
                }            
                
                // Lookup Path reference                
                lhsComp = null;
                LookupQueryBean lpb = lhs.getLookupQueryBean();
                if (lpb != null) {
                    lhsComp = SAFRApplication.getSAFRFactory().getLookupPath(
                        lpb.getId(),lpb.getEnvironmentId());                         
                }
                
                rhsComp = null;
                lpb = rhs.getLookupQueryBean();
                if (lpb != null) {
                    rhsComp = SAFRApplication.getSAFRFactory().getLookupPath(
                        lpb.getId(),lpb.getEnvironmentId());                         
                }
                
                refList = diffSingleReferences(MetaType.LOOKUP_PATHS, "Lookup LP", lhsComp, rhsComp, 
                    lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
                for (DiffFieldReference ref :refList) {         
                    diff.add(ref.getState());
                    getFields().add(ref);
                }   
                
                String lhsField = "";
                if (lhs.getLRFieldID() != null && lhs.getLRFieldID() != 0 && lhs.getLRField() != null) {
                    lhsField = lhs.getLRField().getDescriptor();
                }
                String rhsField = "";
                if (rhs.getLRFieldID() != null && rhs.getLRFieldID() != 0 && rhs.getLRField() != null) {
                    rhsField = rhs.getLRField().getDescriptor();
                }
                diff.add(addStringField("Lookup Field", lhsField, rhsField, lhs.getEnvironmentId(), rhs.getEnvironmentId()));                 
                diff.add(addCodeField("Eff Date Type", lhs.getEffectiveDateTypeCode(), rhs.getEffectiveDateTypeCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
                diff.add(addStringField("Eff Date Value", lhs.getEffectiveDateValue(), rhs.getEffectiveDateValue(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));                             
            }
            else if (lhs.getSourceType().getGeneralId().equals(Codes.FORMULA)) {
                // add logic references
                DiffNode ea = generateExtractAssignment();
                addChild(ea);      
                diff.add(ea.getState());        
            }
            else if (lhs.getSourceType().getGeneralId().equals(Codes.CONSTANT)) {
                diff.add(addStringField("Value", lhs.getSourceValue(), rhs.getSourceValue(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));                                                             
            }
            else if (lhs.getSourceType().getGeneralId().equals(Codes.SOURCE_FILE_FIELD)) {
                
                String lhsField = "";
                if (lhs.getLRFieldID() != null && lhs.getLRFieldID() != 0 && lhs.getLRField() != null) {
                    lhsField = lhs.getLRField().getDescriptor();
                }
                String rhsField = "";
                if (rhs.getLRFieldID() != null && rhs.getLRFieldID() != 0 && rhs.getLRField() != null) {
                    rhsField = rhs.getLRField().getDescriptor();
                }
                
                diff.add(addStringField("LR Field", lhsField, rhsField, lhs.getEnvironmentId(), rhs.getEnvironmentId()));                
            }
            
            // sort title details
            String lhsTitleStr = getLrLookupFieldString(lhs.getSortKeyTitleLogicalRecordQueryBean(),
                lhs.getSortKeyTitleLookupPathQueryBean(),lhs.getSortKeyTitleLRField());
            
            String rhsTitleStr = getLrLookupFieldString(rhs.getSortKeyTitleLogicalRecordQueryBean(),
                rhs.getSortKeyTitleLookupPathQueryBean(),rhs.getSortKeyTitleLRField());
            
            if (lhsTitleStr != null || rhsTitleStr != null) {
                diff.add(addStringField("Sort Title Lookup Field", lhsTitleStr, rhsTitleStr, lhs.getEnvironmentId(), rhs.getEnvironmentId()));
                diff.add(addCodeField("Sort Title Eff Date Type", lhs.getEffectiveDateTypeCode(), rhs.getEffectiveDateTypeCode(),lhs.getEnvironmentId(), rhs.getEnvironmentId()));
                diff.add(addStringField("Sort Title Eff Date Value", lhs.getEffectiveDateValue(), rhs.getEffectiveDateValue(),lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            }
            
            if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
                setState(DiffNodeState.Different);
            }
            else {
                setState(DiffNodeState.Same);
            }
            
            storeGenerated(DiffViewColumnSource.class, lhs.getId(), this);                                    
        }
    }

}
