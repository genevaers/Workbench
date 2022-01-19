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
 * Query information for a Lookup. Includes just the info displayed in a list of
 * Lookup and the key info needed to instantiate a Lookup.
 * 
 */
public class LookupQueryBean extends ActivateQueryBean {
    
	private Integer validInd;
	private Integer nSteps;
    private String sourceLR;
    private String targetLR;
    private String targetLF;
    
	/**
	 *Parameterized constructor to initialize values
	 */
	public LookupQueryBean(Integer environmentId, Integer id, String name,
			String sourceLR, int validInd, int nSteps, String targetLR, String targetLF, EditRights rights, 
			Date createTime, String createBy, Date modifyTime, String modifyBy, Date activatedTime, String activatedBy) {
		super(environmentId, id, name, rights, createTime,
				createBy, modifyTime, modifyBy, activatedTime, activatedBy);
		this.sourceLR = sourceLR;
		this.validInd = validInd;
		this.nSteps = nSteps;
		this.targetLR = targetLR;
        this.targetLF = targetLF;
	}

	public Integer getnSteps() {
        return nSteps;
    }

    public String getTargetLR() {
        return targetLR;
    }

    public String getTargetLF() {
        return targetLF;
    }

    public String getSourceLR() {
		return sourceLR;
	}

	public Integer getValidInd() {
		return validInd;
	}

}
