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
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;

public class TestMigrationPF extends TestMigrationBase {

    private void compare(PhysicalFile new_component, PhysicalFile base_component)
    throws SAFRException {
        assertEquals(new_component.getName(), base_component.getName());
        assertEquals(new_component.getComment(), base_component.getComment());
        assertEquals(( new_component).getFileTypeCode(),
                ( base_component).getFileTypeCode());
        assertEquals(( new_component).getAccessMethodCode(),
                ( base_component).getAccessMethodCode());
    }
    
    public void testMigratePF() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;
        boolean migrate_related = true;
        ComponentType pf = ComponentType.PhysicalFile;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for physical file */
        List<PhysicalFileQueryBean> pf_query_list = SAFRQuery.queryAllPhysicalFiles(env_Source, SortType.SORT_BY_ID);
        PhysicalFileQueryBean pf_item = pf_query_list.get(0);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), pf, pf_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        PhysicalFile pf_migrate = SAFRApplication.getSAFRFactory().getPhysicalFile(pf_item.getId());

        /* obtain base PF file */
        helper.setEnv(env_Source);
        PhysicalFile pf_base = SAFRApplication.getSAFRFactory().getPhysicalFile(pf_migrate.getId());

        /* Verification test */
        compare(pf_migrate, pf_base);

    }

    public void testMigratePFExist() throws SAFRException {

        int env_Target = 115;
        int env_Source = 102;
        boolean migrate_related = false;
        ComponentType pf = ComponentType.PhysicalFile;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for physical file */
        
        PhysicalFileQueryBean pf_item = getPFQB(8416,102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), pf, pf_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        PhysicalFile pf_migrate = SAFRApplication.getSAFRFactory().getPhysicalFile(pf_item.getId());

        /* obtain base PF file */
        helper.setEnv(env_Source);
        PhysicalFile pf_base = SAFRApplication.getSAFRFactory().getPhysicalFile(pf_migrate.getId());

        /* Verification test */
        compare(pf_migrate, pf_base);

    }
    
}
