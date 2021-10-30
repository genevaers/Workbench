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


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser.GoalContext;
import org.genevaers.ccb2lr.grammar.CopybookLexer;

public class Copybook2LR {

	private GoalContext tree;
	private ParseErrorListener errorListener;
    private CopybookListener ccbListener;

    public void readFileFrom(Path fp) {

    }

	public void processCopybook(Path fp) throws IOException {
        InputStream is = new FileInputStream(fp.toFile());
        @SuppressWarnings("deprecation")
		ANTLRInputStream input = new ANTLRInputStream(is);

        CopybookLexer lexer = new CopybookLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CobolCopybookParser parser = new CobolCopybookParser(tokens);
        parser.removeErrorListeners(); // remove ConsoleErrorListener
        errorListener = new ParseErrorListener();
        parser.addErrorListener(errorListener); // add ours
        tree = parser.goal(); // parse
	}

	public void generateData() {
		ccbListener = new CopybookListener();
        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        walker.walk(ccbListener, tree); // initiate walk of tree with listener		
	}


    public boolean hasErrors() {
        return errorListener.getErrors().size() > 0;
    }

    public RecordModel getRecordModel() {
        return ccbListener.getRecordModel();
    }
}
