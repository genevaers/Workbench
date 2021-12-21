package com.ibm.safr.we.ui.reports;

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


import java.util.List;

/**
 * This interface will be implemented by all the report data classes used to
 * fetch report data.
 * 
 */
public interface IReportData {
	/**
	 * This method returns a list of error messages. This could be a list of
	 * errors encountered while loading a component.
	 * 
	 * @return list of error messages.
	 */
	public List<String> getErrors();

	/**
	 * This method is to check if there is any data to report on. If all the
	 * components, to report on, fails to load, this method will return false.
	 * 
	 * @return boolean indicating if data is available to report on.
	 */
	public boolean hasData();
}
