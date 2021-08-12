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


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.dao.PGControlRecordDAO;
import com.ibm.safr.we.internal.data.dao.PGLogicalFileDAO;
import com.ibm.safr.we.internal.data.dao.PGLogicalRecordDAO;
import com.ibm.safr.we.internal.data.dao.PGLookupDAO;
import com.ibm.safr.we.internal.data.dao.PGPhysicalFileDAO;
import com.ibm.safr.we.internal.data.dao.PGUserExitRoutineDAO;
import com.ibm.safr.we.internal.data.dao.PGViewDAO;
import com.ibm.safr.we.internal.data.dao.PGViewFolderDAO;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.query.ControlRecordQueryBean;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.view.View;

public class TestMigration extends TestMigrationBase {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.utilities.TestMigration");

    public void testGetTargetEnvironment() throws SAFRException {

        dbsetup();

        Environment env = null;
        env = SAFRApplication.getSAFRFactory().getEnvironment(104);

        Integer env_id = env.getId();
        String env_name = env.getName();
        Date env_ctime = env.getCreateTime();
        String env_cuser = env.getCreateBy();
        Date env_modtime = env.getModifyTime();
        String env_moduser = env.getModifyBy();

        EnvironmentQueryBean envQB_set = new EnvironmentQueryBean(env_id, env_name, true, env_ctime,
                env_cuser, env_modtime, env_moduser);

        Migration migration = new Migration(null, envQB_set, null, (EnvironmentalQueryBean)null, null);

        EnvironmentQueryBean target_env = migration.getTargetEnvironment();

        assertEquals(env_id, target_env.getId());
        assertEquals(env_name, target_env.getName());

    }

    public void testGetSourceEnvironment() throws SAFRException {

        dbsetup();

        Environment env = null;
        env = SAFRApplication.getSAFRFactory().getEnvironment(102);

        Integer env_id = env.getId();
        String env_name = env.getName();
        Date env_ctime = env.getCreateTime();
        String env_cuser = env.getCreateBy();
        Date env_modtime = env.getModifyTime();
        String env_moduser = env.getModifyBy();

        EnvironmentQueryBean envQB_set = new EnvironmentQueryBean(env_id, env_name, true, env_ctime,
                env_cuser, env_modtime, env_moduser);

        Migration migration = new Migration(envQB_set, null, null, (EnvironmentalQueryBean)null, null);

        EnvironmentQueryBean source_env = migration.getSourceEnvironment();

        assertEquals(env_id, source_env.getId());
        assertEquals(env_name, source_env.getName());

    }

    public void testGetComponentType() {

        ComponentType pf = ComponentType.PhysicalFile;
        ComponentType lr = ComponentType.LogicalRecord;
        ComponentType lp = ComponentType.LookupPath;
        ComponentType v = ComponentType.View;
        ComponentType lf = ComponentType.LogicalFile;

        /* migration object created with physical file component type */

        Migration migration_pf = new Migration(null, null, pf, (EnvironmentalQueryBean)null, null);

        ComponentType pf_Get = migration_pf.getComponentType();
        assertEquals(pf_Get.name(), pf.name());

        /* migration object created with logical record component type */

        Migration migration_lr = new Migration(null, null, lr, (EnvironmentalQueryBean)null, null);

        ComponentType lr_Get = migration_lr.getComponentType();
        assertEquals(lr_Get.name(), lr.name());

        /* migration object created with lookup path component type */

        Migration migration_lp = new Migration(null, null, lp, (EnvironmentalQueryBean)null, null);

        ComponentType lp_Get = migration_lp.getComponentType();
        assertEquals(lp_Get.name(), lp.name());

        /* migration object created with view component type */

        Migration migration_v = new Migration(null, null, v, (EnvironmentalQueryBean)null, null);

        ComponentType v_Get = migration_v.getComponentType();
        assertEquals(v_Get.name(), v.name());

        /* migration object created with logical file component type */

        Migration migration_lf = new Migration(null, null, lf, (EnvironmentalQueryBean)null, null);

        ComponentType lf_Get = migration_lf.getComponentType();
        assertEquals(lf_Get.name(), lf.name());

    }

    public void testGetComponenet_pf() throws SAFRException, SQLException {
        helper.initDataLayer(102);

        TestDataLayerHelper PGconnect = new TestDataLayerHelper();
        ConnectionParameters PG_params = PGconnect.getParams();
        UserSessionParameters user = new UserSessionParameters("ADMIN", null, null);

        SortType sort_id = SortType.SORT_BY_ID;

        PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
        Connection con = fact.getConnection();

        PGPhysicalFileDAO PG_pfDAO = new PGPhysicalFileDAO(con, PG_params, user);

        List<PhysicalFileQueryBean> pf_query_list = PG_pfDAO.queryAllPhysicalFiles(102, sort_id);
        PhysicalFileQueryBean pf_item = pf_query_list.get(0);

        Migration migration_pf = new Migration(null, null, null, pf_item, null);

        EnvironmentalQueryBean pf_env_qb = migration_pf.getComponent();

        PhysicalFile pf_spec = SAFRApplication.getSAFRFactory().getPhysicalFile(1);

        assertEquals(pf_env_qb.getName(), "SimplePF");
        assertEquals(pf_env_qb.getId(), pf_spec.getId());
        assertEquals(pf_env_qb.getEnvironmentIdLabel(), "102");

    }

    public void testGetComponenet_lf() throws SAFRException, SQLException {
        helper.initDataLayer(102);

        TestDataLayerHelper PGconnect = new TestDataLayerHelper();
        ConnectionParameters PG_params = PGconnect.getParams();
        UserSessionParameters user = new UserSessionParameters("ADMIN", null, null);

        SortType sort_id = SortType.SORT_BY_ID;

        PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
        Connection con = fact.getConnection();

        PGLogicalFileDAO PG_lfDAO = new PGLogicalFileDAO(con, PG_params, user);

        List<LogicalFileQueryBean> lf_query_list = PG_lfDAO.queryAllLogicalFiles(102, sort_id);
        LogicalFileQueryBean lf_item = lf_query_list.get(0);

        Migration migration_lf = new Migration(null, null, null, lf_item, null);

        EnvironmentalQueryBean lf_env_qb = migration_lf.getComponent();

        LogicalFile lf_spec = SAFRApplication.getSAFRFactory().getLogicalFile(1);

        assertEquals(lf_env_qb.getName(), "SimpleLFM");
        assertEquals(lf_env_qb.getId(), lf_spec.getId());
        assertEquals(lf_env_qb.getEnvironmentIdLabel(), "102");

    }

    public void testGetComponenet_lr() throws SAFRException, SQLException {
        helper.initDataLayer(102);

        TestDataLayerHelper PGconnect = new TestDataLayerHelper();
        ConnectionParameters PG_params = PGconnect.getParams();
        UserSessionParameters user = new UserSessionParameters("ADMIN", null, null);

        SortType sort_id = SortType.SORT_BY_ID;

        PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
        Connection con = fact.getConnection();

        PGLogicalRecordDAO PG_lrDAO = new PGLogicalRecordDAO(con, PG_params, user);

        List<LogicalRecordQueryBean> lr_query_list = PG_lrDAO.queryAllLogicalRecords(102, sort_id);
        LogicalRecordQueryBean lr_item = lr_query_list.get(0);

        Migration migration_lr = new Migration(null, null, null, lr_item, null);

        EnvironmentalQueryBean lr_env_qb = migration_lr.getComponent();

        LogicalRecord lr_spec = SAFRApplication.getSAFRFactory().getLogicalRecord(1);

        assertEquals(lr_env_qb.getName(), "ViewLRLFDep");
        assertEquals(lr_env_qb.getId(), lr_spec.getId());
        assertEquals(lr_env_qb.getEnvironmentIdLabel(), "102");

    }

    public void testGetComponenet_lp() throws SAFRException, SQLException {
        helper.initDataLayer(102);

        TestDataLayerHelper PGconnect = new TestDataLayerHelper();
        ConnectionParameters PG_params = PGconnect.getParams();
        UserSessionParameters user = new UserSessionParameters("ADMIN", null, null);

        SortType sort_id = SortType.SORT_BY_ID;

        PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
        Connection con = fact.getConnection();

        PGLookupDAO PG_lpDAO = new PGLookupDAO(con, PG_params, user);

        List<LookupQueryBean> lp_query_list = PG_lpDAO.queryAllLookups(102, sort_id);
        LookupQueryBean lp_item = lp_query_list.get(0);

        Migration migration_lp = new Migration(null, null, null, lp_item, null);

        EnvironmentalQueryBean lp_env_qb = migration_lp.getComponent();

        LookupPath lp_spec = SAFRApplication.getSAFRFactory().getLookupPath(627);

        assertEquals(lp_env_qb.getName(), "FDWA_LB556_from_LB745_DATE_LOOKUP");
        assertEquals(lp_env_qb.getId(), lp_spec.getId());
        assertEquals(lp_env_qb.getEnvironmentIdLabel(), "102");

    }

    public void testGetComponenet_v() throws SAFRException, SQLException {
        helper.initDataLayer(102);

        TestDataLayerHelper PGconnect = new TestDataLayerHelper();
        ConnectionParameters PG_params = PGconnect.getParams();
        UserSessionParameters user = new UserSessionParameters("ADMIN", null, null);

        SortType sort_id = SortType.SORT_BY_ID;

        PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
        Connection con = fact.getConnection();

        PGViewDAO PG_VDAO = new PGViewDAO(con, PG_params, user);

        List<ViewQueryBean> v_query_list = PG_VDAO.queryAllViewsOld(sort_id, 102, -1);
        ViewQueryBean v_item = v_query_list.get(0);

        Migration migration_v = new Migration(null, null, null, v_item, null);

        EnvironmentalQueryBean v_env_qb = migration_v.getComponent();

        View v_spec = SAFRApplication.getSAFRFactory().getView(2725);

        assertEquals(v_env_qb.getName(), "Profit_And_Loss_Allocations_View");
        assertEquals(v_env_qb.getId(), v_spec.getId());
        assertEquals(v_env_qb.getEnvironmentIdLabel(), "102");

    }

    public void testGetComponenet_cr() throws SAFRException, SQLException {
        helper.initDataLayer(102);

        TestDataLayerHelper PGconnect = new TestDataLayerHelper();
        ConnectionParameters PG_params = PGconnect.getParams();
        UserSessionParameters user = new UserSessionParameters("ADMIN", null, null);

        SortType sort_id = SortType.SORT_BY_ID;

        PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
        Connection con = fact.getConnection();

        PGControlRecordDAO PG_crDAO = new PGControlRecordDAO(con, PG_params, user);

        List<ControlRecordQueryBean> cr_query_list = PG_crDAO.queryAllControlRecords(102, sort_id);
        ControlRecordQueryBean cr_item = cr_query_list.get(1);

        Migration migration_cr = new Migration(null, null, null, cr_item, null);

        EnvironmentalQueryBean cr_env_qb = migration_cr.getComponent();

        ControlRecord cr_spec = SAFRApplication.getSAFRFactory().getControlRecord(79);

        assertEquals(cr_env_qb.getName(), "test_CR");
        assertEquals(cr_env_qb.getId(), cr_spec.getId());
        assertEquals(cr_env_qb.getEnvironmentIdLabel(), "102");
    }

    public void testGetComponenet_UER() throws SAFRException, SQLException {
        helper.initDataLayer(102);

        TestDataLayerHelper PGconnect = new TestDataLayerHelper();
        ConnectionParameters PG_params = PGconnect.getParams();
        UserSessionParameters user = new UserSessionParameters("ADMIN", null, null);

        SortType sort_id = SortType.SORT_BY_ID;

        PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
        Connection con = fact.getConnection();

        PGUserExitRoutineDAO PG_uerDAO = new PGUserExitRoutineDAO(con, PG_params, user);

        List<UserExitRoutineQueryBean> uer_query_list = PG_uerDAO.queryAllUserExitRoutines(102, sort_id);
        UserExitRoutineQueryBean uer_item = uer_query_list.get(uer_query_list.size()-2);

        Migration migration_uer = new Migration(null, null, null, uer_item, null);

        EnvironmentalQueryBean uer_env_qb = migration_uer.getComponent();

        UserExitRoutine uer_spec = SAFRApplication.getSAFRFactory().getUserExitRoutine(239);

        assertEquals(uer_env_qb.getName(), "test_UER");
        assertEquals(uer_env_qb.getId(), uer_spec.getId());
        assertEquals(uer_env_qb.getEnvironmentIdLabel(), "102");
    }

    public void testGetComponenet_vf() throws SAFRException, SQLException {
        helper.initDataLayer(102);

        TestDataLayerHelper PGconnect = new TestDataLayerHelper();
        ConnectionParameters PG_params = PGconnect.getParams();
        UserSessionParameters user = new UserSessionParameters("ADMIN", null, null);

        SortType sort_id = SortType.SORT_BY_ID;

        PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
        Connection con = fact.getConnection();

        PGViewFolderDAO PG_vfDAO = new PGViewFolderDAO(con, PG_params, user);

        List<ViewFolderQueryBean> vf_query_list = PG_vfDAO.queryAllViewFolders(102, sort_id);
        ViewFolderQueryBean vfItem = null;
        for (ViewFolderQueryBean bean : vf_query_list) {
            if (bean.getName().equals("test_VF")) {
                vfItem = bean;
                break;
            }
        }

        Migration migration_vf = new Migration(null, null, null, vfItem, null);

        EnvironmentalQueryBean vf_env_qb = migration_vf.getComponent();

        ViewFolder vf_spec = SAFRApplication.getSAFRFactory().getViewFolder(821);

        assertEquals(vf_env_qb.getName(), "test_VF");
        assertEquals(vf_env_qb.getId(), vf_spec.getId());
        assertEquals(vf_env_qb.getEnvironmentIdLabel(), "102");
    }

    public void testIsSetMigrateRelatedComponents() throws SAFRException {

        dbsetup();
        int env_Target = 104;

        Migration migration = new Migration(null, getEnvQB(env_Target), null, (EnvironmentalQueryBean)null, false);

        assertEquals(migration.isMigrateRelatedComponents(), false);
        migration.setMigrateRelatedComponents(true);
        assertEquals(migration.isMigrateRelatedComponents(), true);

    }
    public void testMigrateOtherComponent() throws SAFRException {
    	
    	int env_Target = 104;
    	int env_Source = 102;
    	
    	boolean migrate_related = true;
    	
    	/* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_cancel = new MockConfirmWarningStrategy(false);

        /* set up migration object */
        ComponentType user = ComponentType.User;
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), user, (EnvironmentalQueryBean)null, migrate_related);
        migration.setConfirmWarningStrategy(migrate_cancel);
        try {
            migration.migrate();
            } catch (SAFRValidationException e) {
        String msg = e.getMessage();
        assertEquals(
                msg,"Specify a component." + SAFRUtilities.LINEBREAK + "The only component types that can be migrated are Control Record, User Exit Routine, Physical File, Logical File, Logical Record, Lookup Path, View and View Folder.");
            		}
       	}
    
    public void testMigrateSpecifyComponent() throws SAFRException {
     	
     	int env_Target = 104;
     	int env_Source = 102;
     	
     	boolean migrate_related = true;
     	
     	/* login to environment (target) */
         helper.initDataLayer(env_Target);

         /* set up confirm warning strategy to true */
         MockConfirmWarningStrategy migrate_cancel = new MockConfirmWarningStrategy(false);

         /* set up migration object */
         ComponentType vf = ComponentType.ViewFolder;
         Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), vf, (EnvironmentalQueryBean)null, migrate_related);

         migration.setConfirmWarningStrategy(migrate_cancel);
         try {
             migration.migrate();
             } catch (SAFRValidationException e) {
            	 String msg = e.getMessage();
         assertEquals(
                 msg,
                 "Specify a component.");
     	 }
	}
    
    public void testMigrateSpecifyEnvironment() throws SAFRException {
     	
     	int env_Target = 104;
     	
     	boolean migrate_related = true;
     	
     	/* login to environment (target) */
         helper.initDataLayer(env_Target);

         /* set up confirm warning strategy to true */
         MockConfirmWarningStrategy migrate_cancel = new MockConfirmWarningStrategy(false);

         EnvironmentQueryBean Source_EQB = null;
         EnvironmentQueryBean Target_EQB = null;

         /* set up migration object */
         ComponentType pf = ComponentType.PhysicalFile;
         
         Migration migration = new Migration(Source_EQB, Target_EQB, pf, (EnvironmentalQueryBean)null, migrate_related);

         migration.setConfirmWarningStrategy(migrate_cancel);
         try {
             migration.migrate();
             } catch (SAFRValidationException e) {
            	 String msg = e.getMessage();
         assertEquals(
                 msg,
                 "Specify a source environment." + SAFRUtilities.LINEBREAK + "Specify a target environment." + SAFRUtilities.LINEBREAK + "Specify a component.");
             		}
        	}
    
    public void testMigrateComponentTypeNull() throws SAFRException {
    	
    	int env_Target = 104;
    	int env_Source = 102;
    	
    	boolean migrate_related = true;
    	
    	/* login to environment (target) */
        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */
        MockConfirmWarningStrategy migrate_cancel = new MockConfirmWarningStrategy(false);

        /* set up migration object */
        ComponentType CT = null;
        
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), CT, (EnvironmentalQueryBean)null, migrate_related);

        migration.setConfirmWarningStrategy(migrate_cancel);
        try {
            migration.migrate();
            } catch (SAFRValidationException e) {
        String msg = e.getMessage();
        assertEquals(
                msg,
                "Specify a component." + SAFRUtilities.LINEBREAK + "Specify a component type.");
            		}
       	}
}
