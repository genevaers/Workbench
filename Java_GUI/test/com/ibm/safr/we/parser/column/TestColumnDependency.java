package com.ibm.safr.we.parser.column;

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
import java.util.Iterator;
import java.util.logging.Logger;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.genevaers.sycadas.ExtractColumnDependencyListener;
import org.genevaers.sycadas.ParseErrorListener;
import org.genevaers.sycadas.grammar.ExtractColumnLexer;
import org.genevaers.sycadas.grammar.ExtractColumnParser;

import junit.framework.TestCase;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.TestPGModelSetupHelper;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewSource;

public class TestColumnDependency extends TestCase {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.parser.column.TestColumnDependency");
    
    TestPGModelSetupHelper testSetupHelper = new TestPGModelSetupHelper();
    
    public void testWritExit() throws SAFRException, IOException {
    	testSetupHelper.makeTestView();
        String inputString = "WRITE ( SOURCE=INPUT, DEST=EXTRACT=9, PROCEDURE={wrexit} )";
        InputStream is = new ByteArrayInputStream(inputString.getBytes());
        @SuppressWarnings("deprecation")
		ANTLRInputStream input = new ANTLRInputStream(is);

        ExtractColumnLexer lexer = new ExtractColumnLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExtractColumnParser parser = new ExtractColumnParser(tokens);
        ParseTree tree = parser.goal(); // parse

        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        ExtractColumnDependencyListener extractor = new ExtractColumnDependencyListener();
        walker.walk(extractor, tree); // initiate walk of tree with listener
        assertTrue(extractor.getExitIDs().size()>0);
        Iterator<Integer> wei = extractor.getExitIDs().iterator();
        assertTrue(wei.hasNext());
        assertTrue(wei.next().equals("wrexit"));
    }
    
    public void testFieldFound() throws SAFRException, IOException {
    	testSetupHelper.makeTestView();
        String inputString = "COLUMN = {Binary4}";
        
        InputStream is = new ByteArrayInputStream(inputString.getBytes());
        @SuppressWarnings("deprecation")
		ANTLRInputStream input = new ANTLRInputStream(is);

        ExtractColumnLexer lexer = new ExtractColumnLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExtractColumnParser parser = new ExtractColumnParser(tokens);
        ParseTree tree = parser.goal(); // parse

        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        ExtractColumnDependencyListener extractor = new ExtractColumnDependencyListener();
        walker.walk(extractor, tree); // initiate walk of tree with listener
        assertTrue(extractor.getFieldIDs().size() > 0);
        Iterator<Integer> fi = extractor.getFieldIDs().iterator();
        assertTrue(fi.hasNext());
        assertTrue(fi.next().equals("Binary4"));
    }
    
    public void testFieldNotFound() throws SAFRException, IOException {
    	testSetupHelper.makeTestView();
        String inputString = "COLUMN = {NoFindMe}";
        
        InputStream is = new ByteArrayInputStream(inputString.getBytes());
        @SuppressWarnings("deprecation")
		ANTLRInputStream input = new ANTLRInputStream(is);

        ExtractColumnLexer lexer = new ExtractColumnLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExtractColumnParser parser = new ExtractColumnParser(tokens);
        ParseTree tree = parser.goal(); // parse

        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        ExtractColumnDependencyListener extractor = new ExtractColumnDependencyListener();
        walker.walk(extractor, tree); // initiate walk of tree with listener
        assertTrue(extractor.getFieldIDs().size() > 0);
        Iterator<Integer> fi = extractor.getFieldIDs().iterator();
        assertTrue(fi.hasNext());
        assertTrue(fi.next().equals("NoFindMe"));
        // Now the real question is does the source LR have this field.
        
    }
    
    
    public void testCompleteBollocks() throws SAFRException, IOException {
    	testSetupHelper.makeTestView();
        String inputString = "complete collocks";
        
        InputStream is = new ByteArrayInputStream(inputString.getBytes());
        @SuppressWarnings("deprecation")
		ANTLRInputStream input = new ANTLRInputStream(is);

        ExtractColumnLexer lexer = new ExtractColumnLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExtractColumnParser parser = new ExtractColumnParser(tokens);
        parser.removeErrorListeners(); // remove ConsoleErrorListener
        ParseErrorListener errorListener = new ParseErrorListener();
        parser.addErrorListener(errorListener); // add ours
        ParseTree tree = parser.goal(); // parse
        int syntaxErrors = parser.getNumberOfSyntaxErrors();
        assertTrue(syntaxErrors > 0);
        for(String err : errorListener.getErrors()) {
        	System.out.println(err);
        }

        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        ExtractColumnDependencyListener extractor = new ExtractColumnDependencyListener();
        walker.walk(extractor, tree); // initiate walk of tree with listener
        
    }
    
    
}
