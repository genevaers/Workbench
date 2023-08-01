package org.genevaers.ccb2lr;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


public class OccursGroup extends GroupField {

    private int times = 1;
    FieldType fieldType;

    @Override
    public FieldType getType() {
        if(fieldType == null) {
            return FieldType.OCCURSGROUP;
        } else {
            return fieldType;
        }
    }

    @Override
    public boolean isSigned() {
        return false;
    }


    public void setTimes(int t) {
        times = t;
    }

    public int getTimes() {
        return times;
    }
    
    public void resetOccurs() {
        fieldType = FieldType.GROUP;
    }

}
