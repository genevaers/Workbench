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
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;

/**
 * This is an interface for all the methods related to Physical File metadata
 * component. All these unimplemented methods are implemented in
 * DB2PhysicalFileDAO.
 * 
 */
public interface PhysicalFileDAO {

	/**
	 * This method is used to retrieve a physical file with the specified Id.
	 * 
	 * @param id
	 *            : This is the ID of the Physical File which is to be
	 *            retrieved.
	 * @param environmentId
	 *            : The Id of the environment to which the Physical file
	 *            belongs.
	 * @return A Physical File transfer objects which contains the details of
	 *         all the fields for the Physical File retrieved from
	 *         PHYFILE table.
	 * @throws DAOException
	 *             It throws an exception if no Physical File is there with the
	 *             specified Id.
	 */
	PhysicalFileTransfer getPhysicalFile(Integer id, Integer environmentId)
			throws DAOException;

	PhysicalFileTransfer getPhysicalFile(String name, Integer environmentId)
			throws DAOException;

	/**
	 * This method is used to retrieve all the Physical Files from
	 * PHYFILE table with only selected columns. As the number of columns
	 * selected is less, the performance improves while populating the list of
	 * Physical Files.
	 * 
	 * @param environmentId
	 *            : The id of the environment to which the physical files
	 *            belongs.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all PhysicalFileBean objects.
	 * @throws DAOException
	 */
	List<PhysicalFileQueryBean> queryAllPhysicalFiles(Integer environmentId,
			SortType sortType) throws DAOException;

	/**
	 * This method is called from the respective model class. This later calls
	 * respective method either to create or update a Physical File.
	 * 
	 * @param physicalFileTransfer
	 *            : The transfer objects whose parameters are set with the
	 *            values received from the UI.
	 * @return The transfer objects whose value are set according to the fields
	 *         of created or updated Physical File in PHYFILE table.
	 * @throws DAOException
	 *             if any SQLException occurs.
	 * @throws SAFRNotFoundException
	 *             if no rows are updated.
	 */
	PhysicalFileTransfer persistPhysicalFile(
			PhysicalFileTransfer physicalFileTransfer) throws DAOException,
			SAFRNotFoundException;

	/**
	 * This method is used to remove a Physical File with the specified Id
	 * 
	 * @param Id
	 *            : The id of the physical file which is to be removed.
	 * @param environmentId
	 *            : The id of the environment in which the physical file exists.
	 * @throws DAOException
	 *             It throws an exception if no Physical File is there with the
	 *             specified Id.
	 */
	void removePhysicalFile(Integer Id, Integer environmentId)
			throws DAOException;

	/**
	 */
	PhysicalFileTransfer getDuplicatePhysicalFile(String physicalFileName,
			Integer physicalFileId, Integer environmentId) throws DAOException;

	/**
	 * This method returns the logical files associated with this physical file
	 * as list of FileAssociationTransfer objects.
	 * 
	 * @param id
	 *            :The ID of the physical file
	 * @param environmentId
	 *            :The ID of the environment to which the physical file belongs.
	 * @return A List of FileAssociationTransfer objects
	 * @throws DAOException
	 *             It throws an exception if no Physical File is there with the
	 *             specified Id.
	 */
	List<FileAssociationTransfer> getAssociatedLogicalFiles(Integer id,
			Integer environmentId) throws DAOException;

	/**
	 * This method is to retrieve all the views in which the specified Physical
	 * File is used.
	 * 
	 * @param environmentId
	 *            : The ID of the environment to which the physical file belongs
	 * @param physicalFileId
	 *            : The ID of the physical file
	 * @return A list of DependentComponentTransfer objects.
	 * @throws DAOException
	 */
	List<DependentComponentTransfer> getViewDependencies(Integer environmentId,
			Integer physicalFileId) throws DAOException;

	/**
	 * This method is to retrieve the Logical Files which are associated with
	 * the specified Physical File and has no other Physical File associated
	 * with them.
	 * 
	 * @param environmentId
	 *            : The Id of the environment.
	 * @param physicalFileId
	 *            : The Id of the Physical File.
	 * @return A list of DependentComponentTransfer objects.
	 * @throws DAOException
	 */
	List<DependentComponentTransfer> getAssociatedLogicalFilesWithOneAssociatedPF(
			Integer environmentId, Integer physicalFileId) throws DAOException;

}
