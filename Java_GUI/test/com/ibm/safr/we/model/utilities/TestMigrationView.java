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
import java.util.List;
import java.util.Map;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ControlRecordQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.view.View;

public class TestMigrationView extends TestMigrationBase {

    private void compare(View left, View right) throws SAFRException {

        assertEquals(left.getName(), right.getName());
        assertEquals(left.getComment(), right.getComment());
        assertEquals((left).getOutputFormat(), (right).getOutputFormat());
        assertEquals((left).getFormatRecordFilter(), (right).getFormatRecordFilter());
        assertEquals((left).getWriteExitParams(), (right).getWriteExitParams());
        assertEquals((left).getViewColumns().get(0).getHeading1(), (right).getViewColumns().get(0).getHeading1());
        assertEquals((left).getViewColumns().get(0).getSortKeyLabel(), (right).getViewColumns().get(0)
                .getSortKeyLabel());
        assertEquals((left).getViewColumns().get(0).getLength(), (right).getViewColumns().get(0).getLength());
    }

    public void testMigrateViewWithExits() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;

 
        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        ViewQueryBean v_item = getViewQB(8551,102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.View, v_item, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        // migrate again
        migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.View, v_item, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();
        
        View v_migrate = SAFRApplication.getSAFRFactory().getView(v_item.getId());

        /* obtain base V file */
        helper.setEnv(env_Source);
        View v_base = SAFRApplication.getSAFRFactory().getView(v_migrate.getId());

        /* Verification test */
        compare(v_migrate, v_base);
    }

    public void testMigrateLargeView() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        ViewQueryBean vQB = getViewQB(2725, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.View, vQB, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        // migrate again
        migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.View, vQB, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();
        
        View v_migrate = SAFRApplication.getSAFRFactory().getView(vQB.getId());

        /* obtain base V file */
        helper.setEnv(env_Source);
        View v_base = SAFRApplication.getSAFRFactory().getView(v_migrate.getId());

        /* Verification test */
        compare(v_migrate, v_base);
    }
    
    private void migrateLR() throws SAFRException {
        int env_Target = 104;
        int env_Source = 102;

        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        LogicalRecordQueryBean lrMig = getLRQB(1360, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.LogicalRecord, lrMig, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

    }

    private void migrateCR() throws SAFRException {
        int env_Target = 104;
        int env_Source = 102;

        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        List<ControlRecordQueryBean> crList = SAFRQuery.queryAllControlRecords(env_Source, SortType.SORT_BY_NAME);
        ControlRecordQueryBean crMig = crList.get(0);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.ControlRecord, crMig, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

    }

    public void testMigrateViewMisLookup() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        // migrate existing metatdata
        migrateCR();
        migrateLR();

        ViewQueryBean vQB = getViewQB(8690, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.View, vQB, false);
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);
        migration.setConfirmWarningStrategy(migrate_accept);
        try {
            migration.migrate();
        } catch (SAFRValidationException e) {
            e.printStackTrace();
            String msg = e.getErrorMessages().get(0);
            assertEquals(msg, "Related components that are not migrated must exist in the target "
                    + "Environment but the related Lookup Path testImportMetadata_LP[2008] does not exist there.");
        }
    }

    public void testMigrateViewLogicLUDep() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        ViewQueryBean vQB = getViewQB(8691, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.View, vQB, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        View v_migrate = SAFRApplication.getSAFRFactory().getView(vQB.getId());

        /* obtain base V file */
        helper.setEnv(env_Source);
        View v_base = SAFRApplication.getSAFRFactory().getView(v_migrate.getId());

        /* Verification test */
        compare(v_migrate, v_base);
    }

    public void testMigrateViewLUInvalid() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        ViewQueryBean vQB = getViewQB(8692, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.View, vQB, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        try {
            migration.migrate();
        } catch (SAFRDependencyException e) {
            e.printStackTrace();
            String msg = e.getContextMessage();
            assertEquals(
                    msg,
                    "The Lookup Path Invalid[2108] in the source Environment cannot be loaded because of Inactive components shown below. Activate these components first.");
            String msg2 = e.getDependencyString();
            String pattern = "(?s)^Logical Record:.*invalidTargLR\\[1484\\].*$";
            assertTrue(msg2.matches(pattern));

        }
    }

    public void testMigrateViewLUInvalidLogic() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        ViewQueryBean vQB = getViewQB(8693, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.View, vQB, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        try {
            migration.migrate();
        } catch (SAFRDependencyException e) {
            e.printStackTrace();
            String msg = e.getContextMessage();
            assertEquals(
                    msg,
                    "The Lookup Path Invalid[2108] in the source Environment cannot be loaded because of Inactive components shown below. Activate these components first.");
            String msg2 = e.getDependencyString();
            String pattern = "(?s)^Logical Record:.*invalidTargLR\\[1484\\].*$";
            assertTrue(msg2.matches(pattern));

        }
    }

    public void testMigrateViewExitLogicDep() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        ViewQueryBean vQB = getViewQB(8694, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.View, vQB, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        View v_migrate = SAFRApplication.getSAFRFactory().getView(vQB.getId());

        /* obtain base V file */
        helper.setEnv(env_Source);
        View v_base = SAFRApplication.getSAFRFactory().getView(v_migrate.getId());

        /* Verification test */
        compare(v_migrate, v_base);
    }

    public void testMigrateViewLFPFLogicDep() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;

        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        ViewQueryBean vQB = getViewQB(8695, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.View, vQB, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        View v_migrate = SAFRApplication.getSAFRFactory().getView(vQB.getId());

        /* obtain base V file */
        helper.setEnv(env_Source);
        View v_base = SAFRApplication.getSAFRFactory().getView(v_migrate.getId());

        /* Verification test */
        compare(v_migrate, v_base);
    }
    
    public void testMigrateEditRightLF() throws SAFRException {
    	
    	int env_Target = 115;
    	int env_Source = 102;
    	
    	boolean migrate_related = true;
    	
    	/* login to environment (target) */
        helper.initDataLayer(env_Target);

        helper.setUser("MIG_EDIT");
        
        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_okay = new MockConfirmWarningStrategy(true);

        /* set up migration object */
        LogicalFileQueryBean lf_item = getLFQB(10,102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), 
                ComponentType.LogicalFile, lf_item, migrate_related);
        
        migration.setConfirmWarningStrategy(migrate_okay);
        try {
        migration.migrate();
        } catch (SAFRDependencyException e) {
            String msg = e.getContextMessage();           
            assertEquals(
                    msg,
                    "The login Group must have at least Read rights on all related components but has no edit rights on the following components:");
                		}
       helper.setUser("ADMIN");
    }
    
    public void testMigrateViewInactive() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;


        /* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        ViewQueryBean vQB = getViewQB(8696, 102);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), ComponentType.View, vQB, true);

        migration.setConfirmWarningStrategy(migrate_accept);
        try {
            migration.migrate();
        } catch (SAFRValidationException e) {
            Map<Object, ArrayList<String>> e_array = e.getErrorMessageMap();
             String msg = e_array.toString();
            assertEquals(
                    msg,
                    "{View=[View Inactive_view is not Active and cannot be migrated.]}");

        }
    }
    
}
