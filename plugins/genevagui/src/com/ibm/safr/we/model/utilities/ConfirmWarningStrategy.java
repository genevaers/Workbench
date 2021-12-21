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


import java.util.List;


/**
 * This interface represents a strategy for reporting a warning condition to the
 * caller and obtaining confirmation to proceed or not. The WE model layer will
 * call objects that implement this interface to determine how to proceed on a
 * warning condition. Calling applications should implement this interface to
 * define their own behavior for dealing with warning messages and confirmation
 * and should then register objects of this implementation class with the
 * model layer object that needs to issue the warning.
 * 
 * @see ImportUtility.registerConfirmWarningStrategy(ConfirmWarningStrategy)
 */
public interface ConfirmWarningStrategy {

	/**
	 * Call this method to report a warning condition and determine how to
	 * proceed. Callers of the WE Model API must provide their own
	 * implementation and register this with the WE importer.
	 * 
	 * @param topic
	 *            a String describing the context of the warning message, if
	 *            required. For example, could be used as a Title when reporting
	 *            the warning message.
	 * @param message
	 *            a String that describes the warning to be reported.
	 * @return true if the response is to accept the warning and proceed,
	 *         otherwise false.
	 */
	public boolean confirmWarning(String topic, String message);

	/**
	 * Call this method to report a warning condition and determine how to
	 * proceed. Callers of the WE Model API must provide their own
	 * implementation and register this with the WE importer.
	 * 
	 * @param topic
	 *            a String describing the context of the warning message, if
	 *            required. For example, could be used as a Title when reporting
	 *            the warning message.
	 * @param shortMessage
	 *            a String that briefly describes the warning to be reported.
	 * @param detailMessage
	 *            a string that describes the warning message in detail,
	 *            supporting the 'shortMessage' parameter.
	 * @return true if the response is to accept the warning and proceed,
	 *         otherwise false.
	 */
	public boolean confirmWarning(String topic, String shortMessage,
			String detailMessage);

	public boolean confirmWarning(String topic, String shortMessage,
			List<DependencyData> dependencyList);
}
