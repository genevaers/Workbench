package org.genevaers.sycadas;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;
import org.genevaers.sycadas.dataprovider.DummyDataProvider;
import org.genevaers.sycadas.dataprovider.SycadaDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestExtractColumnSycada {


	@BeforeEach
	public void setup() {
		ExtractDependencyCache.clear();
   	}

	@Test
	public void testGetSycada() throws IOException {
		SyntaxChecker ecs = SycadaFactory.getProcessorFor(SycadaType.EXTRACT_COLUMN);
		assertNotNull(ecs);
		assertTrue(ecs instanceof ExtractColumnSycada);
		SyntaxChecker efs = SycadaFactory.getProcessorFor(SycadaType.EXTRACT_FILTER);
		assertNotNull(ecs);
		assertTrue(efs instanceof ExtractFilterSycada);
		SyntaxChecker eos = SycadaFactory.getProcessorFor(SycadaType.EXTRACT_OUTPUT);
		assertNotNull(ecs);
		assertTrue(eos instanceof ExtractOutputSycada);
		SyntaxChecker fcs = SycadaFactory.getProcessorFor(SycadaType.FORMAT_CALCULATION);
		assertNotNull(ecs);
		assertTrue(fcs instanceof FormatCalculationSyntaxChecker);
		SyntaxChecker ffs = SycadaFactory.getProcessorFor(SycadaType.FORMAT_FILTER);
		assertNotNull(ecs);
		assertTrue(ffs instanceof FormatFilterSyntaxChecker);
	}

	@Test
	public void testExtractColumnErrorDetection() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcessorFor(SycadaType.EXTRACT_COLUMN);

		assertNotNull(ecs);
		ecs.syntaxCheckLogic("Hey diddle diddle");
		assertEquals(1, ecs.getSyntaxErrors().size());
		ParseTree pt = ecs.getParseTree();
		assertNotNull(pt);		
	}

	@Test
    public void testFieldNotFound() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
		ecs.getFieldsForSourceLr(19);
		ecs.syntaxCheckLogic("COLUMN = {NoFindMe}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();
		assertTrue(ecs.hasDataErrors());
    }

	@Test
    public void testFieldFound() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
		ecs.syntaxCheckLogic("COLUMN = {FindMe}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
		assertNotNull(ecs.getFieldIDs().filter(f -> f == 99 ).findAny().orElse(null));
    }

	@Test
    public void testFieldWithUnderscoreFound() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
		ecs.syntaxCheckLogic("COLUMN = {Find_Me}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();
		assertTrue(ecs.hasDataErrors());
    }

	@Test
    public void testLookupNotFound() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
		ecs.syntaxCheckLogic("COLUMN = {DeadLookup.field}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();
		assertTrue(ecs.hasDataErrors());
    }

	@Test
    public void testIsfoundFound() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
		ecs.syntaxCheckLogic("IF ISFOUND({FindLookup}) THEN COLUMN = {FindLookup.lkfield} ELSE COLUMN = 0  ENDIF");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
		LookupRef lkref = ecs.getLookupsStream().filter(lref -> lref.getId() == 123).findAny().orElse(null);
		assertNotNull(lkref);
		Integer xlf = 321;
		assertEquals(xlf, lkref.getField("lkfield"));
	}

	@Test
    public void testIsfoundNotFound() throws IOException {
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcessorFor(SycadaType.EXTRACT_COLUMN);

		assertNotNull(ecs);
		ecs.syntaxCheckLogic("IF ISFOUND({NotFindLookup}) THEN COLUMN = 0  ENDIF");
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertTrue(ecs.hasDataErrors());
		assertEquals("Lookup NotFindLookup not found", ecs.getDataErrors().get(0));
	}

	@Test
	public void testLookupRefWithEffDt() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
		ecs.syntaxCheckLogic("IF ISFOUND({FindLookup, {FindMe}}) THEN COLUMN = {FindLookup.lkfield} ELSE COLUMN = 0  ENDIF");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
	}

	@Test
    public void testLookupFound() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
		ecs.syntaxCheckLogic("COLUMN = {FindLookup.lkfield}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
		LookupRef lkref = ecs.getLookupsStream().filter(lref -> lref.getId() == 123).findAny().orElse(null);
		assertNotNull(lkref);
		Integer xlf = 321;
		assertEquals(xlf, lkref.getField("lkfield"));
    }

	@Test
    public void testLookupRundate() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
		ecs.syntaxCheckLogic("COLUMN = {FindLookup.lkfield, FISCALDAY()}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
		LookupRef lkref = ecs.getLookupsStream().filter(lref -> lref.getId() == 123).findAny().orElse(null);
		assertNotNull(lkref);
		Integer xlf = 321;
		assertEquals(xlf, lkref.getField("lkfield"));
    }

	@Test
    public void testLookupFoundSymbol() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
		ecs.syntaxCheckLogic("IF ISFOUND({FDWF_EC959_Mrkt_Unit_Eff_DT_Lkup_From_LB587,FISCALDAY()}) THEN\r\n" + //
				"   COLUMN = {FDWF_EC959_Mrkt_Unit_Eff_DT_Lkup_From_LB587.MRKT_UNIT_BUSN_ID,FISCALDAY()}\r\n" + //
				"ELSE\r\n" + //
				"   IF ISFOUND({FDWF_EC959_Mrkt_Unit_Eff_DT_Lkup_From_LB587,FISCALDAY();$SRC_AGRE_ROLE_IND=\"A\"}) THEN \r\n" + //
				"     COLUMN = {FDWF_EC959_Mrkt_Unit_Eff_DT_Lkup_From_LB587.MRKT_UNIT_BUSN_ID,FISCALDAY();$SRC_AGRE_ROLE_IND=\"A\"}\r\n" + //
				"   ELSE\r\n" + //
				"     COLUMN = 0\r\n" + //
				"ENDIF\r\n" + //
				"ENDIF\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"");
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
		LookupRef lkref = ecs.getLookupsStream().filter(lref -> lref.getId() == 333).findAny().orElse(null);
		assertNotNull(lkref);
		Integer xlf = 777;
		assertEquals(xlf, lkref.getField("MRKT_UNIT_BUSN_ID"));
    }

	@Test
    public void testLookupfieldNotFound() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
		ecs.syntaxCheckLogic("COLUMN = {FindLookup.garbage}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertTrue(ecs.hasDataErrors());
		LookupRef lkref = ecs.getLookupsStream().filter(lref -> lref.getId() == 123).findAny().orElse(null);
		assertNotNull(lkref);
		assertNull(lkref.getField("garbage"));
    }

	@Test
    public void testWritExit() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "WRITE ( SOURCE=INPUT, DEST=EXTRACT=9, PROCEDURE={wrexit} )";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertTrue(ecs.hasDataErrors());
    }
	
	@Test
    public void testNewLine() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "COLUMN = {FindMe}\n";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
    }
	
	@Test
    public void testWriteExitNotFound() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "WRITE ( SOURCE=INPUT, DEST=EXTRACT=9, PROCEDURE={notMe} )";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertTrue(ecs.hasDataErrors());
		Set<Integer> fs = ecs.getExitIDs();
		assertEquals(0, fs.size());
    }
	
	@Test
    public void testWriteExit() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "WRITE ( SOURCE=INPUT, DEST=EXTRACT=9, USEREXIT={wrexit} )";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
		Set<Integer> fs = ecs.getExitIDs();
		assertTrue(fs.contains(60));
    }
	
	@Test
    public void testWriteProc() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "WRITE ( SOURCE=INPUT, DEST=EXTRACT=9, PROC={wrproc} )";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
		Set<Integer> fs = ecs.getExitIDs();
		assertTrue(fs.contains(33));
    }
	
	@Test
    public void testWriteProcedure() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "WRITE ( SOURCE=INPUT, DEST=EXTRACT=9, PROCEDURE={wrproc} )";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
		Set<Integer> fs = ecs.getExitIDs();
		assertTrue(fs.contains(33));
    }
	
	@Test
    public void testWriteToFile() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "WRITE ( SOURCE=INPUT, DEST=FILE={LFname.PFname} )";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
		assertNotNull(ecs.getLFPFAssocIDs().filter(assid -> assid == 199).findAny().orElse(null));
    }
	
	@Test
    public void testLookupFldSym() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "COLUMN = {FindLookup.lkfield;$SRC_CD=\"E\"}";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
    }
	
	@Test
    public void testPriorFld() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "COLUMN =   PRIOR({FindMe})";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
		assertNotNull(ecs.getFieldIDs().filter(f -> f == 99 ).findAny().orElse(null));
    }

	@Test
    public void testCastFld() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "COLUMN =   <ZONED> {FindMe}";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());
		assertNotNull(ecs.getFieldIDs().filter(f -> f == 99 ).findAny().orElse(null));
    }

	@Test
    public void testSelectIF() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "SELECTIF({FindMe} > 0)";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertTrue(ecs.hasDataErrors());
		assertTrue(ecs.getDataErrors().get(0).contains("SELECTIF"));
    }

	@Test
    public void testSkipIF() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "SKIPIF({FindMe} > 0)";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertTrue(ecs.hasDataErrors());
		assertTrue(ecs.getDataErrors().get(0).contains("SKIP"));
    }

	@Test
    public void testRubbish() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "this is gobble de gook";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(1, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();
		assertTrue(ecs.hasSyntaxErrors());
		assertTrue(ecs.getSyntaxErrors().get(0).startsWith("Invalid"));
    }

	@Test
    public void testHalfRubbish() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "COLUMN = gobble de gook";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(1, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();
		assertTrue(ecs.hasSyntaxErrors());
		assertTrue(ecs.getSyntaxErrors().get(0).contains("Invalid"));
    }

	@Test
    public void testAccumulate() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "COLUMN =   <ZONED> {FindMe}";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		SycadaDataProvider dataProvider = new DummyDataProvider();
		ecs.generateDependencies();

		String inputStringE = "COLUMN = gobble de gook";
		ecs.syntaxCheckLogic(inputStringE);
		assertEquals(1, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();

		String inputString2 = "COLUMN =   {Another}";
		ecs.syntaxCheckLogic(inputString2);
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();

		ecs.syntaxCheckLogic("COLUMN = {FindLookup.lkfield}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();

        String inputStringPF = "WRITE ( SOURCE=INPUT, DEST=FILE={LFname.PFname} )";
		ecs.syntaxCheckLogic(inputStringPF);
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();

		String inputString3 = "WRITE ( SOURCE=INPUT, DEST=EXTRACT=9, PROCEDURE={wrproc} )";
		ecs.syntaxCheckLogic(inputString3);
		ecs.generateDependencies();

		assertFalse(ecs.hasDataErrors());
		assertNotNull(ecs.getFieldIDs().filter(f -> f == 99 ).findAny().orElse(null));
		assertNotNull(ecs.getFieldIDs().filter(f -> f == 25 ).findAny().orElse(null));
		Set<Integer> fs = ecs.getExitIDs();
		assertTrue(fs.contains(33));
    }

	@Test
    public void testAccumulateTwiceWithClearErrors() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
		ecs.syntaxCheckLogic("COLUMN =   <ZONED> {FindMe}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();

		ecs.syntaxCheckLogic("COLUMN = gobble de gook");
		assertEquals(1, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();

		ecs.syntaxCheckLogic("COLUMN = {No_Find_Me}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();
		assertTrue(ecs.hasDataErrors());
		
		ecs.syntaxCheckLogic("COLUMN =   {Another}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();
		ecs.clearErrors();

		ecs.syntaxCheckLogic("COLUMN = {FindLookup.lkfield}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();
		assertFalse(ecs.hasDataErrors());

		ecs.syntaxCheckLogic("WRITE ( SOURCE=INPUT, DEST=FILE={LFname.PFname} )");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();

		ecs.syntaxCheckLogic("WRITE ( SOURCE=INPUT, DEST=EXTRACT=9, PROCEDURE={wrproc} )");
		ecs.generateDependencies();

		assertFalse(ecs.hasDataErrors());
		assertNotNull(ecs.getFieldIDs().filter(f -> f == 99 ).findAny().orElse(null));
		assertNotNull(ecs.getFieldIDs().filter(f -> f == 25 ).findAny().orElse(null));
		Set<Integer> fs = ecs.getExitIDs();
		assertTrue(fs.contains(33));
    }

	@Test
    public void testMixedSycadas() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
        String inputString = "COLUMN =   <ZONED> {FindMe}";
		ecs.syntaxCheckLogic(inputString);
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();

		ExtractFilterSycada efs = (ExtractFilterSycada) SycadaFactory.getProcessorFor(SycadaType.EXTRACT_FILTER);
		efs.syntaxCheckLogic("SKIPIF({Another} > 0)");
		efs.generateDependencies();

		assertFalse(ecs.hasDataErrors());
		assertNotNull(ecs.getFieldIDs().filter(f -> f == 99 ).findAny().orElse(null));
		assertNotNull(efs.getFieldIDs().filter(f -> f == 25 ).findAny().orElse(null));
    }

	@Test
    public void testRepeatedDependencies() throws IOException {
		ExtractSycada efs = getInitialSycada(SycadaType.EXTRACT_FILTER);
		efs.syntaxCheckLogic("SELECTIF({FINdMe} > 0)");
		efs.generateDependencies();

		//If the depAnalyser is always news it can be done internally
		ExtractColumnSycada ecs = (ExtractColumnSycada) SycadaFactory.getProcessorFor(SycadaType.EXTRACT_COLUMN);
		ecs.syntaxCheckLogic("COLUMN = {FindMe}");
		ecs.generateDependencies();

		ExtractColumnSycada ecs2 = (ExtractColumnSycada) SycadaFactory.getProcessorFor(SycadaType.EXTRACT_COLUMN);
		ecs2.syntaxCheckLogic("COLUMN = {Another}");
		ecs2.generateDependencies();

		ecs.syntaxCheckLogic("COLUMN = {FindLookup.lkfield}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();

		ecs.syntaxCheckLogic("COLUMN = {Findlookup.lkfield}");
		assertEquals(0, ecs.getSyntaxErrors().size());
		ecs.generateDependencies();

		ExtractOutputSycada eos = (ExtractOutputSycada) SycadaFactory.getProcessorFor(SycadaType.EXTRACT_OUTPUT);
		eos.syntaxCheckLogic("WRITE ( SOURCE=INPUT, DEST=FILE={LFname.PFname} )");
		eos.generateDependencies();

		assertFalse(ecs.hasDataErrors());
		assertNotNull(ecs.getFieldIDs().filter(f -> f == 99 ).findAny().orElse(null));
    }

	@Test
    public void testRepeatedMixedCaseDependencies() throws IOException {
		ExtractSycada ecs = getInitialSycada(SycadaType.EXTRACT_COLUMN);
		ecs.syntaxCheckLogic("COLUMN = {findme}");
		ecs.generateDependencies();
		ecs.syntaxCheckLogic("COLUMN = {FindMe}");
		ecs.generateDependencies();
		ecs.syntaxCheckLogic("COLUMN = {FDWF_EC959_Mrkt_Unit_Eff_Dt_Lkup_From_LB587.MRKT_UNIT_BUSN_ID, FISCALDAY(); $SRC_AGRE_ROLE_IND=\"A\"} ");
		ecs.generateDependencies();

		assertFalse(ecs.hasDataErrors());
		assertFalse(ecs.hasSyntaxErrors());
		assertNotNull(ecs.getFieldIDs().filter(f -> f == 99 ).findAny().orElse(null));
    }

   @Test
    public void testFilterLookupAndLookup() throws IOException {
		ExtractSycada efs = getInitialSycada(SycadaType.EXTRACT_FILTER);
		efs.syntaxCheckLogic("SELECTIF(ISFOUND({GL_LLA_CONSTANT_TO_FIRST_LOW_LEVEL_STEP}) And ISNOTFOUND({GL_LLA_FODS_LINES_TO_JUA_BUS_UNITS}) )) ");
		assertEquals(1, efs.getSyntaxErrors().size());
		ParseTree pt = (ParseTree) efs.getParseTree();
		assertNotNull(pt);		
		efs.generateDependencies();
       
    }

    @Test
    public void testFilterAssignment() throws IOException {
		ExtractSycada efs = getInitialSycada(SycadaType.EXTRACT_FILTER);
		efs.syntaxCheckLogic("COLUMN = {findMe} ");
		assertEquals(0, efs.getSyntaxErrors().size());
		ParseTree pt = (ParseTree) efs.getParseTree();
		assertNotNull(pt);		
		efs.generateDependencies();
		assertTrue(efs.hasDataErrors());
		assertTrue(efs.getDataErrors().get(0).contains("Column assignment"));
      
    }

    @Test
    public void testGetVersion() throws IOException {
		assertTrue(ExtractSycada.getVersion().startsWith("sycadas", 0));
      
    }

	private ExtractSycada getInitialSycada(SycadaType type) {
		DummyDataProvider dataProvider = new DummyDataProvider();
		ExtractSycada ecs = (ExtractSycada) SycadaFactory.getProcessorFor(type);
		ecs.setDataProvider(dataProvider);
		ecs.getFieldsForSourceLr(19);
		return ecs;
	}

}
