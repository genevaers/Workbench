package com.ibm.safr.we.constants;

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


/**
 * This enum is used to define the type of logic text selected.
 * 
 */
public enum LogicTextType {
	Invalid(0), Extract_Record_Filter(1), Extract_Column_Assignment(2), 
	Format_Column_Calculation(3), Format_Record_Filter(4), Extract_Record_Output(5);

	private int typeValue;

	LogicTextType(int typeValue) {
		this.typeValue = typeValue;
	}

	/**
	 * This method is used to get the integer value of the type of the logic
	 * text selected which is defined in the enum.
	 * 
	 * @return the value of the type of logic text.
	 */
	public int getTypeValue() {
		return typeValue;
	}
	
	public String getExportStr() {
        if (typeValue == 1) {
			return "EXTRACTFILTLOGIC";
		} else if (typeValue == 2) {
			return "EXTRACTCALCLOGIC";
		} else if (typeValue == 3) {
			return "FORMATCALCLOGIC";
		} else if (typeValue == 4) {
			return "FORMATFILTLOGIC";
        } else if (typeValue == 5) {
            return "EXTRACTOUTPUTLOGIC";
		} else {
			return null;
		}
	}

    public String getOpStr() {
        if (typeValue == 1) {
            return "ERF";
        } else if (typeValue == 2) {
            return "ECC";
        } else if (typeValue == 3) {
            return "FCC";
        } else if (typeValue == 4) {
            return "FRF";
        } else if (typeValue == 5) {
            return "ERL";
        } else {
            return null;
        }
    }
	
	/**
	 * This method returns the enum value {@link LogicTextType} according to
	 * string parameter specified.
	 * 
	 * @param stringValue
	 *            string value one of the ECC,FCC,FRF,ERF.
	 * @return enum {@link LogicTextType} representation of the string value.
	 */
	public static LogicTextType getEnum(String stringValue) {
		if (stringValue.equalsIgnoreCase("ECC")) {
			return LogicTextType.Extract_Column_Assignment;
		} else if (stringValue.equalsIgnoreCase("FCC")) {
			return LogicTextType.Format_Column_Calculation;
		} else if (stringValue.equalsIgnoreCase("FRF")) {
			return LogicTextType.Format_Record_Filter;
		} else if (stringValue.equalsIgnoreCase("ERF")) {
			return LogicTextType.Extract_Record_Filter;
        } else if (stringValue.equalsIgnoreCase("ERL")) {
            return LogicTextType.Extract_Record_Output;
        } else {
			throw new IllegalArgumentException(
					"Illegal argument for Logic text type");
		}
	}
	
	public static LogicTextType intToEnum(int logicTextType) {
		if (logicTextType == 0) {
			return LogicTextType.Invalid;
		} else if (logicTextType == 1) {
			return LogicTextType.Extract_Record_Filter;
		} else if (logicTextType == 2) {
			return LogicTextType.Extract_Column_Assignment;
		} else if (logicTextType == 3) {
			return LogicTextType.Format_Column_Calculation;
		} else if (logicTextType == 4) {
			return LogicTextType.Format_Record_Filter;
        } else if (logicTextType == 5) {
            return LogicTextType.Extract_Record_Output;
		} else {
			return null;
		}
	}

	
}
