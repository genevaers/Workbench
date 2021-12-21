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


import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * Writes timestamp tracing to log file, system console or both.
 * Use the stamp() methods to simply output timestamps.
 * Use startStamp() and stopStamp() methods with TimeReporter to 
 * calculate total time from multiple start/stop timestamp pairs.
 * See the main() method for test cases that demonstrate the pgm model.
 */
public class TimeStamper {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.TimeStamper");

	private static final String eyecatcher = "TIMESTAMP: ";

	public static final int NONE = 0;
	public static final int LOG = 1;
	public static final int CONSOLE = 2;
	public static final int LOG_CONSOLE = 3;

	private int destination;
	
	private TimeReporter reporter = null;

	public TimeStamper() {
		destination = NONE;
	}
	
	public TimeStamper(TimeReporter reporter) {
		this();
		this.reporter = reporter;
	}

	public TimeStamper(int destination) {
		this.destination = destination;
	}

	public TimeStamper(int destination, TimeReporter reporter) {
		this(destination);
		this.reporter = reporter;
	}

	private Long timestamp() {
		return new Long(new Date().getTime());
	}

	public String stamp() {
		String s = eyecatcher + timestamp();
		writeStamp(s);
		return s;
	}

	public String stamp(String label) {
		String s = eyecatcher + timestamp() + ": " + label;
		writeStamp(s);
		return s;
	}

	public String stamp(String label, String label2) {
		// e.g. use label for 'start' or 'stop' and label2 for method name
		String s = eyecatcher + timestamp() + ": " + label + ": " + label2;
		writeStamp(s);
		return s;
	}

	public String startStamp() {
		Long ts = timestamp();
		String s = eyecatcher + "START " + ts;
		writeStamp(s);
		reporter.start(this, ts);
		return s;
		
	}

	public String startStamp(String label) {
		Long ts = timestamp();
		String s = eyecatcher + "START " + ts + ": " + label;
		writeStamp(s);
		reporter.start(this, ts);
		return s;
		
	}

	public String startStamp(String label, String label2) {
		Long ts = timestamp();
		String s = eyecatcher + "START " + ts + ": " + label + ": " + label2;
		writeStamp(s);
		reporter.start(this, ts);
		return s;
		
	}

	public String stopStamp() {
		Long ts = timestamp();
		String s = eyecatcher + "STOP " + ts;
		writeStamp(s);
		reporter.stop(this, ts);
		return s;
		
	}

	public String stopStamp(String label) {
		Long ts = timestamp();
		String s = eyecatcher + "STOP " + ts + ": " + label;
		writeStamp(s);
		reporter.stop(this, ts);
		return s;
		
	}

	public String stopStamp(String label, String label2) {
		Long ts = timestamp();
		String s = eyecatcher + "STOP " + ts + ": " + label + ": " + label2;
		writeStamp(s);
		reporter.stop(this, ts);
		return s;
		
	}
	
	private void writeStamp(String s) {
		switch (destination) {
		case NONE:
			break;
		case LOG:
		    SAFRLogger.logAll(logger, Level.INFO, s);
			break;
		case CONSOLE:
			System.out.println(s);
			break;
		case LOG_CONSOLE:
		    SAFRLogger.logAll(logger, Level.INFO, s);
			System.out.println(s);
			break;
		default:
			String e = "TimeStamper error - invalid destination '" + destination + "'.";
			logger.info(e);
			System.out.println(e);
		}
	}
	
	public static void main(String[] args) throws Exception {

		// ===== test basic timestamping ===
		TimeStamper ts;

		// default output is NONE
		ts = new TimeStamper();
		ts.stamp("Default NONE 0");
		ts.stamp();
		ts.stamp("start", "0");
		ts.stamp("stop", "0");

		// output to log file
		ts = new TimeStamper(TimeStamper.LOG);
		ts.stamp("LOG 1");
		ts.stamp();
		ts.stamp("start", "1");
		ts.stamp("stop", "1");

		// output to system console
		ts = new TimeStamper(TimeStamper.CONSOLE);
		ts.stamp("CONSOLE 2");
		ts.stamp();
		ts.stamp("start", "2");
		ts.stamp("stop", "2");

		// output to log file and system console
		ts = new TimeStamper(TimeStamper.LOG_CONSOLE);
		ts.stamp("LOG_CONSOLE 3");
		ts.stamp();
		ts.stamp("start", "3");
		ts.stamp("stop", "3");
		
		ts = new TimeStamper(42);
		ts.stamp("Invalid destination 42");
		
		// ===== test timestamp accumulation using TimeReporter =======
		
		TimeReporter reporter = new TimeReporter();
		TimeStamper stamper = new TimeStamper(TimeStamper.CONSOLE, reporter);
		reporter.addContext(stamper);
		// Do start/stop timestamps wasting some time in between
		for (int j = 0; j < 10; j++) {
			stamper.startStamp("Start timing now");
			for (int i = 0; i < 10000; i++) {
				stamper.timestamp();
			}
			stamper.stopStamp("Stop timing now");
		}
		String report = reporter.report(stamper, "The total elapsed time.");
		System.out.println(report);
		
	}
}
