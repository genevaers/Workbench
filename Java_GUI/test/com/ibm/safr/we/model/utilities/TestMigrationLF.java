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
import java.util.Map;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.exceptions.SAFRCancelException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;

public class TestMigrationLF extends TestMigrationBase {

    private void compare(LogicalFile new_component, LogicalFile base_component)
    throws SAFRException {
        assertEquals(new_component.getName(), base_component.getName());
        assertEquals(new_component.getComment(), base_component.getComment());        
    }
    
    public void testMigrateLF() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for physical file */
        LogicalFileQueryBean lf_item = getLFQB(10,102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), 
                ComponentType.LogicalFile, lf_item, true);
        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        LogicalFile lf_migrate = SAFRApplication.getSAFRFactory().getLogicalFile(lf_item.getId());

        /* obtain base LF file */
        helper.setEnv(env_Source);
        LogicalFile lf_base = SAFRApplication.getSAFRFactory().getLogicalFile(lf_migrate.getId());

        /* Verification test */
        compare(lf_migrate, lf_base);
    }
    
    public void testMigrateLFAssRemDepView() throws SAFRException {

        int env_Target = 115;
        int env_Source = 102;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to false */
        MockConfirmWarningStrategy notaccept = new MockConfirmWarningStrategy(false);
        notaccept.setNumConfirm(1);
        
        /* set up QueryBean for physical file */
        LogicalFileQueryBean lf_item = getLFQB(1,102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), 
                ComponentType.LogicalFile, lf_item, true);
        migration.setConfirmWarningStrategy(notaccept);
        try {
            migration.migrate();
            fail();
        } catch (SAFRCancelException e) {
            String msg = notaccept.getShortMessage();
            assertEquals(msg, "When Logical File 'SimpleLFM[1]' is overwritten in the target environment the following Physical Files will no longer be associated with it because they were not associated with the file in the source environment and Views which use these for Output will be made Inactive.");
            String msg2 = notaccept.getDetailMessage();
            String pattern2 = "(?s)^Physical File: SimplePFLU\\[20\\].*View:.*LFPFDep\\[1\\] - \\[View Properties\\].*$";
            assertTrue(msg2.matches(pattern2));
        }
    }
    
    
    public void testMigrateLFRelatedComps() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;

        boolean migrate_related = false;
        
        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        /* set up QueryBean for Logical file */
        LogicalFileQueryBean lf_item = getLFQB(1357,102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), 
                ComponentType.LogicalFile, lf_item, migrate_related);
        migration.setConfirmWarningStrategy(migrate_accept);
        try {
        migration.migrate();
        }catch (SAFRValidationException e) {
        	e.getContextMessage();
        	Map<Object, ArrayList<String>> e_array = e.getErrorMessageMap();
     
        	String msg = e_array.toString();
            assertEquals(
                    msg,
                    "{PhysicalFile=[Related components that are not migrated must exist in the target Environment but the related Physical File srcPF[8416] does not exist there.]}");     		
    	}   
	}
}
