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
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SearchCriteria;
import com.ibm.safr.we.constants.SearchPeriod;
import com.ibm.safr.we.constants.SearchViewsIn;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.FindTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;

/**
 *This class is an interface for all the methods related to View metadata
 * component.
 * 
 */
public interface ViewDAO {

	/**
	 * This method is used to retrieve a View with the specified Id.
	 * 
	 * @param id
	 *            : This is the ID of the View which is to be retrieved.
	 * @param environmentId
	 *            : The Id of the Environment to which the View belongs.
	 * @return A View transfer object which contains the details of all the
	 *         fields for the View retrieved from VIEW table.
	 * @throws DAOException
	 *             It throws an exception if no View is found.
	 */
	ViewTransfer getView(Integer id, Integer environmentId) throws DAOException;

	/**
	 * 
	 * @return
	 */
	public List<ViewTransfer> queryAllLogicBlocks();
	
    /**
     */
	
    byte [] getLogicTextBytes(Integer id, Integer environmentId);
	
	/**
	 * This function is used to retrieve all the Views from VIEW table with
	 * only selected columns. The selected columns do not include information
	 * about the View Folder to which Views belong. As the number of columns
	 * selected is less, the performance improves while populating the list of
	 * Views.
	 * 
	 * @param sortType
	 *            : The type on which the result is to be sorted.
	 * @param environmentId
	 *            : the ID of the environment to which the Views belong.
	 * @param viewFolderId
	 *            : The Id of the view folder from which views are to be
	 *            extracts. If its -1 then it will bring views from all view
	 *            folders.
	 * 
	 * @return A list of all ViewQueryBean objects.
	 * @throws DAOException
	 */
	List<ViewQueryBean> queryAllViewsOld(SortType sortType, Integer environmentId,
			Integer viewFolderId) throws DAOException;

    /**
     * This function is used to retrieve all the Views from VIEW table with
     * only selected columns. The selected columns do not include information
     * about the View Folder to which Views belong. As the number of columns
     * selected is less, the performance improves while populating the list of
     * Views.
     * 
     * @param sortType
     *            : The type on which the result is to be sorted.
     * @param environmentId
     *            : the ID of the environment to which the Views belong.
     * @param viewFolderId
     *            : The Id of the view folder from which views are to be
     *            extracts. If its -1 then it will bring views from all view
     *            folders.
     * 
     * @return A list of all ViewQueryBean objects.
     * @throws DAOException
     */
    List<ViewQueryBean> queryAllViews(SortType sortType, Integer environmentId,
        Integer viewFolderId);

	
	/**
	 * This function is used to retrieve all the Views from VIEW table with
	 * only selected columns. The selected columns include the information of
	 * View Folder id and name to which the Views belong. The function doesn't
	 * return any information about the version history or Check Out status. As
	 * the number of columns selected is less, the performance improves while
	 * populating the list of Views.
	 * 
	 * @param sortType
	 *            : The type on which the result is to be sorted.
	 * @param environmentId
	 *            : the ID of the environment to which the Views belong.
	 * 
	 * @return A list of all ViewQueryBean objects.
	 * @throws DAOException
	 */
	List<ViewQueryBean> queryAllViews(SortType sortType, Integer environmentId, boolean admin) throws DAOException;

	/**
	 * This method is called from the respective model class to persist that
	 * objects state in the database.
	 * 
	 * @param viewTransfer
	 *            : The transfer object whose attributes are set with the values
	 *            received from the model object.
	 * @return The transfer object whose attributes are set according to the
	 *         fields of created or updated View in VIEW table.
	 * @throws DAOException
	 *             if any SQLException occurs.
	 * @throws SAFRNotFoundException
	 *             if no rows are updated.
	 */
	ViewTransfer persistView(ViewTransfer viewTransfer) throws DAOException,
			SAFRNotFoundException;

	/**
	 * Use this method to persist the logic text bytes contained in a view. This
	 * will store the human readable logic text and also its compiled version.
	 * 
	 * @param viewTransfer
	 *            the transfer object containing the logic text bytes to be
	 *            stored.
	 * @param saveCompiledLogicText
	 *            true if the compiled logic text (i.e.Logic text type 2) is to
	 *            be saved else false.
	 * @throws DAOException
	 */
	void persistViewLogic(ViewTransfer viewTransfer,
			boolean saveCompiledLogicText) throws DAOException;

	/**
	 * This method is to populate all the lists of different metadata components
	 * which are used in View Properties. This uses stored procedure
	 * GP_GETVWPROPS to retrieve those lists.
	 * 
	 * @param environmentId
	 *            : The Id of the environment.
	 * @return A Map with Component type as key and value as a list of
	 *         EnvironmentalQueryBean objects.
	 * @throws DAOException
	 */
	Map<ComponentType, List<EnvironmentalQueryBean>> queryViewPropertiesLists(
			Integer environmentId) throws DAOException;

	/**
	 * 
	 * This method is used to get all the Inactive dependencies of a View, and
	 * returns the components inactive Logical Records or Lookup paths.
	 * 
	 * 
	 * @param environmentId
	 *            :The Id of the environment.
	 * @param viewId
	 *            : The Id of the view.
	 * @return A Map with component type as a key and value as a list of
	 *         dependent component transfer objects.
	 * @throws DAOException
	 */
	Map<ComponentType, List<DependentComponentTransfer>> getInactiveDependenciesOfView(
			Integer environmentId, Integer viewId) throws DAOException;

	/**
	 * This method is used to retrieve a View with the specified name.
	 * 
	 */
	ViewTransfer getDuplicateView(String viewName, Integer viewId,
			Integer environmentId) throws DAOException;

	/**
	 * This method is to inactivate multiple Views.
	 * 
	 * @param viewIds
	 *            : The list of Ids of Views which are to be inactivated.
	 * @param environmentId
	 *            : The Id of the environment to which these Views belong.
	 * @throws DAOException
	 */
	void makeViewsInactive(Collection<Integer> viewIds, Integer environmentId)
			throws DAOException;

	/**
	 * This method searches views in the database according to the specified
	 * parameters.
	 * 
	 */
	public List<FindTransfer> searchViewsToReplaceLogicText(
			Integer environmentId, SearchViewsIn searchViewsIn,
			List<Integer> componentsToSearchViewList,
			SearchCriteria searchCriteria, Date dateToSearch,
			SearchPeriod searchPeriod) throws DAOException;

    /**
     * Replace the logic text
     * @param environmentId
     * @param replacements
     * @throws DAOException
     */
    public void replaceLogicText(Integer environmentId, 
        List<FindTransfer> replacements) throws DAOException;
	
	/**
	 * This method is to remove a View and all its components permanently
	 * 
	 * from database.
	 * 
	 * @param viewId
	 *            : The Id of the View which is to be deleted.
	 * @param environmentId
	 *            : The Id of the environment to which the View belongs.
	 * @throws DAOException
	 */
	public void deleteView(Integer viewId, Integer environmentId)
			throws DAOException;

	/**
	 * Get all View folder associations
	 * @param environmentId
	 * @param id
	 * @return
	 */
    public List<ViewFolderViewAssociationTransfer> getVVFAssociation(Integer environmentId, Integer id, boolean admin);

    /**
     * 
     * @param environmentId
     * @param viewId
     * @return
     */
    public ViewQueryBean queryView(Integer environmentId, Integer viewId);
    
    /**
     * 
     * @param envId
     * @param viewId
     * @param viewSourceId
     * @return
     */
    public List<String> getViewSourceLogic(Integer envId, Integer viewId, Integer viewSourceId);

}
