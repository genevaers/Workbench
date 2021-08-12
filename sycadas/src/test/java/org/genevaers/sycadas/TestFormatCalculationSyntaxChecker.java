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

public class TestFormatCalculationSyntaxChecker {


	@Test
    public void testCOLUMNAssign() throws IOException {
		FormatCalculationSyntaxChecker fcsc = (FormatCalculationSyntaxChecker) SycadaFactory.getProcesorFor(SycadaType.FORMAT_CALCULATION);
		assertNotNull(fcsc);
		fcsc.processLogic("CoLUMN = Col.1 + 3");
		assertEquals(0, fcsc.getSyntaxErrors().size());
		assertFalse(fcsc.hasSyntaxErrors());
    }

}
