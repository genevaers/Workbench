package com.ibm.safr.we.ui.utilities;

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


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.model.utilities.ComponentData;
import com.ibm.safr.we.model.utilities.ConfirmWarningStrategy;
import com.ibm.safr.we.model.utilities.DependencyData;
import com.ibm.safr.we.model.utilities.importer.DependentComponentNode;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.dialogs.DependencyMessageDialog;
import com.ibm.safr.we.ui.dialogs.NewComponentNameDialog;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * Provides a SAFR GUI-specific JFace implementation of the
 * ConfirmWarningStrategy interface. This implementation will display a
 * MessageDialog prompt to show the warning message and request confirmation to
 * proceed (OK/Cancel).
 * 
 */
public class SAFRGUIConfirmWarningStrategy implements ConfirmWarningStrategy {
	
	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy");
	
	public enum SAFRGUIContext {
	    MODEL,
		IMPORT,
		MIGRATE
	};
	
	private class UIWarningRunnable implements Runnable {
		
		private String topic;
		private String message;
		private boolean okPressed = false;
		
		public UIWarningRunnable(
				String topic, 
				String shortMessage) {
			this.topic = topic;
			this.message = shortMessage;
		}

		public void run() {
			okPressed = showMessageDialog(topic, message);
		}
		
		public boolean isOkPressed() {
			return okPressed;
		}		
	}
	
	private class UIDepWarningRunnable implements Runnable {
		
		private String topic;
		private String shortMessage;
		private String detailMessage;
		private List<DependencyData> dependencyList;		
		private boolean okPressed = false;
		
		public UIDepWarningRunnable(
				String topic, 
				String shortMessage, 
				String detailMessage,
				List<DependencyData> dependencyList) {
			this.topic = topic;
			this.shortMessage = shortMessage;
			this.detailMessage = detailMessage;
			this.dependencyList = dependencyList;			
		}

		public void run() {
			okPressed = showDepMessageDialog(
					topic, 
					shortMessage, 
					detailMessage,
					dependencyList);				
		}
		
		public boolean isOkPressed() {
			return okPressed;
		}		
	}
	


	private class UINewNamesRunnable implements Runnable {
		
		private String topic;
		private String shortMessage;
		private DependentComponentNode dcnRoot;		
		private boolean okPressed = false;
		
		public UINewNamesRunnable(
				String topic, 
				String shortMessage,
				DependentComponentNode dcnRoot) {
			this.topic = topic;
			this.shortMessage = shortMessage;
			this.dcnRoot = dcnRoot;			
		}

		public void run() {
			okPressed = showDepMessageDialog(
					topic, 
					shortMessage, 
					dcnRoot);				
		}
		
		public boolean isOkPressed() {
			return okPressed;
		}		
	}

	// Used to indicate the caller context
	private SAFRGUIContext context = null;
	
	public SAFRGUIConfirmWarningStrategy(SAFRGUIContext context) {
		this.context = context;
	}

	protected boolean showMessageDialog(
			String topic, 
			String message) {
		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		Cursor cursor = shell.getCursor();
		shell.setCursor(null);
		MessageDialog dialog = new MessageDialog(shell, topic, null, message,
				MessageDialog.WARNING, new String[] { "&OK", "&Cancel" }, 0);
		// if OK (0) was pressed then retVal true.
		shell.setCursor(cursor);
		boolean okPressed = dialog.open() == 0 ? true : false;
		return okPressed;
	}
	
	protected boolean showDepMessageDialog(
			String topic, 
			String shortMessage, 
			String detailMessage,
			List<DependencyData> dependencyList) {
		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		.getShell();
		int confirm = 0;
		Cursor cursor = shell.getCursor();
		shell.setCursor(null);
		if (detailMessage != null) {
			confirm = DependencyMessageDialog.openDependencyDialog(shell, topic,
					shortMessage, detailMessage, MessageDialog.WARNING,
					new String[] { "&OK", "&Cancel" }, 0);			
		} else if (dependencyList != null) {
			confirm = DependencyMessageDialog.openDependencyDialog(shell, topic,
					shortMessage, dependencyList, MessageDialog.WARNING,
					new String[] { "&OK", "&Cancel" }, 0);			
		}
		shell.setCursor(cursor);
		return confirm == 0 ? true : false;
	}
	
	protected boolean showDepMessageDialog(
			String topic, 
			String shortMessage, 
			DependentComponentNode dcnRoot) {
		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		.getShell();
		int confirm = 0;
		Cursor cursor = shell.getCursor();
		shell.setCursor(null);
		confirm = NewComponentNameDialog.openDependencyDialog(shell, topic,
				shortMessage, dcnRoot, MessageDialog.WARNING,
				new String[] { "&OK", "&Cancel" }, 0);
				shell.setCursor(cursor);
				
		return confirm == 0 ? true : false;
	}
	
	protected boolean ignoreWarnings() {
		if (context != null && context == SAFRGUIContext.IMPORT) {
			if ((new SAFRPreferences()).isIgnoreImportWarnings()) {
				return true;
			}
		}
		if (context != null && context == SAFRGUIContext.MIGRATE) {
			if ((new SAFRPreferences()).isIgnoreMigrateWarnings()) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.safr.we.model.utilities.ConfirmWarningStrategy#
	 * confirmWarning(java.lang.String, java.lang.String)
	 */
	public boolean confirmWarning(String topic, String message) {
		
		// Log dependency warnings
		if (!SAFRGUIContext.MIGRATE.equals(context)) {
			// CQ10518 migration logs its own warnings
			logger.warning(message);
		}
		
		if (ignoreWarnings()) {
			return true;
		}
		
		// Show warning in the UI		
		if ( context != null && 
			(context == SAFRGUIContext.MIGRATE || context == SAFRGUIContext.IMPORT)) {
			
			UIWarningRunnable warn = new UIWarningRunnable(topic, message);
			Display.getDefault().syncExec(warn);
			return warn.isOkPressed();
		}
		else {
			return showMessageDialog(topic, message);
		}
	}

	public boolean confirmWarning(String topic, String shortMessage,
			String detailMessage) {
		
		// Log dependency warnings
		if (!SAFRGUIContext.MIGRATE.equals(context)) {
			// CQ10518 migration logs its own warnings
			SAFRLogger.logAll(logger, Level.WARNING, shortMessage + SAFRUtilities.LINEBREAK + detailMessage);		
		}
		
		if (ignoreWarnings()) {
			return true;
		}
		
		// Show warning in the UI
		if ( context != null && 
			(context == SAFRGUIContext.MIGRATE || context == SAFRGUIContext.IMPORT)) {
				
			UIDepWarningRunnable warn = new UIDepWarningRunnable(topic, shortMessage, detailMessage, null);
			Display.getDefault().syncExec(warn);
			return warn.isOkPressed();
		}
		else {
			return showDepMessageDialog(topic, shortMessage, detailMessage, null);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.ibm.safr.we.model.utilities.ConfirmWarningStrategy#
	 * confirmWarning(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean confirmWarning(String topic, String shortMessage,
			List<DependencyData> dependencyList) {
		
		// Log dependency warnings
		String logStr = shortMessage + SAFRUtilities.LINEBREAK;
		for (DependencyData dat : dependencyList) {
			logStr += dat.getComponentTypeName() + SAFRUtilities.LINEBREAK;
			for (ComponentData cdat : dat.getComponentDataList()) {
				logStr += "\t" + cdat.getComponentName() + SAFRUtilities.LINEBREAK;
			}
		}
		if (!SAFRGUIContext.MIGRATE.equals(context)) {
			// CQ10518 migration logs its own warnings
			logger.warning(logStr);
		}
			
		if (ignoreWarnings()) {
			return true;
		}
		// Show warning in the UI
		if ( context != null && 
			(context == SAFRGUIContext.MIGRATE || context == SAFRGUIContext.IMPORT)) {
				
			UIDepWarningRunnable warn = new UIDepWarningRunnable(topic, shortMessage, null, dependencyList);
			Display.getDefault().syncExec(warn);
			return warn.isOkPressed();
		}
		else {
			return showDepMessageDialog(topic, shortMessage, null, dependencyList);
		}
	}

	
	public boolean correctNamesWarning(String topic, String shortMessage,
			DependentComponentNode dcnRoot) {
		//Need a new Dialog
		// Show warning in the UI		
		if ( context != null && context == SAFRGUIContext.IMPORT ) {
			UINewNamesRunnable warn = new UINewNamesRunnable(topic, shortMessage, dcnRoot);
			Display.getDefault().syncExec(warn);
			return warn.isOkPressed();
		}
		else {
			return 	showDepMessageDialog(topic,	shortMessage, dcnRoot);				
		}
	}

}
