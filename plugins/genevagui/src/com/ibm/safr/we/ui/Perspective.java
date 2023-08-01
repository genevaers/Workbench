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


import java.util.logging.Logger;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

import com.ibm.safr.we.ui.editors.CompareTextView;
import com.ibm.safr.we.ui.views.logic.LogicTextView;
import com.ibm.safr.we.ui.views.metadatatable.MetadataView;
import com.ibm.safr.we.ui.views.navigatortree.NavigatorView;
import com.ibm.safr.we.ui.views.vieweditor.ActivationLogViewNew;
import com.ibm.safr.we.ui.views.vieweditor.ColumnSourceView;
import com.ibm.safr.we.ui.views.vieweditor.DataSourceView;
import com.ibm.safr.we.ui.views.vieweditor.SortKeyTitleView;
import com.ibm.safr.we.ui.views.vieweditor.SortKeyView;

public class Perspective implements IPerspectiveFactory {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.ui.Perspective");
	public static String viewProperties = "VIEW_PROPERTIES";
	public static String helperViews = "HELPER_VIEWS";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		// property views and logic text helper view.
		IPlaceholderFolderLayout folderLayout = layout.createPlaceholderFolder(
				viewProperties, IPageLayout.RIGHT, 0.75f, editorArea);
		folderLayout.addPlaceholder(SortKeyView.ID);
		folderLayout.addPlaceholder(ColumnSourceView.ID);
		folderLayout.addPlaceholder(LogicTextView.ID);

		// IPlaceholderFolderLayout folderLayout1 = layout
		// .createPlaceholderFolder(viewProperties1, IPageLayout.BOTTOM,
		// 0.75f, viewProperties);

		// SAFRExplorer view
		layout.addView(NavigatorView.ID, IPageLayout.LEFT, 0.20f, editorArea);
		// Metadata view and other views dependent on View editor..
		IFolderLayout folderLayout2 = layout.createFolder(helperViews,
				IPageLayout.BOTTOM, 0.65f, editorArea);
		folderLayout2.addView(MetadataView.ID);
        folderLayout2.addPlaceholder(ActivationLogViewNew.ID);
		folderLayout2.addPlaceholder(DataSourceView.ID);
		folderLayout2.addPlaceholder(SortKeyTitleView.ID);
        folderLayout2.addPlaceholder(CompareTextView.ID);
	}

}
