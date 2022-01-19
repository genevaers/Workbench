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


import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.preferences.SAFRPreferences;

public class RecentViewsContributionItem extends CompoundContributionItem {

	@Override
	protected IContributionItem[] getContributionItems() {
		// get a list of views from preferences
		Preferences preferences = SAFRPreferences.getSAFRPreferences(); 
		String views = preferences.get(UserPreferencesNodes.RECENT_VIEWS,"");
		if (!views.equals("")) {
			// recent views exits. create menu items.
			// the recent views string is a ';' delimited string containing
			// recent views. Split this string and create menu items.
			String[] items = views.split(";");
			CommandContributionItemParameter param = new CommandContributionItemParameter(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
					"SAFRWE.mnuRecentViewsList", "SAFRWE.recentViewsList",
					CommandContributionItem.STYLE_PUSH);
			List<IContributionItem> mItems = new ArrayList<IContributionItem>();
			int icounter = 1;
			for (String item : items) {
				if (!item.equals("")) {
					param.label = "&" + icounter++ + " " + item;
					CommandContributionItem mItem = new CommandContributionItem(
							param);
					mItems.add(mItem);
				}
			}
			return mItems.toArray(new IContributionItem[] {});
		}
		// no views exists. return a dummy disabled menu item.
		CommandContributionItemParameter param = new CommandContributionItemParameter(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
				"SAFRWE.dummyID", "SAFRWE.dummyID",
				CommandContributionItem.STYLE_PUSH);
		param.label = "None";
		return new IContributionItem[] { new CommandContributionItem(param) };
	}

}
