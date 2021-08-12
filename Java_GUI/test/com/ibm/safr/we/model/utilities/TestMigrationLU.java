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


import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.LookupPathStep;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LookupQueryBean;

public class TestMigrationLU extends TestMigrationBase {

    private void compare(LookupPath new_component, LookupPath base_component)
    throws SAFRException {
        assertEquals(new_component.getName(), base_component.getName());
        assertEquals(new_component.getComment(), base_component.getComment());

        LookupPathStep new_component_steps = ( new_component).getLookupPathSteps().get(0);
        LookupPathStep base_component_steps = ( base_component).getLookupPathSteps().get(0);

        assertEquals(new_component_steps.getSourceLRId(), base_component_steps.getSourceLRId());
        assertEquals(new_component_steps.getSourceLength(), base_component_steps.getSourceLength());
    }
    
    public void testMigrateLU() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;
        boolean migrate_related = true;
        ComponentType lp = ComponentType.LookupPath;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for logical Record */
        LookupQueryBean lp_item = getLookupQB(2008, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), lp, lp_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        LookupPath lp_migrate = SAFRApplication.getSAFRFactory().getLookupPath(lp_item.getId());

        /* obtain base LP file */
        helper.setEnv(env_Source);
        LookupPath lp_base = SAFRApplication.getSAFRFactory().getLookupPath(lp_migrate.getId());

        /* Verification test */

        compare(lp_migrate, lp_base);

        assertEquals(lp_migrate.getName(), "testImportMetadata_LP");
        assertEquals(lp_migrate.getComment(), "testImportMetadata Lookup Paths");

    }    
    
    public void testMigrateLUInactive() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;
        boolean migrate_related = true;
        ComponentType lp = ComponentType.LookupPath;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for logical Record */
        LookupQueryBean lp_item = getLookupQB(2109, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), lp, lp_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        try {
            migration.migrate();
            } catch (SAFRValidationException e) {
                String msg = e.getMessage();
                assertEquals(msg,"Lookup Path test_lk_inactive is not Active and cannot be migrated.");
                    		}
    }    
    
    public void testMigrateLUExist() throws SAFRException {

        int env_Target = 115;
        int env_Source = 102;
        boolean migrate_related = true;
        ComponentType lp = ComponentType.LookupPath;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for logical Record */
        LookupQueryBean lp_item = getLookupQB(2110, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), lp, lp_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        
        migration.migrate();

        LookupPath lp_migrate = SAFRApplication.getSAFRFactory().getLookupPath(lp_item.getId());

        /* obtain base LP file */
        helper.setEnv(env_Source);
        LookupPath lp_base = SAFRApplication.getSAFRFactory().getLookupPath(lp_migrate.getId());

        /* Verification test */

        compare(lp_migrate, lp_base);

        assertEquals(lp_migrate.getName(), "test_LK_Exist");
   
        assertEquals(lp_migrate.getComment(), "test LK exist");

                    	
    }    
}
