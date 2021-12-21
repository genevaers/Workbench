package com.ibm.safr.we.message;

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


import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class is used for formatting error messages. Unformatted error messages
 * are stored in a resource bundle. Formatting involves replacing any parameters
 * in the unformatted message text with values supplied at runtime. To support
 * message translation into localized resource bundles, a locale may be specifed
 * when calling the message formatter.
 * 
 */
public class MessageFormatter {

	private static final Object[] emptyMsgArray = new Object[] {};

	/**
	 * The specified key is used to retrieve an unformatted message from a
	 * resource bundle localized for the specified locale. This text is then
	 * formatted with the specified message args.
	 * 
	 * @param locale
	 *            the required locale
	 * @param key
	 *            the message key
	 * @param args
	 *            message parameter values
	 * @return the formatted message text
	 * 
	 * @throws NullPointerException
	 *             if key is null
	 * @throws MissingResourceException
	 *             if a resource bundle or the specified key cannot be found
	 * @throws ClassCastException
	 *             if the object found for the specified key is not a string
	 * @throws IllegalArgumentException
	 *             if the args don't match the message.
	 */
	public String formatMessage(Locale locale, String key, Object[] args,
			String[] bundleNames) {

		ResourceBundle bundle = null;
		MissingResourceException mre = null;

		for (int i = 0; i < bundleNames.length; i++) {
			String bundleName = bundleNames[i];
			try {
				if (locale == null) {
					bundle = ResourceBundle.getBundle(bundleName);
				} else {
					bundle = ResourceBundle.getBundle(bundleName, locale);
				}
				String unformattedMsg = bundle.getString(key);
				String formattedMsg = MessageFormat.format(unformattedMsg,
						args != null ? args : emptyMsgArray);
				return formattedMsg;
			} catch (MissingResourceException e) {
				if (i == (bundleNames.length - 1)) {
					mre = e; // key not found in any bundles
				} else {
					continue; // try the next bundle
				}
			}
		}
		throw mre;

	}
}
