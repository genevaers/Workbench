package com.ibm.safr.we.ui.commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.cli.ScriptProcessor;
import com.ibm.safr.we.constants.ReportType;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.reports.ReportUtils;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.CSVWriter;
import com.ibm.safr.we.utilities.ProfileLocation;



public class ScriptHandler extends AbstractHandler{
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
        String location = "";
        
        Preferences preferences = SAFRPreferences.getSAFRPreferences(); 
        String scriptPath = preferences.get(UserPreferencesNodes.SCRIPT_PATH,"");
        if (scriptPath==null || scriptPath.equals("")) { 
            location=ProfileLocation.getProfileLocation().getLocalProfile() + "/scripts";
        }
        else {
            location=scriptPath;            
        }
        
        // make sure folders are created
        File loc = new File(location);
        File path = new File(loc.getParent());
        loc.mkdirs();
        
        // open dialog to choose folder
        FileDialog dialogLocation = new FileDialog(HandlerUtil.getActiveSite(event).getShell(), SWT.OPEN);
        dialogLocation.setText("Run Script");
        dialogLocation.setFilterPath("scripts");
        String[] exts = new String[1];
        exts[0] = "*.grs";
        dialogLocation.setFilterExtensions(exts);
        String dialogOpen = dialogLocation.open();
        
        // check dialog result, if ok pressed then export
        if (dialogOpen != null) {
            preferences.put(UserPreferencesNodes.SCRIPT_PATH, dialogOpen);
            try {
                preferences.flush();
            } catch (BackingStoreException e1) {
                UIUtilities.handleWEExceptions(e1, "Error reading script path in preferences", "");
                throw new SAFRFatalException("Error reading script path in preferences " + e1.getMessage());                    
            }       
            
            File scriptFile = new File(dialogOpen);
            ScriptProcessor.readFile(scriptFile);
            
        }
		return null;
	}
	
}
