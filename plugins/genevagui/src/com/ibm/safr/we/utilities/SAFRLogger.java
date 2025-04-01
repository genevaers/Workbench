package com.ibm.safr.we.utilities;

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


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.ErrorManager;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.preferences.OverridePreferences;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * Utility class to control SAFR logging
 */
public class SAFRLogger {

    public static final String USER = "USER";
    public static final String SEPARATOR = "================================================================================";
    public static final String NEWLINE = System.getProperty("line.separator");
    public static String currentLog = null;
    private static ErrorManager errorManager = null;    
    private static FileHandler userHandler = null;
    
    private static void addHandlers(String logPath) throws SAFRException {
    	
    	// add main handler
        final Logger logger = Logger.getLogger("");
        setupConsoleHandler(logger);
        addUserLogger(logPath);
    }

    public static void addUserLogger(String logPath) {
        if (userHandler == null) {
            final Logger logger = Logger.getLogger("");
            try {
            	SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'@'HH_mm_ss");
                Date dt = Calendar.getInstance().getTime();                            
                String pattern = logPath + "/WE." + timeFormat.format(dt) + ".%g.log";
                userHandler = new FileHandler(pattern, 1000000, 10);
            } catch (SecurityException ef) {
                UIUtilities.handleWEExceptions(ef, ef.getMessage(), "Security Exception");
            } catch (IOException ei) {
                UIUtilities.handleWEExceptions(ei, ei.getMessage(), "IO Exception");
            }
            
            // Create txt Formatter
            userHandler.setFormatter(getTraceFormatter());
            if (errorManager != null) {
            	userHandler.setErrorManager(errorManager);            
            }
            logger.addHandler(userHandler);            

            currentLog = searchLogFileName();

        }
    }

    protected static void setupConsoleHandler(final Logger logger) {
        // setup console handler
        if (logger.getHandlers().length > 0) {
            Handler consoleHandler = logger.getHandlers()[0];
            consoleHandler.setFormatter(getTraceFormatter());
        }
    }

    protected static Formatter getTraceFormatter() {
        return new Formatter () {
            @Override
            public String format(LogRecord record) {
//                if (record.getParameters() != null && record.getParameters()[0].equals(Boolean.valueOf(true))) {
                if (record.getParameters() != null ) {
                    String result =  record.getLevel() + ": " + record.getMessage() + SAFRUtilities.LINEBREAK;
                    return result;                        
                } else {
                    String tm = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(record.getMillis());
                    String result = "[" + tm + "] " + record.getLoggerName() + SAFRUtilities.LINEBREAK + 
                        record.getLevel() + ": " + record.getMessage() + SAFRUtilities.LINEBREAK;
                    if (record.getThrown() != null) {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        record.getThrown().printStackTrace(new PrintStream(buffer));
                        result += buffer.toString();
                    }
                    return result;
                }
            }            
        };
    }

    /**
     * Setup SAFR Logger
     */
    public static boolean setupLogger() throws SAFRException {
        boolean stateChanged = false;
        String logPath = getLogPath(); 
        clearOldLogFiles();   
        addHandlers(logPath);
        return stateChanged;
    }
    
    /**
     * Teardown SAFR logger
     */
    public static void teardownLogger() {
        // remove current log handlers
        final Logger logger = Logger.getLogger("");
        for (Handler handler : logger.getHandlers()) {
            handler.close();
            logger.removeHandler(handler);        	
        }    	
        userHandler = null;
    }
    
    /**
     * Determine the log path location
     * @return the log path
     */
    public static String getLogPath() {
        String userLogPath = SAFRPreferences.getSAFRPreferences().get(UserPreferencesNodes.LOG_FILE_PATH, "");
        String defaultPath = ProfileLocation.getProfileLocation().getLocalProfile() + "/logs";
        String logPath;
        
        if (userLogPath == null || userLogPath.length() == 0) {
            logPath = defaultPath;
        } else {
            if (!userLogPath.endsWith("\\") && !userLogPath.endsWith("/")) {
                userLogPath += "/";
            }
            File userLogDir = new File(userLogPath);
            if (!userLogDir.exists()) {
                logPath = defaultPath;
            } else {
                logPath = userLogPath;
            }
        }
        
        File logDir = new File(logPath);
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create logs directory: " + logPath);
            }
        }
        
        return logPath;
    }

    /**
     * Change the log path location
     * 
     * @param newLogPath - new log location
     */
    public static void changeLogPath(String newLogPath) throws SAFRException {

        if (newLogPath.equals(getLogPath())) {
            // no change so do nothing
            return;
        }
        
        // Create Logger
        File logDirs = new File(newLogPath);
        if (!logDirs.exists()) {
            if (!logDirs.mkdirs()) {
                throw new SAFRException("Invalid log path in preferences, cannot make directory " + newLogPath);
            }
        }
        
        File existingLog = new File(getLogPath() + "/" + getCurrentLogFileName());
        
        teardownLogger();
        
        // grab a copy of the current log contents
        String currLog = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(existingLog));
            String line=reader.readLine();
            if (line != null) {
                // if already changed log path skip "Original logging" line
                if (line.contains("changeLogPath")) {
                    // skip the next line
                    line = reader.readLine();
                }
                else {
                    currLog += line+SAFRUtilities.LINEBREAK;
                }
            }
            if (line != null) {
                while ((line = reader.readLine()) != null) {                 
                    currLog += line+SAFRUtilities.LINEBREAK;
                }
            }
            if (currLog.length() > 2) {
                currLog = currLog.substring(0, currLog.length()-2);                
            }
            reader.close();
                       
        } catch (FileNotFoundException ef) {
            UIUtilities.handleWEExceptions(ef, ef.getMessage(), "File Not Found");
        } catch (IOException ei) {
            UIUtilities.handleWEExceptions(ei, ei.getMessage(), "IO Exception");
        }
        existingLog.delete();
        
        // add new log handler
        addHandlers(newLogPath);
        
        // relog original log
        final Logger logger = Logger.getLogger("");        
        if (currLog.length() > 0) { 
            SAFRLogger.logAllStamp(logger, Level.INFO, "Original logging"+ SAFRUtilities.LINEBREAK + currLog);
        }
        SAFRLogger.logAllStamp(logger, Level.INFO, "Changed log path to " + newLogPath);

        // set log path in preferences
        SAFRPreferences.getSAFRPreferences().put(UserPreferencesNodes.LOG_FILE_PATH, newLogPath);
        try {
            SAFRPreferences.getSAFRPreferences().flush();
        } catch (BackingStoreException e) {
            UIUtilities.handleWEExceptions(e, e.getMessage(), "BackingStoreException");
        }        
                
    }
    
    public static void clearOldLogFiles() {
        File logPath = new File(getLogPath());
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)-10);
        Date fileCutoff = cal.getTime();
        // Delete WE.*.log and WE.*.log.lck files older than one week
        for (File file : logPath.listFiles()) {
            if (file.getName().matches("^WE.*log(\\.lck)?$")) {
                if (new Date(file.lastModified()).before(fileCutoff)) { 
                    // would like to log files deleted, but can't in this module before logger is initialized
                    // SAFRLogger.logAllSeparator(logger, Level.INFO, "Deleting old log file: [" + file.getName() + "]");
                    file.delete(); 
                }
            }
        }
        return;
    }

    /**
     * @return the current log filename
     */
    public static String getCurrentLogFileName() throws SAFRException {
        return currentLog;
    }

    public static String searchLogFileName() throws SAFRException {
        // it's dumb, but filehandler does not provide a way to get the current log file.  So we search for it to save it.
        File logPath = new File(getLogPath());
        long lastModDelta = Long.MAX_VALUE;
        long current = System.currentTimeMillis();
        File currLog = null;
        // find log file with most current modify date
        for (File file : logPath.listFiles()) {
            if (file.getName().matches("WE\\.\\d\\d\\d\\d-\\d\\d-\\d\\d@\\d\\d_\\d\\d_\\d\\d\\.[0-9]+\\.log")) {
                    long delta = current - file.lastModified(); 
                if (lastModDelta > delta) {
                    lastModDelta = delta;
                    currLog = file;
                }
            }
        }
        if (currLog != null) {
            return currLog.getName();
        }
        else {
            return "";
        }
    }
    public static String getTraceLogFileName() throws SAFRException {
        File logPath = new File(getLogPath());
        
        long lastModDelta = Long.MAX_VALUE;
        long current = System.currentTimeMillis();
        File currLog = null;
        // find log file with most current modify date
        for (File file : logPath.listFiles()) {
            if (file.getName().matches("Trace\\.\\d+\\.log(\\.\\d+){0,1}")) {
                long delta = current - file.lastModified(); 
                if (lastModDelta > delta) {
                    lastModDelta = delta;
                    currLog = file;
                }
            }
        }
        if (currLog != null) {
            return currLog.getName();
        }
        else {
            return "";
        }
    }
    
    /**
     * Set an error handler for the logger
     */    
    public static void setErrorManager(ErrorManager em) {
        errorManager = em;
    }

    public static void logEnd(Logger devLogger) {
        devLogger.log(Level.INFO, SAFRUtilities.LINEBREAK);
    }

    public static void logAll(Logger devLogger, Level level, String message) {
        devLogger.log(level, message, true);
    }
    
    public static void logAllStamp(Logger devLogger, Level level, String message, Throwable ex) {
        devLogger.log(level, message, ex);
    }

    public static void logAllStamp(Logger logger, Level info, String string) {
        logAllStamp(logger, info, string, null);
    }
    
    public static void logAllSeparator(Logger logger, Level info, String string) {
        String lstring = SEPARATOR + SAFRUtilities.LINEBREAK + string;
        logAllStamp(logger, info, lstring);
    }
    
}
