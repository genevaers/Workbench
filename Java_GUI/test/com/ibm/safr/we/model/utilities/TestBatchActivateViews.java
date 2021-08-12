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
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.view.View;

public class TestBatchActivateViews extends TestCase{
	
	protected TestDataLayerHelper helper = new TestDataLayerHelper();
	   
	public void setUp(){
		
		 helper.initDataLayer();
	}
	
	public void tearDown(){
		
		helper.closeDataLayer();
	}
	
    protected ViewQueryBean getViewQB(int id, int env) throws SAFRException {
        View vw = SAFRApplication.getSAFRFactory().getView(id, env);
        
        String name = vw.getName();
        String status = vw.getStatusCode().toString();
        String output = vw.getOutputFormat().toString();
        String type = vw.getOutputFormat().toString();
        Date ctime = vw.getCreateTime();
        String cuser = vw.getCreateBy();
        Date modtime = vw.getModifyTime();
        String moduser = vw.getModifyBy();
                        
        ViewQueryBean qb = new ViewQueryBean(env, id, name, status, output, type, null, 
                ctime, cuser, modtime, moduser, vw.getCompilerVersion(),vw.getActivatedTime(),vw.getActivatedBy());
        return qb;
    }
    

    @SuppressWarnings("static-access")
    public void testActivate() throws SAFRException{
		
		int env_id = 119;
		int view_1_id = 8697;
		int view_2_id = 8711;
		
		helper.initDataLayer(env_id);
		
		Boolean activate = true;
		
		BatchActivateViews BV = new BatchActivateViews();
		MockConfirmWarningStrategy confirm_activate = new MockConfirmWarningStrategy(activate);
		
		List<BatchComponent> batchComponentList = new ArrayList<BatchComponent>();
		
		/* Make views inactive */
		View view1 = SAFRApplication.getSAFRFactory().getView(view_1_id);
		view1.makeViewInactive();
		view1.store();
		
		View view2 = SAFRApplication.getSAFRFactory().getView(view_2_id);
		view2.makeViewInactive();
		view2.store();
		
		
		ViewQueryBean view_1 = getViewQB(view_1_id,env_id);
		ViewQueryBean view_2 = getViewQB(view_2_id,env_id);
		
		BatchComponent v1 = new BatchComponent(view_1, activate) ;
		BatchComponent v2 = new BatchComponent(view_2, activate) ;
		
		batchComponentList.add(v1);
		batchComponentList.add(v2);
		
		BV.activate(batchComponentList, confirm_activate);
		
		View view1_active = SAFRApplication.getSAFRFactory().getView(view_1_id);
		View view2_active = SAFRApplication.getSAFRFactory().getView(view_2_id);
		
		Code view1_active_sc = view1_active.getStatusCode();
		Code view2_active_sc = view2_active.getStatusCode();
		
		assertEquals(true,view1_active_sc.getDescription().matches("Active"));
		assertEquals(true,view2_active_sc.getDescription().matches("Active"));
		
	}
    
    @SuppressWarnings("static-access")
    public void testActivateDep() throws SAFRException{
		
		int env_id = 119;
		int view_1_id = 8712;
		int lk_path_id = 2008;
		int lr_id = 1360;
		
		helper.initDataLayer(env_id);
		
		Boolean activate = true;
		
		BatchActivateViews BV = new BatchActivateViews();
		MockConfirmWarningStrategy confirm_activate = new MockConfirmWarningStrategy(activate);
		
		List<BatchComponent> batchComponentList = new ArrayList<BatchComponent>();
		
		
		ViewQueryBean view_1 = getViewQB(view_1_id,env_id);
		
		LookupPath lkpath = SAFRApplication.getSAFRFactory().getLookupPath(lk_path_id);
		lkpath.setValid(false);
		lkpath.store();
		
		LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(lr_id);
		lr.setActive(false);
		lr.store();
		
		BatchComponent v1 = new BatchComponent(view_1, activate) ;
		
		batchComponentList.add(v1);
		
		BV.activate(batchComponentList, confirm_activate);
		
		/* clean up, make sure lookup path is active */
		lkpath.setValid(true);
		lkpath.store();
		
		lr.setActive(true);
		lr.store();
		
		View view1_active = SAFRApplication.getSAFRFactory().getView(view_1_id);
		Code view1_active_sc = view1_active.getStatusCode();

		assertEquals(true,view1_active_sc.getDescription().matches("Inactive"));
		
	}
    
    @SuppressWarnings("static-access")
    public void testActivateError() throws SAFRException{
		
		int env_id = 119;
		int view_1_id = 8713;
		
		helper.initDataLayer(env_id);
		
		Boolean activate = true;
		
		BatchActivateViews BV = new BatchActivateViews();
		MockConfirmWarningStrategy confirm_activate = new MockConfirmWarningStrategy(activate);
		
		List<BatchComponent> batchComponentList = new ArrayList<BatchComponent>();
		
		ViewQueryBean view_1 = getViewQB(view_1_id,env_id);
		
		BatchComponent v1 = new BatchComponent(view_1, activate) ;
		
		batchComponentList.add(v1);
	
	    BV.activate(batchComponentList, confirm_activate);
	    
	    ActivityResult v_bv_result = v1.getResult();
	    assertEquals("Fail",v_bv_result.getLabel());
	}
}
