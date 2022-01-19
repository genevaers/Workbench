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

public abstract class ActivateQueryBean extends EnvironmentalQueryBean {

    private Date activatedTime;
    private String activatedBy;
    
    public ActivateQueryBean(Integer environmentId, Integer id, String name,
        EditRights rights, Date createTime, String createBy, Date modifyTime,
        String modifyBy, Date activatedTime, String activatedBy) {
        super(environmentId, id, name, rights, createTime, createBy, modifyTime,
            modifyBy);
        this.setActivateTime(activatedTime);
        this.setActivateBy(activatedBy);
    }

    public Date getActivatedTime() {
        return activatedTime;
    }

    public void setActivateTime(Date activatedTime) {
        this.activatedTime = activatedTime;
    }

    public String getActivatedBy() {
        return activatedBy;
    }

    public void setActivateBy(String activatedBy) {
        this.activatedBy = activatedBy;
    }

}
