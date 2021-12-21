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
import com.ibm.safr.we.model.UserExitRoutine;

public class DiffUserExitRoutine extends DiffNodeComp {

	UserExitRoutine lhs;
	UserExitRoutine rhs;

	public DiffUserExitRoutine(UserExitRoutine lhs, UserExitRoutine rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	protected void generateTree() throws SAFRException {		

		DiffUserExitRoutine tree = (DiffUserExitRoutine) getGenerated(DiffUserExitRoutine.class, lhs.getId());
		if (tree == null) {
			setId(lhs.getId());
			setName("User Exit Routine");
			
			// process general information
			Set<DiffNodeState> diff = new HashSet<DiffNodeState>();

			DiffNodeSection gi = new DiffNodeSection();
			gi.setName("General");
			gi.setParent(this);
			diff.add(gi.addStringField("Name", lhs.getName(), rhs.getName(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
			diff.add(gi.addCodeField("Type", lhs.getTypeCode(), rhs.getTypeCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addBoolField("Optimize", lhs.isOptimize(), rhs.isOptimize(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
			diff.add(gi.addCodeField("Language", lhs.getLanguageCode(), rhs.getLanguageCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
			diff.add(gi.addStringField("Executable", lhs.getExecutable(), rhs.getExecutable(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
	        if (DiffNode.weFields) {			
    			diff.add(gi.addStringField("Comments", lhs.getComment(), rhs.getComment(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
    			diff.add(gi.addStringField("Created By", lhs.getCreateBy(), rhs.getCreateBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
    			diff.add(gi.addDateField("Created Time", lhs.getCreateTime(), rhs.getCreateTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
    			diff.add(gi.addStringField("Modified By", lhs.getModifyBy(), rhs.getModifyBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
    			diff.add(gi.addDateField("Last Modified", lhs.getModifyTime(), rhs.getModifyTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
	        }
			if (diff.contains(DiffNodeState.Different)) {
				gi.setState(DiffNodeState.Different);
				setState(DiffNodeState.Different);
			}
			else {
				gi.setState(DiffNodeState.Same);			
				setState(DiffNodeState.Same);
			}			
			addChild(gi);
			
			storeGenerated(DiffUserExitRoutine.class, lhs.getId(), this);
			addMetadataNode(MetaType.USER_EXIT_ROUTINES, this);
		}
	}

}
