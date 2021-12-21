package com.ibm.safr.we.model.query;

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


import java.util.Date;

import com.ibm.safr.we.constants.EditRights;

/**
 * Query information for a User-Exit Routine. Includes just the info displayed
 * in a list of User-Exit Routine and the key info needed to instantiate a
 * User-Exit Routine.
 * 
 */
public class UserExitRoutineQueryBean extends EnvironmentalQueryBean {

	private String program;
	private String type;
	private String programType;
	
	/**
	 *Parameterized constructor to initialize values
	 */
	public UserExitRoutineQueryBean(Integer environmentId, Integer id,
			String name, String program, String type, String programType, EditRights rights, Date createTime, String createBy,
			Date modifyTime, String modifyBy) {
		super(environmentId, id, name, rights, createTime,
				createBy, modifyTime, modifyBy);
		this.program = program;
		this.type = type;
		this.programType = programType;
	}

	public String getType() {
        return type;
    }

    /**
	 * @return the program
	 */
	public String getProgram() {
		return program;
	}

    /**
     * @return the program
     */
    public String getProgramType() {
        return programType;
    }
	
	/**
	 * @return the program name or an empty string if it's missing
	 */
	public String getProgramLabel() {
		return program != null ? program : "";
	}
	
    @Override
	public String getDescriptor() {
		return getName() + " [" + Integer.toString(getId()) + "]";
	}

    public String getComboString() {
    	String info = "";
    	if(getName().length() > 0) {
    		info = getName() + " [" + Integer.toString(getId()) + "]   Exec: " + program;
    	}
    	return info;
    }

}
