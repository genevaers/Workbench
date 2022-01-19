package com.ibm.safr.we.constants;

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


public enum SAFRValidationType {
	ERROR, WARNING, PARAMETER_ERROR, DEPENDENCY_PF_ASSOCIATION_WARNING, DEPENDENCY_PF_ASSOCIATION_ERROR, DEPENDENCY_LR_WARNING, DEPENDENCY_LF_ASSOCIATION_ERROR, DEPENDENCY_LR_FIELDS_ERROR, DEPENDENCY_LOOKUP_ERROR, DEPENDENCY_LOOKUP_WARNING, DEPENDENCY_EXPORT_ERROR;
	
	public boolean isDependencyError() {
		switch (this) {
		case DEPENDENCY_LOOKUP_ERROR :
		case DEPENDENCY_PF_ASSOCIATION_ERROR :
		case DEPENDENCY_LF_ASSOCIATION_ERROR :
		case DEPENDENCY_LR_FIELDS_ERROR :
		case DEPENDENCY_EXPORT_ERROR :
			return true;
		default :
			return false;
		}
	}
	
	public boolean isDependencyWarning() {
		switch (this) {
		case DEPENDENCY_PF_ASSOCIATION_WARNING :
		case DEPENDENCY_LR_WARNING :
		case DEPENDENCY_LOOKUP_WARNING :
			return true;
		default :
			return false;
		}
	}
}
