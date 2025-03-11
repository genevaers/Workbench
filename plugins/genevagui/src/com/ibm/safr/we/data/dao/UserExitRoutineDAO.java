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

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;

/**
 * This is an interface for all the methods related to User-Exit Routine
 * metadata component. All these unimplemented methods are implemented in
 * DB2UserExitRoutineDAO.
 */
public interface UserExitRoutineDAO {
	/**
	 * This method is used to retrieve a User-Exit Routine with the specified
	 * Id.
	 * 
	 * @param id
	 *            :This is the ID of the User-Exit Routine which is to be
	 *            retrieved.
	 * @param environmentId
	 *            :This is the ID of the environment to which the User Exit
	 *            Routine belongs.
	 * @return User-Exit Routine transfer objects which contains the details of
	 *         all the fields for the User Exit Routine retrieved from
	 *         EXIT table.
	 * @throws DAOException
	 *             It throws an exception if no User Exit Routine is there with
	 *             the specified ID.
	 */
	UserExitRoutineTransfer getUserExitRoutine(Integer id, Integer environmentId)
			throws DAOException;
	Integer getUserExitRoutine(String exitName, Integer environmentId, boolean procedure);

	/**
	 * This method is used to retrieve all the User-Exit Routines from
	 * EXIT table.
	 * 
	 * @param environmentId
	 *            :The ID of the environment to which the User-Exit Routines
	 *            belong.
	 * @return A list of all User-Exit Routines transfer objects. Transfer
	 *         objects are set with the values which are from the fields for the
	 *         User Exit Routines which belong to the environment with specified
	 *         environmentId retrieved from EXIT table.
	 * @throws DAOException
	 */
	List<UserExitRoutineTransfer> getAllUserExitRoutines(Integer environmentId)
			throws DAOException;

	/**
	 * This method is called from the respective model class. This later calls
	 * respective method either to create or update a User-Exit Routine.
	 * 
	 * @param userExitRoutineTransfer
	 *            The transfer objects whose parameters are set with the values
	 *            received from the UI.
	 * @return The transfer objects whose value are set according to the fields
	 *         of created or updated User Exit Routine in EXIT table.
	 * @throws DAOException
	 *             if any SQLException occurs.
	 * @throws SAFRNotFoundException
	 *             if no rows are updated.
	 */
	UserExitRoutineTransfer persistUserExitRoutine(
			UserExitRoutineTransfer userExitRoutineTransfer)
			throws DAOException, SAFRNotFoundException;

	/**
	 * This method is used to delete a User-Exit Routine from the EXIT
	 * table.
	 */
	void removeUserExitRoutine(Integer id, Integer environmentId)
			throws DAOException;

	/**
	 * This method is used to retrieve a User-Exit Routine with the specified
	 * name.
	 * 
	 * @param userExitRoutineName
	 *            : The name of User-Exit Routine which is to be retrieved.
	 * @param userExitRoutineId
	 *            : The Id of the User Exit Routine which is to be retrieved.
	 * @param environmentId
	 *            : The Id of the environment to which the User Exit Routine
	 *            belongs.
	 * @return User-Exit Routine Transfer objects which contains the details of
	 *         all the fields for the User Exit Routine retrieved from
	 *         EXIT table for the specified name.
	 * @throws DAOException
	 */
	UserExitRoutineTransfer getDuplicateUserExitRoutine(
			String userExitRoutineName, Integer userExitRoutineId,
			Integer environmentId) throws DAOException;

	/**
	 * This method is used to retrieve a User-Exit Routine with the specified
	 * executable
	 * 
	 * @param userExitRoutineExecutable
	 *            : The executable of User-Exit Routine which is to be
	 *            retrieved.
	 * @param userExitRoutineId
	 *            : The Id of the User-Exit Routine which is to be retrieved.
	 * @param environmentId
	 *            : The Id of the environment to which the User Exit Routine
	 *            belongs.
	 * @return User-Exit Routine Transfer objects which contains the details of
	 *         all the fields for the User Exit Routine retrieved from
	 *         EXIT table for the specified executable.
	 * @throws DAOException
	 */
	UserExitRoutineTransfer getDuplicateUserExitExecutable(
			String userExitRoutineExecutable, Integer userExitRoutineId,
			Integer environmentId) throws DAOException;

	/**
	 * This method is used to retrieve all the User-Exit Routines from
	 * EXIT table with only selected columns. As the number of columns
	 * selected is less, the performance improves while populating the list of
	 * User-Exit Routines.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which the User Exit Routines
	 *            belongs.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all UserExitRoutineBean objects.
	 * @throws DAOException
	 */
	List<UserExitRoutineQueryBean> queryAllUserExitRoutines(
			Integer environmentId, SortType sortType) throws DAOException;

	/**
	 * This method is used to retrieve the User-Exit Routines, of specified type
	 * from EXIT table with only selected columns.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which the User Exit Routines
	 *            belongs.
	 * @param typeCodeKey
	 *            : The type of User Exit Routine.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of UserExitRoutineBean objects which matches the type
	 *         specified.
	 * @throws DAOException
	 */
	List<UserExitRoutineQueryBean> queryUserExitRoutines(Integer environmentId,
			String typeCodeKey, SortType sortType) throws DAOException;

	/**
	 * This method will retrieve the components which depends on the specified
	 * User Exit Routine.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which the User Exit Routines
	 *            belongs.
	 * @param userExitRoutineId
	 *            : The Id of the User Exit Routine whose dependencies are to be
	 *            retrieved.
	 * @return A Map whose key is the type of metadata component and the value
	 *         is a list of DependentComponentTransfer of all dependent
	 *         component.
	 * @throws DAOException
	 */
	public Map<ComponentType, List<DependentComponentTransfer>> getUserExitRoutineDependencies(
			Integer environmentId, Integer userExitRoutineId)
			throws DAOException;

    /**
     * This method will retrieve the views which have a logic text dependency
     *  on the specified User Exit Routine.
     * 
     * @param environmentId
     *            : The Id of the environment to which the User Exit Routines
     *            belongs.
     * @param userExitRoutineId
     *            : The Id of the User Exit Routine whose dependencies are to be
     *            retrieved.
     * @return A List whose value is a list of DependentComponentTransfer of all dependent
     *         views.
     * @throws DAOException
     */
    public List<DependentComponentTransfer> getUserExitRoutineLogicViewDeps(
            Integer environmentId, Integer userExitRoutineId)
            throws DAOException;

}
