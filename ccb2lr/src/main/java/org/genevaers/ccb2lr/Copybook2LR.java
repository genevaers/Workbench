package org.genevaers.ccb2lr;

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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.genevaers.ccb2lr.grammar.CobolCopybookLexer;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser.GoalContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Copybook2LR {
   	static transient Logger logger = Logger.getLogger("org.genevaers.ccb2lr.Copybook2LR");

    
	private GoalContext tree;
	private ParseErrorListener errorListener;
    private CopybookListener ccbListener;
    private ObjectMapper yamlMapper;
    private ObjectNode copyRecord;

    public void processCopybook(Path fp) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(getReformattedInput(fp));

        CobolCopybookLexer lexer = new CobolCopybookLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CobolCopybookParser parser = new CobolCopybookParser(tokens);
        parser.removeErrorListeners(); // remove ConsoleErrorListener
        errorListener = new ParseErrorListener();
        parser.addErrorListener(errorListener); // add ours
        tree = parser.goal(); // parse
        generateData();
        if (noParserErrors()) {
            if(noGenerationErrors()) {
                ccbListener.getCollection().expandOccursGroupsIfNeeded();
                ccbListener.getCollection().resolvePositions();
            } else {
                addToErrorListener();
            }
        } else {
            errorListener.addErrorMessage("Please ensure the copybook compiles via IBM Enterprise COBOL for z/OS");
        }
    }

    private void addToErrorListener() {
        for(String err : ccbListener.getErrors()) {
            errorListener.addErrorMessage(err);
        }
    }

    private boolean noGenerationErrors() {
        return ccbListener.getErrors().isEmpty();
    }

    private boolean noParserErrors() {
        return errorListener.getErrors().isEmpty();
    }

    private StringReader getReformattedInput(Path fp) throws FileNotFoundException, IOException {
        Reader reader = new FileReader(fp.toFile());
        StringBuilder data = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {

            String line = bufferedReader.readLine();
            while (line != null) {
                if (line.length() > 7) {
                    String addMe;
                    if(line.length() > 72) {
                        addMe = line.substring(6, 72);
                    } else {
                        addMe = line.substring(6);
                    }
                    data.append(addMe + "\n");
                }
                line = bufferedReader.readLine();
            }
        }
        return new StringReader(data.toString());
    }

	public void generateData() {
		ccbListener = new CopybookListener();
        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        walker.walk(ccbListener, tree); // initiate walk of tree with listener
	}


    public boolean hasErrors() {
        return !noParserErrors();
    }

    public List<String> getErrors() {
        return errorListener.getErrors();
    }

    public GroupField getRecordField() {
        return ccbListener.getCollection().getRecordGroup();
    }

    public void writeYAMLTo(String filename) {
        addRecordFieldToYamlTree();
        writeYaml(filename);
     }

    private void writeYaml(String filename) {
        try {
            yamlMapper.writeValue(new File(filename), copyRecord);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.severe("Cannot write yaml file\n" + e.getMessage());
        }
    }

    public void addRecordFieldToYamlTree() {
        yamlMapper = new ObjectMapper(new YAMLFactory());
        copyRecord = yamlMapper.createObjectNode();
		CobolCollection cbc = getCobolCollection();
        cbc.expandOccursGroupsIfNeeded();
		cbc.resolvePositions();
        addRecordFieldToRoot(cbc, copyRecord);
    }

    private void addRedefines(CobolCollection cbc, ArrayNode fieldsArray) {
        Iterator<CobolField> ri = cbc.getRedifinesIterator();
        while(ri.hasNext()) {
            CobolField r = ri.next();
            addFieldToFieldsArray(r, fieldsArray);
        }
    }

    public ObjectNode getRecord() {
        return copyRecord;
    }

	private void addRecordFieldToRoot(CobolCollection cbc, ObjectNode yamlRecord) {
        GroupField rec = cbc.getRecordGroup();
        yamlRecord.put("recordName", rec.getName().replace('-','_'));
        ArrayNode fieldsArray = yamlRecord.putArray("fields");
        CobolField n = rec.next();
		while(n != null) {
            addFieldToFieldsArray(n, fieldsArray);
            n = n.next();
		}
        addRedefines(cbc, fieldsArray);
	}

	private void addFieldToFieldsArray(CobolField f, ArrayNode fieldsArray) {
        ObjectNode fieldObj = yamlMapper.createObjectNode();
        fieldObj.put("name", f.getName().replace('-','_'));
        fieldObj.put("datatype", f.getType().getDataType());
        fieldObj.put("datatypeCode", f.getType().getCode());
        fieldObj.put("position", f.getPosition());
        fieldObj.put("length", f.getLength());
        fieldObj.put("decimalPlaces", f.getNumberOfDecimalPlaces());
        fieldObj.put("signed", f.isSigned());
        fieldsArray.add(fieldObj);
    }

    public CobolCollection getCobolCollection() {
        return ccbListener.getCollection();
    }

    public int getNumberOfCobolFields() {
        return ccbListener.getCollection().getNumberOfFields();
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
