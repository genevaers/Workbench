package com.ibm.safr.we.data.transfer;

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

/**
 * This class used to transfer data from data layer to model in the find replace
 * utility.
 * 
 */
public class FindTransfer {
	private Integer viewId;
	private String viewName;
	private LogicTextType logicTextType;
	// for FRF cellid =0
	// for ERF cellid =view source id
	// for ECC cellid =view column source id
	// for FCC cellid =view column id
	private Integer cellId;
	// for ERF,FRF ref id =0
	// for FCC,ECC ref id= column no.
	private Integer referenceId;
	private EditRights rights;	
	private String logicText;

	/**
	 * @return the view id .
	 */
	public Integer getViewId() {
		return viewId;
	}

	/**
	 * sets the view id for the transfer.
	 * 
	 * @param viewId
	 *            to set for the transfer.
	 */
	public void setViewId(Integer viewId) {
		this.viewId = viewId;
	}

	/**
	 * This method returns the view name.
	 * 
	 * @return the view name of the transfer.
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * Sets the view name.
	 * 
	 * @param viewName
	 *            view name to set .
	 */
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	/**
	 * @return the {@link LogicTextType} of the transfer object.
	 */
	public LogicTextType getLogicTextType() {
		return logicTextType;
	}

	/**
	 * sets the {@link LogicTextType}.
	 * 
	 * @param logicTextType
	 *            {@link LogicTextType} to set.
	 */
	public void setLogicTextType(LogicTextType logicTextType) {
		this.logicTextType = logicTextType;
	}

	/**
	 * 
	 * @return the cell id stored the database.
	 */
	public Integer getCellId() {
		return cellId;
	}

	/**
	 * Sets the cell is for the transfer.
	 * 
	 * @param cellId
	 *            to set for the tranfer.
	 */
	public void setCellId(Integer cellId) {
		this.cellId = cellId;
	}

	/**
	 * @return the referesnce id stored the database.
	 */
	public Integer getReferenceId() {
		return referenceId;
	}

	/**
	 * Sets the reference id for the transfer.
	 * 
	 * @param referenceId
	 *            to set for the transfer.
	 */
	public void setReferenceId(Integer referenceId) {
		this.referenceId = referenceId;
	}

	/**
	 * @return the logic text
	 */
	public String getLogicText() {
		return logicText;
	}

	/**
	 * Sets the logic text bytes array.
	 * 
	 * @param logicText logic text to set.
	 */
	public void setLogicText(String logicText) {
		this.logicText = logicText;
	}

    public EditRights getRights() {
        return rights;
    }

    public void setRights(EditRights rights) {
        this.rights = rights;
    }

}
