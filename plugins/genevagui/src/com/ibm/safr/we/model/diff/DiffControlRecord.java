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
import java.util.Set;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.ControlRecord;

public class DiffControlRecord extends DiffNodeComp {

	private ControlRecord lhs;
	private ControlRecord rhs;
	
	public DiffControlRecord(ControlRecord lhsCR, ControlRecord rhsCR) {
		this.lhs = lhsCR;
		this.rhs = rhsCR;
	}

	protected DiffNode generateGeneral() {
		Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
		
		// process general information
		DiffNodeSection gi = new DiffNodeSection();
		gi.setName("General");
		gi.setParent(this);
		diff.add(gi.addStringField("Name", lhs.getName(), rhs.getName(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
		if (DiffNode.weFields) {
    		diff.add(gi.addStringField("Comments", lhs.getComment(), rhs.getComment(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
    		diff.add(gi.addStringField("Created By", lhs.getCreateBy(), rhs.getCreateBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
    		diff.add(gi.addDateField("Created Time", lhs.getCreateTime(), rhs.getCreateTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
    		diff.add(gi.addStringField("Modified By", lhs.getModifyBy(), rhs.getModifyBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
    		diff.add(gi.addDateField("Last Modified", lhs.getModifyTime(), rhs.getModifyTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
		}
		if (diff.contains(DiffNodeState.Different)) {
			gi.setState(DiffNodeState.Different);
		}
		else {
			gi.setState(DiffNodeState.Same);			
		}
		return gi;
	}

	protected DiffNode generateFiscalParameters() {
		Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
		
		// process fiscal parametersS
		DiffNodeSection fp = new DiffNodeSection();
		fp.setName("Fiscal Parameters");
		fp.setParent(this);
		diff.add(fp.addIntField("First Fiscal Month", lhs.getFirstFiscalMonth(), rhs.getFirstFiscalMonth(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
		diff.add(fp.addIntField("Begin Period", lhs.getBeginPeriod(), rhs.getBeginPeriod(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
		diff.add(fp.addIntField("End Period", lhs.getEndPeriod(), rhs.getEndPeriod(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));		
		if (diff.contains(DiffNodeState.Different)) {
			fp.setState(DiffNodeState.Different);
		}
		else {
			fp.setState(DiffNodeState.Same);			
		}
		return fp;
	}
	
	protected void generateTree() throws SAFRException {

		DiffControlRecord tree = (DiffControlRecord) getGenerated(DiffControlRecord.class, lhs.getId());
		if (tree == null) {
			
			setName("Control Record");
			setId(lhs.getId());
			
			// process fields
			DiffNode gi = generateGeneral();
			addChild(gi);
			
			DiffNode fp = generateFiscalParameters();
			addChild(fp);
			
			if (gi.getState() == DiffNodeState.Same &&
				fp.getState() == DiffNodeState.Same) {
				setState(DiffNodeState.Same);
			}
			else {
				setState(DiffNodeState.Different);
			}
			storeGenerated(DiffControlRecord.class, lhs.getId(), this);
			addMetadataNode(MetaType.CONTROL_RECORDS, this);
		}
	}

}
