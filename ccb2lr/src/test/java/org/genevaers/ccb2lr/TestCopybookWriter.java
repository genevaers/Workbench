package org.genevaers.ccb2lr;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.genevaers.ccb2lr.CobolField.FieldType;
import org.junit.Test;

public class TestCopybookWriter {

	@Test
	public void testCCB2LRtoYaml() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simple.cb");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		File out = new File("build/out");
		out.mkdirs();
		ccb2lr.writeYAMLTo("build/out/simplecb.yaml");
		File scbf = new File("build/out/simplecb.yaml");
		assertTrue(scbf.exists());
	}

	@Test
	public void testCCB2LRGroupInGrouptoYaml() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupInGroup.cb");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		File out = new File("build/out");
		out.mkdirs();
		ccb2lr.writeYAMLTo("build/out/groupInGroup.yaml");
		File scbf = new File("build/out/groupInGroup.yaml");
		assertTrue(scbf.exists());
	}


}
