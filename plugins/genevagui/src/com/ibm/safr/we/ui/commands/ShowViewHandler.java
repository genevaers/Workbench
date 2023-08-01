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


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.logic.LogicTextView;
import com.ibm.safr.we.ui.views.metadatatable.MetadataView;
import com.ibm.safr.we.ui.views.navigatortree.NavigatorView;
import com.ibm.safr.we.ui.views.vieweditor.ActivationLogViewNew;
import com.ibm.safr.we.ui.views.vieweditor.ColumnSourceView;
import com.ibm.safr.we.ui.views.vieweditor.DataSourceView;
import com.ibm.safr.we.ui.views.vieweditor.SortKeyTitleView;
import com.ibm.safr.we.ui.views.vieweditor.SortKeyView;

/**
 * This class is used as a handler for commands to open views which are closed.
 * 
 */
public class ShowViewHandler extends AbstractHandler implements IHandler {

	private static final String METADATAVIEW = "SAFRWE.showMetadataView";
	private static final String NAVIGATORVIEW = "SAFRWE.ShowNavigatorView";
	private static final String COLUMN_SOURCE_VIEW = "SAFRWE.ColumnSourceView";
	private static final String SORT_KEY_VIEW = "SAFRWE.SortKeyView";
	private static final Object DATA_SOURCE_VIEW = "SAFRWE.DataSourceView";
	private static final Object SORT_KEY_TITLES = "SAFRWE.SortKeyTitleView";
	private static final Object VIEW_ACTIVATION_LOG_NEW = "SAFRWE.ActivationLogViewNew";
	private static final Object LOGIC_TEXT_HELPER = "SAFRWE.LogicTextView";;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			if (event.getCommand().getId().equals(NAVIGATORVIEW)) {
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
						.showView(NavigatorView.ID);
			} else if (event.getCommand().getId().equals(METADATAVIEW)) {
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
						.showView(MetadataView.ID);
			} else if (event.getCommand().getId().equals(COLUMN_SOURCE_VIEW)) {
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
						.showView(ColumnSourceView.ID);
			} else if (event.getCommand().getId().equals(SORT_KEY_VIEW)) {
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
						.showView(SortKeyView.ID);
			} else if (event.getCommand().getId().equals(DATA_SOURCE_VIEW)) {
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
						.showView(DataSourceView.ID);
			} else if (event.getCommand().getId().equals(SORT_KEY_TITLES)) {
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
						.showView(SortKeyTitleView.ID);
			} else if (event.getCommand().getId().equals(VIEW_ACTIVATION_LOG_NEW)) {
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
						.showView(ActivationLogViewNew.ID);
			} else if (event.getCommand().getId().equals(LOGIC_TEXT_HELPER)) {
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
						.showView(LogicTextView.ID);
			}

		} catch (PartInitException e) {
			UIUtilities.handleWEExceptions(e,"Unexpected error occurred while opening a view.", null);
		}

		return null;
	}

}
