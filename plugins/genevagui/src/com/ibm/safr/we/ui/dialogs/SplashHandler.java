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


import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.splash.EclipseSplashHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.security.UserSession;
import com.ibm.safr.we.ui.utilities.ImageKeys;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

@SuppressWarnings("restriction")
/*
 * A class that adds version number and description details to the Splash
 * Screen. CQ8895
 */
public class SplashHandler extends EclipseSplashHandler {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.ui.dialogs.SplashHandler");
	
	private Image image;

	public SplashHandler() {
		super();
	}

	@Override
	public void init(Shell splash) {
		super.init(splash);

		ImageDescriptor descriptor = AbstractUIPlugin
				.imageDescriptorFromPlugin("GenevaERS", ImageKeys.ABOUT_SAFR);
		if (descriptor != null)
			image = descriptor.createImage();

		if (image != null) {
			final int xposition = 20;
			getContent().addPaintListener(new PaintListener() 
				{
				public void paintControl(PaintEvent e) {
					e.gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));				    
					int y=450;
					int height = e.gc.getFontMetrics().getHeight();
					if (height > 20) {
					    y=400;
					}
					for (String line : UIUtilities.getVersionDetails().split("\n"))
						e.gc.drawString(line, xposition, y += e.gc.getFontMetrics().getHeight()+3);
				
				}
			});
		}

		// logon should be called after this point so it doesn't interrupt the
		// splash screen
		SAFRConnectionManager CManager = new SAFRConnectionManager(Display
				.getCurrent().getActiveShell());
		// Check whether connection settings are already saved.
		try {
			if (!(SAFRPreferences.getSAFRPreferences()
					.nodeExists(UserPreferencesNodes.SAVED_CONNECTION))) {
				if (CManager.open() == IDialogConstants.CANCEL_ID) {
					WorkbenchPlugin.unsetSplashShell(Display.getCurrent());
					System.exit(0);
				}
			}
		} catch (BackingStoreException e) {
            logger.log(Level.SEVERE, "Failure to save preferences", e);
		}

		// check for passed in connection parameters
		String user=null;
        String pass=null;
        String env=null;
        String grp=null;
		String args[]=  Platform.getCommandLineArgs();
		int i=0;
		while (i < args.length-1) {
		    if (args[i].equalsIgnoreCase("-usr")) {
                i++;
		        user = args[i++];
		        continue;
		    }
		    else if (args[i].equalsIgnoreCase("-pass")) {
                i++;
                pass = args[i++];
                continue;
            }
            else if (args[i].equalsIgnoreCase("-env")) {
                i++;
                env = args[i++];
                continue;
            }
            else if (args[i].equalsIgnoreCase("-grp")) {
                i++;
                grp = args[i++];
                continue;
            }
		    i++;
		}
		
		if (user == null || env==null) {
			// do the login before loading the workbench
			SAFRLogin login = new SAFRLogin(Display.getCurrent().getActiveShell());
			int returnCode;
			returnCode = login.open();
			if (returnCode == IDialogConstants.CANCEL_ID) {
				// user canceled login
				WorkbenchPlugin.unsetSplashShell(Display.getCurrent());
				System.exit(0);
			}			
		}
		else {
			// grab settings from parameters
			try {
				login(user, pass, env, grp);
			} catch (Exception e) {
				UIUtilities.handleWEExceptions(e,"Error logging in.", null);				
				System.exit(0);
			}
		}
		
	}

	public boolean login(String userID, String pass, String env, String group) throws SAFRException {
	    SAFRLogger.logAllStamp(logger, Level.INFO, "====== START GenevaERS WORKBENCH SESSION ======");
	    SAFRLogger.logAllStamp(logger, Level.INFO, "Workbench Eclipse (WE) " + UIUtilities.getVersionDetails().split("\n")[0]);
		
		// Check if the supplied user exists and the password is correct
		User currentUser = null;
		currentUser = SAFRApplication.getSAFRFactory().getUser(userID.toUpperCase());

		// load all codes from codetable
		SAFRApplication.getSAFRFactory().getAllCodeSets();

		// log stored procedure version
		SAFRLogger.logAllStamp(logger, Level.INFO, "Stored Procedure Version is " + SAFRApplication.getStoredProcedureVersion());
		
		// to avoid null pointer exception when group not selected.
		Group currentGroup = null;
		if (group != null && group != "") {
			currentGroup = SAFRApplication.getSAFRFactory().getGroup(group);
		}

		Environment currentEnv = SAFRApplication.getSAFRFactory().getEnvironment(env);

		SAFRApplication.setUserSession(new UserSession(currentUser,
				currentEnv, currentGroup));
		
		// log the SAFR user, Environment and Group
		StringBuffer buffer = new StringBuffer();
		buffer.append("GenevaERS Login Details:");
		buffer.append(SAFRUtilities.LINEBREAK + "GenevaERS Userid  "
				+ currentUser.getUserid());
		buffer.append(SAFRUtilities.LINEBREAK + "Environment  "
				+ currentEnv.getDescriptor());
		if (currentUser.isSystemAdmin()) {
			buffer.append(SAFRUtilities.LINEBREAK
					+ "Group        [not applicable]");
		} else {
			buffer.append(SAFRUtilities.LINEBREAK + "Group        "
					+ currentGroup.getDescriptor());
		}
		if (currentUser.isSystemAdmin()) {
			buffer.append(SAFRUtilities.LINEBREAK + "Authority    System administrator");
		} else if (SAFRApplication.getUserSession()
				.isEnvironmentAdministrator()) {
			buffer.append(SAFRUtilities.LINEBREAK + "Authority    Environment administrator");
		} else {
			buffer.append(SAFRUtilities.LINEBREAK + "Authority    Normal user");
		}
		SAFRLogger.logAllStamp(logger, Level.INFO, buffer.toString());

		String version= "Sycada Version 0.0.1"; //Try to automate this from Gradle build
		if (version != null)
		{
		    SAFRLogger.logAll(logger, Level.INFO, "Compiler version is " + version);
		}
		
		return true;
	}
	
	
	@Override
	public void dispose() {
		super.dispose();
		if (image != null)
			image.dispose();
	}

}
