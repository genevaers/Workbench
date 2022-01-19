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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a helper class used to calculate and report elapsed times of 
 * workbench functions, like data retrieval, store methods, validate methods
 * and so on. It's use is arbitrary, not constrained by the class. It uses
 * a start and end timestamp (or a series of start/end timestamps) issued by
 * the TimeStamper class to calculate the total elapsed time.
 * 
 */
public class TimeReporter {
	
	private String prefix = "TOTALTIME: ";
	
	Map<TimeStamper, List<Time>> contexts = new HashMap<TimeStamper, List<Time>>();	

	public TimeReporter() {
		super();
	}
	
	public TimeReporter(String prefix) {
		super();
		this.prefix = prefix;
	}

	private class Time {
		private Time (int type, Long time) {
			this.type = type;
			this.time = time;
		}
		private int type; // 0=start 1=stop
		private Long time;
	}
	
	public void reset() {
		contexts.clear();
	}
	
	public void addContext(TimeStamper ts) {
		contexts.put(ts, new ArrayList<Time>());
	}
	
	public void start(TimeStamper ts, Long time) {
		if (!contexts.containsKey(ts)) {
			addContext(ts);
		}
		contexts.get(ts).add(new Time(0, time));
	}

	public void stop(TimeStamper ts, Long time) {
		if (!contexts.containsKey(ts)) {
			return; //no-op
		}
		contexts.get(ts).add(new Time(1, time));
	}

	
	public Long calculate(TimeStamper ts) throws Exception {
		List<Time> timeList = contexts.get(ts);
		if (timeList == null) {
		    return new Long(0);
		}
		Time[] times = timeList.toArray(new Time[timeList.size()]);
		Long totalTime = new Long(0);
		
		for (int i=0; i<times.length; i+=2) {
			// list must consist of pairs of start/stop times
			if (times[i].type != 0 || times[i+1].type != 1) {
				throw new Exception("Timestamps are not in start/stop pairs, so cannot calculate total time.");
			}
			totalTime += (times[i+1].time - times[i].time);
		}
		return totalTime/1000; //In seconds
	}
	
	public String report(TimeStamper ts, String contextMsg) {
		String rpt;
		try {
			rpt = prefix + contextMsg + " " + calculate(ts).toString() + " seconds";
		} catch (Exception e) {
			rpt = prefix + e.getMessage();
		}
		return rpt;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
