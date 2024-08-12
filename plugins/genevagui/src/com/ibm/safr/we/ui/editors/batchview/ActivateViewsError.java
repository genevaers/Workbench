package com.ibm.safr.we.ui.editors.batchview;

import java.nio.file.Path;
import java.nio.file.Paths;

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


import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.utilities.BatchComponent;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.utilities.ProfileLocation;

public class ActivateViewsError {
    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.batchview.ActivateViewsErrors");
    
    private ActivateViewsMediator mediator;

    // section errors
    private Composite parent;
    private Section sectionErrors;
    private Composite compositeErrors;

	private Browser browser;
        
    public ActivateViewsError(ActivateViewsMediator mediator, Composite parent) {
        super();
        this.mediator = mediator;
        this.parent = parent;
    }
    
    protected void create() {
        
        sectionErrors = mediator.getGUIToolKit().createSection(parent, Section.TITLE_BAR
            | Section.TWISTIE, "Show Results");
        FormData dataErrors = new FormData();
        dataErrors.left = new FormAttachment(mediator.getTableSection(), 5);
        dataErrors.top = new FormAttachment(mediator.getEnvironmentSection(), 10);
        dataErrors.bottom = new FormAttachment(100, 0);
        dataErrors.right = new FormAttachment(100, -5);
        sectionErrors.setLayoutData(dataErrors);
    
        
        compositeErrors = mediator.getGUIToolKit().createComposite(sectionErrors, SWT.BORDER);
        compositeErrors.setLayout(new FormLayout());
		FormData errsData = new FormData();
		errsData.left = new FormAttachment(0, 0);
		errsData.right = new FormAttachment(100, 0);
		errsData.top = new FormAttachment(0, 0);
		errsData.bottom = new FormAttachment(100, 0);
		compositeErrors.setLayoutData(errsData);
    
        createBrowser(compositeErrors);
        
        showSectionLoadErrors(false);
        sectionErrors.setClient(compositeErrors);
        
    }
    
    /**
     * This method is used to display the errors section expanded or enabled
     * based upon the value passed
     * 
     * @param visible
     */
    protected void showSectionLoadErrors(Boolean visible) {
        BatchComponent view = mediator.getSelectedView();
        if (view != null) {
            if(view.getResult() == ActivityResult.LOADERRORS) {
                browser.setText("<pre>"+view.getException().toString()+"</pre>");                          
            } else {
            	Path reportPath  =Paths.get(SAFRPreferences.getSAFRPreferences().get(UserPreferencesNodes.REPORTS_PATH, ProfileLocation.getProfileLocation().getLocalProfile()));
        		Path htmlPath = reportPath.resolve("html");
        		Path viewName = htmlPath.resolve("ActReport_Env" + mediator.getCurrentEnvironment().getId() +"_V" + view.getComponent().getId()+ ".html");
    			browser.setUrl(viewName.toString());
            }
        } else {
            browser.setText("Tell about the dependency issues");          
        }
        sectionErrors.setExpanded(visible);
        sectionErrors.setEnabled(visible);
    }

    protected void clearErrors() {
		browser.setText("");
    }

	private void createBrowser(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		FormData browserData = new FormData();
		browserData.left = new FormAttachment(0, 0);
		browserData.right = new FormAttachment(100, 0);
		browserData.top = new FormAttachment(0, 0);
		browserData.bottom = new FormAttachment(100, 0);
		browser.setLayoutData(browserData);
	} 

    
}
