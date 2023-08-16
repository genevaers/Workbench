package com.ibm.safr.we.ui.reports;

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


public enum LrReportStep {
	EVENT_LR("lightpink"),
	STEP1_LR("palegreen"),
	STEP2_LR("lightcyan"),
	STEP3_LR("lightblue"),
	STEP4_LR("lightskyblue"),
	STEP5_LR("lightyellow"),
	STEP6_LR("bisque"),
	STEP7_LR("gold"),
	STEP8_LR("aqua"),
	STEP9_LR("hotpink"),
	STEP10_LR("lavender"),
	STEP11_LR("lightsalmon"),
	STEP12_LR("plum"),
	STEP13_LR("yellowgreen"),
	STEP14_LR("thistle"),
	OUT_OF_RANGE("red");
	
	private String colour;
	private String lrName;

	private LrReportStep(String clr) {
		colour = clr;
	}
	
	public void setLrName(String name) {
		lrName = name;
	}
	
	public String getLrName() {
		return lrName;
	}
	
	public String getColour() {
		return colour;
	}

}
