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
import java.util.SortedMap;
import java.util.TreeMap;

import org.genevaers.ccb2lr.CobolField.FieldType;
import org.genevaers.ccb2lr.grammar.CobolCopybookBaseListener;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser;
import org.genevaers.ccb2lr.grammar.CobolCopybookParser.GroupContext;

public class CopybookListener extends CobolCopybookBaseListener {

	private List<String> errors = new ArrayList<>();
	private String name;
	private int section;
	private TreeMap<Integer, List<GroupField>> sections = new TreeMap<>();
	private GroupField group;
	//private List<OccursGroup> occursList;
	//private RecordField recordField;
	//private CobolField currentCopybookField;
	private String usage;
	private String picType;
	private String picCode;
	private int times = 1;
	private GroupField root;
	
	public boolean hasErrors() {
		return errors.size() > 0;
	}
	
	public List<String> getErrors() {
		return errors;
	}

	@Override 
	public void exitGroup(CobolCopybookParser.GroupContext ctx) { 
		//Want to get the identifier name
		name = ctx.identifier().getText();	
		GroupField newGroup = CobolFieldFactory.makeNewGroup(times);

		section = Integer.parseInt(ctx.section().getText());
		List<GroupField> groups = sections.computeIfAbsent(section, s -> makeNewGroupList(times));
		groups.add(newGroup);
//		group = fieldTree.computeIfAbsent(section, s -> CobolFieldFactory.makeNewGroup(times));
		newGroup.setName(name);
		newGroup.setSection(section);
		//GroupField grp = fieldTree.get(section);
		// if(grp != null) {
		// 	group = grp.getParent();
		// }
		if(root != null) {
			if(root.getSection() == section) {
				// the  parent is going to be the new root
				root = groups.get(0).getParent();
				newGroup.setParent(root);
				root.addField(newGroup);
			} else {
				newGroup.setParent(root);
				root.addField(newGroup);
				root = newGroup;
			}
		} else {
			newGroup.setParent(root);
			root = newGroup;
		}
		if(times > 1) { 
			((OccursGroup)newGroup).setTimes(times);
			times = 1;
		}
	}

	private List<GroupField> makeNewGroupList(int times) {
		return new ArrayList<>();
	}

	private GroupField findNamedGroup(List<GroupField> groups, String name) {
		return groups.stream()
				.filter(grp -> name.equals(grp.getName()))
				.findAny()
				.orElse(null);

	}

	@Override public void enterPrimitive(CobolCopybookParser.PrimitiveContext ctx) { 
		usage = null;
	}

	@Override public void exitPrimitive(CobolCopybookParser.PrimitiveContext ctx) { 
			//alternative is to gather info and put it together when we exit
		//that would be much better
		CobolField cbf = CobolFieldFactory.makeField(usage, picType);
		cbf.setName(name);
		cbf.setSection(section);
		cbf.setPicType(picType);
		cbf.setPicCode(picCode);

		List<GroupField> groups = sections.get(section);
		if(groups != null) {
			GroupField grp = groups.get(groups.size()-1);
			root = grp.getParent();
		}
		root.addField(cbf);
		// grp.addField(currentCopybookField);

		// if(group != null) {

		// 	// if section > lastGroupSection  - keep groups as a list.
		// 	//	addfield to last Group
		// 	// else 
		// 	//  grp = getGroupWithSection (s)
		// 	//  --- grp may be record level.... levels intead of groups?
		// 	//  record level no different to another level ... just no parent
		// 	// grp.close
		// 	if( section == group.getSection() ) { //get group at this level
		// 		closeGroup();
		// 		group = null; //end of group
		// 		recordField.addField(currentCopybookField);
		// 	} else {
		// 		if(section > group.getSection()) {
		// 			group.addField(currentCopybookField);
		// 		} else {
		// 			ParentField p = group.getParent();
		// 			int grpSec = p.getSection();
		// 			while(section <= grpSec) {
		// 				p = p.getParent();
		// 				grpSec = p.getSection();
		// 			}
		// 			p.addField(currentCopybookField);
		// 		}
		// 	}
		// } else {
		// 	recordField.addField(currentCopybookField);
		// }
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

	@Override 
	public void exitOccurs(CobolCopybookParser.OccursContext ctx) { 
		int numChildren = ctx.getChildCount();
		if(numChildren == 3) { //simple OCCURS x TIMES
			times = Integer.parseInt(ctx.getChild(1).getText());
		}
	}

	public SortedMap<Integer, List<GroupField>> getFieldTree() {
		return sections;
	}

}
