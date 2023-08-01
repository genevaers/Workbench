package com.ibm.safr.we.ui.reports;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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
import java.util.Map;

import com.ibm.safr.we.model.query.ViewPropertiesReportQueryBean;

/************************************************************************************************************************/
/************************************************************************************************************************/

public class ViewReportData {

	private ViewPropertiesReportQueryBean view;
	private List<ViewSourceReportData> viewSources;
	
	public ViewReportData(ViewPropertiesReportQueryBean vp) {
		view = vp;
	}

	public String getPhase() {
		if(view.getViewtypecd().equals("DETL") || view.getViewtypecd().equals("SUMRY")) {
			return "Format";
		} else {
			return "Extract";
		}
	}

	public void setSorkKeys(Map<Integer, SortKeysWrapper> map) {
		// TODO Auto-generated method stub
		
	}

	public void setViewSourcesData(List<ViewSourceReportData> viewSourcesData) {
		viewSources = viewSourcesData;
	}

	public boolean isReport() {
		return view.getOutputmediacd().equals("HCOPY");
	}

	public ViewPropertiesReportQueryBean getView() {
		return view;
	}

	public List<ViewSourceReportData> getViewSources() {
		return viewSources;
	}
	
}
