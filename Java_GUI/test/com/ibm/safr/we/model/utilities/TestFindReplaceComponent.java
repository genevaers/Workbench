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


import junit.framework.TestCase;

import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.TestDataLayerHelper;

public class TestFindReplaceComponent extends TestCase {
	protected TestDataLayerHelper helper = new TestDataLayerHelper();
	   
	public void setUp(){
		
		 helper.initDataLayer();
	}
	
	public void tearDown(){
		
		helper.closeDataLayer();
	}
	
	public void testGetSetViewId(){
		
		Integer viewId = 0;
		Integer viewID_set = 1;
		String viewName = "";
		LogicTextType logicTextType = null;
		String logicText = "";
		Integer parentId = 0;
		
		FindReplaceComponent frcomp = new FindReplaceComponent(
		    viewId, viewName, logicTextType, logicText, parentId, 0,EditRights.ReadModifyDelete);
		
		assertEquals(viewId,frcomp.getViewId());
		
		frcomp.setViewId(viewID_set);
		
		assertEquals(viewID_set,frcomp.getViewId());	
		
	}
	
	public void testGetSetViewName(){
		
		Integer viewId = 0;
		String viewName_set = "test";
		String viewName = "";
		LogicTextType logicTextType = null;
		String logicText = "";
		Integer parentId = 0;
		
		FindReplaceComponent frcomp = new FindReplaceComponent(
            viewId, viewName, logicTextType, logicText, parentId, 0,EditRights.ReadModifyDelete);
		
		assertEquals(viewName,frcomp.getViewName());
		
		frcomp.setViewName(viewName_set);
		
		assertEquals(viewName_set,frcomp.getViewName());	
		
	}
	
	public void testGetSetLogicTextType(){
		
		Integer viewId = 0;
		String viewName = "";
		LogicTextType logicTextType = LogicTextType.Format_Column_Calculation;
		LogicTextType logicTextType_set = LogicTextType.Extract_Record_Filter;
		String logicText = "";
		Integer parentId = 0;
		
		FindReplaceComponent frcomp = new FindReplaceComponent(
            viewId, viewName, logicTextType, logicText, parentId, 0,EditRights.ReadModifyDelete);
		
		assertEquals(logicTextType,frcomp.getLogicTextType());
		
		frcomp.setLogicTextType(logicTextType_set);
		
		assertEquals(logicTextType_set,frcomp.getLogicTextType());	
		
	}
	
	public void testGetSetLogicText(){
		
		Integer viewId = 0;
		String viewName = "";
		LogicTextType logicTextType = LogicTextType.Format_Column_Calculation;
		String logicText = "";
		String logicText_set = "test";
		Integer parentId = 0;
		
		FindReplaceComponent frcomp = new FindReplaceComponent(
		    viewId, viewName, logicTextType, logicText, parentId, 0,EditRights.ReadModifyDelete);
		
		assertEquals(logicText,frcomp.getLogicText());
		
		frcomp.setLogicText(logicText_set);
		
		assertEquals(logicText_set,frcomp.getLogicText());	
		
	}
	
	public void testGetSetParentId(){
		
		Integer viewId = 0;
		String viewName = "";
		LogicTextType logicTextType = LogicTextType.Format_Column_Calculation;
		String logicText = "";
		Integer parentId = 0;
		Integer parentId_set = 1;
		
		FindReplaceComponent frcomp = new FindReplaceComponent(
		    viewId, viewName, logicTextType, logicText, parentId, 0,EditRights.ReadModifyDelete);
		
		assertEquals(parentId,frcomp.getParentId());
		
		frcomp.setParentId(parentId_set);
		
		assertEquals(parentId_set,frcomp.getParentId());	
		
	}
	
	public void testIsSetSelected(){
		
		Integer viewId = 0;
		String viewName = "";
		LogicTextType logicTextType = LogicTextType.Format_Column_Calculation;
		String logicText = "";
		Integer parentId = 0;
		Boolean selected = true;
		
		FindReplaceComponent frcomp = new FindReplaceComponent(
		    viewId, viewName, logicTextType, logicText, parentId, 0,EditRights.ReadModifyDelete);
		
		frcomp.setSelected(selected);
		
		assertEquals(selected,frcomp.isSelected());
		
	}
}
