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

public class EnvComponentQueryBean extends SAFRQueryBean {

	public EnvComponentQueryBean(
			Integer environmentId, String ename, String cname, Date createTime, 
			String createBy, Date modifyTime, String modifyBy) {
		super(cname, createTime, createBy, modifyTime, modifyBy);
		this.setEnvironmentId(environmentId);
		this.setEname(ename);
	}
	
	private Integer environmentId;
	private String ename;
	
	@Override
	public String getIdLabel() {
		return environmentId != null ? environmentId.toString() : "";
	}

	@Override
	public String getDescriptor() {
		return null;
	}

	public Integer getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(Integer environmentId) {
		this.environmentId = environmentId;
	}
	
	public String getCSVString() {
		return getIdLabel()+","+getEnameLabel()+","+getNameLabel()+","+getCreateTimeLabel()+","+getCreateByLabel()
			+","+getModifyTimeLabel()+","+getModifyByLabel();
	}

    public String getEnameLabel() {
        if (ename == null) {
            return "";
        }
        else {
            return ename;
        }
    }

    public void setEname(String ename) {
        this.ename = ename;
    }
}
