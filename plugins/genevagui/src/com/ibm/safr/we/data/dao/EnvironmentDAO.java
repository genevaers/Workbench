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

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.EnvironmentTransfer;
import com.ibm.safr.we.data.transfer.GroupEnvironmentAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.query.EnvComponentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.GroupQueryBean;

/**
 *This is an interface for all the methods related to an Environment metadata
 * component. All these unimplemented methods are implemented in
 * DB2EnvironmentDAO.
 * 
 */
public interface EnvironmentDAO {

	/**
	 * This method is used to retrieve an environment with the specified Id.
	 * 
	 * @param id
	 *            : This is the ID of the environment which is to be retrieved.
	 * @return An Environment transfer objects which contains the details of all
	 *         the fields for the environment retrieved from ENVIRON table.
	 * @throws DAOException
	 *             It throws an exception if no environment is there with the
	 *             specified ID.
	 */
	EnvironmentTransfer getEnvironment(Integer id) throws DAOException;

	/**
	 * This method is used to retrieve an environment with the specified name.
	 * 
	 * @param name
	 *            : This is the name of the environment which is to be retrieved.
	 * @return An Environment transfer objects which contains the details of all
	 *         the fields for the environment retrieved from ENVIRON table.
	 * @throws DAOException
	 *             It throws an exception if no environment is there with the
	 *             specified ID.
	 */
	EnvironmentTransfer getEnvironment(String name) throws DAOException;
	
	/**
	 * This method is called from the respective model class. This later calls
	 * respective method either to create or update an environment.
	 * 
	 * @param environmentTransfer
	 *            : The transfer objects whose parameters are set with the
	 *            values received from the UI.
	 * @return The transfer objects whose value are set according to the fields
	 *         of created or updated environment in ENVIRON table.
	 * @throws DAOException
	 *             if any SQLException occurs.
	 * @throws SAFRNotFoundException
	 *             if no rows are updated.
	 */
	EnvironmentTransfer persistEnvironment(
			EnvironmentTransfer environmentTransfer) throws DAOException,
			SAFRNotFoundException;

	/**
	 * This method is used to retrieve an environment with the specified name.
	 * 
	 * @param environmentName
	 *            : The name of the environment which is to be retrieved.
	 * @param environmentId
	 *            : The ID of the environment which is to be retrieved.
	 * @return an Environment transfer objects which contains the details of all
	 *         the fields for the environment retrieved from ENVIRON table
	 *         for the specified name.
	 * @throws DAOException
	 */
	EnvironmentTransfer getDuplicateEnvironment(String environmentName,
			Integer environmentId) throws DAOException;

	/**
	 * This method is used to retrieve all the Environments from ENVIRON
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of Environments.
	 * 
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all EnvironmentBean objects.
	 * @throws DAOException
	 */
	List<EnvironmentQueryBean> queryAllEnvironments(SortType sortType)
			throws DAOException;

	/**
	 * Return a list of components in different environments with the same id 
	 * and type
	 * 
	 * @param id : identifier of the component to check
	 * @param type : type of the component to check
	 * @return A list of all EnvComponentQueryBean objects.
	 * @throws DAOException
	 */
	List<EnvComponentQueryBean> queryComponentEnvironments(Integer id, ComponentType type)
			throws DAOException;
	
	/**
	 * Returns a sorted list of query beans representing the Environments
	 * associated with all Groups that the specified User belongs to if param
	 * 'onlyWithEnvAdminRights' is false, else returns list of environments
	 * associated with all Groups that the specified User belongs to and on user
	 * has admin rights on that environments.
	 * 
	 * @param sortType
	 *            the sort sequence (Id or Name)
	 * @param userid
	 *            the specified user
	 * @param onlyWithEnvAdminRights
	 *            if true returns only those environment on user have admin
	 *            rights
	 * @return a List of EnvironmentQueryBean
	 * @throws DAOException
	 */
	List<EnvironmentQueryBean> queryAllEnvironments(SortType sortType,
			String userid, Boolean onlyWithAdminRights) throws DAOException;

	/**
	 * Returns a sorted list of query beans representing all Environments
	 * associated with the specified Group.
	 * 
	 * @param sortType
	 *            the sort sequence (Id or Name)
	 * @param groupId
	 *            the Group the Environments are associated with
	 * @param onlyWithAdminRights
	 *            if true get only those environments on which group has admin
	 *            rights.
	 * @return a List of EnvironmentQueryBean
	 * @throws DAOException
	 */
	List<EnvironmentQueryBean> queryEnvironmentsForGroup(SortType sortType,
			Integer groupId, Boolean onlyWithAdminRights) throws DAOException;

	/**
	 * This method returns the list of groups associated with this environment
	 * as a list of GroupEnvironmentAssociationTransfer objects
	 * 
	 * @param environmentId
	 *            : The Id of the environment whose associated groups are to be
	 *            found.
	 * @param sortType
	 *            :the sort sequence (Id or Name)
	 * @return A list of GroupEnvironmentAssociationTransfer objects.
	 * @throws DAOException
	 */
	List<GroupEnvironmentAssociationTransfer> getAssociatedGroups(
			Integer environmentId, SortType sortType) throws DAOException;

	/**
	 * This method is used to permanently clear all the components data in a
	 * selected environment.
	 * 
	 * @param environmentId
	 *            : The Id of the environment from which the components data to
	 *            be cleared.
	 * 
	 * @throws DAOException
	 */
	public void clearEnvironment(Integer environmentId) throws DAOException;

	/**
	 * This method is used to check if an environment is empty or not. Returns
	 * True if the environment has dependencies or false if it has none.
	 * 
	 * @param id
	 *            : The ID of the Environment for which the dependency is to be
	 *            checked.
	 * @return : returns true if there are any dependencies or returns false if
	 *         there are none.
	 * @throws DAOException
	 */

	Boolean hasDependencies(Integer environmentId) throws DAOException;

	/**
	 * This method is used to delete an Environment from ENVIRON.
	 * 
	 * @param id
	 *            : The ID of the Environment which is to be deleted.
	 * @throws DAOException
	 */
	void removeEnvironment(Integer environmentId) throws DAOException;

	/**
	 * This method is to retrieve all the possible groups which can be
	 * associated with this environment.
	 * 
	 * 
	 * @param associatedGroupIds
	 *            : A list of the IDs of groups which are already associated
	 *            with the environment.
	 * @return A list of GroupQueryBean objects.
	 */
	List<GroupQueryBean> queryPossibleGroupAssociations(
			List<Integer> associatedGroupIds) throws DAOException;

    /**
     * Return environments available for batch activate lookups
     * @param sortType
     * @return
     * @throws DAOException
     */
    List<EnvironmentQueryBean> queryBALEnvironments(SortType sortType) throws DAOException;

    /**
     * Return environments available for batch activate views
     * @param sortType
     * @return
     * @throws DAOException
     */
    List<EnvironmentQueryBean> queryBAVEnvironments(SortType sortType) throws DAOException;
    
}
