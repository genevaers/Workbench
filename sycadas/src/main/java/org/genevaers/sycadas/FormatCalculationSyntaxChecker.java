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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.genevaers.grammar.FormatCalculationLexer;
import org.genevaers.grammar.FormatCalculationParser;
import org.genevaers.grammar.FormatCalculationParser.GoalContext;

public class FormatCalculationSyntaxChecker implements SyntaxChecker {

	private ParseErrorListener errorListener;
	private GoalContext tree;

	private FormatCalculationListener formatListener = new FormatCalculationListener();


	@Override
	public void syntaxCheckLogic(String logicText) {
        InputStream is = new ByteArrayInputStream(logicText.getBytes());
        @SuppressWarnings("deprecation")
		ANTLRInputStream input;
		try {
			input = new ANTLRInputStream(is);
	        FormatCalculationLexer lexer = new FormatCalculationLexer(input);
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        FormatCalculationParser parser = new FormatCalculationParser(tokens);
	        parser.removeErrorListeners(); // remove ConsoleErrorListener
	        errorListener = new ParseErrorListener();
	        parser.addErrorListener(errorListener); // add ours
	        tree = parser.goal(); // parse
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public ParseTree getParseTree() {
		return tree;
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
	public boolean hasSyntaxErrors() {
		return errorListener.getErrors().size() > 0;
	}

    public Set<Integer> getColumnRefs() {
       	ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
       	walker.walk(formatListener, tree); // initiate walk of tree with listener		
		return formatListener.getColumnRefs();
    }


}
