package com.ibm.safr.we.model.utilities.importer;

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


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.exceptions.SAFRValidationException;

/**
 * This class represents a metadata archive file (eg. XML) which
 * can be imported by the Import utility. It refers to the File and
 * indicates whether it is to be imported. It also stores the result
 * of the import attempt and any import errors that were returned.
 */
public class ImportFile {
	
	private File file;
	private String name;
	private InputStream stream;
	private Boolean selected = false; //TODO remove this and accessor methods if not needed.
	private ActivityResult result;
	private String errorMsg;
	private SAFRValidationException exception;
	
	public ImportFile(File file) {
		this.file = file;
	}

    public ImportFile(InputStream stream, String name) {
        this.stream = stream;
        this.name = name;
    }
	
	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * @return the selected
	 */
	public Boolean isSelected() {
		return selected;
	}
	
	/**
	 * @param selected the selected to set
	 */
	public void setSelected(Boolean selected) {
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
	 * @return the errors
	 */
	public List<String> getErrors() {
		if (exception == null) {
			//return empty list
			return new ArrayList<String>();
		} else {
			return exception.getErrorMessages();
		}
	}
	
	/**
	 * @return a String representing the file name
	 */
	public String getName() {
	    if (file != null) {
	        return file.getName();
	    }
	    else {
	        return name;
	    }
	}

	/**
	 * Returns the SAFRValidationException, if any, that caused the import of
	 * this file to fail. Typically, this will be present when there is some
	 * dependency error message relating to the failure. The exception object
	 * will contain such a message.
	 * 
	 * @return the exception
	 */
	public SAFRValidationException getException() {
		return exception;
	}

	/**
	 * @param exception the exception to set
	 */
	public void setException(SAFRValidationException exception) {
		this.exception = exception;
	}

	/**
	 * @return the errorMsg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * @param errorMsg the errorMsg to set
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

    public InputStream getStream() {
        return stream;
    }

}
