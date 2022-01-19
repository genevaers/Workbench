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
 * Represents exceptions specific to the SAFR workbench domain. It should be
 * used for SAFR application errors that need to be exposed as Java exceptions
 * to callers of the business object model (e.g. to the UI layer).
 * <p>
 * Note, only application-specific errors need to be represented by this class.
 * Other types of system level errors get thrown using the standard Java
 * exceptions and in appropriate cases these may be exposed (rethrown) by the
 * business model API. All anticipated exceptions (SAFRExceptions and standard
 * Java exceptions) should be fully documented in the API using the throws
 * clause and Javadoc.
 * 
 */
public class SAFRException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * To be used by subclasses as the line break character instead of explicit
	 * platform-dependent escaped characters like '\r' and '\n'.
	 */
	transient protected static final String LINEBREAK = System
			.getProperty("line.separator");
	
	/**
	 * Default Constructor for {@link SAFRException}. This constructor used when
	 * creating new {@link SAFRException}.
	 */
	public SAFRException() {
		super();
	}

	/**
	 * Constructor of {@link SAFRException} with message to pass along with the
	 * {@link SAFRException}.
	 * 
	 * @param message
	 *            to pass along with the {@link SAFRException}.
	 */
	public SAFRException(String message) {
		super(message);
	}

	/**
	 * Constructor of {@link SAFRException} with message to pass and
	 * {@link Throwable} cause of Exception.
	 * 
	 * @param message
	 *            to pass along with the {@link SAFRException}.
	 * @param cause
	 *            {@link Throwable} cause of the Exception.
	 */
	public SAFRException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor of {@link SAFRException} with {@link Throwable} cause of
	 * Exception.
	 * 
	 * @param cause
	 *            {@link Throwable} cause of the Exception.
	 */
	public SAFRException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 * @return boolean true if a stack trace is available.
	 */
	public boolean isTraceAvailable() {
		
		Throwable t = getCause();
		if (t != null && t.getStackTrace().length > 0) {
			return true;
		} else {
			return false;
		}
	}

}
