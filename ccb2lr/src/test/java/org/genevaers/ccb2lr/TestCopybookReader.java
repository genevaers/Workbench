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


import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.genevaers.ccb2lr.Copybook2LR;
import org.genevaers.ccb2lr.CobolField.FieldType;

public class TestCopybookReader {

	@Test
	public void testCCB2LR() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/OneLine.cb");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("ONE_LINE", ccb2lr.getRecordField().getName());
		assertFalse(ccb2lr.hasErrors());
	}

	@Test
	public void testCCB2LRSimple() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simple.cb");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(7, ccb2lr.getRecordField().getFields().size());
	}

	@Test
	public void testCCB2LRSimplePackedBinary() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simplePackedBinary.cb");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		Iterator<CobolField> fit = ccb2lr.getRecordField().getFieldIterator();
		boolean packedFound = false;
		boolean binaryFound = false;
		while(fit.hasNext()) {
			CobolField cb = fit.next();
			if(cb.getType() == FieldType.PACKED) {
				packedFound = true;
			}
			if(cb.getType() == FieldType.BINARY) {
				binaryFound = true;
			}
		}
		assertEquals(7, ccb2lr.getRecordField().getFields().size());
		assertTrue(packedFound);
		assertTrue(binaryFound);
	}

	@Test
	public void testCCB2LRSimpleLength() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simple.cb");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(7, ccb2lr.getRecordField().getFields().size());
		assertEquals(77, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRSimplePackedBinaryLength() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simplePackedBinary.cb");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(7, ccb2lr.getRecordField().getFields().size());
		assertEquals(75, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRSimpleSigned() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simpleSigned.cb");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(8, ccb2lr.getRecordField().getFields().size());
		//maybe we should store the fields in a map then we can get them by name
		assertTrue(ccb2lr.getRecordField().getField("AMOUNT").isSigned());
		assertFalse(ccb2lr.getRecordField().getField("UNSIGN").isSigned());
		assertEquals(82, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRGroup() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/group.cb");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(8, ccb2lr.getRecordField().getNumberOfFields());
		GroupField group = (GroupField) ccb2lr.getRecordField().getField("CUSTOMER-NAME");
		assertEquals(FieldType.GROUP,  group.getType());
		assertEquals(FieldType.ALPHA,  group.getField("LAST-NAME").getType());

		assertEquals(77, ccb2lr.getRecordField().getLength());
	}

}
