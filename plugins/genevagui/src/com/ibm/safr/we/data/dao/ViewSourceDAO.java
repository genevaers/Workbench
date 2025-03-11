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
import java.util.Map;

import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;

/**
 * A data access interface for the ViewSource class.
 * 
 */
public interface ViewSourceDAO {

	/**
	 * This method is used to retrieve the ViewSources for the specified View if
	 * the View ID passed is greater than 0, else it will retrieve all the
	 * ViewSources in the specified environment.
	 * 
	 * @param viewId
	 *            : This is the ID of the parent View. If this is 0, this method
	 *            will retrieve all the ViewSources in the specified
	 *            environment.
	 * @param environmentId
	 *            : The Id of the Environment to which the View belongs.
	 * @return A List of ViewSourceTransfer objects.
	 * @throws DAOException
	 */
	List<ViewSourceTransfer> getViewSources(Integer viewId,
			Integer environmentId) throws DAOException;

	/**
	 * This method is to store the View Sources.
	 * 
	 * @param viewSrcTransferList
	 *            : A list of ViewSourceTransfer objects which are to be created
	 *            or modified.
	 * @throws DAOException
	 */
	void persistViewSources(List<ViewSourceTransfer> viewSrcTransferList) throws DAOException;

	/**
	 * This method is to retrieve all the Lookup Paths which has the Logical
	 * Record as its source LR and all the target LR for those Lookup Paths.
	 * 
	 * @param logicalRecordId
	 *            : The Id of the Logical Record.
	 * @param environmentId
	 *            : The Id of the environment.
	 * @return A Map with the id of Logical Record as key and a list of
	 *         EnvironmentalQueryBean as value, with the first QueryBean for the
	 *         Logical Record and the rest QueryBeans for list of associated
	 *         Lookup Paths.
	 * @throws DAOException
	 */
	Map<Integer, List<EnvironmentalQueryBean>> getViewSourceLookupPathDetails(
			Integer logicalRecordId, Integer environmentId) throws DAOException;

	/**
	 * This method is to get all the source fields of the Lookup Paths which are
	 * of type Symbolic.
	 * 
	 * @param lkupPathIds
	 *            : The list of Ids of Lookup Paths whose Symbolic Source Fields
	 *            are to be retrieved.
	 * @param environmentId
	 *            : The Id of the environment.
	 * @return A Map with Lookup Path Id as the key and list of
	 *         LookupPathSourceFieldTransfer objects as value.
	 * @throws DAOException
	 */
	Map<Integer, List<LookupPathSourceFieldTransfer>> getLookupPathSymbolicFields(
			List<Integer> lkupPathIds, Integer environmentId)
			throws DAOException;

	/**
	 * This method is to be remove the View Sources. Before removing the View
	 * Sources, all the column sources related to these View Sources must be
	 * removed.
	 * 
	 * @param vwSrcIds
	 *            : The List of Ids of View Sources which are to be removed.
	 * @param environmentId
	 *            : The Id of the environment to which the View belongs.
	 * @throws DAOException
	 */
	public void removeViewSources(List<Integer> vwSrcIds, Integer environmentId)
			throws DAOException;

    /**
     * Returns a View sources LR Id
     * @param viewSrcId
     * @param environmentId
     * @return LrId
     * @throws DAOException
     */
    public int getViewSourceLrId(Integer viewSrcId, Integer environmentId) throws DAOException;
	
}
