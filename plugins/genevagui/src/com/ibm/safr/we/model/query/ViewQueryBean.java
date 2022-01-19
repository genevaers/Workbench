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
 * Query information for a View. Includes just the info displayed in a list of
 * Views and the key info needed to instantiate a View
 * 
 */
public class ViewQueryBean extends ActivateQueryBean {

	private String status;
	private String oldOutputFormat;
	private String oldType;
	private String compilerVersion;

	public ViewQueryBean(Integer environmentId, Integer id, String name,
			String status, String oldOutputFormat, String oldType, EditRights rights, Date createTime,
			String createBy, Date modifyTime, String modifyBy,  String compilerVersion,
			Date activatedTime, String activatedBy) {
		super(environmentId, id, name, rights, createTime,
				createBy, modifyTime, modifyBy, activatedTime, activatedBy);
		this.status = status;
		this.oldOutputFormat = oldOutputFormat;
		this.oldType = oldType;
        this.compilerVersion = compilerVersion;
	}

	public String getStatus() {
		return status;
	}

	public String getOldOutputFormat() {
		return oldOutputFormat;
	}

	public String getOldType() {
		return oldType;
	}

    public String getCompilerVersion() {
        return compilerVersion;
    }

    public void setCompilerVersion(String compilerVersion) {
        this.compilerVersion = compilerVersion;
    }

}
