package com.ibm.safr.we.cli;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.genevaers.wbscript.grammar.WBScriptLexer;
import org.genevaers.wbscript.grammar.WBScriptListener;
import org.genevaers.wbscript.grammar.WBScriptParser;
import org.genevaers.wbscript.grammar.WBScriptParser.AddStatementContext;
import org.genevaers.wbscript.grammar.WBScriptParser.ColumnContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Column_sourceContext;
import org.genevaers.wbscript.grammar.WBScriptParser.ColumnfieldsContext;
import org.genevaers.wbscript.grammar.WBScriptParser.ColumnsetsContext;
import org.genevaers.wbscript.grammar.WBScriptParser.ComponentContext;
import org.genevaers.wbscript.grammar.WBScriptParser.CrContext;
import org.genevaers.wbscript.grammar.WBScriptParser.CreateStatementContext;
import org.genevaers.wbscript.grammar.WBScriptParser.CrfieldsContext;
import org.genevaers.wbscript.grammar.WBScriptParser.CrsetsContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Cs_setContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Cs_src_typeContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Cs_typeContext;
import org.genevaers.wbscript.grammar.WBScriptParser.GoalContext;
import org.genevaers.wbscript.grammar.WBScriptParser.LfContext;
import org.genevaers.wbscript.grammar.WBScriptParser.LfaddsContext;
import org.genevaers.wbscript.grammar.WBScriptParser.LrContext;
import org.genevaers.wbscript.grammar.WBScriptParser.LraddsContext;
import org.genevaers.wbscript.grammar.WBScriptParser.PfContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Pf_fieldsContext;
import org.genevaers.wbscript.grammar.WBScriptParser.PfsetsContext;
import org.genevaers.wbscript.grammar.WBScriptParser.StmtContext;
import org.genevaers.wbscript.grammar.WBScriptParser.StmtListContext;
import org.genevaers.wbscript.grammar.WBScriptParser.ViewContext;
import org.genevaers.wbscript.grammar.WBScriptParser.View_add_fieldsContext;
import org.genevaers.wbscript.grammar.WBScriptParser.View_sourceContext;
import org.genevaers.wbscript.grammar.WBScriptParser.ViewaddsContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Vs_set_fieldsContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Vs_setsContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CliProcessor {
   	static transient Logger logger = Logger.getLogger("org.genevaers.ccb2lr.Copybook2LR");

    
	private GoalContext tree;
	private ParseErrorListener errorListener;
    private CLIListener cliListener;

    public void processScript(File fp) throws IOException {
        CharStream stream = CharStreams.fromFileName(fp.toString());

        WBScriptLexer lexer = new WBScriptLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        WBScriptParser parser = new WBScriptParser(tokens);
        parser.removeErrorListeners(); // remove ConsoleErrorListener
        errorListener = new ParseErrorListener();
        parser.addErrorListener(errorListener); // add ours
        tree = parser.goal(); // parse
        generateData();
        if (noParserErrors()) {
            if(noGenerationErrors()) {
            } else {
                addToErrorListener();
            }
        } else {
            errorListener.addErrorMessage("Parse errors please examine the script file.");
        }
    }

    private void addToErrorListener() {
        for(String err : cliListener.getErrors()) {
            errorListener.addErrorMessage(err);
        }
    }

    private boolean noGenerationErrors() {
        return cliListener.getErrors().isEmpty();
    }

    private boolean noParserErrors() {
        return errorListener.getErrors().isEmpty();
    }

	public void generateData() {
		cliListener = new CLIListener();
        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        walker.walk(cliListener, tree); // initiate walk of tree with listener
	}


    public boolean hasErrors() {
        return !noParserErrors();
    }

    public List<String> getErrors() {
        return errorListener.getErrors();
    }

	public static String getVersion() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
        String ver = "";
		try (InputStream resourceStream = loader.getResourceAsStream("ccb2lr.properties")) {
			properties.load(resourceStream);
            ver = properties.getProperty("library.name") + ": " + properties.getProperty("build.version");
		} catch (IOException e) {
			logger.severe("Cannot get build version\n" + e.getMessage());
		}
        return ver;
    }
}
