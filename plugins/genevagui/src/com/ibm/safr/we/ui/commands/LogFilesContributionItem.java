package com.ibm.safr.we.ui.commands;

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


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class LogFilesContributionItem extends CompoundContributionItem {

	@Override
	protected IContributionItem[] getContributionItems() {
		// get a list of log files
		File logDir = new File(SAFRLogger.getLogPath());
		String currFile = "";
        try {
            currFile = SAFRLogger.getCurrentLogFileName();
        } catch (SAFRException e) {
            UIUtilities.handleWEExceptions(e, "", "");
        }

		CommandContributionItemParameter param = new CommandContributionItemParameter(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
				"SAFRWE.mnuLogFileList", "SAFRWE.logFileList",
				CommandContributionItem.STYLE_PUSH);
		List<IContributionItem> mItems = new ArrayList<IContributionItem>();
		for (File file : logDir.listFiles()) {
			// match WE log file pattern eg. WE.0.log or WE.0.log.1
			if (file.getName().matches("WE.*\\d\\.log(\\.\\d+){0,1}")) {
				// add to list
				if (file.getName().equals(currFile)) {
					param.label = '\u2022' + " &" + file.getName();
				} else {
					param.label = file.getName();
				}
				CommandContributionItem mItem = new CommandContributionItem(
						param);
				mItems.add(mItem);
			}
		}
		if (mItems.isEmpty()) {
			// no log files found
			// return a dummy disabled menu item.
			CommandContributionItemParameter paramD = new CommandContributionItemParameter(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
					"SAFRWE.dummyID", "SAFRWE.dummyID",
					CommandContributionItem.STYLE_PUSH);
			paramD.label = "None";
			return new IContributionItem[] { new CommandContributionItem(paramD) };
		} else {
			return mItems.toArray(new IContributionItem[] {});
		}
	}


}
