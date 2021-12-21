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


import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.utilities.SAFRLogger;

public class LogErrorManager extends ErrorManager {

    @Override
    public void error(String message, Exception exception, int errorCode) {
        
        // ignore close errors
        if (errorCode == ErrorManager.CLOSE_FAILURE) {
            return;
        }
        
        // Problem occured with the current log path so reset it
        try {
            String oldLog = SAFRLogger.getLogPath();   
            SAFRLogger.teardownLogger();
            SAFRLogger.setupLogger();
            final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.utilities.LogErrorManager");        
            
            MessageDialog.openError(Display.getCurrent()
                    .getActiveShell(), "Log Changed",
                    "Log path " + oldLog + " is now invalid, using " + SAFRLogger.getLogPath());
            logger.log(Level.WARNING, "Log path " + oldLog + 
                    " is now invalid, using " + SAFRLogger.getLogPath());
            
        } catch (SAFRException e) {
            UIUtilities.handleWEExceptions(e,"Resetting log " + SAFRLogger.getLogPath() +" failed, ","Log Failure");
        }
    }

}
