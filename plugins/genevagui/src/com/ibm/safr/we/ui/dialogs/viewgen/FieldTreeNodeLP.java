package com.ibm.safr.we.ui.dialogs.viewgen;

import com.ibm.safr.we.model.query.LogicalRecordQueryBean;

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


import com.ibm.safr.we.model.query.LookupQueryBean;


public class FieldTreeNodeLP extends FieldTreeNode {

    private LookupQueryBean lpBean;
	private LogicalRecordQueryBean lrBean;
    
    public FieldTreeNodeLP(FieldTreeNode parent, FieldNodeType type, String name, LookupQueryBean lpBean) {
        super(parent, type, name);
        this.lpBean = lpBean;
    }
    public LookupQueryBean getLPBean() {
        return lpBean;
    }
	public void addLrBean(LogicalRecordQueryBean lrBean) {
		this.lrBean = lrBean;
	}
	public LogicalRecordQueryBean getLrBean() {
		return lrBean;
	}
    
}
