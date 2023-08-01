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

import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.model.query.LogicalRecordFieldQueryBean;

/**
 * This is an interface for all the methods related to LRField metadata
 * component. All these unimplemented methods are implemented in DB2LRFieldDAO.
 * 
 */

public interface LRFieldDAO {

	/**
	 *This method is used to retrieve all the LR fields belonging to the
	 * specified logical record.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which the Logical Record
	 *            belongs.
	 * @param logicalRecordId
	 *            : The Id of the Logical Record to which the LR Fields belong.
	 * @return A List of LRFieldTransfer
	 * @throws DAOException
	 */
	List<LRFieldTransfer> getLRFields(Integer environmentId,
			Integer logicalRecordId) throws DAOException;

	/**
	 * Retrieve the LRField with the specified ID. The key information may or
	 * may not be retrieved depending on the boolean passed.
	 * 
	 * @param environmentId
	 *            the ID of the environment the LRField belongs to
	 * @param lrFieldId
	 *            the ID of the required LRField
	 * @param retrieveKeyInfo
	 *            : A Boolean which specifies whether or not the information
	 *            about the primary key is to be retrieved.
	 * @return an LRFieldTransfer
	 * @throws DAOException
	 */
	LRFieldTransfer getLRField(Integer environmentId, Integer lrFieldId,
			Boolean retrieveKeyInfo) throws DAOException;

	/**
	 * This method is called from the respective model class. This later calls
	 * respective method either to create or update a LRField.
	 * 
	 * @param lrFieldTransfer
	 *            : The transfer objects whose parameters are set with the
	 *            values received from the UI.
	 * @return The transfer objects whose value are set according to the fields
	 *         of created or updated LR Field in LRFIELDATTR, LRFIELD
	 *         table.
	 * @throws DAOException
	 */
	LRFieldTransfer persistLRField(LRFieldTransfer lrFieldTransfer)
			throws DAOException;

	/**
	 * This method is called from the respective model class. This later calls
	 * respective method either to create or update a list of LRFields related
	 * to a particular Logical Records.
	 * 
	 * @param lrFieldTransfer
	 *            : The list of transfer objects for LRField whose parameters
	 *            are set with the values received from the UI.
	 * @return The list of transfer objects whose value are set according to the
	 *         fields of created or updated LR Field in LRFIELDATTR,
	 *         LRFIELD,table.
	 * @throws DAOException
	 */
	List<LRFieldTransfer> persistLRField(List<LRFieldTransfer> lrFieldTransfer)
			throws DAOException;

	/**
	 * This method is used to remove a LR Field with the specified Id.
	 * 
	 * @param fieldIds
	 *            : The list of ids of the LR Fields which are to be removed.
	 * @param environmentId
	 *            : The id of the environment in which the LR Fields exist.
	 * @throws DAOException
	 *             It throws an exception if no LR Field is there with the
	 *             specified Id.
	 */
	void removeLRField(List<Integer> fieldIds, Integer environmentId)
			throws DAOException;

	List<LogicalRecordFieldQueryBean> queryAllLogicalRecordFields(
			SortType sortType, Integer environmentId, Integer logicalRecordId)
			throws DAOException;

	List<LogicalRecordFieldQueryBean> queryAllLogicalRecordFields(
			SortType sortType, Integer environmentId) throws DAOException;

	List<LogicalRecordFieldQueryBean> queryLRFields(SortType sortType,
			Integer environmentId) throws DAOException;

	/**
	 * This method is used to retrieve all the LR fields in a particular
	 * environment
	 * 
	 * @param environmentId
	 *            : The Id of the environment
	 * @param ids
	 *            : The list of Ids whose LR Fields are to be retrieved
	 * @return A List of LRFieldTransfer
	 * @throws DAOException
	 */
	List<LRFieldTransfer> getLRFields(Integer currentEnvironmentId,
			List<Integer> ids) throws DAOException;

	/**
	 * @return the next LRField identifier
	 */
    Integer getNextKey();

    public Integer getLRField(Integer environID, Integer sourceLRID, String fieldName);

	Integer getLookupLRField(Integer environID, int lookupid,
			String targetFieldName);

	Map<String, Integer> getFields(int lrid, Integer environID);

}
