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

import org.junit.Test;

public class TestCopybookReader {

	@Test
	public void testCCB2LR() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/OneLine.cpy");
		ccb2lr.processCopybook(testPath);
		assertEquals("ONE_LINE", ccb2lr.getRecordField().getName());
		assertFalse(ccb2lr.hasErrors());
	}

	@Test
	public void testCCB2LRSimple() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simple.cpy");
		ccb2lr.processCopybook(testPath);
		CCB2Dot.writeFromRecord(ccb2lr.getCobolCollection(), Paths.get("cbrec.gv"));
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(7, ccb2lr.getRecordField().getNumberOfCobolFields());
	}

	@Test
	public void testCCB2LRSimplePackedBinary() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simplePackedBinary.cpy");
		ccb2lr.processCopybook(testPath);
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
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(7, ccb2lr.getRecordField().getNumberOfCobolFields());
		assertEquals(77, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRSimplePackedBinaryLength() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simplePackedBinary.cpy");
		ccb2lr.processCopybook(testPath);
		CCB2Dot.write(ccb2lr.getCobolCollection(), Paths.get("simplePackedBinary.gv"));
		assertEquals("CUSTOMER-RECORD", ccb2lr.getRecordField().getName());
		assertEquals(7, ccb2lr.getRecordField().getNumberOfCobolFields());
		assertEquals(75, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRSimpleSigned() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simpleSigned.cpy");
		ccb2lr.processCopybook(testPath);
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
		CCB2Dot.writeFromRecord(ccb2lr.getCobolCollection(), Paths.get("cbrec.gv"));
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
		CCB2Dot.writeFromRecord(ccb2lr.getCobolCollection(), Paths.get("simple.gv"));
		GroupField rf = ccb2lr.getRecordField();
		int positions [] = {1,16,24,44,61,63,68};
		checkFieldPositions(rf, positions);
		assertEquals(77, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRGoupReslovePositions() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/group.cpy");
		ccb2lr.processCopybook(testPath);
		CCB2Dot.writeFromRecord(ccb2lr.getCobolCollection(), Paths.get("group.gv"));
		GroupField rf = ccb2lr.getRecordField();
		int positions [] = {1,1,16,24,44,61,63,68};
		checkFieldPositions(rf, positions);
		assertEquals(77, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRGoupInGroupReslovePositions() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupInGroup.cpy");
		ccb2lr.processCopybook(testPath);
		CCB2Dot.writeFromRecord(ccb2lr.getCobolCollection(), Paths.get("groupInGroup.gv"));
		GroupField rf = ccb2lr.getRecordField();
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
		assertFalse(ccb2lr.hasErrors());
		CCB2Dot.write(ccb2lr.getCobolCollection(), Paths.get("expandedGroupCustomerArray.gv"));
		assertEquals(192, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRGroupAtTheEnd() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupAtTheEnd.cpy");
		ccb2lr.processCopybook(testPath);
		assertFalse(ccb2lr.hasErrors());
		assertEquals(146, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRGroupOccursAtTheEnd() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupCustomerArrayAtEnd.cpy");
		ccb2lr.processCopybook(testPath);
		CCB2Dot.write(ccb2lr.getCobolCollection(), Paths.get("expandedGroupCustomerArrayAtEnd.gv"));
		assertFalse(ccb2lr.hasErrors());
		assertEquals(192, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRGroupInGroupOccurs() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupInGroupOccurs.cpy");
		ccb2lr.processCopybook(testPath);
		assertFalse(ccb2lr.hasErrors());
		CCB2Dot.write(ccb2lr.getCobolCollection(), Paths.get("expandedgroupInGroupOccurs.gv"));
		assertFalse(ccb2lr.hasErrors());
		assertEquals(274, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRAllContacts() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupAllContacts.cpy");
		ccb2lr.processCopybook(testPath);
		assertFalse(ccb2lr.hasErrors());
		CCB2Dot.write(ccb2lr.getCobolCollection(), Paths.get("expandedgroupAllContacts.gv"));
		assertEquals(274, ccb2lr.getRecordField().getLength());
	}

	@Test
	public void testCCB2LRRedineField() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupRedefineZip.cpy");
		ccb2lr.processCopybook(testPath);
		assertFalse(ccb2lr.hasErrors());
		CCB2Dot.writeFromRecord(ccb2lr.getCobolCollection(), Paths.get("expandedgroupRedefineZip.gv"));
		assertEquals(77, ccb2lr.getRecordField().getLength());
		assertEquals(10,ccb2lr.getNumberOfCobolFields());
		CobolField zc = ccb2lr.getRecordField().getField("ZIP-CODE");
		CobolField zn = ccb2lr.getCobolCollection().getNamedRedefine("ZIP-NAME");
		assertEquals(zn.getPosition(), zc.getPosition());
	}

	@Test
	public void testCCB2LRRedineGroup() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupRedefined.cpy");
		ccb2lr.processCopybook(testPath);
		assertFalse(ccb2lr.hasErrors());
		CCB2Dot.writeFromRecord(ccb2lr.getCobolCollection(), Paths.get("expandedgroupRedefined.gv"));
		assertEquals(77, ccb2lr.getRecordField().getLength());
		assertEquals(12,ccb2lr.getNumberOfCobolFields());
	}

	@Test
	public void testCCB2LRGetDecimals() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/decimals.cpy");
		ccb2lr.processCopybook(testPath);
		assertFalse(ccb2lr.hasErrors());
		CobolField snines = ccb2lr.getRecordField().getField("SNINES-WITH-V");
		assertEquals(5, snines.getNunberOfDecimalPlaces());
		assertEquals(11, snines.getLength());
		CobolField sbracketed = ccb2lr.getRecordField().getField("SBRACKETED-NINES");
		assertEquals(3, sbracketed.getNunberOfDecimalPlaces());
		assertEquals(8, sbracketed.getLength());
		CobolField nines = ccb2lr.getRecordField().getField("NINES-WITH-V");
		assertEquals(5, nines.getNunberOfDecimalPlaces());
		assertEquals(11, nines.getLength());
		CobolField bracketed = ccb2lr.getRecordField().getField("BRACKETED-NINES");
		assertEquals(3, bracketed.getNunberOfDecimalPlaces());
		assertEquals(8, bracketed.getLength());
	}

	@Test
	public void testCCB2LRRedefineMS1() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/COPYBOOK01.CPY");
		ccb2lr.processCopybook(testPath);
		assertFalse(ccb2lr.hasErrors());
		CCB2Dot.writeFromRecord(ccb2lr.getCobolCollection(), Paths.get("expandedCOPYBOOK01.gv"));
		//assertEquals(77, ccb2lr.getRecordField().getLength());
		//assertEquals(12,ccb2lr.getNumberOfCobolFields());
	}

	@Test
	public void testCCB2LRRedefineMS2() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/COPYBOOK02cleaned.CPY");
		ccb2lr.processCopybook(testPath);
		assertFalse(ccb2lr.hasErrors());
		CCB2Dot.writeFromRecord(ccb2lr.getCobolCollection(), Paths.get("expandedgroupRedefineMS2.gv"));
		//assertEquals(77, ccb2lr.getRecordField().getLength());
		//assertEquals(12,ccb2lr.getNumberOfCobolFields());
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
