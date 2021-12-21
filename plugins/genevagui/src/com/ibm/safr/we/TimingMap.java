package com.ibm.safr.we;

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


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Holds a collection of time stamps to report multiple times at once. 
 * Used to record elapsed event times inside a long running process.  
 * 
 * Must call startRecording initially to record any times. Then each different
 * part of the monitored process must call startTiming and stopTiming to 
 * record elapsed times. 
 * Then calls report to log the elapsed times of the different event types.
 * Finally call stopRecording to reset the times held in TimingMap.
 */
public class TimingMap {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.internal.data.TimingMap");
    
    private TimeReporter reporter;
    private Map<String, TimeStamper> map;
    private boolean active;
    
    /**
     * Constructs a new TimingMap, initially will be inactive until
     * startRecording is called.
     * 
     */
    public TimingMap() {
        this.reporter = new TimeReporter("");
        this.map = new HashMap<String, TimeStamper>();
        this.active = false;
    }
    
    /**
     * Starts all event time recording
     */
    public void startRecording() {
        active = true;
    }

    /**
     * Stops all event time recording
     */    
    public void stopRecording() {
        map.clear();
        active = false;
    }

    /**
     * Log all event recorded times up to this point
     * 
     * @param header - header for the timing report
     */        
    public void report(String header) {
        // report all timing results
        String report = header + SAFRUtilities.LINEBREAK;  
        for (Entry<String, TimeStamper> ent : map.entrySet()) {
            report += reporter.report(ent.getValue(), ent.getKey()) + SAFRUtilities.LINEBREAK;            
        }
        logger.log(Level.INFO, report);
    }    
    
    /**
     * Start timing an individual event
     * 
     * @param event - description of event
     */
    public void startTiming(String event) {
        if (!active) {
            return;
        }
        if (!map.containsKey(event)) {
            map.put(event, new TimeStamper(TimeStamper.NONE, reporter));
        }
        map.get(event).startStamp();
    }

    /**
     * Stop timing an individual event
     *
     * @param event - description of event
     */    
    public void stopTiming(String key) {
        if (!active) {
            return;
        }
        map.get(key).stopStamp();      
    }
    
    
}
