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
import com.ibm.safr.we.data.transfer.GroupComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.GroupEnvironmentAssociationTransfer;
import com.ibm.safr.we.data.transfer.GroupTransfer;
import com.ibm.safr.we.data.transfer.GroupUserAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.GroupQueryBean;
import com.ibm.safr.we.model.query.UserQueryBean;

/**
 *This is an interface for all the methods related to a Group metadata
 * component. All these unimplemented methods are implemented in DB2GroupDAO.
 * 
 */
public interface GroupDAO {
	/**
	 * This method is used to retrieve a group with the specified Id.
	 * 
	 * @param id
	 *            This is the ID of the Group which is to be retrieved.
	 * @return A Group transfer objects which contains the details of all the
	 *         fields for the group retrieved from GROUP table.
	 * @throws DAOException
	 *             It throws an exception if no Group is there with the
	 *             specified Id.
	 */
	GroupTransfer getGroup(Integer id) throws DAOException;

	/**
	 * This method is used to retrieve a group with the specified name.
	 * 
	 * @param name
	 *            This is the name of the Group which is to be retrieved.
	 * @return A Group transfer objects which contains the details of all the
	 *         fields for the group retrieved from GROUP table.
	 * @throws DAOException
	 *             It throws an exception if no Group is there with the
	 *             specified Id.
	 */
	GroupTransfer getGroup(String name) throws DAOException;
	
	/**
	 * This method is called from the respective model class. This later calls
	 * respective method either to create or update a group.
	 * 
	 * @param groupTransfer
	 *            : The transfer objects whose parameters are set with the
	 *            values received from the UI.
	 * @return The transfer objects whose value are set according to the fields
	 *         of created or updated group in GROUP table.
	 * @throws DAOException
	 *             if any SQLException occurs.
	 * @throws SAFRNotFoundException
	 *             if no rows are updated.
	 */
	GroupTransfer persistGroup(GroupTransfer groupTransfer)
			throws DAOException, SAFRNotFoundException;

	/**
	 * This method is used to retrieve all the Groups from GROUP table
	 * with only selected columns. As the number of columns selected is less,
	 * the performance improves while populating the list of Groups.
	 * 
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all GroupBean objects.
	 * @throws DAOException
	 */
	List<GroupQueryBean> queryAllGroups(SortType sortType) throws DAOException;

	/**
	 * This method is used to delete a Group from the GROUP table.
	 * 
	 * @param id
	 *            The ID of the Group which is to be deleted.
	 * @throws DAOException
	 */
	void removeGroup(Integer id) throws DAOException;

	/**
	 * This method is used to retrieve a group with the specified name.
	 * 
	 * @param groupName
	 *            : The name of the group which is to be retrieved.
	 * @param groupId
	 *            : The Id of the group which is to be retrieved.
	 * @return A Group transfer objects which contains the details of all the
	 *         fields for the group retrieved from GROUP table for the
	 *         specified name.
	 * @throws DAOException
	 */
	GroupTransfer getDuplicateGroup(String groupName, Integer groupId)
			throws DAOException;

	/**
	 * This method returns the list of users associated with this group as a
	 * list of GroupUserAssociationTransfer objects
	 * 
	 * @param groupId
	 *            : The Id of the group whose associated user are to be found.
	 * @return A list of GroupUserAssociationTransfer objects.
	 * @throws DAOException
	 */
	List<GroupUserAssociationTransfer> getAssociatedUsers(Integer groupId)
			throws DAOException;

	/**
	 * This method is called from the respective model class. This later calls
	 * create of associations of Group with Users
	 * 
	 * @param groupUserAssociationTransfer
	 *            : A list of association transfer objects which are to be
	 *            created in SECUSER.
	 * @param groupId
	 *            : The Id of the Group
	 * @return A list of GroupUserAssociationTransfer objects which are created
	 *         in SECUSER.
	 * @throws DAOException
	 */
	List<GroupUserAssociationTransfer> persistAssociatedUsers(
			List<GroupUserAssociationTransfer> groupUserAssociationTransfer,
			Integer groupId) throws DAOException;

	/**
	 * This method is to delete an association in SECUSER
	 * 
	 * @param groupId
	 *            : The Id of the Group whose associations are to be removed.
	 * @param deletionIds
	 *            : A comma delimited String which contains the IDs of users
	 *            whose associations with the group are to be deleted.
	 * @throws DAOException
	 */
	public void deleteAssociatedUser(Integer groupId, List<String> deletionIds)
			throws DAOException;

	/**
	 * This method is to retrieve all the possible Users which can be associated
	 * with this Group.
	 * 
	 * @param notInParam
	 *            : A comma delimited String which contains the IDs of Users
	 *            which are already associated with the Group.
	 * @return A list of UserQueryBean objects.
	 */
	List<UserQueryBean> queryPossibleUserAssociations(List<String> notInParam)
			throws DAOException;

	/**
	 * This method returns the environments associated with the group as a list
	 * of EnvironmentGroupAssociationTransfer objects
	 * 
	 * @param groupId
	 *            : The Id of the group whose associated environments are to be
	 *            retrieved.
	 * @return A list of EnvironmentGroupAssociationTransfer objects.
	 * @throws DAOException
	 */
	List<GroupEnvironmentAssociationTransfer> getAssociatedEnvironments(
			Integer groupId) throws DAOException;

	/**
	 * This method returns the GroupEnvironmentAssociationTransfer of the
	 * Association of the Environment and Group whose Ids are passed to it.
	 * 
	 * @param groupId
	 *            : The Id of the Group.
	 * @param environmentId
	 *            : The Id of the Environment.
	 * @return A GroupEnvironmentAssociationTransfer object.
	 * @throws DAOException
	 */
	GroupEnvironmentAssociationTransfer getAssociatedEnvironment(
			Integer groupId, Integer environmentId) throws DAOException;

	/**
	 * This method is to retrieve all the possible environments which can be
	 * associated with this Group.
	 * 
	 * 
	 * @param associatedEnvIds
	 *            : A list of the IDs of environments which are already
	 *            associated with the Group.
	 * @return A list of EnvironmentalQueryBean objects.
	 */
	List<EnvironmentQueryBean> queryPossibleEnvironmentAssociations(
			List<Integer> associatedEnvIds) throws DAOException;

	/**
	 * This method is called from the respective model class. This method
	 * creates, updates or deletes the Group to Environment associations using a
	 * Stored Procedure named <code>GP_INSGRPENVTINFO</code>.
	 * 
	 * @throws DAOException
	 */
	public void persistGroupEnvironmentAssociations(
			List<GroupEnvironmentAssociationTransfer> createList,
			List<GroupEnvironmentAssociationTransfer> updateList,
			List<GroupEnvironmentAssociationTransfer> deleteList)
			throws DAOException;

	/**
	 * This method is to retrieve a list of a component along with their edit
	 * rights for a particular environment-group association.
	 * 
	 * @param compType
	 *            : An enum which defines the type of component which is to be
	 *            retrieved.
	 * @param environmentId
	 *            : The Id of the environment to which the component belongs.
	 * @param groupId
	 *            : The Id of the group which is associated with the
	 *            environment.
	 * @return A list of GroupComponentAssociationTransfer objects with the id,
	 *         name and the edit rights of the component.
	 * @throws DAOException
	 */
	public List<GroupComponentAssociationTransfer> getComponentEditRights(
			ComponentType compType, Integer environmentId, Integer groupId)
			throws DAOException;

	/**
	 * This method is to retrieve all the possible components which can be
	 * associated with this Group.
	 * 
	 * @param compType
	 *            : The type of component. One of Physical File, Logical File.
	 *            Logical Record, User Exit Routine and View Folder.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which component belongs.
	 * @param associatedCompIds
	 *            : The list of Ids of the component which is already associated
	 *            with the Group
	 * @return A list of EnvironmentalQueryBean objects which can be associated
	 *         with the Group.
	 * @throws DAOException
	 */
	List<EnvironmentalQueryBean> queryPossibleComponentAssociations(
			ComponentType compType, Integer environmentId,
			List<Integer> associatedCompIds) throws DAOException;

	/**
	 * Returns the Groups that the specified User belongs to which are also
	 * associated with the specified Environment. This is the list of Groups
	 * that a general user may choose from during login. The list is sorted by
	 * Group name.
	 * 
	 * @param userId
	 * @param environmentId
	 * @return a List of GroupQueryBean
	 * @throws DAOException
	 */
	List<GroupQueryBean> queryGroups(String userId, Integer environmentId)
			throws DAOException;

	/**
	 * This method is to store the edit rights of the components. The edit
	 * rights include create, modify or delete rights on a particular component.
	 * 
	 * @param compType
	 *            : The type of component. One of Physical File, Logical File.
	 *            Logical Record, User Exit Routine and View Folder.
	 * @param groupId
	 *            : The Id of the group with which the component is associated.
	 * @param createList
	 *            : The list of GroupComponentAssociationTransfer which contains
	 *            the association which are to be created.
	 * @param updateList
	 *            : The list of GroupComponentAssociationTransfer which contains
	 *            the association which are to be updated.
	 * @param deleteList
	 *            : The list of GroupComponentAssociationTransfer which contains
	 *            the association which are to be deleted.
	 * @throws DAOException
	 */
	public void persistComponentEditRights(ComponentType compType,
			Integer groupId,
			List<GroupComponentAssociationTransfer> createList,
			List<GroupComponentAssociationTransfer> updateList,
			List<GroupComponentAssociationTransfer> deleteList)
			throws DAOException;
}
