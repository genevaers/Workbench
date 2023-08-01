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

import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;

/**
 * This is an interface for all the methods related to View Folder metadata
 * component. All these unimplemented methods are implemented in
 * DB2ViewFolderDAO.
 * 
 */
public interface ViewFolderDAO {

	/**
	 * This method is used to retrieve a view folder with the specified Id.
	 * 
	 * @param id
	 *            : This is the ID of the View Folder which is to be retrieved.
	 * @param environmentId
	 *            : The id of the environment to which the view folder belongs.
	 * @return A View Folder transfer objects which contains the details of all
	 *         the fields for the View Folder retrieved from VIEWFOLDER
	 *         table.
	 * @throws DAOException
	 */
	ViewFolderTransfer getViewFolder(Integer id, Integer environmentId)
			throws DAOException;

	/**
	 * This method is called from the respective model class. This later calls
	 * respective method either to create or update a View Folder.
	 * 
	 * @param viewFolderTransfer
	 *            : The transfer objects whose parameters are set with the
	 *            values received from the UI.
	 * @return the transfer objects whose value are set according to the fields
	 *         of created or updated View Folder in VIEWFOLDER table.
	 * @throws DAOException
	 *             if any SQLException occurs.
	 * @throws SAFRNotFoundException
	 *             if no rows are updated.
	 */
	ViewFolderTransfer persistViewFolder(ViewFolderTransfer viewFolderTransfer)
			throws DAOException, SAFRNotFoundException;

	/**
	 * This method is used to retrieve all the View Folders from VIEWFOLDER
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of View Folders.
	 * 
	 * @param environmentId
	 *            : The id of the environment to which the view folders belongs.
	 * @return A list of all ViewFolderBean objects.
	 * @throws DAOException
	 */

	List<ViewFolderQueryBean> queryAllViewFolders(Integer environmentId,
			SortType sortType)
			throws DAOException;

	/**
	 * Returns a list of query beans representing the View Folders from the
	 * specified Environment that a user will have access to. If the user is a
	 * system administrator, this will be all View Folders in the Environment.
	 * If not, it will be the View Folders in the Environment that the specified
	 * Group has access to. This is used, for example, when setting the Default
	 * View Folder for a User.
	 * 
	 * @param environmentId
	 *            target Environment containing the required View Folders
	 * @param groupId
	 *            target Group that must have access to the View Folders, where
	 *            isSystemAdmin is false (ignored if isSystemAdmin is true)
	 * @param isSystemAdmin
	 *            true the the subject user of this query is a System
	 *            Administrator, otherwise false (this will affect which View
	 *            Folders are accessible to the user)
	 * @param sortType
	 *            sort by ID or Name
	 * @return a list of ViewFolderQueryBean
	 * @throws DAOException
	 */
	List<ViewFolderQueryBean> queryViewFolders(Integer environmentId,
			Integer groupId, boolean isSystemAdmin, SortType sortType)
			throws DAOException;

	/**
	 * This method is used to remove a View Folder with the specified Id
	 * 
	 * @param Id
	 *            : The id of the view folder which is to be removed.
	 * @param environmentId
	 *            : The id of the environment in which the View Folder exists.
	 * @throws DAOException
	 */
	void removeViewFolder(Integer id, Integer environmentId)
			throws DAOException;

	/**
	 * This method is used to retrieve a View Folder with the specified name.
	 * 
	 * @param viewFolderName
	 *            : the name of the View Folder to be searched.
	 * @param viewFolderId
	 *            : The id of the View Folder.
	 * @param environmentId
	 *            : The id of the environment to which the View Folder belongs.
	 * @return: a View Folder transfer objects which contains the details of all
	 *          the fields for the View Folder retrieved from VIEWFOLDER
	 *          table for the specified name.
	 * @throws DAOException
	 */
	ViewFolderTransfer getDuplicateViewFolder(String viewFolderName,
			Integer viewFolderId, Integer environmentId) throws DAOException;

	/**
	 * This method is to return a count of views which are in the specified View
	 * Folder.
	 * 
	 * @param viewFolderId
	 *            : The id of the View Folder.
	 * @param environmentId
	 *            : The id of the environment to which the View Folder belongs.
	 * @return An integer which is the number of views in that View Folder.
	 * @throws DAOException
	 */
	public Integer getCountOfViewsInViewFolder(Integer viewFolderId,
			Integer environmentId) throws DAOException;

	/**
	 * This method is to get the users in which the specified view folder is
	 * used as a default View Folder.
	 * 
	 * @param viewFolderId
	 *            : The Id of the view folder.
	 * @return A list of DependentComponentTransfer.
	 * @throws DAOException
	 */
	public List<DependentComponentTransfer> getUserDependencies(
			Integer viewFolderId) throws DAOException;

	/**
	 * Delete associated Views from the View folder
	 * @param environmentId
	 * @param deletionIds
	 */
    void deleteAssociatedViews(Integer environmentId, List<Integer> deletionIds);

    /**
     * Add associated Views to the View folder
     * @param list
     * @return
     */
    void persistAssociatedViews(List<ViewFolderViewAssociationTransfer> list);

    /**
     * Get all View associations for a View folder
     * @param environmentId
     * @param id
     * @return
     */
    List<ViewFolderViewAssociationTransfer> getVFVAssociation(Integer environmentId, Integer id);

    /**
     * Get all View associations for an environment
     * @param environmentId
     * @return
     */
    List<ViewFolderViewAssociationTransfer> getVFVAssociations(Integer environmentId);

    /**
     * Get all available View associations to add
     * @param environmentId
     * @param notInParam
     * @return
     */
    List<ViewQueryBean> queryPossibleViewAssociations(int environmentId, List<Integer> notInParam);

    /**
     * 
     */
    void clearAssociations();

    /**
     * 
     * @param id
     * @param environmentId
     */
    void addAllViewsAssociation(Integer id, Integer environmentId);

	List<ViewFolderViewAssociationTransfer> getinactiveviewinfolders(int environment, List<Integer> folderIds);
}
