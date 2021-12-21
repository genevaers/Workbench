package com.ibm.safr.we.ui;

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


import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.

	private IWorkbenchAction helpAction;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.

		IWorkbenchAction saveAction = ActionFactory.SAVE.create(window);
		register(saveAction);

		IWorkbenchAction saveAsAction = ActionFactory.SAVE_AS.create(window);
		register(saveAsAction);

		helpAction = ActionFactory.HELP_CONTENTS.create(window);
		register(helpAction);
	}

	protected void fillMenuBar(IMenuManager menuBar) {

	}

	@Override
	protected void fillStatusLine(IStatusLineManager statusLineManager) {
		super.fillStatusLine(statusLineManager);

        // add logic position 
        StatusLineContributionItem viewlogic = new StatusLineContributionItem(
            ApplicationMediator.STATUSBARLOGIC, StatusLineContributionItem.CALC_TRUE_WIDTH);
        viewlogic.setText("");
        viewlogic.setVisible(false);
        statusLineManager.add(viewlogic);       
		
        // add view length 
        StatusLineContributionItem viewlength = new StatusLineContributionItem(
            ApplicationMediator.STATUSBARVIEW, StatusLineContributionItem.CALC_TRUE_WIDTH);
        viewlength.setText("");
        viewlength.setVisible(false);
        statusLineManager.add(viewlength);       

        // add metadata counts 
        String countLineMsg = ApplicationMediator.getAppMediator().getMetaLine();
        StatusLineContributionItem countLineItem = new StatusLineContributionItem(
            ApplicationMediator.STATUSBARMETA, StatusLineContributionItem.CALC_TRUE_WIDTH);
        countLineItem.setText(countLineMsg);
        viewlength.setVisible(false);
        statusLineManager.add(countLineItem);       
		
		// add main status item
        String statusLineMsg = ApplicationMediator.getAppMediator().getStatusLine();		
		StatusLineContributionItem statusLineItem = new StatusLineContributionItem(
				ApplicationMediator.STATUSBARMAIN, StatusLineContributionItem.CALC_TRUE_WIDTH);
		statusLineItem.setText(statusLineMsg);
		statusLineManager.add(statusLineItem);
	}
}
