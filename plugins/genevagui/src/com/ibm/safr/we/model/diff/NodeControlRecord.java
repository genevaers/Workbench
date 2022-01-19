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


import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.ControlRecord;

public class NodeControlRecord extends DiffNodeComp {

	private ControlRecord cr;
	@SuppressWarnings("unused")
    private int otherEnv;
	
	public NodeControlRecord(ControlRecord cr, DiffNodeState state, int otherEnv) {
		this.cr = cr;
		this.state = state;
		this.otherEnv = otherEnv;
	}

	protected DiffNode generateGeneral() throws SAFRException {
		
		// process general information
		DiffNodeSection gi = new DiffNodeSection();
		gi.setName("General");
		gi.setParent(this);
		gi.setState(state);
		gi.addStringField("Name", cr.getName(), cr.getEnvironmentId(), state);
	    if (DiffNode.weFields) {
    		gi.addStringField("Comments", cr.getComment(), cr.getEnvironmentId(), state);
    		gi.addStringField("Created By", cr.getCreateBy(), cr.getEnvironmentId(), state);
    		gi.addDateField("Created Time", cr.getCreateTime(), cr.getEnvironmentId(), state);
    		gi.addStringField("Modified By", cr.getModifyBy(), cr.getEnvironmentId(), state);
    		gi.addDateField("Last Modified", cr.getModifyTime(), cr.getEnvironmentId(), state);
	    }
		return gi;
	}

	protected DiffNode generateFiscalParameters() throws SAFRException {
		
		// process fiscal parametersS
		DiffNodeSection fp = new DiffNodeSection();
		fp.setName("Fiscal Parameters");
		fp.setParent(this);
		fp.setState(state);
		fp.addIntField("First Fiscal Month", cr.getFirstFiscalMonth(), cr.getEnvironmentId(), state);
		fp.addIntField("Begin Period", cr.getBeginPeriod(), cr.getEnvironmentId(), state);
		fp.addIntField("End Period", cr.getEndPeriod(), cr.getEnvironmentId(), state);		
		return fp;
	}
	
	protected void generateTree() throws SAFRException {

		NodeControlRecord tree = (NodeControlRecord) getGenerated(NodeControlRecord.class, cr.getId());
		if (tree == null) {
			
			setName("Control Record");
			setId(cr.getId());
			setEnvID(cr.getEnvironmentId());
			
			// process fields
			DiffNode gi = generateGeneral();
			addChild(gi);
			
			DiffNode fp = generateFiscalParameters();
			addChild(fp);
			
			storeGenerated(NodeControlRecord.class, cr.getId(), this);
			addMetadataNode(MetaType.CONTROL_RECORDS, this);
		}
	}

}
