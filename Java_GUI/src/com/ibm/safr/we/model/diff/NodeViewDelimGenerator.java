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


import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.diff.DiffBaseNode.DiffNodeState;
import com.ibm.safr.we.model.view.View;

public class NodeViewDelimGenerator {

    private DiffNodeComp parent;
    private View view;
    private DiffNodeState state;
    private DiffNodeSection fm;
    
    public NodeViewDelimGenerator(DiffNodeComp parent, View view, DiffNodeState state) {
        super();
        this.parent = parent;
        this.view = view;
        this.state = state;
    }

    protected DiffNode generateTree() throws SAFRException {
        if (view.getOutputFormat() == OutputFormat.Format_Delimited_Fields) {
            return generateDelim();
        } else {
            return null;
        }
    }

    protected DiffNode generateDelim() throws SAFRException {
        fm = new DiffNodeSection();
        fm.setName("Delimiters"); 
        fm.setParent(parent);
        
        fm.addCodeField("Delim Field", view.getFileFieldDelimiterCode(), view.getEnvironmentId(), state);
        fm.addCodeField("Delim String", view.getFileStringDelimiterCode(), view.getEnvironmentId(), state);
        fm.setState(state);
        
        return fm;
    }    
    
}
