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
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.associations.ViewFolderViewAssociation;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.View;

public class DiffViewFolder extends DiffNodeComp {

    private ViewFolder lhs;
    private ViewFolder rhs;
    
    
    public DiffViewFolder(ViewFolder lhs, ViewFolder rhs) {
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
        if (DiffNode.weFields) {
            diff.add(gi.addStringField("Comment", lhs.getComment(), rhs.getComment(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(gi.addStringField("Created By", lhs.getCreateBy(), rhs.getCreateBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addDateField("Created Time", lhs.getCreateTime(), rhs.getCreateTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addStringField("Modified By", lhs.getModifyBy(), rhs.getModifyBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addDateField("Last Modified", lhs.getModifyTime(), rhs.getModifyTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        }
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            gi.setState(DiffNodeState.Different);
        }
        else {
            gi.setState(DiffNodeState.Same);
        }
        return gi;
    }

    protected DiffNode generateViews() throws SAFRException {
        DiffNodeSection views = new DiffNodeSection();
        views.setName("Views");
        views.setParent(this);
        List<SAFREnvironmentalComponent> lhsList = new ArrayList<SAFREnvironmentalComponent>();
        for (ViewFolderViewAssociation fal : lhs.getViewAssociations()) {
            if (fal.getAssociatedComponentIdNum() != 0) {
                View lhsComp = SAFRApplication.getSAFRFactory().getView(fal.getAssociatedComponentIdNum(), fal.getEnvironmentId());
                lhsList.add(lhsComp);
            }
        }

        List<SAFREnvironmentalComponent> rhsList = new ArrayList<SAFREnvironmentalComponent>();
        for (ViewFolderViewAssociation far : rhs.getViewAssociations()) {
            if (far.getAssociatedComponentIdNum() != 0) {
                View rhsComp = SAFRApplication.getSAFRFactory().getView(far.getAssociatedComponentIdNum(), far.getEnvironmentId());
                rhsList.add(rhsComp);
            }
        }
        
        List<DiffFieldReference> refList = diffReferences(MetaType.VIEWS, "View", lhsList, rhsList, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();        
        for (DiffFieldReference ref :refList) {         
            diff.add(ref.getState());
            views.getFields().add(ref);
        }
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            views.setState(DiffNodeState.Different);
        }
        else {
            views.setState(DiffNodeState.Same);
        }
        
        return views;
    }
    
    @Override
    protected void generateTree() throws SAFRException {
        DiffViewFolder tree = (DiffViewFolder) getGenerated(DiffViewFolder.class, lhs.getId());
        if (tree == null) {
            setId(lhs.getId());
            setName("View Folder");

            DiffNode gi = generateGeneral();
            addChild(gi);            

            DiffNode views = generateViews();
            addChild(views);            
            
            if (gi.getState() == DiffNodeState.Same && views.getState() == DiffNodeState.Same) {
                setState(DiffNodeState.Same);
            }
            else {
                setState(DiffNodeState.Different);
            }   
            
            storeGenerated(DiffViewFolder.class, lhs.getId(), this);
            addMetadataNode(MetaType.VIEW_FOLDERS, this);         
        }

    }

}
