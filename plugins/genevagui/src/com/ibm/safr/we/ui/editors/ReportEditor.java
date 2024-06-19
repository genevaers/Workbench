package com.ibm.safr.we.ui.editors;

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


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.EditorPart;

import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.utilities.ProfileLocation;

public class ReportEditor extends EditorPart {

    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.ReportEditor");
    
	public static String ID = "SAFRWE.ReportEditor";
	ReportEditorInput reportInput = null;

	private ScrolledForm form;
	private FormToolkit toolkit;
	private Browser browser;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)	throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(getEditorInput().getName());
		reportInput = (ReportEditorInput) getEditorInput();
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		createBrowser(parent);
		generateReportsAndBrowse();
	}

	private void createBrowser(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FormLayout());
		

		browser = new Browser(form.getBody(), SWT.NONE);
		FormData browserData = new FormData();
		browserData.left = new FormAttachment(0, 0);
		browserData.right = new FormAttachment(100, 0);
		browserData.top = new FormAttachment(0, 0);
		browserData.bottom = new FormAttachment(100, 0);
		browser.setLayoutData(browserData);
	} 

	private void generateReportsAndBrowse()  {
		try {
			reportInput.writeReportFiles(getAndMakeReportsPath());
			browser.setUrl(reportInput.getHtmlReportUrl());
		} catch (SAFRException e) {
            logger.log(Level.SEVERE, "Report failure: " + e);
		}
	}

	public static Path getAndMakeReportsPath() {
		Path reportPath  = Paths.get(SAFRPreferences.getSAFRPreferences().get(UserPreferencesNodes.REPORTS_PATH, ProfileLocation.getProfileLocation().getLocalProfile()));
		if (!reportPath.toFile().exists()) {
			reportPath.toFile().mkdirs();
		}
		return reportPath ;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		browser.dispose();
	}

	@Override
	public void setFocus() {

	}

}
