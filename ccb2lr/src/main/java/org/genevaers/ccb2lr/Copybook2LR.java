package org.genevaers.ccb2lr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser.GoalContext;
import org.genevaers.ccb2lr.grammar.CobolCopybookLexer;

public class Copybook2LR {

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
        if (errorListener.getErrors().isEmpty()) {
            ccbListener.getCollection().expandOccursGroupsIfNeeded();
            ccbListener.getCollection().resolvePositions();
        } else {
            errorListener.addErrorMessage("Please ensure the copybook compiles via IBM Enterprise COBOL for z/OS");
        }
    }

    private StringReader getReformattedInput(Path fp) throws FileNotFoundException, IOException {
        Reader reader = new FileReader(fp.toFile());
        StringBuilder data = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {

            String line = bufferedReader.readLine();
            while (line != null) {
                if (line.length() > 7) {
                    String addMe = line.substring(6);
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
        return !errorListener.getErrors().isEmpty();
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
            e.printStackTrace();
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
        fieldObj.put("decimalPlaces", f.getNunberOfDecimalPlaces());
        fieldObj.put("signed", f.isSigned());
        fieldsArray.add(fieldObj);
    }

    public CobolCollection getCobolCollection() {
        return ccbListener.getCollection();
    }

    public int getNumberOfCobolFields() {
        return ccbListener.getCollection().getNumberOfFields();
    }
}
