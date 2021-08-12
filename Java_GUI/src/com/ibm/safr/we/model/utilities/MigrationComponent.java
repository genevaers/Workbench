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

public class MigrationComponent {
	
	private EnvironmentalQueryBean component;
	private boolean selected = false;
	private ActivityResult result;
	private String msgTopic;
	private String mainMsg;
	private String dependencyMsg;
	private SAFRException exception;
	
	public MigrationComponent(EnvironmentalQueryBean component) {
		this.component = component;
	}

	/**
	 * @return the component
	 */
	public EnvironmentalQueryBean getComponent() {
		return component;
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @return the result
	 */
	public ActivityResult getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(ActivityResult result) {
		this.result = result;
	}

	/**
	 * @return the exception
	 */
	public SAFRException getException() {
		return exception;
	}

	/**
	 * @param exception the exception to set
	 */
	public void setException(SAFRException exception) {
		this.exception = exception;
	}

	/**
	 * @return the msgTopic
	 */
	public String getMsgTopic() {
		return msgTopic;
	}

	/**
	 * @param msgTopic the msgTopic to set
	 */
	public void setMsgTopic(String msgTopic) {
		this.msgTopic = msgTopic;
	}

	/**
	 * @return the mainMsg
	 */
	public String getMainMsg() {
		return mainMsg;
	}

	/**
	 * @param mainMsg the mainMsg to set
	 */
	public void setMainMsg(String mainMsg) {
		this.mainMsg = mainMsg;
	}

	/**
	 * @return the dependencyMsg
	 */
	public String getDependencyMsg() {
		return dependencyMsg;
	}

	/**
	 * @param dependencyMsg the dependencyMsg to set
	 */
	public void setDependencyMsg(String dependencyMsg) {
		this.dependencyMsg = dependencyMsg;
	}
	
	public boolean hasError() {
		if (result == null || result == ActivityResult.PASS) {
			return false;
		} else {
			return true;
		}
	}

}
