package com.ibm.safr.we.ui.editors;

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
import com.ibm.safr.we.ui.reports.ActivationReportGenerator;
import com.ibm.safr.we.ui.reports.DependencyCheckerReportData;
import com.ibm.safr.we.ui.reports.EnvironmentSecurityReportGenerator;
import com.ibm.safr.we.ui.reports.HelpReport;
import com.ibm.safr.we.ui.reports.IReportGenerator;
import com.ibm.safr.we.ui.reports.LRReportData;
import com.ibm.safr.we.ui.reports.LogicTableReportGenerator;
import com.ibm.safr.we.ui.reports.LogicalRecordReportGenerator;
import com.ibm.safr.we.ui.reports.LookupPathReportGenerator;
import com.ibm.safr.we.ui.reports.UserGroupsReportGenerator;
import com.ibm.safr.we.ui.reports.ViewColumnPICReportGenerator;
import com.ibm.safr.we.ui.reports.ViewColumnReport;
import com.ibm.safr.we.ui.reports.ViewPropertiesReportGenerator;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class ReportEditorInput implements IEditorInput {

	List<Integer> reportParams = new ArrayList<Integer>();
	String dpReportParam;
	ReportType reportType;
	IReportGenerator reportGenerator;
	String data;

	public ReportEditorInput(List<Integer> reportParams, ReportType reportType) {
		this.reportParams = reportParams;
		this.reportType = reportType;

		// I don't think the model can ever be null
		// calling function checks this
		try {
			if (reportType.equals(ReportType.ViewProperties)) {
					reportGenerator = new ViewPropertiesReportGenerator(reportParams);
			} else if (reportType.equals(ReportType.LogicalRecord)) {
					reportGenerator = new LogicalRecordReportGenerator(reportParams);
			} else if (reportType.equals(ReportType.LookupPath)) {
					reportGenerator = new LookupPathReportGenerator(reportParams);
			} else if (reportType.equals(ReportType.HelpReport)) {
				reportGenerator = new HelpReport(reportParams);
			} else if (reportType.equals(ReportType.LogicTable)) {
				reportGenerator = new LogicTableReportGenerator(reportParams);
			} else if (reportType.equals(ReportType.ActivationReport)) {
				reportGenerator = new ActivationReportGenerator(reportParams);
			} else if (reportType.equals(ReportType.ViewColumnPICReport)) {
					reportGenerator = new ViewColumnPICReportGenerator(reportParams);
			} else if (reportType.equals(ReportType.EnvironmentSecurityById)) {
					reportGenerator = new EnvironmentSecurityReportGenerator(reportParams);
			} else if (reportType.equals(ReportType.EnvironmentSecurityByName)) {
					reportGenerator = new UserGroupsReportGenerator();
			}
		} catch (SAFRException se) {
			UIUtilities.handleWEExceptions(se);
		}
	}

	public ReportEditorInput(String dpReportParam, ReportType reportType, Object reportDataObject) {
		super();
		this.dpReportParam = dpReportParam;
		this.reportType = reportType;

		if (reportType == ReportType.DependencyChecker) {
			this.reportGenerator = new DependencyCheckerReportData(dpReportParam,
					(DependencyChecker) reportDataObject);
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

	public IReportGenerator getReportDataObject() {
		return reportGenerator;
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
			DependencyCheckerReportData thisData = (DependencyCheckerReportData) this.reportGenerator;
			DependencyCheckerReportData thatData = (DependencyCheckerReportData) thatInput.reportGenerator;
			if (thisData.getEnvironmentName().equals(thatData.getEnvironmentName())
					&& thisData.getComponentType().equals(thatData.getComponentType())
					&& thisData.getComponentName().equals(thatData.getComponentName())) {
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

	public void writeReportFiles(Path path) {
		reportGenerator.writeReportFiles(path, reportType.getOutputFile());
	}

	public String getHtmlReportUrl() {
		return reportGenerator.getHtmlUrl();
	}

	public String getReportText() {
		return reportGenerator.getHtmlUrl();
	}
	
	public void setData(String d) {
		data = d;
	}
	
	public String getData() {
		return data;
	}
}
