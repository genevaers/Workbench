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


/**
 * Represents exceptions specific to the SAFR workbench domain. These exceptions
 * represent an unrecoverable situation where Workbench cannot continue. All
 * that can be done is to log the stack trace for diagnostic purposes.
 * <p>
 * Note, only application-specific errors need to be represented by this class.
 * Other types of RuntimeException errors get thrown using the standard Java
 * exceptions and in appropriate cases these may be exposed (rethrown) by the
 * business model API. All anticipated exceptions (SAFRExceptions and standard
 * Java exceptions) should be fully documented in the API using the throws
 * clause and Javadoc.
 */

public class SAFRFatalException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of {@link SAFRFatalException} with message to pass along with
	 * the {@link SAFRFatalException}.
	 * 
	 * @param message
	 *            to pass along with the {@link SAFRFatalException}.
	 */
	public SAFRFatalException(String message) {
		super(message);
	}

	/**
	 * Constructor of {@link SAFRFatalException} with {@link Throwable} cause of
	 * Exception.
	 * 
	 * @param cause
	 *            {@link Throwable} cause of the Exception.
	 */
	public SAFRFatalException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Constructs an instance which specifies an error message and wraps
	 * the cause exception.
	 * 
	 * @param message String
	 * @param cause Throwable
	 */
	public SAFRFatalException(String message, Throwable cause) {
		super(message, cause);
	}

}
