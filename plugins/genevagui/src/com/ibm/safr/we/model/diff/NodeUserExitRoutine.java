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
import com.ibm.safr.we.model.UserExitRoutine;

public class NodeUserExitRoutine extends DiffNodeComp {

	UserExitRoutine exit;
	int otherEnv;

	public NodeUserExitRoutine(UserExitRoutine lhs, DiffNodeState state, int otherEnv) {
		this.exit = lhs;
		this.state = state;
		this.otherEnv = otherEnv;
	}

	protected void generateTree() throws SAFRException {		

		NodeUserExitRoutine tree = (NodeUserExitRoutine) getGenerated(NodeUserExitRoutine.class, exit.getId());
		if (tree == null) {
			setId(exit.getId());
	        setEnvID(exit.getEnvironmentId());

			setName("User Exit Routine");
			
			DiffNodeSection gi = new DiffNodeSection();
			gi.setState(state);
			gi.setName("General"); 
			gi.setParent(this);
			gi.addStringField("Name", exit.getName(), exit.getEnvironmentId(), state); 
			gi.addCodeField("Type", exit.getTypeCode(), exit.getEnvironmentId(), state);
            gi.addBoolField("Optimize", exit.isOptimize(), exit.getEnvironmentId(), state);
			gi.addCodeField("Language", exit.getLanguageCode(), exit.getEnvironmentId(), state);
			gi.addStringField("Executable", exit.getExecutable(), exit.getEnvironmentId(), state);
  	        if (DiffNode.weFields) {
    			gi.addStringField("Comments", exit.getComment(), exit.getEnvironmentId(), state);
    			gi.addStringField("Created By", exit.getCreateBy(), exit.getEnvironmentId(), state);
    			gi.addDateField("Created Time", exit.getCreateTime(), exit.getEnvironmentId(), state);
    			gi.addStringField("Modified By", exit.getModifyBy(), exit.getEnvironmentId(), state);
    			gi.addDateField("Last Modified", exit.getModifyTime(), exit.getEnvironmentId(), state);
  	        }
			addChild(gi);
			
			storeGenerated(NodeUserExitRoutine.class, exit.getId(), this);
			addMetadataNode(MetaType.USER_EXIT_ROUTINES, this);
		}
	}

}
