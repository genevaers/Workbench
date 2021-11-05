package org.genevaers.ccb2lr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.genevaers.ccb2lr.CobolField.FieldType;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser.GoalContext;
import org.genevaers.ccb2lr.grammar.CopybookLexer;

public class Copybook2LR {

	private GoalContext tree;
	private ParseErrorListener errorListener;
    private CopybookListener ccbListener;
    private ObjectMapper yamlMapper;
    private ObjectNode record;

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

    public RecordField getRecordField() {
        return ccbListener.getRecordField();
    }

    public void writeYAMLTo(String filename) {
        addRecordFieldToYamlTree();
        writeYaml(filename);
     }

    private void writeYaml(String filename) {
        try {
            yamlMapper.writeValue(new File(filename), record);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void addRecordFieldToYamlTree() {
        yamlMapper = new ObjectMapper(new YAMLFactory());
        record = yamlMapper.createObjectNode();
        RecordField recField = ccbListener.getRecordField();
        recField.resolvePositions();
        addRecordFieldToRoot(recField, record);
    }

    public ObjectNode getRecord() {
        return record;
    }

	private void addRecordFieldToRoot(RecordField rf, ObjectNode record) {
		Iterator<CobolField> fit = rf.getFieldIterator();
        record.put("recordName", rf.getName().replace('-','_'));
        ArrayNode fieldsArray = record.putArray("fields");
		while(fit.hasNext()) {
			CobolField f = fit.next();
            addFieldToFieldsArray(f, fieldsArray);
			if(f.getType() == FieldType.GROUP) {
				recurseGroup(f, fieldsArray);
			}
		}
	}

	private void addFieldToFieldsArray(CobolField f, ArrayNode fieldsArray) {
        ObjectNode fieldObj = yamlMapper.createObjectNode();
        fieldObj.put("name", f.getName().replace('-','_'));
        fieldObj.put("datatype", f.getType().getDataType());
        fieldObj.put("datatypeCode", f.getType().getCode());
        fieldObj.put("position", f.getPosition());
        fieldObj.put("length", f.getLength());
        fieldObj.put("signed", f.isSigned());
        fieldsArray.add(fieldObj);
    }

    private void recurseGroup(CobolField f, ArrayNode fieldsArray) {
        Iterator<CobolField> git = ((GroupField) f).getChildIterator();
        while (git.hasNext()) {
            CobolField gf = git.next();
            addFieldToFieldsArray(gf, fieldsArray);
            if (gf.getType() == FieldType.GROUP) {
                recurseGroup(gf, fieldsArray);
            }
        }
    }
}
