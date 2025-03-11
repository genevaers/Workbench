package com.ibm.safr.we.model.utilities;

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


import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.transfer.FindTransfer;

/**
 *This class is used in the find replace logic text utility.
 */
public class FindReplaceComponent {
	private Integer viewId;
	private String viewName;
	private LogicTextType logicTextType;
	private String logicText;
	private Integer parentId;
    private Integer columnId;	
	private Boolean selected;
	private EditRights rights;

	/**
	 */
	public FindReplaceComponent(Integer viewId, String viewName,
			LogicTextType logicTextType, String logicText, 
			Integer parentId, Integer columnId, EditRights rights) {
		super();
		this.viewId = viewId;
		this.viewName = viewName;
		this.logicTextType = logicTextType;
		this.logicText = logicText;
		this.parentId = parentId;
		this.columnId = columnId;
		this.rights = rights;
	}

    /**
	 * This method returns the view id of the component.
	 * 
	 * @return the view id of the component.
	 */
	public Integer getViewId() {
		return viewId;
	}

	/**
	 * Sets the view id for the component.
	 * 
	 * @param viewId
	 *            to set for the component.
	 */
	public void setViewId(Integer viewId) {
		this.viewId = viewId;
	}

	/**
	 * This method returns the view name of the component.
	 * 
	 * @return the view name of the component.
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * Sets the view name for the component.
	 * 
	 * @param viewName
	 *            to set for the component.
	 */
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	/**
	 * This method returns the {@link LogicTextType} of the component.
	 * 
	 * @return returns the {@link LogicTextType} of the component.
	 */
	public LogicTextType getLogicTextType() {
		return logicTextType;
	}

	/**
	 * Sets the {@link LogicTextType} .
	 * 
	 * @param logicTextType
	 *            {@link LogicTextType} to set foe the component.
	 */
	public void setLogicTextType(LogicTextType logicTextType) {
		this.logicTextType = logicTextType;
	}

	/**
	 * This method returns the logic text of the component.
	 * 
	 * @return returns the logic text of the component.
	 */
	public String getLogicText() {
		return logicText;
	}

	/**
	 * This method sets logic text for the component.
	 * 
	 * @param logicText
	 *            to set for the component.
	 */
	public void setLogicText(String logicText) {
		this.logicText = logicText;
	}

	/**
	 * This method returns the parent id of the component according to type of
	 * logic text.<br>
	 * <ul>
	 * <li>for format record filter it is View id. </li> 
	 * <li>for extract record
	 * filter it is View source id. </li> <li>for format phase calculation it is
	 * View column id. </li>
	 * <li>for extract column calculation it is View column
	 * source id.</li>
	 * </ul>
	 * 
	 * @return returns the parent id of the component according to type of logic
	 *         text.
	 */
	public Integer getParentId() {
		return parentId;
	}

	void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

    /**
     * @return The column id where the logic was found
     */
    public Integer getColumnId() {
        return columnId;
    }

    /**
     * Set The column id where the logic was found
     * @param columnId
     */
    public void setColumnId(Integer columnId) {
        this.columnId = columnId;
    }

	
	/**
	 * To get the user selection in the editor.
	 * 
	 * @return the selected
	 */
	public Boolean isSelected() {
		return selected;
	}

	/**
	 * To set the user selection in the editor.
	 * 
	 * @param selected
	 *            the selected to set
	 */
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

    public EditRights getRights() {
        return rights;
    }
	
    protected void setTransferData(FindTransfer trans) {
        trans.setViewId(viewId);
        trans.setViewName(viewName);
        trans.setLogicTextType(logicTextType);
        trans.setLogicText(logicText);
        trans.setCellId(parentId);
        trans.setReferenceId(columnId);
        trans.setRights(rights);
    }
    
}
