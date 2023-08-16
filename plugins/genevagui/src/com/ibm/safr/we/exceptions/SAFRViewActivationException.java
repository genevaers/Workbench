package com.ibm.safr.we.exceptions;

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

import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewActivationError;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewSource;

/**
 * This class is used to throw all the errors generated during the view
 * activation and report it to the UI layer.
 * 
 */
public class SAFRViewActivationException extends SAFRException {

	private static final long serialVersionUID = 1L;
	View view;
    boolean errorNewOccured=false;
	List<ViewActivationError> activationLogNew = new ArrayList<ViewActivationError>();


	public SAFRViewActivationException(View view) {
		super();
		this.view = view;
	}

	/**
	 * @return the activationLog
	 */
	public List<ViewActivationError> getActivationLogNew() {
		return activationLogNew;
	}

    public void addActivationError(ViewActivationError error) {
 		errorNewOccured = true;
		activationLogNew.add(error);
	}

    public void addActivationWarning(ViewActivationError error) {
        activationLogNew.add(error);
    }
	
    public void addCompilerErrorsNew(List<String> errors, ViewSource viewSource, ViewColumn viewColumn,
            SAFRCompilerErrorType errorType) {
            for (String error : errors) {
                activationLogNew.add(new ViewActivationError(viewSource, viewColumn, errorType, error));
            }
            errorNewOccured = true;
        }
    	
	public void addCompilerWarnings(List<String> warnings, ViewSource viewSource, ViewColumn viewColumn,
			SAFRCompilerErrorType errorType) {
		for (String warning : warnings) {
			activationLogNew.add(new ViewActivationError(viewSource, viewColumn, errorType, warning));
		}
	}
	
	public View getView() {
		return view;
	}

	public boolean hasErrorOccured() {
		return  errorNewOccured;
	}

    public boolean hasNewErrorOccured() {
        return errorNewOccured;
    }
    
    public boolean hasErrorOrWarningOccured() {
        return !activationLogNew.isEmpty() ;
    }
	
}
