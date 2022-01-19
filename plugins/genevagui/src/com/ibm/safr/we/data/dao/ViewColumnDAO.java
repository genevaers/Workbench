/**
 * 
 */
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
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;

/**
 * A data access interface for the ViewColumn class.
 * 
 */
public interface ViewColumnDAO {

	/**
	 * This method is used to retrieve the ViewColumns for the specified View if
	 * the View ID passed is greater than 0, else it will retrieve all the
	 * ViewColumns in the specified environment.
	 * 
	 * @param viewId
	 *            : This is the ID of the parent View. If this is 0, this method
	 *            will retrieve all the ViewColumns in the specified
	 *            environment.
	 * @param environmentId
	 *            : The Id of the Environment to which the View belongs.
	 * @return A List of ViewColumnTransfer objects.
	 * @throws DAOException
	 */
	List<ViewColumnTransfer> getViewColumns(Integer viewId,
			Integer environmentId) throws DAOException;

	/**
	 * This method is to store the View Columns.
	 * 
	 * @param viewColTransferList
	 *            : A list of ViewColumnTransfer objects which are to be created
	 *            or modified.
	 * @return A list of ViewColumnTransfer which are created or modified.
	 * @throws DAOException
	 */
	List<ViewColumnTransfer> persistViewColumns(
			List<ViewColumnTransfer> viewColTransferList) throws DAOException;

	/**
	 * This method is to be remove the View Columns. Before removing the View
	 * Columns, all the column sources and sort keys related to that View Column
	 * must be removed
	 * 
	 * @param vwColumnIds
	 *            : The List of Ids of View Columns which are to be removed.
	 * @param environmentId
	 *            : The Id of the environment to which the view belongs.
	 * @throws DAOException
	 */
	public void removeViewColumns(List<Integer> vwColumnIds,
			Integer environmentId) throws DAOException;

}
