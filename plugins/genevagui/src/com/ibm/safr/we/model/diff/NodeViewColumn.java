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
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewSortKey;

public class NodeViewColumn extends DiffNodeComp {

    private ViewColumn col;
    private int otherEnv;
    
    public NodeViewColumn(ViewColumn col, DiffNodeState state, int otherEnv) {
        super();
        this.col = col;
        this.state = state;
        this.otherEnv = otherEnv;
    }

    protected DiffNode generateSortKey() throws SAFRException {
        DiffNode skys = new DiffNodeSection();
        skys.setName("View Sort Keys"); 
        skys.setParent(this);
        skys.setState(state);
        List<SAFREnvironmentalComponent> lhsList = new ArrayList<SAFREnvironmentalComponent>();
        ViewSortKey lhsSK = col.getViewSortKey();
        if (lhsSK != null) {
            lhsList.add(lhsSK);
        }

        List<DiffNodeComp> chList = nodeChildren(lhsList, state, otherEnv); 
        for (DiffNodeComp ch :chList) {
            ch.setParent(skys);
            skys.addChild(ch);
        }
        return skys;
    }
    
    @SuppressWarnings("unchecked")
    protected DiffNode generateColumnSources() throws SAFRException {
        DiffNode srcs = new DiffNodeSection();
        srcs.setName("View Column Sources");
        srcs.setParent(this);
        srcs.setState(state);
        List<SAFREnvironmentalComponent> lhsList = (List<SAFREnvironmentalComponent>)(List<?>)col.getViewColumnSources();
        List<DiffNodeComp> flist = nodeChildren(lhsList, state, otherEnv); 

        for (DiffNodeComp node : flist) {
            node.setParent(srcs);
            srcs.addChild(node);
        }
        
        return srcs;        
    }
    
    @Override
    protected void generateTree() throws SAFRException {
        NodeViewColumn tree = (NodeViewColumn) getGenerated(NodeViewColumn.class, col.getId());
        if (tree == null) {
            setId(col.getId());
            setDispId(col.getColumnNo().toString());
            setEnvID(col.getEnvironmentId());
            setName("View Column");
            
            addIntField("Column Number", col.getColumnNo(), col.getEnvironmentId(), state); 
            addStringField("Heading 1", col.getHeading1(), col.getEnvironmentId(), state); 
            addStringField("Heading 2", col.getHeading2(), col.getEnvironmentId(), state); 
            addStringField("Heading 3", col.getHeading3(), col.getEnvironmentId(), state); 
            addIntField("Start Position", col.getStartPosition(), col.getEnvironmentId(), state); 
            addCodeField("Data Type", col.getDataTypeCode(), col.getEnvironmentId(), state);             
            addCodeField("Date/Time Format", col.getDateTimeFormatCode(), col.getEnvironmentId(), state);             
            addIntField("Length", col.getLength(), col.getEnvironmentId(), state); 
            addCodeField("Data Align", col.getDataAlignmentCode(), col.getEnvironmentId(), state); 
            addBoolField("Visible", col.isVisible(), col.getEnvironmentId(), state); 
            addIntField("Spaces", col.getSpacesBeforeColumn(), col.getEnvironmentId(), state); 
            addCodeField("Header Align", col.getHeaderAlignmentCode(), col.getEnvironmentId(), state);             
            addIntField("Decimal Places", col.getDecimals(), col.getEnvironmentId(), state); 
            addIntField("Scaling Factor", col.getScaling(), col.getEnvironmentId(), state); 
            addBoolField("Signed", col.isSigned(), col.getEnvironmentId(), state); 
            addCodeField("Numeric Mask", col.getNumericMaskCode(), col.getEnvironmentId(), state); 
            addStringField("Format Calc", col.getFormatColumnCalculation(), col.getEnvironmentId(), state);
            addCodeField("Rec aggr", col.getRecordAggregationCode(), col.getEnvironmentId(), state);             
            addCodeField("Grp aggr", col.getGroupAggregationCode(), col.getEnvironmentId(), state);
            
            DiffNode cols = generateColumnSources();
            addChild(cols);     
            
            DiffNode skys = generateSortKey();
            addChild(skys);     
            
            storeGenerated(NodeViewColumn.class, col.getId(), this);                                    
        }
    }

}
