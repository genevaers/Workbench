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
import com.ibm.safr.we.data.transfer.ControlRecordTransfer;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.query.ControlRecordQueryBean;

/**
 *This is an interface for all the methods related to Control Record metadata
 * component. All these unimplemented methods are implemented in
 * DB2ControlRecordDAO.
 * 
 */
public interface ControlRecordDAO {
	/**
	 * This method is used to retrieve a Control Record with the specified Id.
	 * 
	 * @param id
	 *            : This is the Id of the Control Record which is to be
	 *            retrieved.
	 * @param environmentId
	 *            : This is the Id of the Environment to which the Control
	 *            Record belongs.
	 * @return Control Record transfer objects which contains the details of all
	 *         the fields for the Control Record retrieved from CONTROLREC
	 *         table.
	 * @throws DAOException
	 *             It throws an exception if no Control Record is there with the
	 *             specified Id.
	 */
	ControlRecordTransfer getControlRecord(Integer id, Integer environmentId)
			throws DAOException;

	/**
	 * This method is called from the respective model class. This later calls
	 * respective method either to create or update a Control Record.
	 * 
	 * @param controlRecordTransfer
	 *            : The transfer objects whose parameters are set with the
	 *            values received from the UI.
	 * @return The transfer objects whose value are set according to the fields
	 *         of created or updated Control Record in CONTROLREC table.
	 * @throws DAOException
	 *             if any kind of SQLException occur.
	 * @throws SAFRNotFoundException
	 *             if no rows are updated.
	 */
	ControlRecordTransfer persistControlRecord(
			ControlRecordTransfer controlRecordTransfer) throws DAOException,
			SAFRNotFoundException;

	/**
	 * This method is used to delete an Control Record from the CONTROLREC
	 * table.
	 * 
	 * @param id
	 *            : The Id of the Control Record which is to be deleted.
	 * @param environmentId
	 *            : The Id of the environment to which the control record
	 *            belongs.
	 * @throws DAOException
	 *             It throws an exception if no Control Record is there with the
	 *             specified Id.
	 */
	void removeControlRecord(Integer id, Integer environmentId)
			throws DAOException;

	/**
	 * This method is used to retrieve a Control Record with the specified name.
	 * 
	 * @param controlRecordName
	 *            : The name of the Control Record which is to be retrieved.
	 * @param controlRecordId
	 *            : The Id of Control Record which is to be retrieved.
	 * @param environmentId
	 *            : The Id of the environment to which the Control Record
	 *            belongs.
	 * @return a Control Record transfer objects which contains the details of
	 *         all the fields for the Control Record retrieved from CONTROLREC
	 *         table for the specified name.
	 * @throws DAOException
	 *             It throws an exception if no Control Record is there with the
	 *             specified Id.
	 */
	ControlRecordTransfer getDuplicateControlRecord(String controlRecordName,
			Integer controlRecordId, Integer environmentId) throws DAOException;

	/**
	 * This method is used to retrieve all the Control Records from CONTROLREC
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of Control
	 * Records.
	 * 
	 * @param environmentId
	 *            : the Id of the environment to which the control records
	 *            belongs.
	 *@param sortType
	 *            : the type of sorting needed on the list.
	 * @return A list of all ControlRecordBean objects.
	 * @throws DAOException
	 */
	List<ControlRecordQueryBean> queryAllControlRecords(Integer environmentId,
			SortType sortType) throws DAOException;

	/**
	 * This method is to retrieve all the Views in which the specified Control
	 * Record is used.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which the control records
	 *            belongs.
	 * @param controlRecordId
	 *            : The Id of the Control Record for which dependent Views are
	 *            to be retrieved.
	 * @return A List of DependentComponentTransfer objects.
	 * @throws DAOException
	 */
	List<DependentComponentTransfer> getControlRecordViewDependencies(
			Integer environmentId, Integer controlRecordId) throws DAOException;

	void deleteAllControlRecordsFromEnvironment(Integer environmentId) throws DAOException;
}
