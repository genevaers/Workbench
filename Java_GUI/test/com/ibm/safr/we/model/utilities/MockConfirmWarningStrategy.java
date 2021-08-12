package com.ibm.safr.we.model.utilities;

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


import java.util.List;

public class MockConfirmWarningStrategy implements ConfirmWarningStrategy {

    private Integer confirmed = 0;
    private String topic = null;
    private String shortMessage = null;
    private String detailMessage = null;
    private List<DependencyData> depData = null;
    
    private Integer numConfirm = null;
    
    public MockConfirmWarningStrategy(boolean result) {
        setResult(result);
    }
    
    public void reset() {
        confirmed = 0;
        topic = null;
        shortMessage = null;
        detailMessage = null;
        depData = null;
    }
    
    public boolean isConfirmed() {
        return confirmed > 0;
    }

    public void setNumConfirm(Integer num) {
        numConfirm = num;
    }
    
    public boolean confirmWarning(String topic, String message) {
        this.topic = topic;
        this.shortMessage = message;
        confirmed++;
        return getResult();
    }

    public boolean confirmWarning(String topic, String shortMessage, String detailMessage) {
        this.topic = topic;
        this.shortMessage = shortMessage;
        this.detailMessage = detailMessage;
        confirmed++;
        return getResult();
    }

    public boolean confirmWarning(String topic, String shortMessage, List<DependencyData> dependencyList) {
        this.topic = topic;
        this.shortMessage = shortMessage;   
        this.depData = dependencyList;
        confirmed++;
        return getResult();
    }

    public String getTopic() {
        return topic;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public List<DependencyData> getDepData() {
        return depData;
    }

    public void setResult(boolean result) {
        if (result) {
            numConfirm = null;
        }
        else {
            numConfirm = 0;
        }
    }

    
    private boolean getResult() {
        if (numConfirm == null) {
            return true;
        }
        else {
            if (confirmed > numConfirm) {
                return false;
            }
            else {
                return true;
            }
        }
    }
    
}
