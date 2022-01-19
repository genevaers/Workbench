package com.ibm.safr.we.ui.dialogs.viewgen;

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


import java.util.LinkedList;
import java.util.List;

public class FieldTreeNode {
    enum FieldNodeType {TOP,LRFIELDTOP,LRFIELDHEADER,LRFIELD,LPFIELDTOP,LP,LR,LPFIELDHEADER,LPFIELD};

    private FieldTreeNode parent;
    private FieldNodeType type;
    private String name;
    
    private List<FieldTreeNode> children = new LinkedList<FieldTreeNode>();
    
    public FieldTreeNode(FieldTreeNode parent, FieldNodeType type, String name) {
        this.parent = parent;
        this.type = type;
        this.name = name;
    }
    
    String getName() {
        return name;
    }
    
    public FieldTreeNode getParent() {
        return parent;
    }

    public FieldNodeType getType() {
        return type;
    }

    public List<FieldTreeNode> getChildren() {
        return children;
    }
    
}
