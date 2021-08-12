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
import com.ibm.safr.we.constants.ExportElementType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.XMLTableDataTransfer;

/**
 *This is an interface for all the methods related to exporting a metadata
 * component. All these unimplemented methods are implemented in DB2ExportDAO.
 * 
 */
public interface ExportDAO {

	/**
	 * This method is to get all the dependencies of a metadata component in
	 * terms of other metadata components. 
	 * 
	 * @param compType
	 *            : The type of component whose dependencies are to be found. It
	 *            can be one of Physical File, Logical File, Logical Record,
	 *            Lookup Path or View.
	 * @param componentId
	 *            : The Id of the component whose dependencies are to be found.
	 * @param environmentId
	 *            : The Id of the environment to which component belongs.
	 * @return A Map which has Component Type as key, and value is a List of
	 *         DependentComponentTransfer objects with all the components
	 *         dependent on the specified component.
	 * @throws DAOException
	 */
	Map<ComponentType, List<DependentComponentTransfer>> getComponentDependencies(
			ComponentType compType, Integer componentId, Integer environmentId)
			throws DAOException;

	/**
	 * This method is to retrieve all the data from PHYFILE for one or
	 * more Physical File(s). 
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which Physical File(s) belong.
	 * @param physicalFileIds
	 *            : A List of the Ids of the Physical File(s) whose data is to
	 *            be retrieved.
	 * @return A Map which has ExportElementType as key. Value is another Map
	 *         with record number as key and value is list of
	 *         XMLTableDataTrasfer objects.
	 * @throws DAOException
	 */
	Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getPhysicalFileData(
			Integer environmentId, List<Integer> physicalFileIds)
			throws DAOException;

	/**
	 * This method is to retrieve all the data from LOGFILE for Logical
	 * File(s) and all their Physical File associations from LFPFASSOC
	 * . 
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which Logical File(s) belong.
	 * @param logicalFileIds
	 *            : A List of the Ids of the Logical File(s) whose data is to be
	 *            retrieved.
	 * @return A Map which has ExportElementType as key. Value is another Map
	 *         with record number as key and value is list of
	 *         XMLTableDataTrasfer objects.
	 * @throws DAOException
	 */
	Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getLogicalFileData(
			Integer environmentId, List<Integer> logicalFileIds)
			throws DAOException;

	/**
	 * This method is to retrieve all the data from EXIT for User Exit
	 * Routine(s). 
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which User Exit Routine(s)
	 *            belong.
	 * @param userExitIds
	 *            : A List of the Ids of the User Exit Routine(s) whose data is
	 *            to be retrieved.
	 * @return A Map which has ExportElementType as key. Value is another Map
	 *         with record number as key and value is list of
	 *         XMLTableDataTrasfer objects.
	 * @throws DAOException
	 */
	Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getUserExitRoutineData(
			Integer environmentId, List<Integer> userExitIds)
			throws DAOException;

	/**
	 * This method is to retrieve all the data from LOGREC and other related
	 * tables for Logical Record(s) and all their Logical File associations from
	 * LRLFASSOC. 
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which Logical Record(s) belong.
	 * @param logicalRecordIds
	 *            : A List of the Ids of the Logical Records whose data is to be
	 *            retrieved.
	 * @return A Map which has ExportElementType as key. Value is another Map
	 *         with record number as key and value is list of
	 *         XMLTableDataTrasfer objects.
	 * @throws DAOException
	 */
	Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getLogicalRecordData(
			Integer environmentId, List<Integer> logicalRecordIds)
			throws DAOException;

	/**
	 * This method is to retrieve all the data from LOOKUP and related tables
	 * for Lookup Path(s). 
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which Lookup Path(s) belong.
	 * @param lookupPathIds
	 *            : A List of the Ids of the Lookup Path(s) whose data is to be
	 *            retrieved.
	 * @return A Map which has ExportElementType as key. Value is another Map
	 *         with record number as key and value is list of
	 *         XMLTableDataTrasfer objects.
	 * @throws DAOException
	 */
	Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getLookupPathData(
			Integer environmentId, List<Integer> lookupPathIds)
			throws DAOException;

	/**
	 * This method is to retrieve all the data from VIEW and related tables
	 * for View(s). 
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which View(s) belong.
	 * @param viewIds
	 *            : A List of the Ids of the View(s) whose data is to be
	 *            retrieved.
	 * @return A Map which has ExportElementType as key. Value is another Map
	 *         with record number as key and value is list of
	 *         XMLTableDataTrasfer objects.
	 * @throws DAOException
	 */
	Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getViewData(
			Integer environmentId, List<Integer> viewIds) throws DAOException;

    /**
     * This method is to retrieve all the data from View Folder and related tables
     * for View Folders(s). 
     * 
     * @param environmentId
     *            : The Id of the environment to which View Folder(s) belong.
     * @param viewFolderIds
     *            : A List of the Ids of the ViewFolder(s) whose data is to be
     *            retrieved.
     * @return A Map which has ExportElementType as key. Value is another Map
     *         with record number as key and value is list of
     *         XMLTableDataTrasfer objects.
     * @throws DAOException
     */
    Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getViewFolderData(
            Integer environmentId, List<Integer> viewFolderIds) throws DAOException;
	
	/**
	 * This method is to retrieve all the data from CONTROLREC for Control
	 * Record(s). 
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which Control Record(s) belong.
	 * @param controlRecordIds
	 *            : A List of the Ids of the Control Record(s) whose data is to
	 *            be retrieved.
	 * @return A Map which has ExportElementType as key. Value is another Map
	 *         with record number as key and value is list of
	 *         XMLTableDataTrasfer objects.
	 * @throws DAOException
	 */
	Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> getControlRecordData(
			Integer environmentId, List<Integer> controlRecordIds)
			throws DAOException;

}
