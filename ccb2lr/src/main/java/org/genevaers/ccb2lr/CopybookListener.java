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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.genevaers.ccb2lr.grammar.CobolCopybookBaseListener;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser.Alpha_xContext;

public class CopybookListener extends CobolCopybookBaseListener {

	private List<String> errors = new ArrayList<>();
	private String name;
	private String section;
	private RecordModel recordModel;
	private CobolField currentCopybookField;
	private String usage;
	private String picType;
	private String picCode;
	
	public boolean hasErrors() {
		return errors.size() > 0;
	}
	
	public List<String> getErrors() {
		return errors;
	}

	@Override 
	public void enterGroup(CobolCopybookParser.GroupContext ctx) { 
		//Want to get the identifier name
		name = ctx.identifier().getText();
		section = ctx.section().getText();
		if(recordModel == null  && section.equals("01")) {
			recordModel = new RecordModel();
			recordModel.setName(name);
		} else {
			//error condition - there should be only one 01 line
		}

	}

	@Override public void exitPrimitive(CobolCopybookParser.PrimitiveContext ctx) { 
		//alternative is to gather info and put it together when we exit
		//that would be much better
		CobolField cb = makeField();
		currentCopybookField.setName(name);
		currentCopybookField.setSection(section);
		currentCopybookField.setPicType(picType);
		currentCopybookField.setPicCode(picCode);
		recordModel.addField(currentCopybookField);
	}


	@Override public void enterIdentifier(CobolCopybookParser.IdentifierContext ctx) { 
		name = ctx.getText();
	}

	@Override public void enterSection(CobolCopybookParser.SectionContext ctx) { 
		section = ctx.getText();	
	}

	@Override public void enterUsage(CobolCopybookParser.UsageContext ctx) { 
		usage = ctx.getText();
	}


	@Override public void exitAlpha_x(CobolCopybookParser.Alpha_xContext ctx) { 
		//But a COMP usage may make it a different type
		//So use exit?
		picType = "alpha_x";
		picCode = ctx.getText();
	}

	@Override public void exitSign_precision_9(CobolCopybookParser.Sign_precision_9Context ctx) { 
		picType = "signed_precision_9";
		picCode = ctx.getText();
	}

	public RecordModel getRecordModel() {
		return recordModel;
	}

	private CobolField makeField() {
		//Depends on usage and pic code type
		if(usage == null) {
			if(picType.equals("alpha_x")) {
				currentCopybookField = new AlphanumericField();
			} else if(picType.equals("signed_precision_9")) {
				currentCopybookField = new ZonedField();
			}
		} else {
			switch(usage.toLowerCase()) {
				case "comp-3":
				currentCopybookField = new PackedField();
				break;
				case "comp-4":
				case "comp-5":
				currentCopybookField = new BinaryField();
				break;
			}
		}
		return null;
	}

}
