package com.ibm.safr.we.model.base;

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

import com.ibm.safr.we.data.transfer.SAFRActiveComponentTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public abstract class SAFRActivatedComponent extends SAFREnvironmentalComponent {

    private Date activatedTime;
    private String activatedBy;
    boolean activated = false;
    boolean updated = false;
    
    public SAFRActivatedComponent(Environment environment) {
        super(environment);
    }

    public SAFRActivatedComponent(Integer environmentId) {
        super(environmentId);
    }

    public SAFRActivatedComponent(SAFRActiveComponentTransfer trans) {
        super(trans);
    }
    
    @Override
    protected void setTransferData(SAFRTransfer tran) {
        super.setTransferData(tran);
        SAFRActiveComponentTransfer trans = (SAFRActiveComponentTransfer)tran;
        trans.setActivatedTime(this.activatedTime);
        trans.setActivatedBy(this.activatedBy);
        trans.setUpdated(isUpdated());
        trans.setActivated(isActivated());
    }
    
    protected void setObjectData(SAFRTransfer tran) {
        super.setObjectData(tran);
        SAFRActiveComponentTransfer trans = (SAFRActiveComponentTransfer)tran;
        this.activatedTime = trans.getActivatedTime();
        this.activatedBy = trans.getActivatedBy();
        this.activated = trans.isActivated();
        this.updated = trans.isUpdated();
    }

    public Date getActivatedTime() {
        return activatedTime;
    }

    public String getActivatedTimeString() {
        return UIUtilities.formatDate(activatedTime);
    }
    
    public void setActivatedTime(Date activatedTime) {
        this.activatedTime = activatedTime;
    }

    public String getActivatedBy() {
        return activatedBy;
    }

    public void setActivatedBy(String activatedBy) {
        this.activatedBy = activatedBy;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public void markActivated() {
        setActivated(true);
    }

    public void markUpdated() {
        setUpdated(true);
    }

    @Override
    public void markModified() {
        super.markModified();
        markUpdated();
    }

    
}
