package com.ibm.safr.we.data.dao;

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

import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;

/**
 * A data access interface for the ViewSortKey class.
 * 
 */
public interface ViewSortKeyDAO {

	/**
	 * This method is used to retrieve the ViewSortKeys for the specified View
	 * if the View ID passed is greater than 0, else it will retrieve all the
	 * ViewSortKeys in the specified environment..
	 * 
	 * @param viewId
	 *            : This is the ID of the parent View. If this is 0, this method
	 *            will retrieve all the ViewSortKeys in the specified
	 *            environment.
	 * @param environmentId
	 *            : The Id of the Environment to which the View belongs.
	 * @return A List of ViewSortKeyTransfer objects.
	 * @throws DAOException
	 */
	List<ViewSortKeyTransfer> getViewSortKeys(Integer viewId,
			Integer environmentId) throws DAOException;

	/**
	 * This method is to store View Sort Keys.
	 * 
	 * @param vskTransferList
	 *            : A List of ViewSortKeyTransfer objects.
	 * @throws DAOException
	 */
	public void persistViewSortKeys(
			List<ViewSortKeyTransfer> vskTransferList) throws DAOException;

	/**
	 * This method is to remove View Sort Keys.
	 * 
	 * @param vskIdsList
	 *            : A List of Ids of the View Sort Keys which are to be removed.
	 * @throws DAOException
	 */
	public void removeViewSortKeys(List<Integer> vskIdsList,
			Integer environmentId) throws DAOException;

}
