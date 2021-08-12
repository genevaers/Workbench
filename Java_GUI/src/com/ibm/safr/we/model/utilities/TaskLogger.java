package com.ibm.safr.we.model.utilities;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.exceptions.SAFRCancelException;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.base.SAFRObject;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * This utility class is used to accumulate info, warning and error messages
 * relating to a 'task' so they can be written to the log file at one point.
 * That is, so they appear together in the log file. A 'task' can be any logical
 * grouping but typically will be a utility task like export, import, migration,
 * batch activate, etc.
 */
public class TaskLogger extends SAFRObject {
	
	transient Logger logger;
	
	private List<String> infos = new ArrayList<String>();
	private List<String> warnings = new ArrayList<String>();
	private List<String> errors = new ArrayList<String>();
	boolean noErrors = true;
	private List<SAFRException> recordedSEs = new ArrayList<SAFRException>();
	
	final private String messageSeparator = LINEBREAK;
	private String infoBanner;
	private String warningBanner;
	private String errorBanner;
	private String cancelBanner;
	private String completeErrorBanner;
	private String completeNoErrorBanner;
	
	public  TaskLogger(Logger logger, String taskName) {
		this.logger = logger;
		
		this.cancelBanner = LINEBREAK + taskName + " Cancelled on warning." + LINEBREAK;
		this.infoBanner = taskName + " Starting ";
		this.warningBanner = LINEBREAK + taskName + " Warnings ..............." + LINEBREAK;
		this.errorBanner = LINEBREAK + taskName + " Errors ..............." + LINEBREAK;
		this.completeErrorBanner = taskName + " Completed with errors.";
		this.completeNoErrorBanner = taskName + " Completed without errors.";
	}
	
	public boolean isAlreadyRecorded(SAFRException se) {
		return recordedSEs.contains(se);
	}
	
	public void clearInfos() {
		infos.clear();
	}
	
	public void clearWarnings() {
		warnings.clear();
	}
	
	public void clearErrors() {
		errors.clear();
		recordedSEs.clear();
	}
	
	public void clearAll() {
		clearInfos();
		clearWarnings();
		clearErrors();
	}
	
	// INFO messages ...
	
	public void logInfo(String topic, String contextMsg, String detailMsg) {
		if (topic != null && topic != "") {
			infos.add(LINEBREAK + topic);
		}
		if (contextMsg != null && contextMsg != "") {
			infos.add(LINEBREAK + contextMsg);
		}
		if (detailMsg != null && detailMsg != "") {
			infos.add(LINEBREAK + detailMsg);
		}
		infos.add(messageSeparator);
	}
	
	//WARNING messages ...
	
	public void logWarning(String topic, String contextMsg, String detailMsg) {
		if (topic != null && topic != "") {
			warnings.add(LINEBREAK + topic);
		}
		if (contextMsg != null && contextMsg != "") {
			warnings.add(LINEBREAK + contextMsg);
		}
		if (detailMsg != null && detailMsg != "") {
			warnings.add(LINEBREAK + detailMsg);
		}
		warnings.add(messageSeparator);
	}
	
	// CANCEL response to a warning...
	
	public void logCancelled(SAFRCancelException sce) {
		logCancelled(sce.getMessage());
	}
	
	public void logCancelled(String message) {
		warnings.add(cancelBanner);
		if (message != null && message != "") {
			warnings.add(LINEBREAK + message);
		}
		warnings.add(messageSeparator);
	}
	
	// ERROR messages ...
	
	public void logError(String topic, SAFRDependencyException sde) {
		logError(topic, sde.getContextMessage(), sde.getDependencyString(), sde);
	}
	
	public void logError(String topic, SAFRValidationException sve) {
		logError(topic, sve.getContextMessage(), sve.getMessageString(), sve);
	}
	
	public void logError(String topic, String contextMsg, String detailMsg) {
		logError(topic, contextMsg, detailMsg, null);
	}
	
	public void logError(String topic, String contextMsg,
			String detailMsg, SAFRException se) {
		if (topic != null && topic != "") {
			errors.add(LINEBREAK + topic);
		}
		if (contextMsg != null && contextMsg != "") {
			errors.add(LINEBREAK + contextMsg);
		}
		if (detailMsg != null && detailMsg != "") {
			errors.add(LINEBREAK + detailMsg);
		}
		if (se != null && !recordedSEs.contains(se)) {
			recordedSEs.add(se);
		}
		errors.add(messageSeparator);
	}
	
	// Write all info, warning, error messages to the log file ...
	
	public void writeLog() {
		writeLog(false);
	}
	
	public void writeLog(boolean endOfTask) {
		if (!infos.isEmpty()) {
			StringBuffer buffer = new StringBuffer();
			for (String line: infos) {
				buffer.append(line);
			}
			SAFRLogger.logAllSeparator(logger, Level.INFO, infoBanner + buffer.toString());
		}
		
		if (!warnings.isEmpty()) {
			StringBuffer buffer = new StringBuffer();
			for (String line : warnings) {
				buffer.append(line);
			}
			SAFRLogger.logAll(logger, Level.INFO, warningBanner + buffer.toString());
		}
		
		if (!errors.isEmpty()) {
			noErrors = false;
			StringBuffer buffer = new StringBuffer();
			for (String line : errors) {
				buffer.append(line);
			}
			SAFRLogger.logAll(logger, Level.SEVERE, errorBanner + buffer.toString());
		}
		
		if (endOfTask) {
			if (noErrors) {
			    SAFRLogger.logAll(logger, Level.INFO, completeNoErrorBanner);
			} else {
			    SAFRLogger.logAll(logger, Level.INFO, completeErrorBanner);
			}
			SAFRLogger.logEnd(logger);
		}
	}
}
