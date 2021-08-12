package com.ibm.safr.we.query;

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
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;

public class TestBatchQuery extends TestCase {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.TestBatchQuery");
    TestDataLayerHelper helper = new TestDataLayerHelper();

    public void dbStartup() {
        helper.initDataLayer();
    }
    
    public void testQueryBALEnvComp() throws SAFRException {        
        dbStartup();
        
        helper.setUser("BATCH");
        List<EnvironmentQueryBean> envQueryBeans = null;
        envQueryBeans = SAFRQuery.queryEnvironmentsForBAL(SortType.SORT_BY_NAME);        
        assertNotNull(envQueryBeans);        
        assertEquals(envQueryBeans.size(), 3);
        assertEquals(envQueryBeans.get(0).getId(), new Integer(119));
        assertEquals(envQueryBeans.get(1).getId(), new Integer(118));
        assertEquals(envQueryBeans.get(2).getId(), new Integer(1));
    }

    public void testQueryBALEnv() throws SAFRException {        
        dbStartup();
        helper.setUser("NOPERM");
        List<EnvironmentQueryBean> envQueryBeans = null;
        envQueryBeans = SAFRQuery.queryEnvironmentsForBAL(SortType.SORT_BY_NAME);
        assertNotNull(envQueryBeans);
        assertTrue(envQueryBeans.isEmpty());
        
        helper.setUser("ADMIN");
        envQueryBeans = SAFRQuery.queryEnvironmentsForBAL(SortType.SORT_BY_NAME);
        assertNotNull(envQueryBeans);
        List<EnvironmentQueryBean> envQueryBeans2 = SAFRQuery.queryAllEnvironments(SortType.SORT_BY_NAME);
        assertEquals(envQueryBeans.size(), envQueryBeans2.size());
        int i=0;
        for (EnvironmentQueryBean bean1 : envQueryBeans) {
            EnvironmentQueryBean bean2 = envQueryBeans2.get(i++);
            assertEquals(bean1.getId(),bean2.getId());
        }        
    }
    
    public void testQueryBAVEnvComp() throws SAFRException {        
        dbStartup();
        
        helper.setUser("BATCH");
        List<EnvironmentQueryBean> envQueryBeans = null;
        envQueryBeans = SAFRQuery.queryEnvironmentsForBAV(SortType.SORT_BY_NAME);        
        assertNotNull(envQueryBeans);        
        assertEquals(envQueryBeans.size(), 3);
        assertEquals(envQueryBeans.get(0).getId(), new Integer(119));
        assertEquals(envQueryBeans.get(1).getId(), new Integer(118));
        assertEquals(envQueryBeans.get(2).getId(), new Integer(1));
    }

    public void testQueryBAVEnv() throws SAFRException {
        dbStartup();
        helper.setUser("NOPERM");
        List<EnvironmentQueryBean> envQueryBeans = null;
        envQueryBeans = SAFRQuery.queryEnvironmentsForBAV(SortType.SORT_BY_NAME);
        assertNotNull(envQueryBeans);
        assertTrue(envQueryBeans.isEmpty());
        
        helper.setUser("ADMIN");
        envQueryBeans = SAFRQuery.queryEnvironmentsForBAV(SortType.SORT_BY_NAME);
        assertNotNull(envQueryBeans);
        List<EnvironmentQueryBean> envQueryBeans2 = SAFRQuery.queryAllEnvironments(SortType.SORT_BY_NAME);
        assertEquals(envQueryBeans.size(), envQueryBeans2.size());
        int i=0;
        for (EnvironmentQueryBean bean1 : envQueryBeans) {
            EnvironmentQueryBean bean2 = envQueryBeans2.get(i++);
            assertEquals(bean1.getId(),bean2.getId());
        }
        
    }
    
}
