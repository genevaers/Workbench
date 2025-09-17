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
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;

import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

import org.eclipse.core.runtime.Platform;

public class OpenLogFile extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Event e = (Event) event.getTrigger();
		String fileClicked = ((MenuItem) e.widget).getText();
		String prefix = '\u2022' + " &";
		// open the file in notepad
		if(fileClicked.startsWith(prefix)){
			//remove the extra prefixed chars
			fileClicked=fileClicked.substring(3);
		}
		File logFile = new File(SAFRLogger.getLogPath() + File.separatorChar
				+ fileClicked);
		try {
			String os = Platform.getOS(); 
			if (Platform.OS_MACOSX.equals(os)) {
				// Force TextEdit explicitly
				// Equivalent to: open -a TextEdit /path/to/file.txt
				Runtime.getRuntime().exec(new String[] { 
					"open", 
					"-a", 
					"TextEdit", 
					logFile.getAbsolutePath() 
				});
			}
			else {
				// Platform.OS_WIN32: use Notepad on Windows:
				Runtime.getRuntime().exec("Notepad.exe" + " " + logFile.getAbsolutePath());
			}
		} catch (IOException re) {
			UIUtilities.handleWEExceptions(re);
		}
		return null;
	}
}
