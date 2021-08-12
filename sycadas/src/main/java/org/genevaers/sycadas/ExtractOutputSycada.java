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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.genevaers.sycadas.dataprovider.SycadaDataProvider;
import org.genevaers.sycadas.grammar.ExtractFilterLexer;
import org.genevaers.sycadas.grammar.ExtractFilterParser;
import org.genevaers.sycadas.grammar.ExtractOutputLexer;
import org.genevaers.sycadas.grammar.ExtractOutputParser;

public class ExtractOutputSycada implements SyntaxChecker, DependencyAnalyser {

	private ParseTree tree;
	private ExtractOutputDependencyListener dependencies;
	private ParseErrorListener errorListener;

	@Override
	public void processLogic(String logicText) throws IOException {
		System.out.println(logicText);
        InputStream is = new ByteArrayInputStream(logicText.getBytes());
        @SuppressWarnings("deprecation")
		ANTLRInputStream input = new ANTLRInputStream(is);

        ExtractOutputLexer lexer = new ExtractOutputLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExtractOutputParser parser = new ExtractOutputParser(tokens);
        parser.removeErrorListeners(); // remove ConsoleErrorListener
        errorListener = new ParseErrorListener();
        parser.addErrorListener(errorListener); // add ours
        tree = parser.goal(); // parse
	}

	@Override
	public ParseTree getParseTree() {
		return tree;
	}

	@Override
	public void generateDependencyDataFrom(SycadaDataProvider dataFromHere) {
		dependencies = new ExtractOutputDependencyListener();
        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        dependencies.setDataProvider(dataFromHere);
        walker.walk(dependencies, tree); // initiate walk of tree with listener		
	}

	@Override
	public List<String> getSyntaxErrors() {
		return errorListener.getErrors();
	}

	@Override
	public int getNumberOfSyntaxWarningss() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasDataErrors() {
		return dependencies.hasErrors();
	}

	@Override
	public Set<Integer> getFieldIDs() {
		return dependencies.getFieldIDs();
	}

	@Override
	public Map<Integer, List<Integer>> getLookupIDs() {
		return dependencies.getLookupIDs();
	}

	@Override
	public List<String> getDataErrors() {
		return dependencies.getErrors();
	}

	@Override
	public boolean hasSyntaxErrors() {
		return errorListener.getErrors().size() > 0;
	}

	public Set<Integer> getExitIDs() {
		return dependencies.getExitIDs();
	}

	public Set<Integer> getLFPFAssocIDs() {
		return dependencies.getLFPFAssocIDs();
	}
}
