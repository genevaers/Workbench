package com.ibm.safr.we.data.transfer;

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


import com.ibm.safr.we.constants.EditRights;

public class DependentComponentTransfer extends SAFRComponentTransfer {
	EditRights editRights;
	private String dependencyInfo;
	boolean status = true; // default if not applicable

	public EditRights getEditRights() {
		return editRights;
	}

	public void setEditRights(EditRights editRights) {
		this.editRights = editRights;
	}

	public String getDependencyInfo() {
		return dependencyInfo;
	}

	public void setDependencyInfo(String dependencyInfo) {
		this.dependencyInfo = dependencyInfo;
	}

	/**
	 * Applicable only for component types which have a status attribute, namely
	 * LR, LKUP, VIEW. Returns true if status is Active, false if it is
	 * Inactive. Method should not be used for other component types.
	 */
	public boolean isActive() {
		return status;
	}

	/**
	 * Applicable only for component types which have a status attribute, namely
	 * LR, LKUP, VIEW. Specify true if status is active, false if it is
	 * inactive. Method should not be used for other component types.
	 */
	public void setActive(boolean status) {
		this.status = status;
	}

}
