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


import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.StatusHandler;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.window.Window.IExceptionHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.ui.utilities.LogErrorManager;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;


/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	public static final String PLUGIN_ID = "GenevaERS";
	private Logger logger;  
	
	public Object start(IApplicationContext context) throws Exception {

		// The following root logger setup will be used for WE 4.10. Later
		// releases of WE will use a log4j implementation. WE 4.10 will
		// use this basic Java logging implementation, where the log file
		// is cycled through 3 iterations of 1Mb each.

		// The log files will appear under:
		// @user.home/Application data/SAFR/Workbench
		// Eclipse/workspace/.metadata

		
		Display display = PlatformUI.createDisplay();
		
		// try block for log file setup
		try {
	        // Setup Logger
	        SAFRLogger.setErrorManager(new LogErrorManager());
	        String oldLog = SAFRLogger.getLogPath();
	        boolean stateChanged =  SAFRLogger.setupLogger();
	        logger = Logger.getLogger("com.ibm.safr.we.ui.Application");        
	        if (stateChanged) {
	            logger.log(Level.WARNING, "An invalid log " + oldLog + 
	                    " was entered in preferences, now using " + SAFRLogger.getLogPath());
	            MessageDialog.openError(Display.getCurrent()
	                    .getActiveShell(), "Log Changed",
	                    "An invalid log " + oldLog
	                    + " was entered in preferences, now using " + SAFRLogger.getLogPath());
	        }
	        
	        SAFRLogger.logAllSeparator(logger, Level.INFO, "Started Workbench Session " + UIUtilities.getVersionDetails().split("\n")[0]);
	        SAFRLogger.logAllSeparator(logger, Level.INFO, "JVM " + System.getProperty("java.version"));
	        SAFRLogger.logAllSeparator(logger, Level.INFO, "Java vendor " + System.getProperty("java.vendor.url"));
	        SAFRLogger.logAllSeparator(logger, Level.INFO, "VM name " + System.getProperty("java.vm.name"));
	        SAFRLogger.logAllSeparator(logger, Level.INFO, "Java runtime " + System.getProperty("java.runtime.name"));
	        SAFRLogger.logAllSeparator(logger, Level.INFO, "Java runtime version " + System.getProperty("java.runtime.version"));
	        
	        int returnCode;
	        
	        // try block for WE setup
	        try {

	            Window.setExceptionHandler(new IExceptionHandler() {

	                public void handleException(Throwable t) {
	                    logger.log(Level.SEVERE, "An unexpected error occurred:", t);
	                    MessageDialog.openError(Display.getCurrent()
	                            .getActiveShell(), "Unexpected Workbench Error",
	                            "An unexpected error occurred: " + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK + t.toString()
	                                    + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK + "Check log file for details.");
	                }
	            });
	            // add a custom status handler to handle JFace errors.
	            Policy.setStatusHandler(new StatusHandler() {

	                @Override
	                public void show(IStatus status, String title) {
	                    logger.log(Level.SEVERE, "An unexpected error occurred:",
	                            status.getException());
	                    MessageDialog.openError(Display.getCurrent()
	                            .getActiveShell(), "Unexpected Workbench Error",
	                            "An unexpected error occurred: " + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK
	                                    + status.getException().toString()
	                                    + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK + "Check log file for details.");
	                }
	            });
	            returnCode = PlatformUI.createAndRunWorkbench(display,
	                    new ApplicationWorkbenchAdvisor());
	            if (returnCode == PlatformUI.RETURN_RESTART) {
	                return IApplication.EXIT_RESTART;
	            }
	            SAFRLogger.logAllStamp(logger, Level.INFO, "Stopped Workbench Session " + UIUtilities.getVersionDetails().split("\n")[0]);
	            return IApplication.EXIT_OK;
	        } catch (Exception e) {
	            logger.log(Level.SEVERE, "Unexpected Workbench Error:", e);
	        } finally {
	            UIUtilities.disposeImages();
	            display.dispose();
	        }
		    	
	    // no log file to write to
	    } catch (Exception e) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Log Access Failed",
                    "Failed to access log file " + e.getMessage());	        
	    }
        SAFRLogger.logAllStamp(logger, Level.INFO, "Stopped Workbench " + UIUtilities.getVersionDetails().split("\n")[0]);
		return IApplication.EXIT_OK;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {

		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
