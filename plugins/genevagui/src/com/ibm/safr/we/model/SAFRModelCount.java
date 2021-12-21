package com.ibm.safr.we.model;

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


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.SAFRUtilities;

public class SAFRModelCount {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.SAFRModelCount");
    
    private Map<Class<? extends Object>, Integer> map;
    
    public SAFRModelCount() {
        map = new HashMap<Class<? extends Object>, Integer>();        
    }
    
    public void restartCount() {
        map.clear();
    }
    
    public void incCount(Class<? extends Object> cl, int count) {
        if (!map.containsKey(cl)) {
            map.put(cl, 0);
        }
        int oldCount = map.get(cl);
        map.put(cl, count + oldCount);
    }
    
    public void report() {
        // report all model counts
        String report = "Model counts" + SAFRUtilities.LINEBREAK; 
        for (Entry<Class<? extends Object>, Integer> ent : map.entrySet()) {
            report += ent.getKey().getName() + ": " + ent.getValue() + " instances" + SAFRUtilities.LINEBREAK;            
        }
        logger.log(Level.INFO, report);
    }    
    
}
