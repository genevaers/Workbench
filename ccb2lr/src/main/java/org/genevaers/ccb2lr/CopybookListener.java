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
import java.util.List;

import org.genevaers.ccb2lr.grammar.CobolCopybookBaseListener;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser;

public class CopybookListener extends CobolCopybookBaseListener {

	private List<String> errors = new ArrayList<>();
	private String name;
	private int section;
	private GroupField group;
	private String currentSection;
	private RecordField recordField;
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
		section = Integer.parseInt(ctx.section().getText());
		if(section == 1) {
			//could cross check that this is the only 01 but we assume copybook
			//has already been compiled
			recordField = new RecordField();
			recordField.setName(name);
		} else {
			//This must be a group
			ParentField parent;
			if(group == null) {
				parent = recordField;
			} else if( group.getSection() == section) {
				parent = group.getParent();
			} else {
				parent = group;
			}
			group = new GroupField();
			group.setName(name);
			group.setSection(section);
			group.setParent(parent);
			parent.addField(group);
		}

	}

	@Override 
	public void exitGroup(CobolCopybookParser.GroupContext ctx) { 
	}

	@Override public void enterPrimitive(CobolCopybookParser.PrimitiveContext ctx) { 
		usage = null;
	}

	@Override public void exitPrimitive(CobolCopybookParser.PrimitiveContext ctx) { 
			//alternative is to gather info and put it together when we exit
		//that would be much better
		CobolField cb = makeField();
		currentCopybookField.setName(name);
		currentCopybookField.setSection(section);
		currentCopybookField.setPicType(picType);
		currentCopybookField.setPicCode(picCode);
		if(group != null) {
			if( section == group.getSection() ) {
				group = null;
				recordField.addField(currentCopybookField);
			} else {
				if(section > group.getSection()) {
					group.addField(currentCopybookField);
				} else {
					ParentField parent = group.getParent();
					int grpSec = parent.getSection();
					while(section <= grpSec) {
						parent = parent.getParent();
						grpSec = parent.getSection();
					}
					parent.addField(currentCopybookField);
				}
			}
		} else {
			recordField.addField(currentCopybookField);
		}
	}


	@Override public void enterIdentifier(CobolCopybookParser.IdentifierContext ctx) { 
		name = ctx.getText();
	}

	@Override public void enterSection(CobolCopybookParser.SectionContext ctx) { 
		section = Integer.parseInt(ctx.getText());	
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

	public RecordField getRecordField() {
		return recordField;
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
