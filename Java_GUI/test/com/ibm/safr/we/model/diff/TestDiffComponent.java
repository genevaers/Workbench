package com.ibm.safr.we.model.diff;

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

import com.ibm.safr.we.exceptions.SAFRException;

public class TestDiffComponent extends TestCase {

	public void testDiffComponent() throws SAFRException {
/*		MockDiffComponent comp = new MockDiffComponent();
		DiffNode root = comp.generateWholeTree();
		DiffNodeComp testLF = new DiffNodeComp();
		testLF.setName("Logical File");
		testLF.setId(23);
		testLF.setState(DiffNodeCompState.Same);
		testLF.addStringField("Name", "TestLF", "TestLF");
		comp.addMetadataNode(MetaType.LOGICAL_FILES, testLF);
		
		DiffNodeComp testCR = new DiffNodeComp();
		testCR.setName("Control Record");
		testCR.setId(12);
		testCR.setState(DiffNodeCompState.Same);
		testCR.addStringField("Name", "TestCR", "TestCR");
		comp.addMetadataNode(MetaType.CONTROL_RECORDS, testCR);

		DiffNodeComp testLR = new DiffNodeComp();
		testLR.setName("Logical Record");
		testLR.setId(1256);
		testLR.setState(DiffNodeCompState.Same);
		testLR.addStringField("Name", "TestLR", "TestLR");
		comp.addMetadataNode(MetaType.LOGICAL_RECORDS, testLR);

		DiffNodeComp testPF = new DiffNodeComp();
		testPF.setName("Physical File");
		testPF.setId(123455);
		testPF.setState(DiffNodeCompState.Same);
		testPF.addStringField("Name", "TestPF", "TestPF");
		comp.addMetadataNode(MetaType.PHYSICAL_FILES, testPF);
			
		String str = root.dumper();
		System.out.print(str);
		
		assertEquals(str, "Difference" + SAFRUtilities.LINEBREAK
				+ " Metadata" + SAFRUtilities.LINEBREAK
				+ "  Control Records" + SAFRUtilities.LINEBREAK
				+ "   Control Record (12) Same" + SAFRUtilities.LINEBREAK
				+ "    Name: TestCR" + SAFRUtilities.LINEBREAK
				+ "  Physical Files" + SAFRUtilities.LINEBREAK
				+ "   Physical File (123455) Same" + SAFRUtilities.LINEBREAK
				+ "    Name: TestPF" + SAFRUtilities.LINEBREAK
				+ "  Logical Files" + SAFRUtilities.LINEBREAK
				+ "   Logical File (23) Same" + SAFRUtilities.LINEBREAK
				+ "    Name: TestLF" + SAFRUtilities.LINEBREAK
				+ "  Logical Records" + SAFRUtilities.LINEBREAK
				+ "   Logical Record (1256) Same" + SAFRUtilities.LINEBREAK
				+ "    Name: TestLR" + SAFRUtilities.LINEBREAK);*/
	}
	
}
