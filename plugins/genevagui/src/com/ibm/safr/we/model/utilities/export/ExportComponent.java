package com.ibm.safr.we.model.utilities.export;

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


import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;

public class ExportComponent {
	private EnvironmentalQueryBean component;
	private ActivityResult result;
	private List<String> errors;
	private Boolean selected;

    public ExportComponent(EnvironmentalQueryBean component) {
		super();
		this.component = component;
		this.errors = new ArrayList<String>();
		this.selected = false;
	}

    public ExportComponent(EnvironmentalQueryBean component, OutputStream stream) {
        this(component);
    }
	
	/**
	 * This method is used to get the component to be exported.
	 * 
	 * @return the component
	 */
	public EnvironmentalQueryBean getComponent() {
		return component;
	}

	/**
	 * @param component
	 *            the component to set
	 */
	public void setComponent(EnvironmentalQueryBean component) {
		this.component = component;
	}

	/**
	 * This method is used to get the result of export activity (Fail, Pass)for
	 * the current component.
	 * 
	 * @return the result
	 */
	public ActivityResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(ActivityResult result) {
		this.result = result;
	}

	/**
	 * @return the errors
	 */
	public List<String> getErrors() {
		return errors;
	}
	
	public boolean hasErrors() {
	    return !errors.isEmpty();
	}

	/**
	 * @param errors
	 *            the errors to set
	 */
	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

	/**
	 * @return the selected
	 */
	public Boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected
	 *            the selected to set
	 */
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

}
