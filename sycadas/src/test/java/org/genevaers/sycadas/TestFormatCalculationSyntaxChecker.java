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


import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

public class TestFormatCalculationSyntaxChecker {


	@Test
    public void testColumnAssign() throws IOException {
		FormatCalculationSyntaxChecker fcsc = (FormatCalculationSyntaxChecker) SycadaFactory.getProcessorFor(SycadaType.FORMAT_CALCULATION);
		assertNotNull(fcsc);
		fcsc.syntaxCheckLogic("CoLUMN = Col.1 + 3");
		assertEquals(0, fcsc.getSyntaxErrors().size());
		assertFalse(fcsc.hasSyntaxErrors());
    }

	@Test
    public void testColumnMoreThanOneAssign() throws IOException {
		FormatCalculationSyntaxChecker fcsc = (FormatCalculationSyntaxChecker) SycadaFactory.getProcessorFor(SycadaType.FORMAT_CALCULATION);
		assertNotNull(fcsc);
		fcsc.syntaxCheckLogic("CoLUMN = Col.4 + Col.7");
		Set<Integer> crefs = fcsc.getColumnRefs();
		assertEquals(2, crefs.size());
		Iterator<Integer> cfr = crefs.iterator();
		assertEquals(4, cfr.next().intValue());
		assertEquals(7, cfr.next().intValue());
    }


	@Test
    public void testFormatFilter() throws IOException {
		FormatFilterSyntaxChecker ffsc = (FormatFilterSyntaxChecker) SycadaFactory.getProcessorFor(SycadaType.FORMAT_FILTER);
		assertNotNull(ffsc);
		ffsc.syntaxCheckLogic("SELECTIF(Col.3 > 0)");
		Set<Integer> crefs = ffsc.getColumnRefs();
		assertEquals(1, crefs.size());
		Iterator<Integer> cfr = crefs.iterator();
		assertEquals(3, cfr.next().intValue());
    }

	@Test
    public void testTwoColumnFormatFilter() throws IOException {
		FormatFilterSyntaxChecker ffsc = (FormatFilterSyntaxChecker) SycadaFactory.getProcessorFor(SycadaType.FORMAT_FILTER);
		assertNotNull(ffsc);
		ffsc.syntaxCheckLogic("SELECTIF(Col.3 + COL.9 > 0)");
		Set<Integer> crefs = ffsc.getColumnRefs();
		assertEquals(2, crefs.size());
		Iterator<Integer> cfr = crefs.iterator();
		assertEquals(3, cfr.next().intValue());
		assertEquals(9, cfr.next().intValue());
    }

}
