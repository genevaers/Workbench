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
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewSortKey;

public class DiffViewColumn extends DiffNodeComp {

    private ViewColumn lhs;
    private ViewColumn rhs;
    
    public DiffViewColumn(ViewColumn lhs, ViewColumn rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }
    
    protected DiffNode generateSortKey() throws SAFRException {
        DiffNode skys = new DiffNodeSection();
        skys.setName("View Sort Keys"); 
        skys.setParent(this);
        
        List<SAFREnvironmentalComponent> lhsList = new ArrayList<SAFREnvironmentalComponent>();
        ViewSortKey lhsSK = lhs.getViewSortKey();
        if (lhsSK != null) {
            lhsList.add(lhsSK);
        }

        List<SAFREnvironmentalComponent> rhsList = new ArrayList<SAFREnvironmentalComponent>();
        ViewSortKey rhsSK = rhs.getViewSortKey();
        if (rhsSK != null) {
            rhsList.add(rhsSK);
        }
        
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();                        
        List<DiffNodeComp> chList = diffChildren(lhsList, rhsList, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
        for (DiffNodeComp ch :chList) {
            ch.setParent(skys);
            diff.add(ch.getState());
            skys.addChild(ch);
        }
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            skys.setState(DiffNodeState.Different);
        }
        else {
            skys.setState(DiffNodeState.Same);
        }        
        return skys;
    }

    @SuppressWarnings("unchecked")
    protected DiffNode generateColumnSources() throws SAFRException {
        DiffNode srcs = new DiffNodeSection();
        srcs.setName("View Column Sources"); 
        srcs.setParent(this);
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();                

        List<SAFREnvironmentalComponent> lhsList = (List<SAFREnvironmentalComponent>)(List<?>)lhs.getViewColumnSources();
        List<SAFREnvironmentalComponent> rhsList = (List<SAFREnvironmentalComponent>)(List<?>)rhs.getViewColumnSources();
        List<DiffNodeComp> flist = diffChildren(lhsList, rhsList, 
            lhs.getEnvironment().getId(), rhs.getEnvironment().getId());

        for (DiffNodeComp node : flist) {
            node.setParent(srcs);
            diff.add(node.getState());
            srcs.addChild(node);
        }
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            srcs.setState(DiffNodeState.Different);
        }
        else {
            srcs.setState(DiffNodeState.Same);
        }
        return srcs;        
    }
        
    
    @Override
    protected void generateTree() throws SAFRException {
        DiffViewColumn tree = (DiffViewColumn) getGenerated(DiffViewColumn.class, lhs.getId());
        if (tree == null) {
            setId(lhs.getId());
            setDispId((new Integer(Math.max(lhs.getColumnNo(), rhs.getColumnNo()))).toString());            
            setName("View Column");
            
            Set<DiffNodeState> diff = new HashSet<DiffNodeState>();                
            diff.add(addIntField("Column Number", lhs.getColumnNo(), rhs.getColumnNo(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addStringField("Heading 1", lhs.getHeading1(), rhs.getHeading1(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addStringField("Heading 2", lhs.getHeading2(), rhs.getHeading2(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addStringField("Heading 3", lhs.getHeading3(), rhs.getHeading3(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addIntField("Start Position", lhs.getStartPosition(), rhs.getStartPosition(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addCodeField("Data Type", lhs.getDataTypeCode(), rhs.getDataTypeCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            diff.add(addCodeField("Date/Time Format", lhs.getDateTimeFormatCode(), rhs.getDateTimeFormatCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            diff.add(addIntField("Length", lhs.getLength(), rhs.getLength(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addCodeField("Data Align", lhs.getDataAlignmentCode(), rhs.getDataAlignmentCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addBoolField("Visible", lhs.isVisible(), rhs.isVisible(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addIntField("Spaces", lhs.getSpacesBeforeColumn(), rhs.getSpacesBeforeColumn(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addCodeField("Header Align", lhs.getHeaderAlignmentCode(), rhs.getHeaderAlignmentCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            diff.add(addIntField("Decimal Places", lhs.getDecimals(), rhs.getDecimals(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addIntField("Scaling Factor", lhs.getScaling(), rhs.getScaling(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addBoolField("Signed", lhs.isSigned(), rhs.isSigned(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addCodeField("Numeric Mask", lhs.getNumericMaskCode(), rhs.getNumericMaskCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
            diff.add(addLargeStringField("Format Calculation", lhs.getFormatColumnCalculation(), rhs.getFormatColumnCalculation(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(addCodeField("Rec aggr", lhs.getRecordAggregationCode(), rhs.getRecordAggregationCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));             
            diff.add(addCodeField("Grp aggr", lhs.getGroupAggregationCode(), rhs.getGroupAggregationCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            
            DiffNode cols = generateColumnSources();
            addChild(cols);     
            diff.add(cols.getState());

            DiffNode skys = generateSortKey();
            addChild(skys);     
            diff.add(skys.getState());
            
            if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
                setState(DiffNodeState.Different);
            }
            else {
                setState(DiffNodeState.Same);
            }
            storeGenerated(DiffViewColumn.class, lhs.getId(), this);                        
        }
    }

}
