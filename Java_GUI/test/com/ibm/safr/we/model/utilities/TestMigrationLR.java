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


import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;

public class TestMigrationLR extends TestMigrationBase {

    private void compare(LogicalRecord new_component, LogicalRecord base_component)
    throws SAFRException {
        assertEquals(new_component.getName(), base_component.getName());
        assertEquals(new_component.getComment(), base_component.getComment());
        assertEquals(( new_component).isActive(), ( base_component).isActive());
        assertEquals(( new_component).getLRTypeCode(), ( base_component).getLRTypeCode());
        assertEquals(( new_component).getTotalLength(),
                ( base_component).getTotalLength());
        assertEquals(( new_component).getPrimayKeyLength(),
                ( base_component).getPrimayKeyLength());
        assertEquals(( new_component).getLRFields().get(0).getDatabaseColumnName(),
                ( base_component).getLRFields().get(0).getDatabaseColumnName());
    }
    
    public void testMigrateLR() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for logical Record */
        LogicalRecordQueryBean lr_item = getLRQB(1360, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), 
                ComponentType.LogicalRecord, lr_item, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        LogicalRecord lr_migrate = SAFRApplication.getSAFRFactory().getLogicalRecord(lr_item.getId());

        /* obtain base LR file */
        helper.initDataLayer(env_Source);
        LogicalRecord lr_base = SAFRApplication.getSAFRFactory().getLogicalRecord(lr_migrate.getId());

        /* Verification test */
        compare(lr_migrate, lr_base);

    }

    public void testMigrateLRLFDepView() throws SAFRException {

        int env_Target = 115;
        int env_Source = 102;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for logical Record */
        LogicalRecordQueryBean lr_item = getLRQB(1, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), 
                ComponentType.LogicalRecord, lr_item, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        try {
            migration.migrate();
            fail();
        } catch (SAFRValidationException e) {
            e.printStackTrace();
            String msg = e.getErrorMessages().get(0);
            String pattern = "(?s)Logical File: SimpleLFM\\[1\\].*VIEWS :.*LFPFDep\\[1\\].*$";
            assertTrue(msg.matches(pattern));
            String msg2 = e.getErrorMessages().get(1);
            String pattern2 = "(?s)^Migration cancelled due to dependencies in the target Environment\\..*" +
            		"Logical Record ViewLRLFDep\\[1\\]exists in the target environment so it will be replaced by " +
            		"the source LR and any Logical File associations which appear in the target LR but not in " +
            		"the source LR should be removed from the target environment\\. However, some of these " +
            		"Logical File associations are already used by existing Lookup Paths or Views in the target environment\\..*$";
            assertTrue(msg2.matches(pattern2));
        }
    }
    
    public void testMigrateLRLFDepLU() throws SAFRException {

        int env_Target = 115;
        int env_Source = 102;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for logical Record */
        LogicalRecordQueryBean lr_item = getLRQB(10, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), 
                ComponentType.LogicalRecord, lr_item, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        try {
            migration.migrate();
            fail();
        } catch (SAFRValidationException e) {
            e.printStackTrace();
            String msg = e.getErrorMessages().get(0);
            String pattern = "(?s)Logical File: SimpleLFLU\\[20\\].*LOOKUP PATHS :.*SimpleLU\\[2\\].*$";
            assertTrue(msg.matches(pattern));
            String msg2 = e.getErrorMessages().get(1);
            String pattern2 = "(?s)^Migration cancelled due to dependencies in the target Environment\\..*" +
                    "Logical Record LookupLRLFDep\\[10\\]exists in the target environment so it will be replaced" +
                    " by the source LR and any Logical File associations which appear in the target LR but " +
                    "not in the source LR should be removed from the target environment\\. However, some of " +
                    "these Logical File associations are already used by existing Lookup Paths or Views in the target environment\\..*$";
            assertTrue(msg2.matches(pattern2));
        }
    }
 
    public void testMigrateLRFieldsDep() throws SAFRException {

        int env_Target = 115;
        int env_Source = 102;

        boolean migrate_related = false;
        
        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_cancel = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for logical Record */
        LogicalRecordQueryBean lr_item = getLRQB(1487, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), 
                ComponentType.LogicalRecord, lr_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_cancel);
        try {
            migration.migrate();
        } catch (SAFRValidationException e) {
       	 String msg = e.getMessage().substring(45,174);
         assertEquals(
                 msg,"View_LR_Dependency[8705] - [Col 1, test1, Column Source]" + SAFRUtilities.LINEBREAK + "Migration cancelled due to dependency errors in the target Environment.");
        }
        }
    
    public void testMigrateLRInactive() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;

        boolean migrate_related = false;
        
        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(false);

        /* set up QueryBean for logical Record */
        LogicalRecordQueryBean lr_item = getLRQB(1484, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), 
                ComponentType.LogicalRecord, lr_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        try {
            migration.migrate();
        } catch (SAFRValidationException e) {
       	 String msg = e.getMessage();
         assertEquals(
                 msg,"Logical Record invalidTargLR is not Active and cannot be migrated.");
        }
        }
    
    public void testMigrateLRNoPerm() throws SAFRException {

        int env_Target = 115;
        int env_Source = 102;

        boolean migrate_related = false;
        
        /* login to environment (target) */
        helper.initDataLayer(env_Target);
        helper.setUser("NOPERM");

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for logical Record */
        LogicalRecordQueryBean lr_item = getLRQB(1360, env_Source);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), 
                ComponentType.LogicalRecord, lr_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        try {
            migration.migrate();
            fail();
        } catch (SAFRException e) {
       	 String msg = e.getMessage();
         assertEquals(msg,"The user is not authorized to migrate into Environment 115");
        	}
        helper.setUser("ADMIN");
       	}
    
    public void testMigrateLRNoPermUser() throws SAFRException {

        int env_Target = 105;
        int env_Source = 102;

        boolean migrate_related = true;
        
        /* login to environment (target) */
        helper.initDataLayer(env_Target);
        helper.setUser("TSTLR");

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for logical Record */
        LogicalRecordQueryBean lr_item = getLRQB(1360, env_Source);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), 
                ComponentType.LogicalRecord, lr_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        try {
            migration.migrate();
            fail();
        } catch (SAFRException e) {
       	 String msg = e.getMessage();
         assertEquals(msg,"The user is not authorized to migrate into Environment 105");
        	}
        helper.setUser("ADMIN");
       	}
}
