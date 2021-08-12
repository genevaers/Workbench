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

import junit.framework.TestCase;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;

public class TestDependencyChecker extends TestCase {
	
	protected TestDataLayerHelper helper = new TestDataLayerHelper();
	   
	public void setUp(){
		
		 helper.initDataLayer();
	}
	
	public void tearDown(){
		
		helper.closeDataLayer();
	}
	
	public void testGetDependency() throws DAOException{
		
		int env_id =118;
		String env_name = "DependencyChecker";
		int component_id = 8697;
		ComponentType v = ComponentType.View;
		String comp_name = "Test";
		Boolean direct_deps = false;
		
		DependencyChecker dc = new DependencyChecker(env_id, env_name, component_id, v, comp_name, direct_deps);

		DependencyCheckerNode dc_node = dc.getDependency();
		
		assertEquals("Test",dc_node.getName());
		}
	
	
	
	public void testGetSetEnvironmentID(){
	
	int env_get =118;
	int env_set = 115;
	String env_name = "DependencyChecker";
	int component_id = 0;
	ComponentType v = ComponentType.View;
	String comp_name = "NA";
	Boolean direct_deps = false;
	
	DependencyChecker dc = new DependencyChecker(env_get, env_name, component_id, v, comp_name, direct_deps);

	assertEquals(env_get, dc.getEnvironmentId());

	dc.setEnvironmentId(env_set);
	
	assertEquals(env_set, dc.getEnvironmentId());
	
	}
	
	public void testGetEnvironmentName(){
		
		int env_id =118;
		String env_name = "DependencyChecker";
		int component_id = 0;
		ComponentType v = ComponentType.View;
		String comp_name = "NA";
		Boolean direct_deps = false;
		
		DependencyChecker dc = new DependencyChecker(env_id, env_name, component_id, v, comp_name, direct_deps);

		assertEquals(env_name, dc.getEnvironmentName());
		
		}
	
	public void testGetSetComponentID(){
		
		int env_id =118;
		String env_name = "DependencyChecker";
		
		int component_id_get = 0;
		int component_id_set = 8697;
		
		ComponentType v = ComponentType.View;
		String comp_name = "Test";
		Boolean direct_deps = false;
		
		DependencyChecker dc = new DependencyChecker(env_id, env_name, component_id_get, v, comp_name, direct_deps);

		assertEquals(component_id_get, dc.getComponentId());

		dc.setComponentId(component_id_set);
		
		assertEquals(component_id_set, dc.getComponentId());
		
		}
	
	public void testGetSetComponentType(){
		
		int env_id =118;
		String env_name = "DependencyChecker";

		int component_id = 8697;
		
		ComponentType v = ComponentType.View;
		ComponentType pf = ComponentType.PhysicalFile;
		
		String comp_name = "Test";
		Boolean direct_deps = false;
		
		DependencyChecker dc = new DependencyChecker(env_id, env_name, component_id, v, comp_name, direct_deps);

		assertEquals(v, dc.getComponentType());

		dc.setComponentType(pf);
		
		assertEquals(pf, dc.getComponentType());
		
		}
	
	public void testGetSetComponentName(){
		
		int env_id =118;
		String env_name = "DependencyChecker";

		int component_id = 8697;
		
		ComponentType v = ComponentType.View;
		
		String comp_name_get = "Test";
		String comp_name_set = "Test2";
		Boolean direct_deps = false;
		
		DependencyChecker dc = new DependencyChecker(env_id, env_name, component_id, v, comp_name_get, direct_deps);

		assertEquals(comp_name_get, dc.getComponentName());

		dc.setComponentName(comp_name_set);
		
		assertEquals(comp_name_set, dc.getComponentName());
		
		}

	
	public void testDepencyNullComponent() throws DAOException {

		int env_id =118;
		String env_name = "DependencyChecker";
		int component_id = 1360;
		ComponentType lr = ComponentType.LogicalRecord;
		String comp_name = "srcLR";
		Boolean direct_deps = true;
		
		DependencyChecker dc = new DependencyChecker(env_id, env_name, component_id, lr, comp_name, direct_deps);

		DependencyCheckerNode dc_node = dc.getDependency();
		
		List<DependencyCheckerNode> lr_node_list = dc_node.getChildNodes();
		DependencyCheckerNode lr_node_child = lr_node_list.get(0);
		List<DependencyCheckerNode> lr_node_child_list = lr_node_child.getChildNodes();
		DependencyCheckerNode lr_field_node = lr_node_child_list.get(0);
		
		try {
	        dc.getDependency(lr_field_node);
        } catch (IllegalArgumentException e) {
	        String msg = e.toString();
	        assertEquals(msg,"java.lang.IllegalArgumentException: The node passed in as a parameter should represent a component");
        }	
	}
	
	public void testGetDependencyDep() throws DAOException {

		int env_id =118;
		String env_name = "DependencyChecker";
		int component_id = 1360;
		ComponentType lr = ComponentType.LogicalRecord;
		String comp_name = "srcLR";
		Boolean direct_deps = true;
		
		DependencyChecker dc = new DependencyChecker(env_id, env_name, component_id, lr, comp_name, direct_deps);

		DependencyCheckerNode dc_node = dc.getDependency();
		
		
		List<DependencyCheckerNode> lr_node_list = dc_node.getChildNodes();
		DependencyCheckerNode lr_node_child = lr_node_list.get(0);
		List<DependencyCheckerNode> lr_node_child_list = lr_node_child.getChildNodes();
		DependencyCheckerNode lf_node = lr_node_child_list.get(1);
		List<DependencyCheckerNode> lf_node_list = lf_node.getChildNodes();
		DependencyCheckerNode lf_checker_node = lf_node_list.get(0);

	     DependencyCheckerNode lf_srcLF_node = dc.getDependency(lf_checker_node);
	     String lf_name = lf_srcLF_node.getName();
	     assertEquals(lf_name,"srcLF");
	     
	     String lf_label = lf_srcLF_node.getLabel();
	     assertEquals(lf_label,"srcLF [1357]");
	}
}
