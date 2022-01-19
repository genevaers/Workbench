package com.ibm.safr.we.data;

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


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.DependencyUsageType;

/**
 * This class is to provide some common methods which are used by some DB2DAO
 * classes.
 * 
 */
public class DataUtilities {

    /**
     * 
     * @param val
     * @return
     */
    public static int getInt(Integer val) {
        if (val == null) {
            return 0;
        }
        else {
            return val;
        }
    }
    
	/**
	 * This method is to convert java.util.date to java .sql.date.
	 * 
	 * @param date
	 *            : Its a date of the type <code>java.util.date</code>
	 * @return a date of the type <code>java.sql.date</code>.
	 */
	public static java.sql.Date getSQLDate(Date date) {
		java.sql.Date returnDate = null;
		if (date != null) {
			Long time = date.getTime();
			returnDate = new java.sql.Date(time);
		}
		return returnDate;
	}

	/**
	 * This method is to convert a boolean value to an integer value.
	 * 
	 * @param booleanValue
	 *            : The boolean value (true or false)
	 * @return an integer equivalent to the boolean value ('1' for
	 *         <code>true</code> and '0' for <code>false</code>)
	 */
	public static int booleanToInt(boolean booleanValue) {
		int intValue;
		if (booleanValue == true) {
			intValue = 1;
		} else {
			intValue = 0;
		}
		return intValue;

	}

	/**
	 * This method is to convert a integer value to an boolean value.
	 * 
	 * @param intValue
	 *            : The integer value (0 or 1)
	 * @return a boolean value equivalent to the integer value (
	 *         <code>true</code> for '1' and <code>false</code> for '0').
	 */
	public static boolean intToBoolean(Integer intValue) {
		// CQ 8983. Nikita. 11/01/2011
		// To avoid NPE occurring if the intValue passed is null
		if (intValue == null) {
			return false;
		}
		boolean booleanValue;
		if (intValue == 1) {
			booleanValue = true;
		} else {
			booleanValue = false;
		}
		return booleanValue;
	}

	/**
	 * This method is used to create a SQL <code>Timestamp</code> object from a
	 * <code>java.util.Date</code> object.
	 * 
	 * @param date
	 *            the Date object from which Timestamp is required.
	 * @return the SQL Timestamp object.
	 */
	public static Timestamp getTimeStamp(Date date) {
		Timestamp returnStamp = null;
		if (date != null) {
			returnStamp = new Timestamp(date.getTime());
		}
		return returnStamp;
	}

	/**
	 * This method is used to trim the String. This is used to remove leading
	 * and trailing spaces from the String data retrieved from database.
	 * 
	 * @param stringToTrim
	 *            : The String to be trimmed.
	 * @return A copy of String with leading and trailing spaces removed.
	 */
	public static String trimString(String stringToTrim) {
		if (stringToTrim != null) {
			stringToTrim = stringToTrim.trim();
		}
		return stringToTrim;
	}

	/**
	 * This method is used to convert a list of <code>Integer</code> variables
	 * into a comma delimited String of all the <code>Integer</code> variables.
	 * This is used to generate the parameter list used for <code>"IN"</code>
	 * and <code>"NOT IN"</code> functions of the SQL.
	 * 
	 * @param listOfIntegerVariables
	 *            : The List of <code>Integer</code> variables.
	 * @return : A comma delimited String
	 */
	public static String integerListToString(
			Collection<Integer> listOfIntegerVariables) {
		String commaDelimitedString = "";
		if ((listOfIntegerVariables != null)
				&& (!listOfIntegerVariables.isEmpty())) {
			for (Integer integerVariable : listOfIntegerVariables) {
				commaDelimitedString += integerVariable.toString() + ",";
			}
			commaDelimitedString = commaDelimitedString.substring(0,
					commaDelimitedString.length() - 1);

		} else {
			commaDelimitedString = "0";
		}
		commaDelimitedString = "(" + commaDelimitedString + ")";

		return commaDelimitedString;

	}

	/**
	 * This method is to get a list of string chunks of certain maximum
	 * characters from a String delimited by a character. If the maximum
	 * character limit falls in between the String at position other than that
	 * of delimiter, then it creates the String chunk which ended just before
	 * the last delimiter in that maximum limit of characters. </p>This method
	 * is mostly used to get the a list of Source Id Strings for Stored
	 * Procedures which has some maximum limit of characters that it can accept.
	 * 
	 * @param srcIdString
	 *            : The String delimited by a particular character.
	 * @param maxLimit
	 *            : The maximum length of the string chunks which are to be
	 *            returned.
	 * @param delimiter
	 *            : The character which is used as delimiter in the String
	 *            provided to it.
	 * @return A list of Strings chunks of characters equal or less than the
	 *         maximum limit.
	 */
	public static List<String> getStringChunks(String srcIdString,
			int maxLimit, char delimiter) {
		List<String> subSrcIdStrings = new ArrayList<String>();
		String subSrcIdString = "";
		StringBuffer srcIdStringBuffer = new StringBuffer(srcIdString);
		while (srcIdStringBuffer.length() > 0) {
			if (srcIdStringBuffer.length() > maxLimit) {
				subSrcIdString = srcIdStringBuffer.substring(0, maxLimit);
				int pos;
				for (pos = maxLimit - 1; pos > 0; pos--) {
					if ((subSrcIdString.charAt(pos)) == delimiter) {
						break;
					}
				}
				subSrcIdString = srcIdStringBuffer.substring(0, pos);
				srcIdStringBuffer.delete(0, pos + 1);
			} else {
				subSrcIdString = srcIdStringBuffer.toString();
				srcIdStringBuffer.setLength(0);
			}
			subSrcIdStrings.add(subSrcIdString);
		}
		return subSrcIdStrings;

	}

	/**
	 * This method is to convert string "Y" or "N" into true or false.
	 * 
	 */
	public static boolean stringToBoolean(String string) {
		boolean flag = false;
		if (string == null) {
			return false;
		}
		if (string.equals("Y")) {
			flag = true;
		} else if (string.equals("N")) {
			flag = false;
		}
		return flag;
	}

	/**
	 * This method is to convert a string into its equivalent enum from
	 * ComponentType
	 * 
	 * @param string1
	 *            : The input string.
	 * @return An equivalent enum.
	 */
	public static ComponentType getComponentTypeFromString(String string1) {
		ComponentType compType = null;

		if (string1.equals("PHYSICAL FILES") || string1.equals("PARTITION")) {
			compType = ComponentType.PhysicalFile;

		} else if (string1.equals("FILE") || string1.equals("LOGICAL FILES")
				|| string1.equals("LFPIPE")) {
			compType = ComponentType.LogicalFile;

		} else if (string1.equals("LOGICAL RECORDS") || string1.equals("LR")) {
			compType = ComponentType.LogicalRecord;

		} else if (string1.equals("JOIN") || string1.equals("JOINS")) {
			compType = ComponentType.LookupPath;

		} else if (string1.equals("PROCEDURE") || string1.equals("PROCEDURES")
				|| string1.equals("EXIT")) {
			compType = ComponentType.UserExitRoutine;

		} else if (string1.equals("WRITEEXIT")) {
			compType = ComponentType.WriteUserExitRoutine;

		} else if (string1.equals("FORMATEXIT")) {
			compType = ComponentType.FormatUserExitRoutine;

		} else if (string1.equals("CREC") || string1.equals("CONTROL RECORDS")
				|| string1.equals("CONTROLREC")) {
			compType = ComponentType.ControlRecord;

		} else if (string1.equals("VFOLDER") || string1.equals("VIEWFOLDERS") || string1.equals("VIEW FOLDERS") ||
				 string1.equals("VF")) {

			compType = ComponentType.ViewFolder;
		} else if (string1.equals("VIEW") || string1.equals("VIEWS")) {
			compType = ComponentType.View;

		} else if (string1.equals("FIELD") || string1.equals("FIELDS")) {
			compType = ComponentType.LogicalRecordField;

		}
		return compType;
	}

	/**
	 * This method is used by all DAO classes to handle SQLException.
	 * 
	 * @param message
	 *            : The message depicting the error which has occurred while
	 *            database access.
	 * @param e
	 *            : The Exception object.
	 * @return A DAOException with the message and exception object.
	 */
	public static DAOException createDAOException(String message, Exception e) {
		return new DAOException(message, e);
	}

	/**
	 * This method is to convert a string into its corresponding Child Type
	 * Enum.
	 * 
	 * @param childTypeString
	 *            : The string to be converted.
	 * @return the corresponding enum.
	 */
	public static DependencyUsageType getChildTypeFromString(
			String childTypeString) {
		DependencyUsageType childType = null;
		if (childTypeString.toUpperCase().equals("SOURCE")) {
			childType = DependencyUsageType.SOURCE;
		} else if (childTypeString.toUpperCase().equals("TARGET")) {
			childType = DependencyUsageType.TARGET;
		} else if (childTypeString.toUpperCase().equals("EFFECTIVE DATE")) {
			childType = DependencyUsageType.EFFECTIVE_DATE;
		} else if (childTypeString.toUpperCase().equals("LOGIC TEXT")) {
			childType = DependencyUsageType.LOGIC_TEXT;
		} else if (childTypeString.toUpperCase().equals("SORT KEY TITLE")) {
			childType = DependencyUsageType.SORT_KEY_TITLE;
		} else if (childTypeString.toUpperCase().equals("OUTPUT")) {
			childType = DependencyUsageType.OUTPUT;
		} else if (childTypeString.toUpperCase().equals("FORMAT")) {
			childType = DependencyUsageType.FORMAT;
		} else if (childTypeString.toUpperCase().equals("WRITE")) {
			childType = DependencyUsageType.WRITE;
		} else if (childTypeString.toUpperCase().equals("READ")) {
			childType = DependencyUsageType.READ;
		} else if (childTypeString.toUpperCase().equals("LOOKUP")) {
			childType = DependencyUsageType.LOOKUP;
		} else if (childTypeString.toUpperCase().equals("NONE")) {
			childType = DependencyUsageType.NONE;
		}
		return childType;

	}
}
