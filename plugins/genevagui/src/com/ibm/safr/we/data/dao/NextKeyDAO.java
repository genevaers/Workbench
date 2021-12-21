package com.ibm.safr.we.data.dao;

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


import java.util.Map;

import com.ibm.safr.we.data.transfer.SAFRTransfer;


public interface NextKeyDAO {

    /**
     * 
     */    
    public void convertKeyIds();
    
    /**
     * 
     */
    public Map<Class<? extends SAFRTransfer>, Integer> getNextKeyIds();

    /**
     * 
     * @param transferClass
     * @param tfrId
     */
    public void setNextKeyId(Class<? extends SAFRTransfer> transferClass, Integer tfrId);
	
}
