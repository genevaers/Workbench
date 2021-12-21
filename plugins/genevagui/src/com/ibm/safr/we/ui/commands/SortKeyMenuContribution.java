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


import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import com.ibm.safr.we.ui.editors.view.ViewEditor;

public class SortKeyMenuContribution extends CompoundContributionItem {
	@Override
	protected IContributionItem[] getContributionItems() {
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (!(part instanceof ViewEditor)) {
			return new IContributionItem[] { };
		}		
		ViewEditor editor = (ViewEditor) part;
		CommandContributionItemParameter param = new CommandContributionItemParameter(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
				"SAFRWE.dynamicAddRemoveSortKey", "SAFRWE.viewMakeSortKeyCol",
				CommandContributionItem.STYLE_PUSH);
		if (editor != null && editor.getCurrentColIndex() >= 0) {
			if (editor.getCurrentColumn().isSortKey()) {
				param.label = "&Make Non-Sort Key";
			} else {
				param.label = "&Make Sort Key";
			}
		} else {
			param.label = "Make Sort Key";
		}
		CommandContributionItem item = new CommandContributionItem(param);
		return new IContributionItem[] { item };
	}
}
