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

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.ui.ApplicationMediator;

/**
 * This class is used as a handler for commands to open views which are closed.
 * 
 */
public class MetaSelectHandler extends AbstractHandler implements IHandler {

	private static final String META_ENVIRONMENTS = "SAFRWE.metaEnvironments";
    private static final String META_USER_EXITS = "SAFRWE.metaUserExits";
    private static final String META_CONTROL_RECORDS = "SAFRWE.metaControlRecords";
    private static final String META_PHYSICAL_FILES = "SAFRWE.metaPhysicalFiles";
    private static final String META_LOGICAL_FILES = "SAFRWE.metaLogicalFiles";
    private static final String META_LOGICAL_RECORDS = "SAFRWE.metaLogicalRecords";
    private static final String META_LOOKUP_PATHS = "SAFRWE.metaLookupPaths";
    private static final String META_VIEWS = "SAFRWE.metaViews";
    private static final String META_VIEW_FOLDERS = "SAFRWE.metaViewFolders";
    private static final String META_USERS = "SAFRWE.metaUsers";
    private static final String META_GROUPS = "SAFRWE.metaGroups";

	public Object execute(ExecutionEvent event) throws ExecutionException {
	    
		if (event.getCommand().getId().equals(META_ENVIRONMENTS)) {
		    ApplicationMediator.getAppMediator().setNavigatorSelType(ComponentType.Environment);
		} 
        else if (event.getCommand().getId().equals(META_USER_EXITS)) {
            ApplicationMediator.getAppMediator().setNavigatorSelType(ComponentType.UserExitRoutine);
        } 
        else if (event.getCommand().getId().equals(META_CONTROL_RECORDS)) {
            ApplicationMediator.getAppMediator().setNavigatorSelType(ComponentType.ControlRecord);
        } 
        else if (event.getCommand().getId().equals(META_PHYSICAL_FILES)) {
            ApplicationMediator.getAppMediator().setNavigatorSelType(ComponentType.PhysicalFile);
        } 
        else if (event.getCommand().getId().equals(META_LOGICAL_FILES)) {
            ApplicationMediator.getAppMediator().setNavigatorSelType(ComponentType.LogicalFile);
        } 
        else if (event.getCommand().getId().equals(META_LOGICAL_RECORDS)) {
            ApplicationMediator.getAppMediator().setNavigatorSelType(ComponentType.LogicalRecord);
        } 
        else if (event.getCommand().getId().equals(META_LOOKUP_PATHS)) {
            ApplicationMediator.getAppMediator().setNavigatorSelType(ComponentType.LookupPath);
        } 
        else if (event.getCommand().getId().equals(META_VIEWS)) {
            ApplicationMediator.getAppMediator().setNavigatorSelType(ComponentType.View);
        } 
        else if (event.getCommand().getId().equals(META_VIEW_FOLDERS)) {
            ApplicationMediator.getAppMediator().setNavigatorSelType(ComponentType.ViewFolder);
        } 
        else if (event.getCommand().getId().equals(META_USERS)) {
            ApplicationMediator.getAppMediator().setNavigatorSelType(ComponentType.User);
        } 
        else if (event.getCommand().getId().equals(META_GROUPS)) {
            ApplicationMediator.getAppMediator().setNavigatorSelType(ComponentType.Group);
        } 

		return null;
	}

}
