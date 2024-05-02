package com.ibm.safr.we.data;

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
 * Represents exceptions specific to the SAFR workbench data layer. Should be
 * used in cases where a connection to the back-end database has failed due to
 * authentication problems e.g. the password is wrong.
 */

public class DAOAuthorizationException extends DAOException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of {@link DAOAuthorizationException} with message to pass and
	 * {@link Throwable} cause of Exception.
	 * 
	 * @param message
	 *            to pass along with the {@link DAOAuthorizationException}.
	 * @param cause
	 *            {@link Throwable} cause of the Exception.
	 */
	public DAOAuthorizationException(String message) {
		super(message);
	}

	public DAOAuthorizationException(String message, Throwable cause) {
		super(message, cause);
	}
}
