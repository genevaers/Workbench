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


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.MetadataView;
import com.ibm.safr.we.utilities.CSVWriter;
import com.ibm.safr.we.utilities.ProfileLocation;

public class ExportMetadataHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
	    
		IWorkbenchPart metadata = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView(MetadataView.ID);
		
		if (metadata instanceof MetadataView) {
			try {
			    ApplicationMediator.getAppMediator().waitCursor();

			    // get path from preferences
			    String location = "";
		        Preferences preferences = SAFRPreferences.getSAFRPreferences(); 
		        String metaPath = preferences.get(UserPreferencesNodes.META_EXPORT_PATH,"");
		        if (metaPath==null || metaPath.equals("")) { 
		            location=ProfileLocation.getProfileLocation().getLocalProfile() + "csv\\metadata.csv";
		        }
		        else {
		            location=metaPath;            
		        }
			    
		        // make sure folders are created
		        File loc = new File(location);
		        File path = new File(loc.getParent());
		        path.mkdirs();
		        
			    // open dialog to choose folder
			    FileDialog dialogLocation = new FileDialog(metadata.getSite().getShell(), SWT.SAVE);
			    dialogLocation.setText("Export Metadata");
                dialogLocation.setFileName(location);
                String dialogOpen = dialogLocation.open();
                
                // check dialog result, if ok pressed then export
                if (dialogOpen != null) {
                    preferences.put(UserPreferencesNodes.META_EXPORT_PATH, dialogOpen);
                    try {
                        preferences.flush();
                    } catch (BackingStoreException e1) {
                        UIUtilities.handleWEExceptions(e1, "Error storing export path in preferences", "");
                        throw new SAFRFatalException("Error storing export path in preferences " + e1.getMessage());                    
                    }       
                    
                    File exportFile = new File(dialogOpen);
                    
                    // do the export
                    List<List<String>> table = ApplicationMediator.getAppMediator().getMetaView().getTableContents();
                    
                    try {
                        CSVWriter cwriter = new CSVWriter(new FileWriter(exportFile)); 
                        for (List<String> line : table) {
                            cwriter.writeLine(line);
                        }
                        cwriter.close();
                    } catch (IOException e) {
                        UIUtilities.handleWEExceptions(e,"Error writing metadata export to file " + dialogOpen,null);                        
                    }
                }
			} finally {
                ApplicationMediator.getAppMediator().normalCursor();
			}
		}

		return null;
	}

}
