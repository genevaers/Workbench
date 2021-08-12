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


import org.junit.After;
import org.junit.Before;

import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.security.UserSession;

public class TestModelBase {

    private Environment env;
    private User user;
    
    public class MockSAFRFactory extends SAFRFactory {

        Integer nextId = 0;
        
        protected MockSAFRFactory() {
            super();
        }

        @Override
        public int getNextLRFieldId()
            throws SAFRException {
            nextId = nextId +1;
            return nextId;
        }
        
        @Override
        public Environment getEnvironment(Integer id) throws SAFRException {
            return env;
        }

        @Override
        public User getUser(String id) throws SAFRException {
            return user;
        }

        
    }
    
    @Before 
    public void setup() {
        user = new User("TestUser");
        user.setSystemAdmin(true);
        env = new Environment();
        env.setId(1);
        env.setName("TestEnv");
        SAFRApplication.setFactory(new MockSAFRFactory());
        UserSession session = new UserSession(user, env, null);
        SAFRApplication.setUserSessionModel(session);        
        SAFRApplication.getSAFRFactory().setAllCodeSets(Codes.getAllCodeSets());
    }
    
    @After
    public void teardown() {
        DAOFactoryHolder.setDaoFactory(null);
        SAFRApplication.setFactory(null);
        SAFRApplication.setUserSessionModel(null);        
    }
    
}
