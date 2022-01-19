package com.ibm.safr.we.model.view;

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


import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;

/**
 * This class is used to create a single compilation error which can be later
 * added to {@link SAFRViewActivationException}.
 * 
 */
public class ViewActivationError {
	ViewSource viewSource;
	ViewColumn viewColumn;
	SAFRCompilerErrorType errorType;
	String errorText;

	public ViewActivationError(ViewSource viewSource, ViewColumn viewColumn,
			SAFRCompilerErrorType errorType, String errorText) {
		super();
		this.viewSource = viewSource;
		this.viewColumn = viewColumn;
		this.errorType = errorType;
		this.errorText = errorText;
	}

	/**
	 * @return the viewSource
	 */
	public ViewSource getViewSource() {
		return viewSource;
	}

	/**
	 * @param viewSource
	 *            the viewSource to set
	 */
	public void setViewSource(ViewSource viewSource) {
		this.viewSource = viewSource;
	}

	/**
	 * @return the viewColumn
	 */
	public ViewColumn getViewColumn() {
		return viewColumn;
	}

	/**
	 * @param viewColumn
	 *            the viewColumn to set
	 */
	public void setViewColumn(ViewColumn viewColumn) {
		this.viewColumn = viewColumn;
	}

	/**
	 * @return the errorType
	 */
	public SAFRCompilerErrorType getErrorType() {
		return errorType;
	}

	/**
	 * @param errorType
	 *            the errorType to set
	 */
	public void setErrorType(SAFRCompilerErrorType errorType) {
		this.errorType = errorType;
	}

	/**
	 * @return the errorText
	 */
	public String getErrorText() {
		return errorText;
	}

	/**
	 * @param errorText
	 *            the errorText to set
	 */
	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}

}
