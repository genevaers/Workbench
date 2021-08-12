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


public class SycadaFactory {
	
	private static SyntaxChecker ecs = new ExtractColumnSycada();
	private static SyntaxChecker efs = new ExtractFilterSycada();
	private static SyntaxChecker eos = new ExtractOutputSycada();
	private static SyntaxChecker ffs = new FormatFilterSyntaxChecker();
	private static SyntaxChecker fcs = new FormatCalculationSyntaxChecker();

	public static SyntaxChecker getProcesorFor(SycadaType type) {
		switch(type) {
		case EXTRACT_COLUMN:
			return ecs;
		case EXTRACT_FILTER:
			return efs;
		case EXTRACT_OUTPUT:
			return eos;
		case FORMAT_FILTER:
			return ffs;
		case FORMAT_CALCULATION:
			return fcs;
		default:
			return null;
		}
	}

}
