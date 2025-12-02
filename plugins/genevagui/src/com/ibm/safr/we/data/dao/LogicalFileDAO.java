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
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.LogicalFileTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;

/**
 * This is an interface for all the methods related to Logical File metadata
 * component. All these unimplemented methods are implemented in
 * DB2LogicalFileDAO.
 * 
 */
public interface LogicalFileDAO {
	/**
	 * This method is used to retrieve a Logical File with the specified Id.
	 * 
	 * @param id
	 *            : This is the ID of the Logical File which is to be retrieved.
	 * @param environmentId
	 *            : The Id of the environment to which the Logical file belongs.
	 * @return A Logical File transfer objects which contains the details of all
	 *         the fields for the Logical File retrieved from LOGFILE table.
	 * @throws DAOException
	 *             It throws an exception if no Logical File is there with the
	 *             specified Id.
	 */
    LogicalFileTransfer getLogicalFile(Integer id, Integer environmentId)
            throws DAOException;
    LogicalFileTransfer getLogicalFile(String name, Integer environmentId)
            throws DAOException;

	/**
	 * This method is called from the respective model class. This later calls
	 * respective method either to create or update a Logical File.
	 * 
	 * @param logicalFileTransfer
	 *            : The transfer objects whose parameters are set with the
	 *            values received from the UI.
	 * @return The transfer objects whose value are set according to the fields
	 *         of created or updated Logical File in LOGFILE table.
	 * @throws DAOException
	 *             if any SQLException occurs.
	 * @throws SAFRNotFoundException
	 *             if no rows are updated.
	 */
	LogicalFileTransfer persistLogicalFile(
			LogicalFileTransfer logicalFileTransfer) throws DAOException,
			SAFRNotFoundException;

	/**
	 * This method is used to remove a Logical File with the specified Id
	 * 
	 * @param Id
	 *            : The id of the logical file which is to be removed.
	 * @param environmentId
	 *            : The id of the environment in which the logical file exists.
	 * @throws DAOException
	 *             It throws an exception if no Logical File is there with the
	 *             specified Id.
	 */
	void removeLogicalFile(Integer id, Integer environmentId)
			throws DAOException;

	/**
	 * This method is used to retrieve a Logical File with the specified name.
	 * 
	 * @param logicalFileName
	 *            : The name of the logical file to be searched.
	 * @param logicalFileId
	 *            : The Id of the Logical file.
	 * @param environmentId
	 *            : The Id of the environment to which the logical file belongs.
	 * @return: A Logical File transfer objects which contains the details of
	 *          all the fields for the Logical File retrieved from LOGFILE
	 *          table for the specified name.
	 * @throws DAOException
	 *             It throws an exception if no Logical File is there with the
	 *             specified Id.
	 */
	LogicalFileTransfer getDuplicateLogicalFile(String logicalFileName,
			Integer logicalFileId, Integer environmentId) throws DAOException;

	/**
	 * This method is used to retrieve all the Logical Files from LOGFILE
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of Logical
	 * Files.
	 * 
	 * @param environmentId
	 *            : The id of the environment to which the logical files
	 *            belongs.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all logicalFileBean objects.
	 * @throws DAOException
	 */
	List<LogicalFileQueryBean> queryAllLogicalFiles(Integer environmentId,
			SortType sortType) throws DAOException;

	/**
	 * Returns the physical files associated with this Logical File as list of
	 * FileAssociationTransfer objects.
	 * 
	 * @param id
	 *            : the ID of the Logical File
	 * @param environmentId
	 *            : the ID of the environment to which the logical file belongs
	 * @return a List of FileAssociationTransfer objects
	 * @throws DAOException
	 */
	List<FileAssociationTransfer> getAssociatedPhysicalFiles(Integer id,
			Integer environmentId) throws DAOException;

	/**
	 * Returns the Logical Records associated with this Logical File as list of
	 * ComponentAssociationTransfer objects.
	 * 
	 * @param id
	 *            : the ID of the Logical File
	 * @param environmentId
	 *            : the ID of the environment to which the logical file belongs
	 * @return A List of ComponentAssociationTransfer objects
	 * @throws DAOException
	 */
	List<ComponentAssociationTransfer> getAssociatedLogicalRecords(Integer id,
			Integer environmentId) throws DAOException;

	/**
	 * This method is to retrieve all the possible Physical Files which can be
	 * associated with this Logical File.
	 * 
	 * @param environmentId
	 *            : the ID of the environment to which the Logical File belongs
	 * @param notInParam
	 *            : A comma delimited String which contains the IDs of Physical
	 *            Files which are already associated with the Logical File
	 * @return A list of PhysicalFileQueryBean objects.
	 * @throws DAOException
	 */
	List<PhysicalFileQueryBean> queryPossiblePFAssociations(
			Integer environmentId, List<Integer> notInParam) throws DAOException;

	/**
	 * This method is called from the respective model class. This later calls
	 * respective method either to create or update an association with a
	 * Physical File.
	 * 
	 * @param fileAssociationTransfer
	 *            : A list of association objects which are to be created or
	 *            updated in LFPFASSOC.
	 * @param logicalFileId
	 *            : The ID of the Logical File.
	 * @return A list of association objects which are created or updated in
	 *         LFPFASSOC.
	 * @throws DAOException
	 */
	List<FileAssociationTransfer> persistAssociatedPFs(
			List<FileAssociationTransfer> fileAssociationTransfer,
			Integer logicalFileId) throws DAOException;

	/**
	 * This method is to delete multiple Physical File associations in LFPFASSOC
	 * 
	 * @param environmentId
	 *            : the ID of the environment to which the Logical File belongs
	 * @param deletionIds
	 *            A comma delimited String which contains the IDs of
	 *            associations which are to be deleted
	 * @throws DAOException
	 */
	void deleteAssociatedPFs(Integer environmentId, List<Integer> deletionIds)
			throws DAOException;

	/**
	 * This method finds cases where the LF has a target dependency on a lookup
	 * with no lookup exit
	 * @param environmentId
	 * @param lfId
	 * @return
	 * @throws DAOException
	 */
    List<DependentComponentTransfer> getAssociatedLookupDependencies(
        Integer environmentId, Integer lfId) throws DAOException;
	
	/**
	 * This method is used to get the list of dependencies of LF-PF association
	 * to a view.
	 * 
	 * @param environmentId
	 *            : the ID of the environment to which the Logical File belongs.
	 * @param associationsLFPFId
	 *            : The Id of the association between Logical File and Physical
	 *            File.
	 * @return a list of dependent component transfer objects.
	 * @throws DAOException
	 */
	List<DependentComponentTransfer> getAssociatedPFViewDependencies(
			Integer environmentId, Integer associationsLFPFId)
			throws DAOException;

	/**
	 * This method is used to get the list of dependencies of LF-PF association
	 * to a view which are newly created and not retrieved in previous
	 * dependency checks.
	 * 
	 * @param environmentId
	 *            : The Id of the Environment to which the Logical File belongs.
	 * @param LFPFAssociationIds
	 *            : List of Ids of the associations between LF and PF.
	 * @param exceptionList
	 *            : List of association Ids which have been retrieved in
	 *            previous dependency checks.
	 * @return: A Map which has association id as the key and the list of
	 *          dependent component transfer objects as value.
	 * @throws DAOException
	 */
	Map<Integer, List<DependentComponentTransfer>> getAssociatedPFViewDependencies(
			Integer environmentId, List<Integer> LFPFAssociationIds,
			List<Integer> exceptionList) throws DAOException;

	/**
	 * Get the LF-PF associations in the specified environment.
	 * 
	 * @param environmentId
	 *            : The Id of the Environment.
	 * @return List<FileAssociationTransfer>
	 * @throws DAOException
	 */
	List<FileAssociationTransfer> getLFPFAssociations(Integer environmentId)
			throws DAOException;

	/**
	 * Get the LF-PF association with the specified association Id. Treats the
	 * Logical File as the associating object and the Physical File as the
	 * associated object.
	 * 
	 * @param associationId
	 *            : The Id of thr LF-PF association.
	 * @param environmentId
	 *            : The Id of the Environment.
	 * @return FileAssociationTransfer
	 * @throws DAOException
	 */
	FileAssociationTransfer getLFPFAssociation(Integer associationId,
			Integer environmentId) throws DAOException;

	/**
	 * Returns the file association for the specified LF and PF ids in the specified
	 * environment if one exists, otherwise returns null. Note, LF id and PF id make
	 * up a composite unique key for file associations within an environment.
	 *
	 * @param logicalFileId the LF id
	 * @param physicalFileId the PF id
	 * @param environmentId the Environment id
	 * @return a FileAssociationTransfer object or null
	 * @throws DAOException on unexpected DB error
	 */
	FileAssociationTransfer getLFPFAssociation(Integer logicalFileId,
			Integer physicalFileId, Integer environmentId) throws DAOException;
	
	/**
	 * This method is to get all the Logical Records which are associated only
	 * to the specified Logical File.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which Logical File belongs.
	 * @param logicalFileId
	 *            : The Id of the Logical File.
	 * @return A list of DependentComponentTransfer objects.
	 * @throws DAOException
	 */
	public List<DependentComponentTransfer> getAssociatedLogicalRecordsWithOneAssociatedLF(
			Integer environmentId, Integer logicalFileId) throws DAOException;

	/**
	 * This method is to get all the Views and Lookup Paths in which the
	 * specified Logical File is referred.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which Logical File belongs.
	 * @param logicalFileId
	 *            : The Id of the Logical File.
	 * @return A Map with ComponentType as key and List of
	 *         DependentComponentTransfer as value which contains dependencies
	 *         of Logical File.
	 * @throws DAOException
	 */
	Map<ComponentType, List<DependentComponentTransfer>> getDependencies(
			Integer environmentId, Integer logicalFileId) throws DAOException;
	
	Integer getLFPFAssocID(int environid, String lfName, String pfName);


}
