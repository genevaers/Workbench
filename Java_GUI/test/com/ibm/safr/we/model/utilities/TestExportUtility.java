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
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.utilities.export.ExportComponent;
import com.ibm.safr.we.model.utilities.export.ExportUtility;

public class TestExportUtility extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestExportUtility");
	TestDataLayerHelper helper = new TestDataLayerHelper();
	List<ExportComponent> compListPF = new ArrayList<ExportComponent>();
	List<ExportComponent> compListLF = new ArrayList<ExportComponent>();
	List<ExportComponent> compListLR = new ArrayList<ExportComponent>();
	List<ExportComponent> compListLK = new ArrayList<ExportComponent>();
	List<ExportComponent> compListView = new ArrayList<ExportComponent>();

	public void dbStartup() {
		helper.initDataLayer();
	}
	
	public void tearDown() {
		helper.closeDataLayer();
	}

	public void testExport() throws DAOException {
		dbStartup();

		// test an invalid file path (path not found).
		PhysicalFileQueryBean pf = new PhysicalFileQueryBean(1, 8374,
				"testPhysicalFile", null, null, null, null, null, null, null, null, null, null, null);
		PhysicalFileQueryBean pf1 = new PhysicalFileQueryBean(1, 8361, "fdfd", null,
				null, null, null, null, null, null, null, null, null, null);
		ExportComponent component = new ExportComponent(pf);
		ExportComponent component1 = new ExportComponent(pf1);
		compListPF.add(component);
		compListPF.add(component1);
		EnvironmentQueryBean env = new EnvironmentQueryBean(1, "Development",
				true, null, null, null, null);
		ExportUtility exportpf = new ExportUtility(env,
				"C:/Program Files/SAFR/WORKBENCH/%#U*^%#^*","",
				ComponentType.PhysicalFile,false);

		try {
			exportpf.export(compListPF);
			fail("Invalid file path did not throw SAFRValidationException as expected.");
		} catch (SAFRValidationException e) {
			// no op, exception expected
		}

		// test when environment is null.
		ExportUtility exportpf2 = new ExportUtility(null,
				"C:/Program Files/SAFR/WORKBENCH", "", ComponentType.PhysicalFile,false);
		try {
			exportpf2.export(compListPF);
			fail("Null environment did not throw SAFRValidationException as expected.");
		} catch (SAFRValidationException e) {
			// no op, exception expected
		}

		// test when environment id is 0.
		EnvironmentQueryBean env1 = new EnvironmentQueryBean(0, "Dummy",
				true, null, null, null, null);
		ExportUtility exportpf3 = new ExportUtility(env1,
				"C:/Program Files/SAFR/WORKBENCH", "", ComponentType.PhysicalFile,false);
		try {
			exportpf3.export(compListPF);
			fail("Zero environid did not throw SAFRValidationException as expected.");
		} catch (SAFRValidationException e) {
			// no op, exception expected
		}

		// when component type is set as null.
		ExportUtility exportpf4 = new ExportUtility(env,
				"C:/Program Files/SAFR/WORKBENCH", "", null,false);
		try {
			exportpf4.export(compListPF);
			fail("Null component type did not throw SAFRValidationException as expected.");
		} catch (SAFRValidationException e) {
			// no op, exception expected
		}

		// test valid Physical File export.
		ExportUtility exportpf1 = new ExportUtility(env,
				System.getProperty("java.io.tmpdir"), "phyicialfile.xml", ComponentType.PhysicalFile,false);

		try {
			exportpf1.export(compListPF);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception on valid PF export.");
		}

		// test valid Logical File export.
		LogicalFileQueryBean lf = new LogicalFileQueryBean(1, 1288, "ss", null, null, null, null, null);
		ExportComponent componentLF = new ExportComponent(lf);
		ExportUtility exportlf = new ExportUtility(env,
				System.getProperty("java.io.tmpdir"), "logicalfile.xml", ComponentType.LogicalFile,false);

		compListLF.clear(); // make sure list is empty
		compListLF.add(componentLF);
		try {
			exportlf.export(compListLF);
		} catch (Exception e) {
			fail("Unexpected exception on valid LF export.");
		}

		// test valid Logical Record export
		LogicalRecordQueryBean lr = new LogicalRecordQueryBean(1, 1288,
				"test_LogicalRecord123", "Active", null, null, null, null, null,
				null, null, null, null, null);
		LogicalRecordQueryBean lr1 = new LogicalRecordQueryBean(1, 1212,
				"SS_MANAGER_ZONED", "Active", null, null, null, null, null, null,
				null, null, null, null);
		ExportComponent componentLR = new ExportComponent(lr);
		ExportComponent componentLR1 = new ExportComponent(lr1);
		ExportUtility exportlr = new ExportUtility(env,
				System.getProperty("java.io.tmpdir"), "logicalrecord.xml", ComponentType.LogicalRecord,false);
		compListLR.add(componentLR);
		compListLR.add(componentLR1);

		try {
			exportlr.export(compListLR);
		} catch (Exception e) {
			fail("Unexpected exception on valid LR export.");
		}

		// test valid Lookup Path export
		LookupQueryBean lookup = new LookupQueryBean(1, 1997, "SALES", null, 0, 0, null, null,
				null, null, null, null, null, null, null);
		ExportComponent componentLK = new ExportComponent(lookup);
		ExportUtility exportlk = new ExportUtility(env,
				System.getProperty("java.io.tmpdir"),"SALES.xml", ComponentType.LookupPath,false);
		compListLK.add(componentLK);

		try {
			exportlk.export(compListLK);
		} catch (SAFRValidationException e) {
			fail("Unexpected exception on valid LK export.");
		}

		// test valid View export
		ViewQueryBean view = new ViewQueryBean(1, 2498, "Excercise#8",
				"inactive", "file", "Extract", null, null, null,
				null, null, null, null, null);
		ExportComponent componentView = new ExportComponent(view);
		ExportUtility exportView = new ExportUtility(env,
				System.getProperty("java.io.tmpdir"),"Excercise#8", ComponentType.View,false);
		compListView.add(componentView);

		try {
			exportView.export(compListView);
		} catch (SAFRValidationException e) {
			fail("Unexpected exception on valid View export.");
		}

	}

}
