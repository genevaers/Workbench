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


import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;

/**
 * This model class is used to pass environment related components to Batch
 * Activate utilities. These utilities accepts a list of this class objects and
 * activates the components in it. The errors, if any, and the result is than
 * populated by the Batch activate utilities.
 */
public class BatchComponent {
	private EnvironmentalQueryBean component;
	private ActivityResult result = ActivityResult.NONE;
	private SAFRException exception;
    private Boolean active;
	private Boolean selected;

	public BatchComponent(EnvironmentalQueryBean component, Boolean active) {
		super();
		this.component = component;
		this.active = active;
		this.selected = false;
	}

	/**
	 * @return the component to activate
	 */
	public EnvironmentalQueryBean getComponent() {
		return component;
	}

	/**
	 * @return the result after batch activation
	 */
	public ActivityResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result after batch activation
	 */
	public void setResult(ActivityResult result) {
		this.result = result;
	}

    public SAFRException getException() {
        return exception;
    }

    public void setException(SAFRException exception) {
        this.exception = exception;
    }

	/**
	 * @return if the component is active
	 */
	public Boolean isActive() {
		return active;
	}

	/**
	 * @param isActive
	 *            the active status of the component
	 */
	public void setActive(Boolean isActive) {
		this.active = isActive;
	}

	/**
	 * @return the select
	 */
	public Boolean isSelected() {
		return selected;
	}

	/**
	 */
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

}
