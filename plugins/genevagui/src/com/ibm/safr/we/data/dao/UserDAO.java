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
import com.ibm.safr.we.data.transfer.UserGroupAssociationTransfer;
import com.ibm.safr.we.data.transfer.UserTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.query.UserQueryBean;

/**
 *This is an interface for all the methods related to a User. All these
 * unimplemented methods are implemented in DB2UserDAO.
 */
public interface UserDAO {

	/**
	 * This method is used to retrieve a user with the specified Id
	 * 
	 * @param id
	 *            : This is the ID of the user which is to be retrieved.
	 * @return A User transfer object which contains the details of all the
	 *         fields for the user retrieved from USER table.
	 * @throws DAOException
	 *             : It throws an exception if no user is there with the
	 *             specified ID.
	 */
	UserTransfer getUser(String id) throws DAOException;

	/**
	 * This method is used to remove a user with the specified id.
	 * 
	 * @param id
	 *            : It is the id of the user which is to be removed.
	 * @throws DAOException
	 *             It throws an exception if no user is there with the specified
	 *             ID.
	 */
	void removeUser(String id) throws DAOException;

	/**
	 * This method is called from the respective model class. This later calls
	 * respective method either to create or update a user.
	 * 
	 * @param userTransfer
	 *            : The transfer objects whose parameters are set with the
	 *            values received from the UI.
	 * @return The transfer objects whose value are set according to the fields
	 *         of created or updated user in USER table.
	 * @throws DAOException
	 *             if any SQLException occurs.
	 * @throws SAFRNotFoundException
	 *             if no rows are updated.
	 */
	UserTransfer persistUser(UserTransfer userTransfer) throws DAOException,
			SAFRNotFoundException;

	/**
	 * This method is used to retrieve a user with the specified user Id.
	 * 
	 * @param userId
	 *            : The Id of the user which is to be retrieved.
	 * @return A User transfer objects which contains the details of all the
	 *         fields for the user retrieved from USER table for the
	 *         specified name.
	 * @throws DAOException
	 */
	UserTransfer getDuplicateUser(String userId) throws DAOException;

	/**
	 * This method is used to retrieve all the Users from USER table with
	 * only selected columns. As the number of columns selected is less, the
	 * performance improves while populating the list of Users.
	 * 
	 * @return A list of all UserBean objects.
	 * @throws DAOException
	 */
	List<UserQueryBean> queryAllUsers() throws DAOException;

	/**
	 * This method is to set user preferences for the default environment and
	 * default group.
	 * 
	 * @param userId
	 *            : The Id of the user for which preferences are to be set.
	 * @param environmentId
	 *            : The Id of the environment which is to be set as default
	 *            environment.
	 * @param groupId
	 *            : The Id of the group which is to be set as default group.
	 * @throws DAOException
	 */
	void setUserPreferences(String userId, Integer environmentId,
			Integer groupId) throws DAOException;

	/**
	 * This method returns the list of groups associated with this user as a
	 * list of UserGroupAssociationTransfer objects
	 * 
	 */
	List<UserGroupAssociationTransfer> getAssociatedGroups(String userId)
			throws DAOException;

	/**
	 * This method is to remove references of a view folder which is being used
	 * as a default view folder for any user.
	 * 
	 * @param viewFolderId
	 *            : The Id of the view folder.
	 * @throws DAOException
	 */
	void removeViewFolderReferences(Integer viewFolderId) throws DAOException;

	/**
	 * 
	 * @param userId
	 * @param deletionIds
	 * @throws DAOException
	 */
    void deleteAssociatedGroups(String userId, List<Integer> deletionIds) throws DAOException;
	
	/**
	 * This method is used to store the associated groups of a user.
	 * 
	 * @param list
	 * @param userid
	 * @return
	 * @throws DAOException
	 */
	List<UserGroupAssociationTransfer> persistAssociatedGroups(
			List<UserGroupAssociationTransfer> list, String userid)
			throws DAOException;

}
