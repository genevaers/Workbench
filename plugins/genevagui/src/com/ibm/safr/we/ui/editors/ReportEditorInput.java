package com.ibm.safr.we.ui.editors;

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


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.ibm.safr.we.constants.ReportType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.utilities.DependencyChecker;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.ui.reports.DependencyCheckerReportData;
import com.ibm.safr.we.ui.reports.EnvironmentSecurityReportData;
import com.ibm.safr.we.ui.reports.IReportData;
import com.ibm.safr.we.ui.reports.LRReportData;
import com.ibm.safr.we.ui.reports.LookupPathReportData;
import com.ibm.safr.we.ui.reports.ViewColumnPICReport;
import com.ibm.safr.we.ui.reports.ViewColumnReport;
import com.ibm.safr.we.ui.reports.ViewPropertiesReportData;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class ReportEditorInput implements IEditorInput {

	List<Integer> reportParams = new ArrayList<Integer>();
	String dpReportParam;
	ReportType reportType;
	IReportData reportDataObject;

	public ReportEditorInput(List<Integer> reportParams, ReportType reportType,
			SAFRPersistentObject model) {
		this.reportParams = reportParams;
		this.reportType = reportType;

		try {
			if (reportType.equals(ReportType.ViewProperties)) {
				if (model == null) {
					reportDataObject = new ViewPropertiesReportData(
							reportParams);
				} else {
					reportDataObject = new ViewPropertiesReportData(
							(View) model);
					reportParams.add(((View) model).getId());
				}
			} else if (reportType.equals(ReportType.LogicalRecord)) {
				if (model == null) {
					reportDataObject = new LRReportData(reportParams);
				} else {
					reportDataObject = new LRReportData((LogicalRecord) model);
					reportParams.add(((LogicalRecord) model).getId());
				}

			} else if (reportType.equals(ReportType.LookupPath)) {
				if (model == null) {
					reportDataObject = new LookupPathReportData(reportParams);
				} else {
					reportDataObject = new LookupPathReportData(
							(LookupPath) model);
					reportParams.add(((LookupPath) model).getId());
				}

			} else if (reportType.equals(ReportType.ViewColumnReport)) {
				if (model == null) {
					reportDataObject = new ViewColumnReport(reportParams);
				} else {
					reportDataObject = new ViewColumnReport((View) model);
					reportParams.add(((View) model).getId());
				}

			} else if (reportType.equals(ReportType.ViewColumnPICReport)) {
				if (model == null) {
					reportDataObject = new ViewColumnPICReport(reportParams);
				} else {
					reportDataObject = new ViewColumnPICReport((View) model);
					reportParams.add(((View) model).getId());
				}
			} else if (reportType.equals(ReportType.EnvironmentSecurityById)) {
				if (model == null) {
					reportDataObject = new EnvironmentSecurityReportData(
							reportParams, SortType.SORT_BY_ID);
				} else {
					reportDataObject = new EnvironmentSecurityReportData(
							(Environment) model, SortType.SORT_BY_ID);
					reportParams.add(((Environment) model).getId());
				}

			} else if (reportType.equals(ReportType.EnvironmentSecurityByName)) {
				if (model == null) {
					reportDataObject = new EnvironmentSecurityReportData(
							reportParams, SortType.SORT_BY_NAME);
				} else {
					reportDataObject = new EnvironmentSecurityReportData(
							(Environment) model, SortType.SORT_BY_NAME);
					reportParams.add(((Environment) model).getId());
				}

			}
		} catch (SAFRException se) {
			UIUtilities.handleWEExceptions(se);
		}
	}

	public ReportEditorInput(String dpReportParam, ReportType reportType,
			Object reportDataObject) {
		super();
		this.dpReportParam = dpReportParam;
		this.reportType = reportType;

		if (reportType == ReportType.DependencyChecker) {
			this.reportDataObject = new DependencyCheckerReportData(
					dpReportParam, (DependencyChecker) reportDataObject);
		}
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return reportType.getReportName();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return reportType.getReportName();
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public List<Integer> getReportParams() {
		return reportParams;
	}

	public ReportType getReportType() {
		return reportType;
	}

	public IReportData getReportDataObject() {
		return reportDataObject;
	}

	public boolean equals(Object obj) {
		if (super.equals(obj))
			return true;
		if (!(obj instanceof ReportEditorInput))
			return false;
		ReportEditorInput thatInput = (ReportEditorInput) obj;
		if (!thatInput.getReportType().equals(this.reportType)) {
			return false;
		}

		if (this.reportType == ReportType.DependencyChecker) {
			// If the report is of type Dependency Checker, check if the report
			// has been requested on the same combination of Environment,
			// Component Type and Component.
			DependencyCheckerReportData thisData = (DependencyCheckerReportData) this.reportDataObject;
			DependencyCheckerReportData thatData = (DependencyCheckerReportData) thatInput.reportDataObject;
			if (thisData.getEnvironmentName().equals(
					thatData.getEnvironmentName())
					&& thisData.getComponentType().equals(
							thatData.getComponentType())
					&& thisData.getComponentName().equals(
							thatData.getComponentName())) {
				return true;
			} else {
				return false;
			}
		} else {
			// compare the report parameters
			List<Integer> thatParams = thatInput.getReportParams();
			List<Integer> thisParams = this.getReportParams();
			if (thatParams.size() != thisParams.size()) {
				return false;
			}
			boolean listEqual = thisParams.containsAll(thatParams);
			if (listEqual) {
				return true;
			} else {
				return false;
			}
		}
	}

}
