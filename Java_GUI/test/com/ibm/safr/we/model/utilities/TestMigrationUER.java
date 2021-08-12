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

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;

public class TestMigrationUER extends TestMigrationBase {

    private void compare(UserExitRoutine new_component, UserExitRoutine base_component)
    throws SAFRException {
        assertEquals(new_component.getName(), base_component.getName());
        assertEquals(new_component.getComment(), base_component.getComment());
        assertEquals(( new_component).getExecutable(),
                ( base_component).getExecutable());
        assertEquals(( new_component).getLanguageCode(),
                ( base_component).getLanguageCode());
        assertEquals(( new_component).getTypeCode(), ( base_component).getTypeCode());
    }
    
    public void testMigrateUER() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;
        boolean migrate_related = true;
        ComponentType uer = ComponentType.UserExitRoutine;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for User Exit Routine */
        List<UserExitRoutineQueryBean> uer_query_list = SAFRQuery.queryAllUserExitRoutines(env_Source, SortType.SORT_BY_ID);
        UserExitRoutineQueryBean uer_item = uer_query_list.get(0);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), uer, uer_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        UserExitRoutine uer_migrate = SAFRApplication.getSAFRFactory().getUserExitRoutine(uer_item.getId());

        /* obtain base UER file */
        helper.setEnv(env_Source);
        UserExitRoutine uer_base = SAFRApplication.getSAFRFactory().getUserExitRoutine(uer_migrate.getId());

        /* Verification test */
        compare(uer_migrate, uer_base);
    }
    
    public void testMigrateUERExist() throws SAFRException {

        int env_Target = 115;
        int env_Source = 102;
        boolean migrate_related = true;
        ComponentType uer = ComponentType.UserExitRoutine;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for User Exit Routine */
        List<UserExitRoutineQueryBean> uer_query_list = SAFRQuery.queryAllUserExitRoutines(env_Source, SortType.SORT_BY_ID);
        UserExitRoutineQueryBean uer_item = uer_query_list.get(0);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), uer, uer_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        UserExitRoutine uer_migrate = SAFRApplication.getSAFRFactory().getUserExitRoutine(uer_item.getId());

        /* obtain base UER file */
        helper.setEnv(env_Source);
        UserExitRoutine uer_base = SAFRApplication.getSAFRFactory().getUserExitRoutine(uer_migrate.getId());

        /* Verification test */
        compare(uer_migrate, uer_base);
    }
}
