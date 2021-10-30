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

public class CopybookListener extends CobolCopybookBaseListener {

	private List<String> errors = new ArrayList<>();
	private String name;
	private List<CobolField> fields = new ArrayList<>();
	
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
		
	}

	@Override public void enterPrimitive(CobolCopybookParser.PrimitiveContext ctx) { 
		CobolField cbf = new CobolField();
		cbf.setName(ctx.identifier().getText());
		fields.add(cbf);
	}


	public String getName() {
		return name;
	}

	public List<CobolField> getFields() {
		return fields;
	}
}
