package com.ibm.safr.we.ui.views.logic;

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

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class LogicTextViewTreeNode {
	private String toolTipText = null;
	private String editorText = null;
	private String titleText = null;
	private int offset = 0;
	private TreeItemId id = null;
	private LogicTextViewTreeNode parent = null;
	List<LogicTextViewTreeNode> children = new ArrayList<LogicTextViewTreeNode>();
	private Object data;// store optional data related to the model object.Eg.

	// for lookuppaths it stores its id.

	public enum TreeItemId {
		ROOT, KEYWORDS, LANGCONSTRUCTS, /**
		 * It is a main node in the logic Text
		 * tree.
		 */
		COLUMNS, COLUMNS_CHILD, /**
		 * It is a child node under language constructs
		 * in the logic Text tree.
		 */
		COLUMN, COLDESC, COLDOT, ITE, ITEE, SELECTIF, SKIPIF, SELECT, SKIP, COLEQUAL, FUNCTIONS, ALL, 
		CURRENT, PRIOR, DATE, DAYSBETWEEN, MONTHSBETWEEN, YEARSBETWEEN, BATCHDATE, TIMESTAMP, FISCALDAY, FISCALMONTH, 
		FISCALPERIOD, FISCALYEAR, ISFOUND, ISNOTFOUND, ISNULL, ISNOTNULL, ISNUMERIC, ISNOTNUMERIC, ISSPACES, 
		ISNOTSPACES, REPEAT, RUNDAY, RUNMONTH, RUNYEAR, WRITE, SUBSTR, LEFT, RIGHT,/**
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * It is a child node under the Function 'Write' Node in the Logic Text
		 * Tree.
		 */
		PROCEDURE, /**
		 * It is a child node under the Function 'Write' Node in the
		 * Logic Text Tree.
		 */
		USEREXIT, SOURCEINPUT, SOURCEVIEW, SOURCEDATA, DESTINATIONEXTRACT, DESTINATIONDEFAULT, DESTINATIONFILE, STRINGOPERATORS, 
		ANDOP, LOGICALOPERATORS, AND, OR, NOT, ARITHMETICOPR, ADD, MINUS, MUL, DIVIDE, COMPARISIONOPR, 
		BEGINSWITH, ENDWITH, CONTAINS, 
		GREATERTHAN, LESSTHAN, EQUALSTO, GREATERTHANEQUALS, LESSTHANEQUALS, 
		NOTEQUALS, FIELDSORCOL, LOOKUPPATHS, LOOKUPSYMBOLS, WRITEPARAM, /**
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * It is a child node under node 'Write Parameters' in the Logic Text
		 * tree.
		 */
		PROCEDURES, FILES, FIELDS, FIELDS_CHILD, LOOKUPPATHS_CHILD, LOOKUPPATHS_CHILD_FIELD, LOOKUPSYMBOLS_CHILD, LOOKUPSYMBOLS_CHILD_SYMBOL, /**
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * It is a child node under node 'Write Parameters' in the Logic Text
		 * tree.
		 */

		USEREXITROUTINES, FILES_CHILDLF, PROCEDURES_CHILD, USEREXITROUTINES_CHILD, FILES_CHILDLF_CHILDPF,
		
		/**
		 * Cast operators
		 */
		CAST_OPR, CAST_ALPHA, CAST_NODTF, CAST_BINARY, CAST_BCD, CAST_EDITED, CAST_MASKED, 
		CAST_PACKED,CAST_SBINARY, CAST_SPACKED, CAST_ZONED
	};

	public LogicTextViewTreeNode(TreeItemId id, String titleText,
			LogicTextViewTreeNode parent, String editorText, int offset,
			String toolTipText, List<LogicTextViewTreeNode> children) {
		this.id = id;
		this.parent = parent;
		this.titleText = titleText;
		this.editorText = editorText;
		this.offset = offset;
		this.toolTipText = toolTipText;
		this.children = children;
	}

	public TreeItemId getId() {
		return id;
	}

	public void setId(TreeItemId id) {
		this.id = id;
	}

	public String getName() {
		return titleText;
	}

	public void setName(String titleText) {
		this.titleText = titleText;
	}

	public LogicTextViewTreeNode getParent() {
		return parent;
	}

	public void setParent(LogicTextViewTreeNode parent) {
		this.parent = parent;
	}

	public List<LogicTextViewTreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<LogicTextViewTreeNode> children) {
		this.children = children;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public String toString() {
		return titleText;
	}

	public String getToolTipText() {
		return toolTipText;
	}

	public void setToolTipText(String toolTipText) {
		this.toolTipText = toolTipText;
	}

	public String getEditorText() {
		return editorText;
	}

	public void setEditorText(String editorText) {
		this.editorText = editorText;
	}

	public String getTitleText() {
		return titleText;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}

	/**
	 * @return optional data stored in the model object.
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Store optional data related to the model object.Eg. for LookUpPaths it
	 * stores its id.
	 * 
	 * @param data
	 *            to store in the model object.
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * This method returns the edit rights for the {@link LogicTextViewTreeNode}
	 * . The rights are returned as follows: <li>If the node has no rights then
	 * it returns null. <li>If node is of type field then it returns the rights
	 * of its parent logical record. <li>For the node of type physical file,if
	 * its parent logical file node has no rights then returns null, else it
	 * returns the rights of the physical file itself.
	 * 
	 * 
	 * @return the edit rights for the {@link LogicTextViewTreeNode}
	 */
	public EditRights getRights() {
		if (SAFRApplication.getUserSession().isSystemAdministrator()) {
			return EditRights.ReadModifyDelete;
		}

		Object elementModel = this.getData();
		if (elementModel == null) {
			return EditRights.ReadModifyDelete;
		}
		if (elementModel instanceof EnvironmentalQueryBean) {
			return ((EnvironmentalQueryBean) elementModel).getRights();
		}
		try {
			// for lf-pf association.
			if (elementModel instanceof FileAssociation) {
				return ((FileAssociation) elementModel).getAssociatedComponentRights();
			}
			// for lr fields.
			if (elementModel instanceof ComponentAssociation) {
				return SAFRApplication.getUserSession().getEditRights(ComponentType.LogicalRecord,
				    ((ComponentAssociation) elementModel).getAssociatingComponentId());
			}
		} catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e, "Error in getting edit rights.",null);
			return EditRights.Read;
		}
		return EditRights.Read;
	}

}
