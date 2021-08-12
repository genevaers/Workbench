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


import java.util.ArrayList;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.exceptions.SAFRCancelException;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;

public class TestMigrationVF extends TestMigrationBase {

    private void compare(ViewFolder new_component, ViewFolder base_component)
    throws SAFRException {
        assertEquals(new_component.getName(), base_component.getName());
        assertEquals(new_component.getComment(), base_component.getComment());        
    }
    
    public void testMigrateVF() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;
        boolean migrate_related = true;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        ViewFolderQueryBean vf_item = getFolderQB(821,102);

        /* set up migration object */
        ComponentType vf = ComponentType.ViewFolder;
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), vf, vf_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), vf, vf_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();
        
        ViewFolder vf_migrate = SAFRApplication.getSAFRFactory().getViewFolder(vf_item.getId());

        /* obtain base VF file */
        helper.setEnv(env_Source);
        ViewFolder vf_base = SAFRApplication.getSAFRFactory().getViewFolder(vf_migrate.getId());

        /* Verification test */
        compare(vf_migrate, vf_base);

    }
    public void testMigrateVFDuplicate() throws SAFRException {
    	
    	int env_Target = 104;
    	int env_Source = 117;
    	
    	boolean migrate_related = true;
    	
    
    	/* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        ViewFolderQueryBean vf_item = getFolderQB(5,117);

        /* set up migration object */
        ComponentType vf = ComponentType.ViewFolder;
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), vf, vf_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        try {
        migration.migrate();
        } catch (SAFRValidationException e) {
        ArrayList<String> e_array= e.getErrorMessages();
        String msg = e_array.get(0);
        assertEquals(
                msg,
                "The View Folder name 'Default_View_Folder' already exists. Please specify a different name.");
    
        }
    }
    
    public void testMigrateVFInactive() throws SAFRException {
    	
    	int env_Target = 104;
    	int env_Source = 102;
    	
    	boolean migrate_related = true;
    	
    	/* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_cancel = new MockConfirmWarningStrategy(false);

        ViewFolderQueryBean vf_item = getFolderQB(927,102);

        /* set up migration object */
        ComponentType vf = ComponentType.ViewFolder;
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), vf, vf_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_cancel);
        try {
            migration.migrate();
            } catch (SAFRCancelException e) {
        String msg = migrate_cancel.getShortMessage();
        assertEquals(
                msg,
                "These inactive Views in the source View Folder will not be migrated:");
            		}
       	}
    
    public void testMigrateVFInactivVieweDep() throws SAFRException {
    	
    	int env_Target = 115;
    	int env_Source = 102;
    	
    	boolean migrate_related = true;
    	
    
    	/* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_cancel = new MockConfirmWarningStrategy(false);

        ViewFolderQueryBean vf_item = getFolderQB(930,102);

        /* set up migration object */
        ComponentType vf = ComponentType.ViewFolder;
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), vf, vf_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_cancel);
        try {
            migration.migrate();
        } 
        catch (SAFRDependencyException e) {
            assertEquals(e.getContextMessage(),
                "The View ViewSimple2[8719] in the target Environment cannot be loaded because of Inactive components shown below. Activate these components first or delete the View from the target Environment.");
        }
    }
    

}
