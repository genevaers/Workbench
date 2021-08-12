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
import java.util.List;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.internal.data.PGDAOFactory;
import com.ibm.safr.we.internal.data.dao.PGControlRecordDAO;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ControlRecordQueryBean;

public class TestMigrationCR extends TestMigrationBase {

    private void compare(ControlRecord new_component, ControlRecord base_component)
    throws SAFRException {
        assertEquals(new_component.getName(), base_component.getName());
        assertEquals(new_component.getComment(), base_component.getComment());
        assertEquals(( new_component).getFirstFiscalMonth(),
                ( base_component).getFirstFiscalMonth());
        assertEquals(( new_component).getBeginPeriod(),
                ( base_component).getBeginPeriod());
        assertEquals(( new_component).getEndPeriod(), ( base_component).getEndPeriod());
    }
    
    public void testMigrateCR() throws SAFRException {

        int env_Target = 104;
        int env_Source = 102;
        boolean migrate_related = true;
        ComponentType cr = ComponentType.ControlRecord;

        /* login to environment (target) */

        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */

        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        TestDataLayerHelper PGconnect = new TestDataLayerHelper();
        ConnectionParameters PG_params = PGconnect.getParams();
        UserSessionParameters user = new UserSessionParameters("ADMIN", null, null);

        SortType sort_id = SortType.SORT_BY_ID;

        PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
        Connection con = fact.getConnection();

        PGControlRecordDAO PG_crDAO = new PGControlRecordDAO(con, PG_params, user);

        List<ControlRecordQueryBean> cr_query_list = PG_crDAO.queryAllControlRecords(102, sort_id);
        ControlRecordQueryBean cr_item = cr_query_list.get(1);

        /* set up migration object */

        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), cr, cr_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        ControlRecord cr_migrate = SAFRApplication.getSAFRFactory().getControlRecord(cr_item.getId());

        /* obtain base CR file */
        helper.setEnv(env_Source);
        ControlRecord cr_base = SAFRApplication.getSAFRFactory().getControlRecord(cr_migrate.getId());

        /* Verification test */
        compare(cr_migrate, cr_base);

    }
    
    public void testMigrateCRExist() throws SAFRException {

        int env_Target = 115;
        int env_Source = 102;
        boolean migrate_related = true;
        ComponentType cr = ComponentType.ControlRecord;

        /* login to environment (target) */

        helper.initDataLayer(env_Target);

        /* set up confirm warning strategy to true */

        MockConfirmWarningStrategy migrate_accept = new MockConfirmWarningStrategy(true);

        TestDataLayerHelper PGconnect = new TestDataLayerHelper();
        ConnectionParameters PG_params = PGconnect.getParams();
        UserSessionParameters user = new UserSessionParameters("ADMIN", null, null);

        SortType sort_id = SortType.SORT_BY_ID;

        PGDAOFactory fact = (PGDAOFactory) DAOFactoryHolder.getDAOFactory();
        Connection con = fact.getConnection();

        PGControlRecordDAO PG_crDAO = new PGControlRecordDAO(con, PG_params, user);

        List<ControlRecordQueryBean> cr_query_list = PG_crDAO.queryAllControlRecords(102, sort_id);
        ControlRecordQueryBean cr_item = cr_query_list.get(0);

        /* set up migration object */
        Migration migration = new Migration(getEnvQB(env_Source), getEnvQB(env_Target), cr, cr_item, migrate_related);

        migration.setConfirmWarningStrategy(migrate_accept);
        migration.migrate();

        ControlRecord cr_migrate = SAFRApplication.getSAFRFactory().getControlRecord(cr_item.getId());

        /* obtain base CR file */
        helper.setEnv(env_Source);
        ControlRecord cr_base = SAFRApplication.getSAFRFactory().getControlRecord(cr_migrate.getId());

        /* Verification test */
        compare(cr_migrate, cr_base);

    }
    
}
