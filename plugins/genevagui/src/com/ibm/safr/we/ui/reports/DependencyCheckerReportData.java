package com.ibm.safr.we.ui.reports;

import java.nio.file.Path;

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

import com.ibm.safr.we.model.utilities.DependencyChecker;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class DependencyCheckerReportData implements IReportGenerator {
	private String treeString;
	private DependencyChecker dpChecker;

	public DependencyCheckerReportData(String treeString,
			DependencyChecker dpChecker) {
		super();
		this.treeString = treeString;
		this.dpChecker = dpChecker;

	}

	public String getEnvironmentName() {
		return UIUtilities.getComboString(dpChecker.getEnvironmentName(),
				dpChecker.getEnvironmentId());
	}

	public String getTreeString() {
		return treeString;
	}

	public String getComponentName() {
		return UIUtilities.getComboString(dpChecker.getComponentName(),
				dpChecker.getComponentId());
	}

	public String getComponentType() {
		return dpChecker.getComponentType().getLabel();
	}

	public List<String> getErrors() {
		return null;
	}

	public boolean hasData() {
		return false;
	}

	@Override
	public String getHtmlUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeReportFiles(Path path, String bsseName) {
		// TODO Auto-generated method stub
		
	}

}
