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


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.LookupPathTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.query.LookupQueryBean;

public interface LookupDAO {
	/**
	 * This function is used to retrieve all the Lookup Paths from LOOKUP
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of Lookup Paths.
	 * 
	 * @param environmentId
	 *            : the ID of the environment to which the Lookup Path belongs.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all LookupQueryBean objects.
	 * @throws DAOException
	 */
	List<LookupQueryBean> queryAllLookups(Integer environmentId,
			SortType sortType) throws DAOException;

	/**
	 * This function is used to retrieve a Lookup Path with the specified ID.
	 * 
	 * @param id
	 *            : The ID of the Lookup Path which is to be retrieved.
	 * @param environmentId
	 *            : The ID of the environment to which the Lookup Path belongs.
	 * @return Lookup Path transfer objects which contains the details of all
	 *         the columns for the Lookup Path retrieved from LOOKUP table.
	 * @throws DAOException
	 */
	LookupPathTransfer getLookupPath(Integer id, Integer environmentId)
			throws DAOException;

	/**
	 * This function is called from the respective model class. This later calls
	 * respective function either to create or update a Lookup Path.
	 * 
	 * @param lookupPathTransfer
	 *            : The transfer objects whose parameters are set with the
	 *            values received from the UI.
	 * @return The transfer objects whose value are set according to the columns
	 *         of created or updated Lookup Path in LOOKUP table.
	 * @throws DAOException
	 *             if any SQLException occurs.
	 * @throws SAFRNotFoundException
	 *             if no rows are updated.
	 */

	LookupPathTransfer persistLookupPath(LookupPathTransfer lookupPathTransfer)
			throws DAOException, SAFRNotFoundException;

	/**
	 * This function is used to delete an Lookup Path from the LOOKUP table.
	 * 
	 * @param id
	 *            : The ID of the Lookup Path which is to be deleted.
	 * @param environmentId
	 *            : The ID of the environment to which the Lookup Path belongs.
	 * @throws DAOException
	 */

	void removeLookupPath(Integer id, Integer environmentId)
			throws DAOException;

	/**
	 * This function is used to retrieve a Lookup Path with the specified name.
	 * 
	 * @param lookupName
	 *            : The name of the Lookup Path which is to be retrieved.
	 * @param lookupId
	 *            : The ID which should not be equal to the id of the Lookup
	 *            Path found.
	 * @param environmentId
	 *            : The ID of the environment to which the Lookup Path belongs.
	 * @return a Lookup Path transfer objects which contains the details of all
	 *         the columns for the Lookup Path retrieved from LOOKUP table
	 *         for the specified name.
	 * @throws DAOException
	 */

	LookupPathTransfer getDuplicateLookupPath(String lookupName,
			Integer lookupId, Integer environmentId) throws DAOException;

	/**
	 * This method is to return Views dependent on the Lookup Path. The Lookup
	 * Path can be used in any source field of View, in Sort key title of a Sort
	 * key or in View's logic text.
	 * 
	 * @param environmentId
	 *            : The Id of the environment in which the Lookup Path is
	 *            present.
	 * @param lookupPathId
	 *            : The Id of the Lookup Path for which dependencies are to be
	 *            checked.
	 * @param exceptionList
	 *            : The View dependencies which are to be excluded from the
	 *            list.
	 * @return A List of DependentComponentTransfer objects which contains the
	 *         views dependent on the Lookup Path.
	 * @throws DAOException
	 */

	List<DependentComponentTransfer> getLookupPathViewDependencies(
			Integer environmentId, Integer lookupPathId,
			Set<Integer> exceptionList) throws DAOException;

	/**
	 * This method is to retrieve the Logical Records which are currently
	 * inactive and used in the Lookup Path.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which the Lookup Path belongs.
	 * @param lookupPathId
	 *            : The Id of the Lookup Path.
	 * @return A list of DependentComponentTransfer which contains the inactive
	 *         Logical Records used in the Lookup Path.
	 * @throws DAOException
	 */
	List<DependentComponentTransfer> getLookupPathInactiveLogicalRecordsDependencies(
			Integer environmentId, Integer lookupPathId) throws DAOException;

	/**
	 * This method is to activate multiple Lookup Paths.
	 * 
	 * @param lookupPathIds
	 *            : The list of Ids of Lookup Paths which are to be activated.
	 * @param environmentId
	 *            : The Id of the environment to which these Lookup Paths
	 *            belong.
	 * @throws DAOException
	 */
	void makeLookupPathsActive(List<Integer> lookupPathIds,
			Integer environmentId) throws DAOException;

	/**
	 * This method is to inactivate multiple Lookup Paths.
	 * 
	 * @param lookupPathIds
	 *            : The list of Ids of Lookup Paths which are to be inactivated.
	 * @param environmentId
	 *            : The Id of the environment to which these Lookup Paths
	 *            belong.
	 * @throws DAOException
	 */
	void makeLookupPathsInactive(Collection<Integer> lookupPathIds,
			Integer environmentId) throws DAOException;

	/**
	 * This method takes the Id of a Lookup Path and return the Query Bean of
	 * the Lookup Path.
	 * 
	 * @param lookupPathId
	 *            : The Id of the Lookup Path.
	 * @param environmentId
	 *            : The Id of the environment to which Lookup Path belongs.
	 * @return A LookupQueryBean object.
	 * @throws DAOException
	 */
	LookupQueryBean queryLookupPath(Integer lookupPathId, Integer environmentId)
			throws DAOException;

	/**
	 * This method checks if all the Lookup Paths whose Ids are passed to it are
	 * using the same LR-LF association as their target.
	 * 
	 * @param lookupPathIds
	 *            : The list of Ids of Lookup Paths.
	 * @param environmentId
	 *            : The Id of the environment to which Lookup Paths belong.
	 * @return It returns true if all Lookup Paths are using the same LR-LF
	 *         association as their target, false otherwise.
	 * @throws DAOException
	 */
	Boolean isSameTarget(List<Integer> lookupPathIds, Integer environmentId)
			throws DAOException;

	/**
	 * Find all accessible lookups for an environment
	 * @param environmentId
	 * @param sortType
	 * @return
	 */
    List<LookupQueryBean> queryLookupsForBAL(Integer environmentId, SortType sortType)
         throws DAOException;

	Integer getLookupPath(String name, Integer environID);

	Map<String, Integer> getTargetFields(String name, Integer environID);
	
}
