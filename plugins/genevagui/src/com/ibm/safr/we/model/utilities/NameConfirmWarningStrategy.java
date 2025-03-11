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


import com.ibm.safr.we.model.utilities.importer.DependentComponentNode;


/**
 * This interface represents a strategy for reporting a warning condition to the
 * caller and obtaining confirmation to proceed or not. The WE model layer will
 * call objects that implement this interface to determine how to proceed on a
 * warning condition. Calling applications should implement this interface to
 * define their own behavior for dealing with warning messages and confirmation
 * and should then register objects of this implementation class with the
 * model layer object that needs to issue the warning.
 * 
 */
public interface NameConfirmWarningStrategy extends ConfirmWarningStrategy {

	public boolean correctNamesWarning(String topic, String shortMessage,
			DependentComponentNode dcnRoot);
}
