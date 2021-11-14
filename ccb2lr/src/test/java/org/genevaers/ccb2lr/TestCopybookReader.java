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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.junit.Test;

public class TestCopybookReader {

	@Test
	public void testCCB2LR() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/OneLine.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("ONE_LINE", ccb2lr.getRecordField().getName());
		assertFalse(ccb2lr.hasErrors());
	}

	@Test
	public void testCCB2LRSimple() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simple.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(7, ccb2lr.getRecordField().getNumberOfCobolFields());
	}

	@Test
	public void testCCB2LRSimplePackedBinary() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simplePackedBinary.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		GroupField rf = ccb2lr.getRecordField();
		boolean packedFound = false;
		boolean binaryFound = false;
		CobolField n = rf.next();
		while(n != null) {
			if(n.getType() == FieldType.PACKED) {
				packedFound = true;
			}
			if(n.getType() == FieldType.BINARY) {
				binaryFound = true;
			}
			n = n.next();
		}
		assertEquals(7, ccb2lr.getRecordField().getNumberOfCobolFields());
		assertTrue(packedFound);
		assertTrue(binaryFound);
	}

	@Test
	public void testCCB2LRSimpleLength() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simple.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(7, ccb2lr.getRecordField().getNumberOfCobolFields());
		assertEquals(77, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRSimplePackedBinaryLength() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simplePackedBinary.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(7, ccb2lr.getRecordField().getNumberOfCobolFields());
		assertEquals(75, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRSimpleSigned() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simpleSigned.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(8, ccb2lr.getRecordField().getNumberOfCobolFields());
		//maybe we should store the fields in a map then we can get them by name
		assertTrue(ccb2lr.getRecordField().getField("AMOUNT").isSigned());
		assertFalse(ccb2lr.getRecordField().getField("UNSIGN").isSigned());
		assertEquals(82, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRGroup() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/group.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(8, ccb2lr.getRecordField().getNumberOfCobolFields());
		GroupField group = (GroupField) ccb2lr.getRecordField().getField("CUSTOMER-NAME");
		assertEquals(FieldType.GROUP,  group.getType());
		assertEquals(FieldType.ALPHA,  group.getField("LAST-NAME").getType());

		assertEquals(77, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRSimpleReslovePositions() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simple.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		GroupField rf = ccb2lr.getRecordField();
		rf.resolvePositions();
		int positions [] = {1,16,24,44,61,63,68};
		checkFieldPositions(rf, positions);
		assertEquals(77, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRGoupReslovePositions() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/group.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		GroupField rf = ccb2lr.getRecordField();
		rf.resolvePositions();
		int positions [] = {1,1,16,24,44,61,63,68};
		checkFieldPositions(rf, positions);
		assertEquals(77, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRGoupInGroupReslovePositions() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupInGroup.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		GroupField rf = ccb2lr.getRecordField();
		rf.resolvePositions();
		int positions [] = {1,31,31,31,46,54,54,69,77,77,87,102,110,125,140,142};
		checkFieldPositions(rf, positions);
		assertEquals(146, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRRubbishErrors() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/rubbish.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		assertTrue(ccb2lr.hasErrors());
	}

	@Test
	public void testCCB2LRGroupOccurs() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupCustomerArray.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		GroupField rf = ccb2lr.getRecordField();
		rf.resolvePositions();
		assertFalse(ccb2lr.hasErrors());
		assertEquals(192, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRGroupAtTheEnd() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupAtTheEnd.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		GroupField rf = ccb2lr.getRecordField();
		rf.resolvePositions();
		assertFalse(ccb2lr.hasErrors());
		assertEquals(146, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRGroupOccursAtTheEnd() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupCustomerArrayAtEnd.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		GroupField rf = ccb2lr.getRecordField();
		rf.resolvePositions();
		assertFalse(ccb2lr.hasErrors());
		assertEquals(192, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRGroupInGroupOccurs() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupInGroupOccurs.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		GroupField rf = ccb2lr.getRecordField();
		rf.resolvePositions();
		assertFalse(ccb2lr.hasErrors());
		assertEquals(192, ccb2lr.getRecordField().getLength());
	}

	private void checkFieldPositions(GroupField rf, int[] positions) {
		CobolField f = rf.getFirstChild();
		int ndx = 0;
		while(f != null) {
			assertEquals(positions[ndx++], f.getPosition());
			f = f.next();
		}
		assertEquals(positions.length, ndx);
	}

}
