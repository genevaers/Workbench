package com.ibm.safr.we.ui.dialogs;

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

import org.genevaers.ccb2lr.Copybook2LR;

import java.time.LocalDate;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.dialogs.AboutPluginsDialog;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.SAFREnvProp;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.view.ViewActivator;
import com.ibm.safr.we.ui.Application;
import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;

@SuppressWarnings("restriction")
public class AboutDialog extends Dialog {
	private final SAFRGUIToolkit safrGUIToolkit;

	private final String WARNING = "Warning: This computer program is protected by copyright law and international treaties. Unauthorized reproduction or distribution of this program, or any portion of it, may result in severe civil and criminal penalties, and will be prosecuted to the maximum extent possible under the law.";

	private Button pluginDetails;
	private Button okay;

	public AboutDialog(Shell parentShell) {
		super(parentShell);
		safrGUIToolkit = new SAFRGUIToolkit();
	}

	@Override
	protected Button createButton(Composite parent, int id, String label,
			boolean defaultButton) {
		return super.createButton(parent, id, label, defaultButton);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = (Composite) super.createButtonBar(parent);
		((GridLayout) composite.getLayout()).marginWidth = convertHorizontalDLUsToPixels(15);
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		pluginDetails = createButton(parent, IDialogConstants.OPEN_ID,
				"&Plug-in Details", true);

		pluginDetails.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Bundle b = FrameworkUtil.getBundle(Application.class);
				if(b.getBundleContext() != null) {
					new AboutPluginsDialog(getShell(), SAFREnvProp.DEV,
						FrameworkUtil.getBundle(Application.class)
								.getBundleContext().getBundles(),
						"Plug-in Details", "Installed plug-ins.", "").open();
				} else {
					System.out.println("Note no bundle context");
				}
			}
		});
		okay = createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		okay.setBounds(10, 10, 100, 100);

	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		getShell().setText("About " + SAFREnvProp.WORKBENCH);
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		createAboutComposite(parent);
		return parent;
	}

	protected void createAboutComposite(Composite parent) {
		Image image = UIUtilities.getAndRegisterImage(ImageKeys.ABOUT_SAFR);

		Label label = safrGUIToolkit.createLabel(parent, SWT.NONE, "");

		Composite composite = safrGUIToolkit.createComposite(parent, SWT.NULL);

		final int xposition = 5;
		label.setImage(image);
		GC gc = new GC(image);
		Rectangle bounds = image.getBounds();
		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		int y = 50;
		LocalDate today = LocalDate.now();
		String line = getAboutVersionDetails() + " : " + today.toString();
		gc.drawString(line, 15,	bounds.height - gc.getFontMetrics().getHeight()*2 - 3);
		Copybook2LR ccb2lr = new Copybook2LR();
		gc.drawString(ccb2lr.getVersion(), 15,	bounds.height - gc.getFontMetrics().getHeight() -3);
		
		gc.dispose(); // this is needed.
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		Label warning = safrGUIToolkit
				.createLabel(composite, SWT.WRAP, "");
		GridData warningData = new GridData();
		warningData.widthHint = 493;
		warning.setLayoutData(warningData);

	}
	
    public static String getAboutVersionDetails() {
        // reads the first line of version.txt
        String versionDetails = UIUtilities.getVersion();

//        String helpVersion = UIUtilities.getHelpVersion();
//        versionDetails += SAFRUtilities.LINEBREAK + "(IC " + helpVersion;
//        
//        String compilerVersion = "Sycada"; //ViewActivator.getAllCompilerVersion(); TODO sort
//        versionDetails += ", " + compilerVersion;
//
//        String storedProcVersion = SAFRApplication.getStoredProcedureVersion().replace("SD", "SD ");
//        versionDetails += ", " + storedProcVersion + ")";
//        
//        versionDetails += SAFRUtilities.LINEBREAK + "IP Asset Family Component ID 6949-17P";
//        versionDetails += SAFRUtilities.LINEBREAK + "(C) Copyright IBM Corporation 1998, 2011.";
//        versionDetails += SAFRUtilities.LINEBREAK + "All Rights Reserved.";

        return versionDetails;
    }
    
	
}
