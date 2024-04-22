package com.ibm.safr.we.ui.reports;

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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ReportType;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.query.NumericIdQueryBean;
import com.ibm.safr.we.ui.dialogs.MultiErrorMessageDialog;
import com.ibm.safr.we.ui.editors.ReportEditor;
import com.ibm.safr.we.ui.editors.ReportEditorInput;
import com.ibm.safr.we.ui.editors.SAFREditorPart;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.MetadataView;

public class ReportUtils {
	private static String text;
	
	public static void openReportEditor(ReportType type) {
		List<Integer> reportIds = new ArrayList<Integer>();
		SAFRComponent model = null;
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page.getActivePart() instanceof MetadataView) {
			getIdsFromMetadataView(reportIds);
		} else if (page.getActivePart() instanceof SAFREditorPart) {
			if(type != ReportType.LogicTable && type != ReportType.ActivationReport) {
				getIdFromPageModel(reportIds, page);
			}
		} else {
			//We should say something here?
			if (type == ReportType.HelpReport || type == ReportType.LogicTable || type == ReportType.ActivationReport){
				//continue
			} else {
				return;
			}
		}
		// open the report only if a parameter is available.
		if (!reportIds.isEmpty() || model != null || type == ReportType.HelpReport || type == ReportType.LogicTable || type == ReportType.ActivationReport) {
			final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			try {
				shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
				ReportEditorInput input = new ReportEditorInput(reportIds, type);

				IEditorReference[] openEditors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.getEditorReferences();

				IEditorPart editor = null;
				for (int i = 0; i < openEditors.length; i++) {
					IEditorReference editorPart = openEditors[i];
					editor = editorPart.getEditor(false);
					if (input.equals(editor.getEditorInput())) {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editor, false);
						break;
					}
				}
				// check for errors
				String err = "";
				for (String msg : input.getReportDataObject().getErrors()) {
					err += msg + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK;
				}

				if (!err.equals("")) {
					shell.setCursor(null);
					// show errors
					MultiErrorMessageDialog.openMultiErrorMessageDialog(Display.getCurrent().getActiveShell(),
							"Error loading components",
							"Error loading below components for report. The report won't be generated for these components. See log file for details.",
							err, MessageDialog.ERROR, new String[] { "OK" }, 0);
					shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
				}

				page.openEditor(input, ReportEditor.ID);
			} catch (PartInitException e) {
				UIUtilities.handleWEExceptions(e, "Unexpected error occurred while opening report.", null);
			} finally {
				shell.setCursor(null);
			}
		}
		return;
	}

	private static void  getIdFromPageModel(List<Integer> reportIds, IWorkbenchPage page) {
		// if editor has focus, then take the model inside that editor as
		// parameter
		SAFREditorPart editorPart = (SAFREditorPart) page.getActivePart();

		// hack done to call focus out event of the current control. This
		// important as some of the WE editors save data in model on focus
		// out event of controls. This focus out is normally only called
		// after the report is generated, hence the report can't use updated
		// data.
		boolean enableState = Display.getCurrent().getFocusControl().isEnabled();
		Control currentFocus = Display.getCurrent().getFocusControl();
		try {
			Display.getCurrent().getFocusControl().setEnabled(false);
		} finally {
			currentFocus.setEnabled(enableState);
			currentFocus.forceFocus();
		}

		// refresh the model with current changes before reporting.
		// refreshModelForSaveAs is used as this will avoid some extra
		// checks done on model objects. We don't require these checks for
		// reporting.
		editorPart.refreshModelForSaveAs();
		reportIds.add( ((SAFRComponent) editorPart.getModel()).getId());
	}

	private static void getIdsFromMetadataView(List<Integer> reportIds) {
		MetadataView metadataview = (MetadataView) (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(MetadataView.ID));
		List<NumericIdQueryBean> list = metadataview.getSelectedComponents();
		if (list.isEmpty()) {
			return;
		}
		for (NumericIdQueryBean bean : list) {
			reportIds.add(Integer.valueOf(bean.getIdLabel()));
		}
	}
}
