package com.ibm.safr.we.model;

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


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * This is a utility class which is used by functions in Model layer.
 * 
 */
public class ModelUtilities {
	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.we.model.ModelUtilities");
	
	public static final Integer MAX_NAME_LENGTH = 48;
	public static final Integer MAX_COMMENT_LENGTH = 254;
	private static final String BLANK_VALUE = "";

	public static String formatDate(Date date) {
		if (date == null) {
			return BLANK_VALUE;
		} else {
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			return formatter.format(date);
		}
	}
	
	/**
	 * This method is used to generate a string of the character that is
	 * specified.The length of the string is the num specified.For eg. if the
	 * parameter is given as 2, then this method returns a string having 2
	 * spaces("  ").
	 */
	public static String genChar(int num, String character) {
		String returnVal = "";
		for (int i = 0; i < num; i++) {
			returnVal += character;
		}
		return returnVal;
	}

	/**
	 * This method is used to get the Code object from the code key.
	 * 
	 * @param codeCategory
	 *            the code category that the Code object to be retrieved belongs
	 *            to.
	 * @param codeKey
	 *            the key of the Code object to be retrieved if the code key is
	 *            not null or blank. Else, it will return null.
	 * @return the Code object
	 * @throws NullPointerException
	 *             if the code category is null.
	 */
	public static Code getCodeFromKey(String codeCategory, String codeKey) {
		if (codeCategory == null) {
			throw new NullPointerException("The Code Category cannot be null.");
		}
		if (codeKey != null && !codeKey.equals("")) {
			return SAFRApplication.getSAFRFactory().getCodeSet(codeCategory)
					.getCode(codeKey);
		} else {
			return null;
		}
	}

	/**
	 * Used by component name validation to decide if a name should be included
	 * in any error message. If this method is called during Import or Migration
	 * the name should be included so return it in single quotes for inclusion
	 * in an error message. If not, the name should only be included if it does
	 * not contain the ampersand character.
	 * <p>
	 * This is a work-around for a defect in Eclipse where the ampersand
	 * character is not being displayed correctly in hover text. See CQ10074 for
	 * details.
	 * 
	 */
	public static String formatNameForErrMsg(String name,
			boolean importOrMigration) {

		String s;
		if (name.contains("&") && !importOrMigration) {
			s = "";
		} else {
			s = "'" + name + "' ";
		}
		return s;
	}	
}
