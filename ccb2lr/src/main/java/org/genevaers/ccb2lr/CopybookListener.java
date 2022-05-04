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

	private CobolCollection collection = new CobolCollection();
	private List<String> errors = new ArrayList<>();
	private int section;
	private int groupSection;
	private String name;
	private String usage;
	private String picType;
	private String picCode;
	private int times = 1;
	private boolean redefines;
	private boolean groupRedefines;
	private String redefinedName;
	private int fillCount;
	
	public boolean hasErrors() {
		return errors.size() > 0;
	}
	
	public List<String> getErrors() {
		return errors;
	}

	@Override 
	public void enterGroup(CobolCopybookParser.GroupContext ctx) { 
		groupRedefines = false;
	}

	@Override 
	public void exitGroup(CobolCopybookParser.GroupContext ctx) { 
		groupSection = Integer.parseInt(ctx.section().getText());

		GroupField newGroup = CobolFieldFactory.makeNewGroup(times);
		newGroup.setName(name);
		newGroup.setSection(groupSection);

		if(times > 1) { 
			((OccursGroup)newGroup).setTimes(times);
			times = 1;
		}
		if(redefines) {
			newGroup.setRedefinedName(redefinedName);
			newGroup.setRedefines(true);
			groupRedefines = redefines;
			redefinedName = "";
			redefines = false;
		}

		collection.addCobolField(newGroup);
	}

	@Override public void enterPrimitive(CobolCopybookParser.PrimitiveContext ctx) { 
		usage = null;
	}

	@Override public void exitPrimitive(CobolCopybookParser.PrimitiveContext ctx) { 
		CobolField cbf = CobolFieldFactory.makeField(usage, picType);
		cbf.setName(name);
		cbf.setSection(section);
		cbf.setPicType(picType);
		cbf.setPicCode(picCode);
		cbf.setRedefines(redefines || groupRedefines);
		cbf.setRedefinedName(redefinedName);

		collection.addCobolField(cbf);
		redefinedName = "";
		redefines = false;
	}

	@Override public void enterIdentifier(CobolCopybookParser.IdentifierContext ctx) { 
		String ctxName = fillerFix(ctx.getText());
		if(redefines) {
			redefinedName = ctxName;
		} else {
			name = ctxName;
		}
	}

	private String fillerFix(String cn) {
		String ctxName = cn;
		if(ctxName.equalsIgnoreCase("FILLER")) {
			fillCount++;
			ctxName += String.format("_%02d", fillCount);
		}
		return ctxName;
	}

	@Override public void enterSection(CobolCopybookParser.SectionContext ctx) { 
		section = Integer.parseInt(ctx.getText());	
		if(section == groupSection) {
			groupRedefines = false;
		}
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

	@Override 
	public void exitOccurs(CobolCopybookParser.OccursContext ctx) { 
		int numChildren = ctx.getChildCount();
		if(numChildren == 3) { //simple OCCURS x TIMES
			times = Integer.parseInt(ctx.getChild(1).getText());
		}
	}

	@Override public void enterRedefines(CobolCopybookParser.RedefinesContext ctx) { 
		redefines = true;
	}

	public CobolCollection getCollection() {
		return collection;
	}

}
