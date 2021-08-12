package org.genevaers.sycadas;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.genevaers.sycadas.dataprovider.SycadaDataProvider;
import org.junit.Test;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class TestExtractColumnSycada {

	@Test
	public void testGetSycada() throws IOException {
		SyntaxChecker ecs = SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
		assertTrue(ecs instanceof ExtractColumnSycada);
		SyntaxChecker efs = SycadaFactory.getProcesorFor(SycadaType.EXTRACT_FILTER);
		assertNotNull(ecs);
		assertTrue(efs instanceof ExtractFilterSycada);
		SyntaxChecker eos = SycadaFactory.getProcesorFor(SycadaType.EXTRACT_OUTPUT);
		assertNotNull(ecs);
		assertTrue(eos instanceof ExtractOutputSycada);
		SyntaxChecker fcs = SycadaFactory.getProcesorFor(SycadaType.FORMAT_CALCULATION);
		assertNotNull(ecs);
		assertTrue(fcs instanceof FormatCalculationSyntaxChecker);
		SyntaxChecker ffs = SycadaFactory.getProcesorFor(SycadaType.FORMAT_FILTER);
		assertNotNull(ecs);
		assertTrue(ffs instanceof FormatFilterSyntaxChecker);
		ecs.processLogic("Hey diddle diddle");
	}

	@Test
	public void testExtractColumnErrorDetection() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
		ecs.processLogic("Hey diddle diddle");
		assertEquals(1, ecs.getSyntaxErrors().size());
		ParseTree pt = ecs.getParseTree();
		assertNotNull(pt);		
	}

	@Test
    public void testFieldNotFound() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
		ecs.processLogic("COLUMN = {NoFindMe}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertTrue(ecs.hasDataErrors());
    }

	@Test
    public void testFieldFound() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
		ecs.processLogic("COLUMN = {FindMe}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertFalse(ecs.hasDataErrors());
		Set<Integer> fs = ecs.getFieldIDs();
		assertTrue(fs.contains(99));
    }

	@Test
    public void testFieldWithUnderscoreFound() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
		ecs.processLogic("COLUMN = {Find_Me}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertTrue(ecs.hasDataErrors());
    }

	@Test
    public void testLookupNotFound() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
		ecs.processLogic("COLUMN = {DeadLookup.field}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertTrue(ecs.hasDataErrors());
    }


	@Test
    public void testIsfoundFound() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
		ecs.processLogic("IF ISFOUND({FindLookup}) THEN COLUMN = {FindLookup.lkfield} ELSE COLUMN = 0  ENDIF");
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertFalse(ecs.hasDataErrors());
		Map<Integer, List<Integer>> lks = ecs.getLookupIDs();
		assertTrue(lks.containsKey(123));
	}

	@Test
    public void testIsfoundNotFound() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
		ecs.processLogic("IF ISFOUND({NotFindLookup}) THEN COLUMN = 0  ENDIF");
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertTrue(ecs.hasDataErrors());
		assertEquals("Lookup {NotFindLookup} not found", ecs.getDataErrors().get(0));
	}

	@Test
	public void testLookupRefWithEffDt() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
		ecs.processLogic("IF ISFOUND({FindLookup, {FindMe}}) THEN COLUMN = {FindLookup.lkfield} ELSE COLUMN = 0  ENDIF");
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertFalse(ecs.hasDataErrors());
	}

	@Test
    public void testLookupFound() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
		ecs.processLogic("COLUMN = {FindLookup.lkfield}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertFalse(ecs.hasDataErrors());
		Map<Integer, List<Integer>> lks = ecs.getLookupIDs();
		assertNotNull(lks.get(123));
		Integer xlf = 321;
		assertEquals(xlf, lks.get(123).get(0));
    }

	@Test
    public void testLookupfieldNotFound() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
		ecs.processLogic("COLUMN = {FindLookup.garbage}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertTrue(ecs.hasDataErrors());
		Map<Integer, List<Integer>> lks = ecs.getLookupIDs();
		assertNotNull(lks.get(123));
		assertEquals(0, lks.get(123).size());
    }

	@Test
    public void testWritExit() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
        String inputString = "WRITE ( SOURCE=INPUT, DEST=EXTRACT=9, PROCEDURE={wrexit} )";
		ecs.processLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertFalse(ecs.hasDataErrors());
		Set<Integer> fs = ecs.getExitIDs();
		assertTrue(fs.contains(60));
    }
	
	@Test
    public void testNewLine() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
        String inputString = "COLUMN = {FindMe}\n";
		ecs.processLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertFalse(ecs.hasDataErrors());
    }
	
	@Test
    public void testWriteExitNotFound() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
        String inputString = "WRITE ( SOURCE=INPUT, DEST=EXTRACT=9, PROCEDURE={notMe} )";
		ecs.processLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertTrue(ecs.hasDataErrors());
		Set<Integer> fs = ecs.getExitIDs();
		assertEquals(0, fs.size());
    }
	
	@Test
    public void testWriteExit() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
        String inputString = "WRITE ( SOURCE=INPUT, DEST=EXTRACT=9, PROCEDURE={wrexit} )";
		ecs.processLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertFalse(ecs.hasDataErrors());
		Set<Integer> fs = ecs.getExitIDs();
		assertTrue(fs.contains(60));
    }
	
	@Test
    public void testWriteToFile() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
        String inputString = "WRITE ( SOURCE=INPUT, DEST=FILE={LFname.PFname} )";
		ecs.processLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertFalse(ecs.hasDataErrors());
		Set<Integer> lfpfs = ecs.getLFPFAssocIDs();
		assertTrue(lfpfs.contains(199));
    }
	
	@Test
    public void testLookupFldSym() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
        String inputString = "COLUMN = {FindLookup.lkfield;$SRC_CD=\"E\"}";
		ecs.processLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertFalse(ecs.hasDataErrors());
    }
	
	@Test
    public void testPriorFld() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
        String inputString = "COLUMN =   PRIOR({FindMe})";
		ecs.processLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertFalse(ecs.hasDataErrors());
		Set<Integer> fs = ecs.getFieldIDs();
		assertTrue(fs.contains(99));
    }

	@Test
    public void testCastFld() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcesorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
        String inputString = "COLUMN =   <ZONED> {FindMe}";
		ecs.processLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencyDataFrom(dataProvider);
		assertFalse(ecs.hasDataErrors());
		Set<Integer> fs = ecs.getFieldIDs();
		assertTrue(fs.contains(99));
    }
}
